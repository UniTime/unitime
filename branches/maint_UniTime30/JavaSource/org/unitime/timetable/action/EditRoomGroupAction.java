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
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EditRoomGroupForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.action path="/editRoomGroup" name="editRoomGroupForm" input="/admin/editRoomGroup.jsp" scope="request"
 * @struts.action-forward name="showRoomDetail" path="/roomDetail.do"
 */
public class EditRoomGroupAction extends Action {

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
		HttpSession webSession = request.getSession();
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}
		
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
			
		//get user information
		User user = Web.getUser(webSession);
		Long sessionId = org.unitime.timetable.model.Session.getCurrentAcadSession(user).getSessionId();
		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager owner = tdao.get(new Long(mgrId));
 	      
		//get groups
		ArrayList globalRoomGroups = new ArrayList();
		ArrayList managerRoomGroups = new ArrayList();
		
		org.hibernate.Session hibSession = null;
		try {
			RoomGroupDAO d = new RoomGroupDAO();
			hibSession = d.getSession();
			
			List list = hibSession
						.createCriteria(RoomGroup.class)
						.addOrder(Order.asc("name"))
						.list();
			
			for (Iterator iter = list.iterator();iter.hasNext();) {
				RoomGroup rg = (RoomGroup) iter.next();
				if (rg.isGlobal().booleanValue()) {
					globalRoomGroups.add(rg);
				} else {
					if (rg.getDepartment()==null) continue;
					if (!rg.getDepartment().getSessionId().equals(sessionId)) continue;
					Department dept = rg.getDepartment();
					for (Iterator i2=location.getRoomDepts().iterator();i2.hasNext();) {
						RoomDept rd = (RoomDept)i2.next();
						if (dept.getUniqueId().equals(rd.getDepartment().getUniqueId())) {
							managerRoomGroups.add(rg); break;
						}
					}
					
				}
			}
			
		} catch (Exception e) {
			Debug.error(e);
		}
		
		for (Iterator iter = globalRoomGroups.iterator(); iter.hasNext();) {
			RoomGroup grg = (RoomGroup) iter.next();
			if (!editRoomGroupForm.getGlobalRoomGroupIds().contains(grg.getUniqueId().toString())) {
				if (user.getRole().equals(Roles.ADMIN_ROLE)) {
					editRoomGroupForm.addToGlobalRoomGroups(grg,Boolean.TRUE, Boolean.valueOf(location.hasGroup(grg)));
				} else {
					editRoomGroupForm.addToGlobalRoomGroups(grg,Boolean.FALSE,Boolean.valueOf(location.hasGroup(grg)));
				}
			}
		}
		
		for (Iterator iter = managerRoomGroups.iterator();iter.hasNext();) {
			RoomGroup mrg = (RoomGroup) iter.next();
			if (!editRoomGroupForm.getManagerRoomGroupIds().contains(mrg.getUniqueId().toString())){
				if (user.getRole().equals(Roles.ADMIN_ROLE) || (mrg.getDepartment() != null && owner.getDepartments().contains(mrg.getDepartment()))) {
					editRoomGroupForm.addToMangaerRoomGroups(mrg, Boolean.TRUE, Boolean.valueOf(location.hasGroup(mrg)));
				} else {
					editRoomGroupForm.addToMangaerRoomGroups(mrg, Boolean.FALSE, Boolean.valueOf(location.hasGroup(mrg)));
				}
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
		
		//get location information
		Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		Collection groups = location.getRoomGroups();
		RoomGroupDAO rgdao = new RoomGroupDAO();
		Set rgs = new HashSet();
		
		//update room features
		Session hibSession = ldao.getSession();
		Transaction tx = null;
		try{
			tx = hibSession.beginTransaction();
			
		if (editRoomGroupForm.getGlobalRoomGroupsAssigned() != null) {
			List globalSelected = editRoomGroupForm.getGlobalRoomGroupsAssigned();
			List globalRg = editRoomGroupForm.getGlobalRoomGroupIds();
			
			if (globalSelected.size() == 0) {
				for (Iterator iter = globalRg.iterator(); iter.hasNext();) {
					String rgId = (String)iter.next();
					RoomGroup rg = rgdao.get(Long.valueOf(rgId));
					rg.getRooms().remove(location);
					hibSession.saveOrUpdate(rg);
				}
			} else {
				int i = 0;
				for (Iterator iter = globalRg.iterator(); iter.hasNext();){
					String rgId = (String) iter.next();	
					String selected = (String)globalSelected.get(i);
					RoomGroup rg = rgdao.get(Long.valueOf(rgId));
					if (selected==null) continue;
					
					if (selected.equalsIgnoreCase("on") || selected.equalsIgnoreCase("true")) {
						rgs.add(rg);
						if (!rg.hasLocation(location)) {
							rg.getRooms().add(location);
						}
					} else {
						if (rg.hasLocation(location)) {
							rg.getRooms().remove(location);
						}
					}
					hibSession.saveOrUpdate(rg);
					i++;
				}
			}
		}
		
		if (editRoomGroupForm.getManagerRoomGroupsAssigned() != null){
			List managerSelected = editRoomGroupForm.getManagerRoomGroupsAssigned();
			List managerRg = editRoomGroupForm.getManagerRoomGroupIds();
			
			if (managerSelected.size() == 0) {
				for (Iterator iter = managerRg.iterator(); iter.hasNext();) {
					String rgId = (String)iter.next();
					RoomGroup rg = rgdao.get(Long.valueOf(rgId));
					rg.getRooms().remove(location);
					hibSession.saveOrUpdate(rg);
				}
			} else {	
				int i = 0;
				for (Iterator iter = managerRg.iterator(); iter.hasNext();){
					String rgId = (String) iter.next();	
					String selected = (String)managerSelected.get(i);
					RoomGroup rg = rgdao.get(Long.valueOf(rgId));
					if (selected==null) continue;
					
					if (selected.equalsIgnoreCase("on") || selected.equalsIgnoreCase("true")) {
						rgs.add(rg);
						if (!rg.hasLocation(location)) {
							rg.getRooms().add(location);
						}
					} else {
						if (rg.hasLocation(location)) {
							rg.getRooms().remove(location);
						}
					}
					hibSession.saveOrUpdate(rg);
					i++;
				}
			}
		}
		
		location.setRoomGroups(rgs);
		hibSession.saveOrUpdate(location);
        
        ChangeLog.addChange(
                hibSession, 
                request, 
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

