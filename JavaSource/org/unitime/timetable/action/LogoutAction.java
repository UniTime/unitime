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

import java.util.Enumeration;

import javax.servlet.http.Cookie;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.unitime.timetable.form.BlankForm;

/**
 * @author Tomas Muller
 */
@Action(value = "logout", results = {
		@Result(name = "logout", location = "/logout.jsp")
})
public class LogoutAction extends UniTimeAction<BlankForm>{
	private static final long serialVersionUID = 3587561668566677588L;
	
	public String execute() throws Exception {
		Cookie cookie = new Cookie("loggedOn", "false" );    	
		response.addCookie( cookie );

		Enumeration<String> e = request.getSession().getAttributeNames();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			request.getSession().setAttribute(key, null);
		}	
		request.getSession().invalidate(); 	

		return "logout";
	}
}
