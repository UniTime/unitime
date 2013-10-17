/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.spring.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Service;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.User;
import org.unitime.timetable.util.LoginManager;

/**
 * @author Tomas Muller
 */
@Service("unitimeAuthenticationFailureHandler")
public class UniTimeAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	public UniTimeAuthenticationFailureHandler() {
		setDefaultFailureUrl("/login.jsp");
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
		
		LoginManager.addFailedLoginAttempt(request.getParameter("j_username"), new Date());
		
		if ("true".equals(ApplicationProperties.getProperty("unitime.password.reset", "true"))
				&& User.findByUserName(request.getParameter("j_username")) != null)
			request.getSession().setAttribute("SUGGEST_PASSWORD_RESET", true);
		
		super.onAuthenticationFailure(request, response, exception);
	}

}
