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
package org.unitime.timetable.gwt.command.server;

import java.io.Serializable;

import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.evaluation.PermissionCheck;
import org.unitime.timetable.security.rights.Right;

public class GwtRpcHelper implements SessionContext {
	private UserContext iUser;
	private String iHttpSessionId;
	private boolean iHttpSessionNew;
	private PermissionCheck iCheck;
	
	public GwtRpcHelper(SessionContext context, PermissionCheck check) {
		iUser = context.getUser();
		iHttpSessionId = context.getHttpSessionId();
		iHttpSessionNew = context.isHttpSessionNew();
		iCheck = check;
	}

	@Override
	public boolean isAuthenticated() { return iUser != null; }

	@Override
	public UserContext getUser() { return iUser; }

	@Override
	public boolean isHttpSessionNew() { return iHttpSessionNew; }

	@Override
	public String getHttpSessionId() { return iHttpSessionId; }

	@Override
	public Object getAttribute(String name) { throw new RuntimeException("Operation not supported."); }

	@Override
	public void removeAttribute(String name) { throw new RuntimeException("Operation not supported."); }

	@Override
	public void setAttribute(String name, Object value) { throw new RuntimeException("Operation not supported."); }

	@Override
	public void removeAttribute(SessionAttribute attribute) {
		removeAttribute(attribute.name());
	}

	@Override
    public void setAttribute(SessionAttribute attribute, Object value) {
		setAttribute(attribute.name(), value);
	}
	
	@Override
    public Object getAttribute(SessionAttribute attribute) {
    	Object value = getAttribute(attribute.name());
    	return (value != null ? value : attribute.defaultValue());
    }

	@Override
	public void checkPermission(Right right) {
		iCheck.checkPermission(getUser(), null, null, right);
	}

	@Override
	public void checkPermission(Serializable targetId, String targetType, Right right) {
		iCheck.checkPermission(getUser(), targetId, targetType, right);
	}

	@Override
	public void checkPermission(Object targetObject, Right right) {
		iCheck.checkPermission(getUser(), targetObject, right);
	}
	
	@Override
	public boolean hasPermission(Right right) {
		return iCheck.hasPermission(getUser(), null, null, right);
	}

	@Override
	public boolean hasPermission(Serializable targetId, String targetType, Right right) {
		return iCheck.hasPermission(getUser(), targetId, targetType, right);
	}

	@Override
	public boolean hasPermission(Object targetObject, Right right) {
		return iCheck.hasPermission(getUser(), targetObject, right);
	}
	
	@Override
	public void checkPermissionAnyAuthority(Right right, Qualifiable... filter) {
		iCheck.checkPermissionAnyAuthority(getUser(), null, null, right, filter);
	}

	@Override
	public void checkPermissionAnyAuthority(Serializable targetId, String targetType, Right right, Qualifiable... filter) {
		iCheck.checkPermissionAnyAuthority(getUser(), targetId, targetType, right, filter);
	}

	@Override
	public void checkPermissionAnyAuthority(Object targetObject, Right right, Qualifiable... filter) {
		iCheck.checkPermissionAnyAuthority(getUser(), targetObject, right, filter);
	}
	
	@Override
	public boolean hasPermissionAnyAuthority(Right right, Qualifiable... filter) {
		return iCheck.hasPermissionAnyAuthority(getUser(), null, null, right, filter);
	}

	@Override
	public boolean hasPermissionAnyAuthority(Serializable targetId, String targetType, Right right, Qualifiable... filter) {
		return iCheck.hasPermissionAnyAuthority(getUser(), targetId, targetType, right, filter);
	}

	@Override
	public boolean hasPermissionAnyAuthority(Object targetObject, Right right, Qualifiable... filter) {
		return iCheck.hasPermissionAnyAuthority(getUser(), targetObject, right, filter);
	}
}
