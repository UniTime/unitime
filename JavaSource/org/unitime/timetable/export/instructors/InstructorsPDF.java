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

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;

@Service("org.unitime.timetable.export.Exporter:instructors.pdf")
public class InstructorsPDF extends InstructorsCSV {

	@Override
	public String reference() {
		return "instructors.pdf";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		exportDataPdf(getInstructors(helper), helper);
	}
	
	protected void exportDataPdf(TableInterface table, ExportHelper helper) throws IOException {
    	PDFPrinter printer = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(printer.getContentType(), table.getId() + "-" + reference(), false);

		for (int i = 0; i < table.getHeader().size(); i++)
			printer.printHeader(i, table.getHeader().size(), printer.toA(table.getHeader().get(0), true));
		if (table.hasErrorMessage()) {
			A a = new A();
			a.italic(); a.center(); a.setColor("red");
			a.setText(table.getErrorMessage());
			a.setColSpan(table.getMaxColumns());
			printer.printLine(a);
		}
		if (table.getLines() != null) {
			for (LineInterface line: table.getLines()) {
				printer.printLine(printer.toA(line, false));
			}
		}
		
		printer.flushTable(table.getName());

		printer.flush(); printer.close();
	}
}
