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
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.reports.exam.AbbvExamScheduleByCourseReport;
import org.unitime.timetable.reports.exam.AbbvScheduleByCourseReport;
import org.unitime.timetable.reports.exam.ConflictsByCourseAndInstructorReport;
import org.unitime.timetable.reports.exam.ConflictsByCourseAndStudentReport;
import org.unitime.timetable.reports.exam.ExamPeriodChartReport;
import org.unitime.timetable.reports.exam.ExamScheduleByPeriodReport;
import org.unitime.timetable.reports.exam.ExamVerificationReport;
import org.unitime.timetable.reports.exam.InstructorExamReport;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.reports.exam.PeriodChartReport;
import org.unitime.timetable.reports.exam.ScheduleByCourseReport;
import org.unitime.timetable.reports.exam.ScheduleByPeriodReport;
import org.unitime.timetable.reports.exam.ScheduleByRoomReport;
import org.unitime.timetable.reports.exam.StudentExamReport;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.IdValue;

/*
 * @author Tomas Muller
 */
public class ExamPdfReportForm extends ExamReportForm {
	private static final long serialVersionUID = 4349609058043519671L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	protected static Log sLog = LogFactory.getLog(ExamPdfReportForm.class);
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
    private boolean iDispLimit = true;
    private String iRoomCodes = null;
    private boolean iEmail = false;
    private String iAddr, iCc, iBcc = null;
    private boolean iEmailDeputies = false;
    private boolean iItype = false;
    private String iMessage = null;
    private String iSubject = null;
    private String iSince = null;
    private boolean iEmailInstructors, iEmailStudents;
    private boolean iClassSchedule = false;
    private boolean iIgnoreEmptyExams = false;
    private boolean iDispNote = true;
    private boolean iCompact = false;
    private boolean iRoomDispNames = false;
    
    public static String getModeLabel(PdfLegacyExamReport.Mode m) {
    	switch (m) {
    	case LegacyPdfLetter: return MSG.formatPdfLetter();
    	case LegacyPdfLedger: return MSG.formatPdfLedger();
    	case LegacyText: return MSG.formatText();
    	case CSV: MSG.formatCSV();
    	case PDF: return MSG.formatPdfNew();
    	case XLS: return MSG.formatXLS();
    	default: return m.name();
		}
    }
    
    public static int sDeliveryDownload = 0;
    public static int sDeliveryEmail = 1;
    
    public static enum RegisteredReport {
        AbbvScheduleByCourseReport(AbbvScheduleByCourseReport.class),
        AbbvExamScheduleByCourseReport(AbbvExamScheduleByCourseReport.class),
        InstructorExamReport(InstructorExamReport.class),
        StudentExamReport(StudentExamReport.class),
        ConflictsByCourseAndInstructorReport(ConflictsByCourseAndInstructorReport.class),
        PeriodChartReport(PeriodChartReport.class),
        ExamPeriodChartReport(ExamPeriodChartReport.class),
    	ScheduleByCourseReport(ScheduleByCourseReport.class),
        ScheduleByPeriodReport(ScheduleByPeriodReport.class),
        ExamScheduleByPeriodReport(ExamScheduleByPeriodReport.class),
        ScheduleByRoomReport(ScheduleByRoomReport.class),
        ConflictsByCourseAndStudentReport(ConflictsByCourseAndStudentReport.class),
        ExamVerificationReport(ExamVerificationReport.class),
    	;
    	
    	private Class<? extends PdfLegacyExamReport> implementation;
    	RegisteredReport(Class<? extends PdfLegacyExamReport> implementation) {
    		this.implementation = implementation;
    	}
    	public Class<? extends PdfLegacyExamReport> getImplementation() { return implementation; }
    }
    
    public String getReportName(RegisteredReport report) {
    	switch (report) {
        case ScheduleByCourseReport: return MSG.reportScheduleByCourseReport();
        case ConflictsByCourseAndStudentReport: return MSG.reportConflictsByCourseAndStudentReport();
        case ConflictsByCourseAndInstructorReport: return MSG.reportConflictsByCourseAndInstructorReport();
        case ScheduleByPeriodReport: return MSG.reportScheduleByPeriodReport();
        case ExamScheduleByPeriodReport: return MSG.reportExamScheduleByPeriodReport();
        case ScheduleByRoomReport: return MSG.reportScheduleByRoomReport();
        case PeriodChartReport: return MSG.reportPeriodChartReport();
        case ExamPeriodChartReport: return MSG.reportExamPeriodChartReport();
        case ExamVerificationReport: return MSG.reportExamVerificationReport();
        case AbbvScheduleByCourseReport: return MSG.reportAbbvScheduleByCourseReport();
        case AbbvExamScheduleByCourseReport: return MSG.reportAbbvExamScheduleByCourseReport();
        case InstructorExamReport: return MSG.reportInstructorExamReport();
        case StudentExamReport: return MSG.reportStudentExamReport();    	
		default: return report.name();
		}
    }
    
    public String getReportName(String report) {
    	try {
    		return getReportName(RegisteredReport.valueOf(report));
    	} catch (Exception e) {
    		return report;
    	}
    }

    @Override
    public void validate(UniTimeAction action) {
    	if (iReports==null || iReports.length==0)
    		action.addFieldError("reports", MSG.errorNoReportSelected());
        
        if (!iAll && (iSubjects==null || iSubjects.length==0))
        	action.addFieldError("subjects", MSG.errorNoSubjectAreaSelected());
        
        if (iSince != null && !iSince.isEmpty() && !Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT).isValid(iSince))
        	action.addFieldError("since", MSG.errorNotValidDate(iSince));
    }

    
    @Override
	public void reset() {
    	super.reset();
        iReports = null;
        iMode = PdfLegacyExamReport.Mode.LegacyPdfLetter.name();
        iAll = false;
        iDispRooms = false;
        iNoRoom = null;
        iDirect = false;
        iM2d = false;
        iBtb = false;
        iLimit = null;
        iTotals = false;
        iRoomCodes = null;
        iEmail = false;
        iAddr = null; iCc = null; iBcc = null; 
        iEmailDeputies = false;
        iSubject = MSG.emailSubjectExaminationReport();
        iMessage = null;
        iDispLimit = false;
        iSince = null;
        iEmailInstructors = false; 
        iEmailStudents = false;
        iClassSchedule = false;
        iIgnoreEmptyExams = false;
        iItype = false;
        iDispNote = false;
        iCompact = false;
        iRoomDispNames = false;
    }
    
    public void load(SessionContext session) {
    	super.load(session);
        setAll(session.getAttribute("ExamPdfReport.all")==null?true:(Boolean)session.getAttribute("ExamPdfReport.all"));
        setReports((String[])session.getAttribute("ExamPdfReport.reports"));
        setMode(session.getAttribute("ExamPdfReport.mode")==null?PdfLegacyExamReport.Mode.LegacyPdfLetter.name():(String)session.getAttribute("ExamPdfReport.mode"));
        setSubjects((String[])session.getAttribute("ExamPdfReport.subjects"));
        setDispRooms("1".equals(session.getUser().getProperty("ExamPdfReport.dispRooms", "1")));
        setNoRoom(session.getUser().getProperty("ExamPdfReport.noRoom", ApplicationProperty.ExaminationsNoRoomText.value()));
        setDirect("1".equals(session.getUser().getProperty("ExamPdfReport.direct", "1")));
        setM2d("1".equals(session.getUser().getProperty("ExamPdfReport.m2d", "1")));
        setBtb("1".equals(session.getUser().getProperty("ExamPdfReport.btb", "0")));
        setLimit(session.getUser().getProperty( "ExamPdfReport.limit"));
        setTotals("1".equals(session.getUser().getProperty("ExamPdfReport.totals","1")));
        setRoomCodes(session.getUser().getProperty("ExamPdfReport.roomCodes", ApplicationProperty.ExaminationRoomCode.value()));
        setEmail("1".equals(session.getUser().getProperty( "ExamPdfReport.email", "0")));
        setAddress(session.getUser().getProperty("ExamPdfReport.addr"));
        setCc(session.getUser().getProperty("ExamPdfReport.cc"));
        setBcc(session.getUser().getProperty("ExamPdfReport.bcc"));
        setEmailDeputies("1".equals(session.getUser().getProperty("ExamPdfReport.emailDeputies", "0")));
        setMessage(session.getUser().getProperty("ExamPdfReport.message"));
        setSubject(session.getUser().getProperty("ExamPdfReport.subject","Examination Report"));
        setDispLimit("1".equals(session.getUser().getProperty("ExamPdfReport.dispLimit", "1")));
        setSince(session.getUser().getProperty("ExamPdfReport.since"));
        setEmailInstructors("1".equals(session.getUser().getProperty("ExamPdfReport.emailInstructors", "0")));
        setEmailStudents("1".equals(session.getUser().getProperty("ExamPdfReport.emailStudents", "0")));
        setItype("1".equals(session.getUser().getProperty("ExamPdfReport.itype", ApplicationProperty.ExaminationReportsShowInstructionalType.isTrue() ? "1" : "0")));
        setClassSchedule("1".equals(session.getUser().getProperty("ExamPdfReport.cschedule", "1")));
        setIgnoreEmptyExams("1".equals(session.getUser().getProperty("ExamPdfReport.ignempty", "1")));
        setDispNote("1".equals(session.getUser().getProperty("ExamPdfReport.dispNote", "0")));
        setCompact("1".equals(session.getUser().getProperty("ExamPdfReport.compact", "0")));
        setRoomDispNames("1".equals(session.getUser().getProperty("ExamPdfReport.roomDispNames", "1")));
        List<IdValue> subjects = new ArrayList<IdValue>();
        TreeSet<SubjectArea> userSubjectAreas = SubjectArea.getUserSubjectAreas(session.getUser(), false);
        for (SubjectArea sa: userSubjectAreas)
        	subjects.add(new IdValue(sa.getUniqueId(), sa.getSubjectAreaAbbreviation()));
        setSubjectAreas(subjects);
    }
    
    public void save(SessionContext session) {
        super.save(session);
        session.setAttribute("ExamPdfReport.reports", getReports());
        session.setAttribute("ExamPdfReport.mode", getMode());
        session.setAttribute("ExamPdfReport.all", getAll());
        session.setAttribute("ExamPdfReport.subjects", getSubjects());
        session.getUser().setProperty("ExamPdfReport.dispRooms", getDispRooms() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.noRoom", getNoRoom());
        session.getUser().setProperty("ExamPdfReport.direct",getDirect() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.m2d",getM2d() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.btb",getBtb() ? "1" : "0");
        session.getUser().setProperty( "ExamPdfReport.limit", getLimit());
        session.getUser().setProperty("ExamPdfReport.totals",getTotals() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.roomCodes", getRoomCodes());
        session.getUser().setProperty( "ExamPdfReport.email", getEmail() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.addr", getAddress());
        session.getUser().setProperty("ExamPdfReport.cc", getCc());
        session.getUser().setProperty("ExamPdfReport.bcc", getBcc());
        session.getUser().setProperty("ExamPdfReport.emailDeputies", getEmailDeputies() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.message", getMessage());
        session.getUser().setProperty("ExamPdfReport.subject", getSubject());
        session.getUser().setProperty("ExamPdfReport.dispLimit", getDispLimit() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.since", getSince());
        session.getUser().setProperty("ExamPdfReport.emailInstructors", getEmailInstructors() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.emailStudents", getEmailStudents() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.itype", getItype() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.cschedule", getClassSchedule() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.ignempty", getIgnoreEmptyExams() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.dispNote", getDispNote() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.compact", getCompact() ? "1" : "0");
        session.getUser().setProperty("ExamPdfReport.roomDispNames", getRoomDispNames() ? "1" : "0");
    }

    public String[] getReports() { return iReports; }
    public void setReports(String[] reports) { iReports = reports;}
    public String getMode() { return iMode; }
    public void setMode(String mode) { iMode = mode; }
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
    public boolean getEmail() { return iEmail; }
    public void setEmail(boolean email) { iEmail = email; }
    public boolean getEmailDeputies() { return iEmailDeputies; }
    public void setEmailDeputies(boolean emailDeputies) { iEmailDeputies = emailDeputies; }
    public String getAddress() { return iAddr; }
    public void setAddress(String addr) { iAddr = addr; }
    public String getCc() { return iCc; }
    public void setCc(String cc) { iCc = cc; }
    public String getBcc() { return iBcc; }
    public void setBcc(String bcc) { iBcc = bcc; }
    public boolean getCanEmail() { return true; }
    public String getMessage() { return iMessage; }
    public void setMessage(String message) { iMessage = message; }
    public String getSubject() { return iSubject; }
    public void setSubject(String subject) { iSubject = subject; }
    public boolean getDispNote() { return iDispNote; }
    public void setDispNote(boolean dispNote) { iDispNote = dispNote; }
    public boolean getCompact() { return iCompact; }
    public void setCompact(boolean compact) { iCompact = compact; }
    public boolean getRoomDispNames() { return iRoomDispNames; }
    public void setRoomDispNames(boolean roomDispNames) { iRoomDispNames = roomDispNames; }
    
    public List<ComboBoxLookup> getAllReports() {
    	List<ComboBoxLookup> ret = new ArrayList<ComboBoxLookup>();
    	for (RegisteredReport r: RegisteredReport.values())
    		ret.add(new ComboBoxLookup(getReportName(r), r.name()));
    	return ret;
    }
    public List<ComboBoxLookup> getModes() { 
    	List<ComboBoxLookup> ret = new ArrayList<ComboBoxLookup>();
    	for (PdfLegacyExamReport.Mode m: PdfLegacyExamReport.Mode.values())
    		ret.add(new ComboBoxLookup(getModeLabel(m), m.name()));
    	return ret;
    }
    
    public boolean getDispLimit() { return iDispLimit; }
    public void setDispLimit(boolean dispLimit) { iDispLimit = dispLimit; }
    public String getSince() { return iSince; }
    public void setSince(String since) { iSince = since; }
    public boolean getEmailInstructors() { return iEmailInstructors; }
    public void setEmailInstructors(boolean emailInstructors) { iEmailInstructors = emailInstructors; }
    public boolean getEmailStudents() { return iEmailStudents; }
    public void setEmailStudents(boolean emailStudents) { iEmailStudents = emailStudents; }
    public boolean getItype() { return iItype; }
    public void setItype(boolean itype) { iItype = itype; }
    public boolean getClassSchedule() { return iClassSchedule; }
    public void setClassSchedule(boolean classSchedule) { iClassSchedule = classSchedule; }
    public boolean getIgnoreEmptyExams() { return iIgnoreEmptyExams; }
    public void setIgnoreEmptyExams(boolean ignoreEmptyExams) { iIgnoreEmptyExams = ignoreEmptyExams; }
    
    public Object clone() {
    	ExamPdfReportForm x = new ExamPdfReportForm();
        x.setAll(getAll());
        x.setReports(getReports());
        x.setMode(getMode());
        x.setSubjects(getSubjects());
        x.setDispRooms(getDispRooms());
        x.setNoRoom(getNoRoom());
        x.setDirect(getDirect());
        x.setM2d(getM2d());
        x.setBtb(getBtb());
        x.setLimit(getLimit());
        x.setTotals(getTotals());
        x.setRoomCodes(getRoomCodes());
        x.setEmail(getEmail());
        x.setAddress(getAddress());
        x.setCc(getCc());
        x.setBcc(getBcc());
        x.setEmailDeputies(getEmailDeputies());
        x.setMessage(getMessage());
        x.setSubject(getSubject());
        x.setDispLimit(getDispLimit());
        x.setSince(getSince());
        x.setEmailInstructors(getEmailInstructors());
        x.setEmailStudents(getEmailStudents());
        x.setItype(getItype());
        x.setClassSchedule(getClassSchedule());
        x.setIgnoreEmptyExams(getIgnoreEmptyExams());
	    x.setShowSections(getShowSections());
	    x.setSubjectArea(getSubjectArea());
	    x.setExamType(getExamType());
	    x.setDispNote(getDispNote());
	    x.setCompact(getCompact());
	    x.setRoomDispNames(getRoomDispNames());
    	return x;
    }
    
    public PdfLegacyExamReport.Mode getReportMode() {
    	try {
    		return PdfLegacyExamReport.Mode.valueOf(getMode());
    	} catch (Exception e) {
    		return PdfLegacyExamReport.Mode.LegacyPdfLetter;
    	}
    }
}
