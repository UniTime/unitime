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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventDetailRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SaveEventRpcRequest;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.SpecialEvent;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;
import org.unitime.timetable.webutil.EventEmail;

public class SaveEventBackend extends EventAction<SaveEventRpcRequest, EventInterface> {
	@Override
	public EventInterface execute(SaveEventRpcRequest request, GwtRpcHelper helper, EventRights rights) {
		if (!rights.canAddEvent(request.getEvent().getType(), request.getEvent().hasContact() ? request.getEvent().getContact().getExternalId() : null)) throw rights.getException();
		
		EventEmail.Result result = save(request.getEvent(), request.getSessionId(), helper, rights);
		
		EventInterface event = null;
		if (request.getEvent().hasMeetings()) {
			event = new EventDetailBackend().execute(EventDetailRpcRequest.requestEventDetails(request.getSessionId(), request.getEvent().getId()), helper, rights);
		} else {
			event = request.getEvent();
		}
		
		event.setMessage((result.isWarning() ? "WARN:" : "") + result.getMessage());
				
		return event;
	}
		
	protected EventEmail.Result save(EventInterface e, Long sessionId, GwtRpcHelper helper, EventRights rights) {
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			EventRoomAvailabilityRpcResponse availability = new EventRoomAvailabilityBackend().execute(
					EventRoomAvailabilityRpcRequest.checkAvailability(new ArrayList<MeetingInterface>(e.getMeetings()), sessionId), helper, rights);
			
			Date now = new Date();
	        String uname = EventPropertiesBackend.lookupMainContact(sessionId, helper.getUser()).getShortName();

			Event event = null;
			if (e.getId() != null) {
				event = EventDAO.getInstance().get(e.getId(), hibSession);
			} else {
				switch (e.getType()) {
				case Special:
					event = new SpecialEvent(); break;
				case Course:
					event = new CourseEvent(); break;
				default:
					throw new GwtRpcException(MESSAGES.failedSaveEventWrongType(e.getType().getName()));
				}
			}
			
			event.setEventName(e.getName());
			event.setEmail(e.getEmail());
			event.setSponsoringOrganization(e.hasSponsor() ? SponsoringOrganizationDAO.getInstance().get(e.getSponsor().getUniqueId()) : null);
			if (event instanceof SpecialEvent) {
				event.setMinCapacity(e.getMaxCapacity());
				event.setMaxCapacity(e.getMaxCapacity());
			}
			if (event.getAdditionalContacts() == null) {
				event.setAdditionalContacts(new HashSet<EventContact>());
			}
			if (rights.canLookupContacts()) {
				Set<EventContact> existingContacts = new HashSet<EventContact>(event.getAdditionalContacts());
				event.getAdditionalContacts().clear();
				if (e.hasAdditionalContacts())
					for (ContactInterface c: e.getAdditionalContacts()) {
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
			if (main == null || !main.getExternalUniqueId().equals(e.getContact().getExternalId())) {
				main = (EventContact)hibSession.createQuery(
						"from EventContact where externalUniqueId = :externalId")
						.setString("externalId", e.getContact().getExternalId()).setMaxResults(1).uniqueResult();
				if (main == null) {
					main = new EventContact();
					main.setExternalUniqueId(e.getContact().getExternalId());
				}
			}
			main.setFirstName(e.getContact().getFirstName());
			main.setMiddleName(e.getContact().getMiddleName());
			main.setLastName(e.getContact().getLastName());
			main.setEmailAddress(e.getContact().getEmail());
			main.setPhone(e.getContact().getPhone());
			hibSession.saveOrUpdate(main);
			event.setMainContact(main);
			
			if (event.getNotes() == null)
				event.setNotes(new HashSet<EventNote>());
						
			if (event.getMeetings() == null) event.setMeetings(new HashSet<Meeting>());
			Set<Meeting> remove = new HashSet<Meeting>(event.getMeetings());
			TreeSet<Meeting> created = new TreeSet<Meeting>();
			for (MeetingInterface m: e.getMeetings()) {
				Meeting meeting = null; 
				if (m.getId() != null)
					for (Iterator<Meeting> i = remove.iterator(); i.hasNext(); ) {
						Meeting x = i.next();
						if (m.getId().equals(x.getUniqueId())) { meeting = x; i.remove(); break; }
					}
				if (meeting != null) {
					meeting.setStartOffset(m.getStartOffset());
					meeting.setStopOffset(m.getEndOffset());
					hibSession.update(meeting);
				} else {
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
					if (!rights.canOverbook(location.getUniqueId()))
						for (MeetingInterface x: availability.getMeetings()) {
							if (x.equals(m) && x.hasConflicts())
								throw new GwtRpcException(MESSAGES.failedSaveEventConflict(toString(m), toString(x.getConflicts().iterator().next())));
						}
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
			
			if (!created.isEmpty()) {
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(event.getUniqueId() == null ? EventNote.sEventNoteTypeCreateEvent : EventNote.sEventNoteTypeAddMeetings);
				note.setTimeStamp(now);
				note.setUser(uname);
				if (e.hasNotes() && e.getNotes().last().getDate() == null)
					note.setTextNote(e.getNotes().last().getNote());
				note.setMeetingCollection(created);
				event.getNotes().add(note);
			}
			TreeSet<Meeting> deleted = new TreeSet<Meeting>(remove); 
			if (!remove.isEmpty()) {
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(EventNote.sEventNoteTypeDeletion);
				note.setTimeStamp(now);
				note.setUser(uname);
				if (e.hasNotes() && e.getNotes().last().getDate() == null)
					note.setTextNote(e.getNotes().last().getNote());
				event.getNotes().add(note);
				note.setMeetingCollection(deleted);
				event.getMeetings().removeAll(remove);
			}
			if (created.isEmpty() && remove.isEmpty()) {
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(event.getUniqueId() == null ? EventNote.sEventNoteTypeCreateEvent : EventNote.sEventNoteTypeEditEvent);
				note.setTimeStamp(now);
				note.setUser(uname);
				if (e.hasNotes() && e.getNotes().last().getDate() == null)
					note.setTextNote(e.getNotes().last().getNote());
				event.getNotes().add(note);
				for (Iterator<Meeting> i = remove.iterator(); i.hasNext(); ) {
					Meeting x = i.next();
					hibSession.delete(x);
					i.remove();
				}
			}
			
			if (e.getType() == EventType.Course) {
				CourseEvent ce = (CourseEvent)event;
				ce.setReqAttendance(e.hasRequiredAttendance());
				if (ce.getRelatedCourses() == null)
					ce.setRelatedCourses(new HashSet<RelatedCourseInfo>());
				else
					ce.getRelatedCourses().clear();
				if (e.hasRelatedObjects())
					for (RelatedObjectInterface r: e.getRelatedObjects()) {
						RelatedCourseInfo related = new RelatedCourseInfo();
						related.setEvent(ce);
						related.setOwnerId(r.getUniqueId());
						related.setOwnerType(r.getType().ordinal());
						related.setCourse(CourseOfferingDAO.getInstance().get(r.getSelection()[1], hibSession));
						ce.getRelatedCourses().add(related);
					}
			}
			
			Long eventId = event.getUniqueId();
			if (eventId == null) {
				eventId = (Long)hibSession.save(event);
			} else if (event.getMeetings().isEmpty()) {
				hibSession.delete(event);
			} else {
				hibSession.update(event);
			}
			
			EventEmail.Result result = new EventEmail(event,
					(e.getId() == null ? EventEmail.sActionCreate : !created.isEmpty() ? EventEmail.sActionAddMeeting : !deleted.isEmpty() ? EventEmail.sActionDelete : EventEmail.sActionUpdate),
					Event.getMultiMeetings(created),
					Event.getMultiMeetings(deleted),
					(e.hasNotes() && e.getNotes().last().getDate() != null ? e.getNotes().last().getNote() : null),
					null).send(helper.getRequestUrl());
			
			tx.commit();
			
			e.setId(eventId);
			return result;
		} catch (Exception ex) {
			tx.rollback();
			if (ex instanceof GwtRpcException) throw (GwtRpcException)ex;
			throw new GwtRpcException(ex.getMessage(), ex);
		}
	}
}
