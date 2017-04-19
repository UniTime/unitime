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
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignedClassesFilterRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignedClassesFilterResponse;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignedClassesRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignedClassesResponse;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:assigned-classes.pdf")
public class ExportAssignedClassesPDF extends TableExporter {

	@Autowired private ApplicationContext applicationContext;

	@Override
	public String reference() {
		return "assigned-classes.pdf";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		GwtRpcImplementation<AssignedClassesFilterRequest, AssignedClassesFilterResponse> filterService = (GwtRpcImplementation<AssignedClassesFilterRequest, AssignedClassesFilterResponse>)applicationContext.getBean(AssignedClassesFilterRequest.class.getName());
		AssignedClassesFilterResponse filter = filterService.execute(new AssignedClassesFilterRequest(), helper.getSessionContext());
		fillInFilter(filter, helper);
		
		GwtRpcImplementation<AssignedClassesRequest, AssignedClassesResponse> service = (GwtRpcImplementation<AssignedClassesRequest, AssignedClassesResponse>)applicationContext.getBean(AssignedClassesRequest.class.getName());
		AssignedClassesRequest request = new AssignedClassesRequest();
		request.setFilter(filter);
		AssignedClassesResponse response = service.execute(request, helper.getSessionContext());
		
		printTablePDF(response, helper);
	}

}
