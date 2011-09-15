/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.solver.exam.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;

import net.sf.cpsolver.exam.model.Exam;
import net.sf.cpsolver.exam.model.ExamModel;
import net.sf.cpsolver.exam.model.ExamPlacement;

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

    public ExamProposedChange(ExamModel model, Hashtable<Exam,ExamPlacement> initialAssignment, Hashtable<Exam,ExamAssignment> initialInfo, Collection<ExamPlacement> conflicts, Vector<Exam> resolvedExams) {
        iValue = model.getTotalValue(); iNrAssigned = model.nrAssignedVariables();
        if (conflicts!=null) {
            iConflicts = new Vector();
            for (ExamPlacement conflict : conflicts) iConflicts.add(initialInfo.get((Exam)conflict.variable()));
        }
        iAssignments = new Vector();
        iInitials = new Hashtable();
        for (Exam exam : resolvedExams) {
            ExamPlacement current = (ExamPlacement)exam.getAssignment();
            ExamPlacement initial = initialAssignment.get(exam);
            if (initial==null) {
                iAssignments.add(new ExamAssignmentInfo(exam,current));
            } else if (!initial.equals(current)) {
                iAssignments.add(new ExamAssignmentInfo(exam,current));
                iInitials.put(exam.getId(),initialInfo.get(exam));
            }
        }
        for (Exam exam: model.assignedVariables()) {
            if (resolvedExams.contains(exam)) continue;
            ExamPlacement current = (ExamPlacement)exam.getAssignment();
            ExamPlacement initial = initialAssignment.get(exam);
            if (initial==null) {
                iAssignments.add(new ExamAssignmentInfo(exam,current));
            } else if (!initial.equals(current)) {
                iAssignments.add(new ExamAssignmentInfo(exam,current));
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
        if (change.getPeriodId()!=null) {
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
    
    public boolean isBetter(ExamModel model) {
        if (iNrAssigned>model.nrAssignedVariables()) return true;
        if (iNrAssigned==model.nrAssignedVariables() && iValue<model.getTotalValue()) return true;
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
    
    public String getHtmlTable() {
        String ret = "<table border='0' cellspacing='0' cellpadding='3' width='100%'>";
        ret += "<tr>";
        ret += "<td><i>Examination</i></td>";
        ret += "<td><i>Period Change</i></td>";
        ret += "<td><i>Room Change</i></td>";
        ret += "<td><i>Direct</i></td>";
        ret += "<td><i>&gt;2 A Day</i></td>";
        ret += "<td><i>BTB</i></td>";
        ret += "</tr>";
        for (ExamAssignment current : iAssignments) {
            ExamAssignment initial = iInitials.get(current.getExamId());
            String bgColor = (current.getExamId().equals(iSelectedExamId)?"rgb(168,187,225)":null);
            ret += "<tr "+(bgColor==null?"":"style=\"background-color:"+bgColor+";\" ")+
            		"onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';\" "+
            		"onmouseout=\"this.style.backgroundColor='"+(bgColor==null?"transparent":bgColor)+"';\" "+
            		"onclick=\"document.location='examInfo.do?examId="+current.getExamId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
            ret += "<td nowrap>";
            ret += "<img src='images/Delete16.gif' border='0' onclick=\"document.location='examInfo.do?delete="+current.getExamId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';event.cancelBubble=true;\">&nbsp;";
            ret += current.getExamNameHtml();
            ret += "</td><td nowrap>";
            if (initial!=null && !initial.getPeriodId().equals(current.getPeriodId()))
                ret += initial.getPeriodAbbreviationWithPref() + " &rarr; ";
            if (initial==null)
                ret += "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font> &rarr; ";
            ret += current.getPeriodAbbreviationWithPref();
            ret += "</td><td nowrap>";
            if (initial!=null && !initial.getRoomIds().equals(current.getRoomIds()))
                ret += initial.getRoomsNameWithPref(", ") + " &rarr; ";
            if (initial==null)
                ret += "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font> &rarr; ";
            ret += current.getRoomsNameWithPref(", ");
            if (current.getNrRooms()==0 && current.getMaxRooms()>0) {
                if (current.getExamId().equals(iSelectedExamId))
                    ret += "<i>Select below ...</i>";
                else
                    ret += "<i><font color='red'>Not selected ...</font></i>";
            }
            ret += "</td><td nowrap>";
            ret += ClassAssignmentDetails.dispNumberShort(false,(initial==null?0:initial.getPlacementNrDirectConflicts()), current.getPlacementNrDirectConflicts());
            ret += "</td><td nowrap>";
            ret += ClassAssignmentDetails.dispNumberShort(false,(initial==null?0:initial.getPlacementNrMoreThanTwoADayConflicts()), current.getPlacementNrMoreThanTwoADayConflicts());
            ret += "</td><td nowrap>";
            ret += ClassAssignmentDetails.dispNumberShort(false,(initial==null?0:initial.getPlacementNrBackToBackConflicts()), current.getPlacementNrBackToBackConflicts());
            if (current.getPlacementNrDistanceBackToBackConflicts()>0)
                ret += " (d:"+ClassAssignmentDetails.dispNumberShort(false,(initial==null?0:initial.getPlacementNrDistanceBackToBackConflicts()), current.getPlacementNrDistanceBackToBackConflicts())+")";
            ret += "</td></tr>";
        }
        for (ExamAssignment conflict : iConflicts) {
            String bgColor = (conflict.getExamId().equals(iSelectedExamId)?"rgb(168,187,225)":null);
            ret += "<tr "+(bgColor==null?"":"style=\"background-color:"+bgColor+";\" ")+
                "onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';\" "+
                "onmouseout=\"this.style.backgroundColor='"+(bgColor==null?"transparent":bgColor)+"';\" "+
                "onclick=\"document.location='examInfo.do?examId="+conflict.getExamId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
            ret += "<td nowrap>";
            ret += conflict.getExamNameHtml();
            ret += "</td><td nowrap>";
            ret += conflict.getPeriodAbbreviationWithPref() + " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>"+"</td>";
            ret += "</td><td nowrap>";
            ret += conflict.getRoomsNameWithPref(", ") + " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>";
            ret += "</td><td nowrap>";
            ret += ClassAssignmentDetails.dispNumberShort(true, conflict.getPlacementNrDirectConflicts(), 0);
            ret += "</td><td nowrap>";
            ret += ClassAssignmentDetails.dispNumberShort(true, conflict.getPlacementNrMoreThanTwoADayConflicts(), 0);
            ret += "</td><td nowrap>";
            ret += ClassAssignmentDetails.dispNumberShort(true, conflict.getPlacementNrBackToBackConflicts(), 0);
            if (conflict.getPlacementNrDistanceBackToBackConflicts()>0)
                ret += " (d:"+ClassAssignmentDetails.dispNumberShort(true,conflict.getPlacementNrDistanceBackToBackConflicts(), 0)+")";
            ret += "</td></tr>";
        }
        ret += "</table>";
        return ret;
    }
    
    public String getHtmlLine(int index) {
        String ret = "<tr onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';\" onmouseout=\"this.style.backgroundColor='transparent';\" onclick=\"document.location='examInfo.do?suggestion="+index+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
        String name = "", period = "", room = "", dc = "", btb = "", m2d = "";
        for (ExamAssignment current : iAssignments) {
            ExamAssignment initial = iInitials.get(current.getExamId());
            if (name.length()>0) { name += "<br>"; period += "<br>"; room += "<br>"; dc += "<br>"; m2d += "<br>"; btb += "<br>"; }
            name += current.getExamNameHtml();
            if (initial!=null && !initial.getPeriodId().equals(current.getPeriodId()))
                period += initial.getPeriodAbbreviationWithPref() + " &rarr; ";
            period += current.getPeriodAbbreviationWithPref();
            if (initial!=null && !initial.getRoomIds().equals(current.getRoomIds()))
                room += initial.getRoomsNameWithPref(", ") + " &rarr; ";
            room += current.getRoomsNameWithPref(", ");
            dc += ClassAssignmentDetails.dispNumberShort(false,(initial==null?0:initial.getPlacementNrDirectConflicts()), current.getPlacementNrDirectConflicts());
            m2d += ClassAssignmentDetails.dispNumberShort(false,(initial==null?0:initial.getPlacementNrMoreThanTwoADayConflicts()), current.getPlacementNrMoreThanTwoADayConflicts());
            btb += ClassAssignmentDetails.dispNumberShort(false,(initial==null?0:initial.getPlacementNrBackToBackConflicts()), current.getPlacementNrBackToBackConflicts());
            if (current.getPlacementNrDistanceBackToBackConflicts()>0)
                btb += " (d:"+ClassAssignmentDetails.dispNumberShort(false,(initial==null?0:initial.getPlacementNrDistanceBackToBackConflicts()), current.getPlacementNrDistanceBackToBackConflicts())+")";
        }
        for (ExamAssignment conflict : iConflicts) {
            if (name.length()>0) { name += "<br>"; period += "<br>"; room += "<br>"; dc += "<br>"; m2d += "<br>"; btb += "<br>"; }
            name += conflict.getExamNameHtml();
            period += conflict.getPeriodAbbreviationWithPref() + " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>"+"</td>";
            room += conflict.getRoomsNameWithPref(", ") + " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>";
            dc += ClassAssignmentDetails.dispNumberShort(true, conflict.getPlacementNrDirectConflicts(), 0);
            m2d += ClassAssignmentDetails.dispNumberShort(true, conflict.getPlacementNrMoreThanTwoADayConflicts(), 0);
            btb += ClassAssignmentDetails.dispNumberShort(true, conflict.getPlacementNrBackToBackConflicts(), 0);
            if (conflict.getPlacementNrDistanceBackToBackConflicts()>0)
                btb += " (d:"+ClassAssignmentDetails.dispNumberShort(true,conflict.getPlacementNrDistanceBackToBackConflicts(), 0)+")";
        }
        ret += "<td align='right' width='1%' nowrap>"+ClassAssignmentDetails.dispNumber(Math.round(getValue()))+"</td><td nowrap>";
        ret += name;
        ret += "</td><td nowrap>";
        ret += period;
        ret += "</td><td nowrap>";
        ret += room;
        ret += "</td><td nowrap>";
        ret += dc;
        ret += "</td><td nowrap>";
        ret += m2d;
        ret += "</td><td nowrap>";
        ret += btb;
        ret += "</td></tr>";
        return ret;
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
