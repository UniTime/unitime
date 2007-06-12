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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


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
		setDeptSize(request);
		ignoreTooFar=false;
		ignoreRoomCheck=false;
	}
	
	/**
	 * 
	 * @param request
	 */
	private void setDeptSize(HttpServletRequest request) {
		HttpSession httpSession = request.getSession();
		User user = Web.getUser(httpSession);
		Long sessionId;
		try {
			sessionId = Session.getCurrentAcadSession(user).getUniqueId();
			String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
			TimetableManagerDAO tdao = new TimetableManagerDAO();
	        TimetableManager manager = tdao.get(new Long(mgrId));
	        Set departments = manager.departmentsForSession(sessionId);
	        setDeptSize(departments.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
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
	
	/**
	 * 
	 * @param deptCode
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public String getDeptName(String deptCode, HttpServletRequest request) throws Exception {
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();	
		return Department.findByDeptCode(deptCode, sessionId).getName();		
	}

}

