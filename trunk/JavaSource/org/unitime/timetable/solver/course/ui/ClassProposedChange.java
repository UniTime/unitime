/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2009 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.unitime.timetable.model.PreferenceLevel;

/**
 * @author Tomas Muller
 */
public class ClassProposedChange implements Serializable, Comparable<ClassProposedChange> {
	private static final long serialVersionUID = 1510362646798301408L;
	private Vector<ClassAssignmentInfo> iAssignments = null;
    private Vector<ClassAssignment> iConflicts = null;
    private Hashtable<Long,ClassAssignment> iInitials = null;
    private Long iSelectedClassId = null; 
    
    public ClassProposedChange() {
        iAssignments = new Vector(); iConflicts = new Vector(); iInitials = new Hashtable();
    }
    
    public void addChange(ClassAssignmentInfo change, ClassAssignment initial) {
        for (ClassAssignment assignment : iAssignments) {
            if (assignment.getClassId().equals(change.getClassId())) {
                iAssignments.remove(assignment); iInitials.remove(assignment.getClassId()); break;
            }
        }
        for (ClassAssignment conflict : iAssignments) {
            if (conflict.getClassId().equals(change.getClassId())) {
                iConflicts.remove(conflict); break;
            }
        }
        //if (initial!=null && initial.assignmentEquals(change)) return;
        if (change.getDateId()!=null) {
            iAssignments.add(change); 
            if (initial!=null && initial.getTimeId()!=null) 
                iInitials.put(initial.getClassId(),initial);
        } else {
            iConflicts.add(initial);
        }
    }
    
    public boolean isEmpty() {
        return iConflicts.isEmpty() && iAssignments.isEmpty();
    }
    
    public Collection<ClassAssignment> getConflicts() { return iConflicts; }
    
    public Collection<ClassAssignmentInfo> getAssignments() { return iAssignments; }
    
    public Hashtable<Long,ClassAssignment> getAssignmentTable() {
        Hashtable<Long,ClassAssignment> table = new Hashtable();
        try {
            for (ClassAssignment conflict : iConflicts)
                table.put(conflict.getClassId(), new ClassAssignment(conflict.getClazz(),null,null,null));
        } catch (Exception e) {}
        for (ClassAssignment assignment : iAssignments)
            table.put(assignment.getClassId(), assignment);
        return table;
    }

    
    public ClassAssignment getInitial(ClassAssignment current) { return iInitials.get(current.getClassId()); }
    
    
    public ClassAssignmentInfo getCurrent(ClassInfo clazz) {
        return getCurrent(clazz.getClassId());
    }
    
    public ClassAssignmentInfo getCurrent(Long classId) { 
        for (ClassAssignmentInfo assignment : iAssignments)
            if (assignment.getClassId().equals(classId)) return assignment;
        return null;
    }
    
    public ClassAssignment getConflict(ClassInfo clazz) {
        return getConflict(clazz.getClassId());
    }
    
    public ClassAssignment getConflict(Long classId) { 
        for (ClassAssignment conflict : iConflicts)
            if (conflict.getClassId().equals(classId)) return conflict;
        return null;
    }

    public ClassAssignment getInitial(Long currentId) { return iInitials.get(currentId); }
    
    public int getNrAssigned() { return iAssignments.size(); }
    
    public int getNrUnassigned() { return iConflicts.size(); }
    
    public double getValue() {
        double value = 0;
        for (ClassAssignment conflict : iConflicts) value -= conflict.getValue();
        for (ClassAssignment current : iAssignments) value += current.getValue();
        for (ClassAssignment initial : iInitials.values()) value -= initial.getValue();
        return value;
    }
    
    public int compareTo(ClassProposedChange change) {
        int cmp = Double.compare(getNrUnassigned(), change.getNrUnassigned());
        if (cmp!=0) return cmp;
        return Double.compare(getValue(), change.getValue());
    }
    
    public void setSelected(Long classId) {
        iSelectedClassId = classId;
    }
    
    public String getHtmlTable() {
        String ret = "<table border='0' cellspacing='0' cellpadding='3' width='100%'>";
        ret += "<tr>";
        ret += "<td><i>Class</i></td>";
        ret += "<td><i>Instructor</i></td>";
        ret += "<td><i>Date Change</i></td>";
        ret += "<td><i>Time Change</i></td>";
        ret += "<td><i>Room Change</i></td>";
        ret += "</tr>";
        for (ClassAssignment current : iAssignments) {
            ClassAssignment initial = iInitials.get(current.getClassId());
            String bgColor = (current.getClassId().equals(iSelectedClassId)?"rgb(168,187,225)":null);
            ret += "<tr "+(bgColor==null?"":"style=\"background-color:"+bgColor+";\" ")+
            		"onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';\" "+
            		"onmouseout=\"this.style.backgroundColor='"+(bgColor==null?"transparent":bgColor)+"';\" "+
            		"onclick=\"document.location='classInfo.do?classId="+current.getClassId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
            ret += "<td nowrap>";
            ret += "<img src='images/Delete16.gif' border='0' onclick=\"document.location='classInfo.do?delete="+current.getClassId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';event.cancelBubble=true;\">&nbsp;";
            ret += current.getClassNameHtml();
            ret += "</td><td nowrap>";
            ret += current.getLeadingInstructorNames(", ");
            ret += "</td><td nowrap>";
            if (initial!=null && !initial.getDateId().equals(current.getDateId()))
                ret += initial.getDateNameHtml() + " &rarr; ";
            if (initial==null)
                ret += "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font> &rarr; ";
            ret += current.getDateNameHtml();
            ret += "</td><td nowrap>";
            if (initial!=null && !initial.getTimeId().equals(current.getTimeId()))
                ret += initial.getTimeNameHtml() + " &rarr; ";
            if (initial==null)
                ret += "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font> &rarr; ";
            ret += current.getTimeNameHtml();
            ret += "</td><td nowrap>";
            if (initial!=null && !initial.getRoomIds().equals(current.getRoomIds()))
                ret += initial.getRoomNamesHtml(", ") + " &rarr; ";
            if (initial==null)
                ret += "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font> &rarr; ";
            ret += current.getRoomNamesHtml(", ");
            if (current.getNrRooms()!=current.getNumberOfRooms()) {
                if (current.getClassId().equals(iSelectedClassId))
                    ret += "<i>Select below ...</i>";
                else
                    ret += "<i><font color='red'>Not selected ...</font></i>";
            }
            ret += "</td></tr>";
        }
        for (ClassAssignment conflict : iConflicts) {
            String bgColor = (conflict.getClassId().equals(iSelectedClassId)?"rgb(168,187,225)":null);
            ret += "<tr "+(bgColor==null?"":"style=\"background-color:"+bgColor+";\" ")+
                "onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';\" "+
                "onmouseout=\"this.style.backgroundColor='"+(bgColor==null?"transparent":bgColor)+"';\" "+
                "onclick=\"document.location='classInfo.do?classId="+conflict.getClassId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
            ret += "<td nowrap>";
            ret += conflict.getClassNameHtml();
            ret += "</td><td nowrap>";
            ret += conflict.getLeadingInstructorNames(", ");
            ret += "</td><td nowrap>";
            ret += conflict.getDateNameHtml() + " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>"+"</td>";
            ret += "</td><td nowrap>";
            ret += conflict.getTimeNameHtml() + " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>"+"</td>";
            ret += "</td><td nowrap>";
            ret += conflict.getRoomNamesHtml(", ") + " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>";
            ret += "</td></tr>";
        }
        ret += "</table>";
        return ret;
    }
    
    public String toString(String delim) {
        String ret = "";
        for (ClassAssignment conflict : iConflicts) {
            if (ret.length()>0) ret+=delim;
            ret += conflict.getClassName() + " " + conflict.getDate() + " " + conflict.getTime().getName()+" "+conflict.getRoomNames(", ") + " -> Not Assigned";
        }
        for (ClassAssignment current : iAssignments) {
            if (ret.length()>0) ret+=delim;
            ClassAssignment initial = iInitials.get(current.getClassId());
            ret += current.getClassName() + " " + (initial==null?"Not Assigned":initial.getDate()+" "+initial.getTime().getName()+" "+initial.getRoomNames(", ")) + " -> " + current.getDate()+" "+current.getTime().getName()+" "+current.getRoomNames(", ");
        }
        return ret;
    }
    
    public String toString() { return toString("\n"); }
    
    public boolean isUnassigned(Long classId) {
        for (ClassAssignment conflict : getConflicts())
            if (classId.equals(conflict.getClassId())) return true;
        return false;
    }
}
