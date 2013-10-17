/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.timetable.security.UserContext;

/**
 * @author Tomas Muller
 */
public class MessageLogFilter implements Filter {
	
	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void destroy() {
		for (Enumeration e = LogManager.getCurrentLoggers(); e.hasMoreElements(); ) {
			Logger logger = (Logger)e.nextElement();
			logger.removeAppender("mlog");
		}
		LogManager.getRootLogger().removeAppender("mlog");
	}
	
	private UserContext getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserContext)
			return (UserContext)authentication.getPrincipal();
		return null;
	}
	
	private int ndcPush() {
		int count = 0;
		try {
			UserContext user = getUser();
			if (user != null) {
				NDC.push("uid:" + user.getExternalUserId()); count++;
				if (user.getCurrentAuthority() != null) {
					NDC.push("role:" + user.getCurrentAuthority().getRole()); count++;
					Long sessionId = user.getCurrentAcademicSessionId();
					if (sessionId != null) {
						NDC.push("sid:" + sessionId); count++;
					}
				}
			}
		} catch (Exception e) {}
		return count;
	}
	
	private void ndcPop(int count) {
		for (int i = 0; i < count; i++)
			NDC.pop();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
		int count = ndcPush();

		try {
			
			chain.doFilter(request,response);
			
		} finally {
			ndcPop(count);
		}
		
	}

}
