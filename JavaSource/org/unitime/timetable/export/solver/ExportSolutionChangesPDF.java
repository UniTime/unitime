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
package org.unitime.timetable.export.solver;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolutionChangesFilterRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolutionChangesFilterResponse;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolutionChangesRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolutionChangesResponse;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:solution-changes.pdf")
public class ExportSolutionChangesPDF extends TableExporter {

	@Autowired private ApplicationContext applicationContext;

	@Override
	public String reference() {
		return "solution-changes.pdf";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		GwtRpcImplementation<SolutionChangesFilterRequest, SolutionChangesFilterResponse> filterService = (GwtRpcImplementation<SolutionChangesFilterRequest, SolutionChangesFilterResponse>)applicationContext.getBean(SolutionChangesFilterRequest.class.getName());
		SolutionChangesFilterResponse filter = filterService.execute(new SolutionChangesFilterRequest(), helper.getSessionContext());
		fillInFilter(filter, helper);
		
		GwtRpcImplementation<SolutionChangesRequest, SolutionChangesResponse> service = (GwtRpcImplementation<SolutionChangesRequest, SolutionChangesResponse>)applicationContext.getBean(SolutionChangesRequest.class.getName());
		SolutionChangesRequest request = new SolutionChangesRequest();
		request.setFilter(filter);
		SolutionChangesResponse response = service.execute(request, helper.getSessionContext());
		
		printTablePDF(response, helper);
	}

}
