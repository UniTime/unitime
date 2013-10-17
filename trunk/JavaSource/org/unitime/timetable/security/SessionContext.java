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
package org.unitime.timetable.security;

import java.io.Serializable;

import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public interface SessionContext {
	
	public boolean isAuthenticated();
	public UserContext getUser();
    
    public boolean isHttpSessionNew();
    public String getHttpSessionId();
    
    public Object getAttribute(String name);
    public void removeAttribute(String name);
    public void setAttribute(String name, Object value);
    public void removeAttribute(SessionAttribute attribute);
    public void setAttribute(SessionAttribute attribute, Object value);
    public Object getAttribute(SessionAttribute attribute);

    public void checkPermission(Right right);
    public void checkPermission(Serializable targetId, String targetType, Right right);
    public void checkPermission(Object targetObject, Right right);
    public void checkPermissionAnyAuthority(Right right, Qualifiable... filter);
    public void checkPermissionAnyAuthority(Serializable targetId, String targetType, Right right, Qualifiable... filter);
    public void checkPermissionAnyAuthority(Object targetObject, Right right, Qualifiable... filter);
    public boolean hasPermission(Right right);
    public boolean hasPermission(Serializable targetId, String targetType, Right right);
    public boolean hasPermission(Object targetObject, Right right);
    public boolean hasPermissionAnyAuthority(Right right, Qualifiable... filter);
    public boolean hasPermissionAnyAuthority(Serializable targetId, String targetType, Right right, Qualifiable... filter);
    public boolean hasPermissionAnyAuthority(Object targetObject, Right right, Qualifiable... filter);
}