/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
import java.util.Hashtable;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.Email;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.reports.enrollment.EnrollmentsViolatingCourseStructureAuditReport;
import org.unitime.timetable.reports.enrollment.MissingCourseEnrollmentsAuditReport;
import org.unitime.timetable.reports.enrollment.MultipleCourseEnrollmentsAuditReport;

/*
 * @author Stephanie Schluttenhofer
 */
public class EnrollmentAuditPdfReportForm extends ActionForm {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4537752846296511516L;

	protected static Logger sLog = Logger.getLogger(EnrollmentAuditPdfReportForm.class);

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
    
    public static Hashtable<String,Class> sRegisteredReports = new Hashtable<String, Class>();
    public static String[] sModes = {"PDF (Letter)", "PDF (Ledger)", "Text"};
    public static int sDeliveryDownload = 0;
    public static int sDeliveryEmail = 1;
    
    static {
        sRegisteredReports.put("Enrollments Violating Course Structure", EnrollmentsViolatingCourseStructureAuditReport.class);
        sRegisteredReports.put("Missing Course Enrollments", MissingCourseEnrollmentsAuditReport.class);
        sRegisteredReports.put("Multiple Course Enrollments", MultipleCourseEnrollmentsAuditReport.class);
    }
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (iReports==null || iReports.length==0)
            errors.add("reports", new ActionMessage("errors.generic", "No report selected."));
        
        if (!iAll && (iSubjects==null || iSubjects.length==0))
            errors.add("subjects", new ActionMessage("errors.generic", "No subject area selected."));
        
        return errors;
    }

    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
    	iOp = null;
        iReports = null;
        iMode = sModes[0];
        iAll = false;
        iEmail = false;
        iExternalId = false;
        iStudentName = false;
        iAddr = null; iCc = null; iBcc = null; 
        iSubject = "Enrollment Audit Report";
        iMessage = null;
        iReport = null;
        if (getAddress()==null) {
            TimetableManager manager = TimetableManager.getManager(Web.getUser(request.getSession()));
            if (manager!=null && manager.getEmailAddress()!=null) setAddress(manager.getEmailAddress());
        }
    }
    
    public void load(HttpSession session) {
 	    setSubjectArea(session.getAttribute("EnrollmentAuditPdfReport.subjectArea")==null?null:(Long)session.getAttribute("EnrollmentAuditPdfReport.subjectArea"));
	    try {
	        iSubjectAreas = new TreeSet(
	                new SubjectAreaDAO().getSession().createQuery(
	                        "select distinct co.subjectArea from CourseOffering co where "+
	                        "co.subjectArea.session.uniqueId=:sessionId")
	                        .setLong("sessionId", Session.getCurrentAcadSession(Web.getUser(session)).getUniqueId())
	                        .setCacheable(true).list());
	    } catch (Exception e) {}

        setAll(session.getAttribute("EnrollmentAuditPdfReport.all")==null?true:(Boolean)session.getAttribute("EnrollmentAuditPdfReport.all"));
        setReports((String[])session.getAttribute("EnrollmentAuditPdfReport.reports"));
        setMode(session.getAttribute("EnrollmentAuditPdfReport.mode")==null?sModes[0]:(String)session.getAttribute("EnrollmentAuditPdfReport.mode"));
        setSubjects((String[])session.getAttribute("EnrollmentAuditPdfReport.subjects"));
        setExternalId(UserData.getPropertyBoolean(session, "EnrollmentAuditPdfReport.externalId", false));
        setStudentName(UserData.getPropertyBoolean(session, "EnrollmentAuditPdfReport.studentName", false));
        setEmail(UserData.getPropertyBoolean(session, "EnrollmentAuditPdfReport.email", false));
        setAddress(UserData.getProperty(session,"EnrollmentAuditPdfReport.addr"));
        setCc(UserData.getProperty(session,"EnrollmentAuditPdfReport.cc"));
        setBcc(UserData.getProperty(session,"EnrollmentAuditPdfReport.bcc"));
        setMessage(UserData.getProperty(session,"EnrollmentAuditPdfReport.message"));
        setSubject(UserData.getProperty(session,"EnrollmentAuditPdfReport.subject","Examination Report"));
    }
    
    public void save(HttpSession session) {
        if (getSubjectArea()==null)
            session.removeAttribute("EnrollmentAuditPdfReport.reports.subjectArea");
        else
            session.setAttribute("EnrollmentAuditPdfReport.reports.subjectArea", getSubjectArea());
        session.setAttribute("EnrollmentAuditPdfReport.reports", getReports());
        session.setAttribute("EnrollmentAuditPdfReport.mode", getMode());
        session.setAttribute("EnrollmentAuditPdfReport.all", getAll());
        session.setAttribute("EnrollmentAuditPdfReport.subjects", getSubjects());
        UserData.setPropertyBoolean(session, "EnrollmentAuditPdfReport.externalId", getExternalId());
        UserData.setPropertyBoolean(session, "EnrollmentAuditPdfReport.studentName", getStudentName());
        UserData.setPropertyBoolean(session, "EnrollmentAuditPdfReport.email", getEmail());
        UserData.setProperty(session,"EnrollmentAuditPdfReport.addr", getAddress());
        UserData.setProperty(session,"EnrollmentAuditPdfReport.cc", getCc());
        UserData.setProperty(session,"EnrollmentAuditPdfReport.bcc", getBcc());
        UserData.setProperty(session,"EnrollmentAuditPdfReport.message", getMessage());
        UserData.setProperty(session,"EnrollmentAuditPdfReport.subject", getSubject());
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
    public boolean getEmail() { return iEmail; }
    public void setEmail(boolean email) { iEmail = email; }
    public String getAddress() { return iAddr; }
    public void setAddress(String addr) { iAddr = addr; }
    public String getCc() { return iCc; }
    public void setCc(String cc) { iCc = cc; }
    public String getBcc() { return iBcc; }
    public void setBcc(String bcc) { iBcc = bcc; }
    public boolean getCanEmail() { return Email.isEnabled(); }
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
    
    public TreeSet<String> getAllReports() {
        return new TreeSet<String>(sRegisteredReports.keySet());
    }
    public String[] getModes() { return sModes; }


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
