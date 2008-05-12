/*
 * UniTime 3.1 (University Timetabling Application)
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
package org.unitime.timetable.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
import org.unitime.timetable.form.ExamPdfReportForm;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.reports.exam.InstructorExamReport;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.reports.exam.StudentExamReport;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamInstructorInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;
import org.unitime.timetable.util.Constants;


/** 
 * @author Tomas Muller
 */
public class ExamPdfReportAction extends Action {
    protected static Logger sLog = Logger.getLogger(ExamPdfReportAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ExamPdfReportForm myForm = (ExamPdfReportForm) form;
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        if (WebSolver.getExamSolver(request.getSession())!=null)
            request.setAttribute(Constants.REQUEST_WARN, "Examination PDF reports are generated from the saved solution (solver assignments are ignored).");

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
                TreeSet<ExamAssignmentInfo> exams = new TreeSet();
                Hashtable<SubjectArea,TreeSet<ExamAssignmentInfo>> examsPerSubject = new Hashtable();
                myForm.setReport("");
                myForm.log("Loading exams...");
                if (myForm.getAll()) {
                    for (Iterator i=Exam.findAll(session.getUniqueId(), myForm.getExamType()).iterator();i.hasNext();) {
                        exams.add(new ExamAssignmentInfo((Exam)i.next()));
                    }
                } else {
                    for (int i=0;i<myForm.getSubjects().length;i++) {
                        SubjectArea subject = new SubjectAreaDAO().get(Long.valueOf(myForm.getSubjects()[i]));
                        TreeSet<ExamAssignmentInfo> examsThisSubject = new TreeSet();
                        for (Iterator j=Exam.findExamsOfSubjectArea(subject.getUniqueId(), myForm.getExamType()).iterator();j.hasNext();) {
                            examsThisSubject.add(new ExamAssignmentInfo((Exam)j.next()));
                        }
                        examsPerSubject.put(subject, examsThisSubject);
                    }
                }
                Hashtable<String,File> output = new Hashtable();
                Hashtable<SubjectArea,Hashtable<String,File>> outputPerSubject = new Hashtable();
                Hashtable<ExamInstructorInfo,File> ireports = null;
                Hashtable<Student,File> sreports = null;
                for (int i=0;i<myForm.getReports().length;i++) {
                    myForm.log("Generating "+myForm.getReports()[i]+"...");
                    Class reportClass = ExamPdfReportForm.sRegisteredReports.get(myForm.getReports()[i]);
                    String reportName = null;
                    for (Map.Entry<String, Class> entry : PdfLegacyExamReport.sRegisteredReports.entrySet())
                        if (entry.getValue().equals(reportClass)) reportName = entry.getKey();
                    if (reportName==null) reportName = "r"+(i+1);
                    String name = session.getAcademicTerm()+session.getYear()+(myForm.getExamType()==Exam.sExamTypeMidterm?"evn":"fin")+"_"+reportName;
                    if (myForm.getAll()) {
                        File file = ApplicationProperties.getTempFile(name, (myForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"));
                        myForm.log("&nbsp;&nbsp;Writing <a href='temp/"+file.getName()+"'>"+reportName+"."+(myForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf")+"</a>... ("+exams.size()+" exams)");
                        PdfLegacyExamReport report = (PdfLegacyExamReport)reportClass.
                            getConstructor(int.class, File.class, Session.class, int.class, SubjectArea.class, Collection.class).
                            newInstance(myForm.getModeIdx(), file, new SessionDAO().get(session.getUniqueId()), myForm.getExamType(), null, exams);
                        report.setDirect(myForm.getDirect());
                        report.setM2d(myForm.getM2d());
                        report.setBtb(myForm.getBtb());
                        report.setDispRooms(myForm.getDispRooms());
                        report.setNoRoom(myForm.getNoRoom());
                        report.setTotals(myForm.getTotals());
                        report.setLimit(myForm.getLimit()==null || myForm.getLimit().length()==0?-1:Integer.parseInt(myForm.getLimit()));
                        report.setRoomCode(myForm.getRoomCodes());
                        report.setDispLimits(myForm.getDispLimit());
                        report.setSince(myForm.getSince()==null || myForm.getSince().length()==0?null:new SimpleDateFormat("MM/dd/yyyy").parse(myForm.getSince()));
                        report.setItype(myForm.getItype());
                        report.setClassSchedule(myForm.getClassSchedule());
                        report.printReport();
                        report.close();
                        output.put(reportName+"."+(myForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"),file);
                        if (report instanceof InstructorExamReport && myForm.getEmailInstructors()) {
                            ireports = ((InstructorExamReport)report).printInstructorReports(
                                    myForm.getModeIdx(), name, new FileGenerator(name), new InstructorExamReport.InstructorFilter() {
                                public boolean generate(ExamInstructorInfo instructor, TreeSet<ExamAssignmentInfo> exams) {
                                    return true;
                                }
                            });
                        } else if (report instanceof StudentExamReport && myForm.getEmailStudents()) {
                            sreports = ((StudentExamReport)report).printStudentReports(
                                    myForm.getModeIdx(), name, new FileGenerator(name), new StudentExamReport.StudentFilter() {
                                public boolean generate(Student student, TreeSet<ExamSectionInfo> sections) {
                                    return true;
                                }
                            });
                        }
                    } else {
                        for (Map.Entry<SubjectArea, TreeSet<ExamAssignmentInfo>> entry : examsPerSubject.entrySet()) {
                            File file = ApplicationProperties.getTempFile(name+"_"+entry.getKey().getSubjectAreaAbbreviation(), (myForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"));
                            myForm.log("&nbsp;&nbsp;Writing <a href='temp/"+file.getName()+"'>"+entry.getKey().getSubjectAreaAbbreviation()+"_"+reportName+"."+(myForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf")+"</a>... ("+entry.getValue().size()+" exams)");
                            PdfLegacyExamReport report = (PdfLegacyExamReport)reportClass.
                                getConstructor(int.class, File.class, Session.class, int.class, SubjectArea.class, Collection.class).
                                newInstance(myForm.getModeIdx(), file, new SessionDAO().get(session.getUniqueId()), myForm.getExamType(), entry.getKey(), entry.getValue());
                            report.setDirect(myForm.getDirect());
                            report.setM2d(myForm.getM2d());
                            report.setBtb(myForm.getBtb());
                            report.setDispRooms(myForm.getDispRooms());
                            report.setNoRoom(myForm.getNoRoom());
                            report.setTotals(myForm.getTotals());
                            report.setLimit(myForm.getLimit()==null || myForm.getLimit().length()==0?-1:Integer.parseInt(myForm.getLimit()));
                            report.setRoomCode(myForm.getRoomCodes());
                            report.setDispLimits(myForm.getDispLimit());
                            report.setItype(myForm.getItype());
                            report.setClassSchedule(myForm.getClassSchedule());
                            report.printReport();
                            report.close();
                            output.put(entry.getKey().getSubjectAreaAbbreviation()+"_"+reportName+"."+(myForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"),file);
                            Hashtable<String,File> files = outputPerSubject.get(entry.getKey());
                            if (files==null) {
                                files = new Hashtable(); outputPerSubject.put(entry.getKey(),files);
                            }
                            files.put(entry.getKey().getSubjectAreaAbbreviation()+"_"+reportName+"."+(myForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"),file);
                        }
                    }
                }
                byte[] buffer = new byte[32*1024];
                int len = 0;
                if (output.isEmpty())
                    myForm.log("<font color='orange'>No report generated.</font>");
                else if (myForm.getEmail()) {
                    TimetableManager mgr = TimetableManager.getManager(Web.getUser(request.getSession()));
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
                    if (myForm.getEmailDeputies()) {
                        Hashtable<TimetableManager,Hashtable<String,File>> files2send = new Hashtable();
                        for (Map.Entry<SubjectArea, Hashtable<String,File>> entry : outputPerSubject.entrySet()) {
                            if (entry.getKey().getDepartment().getTimetableManagers().isEmpty())
                                myForm.log("<font color='orange'>&nbsp;&nbsp;No manager associated with subject area "+entry.getKey().getSubjectAreaAbbreviation()+
                                    " ("+entry.getKey().getDepartment().getLabel()+")</font>");
                            for (Iterator i=entry.getKey().getDepartment().getTimetableManagers().iterator();i.hasNext();) {
                                TimetableManager g = (TimetableManager)i.next();
                                if (g.getEmailAddress()==null || g.getEmailAddress().length()==0) {
                                    myForm.log("<font color='orange'>&nbsp;&nbsp;Manager "+g.getName()+" has no email address.</font>");
                                } else {
                                    Hashtable<String,File> files = files2send.get(g);
                                    if (files==null) { files = new Hashtable<String,File>(); files2send.put(g, files); }
                                    files.putAll(entry.getValue());
                                }
                            }
                        }
                        if (files2send.isEmpty()) {
                            myForm.log("<font color='red'>Nothing to send.</font>");
                        } else {
                            Set<TimetableManager> managers = files2send.keySet();
                            while (!managers.isEmpty()) {
                                TimetableManager manager = managers.iterator().next();
                                Hashtable<String,File> files = files2send.get(manager);
                                managers.remove(manager);
                                myForm.log("Sending email to "+manager.getName()+" ("+manager.getEmailAddress()+")...");
                                MimeMessage mail = new MimeMessage(mailSession);
                                mail.setSubject(myForm.getSubject()==null?"Examination Report":myForm.getSubject());
                                Multipart body = new MimeMultipart();
                                BodyPart text = new MimeBodyPart();
                                text.setText((myForm.getMessage()==null?"":myForm.getMessage()+"\r\n\r\n")+
                                        "For an up-to-date report, please visit "+
                                        request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/\r\n\r\n"+
                                        "This email was automatically generated by "+
                                        "UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+
                                        " (Univesity Timetabling Application, http://www.unitime.org).");
                                body.addBodyPart(text);
                                mail.addRecipient(RecipientType.TO, new InternetAddress(manager.getEmailAddress(),manager.getName()));
                                for (Iterator<TimetableManager> i=managers.iterator();i.hasNext();) {
                                    TimetableManager m = (TimetableManager)i.next();
                                    if (files.equals(files2send.get(m))) {
                                        myForm.log("&nbsp;&nbsp;Including "+m.getName()+" ("+m.getEmailAddress()+")");
                                        mail.addRecipient(RecipientType.TO, new InternetAddress(m.getEmailAddress(),m.getName()));
                                        i.remove();
                                    }
                                }
                                if (myForm.getAddress()!=null) for (StringTokenizer s=new StringTokenizer(myForm.getAddress(),";,\n\r ");s.hasMoreTokens();) 
                                    mail.addRecipient(RecipientType.TO, new InternetAddress(s.nextToken()));
                                if (myForm.getCc()!=null) for (StringTokenizer s=new StringTokenizer(myForm.getCc(),";,\n\r ");s.hasMoreTokens();) 
                                    mail.addRecipient(RecipientType.CC, new InternetAddress(s.nextToken()));
                                if (myForm.getBcc()!=null) for (StringTokenizer s=new StringTokenizer(myForm.getBcc(),";,\n\r ");s.hasMoreTokens();) 
                                    mail.addRecipient(RecipientType.BCC, new InternetAddress(s.nextToken()));
                                if (from!=null)
                                    mail.setFrom(from);
                                for (Map.Entry<String, File> entry : files.entrySet()) {
                                    BodyPart attachement = new MimeBodyPart();
                                    attachement.setDataHandler(new DataHandler(new FileDataSource(entry.getValue())));
                                    attachement.setFileName(session.getAcademicTerm()+session.getYear()+(myForm.getExamType()==Exam.sExamTypeMidterm?"evn":"fin")+"_"+entry.getKey());
                                    body.addBodyPart(attachement);
                                    myForm.log("&nbsp;&nbsp;Attaching <a href='temp/"+entry.getValue().getName()+"'>"+entry.getKey()+"</a>");
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
                        }
                    } else {
                        MimeMessage mail = new MimeMessage(mailSession);
                        mail.setSubject(myForm.getSubject()==null?"Examination Report":myForm.getSubject());
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
                            attachement.setFileName(session.getAcademicTerm()+session.getYear()+(myForm.getExamType()==Exam.sExamTypeMidterm?"evn":"fin")+"_"+entry.getKey());
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
                    if (myForm.getEmailInstructors() && ireports!=null && !ireports.isEmpty()) {
                        myForm.log("Emailing instructors...");
                        for (ExamInstructorInfo instructor : new TreeSet<ExamInstructorInfo>(ireports.keySet())) {
                            File report = ireports.get(instructor);
                            String email = instructor.getInstructor().getEmail();
                            if (email==null || email.length()==0) {
                                myForm.log("&nbsp;&nbsp;<font color='orange'>Unable to email <a href='temp/"+report.getName()+"'>"+instructor.getName()+"</a> -- instructor has no email address.</font>");
                                continue;
                            }
                            MimeMessage mail = new MimeMessage(mailSession);
                            mail.setSubject(myForm.getSubject()==null?"Examination Report":myForm.getSubject());
                            Multipart body = new MimeMultipart();
                            MimeBodyPart text = new MimeBodyPart();
                            text.setText((myForm.getMessage()==null?"":myForm.getMessage()+"\r\n\r\n")+
                                    "For an up-to-date report, please visit "+
                                    request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/\r\n\r\n"+
                                    "This email was automatically generated by "+
                                    "UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+
                                    " (Univesity Timetabling Application, http://www.unitime.org).");
                            body.addBodyPart(text);
                            mail.addRecipient(RecipientType.TO, new InternetAddress(email));
                            if (from!=null) mail.setFrom(from);
                            BodyPart attachement = new MimeBodyPart();
                            attachement.setDataHandler(new DataHandler(new FileDataSource(report)));
                            attachement.setFileName(session.getAcademicTerm()+session.getYear()+(myForm.getExamType()==Exam.sExamTypeMidterm?"evn":"fin")+(myForm.getModeIdx()==PdfLegacyExamReport.sModeText?".txt":".pdf"));
                            mail.setSentDate(new Date());
                            mail.setContent(body);
                            try {
                                Transport.send(mail);
                                myForm.log("&nbsp;&nbsp;An email was sent to <a href='temp/"+report.getName()+"'>"+instructor.getName()+"</a>.");
                            } catch (Exception e) {
                                myForm.log("&nbsp;&nbsp;<font color='orange'>Unable to email <a href='temp/"+report.getName()+"'>"+instructor.getName()+"</a> -- "+e.getMessage()+".</font>");
                            }
                        }
                        myForm.log("Emails sent.");
                    }
                    if (myForm.getEmailStudents() && sreports!=null && !sreports.isEmpty()) {
                        myForm.log("Emailing instructors...");
                        for (Student student : new TreeSet<Student>(sreports.keySet())) {
                            File report = sreports.get(student);
                            String email = student.getEmail();
                            if (email==null || email.length()==0) {
                                myForm.log("&nbsp;&nbsp;<font color='orange'>Unable to email <a href='temp/"+report.getName()+"'>"+student.getName(DepartmentalInstructor.sNameFormatLastFist)+"</a> -- student has no email address.</font>");
                                continue;
                            }
                            MimeMessage mail = new MimeMessage(mailSession);
                            mail.setSubject(myForm.getSubject()==null?"Examination Report":myForm.getSubject());
                            Multipart body = new MimeMultipart();
                            MimeBodyPart text = new MimeBodyPart();
                            text.setText((myForm.getMessage()==null?"":myForm.getMessage()+"\r\n\r\n")+
                                    "For an up-to-date report, please visit "+
                                    request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/\r\n\r\n"+
                                    "This email was automatically generated by "+
                                    "UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?")+
                                    " (Univesity Timetabling Application, http://www.unitime.org).");
                            body.addBodyPart(text);
                            mail.addRecipient(RecipientType.TO, new InternetAddress(email));
                            if (from!=null) mail.setFrom(from);
                            BodyPart attachement = new MimeBodyPart();
                            attachement.setDataHandler(new DataHandler(new FileDataSource(report)));
                            attachement.setFileName(session.getAcademicTerm()+session.getYear()+(myForm.getExamType()==Exam.sExamTypeMidterm?"evn":"fin")+(myForm.getModeIdx()==PdfLegacyExamReport.sModeText?".txt":".pdf"));
                            mail.setSentDate(new Date());
                            mail.setContent(body);
                            try {
                                Transport.send(mail);
                                myForm.log("&nbsp;&nbsp;An email was sent to <a href='temp/"+report.getName()+"'>"+student.getName(DepartmentalInstructor.sNameFormatLastFist)+"</a>.");
                            } catch (Exception e) {
                                myForm.log("&nbsp;&nbsp;<font color='orange'>Unable to email <a href='temp/"+report.getName()+"'>"+student.getName(DepartmentalInstructor.sNameFormatLastFist)+"</a> -- "+e.getMessage()+".</font>");
                            }
                        }
                        myForm.log("Emails sent.");
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
                        File zipFile = ApplicationProperties.getTempFile(session.getAcademicTerm()+session.getYear()+(myForm.getExamType()==Exam.sExamTypeMidterm?"evn":"fin"), "zip");
                        myForm.log("Writing <a href='temp/"+zipFile.getName()+"'>"+session.getAcademicTerm()+session.getYear()+(myForm.getExamType()==Exam.sExamTypeMidterm?"evn":"fin")+".zip</a>...");
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
	
    
    public static class InstructorFilter implements InstructorExamReport.InstructorFilter {
        private ExamPdfReportForm iForm;
        public InstructorFilter(ExamPdfReportForm form) {
            iForm = form;
        }
        public boolean generate(ExamInstructorInfo instructor, TreeSet<ExamAssignmentInfo> exams) {
            if (iForm.getAll()) return true;
            for (int i=0;i<iForm.getSubjects().length;i++) {
                SubjectArea subject = new SubjectAreaDAO().get(Long.valueOf(iForm.getSubjects()[i]));
                for (ExamAssignmentInfo exam : exams)
                    for (ExamSectionInfo section : exam.getSections())
                        if (section.getSubject().equals(subject.getSubjectAreaAbbreviation())) return true;
            }
            return false;
        }
    }
	
    public static class StudentFilter implements StudentExamReport.StudentFilter {
        private ExamPdfReportForm iForm;
        public StudentFilter(ExamPdfReportForm form) {
            iForm = form;
        }
        public boolean generate(Student student, TreeSet<ExamSectionInfo> sections) {
            if (iForm.getAll()) return true;
            for (int i=0;i<iForm.getSubjects().length;i++) {
                SubjectArea subject = new SubjectAreaDAO().get(Long.valueOf(iForm.getSubjects()[i]));
                for (ExamSectionInfo section : sections)
                    if (section.getSubject().equals(subject.getSubjectAreaAbbreviation())) return true;
            }
            return false;
        }
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

