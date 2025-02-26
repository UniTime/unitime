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
package org.unitime.timetable.export.courses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.XLSPrinter;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.ClassesTableBuilder;

@Service("org.unitime.timetable.export.Exporter:classes.xls")
public class ClassesXLS extends OfferingsXLS {

	@Override
	public String reference() {
		return "classes.xls";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		
		List<SubjectArea> subjectAreas = new ArrayList<SubjectArea>();
    	for (String subjectAreaId: helper.getParameter("subjectArea").split(",")) {
    		if (subjectAreaId.isEmpty()) continue;
    		SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(Long.valueOf(subjectAreaId));
    		if (subjectArea != null) {
    			subjectAreas.add(subjectArea);
    			helper.getSessionContext().checkPermissionAnySession(subjectArea.getDepartment(), Right.ClassesExportPDF);
    		}
    	}
    	
    	List<TableInterface> response = new ArrayList<TableInterface>();
    	
    	ClassesTableBuilder builder = new ClassesTableBuilder();
    	builder.setSimple(true);
    	
    	builder.generateTableForClasses(
				helper.getSessionContext(),
				classAssignmentService.getAssignment(),
				examinationSolverService.getSolver(),
		        new OfferingsCSV.Filter(helper), 
		        helper.getParameter("subjectArea").split(","), 
		        true, 
		        response,
		        helper.getParameter("backType"),
		        helper.getParameter("backId"));
    	
    	XLSPrinter printer = new XLSPrinter(helper.getOutputStream(), false);
		helper.setup(printer.getContentType(), reference(), false);
		
		boolean first = true;
		for (TableInterface table: response) {
			if (!first)
				printer.newSheet();
			printer.getWorkbook().setSheetName(printer.getSheetIndex(), table.getName());
			first = false;
			if (table.getHeader() != null)
				for (LineInterface line: table.getHeader())
					printer.printHeader(toLine(line));
			if (table.getLines() != null) {
				for (LineInterface line: table.getLines()) {
					printer.printLine(toA(line, false));
				}
			}
		}
        
    	printer.flush(); printer.close();		
	}
}