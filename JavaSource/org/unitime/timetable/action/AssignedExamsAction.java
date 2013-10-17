/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.action;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.ExamReportForm;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DistributionConflict;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
@Service("/assignedExams")
public class AssignedExamsAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ExamReportForm myForm = (ExamReportForm) form;

        // Check Access
        sessionContext.checkPermission(Right.AssignedExaminations);
        
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if ("Export CSV".equals(op) || "Export PDF".equals(op) || "Apply".equals(op)) {
            myForm.save(sessionContext);
        } else if ("Refresh".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        myForm.load(sessionContext);
        
        Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
        RoomAvailability.setAvailabilityWarning(request, session, myForm.getExamType(), true, false);
        
        ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
        Collection<ExamAssignmentInfo> assignedExams = null;
        if (myForm.getSubjectArea()!=null && myForm.getSubjectArea()!=0 && myForm.getExamType() != null) {
            if (solver!=null && solver.getExamTypeId().equals(myForm.getExamType()))
                assignedExams = solver.getAssignedExams(myForm.getSubjectArea());
            else
                assignedExams = Exam.findAssignedExams(session.getUniqueId(),myForm.getSubjectArea(),myForm.getExamType());
        }
        
        WebTable.setOrder(sessionContext, "assignedExams.ord",request.getParameter("ord"),1);
        
        WebTable table = getTable(true, false, myForm, assignedExams);
        
        if ("Export PDF".equals(op) && table!=null) {
        	ExportUtils.exportPDF(
            		getTable(false, true, myForm, assignedExams),
            		WebTable.getOrder(sessionContext, "assignedExams.ord"),
            		response, "assigned");
            return null;
        }
        
        if ("Export CSV".equals(op) && table!=null) {
        	ExportUtils.exportCSV(
        			getTable(false, false, myForm, assignedExams),
        			WebTable.getOrder(sessionContext, "assignedExams.ord"),
        			response, "assigned");
        	return null;
        }

        if (table!=null)
            myForm.setTable(table.printTable(WebTable.getOrder(sessionContext, "assignedExams.ord")), 10, assignedExams.size());
		
        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));
        
        LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());

        return mapping.findForward("showReport");
	}
	
    public PdfWebTable getTable(boolean html, boolean color, ExamReportForm form, Collection<ExamAssignmentInfo> exams) {
        if (exams==null || exams.isEmpty()) return null;
        String nl = (html?"<br>":"\n");
		PdfWebTable table =
            new PdfWebTable( 11,
                    "Assigned Examinations", "assignedExams.do?ord=%%",
                    new String[] {(form.getShowSections()?"Classes / Courses":"Examination"), "Period", "Room", "Seating"+nl+"Type", "Size", "Instructor", "Violated"+nl+"Distributions", "Direct", "Student N/A", ">2 A Day", "Back-To-Back"},
       				new String[] {"left", "left", "left", "center", "right", "left", "left", "right", "right", "right", "right"},
       				new boolean[] {true, true, true, true, false, true, true, false, false, false, false} );
		table.setRowStyle("white-space:nowrap");
		
        try {
        	for (ExamAssignmentInfo exam : exams) {

        	    int dc = exam.getNrDirectConflicts();
                int edc = exam.getNrNotAvailableDirectConflicts(); dc -= edc;
                String dcStr = (dc<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+dc+"</font>":(color ? "@@COLOR " + PreferenceLevel.prolog2color("P") + " " : "") +String.valueOf(dc));
                String edcStr = (edc<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+edc+"</font>":(color ? "@@COLOR " + PreferenceLevel.prolog2color("P") + " " : "") + String.valueOf(edc));
                int m2d = exam.getNrMoreThanTwoConflicts();
                String m2dStr = (m2d<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+m2d+"</font>":(color ?"@@COLOR " + PreferenceLevel.prolog2color("2") + " " : "") + String.valueOf(m2d));
                int btb = exam.getNrBackToBackConflicts();
                int dbtb = exam.getNrDistanceBackToBackConflicts();
                String btbStr = (btb<=0 && dbtb<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" (d:"+dbtb+")":"")+"</font>":(color ?"@@COLOR " + PreferenceLevel.prolog2color("1") + " " : "") +btb+(dbtb>0?" (d:"+dbtb+")":""));
                
                String rooms = "";
                if (exam.getRooms() != null)
                    for (ExamRoomInfo room : exam.getRooms()) {
                        if (rooms.length()>0) rooms += (html || !color ? ", " : "@@COLOR 000000 , ");
                        rooms += (html ? room.toString(): (color ? "@@COLOR " + PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())) + " ": "") + room.getName());
                    }
                
                String distConfs = "";
                for (DistributionConflict dist: exam.getDistributionConflicts()) {
                	if (distConfs.length()>0) distConfs += (html || !color ? ", " : "@@COLOR 000000 , ");
                	distConfs += (html ? dist.getTypeHtml() : (color ? "@@COLOR " + PreferenceLevel.prolog2color(dist.getPreference()) + " ": "") + dist.getType());
                }
                
        	    table.addLine(
        	            "onClick=\"showGwtDialog('Examination Assignment', 'examInfo.do?examId="+exam.getExamId()+"','900','90%');\"",
                        new String[] {
                            (html?"<a name='"+exam.getExamId()+"'>":"")+(form.getShowSections()?exam.getSectionName(nl):exam.getExamName())+(html?"</a>":""),
                            (html?exam.getPeriodAbbreviationWithPref():(color ? "@@COLOR " + PreferenceLevel.prolog2color(exam.getPeriodPref()) + " " : "" ) + exam.getPeriodAbbreviation()),
                            rooms,
                            (Exam.sSeatingTypeNormal==exam.getSeatingType()?"Normal":"Exam"),
                            String.valueOf(exam.getNrStudents()),
                            exam.getInstructorName(", "),
                            distConfs,
                            dcStr,
                            edcStr,
                            m2dStr,
                            btbStr
                        },
                        new Comparable[] {
                            exam,
                            exam.getPeriodOrd(),
                            exam.getRoomsName(":"),
                            exam.getSeatingType(),
                            exam.getNrStudents(),
                            exam.getInstructorName(":"),
                            exam.getDistributionConflictsList(", "),
                            dc,
                            edc,
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

