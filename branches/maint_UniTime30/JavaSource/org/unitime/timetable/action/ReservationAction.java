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

import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.ReservationForm;
import org.unitime.timetable.model.AcadAreaReservation;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PosReservation;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/**
 * Base Reservations action class
 * @author Heston Fernandes
 */
public class ReservationAction extends Action {
    
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
        
	    HttpSession httpSession = request.getSession();
        if(!Web.isLoggedIn( httpSession )) {
            throw new Exception ("Access Denied.");
        }
        
        ReservationForm frm = (ReservationForm) form;

        // Get operation
        String op = (request.getParameter("op")==null) 
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");		        
        			        
        if(op==null || op.trim().length()==0)
            throw new Exception ("Invalid operation");

        frm.setOp(op);

        // Set labels
        if (frm.getOwnerName()==null)
            frm.setOwnerName((String) request.getAttribute("ownerName"));
        
        if (frm.getOwnerTypeLabel()==null)
            frm.setOwnerTypeLabel((String) request.getAttribute("ownerTypeLabel"));

        if (frm.getIoLimit()==null)
            frm.setIoLimit((String) request.getAttribute("ioLimit"));        

        if (frm.getCrsLimit()==null)
            frm.setCrsLimit((String) request.getAttribute("crsLimit"));        

        if (frm.getUnlimited()==null)
            frm.setUnlimited((Boolean) request.getAttribute("unlimited"));        

        // Set up lists
        LookupTables.setupReservationPriorities(request);
        LookupTables.setupReservationTypes(request);
        
        return mapping.findForward("");
    }
    
    /**
     * Get the list element number of the row to be deleted
     * @param request
     * @return -1 if the id could not be determined, row number otherwise
     */
    public int getDeletedRowId(HttpServletRequest request) {
        int deleteId = -1;
        
        try {
            deleteId = Integer.parseInt(request.getParameter("deleteId"));
        }
        catch(Exception e) {
            deleteId = -1;
        }
        
        return deleteId;
    }
    
    /**
     * Get owner object
     * @param frm
     * @return Owner Object, null if owner object not found
     */
    public Object getOwner(ReservationForm frm) {
        
        String ownerType = frm.getOwnerType();
        Long ownerId = frm.getOwnerId();
        Object owner = null;
        
        if (ownerType.equals(Constants.RESV_OWNER_IO)) {
            owner = new InstructionalOfferingDAO().get(ownerId);
        }
        
        if (ownerType.equals(Constants.RESV_OWNER_CONFIG)) {
            owner = new InstrOfferingConfigDAO().get(new Long(ownerId.intValue()));
        }
        
        if (ownerType.equals(Constants.RESV_OWNER_CLASS)) {
            owner = new Class_DAO().get(ownerId);
        }
        
        //TODO Reservations - functionality to be removed later
        if (ownerType.equals(Constants.RESV_OWNER_COURSE)) {
            owner = new CourseOfferingDAO().get(ownerId);
        }
        //End Bypass
        
        return owner;
    }
      
    /**
     * Get the reservations for the specified reservation class
     * @param frm
     * @param c Of the form - xxxReservation.class
     * @return Reservations of the class specified, null if no reservations found
     * @throws Exception
     */
    public Collection getReservations(ReservationForm frm, Class c) throws Exception {
        String ownerType = frm.getOwnerType();
        Long ownerId = frm.getOwnerId();
        Set resv = null;
        
        // Instructional Offering Reservations
        if (ownerType.equals(Constants.RESV_OWNER_IO)) {
            InstructionalOffering io = new InstructionalOfferingDAO().get(ownerId);
            if (c.getName().equals(IndividualReservation.class.getName()))
                resv = io.getIndividualReservations();
            if (c.getName().equals(CourseOfferingReservation.class.getName()))
                resv = io.getCourseReservations();
            if (c.getName().equals(StudentGroupReservation.class.getName()))
                resv = io.getStudentGroupReservations();
            if (c.getName().equals(AcadAreaReservation.class.getName()))
                resv = io.getAcadAreaReservations();
            if (c.getName().equals(PosReservation.class.getName()))
                resv = io.getPosReservations();
        }
        
        // Configuration Reservations
        if (ownerType.equals(Constants.RESV_OWNER_CONFIG)) {
            InstrOfferingConfig config = new InstrOfferingConfigDAO().get(new Long(ownerId.intValue()));
            if (c.getName().equals(IndividualReservation.class.getName()))
                resv = config.getIndividualReservations();
            if (c.getName().equals(CourseOfferingReservation.class.getName()))
                resv = config.getCourseReservations();
            if (c.getName().equals(StudentGroupReservation.class.getName()))
                resv = config.getStudentGroupReservations();
            if (c.getName().equals(AcadAreaReservation.class.getName()))
                resv = config.getAcadAreaReservations();
            if (c.getName().equals(PosReservation.class.getName()))
                resv = config.getPosReservations();
        }
        
        // Class Reservations
        if (ownerType.equals(Constants.RESV_OWNER_CLASS)) {
            Class_ cls = new Class_DAO().get(ownerId);
            if (c.getName().equals(IndividualReservation.class.getName()))
                resv = cls.getIndividualReservations();
            if (c.getName().equals(CourseOfferingReservation.class.getName()))
                resv = cls.getCourseReservations();
            if (c.getName().equals(StudentGroupReservation.class.getName()))
                resv = cls.getStudentGroupReservations();
            if (c.getName().equals(AcadAreaReservation.class.getName()))
                resv = cls.getAcadAreaReservations();
            if (c.getName().equals(PosReservation.class.getName()))
                resv = cls.getPosReservations();
        }
        
        //TODO Reservations - functionality to be removed later
        // Course Reservations
        if (ownerType.equals(Constants.RESV_OWNER_COURSE)) {
            CourseOffering co = new CourseOfferingDAO().get(ownerId);
            if (c.getName().equals(AcadAreaReservation.class.getName()))
                resv = co.getAcadAreaReservations();
        }
        // End Bypass
        
        return resv;
    }
    
    public void addChange(org.hibernate.Session hibSession, HttpServletRequest request, Object ownerObj) {
        SubjectArea subjArea = null;
        Department dept = null;
        if (ownerObj instanceof Class_) {
            subjArea = ((Class_)ownerObj).getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering().getSubjectArea();
            dept = ((Class_)ownerObj).getManagingDept();
        } else if (ownerObj instanceof CourseOffering) {
            subjArea = ((CourseOffering)ownerObj).getSubjectArea();
            dept = subjArea.getDepartment();
        } else if (ownerObj instanceof InstrOfferingConfig) {
            subjArea = ((InstrOfferingConfig)ownerObj).getControllingCourseOffering().getSubjectArea();
            dept = subjArea.getDepartment();
        } else if (ownerObj instanceof InstructionalOffering) {
            subjArea = ((InstructionalOffering)ownerObj).getControllingCourseOffering().getSubjectArea();
            dept = subjArea.getDepartment();
        }
        
        ChangeLog.addChange(
                hibSession, 
                request, 
                ownerObj, 
                ChangeLog.Source.RESERVATION, 
                ChangeLog.Operation.UPDATE, 
                subjArea, 
                dept);
    }
    
}
