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
package org.unitime.timetable.filter;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Enumeration;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.security.UserContext;


/**
 * @author Tomas Muller
 */
public class PageAccessFilter implements Filter {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Log sLog = LogFactory.getLog(PageAccessFilter.class);
	private static DecimalFormat sDF = new DecimalFormat("0.00");
	private long debugTime = 30000; // Print info about the page if the page load took at least this time.
	private long dumpTime = 300000; // Print debug info about the page if the page load took at least this time.
	private boolean dumpSessionAttribues = false; // Include session attributes in the dump.
	
	public void init(FilterConfig cfg) throws ServletException {
		if (cfg.getInitParameter("debug-time")!=null) {
			debugTime = Long.parseLong(cfg.getInitParameter("debug-time"));
		}
		if (cfg.getInitParameter("dump-time")!=null) {
			dumpTime = Long.parseLong(cfg.getInitParameter("dump-time"));
		}
		if (cfg.getInitParameter("session-attributes")!=null) {
			dumpSessionAttribues = Boolean.parseBoolean(cfg.getInitParameter("session-attributes"));
		}
	}
	
	private UserContext getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserContext)
			return (UserContext)authentication.getPrincipal();
		return null;
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {
		try {
			long t0 = System.currentTimeMillis();
			
			UserContext user = getUser();
			if (user != null)
				ApplicationProperties.setSessionId(user.getCurrentAcademicSessionId());
			
			// Process request
			Throwable exception = null;
			try {
				chain.doFilter(request,response);
			} catch (Throwable t) {
				exception = t;
			}
			
			long t1 = System.currentTimeMillis(); 
			if (request instanceof HttpServletRequest && ((t1-t0)>debugTime || exception!=null)) {
				HttpServletRequest r = (HttpServletRequest)request;
				String message = "Page "+r.getRequestURI()+" took "+sDF.format((t1-t0)/1000.0)+" s.";
				if (exception!=null) {
					message = exception+" seen on page "+r.getRequestURI()+" (page took "+sDF.format((t1-t0)/1000.0)+" s).";
				}
				if (exception!=null || (t1-t0)>dumpTime) {
					UserContext u = null;
					try {
						u = getUser();
					} catch (IllegalStateException e) {}
					if (u==null) {
						message += "\n  User: no user";
					} else {
						message += "\n  User: " + u.getUsername() + (u.getCurrentAuthority() != null ? " ("+u.getCurrentAuthority()+")" : "");
					}
					message += "\n  Request parameters:";
					for (Enumeration e=r.getParameterNames(); e.hasMoreElements();) {
						String n = (String)e.nextElement();
						if ("password".equals(n)) continue;
						message+="\n    "+n+"="+r.getParameter(n);
					}
					try {
						if (dumpSessionAttribues && r.getSession() != null) {
							message += "\n  Session attributes:";
							for (Enumeration e=r.getSession().getAttributeNames(); e.hasMoreElements();) {
								String n = (String)e.nextElement();
								message+="\n    "+n+"="+r.getSession().getAttribute(n);
							}
						}
					} catch (IllegalStateException e) {
						message += "\n    INVALID SESSION";
					}
				} else {
					UserContext u = getUser();
					if (u==null) {
						message += "  (User: no user)";
					} else {
						message += "  (User: " + u.getUsername() + (u.getCurrentAuthority() != null ? " ("+u.getCurrentAuthority()+")" : "");
					}
				}
				if (exception!=null)
					sLog.warn(message);
				else
					sLog.info(message);
			}		

			if (exception!=null) {
				if (exception instanceof PageAccessException && request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
					HttpServletRequest r = (HttpServletRequest)request;
					HttpServletResponse x = (HttpServletResponse)response;
					String message = exception.getMessage();
					if (message == null || message.isEmpty()) {
						HttpSession s = r.getSession();
						if (getUser() == null) {
							if (s.isNew()) 
								message = MESSAGES.authenticationExpired();
							else
								message = MESSAGES.authenticationRequired();
						} else {
							message = MESSAGES.authenticationInsufficient();
						}
					}
					x.sendRedirect(x.encodeURL(r.getContextPath() + "/loginRequired.action?message=" + message));
				} else if (exception instanceof ServletException) {
					throw (ServletException)exception;
				} else  if (exception instanceof IOException) {
					throw (IOException)exception;
				} else if (exception instanceof RuntimeException) {
					throw (RuntimeException)exception;
				} else {
					throw new ServletException(exception);
				}
			}
		
		} finally {
			ApplicationProperties.setSessionId(null);
		}
	}

	public void destroy() {
	}
}
