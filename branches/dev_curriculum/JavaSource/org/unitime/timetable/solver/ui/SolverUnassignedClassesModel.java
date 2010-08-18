/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.ui;

import org.unitime.timetable.solver.TimetableSolver;

import net.sf.cpsolver.coursett.model.Lecture;


/**
 * @author Tomas Muller
 */
public class SolverUnassignedClassesModel extends UnassignedClassesModel {
	
	private static final long serialVersionUID = -6094708695678612559L;

	public SolverUnassignedClassesModel(TimetableSolver solver) {
		super();
		for (Lecture lecture: solver.currentSolution().getModel().unassignedVariables()) {
			String name = lecture.getName();
			String onClick = "window.open('suggestions.do?id="+lecture.getClassId()+"&op=Reset','suggestions','width=1024,height=768,resizable=yes,scrollbars=yes,toolbar=no,location=no,directories=no,status=yes,menubar=no,copyhistory=no').focus();";
			String instructorName = lecture.getInstructorName();
			int nrStudents = lecture.students().size();
			String initial = "";
			if (lecture.getInitialAssignment()!=null)
				initial = lecture.getInitialAssignment().getName();
			rows().addElement(new UnassignedClassRow(onClick, name, instructorName, nrStudents, initial, lecture.getOrd()));
		}
	}
}
