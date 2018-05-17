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
package org.unitime.timetable.server.script;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.Transaction;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.server.UploadServlet;
import org.unitime.timetable.gwt.shared.TaskInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.ExecutionStatus;
import org.unitime.timetable.gwt.shared.TaskInterface.SaveTaskDetailsRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskExecutionInterface;
import org.unitime.timetable.model.PeriodicTask;
import org.unitime.timetable.model.ScriptParameter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TaskExecution;
import org.unitime.timetable.model.TaskParameter;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.PeriodicTaskDAO;
import org.unitime.timetable.model.dao.ScriptDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.CalendarUtils;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SaveTaskDetailsRpcRequest.class)
public class SaveTaskBackend implements GwtRpcImplementation<SaveTaskDetailsRpcRequest, TaskInterface> {

	@Override
	public TaskInterface execute(SaveTaskDetailsRpcRequest request, SessionContext context) {
		context.checkPermission(Right.TaskEdit);
		
		org.hibernate.Session hibSession = PeriodicTaskDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			TaskInterface task = request.getTask();
			
			PeriodicTask t = null;
			if (task.getId() != null)
				t = PeriodicTaskDAO.getInstance().get(task.getId(), hibSession);
			
			if (t == null) {
				t = new PeriodicTask();
				t.setScript(ScriptDAO.getInstance().get(task.getScript().getId(), hibSession));
				t.setOwner(TimetableManager.findByExternalId(context.getUser().getExternalUserId()));
				t.setParameters(new HashSet<TaskParameter>());
				t.setSchedule(new HashSet<TaskExecution>());
				t.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
			}
			
			Date ts = new Date();
			t.setEmail(task.getEmail());
			t.setName(task.getName());
			List<TaskParameter> parameters = new ArrayList<TaskParameter>(t.getParameters());
			for (ScriptParameter p: t.getScript().getParameters()) {
				TaskParameter tp = null;
				for (Iterator<TaskParameter> i = parameters.iterator(); i.hasNext(); ) {
					TaskParameter x = i.next();
					if (x.getName().equals(p.getName())) { tp = x; i.remove(); break; }
				}
				String value = task.getParameter(p.getName());
				if ("file".equals(p.getType())) {
					FileItem file = (FileItem)context.getAttribute(UploadServlet.SESSION_LAST_FILE);
					if (file != null) {
						t.setInputFile(file.get());
						value = file.getName();
					} else if (tp != null && t.getInputFile() != null) {
						value = tp.getValue();
					} else {
						t.setInputFile(null);
					}
				}
				if (value != null) {
					if (tp == null) {
						tp = new TaskParameter();
						tp.setTask(t); tp.setName(p.getName());
						t.getParameters().add(tp);
					}
					tp.setValue(value);
				} else if (tp != null) {
					tp.setTask(null); hibSession.delete(tp);
				}
			}
			for (TaskParameter tp: parameters) {
				t.getParameters().remove(tp);
				tp.setTask(null);
				hibSession.delete(tp);
			}
			
			int base = CalendarUtils.date2dayOfYear(t.getSession().getSessionStartYear(), t.getSession().getSessionBeginDateTime());
			List<TaskExecution> executions = new ArrayList<TaskExecution>(t.getSchedule());
			for (TaskExecutionInterface exec: task.getExecutions()) {
				TaskExecution execution = null;
				int index = exec.getDayOfYear() - base;
				exec.setExecutionDate(getScheduleDate(t.getSession(), index, exec.getSlot()));
				//if (exec.getDayOfYear() == null) exec.setDayOfYear(CalendarUtils.date2dayOfYear(t.getSession().getSessionStartYear(), exec.getExecutionDate()));
				// int index = exec.getDayOfYear() - base;
				for (Iterator<TaskExecution> i = executions.iterator(); i.hasNext(); ) {
					TaskExecution e = i.next();
					if (e.getExecutionPeriod().equals(exec.getSlot()) && e.getExecutionDate().equals(index)) {
						execution = e; i.remove(); break;
					}
				}
				if (execution == null) {
					execution = new TaskExecution();
					execution.setCreatedDate(ts);
					execution.setExecutionDate(index);
					execution.setExecutionPeriod(exec.getSlot());
					execution.setScheduledDate(getScheduleDate(t.getSession(), index, exec.getSlot()));
					execution.setExecutionStatus(ExecutionStatus.CREATED.ordinal());
					execution.setTask(t);
					t.getSchedule().add(execution);
				}
			}
			for (TaskExecution e: executions) {
				if (e.getExecutionStatus() == ExecutionStatus.CREATED.ordinal()) {
					t.getSchedule().remove(e);
					e.setTask(null);
					hibSession.delete(e);
				}
			}
			
			if (t.getUniqueId() == null)
				t.setUniqueId((Long)hibSession.save(t));
			else
				hibSession.update(t);
			
			hibSession.flush();
			tx.commit();
			
			return GetTasksBackend.getTask(t, context);
		} catch (Exception e) {
			tx.rollback();
			throw new GwtRpcException(e.getMessage(), e);
		}
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

}
