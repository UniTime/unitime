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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.UpdateRoomGroupRequest;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.RoomDeptDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(UpdateRoomGroupRequest.class)
public class UpdateRoomGroupBackend implements GwtRpcImplementation<UpdateRoomGroupRequest, GroupInterface> {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public GroupInterface execute(UpdateRoomGroupRequest request, SessionContext context) {
		Transaction tx = null;
        try {
            org.hibernate.Session hibSession = new RoomDeptDAO().getSession();
            tx = hibSession.beginTransaction();

            if (request.hasGroup()) {
            	RoomGroup rg = null;
            	
            	if (request.getGroup().getId() == null) {
            		Department d = request.getGroup().isDepartmental() ? DepartmentDAO.getInstance().get(request.getGroup().getDepartment().getId(), hibSession) : null;
            		if (d == null)
            			context.checkPermission(Right.GlobalRoomGroupAdd);
            		else
            			context.checkPermission(d, Right.DepartmentRoomGroupAdd);
            		
            		rg = new RoomGroup();
            		rg.setGlobal(d == null);
            		rg.setDepartment(d);
        			rg.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()));
        			rg.setRooms(new HashSet<Location>());
        			rg.setDefaultGroup(false);
            	} else {
            		rg = RoomGroupDAO.getInstance().get(request.getGroup().getId(), hibSession);
            		if (rg == null) throw new GwtRpcException(MESSAGES.errorRoomGroupDoesNotExist(request.getGroup().getId()));
            		
            		if (rg.isGlobal())
            			context.checkPermission(rg, Right.GlobalRoomGroupEdit);
            		else
            			context.checkPermission(rg, Right.DepartmenalRoomGroupEdit);
            	}
            	
    			for (Iterator i = RoomGroup.getAllGlobalRoomGroups(context.getUser().getCurrentAcademicSessionId()).iterator();i.hasNext();) {
    				RoomGroup x = (RoomGroup)i.next();
    				if (x.getName().equalsIgnoreCase(request.getGroup().getLabel()) && !x.getUniqueId().equals(request.getGroup().getId()))
    					throw new GwtRpcException(MESSAGES.errorRoomGroupAlreadyExists(request.getGroup().getLabel()));
    			}
    			
    			if (rg.getDepartment() != null) {
    				for (Iterator i=RoomGroup.getAllDepartmentRoomGroups(rg.getDepartment()).iterator();i.hasNext();) {
    					RoomGroup x = (RoomGroup)i.next();
    					if (x.getName().equalsIgnoreCase(request.getGroup().getLabel()) && !x.getUniqueId().equals(request.getGroup().getId()))
    						throw new GwtRpcException(MESSAGES.errorRoomGroupAlreadyExists(request.getGroup().getLabel()));
    				}
    			}
            	
            	if (rg.getDepartment() != null)
            		
            	
            	rg.setAbbv(request.getGroup().getAbbreviation());
            	rg.setName(request.getGroup().getLabel());
            	rg.setDescription(request.getGroup().getDescription());

            	if (rg.isGlobal() && request.getGroup().isDefault() && context.hasPermission(rg, Right.GlobalRoomGroupEditSetDefault)) {
            		for (RoomGroup x: RoomGroup.getAllRoomGroupsForSession(rg.getSession())) {
            			if (!x.getUniqueId().equals(rg.getUniqueId()) && x.isDefaultGroup()) {
            				x.setDefaultGroup(false);
            				hibSession.saveOrUpdate(x);
            			}
            		}
            	}
            	rg.setDefaultGroup(request.getGroup().isDefault() && rg.isGlobal());

            	hibSession.saveOrUpdate(rg);
            	
            	if (request.hasAddLocations())
    				for (Location location: (List<Location>)hibSession.createQuery("from Location where uniqueId in :ids").setParameterList("ids", request.getAddLocations()).list()) {
    					rg.getRooms().add(location);
    					location.getRoomGroups().add(rg);
    					hibSession.saveOrUpdate(location);
    				}

            	if (request.hasDropLocations())
    				for (Location location: (List<Location>)hibSession.createQuery("from Location where uniqueId in :ids").setParameterList("ids", request.getDropLocations()).list()) {
    					rg.getRooms().remove(location);
    					location.getRoomGroups().remove(rg);
    					hibSession.saveOrUpdate(location);
    				}
            	
            	hibSession.saveOrUpdate(rg);
            	
	            ChangeLog.addChange(
	                    hibSession, 
	                    context, 
	                    rg, 
	                    ChangeLog.Source.ROOM_GROUP_EDIT, 
	                    (request.getGroup().getId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE),
	                    null, 
	                    rg.getDepartment());
	            
	            tx.commit();
	            return null;
            } else if (request.getDeleteGroupId() != null) {
            	RoomGroup rg = RoomGroupDAO.getInstance().get(request.getDeleteGroupId(), hibSession);
        		if (rg == null) throw new GwtRpcException(MESSAGES.errorRoomGroupDoesNotExist(request.getDeleteGroupId()));
        		
        		if (rg.isGlobal())
        			context.checkPermission(rg, Right.GlobalRoomGroupDelete);
        		else
        			context.checkPermission(rg, Right.DepartmenalRoomGroupDelete);
        		
                ChangeLog.addChange(
                        hibSession, 
                        context, 
                        rg, 
                        ChangeLog.Source.ROOM_GROUP_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        null, 
                        rg.getDepartment());
                
                for (Location location: rg.getRooms()) {
                	location.getRoomGroups().remove(rg);
                	hibSession.saveOrUpdate(location);
                }
                
                for (RoomGroupPref p: (List<RoomGroupPref>)hibSession.createQuery("from RoomGroupPref p where p.roomGroup.uniqueId = :id")
    						.setLong("id", request.getDeleteGroupId()).list()) {
    					p.getOwner().getPreferences().remove(p);
    					hibSession.delete(p);
    					hibSession.saveOrUpdate(p.getOwner());
    				}
                
                hibSession.delete(rg);
                tx.commit();
    			
            	return null;
            } else {
            	throw new GwtRpcException("Bad request.");
            }
        } catch (Exception e) {
        	e.printStackTrace();
            if (tx != null) tx.rollback();
            if (e instanceof GwtRpcException) throw (GwtRpcException) e;
            throw new GwtRpcException(e.getMessage());
        }
	}

}
