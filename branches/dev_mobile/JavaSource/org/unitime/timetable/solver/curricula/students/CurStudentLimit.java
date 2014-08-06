/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
