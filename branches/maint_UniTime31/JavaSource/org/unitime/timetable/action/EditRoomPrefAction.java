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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EditRoomPrefForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.action path="/editRoomPref" name="editRoomPrefForm" input="/admin/setUpRoomPref.jsp" scope="request"
 * @struts.action-forward name="showRoomDetail" path="/roomDetail.do"
 */
public class EditRoomPrefAction extends Action {

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
		HttpSession webSession = request.getSession();
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}
		
		MessageResources rsc = getResources(request);
		String doit = editRoomPrefForm.getDoit();
		
		//return to room list
		if(doit!= null && doit.equals(rsc.getMessage("button.returnToRoomDetail"))) {
			response.sendRedirect("roomDetail.do?id="+editRoomPrefForm.getId());
			return null;
			//the following call cannot be used since doit is has the same value as for return to room list (Back)
			//return mapping.findForward("showRoomDetail");
		}
		
		//delete room preference
		if(doit != null && doit.equals(rsc.getMessage("button.deleteRoomPref"))) {
			doDelete(editRoomPrefForm,request);
			return mapping.findForward("showRoomDetail");
		}
		
		//update location
		if(doit != null && doit.equals(rsc.getMessage("button.update"))) {
			//Validate input prefs
			ActionMessages errors = new ActionMessages();
			errors = editRoomPrefForm.validate(mapping, request);
			if(errors.size()!=0) {
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
		
		//get user preference information
		User user = Web.getUser(webSession);
		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager owner = tdao.get(new Long(mgrId));
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
        ArrayList availableRoomPrefs = new ArrayList();
        Set departments = new TreeSet();
		if (user.getRole().equals(Roles.ADMIN_ROLE)) {
			departments = Department.findAllBeingUsed(sessionId);
		} else {
			departments = owner.departmentsForSession(sessionId);
		}
        //Set departments = new TreeSet(owner.departmentsForSession(sessionId));
		
        Set availableDepts = new TreeSet();
        for (Iterator iter = location.getRoomDepts().iterator(); iter.hasNext();) {
        	RoomDept rd = (RoomDept) iter.next();
        	Department d = rd.getDepartment();
        	if (departments.contains(d)) {
        		availableDepts.add(d);
        	}
        }
        ArrayList list = new ArrayList();
        list.addAll(availableDepts);
		editRoomPrefForm.setDepts(list);
        
        Set rps = new HashSet();
        ArrayList depts = new ArrayList();
        String[] selectedPrefs = new String[availableDepts.size()];
        int i = 0;
        for (Iterator iter = availableDepts.iterator(); iter.hasNext();) {
        	Department dept = (Department) iter.next();
        	RoomPref roomPref = location.getRoomPreference(dept);
        	if (roomPref != null) {
        		selectedPrefs[i] = roomPref.getPrefLevel().getUniqueId().toString();
        	} else{
        		selectedPrefs[i] = PreferenceLevel.PREF_LEVEL_NEUTRAL;
        	}
        	i++;
	        depts.add(new LabelValueBean( dept.getDeptCode()+"-"+dept.getAbbreviation(), dept.getDeptCode())); 
        }
        editRoomPrefForm.setSelectedPref(selectedPrefs);
        
        request.setAttribute(Department.DEPT_ATTR_NAME, depts);
        
        //set default department
        if (!isAdmin && (departments.size() == 1)) {
        	Department d = (Department) departments.iterator().next();
        	editRoomPrefForm.setDeptCode(d.getDeptCode());
        } else if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null) {
        	editRoomPrefForm.setDeptCode(webSession.getAttribute(
					Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
		}
		
        //set availabe room preferences
    	Vector prefs = new Vector();
    	boolean containsPref = false; 
    	for (Enumeration e=PreferenceLevel.getPreferenceLevelList(false).elements();e.hasMoreElements();) {
    		PreferenceLevel pref = (PreferenceLevel)e.nextElement();
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
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();	
		
		//get location information
		Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		
		//update dept preference information
		String[] selectedId = editRoomPrefForm.getSelectedPref();
		PreferenceLevelDAO pldao = new PreferenceLevelDAO();
		DepartmentDAO ddao = new DepartmentDAO();
		for (int i = 0; i<selectedId.length; i++) {
			PreferenceLevel seletedPL = pldao.get(Long.valueOf(selectedId[i]));
			RoomPref selectedRP = new RoomPref();
			selectedRP.setRoom(location);
			selectedRP.setPrefLevel(seletedPL);
				
			String deptCode = editRoomPrefForm.getDepts(i).split("-")[0].trim();
			Department d = Department.findByDeptCode(deptCode, sessionId);
			selectedRP.setOwner(d);
			Set prefs = d.getPreferences();
	
			for (Iterator iter = prefs.iterator(); iter.hasNext();) {
				Preference p = (Preference)iter.next();
	           	if (p instanceof RoomPref && ((RoomPref)p).getRoom().equals(location)) {
	           		pldao.getSession().delete(p);
	           		iter.remove();
	            }
			}
	  
	       	prefs.add(selectedRP);
	       	ddao.saveOrUpdate(d);
            ChangeLog.addChange(
                    null, 
                    request, 
                    location, 
                    ChangeLog.Source.ROOM_PREF_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    d);
		}
        
	}
 
	/**
	 * 
	 * @param editRoomPrefForm
	 * @param request
	 */
	private void doDelete(EditRoomPrefForm editRoomPrefForm, HttpServletRequest request) {
		//get location information
		Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		
		//update user preference information
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager owner = tdao.get(new Long(mgrId));

        //FIXME: needs to be changed to department based
        /*
        //commented out since currently doDelete function is not used
        if (owner instanceof ScheduleDeputy) {
        	ScheduleDeputy sd = (ScheduleDeputy) owner;	
        	Set prefs = sd.getPreferences();   
        	for (Iterator iter = prefs.iterator(); iter.hasNext();) {
	           	RoomPref rp = (RoomPref)iter.next();
	           	if (rp.getRoom().equals(location)) {
	           		iter.remove();
	            }
	        }
        	sd.setPreferences(prefs);
        	tdao.saveOrUpdate(sd);
            
        } else if (owner instanceof ScheduleDeputyAssistant) {
        	ScheduleDeputyAssistant sda = (ScheduleDeputyAssistant) owner;
        	Set prefs = sda.getPreferences();   
        	for (Iterator iter = prefs.iterator(); iter.hasNext();) {
	           	RoomPref rp = (RoomPref)iter.next();
	           	if (rp.getRoom().equals(location)) {
	           		iter.remove();
	            }
	        }
        	sda.setPreferences(prefs);
        	tdao.saveOrUpdate(sda);
        	
        } else if (owner instanceof UserRoles) {
        	UserRoles userRole =(UserRoles) owner;
        	Set prefs = userRole.getPreferences();   
        	for (Iterator iter = prefs.iterator(); iter.hasNext();) {
	           	RoomPref rp = (RoomPref)iter.next();
	           	if (rp.getRoom().equals(location)) {
	           		iter.remove();
	            }
	        }
        	userRole.setPreferences(prefs);
        	tdao.saveOrUpdate(userRole);
        	
        } else {	
        }
        */
	}
	
}

