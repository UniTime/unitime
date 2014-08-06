/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.test;

import java.util.ArrayList;
import java.util.List;


import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningTestFwk;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment;
import org.unitime.timetable.onlinesectioning.basic.GetRequest;
import org.unitime.timetable.onlinesectioning.solver.ComputeSuggestionsAction;

/**
 * @author Tomas Muller
 */
public class SuggestionsTest extends OnlineSectioningTestFwk {

	public List<Operation> operations() {
		org.hibernate.Session hibSession = new _RootDAO().getSession();
		
		List<Operation> operations = new ArrayList<Operation>();
		
		for (final Long studentId: (List<Long>)hibSession.createQuery(
				"select s.uniqueId from Student s where s.session.uniqueId = :sessionId")
				.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
			
			CourseRequestInterface request = getServer().execute(createAction(GetRequest.class).forStudent(studentId), user());
			if (request == null || request.getCourses().isEmpty()) continue;
			ClassAssignmentInterface assignment = getServer().execute(createAction(GetAssignment.class).forStudent(studentId), user());
			if (assignment == null) continue;
						
			operations.add(new Operation() {
				@Override
				public double execute(OnlineSectioningServer s) {
					CourseRequestInterface request = s.execute(createAction(GetRequest.class).forStudent(studentId), user());
					if (request == null || request.getCourses().isEmpty()) return 1.0;
					ClassAssignmentInterface assignment = getServer().execute(createAction(GetAssignment.class).forStudent(studentId), user());
					if (assignment == null) return 1.0;
					List<ClassAssignmentInterface.ClassAssignment> classes = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
					for (ClassAssignmentInterface.CourseAssignment course: assignment.getCourseAssignments())
						classes.addAll(course.getClassAssignments());
					if (classes.isEmpty()) return 1.0;
					ComputeSuggestionsAction action = createAction(ComputeSuggestionsAction.class).forRequest(request).withAssignment(classes).withSelection(ToolBox.random(classes));
					s.execute(action, user());
					return action.value();
				}
			});
		}
		
		hibSession.close();

		return operations;
	}
	
	public static void main(String args[]) {
		new SuggestionsTest().test(5000, 1, 2, 5, 10, 20, 50, 100, 250, 1000);
	}

}
