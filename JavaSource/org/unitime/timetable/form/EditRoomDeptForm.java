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

import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.form name="editRoomDeptForm"
 */
public class EditRoomDeptForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2305279204925700618L;
	// --------------------------------------------------------- Instance Variables
	private String id;
	private String doit;
	private String name;
	private String sharingTable;
	private boolean nonUniv;
	private String dept;
	private String departments;

	// --------------------------------------------------------- Methods

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}


	public boolean isNonUniv() {
		return nonUniv;
	}

	public void setNonUniv(boolean nonUniv) {
		this.nonUniv = nonUniv;
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
        return errors;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		departments = "";
	}

	public String getDoit() {
		return doit;
	}

	public void setDoit(String doit) {
		this.doit = doit;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSharingTable() {
		return sharingTable;
	}

	public void setSharingTable(String sharingTable) {
		this.sharingTable = sharingTable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDepartments() { return departments; }
	public void setDepartments(String departments) { this.departments = departments; }
	public void addDepartment(String department) {
		if (departments.length()>0)
			departments += ",";
		departments += department;
	}
	public void removeDepartment(String department) {
		String newDepartments = "";
		StringTokenizer stk = new StringTokenizer(departments, ",");
		while (stk.hasMoreTokens()) {
			String dept = stk.nextToken();
			if (!dept.equals(department)) {
				if (newDepartments.length()>0) newDepartments += ",";
				newDepartments += dept;
			}
		}
		departments = newDepartments;
	}
	
	public Vector getDepartmentIds() {
		Vector ids = new Vector();
		StringTokenizer stk = new StringTokenizer(departments, ",");
		while (stk.hasMoreTokens()) {
			ids.addElement(new Long(stk.nextToken()));
		}
		return ids;
	}

}

