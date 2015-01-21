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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.unitime.timetable.model.base.BaseAcademicArea;
import org.unitime.timetable.model.dao.AcademicAreaDAO;




/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
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
