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
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
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
import org.apache.struts.action.ActionMessages;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ExamPdfReportForm;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
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
        myForm.load(request.getSession());
        if (op==null) myForm.setDefaults();
        
        if ("Generate".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if (!errors.isEmpty()) {
                saveErrors(request, errors);
                return mapping.findForward("show");
            }
            Session session = Session.getCurrentAcadSession(Web.getUser(request.getSession()));
            Hashtable<String,File> reports = new Hashtable();
            TreeSet<ExamAssignmentInfo> exams = new TreeSet();
            Hashtable<SubjectArea,TreeSet<ExamAssignmentInfo>> examsPerSubject = new Hashtable();
            sLog.info("Loading exams...");
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
            for (int i=0;i<myForm.getReports().length;i++) {
                sLog.info("Generating "+myForm.getReports()[i]+"...");
                Class reportClass = ExamPdfReportForm.sRegisteredReports.get(myForm.getReports()[i]);
                String reportName = null;
                for (Map.Entry<String, Class> entry : PdfLegacyExamReport.sRegisteredReports.entrySet())
                    if (entry.getValue().equals(reportClass)) reportName = entry.getKey();
                if (reportName==null) reportName = "r"+(i+1);
                String name = session.getAcademicTerm()+session.getYear()+(myForm.getExamType()==Exam.sExamTypeEvening?"evn":"fin")+"_"+reportName;
                if (myForm.getAll()) {
                    File file = ApplicationProperties.getTempFile(name, (myForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"));
                    sLog.info("Writing "+file.getName()+"... ("+exams.size()+" exams)");
                    PdfLegacyExamReport report = (PdfLegacyExamReport)reportClass.
                        getConstructor(int.class, File.class, Session.class, int.class, SubjectArea.class, Collection.class).
                        newInstance(myForm.getModeIdx(), file, session, myForm.getExamType(), null, exams);
                    report.setDirect(myForm.getDirect());
                    report.setM2d(myForm.getM2d());
                    report.setBtb(myForm.getBtb());
                    report.setDispRooms(myForm.getDispRooms());
                    report.setNoRoom(myForm.getNoRoom());
                    report.setTotals(myForm.getTotals());
                    report.setLimit(myForm.getLimit()==null || myForm.getLimit().length()==0?-1:Integer.parseInt(myForm.getLimit()));
                    report.setRoomCode(myForm.getRoomCodes());
                    report.printReport();
                    report.close();
                    reports.put(reportName+"."+(myForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"),file);
                } else {
                    for (Map.Entry<SubjectArea, TreeSet<ExamAssignmentInfo>> entry : examsPerSubject.entrySet()) {
                        File file = ApplicationProperties.getTempFile(name+"_"+entry.getKey().getSubjectAreaAbbreviation(), (myForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"));
                        sLog.info("Writing "+file.getName()+"... ("+entry.getValue().size()+" exams)");
                        PdfLegacyExamReport report = (PdfLegacyExamReport)reportClass.
                            getConstructor(int.class, File.class, Session.class, int.class, SubjectArea.class, Collection.class).
                            newInstance(myForm.getModeIdx(), file, session, myForm.getExamType(), entry.getKey(), entry.getValue());
                        report.setDirect(myForm.getDirect());
                        report.setM2d(myForm.getM2d());
                        report.setBtb(myForm.getBtb());
                        report.setDispRooms(myForm.getDispRooms());
                        report.setNoRoom(myForm.getNoRoom());
                        report.setTotals(myForm.getTotals());
                        report.setLimit(myForm.getLimit()==null || myForm.getLimit().length()==0?-1:Integer.parseInt(myForm.getLimit()));
                        report.setRoomCode(myForm.getRoomCodes());
                        report.printReport();
                        report.close();
                        reports.put(entry.getKey().getSubjectAreaAbbreviation()+"_"+reportName+"."+(myForm.getModeIdx()==PdfLegacyExamReport.sModeText?"txt":"pdf"),file);
                    }
                }
            }
            
            if (reports.isEmpty()) {
                
            } else if (reports.size()==1) {
                request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+reports.elements().nextElement().getName());
            } else {
                FileInputStream fis = null;
                ZipOutputStream zip = null;
                byte[] buffer = new byte[32*1024];
                int len = 0;
                try {
                    File zipFile = ApplicationProperties.getTempFile(session.getAcademicTerm()+session.getYear()+(myForm.getExamType()==Exam.sExamTypeEvening?"evn":"fin"), "zip");
                    sLog.info("Writing "+zipFile+"...");
                    zip = new ZipOutputStream(new FileOutputStream(zipFile));
                    for (Map.Entry<String, File> entry : reports.entrySet()) {
                        zip.putNextEntry(new ZipEntry(entry.getKey()));
                        fis = new FileInputStream(entry.getValue());
                        while ((len=fis.read(buffer))>0) zip.write(buffer, 0, len);
                        fis.close(); fis = null;
                        zip.closeEntry();
                    }
                    zip.flush(); zip.close();
                    request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+zipFile.getName());
                } catch (Exception e) {
                    if (fis!=null) fis.close();
                    if (zip!=null) zip.close();
                    throw e;
                }
            }
        	
        }
        
        return mapping.findForward("show");
	}

}

