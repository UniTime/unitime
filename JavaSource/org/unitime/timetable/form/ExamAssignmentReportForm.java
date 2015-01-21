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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.security.SessionContext;


/** 
 * @author Tomas Muller
 */
public class ExamAssignmentReportForm extends ExamReportForm {
	private static final long serialVersionUID = -1263238076223090733L;
	private String iReport = null; 
    public static final String sExamAssignmentReport = "Exam Assignment Report";
    public static final String sRoomAssignmentReport = "Room Assignment Report";
    public static final String sStatistics = "Statistics";
    public static final String sPeriodUsage = "Period Usage";
    public static final String sNrExamsADay = "Number of Exams A Day";
    public static final String sRoomSplits = "Room Splits";
    public static final String sViolatedDistributions = "Violated Distribution Constraints";
    public static final String sDirectStudentConflicts = "Direct Student Conflicts";
    public static final String sMore2ADayStudentConflicts = "More Than 2 Exams A Day Student Conflicts";
    public static final String sBackToBackStudentConflicts = "Back-To-Back Student Conflicts";
    public static final String sIndividualStudentConflicts = "Individual Student Conflicts";
    public static final String sIndividualDirectStudentConflicts = "Individual Direct Student Conflicts";
    public static final String sIndividualBackToBackStudentConflicts = "Individual Back-To-Back Student Conflicts";
    public static final String sIndividualMore2ADayStudentConflicts = "Individual More Than 2 Exams A Day Student Conflicts";
    public static final String sDirectInstructorConflicts = "Direct Instructor Conflicts";
    public static final String sMore2ADayInstructorConflicts = "More Than 2 Exams A Day Instructor Conflicts";
    public static final String sBackToBackInstructorConflicts = "Back-To-Back Instructor Conflicts";
    public static final String sIndividualInstructorConflicts = "Individual Instructor Conflicts";
    public static final String sIndividualDirectInstructorConflicts = "Individual Direct Instructor Conflicts";
    public static final String sIndividualBackToBackInstructorConflicts = "Individual Back-To-Back Instructor Conflicts";
    public static final String sIndividualMore2ADayInstructorConflicts = "Individual More Than 2 Exams A Day Instructor Conflicts";
    public static final String sIndividualStudentSchedule = "Individual Student Schedule";
    public static final String sIndividualInstructorSchedule = "Individual Instructor Schedule";
    private static final String[] sReports = { 
            sExamAssignmentReport, sRoomAssignmentReport, sStatistics, 
            sPeriodUsage, sNrExamsADay, sRoomSplits, 
            sViolatedDistributions,
            sDirectStudentConflicts, sMore2ADayStudentConflicts, sBackToBackStudentConflicts, 
            sIndividualStudentSchedule,
            sIndividualStudentConflicts, sIndividualDirectStudentConflicts, sIndividualMore2ADayStudentConflicts, sIndividualBackToBackStudentConflicts,
            sDirectInstructorConflicts, sMore2ADayInstructorConflicts, sBackToBackInstructorConflicts, 
            sIndividualInstructorSchedule,
            sIndividualInstructorConflicts, sIndividualDirectInstructorConflicts, sIndividualBackToBackInstructorConflicts, sIndividualMore2ADayInstructorConflicts
            };
    private String iFilter = null;
    private boolean iCanSeeAll = false;
    
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
	    super.reset(mapping, request);
	    iReport = null; iCanSeeAll = false;
	}
	
	public String getReport() { return iReport; }
	public void setReport(String report) { iReport = report; }
	public String[] getReports() { return sReports; }
	
	public String getFilter() { return iFilter; }
	public void setFilter(String filter) { iFilter = filter; }

    public void load(SessionContext session) {
        super.load(session);
        setFilter(session.getAttribute("ExamReport.Filter")==null?"":(String)session.getAttribute("ExamReport.Filter"));
        setReport(session.getAttribute("ExamReport.Report")==null?"":(String)session.getAttribute("ExamReport.Report"));
    }
        
    public void save(SessionContext session) {
        super.save(session);
        if (getFilter()==null)
            session.removeAttribute("ExamReport.Filter");
        else
            session.setAttribute("ExamReport.Filter",getFilter());
        if (getReport()==null)
            session.removeAttribute("ExamReport.Report");
        else
            session.setAttribute("ExamReport.Report",getReport());
    }

    public boolean getCanSeeAll() { return iCanSeeAll; }
    public void setCanSeeAll(boolean seeAll) { iCanSeeAll = seeAll; }
}

