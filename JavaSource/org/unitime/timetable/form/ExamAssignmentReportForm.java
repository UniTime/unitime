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
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.ComboBoxLookup;


/** 
 * @author Tomas Muller
 */
public class ExamAssignmentReportForm extends ExamReportForm {
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	private static final long serialVersionUID = -1263238076223090733L;
	private String iReport = null;
	public static enum ExamReport {
		ExamAssignmentReport,
		RoomAssignmentReport,
		Statistics,
		PeriodUsage,
		NrExamsADay,
		RoomSplits,
		ViolatedDistributions,
		DirectStudentConflicts,
		More2ADayStudentConflicts,
		BackToBackStudentConflicts,
		IndividualStudentConflicts,
		IndividualDirectStudentConflicts,
		IndividualBackToBackStudentConflicts,
		IndividualMore2ADayStudentConflicts,
		DirectInstructorConflicts,
		More2ADayInstructorConflicts,
		BackToBackInstructorConflicts,
		IndividualInstructorConflicts,
		IndividualDirectInstructorConflicts,
		IndividualBackToBackInstructorConflicts,
		IndividualMore2ADayInstructorConflicts,
		IndividualStudentSchedule,
		IndividualInstructorSchedule,
	}
    private String iFilter = null;
    private boolean iCanSeeAll = false;
    
	@Override
	public void reset() {
		super.reset();
		iReport = null; iCanSeeAll = false;
	}
	
	public String getReport() { return iReport; }
	public String getReportName() {
		try {
			return getReportName(ExamReport.valueOf(iReport));
		} catch (Exception e) {
			return iReport;
		}
	}
	public void setReport(String report) { iReport = report; }
	public String getReportName(ExamReport report) {
		switch (report) { 
		case ExamAssignmentReport: return MSG.reportExamAssignmentReport();
		case RoomAssignmentReport: return MSG.reportRoomAssignmentReport();
		case Statistics: return MSG.reportStatistics();
		case PeriodUsage: return MSG.reportPeriodUsage();
		case NrExamsADay: return MSG.reportNrExamsADay();
		case RoomSplits: return MSG.reportRoomSplits();
		case ViolatedDistributions: return MSG.reportViolatedDistributions();
		case DirectStudentConflicts: return MSG.reportDirectStudentConflicts();
		case More2ADayStudentConflicts: return MSG.reportMore2ADayStudentConflicts();
		case BackToBackStudentConflicts: return MSG.reportBackToBackStudentConflicts();
		case IndividualStudentConflicts: return MSG.reportIndividualStudentConflicts();
		case IndividualDirectStudentConflicts: return MSG.reportIndividualDirectStudentConflicts();
		case IndividualBackToBackStudentConflicts: return MSG.reportIndividualBackToBackStudentConflicts();
		case IndividualMore2ADayStudentConflicts: return MSG.reportIndividualMore2ADayStudentConflicts();
		case DirectInstructorConflicts: return MSG.reportDirectInstructorConflicts();
		case More2ADayInstructorConflicts: return MSG.reportMore2ADayInstructorConflicts();
		case BackToBackInstructorConflicts: return MSG.reportBackToBackInstructorConflicts();
		case IndividualInstructorConflicts: return MSG.reportIndividualInstructorConflicts();
		case IndividualDirectInstructorConflicts: return MSG.reportIndividualDirectInstructorConflicts();
		case IndividualBackToBackInstructorConflicts: return MSG.reportIndividualBackToBackInstructorConflicts();
		case IndividualMore2ADayInstructorConflicts: return MSG.reportIndividualMore2ADayInstructorConflicts();
		case IndividualStudentSchedule: return MSG.reportIndividualStudentSchedule();
		case IndividualInstructorSchedule: return MSG.reportIndividualInstructorSchedule();
		default: return report.name();
		}
	}
	
	public List<ComboBoxLookup> getReports() {
		List<ComboBoxLookup> ret = new ArrayList<ComboBoxLookup>();
		for (ExamReport r: ExamReport.values())
			ret.add(new ComboBoxLookup(getReportName(r), r.name()));
		return ret;
	}
	
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

