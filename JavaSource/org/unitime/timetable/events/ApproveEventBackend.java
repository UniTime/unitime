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

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApproveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SaveOrApproveEventRpcResponse;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ApproveEventRpcRequest.class)
public class ApproveEventBackend extends EventAction<ApproveEventRpcRequest, SaveOrApproveEventRpcResponse>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public SaveOrApproveEventRpcResponse execute(ApproveEventRpcRequest request, EventContext context) {
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			Session session = SessionDAO.getInstance().get(request.getSessionId(), hibSession);
			SaveOrApproveEventRpcResponse response = new SaveOrApproveEventRpcResponse();
			
			Event event = (request.getEvent() == null || request.getEvent().getId() == null ? null : EventDAO.getInstance().get(request.getEvent().getId(), hibSession));
			if (event == null)
				throw new GwtRpcException(MESSAGES.failedApproveEventNoEvent());
			
			if (!request.hasMeetings())
				throw new GwtRpcException(MESSAGES.failedApproveEventNoMeetings());

			Date now = new Date();
			int firstDayOfWeek = ApplicationProperty.EventGridStartDay.intValue();
	        
	        Set<Meeting> affectedMeetings = new HashSet<Meeting>();
	        meetings: for (Iterator<Meeting> i = event.getMeetings().iterator(); i.hasNext(); ) {
        		Meeting meeting = i.next();
    			for (MeetingInterface m: request.getMeetings()) {
    				if (meeting.getUniqueId().equals(m.getId())) {
    					response.addUpdatedMeeting(m);
    					affectedMeetings.add(meeting);
    					
    					switch (request.getOperation()) {
    					case REJECT:
    						if (!context.hasPermission(meeting, Right.EventMeetingApprove))
    							throw new GwtRpcException(MESSAGES.failedApproveEventNoRightsToReject(toString(meeting)));
    						
    						// hibSession.delete(meeting);
    						// i.remove();
    						meeting.setStatus(Meeting.Status.REJECTED);
    						meeting.setApprovalDate(now);
    						m.setApprovalDate(now);
    						m.setApprovalStatus(meeting.getApprovalStatus());
    						hibSession.saveOrUpdate(meeting);
    						
    						break;
    					case APPROVE:
    						if (!context.hasPermission(meeting, Right.EventMeetingApprove))
    							throw new GwtRpcException(MESSAGES.failedApproveEventNoRightsToApprove(toString(meeting)));
    						
    						meeting.setStatus(Meeting.Status.APPROVED);
    						meeting.setApprovalDate(now);
    						m.setApprovalDate(now);
    						m.setApprovalStatus(meeting.getApprovalStatus());
    						hibSession.saveOrUpdate(meeting);
    						
    						break;
    					case CANCEL:
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
    						m.setApprovalDate(now);
    						m.setApprovalStatus(meeting.getApprovalStatus());
    						hibSession.saveOrUpdate(meeting);

    						break;
    					}
    					
    					continue meetings;
    				}
    			}
	        }
	        
			EventNote note = new EventNote();
			note.setEvent(event);
			switch (request.getOperation()) {
			case APPROVE:
				note.setNoteType(EventNote.sEventNoteTypeApproval);
				break;
			case REJECT:
				note.setNoteType(EventNote.sEventNoteTypeRejection);
				break;
			case CANCEL:
				note.setNoteType(EventNote.sEventNoteTypeCancel);
				break;
			default:
				note.setNoteType(EventNote.sEventNoteTypeInquire);
			}
			note.setTimeStamp(now);
			note.setUser(context.getUser().getTrueName());
			note.setUserId(context.getUser().getTrueExternalUserId());
			note.setAffectedMeetings(affectedMeetings);
			note.setMeetings(EventInterface.toString(firstDayOfWeek,
					response.getUpdatedMeetings(),
					CONSTANTS,
					"\n",
					new EventInterface.DateFormatter() {
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
					}));
			if (request.hasMessage())
				note.setTextNote(request.getMessage());
			if (note.getTextNote() != null && note.getTextNote().length() > 2000)
				note.setTextNote(note.getTextNote().substring(0, 2000));
			
			FileItem attachment = (FileItem)context.getAttribute(SessionAttribute.LastUploadedFile);
			if (attachment != null) {
				note.setAttachedName(attachment.getName());
				note.setAttachedFile(attachment.get());
				note.setAttachedContentType(attachment.getContentType());
			}
			
			event.getNotes().add(note);
			hibSession.saveOrUpdate(note);
			
			NoteInterface n = new NoteInterface();
			n.setId(note.getUniqueId());
			n.setDate(now);
			n.setMeetings(note.getMeetings());
			n.setUser(context.getUser().getTrueName());
			n.setType(NoteInterface.NoteType.values()[note.getNoteType()]);
			n.setNote(request.getMessage());
			n.setAttachment(attachment == null ? null : attachment.getName());
			n.setLink(attachment == null ? null : QueryEncoderBackend.encode("event=" + event.getUniqueId() + "&note=" + note.getUserId()));
			response.addNote(n);
			
			if (event.getMeetings().isEmpty()) {
				response.setEvent(EventDetailBackend.getEventDetail(SessionDAO.getInstance().get(request.getSessionId(), hibSession), event, context));
				response.getEvent().setId(null);
				hibSession.delete(event);
			} else {
				hibSession.update(event);
				response.setEvent(EventDetailBackend.getEventDetail(session, event, context));
			}
			
			tx.commit(); tx = null;
			
			new EventEmail(request, response).send(context);
			
			return response;
		} catch (Exception ex) {
			if (tx != null) tx.rollback();
			if (ex instanceof GwtRpcException) throw (GwtRpcException)ex;
			throw new GwtRpcException(ex.getMessage(), ex);
		}
	}
	
}
