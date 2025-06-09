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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class ClassProposedChange implements Serializable, Comparable<ClassProposedChange> {
	private static final long serialVersionUID = 1510362646798301408L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
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
    public Long getSelectedClassId() { return iSelectedClassId; }
    
    public String getHtmlTable(SessionContext context) {
        String ret = "<table border='0' cellspacing='0' cellpadding='3' width='100%'>";
        ret += "<tr>";
        ret += "<td><i>"+MSG.columnClass()+"</i></td>";
        ret += "<td><i>"+MSG.columnInstructor()+"</i></td>";
        ret += "<td><i>"+MSG.columnDateChange()+"</i></td>";
        ret += "<td><i>"+MSG.columnTimeChange()+"</i></td>";
        ret += "<td><i>"+MSG.columnRoomChange()+"</i></td>";
        ret += "</tr>";
        for (ClassAssignment current : iAssignments) {
            ClassAssignment initial = iInitials.get(current.getClassId());
            String bgColor = (current.getClassId().equals(iSelectedClassId)?"rgb(168,187,225)":null);
            boolean canAssign = context.hasPermission(current.getClazz(), Right.ClassAssignment);
            ret += "<tr "+(bgColor==null?"":"style=\"background-color:"+bgColor+";\" ")+
            		(canAssign?"onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';\" "+
            		"onmouseout=\"this.style.backgroundColor='"+(bgColor==null?"transparent":bgColor)+"';\" "+
            		"onclick=\"document.location='classInfo.action?classId="+current.getClassId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';\"": "") + ">";
            ret += "<td nowrap>";
            ret += "<img src='images/action_delete.png' border='0' onclick=\"document.location='classInfo.action?delete="+current.getClassId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';event.cancelBubble=true;\">&nbsp;";
            if (!canAssign && context.hasPermission(current.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering(), Right.OfferingCanLock)) {
            	ret += "<img src='images/error.png' border='0' " +
            			"onclick='if (confirm(\"Course " + current.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseName() + " is not locked. Do you want to lock it?\")) " +
            			"document.location=\"classInfo.action?offering="+current.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId()+"&op=Lock&noCacheTS=" + new Date().getTime()+"\";event.cancelBubble=true;' " +
            			"title=\"Course " + current.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseName() + " is not locked. Click the warning icon to lock it.\" style='cursor: pointer;'>&nbsp;";
            }
            ret += current.getClassNameHtml();
            ret += "</td><td nowrap>";
            ret += current.getLeadingInstructorNames(", ");
            ret += "</td><td nowrap>";
            if (initial!=null && !initial.getDateId().equals(current.getDateId()))
                ret += initial.getDateNameHtml() + " &rarr; ";
            if (initial==null)
                ret += "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>"+MSG.notAssigned()+"</i></font> &rarr; ";
            ret += current.getDateNameHtml();
            ret += "</td><td nowrap>";
            if (initial!=null && !initial.getTimeId().equals(current.getTimeId()))
                ret += initial.getTimeNameHtml() + " &rarr; ";
            if (initial==null)
                ret += "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>"+MSG.notAssigned()+"</i></font> &rarr; ";
            ret += current.getTimeNameHtml();
            ret += "</td><td nowrap>";
            if (initial!=null && !initial.getRoomIds().equals(current.getRoomIds()))
                ret += initial.getRoomNamesHtml(", ") + " &rarr; ";
            if (initial==null)
                ret += "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>"+MSG.notAssigned()+"</i></font> &rarr; ";
            ret += current.getRoomNamesHtml(", ");
            if (current.getNrRooms()!=current.getNumberOfRooms()) {
                if (current.getClassId().equals(iSelectedClassId))
                    ret += "<i>"+MSG.assignmentRoomSelectBelow()+"</i>";
                else
                    ret += "<i><font color='red'>"+MSG.assignmentRoomNotSelected()+"</font></i>";
            }
            ret += "</td></tr>";
        }
        for (ClassAssignment conflict : iConflicts) {
            String bgColor = (conflict.getClassId().equals(iSelectedClassId)?"rgb(168,187,225)":null);
            boolean canAssign = context.hasPermission(conflict.getClazz(), Right.ClassAssignment);
            ret += "<tr "+(bgColor==null?"":"style=\"background-color:"+bgColor+";\" ")+
            	(canAssign ? "onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';\" "+
                "onmouseout=\"this.style.backgroundColor='"+(bgColor==null?"transparent":bgColor)+"';\" "+
                "onclick=\"document.location='classInfo.action?classId="+conflict.getClassId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';\"" : "") + ">";
            ret += "<td nowrap>";
            if (!canAssign && context.hasPermission(conflict.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering(), Right.OfferingCanLock)) {
            	ret += "<img src='images/error.png' border='0' " +
            			"onclick='if (confirm(\"" + MSG.messageCourseNotLocked(conflict.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseName()) + "\")) " +
            			"document.location=\"classInfo.action?offering="+conflict.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId()+"&op=Lock&noCacheTS=" + new Date().getTime()+"\";event.cancelBubble=true;' " +
            			"title=\"" + MSG.titleCourseNotLocked(conflict.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseName()) + "\" style='cursor: pointer;'>&nbsp;";
            }
            ret += conflict.getClassNameHtml();
            ret += "</td><td nowrap>";
            ret += conflict.getLeadingInstructorNames(", ");
            ret += "</td><td nowrap>";
            ret += conflict.getDateNameHtml() + " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>"+MSG.notAssigned()+"</i></font>"+"</td>";
            ret += "</td><td nowrap>";
            ret += conflict.getTimeNameHtml() + " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>"+MSG.notAssigned()+"</i></font>"+"</td>";
            ret += "</td><td nowrap>";
            ret += conflict.getRoomNamesHtml(", ") + " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>"+MSG.notAssigned()+"</i></font>";
            ret += "</td></tr>";
        }
        ret += "</table>";
        return ret;
    }
    
    public String toString(String delim) {
        String ret = "";
        for (ClassAssignment conflict : iConflicts) {
            if (ret.length()>0) ret+=delim;
            ret += conflict.getClassName() + " " + conflict.getDate() + " " + conflict.getTime().getName()+" "+conflict.getRoomNames(", ") + " -> " + MSG.assignmentNotAssigned();
        }
        for (ClassAssignment current : iAssignments) {
            if (ret.length()>0) ret+=delim;
            ClassAssignment initial = iInitials.get(current.getClassId());
            ret += current.getClassName() + " " + (initial==null?MSG.assignmentNotAssigned():initial.getDate()+" "+initial.getTime().getName()+" "+initial.getRoomNames(", ")) + " -> " + current.getDate()+" "+current.getTime().getName()+" "+current.getRoomNames(", ");
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
