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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.security.roles.AdminRole;
import org.unitime.timetable.security.roles.DepartmentDependentRole;
import org.unitime.timetable.security.roles.SystemAdminRole;
import org.unitime.timetable.security.spring.UniTimeUser;

@Service("permissionClassEdit")
public class ClassEditPermission implements Permission<Class_> {
	@Autowired
	SimpleSessionPermission permissionSession;

	@Override
	public boolean check(UniTimeUser user, Class_ clazz) {
		// Not authenticated -> no editing
		if (user == null || !user.hasRole()) return false;
		
		// System administrator can always edit
		if (user.getRole().hasRight(Right.IsSystemAdmin)) return true;
		// or
		if (user.getRole() instanceof SystemAdminRole) return true;
		
		// For all other users, class must be of the correct academic session
		Session session = clazz.getControllingDept().getSession();
		if (session == null || !session.getUniqueId().equals(user.getSessionId()))
			return false;
		
		// Administrator can edit if the class is of a correct academic session
		if (user.getRole().hasRight(Right.IsAdmin)) return true;
		// or
		if (user.getRole() instanceof AdminRole) return true;
		
		// Change the class edit right
		if (!user.getRole().hasRight(Right.ClassEdit)) return false;
		
		// or we can skip all the above by injecting (and depending on) SimpleSessionPermission
		if (!permissionSession.check(user, clazz.getControllingDept().getSession(), Right.ClassEdit)) return false;
		
		if (user.getRole() instanceof DepartmentDependentRole) {
			List<Long> departmentIds = ((DepartmentDependentRole)user.getRole()).getDepartmentIds();
			
			if (clazz.getManagingDept() != null && departmentIds.contains(clazz.getManagingDept().getUniqueId())) {
				// Check department / session status -- for the manager
				if (clazz.getManagingDept().effectiveStatusType().canManagerEdit()) return true;
			}
			
			if (clazz.getControllingDept() != null && departmentIds.contains(clazz.getControllingDept().getUniqueId())) {
				// Check department / session status -- for the owner
				if (clazz.getControllingDept().effectiveStatusType().canOwnerEdit()) return true;
			}			
		}
		
		return false;
	}

	@Override
	public Class<Class_> type() {
		return Class_.class;
	}
}
