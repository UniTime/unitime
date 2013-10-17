/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.unitime.commons.web.WebTable;
import org.unitime.timetable.webutil.PdfWebTable;

import net.sf.cpsolver.ifs.util.CSVFile;

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
