/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NoCacheFilter implements Filter {
	private Pattern iUserAgent = null;
	
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		iUserAgent = Pattern.compile(cfg.getInitParameter("user-agent"));
	}
	
	@Override
	public void destroy() {
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			String agent = ((HttpServletRequest)request).getHeader("user-agent");
			if (agent != null && iUserAgent.matcher(agent).find())
				response = new HttpServletResponseWrapper((HttpServletResponse)response).createResponse();
		}
		chain.doFilter(request,response);
	}
	
	private static class HttpServletResponseWrapper implements InvocationHandler {
		private HttpServletResponse iResponse;
		
		public HttpServletResponseWrapper(HttpServletResponse r) {
			iResponse = r;
		}

		public HttpServletResponse createResponse() {
			return (HttpServletResponse)Proxy.newProxyInstance(QueryLogFilter.class.getClassLoader(), new Class[] {HttpServletResponse.class}, this);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("sendRedirect".equals(method.getName())) {
				String location = (String)args[0];
				String hash = null;
				int hashIdx = location.lastIndexOf('#');
				if (hashIdx >= 0) {
					hash = location.substring(hashIdx);
					location = location.substring(0, hashIdx);
				}
				String redirect = location + (location.indexOf('?') >= 0 ? "&" : "?") + "noCacheTS=" + new Date().getTime() + (hash == null ? "" : hash);
				iResponse.setHeader("Pragma", "no-cache" );
				iResponse.addHeader("Cache-Control", "must-revalidate" );
				iResponse.addHeader("Cache-Control", "no-cache" );
				iResponse.addHeader("Cache-Control", "no-store" );
				iResponse.setDateHeader("Expires", 0);
				iResponse.sendRedirect(redirect);
				return null;
			}
			return iResponse.getClass().getMethod(method.getName(),method.getParameterTypes()).invoke(iResponse, args);
		}
	}
	
}
