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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DynamicList;


/** 
 * MyEclipse Struts
 * Creation date: 09-01-2006
 * 
 * XDoclet definition:
 * @struts:form name="individualReservationEditForm"
 */
public class IndividualReservationEditForm extends ReservationForm {

    // --------------------------------------------------------- Instance Variables
    
    private List puid;
    private List studentName;
    private List overLimit;
    private List expirationDate;

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
        
        //TODO hfernan - Individual Reservation: validate puid for length and by lookup
        
        if(!checkList(puid, true)) {
            errors.add("puid", 
                    new ActionMessage(
                            "errors.generic", 
                            "Invalid PuID: Check for duplicate / blank value. ") );
        }
        
        if(!checkListDate(expirationDate, false)) {
            errors.add("expirationDate", 
                    new ActionMessage( "errors.generic", "Expiration Date must be of the form mm/dd/yyyy and must be in the future. ") );
        }
        
        return errors;        
    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        puid = DynamicList.getInstance(new ArrayList(), factoryResv);;
        studentName = DynamicList.getInstance(new ArrayList(), factoryResv);;
        overLimit = DynamicList.getInstance(new ArrayList(), factoryResv);;
        expirationDate = DynamicList.getInstance(new ArrayList(), factoryResv);;
    }

    public List getExpirationDate() {
        return expirationDate;
    }
    public void setExpirationDate(List expirationDate) {
        this.expirationDate = expirationDate;
    }
    public String getExpirationDate(int key) {
        return expirationDate.get(key).toString();
    }
    public void setExpirationDate(int key, Object value) {
        this.expirationDate.set(key, value);
    }
    
    
    public List getOverLimit() {
        return overLimit;
    }
    public void setOverLimit(List overLimit) {
        this.overLimit = overLimit;
    }
    public String getOverLimit(int key) {
        return overLimit.get(key).toString();
    }
    public void setOverLimit(int key, Object value) {
        this.overLimit.set(key, value);
    }
    
    
    public List getPuid() {
        return puid;
    }
    public void setPuid(List puid) {
        this.puid = puid;
    }
    public String getPuid(int key) {
        return puid.get(key).toString();
    }
    public void setPuid(int key, Object value) {
        this.puid.set(key, value);
    }
    
    
    public List getStudentName() {
        return studentName;
    }
    public void setStudentName(List studentName) {
        this.studentName = studentName;
    }
    public String getStudentName(int key) {
        return studentName.get(key).toString();
    }
    public void setStudentName(int key, Object value) {
        this.studentName.set(key, value);
    }
    
    
    public void addToExpirationDate(String expirationDate) {
        this.expirationDate.add(expirationDate);
    }
    
    public void addToOverLimit(String overLimit) {
        this.overLimit.add(overLimit);
    }
    
    public void addToPuid(String puid) {
        this.puid.add(puid);
    }
    public void addToStudentName(String studentName) {
        this.studentName.add(studentName);
    }
        
    
   public void addBlankRows() {
       super.addBlankRows();
       for (int i=0; i<RESV_ROWS_ADDED; i++) {
           addToExpirationDate(Constants.BLANK_OPTION_VALUE);
           addToOverLimit("false");
           addToPuid(Constants.BLANK_OPTION_VALUE);
           addToStudentName(Constants.BLANK_OPTION_VALUE);
       }
    }
    
    public void clear() {
        super.clear();
        this.expirationDate.clear();
        this.overLimit.clear();
        this.puid.clear();
        this.studentName.clear();
    }

    public void addReservation(IndividualReservation resv) {
        super.addReservation(resv);
        
        addToPuid(resv.getExternalUniqueId());
        addToStudentName(""); //TODO hfernan - Individual Reservation: do llokup on puid
        addToOverLimit(resv.isOverLimit().toString());
        
        if (resv.getExpirationDate()!=null)
            addToExpirationDate(new SimpleDateFormat("MM/dd/yyyy").format(resv.getExpirationDate()));
        else
            addToExpirationDate("");
    }

    public void removeRow(int rowNum) {
        if (rowNum>=0) {
            super.removeRow(rowNum);
            expirationDate.remove(rowNum);
            overLimit.remove(rowNum);
            puid.remove(rowNum);
            studentName.remove(rowNum);
        }
    }
}
