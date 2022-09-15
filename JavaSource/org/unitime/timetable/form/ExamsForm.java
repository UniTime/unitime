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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.ComboBoxLookup;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class ExamsForm implements UniTimeForm {
	private static final long serialVersionUID = 8434268097497866325L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static ExaminationMessages XMSG = Localization.create(ExaminationMessages.class);

	private String iOp = null;
	private Long iSession = null;
	private String iSubjectArea = null;
	private Collection<ComboBoxLookup> iSubjectAreas = null;
	private List<ComboBoxLookup> iSessions = null;
	private String iTable = null;
	private int iNrColumns;
	private int iNrRows;
	private Long iExamType;
	private String iMessage;
	
	private String iUser, iPassword;

	@Override
	public void validate(UniTimeAction action) {
	}

	@Override
	public void reset() {
		iOp = null;
		iTable = null;
		iNrRows = iNrColumns = 0;
		iExamType = null;
		iSession = null; 
		iUser = null;
		iPassword = null;
		iMessage = null;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	
	public String getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(String subjectArea) { iSubjectArea = subjectArea; } 
	public Collection<ComboBoxLookup> getSubjectAreas() { return iSubjectAreas; }
	
	public Long getSession() { return iSession; }
	public void setSession(Long session) { iSession = session; }
	public Collection<ComboBoxLookup> getSessions() { return iSessions; }
	
	public Boolean canDisplayAllSubjectsAtOnce(){
		Boolean displayAll = Boolean.valueOf(false); 
		if (iSession != null){
			String queryStr = "select count(e) from Exam e where e.session.uniqueId = :sessionId";
			int count = ((Number)SessionDAO.getInstance().getQuery(queryStr).setLong("sessionId", iSession).setCacheable(true).uniqueResult()).intValue();
			if (count <= 300){
				displayAll = Boolean.valueOf(true);
			}
		}
		return(displayAll);
	}

	public void load(HttpSession session) {
	    setSubjectArea(session.getAttribute("Exams.subjectArea")==null?null:(String)session.getAttribute("Exams.subjectArea"));
	    iSessions = new ArrayList<ComboBoxLookup>();
        setSession(session.getAttribute("Exams.session")==null?null:(Long)session.getAttribute("Exams.session"));
        Long lastSessionId = null;
	    for (Iterator i=Session.getAllSessions().iterator();i.hasNext();) {
	        Session s = (Session)i.next();
	        if (s.getStatusType()!=null && (s.canNoRoleReportExamFinal() || s.canNoRoleReportExamMidterm()) && Exam.hasTimetable(s.getUniqueId())) {
	        	lastSessionId = s.getUniqueId();
	            iSessions.add(new ComboBoxLookup(s.getLabel(),s.getUniqueId().toString()));
	        }
	    }
	    if (lastSessionId == null) { setSession(null); setSubjectArea(null); }
	    if (getSession() == null && lastSessionId != null) setSession(lastSessionId);
	    iSubjectAreas = new ArrayList<ComboBoxLookup>();
	    if (canDisplayAllSubjectsAtOnce())
	    	iSubjectAreas.add(new ComboBoxLookup(MSG.allSubjects(), "--ALL--"));
	    else if ("--ALL--".equals(iSubjectArea))
	    	iSubjectArea = null;
	    boolean hasSubject = false;
	    if (iSession != null) {
	    	for (SubjectArea subject: SubjectArea.getAllSubjectAreas(iSession)) {
	    		if (subject.getSubjectAreaAbbreviation().equals(iSubjectArea)) hasSubject = true;
	    		iSubjectAreas.add(new ComboBoxLookup(subject.getSubjectAreaAbbreviation(), subject.getSubjectAreaAbbreviation()));
	    	}
	    }
	    if (!hasSubject && !"--ALL--".equals(iSubjectArea))
	    	iSubjectArea = null;
	    setExamType(session.getAttribute("Exams.examType")==null?iExamType:(Long)session.getAttribute("Exams.examType"));
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
    public Long getExamType() { return iExamType; }
    public void setExamType(Long type) { iExamType = type; }

    
    public String getUsername() { return iUser; }
    public void setUsername(String user) { iUser = user; }
    public String getPassword() { return iPassword; }
    public void setPassword(String password) { iPassword = password; }
    public String getMessage() { return iMessage; }
    public void setMessage(String message) { iMessage = message; }
    
    public String getExamTypeLabel() {
    	ExamType type = (iExamType == null ? null : ExamTypeDAO.getInstance().get(iExamType));
    	return (type == null ? "" : type.getLabel());
    }
    
    public boolean isFinals() {
    	ExamType type = (iExamType == null ? null : ExamTypeDAO.getInstance().get(iExamType));
    	return (type == null || type.getType() == ExamType.sExamTypeFinal);
    }
    
    public String getSessionLabel() {
        if (iSessions==null) return "";
        for (ComboBoxLookup s: iSessions) {
            if (Long.valueOf(s.getValue()).equals(getSession())) return s.getLabel();
        }
        return "";
    }
    
    public String getEmptyMessage() {
    	if (getSubjectArea() == null || getSubjectArea().isEmpty()) {
    		if (XMSG.buttonApply().equals(iOp))
    			return XMSG.infoNoSubjectAreaSelected();
    		else
    			return null;
    	} else {
    		if ("--ALL--".equals(getSubjectArea())) {
    			return XMSG.infoNoExaminationsAvailable(getExamTypeLabel(), getSessionLabel());
    		} else {
    			return XMSG.infoNoExaminationsAvailableForSubject(getExamTypeLabel(), getSubjectArea());
    		}
    	}
    }
}
