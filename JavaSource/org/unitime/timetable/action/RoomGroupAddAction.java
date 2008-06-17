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
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.RoomGroupEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 06-28-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomGroupAdd" name="roomGroupEditForm" input="/admin/roomGroupAdd.jsp" scope="request"
 * @struts.action-forward name="showRoomGroupList" path="/roomGroupList.do"
 * @struts.action-forward name="showAdd" path="roomGroupAddTile"
 */
public class RoomGroupAddAction extends Action {

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
		RoomGroupEditForm roomGroupEditForm = (RoomGroupEditForm) form;
		
		HttpSession webSession = request.getSession();
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}
		
		MessageResources rsc = getResources(request);
		String doit = roomGroupEditForm.getDoit();
		
		if (doit != null) {
			//add new
			if(doit.equals(rsc.getMessage("button.addNew"))) {
				ActionMessages errors = new ActionMessages();
				errors = roomGroupEditForm.validate(mapping, request);
		        if(errors.size()==0) {
		        	save(mapping, roomGroupEditForm, request, response);
					return mapping.findForward("showRoomGroupList");
		        } else {
		        	saveErrors(request, errors);
		        }
			}
			
			//return to room list
			if(doit.equals(rsc.getMessage("button.returnToRoomGroupList"))) {
				return mapping.findForward("showRoomGroupList");
			}
		}
		
		//get depts owned by user
		setDeptList(request, roomGroupEditForm);
		
		User user = Web.getUser(webSession);
		if (user.getRole().equals(Roles.ADMIN_ROLE) || user.getRole().equals(Roles.EXAM_MGR_ROLE)) {
			roomGroupEditForm.setGlobal(roomGroupEditForm.getDeptCode()==null 
			        || roomGroupEditForm.getDeptCode().trim().length()==0
			        || roomGroupEditForm.getDeptCode().equalsIgnoreCase("exam")
                    || roomGroupEditForm.getDeptCode().equalsIgnoreCase("eexam"));
		} else {
			roomGroupEditForm.setGlobal(false);
		}
		
		return mapping.findForward("showAdd");
	}
	
	private void save(
			ActionMapping mapping, 
			RoomGroupEditForm roomGroupEditForm, 
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception{
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
		
		//create new roomGroup
		LocationDAO rdao = new LocationDAO();
		RoomGroupDAO rgdao = new RoomGroupDAO();
		RoomGroup rg = new RoomGroup();
		
		rg.setName(roomGroupEditForm.getName());
        rg.setAbbv(roomGroupEditForm.getAbbv());
		rg.setSession(Session.getCurrentAcadSession(user));
		rg.setDescription(roomGroupEditForm.getDesc());
		
		rg.setGlobal(Boolean.valueOf(roomGroupEditForm.isGlobal()));
		
		rg.setDefaultGroup(Boolean.valueOf(roomGroupEditForm.isDeft()));

		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
		
        TimetableManager owner = tdao.get(new Long(mgrId));  
    	Set depts = owner.departmentsForSession(sessionId);
        if (!roomGroupEditForm.isGlobal()) {
            Department d = Department.findByDeptCode(roomGroupEditForm.getDeptCode(), sessionId);
            rg.setDepartment(d);
        }

		org.hibernate.Session hibSession = rgdao.getSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();			
			checkDefault(hibSession, rg);			
			hibSession.saveOrUpdate(rg);
            
            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    rg, 
                    ChangeLog.Source.ROOM_GROUP_EDIT, 
                    ChangeLog.Operation.CREATE, 
                    null, 
                    rg.getDepartment());
            
			tx.commit();			
			hibSession.refresh(rg);
    		request.setAttribute("hash", "A"+rg.getUniqueId());
		}catch (Exception e) {
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
	 * @param request
	 * @param roomFeatureEditForm
	 * @throws Exception 
	 */
	private void setDeptList(HttpServletRequest request, RoomGroupEditForm roomGroupEditForm) throws Exception {
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE) || user.getRole().equals(Roles.EXAM_MGR_ROLE);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager manager = tdao.get(new Long(mgrId)); 
        Set departments = new TreeSet();
		if (user.getRole().equals(Roles.ADMIN_ROLE)) {
			departments = Department.findAllBeingUsed(sessionId);
		} else {
			departments = new TreeSet(manager.departmentsForSession(sessionId));
		}
		//Set departments = new TreeSet(manager.departmentsForSession(sessionId));
		roomGroupEditForm.setDeptSize(departments.size());
		
        ArrayList list = new ArrayList();
        roomGroupEditForm.setDeptCode(null);
        for (Iterator iter = departments.iterator(); iter.hasNext();) {
        	Department dept = (Department) iter.next();
        	if (!dept.isEditableBy(user)) continue;
        	list.add(new LabelValueBean( dept.getDeptCode()+" - "+dept.getName(), dept.getDeptCode())); 
        }
        
        request.setAttribute(Department.DEPT_ATTR_NAME, list);
        
        //set default department
        if (!isAdmin && (departments.size() == 1)) {
        	Department d = (Department) departments.iterator().next();
        	roomGroupEditForm.setDeptCode(d.getDeptCode());
        } else if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null && !"All".equalsIgnoreCase((String)webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME))
                && !"Exam".equalsIgnoreCase((String)webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME))) {
        	roomGroupEditForm.setDeptCode(webSession.getAttribute(
					Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
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

}

