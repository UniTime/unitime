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
package org.unitime.timetable.solver.exam.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import org.cpsolver.exam.model.Exam;
import org.cpsolver.exam.model.ExamModel;
import org.cpsolver.exam.model.ExamPlacement;
import org.cpsolver.ifs.assignment.Assignment;
import org.unitime.timetable.model.ExamPeriod;


/**
 * @author Tomas Muller
 */
public class ExamProposedChange implements Serializable, Comparable<ExamProposedChange> {
	private static final long serialVersionUID = -5497603865422857068L;
	private Vector<ExamAssignmentInfo> iAssignments = null;
    private Vector<ExamAssignment> iConflicts = null;
    private Hashtable<Long,ExamAssignment> iInitials = null;
    private Long iSelectedExamId = null; 
    
    private double iValue = 0;
    private int iNrAssigned = 0;
    
    public ExamProposedChange() {
        iAssignments = new Vector(); iConflicts = new Vector(); iInitials = new Hashtable();
    }

    public ExamProposedChange(ExamModel model, Assignment<Exam, ExamPlacement> assignment, Hashtable<Exam,ExamPlacement> initialAssignment, Hashtable<Exam,ExamAssignment> initialInfo, Collection<ExamPlacement> conflicts, Vector<Exam> resolvedExams) {
        iValue = model.getTotalValue(assignment); iNrAssigned = model.nrAssignedVariables(assignment);
        if (conflicts!=null) {
            iConflicts = new Vector();
            for (ExamPlacement conflict : conflicts) iConflicts.add(initialInfo.get((Exam)conflict.variable()));
        }
        iAssignments = new Vector();
        iInitials = new Hashtable();
        for (Exam exam : resolvedExams) {
            ExamPlacement current = assignment.getValue(exam);
            ExamPlacement initial = initialAssignment.get(exam);
            if (initial==null) {
                iAssignments.add(new ExamAssignmentInfo(exam,current,assignment));
            } else if (!initial.equals(current)) {
                iAssignments.add(new ExamAssignmentInfo(exam,current,assignment));
                iInitials.put(exam.getId(),initialInfo.get(exam));
            }
        }
        for (Exam exam: model.assignedVariables(assignment)) {
            if (resolvedExams.contains(exam)) continue;
            ExamPlacement current = (ExamPlacement)assignment.getValue(exam);
            ExamPlacement initial = initialAssignment.get(exam);
            if (initial==null) {
                iAssignments.add(new ExamAssignmentInfo(exam,current,assignment));
            } else if (!initial.equals(current)) {
                iAssignments.add(new ExamAssignmentInfo(exam,current,assignment));
                iInitials.put(exam.getId(),initialInfo.get(exam));
            }
        }
    }
    
    public void addChange(ExamAssignmentInfo change, ExamAssignment initial) {
        for (ExamAssignment assignment : iAssignments) {
            if (assignment.getExamId().equals(change.getExamId())) {
                iAssignments.remove(assignment); iInitials.remove(assignment.getExamId()); break;
            }
        }
        for (ExamAssignment conflict : iAssignments) {
            if (conflict.getExamId().equals(change.getExamId())) {
                iConflicts.remove(conflict); break;
            }
        }
        //if (initial!=null && initial.assignmentEquals(change)) return;
        if (change != null && change.getPeriodId()!=null) {
            iAssignments.add(change); 
            if (initial!=null && initial.getPeriodId()!=null) 
                iInitials.put(initial.getExamId(),initial);
        } else {
            iConflicts.add(initial);
        }
    }
    
    public boolean isEmpty() {
        return iConflicts.isEmpty() && iAssignments.isEmpty();
    }
    
    public boolean isBetter(ExamModel model, Assignment<Exam, ExamPlacement> assignment) {
        if (iNrAssigned>model.nrAssignedVariables(assignment)) return true;
        if (iNrAssigned==model.nrAssignedVariables(assignment) && iValue<model.getTotalValue(assignment)) return true;
        return false;
    }
    
    public Collection<ExamAssignment> getConflicts() { return iConflicts; }
    
    public Collection<ExamAssignmentInfo> getAssignments() { return iAssignments; }
    
    public Hashtable<Long,ExamAssignment> getAssignmentTable() {
        Hashtable<Long,ExamAssignment> table = new Hashtable();
        try {
            for (ExamAssignment conflict : iConflicts)
                table.put(conflict.getExamId(), new ExamAssignment(conflict.getExam(),(ExamPeriod)null,null));
        } catch (Exception e) {}
        for (ExamAssignment assignment : iAssignments)
            table.put(assignment.getExamId(), assignment);
        return table;
    }

    
    public ExamAssignment getInitial(ExamAssignment current) { return iInitials.get(current.getExamId()); }
    
    
    public ExamAssignmentInfo getCurrent(ExamInfo exam) {
        return getCurrent(exam.getExamId());
    }
    
    public ExamAssignmentInfo getCurrent(Long examId) { 
        for (ExamAssignmentInfo assignment : iAssignments)
            if (assignment.getExamId().equals(examId)) return assignment;
        return null;
    }
    
    public ExamAssignment getConflict(ExamInfo exam) {
        return getConflict(exam.getExamId());
    }
    
    public ExamAssignment getConflict(Long examId) { 
        for (ExamAssignment conflict : iConflicts)
            if (conflict.getExamId().equals(examId)) return conflict;
        return null;
    }

    public ExamAssignment getInitial(Long currentId) { return iInitials.get(currentId); }
    
    public int getNrAssigned() { return iAssignments.size(); }
    
    public int getNrUnassigned() { return iConflicts.size(); }
    
    public double getValue() {
        double value = 0;
        for (ExamAssignment conflict : iConflicts) value -= conflict.getPlacementValue();
        for (ExamAssignment current : iAssignments) value += current.getPlacementValue();
        for (ExamAssignment initial : iInitials.values()) value -= initial.getPlacementValue();
        return value;
    }
    
    public int compareTo(ExamProposedChange change) {
        int cmp = Double.compare(getNrUnassigned(), change.getNrUnassigned());
        if (cmp!=0) return cmp;
        return Double.compare(getValue(), change.getValue());
    }
    
    public void setSelected(Long examId) {
        iSelectedExamId = examId;
    }
    
    public Long getSelected() {
    	return iSelectedExamId;
    }
    
    public String toString(String delim) {
        String ret = "";
        for (ExamAssignment conflict : iConflicts) {
            if (ret.length()>0) ret+=delim;
            ret += conflict.getExamName() + " " + conflict.getPeriodName()+" "+conflict.getRoomsName(", ") + " -> Not Assigned";
        }
        for (ExamAssignment current : iAssignments) {
            if (ret.length()>0) ret+=delim;
            ExamAssignment initial = iInitials.get(current.getExamId());
            ret += current.getExamName() + " " + (initial==null?"Not Assigned":initial.getPeriodName()+" "+initial.getRoomsName(", ")) + " -> " + current.getPeriodName()+" "+current.getRoomsName(", ");
        }
        return ret;
    }
    
    public String toString() { return toString("\n"); }
    
    public boolean isUnassigned(Long examId) {
        for (ExamAssignment conflict : getConflicts())
            if (examId.equals(conflict.getExamId())) return true;
        return false;
    }
}
