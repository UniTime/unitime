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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.ExamType;

/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.form name="roomDetailForm"
 *
 * @author Tomas Muller
 */
public class RoomDetailForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables
	private String id;
	private String doit;
	private String sharingTable;
	private String name;
    private String externalId;
	private Integer capacity;
	private Double coordinateX;
	private Double coordinateY;
	private Long type;
    private String typeName;
	private Collection groups;
	private Collection globalFeatures;	
	private Collection departmentFeatures;	
	private List roomPrefs;	
	private List depts;
	private boolean ignoreTooFar = false;
	private boolean ignoreRoomCheck = false;
	private String control = null;
	private boolean nonUniv;
    private Map<String,Boolean> examEnabled = new HashMap<String, Boolean>();
	private Integer examCapacity;
	private String eventDepartment;
	private String area;
    private String breakTime;
    private String note;
    private String eventStatus;
    private List<String> pictures = new ArrayList<String>();
	
	private Long previos, next;

	// --------------------------------------------------------- Methods

	/**
	 * 
	 */
	private static final long serialVersionUID = -542603705961314236L;

	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(
		ActionMapping mapping,
		HttpServletRequest request) {

		return null;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		ignoreTooFar = false;
		ignoreRoomCheck = false;
		control = null;
		examEnabled.clear(); 
		previos = null; next = null;
		eventDepartment = null;
		pictures.clear();
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public Double getCoordinateX() {
		return coordinateX;
	}

	public void setCoordinateX(Double coordinateX) {
		this.coordinateX = coordinateX;
	}

	public Double getCoordinateY() {
		return coordinateY;
	}

	public void setCoordinateY(Double coordinateY) {
		this.coordinateY = coordinateY;
	}

	public Collection getGlobalFeatures() {
		return globalFeatures;
	}

	public void setGlobalFeatures(Collection globalFeatures) {
		this.globalFeatures = globalFeatures;
	}

	public Collection getDepartmentFeatures() {
		return departmentFeatures;
	}

	public void setDepartmentFeatures(Collection departmentFeatures) {
		this.departmentFeatures = departmentFeatures;
	}

	public Collection getGroups() {
		return groups;
	}

	public void setGroups(Collection groups) {
		this.groups = groups;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

	public Long getType() {
		return type;
	}

	public void setType(Long type) {
		this.type = type;
	}

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSharingTable() {
		return sharingTable;
	}

	public void setSharingTable(String sharingTable) {
		this.sharingTable = sharingTable;
	}

	public String getDoit() {
		return doit;
	}

	public void setDoit(String doit) {
		this.doit = doit;
	}

	public void setIgnoreTooFar(boolean ignoreTooFar) {
		this.ignoreTooFar = ignoreTooFar;
	}
	
	public boolean getIgnoreTooFar() { return ignoreTooFar; }

	public boolean isIgnoreRoomCheck() {
		return ignoreRoomCheck;
	}

	public void setIgnoreRoomCheck(boolean ignoreRoomCheck) {
		this.ignoreRoomCheck = ignoreRoomCheck;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String control) {
		this.control = control;
	}

	public List getRoomPrefs() {
		return roomPrefs;
	}

	public void setRoomPrefs(List roomPrefs) {
		this.roomPrefs = roomPrefs;
	}

	public List getDepts() {
		return depts;
	}

	public void setDepts(List depts) {
		this.depts = depts;
	}

	public boolean isNonUniv() {
		return nonUniv;
	}

	public void setNonUniv(boolean nonUniv) {
		this.nonUniv = nonUniv;
	}
	
	public Integer getExamCapacity() {
	    return examCapacity;
	}
	
	public void setExamCapacity(Integer examCapacity) {
	    this.examCapacity = examCapacity;
	}
	
	public boolean getExamEnabled(String type) {
		Boolean enabled = examEnabled.get(type);
	    return enabled != null && enabled;
	}
	
	public void setExamEnabled(String type, boolean examEnabled) {
	    this.examEnabled.put(type, examEnabled);
	}
		
	public String getExamEnabledProblems() {
		String ret = "";
		for (ExamType type: ExamType.findAll()) {
			if (getExamEnabled(type.getUniqueId().toString())) {
				if (!ret.isEmpty()) ret += ", ";
				ret += type.getLabel();
			}
		}
		return ret; 
	}

    public Long getNext() {
    	return next;
    }
    
    public void setNext(Long next) {
    	this.next = next;
    }
    
    public Long getPrevious() {
    	return previos;
    }
    
    public void setPrevious(Long previous) {
    	this.previos = previous;
    }
    
    public String getEventDepartment() {
    	return eventDepartment;
    }
    
    public void setEventDepartment(String eventDepartment) {
    	this.eventDepartment = eventDepartment;
    }
    
    public String getArea() { return area; }
    
    public void setArea(String area) { this.area = area; }
    
    public String getNote() { return note; }
    
    public void setNote(String note) { this.note = note; }
    
    public String getBreakTime() { return breakTime; }
    
    public void setBreakTime(String breakTime) { this.breakTime = breakTime; }

    public String getEventStatus() { return eventStatus; }
    
    public void setEventStatus(String eventStatus) { this.eventStatus = eventStatus; }
    
    public List<String> getPictures() { return pictures; }
    
    public void setPictures(List<String> pictures) { this.pictures = pictures; }
}

