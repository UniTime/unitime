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
import java.util.List;
import java.util.Set;

import org.unitime.timetable.model.base.BaseClassEvent;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.RelatedCourseInfoDAO;



/**
 * @author Tomas Muller
 */
public class ClassEvent extends BaseClassEvent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ClassEvent () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ClassEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public Set<Student> getStudents() {
        HashSet<Student> students = new HashSet();
        for (Iterator i=getClazz().getStudentEnrollments().iterator();i.hasNext();)
            students.add(((StudentClassEnrollment)i.next()).getStudent());
        return students;
    }
    
    public Collection<Long> getStudentIds() {
        return new RelatedCourseInfoDAO().getSession().createQuery(
                "select distinct e.student.uniqueId from StudentClassEnrollment e where e.clazz.uniqueId = :classId")
                .setLong("classId", getClazz().getUniqueId())
                .setCacheable(true)
                .list();
    }

    
    public Set<DepartmentalInstructor> getInstructors() {
        HashSet<DepartmentalInstructor> instructors = new HashSet();
        for (Iterator i=getClazz().getClassInstructors().iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            if (ci.isLead()) instructors.add(ci.getInstructor());
        }
        return instructors;
    }
    
    public int getEventType() { return sEventTypeClass; }
    
    public Session getSession() { return getClazz().getSession(); }

	@Override
	public Collection<StudentClassEnrollment> getStudentClassEnrollments() {
		return (List<StudentClassEnrollment>)
			ClassEventDAO.getInstance().getSession().createQuery(
					"select distinct e from StudentClassEnrollment e, StudentClassEnrollment f where f.clazz.uniqueId = :classId" +
        			" and e.courseOffering.instructionalOffering = f.courseOffering.instructionalOffering and e.student = f.student")
				.setLong("classId", getClazz().getUniqueId())
				.list();
	}

}