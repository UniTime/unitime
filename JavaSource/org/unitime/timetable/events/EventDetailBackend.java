/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.unitime.commons.User;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventException;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventDetailRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConglictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;

public class EventDetailBackend implements GwtRpcImplementation<EventDetailRpcRequest, EventInterface> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public EventInterface execute(EventDetailRpcRequest request, GwtRpcHelper helper) {
		Event event = EventDAO.getInstance().get(request.getEventId());
		if (event == null)
			throw new EventException("No event with id " + request.getEventId() + " found.");
		
		checkAccess(event, helper);
		
		EventInterface detail = getEventDetail(SessionDAO.getInstance().get(request.getSessionId()), event, helper.getUser());
		
		return detail;
	}
	
	public void checkAccess(Event e, GwtRpcHelper helper) throws PageAccessException {
		if (helper.getUser() == null) {
			throw new PageAccessException(helper.isHttpSessionNew() ? MESSAGES.authenticationExpired() : MESSAGES.authenticationRequired());
		}
		if (helper.getUser().getRole() == null) {
			if (e.getMainContact() == null || !helper.getUser().getId().equals(e.getMainContact().getExternalUniqueId()))
				throw new PageAccessException(MESSAGES.authenticationInsufficient());
		}
	}
	
	public boolean canEdit(Event e, GwtRpcHelper helper) {
		if (helper.getUser() == null) {
			return false;
		}
		if (e.getMainContact() != null && helper.getUser().getId().equals(e.getMainContact().getExternalUniqueId()))
			return true;
		if (Roles.ADMIN_ROLE.equals(helper.getUser().getRole()) || Roles.EVENT_MGR_ROLE.equals(helper.getUser().getRole()))
			return true;
		return false;
	}

	public EventInterface getEventDetail(Session session, Event e, User user) throws EventException {
		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		Date now = new Date();
		
        Set<Department> userDepartments = null;
		if (user != null && Roles.EVENT_MGR_ROLE.equals(user.getRole())) {
			TimetableManager mgr = TimetableManager.getManager(user);
			if (mgr != null)
				userDepartments = mgr.getDepartments();
		}

		
		EventInterface event = new EventInterface();
		event.setId(e.getUniqueId());
		event.setName(e.getEventName());
		event.setType(EventInterface.EventType.values()[e.getEventType()]);
		event.setCanView(true);
		event.setEmail(e.getEmail());
		event.setMaxCapacity(e.getMaxCapacity());
				
		if (e.getMainContact() != null) {
			ContactInterface contact = new ContactInterface();
			contact.setFirstName(e.getMainContact().getFirstName());
			contact.setMiddleName(e.getMainContact().getMiddleName());
			contact.setLastName(e.getMainContact().getLastName());
			contact.setExternalId(e.getMainContact().getExternalUniqueId());
			contact.setPhone(e.getMainContact().getPhone());
			contact.setEmail(e.getMainContact().getEmailAddress());
			event.setContact(contact);
		}
		
		for (EventContact c: e.getAdditionalContacts()) {
			ContactInterface contact = new ContactInterface();
			contact.setFirstName(c.getFirstName());
			contact.setMiddleName(c.getMiddleName());
			contact.setLastName(c.getLastName());
			contact.setExternalId(c.getExternalUniqueId());
			contact.setPhone(c.getPhone());
			contact.setEmail(c.getEmailAddress());
			event.addAdditionalContact(contact);
		}
		
		if (e.getSponsoringOrganization() != null) {
			SponsoringOrganizationInterface sponsor = new SponsoringOrganizationInterface();
			sponsor.setEmail(e.getSponsoringOrganization().getEmail());
			sponsor.setName(e.getSponsoringOrganization().getName());
			sponsor.setUniqueId(e.getSponsoringOrganization().getUniqueId());
			event.setSponsor(sponsor);
		}
		
    	if (Event.sEventTypeClass == e.getEventType()) {
    		ClassEvent ce = (e instanceof ClassEvent ? (ClassEvent)e : ClassEventDAO.getInstance().get(e.getUniqueId(), hibSession));
    		
    		Class_ clazz = ce.getClazz();
    		event.setEnrollment(clazz.getEnrollment());

    		RelatedObjectInterface related = new RelatedObjectInterface();
    		related.setType(RelatedObjectInterface.RelatedObjectType.Class);
    		related.setUniqueId(clazz.getUniqueId());
    		related.setName(clazz.getClassLabel(hibSession));
    		CourseOffering courseOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
    		related.addCourseName(courseOffering.getCourseName());
    		related.setSectionNumber(clazz.getSectionNumberString(hibSession));
    		if (clazz.getClassSuffix() != null)
    			related.addExternalId(clazz.getClassSuffix());
    		related.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
    		related.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
    		for (CourseOffering co: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
				if (!co.isIsControl()) {
					related.addCourseName(co.getCourseName());
					if (clazz.getClassSuffix(co) != null)
		    			related.addExternalId(clazz.getClassSuffix(co));
				}
    		}
    		if (clazz.getDisplayInstructor()) {
    			for (ClassInstructor i: clazz.getClassInstructors()) {
					ContactInterface instructor = new ContactInterface();
					instructor.setFirstName(i.getInstructor().getFirstName());
					instructor.setMiddleName(i.getInstructor().getMiddleName());
					instructor.setLastName(i.getInstructor().getLastName());
					instructor.setExternalId(i.getInstructor().getExternalUniqueId());
					instructor.setEmail(i.getInstructor().getEmail());
					related.addInstructor(instructor);
    			}
    		}
    		Assignment assignment = clazz.getCommittedAssignment();
    		if (assignment != null) {
    			TimeLocation time = assignment.getTimeLocation();
    			if (time != null) {
    				related.setTime(time.getDayHeader() + " " + time.getStartTimeHeader() + " - " + time.getEndTimeHeader());
    				related.setDate(time.getDatePatternName());
    			}
    			for (Location r: assignment.getRooms()) {
    				ResourceInterface location = new ResourceInterface();
    				location.setType(ResourceType.ROOM);
    				location.setId(r.getUniqueId());
    				location.setName(r.getLabel());
    				location.setHint(r.getHtmlHint());
    				location.setSize(r.getCapacity());
    				location.setRoomType(r.getRoomTypeLabel());
    				related.addLocation(location);
    			}
    		}
    		event.addRelatedObject(related);
    	} else if (Event.sEventTypeFinalExam == e.getEventType() || Event.sEventTypeMidtermExam == e.getEventType()) {
    		ExamEvent xe = (e instanceof ExamEvent ? (ExamEvent)e : ExamEventDAO.getInstance().get(e.getUniqueId(), hibSession));
    		event.setEnrollment(xe.getExam().countStudents());
    		
    		RelatedObjectInterface related = new RelatedObjectInterface();
    		related.setType(RelatedObjectInterface.RelatedObjectType.Examination);
    		related.setUniqueId(xe.getExam().getUniqueId());
    		related.setName(xe.getExam().getName() == null ? xe.getExam().generateName() : xe.getExam().getName());
    		related.addCourseName(related.getName());
    		related.setInstruction(e.getEventTypeAbbv());
    		related.setInstructionType(xe.getExam().getExamType());
    		related.setSectionNumber(xe.getExam().getLength().toString());
    		if (xe.getExam().getAssignedPeriod() != null) {
    			ExamPeriod period = xe.getExam().getAssignedPeriod();
    			related.setDate(period.getStartDateLabel());
    			int printOffset = (xe.getExam().getPrintOffset() == null ? 0 : xe.getExam().getPrintOffset());
    			related.setTime(period.getStartTimeLabel(printOffset) + " - " + period.getEndTimeLabel(xe.getExam().getLength(), printOffset));
    		}
    		for (Location r: xe.getExam().getAssignedRooms()) {
				ResourceInterface location = new ResourceInterface();
				location.setType(ResourceType.ROOM);
				location.setId(r.getUniqueId());
				location.setName(r.getLabel());
				location.setHint(r.getHtmlHint());
				location.setSize(r.getCapacity());
				location.setRoomType(r.getRoomTypeLabel());
				related.addLocation(location);
    		}
    		for (DepartmentalInstructor i: xe.getExam().getInstructors()) {
				ContactInterface instructor = new ContactInterface();
				instructor.setFirstName(i.getFirstName());
				instructor.setMiddleName(i.getMiddleName());
				instructor.setLastName(i.getLastName());
				instructor.setExternalId(i.getExternalUniqueId());
				instructor.setEmail(i.getEmail());
				related.addInstructor(instructor);
			}
    		event.addRelatedObject(related);
    		
			for (ExamOwner owner: new TreeSet<ExamOwner>(xe.getExam().getOwners())) {
				related = new RelatedObjectInterface();
				related.setType(RelatedObjectInterface.RelatedObjectType.values()[owner.getOwnerType()]);
				related.setUniqueId(owner.getOwnerId());
				related.setName(owner.getLabel());
				related.addCourseName(owner.getCourse().getCourseName());
				if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass) {
					Class_ clazz = (Class_)owner.getOwnerObject();
					related.setSectionNumber(clazz.getSectionNumberString(hibSession));
		    		if (clazz.getClassSuffix() != null)
		    			related.addExternalId(clazz.getClassSuffix());
		    		related.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
		    		related.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
						if (!course.isIsControl()) {
							related.addCourseName(course.getCourseName());
							if (clazz.getClassSuffix(course) != null)
				    			related.addExternalId(clazz.getClassSuffix(course));
						}
		    		}
		    		Assignment assignment = clazz.getCommittedAssignment();
		    		if (assignment != null) {
		    			TimeLocation time = assignment.getTimeLocation();
		    			if (time != null) {
		    				related.setTime(time.getDayHeader() + " " + time.getStartTimeHeader() + " - " + time.getEndTimeHeader());
		    				related.setDate(time.getDatePatternName());
		    			}
		    			for (Location r: assignment.getRooms()) {
		    				ResourceInterface location = new ResourceInterface();
		    				location.setType(ResourceType.ROOM);
		    				location.setId(r.getUniqueId());
		    				location.setName(r.getLabel());
		    				location.setHint(r.getHtmlHint());
		    				location.setSize(r.getCapacity());
		    				location.setRoomType(r.getRoomTypeLabel());
		    				related.addLocation(location);
		    			}
		    		}
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeOffering) {
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
		    			if (!course.isIsControl()) {
							related.addCourseName(course.getCourseName());
		    			}
		    		}
					related.setInstruction("Offering");
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeConfig) {
					InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
					related.setSectionNumber(config.getName());
					related.setInstruction("Configuration");
				} else {
					related.setInstruction("Course");
				}
				event.addRelatedObject(related);
			}
    	} else if (Event.sEventTypeCourse == e.getEventType()) {
    		CourseEvent ce = (e instanceof CourseEvent ? (CourseEvent)e : CourseEventDAO.getInstance().get(e.getUniqueId(), hibSession));
    		
    		int enrl = 0;
			for (RelatedCourseInfo owner: new TreeSet<RelatedCourseInfo>(ce.getRelatedCourses())) {
				RelatedObjectInterface related = new RelatedObjectInterface();
				related.setType(RelatedObjectInterface.RelatedObjectType.values()[owner.getOwnerType()]);
				related.setUniqueId(owner.getOwnerId());
				related.addCourseName(owner.getCourse().getCourseName());
				related.setName(owner.getLabel());
				related.setSelection(new long[] { owner.getCourse().getSubjectArea().getUniqueId(), owner.getCourse().getUniqueId()});
				if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass) {
					Class_ clazz = (Class_)owner.getOwnerObject();
					related.setSelection(new long[] { owner.getCourse().getSubjectArea().getUniqueId(), owner.getCourse().getUniqueId(), clazz.getSchedulingSubpart().getUniqueId(), clazz.getUniqueId()});
					related.setSectionNumber(clazz.getSectionNumberString(hibSession));
		    		if (clazz.getClassSuffix() != null)
		    			related.addExternalId(clazz.getClassSuffix());
		    		related.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
		    		related.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
						if (!course.isIsControl()) {
							related.addCourseName(course.getCourseName());
							if (clazz.getClassSuffix(course) != null)
				    			related.addExternalId(clazz.getClassSuffix(course));
						}
		    		}
		    		Assignment assignment = clazz.getCommittedAssignment();
		    		if (assignment != null) {
		    			TimeLocation time = assignment.getTimeLocation();
		    			if (time != null) {
		    				related.setTime(time.getDayHeader() + " " + time.getStartTimeHeader() + " - " + time.getEndTimeHeader());
		    				related.setDate(time.getDatePatternName());
		    			}
		    			for (Location r: assignment.getRooms()) {
		    				ResourceInterface location = new ResourceInterface();
		    				location.setType(ResourceType.ROOM);
		    				location.setId(r.getUniqueId());
		    				location.setName(r.getLabel());
		    				location.setHint(r.getHtmlHint());
		    				location.setSize(r.getCapacity());
		    				location.setRoomType(r.getRoomTypeLabel());
		    				related.addLocation(location);
		    			}
		    		}
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeOffering) {
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
		    			if (!course.isIsControl()) {
							related.addCourseName(course.getCourseName());
		    			}
		    		}
		    		related.setInstruction("Offering");
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeConfig) {
					InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
					related.setSelection(new long[] { owner.getCourse().getSubjectArea().getUniqueId(), owner.getCourse().getUniqueId(), -config.getUniqueId()});
					related.setSectionNumber(config.getName());
					related.setInstruction("Configuration");
				} else {
					related.setInstruction("Course");
				}
				event.addRelatedObject(related);
				enrl += owner.countStudents();
			}
			event.setEnrollment(enrl);
    		
    	}
    	
    	// overlaps
    	Map<Long, Set<Meeting>> overlaps = new HashMap<Long, Set<Meeting>>();
    	for (Object[] o: (List<Object[]>)EventDAO.getInstance().getSession().createQuery(
				"select m.uniqueId, o from Event e inner join e.meetings m, Meeting o "+
				"where e.uniqueId = :eventId and m.uniqueId != o.uniqueId and " +
				"o.startPeriod < m.stopPeriod and o.stopPeriod > m.startPeriod and " +
				"m.locationPermanentId = o.locationPermanentId and m.meetingDate = o.meetingDate")
				.setLong("eventId", e.getUniqueId())
				.list()) {
    		Long meetingId = (Long)o[0];
    		Meeting overlap = (Meeting)o[1];
    		Set<Meeting> overlapsThisMeeting = overlaps.get(meetingId);
    		if (overlapsThisMeeting == null) {
    			overlapsThisMeeting = new TreeSet<Meeting>();
    			overlaps.put(meetingId, overlapsThisMeeting);
    		}
    		overlapsThisMeeting.add(overlap);
		}
    		
    	for (Meeting m: e.getMeetings()) {
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
			meeting.setPast(m.getStartTime().before(now));
			if (m.isApproved())
				meeting.setApprovalDate(m.getApprovedDate());
			if (user == null || meeting.isPast() || (event.getType() != EventType.Special && event.getType() != EventType.Course)) {
				meeting.setCanEdit(false);
			} else {
				meeting.setCanEdit(m.getEvent().getMainContact() != null && user.getId().equals(m.getEvent().getMainContact().getExternalUniqueId()));
				if (Roles.ADMIN_ROLE.equals(user.getRole())) {
					meeting.setCanApprove(true);
				} else if (Roles.EVENT_MGR_ROLE.equals(user.getRole())) {
					meeting.setCanApprove(m.getLocation() == null || 
							(userDepartments != null && m.getLocation().getControllingDepartment() != null && userDepartments.contains(m.getLocation().getControllingDepartment()))
							);
				} else {
					meeting.setCanApprove(false);
				}
			}
			if (m.getLocation() != null) {
				ResourceInterface location = new ResourceInterface();
				location.setType(ResourceType.ROOM);
				location.setId(m.getLocation().getUniqueId());
				location.setName(m.getLocation().getLabel());
				location.setHint(m.getLocation().getHtmlHint());
				location.setSize(m.getLocation().getCapacity());
				location.setRoomType(m.getLocation().getRoomTypeLabel());
				meeting.setLocation(location);
			}
			Set<Meeting> overlapsThisMeeting = overlaps.get(m.getUniqueId());
			if (overlapsThisMeeting != null) {
				for (Meeting overlap: overlapsThisMeeting) {
					MeetingConglictInterface conflict = new MeetingConglictInterface();

					conflict.setEventId(overlap.getEvent().getUniqueId());
					conflict.setName(overlap.getEvent().getEventName());
					conflict.setType(EventInterface.EventType.values()[overlap.getEvent().getEventType()]);
					
					conflict.setId(overlap.getUniqueId());
					conflict.setMeetingDate(overlap.getMeetingDate());
					conflict.setDayOfYear(meeting.getDayOfYear());
					conflict.setStartSlot(overlap.getStartPeriod());
					conflict.setEndSlot(overlap.getStopPeriod());
					conflict.setStartOffset(overlap.getStartOffset() == null ? 0 : overlap.getStartOffset());
					conflict.setEndOffset(overlap.getStopOffset() == null ? 0 : overlap.getStopOffset());
					if (overlap.isApproved())
						conflict.setApprovalDate(overlap.getApprovedDate());
					
					meeting.addConflict(conflict);
				}
			}
			
			event.addMeeting(meeting);
		}
    	
    	for (EventNote n: e.getNotes()) {
    		NoteInterface note = new NoteInterface();
    		note.setDate(n.getTimeStamp());
    		note.setType(NoteInterface.NoteType.values()[n.getNoteType()]);
    		note.setMeetings(n.getMeetingsHtml());
    		note.setNote(n.getTextNote());
    		note.setUser(n.getUser());
    		event.addNote(note);
    	}
    	
    	return event;
	}
}
