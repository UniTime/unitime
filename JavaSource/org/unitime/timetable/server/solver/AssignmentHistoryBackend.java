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
package org.unitime.timetable.server.solver;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import org.hibernate.type.LongType;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.TableInterface;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignmentHistoryRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignmentHistoryResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellChange;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellMultiLine;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellInterface;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellMulti;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellRooms;
import org.unitime.timetable.gwt.shared.TableInterface.TableHeaderIterface;
import org.unitime.timetable.gwt.shared.TableInterface.TableRowInterface;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableSolver.AssignmentRecord;
import org.unitime.timetable.solver.TimetableSolver.RecordedAssignment;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails.RoomInfo;
import org.unitime.timetable.solver.interactive.Suggestion;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;
import org.unitime.timetable.webutil.BackTracker;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(AssignmentHistoryRequest.class)
public class AssignmentHistoryBackend implements GwtRpcImplementation<AssignmentHistoryRequest, AssignmentHistoryResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static DecimalFormat sDF = new DecimalFormat("0.###",new java.text.DecimalFormatSymbols(Locale.US));
	protected static Format<Date> sTS = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public AssignmentHistoryResponse execute(AssignmentHistoryRequest request, SessionContext context) {
		context.checkPermission(Right.AssignmentHistory);
		AssignmentHistoryResponse response = new AssignmentHistoryResponse();
		
		context.getUser().setProperty("SuggestionsModel.simpleMode", request.getFilter().getParameterValue("simpleMode"));
		boolean simple = "1".equals(request.getFilter().getParameterValue("simpleMode"));
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		
		List<AssignmentRecord> history = null;
		if (solver==null) {
			response.setMessage(MESSAGES.errorNoSolverLoaded());
        } else {
        	history = solver.getAssignmentRecords();
        }
		
		if (history != null) {
			long idx = 0;
			for (Iterator<AssignmentRecord> it = history.iterator(); it.hasNext(); idx++) {
				AssignmentRecord record = it.next();
				
				TableCellMultiLine allClasses = new TableCellMultiLine();
				TableCellMultiLine allDates = new TableCellMultiLine();
				TableCellMultiLine allTimes = new TableCellMultiLine();
				TableCellMultiLine allRooms = new TableCellMultiLine();
				Long classId = null;
				
				for (RecordedAssignment assignment: record.getAssignments()) {
					ClassAssignmentDetails before = (assignment.getBefore()==null?null:assignment.getBefore().getDetails(context, solver, false));
	    	    	ClassAssignmentDetails after = (assignment.getAfter()==null?null:assignment.getAfter().getDetails(context, solver, false));
	    	    	ClassAssignmentDetails ca = (after == null ? before : after);
	    	    	if (classId == null) classId = ca.getClazz().getClassId();
	    	    	
	    	    	TableCellChange date = new TableCellChange(
	    	    			before == null || before.getAssignedTime() == null ? null : new TableCellInterface(before.getAssignedTime().getDatePatternName()).setColor(PreferenceLevel.int2color(before.getAssignedTime().getDatePatternPreference())),
	    	    			after == null || after.getAssignedTime() == null ? null : new TableCellInterface(after.getAssignedTime().getDatePatternName()).setColor(PreferenceLevel.int2color(after.getAssignedTime().getDatePatternPreference())));
	    	    	
	    	    	TableCellChange time = new TableCellChange(
	    	    			before == null || before.getAssignedTime() == null ? null : new TableInterface.TableCellTime(before.getAssignedTime().getDaysName() + " " + before.getAssignedTime().getStartTime())
	    	    	    			.setId(before.getClazz().getClassId() + "," + before.getAssignedTime().getDays() + "," + before.getAssignedTime().getStartSlot()).setColor(PreferenceLevel.int2color(before.getAssignedTime().getPref())),
	    	    	    	after == null || after.getAssignedTime() == null ? null : new TableInterface.TableCellTime(after.getAssignedTime().getDaysName() + " " + after.getAssignedTime().getStartTime())
	    	    	    	    	.setId(after.getClazz().getClassId() + "," + after.getAssignedTime().getDays() + "," + after.getAssignedTime().getStartSlot()).setColor(PreferenceLevel.int2color(after.getAssignedTime().getPref())));
	    	    	
	    	    	TableCellChange room = new TableCellChange();
	    	    	if (before != null && before.getAssignedRoom() != null) {
	    	    		TableCellRooms beforeRooms = new TableCellRooms();
	    	    		for (RoomInfo r: new TreeSet<RoomInfo>(Arrays.asList(before.getAssignedRoom())))
	        	    		beforeRooms.add(r.getName(), r.getColor(), r.getId(), PreferenceLevel.int2string(r.getPref()));
	        	    	room.setFirst(beforeRooms);
	        	    	if (before.getAssignedRoom().length == 0 && after == null)
	        	    		room.setSecond(new TableCellRooms());
	        	    }
	    	    	if (after != null && after.getAssignedRoom() != null) {
	    	    		TableCellRooms afterRooms = new TableCellRooms();
	    	    		for (RoomInfo r: new TreeSet<RoomInfo>(Arrays.asList(after.getAssignedRoom())))
	    	    			afterRooms.add(r.getName(), r.getColor(), r.getId(), PreferenceLevel.int2string(r.getPref()));
	        	    	room.setSecond(afterRooms);
	        	    	if (after.getAssignedRoom().length == 0 && before == null)
	        	    		room.setFirst(new TableCellRooms());	        	    	
	        	    }
	    	    	
					allClasses.add(new TableInterface.TableCellClassName(ca.getClazz().getName()).setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref())));
					allDates.add(date);
					allTimes.add(time);
					allRooms.add(room);
				}
				
        	    Suggestion bSg = record.getBefore();
        	    Suggestion aSg = record.getAfter();
    	        
    	        TableCellMulti studentConfs = new TableCellMulti();
    			studentConfs.add(dispNumber(aSg.getViolatedStudentConflicts() - bSg.getViolatedStudentConflicts()));
    			if (aSg.getCommitedStudentConflicts()-bSg.getCommitedStudentConflicts()!=0) {
    				if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    				studentConfs.add(dispNumber(aSg.getCommitedStudentConflicts()-bSg.getCommitedStudentConflicts()).setFormattedValue("c" + (aSg.getCommitedStudentConflicts()-bSg.getCommitedStudentConflicts())));
        	    }
        	    if (aSg.getDistanceStudentConflicts()-bSg.getDistanceStudentConflicts()!=0) {
        	    	if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
        	    	studentConfs.add(dispNumber(aSg.getDistanceStudentConflicts()-bSg.getDistanceStudentConflicts()).setFormattedValue("d" + (aSg.getDistanceStudentConflicts()-bSg.getDistanceStudentConflicts())));
        	    }
        	    if (aSg.getHardStudentConflicts()-bSg.getHardStudentConflicts()!=0) {
        	    	if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
        	    	studentConfs.add(dispNumber(aSg.getHardStudentConflicts()-bSg.getHardStudentConflicts()).setFormattedValue("h" + (aSg.getHardStudentConflicts()-bSg.getHardStudentConflicts())));
        	    }
        	    if (studentConfs.getNrChunks()>1) studentConfs.add(")");
        	 
        	    if (simple)
        	    	response.addRow(new TableRowInterface(
        	    			idx,
        	    			"gwt.jsp?page=suggestions&menu=hide&id=" + classId + "&history=" + idx,
        	    			MESSAGES.dialogSuggestions(),
        	    			new TableCellInterface<Date>(record.getTimeStamp(), sTS.format(record.getTimeStamp())),
        	    			dispNumber(aSg.getValue()-bSg.getValue()),
        	    			allClasses, allDates, allTimes, allRooms,
        	    			dispNumber(aSg.getUnassignedVariables()-bSg.getUnassignedVariables()),
        	    			studentConfs
        	    			));
        	    else
        	    	response.addRow(new TableRowInterface(
        	    			idx,
        	    			"gwt.jsp?page=suggestions&menu=hide&id=" + classId + "&history=" + idx,
        	    			MESSAGES.dialogSuggestions(),
        	    			new TableCellInterface<Date>(record.getTimeStamp(), sTS.format(record.getTimeStamp())),
        	    			dispNumber(aSg.getValue()-bSg.getValue()),
        	    			allClasses, allDates, allTimes, allRooms,
        	    			dispNumber(aSg.getUnassignedVariables()-bSg.getUnassignedVariables()),
        	    			studentConfs,
        	                dispNumber(aSg.getGlobalTimePreference()-bSg.getGlobalTimePreference()),
        	                dispNumber(aSg.getGlobalRoomPreference()-bSg.getGlobalRoomPreference()),
        	                dispNumber(aSg.getGlobalGroupConstraintPreference()-bSg.getGlobalGroupConstraintPreference()),
        	                dispNumber(aSg.getInstructorDistancePreference()-bSg.getInstructorDistancePreference()),
        	                dispNumber(aSg.getUselessSlots()-bSg.getUselessSlots()),
        	                dispNumber(aSg.getTooBigRooms()-bSg.getTooBigRooms()),
        	                dispNumber(aSg.getDepartmentSpreadPenalty()-bSg.getDepartmentSpreadPenalty()),
        	                dispNumber(aSg.getSpreadPenalty()-bSg.getSpreadPenalty()),
        	                dispNumber(aSg.getPerturbationPenalty()-bSg.getPerturbationPenalty())
            	    		));
    		}
		}
			
			
		
		if (simple)
			response.setHeader(
					new TableHeaderIterface(MESSAGES.colTimeStamp()),
					new TableHeaderIterface(MESSAGES.colScore()),
					new TableHeaderIterface(MESSAGES.colClass()),
					new TableHeaderIterface(MESSAGES.colDate()),
					new TableHeaderIterface(MESSAGES.colTime()),
					new TableHeaderIterface(MESSAGES.colRoom()),
					new TableHeaderIterface(MESSAGES.colShortUnassignments()),
					new TableHeaderIterface(MESSAGES.colNrStudentConflicts()));
		else
			response.setHeader(
					new TableHeaderIterface(MESSAGES.colTimeStamp()),
					new TableHeaderIterface(MESSAGES.colScore()),
					new TableHeaderIterface(MESSAGES.colClass()),
					new TableHeaderIterface(MESSAGES.colDate()),
					new TableHeaderIterface(MESSAGES.colTime()),
					new TableHeaderIterface(MESSAGES.colRoom()),
					new TableHeaderIterface(MESSAGES.colShortUnassignments()),
					new TableHeaderIterface(MESSAGES.colNrStudentConflicts()),
					new TableHeaderIterface(MESSAGES.colShortTimePref()),
					new TableHeaderIterface(MESSAGES.colShortRoomPref()),
					new TableHeaderIterface(MESSAGES.colShortDistPref()),
					new TableHeaderIterface(MESSAGES.colShortInstructorBtbPref()),
					new TableHeaderIterface(MESSAGES.colShortUselessHalfHours()),
					new TableHeaderIterface(MESSAGES.colShortTooBigRooms()),
					new TableHeaderIterface(MESSAGES.colShortDepartmentBalance()),
					new TableHeaderIterface(MESSAGES.colShortSameSubpartBalance()),
					new TableHeaderIterface(MESSAGES.colShortPerturbations()));
		
		SolverPageBackend.fillSolverWarnings(context, solver, SolverType.COURSE, response);
		BackTracker.markForBack(context, "gwt.jsp?page=solutionChanges", MESSAGES.pageSolutionChanges(), true, true);
		
		return response;
	}
	
	public TableCellInterface dispNumber(int value) {
		return new TableCellInterface<Integer>(value, value == 0 ? "" : value <= 0 ? String.valueOf(value) : "+" + String.valueOf(value)).setColor(value < 0 ? "green" : value > 0 ? "red" : null);
	}
	
	public TableCellInterface dispNumber(double value) {
		return new TableCellInterface<Double>(value, Math.round(1000.0 * value) == 0.0 ? "" : (value >= 0.0005 ? "+" : "") + sDF.format(value)).setColor(value < 0 ? "green" : value > 0 ? "red" : null);
	}

}
