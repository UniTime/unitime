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

import java.io.OutputStream;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.impl.LocalizedLookupDispatchAction;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.Messages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ClassAssignmentsReportForm;
import org.unitime.timetable.form.ClassListFormInterface;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.CsvClassAssignmentExport;
import org.unitime.timetable.webutil.pdf.PdfClassAssignmentReportListTableBuilder;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
@Service("/classAssignmentsReportSearch")
public class ClassAssignmentsReportSearchAction extends LocalizedLookupDispatchAction {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	private void initializeFilters(HttpServletRequest request, ClassAssignmentsReportForm classListForm){
	    if ("1".equals(request.getParameter("loadFilter"))) {
            ClassAssignmentsReportSearchAction.setupGeneralFormFilters(sessionContext.getUser(), classListForm);
	    } else {
	    	sessionContext.getUser().setProperty("ClassAssignments.sortByKeepSubparts", classListForm.getSortByKeepSubparts() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassAssignments.sortBy", classListForm.getSortBy());
	    	sessionContext.getUser().setProperty("ClassAssignments.filterAssignedRoom", classListForm.getFilterAssignedRoom());		    	
	    	//sessionContext.getUser().setProperty("ClassAssignments.filterInstructor", classListForm.getFilterInstructor());		    	
	    	sessionContext.getUser().setProperty("ClassAssignments.filterManager", classListForm.getFilterManager());		
	    	sessionContext.getUser().setProperty("ClassAssignments.filterIType", classListForm.getFilterIType());
	    	sessionContext.getUser().setProperty("ClassAssignments.filterDayCode", String.valueOf(classListForm.getFilterDayCode()));
	    	sessionContext.getUser().setProperty("ClassAssignments.filterStartSlot", String.valueOf(classListForm.getFilterStartSlot()));
	    	sessionContext.getUser().setProperty("ClassAssignments.filterLength", String.valueOf(classListForm.getFilterLength()));
	    	sessionContext.getUser().setProperty("ClassAssignments.showCrossListedClasses", String.valueOf(classListForm.getShowCrossListedClasses()));
	    }

	}
	
    
    public static void setupGeneralFormFilters(UserContext user, ClassListFormInterface form){
        form.setSortBy(user.getProperty("ClassAssignments.sortBy", ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)));
        form.setFilterAssignedRoom(user.getProperty("ClassAssignments.filterAssignedRoom", ""));
        form.setFilterManager(user.getProperty("ClassAssignments.filterManager", ""));
        form.setFilterIType(user.getProperty("ClassAssignments.filterIType", ""));
        form.setFilterDayCode(Integer.valueOf(user.getProperty("ClassAssignments.filterDayCode", "-1")));
        form.setFilterStartSlot(Integer.valueOf(user.getProperty("ClassAssignments.filterStartSlot", "-1")));
        form.setFilterLength(Integer.valueOf(user.getProperty("ClassAssignments.filterLength", "-1")));
        form.setSortByKeepSubparts("1".equals(user.getProperty("ClassAssignments.sortByKeepSubparts", "1")));
        form.setShowCrossListedClasses("1".equals(user.getProperty("ClassAssignments.showCrossListedClasses", "0")));
    
    }
    
	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */

	public ActionForward searchClasses(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {	
		
		return(performAction(mapping, form, request, response, "search"));
		
	}

	public ActionForward exportPdf(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		return(performAction(mapping, form, request, response, "exportPdf"));
		
	}
	
	public ActionForward exportCsv(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		return(performAction(mapping, form, request, response, "exportCsv"));
		
	}
	
	public ActionForward performAction(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response,
			String action) throws Exception{
		
		sessionContext.checkPermission(Right.ClassAssignments);
        
        if (!action.equals("search") && !action.equals("exportPdf") && !action.equals("exportCsv"))
        	throw new Exception ("Unrecognized Action");
        
        ClassAssignmentsReportForm classListForm = (ClassAssignmentsReportForm) form;
        
        request.setAttribute(Department.EXTERNAL_DEPT_ATTR_NAME, Department.findAllExternal(sessionContext.getUser().getCurrentAcademicSessionId()));
        
	    this.initializeFilters(request, classListForm);
	    
	    classListForm.setSubjectAreas(SubjectArea.getAllSubjectAreas(sessionContext.getUser().getCurrentAcademicSessionId()));
	    classListForm.setClasses(ClassSearchAction.getClasses(classListForm, classAssignmentService.getAssignment()));
	    
		Collection classes = classListForm.getClasses();
		if (classes.isEmpty()) {
		    ActionMessages errors = new ActionMessages();
		    errors.add("searchResult", new ActionMessage("errors.generic", MSG.errorNoRecords()));
		    saveErrors(request, errors);
		    return mapping.findForward("showClassAssignmentsReportSearch");
		} else {
			StringBuffer subjIds = new StringBuffer();
			StringBuffer ids = new StringBuffer();
			StringBuffer names = new StringBuffer();
			for (int i=0;i<classListForm.getSubjectAreaIds().length;i++) {
				if (i>0) {
					names.append(","); 
					subjIds.append(",");
					}
				ids.append("&subjectAreaIds="+classListForm.getSubjectAreaIds()[i]);
				subjIds.append(classListForm.getSubjectAreaIds()[i]);
				names.append(((new SubjectAreaDAO()).get(new Long(classListForm.getSubjectAreaIds()[i]))).getSubjectAreaAbbreviation());
			}
			sessionContext.setAttribute(SessionAttribute.ClassAssignmentsSubjectAreas, subjIds);
			if("search".equals(action)){
				BackTracker.markForBack(
						request, 
						"classAssignmentsReportSearch.do?doit=Search&loadFilter=1"+ids, 
						MSG.backClassAssignments(names.toString()), 
						true, true);
			} else if ("exportPdf".equals(action)) {
				OutputStream out = ExportUtils.getPdfOutputStream(response, "classassign");
				
				new PdfClassAssignmentReportListTableBuilder().pdfTableForClasses(out, classAssignmentService.getAssignment(), examinationSolverService.getSolver(), classListForm, sessionContext);
				
				out.flush(); out.close();
				
				return null;
			} else if ("exportCsv".equals(action)) {

				ExportUtils.exportCSV(
						CsvClassAssignmentExport.exportCsv(sessionContext.getUser(), classListForm.getClasses(), classAssignmentService.getAssignment()),
						response,
						"classassign");
				
	    		return null;
			} 
			return mapping.findForward("showClassAssignmentsReportList");
		}

	}

	@Override
	protected Messages getMessages() {
		return MSG;
	}
}
