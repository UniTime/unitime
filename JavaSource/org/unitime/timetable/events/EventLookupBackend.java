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
package org.unitime.timetable.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.events.EventFilterBackend.EventQuery.EventInstance;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConflictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentGroupDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(EventLookupRpcRequest.class)
public class EventLookupBackend extends EventAction<EventLookupRpcRequest, GwtRpcResponseList<EventInterface>>{
	private static Logger sLog = Logger.getLogger(EventLookupBackend.class);

	@Override
	public GwtRpcResponseList<EventInterface> execute(EventLookupRpcRequest request, EventContext context) {
		if (request.getResourceType() == ResourceType.PERSON) {
			if (!request.hasResourceExternalId()) request.setResourceExternalId(context.isAuthenticated() ? context.getUser().getExternalUserId() : null);
			else if (!request.getResourceExternalId().equals(context.isAuthenticated() ? context.getUser().getExternalUserId() : null)) {
				context.checkPermission(Right.EventLookupSchedule);
				Set<String> roles = request.getEventFilter().getOptions("role");
				if (roles == null) {
					roles = new HashSet<String>();
					if (context.hasPermission(Right.CanLookupStudents)) {
						roles.add("Student");
					}
					if (context.hasPermission(Right.CanLookupInstructors)) {
						roles.add("Instructor");
						roles.add("Coordinator");
					}
					if (context.hasPermission(Right.CanLookupEventContacts)) {
						roles.add("Contact");
					}
					if (roles.size() < 4)
						request.getEventFilter().setOptions("role", roles);
				} else {
					if (!context.hasPermission(Right.CanLookupStudents)) {
						roles.remove("Student"); roles.remove("student");
					}
					if (!context.hasPermission(Right.CanLookupInstructors)) {
						roles.remove("Instructor"); roles.remove("instructor");
						roles.remove("Coordinator"); roles.remove("coordinator");
					}
					if (!context.hasPermission(Right.CanLookupStudents)) {
						roles.remove("Contact"); roles.remove("contact");
					}
				}
			}
		}
		
		if (request.getEventFilter() == null) {
			EventFilterRpcRequest eventFilter = new EventFilterRpcRequest();
			eventFilter.setSessionId(request.getSessionId());
			request.setEventFilter(eventFilter);
		}
		
		return findEvents(request, context);
	}
	
	private boolean hasChild(Set<Long> restrictions, Class_ clazz) {
		if (restrictions.contains(clazz.getUniqueId())) return true;
    	for (Class_ child: clazz.getChildClasses())
    		if (hasChild(restrictions, child)) return true;
    	return false;
	}
	
	private boolean hasClassRestrictionChild(Set<Long> restrictions, SchedulingSubpart subpart) {
		for (Class_ other: subpart.getClasses())
			if (restrictions.contains(other.getUniqueId())) return true;
    	for (SchedulingSubpart child: subpart.getChildSubparts())
    		if (hasClassRestrictionChild(restrictions, child)) return true;
		return false;
	}
	
	private boolean hasClassRestriction(Set<Long> restrictions, Class_ clazz) {
		if (restrictions.isEmpty()) return false;
		SchedulingSubpart parent = clazz.getSchedulingSubpart().getParentSubpart();
		while (parent != null) {
			for (Class_ other: parent.getClasses())
				if (restrictions.contains(other.getUniqueId())) return true;
			parent = parent.getParentSubpart();
		}
		return hasClassRestrictionChild(restrictions, clazz.getSchedulingSubpart());
	}
	
	protected boolean hide(Set<Long>[] restrictions, Class_ clazz) {
		// check configs
		if (!restrictions[0].isEmpty() && !restrictions[0].contains(clazz.getSchedulingSubpart().getInstrOfferingConfig().getUniqueId()))
			return true;
		// check classes
		if (hasClassRestriction(restrictions[1], clazz)) {
			Class_ parent = clazz;
			while (parent != null) {
				if (restrictions[1].contains(parent.getUniqueId())) return false;
				parent = parent.getParentClass();
			}
			for (Class_ child: clazz.getChildClasses())
				if (hasChild(restrictions[1], child)) return false;
			return true;
		}
		return false;
	}
	
	public GwtRpcResponseList<EventInterface> findEvents(EventLookupRpcRequest request, EventContext context) {
		try {
			org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
			try {
				Map<Long, Double> distances = new HashMap<Long, Double>();
				Map<Long, Location> locationMap = null;
				if (request.getRoomFilter() != null && !request.getRoomFilter().isEmpty()) {
					locationMap = new HashMap<Long, Location>();
					if (context.isAuthenticated())
						request.getRoomFilter().setOption("user", context.getUser().getExternalUserId());
					for (Location location: new RoomFilterBackend().locations(request.getSessionId(), request.getRoomFilter(), 1000, distances, context)) {
						request.getEventFilter().addOption("room", location.getUniqueId().toString());
						locationMap.put(location.getPermanentId(), location);
					}
				}
				if (request.getResourceType() == ResourceType.ROOM && request.getEventFilter().hasOptions("type") && !request.getEventFilter().getOptions("type").contains(Event.sEventTypesAbbv[Event.sEventTypeUnavailable])) {
					request.getEventFilter().addOption("type", Event.sEventTypesAbbv[Event.sEventTypeUnavailable]);
				}
				EventFilterBackend.EventQuery query = EventFilterBackend.getQuery(request.getEventFilter(), context);
				int limit = request.getLimit();
				String nameFormat = context.getUser().getProperty(UserProperty.NameFormat);
				
				List<Meeting> meetings = null;
				Map<Long, Set<Long>[]> restrictions = null;
				Session session = SessionDAO.getInstance().get(request.getSessionId(), hibSession);
				Collection<Long> curriculumCourses = null;
				Collection<Long> curriculumClasses = null;
				Department department = null;
				StudentGroup group = null;
				if (request.getResourceType() == ResourceType.GROUP)
					group = StudentGroupDAO.getInstance().get(request.getResourceId(), hibSession);
				boolean groupEnrollments = (request.getResourceType() == ResourceType.GROUP && group != null && ApplicationProperty.StudentGroupsTimetableGroupEnrollments.isTrue());
				
				switch (request.getResourceType()) {
				case ROOM:
					if (request.getResourceId() != null)
						meetings = (List<Meeting>)query.select("distinct m")
						.joinWithLocation()
						.where("l.uniqueId = :resourceId")
						.set("resourceId", request.getResourceId())
						.limit(1 + limit)
						.query(hibSession).list();
					else if (request.getResourceExternalId() != null)
						meetings = (List<Meeting>)query.select("distinct m")
						.joinWithLocation()
						.where("l.externalUniqueId = :externalId")
						.set("externalId", request.getResourceExternalId())
						.limit(1 + limit)
						.query(hibSession).list();
					else
						meetings = (List<Meeting>)query.select("distinct m").limit(1 + limit).query(hibSession).list();
					break;
				case SUBJECT:
				case COURSE:
					String resourceCheck = (request.getResourceType() == ResourceType.SUBJECT ? "co.subjectArea.uniqueId = :resourceId" : "co.uniqueId = :resourceId");
					
					meetings = new ArrayList<Meeting>();
					
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("ClassEvent")
							.from("inner join e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
							.where(resourceCheck)
							.set("resourceId", request.getResourceId())
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("CourseEvent")
							.from("inner join e.relatedCourses o, CourseOffering co")
							.where("o.ownerType = :type and o.ownerId = co.uniqueId")
							.set("type", ExamOwner.sOwnerTypeCourse)
							.where(resourceCheck)
							.set("resourceId", request.getResourceId())
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("CourseEvent")
							.from("inner join e.relatedCourses o, CourseOffering co")
							.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
							.set("type", ExamOwner.sOwnerTypeOffering)
							.where(resourceCheck)
							.set("resourceId", request.getResourceId())
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("CourseEvent")
							.from("inner join e.relatedCourses o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
							.where("o.ownerType = :type and o.ownerId = c.uniqueId")
							.set("type", ExamOwner.sOwnerTypeClass)
							.where(resourceCheck)
							.set("resourceId", request.getResourceId())
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit) 
						meetings.addAll(query.select("distinct m").type("CourseEvent")
							.from("inner join e.relatedCourses o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg")
							.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
							.set("type", ExamOwner.sOwnerTypeConfig)
							.where(resourceCheck)
							.set("resourceId", request.getResourceId())
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					
					if (limit <= 0 || meetings.size() < limit) 
						meetings.addAll(query.select("distinct m").type("ExamEvent")
							.from("inner join e.exam.owners o, CourseOffering co")
							.where("o.ownerType = :type and o.ownerId = co.uniqueId")
							.set("type", ExamOwner.sOwnerTypeCourse)
							.where(resourceCheck)
							.set("resourceId", request.getResourceId())
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit) 
						meetings.addAll(query.select("distinct m").type("ExamEvent")
							.from("inner join e.exam.owners o, CourseOffering co")
							.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
							.set("type", ExamOwner.sOwnerTypeOffering)
							.where(resourceCheck)
							.set("resourceId", request.getResourceId())
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit) 
						meetings.addAll(query.select("distinct m").type("ExamEvent")
							.from("inner join e.exam.owners o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
							.where("o.ownerType = :type and o.ownerId = c.uniqueId")
							.set("type", ExamOwner.sOwnerTypeClass)
							.where(resourceCheck)
							.set("resourceId", request.getResourceId())
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit) 
						meetings.addAll(query.select("distinct m").type("ExamEvent")
							.from("inner join e.exam.owners o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg")
							.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
							.set("type", ExamOwner.sOwnerTypeConfig)
							.where(resourceCheck)
							.set("resourceId", request.getResourceId())
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());

					break;			
				case CURRICULUM:
					curriculumCourses = (List<Long>)hibSession.createQuery(
							"select cc.course.uniqueId from CurriculumCourse cc where cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
							.setLong("resourceId", request.getResourceId()).list();
					
					meetings = new ArrayList<Meeting>();

					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("ClassEvent")
							.from("inner join e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc")
							.where("co = cc.course")
							.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
							.set("resourceId", request.getResourceId())
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					
					restrictions = new Hashtable<Long, Set<Long>[]>();
					for (Object[] o: (List<Object[]>)hibSession.createQuery(
							"select distinct cc.course.instructionalOffering.uniqueId, g.uniqueId, z.uniqueId " +
							"from CurriculumReservation r left outer join r.configurations g left outer join r.classes z " +
							"left outer join r.majors rm left outer join r.classifications rc, " +
							"CurriculumCourse cc inner join cc.classification.curriculum.majors cm " +
							"where (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId) " +
							"and cc.course.instructionalOffering = r.instructionalOffering and r.area = cc.classification.curriculum.academicArea "+
							"and (rm is null or rm = cm) and (rc is null or rc = cc.classification.academicClassification)")
							.setLong("resourceId", request.getResourceId()).setCacheable(true).list()) {
						Long offeringId = (Long)o[0];
						Long configId = (Long)o[1];
						Long clazzId = (Long)o[2];
						Set<Long>[] r = restrictions.get(offeringId);
						if (r == null) {
							r = new Set[] { new HashSet<Long>(), new HashSet<Long>()};
							restrictions.put(offeringId, r);
						}
						if (configId != null) r[0].add(configId);
						if (clazzId != null) r[1].add(clazzId);
					}
					
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("ExamEvent")
							.from("inner join e.exam.owners o, CourseOffering co, CurriculumCourse cc")
							.where("co = cc.course")
							.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
							.set("resourceId", request.getResourceId())
							.where("o.ownerType = :type and o.ownerId = co.uniqueId")
							.set("type", ExamOwner.sOwnerTypeCourse)
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("ExamEvent")
							.from("inner join e.exam.owners o, CourseOffering co, CurriculumCourse cc")
							.where("co = cc.course")
							.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
							.set("resourceId", request.getResourceId())
							.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
							.set("type", ExamOwner.sOwnerTypeOffering)
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("ExamEvent")
							.from("inner join e.exam.owners o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc")
							.where("co = cc.course")
							.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
							.set("resourceId", request.getResourceId())
							.where("o.ownerType = :type and o.ownerId = c.uniqueId")
							.set("type", ExamOwner.sOwnerTypeClass)
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("ExamEvent")
							.from("inner join e.exam.owners o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg, CurriculumCourse cc")
							.where("co = cc.course")
							.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
							.set("resourceId", request.getResourceId())
							.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
							.set("type", ExamOwner.sOwnerTypeConfig)
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("CourseEvent")
							.from("inner join e.relatedCourses o, CourseOffering co, CurriculumCourse cc")
							.where("co = cc.course")
							.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
							.set("resourceId", request.getResourceId())
							.where("o.ownerType = :type and o.ownerId = co.uniqueId")
							.set("type", ExamOwner.sOwnerTypeCourse)
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("CourseEvent")
							.from("inner join e.relatedCourses o, CourseOffering co, CurriculumCourse cc")
							.where("co = cc.course")
							.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
							.set("resourceId", request.getResourceId())
							.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
							.set("type", ExamOwner.sOwnerTypeOffering)
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("CourseEvent")
							.from("inner join e.relatedCourses o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc")
							.where("co = cc.course")
							.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
							.set("resourceId", request.getResourceId())
							.where("o.ownerType = :type and o.ownerId = c.uniqueId")
							.set("type", ExamOwner.sOwnerTypeClass)
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m").type("CourseEvent")
							.from("inner join e.relatedCourses o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg, CurriculumCourse cc")
							.where("co = cc.course")
							.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
							.set("resourceId", request.getResourceId())
							.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
							.set("type", ExamOwner.sOwnerTypeConfig)
							.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
							.query(hibSession).list());

					break;
					
				case DEPARTMENT:
					department = DepartmentDAO.getInstance().get(request.getResourceId(), hibSession);
					if (department.isExternalManager()) {
						meetings = (List<Meeting>)query.select("distinct m").type("ClassEvent")
									.from("inner join e.clazz.managingDept d")
									.where("d.uniqueId = :resourceId")
									.set("resourceId", request.getResourceId())
									.limit(1 + limit)
									.query(hibSession).list();
					} else {
						meetings = (List<Meeting>)query.select("distinct m").type("ClassEvent")
								.from("inner join e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d")
								.where("d.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.limit(1 + limit)
								.query(hibSession).list();
						
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("ExamEvent")
								.from("inner join e.exam.owners o, CourseOffering co inner join co.subjectArea.department d")
								.where("d.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("ExamEvent")
								.from("inner join e.exam.owners o, CourseOffering co inner join co.subjectArea.department d")
								.where("d.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("ExamEvent")
								.from("inner join e.exam.owners o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d")
								.where("d.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.set("type", ExamOwner.sOwnerTypeClass)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("ExamEvent")
								.from("inner join e.exam.owners o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg inner join co.subjectArea.department d")
								.where("d.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co inner join co.subjectArea.department d")
								.where("d.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co inner join co.subjectArea.department d")
								.where("d.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d")
								.where("d.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.set("type", ExamOwner.sOwnerTypeClass)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg inner join co.subjectArea.department d")
								.where("d.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
					}
					
					break;
				case PERSON:
					boolean allSessions = request.getEventFilter().hasOption("flag") && request.getEventFilter().getOptions("flag").contains("All Sessions");
					Set<String> roles = request.getEventFilter().getOptions("role");
					boolean student = (roles == null || roles.contains("student") || roles.contains("Student"));
					boolean instructor = (roles == null || roles.contains("instructor") || roles.contains("Instructor"));
					boolean contact = (roles == null || roles.contains("contact") || roles.contains("Contact"));
					boolean coordinator = (roles != null && (roles.contains("coordinator") || roles.contains("Coordinator")));
					curriculumCourses = new HashSet<Long>();
					curriculumClasses = new HashSet<Long>();
					if (allSessions) {
						if (student) {
							curriculumCourses.addAll(hibSession.createQuery("select e.courseOffering.uniqueId from StudentClassEnrollment e where e.student.externalUniqueId = :externalId")
								.setString("externalId", request.getResourceExternalId()).list());
							curriculumClasses.addAll(hibSession.createQuery("select e.clazz.uniqueId from StudentClassEnrollment e where e.student.externalUniqueId = :externalId")
								.setString("externalId", request.getResourceExternalId()).list());
						}
						if (instructor) {
							curriculumCourses.addAll(hibSession.createQuery("select co.uniqueId from ClassInstructor i inner join i.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where i.instructor.externalUniqueId = :externalId and co.isControl = true")
									.setString("externalId", request.getResourceExternalId()).list());
							curriculumClasses.addAll(hibSession.createQuery("select i.classInstructing.uniqueId from ClassInstructor i where i.instructor.externalUniqueId = :externalId")
									.setString("externalId", request.getResourceExternalId()).list());
						}
						if (coordinator) {
							curriculumCourses.addAll(hibSession.createQuery("select co.uniqueId from OfferingCoordinator c inner join c.offering.courseOfferings co where c.instructor.externalUniqueId = :externalId")
									.setString("externalId", request.getResourceExternalId()).list());
							curriculumClasses.addAll(hibSession.createQuery("select z.uniqueId from Class_ z inner join z.schedulingSubpart.instrOfferingConfig.instructionalOffering.offeringCoordinators c where c.instructor.externalUniqueId = :externalId")
									.setString("externalId", request.getResourceExternalId()).list());
						}
					} else {
						if (student) {
							curriculumCourses.addAll(hibSession.createQuery("select e.courseOffering.uniqueId from StudentClassEnrollment e where e.student.session.uniqueId = :sessionId and e.student.externalUniqueId = :externalId")
									.setLong("sessionId", request.getSessionId()).setString("externalId", request.getResourceExternalId()).list());
							curriculumClasses.addAll(hibSession.createQuery("select e.clazz.uniqueId from StudentClassEnrollment e where e.student.session.uniqueId = :sessionId and e.student.externalUniqueId = :externalId")
									.setLong("sessionId", request.getSessionId()).setString("externalId", request.getResourceExternalId()).list());
						}
						if (instructor) {
							curriculumCourses.addAll(hibSession.createQuery("select co.uniqueId from ClassInstructor i inner join i.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co " +
									"where i.instructor.externalUniqueId = :externalId and co.isControl = true and i.instructor.department.session.uniqueId = :sessionId")
									.setLong("sessionId", request.getSessionId()).setString("externalId", request.getResourceExternalId()).list());
							curriculumClasses.addAll(hibSession.createQuery("select i.classInstructing.uniqueId from ClassInstructor i where i.instructor.externalUniqueId = :externalId and i.instructor.department.session.uniqueId = :sessionId")
									.setLong("sessionId", request.getSessionId()).setString("externalId", request.getResourceExternalId()).list());
						}
						if (coordinator) {
							curriculumCourses.addAll(hibSession.createQuery("select co.uniqueId from OfferingCoordinator c inner join c.offering.courseOfferings co where c.instructor.externalUniqueId = :externalId and c.instructor.department.session.uniqueId = :sessionId")
									.setLong("sessionId", request.getSessionId()).setString("externalId", request.getResourceExternalId()).list());
							curriculumClasses.addAll(hibSession.createQuery("select z.uniqueId from Class_ z inner join z.schedulingSubpart.instrOfferingConfig.instructionalOffering.offeringCoordinators c where c.instructor.externalUniqueId = :externalId and c.instructor.department.session.uniqueId = :sessionId")
									.setLong("sessionId", request.getSessionId()).setString("externalId", request.getResourceExternalId()).list());
						}						
					}
					meetings = new ArrayList<Meeting>();

					if (allSessions) {
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.studentEnrollments enrl")
									.where("enrl.student.externalUniqueId = :externalId").where("enrl.student.session.uniqueId = s.uniqueId")
									.set("externalId", request.getResourceExternalId())
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.classInstructors ci")
									.where("ci.instructor.externalUniqueId = :externalId").where("ci.instructor.department.session.uniqueId = s.uniqueId")
									.set("externalId", request.getResourceExternalId()).limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.offeringCoordinators cc")
									.where("cc.instructor.externalUniqueId = :externalId").where("cc.instructor.department.session.uniqueId = s.uniqueId")
									.set("externalId", request.getResourceExternalId()).limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					} else {
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.studentEnrollments enrl")
									.where("enrl.student.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("enrl.student.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.classInstructors ci")
									.where("ci.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("ci.instructor.department.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.offeringCoordinators cc")
									.where("cc.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("cc.instructor.department.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}

					if (allSessions) {
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
									.where("enrl.student.externalUniqueId = :externalId")
									.set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.uniqueId")
									.set("type", ExamOwner.sOwnerTypeCourse)
									.where("enrl.student.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
									.where("enrl.student.externalUniqueId = :externalId")
									.set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
									.set("type", ExamOwner.sOwnerTypeOffering)
									.where("enrl.student.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
									.where("enrl.student.externalUniqueId = :externalId")
									.set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = c.uniqueId")
									.set("type", ExamOwner.sOwnerTypeClass)
									.where("enrl.student.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig cfg")
									.where("enrl.student.externalUniqueId = :externalId")
									.set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
									.set("type", ExamOwner.sOwnerTypeConfig)
									.where("enrl.student.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());

						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.instructors i")
									.where("i.externalUniqueId = :externalId")
									.set("externalId", request.getResourceExternalId())
									.where("i.department.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (instructor && ApplicationProperty.EventExamsConsiderClassInstructorAssignments.isTrue() && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, ClassInstructor ci inner join ci.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
									.where("ci.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.uniqueId")
									.set("type", ExamOwner.sOwnerTypeCourse)
									.where("ci.instructor.department.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (instructor && ApplicationProperty.EventExamsConsiderClassInstructorAssignments.isTrue() && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, ClassInstructor ci")
									.where("ci.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = ci.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId")
									.set("type", ExamOwner.sOwnerTypeOffering)
									.where("ci.instructor.department.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (instructor && ApplicationProperty.EventExamsConsiderClassInstructorAssignments.isTrue() && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, ClassInstructor ci")
									.where("ci.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = ci.classInstructing.uniqueId")
									.set("type", ExamOwner.sOwnerTypeClass)
									.where("ci.instructor.department.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (instructor && ApplicationProperty.EventExamsConsiderClassInstructorAssignments.isTrue() && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, ClassInstructor ci")
									.where("ci.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = ci.classInstructing.schedulingSubpart.instrOfferingConfig.uniqueId")
									.set("type", ExamOwner.sOwnerTypeConfig)
									.where("ci.instructor.department.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, CourseOffering co inner join co.instructionalOffering.offeringCoordinators cc")
									.where("cc.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.uniqueId")
									.set("type", ExamOwner.sOwnerTypeCourse)
									.where("cc.instructor.department.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, CourseOffering co inner join co.instructionalOffering.offeringCoordinators cc")
									.where("cc.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
									.set("type", ExamOwner.sOwnerTypeOffering)
									.where("cc.instructor.department.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.offeringCoordinators cc")
									.where("cc.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = c.uniqueId")
									.set("type", ExamOwner.sOwnerTypeClass)
									.where("cc.instructor.department.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, InstrOfferingConfig cfg inner join cfg.instructionalOffering.offeringCoordinators cc")
									.where("cc.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
									.set("type", ExamOwner.sOwnerTypeConfig)
									.where("cc.instructor.department.session.uniqueId = s.uniqueId")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					} else {
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("ExamEvent")
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
									.where("enrl.student.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.uniqueId").set("type", ExamOwner.sOwnerTypeCourse)
									.where("e.exam.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("ExamEvent")
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
									.where("enrl.student.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId").set("type", ExamOwner.sOwnerTypeOffering)
									.where("e.exam.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m").type("ExamEvent")
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
									.where("enrl.student.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = c.uniqueId").set("type", ExamOwner.sOwnerTypeClass)
									.where("e.exam.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m").type("ExamEvent")
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig cfg")
									.where("enrl.student.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = cfg.uniqueId").set("type", ExamOwner.sOwnerTypeConfig)
									.where("e.exam.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());

						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("ExamEvent").from("inner join e.exam.instructors i")
									.where("i.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("e.exam.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (instructor && ApplicationProperty.EventExamsConsiderClassInstructorAssignments.isTrue() && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, ClassInstructor ci inner join ci.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
									.where("ci.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.uniqueId")
									.set("type", ExamOwner.sOwnerTypeCourse)
									.where("ci.instructor.department.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (instructor && ApplicationProperty.EventExamsConsiderClassInstructorAssignments.isTrue() && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, ClassInstructor ci")
									.where("ci.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = ci.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId")
									.set("type", ExamOwner.sOwnerTypeOffering)
									.where("ci.instructor.department.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (instructor && ApplicationProperty.EventExamsConsiderClassInstructorAssignments.isTrue() && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, ClassInstructor ci")
									.where("ci.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = ci.classInstructing.uniqueId")
									.set("type", ExamOwner.sOwnerTypeClass)
									.where("ci.instructor.department.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (instructor && ApplicationProperty.EventExamsConsiderClassInstructorAssignments.isTrue() && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m")
									.type("ExamEvent")
									.from("inner join e.exam.owners o, ClassInstructor ci")
									.where("ci.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = ci.classInstructing.schedulingSubpart.instrOfferingConfig.uniqueId")
									.set("type", ExamOwner.sOwnerTypeConfig)
									.where("ci.instructor.department.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("ExamEvent")
									.from("inner join e.exam.owners o, CourseOffering co inner join co.instructionalOffering.offeringCoordinators cc")
									.where("cc.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.uniqueId").set("type", ExamOwner.sOwnerTypeCourse)
									.where("cc.instructor.department.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("ExamEvent")
									.from("inner join e.exam.owners o, CourseOffering co inner join co.instructionalOffering.offeringCoordinators cc")
									.where("cc.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId").set("type", ExamOwner.sOwnerTypeOffering)
									.where("cc.instructor.department.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m").type("ExamEvent")
									.from("inner join e.exam.owners o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.offeringCoordinators cc")
									.where("cc.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = c.uniqueId").set("type", ExamOwner.sOwnerTypeClass)
									.where("cc.instructor.department.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query
									.select("distinct m").type("ExamEvent")
									.from("inner join e.exam.owners o, InstrOfferingConfig cfg inner join cfg.instructionalOffering.offeringCoordinators cc")
									.where("cc.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = cfg.uniqueId").set("type", ExamOwner.sOwnerTypeConfig)
									.where("cc.instructor.department.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}
					
					if (allSessions) {
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.where("enrl.student.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.where("enrl.student.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.set("type", ExamOwner.sOwnerTypeClass)
								.where("enrl.student.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig cfg")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.where("enrl.student.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						
						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci inner join ci.classInstructing c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.where("ci.instructor.department.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = ci.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.where("ci.instructor.department.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = ci.classInstructing.schedulingSubpart.instrOfferingConfig.uniqueId")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.where("ci.instructor.department.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = ci.classInstructing.uniqueId")
								.set("type", ExamOwner.sOwnerTypeClass)
								.where("ci.instructor.department.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co inner join co.instructionalOffering.offeringCoordinators cc")
								.where("cc.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.where("cc.instructor.department.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co inner join co.instructionalOffering.offeringCoordinators cc")
								.where("cc.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.where("cc.instructor.department.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, InstrOfferingConfig cfg inner join cfg.instructionalOffering.offeringCoordinators cc")
								.where("cc.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.where("cc.instructor.department.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.offeringCoordinators cc")
								.where("cc.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.set("type", ExamOwner.sOwnerTypeClass)
								.where("cc.instructor.department.session.uniqueId = s.uniqueId")
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
					} else {
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.where("enrl.student.session = s")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.where("enrl.student.session = s")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.where("enrl.student.session = s")
								.set("type", ExamOwner.sOwnerTypeClass)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (student && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig cfg")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.where("enrl.student.session = s")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						
						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci inner join ci.classInstructing c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.where("ci.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = ci.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId")
								.where("ci.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = ci.classInstructing.schedulingSubpart.instrOfferingConfig.uniqueId")
								.where("ci.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (instructor && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = ci.classInstructing.uniqueId")
								.where("ci.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeClass)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co inner join co.instructionalOffering.offeringCoordinators cc")
								.where("cc.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.where("cc.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co inner join co.instructionalOffering.offeringCoordinators cc")
								.where("cc.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.where("cc.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.offeringCoordinators cc")
								.where("cc.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.where("cc.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeClass)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (coordinator && (limit <= 0 || meetings.size() < limit))
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, InstrOfferingConfig cfg inner join cfg.instructionalOffering.offeringCoordinators cc")
								.where("cc.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.where("cc.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
					}

					if (contact && (limit <= 0 || meetings.size() < limit))
						meetings.addAll(query.select("distinct m")
                    		.where("e.class in (CourseEvent, SpecialEvent, UnavailableEvent)")
                    		.where("e.mainContact.externalUniqueId = :externalId")
                    		.set("externalId", request.getResourceExternalId())
                    		.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
                    		.query(hibSession).list());
                    
					if (contact && (limit <= 0 || meetings.size() < limit))
						meetings.addAll(query.select("distinct m")
                    		.from("inner join m.event.additionalContacts c")
                    		.where("c.externalUniqueId = :externalId")
                    		.set("externalId", request.getResourceExternalId())
                    		.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
                    		.query(hibSession).list());

					if (contact && (limit <= 0 || meetings.size() < limit))
						meetings.addAll(query.select("distinct m")
                    		.from("EventContact c")
                    		.where("c.externalUniqueId = :externalId")
                    		.where("c.emailAddress is not null")
                    		.where("lower(m.event.email) like '%' || lower(c.emailAddress) || '%'")
                    		.set("externalId", request.getResourceExternalId())
                    		.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
                    		.query(hibSession).list());
                    break;
				case GROUP:
					allSessions = request.getEventFilter().hasOption("flag") && request.getEventFilter().getOptions("flag").contains("All Sessions");
					curriculumCourses = new HashSet<Long>();
					curriculumClasses = new HashSet<Long>();
					Integer minEnrollment = null;
					String pMinEnrl = ApplicationProperty.StudentGroupsTimetableMinimalEnrollment.value();
					if (pMinEnrl != null) {
						if (pMinEnrl.endsWith("%"))
							minEnrollment = (int)Math.floor(Double.parseDouble(pMinEnrl.substring(0, pMinEnrl.length() - 1)) * group.getStudents().size() / 100.0);
						else
							minEnrollment = Integer.parseInt(pMinEnrl);
					}
					
					if (allSessions) {
						if (group.getExternalUniqueId() != null) {
							curriculumCourses.addAll(hibSession.createQuery("select distinct e.courseOffering.uniqueId from StudentGroup g inner join g.students s inner join s.classEnrollments e where g.externalId = :externalId")
									.setString("externalId", group.getExternalUniqueId()).list());
							curriculumClasses.addAll(hibSession.createQuery("select distinct e.clazz.uniqueId from StudentGroup g inner join g.students s inner join s.classEnrollments e where g.externalId = :externalId")
									.setString("externalId", group.getExternalUniqueId()).list());
						} else {
							curriculumCourses.addAll(hibSession.createQuery("select distinct e.courseOffering.uniqueId from StudentGroup g inner join g.students s inner join s.classEnrollments e where g.groupAbbreviation = :abbreviation")
									.setString("abbreviation", group.getGroupAbbreviation()).list());
							curriculumClasses.addAll(hibSession.createQuery("select distinct e.clazz.uniqueId from StudentGroup g inner join g.students s inner join s.classEnrollments e where g.groupAbbreviation = :abbreviation")
									.setString("abbreviation", group.getGroupAbbreviation()).list());
						}
					} else {
						curriculumCourses.addAll(hibSession.createQuery("select distinct e.courseOffering.uniqueId from StudentGroup g inner join g.students s inner join s.classEnrollments e where g.uniqueId = :resourceId")
								.setLong("resourceId", group.getUniqueId()).list());
						curriculumClasses.addAll(hibSession.createQuery("select distinct e.clazz.uniqueId from StudentGroup g inner join g.students s inner join s.classEnrollments e where g.uniqueId = :resourceId")
								.setLong("resourceId", group.getUniqueId()).list());
					}
					meetings = new ArrayList<Meeting>();

					if (limit <= 0 || meetings.size() < limit) {
						EventInstance ec = query.select("distinct m").type("ClassEvent").from("inner join e.clazz.studentEnrollments enrl inner join enrl.student.groups grp");
						if (minEnrollment != null)
							ec.where("(select count(enrlX) from StudentClassEnrollment enrlX inner join enrlX.student.groups grpX where grpX.uniqueId = grp.uniqueId and enrlX.clazz.uniqueId = e.clazz.uniqueId) >= :minEnrl")
								.set("minEnrl", minEnrollment);
						if (allSessions) {
							if (group.getExternalUniqueId() != null)
								ec.where("grp.externalId = :externalId").set("externalId", group.getExternalUniqueId()).where("enrl.student.session.uniqueId = s.uniqueId");
							else
								ec.where("grp.groupAbbreviation = :abbreviation").set("abbreviation", group.getGroupAbbreviation()).where("enrl.student.session.uniqueId = s.uniqueId");
						} else {
							ec.where("grp.uniqueId = :resourceId").set("resourceId", group.getUniqueId());
						}
						meetings.addAll(ec.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}
					
					if (limit <= 0 || meetings.size() < limit) {
						EventInstance ec = query.select("distinct m").type("ExamEvent").from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co inner join enrl.student.groups grp")
								.where("o.ownerType = :type and o.ownerId = co.uniqueId").set("type", ExamOwner.sOwnerTypeCourse);
						if (minEnrollment != null)
							ec.where("(select count(enrlX) from StudentClassEnrollment enrlX inner join enrlX.student.groups grpX where grpX.uniqueId = grp.uniqueId and enrlX.courseOffering.uniqueId = co.uniqueId) >= :minEnrl")
								.set("minEnrl", minEnrollment);
						if (allSessions) {
							if (group.getExternalUniqueId() != null)
								ec.where("grp.externalId = :externalId").set("externalId", group.getExternalUniqueId()).where("enrl.student.session.uniqueId = s.uniqueId");
							else
								ec.where("grp.groupAbbreviation = :abbreviation").set("abbreviation", group.getGroupAbbreviation()).where("enrl.student.session.uniqueId = s.uniqueId");
						} else {
							ec.where("grp.uniqueId = :resourceId").set("resourceId", group.getUniqueId());
						}
						meetings.addAll(ec.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}
					
					if (limit <= 0 || meetings.size() < limit) {
						EventInstance ec = query.select("distinct m").type("ExamEvent").from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co inner join enrl.student.groups grp")
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId").set("type", ExamOwner.sOwnerTypeOffering);
						if (minEnrollment != null)
							ec.where("(select count(enrlX) from StudentClassEnrollment enrlX inner join enrlX.student.groups grpX where grpX.uniqueId = grp.uniqueId and enrlX.courseOffering.uniqueId = co.uniqueId) >= :minEnrl")
								.set("minEnrl", minEnrollment);
						if (allSessions) {
							if (group.getExternalUniqueId() != null)
								ec.where("grp.externalId = :externalId").set("externalId", group.getExternalUniqueId()).where("enrl.student.session.uniqueId = s.uniqueId");
							else
								ec.where("grp.groupAbbreviation = :abbreviation").set("abbreviation", group.getGroupAbbreviation()).where("enrl.student.session.uniqueId = s.uniqueId");
						} else {
							ec.where("grp.uniqueId = :resourceId").set("resourceId", group.getUniqueId());
						}
						meetings.addAll(ec.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}
					
					if (limit <= 0 || meetings.size() < limit) {
						EventInstance ec = query.select("distinct m").type("ExamEvent").from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join enrl.student.groups grp")
								.where("o.ownerType = :type and o.ownerId = c.uniqueId").set("type", ExamOwner.sOwnerTypeClass);
						if (minEnrollment != null)
							ec.where("(select count(enrlX) from StudentClassEnrollment enrlX inner join enrlX.student.groups grpX where grpX.uniqueId = grp.uniqueId and enrlX.clazz.uniqueId = c.uniqueId) >= :minEnrl")
								.set("minEnrl", minEnrollment);
						if (allSessions) {
							if (group.getExternalUniqueId() != null)
								ec.where("grp.externalId = :externalId").set("externalId", group.getExternalUniqueId()).where("enrl.student.session.uniqueId = s.uniqueId");
							else
								ec.where("grp.groupAbbreviation = :abbreviation").set("abbreviation", group.getGroupAbbreviation()).where("enrl.student.session.uniqueId = s.uniqueId");
						} else {
							ec.where("grp.uniqueId = :resourceId").set("resourceId", group.getUniqueId());
						}
						meetings.addAll(ec.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}
					
					if (limit <= 0 || meetings.size() < limit) {
						EventInstance ec = query.select("distinct m").type("ExamEvent").from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig cfg inner join enrl.student.groups grp")
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId").set("type", ExamOwner.sOwnerTypeConfig);
						if (minEnrollment != null)
							ec.where("(select count(enrlX) from StudentClassEnrollment enrlX inner join enrlX.student.groups grpX where grpX.uniqueId = grp.uniqueId and enrlX.clazz.uniqueId = c.uniqueId) >= :minEnrl")
								.set("minEnrl", minEnrollment);
						if (allSessions) {
							if (group.getExternalUniqueId() != null)
								ec.where("grp.externalId = :externalId").set("externalId", group.getExternalUniqueId()).where("enrl.student.session.uniqueId = s.uniqueId");
							else
								ec.where("grp.groupAbbreviation = :abbreviation").set("abbreviation", group.getGroupAbbreviation()).where("enrl.student.session.uniqueId = s.uniqueId");
						} else {
							ec.where("grp.uniqueId = :resourceId").set("resourceId", group.getUniqueId());
						}
						meetings.addAll(ec.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}
					
					
					if (limit <= 0 || meetings.size() < limit) {
						EventInstance ec = query.select("distinct m").type("CourseEvent").from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.courseOffering co inner join enrl.student.groups grp")
								.where("o.ownerType = :type and o.ownerId = co.uniqueId").set("type", ExamOwner.sOwnerTypeCourse);
						if (minEnrollment != null)
							ec.where("(select count(enrlX) from StudentClassEnrollment enrlX inner join enrlX.student.groups grpX where grpX.uniqueId = grp.uniqueId and enrlX.courseOffering.uniqueId = co.uniqueId) >= :minEnrl")
								.set("minEnrl", minEnrollment);
						if (allSessions) {
							if (group.getExternalUniqueId() != null)
								ec.where("grp.externalId = :externalId").set("externalId", group.getExternalUniqueId()).where("enrl.student.session.uniqueId = s.uniqueId");
							else
								ec.where("grp.groupAbbreviation = :abbreviation").set("abbreviation", group.getGroupAbbreviation()).where("enrl.student.session.uniqueId = s.uniqueId");
						} else {
							ec.where("grp.uniqueId = :resourceId").set("resourceId", group.getUniqueId());
						}
						meetings.addAll(ec.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}
					
					if (limit <= 0 || meetings.size() < limit) {
						EventInstance ec = query.select("distinct m").type("CourseEvent").from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.courseOffering co inner join enrl.student.groups grp")
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId").set("type", ExamOwner.sOwnerTypeOffering);
						if (minEnrollment != null)
							ec.where("(select count(enrlX) from StudentClassEnrollment enrlX inner join enrlX.student.groups grpX where grpX.uniqueId = grp.uniqueId and enrlX.courseOffering.uniqueId = co.uniqueId) >= :minEnrl")
								.set("minEnrl", minEnrollment);
						if (allSessions) {
							if (group.getExternalUniqueId() != null)
								ec.where("grp.externalId = :externalId").set("externalId", group.getExternalUniqueId()).where("enrl.student.session.uniqueId = s.uniqueId");
							else
								ec.where("grp.groupAbbreviation = :abbreviation").set("abbreviation", group.getGroupAbbreviation()).where("enrl.student.session.uniqueId = s.uniqueId");
						} else {
							ec.where("grp.uniqueId = :resourceId").set("resourceId", group.getUniqueId());
						}
						meetings.addAll(ec.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}
					
					if (limit <= 0 || meetings.size() < limit) {
						EventInstance ec = query.select("distinct m").type("CourseEvent").from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.clazz c inner join enrl.student.groups grp")
								.where("o.ownerType = :type and o.ownerId = c.uniqueId").set("type", ExamOwner.sOwnerTypeClass);
						if (minEnrollment != null)
							ec.where("(select count(enrlX) from StudentClassEnrollment enrlX inner join enrlX.student.groups grpX where grpX.uniqueId = grp.uniqueId and enrlX.clazz.uniqueId = c.uniqueId) >= :minEnrl")
								.set("minEnrl", minEnrollment);
						if (allSessions) {
							if (group.getExternalUniqueId() != null)
								ec.where("grp.externalId = :externalId").set("externalId", group.getExternalUniqueId()).where("enrl.student.session.uniqueId = s.uniqueId");
							else
								ec.where("grp.groupAbbreviation = :abbreviation").set("abbreviation", group.getGroupAbbreviation()).where("enrl.student.session.uniqueId = s.uniqueId");
						} else {
							ec.where("grp.uniqueId = :resourceId").set("resourceId", group.getUniqueId());
						}
						meetings.addAll(ec.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}
					
					if (limit <= 0 || meetings.size() < limit) {
						EventInstance ec = query.select("distinct m").type("CourseEvent").from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig cfg inner join enrl.student.groups grp")
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId").set("type", ExamOwner.sOwnerTypeConfig);
						if (minEnrollment != null)
							ec.where("(select count(enrlX) from StudentClassEnrollment enrlX inner join enrlX.student.groups grpX where grpX.uniqueId = grp.uniqueId and enrlX.clazz.uniqueId = c.uniqueId) >= :minEnrl")
								.set("minEnrl", minEnrollment);
						if (allSessions) {
							if (group.getExternalUniqueId() != null)
								ec.where("grp.externalId = :externalId").set("externalId", group.getExternalUniqueId()).where("enrl.student.session.uniqueId = s.uniqueId");
							else
								ec.where("grp.groupAbbreviation = :abbreviation").set("abbreviation", group.getGroupAbbreviation()).where("enrl.student.session.uniqueId = s.uniqueId");
						} else {
							ec.where("grp.uniqueId = :resourceId").set("resourceId", group.getUniqueId());
						}
						meetings.addAll(ec.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}
                    break;
                default:
					throw new GwtRpcException("Resource type " + request.getResourceType().getLabel() + " not supported.");
				}
				
				GwtRpcResponseList<EventInterface> ret = new GwtRpcResponseList<EventInterface>();
				Hashtable<Long, EventInterface> events = new Hashtable<Long, EventInterface>();
				Map<Long, Set<Location>> unavailableLocations = new Hashtable<Long, Set<Location>>();
				for (Meeting m: meetings) {
					if (locationMap != null && m.getLocationPermanentId() != null) {
						Location location = locationMap.get(m.getLocationPermanentId());
						if (location != null) m.setLocation(location);
					}
					EventInterface event = events.get(m.getEvent().getUniqueId());
					if (event == null) {
						event = new EventInterface();
						event.setId(m.getEvent().getUniqueId());
						event.setName(m.getEvent().getEventName());
						event.setType(EventInterface.EventType.values()[m.getEvent().getEventType()]);
						events.put(m.getEvent().getUniqueId(), event);
						event.setCanView(context.hasPermission(m.getEvent(), Right.EventDetail));
						event.setMaxCapacity(m.getEvent().getMaxCapacity());
						
						if (m.getEvent().getMainContact() != null) {
							ContactInterface contact = new ContactInterface();
							contact.setFirstName(m.getEvent().getMainContact().getFirstName());
							contact.setMiddleName(m.getEvent().getMainContact().getMiddleName());
							contact.setLastName(m.getEvent().getMainContact().getLastName());
							contact.setAcademicTitle(m.getEvent().getMainContact().getAcademicTitle());
							contact.setEmail(m.getEvent().getMainContact().getEmailAddress());
							contact.setFormattedName(m.getEvent().getMainContact().getName(nameFormat));
							event.setContact(contact);
						}
						for (EventContact additional: m.getEvent().getAdditionalContacts()) {
							ContactInterface contact = new ContactInterface();
							contact.setFirstName(additional.getFirstName());
							contact.setMiddleName(additional.getMiddleName());
							contact.setLastName(additional.getLastName());
							contact.setAcademicTitle(additional.getAcademicTitle());
							contact.setEmail(additional.getEmailAddress());
							contact.setFormattedName(additional.getName(nameFormat));
							event.addAdditionalContact(contact);
						}
						event.setEmail(m.getEvent().getEmail());
						if (m.getEvent().getSponsoringOrganization() != null) {
							SponsoringOrganizationInterface sponsor = new SponsoringOrganizationInterface();
							sponsor.setEmail(m.getEvent().getSponsoringOrganization().getEmail());
							sponsor.setName(m.getEvent().getSponsoringOrganization().getName());
							sponsor.setUniqueId(m.getEvent().getSponsoringOrganization().getUniqueId());
							event.setSponsor(sponsor);
						}
						event.setExpirationDate(m.getEvent().getExpirationDate());
						
				    	if (Event.sEventTypeClass == m.getEvent().getEventType()) {
				    		ClassEvent ce = ClassEventDAO.getInstance().get(m.getEvent().getUniqueId(), hibSession);
				    		Class_ clazz = ce.getClazz();
				    		
				    		Set<Long>[] r = (restrictions == null ? null : restrictions.get(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId()));
				    		if (r != null && hide(r, clazz)) continue;
				    		
							event.setEnrollment(clazz.getEnrollment());
							if (groupEnrollments) {
								int enrl = 0;
								for (StudentClassEnrollment e: clazz.getStudentEnrollments())
									if (group.getStudents().contains(e.getStudent())) enrl ++;
								event.setEnrollment(enrl);
							}
							event.setMaxCapacity(clazz.getClassLimit());
				    		if (clazz.getDisplayInstructor()) {
				    			for (ClassInstructor i: clazz.getClassInstructors()) {
									ContactInterface instructor = new ContactInterface();
									instructor.setFirstName(i.getInstructor().getFirstName());
									instructor.setMiddleName(i.getInstructor().getMiddleName());
									instructor.setLastName(i.getInstructor().getLastName());
									instructor.setAcademicTitle(i.getInstructor().getAcademicTitle());
									instructor.setEmail(i.getInstructor().getEmail());
									instructor.setFormattedName(i.getInstructor().getName(nameFormat));
									event.addInstructor(instructor);
				    			}
				    		}
				    		CourseOffering correctedOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
				    		List<CourseOffering> courses = new ArrayList<CourseOffering>(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings());
				    		boolean instructing = false;
				    		if (request.getResourceType() == ResourceType.PERSON && request.getResourceExternalId() != null) {
				    			for (ClassInstructor i: clazz.getClassInstructors())
				    				if (request.getResourceExternalId().equals(i.getInstructor().getExternalUniqueId())) { instructing = true; break; }
				    		}
				    		switch (request.getResourceType()) {
				    		/*
				    		case SUBJECT:
			    				for (Iterator<CourseOffering> i = courses.iterator(); i.hasNext(); ) {
			    					CourseOffering co = i.next();
			    					if (co.getSubjectArea().getUniqueId().equals(request.getResourceId())) {
			    						if (!correctedOffering.getSubjectArea().getUniqueId().equals(request.getResourceId()))
			    							correctedOffering = co;
			    					} else {
			    						i.remove();
			    					}
			    				}
				    			break;
				    		case COURSE:
			    				for (Iterator<CourseOffering> i = courses.iterator(); i.hasNext(); ) {
			    					CourseOffering co = i.next();
			    					if (co.getUniqueId().equals(request.getResourceId())) {
			    						if (!correctedOffering.getUniqueId().equals(request.getResourceId()))
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
			    					if (co.getSubjectArea().getDepartment().getUniqueId().equals(request.getResourceId())) {
			    						if (!correctedOffering.getSubjectArea().getDepartment().getUniqueId().equals(request.getResourceId()))
			    							correctedOffering = co;
			    					} else {
			    						i.remove();
			    					}
			    				}
				    			break;
				    		*/
				    		case CURRICULUM:
				    		case PERSON:
				    		case GROUP:
			    				for (Iterator<CourseOffering> i = courses.iterator(); i.hasNext(); ) {
			    					CourseOffering co = i.next();
			    					if (curriculumCourses.contains(co.getUniqueId())) {
			    						if (!curriculumCourses.contains(correctedOffering.getUniqueId()))
			    							correctedOffering = co;
			    					} else {
			    						if (!instructing) i.remove();
			    					}
			    				}
				    			break;
				    		}
				    		courses.remove(correctedOffering);
				    		event.addCourseName(correctedOffering.getCourseName());
				    		event.addCourseTitle(correctedOffering.getTitle() == null ? "" : correctedOffering.getTitle());
				    		event.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc().length() <= 20 ? clazz.getSchedulingSubpart().getItype().getDesc() : clazz.getSchedulingSubpart().getItype().getAbbv());
				    		if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() != null)
				    			event.setInstruction(event.getInstruction() + " (" + clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
				    		event.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
				    		event.setSectionNumber(clazz.getSectionNumberString(hibSession));
				    		if (clazz.getClassSuffix(correctedOffering) == null) {
					    		event.setName(clazz.getClassLabel(correctedOffering));
				    		} else {
					    		event.addExternalId(clazz.getClassSuffix(correctedOffering));
				    			event.setName(correctedOffering.getCourseName() + " " + clazz.getClassSuffix(correctedOffering));
				    		}
			    			for (CourseOffering co: courses) {
					    		event.addCourseName(co.getCourseName());
					    		event.addCourseTitle(co.getTitle() == null ? "" : co.getTitle());
					    		if (clazz.getClassSuffix(co) != null)
					    			event.addExternalId(clazz.getClassSuffix(co));
			    			}
			    			if (correctedOffering.getScheduleBookNote() != null && !correctedOffering.getScheduleBookNote().isEmpty()) {
			    				NoteInterface note = new NoteInterface();
			    				note.setId(-2l);
			    				note.setNote(correctedOffering.getScheduleBookNote());
			    				event.addNote(note);
			    			}
				    		if (clazz.getSchedulePrintNote() != null && !clazz.getSchedulePrintNote().isEmpty()) {
				    			NoteInterface note = new NoteInterface();
			    				note.setId(-1l);
			    				note.setNote(clazz.getSchedulePrintNote());
			    				event.addNote(note);
				    		}
				    	} else if (Event.sEventTypeFinalExam == m.getEvent().getEventType() || Event.sEventTypeMidtermExam == m.getEvent().getEventType()) {
				    		ExamEvent xe = ExamEventDAO.getInstance().get(m.getEvent().getUniqueId(), hibSession);
				    		event.setEnrollment(xe.getExam().countStudents());
				    		event.setMaxCapacity(xe.getExam().getSize());
				    		if (groupEnrollments) {
								int enrl = 0;
								Set<Long> studentIds = xe.getExam().getStudentIds();
								for (Student s: group.getStudents())
									if (studentIds.contains(s.getUniqueId())) enrl ++;
								event.setEnrollment(enrl);
							}
			    			for (DepartmentalInstructor i: xe.getExam().getInstructors()) {
								ContactInterface instructor = new ContactInterface();
								instructor.setFirstName(i.getFirstName());
								instructor.setMiddleName(i.getMiddleName());
								instructor.setLastName(i.getLastName());
								instructor.setAcademicTitle(i.getAcademicTitle());
								instructor.setEmail(i.getEmail());
								instructor.setFormattedName(i.getName(nameFormat));
								event.addInstructor(instructor);
			    			}
				    		boolean instructing = false;
				    		if (request.getResourceType() == ResourceType.PERSON && request.getResourceExternalId() != null) {
				    			for (DepartmentalInstructor i: xe.getExam().getInstructors())
				    				if (request.getResourceExternalId().equals(i.getExternalUniqueId())) { instructing = true; break; }
				    		}
			    			String name = null;
			    			for (ExamOwner owner: new TreeSet<ExamOwner>(xe.getExam().getOwners())) {
			    				if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass && !instructing && curriculumClasses != null && !curriculumClasses.contains(owner.getOwnerId())) continue;
			    				TreeSet<CourseOffering> courses = new TreeSet<CourseOffering>();
			    				if (owner.getOwnerType() == ExamOwner.sOwnerTypeCourse || request.getResourceType() == ResourceType.ROOM) {
			    					courses.add(owner.getCourse());
			    				} else {
			    					courses.addAll(owner.getCourse().getInstructionalOffering().getCourseOfferings());
			    				}
			    				courses: for(CourseOffering course: courses) {
						    		switch (request.getResourceType()) {
						    		/*
						    		case SUBJECT:
						    			if (!course.getSubjectArea().getUniqueId().equals(request.getResourceId())) continue courses;
						    			break;
						    		case COURSE:
						    			if (!course.getUniqueId().equals(request.getResourceId())) continue courses;
						    			break;
						    		case DEPARTMENT:
						    			if (department.isExternalManager()) break courses;
						    			if (!course.getSubjectArea().getDepartment().getUniqueId().equals(request.getResourceId())) continue courses;
						    			break;
						    		*/
						    		case CURRICULUM:
						    		case PERSON:
						    		case GROUP:
						    			if ((!instructing || !course.isIsControl()) && !curriculumCourses.contains(course.getUniqueId())) continue courses;
						    			break;
						    		}
						    		event.addCourseName(course.getCourseName());
						    		event.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
						    		name = course.getCourseName();
				    				switch (owner.getOwnerType()) {
				    				case ExamOwner.sOwnerTypeClass:
				    					Class_ clazz = (Class_)owner.getOwnerObject();
				    					if (clazz.getClassSuffix(course) == null) {
				    						event.addExternalId(clazz.getItypeDesc().trim() + " " + clazz.getSectionNumberString(hibSession));
				    						name = course.getCourseName() + " " + clazz.getClassLabel(course);
				    					} else {
				    						event.addExternalId(clazz.getClassSuffix(course));
				    						name = course.getCourseName() + " " + clazz.getClassSuffix(course);
				    					}
				    					break;
				    				case ExamOwner.sOwnerTypeConfig:
				    					InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
				    					event.addExternalId("[" + config.getName() + "]");
				    					break;
				    				case ExamOwner.sOwnerTypeCourse:
				    					event.addExternalId(MESSAGES.colCourse());
				    					break;
				    				case ExamOwner.sOwnerTypeOffering:
				    					event.addExternalId(MESSAGES.colOffering());
				    					break;
				    				}
			    				}
			    			}
			    			if (event.hasCourseNames() && event.getCourseNames().size() == 1 && (request.getResourceType() == ResourceType.PERSON || request.getResourceType() == ResourceType.CURRICULUM || request.getResourceType() == ResourceType.GROUP))
		    					event.setName(name);
				    	} else if (Event.sEventTypeCourse == m.getEvent().getEventType()) {
				    		CourseEvent ce = CourseEventDAO.getInstance().get(m.getEvent().getUniqueId(), hibSession);
				    		event.setRequiredAttendance(ce.isReqAttendance());
							int enrl = 0;
							int cap = 0;
							boolean instructing = false;
							if (request.getResourceType() == ResourceType.PERSON && request.getResourceExternalId() != null) {
								if (request.getResourceExternalId().equals(m.getEvent().getMainContact().getExternalUniqueId())) instructing = true;
								if (!instructing)
									for (EventContact contact: m.getEvent().getAdditionalContacts()) {
										if (request.getResourceExternalId().equals(contact.getExternalUniqueId())) { instructing = true; break; }
									}
							}
							for (RelatedCourseInfo owner: ce.getRelatedCourses()) {
								if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass && !instructing && curriculumClasses != null && !curriculumClasses.contains(owner.getOwnerId())) continue;
								if (groupEnrollments) {
									Set<Long> studentIds = new HashSet<Long>(owner.getStudentIds());
									for (Student s: group.getStudents())
										if (studentIds.contains(s.getUniqueId())) enrl ++;
									event.setEnrollment(enrl);
								} else {
									enrl += owner.countStudents();
								}
								cap += owner.getLimit();
			    				TreeSet<CourseOffering> courses = new TreeSet<CourseOffering>();
			    				if (owner.getOwnerType() == ExamOwner.sOwnerTypeCourse || request.getResourceType() == ResourceType.ROOM) {
			    					courses.add(owner.getCourse());
			    				} else {
			    					courses.addAll(owner.getCourse().getInstructionalOffering().getCourseOfferings());
			    				}
			    				courses: for(CourseOffering course: courses) {
						    		switch (request.getResourceType()) {
						    		/*
						    		case SUBJECT:
						    			if (!course.getSubjectArea().getUniqueId().equals(request.getResourceId())) continue courses;
						    			break;
						    		case COURSE:
						    			if (!course.getUniqueId().equals(request.getResourceId())) continue courses;
						    			break;
						    		case DEPARTMENT:
						    			if (department.isExternalManager()) break courses;
						    			if (!course.getSubjectArea().getDepartment().getUniqueId().equals(request.getResourceId())) continue courses;
						    			break;
						    		*/
						    		case CURRICULUM:
						    		case PERSON:
						    		case GROUP:
						    			if ((!instructing || !course.isIsControl()) && !curriculumCourses.contains(course.getUniqueId())) continue courses;
						    			break;
						    		}
						    		event.addCourseName(course.getCourseName());
						    		event.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
				    				switch (owner.getOwnerType()) {
				    				case ExamOwner.sOwnerTypeClass:
				    					Class_ clazz = (Class_)owner.getOwnerObject();
				    					if (clazz.getClassSuffix(course) == null) {
				    						event.addExternalId(clazz.getClassLabel(course));
				    					} else {
				    						event.addExternalId(clazz.getClassSuffix(course));
				    					}
				    					break;
				    				case ExamOwner.sOwnerTypeConfig:
				    					InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
				    					event.addExternalId("[" + config.getName() + (config.getInstructionalMethod() == null ? "" : " " + config.getInstructionalMethod().getLabel()) + "]");
				    					break;
				    				case ExamOwner.sOwnerTypeCourse:
				    					event.addExternalId(MESSAGES.colCourse());
				    					break;
				    				case ExamOwner.sOwnerTypeOffering:
				    					event.addExternalId(MESSAGES.colOffering());
				    					break;
				    				}
			    				}
			    			}
							event.setEnrollment(enrl);
							event.setMaxCapacity(cap);
				    	}
				    	
				    	if (event.isCanView()) {
							for (EventNote n: m.getEvent().getNotes()) {
				    			NoteInterface note = new NoteInterface();
				    			note.setId(n.getUniqueId());
				        		note.setDate(n.getTimeStamp());
				        		note.setType(NoteInterface.NoteType.values()[n.getNoteType()]);
				        		note.setMeetings(n.getMeetingsHtml());
				        		note.setNote(n.getTextNote());
				        		note.setUser(n.getUser());
				        		note.setAttachment(n.getAttachedName());
				        		note.setLink(n.getAttachedName() == null ? null : QueryEncoderBackend.encode("event=" + m.getEvent().getUniqueId() + "&note=" + n.getUniqueId()));
				    			event.addNote(note);
							}
				    	}
				    	
				    	event.setSequence(m.getEvent().getNotes().size());
				    	for (EventNote n: m.getEvent().getNotes())
				    		if (n.getTimeStamp() != null && (!event.hasTimeStamp() || event.getTimeStamp().compareTo(n.getTimeStamp()) < 0))
				    			event.setTimeStamp(n.getTimeStamp());
				    	
						ret.add(event);
					}
					MeetingInterface meeting = new MeetingInterface();
					meeting.setId(m.getUniqueId());
					meeting.setMeetingDate(m.getMeetingDate());
					meeting.setDayOfWeek(Constants.getDayOfWeek(m.getMeetingDate()));
					meeting.setStartTime(m.getStartTime().getTime());
					meeting.setStopTime(m.getStopTime().getTime());
					meeting.setDayOfYear(CalendarUtils.date2dayOfYear(session.getSessionStartYear(), m.getMeetingDate()));
					meeting.setStartSlot(m.getStartPeriod());
					meeting.setEndSlot(m.getStopPeriod());
					meeting.setStartOffset(m.getStartOffset() == null ? 0 : m.getStartOffset());
					meeting.setEndOffset(m.getStopOffset() == null ? 0 : m.getStopOffset());
					meeting.setPast(context.isPastOrOutside(m.getStartTime()));
					meeting.setCanEdit(context.hasPermission(m, Right.EventMeetingEdit));
					meeting.setCanInquire(context.hasPermission(m, Right.EventMeetingInquire) ||
							(m.getEvent().getEventType() == Event.sEventTypeClass && context.hasPermission(m, Right.EventMeetingInquireClass)) ||
							(m.getEvent().getEventType() == Event.sEventTypeFinalExam && context.hasPermission(m, Right.EventMeetingInquireExam)) ||
							(m.getEvent().getEventType() == Event.sEventTypeMidtermExam && context.hasPermission(m, Right.EventMeetingInquireExam))
							);
					meeting.setCanApprove(context.hasPermission(m, Right.EventMeetingApprove));
					meeting.setCanDelete(context.hasPermission(m, Right.EventMeetingDelete));
					meeting.setCanCancel(context.hasPermission(m, Right.EventMeetingCancel) ||
							(m.getEvent().getEventType() == Event.sEventTypeClass && context.hasPermission(m, Right.EventMeetingCancelClass)) ||
							(m.getEvent().getEventType() == Event.sEventTypeFinalExam && context.hasPermission(m, Right.EventMeetingCancelExam)) ||
							(m.getEvent().getEventType() == Event.sEventTypeMidtermExam && context.hasPermission(m, Right.EventMeetingCancelExam))
							);
					meeting.setApprovalDate(m.getApprovalDate());
					meeting.setApprovalStatus(m.getApprovalStatus());
					if (m.getLocation() != null) {
						ResourceInterface location = new ResourceInterface();
						location.setType(ResourceType.ROOM);
						location.setId(m.getLocation().getUniqueId());
						location.setName(m.getLocation().getLabel());
						location.setSize(m.getLocation().getCapacity());
						location.setDistance(distances.get(m.getLocation().getUniqueId()));
						location.setRoomType(m.getLocation().getRoomTypeLabel());
						location.setBreakTime(m.getLocation().getEffectiveBreakTime());
						location.setMessage(m.getLocation().getEventMessage());
						location.setIgnoreRoomCheck(m.getLocation().isIgnoreRoomCheck());
						meeting.setLocation(location);
					}
					if (request.getEventFilter().hasOptions("flag") && request.getEventFilter().getOptions("flag").contains("Conflicts")) {
						if (m.getLocation() != null && m.getLocation().getEventAvailability() != null && m.getLocation().getEventAvailability().length() == Constants.SLOTS_PER_DAY * Constants.DAY_CODES.length) {
							check: for (int slot = meeting.getStartSlot(); slot < meeting.getEndSlot(); slot++) {
								if (m.getLocation().getEventAvailability().charAt(meeting.getDayOfWeek() * Constants.SLOTS_PER_DAY + slot) == '1') {
									Set<Location> locations = unavailableLocations.get(event.getId());
									if (locations == null) {
										locations = new HashSet<Location>();
										unavailableLocations.put(event.getId(), locations);
									}
									locations.add(m.getLocation());
									break check;
								}
							}
						}
					}
					event.addMeeting(meeting);
				}
				
				if (request.getEventFilter().hasOptions("flag") && request.getEventFilter().getOptions("flag").contains("Conflicts")) {
					request.getEventFilter().setOption("mode", "Conflicting Events");
					query = EventFilterBackend.getQuery(request.getEventFilter(), context);
					
					List<Object[]> conflicts = null;
					switch (request.getResourceType()) {
					case ROOM:
						if (request.getResourceId() == null)
							conflicts = (List<Object[]>)query.select("distinct m.event.uniqueId, Xm").query(hibSession).list();
						else
							conflicts = (List<Object[]>)query.select("distinct m.event.uniqueId, Xm")
								.joinWithLocation()
								.where("l.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.query(hibSession).list();
						break;
					case SUBJECT:
					case COURSE:
						String resourceCheck = (request.getResourceType() == ResourceType.SUBJECT ? "co.subjectArea.uniqueId = :resourceId" : "co.uniqueId = :resourceId");
						
						conflicts = new ArrayList<Object[]>();
						
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ClassEvent")
								.from("inner join e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where(resourceCheck)
								.set("resourceId", request.getResourceId())
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co")
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.where(resourceCheck)
								.set("resourceId", request.getResourceId())
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co")
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.where(resourceCheck)
								.set("resourceId", request.getResourceId())
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
								.from("inner join e.relatedCourses o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.set("type", ExamOwner.sOwnerTypeClass)
								.where(resourceCheck)
								.set("resourceId", request.getResourceId())
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg")
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.where(resourceCheck)
								.set("resourceId", request.getResourceId())
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
								.from("inner join e.exam.owners o, CourseOffering co")
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.where(resourceCheck)
								.set("resourceId", request.getResourceId())
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
								.from("inner join e.exam.owners o, CourseOffering co")
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.where(resourceCheck)
								.set("resourceId", request.getResourceId())
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
								.from("inner join e.exam.owners o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.set("type", ExamOwner.sOwnerTypeClass)
								.where(resourceCheck)
								.set("resourceId", request.getResourceId())
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
								.from("inner join e.exam.owners o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg")
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.where(resourceCheck)
								.set("resourceId", request.getResourceId())
								.query(hibSession).list());

						break;			
					case CURRICULUM:
						conflicts = new ArrayList<Object[]>();

						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ClassEvent")
								.from("inner join e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc")
								.where("co = cc.course")
								.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
								.from("inner join e.exam.owners o, CourseOffering co, CurriculumCourse cc")
								.where("co = cc.course")
								.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
								.from("inner join e.exam.owners o, CourseOffering co, CurriculumCourse cc")
								.where("co = cc.course")
								.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
								.from("inner join e.exam.owners o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc")
								.where("co = cc.course")
								.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.set("type", ExamOwner.sOwnerTypeClass)
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
								.from("inner join e.exam.owners o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg, CurriculumCourse cc")
								.where("co = cc.course")
								.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co, CurriculumCourse cc")
								.where("co = cc.course")
								.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co, CurriculumCourse cc")
								.where("co = cc.course")
								.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
								.from("inner join e.relatedCourses o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc")
								.where("co = cc.course")
								.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.set("type", ExamOwner.sOwnerTypeClass)
								.query(hibSession).list());
						conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
								.from("inner join e.relatedCourses o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg, CurriculumCourse cc")
								.where("co = cc.course")
								.where("cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId")
								.set("resourceId", request.getResourceId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.query(hibSession).list());

						break;
						
					case DEPARTMENT:

						if (department.isExternalManager()) {
							conflicts = (List<Object[]>)query.select("distinct m.event.uniqueId, Xm").type("ClassEvent")
										.from("inner join e.clazz.managingDept d")
										.where("d.uniqueId = :resourceId")
										.set("resourceId", request.getResourceId())
										.query(hibSession).list();
						} else {
							conflicts = (List<Object[]>)query.select("distinct m.event.uniqueId, Xm").type("ClassEvent")
									.from("inner join e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d")
									.where("d.uniqueId = :resourceId")
									.set("resourceId", request.getResourceId())
									.query(hibSession).list();
							
							conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
									.from("inner join e.exam.owners o, CourseOffering co inner join co.subjectArea.department d")
									.where("d.uniqueId = :resourceId")
									.set("resourceId", request.getResourceId())
									.where("o.ownerType = :type and o.ownerId = co.uniqueId")
									.set("type", ExamOwner.sOwnerTypeCourse)
									.query(hibSession).list());
							conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
									.from("inner join e.exam.owners o, CourseOffering co inner join co.subjectArea.department d")
									.where("d.uniqueId = :resourceId")
									.set("resourceId", request.getResourceId())
									.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
									.set("type", ExamOwner.sOwnerTypeOffering)
									.query(hibSession).list());
							conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
									.from("inner join e.exam.owners o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d")
									.where("d.uniqueId = :resourceId")
									.set("resourceId", request.getResourceId())
									.where("o.ownerType = :type and o.ownerId = c.uniqueId")
									.set("type", ExamOwner.sOwnerTypeClass)
									.query(hibSession).list());
							conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("ExamEvent")
									.from("inner join e.exam.owners o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg inner join co.subjectArea.department d")
									.where("d.uniqueId = :resourceId")
									.set("resourceId", request.getResourceId())
									.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
									.set("type", ExamOwner.sOwnerTypeConfig)
									.query(hibSession).list());
							
							conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
									.from("inner join e.relatedCourses o, CourseOffering co inner join co.subjectArea.department d")
									.where("d.uniqueId = :resourceId")
									.set("resourceId", request.getResourceId())
									.where("o.ownerType = :type and o.ownerId = co.uniqueId")
									.set("type", ExamOwner.sOwnerTypeCourse)
									.query(hibSession).list());
							conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
									.from("inner join e.relatedCourses o, CourseOffering co inner join co.subjectArea.department d")
									.where("d.uniqueId = :resourceId")
									.set("resourceId", request.getResourceId())
									.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
									.set("type", ExamOwner.sOwnerTypeOffering)
									.query(hibSession).list());
							conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
									.from("inner join e.relatedCourses o, Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co inner join co.subjectArea.department d")
									.where("d.uniqueId = :resourceId")
									.set("resourceId", request.getResourceId())
									.where("o.ownerType = :type and o.ownerId = c.uniqueId")
									.set("type", ExamOwner.sOwnerTypeClass)
									.query(hibSession).list());
							conflicts.addAll(query.select("distinct m.event.uniqueId, Xm").type("CourseEvent")
									.from("inner join e.relatedCourses o, CourseOffering co inner join co.instructionalOffering.instrOfferingConfigs cfg inner join co.subjectArea.department d")
									.where("d.uniqueId = :resourceId")
									.set("resourceId", request.getResourceId())
									.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
									.set("type", ExamOwner.sOwnerTypeConfig)
									.query(hibSession).list());
						}
						break;
					}
					
					if (conflicts != null) {
						Hashtable<Long, EventInterface> conflictingEvents = new Hashtable<Long, EventInterface>();
						for (Object[] o: conflicts) {
							EventInterface parent = events.get((Long)o[0]);
							if (parent == null) continue;
							Meeting m = (Meeting)o[1];
							if (locationMap != null && m.getLocationPermanentId() != null) {
								Location location = locationMap.get(m.getLocationPermanentId());
								if (location != null) m.setLocation(location);
							}
							EventInterface event = conflictingEvents.get(m.getEvent().getUniqueId());
							if (event == null) {
								event = new EventInterface();
								event.setId(m.getEvent().getUniqueId());
								event.setName(m.getEvent().getEventName());
								event.setType(EventInterface.EventType.values()[m.getEvent().getEventType()]);
								conflictingEvents.put(m.getEvent().getUniqueId(), event);
								event.setCanView(context.hasPermission(m.getEvent(), Right.EventDetail));
								event.setMaxCapacity(m.getEvent().getMaxCapacity());
								if (m.getEvent().getMainContact() != null) {
									ContactInterface contact = new ContactInterface();
									contact.setFirstName(m.getEvent().getMainContact().getFirstName());
									contact.setMiddleName(m.getEvent().getMainContact().getMiddleName());
									contact.setLastName(m.getEvent().getMainContact().getLastName());
									contact.setAcademicTitle(m.getEvent().getMainContact().getAcademicTitle());
									contact.setEmail(m.getEvent().getMainContact().getEmailAddress());
									contact.setFormattedName(m.getEvent().getMainContact().getName(nameFormat));
									event.setContact(contact);
								}
								for (EventContact additional: m.getEvent().getAdditionalContacts()) {
									ContactInterface contact = new ContactInterface();
									contact.setFirstName(additional.getFirstName());
									contact.setMiddleName(additional.getMiddleName());
									contact.setLastName(additional.getLastName());
									contact.setAcademicTitle(additional.getAcademicTitle());
									contact.setEmail(additional.getEmailAddress());
									contact.setFormattedName(additional.getName(nameFormat));
									event.addAdditionalContact(contact);
								}
								event.setEmail(m.getEvent().getEmail());
								if (m.getEvent().getSponsoringOrganization() != null) {
									SponsoringOrganizationInterface sponsor = new SponsoringOrganizationInterface();
									sponsor.setEmail(m.getEvent().getSponsoringOrganization().getEmail());
									sponsor.setName(m.getEvent().getSponsoringOrganization().getName());
									sponsor.setUniqueId(m.getEvent().getSponsoringOrganization().getUniqueId());
									event.setSponsor(sponsor);
								}
								String note = null;
								for (EventNote n: m.getEvent().getNotes()) {
									if (n.getTextNote() != null && !n.getTextNote().isEmpty())
										note = (note == null ? "" : note + "n") + n.getTextNote();
								}
						    	if (Event.sEventTypeClass == m.getEvent().getEventType()) {
						    		ClassEvent ce = ClassEventDAO.getInstance().get(m.getEvent().getUniqueId(), hibSession);
						    		Class_ clazz = ce.getClazz();
									event.setEnrollment(clazz.getEnrollment());
									if (groupEnrollments) {
										int enrl = 0;
										for (StudentClassEnrollment e: clazz.getStudentEnrollments())
											if (group.getStudents().contains(e.getStudent())) enrl ++;
										event.setEnrollment(enrl);
									}
									event.setMaxCapacity(clazz.getClassLimit());
						    		if (clazz.getDisplayInstructor()) {
						    			for (ClassInstructor i: clazz.getClassInstructors()) {
											ContactInterface instructor = new ContactInterface();
											instructor.setFirstName(i.getInstructor().getFirstName());
											instructor.setMiddleName(i.getInstructor().getMiddleName());
											instructor.setLastName(i.getInstructor().getLastName());
											instructor.setAcademicTitle(i.getInstructor().getAcademicTitle());
											instructor.setEmail(i.getInstructor().getEmail());
											instructor.setFormattedName(i.getInstructor().getName(nameFormat));
											event.addInstructor(instructor);
						    			}
						    		}
						    		CourseOffering correctedOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
						    		List<CourseOffering> courses = new ArrayList<CourseOffering>(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings());
						    		/*
						    		switch (request.getResourceType()) {
						    		case SUBJECT:
					    				for (Iterator<CourseOffering> i = courses.iterator(); i.hasNext(); ) {
					    					CourseOffering co = i.next();
					    					if (co.getSubjectArea().getUniqueId().equals(request.getResourceId())) {
					    						if (!correctedOffering.getSubjectArea().getUniqueId().equals(request.getResourceId()))
					    							correctedOffering = co;
					    					} else {
					    						i.remove();
					    					}
					    				}
						    			break;
						    		case COURSE:
					    				for (Iterator<CourseOffering> i = courses.iterator(); i.hasNext(); ) {
					    					CourseOffering co = i.next();
					    					if (co.getUniqueId().equals(request.getResourceId())) {
					    						if (!correctedOffering.getUniqueId().equals(request.getResourceId()))
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
					    					if (co.getSubjectArea().getDepartment().getUniqueId().equals(request.getResourceId())) {
					    						if (!correctedOffering.getSubjectArea().getDepartment().getUniqueId().equals(request.getResourceId()))
					    							correctedOffering = co;
					    					} else {
					    						i.remove();
					    					}
					    				}
						    			break;
						    		case CURRICULUM:
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
						    		*/
						    		courses.remove(correctedOffering);
						    		event.addCourseName(correctedOffering.getCourseName());
						    		event.addCourseTitle(correctedOffering.getTitle() == null ? "" : correctedOffering.getTitle());
						    		event.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc().length() <= 20 ? clazz.getSchedulingSubpart().getItype().getDesc() : clazz.getSchedulingSubpart().getItype().getAbbv());
						    		if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() != null)
						    			event.setInstruction(event.getInstruction() + " (" + clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
						    		event.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
						    		event.setSectionNumber(clazz.getSectionNumberString(hibSession));
						    		if (clazz.getClassSuffix(correctedOffering) == null) {
							    		event.setName(clazz.getClassLabel(correctedOffering));
						    		} else {
							    		event.addExternalId(clazz.getClassSuffix(correctedOffering));
						    			event.setName(correctedOffering.getCourseName() + " " + clazz.getClassSuffix(correctedOffering));
						    		}
					    			for (CourseOffering co: courses) {
							    		event.addCourseName(co.getCourseName());
							    		event.addCourseTitle(co.getTitle() == null ? "" : co.getTitle());
							    		if (clazz.getClassSuffix(co) != null)
							    			event.addExternalId(clazz.getClassSuffix(co));
					    			}
						    		note = correctedOffering.getScheduleBookNote();
						    		if (clazz.getSchedulePrintNote() != null && !clazz.getSchedulePrintNote().isEmpty())
						    			note = (note == null || note.isEmpty() ? "" : note + "\n") + clazz.getSchedulePrintNote();
						    	} else if (Event.sEventTypeFinalExam == m.getEvent().getEventType() || Event.sEventTypeMidtermExam == m.getEvent().getEventType()) {
						    		ExamEvent xe = ExamEventDAO.getInstance().get(m.getEvent().getUniqueId(), hibSession);
						    		event.setEnrollment(xe.getExam().countStudents());
						    		event.setMaxCapacity(xe.getExam().getSize());
						    		if (groupEnrollments) {
										int enrl = 0;
										Set<Long> studentIds = xe.getExam().getStudentIds();
										for (Student s: group.getStudents())
											if (studentIds.contains(s.getUniqueId())) enrl ++;
										event.setEnrollment(enrl);
									}
					    			for (DepartmentalInstructor i: xe.getExam().getInstructors()) {
										ContactInterface instructor = new ContactInterface();
										instructor.setFirstName(i.getFirstName());
										instructor.setMiddleName(i.getMiddleName());
										instructor.setLastName(i.getLastName());
										instructor.setAcademicTitle(i.getAcademicTitle());
										instructor.setEmail(i.getEmail());
										instructor.setFormattedName(i.getName(nameFormat));
										event.addInstructor(instructor);
					    			}
					    			for (ExamOwner owner: new TreeSet<ExamOwner>(xe.getExam().getOwners())) {
					    				/* courses: */ 
					    				for(CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
					    					/*
								    		switch (request.getResourceType()) {
								    		case SUBJECT:
								    			if (!course.getSubjectArea().getUniqueId().equals(request.getResourceId())) continue courses;
								    			break;
								    		case COURSE:
								    			if (!course.getUniqueId().equals(request.getResourceId())) continue courses;
								    			break;
								    		case DEPARTMENT:
								    			if (department.isExternalManager()) break courses;
								    			if (!course.getSubjectArea().getDepartment().getUniqueId().equals(request.getResourceId())) continue courses;
								    			break;
								    		case CURRICULUM:
								    			if (!curriculumCourses.contains(course.getUniqueId())) continue courses;
								    			break;
								    		}
								    		*/
						    				String courseName = owner.getCourse().getCourseName();
						    				String label = owner.getLabel();
						    				if (label.startsWith(courseName)) {
						    					label = label.substring(courseName.length());
						    				}
						    				event.addCourseName(course.getCourseName());
						    				event.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
						    				event.addExternalId(label.trim());
					    				}
					    			}
					    			if (event.hasCourseNames() && event.getCourseNames().size() == 1 && (request.getResourceType() == ResourceType.PERSON || request.getResourceType() == ResourceType.CURRICULUM || request.getResourceType() == ResourceType.GROUP))
				    					event.setName((event.getCourseNames().get(0) + " " + event.getExternalIds().get(0)).trim());
						    	} else if (Event.sEventTypeCourse == m.getEvent().getEventType()) {
						    		CourseEvent ce = CourseEventDAO.getInstance().get(m.getEvent().getUniqueId(), hibSession);
						    		event.setRequiredAttendance(ce.isReqAttendance());
									int enrl = 0, cap = 0;
									for (RelatedCourseInfo owner: ce.getRelatedCourses()) {
										if (groupEnrollments) {
											Set<Long> studentIds = new HashSet<Long>(owner.getStudentIds());
											for (Student s: group.getStudents())
												if (studentIds.contains(s.getUniqueId())) enrl ++;
											event.setEnrollment(enrl);
										} else {
											enrl += owner.countStudents();
										}
										cap += owner.getLimit();
										/* courses: */
										for(CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
											/*
								    		switch (request.getResourceType()) {
								    		case SUBJECT:
								    			if (!course.getSubjectArea().getUniqueId().equals(request.getResourceId())) continue courses;
								    			break;
								    		case COURSE:
								    			if (!course.getUniqueId().equals(request.getResourceId())) continue courses;
								    			break;
								    		case DEPARTMENT:
								    			if (department.isExternalManager()) break courses;
								    			if (!course.getSubjectArea().getDepartment().getUniqueId().equals(request.getResourceId())) continue courses;
								    			break;
								    		case CURRICULUM:
								    			if (!curriculumCourses.contains(course.getUniqueId())) continue courses;
								    			break;
								    		}
								    		*/
						    				String courseName = owner.getCourse().getCourseName();
						    				String label = owner.getLabel();
						    				if (label.startsWith(courseName)) {
						    					label = label.substring(courseName.length());
						    				}
						    				event.addCourseName(course.getCourseName());
						    				event.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
						    				event.addExternalId(label.trim());
					    				}
									}
									event.setEnrollment(enrl);
									event.setMaxCapacity(cap);
						    	}
					    		if (note != null && !note.isEmpty()) {
					    			NoteInterface n = new NoteInterface();
					    			n.setNote(note);
					    			event.addNote(n);
					    		}
							}
							MeetingInterface meeting = new MeetingInterface();
							meeting.setId(m.getUniqueId());
							meeting.setMeetingDate(m.getMeetingDate());
							meeting.setDayOfWeek(Constants.getDayOfWeek(m.getMeetingDate()));
							meeting.setStartTime(m.getStartTime().getTime());
							meeting.setStopTime(m.getStopTime().getTime());
							meeting.setDayOfYear(CalendarUtils.date2dayOfYear(session.getSessionStartYear(), m.getMeetingDate()));
							meeting.setStartSlot(m.getStartPeriod());
							meeting.setEndSlot(m.getStopPeriod());
							meeting.setStartOffset(m.getStartOffset() == null ? 0 : m.getStartOffset());
							meeting.setEndOffset(m.getStopOffset() == null ? 0 : m.getStopOffset());
							meeting.setPast(context.isPastOrOutside(m.getStartTime()));
							meeting.setApprovalDate(m.getApprovalDate());
							meeting.setApprovalStatus(m.getApprovalStatus());
							if (m.getLocation() != null) {
								ResourceInterface location = new ResourceInterface();
								location.setType(ResourceType.ROOM);
								location.setId(m.getLocation().getUniqueId());
								location.setName(m.getLocation().getLabel());
								location.setSize(m.getLocation().getCapacity());
								location.setRoomType(m.getLocation().getRoomTypeLabel());
								location.setBreakTime(m.getLocation().getEffectiveBreakTime());
								location.setMessage(m.getLocation().getEventMessage());
								location.setIgnoreRoomCheck(m.getLocation().isIgnoreRoomCheck());
								meeting.setLocation(location);
							}
							event.addMeeting(meeting);	
							parent.addConflict(event);
						}
					}
					
					for (Map.Entry<Long, Set<Location>> entry: unavailableLocations.entrySet()) {
						EventInterface parent = events.get(entry.getKey());
						if (parent == null) continue;
						for (Location location: entry.getValue()) {
							EventInterface unavailability = generateUnavailabilityEvent(location, parent);
							if (unavailability != null)
								parent.addConflict(unavailability);
						}
					}
				}
				
				// Retrieve arrange hours classes
				if ((!request.getEventFilter().hasOptions("type") || request.getEventFilter().getOptions("type").contains("Class")) &&
					!request.getEventFilter().hasOptions("from") && !request.getEventFilter().hasOptions("to") && !request.getEventFilter().hasOptions("requested") &&
					!request.getEventFilter().hasOptions("day") && !request.getEventFilter().hasOptions("after") && !request.getEventFilter().hasOptions("before") &&
					(context.hasPermission(Right.HasRole) || session.canNoRoleReportClass()) && Solution.hasTimetable(session.getUniqueId())) {
					String datePatternFormat = ApplicationProperty.DatePatternFormatUseDates.value();
					List<Class_> arrageHourClasses = null; 
					switch (request.getResourceType()) {
					case SUBJECT:
					case COURSE:
						arrageHourClasses = hibSession.createQuery(
								"select c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where c.committedAssignment is null and c.cancelled = false and " +
								(request.getResourceType() == ResourceType.SUBJECT ? "co.subjectArea.uniqueId = :resourceId" : "co.uniqueId = :resourceId")).setLong("resourceId", request.getResourceId())
								.setCacheable(true).list();
						break;
					case DEPARTMENT:
						arrageHourClasses = hibSession.createQuery(
								"select c from Class_ c inner join c.managingDept d where c.committedAssignment is null and c.cancelled = false and d.uniqueId = :resourceId").setLong("resourceId", request.getResourceId())
								.setCacheable(true).list();
						break;
					case CURRICULUM:
						arrageHourClasses = hibSession.createQuery(
								"select c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc " +
								"where c.committedAssignment is null and c.cancelled = false and co = cc.course and (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId)")
								.setLong("resourceId", request.getResourceId())
								.setCacheable(true).list();
						break;
					case PERSON:
						Set<String> roles = request.getEventFilter().getOptions("role");
						boolean student = (roles == null || roles.contains("student") || roles.contains("Student"));
						boolean instructor = (roles == null || roles.contains("instructor") || roles.contains("Instructor"));
						boolean coordinator = (roles != null && (roles.contains("coordinator") || roles.contains("Coordinator")));
						arrageHourClasses = new ArrayList<Class_>();
						
						if (student)
							arrageHourClasses.addAll(hibSession.createQuery(
								"select c from StudentClassEnrollment e inner join e.clazz c where c.committedAssignment is null and c.cancelled = false and e.student.session.uniqueId = :sessionId and e.student.externalUniqueId = :externalId")
								.setString("externalId", request.getResourceExternalId()).setLong("sessionId", request.getSessionId())
								.setCacheable(true).list());
						
						if (instructor)
							arrageHourClasses.addAll(
								hibSession.createQuery("select c from ClassInstructor ci inner join ci.classInstructing c where c.committedAssignment is null and c.cancelled = false and ci.instructor.department.session.uniqueId = :sessionId and  ci.instructor.externalUniqueId = :externalId")
								.setString("externalId", request.getResourceExternalId()).setLong("sessionId", request.getSessionId())
								.setCacheable(true).list());

						if (coordinator)
							arrageHourClasses.addAll(
								hibSession.createQuery("select c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.offeringCoordinators cc where c.committedAssignment is null and c.cancelled = false and cc.instructor.department.session.uniqueId = :sessionId and cc.instructor.externalUniqueId = :externalId")
								.setString("externalId", request.getResourceExternalId()).setLong("sessionId", request.getSessionId())
								.setCacheable(true).list());
						break;
					case GROUP:
						Integer minEnrollment = null;
						String pMinEnrl = ApplicationProperty.StudentGroupsTimetableMinimalEnrollment.value();
						if (pMinEnrl != null) {
							if (pMinEnrl.endsWith("%"))
								minEnrollment = (int)Math.floor(Double.parseDouble(pMinEnrl.substring(0, pMinEnrl.length() - 1)) * group.getStudents().size() / 100.0);
							else
								minEnrollment = Integer.parseInt(pMinEnrl);
						}
						arrageHourClasses = new ArrayList<Class_>();
						if (minEnrollment == null)
							arrageHourClasses.addAll(hibSession.createQuery(
								"select distinct c from StudentGroup g inner join g.students s inner join s.classEnrollments e inner join e.clazz c where c.committedAssignment is null and c.cancelled = false and g.uniqueId = :resourceId")
								.setLong("resourceId", group.getUniqueId())
								.setCacheable(true).list());
						else
							arrageHourClasses.addAll(hibSession.createQuery(
									"select distinct c from StudentGroup g inner join g.students s inner join s.classEnrollments e inner join e.clazz c where c.committedAssignment is null and c.cancelled = false and g.uniqueId = :resourceId "+
									"and (select count(x) from StudentClassEnrollment x inner join x.student.groups y where y.uniqueId = g.uniqueId and x.clazz.uniqueId = e.clazz.uniqueId) >= :minEnrl")
									.setLong("resourceId", group.getUniqueId())
									.setInteger("minEnrl", minEnrollment)
									.setCacheable(true).list());
						break;
					}
					
					if (arrageHourClasses != null) {
						boolean checkDepartment = (ApplicationProperty.EventHasRoleCheckReportStatus.isTrue() && !context.hasPermission(Right.DepartmentIndependent) && !context.hasPermission(Right.StatusIndependent) && !session.canNoRoleReportClass());

						 for (Class_ clazz: arrageHourClasses) {
							 if (checkDepartment && !context.getUser().getCurrentAuthority().hasQualifier(clazz.getControllingDept())) continue;
							 EventInterface event = new EventInterface();
								event.setId(-clazz.getUniqueId());
								event.setName(clazz.getClassLabel(hibSession));
								event.setType(EventInterface.EventType.Class);
								event.setCanView(context.hasPermission(clazz, Right.EventDetailArrangeHourClass));
								event.setMaxCapacity(clazz.getClassLimit());
								event.setEnrollment(clazz.getEnrollment());
								if (groupEnrollments) {
									int enrl = 0;
									for (StudentClassEnrollment e: clazz.getStudentEnrollments())
										if (group.getStudents().contains(e.getStudent())) enrl ++;
									event.setEnrollment(enrl);
								}
								Set<Long> addedInstructorIds = new HashSet<Long>();
								if (clazz.getDisplayInstructor()) {
									for (ClassInstructor i: clazz.getClassInstructors()) {
										ContactInterface instructor = new ContactInterface();
										instructor.setFirstName(i.getInstructor().getFirstName());
										instructor.setMiddleName(i.getInstructor().getMiddleName());
										instructor.setLastName(i.getInstructor().getLastName());
										instructor.setAcademicTitle(i.getInstructor().getAcademicTitle());
										instructor.setEmail(i.getInstructor().getEmail());
										instructor.setFormattedName(i.getInstructor().getName(nameFormat));
										event.addInstructor(instructor);
										addedInstructorIds.add(i.getInstructor().getUniqueId());
					    			}
					    		}
								for (OfferingCoordinator oc: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getOfferingCoordinators()) {
									DepartmentalInstructor c = oc.getInstructor();
									if (addedInstructorIds.add(c.getUniqueId())) {
						    			ContactInterface coordinator = new ContactInterface();
										coordinator.setFirstName(c.getFirstName());
										coordinator.setMiddleName(c.getMiddleName());
										coordinator.setLastName(c.getLastName());
										coordinator.setAcademicTitle(c.getAcademicTitle());
										coordinator.setEmail(c.getEmail());
										coordinator.setFormattedName(c.getName(nameFormat));
										event.addCoordinator(coordinator);
									}
								}
					    		CourseOffering correctedOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
					    		List<CourseOffering> courses = new ArrayList<CourseOffering>(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings());
					    		switch (request.getResourceType()) {
					    		case CURRICULUM:
					    		case PERSON:
					    		case GROUP:
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
					    		event.addCourseTitle(correctedOffering.getTitle() == null ? "" : correctedOffering.getTitle());
					    		event.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc().length() <= 20 ? clazz.getSchedulingSubpart().getItype().getDesc() : clazz.getSchedulingSubpart().getItype().getAbbv());
					    		if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() != null)
					    			event.setInstruction(event.getInstruction() + " (" + clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
					    		event.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
					    		event.setSectionNumber(clazz.getSectionNumberString(hibSession));
					    		if (clazz.getClassSuffix(correctedOffering) == null) {
						    		event.setName(clazz.getClassLabel(correctedOffering));
					    		} else {
						    		event.addExternalId(clazz.getClassSuffix(correctedOffering));
					    			event.setName(correctedOffering.getCourseName() + " " + clazz.getClassSuffix(correctedOffering));
					    		}
				    			for (CourseOffering co: courses) {
						    		event.addCourseName(co.getCourseName());
						    		event.addCourseTitle(co.getTitle() == null ? "" : co.getTitle());
						    		if (clazz.getClassSuffix(co) != null)
						    			event.addExternalId(clazz.getClassSuffix(co));
				    			}
				    			DatePattern pattern = clazz.effectiveDatePattern();
				    			if (pattern != null) {
				    		    	if ("never".equals(datePatternFormat)) event.setMessage(pattern.getName());
				    		    	else if ("extended".equals(datePatternFormat) && pattern.getType() != DatePattern.sTypeExtended) event.setMessage(pattern.getName());
				    		    	else if ("alternate".equals(datePatternFormat) && pattern.getType() == DatePattern.sTypeAlternate) event.setMessage(pattern.getName());
				    		    	else {
				    		    		Date first = pattern.getStartDate();
				    		    		Date last = pattern.getEndDate();
				    		    		event.setMessage((first.equals(last) ? Formats.getDateFormat(Formats.Pattern.DATE_EVENT_LONG).format(first) : Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT).format(first) + " - " + Formats.getDateFormat(Formats.Pattern.DATE_EVENT_LONG).format(last)));
				    		    	}
				    			}
				    			for (RoomPref rp: (Set<RoomPref>)clazz.effectivePreferences(RoomPref.class)) {
				    				if (!PreferenceLevel.sRequired.equals(rp.getPrefLevel().getPrefProlog())) continue;
				    				if (request.getEventFilter().hasOptions("room") && !request.getEventFilter().getOptions("room").contains(rp.getRoom().getUniqueId().toString())) continue;
				    				MeetingInterface meeting = new MeetingInterface();
									meeting.setPast(true);
									ResourceInterface location = new ResourceInterface();
									location.setType(ResourceType.ROOM);
									location.setId(rp.getRoom().getUniqueId());
									location.setName(rp.getRoom().getLabel());
									location.setSize(rp.getRoom().getCapacity());
									location.setDistance(distances.get(rp.getRoom().getUniqueId()));
									location.setRoomType(rp.getRoom().getRoomTypeLabel());
									location.setBreakTime(rp.getRoom().getEffectiveBreakTime());
									location.setMessage(rp.getRoom().getEventMessage());
									location.setIgnoreRoomCheck(rp.getRoom().isIgnoreRoomCheck());
									meeting.setLocation(location);
									event.addMeeting(meeting);
				    			}
				    			if (!event.hasMeetings()) {
				    				if (request.getEventFilter().hasOptions("room")) continue;
				    				MeetingInterface meeting = new MeetingInterface();
									meeting.setPast(true);
									event.addMeeting(meeting);
				    			}
				    			if (correctedOffering.getScheduleBookNote() != null && !correctedOffering.getScheduleBookNote().isEmpty()) {
				    				NoteInterface note = new NoteInterface();
				    				note.setId(-2l);
				    				note.setNote(correctedOffering.getScheduleBookNote());
				    				event.addNote(note);
				    			}
					    		if (clazz.getSchedulePrintNote() != null && !clazz.getSchedulePrintNote().isEmpty()) {
					    			NoteInterface note = new NoteInterface();
				    				note.setId(-1l);
				    				note.setNote(clazz.getSchedulePrintNote());
				    				event.addNote(note);
					    		}
				    			if (request.getEventFilter().hasText() && !event.getName().toLowerCase().startsWith(request.getEventFilter().getText().toLowerCase()) &&
				    				(request.getEventFilter().getText().length() < 2 || !event.getName().toLowerCase().contains(" " + request.getEventFilter().getText().toLowerCase()))) continue;
								ret.add(event);
						 }
					}					
				}
				
				// Retrieve room unavailabilities
				if (request.getResourceType() == ResourceType.ROOM) {
					for (Location location: new RoomFilterBackend().locations(request.getSessionId(), request.getRoomFilter(), 1000, distances, context)) {
						EventInterface unavailability = generateUnavailabilityEvent(location, null);
						if (unavailability != null)
							ret.add(unavailability);
					}
				}
				
				return ret;
			} finally {
				hibSession.close();
			}
		} catch (GwtRpcException e) {
			throw e;
		} catch (Exception e) {
			sLog.error("Unable to find events for " + request.getResourceType().getLabel() + " " + request.getResourceId() + ": " + e.getMessage(), e);
			throw new GwtRpcException("Unable to find events for " + request.getResourceType().getLabel() + " " + request.getResourceId() + ": " + e.getMessage(), e);
		}
	}
	
	public static EventInterface generateUnavailabilityEvent(Location location, EventInterface parent) {
		TreeSet<MeetingInterface> meetings = generateUnavailabilityMeetings(location, false);
		if (meetings == null || meetings.isEmpty()) return null;
		EventInterface event = new EventInterface();
		event.setId(-location.getUniqueId());
		event.setName(MESSAGES.unavailableEventDefaultName());
		event.setType(EventInterface.EventType.Unavailabile);
		if (parent == null)
			event.setMeetings(meetings);
		else
			for (MeetingInterface m: meetings)
				if (parent.inConflict(m))
					event.addMeeting(m);
		if (location.getNote() != null && !location.getNote().isEmpty()) {
			NoteInterface n = new NoteInterface();
			n.setNote(location.getNote());
			event.addNote(n);
		}
		return event;
	}
	
	public static TreeSet<MeetingInterface> generateUnavailabilityMeetings(Location location, boolean conflict) {
		if (location.getEventAvailability() == null || location.getEventAvailability().length() != Constants.SLOTS_PER_DAY * Constants.DAY_CODES.length) return null;

		TreeSet<MeetingInterface> ret = new TreeSet<MeetingInterface>();
		
		ResourceInterface resource = new ResourceInterface();
		resource.setType(ResourceType.ROOM);
		resource.setId(location.getUniqueId());
		resource.setName(location.getLabel());
		resource.setSize(location.getCapacity());
		resource.setRoomType(location.getRoomTypeLabel());
		resource.setBreakTime(location.getEffectiveBreakTime());
		resource.setMessage(location.getEventMessage());
		resource.setIgnoreRoomCheck(location.isIgnoreRoomCheck());
		
		Calendar calendar = Calendar.getInstance();
        for (int day = 0; day < Constants.DAY_CODES.length; day++)
        	for (int startTime = 0; startTime < Constants.SLOTS_PER_DAY; ) {
        		if (location.getEventAvailability().charAt(day * Constants.SLOTS_PER_DAY + startTime) != '1') { startTime++; continue; }
        		int endTime = startTime + 1;
        		while (endTime < Constants.SLOTS_PER_DAY && location.getEventAvailability().charAt(day * Constants.SLOTS_PER_DAY + endTime) == '1') endTime++;
        		
        		calendar.setTime(location.getSession().getEventBeginDate());
        		int dayOfYear = CalendarUtils.date2dayOfYear(location.getSession().getSessionStartYear(), calendar.getTime());
        		do {
        			int dayOfWeek = -1;
        			switch (calendar.get(Calendar.DAY_OF_WEEK)) {
        			case Calendar.MONDAY: dayOfWeek = Constants.DAY_MON; break;
        			case Calendar.TUESDAY: dayOfWeek = Constants.DAY_TUE; break;
        			case Calendar.WEDNESDAY: dayOfWeek = Constants.DAY_WED; break;
        			case Calendar.THURSDAY: dayOfWeek = Constants.DAY_THU; break;
        			case Calendar.FRIDAY: dayOfWeek = Constants.DAY_FRI; break;
        			case Calendar.SATURDAY: dayOfWeek = Constants.DAY_SAT; break;
        			case Calendar.SUNDAY: dayOfWeek = Constants.DAY_SUN; break;
        			}
        			
        			if (day == dayOfWeek) {
        				MeetingInterface m = null;
        				if (conflict) {
        					MeetingConflictInterface c = new MeetingConflictInterface();
        					c.setName(MESSAGES.unavailableEventDefaultName());
        					c.setType(EventInterface.EventType.Unavailabile);
        					m = c;
        				} else {
        					m = new MeetingInterface();
        				}
                		m.setStartSlot(startTime);
                		m.setEndSlot(endTime);
                		m.setDayOfWeek(dayOfWeek);
                		m.setMeetingDate(calendar.getTime());
                		m.setDayOfYear(dayOfYear);
                		m.setLocation(resource);
                		ret.add(m);
        			}
        			calendar.add(Calendar.DAY_OF_YEAR, 1); dayOfYear++;
        		} while (!calendar.getTime().after(location.getSession().getEventEndDate()));
        		startTime = endTime;
        	}
		return ret;
	}

}