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
package org.unitime.timetable.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.cpsolver.ifs.util.CSVFile;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.webutil.PdfWebTable;


/**
 * @author Tomas Muller
 */
public class ExportUtils {
	
	public static OutputStream getPdfOutputStream(HttpServletResponse response, String name) throws IOException {
        response.setContentType("application/pdf; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader( "Content-Disposition", "attachment; filename=\"" + name + ".pdf\"" );
		
		return response.getOutputStream();
	}
	
	public static OutputStream getXmlOutputStream(HttpServletResponse response, String name) throws IOException {
        response.setContentType("application/xml; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader( "Content-Disposition", "attachment; filename=\"" + name + ".xml\"" );
		
		return response.getOutputStream();
	}

	public static PrintWriter getCsvWriter(HttpServletResponse response, String name) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader( "Content-Disposition", "attachment; filename=\"" + name + ".csv\"" );
		
		return response.getWriter();
	}

	public static PrintWriter getPlainTextWriter(HttpServletResponse response, String name) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader( "Content-Disposition", "attachment; filename=\"" + (name.indexOf('.') < 0 ? name + ".txt" : name ) + "\"" );
		
		return response.getWriter();
	}

	public static void exportCSV(CSVFile csv, HttpServletResponse response, String name) throws IOException {
		PrintWriter writer = getCsvWriter(response, name);
		
		if (csv.getHeader() != null)
			writer.println(csv.getHeader().toString());
		if (csv.getLines() != null)
			for (CSVFile.CSVLine line: csv.getLines())
				writer.println(line.toString());
		
		writer.flush(); writer.close();		
	}
	
	public static void exportPDF(PdfWebTable table, int ordCol, HttpServletResponse response, String name) throws Exception {
		OutputStream out = getPdfOutputStream(response, name);
		
		table.exportPdf(out, ordCol);
		
		out.flush(); out.close();		
	}
	
	public static void exportCSV(WebTable table, int ordCol, HttpServletResponse response, String name) throws IOException {
		exportCSV(table.printCsvTable(ordCol), response, name);		
	}

}
