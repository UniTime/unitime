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
package org.unitime.timetable.security.roles;

// FIXME: This should be a database table
public enum UniTimeRoles {
	ROLE_NO_ROLE(NoRole.class),
	ROLE_ADMIN(SessionAdminRole.class),
	ROLE_SYSTEM_ADMIN(SystemAdminRole.class),
	ROLE_DEPT_MGR(DeptSchedMgrRole.class),
	ROLE_CURRICULUM_MGR(CurriculumMgrRole.class),
	ROLE_VIEW_ALL(ViewAllRole.class),
	ROLE_EXAM_MGR(ExamMgrRole.class),
	ROLE_EVENT_MGR(EventMgrRole.class),
	ROLE_ADVISOR(StudentAdvisorRole.class),
	ROLE_STUDENT(StudentRole.class),
	ROLE_INSTRUCTOR(InstructorRole.class)
	;
	
	private String iReference = null;
	private Class<? extends Role> iRole;
	
	private UniTimeRoles(Class<? extends Role> role) {
		LegacyRole reference = role.getAnnotation(LegacyRole.class);
		if (reference != null) iReference = reference.value();
		iRole = role;
	}
	
	public Class<? extends Role> toRole() { return iRole; }
	
	public String reference() { return iReference; }
	public static Class<? extends Role> fromReference(String reference) {
		for (UniTimeRoles r: values())
			if (reference.equals(r.reference())) return r.toRole();
		return null;
	}

}
