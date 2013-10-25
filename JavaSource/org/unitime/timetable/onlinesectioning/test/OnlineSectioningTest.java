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
import java.util.Collections;
import java.util.List;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningTestFwk;
import org.unitime.timetable.onlinesectioning.basic.GetRequest;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction;
import org.unitime.timetable.onlinesectioning.updates.EnrollStudent;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningTest extends OnlineSectioningTestFwk {

	public List<Operation> operations() {
		
		getServer().getAcademicSession().setSectioningEnabled(true);

		org.hibernate.Session hibSession = new _RootDAO().getSession();
		
		List<Operation> operations = new ArrayList<Operation>();
		
		for (final Long studentId: (List<Long>)hibSession.createQuery(
				"select s.uniqueId from Student s where s.session.uniqueId = :sessionId")
				.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
			
			CourseRequestInterface request = getServer().execute(new GetRequest(studentId), user());
			if (request == null || request.getCourses().isEmpty()) continue;
			
			operations.add(new Operation() {
				@Override
				public double execute(OnlineSectioningServer s) {
					CourseRequestInterface request = s.execute(new GetRequest(studentId), user());
					if (request != null && !request.getCourses().isEmpty()) {
						FindAssignmentAction action = null;
						List<ClassAssignmentInterface> assignments = null;
						for (int i = 1; i <= 5; i++) {
							try {
								action = new FindAssignmentAction(request, new ArrayList<ClassAssignmentInterface.ClassAssignment>()); 
								assignments = s.execute(action, user());
								if (assignments != null && !assignments.isEmpty()) {
									List<ClassAssignmentInterface.ClassAssignment> assignment = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
									for (ClassAssignmentInterface.CourseAssignment course: assignments.get(0).getCourseAssignments())
										assignment.addAll(course.getClassAssignments());
									s.execute(new EnrollStudent(studentId, request, assignment), user());
								}
								break;
							} catch (SectioningException e) {
								if (e.getMessage().contains("the class is no longer available")) {
									sLog.warn("Enrollment failed: " +e.getMessage() + " become unavailable (" + i + ". attempt)");
									continue;
								}
							}
						}
						return (assignments == null || assignments.isEmpty() ? 0.0 : assignments.get(0).getValue());
					} else {
						return 1.0;
					}
				}
			});
		}
		
		hibSession.close();
		
		Collections.shuffle(operations);

		return operations;
	}
	
	public static void main(String args[]) {
		new OnlineSectioningTest().test(-1, Integer.valueOf(System.getProperty("nrConcurrent", "10")));
	}
}
