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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
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
		if (request.hasSessionId())
			context = new EventContext(context, request.getSessionId());

		Transaction tx = null;
		RoomFeature f = null;
        try {
            org.hibernate.Session hibSession = new RoomDeptDAO().getSession();
            tx = hibSession.beginTransaction();

            if (request.hasFeature()) {
            	
            	if (request.hasFutureSessions())
            		for (Long id: request.getFutureSessions())
            			createOrUpdateFeature(request.getFeature(), request.getAddLocations(), request.getDropLocations(), id, hibSession, new EventContext(context, context.getUser(), id), true);
            	f = createOrUpdateFeature(request.getFeature(), request.getAddLocations(), request.getDropLocations(), context.getUser().getCurrentAcademicSessionId(), hibSession, context, false);
	            
            } else if (request.getDeleteFeatureId() != null) {
            	
            	if (request.hasFutureSessions())
            		for (Long id: request.getFutureSessions())
            			dropFeature(request.getDeleteFeatureId(), id, hibSession, new EventContext(context, context.getUser(), id), true);
            	dropFeature(request.getDeleteFeatureId(), context.getUser().getCurrentAcademicSessionId(), hibSession, context, false);
            	
            } else {
            	throw new GwtRpcException("Bad request.");
            }
            
            FeatureInterface feature = null;
            if (f != null) {
            	feature = new FeatureInterface(f.getUniqueId(), f.getAbbv(), f.getLabel());
            	feature.setDescription(f.getDescription());
        		if (f.getFeatureType() != null)
        			feature.setType(new FeatureTypeInterface(f.getFeatureType().getUniqueId(), f.getFeatureType().getReference(), f.getFeatureType().getLabel(), f.getFeatureType().isShowInEventManagement()));
        		if (f instanceof DepartmentRoomFeature) {
        			Department d = ((DepartmentRoomFeature)f).getDepartment();
        			feature.setDepartment(RoomDetailsBackend.wrap(d, null, null));
        			feature.setTitle((f.getDescription() == null || f.getDescription().isEmpty() ? f.getLabel() : f.getDescription()) + " (" + d.getName() + (f.getFeatureType() == null ? "" : ", " + f.getFeatureType().getLabel()) + ")");
        		} else {
        			feature.setTitle((f.getDescription() == null || f.getDescription().isEmpty() ? f.getLabel() : f.getDescription()) + (f.getFeatureType() == null ? "" : " (" + f.getFeatureType().getLabel() + ")"));
        		}
            }

            tx.commit();
            
            return feature;
        } catch (Exception e) {
        	e.printStackTrace();
            if (tx != null) tx.rollback();
            if (e instanceof GwtRpcException) throw (GwtRpcException) e;
            throw new GwtRpcException(e.getMessage());
        }
	}
	
	protected Department lookuDepartment(org.hibernate.Session hibSession, DepartmentInterface original, boolean future, Long sessionId) {
		if (original == null) return null;
		if (future) {
			return Department.findByDeptCode(original.getDeptCode(), sessionId, hibSession);
		} else {
			return DepartmentDAO.getInstance().get(original.getId(), hibSession);
		}
	}
	
	protected RoomFeature lookupFeature(org.hibernate.Session hibSession, FeatureInterface original, boolean future, Long sessionId) {
		if (original == null) return null;
		if (future) {
			if (original.isDepartmental())
				return (DepartmentRoomFeature)hibSession.createQuery(
					"select f from DepartmentRoomFeature f, DepartmentRoomFeature o where o.uniqueId = :originalId and f.department.session.uniqueId = :sessionId " +
					"and f.abbv = o.abbv and f.department.deptCode = o.department.deptCode")
					.setLong("sessionId", sessionId).setLong("originalId", original.getId()).setCacheable(true).setMaxResults(1).uniqueResult();
			else
				return (GlobalRoomFeature)hibSession.createQuery(
					"select f from GlobalRoomFeature f, GlobalRoomFeature o where o.uniqueId = :originalId and f.session.uniqueId = :sessionId " +
					"and f.abbv = o.abbv")
					.setLong("sessionId", sessionId).setLong("originalId", original.getId()).setCacheable(true).setMaxResults(1).uniqueResult();
		} else {
			return RoomFeatureDAO.getInstance().get(original.getId(), hibSession);
		}
	}
	
	protected RoomFeature lookupFeature(org.hibernate.Session hibSession, Long featureId, boolean future, Long sessionId) {
		if (featureId == null) return null;
		if (future) {
			RoomFeature feature = (DepartmentRoomFeature)hibSession.createQuery(
					"select f from DepartmentRoomFeature f, DepartmentRoomFeature o where o.uniqueId = :originalId and f.department.session.uniqueId = :sessionId " +
					"and f.abbv = o.abbv and f.department.deptCode = o.department.deptCode")
					.setLong("sessionId", sessionId).setLong("originalId", featureId).setCacheable(true).setMaxResults(1).uniqueResult();
			if (feature == null)
				feature = (GlobalRoomFeature)hibSession.createQuery(
					"select f from GlobalRoomFeature f, GlobalRoomFeature o where o.uniqueId = :originalId and f.session.uniqueId = :sessionId " +
					"and f.abbv = o.abbv")
					.setLong("sessionId", sessionId).setLong("originalId", featureId).setCacheable(true).setMaxResults(1).uniqueResult();
			return feature;
		} else {
			return RoomFeatureDAO.getInstance().get(featureId, hibSession);
		}
	}
	
	protected Collection<Location> lookupLocations(org.hibernate.Session hibSession, List<Long> ids, boolean future, Long sessionId) {
		if (ids == null || ids.isEmpty()) return new ArrayList<Location>();
		if (future) {
			return Location.lookupFutureLocations(hibSession, ids, sessionId);
		} else {
			return (List<Location>)hibSession.createQuery("from Location where uniqueId in :ids").setParameterList("ids", ids).list();
		}
	}
	
	protected RoomFeature createOrUpdateFeature(FeatureInterface feature, List<Long> add, List<Long> drop, Long sessionId, org.hibernate.Session hibSession, SessionContext context, boolean future) {
		Department d = feature.isDepartmental() ? lookuDepartment(hibSession, feature.getDepartment(), future, sessionId) : null;
		if (feature.isDepartmental() && d == null) return null;

		RoomFeature rf = (feature.getId() == null ? null : lookupFeature(hibSession, feature, future, sessionId));

		if (rf == null) {
			if (!future && feature.getId() != null)
				throw new GwtRpcException(MESSAGES.errorRoomFeatureDoesNotExist(feature.getId()));
    		if (d == null) {
    			context.checkPermission(Right.GlobalRoomFeatureAdd);
    			rf = new GlobalRoomFeature();
    			((GlobalRoomFeature)rf).setSession(SessionDAO.getInstance().get(sessionId));
    		} else {
    			context.checkPermission(d, Right.DepartmentRoomFeatureAdd);
    			rf = new DepartmentRoomFeature();
    			((DepartmentRoomFeature)rf).setDepartment(d);
    		}
    		rf.setRooms(new HashSet<Location>());
		} else {
			if (rf instanceof GlobalRoomFeature) {
    			context.checkPermission(rf, Right.GlobalRoomFeatureEdit);
			} else {
    			context.checkPermission(rf, Right.DepartmenalRoomFeatureEdit);
    			((DepartmentRoomFeature)rf).setDepartment(d);
			}
		}
    	
		for (Iterator i = RoomFeature.getAllGlobalRoomFeatures(sessionId).iterator();i.hasNext();) {
			RoomFeature x = (RoomFeature)i.next();
			if ((x.getLabel().equalsIgnoreCase(feature.getLabel()) || x.getAbbv().equalsIgnoreCase(feature.getAbbreviation())) && !x.getUniqueId().equals(rf.getUniqueId()))
				throw new GwtRpcException(MESSAGES.errorRoomFeatureAlreadyExists(feature.getLabel(), SessionDAO.getInstance().get(sessionId).getLabel()));
		}
		
		if (rf instanceof DepartmentRoomFeature) {
			for (Iterator i=RoomFeature.getAllDepartmentRoomFeatures(d).iterator();i.hasNext();) {
				RoomFeature x = (RoomFeature)i.next();
				if ((x.getLabel().equalsIgnoreCase(feature.getLabel()) || x.getAbbv().equalsIgnoreCase(feature.getAbbreviation())) && !x.getUniqueId().equals(rf.getUniqueId()))
					throw new GwtRpcException(MESSAGES.errorRoomFeatureAlreadyExists(feature.getLabel(), d.getSession().getLabel()));
			}
		}
    	
    	rf.setAbbv(feature.getAbbreviation());
    	rf.setLabel(feature.getLabel());
    	rf.setFeatureType(feature.getType() == null ? null : RoomFeatureTypeDAO.getInstance().get(feature.getType().getId(), hibSession));
    	rf.setDescription(feature.getDescription());

    	hibSession.saveOrUpdate(rf);
    	
    	if (add != null && !add.isEmpty())
			for (Location location: lookupLocations(hibSession, add, future, sessionId)) {
				rf.getRooms().add(location);
				location.getFeatures().add(rf);
				hibSession.saveOrUpdate(location);
			}

    	if (drop != null && !drop.isEmpty())
			for (Location location: lookupLocations(hibSession, drop, future, sessionId)) {
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
                (feature.getId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE),
                null, 
                rf instanceof DepartmentRoomFeature ? ((DepartmentRoomFeature)rf).getDepartment() : null);
        
        return rf;
	}
	
	protected boolean dropFeature(Long featureId, Long sessionId, org.hibernate.Session hibSession, SessionContext context, boolean future) {
		RoomFeature rf = lookupFeature(hibSession, featureId, future, sessionId);
		if (rf == null) {
			if (!future) throw new GwtRpcException(MESSAGES.errorRoomFeatureDoesNotExist(featureId));
			return false;
		}
		
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
        
        for (RoomFeaturePref p: (List<RoomFeaturePref>)hibSession.createQuery("from RoomFeaturePref p where p.roomFeature.uniqueId = :id").setLong("id", rf.getUniqueId()).list()) {
				p.getOwner().getPreferences().remove(p);
				hibSession.delete(p);
				hibSession.saveOrUpdate(p.getOwner());
			}
        
        hibSession.delete(rf);
        return true;
	}

}
