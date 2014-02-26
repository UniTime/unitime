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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.command.server.GwtRpcServlet;
import org.unitime.timetable.gwt.server.UploadServlet;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConflictInterface;
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
import org.unitime.timetable.model.UnavailableEvent;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SaveEventRpcRequest.class)
public class SaveEventBackend extends EventAction<SaveEventRpcRequest, SaveOrApproveEventRpcResponse> {
	private @Autowired ApplicationContext applicationContext;
	
	@Override
	public SaveOrApproveEventRpcResponse execute(SaveEventRpcRequest request, EventContext context) {
		if (request.getEvent().hasContact() && (request.getEvent().getContact().getExternalId() == null || !request.getEvent().getContact().getExternalId().equals(context.getUser().getExternalUserId()))) {
			switch (request.getEvent().getType()) {
			case Special:
			case Course:
			case Unavailabile:
				context.checkPermission(Right.EventLookupContact);
			}
		}
		if (request.getEvent().getId() == null) { // new even
			switch (request.getEvent().getType()) {
			case Special:
				context.checkPermission(Right.EventAddSpecial);
				break;
			case Course:
				context.checkPermission(Right.EventAddCourseRelated);
				break;
			case Unavailabile:
				context.checkPermission(Right.EventAddUnavailable);
				break;
			default:
				throw context.getException();
			}
		} else { // existing event
			context.checkPermission(request.getEvent().getId(), "Event", Right.EventEdit);
		}
		
		// Check main contact email
		if (request.getEvent().hasContact() && request.getEvent().getContact().hasEmail()) {
			try {
				new InternetAddress(request.getEvent().getContact().getEmail(), true);
			} catch (AddressException e) {
				throw new GwtRpcException(MESSAGES.badEmailAddress(request.getEvent().getContact().getEmail(), e.getMessage()));
			}
		}
		// Check additional emails
		if (request.getEvent().hasEmail()) {
			String suffix = ApplicationProperties.getProperty("unitime.email.event.suffix", null);
			for (String address: request.getEvent().getEmail().split("[\n,]")) {
				String email = address.trim();
				if (email.isEmpty()) continue;
				if (suffix != null && email.indexOf('@') < 0)
					email += suffix;
				try {
					new InternetAddress(email, true);
				} catch (AddressException e) {
					throw new GwtRpcException(MESSAGES.badEmailAddress(address, e.getMessage()));
				}
			}
		}
		
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			Session session = SessionDAO.getInstance().get(request.getSessionId(), hibSession);
			Date now = new Date();
			String uname = GwtRpcServlet.execute(new EventPropertiesRpcRequest(request.getSessionId()), applicationContext, context).getMainContact().getShortName();

			Event event = null;
			if (request.getEvent().getId() != null) {
				event = EventDAO.getInstance().get(request.getEvent().getId(), hibSession);
			} else {
				switch (request.getEvent().getType()) {
				case Special:
					event = new SpecialEvent(); break;
				case Course:
					event = new CourseEvent(); break;
				case Unavailabile:
					event = new UnavailableEvent(); break;
				default:
					throw new GwtRpcException(MESSAGES.failedSaveEventWrongType(request.getEvent().getType().getName(CONSTANTS)));
				}
			}
			
			SaveOrApproveEventRpcResponse response = new SaveOrApproveEventRpcResponse();
			
			event.setEventName(request.getEvent().getName());
			if (event.getEventName() == null || event.getEventName().isEmpty() && request.getEvent().getType() == EventType.Unavailabile)
				event.setEventName(MESSAGES.unavailableEventDefaultName());
			event.setEmail(request.getEvent().getEmail());
			if (context.hasPermission(Right.EventSetExpiration) || event.getExpirationDate() != null)
				event.setExpirationDate(request.getEvent().getExpirationDate());
			event.setSponsoringOrganization(request.getEvent().hasSponsor() ? SponsoringOrganizationDAO.getInstance().get(request.getEvent().getSponsor().getUniqueId()) : null);
			if (event instanceof UnavailableEvent) {
			} else if (event instanceof SpecialEvent) {
				event.setMinCapacity(request.getEvent().getMaxCapacity());
				event.setMaxCapacity(request.getEvent().getMaxCapacity());
			}
			if (event.getAdditionalContacts() == null) {
				event.setAdditionalContacts(new HashSet<EventContact>());
			}
			if (context.hasPermission(Right.EventLookupContact)) {
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
			TreeSet<Meeting> createdMeetings = new TreeSet<Meeting>();
			Set<Meeting> cancelledMeetings = new TreeSet<Meeting>();
			Set<Meeting> updatedMeetings = new TreeSet<Meeting>();
			for (MeetingInterface m: request.getEvent().getMeetings()) {
				Meeting meeting = null; 
				if (m.getApprovalStatus() == ApprovalStatus.Deleted) {
					if (!context.hasPermission(meeting, Right.EventMeetingDelete) && context.hasPermission(meeting, Right.EventMeetingCancel)) {
						// Cannot delete, but can cancel --> cancel the meeting instead
						m.setApprovalStatus(ApprovalStatus.Cancelled);
					} else {
						continue;
					}
				}
				if (m.getId() != null)
					for (Iterator<Meeting> i = remove.iterator(); i.hasNext(); ) {
						Meeting x = i.next();
						if (m.getId().equals(x.getUniqueId())) { meeting = x; i.remove(); break; }
					}
				if (meeting != null) {
					if (m.getApprovalStatus().ordinal() != meeting.getApprovalStatus()) {
						switch (m.getApprovalStatus()) {
						case Cancelled:
    						switch (meeting.getEvent().getEventType()) {
    						case Event.sEventTypeFinalExam:
    						case Event.sEventTypeMidtermExam:
        						if (!context.hasPermission(meeting, Right.EventMeetingCancelExam))
        							throw new GwtRpcException(MESSAGES.failedApproveEventNoRightsToCancel(toString(meeting)));
        						break;
    						case Event.sEventTypeClass:
        						if (!context.hasPermission(meeting, Right.EventMeetingCancelClass))
        							throw new GwtRpcException(MESSAGES.failedApproveEventNoRightsToCancel(toString(meeting)));
        						break;
        					default:
        						if (!context.hasPermission(meeting, Right.EventMeetingCancel))
        							throw new GwtRpcException(MESSAGES.failedApproveEventNoRightsToCancel(toString(meeting)));
        						break;
    						}
							meeting.setStatus(Meeting.Status.CANCELLED);
							meeting.setApprovalDate(now);
							hibSession.update(meeting);
							cancelledMeetings.add(meeting);
							response.addCancelledMeeting(m);
						}
					} else {
						if (m.getStartOffset() != (meeting.getStartOffset() == null ? 0 : meeting.getStartOffset()) || m.getEndOffset() != (meeting.getStopOffset() == null ? 0 : meeting.getStopOffset())) {
							if (!context.hasPermission(meeting, Right.EventMeetingEdit))
								throw new GwtRpcException(MESSAGES.failedSaveEventCanNotEditMeeting(toString(meeting)));
							meeting.setStartOffset(m.getStartOffset());
							meeting.setStopOffset(m.getEndOffset());
							hibSession.update(meeting);
							response.addUpdatedMeeting(m);
							updatedMeetings.add(meeting);
						}
					}
				} else {
					response.addCreatedMeeting(m);
					meeting = new Meeting();
					meeting.setEvent(event);
					Location location = (m.hasLocation() ? LocationDAO.getInstance().get(m.getLocation().getId(), hibSession) : null);
					if (location == null) throw new GwtRpcException(MESSAGES.failedSaveEventNoLocation(toString(m)));
					meeting.setLocationPermanentId(location.getPermanentId());
					meeting.setStatus(Meeting.Status.PENDING);
					meeting.setApprovalDate(null);
					if (!context.hasPermission(location, Right.EventLocation))
						throw new GwtRpcException(MESSAGES.failedSaveEventWrongLocation(m.getLocationName()));
					if (request.getEvent().getType() == EventType.Unavailabile && !context.hasPermission(location, Right.EventLocationUnavailable))
						throw new GwtRpcException(MESSAGES.failedSaveCannotMakeUnavailable(m.getLocationName()));
					if (m.getApprovalStatus() == ApprovalStatus.Approved && context.hasPermission(location, Right.EventLocationApprove)) {
						meeting.setStatus(Meeting.Status.APPROVED);
						meeting.setApprovalDate(now);
					}
					if (context.isPastOrOutside(m.getMeetingDate()))
						throw new GwtRpcException(MESSAGES.failedSaveEventPastOrOutside(getDateFormat().format(m.getMeetingDate())));
					if (!context.hasPermission(location, Right.EventLocationOverbook)) {
						List<MeetingConflictInterface> conflicts = computeConflicts(hibSession, m, event.getUniqueId());
						if (!conflicts.isEmpty())
							throw new GwtRpcException(MESSAGES.failedSaveEventConflict(toString(m), toString(conflicts.get(0))));
					}
					m.setApprovalDate(meeting.getApprovalDate());
					m.setApprovalStatus(meeting.getApprovalStatus());
					meeting.setStartPeriod(m.getStartSlot());
					meeting.setStopPeriod(m.getEndSlot());
					meeting.setStartOffset(m.getStartOffset());
					meeting.setStopOffset(m.getEndOffset());
					meeting.setClassCanOverride(true);
                    meeting.setMeetingDate(m.getMeetingDate());
                    event.getMeetings().add(meeting);
                    createdMeetings.add(meeting);
				}
				// automatic approval
				if (meeting.getApprovalDate() == null) {
					switch (request.getEvent().getType()) {
					case Unavailabile:
					case Class:
					case FinalExam:
					case MidtermExam:
						meeting.setStatus(Meeting.Status.APPROVED);
						meeting.setApprovalDate(now);
						break;
					}
				}
			}
			
			if (!remove.isEmpty()) {
				for (Meeting m: remove) {
					if (!context.hasPermission(m, Right.EventMeetingDelete))
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
					response.addDeletedMeeting(meeting);
				}
				event.getMeetings().removeAll(remove);
			}
			
			EventInterface.DateFormatter df = new EventInterface.DateFormatter() {
				Formats.Format<Date> dfShort = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
				Formats.Format<Date> dfLong = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_LONG);
				@Override
				public String formatFirstDate(Date date) {
					return dfShort.format(date);
				}
				@Override
				public String formatLastDate(Date date) {
					return dfLong.format(date);
				}
			};
			
			FileItem attachment = (FileItem)context.getAttribute(UploadServlet.SESSION_LAST_FILE);
			boolean attached = false;
			if (response.hasCreatedMeetings()) {
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(event.getUniqueId() == null ? EventNote.sEventNoteTypeCreateEvent : EventNote.sEventNoteTypeAddMeetings);
				note.setTimeStamp(now);
				note.setUser(uname);
				note.setUserId(context.getUser().getExternalUserId());
				if (request.hasMessage()) note.setTextNote(request.getMessage());
				note.setMeetings(EventInterface.toString(
						response.getCreatedMeetings(),
						CONSTANTS, "\n", df));
				note.setAffectedMeetings(createdMeetings);
				event.getNotes().add(note);
				NoteInterface n = new NoteInterface();
				n.setDate(now);
				n.setMeetings(note.getMeetings());
				n.setUser(uname);
				n.setType(NoteInterface.NoteType.values()[note.getNoteType()]);
				n.setNote(note.getTextNote());
				if (attachment != null) {
					note.setAttachedName(attachment.getName());
					note.setAttachedFile(attachment.get());
					note.setAttachedContentType(attachment.getContentType());
					attached = true;
					n.setAttachment(attachment.getName());
				}
				response.addNote(n);
			}
			if (response.hasUpdatedMeetings() || (!response.hasCreatedMeetings() && !response.hasDeletedMeetings() && !response.hasCancelledMeetings())) {
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(EventNote.sEventNoteTypeEditEvent);
				note.setTimeStamp(now);
				note.setUser(uname);
				note.setUserId(context.getUser().getExternalUserId());
				note.setAffectedMeetings(updatedMeetings);
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
				if (attachment != null && !attached) {
					note.setAttachedName(attachment.getName());
					note.setAttachedFile(attachment.get());
					note.setAttachedContentType(attachment.getContentType());
					attached = true;
					n.setAttachment(attachment.getName());
				}
				response.addNote(n);
			}
			if (response.hasDeletedMeetings()) {
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(EventNote.sEventNoteTypeDeletion);
				note.setTimeStamp(now);
				note.setUser(uname);
				note.setUserId(context.getUser().getExternalUserId());
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
				if (attachment != null && !attached) {
					note.setAttachedName(attachment.getName());
					note.setAttachedFile(attachment.get());
					note.setAttachedContentType(attachment.getContentType());
					attached = true;
					n.setAttachment(attachment.getName());
				}
				response.addNote(n);
			}
			if (response.hasCancelledMeetings()) {
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(EventNote.sEventNoteTypeCancel);
				note.setTimeStamp(now);
				note.setUser(uname);
				note.setUserId(context.getUser().getExternalUserId());
				note.setAffectedMeetings(cancelledMeetings);
				if (request.hasMessage()) note.setTextNote(request.getMessage());
				note.setMeetings(EventInterface.toString(
						response.getCancelledMeetings(),
						CONSTANTS, "\n", df));
				event.getNotes().add(note);
				NoteInterface n = new NoteInterface();
				n.setDate(now);
				n.setMeetings(note.getMeetings());
				n.setUser(uname);
				n.setType(NoteInterface.NoteType.values()[note.getNoteType()]);
				n.setNote(note.getTextNote());
				if (attachment != null && !attached) {
					note.setAttachedName(attachment.getName());
					note.setAttachedFile(attachment.get());
					note.setAttachedContentType(attachment.getContentType());
					attached = true;
					n.setAttachment(attachment.getName());
				}
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
				response.setEvent(EventDetailBackend.getEventDetail(SessionDAO.getInstance().get(request.getSessionId(), hibSession), event, context));
			} else if (event.getMeetings().isEmpty()) {
				response.setEvent(EventDetailBackend.getEventDetail(SessionDAO.getInstance().get(request.getSessionId(), hibSession), event, context));
				response.getEvent().setId(null);
				hibSession.delete(event);
			} else {
				hibSession.update(event);
				response.setEvent(EventDetailBackend.getEventDetail(SessionDAO.getInstance().get(request.getSessionId(), hibSession), event, context));
			}
			
			tx.commit();
			
			new EventEmail(request, response).send(context);
			
			return response;
		} catch (Exception ex) {
			tx.rollback();
			if (ex instanceof GwtRpcException) throw (GwtRpcException)ex;
			throw new GwtRpcException(ex.getMessage(), ex);
		}
	}
	
	private List<MeetingConflictInterface> computeConflicts(org.hibernate.Session hibSession, MeetingInterface meeting, Long eventId) {
		List<MeetingConflictInterface> conflicts = new ArrayList<EventInterface.MeetingConflictInterface>();
		for (Meeting m: (List<Meeting>)hibSession.createQuery(
				"select m from Meeting m, Location l "+
				"where m.startPeriod < :stopTime and m.stopPeriod > :startTime and l.ignoreRoomCheck = false and " +
				"m.locationPermanentId = l.permanentId and l.uniqueId = :locationdId and m.meetingDate = :meetingDate and m.uniqueId != :meetingId and m.event.uniqueId != :eventId and m.approvalStatus <= 1")
				.setInteger("startTime", meeting.getStartSlot())
				.setInteger("stopTime", meeting.getEndSlot())
				.setDate("meetingDate", meeting.getMeetingDate())
				.setLong("locationdId", meeting.getLocation().getId())
				.setLong("meetingId", meeting.getId() == null ? -1 : meeting.getId())
				.setLong("eventId", eventId == null ? -1 : eventId)
				.list()) {
			
			MeetingConflictInterface conflict = new MeetingConflictInterface();

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
			
			conflicts.add(conflict);
		}
		return conflicts;
	}
		
}
