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

import org.apache.commons.text.StringEscapeUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.StartupService;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

/**
 * @author Tomas Muller
 */
@Action(value = "main", results = {
		@Result(name = "main", type = "tiles", location = "main.tiles")
})
@TilesDefinition(name = "main.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "University Timetabling Application"),
		@TilesPutAttribute(name = "body", value = "/main.jsp"),
		@TilesPutAttribute(name = "checkLogin", value = "false"),
		@TilesPutAttribute(name = "checkRole", value = "false"),
		@TilesPutAttribute(name = "updateRegistration", value="true")
	})
public class MainAction extends UniTimeAction<BlankForm>{
	private static final long serialVersionUID = 578394783461408015L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String message;
	
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	
	public String getSystemMessage() {
		return ApplicationProperty.SystemMessage.value();
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
		if (message != null && !message.isEmpty())
			message = StringEscapeUtils.escapeHtml4(message);
		else if (message == null)
			message = getSystemMessage();
		if ("cas-logout".equals(op)) {
			message = MSG.casLoggedOut();
		}
		return "main";
	}
}
