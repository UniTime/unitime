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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Email;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.EnrollmentAuditPdfReportForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.reports.enrollment.PdfEnrollmentAuditReport;
import org.unitime.timetable.reports.exam.InstructorExamReport;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Service("/enrollmentAuditPdfReport")
public class EnrollmentAuditPdfReportAction extends Action {
    protected static Logger sLog = Logger.getLogger(EnrollmentAuditPdfReportAction.class);
    
    @Autowired SessionContext sessionContext;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		EnrollmentAuditPdfReportForm myForm = (EnrollmentAuditPdfReportForm) form;
        // Check Access
		sessionContext.checkPermission(Right.EnrollmentAuditPDFReports);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        if ("Generate".equals(op)) myForm.save(sessionContext);
        myForm.load(sessionContext);
        
        if ("Generate".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if (!errors.isEmpty()) {
                saveErrors(request, errors);
                return mapping.findForward("show");
            }
                        
            Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
            try {
                myForm.setReport("");

                Hashtable<String,File> output = new Hashtable();
                for (int i=0;i<myForm.getReports().length;i++) {
                    myForm.log("Generating "+myForm.getReports()[i]+"...");
                    Class reportClass = EnrollmentAuditPdfReportForm.sRegisteredReports.get(myForm.getReports()[i]);
                    String reportName = null;
                    for (Map.Entry<String, Class> entry : PdfEnrollmentAuditReport.sRegisteredReports.entrySet())
                        if (entry.getValue().equals(reportClass)) reportName = entry.getKey();
                    if (reportName==null) reportName = "r"+(i+1);
                    String name = session.getAcademicTerm()+session.getSessionStartYear()+"_"+reportName;
                    if (myForm.getAll()) {
                        File file = ApplicationProperties.getTempFile(name, (myForm.getModeIdx()==PdfEnrollmentAuditReport.sModeText?"txt":"pdf"));
                        myForm.log("&nbsp;&nbsp;Writing <a href='temp/"+file.getName()+"'>"+reportName+"."+(myForm.getModeIdx()==PdfEnrollmentAuditReport.sModeText?"txt":"pdf")+"</a>");
                        PdfEnrollmentAuditReport report = (PdfEnrollmentAuditReport)reportClass.
                            getConstructor(int.class, File.class, Session.class).
                            newInstance(myForm.getModeIdx(), file, session);
                        report.setShowId(myForm.getExternalId());
                        report.setShowName(myForm.getStudentName());
                        report.printReport();
                        report.close();
                        output.put(reportName+"."+(myForm.getModeIdx()==PdfEnrollmentAuditReport.sModeText?"txt":"pdf"),file);
                    } else {
                    	TreeSet<SubjectArea> subjectAreas = new TreeSet<SubjectArea>();
                    	String subjAbbvs = "";
                        for (int j=0;j<myForm.getSubjects().length;j++) {
                            SubjectArea subject = new SubjectAreaDAO().get(Long.valueOf(myForm.getSubjects()[j]));
                            if (subjAbbvs.length() == 0){
                            	subjAbbvs = subject.getSubjectAreaAbbreviation();
                        	}else if (subjAbbvs.length() < 40) {
                            	subjAbbvs += "_" + subject.getSubjectAreaAbbreviation();
                            } else if (!(subjAbbvs.charAt(subjAbbvs.length() - 1) == '.')){
                            	subjAbbvs += "_...";
                            }
                            subjectAreas.add(subject);
                        }
                        File file = ApplicationProperties.getTempFile(name+subjAbbvs, (myForm.getModeIdx()==PdfEnrollmentAuditReport.sModeText?"txt":"pdf"));
                        
                        myForm.log("&nbsp;&nbsp;Writing <a href='temp/"+file.getName()+"'>"+subjAbbvs+"_"+reportName+"."+(myForm.getModeIdx()==PdfEnrollmentAuditReport.sModeText?"txt":"pdf")+"</a>");
                        PdfEnrollmentAuditReport report = (PdfEnrollmentAuditReport)reportClass.
                            getConstructor(int.class, File.class, Session.class, TreeSet.class, String.class).
                            newInstance(myForm.getModeIdx(), file, session, subjectAreas, subjAbbvs);
                        report.setShowId(myForm.getExternalId());
                        report.setShowName(myForm.getStudentName());
                        report.printReport();
                        report.close();
                        output.put(subjAbbvs+"_"+reportName+"."+(myForm.getModeIdx()==PdfEnrollmentAuditReport.sModeText?"txt":"pdf"),file);
                    }
                }
                byte[] buffer = new byte[32*1024];
                int len = 0;
                if (output.isEmpty())
                    myForm.log("<font color='orange'>No report generated.</font>");
                else if (myForm.getEmail()) {
                    myForm.log("Sending email(s)...");
                    try {
                        Email mail = Email.createEmail();
                        mail.setSubject(myForm.getSubject()==null?"Enrollment Audit Report":myForm.getSubject());
                        mail.setText((myForm.getMessage()==null?"":myForm.getMessage()+"\r\n\r\n")+
                                "For an up-to-date report, please visit "+
                                request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/\r\n\r\n"+
                                "This email was automatically generated by "+
                                "UniTime "+Constants.getVersion()+
                                " (Univesity Timetabling Application, http://www.unitime.org).");
                        if (myForm.getAddress()!=null) for (StringTokenizer s=new StringTokenizer(myForm.getAddress(),";,\n\r ");s.hasMoreTokens();) 
                            mail.addRecipient(s.nextToken(), null);
                        if (myForm.getCc()!=null) for (StringTokenizer s=new StringTokenizer(myForm.getCc(),";,\n\r ");s.hasMoreTokens();) 
                            mail.addRecipientCC(s.nextToken(), null);
                        if (myForm.getBcc()!=null) for (StringTokenizer s=new StringTokenizer(myForm.getBcc(),";,\n\r ");s.hasMoreTokens();) 
                            mail.addRecipientBCC(s.nextToken(), null);
                        for (Map.Entry<String, File> entry : output.entrySet()) {
                        	mail.addAttachment(entry.getValue(), session.getAcademicTerm()+session.getSessionStartYear()+"_"+entry.getKey());
                        }
                        mail.send();
                        myForm.log("Email sent.");
                    } catch (Exception e) {
                        myForm.log("<font color='red'>Unable to send email: "+e.getMessage()+"</font>");
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
                        File zipFile = ApplicationProperties.getTempFile(session.getAcademicTerm()+session.getSessionStartYear(), "zip");
                        myForm.log("Writing <a href='temp/"+zipFile.getName()+"'>"+session.getAcademicTerm()+session.getSessionStartYear()+".zip</a>...");
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
                myForm.log("All done.");
            } catch (Exception e) {
                myForm.log("<font color='red'>Process failed: "+e.getMessage()+" (exception "+e.getClass().getName()+")</font>");
                sLog.error(e.getMessage(),e);
                errors.add("report", new ActionMessage("errors.generic", "Unable to generate report, reason: "+e.getMessage()));
                saveErrors(request, errors);
            }
        }
        
        return mapping.findForward("show");
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

