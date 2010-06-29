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
		if (RootPanel.get("UniTimeGWT:Body") != null) {
			LoadingWidget.getInstance().show("Loading page ...");
			DeferredCommand.addCommand(new Command() {
				@Override
				public void execute() {
					initPageAsync(Window.Location.getParameter("page"));
				}
			});
		}
		for (final Components c: Components.values()) {
			final RootPanel p = RootPanel.get(c.id());
			if (p != null) {
				DeferredCommand.addCommand(new Command() {
					@Override
					public void execute() {
						initComponentAsync(p, c);
					}
				});
			}
		}
	}
	
	public void initPageAsync(final String page) {
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				init(page);
				LoadingWidget.getInstance().hide();
			}
			public void onFailure(Throwable reason) {
				Label error = new Label("Failed to load the page (" + reason.getMessage() + ")");
				error.setStyleName("unitime-ErrorMessage");
				RootPanel loading = RootPanel.get("UniTimeGWT:Loading");
				if (loading != null) loading.setVisible(false);
				RootPanel.get("UniTimeGWT:Body").add(error);
				LoadingWidget.getInstance().hide();
			}
		});
	}
	
	public void init(String page) {
		try {
			RootPanel loading = RootPanel.get("UniTimeGWT:Loading");
			if (loading != null) loading.setVisible(false);
			for (Pages p: Pages.values()) {
				if (p.name().equals(page)) {
					LoadingWidget.getInstance().setMessage("Loading " + p.title() + " ...");
					RootPanel title = RootPanel.get("UniTimeGWT:Title");
					if (title != null) {
						title.clear();
						PageLabel label = new PageLabel(); label.setPageName(p.title());
						title.add(label);
					}
					Window.setTitle("UniTime 3.2| " + p.title());
					RootPanel.get("UniTimeGWT:Body").add(p.widget());
					return;
				}
			}
			Label error = new Label("Failed to load the page (" + (page == null ? "page not provided" : "page " + page + " not registered" ) + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Body").add(error);
		} catch (Exception e) {
			Label error = new Label("Failed to load the page (" + e.getMessage() + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Body").add(error);
		}
	}
	
	public void initComponentAsync(final RootPanel panel, final Components comp) {
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				comp.insert(panel);
			}
			public void onFailure(Throwable reason) {
				Label error = new Label("Failed to load the component (" + reason.getMessage() + ")");
				error.setStyleName("unitime-ErrorMessage");
				panel.clear(); panel.add(error);
			}
		});
	}
}
