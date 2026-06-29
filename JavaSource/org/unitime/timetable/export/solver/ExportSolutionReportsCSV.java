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
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.courses.ClassesCSV;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolverReportsRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolverReportsResponse;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:solution-reports.csv")
public class ExportSolutionReportsCSV extends ClassesCSV {

	@Autowired private ApplicationContext applicationContext;

	@Override
	public String reference() {
		return "solution-reports.csv";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		String tableId = helper.getParameter("table");
		
		GwtRpcImplementation<SolverReportsRequest, SolverReportsResponse> service = (GwtRpcImplementation<SolverReportsRequest, SolverReportsResponse>)applicationContext.getBean(SolverReportsRequest.class.getName());
		SolverReportsRequest request = new SolverReportsRequest();
		SolverReportsResponse response = service.execute(request, helper.getSessionContext());
		
		Printer out = new CSVPrinter(helper, false);
		helper.setup(out.getContentType(), reference(), false);
		
		if (response.hasTables()) {
			List<TableInterface> tables = new ArrayList<TableInterface>();
			for (TableInterface table: response.getTables())
				if (table.getId().equals(tableId)) tables.add(sorted(table, helper));
			exportDataCsv(tables, helper);
		}
	}

}
