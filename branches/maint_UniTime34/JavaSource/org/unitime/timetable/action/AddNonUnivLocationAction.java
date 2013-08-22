/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.NonUnivLocationForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.dao.NonUniversityLocationDAO;
import org.unitime.timetable.model.dao.RoomTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LocationPermIdGenerator;


/** 
 * MyEclipse Struts
 * Creation date: 05-05-2006
 * 
 * XDoclet definition:
 * @struts.action path="/addNonUnivLocation" name="nonUnivLocationForm" input="/admin/addNonUnivLocation.jsp" scope="request" validate="true"
 */
@Service("/addNonUnivLocation")
public class AddNonUnivLocationAction extends Action {

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods
	
	@Autowired SessionContext sessionContext;

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
		
		sessionContext.checkPermission(Right.AddNonUnivLocation);
		
		Set<Department> departments = Department.getUserDepartments(sessionContext.getUser());
		
		if (nonUnivLocationForm.getDoit() != null) {
			String doit = nonUnivLocationForm.getDoit();
			if (doit.equals(rsc.getMessage("button.returnToRoomList"))) {
				return mapping.findForward("showRoomList");
			}
			if (doit.equals(rsc.getMessage("button.addNew"))) {
			     // Validate input prefs
	            errors = nonUnivLocationForm.validate(mapping, request);
	            
	            if (errors.isEmpty() && !sessionContext.hasPermission(nonUnivLocationForm.getDeptCode(), "Department", Right.AddNonUnivLocation)) {
	            	errors.add("nonUniversityLocation", new ActionMessage("errors.generic", "Acess denied."));
	            }
	            
	            // No errors
	            if(errors.size()==0) {
	            	update(request, nonUnivLocationForm);
					return mapping.findForward("showRoomList");
	            }
	            else {
	            	setDepts(request, departments);
	                saveErrors(request, errors);
					return mapping.findForward("showAdd");
	            }
			}
		}
		
		setDepts(request, departments);
		
        nonUnivLocationForm.setDeptSize(departments.size());
        if (departments.size() == 1) {
        	Department d = departments.iterator().next();
        	nonUnivLocationForm.setDeptCode(d.getDeptCode());
        } else if (sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom) != null) {
			nonUnivLocationForm.setDeptCode((String) sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom));
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
		
		org.hibernate.Session hibSession = (new NonUniversityLocationDAO()).getSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			NonUniversityLocation nonUniv = new NonUniversityLocation();
			
			nonUniv.setSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()));
				
			if (nonUnivLocationForm.getName() != null && !nonUnivLocationForm.getName().trim().equalsIgnoreCase("")) {
				nonUniv.setName(nonUnivLocationForm.getName());
			}
				
			if (nonUnivLocationForm.getCapacity() != null && !nonUnivLocationForm.getCapacity().trim().equalsIgnoreCase("")) {
				nonUniv.setCapacity(Integer.valueOf(nonUnivLocationForm.getCapacity()));
			}
				
			nonUniv.setIgnoreTooFar(Boolean.valueOf(nonUnivLocationForm.isIgnoreTooFar()));
			nonUniv.setIgnoreRoomCheck(Boolean.valueOf(nonUnivLocationForm.isIgnoreRoomCheck()));
				
			nonUniv.setCoordinateX(nonUnivLocationForm.getCoordX()==null || nonUnivLocationForm.getCoordX().length()==0 ? null : Double.valueOf(nonUnivLocationForm.getCoordX()));
			nonUniv.setCoordinateY(nonUnivLocationForm.getCoordY()==null || nonUnivLocationForm.getCoordY().length()==0 ? null : Double.valueOf(nonUnivLocationForm.getCoordY()));
			
			if (nonUnivLocationForm.getExternalId() != null && !nonUnivLocationForm.getExternalId().isEmpty())
				nonUniv.setExternalUniqueId(nonUnivLocationForm.getExternalId());

            Double area = null;
            if (nonUnivLocationForm.getArea() != null && !nonUnivLocationForm.getArea().isEmpty()) {
            	try {
            		area = new DecimalFormat(ApplicationProperties.getProperty("unitime.room.area.units.format", "#,##0.00")).parse(nonUnivLocationForm.getArea()).doubleValue();
            	} catch (NumberFormatException e) {
            	}
            }
            nonUniv.setArea(area);
			
			nonUniv.setFeatures(new HashSet());
			nonUniv.setAssignments(new HashSet());
			nonUniv.setRoomGroups(new HashSet());
			nonUniv.setRoomDepts(new HashSet());
			nonUniv.setExamCapacity(0);
			nonUniv.setRoomType(RoomTypeDAO.getInstance().get(nonUnivLocationForm.getType()));
			
			LocationPermIdGenerator.setPermanentId(nonUniv);
			
			hibSession.saveOrUpdate(nonUniv);
			
			//set room department for location
			RoomDept rd = new RoomDept();
			rd.setRoom(nonUniv);
			rd.setControl(Boolean.TRUE);
			
			Department d = Department.findByDeptCode(nonUnivLocationForm.getDeptCode(), sessionContext.getUser().getCurrentAcademicSessionId(), hibSession);
			rd.setDepartment(d);
			
			hibSession.saveOrUpdate(rd);
			
			nonUniv.getRoomDepts().add(rd);
			hibSession.saveOrUpdate(nonUniv);
            
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
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
	private void setDepts(HttpServletRequest request, Set<Department> departments) throws Exception {
		List<LabelValueBean> list = new ArrayList<LabelValueBean>();
		for (Department d: departments) {
			String code = d.getDeptCode().trim();
			String abbv = d.getName().trim();
			list.add(new LabelValueBean(code + " - " + abbv, code)); 
		}
		
		request.setAttribute(Department.DEPT_ATTR_NAME, list);
	}

}

