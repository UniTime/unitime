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
import org.unitime.timetable.form.RoomFeatureEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentRoomFeatureDAO;
import org.unitime.timetable.model.dao.GlobalRoomFeatureDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 06-27-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomFeatureAdd" name="roomFeatureEditForm" input="/admin/roomFeatureAdd.jsp" scope="request"
 * @struts.action-forward name="showAdd" path="roomFeatureAddTile"
 * @struts.action-forward name="showRoomFeatureList" path="/roomFeatureList.do"
 */
public class RoomFeatureAddAction extends Action {

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
		HttpServletResponse response) throws Exception {
		RoomFeatureEditForm roomFeatureEditForm = (RoomFeatureEditForm) form;
		
		HttpSession webSession = request.getSession();
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}
		
		MessageResources rsc = getResources(request);
		String doit = roomFeatureEditForm.getDoit();
		
		if (doit != null) {
			//add new
			if(doit.equals(rsc.getMessage("button.addNew"))) {
				ActionMessages errors = new ActionMessages();
				errors = roomFeatureEditForm.validate(mapping, request);
		        if(errors.size()==0) {
		        	save(mapping, roomFeatureEditForm, request, response);
					return mapping.findForward("showRoomFeatureList");
		        } else {
		        	saveErrors(request, errors);
		        }
			}
			
			//return to room list
			if(doit.equals(rsc.getMessage("button.returnToRoomFeatureList"))) {
				return mapping.findForward("showRoomFeatureList");
			}
		}
		
		//get depts owned by user
		setDeptList(request, roomFeatureEditForm);
		
		User user = Web.getUser(webSession);
		if (user.getRole().equals(Roles.ADMIN_ROLE) || user.getRole().equals(Roles.EXAM_MGR_ROLE)) {
			roomFeatureEditForm.setGlobal(roomFeatureEditForm.getDeptCode()==null
			        || roomFeatureEditForm.getDeptCode().trim().length()==0
			        || roomFeatureEditForm.getDeptCode().equalsIgnoreCase("exam")
			        || roomFeatureEditForm.getDeptCode().equalsIgnoreCase("eexam"));
		} else {
			roomFeatureEditForm.setGlobal(false);
		}
		
		return mapping.findForward("showAdd");
	}
	
	/**
	 * 
	 * @param mapping
	 * @param roomFeatureEditForm
	 * @param request
	 * @param response
	 */
	private void save(
			ActionMapping mapping, 
			RoomFeatureEditForm roomFeatureEditForm, 
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception{
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
				
		//if roomFeature is global
		if (roomFeatureEditForm.isGlobal()) {
			GlobalRoomFeatureDAO gdao = new GlobalRoomFeatureDAO();
			org.hibernate.Session hibSession = gdao.getSession();
			Transaction tx = null;
		
			GlobalRoomFeature rf = new GlobalRoomFeature();
			rf.setLabel(roomFeatureEditForm.getName());
            rf.setAbbv(roomFeatureEditForm.getAbbv());

			try {
				tx = hibSession.beginTransaction();				
				hibSession.saveOrUpdate(rf);			
                
                ChangeLog.addChange(
                        hibSession, 
                        request, 
                        rf, 
                        ChangeLog.Source.ROOM_FEATURE_EDIT, 
                        ChangeLog.Operation.CREATE, 
                        null, 
                        null);
                
				tx.commit();				
				hibSession.refresh(rf);
				request.setAttribute("hash", "A"+rf.getUniqueId());
				
			}catch (Exception e) {
				Debug.error(e);
		        if (tx!=null && tx.isActive()) tx.rollback();
		        throw e;
		    }
		} else {
			DepartmentRoomFeatureDAO ddao = new DepartmentRoomFeatureDAO();
			org.hibernate.Session hibSession = ddao.getSession();
			Transaction tx = null;
		
			DepartmentRoomFeature rf = new DepartmentRoomFeature();
			rf.setLabel(roomFeatureEditForm.getName());
            rf.setAbbv(roomFeatureEditForm.getAbbv());
			
	        rf.setDepartment(Department.findByDeptCode(roomFeatureEditForm.getDeptCode(),sessionId));	

			try {
				tx = hibSession.beginTransaction();				
				hibSession.saveOrUpdate(rf);
                
                ChangeLog.addChange(
                        hibSession, 
                        request, 
                        (RoomFeature)rf, 
                        ChangeLog.Source.ROOM_FEATURE_EDIT, 
                        ChangeLog.Operation.CREATE, 
                        null, 
                        rf.getDepartment());
                
				tx.commit();				
				hibSession.refresh(rf);
				request.setAttribute("hash", "A"+rf.getUniqueId());
			}catch (Exception e) {
				Debug.error(e);
				if (tx!=null && tx.isActive()) tx.rollback();
		        throw e;
		    }
		}
		
	}

	/**
	 * 
	 * @param request
	 * @param roomFeatureEditForm
	 * @throws Exception 
	 */
	private void setDeptList(HttpServletRequest request, RoomFeatureEditForm roomFeatureEditForm) throws Exception {
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
		roomFeatureEditForm.setDeptSize(departments.size());
		
		roomFeatureEditForm.setDeptCode(null);
		
        ArrayList list = new ArrayList();
        for (Iterator iter = departments.iterator(); iter.hasNext();) {
        	Department dept = (Department) iter.next();
        	if (!dept.isEditableBy(user)) continue;
        	list.add(new LabelValueBean( dept.getDeptCode()+" - "+dept.getName(), dept.getDeptCode())); 
        }
        
        request.setAttribute(Department.DEPT_ATTR_NAME, list);
        
        //set default department
        if (!isAdmin && (departments.size() == 1)) {
        	Department d = (Department) departments.iterator().next();
        	roomFeatureEditForm.setDeptCode(d.getDeptCode());
        } else if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null && !"All".equalsIgnoreCase((String)webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME))
                && !"Exam".equalsIgnoreCase((String)webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME))) {
        	roomFeatureEditForm.setDeptCode(webSession.getAttribute(
					Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
		}
	}

}

