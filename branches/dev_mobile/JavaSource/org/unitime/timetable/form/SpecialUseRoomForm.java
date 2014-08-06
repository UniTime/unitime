/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;


/** 
 * MyEclipse Struts
 * Creation date: 05-05-2006
 * 
 * XDoclet definition:
 * @struts.form name="specialUseRoomForm"
 */
public class SpecialUseRoomForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables
	private String doit;
	private String bldgId;
	private String roomNum;
	private int deptSize;
	private boolean ignoreTooFar;
	private boolean ignoreRoomCheck;
	private String deptCode;
	
	// --------------------------------------------------------- Methods

	/**
	 * 
	 */
	private static final long serialVersionUID = 3491038065331964669L;

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

		if (deptSize != 1) {
			if (deptCode== null || deptCode.equals("")) {
				errors.add("dept",
						new ActionMessage("errors.required", "Department"));
			}
		}

        if(bldgId==null || bldgId.equalsIgnoreCase("")) {
        	errors.add("bldg", 
                    new ActionMessage("errors.required", "Building") );
        }
        
        if(roomNum==null || roomNum.equalsIgnoreCase("")) {
        	errors.add("roomNum", 
                    new ActionMessage("errors.required", "Room Number") );
        }
        
        return errors;
        
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		ignoreTooFar=false;
		ignoreRoomCheck=false;
	}

	public String getBldgId() {
		return bldgId;
	}

	public void setBldgId(String bldgId) {
		this.bldgId = bldgId;
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
	

	public String getRoomNum() {
		return roomNum;
	}

	public void setRoomNum(String roomNum) {
		this.roomNum = roomNum;
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

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

}

