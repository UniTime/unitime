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

import org.hibernate.Transaction;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.RoomInterface.UpdateRoomDepartmentsRequest;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.RoomDeptDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(UpdateRoomDepartmentsRequest.class)
public class UpdateRoomDepartmentsBackend implements GwtRpcImplementation<UpdateRoomDepartmentsRequest, GwtRpcResponseNull>{

	@Override
	public GwtRpcResponseNull execute(UpdateRoomDepartmentsRequest request, SessionContext context) {
		Transaction tx = null;
        try {
            org.hibernate.Session hibSession = new RoomDeptDAO().getSession();
            tx = hibSession.beginTransaction();

    		if (request.hasDepartment()) {
    			Department department = DepartmentDAO.getInstance().get(request.getDepartment().getId(), hibSession);
    			context.checkPermission(department, Right.EditRoomDepartments);
    			if (request.hasAddLocations())
    				for (Location location: (List<Location>)hibSession.createQuery("from Location where uniqueId in :ids").setParameterList("ids", request.getAddLocations()).list()) {
    					RoomDept rd = null;
                        for (Iterator j = location.getRoomDepts().iterator(); rd==null && j.hasNext(); ) {
                            RoomDept x = (RoomDept)j.next();
                            if (x.getDepartment().equals(department)) rd = x;
                        }
                        if (rd != null) continue;
    					rd = new RoomDept();
                        rd.setDepartment(department);
                        rd.setRoom(location);
                        rd.setControl(Boolean.FALSE);
                        department.getRoomDepts().add(rd);
                        location.getRoomDepts().add(rd);
                        hibSession.saveOrUpdate(location);
                        hibSession.saveOrUpdate(rd);
    					hibSession.saveOrUpdate(location);
    					ChangeLog.addChange(hibSession, context, location, ChangeLog.Source.ROOM_DEPT_EDIT, ChangeLog.Operation.CREATE, null, department);
    				}
    			if (request.hasDropLocations())
    				for (Location location: (List<Location>)hibSession.createQuery("from Location where uniqueId in :ids").setParameterList("ids", request.getDropLocations()).list()) {
    					RoomDept rd = null;
                        for (Iterator j = location.getRoomDepts().iterator(); rd==null && j.hasNext(); ) {
                            RoomDept x = (RoomDept)j.next();
                            if (x.getDepartment().equals(department)) rd = x;
                        }
                        ChangeLog.addChange(hibSession, context, location, ChangeLog.Source.ROOM_DEPT_EDIT, ChangeLog.Operation.DELETE, null, department);
                        department.getRoomDepts().remove(rd);
                        location.getRoomDepts().remove(rd);
                        hibSession.saveOrUpdate(location);
                        hibSession.delete(rd);
                        location.removedFromDepartment(department, hibSession);
    				}
    			hibSession.saveOrUpdate(department);
    		} else if (request.hasExamType()) {
    			context.checkPermission(Right.EditRoomDepartmentsExams);
    			ExamType type = ExamTypeDAO.getInstance().get(request.getExamType().getId(), hibSession);
    			if (request.hasAddLocations())
    				for (Location location: (List<Location>)hibSession.createQuery("from Location where uniqueId in :ids").setParameterList("ids", request.getAddLocations()).list()) {
    					location.setExamEnabled(type, true);
    					hibSession.saveOrUpdate(location);
    					ChangeLog.addChange(hibSession, context, location, ChangeLog.Source.ROOM_DEPT_EDIT, ChangeLog.Operation.UPDATE, null, null);
    				}
    			if (request.hasDropLocations())
    				for (Location location: (List<Location>)hibSession.createQuery("from Location where uniqueId in :ids").setParameterList("ids", request.getDropLocations()).list()) {
    					location.setExamEnabled(type, false);
    					hibSession.saveOrUpdate(location);
    					ChangeLog.addChange(hibSession, context, location, ChangeLog.Source.ROOM_DEPT_EDIT, ChangeLog.Operation.UPDATE, null, null);
    				}
    		}

            tx.commit();
            return new GwtRpcResponseNull();
        } catch (Exception e) {
        	e.printStackTrace();
            if (tx != null) tx.rollback();
            if (e instanceof GwtRpcException) throw (GwtRpcException) e;
            throw new GwtRpcException(e.getMessage());
        }
	}
}
