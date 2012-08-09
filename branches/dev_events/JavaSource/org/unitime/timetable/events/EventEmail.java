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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.TimeZone;

import javax.activation.DataSource;
import javax.mail.MessagingException;

import org.apache.commons.fileupload.FileItem;
import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.export.events.EventsExportEventsToICal;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MultiMeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SaveOrApproveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.SaveOrApproveEventRpcResponse;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;

public class EventEmail {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static Map<Long, String> sMessageId = new Hashtable<Long, String>();
	
	private SaveOrApproveEventRpcRequest iRequest;
	private SaveOrApproveEventRpcResponse iResponse; 
	
	public EventEmail(SaveOrApproveEventRpcRequest request, SaveOrApproveEventRpcResponse response) {
		iRequest = request; iResponse = response;
	}
	
	public SaveOrApproveEventRpcRequest request() { return iRequest; }
	public SaveOrApproveEventRpcResponse response() { return iResponse; }
	
	
	public void send(GwtRpcHelper helper) throws UnsupportedEncodingException, MessagingException {
		try {
			if (!"true".equals(ApplicationProperties.getProperty("unitime.email.confirm.event", ApplicationProperties.getProperty("tmtbl.event.confirmationEmail","true")))) {
				response().info(MESSAGES.emailDisabled());
				return;
	        }
			
			Email email = new Email();
			if (event().hasContact() && event().getContact().getEmail() != null)
				email.addRecipient(event().getContact().getEmail(), event().getContact().getName(MESSAGES));
			if (event().hasAdditionalContacts()) {
				for (ContactInterface contact: event().getAdditionalContacts()) {
					if (contact.getEmail() != null)
						email.addRecipient(contact.getEmail(), contact.getName(MESSAGES));
				}
			}
			if (event().hasSponsor() && event().getSponsor().hasEmail())
				email.addRecipientCC(event().getSponsor().getEmail(), event().getSponsor().getName());
			if (event().hasEmail()) {
				for (String address: event().getEmail().split("\n")) {
					if (!address.trim().isEmpty())
						email.addRecipientCC(address, null);
				}
			}
			
			email.setSubject(event().getName() + " (" + event().getType().getName(CONSTANTS) + ")");
			
			if (helper.getUser() != null) {
				TimetableManager manager = (TimetableManager)TimetableManagerDAO.getInstance().getSession().createQuery("from TimetableManager where externalUniqueId = :id").setString("id", helper.getUser().getId()).uniqueResult();
				if (manager != null && manager.getEmailAddress() != null)
					email.setReplyTo(manager.getEmailAddress(), manager.getName());
			}
			
			final FileItem file = helper.getLastUploadedFile();
			if (file != null) {
				email.addAttachement(new DataSource() {
					@Override
					public OutputStream getOutputStream() throws IOException {
						throw new IOException("No output stream.");
					}
					@Override
					public String getName() {
						return file.getName();
					}
					@Override
					public InputStream getInputStream() throws IOException {
						return file.getInputStream();
					}
					@Override
					public String getContentType() {
						return file.getContentType();
					}
				});
			}
			
			final String ical = icalendar();
			if (ical != null) {
				email.addAttachement(new DataSource() {
					@Override
					public OutputStream getOutputStream() throws IOException {
						throw new IOException("No output stream.");
					}
					@Override
					public String getName() {
						return "event.ics";
					}
					@Override
					public InputStream getInputStream() throws IOException {
						return new ByteArrayInputStream(ical.getBytes("UTF-8"));
					}
					@Override
					public String getContentType() {
						return "text/calendar; charset=UTF-8";
					}
				});
			}
			
			email.setHTML(message());
			
			Long eventId = (response().hasEventWithId() ? response().getEvent().getId() : request().getEvent().getId());
			if (eventId != null) {
				String messageId = sMessageId.get(eventId);
				if (messageId != null)
					email.setInReplyTo(messageId);
			}
			
			email.send();
			
			if (eventId != null) {
				String messageId = email.getMessageId();
				if (messageId != null)
					sMessageId.put(eventId, messageId);
			}
			
			response().info(MESSAGES.infoConfirmationEmailSent(event().hasContact() ? event().getContact().getName(MESSAGES) : "?"));
		} catch (Exception e) {
			response().error(MESSAGES.failedToSendConfirmationEmail(e.getMessage()));
			e.printStackTrace();
		}
	}
	
	private EventInterface event() {
		return response().getEvent() != null ? response().getEvent() : request().getEvent();
	}
	
	private String subject() {
		switch (request().getOperation()) {
		case CREATE: 
			return MESSAGES.emailSubjectCreate(event().getName());
		case UPDATE:
			return MESSAGES.emailSubjectUpdate(event().getName());
		case DELETE:
			return MESSAGES.emailSubjectDelete(event().getName());
		case APPROVE:
			return MESSAGES.emailSubjectApprove(event().getName());
		case INQUIRE:
			return MESSAGES.emailSubjectInquire(event().getName());
		case REJECT:
			return MESSAGES.emailSubjectReject(event().getName());
		default:
			return MESSAGES.emailSubjectUpdate(event().getName());
		}
	}
	
	private String message() {
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);
		
		out.println("<html>");
		out.println("<head>");
		out.println("  <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
		out.println("	<title>" + subject() + "</title>");
		out.println("</head>");
		out.println("<body style=\"font-family: sans-serif, verdana, arial;\">");
		out.println("	<table style=\"border: 1px solid #9CB0CE; padding: 5px; margin-top: 10px; width: 800px;\" align=\"center\">");
		out.println("		<tr><td><table width=\"100%\">");
		out.println("			<tr>");
		out.println("				<td rowspan=\"2\"><img src=\"http://www.unitime.org/include/unitime.png\" border=\"0\" height=\"100px\"/></td>");
		out.println("				<td colspan=\"2\" style=\"font-size: x-large; font-weight: bold; color: #333333; text-align: right; padding: 20px 30px 10px 10px;\">" + subject() + "</td>");
		out.println("			</tr>");
		out.println("		</table></td></tr>");
		out.println("		<tr><td style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 5px; font-size: large; font-weight: bold; color: black; text-align: left;\">" + event().getName() + "</td></tr>");
		out.println("		<tr><td>");
		generateEventDetails(out);
		out.println("       </td></tr>");
		if (response().hasCreatedMeetings()) {
			out.println("		<tr><td style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;\">" + MESSAGES.emailCreatedMeetings() + "</td></tr>");
			out.println("		<tr><td>");
			generateMeetings(out, response().getCreatedMeetings(), true);
			out.println("       </td></tr>");
		}
		if (response().hasDeletedMeetings()) {
			out.println("		<tr><td style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;\">" + MESSAGES.emailDeletedMeetings() + "</td></tr>");
			out.println("		<tr><td>");
			generateMeetings(out, response().getDeletedMeetings(), false);
			out.println("       </td></tr>");
		}
		if (response().hasUpdatedMeetings()) {
			out.println("		<tr><td style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;\">");
			switch (request().getOperation()) {
			case APPROVE: out.println(MESSAGES.emailApprovedMeetings()); break;
			case REJECT: out.println(MESSAGES.emailRejectedMeetings()); break;
			case INQUIRE: out.println(MESSAGES.emailInquiredMeetings()); break;
			default: out.println(MESSAGES.emailUpdatedMeetings()); break;
			}
			out.println("       </td></tr>");
			out.println("		<tr><td>");
			generateMeetings(out, response().getUpdatedMeetings(), true);
			out.println("       </td></tr>");
		}
		if (request().hasMessage()) {
			out.println("		<tr><td style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;\">");
			switch (request().getOperation()) {
			case APPROVE: out.println(MESSAGES.emailMessageApproval()); break;
			case REJECT: out.println(MESSAGES.emailMessageReject()); break;
			case INQUIRE: out.println(MESSAGES.emailMessageInquiry()); break;
			case CREATE: out.println(MESSAGES.emailMessageCreate()); break;
			case UPDATE: out.println(MESSAGES.emailMessageUpdate()); break;
			case DELETE: out.println(MESSAGES.emailMessageDelete()); break;
			default: out.println(MESSAGES.emailMessageUpdate()); break;
			}
			out.println("       </td></tr>");
			out.println("		<tr><td>");
			out.println(request().getMessage().replace("\n", "<br>"));
			out.println("       </td></tr>");
		}
		if (request().getEvent().getId() != null) {
			out.println("		<tr><td style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;\">" +
					MESSAGES.emailAllMeetings(event().getName()) + "</td></tr>");
			out.println("		<tr><td>");
			if (event().hasMeetings()) {
				generateMeetings(out, event().getMeetings(), true);
			} else {
				out.println(MESSAGES.emailEventDeleted(event().getName()));
			}
			out.println("       </td></tr>");
		}
		if (event().hasNotes()) {
			out.println("		<tr><td style=\"width: 100%; border-bottom: 1px solid #9CB0CE; padding-top: 10px; font-size: large; font-weight: bold; color: black; text-align: left;\">" +
					MESSAGES.emailNotes(event().getName()) + "</td></tr>");
			out.println("		<tr><td>");
			generateNotes(out, event().getNotes());
			out.println("       </td></tr>");
		}
		out.println("		</td></tr>");
		out.println("	</table>");
		out.println("	<table style=\"width: 800px; margin-top: -3px;\" align=\"center\">");
		out.println("		<tr>");
		out.println("			<td width=\"33%\" align=\"left\" style=\"font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;\">" +
				MESSAGES.pageVersion(Constants.getVersion(), Constants.getReleaseDate()) + "</td>");
		out.println("			<td width=\"34%\" align=\"center\" style=\"font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;\">" +
				MESSAGES.pageCopyright() + "</td>");
		out.println("			<td width=\"33%\" align=\"right\" style=\"font-size: 9pt; vertical-align: top; font-style: italic; color: #9CB0CE; white-space: nowrap;\">" +
				new SimpleDateFormat(CONSTANTS.timeStampFormat(), Localization.getJavaLocale()).format(new Date()) + "</td>");
		out.println("		</tr>");
		out.println("	</table>");
		out.println("</body>");
		out.println("</html>");
		
		out.flush(); out.close();
		return buffer.getBuffer().toString();
	}
	
	private void generateEventDetails(PrintWriter out) {
		out.println("<table>");
		out.println("	<tr><td>" + MESSAGES.propEventType() + "</td><td>" + event().getType().getName(CONSTANTS) + "</td></tr>");
		out.println("	<tr><td>" + MESSAGES.propContacts() + "</td><td>");
		generateContacts(out);
		out.println("	</td></tr>");
		if (event().hasEmail()) {
			out.println("	<tr><td>" + MESSAGES.propAdditionalEmails() + "</td><td>" + event().getEmail().replace("\n", "<br>") + "</td></tr>");
		}
		if (event().hasSponsor()) {
			out.println("	<tr><td>" + MESSAGES.propSponsor() + "</td><td>" + event().getSponsor().getName() + "</td></tr>");
		}
		if (event().hasEnrollment()) {
			out.println("	<tr><td>" + MESSAGES.propEnrollment() + "</td><td>" + event().getEnrollment() + "</td></tr>");
		}
		if (event().hasMaxCapacity()) {
			out.println("	<tr><td>" + MESSAGES.propAttendance() + "</td><td>" + event().getMaxCapacity() + "</td></tr>");
		}
		out.println("</table>");
	}
	
	private void generateContacts(PrintWriter out) {
		out.println("<table width=\"100%\">");
		out.println("<tr>");
		String style = "white-space: nowrap; font-weight: bold;";
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colName() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colEmail() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colPhone() + "</td>");
		out.println("</tr>");
		if (event().hasContact()) {
			ContactInterface contact = event().getContact();
			out.println("<tr><td>" + contact.getName(MESSAGES) + "</td><td>" + (contact.hasEmail() ? contact.getEmail() : "") + "</td><td>" + (contact.hasPhone() ? contact.getPhone() : "") + "</td></tr>");
		}
		if (event().hasAdditionalContacts()) {
			for (ContactInterface contact: event().getAdditionalContacts())
				out.println("<tr><td>" + contact.getName(MESSAGES) + "</td><td>" + (contact.hasEmail() ? contact.getEmail() : "") + "</td><td>" + (contact.hasPhone() ? contact.getPhone() : "") + "</td></tr>");
		}
		out.println("</table>");
	}
	
	private void generateMeetings(PrintWriter out, Collection<MeetingInterface> meetings, boolean approval) {
		out.println("<table width=\"100%\">");
		out.println("<tr>");
		String style = "white-space: nowrap; font-weight: bold;";
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colDate() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colPublishedTime() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colAllocatedTime() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colLocation() + "</td>");
		if (approval) out.println("	<td style=\"" + style + "\">" + MESSAGES.colApproval() + "</td>");
		out.println("</tr>");
		DateFormat dfShort = new SimpleDateFormat(CONSTANTS.eventDateFormatShort(), Localization.getJavaLocale());
		DateFormat dfLong = new SimpleDateFormat(CONSTANTS.eventDateFormatLong(), Localization.getJavaLocale());
		DateFormat dfApproval = new SimpleDateFormat(CONSTANTS.eventDateFormat(), Localization.getJavaLocale());
		for (MultiMeetingInterface meeting: EventInterface.getMultiMeetings(meetings, approval, approval)) {
			if (meeting.isDelete()) continue;
			out.println("<tr>");
			out.println("  <td>" + meeting.getDays(CONSTANTS) + " " + (meeting.getNrMeetings() <= 1 ? dfLong.format(meeting.getFirstMeetingDate()) : dfShort.format(meeting.getFirstMeetingDate()) + " - " + dfLong.format(meeting.getLastMeetingDate())) + "</td>");
			out.println("  <td>" + meeting.getMeetings().first().getMeetingTime(CONSTANTS) + "</td>");
			out.println("  <td>" + meeting.getMeetings().first().getAllocatedTime(CONSTANTS) + "</td>");
			out.println("  <td>" + meeting.getMeetings().first().getLocationName() + "</td>");
			if (approval)
				out.println("  <td>" + (!meeting.getMeetings().first().isApproved() ? "<i>" + (meeting.getMeetings().first().isPast() ? 
						MESSAGES.approvalNotApprovedPast() : MESSAGES.approvalWaiting()) + "</i>" : dfApproval.format(meeting.getMeetings().first().getApprovalDate())) + "</td>");
			out.println("</tr>");
		}
		out.println("</table>");
	}
	
	private void generateNotes(PrintWriter out, Collection<NoteInterface> notes) {
		out.println("<table width=\"100%\" cellspacing='0' cellpadding='3'>");
		out.println("<tr>");
		String style = "white-space: nowrap; font-weight: bold;";
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colDate() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colUser() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colAction() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colMeetings() + "</td>");
		out.println("	<td style=\"" + style + "\">" + MESSAGES.colNote() + "</td>");
		out.println("</tr>");
		DateFormat df = new SimpleDateFormat(CONSTANTS.timeStampFormat(), Localization.getJavaLocale());
		for (NoteInterface note: notes) {
			style = "";
			switch (note.getType()) {
			case Approve: style = "background-color: #D7FFD7;"; break;
			case Reject: style = "background-color: #FFD7D7;"; break;
			}
			out.println("<tr style='" + style + "'>");
			out.println("  <td style='white-space: nowrap;'>" + df.format(note.getDate()) + "</td>");
			out.println("  <td style='white-space: nowrap;'>" + note.getUser() + "</td>");
			out.println("  <td style='white-space: nowrap;'>" + note.getType().getName() + "</td>");
			out.println("  <td style='white-space: nowrap;'>" + (note.getMeetings() == null ? "" : note.getMeetings()) + "</td>");
			out.println("  <td>" + (note.getNote() == null ? "" : note.getNote()).replace("\n", "<br>") + "</td>");
			out.println("</tr>");
		}
		out.println("</table>");
	}

	public String icalendar() throws IOException {
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);

		out.println("BEGIN:VCALENDAR");
        out.println("VERSION:2.0");
        out.println("CALSCALE:GREGORIAN");
        out.println("METHOD:PUBLISH");
        out.println("X-WR-CALNAME:" + event().getName());
        out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
        out.println("PRODID:-//UniTime " + Constants.getVersion() + "/Events Calendar//NONSGML v1.0//EN");
        
        boolean exp = new EventsExportEventsToICal().print(
        		out,
        		response().hasEventWithId() && response().getEvent().hasMeetings() ? response().getEvent() : request().getEvent(),
        		response().hasEventWithId() && response().getEvent().hasMeetings() ? null : EventsExportEventsToICal.ICalendarStatus.CANCELLED);
        
		out.println("END:VCALENDAR");
		
		out.flush(); out.close();
		return (exp ? buffer.getBuffer().toString() : null);		
	}

}
