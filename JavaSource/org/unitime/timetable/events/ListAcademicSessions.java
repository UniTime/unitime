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
package org.unitime.timetable.events;

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
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(AcademicSessionSelectionBox.ListAcademicSessions.class)
public class ListAcademicSessions implements GwtRpcImplementation<AcademicSessionSelectionBox.ListAcademicSessions, GwtRpcResponseList<AcademicSession>>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public GwtRpcResponseList<AcademicSession> execute(AcademicSessionSelectionBox.ListAcademicSessions command, SessionContext context) {
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
		
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
		
		Right permission = Right.Events;
		if (command.hasSource())
			permission = Right.valueOf(command.getSource());
		
		TreeSet<Session> sessions = new TreeSet<Session>();
		for (Session session: (List<Session>)hibSession.createQuery("select s from Session s").list()) {
			if (session.getStatusType() == null || session.getStatusType().isTestSession()) continue;
			if (!context.hasPermissionAnyAuthority(permission, new SimpleQualifier("Session", session.getUniqueId()))) continue;
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
				else if (context.hasPermissionAnyAuthority(session, Right.EventAddCourseRelated, new SimpleQualifier("Session", session.getUniqueId())))
					preferred.add(session);
				else if (context.hasPermissionAnyAuthority(session, Right.EventAddUnavailable, new SimpleQualifier("Session", session.getUniqueId())))
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
			if (session.canNoRoleReportClass())
				acadSession.set(AcademicSession.Flag.HasClasses);
			if (session.canNoRoleReportExamFinal())
				acadSession.set(AcademicSession.Flag.HasFinalExams);
			if (session.canNoRoleReportExamMidterm())
				acadSession.set(AcademicSession.Flag.HasMidtermExams);
			if (context.hasPermissionAnyAuthority(session, Right.Events, new SimpleQualifier("Session", session.getUniqueId())))
				acadSession.set(AcademicSession.Flag.HasEvents);
			if (context.hasPermissionAnyAuthority(session, Right.EventAddSpecial, new SimpleQualifier("Session", session.getUniqueId())))
				acadSession.set(AcademicSession.Flag.CanAddEvents);
			else if (context.hasPermissionAnyAuthority(session, Right.EventAddCourseRelated, new SimpleQualifier("Session", session.getUniqueId())))
				acadSession.set(AcademicSession.Flag.CanAddEvents);
			else if (context.hasPermissionAnyAuthority(session, Right.EventAddUnavailable, new SimpleQualifier("Session", session.getUniqueId())))
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
