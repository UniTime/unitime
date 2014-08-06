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
import java.util.Iterator;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.EditRoomPrefForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.action path="/editRoomPref" name="editRoomPrefForm" input="/admin/setUpRoomPref.jsp" scope="request"
 * @struts.action-forward name="showRoomDetail" path="/roomDetail.do"
 *
 * @author Tomas Muller
 */
@Service("/editRoomPref")
public class EditRoomPrefAction extends Action {
	
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
	 * @throws Exception 
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		EditRoomPrefForm editRoomPrefForm = (EditRoomPrefForm) form;
		
		MessageResources rsc = getResources(request);
		String doit = editRoomPrefForm.getDoit();
		
		//return to room list
		if(doit!= null && doit.equals(rsc.getMessage("button.returnToRoomDetail"))) {
			response.sendRedirect("roomDetail.do?id="+editRoomPrefForm.getId());
			return null;
			//the following call cannot be used since doit is has the same value as for return to room list (Back)
			//return mapping.findForward("showRoomDetail");
		}
				
		//update location
		if(doit != null && doit.equals(rsc.getMessage("button.update"))) {
			//Validate input prefs
			ActionMessages errors = new ActionMessages();
			errors = editRoomPrefForm.validate(mapping, request);
			if(errors != null && errors.size()!=0) {
		       	saveErrors(request, errors);	
			} else {
		    	doUpdate(editRoomPrefForm,request);	
		    	return mapping.findForward("showRoomDetail");
		    } 
		}
		
		//get location information
		Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		if (location instanceof Room) {
			Room r = (Room) location;
			editRoomPrefForm.setName(r.getLabel());
		} else if (location instanceof NonUniversityLocation) {
				NonUniversityLocation nonUnivLocation = (NonUniversityLocation) location;
				editRoomPrefForm.setName(nonUnivLocation.getName());
		} else {
			ActionMessages errors = new ActionMessages();
			errors.add("editRoomGroup", 
	                   new ActionMessage("errors.lookup.notFound", "Room Group") );
			saveErrors(request, errors);
		}
		
		sessionContext.checkPermission(location, Right.RoomEditPreference);
		
        TreeSet<Department> departments = Department.getUserDepartments(sessionContext.getUser());
		
        TreeSet<Department> availableDepts = new TreeSet<Department>();
        for (RoomDept rd: location.getRoomDepts()) {
        	if (departments.contains(rd.getDepartment()))
        		availableDepts.add(rd.getDepartment());
        }
        
		editRoomPrefForm.setDepts(new ArrayList<Department>(availableDepts));
        
        ArrayList depts = new ArrayList();
        ArrayList selectedPrefs = new ArrayList();
        for (Department dept: availableDepts) {
        	RoomPref roomPref = location.getRoomPreference(dept);
        	if (roomPref != null) {
        		selectedPrefs.add(roomPref.getPrefLevel().getUniqueId().toString());
        	} else{
        		selectedPrefs.add(PreferenceLevel.PREF_LEVEL_NEUTRAL);
        	}
	        depts.add(new LabelValueBean(dept.getDeptCode() + "-" + dept.getAbbreviation(), dept.getDeptCode())); 
        }
        editRoomPrefForm.setRoomPrefLevels(selectedPrefs);
        
        request.setAttribute(Department.DEPT_ATTR_NAME, depts);
        
        //set default department
        if (departments.size() == 1) {
        	editRoomPrefForm.setDeptCode(departments.first().getDeptCode());
        } else if (sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom) != null) {
        	editRoomPrefForm.setDeptCode((String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom));
		}
		
        //set availabe room preferences
    	Vector<PreferenceLevel> prefs = new Vector<PreferenceLevel>();
    	for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList()) {
    		if (!pref.getPrefProlog().equalsIgnoreCase(PreferenceLevel.sRequired))
    			prefs.addElement(pref);
    	}
    	request.setAttribute(PreferenceLevel.PREF_LEVEL_ATTR_NAME, prefs);
        
		return mapping.findForward("showEditRoomPref");
	}

	/**
	 * 
	 * @param editRoomPrefForm
	 * @param request
	 */
	private void doUpdate(EditRoomPrefForm editRoomPrefForm, HttpServletRequest request) throws Exception {

		//get location information
		Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		
		sessionContext.checkPermission(location, Right.RoomEditPreference);
		
		//update dept preference information
		
        TreeSet<Department> departments = Department.getUserDepartments(sessionContext.getUser());
		TreeSet<Department> availableDepts = new TreeSet<Department>();
        for (RoomDept rd: location.getRoomDepts()) {
        	if (departments.contains(rd.getDepartment()))
        		availableDepts.add(rd.getDepartment());
        }

		List selectedId = editRoomPrefForm.getRoomPrefLevels();
		int i = 0;
		for (Department dept: availableDepts) {
			PreferenceLevel seletedPL = PreferenceLevelDAO.getInstance().get(Long.valueOf((String)selectedId.get(i++)));
			RoomPref selectedRP = new RoomPref();
			selectedRP.setRoom(location);
			selectedRP.setPrefLevel(seletedPL);
			selectedRP.setOwner(dept);
			Set prefs = dept.getPreferences();
	
			for (Iterator iter = prefs.iterator(); iter.hasNext();) {
				Preference p = (Preference)iter.next();
	           	if (p instanceof RoomPref && ((RoomPref)p).getRoom().equals(location)) {
	           		PreferenceLevelDAO.getInstance().getSession().delete(p);
	           		iter.remove();
	            }
			}
	  
	       	prefs.add(selectedRP);
	       	PreferenceLevelDAO.getInstance().getSession().saveOrUpdate(dept);
            ChangeLog.addChange(
                    null, 
                    sessionContext, 
                    location, 
                    ChangeLog.Source.ROOM_PREF_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    dept);
		}
        
	}
}

