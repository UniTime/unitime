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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

/**
 * MyEclipse Struts
 * Creation date: 03-17-2005
 *
 * XDoclet definition:
 * @struts:form name="roleListForm"
 */
public class RoleListForm extends ActionForm {

    // --------------------------------------------------------- Instance Variables

    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3546920294733526840L;

	/** primaryRole property */
    private String primaryRole;

    /** action property */
    private String action;

    // --------------------------------------------------------- Methods

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

		// Check Primary Role Selected
		if(action!=null && action.trim().equals("selectRole")
		        && (primaryRole==null || primaryRole.trim().length()==0) )
			errors.add("primaryRole", new ActionMessage("errors.lookup.primaryRole.required"));

		return errors;
    }

    /**
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        primaryRole = "";
        action = "";
    }

     /**
     * Returns the primaryRole.
     * @return String
     */
    public String getPrimaryRole() {
        return primaryRole;
    }

    /**
     * Set the primaryRole.
     * @param primaryRole The primaryRole to set
     */
    public void setPrimaryRole(String primaryRole) {
        this.primaryRole = primaryRole;
    }

    public void setPrimaryRole(String primaryRole, Object yrTerm) {
        if(yrTerm!=null && yrTerm.toString().trim().length()>0)
            this.primaryRole = yrTerm.toString().trim()  + "-" + primaryRole;
        else
            this.primaryRole = primaryRole;
    }

    /**
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }
    /**
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }
}