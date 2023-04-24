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
import java.util.List;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ConstantsMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.IdValue;


/** 
 * @author Tomas Muller
 */
public class ExamReportForm implements UniTimeForm {
	private static final long serialVersionUID = -8009733200124355056L;
	protected static final ConstantsMessages CONST = Localization.create(ConstantsMessages.class);

	private String iOp = null;
	private boolean iShowSections = false;
	private Long iSubjectArea = null;
	private Collection iSubjectAreas = null;
	private String iTable = null;
	private int iNrColumns;
	private int iNrRows;
	private Long iExamType;

	@Override
	public void validate(UniTimeAction action) {}

	@Override
	public void reset() {
		iOp = null;
		iShowSections = false;
		iTable = null;
		iNrRows = iNrColumns = 0;
		iExamType = null;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	
	public boolean getShowSections() { return iShowSections; }
	public void setShowSections(boolean showSections) { iShowSections = showSections; }
	
	public Long getSubjectArea() { return iSubjectArea; }
	public String getSubjectAreaAbbv() { return SubjectAreaDAO.getInstance().get(iSubjectArea).getSubjectAreaAbbreviation(); }
	public void setSubjectArea(Long subjectArea) { iSubjectArea = subjectArea; } 
	public Collection getSubjectAreas() { return iSubjectAreas; }
	public void setSubjectAreas(Collection subjectAreas) { iSubjectAreas = subjectAreas; }
	
	public void load(SessionContext session) {
	    setShowSections("1".equals(session.getUser().getProperty("ExamReport.showSections", "1")));
	    List<IdValue> subjects = new ArrayList<IdValue>();
        subjects.add(new IdValue(null, CONST.select()));
        if (session.hasPermission(Right.DepartmentIndependent)) {
        	subjects.add(new IdValue(-1l, CONST.all()));
        }
        TreeSet<SubjectArea> userSubjectAreas = SubjectArea.getUserSubjectAreas(session.getUser(), false);
        for (SubjectArea sa: userSubjectAreas)
        	subjects.add(new IdValue(sa.getUniqueId(), sa.getSubjectAreaAbbreviation()));
        setSubjectAreas(subjects);
	    setSubjectArea(session.getAttribute("ExamReport.subjectArea")==null?null:(Long)session.getAttribute("ExamReport.subjectArea"));
        if (userSubjectAreas.size() == 1) {
        	setSubjectArea(userSubjectAreas.first().getUniqueId());
        }
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

