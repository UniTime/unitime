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

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.UserContext;

/**
 * @author Tomas Muller
 */
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
