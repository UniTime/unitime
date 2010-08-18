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
import org.unitime.timetable.form.StudentGroupReservationEditForm;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.dao.ReservationTypeDAO;
import org.unitime.timetable.model.dao.StudentGroupDAO;
import org.unitime.timetable.model.dao.StudentGroupReservationDAO;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/** 
 * MyEclipse Struts
 * Creation date: 09-01-2006
 * 
 * XDoclet definition:
 * @struts:action path="/studentGroupReservationEdit" name="studentGroupReservationEditForm" input="/user/studentGroupReservationEdit.jsp" scope="request"
 */
public class StudentGroupReservationEditAction extends ReservationAction {

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
            StudentGroupReservationEditForm frm = (StudentGroupReservationEditForm) form;
    	    ActionMessages errors = null;
            String op = frm.getOp();
            
    	    // Set up lists
            LookupTables.setupStudentGroups(request);

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
            
            return mapping.findForward("displayStuGrpReservationDetail");
        }
        
    
    /**
     * Update reservations
     * @param request
     * @param frm
     * @throws Exception
     */
    private void doUpdate(
            HttpServletRequest request, 
            StudentGroupReservationEditForm frm) throws Exception {
        
        // Get input data
        Long owner = frm.getOwnerId();
        String ownerType = frm.getOwnerType();
        List resvIds = frm.getReservationId();
        List resvTypes = frm.getReservationType();
        List resvPriorities = frm.getPriority();
        List reservedSpaces = frm.getReserved();
        List priorEnrollments = frm.getPriorEnrollment();
        List projEnrollments = frm.getProjectedEnrollment();
        List stuGroups = frm.getStudentGroupId();
        Session hibSession = null;
        Transaction tx = null;

        try {
            StudentGroupReservationDAO sdao = new StudentGroupReservationDAO();
            StudentGroupDAO sgdao = new StudentGroupDAO();
            ReservationTypeDAO rdao = new ReservationTypeDAO();
            Object ownerObj = super.getOwner(frm);
            
            hibSession = sdao.getSession();
            tx = hibSession.beginTransaction();

            // Delete reservations not in resvId list
            Collection stuGrpResv = super.getReservations(frm, StudentGroupReservation.class);
            if (stuGrpResv!=null && stuGrpResv.size()>0) {
                for (Iterator iter=stuGrpResv.iterator(); iter.hasNext();) {
                    StudentGroupReservation resv = (StudentGroupReservation) iter.next();
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
                String stuGrp = (String) stuGroups.get(i);
                StudentGroupReservation r = null;
                
                // New Reservation
                if (resvId==null || resvId.trim().length()==0) {
                    r = new StudentGroupReservation();
                }
                
                // Load existing reservation
                else {
                    r = sdao.get(new Long(resvId.trim()));
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

                r.setStudentGroup(sgdao.get(new Long(stuGrp)));
                
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
            StudentGroupReservationEditForm frm) throws Exception {
        
        Collection stuGroupResv = super.getReservations(frm, StudentGroupReservation.class);
        if (stuGroupResv!=null && stuGroupResv.size()>0) {
            for (Iterator iter=stuGroupResv.iterator(); iter.hasNext();) {
                StudentGroupReservation resv = (StudentGroupReservation) iter.next();
                frm.addReservation(resv);
            }
        }
        
        // Add blank rows
        if (frm.getAddBlankRow().booleanValue())
            frm.addBlankRows();
    }
}
