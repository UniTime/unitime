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
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;


/** 
 * MyEclipse Struts
 * Creation date: 10-14-2005
 * 
 * XDoclet definition:
 * @struts:form name="instructorSearchForm"
 *
 * @author Zuzana Mullerova
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

