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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.ExamChangesForm;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
@Service("/examChanges")
public class ExamChangesAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ExamChangesForm myForm = (ExamChangesForm) form;

        // Check Access
		sessionContext.checkPermission(Right.ExaminationAssignmentChanges);
        
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if ("Export PDF".equals(op) || "Apply".equals(op)) {
            myForm.save(sessionContext);
        } else if ("Refresh".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        myForm.load(sessionContext);
        
        ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
        myForm.setNoSolver(solver==null);
        Collection<ExamAssignmentInfo[]> changes = null;
        if (myForm.getSubjectArea()!=null && myForm.getSubjectArea()!=0 && myForm.getExamType() != null) {
            if (solver!=null) {
                if (ExamChangesForm.sChangeInitial.equals(myForm.getChangeType()))
                    changes = solver.getChangesToInitial(myForm.getSubjectArea());
                else if (ExamChangesForm.sChangeBest.equals(myForm.getChangeType()))
                    changes = solver.getChangesToBest(myForm.getSubjectArea());
                else { //sChangeSaved
                    changes = new Vector<ExamAssignmentInfo[]>();
                    List exams = null;
                    if (myForm.getSubjectArea()<0)
                        exams = Exam.findAll(solver.getSessionId(), solver.getExamTypeId());
                    else
                        exams = Exam.findExamsOfSubjectArea(myForm.getSubjectArea(), solver.getExamTypeId());
                    exams: for (Iterator i=exams.iterator();i.hasNext();) {
                        Exam exam = (Exam)i.next();
                        ExamAssignment assignment = solver.getAssignment(exam.getUniqueId());
                        if (assignment==null && exam.getAssignedPeriod()==null) continue;
                        if (assignment==null || exam.getAssignedPeriod()==null) {
                            changes.add(new ExamAssignmentInfo[] {
                                    new ExamAssignmentInfo(exam),
                                    solver.getAssignmentInfo(exam.getUniqueId())});
                        } else if (!exam.getAssignedPeriod().getUniqueId().equals(assignment.getPeriodId())) {
                            changes.add(new ExamAssignmentInfo[] {
                                    new ExamAssignmentInfo(exam),
                                    solver.getAssignmentInfo(exam.getUniqueId())});
                        } else if (exam.getAssignedRooms().size()!=(assignment.getRooms()==null?0:assignment.getRooms().size())) {
                            changes.add(new ExamAssignmentInfo[] {
                                    new ExamAssignmentInfo(exam),
                                    solver.getAssignmentInfo(exam.getUniqueId())});
                        } else {
                            for (Iterator j=exam.getAssignedRooms().iterator();j.hasNext();) {
                                Location location = (Location)j.next();
                                if (!assignment.hasRoom(location.getUniqueId())) {
                                    changes.add(new ExamAssignmentInfo[] {
                                            new ExamAssignmentInfo(exam),
                                            solver.getAssignmentInfo(exam.getUniqueId())});
                                    continue exams;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        WebTable.setOrder(sessionContext,"examChanges.ord",request.getParameter("ord"),1);
        
        WebTable table = getTable(true, myForm, changes);
        
        if ("Export PDF".equals(op) && table!=null) {
        	ExportUtils.exportPDF(
        			getTable(false, myForm, changes),
        			WebTable.getOrder(sessionContext,"examChanges.ord"),
        			response, "changes");
        	return null;
        }
        
        if (table!=null)
            myForm.setTable(table.printTable(WebTable.getOrder(sessionContext,"examChanges.ord")), 9, changes.size());
		
        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));
        
        LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());

        return mapping.findForward("showReport");
	}
	
    public PdfWebTable getTable(boolean html, ExamChangesForm form, Collection<ExamAssignmentInfo[]> changes) {
        if (changes==null || changes.isEmpty()) return null;
        String nl = (html?"<br>":"\n");
		PdfWebTable table =
            new PdfWebTable( 9,
                    "Examination Assignment Changes", "examChanges.do?ord=%%",
                    new String[] {(form.getShowSections()?"Classes / Courses":"Examination"), "Period", "Room", "Seating"+nl+"Type", "Students", "Instructor", "Direct", ">2 A Day", "Back-To-Back"},
       				new String[] {"left", "left", "left", "center", "right", "left", "right", "right", "right"},
       				new boolean[] {true, true, true, true, false, true, false, false, false} );
		table.setRowStyle("white-space:nowrap");
		
        try {
        	for (ExamAssignmentInfo[] change : changes) {
        	    
        	    ExamAssignmentInfo old = change[form.getReverse()?1:0];
        	    ExamAssignmentInfo exam = change[form.getReverse()?0:1];

        	    String period = "";
        	    if (ToolBox.equals(old.getPeriodId(),exam.getPeriodId())) {
        	        period = (html?exam.getPeriodAbbreviationWithPref():exam.getPeriodAbbreviation());
        	    } else {
        	        if (html) {
        	            period = (old.getPeriodId()==null?"<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>":old.getPeriodAbbreviationWithPref());
        	            period += " &rarr; ";
        	            period += (exam.getPeriodId()==null?"<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>":exam.getPeriodAbbreviationWithPref());
        	        } else {
                        period = (old.getPeriodId()==null?"@@ITALIC not-assigned @END_ITALIC":old.getPeriodAbbreviation());
                        period += " -> ";
                        period += (exam.getPeriodId()==null?"@@ITALIC not-assigned @@END_ITALIC":exam.getPeriodAbbreviation());
        	        }
        	    }
        	    
        	    String room = "";
        	    if (ToolBox.equals(old.getRooms(),exam.getRooms())) {
        	        room = (html?exam.getRoomsNameWithPref(", "):exam.getRoomsName(", "));
        	    } else if (exam.getMaxRooms()>0) {
                    if (html) {
                        room += "<table border='0'><tr><td valign='middle'>";
                        room += (old.getPeriodId()==null?"<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>":old.getRoomsNameWithPref("<br>"));
                        room += "</td><td valign='middle'>&rarr;</td><td valign='middle'>";
                        room += (exam.getPeriodId()==null?"<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>":exam.getRoomsNameWithPref("<br>"));
                        room += "</td></tr></table>";
                    } else {
                        room = (old.getPeriodId()==null?"@@ITALIC not-assigned @END_ITALIC":old.getRoomsName(", "));
                        room += " -> ";
                        room += (exam.getPeriodId()==null?"@@ITALIC not-assigned @@END_ITALIC":exam.getRoomsName(", "));
                    }
        	    }
        	        
        	    int xdc = exam.getNrDirectConflicts();
                int dc = xdc-old.getNrDirectConflicts();
                String dcStr = (xdc<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+xdc+"</font>":String.valueOf(xdc));
                if (html && dc<0)
                    dcStr += "<font color='"+PreferenceLevel.prolog2color("R")+"'> ("+dc+")</font>";
                if (html && dc>0)
                    dcStr += "<font color='"+PreferenceLevel.prolog2color("P")+"'> (+"+dc+")</font>";
                if (!html && dc<0)
                    dcStr += " ("+dc+")";
                if (!html && dc>0)
                    dcStr += " (+"+dc+")";
                
                int xm2d = exam.getNrMoreThanTwoConflicts();
                int m2d = exam.getNrMoreThanTwoConflicts()-old.getNrMoreThanTwoConflicts();
                String m2dStr = (xm2d<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+xm2d+"</font>":String.valueOf(xm2d));
                if (html && m2d<0)
                    m2dStr += "<font color='"+PreferenceLevel.prolog2color("-2")+"'> ("+m2d+")</font>";
                if (html && m2d>0)
                    m2dStr += "<font color='"+PreferenceLevel.prolog2color("2")+"'> (+"+m2d+")</font>";
                if (!html && m2d<0)
                    m2dStr += " ("+m2d+")";
                if (!html && m2d>0)
                    m2dStr += " (+"+m2d+")";

                int xbtb = exam.getNrBackToBackConflicts();
                int btb = exam.getNrBackToBackConflicts() - old.getNrBackToBackConflicts();
                int dbtb = exam.getNrDistanceBackToBackConflicts() - old.getNrDistanceBackToBackConflicts();
                String btbStr = (xbtb<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+xbtb+"</font>":String.valueOf(xbtb));
                if (html) {
                    if (btb<0) btbStr += "<font color='"+PreferenceLevel.prolog2color("-1")+"'> ("+btb+"</font>";
                    else if (btb>0) btbStr += "<font color='"+PreferenceLevel.prolog2color("1")+"'> (+"+btb+"</font>";
                    else if (dbtb!=0) btbStr += " ("+String.valueOf(btb);
                    if (dbtb<0) btbStr += "<font color='"+PreferenceLevel.prolog2color("-1")+"'> d:"+dbtb+"</font>";
                    if (dbtb>0) btbStr += "<font color='"+PreferenceLevel.prolog2color("1")+"'> d:+"+dbtb+"</font>";
                    if (btb<0) btbStr += "<font color='"+PreferenceLevel.prolog2color("-1")+"'>)</font>";
                    else if (btb>0) btbStr += "<font color='"+PreferenceLevel.prolog2color("1")+"'>)</font>";
                    else if (dbtb!=0) btbStr += ")";
                } else {
                    if (btb<0) btbStr += " ("+btb;
                    else if (btb>0) btbStr += " (+"+btb;
                    else if (dbtb!=0) btbStr += " ("+String.valueOf(btb);
                    if (dbtb<0) btbStr += " d:"+dbtb;
                    if (dbtb>0) btbStr += " d:+"+dbtb;
                    if (btb<0) btbStr += ")";
                    else if (btb>0) btbStr += ")";
                    else if (dbtb!=0) btbStr += ")";
                }
                
        	    table.addLine(
                        "onClick=\"showGwtDialog('Examination Assignment', 'examInfo.do?examId="+exam.getExamId()+"','900','90%');\"",
                        new String[] {
                            (html?"<a name='"+exam.getExamId()+"'>":"")+(form.getShowSections()?exam.getSectionName(nl):exam.getExamName())+(html?"</a>":""),
                            period,
                            room,
                            (Exam.sSeatingTypeNormal==exam.getSeatingType()?"Normal":"Exam"),
                            String.valueOf(exam.getNrStudents()),
                            exam.getInstructorName(", "),
                            dcStr,
                            m2dStr,
                            btbStr
                        },
                        new Comparable[] {
                            exam,
                            (exam.getPeriodId()==null?old.getPeriodOrd():exam.getPeriodOrd()),
                            (exam.getPeriodId()==null?"0"+old.getRoomsName(":"):exam.getRoomsName(":")),
                            exam.getSeatingType(),
                            exam.getNrStudents(),
                            exam.getInstructorName(":"),
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

