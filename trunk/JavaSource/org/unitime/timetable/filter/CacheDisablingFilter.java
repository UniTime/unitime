/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Tomas Muller
 */
public class CacheDisablingFilter implements Filter {
	private static final long sOneDayInMilliseconds = 24l * 60l * 60l * 1000l;

	@Override
	public void init(FilterConfig fc) throws ServletException {
	}


	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            Date now = new Date();
            httpResponse.setDateHeader("Date", now.getTime());
            httpResponse.setDateHeader("Expires", now.getTime() + sOneDayInMilliseconds);
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
        }
        chain.doFilter(request, response);
		
	}

	@Override
	public void destroy() {
	}

}
