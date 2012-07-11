/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.impl.LocalizedLookupDispatchAction;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.Messages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.PdfWorksheet;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.pdf.PdfInstructionalOfferingTableBuilder;


/**
 * @author Stephanie Schluttenhofer
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

	    	sessionContext.checkPermission(null, "Department", Right.InstructionalOfferings);
	        
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

		    // Set Session Variables
		    sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, frm.getSubjectAreaId());
		    sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, frm.getCourseNbr());
	        

		    if ("1".equals(request.getParameter("loadInstrFilter"))) {
		    	setupInstrOffrListSpecificFormFilters(sessionContext, frm);
		    } else {
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.divSec", frm.getDivSec() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.demand", frm.getDemand() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.projectedDemand", frm.getProjectedDemand() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.minPerWk", frm.getMinPerWk() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.limit", frm.getLimit() ? "1" : "0");
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
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.consent", frm.getConsent() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.designatorRequired", frm.getDesignatorRequired() ? "1" : "0");
		    	sessionContext.getUser().setProperty("InstructionalOfferingList.sortBy", frm.getSortBy());
		    }
		    
		    if (!sessionContext.hasPermission(Right.Examinations))
		    	frm.setExams(null);
	        
	        // Perform Search
		    Collection instrOfferings = getInstructionalOfferings(sessionContext.getUser().getCurrentAcademicSessionId(), classAssignmentService.getAssignment(), frm);
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
						"instructionalOfferingSearch.do?op=Back&doit=Search&loadInstrFilter=1&subjectAreaId="+frm.getSubjectAreaId()+"&courseNbr="+frm.getCourseNbr(), 
						MSG.labelInstructionalOfferings() + " ("+
							(frm.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(new Long(frm.getSubjectAreaId()))).getSubjectAreaAbbreviation():frm.getSubjectAreaAbbv())+
							(frm.getCourseNbr()==null || frm.getCourseNbr().length()==0?"":" "+frm.getCourseNbr())+
							")", 
						true, true);

				if (request.getParameter("op")==null || 
			            (request.getParameter("op")!=null && !request.getParameter("op").equalsIgnoreCase("Back")) )  {
			        
				    // Search produces 1 result - redirect to offering detail
				    if(instrOfferings.size()==1) {
				    	InstructionalOffering io = (InstructionalOffering)instrOfferings.toArray()[0];
				    	if (sessionContext.hasPermission(io, Right.InstructionalOfferingDetail)) {
					        request.setAttribute("op", "view");
					        request.setAttribute("io", io.getUniqueId().toString());
					        
					        return mapping.findForward("showInstructionalOfferingDetail");
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
            File pdfFile = 
                (new PdfInstructionalOfferingTableBuilder())
                .pdfTableForInstructionalOfferings(
                        WebSolver.getClassAssignmentProxy(request.getSession()),
                        WebSolver.getExamSolver(request.getSession()),
                        frm, 
                        new Long(frm.getSubjectAreaId()), 
                        sessionContext,  
                        true, 
                        frm.getCourseNbr()==null || frm.getCourseNbr().length()==0);
            
            if (pdfFile!=null) {
                request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+pdfFile.getName());
                //response.sendRedirect("temp/"+pdfFile.getName());
            } else {
                getErrors(request).add("searchResult", new ActionMessage("errors.generic", MSG.errorUnableToCreatePdf()));
            }
        }
        
        return fwd;
	}
	
	public static void setupInstrOffrListSpecificFormFilters(SessionContext sessionContext, InstructionalOfferingListForm frm) {
		frm.setDivSec("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.divSec", "0")));	
		frm.setDemand("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.demand", "1")));	
		frm.setProjectedDemand("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.projectedDemand", "1")));	
		frm.setMinPerWk("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.minPerWk", "1")));	
		frm.setLimit("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.limit", "1")));
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
		frm.setDesignatorRequired("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.designatorRequired", "0")));
		frm.setSortBy(sessionContext.getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)));
		frm.setExams("1".equals(sessionContext.getUser().getProperty("InstructionalOfferingList.exams", "0")));		
	}
	
	
	public ActionForward worksheetPdf(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        ActionForward fwd = searchInstructionalOfferings(mapping, form, request, response);
        
        InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
        
        if (getErrors(request).isEmpty()) {
            
            try {
                File file = ApplicationProperties.getTempFile("worksheet", "pdf");
            
                PdfWorksheet.print(file, new SubjectAreaDAO().get(Long.valueOf(frm.getSubjectAreaId())), frm.getCourseNbr());
            
                if (file.exists()) 
                    request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
                else {
                    ActionMessages errors = getErrors(request);
                    errors.add("searchResult", new ActionMessage("errors.generic", MSG.errorUnableToCreateWorksheetPdfNoData()));
                    saveErrors(request, errors);
                }
            } catch (Exception e) {
                ActionMessages errors = getErrors(request);
                errors.add("searchResult", new ActionMessage("errors.generic", MSG.errorUnableToCreateWorksheetPdf(e.getMessage())));
                saveErrors(request, errors);
                Debug.error(e);
            }
        }
        
        return fwd;
	}
	
	/*
	public ActionForward saveNotOfferedChanges(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		    InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
		    
		    frm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
		    frm.setInstructionalOfferings(getInstructionalOfferings(request, frm));

		    if (frm.getInstructionalOfferings().isEmpty()) {
			    return mapping.findForward("showInstructionalOfferingSearch");
			} else {
			    Iterator it = frm.getInstructionalOfferings().iterator();
			    InstructionalOffering io = null;
			    InstructionalOfferingDAO dao = new InstructionalOfferingDAO(); 
			    while (it.hasNext()){
			        io = (InstructionalOffering) it.next();
			        dao.save(io);
			    }
			    return mapping.findForward("showInstructionalOfferingList");
			}
		}
		*/

    public static Set getInstructionalOfferings(Long sessionId, ClassAssignmentProxy classAssignmentProxy, InstructionalOfferingListForm form) {
        
        boolean fetchStructure = true;
        boolean fetchCredits = false;//singleCourseSelection || form.getCredit().booleanValue();
        boolean fetchInstructors = false;//singleCourseSelection || form.getInstructor().booleanValue();
        boolean fetchPreferences = false;//singleCourseSelection || form.getPreferences().booleanValue() || form.getTimePattern().booleanValue();
        boolean fetchAssignments = false;//singleCourseSelection || (form.getTimetable()!=null && form.getTimetable().booleanValue());
        boolean fetchReservations = false;//singleCourseSelection;
        
        TreeSet ts = InstructionalOffering.search(sessionId, form.getSubjectAreaId(), form.getCourseNbr(),
        		fetchStructure, fetchCredits, fetchInstructors, fetchPreferences, fetchAssignments, fetchReservations);
        
		if (form.getInstructor().booleanValue() || form.getPreferences().booleanValue() || form.getTimePattern().booleanValue()) {
			Debug.debug("---- Load Instructors ---- ");
			for (Iterator i=ts.iterator();i.hasNext();) {
				InstructionalOffering io = (InstructionalOffering)i.next();
				for (Iterator j=io.getInstrOfferingConfigs().iterator();j.hasNext();) {
					InstrOfferingConfig ioc = (InstrOfferingConfig)j.next();
					for (Iterator k=ioc.getSchedulingSubparts().iterator();k.hasNext();) {
						SchedulingSubpart s = (SchedulingSubpart)k.next();
						for (Iterator l=s.getClasses().iterator();l.hasNext();) {
							Class_ c = (Class_)l.next();
							Hibernate.initialize(c.getClassInstructors());
						}
					}
				}
			}
			for (Iterator i=ts.iterator();i.hasNext();) {
				InstructionalOffering io = (InstructionalOffering)i.next();
				for (Iterator j=io.getInstrOfferingConfigs().iterator();j.hasNext();) {
					InstrOfferingConfig ioc = (InstrOfferingConfig)j.next();
					for (Iterator k=ioc.getSchedulingSubparts().iterator();k.hasNext();) {
						SchedulingSubpart s = (SchedulingSubpart)k.next();
						for (Iterator l=s.getClasses().iterator();l.hasNext();) {
							Class_ c = (Class_)l.next();
							for (Iterator m=c.getClassInstructors().iterator();m.hasNext();) {
								ClassInstructor ci = (ClassInstructor)m.next();
								Hibernate.initialize(ci.getInstructor());
							}
						}
					}
				}
			}
		}
		
		if (form.getPreferences().booleanValue() || form.getTimePattern().booleanValue()) {
			Debug.debug("---- Load Preferences ---- ");
			for (Iterator i=ts.iterator();i.hasNext();) {
				InstructionalOffering io = (InstructionalOffering)i.next();
				for (Iterator j=io.getInstrOfferingConfigs().iterator();j.hasNext();) {
					InstrOfferingConfig ioc = (InstrOfferingConfig)j.next();
					for (Iterator k=ioc.getSchedulingSubparts().iterator();k.hasNext();) {
						SchedulingSubpart s = (SchedulingSubpart)k.next();
						Hibernate.initialize(s.getPreferences());
						Hibernate.initialize(s.getDistributionObjects());
						for (Iterator l=s.getClasses().iterator();l.hasNext();) {
							Class_ c = (Class_)l.next();
							Hibernate.initialize(c.getPreferences());
							for (Iterator m=c.getClassInstructors().iterator();m.hasNext();) {
								ClassInstructor ci = (ClassInstructor)m.next();
								Hibernate.initialize(ci.getInstructor().getPreferences());
							}
							c.getControllingDept().getPreferences();
							c.getManagingDept().getPreferences();
							Hibernate.initialize(c.getDistributionObjects());
						}
					}
				}
			}
		}
		
		if (form.getTimetable()!=null && form.getTimetable().booleanValue()) {
			Debug.debug("--- Load Assignments --- ");
			if (classAssignmentProxy!=null && classAssignmentProxy instanceof Solution) {
				for (Iterator i=ts.iterator();i.hasNext();) {
					InstructionalOffering io = (InstructionalOffering)i.next();
					for (Iterator j=io.getInstrOfferingConfigs().iterator();j.hasNext();) {
						InstrOfferingConfig ioc = (InstrOfferingConfig)j.next();
						for (Iterator k=ioc.getSchedulingSubparts().iterator();k.hasNext();) {
							SchedulingSubpart s = (SchedulingSubpart)k.next();
							for (Iterator l=s.getClasses().iterator();l.hasNext();) {
								Class_ c = (Class_)l.next();
								try {
									Assignment a = classAssignmentProxy.getAssignment(c);
									if (a!=null)
										Hibernate.initialize(a);
								} catch (Exception e) {}
							}
						}
					}
				}
			}
		}
        
        return (ts);
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
    	sessionContext.checkPermission(frm.getSubjectAreaId(), "SubjectArea", Right.AddCourseOffering);


	    Long subjAreaId = frm.getSubjectAreaId();
		String courseNbr = frm.getCourseNbr().trim();
	    ActionMessages errors = new ActionMessages();
	    
	    // Check blank subject area
	    if (subjAreaId == null) 
	        errors.add("subjAreaId", new ActionMessage("errors.required", "Subject Area"));
	        
	    // Check blank course number
	    if (courseNbr == null || courseNbr.isEmpty()) 
	        errors.add("courseNbr", new ActionMessage("errors.required", "Course Number"));
	    
	    // Check that course number matches a pattern
	    else {
	    	String courseNbrRegex = ApplicationProperties.getProperty("tmtbl.courseNumber.pattern");
	    	String courseNbrInfo = ApplicationProperties.getProperty("tmtbl.courseNumber.patternInfo");
	    	try { 
		    	Pattern pattern = Pattern.compile(courseNbrRegex);
		    	Matcher matcher = pattern.matcher(courseNbr);
		    	if (!matcher.find()) {
			        errors.add("courseNbr", new ActionMessage("errors.generic", courseNbrInfo));
		    	}
	    	} catch (Exception e) {
		        errors.add("courseNbr", new ActionMessage("errors.generic", MSG.errorCourseDoesNotMatchRegEx(courseNbrRegex, e.getMessage())));
	    	}
	    }
	    
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

	    // Convert to uppercase - e.g. 001d -> 001D
	    if ("true".equals(ApplicationProperties.getProperty("tmtbl.courseNumber.upperCase", "true")))
	    	courseNbr = courseNbr.toUpperCase();
	    frm.setCourseNbr(courseNbr);
	    
	    // Set Session Variables
        sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjAreaId.toString());
        sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, courseNbr);        
	    
	    // Offering exists - redirect to offering detail
    	String courseNumbersMustBeUnique = ApplicationProperties.getProperty("tmtbl.courseNumber.unique","true");

    	if (courseNumbersMustBeUnique.equalsIgnoreCase("true")) {
    		CourseOffering course = CourseOffering.findBySessionSubjAreaIdCourseNbr(sessionContext.getUser().getCurrentAcademicSessionId(), subjAreaId, courseNbr);
    		if (course != null) {
		        request.setAttribute("op", "view");
		        request.setAttribute("io", course.getInstructionalOffering().getUniqueId().toString());
		        return mapping.findForward("showInstructionalOfferingDetail");
		    }
    	}

	    // No Errors - create Course Offering	    
	    CourseOffering newCourseOffering = CourseOffering.addNew(subjAreaId, courseNbr);
	    	    	
	    // Offering exists - redirect to offering detail
	    if(newCourseOffering != null) {
	    	// Lock the offering, if needed
		    if (sessionContext.hasPermission(newCourseOffering.getInstructionalOffering(), Right.OfferingCanLock))
		    	newCourseOffering.getInstructionalOffering().getSession().lockOffering(newCourseOffering.getInstructionalOffering().getUniqueId());
	    	
	        request.setAttribute("op", MSG.actionEditCourseOffering());
	        request.setAttribute("courseOfferingId", newCourseOffering.getUniqueId().toString());
	        return mapping.findForward("showCourseOfferingEdit");
	    }
		
	    frm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
	    frm.setInstructionalOfferings(getInstructionalOfferings(sessionContext.getUser().getCurrentAcademicSessionId(), classAssignmentService.getAssignment(), frm));

	    return mapping.findForward("showInstructionalOfferingList");
		
	}
}
