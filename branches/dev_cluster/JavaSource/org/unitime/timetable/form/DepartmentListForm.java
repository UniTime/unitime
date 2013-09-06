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
