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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.SectioningException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

public class EnrollmentConfirmationDialog extends UniTimeDialogBox {
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	private AriaButton iYes, iNo;
	private AsyncCallback<SpecialRegistrationEligibilityResponse> iCommand;
	private boolean iValue = false;
	private P iOverrideMessage = null, iWaiting = null, iMessagePannel = null;
	private boolean iAll = true;
	private SpecialRegistrationEligibilityResponse iResponse;
	
	public EnrollmentConfirmationDialog(Throwable exception, ClassAssignmentInterface result, AsyncCallback<SpecialRegistrationEligibilityResponse> callback) {
		super(true, true);
		addStyleName("unitime-CourseRequestsConfirmationDialog");
		setText(MESSAGES.dialogEnrollmentConfirmation());
		
		iCommand = callback;
		
		P panel = new P("unitime-ConfirmationPanel");
		setEscapeToHide(true);
		
		P bd = new P("body-panel");
		panel.add(bd);

		P ic = new P("icon-panel");
		bd.add(ic);
		ic.add(new Image(exception != null ? RESOURCES.statusError() : RESOURCES.statusWarning()));

		P cp = new P("content-panel");
		bd.add(cp);
		iMessagePannel = new P("message-panel");
		cp.add(iMessagePannel);
		
		if (exception != null) {
			P m1 = new P("message"); m1.setHTML(MESSAGES.messageEnrollmentFailedWithErrors()); iMessagePannel.add(m1);
			if (exception instanceof SectioningException && ((SectioningException)exception).hasErrors()) {
				P ctab = new P("course-table");
				String last = null; Set<String> msg = new HashSet<String>();
				for (ErrorMessage cm: ((SectioningException)exception).getErrors()) {
					if (cm.getCode() != null && cm.getCode().startsWith("UT_")) iAll = false;
					P cn = new P("course-name");
					if (last == null || !last.equals(cm.getCourse())) { msg.clear(); cn.setText(cm.getCourse()); }
					if (!msg.add(cm.getMessage())) continue;
					P m = new P("course-message"); m.setText(cm.getMessage());
					P crow = new P("course-row");
					if (last == null || !last.equals(cm.getCourse())) crow.addStyleName("first-course-line");
					crow.add(cn); crow.add(m);
					ctab.add(crow);
					last = cm.getCourse();
				}
				iMessagePannel.add(ctab);
			} else {
				P em = new P("message", "error-message"); em.setHTML(exception.getMessage()); iMessagePannel.add(em);
			}
		} else {
			P m1 = new P("message"); m1.setHTML(MESSAGES.messageEnrollmentSucceededWithErrors()); iMessagePannel.add(m1);
			if (result.hasErrors()) {
				P ctab = new P("course-table");
				String last = null; Set<String> msg = new HashSet<String>();
				for (ErrorMessage cm: result.getErrors()) {
					if (cm.getCode() != null && cm.getCode().startsWith("UT_")) iAll = false;
					P cn = new P("course-name");
					if (last == null || !last.equals(cm.getCourse())) { msg.clear(); cn.setText(cm.getCourse()); }
					if (!msg.add(cm.getMessage())) continue;
					P m = new P("course-message"); m.setText(cm.getMessage());
					P crow = new P("course-row");
					if (last == null || !last.equals(cm.getCourse())) crow.addStyleName("first-course-line");
					crow.add(cn); crow.add(m);
					ctab.add(crow);
					last = cm.getCourse();
				}
				iMessagePannel.add(ctab);
			} else {
				P em = new P("message", "error-message"); em.setHTML(result.getMessages("\n")); iMessagePannel.add(em);
			}
		}
		
		iOverrideMessage = new P("message", "override-message");
		iOverrideMessage.setHTML(MESSAGES.messageCheckingOverrides());
		iMessagePannel.add(iOverrideMessage);
		
		P bp = new P("buttons-panel");
		panel.add(bp);
		
		iWaiting = new P("unitime-Waiting");
		iWaiting.add(new Image(RESOURCES.loading_small()));
		P wm = new P("waiting-message"); iWaiting.add(wm); wm.setText(MESSAGES.waitOverridesCheck());
		bp.add(iWaiting);
		
		iYes = new AriaButton(MESSAGES.buttonEnrollmentRequestOverrides());
		iYes.setVisible(false); iYes.setEnabled(false);
		iYes.addStyleName("yes");
		iYes.setTitle(MESSAGES.titleEnrollmentRequestOverrides());
		bp.add(iYes);
		iYes.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		
		iNo = new AriaButton(MESSAGES.buttonEnrollmentHideConfirmation());
		iNo.addStyleName("no");
		iNo.setTitle(MESSAGES.titleEnrollmentHideConfirmation());
		bp.add(iNo);
		iNo.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				if (iCommand != null) 
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iCommand.onSuccess(iValue ? iResponse : null);
						}
					});
			}
		});
		
		setWidget(panel);
	}
	
	protected void showError(String message) {
		iWaiting.setVisible(false);
		iOverrideMessage.setHTML(MESSAGES.messageCannotRequestOverrides(message));
		super.center();
	}
	
	protected void showErrors(List<ErrorMessage> errors) {
		iWaiting.setVisible(false);
		iOverrideMessage.setHTML(MESSAGES.messageCannotRequestOverridesErrors());
		P ctab = new P("course-table");
		String last = null; Set<String> msg = new HashSet<String>();
		for (ErrorMessage cm: errors) {
			P cn = new P("course-name");
			if (last == null || !last.equals(cm.getCourse())) { msg.clear(); cn.setText(cm.getCourse()); }
			if (!msg.add(cm.getMessage())) continue;
			P m = new P("course-message"); m.setText(cm.getMessage());
			P crow = new P("course-row");
			if (last == null || !last.equals(cm.getCourse())) crow.addStyleName("first-course-line");
			crow.add(cn); crow.add(m);
			ctab.add(crow);
			last = cm.getCourse();
		}
		iMessagePannel.add(ctab);
		P m1 = new P("message"); m1.setHTML(MESSAGES.messageCannotRequestOverridesErrorsBottom()); iMessagePannel.add(m1);
		super.center();
	}
	
	protected void showRequestOverrides() {
		iWaiting.setVisible(false);
		if (iAll)
			iOverrideMessage.setHTML(MESSAGES.messageCanRequestOverridesAll());
		else
			iOverrideMessage.setHTML(MESSAGES.messageCanRequestOverridesSome());
		iYes.setVisible(true);
		iYes.setEnabled(true);
		iYes.setFocus(true);
		super.center();
	}
	
	public void setResponse(SpecialRegistrationEligibilityResponse eligibilityResponse) {
		if (!isVisible()) return;
		iResponse = eligibilityResponse;
		final Collection<ErrorMessage> errors = eligibilityResponse.getErrors();
		if (eligibilityResponse.isCanSubmit() && errors != null && !errors.isEmpty()) {
			showRequestOverrides();
		} else if (eligibilityResponse.hasDeniedErrors()) {
			showErrors(eligibilityResponse.getDeniedErrors());
		} else if (eligibilityResponse.hasMessage()) {
			showError(eligibilityResponse.getMessage());
		} else if (eligibilityResponse.isCanSubmit()) {
			showError(MESSAGES.errorNoRegistrationErrorsDetected());
		} else {
			showError(MESSAGES.errorRegistrationErrorsBadResponse());
		}
	}
	
	public SpecialRegistrationEligibilityResponse getResponse() { return iResponse; }
	
	@Override
	public void center() {
		super.center();
		AriaStatus.getInstance().setText(ARIA.dialogOpened(getText()));
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				iNo.setFocus(true);
			}
		});
	}
	
	protected void submit() {
		iValue = iYes.isEnabled();
		hide();
	}
}
