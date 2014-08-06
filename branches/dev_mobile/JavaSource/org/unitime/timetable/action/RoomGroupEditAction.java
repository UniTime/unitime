/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoomGroupEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.spring.struts.SpringAwareLookupDispatchAction;
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
 *
 * @author Tomas Muller
 */
@Service("/roomGroupEdit")
public class RoomGroupEditAction extends SpringAwareLookupDispatchAction {
	
	@Autowired SessionContext sessionContext;

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
			
		//get roomGroup from request
		Long id =  new Long(Long.parseLong(request.getParameter("id")));	
		roomGroupEditForm.setId(id.toString());
		RoomGroupDAO rdao = new RoomGroupDAO();
		RoomGroup rg = rdao.get(id);
		
		sessionContext.checkPermission(rg, rg.isGlobal() ? Right.GlobalRoomGroupEdit : Right.DepartmenalRoomGroupEdit);
		
		roomGroupEditForm.setSessionId(sessionContext.getUser().getCurrentAcademicSessionId());
		
		//get depts owned by user
		if (roomGroupEditForm.getName()==null || roomGroupEditForm.getName().isEmpty())
			roomGroupEditForm.setName(rg.getName());
        if (roomGroupEditForm.getAbbv()==null || roomGroupEditForm.getAbbv().isEmpty())
            roomGroupEditForm.setAbbv(rg.getAbbv());
		roomGroupEditForm.setGlobal(rg.isGlobal().booleanValue());
		if (rg.isGlobal()) {
			roomGroupEditForm.setDeptCode(null);
			roomGroupEditForm.setDeptName(null);
			String dept = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
			if (dept != null && dept.matches("Exam[0-9]*")) {
				roomGroupEditForm.setDeptName(ExamTypeDAO.getInstance().get(Long.valueOf(dept.substring(4))).getLabel() + " Examination Rooms");
			} else if (dept != null && !dept.isEmpty() && !"All".equals(dept)) {
				Department department = Department.findByDeptCode(dept, sessionContext.getUser().getCurrentAcademicSessionId());
				if (department != null)
					roomGroupEditForm.setDeptName(department.getDeptCode() + " - " + department.getName());
			}
		} else {
			roomGroupEditForm.setDeptCode(rg.getDepartment().getDeptCode());
			roomGroupEditForm.setDeptName(rg.getDepartment().getDeptCode() + " - " + rg.getDepartment().getName());
		}
		
		roomGroupEditForm.setDeft(rg.isDefaultGroup().booleanValue());
		if (roomGroupEditForm.getDesc()==null || roomGroupEditForm.getDesc().isEmpty())
			roomGroupEditForm.setDesc(rg.getDescription());

		//get rooms owned by user
		Collection assigned = getAssignedRooms(rg);
		Collection available = getAvailableRooms(rg);

		TreeSet sortedAssignedRooms = new TreeSet(assigned);
		roomGroupEditForm.setAssignedRooms(sortedAssignedRooms);
		
		TreeSet sortedAvailableRooms = new TreeSet(available);
		roomGroupEditForm.setNotAssignedRooms(sortedAvailableRooms);
		
		roomGroupEditForm.setRooms();
					
		return mapping.findForward("showEdit");
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
				
				sessionContext.checkPermission(rg, rg.isGlobal() ? Right.GlobalRoomGroupDelete : Right.DepartmenalRoomGroupDelete);
				
                ChangeLog.addChange(
                        hibSession, 
                        sessionContext, 
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
			
		Long id = new Long(roomGroupEditForm.getId()); 
		LocationDAO rdao = new LocationDAO();
		RoomGroupDAO rgdao = new RoomGroupDAO();
		RoomGroup rg = rgdao.get(id);
		
		sessionContext.checkPermission(rg, rg.isGlobal() ? Right.GlobalRoomGroupEdit : Right.DepartmenalRoomGroupEdit);
		
		//update name, defaultGroup, and desc 
		if (roomGroupEditForm.getName() != null && !roomGroupEditForm.getName().trim().equalsIgnoreCase("")) {
			rg.setName(roomGroupEditForm.getName());
		}
        if (roomGroupEditForm.getAbbv() != null && !roomGroupEditForm.getAbbv().trim().equalsIgnoreCase("")) {
            rg.setAbbv(roomGroupEditForm.getAbbv());
        }
		if (roomGroupEditForm.getDesc() != null) {
			rg.setDescription(roomGroupEditForm.getDesc().length() > 200 ? roomGroupEditForm.getDesc().substring(0, 200) : roomGroupEditForm.getDesc());
		}
		if (sessionContext.hasPermission(rg, Right.GlobalRoomGroupEditSetDefault)) {
			if (roomGroupEditForm.isDeft()) {
				rg.setDefaultGroup(Boolean.TRUE);
			} else {
				rg.setDefaultGroup(Boolean.FALSE);
			}
				
		}
		
		//update rooms	
		String[] selectedAssigned = roomGroupEditForm.getAssignedSelected();
		String[] selectedNotAssigned = roomGroupEditForm.getNotAssignedSelected();
		Collection assignedRooms = getAssignedRooms(rg);
		String s1 = null;
		if (selectedAssigned.length != 0)
			s1 = Constants.arrayToStr(selectedAssigned,"",",");
		else
			s1 = new String();
		
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
					if (assignedRooms.contains(r) && s1.indexOf(r.getUniqueId().toString()) == -1) {
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
                    sessionContext, 
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
		for (RoomGroup x: RoomGroup.getAllRoomGroupsForSession(rg.getSession())) {
			if (!x.getUniqueId().equals(rg.getUniqueId()) && x.isDefaultGroup().booleanValue()) {
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
	private Collection getAssignedRooms(RoomGroup rg) throws Exception {
		List<Location> rooms = new ArrayList<Location>(rg.getRooms());
		
		String dept = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
		if (dept != null && dept.matches("Exam[0-9]*")) {
			Long examType = Long.valueOf(dept.substring(4));
			for (Iterator<Location> i = rooms.iterator(); i.hasNext(); ) {
				if (!i.next().isExamEnabled(examType)) i.remove();
			}
		} else if (dept != null && !dept.isEmpty() && !"All".equals(dept)) {
			Department department = Department.findByDeptCode(dept, sessionContext.getUser().getCurrentAcademicSessionId());
			if (department != null) {
				rooms: for (Iterator<Location> i = rooms.iterator(); i.hasNext(); ) {
					Location location = i.next();
					for (RoomDept rd: location.getRoomDepts())
						if (rd.getDepartment().equals(department)) continue rooms;
					i.remove();
				}
			}
		}

		return rooms;
	}
	
	/**
	 * 
	 * @param user
	 * @param d
	 * @return
	 * @throws Exception 
	 */
	private Collection getAvailableRooms(RoomGroup rg) throws Exception {
		List<Location> rooms = null;
		
		if (!rg.isGlobal() && rg.getDepartment() != null) {
			Department dept = rg.getDepartment();
			rooms = new ArrayList<Location>();
			for (RoomDept rd: dept.getRoomDepts())
				rooms.add(rd.getRoom());
		} else {
			Session session = rg.getSession();
			String dept = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
			if (dept != null && dept.matches("Exam[0-9]*")) {
				rooms = new ArrayList<Location>(Location.findAllExamLocations(session.getUniqueId(), Long.valueOf(dept.substring(4))));
			} else if (dept != null && !dept.isEmpty() && !"All".equals(dept)) {
				Department department = Department.findByDeptCode(dept, session.getUniqueId());
				if (department != null) {
					rooms = new ArrayList<Location>();
					for (RoomDept rd: department.getRoomDepts())
						rooms.add(rd.getRoom());
				} else {
					rooms = new ArrayList<Location>(Location.findAll(session.getUniqueId()));	
				}
			} else {
				rooms = new ArrayList<Location>(Location.findAll(session.getUniqueId()));
			}
		}
		
		Collections.sort(rooms);
		
		rooms.removeAll(rg.getRooms());
		
		return rooms;
	}

}

