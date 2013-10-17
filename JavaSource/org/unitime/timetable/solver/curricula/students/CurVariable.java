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

import net.sf.cpsolver.ifs.model.Variable;

/**
 * @author Tomas Muller
 */
public class CurVariable extends Variable<CurVariable, CurValue> {
	private CurCourse iCourse;

	public CurVariable(CurModel model, CurCourse course, int first, int size) {
		super();
		iCourse = course;
		List<CurValue> values = new ArrayList<CurValue>();
		for (int i = 0; i < size; i++)
			values.add(new CurValue(this, model.getStudents().get(first + i)));
		setValues(values);
	}
	
	public CurCourse getCourse() { return iCourse; }
	
	public String toString() {
		return getCourse().getCourseName();
	}
}
