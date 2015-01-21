/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.onlinesectioning.test;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningTestFwk;
import org.unitime.timetable.onlinesectioning.basic.GetRequest;
import org.unitime.timetable.onlinesectioning.match.AnyCourseMatcher;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction;
import org.unitime.timetable.onlinesectioning.updates.ClassAssignmentChanged;
import org.unitime.timetable.onlinesectioning.updates.ReloadStudent;

/**
 * @author Tomas Muller
 */
public class ReloadTest extends OnlineSectioningTestFwk {

	@Override
	public List<Operation> operations() {
		org.hibernate.Session hibSession = new _RootDAO().getSession();
		
		List<Operation> loadRequests = new ArrayList<Operation>();
		
		for (final Long studentId: (List<Long>)hibSession.createQuery(
				"select s.uniqueId from Student s where s.session.uniqueId = :sessionId")
				.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
			loadRequests.add(new Operation() {
				@Override
				public double execute(OnlineSectioningServer s) {
					sLog.info("Reloading " + studentId + " ...");
					s.execute(s.createAction(ReloadStudent.class).forStudents(studentId), user());
					sLog.info("  -- " + studentId + " reloaded");
					return 1.0;
				}
			});
		}
		
		for (final Long offeringId: (List<Long>)hibSession.createQuery(
				"select io.uniqueId from InstructionalOffering io where io.session.uniqueId = :sessionId and io.notOffered = false")
				.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
			loadRequests.add(new Operation() {
				@Override
				public double execute(OnlineSectioningServer s) {
					sLog.info("Locking " + offeringId + " ...");
					s.lockOffering(offeringId);
					sLog.info("  -- " + offeringId + " locked");
					long delay = Math.round(Math.random() * 5000);
					sLog.info("Sleeping for " + delay + " ms  ...");
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {}
					sLog.info("Unlocking " + offeringId + " ...");
					s.unlockOffering(offeringId);
					sLog.info("  -- " + offeringId + " unlocked");
					return 1.0;
				}
			});
		}

		for (final String name: (List<String>)hibSession.createQuery(
				"select co.subjectAreaAbbv || ' ' || co.courseNbr from CourseOffering co where co.instructionalOffering.session.uniqueId = :sessionId and co.instructionalOffering.notOffered = false")
				.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
			loadRequests.add(new Operation() {
				@Override
				public double execute(OnlineSectioningServer s) {
					for (int i = 1; i < name.length(); i++) {
						sLog.info("Looking for " + name.substring(0, i) + " ...");
						s.findCourses(name.substring(0, i), 20, new AnyCourseMatcher());
					}
					return 1.0;
				}
			});
		}

		for (final Long classId: (List<Long>)hibSession.createQuery(
				"select c.uniqueId from Class_ c where c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
				.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
			loadRequests.add(new Operation() {
				@Override
				public double execute(OnlineSectioningServer s) {
					sLog.info("Assignment changed for " + classId + " ...");
					s.execute(s.createAction(ClassAssignmentChanged.class).forClasses(classId), user());
					return 1.0;
				}
			});
		}
		
		for (final Long studentId: (List<Long>)hibSession.createQuery("select s.uniqueId from Student s where s.session.uniqueId = :sessionId")
				.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
			
			CourseRequestInterface request = getServer().execute(createAction(GetRequest.class).forStudent(studentId), user());
			if (request == null || request.getCourses().isEmpty()) continue;
			
			loadRequests.add(new Operation() {
				@Override
				public double execute(OnlineSectioningServer s) {
					CourseRequestInterface request = s.execute(createAction(GetRequest.class).forStudent(studentId), user());
					if (request != null && !request.getCourses().isEmpty()) {
						sLog.info("Find assignments for " + studentId + " ...");
						FindAssignmentAction action = s.createAction(FindAssignmentAction.class).forRequest(request).withAssignment(new ArrayList<ClassAssignmentInterface.ClassAssignment>()); 
						List<ClassAssignmentInterface> ret = s.execute(action, user());
						return ret == null || ret.isEmpty() ? 0.0 : ret.get(0).getValue();
					} else {
						return 1.0;
					}
				}
			});
		}
		hibSession.close();
		
		return loadRequests;
	}
	
	public static void main(String args[]) {
		new ReloadTest().test(-1, 20);
	}

}