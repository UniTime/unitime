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
package org.unitime.timetable.solver.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.coursett.constraint.DepartmentSpreadConstraint;
import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.TimetableModel;
import net.sf.cpsolver.ifs.solver.Solver;

/**
 * @author Tomas Muller
 */
public class DeptBalancingReport implements Serializable {
	private static final long serialVersionUID = 1L;
	private HashSet iGroups = new HashSet();
	
	public DeptBalancingReport(Solver solver) {
		TimetableModel model = (TimetableModel)solver.currentSolution().getModel();
		for (DepartmentSpreadConstraint deptSpread: model.getDepartmentSpreadConstraints()) {
			iGroups.add(new DeptBalancingGroup(solver,deptSpread));
		}
		
	}
	
	public Set getGroups() {
		return iGroups;
	}
	
	public class DeptBalancingGroup implements Serializable {
		private static final long serialVersionUID = 1L;
		private Long iDeptId = null;
		private String iDeptName = null;
		private int[][] iLimit;
		private int[][] iUsage;
		private HashSet[][] iCourses;
		
		public DeptBalancingGroup(Solver solver, DepartmentSpreadConstraint deptSpread) {
			iDeptId = deptSpread.getDepartmentId();
			iDeptName = deptSpread.getName();
			iLimit = new int[Constants.SLOTS_PER_DAY_NO_EVENINGS][Constants.NR_DAYS_WEEK];
			iUsage = new int[Constants.SLOTS_PER_DAY_NO_EVENINGS][Constants.NR_DAYS_WEEK];
			iCourses = new HashSet[Constants.SLOTS_PER_DAY_NO_EVENINGS][Constants.NR_DAYS_WEEK];
			Hashtable detailCache = new Hashtable();
			for (int i=0;i<Constants.SLOTS_PER_DAY_NO_EVENINGS;i++) {
				for (int j=0;j<Constants.NR_DAYS_WEEK;j++) {
					iLimit[i][j]=deptSpread.getMaxCourses()[i][j];
					iUsage[i][j]=deptSpread.getNrCourses()[i][j];
					iCourses[i][j]=new HashSet(deptSpread.getCourses()[i][j].size());
					for (Placement placement: deptSpread.getCourses()[i][j]) {
						Lecture lecture = (Lecture)placement.variable();
						ClassAssignmentDetails ca = (ClassAssignmentDetails)detailCache.get(lecture.getClassId());
						if (ca==null) {
							ca = new ClassAssignmentDetails(solver, lecture, false);
							detailCache.put(lecture.getClassId(),ca);
						}
						iCourses[i][j].add(ca);
					}
				}
			}
		}
		
		public Long getDepartmentId() { return iDeptId; }
		public String getDepartmentName() {  return iDeptName; }
		public int getLimit(int slot, int day) {
			if (slot<Constants.DAY_SLOTS_FIRST || slot>Constants.DAY_SLOTS_LAST) return 0;
			if (day>=Constants.NR_DAYS_WEEK) return 0;
			return iLimit[slot-Constants.DAY_SLOTS_FIRST][day];
		}
		public int getUsage(int slot, int day) {
			if (slot<Constants.DAY_SLOTS_FIRST || slot>Constants.DAY_SLOTS_LAST) return 0;
			if (day>=Constants.NR_DAYS_WEEK) return 0;
			return iUsage[slot-Constants.DAY_SLOTS_FIRST][day];
		}
		public Collection getClasses(int slot, int day) {
			if (slot<Constants.DAY_SLOTS_FIRST || slot>Constants.DAY_SLOTS_LAST) return new HashSet(0);
			if (day>=Constants.NR_DAYS_WEEK) return new HashSet(0);
			return iCourses[slot-Constants.DAY_SLOTS_FIRST][day];
		}
		public int getLimit(int slot) {
			if (slot<Constants.DAY_SLOTS_FIRST || slot>Constants.DAY_SLOTS_LAST) return 0;
			int ret = 0;
			for (int day=0;day<Constants.NR_DAYS_WEEK;day++)
				ret += iLimit[slot-Constants.DAY_SLOTS_FIRST][day];
			return ret;
		}
		public int getUsage(int slot) {
			if (slot<Constants.DAY_SLOTS_FIRST || slot>Constants.DAY_SLOTS_LAST) return 0;
			int ret = 0;
			for (int day=0;day<Constants.NR_DAYS_WEEK;day++)
				ret += iUsage[slot-Constants.DAY_SLOTS_FIRST][day];
			return ret;
		}
		public int getExcess(int slot) {
			if (slot<Constants.DAY_SLOTS_FIRST || slot>Constants.DAY_SLOTS_LAST) return 0;
			int ret = 0;
			for (int day=0;day<Constants.NR_DAYS_WEEK;day++)
				ret += Math.max(0,iUsage[slot-Constants.DAY_SLOTS_FIRST][day]-iLimit[slot-Constants.DAY_SLOTS_FIRST][day]);
			return ret;
		}
		public Collection getClasses(int slot) {
			if (slot<Constants.DAY_SLOTS_FIRST || slot>Constants.DAY_SLOTS_LAST) return new HashSet(0);
			HashSet ret = new HashSet();
			for (int day=0;day<Constants.NR_DAYS_WEEK;day++)
				ret.addAll(iCourses[slot-Constants.DAY_SLOTS_FIRST][day]);
			return ret;
		}
	}
}
