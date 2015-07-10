/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
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