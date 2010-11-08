package org.unitime.timetable.filter;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
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

	public void init(FilterConfig cfg) throws ServletException {
		iActive = true;
		Saver iSaver = new Saver();
		iSaver.start();
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain ) throws ServletException, IOException {

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
			if (r.getSession() != null) {
				q.setSessionId(r.getSession().getId());
				User user = Web.getUser(r.getSession());
				if (user != null)
					q.setUid(user.getId());
			}
			String params = "";
			for (Enumeration e=r.getParameterNames(); e.hasMoreElements();) {
				String n = (String)e.nextElement();
				if ("password".equals(n)) continue;
				if (!params.isEmpty()) params += "&";
				params += n + "=" + r.getParameter(n);
			}
			if (!params.isEmpty())
				q.setQuery(params);
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
				}
				if (!ex.isEmpty())
					q.setException(ex);
			}
			synchronized (iQueries) { iQueries.add(q); }
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
				synchronized (iQueries) {
					if (!iQueries.isEmpty()) {
						sLog.debug("Persisting " + iQueries.size() + " log entries...");
						Session hibSession = QueryLogDAO.getInstance().createNewSession();
						try {
							for (QueryLog q: iQueries)
								hibSession.save(q);
							hibSession.flush();
						} finally {
							hibSession.close();
							iQueries.clear();
						}
					}
				}
				if (!iActive) break;
			}
			sLog.debug("Query Log Saver is down.");
		}
		
	}
}
