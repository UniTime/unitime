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

import java.util.List;

import org.unitime.timetable.model.base.BaseCurriculum;
import org.unitime.timetable.model.dao.CurriculumDAO;

public class Curriculum extends BaseCurriculum implements Comparable<Curriculum> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Curriculum () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Curriculum (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static List<Curriculum> findAll(Long sessionId) {
	    return CurriculumDAO.getInstance().getSession()
	        .createQuery("select c from Curriculum c where c.department.session.uniqueId=:sessionId")
	        .setLong("sessionId", sessionId)
	        .setCacheable(true).list();
	}

    public static List<Curriculum> findByDepartment(Long deptId) {
        return CurriculumDAO.getInstance().getSession()
            .createQuery("select c from Curriculum c where c.department.uniqueId=:deptId")
            .setLong("deptId", deptId)
            .setCacheable(true).list();
    }
    
    public int compareTo(Curriculum c) {
    	int cmp = getAbbv().compareToIgnoreCase(c.getAbbv());
    	if (cmp != 0) return cmp;
    	return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(c.getUniqueId() == null ? -1 : c.getUniqueId());
    }
}