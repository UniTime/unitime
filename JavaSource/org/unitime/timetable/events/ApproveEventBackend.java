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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.fileupload.FileItem;
import org.apache.struts.upload.FormFile;
import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.LookupServlet;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApproveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.ApproveEventRpcRequest.Operation;
import org.unitime.timetable.gwt.shared.EventInterface.EventDetailRpcRequest;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.webutil.EventEmail;

public class ApproveEventBackend extends EventAction<ApproveEventRpcRequest, EventInterface>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public EventInterface execute(ApproveEventRpcRequest request, GwtRpcHelper helper, EventRights rights) {
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			Event event = (request.getEventId() == null ? null : EventDAO.getInstance().get(request.getEventId(), hibSession));
			if (event == null)
				throw new GwtRpcException(MESSAGES.failedApproveEventNoEvent());
			
			TimetableManager manager = TimetableManager.getManager(helper.getUser());
			Date now = new Date();
			
	        String uname = helper.getUser().getName();
	        if (helper.getUser().getId().equals(event.getMainContact().getExternalUniqueId())) {
	        	uname = event.getMainContact().getShortName();
	        } else if (manager != null) {
	        	uname = manager.getShortName();
	        } else {
				List<PersonInterface> people = new LookupServlet().lookupPeople(helper.getUser().getName(), "mustHaveExternalId,session=" + request.getSessionId());
				if (people != null)
					for (PersonInterface p: people) {
						if (helper.getUser().getId().equals(p.getId())) {
							uname = p.getShortName();
							break;
						}
					}
	        }
	        
			TreeSet<Meeting> meetings = new TreeSet<Meeting>();
			meetings: for (Iterator<Meeting> i = event.getMeetings().iterator(); i.hasNext(); ) {
				Meeting meeting = i.next();
				if (request.hasMeetingIds() && request.getMeetingIds().contains(meeting.getUniqueId())) {
					meetings.add(meeting);
					switch (request.getOperation()) {
					case REJECT:
						if (!rights.canApprove(meeting))
							throw new GwtRpcException(MESSAGES.failedApproveEventNoRightsToReject(toString(meeting)));
						
						hibSession.delete(meeting);
						i.remove();

						continue meetings;
					case APPROVE:
						if (!rights.canApprove(meeting))
							throw new GwtRpcException(MESSAGES.failedApproveEventNoRightsToApprove(toString(meeting)));
						
						meeting.setApprovedDate(now);
						hibSession.saveOrUpdate(meeting);
						
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
			note.setMeetingCollection(meetings);
			if (request.hasMessage())
				note.setTextNote(request.getMessage() + (uploaded == null ? "" : "\n\n" + MESSAGES.noteAttachement(uploaded.getName())));
			else if (uploaded != null)
				note.setTextNote(MESSAGES.noteAttachement(uploaded.getFieldName()));
			event.getNotes().add(note);
			hibSession.saveOrUpdate(note);
			
			if (event.getMeetings().isEmpty()) {
				hibSession.delete(event);
			} else {
				hibSession.update(event);
			}
			
			FormFile attachement = null;
			if (uploaded != null) {
				helper.clearLastUploadedFile();
				attachement = new FormFile() {
					@Override
					public void setFileSize(int fileSize) {}
					
					@Override
					public void setFileName(String fileName) {}
					
					@Override
					public void setContentType(String contentType) {}
					
					@Override
					public InputStream getInputStream() throws FileNotFoundException, IOException { return uploaded.getInputStream(); }

					@Override
					public int getFileSize() { return (int)uploaded.getSize(); }
					
					@Override
					public String getFileName() { return uploaded.getName(); }
					
					@Override
					public byte[] getFileData() throws FileNotFoundException, IOException { return uploaded.get(); }
					
					@Override
					public String getContentType() { return uploaded.getContentType(); }
					
					@Override
					public void destroy() { uploaded.delete(); }
				};
			}
			
			EventEmail.Result emailResult = new EventEmail(event,
					(request.getOperation() == Operation.APPROVE ? EventEmail.sActionApprove : request.getOperation() == Operation.REJECT ? EventEmail.sActionReject : EventEmail.sActionInquire),
					Event.getMultiMeetings(meetings),
					request.getMessage(),
					attachement).send(helper.getRequestUrl());
			
			tx.commit(); tx = null;
			
			EventInterface result = new EventInterface();
			if (!event.getMeetings().isEmpty())
				result = new EventDetailBackend().execute(EventDetailRpcRequest.requestEventDetails(request.getSessionId(), request.getEventId()), helper, rights);
			
			result.setMessage((emailResult.isWarning() ? "WARN:" : "") + emailResult.getMessage());
			
			return result;
		} catch (Exception ex) {
			if (tx != null) tx.rollback();
			if (ex instanceof GwtRpcException) throw (GwtRpcException)ex;
			throw new GwtRpcException(ex.getMessage(), ex);
		}
	}
	
}
