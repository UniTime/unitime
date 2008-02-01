/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.SpecialUseRoomForm;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.model.ExternalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 05-05-2006
 * 
 * XDoclet definition:
 * @struts.action path="/addSpecialUseRoom" name="specialUseRoomForm" input="/admin/addSpecialUseRoom.jsp" scope="request" validate="true"
 */
public class AddSpecialUseRoomAction extends Action {

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

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
		
		if (specialUseRoomForm.getDoit() != null) {
			String doit = specialUseRoomForm.getDoit();
			if (doit.equals(rsc.getMessage("button.returnToRoomList"))) {
				return mapping.findForward("showRoomList");
			}
			if (doit.equals(rsc.getMessage("button.addNew"))) {
	            // Validate input prefs
	            errors = specialUseRoomForm.validate(mapping, request);
	            
	            // No errors
	            if(errors.size()==0) {
	            	String forward = update(request, specialUseRoomForm, mapping);
	            	if (forward != null) {
	            		return mapping.findForward(forward);
	            	}
	            }
	            else {
	            	setBldgs(request);
	                saveErrors(request, errors);
	            }
				
			}
		}			

		setBldgs(request);
		setDepts(request, specialUseRoomForm);
		
		//set default department based on user selection or department that user owns
		HttpSession httpSession = request.getSession();
		User user = Web.getUser(httpSession);
		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();	
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager manager = tdao.get(new Long(mgrId));
        Set departments = manager.departmentsForSession(sessionId);
        if (!isAdmin && (departments.size() == 1)) {
        	Department d = (Department) departments.iterator().next();
        	specialUseRoomForm.setDeptCode(d.getDeptCode());
        } else if (httpSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null) {
        	specialUseRoomForm.setDeptCode(httpSession.getAttribute(
					Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
		} 
		
		return mapping.findForward("showAdd");
	}

	/**
	 * 
	 * @param request
	 * @param specialUseRoomForm 
	 * @throws Exception
	 */
	private void setDepts(HttpServletRequest request, SpecialUseRoomForm specialUseRoomForm) throws Exception {
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager manager = tdao.get(new Long(mgrId));  
        Set departments = new TreeSet();
        if (user.getRole().equals(Roles.ADMIN_ROLE)) {
        	departments = Department.findAllBeingUsed(sessionId);
		} else {
			departments = manager.departmentsForSession(sessionId);
		}
		//Set departments = new TreeSet(manager.departmentsForSession(sessionId));
		specialUseRoomForm.setDeptSize(departments.size());
		
        ArrayList list = new ArrayList();
        int i = 0;
        for (Iterator iter = departments.iterator(); iter.hasNext();) {
        	Department dept = (Department) iter.next();
        	if (!dept.isEditableBy(user)) continue;
        	list.add(new LabelValueBean( dept.getDeptCode() + " - " +
        			dept.getName(), dept.getDeptCode())); 
        	if (i == 0) {
        		specialUseRoomForm.setDeptCode(dept.getDeptCode());
        		i++;
        	}
        }
        
        request.setAttribute(Department.DEPT_ATTR_NAME, list);
        
        //set default department
        if (!isAdmin && (departments.size() == 1)) {
        	Department d = (Department) departments.iterator().next();
        	specialUseRoomForm.setDeptCode(d.getDeptCode());
        } else if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null) {
        	specialUseRoomForm.setDeptCode(webSession.getAttribute(
					Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
		}
		
	}

	/**
	 * 
	 * @param request
	 * @throws Exception
	 */
	private void setBldgs(HttpServletRequest request) throws Exception {
		//get depts owned by user
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		String depts[] = getDepts(user);
		Collection bldgs = Session.getCurrentAcadSession(user).getBldgsFast(depts);	
		ArrayList list = new ArrayList();

		for (Iterator iter = bldgs.iterator(); iter.hasNext();) {
			Building b = (Building) iter.next();
			list.add(new LabelValueBean(
					b.getAbbreviation() + "-" + b.getName(), 
					b.getUniqueId() + "-" + b.getAbbreviation()));
		}
			
		request.setAttribute(Building.BLDG_LIST_ATTR_NAME, list);
	}

	/**
	 * 
	 * @param request
	 * @param specialUseRoomForm
	 * @param mapping
	 * @return
	 * @throws Exception
	 */
	private String update(
			HttpServletRequest request, 
			SpecialUseRoomForm specialUseRoomForm, 
			ActionMapping mapping) throws Exception {

		ActionMessages errors = new ActionMessages();
		
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
	    String bldgUniqueId = specialUseRoomForm.getBldgId().split("-")[0];
	    String bldgAbbv = specialUseRoomForm.getBldgId().split("-")[1];
	    String roomNum = specialUseRoomForm.getRoomNum().trim();	
	    
	    //check if room already exists
		Collection rooms = Session.getCurrentAcadSession(user).getRoomsFast((String[])null);	
		for (Iterator iter = rooms.iterator(); iter.hasNext();) {
			Location location = (Location) iter.next();
			if (location instanceof Room) {
				if (location.getSession().getUniqueId().equals(sessionId) && location.getLabel().trim().equalsIgnoreCase(bldgAbbv + " " + roomNum)) {	
					errors.add("specialUseRoom", 
		                      new ActionMessage("errors.exists", "Room "));
					saveErrors(request, errors);
					return null;
				}
			}
		}
	        
	    //get room
		ExternalBuilding extBldg = ExternalBuilding.findByAbbv(sessionId, bldgAbbv);
		ExternalRoom extRoom = null;
		if(extBldg != null) {
			extRoom = extBldg.findRoom(roomNum);
		}
		if(extRoom == null) {
			errors.add("specialUseRoom", 
                    new ActionMessage("errors.invalid", "Room number "));
			saveErrors(request, errors);
			return null;
		}
		
		//check ownership of the room
		String depts[] = getDepts(user);
		boolean owned = false;
		if (depts != null) {
			Iterator d = extRoom.getRoomDepartments().iterator();
			while(d.hasNext()) {
				ExternalRoomDepartment roomDept = (ExternalRoomDepartment)d.next();
				if(roomDept.isAssigned()) {
					if(Constants.arrayToStr(depts, "", ",").indexOf(roomDept.getDepartmentCode()) >= 0) {
						owned = true;
						break;
					}
				}
			}
		} else {
			owned = true;
		}
		if(!owned) {
			errors.add("specialUseRoom", 
                      new ActionMessage( "errors.room.ownership"));
			saveErrors(request, errors);
			return null;
		}
		
		org.hibernate.Session hibSession = (new RoomDAO()).getSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			Room room = new Room();

			room.setSession(Session.getCurrentAcadSession(user));
			room.setIgnoreTooFar(Boolean.FALSE);
			room.setIgnoreRoomCheck(Boolean.FALSE);
			room.setCoordinateX(extRoom.getCoordinateX());
			room.setCoordinateY(extRoom.getCoordinateY());
			room.setCapacity(extRoom.getCapacity());
			room.setExamCapacity(0);
			room.setExamEnabled(Boolean.FALSE);
			room.setRoomNumber(roomNum);
			room.setScheduledRoomType("specialUse");
			room.setExternalUniqueId(extRoom.getExternalUniqueId());
			room.setClassification(extRoom.getClassification());
			room.setDisplayName(extRoom.getDisplayName());
						
			String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
			room.setManagerIds(mgrId);
				        
			BuildingDAO bldgDAO = new BuildingDAO();
			Building bldg = bldgDAO.get(Long.valueOf(bldgUniqueId));
			room.setBuildingAbbv(bldgAbbv);
			room.setBuilding(bldg);

			room.setFeatures(new HashSet());
			room.setAssignments(new HashSet());
			room.setRoomGroups(new HashSet());
			room.setRoomDepts(new HashSet());

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

            ChangeLog.addChange(hibSession, request, (Location)room, 
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
		Collection globalRoomFeatures = RoomFeature.getAllGlobalRoomFeatures();
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

	/**
	 * 
	 * @param user
	 * @return
	 */
	private String[] getDepts(User user) throws Exception {

		String[] depts = new String[] {};

		if(user.getRole().equals(Roles.ADMIN_ROLE)) {
			depts = null;
		} else {
			Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		
			String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
			TimetableManagerDAO tdao = new TimetableManagerDAO();
			TimetableManager manager = tdao.get(new Long(mgrId));
		
			Set departments = manager.departmentsForSession(sessionId);
			if (departments!=null) {
				depts = new String[departments.size()];
				int idx = 0;
				for (Iterator i=departments.iterator();i.hasNext();) {
					depts[idx++] = ((Department)i.next()).getDeptCode();
				}
			}
		}
		
		return depts;
	}

}

