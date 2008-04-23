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

import java.util.Hashtable;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.reports.exam.AbbvExamScheduleByCourseReport;
import org.unitime.timetable.reports.exam.AbbvScheduleByCourseReport;
import org.unitime.timetable.reports.exam.ConflictsByCourseAndInstructorReport;
import org.unitime.timetable.reports.exam.ConflictsByCourseAndStudentReport;
import org.unitime.timetable.reports.exam.ExamScheduleByPeriodReport;
import org.unitime.timetable.reports.exam.ExamVerificationReport;
import org.unitime.timetable.reports.exam.PeriodChartReport;
import org.unitime.timetable.reports.exam.ScheduleByCourseReport;
import org.unitime.timetable.reports.exam.ScheduleByPeriodReport;
import org.unitime.timetable.reports.exam.ScheduleByRoomReport;

/*
 * @author Tomas Muller
 */
public class ExamPdfReportForm extends ExamReportForm {
    private String[] iReports = null; 
    private String iMode = null;
    private boolean iAll = false;
    private String[] iSubjects = null;
    
    private boolean iDispRooms = true;
    private String iNoRoom = "";
    private boolean iDirect = true;
    private boolean iM2d = true;
    private boolean iBtb = false;
    private String iLimit = null;
    private boolean iTotals = false;
    private String iRoomCodes = null;
    
    public static Hashtable<String,Class> sRegisteredReports = new Hashtable();
    public static String[] sModes = {"PDF (Letter)", "PDF (Ledger)", "Text"};
    
    static {
        sRegisteredReports.put("Schedule by Course", ScheduleByCourseReport.class);
        sRegisteredReports.put("Student Conflicts", ConflictsByCourseAndStudentReport.class);
        sRegisteredReports.put("Instuctor Conflicts", ConflictsByCourseAndInstructorReport.class);
        sRegisteredReports.put("Schedule by Period", ScheduleByPeriodReport.class);
        sRegisteredReports.put("Schedule by Period (Exams)", ExamScheduleByPeriodReport.class);
        sRegisteredReports.put("Schedule by Room", ScheduleByRoomReport.class);
        sRegisteredReports.put("Period Chart", PeriodChartReport.class);
        sRegisteredReports.put("Verification", ExamVerificationReport.class);
        sRegisteredReports.put("Abbreviated Schedule", AbbvScheduleByCourseReport.class);
        sRegisteredReports.put("Abbreviated Schedule (Exams)", AbbvExamScheduleByCourseReport.class);
    }
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        
        if (iReports==null || iReports.length==0)
            errors.add("reports", new ActionMessage("errors.generic", "No report selected."));
        
        if (!iAll && (iSubjects==null || iSubjects.length==0))
            errors.add("subjects", new ActionMessage("errors.generic", "No subject area selected."));
        
        return errors;
    }

    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        iReports = null;
        iMode = sModes[0];
        iAll = false;
        iDispRooms = false;
        iNoRoom = null;
        iDirect = false;
        iM2d = false;
        iBtb = false;
        iLimit = null;
        iTotals = false;
        iRoomCodes = null;
    }
    
    public void setDefaults() {
        iAll = true;
        iDispRooms = true;
        iNoRoom = ApplicationProperties.getProperty("tmtbl.exam.report.noroom");
        iDirect = true;
        iM2d = (getExamType()==Exam.sExamTypeFinal?true:false);
        iTotals = (getExamType()==Exam.sExamTypeFinal?true:false);
        iRoomCodes = ApplicationProperties.getProperty("tmtbl.exam.report.roomcode");
    }

    public String[] getReports() { return iReports;}
    public void setReports(String[] reports) { iReports = reports;}
    public String getMode() { return iMode; }
    public void setMode(String mode) { iMode = mode; }
    public int getModeIdx() {
        for (int i=0;i<sModes.length;i++)
            if (sModes[i].equals(iMode)) return i;
        return 0;
    }
    public boolean getAll() { return iAll; }
    public void setAll(boolean all) { iAll = all;}
    public String[] getSubjects() { return iSubjects; }
    public void setSubjects(String[] subjects) { iSubjects = subjects; }
    public boolean getDispRooms() { return iDispRooms; }
    public void setDispRooms(boolean dispRooms) { iDispRooms = dispRooms; }
    public String getNoRoom() { return iNoRoom; }
    public void setNoRoom(String noRoom) { iNoRoom = noRoom; }
    public boolean getBtb() { return iBtb; }
    public void setBtb(boolean btb) { iBtb = btb; }
    public boolean getM2d() { return iM2d; }
    public void setM2d(boolean m2d) { iM2d = m2d; }
    public boolean getDirect() { return iDirect; }
    public void setDirect(boolean direct) { iDirect = direct; }
    public String getLimit() { return iLimit; }
    public void setLimit(String limit) { iLimit = limit; }
    public boolean getTotals() { return iTotals; }
    public void setTotals(boolean totals) { iTotals = totals; }
    public String getRoomCodes() { return iRoomCodes; }
    public void setRoomCodes(String roomCodes) { iRoomCodes = roomCodes; }
    
    public TreeSet<String> getAllReports() {
        return new TreeSet<String>(sRegisteredReports.keySet());
    }
    public String[] getModes() { return sModes; }
    
    
}
