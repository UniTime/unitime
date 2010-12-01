/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.LookupDispatchAction;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.RoomGroupEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 05-02-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomGroupEdit" name="roomGroupEditForm" input="/admin/roomGroupEdit.jsp" parameter="doit" scope="request" validate="true"
 * @struts.action-forward name="showRoomGroupList" path="/roomGroupList.do"
 * @struts.action-forward name="showEdit" path="roomGroupEditTile"
 * @struts.action-forward name="showAdd" path="roomGroupEditTile"
 */
public class RoomGroupEditAction extends LookupDispatchAction {

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods
	/**
	 * 
	 * @return
	 */
	protected Map getKeyMethodMap() {
	      Map map = new HashMap();
	      map.put("editRoomGroup", "editRoomGroup");
	      map.put("button.delete", "deleteRoomGroup");
	      map.put("button.update", "saveRoomGroup");
	      map.put("button.addNew", "saveRoomGroup");
	      map.put("button.returnToRoomGroupList", "cancelRoomGroup");
	      return map;
	  }

	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws HibernateException
	 * @throws Exception
	 */
	public ActionForward editRoomGroup(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws HibernateException, Exception {	

		RoomGroupEditForm roomGroupEditForm = (RoomGroupEditForm) form;
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);		
			
		//get roomGroup from request
		Long id =  new Long(Long.parseLong(request.getParameter("id")));	
		roomGroupEditForm.setId(id.toString());
		RoomGroupDAO rdao = new RoomGroupDAO();
		RoomGroup rg = rdao.get(id);
		
		//get depts owned by user
		if (roomGroupEditForm.getName()==null || roomGroupEditForm.getName().length()==0)
			roomGroupEditForm.setName(rg.getName());
        if (roomGroupEditForm.getAbbv()==null || roomGroupEditForm.getAbbv().length()==0)
            roomGroupEditForm.setAbbv(rg.getAbbv());
		roomGroupEditForm.setGlobal(rg.isGlobal().booleanValue());
		roomGroupEditForm.setDeptCode(rg.isGlobal().booleanValue()?null:rg.getDepartment().getDeptCode());
		roomGroupEditForm.setDeft(rg.isDefaultGroup().booleanValue());
		if (roomGroupEditForm.getDesc()==null || roomGroupEditForm.getDesc().length()==0)
			roomGroupEditForm.setDesc(rg.getDescription());

		//get rooms owned by user
		Collection assigned = getAssignedRooms(user, rg);
		Collection available = getAvailableRooms(user, rg);

		TreeSet sortedAssignedRooms = new TreeSet(assigned);
		roomGroupEditForm.setAssignedRooms(sortedAssignedRooms);
		
		TreeSet sortedAvailableRooms = new TreeSet(available);
		roomGroupEditForm.setNotAssignedRooms(sortedAvailableRooms);

		Collection roomFeatures = getRoomFeatures(user, rg);
		roomGroupEditForm.setHeading(getHeading(roomFeatures));
		roomGroupEditForm.setRoomFeatures(roomFeatures);
		
		roomGroupEditForm.setRooms();
					
		return mapping.findForward("showEdit");
	}
	
	/**
	 * 
	 * @return
	 */
	private Collection getRoomFeatures(User user, RoomGroup rg) throws Exception {
		
		Long sessionId = org.unitime.timetable.model.Session.getCurrentAcadSession(user).getSessionId();
		
		ArrayList roomFeatures = new ArrayList();

		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager owner = tdao.get(new Long(mgrId));

		org.hibernate.Session hibSession = null;
		try {
			RoomFeatureDAO d = new RoomFeatureDAO();
			hibSession = d.getSession();
			
			List list = hibSession
						.createCriteria(GlobalRoomFeature.class)
						.addOrder(Order.asc("label"))
						.list();
			
			for (Iterator iter = list.iterator();iter.hasNext();) {
				GlobalRoomFeature rf = (GlobalRoomFeature) iter.next();
				roomFeatures.add(rf);
			}

			list = hibSession
					.createCriteria(DepartmentRoomFeature.class)
					.addOrder(Order.asc("label"))
					.list();
			
			for (Iterator i1 = list.iterator();i1.hasNext();) {
				DepartmentRoomFeature rf = (DepartmentRoomFeature) i1.next();
				if (rg.isGlobal().booleanValue()) {
					//roomFeatures.add(rf);
				} else if (rf.getDepartment()!=null && rf.getDepartment().equals(rg.getDepartment())) {
					roomFeatures.add(rf);
				}
			}
			
		} catch (Exception e) {
			Debug.error(e);
		}
		
		return roomFeatures;
	}
	
	/**
	 * 
	 * @return
	 */
	private String[] getHeading(Collection roomFeatures) {
		//set headings
		String fixedHeading[][] = { 
				{ "Room", "left", "true" },
				{ "Type", "left", "true" },
				{ "Capacity", "right", "false" },
				{ "Exam Capacity", "right", "false" }
				};
		
		String heading[] = new String[fixedHeading.length
				+ roomFeatures.size()];
		String alignment[] = new String[heading.length];
		boolean sorted[] = new boolean[heading.length];
		for (int i = 0; i < fixedHeading.length; i++) {
			heading[i] = fixedHeading[i][0];
			alignment[i] = fixedHeading[i][1];
			sorted[i] = (Boolean.valueOf(fixedHeading[i][2])).booleanValue();
		}
		int i = fixedHeading.length;
		for (Iterator it = roomFeatures.iterator(); it.hasNext();) {
			heading[i] = ((RoomFeature) it.next()).getLabel();
			heading[i] = heading[i].replaceAll(" ", "<br>");
			alignment[i] = "center";
			sorted[i] = true;
			i++;
		}
		
		return heading;
	}

	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	public ActionForward cancelRoomGroup(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
		RoomGroupEditForm roomGroupEditForm = (RoomGroupEditForm) form;
		if (roomGroupEditForm.getId()!=null)
			request.setAttribute("hash", "A"+roomGroupEditForm.getId());
		return mapping.findForward("showRoomGroupList");
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception 
	 */
	public ActionForward deleteRoomGroup(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		RoomGroupEditForm roomGroupEditForm = (RoomGroupEditForm) form;
		Long id = new Long(roomGroupEditForm.getId());
		RoomGroupDAO rgdao = new RoomGroupDAO();
		
		org.hibernate.Session hibSession = rgdao.getSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			
			RoomGroup rg = rgdao.get(id, hibSession);
			
			if (rg != null) {
                ChangeLog.addChange(
                        hibSession, 
                        request, 
                        rg, 
                        ChangeLog.Source.ROOM_GROUP_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        null, 
                        rg.getDepartment());

                Collection rooms = rg.getRooms();
				
				//remove roomGroup from room
				for (Iterator iter = rooms.iterator(); iter.hasNext();) {
					Location r = (Location) iter.next();
					Collection roomGroups = r.getRoomGroups();
					roomGroups.remove(rg);
					hibSession.saveOrUpdate(r);

				}
				
				hibSession.delete(rg);
			}
			
			tx.commit();
		} catch (Exception e) {
			Debug.error(e);
			try {
				if(tx!=null && tx.isActive())
					tx.rollback();
			}
			catch (Exception e1) { }
			throw e;
		}
	
		return mapping.findForward("showRoomGroupList");
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward saveRoomGroup(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		RoomGroupEditForm roomGroupEditForm = (RoomGroupEditForm) form;
		ActionMessages errors = new ActionMessages();
		
		//Validate input prefs
        errors = roomGroupEditForm.validate(mapping, request);
        if(errors.size()==0) {
			update(mapping, roomGroupEditForm, request, response);
        } else {
        	saveErrors(request, errors);
			editRoomGroup(mapping, form, request, response);
			return mapping.findForward("showEdit");
        }
				
		if (roomGroupEditForm.getId()!=null)
			request.setAttribute("hash", "A"+roomGroupEditForm.getId());
		return mapping.findForward("showRoomGroupList");
	}
	
	/**
	 * 
	 * @param mapping
	 * @param roomGroupEditForm
	 * @param request
	 * @param response
	 */
	private void update(
			ActionMapping mapping, 
			RoomGroupEditForm roomGroupEditForm, 
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
			
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
		Long id = new Long(roomGroupEditForm.getId()); 
		LocationDAO rdao = new LocationDAO();
		RoomGroupDAO rgdao = new RoomGroupDAO();
		RoomGroup rg = rgdao.get(id);
		
		//update name, defaultGroup, and desc 
		if (roomGroupEditForm.getName() != null && !roomGroupEditForm.getName().trim().equalsIgnoreCase("")) {
			rg.setName(roomGroupEditForm.getName());
		}
        if (roomGroupEditForm.getAbbv() != null && !roomGroupEditForm.getAbbv().trim().equalsIgnoreCase("")) {
            rg.setAbbv(roomGroupEditForm.getAbbv());
        }
		if (roomGroupEditForm.getDesc() != null) {
			rg.setDescription(roomGroupEditForm.getDesc());
		}
		if (user.getRole().equals(Roles.ADMIN_ROLE)) {
			if (roomGroupEditForm.isDeft()) {
				rg.setDefaultGroup(Boolean.TRUE);
			} else {
				rg.setDefaultGroup(Boolean.FALSE);
			}
				
		}
		
		//update rooms	
		String[] selectedAssigned = roomGroupEditForm.getAssignedSelected();
		String[] selectedNotAssigned = roomGroupEditForm.getNotAssignedSelected();
		Collection assignedRooms = getAssignedRooms(user, rg);
		Collection notAssignedRooms = getAvailableRooms(user, rg);
		String s1 = null;
		String s2 = null;
		if (selectedAssigned.length != 0)
			s1 = Constants.arrayToStr(selectedAssigned,"",",");
		else
			s1 = new String();
		if (selectedNotAssigned.length != 0)
			s2 = Constants.arrayToStr(selectedNotAssigned,"",",");
		else 
			s2 = new String();
		
		org.hibernate.Session hibSession = rgdao.getSession();
		
		
		Transaction tx = null;
		try {	
			tx = hibSession.beginTransaction();
			
			checkDefault(hibSession, rg);
			
			//move room from assignedRooms to notAssignedRooms
			if (selectedAssigned.length != assignedRooms.size()) {

				Collection rooms = rg.getRooms();
				Collection m = new HashSet();
				
				
				//remove roomGroup from room
				for (Iterator iter = rooms.iterator(); iter.hasNext();) {
					Location r = (Location) iter.next();
					if (r.getSession().getUniqueId().equals(sessionId) && s1.indexOf(r.getUniqueId().toString()) == -1) {
						Collection roomGroups = r.getRoomGroups();
						roomGroups.remove(rg);
						hibSession.saveOrUpdate(r);
						m.add(r);
					}
				}
				
				//remove room from roomGroup
				rooms.removeAll(m);
			}
			
			//move room from notAssignedRooms to assignedRooms
			if (selectedNotAssigned.length != 0) {
				Collection rooms = rg.getRooms();
				Set m = new HashSet();
				
				//add roomGroup to room
				for (int i = 0; i<selectedNotAssigned.length; i++) {
					Location r = rdao.get(Long.valueOf(selectedNotAssigned[i]));
					Set groups = r.getRoomGroups();
					groups.add(rg);
					hibSession.saveOrUpdate(r);
					m.add(r);
				}
				
				//add room to roomGroup
				rg.setRooms(m);
			}

			hibSession.saveOrUpdate(rg);
            
            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    rg, 
                    ChangeLog.Source.ROOM_GROUP_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    rg.getDepartment());

			tx.commit();
			
			hibSession.refresh(rg);
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
	
	/**
	 * 
	 * @param hibSession
	 * @param rg
	 */
	public void checkDefault(org.hibernate.Session hibSession, RoomGroup rg) {
		if (!rg.isDefaultGroup().booleanValue()) return;
		for (Iterator i=(new RoomGroupDAO()).findAll(hibSession).iterator();i.hasNext();) {
			RoomGroup x = (RoomGroup)i.next();
			if (x.getUniqueId().equals(rg.getUniqueId())) continue;
			if (x.isDefaultGroup().booleanValue()) {
				x.setDefaultGroup(Boolean.FALSE);
				hibSession.saveOrUpdate(x);
			}
		}
	}
	
	/**
	 * 
	 * @param user 
	 * @param rooms
	 * @param rg 
	 * @return
	 * @throws Exception 
	 */
	private Collection getAssignedRooms(User user, RoomGroup rg) throws Exception {
		//get depts owned by user
		String depts[] = null;
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		
		if (!rg.isGlobal().booleanValue()) {
			Department dept = rg.getDepartment();
			depts = new String[] { dept.getDeptCode() };
		}

		//get rooms owned by user
		Collection rooms = Session.getCurrentAcadSession(user).getRoomsFast(depts);
		if (rg.isGlobal().booleanValue() && user.getRole().equals(Roles.EXAM_MGR_ROLE))
		    rooms = Location.findAllExamLocations(sessionId, -1);
		Collection assigned = new HashSet();
		
		for (Iterator iter = rooms.iterator(); iter.hasNext();)  {
			Location r = (Location) iter.next();
			if (r.hasGroup(rg))  assigned.add(r);
		}
		return assigned;
	}
	
	/**
	 * 
	 * @param user
	 * @param d
	 * @return
	 * @throws Exception 
	 */
	private Collection getAvailableRooms(User user, RoomGroup rg) throws Exception {
		//get depts owned by user
		String depts[] = null;
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		
		if (!rg.isGlobal().booleanValue()) {
			Department dept = rg.getDepartment();
			depts = new String[] { dept.getDeptCode() };
		}

		//get rooms owned by user
		Collection rooms = Session.getCurrentAcadSession(user).getRoomsFast(depts);	
        if (rg.isGlobal().booleanValue() && user.getRole().equals(Roles.EXAM_MGR_ROLE))
            rooms = Location.findAllExamLocations(sessionId, -1);
		Collection available = new HashSet();
		
		for (Iterator iter = rooms.iterator(); iter.hasNext();)  {
			Location r = (Location) iter.next();
			if (!r.hasGroup(rg))  available.add(r);
		}
		return available;
	}

}

