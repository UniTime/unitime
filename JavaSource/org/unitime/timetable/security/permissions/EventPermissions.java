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
package org.unitime.timetable.security.permissions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.UserQualifier;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class EventPermissions {
	
	@PermissionForRight(Right.PersonalSchedule)
	public static class PersonalSchedule implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return
				(permissionSession.check(user, source, DepartmentStatusType.Status.ReportClasses) && Solution.hasTimetable(source.getSessionId())) ||
				(permissionSession.check(user, source, DepartmentStatusType.Status.ReportExamsFinal) && Exam.hasTimetable(source.getUniqueId(), ExamType.sExamTypeFinal)) ||
				(permissionSession.check(user, source, DepartmentStatusType.Status.ReportExamsMidterm) && Exam.hasTimetable(source.getUniqueId(), ExamType.sExamTypeMidterm));
		}

		@Override
		public Class<Session> type() { return Session.class; }
		
	}
	
	@PermissionForRight(Right.PersonalScheduleLookup)
	public static class PersonalScheduleLookup extends PersonalSchedule {}
	
	protected static abstract class EventPermission<T> implements Permission<T> {
		@Autowired PermissionSession permissionSession;
		
		protected Date today() {
			Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime();
		}
		
		protected Date begin(Session session) {
			return session.getEventBeginDate();
		}
		
		protected Date end(Session session) {
			Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
			cal.setTime(session.getEventEndDate());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			return cal.getTime();
		}
		
		protected boolean isOutside(Date date, Session session) {
			return date == null || date.before(begin(session)) || !date.before(end(session));
		}
		
		protected boolean isPast(Date date) {
			return date == null || date.before(today());
		}
		
		protected List<Long> locations(Long sessionId, UserContext user) {
			if (sessionId == null) return new ArrayList<Long>();
			return locations(SessionDAO.getInstance().get(sessionId), user);
		}
		
		protected List<Long> locations(Session session, UserContext user) {
			if (session == null || !permissionSession.check(user, session, DepartmentStatusType.Status.EventManagement))
				return new ArrayList<Long>();
			
			String anyRequest = "";
			String deptRequest = "";
			String mgrRequest = "";
			for (RoomTypeOption.Status state: RoomTypeOption.Status.values()) {
				if (state.isAuthenticatedUsersCanRequestEvents())
					anyRequest += (anyRequest.isEmpty() ? "" : ", ") + state.ordinal();
				else {
					if (state.isDepartmentalUsersCanRequestEvents())
						deptRequest += (deptRequest.isEmpty() ? "" : ", ") + state.ordinal();
					if (state.isEventManagersCanRequestEvents())
						mgrRequest += (mgrRequest.isEmpty() ? "" : ", ") + state.ordinal();
				}
			}
			Set<Serializable> roleDeptIds = new HashSet<Serializable>(), mgrDeptIds = new HashSet<Serializable>();
			if (!user.getCurrentAuthority().hasRight(Right.DepartmentIndependent)) {
				for (UserAuthority a: user.getAuthorities()) {
					if (!session.getUniqueId().equals(a.getAcademicSession().getQualifierId())) continue;
					for (UserQualifier q: a.getQualifiers("Department")) {
						roleDeptIds.add(q.getQualifierId());
						if (a.hasRight(Right.EventMeetingApprove))
							mgrDeptIds.add(q.getQualifierId());
					}
				}
			}
			String roleDept = null, mgrDept = null;
			for (Serializable id: roleDeptIds)
				roleDept = (roleDept == null ? "" : roleDept + ",") + id;
			for (Serializable id: mgrDeptIds)
				mgrDept = (mgrDept == null ? "" : mgrDept + ",") + id;
			
			return (List<Long>) SessionDAO.getInstance().getSession().createQuery(
					"select distinct l.uniqueId " +
					"from Location l, RoomTypeOption o " +
					"where l.eventDepartment.allowEvents = true and l.session.uniqueId = :sessionId and (" +
					"(l.eventStatus in (" + anyRequest + ") or (l.eventStatus is null and o.status in (" + anyRequest + ") and o.roomType = l.roomType and o.department = l.eventDepartment))" +
					(user.getCurrentAuthority().hasRight(Right.DepartmentIndependent)
							? " or (l.eventStatus in (" + deptRequest + ") or (l.eventStatus is null and o.status in (" + deptRequest + ") and o.roomType = l.roomType and o.department = l.eventDepartment))"
							: roleDept == null ? ""
							: " or ((l.eventStatus in (" + deptRequest + ") or (l.eventStatus is null and o.status in (" + deptRequest + ") and o.roomType = l.roomType and o.department = l.eventDepartment)) and l.eventDepartment.uniqueId in (" + roleDept + "))"
					) +
					(user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) && user.getCurrentAuthority().hasRight(Right.EventMeetingApprove)
							? " or (l.eventStatus in (" + mgrRequest + ") or (l.eventStatus is null and o.status in (" + mgrRequest + ") and o.roomType = l.roomType and o.department = l.eventDepartment))"
							: mgrDept == null ? ""
							: " or ((l.eventStatus in (" + mgrRequest + ") or (l.eventStatus is null and o.status in (" + mgrRequest + ") and o.roomType = l.roomType and o.department = l.eventDepartment)) and l.eventDepartment.uniqueId in (" + mgrDept + "))"
					) +
					")")
					.setLong("sessionId", session.getUniqueId()).setCacheable(true).list();
		}
	}
	
	@PermissionForRight(Right.Events)
	public static class Events extends EventPermission<Session> {
		@Override
		public boolean check(UserContext user, Session source) {
			return source.getStatusType().canNoRoleReport() || (user.getCurrentAuthority().hasRight(Right.EventAnyLocation) || !locations(source, user).isEmpty());
		}
		
		@Override
		public Class<Session> type() { return Session.class; }
	}

	@PermissionForRight(Right.EventAddSpecial)
	public static class EventAddSpecial extends EventPermission<Session> {
		@Override
		public boolean check(UserContext user, Session source) {
			return (!isPast(end(source)) || user.getCurrentAuthority().hasRight(Right.EventEditPast)) &&
					(user.getCurrentAuthority().hasRight(Right.EventAnyLocation) || !locations(source, user).isEmpty());
		}
		
		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.EventAddCourseRelated)
	public static class EventAddCourseRelated extends EventAddSpecial { }
	
	@PermissionForRight(Right.EventAddUnavailable)
	public static class EventAddUnavailable extends EventPermission<Session> {
		@Autowired Permission<Session> permissionEventAddSpecial;
		
		@Override
		public boolean check(UserContext user, Session source) {
			return user.getCurrentAuthority().hasRight(Right.EventLocationUnavailable) && permissionEventAddSpecial.check(user, source);
		}
		
		@Override
		public Class<Session> type() { return Session.class; }
	}

	@PermissionForRight(Right.EventDetail)
	public static class EventDetail implements Permission<Event> {
		@Autowired Permission<Class_> permissionClassDetail;
		@Autowired Permission<Exam> permissionExaminationDetail;
		
		@Override
		public boolean check(UserContext user, Event source) {
			// Owner can always see
			if (source.getMainContact() != null && user.getExternalUserId().equals(source.getMainContact().getExternalUniqueId())) return true;
			
			// Additional contacts can also see
			for (EventContact contact: source.getAdditionalContacts())
				if (user.getExternalUserId().equals(contact.getExternalUniqueId())) return true;
			
			switch (source.getEventType()) {
			case Event.sEventTypeClass:
				// Class event -- can see Class Detail page?
				if (user.getCurrentAuthority().hasRight(Right.ClassDetail)) {
					Class_ clazz = (source instanceof ClassEvent ? (ClassEvent)source : ClassEventDAO.getInstance().get(source.getUniqueId())).getClazz();
					if (permissionClassDetail.check(user, clazz)) return true;
				}
				// Instructors and course coordinators can also see details
				if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
					Class_ clazz = (source instanceof ClassEvent ? (ClassEvent)source : ClassEventDAO.getInstance().get(source.getUniqueId())).getClazz();
					if (clazz == null) return false;
					for (OfferingCoordinator coordinator: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getOfferingCoordinators()) {
						if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
					}
					for (ClassInstructor instructor: clazz.getClassInstructors()) {
						if (user.getExternalUserId().equals(instructor.getInstructor().getExternalUniqueId())) return true;
					}
				}
				return false;
			case Event.sEventTypeFinalExam:
			case Event.sEventTypeMidtermExam:
				// Examination event -- can see ExaminationDetail page?
				if (user.getCurrentAuthority().hasRight(Right.ExaminationDetail)) {
					Exam exam = (source instanceof ExamEvent ? (ExamEvent)source : ExamEventDAO.getInstance().get(source.getUniqueId())).getExam();
					if (permissionExaminationDetail.check(user, exam)) return true;
				}
				// Instructors and course coordinators can also see details
				if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
					Exam exam = (source instanceof ExamEvent ? (ExamEvent)source : ExamEventDAO.getInstance().get(source.getUniqueId())).getExam();
					if (exam == null) return false;
					for (DepartmentalInstructor instructor: exam.getInstructors()) {
						if (user.getExternalUserId().equals(instructor.getExternalUniqueId())) return true;
					}
					for (ExamOwner owner: exam.getOwners()) {
						for (OfferingCoordinator coordinator: owner.getCourse().getInstructionalOffering().getOfferingCoordinators())
							if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
					}
				}
				return false;
			case Event.sEventTypeCourse:
				// Course coordinators can also see details
				if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
					CourseEvent event = CourseEventDAO.getInstance().get(source.getUniqueId());
					for (RelatedCourseInfo owner: event.getRelatedCourses()) {
						for (OfferingCoordinator coordinator: owner.getCourse().getInstructionalOffering().getOfferingCoordinators())
							if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
					}
				}
				// Also event managers can see
				return user.getCurrentAuthority().hasRight(Right.EventLookupContact);
			default:
				// Event managers can see other events
				return user.getCurrentAuthority().hasRight(Right.EventLookupContact);
			}
		}
		
		@Override
		public Class<Event> type() { return Event.class; }

	}
	
	@PermissionForRight(Right.EventDetailArrangeHourClass)
	public static class EventDetailArrangeHourClass implements Permission<Class_> {
		@Autowired Permission<Class_> permissionClassDetail;
		@Autowired Permission<Exam> permissionExaminationDetail;
		
		@Override
		public boolean check(UserContext user, Class_ source) {
			// Class event -- can see Class Detail page?
			if (user.getCurrentAuthority().hasRight(Right.ClassDetail)) {
				if (permissionClassDetail.check(user, source)) return true;
			}
			// Instructors and course coordinators can also see details
			if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
				for (OfferingCoordinator coordinator: source.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getOfferingCoordinators()) {
					if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
				}
				for (ClassInstructor instructor: source.getClassInstructors()) {
					if (user.getExternalUserId().equals(instructor.getInstructor().getExternalUniqueId())) return true;
				}
			}
			return false;
		}
		
		@Override
		public Class<Class_> type() { return Class_.class; }
	}
	
	@PermissionForRight(Right.EventEdit)
	public static class EventEdit extends EventPermission<Event> {
		@Autowired PermissionSession permissionSession;
		@Autowired Permission<Date> permissionEventDate;
		@Autowired Permission<ClassEvent> permissionEventEditClass;
		@Autowired Permission<ExamEvent> permissionEventEditExam;
		
		@Override
		public boolean check(UserContext user, Event source) {
			switch (source.getEventType()) {
			case Event.sEventTypeClass:
				// Class event -- can see Class Assignment page?
				return user.getCurrentAuthority().hasRight(Right.EventEditClass) &&
						permissionEventEditClass.check(user, (source instanceof ClassEvent ? (ClassEvent)source : ClassEventDAO.getInstance().get(source.getUniqueId())));
			case Event.sEventTypeFinalExam:
			case Event.sEventTypeMidtermExam:
				// Exam event -- can see Exam Assignment page?
				return user.getCurrentAuthority().hasRight(Right.EventEditExam) &&
						permissionEventEditExam.check(user, (source instanceof ExamEvent ? (ExamEvent)source : ExamEventDAO.getInstance().get(source.getUniqueId())));
			case Event.sEventTypeUnavailable:
				if (!user.getCurrentAuthority().hasRight(Right.EventAddUnavailable)) return false;
				break;
			case Event.sEventTypeCourse:
				if (!user.getCurrentAuthority().hasRight(Right.EventAddCourseRelated)) return false;
				break;
			case Event.sEventTypeSpecial:
				if (!user.getCurrentAuthority().hasRight(Right.EventAddSpecial)) return false;
				break;
			}
			
			// Must be the owner or an event admin
			return user.getCurrentAuthority().hasRight(Right.EventLookupContact) || user.getExternalUserId().equals(source.getMainContact().getExternalUniqueId());
		}

		@Override
		public Class<Event> type() { return Event.class; }
	}
	
	@PermissionForRight(Right.EventEditClass)
	public static class EventEditClass extends EventPermission<ClassEvent> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, ClassEvent source) {
			if (source == null || source.getClazz() == null) return false;
			
			// Schedule managers can edit when allowed by academic session status
			if (user.getCurrentAuthority().hasRight(Right.ClassEdit) && permissionDepartment.check(user, source.getClazz().getManagingDept(), DepartmentStatusType.Status.Timetable))
				return true;

			// Course coordinators can edit a class
			if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
				for (OfferingCoordinator coordinator: source.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getOfferingCoordinators()) {
					if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
				}
			}
			
			return false;
		}

		@Override
		public Class<ClassEvent> type() { return ClassEvent.class; }
	}
	
	@PermissionForRight(Right.EventEditExam)
	public static class EventEditExam extends EventPermission<ExamEvent> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, ExamEvent source) {
			if (source == null || source.getExam() == null) return false;
			
			// Examination manager can edit when allowed by academic session status
			if (user.getCurrentAuthority().hasRight(Right.ExaminationEdit) && permissionSession.check(user, source.getSession(), DepartmentStatusType.Status.ExamTimetable))
				return true;
			
			// Course coordinators can edit an exam
			if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
				for (ExamOwner owner: source.getExam().getOwners()) {
					for (OfferingCoordinator coordinator: owner.getCourse().getInstructionalOffering().getOfferingCoordinators())
						if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
				}
			}
			
			return false;
		}

		@Override
		public Class<ExamEvent> type() { return ExamEvent.class; }
	}
	
	@PermissionForRight(Right.EventDate)
	public static class EventDate extends EventPermission<Date> {

		@Override
		public boolean check(UserContext user, Date source) {
			// Must be inside of the academic session, and cannot be in the past (or must have the EventEditPast override)
			return (user.getCurrentAuthority().hasRight(Right.EventEditPast) || !isPast(source)) &&
					!isOutside(source, SessionDAO.getInstance().get(user.getCurrentAcademicSessionId()));
		}
		
		@Override
		public Class<Date> type() { return Date.class; }
	}
	
	@PermissionForRight(Right.EventLocation)
	public static class EventLocation extends EventPermission<Location> {
		@Override
		public boolean check(UserContext user, Location source) {
			// Must be within user's locations (or must have the EventAnyLocation override)
			return source == null || user.getCurrentAuthority().hasRight(Right.EventAnyLocation) || locations(user.getCurrentAcademicSessionId(), user).contains(source.getUniqueId());
		}
		
		@Override
		public Class<Location> type() { return Location.class; }
	}
	
	@PermissionForRight(Right.EventLocationApprove)
	public static class EventLocationApprove extends EventPermission<Location> {
		@Autowired Permission<Location> permissionEventLocation;
		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, Location source) {
			// Has the EventAnyLocation override? 
			if (user.getCurrentAuthority().hasRight(Right.EventAnyLocation)) return true;
			
			// Must be within user's locations
			if (!locations(user.getCurrentAcademicSessionId(), user).contains(source.getUniqueId())) return false;
			
			// Can manager approve? 
			if (!user.getCurrentAuthority().hasRight(Right.StatusIndependent) && !source.getEffectiveEventStatus().isEventManagersCanApprove()) return false;
			
			// Has event department?
			return permissionDepartment.check(user, source.getEventDepartment());
		}
		
		@Override
		public Class<Location> type() { return Location.class; }
	}
	
	@PermissionForRight(Right.EventLocationUnavailable)
	public static class EventLocationUnavailable extends EventPermission<Location> {
		@Autowired Permission<Location> permissionEventLocation;
		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, Location source) {
			// Has the EventAnyLocation override? 
			if (user.getCurrentAuthority().hasRight(Right.EventAnyLocation)) return true;

			// Must be within user's locations
			if (!locations(user.getCurrentAcademicSessionId(), user).contains(source.getUniqueId())) return false;

			// Can manager request?
			if (!source.getEffectiveEventStatus().isEventManagersCanRequestEvents()) return false;
			
			// Has event department?
			return permissionDepartment.check(user, source.getEventDepartment());
		}
		
		@Override
		public Class<Location> type() { return Location.class; }
	}

	@PermissionForRight(Right.EventLocationOverbook)
	public static class EventLocationOverbook extends EventPermission<Location> {
		@Autowired Permission<Location> permissionEventLocation;
		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, Location source) {
			// Has the EventAnyLocation override? 
			if (user.getCurrentAuthority().hasRight(Right.EventAnyLocation)) return true;

			// Must be within user's locations
			if (!locations(user.getCurrentAcademicSessionId(), user).contains(source.getUniqueId())) return false;

			// Can manager request?
			if (!source.getEffectiveEventStatus().isEventManagersCanRequestEvents()) return false;
			
			// Has event department?
			return permissionDepartment.check(user, source.getEventDepartment());
		}
		
		@Override
		public Class<Location> type() { return Location.class; }
	}
	
	@PermissionForRight(Right.EventMeetingEdit)
	public static class EventMeetingEdit extends EventPermission<Meeting> {
		@Autowired Permission<Event> permissionEventEdit;
		@Autowired Permission<Date> permissionEventDate;
		@Autowired Permission<Location> permissionEventLocation;

		@Override
		public boolean check(UserContext user, Meeting source) {
			// Only pending and approved meetings can be edited
			if (source.getStatus() != Meeting.Status.PENDING && source.getStatus() != Meeting.Status.APPROVED) return false;
			
			// Is the event editable?
			if (!permissionEventEdit.check(user, source.getEvent())) return false;
			
			// Is the date ok?
			if (!permissionEventDate.check(user, source.getMeetingDate())) return false;
			
			// Is the location ok?
			if (!permissionEventLocation.check(user, source.getLocation())) return false;
			
			return true;
		}
		
		@Override
		public Class<Meeting> type() { return Meeting.class; }
	}
	
	@PermissionForRight(Right.EventMeetingDelete)
	public static class EventMeetingDelete extends EventPermission<Meeting> {
		@Autowired Permission<Date> permissionEventDate;
		@Autowired Permission<Location> permissionEventLocation;

		@Override
		public boolean check(UserContext user, Meeting source) {
			switch (source.getEvent().getEventType()) {
			case Event.sEventTypeClass:
			case Event.sEventTypeFinalExam:
			case Event.sEventTypeMidtermExam:
				// Examination and class events cannot be deleted through the event management
				return false;
			case Event.sEventTypeSpecial:
			case Event.sEventTypeCourse:
				// Only pending meetings can be deleted
				if (source.getStatus() == Meeting.Status.APPROVED) {
					Location location = source.getLocation();
					if (location == null || location.getEffectiveEventStatus().isAutomaticApproval()) return true;
				}
				if (source.getStatus() != Meeting.Status.PENDING) return false;
				break;
			case Event.sEventTypeUnavailable:
				// Only approved meetings can be deleted
				if (source.getStatus() != Meeting.Status.APPROVED) return false;
				break;
			}
			
			// Is the date ok?
			if (!permissionEventDate.check(user, source.getMeetingDate())) return false;

			// Owner can delete
			if (user.getExternalUserId().equals(source.getEvent().getMainContact().getExternalUniqueId())) return true;
			
			// Otherwise check location too
			return permissionEventLocation.check(user, source.getLocation());
		}
		
		@Override
		public Class<Meeting> type() { return Meeting.class; }
	}
	
	@PermissionForRight(Right.EventMeetingCancel)
	public static class EventMeetingCancel extends EventPermission<Meeting> {
		@Autowired Permission<Date> permissionEventDate;
		@Autowired Permission<Location> permissionEventLocationApprove;

		@Override
		public boolean check(UserContext user, Meeting source) {
			switch (source.getEvent().getEventType()) {
			case Event.sEventTypeClass:
				// Permissions EventMeetingCancelClass should be used instead
				return false;
			case Event.sEventTypeFinalExam:
			case Event.sEventTypeMidtermExam:
				// Permissions EventMeetingCancelExam should be used instead
				return false;
			case Event.sEventTypeSpecial:
			case Event.sEventTypeCourse:
				// Only pending meetings can be cancelled
				if (source.getStatus() != Meeting.Status.PENDING && source.getStatus() != Meeting.Status.APPROVED) return false;
				break;
			case Event.sEventTypeUnavailable:
				// Only approved meetings can be cancelled
				if (source.getStatus() != Meeting.Status.APPROVED) return false;
				break;
			}
			
			// If the date is ok
			if (permissionEventDate.check(user, source.getMeetingDate())) {

				// Owner can delete if date is ok
				if (user.getExternalUserId().equals(source.getEvent().getMainContact().getExternalUniqueId())) 
					return true;
				
				// Course events -- check course coordinators
				if (source.getEvent().getEventType() == Event.sEventTypeCourse && Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
					CourseEvent event = CourseEventDAO.getInstance().get(source.getEvent().getUniqueId());
					for (RelatedCourseInfo owner: event.getRelatedCourses()) {
						for (OfferingCoordinator coordinator: owner.getCourse().getInstructionalOffering().getOfferingCoordinators())
							if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
					}
				}

			}
			
			// Otherwise must be a manager
			if (!user.getCurrentAuthority().hasRight(Right.EventLookupContact)) return false;
			
			// Correct academic session?
			if (isOutside(source.getMeetingDate(), SessionDAO.getInstance().get(user.getCurrentAcademicSessionId()))) return false;
			
			// Is in the past?
			if (!user.getCurrentAuthority().hasRight(Right.EventApprovePast) && isPast(source.getMeetingDate())) return false;

			// Check the location
			return permissionEventLocationApprove.check(user, source.getLocation());
		}
		
		@Override
		public Class<Meeting> type() { return Meeting.class; }
	}
	
	@PermissionForRight(Right.EventMeetingCancelClass)
	public static class EventMeetingCancelClass extends EventPermission<Meeting> {
		@Autowired Permission<Date> permissionEventDate;
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Meeting source) {
			if (source.getEvent().getEventType() != Event.sEventTypeClass) return false;

			// Only approved meetings can be cancelled
			if (source.getStatus() != Meeting.Status.APPROVED) return false;

			// Is the date ok?
			if (!permissionEventDate.check(user, source.getMeetingDate())) return false;
			
			Class_ clazz = (source.getEvent() instanceof ClassEvent ? (ClassEvent)source.getEvent() : ClassEventDAO.getInstance().get(source.getEvent().getUniqueId())).getClazz();
			if (clazz == null) return false;
			
			// Course coordinators can cancel a class
			if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
				for (OfferingCoordinator coordinator: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getOfferingCoordinators()) {
					if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
				}
				return false;
			}
			
			// Check departmental permissions
			return permissionDepartment.check(user, clazz.getManagingDept(), DepartmentStatusType.Status.Timetable);
		}

		@Override
		public Class<Meeting> type() { return Meeting.class; }
	}
	
	@PermissionForRight(Right.EventMeetingCancelExam)
	public static class EventMeetingCancelExam extends EventPermission<Meeting> {
		@Autowired Permission<Date> permissionEventDate;
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Meeting source) {
			if (source.getEvent().getEventType() != Event.sEventTypeFinalExam && source.getEvent().getEventType() != Event.sEventTypeMidtermExam) return false;

			// Only approved meetings can be cancelled
			if (source.getStatus() != Meeting.Status.APPROVED) return false;

			// Is the date ok?
			if (!permissionEventDate.check(user, source.getMeetingDate())) return false;

			Exam exam = (source.getEvent() instanceof ExamEvent ? (ExamEvent)source.getEvent() : ExamEventDAO.getInstance().get(source.getEvent().getUniqueId())).getExam();
			if (exam == null) return false;
			
			// Course coordinators can cancel an exam
			if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
				for (ExamOwner owner: exam.getOwners()) {
					for (OfferingCoordinator coordinator: owner.getCourse().getInstructionalOffering().getOfferingCoordinators())
						if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
				}
				return false;
			}
			
			// Otherwise check session permission
			return permissionSession.check(user, exam.getSession(), DepartmentStatusType.Status.ExamTimetable);
		}

		@Override
		public Class<Meeting> type() { return Meeting.class; }
	}
	
	@PermissionForRight(Right.EventMeetingInquireClass)
	public static class EventMeetingInquireClass extends EventPermission<Meeting> {
		@Autowired Permission<Date> permissionEventDate;
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Meeting source) {
			if (source.getEvent().getEventType() != Event.sEventTypeClass) return false;

			// Only pending and approved meetings can be inquired
			if (source.getStatus() != Meeting.Status.PENDING && source.getStatus() != Meeting.Status.APPROVED) return false;

			// Is the date ok?
			if (!permissionEventDate.check(user, source.getMeetingDate())) return false;
			
			Class_ clazz = (source.getEvent() instanceof ClassEvent ? (ClassEvent)source.getEvent() : ClassEventDAO.getInstance().get(source.getEvent().getUniqueId())).getClazz();
			if (clazz == null) return false;
			
			// Course coordinators can cancel a class
			if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
				for (OfferingCoordinator coordinator: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getOfferingCoordinators()) {
					if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
				}
				return false;
			}
			
			// Check departmental permissions
			return permissionDepartment.check(user, clazz.getControllingDept(), DepartmentStatusType.Status.OwnerView);
		}

		@Override
		public Class<Meeting> type() { return Meeting.class; }
	}
	
	@PermissionForRight(Right.EventMeetingInquireExam)
	public static class EventMeetingInquireExam extends EventPermission<Meeting> {
		@Autowired Permission<Date> permissionEventDate;
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Meeting source) {
			if (source.getEvent().getEventType() != Event.sEventTypeFinalExam && source.getEvent().getEventType() != Event.sEventTypeMidtermExam) return false;

			// Only pending and approved meetings can be inquired
			if (source.getStatus() != Meeting.Status.PENDING && source.getStatus() != Meeting.Status.APPROVED) return false;

			// Only approved meetings can be cancelled
			if (source.getStatus() != Meeting.Status.APPROVED) return false;

			// Is the date ok?
			if (!permissionEventDate.check(user, source.getMeetingDate())) return false;

			Exam exam = (source.getEvent() instanceof ExamEvent ? (ExamEvent)source.getEvent() : ExamEventDAO.getInstance().get(source.getEvent().getUniqueId())).getExam();
			if (exam == null) return false;
			
			// Course coordinators can cancel an exam
			if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
				for (ExamOwner owner: exam.getOwners()) {
					for (OfferingCoordinator coordinator: owner.getCourse().getInstructionalOffering().getOfferingCoordinators())
						if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
				}
				return false;
			}
			
			// Otherwise check session permission
			return permissionSession.check(user, exam.getSession(), DepartmentStatusType.Status.OwnerView, DepartmentStatusType.Status.ManagerView);
		}

		@Override
		public Class<Meeting> type() { return Meeting.class; }
	}
	
	@PermissionForRight(Right.EventMeetingApprove)
	public static class EventMeetingApprove extends EventPermission<Meeting> {
		@Autowired Permission<Location> permissionEventLocationApprove;
		
		@Override
		public boolean check(UserContext user, Meeting source) {
			// Only pending meetings can be approved
			if (source.getStatus() != Meeting.Status.PENDING) return false;
			
			// Following events are implicitly approved
			switch (source.getEvent().getEventType()) {
			case Event.sEventTypeClass:
			case Event.sEventTypeFinalExam:
			case Event.sEventTypeMidtermExam:
			case Event.sEventTypeUnavailable:
				return false;
			}
			
			// Correct academic session?
			if (isOutside(source.getMeetingDate(), SessionDAO.getInstance().get(user.getCurrentAcademicSessionId()))) return false;
			
			// Is in the past?
			if (!user.getCurrentAuthority().hasRight(Right.EventApprovePast) && isPast(source.getMeetingDate())) return false;

			// Check the location
			return permissionEventLocationApprove.check(user, source.getLocation());
		}
		
		@Override
		public Class<Meeting> type() { return Meeting.class; }
	}
	
	@PermissionForRight(Right.EventMeetingInquire)
	public static class EventMeetingInquire extends EventPermission<Meeting> {
		@Autowired Permission<Location> permissionEventLocation;
		
		@Override
		public boolean check(UserContext user, Meeting source) {
			// Only pending and approved meetings can be inquired
			if (source.getStatus() != Meeting.Status.PENDING && source.getStatus() != Meeting.Status.APPROVED) return false;
			
			// Following events cannot be inquired
			switch (source.getEvent().getEventType()) {
			case Event.sEventTypeClass:
			case Event.sEventTypeFinalExam:
			case Event.sEventTypeMidtermExam:
				return false;
			}
			
			// Correct academic session?
			if (isOutside(source.getMeetingDate(), SessionDAO.getInstance().get(user.getCurrentAcademicSessionId()))) return false;
			
			// Is in the past?
			if (!user.getCurrentAuthority().hasRight(Right.EventApprovePast) && isPast(source.getMeetingDate())) return false;

			// Check the location
			return permissionEventLocation.check(user, source.getLocation());
		}
		
		@Override
		public Class<Meeting> type() { return Meeting.class; }
	}
	
}
