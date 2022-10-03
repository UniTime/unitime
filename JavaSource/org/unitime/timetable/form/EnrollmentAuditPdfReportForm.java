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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.reports.enrollment.EnrollmentsViolatingCourseStructureAuditReport;
import org.unitime.timetable.reports.enrollment.MissingCourseEnrollmentsAuditReport;
import org.unitime.timetable.reports.enrollment.MultipleConfigEnrollmentsAuditReport;
import org.unitime.timetable.reports.enrollment.MultipleCourseEnrollmentsAuditReport;
import org.unitime.timetable.reports.enrollment.PdfEnrollmentAuditReport;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.ComboBoxLookup;

/*
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
public class EnrollmentAuditPdfReportForm implements UniTimeForm {
	private static final long serialVersionUID = -4537752846296511516L;
	protected static Log sLog = LogFactory.getLog(EnrollmentAuditPdfReportForm.class);
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);

    private String iOp = null;
	private Long iSubjectArea = null;
	private Collection iSubjectAreas = null;

	private String[] iReports = null; 
    private String iMode = null;
    private boolean iAll = false;
    private String[] iSubjects = null;
    
    private boolean iExternalId = false;
    private boolean iStudentName = false;
    private boolean iEmail = false;
    private String iAddr, iCc, iBcc = null;
    private String iReport = null;
    private String iMessage = null;
    private String iSubject = null;
    
    public static enum RegisteredReport {
    	EnrollmentsViolatingCourseStructureAuditReport(EnrollmentsViolatingCourseStructureAuditReport.class),
    	MissingCourseEnrollmentsAuditReport(MissingCourseEnrollmentsAuditReport.class),
    	MultipleCourseEnrollmentsAuditReport(MultipleCourseEnrollmentsAuditReport.class),
    	MultipleConfigEnrollmentsAuditReport(MultipleConfigEnrollmentsAuditReport.class),
    	;
    	
    	private Class<? extends PdfEnrollmentAuditReport> implementation;
    	RegisteredReport(Class<? extends PdfEnrollmentAuditReport> implementation) {
    		this.implementation = implementation;
    	}
    	public Class<? extends PdfEnrollmentAuditReport> getImplementation() { return implementation; }
    }
    
    public String getReportName(RegisteredReport report) {
    	switch (report) {
    	case EnrollmentsViolatingCourseStructureAuditReport: 
    		return MSG.reportEnrollmentsViolatingCourseStructureAudit();
    	case MissingCourseEnrollmentsAuditReport:
    		return MSG.reportMissingCourseEnrollmentsAudit();
    	case MultipleCourseEnrollmentsAuditReport:
    		return MSG.reportMultipleCourseEnrollmentsAudit();
    	case MultipleConfigEnrollmentsAuditReport:
    		return MSG.reportMultipleConfigEnrollmentsAudit();
    	default:
    		return report.name();
    	}
    }
    
    @Override
    public void validate(UniTimeAction action) {
        if (iReports==null || iReports.length==0)
            action.addFieldError("reports", MSG.errorNoReportSelected());
        if (!iAll && (iSubjects==null || iSubjects.length==0))
            action.addFieldError("reports", MSG.errorNoSubjectAreaSelected());
    }

    @Override
    public void reset() {
    	iOp = null;
        iReports = null;
        iMode = getModes().get(0).getValue();
        iAll = false;
        iEmail = false;
        iExternalId = false;
        iStudentName = false;
        iAddr = null; iCc = null; iBcc = null; 
        iSubject = MSG.subjectEnrollmentAuditReport();
        iMessage = null;
        iReport = null;
    }
    
    public void load(SessionContext context) {
 	    setSubjectArea(context.getAttribute("EnrollmentAuditPdfReport.subjectArea")==null?null:(Long)context.getAttribute("EnrollmentAuditPdfReport.subjectArea"));
	    try {
	        iSubjectAreas = new TreeSet(
	                new SubjectAreaDAO().getSession().createQuery(
	                        "select distinct co.subjectArea from CourseOffering co where "+
	                        "co.subjectArea.session.uniqueId=:sessionId")
	                        .setLong("sessionId", context.getUser().getCurrentAcademicSessionId())
	                        .setCacheable(true).list());
	    } catch (Exception e) {}

        setAll(context.getAttribute("EnrollmentAuditPdfReport.all")==null ? true : (Boolean)context.getAttribute("EnrollmentAuditPdfReport.all"));
        setReports((String[])context.getAttribute("EnrollmentAuditPdfReport.reports"));
        setMode(context.getAttribute("EnrollmentAuditPdfReport.mode") == null ? getModes().get(0).getValue() : (String)context.getAttribute("EnrollmentAuditPdfReport.mode"));
        setSubjects((String[])context.getAttribute("EnrollmentAuditPdfReport.subjects"));
        setExternalId("1".equals(context.getUser().getProperty("EnrollmentAuditPdfReport.externalId", "0")));
        setStudentName("1".equals(context.getUser().getProperty( "EnrollmentAuditPdfReport.studentName", "0")));
        setEmail("1".equals(context.getUser().getProperty("EnrollmentAuditPdfReport.email", "0")));
        setAddress(context.getUser().getProperty("EnrollmentAuditPdfReport.addr", context.getUser().getEmail()));
        setCc(context.getUser().getProperty("EnrollmentAuditPdfReport.cc"));
        setBcc(context.getUser().getProperty("EnrollmentAuditPdfReport.bcc"));
        setMessage(context.getUser().getProperty("EnrollmentAuditPdfReport.message"));
        setSubject(context.getUser().getProperty("EnrollmentAuditPdfReport.subject","Enrollment Audit"));
    }
    
    public void save(SessionContext context) {
    	context.setAttribute("EnrollmentAuditPdfReport.reports.subjectArea", getSubjectArea());
    	context.setAttribute("EnrollmentAuditPdfReport.reports", getReports());
    	context.setAttribute("EnrollmentAuditPdfReport.mode", getMode());
    	context.setAttribute("EnrollmentAuditPdfReport.all", getAll());
    	context.setAttribute("EnrollmentAuditPdfReport.subjects", getSubjects());
        context.getUser().setProperty("EnrollmentAuditPdfReport.externalId", getExternalId() ? "1" : "0");
        context.getUser().setProperty("EnrollmentAuditPdfReport.studentName", getStudentName() ? "1" : "0");
        context.getUser().setProperty("EnrollmentAuditPdfReport.email", getEmail() ? "1" : "0");
        context.getUser().setProperty("EnrollmentAuditPdfReport.addr", getAddress());
        context.getUser().setProperty("EnrollmentAuditPdfReport.cc", getCc());
        context.getUser().setProperty("EnrollmentAuditPdfReport.bcc", getBcc());
        context.getUser().setProperty("EnrollmentAuditPdfReport.message", getMessage());
        context.getUser().setProperty("EnrollmentAuditPdfReport.subject", getSubject());
    }

    public String[] getReports() { return iReports;}
    public void setReports(String[] reports) { iReports = reports;}
    public String getMode() { return iMode; }
    public void setMode(String mode) { iMode = mode; }
    public boolean getAll() { return iAll; }
    public void setAll(boolean all) { iAll = all;}
    public String[] getSubjects() { return iSubjects; }
    public void setSubjects(String[] subjects) { iSubjects = subjects; }
    public boolean getEmail() { return iEmail; }
    public void setEmail(boolean email) { iEmail = email; }
    public String getAddress() { return iAddr; }
    public void setAddress(String addr) { iAddr = addr; }
    public String getCc() { return iCc; }
    public void setCc(String cc) { iCc = cc; }
    public String getBcc() { return iBcc; }
    public void setBcc(String bcc) { iBcc = bcc; }
    public boolean getCanEmail() { return true; }
    public String getReport() { return iReport; }
    public void setReport(String report) { iReport = report; }
    public void log(String message) {
        sLog.info(message);
        iReport += message+"<br>"; 
    }
    public String getMessage() { return iMessage; }
    public void setMessage(String message) { iMessage = message; }
    public String getSubject() { return iSubject; }
    public void setSubject(String subject) { iSubject = subject; }
    
    public List<ComboBoxLookup> getAllReports() {
    	List<ComboBoxLookup> ret = new ArrayList<ComboBoxLookup>();
    	for (RegisteredReport r: RegisteredReport.values())
    		ret.add(new ComboBoxLookup(getReportName(r), r.name()));
    	return ret;
    }
    public List<ComboBoxLookup> getModes() { 
    	List<ComboBoxLookup> ret = new ArrayList<ComboBoxLookup>();
    	for (PdfLegacyExamReport.Mode m: PdfLegacyExamReport.Mode.values())
    		ret.add(new ComboBoxLookup(ExamPdfReportForm.getModeLabel(m), m.name()));
    	return ret;
    }
    public PdfLegacyExamReport.Mode getReportMode() {
    	try {
    		return PdfLegacyExamReport.Mode.valueOf(getMode());
    	} catch (Exception e) {
    		return PdfLegacyExamReport.Mode.LegacyPdfLetter;
    	}
    }

	/**
	 * @return the iOp
	 */
	public String getOp() {
		return iOp;
	}


	/**
	 * @param iOp the iOp to set
	 */
	public void setOp(String op) {
		this.iOp = op;
	}


	/**
	 * @return the iSubjectArea
	 */
	public Long getSubjectArea() {
		return iSubjectArea;
	}


	/**
	 * @param iSubjectArea the iSubjectArea to set
	 */
	public void setSubjectArea(Long subjectArea) {
		this.iSubjectArea = subjectArea;
	}


	/**
	 * @return the iSubjectAreas
	 */
	public Collection getSubjectAreas() {
		return iSubjectAreas;
	}


	/**
	 * @param iSubjectAreas the iSubjectAreas to set
	 */
	public void setSubjectAreas(Collection subjectAreas) {
		this.iSubjectAreas = subjectAreas;
	}


	/**
	 * @return the externalId
	 */
	public boolean getExternalId() {
		return iExternalId;
	}


	/**
	 * @param externalId the externalId to set
	 */
	public void setExternalId(boolean externalId) {
		this.iExternalId = externalId;
	}


	/**
	 * @return the studentName
	 */
	public boolean getStudentName() {
		return iStudentName;
	}


	/**
	 * @param studentName the studentName to set
	 */
	public void setStudentName(boolean studentName) {
		this.iStudentName = studentName;
	}
    
}
