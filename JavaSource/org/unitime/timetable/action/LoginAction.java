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
package org.unitime.timetable.action;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.SecurityMessages;
import org.unitime.timetable.StartupService;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

/**
 * @author Tomas Muller
 */
@Action(value = "login", results = {
		@Result(name = "login", location = "/login.jsp"),
		@Result(name = "selectPrimaryRole", type = "redirect", location = "/selectPrimaryRole.action"),
})
public class LoginAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = -7818620515562212897L;
	protected static SecurityMessages MSG = Localization.create(SecurityMessages.class);
	private int error = 0;
	private String menu;
	private String target;
	
	public Integer getE() { return error; }
	public void setE(Integer error) { this.error = error; }
	public String getErrorMsg() {
		switch(error) {
		case 1: return MSG.errorInvalidUserPasswd();
		case 2: return MSG.errorAuthenticationFailed();
		case 3: return MSG.errorAuthenticationFailed();
		case 4: return MSG.errorUserLockedOut();
		default: return null;
		}
	}
	
	public String getMenu() { return menu; }
	public void setMenu(String menu) { this.menu = menu; }
	
	public String getTarget() { return target; }
	public void setTarget(String target) { this.target = target; }
	
	public String getExternalHeader() {
		return ApplicationProperty.LoginPageHeader.value();
	}
	
	public String getExternalFooter() {
		return ApplicationProperty.LoginPageFooter.value();
	}
	
	protected StartupService getStartupService() {
		return (StartupService)SpringApplicationContextHolder.getBean("startupService");
	}
	
	public boolean getHasInitializationError() {
		return getStartupService().getInitializationException() != null;
	}
	
	public void printInitializationError() throws IOException {
		Throwable t = getStartupService().getInitializationException();
		while (t != null) {
			String clazz = t.getClass().getName();
			if (clazz.indexOf('.') >= 0) clazz = clazz.substring(1 + clazz.lastIndexOf('.'));
			getPageContext().getOut().println(
					"<br>" + clazz + ": " + t.getMessage() + (t.getStackTrace() != null && t.getStackTrace().length > 0 ? " (at " + t.getStackTrace()[0].getFileName() + ":" + t.getStackTrace()[0].getLineNumber() + ")": ""));
			t = t.getCause();
		}
	}

	public String execute() throws Exception {
		if ("forward".equalsIgnoreCase(ApplicationProperty.LoginMethod.value())) {
			String login = ApplicationProperty.LoginPage.value();
			
			if (login == null || "login.jsp".equals(login) || "/login.jsp".equals(login) || "login".equals(login))
				return "login";
			else if ("selectPrimaryRole.do".equals(login) || "/selectPrimaryRole.do".equals(login) || "selectPrimaryRole".equals(login))
				return "selectPrimaryRole";
			
			request.getRequestDispatcher(ApplicationProperty.LoginPage.value()).forward(request, response);
		}

		if (target == null)
			response.sendRedirect(ApplicationProperty.LoginPage.value());
		else
			response.sendRedirect(ApplicationProperty.LoginPage.value() + "?target=" + URLEncoder.encode(target, "UTF-8"));
		return null;
	}
}
