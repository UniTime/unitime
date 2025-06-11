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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.shared.TableInterface.NaturalOrderComparator;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.ClassAssignmentsTableBuilder;
import org.unitime.timetable.server.courses.ClassesTableBuilder;
import org.unitime.timetable.server.courses.DistributionsTableBuilder;
import org.unitime.timetable.server.courses.ExaminationsTableBuilder;
import org.unitime.timetable.server.courses.InstructionalOfferingTableBuilder;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;

@Service("org.unitime.timetable.export.Exporter:offerings.csv")
public class OfferingsCSV implements Exporter {
	protected static ExaminationMessages EXAM = Localization.create(ExaminationMessages.class);

	protected @Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	protected @Autowired SolverService<ExamSolverProxy> examinationSolverService;

	@Override
	public String reference() {
		return "offerings.csv";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		checkPermission(helper, Right.InstructionalOfferingsExportPDF);
		exportDataCsv(getOfferings(helper), helper);
	}
	
	protected void checkPermission(ExportHelper helper, Right right) {
		List<SubjectArea> subjectAreas = new ArrayList<SubjectArea>();
    	for (String subjectAreaId: helper.getParameter("subjectArea").split(",")) {
    		if (subjectAreaId.isEmpty()) continue;
    		SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(Long.valueOf(subjectAreaId));
    		if (subjectArea != null) {
    			subjectAreas.add(subjectArea);
    			helper.getSessionContext().checkPermissionAnySession(subjectArea.getDepartment(), right);
    		}
    	}
	}
	
	protected List<TableInterface> getOfferings(ExportHelper helper) {
    	List<TableInterface> response = new ArrayList<TableInterface>();
    	
    	InstructionalOfferingTableBuilder builder = new InstructionalOfferingTableBuilder(
    			helper.getSessionContext(),
    			helper.getParameter("backType"),
		        helper.getParameter("backId")
		        );
    	builder.setSimple(true);
    	
    	builder.generateTableForInstructionalOfferings(
				classAssignmentService.getAssignment(),
				examinationSolverService.getSolver(),
		        new Filter(helper), 
		        helper.getParameter("subjectArea").split(","), 
		        response);

    	return response;
	}
	
	protected List<TableInterface> getClasses(ExportHelper helper) {
    	List<TableInterface> response = new ArrayList<TableInterface>();
    	
    	ClassesTableBuilder builder = new ClassesTableBuilder(
    			helper.getSessionContext(),
    			helper.getParameter("backType"),
		        helper.getParameter("backId")
    			);
    	builder.setSimple(true);
    	
    	builder.generateTableForClasses(
				classAssignmentService.getAssignment(),
				examinationSolverService.getSolver(),
		        new Filter(helper), 
		        helper.getParameter("subjectArea").split(","), 
		        response);

    	return response;
	}
	
	protected List<TableInterface> getClassAssignments(ExportHelper helper) {
    	List<TableInterface> response = new ArrayList<TableInterface>();
    	
    	ClassAssignmentsTableBuilder builder = new ClassAssignmentsTableBuilder(
    			helper.getSessionContext(),
    			helper.getParameter("backType"),
		        helper.getParameter("backId"));
    	builder.setSimple(true);
    	
    	builder.generateTableForClassAssignments(
				classAssignmentService.getAssignment(),
				examinationSolverService.getSolver(),
		        new Filter(helper), 
		        helper.getParameter("subjectArea").split(","), 
		        response);

    	return response;
	}
	
	protected List<TableInterface> getDistributions(ExportHelper helper) {
    	List<TableInterface> response = new ArrayList<TableInterface>();
    	
    	DistributionsTableBuilder builder = new DistributionsTableBuilder(
    			helper.getSessionContext(),
    			helper.getParameter("backType"),
		        helper.getParameter("backId")
		        );
    	builder.setSimple(true);
    	
    	Filter filter = new Filter(helper);
    	for (String subjectAreaId: helper.getParameter("subjectArea").split(",")) {
    		if (subjectAreaId.isEmpty()) continue;
    		SubjectArea area = SubjectAreaDAO.getInstance().get(Long.valueOf(subjectAreaId));
    		if (area == null) continue;
    		TableInterface table = builder.getDistPrefsTableForFilter(filter, area.getUniqueId());
    		table.setName(area.getLabel());
    		table.setId(subjectAreaId);
    		response.add(table);
    	}

    	return response;
	}
	
	protected List<TableInterface> getExams(ExportHelper helper) {
    	List<TableInterface> response = new ArrayList<TableInterface>();
    	
    	ExaminationsTableBuilder builder = new ExaminationsTableBuilder(
    			helper.getSessionContext(),
    			helper.getParameter("backType"),
		        helper.getParameter("backId")
		        );
    	builder.setSimple(true);
    	
    	Filter filter = new Filter(helper);
    	String examType = filter.getParameterValue("examType");
		ExamType type = null;
		try {
			type = ExamTypeDAO.getInstance().get(Long.valueOf(examType));
		} catch (Exception e) {}
		if (type == null)
			type = ExamType.findByReference(examType);
		if (type == null)
			throw new GwtRpcException(EXAM.messageNoExamType());
		
		response.add(sorted(builder.generateTableForExams(
				type,
				examinationSolverService.getSolver(),
		        filter, 
		        helper.getParameter("subjectArea").split(",")), helper));

    	return response;
	}
	
	protected TableInterface sorted(TableInterface table, ExportHelper helper) {
		String sort = helper.getParameter("sort");
		if (sort != null && !sort.isEmpty() && table.hasLines()) {
			int sortColumn = -1;
			boolean sortAsc = true;
			if (table.getHeader() != null)
				for (LineInterface line: table.getHeader()) {
					for (int col = 0; col < line.getCells().size(); col++) {
						CellInterface cell = line.getCells().get(col);
						if (cell.hasText() && sort.equals(cell.getText())) {
							sortColumn = col; sortAsc = true;
							break;
						}
						if (cell.hasText() && sort.equals("!" + cell.getText())) {
							sortColumn = col; sortAsc = false;
							break;
						}
					}
					break;
				}
			if (sortColumn >= 0) {
				final int col = sortColumn;
				final boolean asc = sortAsc;
				Collections.sort(table.getLines(), new Comparator<LineInterface>() {
					@Override
					@SuppressWarnings({ "rawtypes", "unchecked" })
					public int compare(LineInterface l1, LineInterface l2) {
						CellInterface c1 = l1.getCells().get(col);
						CellInterface c2 = l2.getCells().get(col);
						Comparable o1 = c1.getComparable();
						Comparable o2 = c2.getComparable();
						if (o1 instanceof String)
							return asc ?
									NaturalOrderComparator.compare(o1.toString(), o2.toString()):
									NaturalOrderComparator.compare(o2.toString(), o1.toString());
						else
							return asc ? o1.compareTo(o2) : o2.compareTo(o1);
					}
				});
			}
		}
		return table;
	}
	
	protected void exportDataCsv(List<TableInterface> response, ExportHelper helper) throws IOException {
    	Printer printer = new CSVPrinter(helper, false);
		helper.setup(printer.getContentType(), reference(), false);
		
		for (TableInterface table: response) {
			if (table.hasName())
				printer.printLine(table.getName());
			if (table.hasErrorMessage())
				printer.printLine(table.getErrorMessage());
			if (table.getHeader() != null) {
				for (LineInterface line: table.getHeader()) {
					printer.printHeader(toLine(line));
				}
			}
			if (table.getLines() != null) {
				for (LineInterface line: table.getLines()) {
					printer.printLine(toLine(line));
				}
			}
		}
        
    	printer.flush(); printer.close();
	}
	
	protected String[] toLine(LineInterface line) {
		List<String> row = new ArrayList<String>();
		if (line.hasCells()) {
			for (CellInterface cell: line.getCells()) {
				row.add(cell.toString());
				for (int i = 1; i < cell.getColSpan(); i++) row.add("");
			}
		}
		return row.toArray(new String[0]);
	}

	public static class Filter implements FilterInterface {
		ExportHelper iHelper;
		
		public Filter(ExportHelper helper) { iHelper = helper; }

		@Override
		public boolean hasParameter(String name) {
			return iHelper.getParameter(name) != null;
		}

		@Override
		public String getParameterValue(String name) {
			return iHelper.getParameter(name);
		}

		@Override
		public String getParameterValue(String name, String defaultValue) {
			String value = iHelper.getParameter(name);
			return (value == null ? defaultValue : value);
		}
	}
}
