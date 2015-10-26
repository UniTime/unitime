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
package org.unitime.timetable.api;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.CacheMode;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public abstract class AbstractApiHelper implements ApiHelper {
	protected SessionContext iContext;
	protected HttpServletRequest iRequest;
	protected HttpServletResponse iResponse;
	protected Long iSessionId = null;
	protected CacheMode iCacheMode = null;
	protected org.hibernate.Session iHibSession = null;
	
	public AbstractApiHelper(HttpServletRequest request, HttpServletResponse response, SessionContext context, CacheMode cacheMode) {
		iRequest = request;
		iResponse = response;
		iContext = context;
		iCacheMode = cacheMode;
	}
			
	@Override
	public void sendError(int code, String message) throws IOException {
		iResponse.sendError(code, message);
	}
	
	@Override
	public void sendError(int code) throws IOException {
		iResponse.sendError(code);
	}
	
	@Override
	public void sendError(int code, Throwable error) throws IOException {
		iResponse.sendError(code, error.getMessage());
	}

	protected Long guessAcademicSessionId() {
		Long sessionId = null;
		if (getParameter("sid") != null) {
			sessionId = Long.valueOf(getParameter("sid"));
		} else {
			if (iContext.isAuthenticated())
				sessionId = (Long)(iContext.getUser().getCurrentAuthority() == null ? null : iContext.getUser().getCurrentAuthority().getAcademicSession().getQualifierId());
			else
				sessionId = (Long)iContext.getAttribute("sessionId");
		}
		if (getParameter("term") != null) {
			org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
			try {
				List<Long> sessions = hibSession.createQuery("select s.uniqueId from Session s where " +
						"s.academicTerm || s.academicYear = :term or " +
						"s.academicTerm || s.academicYear || s.academicInitiative = :term").
						setString("term", getParameter("term")).list();
				if (!sessions.isEmpty())
					sessionId = sessions.get(0);
			} finally {
				hibSession.close();
			}
		}
		return sessionId;
	}
	
	@Override
	public Long getAcademicSessionId() {
		if (iSessionId == null)
			iSessionId = guessAcademicSessionId();
		return iSessionId;
	}
	
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
	
	@Override
	public SessionContext getSessionContext() {
		return iContext;
	}
	
	@Override
	public org.hibernate.Session getHibSession() {
		if (iHibSession == null) {
			iHibSession = new _RootDAO().getSession();
			if (iCacheMode != null)
				iHibSession.setCacheMode(iCacheMode);
		}
		return iHibSession;
	}
	
	@Override
	public void close() {}
	
	@Override
	public String getOptinalParameter(String name, String defaultValue) {
		String ret = getParameter(name);
		return (ret != null ? ret : defaultValue);
	}
	
	@Override
	public String getRequiredParameter(String name) {
		String ret = getParameter(name);
		if (ret == null)
			throw new IllegalArgumentException("Parameter '" + name + "' was not provided.");
		return ret;
	}
	
	@Override
	public Integer getOptinalParameterInteger(String name, Integer defaultValue) {
		String ret = getParameter(name);
		if (ret != null) {
			try {
				return Integer.valueOf(ret);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Parameter '" + name + "' must be an integer.");
			}
		}
		return defaultValue;
	}
	
	@Override
	public Integer getRequiredParameterInteger(String name) {
		String ret = getParameter(name);
		if (ret == null)
			throw new IllegalArgumentException("Parameter '" + name + "' was not provided.");
		try {
			return Integer.valueOf(ret);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Parameter '" + name + "' must be an integer.");
		}
	}
	
	@Override
	public Long getOptinalParameterLong(String name, Long defaultValue) {
		String ret = getParameter(name);
		if (ret != null) {
			try {
				return Long.valueOf(ret);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Parameter '" + name + "' must be a long.");
			}
		}
		return defaultValue;
	}
	
	@Override
	public Long getRequiredParameterLong(String name) {
		String ret = getParameter(name);
		if (ret == null)
			throw new IllegalArgumentException("Parameter '" + name + "' was not provided.");
		try {
			return Long.valueOf(ret);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Parameter '" + name + "' must be a long.");
		}
	}
	
	@Override
	public Boolean getOptinalParameterBoolean(String name, Boolean defaultValue) {
		String ret = getParameter(name);
		if (ret != null) {
			return "true".equalsIgnoreCase(ret) || "1".equalsIgnoreCase(ret);
		}
		return defaultValue;
	}
	
	@Override
	public Boolean getRequiredParameterBoolean(String name) {
		String ret = getParameter(name);
		if (ret == null)
			throw new IllegalArgumentException("Parameter '" + name + "' was not provided.");
		return "true".equalsIgnoreCase(ret) || "1".equalsIgnoreCase(ret);
	}
}
