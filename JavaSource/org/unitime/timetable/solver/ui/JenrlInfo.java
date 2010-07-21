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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

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
			if (isDistance())
				setDistance(Placement.getDistanceInMeters(((TimetableModel)jc.getModel()).getDistanceMetric(),firstPl,secondPl));
		}
	}
	
	public ClassAssignmentDetails getFirst() { return iFirst; }
	public ClassAssignmentDetails getSecond() { return iSecond; }
	
	public static Hashtable getCommitedJenrlInfos(Lecture lecture) {
		return getCommitedJenrlInfos(null, lecture);
	}
	
	public static Hashtable getCommitedJenrlInfos(Solver solver, Lecture lecture) {
		Hashtable ret = new Hashtable();
		Placement placement = (Placement)lecture.getAssignment();
		if (placement==null) return ret;
		for (Iterator i2=lecture.students().iterator();i2.hasNext();) {
			Student student = (Student)i2.next();
			Set conflicts = student.conflictPlacements(placement);
			if (conflicts==null) continue;
			for (Iterator i3=conflicts.iterator();i3.hasNext();) {
				Placement pl = (Placement)i3.next();
				JenrlInfo info = (JenrlInfo)ret.get(pl.getAssignmentId());
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
				info.setJenrl(info.getJenrl()+1.0);
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
}
