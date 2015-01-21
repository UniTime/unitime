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
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.SpecialUseRoomForm;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LocationPermIdGenerator;


/** 
 * MyEclipse Struts
 * Creation date: 05-05-2006
 * 
 * XDoclet definition:
 * @struts.action path="/addSpecialUseRoom" name="specialUseRoomForm" input="/admin/addSpecialUseRoom.jsp" scope="request" validate="true"
 *
 * @author Tomas Muller
 */
@Service("/addSpecialUseRoom")
public class AddSpecialUseRoomAction extends Action {

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	@Autowired SessionContext sessionContext;
	
	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws Exception 
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		SpecialUseRoomForm specialUseRoomForm = (SpecialUseRoomForm) form;
		MessageResources rsc = getResources(request);
		ActionMessages errors = new ActionMessages();
		
		sessionContext.checkPermission(Right.AddSpecialUseRoom);
		
		Set<Department> departments = Department.getUserDepartments(sessionContext.getUser());
		List<Building> buildings = Building.findAll(sessionContext.getUser().getCurrentAcademicSessionId());

		if (specialUseRoomForm.getDoit() != null) {
			String doit = specialUseRoomForm.getDoit();
			if (doit.equals(rsc.getMessage("button.returnToRoomList"))) {
				return mapping.findForward("showRoomList");
			}
			if (doit.equals(rsc.getMessage("button.addNew"))) {
	            // Validate input prefs
	            errors = specialUseRoomForm.validate(mapping, request);
	            
	            // No errors
	            if (errors.isEmpty()) {
	            	String forward = update(request, specialUseRoomForm);
	            	if (forward != null)
	            		return mapping.findForward(forward);
	            } else {
	                saveErrors(request, errors);
	            }
				
			}
		}			

		setup(request, departments, buildings);

        //set default department
        specialUseRoomForm.setDeptSize(departments.size());
        if (departments.size() == 1) {
        	Department d = departments.iterator().next();
        	specialUseRoomForm.setDeptCode(d.getDeptCode());
        } else if (sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom) != null) {
        	specialUseRoomForm.setDeptCode((String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom));
		}

		return mapping.findForward("showAdd");
	}

	/**
	 * 
	 * @param request
	 * @throws Exception
	 */
	private void setup(HttpServletRequest request, Set<Department> departments, List<Building> buildings) throws Exception {
		List<LabelValueBean> deptList = new ArrayList<LabelValueBean>();
		for (Department d: departments) {
			String code = d.getDeptCode().trim();
			String abbv = d.getName().trim();
			deptList.add(new LabelValueBean(code + " - " + abbv, code)); 
		}
		request.setAttribute(Department.DEPT_ATTR_NAME, deptList);
		
		List<LabelValueBean> bldgList = new ArrayList<LabelValueBean>();
		for (Building b: buildings) {
			bldgList.add(new LabelValueBean(
					b.getAbbreviation() + "-" + b.getName(), 
					b.getUniqueId() + "-" + b.getAbbreviation()));
		}
		request.setAttribute(Building.BLDG_LIST_ATTR_NAME, bldgList);
	}

	/**
	 * 
	 * @param request
	 * @param specialUseRoomForm
	 * @param mapping
	 * @return
	 * @throws Exception
	 */
	private String update(HttpServletRequest request, SpecialUseRoomForm specialUseRoomForm) throws Exception {

		ActionMessages errors = new ActionMessages();
		
		Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
	    Long bldgUniqueId = Long.valueOf(specialUseRoomForm.getBldgId().split("-")[0]);
	    String bldgAbbv = specialUseRoomForm.getBldgId().split("-")[1];
	    String roomNum = specialUseRoomForm.getRoomNum().trim();	
	    
	    //check if room already exists
	    Room existingRoom = Room.findByBldgIdRoomNbr(bldgUniqueId, roomNum, sessionId);
	    if (existingRoom != null) {
	    	errors.add("specialUseRoom", new ActionMessage("errors.exists", "Room "));
	    	saveErrors(request, errors);
	    	return null;
	    }
	    
	    //get room
		ExternalBuilding extBldg = ExternalBuilding.findByAbbv(sessionId, bldgAbbv);
		ExternalRoom extRoom = null;
		if(extBldg != null)
			extRoom = extBldg.findRoom(roomNum);
		if(extRoom == null) {
			errors.add("specialUseRoom", new ActionMessage("errors.invalid", "Room number "));
			saveErrors(request, errors);
			return null;
		}
		
		if (!sessionContext.hasPermission(extRoom, Right.AddSpecialUseRoomExternalRoom)) {
			errors.add("specialUseRoom", new ActionMessage( "errors.room.ownership"));
			saveErrors(request, errors);
			return null;
		}

		org.hibernate.Session hibSession = (new RoomDAO()).getSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			Room room = new Room();

			room.setSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId(), hibSession));
			room.setIgnoreTooFar(Boolean.FALSE);
			room.setIgnoreRoomCheck(Boolean.FALSE);
			room.setCoordinateX(extRoom.getCoordinateX());
			room.setCoordinateY(extRoom.getCoordinateY());
			room.setArea(extRoom.getArea());
			room.setCapacity(extRoom.getCapacity());
			room.setExamCapacity(0);
			room.setRoomNumber(roomNum);
			room.setRoomType(extRoom.getRoomType());
			room.setExternalUniqueId(extRoom.getExternalUniqueId());
			room.setClassification(extRoom.getClassification());
			room.setDisplayName(extRoom.getDisplayName());
			
			BuildingDAO bldgDAO = new BuildingDAO();
			Building bldg = bldgDAO.get(Long.valueOf(bldgUniqueId));
			room.setBuildingAbbv(bldgAbbv);
			room.setBuilding(bldg);

			room.setFeatures(new HashSet());
			room.setAssignments(new HashSet());
			room.setRoomGroups(new HashSet());
			room.setRoomDepts(new HashSet());
			
			LocationPermIdGenerator.setPermanentId(room);

			hibSession.saveOrUpdate(room);
			
			Set extRoomFeatures = extRoom.getRoomFeatures();
			if(!extRoomFeatures.isEmpty()) {
				addRoomFeatures(extRoomFeatures, room, hibSession);
				hibSession.saveOrUpdate(room);
			}
			
			Department dept = null;
			if (specialUseRoomForm.getDeptCode()!= null 
					&& specialUseRoomForm.getDeptCode().length() > 0) {
				String deptSelected = specialUseRoomForm.getDeptCode();
				RoomDept roomdept = new RoomDept();
				roomdept.setRoom(room);
				roomdept.setControl(Boolean.TRUE);
				dept = Department.findByDeptCode(deptSelected, sessionId);
				roomdept.setDepartment(dept);
				hibSession.saveOrUpdate(roomdept);
			}			

            ChangeLog.addChange(hibSession, sessionContext, (Location)room, 
                ChangeLog.Source.ROOM_EDIT, ChangeLog.Operation.CREATE, null, dept);

            tx.commit();
				
			if (dept != null) {
				hibSession.refresh(dept);
			}
			hibSession.refresh(room);
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
				throw e;
		}
			
		return ("showRoomList");
	}
	
	/**
	 * Add room features
	 * @param extRoomFeatures
	 * @param room
	 */
	private void addRoomFeatures(Set extRoomFeatures, Room room, 
			org.hibernate.Session hibSession) {

		Set roomFeatures = room.getFeatures();
		Iterator f = extRoomFeatures.iterator();
		Collection globalRoomFeatures = RoomFeature.getAllGlobalRoomFeatures(room.getSession());
		while(f.hasNext()) {
			ExternalRoomFeature extRoomFeature = (ExternalRoomFeature)f.next();
			String featureValue = extRoomFeature.getValue();
			Iterator g = globalRoomFeatures.iterator();
			while(g.hasNext()) {
				RoomFeature globalFeature = (RoomFeature)g.next();
				if(globalFeature.getLabel().equalsIgnoreCase(featureValue)) {
					globalFeature.getRooms().add((Location)room);
					hibSession.save(globalFeature);
					roomFeatures.add(globalFeature);
					break;
				}
			}
		}
		
		room.setFeatures(roomFeatures);
		
		return;
	}

}

