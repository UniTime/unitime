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
package org.unitime.timetable.solver.ui;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Element;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;

import net.sf.cpsolver.coursett.constraint.JenrlConstraint;
import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.Student;
import net.sf.cpsolver.coursett.model.TimetableModel;
import net.sf.cpsolver.ifs.solver.Solver;

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
		Lecture first = (Lecture)jc.first();
		Placement firstPl = (Placement)first.getAssignment();
		Lecture second = (Lecture)jc.second();
		Placement secondPl = (Placement)second.getAssignment();
		if (solver!=null) {
			if (firstPl!=null)
				iFirst = new ClassAssignmentDetails(solver,first,firstPl,false);
			if (secondPl!=null)
				iSecond = new ClassAssignmentDetails(solver,second,secondPl,false);
		}
		if (firstPl==null || secondPl==null) return;
		setJenrl(jc.getJenrl());
		setIsSatisfied(jc.isInConflict());
		if (jc.isInConflict()) {
			setIsHard(first.areStudentConflictsHard(second));
			setIsFixed(first.nrTimeLocations()==1 && second.nrTimeLocations()==1);
			setIsDistance(!firstPl.getTimeLocation().hasIntersection(secondPl.getTimeLocation()));
			setIsCommited(jc.areStudentConflictsCommitted());
			if (isDistance())
				setDistance(Placement.getDistanceInMeters(((TimetableModel)jc.getModel()).getDistanceMetric(),firstPl,secondPl));
		}
		Hashtable<String, Double> curriculum2nrStudents = new Hashtable<String, Double>();
		for (Student student: jc.first().sameStudents(jc.second())) {
			if (student.getCurriculum() == null) continue;
			Double nrStudents = curriculum2nrStudents.get(student.getCurriculum());
			curriculum2nrStudents.put(student.getCurriculum(), jc.getJenrlWeight(student) + (nrStudents == null ? 0.0 : nrStudents));
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
		Hashtable<Long, JenrlInfo> ret = new Hashtable<Long, JenrlInfo>();
		Hashtable<Long, Hashtable<String, Double>> assignment2curriculum2nrStudents = new Hashtable<Long, Hashtable<String,Double>>();
		Placement placement = (Placement)lecture.getAssignment();
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
				if (student.getCurriculum() != null) {
					Hashtable<String, Double> curriculum2nrStudents = assignment2curriculum2nrStudents.get(pl.getAssignmentId());
					if (curriculum2nrStudents == null) {
						curriculum2nrStudents = new Hashtable<String, Double>();
						assignment2curriculum2nrStudents.put(pl.getAssignmentId(), curriculum2nrStudents);
					}
					Double nrStudents = curriculum2nrStudents.get(student.getCurriculum());
					curriculum2nrStudents.put(student.getCurriculum(), student.getJenrlWeight(lecture, pl.variable()) + (nrStudents == null ? 0.0 : nrStudents));
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
	public double getDistance() { return iDistance; }
	public void setDistance(double distance) { iDistance = distance; }
	
	public boolean hasCurricula() { return iCurriculum2nrStudents != null; }
	public String getCurriculumText() {
		if (!hasCurricula()) return "";
		int top = 0;
		double cover = 0.0;
		double total = 0.0;
		for (CurriculumInfo i: iCurriculum2nrStudents) {
			total += i.getNrStudents();
		}
		String ret = "";
		for (CurriculumInfo i: iCurriculum2nrStudents) {
			double fraction = i.getNrStudents() / total;
			if (top < 3) {
				top++;
				cover += fraction;
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
	
	public void load(Element root) throws Exception {
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
		}
	}
	
	public void save(Element root) throws Exception {
		root.addAttribute("version", String.valueOf(sVersion));
		root.addElement("jenrl").setText(String.valueOf(iJenrl));
		root.addElement("satisfied").setText(String.valueOf(iIsSatisfied));
		root.addElement("dist").setText(String.valueOf(iIsDistance));
		root.addElement("fixed").setText(String.valueOf(iIsFixed));
		root.addElement("hard").setText(String.valueOf(iIsHard));
		root.addElement("commited").setText(String.valueOf(iIsCommited));
		root.addElement("distance").setText(String.valueOf(iDistance));
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
