/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 07-18-2006
 * 
 * XDoclet definition:
 * @struts.form name="updateInstructorListForm"
 */
public class InstructorListUpdateForm extends ActionForm {

	// --------------------------------------------------------- Instance Variables
	private String deptCode;
	private String deptName;
	private String op;
	private Collection assignedInstr;
	private Collection availableInstr;
	private String[] assigned;
	private String[] available;
	private String[] assignedSelected = {};
	private String[] availableSelected = {};
	private String nameFormat;
	
	// Filter settings
	private String displayListType;
	private String[] displayPosType;
	
	// --------------------------------------------------------- Methods

	/**
	 * 
	 */
	private static final long serialVersionUID = 6131705766470532900L;

	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(
		ActionMapping mapping,
		HttpServletRequest request) {

		return null;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request){
		setDeptName(request);
	}

	/**
	 * 
	 * @param request
	 */
	private void setDeptName(HttpServletRequest request){
		HttpSession httpSession = request.getSession();
		
		if (httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME) != null) {
			String deptId = (String) httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME);
			try {
				Department d = new DepartmentDAO().get(new Long(deptId));
				if (d != null) {
					setDeptName(d.getName().trim());
				}
			} catch (Exception e) {
			    Debug.error(e);
			}			
		}		
	}

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String[] getAssigned() {
		return assigned;
	}

	public void setAssigned(String[] assigned) {
		this.assigned = assigned;
	}

	public Collection getAssignedInstr() {
		return assignedInstr;
	}

	public void setAssignedInstr(Collection assignedInstr) {
		this.assignedInstr = assignedInstr;
	}

	public String[] getAssignedSelected() {
		return assignedSelected;
	}

	public void setAssignedSelected(String[] assignedSelected) {
		this.assignedSelected = assignedSelected;
	}

	public String[] getAvailable() {
		return available;
	}

	public void setAvailable(String[] available) {
		this.available = available;
	}

	public Collection getAvailableInstr() {
		return availableInstr;
	}

	public void setAvailableInstr(Collection availableInstr) {
		this.availableInstr = availableInstr;
	}

	public String[] getAvailableSelected() {
		return availableSelected;
	}

	public void setAvailableSelected(String[] availableSelected) {
		this.availableSelected = availableSelected;
	}
	
    public String getDisplayListType() {
        return displayListType;
    }
    
    public void setDisplayListType(String displayListType) {
        this.displayListType = displayListType;
    }
    
    public String[] getDisplayPosType() {
        return displayPosType;
    }
    
    public void setDisplayPosType(String[] displayPosType) {
        this.displayPosType = displayPosType;
    }
    
	public void setInstructors() {
		int i = 0;
		Iterator iter = null;

		if (assignedInstr != null) {
			String[] assignedSelection = new String[assignedInstr.size()];
			assigned = new String[assignedInstr.size()];
			for (iter = assignedInstr.iterator(); iter.hasNext();) {
				DepartmentalInstructor inst = (DepartmentalInstructor) iter.next();
				assignedSelection[i] = inst.getUniqueId().toString();
				assigned[i] = inst.getUniqueId().toString();
				i++;
			}
			assignedSelected = assignedSelection;
		}

		i = 0;
		if (availableInstr != null) {
			available = new String[availableInstr.size()];
			for (iter = availableInstr.iterator(); iter.hasNext();) {
				Staff s = (Staff) iter.next();
				available[i] = s.getUniqueId().toString();
				i++;
			}
		}
		
	}
	
	public String getNameFormat() { return nameFormat; }
	public void setNameFormat(String nameFormat) { this.nameFormat = nameFormat; }

}

