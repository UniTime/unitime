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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import org.cpsolver.ifs.util.DistanceMetric;
import org.unitime.timetable.model.base.BaseTravelTime;
import org.unitime.timetable.model.dao.TravelTimeDAO;

/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
@Table(name = "travel_time")
public class TravelTime extends BaseTravelTime {
	private static final long serialVersionUID = -5810111960278939304L;

	public TravelTime() {
		super();
	}
	
	public static void populateTravelTimes(DistanceMetric metric, Long sessionId, org.hibernate.Session hibSession) {
		for (TravelTime time: hibSession.createQuery(
				"from TravelTime where session.uniqueId = :sessionId", TravelTime.class)
				.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).setCacheable(true).list())
			metric.addTravelTime(time.getLocation1Id(), time.getLocation2Id(), time.getDistance());
	}
	
	public static void populateTravelTimes(DistanceMetric metric, Long sessionId) {
		org.hibernate.Session hibSession = TravelTimeDAO.getInstance().createNewSession();
		populateTravelTimes(metric, sessionId, hibSession);
		hibSession.close();
	}
	
	public static void populateTravelTimes(DistanceMetric metric, org.hibernate.Session hibSession) {
		for (TravelTime time: hibSession.createQuery("from TravelTime", TravelTime.class).setCacheable(true).list())
			metric.addTravelTime(time.getLocation1Id(), time.getLocation2Id(), time.getDistance());
	}
	
	public static void populateTravelTimes(DistanceMetric metric) {
		org.hibernate.Session hibSession = TravelTimeDAO.getInstance().createNewSession();
		populateTravelTimes(metric, hibSession);
		hibSession.close();
	}

}
