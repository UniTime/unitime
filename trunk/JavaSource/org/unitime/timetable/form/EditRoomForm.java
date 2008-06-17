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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.RoomDAO;

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
    private String bldgId;
	private String coordX, coordY;
    private String externalId;
    private Long type;
	private boolean owner;
	private boolean room;
    private Boolean examEnabled;
    private Boolean examEEnabled;
    private String examCapacity;
	
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
    public Long getType() {
        return type;
    }
    public void setType(Long type) {
        this.type = type;
    }

    public String getBldgId() {
        return bldgId;
    }
    public void setBldgId(String bldgId) {
        this.bldgId = bldgId;
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

        if ((id==null || id.length()==0) && (bldgId==null || bldgId.length()==0)) {
            errors.add("Building", new ActionMessage("errors.required", "Building") );
        }
        
        if(name==null || name.equalsIgnoreCase("")) {
        	errors.add("Name", new ActionMessage("errors.required", "Name") );
        }
        
        if (room && name!=null && name.length()>0) {
            if (id==null || id.length()==0) {
                if (bldgId!=null && bldgId.length()>0) {
                    try {
                        Room room = Room.findByBldgIdRoomNbr(Long.valueOf(bldgId), name, Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId());
                        if (room!=null) errors.add("Name", new ActionMessage("errors.exists", room.getLabel()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    Room room = Room.findByBldgIdRoomNbr(new RoomDAO().get(Long.valueOf(id)).getBuilding().getUniqueId(), name, Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId());
                    if (room!=null && !room.getUniqueId().toString().equals(id)) errors.add("Name", new ActionMessage("errors.exists", room.getLabel()));
                } catch (Exception e) {}
            }
        }
        
        if(capacity==null || capacity.equalsIgnoreCase("")) {
        	errors.add("Capacity", 
                    new ActionMessage("errors.required", "Capacity") );
        }

        if(examCapacity==null || examCapacity.equalsIgnoreCase("")) {
            errors.add("examCapacity", 
                    new ActionMessage("errors.required", "Examination Seating Capacity") );
        }

        /*
        if(room && coordX==null || coordX.equalsIgnoreCase("") || coordY==null || coordY.equalsIgnoreCase("")) {
            errors.add("Coordinates", 
                    new ActionMessage("errors.required", "Coordinates") );
        }
        */
        
        return errors;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
        bldgName=null; capacity=null; coordX=null; coordY=null; doit=null;
        externalId=null; id=null; name=null; owner=false; room=true; type=null; bldgId = null;
		ignoreTooFar=Boolean.FALSE; ignoreRoomCheck=Boolean.FALSE;
		examEnabled=Boolean.FALSE; examEEnabled=Boolean.FALSE;  examCapacity=null;
	}
	
	public Boolean getExamEnabled() {
	    return examEnabled;
	}
	
	public void setExamEnabled(Boolean examEnabled) {
	    this.examEnabled = examEnabled;
	}
	
    public Boolean getExamEEnabled() {
        return examEEnabled;
    }
    
    public void setExamEEnabled(Boolean examEEnabled) {
        this.examEEnabled = examEEnabled;
    }

    public String getExamCapacity() {
	    return examCapacity;
	}
	
	public void setExamCapacity(String examCapacity) {
	    this.examCapacity = examCapacity;
	}
	
	public Set<RoomType> getRoomTypes() {
	    return RoomType.findAll();
	}
    
}

