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
package org.unitime.timetable.gwt.client.sectioning;

import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaDialogBox;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.SectioningException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

public class PinDialog extends AriaDialogBox {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	
	private static final SectioningServiceAsync sSectioningService = GWT.create(SectioningService.class);
	
	private AriaTextBox iPin = null;
	private AriaButton iButton = null, iCancel = null;
	private PinCallback iCallback = null;
	
	private boolean iOnline;
	private Long iSessionId, iStudentId;

	public PinDialog() {
		super();
		setText(MESSAGES.dialogPin());
		setAnimationEnabled(true);
		setAutoHideEnabled(false);
		setGlassEnabled(true);
		setModal(true);
		addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				iPin.setText("");
			}
		});
		
		HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(5);

		panel.add(new Label(MESSAGES.pin()));
		iPin = new AriaTextBox();
		iPin.setStyleName("gwt-SuggestBox");
		iPin.setAriaLabel(ARIA.propPinNumber());
		panel.add(iPin);
		
		
		iButton = new AriaButton(MESSAGES.buttonSetPin());
		panel.add(iButton);
		
		iCancel = new AriaButton(MESSAGES.buttonCancelPin());
		panel.add(iCancel);
		
		setWidget(panel);
		
		iButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sendPin();
			}
		});
		
		iCancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
				iCallback.onFailure(new SectioningException(MESSAGES.exceptionAuthenticationPinNotProvided()));
			}
		});
		
		iPin.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendPin();
				} else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
					hide();
					iCallback.onFailure(new SectioningException(MESSAGES.exceptionAuthenticationPinNotProvided()));
				}
			}
		});
	}
	
	protected void sendPin() {
		final String pin = iPin.getText();
		hide();
		LoadingWidget.getInstance().show(MESSAGES.waitEligibilityCheck());
		sSectioningService.checkEligibility(iOnline, iSessionId, iStudentId, pin, new AsyncCallback<EligibilityCheck>() {
			
			@Override
			public void onSuccess(EligibilityCheck result) {
				LoadingWidget.getInstance().hide();
				iCallback.onMessage(result);
				if (result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.PIN_REQUIRED)) {
					center();
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iPin.selectAll();
							iPin.setFocus(true);
						}
					});
				} else {
					iCallback.onSuccess(result);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iCallback.onFailure(caught);
			}
		});
	}
	
	public void checkEligibility(boolean online, Long sessionId, Long studentId, PinCallback callback) {
		iOnline = online;
		iSessionId = sessionId;
		iStudentId = studentId;
		iCallback = callback;
		iPin.setText("");
		center();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				iPin.setFocus(true);
			}
		});
	}
	
	public static interface PinCallback extends AsyncCallback<EligibilityCheck> {
		public void onMessage(EligibilityCheck result);
	}

}
