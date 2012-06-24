/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.unitime.timetable.model.base.BaseCourseEvent;



public class CourseEvent extends BaseCourseEvent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseEvent () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public Set<Student> getStudents() {
        HashSet<Student> students = new HashSet();
        if (isReqAttendance()==null || !isReqAttendance().booleanValue()) return students;
        for (Iterator i=getRelatedCourses().iterator();i.hasNext();)
            students.addAll(((RelatedCourseInfo)i.next()).getStudents());
        return students;
  
    }
    
    public Set<DepartmentalInstructor> getInstructors() {
        HashSet<DepartmentalInstructor> instructors = new HashSet();
        if (isReqAttendance()==null || !isReqAttendance().booleanValue()) return instructors;
        for (Iterator i=getRelatedCourses().iterator();i.hasNext();)
            instructors.addAll(((RelatedCourseInfo)i.next()).getInstructors());
        return instructors;
    }

    public int getEventType() { return sEventTypeCourse; }

    public Collection<Long> getStudentIds() {
        HashSet<Long> studentIds = new HashSet();
        for (Iterator i=getRelatedCourses().iterator();i.hasNext();)
            studentIds.addAll(((RelatedCourseInfo)i.next()).getStudentIds());
        return studentIds;
    }

	@Override
	public Collection<StudentClassEnrollment> getStudentClassEnrollments() {
        HashSet<StudentClassEnrollment> enrollments = new HashSet();
        for (RelatedCourseInfo owner: getRelatedCourses())
            enrollments.addAll(owner.getStudentClassEnrollments());
        return enrollments;
	}

}