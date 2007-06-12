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

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseStudentGroup;
import org.unitime.timetable.model.dao.StudentGroupDAO;




public class StudentGroup extends BaseStudentGroup {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public StudentGroup () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public StudentGroup (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public StudentGroup (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.Long sessionId,
		java.lang.String groupAbbreviation,
		java.lang.String groupName) {

		super (
			uniqueId,
			session,
			sessionId,
			groupAbbreviation,
			groupName);
	}

/*[CONSTRUCTOR MARKER END]*/

    /** Request attribute name for available student groups**/
    public static String STUGRP_ATTR_NAME = "studentGroupList";  
    
	/**
	 * Retrieves all student groups in the database for the academic session
	 * ordered by column group name
	 * @param sessionId academic session
	 * @return Vector of StudentGroup objects
	 */
    public static List getStudentGroupList(Long sessionId) {
        StudentGroupDAO sdao = new StudentGroupDAO();
	    Session hibSession = sdao.getSession();
	    List l = hibSession.createCriteria(StudentGroup.class)
				    .add(Restrictions.eq("sessionId", sessionId))
				    .addOrder(Order.asc("groupName"))
				    .list();
		return l;
    }

    public static StudentGroup findByAbbv(Long sessionId, String abbv) {
        return (StudentGroup)new StudentGroupDAO().
            getSession().
            createQuery(
                    "select a from StudentGroup a where "+
                    "a.session.uniqueId=:sessionId and "+
                    "a.groupAbbreviation=:abbv").
             setLong("sessionId", sessionId.longValue()).
             setString("abbv", abbv).
             setCacheable(true).
             uniqueResult(); 
    }

}