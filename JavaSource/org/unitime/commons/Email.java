/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.commons;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
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

import org.unitime.timetable.ApplicationProperties;

public class Email {
	private javax.mail.Session iMailSession = null;
	private MimeMessage iMail = null;
	private Multipart iBody = null;
	
	public Email() {
        Properties p = ApplicationProperties.getProperties();
        if (p.getProperty("mail.smtp.host")==null && p.getProperty("tmtbl.smtp.host")!=null)
            p.setProperty("mail.smtp.host", p.getProperty("tmtbl.smtp.host"));
        
        Authenticator a = null;
        if (ApplicationProperties.getProperty("tmtbl.mail.user")!=null && ApplicationProperties.getProperty("tmtbl.mail.pwd")!=null) {
            p.setProperty("mail.smtp.user", ApplicationProperties.getProperty("tmtbl.mail.user"));
            p.setProperty("mail.smtp.auth", "true");
            a = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            ApplicationProperties.getProperty("tmtbl.mail.user"),
                            ApplicationProperties.getProperty("tmtbl.mail.pwd"));
                }
            };
        }
        iMailSession = javax.mail.Session.getDefaultInstance(p, a);
        iMail = new MimeMessage(iMailSession);
        iBody = new MimeMultipart();
	}
	
	public void setSubject(String subject) throws MessagingException {
		iMail.setSubject(subject);
	}
	
	private void setFrom(String email, String name) throws MessagingException, UnsupportedEncodingException {
		if (email != null)
			iMail.setFrom(new InternetAddress(email, name));
	}
	
	private void addRecipient(RecipientType type, String email, String name) throws UnsupportedEncodingException, MessagingException {
		iMail.addRecipient(type, new InternetAddress(email, name));
	}
	
	public void addRecipient(String email, String name) throws UnsupportedEncodingException, MessagingException {
		addRecipient(RecipientType.TO, email, name);
	}
	
	public void addRecipientCC(String email, String name) throws UnsupportedEncodingException, MessagingException {
		addRecipient(RecipientType.CC, email, name);
	}

	public void addRecipientBCC(String email, String name) throws UnsupportedEncodingException, MessagingException {
		addRecipient(RecipientType.BCC, email, name);
	}
	
	public void setText(String message) throws MessagingException {
        MimeBodyPart text = new MimeBodyPart(); text.setContent(message, "text/plain");
       iBody.addBodyPart(text);
	}
	
	public void setHTML(String message) throws MessagingException {
        MimeBodyPart text = new MimeBodyPart(); text.setContent(message, "text/html");
       iBody.addBodyPart(text);
	}

	public void addNotify(RecipientType type) throws MessagingException, UnsupportedEncodingException {
		iMail.addRecipient(RecipientType.TO, new InternetAddress(
				ApplicationProperties.getProperty("tmtbl.notif.email", ApplicationProperties.getProperty("tmtbl.notif.commit.email")),
				ApplicationProperties.getProperty("tmtbl.notif.email.name", "UniTime Operator")));
	}

	public void addNotify() throws MessagingException, UnsupportedEncodingException {
		addNotify(RecipientType.TO);
	}
	
	public void addNotifyCC() throws MessagingException, UnsupportedEncodingException {
		addNotify(RecipientType.CC);
	}
	
	public void addAttachement(File file, String name) throws MessagingException {
        BodyPart attachement = new MimeBodyPart();
        attachement.setDataHandler(new DataHandler(new FileDataSource(file)));
        attachement.setFileName(name == null ? file.getName() : name);
        iBody.addBodyPart(attachement);
	}

	public void send() throws MessagingException, UnsupportedEncodingException {
        setFrom(ApplicationProperties.getProperty("tmtbl.inquiry.sender", ApplicationProperties.getProperty("tmtbl.contact.email")),
        		ApplicationProperties.getProperty("tmtbl.inquiry.sender.name", ApplicationProperties.getProperty("tmtbl.contact.email.name", "UniTime Email")));
        iMail.setSentDate(new Date());
        iMail.setContent(iBody);
        Transport.send(iMail);
	}

}
