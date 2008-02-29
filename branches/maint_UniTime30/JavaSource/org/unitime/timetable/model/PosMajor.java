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

import org.hibernate.Query;
import org.hibernate.Session;
import org.unitime.timetable.model.base.BasePosMajor;
import org.unitime.timetable.model.dao.PosMajorDAO;




public class PosMajor extends BasePosMajor {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public PosMajor () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public PosMajor (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public PosMajor (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.String code,
		java.lang.String name) {

		super (
			uniqueId,
			session,
			code,
			name);
	}

/*[CONSTRUCTOR MARKER END]*/

    /** Request attribute name for available pos majors **/
    public static String POSMAJOR_ATTR_NAME = "posMajorList";  
    
	/**
	 * Retrieves all pos majors in the database for the academic session
	 * ordered by column name
	 * @param sessionId academic session
	 * @return Vector of PosMajor objects
	 */
    public static List getPosMajorList(Long sessionId) {
	    Session hibSession = new PosMajorDAO().getSession();
	    String query = "from PosMajor where academicArea.sessionId=:acadSessionId order by name";
	    Query q = hibSession.createQuery(query);
	    q.setLong("acadSessionId", sessionId.longValue());
		return q.list();
    }

    /**
     * Creates label of the format Name - Code
     * @return
     */

	public String getLabelNameCode() {
        return this.getName() + " - " + this.getCode();
    }

    /**
     * Creates label of the format Code - Name
     * @return
     */
    public String getLabelCodeName() {
        return this.getCode() + " - " + this.getName();
    }

    public static PosMajor findByCode(Long sessionId, String code) {
        return (PosMajor)new PosMajorDAO().
        getSession().
        createQuery(
                "select a from PosMajor a where "+
                "a.session.uniqueId=:sessionId and "+
                "a.code=:code").
         setLong("sessionId", sessionId.longValue()).
         setString("code", code).
         setCacheable(true).
         uniqueResult(); 
    }

    public static PosMajor findByCodeAcadAreaId(Long sessionId, String code, Long areaId) {
        if (areaId==null) return findByCode(sessionId, code);
        return (PosMajor)new PosMajorDAO().
        getSession().
        createQuery(
                "select p from PosMajor p inner join p.academicAreas a where "+
                "p.session.uniqueId=:sessionId and "+
                "a.uniqueId=:areaId and p.code=:code").
         setLong("sessionId", sessionId.longValue()).
         setLong("areaId", areaId.longValue()).
         setString("code", code).
         setCacheable(true).
         uniqueResult(); 
    }

    public static PosMajor findByCodeAcadAreaAbbv(Long sessionId, String code, String areaAbbv) {
        if (areaAbbv==null || areaAbbv.trim().length()==0) return findByCode(sessionId, code);
        return (PosMajor)new PosMajorDAO().
        getSession().
        createQuery(
                "select p from PosMajor p inner join p.academicAreas a where "+
                "p.session.uniqueId=:sessionId and "+
                "a.academicAreaAbbreviation=:areaAbbv and p.code=:code").
         setLong("sessionId", sessionId.longValue()).
         setString("areaAbbv", areaAbbv).
         setString("code", code).
         setCacheable(true).
         uniqueResult(); 
    }
}