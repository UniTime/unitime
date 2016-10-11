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
package org.unitime.timetable.gwt.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unitime.timetable.gwt.client.page.UniTimeMenu;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseBoolean;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.MenuInterface.IsSessionBusyRpcRequest;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class Client implements EntryPoint {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	public static List<GwtPageChangedHandler> iGwtPageChangedHandlers = new ArrayList<GwtPageChangedHandler>();
	public static Logger sLogger = Logger.getLogger(Client.class.getName());
	private Timer iPageLoadingTimer;
	
	public void onModuleLoad() {
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void onUncaughtException(Throwable e) {
				Throwable u = ToolBox.unwrap(e);
				sLogger.log(Level.WARNING, MESSAGES.failedUncaughtException(u.getMessage()), u);
			}
		});
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				onModuleLoadDeferred();
			}
		});
	}
	
	public void onModuleLoadDeferred() {
		// register triggers
		GWT.runAsync(new RunAsyncCallback() {
			@Override
			public void onSuccess() {
				for (Triggers t: Triggers.values())
					t.register();
				callGwtOnLoadIfExists();
			}
			@Override
			public void onFailure(Throwable reason) {
			}
		});
		
		// load page
		if (RootPanel.get("UniTimeGWT:Body") != null) {
			LoadingWidget.getInstance().show(MESSAGES.waitLoadingPage());
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
			if (p == null && c.isMultiple()) {
				NodeList<Element> x = getElementsByName(c.id());
				if (x != null && x.getLength() > 0)
					for (int i = 0; i < x.getLength(); i++) {
						Element e = x.getItem(i);
						e.setId(DOM.createUniqueId());
						final RootPanel q = RootPanel.get(e.getId());
						Scheduler.get().scheduleDeferred(new ScheduledCommand() {
							@Override
							public void execute() {
								initComponentAsync(q, c);
							}
						});
					}
			}
		}
		
		Window.addWindowClosingHandler(new Window.ClosingHandler() {
			@Override
			public void onWindowClosing(Window.ClosingEvent event) {
				if (isLoadingDisplayed() || LoadingWidget.getInstance().isShowing()) return;
				LoadingWidget.showLoading(MESSAGES.waitPlease());
				iPageLoadingTimer = new Timer() {
					@Override
					public void run() {
						RPC.execute(new IsSessionBusyRpcRequest(), new AsyncCallback<GwtRpcResponseBoolean>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.hideLoading();
							}
							@Override
							public void onSuccess(GwtRpcResponseBoolean result) {
								if (result.getValue()) {
									iPageLoadingTimer.schedule(500);
								} else {
									LoadingWidget.hideLoading();
								}
							}
						});
					}
				};
				iPageLoadingTimer.schedule(500);
			}
		});
	}
	
	public void initPageAsync(final String page) {
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				init(page);
				LoadingWidget.getInstance().hide();
			}
			public void onFailure(Throwable reason) {
				Label error = new Label(MESSAGES.failedToLoadPage(reason.getMessage()));
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
					LoadingWidget.getInstance().setMessage(MESSAGES.waitLoading(p.name(MESSAGES)));
					UniTimePageLabel.getInstance().setPageName(p.name(MESSAGES));
					Window.setTitle("UniTime " + CONSTANTS.version() + "| " + p.name(MESSAGES));
					RootPanel.get("UniTimeGWT:Body").add(p.widget());
					return;
				}
			}
			Label error = new Label(page == null ? MESSAGES.failedToLoadPageNotProvided() :MESSAGES.failedToLoadPageNotRegistered(page));
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Body").add(error);
		} catch (Exception e) {
			Label error = new Label(MESSAGES.failedToLoadPage(e.getMessage()));
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
	
	public static native void callGwtOnLoadIfExists()/*-{
		if ($wnd.gwtOnLoad)
			$wnd.gwtOnLoad();
	}-*/;
	
	public static void reloadMenu() {
		for (final Components c: Components.values()) {
			final RootPanel p = RootPanel.get(c.id());
			if (p != null) {
				for (int i = 0; i < p.getWidgetCount(); i++)
					if (p.getWidget(i) instanceof UniTimeMenu)
						((UniTimeMenu)p.getWidget(i)).reload();
			}
		}
	}
	
	public final native static NodeList<Element> getElementsByName(String name) /*-{
    	return $doc.getElementsByName(name);
  	}-*/;
	
	static final native boolean isLoadingDisplayed() /*-{
		if ($wnd.isLoadingDisplayed)
			return $wnd.isLoadingDisplayed();
		return false;
	}-*/;
}
