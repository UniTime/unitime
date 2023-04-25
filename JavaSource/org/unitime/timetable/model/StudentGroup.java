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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;

import org.hibernate.Session;
import org.unitime.timetable.model.base.BaseStudentGroup;
import org.unitime.timetable.model.dao.StudentGroupDAO;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
@Table(name = "student_group")
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

/*[CONSTRUCTOR MARKER END]*/

    /** Request attribute name for available student groups**/
    public static String STUGRP_ATTR_NAME = "studentGroupList";  
    
	/**
	 * Retrieves all student groups in the database for the academic session
	 * ordered by column group name
	 * @param sessionId academic session
	 * @return Vector of StudentGroup objects
	 */
    public static List<StudentGroup> getStudentGroupList(Long sessionId) {
    	return StudentGroupDAO.getInstance().getSession()
				.createQuery("from StudentGroup where sessionId = :sessionId order by groupName", StudentGroup.class)
				.setParameter("sessionId", sessionId)
				.list();
    }

    public static StudentGroup findByAbbv(Long sessionId, String abbv) {
        return StudentGroupDAO.getInstance().
            getSession().
            createQuery(
                    "select a from StudentGroup a where "+
                    "a.session.uniqueId=:sessionId and "+
                    "a.groupAbbreviation=:abbv", StudentGroup.class).
             setParameter("sessionId", sessionId.longValue()).
             setParameter("abbv", abbv).
             setCacheable(true).
             uniqueResult(); 
    }
    
    public static StudentGroup findByExternalId(Session hibSession, String externalId, Long acadSessionId) {
    	 
        return hibSession.
            createQuery(
                    "select a from StudentGroup a where "+
                    "a.session.uniqueId = :acadSessionId and "+
                    "externalUniqueId=:eId ", StudentGroup.class).
             setParameter("acadSessionId", acadSessionId.longValue()).
             setParameter("eId", externalId).
             setCacheable(true).
             uniqueResult(); 
    }
    
    public static List<StudentGroup> findByType(org.hibernate.Session hibSession, Long sessionId, Long typeId) {
		if (typeId == null)
			return hibSession.createQuery("from StudentGroup x where x.session.uniqueId = :sessionId and x.type is null", StudentGroup.class).setParameter("sessionId", sessionId).list();
		return hibSession.createQuery("from StudentGroup x where x.session.uniqueId = :sessionId and x.type.uniqueId = :typeId", StudentGroup.class).setParameter("sessionId", sessionId).setParameter("typeId", typeId).list();
	}
}
