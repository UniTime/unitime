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
package org.unitime.timetable.export;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;

/**
 * @author Tomas Muller
 */
public class ExportServlet extends HttpServlet {
	private static Log sLog = LogFactory.getLog(ExportServlet.class);
	private static final long serialVersionUID = 1L;
	
	protected SessionContext getSessionContext() {
		return HttpSessionContext.getSessionContext(getServletContext());
	}

	protected Exporter getExporter(String reference) {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		return (Exporter) applicationContext.getBean("org.unitime.timetable.export.Exporter:" + reference);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ExportServletHelper helper = null;
		String ref = null;
		try {
			helper = new ExportServletHelper(request, response, getSessionContext());
			
			ref = helper.getParameter("output");
			if (ref == null) {
				sLog.info("No exporter provided.");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No exporter provided, please set the output parameter.");
				return;
			}
			
			getExporter(ref).export(helper);
		} catch (NoSuchBeanDefinitionException e) {
			sLog.info("Exporter " + ref + " not known.");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Exporter " + ref + " not known, please check the output parameter.");
		} catch (IllegalArgumentException e) {
			sLog.info(e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		} catch (PageAccessException e) {
			sLog.info(e.getMessage());
			response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
		} catch (Exception e) {
			sLog.warn(e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} finally {
			if (helper != null) {
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
}
