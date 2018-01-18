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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.coursett.constraint.FlexibleConstraint;
import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.InstructorConstraint;
import org.cpsolver.coursett.constraint.JenrlConstraint;
import org.cpsolver.coursett.criteria.StudentCommittedConflict;
import org.cpsolver.coursett.criteria.StudentConflict;
import org.cpsolver.coursett.criteria.StudentDistanceConflict;
import org.cpsolver.coursett.criteria.StudentHardConflict;
import org.cpsolver.coursett.criteria.StudentOverlapConflict;
import org.cpsolver.coursett.criteria.placement.DeltaTimePreference;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.Student;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.criteria.Criterion;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.CPSolverMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.BtbInstructorInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassAssignmentDetails;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.DistributionInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.RoomInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignmentsRequest;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.StudentConflictInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.Suggestion;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.TimeInfo;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellInterface;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellMulti;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableSolver;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.JenrlInfo;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SelectedAssignmentsRequest.class)
public class SelectedAssignmentBackend implements GwtRpcImplementation<SelectedAssignmentsRequest, Suggestion> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static CPSolverMessages MSG = Localization.create(CPSolverMessages.class);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public Suggestion execute(SelectedAssignmentsRequest request, SessionContext context) {
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
		
		Suggestion response = solver.getSelectedSuggestion(cx, request);
		if (request.getClassId() != null)
			response.setSelectedPlacement(solver.getClassAssignmentDetails(cx, request.getClassId(), true, true));
		
		return response;
	}
	
	public static Placement getPlacement(TimetableModel model, SelectedAssignment assignment, boolean checkValidity) {
		for (Lecture lecture: model.variables()) {
			if (!lecture.getClassId().equals(assignment.getClassId())) continue;
    		TimeLocation timeLocation = null;
        	for (TimeLocation t: lecture.timeLocations()) {
        		if (t.getDayCode() != assignment.getDays()) continue;
        		if (t.getStartSlot() != assignment.getStartSlot()) continue;
        		if (!t.getTimePatternId().equals(assignment.getPatternId())) continue;
        		if (!t.getDatePatternId().equals(assignment.getDatePatternId())) continue;
        		timeLocation = t; break;
        	}
        	List<RoomLocation> roomLocations = new ArrayList<RoomLocation>();
        	if (lecture.getNrRooms() > 0) {
            	for (Long roomId: assignment.getRoomIds()) {
                	for (RoomLocation r: lecture.roomLocations()) {
                		if (r.getId().equals(roomId))
                			roomLocations.add(r);
                	}
        		}
        	}
    		if (timeLocation != null && roomLocations.size() == lecture.getNrRooms()) {
    			Placement placement = new Placement(lecture, timeLocation, roomLocations);
    			if (checkValidity && !placement.isValid()) return null;
    			return placement;
    		}
		}
		return null;
	}
	
	public static void setAssigned(ClassAssignmentDetails details, SelectedAssignment assignment) {
		details.setAssignedTime(null);
		if (details.hasTimes())
			for (TimeInfo time: details.getTimes()) {
				if (time.getDays() == assignment.getDays() && time.getStartSlot() == assignment.getStartSlot() &&
					(assignment.getPatternId() == null || assignment.getPatternId().equals(time.getPatternId())) &&
					(assignment.getDatePatternId() == null || assignment.getDatePatternId().equals(time.getDatePatternId()))) {
					details.setAssignedTime(time);
				}
			}
		if (details.getNrAssignedRooms() > 0) details.getAssignedRoom().clear();
		if (details.hasRooms() && assignment.getRoomIds() != null) {
			for (Long roomId: assignment.getRoomIds()) {
				for (RoomInfo room: details.getRooms()) {
					if (room.getId().equals(roomId)) {
						details.setAssignedRoom(room); break;
					}
				}
			}
		}
	}
	
	public static ClassAssignmentDetails createClassAssignmentDetails(SuggestionsContext context, Solver solver, Lecture lecture, Placement oldPlacement, Placement newPlacement) {
		ClassAssignmentDetails details = ClassAssignmentDetailsBackend.createClassAssignmentDetails(context, solver, lecture, oldPlacement, false, false);
		if (newPlacement != null) {
			if (newPlacement.isMultiRoom()) {
				for (RoomLocation room: newPlacement.getRoomLocations()) {
					details.setAssignedRoom(new RoomInfo(
							room.getName(),
							room.getId(),
							room.getRoomSize(),
							(room.getPreference() == 0 && lecture.nrRoomLocations() == lecture.getNrRooms() ? PreferenceLevel.sIntLevelRequired : room.getPreference())
							));
				}
			} else {
				RoomLocation room = newPlacement.getRoomLocation();
				details.setAssignedRoom(new RoomInfo(
						room.getName(),
						room.getId(),
						room.getRoomSize(),
						(room.getPreference() == 0 && lecture.nrRoomLocations() == lecture.getNrRooms() ? PreferenceLevel.sIntLevelRequired : room.getPreference())
						));
			}
			TimeLocation time = newPlacement.getTimeLocation();
			int min = Constants.SLOT_LENGTH_MIN * time.getNrSlotsPerMeeting() - time.getBreakTime();
			details.setAssignedTime(new TimeInfo(
					time.getDayCode(),
					time.getStartSlot(),
					(time.getPreference() == 0 && lecture.nrTimeLocations() == 1 ? PreferenceLevel.sIntLevelRequired : time.getPreference()),
					min,
					time.getDatePatternName(),
					time.getTimePatternId(),
					time.getDatePatternId(),
					time.getDatePatternPreference()
					));
			if (!lecture.getInstructorConstraints().isEmpty()) {
				for (int i=0;i<lecture.getInstructorConstraints().size();i++) {
					InstructorConstraint ic = (InstructorConstraint)lecture.getInstructorConstraints().get(i);
					details.setInstructor(new InstructorInfo(ic.getName(), ic.getResourceId()));
				}
			}
			Map<String, String> translations = context.courseObjectives();
			for (Criterion<Lecture, Placement> criterion: lecture.getModel().getCriteria()) {
				if (criterion instanceof StudentOverlapConflict) continue;
				if (criterion instanceof DeltaTimePreference) continue;
				String translatedName = (translations == null || translations.isEmpty() ? null : translations.get(criterion.getName()));
				if (translatedName != null)
					details.setAssignedObjective(translatedName, criterion.getValue(solver.currentSolution().getAssignment(), newPlacement, null));
				else
					details.setAssignedObjective(criterion.getName(), criterion.getValue(solver.currentSolution().getAssignment(), newPlacement, null));
			}
		}
		return details;
	}
	
	public static Suggestion createSuggestion(SuggestionsContext context, TimetableSolver solver, Map<Lecture, Placement> initialAssignments, List<Long> order, Collection<Placement> unresolvedConflicts) {
		return createSuggestion(context, solver, initialAssignments, order, unresolvedConflicts, null);
	}
	
	public static Suggestion createSuggestion(SuggestionsContext context, TimetableSolver solver, Map<Lecture, Placement> initialAssignments, List<Long> order, Collection<Placement> unresolvedConflicts, Map<Lecture, Placement> unresolvedAssignments) {
		Suggestion suggestion = new Suggestion();
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
    	if (unresolvedConflicts != null) {
    		for (Placement p: unresolvedConflicts) {
    			suggestion.addUnresolvedConflict(createClassAssignmentDetails(context, solver, p.variable(), p, null));
    		}
    	}
        if (initialAssignments != null) {
        	Set<JenrlConstraint> jenrls = new HashSet<JenrlConstraint>();
        	Set<GroupConstraint> gcs = new HashSet<GroupConstraint>();
        	Set<FlexibleConstraint> fcs = new HashSet<FlexibleConstraint>();
        	Map<Placement, Map<Placement, Integer>> committed = new HashMap<Placement, Map<Placement, Integer>>();
        	for (Lecture lecture: assignment.assignedVariables()) {
        		Placement p = assignment.getValue(lecture);
        		if (unresolvedAssignments != null && unresolvedAssignments.containsKey(lecture))
        			p = unresolvedAssignments.get(lecture);
        		Placement ini = initialAssignments.get(p.variable());
        		if (ini==null || !ini.equals(p)) {
        			suggestion.addDifferentAssignment(createClassAssignmentDetails(context, solver, p.variable(), ini, p));
        			jenrls.addAll(lecture.activeJenrls(assignment));
        			if (p.getCommitedConflicts() > 0) {
        				Map<Placement, Integer> x = new HashMap<Placement, Integer>();
        				for (Iterator i=lecture.students().iterator();i.hasNext();) {
        					Student s = (Student)i.next();
        					Set confs = s.conflictPlacements(p);
        					if (confs==null) continue;
        					for (Iterator j=confs.iterator();j.hasNext();) {
        						Placement commitedPlacement = (Placement)j.next();
        						Integer current = (Integer)x.get(commitedPlacement);
        						x.put(commitedPlacement, new Integer(1 + (current == null ? 0 : current.intValue())));
        					}
        				}
        				committed.put(p, x);
        			}
        			gcs.addAll(lecture.groupConstraints());
        			fcs.addAll(lecture.getFlexibleGroupConstraints());
        			for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
        			    for (Lecture other: ic.variables()) {
        			    	Placement otherPlacement = assignment.getValue(other);
        			        if (other.equals(lecture) || otherPlacement==null) continue;
        			        int pref = ic.getDistancePreference(p, otherPlacement);
        			        if (pref==PreferenceLevel.sIntLevelNeutral) continue;
        			        BtbInstructorInfo conf = new BtbInstructorInfo();
        			        conf.setOther(createClassAssignmentDetails(context, solver, p.variable(), p, null));
        			        conf.setAnother(createClassAssignmentDetails(context, solver, otherPlacement.variable(), otherPlacement, null));
        			        conf.setPreference(pref);
        			        suggestion.addBtbInstructorConflict(conf);
        			    }
        			}
        		}
        	}
            if (unresolvedAssignments != null)
                for (Map.Entry<Lecture, Placement> entry: unresolvedAssignments.entrySet()) {
                	Lecture lecture = entry.getKey();
                	Placement p = entry.getValue();
                	if (assignment.getValue(lecture) != null) continue;
                	Placement ini = initialAssignments.get(p.variable());
            		if (ini==null || !ini.equals(p)) {
            			suggestion.addDifferentAssignment(createClassAssignmentDetails(context, solver, p.variable(), ini, p));
            			jenrls.addAll(lecture.activeJenrls(assignment));
            			if (p.getCommitedConflicts() > 0) {
            				Map<Placement, Integer> x = new HashMap<Placement, Integer>();
            				for (Iterator i=lecture.students().iterator();i.hasNext();) {
            					Student s = (Student)i.next();
            					Set confs = s.conflictPlacements(p);
            					if (confs==null) continue;
            					for (Iterator j=confs.iterator();j.hasNext();) {
            						Placement commitedPlacement = (Placement)j.next();
            						Integer current = (Integer)x.get(commitedPlacement);
            						x.put(commitedPlacement, new Integer(1 + (current == null ? 0 : current.intValue())));
            					}
            				}
            				committed.put(p, x);
            			}
            			gcs.addAll(lecture.groupConstraints());
            			fcs.addAll(lecture.getFlexibleGroupConstraints());
            			for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
            			    for (Lecture other: ic.variables()) {
            			    	Placement otherPlacement = assignment.getValue(other);
            			        if (other.equals(lecture) || otherPlacement==null) continue;
            			        int pref = ic.getDistancePreference(p, otherPlacement);
            			        if (pref==PreferenceLevel.sIntLevelNeutral) continue;
            			        BtbInstructorInfo conf = new BtbInstructorInfo();
            			        conf.setOther(createClassAssignmentDetails(context, solver, p.variable(), p, null));
            			        conf.setAnother(createClassAssignmentDetails(context, solver, otherPlacement.variable(), otherPlacement, null));
            			        conf.setPreference(pref);
            			        suggestion.addBtbInstructorConflict(conf);
            			    }
            			}
            		}
                	
                }
            if (order != null && suggestion.hasDifferentAssignments())
            	Collections.sort(suggestion.getDifferentAssignments(), new ClassAssignmentDetailsComparator(order));
            for (JenrlConstraint jenrl: jenrls) {
            	if (jenrl.jenrl() <= 0.0) continue;
            	StudentConflictInfo conf = new StudentConflictInfo();
            	conf.setOther(createClassAssignmentDetails(context, solver, jenrl.first(), assignment.getValue(jenrl.first()), null));
            	conf.setAnother(createClassAssignmentDetails(context, solver, jenrl.second(), assignment.getValue(jenrl.second()), null));
            	if (suggestion.hasDifferentAssignments()) {
            		int i1 = suggestion.getDifferentAssignments().indexOf(conf.getOther());
            		int i2 = suggestion.getDifferentAssignments().indexOf(conf.getAnother());
            		if (i2 > 0 && i1 < i2) {
            			ClassAssignmentDetails d = conf.getOther();
            			conf.setOther(conf.getAnother());
            			conf.setAnother(d);
            		}
            	}
            	conf.setInfo(ClassAssignmentDetailsBackend.toJenrlInfo(new JenrlInfo(solver, jenrl)));
            	suggestion.addStudentConflict(conf);
            }
            for (GroupConstraint gc: gcs) {
            	if (gc.isSatisfied(assignment)) continue;
            	DistributionInfo dist = new DistributionInfo();
            	dist.setInfo(ClassAssignmentDetailsBackend.toGroupConstraintInfo(new org.unitime.timetable.solver.ui.GroupConstraintInfo(assignment, gc)));
            	for (Lecture another: gc.variables()) {
					Placement anotherPlacement = assignment.getValue(another);
					if (anotherPlacement != null)
						dist.addClass(createClassAssignmentDetails(context, solver, another, anotherPlacement, null));
            	}
            	suggestion.addDistributionConflict(dist);
            }
            for (FlexibleConstraint fc: fcs) {
            	if (fc.isHard() || fc.getNrViolations(assignment, new HashSet<Placement>(), new HashMap<Lecture, Placement>()) == 0.0) continue;
            	DistributionInfo dist = new DistributionInfo();
            	dist.setInfo(ClassAssignmentDetailsBackend.toGroupConstraintInfo(new org.unitime.timetable.solver.ui.GroupConstraintInfo(assignment, fc)));
            	for (Lecture another: fc.variables()) {
					Placement anotherPlacement = assignment.getValue(another);
					if (anotherPlacement != null)
						dist.addClass(createClassAssignmentDetails(context, solver, another, anotherPlacement, null));
            	}
            	suggestion.addDistributionConflict(dist);
            }
        }
        TimetableModel m = (TimetableModel)solver.currentSolution().getModel();
        suggestion.setValue(m.getTotalValue(assignment));
        suggestion.setUnassignedVariables(m.nrUnassignedVariables(assignment));
        Map<String, String> translations = context.courseObjectives();
        for (Criterion<Lecture, Placement> c: m.getCriteria()) {
        	if (c instanceof StudentOverlapConflict) continue;
        	if (c instanceof DeltaTimePreference) continue;
        	String translatedName = (translations == null || translations.isEmpty() ? null : translations.get(c.getName()));
        	if (translatedName != null)
        		suggestion.setCriterion(translatedName, c.getValue(assignment));
        	else
        		suggestion.setCriterion(c.getName(), c.getValue(assignment));
        }
        
		Criterion<Lecture, Placement> sc = m.getCriterion(StudentConflict.class);
        Criterion<Lecture, Placement> shc = m.getCriterion(StudentHardConflict.class);
        Criterion<Lecture, Placement> sdc = m.getCriterion(StudentDistanceConflict.class);
        Criterion<Lecture, Placement> scc = m.getCriterion(StudentCommittedConflict.class);
        long studentConflicts = Math.round((scc == null ? 0.0 : scc.getValue(assignment)) + (sc == null ? 0.0 : sc.getValue(assignment)) - context.getBaseStudentConflicts());
        long studentConflictsCommitted = Math.round((scc == null ? 0.0 : scc.getValue(assignment)) - context.getBaseStudentConflictsCommitted());
        long studentConflictsDistance = Math.round((sdc == null ? 0.0 : sdc.getValue(assignment)) - context.getBaseStudentConflictsDistance());
        long studentConflictsHard = Math.round((shc == null ? 0.0 : shc.getValue(assignment)) - context.getBaseStudentConflictsHard());
        TableCellMulti studentConfs = new TableCellMulti();
		studentConfs.add(dispNumber(studentConflicts));
		if (studentConflictsCommitted != 0) {
			if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
			studentConfs.add(dispNumber(studentConflictsCommitted).setFormattedValue("c" + (studentConflictsCommitted > 0 ? "+" : "") + studentConflictsCommitted));
	    }
	    if (studentConflictsDistance != 0) {
	    	if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
	    	studentConfs.add(dispNumber(studentConflictsDistance).setFormattedValue("d" + (studentConflictsDistance > 0 ? "+" : "") + studentConflictsDistance));
	    }
	    if (studentConflictsHard != 0) {
	    	if (studentConfs.getNrChunks() <= 1) studentConfs.add(" ("); else studentConfs.add(",");
	    	studentConfs.add(dispNumber(studentConflictsHard).setFormattedValue("h" + (studentConflictsHard > 0 ? "+" : "") + studentConflictsHard));
	    }
	    if (studentConfs.getNrChunks() > 1) studentConfs.add(")");
	    suggestion.setStudentConflictSummary(studentConfs);
	    
		return suggestion;
	}
	
	protected static void fillDescriptions(Assignment<Lecture, Placement> assignment, Placement p, Map<Long, String> descriptions) {
		for (Constraint constraint: p.variable().hardConstraints()) {
            Set<Placement> conflicts = new HashSet<Placement>();
            constraint.computeConflicts(assignment, p, conflicts);
            for (Placement conflict: conflicts) {
        		if (!descriptions.containsKey(conflict.variable().getClassId())) {
    				descriptions.put(conflict.variable().getClassId(), TimetableSolver.getConstraintName(constraint));
        		}
            }
        }
	}
	
	public static Suggestion computeSuggestion(SuggestionsContext context, TimetableSolver solver, List<SelectedAssignment> assignments, Placement placement) {
		Suggestion ret = null;
        Solution<Lecture, Placement> solution = solver.currentSolution();
    	TimetableModel model = (TimetableModel)solution.getModel();
    	Assignment<Lecture, Placement> assignment = solution.getAssignment();
    	
        List<Lecture> unAssignedVariables = new ArrayList<Lecture>(assignment.unassignedVariables(model));
        Map<Lecture, Placement> initialAssignments = new HashMap<Lecture, Placement>();
        for (Lecture lec: assignment.assignedVariables())
            initialAssignments.put(lec, assignment.getValue(lec));

        Map<Lecture, Placement> conflictsToResolve = new HashMap<Lecture, Placement>();
        List<Long> resolvedLectures = new ArrayList<Long>();
        Map<Lecture, Placement> unresolvedLectures = new HashMap<Lecture, Placement>();
        List<Placement> hints = new ArrayList<Placement>();
        Map<Long, String> descriptions = new HashMap<Long, String>();
        if (assignments != null) {
        	for (SelectedAssignment a: assignments) {
        		Placement plac = getPlacement(model, a, false);
        		if (plac == null) continue;
        		if (!plac.isValid()) {
        			String reason = TimetableSolver.getNotValidReason(plac, assignment, solver.getProperties().getPropertyBoolean("General.UseAmPm", true));
        			throw new GwtRpcException(reason == null ? MSG.reasonNotKnown() : reason);
        		}
        		Lecture lect = (Lecture)plac.variable();
                if (placement != null && placement.variable().equals(lect)) continue;
                hints.add(plac);
                fillDescriptions(assignment, plac, descriptions);
                Set<Placement> conflicts = model.conflictValues(assignment, plac);
                for (Placement conflictPlacement: conflicts) {
                    conflictsToResolve.put(conflictPlacement.variable(),conflictPlacement);
                    assignment.unassign(0, conflictPlacement.variable());
                }
                if (!conflicts.contains(plac)) {
                    resolvedLectures.add(lect.getClassId());
                	conflictsToResolve.remove(lect);
                	assignment.assign(0,plac);
                } else {
                	unresolvedLectures.put(plac.variable(), plac);
                }
        	}
        }
        if (placement != null) {
        	fillDescriptions(assignment, placement, descriptions);
            Lecture lect = (Lecture)placement.variable();
            Set conflicts = model.conflictValues(assignment, placement);
            for (Iterator i=conflicts.iterator();i.hasNext();) {
                Placement conflictPlacement = (Placement)i.next();
                conflictsToResolve.put(conflictPlacement.variable(),conflictPlacement);
                assignment.unassign(0, conflictPlacement.variable());
            }
            if (!conflicts.contains(placement)) {
                resolvedLectures.add(lect.getClassId());
                conflictsToResolve.remove(lect);
                assignment.assign(0,placement);
            } else {
            	unresolvedLectures.put(placement.variable(), placement);
            }
        }
        ret = createSuggestion(context, solver, initialAssignments, null, conflictsToResolve.values(), unresolvedLectures);
        ret.setCanAssign(unresolvedLectures.isEmpty());
        if (placement!=null) ret.setPlacement(createClassAssignmentDetails(context, solver, placement.variable(), placement, null));
        
    	for (Placement plac: hints) {
    		Lecture lect = plac.variable();
    		if (assignment.getValue(lect) != null) assignment.unassign(0, lect);
    	}
        for (Lecture lect: unAssignedVariables) {
        	if (assignment.getValue(lect) != null) assignment.unassign(0, lect);
        }
        if (placement != null) assignment.unassign(0, placement.variable());
        for (Placement plac: initialAssignments.values()) {
        	Lecture lect = plac.variable();
            if (!plac.equals(assignment.getValue(lect))) assignment.assign(0, plac);
        }
        
        if (ret.hasDifferentAssignments())
        	for (ClassAssignmentDetails d: ret.getDifferentAssignments())
        		d.setConflict(descriptions.get(d.getClazz().getClassId()));
        if (ret.hasUnresolvedConflicts())
        	for (ClassAssignmentDetails d: ret.getUnresolvedConflicts())
        		d.setConflict(descriptions.get(d.getClazz().getClassId()));
        
        Map<String, String> translations = context.courseObjectives();
        for (Criterion<Lecture, Placement> c: model.getCriteria()) {
        	if (c instanceof StudentOverlapConflict) continue;
        	if (c instanceof DeltaTimePreference) continue;
        	String translatedName = (translations == null || translations.isEmpty() ? null : translations.get(c.getName()));
        	if (translatedName != null)
        		ret.setBaseCriterion(translatedName, c.getValue(assignment));
        	else
        		ret.setBaseCriterion(c.getName(), c.getValue(assignment));
        }
        ret.setBaseValue(model.getTotalValue(assignment));
        ret.setBaseUnassignedVariables(model.nrUnassignedVariables(assignment));
        
        return ret;
	}
	
	public static class ClassAssignmentDetailsComparator implements Comparator<ClassAssignmentDetails> {
    	private List<Long> iOrder; 
        public ClassAssignmentDetailsComparator(List<Long> order) { iOrder = order; }
        public int compare(ClassAssignmentDetails d1, ClassAssignmentDetails d2) {
            int i1 = iOrder.indexOf(d1.getClazz().getClassId());
            int i2 = iOrder.indexOf(d2.getClazz().getClassId());
            return (new Integer(i1)).compareTo(new Integer(i2));
        }
    }
	
	public static TableCellInterface dispNumber(long value) {
		TableCellInterface cell = new TableCellInterface<Long>(value).setColor(value < 0 ? "green" : value > 0 ? "red" : null);
		if (value > 0) cell.setFormattedValue("+" + value);
		return cell;
	}
}