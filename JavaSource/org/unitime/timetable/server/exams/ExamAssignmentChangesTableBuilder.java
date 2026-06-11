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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ExamChangesForm;
import org.unitime.timetable.form.ExamChangesForm.ExamChange;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.ExaminationsTableBuilder;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.webutil.Navigation;

public class ExamAssignmentChangesTableBuilder extends ExaminationsTableBuilder {

	public ExamAssignmentChangesTableBuilder(SessionContext context, String backType, String backId) {
		super(context, backType, backId);
	}
	
	public TableInterface generateExamAssignmentChangesTable(
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
		
		String changeType = filter.getParameterValue("changeType", ExamChange.Initial.name());
		Collection<ExamAssignmentInfo[]> changes = null;
		if (ExamChangesForm.ExamChange.Initial.name().equals(changeType))
            changes = solver.getChangesToInitial(ids);
        else if (ExamChangesForm.ExamChange.Best.name().equals(changeType))
            changes = solver.getChangesToBest(ids);
        else { //sChangeSaved
            changes = new Vector<ExamAssignmentInfo[]>();
            List<Exam> exams = null;
            if (ids.isEmpty() || ids.contains(-1l))
                exams = Exam.findAll(solver.getSessionId(), solver.getExamTypeId());
            else
                exams = Exam.findExamsOfSubjectAreas(solver.getSessionId(), ids, solver.getExamTypeId());
            exams: for (Exam exam: exams) {
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
                    for (Location location: exam.getAssignedRooms()) {
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
		
		return generateExamAssignmentChangesTable(changes, filter);
	}
	
	public TableInterface generateExamAssignmentChangesTable(Collection<ExamAssignmentInfo[]> changes, FilterInterface filter) {
		TableInterface ret = new TableInterface();
		ret.setName(XMSG.sectionNotAssingedExaminations());
		ret.setId("ExamAssignmentChanges");
		if (changes == null || changes.isEmpty()) {
			String subjectArea = filter.getParameterValue("subjectArea");
			ret.setErrorMessage(XMSG.messageNoChanges());
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
			ret.setErrorMessage(XMSG.messageNoChangesInSubject(subjects));
			return ret;
		}
		
		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		
		LineInterface header = ret.addHeader();
        header.addCell(showSection ? XMSG.colOwner() : XMSG.colExamination());
        header.addCell(XMSG.colPeriod());
        header.addCell(XMSG.colRoom());
        header.addCell(XMSG.colSeatingType()).setTextAlignment(Alignment.CENTER);
        header.addCell(XMSG.colStudents()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.colInstructor());
        header.addCell(XMSG.conflictDirect()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.conflictStudentNotAvailable()).setSortable(true).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.conflictMoreThanTwoADay()).setTextAlignment(Alignment.RIGHT);
        header.addCell(XMSG.conflictBackToBack()).setTextAlignment(Alignment.RIGHT);
        
        boolean reverse = "1".equals(filter.getParameterValue("reverse", "0"));
        
        boolean hasBack = false;
        List<Long> examIds = new ArrayList<Long>();
        if (changes instanceof List)
        	Collections.sort((List<ExamAssignmentInfo[]>)changes, new Comparator<ExamAssignmentInfo[]>() {
				@Override
				public int compare(ExamAssignmentInfo[] o1, ExamAssignmentInfo[] o2) {
					return o1[1].compareTo(o2[1]);
				}
			});
        
        for (ExamAssignmentInfo[] change: changes) {
        	ExamAssignmentInfo old = change[reverse?1:0];
    	    ExamAssignmentInfo exam = change[reverse?0:1];
    	    
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
        	
        	if (ToolBox.equals(old.getPeriodId(), exam.getPeriodId())) {
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
        	} else {
        		CellInterface cell = line.addCell();
        		if (old.getPeriodId() == null) {
        			cell.add(XMSG.notAssigned()).addStyle("font-style:italic;").setColor(PreferenceLevel.prolog2color("P"));
        		} else {
        			CellInterface period = cell.add(old.getPeriodAbbreviation());
                	if (isUsePrefStyles())
                		period.setClassName("pref-" + PreferenceLevel.prolog2char(old.getPeriodPref()));
                	else
                		period.setColor(PreferenceLevel.prolog2color(old.getPeriodPref()));
                	if (ApplicationProperty.LegacyPeriodPreferences.isTrue()) {
                		period.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement,'" + old.getExamId() + "," + old.getPeriodId() + "');");
                		period.setMouseOut("$wnd.hideGwtTimeHint();");
                	} else {
                		period.setMouseOver("$wnd.showGwtExamPeriodPreferencesHint($wnd.lastMouseOverElement, '" + old.getExamId() + "','" + old.getPeriodId() + "');");
                		period.setMouseOut("$wnd.hideGwtPeriodPreferencesHint();");
                	}
                	if (old.getPeriodIndex() != null && old.getPeriodIndex() >= 0)
                		cell.setComparable(old.getPeriodIndex());
        		}
        		cell.add(" \u2192 ");
        		if (exam.getPeriodId() == null) {
        			cell.add(XMSG.notAssigned()).addStyle("font-style:italic;").setColor(PreferenceLevel.prolog2color("P"));
        		} else {
        			CellInterface period = cell.add(exam.getPeriodAbbreviation());
                	if (isUsePrefStyles())
                		period.setClassName("pref-" + PreferenceLevel.prolog2char(exam.getPeriodPref()));
                	else
                		period.setColor(PreferenceLevel.prolog2color(exam.getPeriodPref()));
                	if (ApplicationProperty.LegacyPeriodPreferences.isTrue()) {
                		period.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement,'" + exam.getExamId() + "," + exam.getPeriodId() + "');");
                		period.setMouseOut("$wnd.hideGwtTimeHint();");
                	} else {
                		period.setMouseOver("$wnd.showGwtExamPeriodPreferencesHint($wnd.lastMouseOverElement, '" + exam.getExamId() + "','" + exam.getPeriodId() + "');");
                		period.setMouseOut("$wnd.hideGwtPeriodPreferencesHint();");
                	}
                	if (exam.getPeriodIndex() != null && exam.getPeriodIndex() >= 0)
                		cell.setComparable(exam.getPeriodIndex());
        		}
        	}
        	
        	if (exam.getMaxRooms() == 0 || ((old.getRooms() == null || old.getRooms().isEmpty()) && (exam.getRooms() == null || exam.getRooms().isEmpty()))) {
        		line.addCell();
        	} else if (ToolBox.equals(old.getRooms(),exam.getRooms())) {
        		CellInterface rooms = line.addCell();
            	if (exam.getRooms() != null)
                    for (ExamRoomInfo room : exam.getRooms()) {
                    	if (rooms.hasItems() && isSimple()) rooms.add(", ");
                    	CellInterface c = rooms.add(room.getName())
                    			.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');")
                    			.setMouseOut("$wnd.hideGwtRoomHint();");
                    	if (isUsePrefStyles())
                    		c.setClassName("pref-" + PreferenceLevel.prolog2char(PreferenceLevel.int2prolog(room.getPreference())));
                		else
                			c.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
                    	c.addStyle("display: block;");
                    }
            	rooms.addStyle("display: inline-block;");
        	} else {
        		CellInterface cell = line.addCell();
        		if (old.getRooms() == null) {
        			cell.add(XMSG.notAssigned()).addStyle("font-style:italic;").setColor(PreferenceLevel.prolog2color("P"));
        		} else {
        			CellInterface rooms = cell.add(null);
        			for (ExamRoomInfo room : old.getRooms()) {
                    	if (rooms.hasItems() && isSimple()) rooms.add(", ");
                    	CellInterface c = rooms.add(room.getName())
                    			.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');")
                    			.setMouseOut("$wnd.hideGwtRoomHint();");
                    	if (isUsePrefStyles())
                    		c.setClassName("pref-" + PreferenceLevel.prolog2char(PreferenceLevel.int2prolog(room.getPreference())));
                		else
                			c.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
                    	c.addStyle("display: block;");
                    }
        			rooms.addStyle("display: inline-block; vertical-align: top;");
        		}
        		cell.add(" \u2192 ").addStyle("vertical-align: top;");
        		if (exam.getRooms() == null) {
        			cell.add(XMSG.notAssigned()).addStyle("font-style:italic;").setColor(PreferenceLevel.prolog2color("P"));
        		} else {
        			CellInterface rooms = cell.add(null);
        			for (ExamRoomInfo room : exam.getRooms()) {
        				if (rooms.hasItems() && isSimple()) rooms.add(", ");
                    	CellInterface c = rooms.add(room.getName())
                    			.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');")
                    			.setMouseOut("$wnd.hideGwtRoomHint();");
                    	if (isUsePrefStyles())
                    		c.setClassName("pref-" + PreferenceLevel.prolog2char(PreferenceLevel.int2prolog(room.getPreference())));
                		else
                			c.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
                    	c.addStyle("display: block;");
                    }
        			rooms.addStyle("display: inline-block; vertical-align: top;");
        		}
        	}
        	
        	line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
	        	.setTextAlignment(Alignment.CENTER);
        	line.addCell(String.valueOf(exam.getNrStudents())).setComparable(exam.getNrStudents())
    			.setTextAlignment(Alignment.RIGHT);
        	line.addCell(exam.getInstructorName("\n")).addStyle("white-space:pre;");
        	
    	    int xdc = exam.getNrDirectConflicts();
            int xedc = exam.getNrNotAvailableDirectConflicts(); xdc -= xedc;
            int xm2d = exam.getNrMoreThanTwoConflicts();
            int xbtb = exam.getNrBackToBackConflicts();

            int dc = (exam.getNrDirectConflicts() - exam.getNrNotAvailableDirectConflicts()) - (old.getNrDirectConflicts() - old.getNrNotAvailableDirectConflicts());
            int edc = exam.getNrNotAvailableDirectConflicts() - old.getNrNotAvailableDirectConflicts();
            int m2d = exam.getNrMoreThanTwoConflicts() - old.getNrMoreThanTwoConflicts();
            int btb = exam.getNrBackToBackConflicts() - old.getNrBackToBackConflicts();
            int dbtb = exam.getNrDistanceBackToBackConflicts() - old.getNrDistanceBackToBackConflicts();

            CellInterface cell = line.addCell(xdc <= 0 ? "" : String.valueOf(xdc)).setComparable(xdc).setColor(xdc <= 0 ? null : PreferenceLevel.prolog2color("P"))
        		.setTextAlignment(Alignment.RIGHT);
            if (dc < 0)
            	cell.add(" (" + dc + ")").setColor(PreferenceLevel.prolog2color("R"));
            else if (dc > 0)
            	cell.add(" (+" + dc + ")").setColor(PreferenceLevel.prolog2color("P"));
            
            cell = line.addCell(xedc <= 0 ? "" : String.valueOf(xedc)).setComparable(xedc).setColor(xedc <= 0 ? null : PreferenceLevel.prolog2color("P"))
            	.setTextAlignment(Alignment.RIGHT);
            if (edc < 0)
            	cell.add(" (" + edc + ")").setColor(PreferenceLevel.prolog2color("R"));
            else if (edc > 0)
            	cell.add(" (+" + edc + ")").setColor(PreferenceLevel.prolog2color("P"));
            
            cell = line.addCell(xm2d <= 0 ? "" : String.valueOf(xm2d)).setComparable(xm2d).setColor(xm2d <= 0 ? null : PreferenceLevel.prolog2color("2"))
            	.setTextAlignment(Alignment.RIGHT);
            if (m2d < 0)
            	cell.add(" (" + m2d + ")").setColor(PreferenceLevel.prolog2color("-2"));
            else if (m2d > 0)
            	cell.add(" (+" + m2d + ")").setColor(PreferenceLevel.prolog2color("2"));
            
            cell = line.addCell(xbtb <= 0 ? "" : String.valueOf(xbtb)).setComparable(xbtb).setColor(xbtb <= 0 ? null : PreferenceLevel.prolog2color("1"))
    			.setTextAlignment(Alignment.RIGHT);
            if (btb < 0)
            	cell.add(" (" + btb +
            			(dbtb < 0 ? ", " + XMSG.prefixDistanceConclict() + dbtb : dbtb > 0 ? ", " + XMSG.prefixDistanceConclict() + "+" + dbtb : "") + 
            			")").setColor(PreferenceLevel.prolog2color("-1"));
            else if (btb > 0)
            	cell.add(" (+" + btb +
            			(dbtb < 0 ? ", " + XMSG.prefixDistanceConclict() + dbtb : dbtb > 0 ? ", " + XMSG.prefixDistanceConclict() + "+" + dbtb : "") +
            			")").setColor(PreferenceLevel.prolog2color("1"));
            else if (dbtb < 0)
            	cell.add(" (" + XMSG.prefixDistanceConclict() + dbtb + ")").setColor(PreferenceLevel.prolog2color("-1"));
            else if (btb > 0)
            	cell.add(" (" + XMSG.prefixDistanceConclict() + "+" + dbtb + ")").setColor(PreferenceLevel.prolog2color("1"));
            
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
