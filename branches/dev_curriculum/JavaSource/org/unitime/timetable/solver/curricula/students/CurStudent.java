/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.curricula.students;

import java.util.HashSet;
import java.util.Set;

public class CurStudent {
	private Set<CurCourse> iCourses = new HashSet<CurCourse>();
	private Long iStudentId;
	
	public CurStudent(Long studentId) {
		iStudentId = studentId;
	}
	
	public Long getStudentId() {
		return iStudentId;
	}
	
	public Set<CurCourse> getCourses() {
		return iCourses;
	}
	
	
}
