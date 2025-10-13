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
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.security.rights.Right;

@Service("org.unitime.timetable.export.Exporter:classes.csv")
public class ClassesCSV extends OfferingsCSV {

	@Override
	public String reference() {
		return "classes.csv";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		checkPermission(helper, Right.ClassesExportPDF);
		exportDataCsv(getClasses(helper), helper);
	}
	
	@Override
	protected void exportDataCsv(List<TableInterface> response, ExportHelper helper) throws IOException {
    	Printer printer = new CSVPrinter(helper, false);
		helper.setup(printer.getContentType(), reference(), false);
		
		boolean firstTable = true;
		for (TableInterface table: response) {
			if (table.getHeader() != null) {
				if (firstTable)
					for (LineInterface line: table.getHeader())
						printer.printHeader(toLine(line));
				firstTable = false;
			}
			if (table.getLines() != null) {
				for (LineInterface line: table.getLines()) {
					printer.printLine(toLine(line));
				}
			}
		}
        
    	printer.flush(); printer.close();
	}
}
