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
import org.unitime.timetable.spring.SpringApplicationContextHolder;

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
	
	public HttpSessionContext(HttpSession session) {
		iSession = session;
		unitimePermissionCheck = (PermissionCheck)SpringApplicationContextHolder.getBean("unitimePermissionCheck");
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