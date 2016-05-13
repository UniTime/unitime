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
package org.unitime.timetable.server;

import java.text.DecimalFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.gwt.client.solver.SolverAllocatedMemory.SolverAllocatedMemoryRpcRequest;
import org.unitime.timetable.gwt.client.solver.SolverAllocatedMemory.SolverAllocatedMemoryRpcResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.service.SolverServerService;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SolverAllocatedMemoryRpcRequest.class)
public class SolverAllocatedMemoryBackend implements GwtRpcImplementation<SolverAllocatedMemoryRpcRequest, SolverAllocatedMemoryRpcResponse>{
	@Autowired SolverServerService solverServerService;

	@Override
	public SolverAllocatedMemoryRpcResponse execute(SolverAllocatedMemoryRpcRequest request, SessionContext context) {
		long memUsage = 0;
		switch (request.getSolverId().charAt(0)) {
		case 'C':
			memUsage = solverServerService.getCourseSolverContainer().getMemUsage(request.getSolverId().substring(1));
			break;
		case 'X':
			memUsage = solverServerService.getExamSolverContainer().getMemUsage(request.getSolverId().substring(1));
			break;
		case 'S':
			memUsage = solverServerService.getStudentSolverContainer().getMemUsage(request.getSolverId().substring(1));
			break;
		case 'I':
			memUsage = solverServerService.getInstructorSchedulingContainer().getMemUsage(request.getSolverId().substring(1));
			break;
		case 'O':
			if (request.getSolverId().indexOf(':') >= 0) {
				String[] idHost = request.getSolverId().substring(1).split(":");
				OnlineSectioningServer server = solverServerService.getServer(idHost[0]).getOnlineStudentSchedulingContainer().getSolver(idHost[1]);
				if (server != null)
					memUsage = server.getMemUsage();
			} else {
				memUsage = solverServerService.getOnlineStudentSchedulingContainer().getMemUsage(request.getSolverId().substring(1));
			}
			break;
		}
		if (memUsage == 0)
			return null;
		else
			return new SolverAllocatedMemoryRpcResponse(new DecimalFormat("0.00").format(memUsage / 1048576.0) + "M");
	}

}
