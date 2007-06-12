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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.AcademicAreaReservationEditForm;
import org.unitime.timetable.model.AcadAreaReservation;
import org.unitime.timetable.model.comparators.AcadAreaReservationComparator;
import org.unitime.timetable.model.dao.AcadAreaReservationDAO;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.ReservationTypeDAO;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/** 
 * MyEclipse Struts
 * Creation date: 09-01-2006
 * 
 * XDoclet definition:
 * @struts:action path="/academicAreaReservationEdit" name="academicAreaReservationEditForm" input="/user/academicAreaReservationEdit.jsp" scope="request"
 */
public class AcademicAreaReservationEditAction extends ReservationAction {

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
        AcademicAreaReservationEditForm frm = (AcademicAreaReservationEditForm) form;
	    ActionMessages errors = null;
        String op = frm.getOp();
        
        //TODO Reservations - functionality to be removed later
        if (frm.getOwnerId()==null)
            frm.setOwnerId((Long) request.getAttribute("ownerId"));
        
        if (frm.getOwnerType()==null)
            frm.setOwnerType((String) request.getAttribute("ownerType"));        
        //End Bypass
        
        // Set up lists
        LookupTables.setupAcademicAreas(request);
        LookupTables.setupAcademicClassifications(request);

        // New reservation
        //if ( op.equals(rsc.getMessage("button.reservationNextStep")) ) {
        //TODO Reservations - functionality to be removed later
        if ( op.equals(rsc.getMessage("button.reservationNextStep")) 
                || op.equals(rsc.getMessage("button.addReservation")) ) {
            doLoad(frm);
        }        
        
        // Add Blank Rows
        if ( op.equals(rsc.getMessage("button.addReservationRow")) ) {
            frm.addBlankRows();        
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
        
        return mapping.findForward("displayAcadAreaReservationDetail");
    }

    
    /**
     * Update reservations
     * @param request
     * @param frm
     * @throws Exception
     */
    private void doUpdate(
            HttpServletRequest request, 
            AcademicAreaReservationEditForm frm) throws Exception {
        
        // Get input data
        Long owner = frm.getOwnerId();
        String ownerType = frm.getOwnerType();
        List resvIds = frm.getReservationId();
        List resvTypes = frm.getReservationType();
        List resvPriorities = frm.getPriority();
        List reservedSpaces = frm.getReserved();
        List priorEnrollments = frm.getPriorEnrollment();
        List projEnrollments = frm.getProjectedEnrollment();
        List acadClassifs = frm.getAcademicClassificationId();
        List acadAreas = frm.getAcademicAreaId();
        Session hibSession = null;
        Transaction tx = null;

        try {
            AcadAreaReservationDAO adao = new AcadAreaReservationDAO();
            AcademicAreaDAO aadao = new AcademicAreaDAO();
            AcademicClassificationDAO acdao = new AcademicClassificationDAO();
            ReservationTypeDAO rdao = new ReservationTypeDAO();
            Object ownerObj = super.getOwner(frm);
            
            hibSession = adao.getSession();
            tx = hibSession.beginTransaction();

            // Delete reservations not in resvId list
            Collection acadAreaResv = super.getReservations(frm, AcadAreaReservation.class);
            if (acadAreaResv!=null && acadAreaResv.size()>0) {
                for (Iterator iter=acadAreaResv.iterator(); iter.hasNext();) {
                    AcadAreaReservation resv = (AcadAreaReservation) iter.next();
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
                String acadClassif = (String) acadClassifs.get(i);
                String acadArea = (String) acadAreas.get(i);
                AcadAreaReservation r = null;
                
                // New Reservation
                if (resvId==null || resvId.trim().length()==0) {
                    r = new AcadAreaReservation();
                }
                
                // Load existing reservation
                else {
                    r = adao.get(new Long(resvId.trim()));
                }
                
                r.setOwner(owner);
                r.setOwnerClassId(ownerType);
                r.setPriority(new Integer(resvPriority));
                r.setReserved(new Integer(reservedSpace));
                r.setReservationType(rdao.get(new Long(resvType)));

                if (priorEnrollment==null || priorEnrollment.trim().length()==0)
                    r.setPriorEnrollment(null);
                else
                    r.setPriorEnrollment(new Integer(priorEnrollment));
                
                if (projEnrollment==null || projEnrollment.trim().length()==0)
                    r.setProjectedEnrollment(null);
                else
                    r.setProjectedEnrollment(new Integer(projEnrollment));

                r.setAcademicArea(aadao.get(new Long(acadArea)));
                
                if (acadClassif==null || acadClassif.trim().length()==0)
                    r.setAcademicClassification(null);
                else
                    r.setAcademicClassification(acdao.get(new Long(acadClassif)));
                
                hibSession.saveOrUpdate(r);
            }
            
            addChange(hibSession, request, ownerObj);
            
            tx.commit();
            hibSession.clear();
            hibSession.refresh(ownerObj);
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
    public void doLoad(AcademicAreaReservationEditForm frm) throws Exception {
        
        Vector acadAreaResv = new Vector(super.getReservations(frm, AcadAreaReservation.class));
        if (acadAreaResv!=null && acadAreaResv.size()>0) {
            Collections.sort(acadAreaResv, new AcadAreaReservationComparator());
            for (Iterator iter=acadAreaResv.iterator(); iter.hasNext();) {
                AcadAreaReservation resv = (AcadAreaReservation) iter.next();
                frm.addReservation(resv);
            }
        }
        else {
	        // Add blank rows
	        if (frm.getAddBlankRow().booleanValue())
	            frm.addBlankRows();
        }
    }

}