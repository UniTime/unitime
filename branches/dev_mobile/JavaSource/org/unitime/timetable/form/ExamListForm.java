/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.form;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * @author Tomas Muller
 */
public class ExamListForm extends ActionForm {
    private static final long serialVersionUID = 0L;
    private String iSubjectAreaId = null;
    private String iCourseNbr = null;
    private String iOp = null;
    private Collection iSubjectAreas = null;
    private Long iExamType = null;
    
    public String getSubjectAreaId() { return iSubjectAreaId; }
    public void setSubjectAreaId(String subjectAreaId) { iSubjectAreaId = subjectAreaId; }
    public String getCourseNbr() { return iCourseNbr; }
    public void setCourseNbr(String courseNbr) { 
        iCourseNbr = courseNbr;
        if ("null".equals(iCourseNbr)) iCourseNbr = "";
    }
    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
    public Collection getSubjectAreas() { return iSubjectAreas; }
    public void setSubjectAreas(Collection subjectAreas) { iSubjectAreas = subjectAreas; }
    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        iSubjectAreaId = null; iCourseNbr = null; iOp = null;
        iExamType = null;
    }
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }
    
    public Long getExamType() { return iExamType; }
    public void setExamType(Long type) { iExamType = type; }
}
