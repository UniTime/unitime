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
			
			Roles anonRole = Roles.getRole(Roles.ROLE_ANONYMOUS, hibSession);
			if (anonRole != null && anonRole.isEnabled()) {
				for (Session session: new TreeSet<Session>(SessionDAO.getInstance().findAll(hibSession))) {
					if (session.getStatusType() == null || !session.getStatusType().isAllowNoRole() || session.getStatusType().isTestSession()) continue;
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