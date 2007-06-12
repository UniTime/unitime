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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.ReservationType;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DynamicList;


/** 
 * MyEclipse Struts
 * Creation date: 09-01-2006
 * 
 * XDoclet definition:
 * @struts:form name="courseReservationEditForm"
 */
public class CourseReservationEditForm extends CharacteristicReservationForm {

    // --------------------------------------------------------- Instance Variables
    
    private List courseOfferingId;
    private String reservationTypeLabel;
    
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

        if(!checkList(courseOfferingId, false)) {
            errors.add("courseOfferingId", 
                    new ActionMessage(
                            "errors.generic", 
                            "Invalid Course Offering: Check for duplicate / blank values. ") );
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
        courseOfferingId = DynamicList.getInstance(new ArrayList(), factoryResv);
        
        ReservationType rt = ReservationType.getReservationTypebyRef(Constants.RESV_TYPE_PERM_REF);
        reservationTypeLabel = rt!=null	? rt.getLabel() : "Not Found";
     }

    public List getCourseOfferingId() {
        return courseOfferingId;
    }
    public void setCourseOfferingId(List courseOfferingId) {
        this.courseOfferingId = courseOfferingId;
    }
    
    public String getCourseOfferingId(int key) {
        return courseOfferingId.get(key).toString();
    }
    public void setCourseOfferingId(int key, Object value) {
        this.courseOfferingId.set(key, value);
    }
    
    public String getReservationTypeLabel() {
        return reservationTypeLabel;
    }
    public void setReservationTypeLabel(String reservationTypeLabel) {
        this.reservationTypeLabel = reservationTypeLabel;
    }
    
    public void addToCourseOfferingId(String courseOfferingId) {
        this.courseOfferingId.add(courseOfferingId);
    }
    
    public void addBlankRows() {
        ReservationType rt = ReservationType.getReservationTypebyRef(Constants.RESV_TYPE_PERM_REF);
        int currRows = this.courseOfferingId.size();
        super.addBlankRows();
        for (int i=0; i<RESV_ROWS_ADDED; i++) {
            addToCourseOfferingId(Constants.BLANK_OPTION_VALUE);
            setReservationType(currRows+i, rt.getUniqueId().toString());
        }
     }
     
     public void clear() {
         super.clear();
         this.courseOfferingId.clear();
     }

     public void addReservation(CourseOfferingReservation resv) {
         super.addReservation(resv);
         addToCourseOfferingId(resv.getCourseOffering().getUniqueId().toString());
     }
}