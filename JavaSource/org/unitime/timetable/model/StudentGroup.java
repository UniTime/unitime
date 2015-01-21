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

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseStudentGroup;
import org.unitime.timetable.model.dao.StudentGroupDAO;




/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
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
    
    public static StudentGroup findByExternalId(Session hibSession, String externalId, Long acadSessionId) {
    	 
        return (StudentGroup)hibSession.
            createQuery(
                    "select a from StudentGroup a where "+
                    "a.session.uniqueId = :acadSessionId and "+
                    "externalUniqueId=:eId ").
             setLong("acadSessionId", acadSessionId.longValue()).
             setString("eId", externalId).
             setCacheable(true).
             uniqueResult(); 
    }


}
