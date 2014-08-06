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
package org.unitime.timetable.action;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ClassAssignmentsReportForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.webutil.BackTracker;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
@Service("/classAssignmentsReportShowSearch")
public class ClassAssignmentsReportShowSearchAction extends Action {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;

	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
    	sessionContext.checkPermission(Right.ClassAssignments);
        
	    ClassAssignmentsReportForm classListForm = (ClassAssignmentsReportForm)form;

	    request.setAttribute(Department.EXTERNAL_DEPT_ATTR_NAME, Department.findAllExternal(sessionContext.getUser().getCurrentAcademicSessionId()));
	    
	    ClassAssignmentsReportSearchAction.setupGeneralFormFilters(sessionContext.getUser(), classListForm);
	    
		if (request.getParameter("sortBy") != null) {
			classListForm.setSortBy((String)request.getParameter("sortBy"));
			classListForm.setFilterAssignedRoom((String)request.getParameter("filterAssignedRoom"));
			//classListForm.setFilterInstructor((String)request.getParameter("filterInstructor"));
			classListForm.setFilterManager((String)request.getParameter("filterManager"));
			classListForm.setFilterIType((String)request.getParameter("filterIType"));
			classListForm.setFilterAssignedTimeMon(request.getParameter("filterAssignedTimeMon")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeMon")));
			classListForm.setFilterAssignedTimeTue(request.getParameter("filterAssignedTimeTue")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeTue")));
			classListForm.setFilterAssignedTimeWed(request.getParameter("filterAssignedTimeWed")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeWed")));
			classListForm.setFilterAssignedTimeThu(request.getParameter("filterAssignedTimeThu")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeThu")));
			classListForm.setFilterAssignedTimeFri(request.getParameter("filterAssignedTimeFri")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeFri")));
			classListForm.setFilterAssignedTimeSat(request.getParameter("filterAssignedTimeSat")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeSat")));
			classListForm.setFilterAssignedTimeSun(request.getParameter("filterAssignedTimeSun")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeSun")));
			classListForm.setFilterAssignedTimeHour((String)request.getParameter("filterAssignedTimeHour"));
			classListForm.setFilterAssignedTimeMin((String)request.getParameter("filterAssignedTimeMin"));
			classListForm.setFilterAssignedTimeAmPm((String)request.getParameter("filterAssignedTimeAmPm"));
			classListForm.setFilterAssignedTimeLength((String)request.getParameter("filterAssignedTimeLength"));
			classListForm.setSortByKeepSubparts(Boolean.getBoolean((String)request.getParameter("sortByKeepSubparts")));
		}
		
		classListForm.setSubjectAreas(SubjectArea.getAllSubjectAreas(sessionContext.getUser().getCurrentAcademicSessionId()));

		Object sas = sessionContext.getAttribute(SessionAttribute.ClassAssignmentsSubjectAreas);
	    if(sas!=null && sas.toString().trim().length() > 0) {
	    	String subjectAreaIds = sas.toString();
	        try {
	        	
		        Debug.debug("Subject Areas: " + subjectAreaIds);
		        
		        
		        classListForm.setSubjectAreaIds(subjectAreaIds.split(","));
		        
				classListForm.setClasses(ClassSearchAction.getClasses(classListForm, classAssignmentService.getAssignment()));
				Collection classes = classListForm.getClasses();
				if (classes.isEmpty()) {
					    ActionMessages errors = new ActionMessages();
					    errors.add("searchResult", new ActionMessage("errors.generic", "No records matching the search criteria were found."));
					    saveErrors(request, errors);
					    return mapping.findForward("showClassAssignmentsReportSearch");
				} else {
					StringBuffer ids = new StringBuffer();
					StringBuffer names = new StringBuffer();
					for (int i=0;i<classListForm.getSubjectAreaIds().length;i++) {
						if (i>0) names.append(","); 
						ids.append("&subjectAreaIds="+classListForm.getSubjectAreaIds()[i]);
						names.append(((new SubjectAreaDAO()).get(new Long(classListForm.getSubjectAreaIds()[i]))).getSubjectAreaAbbreviation());
					}
					BackTracker.markForBack(
							request, 
							"classAssignmentsReportSearch.do?doit=Search&loadFilter=1"+ids, 
							"Class Assignments ("+names+")", 
							true, true);
					return mapping.findForward("showClassAssignmentsReportList");
				}
	        } catch (NumberFormatException nfe) {
	        	Debug.error("Subject Area Ids session attribute is corrupted. Resetting ... ");
		        sessionContext.removeAttribute(SessionAttribute.ClassAssignmentsSubjectAreas);
		    }
	    }

		return mapping.findForward("showClassAssignmentsReportSearch");
	}

}
