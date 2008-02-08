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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.util.MessageResources;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ClassListForm;
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.PdfWorksheet;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.pdf.PdfInstructionalOfferingTableBuilder;


/**
 * @author Stephanie Schluttenhofer
 */

public class InstructionalOfferingSearchAction extends LookupDispatchAction {

	protected Map getKeyMethodMap() {
	      Map map = new HashMap();
	      map.put("button.searchInstructionalOfferings", "searchInstructionalOfferings");
	      map.put("button.exportPDF", "exportPdf");
	      map.put("button.worksheetPDF", "worksheetPdf");
	      map.put("button.saveNotOfferedChanges", "saveNotOfferedChanges");
	      map.put("button.addNew", "addInstructionalOfferings");
	      map.put("button.update", "updateInstructionalOfferings");
	      map.put("button.delete", "deleteCourseOffering");
	      map.put("button.cancel", "searchInstructionalOfferings");
	      return map;
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

	    	HttpSession httpSession = request.getSession();
	        if(!Web.isLoggedIn( httpSession )) {
	            throw new Exception ("Access Denied.");
	        }
        
	        // Check that a valid subject area is selected
		    InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
		    ActionMessages errors = null;
		    errors = frm.validate(mapping, request);
		    
		    // Validation fails
		    if(errors.size()>0) {
			    saveErrors(request, errors);
			    frm.setCollections(request, null);
			    return mapping.findForward("showInstructionalOfferingSearch");
		    }

		    // Set Session Variables
	        httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, frm.getSubjectAreaId());
	        httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, frm.getCourseNbr());
	        

		    if ("1".equals(request.getParameter("loadInstrFilter"))) {
				setupInstrOffrListSpecificFormFilters(httpSession, frm);
		    } else {
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.divSec",frm.getDivSec().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.demand",frm.getDemand().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.projectedDemand",frm.getProjectedDemand().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.minPerWk",frm.getMinPerWk().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.limit",frm.getLimit().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.roomLimit",frm.getRoomLimit().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.manager",frm.getManager().booleanValue());
			   	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.datePattern",frm.getDatePattern().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.timePattern",frm.getTimePattern().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.instructor",frm.getInstructor().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.preferences",frm.getPreferences().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.timetable",(frm.getTimetable()==null?false:frm.getTimetable().booleanValue()));
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.credit",frm.getCredit().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.subpartCredit",frm.getSubpartCredit().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.schedulePrintNote",frm.getSchedulePrintNote().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.note",frm.getNote().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.title",frm.getTitle().booleanValue());
                if (frm.getCanSeeExams())
                    UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.exams",frm.getExams().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.consent",frm.getConsent().booleanValue());
		    	UserData.setPropertyBoolean(httpSession,"InstructionalOfferingList.designatorRequired",frm.getDesignatorRequired().booleanValue());
		    	UserData.setProperty(httpSession,"InstructionalOfferingList.sortBy",frm.getSortBy());
		    }

	        
	        // Perform Search
		    frm.setCollections(request, getInstructionalOfferings(request, frm));
			Collection instrOfferings = frm.getInstructionalOfferings();
			
			// No results returned
			if (instrOfferings.isEmpty()) {
			    if(errors==null) errors = new ActionMessages();
			    errors.add("searchResult", new ActionMessage("errors.generic", "No records matching the search criteria were found."));
			    saveErrors(request, errors);
			    return mapping.findForward("showInstructionalOfferingSearch");
			} 
			else {

				BackTracker.markForBack(
						request, 
						"instructionalOfferingSearch.do?op=Back&doit=Search&loadInstrFilter=1&subjectAreaId="+frm.getSubjectAreaId()+"&courseNbr="+frm.getCourseNbr(), 
						"Instructional Offerings ("+
							(frm.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(new Long(frm.getSubjectAreaId()))).getSubjectAreaAbbreviation():frm.getSubjectAreaAbbv())+
							(frm.getCourseNbr()==null || frm.getCourseNbr().length()==0?"":" "+frm.getCourseNbr())+
							")", 
						true, true);

				if (request.getParameter("op")==null || 
			            (request.getParameter("op")!=null && !request.getParameter("op").equalsIgnoreCase("Back")) )  {
			        
				    // Search produces 1 result - redirect to offering detail
				    if(instrOfferings.size()==1) {
				    	InstructionalOffering io = (InstructionalOffering)instrOfferings.toArray()[0];
				        request.setAttribute("op", "view");
				        request.setAttribute("io", io.getUniqueId().toString());
				        
				        return mapping.findForward("showInstructionalOfferingDetail");
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
                        frm, 
                        new Long(frm.getSubjectAreaId()), 
                        Web.getUser(request.getSession()),  
                        true, 
                        frm.getCourseNbr()==null || frm.getCourseNbr().length()==0);
            
            if (pdfFile!=null) {
                request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+pdfFile.getName());
                //response.sendRedirect("temp/"+pdfFile.getName());
            } else {
                getErrors(request).add("searchResult", new ActionMessage("errors.generic", "Unable to create PDF file."));
            }
        }
        
        return fwd;
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
                    errors.add("searchResult", new ActionMessage("errors.generic", "Unable to create worksheet PDF file -- nothing to export."));
                    saveErrors(request, errors);
                }
            } catch (Exception e) {
                ActionMessages errors = getErrors(request);
                errors.add("searchResult", new ActionMessage("errors.generic", "Unable to create worksheet PDF file -- "+e.getMessage()+"."));
                saveErrors(request, errors);
                Debug.error(e);
            }
        }
        
        return fwd;
	}
	
	public ActionForward saveNotOfferedChanges(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		    InstructionalOfferingListForm instructionalOfferingListForm = (InstructionalOfferingListForm) form;
		    instructionalOfferingListForm.setCollections(request, getInstructionalOfferings(request, instructionalOfferingListForm));
			if (instructionalOfferingListForm.getInstructionalOfferings().isEmpty()) {
			    return mapping.findForward("showInstructionalOfferingSearch");
			} else {
			    Iterator it = instructionalOfferingListForm.getInstructionalOfferings().iterator();
			    InstructionalOffering io = null;
			    InstructionalOfferingDAO dao = new InstructionalOfferingDAO(); 
			    while (it.hasNext()){
			        io = (InstructionalOffering) it.next();
			        dao.save(io);
			    }
			    return mapping.findForward("showInstructionalOfferingList");
			}
		}

    public static Set getInstructionalOfferings(
            HttpServletRequest request, InstructionalOfferingListForm form) {
        
        HttpSession httpSession = request.getSession();
        User user = Web.getUser(httpSession);
        Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
        
        boolean singleCourseSelection = (form.getCourseNbr()!=null && form.getCourseNbr().length()>0);
        
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
			ClassAssignmentProxy classAssignmentProxy = WebSolver.getClassAssignmentProxy(request.getSession());
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

        HttpSession httpSession = request.getSession();
        if(!Web.isLoggedIn( httpSession )) {
            throw new Exception ("Access Denied.");
        }
        
        User user = Web.getUser(httpSession);
        Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);

        InstructionalOfferingListForm instructionalOfferingListForm = (InstructionalOfferingListForm) form;
		String courseNbr = instructionalOfferingListForm.getCourseNbr();
	    String subjAreaId = instructionalOfferingListForm.getSubjectAreaId();
	    ActionMessages errors = new ActionMessages();
	    
	    // Check blank subject area
	    if(subjAreaId==null || subjAreaId.trim().length()==0) 
	        errors.add("subjAreaId", new ActionMessage("errors.required", "Subject Area"));
	        
	    // Check blank course number
	    if(courseNbr==null || courseNbr.trim().length()==0) 
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
	    	}
	    	catch (Exception e) {
		        errors.add("courseNbr", new ActionMessage("errors.generic", "Course Number cannot be matched to regular expression: " + courseNbrRegex + ". Reason: " + e.getMessage()));
	    	}
	    }
	    
	    // Check if errors were found
	    if(!errors.isEmpty()) {
	        instructionalOfferingListForm.setCollections(request, null);
	        saveErrors(request, errors);
	        if (instructionalOfferingListForm.getInstructionalOfferings()==null
	                || instructionalOfferingListForm.getInstructionalOfferings().isEmpty()) {
			    return mapping.findForward("showInstructionalOfferingSearch");
			} else {
			    return mapping.findForward("showInstructionalOfferingList");
			}
	    }

	    // Convert to uppercase - e.g. 001d -> 001D
	    courseNbr = courseNbr.toUpperCase();
	    instructionalOfferingListForm.setCourseNbr(courseNbr);
	    
	    instructionalOfferingListForm.setCollections(request, getInstructionalOfferings(request, instructionalOfferingListForm));

	    // Set Session Variables
        httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, instructionalOfferingListForm.getSubjectAreaId());
        httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, instructionalOfferingListForm.getCourseNbr());        
	    
	    // Offering exists - redirect to offering detail
	    List l = CourseOffering.search(sessionId, subjAreaId, courseNbr);
	    if(l.size()>0) {
	        // errors.add("courseNbr", new ActionMessage("errors.exists", courseNbr));	        
	        InstructionalOffering io = ((CourseOffering) l.get(0)).getInstructionalOffering();
	        request.setAttribute("op", "view");
	        request.setAttribute("io", io.getUniqueId().toString());
	        return mapping.findForward("showInstructionalOfferingDetail");
	    }

	    // No Errors - create Course Offering	    
	    CourseOffering.addNew(subjAreaId, courseNbr);
	    
        if(!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        Debug.debug("before get collection");
	    instructionalOfferingListForm.setCollections(request, getInstructionalOfferings(request, instructionalOfferingListForm));
		Debug.debug("after get collection size = " + instructionalOfferingListForm.getInstructionalOfferings().size());
		
	    // Offering exists - redirect to offering detail
	    List l2 = CourseOffering.search(sessionId, subjAreaId, courseNbr);
	    if(l2.size()>0) {
	        // errors.add("courseNbr", new ActionMessage("errors.exists", courseNbr));
	    	/*
	        InstructionalOffering io = ((CourseOffering) l2.get(0)).getInstructionalOffering();
	        request.setAttribute("op", "view");
	        request.setAttribute("io", io.getUniqueId().toString());
	        return mapping.findForward("showInstructionalOfferingDetail");
	    	*/
	        MessageResources rsc = getResources(request);
	        request.setAttribute("op", rsc.getMessage("button.editCourseOffering"));
	        request.setAttribute("courseOfferingId", ((CourseOffering) l2.get(0)).getUniqueId().toString());
	        return mapping.findForward("showCourseOfferingEdit");
	    }
		
		return mapping.findForward("showInstructionalOfferingList");
		
	}

	
	/**
	 * Updates changes made to instructional offerings
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateInstructionalOfferings(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

        HttpSession httpSession = request.getSession();
        if(!Web.isLoggedIn( httpSession )) {
            throw new Exception ("Access Denied.");
        }
        
        User user = Web.getUser(httpSession);
        Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
        
        // Read values
	    InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
	    ActionMessages errors = new ActionMessages();
        
        String subjAreaId = frm.getSubjectAreaId();
        String courseNbr = frm.getCourseNbr();
        String ctrCrsOffrId = frm.getCtrlInstrOfferingId();	        
        Boolean isControl = frm.getIsControl();
        
        if(ctrCrsOffrId==null) ctrCrsOffrId = "";
        if(isControl==null) isControl = new Boolean(false);
        
        frm.setCollections(request, getInstructionalOfferings(request, frm));
	    frm.setSubjectAreaAbbv(new SubjectAreaDAO().get(new Long(subjAreaId)).getSubjectAreaAbbreviation());

        List l = CourseOffering.search(sessionId, subjAreaId, courseNbr);
	    if(l.size()>0) {
	        
            CourseOfferingDAO cdao = new CourseOfferingDAO();
    	    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
    	    org.hibernate.Session hibSession = idao.getSession();
    	    Transaction tx = hibSession.beginTransaction();
    	    
    	    try {
		        // Update
		        CourseOffering co = (CourseOffering) l.get(0);
		        InstructionalOffering io = co.getInstructionalOffering();

		        String ctrCrsOffrId2 = io.getCtrlCourseId().toString();
		        Boolean isControl2 = co.isIsControl();
		        
		        if(ctrCrsOffrId2==null) ctrCrsOffrId2 = "";
		        if(isControl2==null) isControl2 = new Boolean(false);

		        // Check if value is changed
		        if(isControl2.booleanValue()!=isControl.booleanValue() 
		               || !ctrCrsOffrId2.equals(ctrCrsOffrId) ) {

		            // Control flag is changed
		            if(isControl2.booleanValue()!=isControl.booleanValue()) {
		                
		                
		                // It is now a controlling course
		                if(isControl.booleanValue()) {
		                    co.setIsControl(isControl);
		                    
		                    // Loop through IO and update other controlling course to false
		                    Set offerings = io.getCourseOfferings();
		                    Iterator iter = offerings.iterator();
		                    while(iter.hasNext()) {
		                        CourseOffering co2 = (CourseOffering) iter.next();
		                        if(co2.getUniqueId().intValue()!=co.getUniqueId().intValue() && co2.isIsControl().booleanValue()) {
		                            co2.setIsControl(new Boolean(false));
		                        }
		                    }
		                    idao.saveOrUpdate(io);
		                }
		                // It is now NOT a controlling course
		                else {

			                // Check that controlling course is not the same
		                    if(ctrCrsOffrId2.equals(ctrCrsOffrId) || ctrCrsOffrId.trim().length()==0) {
		        		        errors.add("ctrlInstrOfferingId", new ActionMessage("errors.ctrlCourse.invalid"));
		                    } 
		                    else {
		                        
		                        // Has other course offerings attached to it
		                        if(io.getCourseOfferings().size()>1) {
			        		        errors.add("ctrlInstrOfferingId", new ActionMessage("errors.ctrlCourse.multipleChildren"));
		                        }
		                        else {
								    io.removeCourseOffering(co);
								    io.setCourseOfferings(null);
								    co.setIsControl(new Boolean(false));
								    co.setInstructionalOffering(null);
								    Exam.deleteFromExams(hibSession, io);
								    idao.delete(io);
								    
			                        CourseOffering co2 = cdao.get(new Long(ctrCrsOffrId));
			                        InstructionalOffering io2 = co2.getInstructionalOffering();
			                        io2.addTocourseOfferings(co);
			                        co.setInstructionalOffering(io2);
			                        idao.save(io2);
		                        }
		                    }
		                }
		            }
		            else {
			            // Controlling course has changed
			            if(!ctrCrsOffrId2.equals(ctrCrsOffrId)) {
			                // Check that is not a controlling course
			                if(!isControl2.booleanValue()) {
		                        // Has other course offerings attached to it
		                        if(io.getCourseOfferings().size()>1 && isControl.booleanValue()) {
			        		        errors.add("ctrlInstrOfferingId", new ActionMessage("errors.ctrlCourse.multipleChildren"));
		                        }
		                        else {
								    io.removeCourseOffering(co);
								    co.setInstructionalOffering(null);
								    idao.save(io);

								    CourseOffering co2 = cdao.get(new Long(ctrCrsOffrId));
			                        InstructionalOffering io2 = co2.getInstructionalOffering();
			                        io2.addTocourseOfferings(co);
			                        co.setInstructionalOffering(io2);
			                        idao.save(io2);
		                        }				                    
			                }
			                // Ambiguous - cannot have a controlling course if it is controlling 
			                else {
			                    errors.add("ctrlInstrOfferingId", 
	                            	new ActionMessage("errors.exception", 
	                            	    "Ambiguous operation requested - cannot assign a controlling offering it is flagged as a controlling course"));
			                }
			            }
		            }

				    tx.commit();
			    }
	        }
		    catch (Exception e) {
		        tx.rollback();	   
		        Debug.error(e);
		        errors.add("", new ActionMessage("errors.exception", "ERRORS: " + e.getMessage()));
			    addErrors(request, errors);
			    return mapping.findForward("editInstructionalOffering");
		    }		            
	    }
	    else {
	        String crsName = frm.getSubjectAreaAbbv() + " " + frm.getCourseNbr();
	        errors.add("courseNbr", new ActionMessage("errors.lookup.notFound", "Course Offering: " + crsName ));
	    }

	    addErrors(request, errors);
	    if(errors.size()>0) 
		    return mapping.findForward("editInstructionalOffering");

	    frm.setCollections(request, getInstructionalOfferings(request, frm));
	    return mapping.findForward("showInstructionalOfferingList");
	}


	public ActionForward deleteCourseOffering(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

        HttpSession httpSession = request.getSession();
        if(!Web.isLoggedIn( httpSession )) {
            throw new Exception ("Access Denied.");
        }
        
        User user = Web.getUser(httpSession);
        Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);

        InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
        ActionMessages errors = new ActionMessages();
        
        String subjAreaId = frm.getSubjectAreaId();
        String courseNbr = frm.getCourseNbr();
        List l = CourseOffering.search(sessionId, subjAreaId, courseNbr);
	    if(l.size()>0) {
	        // Delete
	        InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        CourseOffering co = (CourseOffering) l.get(0);
	        InstructionalOffering io = co.getInstructionalOffering();
	        
    	    org.hibernate.Session hibSession = idao.getSession();
    	    Transaction tx = hibSession.beginTransaction();
    	    
    	    try {
                Exam.deleteFromExams(hibSession, co);
                
    	        if(co.isIsControl().booleanValue()) {
    	            Exam.deleteFromExams(hibSession, io);
    	            idao.delete(io);
    	        } else {
    	            io.removeCourseOffering(co);
    	            idao.save(io);
    	        }
    	        
		        tx.commit();
    	    }	            
		    catch (Exception e) {
		        tx.rollback();	   
		        Debug.error(e);
		        errors.add("subjectAreaId", new ActionMessage("errors.exception", e.getMessage()));
		        addErrors(request, errors);
			    return mapping.findForward("addInstructionalOffering");
		    }		            
	    }     

	    // Redirect back to search
	    frm.setSubjectAreaId(subjAreaId);
	    frm.setCourseNbr("");
	    frm.setCollections(request, getInstructionalOfferings(request, frm));
	    return mapping.findForward("showInstructionalOfferingList");
	}
	public static void setupInstrOffrListSpecificFormFilters(HttpSession httpSession, InstructionalOfferingListForm form){
		form.setDivSec(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.divSec", false)));	
		form.setDemand(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.demand", true)));	
		form.setProjectedDemand(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.projectedDemand", true)));	
		form.setMinPerWk(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.minPerWk", true)));	
		form.setLimit(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.limit", true)));
		form.setRoomLimit(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.roomLimit", true)));
		form.setManager(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.manager", true)));	
		form.setDatePattern(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.datePattern", true)));
		form.setTimePattern(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.timePattern", true)));
		form.setInstructor(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.instructor", true)));
		form.setPreferences(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.preferences", true)));
		form.setTimetable(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.timetable", true)));	
		form.setCredit(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.credit", false)));
		form.setSubpartCredit(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.subpartCredit", false)));
		form.setSchedulePrintNote(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.schedulePrintNote", true)));
		form.setNote(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.note", false)));
		form.setTitle(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.title", false)));
		form.setConsent(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.consent", false)));
		form.setDesignatorRequired(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.designatorRequired", false)));
		form.setSortBy(UserData.getProperty(httpSession,"InstructionalOfferingList.sortBy",ClassListForm.sSortByName));
        try {
            User user = Web.getUser(httpSession);
            TimetableManager manager = TimetableManager.getManager(user);
            Session session = Session.getCurrentAcadSession(user);
            if (manager.canSeeExams(session, user)) {
                form.setCanSeeExams(Boolean.TRUE);
                form.setExams(new Boolean(UserData.getPropertyBoolean(httpSession,"InstructionalOfferingList.exams", false)));
            } else {
                form.setCanSeeExams(Boolean.FALSE);
            }
        } catch (Exception e) {}
	}

}
