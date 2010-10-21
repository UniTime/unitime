/*
 * UniTime 3.2 (University Timetabling Application)
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.unitime.timetable.gwt.services.EventService;
import org.unitime.timetable.gwt.shared.EventException;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EventServlet extends RemoteServiceServlet implements EventService {
	private static final long serialVersionUID = 7949018510304934636L;
	private static Logger sLog = Logger.getLogger(EventServlet.class);

	public void init() throws ServletException {
	}
	
	public Session findSession(org.hibernate.Session hibSession, String session) throws EventException {
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
		throw new EventException("Academic session " + session + " not found.");
	}

	@Override
	public ResourceInterface findResource(String session, ResourceType type, String name) throws EventException {
		try {
			org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
			try {
				Session academicSession = findSession(hibSession, session);
				switch (type) {
				case ROOM:
					List<Room> rooms = hibSession.createQuery("select r from Room r where r.session.uniqueId = :sessionId and (" +
							"r.buildingAbbv || ' ' || r.roomNumber = :name or r.buildingAbbv || r.roomNumber = :name)")
							.setString("name", name).setLong("sessionId", academicSession.getUniqueId()).list();
					if (!rooms.isEmpty()) {
						Room room = rooms.get(0);
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.ROOM);
						ret.setId(room.getUniqueId());
						ret.setName(room.getLabel());
						ret.setSessionId(room.getSession().getUniqueId());
						ret.setSessionName(room.getSession().getLabel());
						return ret;
					}
					List<NonUniversityLocation> locations = hibSession.createQuery("select l from NonUniversityLocation l where " +
							"l.session.uniqueId = :sessionId and l.name = :name")
							.setString("name", name).setLong("sessionId", academicSession.getUniqueId()).list();
					if (!locations.isEmpty()) {
						NonUniversityLocation location = locations.get(0);
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.ROOM);
						ret.setId(location.getUniqueId());
						ret.setName(location.getLabel());
						ret.setSessionId(location.getSession().getUniqueId());
						ret.setSessionName(location.getSession().getLabel());
						return ret;
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
						ret.setName(subject.getLongTitle() == null ? subject.getShortTitle() : subject.getLongTitle());
						ret.setSessionId(subject.getSession().getUniqueId());
						ret.setSessionName(subject.getSession().getLabel());
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
						ret.setName(curriculum.getName());
						ret.setSessionId(curriculum.getDepartment().getSession().getUniqueId());
						ret.setSessionName(curriculum.getDepartment().getSession().getLabel());
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
						ret.setName(department.getName());
						ret.setSessionId(department.getSession().getUniqueId());
						ret.setSessionName(department.getSession().getLabel());
						return ret;
					}
					throw new EventException("Unable to find a " + type.getLabel() + " named " + name + ".");
				default:
					throw new EventException("Resource type " + type.getLabel() + " not supported.");
				}
			} finally {
				hibSession.close();
			}
		} catch (Exception e) {
			if (e instanceof EventException)
				throw (EventException)e;
			sLog.error(e.getMessage(), e);
			throw new EventException("Unable to find a " + type.getLabel() + " named " + name + ": " + e.getMessage());
		}
	}

	@Override
	public List<EventInterface> findEvents(ResourceInterface resource) throws EventException {
		try {
			org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
			try {
				
				List<Meeting> meetings = null;
				Session session = SessionDAO.getInstance().get(resource.getSessionId(), hibSession);
				switch (resource.getType()) {
				case ROOM:
					meetings = (List<Meeting>)hibSession.createQuery("select m from Meeting m, Location l where " +
							"l.uniqueId = :resourceId and m.locationPermanentId = l.permanentId " +
							"and m.meetingDate >= l.session.eventBeginDate and m.meetingDate <= l.session.eventEndDate")
							.setLong("resourceId", resource.getId()).list();
					break;
				case SUBJECT:
					meetings = (List<Meeting>)hibSession.createQuery("select m from ClassEvent e inner join e.meetings m inner join " +
							"e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.subjectArea.uniqueId = :resourceId " +
							"and m.meetingDate >= co.subjectArea.session.eventBeginDate and " +
							"m.meetingDate <= co.subjectArea.session.eventEndDate")
							.setLong("resourceId", resource.getId()).list();
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:courseType and o.ownerId = co.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
							"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:classType and o.ownerId = c.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
							"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:configType and o.ownerId = cfg.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:courseType and o.ownerId = co.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
							"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:classType and o.ownerId = c.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
							"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg where " +
							"co.subjectArea.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:configType and o.ownerId = cfg.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					break;
				case CURRICULUM:
					meetings = (List<Meeting>)hibSession.createQuery("select m from ClassEvent e inner join e.meetings m inner join " +
							"e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc where " +
							"co = cc.course and cc.classification.curriculum.uniqueId = :resourceId " +
							"and m.meetingDate >= co.subjectArea.session.eventBeginDate and " +
							"m.meetingDate <= co.subjectArea.session.eventEndDate")
							.setLong("resourceId", resource.getId()).list();
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co, CurriculumCourse cc where " +
							"co = cc.course and cc.classification.curriculum.uniqueId = :resourceId and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:courseType and o.ownerId = co.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co, CurriculumCourse cc where " +
							"co = cc.course and cc.classification.curriculum.uniqueId = :resourceId and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
							"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc where " +
							"co = cc.course and cc.classification.curriculum.uniqueId = :resourceId and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:classType and o.ownerId = c.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
							"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg, CurriculumCourse cc where " +
							"co = cc.course and cc.classification.curriculum.uniqueId = :resourceId and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:configType and o.ownerId = cfg.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co, CurriculumCourse cc where " +
							"co = cc.course and cc.classification.curriculum.uniqueId = :resourceId and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:courseType and o.ownerId = co.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co, CurriculumCourse cc where " +
							"co = cc.course and cc.classification.curriculum.uniqueId = :resourceId and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
							"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc where " +
							"co = cc.course and cc.classification.curriculum.uniqueId = :resourceId and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:classType and o.ownerId = c.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
					meetings.addAll(
							(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
							"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg, CurriculumCourse cc where " +
							"co = cc.course and cc.classification.curriculum.uniqueId = :resourceId and " +
							"m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
							"o.ownerType=:configType and o.ownerId = cfg.uniqueId")
							.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					break;
				case DEPARTMENT:
					Department d = DepartmentDAO.getInstance().get(resource.getId(), hibSession);
					if (d.isExternalManager()) {
						meetings = (List<Meeting>)hibSession.createQuery("select m from ClassEvent e inner join e.meetings m inner join e.clazz.managingDept d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= d.session.eventBeginDate and m.meetingDate <= d.session.eventEndDate")
								.setLong("resourceId", resource.getId()).list();
					} else {
						meetings = (List<Meeting>)hibSession.createQuery("select m from ClassEvent e inner join e.meetings m inner join " +
								"e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= d.session.eventBeginDate and m.meetingDate <= d.session.eventEndDate")
								.setLong("resourceId", resource.getId()).list();
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:courseType and o.ownerId = co.uniqueId")
								.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, CourseOffering co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId")
								.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
								"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:classType and o.ownerId = c.uniqueId")
								.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from ExamEvent e inner join e.meetings m inner join e.exam.owners o, " +
								"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:configType and o.ownerId = cfg.uniqueId")
								.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:courseType and o.ownerId = co.uniqueId")
								.setLong("resourceId", resource.getId()).setInteger("courseType", ExamOwner.sOwnerTypeCourse).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, CourseOffering co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:offeringType and o.ownerId = co.instructionalOffering.uniqueId")
								.setLong("resourceId", resource.getId()).setInteger("offeringType", ExamOwner.sOwnerTypeOffering).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
								"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:classType and o.ownerId = c.uniqueId")
								.setLong("resourceId", resource.getId()).setInteger("classType", ExamOwner.sOwnerTypeClass).list());
						meetings.addAll(
								(List<Meeting>)hibSession.createQuery("select m from CourseEvent e inner join e.meetings m inner join e.relatedCourses o, " +
								"CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg inner join co.subjectArea.department d where " +
								"d.uniqueId = :resourceId and m.meetingDate >= co.subjectArea.session.eventBeginDate and m.meetingDate <= co.subjectArea.session.eventEndDate and " +
								"o.ownerType=:configType and o.ownerId = cfg.uniqueId")
								.setLong("resourceId", resource.getId()).setInteger("configType", ExamOwner.sOwnerTypeConfig).list());
					}
					break;
				default:
					throw new EventException("Resource type " + resource.getType().getLabel() + " not supported.");
				}
				
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
					}
					MeetingInterface meeting = new MeetingInterface();
					meeting.setId(m.getUniqueId());
					meeting.setMeetingDate(new SimpleDateFormat("MM/dd").format(m.getMeetingDate()));
					meeting.setDayOfWeek(Constants.getDayOfWeek(m.getMeetingDate()));
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
					if (resource.getType() == ResourceType.ROOM)
						meeting.setLocation(resource);
					else if (m.getLocation() != null) {
						ResourceInterface location = new ResourceInterface();
						location.setType(ResourceType.ROOM);
						location.setId(m.getLocation().getUniqueId());
						location.setName(m.getLocation().getLabel());
						location.setSessionId(m.getLocation().getSession().getUniqueId());
						location.setSessionName(m.getLocation().getSession().getLabel());
						meeting.setLocation(location);
					}
					event.addMeeting(meeting);
				}
				return ret;
			} finally {
				hibSession.close();
			}
		} catch (Exception e) {
			if (e instanceof EventException)
				throw (EventException)e;
			sLog.error(e.getMessage(), e);
			throw new EventException("Unable to find events for " + resource + ": " + e.getMessage());
		}
	}

}
