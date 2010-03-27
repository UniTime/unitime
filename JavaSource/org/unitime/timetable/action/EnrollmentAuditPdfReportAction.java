/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
package org.unitime.timetable.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.EnrollmentAuditPdfReportForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.reports.enrollment.PdfEnrollmentAuditReport;
import org.unitime.timetable.reports.exam.InstructorExamReport;
import org.unitime.timetable.util.Constants;


/** 
 * @author Tomas Muller
 */
public class EnrollmentAuditPdfReportAction extends Action {
    protected static Logger sLog = Logger.getLogger(EnrollmentAuditPdfReportAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		EnrollmentAuditPdfReportForm myForm = (EnrollmentAuditPdfReportForm) form;
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        TimetableManager mgr = TimetableManager.getManager(Web.getUser(request.getSession()));

        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        if ("Generate".equals(op)) myForm.save(request.getSession());
        myForm.load(request.getSession());
        
        if ("Generate".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if (!errors.isEmpty()) {
                saveErrors(request, errors);
                return mapping.findForward("show");
            }
                        
            Session session = Session.getCurrentAcadSession(Web.getUser(request.getSession()));
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
                            newInstance(myForm.getModeIdx(), file, new SessionDAO().get(session.getUniqueId()));
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
                            newInstance(myForm.getModeIdx(), file, new SessionDAO().get(session.getUniqueId()), subjectAreas, subjAbbvs);
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
                    InternetAddress from = 
                        (mgr.getEmailAddress()==null?
                                new InternetAddress(
                                        ApplicationProperties.getProperty("tmtbl.inquiry.sender",ApplicationProperties.getProperty("tmtbl.contact.email")),
                                        ApplicationProperties.getProperty("tmtbl.inquiry.sender.name")):
                                new InternetAddress(mgr.getEmailAddress(),mgr.getName()));
                    myForm.log("Sending email(s)...");
                    Properties p = ApplicationProperties.getProperties();
                    if (p.getProperty("mail.smtp.host")==null && p.getProperty("tmtbl.smtp.host")!=null)
                        p.setProperty("mail.smtp.host", p.getProperty("tmtbl.smtp.host"));
                    Authenticator a = null;
                    if (ApplicationProperties.getProperty("tmtbl.mail.user")!=null && ApplicationProperties.getProperty("tmtbl.mail.pwd")!=null) {
                        a = new Authenticator() {
                            public PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(
                                        ApplicationProperties.getProperty("tmtbl.mail.user"),
                                        ApplicationProperties.getProperty("tmtbl.mail.pwd"));
                            }
                        };
                    }
                    javax.mail.Session mailSession = javax.mail.Session.getDefaultInstance(p, a);
                    MimeMessage mail = new MimeMessage(mailSession);
                    mail.setSubject(myForm.getSubject()==null?"Enrollment Audit Report":myForm.getSubject());
                    Multipart body = new MimeMultipart();
                    MimeBodyPart text = new MimeBodyPart();
                    text.setText((myForm.getMessage()==null?"":myForm.getMessage()+"\r\n\r\n")+
                            "For an up-to-date report, please visit "+
                            request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/\r\n\r\n"+
                            "This email was automatically generated by "+
                            "UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+
                            " (Univesity Timetabling Application, http://www.unitime.org).");
                    body.addBodyPart(text);
                    if (myForm.getAddress()!=null) for (StringTokenizer s=new StringTokenizer(myForm.getAddress(),";,\n\r ");s.hasMoreTokens();) 
                        mail.addRecipient(RecipientType.TO, new InternetAddress(s.nextToken()));
                    if (myForm.getCc()!=null) for (StringTokenizer s=new StringTokenizer(myForm.getCc(),";,\n\r ");s.hasMoreTokens();) 
                        mail.addRecipient(RecipientType.CC, new InternetAddress(s.nextToken()));
                    if (myForm.getBcc()!=null) for (StringTokenizer s=new StringTokenizer(myForm.getBcc(),";,\n\r ");s.hasMoreTokens();) 
                        mail.addRecipient(RecipientType.BCC, new InternetAddress(s.nextToken()));
                    if (from!=null)
                        mail.setFrom(from);
                    for (Map.Entry<String, File> entry : output.entrySet()) {
                        BodyPart attachement = new MimeBodyPart();
                        attachement.setDataHandler(new DataHandler(new FileDataSource(entry.getValue())));
                         attachement.setFileName(session.getAcademicTerm()+session.getSessionStartYear()+"_"+entry.getKey());
                        body.addBodyPart(attachement);
                    }
                    mail.setSentDate(new Date());
                    mail.setContent(body);
                    try {
                        Transport.send(mail);
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

