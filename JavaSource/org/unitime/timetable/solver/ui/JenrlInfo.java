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
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.constraint.JenrlConstraint;
import org.cpsolver.coursett.criteria.StudentConflict;
import org.cpsolver.coursett.criteria.additional.ImportantStudentConflict;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.Student;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solver.Solver;
import org.dom4j.Element;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;


/**
 * @author Tomas Muller
 */
public class JenrlInfo implements TimetableInfo, Serializable {
	private static DecimalFormat sDF = new DecimalFormat("0.#");
	private static final long serialVersionUID = 1L;
	public static int sVersion = 1; // to be able to do some changes in the future
	public double iJenrl = 0.0;
	public boolean iIsSatisfied = false;
	public boolean iIsHard = false;
	public boolean iIsDistance = false;
	public boolean iIsFixed = false;
	public boolean iIsCommited = false;
	public boolean iIsImportant = false;
	public boolean iIsInstructor = false;
	public double iDistance = 0.0;
	public ClassAssignmentDetails iFirst = null, iSecond = null;
	private TreeSet<CurriculumInfo> iCurriculum2nrStudents = null;
	
	public JenrlInfo() {
		super();
	}
	
	public JenrlInfo(JenrlConstraint jc) {
		this(null, jc);
	}
	
	public JenrlInfo(Solver solver, JenrlConstraint jc) {
		super();
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		TimetableModel model = (TimetableModel)solver.currentSolution().getModel();
		Lecture first = (Lecture)jc.first();
		Placement firstPl = (Placement)assignment.getValue(first);
		Lecture second = (Lecture)jc.second();
		Placement secondPl = (Placement)assignment.getValue(second);
		if (solver!=null) {
			if (firstPl!=null)
				iFirst = new ClassAssignmentDetails(solver,first,firstPl,false);
			if (secondPl!=null)
				iSecond = new ClassAssignmentDetails(solver,second,secondPl,false);
		}
		if (firstPl==null || secondPl==null) return;
		setJenrl(jc.getJenrl());
		setIsSatisfied(jc.isInConflict(assignment));
		if (jc.isInConflict(assignment)) {
			setIsHard(first.areStudentConflictsHard(second));
			setIsFixed(first.nrTimeLocations()==1 && second.nrTimeLocations()==1);
			setIsDistance(!firstPl.getTimeLocation().hasIntersection(secondPl.getTimeLocation()));
			setIsCommited(jc.areStudentConflictsCommitted());
			if (isDistance())
				setDistance(Placement.getDistanceInMeters(model.getDistanceMetric(),firstPl,secondPl));
			StudentConflict imp = (StudentConflict)model.getCriterion(ImportantStudentConflict.class);
			setIsImportant(imp != null && imp.isApplicable(first, second) && imp.inConflict(firstPl, secondPl));
			setIsInstructor(jc.getNrInstructors() > 0);
		}
		Hashtable<String, Double> curriculum2nrStudents = new Hashtable<String, Double>();
		for (Student student: jc.first().sameStudents(jc.second())) {
			if (student.getCurriculum() == null || student.getCurriculum().isEmpty()) continue;
			for (String c: student.getCurriculum().split("\\|")) {
				Double nrStudents = curriculum2nrStudents.get(c);
				curriculum2nrStudents.put(student.getCurriculum(), jc.getJenrlWeight(student) + (nrStudents == null ? 0.0 : nrStudents));
			}
		}
		if (!curriculum2nrStudents.isEmpty()) {
			iCurriculum2nrStudents = new TreeSet<CurriculumInfo>();
			for (Map.Entry<String, Double> entry: curriculum2nrStudents.entrySet()) {
				iCurriculum2nrStudents.add(new CurriculumInfo(entry.getKey(), entry.getValue()));
			}
		}
	}
	
	public ClassAssignmentDetails getFirst() { return iFirst; }
	public ClassAssignmentDetails getSecond() { return iSecond; }
	
	public static Hashtable getCommitedJenrlInfos(Lecture lecture) {
		return getCommitedJenrlInfos(null, lecture);
	}
	
	public static Hashtable<Long, JenrlInfo> getCommitedJenrlInfos(Solver solver, Lecture lecture) {
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		Hashtable<Long, JenrlInfo> ret = new Hashtable<Long, JenrlInfo>();
		Hashtable<Long, Hashtable<String, Double>> assignment2curriculum2nrStudents = new Hashtable<Long, Hashtable<String,Double>>();
		Placement placement = (Placement)assignment.getValue(lecture);
		if (placement==null) return ret;
		for (Iterator i2=lecture.students().iterator();i2.hasNext();) {
			Student student = (Student)i2.next();
			Set conflicts = student.conflictPlacements(placement);
			if (conflicts==null) continue;
			for (Iterator i3=conflicts.iterator();i3.hasNext();) {
				Placement pl = (Placement)i3.next();
				JenrlInfo info = ret.get(pl.getAssignmentId());
				if (info==null) {
					info = new JenrlInfo();
					info.setIsCommited(true);
					info.setIsDistance(!pl.getTimeLocation().hasIntersection(placement.getTimeLocation()));
					info.setIsFixed(lecture.nrTimeLocations()==1);
					if (solver!=null) {
						info.iFirst = new ClassAssignmentDetails(solver,lecture,placement,false);
						info.iSecond = new ClassAssignmentDetails(solver,(Lecture)pl.variable(),pl,false);
					}
					if (info.isDistance())
						info.setDistance(Placement.getDistanceInMeters(((TimetableModel)lecture.getModel()).getDistanceMetric(),placement,pl));
					ret.put(pl.getAssignmentId(),info);
				}
				if (student.getCurriculum() != null && !student.getCurriculum().isEmpty()) {
					Hashtable<String, Double> curriculum2nrStudents = assignment2curriculum2nrStudents.get(pl.getAssignmentId());
					if (curriculum2nrStudents == null) {
						curriculum2nrStudents = new Hashtable<String, Double>();
						assignment2curriculum2nrStudents.put(pl.getAssignmentId(), curriculum2nrStudents);
					}
					for (String c: student.getCurriculum().split("\\|")) {
						Double nrStudents = curriculum2nrStudents.get(c);
						curriculum2nrStudents.put(student.getCurriculum(), student.getJenrlWeight(lecture, pl.variable()) + (nrStudents == null ? 0.0 : nrStudents));
					}
				}
				info.setJenrl(info.getJenrl() + student.getJenrlWeight(lecture, pl.variable()));
			}
		}
		for (Map.Entry<Long, Hashtable<String, Double>> entry: assignment2curriculum2nrStudents.entrySet()) {
			Long assignmentId = entry.getKey();
			Hashtable<String, Double> curriculum2nrStudents = entry.getValue();
			if (!curriculum2nrStudents.isEmpty()) {
				JenrlInfo info = ret.get(assignmentId);
				info.iCurriculum2nrStudents = new TreeSet<CurriculumInfo>();
				for (Map.Entry<String, Double> e: curriculum2nrStudents.entrySet()) {
					info.iCurriculum2nrStudents.add(new CurriculumInfo(e.getKey(), e.getValue()));
				}
			}
		}
		return ret;
	}
	
	public double getJenrl() { return iJenrl; }
	public void setJenrl(double jenrl) { iJenrl = jenrl; }
	public boolean isSatisfied() { return iIsSatisfied; }
	public void setIsSatisfied(boolean isSatisfied) { iIsSatisfied = isSatisfied; }
	public boolean isHard() { return iIsHard; }
	public void setIsHard(boolean isHard) { iIsHard = isHard; }
	public boolean isDistance() { return iIsDistance; }
	public void setIsDistance(boolean isDistance) { iIsDistance = isDistance; }
	public boolean isFixed() { return iIsFixed; }
	public void setIsFixed(boolean isFixed) { iIsFixed = isFixed; }
	public boolean isCommited() { return iIsCommited; }
	public void setIsCommited(boolean isCommited) { iIsCommited = isCommited; }
	public boolean isImportant() { return iIsImportant; }
	public void setIsImportant(boolean isImportant) { iIsImportant = isImportant; }
	public boolean isInstructor() { return iIsInstructor; }
	public void setIsInstructor(boolean isInstructor) { iIsInstructor = isInstructor; }
	public double getDistance() { return iDistance; }
	public void setDistance(double distance) { iDistance = distance; }
	
	public boolean hasCurricula() { return iCurriculum2nrStudents != null; }
	public String getCurriculumText() {
		if (!hasCurricula()) return "";
		int top = 0;
		double total = 0.0;
		for (CurriculumInfo i: iCurriculum2nrStudents) {
			total += i.getNrStudents();
		}
		String ret = "";
		for (CurriculumInfo i: iCurriculum2nrStudents) {
			double fraction = i.getNrStudents() / total;
			if (top < 3) {
				top++;
				if (!ret.isEmpty()) ret += ", ";
				ret += sDF.format(100.0 * fraction) + "% " + i.getName();
				if (fraction == 1.0) return i.getName();
			} else {
				ret += ", ...";
				break;
			}
		}
		return ret;
	}
	
	public void load(Element root) {
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==1) {
			iJenrl = Double.parseDouble(root.elementText("jenrl"));
			iIsSatisfied = Boolean.valueOf(root.elementText("satisfied")).booleanValue();
			iIsDistance = Boolean.valueOf(root.elementText("dist")).booleanValue();
			iIsFixed = Boolean.valueOf(root.elementText("fixed")).booleanValue();
			iIsHard = Boolean.valueOf(root.elementText("hard")).booleanValue();
			if (root.elementText("distance")!=null)
				iDistance = Double.parseDouble(root.elementText("distance"));
			if (root.elementText("commited")==null) {
				iIsCommited = false;
			} else {
				iIsCommited = Boolean.valueOf(root.elementText("commited")).booleanValue();
			}
			if (root.elementText("important")==null) {
				iIsImportant = false;
			} else {
				iIsImportant = Boolean.valueOf(root.elementText("important")).booleanValue();
			}
			if (root.elementText("instructor")==null) {
				iIsInstructor = false;
			} else {
				iIsInstructor = Boolean.valueOf(root.elementText("instructor")).booleanValue();
			}
		}
	}
	
	public void save(Element root) {
		root.addAttribute("version", String.valueOf(sVersion));
		root.addElement("jenrl").setText(String.valueOf(iJenrl));
		root.addElement("satisfied").setText(String.valueOf(iIsSatisfied));
		root.addElement("dist").setText(String.valueOf(iIsDistance));
		root.addElement("fixed").setText(String.valueOf(iIsFixed));
		root.addElement("hard").setText(String.valueOf(iIsHard));
		root.addElement("commited").setText(String.valueOf(iIsCommited));
		root.addElement("distance").setText(String.valueOf(iDistance));
		root.addElement("important").setText(String.valueOf(iIsImportant));
		root.addElement("instructor").setText(String.valueOf(iIsInstructor));
	}

	public boolean saveToFile() {
		return false;
	}
	
	public static class CurriculumInfo implements Serializable, Comparable<CurriculumInfo> {
		private static final long serialVersionUID = 1L;
		private String iName;
		private double iNrStudents;
		public CurriculumInfo(String name, double nrStudents) {
			iName = name;
			iNrStudents = nrStudents;
		}
		
		public String getName() { return iName; }
		public double getNrStudents() { return iNrStudents; }
		
		public int compareTo(CurriculumInfo i) {
			int cmp = Double.compare(i.getNrStudents(), getNrStudents());
			if (cmp != 0) return cmp;
			return getName().compareTo(i.getName());
		}
	}
}
