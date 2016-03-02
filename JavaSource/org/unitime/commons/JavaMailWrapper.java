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
package org.unitime.commons;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Tomas Muller
 */
public class JavaMailWrapper extends Email {
	private static Log sLog = LogFactory.getLog(Email.class);
	private javax.mail.Session iMailSession = null;
	private MimeMessage iMail = null;
	private Multipart iBody = null;
	
	public JavaMailWrapper() {
        Properties p = ApplicationProperties.getProperties();
        if (p.getProperty("mail.smtp.host")==null && p.getProperty("tmtbl.smtp.host")!=null)
            p.setProperty("mail.smtp.host", p.getProperty("tmtbl.smtp.host"));
        
        final String user = ApplicationProperty.EmailSmtpUser.value();
        final String password = ApplicationProperty.EmailSmtpPassword.value();
        
        Authenticator a = null;
        if (user != null && password != null) {
            p.setProperty("mail.smtp.user", user);
            p.setProperty("mail.smtp.auth", "true");
            a = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password); }
            };
        }

        iMailSession = javax.mail.Session.getDefaultInstance(p, a);
        iMail = new MimeMessage(iMailSession);
        iBody = new MimeMultipart();
	}
	
	@Override
	public void setSubject(String subject) throws MessagingException {
		iMail.setSubject(subject, "UTF-8");
	}
	
	@Override
	public void setFrom(String email, String name) throws MessagingException, UnsupportedEncodingException {
		if (email != null)
			iMail.setFrom(new InternetAddress(email, name, "UTF-8"));
	}

	@Override
	public void setReplyTo(String email, String name) throws UnsupportedEncodingException, MessagingException {
		if (email != null)
			iMail.setReplyTo(new InternetAddress[] {new InternetAddress(email, name, "UTF-8")});
	}
	
	@Override
	public void addReplyTo(String email, String name) throws UnsupportedEncodingException, MessagingException {
		if (email == null || email.isEmpty()) return;
		Address[] replyTo = iMail.getReplyTo();
		if (replyTo == null || replyTo.length == 0) {
			iMail.setReplyTo(new InternetAddress[] {new InternetAddress(email, name, "UTF-8")});
		} else {
			Address[] newReplyTo = new Address[replyTo.length + 1];
			for (int i = 0; i < replyTo.length; i++) newReplyTo[i] = replyTo[i];
			newReplyTo[replyTo.length] = new InternetAddress(email, name, "UTF-8");
			iMail.setReplyTo(newReplyTo);
		}
	}
	
	protected void addRecipient(RecipientType type, String email, String name) throws UnsupportedEncodingException, MessagingException {
		iMail.addRecipient(type, new InternetAddress(email, name, "UTF-8"));
	}
	
	@Override
	public void addRecipient(String email, String name) throws UnsupportedEncodingException, MessagingException {
		addRecipient(RecipientType.TO, email, name);
	}
	
	@Override
	public void addRecipientCC(String email, String name) throws UnsupportedEncodingException, MessagingException {
		addRecipient(RecipientType.CC, email, name);
	}

	@Override
	public void addRecipientBCC(String email, String name) throws UnsupportedEncodingException, MessagingException {
		addRecipient(RecipientType.BCC, email, name);
	}
		
	@Override
	public void setBody(String message, String type) throws MessagingException {
        MimeBodyPart text = new MimeBodyPart(); text.setContent(message, type);
       iBody.addBodyPart(text);
	}
	
	@Override
	protected void addAttachment(String name, DataHandler data) throws MessagingException {
        BodyPart attachment = new MimeBodyPart();
        attachment.setDataHandler(data);
        attachment.setFileName(name);
        attachment.setHeader("Content-ID", "<" + name + ">");
        iBody.addBodyPart(attachment);
	}
	
	@Override
	public void send() throws MessagingException, UnsupportedEncodingException {
		long t0 = System.currentTimeMillis();
		try {
			if (iMail.getFrom() == null || iMail.getFrom().length == 0)
		        setFrom(ApplicationProperty.EmailSenderAddress.value(), ApplicationProperty.EmailSenderName.value());
	        if (iMail.getReplyTo() == null || iMail.getReplyTo().length == 0)
	        	setReplyTo(ApplicationProperty.EmailReplyToAddress.value(), ApplicationProperty.EmailReplyToName.value());
	        iMail.setSentDate(new Date());
	        iMail.setContent(iBody);
	        iMail.saveChanges();
	        Transport.send(iMail);
		} finally {
			long t = System.currentTimeMillis() - t0;
			if (t > 30000)
				sLog.warn("It took " + new DecimalFormat("0.00").format(t / 1000.0) + " seconds to send an email.");
			else if (t > 5000)
				sLog.info("It took " + new DecimalFormat("0.00").format(t / 1000.0) + " seconds to send an email.");
		}
	}
	
	@Override
	public void setInReplyTo(String messageId) throws MessagingException {
		if (messageId != null)
			iMail.setHeader("In-Reply-To", messageId);
	}
	
	@Override
	public String getMessageId() throws MessagingException {
		return iMail.getHeader("Message-Id", null);
	}
}