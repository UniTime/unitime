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
package org.unitime.timetable.api;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.CacheMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.onlinesectioning.HasCacheMode;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.AnonymousUserContext;

/**
 * @author Tomas Muller
 */
public abstract class ApiConnector {
	@Autowired protected SessionContext sessionContext;
	
	@Autowired protected ApiToken apiToken;
	
	public void doGet(ApiHelper helper) throws IOException {
		helper.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}
	
	public void doPut(ApiHelper helper) throws IOException {
		helper.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}
	
	public void doPost(ApiHelper helper) throws IOException {
		helper.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}
	
	public void doDelete(ApiHelper helper) throws IOException {
		helper.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}
	
	protected ApiHelper createHelper(HttpServletRequest request, HttpServletResponse response) {
		return new JsonApiHelper(request, response, sessionContext, getCacheMode());
	}
	
	protected abstract String getName();
	
	protected CacheMode getCacheMode() {
		String cacheMode = (getName() != null && !getName().isEmpty() ? ApplicationProperty.ApiCacheMode.value(getName()) : null);
		return cacheMode != null ? CacheMode.valueOf(cacheMode) : this instanceof HasCacheMode ? ((HasCacheMode)this).getCacheMode() : null;
	}
	
	protected void authenticateWithTokenIfNeeded(HttpServletRequest request, HttpServletResponse response) {
		if ((!sessionContext.isAuthenticated() || sessionContext.getUser() instanceof AnonymousUserContext) && request.getParameter("token") != null && ApplicationProperty.ApiCanUseAPIToken.isTrue()) {
			UserContext context = apiToken.getContext(request.getParameter("token"));
			if (context != null) {
				SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(context));
			}
		}
	}
	
	protected void revokeTokenAuthenticationIfNeeded(HttpServletRequest request, HttpServletResponse response) {
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		authenticateWithTokenIfNeeded(request, response);
		ApiHelper helper = createHelper(request, response);
		try {
			doGet(helper);
		} finally {
			helper.close();
			revokeTokenAuthenticationIfNeeded(request, response);
		}
	}
	
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
		authenticateWithTokenIfNeeded(request, response);
		ApiHelper helper = createHelper(request, response);
		try {
			doPut(helper);
		} finally {
			helper.close();
			revokeTokenAuthenticationIfNeeded(request, response);
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		authenticateWithTokenIfNeeded(request, response);
		ApiHelper helper = createHelper(request, response);
		try {
			doPost(helper);
		} finally {
			helper.close();
			revokeTokenAuthenticationIfNeeded(request, response);
		}
	}
	
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		authenticateWithTokenIfNeeded(request, response);
		ApiHelper helper = createHelper(request, response);
		try {
			doDelete(helper);
		} finally {
			helper.close();
			revokeTokenAuthenticationIfNeeded(request, response);
		}
	}
	
	protected static class TokenAuthentication implements Authentication {
		private static final long serialVersionUID = 1L;
		private UserContext iContext;
		
		public TokenAuthentication(UserContext context) {
			iContext = context;
		}

		@Override
		public String getName() {
			return iContext.getName();
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return iContext.getAuthorities();
		}

		@Override
		public Object getCredentials() {
			return iContext.getPassword();
		}

		@Override
		public Object getDetails() {
			return null;
		}

		@Override
		public Object getPrincipal() {
			return iContext;
		}

		@Override
		public boolean isAuthenticated() {
			return true;
		}

		@Override
		public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
			throw new IllegalArgumentException("Operation not supported.");
		}
	}
}