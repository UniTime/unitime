/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.struts.upload.FormFile;
import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Tomas Muller
 */
public abstract class Email {

	public static Email createEmail() throws Exception {
		return (Email)Class.forName(ApplicationProperty.EmailProvider.value()).newInstance();
	}
	
	public abstract void setSubject(String subject) throws Exception;
	
	public abstract void setFrom(String email, String name) throws Exception;
	
	public abstract void setReplyTo(String email, String name) throws Exception;
	
	public abstract void addReplyTo(String email, String name) throws Exception;
	
	public abstract void addRecipient(String email, String name) throws Exception;
	
	public abstract void addRecipientCC(String email, String name) throws Exception;

	public abstract void addRecipientBCC(String email, String name) throws Exception;
	
	protected abstract void setBody(String message, String type) throws Exception;
	
	public void setText(String message) throws Exception {
		setBody(message, "text/plain; charset=UTF-8");
	}
	
	public void setHTML(String message) throws Exception {
		setBody(message, "text/html; charset=UTF-8");
	}

	public void addNotify() throws Exception {
		addRecipient(ApplicationProperty.EmailNotificationAddress.value(), ApplicationProperty.EmailNotificationAddressName.value());
	}
	
	public void addNotifyCC() throws Exception {
		addRecipientCC(ApplicationProperty.EmailNotificationAddress.value(), ApplicationProperty.EmailNotificationAddressName.value());
	}
	
	protected abstract void addAttachement(String name, DataHandler data) throws Exception;
	
	public void addAttachement(File file, String name) throws Exception {
		addAttachement(name == null ? file.getName() : name, new DataHandler(new FileDataSource(file)));
	}

	public void addAttachement(final FormFile file) throws Exception {
		addAttachement(file.getFileName(), 
				new DataHandler(new DataSource() {
					@Override
					public OutputStream getOutputStream() throws IOException {
						throw new IOException("No output stream.");
					}
					
					@Override
					public String getName() {
						return file.getFileName();
					}
					
					@Override
					public InputStream getInputStream() throws IOException {
						return file.getInputStream();
					}
					
					@Override
					public String getContentType() {
						return file.getContentType();
					}
				}));
	}
	
	public void addAttachement(DataSource source) throws Exception {
		addAttachement(source.getName(), new DataHandler(source));
	}

	public abstract void send() throws Exception;
	
	public abstract void setInReplyTo(String messageId) throws Exception;
	
	public abstract String getMessageId() throws Exception;
}
