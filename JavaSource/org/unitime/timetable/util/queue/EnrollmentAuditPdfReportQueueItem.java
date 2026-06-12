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
package org.unitime.timetable.util.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.EnrollmentAuditPdfReportForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.reports.enrollment.PdfEnrollmentAuditReport;
import org.unitime.timetable.reports.exam.InstructorExamReport;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.util.Constants;

import jakarta.servlet.http.HttpServletRequest;

public class EnrollmentAuditPdfReportQueueItem extends QueueItem {
	private static final long serialVersionUID = 1L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);

	public static String TYPE = "PDF Enrollment Report";
	
	private EnrollmentAuditPdfReportForm iForm;
	private String iUrl = null;
	private String iName = null;
	private double iProgressPct = 0;

	public EnrollmentAuditPdfReportQueueItem(Session session, UserContext owner, EnrollmentAuditPdfReportForm form, HttpServletRequest request) {
		super(session, owner);
		iForm = form;
		if (request != null)
			iUrl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		else
			iUrl = ApplicationProperty.UniTimeUrl.value();
		iName = "";
        for (int i=0;i<iForm.getReports().length;i++) {
        	if (i > 0) iName += ", ";
        	iName += EnrollmentAuditPdfReportForm.getReportName(EnrollmentAuditPdfReportForm.RegisteredReport.valueOf(iForm.getReports()[i]));
        }
        if (!iForm.getAll()) {
        	iName += " (";
            for (int i=0;i<iForm.getSubjects().length;i++) {
                SubjectArea subject = SubjectAreaDAO.getInstance().get(Long.valueOf(iForm.getSubjects()[i]));
                if (i > 0) iName += ", ";
                iName += subject.getSubjectAreaAbbreviation();
            }
            iName += ")";
        }
	}
	
	@Override
	public void execute() {
		org.hibernate.Session hibSession = ExamDAO.getInstance().getSession();
		createReports(hibSession);
		if (hibSession.isOpen()) hibSession.close();
	}
	
	private void createReports(org.hibernate.Session hibSession) {
        try {
        	iProgressPct = 0.1;
        	Session session = getSession();
            Hashtable<String,File> output = new Hashtable();
            for (int i=0;i<iForm.getReports().length;i++) {
            	iProgressPct = 0.1 + (0.8 * i) / iForm.getReports().length;
                Class reportClass = EnrollmentAuditPdfReportForm.RegisteredReport.valueOf(iForm.getReports()[i]).getImplementation();
                String reportName = EnrollmentAuditPdfReportForm.getReportName(EnrollmentAuditPdfReportForm.RegisteredReport.valueOf(iForm.getReports()[i]));
                String name = session.getAcademicTerm()+session.getAcademicYear()+"_"+iForm.getReports()[i];
            	String ext = PdfLegacyExamReport.getExtension(iForm.getReportMode().ordinal());
                log(MSG.statusGeneratingReport(reportName));
                if (iForm.getAll()) {
                    File file = ApplicationProperties.getTempFile(name, ext);
                    log("  "+MSG.infoWritingReport("<a href='temp/"+file.getName()+"'>"+reportName+ext+"</a>"));
                    PdfEnrollmentAuditReport report = (PdfEnrollmentAuditReport)reportClass.
                        getConstructor(int.class, File.class, Session.class).
                        newInstance(iForm.getReportMode().ordinal(), file, session);
                    report.setShowId(iForm.getExternalId());
                    report.setShowName(iForm.getStudentName());
                    report.printReport();
                    report.close();
                    output.put(reportName+ext,file);
                } else {
                	TreeSet<SubjectArea> subjectAreas = new TreeSet<SubjectArea>();
                	String subjAbbvs = "";
                    for (int j=0;j<iForm.getSubjects().length;j++) {
                        SubjectArea subject = SubjectAreaDAO.getInstance().get(Long.valueOf(iForm.getSubjects()[j]));
                        if (subjAbbvs.length() == 0){
                        	subjAbbvs = subject.getSubjectAreaAbbreviation();
                    	}else if (subjAbbvs.length() < 40) {
                        	subjAbbvs += "_" + subject.getSubjectAreaAbbreviation();
                        } else if (!(subjAbbvs.charAt(subjAbbvs.length() - 1) == '.')){
                        	subjAbbvs += "_...";
                        }
                        subjectAreas.add(subject);
                    }
                    File file = ApplicationProperties.getTempFile(name+subjAbbvs, ext);
                    
                    log("  "+MSG.infoWritingReport("<a href='temp/"+file.getName()+"'>"+subjAbbvs+"_"+reportName+ext+"</a>"));
                    PdfEnrollmentAuditReport report = (PdfEnrollmentAuditReport)reportClass.
                        getConstructor(int.class, File.class, Session.class, TreeSet.class, String.class).
                        newInstance(iForm.getReportMode().ordinal(), file, session, subjectAreas, subjAbbvs);
                    report.setShowId(iForm.getExternalId());
                    report.setShowName(iForm.getStudentName());
                    report.printReport();
                    report.close();
                    output.put(subjAbbvs+"_"+reportName+ext,file);
                }
            }
            iProgressPct = 0.9;
            byte[] buffer = new byte[32*1024];
            int len = 0;
            if (output.isEmpty())
                warn(MSG.warnNoReportGenerated());
            else if (iForm.getEmail()) {
                log(MSG.statusSendingEmails());
                try {
                    Email mail = Email.createEmail();
                    mail.setSubject(iForm.getSubject()==null?"Enrollment Audit Report":iForm.getSubject());
                    mail.setText((iForm.getMessage()==null?"":iForm.getMessage()+"\r\n\r\n")+
                    		(iUrl == null ? "" : MSG.emailForUpToDateEnrlReportVisit(iUrl)+"/\r\n\r\n")+
                    		MSG.emailFooter(Constants.getVersion()));
                    if (iForm.getAddress()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getAddress(),";,\n\r ");s.hasMoreTokens();) 
                        mail.addRecipient(s.nextToken(), null);
                    if (iForm.getCc()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getCc(),";,\n\r ");s.hasMoreTokens();) 
                        mail.addRecipientCC(s.nextToken(), null);
                    if (iForm.getBcc()!=null) for (StringTokenizer s=new StringTokenizer(iForm.getBcc(),";,\n\r ");s.hasMoreTokens();) 
                        mail.addRecipientBCC(s.nextToken(), null);
                    for (Map.Entry<String, File> entry : output.entrySet()) {
                    	mail.addAttachment(entry.getValue(), session.getAcademicTerm()+session.getAcademicYear()+"_"+entry.getKey());
                    }
                    mail.send();
                    log(MSG.infoEmailSent());
                } catch (Exception e) {
                    error(MSG.errorUnableToSendEmail(e.getMessage()));
                }
                
            }
            if (output.isEmpty()) {
            	throw new Exception(MSG.errorNoReportGenerated());
            } else if (output.size()==1) {
            	setOutput(output.elements().nextElement());
            } else {
                FileInputStream fis = null;
                ZipOutputStream zip = null;
                try {
                    File zipFile = ApplicationProperties.getTempFile(session.getAcademicTerm()+session.getAcademicYear(), "zip");
                    log(MSG.statusWritingReport("<a href='temp/"+zipFile.getName()+"'>"+session.getAcademicTerm()+session.getAcademicYear()+".zip</a>"));
                    zip = new ZipOutputStream(new FileOutputStream(zipFile));
                    for (Map.Entry<String, File> entry : output.entrySet()) {
                        zip.putNextEntry(new ZipEntry(entry.getKey()));
                        fis = new FileInputStream(entry.getValue());
                        while ((len=fis.read(buffer))>0) zip.write(buffer, 0, len);
                        fis.close(); fis = null;
                        zip.closeEntry();
                    }
                    zip.flush(); zip.close();
                    setOutput(zipFile);
                } catch (IOException e) {
                    if (fis!=null) fis.close();
                    if (zip!=null) zip.close();
                }
            }
        	iProgressPct = 1.0;
            setStatus(MSG.statusAllDone());
        } catch (Exception e) {
            fatal(MSG.errorTaskFailed(), e);
        }
	}

	@Override
	public String name() {
		return iName;
	}

	@Override
	public double progress() {
		return iProgressPct;
	}
	
	@Override
	public String type() {
		return TYPE;
	}
	
	public static class FileGenerator implements InstructorExamReport.FileGenerator {
        String iName;
        public FileGenerator(String name) {
            iName = name;
        }
        public File generate(String prefix, String ext) {
            return ApplicationProperties.getTempFile(iName+"_"+prefix, ext);
        }
    }

}
