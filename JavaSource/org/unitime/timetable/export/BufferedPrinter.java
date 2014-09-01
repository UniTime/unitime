/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.export.Exporter.Printer;

/**
 * @author Tomas Muller
 */
public class BufferedPrinter implements Printer {
	Printer iPrinter = null;
	private List<String[]> iLines = new ArrayList<String[]>();
	
	public BufferedPrinter(Printer printer) {
		iPrinter = printer;
	}
	
	@Override
	public String getContentType() {
		return iPrinter.getContentType();
	}
	
	@Override
	public void hideColumn(int col) {
		iPrinter.hideColumn(col);
	}
	
	@Override
	public void printHeader(String... fields) throws IOException {
		iPrinter.printHeader(fields);
	}
	
	@Override
	public void printLine(String... fields) {
		iLines.add(fields);
	}
	
	public List<String[]> getBuffer() {
		return iLines;
	}
	
	@Override
	public void flush() {
	}
	
	@Override
	public void close() throws IOException {
		try {
			for (String[] line: iLines) {
				iPrinter.printLine(line);
			}
			iPrinter.flush();
		} finally {
			iPrinter.close();
		}
	}
}