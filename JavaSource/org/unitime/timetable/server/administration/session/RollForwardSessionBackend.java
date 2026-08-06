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
package org.unitime.timetable.server.administration.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.command.server.GwtRpcServlet;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardSessionRequest;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardSessionResponse;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.PointInTimeDataDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.queue.QueueItem;
import org.unitime.timetable.util.queue.RollForwardQueueItem;

@GwtRpcImplements(RollForwardSessionRequest.class)
public class RollForwardSessionBackend implements GwtRpcImplementation<RollForwardSessionRequest, RollForwardSessionResponse>{
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	SolverServerService solverServerService;

	@Override
	public RollForwardSessionResponse execute(RollForwardSessionRequest request, SessionContext context) {
		context.checkPermission(Right.SessionRollForward);
		switch (request.getOperation()) {
		case LOAD:
			Long sessionId = request.getData().getSessionToRollForwardTo();
			List<Session> allSessions = new ArrayList<Session>(Session.getAllSessions());
			if (sessionId == null)
				for (Session session: allSessions) {
					if (session.getStatusType().isAllowRollForward())
						sessionId = session.getUniqueId();
				}
			Session currentSession = SessionDAO.getInstance().get(sessionId == null ? context.getUser().getCurrentAcademicSessionId() : sessionId);
			Collections.sort(allSessions, new SessionComparator(currentSession.getAcademicInitiative()));
			
			RollForwardSessionResponse response = new RollForwardSessionResponse();
			for (Session session: allSessions) {
				if (session.getStatusType().isAllowRollForward()) {
					response.addToSession(session.getUniqueId(), session.getLabel());
					if (sessionId == null)
						sessionId = session.getUniqueId();
				} else {
					response.addFromSession(session.getUniqueId(), session.getLabel());
				}
			}
			if (sessionId != null) {
				for (SubjectArea subject: SubjectArea.getAllSubjectAreas(sessionId))
					response.addSubject(subject.getUniqueId(), subject.getLabel());
				for (Department dept: Department.findAll(sessionId))
					response.addDepartment(dept.getUniqueId(), dept.getLabel());
				for (PointInTimeData pit: PointInTimeDataDAO.getInstance().getSession().createQuery(
						"from PointInTimeData pitd where pitd.session.uniqueId in (select distinct rfio.session.uniqueId " +
						" from InstructionalOffering rfio, Session s inner join s.instructionalOfferings as io " +
						" where s.uniqueId = :sessId " +
						" and rfio.uniqueId = io.uniqueIdRolledForwardFrom )" +
						" and pitd.savedSuccessfully = true ", PointInTimeData.class)
						.setParameter("sessId", sessionId) .list())
					response.addPointInTime(pit.getUniqueId(), pit.getName());
				
				response.setDates(GwtRpcServlet.execute(new ReservationInterface.ReservationDefaultExpirationDatesRpcRequest(sessionId), applicationContext, context));
				response.setToSessionId(sessionId);
			}
			response.setAllowClassPrefs(ApplicationProperty.RollForwardAllowClassPreferences.isTrue());
			response.setParentCourses(ApplicationProperty.StudentSchedulingParentCourse.isTrue());

			return response;
		case EXECUTE:
			RollForwardSessionInterface form = request.getData();
			Session session = SessionDAO.getInstance().get(form.getSessionToRollForwardTo());
			QueueItem queue = solverServerService.getQueueProcessor().add(new RollForwardQueueItem(session, context.getUser(), form));
			response = new RollForwardSessionResponse();
			response.setQueueId(queue.getId());
			return response;
		case POPULATE:
			QueueItem item = solverServerService.getQueueProcessor().get(request.getQueueId());
			response = new RollForwardSessionResponse();
			if (item != null && item instanceof RollForwardQueueItem) {
				RollForwardQueueItem q = (RollForwardQueueItem)item;
				response.setData(q.getForm());
				response.setErrors(q.getErrors());
			}
			return response;
		}
		return null;
	}


	public static class SessionComparator implements Comparator<Session> {
		private String iPreferCampus = null;
		public SessionComparator(String currentCampus) {
			iPreferCampus = currentCampus;
		}
		@Override
		public int compare(Session s1, Session s2) {
			boolean c1 = s1.getAcademicInitiative().equals(iPreferCampus);
			boolean c2 = s2.getAcademicInitiative().equals(iPreferCampus);
			if (c1 != c2)
				return (c1 ? -1 : 1);
			int cmp = s1.getAcademicInitiative().compareTo(s2.getAcademicInitiative());
			if (cmp!=0) return cmp;
			
			cmp = s2.getSessionBeginDateTime().compareTo(s1.getSessionBeginDateTime());
			if (cmp!=0) return cmp;
			
			return s1.getUniqueId().compareTo(s2.getUniqueId());
		}
	}
}
