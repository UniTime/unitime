/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
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
package org.unitime.timetable.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.AnonymousUserContext;
import org.unitime.timetable.security.context.HttpSessionContext;

/**
 * @author Tomas Muller
 */
public class ApiServlet extends HttpServlet {
	private static Log sLog = LogFactory.getLog(ApiServlet.class);
	private static final long serialVersionUID = 1L;
	
	protected SessionContext getSessionContext() {
		return HttpSessionContext.getSessionContext(getServletContext());
	}
	
	protected String getReference(HttpServletRequest request) {
		return request.getServletPath() + request.getPathInfo();
	}

	protected ApiConnector getConnector(HttpServletRequest request) {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		return (ApiConnector) applicationContext.getBean(getReference(request));
	}
	
	protected void checkError(HttpServletRequest request, HttpServletResponse response, Throwable t) throws IOException {
		if (t instanceof NoSuchBeanDefinitionException) {
			sLog.info("Service " + getReference(request) + " not known.");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Service " + getReference(request) + " not known, please check the request path.");
		} else if (t instanceof IllegalArgumentException) {
			sLog.info(t.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, t.getMessage());
		} else if (t instanceof PageAccessException || t instanceof AccessDeniedException) {
			sLog.info(t.getMessage());
			if (!getSessionContext().isAuthenticated() || getSessionContext().getUser() instanceof AnonymousUserContext) {
				response.setHeader("WWW-Authenticate", "Basic");
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, t.getMessage());
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, t.getMessage());	
			}
		} else {
			sLog.warn(t.getMessage(), t);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			getConnector(request).doGet(request, response);
		} catch (Throwable t) {
			checkError(request, response, t);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			getConnector(request).doPost(request, response);
		} catch (Throwable t) {
			checkError(request, response, t);
		}
	}
	
	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			getConnector(request).doPut(request, response);
		} catch (Throwable t) {
			checkError(request, response, t);
		}
	}
	
	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			getConnector(request).doDelete(request, response);
		} catch (Throwable t) {
			checkError(request, response, t);
		}
	}
}
