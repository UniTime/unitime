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
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.form.EditRoomGroupForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.action path="/editRoomGroup" name="editRoomGroupForm" input="/admin/editRoomGroup.jsp" scope="request"
 * @struts.action-forward name="showRoomDetail" path="/roomDetail.do"
 *
 * @author Tomas Muller
 */
@Service("/editRoomGroup")
public class EditRoomGroupAction extends Action {
	
	@Autowired SessionContext sessionContext;

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception{
		EditRoomGroupForm editRoomGroupForm = (EditRoomGroupForm) form;		
		
		MessageResources rsc = getResources(request);
		String doit = editRoomGroupForm.getDoit();

		//return to room list
		if(doit!= null && doit.equals(rsc.getMessage("button.returnToRoomDetail"))) {
			response.sendRedirect("roomDetail.do?id="+editRoomGroupForm.getId());
			return null;
			//the following call cannot be used since doit is has the same value as for return to room list (Back)
			//return mapping.findForward("showRoomDetail");
		}
		
		//update location
		if(doit != null && doit.equals(rsc.getMessage("button.update"))) {
			doUpdate(editRoomGroupForm,request);
			return mapping.findForward("showRoomDetail");
		}	
		
		//get location information
		Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		
		if (location instanceof Room) {
			Room r = (Room) location;
			editRoomGroupForm.setName(r.getLabel());
		} else if (location instanceof NonUniversityLocation) {
				NonUniversityLocation nonUnivLocation = (NonUniversityLocation) location;
				editRoomGroupForm.setName(nonUnivLocation.getName());
		} else {
			ActionMessages errors = new ActionMessages();
			errors.add("editRoomGroup", 
	                   new ActionMessage("errors.lookup.notFound", "Room Group") );
			saveErrors(request, errors);
		}
		
		sessionContext.checkPermission(location, Right.RoomEditGroups);
		
		boolean editGlobalGroups = sessionContext.hasPermission(location, Right.RoomEditGlobalGroups);
		for (RoomGroup rg: RoomGroup.getAllGlobalRoomGroups(location.getSession())) {
			editRoomGroupForm.addToGlobalRoomGroups(rg, editGlobalGroups, location.hasGroup(rg));
		}
		
		Set<Department> departments = Department.getUserDepartments(sessionContext.getUser());
		for (Department department: departments) {
			for (RoomGroup rg: RoomGroup.getAllDepartmentRoomGroups(department)) {
				editRoomGroupForm.addToMangaerRoomGroups(rg, true, location.hasGroup(rg));
			}
		}
		
		for (Department department: Department.findAllExternal(location.getSession().getUniqueId())) {
			if (departments.contains(department)) continue;
			for (RoomGroup rg: RoomGroup.getAllDepartmentRoomGroups(department)) {
				editRoomGroupForm.addToMangaerRoomGroups(rg, false, location.hasGroup(rg));
			}
		}

		return mapping.findForward("showEditRoomGroup");
	}

	/**
	 * 
	 * @param editRoomGroupForm
	 * @param request
	 * @throws Exception
	 */
	private void doUpdate(
			EditRoomGroupForm editRoomGroupForm, 
			HttpServletRequest request) throws Exception {

		org.hibernate.Session hibSession = LocationDAO.getInstance().getSession();
		
		Transaction tx = null;
		try{
			tx = hibSession.beginTransaction();
			
			Location location = LocationDAO.getInstance().get(Long.valueOf(request.getParameter("id")), hibSession);

			sessionContext.checkPermission(location, Right.RoomEditGroups);

			boolean editGlobalGroups = sessionContext.hasPermission(location, Right.RoomEditGlobalGroups);
			if (editGlobalGroups && editRoomGroupForm.getGlobalRoomGroupsAssigned() != null) {
				List globalSelected = editRoomGroupForm.getGlobalRoomGroupsAssigned();
				List globalRg = editRoomGroupForm.getGlobalRoomGroupIds();
				if (globalSelected.size() == 0) {
					for (Iterator iter = globalRg.iterator(); iter.hasNext();) {
						String rgId = (String)iter.next();
						RoomGroup rg = RoomGroupDAO.getInstance().get(Long.valueOf(rgId), hibSession);
						rg.getRooms().remove(location);
						location.getRoomGroups().remove(rg);
						hibSession.saveOrUpdate(rg);
					}
				} else {
					int i = 0;
					for (Iterator iter = globalRg.iterator(); iter.hasNext();){
						String rgId = (String) iter.next();	
						String selected = (String)globalSelected.get(i);
						RoomGroup rg = RoomGroupDAO.getInstance().get(Long.valueOf(rgId), hibSession);
						if (selected==null) continue;
						
						if (selected.equalsIgnoreCase("on") || selected.equalsIgnoreCase("true")) {
							if (!rg.hasLocation(location)) {
								rg.getRooms().add(location);
								location.getRoomGroups().add(rg);
							}
						} else {
							if (rg.hasLocation(location)) {
								rg.getRooms().remove(location);
								location.getRoomGroups().remove(rg);
							}
						}
						hibSession.saveOrUpdate(rg);
						i++;
					}
				}
			}
		
			Set<Department> departments = Department.getUserDepartments(sessionContext.getUser());
			if (!departments.isEmpty() && editRoomGroupForm.getManagerRoomGroupsAssigned() != null){
				List managerSelected = editRoomGroupForm.getManagerRoomGroupsAssigned();
				List managerRg = editRoomGroupForm.getManagerRoomGroupIds();
				
				if (managerSelected.size() == 0) {
					for (Iterator iter = managerRg.iterator(); iter.hasNext();) {
						String rgId = (String)iter.next();
						RoomGroup rg = RoomGroupDAO.getInstance().get(Long.valueOf(rgId), hibSession);
						if (rg.getDepartment() == null || !departments.contains(rg.getDepartment())) continue;
						rg.getRooms().remove(location);
						hibSession.saveOrUpdate(rg);
					}
				} else {	
					int i = 0;
					for (Iterator iter = managerRg.iterator(); iter.hasNext();){
						String rgId = (String) iter.next();	
						String selected = (String)managerSelected.get(i);
						if (selected==null) continue;

						RoomGroup rg = RoomGroupDAO.getInstance().get(Long.valueOf(rgId), hibSession);
						if (rg.getDepartment() == null || !departments.contains(rg.getDepartment())) continue;

						if (selected.equalsIgnoreCase("on") || selected.equalsIgnoreCase("true")) {
							if (!rg.hasLocation(location)) {
								rg.getRooms().add(location);
								location.getRoomGroups().add(rg);
							}
						} else {
							if (rg.hasLocation(location)) {
								rg.getRooms().remove(location);
								location.getRoomGroups().remove(rg);
							}
						}
						hibSession.saveOrUpdate(rg);
						i++;
					}
				}
			}
			
			hibSession.saveOrUpdate(location);
	        
	        ChangeLog.addChange(
	                hibSession, 
	                sessionContext, 
	                location, 
	                ChangeLog.Source.ROOM_GROUP_EDIT, 
	                ChangeLog.Operation.UPDATE, 
	                null, 
	                location.getControllingDepartment());
	        
			tx.commit();
			hibSession.refresh(location);

		} catch (Exception e) {
			Debug.error(e);
			try {
				if(tx!=null && tx.isActive())
					tx.rollback();
			}
		    catch (Exception e1) { }
		    throw e;
		}
	}
}

