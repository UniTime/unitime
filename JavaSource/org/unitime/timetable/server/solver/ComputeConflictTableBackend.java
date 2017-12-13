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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.coursett.constraint.FlexibleConstraint;
import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.JenrlConstraint;
import org.cpsolver.coursett.criteria.StudentConflict;
import org.cpsolver.coursett.criteria.additional.ImportantStudentConflict;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.Student;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solver.Solver;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassAssignmentDetails;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ComputeConflictTableRequest;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.DistributionInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.JenrlInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.StudentConflictInfo;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableSolver;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.GroupConstraintInfo;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ComputeConflictTableRequest.class)
public class ComputeConflictTableBackend implements GwtRpcImplementation<ComputeConflictTableRequest, GwtRpcResponseList<ClassAssignmentDetails>> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public GwtRpcResponseList<ClassAssignmentDetails> execute(ComputeConflictTableRequest request, SessionContext context) {
		context.checkPermission(Right.Suggestions);
		
		SuggestionsContext cx = new SuggestionsContext();
		String instructorFormat = context.getUser().getProperty(UserProperty.NameFormat);
    	if (instructorFormat != null)
    		cx.setInstructorNameFormat(instructorFormat);
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		if (solver == null)
			throw new GwtRpcException(MESSAGES.warnSolverNotLoaded());
		if (solver.isWorking())
			throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
		
		List<ClassAssignmentDetails> response = solver.computeConfTable(cx, request);
		
		return (response == null ? null : new GwtRpcResponseList<ClassAssignmentDetails>(response));
	}
	
	public static List<ClassAssignmentDetails> computeConfTable(SuggestionsContext context, TimetableSolver solver, ComputeConflictTableRequest request) {
		List<ClassAssignmentDetails> conflicts = new ArrayList<ClassAssignmentDetails>();
    	
        Lecture lecture = null;
		for (Lecture l: solver.currentSolution().getModel().variables())
    		if (l.getClassId().equals(request.getClassId()))
    			lecture = l;
		if (lecture == null)
			for (Lecture l: ((TimetableModel)solver.currentSolution().getModel()).constantVariables())
	    		if (l.getClassId().equals(request.getClassId()))
	    			lecture = l;
    	
    	for (TimeLocation t: lecture.timeLocations()) {
    		if (PreferenceLevel.sProhibited.equals(PreferenceLevel.int2prolog(t.getPreference()))) continue;
    		if (t.getPreference() > 500) continue;
    		ClassAssignmentDetails conflict = createConflict(context, solver, lecture, t);
    		if (conflict != null)
    			conflicts.add(conflict);
    	}
    	
		return conflicts;
	}
	
	protected static ClassAssignmentDetails createConflict(SuggestionsContext context, Solver solver, Lecture lecture, TimeLocation time) {
    	Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
    	
    	Placement currentPlacement = assignment.getValue(lecture);
    	if (currentPlacement == null) {
    		List<Placement> values = lecture.values(assignment);
    		currentPlacement = (values.isEmpty() ? null : values.get(0));
    	}
    	
    	if (currentPlacement == null) return null;
    	
    	Placement dummyPlacement = null;
		if (currentPlacement.isMultiRoom())
			dummyPlacement = new Placement(lecture, time, currentPlacement.getRoomLocations());
		else
			dummyPlacement = new Placement(lecture, time, currentPlacement.getRoomLocation());
		
		ClassAssignmentDetails suggestion = ClassAssignmentDetailsBackend.createClassAssignmentDetails(context, solver, lecture, dummyPlacement, false, false);
		TimetableModel m = (TimetableModel)solver.currentSolution().getModel();
		StudentConflict imp = (StudentConflict)m.getCriterion(ImportantStudentConflict.class);
		
		Map<Placement, Integer> committed = new HashMap<Placement, Integer>();
		if (dummyPlacement.getCommitedConflicts()>0) {
			for (Student s: lecture.students()) {
				Set<Placement> confs = s.conflictPlacements(dummyPlacement);
				if (confs == null) continue;
				for (Placement commitedPlacement: confs) {
					Integer current = committed.get(commitedPlacement);
					committed.put(commitedPlacement, new Integer(1+(current==null?0:current.intValue())));
				}
			}
		}

		for (JenrlConstraint jenrl: lecture.jenrlConstraints()) {
    		long j = jenrl.jenrl(assignment, lecture, dummyPlacement);
    		if (j > 0 && !jenrl.isToBeIgnored()) {
    			if (jenrl.areStudentConflictsDistance(assignment, dummyPlacement)) continue;
    			JenrlInfo jInfo = new JenrlInfo();
    			jInfo.setJenrl((int)j);
    			jInfo.setIsHard(jenrl.areStudentConflictsHard());
    			jInfo.setIsDistance(jenrl.areStudentConflictsDistance(assignment, dummyPlacement));
    			jInfo.setIsImportant(imp != null && jenrl.priority() > 0.0);
    			jInfo.setIsWorkDay(jenrl.areStudentConflictsWorkday(assignment, dummyPlacement));
    			jInfo.setIsFixed(jenrl.first().nrTimeLocations() == 1 && jenrl.second().nrTimeLocations() == 1);
    			jInfo.setIsInstructor(jenrl.getNrInstructors() > 0);
    			if (jenrl.first().equals(lecture)) {
    				if (jenrl.second().isCommitted()) jInfo.setIsCommited(true);
    				suggestion.addStudentConflict(new StudentConflictInfo(jInfo, ClassAssignmentDetailsBackend.createClassAssignmentDetails(context, solver, jenrl.second(), false, false)));
    			} else {
    				if (jenrl.first().isCommitted()) jInfo.setIsCommited(true);
    				suggestion.addStudentConflict(new StudentConflictInfo(jInfo, ClassAssignmentDetailsBackend.createClassAssignmentDetails(context, solver, jenrl.first(), false, false)));
    			}
    		}
    	}
		
		for (Map.Entry<Placement, Integer> x: committed.entrySet()) {
			Placement p = x.getKey();
			Integer cnt = x.getValue();
        	JenrlInfo jenrl = new JenrlInfo();
        	jenrl.setIsCommited(true);
        	jenrl.setJenrl(cnt.intValue());
        	jenrl.setIsFixed(lecture.nrTimeLocations()==1);
        	jenrl.setIsHard(lecture.isSingleSection());
        	jenrl.setIsDistance(StudentConflict.distance(m.getDistanceMetric(), dummyPlacement, p));
        	jenrl.setIsWorkDay(StudentConflict.workday(m.getStudentWorkDayLimit(), dummyPlacement, p));
        	suggestion.addStudentConflict(new StudentConflictInfo(jenrl, ClassAssignmentDetailsBackend.createClassAssignmentDetails(context, solver, p.variable(), p, false, false)));
		}
    	for (GroupConstraint gc: lecture.groupConstraints()) {
    		if (gc.getType() == GroupConstraint.ConstraintType.SAME_ROOM) continue;
    		int curPref = gc.getCurrentPreference(assignment, dummyPlacement);
    		if (gc.getType() == GroupConstraint.ConstraintType.BTB) {
    			gc.setType(GroupConstraint.ConstraintType.BTB_TIME);
    			curPref = gc.getCurrentPreference(assignment, dummyPlacement);
    			gc.setType(GroupConstraint.ConstraintType.BTB);
    		}
    		if (gc.getType() == GroupConstraint.ConstraintType.SAME_STUDENTS) {
    			gc.setType(GroupConstraint.ConstraintType.DIFF_TIME);
    			curPref = gc.getCurrentPreference(assignment, dummyPlacement);
    			gc.setType(GroupConstraint.ConstraintType.SAME_STUDENTS);
    		}
    		boolean sat = (curPref <= 0);
    		if (sat) continue;
    		DistributionInfo dist = new DistributionInfo(ClassAssignmentDetailsBackend.toGroupConstraintInfo(new GroupConstraintInfo(assignment, gc)));
    		dist.getInfo().setValue((double)curPref);
			for (Lecture another: gc.variables()) {
				if (!another.equals(lecture) && assignment.getValue(another)!=null)
					dist.addClass(ClassAssignmentDetailsBackend.createClassAssignmentDetails(context, solver, another, false, false));
			}
			suggestion.addDistributionConflict(dist);
    	}
    	HashMap<Lecture, Placement> dummies = new HashMap<Lecture, Placement>();
    	if (dummyPlacement != null) dummies.put(lecture, dummyPlacement);
    	for (FlexibleConstraint fc: lecture.getFlexibleGroupConstraints()) {
    		if (fc.isHard() || fc.getNrViolations(assignment, null, dummies) == 0.0) continue;
    		DistributionInfo dist = new DistributionInfo(ClassAssignmentDetailsBackend.toGroupConstraintInfo(new GroupConstraintInfo(assignment, fc)));
    		dist.getInfo().setValue(Math.abs(fc.getCurrentPreference(assignment, null, dummies)));
    		for (Lecture another: fc.variables()) {
				if (!another.equals(lecture) && assignment.getValue(another)!=null)
					dist.addClass(ClassAssignmentDetailsBackend.createClassAssignmentDetails(context, solver, another, false, false));
			}
    		suggestion.addDistributionConflict(dist);
    	}
    	
    	return suggestion;
    }
}
