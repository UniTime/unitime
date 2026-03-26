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
package org.unitime.timetable.filter;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.security.UserContext;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class UserSettingsFilter implements Filter {
	private static Logger sLog = LogManager.getLogger(UserSettingsFilter.class);
	private static final  ThreadLocal<CachedUserSettings> sCachedUserSettings = new ThreadLocal<UserSettingsFilter.CachedUserSettings>();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		sCachedUserSettings.set(new CachedUserSettings());
		try {
			chain.doFilter(request, response);
		} finally {
			sCachedUserSettings.remove();
		}
	}
	
	public static void enableUserSettings(UserContext context) {
		sCachedUserSettings.set(new CachedUserSettings(context));
	}
	
	public static void releaseUserSettings() {
		sCachedUserSettings.remove();
	}
	
	public static String getUserSetting(UserProperty property) {
		CachedUserSettings cache = sCachedUserSettings.get();
		if (cache == null) {
			sLog.info("Get user settings " + property.key() + " is used outside of a HTTP request, returning the default value.");
			return property.defaultValue();
		}
		return cache.get(property);
	}
	
	private static class CachedUserSettings implements Serializable {
		private static final long serialVersionUID = 1L;
		private Map<UserProperty, String> iCache;
		private transient UserContext iUserContext;
		
		public CachedUserSettings() {}
		public CachedUserSettings(UserContext context) { iUserContext = context; }
		
		public String get(UserProperty property) {
			if (iCache == null) iCache = new HashMap<UserProperty, String>();
			String value = iCache.get(property);
			if (value == null) {
				value = property.get(getUser());
				iCache.put(property, value);
			}
			return value;
		}
		
		private UserContext getUser() {
			if (iUserContext != null) return iUserContext;
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserContext)
				return (UserContext)authentication.getPrincipal();
			return null;
		}

	}

}
