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
package org.unitime.timetable.spring.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.User;
import org.unitime.timetable.util.LoginManager;

/**
 * @author Tomas Muller
 */
@Service("unitimeAuthenticationFailureHandler")
public class UniTimeAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	public UniTimeAuthenticationFailureHandler() {
		setDefaultFailureUrl("/login.action");
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {

		// Is already locked?
		if (exception != null && exception instanceof LockedException) {
			super.onAuthenticationFailure(request, response, exception);
			return;
		}
		
		LoginManager.addFailedLoginAttempt(request.getParameter("username"), new Date());
		
		if (ApplicationProperty.PasswordReset.isTrue() && User.findByUserName(request.getParameter("username")) != null)
			request.getSession().setAttribute("SUGGEST_PASSWORD_RESET", true);
		
		saveException(request, exception);

		String targetUrl = request.getParameter("target");
		if (StringUtils.hasText(targetUrl))
			getRedirectStrategy().sendRedirect(request, response, "/login.action" + "?target=" + URLEncoder.encode(targetUrl, "UTF-8"));
		else
			getRedirectStrategy().sendRedirect(request, response, "/login.action");
	}

}
