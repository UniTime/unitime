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
package org.unitime.timetable.server.sectioning;


import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.CSVFile.CSVLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.client.sectioning.SectioningReports.SectioningReportRpcRequest;
import org.unitime.timetable.gwt.client.sectioning.SectioningReports.SectioningReportRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.basic.GenerateSectioningReport;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SectioningReportRpcRequest.class)
public class SectioningReportsBackend implements GwtRpcImplementation<SectioningReportRpcRequest, SectioningReportRpcResponse> {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	@Autowired SolverServerService solverServerService;

	@Override
	public SectioningReportRpcResponse execute(SectioningReportRpcRequest request, SessionContext context) {
		DataProperties parameters = new DataProperties(request.getParameters());
		CSVFile csv =  null;
		boolean online = parameters.getPropertyBoolean("online", false);
		parameters.setProperty("useAmPm", CONSTANTS.useAmPm() ? "true" : "false");

		if (online) {
			context.checkPermission(Right.SchedulingReports);
			OnlineSectioningServer server = solverServerService.getOnlineStudentSchedulingContainer().getSolver(context.getUser().getCurrentAcademicSessionId().toString());
			if (server == null)
				throw new GwtRpcException("Online student scheduling is not enabled for " + context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel() + ".");
			
			OnlineSectioningLog.Entity user = OnlineSectioningLog.Entity.newBuilder()
					.setExternalId(context.getUser().getExternalUserId())
					.setName(context.getUser().getName() == null ? context.getUser().getUsername() : context.getUser().getName())
					.setType(context.hasPermission(Right.StudentSchedulingAdvisor) ? OnlineSectioningLog.Entity.EntityType.MANAGER : OnlineSectioningLog.Entity.EntityType.STUDENT).build();
			
			csv = server.execute(server.createAction(GenerateSectioningReport.class).withParameters(parameters), user);
		} else {
			context.checkPermission(Right.StudentSectioningSolver);
			
			StudentSolverProxy solver = studentSectioningSolverService.getSolver();
			if (solver == null)
				throw new GwtRpcException("No student solver is running.");
			
			csv = solver.getReport(parameters);
		}
		
		if (csv == null)
			throw new GwtRpcException("No report was created.");
		
		SectioningReportRpcResponse response = new SectioningReportRpcResponse();
		String[] header = new String[csv.getHeader().getFields().size()];
		for (int i = 0; i < csv.getHeader().getFields().size(); i++)
			header[i] = csv.getHeader().getField(i).toString();
		response.addLine(header);
		if (csv.getLines() != null)
			for (CSVLine line: csv.getLines()) {
				String[] row = new String[line.getFields().size()];
				for (int i = 0; i < line.getFields().size(); i++)
					row[i] = line.getField(i).toString();
				response.addLine(row);
			}
	
		return response;
	}

}
