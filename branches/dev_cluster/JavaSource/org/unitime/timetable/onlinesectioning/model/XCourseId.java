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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Serializable;

import net.sf.cpsolver.studentsct.model.Course;

import org.unitime.timetable.model.CourseOffering;

public class XCourseId implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long iOfferingId;
	private Long iCourseId;
	private String iCourseName;
	
	public XCourseId() {}
	
	public XCourseId(CourseOffering course) {
		iOfferingId = course.getInstructionalOffering().getUniqueId();
		iCourseId = course.getUniqueId();
		iCourseName = course.getCourseName();
	}
	
	public XCourseId(Long offeringId, Long courseId, String courseName) {
		iOfferingId = offeringId;
		iCourseId = courseId;
		iCourseName = courseName;
	}
	
	public XCourseId(XCourseId course) {
		iOfferingId = course.getOfferingId();
		iCourseId = course.getCourseId();
		iCourseName = course.getCourseName();
	}
	
	public XCourseId(Course course) {
		iOfferingId = course.getOffering().getId();
		iCourseId = course.getId();
		iCourseName = course.getName();
	}

	/** Instructional offering unique id */
	public Long getOfferingId() {
		return iOfferingId;
	}
	
    /** Course offering unique id */
	public Long getCourseId() {
		return iCourseId;
	}
	
	/** Course name */
	public String getCourseName() {
		return iCourseName;
	}
	
	@Override
	public String toString() {
		return getCourseName();
	}
}
