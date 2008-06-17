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
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


public class RoomFeatureEditForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8725073025870425705L;

	// --------------------------------------------------------- Instance
	// Variables
	private String doit;
	
	private int deptSize;
	
	private String deptCode;
	
	private String name;
    
    private String abbv;

	private String id;

	private boolean global;

	private Collection assignedRooms;

	private Collection notAssignedRooms;

	private String[] assigned;

	private String[] notAssigned;

	private String[] assignedSelected = {};

	private String[] notAssignedSelected = {};

	public Collection getAssignedRooms() {
		return assignedRooms;
	}

	public void setAssignedRooms(Collection assignedRooms) {
		this.assignedRooms = assignedRooms;
	}

	public String[] getAssigned() {
		return assigned;
	}

	public void setAssigned(String[] assigned) {
		this.assigned = assigned;
	}

	public String[] getNotAssigned() {
		return notAssigned;
	}

	public void setNotAssigned(String[] notAssigned) {
		this.notAssigned = notAssigned;
	}

	public Collection getNotAssignedRooms() {
		return notAssignedRooms;
	}

	public void setNotAssignedRooms(Collection notAssignedRooms) {
		this.notAssignedRooms = notAssignedRooms;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
			assigned = new String[assignedRooms.size()];
			for (iter = assignedRooms.iterator(); iter.hasNext();) {
				Location r = (Location) iter.next();
				assignedSelection[i] = r.getUniqueId().toString();

				assigned[i] = r.getUniqueId().toString();
				i++;
			}
			assignedSelected = assignedSelection;
		}

		i = 0;
		if (notAssignedRooms != null) {
			notAssigned = new String[notAssignedRooms.size()];
			for (iter = notAssignedRooms.iterator(); iter.hasNext();) {
				Location r = (Location) iter.next();
				notAssigned[i] = r.getUniqueId().toString();
				i++;
			}
		}

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
        	errors.add("roomFeature", 
                    new ActionMessage("errors.required", "Name") );
        }
        
        if(abbv==null || abbv.equalsIgnoreCase("")) {
            errors.add("roomFeature", 
                    new ActionMessage("errors.required", "Abbreviation") );
        }

        try {
			
			for (Iterator i=RoomFeature.getAllGlobalRoomFeatures().iterator();i.hasNext();) {
				RoomFeature rf = (RoomFeature)i.next();
				if (rf.getLabel().equalsIgnoreCase(name) && !rf.getUniqueId().toString().equals(id))
					errors.add("name", new ActionMessage("errors.exists", name));
			}
			
			Department dept = (deptCode==null?null:Department.findByDeptCode(deptCode, Session.getCurrentAcadSession(Web.getUser(request.getSession())).getSessionId()));
			if (dept!=null) {
				for (Iterator i=RoomFeature.getAllDepartmentRoomFeatures(dept).iterator();i.hasNext();) {
					RoomFeature rf = (RoomFeature)i.next();
					if (rf.getLabel().equalsIgnoreCase(name) && !rf.getUniqueId().toString().equals(id))
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

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
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
    
    public String getAbbv() {
        return abbv;
    }
    
    public void setAbbv(String abbv) {
        this.abbv = abbv;
    }
	
	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		name = ""; abbv = "";
		setDeptSize(request);
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
	 * @param deptCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public String getDeptName(String deptCode, HttpServletRequest request) throws Exception {
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		Department dept = Department.findByDeptCode(deptCode, sessionId);
		return (dept==null?"":dept.getDeptCode() + " - " + dept.getName());	
	}
}
