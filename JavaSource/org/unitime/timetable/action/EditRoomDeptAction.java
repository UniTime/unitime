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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

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
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.EditRoomDeptForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.RequiredTimeTable;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.action path="/editRoomDept" name="editRoomDeptForm" input="/admin/editRoomDept.jsp" scope="request"
 * @struts.action-forward name="showRoomDetail" path="/roomDetail.do"
 *
 * @author Tomas Muller
 */
@Service("/editRoomDept")
public class EditRoomDeptAction extends Action {
	
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
		EditRoomDeptForm editRoomDeptForm = (EditRoomDeptForm) form;
		
		MessageResources rsc = getResources(request);
		String doit = editRoomDeptForm.getDoit();
		
		if (doit != null) {
			//update location
			if(doit.equals(rsc.getMessage("button.update"))) {
				ActionMessages errors = new ActionMessages();
				errors = editRoomDeptForm.validate(mapping, request);
				if (errors.size() == 0) {
					doUpdate(editRoomDeptForm,request);
					return mapping.findForward("showRoomDetail");
				} else {
					saveErrors(request, errors);
				}
			}
			
			//return to room dtail page
			if(doit.equals(rsc.getMessage("button.returnToRoomDetail"))) {
				response.sendRedirect("roomDetail.do?id="+editRoomDeptForm.getId());
				return null;
				//the following call cannot be used since doit is has the same value as for return to room list (Back)
				//return mapping.findForward("showRoomDetail");
			}
			
			//add room departments
			if(doit.equals(rsc.getMessage("button.addRoomDept"))) {
		    	if (editRoomDeptForm.getDept()==null || editRoomDeptForm.getDept().length()==0) {
		    		ActionMessages errors = new ActionMessages();
		    		errors.add("roomDept", new ActionMessage("errors.required", "Department") );
		    		saveErrors(request, errors);
		    	} else if (editRoomDeptForm.getDepartmentIds().contains(new Long(editRoomDeptForm.getDept()))) {
		    		ActionMessages errors = new ActionMessages();
		    		errors.add("roomDept", new ActionMessage("errors.alreadyPresent", "Department") );
		    		saveErrors(request, errors);
		    	} else {
		    		editRoomDeptForm.addDepartment(editRoomDeptForm.getDept());
		    	}
			}
			
			if (doit.equals(rsc.getMessage("button.removeRoomDept"))) {
		    	if (editRoomDeptForm.getDept()==null || editRoomDeptForm.getDept().length()==0) {
		    		ActionMessages errors = new ActionMessages();
		    		errors.add("roomDept", new ActionMessage("errors.required", "Department") );
		    		saveErrors(request, errors);
		    	} else if (!editRoomDeptForm.getDepartmentIds().contains(new Long(editRoomDeptForm.getDept()))) {
		    		ActionMessages errors = new ActionMessages();
		    		errors.add("roomDept", new ActionMessage("errors.notPresent", "Department") );
		    		saveErrors(request, errors);
		    	} else {
		    		editRoomDeptForm.removeDepartment(editRoomDeptForm.getDept());
		    	}
			}
			
			
		}
		
		Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		
		sessionContext.checkPermission(location, Right.RoomEditAvailability);
		
		if(doit!=null && doit.equals(rsc.getMessage("button.modifyRoomDepts"))) {
			TreeSet roomDepts = new TreeSet(location.getRoomDepts());
			for (Iterator i=roomDepts.iterator();i.hasNext();) {
				RoomDept roomDept = (RoomDept)i.next();
				editRoomDeptForm.addDepartment(roomDept.getDepartment().getUniqueId().toString());
			}
		}
		
		//get roomSharingTable and user preference on location
        boolean timeVertical = CommonValues.VerticalGrid.eq(UserProperty.GridOrientation.get(sessionContext.getUser()));
        RequiredTimeTable rtt = location.getRoomSharingTable(sessionContext.getUser(), editRoomDeptForm.getDepartmentIds());
        rtt.getModel().setDefaultSelection(UserProperty.GridSize.get(sessionContext.getUser()));
        if (doit!=null && (doit.equals(rsc.getMessage("button.removeRoomDept")) || doit.equals(rsc.getMessage("button.addRoomDept")))) {
        	rtt.update(request);
        }
        
        editRoomDeptForm.setSharingTable(rtt.print(true, timeVertical));
        
        //get location information
		if (location instanceof Room) {
			Room r = (Room) location;
			editRoomDeptForm.setName(r.getLabel());		
			editRoomDeptForm.setNonUniv(false);
		} else if (location instanceof NonUniversityLocation) {
				NonUniversityLocation nonUnivLocation = (NonUniversityLocation) location;
				editRoomDeptForm.setName(nonUnivLocation.getName());
				editRoomDeptForm.setNonUniv(true);
		} else {
			ActionMessages errors = new ActionMessages();
			errors.add("editRoomDept", 
	                   new ActionMessage("errors.lookup.notFound", "Room Department") );
			saveErrors(request, errors);
		}
		
		setupDepartments(editRoomDeptForm, request, location);
		
		return mapping.findForward("showEditRoomDept");
	}
	
	/**
	 * 
	 * @param editRoomDeptForm
	 * @param request
	 * @throws Exception 
	 */
	private void doUpdate(
			EditRoomDeptForm editRoomDeptForm, 
			HttpServletRequest request) throws Exception {
		
		Transaction tx = null;
		try {
			LocationDAO ldao = new LocationDAO();
			org.hibernate.Session hibSession = ldao.getSession(); 
			tx = hibSession.beginTransaction();

			Location location = ldao.get(new Long(editRoomDeptForm.getId()), hibSession);
			
			sessionContext.checkPermission(location, Right.RoomEditAvailability);
		
			Set deptIds = new HashSet(editRoomDeptForm.getDepartmentIds());
			
			DepartmentDAO ddao = new DepartmentDAO();
		
			for (Iterator i=location.getRoomDepts().iterator();i.hasNext();) {
				RoomDept rd = (RoomDept)i.next();
				Department d = rd.getDepartment();
				if (!deptIds.remove(d.getUniqueId())) {
					d.getRoomDepts().remove(rd);
					hibSession.saveOrUpdate(rd.getDepartment());
					i.remove();
					hibSession.delete(rd);
					location.removedFromDepartment(d, hibSession);
				}
			}
		
			for (Iterator i=deptIds.iterator();i.hasNext();) {
				Long deptId = (Long)i.next();
				Department d = ddao.get(deptId, hibSession);
				RoomDept rd = new RoomDept();
				rd.setRoom(location);
				rd.setDepartment(d);
				rd.setControl(Boolean.FALSE);
				location.getRoomDepts().add(rd);
				hibSession.save(rd);
				d.getRoomDepts().add(rd);
				hibSession.save(d);
			}
		
			RequiredTimeTable rtt = location.getRoomSharingTable(location.getRoomDepts());
			rtt.update(request);
			location.setRoomSharingTable(rtt);
		
			hibSession.saveOrUpdate(location);
			
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    (Location)location, 
                    ChangeLog.Source.ROOM_DEPT_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    location.getControllingDepartment());

            tx.commit();
			
			ldao.getSession().refresh(location);
		} catch (Exception e) {
			e.printStackTrace();
			if (tx!=null && tx.isActive()) tx.rollback();
			throw e;
		}
	}

    public void setupDepartments(EditRoomDeptForm editRoomDeptForm, HttpServletRequest request, Location location) throws Exception {

    	Collection availableDepts = new Vector();
    	Collection currentDepts = new HashSet();

        Set<Department> departments = Department.getUserDepartments(sessionContext.getUser());
        
    	boolean hasControl = false;
        for (RoomDept rd: location.getRoomDepts()) {
    		currentDepts.add(rd.getDepartment());
    		if (departments.contains(rd.getDepartment()) && rd.isControl())
    			hasControl = true;
    	}
        
		Set<Department> set = Department.findAllBeingUsed(location.getSession().getUniqueId());
				
		for (Department d: set) {
			if (hasControl || departments.contains(d) || !currentDepts.contains(d)) 
				availableDepts.add(new LabelValueBean(d.getDeptCode() + " - " + d.getName(), d.getUniqueId().toString()));
		}
		
		request.setAttribute(Department.DEPT_ATTR_NAME, availableDepts);
    }
}

