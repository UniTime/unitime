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
 *
 * @author Tomas Muller, Zuzana Mullerova
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
