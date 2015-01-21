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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.util.IdValue;
import org.unitime.timetable.webutil.WebTextValidation;

/** 
 * MyEclipse Struts
 * Creation date: 07-05-2006
 * 
 * XDoclet definition:
 * @struts.form name="editRoomForm"
 *
 * @author Tomas Muller, Stephanie Schluttenhofer
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
	private String eventDepartment;
	private String bldgName;
    private String bldgId;
	private String coordX, coordY;
    private String externalId;
    private Long type;
	private boolean room;
    private Map<String,Boolean> examEnabled = new HashMap<String, Boolean>();
    private String examCapacity;
    private String area;
    private String breakTime;
    private String note;
    private Integer eventStatus;
	
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
		this.id = ("".equals(id) ? null : id);
	}

	public String getDoit() {
		return doit;
	}

	public void setDoit(String doit) {
		this.doit = doit;
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
    
    private static SessionContext getSessionContext(HttpSession session) {
    	return HttpSessionContext.getSessionContext(session.getServletContext());
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
        if (!room && name!=null && name.length()>0) {
        	Debug.info("checking location regex 2");
	    	String nonUniversityLocationRegex = ApplicationProperty.NonUniversityLocationPattern.value();
	    	String nonUniversityLocationInfo = ApplicationProperty.NonUniversityLocationPatternInfo.value();
	    	if (nonUniversityLocationRegex != null && nonUniversityLocationRegex.trim().length() > 0){
		    	try { 
			    	Pattern pattern = Pattern.compile(nonUniversityLocationRegex);
			    	Matcher matcher = pattern.matcher(name);
			    	if (!matcher.find()) {
				        errors.add("nonUniversityLocation", new ActionMessage("errors.generic", nonUniversityLocationInfo));
			    	}
		    	}
		    	catch (Exception e) {
			        errors.add("nonUniversityLocation", new ActionMessage("errors.generic", "Non University Location cannot be matched to regular expression: " + nonUniversityLocationRegex + ". Reason: " + e.getMessage()));
		    	}
	    	} else {
	    		if (!WebTextValidation.isTextValid(name, true)){
	    			errors.add("nonUniversityLocation", new ActionMessage("errors.invalidCharacters", "Name"));
	    		}
	    	}
        }
        
        if (room && name!=null && name.length()>0) {
            if (id==null || id.length()==0) {
                if (bldgId!=null && bldgId.length()>0) {
                    try {
                        Room room = Room.findByBldgIdRoomNbr(Long.valueOf(bldgId), name, getSessionContext(request.getSession()).getUser().getCurrentAcademicSessionId());
                        if (room!=null) errors.add("Name", new ActionMessage("errors.exists", room.getLabel()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    Room room = Room.findByBldgIdRoomNbr(new RoomDAO().get(Long.valueOf(id)).getBuilding().getUniqueId(), name, getSessionContext(request.getSession()).getUser().getCurrentAcademicSessionId());
                    if (room!=null && !room.getUniqueId().toString().equals(id)) errors.add("Name", new ActionMessage("errors.exists", room.getLabel()));
                } catch (Exception e) {}
            }
        }
        
        if(capacity==null || capacity.equalsIgnoreCase("")) {
        	errors.add("Capacity", 
                    new ActionMessage("errors.required", "Capacity") );
        }

        boolean exams = false;
        for (Boolean x: examEnabled.values())
        	if (x) { exams = true; break; }
        if (exams) {
            if(examCapacity==null || examCapacity.equalsIgnoreCase("")) {
                errors.add("examCapacity", 
                        new ActionMessage("errors.required", "Examination Seating Capacity") );
            }
        }

        /*
        if(room && coordX==null || coordX.equalsIgnoreCase("") || coordY==null || coordY.equalsIgnoreCase("")) {
            errors.add("Coordinates", 
                    new ActionMessage("errors.required", "Coordinates") );
        }
        */
        
        /*
        if (controlDept==null || controlDept.equalsIgnoreCase("")) {
        	errors.add("Department", 
                    new ActionMessage("errors.required", "Department") );
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
        externalId=null; id=null; name=null; room=true; type=null; bldgId = null;
		ignoreTooFar=Boolean.FALSE; ignoreRoomCheck=Boolean.FALSE;
		examEnabled.clear();  examCapacity=null;
		eventStatus = RoomTypeOption.getDefaultStatus();
	}
	
	public boolean getExamEnabled(String type) {
		Boolean enabled = examEnabled.get(type);
	    return enabled != null && enabled;
	}
	
	public void setExamEnabled(String type, boolean examEnabled) {
	    this.examEnabled.put(type, examEnabled);
	}

    public String getExamCapacity() {
	    return examCapacity;
	}
	
	public void setExamCapacity(String examCapacity) {
	    this.examCapacity = examCapacity;
	}
	
	public Set<RoomType> getRoomTypes() {
	    return RoomType.findAll(room);
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

    public Integer getEventStatus() { return eventStatus; }
    
    public void setEventStatus(Integer eventStatus) { this.eventStatus = eventStatus; }
    
    public List<IdValue> getEventStates() { 
    	List<IdValue> ret = new ArrayList<IdValue>();
    	ret.add(new IdValue(-1l, "Default"));
    	for (RoomTypeOption.Status state: RoomTypeOption.Status.values()) {
    		ret.add(new IdValue(new Long(state.ordinal()), state.toString()));
    	}
    	return ret;
    }
}

