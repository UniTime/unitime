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
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ExamReportForm;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
public class AssignedExamsAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ExamReportForm myForm = (ExamReportForm) form;

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
        
        myForm.load(request.getSession());
        
        ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
        Collection<ExamAssignmentInfo> assignedExams = null;
        if (myForm.getSubjectArea()!=null && myForm.getSubjectArea()!=0) {
            if (solver!=null)
                assignedExams = solver.getAssignedExams(myForm.getSubjectArea());
            else
                assignedExams = Exam.findAssignedExams(Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId(),myForm.getSubjectArea());
        }
        
        WebTable.setOrder(request.getSession(),"assignedExams.ord",request.getParameter("ord"),1);
        
        WebTable table = getTable(Web.getUser(request.getSession()), true, myForm, assignedExams);
        
        if ("Export PDF".equals(op) && table!=null) {
            PdfWebTable pdfTable = getTable(Web.getUser(request.getSession()), false, myForm, assignedExams);
            File file = ApplicationProperties.getTempFile("assigned", "pdf");
            pdfTable.exportPdf(file, WebTable.getOrder(request.getSession(),"assignedExams.ord"));
        	request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }
        
        if (table!=null)
            myForm.setTable(table.printTable(WebTable.getOrder(request.getSession(),"assignedExams.ord")), 10, assignedExams.size());
		
        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));

        return mapping.findForward("showReport");
	}
	
    public PdfWebTable getTable(org.unitime.commons.User user, boolean html, ExamReportForm form, Collection<ExamAssignmentInfo> exams) {
        if (exams==null || exams.isEmpty()) return null;
        String instructorNameFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
        String nl = (html?"<br>":"\n");
		PdfWebTable table =
            new PdfWebTable( 10,
                    "Assigned Examinations", "assignedExams.do?ord=%%",
                    new String[] {(form.getShowSections()?"Classes / Courses":"Examination"), "Period", "Room", "Seating"+nl+"Type", "Students", "Instructor", "Direct", ">2 A Day", "Back-To-Back"},
       				new String[] {"left", "left", "left", "center", "right", "left", "right", "right", "right"},
       				new boolean[] {true, true, true, true, false, true, false, false, false} );
		table.setRowStyle("white-space:nowrap");
		
        try {
        	for (ExamAssignmentInfo exam : exams) {

        	    int dc = exam.countDirectConflicts();
                String dcStr = (dc<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+dc+"</font>":String.valueOf(dc));
                int m2d = exam.countMoreThanTwoConflicts();
                String m2dStr = (m2d<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+m2d+"</font>":String.valueOf(m2d));
                int btb = exam.countBackToBackConflicts();
                int dbtb = exam.countDistanceBackToBackConflicts();
                String btbStr = (btb<=0 && dbtb<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" (d:"+dbtb+")":"")+"</font>":btb+(dbtb>0?" (d:"+dbtb+")":""));
                String dbtbStr = (dbtb<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+dbtb+"</font>":String.valueOf(dbtb));
                
        	    table.addLine(
                        "onClick=\"document.location='examDetail.do?examId="+exam.getExamId()+"';\"",
                        new String[] {
                            (html?"<a name='"+exam.getExamId()+"'>":"")+(form.getShowSections()?exam.getSectionName(nl):exam.getExamName())+(html?"</a>":""),
                            (html?exam.getPeriodAbbreviationWithPref():exam.getPeriodAbbreviation()),
                            (html?exam.getRoomsNameWithPref(", "):exam.getRoomsName(", ")),
                            (Exam.sSeatingTypeNormal==exam.getSeatingType()?"Normal":"Exam"),
                            String.valueOf(exam.getNrStudents()),
                            exam.getInstructorName(", ", instructorNameFormat),
                            dcStr,
                            m2dStr,
                            btbStr
                        },
                        new Comparable[] {
                            exam,
                            exam.getPeriodOrd(),
                            exam.getRoomsName(":"),
                            exam.getSeatingType(),
                            exam.getNrStudents(),
                            exam.getInstructorName(":", instructorNameFormat),
                            dc,
                            m2d,
                            btb
                        },
                        exam.getExamId().toString());
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return table;
    }	
}

