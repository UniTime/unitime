/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ResourceLookupRpcRequest.class)
public class ResourceLookupBackend extends EventAction<ResourceLookupRpcRequest, GwtRpcResponseList<ResourceInterface>> {

	@Override
	public GwtRpcResponseList<ResourceInterface> execute(ResourceLookupRpcRequest request, EventContext context) {
		if (request.getResourceType() == ResourceType.PERSON) {
			if (!request.hasName()) request.setName(context.getUser().getExternalUserId());
			else if (!request.getName().equals(context.isAuthenticated() ? context.getUser().getExternalUserId() : null))
				context.checkPermission(Right.EventLookupSchedule);
		}
		
		GwtRpcResponseList<ResourceInterface> response = new GwtRpcResponseList<ResourceInterface>();
		if (request.hasLimit() && request.getLimit() == 1) {
			response.add(findResource(request.getSessionId(), request.getResourceType(), request.getName()));
		} else {
			response.addAll(findResources(request.getSessionId(), request.getResourceType(), request.getName(), request.getLimit()));
		}
		return response;
	}

	public ResourceInterface findResource(Long sessionId, ResourceType type, String name) {
		try {
			org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
			try {
				Session academicSession = SessionDAO.getInstance().get(sessionId);
				switch (type) {
				case ROOM:
					if (ApplicationProperty.EventRoomTimetableAllRooms.isTrue()) {
						List<Room> rooms = hibSession.createQuery("select distinct r from Room r " +
								"where r.session.uniqueId = :sessionId and r.eventDepartment.allowEvents = true and (" +
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
							return ret;
						}
						List<NonUniversityLocation> locations = hibSession.createQuery("select distinct l from NonUniversityLocation l " +
								"where l.session.uniqueId = :sessionId and l.name = :name and l.eventDepartment.allowEvents = true"
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
							return ret;
						}
					}
					throw new GwtRpcException("Unable to find a " + type.getLabel() + " named " + name + ".");
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
						ret.setName(subject.getTitle());
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
						return ret;
					}
					throw new GwtRpcException("Unable to find a " + type.getLabel() + " named " + name + ".");
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
						return ret;
					}
					throw new GwtRpcException("Unable to find a " + type.getLabel() + " named " + name + ".");
				case DEPARTMENT:
					List<Department> departments = hibSession.createQuery("select d from Department d where d.session.uniqueId = :sessionId and " +
							"(lower(d.deptCode) = :name or lower(d.abbreviation) = :name)")
							.setString("name", name.toLowerCase()).setLong("sessionId", academicSession.getUniqueId()).list();
					if (!departments.isEmpty()) {
						Department department = departments.get(0);
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.DEPARTMENT);
						ret.setId(department.getUniqueId());
						ret.setAbbreviation(department.getAbbreviation() == null ? department.getDeptCode() : department.getAbbreviation());
						ret.setName(department.getName());
						return ret;
					}
					throw new GwtRpcException("Unable to find a " + type.getLabel() + " named " + name + ".");
				case PERSON:
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
						return ret;
					}
					throw new GwtRpcException("No events found in " + academicSession.getLabel() + ".");
				default:
					throw new GwtRpcException("Resource type " + type.getLabel() + " not supported.");
				}
			} finally {
				hibSession.close();
			}
		} catch (GwtRpcException e) {
			throw e;
		} catch (Exception e) {
			throw new GwtRpcException("Unable to find a " + type.getLabel() + " named " + name + ": " + e.getMessage(), e);
		}
	}
	
	public List<ResourceInterface> findResources(Long sessionId, ResourceType type, String query, int limit) {
		try {
			if (query == null) query = "";
			org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
			try {
				Session academicSession = SessionDAO.getInstance().get(sessionId);
				
				List<ResourceInterface> resources = new ArrayList<ResourceInterface>();
				switch (type) {
				case ROOM:
					if (ApplicationProperty.EventRoomTimetableAllRooms.isTrue()) {
						List<Room> rooms = hibSession.createQuery("select distinct r from Room r, " +
								"RoomTypeOption o where r.session.uniqueId = :sessionId and " +
								"r.eventDepartment.allowEvents = true and " + 
								"o.status != 0 and o.roomType = r.roomType and o.department = r.eventDepartment and (" +
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
							resources.add(ret);
						}
						List<NonUniversityLocation> locations = hibSession.createQuery("select distinct l from NonUniversityLocation l, " +
								"RoomTypeOption o where l.eventDepartment.allowEvents = true and " + 
								"l.session.uniqueId = :sessionId and o.status != 0 and o.roomType = l.roomType and o.department = l.eventDepartment and lower(l.name) like :name " +
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
							"lower(s.subjectAreaAbbreviation) like :name or lower(' ' || s.title) like :title) " +
							"order by s.subjectAreaAbbreviation")
							.setString("name", query.toLowerCase() + "%").setString("title", "% " + query.toLowerCase() + "%")
							.setLong("sessionId", academicSession.getUniqueId()).setMaxResults(limit).list();
					for (SubjectArea subject: subjects) {
						ResourceInterface ret = new ResourceInterface();
						ret.setType(ResourceType.SUBJECT);
						ret.setId(subject.getUniqueId());
						ret.setAbbreviation(subject.getSubjectAreaAbbreviation());
						ret.setName(subject.getTitle());
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
						resources.add(ret);
					}
					break;
				default:
					throw new GwtRpcException("Resource type " + type.getLabel() + " not supported.");
				}
				if (resources.isEmpty())
					throw new GwtRpcException("No " + type.getLabel() + " " + query + " found.");
				return resources;
			} finally {
				hibSession.close();
			}
		} catch (GwtRpcException e) {
			throw e;
		} catch (Exception e) {
			throw new GwtRpcException("Failed to find resources: " + e.getMessage(), e);
		}
	}

}
