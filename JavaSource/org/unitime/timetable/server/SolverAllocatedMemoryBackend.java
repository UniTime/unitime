/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
