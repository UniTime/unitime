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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.impl.LocalizedLookupDispatchAction;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.Messages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.PdfWorksheet;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.csv.CsvInstructionalOfferingTableBuilder;
import org.unitime.timetable.webutil.pdf.PdfInstructionalOfferingTableBuilder;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
@Service("/instructionalOfferingSearch")
public class InstructionalOfferingSearchAction extends LocalizedLookupDispatchAction {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	
	@Override
	protected Messages getMessages() {
		return MSG;
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
	public ActionForward searchInstructionalOfferings(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

	    	sessionContext.checkPermission(Right.InstructionalOfferings);
	        
	        // Check that a valid subject area is selected
		    InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
		    ActionMessages errors = null;
		    errors = frm.validate(mapping, request);
		    
		    // Validation fails
		    if(errors.size()>0) {
			    saveErrors(request, errors);
			    frm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
			    frm.setInstructionalOfferings(null);
			    return mapping.findForward("showInstructionalOfferingSearch");
		    }

		    StringBuffer ids = new StringBuffer();
			StringBuffer names = new StringBuffer();
			StringBuffer subjIds = new StringBuffer();
			for (int i=0;i<frm.getSubjectAreaIds().length;i++) {
				if (i>0) {
					names.append(","); 
					subjIds.append(",");
				}
				ids.append("&subjectAreaIds="+frm.getSubjectAreaIds()[i]);
				subjIds.append(frm.getSubjectAreaIds()[i]);
				names.append(((new SubjectAreaDAO()).get(new Long(frm.getSubjectAreaIds()[i]))).getSubjectAreaAbbreviation());
			}
			
		    // Set Session Variables
		    sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjIds.toString());
		    sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, frm.getCourseNbr());
	        

		    if ("1".equals(request.getParameter("loadInstrFilter"))) {
		    	setupInstrOffrListSpecificFormFilters(sessionContext, frm);
		    } else {
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.divSec", frm.getDivSec() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.demand", frm.getDemand() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.projectedDemand", frm.getProjectedDemand() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.minPerWk", frm.getMinPerWk() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.limit", frm.getLimit() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.snapshotLimit", frm.getSnapshotLimit() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.roomLimit", frm.getRoomLimit() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.manager", frm.getManager() ? "1" : "0");
			   	sessionContext.getUser().setProperty("InstructionalOfferingList.datePattern", frm.getDatePattern() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.timePattern", frm.getTimePattern() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.instructor", frm.getInstructor() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.preferences", frm.getPreferences() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.timetable", (frm.getTimetable() == null ? "0" : frm.getTimetable() ? "1" : "0"));
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.credit", frm.getCredit() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.subpartCredit", frm.getSubpartCredit() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.schedulePrintNote", frm.getSchedulePrintNote() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.note", frm.getNote() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.title", frm.getTitle() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.exams", (frm.getExams() == null ? "0" : frm.getExams() ? "1" : "0"));
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.instructorAssignment", (frm.getInstructorAssignment() == null ? "0" : frm.getInstructorAssignment() ? "1" : "0"));
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.consent", frm.getConsent() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.sortBy", frm.getSortBy());
		    }
		    
		    if (!sessionContext.hasPermission(Right.Examinations))
		    	frm.setExams(null);
	        
	        // Perform Search
		    Map<Long, TreeSet<InstructionalOffering>> instrOfferings = getInstructionalOfferings(sessionContext.getUser().getCurrentAcademicSessionId(), classAssignmentService.getAssignment(), frm);
		    frm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
		    frm.setInstructionalOfferings(instrOfferings);
			
			// No results returned
			if (instrOfferings.isEmpty()) {
			    errors.add("searchResult", new ActionMessage("errors.generic", MSG.errorNoRecords()));
			    saveErrors(request, errors);
			    return mapping.findForward("showInstructionalOfferingSearch");
			} 
			else {

				BackTracker.markForBack(
						request, 
						"instructionalOfferingSearch.do?op=Back&doit=Search&loadInstrFilter=1"+ids+"&courseNbr="+URLEncoder.encode(frm.getCourseNbr(), "utf-8"), 
						MSG.labelInstructionalOfferings() + " ("+names + (frm.getCourseNbr()==null || frm.getCourseNbr().length()==0?"":" "+frm.getCourseNbr()) + ")", 
						true, true);

				if (request.getParameter("op")==null || 
			            (request.getParameter("op")!=null && !request.getParameter("op").equalsIgnoreCase("Back")) )  {
			        
				    // Search produces 1 result - redirect to offering detail
					if (frm.getSubjectAreaIds().length == 1) {
						TreeSet<InstructionalOffering> offerings = frm.getInstructionalOfferings(Long.valueOf(frm.getSubjectAreaIds()[0]));
						if (offerings != null && offerings.size() == 1) {
					    	InstructionalOffering io = offerings.first();
					    	if (sessionContext.hasPermission(io, Right.InstructionalOfferingDetail)) {
					            ActionRedirect redirect = new ActionRedirect(mapping.findForward("showInstructionalOfferingDetail"));
					            redirect.addParameter("op", "view");
					            redirect.addParameter("io", io.getUniqueId().toString());
					            return redirect;
					    	}
						}
				    }
			    }
			    
			    return mapping.findForward("showInstructionalOfferingList");
			}
		}
	
	
	public ActionForward exportPdf(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	    
        ActionForward fwd = searchInstructionalOfferings(mapping, form, request, response);
        
        InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
        
        if (getErrors(request).isEmpty()) {
        	OutputStream out = ExportUtils.getPdfOutputStream(response, "offerings");
        	
            new PdfInstructionalOfferingTableBuilder().pdfTableForInstructionalOfferings(out,
                        WebSolver.getClassAssignmentProxy(request.getSession()),
                        WebSolver.getExamSolver(request.getSession()),
                        frm, 
                        frm.getSubjectAreaIds(), 
                        sessionContext,  
                        true, 
                        frm.getCourseNbr()==null || frm.getCourseNbr().length()==0);
            
            out.flush(); out.close();
            
            return null;
        }
        
        return fwd;
	}
	
	public ActionForward exportCsv(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	    
        ActionForward fwd = searchInstructionalOfferings(mapping, form, request, response);
        
        InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
        
        if (getErrors(request).isEmpty()) {
        	PrintWriter out = ExportUtils.getCsvWriter(response, "offerings");
        	
            new CsvInstructionalOfferingTableBuilder().csvTableForInstructionalOfferings(out,
                        WebSolver.getClassAssignmentProxy(request.getSession()),
                        WebSolver.getExamSolver(request.getSession()),
                        frm, 
                        frm.getSubjectAreaIds(), 
                        sessionContext,  
                        true, 
                        frm.getCourseNbr()==null || frm.getCourseNbr().length()==0);
            
            out.flush(); out.close();
            
            return null;
        }
        
        return fwd;
	}
	
	public static void setupInstrOffrListSpecificFormFilters(SessionContext sessionContext, InstructionalOfferingListForm frm) {
		frm.setDivSec("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.divSec", "0")));	
		frm.setDemand("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.demand", "1")));	
		frm.setProjectedDemand("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.projectedDemand", "1")));	
		frm.setMinPerWk("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.minPerWk", "1")));	
		frm.setLimit("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.limit", "1")));
		frm.setSnapshotLimit("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.snapshotLimit", "1")));
		frm.setRoomLimit("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.roomLimit", "1")));
		frm.setManager("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.manager", "1")));	
		frm.setDatePattern("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.datePattern", "1")));
		frm.setTimePattern("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.timePattern", "1")));
		frm.setInstructor("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.instructor", "1")));
		frm.setPreferences("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.preferences", "1")));
		frm.setTimetable("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.timetable", "1")));	
		frm.setCredit("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.credit", "0")));
		frm.setSubpartCredit("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.subpartCredit", "0")));
		frm.setSchedulePrintNote("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.schedulePrintNote", "1")));
		frm.setNote("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.note", "0")));
		frm.setTitle("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.title", "0")));
		frm.setConsent("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.consent", "0")));
		frm.setSortBy(sessionContext.getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)));
		frm.setExams("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.exams", "0")));
		frm.setInstructorAssignment("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.instructorAssignment", "0")));
	}
	
	
	public ActionForward worksheetPdf(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        ActionForward fwd = searchInstructionalOfferings(mapping, form, request, response);
        
        InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
        
        if (getErrors(request).isEmpty()) {
        	List<SubjectArea> subjectAreas = new ArrayList<SubjectArea>();
        	for (String subjectAreaId: frm.getSubjectAreaIds()) {
        		SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(Long.valueOf(subjectAreaId));
        		if (subjectArea != null)
        			subjectAreas.add(subjectArea);
        	}
        	if (subjectAreas.isEmpty())
        		return fwd;
        	
        	OutputStream out = ExportUtils.getPdfOutputStream(response, "worksheet");
        	
            PdfWorksheet.print(out, subjectAreas, frm.getCourseNbr());
            
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
        			fetchStructure, fetchCredits, fetchInstructors, fetchPreferences, fetchAssignments, fetchReservations);
        	if (ts.isEmpty()) continue;
        	map.put(Long.valueOf(subjectAreaId), ts);
        	
        }
        

        return map;
    }
    
    /**
     * Creates an instructional offering
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
	public ActionForward addInstructionalOfferings(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
        InstructionalOfferingListForm frm = (InstructionalOfferingListForm)form;

	    Long subjAreaId = (frm.getSubjectAreaIds() == null || frm.getSubjectAreaIds().length < 1 ? null : Long.valueOf(frm.getSubjectAreaIds()[0]));
		String courseNbr = frm.getCourseNbr().trim();
	    ActionMessages errors = new ActionMessages();
	    
	    // Check if errors were found
	    if (!errors.isEmpty()) {
		    frm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
		    saveErrors(request, errors);
	        if (frm.getInstructionalOfferings() == null || frm.getInstructionalOfferings().isEmpty()) {
	        	return mapping.findForward("showInstructionalOfferingSearch");
			} else {
			    return mapping.findForward("showInstructionalOfferingList");
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
		            ActionRedirect redirect = new ActionRedirect(mapping.findForward("showInstructionalOfferingDetail"));
		            redirect.addParameter("op", "view");
		            redirect.addParameter("io", course.getInstructionalOffering().getUniqueId().toString());
		            return redirect;
			    }
	    	}
	    }

	    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showCourseOfferingEdit"));
        redirect.addParameter("op", MSG.actionAddCourseOffering());
        if (subjAreaId != null)
        	redirect.addParameter("subjAreaId", subjAreaId.toString());
        redirect.addParameter("courseNbr", courseNbr);
        return redirect;
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
}
