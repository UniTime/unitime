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
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.security.rights.Right;

@Service("org.unitime.timetable.export.Exporter:offerings.pdf")
public class OfferingsPDF extends OfferingsXLS {

	@Override
	public String reference() {
		return "offerings.pdf";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		checkPermission(helper, Right.InstructionalOfferingsExportPDF);
		exportDataPdf(getOfferings(helper), helper);
	}
	
	protected void exportDataPdf(List<TableInterface> response, ExportHelper helper) throws IOException {
    	PDFPrinter printer = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(printer.getContentType(), reference(), false);
		
		for (TableInterface table: response) {
			for (int i = 0; i < table.getHeader().size(); i++)
				printer.printHeader(i, table.getHeader().size(), toA(table.getHeader().get(0), true));
			if (table.hasErrorMessage()) {
				A a = new A();
				a.italic(); a.center(); a.setColor("red");
				a.setText(table.getErrorMessage());
				a.setColSpan(table.getMaxColumns());
				printer.printLine(a);
			}
			if (table.getLines() != null) {
				for (LineInterface line: table.getLines()) {
					printer.printLine(toA(line, false));
				}
			}
			printer.flushTable(table.getName());
		}
        
    	printer.flush(); printer.close();
	}
	
	@Override
	protected A createCell(CellInterface cell) {
		try {
			if (cell.hasImage() && cell.getImage().getGenerator() != null) {
				return new A((java.awt.Image)cell.getImage().getGenerator().generate());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new A();
	}
}
