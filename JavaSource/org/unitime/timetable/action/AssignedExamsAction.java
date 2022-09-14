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

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.form.ExamChangesForm;
import org.unitime.timetable.form.ExamReportForm;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
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
@Action(value = "assignedExams", results = {
		@Result(name = "showReport", type = "tiles", location = "assignedExams.tiles")
	})
@TilesDefinition(name = "assignedExams.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Assigned Examinations"),
		@TilesPutAttribute(name = "body", value = "/exam/assigned.jsp"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "exams")
	})
public class AssignedExamsAction extends UniTimeAction<ExamReportForm> {
	private static final long serialVersionUID = -2989357391696758181L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);

	public String execute() throws Exception {
        // Check Access
        sessionContext.checkPermission(Right.AssignedExaminations);
        
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
        
        Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
        RoomAvailability.setAvailabilityWarning(request, session, form.getExamType(), true, false);
        
        Collection<ExamAssignmentInfo> assignedExams = null;
        if (form.getSubjectArea()!=null && form.getSubjectArea()!=0 && form.getExamType() != null) {
            if (solver!=null && solver.getExamTypeId().equals(form.getExamType()))
                assignedExams = solver.getAssignedExams(form.getSubjectArea());
            else
                assignedExams = Exam.findAssignedExams(session.getUniqueId(),form.getSubjectArea(),form.getExamType());
        }
        
        WebTable.setOrder(sessionContext, "assignedExams.ord",request.getParameter("ord"),1);
        
        WebTable table = getTable(true, false, assignedExams);
        
        if (MSG.actionExportPdf().equals(op) && table!=null) {
        	ExportUtils.exportPDF(
            		getTable(false, true, assignedExams),
            		WebTable.getOrder(sessionContext, "assignedExams.ord"),
            		response, "assigned");
            return null;
        }
        
        if (MSG.actionExportCsv().equals(op) && table!=null) {
        	ExportUtils.exportCSV(
        			getTable(false, false, assignedExams),
        			WebTable.getOrder(sessionContext, "assignedExams.ord"),
        			response, "assigned");
        	return null;
        }

        if (table!=null)
            form.setTable(table.printTable(WebTable.getOrder(sessionContext, "assignedExams.ord")), 10, assignedExams.size());
		
        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));
        
        LookupTables.setupExamTypes(request, sessionContext.getUser(), DepartmentStatusType.Status.ExamTimetable);

        return "showReport";
	}
	
    public PdfWebTable getTable(boolean html, boolean color, Collection<ExamAssignmentInfo> exams) {
        if (exams==null || exams.isEmpty()) return null;
        String nl = (html?"<br>":"\n");
		PdfWebTable table =
            new PdfWebTable( 11,
                    MSG.sectAssignedExaminations(), "assignedExams.action?ord=%%",
                    new String[] {
                    		(form.getShowSections()?MSG.colOwner():MSG.colExamination()),
                    		MSG.colPeriod(),
                    		MSG.colRoom(),
                    		MSG.colSeatingType().replace("\n", nl),
                    		MSG.colExamSize(),
                    		MSG.colInstructor(),
                    		MSG.colViolatedDistributions().replace("\n", nl),
                    		MSG.conflictDirect(),
                    		MSG.conflictStudentNotAvailable(),
                    		MSG.conflictMoreThanTwoADay(),
                    		MSG.conflictBackToBack()},
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
                String btbStr = (btb<=0 && dbtb<=0?"":html?"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" ("+MSG.prefixDistanceConclict()+dbtb+")":"")+"</font>":(color ?"@@COLOR " + PreferenceLevel.prolog2color("1") + " " : "") +btb+(dbtb>0?" ("+MSG.prefixDistanceConclict()+dbtb+")":""));
                
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
        	            "onClick=\"showGwtDialog('" + MSG.dialogExamAssign() + "', 'examInfo.action?examId="+exam.getExamId()+"','900','90%');\"",
                        new String[] {
                            (html?"<a name='"+exam.getExamId()+"'>":"")+(form.getShowSections()?exam.getSectionName(nl):exam.getExamName())+(html?"</a>":""),
                            (html?exam.getPeriodAbbreviationWithPref():(color ? "@@COLOR " + PreferenceLevel.prolog2color(exam.getPeriodPref()) + " " : "" ) + exam.getPeriodAbbreviation()),
                            rooms,
                            (Exam.sSeatingTypeNormal==exam.getSeatingType()?MSG.seatingNormal():MSG.seatingExam()),
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
        	table.addLine(new String[] {"<font color='red'>"+MSG.error(e.getMessage())+"</font>"},null);
        }
        return table;
    }	
}

