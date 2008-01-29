/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.action;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.InstructorSearchForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.InstructorListBuilder;


/**
 * MyEclipse Struts Creation date: 10-14-2005
 * 
 * XDoclet definition:
 * 
 * @struts:action path="/instructorList" name="instructorSearchForm"
 *                input="/user/instructorList.jsp" parameter="op"
 *                scope="request" validate="true"
 * @struts:action-forward name="showInstructorList" path="instructorListTile"
 */
public class InstructorListAction extends Action {

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
		HttpSession httpSession = request.getSession();
		if (!Web.isLoggedIn(httpSession)) {
			throw new Exception("Access Denied.");
		}

		InstructorSearchForm instructorSearchForm = (InstructorSearchForm) form;
		ActionMessages errors = new ActionMessages();

		// Check if to return to search page
		String op = instructorSearchForm.getOp();
		if (op != null && op.equalsIgnoreCase("Back to Search")) {
			return mapping.findForward("showInstructorSearch");
		}
		
		//get deptCode from request - for user with only one department
		String deptId = (String)request.getAttribute("deptId");
		if (deptId != null) {
			instructorSearchForm.setDeptUniqueId(deptId);
		}

		// Set Form Variable
		if (httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME) != null
				&& ( httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME).equals(instructorSearchForm.getDeptUniqueId())
				     || instructorSearchForm.getDeptUniqueId().equalsIgnoreCase("") ) ) {
			instructorSearchForm.setDeptUniqueId(
			        httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME).toString());
		}

		// Set Session Variable
		if (!instructorSearchForm.getDeptUniqueId().equalsIgnoreCase("")) {
			httpSession.setAttribute(
			        Constants.DEPT_ID_ATTR_NAME,
					instructorSearchForm.getDeptUniqueId());
		}

		if (request.getAttribute(Department.DEPT_ATTR_NAME)!=null) {
		    request.setAttribute(Department.DEPT_ATTR_NAME,	request.getAttribute(Department.DEPT_ATTR_NAME));		    
		}
		else {
		    setupManagerDepartments(request);
		}
		
		// Validate input
		errors = instructorSearchForm.validate(mapping, request);

		// Validation fails
		if (errors.size() > 0) {
			saveErrors(request, errors);
			return mapping.findForward("showInstructorSearch");
		}

		User user = Web.getUser(httpSession);
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		
		if (!user.isAdmin()) {
			TimetableManager mgr = TimetableManager.getManager(user);
			Set mgrDepts = mgr.departmentsForSession(sessionId);
		}
		
		if (Web.hasRole(httpSession, Roles.getAdminRoles())) {
			instructorSearchForm.setAdmin("Y");
		} else {
			instructorSearchForm.setAdmin("N");
		}
		
		WebTable.setOrder(request.getSession(),"instructorList.ord",request.getParameter("order"),2);

		InstructorListBuilder ilb = new InstructorListBuilder();
		String backId = ("PreferenceGroup".equals(request.getParameter("backType"))?request.getParameter("backId"):null);
		String tblData = ilb.htmlTableForInstructor(request, instructorSearchForm.getDeptUniqueId(), WebTable.getOrder(request.getSession(),"instructorList.ord"), backId);
		if (tblData==null || tblData.trim().length()==0) {
			errors.add(
			        "searchResult", 
			        new ActionMessage(
			                "errors.generic",
			                "No instructors were found. Use the option 'Manage Instructor List' to add instructors to your list."));
			saveErrors(request, errors);
		} else {
			if ("Export PDF".equals(op)) {
				ilb.pdfTableForInstructor(request, instructorSearchForm.getDeptUniqueId(), WebTable.getOrder(request.getSession(),"instructorList.ord"));
			}
		}
		
		if (deptId!=null) {
			Department d = (new DepartmentDAO()).get(Long.valueOf(deptId));
			if (d!=null) {
				instructorSearchForm.setEditable(d.isEditableBy(user));
				BackTracker.markForBack(
						request,
						"instructorList.do?deptId="+d.getUniqueId(),
						"Instructors ("+d.getDeptCode()+" - "+d.getName()+")",
						true, true
						);
			}
		} else if (httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME) != null) {
			Department d = (new DepartmentDAO()).get(Long.valueOf(httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME).toString()));
			if (d!=null) {
				instructorSearchForm.setEditable(d.isEditableBy(user));
				BackTracker.markForBack(
						request,
						"instructorList.do?deptId="+d.getUniqueId(),
						"Instructors ("+d.getDeptCode()+" - "+d.getName()+")",
						true, true
						);
			}
		} else {
			BackTracker.markForBack(
					request,
					"instructorList.do",
					"Instructors",
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
        Set mgrDepts = null;
        
		User user = Web.getUser(request.getSession());
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);

		if (user.isAdmin() || user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE)) {
		    mgrDepts = Department.findAllBeingUsed(sessionId);
		}
		else {
			// Get manager info
			TimetableManager mgr = TimetableManager.getManager(user);
			mgrDepts = new TreeSet(mgr.departmentsForSession(sessionId));
		}
		
		//get depts owned by user and forward to the appropriate page
		if (mgrDepts.size() == 0) {
			throw new Exception(
					"You do not have any department to manage. ");
		} 
		
		Vector labelValueDepts = new Vector();
		for (Iterator it = mgrDepts.iterator(); it.hasNext();) {
			Department d = (Department) it.next();
			labelValueDepts.add(
			        new LabelValueBean(
			                d.getDeptCode() + "-" + d.getName(),
			                d.getUniqueId().toString() ) );
			if (mgrDepts.size() == 1) {
				request.setAttribute("deptId", d.getUniqueId().toString());
			} 

		}
		
		request.setAttribute(Department.DEPT_ATTR_NAME,labelValueDepts);
    }

    
}
