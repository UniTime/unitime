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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.CourseReservationEditForm;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingReservationEditAction;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ReservationType;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.comparators.CourseReservationComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CourseOfferingReservationDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;


/** 
 * MyEclipse Struts
 * Creation date: 09-01-2006
 * 
 * XDoclet definition:
 * @struts:action path="/courseReservationEdit" name="courseReservationEditForm" input="/user/courseReservationEdit.jsp" scope="request"
 */
public class CourseReservationEditAction extends ReservationAction {

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
        
        super.execute(mapping, form, request, response);
        
        MessageResources rsc = getResources(request);
        User user = Web.getUser(request.getSession());        
        CourseReservationEditForm frm = (CourseReservationEditForm) form;
	    ActionMessages errors = null;
        String op = frm.getOp();
        
        //TODO Reservations - functionality to be removed later
        if (frm.getOwnerId()==null)
            frm.setOwnerId((Long) request.getAttribute("ownerId"));
        
        if (frm.getOwnerType()==null)
            frm.setOwnerType((String) request.getAttribute("ownerType"));        
        //End Bypass
        
        // Set up lists
        setupCourseOfferings(request, frm);
        
        // New reservation
        if ( op.equals(rsc.getMessage("button.reservationNextStep")) 
                || op.equals(rsc.getMessage("button.addReservation")) 
                || op.equals(rsc.getMessage("button.addReservationIo")) ) {
            doLoad(request, frm);
        }        
        
        // Add Blank Rows
        if ( op.equals(rsc.getMessage("button.addReservationRow")) ) {
        	Collection coList = (Collection)request.getAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME);
        	if (coList!=null && coList.size()>0) {
        		if (frm.getCourseOfferingId().size()<coList.size())
        			frm.addBlankRows();
        		else { 
        			if (errors==null) errors = new ActionMessages();
        			errors.add("courseOfferingId", 
                            new ActionMessage(
                                    "errors.generic", 
                                    "Cannot add reservations that exceed the number of courses in the offering."));
        			saveErrors(request, errors);
        		}
        	}
        	else {
        		frm.addBlankRows();
        	}
        }        
        
        // Delete rows
        if ( op.equals(rsc.getMessage("button.removeReservation")) ) {
            frm.removeRow(super.getDeletedRowId(request));
        }        
        
        // Update
        if ( op.equals(rsc.getMessage("button.updateReservation")) ) {
            errors = frm.validate(mapping, request);
            if (errors.size()>0) {
                saveErrors(request, errors);
            }
            else {
                doUpdate(request, frm);
	            BackTracker.doBack(request, response);
	            return null;
            }
        }        
        
        return mapping.findForward("displayCourseReservationDetail");
    }
    
    
    /**
     * Setup course offerings 
     * @param request
     * @param frm
     */
    private void setupCourseOfferings(
            HttpServletRequest request, 
            CourseReservationEditForm frm) {

        String ownerType = frm.getOwnerType();
        Long ownerId = frm.getOwnerId();
        InstructionalOffering io = null;
        
        if (ownerType.equals(Constants.RESV_OWNER_IO)) {
            io = new InstructionalOfferingDAO().get(ownerId);
        }
        
        if (ownerType.equals(Constants.RESV_OWNER_CONFIG)) {
            InstrOfferingConfig config = new InstrOfferingConfigDAO().get(new Long(ownerId.intValue()));
            io = config.getInstructionalOffering();
        }
        
        if (ownerType.equals(Constants.RESV_OWNER_CLASS)) {
            Class_ cls = new Class_DAO().get(ownerId);
            io = cls.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering();
        }

        //TODO Reservations - functionality to be removed later
        if (ownerType.equals(Constants.RESV_OWNER_COURSE)) {
            CourseOffering crs = new CourseOfferingDAO().get(ownerId);
            io = crs.getInstructionalOffering();
        }
        //End Bypass
        
        Vector coList = new Vector(io.getCourseOfferings());
	    Collections.sort(coList, new CourseOfferingComparator());
		request.setAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME, coList);
    }


    /**
     * Update reservations
     * @param request
     * @param frm
     * @throws Exception
     */
    private void doUpdate(
            HttpServletRequest request, 
            CourseReservationEditForm frm) throws Exception {
        
        //Get Reservation Type
        ReservationType rt = ReservationType.getReservationTypebyRef(Constants.RESV_TYPE_PERM_REF);
        if (rt==null)
            throw new Exception ("Reservation type for ref " + Constants.RESV_TYPE_PERM_REF + " could not be loaded");

        // Get input data
        Long owner = frm.getOwnerId();
        String ownerType = frm.getOwnerType();
        List resvIds = frm.getReservationId();
        List resvTypes = frm.getReservationType();
        List resvPriorities = frm.getPriority();
        List reservedSpaces = frm.getReserved();
        List priorEnrollments = frm.getPriorEnrollment();
        List projEnrollments = frm.getProjectedEnrollment();
        List courseOfferings = frm.getCourseOfferingId();
        Session hibSession = null;
        Transaction tx = null;

        try {
            CourseOfferingReservationDAO cordao = new CourseOfferingReservationDAO();
            CourseOfferingDAO codao = new CourseOfferingDAO();
            Object ownerObj = super.getOwner(frm);
            
            hibSession = cordao.getSession();
            tx = hibSession.beginTransaction();

            // Delete reservations not in resvId list
            Collection coResv = super.getReservations(frm, CourseOfferingReservation.class);
            if (coResv!=null && coResv.size()>0) {
                for (Iterator iter=coResv.iterator(); iter.hasNext();) {
                    CourseOfferingReservation resv = (CourseOfferingReservation) iter.next();
                    boolean found = false;
                    
                    for (int i=0; i<resvIds.size(); i++) {                        
                        String resvId = (String) resvIds.get(i);
                        if (resvId!=null && resvId.equals(resv.getUniqueId().toString())) {
                            found = true;
                            break;
                        }                            
                    }    
                    
                    if (!found) {
                        hibSession.delete(resv);
                        iter.remove();
                        hibSession.saveOrUpdate(ownerObj);
                    }
                }
            }

            // Add / Update
            for (int i=0; i<resvIds.size(); i++) {
                
                String resvId = (String) resvIds.get(i);
                String resvType = (String) resvTypes.get(i);
                String resvPriority = (String) resvPriorities.get(i);
                String reservedSpace = (String) reservedSpaces.get(i);
                String priorEnrollment = (String) priorEnrollments.get(i);
                String projEnrollment = (String) projEnrollments.get(i);
                String co = (String) courseOfferings.get(i);
                CourseOfferingReservation r = null;
                
                // New Reservation
                if (resvId==null || resvId.trim().length()==0) {
                    r = new CourseOfferingReservation();
                }
                
                // Load existing reservation
                else {
                    r = cordao.get(new Long(resvId.trim()));
                }
                
                r.setOwner(owner);
                r.setOwnerClassId(ownerType);
                r.setPriority(new Integer(resvPriority));
                r.setReserved(new Integer(reservedSpace));
                //r.setReservationType(rdao.get(new Integer(resvType)));
                r.setReservationType(rt);

                if (priorEnrollment==null || priorEnrollment.trim().length()==0)
                    r.setPriorEnrollment(null);
                else
                    r.setPriorEnrollment(new Integer(priorEnrollment));
                
                if (projEnrollment==null || projEnrollment.trim().length()==0)
                    r.setProjectedEnrollment(null);
                else
                    r.setProjectedEnrollment(new Integer(projEnrollment));

                r.setCourseOffering(codao.get(new Long(co)));
                
                hibSession.saveOrUpdate(r);
            }
            
            addChange(hibSession, request, ownerObj);
            
            tx.commit();
            hibSession.clear();
            hibSession.refresh(ownerObj);
            
            
            String className = ApplicationProperties.getProperty("tmtbl.external.reservation.edit_action.class");
        	if (className != null && className.trim().length() > 0){
            	ExternalCourseOfferingReservationEditAction editAction = (ExternalCourseOfferingReservationEditAction) (Class.forName(className).newInstance());
           		editAction.performExternalCourseOfferingReservationEditAction(ownerObj, hibSession);
        	}

        }
        catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw(e);
        }
    }


    /**
     * Load existing reservations data for the owner 
     * @param request
     * @param frm
     * @throws Exception
     */
    public void doLoad(
            HttpServletRequest request,
            CourseReservationEditForm frm) throws Exception {

        Vector courseResv = new Vector (super.getReservations(frm, CourseOfferingReservation.class));
        if (courseResv!=null && courseResv.size()>0) {
            Collections.sort(courseResv, new CourseReservationComparator());
            for (Iterator iter=courseResv.iterator(); iter.hasNext();) {
                CourseOfferingReservation resv = (CourseOfferingReservation) iter.next();
                frm.addReservation(resv);
            }
        }
        
        // Add blank rows
    	Collection coList = (Collection)request.getAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME);
        if (frm.getAddBlankRow().booleanValue() && courseResv.size()<coList.size())
            frm.addBlankRows();
    }
}
