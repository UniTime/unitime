package org.unitime.timetable.onlinesectioning.test;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningTestFwk;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction;
import org.unitime.timetable.onlinesectioning.updates.EnrollStudent;

public class OnlineSectioningTest extends OnlineSectioningTestFwk {

	public List<Operation> operations() {

		org.hibernate.Session hibSession = new _RootDAO().getSession();
		
		List<Operation> operations = new ArrayList<Operation>();
		
		for (final Long studentId: (List<Long>)hibSession.createQuery(
				"select s.uniqueId from Student s where s.session.uniqueId = :sessionId")
				.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
			
			CourseRequestInterface request = getServer().getRequest(studentId);
			if (request == null || request.getCourses().isEmpty()) continue;
			
			operations.add(new Operation() {
				@Override
				public double execute(OnlineSectioningServer s) {
					CourseRequestInterface request = s.getRequest(studentId);
					if (request != null && !request.getCourses().isEmpty()) {
						FindAssignmentAction action = new FindAssignmentAction(request, new ArrayList<ClassAssignmentInterface.ClassAssignment>()); 
						List<ClassAssignmentInterface> assignments = s.execute(action);
						if (assignments != null && !assignments.isEmpty()) {
							List<ClassAssignmentInterface.ClassAssignment> assignment = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
							for (ClassAssignmentInterface.CourseAssignment course: assignments.get(0).getCourseAssignments())
								assignment.addAll(course.getClassAssignments());
							s.execute(new EnrollStudent(studentId, request, assignment));
						}
						return action.value();
					} else {
						return 1.0;
					}
				}
			});
		}
		
		hibSession.close();

		return operations;
	}
	
	public static void main(String args[]) {
		new OnlineSectioningTest().test(-1, 100);
	}
}
