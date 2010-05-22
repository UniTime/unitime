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
package org.unitime.timetable.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class Client implements EntryPoint {

	public void onModuleLoad() {
		initAsync(Window.Location.getParameter("page"));
	}
	
	public void initAsync(final String page) {
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				init(page);
			}
			public void onFailure(Throwable reason) {
				Label error = new Label("Failed to load the page (" + reason.getMessage() + ")");
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("loading").setVisible(false);
				RootPanel.get("body").add(error);
			}
		});

	}
	
	public void init(String page) {
		RootPanel.get("loading").setVisible(false);
		for (Pages p: Pages.values()) {
			if (p.name().equals(page)) {
				RootPanel.get("title").clear();
				RootPanel.get("title").add(new Label(p.title()));
				RootPanel.get("body").add(p.widget());
				return;
			}
		}
		Label error = new Label("Failed to load the application (" + (page == null ? "page not provided" : "page " + page + " not registered" ) + ")");
		error.setStyleName("unitime-ErrorMessage");
		RootPanel.get("body").add(error);
	}

}
