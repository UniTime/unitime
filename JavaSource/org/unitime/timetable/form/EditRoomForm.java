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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

/** 
 * MyEclipse Struts
 * Creation date: 07-05-2006
 * 
 * XDoclet definition:
 * @struts.form name="editRoomForm"
 */
public class EditRoomForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9208856268545264291L;
	// --------------------------------------------------------- Instance Variables
	private String doit;
	private String id;
	private String name;
	private String capacity;
	private Boolean ignoreTooFar;
	private Boolean ignoreRoomCheck;
	private String controlDept;
	private String bldgName;
	private String coordX, coordY;
    private String externalId;
    private String type;
	private boolean owner;
	private boolean room;
	
	// --------------------------------------------------------- Methods

	public Boolean getIgnoreTooFar() {
		return ignoreTooFar;
	}

	public Boolean getIgnoreRoomCheck() {
		return ignoreRoomCheck;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public Boolean isIgnoreTooFar() {
		return ignoreTooFar;
	}

	public void setIgnoreTooFar(Boolean ignoreTooFar) {
		this.ignoreTooFar = ignoreTooFar;
	}

	public Boolean isIgnoreRoomCheck() {
		return ignoreRoomCheck;
	}

	public void setIgnoreRoomCheck(Boolean ignoreRoomCheck) {
		this.ignoreRoomCheck = ignoreRoomCheck;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public void setControlDept(String controlDept) {
		this.controlDept = controlDept;
	}

	public String getControlDept() {
		return controlDept;
	}

	public String getBldgName() {
		return bldgName;
	}

	public void setBldgName(String bldgName) {
		this.bldgName = bldgName;
	}

	public String getCoordX() {
		return coordX;
	}

	public void setCoordX(String coordX) {
		this.coordX = coordX;
	}

	public String getCoordY() {
		return coordY;
	}

	public void setCoordY(String coordY) {
		this.coordY = coordY;
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
	
	public boolean isOwner() {
		return owner;
	}
	
	public void setOwner(boolean owner) {
		this.owner = owner;
	}

	public boolean isRoom() {
		return room;
	}
	
	public void setRoom(boolean room) {
		this.room = room;
	}
    
    public String getExternalId() {
        return externalId;
    }
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
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
        	errors.add("Name", 
                    new ActionMessage("errors.required", "Name") );
        }
        
        if(capacity==null || capacity.equalsIgnoreCase("")) {
        	errors.add("Capacity", 
                    new ActionMessage("errors.required", "Capacity") );
        }
        
        return errors;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		ignoreTooFar=Boolean.FALSE; ignoreRoomCheck=Boolean.FALSE;
	}
    
}

