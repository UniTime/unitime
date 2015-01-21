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
package org.unitime.timetable.export;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.export.Exporter.Printer;

/**
 * @author Tomas Muller
 */
public class CSVPrinter implements Printer {
	private PrintWriter iOut;
	private String[] iLastLine = null;
	private boolean iCheckLast = false;
	private Set<Integer> iHiddenColumns = new HashSet<Integer>();
	
	public CSVPrinter(PrintWriter writer, boolean checkLast) {
		iOut = writer;
		iCheckLast = checkLast;
	}
	
	@Override
	public String getContentType() {
		return "text/csv";
	}
	
	@Override
	public void hideColumn(int col) {
		iHiddenColumns.add(col);
	}
	
	@Override
	public void printHeader(String... fields) {
		printLine(fields);
	}
	
	@Override
	public void printLine(String... fields) {
		for (int idx = 0; idx < fields.length; idx++) {
			if (iHiddenColumns.contains(idx)) continue;
			String f = fields[idx];

			if (f != null && !f.isEmpty()) {
				if (!iCheckLast || !f.equals(iLastLine == null || idx >= iLastLine.length ? null : iLastLine[idx]))
					iOut.print("\"" + f.replace("\"", "\"\"") + "\"");
			}
			iOut.print(",");
		}
		iOut.println();
		iLastLine = fields;
	}
	
	@Override
	public void flush() {
		iLastLine = null;
	}
	
	@Override
	public void close() {
	}
}
