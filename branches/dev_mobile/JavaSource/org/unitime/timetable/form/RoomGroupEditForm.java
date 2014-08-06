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
package org.unitime.timetable.form;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.dao.LocationDAO;


/** 
 * MyEclipse Struts
 * Creation date: 05-02-2006
 * 
 * XDoclet definition:
 * @struts.form name="roomGroupEditForm"
 *
 * @author Tomas Muller
 */
public class RoomGroupEditForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables
	private String id;
	private String name;
    private String abbv;
	private boolean global;
	private boolean deft;
	private String desc;
	private Collection assignedRooms;
	private Collection notAssignedRooms;
	private String[] assignedSelected = {};
	private String[] notAssignedSelected = {};
	private String doit;
	private String deptCode;
	private String deptName;
	private Long sessionId;

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
        
        if(abbv==null || abbv.equalsIgnoreCase("")) {
            errors.add("roomGroup", 
                    new ActionMessage("errors.required", "Abbreviation") );
        }

        try {
			
			for (Iterator i=RoomGroup.getAllGlobalRoomGroups(getSessionId()).iterator();i.hasNext();) {
				RoomGroup rg = (RoomGroup)i.next();
				if (rg.getName().equalsIgnoreCase(name) && !rg.getUniqueId().toString().equals(id))
					errors.add("name", new ActionMessage("errors.exists", name));
			}
			
			Department dept = (deptCode==null?null:Department.findByDeptCode(deptCode, getSessionId()));
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
        
        
        if (!global && (deptCode==null || deptCode.equalsIgnoreCase(""))) {
        	errors.add("Department", new ActionMessage("errors.required", "Department") );
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
		name = ""; abbv = null;
		sessionId = null;
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
	
	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	
    public String getAbbv() {
        return abbv;
    }
    
    public void setAbbv(String abbv) {
        this.abbv = abbv;
    }
    
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    
    public String getFeatures(String locationId) {
    	Location location = LocationDAO.getInstance().get(Long.valueOf(locationId));
    	if (location == null) return "";
    	String features = "";
    	for (GlobalRoomFeature feature: location.getGlobalRoomFeatures()) {
    		if (!features.isEmpty()) features += ", ";
    		features += "<span title='" + feature.getLabel() + "' style='white-space:nowrap;'>" + feature.getLabelWithType() + "</span>";
    	}
    	for (DepartmentRoomFeature feature: location.getDepartmentRoomFeatures()) {
    		if (!features.isEmpty()) features += ", ";
    		features += "<span title='" + feature.getLabel() + "' style='white-space:nowrap;'>" + feature.getLabelWithType() + "</span>";
    	}
    	return features;
    }

}

