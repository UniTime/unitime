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
package org.unitime.timetable.security.context;

import java.io.Serializable;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.evaluation.PermissionCheck;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class HttpSessionContext implements SessionContext {
	@Autowired
	private HttpSession iSession;
	@Autowired
	PermissionCheck unitimePermissionCheck;

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
	public void removeAttribute(SessionAttribute attribute) {
		removeAttribute(attribute.key());
	}
	
	@Override
    public void setAttribute(SessionAttribute attribute, Object value) {
		setAttribute(attribute.key(), value);
	}
	
	@Override
    public Object getAttribute(SessionAttribute attribute) {
    	Object value = getAttribute(attribute.key());
    	return (value != null ? value : attribute.defaultValue());
    }

	@Override
	public boolean isHttpSessionNew() {
		return iSession.isNew();
	}

	@Override
	public UserContext getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserContext)
			return (UserContext)authentication.getPrincipal();
		return null;
	}
	
	@Override
	public boolean isAuthenticated() {
		return getUser() != null;
	}

	@Override
	public String getHttpSessionId() {
		try {
			return iSession.getId();
		} catch (IllegalStateException e) {
			return null;
		}
	}
	
	@Override
	public void checkPermission(Right right) {
		unitimePermissionCheck.checkPermission(getUser(), null, null, right);
	}

	@Override
	public void checkPermission(Serializable targetId, String targetType, Right right) {
		unitimePermissionCheck.checkPermission(getUser(), targetId, targetType, right);
	}

	@Override
	public void checkPermission(Object targetObject, Right right) {
		unitimePermissionCheck.checkPermission(getUser(), targetObject, right);
	}
	
	@Override
	public boolean hasPermission(Right right) {
		return unitimePermissionCheck.hasPermission(getUser(), null, null, right);
	}

	@Override
	public boolean hasPermission(Serializable targetId, String targetType, Right right) {
		return unitimePermissionCheck.hasPermission(getUser(), targetId, targetType, right);
	}

	@Override
	public boolean hasPermission(Object targetObject, Right right) {
		return unitimePermissionCheck.hasPermission(getUser(), targetObject, right);
	}
	
	@Override
	public void checkPermissionAnyAuthority(Right right, Qualifiable... filter) {
		unitimePermissionCheck.checkPermissionAnyAuthority(getUser(), null, null, right, filter);
	}

	@Override
	public void checkPermissionAnyAuthority(Serializable targetId, String targetType, Right right, Qualifiable... filter) {
		unitimePermissionCheck.checkPermissionAnyAuthority(getUser(), targetId, targetType, right, filter);
	}

	@Override
	public void checkPermissionAnyAuthority(Object targetObject, Right right, Qualifiable... filter) {
		unitimePermissionCheck.checkPermissionAnyAuthority(getUser(), targetObject, right, filter);
	}
	
	@Override
	public boolean hasPermissionAnyAuthority(Right right, Qualifiable... filter) {
		return unitimePermissionCheck.hasPermissionAnyAuthority(getUser(), null, null, right, filter);
	}

	@Override
	public boolean hasPermissionAnyAuthority(Serializable targetId, String targetType, Right right, Qualifiable... filter) {
		return unitimePermissionCheck.hasPermissionAnyAuthority(getUser(), targetId, targetType, right, filter);
	}

	@Override
	public boolean hasPermissionAnyAuthority(Object targetObject, Right right, Qualifiable... filter) {
		return unitimePermissionCheck.hasPermissionAnyAuthority(getUser(), targetObject, right, filter);
	}
	
	public static SessionContext getSessionContext(ServletContext context) {
		return (SessionContext) WebApplicationContextUtils.getWebApplicationContext(context).getBean("sessionContext");
	}
}