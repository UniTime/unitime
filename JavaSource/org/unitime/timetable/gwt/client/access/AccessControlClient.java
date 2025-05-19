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
package org.unitime.timetable.gwt.client.access;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.access.AccessControlInterface.Operation;
import org.unitime.timetable.gwt.client.access.AccessControlInterface.PingRequest;
import org.unitime.timetable.gwt.client.access.AccessControlInterface.PingResponse;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog.Type;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public abstract class AccessControlClient {
	public static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private Timer iTimer, iLeaveTimer;
	private UniTimeConfirmationDialog iLeaveConfirmation;
	private boolean iActive;
	private String iPage;
	private Event.NativePreviewHandler iNativePreviewHandler;
	private HandlerRegistration iHandlerRegistration;
	private Operation iOperation = Operation.CHECK_ACCESS;
	private boolean iWaiting = true;
	private int iLimit;
	private boolean iExecuted = false;

	public AccessControlClient(String page) {
		iPage = page;
		iNativePreviewHandler = new Event.NativePreviewHandler() {
			@Override
			public void onPreviewNativeEvent(NativePreviewEvent event) {
				switch(event.getTypeInt()) {
				case Event.ONCLICK:
				case Event.ONKEYPRESS:
					iActive = true;
				}
			}
		};
		iHandlerRegistration = Event.addNativePreviewHandler(iNativePreviewHandler);
		iTimer = new Timer() {
			@Override
			public void run() {
				periodicCheckAccess();
			}
		};
	}
	
	public AccessControlClient() {
		this(ToolBox.getPage());
	}
	
	protected String getPageName() {
		return UniTimePageLabel.getInstance().getValue().getName();
	}
	
	protected abstract void executeWhenAccessIsGranted();
	
	protected void showInactiveWarning(int inactiveMins) {
		if (iLeaveConfirmation != null && iLeaveConfirmation.isShowing()) return;
		iLimit = 60;
		iLeaveTimer = new Timer() {
			@Override
			public void run() {
				if (iActive) {
					cancel();
					iLeaveConfirmation.hide();
				}
				iLimit --;
				if (iLimit < 0) {
					cancel();
				} else if (iLimit == 0) {
					if (iLeaveConfirmation.isShowing()) {
						iLeaveConfirmation.hide();
						leave(true);
					}
					cancel();
				} else {
					iLeaveConfirmation.getYes().setHTML(MESSAGES.buttonWarningInactiveStay(iLimit));
				}
			}
		};
		iLeaveConfirmation = new UniTimeConfirmationDialog(Type.CONFIRM, MESSAGES.warnInactive(inactiveMins, getPageName()), null, null, new Command() {
			@Override
			public void execute() {
				iLeaveTimer.cancel();
				iActive = true;
			}
		});
		iLeaveConfirmation.getYes().setHTML(MESSAGES.buttonWarningInactiveStay(iLimit));
		iLeaveConfirmation.getNo().setHTML(MESSAGES.buttonWarningInactiveLeave());
		iLeaveConfirmation.setText(MESSAGES.dialogWarningInactive());
		iLeaveConfirmation.setNoCallback(new Command() {
			@Override
			public void execute() {
				iLeaveTimer.cancel();
				iActive = false;
				leave(true);
			}
		});
		iLeaveConfirmation.center();
		iLeaveTimer.scheduleRepeating(1000);
	}
	
	public void leave(final boolean openMainPage) {
		iTimer.cancel();
		PingRequest req = new PingRequest();
		req.setPage(iPage);
		req.setOperation(Operation.LOGOUT);
		req.setActive(false);
		RPC.execute(req, new AsyncCallback<PingResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				if (openMainPage) openMainPage();
			}
			@Override
			public void onSuccess(PingResponse result) {
				if (openMainPage) openMainPage();
			}
		});
	}
	
	public native static void sleep() /*-{
		var now = new Date().getTime();
		while(new Date().getTime() < now + 200) {}
	}-*/;
	
	protected void openMainPage() {
		ToolBox.open(GWT.getHostPageBaseURL() + "main.action?message=" + URL.encodeQueryString(MESSAGES.closedDueToInactivity(getPageName())));
	}
	
	protected void periodicCheckAccess() {
		PingRequest req = new PingRequest();
		req.setPage(iPage);
		req.setOperation(iOperation);
		req.setActive(iActive || iOperation == Operation.CHECK_ACCESS); iActive = false;
		RPC.execute(req, new AsyncCallback<PingResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iTimer.schedule(20000);
			}
			@Override
			public void onSuccess(PingResponse result) {
				if (iWaiting) LoadingWidget.getInstance().hide();
				if (result.isAccess()) {
					iOperation = Operation.PING;
					if (!iExecuted) {
						executeWhenAccessIsGranted();
						iExecuted = true;
					}
					iWaiting = false;
					if (result.getInactive() != null && !iActive) showInactiveWarning(result.getInactive());
				} else {
					iWaiting = true;
					LoadingWidget.getInstance().show(MESSAGES.waitTooManyUsersWaitInQueue(result.getQueue()));
				}
				iTimer.schedule(20000);
			}
		});
	}
	
	public void checkAccess() {
		iExecuted = false;
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingPage());
		periodicCheckAccess();
		iTimer.schedule(20000);
	}
	
	public void stopClient() {
		if (iHandlerRegistration != null) {
			iHandlerRegistration.removeHandler();
			iHandlerRegistration = null;
		}
		iTimer.cancel();
	}
}
