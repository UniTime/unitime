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
import org.unitime.timetable.export.XLSPrinter;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;

@Service("org.unitime.timetable.export.Exporter:instructors.xls")
public class InstructorsXLS extends InstructorsCSV {
	@Override
	public String reference() {
		return "instructors.xls";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		exportDataXls(getInstructors(helper), helper);
	}

	protected void exportDataXls(TableInterface table, ExportHelper helper) throws IOException {
    	XLSPrinter printer = new XLSPrinter(helper.getOutputStream(), false);
		helper.setup(printer.getContentType(), table.getId() + "-" + reference(), false);
		
		printer.getWorkbook().setSheetName(printer.getSheetIndex(), table.getName());
		if (table.getHeader() != null)
			for (LineInterface line: table.getHeader())
				printer.printHeader(line.toCsvLine());
		if (table.getLines() != null) {
			for (LineInterface line: table.getLines()) {
				printer.printLine(printer.toA(line, false));
			}
		}
        
    	printer.flush(); printer.close();		
	}
}
