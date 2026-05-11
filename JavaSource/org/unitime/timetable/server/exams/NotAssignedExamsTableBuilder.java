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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.ExaminationsTableBuilder;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.webutil.Navigation;

public class NotAssignedExamsTableBuilder extends ExaminationsTableBuilder {

	public NotAssignedExamsTableBuilder(SessionContext context, String backType, String backId) {
		super(context, backType, backId);
	}
	
	public TableInterface generateNotAssignedExamsTable(
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
		
		Collection<ExamInfo> notAssignedExams = getNotAssignedExams(solver, getSessionContext(), examType, ids);
		return generateNotAssignedExamsTable(notAssignedExams, filter);
	}
	
	public static Collection<ExamInfo> getNotAssignedExams(ExamSolverProxy solver, SessionContext context, ExamType type, Set<Long> subjectAreaIds) {
		if (solver != null && solver.getExamTypeId().equals(type.getUniqueId())) {
			return solver.getUnassignedExams(subjectAreaIds);
		} else {
			return Exam.findUnassignedExams(context.getUser().getCurrentAcademicSessionId(), subjectAreaIds, type.getUniqueId());
		}
	}
	
	public TableInterface generateNotAssignedExamsTable(Collection<ExamInfo> notAssignedExams, FilterInterface filter) {
		TableInterface ret = new TableInterface();
		ret.setName(XMSG.sectionNotAssingedExaminations());
		ret.setId("NotAssignedExams");
		if (notAssignedExams == null || notAssignedExams.isEmpty()) {
			String subjectArea = filter.getParameterValue("subjectArea");
			ret.setErrorMessage(XMSG.messageAllExamsAreAssinged());
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
			ret.setErrorMessage(XMSG.messageAllExamsOfASubjectAreAssinged(subjects));
			return ret;
		}
		
		boolean showSection = "1".equals(filter.getParameterValue("showSections"));
		
		LineInterface header = ret.addHeader();
        header.addCell(showSection ? XMSG.colOwner() : XMSG.colExamination());
        header.addCell(MSG.columnExamLength()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.columnExamSeatingType()).setTextAlignment(Alignment.CENTER);
        header.addCell(MSG.columnExamSize()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.columnExamMaxRooms()).setTextAlignment(Alignment.RIGHT);
        header.addCell(MSG.columnExamInstructor());
        header.addCell(MSG.columnExamPeriodPreferences());
        header.addCell(MSG.columnExamRoomPreferences());
        header.addCell(MSG.columnExamDistributionPreferences());     
        
        boolean hasBack = false;
        List<Long> examIds = new ArrayList<Long>();
        if (notAssignedExams instanceof List)
        	Collections.sort((List<ExamInfo>)notAssignedExams);
        for (ExamInfo exam: notAssignedExams) {
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
        	
            CellInterface perPref = new CellInterface(); perPref.setInline(false);
            CellInterface roomPref = new CellInterface(); roomPref.setInline(false);
            CellInterface distPref = new CellInterface(); distPref.setInline(false);
            
            Exam x = exam.getExam();
    		for (Object pref: x.effectivePreferences(RoomPref.class))
    			roomPref.addItem(preferenceCell((Preference)pref));
    		for (Object pref: x.effectivePreferences(BuildingPref.class))
    			roomPref.addItem(preferenceCell((Preference)pref));
    		for (Object pref: x.effectivePreferences(RoomFeaturePref.class))
    			roomPref.addItem(preferenceCell((Preference)pref));
    		for (Object pref: x.effectivePreferences(RoomGroupPref.class))
    			roomPref.addItem(preferenceCell((Preference)pref));
    		perPref.addItem(cellForPeriodPrefs(x, x.effectivePreferences(ExamPeriodPref.class)));
    		
    		for (Object pref: x.effectivePreferences(DistributionPref.class))
				distPref.addItem(preferenceCell((DistributionPref)pref));

        	line.addCell(String.valueOf(exam.getLength())).setComparable(exam.getLength()).setTextAlignment(Alignment.RIGHT);
        	line.addCell(Exam.sSeatingTypeNormal==exam.getSeatingType() ? XMSG.seatingNormal() : XMSG.seatingExam())
        		.setTextAlignment(Alignment.CENTER);
        	line.addCell(String.valueOf(exam.getNrStudents())).setComparable(exam.getNrStudents()).setTextAlignment(Alignment.RIGHT);
        	line.addCell(String.valueOf(exam.getMaxRooms())).setComparable(exam.getMaxRooms()).setTextAlignment(Alignment.RIGHT);

        	line.addCell(exam.getInstructorName("\n")).addStyle("white-space:pre;");
        	
        	line.addCell(perPref);
            line.addCell(roomPref);
            line.addCell(distPref);
            
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
