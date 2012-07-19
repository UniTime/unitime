/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
