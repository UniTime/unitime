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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.criteria.StudentOverlapConflict;
import org.cpsolver.coursett.criteria.placement.DeltaTimePreference;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.criteria.Criterion;
import org.cpsolver.ifs.solution.Solution;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.CPSolverMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ComputeSuggestionsRequest;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.Suggestion;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.Suggestions;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableSolver;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ComputeSuggestionsRequest.class)
public class ComputeSuggestionsBackend implements GwtRpcImplementation<ComputeSuggestionsRequest, Suggestions> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static CPSolverMessages MSG = Localization.create(CPSolverMessages.class);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public Suggestions execute(ComputeSuggestionsRequest request, SessionContext context) {
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
		
		Suggestions response = solver.computeSuggestions(cx, request);
		
		return response;
	}
	
	public static Suggestions computeSuggestions(SuggestionsContext context, TimetableSolver solver, ComputeSuggestionsRequest request) {
		Suggestions suggestions = new Suggestions(request);
		Solution<Lecture, Placement> solution = solver.currentSolution();
    	TimetableModel model = (TimetableModel)solution.getModel();
    	Assignment<Lecture, Placement> assignment = solution.getAssignment();
    	
    	List<Lecture> unAssignedVariables = new ArrayList<Lecture>();
        Map<Lecture, Placement> initialAssignments = new HashMap<Lecture, Placement>();
        for (Lecture lec: assignment.assignedVariables())
            initialAssignments.put(lec, assignment.getValue(lec));
        
        Map<Lecture, Placement> conflictsToResolve = new HashMap<Lecture, Placement>();
        List<Long> resolvedLectures = new ArrayList<Long>();
        List<Placement> hints = new ArrayList<Placement>();
        Map<Long, String> descriptions = new HashMap<Long, String>();
        boolean canAssign = true;
        if (request.hasAssignments()) {
        	for (SelectedAssignment a: request.getAssignments()) {
        		Placement plac = SelectedAssignmentBackend.getPlacement(model, a, false);
        		if (plac == null) continue;
        		if (plac.variable().getClassId().equals(request.getClassId()) && (request.isPlacements() || plac.equals(assignment.getValue(plac.variable())))) continue;
        		if (!plac.isValid()) {
        			String reason = TimetableSolver.getNotValidReason(plac, assignment, solver.getProperties().getPropertyBoolean("General.UseAmPm", true));
        			throw new GwtRpcException(reason == null ? MSG.reasonNotKnown() : reason);
        		}
        		Lecture lect = (Lecture)plac.variable();
                hints.add(plac);
                SelectedAssignmentBackend.fillDescriptions(assignment, plac, descriptions);
                Set conflicts = model.conflictValues(assignment, plac);
                for (Iterator i=conflicts.iterator();i.hasNext();) {
                    Placement conflictPlacement = (Placement)i.next();
                    conflictsToResolve.put(conflictPlacement.variable(),conflictPlacement);
                    assignment.unassign(0, conflictPlacement.variable());
                }
                if (!conflicts.contains(plac)) {
                    resolvedLectures.add(lect.getClassId());
                	conflictsToResolve.remove(lect);
                	assignment.assign(0,plac);
                } else {
                	canAssign = false;
                }
        	}
        }
        
        Lecture lecture = null;
		for (Lecture l: model.variables())
    		if (l.getClassId().equals(request.getClassId()))
    			lecture = l;
		if (lecture == null)
			for (Lecture l: model.constantVariables())
	    		if (l.getClassId().equals(request.getClassId()))
	    			lecture = l;				
		
        if (canAssign) {
        	if (request.isPlacements()) {
        		resolvedLectures.remove(request.getClassId());
        		placements(context, solver, suggestions, new Query(request.getFilter()), System.currentTimeMillis(), lecture, resolvedLectures, conflictsToResolve, initialAssignments);
        	} else {
            	List<Lecture> initialLectures = null; 
                if (!resolvedLectures.contains(request.getClassId())) {
                	initialLectures =  new ArrayList<Lecture>(1); initialLectures.add(lecture);
                }
            	backtrack(context, solver, suggestions, new Query(request.getFilter()), System.currentTimeMillis(), initialLectures, resolvedLectures, conflictsToResolve, initialAssignments, request.getDepth());
        	}
        }
        
        for (Placement plac: hints) {
    		Lecture lect = plac.variable();
    		if (assignment.getValue(lect) != null) assignment.unassign(0, lect);
    	}
        for (Lecture lect: unAssignedVariables) {
        	if (assignment.getValue(lect) != null) assignment.unassign(0, lect);
        }
        for (Placement plac: initialAssignments.values()) {
        	Lecture lect = plac.variable();
            if (!plac.equals(assignment.getValue(lect))) assignment.assign(0, plac);
        }
        
        for (Criterion<Lecture, Placement> c: model.getCriteria()) {
        	if (c instanceof StudentOverlapConflict) continue;
        	if (c instanceof DeltaTimePreference) continue;
        	String name = c.getName();
        	double value = c.getValue(assignment);
        	for (Suggestion suggestion: suggestions.getSuggestions())
        		suggestion.setBaseCriterion(name, value);
        }
        double total = model.getTotalValue(assignment);
        int unassigned = model.nrUnassignedVariables(assignment);
        for (Suggestion suggestion: suggestions.getSuggestions()) {
        	suggestion.setBaseValue(total);
        	suggestion.setBaseUnassignedVariables(unassigned);
        }
		
		return suggestions;
	}
	
	protected static boolean isBetter(Suggestion suggestion, TimetableSolver solver) {
		return suggestion.getValue() < solver.currentSolution().getModel().getTotalValue(solver.currentSolution().getAssignment());
	}
	
	private static void backtrack(SuggestionsContext context, TimetableSolver solver, Suggestions suggestions, Query query, long startTime, List<Lecture> initialLectures, List<Long> resolvedLectures, Map<Lecture, Placement> conflictsToResolve, Map<Lecture, Placement> initialAssignments, int depth) {
		suggestions.setNrCombinationsConsidered(1 + suggestions.getNrCombinationsConsidered());
        int nrUnassigned = conflictsToResolve.size();
        if ((initialLectures==null || initialLectures.isEmpty()) && nrUnassigned==0) {
        	if (suggestions.size() == suggestions.getLimit()) {
        		if (isBetter(suggestions.last(), solver)) return;
        	}
        	suggestions.addSuggestion(SelectedAssignmentBackend.createSuggestion(context, solver, initialAssignments, resolvedLectures, conflictsToResolve.values()));
            return;
        }
        if (depth <= 0) return;
        if (suggestions.getTimeLimit() > 0 && System.currentTimeMillis() - startTime > suggestions.getTimeLimit()) {
        	suggestions.setTimeoutReached(true);
            return;
        }
        if (suggestions.size() == suggestions.getLimit() && suggestions.last().getValue() < getBound(query, suggestions, solver, conflictsToResolve)) {
        	return;
        }
        Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
        TimetableModel model = (TimetableModel)solver.currentSolution().getModel();
        for (Lecture lecture: new ArrayList<Lecture>((initialLectures != null && !initialAssignments.isEmpty() ? initialLectures : conflictsToResolve.keySet()))) {
            if (suggestions.isTimeoutReached()) break;
            if (resolvedLectures.contains(lecture.getClassId())) continue;
            resolvedLectures.add(lecture.getClassId());
            for (PlacementValue placementValue: values(query, suggestions, solver,  lecture)) {
            	if (suggestions.isTimeoutReached()) break;
                Placement placement = placementValue.getPlacement();
                Placement current = assignment.getValue(lecture);
                if (placement.equals(current)) continue;
                if (!suggestions.isAllowBreakHard() && placement.isHard(assignment)) continue;
                if (suggestions.isSameTime() && current!=null && !placement.getTimeLocation().equals(((Placement)current).getTimeLocation())) continue;
                if (suggestions.isSameRoom() && current!=null && !placement.sameRooms((Placement)current)) continue;
                if (suggestions.isSameTime() && current==null) {
                    Placement ini = (Placement)initialAssignments.get(lecture);
                    if (ini!=null && !placement.sameTime(ini)) continue;
                }
                if (suggestions.isSameRoom() && current==null) {
                    Placement ini = (Placement)initialAssignments.get(lecture);
                    if (ini!=null && !placement.sameRooms(ini)) continue;
                }
                Set<Placement> conflicts = model.conflictValues(assignment, placement);
                if (conflicts!=null && (nrUnassigned + conflicts.size()>depth)) continue;
                if (containsCommited(model, conflicts)) continue;
                if (conflicts.contains(placement)) continue;
                boolean containException = false;
                if (conflicts!=null) {
                    for (Iterator<Placement> i=conflicts.iterator();!containException && i.hasNext();) {
                        Placement c = i.next();
                        if (resolvedLectures.contains(c.variable().getClassId())) containException = true;
                    }
                }
                if (containException) continue;
                if (conflicts!=null) {
                    for (Iterator<Placement> i=conflicts.iterator();!containException && i.hasNext();) {
                        Placement c = i.next();
                        assignment.unassign(0, c.variable());
                    }
                }
                assignment.assign(0, placement);
                for (Iterator<Placement> i=conflicts.iterator();!containException && i.hasNext();) {
                    Placement c = i.next();
                    conflictsToResolve.put(c.variable(),c);
                }
                Placement resolvedConf = (Placement)conflictsToResolve.remove(lecture);
                backtrack(context, solver, suggestions, query, startTime, null, resolvedLectures, conflictsToResolve, initialAssignments, depth-1);
                if (current==null)
                	assignment.unassign(0, lecture);
                else
                	assignment.assign(0, current);
                if (conflicts != null) {
                    for (Placement p: conflicts) {
                        assignment.assign(0, p);
                        conflictsToResolve.remove(p.variable());
                    }
                }
                if (resolvedConf != null)
                    conflictsToResolve.put(lecture, resolvedConf);
            }
            resolvedLectures.remove(lecture.getClassId());
        }
    }
	
	private static void placements(SuggestionsContext context, TimetableSolver solver, Suggestions suggestions, Query query, long startTime, Lecture lecture, List<Long> resolvedLectures, Map<Lecture, Placement> conflictsToResolve, Map<Lecture, Placement> initialAssignments) {
		int nrUnassigned = conflictsToResolve.size();
		if (conflictsToResolve.containsKey(lecture)) nrUnassigned--;
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
        TimetableModel model = (TimetableModel)solver.currentSolution().getModel();
        for (PlacementValue placementValue: values(query, suggestions, solver,  lecture)) {
        	if (suggestions.isTimeoutReached()) break;
        	suggestions.setNrCombinationsConsidered(1 + suggestions.getNrCombinationsConsidered());
            Placement placement = placementValue.getPlacement();
            Placement current = initialAssignments.get(lecture);
            if (placement.equals(current)) continue;
            if (!suggestions.isAllowBreakHard() && placement.isHard(assignment)) continue;
            if (suggestions.isSameTime() && current!=null && !placement.getTimeLocation().equals(((Placement)current).getTimeLocation())) continue;
            if (suggestions.isSameRoom() && current!=null && !placement.sameRooms((Placement)current)) continue;
            if (suggestions.isSameTime() && current==null) {
                Placement ini = (Placement)initialAssignments.get(lecture);
                if (ini!=null && !placement.sameTime(ini)) continue;
            }
            if (suggestions.isSameRoom() && current==null) {
                Placement ini = (Placement)initialAssignments.get(lecture);
                if (ini!=null && !placement.sameRooms(ini)) continue;
            }
            Set<Placement> conflicts = model.conflictValues(assignment, placement);
            if (conflicts!=null && (nrUnassigned + conflicts.size() > suggestions.getDepth())) continue;
            if (containsCommited(model, conflicts)) continue;
            if (conflicts.contains(placement)) continue;
            boolean containException = false;
            if (conflicts!=null) {
                for (Iterator<Placement> i=conflicts.iterator();!containException && i.hasNext();) {
                    Placement c = i.next();
                    if (resolvedLectures.contains(c.variable().getClassId())) containException = true;
                }
            }
            if (containException) continue;
            if (conflicts!=null) {
                for (Iterator<Placement> i=conflicts.iterator();!containException && i.hasNext();) {
                    Placement c = i.next();
                    assignment.unassign(0, c.variable());
                }
            }
            assignment.assign(0, placement);
            for (Iterator<Placement> i=conflicts.iterator();!containException && i.hasNext();) {
                Placement c = i.next();
                conflictsToResolve.put(c.variable(),c);
            }
            Placement resolvedConf = (Placement)conflictsToResolve.remove(lecture);
            
            if (suggestions.size() < suggestions.getLimit() || !isBetter(suggestions.last(), solver)) {
            	Suggestion suggestion = SelectedAssignmentBackend.createSuggestion(context, solver, initialAssignments, resolvedLectures, conflictsToResolve.values());
            	if (!suggestion.hasDifferentAssignments())
            		suggestion.addDifferentAssignment(SelectedAssignmentBackend.createClassAssignmentDetails(context, solver, lecture, current, placement));
            	suggestions.addSuggestion(suggestion);
            }
            if (suggestions.getTimeLimit() > 0 && System.currentTimeMillis() - startTime > suggestions.getTimeLimit())
            	suggestions.setTimeoutReached(true);
            
            if (current==null)
            	assignment.unassign(0, lecture);
            else
            	assignment.assign(0, current);
            if (conflicts != null) {
                for (Placement p: conflicts) {
                    assignment.assign(0, p);
                    conflictsToResolve.remove(p.variable());
                }
            }
            if (resolvedConf != null)
                conflictsToResolve.put(lecture, resolvedConf);
        }
    }
	
	protected static double getBound(Query query, Suggestions suggestions, TimetableSolver solver, Map<Lecture, Placement> conflictsToResolve) {
    	double value = solver.currentSolution().getModel().getTotalValue(solver.currentSolution().getAssignment());
    	for (Lecture lect: conflictsToResolve.keySet()) {
    		TreeSet<PlacementValue> values = values(query, suggestions, solver, lect);
    		if (!values.isEmpty()) {
    			PlacementValue val = values.first();
    			value += val.getValue();
    		}
    	}
    	return value;
    }
	
	private static enum Size {
		eq, lt, gt, le, ge
	};
	
	public static class PlacementMatcher implements TermMatcher {
		private Placement iPlacement = null;
		
		public PlacementMatcher(Placement placement) {
			iPlacement = placement;
		}

		@Override
		public boolean match(String attr, String term) {
			if ("time".equals(attr)) {
				return term.equalsIgnoreCase(SuggestionsFilterBackend.getDaysName(iPlacement.getTimeLocation().getDayCode()) + " " + iPlacement.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm()));
			} else if ("date".equals(attr)) {
				return term.equalsIgnoreCase(iPlacement.getTimeLocation().getDatePatternName());
			} else if ("room".equals(attr)) {
				if (iPlacement.getNrRooms() == 0) return false;
				else if (iPlacement.getNrRooms() == 1)
					return term.equalsIgnoreCase(iPlacement.getRoomLocation().getName());
				else
					for (RoomLocation r: iPlacement.getRoomLocations())
						if (term.equalsIgnoreCase(r.getName())) return true;
				return false;
			} else if ("day".equals(attr)) {
				for (int i = 0; i < Constants.DAY_CODES.length; i++)
					if (term.equalsIgnoreCase(Constants.DAY_NAMES_FULL[i]) || term.equalsIgnoreCase(Constants.DAY_NAME[i]) || term.equalsIgnoreCase(Constants.DAY_NAMES_SHORT[i]))
						return (iPlacement.getTimeLocation().getDayCode() & Constants.DAY_CODES[i]) != 0;
				return false;
			} else if ("after".equals(attr)) {
				Integer after = TimeSelector.TimeUtils.parseTime2(CONSTANTS, term, null);
				return after != null && after <= iPlacement.getTimeLocation().getStartSlot();
			} else if ("before".equals(attr)) {
				Integer before = TimeSelector.TimeUtils.parseTime2(CONSTANTS, term, null);
				return before != null && before >= iPlacement.getTimeLocation().getStartSlot() + iPlacement.getTimeLocation().getLength();
			} else if ("size".equals(attr)) {
				int min = 0, max = Integer.MAX_VALUE;
				Size prefix = Size.eq;
				String number = term;
				if (number.startsWith("<=")) { prefix = Size.le; number = number.substring(2); }
				else if (number.startsWith(">=")) { prefix = Size.ge; number = number.substring(2); }
				else if (number.startsWith("<")) { prefix = Size.lt; number = number.substring(1); }
				else if (number.startsWith(">")) { prefix = Size.gt; number = number.substring(1); }
				else if (number.startsWith("=")) { prefix = Size.eq; number = number.substring(1); }
				try {
					int a = Integer.parseInt(number);
					switch (prefix) {
						case eq: min = max = a; break; // = a
						case le: max = a; break; // <= a
						case ge: min = a; break; // >= a
						case lt: max = a - 1; break; // < a
						case gt: min = a + 1; break; // > a
					}
				} catch (NumberFormatException e) {}
				if (term.contains("..")) {
					try {
						String a = term.substring(0, term.indexOf('.'));
						String b = term.substring(term.indexOf("..") + 2);
						min = Integer.parseInt(a); max = Integer.parseInt(b);
					} catch (NumberFormatException e) {}
				}
				if (iPlacement.getNrRooms() == 0) return false;
				else if (iPlacement.getNrRooms() == 1)
					return min <= iPlacement.getRoomLocation().getRoomSize() && iPlacement.getRoomLocation().getRoomSize() <= max;
				else
					for (RoomLocation r: iPlacement.getRoomLocations())
						if (min <= r.getRoomSize() && r.getRoomSize() <= max) return true;
				return false;
			} else if ("flag".equals(attr) || "depth".equals(attr) || "timeout".equals(attr) || "results".equals(attr) || "mode".equals(attr)) {
				return true;
			} else if (attr == null || attr.isEmpty()) {
				return iPlacement.getName(CONSTANTS.useAmPm()).toUpperCase().indexOf(term) >= 0;
			} else {
				return iPlacement.getName(CONSTANTS.useAmPm()).toUpperCase().indexOf(attr + ":" + term) >= 0;
			}
		}
		
	}
	
	protected static boolean match(Query query, Suggestions suggestions, Placement placement) {
		if (query != null)
			return query.match(new PlacementMatcher(placement));
		return true;
	}
	
	protected static TreeSet<PlacementValue> values(Query query, Suggestions suggestions, TimetableSolver solver, Lecture lecture) {
    	TreeSet<PlacementValue> vals = new TreeSet();
    	Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
    	if (lecture.getClassId().equals(suggestions.getClassId())) {
    		for (Placement p: (lecture.allowBreakHard() || !suggestions.isAllowBreakHard() ? lecture.values(assignment) : lecture.computeValues(assignment, true))) {
    			if (match(query, suggestions, p)) vals.add(new PlacementValue(assignment, p));
    		}
    	} else {
    		if (lecture.allowBreakHard() || !suggestions.isAllowBreakHard()) {
    			for (Placement x: lecture.values(assignment)) {
    				vals.add(new PlacementValue(assignment, x));
    			}
    		} else {
    			for (Placement x: lecture.computeValues(assignment, true)) {
    				vals.add(new PlacementValue(assignment, x));
    			}
    		}
    	}
    	return vals;
    }
    
	protected static boolean containsCommited(TimetableModel model, Collection values) {
    	if (model.hasConstantVariables()) {
        	for (Iterator i=values.iterator();i.hasNext();) {
        		Placement placement = (Placement)i.next();
        		Lecture lecture = (Lecture)placement.variable();
        		if (lecture.isCommitted()) return true;
        	}
    	}
    	return false;
    }
    
    public static class PlacementValue implements Comparable<PlacementValue> {
    	private Placement iPlacement;
    	private double iValue;
    	public PlacementValue(Assignment<Lecture, Placement> assignment, Placement placement) {
    		iPlacement = placement;
    		iValue = placement.toDouble(assignment);
    	}
    	public Placement getPlacement() { return iPlacement; }
    	public double getValue() { return iValue; }
    	public int compareTo(PlacementValue p) {
    		int cmp = Double.compare(getValue(), p.getValue());
    		if (cmp!=0) return cmp;
    		return Double.compare(getPlacement().getId(), p.getPlacement().getId());
    	}
    }
}
