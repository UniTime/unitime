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
package org.unitime.timetable.security.permissions;

import org.unitime.timetable.guice.context.UserContext;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.rights.Right;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class SimpleSessionPermission implements Permission<Session> {
	@Inject UserContext context;

	@Override
	public boolean check(Session session) {
		return check(session, right());
	}
	
	public boolean check(Session session, Right right) {
		// Not authenticated -> no editing
		if (!context.isAuthenticated()) return false;
		
		// System administrator can always edit
		if (context.getRole().hasRight(Right.IsSystemAdmin)) return true;
		
		// For all other users, session check must pass
		if (session == null || !session.getUniqueId().equals(context.getAcademicSessionId()))
			return false;
		
		// Administrator can edit if the class is of a correct academic session
		if (context.getRole().hasRight(Right.IsAdmin)) return true;
		
		// Check for the appropriate right
		if (right != null && !context.getRole().hasRight(right())) return false;
		
		// Check department status
		if (!checkStatus(session.getStatusType())) return false;
		
		return true;
	}

	@Override
	public Class<Session> type() {
		return Session.class;
	}
	
	public Right right() { return null; }
	
	public boolean checkStatus(DepartmentStatusType status) { return true; }
}
