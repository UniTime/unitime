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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.command.server.GwtRpcServlet;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.UploadServlet;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApproveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcRequest;
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

@GwtRpcImplements(ApproveEventRpcRequest.class)
public class ApproveEventBackend extends EventAction<ApproveEventRpcRequest, SaveOrApproveEventRpcResponse>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private @Autowired ApplicationContext applicationContext;
	
	@Override
	public SaveOrApproveEventRpcResponse execute(ApproveEventRpcRequest request, EventContext context) {
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
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
			String uname = GwtRpcServlet.execute(new EventPropertiesRpcRequest(request.getSessionId()), applicationContext, context).getMainContact().getShortName();
	        
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
			note.setUser(uname);
			note.setUserId(context.getUser().getExternalUserId());
			note.setAffectedMeetings(affectedMeetings);
			note.setMeetings(EventInterface.toString(
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
			
			FileItem attachment = (FileItem)context.getAttribute(UploadServlet.SESSION_LAST_FILE);
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
			n.setUser(uname);
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
