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

import net.sf.cpsolver.ifs.model.Value;

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
			return variable().getCouse().penalty(iStudent);
		return variable().getCouse().penalty(iStudent, variable().getAssignment().getStudent());
	}

}
