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

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.webutil.WebTextValidation;


/** 
 * MyEclipse Struts
 * Creation date: 05-05-2006
 * 
 * XDoclet definition:
 * @struts.form name="nonUnivLocationForm"
 *
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class NonUnivLocationForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables
	private String doit;
	private String name;
	private String externalId;
	private String capacity;
	private boolean ignoreTooFar;
	private boolean ignoreRoomCheck;
	private String deptCode;
	private int deptSize;
    private Long type;
	private String coordX, coordY;
	private String area;
	
	// --------------------------------------------------------- Methods

	/**
	 * 
	 */
	private static final long serialVersionUID = 683878933677076553L;

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
        } else {
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
        
        if(capacity==null || capacity.equalsIgnoreCase("")) {
        	errors.add("Capacity", 
                    new ActionMessage("errors.required", "Capacity") );
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
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		name = "";
		ignoreTooFar = false;
		ignoreRoomCheck = false;
		coordX=null; coordY=null;
		area = null;
		externalId = null;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public boolean isIgnoreTooFar() {
		return ignoreTooFar;
	}

	public void setIgnoreTooFar(boolean ignoreTooFar) {
		this.ignoreTooFar = ignoreTooFar;
	}

	public boolean isIgnoreRoomCheck() {
		return ignoreRoomCheck;
	}

	public void setIgnoreRoomCheck(boolean ignoreRoomCheck) {
		this.ignoreRoomCheck = ignoreRoomCheck;
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

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public String getDoit() {
		return doit;
	}

	public void setDoit(String doit) {
		this.doit = doit;
	}

	public int getDeptSize() {
		return deptSize;
	}

	public void setDeptSize(int deptSize) {
		this.deptSize = deptSize;
	}
	
    public Long getType() {
        return type;
    }
    public void setType(Long type) {
        this.type = type;
    }
    public Set<RoomType> getRoomTypes() {
        return RoomType.findAll(false);
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
	
    public String getArea() { return area; }
    
    public void setArea(String area) { this.area = area; }
}

