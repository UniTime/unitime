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
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApproveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SaveOrApproveEventRpcRequest.Operation;
import org.unitime.timetable.gwt.shared.EventInterface.SaveOrApproveEventRpcResponse;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.SessionDAO;

public class ApproveEventBackend extends EventAction<ApproveEventRpcRequest, SaveOrApproveEventRpcResponse>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public SaveOrApproveEventRpcResponse execute(ApproveEventRpcRequest request, GwtRpcHelper helper, EventRights rights) {
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
	        String uname = EventPropertiesBackend.lookupMainContact(request.getSessionId(), helper.getUser()).getShortName();
	        
	        meetings: for (Iterator<Meeting> i = event.getMeetings().iterator(); i.hasNext(); ) {
        		Meeting meeting = i.next();
    			for (MeetingInterface m: request.getMeetings()) {
    				if (meeting.getUniqueId().equals(m.getId())) {
    					response.addUpdatedMeeting(m);
    					
    					switch (request.getOperation()) {
    					case REJECT:
    						if (!rights.canApprove(meeting))
    							throw new GwtRpcException(MESSAGES.failedApproveEventNoRightsToReject(toString(meeting)));
    						
    						hibSession.delete(meeting);
    						i.remove();
    						
    						break;
    					case APPROVE:
    						if (!rights.canApprove(meeting))
    							throw new GwtRpcException(MESSAGES.failedApproveEventNoRightsToApprove(toString(meeting)));
    						
    						meeting.setApprovedDate(now);
    						m.setApprovalDate(now);
    						hibSession.saveOrUpdate(meeting);
    						
    						break;
    					}
    					
    					continue meetings;
    				}
    			}
	        }
	        
			final FileItem uploaded = helper.getLastUploadedFile();
			
			EventNote note = new EventNote();
			note.setEvent(event);
			note.setNoteType(request.getOperation() == Operation.APPROVE ? EventNote.sEventNoteTypeApproval : request.getOperation() == Operation.REJECT ? EventNote.sEventNoteTypeRejection : EventNote.sEventNoteTypeInquire);
			note.setTimeStamp(now);
			note.setUser(uname);
			note.setMeetings(EventInterface.toString(
					response.getUpdatedMeetings(),
					CONSTANTS,
					"\n",
					new EventInterface.DateFormatter() {
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
					}));
			if (request.hasMessage())
				note.setTextNote(request.getMessage() + (uploaded == null ? "" : "\n\n" + MESSAGES.noteAttachement(uploaded.getName())));
			else if (uploaded != null)
				note.setTextNote(MESSAGES.noteAttachement(uploaded.getName()));
			event.getNotes().add(note);
			hibSession.saveOrUpdate(note);
			
			NoteInterface n = new NoteInterface();
			n.setDate(now);
			n.setMeetings(note.getMeetings());
			n.setUser(uname);
			n.setType(NoteInterface.NoteType.values()[note.getNoteType()]);
			n.setNote(request.getMessage());
			response.addNote(n);
			
			if (event.getMeetings().isEmpty()) {
				response.setEvent(EventDetailBackend.getEventDetail(SessionDAO.getInstance().get(request.getSessionId(), hibSession), event, rights));
				response.getEvent().setId(null);
				hibSession.delete(event);
			} else {
				hibSession.update(event);
				response.setEvent(EventDetailBackend.getEventDetail(session, event, rights));
			}
			
			new EventEmail(request, response).send(helper);
			
			tx.commit(); tx = null;
			
			return response;
		} catch (Exception ex) {
			if (tx != null) tx.rollback();
			if (ex instanceof GwtRpcException) throw (GwtRpcException)ex;
			throw new GwtRpcException(ex.getMessage(), ex);
		}
	}
	
}
