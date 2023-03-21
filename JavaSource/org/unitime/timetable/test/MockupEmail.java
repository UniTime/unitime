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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.commons.Email;

/**
 * @author Tomas Muller
 */
public class MockupEmail extends Email {
	private static Log sLog = LogFactory.getLog(MockupEmail.class);
	
	private String iSubject = null;
	private String iFrom = null;
	private List<String> iReplyTo = new ArrayList<String>();
	private List<String> iRecipient = new ArrayList<String>();
	private List<String> iRecipientCC = new ArrayList<String>();
	private List<String> iRecipientBCC = new ArrayList<String>();
	private String iBody = null;
	private List<String> iAttachments = new ArrayList<String>();
	
	@Override
	public void setSubject(String subject) throws Exception {
		iSubject = subject;
	}
	
	protected String format(String email, String name) {
		if (name == null || name.isEmpty()) return email;
		return name + " <" + email + ">";
	}

	@Override
	public void setFrom(String email, String name) throws Exception {
		iFrom = format(email, name);
	}

	@Override
	public void setReplyTo(String email, String name) throws Exception {
		iReplyTo.clear(); iReplyTo.add(format(email, name));
	}

	@Override
	public void addReplyTo(String email, String name) throws Exception {
		iReplyTo.add(format(email, name));
	}

	@Override
	public void addRecipient(String email, String name) throws Exception {
		iRecipient.add(format(email, name));
	}

	@Override
	public void addRecipientCC(String email, String name) throws Exception {
		iRecipientCC.add(format(email, name));
	}

	@Override
	public void addRecipientBCC(String email, String name) throws Exception {
		iRecipientBCC.add(format(email, name));
	}

	@Override
	public void setBody(String message, String type) throws Exception {
		iBody = message;
	}

	@Override
	public void addAttachment(String name, DataHandler data) throws Exception {
		int size = 0, read = 0;
		byte[] buffer = new byte[1024];
		InputStream in = data.getInputStream();
		while ((read = in.read(buffer)) > 0)
			size += read;
		in.close();
		iAttachments.add(name + " (" + data.getContentType() + ", " + size + " bytes)");
	}
	
	protected void space(StringBuffer s, int len) {
		for (int i = 0; i < len; i++)
			s.append(' ');
	}
	
	protected String format(String name, List<String> list) {
		if (list == null || list.isEmpty()) return "";
		StringBuffer ret = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			if (i == 0)
				ret.append(name);
			else
				space(ret, name.length());
			ret.append(list.get(i));
			ret.append('\n');
		}
		return ret.toString();
	}
	
	@Override
	public void send() throws Exception {
		StringBuffer message = new StringBuffer("\n");
		if (iSubject != null)
			message.append(   "Subject:  " + iSubject + "\n");
		if (iFrom != null)
			message.append(   "From:     " + iFrom + "\n");
		message.append(format("Reply-To: ", iReplyTo));
		message.append(format("To:       ", iRecipient));
		message.append(format("CC:       ", iRecipientCC));
		message.append(format("BCC:      ", iRecipientBCC));
		message.append(format("Attached: ", iAttachments));
		if (iBody != null)
			message.append(iBody);
		message.append("\n----- END OF EMAIL MESSAGE -----------------------------");
		sLog.info(message.toString());
	}

	@Override
	public void setInReplyTo(String messageId) throws Exception {}

	@Override
	public String getMessageId() throws Exception { return "MESSAGE-ID"; }

}
