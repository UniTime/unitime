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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;


/** 
* MyEclipse Struts
* Creation date: 02-18-2005
* 
* XDoclet definition:
* @struts:form name="roomListForm"
*/
public class RoomListForm extends ActionForm {

	private static final long serialVersionUID = 3256728385592768053L;
	/**
	 * 
	 */
	// --------------------------------------------------------- Instance Variables
	private Collection rooms;
	private String deptCode;
	private boolean editRoomSharing;
	private boolean deptSize;
	private boolean canAdd;
	
	// --------------------------------------------------------- Methods

	public String getDeptCodeX() {
		return deptCode;
	}

	public void setDeptCodeX(String deptCode) {
		this.deptCode = deptCode;
	}

	public boolean getEditRoomSharing() {
		return editRoomSharing;
	}

	public void setEditRoomSharing(boolean editRoomSharing) {
		this.editRoomSharing = editRoomSharing;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		rooms = new ArrayList();
		editRoomSharing = false;
		deptSize=displayDeptList(request);
		canAdd = false;
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
    private boolean displayDeptList(HttpServletRequest request) {
    	deptSize = true;
    	User user = Web.getUser(request.getSession());
    	Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
    	if (!user.getRole().equals(Roles.ADMIN_ROLE)) {
	    	TimetableManager mgr = TimetableManager.getManager(user);
	    	Set mgrDepts = Department.findAllOwned(sessionId, mgr, true);
	    	if (mgrDepts.size() == 1) {
	    		deptSize = false;
	    	}
    	} 
    	return deptSize;
	}
	
	/**
	 * @return Returns the rooms.
	 */
	public Collection getRooms() {
		return rooms;
	}
	/**
	 * @param rooms The rooms to set.
	 */
	public void setRooms(Collection rooms) {
		this.rooms = rooms;
	}
	
	/* (non-Javadoc)
     * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
      
        if(deptCode==null || deptCode.equalsIgnoreCase("")) {
        	errors.add("deptCode", 
                    new ActionMessage("errors.required", "Department") );
        }
       
        return errors;
    }
    
	public boolean isDeptSize() {
		return deptSize;
	}

	public void setCanAdd(boolean canAdd) {
		this.canAdd = canAdd;
	}
	public boolean getCanAdd() {
		return canAdd;
	}
}