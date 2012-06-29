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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConglictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.SaveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.SaveOrApproveEventRpcResponse;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SpecialEvent;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;

public class SaveEventBackend extends EventAction<SaveEventRpcRequest, SaveOrApproveEventRpcResponse> {
	@Override
	public SaveOrApproveEventRpcResponse execute(SaveEventRpcRequest request, GwtRpcHelper helper, EventRights rights) {
		if (!rights.canAddEvent(request.getEvent().getType(), request.getEvent().hasContact() ? request.getEvent().getContact().getExternalId() : null)) throw rights.getException();
		
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			Session session = SessionDAO.getInstance().get(request.getSessionId(), hibSession);
			Date now = new Date();
	        String uname = EventPropertiesBackend.lookupMainContact(request.getSessionId(), helper.getUser()).getShortName();

			Event event = null;
			if (request.getEvent().getId() != null) {
				event = EventDAO.getInstance().get(request.getEvent().getId(), hibSession);
			} else {
				switch (request.getEvent().getType()) {
				case Special:
					event = new SpecialEvent(); break;
				case Course:
					event = new CourseEvent(); break;
				default:
					throw new GwtRpcException(MESSAGES.failedSaveEventWrongType(request.getEvent().getType().getName()));
				}
			}
			
			SaveOrApproveEventRpcResponse response = new SaveOrApproveEventRpcResponse();
			
			event.setEventName(request.getEvent().getName());
			event.setEmail(request.getEvent().getEmail());
			event.setSponsoringOrganization(request.getEvent().hasSponsor() ? SponsoringOrganizationDAO.getInstance().get(request.getEvent().getSponsor().getUniqueId()) : null);
			if (event instanceof SpecialEvent) {
				event.setMinCapacity(request.getEvent().getMaxCapacity());
				event.setMaxCapacity(request.getEvent().getMaxCapacity());
			}
			if (event.getAdditionalContacts() == null) {
				event.setAdditionalContacts(new HashSet<EventContact>());
			}
			if (rights.canLookupContacts()) {
				Set<EventContact> existingContacts = new HashSet<EventContact>(event.getAdditionalContacts());
				event.getAdditionalContacts().clear();
				if (request.getEvent().hasAdditionalContacts())
					for (ContactInterface c: request.getEvent().getAdditionalContacts()) {
						if (c.getExternalId() == null) continue;
						EventContact contact = null;
						for (EventContact x: existingContacts)
							if (c.getExternalId().equals(x.getExternalUniqueId())) {  contact = x; break; }
						if (contact == null) {
							contact = (EventContact)hibSession.createQuery(
									"from EventContact where externalUniqueId = :externalId")
									.setString("externalId", c.getExternalId()).setMaxResults(1).uniqueResult();
						}
						if (contact == null) {
							contact = new EventContact();
							contact.setExternalUniqueId(c.getExternalId());
							contact.setFirstName(c.getFirstName());
							contact.setMiddleName(c.getMiddleName());
							contact.setLastName(c.getLastName());
							contact.setEmailAddress(c.getEmail());
							contact.setPhone(c.getPhone());
							hibSession.save(contact);
						}
						event.getAdditionalContacts().add(contact);
					}				
			}
			
			EventContact main = event.getMainContact();
			if (main == null || main.getExternalUniqueId() == null || !main.getExternalUniqueId().equals(request.getEvent().getContact().getExternalId())) {
				main = (EventContact)hibSession.createQuery(
						"from EventContact where externalUniqueId = :externalId")
						.setString("externalId", request.getEvent().getContact().getExternalId()).setMaxResults(1).uniqueResult();
				if (main == null) {
					main = new EventContact();
					main.setExternalUniqueId(request.getEvent().getContact().getExternalId());
				}
			}
			main.setFirstName(request.getEvent().getContact().getFirstName());
			main.setMiddleName(request.getEvent().getContact().getMiddleName());
			main.setLastName(request.getEvent().getContact().getLastName());
			main.setEmailAddress(request.getEvent().getContact().getEmail());
			main.setPhone(request.getEvent().getContact().getPhone());
			hibSession.saveOrUpdate(main);
			event.setMainContact(main);
			
			if (event.getNotes() == null)
				event.setNotes(new HashSet<EventNote>());
						
			if (event.getMeetings() == null) event.setMeetings(new HashSet<Meeting>());
			Set<Meeting> remove = new HashSet<Meeting>(event.getMeetings());
			TreeSet<Meeting> created = new TreeSet<Meeting>();
			for (MeetingInterface m: request.getEvent().getMeetings()) {
				Meeting meeting = null; 
				if (m.isDelete()) continue;
				if (m.getId() != null)
					for (Iterator<Meeting> i = remove.iterator(); i.hasNext(); ) {
						Meeting x = i.next();
						if (m.getId().equals(x.getUniqueId())) { meeting = x; i.remove(); break; }
					}
				if (meeting != null) {
					if (m.getStartOffset() != meeting.getStartOffset() || m.getEndOffset() != meeting.getStopOffset()) {
						if (!rights.canEdit(meeting))
							throw new GwtRpcException(MESSAGES.failedSaveEventCanNotEditMeeting(toString(meeting)));
						meeting.setStartOffset(m.getStartOffset());
						meeting.setStopOffset(m.getEndOffset());
						hibSession.update(meeting);
						response.addUpdatedMeeting(m);
					}
				} else {
					response.addCreatedMeeting(m);
					meeting = new Meeting();
					meeting.setEvent(event);
					Location location = (m.hasLocation() ? LocationDAO.getInstance().get(m.getLocation().getId(), hibSession) : null);
					if (location == null) throw new GwtRpcException(MESSAGES.failedSaveEventNoLocation(toString(m)));
					meeting.setLocationPermanentId(location.getPermanentId());
					meeting.setApprovedDate(null);
					if (!rights.canCreate(location.getUniqueId()))
						throw new GwtRpcException(MESSAGES.failedSaveEventWrongLocation(m.getLocationName()));
					if (rights.canApprove(location.getUniqueId()))
						meeting.setApprovedDate(now);
					if (rights.isPastOrOutside(m.getMeetingDate()))
						throw new GwtRpcException(MESSAGES.failedSaveEventPastOrOutside(sMeetingDateFormat.format(m.getMeetingDate())));
					if (!rights.canOverbook(location.getUniqueId())) {
						List<MeetingConglictInterface> conflicts = computeConflicts(hibSession, m, event.getUniqueId());
						if (!conflicts.isEmpty())
							throw new GwtRpcException(MESSAGES.failedSaveEventConflict(toString(m), toString(conflicts.get(0))));
					}
					m.setApprovalDate(meeting.getApprovedDate());
					meeting.setStartPeriod(m.getStartSlot());
					meeting.setStopPeriod(m.getEndSlot());
					meeting.setStartOffset(m.getStartOffset());
					meeting.setStopOffset(m.getEndOffset());
					meeting.setClassCanOverride(true);
                    meeting.setMeetingDate(m.getMeetingDate());
                    event.getMeetings().add(meeting);
                    created.add(meeting);
				}
			}
			
			if (!remove.isEmpty()) {
				for (Meeting m: remove) {
					if (!rights.canEdit(m))
						throw new GwtRpcException(MESSAGES.failedSaveEventCanNotDeleteMeeting(toString(m)));
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
					meeting.setPast(rights.isPastOrOutside(m.getStartTime()));
					if (m.isApproved()) meeting.setApprovalDate(m.getApprovedDate());
					if (m.getLocation() != null) {
						ResourceInterface location = new ResourceInterface();
						location.setType(ResourceType.ROOM);
						location.setId(m.getLocation().getUniqueId());
						location.setName(m.getLocation().getLabel());
						location.setHint(m.getLocation().getHtmlHint());
						location.setSize(m.getLocation().getCapacity());
						location.setRoomType(m.getLocation().getRoomTypeLabel());
						location.setBreakTime(m.getLocation().getBreakTime());
						meeting.setLocation(location);
					}
					response.addDeletedMeeting(meeting);
				}
				event.getMeetings().removeAll(remove);
			}
			
			EventInterface.DateFormatter df = new EventInterface.DateFormatter() {
				DateFormat dfShort = new SimpleDateFormat(CONSTANTS.eventDateFormatShort(), Localization.getJavaLocale());
				DateFormat dfLong = new SimpleDateFormat(CONSTANTS.eventDateFormatLong(), Localization.getJavaLocale());
				@Override
				public String formatFirstDate(Date date) {
					return dfShort.format(date);
				}
				@Override
				public String formatLastDate(Date date) {
					return dfLong.format(date);
				}
			};
			
			if (response.hasCreatedMeetings()) {
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(event.getUniqueId() == null ? EventNote.sEventNoteTypeCreateEvent : EventNote.sEventNoteTypeAddMeetings);
				note.setTimeStamp(now);
				note.setUser(uname);
				if (request.hasMessage()) note.setTextNote(request.getMessage());
				note.setMeetings(EventInterface.toString(
						response.getCreatedMeetings(),
						CONSTANTS, "\n", df));
				event.getNotes().add(note);
				NoteInterface n = new NoteInterface();
				n.setDate(now);
				n.setMeetings(note.getMeetings());
				n.setUser(uname);
				n.setType(NoteInterface.NoteType.values()[note.getNoteType()]);
				n.setNote(note.getTextNote());
				response.addNote(n);
			}
			if (response.hasUpdatedMeetings() || (!response.hasCreatedMeetings() && !response.hasDeletedMeetings())) {
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(EventNote.sEventNoteTypeEditEvent);
				note.setTimeStamp(now);
				note.setUser(uname);
				if (request.hasMessage()) note.setTextNote(request.getMessage());
				if (response.hasUpdatedMeetings())
					note.setMeetings(EventInterface.toString(
							response.getUpdatedMeetings(),
							CONSTANTS, "\n", df));
				event.getNotes().add(note);
				NoteInterface n = new NoteInterface();
				n.setDate(now);
				n.setMeetings(note.getMeetings());
				n.setUser(uname);
				n.setType(NoteInterface.NoteType.values()[note.getNoteType()]);
				n.setNote(note.getTextNote());
				response.addNote(n);
			}
			if (response.hasDeletedMeetings()) {
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(EventNote.sEventNoteTypeDeletion);
				note.setTimeStamp(now);
				note.setUser(uname);
				if (request.hasMessage()) note.setTextNote(request.getMessage());
				note.setMeetings(EventInterface.toString(
						response.getDeletedMeetings(),
						CONSTANTS, "\n", df));
				event.getNotes().add(note);
				NoteInterface n = new NoteInterface();
				n.setDate(now);
				n.setMeetings(note.getMeetings());
				n.setUser(uname);
				n.setType(NoteInterface.NoteType.values()[note.getNoteType()]);
				n.setNote(note.getTextNote());
				response.addNote(n);
			}
			
			if (request.getEvent().getType() == EventType.Course) {
				CourseEvent ce = (CourseEvent)event;
				ce.setReqAttendance(request.getEvent().hasRequiredAttendance());
				if (ce.getRelatedCourses() == null)
					ce.setRelatedCourses(new HashSet<RelatedCourseInfo>());
				else
					ce.getRelatedCourses().clear();
				if (request.getEvent().hasRelatedObjects())
					for (RelatedObjectInterface r: request.getEvent().getRelatedObjects()) {
						RelatedCourseInfo related = new RelatedCourseInfo();
						related.setEvent(ce);
						related.setOwnerId(r.getUniqueId());
						related.setOwnerType(r.getType().ordinal());
						related.setCourse(CourseOfferingDAO.getInstance().get(r.getSelection()[1], hibSession));
						ce.getRelatedCourses().add(related);
					}
			}
			
			if (event.getUniqueId() == null) {
				hibSession.save(event);
				response.setEvent(EventDetailBackend.getEventDetail(SessionDAO.getInstance().get(request.getSessionId(), hibSession), event, rights));
			} else if (event.getMeetings().isEmpty()) {
				response.setEvent(EventDetailBackend.getEventDetail(SessionDAO.getInstance().get(request.getSessionId(), hibSession), event, rights));
				response.getEvent().setId(null);
				hibSession.delete(event);
			} else {
				hibSession.update(event);
				response.setEvent(EventDetailBackend.getEventDetail(SessionDAO.getInstance().get(request.getSessionId(), hibSession), event, rights));
			}
			
			new EventEmail(request, response).send(helper);
			
			tx.commit();
			
			return response;
		} catch (Exception ex) {
			tx.rollback();
			if (ex instanceof GwtRpcException) throw (GwtRpcException)ex;
			throw new GwtRpcException(ex.getMessage(), ex);
		}
	}
	
	private List<MeetingConglictInterface> computeConflicts(org.hibernate.Session hibSession, MeetingInterface meeting, Long eventId) {
		List<MeetingConglictInterface> conflicts = new ArrayList<EventInterface.MeetingConglictInterface>();
		for (Meeting m: (List<Meeting>)hibSession.createQuery(
				"select m from Meeting m, Location l "+
				"where m.startPeriod < :stopTime and m.stopPeriod > :startTime and " +
				"m.locationPermanentId = l.permanentId and l.uniqueId = :locationdId and m.meetingDate = :meetingDate and m.uniqueId != :meetingId and m.event.uniqueId != :eventId")
				.setInteger("startTime", meeting.getStartSlot())
				.setInteger("stopTime", meeting.getEndSlot())
				.setDate("meetingDate", meeting.getMeetingDate())
				.setLong("locationdId", meeting.getLocation().getId())
				.setLong("meetingId", meeting.getId() == null ? -1 : meeting.getId())
				.setLong("eventId", eventId == null ? -1 : eventId)
				.list()) {
			
			MeetingConglictInterface conflict = new MeetingConglictInterface();

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
			if (m.isApproved())
				conflict.setApprovalDate(m.getApprovedDate());
			
			conflicts.add(conflict);
		}
		return conflicts;
	}
		
}
