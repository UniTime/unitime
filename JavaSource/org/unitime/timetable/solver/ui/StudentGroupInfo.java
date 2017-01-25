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
package org.unitime.timetable.solver.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.Student;
import org.cpsolver.coursett.model.StudentGroup;
import org.cpsolver.ifs.solver.Solver;
import org.dom4j.Element;
import org.unitime.timetable.onlinesectioning.reports.OnlineSectioningReport.Counter;

/**
 * @author Tomas Muller
 */
public class StudentGroupInfo implements TimetableInfo, Serializable {
	private static final long serialVersionUID = 1L;
	public static int sVersion = 1;
	private long iId;
    private String iName;
    private double iWeight;
    private double iValue;
    private List<ClassInfo> iClasses;
	
	public StudentGroupInfo() {
		super();
	}
	
	public StudentGroupInfo(Solver<Lecture, Placement> solver, StudentGroup group) {
		super();
		iId = group.getId();
		iName = group.getName();
		iWeight = group.getWeight();
		iValue = value(group);
		
		Map<Lecture, List<Student>> class2students = new HashMap<Lecture, List<Student>>();
		for (Student student: group.getStudents()) {
			for (Lecture lecture: student.getLectures()) {
				List<Student> students = class2students.get(lecture);
				if (students == null) {
					students = new ArrayList<Student>();
					class2students.put(lecture, students);
				}
				students.add(student);
			}
		}
		iClasses = new ArrayList<ClassInfo>();
		for (Map.Entry<Lecture, List<Student>> entry: class2students.entrySet()) {
			iClasses.add(new ClassInfo(entry.getKey(), entry.getValue()));
		}
	}
	
	public static double value(StudentGroup group) {
		Map<Long, Match> match = new HashMap<Long, Match>();
        for (Student student: group.getStudents())
            for (Lecture lecture: student.getLectures()) {
                Match m = match.get(lecture.getSchedulingSubpartId());
                if (m == null) { m = new Match(group, lecture.getConfiguration().getOfferingId()); match.put(lecture.getSchedulingSubpartId(), m); }
                m.inc(lecture);
            }
        double value = 0.0;
        for (Match m: match.values())
            value += m.value();
        return value / match.size();
	}
	
	public Long getGroupId() { return iId; }
	public String getGroupName() { return iName; }
	public double getGroupWeight() { return iWeight; }
	public double getGroupValue() { return iValue; }
	public List<ClassInfo> getGroupAssignments() { return iClasses; }
	public ClassInfo getGroupAssignment(Long classId) {
		for (ClassInfo clazz: iClasses)
			if (clazz.getClassId().equals(classId)) return clazz;
		return null;
	}
	public List<StudentInfo> getStudentAssignments(Long classId) {
		for (ClassInfo clazz: iClasses)
			if (clazz.getClassId().equals(classId)) return clazz.getStudents();
		return null;
	}
	
	public int countStudents() {
		Set<Long> studentIds = new HashSet<Long>();
		for (ClassInfo clazz: iClasses)
			for (StudentInfo student: clazz.getStudents())
				studentIds.add(student.getStudentId());
		return studentIds.size();
	}
	
	public double countStudentWeights() {
		Map<Long, Counter> counters = new HashMap<Long, Counter>();
		for (ClassInfo clazz: iClasses)
			for (StudentInfo student: clazz.getStudents()) {
				Counter c = counters.get(student.getStudentId());
				if (c == null) {
					c = new Counter();
					counters.put(student.getStudentId(), c);
				}
				c.inc(student.getWeight());
			}
		double total = 0.0;
		for (Counter c: counters.values())
			total += c.avg();
		return total;
	}
	
	public static class ClassInfo {
		private Long iClassId;
		private List<StudentInfo> iStudents = new ArrayList<StudentInfo>();
		
		public ClassInfo(Lecture clazz, Collection<Student> students) {
			iClassId = clazz.getClassId();
			for (Student student: students)
				iStudents.add(new StudentInfo(clazz, student));
		}
		
		public ClassInfo(Element e) {
			iClassId = Long.valueOf(e.attributeValue("id"));
			for (Iterator i = e.elementIterator("student"); i.hasNext(); ) {
				iStudents.add(new StudentInfo((Element)i.next()));
			}
		}
		
		public Long getClassId() { return iClassId; }
		public List<StudentInfo> getStudents() { return iStudents; }
		public double countStudentsWeight() {
			double weight = 0.0;
			for (StudentInfo student: iStudents)
				weight += student.getWeight();
			return weight;
		}
		
		public void save(Element element) {
			element.addAttribute("id", String.valueOf(iClassId));
			for (StudentInfo student: iStudents) {
				student.save(element.addElement("student"));
			}
		}
	}
	
	public static class StudentInfo {
		private Long iStudentId;
		private double iWeight;
		
		public StudentInfo(Lecture lecture, Student student) {
			iStudentId = student.getId();
			iWeight = student.getOfferingWeight(lecture.getConfiguration());
		}
		
		public StudentInfo(Element e) {
			iStudentId = Long.valueOf(e.attributeValue("id"));
			iWeight = Double.valueOf(e.attributeValue("weight", "1.0"));
		}
		
		public Long getStudentId() { return iStudentId; }
		public double getWeight() { return iWeight; }
		
		public void save(Element element) {
			element.addAttribute("id", String.valueOf(iStudentId));
			if (iWeight != 1.0)
				element.addAttribute("weight", String.valueOf(iWeight));
		}
		
	}

	@Override
	public void load(Element root) {
		iId = Long.valueOf(root.attributeValue("id"));
		iName = root.attributeValue("name");
		iWeight = Double.valueOf(root.attributeValue("weight"));
		iValue = Double.valueOf(root.attributeValue("value", "1.0"));
		iClasses = new ArrayList<ClassInfo>();
		for (Iterator i = root.elementIterator("class"); i.hasNext(); ) {
			iClasses.add(new ClassInfo((Element)i.next()));
		}
	}

	@Override
	public void save(Element root) {
		root.addAttribute("version", String.valueOf(sVersion));
		root.addAttribute("id", String.valueOf(iId));
		if (iName != null)
			root.addAttribute("name", iName);
		root.addAttribute("weight", String.valueOf(iWeight));
		if (iValue != 1.0)
			root.addAttribute("value", String.valueOf(iValue));
		for (ClassInfo clazz: iClasses)
			clazz.save(root.addElement("class"));
	}

	@Override
	public boolean saveToFile() {
		return false;
	}
	
	private static class Match {
        private int iTotal = 0;
        private Map<Long, Integer> iMatch = new HashMap<Long, Integer>();
        
        Match(StudentGroup group, Long offeringId) {
            iTotal = group.countStudents(offeringId);
        }
        
        void inc(Lecture lecture) {
            Integer val = iMatch.get(lecture.getClassId());
            iMatch.put(lecture.getClassId(), 1 + (val == null ? 0 : val.intValue()));
        }
        
        double value() {
            if (iTotal <= 1) return 1.0;
            double value = 0.0;
            for (Integer m: iMatch.values())
                if (m > 1)
                    value += (m * (m - 1.0)) / (iTotal * (iTotal - 1.0));
            return value;
        }
        
        @Override
        public String toString() {
            return iTotal + "/" + iMatch;
        }
    }
}
