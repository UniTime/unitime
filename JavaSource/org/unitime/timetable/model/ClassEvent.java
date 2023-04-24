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


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.unitime.timetable.model.base.BaseClassEvent;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.RelatedCourseInfoDAO;

/**
 * @author Tomas Muller
 */
@Entity
@DiscriminatorValue("0")
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

	@Transient
    public Set<Student> getStudents() {
        HashSet<Student> students = new HashSet();
        for (Iterator i=getClazz().getStudentEnrollments().iterator();i.hasNext();)
            students.add(((StudentClassEnrollment)i.next()).getStudent());
        return students;
    }
    
	@Transient
    public Collection<Long> getStudentIds() {
        return RelatedCourseInfoDAO.getInstance().getSession().createQuery(
                "select distinct e.student.uniqueId from StudentClassEnrollment e where e.clazz.uniqueId = :classId", Long.class)
                .setParameter("classId", getClazz().getUniqueId(), org.hibernate.type.LongType.INSTANCE)
                .setCacheable(true)
                .list();
    }

    
	@Transient
    public Set<DepartmentalInstructor> getInstructors() {
        HashSet<DepartmentalInstructor> instructors = new HashSet();
        for (Iterator i=getClazz().getClassInstructors().iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            if (ci.isLead()) instructors.add(ci.getInstructor());
        }
        return instructors;
    }
    
	@Transient
    public int getEventType() { return sEventTypeClass; }
    
	@Transient
    public Session getSession() { return getClazz().getSession(); }

	@Override
	@Transient
	public Collection<StudentClassEnrollment> getStudentClassEnrollments() {
		return
			ClassEventDAO.getInstance().getSession().createQuery(
					"select distinct e from StudentClassEnrollment e, StudentClassEnrollment f where f.clazz.uniqueId = :classId" +
        			" and e.courseOffering.instructionalOffering = f.courseOffering.instructionalOffering and e.student = f.student", StudentClassEnrollment.class)
				.setParameter("classId", getClazz().getUniqueId(), org.hibernate.type.LongType.INSTANCE)
				.list();
	}

}
