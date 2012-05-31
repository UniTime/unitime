/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.widgets;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;

public class UniTimeFileUpload extends FormPanel {
	private FileUpload iUpload;
	
	public UniTimeFileUpload() {
		setEncoding(ENCODING_MULTIPART);
		setMethod(METHOD_POST);
		setAction(GWT.getHostPageBaseURL() + "upload");
		
		iUpload = new FileUpload();
		iUpload.setName("file");
		setWidget(iUpload);
				
		iUpload.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				submit();
			}
		});
		
		addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String message = event.getResults();
				if (message.startsWith("ERROR:")) {
					UniTimeNotifications.error(message.substring("ERROR:".length()));
					reset();
				} else {
					UniTimeNotifications.info(message);
				}
			}
		});
		
		addSubmitHandler(new SubmitHandler() {
			@Override
			public void onSubmit(SubmitEvent event) {
				String name = iUpload.getFilename();
				if (name.indexOf('/') >= 0) name = name.substring(name.lastIndexOf('/') + 1);
				if (name.indexOf('\\') >= 0) name = name.substring(name.lastIndexOf('\\') + 1);
				if (!name.isEmpty())
					UniTimeNotifications.info("Uploading " + name + " ...");
			}
		});
	}
	
	public String getFileName() {
		return iUpload.getFilename();
	}
}
