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
package org.unitime.timetable.action;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.util.LoginManager;


/**
 * MyEclipse Struts Creation date: 01-16-2007
 * 
 * XDoclet definition:
 * 
 * @struts.action
 */
@Service("/login")
public class LoginAction extends Action {
	/*
	 * Generated Methods
	 */
	
	@Autowired AuthenticationManager authenticationManager;

	/**
	 * Method execute
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String cs = request.getParameter("cs");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String menu = request.getParameter("menu");

		// Check form is submitted
		if (cs == null || !cs.equals("login")) {
		    String m = (String)request.getAttribute("message");
			response.sendRedirect(ApplicationProperties.getProperty("tmtbl.login_url")+(m==null?"":"?m="+m));
			return null;
		}
		
		if (username == null || username.length() == 0  || password == null || password.length() == 0) {
			response.sendRedirect(ApplicationProperties.getProperty("tmtbl.login_url")+"?e=1" + "&menu=" + menu);
			return null;
		}
		
		Date attemptDateTime = new Date();
		
		if (LoginManager.isUserLockedOut(username, attemptDateTime)){
			// count this attempt, allows for slowing down of responses if the user is flooding the system with failed requests
			LoginManager.addFailedLoginAttempt(username, attemptDateTime);
			//TODO figure out what the appropriate message is
			response.sendRedirect(ApplicationProperties.getProperty("tmtbl.login_url")+"?e=4" + "&menu=" + menu);
			return null;
		}
		
    	try {
    		Authentication authRequest = new UsernamePasswordAuthenticationToken(username,	password);
    		Authentication authResult = authenticationManager.authenticate(authRequest);
    		SecurityContextHolder.getContext().setAuthentication(authResult);
    		LoginManager.loginSuceeded(authResult.getName());
    		response.sendRedirect("selectPrimaryRole.do");
    	} catch (Exception e) {
			LoginManager.addFailedLoginAttempt(username, attemptDateTime);
			Debug.error(e.getMessage());
			response.sendRedirect(ApplicationProperties.getProperty("tmtbl.login_url")+"?e=3" + "&menu=" + menu);
    	}

		return null;
	}
}
