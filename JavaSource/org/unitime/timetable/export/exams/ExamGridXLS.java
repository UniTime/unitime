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
package org.unitime.timetable.export.exams;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.XLSPrinter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.export.courses.ClassesCSV;
import org.unitime.timetable.export.exams.ExamGridExportHelper.TableCell;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridTable;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.exams.ExamGridBackend;
import org.unitime.timetable.server.exams.ExamGridContext;

@Service("org.unitime.timetable.export.Exporter:exams-grid.xls")
public class ExamGridXLS extends ClassesCSV {
	
	@Override
	public String reference() {
		return "exams-grid.xls";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		helper.getSessionContext().checkPermission(Right.ExaminationTimetable);
		ExamGridBackend backend = new ExamGridBackend();
		FilterInterface filter = new Filter(helper);
		ExamGridContext cx = new ExamGridContext(helper.getSessionContext(), filter);
		ExamGridTable table = backend.getTable(cx, examinationSolverService.getSolver());
		if (table == null || !table.hasPeriods() || !table.hasModels())
			throw new IllegalArgumentException();
		
		ExamGridExportHelper export = new ExamGridExportHelper(filter, table);
		XLSPrinter printer = new XLSPrinter(helper.getOutputStream(), false);
		helper.setup(printer.getContentType(), reference(), false);
		
		String lastName = null;
		for (int row = 0; row < export.getRowCount(); row++) {
			if (row == 0) {
				lastName = export.getTableName(row);
				if (lastName != null)
					printer.getWorkbook().setSheetName(printer.getSheetIndex(), lastName);
			} else if (export.getTableName(row) != null) {
				printer.newSheet();
				lastName = export.getTableName(row);
				printer.getWorkbook().setSheetName(printer.getSheetIndex(), lastName);
			}
			int rowspan = export.getMaxRowSpan(row);
			for (int idx = 0; idx < rowspan; idx++) {
				A[] line = new A[export.getCellCount(row)];
				for (int col = 0; col < export.getCellCount(row); col++) {
					TableCell c = export.getElement(row, col);
					line[col] = export.getA(row, col, idx, printer);
					if (idx + 1 == c.getNbrItems())
						line[col].setRowSpan(rowspan - idx);
				}
				printer.printLine(line);
			}
		}
        
    	printer.flush(); printer.close();
	}
}
