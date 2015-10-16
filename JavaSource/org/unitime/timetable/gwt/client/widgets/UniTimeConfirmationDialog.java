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
package org.unitime.timetable.gwt.client.widgets;

import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;

/**
 * @author Tomas Muller
 */
public class UniTimeConfirmationDialog extends UniTimeDialogBox {
	protected static GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private String iAnswer = null;
	private UniTimeTextBox iTextBox = null;
	private AriaButton iYes, iNo;
	private Command iCommand;
	private P iError = null;

	protected UniTimeConfirmationDialog(boolean alert, String message, String question, String answer, Command command) {
		super(true, true);
		this.setText(alert ? MESSAGES.dialogAlert() : MESSAGES.dialogConfirmation());
		iAnswer = answer;
		iCommand = command;
		
		P panel = new P("unitime-ConfirmationPanel");
		setEscapeToHide(true);
		setEnterToSubmit(new Command() {
			@Override
			public void execute() {
				submit();
			}
		});
		
		if (message != null && !message.isEmpty()) {
			P mp = new P("message-panel");
			panel.add(mp);
			P m = new P("message"); m.setText(message);
			mp.add(m);
		}
		
		if (question != null) {
			P qp = new P("question-panel");
			panel.add(qp);
			
			P q = new P("question");
			q.setText(question);
			qp.add(q);
			
			iError = new P("error");
			
			iTextBox = new UniTimeTextBox();
			iTextBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					iError.setText("");
				}
			});
			iTextBox.addStyleName("answer");
			qp.add(iTextBox);
		}
		
		if (iError != null) {
			P ep = new P("error-panel");
			panel.add(ep);
			ep.add(iError);
		}
		
		P bp = new P("buttons-panel");
		panel.add(bp);
		iYes = new AriaButton(MESSAGES.buttonConfirmOK());
		iYes.addStyleName("yes");
		bp.add(iYes);
		iYes.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		
		if (!alert) {
			iNo = new AriaButton(MESSAGES.buttonConfirmCancel());
			iNo.addStyleName("no");
			bp.add(iNo);
			iNo.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
				}
			});
		}
		
		setWidget(panel);
	}
	
	@Override
	public void center() {
		super.center();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				if (iTextBox != null) {
					iTextBox.setFocus(true);
					iTextBox.selectAll();
				} else {
					iYes.setFocus(true);
				}
			}
		});
	}
	
	protected void submit() {
		if (iTextBox != null) {
			if (iTextBox.getText().equalsIgnoreCase(iAnswer)) {
				hide();
				if (iCommand != null) iCommand.execute();
			} else {
				iError.setText(MESSAGES.confirmationWrongAnswer(iAnswer));
			}
		} else {
			hide();
			if (iCommand != null) iCommand.execute();
		}
	}
	
	public static void alert(String message) {
		new UniTimeConfirmationDialog(true, message, null, null, null).center();
	}
	
	public static void confirm(String message, Command callback) {
		new UniTimeConfirmationDialog(false, message, null, null, callback).center();
	}
	
	public static void confirm(String message, String question, String answer, Command callback) {
		new UniTimeConfirmationDialog(false, message, question, answer, callback).center();
	}
	
	public static native void fireCallback(JavaScriptObject callback)/*-{
		callback();
	}-*/;
	
	public static void _confirm(String message, final JavaScriptObject callback, String question, String answer) {
		new UniTimeConfirmationDialog(callback == null, message, question, answer, callback == null ? null : new Command() {
			@Override
			public void execute() {
				fireCallback(callback);
			}
		}).center();
	}
	
	public static native void createTriggers()/*-{
		$wnd.gwtConfirm = function(message, callback, question, answer) {
			@org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog::_confirm(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;)(message, callback, question, answer);
		};
	}-*/;
}
