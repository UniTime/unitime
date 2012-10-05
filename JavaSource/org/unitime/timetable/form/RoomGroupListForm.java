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
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

/** 
 * MyEclipse Struts
 * Creation date: 05-02-2006
 * 
 * XDoclet definition:
 * @struts.form name="roomGroupListForm"
 */
public class RoomGroupListForm extends ActionForm {

	private static final long serialVersionUID = -4644102545584357862L;
	
	// --------------------------------------------------------- Instance Variables

	private Collection roomGroups;
	private String deptCode;
	// --------------------------------------------------------- Methods

	/**
	 * 
	 */
	

	public String getDeptCodeX() {
		return deptCode;
	}

	public void setDeptCodeX(String deptCode) {
		this.deptCode = deptCode;
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
        
        if(deptCode==null || deptCode.equalsIgnoreCase("")) {
        	errors.add("deptCode", 
                    new ActionMessage("errors.required", "Department") );
        }
        
        return errors;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		roomGroups = new ArrayList();
	}

	public void setRoomGroups(Collection roomGroups) {
		this.roomGroups = roomGroups;
	}

	public Collection getRoomGroups() {
		return roomGroups;
	}
}

