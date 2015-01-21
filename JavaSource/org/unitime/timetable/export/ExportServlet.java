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
