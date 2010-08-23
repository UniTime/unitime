/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.curricula.students;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.cpsolver.ifs.model.GlobalConstraint;
import net.sf.cpsolver.ifs.util.ToolBox;

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
	public void computeConflicts(CurValue value, Set<CurValue> conflicts) {
		int courses = value.getStudent().getCourses().size();
		if (!value.getStudent().getCourses().contains(value.variable().getCouse())) courses++;
		for (CurValue conflict: conflicts) {
			if (conflict.getStudent().equals(value.getStudent()) && value.getStudent().getCourses().contains(conflict.variable().getCouse()))
				courses--;
		}
		if (courses > iMaxLimit) {
			List<CurValue> adepts = new ArrayList<CurValue>();
			for (CurCourse course: value.getStudent().getCourses()) {
				if (course.equals(value.variable().getCouse())) continue;
				CurValue adept = course.getValue(value.getStudent());
				if (conflicts.contains(adept)) continue;
				adepts.add(adept);
			}
			conflicts.add(ToolBox.random(adepts));
		}
	}

}
