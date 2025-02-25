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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.XLSPrinter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.export.PDFPrinter.F;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.InstructionalOfferingTableBuilder;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;

@Service("org.unitime.timetable.export.Exporter:offerings.xls")
public class OfferingsXLS extends OfferingsCSV {

	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	@Override
	public String reference() {
		return "offerings.xls";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		
		List<SubjectArea> subjectAreas = new ArrayList<SubjectArea>();
    	for (String subjectAreaId: helper.getParameter("subjectArea").split(",")) {
    		if (subjectAreaId.isEmpty()) continue;
    		SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(Long.valueOf(subjectAreaId));
    		if (subjectArea != null) {
    			subjectAreas.add(subjectArea);
    			helper.getSessionContext().checkPermissionAnySession(subjectArea.getDepartment(), Right.InstructionalOfferingsExportPDF);
    		}
    	}
    	
    	List<TableInterface> response = new ArrayList<TableInterface>();
    	
    	InstructionalOfferingTableBuilder builder = new InstructionalOfferingTableBuilder();
    	builder.setSimple(true);
    	
    	builder.generateTableForInstructionalOfferings(
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
	
	protected A[] toA(LineInterface line, boolean header) {
		List<A> ret = new ArrayList<A>();
		if (line.hasCells())
			for (CellInterface cell: line.getCells()) {
				A a = toA(cell, line, null, 0);
				if (!a.hasChunks() && (a.getText() == null || a.getText().isEmpty()))
					a.setText(" ");
				if (header) {
					a.bold();
				}
				ret.add(a);
			}
		return ret.toArray(new A[0]);
	}
	
	protected void applyStyle(A a, String styles) {
		for (String style: styles.split(";")) {
			if (style.indexOf(':') < 0) continue;
			String key = style.substring(0, style.indexOf(':')).trim();
			String value = style.substring(style.indexOf(':') + 1).trim();
			if ("font-weight".equalsIgnoreCase(key) && "bold".equalsIgnoreCase(value))
				a.bold();
			else if ("font-style".equalsIgnoreCase(key) && "italic".equalsIgnoreCase(value))
				a.bold();
			else if ("color".equalsIgnoreCase(key))
				a.setColor(value);
			else if ("background".equalsIgnoreCase(key))
				a.setBackground(value);
		}
	}
	
	protected A createCell(CellInterface cell) {
		return new A();
	}
	
	protected A toA(CellInterface cell, LineInterface line, A parent, int index) {
		A a = createCell(cell);
		a.inline();
		if (parent != null && !cell.isInline() && index > 0)
			parent.clear(F.INLINE);
		if (parent != null && parent.has(F.BOLD)) a.bold();
		if (parent != null && parent.has(F.ITALIC)) a.italic();
		if (parent != null && parent.getColorValue() != null) a.setColor(parent.getColor());
		if (parent == null && !cell.hasNoWrap()) a.wrap();
		a.setColSpan(cell.getColSpan()); a.setRowSpan(cell.getRowSpan());
		if (cell.hasColor()) a.color(cell.getColor());
		if (parent == null) {
			if (line.hasBgColor()) a.setBackground(line.getBgColor());
			if (line.hasStyle()) applyStyle(a, line.getStyle());
		}
		if (cell.hasStyle()) applyStyle(a, cell.getStyle());
		if (cell.getTextAlignment() == Alignment.CENTER)
			a.center();
		else if (cell.getTextAlignment() == Alignment.RIGHT)
			a.right();
		if (a.getImage() != null) {
		} else if (cell.hasAria()) {
			a.setText(cell.getAria());
		} else if (cell.hasText() && !cell.isHtml()) {
			a.setText(cell.getText());
		} else if (cell.hasTitle()) {
			a.setText(cell.getTitle());
		} else if (cell.hasImage()) {
			if (cell.getImage().hasTitle())
				a.setText(cell.getImage().getTitle());
		}
		if (cell.hasIndent())
			for (int i = 0; i < cell.getIndent(); i++)
				a.setText("  " + (a.getText() == null ? "" : a.getText()));
		if (cell.hasItems() && a.getImage() == null && !cell.hasAria()) { 
			int i = 0;
			for (CellInterface c: cell.getItems()) {
				a.add(toA(c, line, a, i++));
			}
		}
		return a;
	}

}