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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
@Service("eventInboundEmailService")
public class EventInboundEmailService {
	private Pattern iSubjectPattern = Pattern.compile("(?i)\\[EVENT-([0-9a-f]+)\\]");
	private static Logger sLog = Logger.getLogger(EventInboundEmailService.class);
	
	public void process(MimeMessage message) throws MessagingException, IOException {
		sLog.info("Recieved message: " + message.getSubject());
 		Matcher subjectMatcher = iSubjectPattern.matcher(message.getSubject());
		if (!subjectMatcher.find()) return;
		org.hibernate.Session hibSession = new _RootDAO().createNewSession();
		try {
			Event event = EventDAO.getInstance().get(Long.parseLong(subjectMatcher.group(1), 16), hibSession);
			if (event == null) return;
			String body = null;
			if (message.getContent() instanceof String) {
				body = (String)message.getContent();
			} else if (message.getContent() instanceof MimeMultipart) {
				MimeMultipart multi = (MimeMultipart)message.getContent();
				if (multi.getBodyPart(0).getContent() instanceof MimeMultipart && ((MimeMultipart)multi.getBodyPart(0).getContent()).getBodyPart(0).getContent() instanceof String) {
					body = (String)((MimeMultipart)multi.getBodyPart(0).getContent()).getBodyPart(0).getContent();
				} else if (multi.getBodyPart(0).getContent() instanceof String) {
					body = (String)multi.getBodyPart(0).getContent();
				}
			}
			BodyPart attachment = null;
			if (message.getContent() instanceof MimeMultipart) {
				MimeMultipart multi = (MimeMultipart)message.getContent();
				for (int i = 1; i < multi.getCount(); i++) {
					if (Part.ATTACHMENT.equalsIgnoreCase(multi.getBodyPart(i).getDisposition()) && multi.getBodyPart(i).getFileName() != null) {
						attachment = multi.getBodyPart(i); break;
					}
				}
			}
			
			if (body == null) return;
			
			BufferedReader reader = new BufferedReader(new StringReader(body));
			String text = null, line = null, skip = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() && text == null && skip == null) continue;
				if (line.matches("(?i)-+ ?Original message ?-+") || line.matches("(?i)-+ ?Forwarded message ?-+") || line.matches("On .* wrote:")) break;
				if (line.matches("(?i)[0-9]+/[0-9]+/[0-9]+ " + ApplicationProperty.EmailSenderName.value() + " <" + ApplicationProperty.EmailSenderAddress.value() + ">")) break;
				if (line.startsWith(">") || line.isEmpty()) {
					skip = (skip == null ? "" : skip + "\n") + line;
				} else {
					text = (text == null ? "" : text + "\n") + (skip == null ? "" : skip + "\n") + line;
					skip = null;
				}
			}
			reader.close();
			
			if (text == null && attachment == null) return;
			
			final EventNote note = new EventNote();
			
			note.setEvent(event);
			note.setNoteType(EventNote.sEventNoteTypeEmail);
			
			Date ts = new Date();
			if (message.getReceivedDate() != null) ts = message.getReceivedDate();
			else if (message.getSentDate() != null) ts = message.getSentDate();
			note.setTimeStamp(ts);
			
			String user = "EMAIL", userId = null;
			InternetAddress from = null;
			if (message.getFrom() != null && message.getFrom().length > 0 && message.getFrom()[0] instanceof InternetAddress) {
				from = (InternetAddress)message.getFrom()[0];
				if (from.getPersonal() != null) user = from.getPersonal(); else user = from.getAddress();
			}
			note.setUser(user);
			note.setUserId(userId);
			
			note.setTextNote(text);
			if (note.getTextNote() != null && note.getTextNote().length() > 2000)
				note.setTextNote(note.getTextNote().substring(0, 2000));
			
			if (attachment != null) {
				note.setAttachedName(attachment.getFileName());
				note.setAttachedContentType(attachment.getContentType());
				InputStream input = attachment.getInputStream();
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len;
				while ((len = input.read(buffer)) != -1) bytes.write(buffer, 0, len);
				input.close(); bytes.flush(); bytes.close();
				note.setAttachedFile(bytes.toByteArray());
			}
			
			event.getNotes().add(note);
			hibSession.saveOrUpdate(note);
		
			hibSession.update(event);
			hibSession.flush();
			
			DataSource data = null;
			if (note.getAttachedFile() != null) {
				data = new DataSource() {
					
					@Override
					public OutputStream getOutputStream() throws IOException {
						return null;
					}
					
					@Override
					public String getName() {
						return note.getAttachedName();
					}
					
					@Override
					public InputStream getInputStream() throws IOException {
						return new ByteArrayInputStream(note.getAttachedFile());
					}
					
					@Override
					public String getContentType() {
						return note.getAttachedContentType();
					}
				};
			}
			
			try {
				EventEmail.eventUpdated(event, text, from, data);
			} catch (Exception e) {
				sLog.error("Failed to sent confirmation email: " + e.getMessage(), e);
			}
		} finally {
			hibSession.close();
		}
	}
}
