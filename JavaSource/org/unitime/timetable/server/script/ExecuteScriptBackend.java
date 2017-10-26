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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ScriptInterface.ExecuteScriptRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemInterface;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.queue.QueueItem;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ExecuteScriptRpcRequest.class)
public class ExecuteScriptBackend implements GwtRpcImplementation<ExecuteScriptRpcRequest, QueueItemInterface> {
	
	@Autowired SolverServerService solverServerService;

	@Override
	@PreAuthorize("checkPermission('Scripts')")
	public QueueItemInterface execute(ExecuteScriptRpcRequest request, SessionContext context) {
		try {

		ScriptExecution execution = new ScriptExecution(request, context);
		
		QueueItem executed = solverServerService.getQueueProcessor().add(execution);
		
		return GetQueueTableBackend.convert(executed, context);
		
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

} 