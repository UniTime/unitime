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

import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.model.base.BaseStudentClassEnrollment;
import org.unitime.timetable.model.dao.StudentClassEnrollmentDAO;



/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
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
		
		public String getName() { return iName; }
	}

	public static List findAll(Long sessionId) {
	    return new StudentClassEnrollmentDAO().getSession().createQuery(
	            "select e from StudentClassEnrollment e where "+
	            "e.student.session.uniqueId=:sessionId").
	            setLong("sessionId", sessionId.longValue()).list();
	}

    public static Iterator findAllForStudent(Long studentId) {
        return new StudentClassEnrollmentDAO().getSession().createQuery(
                "select e from StudentClassEnrollment e where "+
                "e.student.uniqueId=:studentId").
                setLong("studentId", studentId.longValue()).setCacheable(true).list().iterator();
    }

	public static boolean sessionHasEnrollments(Long sessionId) {
		if (sessionId != null) {
		    return(((Number) new StudentClassEnrollmentDAO().getSession().createQuery(
		            "select count(e) from StudentClassEnrollment e where "+
		            "e.student.session.uniqueId=:sessionId").
		            setLong("sessionId", sessionId.longValue()).setCacheable(true).uniqueResult()).longValue() > 0);
		}
		return false;
	}
}
