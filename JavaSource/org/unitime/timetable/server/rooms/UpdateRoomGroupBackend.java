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
            	
            	if (request.hasFutureSessions())
            		for (Long id: request.getFutureSessions())
            			createOrUpdateGroup(request.getGroup(), request.getAddLocations(), request.getDropLocations(), id, hibSession, new EventContext(context, context.getUser(), id), true);
            	createOrUpdateGroup(request.getGroup(), request.getAddLocations(), request.getDropLocations(), context.getUser().getCurrentAcademicSessionId(), hibSession, context, false);

            } else if (request.getDeleteGroupId() != null) {
            	
            	if (request.hasFutureSessions())
            		for (Long id: request.getFutureSessions())
            			dropGroup(request.getDeleteGroupId(), id, hibSession, new EventContext(context, context.getUser(), id), true);
            	dropGroup(request.getDeleteGroupId(), context.getUser().getCurrentAcademicSessionId(), hibSession, context, false);

            } else {
            	throw new GwtRpcException("Bad request.");
            }
            
            tx.commit();
        	return null;
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
	
	protected RoomGroup lookupGroup(org.hibernate.Session hibSession, GroupInterface original, boolean future, Long sessionId) {
		if (original == null) return null;
		if (future) {
			if (original.isDepartmental())
				return (RoomGroup)hibSession.createQuery(
					"select g from RoomGroup g, RoomGroup o where o.uniqueId = :originalId and g.department.session.uniqueId = :sessionId " +
					"and g.abbv = o.abbv and g.department.deptCode = o.department.deptCode and g.global = false")
					.setLong("sessionId", sessionId).setLong("originalId", original.getId()).setCacheable(true).setMaxResults(1).uniqueResult();
			else
				return (RoomGroup)hibSession.createQuery(
					"select g from RoomGroup g, RoomGroup o where o.uniqueId = :originalId and g.session.uniqueId = :sessionId " +
					"and g.abbv = o.abbv and g.global = true")
					.setLong("sessionId", sessionId).setLong("originalId", original.getId()).setCacheable(true).setMaxResults(1).uniqueResult();
		} else {
			return RoomGroupDAO.getInstance().get(original.getId(), hibSession);
		}
	}
	
	protected RoomGroup lookupGroup(org.hibernate.Session hibSession, Long groupId, boolean future, Long sessionId) {
		if (groupId == null) return null;
		if (future) {
			RoomGroup group = (RoomGroup)hibSession.createQuery(
					"select g from RoomGroup g, RoomGroup o where o.uniqueId = :originalId and g.department.session.uniqueId = :sessionId " +
					"and g.abbv = o.abbv and g.department.deptCode = o.department.deptCode and g.global = false")
					.setLong("sessionId", sessionId).setLong("originalId", groupId).setCacheable(true).setMaxResults(1).uniqueResult();
			if (group == null)
				group = (RoomGroup)hibSession.createQuery(
					"select g from RoomGroup g, RoomGroup o where o.uniqueId = :originalId and g.session.uniqueId = :sessionId " +
					"and g.abbv = o.abbv and g.global = true")
					.setLong("sessionId", sessionId).setLong("originalId", groupId).setCacheable(true).setMaxResults(1).uniqueResult();
			return group;
		} else {
			return RoomGroupDAO.getInstance().get(groupId, hibSession);
		}
	}
	
	protected List<Location> lookupLocations(org.hibernate.Session hibSession, List<Long> ids, boolean future, Long sessionId) {
		if (ids == null || ids.isEmpty()) return new ArrayList<Location>();
		if (future) {
			return (List<Location>)hibSession.createQuery(
					"select l from Location l, Location o where o.uniqueId in :ids and l.session.uniqueId = :sessionId and l.permanentId = o.permanentId")
					.setParameterList("ids", ids).setLong("sessionId", sessionId).list();
		} else {
			return (List<Location>)hibSession.createQuery("from Location where uniqueId in :ids").setParameterList("ids", ids).list();
		}
	}
	
	protected RoomGroup createOrUpdateGroup(GroupInterface group, List<Long> add, List<Long> drop, Long sessionId, org.hibernate.Session hibSession, SessionContext context, boolean future) {
		Department d = group.isDepartmental() ? lookuDepartment(hibSession, group.getDepartment(), future, sessionId) : null;
		if (group.isDepartmental() && d == null) return null;
		
    	RoomGroup rg = (group.getId() == null ? null : lookupGroup(hibSession, group, future, sessionId));
    	if (rg == null) {
    		if (!future && group.getId() != null)
    			throw new GwtRpcException(MESSAGES.errorRoomGroupDoesNotExist(group.getId()));

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
    		if (rg.isGlobal()) {
    			context.checkPermission(rg, Right.GlobalRoomGroupEdit);
    		} else {
    			context.checkPermission(rg, Right.DepartmenalRoomGroupEdit);
    			rg.setDepartment(d);
    		}
    	}
    	
		for (Iterator i = RoomGroup.getAllGlobalRoomGroups(sessionId).iterator();i.hasNext();) {
			RoomGroup x = (RoomGroup)i.next();
			if ((x.getName().equalsIgnoreCase(group.getLabel()) || x.getAbbv().equalsIgnoreCase(group.getAbbreviation())) && !x.getUniqueId().equals(rg.getUniqueId()))
				throw new GwtRpcException(MESSAGES.errorRoomGroupAlreadyExists(group.getLabel(), rg.getSession().getLabel()));
		}
		
		if (rg.getDepartment() != null) {
			for (Iterator i=RoomGroup.getAllDepartmentRoomGroups(rg.getDepartment()).iterator();i.hasNext();) {
				RoomGroup x = (RoomGroup)i.next();
				if ((x.getName().equalsIgnoreCase(group.getLabel()) || x.getAbbv().equalsIgnoreCase(group.getAbbreviation())) && !x.getUniqueId().equals(rg.getUniqueId()))
					throw new GwtRpcException(MESSAGES.errorRoomGroupAlreadyExists(group.getLabel(), rg.getDepartment().getSession().getLabel()));
			}
		}
    	
    	rg.setAbbv(group.getAbbreviation());
    	rg.setName(group.getLabel());
    	rg.setDescription(group.getDescription());

    	if (rg.isGlobal() && group.isDefault() && context.hasPermission(rg, Right.GlobalRoomGroupEditSetDefault)) {
    		for (RoomGroup x: RoomGroup.getAllRoomGroupsForSession(rg.getSession())) {
    			if (!x.getUniqueId().equals(rg.getUniqueId()) && x.isDefaultGroup()) {
    				x.setDefaultGroup(false);
    				hibSession.saveOrUpdate(x);
    			}
    		}
    	}
    	rg.setDefaultGroup(group.isDefault() && rg.isGlobal());

    	hibSession.saveOrUpdate(rg);
    	
    	if (add != null && !add.isEmpty())
			for (Location location: lookupLocations(hibSession, add, future, sessionId)) {
				rg.getRooms().add(location);
				location.getRoomGroups().add(rg);
				hibSession.saveOrUpdate(location);
			}

    	if (drop != null && !drop.isEmpty())
			for (Location location: lookupLocations(hibSession, drop, future, sessionId)) {
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
                (group.getId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE),
                null, 
                rg.getDepartment());
        
        return rg;
	}
	
	protected boolean dropGroup(Long groupId, Long sessionId, org.hibernate.Session hibSession, SessionContext context, boolean future) {
    	RoomGroup rg = lookupGroup(hibSession, groupId, future, sessionId);
    	if (rg == null) {
    		if (!future) throw new GwtRpcException(MESSAGES.errorRoomGroupDoesNotExist(groupId));
    		return false;
    	}
		
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
					.setLong("id", rg.getUniqueId()).list()) {
				p.getOwner().getPreferences().remove(p);
				hibSession.delete(p);
				hibSession.saveOrUpdate(p.getOwner());
			}
        
        hibSession.delete(rg);
        return true;
	}
}
