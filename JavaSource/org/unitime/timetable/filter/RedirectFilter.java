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
package org.unitime.timetable.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.SecurityMessages;
import org.unitime.timetable.StartupService;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.UserQualifier;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.spring.SpringApplicationContextHolder;
import org.unitime.timetable.util.AccessDeniedException;
import org.unitime.timetable.webutil.BackTracker;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RedirectFilter implements Filter {
	protected static SecurityMessages MSG = Localization.create(SecurityMessages.class);

	@Override
	public void init(FilterConfig config) throws ServletException {
	}


	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			HttpServletRequest req = (HttpServletRequest)request;
			HttpServletResponse res = (HttpServletResponse)response;
			String uri = req.getRequestURI().substring(req.getContextPath().length() + 1);
			if ("selectPrimaryRole".equals(uri)) {
				if (selectPrimaryRole(req, res)) return;
			} else if ("studentScheduling".equals(uri)) {
				if (studentScheduling(req, res)) return;
			} else if ("back".equals(uri)) {
		        if (back(req, res)) return;
			} else if ("login".equals(uri)) {
		        if (login(req, res)) return;
			} else if ("logout".equals(uri)) {
		        if (logout(req, res)) return;
			} else if ("loginRequired".equals(uri)) {
		        if (loginRequired(req, res)) return;
			} else if ("main".equals(uri)) {
				if (main(req, res)) return;
			}
		}

		chain.doFilter(request, response);
	}
	
	protected boolean selectPrimaryRole(HttpServletRequest req, HttpServletResponse res) throws IOException {
		UserContext user = null;
    	try {
    		user = (UserContext)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	} catch (Exception e) {}
    	
    	if (user == null)
        	throw new AccessDeniedException();
        
        if (user.getAuthorities().isEmpty()) {
        	String uri = "main";
    		if (req.getQueryString() != null && !req.getQueryString().isEmpty())
    			uri += "?" + req.getQueryString();
        	res.sendRedirect(uri);
        	return true;
        }

        if (user.getCurrentAuthority() != null && !"Y".equals(req.getParameter("list"))) {
        	String target = sanitizeRedirectTarget(req.getParameter("target"));
        	if (target != null && !target.isEmpty()) {
        		res.sendRedirect(target);
        		return true;
        	} else {
        		String uri = "main";
	    		if (req.getQueryString() != null && !req.getQueryString().isEmpty())
	    			uri += "?" + req.getQueryString();
	        	res.sendRedirect(uri);
	        	return true;
        	}
        }
        return false;
	}
	
	protected boolean matchCampus(AcademicSessionInfo info, String campus) {
		if (info.hasExternalCampus() && campus.equalsIgnoreCase(info.getExternalCampus())) return true;
		return campus.equalsIgnoreCase(info.getInitiative());
	}

	protected boolean matchTerm(AcademicSessionInfo info, String term) {
		if (info.hasExternalTerm() && term.equalsIgnoreCase(info.getExternalTerm())) return true;
		return term.equalsIgnoreCase(info.getTerm() + info.getYear()) || term.equalsIgnoreCase(info.getYear() + info.getTerm()) || term.equalsIgnoreCase(info.getTerm() + info.getYear() + info.getInitiative());
	}

	protected boolean matchSession(AcademicSessionInfo info, String session) {
		if (info.hasExternalTerm() && info.hasExternalCampus() && session.equalsIgnoreCase(info.getExternalTerm() + info.hasExternalCampus())) return true;
		return session.equalsIgnoreCase(info.getTerm() + info.getYear() + info.getInitiative()) || session.equalsIgnoreCase(info.getTerm() + info.getYear()) || session.equals(info.getSessionId().toString());
	}

	protected boolean match(HttpServletRequest request, Long currentSessionId, AcademicSessionInfo info, boolean useDefault) {
		String campus = request.getParameter("campus");
		if (campus != null && !matchCampus(info, campus)) return false;
		String term = request.getParameter("term");
		if (term != null && !matchTerm(info, term)) return false;
		String session = request.getParameter("session");
		if (session != null && !matchSession(info, session)) return false;
		if (useDefault && campus == null && term == null && session == null)
			return info.getSessionId().equals(currentSessionId);
		else
			return true;
	}
	
	protected boolean studentScheduling(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String target = null;
		for (Map.Entry<String, String[]> entry: request.getParameterMap().entrySet()) {
			for (String value: entry.getValue()) {
				if ("prefer".equals(entry.getKey())) continue;
				if (target == null) target = entry.getKey() + "=" + URLEncoder.encode(value, "UTF-8");
				else target += "&" + entry.getKey() + "=" + URLEncoder.encode(value, "UTF-8");
			}
		}
		
		boolean useDefault = ApplicationProperty.StudentSchedulingUseDefaultSession.isTrue();
		
		HttpSessionContext sessionContext = new HttpSessionContext(request.getSession());
        if (!sessionContext.isAuthenticated())
        	throw new AccessDeniedException();
		
		Long currentSessionId = (sessionContext.getUser() == null ? null : sessionContext.getUser().getCurrentAcademicSessionId());
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
					if (match(request, currentSessionId, session, useDefault)) {
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
						if (match(request, currentSessionId, session, useDefault)) {
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
			return true;
		}
		
		// Only for students (check status)
		if (sessionContext.isAuthenticated() && Roles.ROLE_STUDENT.equals(sessionContext.getUser().getCurrentAuthority().getRole())) {
			List<? extends UserQualifier> q = sessionContext.getUser().getCurrentAuthority().getQualifiers("Student");
			if (q != null && !q.isEmpty()) {
				UserQualifier studentQualifier = q.get(0);
				boolean preferCourseRequests = ApplicationProperty.StudentSchedulingPreferCourseRequests.isTrue();
				String prefer = request.getParameter("prefer");
				if (prefer != null)
					preferCourseRequests = "cr".equalsIgnoreCase(prefer) || "crf".equalsIgnoreCase(prefer);
				if (preferCourseRequests) {
					// 1. Course Requests with the registration enabled
					try {
						for (AcademicSessionInfo session:  service.listAcademicSessions(false)) {
							if (match(request, currentSessionId, session, useDefault)) {
								Student student = Student.findByExternalId(session.getSessionId(), studentQualifier.getQualifierReference());
								if (student == null)
									student = StudentDAO.getInstance().get((Long)studentQualifier.getQualifierId());
								if (student == null) continue;
								StudentSectioningStatus status = student.getEffectiveStatus();
								if (status == null || !status.hasOption(StudentSectioningStatus.Option.regenabled)) continue;
								response.sendRedirect("requests" + (target == null ? "" : "?" + target));
								return true;
							}
						}
					} catch (SectioningException e) {}
					// 2. Scheduling Assistant with the enrollment enabled
					try {
						for (AcademicSessionInfo session:  service.listAcademicSessions(true)) {
							if (match(request, currentSessionId, session, useDefault)) {
								OnlineSectioningServer server = getSolverServerService().getOnlineStudentSchedulingContainer().getSolver(session.getSessionId().toString());
								if (server == null || !server.getAcademicSession().isSectioningEnabled()) continue;
								Student student = Student.findByExternalId(session.getSessionId(), studentQualifier.getQualifierReference());
								if (student == null)
									student = StudentDAO.getInstance().get((Long)studentQualifier.getQualifierId());
								if (student == null) continue;
								StudentSectioningStatus status = student.getEffectiveStatus();
								if (status != null && !status.hasOption(StudentSectioningStatus.Option.enrollment)) continue;
								response.sendRedirect("sectioning" + (target == null ? "" : "?" + target));
								return true;
							}
						}
					} catch (SectioningException e) {}
				} else {
					// 1. Scheduling Assistant with the enrollment enabled
					try {
						for (AcademicSessionInfo session:  service.listAcademicSessions(true)) {
							if (match(request, currentSessionId, session, useDefault)) {
								OnlineSectioningServer server = getSolverServerService().getOnlineStudentSchedulingContainer().getSolver(session.getSessionId().toString());
								if (server == null || !server.getAcademicSession().isSectioningEnabled()) continue;
								Student student = Student.findByExternalId(session.getSessionId(), studentQualifier.getQualifierReference());
								if (student == null)
									student = StudentDAO.getInstance().get((Long)studentQualifier.getQualifierId());
								if (student == null) continue;
								StudentSectioningStatus status = student.getEffectiveStatus();
								if (status != null && !status.hasOption(StudentSectioningStatus.Option.enrollment)) continue;
								response.sendRedirect("sectioning" + (target == null ? "" : "?" + target));
								return true;
							}
						}
					} catch (SectioningException e) {}
					// 2. Course Requests with the registration enabled
					try {
						for (AcademicSessionInfo session:  service.listAcademicSessions(false)) {
							if (match(request, currentSessionId, session, useDefault)) {
								Student student = Student.findByExternalId(session.getSessionId(), studentQualifier.getQualifierReference());
								if (student == null)
									student = StudentDAO.getInstance().get((Long)studentQualifier.getQualifierId());
								if (student == null) continue;
								StudentSectioningStatus status = student.getEffectiveStatus();
								if (status == null || !status.hasOption(StudentSectioningStatus.Option.regenabled)) continue;
								response.sendRedirect("requests" + (target == null ? "" : "?" + target));
								return true;
							}
						}
					} catch (SectioningException e) {}					
				}
			}
		}
		
		// 3. Scheduling Assistant
		try {
			for (AcademicSessionInfo session:  service.listAcademicSessions(true)) {
				if (match(request, currentSessionId, session, useDefault)) {
					response.sendRedirect("sectioning" + (target == null ? "" : "?" + target));
					return true;
				}
			}
		} catch (SectioningException e) {}
		
		// 4. Course Requests
		try {
			for (AcademicSessionInfo session:  service.listAcademicSessions(false)) {
				if (match(request, currentSessionId, session, useDefault)) {
					response.sendRedirect("requests" + (target == null ? "" : "?" + target));
					return true;
				}
			}
		} catch (SectioningException e) {}
		
		// 5. Main page fallback
		response.sendRedirect("main");
		return true;
	}
	
	protected boolean back(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSessionContext sessionContext = new HttpSessionContext(request.getSession());
        if (!sessionContext.isAuthenticated() || sessionContext.getUser().getCurrentAuthority() == null)
        	throw new AccessDeniedException();
        
        if (!BackTracker.doBack(request, response))
        	response.sendRedirect("main");
        return true;
	}
	
	protected boolean login(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if (!"true".equals(request.getParameter("force"))) {
			if ("forward".equalsIgnoreCase(ApplicationProperty.LoginMethod.value())) {
				String login = ApplicationProperty.LoginPage.value();
				if (login != null && !"login.jsp".equals(login) && !"/login.jsp".equals(login) && !"login".equals(login) && !"login.action".equals(login)) {
					request.getRequestDispatcher(login).forward(request, response);
					return true;
				}
			} else {
				String target = request.getParameter("target");
				if (target == null)
					response.sendRedirect(ApplicationProperty.LoginPage.value());
				else
					response.sendRedirect(ApplicationProperty.LoginPage.value() + "?target=" + URLEncoder.encode(target, "UTF-8"));
				return true;
			}
		}
		String errorMsg = request.getParameter("message");
		try {
			switch (Integer.valueOf(request.getParameter("E"))) {
			case 1: errorMsg = MSG.errorInvalidUserPasswd(); break;
			case 2: errorMsg = MSG.errorAuthenticationFailed(); break;
			case 3: errorMsg = MSG.errorAuthenticationFailed(); break;
			case 4: errorMsg = MSG.errorUserLockedOut(); break;
			}
		} catch (Exception e) {}
		if (errorMsg != null)
			request.setAttribute("errorMsg", errorMsg);
		String externalHeader = ApplicationProperty.LoginPageHeader.value();
		if (externalHeader != null && !externalHeader.isEmpty()) request.setAttribute("externalHeader", externalHeader);
		String externalFooter = ApplicationProperty.LoginPageFooter.value();
		if (externalFooter != null && !externalFooter.isEmpty()) request.setAttribute("externalFooter", externalFooter);
		StartupService startupService = (StartupService)SpringApplicationContextHolder.getBean("startupService");
		if (startupService != null && startupService.getInitializationException() != null) {
			Throwable t = startupService.getInitializationException();
			String startupError = null;
			while (t != null) {
				String clazz = t.getClass().getName();
				if (clazz.indexOf('.') >= 0) clazz = clazz.substring(1 + clazz.lastIndexOf('.'));
				startupError = (startupError == null ? "" : startupError + "\n") +
						clazz + ": " + t.getMessage() +
						(t.getStackTrace() != null && t.getStackTrace().length > 0 ? " (at " + t.getStackTrace()[0].getFileName() + ":" + t.getStackTrace()[0].getLineNumber() + ")": "");
				t = t.getCause();
			}
			request.setAttribute("startupError", startupError);
		}
		if (ApplicationProperty.AuthenticationOAuht2ClientId.value() != null && !ApplicationProperty.AuthenticationOAuht2ClientId.value().isEmpty()) {
			request.setAttribute("oauth2LoginUrl", "oauth2/authorization/" + ApplicationProperty.AuthenticationOAuht2Provider.value());
			request.setAttribute("oauth2LoginMessage", ApplicationProperty.AuthenticationOAuht2LoginMessage.value());
		}
		request.getRequestDispatcher("login.jsp").forward(request, response);
        return true;
	}
	
	protected boolean logout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Enumeration<String> e = request.getSession().getAttributeNames();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			request.getSession().setAttribute(key, null);
		}	
		request.getSession().invalidate();
		request.getRequestDispatcher("logout.jsp").forward(request, response);
        return true;
	}
	
	protected boolean main(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		UserContext user = null;
    	try {
    		user = (UserContext)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	} catch (Exception e) {}
    	if (user == null)
        	throw new AccessDeniedException();
    	return false;
	}
	
	protected boolean loginRequired(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String target = request.getParameter("target");
		if (target != null && !target.isEmpty())
			request.setAttribute("target", URLEncoder.encode(target, "UTF-8"));
		request.getRequestDispatcher("loginRequired2.jsp").forward(request, response);
        return true;
	}

	protected SolverServerService getSolverServerService() {
		return (SolverServerService)SpringApplicationContextHolder.getBean("solverServerService");
	}
	
	protected String sanitizeRedirectTarget(String target) {
		if (target == null) return null;
		target = target.trim();
		if (target.isEmpty()) return null;
		if (target.indexOf('\r') >= 0 || target.indexOf('\n') >= 0) return null;
		if (target.contains("://") || target.startsWith("//") || target.startsWith("\\\\")) return null;
		return target;
	}
}
