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

import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMCDATA;
import org.unitime.timetable.model.PeriodicTask;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.ScriptParameter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TaskExecution;
import org.unitime.timetable.model.TaskParameter;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

public class PeriodicTaskExport extends BaseExport {
	protected static Formats.Format<Date> sDateFormat = Formats.getDateFormat("yyyy/MM/dd");
	protected static Formats.Format<Number> sNumberFormat = Formats.getNumberFormat("0000");
	
	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			document.addDocType("tasks", "-//UniTime//UniTime Periodic Tasks DTD/EN", "http://www.unitime.org/interface/Tasks.dtd");
			
			
			Element root = document.addElement("tasks");
			root.addAttribute("created", new Date().toString());
	        root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("year", session.getAcademicYear());
	        root.addAttribute("term", session.getAcademicTerm());
	        root.addAttribute("dateFormat", sDateFormat.toPattern());
	        root.addAttribute("incremental", "false");
	        for (PeriodicTask task: getHibSession().createQuery(
					"from PeriodicTask where session.uniqueId = :sessionId order by name", PeriodicTask.class)
					.setParameter("sessionId", session.getUniqueId()).list()) {
				exportTask(getHibSession(), root.addElement("task"), task);
			}
			
			commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
	}
	
	public static Element exportTask(org.hibernate.Session hibSession, Element taskEl, PeriodicTask task) {
		taskEl.addAttribute("name", task.getName());
        if (task.getEmail() != null && !task.getEmail().isEmpty()) {
        	if (task.getEmail().indexOf('\n') >= 0)
        		taskEl.addElement("email").add(new DOMCDATA(task.getEmail()));
        	else
        		taskEl.addElement("email").setText(task.getEmail());
        }
        taskEl.addAttribute("script", task.getScript().getName());
        taskEl.addAttribute("owner", task.getOwner().getExternalUniqueId());
        if (task.getInputFile() != null && task.getInputFile().length > 0)
        	taskEl.addElement("file").setText(Base64.getEncoder().encodeToString(task.getInputFile()));
        for (TaskParameter param: task.getParameters()) {
        	ScriptParameter parameter = null;
        	for (ScriptParameter p: task.getScript().getParameters()) {
        		if (param.getName().equals(p.getName())) {
        			parameter = p; break;
        		}
        	}
        	if (parameter == null || param.getValue() == null || param.getValue().isEmpty()) {
        		taskEl.addElement("parameter").addAttribute("name", param.getName()).setText(param.getValue() == null ? "" : param.getValue());
            	continue;
        	}
        	String value = null;
        	for (SavedHQL.Option option: SavedHQL.Option.values())
				if (parameter.getType().equalsIgnoreCase(option.name())) {
					value = option.pretifyValue(hibSession, param.getValue());
					break;
				}
        	if (value != null)
        		taskEl.addElement("parameter")
        		.addAttribute("name", param.getName())
        		.setText(value);
        	else if (param.getValue().indexOf('\n') >= 0)
        		taskEl.addElement("parameter")
        		.addAttribute("name", param.getName())
        		.add(new DOMCDATA(value != null ? value : param.getValue()));
        	else
        		taskEl.addElement("parameter")
        		.addAttribute("name", param.getName())
        		.setText(param.getValue());
        }
        Set<Integer> periods = new TreeSet<Integer>();
        for (TaskExecution exec: task.getSchedule())
    		periods.add(exec.getExecutionPeriod());
        for (Integer period: periods) {
			TreeSet<Integer> dates = new TreeSet<Integer>();
			for (TaskExecution exec: task.getSchedule())
				if (exec.getExecutionPeriod().equals(period))
					dates.add(exec.getExecutionDate());
			while (!dates.isEmpty()) {
				Integer first = dates.first();
				dates.remove(first);
				Integer last = first;
				int w = getDayOfWeek(task.getSession(), first);
				int d = first;
				int dow = Constants.DAY_CODES[w];
				int daysOfWeek = 1;
				while (true) {
					do {
						d ++;
						w = (w + 1) % 7;
						if (d - first < 7 && dates.contains(d)) {
							dow += Constants.DAY_CODES[w];
							daysOfWeek ++;
						}
					} while ((dow & Constants.DAY_CODES[w]) == 0);
					if (!dates.contains(d)) break;
					dates.remove(d); 
					last = d;
				}
				Element scheduleEl = taskEl.addElement("schedule");
				scheduleEl.addAttribute("startTime", slot2time(period));
				if (dow < 127 && last - first + 1 > daysOfWeek)
					scheduleEl.addAttribute("days", dayCode2days(dow));
				if (first == last) {
					scheduleEl.addAttribute("date", sDateFormat.format(getScheduleDate(task.getSession(), first, 0)));
				} else {
					scheduleEl.addAttribute("fromDate", sDateFormat.format(getScheduleDate(task.getSession(), first, 0)));
					scheduleEl.addAttribute("toDate", sDateFormat.format(getScheduleDate(task.getSession(), last, 0)));
				}
			}
        }
		return taskEl;
	}
	
	public static Date getScheduleDate(Session session, int date, int slot) {
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
	
	public static int getDayOfWeek(Session session, int date) {
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
	
	protected static String slot2time(int slot) {
		int min = slot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
		return sNumberFormat.format((min / 60) * 100 + (min % 60));
	}

	protected static String dayCode2days(int dayCode) {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<Constants.DAY_CODES.length;i++)
            if ((dayCode & Constants.DAY_CODES[i])!=0)
                sb.append(Constants.DAY_NAMES_SHORT[i]);
        return sb.toString(); 
    }
}
