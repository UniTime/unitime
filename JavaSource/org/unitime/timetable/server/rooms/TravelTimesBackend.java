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
package org.unitime.timetable.server.rooms;

import java.util.List;
import java.util.TreeSet;


import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.DistanceMetric;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.rooms.TravelTimes;
import org.unitime.timetable.gwt.client.rooms.TravelTimes.TravelTimeResponse;
import org.unitime.timetable.gwt.client.rooms.TravelTimes.TravelTimesRequest;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(TravelTimesRequest.class)
public class TravelTimesBackend implements GwtRpcImplementation<TravelTimesRequest, TravelTimeResponse>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public TravelTimeResponse execute(TravelTimesRequest request, SessionContext context) {
		TravelTimeResponse response = new TravelTimeResponse();
		
		switch (request.getCommand()) {
		case INIT:
			context.checkPermission(Right.TravelTimesLoad);
			return new TravelTimeResponse(context.getUser().getCurrentAcademicSessionId(), SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()).getLabel());
		case LOAD:
			context.checkPermission(Right.TravelTimesLoad);
			load(context.getUser().getCurrentAcademicSessionId(), request, response);
			break;
		case SAVE:
			context.checkPermission(Right.TravelTimesSave);
			save(context.getUser().getCurrentAcademicSessionId(), request);
		}
		
		return response;
	}
	
	@SuppressWarnings("deprecation")
	protected void load(Long sessionId, TravelTimesRequest request, TravelTimeResponse response) {
		DataProperties config = new DataProperties();
		config.setProperty("Distances.Ellipsoid", ApplicationProperty.DistanceEllipsoid.value());
		config.setProperty("Distances.Speed", ApplicationProperty.EventDistanceSpeed.value());
		DistanceMetric metric = new DistanceMetric(config);
		TravelTime.populateTravelTimes(metric, sessionId);
		
		String ids = ""; int count = 0;
		if (request.hasRooms()) {
			for (TravelTimes.Room r: request.getRooms()) {
				ids += (ids.isEmpty() ? "" : ",") + r.getId();
				count++;
				if (count == 100) break;
			}
		}
		
		org.hibernate.Session hibSession = LocationDAO.getInstance().getSession();
		TreeSet<Location> locations = new TreeSet<Location>(
				hibSession.createQuery(
						"select distinct l from Location l " +
						"where l.session.uniqueId = :sessionId" + (ids.isEmpty() ? "" : " and l.uniqueId in (" + ids + ")"))
						.setLong("sessionId", sessionId).setCacheable(true).list());
		
		List<TravelTime> times = (List<TravelTime>)hibSession.createQuery(
				"select t from TravelTime t " + 
				"where t.session.uniqueId = :sessionId" + (ids.isEmpty() ? "" : " and t.location1Id in (" + ids + ") and t.location1Id in (" + ids + ")"))
				.setLong("sessionId", sessionId).setCacheable(true).list();
		
		for (Location location: locations) {
			TravelTimes.Room room = null;
			if (location instanceof Room) {
				Room r = (Room)location;
				room = new TravelTimes.Room(r.getUniqueId(), r.getLabel(), new TravelTimes.Building(r.getBuilding().getUniqueId(), r.getBuilding().getAbbreviation()));
 			} else {
 				room = new TravelTimes.Room(location.getUniqueId(), location.getLabel());
 			}
			for (Location other: locations) {
				if (location.equals(other)) continue;
				if (location.getCoordinateX() != null && location.getCoordinateY() != null && other.getCoordinateX() != null && other.getCoordinateY() != null) {
					room.setDistance(other.getUniqueId(), metric.getDistanceInMinutes(
							location.getCoordinateX(), location.getCoordinateY(), other.getCoordinateX(), other.getCoordinateY()));
				}
				room.setTravelTime(other.getUniqueId(), metric.getTravelTimeInMinutes(location.getUniqueId(), other.getUniqueId()));
			}
			for (TravelTime t: times) {
				if (t.getLocation1Id().equals(location.getUniqueId()))
					room.setTravelTime(t.getLocation2Id(), t.getDistance());
				if (t.getLocation2Id().equals(location.getUniqueId()))
					room.setTravelTime(t.getLocation1Id(), t.getDistance());
			}
			response.addRoom(room);
			if (response.getRooms().size() >= 100) break;
		}
	}
	
	protected void save(Long sessionId, TravelTimesRequest request) {
		if (!request.hasRooms()) return;
		
		org.hibernate.Session hibSession = LocationDAO.getInstance().getSession();

		String ids = "";
		for (TravelTimes.Room r: request.getRooms())
			ids += (ids.isEmpty() ? "" : ",") + r.getId();
		
		Session session = SessionDAO.getInstance().get(sessionId);
		
		
		hibSession.createQuery(
				"delete from TravelTime where session.uniqueId = :sessionId" +
				(ids.isEmpty() ? "" : " and location1Id in (" + ids + ") and location2Id in (" + ids + ")"))
				.setLong("sessionId", sessionId)
				.executeUpdate();
		
		for (TravelTimes.Room room: request.getRooms()) {
			for (TravelTimes.Room other: request.getRooms()) {
				if (room.getId().compareTo(other.getId()) < 0) {
					Integer distance = room.getTravelTime(other);
					if (distance != null) {
						TravelTime time = new TravelTime();
						time.setSession(session);
						time.setLocation1Id(room.getId());
						time.setLocation2Id(other.getId());
						time.setDistance(distance);
						hibSession.saveOrUpdate(time);
					}
				}
			}
		}
		hibSession.flush();
	}
}
