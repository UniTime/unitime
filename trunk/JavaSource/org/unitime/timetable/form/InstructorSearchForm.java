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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;


/** 
 * MyEclipse Struts
 * Creation date: 10-14-2005
 * 
 * XDoclet definition:
 * @struts:form name="instructorSearchForm"
 */
public class InstructorSearchForm extends ActionForm {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	// --------------------------------------------------------- Instance Variables

	/**
	 * 
	 */
	private static final long serialVersionUID = -5750116865914272048L;

	/** deptCode property */
	private String deptUniqueId;
	
	/** op property */
	private String op;
	
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


	
	// --------------------------------------------------------- Methods
	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		deptUniqueId = "";
	}
	

	/* (non-Javadoc)
     * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
      
        if(deptUniqueId==null || deptUniqueId.equalsIgnoreCase("")) {
        	errors.add("deptUniqueId", 
                    new ActionMessage("errors.generic", MSG.errorRequiredDepartment()) );
        }
       
        return errors;
    }
	
}

