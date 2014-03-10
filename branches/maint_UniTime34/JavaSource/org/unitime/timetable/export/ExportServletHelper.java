/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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

import org.unitime.timetable.events.QueryEncoderBackend;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;

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
		if (q != null) {
			iParams = new QParams(q);
		} else {
			iParams = new HttpParams(request);
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
		
		QParams(String q) throws UnsupportedEncodingException {
			for (String p: QueryEncoderBackend.decode(q).split("&")) {
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

}
