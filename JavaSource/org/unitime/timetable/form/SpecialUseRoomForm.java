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

