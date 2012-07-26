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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;

public class ExportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected SessionContext getSessionContext() {
		return HttpSessionContext.getSessionContext(getServletContext());
	}

	protected Exporter getExporter(String reference) {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		return (Exporter) applicationContext.getBean("org.unitime.timetable.export.Exporter:" + reference);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ExportServletHelper helper = new ExportServletHelper(request, response, getSessionContext());
		
		String ref = helper.getParameter("output");
		if (ref == null) throw new ServletException("No exporter provided.");
		Exporter exporter = getExporter(ref);
		if (exporter == null) throw new ServletException("Exporter " + ref + " not known.");
		
		try {
			
			exporter.export(helper);
			
		} finally {
			if (helper.hasOutputStream()) {
				helper.getOutputStream().flush();
				helper.getOutputStream().close();
			}
			if (helper.hasWriter()) {
				helper.getWriter().flush();
				helper.getWriter().close();
			}
		}
	}
}
