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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignmentHistoryRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.AssignmentHistoryResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.model.PreferenceLevel;
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
		boolean usePrefStyles = CommonValues.Yes.eq(UserProperty.HighContrastPreferences.get(context.getUser()));
		
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
				
				CellInterface allClasses = new CellInterface();
				CellInterface allDates = new CellInterface();
				CellInterface allTimes = new CellInterface();
				CellInterface allRooms = new CellInterface();
				Long classId = null;
				
				for (RecordedAssignment assignment: record.getAssignments()) {
					ClassAssignmentDetails before = (assignment.getBefore()==null?null:assignment.getBefore().getDetails(context, solver, false));
	    	    	ClassAssignmentDetails after = (assignment.getAfter()==null?null:assignment.getAfter().getDetails(context, solver, false));
	    	    	ClassAssignmentDetails ca = (after == null ? before : after);
	    	    	if (classId == null) classId = ca.getClazz().getClassId();
	    	    	
	        		ClassAssignmentDetails.TimeInfo timeBefore = (before == null ? null : before.getAssignedTime());
	        		ClassAssignmentDetails.TimeInfo timeAfter = (after == null ? null : after.getAssignedTime());
	    	    	
	    	    	CellInterface d = allDates.add(null).setInline(false).setNoWrap(true);
	    	    	if (timeBefore!=null && (timeAfter==null || !timeBefore.getDatePatternId().equals(timeAfter.getDatePatternId())))
	        			d.addItem(timeBefore.toDateCell(usePrefStyles)).add(" \u2192 ");
	        		if (timeBefore==null && timeAfter!=null)
	        			d.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
	        			.add(" \u2192 ");
	        		if (timeAfter == null)
	        			d.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
	        		else
	        			d.addItem(timeAfter.toDateCell(usePrefStyles));
	        		
	        		CellInterface t = allTimes.add(null).setInline(false).setNoWrap(true);
	        		if (timeBefore!=null && (timeAfter==null || timeBefore.getDays() != timeAfter.getDays() || timeBefore.getStartSlot() != timeAfter.getStartSlot() || timeBefore.getMin() != timeAfter.getMin()))
	        			t.addItem(timeBefore.toCell(usePrefStyles)).add(" \u2192 ");
	        		if (timeBefore==null && timeAfter!=null)
	        			t.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
	        			.add(" \u2192 ");
	        		if (timeAfter == null)
	        			t.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
	        		else
	        			t.addItem(timeAfter.toCell(usePrefStyles));
	        		
	    	    	ClassAssignmentDetails.RoomInfo[] roomBefore = (before == null ? null : before.getAssignedRoom());
	    	    	ClassAssignmentDetails.RoomInfo[] roomAfter = (after == null ? null : after.getAssignedRoom());
	        		
	    	    	CellInterface r = allRooms.add(null).setInline(false).setNoWrap(true);
	    	    	if (roomBefore!=null && (roomAfter==null || !toId(roomBefore).equals(toId(roomAfter))))
	        			r.addItem(toRoomCell(roomBefore, usePrefStyles)).add(" \u2192 ");
	        		if (roomBefore==null && roomAfter!=null)
	        			r.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;")
	        			.add(" \u2192 ");
	        		if (roomAfter == null)
	        			r.add(MESSAGES.notAssigned()).setColor(PreferenceLevel.prolog2color("P")).addStyle("font-style: italic;");
	        		else
	        			r.addItem(toRoomCell(roomAfter, usePrefStyles));

	        		CellInterface c = allClasses.add(null).setInline(false).setNoWrap(true);
	        		c.add(ca.getClazz().getName()).setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref()))
	        			.setClassName(usePrefStyles ? "pref-2" : null);
				}
				
        	    Suggestion bSg = record.getBefore();
        	    Suggestion aSg = record.getAfter();
        	    
        	    CellInterface studentConfs = new CellInterface().setTextAlignment(Alignment.RIGHT);
    			studentConfs.setNoWrap(true);
    			studentConfs.addItem(dispNumber(aSg.getViolatedStudentConflicts() - bSg.getViolatedStudentConflicts()));
    			if (aSg.getCommitedStudentConflicts()-bSg.getCommitedStudentConflicts()!=0) {
    				if (studentConfs.getNrItems() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    				studentConfs.addItem(dispNumber("c", aSg.getCommitedStudentConflicts()-bSg.getCommitedStudentConflicts()));
        	    }
        	    if (aSg.getDistanceStudentConflicts()-bSg.getDistanceStudentConflicts()!=0) {
    				if (studentConfs.getNrItems() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    				studentConfs.addItem(dispNumber("d", aSg.getDistanceStudentConflicts()-bSg.getDistanceStudentConflicts()));
        	    }
        	    if (aSg.getHardStudentConflicts()-bSg.getHardStudentConflicts()!=0) {
    				if (studentConfs.getNrItems() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    				studentConfs.addItem(dispNumber("h", aSg.getHardStudentConflicts()-bSg.getHardStudentConflicts()));
        	    }
        	    if (studentConfs.getNrItems()>1) studentConfs.add(")");
        	    studentConfs.setComparable(
        	    		aSg.getViolatedStudentConflicts() - bSg.getViolatedStudentConflicts(),
        	    		aSg.getCommitedStudentConflicts()-bSg.getCommitedStudentConflicts(),
        	    		aSg.getDistanceStudentConflicts()-bSg.getDistanceStudentConflicts(),
        	    		aSg.getHardStudentConflicts()-bSg.getHardStudentConflicts());
        	    
    	    	LineInterface line = response.addLine();
    	    	line.setId(classId);
    	    	line.setURL("suggestions?menu=hide&id=" + classId + "&history=" + idx);
    	    	line.setDialog(MESSAGES.dialogSuggestions());

    	    	line.addCell(sTS.format(record.getTimeStamp())).setComparable(record.getTimeStamp());
    	    	line.addCell(dispNumber(aSg.getValue()-bSg.getValue()));
    	    	line.addCell(allClasses);
    	    	line.addCell(allDates);
    	    	line.addCell(allTimes);
    	    	line.addCell(allRooms);
    	    	line.addCell(dispNumber(aSg.getUnassignedVariables()-bSg.getUnassignedVariables()));
    	    	line.addCell(studentConfs);
        	 
        	    if (!simple) {
        	    	line.addCell(dispNumber(aSg.getGlobalTimePreference()-bSg.getGlobalTimePreference()));
        	    	line.addCell(dispNumber(aSg.getGlobalRoomPreference()-bSg.getGlobalRoomPreference()));
        	    	line.addCell(dispNumber(aSg.getGlobalGroupConstraintPreference()-bSg.getGlobalGroupConstraintPreference()));
        	    	line.addCell(dispNumber(aSg.getInstructorDistancePreference()-bSg.getInstructorDistancePreference()));
        	    	line.addCell(dispNumber(aSg.getUselessSlots()-bSg.getUselessSlots()));
        	    	line.addCell(dispNumber(aSg.getTooBigRooms()-bSg.getTooBigRooms()));
        	    	line.addCell(dispNumber(aSg.getDepartmentSpreadPenalty()-bSg.getDepartmentSpreadPenalty()));
        	    	line.addCell(dispNumber(aSg.getSpreadPenalty()-bSg.getSpreadPenalty()));
        	    	line.addCell(dispNumber(aSg.getPerturbationPenalty()-bSg.getPerturbationPenalty()));
        	    }
    		}
		}
			
			
		LineInterface line = response.addHeader();
		line.addCell(MESSAGES.colTimeStamp());
		line.addCell(MESSAGES.colScore()).setTextAlignment(Alignment.RIGHT);
		line.addCell(MESSAGES.colClass());
		line.addCell(MESSAGES.colDate());
		line.addCell(MESSAGES.colTime());
		line.addCell(MESSAGES.colRoom());
		line.addCell(MESSAGES.colShortUnassignments());
		if (simple) {
			line.addCell(MESSAGES.colNrStudentConflicts());
		} else {
			line.addCell(MESSAGES.colShortStudentConflicts());
			line.addCell(MESSAGES.colShortTimePref());
			line.addCell(MESSAGES.colShortRoomPref());
			line.addCell(MESSAGES.colShortDistPref());
			line.addCell(MESSAGES.colShortInstructorBtbPref());
			line.addCell(MESSAGES.colShortUselessHalfHours());
			line.addCell(MESSAGES.colShortTooBigRooms());
			line.addCell(MESSAGES.colShortDepartmentBalance());
			line.addCell(MESSAGES.colShortSameSubpartBalance());
			line.addCell(MESSAGES.colShortPerturbations());
		}
		for (int i = 0; i < line.getNrCells(); i++) {
			CellInterface cell = line.getCell(i);
			cell.setSortable(true);
			cell.setClassName("unitime-ClickableTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		if (i > 6)
    			cell.setTextAlignment(Alignment.RIGHT);
		}
		response.setId("AssignmentHistory");
		response.setName(MESSAGES.sectAssignmentHistory());
		response.setClassName("unitime-DataTable");
		
		SolverPageBackend.fillSolverWarnings(context, solver, SolverType.COURSE, response);
		BackTracker.markForBack(context, "solutionChanges", MESSAGES.pageSolutionChanges(), true, true);
		
		return response;
	}
	
	public CellInterface dispNumber(int value) {
		return dispNumber(null, value);
	}
	
	public CellInterface dispNumber(String prefix, int value) {
		return new CellInterface().setText(value == 0 ? "" : (prefix == null ? "" : prefix) +  (value <= 0 ? String.valueOf(value) : "+" + String.valueOf(value))).setComparable(value)
				.setColor(value < 0 ? "#1d6600" : value > 0 ? "#b80000" : null)
				.setTextAlignment(Alignment.RIGHT);
	}
	
	public CellInterface dispNumber(double value) {
		return dispNumber(null, value);
	}
	
	public CellInterface dispNumber(String prefix, double value) {
		return new CellInterface().setText(Math.round(1000.0 * value) == 0.0 ? "" : (prefix == null ? "" : prefix) + (value >= 0.0005 ? "+" : "") + sDF.format(value)).setComparable(value)
				.setColor(value < 0 ? "#1d6600" : value > 0 ? "#b80000" : null)
				.setTextAlignment(Alignment.RIGHT);
	}
	
	private static String toId(ClassAssignmentDetails.RoomInfo[] rooms) {
		String ret = "";
		if (rooms != null)
			for (RoomInfo rm: rooms) {
				ret += (ret.isEmpty() ? "" : ",") + rm.getId();
			}
		return ret;
	}
	
	private static  CellInterface toRoomCell(ClassAssignmentDetails.RoomInfo[] rooms, boolean usePrefStyles) {
		CellInterface r = new CellInterface();
		if (rooms != null)
			for (RoomInfo rm: rooms) {
				if (r.hasItems()) r.add(", ").setInline(true);
				r.addItem(rm.toCell(usePrefStyles).setInline(true));
			}
		return r;
	}

}
