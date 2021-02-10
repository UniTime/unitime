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

import java.util.Iterator;
import java.util.List;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.UpdateBuildingRequest;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(UpdateBuildingRequest.class)
public class UpdateBuildingBackend implements GwtRpcImplementation<UpdateBuildingRequest, BuildingInterface>{

	@Override
	public BuildingInterface execute(UpdateBuildingRequest request, SessionContext context) {
		switch (request.getAction()) {
		case CREATE:
			context.checkPermission(Right.BuildingAdd);
			saveOrUpdate(request.getBuilding(), false, context);
			break;
		case UPDATE:
			context.checkPermission(request.getBuilding().getId(), "Building", Right.BuildingEdit);
			saveOrUpdate(request.getBuilding(), request.getUpdateRoomCoordinates(), context);
			break;
		case DELETE:
			context.checkPermission(request.getBuilding().getId(), "Building", Right.BuildingDelete);
			delete(request.getBuilding(), context);
			break;
		case UPDATE_DATA:
			context.checkPermission(Right.BuildingUpdateData);
			updateRooms(context);
			break;
		}
		return request.getBuilding();
	}
	
	protected void saveOrUpdate(BuildingInterface buildingInterface, boolean updateRoomCoordinates, SessionContext context) throws GwtRpcException {
		Transaction tx = null;
        try {
        	org.hibernate.Session hibSession = BuildingDAO.getInstance().getSession();
        	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
        		tx = hibSession.beginTransaction();
        	
            Building building = null;
            if (buildingInterface.getId() != null) {
            	building = BuildingDAO.getInstance().get(buildingInterface.getId(), hibSession);
            }
            if (building==null) {
            	building = new Building();
            	building.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
            }
            building.setName(buildingInterface.getName());
            building.setAbbreviation(buildingInterface.getAbbreviation());
            building.setExternalUniqueId(buildingInterface.getExternalId() != null && buildingInterface.getExternalId().isEmpty() ? null : buildingInterface.getExternalId());
            building.setCoordinateX(buildingInterface.getX());
            building.setCoordinateY(buildingInterface.getY());
            if (building.getUniqueId() == null) {
            	buildingInterface.setId((Long)hibSession.save(building));
            } else {
            	hibSession.update(building);
            }
            
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    building, 
                    ChangeLog.Source.BUILDING_EDIT, 
                    (building.getUniqueId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE), 
                    null, 
                    null);
            
            if (updateRoomCoordinates) {
            	for (Room room: (List<Room>)hibSession.createQuery("from Room r where r.building.uniqueId = :buildingId").setLong("buildingId", building.getUniqueId()).list()) {
            		if (!ToolBox.equals(room.getCoordinateX(), building.getCoordinateX()) || !ToolBox.equals(room.getCoordinateY(), building.getCoordinateY())) {
            			room.setCoordinateX(building.getCoordinateX());
            			room.setCoordinateY(building.getCoordinateY());
            			hibSession.update(room);
            			ChangeLog.addChange(
            	                hibSession, 
            	                context, 
            	                room,
            	                room.getLabel() + " moved to " + room.getCoordinateX() + "," + room.getCoordinateY(),
            	                ChangeLog.Source.BUILDING_EDIT, 
            	                ChangeLog.Operation.UPDATE, 
            	                null, 
            	                null);
            		}
            	}
            }
        	
			tx.commit();
			HibernateUtil.clearCache();
	    } catch (Exception e) {
	    	if (tx!=null) tx.rollback();
	    	throw new GwtRpcException(e.getMessage(), e);
	    }
	}
	
	protected void delete(BuildingInterface buildingInterface, SessionContext context) throws GwtRpcException {
		Transaction tx = null;
        try {
        	org.hibernate.Session hibSession = BuildingDAO.getInstance().getSession();
        	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
        		tx = hibSession.beginTransaction();
        	
            Building building = BuildingDAO.getInstance().get(buildingInterface.getId());
            
            if (building != null) {
                for (Iterator i= hibSession.createQuery("select r from Room r where r.building.uniqueId=:buildingId").setLong("buildingId", building.getUniqueId()).iterate(); i.hasNext();) {
                    Room r = (Room)i.next();
                    hibSession.createQuery("delete RoomPref p where p.room.uniqueId=:roomId").setLong("roomId", r.getUniqueId()).executeUpdate();
                    for (Iterator j=r.getAssignments().iterator();j.hasNext();) {
                        Assignment a = (Assignment)j.next();
                        a.getRooms().remove(r);
                        hibSession.saveOrUpdate(a);
                        j.remove();
                    }
                    hibSession.delete(r);
                }
                
                ChangeLog.addChange(
                        hibSession, 
                        context, 
                        building, 
                        ChangeLog.Source.BUILDING_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        null, 
                        null);
                hibSession.delete(building);
            }
        	
			tx.commit();
			HibernateUtil.clearCache();
	    } catch (Exception e) {
	    	if (tx!=null) tx.rollback();
	    	throw new GwtRpcException(e.getMessage(), e);
	    }
	}
	
	protected void updateRooms(SessionContext context) throws GwtRpcException {
		Transaction tx = null;
        try {
        	org.hibernate.Session hibSession = BuildingDAO.getInstance().getSession();
        	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
        		tx = hibSession.beginTransaction();
        	
        	Room.addNewExternalRoomsToSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()));
        	
			tx.commit();
			HibernateUtil.clearCache();
	    } catch (Exception e) {
	    	if (tx!=null) tx.rollback();
	    	throw new GwtRpcException(e.getMessage(), e);
	    }
	}

}
