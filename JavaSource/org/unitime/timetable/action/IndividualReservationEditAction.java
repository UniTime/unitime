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

import java.text.SimpleDateFormat;
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
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.IndividualReservationEditForm;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.dao.IndividualReservationDAO;
import org.unitime.timetable.model.dao.ReservationTypeDAO;
import org.unitime.timetable.webutil.BackTracker;


/** 
 * MyEclipse Struts
 * Creation date: 09-01-2006
 * 
 * XDoclet definition:
 * @struts:action path="/individualReservationEdit" name="individualReservationEditForm" input="/user/individualReservationEdit.jsp" scope="request"
 */
public class IndividualReservationEditAction extends ReservationAction {

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
        IndividualReservationEditForm frm = (IndividualReservationEditForm) form;
	    ActionMessages errors = null;
        String op = frm.getOp();
        
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
        
        return mapping.findForward("displayIndividualReservationDetail");
    }
    
    
    /**
     * Update reservations
     * @param request
     * @param frm
     * @throws Exception
     */
    private void doUpdate(
            HttpServletRequest request, 
            IndividualReservationEditForm frm) throws Exception {
        
        // Get input data
        Long owner = frm.getOwnerId();
        String ownerType = frm.getOwnerType();
        List resvIds = frm.getReservationId();
        List resvTypes = frm.getReservationType();
        List resvPriorities = frm.getPriority();
        List puids = frm.getPuid();
        List overLimits = frm.getOverLimit();
        List expDates = frm.getExpirationDate();
        Session hibSession = null;
        Transaction tx = null;

        try {
            IndividualReservationDAO idao = new IndividualReservationDAO();
            ReservationTypeDAO rdao = new ReservationTypeDAO();
            Object ownerObj = super.getOwner(frm);
            
            hibSession = idao.getSession();
            tx = hibSession.beginTransaction();

            // Delete reservations not in resvId list
            Collection individualResv = super.getReservations(frm, IndividualReservation.class);
            if (individualResv!=null && individualResv.size()>0) {
                for (Iterator iter=individualResv.iterator(); iter.hasNext();) {
                    IndividualReservation resv = (IndividualReservation) iter.next();
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
                String puid = (String) puids.get(i);
                String overLimit = (String) overLimits.get(i);
                String expDate = (String) expDates.get(i);
                IndividualReservation r = null;
                
                // New Reservation
                if (resvId==null || resvId.trim().length()==0) {
                    r = new IndividualReservation();
                }
                
                // Load existing reservation
                else {
                    r = idao.get(new Long(resvId.trim()));
                }
                
                r.setOwner(owner);
                r.setOwnerClassId(ownerType);
                r.setPriority(new Integer(resvPriority));
                r.setExternalUniqueId(puid);
                r.setOverLimit(new Boolean(overLimit));
                r.setReservationType(rdao.get(new Long(resvType)));

                if (expDate==null || expDate.trim().length()==0)
                    r.setExpirationDate(null);
                else
                    r.setExpirationDate(new SimpleDateFormat("MM/dd/yyyy").parse(expDate));
                
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
            IndividualReservationEditForm frm) throws Exception {
        
        Collection individualResv = super.getReservations(frm, IndividualReservation.class);
        if (individualResv!=null && individualResv.size()>0) {
            for (Iterator iter=individualResv.iterator(); iter.hasNext();) {
                IndividualReservation resv = (IndividualReservation) iter.next();
                frm.addReservation(resv);
            }
        }
        
        // Add blank rows
        if (frm.getAddBlankRow().booleanValue())
            frm.addBlankRows();
    }

}