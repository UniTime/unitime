/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org, and individual contributors
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
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.ComboBoxLookup;


/** 
 * @author Tomas Muller
 */
public class ExamsForm extends ActionForm {
	private String iOp = null;
	private Long iSession = null;
	private String iSubjectArea = null;
	private Collection iSubjectAreas = null;
	private Vector iSessions = null;
	private String iTable = null;
	private int iNrColumns;
	private int iNrRows;
	private int iExamType;
	private String iMessage;
	
	private String iUser, iPassword;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null;
		iTable = null;
		iNrRows = iNrColumns = 0;
		iExamType = Exam.sExamTypeFinal;
		iSession = null; 
		iUser = null;
		iPassword = null;
		iMessage = null;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	
	public String getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(String subjectArea) { iSubjectArea = subjectArea; } 
	public Collection getSubjectAreas() { return iSubjectAreas; }
	
	public Long getSession() { return iSession; }
	public void setSession(Long session) { iSession = session; }
	public Collection getSessions() { return iSessions; }
	
	public void load(HttpSession session) {
	    setSubjectArea(session.getAttribute("Exams.subjectArea")==null?null:(String)session.getAttribute("Exams.subjectArea"));
	    iSessions = new Vector();
	    for (Iterator i=Session.getAllSessions().iterator();i.hasNext();) {
	        Session s = (Session)i.next();
	        if (s.getStatusType()!=null && s.getStatusType().canNoRoleReport() && Exam.hasTimetable(s.getUniqueId())) 
	            iSessions.add(new ComboBoxLookup(s.getLabel(),s.getUniqueId().toString()));
	    }
	    setSession(session.getAttribute("Exams.session")==null?iSessions.isEmpty()?null:Long.valueOf(((ComboBoxLookup)iSessions.lastElement()).getValue()):(Long)session.getAttribute("Exams.session"));
	    iSubjectAreas = new TreeSet(new SubjectAreaDAO().getSession().createQuery("select distinct sa.subjectAreaAbbreviation from SubjectArea sa").setCacheable(true).list());
	    setExamType(session.getAttribute("Exams.examType")==null?iExamType:(Integer)session.getAttribute("Exams.examType"));
	}
	    
    public void save(HttpSession session) {
        if (getSubjectArea()==null)
            session.removeAttribute("Exams.subjectArea");
        else
            session.setAttribute("Exams.subjectArea", getSubjectArea());
        session.setAttribute("Exams.examType", getExamType());
        if (getSession()==null)
            session.removeAttribute("Exams.session");
        else
            session.setAttribute("Exams.session", getSession());
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
            ret.add(new ComboBoxLookup(ApplicationProperties.getProperty("tmtbl.exam.name.type."+Exam.sExamTypes[i],Exam.sExamTypes[i]), String.valueOf(i)));
        }
    	return ret;
    }
    
    public String getUsername() { return iUser; }
    public void setUsername(String user) { iUser = user; }
    public String getPassword() { return iPassword; }
    public void setPassword(String password) { iPassword = password; }
    public String getMessage() { return iMessage; }
    public void setMessage(String message) { iMessage = message; }
}

