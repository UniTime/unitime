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

import java.util.Collection;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.SubjectAreaDAO;


/** 
 * @author Tomas Muller
 */
public class ExamReportForm extends ActionForm {
	private String iOp = null;
	private boolean iShowSections = false;
	private Long iSubjectArea = null;
	private Collection iSubjectAreas = null;
	private String iTable = null;
	private int iNrColumns;
	private int iNrRows;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null;
		iShowSections = false;
		iTable = null;
		iNrRows = iNrColumns = 0;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	
	public boolean getShowSections() { return iShowSections; }
	public void setShowSections(boolean showSections) { iShowSections = showSections; }
	
	public Long getSubjectArea() { return iSubjectArea; }
	public String getSubjectAreaAbbv() { return new SubjectAreaDAO().get(iSubjectArea).getSubjectAreaAbbreviation(); }
	public void setSubjectArea(Long subjectArea) { iSubjectArea = subjectArea; } 
	public Collection getSubjectAreas() { return iSubjectAreas; }
	
	public void load(HttpSession session) {
	    setShowSections(UserData.getPropertyBoolean(session,"ExamReport.showSections", true));
	    setSubjectArea(session.getAttribute("ExamReport.subjectArea")==null?null:(Long)session.getAttribute("ExamReport.subjectArea"));
	    try {
	        iSubjectAreas = new TreeSet(SubjectArea.getSubjectAreaList(Session.getCurrentAcadSession(Web.getUser(session)).getUniqueId()));
	    } catch (Exception e) {}
	}
	    
    public void save(HttpSession session) {
        UserData.setPropertyBoolean(session,"ExamReport.showSections", getShowSections());
        if (getSubjectArea()==null)
            session.removeAttribute("ExamReport.subjectArea");
        else
            session.setAttribute("ExamReport.subjectArea", getSubjectArea());
    }
    
    public void setTable(String table, int cols, int rows) {
        iTable = table; iNrColumns = cols; iNrRows = rows;
    }
    
    public String getTable() { return iTable; }
    public int getNrRows() { return iNrRows; }
    public int getNrColumns() { return iNrColumns; }

}

