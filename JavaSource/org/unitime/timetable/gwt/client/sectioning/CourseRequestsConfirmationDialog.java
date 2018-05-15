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
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CourseMessage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * @author Tomas Muller
 */
public class CourseRequestsConfirmationDialog extends UniTimeDialogBox {
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	private AriaButton iYes, iNo;
	private AsyncCallback<Boolean> iCommand;
	private String iMessage;
	private boolean iValue = false;
	private TextArea iNote = null;

	public CourseRequestsConfirmationDialog(CheckCoursesResponse response, int confirm, AsyncCallback<Boolean> callback) {
		super(true, true);
		addStyleName("unitime-CourseRequestsConfirmationDialog");
		setText(response.getConfirmationTitle(confirm, MESSAGES.dialogConfirmation()));
		iMessage = response.getConfirmations(confirm, " \n");
		iCommand = callback;
		
		P panel = new P("unitime-ConfirmationPanel");
		setEscapeToHide(true);
		
		P bd = new P("body-panel");
		panel.add(bd);

		P ic = new P("icon-panel");
		bd.add(ic);
		ic.add(new Image(RESOURCES.statusWarning()));

		P cp = new P("content-panel");
		bd.add(cp);
		P mp = new P("message-panel");
		cp.add(mp);
		P ctab = null;
		String last = null;
		for (final CourseMessage cm: response.getMessages()) {
			if (confirm != cm.getConfirm()) continue;
			if (cm.hasCourse()) {
				if (ctab == null) { ctab = new P("course-table"); last = null; }
				P cn = new P("course-name");
				if (last == null || !last.equals(cm.getCourse())) cn.setText(cm.getCourse());
				P m = new P("course-message"); m.setText(cm.getMessage());
				P crow = new P("course-row");
				if (last == null || !last.equals(cm.getCourse())) crow.addStyleName("first-course-line");
				crow.add(cn); crow.add(m);
				ctab.add(crow);
				last = cm.getCourse();
			} else if ("REQUEST_NOTE".equals(cm.getCode())) {
				iNote = new TextArea();
				iNote.setStyleName("unitime-TextArea"); iNote.addStyleName("request-note");
				iNote.setVisibleLines(5);
				iNote.setCharacterWidth(80);
				if (cm.getMessage() != null) iNote.setText(cm.getMessage());
				iNote.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						cm.setMessage(event.getValue());
					}
				});
				mp.add(iNote);
			} else {
				if (ctab != null) { mp.add(ctab); ctab = null; }
				P m = new P("message"); m.setHTML(cm.getMessage());
				mp.add(m);
				
			}
		}
		
		P bp = new P("buttons-panel");
		panel.add(bp);
		iYes = new AriaButton(response.getConfirmationYesButton(confirm, MESSAGES.buttonConfirmYes()));
		iYes.addStyleName("yes");
		String yesTitle = response.getConfirmationYesButtonTitle(confirm, null);
		if (yesTitle != null && !yesTitle.isEmpty()) iYes.setTitle(yesTitle);
		bp.add(iYes);
		iYes.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		
		iNo = new AriaButton(response.getConfirmationNoButton(confirm, MESSAGES.buttonConfirmNo()));
		iNo.addStyleName("no");
		String noTitle = response.getConfirmationNoButtonTitle(confirm, null);
		if (noTitle != null && !noTitle.isEmpty()) iNo.setTitle(noTitle);
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
							iCommand.onSuccess(iValue);
						}
					});
			}
		});
		
		setWidget(panel);
	}
	
	@Override
	public void center() {
		super.center();
		if (iMessage != null && !iMessage.isEmpty())
			AriaStatus.getInstance().setText(ARIA.dialogOpened(getText()) + " " + iMessage + " " + ARIA.confirmationEnterToAcceptEscapeToReject());
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				if (iNote != null)
					iNote.setFocus(true);
				else
					iYes.setFocus(true);
			}
		});
	}
	
	protected void submit() {
		iValue = true;
		hide();
	}
	
	public String getNote() {
		return iNote == null ? null : iNote.getText();
	}
	
	public static void confirm(CheckCoursesResponse response, int confirm, AsyncCallback<Boolean> callback) {
		new CourseRequestsConfirmationDialog(response, confirm, callback).center();
	}
}
