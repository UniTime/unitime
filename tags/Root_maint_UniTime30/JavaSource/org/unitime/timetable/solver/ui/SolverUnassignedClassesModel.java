/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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

import java.util.Enumeration;

import org.unitime.timetable.solver.TimetableSolver;

import net.sf.cpsolver.coursett.model.Lecture;


/**
 * @author Tomas Muller
 */
public class SolverUnassignedClassesModel extends UnassignedClassesModel {
	
	public SolverUnassignedClassesModel(TimetableSolver solver) {
		super();
		for (Enumeration e=solver.currentSolution().getModel().unassignedVariables().elements();e.hasMoreElements();) {
			Lecture lecture = (Lecture)e.nextElement();
			String name = lecture.getName();
			String onClick = "window.open('suggestions.do?id="+lecture.getClassId()+"&op=Reset','suggestions','width=1000,height=600,resizable=yes,scrollbars=yes,toolbar=no,location=no,directories=no,status=yes,menubar=no,copyhistory=no');";
			String instructorName = lecture.getInstructorName();
			int nrStudents = lecture.students().size();
			String initial = "";
			if (lecture.getInitialAssignment()!=null)
				initial = lecture.getInitialAssignment().getName();
			rows().addElement(new UnassignedClassRow(onClick, name, instructorName, nrStudents, initial, lecture.getOrd()));
		}
	}
}
