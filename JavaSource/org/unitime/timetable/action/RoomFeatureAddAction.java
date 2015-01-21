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
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoomFeatureEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.dao.DepartmentRoomFeatureDAO;
import org.unitime.timetable.model.dao.GlobalRoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 06-27-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomFeatureAdd" name="roomFeatureEditForm" input="/admin/roomFeatureAdd.jsp" scope="request"
 * @struts.action-forward name="showAdd" path="roomFeatureAddTile"
 * @struts.action-forward name="showRoomFeatureList" path="/roomFeatureList.do"
 *
 * @author Tomas Muller
 */
@Service("/roomFeatureAdd")
public class RoomFeatureAddAction extends Action {
	
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
		HttpServletResponse response) throws Exception {
		RoomFeatureEditForm roomFeatureEditForm = (RoomFeatureEditForm) form;
		
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
		LookupTables.setupDepartments(request, sessionContext, false);
		LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
		request.setAttribute("featureTypes", RoomFeatureTypeDAO.getInstance().findAll(Order.asc("label")));
		
        //set default department
		TreeSet<Department> departments = Department.getUserDepartments(sessionContext.getUser());
        if (departments.size() == 1) {
        	roomFeatureEditForm.setDeptCode(departments.first().getDeptCode());
        } else {
        	String deptCode = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
        	if (deptCode != null && !deptCode.isEmpty() && !deptCode.equals("All") && !deptCode.matches("Exam[0-9]*"))
        		roomFeatureEditForm.setDeptCode(deptCode);
		}
		
		if (roomFeatureEditForm.getDeptCode() == null || roomFeatureEditForm.getDeptCode().isEmpty() || roomFeatureEditForm.getDeptCode().matches("Exam[0-9]*") ||
				!sessionContext.hasPermission(roomFeatureEditForm.getDeptCode(), "Department", Right.DepartmentRoomFeatureAdd)) {
			sessionContext.checkPermission(Right.GlobalRoomFeatureAdd);
			roomFeatureEditForm.setGlobal(true);
		} else {
			sessionContext.checkPermission(roomFeatureEditForm.getDeptCode(), "Department", Right.DepartmentRoomFeatureAdd);
			roomFeatureEditForm.setGlobal(false);
		}
		
		roomFeatureEditForm.setSessionId(sessionContext.getUser().getCurrentAcademicSessionId());
		
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
				
		//if roomFeature is global
		if (roomFeatureEditForm.isGlobal()) {
			sessionContext.checkPermission(Right.GlobalRoomFeatureAdd);
			
			GlobalRoomFeatureDAO gdao = new GlobalRoomFeatureDAO();
			org.hibernate.Session hibSession = gdao.getSession();
			Transaction tx = null;
		
			GlobalRoomFeature rf = new GlobalRoomFeature();
			rf.setLabel(roomFeatureEditForm.getName());
            rf.setAbbv(roomFeatureEditForm.getAbbv());
            rf.setSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()));
            
            if (roomFeatureEditForm.getFeatureTypeId() != null && roomFeatureEditForm.getFeatureTypeId() >= 0)
            	rf.setFeatureType(RoomFeatureTypeDAO.getInstance().get(roomFeatureEditForm.getFeatureTypeId()));

			try {
				tx = hibSession.beginTransaction();				
				hibSession.saveOrUpdate(rf);			
                
                ChangeLog.addChange(
                        hibSession, 
                        sessionContext, 
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
			Department department = Department.findByDeptCode(roomFeatureEditForm.getDeptCode(), sessionContext.getUser().getCurrentAcademicSessionId());
			sessionContext.checkPermission(department, Right.DepartmentRoomFeatureAdd);
			
			DepartmentRoomFeatureDAO ddao = new DepartmentRoomFeatureDAO();
			org.hibernate.Session hibSession = ddao.getSession();
			Transaction tx = null;
		
			DepartmentRoomFeature rf = new DepartmentRoomFeature();
			rf.setLabel(roomFeatureEditForm.getName());
            rf.setAbbv(roomFeatureEditForm.getAbbv());
			
	        rf.setDepartment(department);	
	        
            if (roomFeatureEditForm.getFeatureTypeId() != null && roomFeatureEditForm.getFeatureTypeId() >= 0)
            	rf.setFeatureType(RoomFeatureTypeDAO.getInstance().get(roomFeatureEditForm.getFeatureTypeId()));

			try {
				tx = hibSession.beginTransaction();				
				hibSession.saveOrUpdate(rf);
                
                ChangeLog.addChange(
                        hibSession, 
                        sessionContext, 
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

}

