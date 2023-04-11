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
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.ComboBoxLookup;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class ClassesForm implements UniTimeForm {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private static final long serialVersionUID = -2473101585994845220L;
	private String iOp = null;
	private Long iSession = null;
	private String iSubjectArea = null;
	private String iCourseNumber = null;
	private Collection<ComboBoxLookup> iSubjectAreas = null;
	private List<ComboBoxLookup> iSessions = null;
	private String iTable = null;
	private int iNrColumns;
	private int iNrRows;
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
	
	public String getCourseNumber() { return iCourseNumber; }
	public void setCourseNumber(String courseNumber) { iCourseNumber = courseNumber; }
	
	public Long getSession() { return iSession; }
	public void setSession(Long session) { iSession = session; }
	public Collection<ComboBoxLookup> getSessions() { return iSessions; }
	
	public Boolean canDisplayAllSubjectsAtOnce(){
		Boolean displayAll = Boolean.valueOf(false); 
		if (iSession != null){
			String queryStr = "select count(io) from InstructionalOffering io where io.session.uniqueId = :sessionId";
			int count = ((Number)SessionDAO.getInstance().getSession().createQuery(queryStr).setParameter("sessionId", iSession, org.hibernate.type.LongType.INSTANCE).setCacheable(true).uniqueResult()).intValue();
			if (count <= 300){
				displayAll = Boolean.valueOf(true);
			}
		}
		return(displayAll);
	}

	
	public void load(HttpSession session) {
	    setSubjectArea(session.getAttribute("Classes.subjectArea")==null?null:(String)session.getAttribute("Classes.subjectArea"));
        setCourseNumber(session.getAttribute("Classes.courseNumber")==null?null:(String)session.getAttribute("Classes.courseNumber"));
	    iSessions = new ArrayList<ComboBoxLookup>();
        setSession(session.getAttribute("Classes.session")==null?null:(Long)session.getAttribute("Classes.session"));
        Long lastSessionId = null;
        boolean hasSession = false;
	    for (Iterator i=Session.getAllSessions().iterator();i.hasNext();) {
	        Session s = (Session)i.next();
	        if (s.getStatusType()!=null && s.getStatusType().canNoRoleReportClass() && Solution.hasTimetable(s.getUniqueId())) {
	            if (s.getUniqueId().equals(getSession())) hasSession = true;
	            lastSessionId = s.getUniqueId();
	            iSessions.add(new ComboBoxLookup(s.getLabel(),s.getUniqueId().toString()));
	        }
	    }
	    if (!hasSession) { setSession(null); setSubjectArea(null); }
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
	}
	    
    public void save(HttpSession session) {
        if (getSubjectArea()==null)
            session.removeAttribute("Classes.subjectArea");
        else
            session.setAttribute("Classes.subjectArea", getSubjectArea());
        if (getCourseNumber()==null)
            session.removeAttribute("Classes.courseNumber");
        else
            session.setAttribute("Classes.courseNumber", getCourseNumber());
        if (getSession()==null)
            session.removeAttribute("Classes.session");
        else
            session.setAttribute("Classes.session", getSession());
    }
    
    public void setTable(String table, int cols, int rows) {
        iTable = table; iNrColumns = cols; iNrRows = rows;
    }
    
    public String getTable() { return iTable; }
    public int getNrRows() { return iNrRows; }
    public int getNrColumns() { return iNrColumns; }
    
    public String getUsername() { return iUser; }
    public void setUsername(String user) { iUser = user; }
    public String getPassword() { return iPassword; }
    public void setPassword(String password) { iPassword = password; }
    public String getMessage() { return iMessage; }
    public void setMessage(String message) { iMessage = message; }
    
    public String getSessionLabel() {
        if (iSessions==null) return "";
        for (ComboBoxLookup s: iSessions) {
            if (Long.valueOf(s.getValue()).equals(getSession())) return s.getLabel();
        }
        return "";
    }
    
    public String getEmptyMessage() {
    	if (getSession() == null) {
    		return MSG.infoNoClassesAvailable();
    	} else if (getSubjectArea() == null || getSubjectArea().isEmpty()) {
    		if (MSG.buttonApply().equals(iOp))
    			return MSG.infoNoSubjectAreaSelected();
    		else
    			return null;
    	} else {
    		if ("--ALL--".equals(getSubjectArea())) {
    			return MSG.infoNoClassesAvailableForSession(getSessionLabel());
    		} else {
    			if (getCourseNumber() != null && !getCourseNumber().isEmpty())
    				return MSG.infoNoClassesAvailableForCourse(getSubjectArea(), getCourseNumber());
    			else
    				return MSG.infoNoClassesAvailableForSubject(getSubjectArea());
    		}
    	}
    }
}
