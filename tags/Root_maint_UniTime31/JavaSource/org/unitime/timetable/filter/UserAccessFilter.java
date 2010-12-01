/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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

import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Roles;

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
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {

		if (!sAllowAll.equals(iAllow)) {
			if (request instanceof HttpServletRequest) {
				HttpServletRequest httpRequest = (HttpServletRequest)request;
				HttpServletResponse httpResponse = (HttpServletResponse)response; 
				HttpSession session = httpRequest.getSession();

				if (sAllowLoggedIn.equals(iAllow) && !Web.isLoggedIn(session)) {
					session.setAttribute("exception", new ServletException("Access Denied."));
					httpResponse.sendRedirect(httpRequest.getContextPath()+"/error.jsp");
					return;
				}
				
				User user = Web.getUser(session);
				if (sAllowAdmin.equals(iAllow) && (user==null || !user.getRole().equals(Roles.ADMIN_ROLE))) {
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
