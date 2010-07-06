/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.base.BaseStudentAccomodation;
import org.unitime.timetable.model.dao.StudentAccomodationDAO;



public class StudentAccomodation extends BaseStudentAccomodation {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public StudentAccomodation () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public StudentAccomodation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public static StudentAccomodation findByAbbv(Long sessionId, String abbv) {
        return (StudentAccomodation)new StudentAccomodationDAO().
            getSession().
            createQuery(
                    "select a from StudentAccomodation a where "+
                    "a.session.uniqueId=:sessionId and "+
                    "a.abbreviation=:abbv").
             setLong("sessionId", sessionId.longValue()).
             setString("abbv", abbv).
             setCacheable(true).
             uniqueResult(); 
    }

}
