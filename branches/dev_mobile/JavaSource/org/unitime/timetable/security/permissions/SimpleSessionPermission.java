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
package org.unitime.timetable.security.permissions;

import org.springframework.stereotype.Service;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.permissions.Permission.PermissionSession;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("permissionSession")
public class SimpleSessionPermission implements PermissionSession {

	@Override
	public boolean check(UserContext user, Session session) {
		return check(user, session, new DepartmentStatusType.Status[] {}) && checkStatus(session.getStatusType());
	}
	
	@Override
	public boolean check(UserContext user, Session session, DepartmentStatusType.Status... status) {
		// Not authenticated or no authority -> no permission
		if (user == null || user.getCurrentAuthority() == null || session == null) return false;
		
		UserAuthority authority = user.getCurrentAuthority();
		
		// Academic session check
		if (!authority.hasRight(Right.SessionIndependent) && !authority.hasQualifier(session))
			return false;
		
		// Test session check
		if (!authority.hasRight(Right.AllowTestSessions) && (session.getStatusType() == null || session.getStatusType().isTestSession()))
			return false;
		
		// Check session status
		if (status.length > 0 && !authority.hasRight(Right.StatusIndependent)) {
			DepartmentStatusType type = session.getStatusType();
			if (type == null) return false;
			for (DepartmentStatusType.Status s: status) {
				if (type.can(s)) return true;
			}
			return false;
		}
		
		return true;
	}

	@Override
	public Class<Session> type() {
		return Session.class;
	}
	
	public boolean checkStatus(DepartmentStatusType status) { return true; }
}
