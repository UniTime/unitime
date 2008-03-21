/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
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
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ExamAssignmentReportForm;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
public class ExamAssignmentReportAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	    ExamAssignmentReportForm myForm = (ExamAssignmentReportForm) form;

        // Check Access
        if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if ("Export PDF".equals(op) || "Apply".equals(op)) {
            myForm.save(request.getSession());
        } else if ("Refresh".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        Long sessionId = Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId();
        
        myForm.load(request.getSession());
        
        ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
        Collection<ExamAssignmentInfo> assignedExams = null;
        if (myForm.getSubjectArea()!=null && myForm.getSubjectArea()!=0) {
            if (solver!=null && solver.getExamType()==myForm.getExamType())
                assignedExams = solver.getAssignedExams(myForm.getSubjectArea());
            else
                assignedExams = Exam.findAssignedExams(sessionId,myForm.getSubjectArea(),myForm.getExamType());
        }
        
        WebTable.setOrder(request.getSession(),"examAssignmentReport["+myForm.getReport()+"].ord",request.getParameter("ord"),1);
        
        WebTable table = getTable(sessionId, Web.getUser(request.getSession()), true, myForm, assignedExams);
        
        if ("Export PDF".equals(op) && table!=null) {
            PdfWebTable pdfTable = getTable(sessionId, Web.getUser(request.getSession()), false, myForm, assignedExams);
            File file = ApplicationProperties.getTempFile("xreport", "pdf");
            pdfTable.exportPdf(file, WebTable.getOrder(request.getSession(),"examAssignmentReport["+myForm.getReport()+"].ord"));
        	request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }
        
        if ("Export CSV".equals(op) && table!=null) {
            WebTable csvTable = getTable(sessionId, Web.getUser(request.getSession()), false, myForm, assignedExams);
            File file = ApplicationProperties.getTempFile("xreport", "csv");
            csvTable.toCSVFile(WebTable.getOrder(request.getSession(),"examAssignmentReport["+myForm.getReport()+"].ord")).save(file);
            request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }

        if (table!=null)
            myForm.setTable(table.printTable(WebTable.getOrder(request.getSession(),"examAssignmentReport["+myForm.getReport()+"].ord")), table.getNrColumns(), assignedExams.size());
		
        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));

        return mapping.findForward("show");
	}
	
	public boolean match(ExamAssignmentReportForm form, String name) {
	    if (form.getFilter()==null || form.getFilter().trim().length()==0) return true;
        String n = name.toUpperCase();
        StringTokenizer stk1 = new StringTokenizer(form.getFilter().toUpperCase(),";");
        while (stk1.hasMoreTokens()) {
            StringTokenizer stk2 = new StringTokenizer(stk1.nextToken()," ,");
            boolean match = true;
            while (match && stk2.hasMoreTokens()) {
                String token = stk2.nextToken().trim();
                if (token.length()==0) continue;
                if (token.indexOf('*')>=0 || token.indexOf('?')>=0) {
                    try {
                        String tokenRegExp = "\\s+"+token.replaceAll("\\.", "\\.").replaceAll("\\?", ".+").replaceAll("\\*", ".*");
                        if (!Pattern.compile(tokenRegExp).matcher(n).find()) match = false;
                    } catch (PatternSyntaxException e) { match = false; }
                } else if (n.indexOf(token)<0) match = false;
            }
            if (match) return true;
        }
        return false;
	}
	
	public PdfWebTable getTable(Long sessionId, org.unitime.commons.User user, boolean html, ExamAssignmentReportForm form, Collection<ExamAssignmentInfo> exams) {
        if (exams==null || exams.isEmpty()) return null;
        String nl = (html?"<br>":"\n");
        SimpleDateFormat df = new SimpleDateFormat("EEE, MM/dd");
        SimpleDateFormat tf = new SimpleDateFormat("hh:mmaa");
		if (ExamAssignmentReportForm.sExamAssignmentReport.equals(form.getReport())) {
            PdfWebTable table = new PdfWebTable( 10,
                    form.getReport(), "examAssignmentReport.do?ord=%%",
                    new String[] {
                        (form.getShowSections()?"Classe / Course":"Examination"),
                        "Enrollment",
                        "Seating"+nl+"Type",
                        "Date",
                        "Time",
                        "Room",
                        "Capacity",
                        "Instructor",
                        "Student"+nl+"Conflicts",
                        "Instructor"+nl+"Conflicts"},
                    new String[] {"left", "right", "center", "left", "left", "left", "right", "left", "center", "center"},
                    new boolean[] {true, false, true, true, true, true, false, true, false, false} );
            table.setRowStyle("white-space:nowrap");
            for (ExamAssignmentInfo exam : exams) {
                if (form.getShowSections()) {
                    boolean firstSection = true; 
                    for (ExamSectionInfo section : exam.getSections()) {
                        if (!match(form, section.getName())) continue;
                        int idc = exam.getNrInstructorDirectConflicts(section);
                        String idcStr = (idc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+idc+"</font>":String.valueOf(idc));
                        int im2d = exam.getNrInstructorMoreThanTwoConflicts(section);
                        String im2dStr = (im2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+im2d+"</font>":String.valueOf(im2d));
                        int ibtb = exam.getNrInstructorBackToBackConflicts(section);
                        int idbtb = exam.getNrInstructorDistanceBackToBackConflicts(section);
                        String ibtbStr = (ibtb<=0 && idbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+ibtb+(idbtb>0?" (d:"+idbtb+")":"")+"</font>":ibtb+(idbtb>0?" (d:"+idbtb+")":""));

                        int dc = exam.getNrDirectConflicts(section);
                        String dcStr = (dc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+dc+"</font>":String.valueOf(dc));
                        int m2d = exam.getNrMoreThanTwoConflicts(section);
                        String m2dStr = (m2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+m2d+"</font>":String.valueOf(m2d));
                        int btb = exam.getNrBackToBackConflicts(section);
                        int dbtb = exam.getNrDistanceBackToBackConflicts(section);
                        String btbStr = (btb<=0 && dbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" (d:"+dbtb+")":"")+"</font>":btb+(dbtb>0?" (d:"+dbtb+")":""));

                        table.addLine(
                                "onClick=\"document.location='examDetail.do?examId="+exam.getExamId()+"';\"",
                                new String[] {
                                    (html?"<a name='"+exam.getExamId()+"'>":"")+section.getName()+(html?"</a>":""),
                                    String.valueOf(section.getNrStudents()),
                                    (Exam.sSeatingTypeNormal==exam.getSeatingType()?"Normal":"Exam"),
                                    exam.getDate(html),
                                    exam.getTime(html),
                                    exam.getRoomsName(html,", "),
                                    exam.getRoomsCapacity(html, ", "),
                                    exam.getInstructorName(", "),
                                    (dc==0&&m2d==0&&btb==0&&dbtb==0?"":dcStr+", "+m2dStr+", "+btbStr),
                                    (idc==0&&im2d==0&&ibtb==0&&idbtb==0?"":idcStr+", "+im2dStr+", "+ibtbStr),
                                },
                                new Comparable[] {
                                    new MultiComparable(section.getName(), exam),
                                    new MultiComparable(exam.getNrStudents(), section.getName(), exam),
                                    new MultiComparable(exam.getSeatingType(), section.getName(), exam),
                                    new MultiComparable(exam.getPeriodOrd(), section.getName(), exam),
                                    new MultiComparable(exam.getPeriod().getStartSlot(), section.getName(), exam),
                                    new MultiComparable(exam.getRoomsName(":"), section.getName(), exam),
                                    new MultiComparable(exam.getRoomsCapacity(false," "), section.getName(), exam),
                                    new MultiComparable(exam.getInstructorName(":"), section.getName(), exam),
                                    new MultiComparable(dc,m2d,btb,dbtb,section.getName(),exam),
                                    new MultiComparable(idc,im2d,ibtb,idbtb,section.getName(),exam)
                                },
                                (firstSection?exam.getExamId().toString():null));
                        firstSection = false;
                    }
                } else {
                    if (!match(form, exam.getExamName())) continue;
                    int idc = exam.getNrInstructorDirectConflicts();
                    String idcStr = (idc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+idc+"</font>":String.valueOf(idc));
                    int im2d = exam.getNrInstructorMoreThanTwoConflicts();
                    String im2dStr = (im2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+im2d+"</font>":String.valueOf(im2d));
                    int ibtb = exam.getNrInstructorBackToBackConflicts();
                    int idbtb = exam.getNrInstructorDistanceBackToBackConflicts();
                    String ibtbStr = (ibtb<=0 && idbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+ibtb+(idbtb>0?" (d:"+idbtb+")":"")+"</font>":ibtb+(idbtb>0?" (d:"+idbtb+")":""));

                    int dc = exam.getNrDirectConflicts();
                    String dcStr = (dc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+dc+"</font>":String.valueOf(dc));
                    int m2d = exam.getNrMoreThanTwoConflicts();
                    String m2dStr = (m2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+m2d+"</font>":String.valueOf(m2d));
                    int btb = exam.getNrBackToBackConflicts();
                    int dbtb = exam.getNrDistanceBackToBackConflicts();
                    String btbStr = (btb<=0 && dbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" (d:"+dbtb+")":"")+"</font>":btb+(dbtb>0?" (d:"+dbtb+")":""));
                    
                    table.addLine(
                            "onClick=\"document.location='examDetail.do?examId="+exam.getExamId()+"';\"",
                            new String[] {
                                (html?"<a name='"+exam.getExamId()+"'>":"")+exam.getExamName()+(html?"</a>":""),
                                String.valueOf(exam.getNrStudents()),
                                (Exam.sSeatingTypeNormal==exam.getSeatingType()?"Normal":"Exam"),
                                exam.getDate(html),
                                exam.getTime(html),
                                exam.getRoomsName(html,", "),
                                exam.getRoomsCapacity(html, ", "),
                                exam.getInstructorName(", "),
                                (dc==0&&m2d==0&&btb==0&&dbtb==0?"":dcStr+", "+m2dStr+", "+btbStr),
                                (idc==0&&im2d==0&&ibtb==0&&idbtb==0?"":idcStr+", "+im2dStr+", "+ibtbStr),
                            },
                            new Comparable[] {
                                exam,
                                new MultiComparable(exam.getNrStudents(), exam),
                                new MultiComparable(exam.getSeatingType(), exam),
                                new MultiComparable(exam.getPeriodOrd(), exam),
                                new MultiComparable(exam.getPeriod().getStartSlot(), exam),
                                new MultiComparable(exam.getRoomsName(":"), exam),
                                new MultiComparable(exam.getRoomsCapacity(false," "), exam),
                                new MultiComparable(exam.getInstructorName(":"), exam),
                                new MultiComparable(dc,m2d,btb,dbtb,exam),
                                new MultiComparable(idc,im2d,ibtb,idbtb,exam)
                            },
                            exam.getExamId().toString());
                }
            }
            return table;
		}  else if (ExamAssignmentReportForm.sRoomAssignmentReport.equals(form.getReport())) {
            PdfWebTable table = new PdfWebTable( 11,
                    form.getReport(), "examAssignmentReport.do?ord=%%",
                    new String[] {
                        "Room",
                        "Capacity",
                        "Exam"+nl+"Capacity",
                        "Date",
                        "Time",
                        (form.getShowSections()?"Classe / Course":"Examination"),
                        "Enrollment",
                        "Seating"+nl+"Type",
                        "Instructor",
                        "Student"+nl+"Conflicts",
                        "Instructor"+nl+"Conflicts"},
                    new String[] {"left", "right", "right", "left", "left", "left", "right", "center", "left", "center", "center"},
                    new boolean[] {true, false, false, true, true, true, false, true, true, false, false} );
            table.setRowStyle("white-space:nowrap");
            table.setBlankWhenSame(true);
            for (ExamAssignmentInfo exam : exams) {
                boolean match = false;
                for (ExamRoomInfo room : exam.getRooms()) {
                    if (match(form,room.getName())) { match = true; break; }
                }
                if (!match) continue;
                if (form.getShowSections()) {
                    for (ExamSectionInfo section : exam.getSections()) {
                        int idc = exam.getNrInstructorDirectConflicts(section);
                        String idcStr = (idc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+idc+"</font>":String.valueOf(idc));
                        int im2d = exam.getNrInstructorMoreThanTwoConflicts(section);
                        String im2dStr = (im2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+im2d+"</font>":String.valueOf(im2d));
                        int ibtb = exam.getNrInstructorBackToBackConflicts(section);
                        int idbtb = exam.getNrInstructorDistanceBackToBackConflicts(section);
                        String ibtbStr = (ibtb<=0 && idbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+ibtb+(idbtb>0?" (d:"+idbtb+")":"")+"</font>":ibtb+(idbtb>0?" (d:"+idbtb+")":""));

                        int dc = exam.getNrDirectConflicts(section);
                        String dcStr = (dc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+dc+"</font>":String.valueOf(dc));
                        int m2d = exam.getNrMoreThanTwoConflicts(section);
                        String m2dStr = (m2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+m2d+"</font>":String.valueOf(m2d));
                        int btb = exam.getNrBackToBackConflicts(section);
                        int dbtb = exam.getNrDistanceBackToBackConflicts(section);
                        String btbStr = (btb<=0 && dbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" (d:"+dbtb+")":"")+"</font>":btb+(dbtb>0?" (d:"+dbtb+")":""));
                        
                        boolean firstRoom = true;
                        for (ExamRoomInfo room : exam.getRooms()) {
                            if (!match(form,room.getName())) continue;
                            table.addLine(
                                    "onClick=\"document.location='examDetail.do?examId="+exam.getExamId()+"';\"",
                                    new String[] {
                                        room.getName(),
                                        String.valueOf(room.getCapacity()),
                                        String.valueOf(room.getExamCapacity()),
                                        exam.getDate(html),
                                        exam.getTime(html),
                                        (html?"<a name='"+exam.getExamId()+"'>":"")+section.getName()+(html?"</a>":""),
                                        String.valueOf(section.getNrStudents()),
                                        (Exam.sSeatingTypeNormal==exam.getSeatingType()?"Normal":"Exam"),
                                        exam.getInstructorName(", "),
                                        (dc==0&&m2d==0&&btb==0&&dbtb==0?"":dcStr+", "+m2dStr+", "+btbStr),
                                        (idc==0&&im2d==0&&ibtb==0&&idbtb==0?"":idcStr+", "+im2dStr+", "+ibtbStr),
                                    },
                                    new Comparable[] {
                                        new MultiComparable(room),
                                        new MultiComparable(room.getCapacity(), room),
                                        new MultiComparable(room.getExamCapacity(), room),
                                        new MultiComparable(room, exam.getPeriodOrd(), section.getName(), exam),
                                        new MultiComparable(room, exam.getPeriod().getStartSlot(), section.getName(), exam),
                                        new MultiComparable(room, section.getName(), exam),
                                        new MultiComparable(room, section.getNrStudents(), section.getName(), exam),
                                        new MultiComparable(room, exam.getSeatingType(), section.getName(), exam),
                                        new MultiComparable(room, exam.getInstructorName(":"), section.getName(), exam),
                                        new MultiComparable(room, dc,m2d,btb,dbtb,section.getName(),exam),
                                        new MultiComparable(room, idc,im2d,ibtb,idbtb,section.getName(),exam)
                                    },
                                    (firstRoom?exam.getExamId().toString():null));
                            firstRoom = false;
                        }
                    }
                } else {
                    int idc = exam.getNrInstructorDirectConflicts();
                    String idcStr = (idc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+idc+"</font>":String.valueOf(idc));
                    int im2d = exam.getNrInstructorMoreThanTwoConflicts();
                    String im2dStr = (im2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+im2d+"</font>":String.valueOf(im2d));
                    int ibtb = exam.getNrInstructorBackToBackConflicts();
                    int idbtb = exam.getNrInstructorDistanceBackToBackConflicts();
                    String ibtbStr = (ibtb<=0 && idbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+ibtb+(idbtb>0?" (d:"+idbtb+")":"")+"</font>":ibtb+(idbtb>0?" (d:"+idbtb+")":""));

                    int dc = exam.getNrDirectConflicts();
                    String dcStr = (dc<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+dc+"</font>":String.valueOf(dc));
                    int m2d = exam.getNrMoreThanTwoConflicts();
                    String m2dStr = (m2d<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+m2d+"</font>":String.valueOf(m2d));
                    int btb = exam.getNrBackToBackConflicts();
                    int dbtb = exam.getNrDistanceBackToBackConflicts();
                    String btbStr = (btb<=0 && dbtb<=0?"0":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" (d:"+dbtb+")":"")+"</font>":btb+(dbtb>0?" (d:"+dbtb+")":""));
                    
                    boolean firstRoom = true;
                    for (ExamRoomInfo room : exam.getRooms()) {
                        if (!match(form,room.getName())) continue;
                        table.addLine(
                                "onClick=\"document.location='examDetail.do?examId="+exam.getExamId()+"';\"",
                                new String[] {
                                    room.getName(),
                                    String.valueOf(room.getCapacity()),
                                    String.valueOf(room.getExamCapacity()),
                                    exam.getDate(html),
                                    exam.getTime(html),
                                    (html?"<a name='"+exam.getExamId()+"'>":"")+exam.getExamName()+(html?"</a>":""),
                                    String.valueOf(exam.getNrStudents()),
                                    (Exam.sSeatingTypeNormal==exam.getSeatingType()?"Normal":"Exam"),
                                    exam.getInstructorName(", "),
                                    (dc==0&&m2d==0&&btb==0&&dbtb==0?"":dcStr+", "+m2dStr+", "+btbStr),
                                    (idc==0&&im2d==0&&ibtb==0&&idbtb==0?"":idcStr+", "+im2dStr+", "+ibtbStr),
                                },
                                new Comparable[] {
                                    new MultiComparable(room),
                                    new MultiComparable(room.getCapacity(), room),
                                    new MultiComparable(room.getExamCapacity(), room),
                                    new MultiComparable(room, exam.getPeriodOrd(), exam),
                                    new MultiComparable(room, exam.getPeriod().getStartSlot(), exam),
                                    new MultiComparable(room, exam),
                                    new MultiComparable(room, exam.getNrStudents(), exam),
                                    new MultiComparable(room, exam.getSeatingType(), exam),
                                    new MultiComparable(room, exam.getInstructorName(":"), exam),
                                    new MultiComparable(room, dc,m2d,btb,dbtb,exam),
                                    new MultiComparable(room, idc,im2d,ibtb,idbtb,exam)
                                },
                                (firstRoom?exam.getExamId().toString():null));
                        firstRoom = false;
                    }
                }
            }
            return table;
		}  else if (ExamAssignmentReportForm.sPeriodUsage.equals(form.getReport())) {
            PdfWebTable table = new PdfWebTable( 7,
                    form.getReport(), "examAssignmentReport.do?ord=%%",
                    new String[] {
                        "Date",
                        "Time",
                        (form.getShowSections()?"Classes / Courses":"Examinations"),
                        "Students",
                        (form.getShowSections()?"Classes / Courses":"Examinations")+nl+"with 10+ students",
                        (form.getShowSections()?"Classes / Courses":"Examinations")+nl+"with 50+ students",
                        (form.getShowSections()?"Classes / Courses":"Examinations")+nl+"with 100+ students"},
                    new String[] {"left","left","right","right","right","right","right"},
                    new boolean[] {true, true, true, true, true, true, true} );
            table.setRowStyle("white-space:nowrap");
            int tnrExams = 0, tnrStudents = 0, tnrExams10=0, tnrExams50=0, tnrExams100=0;
		    for (Iterator i=ExamPeriod.findAll(sessionId, form.getExamType()).iterator();i.hasNext();) {
		        ExamPeriod period = (ExamPeriod)i.next();
		        String periodDate = df.format(period.getStartDate());
		        String periodTime = tf.format(period.getStartTime())+" - "+tf.format(period.getEndTime());
		        if (html && period.getPrefLevel()!=null && !PreferenceLevel.sNeutral.equals(period.getPrefLevel().getPrefProlog())) {
		            periodDate = "<font color='"+PreferenceLevel.prolog2color(period.getPrefLevel().getPrefProlog())+"'>"+periodDate+"</font>";
		            periodTime = "<font color='"+PreferenceLevel.prolog2color(period.getPrefLevel().getPrefProlog())+"'>"+periodTime+"</font>";
		        }
		        int nrExams = 0, nrStudents = 0, nrExams10=0, nrExams50=0, nrExams100=0;
		        for (ExamAssignmentInfo exam : exams) {
		            if (!period.getUniqueId().equals(exam.getPeriodId())) continue;
		            if (form.getShowSections()) {
		                for (ExamSectionInfo section : exam.getSections()) {
		                    if (!match(form,section.getName())) continue;
		                    nrExams++;
		                    nrStudents+=section.getNrStudents();
		                    if (section.getNrStudents()>10) nrExams10++;
		                    if (section.getNrStudents()>50) nrExams50++;
		                    if (section.getNrStudents()>100) nrExams100++;
		                }
		            } else {
		                if (!match(form,exam.getExamName())) continue;
		                nrExams++;
		                nrStudents+=exam.getNrStudents();
                        if (exam.getNrStudents()>10) nrExams10++;
                        if (exam.getNrStudents()>50) nrExams50++;
                        if (exam.getNrStudents()>100) nrExams100++;
		            }
		        }
		        if (nrExams==0) continue;
		        table.addLine(
		                new String[] {
		                        periodDate,
		                        periodTime,
		                        String.valueOf(nrExams),
		                        String.valueOf(nrStudents),
		                        String.valueOf(nrExams10),
		                        String.valueOf(nrExams50),
		                        String.valueOf(nrExams100)},
		                new Comparable[] {
		                        new MultiComparable(0,period),
		                        new MultiComparable(0,period.getStartSlot(), period.getDateOffset(), period),
		                        new MultiComparable(0,nrExams),
		                        new MultiComparable(0,nrStudents),
		                        new MultiComparable(0,nrExams10),
		                        new MultiComparable(0,nrExams50),
		                        new MultiComparable(0,nrExams100)
		                });
		        tnrExams += nrExams;
		        tnrExams10 += nrExams10;
		        tnrExams50 += nrExams50;
		        tnrExams100 += nrExams100;
		        tnrStudents += nrStudents;
		    }
            table.addLine(
                    new String[] {
                            (html?"<b>Totals</b>":"Totals"),
                            "",
                            (html?"<b>"+tnrExams+"</b>":String.valueOf(tnrExams)),
                            (html?"<b>"+tnrStudents+"</b>":String.valueOf(tnrStudents)),
                            (html?"<b>"+tnrExams10+"</b>":String.valueOf(tnrExams10)),
                            (html?"<b>"+tnrExams50+"</b>":String.valueOf(tnrExams50)),
                            (html?"<b>"+tnrExams100+"</b>":String.valueOf(tnrExams100))},
                    new Comparable[] {
                            new MultiComparable(1,null),
                            new MultiComparable(1,0,0, null),
                            new MultiComparable(1,tnrExams),
                            new MultiComparable(1,tnrStudents),
                            new MultiComparable(1,tnrExams10),
                            new MultiComparable(1,tnrExams50),
                            new MultiComparable(1,tnrExams100)
                    });
            return table;
		}  else if (ExamAssignmentReportForm.sNrExamsADay.equals(form.getReport())) {
            PdfWebTable table = new PdfWebTable( 8,
                    form.getReport(), "examAssignmentReport.do?ord=%%",
                    new String[] {
                        "Date",
                        "Students with"+nl+"No Exam",
                        "Students with"+nl+"One Exam",
                        "Students with"+nl+"Two Exams",
                        "Students with"+nl+"Three Exams",
                        "Students with"+nl+"Four or More Exams",
                        "Student "+nl+"Back-To-Back Exams",
                        "Student Distance"+nl+"Back-To-Back Exams"},
                    new String[] {"left", "right", "right", "right", "right", "right", "right", "right", "right"},
                    new boolean[] {true, true, true, true, true, true, true, true} );
            table.setRowStyle("white-space:nowrap");
            HashSet<Long> studentIds = new HashSet<Long>();
            Hashtable<Date,Hashtable<Long,Integer>> date2students = new Hashtable();
            Hashtable<Date,Integer> date2btb = new Hashtable();
            Hashtable<Date,Integer> date2dbtb = new Hashtable();
            for (ExamAssignmentInfo exam : exams) {
                if (!form.getShowSections() && !match(form,exam.getExamName())) continue;
                Hashtable<Long,Integer> students = date2students.get(exam.getPeriod().getStartDate());
                if (students==null) {
                    students = new Hashtable<Long, Integer>(); date2students.put(exam.getPeriod().getStartDate(),students);
                }
                for (ExamSectionInfo section : exam.getSections()) {
                    if (form.getShowSections() && !match(form,section.getName())) continue;
                    studentIds.addAll(section.getStudentIds());
                    for (Long studentId : section.getStudentIds()) {
                        Integer nrExamsThisDay = students.get(studentId);
                        students.put(studentId, 1+(nrExamsThisDay==null?0:nrExamsThisDay));
                    }
                    int btb = 0, dbtb = 0;
                    for (Iterator i=exam.getBackToBackConflicts().iterator();i.hasNext();) {
                        BackToBackConflict conf = (BackToBackConflict)i.next();
                        if (exam.getPeriod().compareTo(conf.getOtherExam().getPeriod())>=0) continue;
                        if (form.getShowSections() && form.getFilter()!=null && form.getFilter().trim().length()>0) {
                            for (Enumeration e=conf.getStudents().elements();e.hasMoreElements();) {
                                Long studentId = (Long)e.nextElement();
                                if (section.getStudentIds().contains(studentId)) {
                                    btb++;
                                    if (conf.isDistance()) dbtb++;
                                }
                            }
                        } else {
                            btb += conf.getNrStudents();
                            if (conf.isDistance()) dbtb += conf.getNrStudents(); 
                        }
                    }
                    if (btb>0)
                        date2btb.put(exam.getPeriod().getStartDate(), btb + (date2btb.get(exam.getPeriod().getStartDate())==null?0:date2btb.get(exam.getPeriod().getStartDate())));
                    if (dbtb>0)
                        date2dbtb.put(exam.getPeriod().getStartDate(), dbtb + (date2dbtb.get(exam.getPeriod().getStartDate())==null?0:date2dbtb.get(exam.getPeriod().getStartDate())));
                }
            }
            int tNoExam = 0, tOneExam = 0, tTwoExams = 0, tThreeExams = 0, tFourExams = 0, tBtb = 0, tDistBtb = 0;
            for (Map.Entry<Date,Hashtable<Long,Integer>> entry : date2students.entrySet()) {
                int noExam = 0, oneExam = 0, twoExams = 0, threeExams = 0, fourExams = 0, btb = 0, dbtb = 0;
                for (Map.Entry<Long, Integer> student : entry.getValue().entrySet()) {
                    if (student.getValue()==1) oneExam ++;
                    else if (student.getValue()==2) twoExams ++;
                    else if (student.getValue()==3) threeExams ++;
                    else if (student.getValue()>=4) fourExams ++;
                }
                noExam = studentIds.size() - oneExam - twoExams - threeExams - fourExams;
                btb = (date2btb.get(entry.getKey())==null?0:date2btb.get(entry.getKey()));
                dbtb = (date2dbtb.get(entry.getKey())==null?0:date2dbtb.get(entry.getKey()));
                table.addLine(
                        new String[] {
                                df.format(entry.getKey()),
                                String.valueOf(noExam),
                                String.valueOf(oneExam),
                                String.valueOf(twoExams),
                                String.valueOf(threeExams),
                                String.valueOf(fourExams),
                                String.valueOf(btb),
                                String.valueOf(dbtb)},
                        new Comparable[] {
                                new MultiComparable(0,entry.getKey()),
                                new MultiComparable(0,noExam),
                                new MultiComparable(0,oneExam),
                                new MultiComparable(0,twoExams),
                                new MultiComparable(0,threeExams),
                                new MultiComparable(0,fourExams),
                                new MultiComparable(0,btb),
                                new MultiComparable(0,dbtb)
                        });
                tNoExam += noExam;
                tOneExam += oneExam;
                tTwoExams += twoExams;
                tThreeExams += threeExams;
                tFourExams += fourExams;
                tBtb += btb;
                tDistBtb += dbtb;
            }
            table.addLine(
                    new String[] {
                            (html?"<b>Totals</b>":"Totals"),
                            (html?"<b>"+tNoExam+"</b>":String.valueOf(tNoExam)),
                            (html?"<b>"+tOneExam+"</b>":String.valueOf(tOneExam)),
                            (html?"<b>"+tTwoExams+"</b>":String.valueOf(tTwoExams)),
                            (html?"<b>"+tThreeExams+"</b>":String.valueOf(tThreeExams)),
                            (html?"<b>"+tFourExams+"</b>":String.valueOf(tFourExams)),
                            (html?"<b>"+tBtb+"</b>":String.valueOf(tBtb)),
                            (html?"<b>"+tDistBtb+"</b>":String.valueOf(tDistBtb))},
                    new Comparable[] {
                            new MultiComparable(1,null),
                            new MultiComparable(1,tNoExam),
                            new MultiComparable(1,tOneExam),
                            new MultiComparable(1,tTwoExams),
                            new MultiComparable(1,tThreeExams),
                            new MultiComparable(1,tFourExams),
                            new MultiComparable(1,tBtb),
                            new MultiComparable(1,tDistBtb)
                    });
            return table;
		} else if (ExamAssignmentReportForm.sRoomSplits.equals(form.getReport())) {
		    PdfWebTable table = new PdfWebTable( 13,
                    form.getReport(), "examAssignmentReport.do?ord=%%",
                    new String[] {
		                (form.getShowSections()?"Classe / Course":"Examination"),
		                "Enrollment",
		                "Seating"+nl+"Type",
                        "Date",
                        "Time",
                        "1st Room",
                        "1st Room"+nl+"Capacity",
                        "2nd Room",
                        "2nd Room"+nl+"Capacity",
                        "3rd Room",
                        "3rd Room"+nl+"Capacity",
                        "4th Room",
                        "4th Room"+nl+"Capacity"},
                    new String[] {"left","left","center","left","left","left","left","left","left","left","left","left","left"},
                    new boolean[] {true, true, true, true, true, true, true, true, true, true, true, true, true} );
		    table.setRowStyle("white-space:nowrap");
            for (ExamAssignmentInfo exam : exams) {
                if (exam.getRooms()==null || exam.getRooms().size()<=1) continue;
                if (form.getShowSections()) {
                    boolean firstSection = true; 
                    for (ExamSectionInfo section : exam.getSections()) {
                        if (!match(form, section.getName())) continue;
                        ExamRoomInfo[] rooms = new ExamRoomInfo[Math.max(4,exam.getRooms().size())];
                        int idx = 0;
                        for (ExamRoomInfo room : exam.getRooms()) rooms[idx++] = room;
                        table.addLine(
                                "onClick=\"document.location='examDetail.do?examId="+exam.getExamId()+"';\"",
                                new String[] {
                                    (html?"<a name='"+exam.getExamId()+"'>":"")+section.getName()+(html?"</a>":""),
                                    String.valueOf(section.getNrStudents()),
                                    (Exam.sSeatingTypeNormal==exam.getSeatingType()?"Normal":"Exam"),
                                    exam.getDate(html),
                                    exam.getTime(html),
                                    (rooms[0]==null?"":html?rooms[0].toString():rooms[0].getName()),
                                    (rooms[0]==null?"":html?"<font color='"+PreferenceLevel.int2color(rooms[0].getPreference())+"'>"+rooms[0].getCapacity()+"</font>":String.valueOf(rooms[0].getCapacity())),
                                    (rooms[1]==null?"":html?rooms[1].toString():rooms[1].getName()),
                                    (rooms[1]==null?"":html?"<font color='"+PreferenceLevel.int2color(rooms[1].getPreference())+"'>"+rooms[1].getCapacity()+"</font>":String.valueOf(rooms[1].getCapacity())),
                                    (rooms[2]==null?"":html?rooms[2].toString():rooms[2].getName()),
                                    (rooms[2]==null?"":html?"<font color='"+PreferenceLevel.int2color(rooms[2].getPreference())+"'>"+rooms[2].getCapacity()+"</font>":String.valueOf(rooms[2].getCapacity())),
                                    (rooms[3]==null?"":html?rooms[3].toString():rooms[2].getName()),
                                    (rooms[3]==null?"":html?"<font color='"+PreferenceLevel.int2color(rooms[3].getPreference())+"'>"+rooms[3].getCapacity()+"</font>":String.valueOf(rooms[3].getCapacity()))
                                },
                                new Comparable[] {
                                    new MultiComparable(section.getName(), exam),
                                    new MultiComparable(exam.getNrStudents(), section.getName(), exam),
                                    new MultiComparable(exam.getSeatingType(), section.getName(), exam),
                                    new MultiComparable(exam.getPeriodOrd(), section.getName(), exam),
                                    new MultiComparable(exam.getPeriod().getStartSlot(), section.getName(), exam),
                                    new MultiComparable((rooms[0]==null?"":rooms[0].getName()), section.getName(), exam),
                                    new MultiComparable((rooms[0]==null?0:rooms[0].getCapacity()), section.getName(), exam),
                                    new MultiComparable((rooms[1]==null?"":rooms[1].getName()), section.getName(), exam),
                                    new MultiComparable((rooms[1]==null?0:rooms[1].getCapacity()), section.getName(), exam),
                                    new MultiComparable((rooms[2]==null?"":rooms[2].getName()), section.getName(), exam),
                                    new MultiComparable((rooms[2]==null?0:rooms[2].getCapacity()), section.getName(), exam),
                                    new MultiComparable((rooms[3]==null?"":rooms[3].getName()), section.getName(), exam),
                                    new MultiComparable((rooms[3]==null?0:rooms[3].getCapacity()), section.getName(), exam)
                                },
                                (firstSection?exam.getExamId().toString():null));
                        firstSection = false;
                    }
                } else {
                    if (!match(form, exam.getExamName())) continue;
                    ExamRoomInfo[] rooms = new ExamRoomInfo[Math.max(4,exam.getRooms().size())];
                    int idx = 0;
                    for (ExamRoomInfo room : exam.getRooms()) rooms[idx++] = room;
                    table.addLine(
                            "onClick=\"document.location='examDetail.do?examId="+exam.getExamId()+"';\"",
                            new String[] {
                                (html?"<a name='"+exam.getExamId()+"'>":"")+exam.getExamName()+(html?"</a>":""),
                                String.valueOf(exam.getNrStudents()),
                                (Exam.sSeatingTypeNormal==exam.getSeatingType()?"Normal":"Exam"),
                                exam.getDate(html),
                                exam.getTime(html),
                                (rooms[0]==null?"":html?rooms[0].toString():rooms[0].getName()),
                                (rooms[0]==null?"":html?"<font color='"+PreferenceLevel.int2color(rooms[0].getPreference())+"'>"+rooms[0].getCapacity()+"</font>":String.valueOf(rooms[0].getCapacity())),
                                (rooms[1]==null?"":html?rooms[1].toString():rooms[1].getName()),
                                (rooms[1]==null?"":html?"<font color='"+PreferenceLevel.int2color(rooms[1].getPreference())+"'>"+rooms[1].getCapacity()+"</font>":String.valueOf(rooms[1].getCapacity())),
                                (rooms[2]==null?"":html?rooms[2].toString():rooms[2].getName()),
                                (rooms[2]==null?"":html?"<font color='"+PreferenceLevel.int2color(rooms[2].getPreference())+"'>"+rooms[2].getCapacity()+"</font>":String.valueOf(rooms[2].getCapacity())),
                                (rooms[3]==null?"":html?rooms[3].toString():rooms[2].getName()),
                                (rooms[3]==null?"":html?"<font color='"+PreferenceLevel.int2color(rooms[3].getPreference())+"'>"+rooms[3].getCapacity()+"</font>":String.valueOf(rooms[3].getCapacity()))
                            },
                            new Comparable[] {
                                new MultiComparable(exam),
                                new MultiComparable(exam.getNrStudents(), exam),
                                new MultiComparable(exam.getSeatingType(), exam),
                                new MultiComparable(exam.getPeriodOrd(), exam),
                                new MultiComparable(exam.getPeriod().getStartSlot(), exam),
                                new MultiComparable((rooms[0]==null?"":rooms[0].getName()), exam),
                                new MultiComparable((rooms[0]==null?0:rooms[0].getCapacity()), exam),
                                new MultiComparable((rooms[1]==null?"":rooms[1].getName()), exam),
                                new MultiComparable((rooms[1]==null?0:rooms[1].getCapacity()), exam),
                                new MultiComparable((rooms[2]==null?"":rooms[2].getName()), exam),
                                new MultiComparable((rooms[2]==null?0:rooms[2].getCapacity()), exam),
                                new MultiComparable((rooms[3]==null?"":rooms[3].getName()), exam),
                                new MultiComparable((rooms[3]==null?0:rooms[3].getCapacity()), exam)
                            },
                            exam.getExamId().toString());
                }
            }
            return table;
        }
		return null;
    }
	
}

