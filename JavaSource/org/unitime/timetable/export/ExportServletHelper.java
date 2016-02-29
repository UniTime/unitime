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
package org.unitime.timetable.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.unitime.timetable.api.ApiToken;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.events.QueryEncoderBackend;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.AnonymousUserContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

/**
 * @author Tomas Muller
 */
public class ExportServletHelper implements ExportHelper {
	private SessionContext iContext;
	private Exporter.Params iParams;
	private HttpServletResponse iResponse;
	private PrintWriter iWriter = null;
	private OutputStream iOutputStream = null;
	
	public ExportServletHelper(HttpServletRequest request, HttpServletResponse response, SessionContext context) throws UnsupportedEncodingException {
		iResponse = response;
		iContext = context;
		String q = request.getParameter("q");
		String x = request.getParameter("x");
		if (q != null) {
			iParams = new QParams(q, false);
		} else if (x != null) {
			iParams = new QParams(x, true);
		} else {
			iParams = new HttpParams(request);
		}
		if ((!context.isAuthenticated() || context.getUser() instanceof AnonymousUserContext)) {
			UserContext uc = null;
			String token = getParameter("token");
			if (token != null && ApplicationProperty.ApiCanUseAPIToken.isTrue()) {
				uc = ((ApiToken)SpringApplicationContextHolder.getBean("apiToken")).getContext(getParameter("token"));
				if (uc != null) iContext = new CustomExportContext(request.getSession(), uc);
			} else if (isRequestEncoded()) {
				String user = getParameter("user");
				if (user != null)
					uc = new UniTimeUserContext(user, null, null, null);
			}
			if (uc != null) {
	    		String role = getParameter("role");
				Long sessionId = getAcademicSessionId();
	    		if (role != null && sessionId != null) {
	    			for (UserAuthority a: uc.getAuthorities()) {
	    				if (a.getAcademicSession() != null && a.getAcademicSession().getQualifierId().equals(sessionId) && role.equals(a.getRole())) {
	    					uc.setCurrentAuthority(a); break;
	    				}
	    			}
	    		}
	    		iContext = new CustomExportContext(request.getSession(), uc);
			}
		}
	}
		
	@Override
	public void setup(String content, String fileName, boolean binary) {
		iResponse.setContentType("text/calendar".equalsIgnoreCase(content) ? content : content + "; charset=UTF-8");
		iResponse.setCharacterEncoding("UTF-8");
		
		iResponse.setHeader("Pragma", "no-cache" );
		iResponse.addHeader("Cache-Control", "must-revalidate" );
		iResponse.addHeader("Cache-Control", "no-cache" );
		iResponse.addHeader("Cache-Control", "no-store" );
		iResponse.setDateHeader("Date", new Date().getTime());
		iResponse.setDateHeader("Expires", 0);
		
		iResponse.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"" );
	}
	
	@Override
	public String getParameter(String name) {
		return iParams.getParameter(name);
	}

	@Override
	public String[] getParameterValues(String name) {
		return iParams.getParameterValues(name);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return iParams.getParameterNames();
	}
	
	public PrintWriter getWriter() throws IOException {
		if (iWriter == null)
			iWriter = iResponse.getWriter();
		return iWriter;
	}
	
	public boolean hasWriter() {
		return iWriter != null;
	}
	
	public OutputStream getOutputStream() throws IOException {
		if (iOutputStream == null)
			iOutputStream = iResponse.getOutputStream();
		return iOutputStream;
	}
	
	public boolean hasOutputStream() {
		return iOutputStream != null;
	}
	
	public Long getAcademicSessionId() {
		Long sessionId = null;
		if (iParams.getParameter("sid") != null) {
			sessionId = Long.valueOf(iParams.getParameter("sid"));
		} else {
			if (iContext.isAuthenticated())
				sessionId = (Long)(iContext.getUser().getCurrentAuthority() == null ? null : iContext.getUser().getCurrentAuthority().getAcademicSession().getQualifierId());
			else
				sessionId = (Long)iContext.getAttribute("sessionId");
		}
		if (iParams.getParameter("term") != null) {
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				List<Long> sessions = hibSession.createQuery("select s.uniqueId from Session s where " +
						"s.academicTerm || s.academicYear = :term or " +
						"s.academicTerm || s.academicYear || s.academicInitiative = :term").
						setString("term", iParams.getParameter("term")).list();
				if (!sessions.isEmpty())
					sessionId = sessions.get(0);
			} finally {
				hibSession.close();
			}
		}
		return sessionId;
	}
	
	public static class HttpParams implements Exporter.Params {
		HttpServletRequest iRequest;
		
		HttpParams(HttpServletRequest request) { iRequest = request; }

		@Override
		public String getParameter(String name) {
			return iRequest.getParameter(name);
		}

		@Override
		public String[] getParameterValues(String name) {
			return iRequest.getParameterValues(name);
		}
		
		@Override
		public Enumeration<String> getParameterNames() {
			return iRequest.getParameterNames();
		}
	}
	
	public static class QParams implements Exporter.Params {
		private Map<String, List<String>> iParams = new HashMap<String, List<String>>();
		
		QParams(String q, boolean hash) throws UnsupportedEncodingException {
			for (String p: QueryEncoderBackend.decode(q, hash).split("&")) {
				String name = p.substring(0, p.indexOf('='));
				String value = URLDecoder.decode(p.substring(p.indexOf('=') + 1), "UTF-8");
				List<String> values = iParams.get(name);
				if (values == null) {
					values = new ArrayList<String>();
					iParams.put(name, values);
				}
				values.add(value);
			}
		}
		@Override
		public String getParameter(String name) {
			List<String> values = iParams.get(name);
			return (values == null || values.isEmpty() ? null : values.get(0));
		}
		@Override
		public String[] getParameterValues(String name) {
			List<String> values = iParams.get(name);
			if (values == null) return null;
			String[] ret = new String[values.size()];
			values.toArray(ret);
			return ret;
		}
		@Override
		public Enumeration<String> getParameterNames() {
			final Iterator<String> iterator = iParams.keySet().iterator();
			return new Enumeration<String>() {
				@Override
				public boolean hasMoreElements() {
					return iterator.hasNext();
				}
				@Override
				public String nextElement() {
					return iterator.next();
				}
			};
		}
		
	}

	@Override
	public SessionContext getSessionContext() {
		return iContext;
	}
	
	@Override
	public boolean isRequestEncoded() {
		return iParams instanceof QParams;
	}
	
	public class CustomExportContext extends HttpSessionContext {
		private UserContext iUser;
		
		public CustomExportContext(HttpSession session, UserContext user) {
			super(session);
			iUser = user;
		}
		
		@Override
		public boolean isAuthenticated() { return iUser != null; }

		@Override
		public UserContext getUser() { return iUser; }
	}
}
