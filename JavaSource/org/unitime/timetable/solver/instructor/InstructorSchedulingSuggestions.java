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

import org.cpsolver.coursett.preference.PreferenceCombination;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.criteria.Criterion;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.instructor.model.Instructor;
import org.cpsolver.instructor.model.InstructorSchedulingModel;
import org.cpsolver.instructor.model.TeachingAssignment;
import org.cpsolver.instructor.model.TeachingRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.ComputeSuggestionsRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SuggestionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SuggestionsResponse;
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
    private int iNrSolutions = 0, iNrCombinationsConsidered = 0, iNrDomainValues = 0;;
    private long iTimeOut = 5000;
    private long iStartTime = 0;
    private boolean iTimeoutReached = false;
    private String iFilter = null;
    
    private TeachingRequest.Variable iRequest = null;
    private TreeSet<SuggestionInfo> iSuggestions = null;
    private List<TeachingRequest.Variable> iResolvedRequests = null;
    private Map<TeachingRequest.Variable, TeachingAssignment> iConflictsToResolve = null;
    private Map<TeachingRequest, TeachingRequestInfo> iRequestInfos = null;
    private Instructor iInstructor = null;
	
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
    
    public synchronized SuggestionsResponse computeSuggestions(ComputeSuggestionsRequest request) {
        SuggestionsResponse response = new SuggestionsResponse();
        iSuggestions = new TreeSet<SuggestionInfo>();
        
        iResolvedRequests = new ArrayList<TeachingRequest.Variable>();
        iConflictsToResolve = new HashMap<TeachingRequest.Variable, TeachingAssignment>();
        iRequestInfos = new HashMap<TeachingRequest, TeachingRequestInfo>();
        iNrSolutions = -1;
        iNrCombinationsConsidered = 0;
        iNrDomainValues = 0;
        iTimeoutReached = false;
        iRequest = null;
        iDepth = request.getMaxDept();
        iTimeOut = request.getTimeout();
        iLimit = request.getMaxResults();
        
        if (request.getSelectedInstructorId() != null)
        	for (Instructor instructor: iModel.getInstructors())
        		if (instructor.getInstructorId() == request.getSelectedInstructorId()) { iInstructor = instructor; break; }
        TeachingAssignment requestedAssignment = null;
    	List<TeachingAssignment> givenAssignments = new ArrayList<TeachingAssignment>();
    	if (request.getSelectedRequestId() != null)
        	for (TeachingRequest tr: iModel.getRequests()) {
    			if (tr.getRequestId() == request.getSelectedRequestId()) { iRequest = tr.getVariable(request.getSelectedIndex()); }
        	}
    	for (AssignmentInfo assignment: request.getAssignments()) {
    		for (TeachingRequest tr: iModel.getRequests()) {
    			if (tr.getRequestId() == assignment.getRequest().getRequestId()) {
    				TeachingRequest.Variable var = tr.getVariable(assignment.getIndex());
    				TeachingAssignment original = iAssignment.getValue(var);
    				if (assignment.getInstructor() != null) {
						for (TeachingAssignment val: var.values(iAssignment)) {
							if (val.getInstructor().getInstructorId() == assignment.getInstructor().getInstructorId()) {
								if (var.equals(iRequest))
									requestedAssignment = val;
								else if (original == null || !original.equals(val))
									givenAssignments.add(val);
								break;
							}
						}
					}
    			}
    		}
    	}
    	
    	if (iRequest != null) // requestedAssignment != null
    		iAssignment.unassign(0l, iRequest);
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
    	
    	if (request.isComputeDomain()) {
        	int domainSize = 0;
        	TreeSet<SuggestionInfo> domain = new TreeSet<SuggestionInfo>();
        	if (iInstructor != null) {
        		for (TeachingRequest tr: iModel.getRequests())
        			if (iInstructor.canTeach(tr)) {
        				PreferenceCombination attributePref = tr.getAttributePreference(iInstructor);
                        if (attributePref.isProhibited()) continue;
                        for (TeachingRequest.Variable var: tr.getVariables()) {
                        	if (iResolvedRequests.contains(var)) continue;
                        	domainSize++;
                        	SuggestionInfo suggestion = tryAssignment(new TeachingAssignment(var, iInstructor, attributePref.getPreferenceInt()));
                    		if (suggestion != null && !suggestion.getAssignments().isEmpty()) {
                    			if (request.getMaxDomain() > 0 && domain.size() == request.getMaxDomain()) {
                    				if (suggestion.compareTo(domain.last()) < 0) {
                    					domain.add(suggestion);
                    					domain.remove(domain.last());
                    				}
                    			} else {
                    				domain.add(suggestion);
                    			}
                    		}
        				}
        			}
        	} else if (iInstructor == null) {
            	for (TeachingAssignment assignment: iRequest.values(iAssignment)) {
            		SuggestionInfo suggestion = tryAssignment(assignment);
            		if (suggestion != null && !suggestion.getAssignments().isEmpty()) {
                		domainSize++;
            			if (request.getMaxDomain() > 0 && domain.size() == request.getMaxDomain()) {
            				if (suggestion.compareTo(domain.last()) < 0) {
            					domain.add(suggestion);
            					domain.remove(domain.last());
            				}
            			} else {
            				domain.add(suggestion);
            			}
            		}
        		}
        	}
        	response.setDomainSize(domainSize);
            for (SuggestionInfo info: domain)
            	response.addDomainValue(info);
    	}
    	
    	if (request.isComputeSuggestions()) {
        	if (requestedAssignment != null) {
        		for (TeachingAssignment conflict: iModel.conflictValues(iAssignment, requestedAssignment)) {
        			iConflictsToResolve.put(conflict.variable(), conflict);
        			iAssignment.unassign(0, conflict.variable());
        		}
        		if (!iConflictsToResolve.isEmpty())
        			iResolvedRequests.add(requestedAssignment.variable());
        		iAssignment.assign(0, requestedAssignment);
        	}
        	if (iRequest == null && !iResolvedRequests.isEmpty() && iConflictsToResolve.isEmpty()) {
        		for (TeachingAssignment assignment: givenAssignments)
        			if (assignment.getInstructor().equals(iInstructor)) {
        				iRequest = assignment.variable();
        				iResolvedRequests.remove(iRequest);
        				break;
        			}
        		if (iRequest == null && !iResolvedRequests.isEmpty()) {
        			iRequest = iResolvedRequests.get(0);
        			iResolvedRequests.remove(iRequest);
        		}
        	}    	
        	
        	response.setCurrentAssignment(createSuggestion(iModel.getTotalValue(iAssignment) - iValue));

        	iStartTime = System.currentTimeMillis();
        	if (iInstructor != null && iResolvedRequests.isEmpty())
        		instructorBacktrack();
        	else
        		backtrack(iDepth);
        	
        	for (SuggestionInfo suggestion: iSuggestions)
        		response.addSuggestion(suggestion);
        	response.setTimeoutReached(wasTimeoutReached());
        	response.setNrCombinationsConsidered(getNrCombinationsConsidered());
        	response.setNrSolutions(getNrSolutions());
    	}
        
        for (TeachingRequest.Variable x : iInitialUnassignments)
            if (iAssignment.getValue(x) != null) iAssignment.unassign(0, x);
        List<TeachingAssignment> changes = new ArrayList<TeachingAssignment>();
        for (Map.Entry<TeachingRequest.Variable, TeachingAssignment> x : iInitialAssignments.entrySet()) {
        	TeachingRequest.Variable var = x.getKey();
        	TeachingAssignment current = iAssignment.getValue(var);
        	TeachingAssignment initial = x.getValue();
            if (!initial.equals(current)) {
            	if (current != null) iAssignment.unassign(0, var);
            	changes.add(initial);
            }
        }
        for (TeachingAssignment initial: changes) iAssignment.assign(0, initial);
        
    	for (Map.Entry<TeachingRequest, TeachingRequestInfo> e: iRequestInfos.entrySet()) {
    		TeachingRequestInfo info = e.getValue();
        	for (TeachingRequest.Variable var: e.getKey().getVariables()) {
        		TeachingAssignment initial = iInitialAssignments.get(var);
        		if (initial != null)
        			info.addInstructor(toInstructorInfo(initial));
        	}
    	}

        return response;
    }
    
    protected InstructorInfo toInstructorInfo(TeachingAssignment assignment) {
    	return iSolver.toInstructorInfo(assignment);
    }
    
    protected TeachingRequestInfo toRequestInfo(TeachingRequest request) {
    	TeachingRequestInfo info = iRequestInfos.get(request);
    	if (info == null) {
    		info = iSolver.toRequestInfo(request);
    		iRequestInfos.put(request, info);
    	}
    	return info;
    }
    
    protected AssignmentInfo toAssignmentInfo(TeachingRequest.Variable request) {
    	AssignmentInfo info = new AssignmentInfo();
		info.setRequest(toRequestInfo(request.getRequest()));
		info.setIndex(request.getInstructorIndex());
		TeachingAssignment current = iAssignment.getValue(request);
		if (current != null)
			info.setInstructor(toInstructorInfo(current));
		else {
			TeachingAssignment initial = iInitialAssignments.get(request);
    		if (initial != null) {
    			for (Constraint<TeachingRequest.Variable, TeachingAssignment> c: iModel.constraints())
    				if (c.inConflict(iAssignment, initial)) info.addConflict(c.getName());
    			for (Constraint<TeachingRequest.Variable, TeachingAssignment> c: iModel.globalConstraints())
    				if (c.inConflict(iAssignment, initial)) info.addConflict(c.getName());
    		}
		}
		return info;
    }
    
    protected SuggestionInfo tryAssignment(TeachingAssignment assignment) {
    	TeachingAssignment original = iAssignment.getValue(assignment.variable());
    	Set<TeachingAssignment> conflicts = iModel.conflictValues(iAssignment, assignment);
    	if (conflicts.contains(assignment)) return null;
    	
    	for (TeachingAssignment conflict: conflicts)
    		iAssignment.unassign(0l, conflict.variable());
    	iAssignment.assign(0l, assignment);
    	
    	SuggestionInfo suggestion = new SuggestionInfo();
    	suggestion.setValue(iModel.getTotalValue(iAssignment) - iValue);
    	suggestion.setId(new Long(iNrDomainValues ++));
    	for (Criterion<TeachingRequest.Variable, TeachingAssignment> c: iModel.getCriteria()) {
    		double v = c.getValue(iAssignment);
    		Double base = iValues.get(c.getName());
    		suggestion.addValue(c.getName(), base == null ? v : v - base);
    	}
    	suggestion.addAssignment(toAssignmentInfo(assignment.variable()));
    	for (TeachingRequest.Variable var: iResolvedRequests) {
    		if (var.equals(iRequest) || var.equals(assignment.variable())) {
    			continue;
    		}
    		suggestion.addAssignment(toAssignmentInfo(var));
    	}
    	for (TeachingAssignment conflict: conflicts) {
    		if (conflict.variable().equals(iRequest) || iConflictsToResolve.containsKey(conflict.variable()) || iResolvedRequests.contains(conflict.variable())) {
    			continue;
    		}
    		suggestion.addAssignment(toAssignmentInfo(conflict.variable()));
    	}
    	
    	iAssignment.unassign(0l, assignment.variable());
    	for (TeachingAssignment conflict: conflicts)
    		iAssignment.assign(0l, conflict);
    	if (original != null)
    		iAssignment.assign(0l, original);
    	
    	return suggestion;
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
    	if (iRequest != null) {
    		suggestion.addAssignment(toAssignmentInfo(iRequest));
    	}
    	for (TeachingRequest.Variable var: iResolvedRequests) {
    		if (var.equals(iRequest)) continue;
    		suggestion.addAssignment(toAssignmentInfo(var));
    	}
    	for (TeachingRequest.Variable var: iConflictsToResolve.keySet()) {
    		if (var.equals(iRequest)) continue;
    		suggestion.addAssignment(toAssignmentInfo(var));
    	}
    	for (TeachingRequest.Variable var: iModel.assignedVariables(iAssignment)) {
    		if (iResolvedRequests.contains(var) || var.equals(iRequest) || iAssignment.getValue(var).equals(iInitialAssignments.get(var)) || iConflictsToResolve.containsKey(var)) continue;
    		suggestion.addAssignment(toAssignmentInfo(var));
    	}
    	return suggestion;
    }
    
    private void instructorBacktrack() {
    	for (TeachingRequest tr: iModel.getRequests())
			if (iInstructor.canTeach(tr)) {
				PreferenceCombination attributePref = tr.getAttributePreference(iInstructor);
                if (attributePref.isProhibited()) continue;
                variables: for (TeachingRequest.Variable var: tr.getVariables()) {
                	if (iResolvedRequests.contains(var)) continue;
                	TeachingAssignment assignment = new TeachingAssignment(var, iInstructor, attributePref.getPreferenceInt());
                	
                	if (assignment.equals(iAssignment.getValue(var))) continue;
                	if (var.equals(iRequest) && !match(assignment.getInstructor().getExternalId() + " " + assignment.getInstructor().getName())) continue;
                	
                	Set<TeachingAssignment> conflicts = iModel.conflictValues(iAssignment, assignment);

                	iNrCombinationsConsidered++;
                    if (iConflictsToResolve.size() + conflicts.size() > iDepth) continue;

                    for (TeachingAssignment c: conflicts) {
                        if (iResolvedRequests.contains(c.variable()) && var.equals(c.variable())) {
                        	continue variables;
                        }
                    }
                    
                    TeachingAssignment cur = iAssignment.getValue(var);
                    for (TeachingAssignment c: conflicts) {
                    	iAssignment.unassign(0, c.variable());
                    	iConflictsToResolve.put(c.variable(), c);
                    }
                    iAssignment.assign(0, assignment);
                    TeachingAssignment resolvedConf = iConflictsToResolve.remove(var);
                    
                	iResolvedRequests.add(var);
                    backtrack(iDepth - 1);
                	iResolvedRequests.remove(var);
                    
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
			}
    }
	
    private void backtrack(int depth) {
        if (iDepth > depth && iConflictsToResolve.isEmpty()) {
        	double value = (iModel.getTotalValue(iAssignment) - iValue);
            if (iSuggestions.size() == iLimit && (iSuggestions.last().getValue() < value || (iSuggestions.last().getValue() == value && iSuggestions.last().getAssignments().size() <= iResolvedRequests.size()))) return;
            iSuggestions.add(createSuggestion(value));
            if (iSuggestions.size() > iLimit) iSuggestions.remove(iSuggestions.last());
            return;
        }
        if (depth <= 0) return;
        if (iTimeOut > 0 && System.currentTimeMillis() - iStartTime > iTimeOut) {
            iTimeoutReached = true;
            return;
        }
        TeachingRequest.Variable var = (iDepth == depth && iRequest != null && !iResolvedRequests.contains(iRequest) ? iRequest : iConflictsToResolve.keySet().iterator().next());
        if (iResolvedRequests.contains(var)) return;
        iResolvedRequests.add(var);
        values: for (TeachingAssignment assignment: var.values(iAssignment)) {
        	if (assignment.equals(iInitialAssignments.get(var))) continue;
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
