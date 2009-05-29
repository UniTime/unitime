/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.CrossListsModifyForm;
import org.unitime.timetable.interfaces.ExternalCourseCrosslistAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingRemoveAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingInCrosslistAddAction;
import org.unitime.timetable.model.AcadAreaReservation;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ReservationType;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 07-15-2005
 * 
 * XDoclet definition:
 * @struts:action path="/courseOfferingEdit" name="instructionalOfferingListForm" input="/user/instructionalOfferingSearch.jsp" scope="request"
 */
public class CrossListsModifyAction extends Action {

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
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        if(!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        HttpSession httpSession = request.getSession();
        MessageResources rsc = getResources(request);
        User user = Web.getUser(request.getSession());        
        CrossListsModifyForm frm = (CrossListsModifyForm) form;
        
        // Get operation
        String op = (request.getParameter("op")==null) 
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");		        
        			        
        if(op==null)
            op = request.getParameter("hdnOp");
        
        if(op==null || op.trim().length()==0)
            throw new Exception ("Operation could not be interpreted: " + op);

        // Course Offering Id
        String courseOfferingId = "";

        // Set up Lists
        frm.setOp(op);
        LookupTables.setupCourseOfferings(request);
        
        // First access to screen
        if(op.equalsIgnoreCase(rsc.getMessage("button.crossLists"))) {
            
		    courseOfferingId = (request.getParameter("uid")==null)
								? (request.getAttribute("uid")==null)
								        ? null
								        : request.getAttribute("uid").toString()
								: request.getParameter("uid");

            doLoad(frm, courseOfferingId, user);
        }
        
        // Add a course offering
        if(op.equalsIgnoreCase(rsc.getMessage("button.add"))) {
            // Validate data input
            ActionMessages errors = frm.validate(mapping, request);

            if(errors.size()==0) {
                Long addedOffering = frm.getAddCourseOfferingId();
                CourseOfferingDAO cdao = new CourseOfferingDAO();
                CourseOffering co = cdao.get(addedOffering);
                
                // Check reservations limit
                InstructionalOffering io = co.getInstructionalOffering();
                frm.addToCourseOfferings(co, getCourseReservation(io, co), new Boolean(co.isEditableBy(user)));
                frm.setAddCourseOfferingId(null);
            }
            else {
                saveErrors(request, errors);
            }
        }
        
        // Remove a course offering
        if(op.equalsIgnoreCase(rsc.getMessage("button.delete"))) {
            String deletedOffering = request.getParameter("deletedCourseOfferingId");
            if(deletedOffering!=null && deletedOffering.trim().length()>0)
                frm.removeFromCourseOfferings(deletedOffering);
        }
        
        // Update the course offering
        if(op.equalsIgnoreCase(rsc.getMessage("button.update"))) {
            // Validate data input
            ActionMessages errors = frm.validate(mapping, request);
            
            if(errors.size()==0) {
                doUpdate(request, frm);
                request.setAttribute("io", frm.getInstrOfferingId());
                return mapping.findForward("instructionalOfferingDetail");
            }
            else {
                saveErrors(request, errors);
            }
        }
        
        // Determine if a course offering cannot be deleted
        setReadOnlyCourseId(request, frm);
        
        // Remove the courses that are already part of this offering from list of courses
        filterCourseOfferingList(request, frm);
        
        return mapping.findForward("crossListsModify");
    }

    /**
     * Ensures that all offerings that are part of the instructional offering
     * does not appear in the drop down list 
     * @param request
     * @param frm
     */
    private void filterCourseOfferingList(HttpServletRequest request, CrossListsModifyForm frm) {
        Collection existingOfferings = frm.getCourseOfferingNames();
        Collection offerings = (Collection) request.getAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME);
        
        for (Iterator i=offerings.iterator(); i.hasNext(); ) {
            CourseOffering co = (CourseOffering) i.next();
            for (Iterator j=existingOfferings.iterator(); j.hasNext(); ) {
                String course = (String) j.next();
                if (course.equals(co.getCourseName()))
                    i.remove();
            }
        }
        
        request.setAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME, offerings);        
    }

    /**
     * Compares the modified offering to the original offering
     * If more than one offering is in common then all offerings show the 'Delete' icon
     * If only one offering is in common that offering cannot be deleted
     * @param request
     * @param frm
     */
    private void setReadOnlyCourseId(HttpServletRequest request, CrossListsModifyForm frm) {
        short ct = 0;
        String originalOfferings = frm.getOriginalOfferings();
        List courseOfferingIds = frm.getCourseOfferingIds();
        
        for (Iterator i=courseOfferingIds.iterator(); i.hasNext(); ) {
            String cid = i.next().toString();
            if(originalOfferings.indexOf(cid)>0) {
                
                // More than one course from the original offering exists in the modified one
                if(++ct>1) {
                    frm.setReadOnlyCrsOfferingId(null);
                    break;
                }
                else {
                    frm.setReadOnlyCrsOfferingId(new Long(cid));
                }                    
            }
        }        

        Debug.debug("Read Only Ctr Course: " + frm.getReadOnlyCrsOfferingId());
    }

    /**
     * Update the instructional offering
     * @param request
     * @param frm
     */
    private void doUpdate(HttpServletRequest request, CrossListsModifyForm frm) 
    	throws Exception {
        
        // Get the modified offering
        List ids = frm.getCourseOfferingIds();
        String courseIds = Constants.arrayToStr(ids.toArray(), "", " ");
        String origCourseIds = frm.getOriginalOfferings();
        
        // Get Offering
        CourseOfferingDAO cdao = new CourseOfferingDAO();
        InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
        InstructionalOffering io = idao.get(frm.getInstrOfferingId());
        Session hibSession = idao.getSession();
        hibSession.setFlushMode(FlushMode.MANUAL);
        Transaction tx = null;
        HashMap saList = new HashMap();
        
        ReservationType permResvType=ReservationType.getReservationTypebyRef(Constants.RESV_TYPE_PERM_REF);
        
        try {
	        tx = hibSession.beginTransaction();
	        StringTokenizer strTok = new StringTokenizer(origCourseIds);
	        
	        while (strTok.hasMoreTokens()) {
	            
	            String origCrs = strTok.nextToken();
	            
		        // 1. For all deleted courses - create new offering and make 'not offered'
	            if (courseIds.indexOf(origCrs)<0) {
	                Debug.debug("Course removed from offering: " + origCrs);
	                
	                // Create new instructional offering 
	                InstructionalOffering io1 = new InstructionalOffering();
	                CourseOffering co1 = cdao.get(new Long(origCrs.trim()));
	                
	                // Copy attributes of old instr offering - make not offered
	                io1.setDemand(io.getDemand());
	                io1.setLimit(io.getLimit());
	                io1.setNotOffered(new Boolean(true));
	                io1.setSession(io.getSession());
	                io1.setProjectedDemand(io.getProjectedDemand());
	                io1.setDesignatorRequired(new Boolean(false));
	                
	                // Copy attributes of old crs offering - set controlling	                
                    CourseOffering co2 = (CourseOffering)co1.clone();                    
                    co2.setIsControl(new Boolean(true));

                    // Copy academic reservations from course offering
                    HashSet resvs = new HashSet(); 
                    Set resv1 = co1.getAcadAreaReservations();
                    if (resv1==null) {
                        resvs.add(null);
                    }
                    else {
	                    for (Iterator resvIter=resv1.iterator(); resvIter.hasNext();) {
	                    	
	                        AcadAreaReservation ar1 = (AcadAreaReservation) resvIter.next();
	                        
	                        AcadAreaReservation ar2 = new AcadAreaReservation();
	                        ar2.setAcademicArea(ar1.getAcademicArea());
	                        ar2.setAcademicClassification(ar1.getAcademicClassification());
	                        ar2.setOwnerClassId(ar1.getOwnerClassId());
	                        ar2.setPriorEnrollment(ar1.getPriorEnrollment());
	                        ar2.setPriority(ar1.getPriority());
	                        ar2.setProjectedEnrollment(ar1.getProjectedEnrollment());
	                        ar2.setRequested(ar1.getRequested());
	                        ar2.setReservationType(ar1.getReservationType());
	                        ar2.setReserved(ar1.getReserved());
	                        resvs.add(ar2);
	                        
	                        // Delete academic area reservations
	                        Debug.debug("Removing academic area reservation from course offering");
	                        hibSession.delete(ar1);
	                        resvIter.remove();
	                    }
                    }
                    co1.setAcadAreaReservations(null);
                    
/*	                
	                hibSession.saveOrUpdate(io1);
	                hibSession.flush();
*/
	                // Remove from original inst offr
			        Set offerings = io.getCourseOfferings();
			        for (Iterator i=offerings.iterator(); i.hasNext(); ) {
			            
			            CourseOffering co3 = (CourseOffering) i.next();
			            if (co3.equals(co1)) {
		                    // Remove from Subject Area
					        SubjectArea sa = co3.getSubjectArea();
					        sa.getCourseOfferings().remove(co1);
			                hibSession.saveOrUpdate(sa);
			                saList.put(sa.getSubjectAreaAbbreviation(), sa);
			            }
			        }
			        
			        // Delete old course offering
			        io.removeCourseOffering(co1);
			        
			        // Remove the course from course reservations - if more than one course still exists in the offering 
			        // Remove all course reservations - if only one course exists in the offering
		            Set ioResv = io.getCourseReservations();
		            for (Iterator iterR=ioResv.iterator(); iterR.hasNext(); ) {
		                CourseOfferingReservation resv = (CourseOfferingReservation) iterR.next();
		                if ( (ids.size()>1 && resv.getCourseOffering().getUniqueId().equals(co1.getUniqueId()))
		                      || ids.size()==1  ) {
	                        Debug.debug("Removing course offering from course offering reservations");
	                        resv.getCourseOffering().getCourseReservations().remove(resv);
		                    iterR.remove();
		                    hibSession.delete(resv);
		                }
		            }
			            
		            Set configs = io.getInstrOfferingConfigs();
		            for (Iterator iterCfg=configs.iterator(); iterCfg.hasNext(); ) {
		                InstrOfferingConfig config = (InstrOfferingConfig) iterCfg.next();
		                
		                Set cfgResv = config.getCourseReservations();
			            for (Iterator iterR=cfgResv.iterator(); iterR.hasNext(); ) {
			                CourseOfferingReservation resv = (CourseOfferingReservation) iterR.next();
			                if ( (ids.size()>1 && resv.getCourseOffering().getUniqueId().equals(co1.getUniqueId()))
			                      || ids.size()==1  ) {
			                    resv.getCourseOffering().getCourseReservations().remove(resv);
			                    iterR.remove();
			                    hibSession.delete(resv);
			                }
			            }
				        hibSession.saveOrUpdate(config);
				        
			            Set subparts = config.getSchedulingSubparts();;
			            for (Iterator iterSubparts=subparts.iterator(); iterSubparts.hasNext(); ) {
			                SchedulingSubpart subpart = (SchedulingSubpart) iterSubparts.next();
			                
			                Set classes = subpart.getClasses();
				            for (Iterator iterClasses=classes.iterator(); iterClasses.hasNext(); ) {
				                Class_ cls = (Class_) iterClasses.next();
				                
				                Set clsResv = cls.getCourseReservations();
					            for (Iterator iterR=clsResv.iterator(); iterR.hasNext(); ) {
					                CourseOfferingReservation resv = (CourseOfferingReservation) iterR.next();
					                if ( (ids.size()>1 && resv.getCourseOffering().getUniqueId().equals(co1.getUniqueId()))
					                      || ids.size()==1  ) {
					                    resv.getCourseOffering().getCourseReservations().remove(resv);
					                    iterR.remove();
					                    hibSession.delete(resv);
					                }
					            }
						        hibSession.saveOrUpdate(cls);
				            }			                
			            }
		            }

                    Event.deleteFromEvents(hibSession, co1);
		            Exam.deleteFromExams(hibSession, co1);
			        hibSession.delete(co1);
			        
			        //io.setCourseOfferings(offerings);
			        
		            hibSession.saveOrUpdate(io);
		            hibSession.flush();
	                
	                // Add course to instructional offering
	                co2.setInstructionalOffering(io1);
	                io1.addTocourseOfferings(co2);

	                // Update
                    if (io1.getInstrOfferingPermId()==null) io1.generateInstrOfferingPermId();
	                hibSession.saveOrUpdate(io1);
	                hibSession.flush();

	    	        hibSession.refresh(io);
	                hibSession.refresh(io1);
	            	String className = ApplicationProperties.getProperty("tmtbl.external.instr_offr_in_crosslist.add_action.class");
	            	if (className != null && className.trim().length() > 0){
	            		ExternalInstructionalOfferingInCrosslistAddAction addAction = (ExternalInstructionalOfferingInCrosslistAddAction) (Class.forName(className).newInstance());
	    	       		addAction.performExternalInstructionalOfferingInCrosslistAddAction(io1, hibSession);
	            	}
	                
	                // Add reservations to newly created offering
		            if (resvs!=null) {
		                hibSession.refresh(co2);
	                    for (Iterator resvIter=resvs.iterator(); resvIter.hasNext();) {
	                        AcadAreaReservation ar = (AcadAreaReservation) resvIter.next();
	                        ar.setOwner(co2.getUniqueId());
	                        hibSession.saveOrUpdate(ar);
	                    }
	                    co2.getAcadAreaReservations().addAll(resvs);
	                    hibSession.saveOrUpdate(co2);
		                hibSession.flush();
		            }
	            }
	            
	            // 2. For all existing courses - update controlling attribute and reservation limits
	            else {
	                Debug.debug("Updating controlling course  and course reservation: " + origCrs);	                

	                // Update controlling course attribute
	                CourseOffering co = cdao.get(new Long (origCrs));
	                if(frm.getCtrlCrsOfferingId().equals(co.getUniqueId()))
	                    co.setIsControl(new Boolean(true));
	                else
	                    co.setIsControl(new Boolean(false));
	                hibSession.saveOrUpdate(co);
	                
	                // Update course reservation
	                CourseOffering co1 = cdao.get(new Long(origCrs.trim()));                
	                CourseOfferingReservation cor1 = getCourseReservation(io, co1);
	                int indx = frm.getIndex(origCrs);
	                
	                if (ids.size()>1) {
		                if (cor1==null) {
		                    cor1 = new CourseOfferingReservation();
		                    cor1.setCourseOffering(co1);
		                    cor1.setOwner(io.getUniqueId());
		                    cor1.setOwnerClassId(Constants.RESV_OWNER_IO);
		                    cor1.setPriorEnrollment(null);
		                    cor1.setPriority(new Integer(1));
		                    cor1.setProjectedEnrollment(null);
		                    cor1.setRequested(null);
		                    cor1.setReservationType(permResvType);
			                cor1.setReserved(Integer.valueOf(frm.getLimits(indx)));
			                io.getCourseReservations().add(cor1);
			                hibSession.saveOrUpdate(cor1);
			                hibSession.saveOrUpdate(io);
		                }
		                else {
			                cor1.setReserved(Integer.valueOf(frm.getLimits(indx)));
			                hibSession.saveOrUpdate(cor1);	                
		                }
	                }
	                
	                hibSession.flush();
	                hibSession.refresh(co);
	            }
	        }
	        
	        // 3. For all added courses - delete all preferences and change the instr offering id  
	        Vector addedOfferings = new Vector();
	        Vector addedResvs = new Vector();
	        Vector addedCourseResvs = new Vector();
	        StringTokenizer strTok2 = new StringTokenizer(courseIds);
	
	        while (strTok2.hasMoreTokens()) {
	            String course = strTok2.nextToken();
	            
	            // Course added to offering
	            if (origCourseIds.indexOf(course)<0) {
	                
	                Debug.debug("Course added to offering: " + course);
	
	                CourseOffering co1 = cdao.get(new Long(course.trim()));                
	                InstructionalOffering io1 = co1.getInstructionalOffering();
	                SubjectArea sa = io1.getControllingCourseOffering().getSubjectArea();
	                Set offerings = io1.getCourseOfferings();
	                
	                // Copy course offerings
	                for (Iterator i=offerings.iterator(); i.hasNext(); ) {
	                    CourseOffering co2 = (CourseOffering) i.next();
	                    SubjectArea sa2 = co2.getSubjectArea();
	                    
	                    // Create a copy
	                    CourseOffering co3 = (CourseOffering)co2.clone();
	                    if(frm.getCtrlCrsOfferingId().equals(co2.getUniqueId()))
	                        co3.setIsControl(new Boolean(true));
	                    else
	                        co3.setIsControl(new Boolean(false));

	                    // Copy academic reservations from course offering
	                    HashSet resvs = new HashSet(); 
	                    Set resv2 = co2.getAcadAreaReservations();
	                    if (resv2==null) {
	                        resvs.add(null);
	                    }
	                    else {
		                    for (Iterator resvIter=resv2.iterator(); resvIter.hasNext();) {
		                        AcadAreaReservation ar2 = (AcadAreaReservation) resvIter.next();

		                        AcadAreaReservation ar3 = new AcadAreaReservation();
		                        ar3.setAcademicArea(ar2.getAcademicArea());
		                        ar3.setAcademicClassification(ar2.getAcademicClassification());
		                        ar3.setOwnerClassId(ar2.getOwnerClassId());
		                        ar3.setPriorEnrollment(ar2.getPriorEnrollment());
		                        ar3.setPriority(ar2.getPriority());
		                        ar3.setProjectedEnrollment(ar2.getProjectedEnrollment());
		                        ar3.setRequested(ar2.getRequested());
		                        ar3.setReservationType(ar2.getReservationType());
		                        ar3.setReserved(ar2.getReserved());
		                        resvs.add(ar3);

		                        // Delete academic area reservations
		                        Debug.debug("Removing academic area reservation from course offering");
		                        hibSession.delete(ar2);
		                        resvIter.remove();
		                    }
	                    }
	                    co2.setAcadAreaReservations(null);
	                    
	                    addedOfferings.addElement(co3);
	                    addedResvs.addElement(resvs);

		                // Copy course reservations from instructional offering
	                    CourseOfferingReservation cor2 = getCourseReservation(io1, co2);
    	                int indx = frm.getIndex(course);

    	                CourseOfferingReservation cor3 = new CourseOfferingReservation();
                        //cor3.setCourseOffering(co3);
                        cor3.setOwner(io.getUniqueId());
		                cor3.setReserved(Integer.valueOf(frm.getLimits(indx)));
    	                
	                    if (cor2!=null) {
	                        cor3.setOwnerClassId(cor2.getOwnerClassId());
	                        cor3.setPriorEnrollment(cor2.getPriorEnrollment());
	                        cor3.setPriority(cor2.getPriority());
	                        cor3.setProjectedEnrollment(cor2.getProjectedEnrollment());
	                        cor3.setRequested(cor2.getRequested());
	                        cor3.setReservationType(cor2.getReservationType());
	                    } else {
		                    cor3.setOwnerClassId(Constants.RESV_OWNER_IO);
		                    cor3.setPriorEnrollment(null);
		                    cor3.setPriority(new Integer(1));
		                    cor3.setProjectedEnrollment(null);
		                    cor3.setRequested(null);
		                    cor3.setReservationType(permResvType);
	                    }
                        addedCourseResvs.addElement(cor3);
		                
	                    // Remove from collection
	                    //i.remove();

                        sa2.getCourseOfferings().remove(co2);
	                    hibSession.saveOrUpdate(sa2);
		                saList.put(sa2.getSubjectAreaAbbreviation(), sa2);
                        
	                    // Delete course offering
                        io1.removeCourseOffering(co2);
                        Event.deleteFromEvents(hibSession, co2);
                        Exam.deleteFromExams(hibSession, co2);
                    	String className = ApplicationProperties.getProperty("tmtbl.external.course_offering.remove_action.class");
                    	if (className != null && className.trim().length() > 0){
                    		ExternalCourseOfferingRemoveAction removeAction = (ExternalCourseOfferingRemoveAction) (Class.forName(className).newInstance());
            	       		removeAction.performExternalCourseOfferingRemoveAction(co2, hibSession);
                    	}

	                    hibSession.delete(co2);
	                    hibSession.flush();

	                    //hibSession.refresh(sa2);
	                    
	                }
	                
	                //io1.setCourseOfferings(offerings);
	                //hibSession.saveOrUpdate(io1);
	                Event.deleteFromEvents(hibSession, io1);
	                Exam.deleteFromExams(hibSession, io1);

	                hibSession.delete(io1);
	                hibSession.flush();
	                
	                hibSession.saveOrUpdate(sa);
	                saList.put(sa.getSubjectAreaAbbreviation(), sa);

	    	        //hibSession.refresh(sa);
	                
	            }            
	        }
	
	        hibSession.flush();
	        
	        // Update Offering - Added Offerings       
	        for (int i=0; i<addedOfferings.size(); i++) {
	            CourseOffering co3 = (CourseOffering) addedOfferings.elementAt(i);
	            co3.setInstructionalOffering(io);
	            io.addTocourseOfferings(co3);
	            hibSession.saveOrUpdate(co3);
	            
	            hibSession.flush();
	            hibSession.refresh(co3);
            
	            // Add academic area reservations
	            HashSet resvs = (HashSet) addedResvs.get(i);
	            if (resvs!=null) {
                    for (Iterator resvIter=resvs.iterator(); resvIter.hasNext();) {
                        AcadAreaReservation ar = (AcadAreaReservation) resvIter.next();
                        ar.setOwner(co3.getUniqueId());
                        hibSession.saveOrUpdate(ar);
                    }
                    co3.getAcadAreaReservations().addAll(resvs);
                    hibSession.saveOrUpdate(co3);
	            }
	            
	            // Update course reservations
	            CourseOfferingReservation cor = (CourseOfferingReservation) addedCourseResvs.get(i);
	            cor.setCourseOffering(co3);
	            hibSession.saveOrUpdate(cor);
	            
	            io.getCourseOfferings().add(cor);
	            hibSession.saveOrUpdate(io);
	        }
            
	        // Update managing department on all classes
	        Department dept = io.getControllingCourseOffering().getDepartment();
	        Set cfgs = io.getInstrOfferingConfigs();
	        for (Iterator iterCfg=cfgs.iterator(); iterCfg.hasNext(); ) {
	        	InstrOfferingConfig cfg = (InstrOfferingConfig) iterCfg.next();
	        	Set subparts = cfg.getSchedulingSubparts();
		        for (Iterator iterSbp=subparts.iterator(); iterSbp.hasNext(); ) {
		        	SchedulingSubpart subpart = (SchedulingSubpart) iterSbp.next();
		        	Set classes = subpart.getClasses();
			        for (Iterator iterCls=classes.iterator(); iterCls.hasNext(); ) {
			        	Class_ cls = (Class_) iterCls.next();
			        	// Only change departmental class managing dept and not externally managed
			        	if (!cls.getManagingDept().isExternalManager()) {
				        	cls.setManagingDept(dept);
				        	hibSession.saveOrUpdate(cls);
			        	}
			        }
		        }	        		        	
	        }
	        
	        
            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    io, 
                    ChangeLog.Source.CROSS_LIST, 
                    ChangeLog.Operation.UPDATE, 
                    io.getControllingCourseOffering().getSubjectArea(),
                    null);
	        
            tx.commit();
	        hibSession.flush();
	        hibSession.clear();
	        hibSession.refresh(io);
	        
	        // Refresh objects
	        for (Iterator i1=io.getInstrOfferingConfigs().iterator();i1.hasNext();) {
	        	InstrOfferingConfig cfg = (InstrOfferingConfig)i1.next();
	        	for (Iterator i2=cfg.getSchedulingSubparts().iterator();i2.hasNext();) {
	        		SchedulingSubpart ss = (SchedulingSubpart)i2.next();
	        		for (Iterator i3=ss.getClasses().iterator();i3.hasNext();) {
	        			Class_ c = (Class_)i3.next();
	        			hibSession.refresh(c);
	        		}
	        		hibSession.refresh(ss);
	        	}
	        }
	        
	        Set keys = saList.keySet();
	        for (Iterator i1=keys.iterator(); i1.hasNext();) {
	        	hibSession.refresh(saList.get(i1.next()));
	        }	
        	String className = ApplicationProperties.getProperty("tmtbl.external.instr_offr.crosslist_action.class");
        	if (className != null && className.trim().length() > 0){
	        	ExternalCourseCrosslistAction addAction = (ExternalCourseCrosslistAction) (Class.forName(className).newInstance());
	       		addAction.performExternalCourseCrosslistAction(io, hibSession);
        	}

        }
        catch (Exception e) {
            Debug.error(e);
            try {
	            if(tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }
            throw e;
        }
    }

    /**
     * Loads the form with the offering detail
     * @param frm Form object
     * @param courseOfferingId Course Offering Id of controlling course
     * @param user User object
     */
    private void doLoad(
            CrossListsModifyForm frm, 
            String courseOfferingId,
            User user ) throws Exception {
        
        // Check uniqueid
        if(courseOfferingId==null || courseOfferingId.trim().length()==0)
            throw new Exception ("Unique Id need for operation. ");

        // Load details
        CourseOfferingDAO coDao = new CourseOfferingDAO();
        CourseOffering co = coDao.get(Long.valueOf(courseOfferingId));
        InstructionalOffering io = co.getInstructionalOffering();

        // Sort Offerings
        ArrayList offerings = new ArrayList(io.getCourseOfferings());
        Collections.sort(
                offerings, 
                new CourseOfferingComparator(CourseOfferingComparator.COMPARE_BY_CTRL_CRS));
        
        // Load form properties
        frm.setInstrOfferingId(io.getUniqueId());
        frm.setCtrlCrsOfferingId(io.getControllingCourseOffering().getUniqueId());
        frm.setReadOnlyCrsOfferingId(null);
        frm.setSubjectAreaId(co.getSubjectArea().getUniqueId());
        frm.setInstrOfferingName(io.getCourseNameWithTitle());
        frm.setOwnedInstrOffr(new Boolean(io.isEditableBy(user)));
        frm.setIoLimit(io.getLimit());
        frm.setUnlimited(io.hasUnlimitedEnrollment());

        for(Iterator i = offerings.iterator(); i.hasNext(); ) {
            CourseOffering co1 = ((CourseOffering) i.next());
            frm.addToCourseOfferings(co1, getCourseReservation(io, co1), new Boolean(co1.isEditableBy(user)));
            frm.addToOriginalCourseOfferings(co1);
        }        
    }
    
    /**
     * Get reservation for the specified course offering
     * @param io
     * @param co
     * @return null if not found
     */
    private CourseOfferingReservation getCourseReservation(InstructionalOffering io, CourseOffering co) {
        
        // Check reservations for limits
        Set crsResv = io.getCourseReservations();
        if (crsResv!=null && crsResv.size()>0) {
            for (Iterator j=crsResv.iterator(); j.hasNext(); ) {
                CourseOfferingReservation cor = (CourseOfferingReservation) j.next();
                if (cor.getCourseOffering().equals(co)) {
                    return cor;
                }
            }
        }
        
        return null;
    }

}
