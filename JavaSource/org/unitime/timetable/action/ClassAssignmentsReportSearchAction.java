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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ClassAssignmentsReportForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.IdValue;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.CsvClassAssignmentExport;
import org.unitime.timetable.webutil.WebClassAssignmentReportListTableBuilder;
import org.unitime.timetable.webutil.pdf.PdfClassAssignmentReportListTableBuilder;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
@Action(value="classAssignmentsReportSearch", results = {
		@Result(name = "showClassAssignmentsReportSearch", type = "tiles", location = "classAssignmentsReportSearch.tiles")
	})
@TilesDefinition(name = "classAssignmentsReportSearch.tiles", extend =  "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Class Assignments"),
		@TilesPutAttribute(name = "body", value = "/user/classAssignmentsReportSearch.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment"),
		@TilesPutAttribute(name = "checkRole", value = "false")
})
public class ClassAssignmentsReportSearchAction extends UniTimeAction<ClassAssignmentsReportForm> {
	private static final long serialVersionUID = -2339315783902007234L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	private String doit;
	private String[] subjectAreaIds;
	private String loadFilter;
	private boolean showTable = false;
	
	public String getDoit() { return doit; }
	public void setDoit(String doit) { this.doit = doit; }
	public String[] getSubjectAreaIds() { return subjectAreaIds; }
	public void setSubjectAreaIds(String[] subjectAreaIds) { this.subjectAreaIds = subjectAreaIds; }
	public String getLoadFilter() { return loadFilter; }
	public void setLoadFilter(String loadFilter) { this.loadFilter = loadFilter; }
	public boolean isShowTable() { return showTable; }
	public void setShowTable(boolean showTable) { this.showTable = showTable; }	
	
	public String execute() throws Exception {
    	if (ApplicationProperty.LegacyClassAssignments.isFalse()) {
    		String url = "classAssignments";
    		boolean first = true;
    		for (Enumeration<String> e = getRequest().getParameterNames(); e.hasMoreElements(); ) {
    			String param = e.nextElement();
    			url += (first ? "?" : "&") + param + "=" + URLEncoder.encode(getRequest().getParameter(param), "utf-8");
    			first = false;
    		}
    		response.sendRedirect(url);
			return null;
    	}
		if (form == null)
			form = new ClassAssignmentsReportForm();
		
    	if (getSubjectAreaIds() != null)
    		form.setSubjectAreaIds(getSubjectAreaIds());
    	
		LookupTables.setupItypes(request,true);
		
    	if (MSG.actionSearchClassAssignments().equals(doit) || "Search".equals(doit))
    		return searchClasses();
    	if (MSG.actionExportPdf().equals(doit))
    		return exportPdf();
    	if (MSG.actionExportCsv().equals(doit))
    		return exportCsv();

        BackTracker.markForBack(request, null, null, false, true);
        
        sessionContext.checkPermission(Right.ClassAssignments);

	    request.setAttribute(Department.EXTERNAL_DEPT_ATTR_NAME, Department.findAllExternal(sessionContext.getUser().getCurrentAcademicSessionId()));
	    
	    ClassAssignmentsReportSearchAction.setupGeneralFormFilters(sessionContext.getUser(), form);
	    
		if (request.getParameter("sortBy") != null) {
			form.setSortBy((String)request.getParameter("sortBy"));
			form.setFilterAssignedRoom((String)request.getParameter("filterAssignedRoom"));
			form.setFilterManager((String)request.getParameter("filterManager"));
			form.setFilterIType((String)request.getParameter("filterIType"));
			form.setFilterAssignedTimeMon(request.getParameter("filterAssignedTimeMon")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeMon")));
			form.setFilterAssignedTimeTue(request.getParameter("filterAssignedTimeTue")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeTue")));
			form.setFilterAssignedTimeWed(request.getParameter("filterAssignedTimeWed")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeWed")));
			form.setFilterAssignedTimeThu(request.getParameter("filterAssignedTimeThu")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeThu")));
			form.setFilterAssignedTimeFri(request.getParameter("filterAssignedTimeFri")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeFri")));
			form.setFilterAssignedTimeSat(request.getParameter("filterAssignedTimeSat")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeSat")));
			form.setFilterAssignedTimeSun(request.getParameter("filterAssignedTimeSun")==null?false:Boolean.getBoolean((String)request.getParameter("filterAssignedTimeSun")));
			form.setFilterAssignedTimeHour((String)request.getParameter("filterAssignedTimeHour"));
			form.setFilterAssignedTimeMin((String)request.getParameter("filterAssignedTimeMin"));
			form.setFilterAssignedTimeAmPm((String)request.getParameter("filterAssignedTimeAmPm"));
			form.setFilterAssignedTimeLength((String)request.getParameter("filterAssignedTimeLength"));
			form.setSortByKeepSubparts(Boolean.getBoolean((String)request.getParameter("sortByKeepSubparts")));
		}
		
		form.setSubjectAreas(SubjectArea.getAllSubjectAreas(sessionContext.getUser().getCurrentAcademicSessionId()));

		Object sas = sessionContext.getAttribute(SessionAttribute.ClassAssignmentsSubjectAreas);
		if (sas == null)
			sas = sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
		if (Constants.ALL_OPTION_VALUE.equals(sas))
			sas = null;
	    if(sas!=null && sas.toString().trim().length() > 0) {
	    	String subjectAreaIds = sas.toString();
	        try {
	        	
		        Debug.debug("Subject Areas: " + subjectAreaIds);
		        
		        
		        form.setSubjectAreaIds(subjectAreaIds.split(","));
		        
		        Integer maxSubjectsToSearch = ApplicationProperty.MaxSubjectsToSearchAutomatically.intValue();
		        if (maxSubjectsToSearch != null && maxSubjectsToSearch >= 0 && form.getSubjectAreaIds().length > maxSubjectsToSearch) {
		        	setShowTable(false);
		        	return "showClassAssignmentsReportSearch";
		        }
		        
				form.validate(this);
		    	if (hasFieldErrors()) {
		    		setShowTable(false);
					return "showClassAssignmentsReportSearch";
		    	} 
				form.setClasses(ClassSearchAction.getClasses(form, getClassAssignmentService().getAssignment()));
				Collection classes = form.getClasses();
		    	if (classes.isEmpty()) {
		    		addFieldError("searchResult", MSG.errorNoRecords());
		    		setShowTable(false);
				    return "showClassAssignmentsReportSearch";
				} else {
					StringBuffer ids = new StringBuffer();
					StringBuffer names = new StringBuffer();
					for (int i=0;i<form.getSubjectAreaIds().length;i++) {
						if (i>0) names.append(","); 
						ids.append("&subjectAreaIds="+form.getSubjectAreaIds()[i]);
						names.append(((SubjectAreaDAO.getInstance()).get(Long.valueOf(form.getSubjectAreaIds()[i]))).getSubjectAreaAbbreviation());
					}
					BackTracker.markForBack(
							request, 
							"classAssignmentsReportSearch.action?doit=Search&loadFilter=1"+ids, 
							"Class Assignments ("+names+")", 
							true, true);
					setShowTable(true);
					return "showClassAssignmentsReportSearch";
				}
	        } catch (NumberFormatException nfe) {
	        	Debug.error("Subject Area Ids session attribute is corrupted. Resetting ... ");
		        sessionContext.removeAttribute(SessionAttribute.ClassAssignmentsSubjectAreas);
		    }
	    }

	    setShowTable(false);
	    return "showClassAssignmentsReportSearch";
	}

	private void initializeFilters() {
	    if ("1".equals(getLoadFilter())) {
            ClassAssignmentsReportSearchAction.setupGeneralFormFilters(sessionContext.getUser(), form);
	    } else {
	    	sessionContext.getUser().setProperty("ClassAssignments.sortByKeepSubparts", form.getSortByKeepSubparts() ? "1" : "0");
	    	sessionContext.getUser().setProperty("ClassAssignments.sortBy", form.getSortBy());
	    	sessionContext.getUser().setProperty("ClassAssignments.filterAssignedRoom", form.getFilterAssignedRoom());		    	
	    	sessionContext.getUser().setProperty("ClassAssignments.filterManager", form.getFilterManager());		
	    	sessionContext.getUser().setProperty("ClassAssignments.filterIType", form.getFilterIType());
	    	sessionContext.getUser().setProperty("ClassAssignments.filterDayCode", String.valueOf(form.getFilterDayCode()));
	    	sessionContext.getUser().setProperty("ClassAssignments.filterStartSlot", String.valueOf(form.getFilterStartSlot()));
	    	sessionContext.getUser().setProperty("ClassAssignments.filterLength", String.valueOf(form.getFilterLength()));
	    	sessionContext.getUser().setProperty("ClassAssignments.showCrossListedClasses", form.getShowCrossListedClasses() ? "1" : "0");
	    }

	}
	
    public static void setupGeneralFormFilters(UserContext user, ClassAssignmentsReportForm form){
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

	public String searchClasses() throws Exception {
		return performAction("search");
	}

	public String exportPdf() throws Exception {
		return performAction("exportPdf");
	}

	public String exportCsv() throws Exception {
		return performAction("exportCsv");
	}
	
	public String performAction(String action) throws Exception{
		
		sessionContext.checkPermission(Right.ClassAssignments);
        
        if (!action.equals("search") && !action.equals("exportPdf") && !action.equals("exportCsv"))
        	throw new Exception ("Unrecognized Action");
        
        request.setAttribute(Department.EXTERNAL_DEPT_ATTR_NAME, Department.findAllExternal(sessionContext.getUser().getCurrentAcademicSessionId()));
        
	    initializeFilters();
	    
	    form.setSubjectAreas(SubjectArea.getAllSubjectAreas(sessionContext.getUser().getCurrentAcademicSessionId()));
	    
		form.validate(this);
    	if (hasFieldErrors()) {
    		setShowTable(false);
			return "showClassAssignmentsReportSearch";
    	} 

    	form.setClasses(ClassSearchAction.getClasses(form, getClassAssignmentService().getAssignment()));
	    Collection classes = form.getClasses();
	    if (classes.isEmpty()) {
    		addFieldError("searchResult", MSG.errorNoRecords());
    		setShowTable(false);
		    return "showClassAssignmentsReportSearch";
		} else {
			StringBuffer subjIds = new StringBuffer();
			StringBuffer ids = new StringBuffer();
			StringBuffer names = new StringBuffer();
			for (int i=0;i<form.getSubjectAreaIds().length;i++) {
				if (i>0) {
					names.append(","); 
					subjIds.append(",");
					}
				ids.append("&subjectAreaIds="+form.getSubjectAreaIds()[i]);
				subjIds.append(form.getSubjectAreaIds()[i]);
				names.append(((SubjectAreaDAO.getInstance()).get(Long.valueOf(form.getSubjectAreaIds()[i]))).getSubjectAreaAbbreviation());
			}
			sessionContext.setAttribute(SessionAttribute.ClassAssignmentsSubjectAreas, subjIds);
			if("search".equals(action)){
				BackTracker.markForBack(
						request, 
						"classAssignmentsReportSearch.action?doit=Search&loadFilter=1"+ids, 
						MSG.backClassAssignments(names.toString()), 
						true, true);
				setShowTable(true);
			} else if ("exportPdf".equals(action)) {
				OutputStream out = ExportUtils.getPdfOutputStream(response, "classassign");
				
				new PdfClassAssignmentReportListTableBuilder().pdfTableForClasses(out, getClassAssignmentService().getAssignment(), getExaminationSolverService().getSolver(), form, sessionContext);
				
				out.flush(); out.close();
				
				return null;
			} else if ("exportCsv".equals(action)) {

				ExportUtils.exportCSV(
						CsvClassAssignmentExport.exportCsv(sessionContext.getUser(), form.getClasses(), getClassAssignmentService().getAssignment()),
						response,
						"classassign");
				
	    		return null;
			} 
			return "showClassAssignmentsReportSearch";
		}

	}
	
	public List<IdValue> getManagers() {
		List<IdValue> ret = new ArrayList<IdValue>();
		// ret.add(new IdValue(null, MSG.dropManagerAll()));
		ret.add(new IdValue(-2l, MSG.dropDeptDepartment()));
		for (Department d: (TreeSet<Department>)request.getAttribute(Department.EXTERNAL_DEPT_ATTR_NAME))
			ret.add(new IdValue(d.getUniqueId(), d.getManagingDeptLabel()));
		return ret;
	}
	
	public int getFilterManagerSize() {
		int size = 1 + ((TreeSet<Department>)request.getAttribute(Department.EXTERNAL_DEPT_ATTR_NAME)).size();
		return Math.min(size, 3);
	}
	
	public String printTable() throws Exception {
		new WebClassAssignmentReportListTableBuilder().htmlTableForClasses(
				sessionContext,
				getClassAssignmentService().getAssignment(),
				getExaminationSolverService().getSolver(),
				form, 
				getPageContext().getOut(),
				request.getParameter("backType"),
				request.getParameter("backId")
			);
		return "";
	}
}
