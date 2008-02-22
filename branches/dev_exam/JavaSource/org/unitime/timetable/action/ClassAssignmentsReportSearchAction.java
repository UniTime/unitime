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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.cpsolver.ifs.util.CSVFile;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.LookupDispatchAction;
import org.hibernate.HibernateException;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ClassAssignmentsReportForm;
import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.form.ClassListFormInterface;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.CsvClassAssignmentExport;
import org.unitime.timetable.webutil.pdf.PdfClassAssignmentReportListTableBuilder;


/**
 * @author Stephanie Schluttenhofer
 */

public class ClassAssignmentsReportSearchAction extends LookupDispatchAction {

	protected Map getKeyMethodMap() {
	      Map map = new HashMap();
	      map.put("button.searchClasses", "searchClasses");
	      map.put("button.cancel", "searchClasses");
	      map.put("button.exportPDF", "exportPdf");
	      map.put("button.exportCSV", "exportCsv");
	      map.put("button.msfExport", "msfExport");
	      return map;
	}
	
	private void initializeFilters(HttpServletRequest request, ClassAssignmentsReportForm classListForm){
		HttpSession httpSession = request.getSession();
	    if ("1".equals(request.getParameter("loadFilter"))) {
            ClassAssignmentsReportSearchAction.setupGeneralFormFilters(httpSession, classListForm);
	    } else {
	    	UserData.setPropertyBoolean(httpSession,"ClassAssignments.sortByKeepSubparts", classListForm.getSortByKeepSubparts());
	    	UserData.setProperty(httpSession,"ClassAssignments.sortBy", classListForm.getSortBy());
	    	UserData.setProperty(httpSession,"ClassAssignments.filterAssignedRoom", classListForm.getFilterAssignedRoom());		    	
	    	//UserData.setProperty(httpSession,"ClassAssignments.filterInstructor", classListForm.getFilterInstructor());		    	
	    	UserData.setProperty(httpSession,"ClassAssignments.filterManager", classListForm.getFilterManager());		
	    	UserData.setProperty(httpSession,"ClassAssignments.filterIType", classListForm.getFilterIType());
	    	UserData.setPropertyInt(httpSession,"ClassAssignments.filterDayCode", classListForm.getFilterDayCode());
	    	UserData.setPropertyInt(httpSession,"ClassAssignments.filterStartSlot", classListForm.getFilterStartSlot());
	    	UserData.setPropertyInt(httpSession,"ClassAssignments.filterLength", classListForm.getFilterLength());
	    }

	}
	
    
    public static void setupGeneralFormFilters(HttpSession httpSession, ClassListFormInterface form){
        form.setSortBy(UserData.getProperty(httpSession, "ClassAssignments.sortBy", ClassListForm.sSortByName));
        form.setFilterAssignedRoom(UserData.getProperty(httpSession, "ClassAssignments.filterAssignedRoom", ""));
        form.setFilterManager(UserData.getProperty(httpSession, "ClassAssignments.filterManager", ""));
        form.setFilterIType(UserData.getProperty(httpSession, "ClassAssignments.filterIType", ""));
        form.setFilterDayCode(UserData.getPropertyInt(httpSession, "ClassAssignments.filterDayCode", -1));
        form.setFilterStartSlot(UserData.getPropertyInt(httpSession, "ClassAssignments.filterStartSlot", -1));
        form.setFilterLength(UserData.getPropertyInt(httpSession, "ClassAssignments.filterLength", -1));
        form.setSortByKeepSubparts(UserData.getPropertyBoolean(httpSession, "ClassAssignments.sortByKeepSubparts", true));
    
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

	public ActionForward msfExport(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		return(performAction(mapping, form, request, response, "msfExport"));
		
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
        if(!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        if (!action.equals("search") && !action.equals("msfExport") && !action.equals("exportPdf") && !action.equals("exportCsv")){
        	throw new Exception ("Unrecognized Action");
        }
        
        HttpSession httpSession = request.getSession();
        
        ClassAssignmentsReportForm classListForm = (ClassAssignmentsReportForm) form;
	    User user = Web.getUser(request.getSession());
	    LookupTables.setupExternalDepts(request, (Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME));
        classListForm.setIsAdmin(user.isAdmin());
        
	    this.initializeFilters(request, classListForm);
        	        
	    classListForm.setCollections(request, ClassSearchAction.getClasses(classListForm, WebSolver.getClassAssignmentProxy(request.getSession())));
		Collection classes = classListForm.getClasses();
		if (classes.isEmpty()) {
		    ActionMessages errors = new ActionMessages();
		    errors.add("searchResult", new ActionMessage("errors.generic", "No records matching the search criteria were found."));
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
			httpSession.setAttribute(Constants.CRS_ASGN_LST_SUBJ_AREA_IDS_ATTR_NAME, subjIds);
			if("search".equals(action)){
				BackTracker.markForBack(
						request, 
						"classAssignmentsReportSearch.do?doit=Search&loadFilter=1"+ids, 
						"Class Assignments ("+names+")", 
						true, true);
			} else if ("exportPdf".equals(action)) {
				PdfClassAssignmentReportListTableBuilder tb = new PdfClassAssignmentReportListTableBuilder();
				File outFile = tb.pdfTableForClasses(WebSolver.getClassAssignmentProxy(httpSession), WebSolver.getExamSolver(httpSession), classListForm, user);
				//if (outFile!=null) response.sendRedirect("temp/"+outFile.getName());
				if (outFile!=null) request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+outFile.getName());
				BackTracker.markForBack(
						request, 
						"classAssignmentsReportSearch.do?doit=Search&loadFilter=1"+ids, 
						"Class Assignments ("+names+")", 
						true, true);
			} else if ("exportCsv".equals(action)) {
				CSVFile csvFile = CsvClassAssignmentExport.exportCsv(user, classListForm.getClasses(), WebSolver.getClassAssignmentProxy(httpSession));
				File file = ApplicationProperties.getTempFile("classassign", "csv");
	        	csvFile.save(file);
				request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
				BackTracker.markForBack(
						request, 
						"classAssignmentsReportSearch.do?doit=Search&loadFilter=1"+ids, 
						"Class Assignments ("+names+")", 
						true, true);
	        	/*
	        	response.sendRedirect("temp/"+file.getName());
	       		response.setContentType("text/csv");
	       		*/
			} 
			return mapping.findForward("showClassAssignmentsReportList");
		}

	}
}
