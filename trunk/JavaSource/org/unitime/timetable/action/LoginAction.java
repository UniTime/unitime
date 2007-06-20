/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.action;

import java.util.Iterator;
import java.util.Set;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.authenticate.jaas.UserPasswordHandler;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.util.Constants;


/**
 * MyEclipse Struts Creation date: 01-16-2007
 * 
 * XDoclet definition:
 * 
 * @struts.action
 */
public class LoginAction extends Action {
	/*
	 * Generated Methods
	 */

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

		// Check form is submitted
		if (cs == null || !cs.equals("login")) {
			response.sendRedirect(ApplicationProperties.getProperty("tmtbl.login_url"));
			return null;
		}
		
		if (username == null || username.length() == 0 
				|| password == null || password.length() == 0) {

			response.sendRedirect(ApplicationProperties.getProperty("tmtbl.login_url")+"?e=1");
			return null;
		}

		try {
			UserPasswordHandler handler = new UserPasswordHandler(username,	password);
			LoginContext lc = new LoginContext("Timetabling", handler);
			lc.login();
			
			Set creds = lc.getSubject().getPublicCredentials();
			if (creds==null || creds.size()==0) {
				response.sendRedirect(ApplicationProperties.getProperty("tmtbl.login_url")+"?e=2");
				return null;
			}
			
			for (Iterator i=creds.iterator(); i.hasNext(); ) {
				Object o = i.next();
				if (o instanceof User) {
					User user = (User) o;
					
					// Set Session Variables
					HttpSession session = request.getSession();
					session.setAttribute("loggedOn", "true");
					session.setAttribute("hdnCallingScreen", "main.jsp");
					Web.setUser(session, user);
					
					// Check App Status 
					String appStatus = ApplicationConfig.getConfigValue(Constants.CFG_APP_ACCESS_LEVEL, Constants.APP_ACL_ALL);
					session.setAttribute(Constants.CFG_APP_ACCESS_LEVEL, appStatus);
                    
                    session.setAttribute("authUserExtId", user.getId());
					
				    response.sendRedirect("selectPrimaryRole.do");
					break;
				}
			}
			 
		} 
		catch (LoginException le) {
			Debug.error(le.getMessage());
			response.sendRedirect(ApplicationProperties.getProperty("tmtbl.login_url")+"?e=3");
		}

		return null;
	}
}