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
import java.util.List;
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
import org.unitime.timetable.form.InstructorSearchForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 10-14-2005
 * 
 * XDoclet definition:
 * @struts:action path="/instructorSearch" name="instructorSearchForm" input="/user/instructorSearch.jsp" scope="request" validate="true"
 * @struts:action-forward name="showInstructorSearch" path="instructorSearchTile"
 */
public class InstructorSearchAction extends Action {

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

		//Check permissions
		HttpSession httpSession = request.getSession();
		if (!Web.isLoggedIn(httpSession)) {
			throw new Exception("Access Denied.");
		}		

		InstructorSearchForm instructorSearchForm = (InstructorSearchForm) form;

		User user = Web.getUser(httpSession);
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		Set mgrDepts = setupManagerDepartments(request);

		// Dept code is saved to the session - go to instructor list
		Object dc = httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME);
		String deptId= "";

		if (dc != null && !dc.toString().equals(Constants.BLANK_OPTION_VALUE)) {
		    boolean allowed = false;
		    deptId = dc.toString();
			if (mgrDepts!=null && mgrDepts.size()>0 && !user.isAdmin()) {
			    for (Iterator i=mgrDepts.iterator(); i.hasNext(); ) {
			        Department d = (Department) i.next();
			        if (d.getUniqueId().toString().equals(deptId)) {
			            allowed = true;
			            break;
			        }
			    }
			}
		    
			if (user.isAdmin() || allowed) {
			    getInstructorList(instructorSearchForm, request, deptId, sessionId);
			    return mapping.findForward("instructorList");
			}
		}

		// No session attribute found - Load dept code
		else {
		    if (mgrDepts.size()==1) {
		        Department d = (Department) mgrDepts.iterator().next();
		        deptId = d.getUniqueId().toString();
		        httpSession.setAttribute(Constants.DEPT_ID_ATTR_NAME, deptId);
			    getInstructorList(instructorSearchForm, request, deptId, sessionId);
			    return mapping.findForward("instructorList");
		    }
		}

		return mapping.findForward("showInstructorSearch");
	}

	private void getInstructorList(
	        InstructorSearchForm instructorSearchForm,
	        HttpServletRequest request,
	        String deptId, 
	        Long sessionId ) throws Exception {
		instructorSearchForm.setDeptUniqueId(deptId);
		List v = DepartmentalInstructor.getInstructorByDept(sessionId, new Long(deptId));
		
		if (v==null || v.size()==0) {
			ActionMessages errors = new ActionMessages();
			errors.add("searchResult", 
			        	new ActionMessage(
			        	        "errors.generic",
			        	        "No instructors for the selected department were found."));
			saveErrors(request, errors);
		}
	}
	
    /**
     * @return
     */
    private Set setupManagerDepartments(HttpServletRequest request) throws Exception{
        Set mgrDepts = null;
        
		User user = Web.getUser(request.getSession());
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);

		if (user.isAdmin() || user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE) || user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE)) {
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
		return mgrDepts;
    }

}
