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
			return permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerLimitedEdit, DepartmentStatusType.Status.ManagerLimitedEdit);
		}

		@Override
		public Class<Department> type() { return Department.class; }
		
	}
	
	@PermissionForRight(Right.ReservationOffering)
	public static class ReservationOffering implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeededOnlyWhenWaitListing;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			return !permissionOfferingLockNeededOnlyWhenWaitListing.check(user, source) &&
					!source.isNotOffered() &&
					permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerLimitedEdit, DepartmentStatusType.Status.ManagerLimitedEdit);
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
