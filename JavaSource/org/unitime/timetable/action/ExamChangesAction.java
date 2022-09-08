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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.cpsolver.ifs.util.ToolBox;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.form.ExamChangesForm;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
@Action(value = "examChanges", results = {
		@Result(name = "showReport", type = "tiles", location = "examChanges.tiles")
	})
@TilesDefinition(name = "examChanges.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Examination Assignment Changes"),
		@TilesPutAttribute(name = "body", value = "/exam/changes.jsp"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "exams")
	})
public class ExamChangesAction extends UniTimeAction<ExamChangesForm> {
	private static final long serialVersionUID = -1849884878783974335L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	
	public String execute() throws Exception {
        // Check Access
		sessionContext.checkPermission(Right.ExaminationAssignmentChanges);
		
		ExamSolverProxy solver = getExaminationSolverService().getSolver();

    	if (form == null) {
	    	form = new ExamChangesForm();
	    	form.reset();
	    	if (solver != null) form.setExamType(solver.getExamTypeId());
	    }
	    
    	if (form.getOp() != null) op = form.getOp();

    	if (MSG.actionExportPdf().equals(op) || MSG.actionExportCsv().equals(op) || MSG.buttonApply().equals(op)) {
            form.save(sessionContext);
        } else if (MSG.buttonRefresh().equals(op)) {
            form.reset();
            if (solver != null) form.setExamType(solver.getExamTypeId());
        }
        
        form.load(sessionContext);
        
        form.setNoSolver(solver==null);
        Collection<ExamAssignmentInfo[]> changes = null;
        if (form.getSubjectArea()!=null && form.getSubjectArea()!=0 && form.getExamType() != null) {
            if (solver!=null) {
                if (ExamChangesForm.ExamChange.Initial.name().equals(form.getChangeType()))
                    changes = solver.getChangesToInitial(form.getSubjectArea());
                else if (ExamChangesForm.ExamChange.Best.name().equals(form.getChangeType()))
                    changes = solver.getChangesToBest(form.getSubjectArea());
                else { //sChangeSaved
                    changes = new Vector<ExamAssignmentInfo[]>();
                    List exams = null;
                    if (form.getSubjectArea()<0)
                        exams = Exam.findAll(solver.getSessionId(), solver.getExamTypeId());
                    else
                        exams = Exam.findExamsOfSubjectArea(form.getSubjectArea(), solver.getExamTypeId());
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
        
        WebTable table = getTable(Format.html, changes);
        
        if (MSG.actionExportPdf().equals(op) && table!=null) {
        	ExportUtils.exportPDF(
        			getTable(Format.pdf, changes),
        			WebTable.getOrder(sessionContext,"examChanges.ord"),
        			response, "changes");
        	return null;
        }
        
        if (MSG.actionExportCsv().equals(op) && table!=null) {
        	ExportUtils.exportCSV(
        			getTable(Format.csv, changes),
        			WebTable.getOrder(sessionContext,"examChanges.ord"),
        			response, "changes");
        	return null;
        }
        
        if (table!=null)
            form.setTable(table.printTable(WebTable.getOrder(sessionContext,"examChanges.ord")), 9, changes.size());
		
        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));
        
        LookupTables.setupExamTypes(request, sessionContext.getUser(), DepartmentStatusType.Status.ExamTimetable);

        return "showReport";
	}
	
	private enum Format { html, csv, pdf };
	
    public PdfWebTable getTable(Format format, Collection<ExamAssignmentInfo[]> changes) {
        if (changes==null || changes.isEmpty()) return null;
        String nl = (format == Format.html?"<br>":"\n");
		PdfWebTable table =
            new PdfWebTable( 9,
                    MSG.sectExaminationAssingmentChanges(), "examChanges.action?ord=%%",
                    new String[] {
                    		(form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                    		MSG.colPeriod(),
                    		MSG.colRoom(),
                    		MSG.colSeatingType().replace("\n", nl),
                    		MSG.colStudents(),
                    		MSG.colInstructor(),
                    		MSG.conflictDirect(),
                    		MSG.conflictMoreThanTwoADay(),
                    		MSG.conflictBackToBack()},
       				new String[] {"left", "left", "left", "center", "right", "left", "right", "right", "right"},
       				new boolean[] {true, true, true, true, false, true, false, false, false} );
		table.setRowStyle("white-space:nowrap");
		
        try {
        	for (ExamAssignmentInfo[] change : changes) {
        	    
        	    ExamAssignmentInfo old = change[form.getReverse()?1:0];
        	    ExamAssignmentInfo exam = change[form.getReverse()?0:1];

        	    String period = "";
        	    if (ToolBox.equals(old.getPeriodId(),exam.getPeriodId())) {
        	        period = (format == Format.html?exam.getPeriodAbbreviationWithPref():exam.getPeriodAbbreviation());
        	    } else {
        	        if (format == Format.html) {
        	            period = (old.getPeriodId()==null?"<font color='"+PreferenceLevel.prolog2color("P")+"'><i>"+MSG.notAssigned()+"</i></font>":old.getPeriodAbbreviationWithPref());
        	            period += " &rarr; ";
        	            period += (exam.getPeriodId()==null?"<font color='"+PreferenceLevel.prolog2color("P")+"'><i>"+MSG.notAssigned()+"</i></font>":exam.getPeriodAbbreviationWithPref());
        	        } else if (format == Format.pdf) {
                        period = (old.getPeriodId()==null?"@@ITALIC "+MSG.notAssigned()+" @@END_ITALIC":old.getPeriodAbbreviation());
                        period += " -> ";
                        period += (exam.getPeriodId()==null?"@@ITALIC "+MSG.notAssigned()+" @@END_ITALIC ":exam.getPeriodAbbreviation());
        	        } else {
                        period = (old.getPeriodId()==null?MSG.notAssigned():old.getPeriodAbbreviation());
                        period += " -> ";
                        period += (exam.getPeriodId()==null?MSG.notAssigned():exam.getPeriodAbbreviation());
        	        }
        	    }
        	    
        	    String room = "";
        	    if (ToolBox.equals(old.getRooms(),exam.getRooms())) {
        	        room = (format == Format.html?exam.getRoomsNameWithPref(", "):exam.getRoomsName(", "));
        	    } else if (exam.getMaxRooms()>0) {
                    if (format == Format.html) {
                        room += "<table border='0'><tr><td valign='middle'>";
                        room += (old.getPeriodId()==null?"<font color='"+PreferenceLevel.prolog2color("P")+"'><i>"+MSG.notAssigned()+"</i></font>":old.getRoomsNameWithPref("<br>"));
                        room += "</td><td valign='middle'>&rarr;</td><td valign='middle'>";
                        room += (exam.getPeriodId()==null?"<font color='"+PreferenceLevel.prolog2color("P")+"'><i>"+MSG.notAssigned()+"</i></font>":exam.getRoomsNameWithPref("<br>"));
                        room += "</td></tr></table>";
                    } else if (format == Format.pdf) {
                        room = (old.getPeriodId()==null?"@@ITALIC "+MSG.notAssigned()+" @@END_ITALIC":old.getRoomsName(", "));
                        room += " -> ";
                        room += (exam.getPeriodId()==null?"@@ITALIC "+MSG.notAssigned()+" @@END_ITALIC ":exam.getRoomsName(", "));
                    } else {
                    	room = (old.getPeriodId()==null?MSG.notAssigned():old.getRoomsName(", "));
                        room += " -> ";
                        room += (exam.getPeriodId()==null?MSG.notAssigned():exam.getRoomsName(", "));
                    }
        	    }
        	        
        	    int xdc = exam.getNrDirectConflicts();
                int dc = xdc-old.getNrDirectConflicts();
                String dcStr = (xdc<=0?"":format == Format.html?"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+xdc+"</font>":String.valueOf(xdc));
                if (format == Format.html && dc<0)
                    dcStr += "<font color='"+PreferenceLevel.prolog2color("R")+"'> ("+dc+")</font>";
                if (format == Format.html && dc>0)
                    dcStr += "<font color='"+PreferenceLevel.prolog2color("P")+"'> (+"+dc+")</font>";
                if (format != Format.html && dc<0)
                    dcStr += " ("+dc+")";
                if (format != Format.html && dc>0)
                    dcStr += " (+"+dc+")";
                
                int xm2d = exam.getNrMoreThanTwoConflicts();
                int m2d = exam.getNrMoreThanTwoConflicts()-old.getNrMoreThanTwoConflicts();
                String m2dStr = (xm2d<=0?"":format == Format.html?"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+xm2d+"</font>":String.valueOf(xm2d));
                if (format == Format.html && m2d<0)
                    m2dStr += "<font color='"+PreferenceLevel.prolog2color("-2")+"'> ("+m2d+")</font>";
                if (format == Format.html && m2d>0)
                    m2dStr += "<font color='"+PreferenceLevel.prolog2color("2")+"'> (+"+m2d+")</font>";
                if (format != Format.html && m2d<0)
                    m2dStr += " ("+m2d+")";
                if (format != Format.html && m2d>0)
                    m2dStr += " (+"+m2d+")";

                int xbtb = exam.getNrBackToBackConflicts();
                int btb = exam.getNrBackToBackConflicts() - old.getNrBackToBackConflicts();
                int dbtb = exam.getNrDistanceBackToBackConflicts() - old.getNrDistanceBackToBackConflicts();
                String btbStr = (xbtb<=0?"":format == Format.html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+xbtb+"</font>":String.valueOf(xbtb));
                if (format == Format.html) {
                    if (btb<0) btbStr += "<font color='"+PreferenceLevel.prolog2color("-1")+"'> ("+btb+"</font>";
                    else if (btb>0) btbStr += "<font color='"+PreferenceLevel.prolog2color("1")+"'> (+"+btb+"</font>";
                    else if (dbtb!=0) btbStr += " ("+String.valueOf(btb);
                    if (dbtb<0) btbStr += "<font color='"+PreferenceLevel.prolog2color("-1")+"'> "+MSG.prefixDistanceConclict()+dbtb+"</font>";
                    if (dbtb>0) btbStr += "<font color='"+PreferenceLevel.prolog2color("1")+"'> "+MSG.prefixDistanceConclict()+"+"+dbtb+"</font>";
                    if (btb<0) btbStr += "<font color='"+PreferenceLevel.prolog2color("-1")+"'>)</font>";
                    else if (btb>0) btbStr += "<font color='"+PreferenceLevel.prolog2color("1")+"'>)</font>";
                    else if (dbtb!=0) btbStr += ")";
                } else {
                    if (btb<0) btbStr += " ("+btb;
                    else if (btb>0) btbStr += " (+"+btb;
                    else if (dbtb!=0) btbStr += " ("+String.valueOf(btb);
                    if (dbtb<0) btbStr += " "+MSG.prefixDistanceConclict()+dbtb;
                    if (dbtb>0) btbStr += " "+MSG.prefixDistanceConclict()+"+"+dbtb;
                    if (btb<0) btbStr += ")";
                    else if (btb>0) btbStr += ")";
                    else if (dbtb!=0) btbStr += ")";
                }
                
        	    table.addLine(
                        "onClick=\"showGwtDialog('" + MSG.dialogExamAssign() + "', 'examInfo.do?examId="+exam.getExamId()+"','900','90%');\"",
                        new String[] {
                            (format == Format.html?"<a name='"+exam.getExamId()+"'>":"")+(form.getShowSections()?exam.getSectionName(nl):exam.getExamName())+(format == Format.html?"</a>":""),
                            period,
                            room,
                            (Exam.sSeatingTypeNormal==exam.getSeatingType()?MSG.seatingNormal():MSG.seatingExam()),
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
        	table.addLine(new String[] {"<font color='red'>"+MSG.error(e.getMessage())+"</font>"},null);
        }
        return table;
    }	
}

