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

import java.util.Collections;
import java.util.List;

import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.TaskInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.ExecutionStatus;
import org.unitime.timetable.gwt.shared.TaskInterface.GetTasksRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskExecutionInterface;
import org.unitime.timetable.model.PeriodicTask;
import org.unitime.timetable.model.TaskExecution;
import org.unitime.timetable.model.TaskParameter;
import org.unitime.timetable.model.dao.PeriodicTaskDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(GetTasksRpcRequest.class)
public class GetTasksBackend implements GwtRpcImplementation<GetTasksRpcRequest, GwtRpcResponseList<TaskInterface>> {
	
	@Override
	public GwtRpcResponseList<TaskInterface> execute(GetTasksRpcRequest request, SessionContext context) {
		context.checkPermission(Right.Tasks);
		
		GwtRpcResponseList<TaskInterface> tasks = new GwtRpcResponseList<TaskInterface>();
		
		for (PeriodicTask t: (List<PeriodicTask>)PeriodicTaskDAO.getInstance().getSession().createQuery(
				"from PeriodicTask where session.uniqueId = :sessionId").setLong("sessionId", context.getUser().getCurrentAcademicSessionId())
				.setCacheable(true).list()) {
			tasks.add(getTask(t, context));
		}

		Collections.sort(tasks);
		return tasks;
	}
	
	public static TaskInterface getTask(PeriodicTask t, SessionContext context) {
		TaskInterface task = new TaskInterface();
		task.setId(t.getUniqueId());
		task.setName(t.getName());
		task.setEmail(t.getEmail());

		String nameFormat = context.getUser().getProperty(UserProperty.NameFormat);
		ContactInterface owner = new ContactInterface();
		owner.setAcademicTitle(t.getOwner().getAcademicTitle());
		owner.setEmail(t.getOwner().getEmailAddress());
		owner.setExternalId(t.getOwner().getExternalUniqueId());
		owner.setFirstName(t.getOwner().getFirstName());
		owner.setMiddleName(t.getOwner().getMiddleName());
		owner.setLastName(t.getOwner().getLastName());
		owner.setFormattedName(t.getOwner().getName(nameFormat));
		task.setOwner(owner);
		
		task.setScript(LoadAllScriptsBackend.load(t.getScript(), context));
		task.setCanEdit(context.hasPermission(Right.TaskEdit) && task.getScript().canEdit());
		task.setCanView(context.hasPermission(Right.TaskDetail) && task.getScript().canExecute());
		for (TaskParameter p: t.getParameters()) {
			task.setParameter(p.getName(), p.getValue());
		}
		for (TaskExecution e: t.getSchedule()) {
			TaskExecutionInterface exec = new TaskExecutionInterface();
			exec.setId(e.getUniqueId());
			exec.setCreated(e.getCreatedDate());
			exec.setStatus(ExecutionStatus.values()[e.getExecutionStatus()]);
			exec.setExecutionDate(t.getSession().getDate(e.getExecutionDate()));
			exec.setSlot(e.getExecutionPeriod());
			exec.setDayOfWeek(Constants.getDayOfWeek(exec.getExecutionDate()));
			exec.setDayOfYear(CalendarUtils.date2dayOfYear(t.getSession().getSessionStartYear(), exec.getExecutionDate()));
			exec.setFinished(e.getFinishedDate());
			exec.setQueued(e.getQueuedDate());
			exec.setStarted(e.getStartedDate());
			exec.setStatusMessage(e.getStatusMessage());
			if (e.getStartedDate() != null && (task.getLastExecuted() == null || task.getLastExecuted().before(e.getStartedDate()))) {
				task.setLastExecuted(e.getStartedDate());
				task.setLastStatus(ExecutionStatus.values()[e.getExecutionStatus()]);
			}
			exec.setOutput(e.getOutputName());
			task.addExecution(exec);
		}
		return task;
	}

}
