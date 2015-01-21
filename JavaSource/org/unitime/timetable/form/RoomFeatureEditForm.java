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
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.dao.LocationDAO;


/**
 * @author Tomas Muller
 */
public class RoomFeatureEditForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8725073025870425705L;

	// --------------------------------------------------------- Instance
	// Variables
	private String doit;
	
	private String deptCode;
	
	private String deptName;
	
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
	
	private Long iSessionId;
	
	private Long featureTypeId;

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
			for (Iterator i=RoomFeature.getAllGlobalRoomFeatures(getSessionId()).iterator();i.hasNext();) {
				RoomFeature rf = (RoomFeature)i.next();
				if (rf.getLabel().equalsIgnoreCase(name) && !rf.getUniqueId().toString().equals(id))
					errors.add("name", new ActionMessage("errors.exists", name));
			}
			
			Department dept = (deptCode==null?null:Department.findByDeptCode(deptCode, getSessionId()));
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
		
        
        
        if (!global && (deptCode==null || deptCode.equalsIgnoreCase(""))) {
        	errors.add("Department", new ActionMessage("errors.required", "Department") );
        }
        
        return errors;
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
		iSessionId = null;
		featureTypeId = null;
	}
	
	public Long getSessionId() { return iSessionId; }
	
	public void setSessionId(Long sessionId) { iSessionId = sessionId; }
	
	public Long getFeatureTypeId() { return featureTypeId; }
	
	public void setFeatureTypeId(Long featureTypeId) { this.featureTypeId = featureTypeId; }
	
    public String getFeatures(String locationId) {
    	Location location = LocationDAO.getInstance().get(Long.valueOf(locationId));
    	if (location == null) return "";
    	String features = "";
    	for (GlobalRoomFeature feature: location.getGlobalRoomFeatures()) {
    		if (feature.getUniqueId().toString().equals(id)) continue;
    		if (!features.isEmpty()) features += ", ";
    		features += "<span title='" + feature.getLabel() + "' style='white-space:nowrap;'>" + feature.getLabelWithType() + "</span>";
    	}
    	for (DepartmentRoomFeature feature: location.getDepartmentRoomFeatures()) {
    		if (feature.getUniqueId().toString().equals(id)) continue;
    		if (!features.isEmpty()) features += ", ";
    		features += "<span title='" + feature.getLabel() + "' style='white-space:nowrap;'>" + feature.getLabelWithType() + "</span>";
    	}
    	return features;
    }
}
