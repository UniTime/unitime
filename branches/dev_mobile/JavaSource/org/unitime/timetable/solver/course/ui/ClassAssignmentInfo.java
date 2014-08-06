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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.Class_DAO;

/**
 * @author Tomas Muller
 */
public class ClassAssignmentInfo extends ClassAssignment implements Serializable {
	private static final long serialVersionUID = -4277344877497509285L;
	private TreeSet<StudentConflict> iStudentConflicts = new TreeSet();
	
	public ClassAssignmentInfo(Assignment assignment) {
		super(assignment);
		findStudentConflicts(null);
	}

	public ClassAssignmentInfo(Class_ clazz, ClassTimeInfo time, ClassDateInfo date, Collection<ClassRoomInfo> rooms) {
		super(clazz, time, date, rooms);
		findStudentConflicts(null);
	}
	
	public ClassAssignmentInfo(Class_ clazz, ClassTimeInfo time, ClassDateInfo date, Collection<ClassRoomInfo> rooms, Hashtable<Long,ClassAssignment> assignmentTable) {
		super(clazz, time, date, rooms);
		findStudentConflicts(assignmentTable);
	}
	
	private void findStudentConflicts(Hashtable<Long,ClassAssignment> assignmentTable) {
		if (!hasTime()) return;
		//TODO: This might be done much faster
		Hashtable<Long,Set<Long>> conflicts = Student.findConflictingStudents(getClassId(), getTime().getStartSlot(), getTime().getLength(), getTime().getDates());
		for (Map.Entry<Long, Set<Long>> entry: conflicts.entrySet()) {
			if (getClassId().equals(entry.getKey())) continue;
			if (assignmentTable!=null && assignmentTable.containsKey(entry.getKey())) continue;
			Class_ clazz = Class_DAO.getInstance().get(entry.getKey());
			iStudentConflicts.add(new StudentConflict(new ClassAssignment(clazz.getCommittedAssignment()), entry.getValue()));
		}
		if (assignmentTable!=null) for (Map.Entry<Long, ClassAssignment> entry: assignmentTable.entrySet()) {
			if (getClassId().equals(entry.getKey())) continue;
			if (!entry.getValue().hasTime()) continue;
			if (!getTime().overlaps(entry.getValue().getTime())) continue;
			Set<Long> conf = merge(getStudents(), entry.getValue().getStudents());
			if (!conf.isEmpty())
				iStudentConflicts.add(new StudentConflict(entry.getValue(), conf));
		}
	}
	
	public Set<StudentConflict> getStudentConflicts() {
		return iStudentConflicts;
	}
	
	public int getNrStudentCounflicts() {
		Set<Long> all = new HashSet<Long>();
		for (StudentConflict c:iStudentConflicts)
			all.addAll(c.getConflictingStudents());
		return all.size();
	}
	
	public String getConflictTable() {
        return getConflictTable(true);
    }
    
    public String getConflictTable(boolean header) {
        String ret = "<table border='0' width='100%' cellspacing='0' cellpadding='3'>";
        if (header) {
            ret += "<tr>";
            ret += "<td><i>Students</i></td>";
            ret += "<td><i>Class</i></td>";
            ret += "<td><i>Date</i></td>";
            ret += "<td><i>Time</i></td>";
            ret += "<td><i>Room</i></td>";
            ret += "</tr>";
        }
        for (StudentConflict conf: getStudentConflicts())
        	ret += conf.toHtml();
        ret += "</table>";
        return ret;
    }
	 
	
	public static Set<Long> merge(Set<Long> a, Set<Long> b) {
		Set<Long> ret = new HashSet<Long>();
		for (Long x:a)
			if (b.contains(x)) ret.add(x);
		return ret;
	}
	
	public class StudentConflict implements Serializable, Comparable<StudentConflict> {
		private static final long serialVersionUID = -4480647127446582658L;
		private ClassAssignment iOtherClass = null;
		private Set<Long> iConflictingStudents = null;
		
		public StudentConflict(ClassAssignment other, Set<Long> students) {
			iOtherClass = other;
			iConflictingStudents = students;
		}
		
		public ClassAssignment getOtherClass() {
			return iOtherClass;
		}
		
		public Set<Long> getConflictingStudents() {
			return iConflictingStudents;
		}
		
		public int hashCode() {
			return getClassId().hashCode() ^ getOtherClass().getClassId().hashCode();
		}
		
		public boolean equals(Object o) {
			if (o==null || !(o instanceof StudentConflict)) return false;
			return getOtherClass().equals(((StudentConflict)o).getOtherClass());
		}

		public int compareTo(StudentConflict c) {
			int cmp = c.getConflictingStudents().size() - getConflictingStudents().size();
			if (cmp!=0) return cmp;
			return getOtherClass().compareTo(c.getOtherClass());
		}
        public String toHtml() {
            String ret = "";
            ret += "<tr onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';\" onmouseout=\"this.style.backgroundColor='transparent';\" onclick=\"document.location='classInfo.do?classId="+getOtherClass().getClassId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
            ret += "<td style='font-weight:bold;color:"+PreferenceLevel.prolog2color("P")+";'>";
            ret += String.valueOf(getConflictingStudents().size());
            ret += "<td>"+getOtherClass().getClassNameHtml()+"</td>";
            ret += "<td>"+getOtherClass().getDate().toHtml()+"</td>";
            ret += "<td>"+getOtherClass().getTime().getLongNameHtml()+"</td>";
            ret += "<td>"+getOtherClass().getRoomNamesHtml(", ")+"</td>";
            ret += "</tr>";
            return ret;
        }
        public String toHtml2() {
            String ret = "";
            ret += "<tr onmouseover=\"this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';\" onmouseout=\"this.style.backgroundColor='transparent';\" onclick=\"document.location='classInfo.do?classId="+getOtherClass().getClassId()+"&op=Select&noCacheTS=" + new Date().getTime()+"';\">";
            ret += "<td nowrap style='font-weight:bold;color:"+PreferenceLevel.prolog2color("P")+";'>";
            ret += String.valueOf(getConflictingStudents().size())+"<br>";
            ret += "<td nowrap>"+getClassNameHtml()+"<br>"+getOtherClass().getClassNameHtml()+"</td>";
            ret += "<td nowrap>"+getDate().toHtml()+"<br>"+getOtherClass().getDate().toHtml()+"</td>";
            ret += "<td nowrap>"+getTime().getLongNameHtml()+"<br>"+getOtherClass().getTime().getLongNameHtml()+"</td>";
            ret += "<td nowrap>"+getRoomNamesHtml(", ")+"<br>"+getOtherClass().getRoomNamesHtml(", ")+"</td>";
            ret += "</tr>";
            return ret;
        }
	}

}
