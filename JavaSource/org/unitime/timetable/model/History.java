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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseHistory;
import org.unitime.timetable.model.dao.HistoryDAO;




/**
 * @author Tomas Muller
 */
public class History extends BaseHistory {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public History () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public History (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/**
	 * Retrieves all history data for the academic session
	 * @param sessionId academic session
	 * @param aClass history class
	 * @return List of aClassHistory objects
	 */
	public static List getHistoryList(Long sessionId, Class aClass) 
			throws HibernateException {
	    
	    HistoryDAO adao = new HistoryDAO();
	    Session hSession = adao.getSession();
	    List aaList = hSession.createCriteria(aClass)
				    .add(Restrictions.eq("sessionId", sessionId))
				    .list();
		return aaList;
	}
	

}
