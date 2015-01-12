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
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.unitime.timetable.defaults.UserProperty;
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
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType.Status;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;

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
			else if (!request.getResourceExternalId().equals(context.isAuthenticated() ? context.getUser().getExternalUserId() : null))
				context.checkPermission(Right.EventLookupSchedule);
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
					for (Location location: new RoomFilterBackend().locations(request.getSessionId(), request.getRoomFilter(), 1000, distances)) {
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
				Department department = null;
				switch (request.getResourceType()) {
				case ROOM:
					if (request.getResourceId() == null)
						meetings = (List<Meeting>)query.select("distinct m").limit(1 + limit).query(hibSession).list();
					else
						meetings = (List<Meeting>)query.select("distinct m")
							.joinWithLocation()
							.where("l.uniqueId = :resourceId")
							.set("resourceId", request.getResourceId())
							.limit(1 + limit)
							.query(hibSession).list();
					
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
					boolean overrideStatus = context.hasPermission(Right.EventLookupSchedule); 
					boolean canViewFinalExams = overrideStatus || session.getStatusType().canNoRoleReportExamFinal();
					boolean canViewMidtermExams = overrideStatus || session.getStatusType().canNoRoleReportExamMidterm();
					boolean canViewClasses = overrideStatus || session.getStatusType().canNoRoleReportClass();
					boolean allSessions = request.getEventFilter().hasOption("flag") && request.getEventFilter().getOptions("flag").contains("All Sessions");
					curriculumCourses = new HashSet<Long>();
					if (allSessions) {
						curriculumCourses.addAll(hibSession.createQuery("select e.courseOffering.uniqueId from StudentClassEnrollment e where e.student.externalUniqueId = :externalId")
								.setString("externalId", request.getResourceExternalId()).list());
						curriculumCourses
								.addAll(hibSession.createQuery("select o.course.uniqueId from Exam x inner join x.owners o inner join x.instructors i where i.externalUniqueId = :externalId")
										.setString("externalId", request.getResourceExternalId()).list());
					} else {
						curriculumCourses
								.addAll(hibSession.createQuery("select e.courseOffering.uniqueId from StudentClassEnrollment e where e.student.session.uniqueId = :sessionId and e.student.externalUniqueId = :externalId")
										.setLong("sessionId", request.getSessionId()).setString("externalId", request.getResourceExternalId()).list());
						curriculumCourses
								.addAll(hibSession.createQuery("select o.course.uniqueId from Exam x inner join x.owners o inner join x.instructors i where x.session.uniqueId = :sessionId and i.externalUniqueId = :externalId")
										.setLong("sessionId", request.getSessionId())
										.setString("externalId", request.getResourceExternalId()).list());
					}
					meetings = new ArrayList<Meeting>();

					if (allSessions && !overrideStatus) {
						if (overrideStatus) {
							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.studentEnrollments enrl")
										.where("enrl.student.externalUniqueId = :externalId").where("enrl.student.session.uniqueId = s.uniqueId")
										.set("externalId", request.getResourceExternalId())
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.classInstructors ci")
										.where("ci.instructor.externalUniqueId = :externalId").where("ci.instructor.department.session.uniqueId = s.uniqueId")
										.set("externalId", request.getResourceExternalId()).limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						} else {
							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.studentEnrollments enrl")
										.where("enrl.student.externalUniqueId = :externalId").where("enrl.student.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportClasses.toInt())
										.set("externalId", request.getResourceExternalId()).limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.classInstructors ci")
										.where("ci.instructor.externalUniqueId = :externalId").where("ci.instructor.department.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportClasses.toInt())
										.set("externalId", request.getResourceExternalId()).limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						}
					} else if (canViewClasses) {
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.studentEnrollments enrl")
									.where("enrl.student.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("enrl.student.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("ClassEvent").from("inner join e.clazz.classInstructors ci")
									.where("ci.instructor.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("ci.instructor.department.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}

					if (allSessions) {
						if (overrideStatus) {
							if (limit <= 0 || meetings.size() < limit)
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
							if (limit <= 0 || meetings.size() < limit)
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
							if (limit <= 0 || meetings.size() < limit)
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
							if (limit <= 0 || meetings.size() < limit)
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

							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query
										.select("distinct m")
										.type("ExamEvent")
										.from("inner join e.exam.instructors i")
										.where("i.externalUniqueId = :externalId")
										.set("externalId", request.getResourceExternalId())
										.where("i.department.session.uniqueId = s.uniqueId")
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						} else {
							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query
										.select("distinct m")
										.type("FinalExamEvent")
										.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
										.where("enrl.student.externalUniqueId = :externalId")
										.set("externalId", request.getResourceExternalId())
										.where("o.ownerType = :type and o.ownerId = co.uniqueId")
										.set("type", ExamOwner.sOwnerTypeCourse)
										.where("enrl.student.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportExamsFinal.toInt())
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query
										.select("distinct m")
										.type("MidtermExamEvent")
										.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
										.where("enrl.student.externalUniqueId = :externalId")
										.set("externalId", request.getResourceExternalId())
										.where("o.ownerType = :type and o.ownerId = co.uniqueId")
										.set("type", ExamOwner.sOwnerTypeCourse)
										.where("enrl.student.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportExamsMidterm.toInt())
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());

							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query
										.select("distinct m")
										.type("FinalExamEvent")
										.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
										.where("enrl.student.externalUniqueId = :externalId")
										.set("externalId", request.getResourceExternalId())
										.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
										.set("type", ExamOwner.sOwnerTypeOffering)
										.where("enrl.student.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportExamsFinal.toInt())
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query
										.select("distinct m")
										.type("MidtermExamEvent")
										.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
										.where("enrl.student.externalUniqueId = :externalId")
										.set("externalId", request.getResourceExternalId())
										.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
										.set("type", ExamOwner.sOwnerTypeOffering)
										.where("enrl.student.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportExamsMidterm.toInt())
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());

							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query
										.select("distinct m")
										.type("FinalExamEvent")
										.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
										.where("enrl.student.externalUniqueId = :externalId")
										.set("externalId", request.getResourceExternalId())
										.where("o.ownerType = :type and o.ownerId = c.uniqueId")
										.set("type", ExamOwner.sOwnerTypeClass)
										.where("enrl.student.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportExamsFinal.toInt())
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query
										.select("distinct m")
										.type("MidtermExamEvent")
										.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
										.where("enrl.student.externalUniqueId = :externalId")
										.set("externalId", request.getResourceExternalId())
										.where("o.ownerType = :type and o.ownerId = c.uniqueId")
										.set("type", ExamOwner.sOwnerTypeClass)
										.where("enrl.student.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportExamsMidterm.toInt())
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
							
							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query
										.select("distinct m")
										.type("FinalExamEvent")
										.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig cfg")
										.where("enrl.student.externalUniqueId = :externalId")
										.set("externalId", request.getResourceExternalId())
										.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
										.set("type", ExamOwner.sOwnerTypeConfig)
										.where("enrl.student.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportExamsFinal.toInt())
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query
										.select("distinct m")
										.type("MidtermExamEvent")
										.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig cfg")
										.where("enrl.student.externalUniqueId = :externalId")
										.set("externalId", request.getResourceExternalId())
										.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
										.set("type", ExamOwner.sOwnerTypeConfig)
										.where("enrl.student.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportExamsMidterm.toInt())
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());

							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query
										.select("distinct m")
										.type("FinalExamEvent")
										.from("inner join e.exam.instructors i")
										.where("i.externalUniqueId = :externalId")
										.set("externalId", request.getResourceExternalId())
										.where("i.department.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportExamsFinal.toInt())
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
							if (limit <= 0 || meetings.size() < limit)
								meetings.addAll(query
										.select("distinct m")
										.type("MidtermExamEvent")
										.from("inner join e.exam.instructors i")
										.where("i.externalUniqueId = :externalId")
										.set("externalId", request.getResourceExternalId())
										.where("i.department.session.uniqueId = s.uniqueId")
										.where("bit_and(s.statusType.status, :flag) > 0").set("flag", Status.ReportExamsMidterm.toInt())
										.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						}
					} else if (canViewFinalExams || canViewMidtermExams) {
						String table = (canViewFinalExams ? canViewMidtermExams ? "ExamEvent" : "FinalExamEvent" : "MidtermExamEvent");
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type(table)
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
									.where("enrl.student.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.uniqueId").set("type", ExamOwner.sOwnerTypeCourse)
									.where("e.exam.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type(table)
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
									.where("enrl.student.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId").set("type", ExamOwner.sOwnerTypeOffering)
									.where("e.exam.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query
									.select("distinct m")
									.type(table)
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
									.where("enrl.student.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = c.uniqueId").set("type", ExamOwner.sOwnerTypeClass)
									.where("e.exam.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query
									.select("distinct m")
									.type(table)
									.from("inner join e.exam.owners o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig cfg")
									.where("enrl.student.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("o.ownerType = :type and o.ownerId = cfg.uniqueId").set("type", ExamOwner.sOwnerTypeConfig)
									.where("e.exam.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());

						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type(table).from("inner join e.exam.instructors i")
									.where("i.externalUniqueId = :externalId").set("externalId", request.getResourceExternalId())
									.where("e.exam.session = s")
									.limit(limit <= 0 ? -1 : 1 + limit - meetings.size()).query(hibSession).list());
					}
					
					if (allSessions) {
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.set("type", ExamOwner.sOwnerTypeClass)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig cfg")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci inner join ci.classInstructing c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci inner join ci.classInstructing c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci inner join ci.classInstructing c inner join c.schedulingSubpart.instrOfferingConfig cfg")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci inner join ci.classInstructing c")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.set("type", ExamOwner.sOwnerTypeClass)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());						
					} else {
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.where("enrl.student.session = s")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.courseOffering co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.where("enrl.student.session = s")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.where("enrl.student.session = s")
								.set("type", ExamOwner.sOwnerTypeClass)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, StudentClassEnrollment enrl inner join enrl.clazz c inner join c.schedulingSubpart.instrOfferingConfig cfg")
								.where("enrl.student.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.where("enrl.student.session = s")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci inner join ci.classInstructing c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.uniqueId")
								.where("ci.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeCourse)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci inner join ci.classInstructing c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = co.instructionalOffering.uniqueId")
								.where("ci.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeOffering)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci inner join ci.classInstructing c inner join c.schedulingSubpart.instrOfferingConfig cfg")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = cfg.uniqueId")
								.where("ci.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeConfig)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
						if (limit <= 0 || meetings.size() < limit)
							meetings.addAll(query.select("distinct m").type("CourseEvent")
								.from("inner join e.relatedCourses o, ClassInstructor ci inner join ci.classInstructing c")
								.where("ci.instructor.externalUniqueId = :externalId")
								.set("externalId", request.getResourceExternalId())
								.where("o.ownerType = :type and o.ownerId = c.uniqueId")
								.where("ci.instructor.department.session = s")
								.set("type", ExamOwner.sOwnerTypeClass)
								.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
								.query(hibSession).list());
					}



					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m")
                    		.where("e.class in (CourseEvent, SpecialEvent, UnavailableEvent)")
                    		.where("e.mainContact.externalUniqueId = :externalId")
                    		.set("externalId", request.getResourceExternalId())
                    		.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
                    		.query(hibSession).list());
                    
					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m")
                    		.from("inner join m.event.additionalContacts c")
                    		.where("c.externalUniqueId = :externalId")
                    		.set("externalId", request.getResourceExternalId())
                    		.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
                    		.query(hibSession).list());

					if (limit <= 0 || meetings.size() < limit)
						meetings.addAll(query.select("distinct m")
                    		.from("EventContact c")
                    		.where("c.externalUniqueId = :externalId")
                    		.where("c.emailAddress is not null")
                    		.where("lower(m.event.email) like '%' || lower(c.emailAddress) || '%'")
                    		.set("externalId", request.getResourceExternalId())
                    		.limit(limit <= 0 ? -1 : 1 + limit - meetings.size())
                    		.query(hibSession).list());
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
			    			String name = null;
			    			for (ExamOwner owner: new TreeSet<ExamOwner>(xe.getExam().getOwners())) {
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
						    			if (!curriculumCourses.contains(course.getUniqueId())) continue courses;
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
			    			if (event.hasCourseNames() && event.getCourseNames().size() == 1 && (request.getResourceType() == ResourceType.PERSON || request.getResourceType() == ResourceType.CURRICULUM))
		    					event.setName(name);
				    	} else if (Event.sEventTypeCourse == m.getEvent().getEventType()) {
				    		CourseEvent ce = CourseEventDAO.getInstance().get(m.getEvent().getUniqueId(), hibSession);
				    		event.setRequiredAttendance(ce.isReqAttendance());
							int enrl = 0;
							int cap = 0;
							for (RelatedCourseInfo owner: ce.getRelatedCourses()) {
								enrl += owner.countStudents();
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
						    			if (!curriculumCourses.contains(course.getUniqueId())) continue courses;
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
						if (m.getLocation().getEventAvailability() != null && m.getLocation().getEventAvailability().length() == Constants.SLOTS_PER_DAY * Constants.DAY_CODES.length) {
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
					    			if (event.hasCourseNames() && event.getCourseNames().size() == 1 && (request.getResourceType() == ResourceType.PERSON || request.getResourceType() == ResourceType.CURRICULUM))
				    					event.setName((event.getCourseNames().get(0) + " " + event.getExternalIds().get(0)).trim());
						    	} else if (Event.sEventTypeCourse == m.getEvent().getEventType()) {
						    		CourseEvent ce = CourseEventDAO.getInstance().get(m.getEvent().getUniqueId(), hibSession);
						    		event.setRequiredAttendance(ce.isReqAttendance());
									int enrl = 0, cap = 0;
									for (RelatedCourseInfo owner: ce.getRelatedCourses()) {
										enrl += owner.countStudents();
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
					(context.hasPermission(Right.HasRole) || session.getStatusType().canNoRoleReportClass()) && Solution.hasTimetable(session.getUniqueId())) {
					List<Class_> arrageHourClasses = null; 
					switch (request.getResourceType()) {
					case SUBJECT:
					case COURSE:
						arrageHourClasses = hibSession.createQuery(
								"select c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where c.committedAssignment is null and " +
								(request.getResourceType() == ResourceType.SUBJECT ? "co.subjectArea.uniqueId = :resourceId" : "co.uniqueId = :resourceId")).setLong("resourceId", request.getResourceId())
								.setCacheable(true).list();
						break;
					case DEPARTMENT:
						arrageHourClasses = hibSession.createQuery(
								"select c from Class_ c inner join c.managingDept d where c.committedAssignment is null and d.uniqueId = :resourceId").setLong("resourceId", request.getResourceId())
								.setCacheable(true).list();
						break;
					case CURRICULUM:
						arrageHourClasses = hibSession.createQuery(
								"select c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co, CurriculumCourse cc " +
								"where c.committedAssignment is null and co = cc.course and (cc.classification.curriculum.uniqueId = :resourceId or cc.classification.uniqueId = :resourceId)")
								.setLong("resourceId", request.getResourceId())
								.setCacheable(true).list();
						break;
					case PERSON:
						arrageHourClasses = hibSession.createQuery(
								"select c from StudentClassEnrollment e inner join e.clazz c where c.committedAssignment is null and e.student.session.uniqueId = :sessionId and e.student.externalUniqueId = :externalId")
								.setString("externalId", request.getResourceExternalId()).setLong("sessionId", request.getSessionId())
								.setCacheable(true).list();
						
						arrageHourClasses.addAll(
								hibSession.createQuery("select c from ClassInstructor ci inner join ci.classInstructing c where c.committedAssignment is null and ci.instructor.department.session.uniqueId = :sessionId and  ci.instructor.externalUniqueId = :externalId")
								.setString("externalId", request.getResourceExternalId()).setLong("sessionId", request.getSessionId())
								.setCacheable(true).list());

					}
					
					if (arrageHourClasses != null) {
						 for (Class_ clazz: arrageHourClasses) {
							 
							 EventInterface event = new EventInterface();
								event.setId(-clazz.getUniqueId());
								event.setName(clazz.getClassLabel(hibSession));
								event.setType(EventInterface.EventType.Class);
								event.setCanView(false);
								event.setMaxCapacity(clazz.getClassLimit());
								event.setEnrollment(clazz.getEnrollment());
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
					    		switch (request.getResourceType()) {
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
					    		event.addCourseTitle(correctedOffering.getTitle() == null ? "" : correctedOffering.getTitle());
					    		event.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc().length() <= 20 ? clazz.getSchedulingSubpart().getItype().getDesc() : clazz.getSchedulingSubpart().getItype().getAbbv());
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
				    			for (RoomPref rp: (Set<RoomPref>)clazz.effectivePreferences(RoomPref.class)) {
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
					for (Location location: new RoomFilterBackend().locations(request.getSessionId(), request.getRoomFilter(), 1000, distances)) {
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