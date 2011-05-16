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

