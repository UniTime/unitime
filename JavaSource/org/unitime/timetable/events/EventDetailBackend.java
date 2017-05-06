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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.model.TimeLocation;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventDetailRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConflictInterface;
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
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SpecialEvent;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(EventDetailRpcRequest.class)
public class EventDetailBackend extends EventAction<EventDetailRpcRequest, EventInterface> {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Override
	public EventInterface execute(EventDetailRpcRequest request, EventContext context) {
		if (request.getEventId() != null && request.getEventId() < 0) {
			Class_ clazz = Class_DAO.getInstance().get(-request.getEventId());
			if (clazz == null)
				throw new GwtRpcException("No class with id " + (-request.getEventId()) + " found.");
			
			if (clazz.getEvent() != null) {
				context.checkPermission(clazz.getEvent(), Right.EventDetail);
				return getEventDetail(clazz.getControllingDept().getSession(), clazz.getEvent(), context);
			}
			
			context.checkPermission(clazz, Right.EventDetailArrangeHourClass);
			return getArrangeHoursClassDetail(clazz, context);
		} else {
			Event event = EventDAO.getInstance().get(request.getEventId());
			if (event == null)
				throw new GwtRpcException("No event with id " + request.getEventId() + " found.");
			
			context.checkPermission(event, Right.EventDetail);
			return getEventDetail(SessionDAO.getInstance().get(request.getSessionId()), event, context);
		}
	}
	
	public static EventInterface getEventDetail(Session session, Event e, EventContext context) throws GwtRpcException {
		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		EventInterface event = new EventInterface();
		event.setId(e.getUniqueId());
		event.setName(e.getEventName());
		event.setType(EventInterface.EventType.values()[e.getEventType()]);
		event.setCanView(context != null && context.hasPermission(e, Right.EventDetail));
		event.setCanEdit(context != null && context.hasPermission(e, Right.EventEdit)); 
		event.setEmail(e.getEmail());
		event.setExpirationDate(e.getExpirationDate());
		event.setMaxCapacity(e.getMaxCapacity());
		String nameFormat = (context != null ? context.getUser().getProperty(UserProperty.NameFormat) : NameFormat.LAST_FIRST_MIDDLE.reference());
				
		if (e.getMainContact() != null) {
			ContactInterface contact = new ContactInterface();
			contact.setFirstName(e.getMainContact().getFirstName());
			contact.setMiddleName(e.getMainContact().getMiddleName());
			contact.setLastName(e.getMainContact().getLastName());
			contact.setAcademicTitle(e.getMainContact().getAcademicTitle());
			contact.setExternalId(e.getMainContact().getExternalUniqueId());
			contact.setPhone(e.getMainContact().getPhone());
			contact.setEmail(e.getMainContact().getEmailAddress());
			contact.setFormattedName(e.getMainContact().getName(nameFormat));
			event.setContact(contact);
		}
		
		for (EventContact c: e.getAdditionalContacts()) {
			ContactInterface contact = new ContactInterface();
			contact.setFirstName(c.getFirstName());
			contact.setMiddleName(c.getMiddleName());
			contact.setLastName(c.getLastName());
			contact.setAcademicTitle(c.getAcademicTitle());
			contact.setExternalId(c.getExternalUniqueId());
			contact.setPhone(c.getPhone());
			contact.setEmail(c.getEmailAddress());
			contact.setFormattedName(c.getName(nameFormat));
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
    		event.setMaxCapacity(clazz.getClassLimit());
    		Set<Long> addedInstructorIds = new HashSet<Long>();
    		if (clazz.getDisplayInstructor()) {
    			for (ClassInstructor i: clazz.getClassInstructors()) {
    				if (i.getResponsibility() != null && i.getResponsibility().hasOption(TeachingResponsibility.Option.noevents)) continue;
					ContactInterface instructor = new ContactInterface();
					instructor.setFirstName(i.getInstructor().getFirstName());
					instructor.setMiddleName(i.getInstructor().getMiddleName());
					instructor.setLastName(i.getInstructor().getLastName());
					instructor.setAcademicTitle(i.getInstructor().getAcademicTitle());
					instructor.setEmail(i.getInstructor().getEmail());
					instructor.setFormattedName(i.getInstructor().getName(nameFormat));
					if (i.getResponsibility() != null) {
						instructor.setResponsibility(i.getResponsibility().getLabel());
						instructor.setResponsibilityAbbreviation(i.getResponsibility().getAbbreviation());
    				}
					event.addInstructor(instructor);
					addedInstructorIds.add(i.getInstructor().getUniqueId());
    			}
    		}
    		for (OfferingCoordinator oc: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getOfferingCoordinators()) {
    			if (oc.getResponsibility() != null && oc.getResponsibility().hasOption(TeachingResponsibility.Option.noevents)) continue;
    			DepartmentalInstructor c = oc.getInstructor();
    			if (addedInstructorIds.add(c.getUniqueId())) {
        			ContactInterface coordinator = new ContactInterface();
    				coordinator.setFirstName(c.getFirstName());
    				coordinator.setMiddleName(c.getMiddleName());
    				coordinator.setLastName(c.getLastName());
    				coordinator.setAcademicTitle(c.getAcademicTitle());
    				coordinator.setEmail(c.getEmail());
    				coordinator.setFormattedName(c.getName(nameFormat));
    				if (oc.getResponsibility() != null) {
    					coordinator.setResponsibility(oc.getResponsibility().getLabel());
    					coordinator.setResponsibilityAbbreviation(oc.getResponsibility().getAbbreviation());
    				}
    				event.addCoordinator(coordinator);
    			}
    		}
    		
    		RelatedObjectInterface related = new RelatedObjectInterface();
    		related.setType(RelatedObjectInterface.RelatedObjectType.Class);
    		related.setUniqueId(clazz.getUniqueId());
    		related.setName(clazz.getClassLabel(hibSession));
    		String note = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getScheduleBookNote();
    		if (clazz.getSchedulePrintNote() != null && !clazz.getSchedulePrintNote().isEmpty())
    			note = (note == null || note.isEmpty() ? "" : note + "\n") + clazz.getSchedulePrintNote();
			related.setNote(note);
			if (context != null && context.hasPermission(clazz, Right.ClassDetail))
				related.setDetailPage("classDetail.do?cid=" + clazz.getUniqueId());

    		CourseOffering courseOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
    		related.addCourseId(courseOffering.getUniqueId());
    		related.addCourseName(courseOffering.getCourseName());
    		related.addCourseTitle(courseOffering.getTitle() == null ? "" : courseOffering.getTitle());
    		related.setSectionNumber(clazz.getSectionNumberString(hibSession));
    		if (clazz.getClassSuffix() != null)
    			related.addExternalId(clazz.getClassSuffix());
    		related.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
    		if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() != null)
    			related.setInstruction(related.getInstruction() + " (" + clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
    		related.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
    		for (CourseOffering co: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
				if (!co.isIsControl()) {
					related.addCourseId(co.getUniqueId());
					related.addCourseName(co.getCourseName());
					related.addCourseTitle(co.getTitle() == null ? "" : co.getTitle());
					if (clazz.getClassSuffix(co) != null)
		    			related.addExternalId(clazz.getClassSuffix(co));
				}
    		}
    		if (clazz.getDisplayInstructor()) {
    			for (ClassInstructor i: clazz.getClassInstructors()) {
    				if (i.getResponsibility() != null && i.getResponsibility().hasOption(TeachingResponsibility.Option.noevents)) continue;
					ContactInterface instructor = new ContactInterface();
					instructor.setFirstName(i.getInstructor().getFirstName());
					instructor.setMiddleName(i.getInstructor().getMiddleName());
					instructor.setLastName(i.getInstructor().getLastName());
					instructor.setAcademicTitle(i.getInstructor().getAcademicTitle());
					instructor.setExternalId(i.getInstructor().getExternalUniqueId());
					instructor.setEmail(i.getInstructor().getEmail());
					instructor.setFormattedName(i.getInstructor().getName(nameFormat));
					if (i.getResponsibility() != null) {
						instructor.setResponsibility(i.getResponsibility().getLabel());
						instructor.setResponsibilityAbbreviation(i.getResponsibility().getAbbreviation());
    				}
					related.addInstructor(instructor);
    			}
    		}
    		Assignment assignment = clazz.getCommittedAssignment();
    		if (assignment != null) {
    			TimeLocation time = assignment.getTimeLocation();
    			if (time != null) {
    				related.setTime(time.getDayHeader() + " " + time.getStartTimeHeader(CONSTANTS.useAmPm()) + " - " + time.getEndTimeHeader(CONSTANTS.useAmPm()));
    				related.setDate(time.getDatePatternName());
    			}
    			for (Location r: assignment.getRooms()) {
    				ResourceInterface location = new ResourceInterface();
    				location.setType(ResourceType.ROOM);
    				location.setId(r.getUniqueId());
    				location.setName(r.getLabel());
    				location.setSize(r.getCapacity());
    				location.setRoomType(r.getRoomTypeLabel());
    				location.setBreakTime(r.getEffectiveBreakTime());
    				location.setMessage(r.getEventMessage());
    				location.setIgnoreRoomCheck(r.isIgnoreRoomCheck());
    				location.setDisplayName(r.getDisplayName());
    				related.addLocation(location);
    			}
    		}
    		event.addRelatedObject(related);
    		
    		event.addCourseId(courseOffering.getUniqueId());
    		event.addCourseName(courseOffering.getCourseName());
    		event.addCourseTitle(courseOffering.getTitle() == null ? "" : courseOffering.getTitle());
    		event.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc().length() <= 20 ? clazz.getSchedulingSubpart().getItype().getDesc() : clazz.getSchedulingSubpart().getItype().getAbbv());
    		if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() != null)
    			event.setInstruction(event.getInstruction() + " (" + clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
    		event.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
    		event.setSectionNumber(clazz.getSectionNumberString(hibSession));
    		if (clazz.getClassSuffix(courseOffering) == null) {
	    		event.setName(clazz.getClassLabel(courseOffering));
    		} else {
	    		event.addExternalId(clazz.getClassSuffix(courseOffering));
    			event.setName(courseOffering.getCourseName() + " " + clazz.getClassSuffix(courseOffering));
    		}
			for (CourseOffering co: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
				if (!co.isIsControl()) {
					event.addCourseId(co.getUniqueId());
					event.addCourseName(co.getCourseName());
					event.addCourseTitle(co.getTitle() == null ? "" : co.getTitle());
					if (clazz.getClassSuffix(co) != null)
						event.addExternalId(clazz.getClassSuffix(co));
				}
			}
    		
    	} else if (Event.sEventTypeFinalExam == e.getEventType() || Event.sEventTypeMidtermExam == e.getEventType()) {
    		ExamEvent xe = (e instanceof ExamEvent ? (ExamEvent)e : ExamEventDAO.getInstance().get(e.getUniqueId(), hibSession));
    		event.setEnrollment(xe.getExam().countStudents());
    		event.setMaxCapacity(xe.getExam().getSize());
    		Set<Long> addedInstructorIds = new HashSet<Long>();
    		for (DepartmentalInstructor i: xe.getExam().getInstructors()) {
				ContactInterface instructor = new ContactInterface();
				instructor.setFirstName(i.getFirstName());
				instructor.setMiddleName(i.getMiddleName());
				instructor.setLastName(i.getLastName());
				instructor.setAcademicTitle(i.getAcademicTitle());
				instructor.setEmail(i.getEmail());
				instructor.setFormattedName(i.getName(nameFormat));
				event.addInstructor(instructor);
				addedInstructorIds.add(i.getUniqueId());
			}
    		
    		RelatedObjectInterface related = new RelatedObjectInterface();
    		related.setType(RelatedObjectInterface.RelatedObjectType.Examination);
    		related.setUniqueId(xe.getExam().getUniqueId());
    		related.setName(xe.getExam().getName() == null ? xe.getExam().generateName() : xe.getExam().getName());
    		related.addCourseName(related.getName());
    		related.addCourseTitle("");
    		related.setInstruction(e.getEventTypeAbbv());
    		related.setInstructionType(xe.getExam().getExamType().getType());
    		related.setSectionNumber(xe.getExam().getLength().toString());
			if (context != null && context.hasPermission(xe.getExam(), Right.ExaminationDetail))
				related.setDetailPage("examDetail.do?examId=" + xe.getExam().getUniqueId());
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
				location.setSize(r.getCapacity());
				location.setRoomType(r.getRoomTypeLabel());
				location.setBreakTime(r.getEffectiveBreakTime());
				location.setMessage(r.getEventMessage());
				location.setIgnoreRoomCheck(r.isIgnoreRoomCheck());
				location.setDisplayName(r.getDisplayName());
				related.addLocation(location);
    		}
    		for (DepartmentalInstructor i: xe.getExam().getInstructors()) {
				ContactInterface instructor = new ContactInterface();
				instructor.setFirstName(i.getFirstName());
				instructor.setMiddleName(i.getMiddleName());
				instructor.setLastName(i.getLastName());
				instructor.setAcademicTitle(i.getAcademicTitle());
				instructor.setExternalId(i.getExternalUniqueId());
				instructor.setEmail(i.getEmail());
				instructor.setFormattedName(i.getName(nameFormat));
				related.addInstructor(instructor);
			}
    		event.addRelatedObject(related);
    		
			for (ExamOwner owner: new TreeSet<ExamOwner>(xe.getExam().getOwners())) {
				related = new RelatedObjectInterface();
				related.setType(RelatedObjectInterface.RelatedObjectType.values()[owner.getOwnerType()]);
				related.setUniqueId(owner.getOwnerId());
				related.setName(owner.getLabel());
				related.addCourseId(owner.getCourse().getUniqueId());
				related.addCourseName(owner.getCourse().getCourseName());
				related.addCourseTitle(owner.getCourse().getTitle() == null ? "" : owner.getCourse().getTitle());
				if (owner.getOwnerType() != ExamOwner.sOwnerTypeClass && context != null && context.hasPermission(owner.getCourse().getInstructionalOffering(), Right.InstructionalOfferingDetail))
					related.setDetailPage("instructionalOfferingDetail.do?io=" + owner.getCourse().getInstructionalOffering().getUniqueId());
				if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass) {
					Class_ clazz = (Class_)owner.getOwnerObject();
					related.setSectionNumber(clazz.getSectionNumberString(hibSession));
		    		if (clazz.getClassSuffix() != null)
		    			related.addExternalId(clazz.getClassSuffix());
		    		related.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
		    		if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() != null)
		    			related.setInstruction(related.getInstruction() + " (" + clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
		    		related.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
						if (!course.isIsControl()) {
							related.addCourseId(course.getUniqueId());
							related.addCourseName(course.getCourseName());
							related.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
							if (clazz.getClassSuffix(course) != null)
				    			related.addExternalId(clazz.getClassSuffix(course));
						}
		    		}
		    		Assignment assignment = clazz.getCommittedAssignment();
		    		if (assignment != null) {
		    			TimeLocation time = assignment.getTimeLocation();
		    			if (time != null) {
		    				related.setTime(time.getDayHeader() + " " + time.getStartTimeHeader(CONSTANTS.useAmPm()) + " - " + time.getEndTimeHeader(CONSTANTS.useAmPm()));
		    				related.setDate(time.getDatePatternName());
		    			}
		    			for (Location r: assignment.getRooms()) {
		    				ResourceInterface location = new ResourceInterface();
		    				location.setType(ResourceType.ROOM);
		    				location.setId(r.getUniqueId());
		    				location.setName(r.getLabel());
		    				location.setSize(r.getCapacity());
		    				location.setRoomType(r.getRoomTypeLabel());
		    				location.setBreakTime(r.getEffectiveBreakTime());
		    				location.setMessage(r.getEventMessage());
		    				location.setIgnoreRoomCheck(r.isIgnoreRoomCheck());
		    				location.setDisplayName(r.getDisplayName());
		    				related.addLocation(location);
		    			}
		    		}
					if (context != null && context.hasPermission(clazz, Right.ClassDetail))
						related.setDetailPage("classDetail.do?cid=" + clazz.getUniqueId());
					if (clazz.getDisplayInstructor()) {
		    			for (ClassInstructor i: clazz.getClassInstructors()) {
		    				if (i.getResponsibility() != null && i.getResponsibility().hasOption(TeachingResponsibility.Option.noevents)) continue;
							ContactInterface instructor = new ContactInterface();
							instructor.setFirstName(i.getInstructor().getFirstName());
							instructor.setMiddleName(i.getInstructor().getMiddleName());
							instructor.setLastName(i.getInstructor().getLastName());
							instructor.setAcademicTitle(i.getInstructor().getAcademicTitle());
							instructor.setExternalId(i.getInstructor().getExternalUniqueId());
							instructor.setEmail(i.getInstructor().getEmail());
							instructor.setFormattedName(i.getInstructor().getName(nameFormat));
							if (i.getResponsibility() != null) {
								instructor.setResponsibility(i.getResponsibility().getLabel());
								instructor.setResponsibilityAbbreviation(i.getResponsibility().getAbbreviation());
		    				}
							related.addInstructor(instructor);
		    			}
		    		}
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeOffering) {
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
		    			if (!course.isIsControl()) {
							related.addCourseId(course.getUniqueId());
							related.addCourseName(course.getCourseName());
							related.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
		    			}
		    		}
					related.setInstruction(MESSAGES.colOffering());
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeConfig) {
					InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
					related.setSectionNumber(config.getName());
					related.setInstruction(MESSAGES.colConfig());
					if (config.getInstructionalMethod() != null)
		    			related.setInstruction(related.getInstruction() + " (" + config.getInstructionalMethod().getLabel() + ")");
				} else {
					related.setInstruction(MESSAGES.colCourse());
				}
				event.addRelatedObject(related);
	    		for (OfferingCoordinator oc: owner.getCourse().getInstructionalOffering().getOfferingCoordinators()) {
	    			if (oc.getResponsibility() != null && oc.getResponsibility().hasOption(TeachingResponsibility.Option.noevents)) continue;
	    			DepartmentalInstructor c = oc.getInstructor();
	    			if (addedInstructorIds.add(c.getUniqueId())) {
		    			ContactInterface coordinator = new ContactInterface();
						coordinator.setFirstName(c.getFirstName());
						coordinator.setMiddleName(c.getMiddleName());
						coordinator.setLastName(c.getLastName());
						coordinator.setAcademicTitle(c.getAcademicTitle());
						coordinator.setEmail(c.getEmail());
						coordinator.setFormattedName(c.getName(nameFormat));
						if (oc.getResponsibility() != null) {
	    					coordinator.setResponsibility(oc.getResponsibility().getLabel());
	    					coordinator.setResponsibilityAbbreviation(oc.getResponsibility().getAbbreviation());
	    				}
						event.addCoordinator(coordinator);
		    		}
				}
			}
    	} else if (Event.sEventTypeCourse == e.getEventType()) {
    		CourseEvent ce = (e instanceof CourseEvent ? (CourseEvent)e : CourseEventDAO.getInstance().get(e.getUniqueId(), hibSession));
    		
    		event.setRequiredAttendance(ce.isReqAttendance());
    		int enrl = 0, limit = 0;
    		Set<Long> addedInstructorIds = new HashSet<Long>();
			for (RelatedCourseInfo owner: new TreeSet<RelatedCourseInfo>(ce.getRelatedCourses())) {
				RelatedObjectInterface related = new RelatedObjectInterface();
				related.setType(RelatedObjectInterface.RelatedObjectType.values()[owner.getOwnerType()]);
				related.setUniqueId(owner.getOwnerId());
				related.addCourseId(owner.getCourse().getUniqueId());
				related.addCourseName(owner.getCourse().getCourseName());
				related.addCourseTitle(owner.getCourse().getTitle() == null ? "" : owner.getCourse().getTitle());
				related.setName(owner.getLabel());
				related.setSelection(new long[] { owner.getCourse().getSubjectArea().getUniqueId(), owner.getCourse().getUniqueId()});
				if (owner.getOwnerType() != ExamOwner.sOwnerTypeClass && context != null && context.hasPermission(owner.getCourse().getInstructionalOffering(), Right.InstructionalOfferingDetail))
					related.setDetailPage("instructionalOfferingDetail.do?io=" + owner.getCourse().getInstructionalOffering().getUniqueId());
				if (owner.getOwnerType() == ExamOwner.sOwnerTypeClass) {
					Class_ clazz = (Class_)owner.getOwnerObject();
					related.setSelection(new long[] { owner.getCourse().getSubjectArea().getUniqueId(), owner.getCourse().getUniqueId(), clazz.getSchedulingSubpart().getUniqueId(), clazz.getUniqueId()});
					related.setSectionNumber(clazz.getSectionNumberString(hibSession));
		    		if (clazz.getClassSuffix() != null)
		    			related.addExternalId(clazz.getClassSuffix());
		    		related.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
		    		if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() != null)
		    			related.setInstruction(related.getInstruction() + " (" + clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
		    		related.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
						if (!course.isIsControl()) {
							related.addCourseId(course.getUniqueId());
							related.addCourseName(course.getCourseName());
							related.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
							if (clazz.getClassSuffix(course) != null)
				    			related.addExternalId(clazz.getClassSuffix(course));
						}
		    		}
		    		Assignment assignment = clazz.getCommittedAssignment();
		    		if (assignment != null) {
		    			TimeLocation time = assignment.getTimeLocation();
		    			if (time != null) {
		    				related.setTime(time.getDayHeader() + " " + time.getStartTimeHeader(CONSTANTS.useAmPm()) + " - " + time.getEndTimeHeader(CONSTANTS.useAmPm()));
		    				related.setDate(time.getDatePatternName());
		    			}
		    			for (Location r: assignment.getRooms()) {
		    				ResourceInterface location = new ResourceInterface();
		    				location.setType(ResourceType.ROOM);
		    				location.setId(r.getUniqueId());
		    				location.setName(r.getLabel());
		    				location.setSize(r.getCapacity());
		    				location.setRoomType(r.getRoomTypeLabel());
		    				location.setBreakTime(r.getEffectiveBreakTime());
		    				location.setMessage(r.getEventMessage());
		    				location.setIgnoreRoomCheck(r.isIgnoreRoomCheck());
		    				location.setDisplayName(r.getDisplayName());
		    				related.addLocation(location);
		    			}
		    		}
					if (context != null && context.hasPermission(clazz, Right.ClassDetail))
						related.setDetailPage("classDetail.do?cid=" + clazz.getUniqueId());
					if (clazz.getDisplayInstructor()) {
		    			for (ClassInstructor i: clazz.getClassInstructors()) {
		    				if (i.getResponsibility() != null && i.getResponsibility().hasOption(TeachingResponsibility.Option.noevents)) continue;
							ContactInterface instructor = new ContactInterface();
							instructor.setFirstName(i.getInstructor().getFirstName());
							instructor.setMiddleName(i.getInstructor().getMiddleName());
							instructor.setLastName(i.getInstructor().getLastName());
							instructor.setAcademicTitle(i.getInstructor().getAcademicTitle());
							instructor.setExternalId(i.getInstructor().getExternalUniqueId());
							instructor.setEmail(i.getInstructor().getEmail());
							instructor.setFormattedName(i.getInstructor().getName(nameFormat));
							if (i.getResponsibility() != null) {
								instructor.setResponsibility(i.getResponsibility().getLabel());
								instructor.setResponsibilityAbbreviation(i.getResponsibility().getAbbreviation());
		    				}
							related.addInstructor(instructor);
		    			}
		    		}
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeOffering) {
		    		for (CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
		    			if (!course.isIsControl()) {
							related.addCourseId(course.getUniqueId());
							related.addCourseName(course.getCourseName());
							related.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
		    			}
		    		}
		    		related.setInstruction(MESSAGES.colOffering());
				} else if (owner.getOwnerType() == ExamOwner.sOwnerTypeConfig) {
					InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
					related.setSelection(new long[] { owner.getCourse().getSubjectArea().getUniqueId(), owner.getCourse().getUniqueId(), config.getUniqueId()});
					related.setSectionNumber(config.getName());
					related.setInstruction(MESSAGES.colConfig());
					if (config.getInstructionalMethod() != null)
		    			related.setInstruction(related.getInstruction() + " (" + config.getInstructionalMethod().getLabel() + ")");
				} else {
					related.setInstruction(MESSAGES.colCourse());
				}
				event.addRelatedObject(related);
				enrl += owner.countStudents();
				limit += owner.getLimit();
	    		for (OfferingCoordinator oc: owner.getCourse().getInstructionalOffering().getOfferingCoordinators()) {
	    			if (oc.getResponsibility() != null && oc.getResponsibility().hasOption(TeachingResponsibility.Option.noevents)) continue;
	    			DepartmentalInstructor c = oc.getInstructor();
	    			if (addedInstructorIds.add(c.getUniqueId())) {
		    			ContactInterface coordinator = new ContactInterface();
						coordinator.setFirstName(c.getFirstName());
						coordinator.setMiddleName(c.getMiddleName());
						coordinator.setLastName(c.getLastName());
						coordinator.setAcademicTitle(c.getAcademicTitle());
						coordinator.setEmail(c.getEmail());
						coordinator.setFormattedName(c.getName(nameFormat));
						if (oc.getResponsibility() != null) {
	    					coordinator.setResponsibility(oc.getResponsibility().getLabel());
	    					coordinator.setResponsibilityAbbreviation(oc.getResponsibility().getAbbreviation());
	    				}
						event.addCoordinator(coordinator);
		    		}
				}
			}
			event.setEnrollment(enrl);
			event.setMaxCapacity(limit);
    	}
    	
    	// overlaps
    	Map<Long, Set<Meeting>> overlaps = new HashMap<Long, Set<Meeting>>();
    	if (e.getUniqueId() != null)
    		for (Object[] o: (List<Object[]>)EventDAO.getInstance().getSession().createQuery(
    				"select m.uniqueId, o from Event e inner join e.meetings m, Meeting o " +
    				"where e.uniqueId = :eventId and m.uniqueId != o.uniqueId and " +
    				"o.startPeriod < m.stopPeriod and o.stopPeriod > m.startPeriod and m.approvalStatus <= 1 and o.approvalStatus <= 1 and " +
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
    		
    	Hashtable<Long, EventInterface> conflictingEvents = new Hashtable<Long, EventInterface>();
    	Set<Location> unavailableLocations = new HashSet<Location>();
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
			meeting.setPast(context != null && context.isPastOrOutside(m.getStartTime()));
			meeting.setApprovalDate(m.getApprovalDate());
			meeting.setApprovalStatus(m.getApprovalStatus());
			meeting.setCanEdit(context != null && context.hasPermission(m, Right.EventMeetingEdit));
			meeting.setCanInquire(context != null && (context.hasPermission(m, Right.EventMeetingInquire) ||
					(m.getEvent().getEventType() == Event.sEventTypeClass && context.hasPermission(m, Right.EventMeetingInquireClass)) ||
					(m.getEvent().getEventType() == Event.sEventTypeFinalExam && context.hasPermission(m, Right.EventMeetingInquireExam)) ||
					(m.getEvent().getEventType() == Event.sEventTypeMidtermExam && context.hasPermission(m, Right.EventMeetingInquireExam))
					));
			meeting.setCanApprove(context != null && context.hasPermission(m, Right.EventMeetingApprove));
			meeting.setCanDelete(context != null && context.hasPermission(m, Right.EventMeetingDelete));
			meeting.setCanCancel(context != null && (context.hasPermission(m, Right.EventMeetingCancel) ||
					(m.getEvent().getEventType() == Event.sEventTypeClass && context.hasPermission(m, Right.EventMeetingCancelClass)) ||
					(m.getEvent().getEventType() == Event.sEventTypeFinalExam && context.hasPermission(m, Right.EventMeetingCancelExam)) ||
					(m.getEvent().getEventType() == Event.sEventTypeMidtermExam && context.hasPermission(m, Right.EventMeetingCancelExam))
					));
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
				location.setDisplayName(m.getLocation().getDisplayName());
				meeting.setLocation(location);
				if ((e instanceof SpecialEvent || e instanceof CourseEvent) && (meeting.getApprovalStatus() == ApprovalStatus.Approved || meeting.getApprovalStatus() == ApprovalStatus.Pending)) {
					if (m.getLocation().getEventDepartment() != null && m.getLocation().getEventDepartment().isAllowEvents()) {
						String message = m.getLocation().getEventMessage();
						if (message != null && !message.isEmpty()) {
							MeetingConflictInterface conflict = new MeetingConflictInterface();
							conflict.setName(message);
							conflict.setType(EventInterface.EventType.Message);
							conflict.setMeetingDate(meeting.getMeetingDate());
							conflict.setDayOfYear(meeting.getDayOfYear());
							conflict.setStartOffset(0);
							conflict.setEndOffset(0);
							conflict.setStartSlot(0);
							conflict.setEndSlot(288);
							meeting.addConflict(conflict);
						}
					} else {
						MeetingConflictInterface conflict = new MeetingConflictInterface();
						conflict.setName(MESSAGES.conflictNotEventRoom(location.getName()));
						conflict.setType(EventInterface.EventType.Message);
						conflict.setMeetingDate(meeting.getMeetingDate());
						conflict.setDayOfYear(meeting.getDayOfYear());
						conflict.setStartOffset(0);
						conflict.setEndOffset(0);
						conflict.setStartSlot(0);
						conflict.setEndSlot(288);
						meeting.addConflict(conflict);
					}
				}
			}
			Set<Meeting> overlapsThisMeeting = overlaps.get(m.getUniqueId());
			if (overlapsThisMeeting != null && m.getLocation() != null && !m.getLocation().isIgnoreRoomCheck()) {
				for (Meeting overlap: overlapsThisMeeting) {
					MeetingConflictInterface conflict = new MeetingConflictInterface();

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
					conflict.setApprovalDate(overlap.getApprovalDate());
					conflict.setApprovalStatus(overlap.getApprovalStatus());
					conflict.setLocation(meeting.getLocation());
					
					EventInterface confEvent = conflictingEvents.get(overlap.getEvent().getUniqueId());
					if (confEvent == null) {
						confEvent = new EventInterface();
						confEvent.setId(overlap.getEvent().getUniqueId());
						confEvent.setName(overlap.getEvent().getEventName());
						confEvent.setType(EventInterface.EventType.values()[overlap.getEvent().getEventType()]);
						conflictingEvents.put(overlap.getEvent().getUniqueId(), confEvent);
						confEvent.setCanView(context != null && context.hasPermission(overlap.getEvent(), Right.EventDetail));
						confEvent.setMaxCapacity(overlap.getEvent().getMaxCapacity());
						if (overlap.getEvent().getMainContact() != null) {
							ContactInterface contact = new ContactInterface();
							contact.setFirstName(overlap.getEvent().getMainContact().getFirstName());
							contact.setMiddleName(overlap.getEvent().getMainContact().getMiddleName());
							contact.setLastName(overlap.getEvent().getMainContact().getLastName());
							contact.setAcademicTitle(overlap.getEvent().getMainContact().getAcademicTitle());
							contact.setFormattedName(overlap.getEvent().getMainContact().getName(nameFormat));
							confEvent.setContact(contact);
						}
						if (overlap.getEvent().getSponsoringOrganization() != null) {
							SponsoringOrganizationInterface sponsor = new SponsoringOrganizationInterface();
							sponsor.setEmail(overlap.getEvent().getSponsoringOrganization().getEmail());
							sponsor.setName(overlap.getEvent().getSponsoringOrganization().getName());
							sponsor.setUniqueId(overlap.getEvent().getSponsoringOrganization().getUniqueId());
							confEvent.setSponsor(sponsor);
						}
				    	if (Event.sEventTypeClass == overlap.getEvent().getEventType()) {
				    		ClassEvent ce = ClassEventDAO.getInstance().get(overlap.getEvent().getUniqueId(), hibSession);
				    		Class_ clazz = ce.getClazz();
							confEvent.setEnrollment(clazz.getEnrollment());
							confEvent.setMaxCapacity(clazz.getClassLimit());
				    		if (clazz.getDisplayInstructor()) {
				    			for (ClassInstructor i: clazz.getClassInstructors()) {
				    				if (i.getResponsibility() != null && i.getResponsibility().hasOption(TeachingResponsibility.Option.noevents)) continue;
									ContactInterface instructor = new ContactInterface();
									instructor.setFirstName(i.getInstructor().getFirstName());
									instructor.setMiddleName(i.getInstructor().getMiddleName());
									instructor.setLastName(i.getInstructor().getLastName());
									instructor.setAcademicTitle(i.getInstructor().getAcademicTitle());
									instructor.setEmail(i.getInstructor().getEmail());
									instructor.setFormattedName(i.getInstructor().getName(nameFormat));
									if (i.getResponsibility() != null) {
										instructor.setResponsibility(i.getResponsibility().getLabel());
										instructor.setResponsibilityAbbreviation(i.getResponsibility().getAbbreviation());
				    				}
									confEvent.addInstructor(instructor);
				    			}
				    		}
				    		CourseOffering correctedOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
				    		List<CourseOffering> courses = new ArrayList<CourseOffering>(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings());
				    		confEvent.addCourseId(correctedOffering.getUniqueId());
				    		confEvent.addCourseName(correctedOffering.getCourseName());
				    		confEvent.addCourseTitle(correctedOffering.getTitle() == null ? "" : correctedOffering.getTitle());
				    		confEvent.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc().length() <= 20 ? clazz.getSchedulingSubpart().getItype().getDesc() : clazz.getSchedulingSubpart().getItype().getAbbv());
				    		if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() != null)
				    			event.setInstruction(event.getInstruction() + " (" + clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
				    		confEvent.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
				    		confEvent.setSectionNumber(clazz.getSectionNumberString(hibSession));
				    		if (clazz.getClassSuffix(correctedOffering) == null) {
					    		confEvent.setName(clazz.getClassLabel(correctedOffering));
				    		} else {
					    		confEvent.addExternalId(clazz.getClassSuffix(correctedOffering));
				    			confEvent.setName(correctedOffering.getCourseName() + " " + clazz.getClassSuffix(correctedOffering));
				    		}
			    			for (CourseOffering co: courses) {
			    				confEvent.addCourseId(co.getUniqueId());
					    		confEvent.addCourseName(co.getCourseName());
					    		confEvent.addCourseTitle(co.getTitle() == null ? "" : co.getTitle());
					    		if (clazz.getClassSuffix(co) != null)
					    			confEvent.addExternalId(clazz.getClassSuffix(co));
			    			}
				    	} else if (Event.sEventTypeFinalExam == overlap.getEvent().getEventType() || Event.sEventTypeMidtermExam == overlap.getEvent().getEventType()) {
				    		ExamEvent xe = ExamEventDAO.getInstance().get(overlap.getEvent().getUniqueId(), hibSession);
				    		confEvent.setEnrollment(xe.getExam().countStudents());
				    		confEvent.setMaxCapacity(xe.getExam().getSize());
			    			for (DepartmentalInstructor i: xe.getExam().getInstructors()) {
								ContactInterface instructor = new ContactInterface();
								instructor.setFirstName(i.getFirstName());
								instructor.setMiddleName(i.getMiddleName());
								instructor.setLastName(i.getLastName());
								instructor.setAcademicTitle(i.getAcademicTitle());
								instructor.setEmail(i.getEmail());
								instructor.setFormattedName(i.getName(nameFormat));
								confEvent.addInstructor(instructor);
			    			}
			    			for (ExamOwner owner: new TreeSet<ExamOwner>(xe.getExam().getOwners())) {
			    				for(CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
						    		String courseName = owner.getCourse().getCourseName();
				    				String label = owner.getLabel();
				    				if (label.startsWith(courseName)) {
				    					label = label.substring(courseName.length());
				    				}
				    				confEvent.addCourseId(course.getUniqueId());
				    				confEvent.addCourseName(course.getCourseName());
				    				confEvent.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
				    				confEvent.addExternalId(label.trim());
			    				}
			    			}
				    	} else if (Event.sEventTypeCourse == overlap.getEvent().getEventType()) {
				    		CourseEvent ce = CourseEventDAO.getInstance().get(overlap.getEvent().getUniqueId(), hibSession);
				    		confEvent.setRequiredAttendance(ce.isReqAttendance());
							int enrl = 0, limit = 0;
							for (RelatedCourseInfo owner: ce.getRelatedCourses()) {
								enrl += owner.countStudents();
								limit += owner.getLimit();
								for(CourseOffering course: owner.getCourse().getInstructionalOffering().getCourseOfferings()) {
				    				String courseName = owner.getCourse().getCourseName();
				    				String label = owner.getLabel();
				    				if (label.startsWith(courseName)) {
				    					label = label.substring(courseName.length());
				    				}
				    				confEvent.addCourseId(course.getUniqueId());
				    				confEvent.addCourseName(course.getCourseName());
				    				confEvent.addCourseTitle(course.getTitle() == null ? "" : course.getTitle());
				    				confEvent.addExternalId(label.trim());
			    				}
							}
							confEvent.setEnrollment(enrl);
							confEvent.setMaxCapacity(limit);
				    	}
					}
					
					if (context.hasPermission(Right.EventCanViewMeetingContacts)) {
						Map<Long, ContactInterface> contacts = new HashMap<Long, ContactInterface>();
						for (EventContact c: overlap.getMeetingContacts()) {
							ContactInterface contact = contacts.get(c.getUniqueId());
							if (contact == null) {
								contact = new ContactInterface();
								contact.setFirstName(c.getFirstName());
								contact.setMiddleName(c.getMiddleName());
								contact.setLastName(c.getLastName());
								contact.setAcademicTitle(c.getAcademicTitle());
								contact.setExternalId(c.getExternalUniqueId());
								contact.setPhone(c.getPhone());
								contact.setEmail(c.getEmailAddress());
								contact.setFormattedName(c.getName(nameFormat));
								contacts.put(c.getUniqueId(), contact);
							}
							conflict.addMeetingContact(contact);
						}
					}
					confEvent.addMeeting(conflict);
					
					meeting.addConflict(conflict);
				}
			}
			
			if (m.getLocation() != null && m.getLocation().getEventAvailability() != null && m.getLocation().getEventAvailability().length() == Constants.SLOTS_PER_DAY * Constants.DAY_CODES.length) {
				check: for (int slot = meeting.getStartSlot(); slot < meeting.getEndSlot(); slot++) {
					if (m.getLocation().getEventAvailability().charAt(meeting.getDayOfWeek() * Constants.SLOTS_PER_DAY + slot) == '1') {
						unavailableLocations.add(m.getLocation());
						break check;
					}
				}
			}
			if (context.hasPermission(Right.EventCanViewMeetingContacts)) {
				Map<Long, ContactInterface> contacts = new HashMap<Long, ContactInterface>();
				for (EventContact c: m.getMeetingContacts()) {
					ContactInterface contact = contacts.get(c.getUniqueId());
					if (contact == null) {
						contact = new ContactInterface();
						contact.setFirstName(c.getFirstName());
						contact.setMiddleName(c.getMiddleName());
						contact.setLastName(c.getLastName());
						contact.setAcademicTitle(c.getAcademicTitle());
						contact.setExternalId(c.getExternalUniqueId());
						contact.setPhone(c.getPhone());
						contact.setEmail(c.getEmailAddress());
						contact.setFormattedName(c.getName(nameFormat));
						contacts.put(c.getUniqueId(), contact);
					}
					meeting.addMeetingContact(contact);
				}
			}
			
			event.addMeeting(meeting);
		}
    	
		for (EventInterface confEvent: conflictingEvents.values())
			event.addConflict(confEvent);
		
		for (Location location: unavailableLocations) {
			Set<MeetingInterface> unavailabilities = EventLookupBackend.generateUnavailabilityMeetings(location, true);
			if (unavailabilities != null && !unavailabilities.isEmpty()) {
				for (MeetingInterface meeting: event.getMeetings())
					for (MeetingInterface conf: unavailabilities)
						if (meeting.inConflict(conf)) meeting.addConflict((MeetingConflictInterface)conf);
				EventInterface unavailability = new EventInterface();
				unavailability.setId(-location.getUniqueId());
				unavailability.setName(MESSAGES.unavailableEventDefaultName());
				unavailability.setType(EventInterface.EventType.Unavailabile);
				for (MeetingInterface m: unavailabilities)
					if (event.inConflict(m))
						unavailability.addMeeting(m);
				event.addConflict(unavailability);
			}
		}
    	
    	for (EventNote n: e.getNotes()) {
    		NoteInterface note = new NoteInterface();
    		note.setId(n.getUniqueId());
    		note.setDate(n.getTimeStamp());
    		note.setType(NoteInterface.NoteType.values()[n.getNoteType()]);
    		note.setMeetings(n.getMeetingsHtml());
    		note.setNote(n.getTextNote() == null ? null : n.getTextNote().replace("\n", "<br>"));
    		note.setUser(n.getUser());
    		note.setAttachment(n.getAttachedName());
    		note.setLink(n.getAttachedName() == null ? null : QueryEncoderBackend.encode("event=" + e.getUniqueId() + "&note=" + n.getUniqueId()));
    		event.addNote(note);
    	}
    	
    	return event;
	}
	
	public static EventInterface getArrangeHoursClassDetail(Class_ clazz, EventContext context) throws GwtRpcException {
		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		EventInterface event = new EventInterface();
		event.setId(-clazz.getUniqueId());
		event.setName(clazz.getClassLabel(hibSession));
		event.setType(EventInterface.EventType.Class);
		event.setCanView(true);
		event.setMaxCapacity(clazz.getClassLimit());
		event.setEnrollment(clazz.getEnrollment());
		
		String nameFormat = (context != null ? context.getUser().getProperty(UserProperty.NameFormat) : NameFormat.LAST_FIRST_MIDDLE.reference());
		Set<Long> addedInstructorIds = new HashSet<Long>();
		if (clazz.getDisplayInstructor()) {
			for (ClassInstructor i: clazz.getClassInstructors()) {
				if (i.getResponsibility() != null && i.getResponsibility().hasOption(TeachingResponsibility.Option.noevents)) continue;
				ContactInterface instructor = new ContactInterface();
				instructor.setFirstName(i.getInstructor().getFirstName());
				instructor.setMiddleName(i.getInstructor().getMiddleName());
				instructor.setLastName(i.getInstructor().getLastName());
				instructor.setAcademicTitle(i.getInstructor().getAcademicTitle());
				instructor.setEmail(i.getInstructor().getEmail());
				instructor.setFormattedName(i.getInstructor().getName(nameFormat));
				if (i.getResponsibility() != null) {
					instructor.setResponsibility(i.getResponsibility().getLabel());
					instructor.setResponsibilityAbbreviation(i.getResponsibility().getAbbreviation());
				}
				event.addInstructor(instructor);
				addedInstructorIds.add(i.getInstructor().getUniqueId());
			}
		}
		for (OfferingCoordinator oc: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getOfferingCoordinators()) {
			if (oc.getResponsibility() != null && oc.getResponsibility().hasOption(TeachingResponsibility.Option.noevents)) continue;
			DepartmentalInstructor c = oc.getInstructor();
			if (addedInstructorIds.add(c.getUniqueId())) {
    			ContactInterface coordinator = new ContactInterface();
				coordinator.setFirstName(c.getFirstName());
				coordinator.setMiddleName(c.getMiddleName());
				coordinator.setLastName(c.getLastName());
				coordinator.setAcademicTitle(c.getAcademicTitle());
				coordinator.setEmail(c.getEmail());
				coordinator.setFormattedName(c.getName(nameFormat));
				if (oc.getResponsibility() != null) {
					coordinator.setResponsibility(oc.getResponsibility().getLabel());
					coordinator.setResponsibilityAbbreviation(oc.getResponsibility().getAbbreviation());
				}
				event.addCoordinator(coordinator);
			}
		}
		CourseOffering correctedOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
		List<CourseOffering> courses = new ArrayList<CourseOffering>(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings());
		event.addCourseId(correctedOffering.getUniqueId());
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
			event.addCourseId(co.getUniqueId());
    		event.addCourseName(co.getCourseName());
    		event.addCourseTitle(co.getTitle() == null ? "" : co.getTitle());
    		if (clazz.getClassSuffix(co) != null)
    			event.addExternalId(clazz.getClassSuffix(co));
		}
		DatePattern pattern = clazz.effectiveDatePattern();
		if (pattern != null) {
			String datePatternFormat = ApplicationProperty.DatePatternFormatUseDates.value();
	    	if ("never".equals(datePatternFormat)) event.setMessage(pattern.getName());
	    	else if ("extended".equals(datePatternFormat) && pattern.getType() != DatePattern.sTypeExtended) event.setMessage(pattern.getName());
	    	else if ("alternate".equals(datePatternFormat) && pattern.getType() == DatePattern.sTypeAlternate) event.setMessage(pattern.getName());
	    	else {
	    		Date first = pattern.getStartDate();
	    		Date last = pattern.getEndDate();
	    		event.setMessage((first.equals(last) ? Formats.getDateFormat(Formats.Pattern.DATE_EVENT_LONG).format(first) : Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT).format(first) + " - " + Formats.getDateFormat(Formats.Pattern.DATE_EVENT_LONG).format(last)));
	    	}
		}
		List<ResourceInterface> locations = new ArrayList<ResourceInterface>();
		for (RoomPref rp: (Set<RoomPref>)clazz.effectivePreferences(RoomPref.class)) {
			if (!PreferenceLevel.sRequired.equals(rp.getPrefLevel().getPrefProlog())) continue;
			MeetingInterface meeting = new MeetingInterface();
			meeting.setPast(true);
			ResourceInterface location = new ResourceInterface();
			location.setType(ResourceType.ROOM);
			location.setId(rp.getRoom().getUniqueId());
			location.setName(rp.getRoom().getLabel());
			location.setSize(rp.getRoom().getCapacity());
			location.setRoomType(rp.getRoom().getRoomTypeLabel());
			location.setBreakTime(rp.getRoom().getEffectiveBreakTime());
			location.setMessage(rp.getRoom().getEventMessage());
			location.setIgnoreRoomCheck(rp.getRoom().isIgnoreRoomCheck());
			location.setDisplayName(rp.getRoom().getDisplayName());
			meeting.setLocation(location);
			event.addMeeting(meeting);
			locations.add(location);
		}
		if (!event.hasMeetings()) {
			MeetingInterface meeting = new MeetingInterface();
			meeting.setPast(true);
			event.addMeeting(meeting);
		}

		RelatedObjectInterface related = new RelatedObjectInterface();
		related.setType(RelatedObjectInterface.RelatedObjectType.Class);
		related.setUniqueId(clazz.getUniqueId());
		related.setName(clazz.getClassLabel(hibSession));
		String note = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getScheduleBookNote();
		if (clazz.getSchedulePrintNote() != null && !clazz.getSchedulePrintNote().isEmpty())
			note = (note == null || note.isEmpty() ? "" : note + "\n") + clazz.getSchedulePrintNote();
		related.setNote(note);
		if (context != null && context.hasPermission(clazz, Right.ClassDetail))
			related.setDetailPage("classDetail.do?cid=" + clazz.getUniqueId());

		CourseOffering courseOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
		related.addCourseId(courseOffering.getUniqueId());
		related.addCourseName(courseOffering.getCourseName());
		related.addCourseTitle(courseOffering.getTitle() == null ? "" : courseOffering.getTitle());
		related.setSectionNumber(clazz.getSectionNumberString(hibSession));
		if (clazz.getClassSuffix() != null)
			related.addExternalId(clazz.getClassSuffix());
		related.setInstruction(clazz.getSchedulingSubpart().getItype().getDesc());
		if (clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() != null)
			related.setInstruction(related.getInstruction() + " (" + clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
		related.setInstructionType(clazz.getSchedulingSubpart().getItype().getItype());
		for (CourseOffering co: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
			if (!co.isIsControl()) {
				related.addCourseId(co.getUniqueId());
				related.addCourseName(co.getCourseName());
				related.addCourseTitle(co.getTitle() == null ? "" : co.getTitle());
				if (clazz.getClassSuffix(co) != null)
	    			related.addExternalId(clazz.getClassSuffix(co));
			}
		}
		if (clazz.getDisplayInstructor()) {
			for (ClassInstructor i: clazz.getClassInstructors()) {
				if (i.getResponsibility() != null && i.getResponsibility().hasOption(TeachingResponsibility.Option.noevents)) continue;
				ContactInterface instructor = new ContactInterface();
				instructor.setFirstName(i.getInstructor().getFirstName());
				instructor.setMiddleName(i.getInstructor().getMiddleName());
				instructor.setLastName(i.getInstructor().getLastName());
				instructor.setAcademicTitle(i.getInstructor().getAcademicTitle());
				instructor.setExternalId(i.getInstructor().getExternalUniqueId());
				instructor.setEmail(i.getInstructor().getEmail());
				instructor.setFormattedName(i.getInstructor().getName(nameFormat));
				if (i.getResponsibility() != null) {
					instructor.setResponsibility(i.getResponsibility().getLabel());
					instructor.setResponsibilityAbbreviation(i.getResponsibility().getAbbreviation());
				}
				related.addInstructor(instructor);
			}
		}
		for (ResourceInterface location: locations)
			related.addLocation(location);
		if (pattern != null) related.setDate(pattern.getName());
		related.setTime(CONSTANTS.arrangeHours());
		event.addRelatedObject(related);
		
		return event;
	}
}
