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
package org.unitime.timetable.gwt.client.page;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

public class MainPage extends P {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static GwtConstants CONST = GWT.create(GwtConstants.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	public MainPage() {
		super("unitime-MainContent", "unitime-MainLogo");
		
		RootPanel.get("unitime-Page").setStyleName("body unitime-MainLogoFaded");

		MainPageRequest req = new MainPageRequest();
		req.setRefresh("1".equals(Window.Location.getParameter("refresh")));
		RPC.execute(req, new AsyncCallback<MainPageResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(MainPageResponse result) {
				boolean fade = false;
				P sysMessages = new P("messages");
				P sysHeader = new P("WelcomeRowHead"); sysHeader.setText(COURSE.sectSystemMessages());
				sysMessages.add(sysHeader);
				if (result.hasSystemMessage()) {
					P m = new P("message"); m.setHTML(result.getSystemMessage());
					sysMessages.add(m);
				}

				String message = Window.Location.getParameter("message");
				if (message != null && "cas-logout".equals(Window.Location.getParameter("op")))
					message = COURSE.casLoggedOut();
				if (message != null && "logout".equals(Window.Location.getParameter("op")))
					message = COURSE.opLoggedOut();
				if (message != null && !message.isEmpty()) {
					P m = new P("message"); m.setText((sysMessages.getWidgetCount() == 1 ? "" : "\n") + message);
					sysMessages.add(m);
				}
				if (sysMessages.getWidgetCount() > 1) {
					fade = true;
					add(sysMessages);
				}

				if (result.hasRegistrationMessage()) {
					fade = true;
					P messages = new P("messages");
					P header = new P("WelcomeRowHead"); header.setText(COURSE.sectRegistrationMessages());
					messages.add(header);
					P m = new P("message"); m.setHTML(result.getRegistrationMessage());
					messages.add(m);
					MainPage.this.add(messages);
				}
				if (result.hasInitializationError()) {
					fade = true;
					P messages = new P("messages");
					P header = new P("WelcomeRowHead");
					header.getElement().getStyle().setColor("#ec0000");
					header.setText(COURSE.errorUniTimeFailedToStart(result.getVersion()));
					messages.add(header);
					P m = new P("message"); m.setHTML(result.getInitializationError());
					messages.add(m);
					MainPage.this.add(messages);
				}
				if (fade)
					removeStyleName("unitime-MainLogo");
				if (result.hasRegistration()) {
					RootPanel panel = RootPanel.get("UniTimeGWT:Registration");
					if (panel != null) {
						panel.clear(); panel.getElement().setInnerText("");
						P test = new P(); test.setHTML(result.getRegistration());
						panel.add(test);
					}
				}
				if (result.hasPopupMessage()) {
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							UniTimeNotifications.info(result.getPopupMessage());
						}
					});
				}
			}
		});
		
		checkParent();
	}

	public static class MainPageRequest implements GwtRpcRequest<MainPageResponse>, GwtRpcRequest.HasUniTimeUrl {
		private boolean iRefresh = false;
		private String iUniTimeUrl = null;
		
		public boolean isRefresh() { return iRefresh; }
		public void setRefresh(boolean refresh) { iRefresh = refresh; }
		@Override
		public String getUniTimeUrl() { return iUniTimeUrl; }
		@Override
		public void setUniTimeUrl(String url) { iUniTimeUrl = url; }
	}
	
	public static class MainPageResponse implements GwtRpcResponse {
		private String iSystemMessage;
		private String iInitializationError;
		private String iVersion;
		private String iRegistrationMessage;
		private String iRegistration;
		private String iPopupMessage;
		public boolean hasSystemMessage() { return iSystemMessage != null && !iSystemMessage.isEmpty(); }
		public String getSystemMessage() { return iSystemMessage; }
		public void setSystemMessage(String message) { iSystemMessage = message; }

		public boolean hasRegistrationMessage() { return iRegistrationMessage != null && !iRegistrationMessage.isEmpty(); }
		public String getRegistrationMessage() { return iRegistrationMessage; }
		public void setRegistrationMessage(String message) { iRegistrationMessage = message; }

		public boolean hasRegistration() { return iRegistration != null && !iRegistration.isEmpty(); }
		public String getRegistration() { return iRegistration; }
		public void setRegistration(String message) { iRegistration = message; }

		public boolean hasPopupMessage() { return iPopupMessage != null && !iPopupMessage.isEmpty(); }
		public String getPopupMessage() { return iPopupMessage; }
		public void setPopupMessage(String message) { iPopupMessage = message; }

		public boolean hasInitializationError() { return iInitializationError != null && !iInitializationError.isEmpty(); }
		public String getInitializationError() { return iInitializationError; }
		public void setInitializationError(String error) { iInitializationError = error; }
		public void addInitializationError(String error) {
			if (iInitializationError == null)
				iInitializationError = error;
			else
				iInitializationError += "\n" + error;
		}
		
		public void setVersion(String version) { iVersion = version; }
		public String getVersion() { return iVersion; }
	}
	
	public static native boolean checkParent()/*-{
		if ($wnd.parent && $wnd.parent !== $wnd.self && $wnd.parent.hideGwtDialog && $wnd.parent.refreshPage) {
			$wnd.parent.hideGwtDialog();
			$wnd.parent.refreshPage();
		}
	}-*/;	
}
