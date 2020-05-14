package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;

import org.unitime.commons.Email;
import org.unitime.commons.JavaMailWrapper;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.StudentEmailProvider;

public class BoilerConnectEmailAddress implements StudentEmailProvider {

	@Override
	public Email createEmail(OnlineSectioningServer server, OnlineSectioningHelper helper, Boolean optional) {
		if (optional == null) {
			if (isAlwaysUse())
				return new BoilerConnectEmail(helper.getAction());
			else
				return new JavaMailWrapper();
		} else if (optional.booleanValue()) {
			return new BoilerConnectEmail(helper.getAction());
		} else {
			return new JavaMailWrapper();
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

	static class BoilerConnectEmail extends JavaMailWrapper {
		String iReplyToEmail = null, iReplyToName = null;
		String iRecipientEmail = null, iRecipientName = null;
		String iSuffix;
		OnlineSectioningLog.Action.Builder iAction;
		
		BoilerConnectEmail(OnlineSectioningLog.Action.Builder action) {
			super();
			iSuffix = ApplicationProperties.getProperty("purdue.boilerconnect.oldSuffix", "@purdue.edu");
			iAction = action;
		}
		
		@Override
		public void setReplyTo(String email, String name) throws UnsupportedEncodingException, MessagingException {
			if (email.endsWith(iSuffix)) {
				iReplyToEmail = email; iReplyToName = name;
			}
			super.setReplyTo(email, name);
		}
		
		@Override
		public void addRecipient(String email, String name) throws UnsupportedEncodingException, MessagingException {
			if (iRecipientEmail == null && email.endsWith(iSuffix)) {
				iRecipientEmail = email; iRecipientName = name;
			} else {
				super.addRecipient(email, name);
			}
		}
		
		@Override
		public void send() throws MessagingException, UnsupportedEncodingException {
			if (iRecipientEmail != null && iReplyToEmail != null) {
				String email = iRecipientEmail.replace(iSuffix, ApplicationProperties.getProperty("purdue.boilerconnect.newSuffix", "@boilerconnect.purdue.edu"));
				super.addRecipient(email, iRecipientName);
				setFrom(iReplyToEmail, iReplyToName);
				if (iAction != null) {
					iAction.addOptionBuilder().setKey("bc-email").setValue(email);
					iAction.addOptionBuilder().setKey("bc-sender").setValue(iReplyToEmail);
				}
			} else if (iRecipientEmail != null) {
				super.addRecipient(iRecipientEmail, iRecipientName);
			}
			super.send();
		}
	}
}
