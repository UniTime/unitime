/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.resources.GwtConstants;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class Client implements EntryPoint {
	public static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	public static List<GwtPageChangedHandler> iGwtPageChangedHandlers = new ArrayList<GwtPageChangedHandler>();
	public static Logger sLogger = Logger.getLogger(Client.class.getName()); 
	
	public void onModuleLoad() {
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void onUncaughtException(Throwable e) {
				Throwable u = unwrap(e);
				sLogger.log(Level.WARNING, "Uncaught exception: " + u.getMessage(), u);
			}
		});
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				onModuleLoadDeferred();
			}
		});
	}
	
	public Throwable unwrap(Throwable e) {
		if (e instanceof UmbrellaException) {
			UmbrellaException ue = (UmbrellaException) e;
			if (ue.getCauses().size() == 1) {
				return unwrap(ue.getCauses().iterator().next());
			}
		}
		return e;
	}
	
	public void onModuleLoadDeferred() {
		// register triggers
		GWT.runAsync(new RunAsyncCallback() {
			@Override
			public void onSuccess() {
				for (Triggers t: Triggers.values())
					t.register();
			}
			@Override
			public void onFailure(Throwable reason) {
			}
		});
		
		// load page
		if (RootPanel.get("UniTimeGWT:Body") != null) {
			LoadingWidget.getInstance().show("Loading page ...");
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					initPageAsync(Window.Location.getParameter("page"));
				}
			});
		}
		
		// load components
		for (final Components c: Components.values()) {
			final RootPanel p = RootPanel.get(c.id());
			if (p != null) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
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
					UniTimePageLabel.getInstance().setPageName(p.title());
					Window.setTitle("UniTime " + CONSTANTS.version() + "| " + p.title());
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
			}
		});
	}
	
	public static class GwtPageChangeEvent {
		
	}
	
	public interface GwtPageChangedHandler {
		public void onChange(GwtPageChangeEvent event);
	}
	
	public static void addGwtPageChangedHandler(GwtPageChangedHandler h) {
		iGwtPageChangedHandlers.add(h);
	}
	
	public static void fireGwtPageChanged(GwtPageChangeEvent event) {
		for (GwtPageChangedHandler h: iGwtPageChangedHandlers)
			h.onChange(event);
	}
}
