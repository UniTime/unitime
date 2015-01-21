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
package org.unitime.timetable.model.dao;

import java.util.List;

import org.hibernate.criterion.Order;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.base.BaseSubjectAreaDAO;



/**
 * @author Tomas Muller
 */
public class SubjectAreaDAO extends BaseSubjectAreaDAO {

	/**
	 * Default constructor.  Can be used in place of getInstance()
	 */
	public SubjectAreaDAO () {}

	public Order getDefaultOrder() {
		return Order.asc(SubjectArea.PROP_SUBJECT_AREA_ABBREVIATION);
	}
	
	public SubjectArea getSubjectAreaForSession(String subjectAreaAbbreviation, Session session){
	     StringBuffer sb = new StringBuffer();
	     sb.append("select sa.* from SubjectArea as sa ");
	     sb.append(" where sa.subjectAreaAbbreviation = '" + subjectAreaAbbreviation + "' ");
	     sb.append(" and sa.session.getUniqueId = " + session.getUniqueId());
	     
	     List results = getQuery(sb.toString()).list();
	     if (results.size() != 1){
	    	 return(null);
	     } else {
	    	 return((SubjectArea) results.get(0));
	     }
	}
	
}
