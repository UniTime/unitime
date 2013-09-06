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
import java.util.List;
import java.util.Set;

import org.unitime.timetable.model.base.BaseClassEvent;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.RelatedCourseInfoDAO;



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