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

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.UserContext;

public interface Permission<T> {
	public boolean check(UserContext user, T source);
	public Class<T> type();
	
	public static interface PermissionDepartment extends Permission<Department> {
		public boolean check(UserContext user, Department department, DepartmentStatusType.Status... status);
		public boolean check(UserContext user, Department controllingDepartment, DepartmentStatusType.Status ownerStatus,
											   Department managingDepartment, DepartmentStatusType.Status managerStatus);
	}
	
	public static interface PermissionSession extends Permission<Session> {
		public boolean check(UserContext user, Session session, DepartmentStatusType.Status... status);
	}
}
