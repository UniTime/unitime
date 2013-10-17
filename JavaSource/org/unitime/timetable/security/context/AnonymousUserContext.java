/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.security.context;

import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.authority.RoleAuthority;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;

/**
 * @author Tomas Muller
 */
public class AnonymousUserContext extends AbstractUserContext {
	private static final long serialVersionUID = 1L;
	
	public AnonymousUserContext() {
		org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().createNewSession();
		try {
			TreeSet<Session> sessions = new TreeSet<Session>();
			
			Roles anonRole = Roles.getRole(Roles.ROLE_ANONYMOUS);
			if (anonRole != null && anonRole.isEnabled()) {
				for (Session session: new TreeSet<Session>(SessionDAO.getInstance().findAll())) {
					if (session.getStatusType() == null || !session.getStatusType().isActive() || session.getStatusType().isTestSession()) continue;
					List<? extends UserAuthority> authorities = getAuthorities(null, new SimpleQualifier("Session", session.getUniqueId()));
					if (authorities.isEmpty()) {
						UserAuthority authority = new RoleAuthority(-1l, anonRole);
						authority.addQualifier(session);
						addAuthority(authority);
						sessions.add(session);
					}
				}
			}
			
			if (getCurrentAuthority() == null) {
				Session session = UniTimeUserContext.defaultSession(sessions, null);
				if (session != null) {
					List<? extends UserAuthority> authorities = getAuthorities(null, new SimpleQualifier("Session", session.getUniqueId()));
					if (!authorities.isEmpty())
						setCurrentAuthority(authorities.get(0));
				}
			}
		} finally {
			hibSession.close();
		}
	}

	@Override
	public String getExternalUserId() {
		return "";
	}

	@Override
	public String getName() {
		return "Guest";
	}

	@Override
	public String getEmail() {
		return null;
	}

	@Override
	public String getUsername() {
		return "";
	}
}