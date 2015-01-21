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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * 
 * @author Tomas Muller
 *
 */
public class UserAccessFilter implements Filter {
	public static String sAllowNone = "none";
	public static String sAllowAdmin = "admin";
	public static String sAllowLoggedIn = "logged-in";
	public static String sAllowAll = "all";
	
	private String iAllow = null;

	public void init(FilterConfig cfg) throws ServletException {
		iAllow = cfg.getInitParameter("allow");
	}
	
	private UserContext getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserContext)
			return (UserContext)authentication.getPrincipal();
		return null;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {

		if (!sAllowAll.equals(iAllow)) {
			if (request instanceof HttpServletRequest) {
				HttpServletRequest httpRequest = (HttpServletRequest)request;
				HttpServletResponse httpResponse = (HttpServletResponse)response; 
				HttpSession session = httpRequest.getSession();

				UserContext user = getUser();

				if (sAllowLoggedIn.equals(iAllow) && user != null) {
					session.setAttribute("exception", new ServletException("Access Denied."));
					httpResponse.sendRedirect(httpRequest.getContextPath()+"/error.jsp");
					return;
				}
				
				if (sAllowAdmin.equals(iAllow) && (user==null || user.getCurrentAuthority() == null || !user.getCurrentAuthority().hasRight(Right.IsAdmin))) {
					session.setAttribute("exception", new ServletException("Access Denied."));
					httpResponse.sendRedirect(httpRequest.getContextPath()+"/error.jsp");
					return;
				}
				
	        } else {
	        	throw new ServletException("Access Denied.");
	        }
		}

		// Process request
		chain.doFilter(request,response);
	}

	public void destroy() {
	}
}
