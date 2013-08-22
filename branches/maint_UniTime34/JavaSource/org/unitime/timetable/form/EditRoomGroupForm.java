/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.form name="editRoomGroupForm"
 */
public class EditRoomGroupForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables
	private String id;
	private String name;
	private String doit;
	private List globalRoomGroupIds;
	private List managerRoomGroupIds;
	private List globalRoomGroupNames;
	private List managerRoomGroupNames;
	private List globalRoomGroupsEditable;
	private List managerRoomGroupsEditable;
	private List globalRoomGroupsAssigned;
	private List managerRoomGroupsAssigned;
	
	
    // --------------------------------------------------------- Classes

    /** Factory to create dynamic list element for room groups */
    protected DynamicListObjectFactory factoryRoomGroups = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

	// --------------------------------------------------------- Methods

	/**
	 * 
	 */
	private static final long serialVersionUID = 5665231020466902579L;

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
        
        return errors;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		globalRoomGroupIds = DynamicList.getInstance(new ArrayList(), factoryRoomGroups);
		managerRoomGroupIds = DynamicList.getInstance(new ArrayList(), factoryRoomGroups);
		globalRoomGroupNames = DynamicList.getInstance(new ArrayList(), factoryRoomGroups);
		managerRoomGroupNames = DynamicList.getInstance(new ArrayList(), factoryRoomGroups);
		globalRoomGroupsEditable = DynamicList.getInstance(new ArrayList(), factoryRoomGroups);
		managerRoomGroupsEditable = DynamicList.getInstance(new ArrayList(), factoryRoomGroups);
		globalRoomGroupsAssigned = DynamicList.getInstance(new ArrayList(), factoryRoomGroups);
		managerRoomGroupsAssigned = DynamicList.getInstance(new ArrayList(), factoryRoomGroups);
	}

	public String getDoit() {
		return doit;
	}

	public void setDoit(String doit) {
		this.doit = doit;
	}

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
    
	public List getGlobalRoomGroupIds() {
		return globalRoomGroupIds;
	}

	public void setGlobalRoomGroupIds(List globalRoomGroupIds) {
		this.globalRoomGroupIds = globalRoomGroupIds;
	}

	public List getGlobalRoomGroupNames() {
		return globalRoomGroupNames;
	}

	public void setGlobalRoomGroupNames(List globalRoomGroupNames) {
		this.globalRoomGroupNames = globalRoomGroupNames;
	}

	public List getGlobalRoomGroupsEditable() {
		return globalRoomGroupsEditable;
	}

	public void setGlobalRoomGroupsEditable(List globalRoomGroupsEditable) {
		this.globalRoomGroupsEditable = globalRoomGroupsEditable;
	}

    public String getGlobalRoomGroupsEditable(int key) {
        return globalRoomGroupsEditable.get(key).toString();
    }
    
    public void setGlobalRoomGroupsEditable(int key, Object value) {
        this.globalRoomGroupsEditable.set(key, value);
    }
    
	public List getManagerRoomGroupIds() {
		return managerRoomGroupIds;
	}

	public void setManagerRoomGroupIds(List managerRoomGroupIds) {
		this.managerRoomGroupIds = managerRoomGroupIds;
	}

	public List getManagerRoomGroupNames() {
		return managerRoomGroupNames;
	}

	public void setManagerRoomGroupNames(List managerRoomGroupNames) {
		this.managerRoomGroupNames = managerRoomGroupNames;
	}

	public List getManagerRoomGroupsEditable() {
		return managerRoomGroupsEditable;
	}

	public void setManagerRoomGroupsEditable(List managerRoomGroupsEditable) {
		this.managerRoomGroupsEditable = managerRoomGroupsEditable;
	}
	
    public String getManagerRoomGroupsEditable(int key) {
        return managerRoomGroupsEditable.get(key).toString();
    }
    
    public void setManagerRoomGroupsEditable(int key, Object value) {
        this.managerRoomGroupsEditable.set(key, value);
    }
	
	public void addToGlobalRoomGroups(RoomGroup rf, Boolean editable, Boolean assigned) {
		this.globalRoomGroupIds.add(rf.getUniqueId().toString());
		this.globalRoomGroupNames.add(rf.getName());
		this.globalRoomGroupsEditable.add(editable);
		this.globalRoomGroupsAssigned.add(assigned);
	}
	
	public void addToMangaerRoomGroups(RoomGroup rf, Boolean editable, Boolean assigned) {
		this.managerRoomGroupIds.add(rf.getUniqueId().toString());
		this.managerRoomGroupNames.add(rf.getName()+" ("+(rf.getDepartment().isExternalManager().booleanValue()?rf.getDepartment().getExternalMgrLabel():rf.getDepartment().getDeptCode()+" - "+rf.getDepartment().getName())+")");
		this.managerRoomGroupsEditable.add(editable);
		this.managerRoomGroupsAssigned.add(assigned);
	}

	public List getGlobalRoomGroupsAssigned() {
		return globalRoomGroupsAssigned;
	}

	public void setGlobalRoomGroupsAssigned(List globalRoomGroupsAssigned) {
		this.globalRoomGroupsAssigned = globalRoomGroupsAssigned;
	}

	public List getManagerRoomGroupsAssigned() {
		return managerRoomGroupsAssigned;
	}

	public void setManagerRoomGroupsAssigned(List managerRoomGroupsAssigned) {
		this.managerRoomGroupsAssigned = managerRoomGroupsAssigned;
	}    
 
}

