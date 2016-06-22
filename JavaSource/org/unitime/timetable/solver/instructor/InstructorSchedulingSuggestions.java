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
package org.unitime.timetable.solver.instructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.criteria.Criterion;
import org.cpsolver.instructor.model.InstructorSchedulingModel;
import org.cpsolver.instructor.model.TeachingAssignment;
import org.cpsolver.instructor.model.TeachingRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SuggestionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;

/**
 * @author Tomas Muller
 */
public class InstructorSchedulingSuggestions {
	private InstructorSchedulingSolver iSolver;
	private InstructorSchedulingModel iModel;
	private Assignment<TeachingRequest.Variable, TeachingAssignment> iAssignment;
	private List<TeachingRequest.Variable> iInitialUnassignments = new ArrayList<TeachingRequest.Variable>();
	private Map<TeachingRequest.Variable, TeachingAssignment> iInitialAssignments = new HashMap<TeachingRequest.Variable, TeachingAssignment>();
    private double iValue;
    private Map<String, Double> iValues = new HashMap<String, Double>();
	
    private int iDepth = 2;
    private int iLimit = 20;
    private int iNrSolutions = 0, iNrCombinationsConsidered = 0;
    private long iTimeOut = 5000;
    private long iStartTime = 0;
    private boolean iTimeoutReached = false;
    private String iFilter = null;
    
    private TeachingRequest.Variable iRequest = null;
    private TreeSet<SuggestionInfo> iSuggestions = null;
    private List<TeachingRequest.Variable> iResolvedRequests = null;
    private Map<TeachingRequest.Variable, TeachingAssignment> iConflictsToResolve = null;

	
	public InstructorSchedulingSuggestions(InstructorSchedulingSolver solver) {
		iSolver = solver;
		iModel = (InstructorSchedulingModel)solver.currentSolution().getModel();
		iAssignment = solver.currentSolution().getAssignment();
		for (TeachingRequest.Variable variable: iModel.variables()) {
			TeachingAssignment value = iAssignment.getValue(variable);
			if (value == null) {
				iInitialUnassignments.add(variable);
			} else {
				iInitialAssignments.put(variable, value);
			}
		}
		iValue = iModel.getTotalValue(iAssignment);
		for (Criterion<TeachingRequest.Variable, TeachingAssignment> c: iModel.getCriteria())
			iValues.put(c.getName(), c.getValue(iAssignment));
	}
	
    public int getDepth() { return iDepth; }
    public void setDepth(int depth) { iDepth = depth; }
    public int getLimit() { return iLimit; }
    public void setLimit(int limit) { iLimit = limit; }
    public long getTimeOut() { return iTimeOut; }
    public void setTimeOut(long timeOut) { iTimeOut = timeOut; }
    public String getFilter() { return iFilter; }
    public void setFilter(String filter) { iFilter = filter; }
    
    public int getNrSolutions() { return iNrSolutions; }
    public int getNrCombinationsConsidered() { return iNrCombinationsConsidered; }
    public boolean wasTimeoutReached() { return iTimeoutReached; }
    
    public boolean match(String name) {
        if (iFilter == null || iFilter.trim().isEmpty()) return true;
        String n = name.toUpperCase();
        StringTokenizer stk1 = new StringTokenizer(iFilter.toUpperCase(),";");
        while (stk1.hasMoreTokens()) {
            StringTokenizer stk2 = new StringTokenizer(stk1.nextToken()," ,");
            boolean match = true;
            while (match && stk2.hasMoreTokens()) {
                String token = stk2.nextToken().trim();
                if (token.length()==0) continue;
                if (token.indexOf('*')>=0 || token.indexOf('?')>=0) {
                    try {
                        String tokenRegExp = "\\s+"+token.replaceAll("\\.", "\\.").replaceAll("\\?", ".+").replaceAll("\\*", ".*")+"\\s";
                        if (!Pattern.compile(tokenRegExp).matcher(" "+n+" ").find()) match = false;
                    } catch (PatternSyntaxException e) { match = false; }
                } else if (n.indexOf(token)<0) match = false;
            }
            if (match) return true;
        }
        return false;
    }
    
    public synchronized TreeSet<SuggestionInfo> computeSuggestions(SuggestionInfo suggestion) {
        iSuggestions = new TreeSet<SuggestionInfo>();
        
        iResolvedRequests = new ArrayList<TeachingRequest.Variable>();
        iConflictsToResolve = new HashMap<TeachingRequest.Variable, TeachingAssignment>();
        iNrSolutions = 0;
        iNrCombinationsConsidered = 0;
        iTimeoutReached = false;
        iRequest = null;
        
    	List<TeachingAssignment> givenAssignments = new ArrayList<TeachingAssignment>();
    	for (AssignmentInfo assignment: suggestion.getAssignments()) {
    		for (TeachingRequest request: iModel.getRequests()) {
    			if (request.getRequestId() == assignment.getRequest().getRequestId()) {
    				TeachingRequest.Variable var = request.getVariable(assignment.getIndex());
    				if (iRequest == null) {
    					iRequest = var;
    				} else {
    					if (assignment.getInstructor() != null) {
    						for (TeachingAssignment val: var.values(iAssignment)) {
    							if (val.getInstructor().getInstructorId() == assignment.getInstructor().getInstructorId()) {
    								givenAssignments.add(val);
    								break;
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    	
    	for (TeachingAssignment assignment: givenAssignments) {
    		iAssignment.unassign(0l, assignment.variable());
    	}
    	for (TeachingAssignment assignment: givenAssignments) {
    		for (TeachingAssignment conflict: iModel.conflictValues(iAssignment, assignment)) {
    			iConflictsToResolve.put(conflict.variable(), conflict);
    			iAssignment.unassign(0, conflict.variable());
    		}
    		iResolvedRequests.add(assignment.variable());
    		iAssignment.assign(0, assignment);
    	}
    	
    	iStartTime = System.currentTimeMillis();
        backtrack(iDepth);
        
        for (TeachingRequest.Variable x : iInitialUnassignments)
            if (iAssignment.getValue(x) != null) iAssignment.unassign(0, x);
        for (Map.Entry<TeachingRequest.Variable, TeachingAssignment> x : iInitialAssignments.entrySet())
            if (!x.getValue().equals(iAssignment.getValue(x.getKey()))) iAssignment.assign(0, x.getValue());
        
        return iSuggestions;
    }
    
    protected SuggestionInfo createSuggestion(double value) {
    	SuggestionInfo suggestion = new SuggestionInfo();
    	suggestion.setValue(value);
    	suggestion.setId(new Long(iNrSolutions ++));
    	for (Criterion<TeachingRequest.Variable, TeachingAssignment> c: iModel.getCriteria()) {
    		double v = c.getValue(iAssignment);
    		Double base = iValues.get(c.getName());
    		suggestion.addValue(c.getName(), base == null ? v : v - base);
    	}
    	for (TeachingRequest.Variable var: iResolvedRequests) {
    		TeachingRequestInfo req = iSolver.toRequestInfo(var.getRequest());
    		TeachingAssignment initial = iInitialAssignments.get(var);
    		if (initial != null)
    			req.addInstructor(iSolver.toInstructorInfo(initial.getInstructor()));
    		TeachingAssignment current = iAssignment.getValue(var);
    		AssignmentInfo info = new AssignmentInfo();
    		info.setRequest(req);
    		info.setIndex(var.getInstructorIndex());
    		if (current != null)
    			info.setInstructor(iSolver.toInstructorInfo(current));
    		suggestion.addAssignment(info);
    	}
    	for (TeachingRequest.Variable var: iConflictsToResolve.keySet()) {
    		TeachingRequestInfo req = iSolver.toRequestInfo(var.getRequest());
    		TeachingAssignment initial = iInitialAssignments.get(var);
    		if (initial != null)
    			req.addInstructor(iSolver.toInstructorInfo(initial.getInstructor()));
    		AssignmentInfo info = new AssignmentInfo();
    		info.setRequest(req);
    		info.setIndex(var.getInstructorIndex());
    		suggestion.addAssignment(info);
    	}
    	for (TeachingRequest.Variable var: iModel.assignedVariables(iAssignment)) {
    		if (iResolvedRequests.contains(var)) continue;
    		TeachingRequestInfo req = iSolver.toRequestInfo(var.getRequest());
    		TeachingAssignment initial = iInitialAssignments.get(var);
    		if (initial != null)
    			req.addInstructor(iSolver.toInstructorInfo(initial.getInstructor()));
    		TeachingAssignment current = iAssignment.getValue(var);
    		if (current.equals(initial)) continue;
    		AssignmentInfo info = new AssignmentInfo();
    		info.setRequest(req);
    		info.setIndex(var.getInstructorIndex());
    		if (current != null)
    			info.setInstructor(iSolver.toInstructorInfo(current));
    		suggestion.addAssignment(info);
    	}
    	return suggestion;
    }
	
    private void backtrack(int depth) {
        if (iDepth > depth && iConflictsToResolve.isEmpty()) {
        	double value = (iModel.getTotalValue(iAssignment) - iValue);
            if (iSuggestions.size() == iLimit && iSuggestions.last().getValue() <= value) return;
            iSuggestions.add(createSuggestion(value));
            if (iSuggestions.size() > iLimit) iSuggestions.remove(iSuggestions.last());
            return;
        }
        if (depth <= 0) return;
        if (iTimeOut > 0 && System.currentTimeMillis() - iStartTime > iTimeOut) {
            iTimeoutReached = true;
            return;
        }
        TeachingRequest.Variable var = (iDepth == depth && !iResolvedRequests.contains(iRequest) ? iRequest : iConflictsToResolve.keySet().iterator().next());
        if (iResolvedRequests.contains(var)) return;
        iResolvedRequests.add(var);
        values: for (TeachingAssignment assignment: var.values(iAssignment)) {
        	if (assignment.equals(iAssignment.getValue(var))) continue;
        	if (var.equals(iRequest) && !match(assignment.getInstructor().getExternalId() + " " + assignment.getInstructor().getName())) continue;
        	Set<TeachingAssignment> conflicts = iModel.conflictValues(iAssignment, assignment);

        	iNrCombinationsConsidered++;
            if (iConflictsToResolve.size() + conflicts.size() > depth) continue;
            for (TeachingAssignment c: conflicts) {
                if (iResolvedRequests.contains(c.variable())) continue values;
            }
            
            TeachingAssignment cur = iAssignment.getValue(var);
            for (TeachingAssignment c: conflicts) {
            	iAssignment.unassign(0, c.variable());
            	iConflictsToResolve.put(c.variable(), c);
            }
            iAssignment.assign(0, assignment);
            TeachingAssignment resolvedConf = iConflictsToResolve.remove(var);
            
            backtrack(depth-1);
            
            if (cur == null)
            	iAssignment.unassign(0, var);
            else
            	iAssignment.assign(0, cur);
            for (TeachingAssignment c: conflicts) {
            	iAssignment.assign(0, c);
                iConflictsToResolve.remove(c.variable());
            }
            if (resolvedConf != null)
                iConflictsToResolve.put(var, resolvedConf);
        }
        iResolvedRequests.remove(var);
    }
}
