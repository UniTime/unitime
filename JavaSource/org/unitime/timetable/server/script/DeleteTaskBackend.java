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

import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.TaskInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.DeleteTaskDetailsRpcRequest;
import org.unitime.timetable.model.PeriodicTask;
import org.unitime.timetable.model.dao.PeriodicTaskDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(DeleteTaskDetailsRpcRequest.class)
public class DeleteTaskBackend implements GwtRpcImplementation<DeleteTaskDetailsRpcRequest, TaskInterface> {

	@Override
	public TaskInterface execute(DeleteTaskDetailsRpcRequest request, SessionContext context) {
		context.checkPermission(Right.TaskEdit);
		
		PeriodicTask t = PeriodicTaskDAO.getInstance().get(request.getTaskId());
		if (t == null) return null;
		
		PeriodicTaskDAO.getInstance().delete(t);
		return null;
		/*
		for (Iterator<TaskExecution> i = t.getSchedule().iterator(); i.hasNext(); ) {
			TaskExecution e = i.next();
			if (e.getExecutionStatus() == ExecutionStatus.CREATED.ordinal())
				i.remove();
		}
		
		if (t.getSchedule().isEmpty()) {
			PeriodicTaskDAO.getInstance().delete(t);
			return null;
		} else {
			PeriodicTaskDAO.getInstance().update(t);
			return GetTasksBackend.getTask(t, context);
		}
		*/
	}

}
