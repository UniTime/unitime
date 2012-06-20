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
package org.unitime.timetable.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;

public class HttpSessionContext implements SessionContext {
	@Autowired
	private HttpSession iSession;
	@Autowired
	private HttpServletRequest iRequest;

	public HttpSessionContext() {
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
	public boolean isHttpSessionNew() {
		return iSession.isNew();
	}

	@Override
	public UserContext getUser() {
		User user = (User)iSession.getAttribute(Web.USER_ATTR_NAME);
		return (user == null ? null : new LegacyUserContext(user));
	}
	
	@Override
	public boolean isAuthenticated() {
		return getUser() != null;
	}

	@Override
	public HttpSession getHttpSession() {
		return iSession;
	}

	@Override
	public HttpServletRequest getHttpServletRequest() {
		return iRequest;
	}

	@Override
	public String getHttpSessionId() {
		try {
			return iSession.getId();
		} catch (IllegalStateException e) {
			return null;
		}
	}
}