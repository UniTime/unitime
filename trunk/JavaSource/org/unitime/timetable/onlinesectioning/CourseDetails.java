/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning;

import java.io.Serializable;

import net.sf.cpsolver.studentsct.model.Course;

public class CourseDetails implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int iEnrollment = 0, iLimit = 0;
	
	public CourseDetails(Course course) {
		iEnrollment = course.getEnrollments().size();
		iLimit = course.getLimit();
	}
	
	public int getEnrollment() { return iEnrollment; }
	public void setEnrollment(int enrollment) { iEnrollment = enrollment; }
	
	public int getLimit() { return iLimit; }
	public void setLimit(int limit) { iLimit = limit; }
}
