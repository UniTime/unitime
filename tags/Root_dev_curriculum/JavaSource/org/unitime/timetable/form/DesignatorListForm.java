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

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 07-26-2006
 * 
 * XDoclet definition:
 * @struts:form name="designatorListForm"
 */
public class DesignatorListForm extends ActionForm {

    // --------------------------------------------------------- Instance Variables
	private String subjectAreaId;
	private Collection subjectAreas;
	private String op;
	private boolean editable;

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

        ActionErrors errors = new ActionErrors();
        
        if(subjectAreaId==null || subjectAreaId.trim().length()==0
                || subjectAreaId.equals(Constants.BLANK_OPTION_VALUE)) {
            errors.add("subjectAreaId", 
                    new ActionMessage("errors.required", "Subject Area") );
        }
        
        return errors;
        
    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        subjectAreaId = "";
        editable = false;
    }
    
    
    public String getSubjectAreaId() {
        return subjectAreaId;
    }
    public void setSubjectAreaId(String subjectAreaId) {
        this.subjectAreaId = subjectAreaId;
    }    
    
    public Collection getSubjectAreas() {
        return subjectAreas;
    }
    public void setSubjectAreas(Collection subjectAreas) {
        this.subjectAreas = subjectAreas;
    }
    
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
    
    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }
}
