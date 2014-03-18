/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.ui;

import org.cpsolver.coursett.model.Lecture;
import org.unitime.timetable.solver.TimetableSolver;



/**
 * @author Tomas Muller
 */
public class SolverUnassignedClassesModel extends UnassignedClassesModel {
	
	private static final long serialVersionUID = -6094708695678612559L;

	public SolverUnassignedClassesModel(TimetableSolver solver, String prefix) {
		super();
		for (Lecture lecture: solver.currentSolution().getModel().unassignedVariables(solver.currentSolution().getAssignment())) {
			String name = lecture.getName();
			if (prefix != null && !name.startsWith(prefix)) continue;
			String onClick = "showGwtDialog('Suggestions', 'suggestions.do?id="+lecture.getClassId()+"&op=Reset','900','90%');";
			String instructorName = lecture.getInstructorName();
			int nrStudents = lecture.students().size();
			String initial = "";
			if (lecture.getInitialAssignment()!=null)
				initial = lecture.getInitialAssignment().getName();
			rows().addElement(new UnassignedClassRow(onClick, name, instructorName, nrStudents, initial, lecture.getOrd()));
		}
	}
}
