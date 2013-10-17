/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.rooms;

import java.util.List;
import java.util.TreeSet;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.DistanceMetric;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
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
		config.setProperty("Distances.Ellipsoid", ApplicationProperties.getProperty("unitime.distance.ellipsoid", DistanceMetric.Ellipsoid.LEGACY.name()));
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
