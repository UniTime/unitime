/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.dataexchange;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Element;
import org.unitime.timetable.gwt.shared.TaskInterface.ExecutionStatus;
import org.unitime.timetable.model.PeriodicTask;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.ScriptParameter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TaskExecution;
import org.unitime.timetable.model.TaskParameter;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;

public class PeriodicTaskImport extends BaseImport {
	protected Session iSession;
	protected SimpleDateFormat iDateFormat;
	
	@Override
	public void loadXml(Element root) throws Exception {
		if (!root.getName().equalsIgnoreCase("tasks")) {
        	throw new Exception("Given XML file is not periodic tasks load file.");
        }
		try {
			beginTransaction();
			
            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");
            iDateFormat = new SimpleDateFormat(root.attributeValue("dateFormat", "yyyy/MM/dd"), Locale.US);
            iSession = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if (iSession == null)
                throw new Exception("No session found for the given campus, year, and term.");
			
            boolean incremental = "true".equalsIgnoreCase(root.attributeValue("incremental", "true"));
	        if (incremental)
	        	info("Incremental mode.");
	        
	        Map<String, PeriodicTask> tasks = new HashMap<String, PeriodicTask>();
	        if (!incremental) {
            	info("Deleting all existing periodic tasks...");
            	for (PeriodicTask t: getHibSession().createQuery("select t from PeriodicTask t where t.session.uniqueId=:sessionId", PeriodicTask.class).
                	setParameter("sessionId", iSession.getUniqueId()).list()) {
            		getHibSession().remove(t);
            	}
            } else {
    	        for (PeriodicTask task: getHibSession().createQuery(
    				"from PeriodicTask where session.uniqueId = :sessionId order by name", PeriodicTask.class)
    	        	.setParameter("sessionId", iSession.getUniqueId()).list()) {
    	        	tasks.put(task.getName(), task);
    	        }
            }
	        
	        Element dates = root.element("dates");
	        Map<String, String> dateMapping = new HashMap<String, String>();
	        if (dates != null) {
	        	for (Iterator i = dates.elementIterator("date"); i.hasNext(); ) {
	                Element dateEl = (Element) i.next();
	                dateMapping.put(dateEl.attributeValue("name"), dateEl.getText());
	        	}
	        }

	        info("Importing tasks...");
	        for (Iterator i = root.elementIterator("task"); i.hasNext(); ) {
                Element taskElement = (Element) i.next();
                importTask(taskElement, tasks.get(taskElement.attributeValue("name")), dateMapping);
	        }
			
			commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
	}
	
	protected PeriodicTask importTask(Element taskEl, PeriodicTask task, Map<String, String> dateMapping) {
		Map<Integer, TaskExecution> execs = new HashMap<Integer, TaskExecution>();
		Map<String, TaskParameter> params = new HashMap<String, TaskParameter>();
		if (task == null) {
        	task = new PeriodicTask();
        	task.setName(taskEl.attributeValue("name"));
        	task.setParameters(new HashSet<TaskParameter>());
        	task.setSchedule(new HashSet<TaskExecution>());
        	task.setSession(iSession);
		} else {
			for (TaskParameter param: task.getParameters())
				params.put(param.getName(), param);
			for (TaskExecution exec: task.getSchedule())
				execs.put(exec.getExecutionDate() * 288 + exec.getExecutionPeriod(), exec);
		}
		String owner = taskEl.attributeValue("owner");
		if (owner != null && !owner.isEmpty())
			task.setOwner(TimetableManager.findByExternalId(owner));
		if (task.getOwner() == null)
			task.setOwner(getManager());
		task.setScript(getHibSession().createQuery("from Script where name = :name", Script.class)
				.setParameter("name", taskEl.attributeValue("script"))
				.setMaxResults(1).uniqueResult());
		Element emailEl = taskEl.element("email");
		task.setEmail(emailEl == null ? null : emailEl.getText());
		Element fileEl = taskEl.element("file");
		task.setInputFile(fileEl == null ? null : Base64.getDecoder().decode(fileEl.getText()));
		if (task.getUniqueId() == null)
			getHibSession().persist(task);
		else
			getHibSession().merge(task);
		
		Date ts = new Date();
		
		for (Iterator i = taskEl.elementIterator("parameter"); i.hasNext(); ) {
			Element paramEl = (Element)i.next();
			TaskParameter param = params.remove(paramEl.attributeValue("name"));
			if (param == null) {
				param = new TaskParameter();
				param.setName(paramEl.attributeValue("name"));
				param.setTask(task); task.addToParameters(param);
			}
			param.setValue(paramEl.getText());
			if (param.getValue() != null && !param.getValue().isEmpty()) {
				ScriptParameter parameter = null;
	        	for (ScriptParameter p: task.getScript().getParameters()) {
	        		if (param.getName().equals(p.getName())) {
	        			parameter = p; break;
	        		}
	        	}
	        	if (parameter != null) {
	        		for (SavedHQL.Option option: SavedHQL.Option.values())
	    				if (parameter.getType().equalsIgnoreCase(option.name())) {
	    					param.setValue(option.uglifyValue(getHibSession(), iSession.getUniqueId(), param.getValue()));
	    					break;
	    				}
	        	}
			}
		}
		for (TaskParameter param: params.values()) {
			task.getParameters().remove(param);
			param.setTask(null);
			getHibSession().remove(param);
		}
			
		
		for (Iterator i = taskEl.elementIterator("schedule"); i.hasNext(); ) {
			Element scheduleEl = (Element)i.next();
			int period = time2slot(Integer.valueOf(scheduleEl.attributeValue("startTime", "0000")));
			String days = scheduleEl.attributeValue("days", "");
			if (dateMapping.containsKey(days)) days = dateMapping.get(days);
			int dayCode = days2code(days);
			Date sd = null, ed = null;
			try {
				String startDate = scheduleEl.attributeValue("fromDate", scheduleEl.attributeValue("date"));
				if (dateMapping.containsKey(startDate)) startDate = dateMapping.get(startDate);
				sd = iDateFormat.parse(startDate);
			} catch (ParseException e) {
				warn("Failed to parse date " + scheduleEl.attributeValue("fromDate", scheduleEl.attributeValue("date")));
				continue;
			}
			int i1 = DateUtils.daysBetween(iSession.getSessionBeginDateTime(), sd);
			i1 += Integer.parseInt(scheduleEl.attributeValue("fromOffset", scheduleEl.attributeValue("offset", "0")));
			try {
				String endDate = scheduleEl.attributeValue("toDate", scheduleEl.attributeValue("date"));
				if (dateMapping.containsKey(endDate)) endDate = dateMapping.get(endDate);
				ed = iDateFormat.parse(endDate);
			} catch (ParseException e) {
				warn("Failed to parse date " + scheduleEl.attributeValue("toDate", scheduleEl.attributeValue("date"))); 
			}
			int i2 = DateUtils.daysBetween(iSession.getSessionBeginDateTime(), ed);
			i2 += Integer.parseInt(scheduleEl.attributeValue("toOffset", scheduleEl.attributeValue("offset", "0")));
			for (int j = i1; j <= i2; j++) {
				if (dayCode != 0 && ((Constants.DAY_CODES[getDayOfWeek(iSession, j)] & dayCode) == 0)) continue;
				TaskExecution exec = execs.remove(j * 288 + period);
				if (exec == null) {
					exec = new TaskExecution();
					exec.setCreatedDate(ts);
					exec.setExecutionDate(j);
					exec.setExecutionPeriod(period);
					exec.setScheduledDate(getScheduleDate(iSession, j, period));
					exec.setExecutionStatus(ExecutionStatus.CREATED.ordinal());
					exec.setTask(task);
					task.getSchedule().add(exec);
				}
			}
		}
		for (TaskExecution exec: execs.values()) {
			if (exec.getExecutionStatus() == ExecutionStatus.CREATED.ordinal()) {
				task.getSchedule().remove(exec);
				exec.setTask(null);
				getHibSession().remove(exec);
			}
		}
		
		getHibSession().merge(task);
		return task;
	}
	
	public int days2code(String daysOfWeek) {
    	int code = 0;
		String tmpDays = daysOfWeek;
		if(tmpDays.contains("Th")){
			code += Constants.DAY_CODES[Constants.DAY_THU];
			tmpDays = tmpDays.replace("Th", "..");
		}
		if(tmpDays.contains("R")){
			code += Constants.DAY_CODES[Constants.DAY_THU];
			tmpDays = tmpDays.replace("R", "..");
		}
		if (tmpDays.contains("Su")){
			code += Constants.DAY_CODES[Constants.DAY_SUN];
			tmpDays = tmpDays.replace("Su", "..");
		}
		if (tmpDays.contains("U")){
			code += Constants.DAY_CODES[Constants.DAY_SUN];
			tmpDays = tmpDays.replace("U", "..");
		}
		if (tmpDays.contains("M")){
			code += Constants.DAY_CODES[Constants.DAY_MON];
			tmpDays = tmpDays.replace("M", ".");
		}
		if (tmpDays.contains("T")){
			code += Constants.DAY_CODES[Constants.DAY_TUE];
			tmpDays = tmpDays.replace("T", ".");
		}
		if (tmpDays.contains("W")){
			code += Constants.DAY_CODES[Constants.DAY_WED];
			tmpDays = tmpDays.replace("W", ".");
		}
		if (tmpDays.contains("F")){
			code += Constants.DAY_CODES[Constants.DAY_FRI];
			tmpDays = tmpDays.replace("F", ".");
		}
		if (tmpDays.contains("S")){
			code += Constants.DAY_CODES[Constants.DAY_SAT];
			tmpDays = tmpDays.replace("S", ".");
		}
		return code;
    }
	
	public int time2slot(int time) {
		int hour = time/100;
		int min = time%100;
		if ((min % Constants.SLOT_LENGTH_MIN) != 0){
			min = min - (min % Constants.SLOT_LENGTH_MIN);
		}
		return (hour * 60 + min - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
	}
	
	public Date getScheduleDate(Session session, int date, int slot) {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(session.getSessionBeginDateTime());
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.DAY_OF_YEAR, date);
		c.add(Calendar.MINUTE, slot * 5);
		return c.getTime();
	}
	
	public int getDayOfWeek(Session session, int date) {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(session.getSessionBeginDateTime());
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.DAY_OF_YEAR, date);
		switch(c.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.MONDAY: return Constants.DAY_MON;
			case Calendar.TUESDAY: return Constants.DAY_TUE;
			case Calendar.WEDNESDAY: return Constants.DAY_WED;
			case Calendar.THURSDAY: return Constants.DAY_THU;
			case Calendar.FRIDAY: return Constants.DAY_FRI;
			case Calendar.SATURDAY: return Constants.DAY_SAT;
			case Calendar.SUNDAY: return Constants.DAY_SUN;
			default: return 0;
		}
	}
}
