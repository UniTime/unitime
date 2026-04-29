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
package org.unitime.timetable.server.exams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.TableBuilder;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DistributionConflict;
import org.unitime.timetable.webutil.Navigation;

public class AssignedExamsTableBuilder extends TableBuilder {

	public AssignedExamsTableBuilder(SessionContext context, String backType, String backId) {
		super(context, backType, backId);
	}
	
	public TableInterface generateAssignedExamsTable(
            ExamType examType, 
            ExamSolverProxy solver,
            FilterInterface filter, 
            String[] subjectAreaIds){
		
		Set<Long> ids = new HashSet<Long>();
		for (String id: subjectAreaIds) {
			try {
				ids.add(Long.valueOf(id));
			} catch (NumberFormatException e) {
				SubjectArea subject = SubjectArea.findByAbbv(getCurrentAcademicSessionId(), id);
				if (subject != null)
					ids.add(subject.getUniqueId());
			}
		}
		
		Collection<ExamAssignmentInfo> assignedExams = getAssignedExams(solver, getSessionContext(), examType, ids);
		return generateAssignedExamsTable(assignedExams, filter);
	}
	
	public static Collection<ExamAssignmentInfo> getAssignedExams(ExamSolverProxy solver, SessionContext context, ExamType type, Set<Long> subjectAreaIds) {
		if (solver != null && solver.getExamTypeId().equals(type.getUniqueId())) {
			return solver.getAssignedExams(subjectAreaIds);
		} else {
			return Exam.findAssignedExams(context.getUser().getCurrentAcademicSessionId(), subjectAreaIds, type.getUniqueId());
		}
	}
	
	public TableInterface generateAssignedExamsTable(Collection<ExamAssignmentInfo> assignedExams, FilterInterface filter) {
		TableInterface ret = new TableInterface();
		ret.setName(XMSG.sectAssignedExaminations());
		ret.setId("AssignedExams");
		if (assignedExams == null || assignedExams.isEmpty()) {
			String subjectArea = filter.getParameterValue("subjectArea");
			ret.setErrorMessage(XMSG.messageAllExamsAreNotAssinged());
			String subjects = "";
			int count = 0;
			for (String id: subjectArea.split(",")) {
				if ("-1".equals(id)) return ret;
				SubjectArea sa = SubjectAreaDAO.getInstance().get(Long.valueOf(id));
				if (sa != null) {
					count++;
					if (count == 1)
						subjects += sa.getSubjectAreaAbbreviation();
					else if (count <= 3)
						subjects += ", " + sa.getSubjectAreaAbbreviation();
					else if (count == 4) {
						subjects += "\u2026";
						break;
					}
				}
			}
			ret.setErrorMessage(XMSG.messageAllExamsOfASubjectAreNotAssinged(subjects));
			return ret;
		}
		
		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		
		LineInterface header = ret.addHeader();
        header.addCell(showSection ? XMSG.colOwner() : XMSG.colExamination());
        header.addCell(XMSG.colPeriod()).setSortable(true);
        header.addCell(XMSG.colRoom()).setSortable(true);
        header.addCell(XMSG.colSeatingType()).setSortable(true).setTextAlignment(Alignment.CENTER);
        header.addCell(XMSG.colExamSize()).setSortable(true).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colInstructor()).setSortable(true);
        header.addCell(XMSG.colViolatedDistributions()).setSortable(true);
        header.addCell(XMSG.conflictDirect()).setSortable(true).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.conflictStudentNotAvailable()).setSortable(true).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.conflictMoreThanTwoADay()).setSortable(true).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.conflictBackToBack()).setSortable(true).setTextAlignment(Alignment.RIGHT);
        
        boolean hasBack = false;
        List<Long> examIds = new ArrayList<Long>();
        for (ExamAssignmentInfo exam: assignedExams) {
        	examIds.add(exam.getExamId());
        	LineInterface line = ret.addLine();
        	if (getSessionContext().hasPermission(exam.getExam(), Right.ExaminationAssignment)) {
        		line.setURL("examInfo.action?examId=" + exam.getExamId());
        		line.setDialog(XMSG.dialogExamAssign());
        	}
        	CellInterface owner = line.addCell().setText(showSection ? exam.getSectionName("\n") : exam.getExamName())
        			.addStyle("white-space:pre;");
        	if (exam.getExamId().toString().equals(getBackId())) {
            	hasBack = true;
            	owner.addAnchor("back");
            }
        	owner.addAnchor(String.valueOf(exam.getExamId()));
        	CellInterface period = line.addCell(exam.getPeriodAbbreviation());
        	if (isUsePrefStyles())
        		period.setClassName("pref-" + PreferenceLevel.prolog2char(exam.getPeriodPref()));
        	else
        		period.setColor(PreferenceLevel.prolog2color(exam.getPeriodPref()));
        	if (exam.getPeriodIndex() != null && exam.getPeriodIndex() >= 0)
        		period.setComparable(exam.getPeriodIndex());
        	else
        		period.setComparable(exam.getPeriod().getStartTime().getTime());
        	if (ApplicationProperty.LegacyPeriodPreferences.isTrue()) {
        		period.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement,'" + exam.getExamId() + "," + exam.getPeriodId() + "');");
        		period.setMouseOut("$wnd.hideGwtTimeHint();");
        	} else {
        		period.setMouseOver("$wnd.showGwtExamPeriodPreferencesHint($wnd.lastMouseOverElement, '" + exam.getExamId() + "','" + exam.getPeriodId() + "');");
        		period.setMouseOut("$wnd.hideGwtPeriodPreferencesHint();");
        	}
        	CellInterface rooms = line.addCell();
        	if (exam.getRooms() != null)
                for (ExamRoomInfo room : exam.getRooms()) {
                	if (rooms.hasItems()) rooms.add(", ");
                	CellInterface c = rooms.add(room.getName())
                			.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');")
                			.setMouseOut("$wnd.hideGwtRoomHint();");
                	if (isUsePrefStyles())
                		c.setClassName("pref-" + PreferenceLevel.prolog2char(PreferenceLevel.int2prolog(room.getPreference())));
            		else
            			c.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
                }
        	line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
        		.setTextAlignment(Alignment.CENTER);
        	line.addCell(String.valueOf(exam.getNrStudents())).setComparable(exam.getNrStudents())
        		.setTextAlignment(Alignment.RIGHT);
        	line.addCell(exam.getInstructorName(", "));
        	CellInterface dst = line.addCell();
        	for (DistributionConflict dist: exam.getDistributionConflicts()) {
        		if (dst.hasItems() && !isUsePrefStyles()) dst.add(", ");
        		CellInterface c = dst.add(dist.getType());
        		if (isUsePrefStyles())
        			c.setClassName("pref-" + PreferenceLevel.prolog2char(dist.getPreference()));
        		else
        			c.setColor(PreferenceLevel.prolog2color(dist.getPreference()));
        		String title = PreferenceLevel.prolog2string(dist.getPreference()) + " " + dist.getType() + " with ";
                for (Iterator<ExamInfo> i = dist.getOtherExams().iterator();i.hasNext();) {
                    ExamInfo a = i.next();
                    title += a.getExamName();
                    if (i.hasNext()) title += " and ";
                }
                c.setTitle(title);
                c.setAria(PreferenceLevel.prolog2abbv(dist.getPreference()) + " " + dist.getType());
            }
        	dst.setComparable(exam.getDistributionConflictsList(", "));
        	
    	    int dc = exam.getNrDirectConflicts();
            int edc = exam.getNrNotAvailableDirectConflicts(); dc -= edc;
            int m2d = exam.getNrMoreThanTwoConflicts();
            int btb = exam.getNrBackToBackConflicts();
            int dbtb = exam.getNrDistanceBackToBackConflicts();

        	line.addCell(dc <= 0 ? "" : String.valueOf(dc)).setComparable(dc).setColor(dc <= 0 ? null : PreferenceLevel.prolog2color("P"))
        		.setTextAlignment(Alignment.RIGHT);
        	line.addCell(edc <= 0 ? "" : String.valueOf(edc)).setComparable(edc).setColor(edc <= 0 ? null : PreferenceLevel.prolog2color("P"))
        		.setTextAlignment(Alignment.RIGHT);
        	line.addCell(m2d <= 0 ? "" : String.valueOf(m2d)).setComparable(m2d).setColor(m2d <= 0 ? null : PreferenceLevel.prolog2color("2"))
        		.setTextAlignment(Alignment.RIGHT);
        	line.addCell(btb <= 0 ? "" : btb + (dbtb > 0 ? " ("+XMSG.prefixDistanceConclict() + dbtb + ")" : ""))
        			.setComparable(btb).setColor(btb <= 0 ? null : PreferenceLevel.prolog2color("1"))
        			.setTextAlignment(Alignment.RIGHT);
        	line.setId(exam.getExamId());
        }
        
        if (!isSimple()) {
        	Navigation.set(getSessionContext(), Navigation.sInstructionalOfferingLevel, examIds);
        	ret.setNavigationLevel(Navigation.sInstructionalOfferingLevel);
        }

        if (!hasBack && "Exam".equals(getBackType()))
        	ret.addHeader().getCells().get(0).addAnchor("back");
        
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
		return ret;
	}

}
