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

import org.cpsolver.coursett.constraint.DepartmentSpreadConstraint;
import org.cpsolver.coursett.constraint.SpreadConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solver.Solver;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.util.Constants;


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
			Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
			iDeptId = deptSpread.getDepartmentId();
			iDeptName = deptSpread.getName();
			iLimit = new int[Constants.SLOTS_PER_DAY_NO_EVENINGS][Constants.NR_DAYS_WEEK];
			iUsage = new int[Constants.SLOTS_PER_DAY_NO_EVENINGS][Constants.NR_DAYS_WEEK];
			iCourses = new HashSet[Constants.SLOTS_PER_DAY_NO_EVENINGS][Constants.NR_DAYS_WEEK];
			Hashtable detailCache = new Hashtable();
			SpreadConstraint.SpreadConstraintContext context = deptSpread.getContext(assignment);
			for (int i=0;i<Constants.SLOTS_PER_DAY_NO_EVENINGS;i++) {
				for (int j=0;j<Constants.NR_DAYS_WEEK;j++) {
					iLimit[i][j]=context.getMaxCourses(i + Constants.DAY_SLOTS_FIRST, j);
					iUsage[i][j]=context.getCourses(i + Constants.DAY_SLOTS_FIRST, j).size();
					iCourses[i][j]=new HashSet(context.getCourses(i + Constants.DAY_SLOTS_FIRST, j).size());
					for (Placement placement: context.getCourses(i + Constants.DAY_SLOTS_FIRST, j)) {
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
