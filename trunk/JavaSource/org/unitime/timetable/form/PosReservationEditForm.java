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
import org.unitime.timetable.model.PosReservation;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DynamicList;


/** 
 * MyEclipse Struts
 * Creation date: 09-01-2006
 * 
 * XDoclet definition:
 * @struts:form name="posReservationEditForm"
 */
public class PosReservationEditForm extends AcademicAreaPosReservationForm {

    // --------------------------------------------------------- Instance Variables
    
    private List posMajorId;

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

        ArrayList l = new ArrayList();
        List academicClassificationId = super.getAcademicClassificationId();
        for (int i=0; i<posMajorId.size(); i++) {
            l.add(posMajorId.get(i).toString() + academicClassificationId.get(i).toString());
        }
        
        if(!checkList(l, false)) {
            errors.add("posMajorId", 
                    new ActionMessage(
                            "errors.generic", 
                            "Invalid POS Major / Academic Classification: Check for duplicate / blank values. ") );
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
        posMajorId = DynamicList.getInstance(new ArrayList(), factoryResv);
    }

    public List getPosMajorId() {
        return posMajorId;
    }
    public void setPosMajorId(List posMajorId) {
        this.posMajorId = posMajorId;
    }
    public String getPosMajorId(int key) {
        return posMajorId.get(key).toString();
    }
    public void setPosMajorId(int key, Object value) {
        this.posMajorId.set(key, value);
    }
    
    public void addToPosMajorId(String posMajorId) {
        this.posMajorId.add(posMajorId);
    }
    
    
    public void addBlankRows() {
       super.addBlankRows();
       for (int i=0; i<RESV_ROWS_ADDED; i++) {
           addToPosMajorId(Constants.BLANK_OPTION_VALUE);
       }
    }
    
    public void clear() {
        super.clear();
        this.posMajorId.clear();
    }

    public void addReservation(PosReservation resv) {
        super.addReservation(resv);        
        addToPosMajorId(resv.getPosMajor().getUniqueId().toString());
    }
    
    public void removeRow(int rowNum) {
        if (rowNum>=0) {
            super.removeRow(rowNum);
            posMajorId.remove(rowNum);
        }
    }
}
