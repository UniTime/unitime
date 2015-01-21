/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.form;

import java.util.Collection;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;


/** 
 * @author Tomas Muller
 */
public class ExamReportForm extends ActionForm {
	private static final long serialVersionUID = -8009733200124355056L;
	private String iOp = null;
	private boolean iShowSections = false;
	private Long iSubjectArea = null;
	private Collection iSubjectAreas = null;
	private String iTable = null;
	private int iNrColumns;
	private int iNrRows;
	private Long iExamType;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null;
		iShowSections = false;
		iTable = null;
		iNrRows = iNrColumns = 0;
		iExamType = null;
		ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
		try {
			if (solver!=null)
				iExamType = solver.getProperties().getPropertyLong("Exam.Type", iExamType);
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
	
	public void load(SessionContext session) {
		load(session, false);
	}
	
	public void load(SessionContext session, boolean allSubjects) {
	    setShowSections("1".equals(session.getUser().getProperty("ExamReport.showSections", "1")));
	    setSubjectArea(session.getAttribute("ExamReport.subjectArea")==null?null:(Long)session.getAttribute("ExamReport.subjectArea"));
	    try {
	    	if (allSubjects) {
		        iSubjectAreas = new TreeSet(
		                new SubjectAreaDAO().getSession().createQuery(
		                        "from SubjectArea where session.uniqueId=:sessionId")
		                        .setLong("sessionId", session.getUser().getCurrentAcademicSessionId())
		                        .setCacheable(true).list());
	    	} else {
		        iSubjectAreas = new TreeSet(
		                new SubjectAreaDAO().getSession().createQuery(
		                        "select distinct o.course.subjectArea from Exam x inner join x.owners o where "+
		                        "x.session.uniqueId=:sessionId")
		                        .setLong("sessionId", session.getUser().getCurrentAcademicSessionId())
		                        .setCacheable(true).list());
	    	}
	    } catch (Exception e) {}
	    setExamType(session.getAttribute("Exam.Type")==null?iExamType:(Long)session.getAttribute("Exam.Type"));
	}
	    
    public void save(SessionContext session) {
    	session.getUser().setProperty("ExamReport.showSections", getShowSections() ? "1" : "0");
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
    public Long getExamType() { return iExamType; }
    public void setExamType(Long type) { iExamType = type; }
}

