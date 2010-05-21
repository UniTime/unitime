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
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.util.ComboBoxLookup;


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
	private int iExamType;
    private boolean iHasMidtermExams = false;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null;
		iShowSections = false;
		iTable = null;
		iNrRows = iNrColumns = 0;
		iExamType = Exam.sExamTypeFinal;
		ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
		try {
			if (solver!=null)
				iExamType = solver.getProperties().getPropertyInt("Exam.Type", iExamType);
		} catch (Exception e) {}
        try {
            iHasMidtermExams = Exam.hasMidtermExams(Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId());
        } catch (Exception e) {}
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	
	public boolean getShowSections() { return iShowSections; }
	public void setShowSections(boolean showSections) { iShowSections = showSections; }
	
	public Long getSubjectArea() { return iSubjectArea; }
	public String getSubjectAreaAbbv() { return new SubjectAreaDAO().get(iSubjectArea).getSubjectAreaAbbreviation(); }
	public void setSubjectArea(Long subjectArea) { iSubjectArea = subjectArea; } 
	public Collection getSubjectAreas() { return iSubjectAreas; }
	public void setSubjectAreas(Collection subjectAreas) { iSubjectAreas = subjectAreas; }
	
	public void load(HttpSession session) {
	    setShowSections(UserData.getPropertyBoolean(session,"ExamReport.showSections", true));
	    setSubjectArea(session.getAttribute("ExamReport.subjectArea")==null?null:(Long)session.getAttribute("ExamReport.subjectArea"));
	    try {
	        iSubjectAreas = new TreeSet(
	                new SubjectAreaDAO().getSession().createQuery(
	                        "select distinct o.course.subjectArea from Exam x inner join x.owners o where "+
	                        "x.session.uniqueId=:sessionId")
	                        .setLong("sessionId", Session.getCurrentAcadSession(Web.getUser(session)).getUniqueId())
	                        .setCacheable(true).list());
	    } catch (Exception e) {}
	    setExamType(session.getAttribute("Exam.Type")==null?iExamType:(Integer)session.getAttribute("Exam.Type"));
	}
	    
    public void save(HttpSession session) {
        UserData.setPropertyBoolean(session,"ExamReport.showSections", getShowSections());
        if (getSubjectArea()==null)
            session.removeAttribute("ExamReport.subjectArea");
        else
            session.setAttribute("ExamReport.subjectArea", getSubjectArea());
        session.setAttribute("Exam.Type", getExamType());
    }
    
    public void setTable(String table, int cols, int rows) {
        iTable = table; iNrColumns = cols; iNrRows = rows;
    }
    
    public String getTable() { return iTable; }
    public int getNrRows() { return iNrRows; }
    public int getNrColumns() { return iNrColumns; }
    public int getExamType() { return iExamType; }
    public void setExamType(int type) { iExamType = type; }
    public Collection getExamTypes() {
    	Vector ret = new Vector(Exam.sExamTypes.length);
        for (int i=0;i<Exam.sExamTypes.length;i++) {
            if (i==Exam.sExamTypeMidterm && !iHasMidtermExams) continue;
            ret.add(new ComboBoxLookup(Exam.sExamTypes[i], String.valueOf(i)));
        }
    	return ret;
    }
}

