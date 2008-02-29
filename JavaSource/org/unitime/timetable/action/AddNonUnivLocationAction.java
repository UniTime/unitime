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
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.NonUnivLocationForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.NonUniversityLocationDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 05-05-2006
 * 
 * XDoclet definition:
 * @struts.action path="/addNonUnivLocation" name="nonUnivLocationForm" input="/admin/addNonUnivLocation.jsp" scope="request" validate="true"
 */
public class AddNonUnivLocationAction extends Action {

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
		NonUnivLocationForm nonUnivLocationForm = (NonUnivLocationForm) form;
		MessageResources rsc = getResources(request);
		ActionMessages errors = new ActionMessages();
		
		if (nonUnivLocationForm.getDoit() != null) {
			String doit = nonUnivLocationForm.getDoit();
			if (doit.equals(rsc.getMessage("button.returnToRoomList"))) {
				return mapping.findForward("showRoomList");
			}
			if (doit.equals(rsc.getMessage("button.addNew"))) {
			     // Validate input prefs
	            errors = nonUnivLocationForm.validate(mapping, request);
	            
	            // No errors
	            if(errors.size()==0) {
	            	update(request, nonUnivLocationForm);
					return mapping.findForward("showRoomList");
	            }
	            else {
	            	setDepts(request);
	                saveErrors(request, errors);
					return mapping.findForward("showAdd");
	            }
			}
		}
			
		setDepts(request);
		
		//set default department based on user selection or department that user owns
		HttpSession httpSession = request.getSession();
		User user = Web.getUser(httpSession);
		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();	
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager manager = tdao.get(new Long(mgrId));
        Set departments = manager.departmentsForSession(sessionId);
        if (isAdmin)
        	departments = Department.findAllBeingUsed(sessionId);
        nonUnivLocationForm.setDeptSize(departments.size());
        if (!isAdmin && (departments.size() == 1)) {
        	Department d = (Department) departments.iterator().next();
        	nonUnivLocationForm.setDeptCode(d.getDeptCode());
        } else if (httpSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null) {
			nonUnivLocationForm.setDeptCode(httpSession.getAttribute(
					Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
		} 
		
		return mapping.findForward("showAdd");
	}
	
	/**
	 * 
	 * @param request
	 * @param nonUnivLocationForm
	 * @throws Exception
	 */
	private void update(
			HttpServletRequest request, 
			NonUnivLocationForm nonUnivLocationForm) throws Exception {

		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
			
		org.hibernate.Session hibSession = (new NonUniversityLocationDAO()).getSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			NonUniversityLocation nonUniv = new NonUniversityLocation();
			
			nonUniv.setSession(Session.getCurrentAcadSession(user));
				
			String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		    nonUniv.setManagerIds(mgrId);
		        
			if (nonUnivLocationForm.getName() != null && !nonUnivLocationForm.getName().trim().equalsIgnoreCase("")) {
				nonUniv.setName(nonUnivLocationForm.getName());
			}
				
			if (nonUnivLocationForm.getCapacity() != null && !nonUnivLocationForm.getCapacity().trim().equalsIgnoreCase("")) {
				nonUniv.setCapacity(Integer.valueOf(nonUnivLocationForm.getCapacity()));
			}
				
			nonUniv.setIgnoreTooFar(Boolean.valueOf(nonUnivLocationForm.isIgnoreTooFar()));
			nonUniv.setIgnoreRoomCheck(Boolean.valueOf(nonUnivLocationForm.isIgnoreRoomCheck()));
				
			Integer coordinate = Integer.valueOf("-1");
			nonUniv.setCoordinateX(coordinate);
			nonUniv.setCoordinateY(coordinate);
			
			nonUniv.setFeatures(new HashSet());
			nonUniv.setAssignments(new HashSet());
			nonUniv.setRoomGroups(new HashSet());
			nonUniv.setRoomDepts(new HashSet());
			nonUniv.setExamEnabled(Boolean.FALSE);
			nonUniv.setExamCapacity(0);
			
			hibSession.saveOrUpdate(nonUniv);
			
			//set room department for location
			RoomDept rd = new RoomDept();
			rd.setRoom(nonUniv);
			rd.setControl(Boolean.TRUE);
			
			Department d = null;
			if (nonUnivLocationForm.getDeptCode() == null) {
				TimetableManagerDAO tdao = new TimetableManagerDAO();
		        TimetableManager owner = tdao.get(new Long(mgrId));
		        d = (Department)owner.departmentsForSession(sessionId).iterator().next();
			} else {
				String deptCode = nonUnivLocationForm.getDeptCode();
				d = Department.findByDeptCode(deptCode, sessionId);
			}
			
			rd.setDepartment(d);
			
			hibSession.saveOrUpdate(rd);
			
			nonUniv.getRoomDepts().add(rd);
			hibSession.saveOrUpdate(nonUniv);
            
            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    (Location)nonUniv, 
                    ChangeLog.Source.ROOM_EDIT, 
                    ChangeLog.Operation.CREATE, 
                    null, 
                    d);
            
			tx.commit();
			
			hibSession.refresh(d);
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			throw e;
		}
	}

	/**
	 * 
	 * @param request
	 * @param nonUnivLocationForm 
	 * @throws Exception
	 */
	private void setDepts(HttpServletRequest request) throws Exception {
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
		ArrayList departments = new ArrayList();
		TreeSet depts = new TreeSet();
		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager owner = tdao.get(new Long(mgrId));

		if (user.getRole().equals(Roles.ADMIN_ROLE)) {
			depts = Department.findAllBeingUsed(sessionId);
		} else {
			depts = new TreeSet(owner.departmentsForSession(sessionId));
		}
			
		for (Iterator i=depts.iterator();i.hasNext();) {
			Department d = (Department)i.next();
			if (!d.isEditableBy(user)) continue;
			String code = d.getDeptCode().trim();
			String abbv = d.getName().trim();
			departments.add(new LabelValueBean(code + " - " + abbv, code)); 
		}
		
		request.setAttribute(Department.DEPT_ATTR_NAME, departments);
		
	}

}

