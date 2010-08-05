/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.apache.log4j.Logger;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.authenticate.jaas.LoginConfiguration;
import org.unitime.timetable.authenticate.jaas.UserPasswordHandler;
import org.unitime.timetable.gwt.server.custom.CustomSectionNames;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SectioningServlet extends RemoteServiceServlet implements SectioningService {
	private static final long serialVersionUID = 1L;
	private static Logger sLog = Logger.getLogger(SectioningServlet.class);

	public void init() throws ServletException {
		System.out.println("Student Sectioning Service is starting up ...");
        SectioningServer.init();
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		String year = ApplicationProperties.getProperty("unitime.enrollment.year");
		String term = ApplicationProperties.getProperty("unitime.enrollment.term");
		try {
			for (Iterator<Session> i = SessionDAO.getInstance().findAll(hibSession).iterator(); i.hasNext(); ) {
				final Session session = i.next();
				
				if (year != null && !year.equals(session.getAcademicYear())) continue;
				if (term != null && !term.equals(session.getAcademicTerm())) continue;
				
				if (year == null && term == null &&
					!session.getStatusType().canNoRoleReportClass()) continue;

				int nrSolutions = ((Number)hibSession.createQuery(
						"select count(s) from Solution s where s.owner.session.uniqueId=:sessionId")
						.setLong("sessionId", session.getUniqueId()).uniqueResult()).intValue();
				if (nrSolutions == 0) continue;
				final Long sessionId = session.getUniqueId();
				if ("true".equals(ApplicationProperties.getProperty("unitime.enrollment.autostart", "false"))) {
					Thread t = new Thread(new Runnable() {
						public void run() {
							try {
								SectioningServer.createInstance(sessionId);
							} catch (Exception e) {
								sLog.fatal("Unable to upadte session " + session.getAcademicTerm() + " " + session.getAcademicYear() +
										" (" + session.getAcademicInitiative() + "), reason: "+ e.getMessage(), e);
							}
						}
					});
					t.setName("CourseLoader[" + session.getAcademicTerm()+session.getAcademicYear()+" "+session.getAcademicInitiative()+"]");
					t.setDaemon(true);
					t.start();
				} else {
					try {
						SectioningServer.createInstance(sessionId);
					} catch (Exception e) {
						sLog.fatal("Unable to upadte session " + session.getAcademicTerm() + " " + session.getAcademicYear() +
								" (" + session.getAcademicInitiative() + "), reason: "+ e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			throw new ServletException("Unable to initialize, reason: "+e.getMessage(), e);
		} finally {
			hibSession.close();
		}
	}
	
	public void destroy() {
		System.out.println("Student Sectioning Service is going down ...");
	}
	
	public Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(Long sessionId, String query, Integer limit) throws SectioningException {
		if (sessionId==null) throw new SectioningException(SectioningExceptionType.NO_ACADEMIC_SESSION);
		setLastSessionId(sessionId);
		ArrayList<ClassAssignmentInterface.CourseAssignment> ret = new ArrayList<ClassAssignmentInterface.CourseAssignment>();
		try {
			for (CourseInfo c: SectioningServer.getInstance(sessionId).findCourses(query, limit)) {
				CourseAssignment course = new CourseAssignment();
				course.setCourseId(c.getUniqueId());
				course.setSubject(c.getSubjectArea());
				course.setCourseNbr(c.getCourseNbr());
				course.setNote(c.getNote());
				course.setTitle(c.getTitle());
				course.setHasUniqueName(c.hasUniqueName());
				ret.add(course);
			}
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
		}
		if (ret.isEmpty()) {
			throw new SectioningException(SectioningExceptionType.COURSE_NOT_EXIST, query);
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<ClassAssignmentInterface.ClassAssignment> listClasses(Long sessionId, String course) throws SectioningException {
		setLastSessionId(sessionId);
		ArrayList<ClassAssignmentInterface.ClassAssignment> ret = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
		try {
			if (sessionId==null) throw new SectioningException(SectioningExceptionType.NO_ACADEMIC_SESSION);
			SectioningServer server = SectioningServer.getInstance(sessionId);
			CourseInfo c = server.getCourseInfo(course);
			if (c == null) throw new SectioningException(SectioningExceptionType.COURSE_NOT_EXIST, course);
			Long studentId = getStudentId(sessionId);
			List<Section> sections = server.getSections(c);
			Collections.sort(sections, new Comparator<Section>() {
				public int compare(Config c1, Config c2) {
					int cmp = c1.getName().compareToIgnoreCase(c2.getName());
					if (cmp != 0) return cmp;
					return Double.compare(c1.getId(), c2.getId());
				}
				public boolean isParent(Subpart s1, Subpart s2) {
					Subpart p1 = s1.getParent();
					if (p1==null) return false;
					if (p1.equals(s2)) return true;
					return isParent(p1, s2);
				}
				public int compare(Subpart s1, Subpart s2) {
					int cmp = compare(s1.getConfig(), s2.getConfig());
					if (cmp != 0) return cmp;
			        if (isParent(s1,s2)) return 1;
			        if (isParent(s2,s1)) return -1;
			        cmp = s1.getInstructionalType().compareTo(s2.getInstructionalType());
			        if (cmp != 0) return cmp;
			        return Double.compare(s1.getId(), s2.getId());
				}
				public int compare(Section s1, Section s2) {
					int cmp = compare(s1.getSubpart(), s2.getSubpart());
					if (cmp != 0) return cmp;
					cmp = (s1.getName() == null ? "" : s1.getName()).compareTo(s2.getName() == null ? "" : s2.getName());
					if (cmp != 0) return cmp;
			        return Double.compare(s1.getId(), s2.getId());
				}
			});
			Hashtable<Long, int[]> limits = null;
			if (SectioningServer.sSectionLimitProvider != null) {
				ArrayList<Long> classIds = new ArrayList<Long>();
				final Hashtable<Long, String> classNames = new Hashtable<Long, String>();
				for (Section section: sections) {
					classIds.add(section.getId());
					classNames.put(section.getId(), section.getName());
				}
				CustomSectionNames x = new CustomSectionNames() {
					public void update(AcademicSessionInfo session) {
					}

					public String getClassSuffix(Long sessionId, Long courseId, Long classId) {
						if (SectioningServer.sCustomSectionNames != null) {
							String ret = SectioningServer.sCustomSectionNames.getClassSuffix(sessionId, courseId, classId);
							if (ret != null) return ret;
						}
						return classNames.get(classId);
					}
				};
				limits = SectioningServer.sSectionLimitProvider.getSectionLimits(server.getAcademicSession(), c.getUniqueId(), classIds, x);
			}
			ClassAssignmentInterface.CourseAssignment courseAssign = new ClassAssignmentInterface.CourseAssignment();
			courseAssign.setCourseId(c.getUniqueId());
			courseAssign.setCourseNbr(c.getCourseNbr());
			courseAssign.setSubject(c.getSubjectArea());
			for (Section section: sections) {
				String room = null;
				if (section.getRooms() != null) {
					for (RoomLocation rm: section.getRooms()) {
						if (room == null) room = ""; else room += ", ";
						room += rm.getName();
					}
				}
				int[] limit = (limits == null ? new int[] { -1, section.getLimit()} : limits.get(section.getId()));
				if (limits == null) {
					int actual = section.getEnrollments().size();
					if (studentId != null) {
						for (Iterator<Enrollment> i = section.getEnrollments().iterator(); i.hasNext();) {
							Enrollment enrollment = i.next();
							if (enrollment.getStudent().getId() == studentId) { actual--; break; }
						}
					}
					limit = new int[] {actual, section.getLimit()};
				}
				ClassAssignmentInterface.ClassAssignment a = courseAssign.addClassAssignment();
				a.setClassId(section.getId());
				a.setSubpart(section.getSubpart().getName());
				a.setSection(server.getSectionName(c.getUniqueId(), section));
				a.setLimit(limit);
				if (section.getTime() != null) {
					for (DayCode d: DayCode.toDayCodes(section.getTime().getDayCode()))
						a.addDay(d.getIndex());
					a.setStart(section.getTime().getStartSlot());
					a.setLength(section.getTime().getLength());
					a.setBreakTime(section.getTime().getBreakTime());
					a.setDatePattern(section.getTime().getDatePatternName());
				}
				if (section.getRooms() != null) {
					for (RoomLocation rm: section.getRooms()) {
						a.addRoom(rm.getName());
					}
				}
				if (section.getChoice().getInstructorNames() != null && !section.getChoice().getInstructorNames().isEmpty()) {
					String[] instructors = section.getChoice().getInstructorNames().split(":");
					for (String instructor: instructors) {
						String[] nameEmail = instructor.split("\\|");
						a.addInstructor(nameEmail[0]);
						a.addInstructoEmailr(nameEmail.length < 2 ? "" : nameEmail[1]);
					}
				}
				if (section.getParent() != null)
					a.setParentSection(server.getSectionName(c.getUniqueId(), section.getParent()));
				a.setSubpartId(section.getSubpart().getId());
				if (a.getParentSection() == null)
					a.setParentSection(c.getConsent());
				a.setExpected(section.getSpaceExpected());
				ret.add(a);
			}
		} catch (Exception e) {
			if (e instanceof SectioningException) throw (SectioningException)e;
			sLog.error(e.getMessage(), e);
			throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
		}
		if (ret.isEmpty())
			throw new SectioningException(SectioningExceptionType.NO_CLASSES_FOR_COURSE, course);
		return ret;
	}

	public Collection<String[]> listAcademicSessions() throws SectioningException {
		UniTimePrincipal principal = (UniTimePrincipal)getThreadLocalRequest().getSession().getAttribute("user");
		ArrayList<String[]> ret = new ArrayList<String[]>();
		for (AcademicSessionInfo s: SectioningServer.getAcademicSessions()) {
			if (principal != null && principal.getStudentId(s.getUniqueId()) == null) continue;
			ret.add(new String[] {
					String.valueOf(s.getUniqueId()),
					s.getYear(),
					s.getTerm(),
					s.getCampus()
			});
		}
		if (ret.isEmpty() && principal != null)
			for (AcademicSessionInfo s: SectioningServer.getAcademicSessions()) {
				ret.add(new String[] {
						String.valueOf(s.getUniqueId()),
						s.getYear(),
						s.getTerm(),
						s.getCampus()
				});
			}
		if (ret.isEmpty()) {
			throw new SectioningException(SectioningExceptionType.NO_SUITABLE_ACADEMIC_SESSIONS);
		}
		return ret;
	}

	public String retrieveCourseDetails(Long sessionId, String course) throws SectioningException {
		setLastSessionId(sessionId);
		CourseInfo c = SectioningServer.getInstance(sessionId).getCourseInfo(course);
		if (c == null) throw new SectioningException(SectioningExceptionType.COURSE_NOT_EXIST, course);
		return c.getDetails();
	}
	
	public Collection<String[]> retrieveApplicationProperties(String startsWith) throws SectioningException {
		ArrayList<String[]> ret = new ArrayList<String[]>();
		Properties p = ApplicationProperties.getProperties();
		for (Map.Entry<Object, Object> entry: p.entrySet()) {
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			if (startsWith==null || key.startsWith(startsWith))
				ret.add(new String[] {key, value});
		}
		return ret;
	}
	
	public Long retrieveCourseOfferingId(Long sessionId, String course) throws SectioningException {
		setLastSessionId(sessionId);
		CourseInfo c = SectioningServer.getInstance(sessionId).getCourseInfo(course);
		if (c == null) throw new SectioningException(SectioningExceptionType.COURSE_NOT_EXIST, course);
		return c.getUniqueId();
	}

	public ClassAssignmentInterface section(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException {
		try {
			setLastSessionId(request.getAcademicSessionId());
			setLastRequest(request);
			request.setStudentId(getStudentId(request.getAcademicSessionId()));
			return SectioningServer.getInstance(request.getAcademicSessionId()).section(request, currentAssignment);
		} catch (Exception e) {
			if (e instanceof SectioningException) throw (SectioningException)e;
			sLog.error(e.getMessage(), e);
			throw new SectioningException(SectioningExceptionType.SECTIONING_FAILED, e);
		}
	}
	
	public Collection<String> checkCourses(CourseRequestInterface request) throws SectioningException {
		try {
			setLastSessionId(request.getAcademicSessionId());
			setLastRequest(request);
			request.setStudentId(getStudentId(request.getAcademicSessionId()));
			return SectioningServer.getInstance(request.getAcademicSessionId()).checkCourses(request);
		} catch (Exception e) {
			if (e instanceof SectioningException) throw (SectioningException)e;
			sLog.error(e.getMessage(), e);
			throw new SectioningException(SectioningExceptionType.SECTIONING_FAILED, e);
		}
	}
	
	public 	Collection<ClassAssignmentInterface> computeSuggestions(CourseRequestInterface request, Collection<ClassAssignmentInterface.ClassAssignment> currentAssignment, int selectedAssignmentIndex) throws SectioningException {
		try {
			setLastSessionId(request.getAcademicSessionId());
			setLastRequest(request);
			request.setStudentId(getStudentId(request.getAcademicSessionId()));
			ClassAssignmentInterface.ClassAssignment selectedAssignment = ((List<ClassAssignmentInterface.ClassAssignment>)currentAssignment).get(selectedAssignmentIndex);
			return SectioningServer.getInstance(request.getAcademicSessionId()).computeSuggestions(request, currentAssignment, selectedAssignment);
		} catch (Exception e) {
			if (e instanceof SectioningException) throw (SectioningException)e;
			sLog.error(e.getMessage(), e);
			throw new SectioningException(SectioningExceptionType.SECTIONING_FAILED, e);
		}
	}
	
	public String logIn(String userName, String password) throws SectioningException {
		Integer nrAttempts = (Integer)getThreadLocalRequest().getSession().getAttribute("login.nrAttempts");
		if (nrAttempts == null) nrAttempts = 1; else nrAttempts ++;
		getThreadLocalRequest().getSession().setAttribute("login.nrAttempts", nrAttempts);
		if (nrAttempts > 3) {
			throw new SectioningException(SectioningExceptionType.LOGIN_TOO_MANY_ATTEMPTS);
		}
		try {
			if (userName == null || userName.isEmpty()) throw new SectioningException(SectioningExceptionType.LOGIN_NO_USERNAME);
			
			UserPasswordHandler handler = new UserPasswordHandler(userName,	password);
			LoginContext lc = new LoginContext("Timetabling", new Subject(), handler, new LoginConfiguration());
			lc.login();
			
			Set creds = lc.getSubject().getPublicCredentials();
			if (creds==null || creds.size()==0) {
				throw new SectioningException(SectioningExceptionType.LOGIN_FAILED);
			}
			
			UniTimePrincipal principal = null;
			for (Iterator i=creds.iterator(); i.hasNext(); ) {
				Object o = i.next();
				if (o instanceof User) {
					User user = (User) o;
					
					principal = new UniTimePrincipal(user.getId(), user.getName());

					break;
				}
			}
			
			if (principal == null) throw new SectioningException(SectioningExceptionType.LOGIN_FAILED);
			getThreadLocalRequest().getSession().setAttribute("user", principal);
			getThreadLocalRequest().getSession().removeAttribute("login.nrAttempts");
			return principal.getName();
		} catch (LoginException e) {
			if ("Login Failure: all modules ignored".equals(e.getMessage())) {
				if (nrAttempts == 3)
					throw new SectioningException(SectioningExceptionType.LOGIN_TOO_MANY_ATTEMPTS);
				throw new SectioningException(SectioningExceptionType.LOGIN_FAILED);
			}
			throw new SectioningException(SectioningExceptionType.LOGIN_FAILED_UNKNOWN, e);
		}
	}
	
	public Boolean logOut() throws SectioningException {
		getThreadLocalRequest().getSession().removeAttribute("user");
		getThreadLocalRequest().getSession().removeAttribute("sessionId");
		getThreadLocalRequest().getSession().removeAttribute("request");
		// getThreadLocalRequest().getSession().invalidate();
		return true;
	}
	
	public String whoAmI() throws SectioningException {
		UniTimePrincipal principal = (UniTimePrincipal)getThreadLocalRequest().getSession().getAttribute("user");
		if (getThreadLocalRequest().getSession().isNew()) throw new SectioningException(SectioningExceptionType.USER_NOT_LOGGED_IN);
		if (principal == null) {
			User user = Web.getUser(getThreadLocalRequest().getSession());
			if (user != null)
				principal = new UniTimePrincipal(user.getId(), user.getName());
		}
		if (principal == null) return "Guest";
		return principal.getName();
	}
	
	public Long getStudentId(Long sessionId) {
		UniTimePrincipal principal = (UniTimePrincipal)getThreadLocalRequest().getSession().getAttribute("user");
		if (principal == null) return null;
		return principal.getStudentId(sessionId);
	}
	
	public Long getLastSessionId() {
		Long lastSessionId = (Long)getThreadLocalRequest().getSession().getAttribute("sessionId");
		if (lastSessionId == null) {
			User user = Web.getUser(getThreadLocalRequest().getSession());
			if (user != null) {
				Session session = Session.getCurrentAcadSession(user);
				if (session != null && SectioningServer.getInstance(session.getUniqueId()) != null)
					lastSessionId = session.getUniqueId();
			}
		}
		return lastSessionId;
	}

	public void setLastSessionId(Long sessionId) {
		getThreadLocalRequest().getSession().setAttribute("sessionId", sessionId);
	}
	
	public CourseRequestInterface getLastRequest() {
		return (CourseRequestInterface)getThreadLocalRequest().getSession().getAttribute("request");
	}
	
	public void setLastRequest(CourseRequestInterface request) {
		getThreadLocalRequest().getSession().setAttribute("request", request);
	}
	
	public String[] lastAcademicSession() throws SectioningException {
		if (getThreadLocalRequest().getSession().isNew()) throw new SectioningException(SectioningExceptionType.USER_NOT_LOGGED_IN);
		Long sessionId = getLastSessionId();
		if (sessionId == null) throw new SectioningException(SectioningExceptionType.LAST_ACADEMIC_SESSION_FAILED, "no session was used");
		SectioningServer server = SectioningServer.getInstance(sessionId);
		if (server == null) throw new SectioningException(SectioningExceptionType.LAST_ACADEMIC_SESSION_FAILED, "no server data");
		AcademicSessionInfo s = server.getAcademicSession();
		if (s == null) throw new SectioningException(SectioningExceptionType.LAST_ACADEMIC_SESSION_FAILED, "no session info");
		return new String[] {
				String.valueOf(s.getUniqueId()),
				s.getYear(),
				s.getTerm(),
				s.getCampus()
		};
	}
	
	public CourseRequestInterface lastRequest(Long sessionId) throws SectioningException {
		CourseRequestInterface request = getLastRequest();
		if (request == null) {
			Long studentId = getStudentId(sessionId);
			if (studentId == null) throw new SectioningException(SectioningExceptionType.NO_STUDENT);
			org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
			try {
				SectioningServer server = SectioningServer.getInstance(sessionId);
				Student student = StudentDAO.getInstance().get(studentId, hibSession);
				if (student == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
				if (!student.getCourseDemands().isEmpty()) {
					request = new CourseRequestInterface();
					request.setAcademicSessionId(sessionId);
					TreeSet<CourseDemand> demands = new TreeSet<CourseDemand>(new Comparator<CourseDemand>() {
						public int compare(CourseDemand d1, CourseDemand d2) {
							if (d1.isAlternative() && !d2.isAlternative()) return 1;
							if (!d1.isAlternative() && d2.isAlternative()) return -1;
							int cmp = d1.getPriority().compareTo(d2.getPriority());
							if (cmp != 0) return cmp;
							return d1.getUniqueId().compareTo(d2.getUniqueId());
						}
					});
					demands.addAll(student.getCourseDemands());
					CourseRequestInterface.Request lastRequest = null;
					int lastRequestPriority = -1;
					for (CourseDemand cd: demands) {
						CourseRequestInterface.Request r = null;
						if (cd.getFreeTime() != null) {
							CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
							ft.setStart(cd.getFreeTime().getStartSlot());
							ft.setLength(cd.getFreeTime().getLength());
							for (DayCode day : DayCode.toDayCodes(cd.getFreeTime().getDayCode()))
								ft.addDay(day.getIndex());
							if (lastRequest != null && lastRequestPriority == cd.getPriority()) {
								r = lastRequest;
								lastRequest.addRequestedFreeTime(ft);
								lastRequest.setRequestedCourse(lastRequest.getRequestedCourse() + ", " + ft.toString());
							} else {
								r = new CourseRequestInterface.Request();
								r.addRequestedFreeTime(ft);
								r.setRequestedCourse("Free " + ft.toString());
								if (cd.isAlternative())
									request.getAlternatives().add(r);
								else
									request.getCourses().add(r);
							}
						} else if (!cd.getCourseRequests().isEmpty()) {
							r = new CourseRequestInterface.Request();
							for (Iterator<CourseRequest> i = cd.getCourseRequests().iterator(); i.hasNext(); ) {
								CourseRequest course = i.next();
								CourseInfo c = server.getCourseInfo(course.getCourseOffering().getUniqueId());
								if (c == null) continue;
								switch (course.getOrder()) {
								case 0: 
									r.setRequestedCourse(c.getSubjectArea() + " " + c.getCourseNbr() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
									break;
								case 1:
									r.setFirstAlternative(c.getSubjectArea() + " " + c.getCourseNbr() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
									break;
								case 2:
									r.setSecondAlternative(c.getSubjectArea() + " " + c.getCourseNbr() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
								}
							}
							if (r.hasRequestedCourse()) {
								if (cd.isAlternative())
									request.getAlternatives().add(r);
								else
									request.getCourses().add(r);
							}
						}
						lastRequest = r;
						lastRequestPriority = cd.getPriority();
					}
					if (!request.getCourses().isEmpty()) return request;
				}
				if (!student.getClassEnrollments().isEmpty()) {
					TreeSet<CourseInfo> courses = new TreeSet<CourseInfo>();
					for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
						StudentClassEnrollment enrl = i.next();
						CourseInfo c = server.getCourseInfo(enrl.getCourseOffering().getUniqueId());
						if (c != null)  courses.add(c);
					}
					request = new CourseRequestInterface();
					request.setAcademicSessionId(sessionId);
					for (CourseInfo c: courses) {
						CourseRequestInterface.Request r = new CourseRequestInterface.Request();
						r.setRequestedCourse(c.getSubjectArea() + " " + c.getCourseNbr() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
						request.getCourses().add(r);
					}
					if (!request.getCourses().isEmpty()) return request;
				}
				throw new SectioningException(SectioningExceptionType.NO_STUDENT_REQUESTS);
			} catch (Exception e) {
				if (e instanceof SectioningException) throw (SectioningException)e;
				throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
			} finally {
				hibSession.close();
			}
		}
		if (!request.getAcademicSessionId().equals(sessionId)) throw new SectioningException(SectioningExceptionType.BAD_SESSION);
		return request;
	}
	
	public ArrayList<ClassAssignmentInterface.ClassAssignment> lastResult(Long sessionId) throws SectioningException {
		Long studentId = getStudentId(sessionId);
		if (studentId == null) throw new SectioningException(SectioningExceptionType.NO_STUDENT);
		org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
		try {
			SectioningServer server = SectioningServer.getInstance(sessionId);
			Student student = StudentDAO.getInstance().get(studentId, hibSession);
			if (student == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
			if (!student.getClassEnrollments().isEmpty()) {
				ArrayList<ClassAssignmentInterface.ClassAssignment> ret = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
				for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
					StudentClassEnrollment enrl = i.next();
					CourseInfo course = server.getCourseInfo(enrl.getCourseOffering().getUniqueId());
					Section section = server.getSection(enrl.getClazz().getUniqueId());
					if (course == null || section == null) continue;
					ClassAssignmentInterface.ClassAssignment ca = new ClassAssignmentInterface.ClassAssignment();
					ca.setCourseId(course.getUniqueId());
					ca.setClassId(section.getId());
					ca.setPinned(true);
					ca.setSubject(course.getSubjectArea());
					ca.setCourseNbr(course.getCourseNbr());
					ca.setSubpart(section.getSubpart().getName());
					ca.setSection(server.getSectionName(course.getUniqueId(), section));
					ret.add(ca);
				}
				if (!ret.isEmpty()) return ret;
			}
			throw new SectioningException(SectioningExceptionType.NO_STUDENT_SCHEDULE);
		} catch (Exception e) {
			if (e instanceof SectioningException) throw (SectioningException)e;
			throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
		} finally {
			hibSession.close();
		}
	}

	public Boolean saveRequest(CourseRequestInterface request) throws SectioningException {
		if ("true".equals(ApplicationProperties.getProperty("unitime.enrollment.enabled","true"))) return false;
		if (!"true".equals(ApplicationProperties.getProperty("unitime.enrollment.requests.save","false"))) return false;
		UniTimePrincipal principal = (UniTimePrincipal)getThreadLocalRequest().getSession().getAttribute("user");
		if (principal == null) throw new SectioningException(SectioningExceptionType.ENROLL_NOT_AUTHENTICATED);
		Long studentId = principal.getStudentId(request.getAcademicSessionId());
		SectioningServer server = SectioningServer.getInstance(request.getAcademicSessionId());
		if (studentId == null) throw new SectioningException(SectioningExceptionType.ENROLL_NOT_STUDENT, server.getAcademicSession().toString());
		org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
		try {
			Student student = StudentDAO.getInstance().get(studentId, hibSession);
			if (student == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
			saveRequest(hibSession, student, request, true);
			hibSession.save(student);
			hibSession.flush();
			return true;
		} catch (Exception e) {
			if (e instanceof SectioningException) throw (SectioningException)e;
			sLog.error(e.getMessage(), e);
			throw new SectioningException(SectioningExceptionType.UNKNOWN, e.getMessage(), e);
		} finally {
			hibSession.close();
		}
	}
	
	public Hashtable<Long, CourseRequest> saveRequest(org.hibernate.Session hibSession, Student student, CourseRequestInterface request, boolean keepEnrollments) throws SectioningException {
		Hashtable<Long, CourseRequest> ret = new Hashtable<Long, CourseRequest>();
		SectioningServer server = SectioningServer.getInstance(student.getSession().getUniqueId());
		for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
			StudentClassEnrollment enrl = i.next();
			if (keepEnrollments) {
				if (enrl.getCourseRequest() != null) {
					enrl.getCourseRequest().getClassEnrollments().remove(enrl);
					enrl.setCourseRequest(null);
					hibSession.save(enrl);
				}
			} else {
				enrl.getClazz().getStudentEnrollments().remove(enrl);
				hibSession.delete(enrl);
			}
		}
		if (!keepEnrollments) student.getClassEnrollments().clear();
		for (Iterator<CourseDemand> j =  student.getCourseDemands().iterator(); j.hasNext(); ) {
			CourseDemand cd = j.next();
			if (cd.getFreeTime() != null) hibSession.delete(cd.getFreeTime());
			for (Iterator<CourseRequest> k = cd.getCourseRequests().iterator(); k.hasNext(); ) {
				CourseRequest cr = k.next();
				hibSession.delete(cr);
			}
			hibSession.delete(cd);
		}
		student.getCourseDemands().clear();
		int priority = 0;
		Date ts = new Date();
		for (CourseRequestInterface.Request r: request.getCourses()) {
			if (r.hasRequestedFreeTime()) {
				for (CourseRequestInterface.FreeTime ft: r.getRequestedFreeTime()) {
					CourseDemand cd = new CourseDemand();
					cd.setAlternative(false);
					cd.setPriority(priority);
					cd.setTimestamp(ts);
					cd.setWaitlist(false);
					FreeTime free = new FreeTime();
					free.setCategory(0);
					free.setDayCode(DayCode.toInt(DayCode.toDayCodes(ft.getDays())));
					free.setStartSlot(ft.getStart());
					free.setLength(ft.getLength());
					free.setSession(student.getSession());
					free.setName(ft.toString());
					cd.setFreeTime(free);
					cd.setStudent(student);
					student.getCourseDemands().add(cd);
				}
			} else {
				CourseDemand cd = new CourseDemand();
				cd.setAlternative(false);
				cd.setPriority(priority);
				cd.setTimestamp(ts);
				cd.setWaitlist(false);
				cd.setCourseRequests(new HashSet<CourseRequest>());
				if (r.hasRequestedCourse()) {
					CourseInfo c = server.getCourseInfo(r.getRequestedCourse());
					CourseOffering co = (c == null ? null : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
					if (co != null) {
						CourseRequest cr = new CourseRequest();
						cr.setAllowOverlap(false);
						cr.setCredit(0);
						cr.setOrder(0);
						cr.setCourseOffering(co);
						cd.getCourseRequests().add(cr);
						cr.setCourseDemand(cd);
						ret.put(co.getUniqueId(), cr);
					}
				}
				if (r.hasFirstAlternative()) {
					CourseInfo c = server.getCourseInfo(r.getFirstAlternative());
					CourseOffering co = (c == null ? null : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
					if (co != null) {
						CourseRequest cr = new CourseRequest();
						cr.setAllowOverlap(false);
						cr.setCredit(0);
						cr.setOrder(1);
						cr.setCourseOffering(co);
						cd.getCourseRequests().add(cr);
						cr.setCourseDemand(cd);
						ret.put(co.getUniqueId(), cr);
					}
				}
				if (r.hasSecondAlternative()) {
					CourseInfo c = server.getCourseInfo(r.getSecondAlternative());
					CourseOffering co = (c == null ? null : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
					if (co != null) {
						CourseRequest cr = new CourseRequest();
						cr.setAllowOverlap(false);
						cr.setCredit(0);
						cr.setOrder(2);
						cr.setCourseOffering(co);
						cd.getCourseRequests().add(cr);
						cr.setCourseDemand(cd);
						ret.put(co.getUniqueId(), cr);
					}
				}
				if (cd.getCourseRequests().isEmpty()) continue;
				cd.setStudent(student);
				student.getCourseDemands().add(cd);
			}
			priority++;
		}
		priority = 0;
		for (CourseRequestInterface.Request r: request.getAlternatives()) {
			CourseDemand cd = new CourseDemand();
			cd.setAlternative(true);
			cd.setPriority(priority);
			cd.setTimestamp(ts);
			cd.setWaitlist(false);
			cd.setCourseRequests(new HashSet<CourseRequest>());
			if (r.hasRequestedCourse()) {
				CourseInfo c = server.getCourseInfo(r.getRequestedCourse());
				CourseOffering co = (c == null ? null : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
				if (co != null) {
					CourseRequest cr = new CourseRequest();
					cr.setAllowOverlap(false);
					cr.setCredit(0);
					cr.setOrder(0);
					cr.setCourseOffering(co);
					cd.getCourseRequests().add(cr);
					cr.setCourseDemand(cd);
					ret.put(co.getUniqueId(), cr);
				}
			}
			if (r.hasFirstAlternative()) {
				CourseInfo c = server.getCourseInfo(r.getFirstAlternative());
				CourseOffering co = (c == null ? null : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
				if (co != null) {
					CourseRequest cr = new CourseRequest();
					cr.setAllowOverlap(false);
					cr.setCredit(0);
					cr.setOrder(1);
					cr.setCourseOffering(co);
					cd.getCourseRequests().add(cr);
					cr.setCourseDemand(cd);
					ret.put(co.getUniqueId(), cr);
				}
			}
			if (r.hasSecondAlternative()) {
				CourseInfo c = server.getCourseInfo(r.getSecondAlternative());
				CourseOffering co = (c == null ? null : CourseOfferingDAO.getInstance().get(c.getUniqueId(), hibSession));
				if (co != null) {
					CourseRequest cr = new CourseRequest();
					cr.setAllowOverlap(false);
					cr.setCredit(0);
					cr.setOrder(2);
					cr.setCourseOffering(co);
					cr.setCourseDemand(cd);
					cd.getCourseRequests().add(cr);
					ret.put(co.getUniqueId(), cr);
				}
			}
			if (cd.getCourseRequests().isEmpty()) continue;
			cd.setStudent(student);
			student.getCourseDemands().add(cd);
			priority++;
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private boolean isAvailable(org.hibernate.Session hibSession, Student student, Class_ clazz, Section section) {
		if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment()) return true;
		int limit = clazz.getMaxExpectedCapacity();
		if (clazz.getExpectedCapacity() < clazz.getMaxExpectedCapacity()) {
			Assignment commited = clazz.getCommittedAssignment();
			int roomLimit = 0;
			if (commited != null) {
				int roomCap = 0;
				for (Iterator<Location> i = commited.getRooms().iterator(); i.hasNext(); ) roomCap += i.next().getCapacity();
				roomLimit = Math.round(clazz.getRoomRatio() * roomCap);
			}
			limit = Math.min(Math.max(roomLimit, clazz.getExpectedCapacity()), clazz.getMaxExpectedCapacity());
		}
		if (limit != section.getLimit()) {
			sLog.warn("Limit of " + clazz.getClassLabel() + " changed (" + limit +" != " + section.getLimit() + ").");
		}
		if (clazz.getStudentEnrollments().size() != section.getEnrollments().size()) {
			sLog.warn("Enrollment of " + clazz.getClassLabel() + " changed (" + clazz.getStudentEnrollments().size() +" != " + section.getEnrollments().size() + ").");
			enrl: for (Iterator<StudentClassEnrollment> i = clazz.getStudentEnrollments().iterator(); i.hasNext(); ) {
				StudentClassEnrollment enrl = i.next();
				for (Iterator<Enrollment> j = section.getEnrollments().iterator(); j.hasNext();) {
					Enrollment enrollment = j.next();
					if (enrollment.getStudent().getId() == enrl.getStudent().getUniqueId()) continue enrl;
				}
				sLog.warn(" -- student " + enrl.getStudent().getExternalUniqueId() + " not present in section enrollments (solver).");
			}
			enrl: for (Iterator<Enrollment> i = section.getEnrollments().iterator(); i.hasNext();) {
				Enrollment enrollment = i.next();
				for (Iterator<StudentClassEnrollment> j = clazz.getStudentEnrollments().iterator(); j.hasNext(); ) {
					StudentClassEnrollment enrl = j.next();
					if (enrollment.getStudent().getId() == enrl.getStudent().getUniqueId()) continue enrl;
				}
				Student s = StudentDAO.getInstance().get(enrollment.getStudent().getId(), hibSession);
				sLog.warn(" -- student " + s.getExternalUniqueId() + " not present in class enrollments (db).");
			}
		}
		if (clazz.getStudentEnrollments().size() < limit) return true;
		if (clazz.getStudentEnrollments().size() > limit) return false;
		for (Iterator<StudentClassEnrollment> i = clazz.getStudentEnrollments().iterator(); i.hasNext(); ) {
			StudentClassEnrollment enrl = i.next();
			if (enrl.getStudent().equals(student)) return true;
		}
		return false;
	}
	
	public synchronized ArrayList<Long> enroll(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException {
		UniTimePrincipal principal = (UniTimePrincipal)getThreadLocalRequest().getSession().getAttribute("user");
		if (principal == null) throw new SectioningException(SectioningExceptionType.ENROLL_NOT_AUTHENTICATED);
		Long studentId = principal.getStudentId(request.getAcademicSessionId());
		SectioningServer server = SectioningServer.getInstance(request.getAcademicSessionId());
		if (studentId == null) throw new SectioningException(SectioningExceptionType.ENROLL_NOT_STUDENT, server.getAcademicSession().toString());
		if (!"true".equals(ApplicationProperties.getProperty("unitime.enrollment.enabled","true")))
			throw new SectioningException(SectioningExceptionType.FEATURE_NOT_SUPPORTED);
		org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
		try {
			Student student = StudentDAO.getInstance().get(studentId, hibSession);
			if (student == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
			Hashtable<Long, Class_> classes = new Hashtable<Long, Class_>();
			for (ClassAssignmentInterface.ClassAssignment ca: currentAssignment) {
				if (ca.isFreeTime() || ca.getClassId() == null) continue;
				Class_ clazz = Class_DAO.getInstance().get(ca.getClassId(), hibSession);
				if (!isAvailable(hibSession, student, clazz, server.getSection(ca.getClassId())))
					throw new SectioningException(SectioningExceptionType.ENROLL_NOT_AVAILABLE, ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection());
				classes.put(clazz.getUniqueId(), clazz);
			}
			Hashtable<Long, CourseRequest> req = saveRequest(hibSession, student, request, false);
			Date ts = new Date();
			for (ClassAssignmentInterface.ClassAssignment ca: currentAssignment) {
				if (ca.isFreeTime() || ca.getClassId() == null) continue;
				Class_ clazz = classes.get(ca.getClassId());
				CourseRequest cr = req.get(ca.getCourseId());
				if (clazz == null || cr == null) continue;
				StudentClassEnrollment enrl = new StudentClassEnrollment();
				enrl.setClazz(clazz);
				clazz.getStudentEnrollments().add(enrl);
				enrl.setCourseOffering(cr.getCourseOffering());
				enrl.setCourseRequest(cr);
				enrl.setTimestamp(ts);
				enrl.setStudent(student);
				student.getClassEnrollments().add(enrl);
			}
			hibSession.save(student);
			hibSession.flush();
			hibSession.refresh(student);
			server.reloadStudent(student);
			ArrayList<Long> ret = new ArrayList<Long>();
			ret.addAll(server.getSavedClasses(student.getUniqueId()));
			return ret;
		} catch (Exception e) {
			if (e instanceof SectioningException) throw (SectioningException)e;
			sLog.error(e.getMessage(), e);
			throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
		} finally {
			hibSession.close();
		}
	}

}