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

import org.unitime.timetable.gwt.widgets.LoadingWidget;
import org.unitime.timetable.gwt.widgets.PageLabel;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class Client implements EntryPoint {
	public void onModuleLoad() {
		LoadingWidget.getInstance().show();
		DeferredCommand.addCommand(new Command() {
			@Override
			public void execute() {
				initAsync(Window.Location.getParameter("page"));
			}
		});
	}
	
	public void initAsync(final String page) {
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				init(page);
				LoadingWidget.getInstance().hide();
			}
			public void onFailure(Throwable reason) {
				Label error = new Label("Failed to load the page (" + reason.getMessage() + ")");
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("loading").setVisible(false);
				RootPanel.get("body").add(error);
				LoadingWidget.getInstance().hide();
			}
		});

	}
	
	public void init(String page) {
		try {
			RootPanel.get("loading").setVisible(false);
			for (Pages p: Pages.values()) {
				if (p.name().equals(page)) {
					RootPanel.get("title").clear();
					PageLabel label = new PageLabel(); label.setPageName(p.title());
					RootPanel.get("title").add(label);
					RootPanel.get("body").add(p.widget());
					return;
				}
			}
			Label error = new Label("Failed to load the page (" + (page == null ? "page not provided" : "page " + page + " not registered" ) + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("body").add(error);
		} catch (Exception e) {
			Label error = new Label("Failed to load the page (" + e.getMessage() + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("body").add(error);
		}
	}
	
}
