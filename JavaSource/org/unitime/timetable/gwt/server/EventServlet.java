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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.services.EventService;
import org.unitime.timetable.gwt.shared.EventException;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.IdValueInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author Tomas Muller
 */
public class EventServlet extends RemoteServiceServlet implements EventService {
	private static final long serialVersionUID = 7949018510304934636L;
	private static Logger sLog = Logger.getLogger(EventServlet.class);

	public void init() throws ServletException {
	}
	
	public Session findSession(org.hibernate.Session hibSession, String session) throws EventException, PageAccessException {
		try {
			Session ret = SessionDAO.getInstance().get(Long.parseLong(session), hibSession);
			if (ret != null) return ret;
		} catch (NumberFormatException e) {}
		List<Session> sessions = hibSession.createQuery("select s from Session s where " +
				"s.academicTerm || s.academicYear = :term or " +
				"s.academicTerm || s.academicYear || s.academicInitiative = :term").
				setString("term", session).list();
		if (!sessions.isEmpty())
			return sessions.get(0);
		if ("current".equalsIgnoreCase(session)) {
			sessions = hibSession.createQuery("select s from Session s where " +
					"s.eventBeginDate <= :today and s.eventEndDate >= :today").
					setDate("today",new Date()).list();
			if (!sessions.isEmpty())
				return sessions.get(0);
		}
		throw new EventException("Academic session " + session + " not found.");
	}
	
	private void fillInSessionInfo(ResourceInterface resource, Session session) {
		resource.setSessionId(session.getUniqueId());
		resource.setSessionAbbv(session.getAcademicTerm() + session.getAcademicYear() + session.getAcademicInitiative());
		resource.setSessionName(session.getLabel());
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(session.getEventBeginDate());
		while (c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
			c.add(Calendar.DAY_OF_YEAR, -1);
		}
		int sessionYear = session.getSessionStartYear();
		DateFormat df = new SimpleDateFormat("MM/dd");
		while (c.getTime().before(session.getEventEndDate())) {
			int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
			if (c.get(Calendar.YEAR) < sessionYear) {
				Calendar x = Calendar.getInstance(Locale.US);
			    x.set(c.get(Calendar.YEAR),11,31,0,0,0);
			    dayOfYear -= x.get(Calendar.DAY_OF_YEAR);
			} else if (c.get(Calendar.YEAR) > sessionYear) {
				Calendar x = Calendar.getInstance(Locale.US);
			    x.set(sessionYear,11,31,0,0,0);
			    dayOfYear += x.get(Calendar.DAY_OF_YEAR);
			}
			WeekInterface week = new WeekInterface();
			week.setDayOfYear(dayOfYear);
			for (int i = 0; i < 7; i++) {
				week.addDayName(df.format(c.getTime()));
				c.add(Calendar.DAY_OF_YEAR, 1);
			}
			resource.addWeek(week);
		}
	}
	
	private void fillInCalendarUrl(ResourceInterface resource) {
		resource.setCalendar(CalendarServlet.encode("sid=" + resource.getSessionId() + 
				"&type=" + resource.getType().toString().toLowerCase() + "&id=" + resource.getId() + 
				(resource.getExternalId() == null ? "" : "&ext=" + resource.getExternalId())));
		/*
		switch (resource.getType()) {
		case PERSON:
			resource.setCalendar(CalendarServlet.encode("sid=" + resource.getSessionId() + "&uid=" + resource.getExternalId()));
			break;
		default:
			resource.setCalendar(CalendarServlet.encode("sid=" + resource.getSessionId() + "&type=" + resource.getType().toString().toLowerCase() + "&id=" + resource.getId()));
		}
		*/
	}

	@Override
	public ResourceInterface findResource(String session, ResourceType type, String name) throws EventException, PageAccessException {
		try {
			org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
			try {
				Session academicSession = null;
				MenuServlet.UserInfo userInfo = new MenuServlet.UserInfo(getThreadLocalRequest().getSession());
				if ("true".equals(ApplicationProperties.getProperty("unitime.event_timetable.requires_authentication", "true")) &&
					userInfo.getUser() == null)
						throw new PageAccessException(type.getPageTitle().substring(0, 1).toUpperCase() +
								type.getPageTitle().substring(1).toLowerCase() + " is only available to authenticated users.");

				if (session != null && !session.isEmpty()) {
					academicSession = findSession(hibSession, session);
				} else if (userInfo.getSession() != null) {
					academicSession = userInfo.getSession();
				} else {
					throw new EventException("Academic session not provided.");
				}
				
				if (type == null)
					type = ResourceType.PERSON;
				switch (type) {
				case ROOM:
					if ("true".equals(ApplicationProperties.getProperty("unitime.event_timetable.event_rooms_only", "true"))) {
						List<Room> rooms = hibSession.createQuery("select distinct r from Room r " +
								"inner join r.roomDepts rd inner join rd.department.timetableManagers m inner join m.managerRoles mr " +
								"where r.session.uniqueId = :sessionId and rd.control=true and mr.role.reference=:eventMgr and (" +
								"r.buildingAbbv || ' ' || r.roomNumber = :name or r.buildingAbbv || r.roomNumber = :name)")
								.setString("name", name)
								.setLong("sessionId", academicSession.getUniqueId())
								.setString("eventMgr", Roles.EVENT_MGR_ROLE)
								.list();
						if (!rooms.isEmpty()) {
							Room room = rooms.get(0);
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.ROOM);
							ret.setId(room.getUniqueId());
							ret.setAbbreviation(room.getLabel());
							ret.setName(room.getLabel());
							ret.setHint(room.getHtmlHint());
							fillInSessionInfo(ret, room.getSession());
							fillInCalendarUrl(ret);
							return ret;
						}
						List<NonUniversityLocation> locations = hibSession.createQuery("select distinct l from NonUniversityLocation l " +
								"inner join l.roomDepts rd inner join rd.department.timetableManagers m inner join m.managerRoles mr " +
								"where l.session.uniqueId = :sessionId and l.name = :name and " + 
								"rd.control=true and mr.role.reference=:eventMgr"
								)
								.setString("name", name)
								.setLong("sessionId", academicSession.getUniqueId())
								.setString("eventMgr", Roles.EVENT_MGR_ROLE)
								.list();
						if (!locations.isEmpty()) {
							NonUniversityLocation location = locations.get(0);
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.ROOM);
							ret.setId(location.getUniqueId());
							ret.setAbbreviation(location.getLabel());
							ret.setName(location.getLabel());
							ret.setHint(location.getHtmlHint());
							fillInSessionInfo(ret, location.getSession());
							fillInCalendarUrl(ret);
							return ret;
						}
					} else {
						List<Room> rooms = hibSession.createQuery("select distinct r from Room r " +
								"where r.session.uniqueId = :sessionId and (" +
								"r.buildingAbbv || ' ' || r.roomNumber = :name or r.buildingAbbv || r.roomNumber = :name)")
								.setString("name", name)
								.setLong("sessionId", academicSession.getUniqueId())
								.list();
						if (!rooms.isEmpty()) {
							Room room = rooms.get(0);
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.ROOM);
							ret.setId(room.getUniqueId());
							ret.setAbbreviation(room.getLabel());
							ret.setName(room.getLabel());
							ret.setHint(room.getHtmlHint());
							fillInSessionInfo(ret, room.getSession());
							fillInCalendarUrl(ret);
							return ret;
						}
						List<NonUniversityLocation> locations = hibSession.createQuery("select distinct l from NonUniversityLocation l " +
								"where l.session.uniqueId = :sessionId and l.name = :name"
								)
								.setString("name", name)
								.setLong("sessionId", academicSession.getUniqueId())
								.list();
						if (!locations.isEmpty()) {
							NonUniversityLocation location = locations.get(0);
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.ROOM);
							ret.setId(location.getUniqueId());
							ret.setAbbreviation(location.getLabel());
							ret.setName(location.getLabel());
							ret.setHint(location.getHtmlHint());
							fillInSessionInfo(ret, location.getSession());
							fillInCalendarUrl(ret);
							return ret;
						}
					}
					throw new EventException("Unable to find a " + type.getLabel() + " named " + name + ".");
				case SUBJECT:
					List<SubjectArea> subjects = hibSession.createQuery("select s from SubjectArea s where s.session.uniqueId = :sessionId and " +
							"lower(s.subjectAreaAbbreviation) = :name")
							.setString("name", name.toLowerCase()).setLong("sessionId", academicSession.getUniqueId()).list();
					if (!subjects.isEmpty()) {
						SubjectArea subject = subjects.get(0);
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.SUBJECT);
						ret.setId(subject.getUniqueId());
						ret.setAbbreviation(subject.getSubjectAreaAbbreviation());
						ret.setName(subject.getLongTitle() == null ? subject.getShortTitle() : subject.getLongTitle());
						fillInSessionInfo(ret, subject.getSession());
						fillInCalendarUrl(ret);
						return ret;
					}
				case COURSE:
					List<CourseOffering> courses = hibSession.createQuery("select c from CourseOffering c inner join c.subjectArea s where s.session.uniqueId = :sessionId and " +
							"lower(s.subjectAreaAbbreviation || ' ' || c.courseNbr) = :name and c.instructionalOffering.notOffered = false")
							.setString("name", name.toLowerCase()).setLong("sessionId", academicSession.getUniqueId()).list();
					if (!courses.isEmpty()) {
						CourseOffering course = courses.get(0);
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.COURSE);
						ret.setId(course.getUniqueId());
						ret.setAbbreviation(course.getCourseName());
						ret.setName(course.getTitle() == null ? course.getCourseName() : course.getTitle());
						fillInSessionInfo(ret, course.getSubjectArea().getSession());
						fillInCalendarUrl(ret);
						return ret;
					}
					throw new EventException("Unable to find a " + type.getLabel() + " named " + name + ".");
				case CURRICULUM:
					List<Curriculum> curricula = hibSession.createQuery("select c from Curriculum c where c.department.session.uniqueId = :sessionId and " +
							"lower(c.abbv) = :name or lower(c.name) = :name")
							.setString("name", name.toLowerCase()).setLong("sessionId", academicSession.getUniqueId()).list();
					if (!curricula.isEmpty()) {
						Curriculum curriculum = curricula.get(0);
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.CURRICULUM);
						ret.setId(curriculum.getUniqueId());
						ret.setAbbreviation(curriculum.getAbbv());
						ret.setName(curriculum.getName());
						fillInSessionInfo(ret, curriculum.getDepartment().getSession());
						fillInCalendarUrl(ret);
						return ret;
					}
					List<CurriculumClassification> classifications = hibSession.createQuery("select f from CurriculumClassification f inner join f.curriculum c where " +
							"c.department.session.uniqueId = :sessionId and (" +
							"lower(c.abbv || '/' || f.name) = :name or lower(c.name || '/' || f.name) = :name or " +
							"lower(c.abbv || '/' || f.academicClassification.code) = :name or lower(c.name || '/' || f.academicClassification.code) = :name or " + 
							"lower(c.abbv || '/' || f.academicClassification.name) = :name or lower(c.name || '/' || f.academicClassification.name) = :name or " +
							"lower(c.abbv || ' ' || f.name) = :name or lower(c.name || ' ' || f.name) = :name or " +
							"lower(c.abbv || ' ' || f.academicClassification.code) = :name or lower(c.name || ' ' || f.academicClassification.code) = :name or " + 
							"lower(c.abbv || ' ' || f.academicClassification.name) = :name or lower(c.name || ' ' || f.academicClassification.name) = :name or " +
							"lower(c.abbv || f.name) = :name or lower(c.name || f.name) = :name or " +
							"lower(c.abbv || f.academicClassification.code) = :name or lower(c.name || f.academicClassification.code) = :name or " + 
							"lower(c.abbv || f.academicClassification.name) = :name or lower(c.name || f.academicClassification.name) = :name)")
							.setString("name", name.toLowerCase()).setLong("sessionId", academicSession.getUniqueId()).list();
					if (!classifications.isEmpty()) {
						CurriculumClassification classification = classifications.get(0);
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.CURRICULUM);
						ret.setId(classification.getUniqueId());
						ret.setAbbreviation(classification.getCurriculum().getAbbv() + " " + classification.getAcademicClassification().getCode());
						ret.setName(classification.getCurriculum().getName() + " " + classification.getAcademicClassification().getName());
						fillInSessionInfo(ret, classification.getCurriculum().getDepartment().getSession());
						fillInCalendarUrl(ret);
						return ret;
					}
					throw new EventException("Unable to find a " + type.getLabel() + " named " + name + ".");
				case DEPARTMENT:
					List<Department> departments = hibSession.createQuery("select d from Department d where d.session.uniqueId = :sessionId and " +
							"lower(d.deptCode) = :name or lower(d.abbreviation) = :name")
							.setString("name", name.toLowerCase()).setLong("sessionId", academicSession.getUniqueId()).list();
					if (!departments.isEmpty()) {
						Department department = departments.get(0);
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.DEPARTMENT);
						ret.setId(department.getUniqueId());
						ret.setAbbreviation(department.getAbbreviation() == null ? department.getDeptCode() : department.getAbbreviation());
						ret.setName(department.getName());
						fillInSessionInfo(ret, department.getSession());
						fillInCalendarUrl(ret);
						return ret;
					}
					throw new EventException("Unable to find a " + type.getLabel() + " named " + name + ".");
				case PERSON:
					if (userInfo.getUser() == null)
						throw new EventException(type.getPageTitle().substring(0, 1).toUpperCase() +
								type.getPageTitle().substring(1).toLowerCase() + " is only available to authenticated users.");
					if (!canLookupPeople()) {
						if (name != null && !name.isEmpty() && !name.equals(userInfo.getUser().getId()))
							throw new EventException("It is not allowed to access a timetable of someone else.");
						name = userInfo.getUser().getId();
					} else if (name == null || name.isEmpty()) {
						name = userInfo.getUser().getId();
					}
					List<Student> students = hibSession.createQuery("select s from Student s where s.session.uniqueId = :sessionId and " +
							"s.externalUniqueId = :name or lower(s.email) = lower(:name)")
							.setString("name", name).setLong("sessionId", academicSession.getUniqueId()).list();
					if (!students.isEmpty()) {
						Student student = students.get(0);
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.PERSON);
						ret.setId(student.getUniqueId());
						ret.setAbbreviation(student.getName(DepartmentalInstructor.sNameFormatShort));
						ret.setName(student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle));
						ret.setExternalId(student.getExternalUniqueId());
						fillInSessionInfo(ret, student.getSession());
						fillInCalendarUrl(ret);
						return ret;
					}
					List<DepartmentalInstructor> instructors = hibSession.createQuery("select i from DepartmentalInstructor i where i.department.session.uniqueId = :sessionId and " +
							"i.externalUniqueId = :name or lower(i.careerAcct) = lower(:name) or lower(i.email) = lower(:name)")
							.setString("name", name).setLong("sessionId", academicSession.getUniqueId()).list();
					if (!instructors.isEmpty()) {
						DepartmentalInstructor instructor = instructors.get(0);
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.PERSON);
						ret.setId(instructor.getUniqueId());
						ret.setAbbreviation(instructor.getName(DepartmentalInstructor.sNameFormatShort));
						ret.setName(instructor.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle));
						ret.setExternalId(instructor.getExternalUniqueId());
						fillInSessionInfo(ret, instructor.getDepartment().getSession());
						fillInCalendarUrl(ret);
						return ret;
					}
					List<EventContact> contacts = hibSession.createQuery("select c from EventContact c where " +
							"c.externalUniqueId = :name or lower(c.emailAddress) = lower(:name)")
							.setString("name", name).list();
					if (!contacts.isEmpty()) {
						EventContact contact = contacts.get(0);
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.PERSON);
						ret.setId(contact.getUniqueId());
						ret.setAbbreviation(contact.getName());
						ret.setName(contact.getName());
						ret.setExternalId(contact.getExternalUniqueId());
						fillInSessionInfo(ret, academicSession);
						fillInCalendarUrl(ret);
						return ret;
					}
					throw new EventException("No events found in " + academicSession.getLabel() + ".");
				default:
					throw new EventException("Resource type " + type.getLabel() + " not supported.");
				}
			} finally {
				hibSession.close();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (EventException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new EventException("Unable to find a " + type.getLabel() + " named " + name + ": " + e.getMessage());
		}
	}
	
	@Override
	public List<EventInterface> findEvents(ResourceInterface resource) throws EventException, PageAccessException {
		return findEvents(resource, true);
	}	

	public List<EventInterface> findEvents(ResourceInterface resource, boolean checkAuthentication) throws EventException, PageAccessException {
		try {
			org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
			boolean suffix = "true".equals(ApplicationProperties.getProperty("tmtbl.exam.report.suffix","false"));
			try {
				if (checkAuthentication) {
					MenuServlet.UserInfo userInfo = new MenuServlet.UserInfo(getThreadLocalRequest().getSession());
					if ("true".equals(ApplicationProperties.getProperty("unitime.event_timetable.requires_authentication", "true")) && userInfo.getUser() == null)
						throw new PageAccessException(resource.getType().getPageTitle().substring(0, 1).toUpperCase() +
								resource.getType().getPageTitle().substring(1).toLowerCase() + " is only available to authenticated users.");
				}

				List<Meeting> meetings = null;
				Session session = SessionDAO.getInstance().get(resource.getSessionId(), hibSession);
				Collection<Long> curriculumCourses = null;
				Collection<Long> curriculumConfigs = null;
				Collection<Long> curriculumClasses = null;
				Department department = null;
				switch (resource.getType()) {
				case ROOM:
					meetings = (List<Meeting>)hibSession.createQuery("select m from Meeting m, Location l where " +
							"l.uniqueId = :resourceId and m.locationPermanentId = l.permanentId " +
							"and m.meetingDate >= l.session.eventBeginDate and m.meetingDate <= l.session.eventEndDate and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).list();
					break;
				case SUBJECT:
					meetings = (List<Meeting>)hibSession.createQuery("select m from ClassEvent e inner join e.meetings m inner join " +
							"e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.subjectArea.uniqueId = :resourceId " +
							"and m.meetingDate >= co.subjectArea.session.eventBeginDate and " +
							"m.meetingDate <= co.subjectArea.session.eventEndDate and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).list();
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:courseType and o.ownerId = co.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
							"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:classType and o.ownerId = c.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
							"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:configType and o.ownerId = cfg.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:courseType and o.ownerId = co.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
							"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:classType and o.ownerId = c.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
							"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:configType and o.ownerId = cfg.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					break;
				case COURSE:
					meetings = (List<Meeting>)hibSession.createQuery("select m from ClassEvent e inner join e.meetings m inner join " +
							"e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.uniqueId = :resourceId " +
							"and m.meetingDate >= co.subjectArea.session.eventBeginDate and " +
							"m.meetingDate <= co.subjectArea.session.eventEndDate and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).list();
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co where " +
							"co.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:courseType and o.ownerId = co.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co where " +
							"co.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
							"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:classType and o.ownerId = c.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
							"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg where " +
							"co.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:configType and o.ownerId = cfg.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co where " +
							"co.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:courseType and o.ownerId = co.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co where " +
							"co.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
							"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:classType and o.ownerId = c.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
							"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg where " +
							"co.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:configType and o.ownerId = cfg.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					break;			
				case CURRICULUM:
					curriculumCourses = (List<Long>)hibSession.createQuery("select cc.course.uniqueId from CurriculumCourse cc where cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
							.setLong("resourceId", resource.getId()).list();
					meetings = (List<Meeting>)hibSession.createQuery("select m from ClassEvent e inner join e.meetings m inner join " +
							"e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc where " +
							"co = cc.course and (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId) " +
							"and m.meetingDate >= co.subjectArea.session.eventBeginDate and " +
							"m.meetingDate <= co.subjectArea.session.eventEndDate and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).list();
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co, CurriculumCourse cc where " +
							"co = cc.course and (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId) and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:courseType and o.ownerId = co.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co, CurriculumCourse cc where " +
							"co = cc.course and (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId) and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
							"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc where " +
							"co = cc.course and (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId) and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:classType and o.ownerId = c.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
							"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg, CurriculumCourse cc where " +
							"co = cc.course and (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId) and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:configType and o.ownerId = cfg.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co, CurriculumCourse cc where " +
							"co = cc.course and (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId) and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:courseType and o.ownerId = co.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co, CurriculumCourse cc where " +
							"co = cc.course and (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId) and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
							"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc where " +
							"co = cc.course and (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId) and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:classType and o.ownerId = c.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
							"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg, CurriculumCourse cc where " +
							"co = cc.course and (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId) and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:configType and o.ownerId = cfg.uniqueId and m.approvedDate is not null")
							.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					break;
				case DEPARTMENT:
					department = DepartmentDAO.getInstance().get(resource.getId(), hibSession);
					if (department.isExternalManager()) {
						meetings = (List<Meeting>)hibSession.createQuery("select m from ClassEvent e inner join e.meetings m inner join e.clazz.managingDept d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= d.session.eventBeginDate and m.meetingDate <= d.session.eventEndDate and m.approvedDate is not null")
								.setLong("resourceId", resource.getId()).list();
					} else {
						meetings = (List<Meeting>)hibSession.createQuery("select m from ClassEvent e inner join e.meetings m inner join " +
								"e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= d.session.eventBeginDate and m.meetingDate <= d.session.eventEndDate and m.approvedDate is not null")
								.setLong("resourceId", resource.getId()).list();
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:courseType and o.ownerId = co.uniqueId and m.approvedDate is not null")
								.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId and m.approvedDate is not null")
								.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
								"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:classType and o.ownerId = c.uniqueId and m.approvedDate is not null")
								.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
								"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:configType and o.ownerId = cfg.uniqueId and m.approvedDate is not null")
								.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:courseType and o.ownerId = co.uniqueId and m.approvedDate is not null")
								.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId and m.approvedDate is not null")
								.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
								"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:classType and o.ownerId = c.uniqueId and m.approvedDate is not null")
								.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
								"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:configType and o.ownerId = cfg.uniqueId and m.approvedDate is not null")
								.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					}
					break;
				case PERSON:
					User user = Web.getUser(getThreadLocalRequest().getSession());
					boolean overrideStatus = user != null && (Roles.ADMIN_ROLE.equals(user.getRole()) || Roles.DEPT_SCHED_MGR_ROLE.equals(user.getRole()));
					boolean canViewFinalExams = overrideStatus || session.getStatusType().canNoRoleReportExamFinal();
					boolean canViewMidtermExams = overrideStatus || session.getStatusType().canNoRoleReportExamMidterm();
					boolean canViewClasses = overrideStatus || session.getStatusType().canNoRoleReportClass();
					curriculumCourses = new HashSet<Long>();
					curriculumConfigs = new HashSet<Long>();
					curriculumClasses = new HashSet<Long>();
					meetings = new ArrayList<Meeting>();
					for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)hibSession.createQuery("select i from DepartmentalInstructor i " +
							"where i.externalUniqueId = :externalId and i.department.session.uniqueId = :sessionId").
							setLong("sessionId", resource.getSessionId()).setString("externalId", resource.getExternalId()).list()) {
	                    if (canViewFinalExams)
		                	for (Exam exam: instructor.getExams(Exam.sExamTypeFinal)) {
		                		if (exam.getEvent() != null)
		                			meetings.addAll(exam.getEvent().getMeetings());
		                		for (ExamOwner owner: exam.getOwners()) {
		                			if (curriculumCourses.add(owner.getCourse().getUniqueId()));
					    			if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass) curriculumClasses.add(owner.getOwnerId());
					    			if (owner.getOwnerType() == ExamOwner.sOwnerTypeConfig) curriculumConfigs.add(owner.getOwnerId());
		                		}
		                	}
	                    if (canViewMidtermExams)
		                	for (Exam exam: instructor.getExams(Exam.sExamTypeMidterm)) {
		                		if (exam.getEvent() != null)
		                			meetings.addAll(exam.getEvent().getMeetings());
		                		for (ExamOwner owner: exam.getOwners()) {
		                			if (curriculumCourses.add(owner.getCourse().getUniqueId()));
					    			if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass) curriculumClasses.add(owner.getOwnerId());
					    			if (owner.getOwnerType() == ExamOwner.sOwnerTypeConfig) curriculumConfigs.add(owner.getOwnerId());
		                		}
		                	}
	                    if (canViewClasses) {
	                        for (ClassInstructor ci: instructor.getClasses()) {
	                        	if (ci.getClassInstructing().getEvent() != null) {
	                        		meetings.addAll(ci.getClassInstructing().getEvent().getMeetings());
	                        	}
	                        	for (CourseOffering course: ci.getClassInstructing().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
	                        		curriculumCourses.add(course.getUniqueId());
	                        	}
	                        }
	                    }
					}
                    for (Student student: (List<Student>)hibSession.createQuery("select s from Student s where " +
                    		"s.externalUniqueId=:externalId and s.session.uniqueId = :sessionId").
                    		setLong("sessionId", resource.getSessionId()).setString("externalId", resource.getExternalId()).list()) {
                    	if (canViewFinalExams) {
                    		for (Exam exam: student.getExams(Exam.sExamTypeFinal))
                    			if (exam.getEvent() != null)
                        			meetings.addAll(exam.getEvent().getMeetings());
                    	}
                    	if (canViewMidtermExams) {
                    		for (Exam exam: student.getExams(Exam.sExamTypeMidterm))
                    			if (exam.getEvent() != null)
                        			meetings.addAll(exam.getEvent().getMeetings());
                    	}
                    	if (canViewClasses) {
                            for (StudentClassEnrollment sce: student.getClassEnrollments()) {
                            	if (sce.getClazz().getEvent() != null)
                        			meetings.addAll(sce.getClazz().getEvent().getMeetings());
                        		curriculumConfigs.add(sce.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getUniqueId());
                        		curriculumClasses.add(sce.getClazz().getUniqueId());
                    			curriculumCourses.add(sce.getCourseOffering().getUniqueId());
                            }
                    	}
    					meetings.addAll(
    							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
    							"Student s inner join s.classEnrollments e where " +
    							"s.uniqueId = :studentId and o.ownerType=:courseType and o.ownerId = e.courseOffering.uniqueId and m.approvedDate is not null")
    							.setLong("studentId", student.getUniqueId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
    					meetings.addAll(
    							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
    							"Student s inner join s.classEnrollments e where " +
    							"s.uniqueId = :studentId and o.ownerType=:offeringType and o.ownerId = e.courseOffering.instructionalOffering.uniqueId and m.approvedDate is not null")
    							.setLong("studentId", student.getUniqueId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
    					meetings.addAll(
    							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
    							"Student s inner join s.classEnrollments e where " +
    							"s.uniqueId = :studentId and o.ownerType=:offeringType and o.ownerId = e.clazz.uniqueId and m.approvedDate is not null")
    							.setLong("studentId", student.getUniqueId()).setInteger("offeringType", ExamOwner.sOwnerTypeClass).list());
    					meetings.addAll(
    							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
    							"Student s inner join s.classEnrollments e where " +
    							"s.uniqueId = :studentId and o.ownerType=:courseType and o.ownerId = e.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and m.approvedDate is not null")
    							.setLong("studentId", student.getUniqueId()).setInteger("courseType", ExamOwner.sOwnerTypeConfig).list());
                    }
                    meetings.addAll(
                    		(List<Meeting>)hibSession.createQuery("select m from Meeting m, Session s where s.uniqueId = :sessionId and " +
                    				"m.event.class in (CourseEvent, SpecialEvent) and " +
                    				"m.event.mainContact.externalUniqueId = :externalId and " +
                    				"m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.approvedDate is not null")
                    				.setString("externalId", resource.getExternalId()).setLong("sessionId", resource.getSessionId()).list());
                    meetings.addAll(
                    		(List<Meeting>)hibSession.createQuery("select m from Meeting m inner join m.event.additionalContacts c, Session s where s.uniqueId = :sessionId and " +
                    				"c.externalUniqueId = :externalId and " +
                    				"m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.approvedDate is not null")
                    				.setString("externalId", resource.getExternalId()).setLong("sessionId", resource.getSessionId()).list());
                    /*
                    meetings.addAll(
                    		(List<Meeting>)hibSession.createQuery("select distinct m from Meeting m, EventContact c, Session s where s.uniqueId = :sessionId and " +
                    				"c.externalUniqueId = :externalId and c.emailAddress is not null and " +
                    				"lower(m.event.email) like '%' || lower(c.emailAddress) || '%' and " +
                    				"m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.approvedDate is not null")
                    				.setString("externalId", resource.getExternalId()).setLong("sessionId", resource.getSessionId()).list());
                    */
                    break;
				default:
					throw new EventException("Resource type " + resource.getType().getLabel() + " not supported.");
				}
				
				Date now = new Date();
				List<EventInterface> ret = new ArrayList<EventInterface>();
				Hashtable<Long, EventInterface> events = new Hashtable<Long, EventInterface>();
				for (Meeting m: meetings) {
					EventInterface event = events.get(m.getEvent().getUniqueId());
					if (event == null) {
						event = new EventInterface();
						event.setId(m.getEvent().getUniqueId());
						event.setName(m.getEvent().getEventName());
						event.setType(m.getEvent().getEventTypeAbbv());
						events.put(m.getEvent().getUniqueId(), event);
						ret.add(event);
						
						if (m.getEvent().getMainContact() != null)
							event.setContact(
									(m.getEvent().getMainContact().getLastName() == null ? "" : m.getEvent().getMainContact().getLastName() + ", ") +
									(m.getEvent().getMainContact().getFirstName() == null ? "" : m.getEvent().getMainContact().getFirstName()) + 
									(m.getEvent().getMainContact().getMiddleName() == null ? "" : " " + m.getEvent().getMainContact().getMiddleName()));
						
						if (m.getEvent().getSponsoringOrganization() != null) {
							event.setSponsor(m.getEvent().getSponsoringOrganization().getName());
							event.setEmail(m.getEvent().getSponsoringOrganization().getEmail());
						}
						
				    	if (Event.sEventTypeClass == m.getEvent().getEventType()) {
				    		ClassEvent ce = ClassEventDAO.getInstance().get(m.getEvent().getUniqueId(), hibSession);
				    		Class_ clazz = ce.getClazz();
				    		if (clazz.getDisplayInstructor()) {
				    			String instructor = "", email = "";
				    			for (ClassInstructor i: clazz.getClassInstructors()) {
				    				if (!instructor.isEmpty()) { instructor += "|"; email += "|"; }
				    				instructor += Constants.toInitialCase(i.nameLastNameFirst());
				    				email += (i.getInstructor().getEmail() == null ? "-" : i.getInstructor().getEmail());
				    			}
				    			event.setInstructor(instructor);
				    			event.setEmail(email);
				    		}
				    		CourseOffering correctedOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
				    		List<CourseOffering> courses = new ArrayList<CourseOffering>(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings());
				    		switch (resource.getType()) {
				    		case SUBJECT:
			    				for (Iterator<CourseOffering> i = courses.iterator(); i.hasNext(); ) {
			    					CourseOffering co = i.next();
			    					if (co.getSubjectArea().getUniqueId().equals(resource.getId())) {
			    						if (!correctedOffering.getSubjectArea().getUniqueId().equals(resource.getId()))
			    							correctedOffering = co;
			    					} else {
			    						i.remove();
			    					}
			    				}
				    			break;
				    		case COURSE:
			    				for (Iterator<CourseOffering> i = courses.iterator(); i.hasNext(); ) {
			    					CourseOffering co = i.next();
			    					if (co.getUniqueId().equals(resource.getId())) {
			    						if (!correctedOffering.getUniqueId().equals(resource.getId()))
			    							correctedOffering = co;
			    					} else {
			    						i.remove();
			    					}
			    				}
				    			break;
				    		case DEPARTMENT:
				    			if (department.isExternalManager()) break;
			    				for (Iterator<CourseOffering> i = courses.iterator(); i.hasNext(); ) {
			    					CourseOffering co = i.next();
			    					if (co.getSubjectArea().getDepartment().getUniqueId().equals(resource.getId())) {
			    						if (!correctedOffering.getSubjectArea().getDepartment().getUniqueId().equals(resource.getId()))
			    							correctedOffering = co;
			    					} else {
			    						i.remove();
			    					}
			    				}
				    			break;
				    		case CURRICULUM:
				    		case PERSON:
			    				for (Iterator<CourseOffering> i = courses.iterator(); i.hasNext(); ) {
			    					CourseOffering co = i.next();
			    					if (curriculumCourses.contains(co.getUniqueId())) {
			    						if (!curriculumCourses.contains(correctedOffering.getUniqueId()))
			    							correctedOffering = co;
			    					} else {
			    						i.remove();
			    					}
			    				}
				    			break;
				    		}
				    		courses.remove(correctedOffering);
				    		event.addCourseName(correctedOffering.getCourseName());
				    		event.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
				    		event.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
				    		String section = (suffix && clazz.getClassSuffix(correctedOffering) != null ? clazz.getClassSuffix(correctedOffering) : clazz.getSectionNumberString(hibSession));
				    		event.addExternalId(section);
				    		if (clazz.getClassSuffix(correctedOffering) == null) {
					    		event.setName(clazz.getClassLabel(correctedOffering));
				    		} else {
				    			event.setName(correctedOffering.getCourseName() + " " + section);
				    		}
			    			for (CourseOffering co: courses) {
					    		event.addCourseName(co.getCourseName());
					    		event.addExternalId(suffix && clazz.getClassSuffix(co) != null ? clazz.getClassSuffix(co) : clazz.getSectionNumberString(hibSession));
			    			}
				    	} else if (Event.sEventTypeFinalExam == m.getEvent().getEventType() || Event.sEventTypeMidtermExam == m.getEvent().getEventType()) {
				    		ExamEvent xe = ExamEventDAO.getInstance().get(m.getEvent().getUniqueId(), hibSession);
			    			String instructor = "", email = "";;
			    			for (DepartmentalInstructor i: xe.getExam().getInstructors()) {
			    				if (!instructor.isEmpty()) { instructor += "|"; email += "|"; }
			    				instructor += Constants.toInitialCase(i.nameLastNameFirst());
			    				email += (i.getEmail() == null ? "" : i.getEmail());
			    			}
			    			event.setInstructor(instructor);
			    			event.setEmail(email);
			    			for (ExamOwner owner: new TreeSet<ExamOwner>(xe.getExam().getOwners())) {
			    				courses: for(CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
						    		switch (resource.getType()) {
						    		case SUBJECT:
						    			if (!course.getSubjectArea().getUniqueId().equals(resource.getId())) continue courses;
						    			break;
						    		case COURSE:
						    			if (!course.getUniqueId().equals(resource.getId())) continue courses;
						    			break;
						    		case DEPARTMENT:
						    			if (department.isExternalManager()) break courses;
						    			if (!course.getSubjectArea().getDepartment().getUniqueId().equals(resource.getId())) continue courses;
						    			break;
						    		case CURRICULUM:
						    			if (!curriculumCourses.contains(course.getUniqueId())) continue courses;
						    			break;
						    		case PERSON:
						    			if (!curriculumCourses.contains(course.getUniqueId())) continue courses;
						    			if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass && !curriculumClasses.contains(owner.getOwnerId())) continue;
						    			if (owner.getOwnerType() == ExamOwner.sOwnerTypeConfig && !curriculumConfigs.contains(owner.getOwnerId())) continue;
						    			break;
						    		}
				    				String courseName = owner.getCourse().getCourseName();
				    				String label = owner.getLabel();
				    				if (label.startsWith(courseName)) {
				    					label = label.substring(courseName.length());
				    				}
				    				event.addCourseName(course.getCourseName());
				    				event.addExternalId(label.trim());
			    				}
			    			}
			    			if (event.hasCourseNames() && event.getCourseNames().size() == 1 && resource.getType() == ResourceType.PERSON)
		    					event.setName((event.getCourseNames().get(0) + " " + event.getExternalIds().get(0)).trim());
				    	}
					}
					MeetingInterface meeting = new MeetingInterface();
					meeting.setId(m.getUniqueId());
					meeting.setMeetingDate(new SimpleDateFormat("MM/dd").format(m.getMeetingDate()));
					meeting.setDayOfWeek(Constants.getDayOfWeek(m.getMeetingDate()));
					meeting.setStartTime(m.getStartTime().getTime());
					meeting.setStopTime(m.getStopTime().getTime());
					Calendar c = Calendar.getInstance(Locale.US);
					c.setTime(m.getMeetingDate());
					int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
					int sessionYear = session.getSessionStartYear();
					if (c.get(Calendar.YEAR) < sessionYear) {
						Calendar x = Calendar.getInstance(Locale.US);
					    x.set(c.get(Calendar.YEAR),11,31,0,0,0);
					    dayOfYear -= x.get(Calendar.DAY_OF_YEAR);
					} else if (c.get(Calendar.YEAR) > sessionYear) {
						Calendar x = Calendar.getInstance(Locale.US);
					    x.set(sessionYear,11,31,0,0,0);
					    dayOfYear += x.get(Calendar.DAY_OF_YEAR);
					}
					meeting.setDayOfYear(dayOfYear);
					meeting.setMeetingTime(m.startTime() + " - " + m.stopTime());
					meeting.setStartSlot(m.getStartPeriod());
					meeting.setEndSlot(m.getStopPeriod());
					meeting.setPast(m.getStartTime().before(now));
					if (m.isApproved())
						meeting.setApprovalDate(new SimpleDateFormat("yy/MM/dd").format(m.getApprovedDate()));
					if (resource.getType() == ResourceType.ROOM)
						meeting.setLocation(resource);
					else if (m.getLocation() != null) {
						ResourceInterface location = new ResourceInterface();
						location.setType(ResourceType.ROOM);
						location.setId(m.getLocation().getUniqueId());
						location.setName(m.getLocation().getLabel());
						location.setSessionId(m.getLocation().getSession().getUniqueId());
						location.setSessionName(m.getLocation().getSession().getLabel());
						location.setHint(m.getLocation().getHtmlHint());
						meeting.setLocation(location);
					}
					event.addMeeting(meeting);
				}
				return ret;
			} finally {
				hibSession.close();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (EventException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new EventException("Unable to find events for " + resource + ": " + e.getMessage());
		}
	}

	@Override
	public List<IdValueInterface> findSessions(String term) throws EventException, PageAccessException {
		try {
			org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
			try {
				if ("true".equals(ApplicationProperties.getProperty("unitime.event_timetable.requires_authentication", "true"))) {
					User user = Web.getUser(getThreadLocalRequest().getSession());
					if (user == null) throw new PageAccessException(
							getThreadLocalRequest().getSession().isNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
				}
				Session selected = null;
				if (term != null) {
					try {
						selected = findSession(hibSession, term);
					} catch (EventException e) {}
				} else {
					MenuServlet.UserInfo userInfo = new MenuServlet.UserInfo(getThreadLocalRequest().getSession());
					if (userInfo.getSession() != null)
						selected = userInfo.getSession();
				}
				if (selected == null)
					try {
						selected = findSession(hibSession, "current");
					} catch (EventException e) {}
				List<IdValueInterface> ret = new ArrayList<IdValueInterface>();
				TreeSet<Session> sessions = new TreeSet<Session>(hibSession.createQuery(
						"select distinct s from Session s, RoomTypeOption o where o.session = s and o.status = 1"
						).list());
				for (Session session: sessions) {
					if (session.getStatusType() == null || session.getStatusType().isTestSession()) continue;
					IdValueInterface idVal = new IdValueInterface(session.getUniqueId().toString(), session.getLabel()); 
					if (session.equals(selected))
						idVal.setSelected(true);
					ret.add(idVal);
				}
				if (ret.isEmpty()) throw new EventException("No academic session is available.");
				return ret;
			} finally {
				hibSession.close();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (EventException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new EventException("No academic session available: " + e.getMessage());
		}
	}

	@Override
	public List<ResourceInterface> findResources(String session, ResourceType type, String query, int limit) throws EventException, PageAccessException {
		try {
			if (query == null) query = "";
			org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
			try {
				
				MenuServlet.UserInfo userInfo = new MenuServlet.UserInfo(getThreadLocalRequest().getSession());
				if ("true".equals(ApplicationProperties.getProperty("unitime.event_timetable.requires_authentication", "true")) &&
					userInfo.getUser() == null)
						throw new PageAccessException(type.getPageTitle().substring(0, 1).toUpperCase() +
								type.getPageTitle().substring(1).toLowerCase() + " is only available to authenticated users.");

				Session academicSession = findSession(hibSession, session);
				List<ResourceInterface> resources = new ArrayList<ResourceInterface>();
				switch (type) {
				case ROOM:
					if ("true".equals(ApplicationProperties.getProperty("unitime.event_timetable.event_rooms_only", "true"))) {
						List<Room> rooms = hibSession.createQuery("select distinct r from Room r " +
								"inner join r.roomDepts rd inner join rd.department.timetableManagers m inner join m.managerRoles mr, " +
								"RoomTypeOption o where r.session.uniqueId = :sessionId and " +
								"rd.control=true and mr.role.reference=:eventMgr and " + 
								"o.status = 1 and o.roomType = r.roomType and o.session = r.session and (" +
								"lower(r.roomNumber) like :name or lower(r.buildingAbbv || ' ' || r.roomNumber) like :name or lower(r.buildingAbbv || r.roomNumber) like :name) " +
								"order by r.buildingAbbv, r.roomNumber")
								.setString("name", query.toLowerCase() + "%")
								.setLong("sessionId", academicSession.getUniqueId())
								.setString("eventMgr", Roles.EVENT_MGR_ROLE)
								.setMaxResults(limit).list();
						for (Room room: rooms) {
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.ROOM);
							ret.setId(room.getUniqueId());
							ret.setAbbreviation(room.getLabel());
							ret.setName(room.getLabel());
							if (room.getDisplayName() != null && !room.getDisplayName().isEmpty()) {
								ret.setTitle(room.getLabel() + " - " + room.getDisplayName());
							} else {
								ret.setTitle(room.getLabel() + " - " + room.getRoomTypeLabel() + (room.getCapacity() > 1 ? " (" + room.getCapacity() + " seats)" : ""));
							}
							fillInSessionInfo(ret, room.getSession());
							fillInCalendarUrl(ret);
							resources.add(ret);
						}
						List<NonUniversityLocation> locations = hibSession.createQuery("select distinct l from NonUniversityLocation l " +
								"inner join l.roomDepts rd inner join rd.department.timetableManagers m inner join m.managerRoles mr, " +
								"RoomTypeOption o where " +
								"rd.control=true and mr.role.reference=:eventMgr and " + 
								"l.session.uniqueId = :sessionId and o.status = 1 and o.roomType = l.roomType and o.session = l.session and lower(l.name) like :name " +
								"order by l.name")
								.setString("name", query.toLowerCase() + "%")
								.setLong("sessionId", academicSession.getUniqueId())
								.setString("eventMgr", Roles.EVENT_MGR_ROLE)
								.setMaxResults(limit).list();
						for (NonUniversityLocation location: locations) {
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.ROOM);
							ret.setId(location.getUniqueId());
							ret.setAbbreviation(location.getLabel());
							ret.setName(location.getLabel());
							if (location.getDisplayName() != null && !location.getDisplayName().isEmpty()) {
								ret.setTitle(location.getLabel() + " - " + location.getDisplayName());
							} else {
								ret.setTitle(location.getLabel() + " - " + location.getRoomTypeLabel() + (location.getCapacity() > 1 ? " (" + location.getCapacity() + " seats)" : ""));
							}
							fillInSessionInfo(ret, location.getSession());
							fillInCalendarUrl(ret);
							resources.add(ret);
						}
						Collections.sort(resources);
						if (limit > 0 && resources.size() > limit) {
							resources = new ArrayList<ResourceInterface>(resources.subList(0, limit));
						}
					} else {
						List<Room> rooms = hibSession.createQuery("select distinct r from Room r " +
								"where r.session.uniqueId = :sessionId and (" +
								"lower(r.roomNumber) like :name or lower(r.buildingAbbv || ' ' || r.roomNumber) like :name or lower(r.buildingAbbv || r.roomNumber) like :name) " +
								"order by r.buildingAbbv, r.roomNumber")
								.setString("name", query.toLowerCase() + "%")
								.setLong("sessionId", academicSession.getUniqueId())
								.setMaxResults(limit).list();
						for (Room room: rooms) {
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.ROOM);
							ret.setId(room.getUniqueId());
							ret.setAbbreviation(room.getLabel());
							ret.setName(room.getLabel());
							if (room.getDisplayName() != null && !room.getDisplayName().isEmpty()) {
								ret.setTitle(room.getLabel() + " - " + room.getDisplayName());
							} else {
								ret.setTitle(room.getLabel() + " - " + room.getRoomTypeLabel() + (room.getCapacity() > 1 ? " (" + room.getCapacity() + " seats)" : ""));
							}
							fillInSessionInfo(ret, room.getSession());
							fillInCalendarUrl(ret);
							resources.add(ret);
						}
						List<NonUniversityLocation> locations = hibSession.createQuery("select distinct l from NonUniversityLocation l where " +
								"l.session.uniqueId = :sessionId and lower(l.name) like :name " +
								"order by l.name")
								.setString("name", query.toLowerCase() + "%")
								.setLong("sessionId", academicSession.getUniqueId())
								.setMaxResults(limit).list();
						for (NonUniversityLocation location: locations) {
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.ROOM);
							ret.setId(location.getUniqueId());
							ret.setAbbreviation(location.getLabel());
							ret.setName(location.getLabel());
							if (location.getDisplayName() != null && !location.getDisplayName().isEmpty()) {
								ret.setTitle(location.getLabel() + " - " + location.getDisplayName());
							} else {
								ret.setTitle(location.getLabel() + " - " + location.getRoomTypeLabel() + (location.getCapacity() > 1 ? " (" + location.getCapacity() + " seats)" : ""));
							}
							fillInSessionInfo(ret, location.getSession());
							fillInCalendarUrl(ret);
							resources.add(ret);
						}
						Collections.sort(resources);
						if (limit > 0 && resources.size() > limit) {
							resources = new ArrayList<ResourceInterface>(resources.subList(0, limit));
						}
					}
					break;
				case SUBJECT:
					List<SubjectArea> subjects = hibSession.createQuery("select s from SubjectArea s where s.session.uniqueId = :sessionId and (" +
							"lower(s.subjectAreaAbbreviation) like :name or lower(' ' || s.shortTitle) like :title or lower(' ' || s.longTitle) like :title) " +
							"order by s.subjectAreaAbbreviation")
							.setString("name", query.toLowerCase() + "%").setString("title", "% " + query.toLowerCase() + "%")
							.setLong("sessionId", academicSession.getUniqueId()).setMaxResults(limit).list();
					for (SubjectArea subject: subjects) {
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.SUBJECT);
						ret.setId(subject.getUniqueId());
						ret.setAbbreviation(subject.getSubjectAreaAbbreviation());
						ret.setName(subject.getLongTitle() == null ? subject.getShortTitle() : subject.getLongTitle());
						fillInSessionInfo(ret, subject.getSession());
						fillInCalendarUrl(ret);
						resources.add(ret);
					}
					if (subjects.size() == 1) {
						for (CourseOffering course: new TreeSet<CourseOffering>(subjects.get(0).getCourseOfferings())) {
							if (course.getInstructionalOffering().isNotOffered()) continue;
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.COURSE);
							ret.setId(course.getUniqueId());
							ret.setAbbreviation(course.getCourseName());
							ret.setName(course.getTitle() == null ? course.getCourseName() : course.getTitle());
							ret.setTitle("&nbsp;&nbsp;&nbsp;&nbsp;" + course.getCourseName() + (course.getTitle() == null ? "" : " - " + course.getTitle()));
							fillInSessionInfo(ret, course.getSubjectArea().getSession());
							fillInCalendarUrl(ret);
							resources.add(ret);
						}
					} else if (subjects.isEmpty()) {
						List<CourseOffering> courses = hibSession.createQuery("select c from CourseOffering c inner join c.subjectArea s where s.session.uniqueId = :sessionId and (" +
								"lower(s.subjectAreaAbbreviation || ' ' || c.courseNbr) like :name or lower(' ' || c.title) like :title) and c.instructionalOffering.notOffered = false " +
								"order by s.subjectAreaAbbreviation, c.courseNbr")
								.setString("name", query.toLowerCase() + "%").setString("title", "% " + query.toLowerCase() + "%")
								.setLong("sessionId", academicSession.getUniqueId()).setMaxResults(limit).list();
						for (CourseOffering course: courses) {
							if (course.getInstructionalOffering().isNotOffered()) continue;
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.COURSE);
							ret.setId(course.getUniqueId());
							ret.setAbbreviation(course.getCourseName());
							ret.setName(course.getTitle() == null ? course.getCourseName() : course.getTitle());
							fillInSessionInfo(ret, course.getSubjectArea().getSession());
							fillInCalendarUrl(ret);
							resources.add(ret);
						}
					}
					break;
				case COURSE:
					List<CourseOffering> courses = hibSession.createQuery("select c from CourseOffering c inner join c.subjectArea s where s.session.uniqueId = :sessionId and (" +
							"lower(s.subjectAreaAbbreviation || ' ' || c.courseNbr) like :name or lower(' ' || c.title) like :title) and c.instructionalOffering.notOffered = false " +
							"order by s.subjectAreaAbbreviation, c.courseNbr")
							.setString("name", query.toLowerCase() + "%").setString("title", "% " + query.toLowerCase() + "%")
							.setLong("sessionId", academicSession.getUniqueId()).setMaxResults(limit).list();
					for (CourseOffering course: courses) {
						if (course.getInstructionalOffering().isNotOffered()) continue;
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.COURSE);
						ret.setId(course.getUniqueId());
						ret.setAbbreviation(course.getCourseName());
						ret.setName(course.getTitle() == null ? course.getCourseName() : course.getTitle());
						fillInSessionInfo(ret, course.getSubjectArea().getSession());
						fillInCalendarUrl(ret);
						resources.add(ret);
					}
					break;
				case CURRICULUM:
					List<Curriculum> curricula = hibSession.createQuery("select c from Curriculum c where c.department.session.uniqueId = :sessionId and (" +
							"lower(c.abbv) like :name or lower(c.name) like :title) order by c.abbv")
							.setString("name", query.toLowerCase() + "%").setString("title", "%" + query.toLowerCase() + "%")
							.setLong("sessionId", academicSession.getUniqueId()).setMaxResults(limit).list();
					for (Curriculum curriculum: curricula) {
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.CURRICULUM);
						ret.setId(curriculum.getUniqueId());
						ret.setAbbreviation(curriculum.getAbbv());
						ret.setName(curriculum.getName());
						fillInSessionInfo(ret, curriculum.getDepartment().getSession());
						fillInCalendarUrl(ret);
						resources.add(ret);
					}
					if (curricula.size() == 1) {
						for (CurriculumClassification classification: new TreeSet<CurriculumClassification>(curricula.get(0).getClassifications())) {
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.CURRICULUM);
							ret.setId(classification.getUniqueId());
							ret.setAbbreviation(classification.getCurriculum().getAbbv() + " " + classification.getAcademicClassification().getCode());
							ret.setName(classification.getCurriculum().getName() + " " + classification.getAcademicClassification().getName());
							ret.setTitle("&nbsp;&nbsp;&nbsp;&nbsp;" + classification.getAcademicClassification().getCode() + " " + classification.getAcademicClassification().getName());
							fillInSessionInfo(ret, classification.getCurriculum().getDepartment().getSession());
							fillInCalendarUrl(ret);
							resources.add(ret);
						}
					} else if (curricula.isEmpty()) {
						List<CurriculumClassification> classifications = hibSession.createQuery("select f from CurriculumClassification f inner join f.curriculum c where " +
								"c.department.session.uniqueId = :sessionId and (" +
								"lower(c.abbv || '/' || f.name) like :name or lower(c.name || '/' || f.name) like :title or " +
								"lower(c.abbv || '/' || f.academicClassification.code) like :name or lower(c.name || '/' || f.academicClassification.code) like :title or " + 
								"lower(c.abbv || '/' || f.academicClassification.name) like :name or lower(c.name || '/' || f.academicClassification.name) like :title or " +
								"lower(c.abbv || ' ' || f.name) like :name or lower(c.name || ' ' || f.name) like :title or " +
								"lower(c.abbv || ' ' || f.academicClassification.code) like :name or lower(c.name || ' ' || f.academicClassification.code) like :title or " + 
								"lower(c.abbv || ' ' || f.academicClassification.name) like :name or lower(c.name || ' ' || f.academicClassification.name) like :title or " +
								"lower(c.abbv || f.name) like :name or lower(c.name || f.name) like :title or " +
								"lower(c.abbv || f.academicClassification.code) like :name or lower(c.name || f.academicClassification.code) like :title or " + 
								"lower(c.abbv || f.academicClassification.name) like :name or lower(c.name || f.academicClassification.name) like :title) " +
								"order by c.abbv, f.academicClassification.code")
								.setString("name", query.toLowerCase() + "%").setString("title", "%" + query.toLowerCase() + "%")
								.setLong("sessionId", academicSession.getUniqueId())
								.setMaxResults(limit - resources.size()).list();
						for (CurriculumClassification classification: classifications) {
							ResourceInterface ret = new ResourceInterface();
							ret.setType(ResourceType.CURRICULUM);
							ret.setId(classification.getUniqueId());
							ret.setAbbreviation(classification.getCurriculum().getAbbv() + " " + classification.getAcademicClassification().getCode());
							ret.setName(classification.getCurriculum().getName() + " " + classification.getAcademicClassification().getName());
							fillInSessionInfo(ret, classification.getCurriculum().getDepartment().getSession());
							fillInCalendarUrl(ret);
							resources.add(ret);
						}
					}
					if (limit > 0 && resources.size() > limit) {
						resources = new ArrayList<ResourceInterface>(resources.subList(0, limit));
					}
					break;
				case DEPARTMENT:
					List<Department> departments = hibSession.createQuery("select d from Department d where d.session.uniqueId = :sessionId and (" +
							"lower(d.deptCode) like :name or lower(d.abbreviation) like :name or lower(d.name) like :title) " +
							"order by d.abbreviation, d.deptCode")
							.setString("name", query.toLowerCase() + "%").setString("title", "%" + query.toLowerCase() + "%")
							.setLong("sessionId", academicSession.getUniqueId()).setMaxResults(limit).list();
					for (Department department: departments) {
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.DEPARTMENT);
						ret.setId(department.getUniqueId());
						ret.setAbbreviation(department.getAbbreviation() == null ? department.getDeptCode() : department.getAbbreviation());
						ret.setName(department.getName());
						fillInSessionInfo(ret, department.getSession());
						fillInCalendarUrl(ret);
						resources.add(ret);
					}
					break;
				default:
					throw new EventException("Resource type " + type.getLabel() + " not supported.");
				}
				if (resources.isEmpty())
					throw new EventException("No " + type.getLabel() + " " + query + " found.");
				return resources;
			} finally {
				hibSession.close();
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (EventException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new EventException("Failed to find resources: " + e.getMessage());
		}
	}

	@Override
	public Boolean canLookupPeople() throws EventException, PageAccessException {
		try {
			User user = Web.getUser(getThreadLocalRequest().getSession());
			return user != null && (Roles.ADMIN_ROLE.equals(user.getRole()) || Roles.STUDENT_ADVISOR.equals(user.getRole()) || Roles.DEPT_SCHED_MGR_ROLE.equals(user.getRole()));
		} catch (PageAccessException e) {
			throw e;
		} catch (EventException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new EventException(e.getMessage());
		}
	}

}
