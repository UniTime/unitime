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
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.JProf;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.QueryLog;
import org.unitime.timetable.model.dao.QueryLogDAO;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.spring.gwt.GwtDispatcherServlet;
import org.unitime.timetable.spring.gwt.GwtDispatcherServlet.GwtCallInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Tomas Muller
 */
public class QueryLogFilter implements Filter {
	private static Log sLog = LogFactory.getLog(QueryLogFilter.class);
	private Saver iSaver;
	private HashSet<String> iExclude = new HashSet<String>();
	private Gson iGson = null;

	public void init(FilterConfig cfg) throws ServletException {
		iSaver = new Saver();
		iSaver.start();
		String exclude = cfg.getInitParameter("exclude");
		if (exclude != null) {
			for (String x: exclude.split(","))
				iExclude.add(x);
		}
		iGson = new GsonBuilder().create();
	}
	
	private UserContext getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserContext)
			return (UserContext)authentication.getPrincipal();
		return null;
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain ) throws ServletException, IOException {

		String sessionId = null;
		String userId = null;
		try {
			if (request instanceof HttpServletRequest) {
				HttpServletRequest r = (HttpServletRequest)request;
				sessionId = r.getSession().getId();
				UserContext user = getUser();
				if (user != null)
					userId = user.getExternalUserId();
			}
		} catch (IllegalStateException e) {}
		
		long t0 = JProf.currentTimeMillis();
		Throwable exception = null;
		try {
			chain.doFilter(request,response);
		} catch (Throwable t) {
			exception = t;
		}
		long t1 = JProf.currentTimeMillis();
		
		if (exception == null) {
			Object ex = request.getAttribute("__exception");
			if (ex != null && ex instanceof Throwable)
				exception = (Throwable)ex;
		}
		
		if (request instanceof HttpServletRequest) {
			HttpServletRequest r = (HttpServletRequest)request;
			QueryLog q = new QueryLog();
			String uri = r.getRequestURI();
			if (uri.indexOf('/') >= 0)
				uri = uri.substring(uri.lastIndexOf('/') + 1);
			if (uri.endsWith(".do"))
				q.setType(QueryLog.Type.STRUCTS.ordinal());
			else if (uri.endsWith(".gwt"))
				q.setType(QueryLog.Type.GWT.ordinal());
			else
				q.setType(QueryLog.Type.OTHER.ordinal());
			q.setUri(uri);
			q.setTimeStamp(new Date());
			q.setTimeSpent(t1 - t0);
			q.setSessionId(sessionId);
			q.setUid(userId);
			try {
				if (sessionId == null)
					q.setSessionId(r.getSession().getId());
				if (userId == null) {
					UserContext user = getUser();
					if (user != null)
						q.setUid(user.getExternalUserId());
				}
			} catch (IllegalStateException e) {}
			GwtCallInfo callInfo = GwtDispatcherServlet.getLastQuery();
			if (callInfo != null) {
				q.setQuery(callInfo.getQuery());
				q.setUri(q.getUri() + ": " + callInfo.getTarget());
			} else if (ApplicationProperty.QueryLogJSON.isTrue()) {
				try {
					Map<String, Object> params = new HashMap<String, Object>();
					for (Map.Entry<String, String[]> e: r.getParameterMap().entrySet()) {
						if ("password".equals(e.getKey()) || "noCacheTS".equals(e.getKey())) continue;
						if (e.getValue() == null || e.getValue().length == 0) continue;
						if (e.getValue().length == 1)
							params.put(e.getKey(), e.getValue()[0]);
						else
							params.put(e.getKey(), e.getValue());
					}
					q.setQuery(iGson.toJson(params));
				} catch (Throwable t) {}
			} else {
				String params = "";
				for (Enumeration e=r.getParameterNames(); e.hasMoreElements();) {
					String n = (String)e.nextElement();
					if ("password".equals(n) || "noCacheTS".equals(n)) continue;
					if (!params.isEmpty()) params += "&";
					params += n + "=" + r.getParameter(n);
				}
				if (!params.isEmpty())
					q.setQuery(params);
			}
			if (exception != null) {
				Throwable t = exception;
				String ex = "";
				while (t != null) {
					String clazz = t.getClass().getName();
					if (clazz.indexOf('.') >= 0) clazz = clazz.substring(1 + clazz.lastIndexOf('.'));
					if (!ex.isEmpty()) ex += "\n";
					ex += clazz + ": " + t.getMessage();
					if (t.getStackTrace() != null && t.getStackTrace().length > 0)
						ex += " (at " + t.getStackTrace()[0].getFileName() + ":" + t.getStackTrace()[0].getLineNumber() + ")";
					t = t.getCause();
				}
				if (!ex.isEmpty())
					q.setException(ex);
			}
			if (!iExclude.contains(q.getUri()) || q.getException() != null) {
				if (iSaver != null) iSaver.add(q);
			}
		}
		
		if (exception!=null) {
			if (exception instanceof ServletException)
				throw (ServletException)exception;
			if (exception instanceof IOException)
				throw (IOException)exception;
			if (exception instanceof RuntimeException)
				throw (RuntimeException)exception;
			throw new ServletException(exception);
		}
	}

	public void destroy() {
		if (iSaver != null)
			iSaver.interrupt();
	}
	
	public static class Saver extends Thread {
		private List<QueryLog> iQueries = new Vector<QueryLog>();
		private boolean iActive = true;
		private int iLogLimit = -1;
		
		public Saver() {
			super("QueryLogSaver");
			iLogLimit = ApplicationProperty.QueryLogLimit.intValue();
			setDaemon(true);
		}
		
		@Override
		public void interrupt() {
			iActive = false;
			super.interrupt();
			try { join(); } catch (InterruptedException e) {}
		}
		
		public void add(QueryLog q) {
			if (!iActive) return;
			synchronized (iQueries) {
				if (iLogLimit <= 0 || iQueries.size() < iLogLimit)
					iQueries.add(q);
			}
		}
		
		public void run() {
			sLog.debug("Query Log Saver is up.");
			while (true) {
				try {
					try {
						sleep(60000);
					} catch (InterruptedException e) {
					}
					List<QueryLog> queriesToSave = null;
					synchronized (iQueries) {
						if (!iQueries.isEmpty()) {
							queriesToSave = new ArrayList<QueryLog>(iQueries);
							iQueries.clear();
						}
					}
					if (queriesToSave != null) {
						sLog.debug("Persisting " + queriesToSave.size() + " log entries...");
						if (iLogLimit > 0 && queriesToSave.size() >= iLogLimit)
							sLog.warn("The limit of " + iLogLimit + " unpersisted log messages was reached, some messages have been dropped.");
						Session hibSession = QueryLogDAO.getInstance().createNewSession();
						hibSession.setCacheMode(CacheMode.IGNORE);
						Transaction tx = hibSession.beginTransaction();
						try {
							for (QueryLog q: queriesToSave)
								hibSession.save(q);
							hibSession.flush();
							tx.commit();
						} catch (Exception e) {
							tx.rollback();
							sLog.error("Failed to persist " + queriesToSave.size() + " log entries:" + e.getMessage(), e);
						} finally {
							hibSession.close();
						}
					}
					if (!iActive) break;
				} catch (Exception e) {
					sLog.error("Failed to persist log entries:" + e.getMessage(), e);
				}
			}
			sLog.debug("Query Log Saver is down.");
		}
		
	}
}
