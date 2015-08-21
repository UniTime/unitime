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
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.UpdateRoomFeatureRequest;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.RoomDeptDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(UpdateRoomFeatureRequest.class)
public class UpdateRoomFeatureBackend implements GwtRpcImplementation<UpdateRoomFeatureRequest, FeatureInterface> {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public FeatureInterface execute(UpdateRoomFeatureRequest request, SessionContext context) {
		Transaction tx = null;
        try {
            org.hibernate.Session hibSession = new RoomDeptDAO().getSession();
            tx = hibSession.beginTransaction();

            if (request.hasFeature()) {
            	RoomFeature rf = null;
            	
            	if (request.getFeature().getId() == null) {
            		Department d = request.getFeature().isDepartmental() ? DepartmentDAO.getInstance().get(request.getFeature().getDepartment().getId(), hibSession) : null;
            		if (d == null)
            			context.checkPermission(Right.GlobalRoomFeatureAdd);
            		else
            			context.checkPermission(d, Right.DepartmentRoomFeatureAdd);
            		
            		if (d == null) {
            			rf = new GlobalRoomFeature();
            			((GlobalRoomFeature)rf).setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()));
            		} else {
            			rf = new DepartmentRoomFeature();
            			((DepartmentRoomFeature)rf).setDepartment(d);
            		}
        			rf.setRooms(new HashSet<Location>());
            	} else {
            		rf = RoomFeatureDAO.getInstance().get(request.getFeature().getId(), hibSession);
            		if (rf == null) throw new GwtRpcException(MESSAGES.errorRoomFeatureDoesNotExist(request.getFeature().getId()));
            		
            		if (rf instanceof GlobalRoomFeature)
            			context.checkPermission(rf, Right.GlobalRoomFeatureEdit);
            		else
            			context.checkPermission(rf, Right.DepartmenalRoomFeatureEdit);
            	}
            	
    			for (Iterator i = RoomFeature.getAllGlobalRoomFeatures(context.getUser().getCurrentAcademicSessionId()).iterator();i.hasNext();) {
    				RoomFeature x = (RoomFeature)i.next();
    				if (x.getLabel().equalsIgnoreCase(request.getFeature().getLabel()) && !x.getUniqueId().equals(request.getFeature().getId()))
    					throw new GwtRpcException(MESSAGES.errorRoomFeatureAlreadyExists(request.getFeature().getLabel()));
    			}
    			
    			if (rf instanceof DepartmentRoomFeature) {
    				for (Iterator i=RoomFeature.getAllDepartmentRoomFeatures(((DepartmentRoomFeature)rf).getDepartment()).iterator();i.hasNext();) {
    					RoomFeature x = (RoomFeature)i.next();
    					if (x.getLabel().equalsIgnoreCase(request.getFeature().getLabel()) && !x.getUniqueId().equals(request.getFeature().getId()))
    						throw new GwtRpcException(MESSAGES.errorRoomFeatureAlreadyExists(request.getFeature().getLabel()));
    				}
    			}
            	
            	rf.setAbbv(request.getFeature().getAbbreviation());
            	rf.setLabel(request.getFeature().getLabel());
            	rf.setFeatureType(request.getFeature().getType() == null ? null : RoomFeatureTypeDAO.getInstance().get(request.getFeature().getType().getId(), hibSession));

            	hibSession.saveOrUpdate(rf);
            	
            	if (request.hasAddLocations())
    				for (Location location: (List<Location>)hibSession.createQuery("from Location where uniqueId in :ids").setParameterList("ids", request.getAddLocations()).list()) {
    					rf.getRooms().add(location);
    					location.getFeatures().add(rf);
    					hibSession.saveOrUpdate(location);
    				}

            	if (request.hasDropLocations())
    				for (Location location: (List<Location>)hibSession.createQuery("from Location where uniqueId in :ids").setParameterList("ids", request.getDropLocations()).list()) {
    					rf.getRooms().remove(location);
    					location.getFeatures().remove(rf);
    					hibSession.saveOrUpdate(location);
    				}
            	
            	hibSession.saveOrUpdate(rf);
            	
	            ChangeLog.addChange(
	                    hibSession, 
	                    context, 
	                    rf, 
	                    ChangeLog.Source.ROOM_FEATURE_EDIT, 
	                    (request.getFeature().getId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE),
	                    null, 
	                    rf instanceof DepartmentRoomFeature ? ((DepartmentRoomFeature)rf).getDepartment() : null);
	            
	            tx.commit();
	            return null;
            } else if (request.getDeleteFeatureId() != null) {
            	RoomFeature rf = RoomFeatureDAO.getInstance().get(request.getDeleteFeatureId(), hibSession);
        		if (rf == null) throw new GwtRpcException(MESSAGES.errorRoomFeatureDoesNotExist(request.getDeleteFeatureId()));
        		
        		if (rf instanceof GlobalRoomFeature)
        			context.checkPermission(rf, Right.GlobalRoomFeatureDelete);
        		else
        			context.checkPermission(rf, Right.DepartmenalRoomFeatureDelete);
        		
                ChangeLog.addChange(
                        hibSession, 
                        context, 
                        rf, 
                        ChangeLog.Source.ROOM_FEATURE_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        null, 
                        rf instanceof DepartmentRoomFeature ? ((DepartmentRoomFeature)rf).getDepartment() : null);
                
                for (Location location: rf.getRooms()) {
                	location.getFeatures().remove(rf);
                	hibSession.saveOrUpdate(location);
                }
                
                for (RoomFeaturePref p: (List<RoomFeaturePref>)hibSession.createQuery("from RoomFeaturePref p where p.roomFeature.uniqueId = :id")
    						.setLong("id", request.getDeleteFeatureId()).list()) {
    					p.getOwner().getPreferences().remove(p);
    					hibSession.delete(p);
    					hibSession.saveOrUpdate(p.getOwner());
    				}
                
                hibSession.delete(rf);
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
