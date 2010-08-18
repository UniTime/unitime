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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 08-30-2006
 * 
 * XDoclet definition:
 * @struts:form name="reservationEditForm"
 */
public class ReservationEditForm extends ReservationForm {

	private static final long serialVersionUID = 6051781096676082898L;

	// --------------------------------------------------------- Instance Variables
    
	private String subjectAreaId;
	private String courseNbr;
	
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
        if (errors==null)
            errors = new ActionErrors();

        // Get Message Resources
        MessageResources rsc = 
            (MessageResources) super.getServlet()
            	.getServletContext().getAttribute(Globals.MESSAGES_KEY);
        
        String op = getOp();
        
        if (op.equals(rsc.getMessage("button.addReservationIo"))) {
            if( subjectAreaId==null 
                    || subjectAreaId.trim().length()==0
                    || subjectAreaId.equals(Constants.BLANK_OPTION_VALUE) ) {
                errors.add("subjectAreaId", 
                        	new ActionMessage("errors.required", "Subject Area") );
            }

            if( courseNbr==null || courseNbr.trim().length()==0 ) {
                errors.add("courseNbr", 
                        	new ActionMessage("errors.required", "Course Number") );
            }            
        }
  
        if ( op.equals(rsc.getMessage("button.reservationNextStep")) ) {
            String resvClass = getReservationClass();
            if( resvClass==null 
                    || resvClass.trim().length()==0
                    || resvClass.equals(Constants.BLANK_OPTION_VALUE) ) {
                errors.add("resvClass", 
                        	new ActionMessage("errors.required", "Type") );
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
        super.reset(mapping, request);
    	subjectAreaId = null;
    	courseNbr = null;
    }
    
    public String getCourseNbr() {
        return courseNbr;
    }
    public void setCourseNbr(String courseNbr) {
        this.courseNbr = courseNbr;
    }
    
    public String getSubjectAreaId() {
        return subjectAreaId;
    }
    public void setSubjectAreaId(String subjectAreaId) {
        this.subjectAreaId = subjectAreaId;
    }
}
