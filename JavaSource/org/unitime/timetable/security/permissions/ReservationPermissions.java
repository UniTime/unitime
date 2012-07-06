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

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

public class ReservationPermissions {

	@PermissionForRight(Right.AddReservation)
	public static class AddReservation implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (permissionOfferingLockNeeded.check(user, source)) return false;
			
			if (source.isNotOffered()) return false;
			
			if (permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit, DepartmentStatusType.Status.ManagerEdit)) return true;
			
			return false;
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
}
