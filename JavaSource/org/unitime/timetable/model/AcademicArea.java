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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.unitime.timetable.model.base.BaseAcademicArea;
import org.unitime.timetable.model.dao.AcademicAreaDAO;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
@Table(name = "academic_area")
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
	@Transient
	public static ArrayList getAll() throws HibernateException {
		return (ArrayList) (AcademicAreaDAO.getInstance()).findAll();
	}

	/**
	 * Retrieves all academic areas in the database for the academic session
	 * ordered by academic area abbreviation
	 * @param sessionId academic session
	 * @return Vector of AcademicArea objects
	 */
	public static List<AcademicArea> getAcademicAreaList(Long sessionId) throws HibernateException {
		return AcademicAreaDAO.getInstance().getSession().createQuery(
				"select a from AcademicArea as a where a.session.uniqueId=:sessionId " +
	    		"order by a.academicAreaAbbreviation", AcademicArea.class).
				setParameter("sessionId", sessionId.longValue(), Long.class).setCacheable(true).list();
	}
	
    /**
     * Creates label of the format Abbr - Short Title 
     * @return
     */
	@Transient
    public String getLabelAbbrTitle() {
        return this.getAcademicAreaAbbreviation() + " : " + this.getTitle();
    }

    /**
     * Creates label of the format Short Title - Abbr
     * @return
     */
	@Transient
    public String getLabelTitleAbbr() {
        return this.getTitle() + " : " + this.getAcademicAreaAbbreviation();
    }
	
	@Transient
	public Long getSessionId(){
		if (getSession() != null){
			return(getSession().getUniqueId());
		} else {
			return(null);
		}
	}
    
    public static AcademicArea findByAbbv(Long sessionId, String abbv) {
    	return(findByAbbv(AcademicAreaDAO.getInstance().getSession(), sessionId, abbv));
    }
    
    public static AcademicArea findByAbbv(Session hibSession, Long sessionId, String abbv) {
        return hibSession.
            createQuery(
                    "select a from AcademicArea a where "+
                    "a.session.uniqueId=:sessionId and "+
                    "a.academicAreaAbbreviation=:abbv", AcademicArea.class).
             setParameter("sessionId", sessionId.longValue(), Long.class).
             setParameter("abbv", abbv, String.class).
             setCacheable(true).
             uniqueResult(); 
    }
    
    public static AcademicArea findByExternalId(Long sessionId, String externalId) {
    	return(findByExternalId(AcademicAreaDAO.getInstance().getSession(), sessionId, externalId));
    }

    public static AcademicArea findByExternalId(Session hibSession, Long sessionId, String externalId) {
        return hibSession.
            createQuery(
                    "select a from AcademicArea a where "+
                    "a.session.uniqueId=:sessionId and "+
                    "a.externalUniqueId=:externalId", AcademicArea.class).
             setParameter("sessionId", sessionId.longValue(), Long.class).
             setParameter("externalId", externalId, String.class).
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
    
    public boolean isUsed(org.hibernate.Session hibSession) {
    	return ((hibSession == null ? AcademicAreaDAO.getInstance().getSession() : hibSession).createQuery(
    			"select count(c) from Curriculum c inner join c.academicArea a where a.uniqueId = :areaId", Number.class)
    			.setParameter("areaId", getUniqueId(), Long.class).setCacheable(true).uniqueResult()).intValue() > 0;
    }
}
