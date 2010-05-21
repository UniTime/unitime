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
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 10-14-2005
 * 
 * XDoclet definition:
 * @struts:form name="instructorSearchForm"
 */
public class InstructorSearchForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables

	/**
	 * 
	 */
	private static final long serialVersionUID = -5750116865914272048L;

	/** deptCode property */
	private String deptUniqueId;
	
	/** displayDeptList property */
	private boolean displayDeptList;
	
	/** op property */
	private String op;
	
	/** admin property */
	private String admin;
	
	private boolean editable;
	// --------------------------------------------------------- Methods

	/** 
	 * Returns the deptUniqueId.
	 * @return String
	 */
	public String getDeptUniqueId() {
		return deptUniqueId;
	}

	/** 
	 * Set the deptUniqueId.
	 * @param deptUniqueId The deptUniqueId to set
	 */
	public void setDeptUniqueId(String deptUniqueId) {
		this.deptUniqueId = deptUniqueId;
	}

	/**
	 * 
	 * @return
	 */
	public String getOp() {
		return op;
	}

	/**
	 * 
	 * @param op
	 */
	public void setOp(String op) {
		this.op = op;
	}

	/**
	 * 
	 * @return
	 */
	public String getAdmin() {
		return admin;
	}

	/**
	 * 
	 * @param admin
	 */
	public void setAdmin(String admin) {
		this.admin = admin;
	}
	
	// --------------------------------------------------------- Methods
	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		deptUniqueId = "";
		editable = false;
		displayDeptList=false;
		HttpSession httpSession = request.getSession();
		if (Web.isLoggedIn(httpSession)) {
			displayDeptList = displayDeptList(request);
		}
	}
	
	/**
	 * this function is used to determin whether there should be a dropdown list for departments on the instructor search page.
	 * if deptSize is 1, no dropdown list; otherwise, there should be a list.
	 * @param request
	 * @return
	 */
    public boolean displayDeptList(HttpServletRequest request) {
    	setDisplayDeptList(true);
    	User user = Web.getUser(request.getSession());
    	Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
    	if (!user.getRole().equals(Roles.ADMIN_ROLE) && !user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE) && !user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE)) {
	    	TimetableManager mgr = TimetableManager.getManager(user);
	    	Set mgrDepts = mgr.departmentsForSession(sessionId);
	    	if (mgrDepts.size() == 1){
	    		setDisplayDeptList(false);
	    	}
    	} 
    	return isDisplayDeptList();
	}

	/* (non-Javadoc)
     * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
      
        if(deptUniqueId==null || deptUniqueId.equalsIgnoreCase("")) {
        	errors.add("deptUniqueId", 
                    new ActionMessage("errors.required", "Department") );
        }
       
        return errors;
    }

	public boolean isDisplayDeptList() {
		return displayDeptList;
	}

	public void setDisplayDeptList(boolean displayDeptList) {
		this.displayDeptList = displayDeptList;
	}
	
	public void setEditable(boolean editable) { this.editable = editable; }
	public boolean isEditable() { return editable; }
	
}

