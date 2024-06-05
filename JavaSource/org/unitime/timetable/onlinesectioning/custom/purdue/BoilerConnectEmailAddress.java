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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import jakarta.activation.DataHandler;

import org.unitime.commons.Email;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.StudentEmailProvider;

public class BoilerConnectEmailAddress implements StudentEmailProvider {

	@Override
	public Email createEmail(OnlineSectioningServer server, OnlineSectioningHelper helper, Boolean optional, String operation) throws Exception {
		if (optional == null) {
			if (isAlwaysUse(operation))
				return new BoilerConnectEmail(helper.getAction(), operation);
			else
				return Email.createEmail();
		} else if (optional.booleanValue()) {
			return new BoilerConnectEmail(helper.getAction(), operation);
		} else {
			return Email.createEmail();
		}
	}

	@Override
	public String getToggleCaptionIfOptional() {
		return ApplicationProperties.getProperty("purdue.boilerconnect.toggleCaption", "Send via BoilerConnect (<i>user@boilerconnect.purdue.edu</i> email address)");
	}

	@Override
	public boolean isOptionCheckedByDefault() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.boilerconnect.toggleDefault", "true"));
	}
	
	public boolean isAlwaysUse(String operation) {
		return "true".equalsIgnoreCase(
				ApplicationProperties.getProperty("purdue.boilerconnect." + operation + ".enabled",
						ApplicationProperties.getProperty("purdue.boilerconnect.isAlwaysUse", "false"))
				);
	}
	
	static class BoilerConnectEmail extends Email {
		Email iEmail;
		String iReplyToEmail = null, iReplyToName = null;
		String iRecipientEmail = null, iRecipientName = null;
		String iSuffix;
		String iOperation;
		OnlineSectioningLog.Action.Builder iAction;
		
		BoilerConnectEmail(OnlineSectioningLog.Action.Builder action, String operation) throws Exception {
			iEmail = Email.createEmail();
			iSuffix = ApplicationProperties.getProperty("purdue.boilerconnect.oldSuffix", "@purdue.edu");
			iAction = action;
			iOperation = operation;
		}
		
		@Override
		public void setReplyTo(String email, String name) throws Exception {
			if (email.endsWith(iSuffix)) {
				iReplyToEmail = email; iReplyToName = name;
			}
			iEmail.setReplyTo(email, name);
		}
		
		@Override
		public void addRecipient(String email, String name) throws Exception {
			if (iRecipientEmail == null && email.endsWith(iSuffix)) {
				iRecipientEmail = email; iRecipientName = name;
			} else {
				iEmail.addRecipient(email, name);
			}
		}
		
		protected String getSenderName() {
			return ApplicationProperties.getProperty("purdue.boilerconnect.senderName");
		}
		
		protected String getSenderEmail() {
			return ApplicationProperties.getProperty("purdue.boilerconnect.senderEmail");
		}
		
		@Override
		public void send() throws Exception {
			if (iRecipientEmail != null && iReplyToEmail != null && (iOperation == null || iOperation.startsWith("user-"))) {
				String email = iRecipientEmail.replace(iSuffix, ApplicationProperties.getProperty("purdue.boilerconnect.newSuffix", "@boilerconnect.purdue.edu"));
				iEmail.addRecipient(email, iRecipientName);
				setFrom(iReplyToEmail, iReplyToName);
				if (iAction != null) {
					iAction.addOptionBuilder().setKey("bc-email").setValue(email);
					iAction.addOptionBuilder().setKey("bc-sender").setValue(iReplyToEmail);
				}
			} else if (iRecipientEmail != null && iOperation != null && !iOperation.startsWith("user-")) {
				String email = iRecipientEmail.replace(iSuffix, ApplicationProperties.getProperty("purdue.boilerconnect.newSuffix", "@boilerconnect.purdue.edu"));
				iEmail.addRecipient(email, iRecipientName);
				if (iAction != null)
					iAction.addOptionBuilder().setKey("bc-email").setValue(email);
				String sender = getSenderEmail();
				if (sender != null && !sender.isEmpty()) {
					setFrom(sender, getSenderName());
					if (iAction != null)
						iAction.addOptionBuilder().setKey("bc-sender").setValue(sender);
				}
			} else if (iRecipientEmail != null) {
				iEmail.addRecipient(iRecipientEmail, iRecipientName);
			}
			iEmail.send();
		}

		@Override
		public void setSubject(String subject) throws Exception {
			iEmail.setSubject(subject);
		}

		@Override
		public void setFrom(String email, String name) throws Exception {
			iEmail.setFrom(email, name);
		}

		@Override
		public void addReplyTo(String email, String name) throws Exception {
			iEmail.addReplyTo(email, name);
		}

		@Override
		public void addRecipientCC(String email, String name) throws Exception {
			iEmail.addRecipientCC(email, name);
		}

		@Override
		public void addRecipientBCC(String email, String name) throws Exception {
			iEmail.addRecipientBCC(email, name);
		}

		@Override
		public void setBody(String message, String type) throws Exception {
			iEmail.setBody(message, type);
		}

		@Override
		public void addAttachment(String name, DataHandler data) throws Exception {
			iEmail.addAttachment(name, data);
		}

		@Override
		public void setInReplyTo(String messageId) throws Exception {
			iEmail.setInReplyTo(messageId);
		}

		@Override
		public String getMessageId() throws Exception {
			return iEmail.getMessageId();
		}
	}

	@Override
	public boolean isPlainText() {
		return true;
	}
}
