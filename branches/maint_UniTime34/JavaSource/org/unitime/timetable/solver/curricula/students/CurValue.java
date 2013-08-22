/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import net.sf.cpsolver.ifs.model.Value;

/**
 * @author Tomas Muller
 */
public class CurValue extends Value<CurVariable, CurValue> {
	private CurStudent iStudent;
	
	public CurValue(CurVariable course, CurStudent student) {
		super(course);
		iStudent = student;
	}
	
	public CurStudent getStudent() {
		return iStudent;
	}
	
	public double toDouble() {
		if (variable().getAssignment() == null)
			return variable().getCourse().penalty(iStudent);
		return variable().getCourse().penalty(iStudent, variable().getAssignment().getStudent());
	}

}
