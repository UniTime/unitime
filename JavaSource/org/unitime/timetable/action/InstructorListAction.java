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

import java.util.Vector;

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
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.InstructorSearchForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.InstructorListBuilder;
import org.unitime.timetable.webutil.PdfWebTable;


/**
 * MyEclipse Struts Creation date: 10-14-2005
 * 
 * XDoclet definition:
 * 
 * @struts:action path="/instructorList" name="instructorSearchForm"
 *                input="/user/instructorList.jsp" parameter="op"
 *                scope="request" validate="true"
 * @struts:action-forward name="showInstructorList" path="instructorListTile"
 *
 * @author Tomas Muller, Zuzana Mullerova
 */
@Service("/instructorList")
public class InstructorListAction extends Action {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;

	// --------------------------------------------------------- Instance
	// Variables

	// --------------------------------------------------------- Methods

	/**
	 * Method execute
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// Check permissions
		sessionContext.checkPermission(Right.Instructors);

		InstructorSearchForm instructorSearchForm = (InstructorSearchForm) form;
		ActionMessages errors = new ActionMessages();

		// Check if to return to search page
		String op = instructorSearchForm.getOp();

/* Suspected unused code
		if (op != null && op.equalsIgnoreCase("Back to Search")) {
			return mapping.findForward("showInstructorSearch");
		}
*/	
		
		//get deptCode from request - for user with only one department
		String deptId = (String)request.getAttribute("deptId");
		if (deptId != null) {
			instructorSearchForm.setDeptUniqueId(deptId);
		}

		// Set Form Variable
		if (sessionContext.getAttribute(SessionAttribute.DepartmentId) != null
				&& ( sessionContext.getAttribute(SessionAttribute.DepartmentId).equals(instructorSearchForm.getDeptUniqueId())
				     || instructorSearchForm.getDeptUniqueId().equalsIgnoreCase("") ) ) {
			instructorSearchForm.setDeptUniqueId(sessionContext.getAttribute(SessionAttribute.DepartmentId).toString());
		}

		// Set Session Variable
		if (!instructorSearchForm.getDeptUniqueId().equalsIgnoreCase("")) {
			sessionContext.setAttribute(SessionAttribute.DepartmentId, instructorSearchForm.getDeptUniqueId());
		}

		if (request.getAttribute(Department.DEPT_ATTR_NAME)!=null) {
		    request.setAttribute(Department.DEPT_ATTR_NAME,	request.getAttribute(Department.DEPT_ATTR_NAME));		    
		} else {
		    setupManagerDepartments(request);
		}
		
		// Validate input
		errors = instructorSearchForm.validate(mapping, request);
		
		// Validation fails
		if (errors.size() > 0) {
			saveErrors(request, errors);
			return mapping.findForward("showInstructorSearch");
		}

		WebTable.setOrder(sessionContext,"instructorList.ord",request.getParameter("order"),2);

		InstructorListBuilder ilb = new InstructorListBuilder();
		String backId = ("PreferenceGroup".equals(request.getParameter("backType"))?request.getParameter("backId"):null);
		String tblData = ilb.htmlTableForInstructor(sessionContext, instructorSearchForm.getDeptUniqueId(), WebTable.getOrder(sessionContext,"instructorList.ord"), backId);
		if (tblData == null || tblData.trim().isEmpty()) {
			errors.add("searchResult", new ActionMessage("errors.generic", MSG.errorNoInstructorsFoundInSearch()));
			saveErrors(request, errors);
		} else {
			if (MSG.actionExportPdf().equals(op)) {
				PdfWebTable table = ilb.pdfTableForInstructor(sessionContext, instructorSearchForm.getDeptUniqueId());
				if (table != null) {
					ExportUtils.exportPDF(table, WebTable.getOrder(sessionContext,"instructorList.ord"), response, "instructors");
					return null;
				}
			}
		}
		
		if (deptId!=null) {
			Department d = (new DepartmentDAO()).get(Long.valueOf(deptId));
			if (d!=null) {
				BackTracker.markForBack(
						request,
						"instructorList.do?deptId="+d.getUniqueId(),
						MSG.backInstructors(d.getDeptCode()+" - "+d.getName()),
						true, true
						);
			}
		} else if (sessionContext.getAttribute(SessionAttribute.DepartmentId) != null) {
			Department d = (new DepartmentDAO()).get(Long.valueOf(sessionContext.getAttribute(SessionAttribute.DepartmentId).toString()));
			if (d!=null) {
				BackTracker.markForBack(
						request,
						"instructorList.do?deptId="+d.getUniqueId(),
						MSG.backInstructors(d.getDeptCode()+" - "+d.getName()),
						true, true
						);
			}
		} else {
			BackTracker.markForBack(
					request,
					"instructorList.do",
					MSG.backInstructors2(),
					true, true
					);
		}
		
		request.setAttribute("instructorList", tblData);
		saveErrors(request, errors);
		return mapping.findForward("showInstructorList");
	}

    /**
     * @return
     */
    private void setupManagerDepartments(HttpServletRequest request) throws Exception{
    	
		Vector<LabelValueBean> labelValueDepts = new Vector<LabelValueBean>();

		for (Department d: Department.getUserDepartments(sessionContext.getUser())) {
			labelValueDepts.add(
			        new LabelValueBean(
			                d.getDeptCode() + "-" + d.getName(),
			                d.getUniqueId().toString() ) );
		}
		
		if (labelValueDepts.size() == 1)
			request.setAttribute("deptId", labelValueDepts.get(0).getValue());

		request.setAttribute(Department.DEPT_ATTR_NAME,labelValueDepts);
    }

    
}
