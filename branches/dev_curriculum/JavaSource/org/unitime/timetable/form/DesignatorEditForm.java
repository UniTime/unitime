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
import org.unitime.timetable.model.dao.DesignatorDAO;


/** 
 * MyEclipse Struts
 * Creation date: 07-26-2006
 * 
 * XDoclet definition:
 * @struts:form name="designatorEditForm"
 */
public class DesignatorEditForm extends ActionForm {

	private static final long serialVersionUID = 2778373661037990914L;
	// --------------------------------------------------------- Instance Variables
    private Long uniqueId;
    private Long subjectAreaId;
    private String subjectAreaAbbv;
    private String instructorName;
    private Long instructorId;
    private String code;
    private Collection subjectAreas;
    private String readOnly;
    private String op;

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

        // Check UniqueId
        if (op.equals(rsc.getMessage("button.updateDesignator"))) {
            if (uniqueId==null || uniqueId.longValue()<1L) {
                errors.add( "uniqueId", 
                        	new ActionMessage("errors.generic", "Cannot update - Unique Id was not supplied"));
            }
        }
        
        if (subjectAreaId==null || subjectAreaId.intValue()<1) {
            errors.add( "subjectAreaId", 
                		new ActionMessage("errors.required", "Subject"));
        }
        
        if (instructorId==null || instructorId.longValue()<1L) {
            errors.add( "instructorId", 
                		new ActionMessage("errors.required", "Instructor"));
        }

        if (code==null || code.trim().length()==0) {
            errors.add( "code", 
                		new ActionMessage("errors.required", "Code"));
        }

        // Check for duplicate designator codes
        if (op.equals(rsc.getMessage("button.saveDesignator")) && errors.size()==0) {
        	if (DesignatorDAO.find(subjectAreaId, code, null))
                errors.add( "code", 
                    	new ActionMessage("errors.generic", "The designator code is assigned to another instructor in this subject area"));
        		
        }
        
        if (op.equals(rsc.getMessage("button.updateDesignator")) && errors.size()==0) {
        	if (DesignatorDAO.find(subjectAreaId, code, uniqueId))
                errors.add( "code", 
                    	new ActionMessage("errors.generic", "The designator code is assigned to another instructor in this subject area"));
        		
        }
        
        return errors;

    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        uniqueId = new Long(0);
        subjectAreaId = new Long(0);
        instructorId = new Long(0);
        code = "";
        subjectAreas = null;
        readOnly = "";
        instructorName = "";
        subjectAreaAbbv = "";
    }
    
    
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public Long getInstructorId() {
        return instructorId;
    }
    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }
    public Long getSubjectAreaId() {
        return subjectAreaId;
    }
    public void setSubjectAreaId(Long subjectAreaId) {
        this.subjectAreaId = subjectAreaId;
    }
    public Collection getSubjectAreas() {
        return subjectAreas;
    }
    public void setSubjectAreas(Collection subjectAreas) {
        this.subjectAreas = subjectAreas;
    }
    public Long getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }
    public String getReadOnly() {
        return readOnly;
    }
    public void setReadOnly(String readOnly) {
        this.readOnly = readOnly;
    }
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }    
    public String getInstructorName() {
        return instructorName;
    }
    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }
    public String getSubjectAreaAbbv() {
        return subjectAreaAbbv;
    }
    public void setSubjectAreaAbbv(String subjectAreaAbbv) {
        this.subjectAreaAbbv = subjectAreaAbbv;
    }
}
