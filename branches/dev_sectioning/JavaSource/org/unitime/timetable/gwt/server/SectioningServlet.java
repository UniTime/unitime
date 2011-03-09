/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;

import net.sf.cpsolver.coursett.model.Placement;
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
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SectioningExceptionType;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningService;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerUpdater;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.solver.ComputeSuggestionsAction;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction;
import org.unitime.timetable.onlinesectioning.updates.EnrollStudent;
import org.unitime.timetable.onlinesectioning.updates.SaveStudentRequests;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author Tomas Muller
 */
public class SectioningServlet extends RemoteServiceServlet implements SectioningService {
	private static final long serialVersionUID = 1L;
	private static Logger sLog = Logger.getLogger(SectioningServlet.class);
	private OnlineSectioningServerUpdater iUpdater;

	public void init() throws ServletException {
		System.out.println("Student Sectioning Service is starting up ...");
		OnlineSectioningService.init();
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		String year = ApplicationProperties.getProperty("unitime.enrollment.year");
		String term = ApplicationProperties.getProperty("unitime.enrollment.term");
		try {
			iUpdater = new OnlineSectioningServerUpdater(StudentSectioningQueue.getLastTimeStamp(hibSession, null));
			for (Iterator<Session> i = SessionDAO.getInstance().findAll(hibSession).iterator(); i.hasNext(); ) {
				final Session session = i.next();
				
				if (year != null && !year.equals(session.getAcademicYear())) continue;
				if (term != null && !term.equals(session.getAcademicTerm())) continue;
				if (!session.getStatusType().canSectioningStudents()) continue;

				int nrSolutions = ((Number)hibSession.createQuery(
						"select count(s) from Solution s where s.owner.session.uniqueId=:sessionId")
						.setLong("sessionId", session.getUniqueId()).uniqueResult()).intValue();
				if (nrSolutions == 0) continue;
				final Long sessionId = session.getUniqueId();
				if ("true".equals(ApplicationProperties.getProperty("unitime.enrollment.autostart", "false"))) {
					Thread t = new Thread(new Runnable() {
						public void run() {
							try {
								OnlineSectioningService.createInstance(sessionId);
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
						OnlineSectioningService.createInstance(sessionId);
					} catch (Exception e) {
						sLog.fatal("Unable to upadte session " + session.getAcademicTerm() + " " + session.getAcademicYear() +
								" (" + session.getAcademicInitiative() + "), reason: "+ e.getMessage(), e);
					}
				}
			}
			iUpdater.start();
		} catch (Exception e) {
			throw new ServletException("Unable to initialize, reason: "+e.getMessage(), e);
		} finally {
			hibSession.close();
		}
	}
	
	public void destroy() {
		System.out.println("Student Sectioning Service is going down ...");
		iUpdater.stopUpdating();
		OnlineSectioningService.unloadAll();
	}
	
	public Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(Long sessionId, String query, Integer limit) throws SectioningException {
		if (sessionId==null) throw new SectioningException(SectioningExceptionType.NO_ACADEMIC_SESSION);
		setLastSessionId(sessionId);
		if (OnlineSectioningService.getInstance(sessionId) == null) {
			ArrayList<ClassAssignmentInterface.CourseAssignment> results = new ArrayList<ClassAssignmentInterface.CourseAssignment>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
					"select c from CourseOffering c where " +
					"c.subjectArea.session.uniqueId = :sessionId and (" +
					"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' " +
					(query.length()>2 ? "or lower(c.title) like '%' || :q || '%'" : "") + ") " +
					"order by case " +
					"when lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' then 0 else 1 end," + // matches on course name first
					"c.subjectArea.subjectAreaAbbreviation, c.courseNbr")
					.setString("q", query.toLowerCase())
					.setLong("sessionId", sessionId)
					.setCacheable(true).setMaxResults(limit == null || limit < 0 ? Integer.MAX_VALUE : limit).list()) {
				CourseAssignment course = new CourseAssignment();
				course.setCourseId(c.getUniqueId());
				course.setSubject(c.getSubjectAreaAbbv());
				course.setCourseNbr(c.getCourseNbr());
				course.setNote(c.getScheduleBookNote());
				course.setTitle(c.getTitle());
				course.setHasUniqueName(true);
				boolean unlimited = false;
				int courseLimit = 0;
				for (Iterator<InstrOfferingConfig> i = c.getInstructionalOffering().getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
					InstrOfferingConfig cfg = i.next();
					if (cfg.isUnlimitedEnrollment()) unlimited = true;
					if (cfg.getLimit() != null) courseLimit += cfg.getLimit();
				}
				if (c.getReservation() != null)
					courseLimit = c.getReservation();
	            if (courseLimit >= 9999) unlimited = true;
				course.setLimit(unlimited ? -1 : courseLimit);
				course.setProjected(c.getProjectedDemand());
				course.setEnrollment(c.getEnrollment());
				course.setLastLike(c.getDemand());
				results.add(course);
			}
			if (results.isEmpty()) {
				throw new SectioningException(SectioningExceptionType.COURSE_NOT_EXIST, query);
			}
			return results;
		} else {
			ArrayList<ClassAssignmentInterface.CourseAssignment> ret = new ArrayList<ClassAssignmentInterface.CourseAssignment>();
			try {
				for (CourseInfo c: OnlineSectioningService.getInstance(sessionId).findCourses(query, limit)) {
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
				if (e instanceof SectioningException) throw (SectioningException)e;
				sLog.error(e.getMessage(), e);
				throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
			}
			if (ret.isEmpty()) {
				throw new SectioningException(SectioningExceptionType.COURSE_NOT_EXIST, query);
			}
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Collection<ClassAssignmentInterface.ClassAssignment> listClasses(Long sessionId, String course) throws SectioningException {
		setLastSessionId(sessionId);
		OnlineSectioningServer server = OnlineSectioningService.getInstance(sessionId);
		if (server == null) {
			ArrayList<ClassAssignmentInterface.ClassAssignment> results = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			CourseOffering courseOffering = null;
			for (CourseOffering c: (List<CourseOffering>)hibSession.createQuery(
					"select c from CourseOffering c where " +
					"c.subjectArea.session.uniqueId = :sessionId and " +
					"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) = :course")
					.setString("course", course.toLowerCase())
					.setLong("sessionId", sessionId)
					.setCacheable(true).setMaxResults(1).list()) {
				courseOffering = c; break;
			}
			if (courseOffering == null) throw new SectioningException(SectioningExceptionType.COURSE_NOT_EXIST);
			List<Class_> classes = new ArrayList<Class_>();
			for (Iterator<InstrOfferingConfig> i = courseOffering.getInstructionalOffering().getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
				InstrOfferingConfig config = i.next();
				for (Iterator<SchedulingSubpart> j = config.getSchedulingSubparts().iterator(); j.hasNext(); ) {
					SchedulingSubpart subpart = j.next();
					classes.addAll(subpart.getClasses());
				}
			}
			Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
			for (Class_ clazz: classes) {
				ClassAssignmentInterface.ClassAssignment a = new ClassAssignmentInterface.ClassAssignment();
				a.setClassId(clazz.getUniqueId());
				a.setSubpart(clazz.getSchedulingSubpart().getItypeDesc());
				a.setSection(clazz.getClassSuffix(courseOffering));
				
				Assignment ass = clazz.getCommittedAssignment();
				Placement p = (ass == null ? null : ass.getPlacement());
				
                int minLimit = clazz.getExpectedCapacity();
            	int maxLimit = clazz.getMaxExpectedCapacity();
            	int limit = maxLimit;
            	if (minLimit < maxLimit && p != null) {
            		int roomLimit = Math.round((clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()) * p.getRoomSize());
            		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
            	}
                if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = -1;
				a.setLimit(new int[] {-1, limit});
				
				if (p != null && p.getTimeLocation() != null) {
					for (DayCode d: DayCode.toDayCodes(p.getTimeLocation().getDayCode()))
						a.addDay(d.getIndex());
					a.setStart(p.getTimeLocation().getStartSlot());
					a.setLength(p.getTimeLocation().getLength());
					a.setBreakTime(p.getTimeLocation().getBreakTime());
					a.setDatePattern(p.getTimeLocation().getDatePatternName());
				}
				if (p != null && p.getRoomLocations() != null) {
					for (RoomLocation rm: p.getRoomLocations()) {
						a.addRoom(rm.getName());
					}
				}
				if (p != null && p.getRoomLocation() != null) {
					a.addRoom(p.getRoomLocation().getName());
				}
				if (!clazz.getClassInstructors().isEmpty()) {
					for (Iterator<ClassInstructor> i = clazz.getClassInstructors().iterator(); i.hasNext(); ) {
						ClassInstructor instr = i.next();
						a.addInstructor(instr.getInstructor().getName(DepartmentalInstructor.sNameFormatShort));
						a.addInstructoEmailr(instr.getInstructor().getEmail());
					}
				}
				if (clazz.getParentClass() != null)
					a.setParentSection(clazz.getParentClass().getClassSuffix(courseOffering));
				a.setSubpartId(clazz.getSchedulingSubpart().getUniqueId());
				if (a.getParentSection() == null)
					a.setParentSection(courseOffering.getInstructionalOffering().getConsentType() == null ? null : courseOffering.getInstructionalOffering().getConsentType().getLabel());
				//TODO: Do we want to populate expected space?
				a.setExpected(0.0);
				results.add(a);
			}
			return results;
		} else {
			ArrayList<ClassAssignmentInterface.ClassAssignment> ret = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
			Lock lock = server.readLock();
			try {
				if (sessionId==null) throw new SectioningException(SectioningExceptionType.NO_ACADEMIC_SESSION);
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
				Map<Long, int[]> limits = null;
				if (OnlineSectioningService.sSectionLimitProvider != null) {
					limits = OnlineSectioningService.sSectionLimitProvider.getSectionLimits(server.getAcademicSession(), c.getUniqueId(), sections);
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
					int[] limit = (limits == null ? new int[] { section.getEnrollments().size(), section.getLimit()} : limits.get(section.getId()));
					ClassAssignmentInterface.ClassAssignment a = courseAssign.addClassAssignment();
					a.setClassId(section.getId());
					a.setSubpart(section.getSubpart().getName());
					a.setSection(section.getName(c.getUniqueId()));
					a.setLimit(limit);
					if (studentId != null) {
						for (Iterator<Enrollment> i = section.getEnrollments().iterator(); i.hasNext();) {
							Enrollment enrollment = i.next();
							if (enrollment.getStudent().getId() == studentId) { a.setSaved(true); break; }
						}
					}
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
						a.setParentSection(section.getParent().getName(c.getUniqueId()));
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
			} finally {
				lock.release();
			}
			if (ret.isEmpty())
				throw new SectioningException(SectioningExceptionType.NO_CLASSES_FOR_COURSE, course);
			return ret;	
		}
	}

	public Collection<String[]> listAcademicSessions(boolean sectioning) throws SectioningException {
		ArrayList<String[]> ret = new ArrayList<String[]>();
		if (sectioning) {
			UniTimePrincipal principal = (UniTimePrincipal)getThreadLocalRequest().getSession().getAttribute("user");
			for (AcademicSessionInfo s: OnlineSectioningService.getAcademicSessions()) {
				if (principal != null && principal.getStudentId(s.getUniqueId()) == null) continue;
				ret.add(new String[] {
						String.valueOf(s.getUniqueId()),
						s.getYear(),
						s.getTerm(),
						s.getCampus()
				});
			}
			if (ret.isEmpty() && principal != null)
				for (AcademicSessionInfo s: OnlineSectioningService.getAcademicSessions()) {
					ret.add(new String[] {
							String.valueOf(s.getUniqueId()),
							s.getYear(),
							s.getTerm(),
							s.getCampus()
					});
				}
		} else {
			for (Session session: SessionDAO.getInstance().findAll()) {
				if (session.getStatusType().canPreRegisterStudents() && !session.getStatusType().canSectioningStudents())
					ret.add(new String[] {
							String.valueOf(session.getUniqueId()),
							session.getAcademicYear(),
							session.getAcademicTerm(),
							session.getAcademicInitiative()
					});
			}
		}
		if (ret.isEmpty()) {
			throw new SectioningException(SectioningExceptionType.NO_SUITABLE_ACADEMIC_SESSIONS);
		}
		return ret;
	}
	
	public String retrieveCourseDetails(Long sessionId, String course) throws SectioningException {
		setLastSessionId(sessionId);
		if (OnlineSectioningService.getInstance(sessionId) == null) {
			CourseOffering courseOffering = SaveStudentRequests.getCourse(CourseOfferingDAO.getInstance().getSession(), sessionId, course);
			if (courseOffering == null) throw new SectioningException(SectioningExceptionType.COURSE_NOT_EXIST, course);
			CourseDetailsProvider provider = null;
			try {
				provider = (CourseDetailsProvider)Class.forName(ApplicationProperties.getProperty("unitime.custom.CourseDetailsProvider")).newInstance();
			} catch (Exception e) {
				throw new SectioningException(SectioningExceptionType.NO_CUSTOM_COURSE_DETAILS);
			}
			String details = provider.getDetails(
					new AcademicSessionInfo(courseOffering.getSubjectArea().getSession()),
					courseOffering.getSubjectAreaAbbv(), courseOffering.getCourseNbr());
			return details;
		} else {
			CourseInfo c = OnlineSectioningService.getInstance(sessionId).getCourseInfo(course);
			if (c == null) throw new SectioningException(SectioningExceptionType.COURSE_NOT_EXIST, course);
			return c.getDetails();
		}
	}
	
	public Long retrieveCourseOfferingId(Long sessionId, String course) throws SectioningException {
		setLastSessionId(sessionId);
		CourseInfo c = OnlineSectioningService.getInstance(sessionId).getCourseInfo(course);
		if (c == null) throw new SectioningException(SectioningExceptionType.COURSE_NOT_EXIST, course);
		return c.getUniqueId();
	}

	public ClassAssignmentInterface section(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException {
		try {
			setLastSessionId(request.getAcademicSessionId());
			setLastRequest(request);
			request.setStudentId(getStudentId(request.getAcademicSessionId()));
			ClassAssignmentInterface ret = OnlineSectioningService.getInstance(request.getAcademicSessionId()).execute(
					new FindAssignmentAction(request, currentAssignment)).get(0);
			if (ret != null) {
				ret.setCanEnroll(true);
				if (!"true".equals(ApplicationProperties.getProperty("unitime.enrollment.enabled","true"))) {
					ret.setCanEnroll(false);
				} else {
					UniTimePrincipal principal = (UniTimePrincipal)getThreadLocalRequest().getSession().getAttribute("user");
					if (principal == null || principal.getStudentId(request.getAcademicSessionId()) == null) {
						ret.setCanEnroll(false);
					}
				}
			}
			return ret;
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
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			if (OnlineSectioningService.getInstance(request.getAcademicSessionId()) == null) {
				ArrayList<String> notFound = new ArrayList<String>();
				for (CourseRequestInterface.Request cr: request.getCourses()) {
					if (!cr.hasRequestedFreeTime() && cr.hasRequestedCourse() && SaveStudentRequests.getCourse(hibSession, request.getAcademicSessionId(), cr.getRequestedCourse()) == null)
						notFound.add(cr.getRequestedCourse());
					if (cr.hasFirstAlternative() && SaveStudentRequests.getCourse(hibSession, request.getAcademicSessionId(), cr.getFirstAlternative()) == null)
						notFound.add(cr.getFirstAlternative());
					if (cr.hasSecondAlternative() && SaveStudentRequests.getCourse(hibSession, request.getAcademicSessionId(), cr.getSecondAlternative()) == null)
						notFound.add(cr.getSecondAlternative());
				}
				for (CourseRequestInterface.Request cr: request.getAlternatives()) {
					if (cr.hasRequestedCourse() && SaveStudentRequests.getCourse(hibSession, request.getAcademicSessionId(),cr.getRequestedCourse()) == null)
						notFound.add(cr.getRequestedCourse());
					if (cr.hasFirstAlternative() && SaveStudentRequests.getCourse(hibSession, request.getAcademicSessionId(),cr.getFirstAlternative()) == null)
						notFound.add(cr.getFirstAlternative());
					if (cr.hasSecondAlternative() && SaveStudentRequests.getCourse(hibSession, request.getAcademicSessionId(),cr.getSecondAlternative()) == null)
						notFound.add(cr.getSecondAlternative());
				}
				return notFound;
			} else {
				request.setStudentId(getStudentId(request.getAcademicSessionId()));
				return OnlineSectioningService.getInstance(request.getAcademicSessionId()).checkCourses(request);
			}
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
			Collection<ClassAssignmentInterface> ret = OnlineSectioningService.getInstance(request.getAcademicSessionId()).execute(new ComputeSuggestionsAction(request, currentAssignment, selectedAssignment));
			if (ret != null) {
				boolean canEnroll = true;
				if (!"true".equals(ApplicationProperties.getProperty("unitime.enrollment.enabled","true"))) {
					canEnroll = false;
				} else {
					UniTimePrincipal principal = (UniTimePrincipal)getThreadLocalRequest().getSession().getAttribute("user");
					if (principal == null || principal.getStudentId(request.getAcademicSessionId()) == null) {
						canEnroll = false;
					}
				}
				for (ClassAssignmentInterface ca: ret)
					ca.setCanEnroll(canEnroll);
			}
			return ret;
		} catch (Exception e) {
			if (e instanceof SectioningException) throw (SectioningException)e;
			sLog.error(e.getMessage(), e);
			throw new SectioningException(SectioningExceptionType.SECTIONING_FAILED, e);
		}
	}
	
	public String logIn(String userName, String password) throws SectioningException {
		if ("LOOKUP".equals(userName)) {
			if (!isAdmin())
				throw new SectioningException(SectioningExceptionType.LOGIN_FAILED);
			org.hibernate.Session hibSession = StudentDAO.getInstance().createNewSession();
			try {
				List<Student> student = hibSession.createQuery("select m from Student m where m.externalUniqueId = :uid").setString("uid", password).list();
				if (!student.isEmpty()) {
					User user = Web.getUser(getThreadLocalRequest().getSession());
					UniTimePrincipal principal = new UniTimePrincipal(user.getId(), user.getName());
					for (Student s: student) {
						principal.addStudentId(s.getSession().getUniqueId(), s.getUniqueId());
						principal.setName(s.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle));
					}
					getThreadLocalRequest().getSession().setAttribute("user", principal);
					getThreadLocalRequest().getSession().removeAttribute("login.nrAttempts");
					return principal.getName();
				}
			} finally {
				hibSession.close();
			}			
		}
		Integer nrAttempts = (Integer)getThreadLocalRequest().getSession().getAttribute("login.nrAttempts");
		if (nrAttempts == null) nrAttempts = 1; else nrAttempts ++;
		getThreadLocalRequest().getSession().setAttribute("login.nrAttempts", nrAttempts);
		if (nrAttempts > 3) {
			throw new SectioningException(SectioningExceptionType.LOGIN_TOO_MANY_ATTEMPTS);
		}
		try {
			if (userName == null || userName.isEmpty()) throw new SectioningException(SectioningExceptionType.LOGIN_NO_USERNAME);
			
			String studentId = null;
			if (userName.indexOf('/') >= 0) {
				studentId = userName.substring(userName.indexOf('/') + 1);
				userName = userName.substring(0, userName.indexOf('/'));
			}
			
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
					
					if (studentId != null) {
						if (!user.getRoles().contains(Roles.ADMIN_ROLE)) { principal = null; continue; }
						org.hibernate.Session hibSession = StudentDAO.getInstance().createNewSession();
						try {
							List<Student> student = hibSession.createQuery("select m from Student m where m.externalUniqueId = :uid").setString("uid", studentId).list();
							if (student.isEmpty()) { principal = null; continue; };
							for (Student s: student) {
								principal.addStudentId(s.getSession().getUniqueId(), s.getUniqueId());
								principal.setName(s.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle));
							}
						} finally {
							hibSession.close();
						}
					}

					break;
				}
			}
			
			if (principal == null) throw new SectioningException(SectioningExceptionType.LOGIN_FAILED);
			getThreadLocalRequest().getSession().setAttribute("user", principal);
			getThreadLocalRequest().getSession().removeAttribute("login.nrAttempts");
			
			CourseRequestInterface req = getLastRequest();
			if (req != null && req.getCourses().isEmpty())
				setLastRequest(null);
			
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
			if (user != null) {
				principal = new UniTimePrincipal(user.getId(), user.getName());
				getThreadLocalRequest().getSession().setAttribute("user", principal);
			}
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
				if (session != null && OnlineSectioningService.getInstance(session.getUniqueId()) != null)
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
		if (request == null)
			getThreadLocalRequest().getSession().removeAttribute("request");
		else
			getThreadLocalRequest().getSession().setAttribute("request", request);
	}
	
	public String[] lastAcademicSession(boolean sectioning) throws SectioningException {
		if (getThreadLocalRequest().getSession().isNew()) throw new SectioningException(SectioningExceptionType.USER_NOT_LOGGED_IN);
		Long sessionId = getLastSessionId();
		if (sessionId == null) throw new SectioningException(SectioningExceptionType.LAST_ACADEMIC_SESSION_FAILED, "no session was used");
		if (sectioning) {
			OnlineSectioningServer server = OnlineSectioningService.getInstance(sessionId);
			if (server == null) throw new SectioningException(SectioningExceptionType.LAST_ACADEMIC_SESSION_FAILED, "no server data");
			AcademicSessionInfo s = server.getAcademicSession();
			if (s == null) throw new SectioningException(SectioningExceptionType.LAST_ACADEMIC_SESSION_FAILED, "no session info");
			return new String[] {
					String.valueOf(s.getUniqueId()),
					s.getYear(),
					s.getTerm(),
					s.getCampus()
			};
		} else {
			Session session = SessionDAO.getInstance().get(sessionId);
			if (session == null)
				throw new SectioningException(SectioningExceptionType.LAST_ACADEMIC_SESSION_FAILED, "session not found");
			if (!session.getStatusType().canPreRegisterStudents() || session.getStatusType().canSectioningStudents())
				throw new SectioningException(SectioningExceptionType.LAST_ACADEMIC_SESSION_FAILED, "registration is not allowed");
			return new String[] {
					session.getUniqueId().toString(),
					session.getAcademicYear(),
					session.getAcademicTerm(),
					session.getAcademicInitiative()
			};
		}
	}
	
	public CourseRequestInterface lastRequest(Long sessionId) throws SectioningException {
		CourseRequestInterface request = getLastRequest();
		if (request != null && !request.getAcademicSessionId().equals(sessionId)) request = null;
		if (request != null && request.getCourses().isEmpty() && request.getAlternatives().isEmpty()) request = null;
		if (request == null) {
			Long studentId = getStudentId(sessionId);
			if (studentId == null) throw new SectioningException(SectioningExceptionType.NO_STUDENT);
			OnlineSectioningServer server = OnlineSectioningService.getInstance(sessionId);
			if (server != null) {
				CourseRequestInterface lastRequest = server.getRequest(studentId);
				if (lastRequest == null)
					throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
				if (!lastRequest.getCourses().isEmpty())
					return lastRequest;
				throw new SectioningException(SectioningExceptionType.NO_STUDENT_REQUESTS);
			} else {
				org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
				try {
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
									r.setRequestedCourse(ft.toString());
									if (cd.isAlternative())
										request.getAlternatives().add(r);
									else
										request.getCourses().add(r);
								}
							} else if (!cd.getCourseRequests().isEmpty()) {
								r = new CourseRequestInterface.Request();
								for (Iterator<CourseRequest> i = cd.getCourseRequests().iterator(); i.hasNext(); ) {
									CourseRequest course = i.next();
									CourseInfo c = (server == null ? new CourseInfo(course.getCourseOffering()) : server.getCourseInfo(course.getCourseOffering().getUniqueId()));
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
							CourseInfo c = (server == null ? new CourseInfo(enrl.getCourseOffering()) : server.getCourseInfo(enrl.getCourseOffering().getUniqueId()));
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
					sLog.error(e.getMessage(), e);
					throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
				} finally {
					hibSession.close();
				}
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
			OnlineSectioningServer server = OnlineSectioningService.getInstance(sessionId);
			if (server == null) throw new SectioningException(SectioningExceptionType.BAD_SESSION);
			ArrayList<ClassAssignmentInterface.ClassAssignment> ret = server.getAssignment(studentId);
			if (ret == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
			if (!ret.isEmpty()) return ret;
			/*
			Student student = StudentDAO.getInstance().get(studentId, hibSession);
			if (student == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
			Lock lock = server.readLock();
			try {
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
						ca.setSection(section.getName(course.getUniqueId()));
						ret.add(ca);
					}
					if (!ret.isEmpty()) return ret;
				}
			} finally {
				lock.release();
			}
			*/
			throw new SectioningException(SectioningExceptionType.NO_STUDENT_SCHEDULE);
		} catch (Exception e) {
			if (e instanceof SectioningException) throw (SectioningException)e;
			throw new SectioningException(SectioningExceptionType.UNKNOWN, e);
		} finally {
			hibSession.close();
		}
	}

	public Boolean saveRequest(CourseRequestInterface request) throws SectioningException {
		UniTimePrincipal principal = (UniTimePrincipal)getThreadLocalRequest().getSession().getAttribute("user");
		if (principal == null) throw new SectioningException(SectioningExceptionType.ENROLL_NOT_AUTHENTICATED);
		Long studentId = principal.getStudentId(request.getAcademicSessionId());
		OnlineSectioningServer server = OnlineSectioningService.getInstance(request.getAcademicSessionId());
		if (studentId == null && isAdmin())
			studentId = request.getStudentId();
		if (server != null) {
			if ("true".equals(ApplicationProperties.getProperty("unitime.enrollment.enabled","true"))) return false;
			if (!"true".equals(ApplicationProperties.getProperty("unitime.enrollment.requests.save","false"))) return false;
			if (studentId == null)
				throw new SectioningException(SectioningExceptionType.ENROLL_NOT_STUDENT, server.getAcademicSession().toString());
			server.execute(new SaveStudentRequests(studentId, request, true));
			return true;
		} else {
			if (studentId == null)
				throw new SectioningException(SectioningExceptionType.ENROLL_NOT_STUDENT, SessionDAO.getInstance().get(request.getAcademicSessionId()).getLabel());
			org.hibernate.Session hibSession = StudentDAO.getInstance().getSession();
			try {
				Student student = StudentDAO.getInstance().get(studentId, hibSession);
				if (student == null) throw new SectioningException(SectioningExceptionType.BAD_STUDENT_ID);
				SaveStudentRequests.saveRequest(null, new OnlineSectioningHelper(hibSession), student, request, true);
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
	}
	
	public ArrayList<Long> enroll(CourseRequestInterface request, ArrayList<ClassAssignmentInterface.ClassAssignment> currentAssignment) throws SectioningException {
		UniTimePrincipal principal = (UniTimePrincipal)getThreadLocalRequest().getSession().getAttribute("user");
		if (principal == null) throw new SectioningException(SectioningExceptionType.ENROLL_NOT_AUTHENTICATED);
		Long studentId = principal.getStudentId(request.getAcademicSessionId());
		OnlineSectioningServer server = OnlineSectioningService.getInstance(request.getAcademicSessionId());
		if (server == null) throw new SectioningException(SectioningExceptionType.BAD_SESSION);
		if (studentId == null && isAdmin())
			studentId = request.getStudentId();
		if (studentId == null)
			throw new SectioningException(SectioningExceptionType.ENROLL_NOT_STUDENT, server.getAcademicSession().toString());
		if (!"true".equals(ApplicationProperties.getProperty("unitime.enrollment.enabled","true")))
			throw new SectioningException(SectioningExceptionType.FEATURE_NOT_SUPPORTED);
		return new ArrayList<Long>(server.execute(new EnrollStudent(studentId, request, currentAssignment)));
	}

	public Boolean isAdmin() throws SectioningException {
		try {
			User user = Web.getUser(getThreadLocalRequest().getSession());
			if (user == null) throw new SectioningException(SectioningExceptionType.UNKNOWN, "not authenticated");
			return Roles.ADMIN_ROLE.equals(user.getRole());
		} catch  (Exception e) {
			if (e instanceof SectioningException) throw (SectioningException)e;
			sLog.error(e.getMessage(), e);
			throw new SectioningException(SectioningExceptionType.UNKNOWN, e.getMessage());
		}
	}

}