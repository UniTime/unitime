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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.PdfWorksheet;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.WebInstructionalOfferingTableBuilder;
import org.unitime.timetable.webutil.csv.CsvInstructionalOfferingTableBuilder;
import org.unitime.timetable.webutil.pdf.PdfInstructionalOfferingTableBuilder;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
@Action(value="instructionalOfferingSearch", results = {
		@Result(name = "showInstructionalOfferingSearch", type = "tiles", location = "instructionalOfferingList.tiles"),
		@Result(name = "showInstructionalOfferingDetail", type = "redirect", location = "/instructionalOfferingDetail.action",
			params = {"op" , "${op}", "io", "${io}"}
		),
		@Result(name = "showCourseOfferingEdit", type = "redirect", location = "/courseOfferingEdit.action",
				params = {"op" , "${op}", "subjAreaId", "${form.subjectAreaIds[0]}", "courseNbr", "${form.courseNbr}"}
		)
	})
@TilesDefinition(name = "instructionalOfferingList.tiles", extend =  "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Instructional Offerings"),
		@TilesPutAttribute(name = "body", value = "/user/instructionalOfferingSearch.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
})
public class InstructionalOfferingSearchAction extends UniTimeAction<InstructionalOfferingListForm> {
	private static final long serialVersionUID = 121359338070354596L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	private Long io;
	private String doit;
	private String[] subjectAreaIds;
	private String courseNbr;
	private String loadInstrFilter;
	private boolean showTable = false;
	
	public Long getIo() { return io; }
	public void setIo(Long io) { this.io = io; }
	public String getDoit() { return doit; }
	public void setDoit(String doit) { this.doit = doit; }
	public String[] getSubjectAreaIds() { return subjectAreaIds; }
	public void setSubjectAreaIds(String[] subjectAreaIds) { this.subjectAreaIds = subjectAreaIds; }
	public String getCourseNbr() { return courseNbr; }
	public void setCourseNbr(String courseNbr) { this.courseNbr = courseNbr; }
	public String getLoadInstrFilter() { return loadInstrFilter; }
	public void setLoadInstrFilter(String loadInstrFilter) { this.loadInstrFilter = loadInstrFilter; }
	public boolean isShowTable() { return showTable; }
	public void setShowTable(boolean showTable) { this.showTable = showTable; }
	
	public String execute() throws Exception {
		if (form == null)
			form = new InstructionalOfferingListForm();
    	sessionContext.checkPermission(Right.InstructionalOfferings);
    	
    	if (getSubjectAreaIds() != null)
    		form.setSubjectAreaIds(getSubjectAreaIds());
    	if (getCourseNbr() != null)
    		form.setCourseNbr(getCourseNbr());
    	if ("1".equals(getLoadInstrFilter()))
    		setupInstrOffrListSpecificFormFilters(sessionContext, form);
    	
    	if (MSG.actionSearchInstructionalOfferings().equals(doit) || "Search".equals(doit))
    		return searchInstructionalOfferings();
    	if (MSG.actionExportPdf().equals(doit))
    		return exportPdf();
    	if (MSG.actionExportCsv().equals(doit))
    		return exportCsv();
    	if (MSG.actionWorksheetPdf().equals(doit))
    		return worksheetPdf();
    	if (MSG.actionAddNewInstructionalOffering().equals(doit))
    		return addInstructionalOfferings();
        
        BackTracker.markForBack(request, null, null, false, true); //clear back list
        
        // Check if subject area / course number saved to session
	    Object sa = sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
	    Object cn = sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
	    
	    if (Constants.ALL_OPTION_VALUE.equals(sa))
	    	sa = null;
	    
	    if ((sa == null || sa.toString().trim().isEmpty()) && (cn == null || cn.toString().trim().isEmpty())) {
		    // use session variables from class search  
		    sa = sessionContext.getAttribute(SessionAttribute.ClassesSubjectAreas);
		    cn = sessionContext.getAttribute(SessionAttribute.ClassesCourseNumber);
		    
		    // Use first subject area
		    if (sa != null) {
		       String saStr = sa.toString();
		       if (saStr.indexOf(",") > 0)
		           sa = saStr.substring(0, saStr.indexOf(","));
		    }
	    }
	    
	    InstructionalOfferingSearchAction.setupInstrOffrListSpecificFormFilters(sessionContext, form);
	    
	    if (!sessionContext.hasPermission(Right.Examinations))
	    	form.setExams(null);

	    Integer maxSubjectsToSearch = ApplicationProperty.MaxSubjectsToSearchAutomatically.intValue();
	    // Subject Area is saved to the session - Perform automatic search
	    if (sa != null) {
	        try {
	            
			    StringBuffer ids = new StringBuffer();
				StringBuffer names = new StringBuffer();
				StringBuffer subjIds = new StringBuffer();
				for (String id: sa.toString().split(",")) {
					if ("-1".equals(id)) continue;
					if (names.length() > 0) {
						names.append(","); 
						subjIds.append(",");
					}
					ids.append("&subjectAreaIds="+id);
					subjIds.append(id);
					names.append(((new SubjectAreaDAO()).get(Long.valueOf(id))).getSubjectAreaAbbreviation());
				}
				

				String courseNbr = "";
				if (cn != null && !cn.toString().isEmpty())
		            courseNbr = cn.toString();
		        
		        Debug.debug("Subject Areas: " + subjIds);
		        Debug.debug("Course Number: " + courseNbr);
		        
		        form.setSubjectAreaIds(sa.toString().split(","));
		        form.setCourseNbr(courseNbr);
		        
		        if (maxSubjectsToSearch != null && maxSubjectsToSearch >= 0 && form.getSubjectAreaIds().length > maxSubjectsToSearch) {
		        	form.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
			    	form.setInstructionalOfferings(null);
			    	setShowTable(false);
			    	return "showInstructionalOfferingSearch";
		        }
		        
		        if (doSearch()) {
					BackTracker.markForBack(
							request, 
							"instructionalOfferingSearch.action?doit=Search&loadInstrFilter=1" + ids + "&courseNbr="+form.getCourseNbr(), 
							"Instructional Offerings (" + names + (form.getCourseNbr()==null || form.getCourseNbr().length()==0?"":" "+form.getCourseNbr()) + ")", 
							true, true);
					setShowTable(true);
		            return "showInstructionalOfferingSearch";
		        }
	        }
	        catch (NumberFormatException nfe) {
	            Debug.error("Subject Area Id session attribute is corrupted. Resetting ... ");
	            sessionContext.removeAttribute(SessionAttribute.OfferingsSubjectArea);
	            sessionContext.removeAttribute(SessionAttribute.OfferingsCourseNumber);
	        }
	    }
	    
	    // No session attribute found - Load subject areas
	    else {
	    	form.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
	    	form.setInstructionalOfferings(null);
	        
	        // Check if only 1 subject area exists
	        Set s = (Set) form.getSubjectAreas();
	        if (s.size() == 1 && (maxSubjectsToSearch == null || maxSubjectsToSearch != 0)) {
	            Debug.debug("Exactly 1 subject area found ... ");
	            form.setSubjectAreaIds(new String[] {((SubjectArea) s.iterator().next()).getUniqueId().toString()});
		        if (doSearch()) {
					BackTracker.markForBack(
							request, 
							"instructionalOfferingSearch.action?doit=Search&loadInstrFilter=1&subjectAreaIds="+form.getSubjectAreaIds()[0]+"&courseNbr="+form.getCourseNbr(), 
							"Instructional Offerings ("+
								(form.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(Long.valueOf(form.getSubjectAreaIds()[0]))).getSubjectAreaAbbreviation():form.getSubjectAreaAbbv())+
								(form.getCourseNbr()==null || form.getCourseNbr().length()==0?"":" "+form.getCourseNbr())+
								")", 
							true, true);
					setShowTable(true);
		            return "showInstructionalOfferingSearch";
		        }
	        }
	    }
	    
	    setShowTable(false);
        return "showInstructionalOfferingSearch";
	}

	/**
	 * Perform search based on form values of subject area and course number
	 * If search produces results - generate html and store the html as a request attribute
	 */
	private boolean doSearch() throws Exception {
	    form.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
	    form.setInstructionalOfferings(InstructionalOfferingSearchAction.getInstructionalOfferings(sessionContext.getUser().getCurrentAcademicSessionId(), getClassAssignmentService().getAssignment(), form));
        
		// Search return results - Generate html
		return !form.getInstructionalOfferings().isEmpty();
	}
	
	public String searchInstructionalOfferings() throws Exception {
	    	sessionContext.checkPermission(Right.InstructionalOfferings);
	        
	        // Check that a valid subject area is selected
		    form.validate(this);
		    
		    // Validation fails
		    if (hasFieldErrors()) {
			    form.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
			    form.setInstructionalOfferings(null);
			    setShowTable(false);
			    return "showInstructionalOfferingSearch";
		    }

		    StringBuffer ids = new StringBuffer();
			StringBuffer names = new StringBuffer();
			StringBuffer subjIds = new StringBuffer();
			for (int i=0;i<form.getSubjectAreaIds().length;i++) {
				if (i>0) {
					names.append(","); 
					subjIds.append(",");
				}
				ids.append("&subjectAreaIds="+form.getSubjectAreaIds()[i]);
				subjIds.append(form.getSubjectAreaIds()[i]);
				names.append(((new SubjectAreaDAO()).get(Long.valueOf(form.getSubjectAreaIds()[i]))).getSubjectAreaAbbreviation());
			}
			
		    // Set Session Variables
		    sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjIds.toString());
		    sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, form.getCourseNbr());
	        

		    if (!"1".equals(getLoadInstrFilter())) {
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.divSec", form.getDivSec() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.demand", form.getDemand() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.projectedDemand", form.getProjectedDemand() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.minPerWk", form.getMinPerWk() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.limit", form.getLimit() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.snapshotLimit", form.getSnapshotLimit() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.roomLimit", form.getRoomLimit() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.manager", form.getManager() ? "1" : "0");
			   	sessionContext.getUser().setProperty("InstructionalOfferingList.datePattern", form.getDatePattern() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.timePattern", form.getTimePattern() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.instructor", form.getInstructor() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.preferences", form.getPreferences() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.timetable", (form.getTimetable() == null ? "0" : form.getTimetable() ? "1" : "0"));
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.credit", form.getCredit() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.subpartCredit", form.getSubpartCredit() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.schedulePrintNote", form.getSchedulePrintNote() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.note", form.getNote() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.title", form.getTitle() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.exams", (form.getExams() == null ? "0" : form.getExams() ? "1" : "0"));
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.instructorAssignment", (form.getInstructorAssignment() == null ? "0" : form.getInstructorAssignment() ? "1" : "0"));
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.lms", (form.getLms() == null ? null : form.getLms() ? "1" : "0"));
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.waitlist", (form.getWaitlist() == null ? null : form.getWaitlist()));
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.consent", form.getConsent() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.sortBy", form.getSortBy());
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.fundingDepartment", (form.getFundingDepartment() == null ? "0" : form.getFundingDepartment() ? "1" : "0"));
		    }
		    
		    if (!sessionContext.hasPermission(Right.Examinations))
		    	form.setExams(null);
	        
	        // Perform Search
		    Map<Long, TreeSet<InstructionalOffering>> instrOfferings = getInstructionalOfferings(sessionContext.getUser().getCurrentAcademicSessionId(), getClassAssignmentService().getAssignment(), form);
		    form.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
		    form.setInstructionalOfferings(instrOfferings);
			
			// No results returned
			if (instrOfferings.isEmpty()) {
				addFieldError("searchResult", MSG.errorNoRecords());
			    return"showInstructionalOfferingSearch";
			} else {
				BackTracker.markForBack(
						request, 
						"instructionalOfferingSearch.action?op=Back&doit=Search&loadInstrFilter=1"+ids+"&courseNbr="+URLEncoder.encode(form.getCourseNbr(), "utf-8"), 
						MSG.labelInstructionalOfferings() + " ("+names + (form.getCourseNbr()==null || form.getCourseNbr().length()==0?"":" "+form.getCourseNbr()) + ")", 
						true, true);

				if (request.getParameter("op")==null || 
			            (request.getParameter("op")!=null && !request.getParameter("op").equalsIgnoreCase("Back")) )  {
			        
				    // Search produces 1 result - redirect to offering detail
					if (form.getSubjectAreaIds().length == 1) {
						TreeSet<InstructionalOffering> offerings = form.getInstructionalOfferings(Long.valueOf(form.getSubjectAreaIds()[0]));
						if (offerings != null && offerings.size() == 1) {
					    	InstructionalOffering io = offerings.first();
					    	if (sessionContext.hasPermission(io, Right.InstructionalOfferingDetail)) {
					    		setOp("view");
					    		setIo(io.getUniqueId());
					    		return "showInstructionalOfferingDetail";
					    	}
						}
				    }
			    }
			    
				setShowTable(true);
			    return "showInstructionalOfferingSearch";
			}
		}
	
	
	public String exportPdf() throws Exception {
        String fwd = searchInstructionalOfferings();
        
        if (!hasFieldErrors()) {
        	OutputStream out = ExportUtils.getPdfOutputStream(response, "offerings");
        	
            new PdfInstructionalOfferingTableBuilder().pdfTableForInstructionalOfferings(out,
                        WebSolver.getClassAssignmentProxy(request.getSession()),
                        WebSolver.getExamSolver(request.getSession()),
                        form, 
                        form.getSubjectAreaIds(), 
                        sessionContext,  
                        true, 
                        form.areAllCoursesGiven());
            
            out.flush(); out.close();
            
            return null;
        }
        
        return fwd;
	}
	
	public String exportCsv() throws Exception {
	    
        String fwd = searchInstructionalOfferings();
        
        if (!hasFieldErrors()) {
        	PrintWriter out = ExportUtils.getCsvWriter(response, "offerings");
        	
            new CsvInstructionalOfferingTableBuilder().csvTableForInstructionalOfferings(out,
                        WebSolver.getClassAssignmentProxy(request.getSession()),
                        WebSolver.getExamSolver(request.getSession()),
                        form, 
                        form.getSubjectAreaIds(), 
                        sessionContext,  
                        true, 
                        form.areAllCoursesGiven());
            
            out.flush(); out.close();
            
            return null;
        }
        
        return fwd;
	}
	
	public static void setupInstrOffrListSpecificFormFilters(SessionContext sessionContext, InstructionalOfferingListForm form) {
		form.setDivSec("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.divSec", "0")));	
		form.setDemand("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.demand", "1")));	
		form.setProjectedDemand("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.projectedDemand", "1")));	
		form.setMinPerWk("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.minPerWk", "1")));	
		form.setLimit("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.limit", "1")));
		form.setSnapshotLimit("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.snapshotLimit", "1")));
		form.setRoomLimit("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.roomLimit", "1")));
		form.setManager("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.manager", "1")));	
		form.setDatePattern("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.datePattern", "1")));
		form.setTimePattern("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.timePattern", "1")));
		form.setInstructor("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.instructor", "1")));
		form.setPreferences("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.preferences", "1")));
		form.setTimetable("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.timetable", "1")));	
		form.setCredit("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.credit", "0")));
		form.setSubpartCredit("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.subpartCredit", "0")));
		form.setSchedulePrintNote("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.schedulePrintNote", "1")));
		form.setNote("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.note", "0")));
		form.setTitle("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.title", "0")));
		form.setConsent("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.consent", "0")));
		form.setSortBy(sessionContext.getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)));
		form.setExams("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.exams", "0")));
		form.setInstructorAssignment("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.instructorAssignment", "0")));
		if (LearningManagementSystemInfo.isLmsInfoDefinedForSession(sessionContext.getUser().getCurrentAcademicSessionId()))
			form.setLms("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.lms", "0")));
		else
			form.setLms(null);
		if (ApplicationProperty.OfferingWaitListShowFilter.isTrue())
			form.setWaitlist(sessionContext.getUser().getProperty("InstructionalOfferingList.waitlist", "A"));
		else
			form.setWaitlist(null);
		
		form.setFundingDepartment("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.fundingDepartment", "0")));
	}
	
	
	public String worksheetPdf() throws Exception {

        String fwd = searchInstructionalOfferings();
        
        if (!hasFieldErrors()) {
        	List<SubjectArea> subjectAreas = new ArrayList<SubjectArea>();
        	for (String subjectAreaId: form.getSubjectAreaIds()) {
        		SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(Long.valueOf(subjectAreaId));
        		if (subjectArea != null)
        			subjectAreas.add(subjectArea);
        	}
        	if (subjectAreas.isEmpty())
        		return fwd;
        	
        	OutputStream out = ExportUtils.getPdfOutputStream(response, "worksheet");
        	
            PdfWorksheet.print(out, subjectAreas, form.getCourseNbr(), form.getWaitlist());
            
            out.flush(); out.close();

            return null;
        }
        
        return fwd;
	}
	
    public static Map<Long, TreeSet<InstructionalOffering>> getInstructionalOfferings(Long sessionId, ClassAssignmentProxy classAssignmentProxy, InstructionalOfferingListForm form) {
        
        boolean fetchStructure = true;
        boolean fetchCredits = false;//singleCourseSelection || form.getCredit().booleanValue();
        boolean fetchInstructors = false;//singleCourseSelection || form.getInstructor().booleanValue();
        boolean fetchPreferences = false;//singleCourseSelection || form.getPreferences().booleanValue() || form.getTimePattern().booleanValue();
        boolean fetchAssignments = false;//singleCourseSelection || (form.getTimetable()!=null && form.getTimetable().booleanValue());
        boolean fetchReservations = false;//singleCourseSelection;
        
        Map<Long, TreeSet<InstructionalOffering>> map = new Hashtable<Long, TreeSet<InstructionalOffering>>();
        for (String subjectAreaId: form.getSubjectAreaIds()) {
        	TreeSet<InstructionalOffering> ts = InstructionalOffering.search(sessionId, Long.valueOf(subjectAreaId), form.getCourseNbr(),
        			fetchStructure, fetchCredits, fetchInstructors, fetchPreferences, fetchAssignments, fetchReservations, form.getWaitlist());
        	if (ts.isEmpty()) continue;
        	map.put(Long.valueOf(subjectAreaId), ts);
        	
        }
        

        return map;
    }
    
    /**
     * Creates an instructional offering
     */
	public String addInstructionalOfferings() throws Exception {

	    Long subjAreaId = (form.getSubjectAreaIds() == null || form.getSubjectAreaIds().length < 1 ? null : Long.valueOf(form.getSubjectAreaIds()[0]));
		String courseNbr = form.getCourseNbr().trim();
	    
	    // Check if errors were found
	    if (hasFieldErrors()) {
		    form.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
	        if (form.getInstructionalOfferings() == null || form.getInstructionalOfferings().isEmpty()) {
	        	setShowTable(false);
	        	return "showInstructionalOfferingSearch";
			} else {
				setShowTable(true);
			    return "showInstructionalOfferingList";
			}
	    }
	    
	    // Set Session Variables
	    if (subjAreaId != null) {
	    	sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjAreaId.toString());
	    	sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, courseNbr);
	    }
	    
	    if (subjAreaId != null && !courseNbr.isEmpty()) {
		    // Offering exists - redirect to offering detail
	    	if (ApplicationProperty.CourseOfferingNumberMustBeUnique.isTrue()) {
	    		CourseOffering course = CourseOffering.findBySessionSubjAreaIdCourseNbr(sessionContext.getUser().getCurrentAcademicSessionId(), subjAreaId, courseNbr);
	    		if (course != null) {
	    			setOp("view");
		    		setIo(course.getInstructionalOffering().getUniqueId());
		            return "showInstructionalOfferingDetail";
			    }
	    	}
	    }
	    
	    if (ApplicationProperty.LegacyCourseEdit.isTrue()) {
	    	setOp(MSG.actionAddCourseOffering());
	        return "showCourseOfferingEdit";
	    } else {
	    	response.sendRedirect("gwt.jsp?page=courseOffering&subjArea=" + (subjAreaId == null ? "" : subjAreaId) + "&courseNbr=" + courseNbr + "&op=addCourseOffering");
	    	return null;
	    }
	}
	
	public static void setLastInstructionalOffering(SessionContext sessionContext, InstructionalOffering offering) {
		if (offering == null) return;
		String subjectAreaIds = (String)sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
		String subjectAreaId = offering.getControllingCourseOffering().getSubjectArea().getUniqueId().toString();
		if (subjectAreaIds == null) {
			sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjectAreaId);
		} else {
			boolean contain = false;
			for (String s: subjectAreaIds.split(","))
				if (s.equals(subjectAreaId)) { contain = true; break; }
			if (!contain && sessionContext.hasPermission(offering.getControllingCourseOffering().getDepartment(), Right.InstructionalOfferings)) {
				sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjectAreaId);
			}
		}
		
		if (sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber) != null && !sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber).toString().isEmpty())
            sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, offering.getControllingCourseOffering().getCourseNbr());

	}
	
	protected void createTable() throws Exception {
		StringWriter out = new StringWriter();
		new WebInstructionalOfferingTableBuilder().htmlTableForInstructionalOfferings(
				sessionContext,
				getClassAssignmentService().getAssignment(),
				getExaminationSolverService().getSolver(),
		        form, 
		        form.getSubjectAreaIds(), 
		        true, 
		        form.areAllCoursesGiven(),
		        out,
		        request.getParameter("backType"),
		        request.getParameter("backId"));
		out.flush(); out.close();
		request.setAttribute("table", out.toString());
	}
	
	public String printTable() throws Exception {
		new WebInstructionalOfferingTableBuilder().htmlTableForInstructionalOfferings(
				sessionContext,
				getClassAssignmentService().getAssignment(),
				getExaminationSolverService().getSolver(),
		        form, 
		        form.getSubjectAreaIds(), 
		        true, 
		        form.areAllCoursesGiven(),
		        getPageContext().getOut(),
		        request.getParameter("backType"),
		        request.getParameter("backId"));
		return "";
	}
}
