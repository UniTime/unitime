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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Query;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConflictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(EventRoomAvailabilityRpcRequest.class)
public class EventRoomAvailabilityBackend extends EventAction<EventRoomAvailabilityRpcRequest, EventRoomAvailabilityRpcResponse> {
	
	@Override
	public EventRoomAvailabilityRpcResponse execute(EventRoomAvailabilityRpcRequest request, EventContext context) {
		EventRoomAvailabilityRpcResponse response = new EventRoomAvailabilityRpcResponse();
		
		Session session = SessionDAO.getInstance().get(request.getSessionId());
		
		if (request.hasDates() && request.hasLocations()) {
			for (int idx = 0; idx < request.getLocations().size(); idx += 1000) {
			
				String dates = "";
				for (int i = 0; i < request.getDates().size(); i++)
					dates += (dates.isEmpty() ? "" : ",") + ":d" + i;
				
				String locations = "";
				for (int i = idx; i + idx < request.getLocations().size() && i < 1000; i++)
					locations += (locations.isEmpty() ? "" : ",") + ":l" + i;
				
				Query query = EventDAO.getInstance().getSession().createQuery(
						"select m from Meeting m " +
						"where m.startPeriod<:stopTime and m.stopPeriod>:startTime and m.approvalStatus <= 1 and " +
						"m.locationPermanentId in (" + locations + ") and m.meetingDate in ("+dates+")");
				
				query.setInteger("startTime", request.getStartSlot());
				query.setInteger("stopTime", request.getEndSlot());
				for (int i = 0; i < request.getDates().size(); i++) {
					Date date = CalendarUtils.dateOfYear2date(session.getSessionStartYear(), request.getDates().get(i));
					query.setDate("d" + i, date);
				}
				for (int i = idx; i + idx < request.getLocations().size() && i < 1000; i++)
					query.setLong("l" + i, request.getLocations().get(idx + i));
				
				for (Meeting m: (List<Meeting>)query.list()) {
					MeetingConflictInterface conflict = new MeetingConflictInterface();

					if (request.hasEventId() && m.getEvent().getUniqueId().equals(request.getEventId())) continue;

					conflict.setEventId(m.getEvent().getUniqueId());
					conflict.setName(m.getEvent().getEventName());
					conflict.setType(EventInterface.EventType.values()[m.getEvent().getEventType()]);
					conflict.setLimit(m.getEvent().getMaxCapacity());
					
					if (m.getEvent().getSponsoringOrganization() != null) {
						SponsoringOrganizationInterface sponsor = new SponsoringOrganizationInterface();
						sponsor.setEmail(m.getEvent().getSponsoringOrganization().getEmail());
						sponsor.setName(m.getEvent().getSponsoringOrganization().getName());
						sponsor.setUniqueId(m.getEvent().getSponsoringOrganization().getUniqueId());
						conflict.setSponsor(sponsor);
					}
					
					if (Event.sEventTypeClass == m.getEvent().getEventType()) {
			    		ClassEvent ce = (m.getEvent() instanceof ClassEvent ? (ClassEvent)m.getEvent() : ClassEventDAO.getInstance().get(m.getEvent().getUniqueId()));
			    		Class_ clazz = ce.getClazz();
			    		conflict.setEnrollment(clazz.getEnrollment());
			    		if (clazz.getDisplayInstructor()) {
			    			for (ClassInstructor i: clazz.getClassInstructors()) {
								ContactInterface instructor = new ContactInterface();
								instructor.setFirstName(i.getInstructor().getFirstName());
								instructor.setMiddleName(i.getInstructor().getMiddleName());
								instructor.setLastName(i.getInstructor().getLastName());
								instructor.setEmail(i.getInstructor().getEmail());
								conflict.addInstructor(instructor);
			    			}
			    		}
					} else if (Event.sEventTypeFinalExam == m.getEvent().getEventType() || Event.sEventTypeMidtermExam == m.getEvent().getEventType()) {
			    		ExamEvent xe = (m.getEvent() instanceof ExamEvent ? (ExamEvent)m.getEvent() : ExamEventDAO.getInstance().get(m.getEvent().getUniqueId()));
			    		conflict.setEnrollment(xe.getExam().countStudents());
		    			for (DepartmentalInstructor i: xe.getExam().getInstructors()) {
							ContactInterface instructor = new ContactInterface();
							instructor.setFirstName(i.getFirstName());
							instructor.setMiddleName(i.getMiddleName());
							instructor.setLastName(i.getLastName());
							instructor.setEmail(i.getEmail());
							conflict.addInstructor(instructor);
		    			}
					} else if (Event.sEventTypeCourse == m.getEvent().getEventType()) {
			    		CourseEvent ce = (m.getEvent() instanceof CourseEvent ? (CourseEvent)m.getEvent() : CourseEventDAO.getInstance().get(m.getEvent().getUniqueId()));
			    		int enrl = 0;
						for (RelatedCourseInfo owner: ce.getRelatedCourses()) {
							enrl += owner.countStudents();
		    			}
						conflict.setEnrollment(enrl);
					}
					
					conflict.setId(m.getUniqueId());
					conflict.setMeetingDate(m.getMeetingDate());
					conflict.setDayOfYear(CalendarUtils.date2dayOfYear(session.getSessionStartYear(), m.getMeetingDate()));
					conflict.setStartOffset(m.getStartOffset() == null ? 0 : m.getStartOffset());
					conflict.setEndOffset(m.getStopOffset() == null ? 0 : m.getStopOffset());
					conflict.setStartSlot(m.getStartPeriod());
					conflict.setEndSlot(m.getStopPeriod());
					conflict.setApprovalDate(m.getApprovalDate());
					conflict.setApprovalStatus(m.getApprovalStatus());
					
					response.addOverlap(CalendarUtils.date2dayOfYear(session.getSessionStartYear(), m.getMeetingDate()), m.getLocationPermanentId(), conflict);
				}
				
				query = EventDAO.getInstance().getSession().createQuery(
						"from Location where session.uniqueId = :sessionId and permanentId in (" + locations + ")");
				for (int i = idx; i + idx < request.getLocations().size() && i < 1000; i++)
					query.setLong("l" + i, request.getLocations().get(idx + i));

				for (Location location: (List<Location>)query.setLong("sessionId", request.getSessionId()).setCacheable(true).list()) {
					if (context.hasPermission(location, request.getEventType() == EventType.Unavailabile ? Right.EventLocationUnavailable : Right.EventLocation)) {
						Set<MeetingConflictInterface> conflicts = generateUnavailabilityMeetings(location, request.getDates(), request.getStartSlot(), request.getEndSlot());
						if (conflicts != null && !conflicts.isEmpty())
							for (MeetingConflictInterface conflict: conflicts)
								response.addOverlap(conflict.getDayOfYear(), location.getPermanentId(), conflict);
					} else {
						for (Integer date: request.getDates()) {
							MeetingConflictInterface conflict = new MeetingConflictInterface();
							if (location == null || location.getEventDepartment() == null || !location.getEventDepartment().isAllowEvents())
								conflict.setName(MESSAGES.conflictNotEventRoom(location.getLabel()));
							else if (request.getEventType() == EventType.Unavailabile)
								conflict.setName(MESSAGES.conflictCannotMakeUnavailable(location.getLabel()));
							else
								conflict.setName(MESSAGES.conflictRoomDenied(location.getLabel()));
							if (location.getEventDepartment() != null && location.getEventDepartment().isAllowEvents()) {
								String message = location.getEventMessage();
								if (message != null && !message.isEmpty()) {
									conflict.setName(message);
								}
							}
							conflict.setType(EventInterface.EventType.Unavailabile);
							conflict.setMeetingDate(CalendarUtils.dateOfYear2date(session.getSessionStartYear(), date));
							conflict.setDayOfYear(date);
							conflict.setStartOffset(0);
							conflict.setEndOffset(0);
							conflict.setStartSlot(0);
							conflict.setEndSlot(288);
							response.addOverlap(date, location.getPermanentId(), conflict);
						}
					}
				}
			}
		}
		
		if (request.hasMeetings()) {
			response.setMeetings(request.getMeetings());
			
			for (MeetingInterface meeting: response.getMeetings()) {
				if (meeting.hasConflicts()) meeting.getConflicts().clear();
				
				if (meeting.getMeetingDate() == null) {
					meeting.setMeetingDate(CalendarUtils.dateOfYear2date(session.getSessionStartYear(), meeting.getDayOfYear()));
					meeting.setDayOfWeek(Constants.getDayOfWeek(meeting.getMeetingDate()));
				}
				
				if (meeting.getApprovalStatus() == ApprovalStatus.Deleted || meeting.getApprovalStatus() == ApprovalStatus.Cancelled || meeting.getApprovalStatus() == ApprovalStatus.Rejected) continue;
				
				if (context.isPastOrOutside(meeting.getMeetingDate())) {
					MeetingConflictInterface conflict = new MeetingConflictInterface();
					conflict.setName(MESSAGES.conflictPastOrOutside(session.getLabel()));
					conflict.setType(meeting.getId() == null ? EventInterface.EventType.Unavailabile : EventInterface.EventType.Message);
					conflict.setMeetingDate(meeting.getMeetingDate());
					conflict.setDayOfYear(meeting.getDayOfYear());
					conflict.setStartOffset(0);
					conflict.setEndOffset(0);
					conflict.setStartSlot(0);
					conflict.setEndSlot(288);
					conflict.setPast(true);
					meeting.addConflict(conflict);
				}
				
				if (!meeting.hasLocation()) continue;
				
				meeting.setCanApprove(context.hasPermission(meeting.getLocation().getId(), "Location", Right.EventLocationApprove));
				
				Location location = LocationDAO.getInstance().get(meeting.getLocation().getId());
				boolean available = true;
				
				if (location == null || !context.hasPermission(location, Right.EventLocation)) {
					MeetingConflictInterface conflict = new MeetingConflictInterface();
					if (location == null || location.getEventDepartment() == null || !location.getEventDepartment().isAllowEvents())
						conflict.setName(MESSAGES.conflictNotEventRoom(meeting.getLocationName()));
					else
						conflict.setName(MESSAGES.conflictRoomDenied(meeting.getLocationName()));
					if (location != null && location.getEventDepartment() != null && location.getEventDepartment().isAllowEvents()) {
						String message = location.getEventMessage();
						if (message != null && !message.isEmpty()) {
							conflict.setName(message);
						}
					}
					conflict.setType(meeting.getId() == null ? EventInterface.EventType.Unavailabile : EventInterface.EventType.Message);
					conflict.setMeetingDate(meeting.getMeetingDate());
					conflict.setDayOfYear(meeting.getDayOfYear());
					conflict.setStartOffset(0);
					conflict.setEndOffset(0);
					conflict.setStartSlot(0);
					conflict.setEndSlot(288);
					meeting.addConflict(conflict);
					available = false;
				} else if (request.getEventType() == EventType.Unavailabile && !context.hasPermission(location, Right.EventLocationUnavailable)) {
					MeetingConflictInterface conflict = new MeetingConflictInterface();
					if (location == null || location.getEventDepartment() == null || !location.getEventDepartment().isAllowEvents())
						conflict.setName(MESSAGES.conflictNotEventRoom(meeting.getLocationName()));
					else
						conflict.setName(MESSAGES.conflictCannotMakeUnavailable(meeting.getLocationName()));
					conflict.setType(meeting.getId() == null ? EventInterface.EventType.Unavailabile : EventInterface.EventType.Message);
					conflict.setMeetingDate(meeting.getMeetingDate());
					conflict.setDayOfYear(meeting.getDayOfYear());
					conflict.setStartOffset(0);
					conflict.setEndOffset(0);
					conflict.setStartSlot(0);
					conflict.setEndSlot(288);
					meeting.addConflict(conflict);
					available = false;
				}
				
				for (Meeting m: (List<Meeting>)EventDAO.getInstance().getSession().createQuery(
						"select m from Meeting m, Location l "+
						"where m.startPeriod < :stopTime and m.stopPeriod > :startTime and m.approvalStatus <= 1 and " +
						"m.locationPermanentId = l.permanentId and l.uniqueId = :locationdId and m.meetingDate = :meetingDate and m.uniqueId != :meetingId")
						.setInteger("startTime", meeting.getStartSlot())
						.setInteger("stopTime", meeting.getEndSlot())
						.setDate("meetingDate", meeting.getMeetingDate())
						.setLong("locationdId", meeting.getLocation().getId())
						.setLong("meetingId", meeting.getId() == null ? -1 : meeting.getId())
						.list()) {
					
					MeetingConflictInterface conflict = new MeetingConflictInterface();
					
					if (request.hasEventId() && m.getEvent().getUniqueId().equals(request.getEventId())) continue;

					conflict.setEventId(m.getEvent().getUniqueId());
					conflict.setName(m.getEvent().getEventName());
					conflict.setType(EventInterface.EventType.values()[m.getEvent().getEventType()]);
					
					conflict.setId(m.getUniqueId());
					conflict.setMeetingDate(m.getMeetingDate());
					conflict.setDayOfYear(meeting.getDayOfYear());
					conflict.setStartSlot(m.getStartPeriod());
					conflict.setEndSlot(m.getStopPeriod());
					conflict.setStartOffset(m.getStartOffset() == null ? 0 : m.getStartOffset());
					conflict.setEndOffset(m.getStopOffset() == null ? 0 : m.getStopOffset());
					conflict.setApprovalDate(m.getApprovalDate());
					conflict.setApprovalStatus(m.getApprovalStatus());
					
					meeting.addConflict(conflict);
				}
				
				if (location != null && location.getEventAvailability() != null && location.getEventAvailability().length() == Constants.SLOTS_PER_DAY * Constants.DAY_CODES.length) {
					check: for (int slot = meeting.getStartSlot(); slot < meeting.getEndSlot(); slot++) {
						if (location.getEventAvailability().charAt(meeting.getDayOfWeek() * Constants.SLOTS_PER_DAY + slot) == '1') {
							for (MeetingConflictInterface conflict: generateUnavailabilityMeetings(location, meeting))
								meeting.addConflict(conflict);
							break check;
						}
					}
				}
				
				if (available) {
					if (location.getEventDepartment() == null || !location.getEventDepartment().isAllowEvents()) { // no event department
						MeetingConflictInterface conflict = new MeetingConflictInterface();
						conflict.setName(MESSAGES.conflictNotEventRoom(meeting.getLocationName()));
						conflict.setType(EventInterface.EventType.Message);
						conflict.setMeetingDate(meeting.getMeetingDate());
						conflict.setDayOfYear(meeting.getDayOfYear());
						conflict.setStartOffset(0);
						conflict.setEndOffset(0);
						conflict.setStartSlot(0);
						conflict.setEndSlot(288);
						meeting.addConflict(conflict);
					} else { // has a message?
						String message = location.getEventMessage();
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
					}
					int tooEarly = Integer.valueOf(ApplicationProperties.getProperty("unitime.event.tooEarly", "-1"));
					if (tooEarly >= 0 && ((meeting.getStartSlot() > 0 && meeting.getStartSlot() <= tooEarly) || (meeting.getStartSlot() == 0 && meeting.getEndSlot() <= tooEarly))) {
						MeetingConflictInterface conflict = new MeetingConflictInterface();
						conflict.setName(MESSAGES.warnMeetingTooEarly(meeting.getAllocatedTime(CONSTANTS)));
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
		}

		
		return response;
	}
	
	public static TreeSet<MeetingConflictInterface> generateUnavailabilityMeetings(Location location, List<Integer> dates, int startSlot, int endSlot) {
		if (location.getEventAvailability() == null || location.getEventAvailability().length() != Constants.SLOTS_PER_DAY * Constants.DAY_CODES.length) return null;

		TreeSet<MeetingConflictInterface> ret = new TreeSet<MeetingConflictInterface>();
		
		ResourceInterface resource = new ResourceInterface();
		resource.setType(ResourceType.ROOM);
		resource.setId(location.getUniqueId());
		resource.setName(location.getLabel());
		resource.setSize(location.getCapacity());
		resource.setRoomType(location.getRoomTypeLabel());
		resource.setBreakTime(location.getEffectiveBreakTime());
		resource.setMessage(location.getEventMessage());
		
		Calendar calendar = Calendar.getInstance();
        for (int day = 0; day < Constants.DAY_CODES.length; day++)
        	for (int startTime = 0; startTime < Constants.SLOTS_PER_DAY; ) {
        		if (location.getEventAvailability().charAt(day * Constants.SLOTS_PER_DAY + startTime) != '1') { startTime++; continue; }
        		int endTime = startTime + 1;
        		while (endTime < Constants.SLOTS_PER_DAY && location.getEventAvailability().charAt(day * Constants.SLOTS_PER_DAY + endTime) == '1') endTime++;
        		if (startTime < endSlot && startSlot < endTime) {
            		calendar.setTime(location.getSession().getEventBeginDate());
            		int dayOfYear = CalendarUtils.date2dayOfYear(location.getSession().getSessionStartYear(), calendar.getTime());
            		
            		do {
            			if (dates.contains(dayOfYear)) {
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
                    			MeetingConflictInterface m = new MeetingConflictInterface();
                				m.setName(MESSAGES.unavailableEventDefaultName());
                				m.setType(EventInterface.EventType.Unavailabile);
                        		m.setStartSlot(startTime);
                        		m.setEndSlot(endTime);
                        		m.setDayOfWeek(dayOfWeek);
                        		m.setMeetingDate(calendar.getTime());
                        		m.setDayOfYear(dayOfYear);
                        		m.setLocation(resource);
                        		ret.add(m);
                			}
            			}
            			calendar.add(Calendar.DAY_OF_YEAR, 1); dayOfYear++;
            		} while (!calendar.getTime().after(location.getSession().getEventEndDate()));        			
        		}
        		startTime = endTime;
        	}
		return ret;
	}
	
	public static TreeSet<MeetingConflictInterface> generateUnavailabilityMeetings(Location location, MeetingInterface meeting) {
		if (location.getEventAvailability() == null || location.getEventAvailability().length() != Constants.SLOTS_PER_DAY * Constants.DAY_CODES.length) return null;

		TreeSet<MeetingConflictInterface> ret = new TreeSet<MeetingConflictInterface>();
		
		ResourceInterface resource = new ResourceInterface();
		resource.setType(ResourceType.ROOM);
		resource.setId(location.getUniqueId());
		resource.setName(location.getLabel());
		resource.setSize(location.getCapacity());
		resource.setRoomType(location.getRoomTypeLabel());
		resource.setBreakTime(location.getEffectiveBreakTime());
		resource.setMessage(location.getEventMessage());
		
		int day = meeting.getDayOfWeek();
		for (int startTime = 0; startTime < Constants.SLOTS_PER_DAY; ) {
    		if (location.getEventAvailability().charAt(day * Constants.SLOTS_PER_DAY + startTime) != '1') { startTime++; continue; }
    		int endTime = startTime + 1;
    		while (endTime < Constants.SLOTS_PER_DAY && location.getEventAvailability().charAt(day * Constants.SLOTS_PER_DAY + endTime) == '1') endTime++;
    		if (startTime < meeting.getEndSlot() && meeting.getStartSlot() < endTime) {
    			
    			MeetingConflictInterface m = new MeetingConflictInterface();
				m.setName(MESSAGES.unavailableEventDefaultName());
				m.setType(EventInterface.EventType.Unavailabile);
        		m.setStartSlot(startTime);
        		m.setEndSlot(endTime);
        		m.setDayOfWeek(meeting.getDayOfWeek());
        		m.setMeetingDate(meeting.getMeetingDate());
        		m.setDayOfYear(meeting.getDayOfYear());
        		m.setLocation(resource);
        		ret.add(m);

    		}
    		startTime = endTime;
    	}
    	
		return ret;
	}
}
