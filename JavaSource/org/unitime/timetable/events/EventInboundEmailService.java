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
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao._RootDAO;

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
			BodyPart attachement = null;
			if (message.getContent() instanceof MimeMultipart) {
				MimeMultipart multi = (MimeMultipart)message.getContent();
				for (int i = 1; i < multi.getCount(); i++) {
					if (Part.ATTACHMENT.equalsIgnoreCase(multi.getBodyPart(i).getDisposition()) && multi.getBodyPart(i).getFileName() != null) {
						attachement = multi.getBodyPart(i); break;
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
				if (line.matches("(?i)[0-9]+/[0-9]+/[0-9]+ " + ApplicationProperties.getProperty("unitime.email.sender.name", "UniTime Application") + " <" + ApplicationProperties.getProperty("unitime.email.sender") + ">")) break;
				if (line.startsWith(">") || line.isEmpty()) {
					skip = (skip == null ? "" : skip + "\n") + line;
				} else {
					text = (text == null ? "" : text + "\n") + (skip == null ? "" : skip + "\n") + line;
					skip = null;
				}
			}
			reader.close();
			
			if (text == null && attachement == null) return;
			
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
			
			if (attachement != null) {
				note.setAttachedName(attachement.getFileName());
				note.setAttachedContentType(attachement.getContentType());
				InputStream input = attachement.getInputStream();
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
