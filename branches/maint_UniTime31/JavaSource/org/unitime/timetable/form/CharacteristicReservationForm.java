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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.CharacteristicReservation;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DynamicList;


/**
 * Base Characteristic Reservation Form
 * Subclasses: StudentGroupReservation, AcademicAreaPosReservation,
 * and CourseReservation forms
 * 
 * @author Heston Fernandes
 */
public class CharacteristicReservationForm extends ReservationForm {
    
    // --------------------------------------------------------- Instance Variables
    private List reserved;
    private List priorEnrollment;
    private List projectedEnrollment;
    private List requested;

    // --------------------------------------------------------- Methods

    /** 
     * Method validate
     * @param mapping
     * @param request
     * @return ActionErrors
     */
    public ActionErrors validate(
        ActionMapping mapping,
        HttpServletRequest request) {

        ActionErrors errors = super.validate(mapping, request);
        
	    //TODO Reservations Bypass - to be removed later
        if(!getOwnerType().equals(Constants.RESV_OWNER_CLASS) && !checkReserved()) {
            errors.add("reserved", 
                    new ActionMessage("errors.generic", "Reserved Spaces must be a valid number. (&gt; 0 for new reservations, &gt;= 0 for pre-existing reservations)") );
        }
	    // End Bypass
	    
        //TODO Reservations - functionality to be made visible later
        /*
        if(!checkListNumber(reserved, false, new Integer(1), null)) {
            errors.add("reserved", 
                    new ActionMessage("errors.integerGt", "Reserved Spaces", "0") );
        }
        */
        
        if(!checkListNumber(priorEnrollment, true, new Integer(0), null)) {
            errors.add("priorEnrollment", 
                    new ActionMessage("errors.integerGtEq", "Prior Enrollment", "0") );
        }
        
        if(!checkListNumber(projectedEnrollment, true, new Integer(0), null)) {
            errors.add("projectedEnrollment", 
                    new ActionMessage("errors.integerGtEq", "Projected Enrollment", "0") );
        }
        
        return errors;        
    }

    /**
     * Checks valid value for reserved spaces
     * Value can be equal to 0 only if it is not a pre-existing reservation
     * (no projected enrollment data exists)
     * @return true if all checks pass, false otherwise
     */
    private boolean checkReserved() {
        for(int i=0; i<reserved.size(); i++) {
            String value = ((String) reserved.get(i));
            int intval = 0;
            
            if(value==null || value.trim().equals(Constants.BLANK_OPTION_VALUE)) 
                return false;
            
            // Check is number 
            try {
                intval = Integer.parseInt(value);
            }
            catch (NumberFormatException nfe) {
                return false;
            }
            
            Object o = projectedEnrollment.get(i);
            if ( (o==null || o.toString().equals(Constants.BLANK_OPTION_VALUE)) 
                    && intval==0) {
                return false;
            }            
        }    
        
        return true;
    }
    
    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        reserved = DynamicList.getInstance(new ArrayList(), factoryResv);
        priorEnrollment = DynamicList.getInstance(new ArrayList(), factoryResv);
        projectedEnrollment = DynamicList.getInstance(new ArrayList(), factoryResv);
        requested = DynamicList.getInstance(new ArrayList(), factoryResv);
    }
    
    public List getPriorEnrollment() {
        return priorEnrollment;
    }
    public void setPriorEnrollment(List priorEnrollment) {
        this.priorEnrollment = priorEnrollment;
    }
    public String getPriorEnrollment(int key) {
        return priorEnrollment.get(key).toString();
    }
    public void setPriorEnrollment(int key, Object value) {
        this.priorEnrollment.set(key, value);
    }
    
    public List getProjectedEnrollment() {
        return projectedEnrollment;
    }
    public void setProjectedEnrollment(List projectedEnrollment) {
        this.projectedEnrollment = projectedEnrollment;
    }
    public String getProjectedEnrollment(int key) {
        return projectedEnrollment.get(key).toString();
    }
    public void setProjectedEnrollment(int key, Object value) {
        this.projectedEnrollment.set(key, value);
    }
    
    public List getReserved() {
        return reserved;
    }
    public void setReserved(List reserved) {
        this.reserved = reserved;
    }
    public String getReserved(int key) {
        return reserved.get(key).toString();
    }
    public void setReserved(int key, Object value) {
        this.reserved.set(key, value);
    }
    
    public List getRequested() {
        return requested;
    }
    public void setRequested(List requested) {
        this.requested = requested;
    }
    public String getRequested(int key) {
        return requested.get(key).toString();
    }
    public void setRequested(int key, Object value) {
        this.requested.set(key, value);
    }

    
    public void addToPriorEnrollment(String priorEnrollment) {
        this.priorEnrollment.add(priorEnrollment);
    }
    
    public void addToProjectedEnrollment(String projectedEnrollment) {
        this.projectedEnrollment.add(projectedEnrollment);
    }
    
    public void addToReserved(String reserved) {
        this.reserved.add(reserved);
    }
    
    public void addToRequested(String requested) {
        this.requested.add(requested);
    }
    
    
   public void addBlankRows() {
       super.addBlankRows();
       for (int i=0; i<RESV_ROWS_ADDED; i++) {
           addToPriorEnrollment(Constants.BLANK_OPTION_VALUE);
           addToProjectedEnrollment(Constants.BLANK_OPTION_VALUE);
           addToReserved(Constants.BLANK_OPTION_VALUE);
           addToRequested(Constants.BLANK_OPTION_VALUE);
       }
    }
    
    public void clear() {
        super.clear();
        this.priorEnrollment.clear();
        this.projectedEnrollment.clear();
        this.reserved.clear();
        this.requested.clear();
        
    }

    public void addReservation(CharacteristicReservation resv) {
        super.addReservation(resv);
        
        if (resv.getPriorEnrollment()!=null)
            addToPriorEnrollment(resv.getPriorEnrollment().toString());
        else
            addToPriorEnrollment("");
        
        if (resv.getProjectedEnrollment()!=null)
            addToProjectedEnrollment(resv.getProjectedEnrollment().toString());
        else
            addToProjectedEnrollment("");
        
        if (resv.getRequested()!=null)
            addToRequested(resv.getRequested().toString());
        else
            addToRequested("");
        
        addToReserved(resv.getReserved().toString());
    }

    public void removeRow(int rowNum) {
        if (rowNum>=0) {
            super.removeRow(rowNum);
            priorEnrollment.remove(rowNum);
            projectedEnrollment.remove(rowNum);
            reserved.remove(rowNum);
            requested.remove(rowNum);
        }
    }
}
