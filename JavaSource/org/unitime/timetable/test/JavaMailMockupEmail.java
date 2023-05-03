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
package org.unitime.timetable.test;

import java.io.UnsupportedEncodingException;

import jakarta.mail.MessagingException;
import jakarta.mail.Address;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;

import org.unitime.commons.JavaMailWrapper;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Tomas Muller
 */
public class JavaMailMockupEmail extends JavaMailWrapper {
	private String iSubject = null;
	
	@Override
	public void setSubject(String subject) throws MessagingException {
		iSubject = subject;
		super.setSubject(ApplicationProperties.getProperty("unitime.email.test.subject", "[TEST]") + " " + subject);
	}
	
	protected void space(StringBuffer s, int len) {
		for (int i = 0; i < len; i++)
			s.append(' ');
	}
	
	protected String format(String name, Address... list) {
		if (list == null || list.length == 0) return "";
		StringBuffer ret = new StringBuffer();
		for (int i = 0; i < list.length; i++) {
			if (i == 0)
				ret.append(name);
			else
				space(ret, name.length());
			ret.append(list[i].toString());
			ret.append('\n');
		}
		return ret.toString();
	}
	
	@Override
	public void send() throws MessagingException, UnsupportedEncodingException {
		StringBuffer message = new StringBuffer("\n");
		if (iSubject != null)
			message.append(   "Subject:  " + iSubject + "\n");
		message.append(format("From:     ", iMail.getFrom()));
		message.append(format("Reply-To: ", iMail.getReplyTo()));
		message.append(format("To:       ", iMail.getRecipients(RecipientType.TO)));
		message.append(format("CC:       ", iMail.getRecipients(RecipientType.CC)));
		message.append(format("BCC:      ", iMail.getRecipients(RecipientType.BCC)));
		MimeBodyPart text = new MimeBodyPart(); text.setContent(message.toString(), "text/plain");
		text.setFileName("original-recipients.txt");
		iBody.addBodyPart(text);
		
		iMail.setRecipients(RecipientType.TO, new InternetAddress[] {
				new InternetAddress(
						ApplicationProperties.getProperty("unitime.email.test.recipient", ApplicationProperty.EmailNotificationAddress.value()),
						ApplicationProperties.getProperty("unitime.email.test.recipient.name", ApplicationProperty.EmailNotificationAddressName.value()),
						"UTF-8")
		});
		iMail.setRecipients(RecipientType.CC, new Address[] {});
		iMail.setRecipients(RecipientType.BCC, new Address[] {});
		
		super.send();

	}
}
