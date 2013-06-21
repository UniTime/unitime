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
package org.unitime.timetable.events;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.client.events.AcademicSessionSelectionBox;
import org.unitime.timetable.gwt.client.events.AcademicSessionSelectionBox.AcademicSession;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(AcademicSessionSelectionBox.ListAcademicSessions.class)
public class ListAcademicSessions implements GwtRpcImplementation<AcademicSessionSelectionBox.ListAcademicSessions, GwtRpcResponseList<AcademicSession>>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public GwtRpcResponseList<AcademicSession> execute(AcademicSessionSelectionBox.ListAcademicSessions command, SessionContext context) {
		DateFormat df = new SimpleDateFormat(CONSTANTS.eventDateFormat(), Localization.getJavaLocale());
		
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		
		Session selected = null;
		if (command.hasTerm()) {
			try {
				selected = findSession(hibSession, command.getTerm());
			} catch (GwtRpcException e) {}
		} else {
			Long sessionId = (context.isAuthenticated() && context.getUser().getCurrentAuthority() != null ? context.getUser().getCurrentAcademicSessionId() : null);
			if (sessionId != null)
				selected = SessionDAO.getInstance().get(sessionId, hibSession);
		}
		if (selected == null)
			try {
				selected = findSession(hibSession, "current");
			} catch (GwtRpcException e) {}
		
		TreeSet<Session> sessions = new TreeSet<Session>();
		for (Session session: (List<Session>)hibSession.createQuery("select s from Session s").list()) {
			if (session.getStatusType() == null || session.getStatusType().isTestSession()) continue;
			if (!context.hasPermissionAnyAuthority(Right.Events, new SimpleQualifier("Session", session.getUniqueId()))) continue;
			sessions.add(session);
		}
		
		if (sessions.isEmpty())
			throw new GwtRpcException(MESSAGES.noSessionAvailable());
		
		if (selected == null || !sessions.contains(selected))
			selected = UniTimeUserContext.defaultSession(sessions, null);
		if (selected == null)
			selected = sessions.last();
		
		if (!command.hasTerm() && !context.hasPermissionAnyAuthority(selected, Right.EventAddSpecial)) {
			TreeSet<Session> preferred = new TreeSet<Session>();
			for (Session session: sessions)
				if (context.hasPermissionAnyAuthority(session, Right.EventAddSpecial, new SimpleQualifier("Session", session.getUniqueId())))
					preferred.add(session);
			if (!preferred.isEmpty()) {
				Session defaultSession = UniTimeUserContext.defaultSession(preferred, null);
				if (defaultSession != null) selected = defaultSession;
			}
		}
		
		GwtRpcResponseList<AcademicSession> ret = new GwtRpcResponseList<AcademicSession>();
		for (Session session: sessions) {
			AcademicSession acadSession = new AcademicSession(
					session.getUniqueId(),
					session.getLabel(),
					session.getAcademicTerm() + session.getAcademicYear() + session.getAcademicInitiative(),
					df.format(session.getEventBeginDate()) + " - " + df.format(session.getEventEndDate()),
					session.equals(selected));
			if (session.getStatusType().canNoRoleReportClass() && Solution.hasTimetable(session.getUniqueId()))
				acadSession.set(AcademicSession.Flag.HasClasses);
			if (session.getStatusType().canNoRoleReportExamFinal() && Exam.hasTimetable(session.getUniqueId(), ExamType.sExamTypeFinal))
				acadSession.set(AcademicSession.Flag.HasFinalExams);
			if (session.getStatusType().canNoRoleReportExamMidterm() && Exam.hasTimetable(session.getUniqueId(), ExamType.sExamTypeMidterm))
				acadSession.set(AcademicSession.Flag.HasMidtermExams);
			if (context.hasPermissionAnyAuthority(session, Right.Events, new SimpleQualifier("Session", session.getUniqueId())))
				acadSession.set(AcademicSession.Flag.HasEvents);
			if (context.hasPermissionAnyAuthority(session, Right.EventAddSpecial, new SimpleQualifier("Session", session.getUniqueId())))
				acadSession.set(AcademicSession.Flag.CanAddEvents);
			Session prev = null, next = null;
			for (Session s: sessions) {
				if (s.getUniqueId().equals(session.getUniqueId()) || !s.getAcademicInitiative().equals(session.getAcademicInitiative())) continue;
				if (s.getSessionEndDateTime().before(session.getSessionBeginDateTime())) { // before
					if (prev == null || prev.getSessionBeginDateTime().before(s.getSessionBeginDateTime())) {
						prev = s;
					}
				} else if (s.getSessionBeginDateTime().after(session.getSessionEndDateTime())) { // after
					if (next == null || next.getSessionBeginDateTime().after(s.getSessionBeginDateTime())) {
						next = s;
					}
					
				}
			}
			if (next != null) acadSession.setNextId(next.getUniqueId());
			if (prev != null) acadSession.setPreviousId(prev.getUniqueId());
			ret.add(acadSession);
		}

		return ret;
	}
	
	public static Session findSession(org.hibernate.Session hibSession, String term) {
		try {
			Session ret = SessionDAO.getInstance().get(Long.parseLong(term), hibSession);
			if (ret != null) return ret;
		} catch (NumberFormatException e) {}
		List<Session> sessions = hibSession.createQuery("select s from Session s where " +
				"s.academicTerm || s.academicYear = :term or " +
				"s.academicTerm || s.academicYear || s.academicInitiative = :term").
				setString("term", term).list();
		if (!sessions.isEmpty()) {
			for (Session session: sessions) {
				if (session.getStatusType() == null || session.getStatusType().isTestSession()) continue;
				return session;
			}
		}
		if ("current".equalsIgnoreCase(term)) {
			sessions = hibSession.createQuery("select s from Session s where " +
					"s.eventBeginDate <= :today and s.eventEndDate >= :today").
					setDate("today",new Date()).list();
			if (!sessions.isEmpty()) {
				for (Session session: sessions) {
					if (session.getStatusType() == null || session.getStatusType().isTestSession()) continue;
					return session;
				}
			}
		}
		throw new GwtRpcException("Academic session " + term + " not found.");
	}

}
