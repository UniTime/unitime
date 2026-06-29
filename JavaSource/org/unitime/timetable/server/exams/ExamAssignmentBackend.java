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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.cpsolver.coursett.preference.SumPreferenceCombination;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.events.RoomFilterBackend;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.events.RoomFilterBackend.LocationMatcher;
import org.unitime.timetable.gwt.client.exams.ExamAssignmentInterface.ChangeInterface;
import org.unitime.timetable.gwt.client.exams.ExamAssignmentInterface.DomainItem;
import org.unitime.timetable.gwt.client.exams.ExamAssignmentInterface.ExamAssignmentRequest;
import org.unitime.timetable.gwt.client.exams.ExamAssignmentInterface.ExamAssignmentResponse;
import org.unitime.timetable.gwt.client.exams.ExamAssignmentInterface.Operation;
import org.unitime.timetable.gwt.client.exams.ExamAssignmentInterface.RoomOrder;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.NaturalOrderComparator;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SuggestionProperties;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAccomodation.AccommodationCounter;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.solver.SolverPageBackend;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamProposedChange;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamSuggestionsInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DistributionConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.RoomAvailability;

@GwtRpcImplements(ExamAssignmentRequest.class)
public class ExamAssignmentBackend implements GwtRpcImplementation<ExamAssignmentRequest, ExamAssignmentResponse> {
	protected final static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	protected final static CourseMessages CMSG = Localization.create(CourseMessages.class);
	protected static final GwtMessages GWT = Localization.create(GwtMessages.class);
	private static Log sLog = LogFactory.getLog(ExamAssignmentBackend.class);
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	
	@Override
	public ExamAssignmentResponse execute(ExamAssignmentRequest request, SessionContext context) {
		Exam selected = ExamDAO.getInstance().get(request.getSelectedExamId());
		context.checkPermission(selected, Right.ExaminationAssignment);

		ExamSolverProxy solver = examinationSolverService.getSolver();
		if (solver != null && !solver.getExamTypeId().equals(selected.getExamType().getUniqueId()))
			solver = null;

		ExamProposedChange proposed = null;
		ExamAssignmentResponse response = new ExamAssignmentResponse();
		if (request.hasChanges()) {
			proposed = new ExamProposedChange();
			proposed.setSelected(request.getSelectedExamId());
			for (ChangeInterface ch: request.getChanges()) {
				if (solver != null) {
					ExamAssignment initial = solver.getAssignment(ch.getExamId());
					
					Long periodId = null;
					Collection<Long> rooms = null;
					if (ch.hasPeriod() && !"null".equals(ch.getPeriod()))
						periodId = Long.valueOf(ch.getPeriod());
					
					if (ch.hasRoom()) {
						rooms = new ArrayList<Long>();
						for (String token: ch.getRoom().split(":")) {
	        	            if (token.trim().isEmpty()) continue;
	        	            rooms.add(Long.valueOf(token));
	        	        }
					}
					
					if (ch.getExamId().equals(request.getSelectedExamId()) && initial != null && initial.getPeriod() != null && !"null".equals(ch.getPeriod())) {
						if (periodId == null) periodId = initial.getPeriodId();
					}
					
					proposed.addChange(solver.getAssignment(ch.getExamId(), periodId, rooms), initial);
				} else {
					Exam exam = ExamDAO.getInstance().get(ch.getExamId());
					if (exam == null) continue;
					ExamAssignment initial = new ExamAssignment(exam);
					
					ExamPeriod period = null;
					Collection<ExamRoomInfo> rooms = null;
					
					if (ch.hasPeriod() && !"null".equals(ch.getPeriod()))
						period = ExamPeriodDAO.getInstance().get(Long.valueOf(ch.getPeriod()));
					
					if (ch.hasRoom()) {
						rooms = new TreeSet<ExamRoomInfo>();
						for (String token: ch.getRoom().split(":")) {
	        	            if (token.trim().isEmpty()) continue;
	        	            Location location = LocationDAO.getInstance().get(Long.valueOf(token));
	        	            if (location != null) {
	        	            	rooms.add(new ExamRoomInfo(location, 0));
	        	            }
	        	        }
					}
					
					if (ch.getExamId().equals(request.getSelectedExamId()) && initial != null && initial.getPeriod() != null && !"null".equals(ch.getPeriod())) {
						if (period == null) period = initial.getPeriod();
					}
					
					try {
						proposed.addChange(new ExamAssignmentInfo(exam, period, rooms), initial);
					} catch (Exception e) {
						sLog.error(e.getMessage(), e);
						response.addErrorMessage(e.getMessage());
					}
				}
			}
		}
		ExamAssignmentInfo examInfo = null;
		if (solver != null) {
			examInfo = solver.getAssignmentInfo(selected.getUniqueId());
		} else {
			examInfo = new ExamAssignmentInfo(selected);
		}
		if (!request.hasChange(selected.getUniqueId()) && examInfo.getPeriodId() != null) {
			if (proposed == null) {
				proposed = new ExamProposedChange();
				proposed.setSelected(selected.getUniqueId());
			}
			if (solver != null) {
				proposed.addChange(solver.getAssignment(selected.getUniqueId(), examInfo.getPeriodId(), null), examInfo);
			} else {
				try {
					proposed.addChange(new ExamAssignmentInfo(selected, examInfo.getPeriod(), null), examInfo);
				} catch (Exception e) {
					sLog.error(e.getMessage(), e);
					response.addErrorMessage(e.getMessage());
				}
			}
		}
		
		try {
			proposed = update(proposed, examInfo, solver);
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			response.addErrorMessage(e.getMessage());
		}
		
		response.setSelectedExamId(request.getSelectedExamId());
		response.setSessionId(selected.getSession().getUniqueId());
		response.setMaxRooms(selected.getMaxNbrRooms());
		response.setMinRoomCapacity(selected.getSize());
		response.setExamName(selected.getLabel());
		response.setExamType(selected.getExamType().getReference());
		response.setProperties(getProperties(selected, context));
		if (examInfo.getPeriodId() != null)
			response.getProperties().addProperty(MSG.propAssignedPeriod()).addItem(examInfo.getPeriodCell());
        if (examInfo.getRooms() != null && !examInfo.getRooms().isEmpty()) {
        	CellInterface cell = response.getProperties().addProperty(MSG.propAssignedRoom());
        	for (ExamRoomInfo room: examInfo.getRooms())
        		cell.addItem(room.toCell().setInline(false));
        }
        
        if (request.getOperation() == Operation.SUGGESTIONS && solver != null) {
        	generateSuggestions(request, response, proposed, solver);
        	return response;
        }
        
        ExamAssignmentInfo current = null;
        DecimalFormat df = new DecimalFormat("#,##0.0");
        if (proposed != null) {
        	for (ExamAssignmentInfo assignment: proposed.getAssignments()) {
        		if (assignment.getExamId().equals(selected.getUniqueId())) {
        			current = assignment;
        			if (assignment.getPeriodId() != null)
        				response.getProperties().addProperty(MSG.propSelectedPeriod()).addItem(examInfo.getPeriodCell());
        	        if (!assignment.getRooms().isEmpty()) {
        	        	CellInterface cell = response.getProperties().addProperty(MSG.propSelectedRoom());
        	        	for (ExamRoomInfo room: assignment.getRooms())
        	        		cell.addItem(room.toCell().setInline(false));
        	        }

        		}
        	}
        	
        	response.setAssignments(generateAssignmentsTable(proposed, context));
        	
        	if (current != null && current.getNrDistributionConflicts() > 0) {
            	TableInterface table = new TableInterface();
            	table.setMultiRows(true);
            	table.setName(MSG.sectViolatedDistributionPreferencesForExam(current.getExamName()));
            	LineInterface header = table.addHeader();
            	header.addCell(MSG.colPreference());
            	header.addCell(MSG.colDistribution());
            	header.addCell(MSG.colExamination());
            	header.addCell(MSG.colPeriod());
            	header.addCell(MSG.colRoom());
            	for (CellInterface h: header.getCells())
                	h.setClassName("WebTableHeader");
            	long id = 0;
            	for (DistributionConflict dc : current.getDistributionConflicts()) {
            		int idx = 0;
            		for (ExamInfo a: dc.getOtherExams()) {
                		LineInterface line = table.addLine();
                		line.setId(id);
                		line.setURL("#id=" + a.getExamId());
                		if (idx == 0) {
                			line.addCell(PreferenceLevel.prolog2string(dc.getPreference()))
                				.setColor(PreferenceLevel.prolog2color(dc.getPreference()))
                				.setRowSpan(dc.getOtherExams().size());
                			line.addCell(dc.getType())
                				.setColor(PreferenceLevel.prolog2color(dc.getPreference()))
                				.setRowSpan(dc.getOtherExams().size());
                		}
                		line.addCell(a.getExamNameCell());
                		if (a instanceof ExamAssignment) {
                			ExamAssignment ea = (ExamAssignment)a;
                			line.addCell(ea.getPeriodCell());
                			line.addCell(ea.toRoomCell());
                		} else {
                			line.addCell();
                			line.addCell();
                		}
                		idx++;
            		}
            		id++;
            	}
            	response.setDistributionConflicts(table);
        	}
        	if (current != null && current.getHasConflicts()) {
        		TableInterface table = new TableInterface();
        		table.setMultiRows(true);
            	table.setName(MSG.sectStudentConflictsForExam(current.getExamName()));
            	LineInterface header = table.addHeader();
            	header.addCell(MSG.colStudents());
            	header.addCell(MSG.colConflict());
            	header.addCell(MSG.colExamination());
            	header.addCell(MSG.colPeriod());
            	header.addCell(MSG.colRoom());
            	for (CellInterface h: header.getCells())
                	h.setClassName("WebTableHeader");
            	long id = 0;
            	for (DirectConflict dc : current.getDirectConflicts()) {
            		LineInterface line = table.addLine();
            		line.setId(id); id++;
            		if (dc.getOtherExam() != null) {
            			line.setURL("#id=" + dc.getOtherExam().getExamId());
            			line.addCell(String.valueOf(dc.getNrStudents()))
            				.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sProhibited))
            				.addStyle("font-weight: bold;");
            			line.addCell(MSG.conflictDirect())
            				.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sProhibited))
            				.addStyle("font-weight: bold;");
            			line.addCell(dc.getOtherExam().getExamNameCell());
            			line.addCell(dc.getOtherExam().getPeriodCell());
            			line.addCell(dc.getOtherExam().toRoomCell());
            		} else {
            			line.addCell(String.valueOf(dc.getNrStudents()))
            				.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sProhibited))
        					.addStyle("font-weight: bold;");
            			line.addCell(dc.isOtherClass() ? MSG.typeClass() : MSG.typeEvent())
        					.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sProhibited))
        					.addStyle("font-weight: bold;");
            			if (dc.getOtherEventName() != null) {
            				line.addCell(dc.getOtherEventName());
            				line.addCell(dc.getOtherEventDate() + " " + dc.getOtherEventTime());
            				line.addCell(dc.getOtherEventRoom());
            			} else {
            				line.addCell(MSG.infoNotAvailableForUnknownReason()).setColSpan(3);
            			}
            		}
            	}
            	for (MoreThanTwoADayConflict m2d : current.getMoreThanTwoADaysConflicts()) {
            		int idx = 0;
            		for (ExamAssignment a: m2d.getOtherExams()) {
                		LineInterface line = table.addLine();
                		line.setId(id);
                		line.setURL("#id=" + a.getExamId());
                		if (idx == 0) {
                			line.addCell(String.valueOf(m2d.getNrStudents()))
            					.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sStronglyDiscouraged))
            					.addStyle("font-weight: bold;")
            					.setRowSpan(m2d.getOtherExams().size());
                			line.addCell(MSG.conflictMoreThanTwoADay())
        						.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sStronglyDiscouraged))
        						.addStyle("font-weight: bold;")
        						.setRowSpan(m2d.getOtherExams().size());
                		}
            			line.addCell(a.getExamNameCell());
            			line.addCell(a.getPeriodCell());
            			line.addCell(a.toRoomCell());
            			idx++;
            		}
            		id++;
            	}
            	for (BackToBackConflict btb : current.getBackToBackConflicts()) {
            		LineInterface line = table.addLine();
            		line.setId(id); id++;
            		line.setURL("#id=" + btb.getOtherExam().getExamId());
            		line.addCell(String.valueOf(btb.getNrStudents()))
            			.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sDiscouraged))
            			.addStyle("font-weight: bold;");
            		CellInterface d = line.addCell(MSG.conflictBackToBack())
            			.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sDiscouraged))
    					.addStyle("font-weight: bold;");
            		if (btb.isDistance())
            			d.add(" (" + df.format(btb.getDistance()) + " m)");
        			line.addCell(btb.getOtherExam().getExamNameCell());
        			line.addCell(btb.getOtherExam().getPeriodCell());
        			line.addCell(btb.getOtherExam().toRoomCell());
            	}
            	response.setStudentConflicts(table);
        	}
        	if (current != null && current.getHasInstructorConflicts()) {
        		TableInterface table = new TableInterface();
        		table.setMultiRows(true);
            	table.setName(MSG.sectInstructorConflictsForExam(current.getExamName()));
            	LineInterface header = table.addHeader();
            	header.addCell(MSG.colStudents());
            	header.addCell(MSG.colConflict());
            	header.addCell(MSG.colExamination());
            	header.addCell(MSG.colPeriod());
            	header.addCell(MSG.colRoom());
            	for (CellInterface h: header.getCells())
                	h.setClassName("WebTableHeader");
            	long id = 0;
            	for (DirectConflict dc : current.getInstructorDirectConflicts()) {
            		LineInterface line = table.addLine();
            		line.setId(id); id++;
            		if (dc.getOtherExam() != null) {
            			line.setURL("#id=" + dc.getOtherExam().getExamId());
            			line.addCell(String.valueOf(dc.getNrStudents()))
            				.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sProhibited))
            				.addStyle("font-weight: bold;");
            			line.addCell(MSG.conflictDirect())
            				.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sProhibited))
            				.addStyle("font-weight: bold;");
            			line.addCell(dc.getOtherExam().getExamNameCell());
            			line.addCell(dc.getOtherExam().getPeriodCell());
            			line.addCell(dc.getOtherExam().toRoomCell());
            		} else {
            			line.addCell(String.valueOf(dc.getNrStudents()))
            				.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sProhibited))
        					.addStyle("font-weight: bold;");
            			line.addCell(dc.isOtherClass() ? MSG.typeClass() : MSG.typeEvent())
        					.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sProhibited))
        					.addStyle("font-weight: bold;");
            			if (dc.getOtherEventName() != null) {
            				line.addCell(dc.getOtherEventName());
            				line.addCell(dc.getOtherEventDate() + " " + dc.getOtherEventTime());
            				line.addCell(dc.getOtherEventRoom());
            			} else {
            				line.addCell(MSG.infoNotAvailableForUnknownReason()).setColSpan(3);
            			}
            		}
            	}
            	for (MoreThanTwoADayConflict m2d : current.getInstructorMoreThanTwoADaysConflicts()) {
            		int idx = 0;
            		for (ExamAssignment a: m2d.getOtherExams()) {
                		LineInterface line = table.addLine();
                		line.setId(id);
                		line.setURL("#id=" + a.getExamId());
                		if (idx == 0) {
                			line.addCell(String.valueOf(m2d.getNrStudents()))
            					.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sStronglyDiscouraged))
            					.addStyle("font-weight: bold;")
            					.setRowSpan(m2d.getOtherExams().size());
                			line.addCell(MSG.conflictMoreThanTwoADay())
        						.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sStronglyDiscouraged))
        						.addStyle("font-weight: bold;")
        						.setRowSpan(m2d.getOtherExams().size());
                		}
            			line.addCell(a.getExamNameCell());
            			line.addCell(a.getPeriodCell());
            			line.addCell(a.toRoomCell());
            			idx++;
            		}
            		id++;
            	}
            	for (BackToBackConflict btb : current.getInstructorBackToBackConflicts()) {
            		LineInterface line = table.addLine();
            		line.setId(id); id++;
            		line.setURL("#id=" + btb.getOtherExam().getExamId());
            		line.addCell(String.valueOf(btb.getNrStudents()))
            			.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sDiscouraged))
            			.addStyle("font-weight: bold;");
            		CellInterface d= line.addCell(MSG.conflictBackToBack())
            			.setColor(PreferenceLevel.prolog2color(PreferenceLevel.sDiscouraged))
    					.addStyle("font-weight: bold;");
            		if (btb.isDistance())
            			d.add(" (" + df.format(btb.getDistance()) + " m)");
        			line.addCell(btb.getOtherExam().getExamNameCell());
        			line.addCell(btb.getOtherExam().getPeriodCell());
        			line.addCell(btb.getOtherExam().toRoomCell());
            	}
            	response.setInstructorConflicts(table);
        	}
        }
        
        try {
    		TableInterface table = new TableInterface();
        	table.setName(MSG.sectAvailablePeriodsForExam(selected.getLabel()));
        	table.setId("ExamAssignment.Periods");
        	LineInterface header = table.addHeader();
        	header.addCell(MSG.colAvailablePeriod());
        	header.addCell(MSG.colViolatedDistributions());
        	header.addCell(MSG.colStudentDirectConflicts()).setTextAlignment(Alignment.RIGHT);
        	header.addCell(MSG.colStudentMoreThanTwoExamsADayConflicts()).setTextAlignment(Alignment.RIGHT);
        	header.addCell(MSG.colStudentBackToBackConflicts()).setTextAlignment(Alignment.RIGHT);
        	header.addCell(MSG.colInstructorDirectConflicts()).setTextAlignment(Alignment.RIGHT);
        	header.addCell(MSG.colInstructorMoreThanTwoExamsADayConflicts()).setTextAlignment(Alignment.RIGHT);
        	header.addCell(MSG.colInstructorBackToBackConflicts()).setTextAlignment(Alignment.RIGHT);
        	for (CellInterface h: header.getCells()) {
            	h.setClassName("WebTableHeader");     
            	h.setSortable(true);
        	}
        	for (ExamAssignmentInfo period: getPeriods(selected, proposed, solver)) {
        		LineInterface line = table.addLine();
        		line.setId(period.getPeriodId());
        		line.setURL("#period=" + period.getPeriodId());
        		if (current != null && current.getPeriodId().equals(period.getPeriodId()))
        			line.setClassName("unitime-TableRowSelected");
        		CellInterface per = period.getPeriodCell().setComparable(period.getPeriodOrd()).setNoWrap(true);
        		line.addCell(per);
        		if (period.getPeriodId().equals(examInfo.getPeriodId()))
        			per.setClassName("assigned-period");
        		CellInterface dist = line.addCell();
        		for (DistributionConflict dc: period.getDistributionConflicts())
        			dist.addItem(dc.getTypeCell().setInline(false));
        		line.addCell(dc2cell(period.getNrDirectConflicts(), (current==null?0:period.getNrDirectConflicts()-current.getNrDirectConflicts()))
        			.setComparable(period.getNrDirectConflicts()));
        		line.addCell(md2cell(period.getNrMoreThanTwoConflicts(), (current==null?0:period.getNrMoreThanTwoConflicts()-current.getNrMoreThanTwoConflicts()))
        				.setComparable(period.getNrMoreThanTwoConflicts()));
        		line.addCell(btb2cell(period.getNrBackToBackConflicts(), (current==null?0:period.getNrBackToBackConflicts()-current.getNrBackToBackConflicts()),
        				period.getNrDistanceBackToBackConflicts(), (current==null?0:period.getNrDistanceBackToBackConflicts()-current.getNrDistanceBackToBackConflicts()))
        				.setComparable(period.getNrBackToBackConflicts()));
        		line.addCell(dc2cell(period.getNrInstructorDirectConflicts(), (current==null?0:period.getNrInstructorDirectConflicts()-current.getNrInstructorDirectConflicts()))
        				.setComparable(period.getNrInstructorDirectConflicts()));
        		line.addCell(md2cell(period.getNrInstructorMoreThanTwoConflicts(), (current==null?0:period.getNrInstructorMoreThanTwoConflicts()-current.getNrInstructorMoreThanTwoConflicts()))
        				.setComparable(period.getNrInstructorMoreThanTwoConflicts()));
        		line.addCell(btb2cell(period.getNrInstructorBackToBackConflicts(), (current==null?0:period.getNrInstructorBackToBackConflicts()-current.getNrInstructorBackToBackConflicts()),
        				period.getNrInstructorDistanceBackToBackConflicts(), (current==null?0:period.getNrInstructorDistanceBackToBackConflicts()-current.getNrInstructorDistanceBackToBackConflicts()))
        				.setComparable(period.getNrInstructorBackToBackConflicts()));
        	}
        	if (current != null && current.getPeriodId() != null) {
        		LineInterface line = table.addLine();
        		line.setURL("#period=null");
        		line.addCell(new CellInterface().add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;").setComparable(9999));
        		line.addCell();
        		line.addCell(dc2cell(0, -current.getNrDirectConflicts()).setComparable(0));
        		line.addCell(md2cell(0, -current.getNrMoreThanTwoConflicts()).setComparable(0));
        		line.addCell(btb2cell(0, -current.getNrBackToBackConflicts(), 0, -current.getNrDistanceBackToBackConflicts()).setComparable(0));
        		line.addCell(dc2cell(0, -current.getNrInstructorDirectConflicts()).setComparable(0));
        		line.addCell(md2cell(0, -current.getNrInstructorMoreThanTwoConflicts()).setComparable(0));
        		line.addCell(btb2cell(0, -current.getNrInstructorBackToBackConflicts(), 0, -current.getNrInstructorBackToBackConflicts()).setComparable(0));
        	}
        	response.setPeriods(table);
        } catch (GwtRpcException e) {
        	sLog.error("Failed to enumerate possible periods: " + e.getMessage(), e);
        	response.addPeriodsErrorMessage(e.getMessage());
        }
        
        if (current != null && current.getPeriodId() != null && current.getMaxRooms() > 0) {
        	try {
        		RoomFilterRpcRequest filter = request.getRoomFilter();
        		if (filter == null) {
        			filter = new RoomFilterRpcRequest();
        			filter.setOption("department", selected.getExamType().getReference());
        			if (selected.getMaxNbrRooms() == 1 || selected.getSize() < 66)
        				filter.setOption("size", ">=" + selected.getSize());
        			else if (selected.getMaxNbrRooms() > 1)
        				filter.setOption("size", ">=" + (selected.getSize() / selected.getMaxNbrRooms()));
        		} else if (request.getPreviousExamId() == null || !request.getPreviousExamId().equals(request.getSelectedExamId())) {
        			if (selected.getMaxNbrRooms() == 1 || selected.getSize() < 66)
        				filter.setOption("size", ">=" + selected.getSize());
        			else if (selected.getMaxNbrRooms() > 1)
        				filter.setOption("size", ">=" + (selected.getSize() / selected.getMaxNbrRooms()));
        			else
        				filter.setOption("size", null);
        		}
        		Collection<ExamRoomInfo> rooms = getRooms(selected, current.getPeriod(), request.isRoomAllowConflicts(),
        				filter, context, proposed, solver, request.getRoomOrder());
        		if (rooms != null) {
                	TableInterface table = new TableInterface();
                	table.setName(MSG.sectAvailableRoomsForExam(examInfo.getExamName()));
                	for (ExamRoomInfo room: rooms) {
                		DomainItem item = new DomainItem();
                		item.setId(room.getLocationId().toString());
                		item.setCell(room.toCell());
                		item.setExtra(new CellInterface().setText("" + room.getCapacity(current)));
                		item.setValue(room.getCapacity(current));
                		item.setAssigned(examInfo != null && examInfo.hasRoom(room.getLocationId()));
                		item.setSelected(current != null && current.hasRoom(room.getLocationId()));
                		response.addRoom(item);
                	}
        		}
        		if (!response.hasRooms())
        			response.addRoomsErrorMessage(MSG.infoNoMatchingRoom());
        	} catch (GwtRpcException e) {
            	sLog.error("Failed to enumerate possible rooms: " + e.getMessage(), e);
            	response.addRoomsErrorMessage(e.getMessage());
            }
        }
        
        if (request.getOperation() == Operation.ASSIGN && getCanAssign(proposed, context)) {
			String error = assign(proposed, context, solver);
			if (error != null) {
				if (response.getAssignments() != null)
					response.getAssignments().setErrorMessage(error);
				else
					response.setErrorMessage(error);
			} else {
				response.setUrl("examination?id=" + request.getSelectedExamId());
			}
		}
        
        if (request.getOperation() == Operation.INIT) {
        	SuggestionProperties properties = new SuggestionProperties();
    		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false)) {
    			properties.addPreference(new PreferenceInterface(
    					pref.getUniqueId(),
    					PreferenceLevel.prolog2color(pref.getPrefProlog()),
    					PreferenceLevel.prolog2bgColor(pref.getPrefProlog()),
    					pref.getPrefProlog(),
    					pref.getPrefName(),
    					pref.getAbbreviation(),
    					Constants.preference2preferenceLevel(pref.getPrefProlog()),
    					null));
    		}
    		properties.setSolver(solver != null);
    		response.setSuggestionProperties(properties);
    		properties.setFirstDay(ApplicationProperty.TimePatternFirstDayOfWeek.intValue());
    		
    		SolverPageBackend.fillSolverWarnings(context, solver, SolverType.EXAM, response);
        }
		
        response.setCanAssign(getCanAssign(proposed, context));
        response.setAssignConfirmation(solver == null ? MSG.questionAssignDatabase() : MSG.questionAssignSolver());
        response.setCanShowSuggestions(solver != null);

		return response;
	}
	
	public ExamProposedChange update(ExamProposedChange change, ExamAssignmentInfo examInfo, ExamSolverProxy solver) throws Exception {
		if (change == null) return null;
		if (solver != null) {
			return solver.update(change);
        } else {
            Vector<ExamAssignment> assignments = new Vector(change.getAssignments());
            Hashtable<Long,ExamAssignment> table = change.getAssignmentTable();
            change.getAssignments().clear();
            for (ExamAssignment assignment : assignments) {
            	change.getAssignments().add(new ExamAssignmentInfo(assignment.getExam(),assignment.getPeriod(),assignment.getRooms(),table));
            }
            if (assignments.isEmpty()) {
            	for (Iterator<ExamAssignment> i = change.getConflicts().iterator(); i.hasNext(); ) {
            		ExamAssignment assignment = i.next();
            		if (assignment == null || !assignment.getExamId().equals(change.getSelected())) i.remove();
            	}
            } else {
            	change.getConflicts().clear();
            }
            assignment: for (ExamAssignment assignment : new Vector<ExamAssignment>(change.getAssignments())) {
                if (assignment.getRooms() != null) {
                	for (ExamRoomInfo room : assignment.getRooms()) {
                		
                		if (!room.isHard()) continue;
                		
                        Set<Long> canShareRoom = (assignment.getRooms().size() == 1 ? getCanShareRoomExams(assignment.getExamId()) : new HashSet<Long>());
                		int size = assignment.getNrStudents();
                		
                		if (!canShareRoom.isEmpty()) {
                    		for (ExamAssignment other: change.getAssignments()) {
                    			if (!other.equals(assignment) && other.getPeriodId().equals(assignment.getPeriodId()) && assignment.getRooms().equals(other.getRooms()))
                    				size += other.getNrStudents();
                    		}
                    		if (size > room.getCapacity(assignment)) {
                    			if (!examInfo.equals(assignment)) {
                    				change.getAssignments().remove(assignment);
                    				change.getConflicts().add(new ExamAssignment(assignment.getExam()));
                    			}
                    			continue assignment;
                    		}
                		}
                		
                        for (Exam x: room.getLocation().getExams(assignment.getPeriodId())) {
                            if (change.getCurrent(x.getUniqueId()) == null && change.getConflict(x.getUniqueId()) == null) {
                            	if (canShareRoom.contains(x.getUniqueId())) {
                            		if (size + x.getSize() <= room.getCapacity(assignment))
                            			size += x.getSize();
                            		else
                            			change.getConflicts().add(new ExamAssignment(x));
                            	} else {
                            		change.getConflicts().add(new ExamAssignment(x));
                            	}
                            }
                        }
                        
                        if (room.getLocation() instanceof Room) {
                        	Room r = RoomDAO.getInstance().get(room.getLocationId());
    						if (r.getParentRoom() != null) {
    							ExamRoomInfo parent = new ExamRoomInfo(r.getParentRoom(), 0);
    							if (assignment.getRooms().contains(parent)) {
    								change.getAssignments().remove(assignment);
                    				change.getConflicts().add(new ExamAssignment(assignment.getExam()));
                    				continue assignment;
    							}
    							if (!r.getParentRoom().isIgnoreRoomCheck())
        							for (Exam x: r.getParentRoom().getExams(assignment.getPeriodId()))
        								if (change.getCurrent(x.getUniqueId()) == null && change.getConflict(x.getUniqueId()) == null)
        									change.getConflicts().add(new ExamAssignment(x));
    						}
    						for (Room p: r.getPartitions()) {
    							if (!p.isIgnoreRoomCheck())
    								for (Exam x: p.getExams(assignment.getPeriodId()))
    									if (change.getCurrent(x.getUniqueId()) == null && change.getConflict(x.getUniqueId()) == null)
    		                            	change.getConflicts().add(new ExamAssignment(x));
    						}
                        }
                    }
                }
            }
            return change;
        }
	}
	
	protected Set<Long> getCanShareRoomExams(Long examId) {
    	return new HashSet<Long>(ExamDAO.getInstance().getSession().createQuery(
    			"select o.prefGroup.uniqueId from DistributionPref p inner join p.distributionObjects x inner join p.distributionObjects o " +
    			"where p.distributionType.reference = :shareType and x.prefGroup.uniqueId = :examId and x.prefGroup != o.prefGroup", Long.class)
    			.setParameter("shareType", "EX_SHARE_ROOM")
    			.setParameter("examId", examId)
    			.setCacheable(true).list());
    }
	
	public static TableInterface getProperties(Exam exam, SessionContext context) {
		TableInterface response = new TableInterface();
		CellInterface owners = response.addProperty(MSG.sectExamOwners());
		for (ExamOwner owner: new TreeSet<ExamOwner>(exam.getOwners()))
			owners.add(owner.getLabel()).setInline(false);
		response.addProperty(MSG.propExamType()).setText(exam.getExamType().getLabel());
		response.addProperty(MSG.propExamLength()).setText(exam.getLength());
		response.addProperty(MSG.propExamSeatingType()).setText(exam.getSeatingType() == Exam.sSeatingTypeNormal ? MSG.seatingNormal() : MSG.seatingExam());
		response.addProperty(MSG.propExamMaxRooms()).setText(exam.getMaxNbrRooms());
		response.addProperty(MSG.propExamSize()).setText(exam.getSize()).addStyle(exam.getExamSize() == null ? "font-style: italic;" : "");
		if (exam.getPrintOffset() != null && exam.getPrintOffset() != 0)
			response.addProperty(MSG.propExamPrintOffset()).setText(exam.getPrintOffset()).add(" " + MSG.offsetUnitMinutes());
		if (exam.getInstructors() != null && !exam.getInstructors().isEmpty()) {
			TableInterface table = new TableInterface();
			String nameFormat = UserProperty.NameFormat.get(context.getUser());
			for (DepartmentalInstructor instructor: new TreeSet<DepartmentalInstructor>(exam.getInstructors())) {
				LineInterface line = table.addLine();
				line.addCell(instructor.getName(nameFormat));
				if (instructor.getEmail() != null && !instructor.getEmail().isEmpty())
					line.addCell(instructor.getEmail());
				else
					line.addCell();
			}
			response.addProperty(MSG.propExamInstructors()).setTable(table);
		}
		if (exam.getAvgPeriod() != null) {
			ExamPeriod ep = exam.getAveragePeriod();
			if (ep != null)
				response.addProperty(MSG.propExamAvgPeriod()).setText(ep.getName());
		}
		if (exam.getNote() != null && !exam.getNote().trim().isEmpty())
			response.addProperty(CMSG.propertyRequestsNotes()).setText(exam.getNote()).addStyle("white-space: pre-wrap;");
        List<AccommodationCounter> acc = StudentAccomodation.getAccommodations(exam);
        if (acc != null && !acc.isEmpty()) {
        	CellInterface c = response.addProperty(MSG.propExamStudentAccommodations());
        	TableInterface table = new TableInterface();
        	for (AccommodationCounter ac: acc)
        		table.addProperty(ac.getAccommodation().getName() + ":").setText(String.valueOf(ac.getCount()));
        	c.setTable(table);
        }
		return response;
	}
	
	public TableInterface generateAssignmentsTable(ExamProposedChange proposed, SessionContext context) {
    	TableInterface table = new TableInterface();
    	table.setName(MSG.sectNewAssignments());
    	LineInterface header = table.addHeader();
    	header.addCell(MSG.colExamination());
    	header.addCell(MSG.colPeriodChange());
    	header.addCell(MSG.colRoomChange());
    	header.addCell(MSG.conflictDirect()).setTextAlignment(Alignment.CENTER);
    	header.addCell(MSG.conflictMoreThanTwoADay()).setTextAlignment(Alignment.CENTER);
    	header.addCell(MSG.conflictBackToBack()).setTextAlignment(Alignment.CENTER);
    	for (CellInterface h: header.getCells())
        	h.setClassName("WebTableHeader");
    	for (ExamAssignmentInfo assignment : proposed.getAssignments()) {
    		ExamAssignment initial = proposed.getInitial(assignment);
    		LineInterface line = table.addLine();
    		line.setURL("#id=" + assignment.getExamId());
    		if (assignment.getExamId().equals(proposed.getSelected()))
    			line.setClassName("unitime-TableRowSelected");
    		CellInterface c = line.addCell();
    		c.add("").setUrl("#delete=" + assignment.getExamId()).setImage().setSource("images/action_delete.png")
				.addStyle("cursor: pointer; padding-right: 5px; vertical-align: bottom;")
				.setAlt(GWT.titleDeleteRow());
			c.addItem(assignment.getExamNameCell());
    		CellInterface t = line.addCell(); t.setNoWrap(true);
    		if (initial!=null && !initial.getPeriodId().equals(assignment.getPeriodId()))
    			t.addItem(initial.getPeriodCell()).add(" \u2192 ");
    		if (initial==null && assignment.getPeriodId() != null)
    			t.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
    			.add(" \u2192 ");
    		if (assignment.getPeriodId() == null)
    			t.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    		else
    			t.addItem(assignment.getPeriodCell());
    		CellInterface r = line.addCell(); r.setNoWrap(true);
    		if (initial!=null && !initial.getRoomIds().equals(assignment.getRoomIds()))
    			r.addItem(initial.toRoomCell()).add(" \u2192 ");
    		if (initial==null && assignment.getNrRooms() > 0 && assignment.getMaxRooms() > 0)
    			r.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
    			.add(" \u2192 ");
    		r.addItem(assignment.toRoomCell());
    		if (assignment.getMaxRooms() > 0 && assignment.getNrRooms() == 0 && assignment.getPeriodId() == null) {
    			r.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    		} else if (assignment.getMaxRooms() > 0 && assignment.getNrRooms() == 0) {
                if (assignment.getExamId().equals(proposed.getSelected()))
                	r.add(CMSG.assignmentRoomSelectBelow()).addStyle("font-style: italic;");
                else
                	r.add(CMSG.assignmentRoomNotSelected()).setColor("#ee0000").addStyle("font-style: italic;");
			}
			if (assignment.getMaxRooms() == 0)
				r.add(CMSG.notApplicable()).addStyle("font-style: italic;");
			dispNumber(line.addCell().setTextAlignment(Alignment.CENTER), false, (initial == null ? 0 : initial.getPlacementNrDirectConflicts()), assignment.getPlacementNrDirectConflicts());
			dispNumber(line.addCell().setTextAlignment(Alignment.CENTER), false, (initial == null ? 0 : initial.getPlacementNrMoreThanTwoADayConflicts()), assignment.getPlacementNrMoreThanTwoADayConflicts());
			CellInterface btb = line.addCell().setTextAlignment(Alignment.CENTER);
			dispNumber(btb, false, (initial == null ? 0 : initial.getPlacementNrBackToBackConflicts()), assignment.getPlacementNrBackToBackConflicts());
			if (assignment.getPlacementNrDistanceBackToBackConflicts() > 0 || (initial != null && initial.getPlacementNrDistanceBackToBackConflicts() > 0)) {
				btb.add(" (" + MSG.prefixDistanceConclict());
				dispNumber(btb, false, (initial == null ? 0 : initial.getPlacementNrDistanceBackToBackConflicts()), assignment.getPlacementNrDistanceBackToBackConflicts());
				btb.add(")");
			}
    	}
    	for (ExamAssignment conflict : proposed.getConflicts()) {
    		LineInterface line = table.addLine();
    		if (conflict.getExamId().equals(proposed.getSelected()))
    			line.setClassName("unitime-TableRowSelected");
    		line.setURL("#id=" + conflict.getExamId());
    		line.addCell(conflict.getExamNameCell());
    		CellInterface t = line.addCell(); t.setNoWrap(true);
    		t.addItem(conflict.getPeriodCell()).add(" \u2192 ");
    		t.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    		CellInterface r = line.addCell(); r.setNoWrap(true);
    		if (conflict.getMaxRooms() > 0) {
    			r.addItem(conflict.toRoomCell()).add(" \u2192 ");
    			r.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    		} else {
    			r.add(CMSG.notApplicable()).addStyle("font-style: italic;");
    		}
			dispNumber(line.addCell(), true, conflict.getPlacementNrDirectConflicts(), 0);
			dispNumber(line.addCell(), true, conflict.getPlacementNrMoreThanTwoADayConflicts(), 0);
			CellInterface btb = line.addCell();
			dispNumber(btb, true, conflict.getPlacementNrBackToBackConflicts(), 0);
			if (conflict.getPlacementNrDistanceBackToBackConflicts() > 0) {
				btb.add(" (" + MSG.prefixDistanceConclict());
				dispNumber(btb, true, conflict.getPlacementNrDistanceBackToBackConflicts(), 0);
				btb.add(")");
			}
    	}
    	
    	return table;
	}
	
	protected void dispNumber(CellInterface cell, long number) {
		dispNumber(cell, "", number);
	}
	
	public static void dispNumber(CellInterface cell, String prefix, long number) {
		if (number > 0) 
			cell.add(prefix + "+" + number).setColor("#ec0000");
		else if (number < 0)
			cell.add(prefix + number).setColor("#006400");
		else
			cell.add(prefix+"0");
	}
	
	protected void dispNumber(CellInterface cell, boolean rem, int n1, int n2) {
		if (n1==0 && n2==0) return;
		if (rem) {
			dispNumber(cell, -n1);
			return;
		}
		int dif = n2-n1;
		if (dif==0)
			cell.add(n1 + "\u2192" + n2);
		else if (dif < 0)
			cell.add(n1 + "\u2192" + n2).setColor("#006400");
		else
			cell.add(n1 + "\u2192" + n2).setColor("#ec0000");
	}
	
    public Collection<ExamAssignmentInfo> getPeriods(Exam exam, ExamProposedChange proposed, ExamSolverProxy solver) {
        if (solver != null)
        	return solver.getPeriods(exam.getUniqueId(), proposed);
        Hashtable<Long, Set<Exam>> studentExams = exam.getStudentExams();
        List<ExamAssignmentInfo> periods = new ArrayList<ExamAssignmentInfo>();
        for (ExamPeriod period: ExamPeriod.findAll(exam.getSession().getUniqueId(), exam.getExamType().getUniqueId())) {
        	try {
                periods.add(new ExamAssignmentInfo(exam, period, null, studentExams, (proposed == null ? null : proposed.getAssignmentTable())));
            } catch (Exception e) {
                if (!MSG.errorPeriodProhibited().equals(e.getMessage()) && !MSG.errorPeriodTooShort().equals(e.getMessage()) && !MSG.errorPeriodNotRequired().equals(e.getMessage()))
                	sLog.error(e.getMessage(), e);
            }
        }
        return periods;
    }
    
	public Collection<ExamRoomInfo> getRooms(Exam exam, ExamPeriod period, boolean allowConflicts, RoomFilterRpcRequest filter, SessionContext context, ExamProposedChange proposed, ExamSolverProxy solver, final RoomOrder ord) {
		if (exam.getMaxNbrRooms() == 0) return null;
		
		if (solver != null) {
			Vector<ExamRoomInfo> ret = solver.getRooms(exam.getUniqueId(), period.getUniqueId(), proposed, -1, -1, null, allowConflicts);
			String f = (filter == null ? null : filter.toQueryString());
    		Set<String> featureTypes = new HashSet<String>();
    		for (RoomFeatureType ft: RoomFeatureTypeDAO.getInstance().findAll())
    			featureTypes.add(ft.getReference().toLowerCase().replace(' ', '_'));
			if (f != null && !f.isEmpty()) {
				Query q = new Query(f);
				for (Iterator<ExamRoomInfo> i = ret.iterator(); i.hasNext(); ) {
					ExamRoomInfo r = i.next();
					if (!q.match(new LocationMatcher(r.getLocation(), featureTypes).setUseExamCapacity(exam.getSeatingType() == Exam.sSeatingTypeExam)))
						i.remove();
				}
			}
			return ret;
		}
		
        Vector<ExamRoomInfo> rooms = new Vector<ExamRoomInfo>();
        boolean reqRoom = false;
        boolean reqBldg = false;
        boolean reqGroup = false;
        
        Set<Long> canShareRoom = getCanShareRoomExams(exam.getUniqueId());
        
        Set<RoomGroupPref> groupPrefs = exam.getPreferences(RoomGroupPref.class);
        for (Preference p: groupPrefs)
        	if (PreferenceLevel.sRequired.equals(p.getPrefLevel().getPrefProlog())) {
        		reqGroup = true; break;
        	}
        Set<RoomPref> roomPrefs = exam.getPreferences(RoomPref.class);
        for (Preference p: roomPrefs)
        	if (PreferenceLevel.sRequired.equals(p.getPrefLevel().getPrefProlog())) {
        		reqRoom = true; break;
        	}
        Set<BuildingPref> bldgPrefs = exam.getPreferences(BuildingPref.class);
        for (Preference p: bldgPrefs)
        	if (PreferenceLevel.sRequired.equals(p.getPrefLevel().getPrefProlog())) {
        		reqBldg = true; break;
        	}
        Set<RoomFeaturePref> featurePrefs = exam.getPreferences(RoomFeaturePref.class);
        
        if (exam.getSeatingType() == Exam.sSeatingTypeExam)
        	filter.addOption("flag", "examcap");
        
        List<Location> locations = new RoomFilterBackend().locations(
				context.getUser().getCurrentAcademicSessionId(), filter, -1,
				new HashMap<Long, Double>(), new EventContext(context, context.getUser().getCurrentAcademicSessionId()));
        Hashtable<Long, Set<Long>> locationTable = Location.findExamLocationTable(period.getUniqueId());
        
        for (Set<Long> examIds: locationTable.values())
        	examIds.remove(exam.getUniqueId());
        if (proposed != null) {
            for (ExamAssignment conflict : proposed.getConflicts()) {
                if (conflict.getPeriod().equals(period) && conflict.getRooms()!=null)
                    for (ExamRoomInfo room : conflict.getRooms()) {
                    	Set<Long> exams = locationTable.get(room.getLocationId());
                    	if (exams != null) exams.remove(conflict.getExamId());
                    }
            }
            for (ExamAssignment current : proposed.getAssignments()) {
                ExamAssignment initial = proposed.getInitial(current);
                if (initial!=null && initial.getPeriod().equals(period) && initial.getRooms()!=null)
                    for (ExamRoomInfo room : initial.getRooms()) {
                    	Set<Long> exams = locationTable.get(room.getLocationId());
                    	if (exams != null) exams.remove(initial.getExamId());
                    }
            }
            for (ExamAssignment current : proposed.getAssignments()) {
                if (!exam.getUniqueId().equals(current.getExamId()) && current.getPeriod().equals(period) && current.getRooms()!=null)
                    for (ExamRoomInfo room : current.getRooms()) {
                    	Set<Long> exams = locationTable.get(room.getLocationId());
                    	if (exams == null) { exams = new HashSet<Long>(); locationTable.put(room.getLocationId(), exams); }
                    	exams.add(current.getExamId());
                    }
            }
        }
        
        rooms: for (Iterator<Location> i1=locations.iterator();i1.hasNext();) {
            Location room = i1.next();
            
            boolean shouldNotBeUsed = PreferenceLevel.sStronglyDiscouraged.equals(room.getExamPreference(period).getPrefProlog());
            
            boolean add = true;
            
            PreferenceCombination pref = new SumPreferenceCombination();
            
            // --- group preference ----------
            PreferenceCombination groupPref = PreferenceCombination.getDefault();
            for (RoomGroupPref p: groupPrefs) {
                if (p.getRoomGroup().getRooms().contains(room))
                    groupPref.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
            }
            
            if (groupPref.getPreferenceProlog().equals(PreferenceLevel.sProhibited)) add=false;
            
            if (reqGroup && !groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired)) add=false;

            if (!groupPref.getPreferenceProlog().equals(PreferenceLevel.sProhibited) && !groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired))
                pref.addPreferenceProlog(groupPref.getPreferenceProlog());
            
            
            // --- room preference ------------
            String roomPref = room.getExamPreference(period).getPrefProlog();
            
            for (RoomPref p: roomPrefs) {
                if (room.equals(p.getRoom())) {
                    roomPref = p.getPrefLevel().getPrefProlog();
                    shouldNotBeUsed = false;
                    break;
                }
            }
            
            if (roomPref!=null && roomPref.equals(PreferenceLevel.sProhibited)) add=false;

            if (reqRoom && (roomPref==null || !roomPref.equals(PreferenceLevel.sRequired))) add=false;
            
            if (roomPref!=null && !roomPref.equals(PreferenceLevel.sProhibited) && !roomPref.equals(PreferenceLevel.sRequired)) pref.addPreferenceProlog(roomPref);

            // --- building preference ------------
            Building bldg = (room instanceof Room ? ((Room)room).getBuilding() : null);

            String bldgPref = null;
            for (BuildingPref p: bldgPrefs) {
                if (bldg!=null && bldg.equals(p.getBuilding())) {
                    bldgPref = p.getPrefLevel().getPrefProlog();
                    break;
                }
            }
            
            if (bldgPref!=null && bldgPref.equals(PreferenceLevel.sProhibited)) add=false;
            
            if (reqBldg && (bldgPref==null || !bldgPref.equals(PreferenceLevel.sRequired))) add=false;
            
            if (!reqBldg && (bldgPref!=null && bldgPref.equals(PreferenceLevel.sRequired))) {
                reqBldg = true;
                rooms.clear();
            }

            if (bldgPref!=null && !bldgPref.equals(PreferenceLevel.sProhibited) && !bldgPref.equals(PreferenceLevel.sRequired)) pref.addPreferenceProlog(bldgPref);
            
            // --- room features preference --------  
            boolean acceptableFeatures = true;
            PreferenceCombination featurePref = new MinMaxPreferenceCombination();
            for (RoomFeaturePref roomFeaturePref: featurePrefs) {
                RoomFeature feature = roomFeaturePref.getRoomFeature();
                String p = roomFeaturePref.getPrefLevel().getPrefProlog();
                
                boolean hasFeature = feature.getRooms().contains(room);
                if (p.equals(PreferenceLevel.sProhibited) && hasFeature) {
                    acceptableFeatures=false;
                }
                if (p.equals(PreferenceLevel.sRequired) && !hasFeature) {
                    acceptableFeatures=false;
                }
                if (p!=null && hasFeature && !p.equals(PreferenceLevel.sProhibited) && !p.equals(PreferenceLevel.sRequired)) 
                    featurePref.addPreferenceProlog(p);
            }
            pref.addPreferenceInt(featurePref.getPreferenceInt());
            
            if (!acceptableFeatures) add=false;
            
            if (!add || shouldNotBeUsed) continue;
            
            Set<Long> exams = locationTable.get(room.getUniqueId());
            boolean roomConflict = false;
            if (exams != null && !exams.isEmpty() && !room.isIgnoreRoomCheck()) {
            	for (Long other: exams) {
            		if (!canShareRoom.contains(other)) {
            			roomConflict = true;
            			if (!allowConflicts) continue rooms;
                		if (proposed != null && proposed.getCurrent(other) != null) continue rooms;
            		}
            	}
            }
            if (room instanceof Room) {
            	Room r = (Room) room;
            	if (r.getParentRoom() != null) {
            		Set<Long> parentExams = locationTable.get(r.getParentRoom().getUniqueId());
                    if (parentExams != null && !parentExams.isEmpty() && !r.getParentRoom().isIgnoreRoomCheck()) {
                    	for (Long other: parentExams) {
                    		if (!canShareRoom.contains(other)) {
                    			roomConflict = true;
                    			if (!allowConflicts) continue rooms;
                        		if (proposed != null && proposed.getCurrent(other) != null) continue rooms;
                    		}
                    	}
                    }
            	}
            	for (Room p: r.getPartitions()) {
            		Set<Long> partitionExams = locationTable.get(p.getUniqueId());
                    if (partitionExams != null && !partitionExams.isEmpty() && !p.isIgnoreRoomCheck()) {
                    	for (Long other: partitionExams) {
                    		if (!canShareRoom.contains(other)) {
                    			roomConflict = true;
                    			if (!allowConflicts) continue rooms;
                        		if (proposed != null && proposed.getCurrent(other) != null) continue rooms;
                    		}
                    	}
                    }
            	}
            }
            
            if (PreferenceLevel.sProhibited.equals(room.getExamPreference(period).getPrefProlog())) continue;
            
            if (!room.isIgnoreRoomCheck() && RoomAvailability.getInstance()!=null) {
                Collection<TimeBlock> times = RoomAvailability.getInstance().getRoomAvailability(
                        room.getUniqueId(),
                        period.getStartTime(), period.getEndTime(), 
                        period.getExamType().getReference());
                if (times!=null) for (TimeBlock time : times) {
                    if (period.overlap(time)) {
                    	if (locations.size() == 1) throw new GwtRpcException(CMSG.messageRoomNotAvailable2(room.getLabel(), period.getName(), time.getEventName()));
                    	sLog.info(CMSG.messageRoomNotAvailable2(room.getLabel(), period.getName(), time.getEventName()));
                        continue rooms;
                    }
                }
            }
            if (room instanceof Room && RoomAvailability.getInstance() != null) {
            	Room r = (Room) room;
            	if (r.getParentRoom() != null && !r.getParentRoom().isIgnoreRoomCheck()) {
            		Collection<TimeBlock> times = RoomAvailability.getInstance().getRoomAvailability(
                            r.getParentRoom().getUniqueId(),
                            period.getStartTime(), period.getEndTime(), 
                            period.getExamType().getReference());
                    if (times!=null) for (TimeBlock time : times) {
                        if (period.overlap(time)) {
                        	if (locations.size() == 1) throw new GwtRpcException(CMSG.messageRoomNotAvailable2(room.getLabel(), period.getName(), time.getEventName()));
                        	sLog.info(CMSG.messageRoomNotAvailable2(room.getLabel(), period.getName(), time.getEventName()));
                            continue rooms;
                        }
                    }
            	}
            	for (Room p: r.getPartitions()) {
            		if (!p.isIgnoreRoomCheck()) {
            			Collection<TimeBlock> times = RoomAvailability.getInstance().getRoomAvailability(
                                p.getUniqueId(),
                                period.getStartTime(), period.getEndTime(), 
                                period.getExamType().getReference());
                        if (times!=null) for (TimeBlock time : times) {
                            if (period.overlap(time)) {
                            	if (locations.size() == 1) throw new GwtRpcException(CMSG.messageRoomNotAvailable2(room.getLabel(), period.getName(), time.getEventName()));
                            	sLog.info(CMSG.messageRoomNotAvailable2(room.getLabel(), period.getName(), time.getEventName()));
                                continue rooms;
                            }
                        }
            		}
            	}
            }
            
            rooms.add(new ExamRoomInfo(room, (roomConflict ? 1000 : 0) + pref.getPreferenceInt()));
        }
        
        Collections.sort(rooms, new Comparator<ExamRoomInfo>() {
			@Override
			public int compare(ExamRoomInfo r1, ExamRoomInfo r2) {
				switch (ord) {
				case NAME_ASC:
					return NaturalOrderComparator.compare(r1.getName(), r2.getName());
				case NAME_DESC:
					return NaturalOrderComparator.compare(r2.getName(), r1.getName());
				case SIZE_ASC:
					if (r1.getCapacity() != r2.getCapacity())
						return r1.getCapacity() < r2.getCapacity() ? -1 : 1;
					return NaturalOrderComparator.compare(r1.getName(), r2.getName());
				case SIZE_DESC:
					if (r1.getCapacity() != r2.getCapacity())
						return r1.getCapacity() > r2.getCapacity() ? -1 : 1;
					return NaturalOrderComparator.compare(r1.getName(), r2.getName());
				default:
					return NaturalOrderComparator.compare(r1.getName(), r2.getName());
				}
			}
		});
		return rooms;
	}
	
	public boolean getCanAssign(ExamProposedChange proposed, SessionContext context) {
        if (proposed==null) return false;
        for (ExamAssignment assignment : proposed.getAssignments())
            if (!assignment.isValid()) return false;
        return true;
    }

	public String assign(ExamProposedChange proposed, SessionContext context, ExamSolverProxy solver) {
		if (proposed == null) return MSG.warnNothingToAssign();
        sLog.info("About to be assigned: " + proposed);
        if (solver != null) {
            String message = null;
            for (ExamAssignment assignment : proposed.getConflicts()) {
                String m = solver.unassign(assignment);
                if (m!=null) message = (message==null?"":message+"\n")+m;
            }
            for (ExamAssignment assignment : proposed.getAssignments()) {
                if (proposed.getInitial(assignment)!=null) {
                    String m = solver.unassign(proposed.getInitial(assignment));
                    if (m!=null) message = (message==null?"":message+"\n")+m;
                }
            }
            for (ExamAssignment assignment : proposed.getAssignments()) {
                String m = solver.assign(assignment);
                if (m!=null) message = (message==null?"":message+"\n")+m;
            }
            return message;
        } else {
            org.hibernate.Session hibSession = ExamDAO.getInstance().getSession();
            String message = null;
            for (ExamAssignment assignment : proposed.getConflicts()) {
                String m = assignment.getExam(hibSession).unassign(context.getUser().getExternalUserId(), hibSession);
                if (m!=null) message = (message==null?"":message+"\n")+m;
            }
            for (ExamAssignment assignment : proposed.getAssignments()) {
                try {
                    String m = assignment.getExam(hibSession).assign(
                    		new ExamAssignmentInfo(assignment.getExam(), assignment.getPeriod(), assignment.getRooms(), proposed.getAssignmentTable()),
                    		context.getUser().getExternalUserId(), hibSession);
                    if (m!=null) message = (message==null?"":message+"\n")+m;
                } catch (Exception e) {
                    message = (message==null?"":message+"\n")+
                    		MSG.errorAssignmentFailed(assignment.getExamName(), assignment.getPeriodAbbreviation(), assignment.getRoomsName(", "), e.getMessage());
                }
            }
            return message;
        }
    }
	
	public static CellInterface dc2cell(int conf, int diff) {
		CellInterface ret = new CellInterface().setTextAlignment(Alignment.RIGHT);
		if (conf != 0 || diff != 0) ret.setText(String.valueOf(conf));
		if (conf > 0) ret.setColor(PreferenceLevel.prolog2color("P"));
		if (diff < 0)
			ret.add(" (" + diff + ")").setColor(PreferenceLevel.prolog2color("R"));
		else if (diff > 0)
			ret.add(" (+" + diff + ")").setColor(PreferenceLevel.prolog2color("P"));
		return ret;
    }
    
    public static CellInterface md2cell(int conf, int diff) {
        CellInterface ret = new CellInterface().setTextAlignment(Alignment.RIGHT);
        if (conf != 0 || diff != 0) ret.setText(String.valueOf(conf));
		if (conf > 0) ret.setColor(PreferenceLevel.prolog2color("2"));
		if (diff < 0)
			ret.add(" (" + diff + ")").setColor(PreferenceLevel.prolog2color("-2"));
		else if (diff > 0)
			ret.add(" (+" + diff + ")").setColor(PreferenceLevel.prolog2color("2"));
		return ret;
    }

    public static CellInterface btb2cell(int conf, int diff, int dconf, int ddiff) {
		CellInterface ret = new CellInterface().setTextAlignment(Alignment.RIGHT);
		if (conf != 0 || diff != 0) ret.setText(String.valueOf(conf));
		if (conf > 0) ret.setColor(PreferenceLevel.prolog2color("1"));
		if (diff < 0)
			ret.add(" (" + diff).setColor(PreferenceLevel.prolog2color("-1"));
		else if (diff > 0)
			ret.add(" (+" + diff).setColor(PreferenceLevel.prolog2color("1"));
		else if (ddiff != 0)
			ret.add(" (" + diff);
		if (ddiff < 0)
			ret.add(" " + MSG.prefixDistanceConclict() + ddiff).setColor(PreferenceLevel.prolog2color("-1"));
		if (ddiff > 0)
			ret.add(" " + MSG.prefixDistanceConclict() + "+" + ddiff).setColor(PreferenceLevel.prolog2color("1"));
        if (diff < 0)
        	ret.add(")").setColor(PreferenceLevel.prolog2color("-1"));
        else if (diff>0)
        	ret.add(")").setColor(PreferenceLevel.prolog2color("1"));
        else if (ddiff!=0)
        	ret.add(")");
        return ret;
    }
    
    protected void generateSuggestions(ExamAssignmentRequest request, ExamAssignmentResponse response, ExamProposedChange proposed, ExamSolverProxy solver) {
    	try {
    		ExamSuggestionsInfo suggestions = solver.getSuggestions(
    				request.getSelectedExamId(), proposed, request.getSuggestionFilter(),
    				request.getSuggestionDepth(), request.getSuggestionMax(), 1000 * request.getSuggestionTimeOut());
    		response.setSuggestionsMessage(suggestions.getMessage());
    		response.setSuggestionsTimeoutReached(suggestions.getTimeoutReached());
    		if (suggestions.getSuggestions() != null) {
    			TableInterface table = new TableInterface();
            	LineInterface header = table.addHeader();
            	header.addCell(MSG.colValue()).setTextAlignment(Alignment.RIGHT);
            	header.addCell(MSG.colExamination());
            	header.addCell(MSG.colPeriodChange());
            	header.addCell(MSG.colRoomChange());
            	header.addCell(MSG.conflictDirect()).setTextAlignment(Alignment.CENTER);
            	header.addCell(MSG.conflictMoreThanTwoADay()).setTextAlignment(Alignment.CENTER);
            	header.addCell(MSG.conflictBackToBack()).setTextAlignment(Alignment.CENTER);
            	for (CellInterface h: header.getCells())
                	h.setClassName("WebTableHeader");
    			for (ExamProposedChange change: suggestions.getSuggestions()) {
    				LineInterface line = table.addLine();
    				CellInterface score = line.addCell().setTextAlignment(Alignment.RIGHT);
    				CellInterface exam = line.addCell();
    				CellInterface period = line.addCell();
    				CellInterface room = line.addCell();
    				CellInterface dc = line.addCell().setTextAlignment(Alignment.CENTER);
    				CellInterface m2d = line.addCell().setTextAlignment(Alignment.CENTER);
    				CellInterface btb = line.addCell().setTextAlignment(Alignment.CENTER);
    				dispNumber(score, Math.round(change.getValue()));
                	String changeId = null;
    				for (ExamAssignment assignment: change.getAssignments()) {
    					if (changeId == null)
    						changeId = assignment.getExamId() + "," + assignment.getPeriodId() + ",";
    					else
    						changeId += ";" + assignment.getExamId() + "," + assignment.getPeriodId() + ",";
    					if (assignment.getRooms() != null)
    						for (Iterator<ExamRoomInfo> i = assignment.getRooms().iterator(); i.hasNext(); ) {
    							changeId += i.next().getLocationId();
    							if (i.hasNext()) changeId += ":";
    						}
    					ExamAssignment initial = change.getInitial(assignment);
    					exam.addItem(assignment.getExamNameCell().setInline(false).setNoWrap(true));
    					CellInterface t = period.add(null).setInline(false).setNoWrap(true);
    		    		if (initial!=null && !initial.getPeriodId().equals(assignment.getPeriodId()))
    		    			t.addItem(initial.getPeriodCell()).add(" \u2192 ");
    		    		if (initial==null && assignment.getPeriodId() != null)
    		    			t.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
    		    			.add(" \u2192 ");
    		    		if (assignment.getPeriodId() == null)
    		    			t.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    		    		else
    		    			t.addItem(assignment.getPeriodCell());
    		    		CellInterface r = room.add(null).setInline(false).setNoWrap(true);
    		    		if (initial!=null && !initial.getRoomIds().equals(assignment.getRoomIds()))
    		    			r.addItem(initial.toRoomCell()).add(" \u2192 ");
    		    		if (initial==null && assignment.getNrRooms() > 0 && assignment.getMaxRooms() > 0)
    		    			r.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
    		    			.add(" \u2192 ");
    		    		r.addItem(assignment.toRoomCell());
    		    		if (assignment.getMaxRooms() > 0 && assignment.getNrRooms() == 0 && assignment.getPeriodId() == null) {
    		    			r.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    		    		}
    		    		if (assignment.getMaxRooms() == 0)
    						r.add(CMSG.notApplicable()).addStyle("font-style: italic;");
    		    		dispNumber(dc.add(null).setInline(false).setNoWrap(true), false,
    		    				initial == null ? 0 : initial.getPlacementNrDirectConflicts(),
    		    				assignment.getPlacementNrDirectConflicts());
    		    		dispNumber(m2d.add(null).setInline(false).setNoWrap(true), false,
    		    				initial == null ? 0 : initial.getPlacementNrMoreThanTwoADayConflicts(),
    		    				assignment.getPlacementNrMoreThanTwoADayConflicts());
    		    		CellInterface x = btb.add(null).setInline(false).setNoWrap(true);
    		    		dispNumber(x, false,
    		    				initial == null ? 0 : initial.getPlacementNrBackToBackConflicts(),
    		    				assignment.getPlacementNrBackToBackConflicts());
    		    		if (assignment.getPlacementNrInstructorDistanceBackToBackConflicts() > 0 || (initial != null && initial.getPlacementNrInstructorDistanceBackToBackConflicts() > 0)) {
    		    			x.add(" (" + MSG.prefixDistanceConclict());
    		    			dispNumber(x, false,
        		    				initial == null ? 0 : initial.getPlacementNrInstructorDistanceBackToBackConflicts(),
        		    				assignment.getPlacementNrInstructorDistanceBackToBackConflicts());
    		    			x.add(")");
    		    		}
    				}
    				for (ExamAssignment conflict: change.getConflicts()) {
    					exam.addItem(conflict.getExamNameCell().setInline(false).setNoWrap(true));
    					CellInterface t = period.add(null).setInline(false).setNoWrap(true);
    					t.addItem(conflict.getPeriodCell()).add(" \u2192 ");
    					t.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    		    		CellInterface r = room.add(null).setInline(false).setNoWrap(true);
    		    		if (conflict.getMaxRooms() == 0 || conflict.getRooms() == null)
    		    			r.add(CMSG.notApplicable()).addStyle("font-style: italic;");
    		    		else {
    		    			r.addItem(conflict.toRoomCell()).add(" \u2192 ");
    		    			r.add(MSG.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
    		    		}
    		    		dispNumber(dc.add(null).setInline(false).setNoWrap(true), true,
    		    				conflict.getPlacementNrDirectConflicts(), 0);
    		    		dispNumber(m2d.add(null).setInline(false).setNoWrap(true), true,
    		    				conflict.getPlacementNrMoreThanTwoADayConflicts(), 0);
    		    		CellInterface x = btb.add(null).setInline(false).setNoWrap(true);
    		    		dispNumber(x, true,
    		    				conflict.getPlacementNrBackToBackConflicts(), 0);
    		    		if (conflict.getPlacementNrInstructorDistanceBackToBackConflicts() > 0) {
    		    			x.add(" (" + MSG.prefixDistanceConclict());
    		    			dispNumber(x, true,
        		    				conflict.getPlacementNrInstructorDistanceBackToBackConflicts(), 0);
    		    			x.add(")");
    		    		}
    				}
    				if (changeId != null)
    					line.setURL("#suggestion=" + changeId);
    			}
    			response.setSuggestions(table);
    		}
    	} catch (Exception e) {
    		sLog.error("Failed to compute suggestions: " + e.getMessage(), e);
    		response.setSuggestionsMessage(e.getMessage());
    	}
    }
}
