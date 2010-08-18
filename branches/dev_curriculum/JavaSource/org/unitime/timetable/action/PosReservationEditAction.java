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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.timetable.form.PosReservationEditForm;
import org.unitime.timetable.model.PosReservation;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.PosMajorDAO;
import org.unitime.timetable.model.dao.PosReservationDAO;
import org.unitime.timetable.model.dao.ReservationTypeDAO;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/** 
 * @author Tomas Muller
 */
public class PosReservationEditAction extends ReservationAction {

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
        PosReservationEditForm frm = (PosReservationEditForm) form;
	    ActionMessages errors = null;
        String op = frm.getOp();
        
	    // Set up lists
        LookupTables.setupPosMajors(request);
        LookupTables.setupAcademicClassifications(request);

        // New reservation
        if ( op.equals(rsc.getMessage("button.reservationNextStep")) ) {
            doLoad(request, frm);
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
        
        return mapping.findForward("displayPosReservationDetail");
    }
    
    
    /**
     * Update reservations
     * @param request
     * @param frm
     * @throws Exception
     */
    private void doUpdate(
            HttpServletRequest request, 
            PosReservationEditForm frm) throws Exception {
        
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
        List posMajors = frm.getPosMajorId();
        Session hibSession = null;
        Transaction tx = null;

        try {
            PosReservationDAO pdao = new PosReservationDAO();
            PosMajorDAO pmdao = new PosMajorDAO();
            AcademicClassificationDAO acdao = new AcademicClassificationDAO();
            ReservationTypeDAO rdao = new ReservationTypeDAO();
            Object ownerObj = super.getOwner(frm);
            
            hibSession = pdao.getSession();
            tx = hibSession.beginTransaction();

            // Delete reservations not in resvId list
            Collection posResv = super.getReservations(frm, PosReservation.class);
            if (posResv!=null && posResv.size()>0) {
                for (Iterator iter=posResv.iterator(); iter.hasNext();) {
                    PosReservation resv = (PosReservation) iter.next();
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
                String posMajor = (String) posMajors.get(i);
                PosReservation r = null;
                
                // New Reservation
                if (resvId==null || resvId.trim().length()==0) {
                    r = new PosReservation();
                }
                
                // Load existing reservation
                else {
                    r = pdao.get(new Long(resvId.trim()));
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

                r.setPosMajor(pmdao.get(new Long(posMajor)));
                
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
    public void doLoad(
            HttpServletRequest request,
            PosReservationEditForm frm) throws Exception {
        
        Collection posResv = super.getReservations(frm, PosReservation.class);
        if (posResv!=null && posResv.size()>0) {
            for (Iterator iter=posResv.iterator(); iter.hasNext();) {
                PosReservation resv = (PosReservation) iter.next();
                frm.addReservation(resv);
            }
        }
        
        // Add blank rows
        if (frm.getAddBlankRow().booleanValue())
            frm.addBlankRows();
    }
}
