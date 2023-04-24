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


import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.model.base.BaseStudentClassEnrollment;
import org.unitime.timetable.model.dao.StudentClassEnrollmentDAO;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
@Table(name = "student_class_enrl")
public class StudentClassEnrollment extends BaseStudentClassEnrollment {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public StudentClassEnrollment () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public StudentClassEnrollment (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static enum SystemChange {
		WAITLIST("System: Wait-List"),
		SYSTEM("System: Course Change"),
		IMPORT("Data Exchange"),
		BATCH("Batch Sectioning"),
		TEST("Test");
		
		private String iName;
		SystemChange(String name) { iName = name; }
		
	@Transient
		public String getName() { return iName; }
	}

	public static List<StudentClassEnrollment> findAll(Long sessionId) {
	    return StudentClassEnrollmentDAO.getInstance().getSession().createQuery(
	            "select e from StudentClassEnrollment e where "+
	            "e.student.session.uniqueId=:sessionId", StudentClassEnrollment.class).
	            setParameter("sessionId", sessionId.longValue(), org.hibernate.type.LongType.INSTANCE).list();
	}

    public static Iterator<StudentClassEnrollment> findAllForStudent(Long studentId) {
        return StudentClassEnrollmentDAO.getInstance().getSession().createQuery(
                "select e from StudentClassEnrollment e where "+
                "e.student.uniqueId=:studentId", StudentClassEnrollment.class).
                setParameter("studentId", studentId.longValue(), org.hibernate.type.LongType.INSTANCE).setCacheable(true).list().iterator();
    }

	public static boolean sessionHasEnrollments(Long sessionId) {
		if (sessionId != null) {
		    return StudentClassEnrollmentDAO.getInstance().getSession().createQuery(
		            "select count(e) from StudentClassEnrollment e where "+
		            "e.student.session.uniqueId=:sessionId", Number.class).
		            setParameter("sessionId", sessionId.longValue(), org.hibernate.type.LongType.INSTANCE).setCacheable(true).uniqueResult().longValue() > 0;
		}
		return false;
	}
}
