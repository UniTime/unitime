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
package org.unitime.timetable.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.unitime.timetable.model.base.BaseCourseEvent;



/**
 * @author Tomas Muller
 */
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
	
	public Session getSession() {
		if (getRelatedCourses() == null) return null;
		for (RelatedCourseInfo rc: getRelatedCourses())
			return rc.getCourse().getInstructionalOffering().getSession();
		return null;
	}

}