/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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