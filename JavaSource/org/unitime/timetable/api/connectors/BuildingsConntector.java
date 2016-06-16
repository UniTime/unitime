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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("/api/buildings")
public class BuildingsConntector extends ApiConnector {
	@Override
	public void doGet(ApiHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		helper.getSessionContext().checkPermissionAnyAuthority(sessionId, "Session", Right.ApiRetrieveRooms);
		
		List<BuildingInterface> buildings = new ArrayList<BuildingInterface>();
		for (Building b: Building.findAll(sessionId)) {
			BuildingInterface building = new BuildingInterface();
			building.setId(b.getUniqueId());
			building.setName(b.getName());
			building.setAbbreviation(b.getAbbreviation());
			building.setX(b.getCoordinateX());
			building.setY(b.getCoordinateY());
			building.setExternalId(b.getExternalUniqueId());
			buildings.add(building);
		}
		helper.setResponse(buildings);
	}
	
	@Override
	public void doDelete(ApiHelper helper) throws IOException {
		Transaction tx = helper.getHibSession().beginTransaction();
		try {
			Building building = null;
			Long buildingId = helper.getOptinalParameterLong("id", null);
			if (buildingId != null) {
				building = BuildingDAO.getInstance().get(buildingId, helper.getHibSession());
				if (building == null)
					throw new IllegalArgumentException("Building " + buildingId + " does not exist.");
			} else {
				Long sessionId = helper.getAcademicSessionId();
				if (sessionId == null)
					throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
				String externalId = helper.getOptinalParameter("externalId", null);
				if (externalId != null) {
					building = (Building)helper.getHibSession().createQuery("from Building where externalUniqueId = :externalId and session.uniqueId = :sessionId")
							.setLong("sessionId", sessionId).setString("externalId", externalId).setMaxResults(1).uniqueResult();
					if (building == null)
						throw new IllegalArgumentException("Building " + externalId + " does not exist.");
				}
				if (building == null) {
					String abbv = helper.getRequiredParameter("building");
					building = (Building)helper.getHibSession().createQuery("from Building where (abbreviation = :abbv or name = :abbv) and session.uniqueId = :sessionId")
							.setLong("sessionId", sessionId).setString("abbv", abbv).setMaxResults(1).uniqueResult();
					if (building == null)
						throw new IllegalArgumentException("Building " + abbv + " does not exist.");
				}
			}
			helper.getSessionContext().checkPermissionAnyAuthority(building.getSession(), Right.ApiRoomEdit);
			helper.getSessionContext().checkPermissionAnyAuthority(building, Right.BuildingDelete);
			
			for (Room r: (List<Room>)BuildingDAO.getInstance().getSession().createQuery("from Room r where r.building.uniqueId = :buildingId").setLong("buildingId", building.getUniqueId()).list()) {
				helper.getHibSession().createQuery("delete RoomPref p where p.room.uniqueId = :roomId").setLong("roomId", r.getUniqueId()).executeUpdate();
				for (Iterator<Assignment> i = r.getAssignments().iterator(); i.hasNext(); ) {
					Assignment a = i.next();
                    a.getRooms().remove(r);
                    helper.getHibSession().saveOrUpdate(a);
                    i.remove();
                }
				helper.getHibSession().delete(r);
			}
			ChangeLog.addChange(
                    helper.getHibSession(),
                    TimetableManager.findByExternalId(sessionContext.getUser().getExternalUserId()),
                    building.getSession(),
                    building, 
                    ChangeLog.Source.BUILDING_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    null);
			helper.getHibSession().delete(building);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) { tx.rollback(); }
			if (e instanceof RuntimeException) throw (RuntimeException)e;
			if (e instanceof IOException) throw (IOException)e;
			throw new IOException(e.getMessage(), e);
		}
	}

		
	@Override
	public void doPost(ApiHelper helper) throws IOException {
		BuildingInterface b = helper.getRequest(BuildingInterface.class);
		Transaction tx = helper.getHibSession().beginTransaction();
		try {
			Building building = null;
			if (b.getId() != null) {
				building = BuildingDAO.getInstance().get(b.getId(), helper.getHibSession());
				if (building == null)
					throw new IllegalArgumentException("Building " + b.getId() + " does not exist.");
			} else {
				Long sessionId = helper.getAcademicSessionId();
				if (sessionId == null)
					throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
				if (b.getExternalId() != null) {
					building = (Building)helper.getHibSession().createQuery("from Building where externalUniqueId = :externalId and session.uniqueId = :sessionId")
							.setLong("sessionId", sessionId).setString("externalId", b.getExternalId()).setMaxResults(1).uniqueResult();
				} else if (b.getAbbreviation() != null) {
					building = (Building)helper.getHibSession().createQuery("from Building where abbreviation = :abbv and session.uniqueId = :sessionId")
							.setLong("sessionId", sessionId).setString("abbv", b.getAbbreviation()).setMaxResults(1).uniqueResult();
				}
			}
			if (building != null) {
				helper.getSessionContext().checkPermissionAnyAuthority(building.getSession(), Right.ApiRoomEdit);
				helper.getSessionContext().checkPermissionAnyAuthority(building, Right.BuildingEdit);
			} else {
				helper.getSessionContext().checkPermissionAnyAuthority(helper.getAcademicSessionId(), "Session", Right.ApiRoomEdit);
				helper.getSessionContext().checkPermissionAnyAuthority(helper.getAcademicSessionId(), "Session", Right.BuildingAdd);
			}
			
			ChangeLog.Operation op = null;
			if (building == null) {
	        	building = new Building();
	        	building.setSession(SessionDAO.getInstance().get(helper.getAcademicSessionId(), helper.getHibSession()));
	        	op = ChangeLog.Operation.CREATE;
	        } else {
	        	op = ChangeLog.Operation.UPDATE;
	        }
	        building.setName(b.getName());
	        building.setAbbreviation(b.getAbbreviation());
	        building.setExternalUniqueId(b.getExternalId());
	        building.setCoordinateX(b.getX());
	        building.setCoordinateY(b.getY());
	        helper.getHibSession().saveOrUpdate(building);
	        
	        b.setId(building.getUniqueId());
	        
	        ChangeLog.addChange(
	        		helper.getHibSession(), 
	        		TimetableManager.findByExternalId(sessionContext.getUser().getExternalUserId()),
                    building.getSession(),
	                building, 
	                ChangeLog.Source.BUILDING_EDIT, 
	                op, 
	                null, 
	                null);
			tx.commit();
		} catch (Exception e) {
			if (tx != null) { tx.rollback(); }
			if (e instanceof RuntimeException) throw (RuntimeException)e;
			if (e instanceof IOException) throw (IOException)e;
			throw new IOException(e.getMessage(), e);
		}
		helper.setResponse(b);
	}
	
	@Override
	protected String getName() {
		return "buildings";
	}
}
