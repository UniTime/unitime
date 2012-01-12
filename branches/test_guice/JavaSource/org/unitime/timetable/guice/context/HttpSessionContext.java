/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.guice.context;

import javax.servlet.http.HttpSession;

import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.UserData;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class HttpSessionContext implements SessionContext {
	private HttpSession iSession;
	
	@Inject
	public HttpSessionContext(HttpSession session) {
		iSession = session;
	}
	
	@Override
	public Object getAttribute(String name) {
		return iSession.getAttribute(name);
	}

	@Override
	public void removeAttribute(String name) {
		iSession.removeAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		iSession.setAttribute(name, value);
	}

	@Override
	public boolean isNew() {
		return iSession.isNew();
	}

	@Override
	public User getUser() {
		return Web.getUser(iSession);
	}

	@Override
	public void setUserProperty(String name, String value) {
		UserData.setProperty(iSession, name, value);
	}

	@Override
	public String getUserProperty(String name) {
		return UserData.getProperty(iSession, name);
	}
}
