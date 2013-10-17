/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
