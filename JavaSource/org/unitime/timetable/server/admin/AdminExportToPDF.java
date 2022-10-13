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
package org.unitime.timetable.server.admin;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.export.PDFPrinter.F;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:admin-report.pdf")
public class AdminExportToPDF extends AdminExportToCSV {
	
	@Override
	public String reference() {
		return "admin-report.pdf";
	}

	protected void export(SimpleEditInterface data, ExportHelper helper, String hidden) throws IOException {
		PDFPrinter out = new PDFPrinter(helper.getOutputStream(), false);
		try {
			helper.setup(out.getContentType(), helper.getParameter("type") + ".pdf", false);
			
			boolean hasDetails = hasDetails(data);
			
			for (int i = 0; i < data.getFields().length; i++) {
				boolean visible = data.getFields()[i].isVisible() && (hidden == null || !hidden.contains("|" + data.getFields()[i].getName() + "|"));
				if (hasDetails && i == 0) visible = false;
				if (data.getFields()[i].isNoList()) visible = false;
				if (!visible)
					out.hideColumn(i);
			}
			
			String[] header = new String[data.getFields().length];
			for (int i = 0; i < data.getFields().length; i++)
				header[i] = header(data, data.getFields()[i]);
			out.printHeader(header);
			
			boolean visible = true;
			for (Record r: data.getRecords()) {
				if (hasDetails) {
					if ("-".equals(r.getField(0))) visible = true;
					else if ("+".equals(r.getField(0))) visible = false;
					else if (!visible) continue;
				}
				
				A[] line = new A[data.getFields().length];
				for (int i = 0; i < data.getFields().length; i++)
					line[i] = pdfCell(data.getFields()[i], r, i);
				
				if (isParent(data, r)) {
					for (A cell: line) {
						cell.setBackground("#f3f3f3");
						if (cell.getText().isEmpty())
							cell.setText(" ");
					}
				} else if (hasDetails) {
					for (int i = 0; i < line.length; i++)
						if (i > 0 && data.getFields()[i].getName().contains("|") && !isParent(data, r))
							line[i].setText("  " + line[i].getText());
				}
				
				out.printLine(line);
			}
		} finally {
			out.close();	
		}
	}
	
	protected A pdfCell(Field field, Record record, int index) {
		A cell = new A(cell(field, record, index));
		if (field.getType() == FieldType.number)
			cell.set(F.RIGHT);
		
		return cell;
	}
}
