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
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.InstructorSearchForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * MyEclipse Struts
 * Creation date: 10-14-2005
 * 
 * XDoclet definition:
 * @struts:action path="/instructorSearch" name="instructorSearchForm" input="/user/instructorSearch.jsp" scope="request" validate="true"
 * @struts:action-forward name="showInstructorSearch" path="instructorSearchTile"
 */
@Service("/instructorSearch")
public class InstructorSearchAction extends Action {

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;
	
	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		sessionContext.checkPermission(Right.Instructors);

		InstructorSearchForm instructorSearchForm = (InstructorSearchForm) form;

		Set<Department> departments = setupManagerDepartments(request);

		// Dept code is saved to the session - go to instructor list
		String deptId = (String)sessionContext.getAttribute(SessionAttribute.DepartmentId);
		
		if ((deptId == null || deptId.isEmpty()) && departments.size() == 1)
			deptId = departments.iterator().next().getUniqueId().toString();
		
		if (deptId == null || deptId.isEmpty() || !sessionContext.hasPermission(deptId, "Department", Right.Instructors)) {
			return mapping.findForward("showInstructorSearch");
		} else {
			instructorSearchForm.setDeptUniqueId(deptId);
			List instructors = DepartmentalInstructor.findInstructorsForDepartment(Long.valueOf(deptId));
			if (instructors == null || instructors.isEmpty()) {
				ActionMessages errors = new ActionMessages();
				errors.add("searchResult", new ActionMessage("errors.generic", MSG.errorNoInstructorsFoundForDepartment()));
				saveErrors(request, errors);
			}
			return mapping.findForward("instructorList");
		}
	}

	
    /**
     * @return
     */
    private Set<Department> setupManagerDepartments(HttpServletRequest request) throws Exception{
    	Set<Department> departments = Department.getUserDepartments(sessionContext.getUser());

		if (departments.isEmpty())
			throw new Exception(MSG.exceptionNoDepartmentToManage());
		
		List<LabelValueBean> labelValueDepts = new ArrayList<LabelValueBean>();
		for (Department d: departments)
			labelValueDepts.add(new LabelValueBean(d.getDeptCode() + "-" + d.getName(), d.getUniqueId().toString()));
		
		if (labelValueDepts.size() == 1)
			request.setAttribute("deptId", labelValueDepts.get(0).getValue());
		
		request.setAttribute(Department.DEPT_ATTR_NAME,labelValueDepts);
		
		return departments;
    }

}
