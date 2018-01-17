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
    public void checkPermissionAnySession(Right right, Qualifiable... filter);
    public void checkPermissionAnySession(Serializable targetId, String targetType, Right right, Qualifiable... filter);
    public void checkPermissionAnySession(Object targetObject, Right right, Qualifiable... filter);
    public boolean hasPermission(Right right);
    public boolean hasPermission(Serializable targetId, String targetType, Right right);
    public boolean hasPermission(Object targetObject, Right right);
    public boolean hasPermissionAnyAuthority(Right right, Qualifiable... filter);
    public boolean hasPermissionAnyAuthority(Serializable targetId, String targetType, Right right, Qualifiable... filter);
    public boolean hasPermissionAnyAuthority(Object targetObject, Right right, Qualifiable... filter);
    public boolean hasPermissionAnySession(Right right, Qualifiable... filter);
    public boolean hasPermissionAnySession(Serializable targetId, String targetType, Right right, Qualifiable... filter);
    public boolean hasPermissionAnySession(Object targetObject, Right right, Qualifiable... filter);
}