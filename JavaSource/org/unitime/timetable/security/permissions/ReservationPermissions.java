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

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class ReservationPermissions {

	@PermissionForRight(Right.Reservations)
	public static class Reservations implements Permission<Department> {
		
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source, DepartmentStatusType.Status.OwnerView, DepartmentStatusType.Status.ManagerView);
		}

		@Override
		public Class<Department> type() { return Department.class; }
		
	}

	@PermissionForRight(Right.ReservationAdd)
	public static class ReservationAdd implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit, DepartmentStatusType.Status.ManagerEdit);
		}

		@Override
		public Class<Department> type() { return Department.class; }
		
	}
	
	@PermissionForRight(Right.ReservationOffering)
	public static class ReservationOffering implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			return !permissionOfferingLockNeeded.check(user, source) &&
					!source.isNotOffered() &&
					permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit, DepartmentStatusType.Status.ManagerEdit);
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
	
	@PermissionForRight(Right.ReservationEdit)
	public static class ReservationEdit implements Permission<Reservation> {
		@Autowired Permission<InstructionalOffering> permissionReservationOffering;

		@Override
		public boolean check(UserContext user, Reservation source) {
			return permissionReservationOffering.check(user, source.getInstructionalOffering());
		}

		@Override
		public Class<Reservation> type() { return Reservation.class; }
		
	}
	
	@PermissionForRight(Right.ReservationDelete)
	public static class ReservationDelete extends ReservationEdit { }
}
