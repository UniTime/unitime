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


import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;
import org.unitime.timetable.model.base.BaseAcademicClassification;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
@Table(name = "academic_classification")
public class AcademicClassification extends BaseAcademicClassification {
	private static final long serialVersionUID = 1L;

	private static HashMap academicClassifications = new HashMap(40);

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AcademicClassification () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AcademicClassification (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/** Request Attribute name for Academic Classification **/
    public static final String ACAD_CLASS_REQUEST_ATTR = "academicClassifications";

	/**
	 * Retrieves all academic classifications in the database for the academic session
	 * ordered by name
	 * @param sessionId academic session
	 * @return Vector of AcademicClassification objects
	 */
 	public static List<AcademicClassification> getAcademicClassificationList(Long sessionId) {
 		return AcademicClassificationDAO.getInstance().getSession().createQuery(
	    		"select c from AcademicClassification as c where c.session.uniqueId=:sessionId " +
	    		"order by c.name", AcademicClassification.class).
 				setParameter("sessionId", sessionId.longValue(), org.hibernate.type.LongType.INSTANCE).setCacheable(true).list();
	}

    /**
     * Creates label of the format Name - Code
     * @return
     */
	@Transient
    public String getLabelNameCode() {
        return this.getName() + " - " + this.getCode();
    }

    /**
     * Creates label of the format Code - Name
     * @return
     */
	@Transient
    public String getLabelCodeName() {
        return this.getCode() + " - " + this.getName();
    }

	/**
	 * Load Academic Classifications
	 */
	public static void loadAcademicClassifications(Long sessionId) {
		
		List acadClasses = getAcademicClassificationList(sessionId);
		
		for(int i = 0; i < acadClasses.size(); i++) {
			AcademicClassification acadClass = 
				(AcademicClassification)acadClasses.get(i);
			String code = acadClass.getCode();
			academicClassifications.put(code, acadClass);
		}
	}

	/**
	 * Get the Academic Classification
	 * @param academicClass
	 */
	public static AcademicClassification getAcademicClassification(
			String academicClass) {
		
		return (AcademicClassification)academicClassifications
					.get(academicClass);
	}
	
	@Transient
	public Long getSessionId(){
		if (getSession() != null){
			return(getSession().getUniqueId());
		} else {
			return(null);
		}
	}
    
    public static AcademicClassification findByCode(Long sessionId, String code) {
    	return (findByCode(AcademicClassificationDAO.getInstance().getSession(), sessionId, code));
    }
    
    public static AcademicClassification findByCode(Session hibSession, Long sessionId, String code) {
        return hibSession.
        createQuery(
                "select a from AcademicClassification a where "+
                "a.session.uniqueId=:sessionId and "+
                "a.code=:code", AcademicClassification.class).
         setParameter("sessionId", sessionId.longValue(), org.hibernate.type.LongType.INSTANCE).
         setParameter("code", code, org.hibernate.type.StringType.INSTANCE).
         setCacheable(true).
         uniqueResult(); 
    }

    public static AcademicClassification findByExternalId(Long sessionId, String externalId) {
    	return(findByExternalId(AcademicClassificationDAO.getInstance().getSession(), sessionId, externalId));
    }

    public static AcademicClassification findByExternalId(Session hibSession, Long sessionId, String externalId) {
        return hibSession.
        createQuery(
                "select a from AcademicClassification a where "+
                "a.session.uniqueId=:sessionId and "+
                "a.externalUniqueId=:externalUniqueId", AcademicClassification.class).
         setParameter("sessionId", sessionId.longValue(), org.hibernate.type.LongType.INSTANCE).
         setParameter("externalUniqueId", externalId, org.hibernate.type.StringType.INSTANCE).
         setCacheable(true).
         uniqueResult(); 
    }

    public Object clone() {
    	AcademicClassification c = new AcademicClassification();
    	c.setExternalUniqueId(getExternalUniqueId());
    	c.setCode(getCode());
    	c.setName(getName());
    	return c;
    }
    
    public boolean isUsed(org.hibernate.Session hibSession) {
    	return ((hibSession == null ? AcademicClassificationDAO.getInstance().getSession() : hibSession).createQuery(
    			"select count(c) from CurriculumClassification c inner join c.academicClassification f where f.uniqueId = :clasfId", Number.class)
    			.setParameter("clasfId", getUniqueId(), org.hibernate.type.LongType.INSTANCE).setCacheable(true).uniqueResult()).intValue() > 0;
    }
    
}
