/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
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
package org.unitime.timetable.api;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public abstract class AbstractApiHelper implements ApiHelper {
	protected SessionContext iContext;
	protected HttpServletRequest iRequest;
	protected HttpServletResponse iResponse;
	protected Long iSessionId = null;
	
	public AbstractApiHelper(HttpServletRequest request, HttpServletResponse response, SessionContext context) {
		iRequest = request;
		iResponse = response;
		iContext = context;
	}
			
	@Override
	public void sendError(int code, String message) throws IOException {
		iResponse.sendError(code, message);
	}
	
	@Override
	public void sendError(int code) throws IOException {
		iResponse.sendError(code);
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
}
