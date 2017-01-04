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

import org.unitime.timetable.model.base.BasePointInTimeData;
import org.unitime.timetable.model.dao.PointInTimeDataDAO;

public class PointInTimeData extends BasePointInTimeData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5725136274124746987L;
	 		
	public PointInTimeData() {
		super();
	}

	public static ArrayList<PointInTimeData> findAllForSession(Session acadSession, org.hibernate.Session hibSession){
		return(findAllForSession(acadSession.getUniqueId(), hibSession));
	}
	
	public static ArrayList<PointInTimeData> findAllForSession(Session acadSession){
		return(findAllForSession(acadSession.getUniqueId(), null));
	}

	public static ArrayList<PointInTimeData> findAllForSession(Long acadSessionUniqueId){
		return(findAllForSession(acadSessionUniqueId, null));
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<PointInTimeData> findAllForSession(Long acadSessionUniqueId, org.hibernate.Session hibSession){
		org.hibernate.Session hibSess = hibSession;
		if (hibSess == null){
			hibSess = PointInTimeDataDAO.getInstance().getSession();
		}
		return((ArrayList<PointInTimeData>) hibSess
				.createQuery("from PointInTimeData pitd where pitd.session.uniqueId = :sessionId")
				.setLong("sessionId", acadSessionUniqueId.longValue())
				.list());
	}
	
	
}
