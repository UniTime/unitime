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
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserQualifier;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

/**
 * @author Tomas Muller
 */
@Action(value = "studentScheduling", results = {
		@Result(name = "main", type = "redirect", location = "/main.action")
	})
public class StudentSchedulingAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = -287721682089077684L;
	
	private String campus, term, session, prefer;
	
	public String getCampus() { return campus; }
	public void setCampus(String campus) { this.campus = campus; }
	public String getTerm() { return term; }
	public void setTerm(String term) { this.term = term; }
	public String getSession() { return session; }
	public void setSession(String session) { this.session = session; }
	public String getPrefer() { return prefer; }
	public void setPrefer(String prefer) { this.prefer = prefer; }

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

	public boolean match(HttpServletRequest request, AcademicSessionInfo info, boolean useDefault) {
		if (campus != null && !matchCampus(info, campus)) return false;
		if (term != null && !matchTerm(info, term)) return false;
		if (session != null && !matchSession(info, session)) return false;
		if (useDefault && campus == null && term == null && session == null)
			return info.getSessionId().equals(sessionContext.getUser().getCurrentAcademicSessionId());
		else
			return true;
	}
	
	@Override
	public String execute() throws Exception {
		String target = null;
		for (Map.Entry<String, String[]> entry: request.getParameterMap().entrySet()) {
			for (String value: entry.getValue()) {
				if ("prefer".equals(entry.getKey())) continue;
				if (target == null) target = entry.getKey() + "=" + URLEncoder.encode(value, "UTF-8");
				else target += "&" + entry.getKey() + "=" + URLEncoder.encode(value, "UTF-8");
			}
		}
		
		boolean useDefault = ApplicationProperty.StudentSchedulingUseDefaultSession.isTrue();
		
		// if instructor role is assigned, prefer student role
		if (sessionContext.isAuthenticated() && Roles.ROLE_INSTRUCTOR.equals(sessionContext.getUser().getCurrentAuthority().getRole())) {
			// Student role of the same session
			for (UserAuthority auth: sessionContext.getUser().getAuthorities(Roles.ROLE_STUDENT, new SimpleQualifier("Session", sessionContext.getUser().getCurrentAcademicSessionId()))) {
				sessionContext.getUser().setCurrentAuthority(auth);
				break;
			}
			// Student role of different sessions
			if (Roles.ROLE_INSTRUCTOR.equals(sessionContext.getUser().getCurrentAuthority().getRole())) {
				TreeSet<Session> sessions = new TreeSet<Session>();
				UserAuthority firstStudentAuth = null;
				for (UserAuthority auth: sessionContext.getUser().getAuthorities(Roles.ROLE_STUDENT)) {
					Session session = SessionDAO.getInstance().get((Long)auth.getAcademicSession().getQualifierId());
					if (session != null) sessions.add(session);
					if (firstStudentAuth == null) firstStudentAuth = auth;
				}
				if (!sessions.isEmpty()) {
					Session session = UniTimeUserContext.defaultSession(sessions, firstStudentAuth, UserProperty.PrimaryCampus.get(sessionContext.getUser()));
					if (session != null)
						for (UserAuthority auth: sessionContext.getUser().getAuthorities(Roles.ROLE_STUDENT, new SimpleQualifier("Session", session.getUniqueId()))) {
							sessionContext.getUser().setCurrentAuthority(auth);
							break;
						}
				}
			}
		}
		
		// Select current role -> prefer advisor, than student in the matching academic session
		SectioningService service = (SectioningService)SpringApplicationContextHolder.getBean("sectioning.gwt");
		if (sessionContext.isAuthenticated()) {
			UserAuthority preferredAuthority = null;
			try {
				for (AcademicSessionInfo session:  service.listAcademicSessions(true)) {
					if (match(request, session, useDefault)) {
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
				// no authority selected --> also check the session for which the course requests are enabled
				if (preferredAuthority == null)
					for (AcademicSessionInfo session:  service.listAcademicSessions(false)) {
						if (match(request, session, useDefault)) {
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
			} catch (SectioningException e) {}
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
				Number myStudents = CourseOfferingDAO.getInstance().getSession().createQuery(
						"select count(s) from Advisor a inner join a.students s where " +
						"a.externalUniqueId = :user and a.role.reference = :role and a.session.uniqueId = :sessionId", Number.class
						).setParameter("sessionId", sessionContext.getUser().getCurrentAcademicSessionId())
						.setParameter("user", sessionContext.getUser().getExternalUserId())
						.setParameter("role", sessionContext.getUser().getCurrentAuthority().getRole()).setCacheable(true).uniqueResult();
				response.sendRedirect("onlinesctdash" + (target == null ? "" : "?" + target) + (myStudents.intValue() > 0 ? "#mode:%22My%20Students%22@" : ""));
			} else
				response.sendRedirect("onlinesctdash" + (target == null ? "" : "?" + target));
			return null;
		}
		
		// Only for students (check status)
		if (Roles.ROLE_STUDENT.equals(sessionContext.getUser().getCurrentAuthority().getRole())) {
			List<? extends UserQualifier> q = sessionContext.getUser().getCurrentAuthority().getQualifiers("Student");
			if (q != null && !q.isEmpty()) {
				UserQualifier studentQualifier = q.get(0);
				boolean preferCourseRequests = ApplicationProperty.StudentSchedulingPreferCourseRequests.isTrue();
				if (prefer != null)
					preferCourseRequests = "cr".equalsIgnoreCase(prefer) || "crf".equalsIgnoreCase(prefer);
				if (preferCourseRequests) {
					// 1. Course Requests with the registration enabled
					try {
						for (AcademicSessionInfo session:  service.listAcademicSessions(false)) {
							if (match(request, session, useDefault)) {
								Student student = Student.findByExternalId(session.getSessionId(), studentQualifier.getQualifierReference());
								if (student == null)
									student = StudentDAO.getInstance().get((Long)studentQualifier.getQualifierId());
								if (student == null) continue;
								StudentSectioningStatus status = student.getEffectiveStatus();
								if (status == null || !status.hasOption(StudentSectioningStatus.Option.regenabled)) continue;
								response.sendRedirect("requests" + (target == null ? "" : "?" + target));
								return null;
							}
						}
					} catch (SectioningException e) {}
					// 2. Scheduling Assistant with the enrollment enabled
					try {
						for (AcademicSessionInfo session:  service.listAcademicSessions(true)) {
							if (match(request, session, useDefault)) {
								OnlineSectioningServer server = getSolverServerService().getOnlineStudentSchedulingContainer().getSolver(session.getSessionId().toString());
								if (server == null || !server.getAcademicSession().isSectioningEnabled()) continue;
								Student student = Student.findByExternalId(session.getSessionId(), studentQualifier.getQualifierReference());
								if (student == null)
									student = StudentDAO.getInstance().get((Long)studentQualifier.getQualifierId());
								if (student == null) continue;
								StudentSectioningStatus status = student.getEffectiveStatus();
								if (status != null && !status.hasOption(StudentSectioningStatus.Option.enrollment)) continue;
								response.sendRedirect("sectioning" + (target == null ? "" : "?" + target));
								return null;
							}
						}
					} catch (SectioningException e) {}
				} else {
					// 1. Scheduling Assistant with the enrollment enabled
					try {
						for (AcademicSessionInfo session:  service.listAcademicSessions(true)) {
							if (match(request, session, useDefault)) {
								OnlineSectioningServer server = getSolverServerService().getOnlineStudentSchedulingContainer().getSolver(session.getSessionId().toString());
								if (server == null || !server.getAcademicSession().isSectioningEnabled()) continue;
								Student student = Student.findByExternalId(session.getSessionId(), studentQualifier.getQualifierReference());
								if (student == null)
									student = StudentDAO.getInstance().get((Long)studentQualifier.getQualifierId());
								if (student == null) continue;
								StudentSectioningStatus status = student.getEffectiveStatus();
								if (status != null && !status.hasOption(StudentSectioningStatus.Option.enrollment)) continue;
								response.sendRedirect("sectioning" + (target == null ? "" : "?" + target));
								return null;
							}
						}
					} catch (SectioningException e) {}
					// 2. Course Requests with the registration enabled
					try {
						for (AcademicSessionInfo session:  service.listAcademicSessions(false)) {
							if (match(request, session, useDefault)) {
								Student student = Student.findByExternalId(session.getSessionId(), studentQualifier.getQualifierReference());
								if (student == null)
									student = StudentDAO.getInstance().get((Long)studentQualifier.getQualifierId());
								if (student == null) continue;
								StudentSectioningStatus status = student.getEffectiveStatus();
								if (status == null || !status.hasOption(StudentSectioningStatus.Option.regenabled)) continue;
								response.sendRedirect("requests" + (target == null ? "" : "?" + target));
								return null;
							}
						}
					} catch (SectioningException e) {}					
				}
			}
		}
		
		// 3. Scheduling Assistant
		try {
			for (AcademicSessionInfo session:  service.listAcademicSessions(true)) {
				if (match(request, session, useDefault)) {
					response.sendRedirect("sectioning" + (target == null ? "" : "?" + target));
					return null;
				}
			}
		} catch (SectioningException e) {}
		
		// 4. Course Requests
		try {
			for (AcademicSessionInfo session:  service.listAcademicSessions(false)) {
				if (match(request, session, useDefault)) {
					response.sendRedirect("requests" + (target == null ? "" : "?" + target));
					return null;
				}
			}
		} catch (SectioningException e) {}
		
		// 5. Main page fallback
		return "main";
	}
}
