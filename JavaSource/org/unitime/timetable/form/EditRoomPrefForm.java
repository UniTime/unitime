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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.form name="editRoomPrefForm"
 */
public class EditRoomPrefForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3038073479295582435L;
	// --------------------------------------------------------- Instance Variables
	private String doit;
	private String pref;
	private String name;
	private String id;
	private List roomPrefLevels;
	private String deptCode;
	private List depts;
	
    // --------------------------------------------------------- Classes
    /** Factory to create dynamic list element for Preference Level */
    protected DynamicListObjectFactory factoryPrefLevel = new DynamicListObjectFactory() {
        public Object create() {
            return new String(PreferenceLevel.PREF_LEVEL_NEUTRAL);
        }
    };
	// --------------------------------------------------------- Methods

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
    /**
     * Checks that pref levels are selected
     * @param lst List of pref levels
     * @return true if checks ok, false otherwise
     */
    public boolean checkPrefLevels(List lst) {
        
        for(int i=0; i<lst.size(); i++) {
            String value = ((String) lst.get(i));
            // No selection made
            if(value.trim().equals(Preference.BLANK_PREF_VALUE)) {
                return false;
            }
        }
        return true;
    }

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		roomPrefLevels = DynamicList.getInstance(new ArrayList(), factoryPrefLevel);
		depts = DynamicList.getInstance(new ArrayList(), factoryPrefLevel); 
	}

	public String getDoit() {
		return doit;
	}

	public void setDoit(String doit) {
		this.doit = doit;
	}

	public String getPref() {
		return pref;
	}

	public void setPref(String pref) {
		this.pref = pref;
	}
	
    /**
     * @return Returns the roomPrefLevels.
     */
    public List getRoomPrefLevels() {
        return roomPrefLevels;
    }
    /**
     * @return Returns the roomPrefLevels.
     */
    public String getRoomPrefLevels(int key) {
        return roomPrefLevels.get(key).toString();
    }
    /**
     * @param roomPrefs The roomPrefLevels to set.
     */
    public void setRoomPrefLevels(int key, Object value) {
        this.roomPrefLevels.set(key, value);
    }
    /**
     * @param roomPrefs The roomPrefLevels to set.
     */
    public void setRoomPrefLevels(List roomPrefLevels) {
        this.roomPrefLevels = roomPrefLevels;
    }

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public List getDepts() {
		return depts;
	}

	public void setDepts(List depts) {
		this.depts = depts;
	}
	
    public String getDepts(int key) {
        return depts.get(key).toString();
    }
    
    public void setDepts(int key, Object value) {
        this.depts.set(key, value);
    }

}

