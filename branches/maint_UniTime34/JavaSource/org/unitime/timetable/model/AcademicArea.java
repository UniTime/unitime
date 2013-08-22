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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.unitime.timetable.model.base.BaseAcademicArea;
import org.unitime.timetable.model.dao.AcademicAreaDAO;




public class AcademicArea extends BaseAcademicArea {
	private static final long serialVersionUID = 1L;


/*[CONSTRUCTOR MARKER BEGIN]*/
	public AcademicArea () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AcademicArea (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/** Request Attribute name for Academic Area **/
    public static final String ACAD_AREA_REQUEST_ATTR = "academicAreas";

    /*
	 * @return all Academic Areas
	 */
	public static ArrayList getAll() throws HibernateException {
		return (ArrayList) (new AcademicAreaDAO()).findAll();
	}

	/**
	 * Retrieves all academic areas in the database for the academic session
	 * ordered by academic area abbreviation
	 * @param sessionId academic session
	 * @return Vector of AcademicArea objects
	 */
	public static List getAcademicAreaList(Long sessionId) throws HibernateException {
	    
	    AcademicAreaDAO adao = new AcademicAreaDAO();
	    Session hibSession = adao.getSession();
	    List l = hibSession.createQuery(
	    		"select a from AcademicArea as a where a.session.uniqueId=:sessionId " +
	    		"order by a.academicAreaAbbreviation").
	    	setLong("sessionId",sessionId.longValue()).setCacheable(true).list();
		return l;
	}
	
    /**
     * Creates label of the format Abbr - Short Title 
     * @return
     */
    public String getLabelAbbrTitle() {
        return this.getAcademicAreaAbbreviation() + " : " + this.getTitle();
    }

    /**
     * Creates label of the format Short Title - Abbr
     * @return
     */
    public String getLabelTitleAbbr() {
        return this.getTitle() + " : " + this.getAcademicAreaAbbreviation();
    }
	
	public Long getSessionId(){
		if (getSession() != null){
			return(getSession().getUniqueId());
		} else {
			return(null);
		}
	}
    
    public static AcademicArea findByAbbv(Long sessionId, String abbv) {
    	return(findByAbbv(new AcademicAreaDAO().getSession(), sessionId, abbv));
    }
    
    public static AcademicArea findByAbbv(Session hibSession, Long sessionId, String abbv) {
        return (AcademicArea)hibSession.
            createQuery(
                    "select a from AcademicArea a where "+
                    "a.session.uniqueId=:sessionId and "+
                    "a.academicAreaAbbreviation=:abbv").
             setLong("sessionId", sessionId.longValue()).
             setString("abbv", abbv).
             setCacheable(true).
             uniqueResult(); 
    }
    
    public static AcademicArea findByExternalId(Long sessionId, String externalId) {
    	return(findByExternalId(new AcademicAreaDAO().getSession(), sessionId, externalId));
    }

    public static AcademicArea findByExternalId(Session hibSession, Long sessionId, String externalId) {
        return (AcademicArea)hibSession.
            createQuery(
                    "select a from AcademicArea a where "+
                    "a.session.uniqueId=:sessionId and "+
                    "a.externalUniqueId=:externalId").
             setLong("sessionId", sessionId.longValue()).
             setString("externalId", externalId).
             setCacheable(true).
             uniqueResult(); 
    }

    public Object clone() {
    	AcademicArea area = new AcademicArea();
    	area.setExternalUniqueId(getExternalUniqueId());
    	area.setAcademicAreaAbbreviation(getAcademicAreaAbbreviation());
    	area.setTitle(getTitle());
    	return area;
    }
        
}
