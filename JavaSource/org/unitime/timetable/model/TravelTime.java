/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model;

import java.util.List;

import net.sf.cpsolver.ifs.util.DistanceMetric;

import org.unitime.timetable.model.base.BaseTravelTime;
import org.unitime.timetable.model.dao.TravelTimeDAO;

public class TravelTime extends BaseTravelTime {
	private static final long serialVersionUID = -5810111960278939304L;

	public TravelTime() {
		super();
	}
	
	public static void populateTravelTimes(DistanceMetric metric, Long sessionId, org.hibernate.Session hibSession) {
		for (TravelTime time: (List<TravelTime>)hibSession.createQuery(
				"from TravelTime where session.uniqueId = :sessionId")
				.setLong("sessionId", sessionId).setCacheable(true).list())
			metric.addTravelTime(time.getLocation1Id(), time.getLocation2Id(), time.getDistance());
	}
	
	public static void populateTravelTimes(DistanceMetric metric, Long sessionId) {
		org.hibernate.Session hibSession = TravelTimeDAO.getInstance().createNewSession();
		populateTravelTimes(metric, sessionId, hibSession);
		hibSession.close();
	}
	
	public static void populateTravelTimes(DistanceMetric metric, org.hibernate.Session hibSession) {
		for (TravelTime time: (List<TravelTime>)hibSession.createQuery("from TravelTime").setCacheable(true).list())
			metric.addTravelTime(time.getLocation1Id(), time.getLocation2Id(), time.getDistance());
	}
	
	public static void populateTravelTimes(DistanceMetric metric) {
		org.hibernate.Session hibSession = TravelTimeDAO.getInstance().createNewSession();
		populateTravelTimes(metric, hibSession);
		hibSession.close();
	}

}
