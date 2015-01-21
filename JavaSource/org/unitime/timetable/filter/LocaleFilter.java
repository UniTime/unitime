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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class LocaleFilter implements Filter {
	private boolean iUseBrowserSettings;
	
	@Override
	public void init(FilterConfig fc) throws ServletException {
		iUseBrowserSettings = "true".equals(fc.getInitParameter("use-browser-settings"));
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		try {
			if (req instanceof HttpServletRequest) {
				HttpServletRequest request = (HttpServletRequest)req;
				String locale = null;
				
				// Try HTTP header, Accept-Language field
				if (iUseBrowserSettings)
					locale = request.getHeader("Accept-Language");
				
				// Try locale parameter (use http session to store)
				if (req.getParameter("locale") != null) {
					locale = req.getParameter("locale");
					request.getSession().setAttribute("unitime.locale", locale);
				} else if (request.getSession().getAttribute("unitime.locale") != null) {
					locale = (String)request.getSession().getAttribute("unitime.locale");
				}
				
				// Fall back to unitime.locale
				if (locale == null)
					locale = ApplicationProperty.Locale.value();
				
				Localization.setLocale(locale);
			}
			chain.doFilter(req, resp);
		} finally {
			Localization.removeLocale();
			Formats.removeFormats();
		}
	}

	@Override
	public void destroy() {
	}

}