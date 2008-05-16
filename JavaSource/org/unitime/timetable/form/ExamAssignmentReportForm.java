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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;


/** 
 * @author Tomas Muller
 */
public class ExamAssignmentReportForm extends ExamReportForm {
    private String iReport = null; 
    public static final String sExamAssignmentReport = "Exam Assignment Report";
    public static final String sRoomAssignmentReport = "Room Assignment Report";
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
            sExamAssignmentReport, sRoomAssignmentReport, 
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
    
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
	    super.reset(mapping, request);
	    iReport = null;
	}
	
	public String getReport() { return iReport; }
	public void setReport(String report) { iReport = report; }
	public String[] getReports() { return sReports; }
	
	public String getFilter() { return iFilter; }
	public void setFilter(String filter) { iFilter = filter; }

    public void load(HttpSession session) {
        super.load(session);
        setFilter(session.getAttribute("ExamReport.Filter")==null?"":(String)session.getAttribute("ExamReport.Filter"));
        setReport(session.getAttribute("ExamReport.Report")==null?"":(String)session.getAttribute("ExamReport.Report"));
    }
        
    public void save(HttpSession session) {
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
}

