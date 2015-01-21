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
package org.unitime.timetable.solver.curricula.students;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.GlobalConstraint;
import org.cpsolver.ifs.util.ToolBox;


/**
 * @author Tomas Muller
 */
public class CurStudentLimit extends GlobalConstraint<CurVariable, CurValue> {
	private int iMinLimit = 0, iMaxLimit = 0;

	public CurStudentLimit(int minLimit, int maxLimit) {
		iMinLimit = minLimit;
		iMaxLimit = maxLimit;
	}
	
	public int getMinLimit() {
		return iMinLimit;
	}

	public int getMaxLimit() {
		return iMaxLimit;
	}

	@Override
	public void computeConflicts(Assignment<CurVariable, CurValue> assignment, CurValue value, Set<CurValue> conflicts) {
		Set<CurCourse> courses = value.getStudent().getCourses(assignment);
		int nrCourses = courses.size();
		if (!courses.contains(value.variable().getCourse())) nrCourses++;
		for (CurValue conflict: conflicts) {
			if (conflict.getStudent().equals(value.getStudent()) && courses.contains(conflict.variable().getCourse()))
				nrCourses--;
		}
		if (nrCourses > iMaxLimit) {
			List<CurValue> adepts = new ArrayList<CurValue>();
			for (CurCourse course: courses) {
				if (course.equals(value.variable().getCourse())) continue;
				CurValue adept = course.getValue(assignment, value.getStudent());
				if (conflicts.contains(adept)) continue;
				adepts.add(adept);
			}
			conflicts.add(ToolBox.random(adepts));
			nrCourses --;
		}
	}
	
	public String toString() {
		return "StudentLimit<" + getMinLimit() + "," + getMaxLimit() + ">";
	}

}
