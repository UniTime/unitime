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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseBoolean;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.MenuInterface.IsSessionBusyRpcRequest;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public class BusySessions {
	
	public static class Filter implements javax.servlet.Filter {
		private Tracker iTracker;
		
		protected Tracker getTracker(ServletRequest request) {
			if (iTracker == null) {
				WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
				iTracker = (Tracker)applicationContext.getBean("unitimeBusySessions");
			}
			return iTracker;
		}
		
		protected String increment(ServletRequest request) {
			if (request instanceof HttpServletRequest) {
				Tracker tracker = getTracker(request);
				if (tracker != null)
					return tracker.increment(((HttpServletRequest)request).getSession().getId());
			}
			return null;
		}
		
		protected void decrement(ServletRequest request, String id) {
			if (request instanceof HttpServletRequest && id != null) {
				Tracker tracker = getTracker(request);
				if (tracker != null)
					tracker.decrement(id);
			}
		}

		@Override
		public void destroy() {}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			String id = increment(request);
			try {
				chain.doFilter(request, response);
			} finally {
				decrement(request, id);
			}
		}

		@Override
		public void init(FilterConfig config) throws ServletException {
		}
	}
	
	public static class Listener implements HttpSessionListener  {
		private Tracker iTracker;
		
		protected Tracker getTracker(HttpSessionEvent event) {
			if (iTracker == null) {
				WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(event.getSession().getServletContext());
				iTracker = (Tracker)applicationContext.getBean("unitimeBusySessions");
			}
			return iTracker;
		}
		
		@Override
		public void sessionCreated(HttpSessionEvent event) {
			Tracker tracker = getTracker(event);
			if (tracker != null) tracker.create(event.getSession().getId());
		}

		@Override
		public void sessionDestroyed(HttpSessionEvent event) {
			Tracker tracker = getTracker(event);
			if (tracker != null) tracker.destroy(event.getSession().getId());
		}
	}
	
	@Service("unitimeBusySessions")
	public static class Tracker {
		private ConcurrentMap<String, Integer> iCounters = new ConcurrentHashMap<String, Integer>();
		
		public void create(String id) {
			iCounters.put(id, 0);
		}
		
		public void destroy(String id) {
			iCounters.remove(id);
		}
		
		public String increment(String id) {
			iCounters.merge(id, 1, (t, u) -> t + u);
			return id;
		}
		
		public void decrement(String id) {
			iCounters.merge(id, -1, (t, u) -> Math.max(0, t + u));
		}
		
		public boolean isWorking(String id) {
			if (id == null) return false;
			Integer counter = iCounters.get(id);
			return counter != null && counter > 1;
		}
		
		public boolean isActive(String id) {
			if (id == null) return false;
			return iCounters.containsKey(id);
		}
	}

	@GwtRpcImplements(IsSessionBusyRpcRequest.class)
	public static class Backend implements GwtRpcImplementation<IsSessionBusyRpcRequest, GwtRpcResponseBoolean> {
		private @Autowired Tracker unitimeBusySessions;
		
		@Override
		public GwtRpcResponseBoolean execute(IsSessionBusyRpcRequest request, SessionContext context) {
			return new GwtRpcResponseBoolean(unitimeBusySessions.isWorking(context.getHttpSessionId()));
		}

	}

}
