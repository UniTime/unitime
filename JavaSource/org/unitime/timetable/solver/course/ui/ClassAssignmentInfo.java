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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.Class_DAO;

/**
 * @author Tomas Muller
 */
public class ClassAssignmentInfo extends ClassAssignment implements Serializable {
	private static final long serialVersionUID = -4277344877497509285L;
	private TreeSet<StudentConflict> iStudentConflicts = new TreeSet();
	
	public ClassAssignmentInfo(Assignment assignment, boolean useRealStudents, Map<ClassAssignment, Set<Long>> conflicts) {
		super(assignment);
		if (conflicts != null)
			findStudentConflicts(null, conflicts);
		else
			findStudentConflicts(null, useRealStudents);
	}

	public ClassAssignmentInfo(Class_ clazz, ClassTimeInfo time, ClassDateInfo date, Collection<ClassRoomInfo> rooms, boolean useRealStudents, Map<ClassAssignment, Set<Long>> conflicts) {
		super(clazz, time, date, rooms);
		if (conflicts != null)
			findStudentConflicts(null, conflicts);
		else
			findStudentConflicts(null, useRealStudents);
	}
	
	public ClassAssignmentInfo(Class_ clazz, ClassTimeInfo time, ClassDateInfo date, Collection<ClassRoomInfo> rooms, Hashtable<Long,ClassAssignment> assignmentTable, boolean useRealStudents, Map<ClassAssignment, Set<Long>> conflicts) {
		super(clazz, time, date, rooms);
		if (conflicts != null)
			findStudentConflicts(assignmentTable, conflicts);
		else
			findStudentConflicts(assignmentTable, useRealStudents);
	}
	
	private void findStudentConflicts(Hashtable<Long,ClassAssignment> assignmentTable, boolean useRealStudents) {
		if (!hasTime()) return;
		//TODO: This might be done much faster
		Hashtable<Long,Set<Long>> conflicts = null;
		if (useRealStudents)
			conflicts = Student.findConflictingStudents(getClassId(), getTime().getStartSlot(), getTime().getLength(), getTime().getDates());
		else
			conflicts = Solution.findConflictingStudents(getClassId(), getTime().getStartSlot(), getTime().getLength(), getTime().getDates());
		for (Map.Entry<Long, Set<Long>> entry: conflicts.entrySet()) {
			if (getClassId().equals(entry.getKey())) continue;
			if (assignmentTable!=null && assignmentTable.containsKey(entry.getKey())) continue;
			Class_ clazz = Class_DAO.getInstance().get(entry.getKey());
			if (clazz.getCommittedAssignment() != null)
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
	
	private void findStudentConflicts(Map<Long,ClassAssignment> assignmentTable, Map<ClassAssignment, Set<Long>> conflicts) {
		for (Map.Entry<ClassAssignment, Set<Long>> e: conflicts.entrySet()) {
			ClassAssignment a = e.getKey();
			ClassAssignment b = (assignmentTable != null ? assignmentTable.get(a.getClassId()) : null);
			if (b != null) a = b;
			if (!a.getClassId().equals(getClassId()) && a.hasTime() && a.getTime().overlaps(getTime()))
				iStudentConflicts.add(new StudentConflict(a, e.getValue()));
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
