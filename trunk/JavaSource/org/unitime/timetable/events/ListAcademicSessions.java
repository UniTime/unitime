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

import java.util.Set;

import org.unitime.timetable.gwt.client.events.AcademicSessionSelectionBox;
import org.unitime.timetable.gwt.client.events.AcademicSessionSelectionBox.AcademicSession;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.model.Session;

public class ListAcademicSessions implements GwtRpcImplementation<AcademicSessionSelectionBox.ListAcademicSessions, GwtRpcResponseList<AcademicSession>>{

	@Override
	public GwtRpcResponseList<AcademicSession> execute(AcademicSessionSelectionBox.ListAcademicSessions command, GwtRpcHelper helper) {
		GwtRpcResponseList<AcademicSession> ret = new GwtRpcResponseList<AcademicSession>();
		Session selected = (helper.getUser() == null ? null : Session.getCurrentAcadSession(helper.getUser()));
		if (selected == null) selected = Session.defaultSession();
		Set<Session> sessions = Session.getAllSessions();
		for (Session session: sessions) {
			AcademicSession acadSession = new AcademicSession(session.getUniqueId(), session.getLabel(), session.equals(selected));
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

}
