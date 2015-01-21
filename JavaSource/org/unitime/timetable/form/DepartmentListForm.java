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

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/** 
* MyEclipse Struts
* Creation date: 02-18-2005
* 
* XDoclet definition:
* @struts:form name="departmentListForm"
*/
/**
 * @author Tomas Muller
 */
public class DepartmentListForm extends ActionForm {
    private String iOp = null;
    private boolean iShowUnusedDepts = false;

	/**
	 * 
	 */
	private static final long serialVersionUID = 3976735861213638709L;
	// --------------------------------------------------------- Instance Variables
	private Collection departments;
	
	// --------------------------------------------------------- Methods

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		departments = new ArrayList();
		iShowUnusedDepts=false;
		iOp = null;
	}
	
	/**
	 * @return Returns the departments.
	 */
	public Collection getDepartments() {
		return departments;
	}
	/**
	 * @param departments The departments to set.
	 */
	public void setDepartments(Collection departments) {
		this.departments = departments;
	}
	
	public boolean getShowUnusedDepts() { return iShowUnusedDepts; }
	public void setShowUnusedDepts(boolean showUnusedDepts) { iShowUnusedDepts = showUnusedDepts; }
    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
}
