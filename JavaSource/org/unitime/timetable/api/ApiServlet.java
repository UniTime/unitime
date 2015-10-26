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
			sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, t);
		} else if (t instanceof IllegalArgumentException) {
			sLog.info(t.getMessage());
			sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, t);
		} else if (t instanceof PageAccessException || t instanceof AccessDeniedException) {
			sLog.info(t.getMessage());
			if (!getSessionContext().isAuthenticated() || getSessionContext().getUser() instanceof AnonymousUserContext) {
				response.setHeader("WWW-Authenticate", "Basic");
				sendError(request, response, HttpServletResponse.SC_UNAUTHORIZED, t);
			} else {
				sendError(request, response, HttpServletResponse.SC_FORBIDDEN, t);	
			}
		} else {
			sLog.warn(t.getMessage(), t);
			sendError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t);
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
	
	protected void sendError(HttpServletRequest request, HttpServletResponse response, int code, String message) throws IOException {
		try {
			getConnector(request).createHelper(request, response).sendError(code, message);
		} catch (Throwable t) {
			response.sendError(code, message);
		}
	}
	
	protected void sendError(HttpServletRequest request, HttpServletResponse response, int code, Throwable error) throws IOException {
		try {
			if (error instanceof NoSuchBeanDefinitionException)
				new JsonApiHelper(request, response, getSessionContext(), null).sendError(code, "Service " + getReference(request) + " not known, please check the request path.");
			else
				getConnector(request).createHelper(request, response).sendError(code, error);
		} catch (Throwable t) {
			response.sendError(code, error.getMessage());
		}
	}
}
