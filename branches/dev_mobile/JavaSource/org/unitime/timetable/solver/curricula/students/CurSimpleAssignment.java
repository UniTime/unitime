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

import java.util.HashMap;
import java.util.Map;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.Neighbour;


/**
 * @author Tomas Muller
 */
public class CurSimpleAssignment implements Neighbour<CurVariable, CurValue> {
	private CurValue iNewValue;
	
	public CurSimpleAssignment(CurValue newValue) {
		iNewValue = newValue;
	}

	@Override
	public void assign(Assignment<CurVariable, CurValue> assignment, long iteration) {
		assignment.assign(iteration, iNewValue);
	}

	@Override
	public double value(Assignment<CurVariable, CurValue> assignment) {
		CurValue old = assignment.getValue(iNewValue.variable());
		return iNewValue.variable().getCourse().penalty(assignment, iNewValue.getStudent(), old == null ? null : old.getStudent());
	}
	
	public String toString() {
		return iNewValue.variable().getCourse().getCourseName() + " = " + iNewValue.getStudent().getStudentId();
	}

	@Override
	public Map<CurVariable, CurValue> assignments() {
		Map<CurVariable, CurValue> ret = new HashMap<CurVariable, CurValue>();
		ret.put(iNewValue.variable(), iNewValue);
		return ret;
	}
}
