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
package org.unitime.timetable.form;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 05-02-2006
 * 
 * XDoclet definition:
 * @struts.form name="roomGroupEditForm"
 */
public class RoomGroupEditForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables
	private String id;
	private String name;
	private boolean global;
	private boolean deft;
	private boolean feature;
	private String desc;
	private Collection assignedRooms;
	private Collection notAssignedRooms;
	private String[] assignedSelected = {};
	private String[] notAssignedSelected = {};
	private String[] heading;
	private Collection roomFeatures;
	private boolean showDeptSelection;
	private int deptSize;
	private String doit;
	private String deptCode;

	// --------------------------------------------------------- Methods

	/**
	 * 
	 */
	private static final long serialVersionUID = -82818547444631422L;

	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(
		ActionMapping mapping,
		HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
        
        if(name==null || name.equalsIgnoreCase("")) {
        	errors.add("roomGroup", 
                    new ActionMessage("errors.required", "Name") );
        }
        
		try {
			
			for (Iterator i=RoomGroup.getAllGlobalRoomGroups().iterator();i.hasNext();) {
				RoomGroup rg = (RoomGroup)i.next();
				if (rg.getName().equalsIgnoreCase(name) && !rg.getUniqueId().toString().equals(id))
					errors.add("name", new ActionMessage("errors.exists", name));
			}
			
			Department dept = (deptCode==null?null:Department.findByDeptCode(deptCode, Session.getCurrentAcadSession(Web.getUser(request.getSession())).getSessionId()));
			if (dept!=null) {
				for (Iterator i=RoomGroup.getAllDepartmentRoomGroups(dept).iterator();i.hasNext();) {
					RoomGroup rg = (RoomGroup)i.next();
					if (rg.getName().equalsIgnoreCase(name) && !rg.getUniqueId().toString().equals(id))
						errors.add("name", new ActionMessage("errors.exists", name));
				}
			}
			
		} catch (Exception e) {
			Debug.error(e);
			errors.add("name", new ActionMessage("errors.generic", e.getMessage()));
		}
        
        
        if (deptSize != 1) {
	        if(deptCode==null || deptCode.equalsIgnoreCase("")) {
	        	errors.add("Department", 
	                    new ActionMessage("errors.required", "Department") );
	        }
        }
        
        return errors;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 * @throws Exception 
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request){
		name = "";
		setDeptSize(request);
		displayDeptSelection(request);
	}
	
	/**
	 * 
	 * @param request
	 */
	private void setDeptSize(HttpServletRequest request) {
		HttpSession httpSession = request.getSession();
		User user = Web.getUser(httpSession);
		Long sessionId;
		try {
			sessionId = Session.getCurrentAcadSession(user).getUniqueId();
			String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
			TimetableManagerDAO tdao = new TimetableManagerDAO();
	        TimetableManager manager = tdao.get(new Long(mgrId));
	        Set departments = manager.departmentsForSession(sessionId);
	        setDeptSize(departments.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	/**
	 * 
	 *
	 */
	public void setRooms() {
		int i = 0;
		Iterator iter = null;

		if (assignedRooms != null) {
			String[] assignedSelection = new String[assignedRooms.size()];
			for (iter = assignedRooms.iterator(); iter.hasNext();) {
				Location r = (Location) iter.next();
				assignedSelection[i] = r.getUniqueId().toString();
				i++;
			}
			assignedSelected = assignedSelection;
		}
	}

	public Collection getAssignedRooms() {
		return assignedRooms;
	}

	public void setAssignedRooms(Collection assignedRooms) {
		this.assignedRooms = assignedRooms;
	}

	public boolean isDeft() {
		return deft;
	}

	public void setDeft(boolean deft) {
		this.deft = deft;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public boolean isGlobal() {
		return global;
	}

	public void setGlobal(boolean global) {
		this.global = global;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection getNotAssignedRooms() {
		return notAssignedRooms;
	}

	public void setNotAssignedRooms(Collection notAssignedRooms) {
		this.notAssignedRooms = notAssignedRooms;
	}

	public String[] getAssignedSelected() {
		return assignedSelected;
	}

	public void setAssignedSelected(String[] assignedSelected) {
		this.assignedSelected = assignedSelected;
	}

	public String[] getNotAssignedSelected() {
		return notAssignedSelected;
	}

	public void setNotAssignedSelected(String[] notAssignedSelected) {
		this.notAssignedSelected = notAssignedSelected;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String[] getHeading() {
		return heading;
	}

	public void setHeading(String[] heading) {
		this.heading = heading;
	}

	public Collection getRoomFeatures() {
		return roomFeatures;
	}

	public void setRoomFeatures(Collection roomFeatures) {
		this.roomFeatures = roomFeatures;
	}

	public boolean isFeature() {
		return feature;
	}

	public void setFeature(boolean feature) {
		this.feature = feature;
	}

	public boolean isShowDeptSelection() {
		return showDeptSelection;
	}

	public void setShowDeptSelection(boolean showDeptSelection) {
		this.showDeptSelection = showDeptSelection;
	}
	
	/**
	 * 
	 * @param request
	 * @throws Exception
	 */
	public void displayDeptSelection(HttpServletRequest request) {
		showDeptSelection = false;
		
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		if (!user.getRole().equals(Roles.ADMIN_ROLE)) {
			try {
				Long sessionId = Session.getCurrentAcadSession(user).getSessionId();			
				String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
				TimetableManagerDAO tdao = new TimetableManagerDAO();
		        TimetableManager owner = tdao.get(new Long(mgrId));  
		        if (owner.departmentsForSession(sessionId).size() != 1) {
		        	showDeptSelection = true;
		        }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
 	}

	public int getDeptSize() {
		return deptSize;
	}

	public void setDeptSize(int deptSize) {
		this.deptSize = deptSize;
	}

	public String getDoit() {
		return doit;
	}

	public void setDoit(String doit) {
		this.doit = doit;
	}

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
	
	/**
	 * 
	 * @param deptCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public String getDeptName(String deptCode, HttpServletRequest request) throws Exception {
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		Department d = Department.findByDeptCode(deptCode, sessionId); 
		return d.getDeptCode() + " - " + d.getName();		
	}
}

