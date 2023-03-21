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

import javax.activation.DataHandler;

import org.unitime.commons.Email;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.StudentEmailProvider;

public class BoilerConnectEmailAddress implements StudentEmailProvider {

	@Override
	public Email createEmail(OnlineSectioningServer server, OnlineSectioningHelper helper, Boolean optional) throws Exception {
		if (optional == null) {
			if (isAlwaysUse())
				return new BoilerConnectEmail(helper.getAction());
			else
				return Email.createEmail();
		} else if (optional.booleanValue()) {
			return new BoilerConnectEmail(helper.getAction());
		} else {
			return Email.createEmail();
		}
	}

	@Override
	public String getToggleCaptionIfOptional() {
		if (isAlwaysUse()) return null;
		return ApplicationProperties.getProperty("purdue.boilerconnect.toggleCaption", "Send via BoilerConnect (<i>user@boilerconnect.purdue.edu</i> email address)");
	}

	@Override
	public boolean isOptionCheckedByDefault() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.boilerconnect.toggleDefault", "true"));
	}
	
	public boolean isAlwaysUse() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.boilerconnect.isAlwaysUse", "false"));
	}
	
	static class BoilerConnectEmail extends Email {
		Email iEmail;
		String iReplyToEmail = null, iReplyToName = null;
		String iRecipientEmail = null, iRecipientName = null;
		String iSuffix;
		OnlineSectioningLog.Action.Builder iAction;
		
		BoilerConnectEmail(OnlineSectioningLog.Action.Builder action) throws Exception {
			iEmail = Email.createEmail();
			iSuffix = ApplicationProperties.getProperty("purdue.boilerconnect.oldSuffix", "@purdue.edu");
			iAction = action;
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
		
		@Override
		public void send() throws Exception {
			if (iRecipientEmail != null && iReplyToEmail != null) {
				String email = iRecipientEmail.replace(iSuffix, ApplicationProperties.getProperty("purdue.boilerconnect.newSuffix", "@boilerconnect.purdue.edu"));
				iEmail.addRecipient(email, iRecipientName);
				setFrom(iReplyToEmail, iReplyToName);
				if (iAction != null) {
					iAction.addOptionBuilder().setKey("bc-email").setValue(email);
					iAction.addOptionBuilder().setKey("bc-sender").setValue(iReplyToEmail);
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
}
