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

import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;

public class LocationPermissions {
	
	@PermissionForRight(Right.AddSpecialUseRoom)
	public static class AddSpecialUseRoom implements Permission<ExternalRoom> {

		@Override
		public boolean check(UserContext user, ExternalRoom source) {
			if (user == null || user.getCurrentAuthority() == null || source == null) return false;
			
			UserAuthority authority = user.getCurrentAuthority();
			
			if (!authority.hasRight(Right.AddSpecialUseRoom)) return false;
			
			if (authority.hasRight(Right.DepartmentIndependent)) {
				return true;
			} else {
				for (ExternalRoomDepartment dept: source.getRoomDepartments()) {
					if (authority.hasQualifier(new SimpleQualifier("Department", dept.getDepartmentCode())))
						return true;
				}
				
				return false;
			}
		}

		@Override
		public Class<ExternalRoom> type() { return ExternalRoom.class; }
	}

}
