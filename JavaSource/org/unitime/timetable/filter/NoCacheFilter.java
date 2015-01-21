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

/**
 * @author Tomas Muller
 */
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
