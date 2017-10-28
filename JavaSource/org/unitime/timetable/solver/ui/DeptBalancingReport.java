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
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.constraint.DepartmentSpreadConstraint;
import org.cpsolver.coursett.constraint.SpreadConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solver.Solver;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;


/**
 * @author Tomas Muller
 */
public class DeptBalancingReport implements Serializable {
	private static final long serialVersionUID = 1L;
	private HashSet iGroups = new HashSet();
	private int iFirstDaySlot, iLastDaySlot, iFirstWorkDay, iLastWorkDay;
	
	public DeptBalancingReport(Solver solver) {
		TimetableModel model = (TimetableModel)solver.currentSolution().getModel();
		iFirstDaySlot = model.getProperties().getPropertyInt("General.FirstDaySlot", Constants.DAY_SLOTS_FIRST);
        iLastDaySlot = model.getProperties().getPropertyInt("General.LastDaySlot", Constants.DAY_SLOTS_LAST);
        iFirstWorkDay = model.getProperties().getPropertyInt("General.FirstWorkDay", 0);
        iLastWorkDay = model.getProperties().getPropertyInt("General.LastWorkDay", Constants.NR_DAYS_WEEK - 1);
        if (iLastWorkDay < iFirstWorkDay) iLastWorkDay += 7;
		for (DepartmentSpreadConstraint deptSpread: model.getDepartmentSpreadConstraints()) {
			iGroups.add(new DeptBalancingGroup(solver,deptSpread));
		}
		
	}
	
	public int getFirstDaySlot() { return iFirstDaySlot; }
	public int getLastDaySlot() { return iLastDaySlot; }
	public int getFirstWorkDay() { return iFirstWorkDay; }
	public int getLastWorkDay() { return iLastWorkDay; }
	public int getSlotsPerDayNoEvening() { return iLastDaySlot - iFirstDaySlot + 1; }
	public int getNrWorkDays() { return iLastWorkDay - iFirstWorkDay + 1; }
	
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
			Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
			iDeptId = deptSpread.getDepartmentId();
			iDeptName = deptSpread.getName();
			iLimit = new int[iLastDaySlot - iFirstDaySlot + 1][iLastWorkDay - iFirstWorkDay + 1];
			iUsage = new int[iLastDaySlot - iFirstDaySlot + 1][iLastWorkDay - iFirstWorkDay + 1];
			iCourses = new HashSet[iLastDaySlot - iFirstDaySlot + 1][iLastWorkDay - iFirstWorkDay + 1];
			Hashtable detailCache = new Hashtable();
			SpreadConstraint.SpreadConstraintContext context = deptSpread.getContext(assignment);
			for (int i=0;i<iLastDaySlot - iFirstDaySlot + 1;i++) {
				for (int j=0;j<iLastWorkDay - iFirstWorkDay + 1;j++) {
					iLimit[i][j]=context.getMaxCourses(i + iFirstDaySlot, j + iFirstWorkDay);
					iUsage[i][j]=context.getCourses(i + iFirstDaySlot, j + iFirstWorkDay).size();
					iCourses[i][j]=new HashSet(context.getCourses(i + iFirstDaySlot, j + iFirstWorkDay).size());
					for (Placement placement: context.getCourses(i + iFirstDaySlot, j + iFirstWorkDay)) {
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
			if (slot<iFirstDaySlot || slot>iLastDaySlot) return 0;
			if (day<iFirstWorkDay || day>iLastWorkDay) return 0;
			return iLimit[slot-iFirstDaySlot][day-iFirstWorkDay];
		}
		public int getUsage(int slot, int day) {
			if (slot<iFirstDaySlot || slot>iLastDaySlot) return 0;
			if (day<iFirstWorkDay || day>iLastWorkDay) return 0;
			return iUsage[slot-iFirstDaySlot][day-iFirstWorkDay];
		}
		public Collection getClasses(int slot, int day) {
			if (slot<iFirstDaySlot || slot>iLastDaySlot) return new HashSet(0);
			if (day<iFirstWorkDay || day>iLastWorkDay) return new HashSet(0);
			return iCourses[slot-iFirstDaySlot][day-iFirstWorkDay];
		}
		public int getLimit(int slot) {
			if (slot<iFirstDaySlot || slot>iLastDaySlot) return 0;
			int ret = 0;
			for (int day=0;day<iLastWorkDay - iFirstWorkDay + 1;day++)
				ret += iLimit[slot-iFirstDaySlot][day];
			return ret;
		}
		public int getUsage(int slot) {
			if (slot<iFirstDaySlot || slot>iLastDaySlot) return 0;
			int ret = 0;
			for (int day=0;day<iLastWorkDay - iFirstWorkDay + 1;day++)
				ret += iUsage[slot-iFirstDaySlot][day];
			return ret;
		}
		public int getExcess(int slot) {
			if (slot<iFirstDaySlot || slot>iLastDaySlot) return 0;
			int ret = 0;
			for (int day=0;day<iLastWorkDay - iFirstWorkDay + 1;day++)
				ret += Math.max(0,iUsage[slot-iFirstDaySlot][day]-iLimit[slot-iFirstDaySlot][day]);
			return ret;
		}
		public Collection getClasses(int slot) {
			if (slot<iFirstDaySlot || slot>iLastDaySlot) return new HashSet(0);
			HashSet ret = new HashSet();
			for (int day=0;day<iLastWorkDay - iFirstWorkDay + 1;day++)
				ret.addAll(iCourses[slot-iFirstDaySlot][day]);
			return ret;
		}
	}
}
