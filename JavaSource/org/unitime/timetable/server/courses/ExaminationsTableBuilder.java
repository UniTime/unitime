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
package org.unitime.timetable.server.courses;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.ImageInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.MidtermPeriodPreferenceModel;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;

public class ExaminationsTableBuilder extends TableBuilder {
	
    public ExaminationsTableBuilder(SessionContext context, String backType, String backId) {
    	super(context, backType, backId);
    }

    protected CellInterface cellForPeriodPrefs(Exam exam, Set prefs){
        if (exam.getExamType().getType() == ExamType.sExamTypeMidterm) {
            MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(exam.getSession(), exam.getExamType());
            epx.load(exam);
            CellInterface cell = epx.toCellInterface();
        	if (ApplicationProperty.LegacyPeriodPreferences.isTrue()) {
        		cell.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement,'" + exam.getUniqueId() + ",-1');");
        		cell.setMouseOut("$wnd.hideGwtTimeHint();");
        	} else {
        		cell.setMouseOver("$wnd.showGwtExamPeriodPreferencesHint($wnd.lastMouseOverElement, '" + exam.getUniqueId() + "',null);");
        		cell.setMouseOut("$wnd.hideGwtPeriodPreferencesHint();");
        	}
        	return cell;
        } else if (getGridAsText()) {
        	CellInterface cell = new CellInterface();
        	for (Object o: prefs) {
        		ExamPeriodPref tp = (ExamPeriodPref)o;
        		cell.add(tp.preferenceAbbv())
                		.setColor(PreferenceLevel.prolog2color(tp.getPrefLevel().getPrefProlog()))
                		.setTitle(tp.getPrefLevel().getPrefName() + " " + tp.preferenceText())
                		.setNoWrap(true)
                		.setInline(false);
        	}
        	if (ApplicationProperty.LegacyPeriodPreferences.isTrue()) {
        		cell.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement,'" + exam.getUniqueId() + ",-1');");
        		cell.setMouseOut("$wnd.hideGwtTimeHint();");
        	} else {
        		cell.setMouseOver("$wnd.showGwtExamPeriodPreferencesHint($wnd.lastMouseOverElement, '" + exam.getUniqueId() + "',null);");
        		cell.setMouseOut("$wnd.hideGwtPeriodPreferencesHint();");
        	}
        	return cell;
        } else {
        	final PeriodPreferenceModel px = new PeriodPreferenceModel(exam.getSession(), exam.getExamType().getUniqueId());
        	px.load(exam);
        	CellInterface c = new CellInterface();
        	c.setImage(new ImageInterface().setSource("pattern?v=" + (getTimeVertival() ? 1 : 0) + "&x=" + exam.getUniqueId()).setAlt(px.toString()));
        	c.addStyle("display: inline-block;");
        	c.setAria(px.toString());
        	if (ApplicationProperty.LegacyPeriodPreferences.isTrue()) {
        		c.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement,'" + exam.getUniqueId() + ".-1');");
        		c.setMouseOut("$wnd.hideGwtTimeHint();");
        	} else {
        		c.setMouseOver("$wnd.showGwtExamPeriodPreferencesHint($wnd.lastMouseOverElement, '" + exam.getUniqueId() + "',null);");
        		c.setMouseOut("$wnd.hideGwtPeriodPreferencesHint();");
        	}
        	return c;
        }
    }
	
	public TableInterface createExamsTable(String type, Long id, ExamSolverProxy solver) {
    	if (!getSessionContext().hasPermission(Right.Examinations) && !getSessionContext().hasPermission(Right.ExaminationSchedule))
    		return null;

    	List<Exam> exams = Exam.findAllRelated(type, id);
        for (Iterator<Exam> i = exams.iterator(); i.hasNext(); ) {
        	if (!getSessionContext().hasPermission(i.next(), Right.ExaminationView))
        		i.remove();
        }
        
        TableInterface ret = new TableInterface();
        ret.setName(exams.size() == 1 ? MSG.sectionTitleExamination() : MSG.sectionTitleExaminations());

        boolean hasBack = false;
        
        Long solverType = (solver == null ? null : solver.getExamTypeId());
        boolean hasSolution = false;
        for (Exam exam: exams) {
            ExamAssignment assignment = null;
            if (exam.getExamType().getUniqueId().equals(solverType))
                assignment = solver.getAssignment(exam.getUniqueId());
            else if (exam.getAssignedPeriod()!=null)
                assignment = new ExamAssignment(exam);
            if (assignment!=null && assignment.getPeriodId()!=null) {
                hasSolution = true; break;
            }
        }

        if (getSessionContext().hasPermission(Right.Examinations)) {
            if (hasSolution) {
                LineInterface header = ret.addHeader();
                header.addCell(MSG.columnExamClassesCourses());
                header.addCell(MSG.columnExamType());
                header.addCell(MSG.columnExamLength()).setTextAlignment(Alignment.RIGHT);
                header.addCell(MSG.columnExamSeatingType()).setTextAlignment(Alignment.CENTER);
                header.addCell(MSG.columnExamSize()).setTextAlignment(Alignment.RIGHT);
                header.addCell(MSG.columnExamMaxRooms()).setTextAlignment(Alignment.RIGHT);
                header.addCell(MSG.columnExamInstructor());
                header.addCell(MSG.columnExamAssignedPeriod());
                header.addCell(MSG.columnExamAssignedRoom());
                header.addCell(MSG.columnExamStudentConflicts());                	
            	
            } else {
                LineInterface header = ret.addHeader();
                header.addCell(MSG.columnExamClassesCourses());
                header.addCell(MSG.columnExamType());
                header.addCell(MSG.columnExamLength()).setTextAlignment(Alignment.RIGHT);
                header.addCell(MSG.columnExamSeatingType()).setTextAlignment(Alignment.CENTER);
                header.addCell(MSG.columnExamSize()).setTextAlignment(Alignment.RIGHT);
                header.addCell(MSG.columnExamMaxRooms()).setTextAlignment(Alignment.RIGHT);
                header.addCell(MSG.columnExamInstructor());
                header.addCell(MSG.columnExamPeriodPreferences());
                header.addCell(MSG.columnExamRoomPreferences());
                header.addCell(MSG.columnExamDistributionPreferences());                	
            }
            for (Exam exam: new TreeSet<Exam>(exams)) {
                boolean view = getSessionContext().hasPermission(exam, Right.ExaminationDetail);
            
                CellInterface objects = new CellInterface(); objects.setInline(false);
                CellInterface instructors = new CellInterface(); instructors.setInline(false);
                CellInterface perPref = new CellInterface(); perPref.setInline(false);
                CellInterface roomPref = new CellInterface(); roomPref.setInline(false);
                CellInterface distPref = new CellInterface(); distPref.setInline(false);
                
                ExamAssignmentInfo assignment = null;
                if (exam.getExamType().getUniqueId().equals(solverType))
                    assignment = solver.getAssignmentInfo(exam.getUniqueId());
                else if (exam.getAssignedPeriod()!=null)
                    assignment = new ExamAssignmentInfo(exam);
                
                for (Enumeration e=exam.getOwnerObjects().elements();e.hasMoreElements();) {
                    Object object = e.nextElement();
                    if (object instanceof Class_)
                    	objects.add(((Class_)object).getClassLabel());
                    else if (object instanceof InstrOfferingConfig)
                    	objects.add(((InstrOfferingConfig)object).toString());
                    else if (object instanceof InstructionalOffering)
                    	objects.add(((InstructionalOffering)object).getCourseName());
                    else if (object instanceof CourseOffering)
                    	objects.add(((CourseOffering)object).getCourseName());
                    else
                    	objects.add(object.toString());
                }
                
                if (!hasSolution || assignment==null || assignment.getPeriodId()==null) {
            		for (Object pref: exam.effectivePreferences(RoomPref.class))
            			roomPref.addItem(preferenceCell((Preference)pref));
            		for (Object pref: exam.effectivePreferences(BuildingPref.class))
            			roomPref.addItem(preferenceCell((Preference)pref));
            		for (Object pref: exam.effectivePreferences(RoomFeaturePref.class))
            			roomPref.addItem(preferenceCell((Preference)pref));
            		for (Object pref: exam.effectivePreferences(RoomGroupPref.class))
            			roomPref.addItem(preferenceCell((Preference)pref));
            		perPref.addItem(cellForPeriodPrefs(exam, exam.effectivePreferences(ExamPeriodPref.class)));
            		if (!hasSolution)
            			for (Object pref: exam.effectivePreferences(DistributionPref.class))
            				distPref.addItem(preferenceCell((DistributionPref)pref));
            		else
            			distPref.add(MSG.notAssigned()).addStyle("font-style: italic;");
                } else {
                	if (view && assignment.getPeriodPref() != null) {
                		CellInterface cell = perPref.add(assignment.getPeriodAbbreviation()).setColor(PreferenceLevel.prolog2color(assignment.getPeriodPref()));
                    	if (ApplicationProperty.LegacyPeriodPreferences.isTrue()) {
                    		cell.setMouseOver("$wnd.showGwtTimeHint($wnd.lastMouseOverElement,'" + exam.getUniqueId() + "," + assignment.getPeriodId() + "');");
                    		cell.setMouseOut("$wnd.hideGwtTimeHint();");
                    	} else {
                    		cell.setMouseOver("$wnd.showGwtExamPeriodPreferencesHint($wnd.lastMouseOverElement, '" + exam.getUniqueId() + "','"+assignment.getPeriodId()+"');");
                    		cell.setMouseOut("$wnd.hideGwtPeriodPreferencesHint();");
                    	}                    		
                	} else {
                		perPref.add(assignment.getPeriodAbbreviation());
                	}
                	if (assignment.getRooms() != null)
                		for (ExamRoomInfo room: assignment.getRooms()) {
                			CellInterface cell = roomPref.add(room.getName());
                			if (view) {
                				cell.setColor(PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())));
                				cell.setMouseOver("$wnd.showGwtRoomHint($wnd.lastMouseOverElement, '" + room.getLocationId() + "', '" + PreferenceLevel.int2string(room.getPreference()) + "');");
                				cell.setMouseOut("$wnd.hideGwtRoomHint();");
                			}
                		}
                	if (view) {
                        int dc = assignment.getNrDirectConflicts();
                        distPref.setInline(false);
                        if (dc <= 0)
                        	distPref.add("0").setColor("808080");
                        else
                        	distPref.add(String.valueOf(dc)).setColor(PreferenceLevel.prolog2color("P"));
                        distPref.add(", ");
                        int m2d = assignment.getNrMoreThanTwoConflicts();
                        if (m2d <= 0)
                        	distPref.add("0").setColor("808080");
                        else
                        	distPref.add(String.valueOf(m2d)).setColor(PreferenceLevel.prolog2color("2"));
                        distPref.add(", ");
                        int btb = assignment.getNrBackToBackConflicts();
                        int dbtb = assignment.getNrDistanceBackToBackConflicts();
                        if (btb<=0 && dbtb<=0)
                        	distPref.add("0").setColor("808080");
                        else
                        	distPref.add(btb+(dbtb>0?" (d:"+dbtb+")":"")).setColor(PreferenceLevel.prolog2color("1"));
                	} else {
                		distPref.add(MSG.notApplicable()).addStyle("font-style: italic;");
                	}
                }
                
                for (DepartmentalInstructor instructor: new TreeSet<DepartmentalInstructor>(exam.getInstructors()))
                    instructors.add(instructor.getName(getInstructorNameFormat()));
                
                int nrStudents = exam.getSize();
                
                if (exam.getUniqueId().toString().equals(getBackId())) {
                	hasBack = true;
                	objects.addAnchor("back");
                }
                
                LineInterface line = ret.addLine();
                line.setURL("examDetail.action?examId="+exam.getUniqueId());
                line.addCell(objects);
                line.addCell(exam.getExamType().getLabel());
                line.addCell(exam.getLength().toString()).setTextAlignment(Alignment.RIGHT);
                line.addCell((Exam.sSeatingTypeNormal==exam.getSeatingType()?MSG.examSeatingTypeNormal():MSG.examSeatingTypeExam())).setTextAlignment(Alignment.CENTER);
                line.addCell(String.valueOf(nrStudents)).setTextAlignment(Alignment.RIGHT);
                line.addCell(exam.getMaxNbrRooms().toString()).setTextAlignment(Alignment.RIGHT);
                line.addCell(instructors);
                line.addCell(perPref);
                line.addCell(roomPref);
                line.addCell(distPref);
            }
        } else {
            if (!hasSolution) return null;
            LineInterface header = ret.addHeader();
            header.addCell(MSG.columnExamClassesCourses());
            header.addCell(MSG.columnExamType());
            header.addCell(MSG.columnExamInstructor());
            header.addCell(MSG.columnExamPeriodPreferences());
            header.addCell(MSG.columnExamRoomPreferences());
            
            for (Exam exam: new TreeSet<Exam>(exams)) {
            
                CellInterface objects = new CellInterface(); objects.setInline(false);
                CellInterface instructors = new CellInterface(); instructors.setInline(false);
                CellInterface perPref = new CellInterface(); perPref.setInline(false);
                CellInterface roomPref = new CellInterface(); roomPref.setInline(false);

                ExamAssignmentInfo assignment = null;
                if (exam.getExamType().getUniqueId().equals(solverType))
                    assignment = solver.getAssignmentInfo(exam.getUniqueId());
                else if (exam.getAssignedPeriod()!=null)
                    assignment = new ExamAssignmentInfo(exam);
                
                for (Enumeration e=exam.getOwnerObjects().elements();e.hasMoreElements();) {
                    Object object = e.nextElement();
                    if (object instanceof Class_)
                    	objects.add(((Class_)object).getClassLabel());
                    else if (object instanceof InstrOfferingConfig)
                    	objects.add(((InstrOfferingConfig)object).toString());
                    else if (object instanceof InstructionalOffering)
                    	objects.add(((InstructionalOffering)object).getCourseName());
                    else if (object instanceof CourseOffering)
                    	objects.add(((CourseOffering)object).getCourseName());
                    else
                    	objects.add(object.toString());
                }
                
                if (assignment==null || assignment.getPeriodId()==null) continue;

                perPref.add(assignment.getPeriodName());
            	if (assignment.getRooms() != null)
            		for (ExamRoomInfo room: assignment.getRooms())
            			roomPref.add(room.getName());
                
                for (DepartmentalInstructor instructor: new TreeSet<DepartmentalInstructor>(exam.getInstructors()))
                    instructors.add(instructor.getName(getInstructorNameFormat()));
                
                if (exam.getUniqueId().toString().equals(getBackId())) {
                	hasBack = true;
                	objects.addAnchor("back");
                }
                
                LineInterface line = ret.addLine();
                line.addCell(objects);
                line.addCell(exam.getExamType().getLabel());
                line.addCell(instructors);
                line.addCell(perPref);
                line.addCell(roomPref);
            }
        }        
        if (!hasBack && "Exam".equals(getBackType()))
        	ret.addHeader().getCells().get(0).addAnchor("back");
        
        for (LineInterface header: ret.getHeader())
        	for (CellInterface cell: header.getCells()) {
        		cell.setClassName("WebTableHeader");
        		cell.setText(cell.getText().replace("<br>", "\n"));
        	}
        	
        return ret;
	}
}
