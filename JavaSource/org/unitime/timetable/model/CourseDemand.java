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

import org.unitime.timetable.model.base.BaseCourseDemand;
import org.unitime.timetable.model.dao.CourseDemandDAO;

/**
 * @author Tomas Muller
 */
public class CourseDemand extends BaseCourseDemand implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseDemand () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseDemand (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public int compareTo(Object o) {
        if (o==null || !(o instanceof CourseDemand)) return -1;
        CourseDemand cd = (CourseDemand)o;
        int cmp = (isAlternative().booleanValue() == cd.isAlternative().booleanValue() ? 0 : isAlternative().booleanValue() ? 1 : -1);
        if (cmp!=0) return cmp;
        cmp = getPriority().compareTo(cd.getPriority());
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(cd.getUniqueId() == null ? -1 : cd.getUniqueId());
    }
    
    public static List findAll(Long sessionId) {
    	return findAll(CourseDemandDAO.getInstance().getSession(), sessionId);
    }
    
    public static List findAll(org.hibernate.Session hibSession, Long sessionId) {
        return hibSession.
            createQuery("select c from CourseDemand c where c.student.session.uniqueId=:sessionId").
            setLong("sessionId", sessionId.longValue()).
            list(); 
    }
}
