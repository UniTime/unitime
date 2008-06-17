/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.interactive;


import java.io.Serializable;
import java.util.*;

import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.solver.interactive.Suggestion;

import net.sf.cpsolver.coursett.model.*;
import net.sf.cpsolver.ifs.solver.*;

/**
 * @author Tomas Muller
 */
public class Suggestions implements Serializable {
	private static final long serialVersionUID = 1L;
    private transient Solver iSolver = null;
    private transient TimetableModel iModel = null;
    private transient Lecture iLecture = null;

    private long iTimeOut = 5000; //5s
    private boolean iSameRoom = false;
    private boolean iSameTime = false;
    private boolean iAllTheSame = true;
    private boolean iAllowBreakHard = false;
    private int iDepth = 2;
    private TreeSet iSuggestions = new TreeSet(); 
    private transient Vector iHints = new Vector();
    
    private boolean iTimeoutReached = false;
    private long iNrCombinationsConsidered = 0;
    private long iNrSolutions = 0;
    
    private Suggestion iCurrentSuggestion = null;
    private Suggestion iEmptySuggestion = null;
    private TreeSet iAllAssignments = null;
    private Vector iConfTable = null;
    private int iNrTries;
    private Vector iOriginalHints = null;
    private int iLimit = 100;
    private String iFilterText = null;
    private boolean iTryAllAssignments = true;
    private boolean iComputeSuggestions = true;
    private boolean iComputeConfTable = true;
    
    private int iMinRoomSize = -1;
    private int iMaxRoomSize = -1;
    
    public Suggestions(Solver solver, SuggestionsModel model) {
        iSolver = solver;
        iModel = (TimetableModel)iSolver.currentSolution().getModel();
        iDepth = model.getDepth();
        iTimeOut = model.getTimeout();
        iAllTheSame = model.isAllTheSame();
        iSameTime = (model.getFilter()==SuggestionsModel.sFilterSameTime);
        iSameRoom = (model.getFilter()==SuggestionsModel.sFilterSameRoom);
        iAllowBreakHard = model.getAllowBreakHard();
        iHints = new Vector(model.getHints().size());
        iOriginalHints = model.getHints();
        iTryAllAssignments = model.getDisplayPlacements();
        iComputeSuggestions = model.getDisplaySuggestions();
        iComputeConfTable = model.getDisplayConfTable();
        iLimit = model.getLimit();
        if (iLimit<=0) {
        	iComputeSuggestions=false;
        	iTryAllAssignments=false;
        }
        iMinRoomSize = model.getMinRoomSize();
        iMaxRoomSize = model.getMaxRoomSize();
        iFilterText = (model.getFilterText()==null?null:model.getFilterText().trim().toUpperCase());
        for (Enumeration e=iModel.variables().elements();e.hasMoreElements();) {
        	Lecture lecture = (Lecture)e.nextElement();
        	if (lecture.getClassId().equals(model.getClassId())) {
        		iLecture = lecture;
        		break;
        	}
        }
        for (Enumeration e=model.getHints().elements();e.hasMoreElements();) {
        	Hint h = (Hint)e.nextElement();
        	Placement p = h.getPlacement(iModel);
        	if (p==null) continue;
        	//if (!h.hasInfo()) h.setInfo(iSolver, p);
        	iHints.add(p);
        }
        if (iLecture!=null)
        	compute();
    }
    
    public boolean match(Placement p) {
    	if (iMinRoomSize>=0 && p.getRoomSize()<iMinRoomSize) return false;
    	if (iMaxRoomSize>=0 && p.getRoomSize()>iMaxRoomSize) return false;
    	if (iFilterText==null || iFilterText.length()==0) return true;
    	StringTokenizer stk = new StringTokenizer(iFilterText);
    	while (stk.hasMoreTokens()) {
    		String token = stk.nextToken();
    		if (p.getName().toUpperCase().indexOf(token)<0) return false;
    	}
    	return true;
    }
    
    public Vector getHints() { return iOriginalHints; }
    
    public void compute() {
    	if (iComputeSuggestions)
    		computeSuggestions();
    	if (iTryAllAssignments)
    		computeTryAllAssignments();
    	if (iComputeConfTable)
    		computeConfTable();
        iCurrentSuggestion = tryAssignment((Placement)null);
        Hashtable initialAssignments = new Hashtable();
        for (Enumeration e=iModel.assignedVariables().elements();e.hasMoreElements();) {
        	Lecture lec = (Lecture)e.nextElement();
            initialAssignments.put(lec,lec.getAssignment());
        }
        iEmptySuggestion = new Suggestion(iSolver, initialAssignments, new Vector(), new Vector());
        computeNrTries();
    }
    
    public TreeSet getSuggestions() {
    	return iSuggestions;
    }

    public void computeSuggestions() {
        iSuggestions.clear();
        iTimeoutReached = false;
        iNrCombinationsConsidered = 0; iNrSolutions = 0;
        synchronized (iSolver.currentSolution()) {
            Vector unAssignedVariables = new Vector(iModel.unassignedVariables());
            Hashtable initialAssignments = new Hashtable();
            for (Enumeration e=iModel.assignedVariables().elements();e.hasMoreElements();) {
                Lecture lec = (Lecture)e.nextElement();
                initialAssignments.put(lec,lec.getAssignment());
            }
            Hashtable conflictsToResolve = new Hashtable();
            Vector resolvedLectures = new Vector();
            if (iHints!=null) {
                for (Enumeration e=iHints.elements();e.hasMoreElements();) {
                    Placement plac = (Placement)e.nextElement();
                    Lecture lect = (Lecture)plac.variable();
                    Set conflicts = iModel.conflictValues(plac);
                    for (Iterator i=conflicts.iterator();i.hasNext();) {
                        Placement conflictPlacement = (Placement)i.next();
                        conflictsToResolve.put(conflictPlacement.variable(),conflictPlacement);
                        conflictPlacement.variable().unassign(0);
                    }
                    resolvedLectures.add(lect.getClassId());
                    conflictsToResolve.remove(lect);
                    lect.assign(0,plac);
                }
            }
            Vector initialLectures = new Vector(1); 
            if (!resolvedLectures.contains(iLecture.getClassId())) initialLectures.add(iLecture);
            backtrack(System.currentTimeMillis(), initialLectures, resolvedLectures, conflictsToResolve, initialAssignments, iDepth);
            for (Enumeration e=unAssignedVariables.elements();e.hasMoreElements();) {
                Lecture lect = (Lecture)e.nextElement();
                if (lect.getAssignment()!=null)
                    lect.unassign(0);
            }
            for (Iterator i=initialAssignments.values().iterator();i.hasNext();) {
                Placement plac = (Placement)i.next();
                Lecture lect = (Lecture)plac.variable();
                if (!plac.equals(lect.getAssignment())) lect.assign(0,plac);
            }
        }
    }
    
    public Suggestion tryAssignment(Hint hint) {
    	return tryAssignment(hint.getPlacement(iModel));
    }
    
    public Suggestion tryAssignment(Placement placement) {
        Suggestion ret = null;
        synchronized (iSolver.currentSolution()) {
            Vector unAssignedVariables = new Vector(iModel.unassignedVariables());
            Hashtable initialAssignments = new Hashtable();
            for (Enumeration e=iModel.assignedVariables().elements();e.hasMoreElements();) {
                Lecture lec = (Lecture)e.nextElement();
                initialAssignments.put(lec,lec.getAssignment());
            }
            Hashtable conflictsToResolve = new Hashtable();
            Vector resolvedLectures = new Vector();
            if (iHints!=null) {
                for (Enumeration e=iHints.elements();e.hasMoreElements();) {
                    Placement plac = (Placement)e.nextElement();
                    Lecture lect = (Lecture)plac.variable();
                    if (placement!=null && placement.variable().equals(lect)) continue;
                    Set conflicts = iModel.conflictValues(plac);
                    for (Iterator i=conflicts.iterator();i.hasNext();) {
                        Placement conflictPlacement = (Placement)i.next();
                        conflictsToResolve.put(conflictPlacement.variable(),conflictPlacement);
                        conflictPlacement.variable().unassign(0);
                    }
                    resolvedLectures.add(lect.getClassId());
                    conflictsToResolve.remove(lect);
                    lect.assign(0,plac);
                }
            }
            if (placement!=null) {
                Lecture lect = (Lecture)placement.variable();
                Set conflicts = iModel.conflictValues(placement);
                for (Iterator i=conflicts.iterator();i.hasNext();) {
                    Placement conflictPlacement = (Placement)i.next();
                    conflictsToResolve.put(conflictPlacement.variable(),conflictPlacement);
                    conflictPlacement.variable().unassign(0);
                }
                resolvedLectures.add(lect.getClassId());
                conflictsToResolve.remove(lect);
                lect.assign(0,placement);
            }
            ret = new Suggestion(iSolver, initialAssignments, resolvedLectures, conflictsToResolve.values());
            if (placement!=null) ret.setHint(new Hint(iSolver, placement));
            if (iHints!=null) {
                for (Enumeration e=iHints.elements();e.hasMoreElements();) {
                    Placement plac = (Placement)e.nextElement();
                    Lecture lect = (Lecture)plac.variable();
                    if (lect.getAssignment()!=null) lect.unassign(0);
                }
            }
            for (Enumeration e=unAssignedVariables.elements();e.hasMoreElements();) {
                Lecture lect = (Lecture)e.nextElement();
                if (lect.getAssignment()!=null)
                    lect.unassign(0);
            }
            if (placement!=null) placement.variable().unassign(0);
            for (Iterator i=initialAssignments.values().iterator();i.hasNext();) {
                Placement plac = (Placement)i.next();
                Lecture lect = (Lecture)plac.variable();
                if (!plac.equals(lect.getAssignment())) lect.assign(0,plac);
            }
        }
        return ret;
    }
    
    public Suggestion currentSuggestion() {
    	return iCurrentSuggestion;
    }

    public Suggestion emptySuggestion() {
    	return iEmptySuggestion;
    }
    
    private Vector values(Lecture lecture) {
    	if (lecture.equals(iLecture)) {
    		Vector vals = new Vector();
    		for (Enumeration e=(lecture.allowBreakHard() || !iAllowBreakHard?lecture.values():lecture.computeValues(true)).elements();e.hasMoreElements();) {
    			Placement p = (Placement)e.nextElement();
    			if (match(p)) vals.add(p);
    		}
    		return vals;
    	} else {
    		if (lecture.allowBreakHard() || !iAllowBreakHard) return lecture.values();
    		return lecture.computeValues(true);
    	}
    }
    
    public boolean containsCommited(TimetableModel model, Collection values) {
    	if (model.hasConstantVariables()) {
        	for (Iterator i=values.iterator();i.hasNext();) {
        		Placement placement = (Placement)i.next();
        		Lecture lecture = (Lecture)placement.variable();
        		if (lecture.isCommitted()) return true;
        	}
    	}
    	return false;
    }    

    private void backtrack(long startTime, Vector initialLectures, Vector resolvedLectures, Hashtable conflictsToResolve, Hashtable initialAssignments, int depth) {
        int nrUnassigned = conflictsToResolve.size();
        if ((initialLectures==null || initialLectures.isEmpty()) && nrUnassigned==0) {
        	if (iSuggestions.size()==iLimit) {
        		if (((Suggestion)iSuggestions.last()).isBetter(iSolver)) return;
        	}
            iSuggestions.add(new Suggestion(iSolver,initialAssignments,resolvedLectures, conflictsToResolve.values()));
            iNrSolutions++;
            if (iSuggestions.size()>iLimit) iSuggestions.remove(iSuggestions.last());
            return;
        }
        if (depth<=0) return;
        if (iTimeOut>0 && System.currentTimeMillis()-startTime>iTimeOut) {
            iTimeoutReached = true;
            return;
        }
        for (Enumeration e1=(initialLectures!=null && !initialLectures.isEmpty()?initialLectures.elements():conflictsToResolve.keys());e1.hasMoreElements();) {
            Lecture lecture = (Lecture)e1.nextElement();
            if (resolvedLectures.contains(lecture.getClassId())) continue;
            resolvedLectures.add(lecture.getClassId());
            for (Enumeration e2=values(lecture).elements();e2.hasMoreElements();) {
                Placement placement = (Placement)e2.nextElement();
                if (placement.equals(lecture.getAssignment())) continue;
                if (!iAllowBreakHard && placement.isHard()) continue;
                if (iSameTime && lecture.getAssignment()!=null && !placement.getTimeLocation().equals(((Placement)lecture.getAssignment()).getTimeLocation())) continue;
                if (iSameRoom && lecture.getAssignment()!=null && !placement.sameRooms((Placement)lecture.getAssignment())) continue;
                if (iAllTheSame && iSameTime && lecture.getAssignment()==null) {
                    Placement ini = (Placement)initialAssignments.get(lecture);
                    if (ini!=null && !placement.sameTime(ini)) continue;
                }
                if (iAllTheSame && iSameRoom && lecture.getAssignment()==null) {
                    Placement ini = (Placement)initialAssignments.get(lecture);
                    if (ini!=null && !placement.sameRooms(ini)) continue;
                }
                iNrCombinationsConsidered++;
                Set conflicts = iModel.conflictValues(placement);
                if (conflicts!=null && (nrUnassigned+conflicts.size()>depth)) continue;
                if (containsCommited(iModel, conflicts)) continue;
                boolean containException = false;
                if (conflicts!=null) {
                    for (Iterator i=conflicts.iterator();!containException && i.hasNext();) {
                        Placement c = (Placement)i.next();
                        if (resolvedLectures.contains(((Lecture)c.variable()).getClassId())) containException = true;
                    }
                }
                if (containException) continue;
                Placement cur = (Placement)lecture.getAssignment();
                if (conflicts!=null) {
                    for (Iterator i=conflicts.iterator();!containException && i.hasNext();) {
                        Placement c = (Placement)i.next();
                        c.variable().unassign(0);
                    }
                }
                lecture.assign(0, placement);
                for (Iterator i=conflicts.iterator();!containException && i.hasNext();) {
                    Placement c = (Placement)i.next();
                    conflictsToResolve.put(c.variable(),c);
                }
                Placement resolvedConf = (Placement)conflictsToResolve.remove(lecture);
                backtrack(startTime, null, resolvedLectures, conflictsToResolve, initialAssignments, depth-1);
                if (cur==null)
                    lecture.unassign(0);
                else
                    lecture.assign(0, cur);
                if (conflicts!=null) {
                    for (Iterator i=conflicts.iterator();i.hasNext();) {
                        Placement p = (Placement)i.next();
                        p.variable().assign(0, p);
                        conflictsToResolve.remove(p.variable());
                    }
                }
                if (resolvedConf!=null)
                    conflictsToResolve.put(lecture, resolvedConf);
            }
            resolvedLectures.remove(lecture.getClassId());
        }
    }
    
    public boolean getTimeoutReached() { return iTimeoutReached; }
    public long getNrCombinationsConsidered() { return iNrCombinationsConsidered; }
    public long getNrSolutions() { return iNrSolutions; }
    
    public void computeConfTable() {
    	iConfTable = new Vector();
    	for (Enumeration e=iLecture.timeLocations().elements();e.hasMoreElements();) {
    		TimeLocation t = (TimeLocation)e.nextElement();
    		if (!iAllowBreakHard && PreferenceLevel.sProhibited.equals(PreferenceLevel.int2prolog(t.getPreference())))
    			continue;
    		if (t.getPreference()>500) continue;
    		iConfTable.add(new Suggestion(iSolver,iLecture,t));
    	}
    }
    
    public void computeTryAllAssignments() {
    	iAllAssignments = new TreeSet();
    	for (Enumeration e=iLecture.values().elements();e.hasMoreElements();) {
    		Placement p = (Placement)e.nextElement();
    		if (p.equals(iLecture.getAssignment())) continue;
    		if (p.isHard() && !iAllowBreakHard) continue;
    		if (!match(p)) continue;
            if (iSameTime && iLecture.getAssignment()!=null && !p.getTimeLocation().equals(((Placement)iLecture.getAssignment()).getTimeLocation())) continue;
            if (iSameRoom && iLecture.getAssignment()!=null && !p.sameRooms((Placement)iLecture.getAssignment())) continue;
        	if (iAllAssignments.size()==iLimit && ((Suggestion)iAllAssignments.last()).isBetter(iSolver)) continue;
            iAllAssignments.add(tryAssignment(p));
    		if (iAllAssignments.size()>iLimit) iAllAssignments.remove(iAllAssignments.last());
    	}
    }
    
    public TreeSet tryAllAssignments() {
    	return iAllAssignments;
    }

    public int getNrTries() {
    	return iNrTries;
    }
    
    public Vector getConfTable() {
    	return iConfTable;
    }
    
    public void computeNrTries() {
    	Placement placement = (Placement)iLecture.getAssignment();
    	if (iSameTime && placement!=null)
    		iNrTries = iLecture.nrValues(placement.getTimeLocation())-1;
    	else if (iSameRoom && placement!=null) {
    		if (placement.isMultiRoom()) {
    			iNrTries = iLecture.nrValues(placement.getRoomLocations())-1;
    		} else {
    			iNrTries = iLecture.nrValues(placement.getRoomLocation())-1;
    		}
    	} else
    		iNrTries = iLecture.nrValues()-(placement==null?0:1);
    }
    
    public String toString() {
        return "Suggestions{\n"+
            "  suggestions = "+getSuggestions()+"\n"+
            "  timeoutReached = "+getTimeoutReached()+"\n"+
            "  nrCombinationsConsidered = "+getNrCombinationsConsidered()+"\n"+
            "  nrSolutions = "+getNrSolutions()+"\n"+
            "  currentSuggestion = "+currentSuggestion()+"\n"+
            "  tryAssignments = "+tryAllAssignments()+"\n"+
            "  nrTries = "+getNrTries()+"\n"+
            "  emptySuggestion = "+emptySuggestion()+"\n"+
            "  hints = "+getHints()+"\n"+
            "  confTable = "+getConfTable()+"\n"+
            "}";
    }
}
