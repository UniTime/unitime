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
import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentStatusType.Status;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

public class ClassPermissions {
	
	public static abstract class AbstractClassPermission implements Permission<Class_> {
		@Autowired
		SimpleDepartmentPermission simpleDepartmentPermission;
		
		@Override
		public boolean check(UserContext user, Class_ clazz) {
			return	clazz != null && (
					simpleDepartmentPermission.check(user, clazz.getControllingDept(), right(), ownerStatus()) ||
					simpleDepartmentPermission.check(user, clazz.getManagingDept(), right(), managerStatus()));
		}

		@Override
		public Class<Class_> type() { return Class_.class; }
		
		public abstract Right right();
		
		public abstract Status ownerStatus();
		
		public abstract Status managerStatus();		
	}
	
	@Service("permissionClassEdit")
	public static class ClassEdit extends AbstractClassPermission {

		@Override
		public Right right() { return Right.ClassEdit; }

		@Override
		public Status ownerStatus() { return Status.OwnerEdit; }

		@Override
		public Status managerStatus() { return Status.ManagerEdit; }

	}
	
	@Service("permissionClassDetail")
	public static class ClassDetail extends AbstractClassPermission {

		@Override
		public Right right() { return Right.ClassDetail; }

		@Override
		public Status ownerStatus() { return Status.OwnerView; }

		@Override
		public Status managerStatus() { return Status.ManagerView; }

	}
	
}
