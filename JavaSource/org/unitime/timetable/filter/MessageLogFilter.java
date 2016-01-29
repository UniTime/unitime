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
				NDC.push("uid:" + user.getTrueExternalUserId()); count++;
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
