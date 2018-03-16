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
package org.unitime.timetable.action;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserQualifier;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;

/**
 * @author Tomas Muller
 */
@Service("/studentScheduling")
public class StudentSchedulingAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverServerService solverServerService;
	
	@Autowired ApplicationContext applicationContext;
	
	protected boolean matchCampus(AcademicSessionInfo info, String campus) {
		if (info.hasExternalCampus() && campus.equalsIgnoreCase(info.getExternalCampus())) return true;
		return campus.equalsIgnoreCase(info.getCampus());
	}

	protected boolean matchTerm(AcademicSessionInfo info, String term) {
		if (info.hasExternalTerm() && term.equalsIgnoreCase(info.getExternalTerm())) return true;
		return term.equalsIgnoreCase(info.getTerm() + info.getYear()) || term.equalsIgnoreCase(info.getYear() + info.getTerm()) || term.equalsIgnoreCase(info.getTerm() + info.getYear() + info.getCampus());
	}

	protected boolean matchSession(AcademicSessionInfo info, String session) {
		if (info.hasExternalTerm() && info.hasExternalCampus() && session.equalsIgnoreCase(info.getExternalTerm() + info.hasExternalCampus())) return true;
		return session.equalsIgnoreCase(info.getTerm() + info.getYear() + info.getCampus()) || session.equalsIgnoreCase(info.getTerm() + info.getYear()) || session.equals(info.getSessionId().toString());
	}

	public boolean match(HttpServletRequest request, AcademicSessionInfo info) {
		String campus = request.getParameter("campus");
		if (campus != null && !matchCampus(info, campus)) return false;
		String term = request.getParameter("term");
		if (term != null && !matchTerm(info, term)) return false;
		String session = request.getParameter("session");
		if (session != null && !matchSession(info, session)) return false;
		if (campus == null && term == null && session == null)
			return info.getSessionId().equals(sessionContext.getUser().getCurrentAcademicSessionId());
		else
			return true;
	}
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String target = null;
		for (Map.Entry<String, String[]> entry: request.getParameterMap().entrySet()) {
			for (String value: entry.getValue()) {
				if (target == null) target = entry.getKey() + "=" + URLEncoder.encode(value, "UTF-8");
				else target += "&" + entry.getKey() + "=" + URLEncoder.encode(value, "UTF-8");
			}
		}
		
		// Select current role -> prefer advisor, than student in the matching academic session
		SectioningService service = (SectioningService)applicationContext.getBean("sectioning.gwt");
		if (sessionContext.isAuthenticated()) {
			UserAuthority preferredAuthority = null;
			for (AcademicSessionInfo session:  service.listAcademicSessions(true)) {
				if (match(request, session)) {
					for (UserAuthority auth: sessionContext.getUser().getAuthorities(null, new SimpleQualifier("Session", session.getSessionId()))) {
						if (preferredAuthority == null && Roles.ROLE_STUDENT.equals(auth.getRole())) {
							preferredAuthority = auth;
						} else if ((preferredAuthority == null || !preferredAuthority.hasRight(Right.StudentSchedulingAdmin)) && auth.hasRight(Right.StudentSchedulingAdvisor)) {
							preferredAuthority = auth;
						} else if (auth.hasRight(Right.StudentSchedulingAdmin)) {
							preferredAuthority = auth;
						}
					}
				}
			}
			if (preferredAuthority == null && sessionContext.getUser().getCurrentAuthority() != null) {
				for (UserAuthority auth: sessionContext.getUser().getAuthorities(null, sessionContext.getUser().getCurrentAuthority().getAcademicSession())) {
					if (preferredAuthority == null && Roles.ROLE_STUDENT.equals(auth.getRole())) {
						preferredAuthority = auth;
					} else if ((preferredAuthority == null || !preferredAuthority.hasRight(Right.StudentSchedulingAdmin)) && auth.hasRight(Right.StudentSchedulingAdvisor)) {
						preferredAuthority = auth;
					} else if (auth.hasRight(Right.StudentSchedulingAdmin)) {
						preferredAuthority = auth;
					}
				}
			}
			if (preferredAuthority != null)
				sessionContext.getUser().setCurrentAuthority(preferredAuthority);
		}
		
		
		// Admins and advisors go to the scheduling dashboard
		if (sessionContext.hasPermission(Right.SchedulingDashboard)) {
			if (!sessionContext.hasPermission(Right.StudentSchedulingAdmin)) {
				Number myStudents = (Number)CourseOfferingDAO.getInstance().getSession().createQuery(
						"select count(s) from Advisor a inner join a.students s where " +
						"a.externalUniqueId = :user and a.role.reference = :role and a.session.uniqueId = :sessionId"
						).setLong("sessionId", sessionContext.getUser().getCurrentAcademicSessionId())
						.setString("user", sessionContext.getUser().getExternalUserId())
						.setString("role", sessionContext.getUser().getCurrentAuthority().getRole()).setCacheable(true).uniqueResult();
				response.sendRedirect("gwt.jsp?page=onlinesctdash" + (target == null ? "" : "&" + target) + (myStudents.intValue() > 0 ? "#mode:%22My%20Students%22@" : ""));
			} else
				response.sendRedirect("gwt.jsp?page=onlinesctdash" + (target == null ? "" : "&" + target));
			return null;
		}
		
		// 1. Scheduling Assistant with the enrollment enabled
		try {
			for (AcademicSessionInfo session:  service.listAcademicSessions(true)) {
				if (match(request, session)) {
					OnlineSectioningServer server = solverServerService.getOnlineStudentSchedulingContainer().getSolver(session.getSessionId().toString());
					if (server == null || !server.getAcademicSession().isSectioningEnabled()) continue;
					if (Roles.ROLE_STUDENT.equals(sessionContext.getUser().getCurrentAuthority().getRole())) {
						List<? extends UserQualifier> q = sessionContext.getUser().getCurrentAuthority().getQualifiers("Student");
						if (q == null || q.isEmpty()) continue;
						Student student = StudentDAO.getInstance().get((Long)q.get(0).getQualifierId());
						if (student == null) continue;
						StudentSectioningStatus status = student.getSectioningStatus();
						if (status == null) status = student.getSession().getDefaultSectioningStatus();
						if (status != null && !status.hasOption(StudentSectioningStatus.Option.enrollment)) continue;
					}
					response.sendRedirect("gwt.jsp?page=sectioning" + (target == null ? "" : "&" + target));
					return null;
				}
			}
		} catch (SectioningException e) {}
		
		// 2. Course Requests with the registration enabled
		try {
			for (AcademicSessionInfo session:  service.listAcademicSessions(false)) {
				if (match(request, session)) {
					if (Roles.ROLE_STUDENT.equals(sessionContext.getUser().getCurrentAuthority().getRole())) {
						List<? extends UserQualifier> q = sessionContext.getUser().getCurrentAuthority().getQualifiers("Student");
						if (q == null || q.isEmpty()) continue;
						Student student = StudentDAO.getInstance().get((Long)q.get(0).getQualifierId());
						if (student == null) continue;
						StudentSectioningStatus status = student.getSectioningStatus();
						if (status == null) status = student.getSession().getDefaultSectioningStatus();
						if (status != null && !status.hasOption(StudentSectioningStatus.Option.regenabled)) continue;
					}
					response.sendRedirect("gwt.jsp?page=requests" + (target == null ? "" : "&" + target));
					return null;
				}
			}
		} catch (SectioningException e) {}
		
		// 3. Scheduling Assistant
		try {
			for (AcademicSessionInfo session:  service.listAcademicSessions(true)) {
				if (match(request, session)) {
					response.sendRedirect("gwt.jsp?page=sectioning" + (target == null ? "" : "&" + target));
					return null;
				}
			}
		} catch (SectioningException e) {}
		
		// 4. Course Requests
		try {
			for (AcademicSessionInfo session:  service.listAcademicSessions(false)) {
				if (match(request, session)) {
					response.sendRedirect("gwt.jsp?page=requests" + (target == null ? "" : "&" + target));
					return null;
				}
			}
		} catch (SectioningException e) {}
		
		// 5. Main page fallback
		response.sendRedirect("main.jsp");
		return null;
	}
}
