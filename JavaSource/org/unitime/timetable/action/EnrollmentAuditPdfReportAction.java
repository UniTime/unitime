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
package org.unitime.timetable.action;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.EnrollmentAuditPdfReportForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.reports.enrollment.PdfEnrollmentAuditReport;
import org.unitime.timetable.reports.exam.InstructorExamReport;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Action(value = "enrollmentAuditPdfReport", results = {
		@Result(name = "show", type = "tiles", location = "enrollmentAuditPdfReport.tiles")
	})
@TilesDefinition(name = "enrollmentAuditPdfReport.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Enrollment Audit PDF Reports"),
		@TilesPutAttribute(name = "body", value = "/user/pdfEnrollmentAuditReport.jsp")
	})
public class EnrollmentAuditPdfReportAction extends UniTimeAction<EnrollmentAuditPdfReportForm> {
	private static final long serialVersionUID = -7475297473237927492L;
	private static Log sLog = LogFactory.getLog(EnrollmentAuditPdfReportAction.class);
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);

	@Override
	public String execute() throws Exception {
		if (form == null) {
			form = new EnrollmentAuditPdfReportForm();
		}
        // Check Access
		sessionContext.checkPermission(Right.EnrollmentAuditPDFReports);
        
        // Read operation to be performed
		if (form.getOp() != null) op = form.getOp();
        if (MSG.actionGenerateReport().equals(op)) form.save(sessionContext);
        form.load(sessionContext);
        
        if (MSG.actionGenerateReport().equals(op)) {
            form.validate(this);
            if (hasFieldErrors()) return "show";
                        
            Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
            try {
                form.setReport("");

                Hashtable<String,File> output = new Hashtable();
                for (int i=0;i<form.getReports().length;i++) {
                    Class reportClass = EnrollmentAuditPdfReportForm.RegisteredReport.valueOf(form.getReports()[i]).getImplementation();
                    String reportName = form.getReportName(EnrollmentAuditPdfReportForm.RegisteredReport.valueOf(form.getReports()[i]));
                    String name = session.getAcademicTerm()+session.getAcademicYear()+"_"+form.getReports()[i];
                	String ext = PdfLegacyExamReport.getExtension(form.getReportMode().ordinal());
                    form.log(MSG.statusGeneratingReport(reportName));
                    if (form.getAll()) {
                        File file = ApplicationProperties.getTempFile(name, ext);
                        form.log("&nbsp;&nbsp;"+MSG.infoWritingReport("<a href='temp/"+file.getName()+"'>"+reportName+ext+"</a>"));
                        PdfEnrollmentAuditReport report = (PdfEnrollmentAuditReport)reportClass.
                            getConstructor(int.class, File.class, Session.class).
                            newInstance(form.getReportMode().ordinal(), file, session);
                        report.setShowId(form.getExternalId());
                        report.setShowName(form.getStudentName());
                        report.printReport();
                        report.close();
                        output.put(reportName+ext,file);
                    } else {
                    	TreeSet<SubjectArea> subjectAreas = new TreeSet<SubjectArea>();
                    	String subjAbbvs = "";
                        for (int j=0;j<form.getSubjects().length;j++) {
                            SubjectArea subject = new SubjectAreaDAO().get(Long.valueOf(form.getSubjects()[j]));
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
                        
                        form.log("&nbsp;&nbsp;"+MSG.infoWritingReport("<a href='temp/"+file.getName()+"'>"+subjAbbvs+"_"+reportName+ext+"</a>"));
                        PdfEnrollmentAuditReport report = (PdfEnrollmentAuditReport)reportClass.
                            getConstructor(int.class, File.class, Session.class, TreeSet.class, String.class).
                            newInstance(form.getReportMode().ordinal(), file, session, subjectAreas, subjAbbvs);
                        report.setShowId(form.getExternalId());
                        report.setShowName(form.getStudentName());
                        report.printReport();
                        report.close();
                        output.put(subjAbbvs+"_"+reportName+ext,file);
                    }
                }
                byte[] buffer = new byte[32*1024];
                int len = 0;
                if (output.isEmpty())
                    form.log("<font color='orange'>" + MSG.warnNoReportGenerated() + "</font>");
                else if (form.getEmail()) {
                    form.log(MSG.statusSendingEmails());
                    try {
                        Email mail = Email.createEmail();
                        mail.setSubject(form.getSubject()==null?"Enrollment Audit Report":form.getSubject());
                        mail.setText((form.getMessage()==null?"":form.getMessage()+"\r\n\r\n")+
                        		MSG.emailForUpToDateEnrlReportVisit(request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath())+"/\r\n\r\n"+
                        		MSG.emailFooter(Constants.getVersion()));
                        if (form.getAddress()!=null) for (StringTokenizer s=new StringTokenizer(form.getAddress(),";,\n\r ");s.hasMoreTokens();) 
                            mail.addRecipient(s.nextToken(), null);
                        if (form.getCc()!=null) for (StringTokenizer s=new StringTokenizer(form.getCc(),";,\n\r ");s.hasMoreTokens();) 
                            mail.addRecipientCC(s.nextToken(), null);
                        if (form.getBcc()!=null) for (StringTokenizer s=new StringTokenizer(form.getBcc(),";,\n\r ");s.hasMoreTokens();) 
                            mail.addRecipientBCC(s.nextToken(), null);
                        for (Map.Entry<String, File> entry : output.entrySet()) {
                        	mail.addAttachment(entry.getValue(), session.getAcademicTerm()+session.getAcademicYear()+"_"+entry.getKey());
                        }
                        mail.send();
                        form.log(MSG.infoEmailSent());
                    } catch (Exception e) {
                        form.log("<font color='red'>"+MSG.errorUnableToSendEmail(e.getMessage())+"</font>");
                    }
                    
                }
                if (output.isEmpty()) {
                    return null;
                } else if (output.size()==1) {
                    request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+output.elements().nextElement().getName());
                } else {
                    FileInputStream fis = null;
                    ZipOutputStream zip = null;
                    try {
                        File zipFile = ApplicationProperties.getTempFile(session.getAcademicTerm()+session.getAcademicYear(), "zip");
                        form.log(MSG.statusWritingReport("<a href='temp/"+zipFile.getName()+"'>"+session.getAcademicTerm()+session.getAcademicYear()+".zip</a>"));
                        zip = new ZipOutputStream(new FileOutputStream(zipFile));
                        for (Map.Entry<String, File> entry : output.entrySet()) {
                            zip.putNextEntry(new ZipEntry(entry.getKey()));
                            fis = new FileInputStream(entry.getValue());
                            while ((len=fis.read(buffer))>0) zip.write(buffer, 0, len);
                            fis.close(); fis = null;
                            zip.closeEntry();
                        }
                        zip.flush(); zip.close();
                        request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+zipFile.getName());
                    } catch (IOException e) {
                        if (fis!=null) fis.close();
                        if (zip!=null) zip.close();
                    }
                }
                form.log(MSG.statusAllDone());
            } catch (Exception e) {
                form.log("<font color='red'>" + MSG.errorTaskFailedWithMessage(e.getMessage())+"</font>");
                sLog.error(e.getMessage(),e);
                addFieldError("report", MSG.errorUnableToGenerateReport(e.getMessage()));
            }
        }
        
        return "show";
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

