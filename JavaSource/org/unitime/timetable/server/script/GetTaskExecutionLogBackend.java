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
*/package org.unitime.timetable.server.script;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.TaskInterface.ExecutionStatus;
import org.unitime.timetable.gwt.shared.TaskInterface.GetTaskExecutionLogRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskExecutionLogInterface;
import org.unitime.timetable.model.TaskExecution;
import org.unitime.timetable.model.dao.TaskExecutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.queue.QueueItem;
import org.unitime.timetable.util.queue.QueueMessage;

import com.google.gson.reflect.TypeToken;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(GetTaskExecutionLogRpcRequest.class)
public class GetTaskExecutionLogBackend implements GwtRpcImplementation<GetTaskExecutionLogRpcRequest, TaskExecutionLogInterface> {
	public static final Type TYPE_LOG = new TypeToken<ArrayList<QueueMessage>>() {}.getType();
	
	@Autowired SolverServerService solverServerService;

	@Override
	public TaskExecutionLogInterface execute(GetTaskExecutionLogRpcRequest request, SessionContext context) {
		context.hasPermission(Right.TaskDetail);
		TaskExecution e = TaskExecutionDAO.getInstance().get(request.getTaskExecutionId());
		if (e == null)
			return null;
		
		List<QueueMessage> log = null;
		if (e.getExecutionStatus() == ExecutionStatus.QUEUED.ordinal() || e.getExecutionStatus() == ExecutionStatus.RUNNING.ordinal()) {
			QueueItem item = solverServerService.getQueueProcessor().getByExecutionId(request.getTaskExecutionId());
			if (item != null)
				log = item.getLog();
		}
		
		if (e.getLogFile() != null) {
			log = TaskExecutionItem.createGson().fromJson(e.getLogFile(), TYPE_LOG);
		}
		
		TaskExecutionLogInterface ret = new TaskExecutionLogInterface();
		if (log != null) {
			String html = "";
			for (QueueMessage m: log) {
				if (!html.isEmpty()) html += "<br>";
				html += m.toHTML();
			}
			ret.setLog(html);
		}
		return ret;
	}

}
