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
package org.unitime.timetable.export.instructors;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.export.courses.OfferingsCSV.Filter;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.shared.TableInterface.NaturalOrderComparator;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.instructor.InstructorsTableBuilder;

@Service("org.unitime.timetable.export.Exporter:instructors.csv")
public class InstructorsCSV implements Exporter {
	
	@Override
	public String reference() {
		return "instructors.csv";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		exportDataCsv(getInstructors(helper), helper);
	}
	
	protected void exportDataCsv(TableInterface table, ExportHelper helper) throws IOException {
    	Printer printer = new CSVPrinter(helper, false);
		helper.setup(printer.getContentType(), table.getId() + "-" + reference(), false);
		
		if (table.hasErrorMessage())
			printer.printLine(table.getErrorMessage());
		if (table.getHeader() != null) {
			for (LineInterface line: table.getHeader()) {
				printer.printHeader(line.toCsvLine());
			}
		}
		if (table.getLines() != null) {
			for (LineInterface line: table.getLines()) {
				printer.printLine(line.toCsvLine());
			}
		}
        
    	printer.flush(); printer.close();
	}
	
	protected TableInterface getInstructors(ExportHelper helper) {
    	InstructorsTableBuilder builder = new InstructorsTableBuilder(
    			helper.getSessionContext(),
    			helper.getParameter("backType"),
		        helper.getParameter("backId")
		        );
    	builder.setSimple(true);
    	
    	Department department = DepartmentDAO.getInstance().get(Long.valueOf(helper.getParameter("deptId")));
		helper.getSessionContext().checkPermissionAnySession(department, Right.InstructorsExportPdf);
    	
		TableInterface table = builder.generateTableForDepartment(
    			department,
		        new Filter(helper),
		        helper.getSessionContext());
		table.setId(department.getDeptCode());
    	
    	String sort = helper.getParameter("sort");
    	if (sort != null && !sort.isEmpty()) {
    		LineInterface header = table.getHeader().get(0);
    		for (int col = 0; col < header.getCells().size(); col++) {
    			if (sort.equals(header.getCells().get(col).getText())) {
    				final int column = col;
    				Collections.sort(table.getLines(), new Comparator<LineInterface>() {
						@Override
						public int compare(LineInterface l1, LineInterface l2) {
							CellInterface c1 = l1.getCells().get(column);
							CellInterface c2 = l2.getCells().get(column);
							Comparable o1 = c1.getComparable();
							Comparable o2 = c2.getComparable();
							if (o1 instanceof String)
								return NaturalOrderComparator.compare(o1.toString(), o2.toString());
							else
								return o1.compareTo(o2);
						}
					});
    			} else if (sort.equals("!" + header.getCells().get(col).getText())) {
    				final int column = col;
    				Collections.sort(table.getLines(), new Comparator<LineInterface>() {
						@Override
						public int compare(LineInterface l1, LineInterface l2) {
							CellInterface c1 = l1.getCells().get(column);
							CellInterface c2 = l2.getCells().get(column);
							Comparable o1 = c1.getComparable();
							Comparable o2 = c2.getComparable();
							if (o1 instanceof String)
								return - NaturalOrderComparator.compare(o1.toString(), o2.toString());
							else
								return - o1.compareTo(o2);
						}
					});
    			}
    		}
    	}
    	
    	return table;
	}
}
