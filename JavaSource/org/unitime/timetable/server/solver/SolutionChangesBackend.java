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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.hibernate.type.LongType;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.TableInterface;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolutionChangesRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolutionChangesResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellChange;
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
import org.unitime.timetable.solver.TimetableSolver.RecordedAssignment;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.SuggestionsModel;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.webutil.BackTracker;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SolutionChangesRequest.class)
public class SolutionChangesBackend implements GwtRpcImplementation<SolutionChangesRequest, SolutionChangesResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static DecimalFormat sDF = new DecimalFormat("0.###",new java.text.DecimalFormatSymbols(Locale.US));
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public SolutionChangesResponse execute(SolutionChangesRequest request, SessionContext context) {
		context.checkPermission(Right.SolutionChanges);
		SolutionChangesResponse response = new SolutionChangesResponse();
		
		context.getUser().setProperty("SuggestionsModel.simpleMode", request.getFilter().getParameterValue("simpleMode"));
		boolean simple = "1".equals(request.getFilter().getParameterValue("simpleMode"));
		SuggestionsModel model = (SuggestionsModel)context.getAttribute("Suggestions.model");
		if (model != null)
			model.setSimpleMode(simple);
		
		context.getUser().setProperty("SolutionChanges.reference", request.getFilter().getParameterValue("reference"));
		int reference = Integer.valueOf(request.getFilter().getParameterValue("reference"));
		
		context.getUser().setProperty("SolutionChanges.reversedMode", request.getFilter().getParameterValue("reversedMode"));
		boolean reversed = "1".equals(request.getFilter().getParameterValue("reversedMode"));
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		
		List<RecordedAssignment> changes = null;
		if (solver==null) {
			response.setMessage(MESSAGES.errorNoSolverLoaded());
        } else if (reference == 0) {
        	if (solver.bestSolutionInfo() == null)
        		response.setMessage(MESSAGES.errorNoBestSolutionSaved());
        	else
        		changes = solver.getChangesToBest();
        } else if (reference == 1) {
        	changes = solver.getChangesToInitial();
        } else {
        	String solutionIdsStr = (String)context.getAttribute(SessionAttribute.SelectedSolution);
        	if (solutionIdsStr == null || solutionIdsStr.isEmpty()) {
        		response.setMessage(MESSAGES.errorNoSolutionSelected());
    		} else {
    			changes = new ArrayList<RecordedAssignment>();
    			for (StringTokenizer s = new StringTokenizer(solutionIdsStr,","); s.hasMoreTokens();) {
    				Long solutionId = Long.valueOf(s.nextToken());
    				List<RecordedAssignment> ch = solver.getChangesToSolution(solutionId);
    				if (ch != null)
    					changes.addAll(ch);
    			}
    		}
        }
		
		if (changes != null)
			for (RecordedAssignment assignment: changes) {
				ClassAssignmentDetails before = (assignment.getBefore()==null?null:assignment.getBefore().getDetails(context, solver, false));
    	    	ClassAssignmentDetails after = (assignment.getAfter()==null?null:assignment.getAfter().getDetails(context, solver, false));
    	    	if (reversed) {
    	    		ClassAssignmentDetails x = after; after = before; before = x;
    	    	}
    	    	ClassAssignmentDetails ca = (after==null?before:after);
    	    	
    	    	TableCellChange date = new TableCellChange(
    	    			before == null || before.getTime() == null ? null : new TableCellInterface(before.getTime().getDatePatternName()).setColor(PreferenceLevel.int2color(before.getTime().getDatePatternPreference())),
    	    			after == null || after.getTime() == null ? null : new TableCellInterface(after.getTime().getDatePatternName()).setColor(PreferenceLevel.int2color(after.getTime().getDatePatternPreference())));
    	    	
    	    	TableCellChange time = new TableCellChange(
    	    			before == null || before.getTime() == null ? null : new TableInterface.TableCellTime(before.getTime().getDaysName() + " " + before.getTime().getStartTime() + " - " + before.getTime().getEndTime())
    	    	    			.setId(before.getClazz().getClassId() + "," + before.getTime().getDays() + "," + before.getTime().getStartSlot()).setColor(PreferenceLevel.int2color(before.getTime().getPref())),
    	    	    	after == null || after.getTime() == null ? null : new TableInterface.TableCellTime(after.getTime().getDaysName() + " " + after.getTime().getStartTime() + " - " + after.getTime().getEndTime())
    	    	    	    	.setId(after.getClazz().getClassId() + "," + after.getTime().getDays() + "," + after.getTime().getStartSlot()).setColor(PreferenceLevel.int2color(after.getTime().getPref())));
    	    	
    	    	String link = "id=" + ca.getClazz().getClassId();
    	    	if (before != null)
    	    		link += "&days=" + before.getTime().getDays() + "&slot=" + before.getTime().getStartSlot() + "&pid=" + before.getTime().getPatternId() + "&did=" + before.getTime().getDatePatternId();
    	    	
    	    	TableCellChange room = new TableCellChange();
    	    	if (before != null && before.getRoom() != null) {
    	    		TableCellRooms beforeRooms = new TableCellRooms();
    	    		String rid = "";
        	    	for (int i = 0; i < before.getRoom().length; i++) {
        	    		rid += (i > 0 ? "," : "") + before.getRoom()[i].getId();
        	    		beforeRooms.add(
        	    				before.getRoom()[i].getName(),
        	    				before.getRoom()[i].getColor(),
        	    				before.getRoom()[i].getId(),
        	    				PreferenceLevel.int2string(before.getRoom()[i].getPref()));
        	    	}
        	    	room.setFirst(beforeRooms);
        	    	link += "&room=" + rid;
        	    	if (before.getRoom().length == 0 && after == null)
        	    		room.setSecond(new TableCellRooms());
        	    }
    	    	if (after != null && after.getRoom() != null) {
    	    		TableCellRooms afterRooms = new TableCellRooms();
    	    		for (int i = 0; i < after.getRoom().length; i++) {
    	    			afterRooms.add(
    	    					after.getRoom()[i].getName(),
    	    					after.getRoom()[i].getColor(),
    	    					after.getRoom()[i].getId(),
        	    				PreferenceLevel.int2string(after.getRoom()[i].getPref()));
        	    	}
        	    	room.setSecond(afterRooms);
        	    	if (after.getRoom().length == 0 && before == null)
        	    		room.setFirst(new TableCellRooms());
        	    }
    	    	
    	    	TableInterface.TableCellItems instructor = new TableInterface.TableCellItems();
        	    if (ca.getInstructor() != null)
        	    	for (int i = 0; i < ca.getInstructor().length; i++) {
        	    		instructor.add(ca.getInstructor()[i].getName(), ca.getInstructor()[i].getColor(), ca.getInstructor()[i].getId());
        	    	}
        	    
    	        AssignmentPreferenceInfo bInf = (before==null?null:before.getInfo());
    	        AssignmentPreferenceInfo aInf = (after==null?null:after.getInfo());
    	        if (aInf==null) aInf = new AssignmentPreferenceInfo();
    	        if (bInf==null) bInf = new AssignmentPreferenceInfo();
    	        
    	        TableCellMulti studentConfs = new TableCellMulti();
    			studentConfs.add(dispNumber(aInf.getNrStudentConflicts() - bInf.getNrStudentConflicts()));
    			if (aInf.getNrCommitedStudentConflicts()-bInf.getNrCommitedStudentConflicts()!=0) {
    				if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
    				studentConfs.add(dispNumber(aInf.getNrCommitedStudentConflicts()-bInf.getNrCommitedStudentConflicts()).setFormattedValue("c" + (aInf.getNrCommitedStudentConflicts()-bInf.getNrCommitedStudentConflicts())));
        	    }
        	    if (aInf.getNrDistanceStudentConflicts()-bInf.getNrDistanceStudentConflicts()!=0) {
        	    	if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
        	    	studentConfs.add(dispNumber(aInf.getNrDistanceStudentConflicts()-bInf.getNrDistanceStudentConflicts()).setFormattedValue("d" + (aInf.getNrDistanceStudentConflicts()-bInf.getNrDistanceStudentConflicts())));
        	    }
        	    if (aInf.getNrHardStudentConflicts()-bInf.getNrHardStudentConflicts()!=0) {
        	    	if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
        	    	studentConfs.add(dispNumber(aInf.getNrHardStudentConflicts()-bInf.getNrHardStudentConflicts()).setFormattedValue("h" + (aInf.getNrHardStudentConflicts()-bInf.getNrHardStudentConflicts())));
        	    }
        	    if (studentConfs.getNrChunks()>1) studentConfs.add(")");
        	    
        	    if (simple)
        	    	response.addRow(new TableRowInterface(
        	    			ca.getClazz().getClassId(),
        	    			"gwt.jsp?page=suggestions&menu=hide&" + link,
        	    			MESSAGES.dialogSuggestions(),
        	    			new TableInterface.TableCellClassName(ca.getClazz().getName()).setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref())),
        	    			date, time, room, instructor, studentConfs
        	    			));
        	    else
        	    	response.addRow(new TableRowInterface(
        	    			ca.getClazz().getClassId(),
        	    			"gwt.jsp?page=suggestions&menu=hide&" + link,
        	    			MESSAGES.dialogSuggestions(),
        	    			new TableInterface.TableCellClassName(ca.getClazz().getName()).setColor(PreferenceLevel.prolog2color(ca.getClazz().getPref())),
        	    			date, time, room, instructor, studentConfs,
        	                dispNumber(aInf.getTimePreference()-bInf.getTimePreference()),
        	                dispNumber(aInf.sumRoomPreference()-bInf.sumRoomPreference()),
        	                dispNumber(aInf.getGroupConstraintPref()-bInf.getGroupConstraintPref()),
        	                dispNumber(aInf.getBtbInstructorPreference()-bInf.getBtbInstructorPreference()),
        	                dispNumber(aInf.getUselessHalfHours()-bInf.getUselessHalfHours()),
        	                dispNumber(aInf.getTooBigRoomPreference()-bInf.getTooBigRoomPreference()),
        	                dispNumber(aInf.getDeptBalancPenalty()-bInf.getDeptBalancPenalty()),
        	                dispNumber(aInf.getSpreadPenalty()-bInf.getSpreadPenalty()),
        	                dispNumber(aInf.getPerturbationPenalty()-bInf.getPerturbationPenalty())
            	    		));
    		}
		
		if (simple)
			response.setHeader(
					new TableHeaderIterface(MESSAGES.colClass()),
					new TableHeaderIterface(MESSAGES.colDate()),
					new TableHeaderIterface(MESSAGES.colTime()),
					new TableHeaderIterface(MESSAGES.colRoom()),
					new TableHeaderIterface(MESSAGES.colInstructor()),
					new TableHeaderIterface(MESSAGES.colNrStudentConflicts()));
		else
			response.setHeader(
					new TableHeaderIterface(MESSAGES.colClass()),
					new TableHeaderIterface(MESSAGES.colDate()),
					new TableHeaderIterface(MESSAGES.colTime()),
					new TableHeaderIterface(MESSAGES.colRoom()),
					new TableHeaderIterface(MESSAGES.colInstructor()),
					new TableHeaderIterface(MESSAGES.colShortStudentConflicts()),
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
		
		if (ApplicationProperty.TimeGridShowCrosslists.isTrue())
			addCrosslistedNames(response, ApplicationProperty.SolverShowClassSufix.isTrue(), ApplicationProperty.SolverShowConfiguratioName.isTrue());
		
		return response;
	}
	
	public TableCellInterface dispNumber(int value) {
		return new TableCellInterface<Integer>(value, value == 0 ? "" : value <= 0 ? String.valueOf(value) : "+" + String.valueOf(value)).setColor(value < 0 ? "green" : value > 0 ? "red" : null);
	}
	
	public TableCellInterface dispNumber(double value) {
		return new TableCellInterface<Double>(value, Math.round(1000.0 * value) == 0.0 ? "" : (value >= 0.0005 ? "+" : "") + sDF.format(value)).setColor(value < 0 ? "green" : value > 0 ? "red" : null);
	}
	
	public static void addCrosslistedNames(TableInterface table, boolean showClassSuffix, boolean showConfigNames) {
		Map<Long, TableInterface.TableRowInterface> id2row = new HashMap<Long, TableInterface.TableRowInterface>();
		for (TableInterface.TableRowInterface row: table.getRows()) {
			if (row.hasId()) id2row.put(row.getId(), row);
		}
		if (id2row.isEmpty()) return;
		for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(
				"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
				"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr").setParameterList("classIds", id2row.keySet(), LongType.INSTANCE).setCacheable(true).list()) {
			Class_ clazz = (Class_)o[0];
			CourseOffering course = (CourseOffering)o[1];
			TableInterface.TableRowInterface row = id2row.get(clazz.getUniqueId());
			if (row != null)
				((TableInterface.TableCellClassName)row.getCell(0)).addAlternative(clazz.getClassLabel(course, showClassSuffix, showConfigNames));
		}
	}

}
