/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
import java.util.Enumeration;

public interface Exporter {
	
	public String reference();
	
	public void export(ExportHelper helper) throws IOException;
	
	public static interface Params {
		public String getParameter(String name);
		public String[] getParameterValues(String name);
		public Enumeration<String> getParameterNames();
	}
	
	public static interface Printer {
		public String getContentType();
		public void hideColumn(int col);
		public void printHeader(String... fields) throws IOException;
		public void printLine(String... fields) throws IOException;
		public void flush() throws IOException;
		public void close() throws IOException;
	}
}
