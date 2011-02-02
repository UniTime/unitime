/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.sf.cpsolver.ifs.util.JProf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.QueryLog;
import org.unitime.timetable.model.dao.QueryLogDAO;

public class QueryLogFilter implements Filter {
	private static Log sLog = LogFactory.getLog(QueryLogFilter.class);
	private List<QueryLog> iQueries = new Vector<QueryLog>();
	private boolean iActive = false;
	private Saver iSaver;
	private HashSet<String> iExclude = new HashSet<String>();

	public void init(FilterConfig cfg) throws ServletException {
		iActive = true;
		Saver iSaver = new Saver();
		iSaver.start();
		String exclude = cfg.getInitParameter("exclude");
		if (exclude != null) {
			for (String x: exclude.split(","))
				iExclude.add(x);
		}
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain ) throws ServletException, IOException {

		String sessionId = null;
		String userId = null;
		try {
			if (request instanceof HttpServletRequest) {
				HttpServletRequest r = (HttpServletRequest)request;
				sessionId = r.getSession().getId();
				User user = Web.getUser(r.getSession());
				if (user != null)
					userId = user.getId();
			}
		} catch (IllegalStateException e) {}
		
		HttpServletRequestWrapper wrapper = null;
		if (request instanceof HttpServletRequest) {
			HttpServletRequest r = (HttpServletRequest)request;
			if (r.getRequestURI().endsWith(".gwt")) {
				wrapper = new HttpServletRequestWrapper(r);
				request = wrapper.createRequest();
			}
		}
		
		long t0 = JProf.currentTimeMillis();
		Throwable exception = null;
		try {
			chain.doFilter(request,response);
		} catch (Throwable t) {
			exception = t;
		}
		long t1 = JProf.currentTimeMillis();
		
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
					User user = Web.getUser(r.getSession());
					if (user != null)
						q.setUid(user.getId());
				}
			} catch (IllegalStateException e) {}
			if (wrapper != null && wrapper.getBody() != null) {
				try {
					String body = new String(wrapper.getBody());
					q.setQuery(body);
					String args[] = body.split("\\|");
					q.setUri(q.getUri() + ": " + args[5].substring(args[5].lastIndexOf('.') + 1) + "#" + args[6]);
				} catch (Exception e) {
					sLog.warn("Error parsing GWT request body: " + e.getMessage());
				}
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
				synchronized (iQueries) { iQueries.add(q); }
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
		iActive = false;
		if (iSaver != null)
			iSaver.interrupt();
	}
	
	private class Saver extends Thread {
		public Saver() {
			super("QueryLogSaver");
			setDaemon(true);
		}
		
		public void run() {
			sLog.debug("Query Log Saver is up.");
			while (true) {
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
					Session hibSession = QueryLogDAO.getInstance().createNewSession();
					try {
						for (QueryLog q: queriesToSave)
							hibSession.save(q);
						hibSession.flush();
					} finally {
						hibSession.close();
					}
				}
				if (!iActive) break;
			}
			sLog.debug("Query Log Saver is down.");
		}
		
	}
	
	private static class HttpServletRequestWrapper implements InvocationHandler {
		private HttpServletRequest iRequest;
		private ServletInputStream iInputStream = null;
		private ByteArrayOutputStream iBody = null;
		
		public HttpServletRequestWrapper(HttpServletRequest r) {
			iRequest = r;
		}

		public HttpServletRequest createRequest() {
			return (HttpServletRequest)Proxy.newProxyInstance(QueryLogFilter.class.getClassLoader(), new Class[] {HttpServletRequest.class}, this);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("getInputStream".equals(method.getName()) && method.getParameterTypes().length == 0)
				return getInputStream();
			return iRequest.getClass().getMethod(method.getName(),method.getParameterTypes()).invoke(iRequest, args);
		}
		
		public ServletInputStream getInputStream() throws IOException {
			if (iInputStream == null) {
				iBody = new ByteArrayOutputStream();
				iInputStream = new ServletInputStreamWrapper(iRequest.getInputStream(), iBody);
			}
			return iInputStream;
		}
		
		public byte[] getBody() {
			return (iBody == null ? null : iBody.toByteArray());
		}

		
	}
	
	private static class ServletInputStreamWrapper extends ServletInputStream {
		private InputStream iInputStream;
		private OutputStream iOutputStream;
		
		public ServletInputStreamWrapper(InputStream in, OutputStream out) {
			iInputStream = in;
			iOutputStream = out;
		}

		@Override
		public int read() throws IOException {
			int out = iInputStream.read();
			iOutputStream.write(out);
			return out;
		}
		
		@Override
		public void close() throws IOException {
			iInputStream.close();
			iOutputStream.flush();
			iOutputStream.close();
		}
	}
}
