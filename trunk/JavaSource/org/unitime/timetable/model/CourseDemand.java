/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model;

import java.util.List;

import org.unitime.timetable.model.base.BaseCourseDemand;
import org.unitime.timetable.model.dao.CourseDemandDAO;

public class CourseDemand extends BaseCourseDemand implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseDemand () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseDemand (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public CourseDemand (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Student student,
		java.lang.Integer priority,
		java.lang.Boolean waitlist,
		java.lang.Boolean alternative,
		java.util.Date timestamp) {

		super (
			uniqueId,
			student,
			priority,
			waitlist,
			alternative,
			timestamp);
	}

/*[CONSTRUCTOR MARKER END]*/

    public int compareTo(Object o) {
        if (o==null || !(o instanceof CourseDemand)) return -1;
        CourseDemand cd = (CourseDemand)o;
        int cmp = (isAlternative().booleanValue() == cd.isAlternative().booleanValue() ? 0 : isAlternative().booleanValue() ? 1 : -1);
        if (cmp!=0) return cmp;
        cmp = getPriority().compareTo(cd.getPriority());
        if (cmp!=0) return cmp;
        return getUniqueId().compareTo(cd.getUniqueId());
    }
    
    public static List findAll(Long sessionId) {
        return new CourseDemandDAO().
            getSession().
            createQuery("select c from CourseDemand c where c.student.session.uniqueId=:sessionId").
            setLong("sessionId", sessionId.longValue()).
            list(); 
    }
}