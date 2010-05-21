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

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 08-30-2006
 * 
 * XDoclet definition:
 * @struts:form name="reservationListForm"
 */
public class ReservationListForm extends ActionForm {

    // --------------------------------------------------------- Instance Variables

    private String op;
	private String subjectAreaId;
	private String courseNbr;
	private Collection subjectAreas;

	// Filters
	private Boolean ioResv;
	private Boolean configResv;
	private Boolean classResv;
	
	//TODO Reservations functionality to be removed later
	private Boolean courseResv;
	// End Bypass

	private Boolean indResv;
	private Boolean sgResv;
	private Boolean aaResv;
	private Boolean posResv;
	private Boolean crsResv;
	
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

        // Get Message Resources
        MessageResources rsc = 
            (MessageResources) super.getServlet()
            	.getServletContext().getAttribute(Globals.MESSAGES_KEY);
        
        if( subjectAreaId==null 
                || subjectAreaId.trim().length()==0
                || subjectAreaId.equals(Constants.BLANK_OPTION_VALUE) ) {
            errors.add("subjectAreaId", 
                    	new ActionMessage("errors.required", "Subject Area") );
        }

        if (op.equals(rsc.getMessage("button.addReservationIo"))) {
            if( courseNbr==null || courseNbr.trim().length()==0 ) {
                errors.add("courseNbr", 
                        	new ActionMessage("errors.required", "Course Number") );
            }
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
        courseNbr = "";
        ioResv = null;
        configResv = null;
        classResv = null;
    	courseResv = null;
    	courseResv = null;
    	indResv = null;
    	sgResv = null;
    	posResv = null;
    	aaResv = null;
    	crsResv = null;
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
    
    public Boolean getClassResv() {
        return classResv;
    }
    public void setClassResv(Boolean classResv) {
        this.classResv = classResv;
    }
    
    public Boolean getConfigResv() {
        return configResv;
    }
    public void setConfigResv(Boolean configResv) {
        this.configResv = configResv;
    }
    
    public String getCourseNbr() {
        return courseNbr;
    }
    public void setCourseNbr(String courseNbr) {
        this.courseNbr = courseNbr;
    }
    
    public Boolean getIoResv() {
        return ioResv;
    }
    public void setIoResv(Boolean ioResv) {
        this.ioResv = ioResv;
    }
    
    public Boolean getCourseResv() {
        return courseResv;
    }
    public void setCourseResv(Boolean courseResv) {
        this.courseResv = courseResv;
    }
    
    public Boolean getAaResv() {
        return aaResv;
    }
    public void setAaResv(Boolean aaResv) {
        this.aaResv = aaResv;
    }
    
    public Boolean getCrsResv() {
        return crsResv;
    }
    public void setCrsResv(Boolean crsResv) {
        this.crsResv = crsResv;
    }
    
    public Boolean getIndResv() {
        return indResv;
    }
    public void setIndResv(Boolean indResv) {
        this.indResv = indResv;
    }
    
    public Boolean getPosResv() {
        return posResv;
    }
    public void setPosResv(Boolean posResv) {
        this.posResv = posResv;
    }
    
    public Boolean getSgResv() {
        return sgResv;
    }
    public void setSgResv(Boolean sgResv) {
        this.sgResv = sgResv;
    }
}
