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

import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoomGroupEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 06-28-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomGroupAdd" name="roomGroupEditForm" input="/admin/roomGroupAdd.jsp" scope="request"
 * @struts.action-forward name="showRoomGroupList" path="/roomGroupList.do"
 * @struts.action-forward name="showAdd" path="roomGroupAddTile"
 *
 * @author Tomas Muller
 */
@Service("/roomGroupAdd")
public class RoomGroupAddAction extends Action {

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
		RoomGroupEditForm roomGroupEditForm = (RoomGroupEditForm) form;
		
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
		LookupTables.setupDepartments(request, sessionContext, false);
		
        //set default department
		TreeSet<Department> departments = Department.getUserDepartments(sessionContext.getUser());
        if (departments.size() == 1) {
        	roomGroupEditForm.setDeptCode(departments.first().getDeptCode());
        } else {
        	String deptCode = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
        	if (deptCode != null && !deptCode.isEmpty() && !deptCode.equals("All") && !deptCode.matches("Exam[0-9]*"))
        		roomGroupEditForm.setDeptCode(deptCode);
		}
		
		if (roomGroupEditForm.getDeptCode() == null || roomGroupEditForm.getDeptCode().isEmpty() || roomGroupEditForm.getDeptCode().matches("Exam[0-9]*") ||
			!sessionContext.hasPermission(roomGroupEditForm.getDeptCode(), "Department", Right.DepartmentRoomGroupAdd)) {
			sessionContext.checkPermission(Right.GlobalRoomGroupAdd);
			roomGroupEditForm.setGlobal(true);
		} else {
			sessionContext.checkPermission(roomGroupEditForm.getDeptCode(), "Department", Right.DepartmentRoomGroupAdd);
			roomGroupEditForm.setGlobal(false);
		}
		
		roomGroupEditForm.setSessionId(sessionContext.getUser().getCurrentAcademicSessionId());
		
		return mapping.findForward("showAdd");
	}
	
	private void save(
			ActionMapping mapping, 
			RoomGroupEditForm roomGroupEditForm, 
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception{
		
		Department d = (roomGroupEditForm.isGlobal() ? null : Department.findByDeptCode(roomGroupEditForm.getDeptCode(), sessionContext.getUser().getCurrentAcademicSessionId()));
		
		if (d == null)
			sessionContext.checkPermission(Right.GlobalRoomGroupAdd);
		else
			sessionContext.checkPermission(d, Right.DepartmentRoomFeatureAdd);
		
		//create new roomGroup
		RoomGroupDAO rgdao = new RoomGroupDAO();
		RoomGroup rg = new RoomGroup();
		
		rg.setName(roomGroupEditForm.getName());
        rg.setAbbv(roomGroupEditForm.getAbbv());
		rg.setSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()));
		rg.setDescription(roomGroupEditForm.getDesc().length() > 200 ? roomGroupEditForm.getDesc().substring(0, 200) : roomGroupEditForm.getDesc());
		
		rg.setGlobal(d == null);
		rg.setDepartment(d);
		
		rg.setDefaultGroup(roomGroupEditForm.isDeft());

		org.hibernate.Session hibSession = rgdao.getSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();			
			checkDefault(hibSession, rg);			
			hibSession.saveOrUpdate(rg);
            
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
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

}

