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
	NO_ROLE("No Role", NoRole.class),
	ADMIN_ROLE("Administrator", AdminRole.class),
	DEPT_SCHED_MGR_ROLE("Dept Sched Mgr", DeptSchedMgrRole.class),
	CURRICULUM_MGR_ROLE("Curriculum Mgr", CurriculumMgrRole.class),
	// VIEW_ALL_ROLE("View All", ),
	// EXAM_MGR_ROLE("Exam Mgr", ),
	// EVENT_MGR_ROLE("Event Mgr", ),
	// STUDENT_ADVISOR("Advisor", ),
	;
	
	private String iReference;
	private Class<? extends Role> iRole;
	private UniTimeRoles(String reference, Class<? extends Role> role) { iReference = reference; iRole = role; }
	
	public Class<? extends Role> toRole() { return iRole; }
	
	public String reference() { return iReference; }
	public static Class<? extends Role> fromReference(String reference) {
		for (UniTimeRoles r: values())
			if (r.reference().equals(reference)) return r.toRole();
		return null;
	}

}
