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

import net.sf.cpsolver.ifs.heuristics.NeighbourSelection;
import net.sf.cpsolver.ifs.model.Neighbour;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;

public class CurStudentSwap implements NeighbourSelection<CurVariable, CurValue>{

	protected CurStudentSwap(DataProperties	config) {
	}

	@Override
	public void init(Solver<CurVariable, CurValue> solver) {
	}

	@Override
	public Neighbour<CurVariable, CurValue> selectNeighbour(
			Solution<CurVariable, CurValue> solution) {
		CurModel model = (CurModel)solution.getModel();
		CurCourse course = ToolBox.random(model.getSwapCourses());
		if (course == null) return null;
		CurStudent student = null;
		CurValue oldValue = null, newValue = null;
		int idx = ToolBox.random(model.getStudents().size());
		for (int i = 0; i < model.getStudents().size(); i++) {
			student = model.getStudents().get((i + idx) % model.getStudents().size());
			oldValue = course.getValue(student);
			if (oldValue == null && student.getCourses().size() < model.getStudentLimit().getMaxLimit()) break;
			if (oldValue != null && student.getCourses().size() > model.getStudentLimit().getMinLimit()) break;
		}
		if (oldValue == null && student.getCourses().size() >= model.getStudentLimit().getMaxLimit()) return null;
		if (oldValue != null && student.getCourses().size() <= model.getStudentLimit().getMinLimit()) return null;
		
		idx = ToolBox.random(model.getStudents().size());
		for (int i = 0; i < model.getStudents().size(); i++) {
			CurStudent newStudent = model.getStudents().get((i + idx) % model.getStudents().size());
			if (oldValue != null) {
				if (course.getStudents().contains(newStudent)) continue;
				if (newStudent.getCourses().size() >= model.getStudentLimit().getMaxLimit()) continue;
				newValue = new CurValue(oldValue.variable(), newStudent);
				break;
			} else {
				if (newStudent.getCourses().size() <= model.getStudentLimit().getMinLimit()) continue;
				oldValue = course.getValue(newStudent);
				if (oldValue != null) {
					newValue = new CurValue(oldValue.variable(), student);
					break;
				}
			}
		}
		return (newValue == null ? null : new CurSimpleAssignment(newValue));
	}
}
