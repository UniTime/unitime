/*
 * UniTime 4.0 (University Timetabling Application)
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
package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;
import java.util.Date;

import org.unitime.timetable.gwt.resources.StudentSectioningMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ValidationErrors extends Composite {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);

	private DialogBox iDialog;
	private Label iProgress, iError;
	private VerticalPanel iErrorsPanel;
	private boolean iAutoHide;
	
	private ArrayList<Validator> iValidators = new ArrayList<Validator>();
	
	private boolean iShowIndividualErrors;
	private String iErrorMessage;
	
	public ValidationErrors(boolean showIndividualErrors, String title, String errorMessage, boolean autoHide) {
		iDialog = new DialogBox();
		iDialog.setText(title);
		iDialog.setAnimationEnabled(false);
		iDialog.setAutoHideEnabled(false);
		iDialog.setGlassEnabled(true);
		iDialog.setModal(true);
		
		iAutoHide = autoHide;
		
		iProgress = new Label(MESSAGES.pleaseWait());
		iProgress.setStyleName("unitime-ProgressMessage");
		iDialog.add(iProgress);
		
		iErrorsPanel = new VerticalPanel();
		iErrorsPanel.setSpacing(5);
		iErrorsPanel.setStyleName("unitime-ValidationErrors");
		
		iError = new Label();
		iErrorMessage = errorMessage;

		iShowIndividualErrors = showIndividualErrors;
		if (!iShowIndividualErrors) {
			iError.setStyleName("unitime-ErrorMessage");
			iErrorsPanel.add(iError);
		}
		
		initWidget(iErrorsPanel);
	}
	
	public void addValidator(Validator validator) {
		iValidators.add(validator);
	}
	
	public synchronized void validate(final AsyncCallback<Boolean> onResult) {
		if (iShowIndividualErrors)
			for (int i=iErrorsPanel.getWidgetCount()-1; i>=0; i--)
				iErrorsPanel.remove(i);
		iError.setText(null);
		iDialog.center();
		final Date d1 = new Date();
		final Counter c = new Counter(iValidators.size());
		for (Validator validator: iValidators) {
			validator.validate(new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {
					onSuccess(caught.getMessage());
				}
				public void onSuccess(String result) {
					if (result!=null && !result.isEmpty()) {
						if (iShowIndividualErrors) {
							Label error = new Label(result);
							error.setStyleName("unitime-ErrorMessage");
							iErrorsPanel.add(error);
						}
					}
					if (c.decrement(result!=null && !result.isEmpty())) {
						if (iAutoHide) {
							DeferredCommand.addCommand(new Command() {
								@Override
								public void execute() {
									do {
										for (int i=0;i<100;i++) {}
									} while (new Date().getTime() - d1.getTime() < 500);
									iDialog.hide();
								}
							});
						}
						if (c.isError()) iError.setText(iErrorMessage);
						onResult.onSuccess(!c.isError());
					}
				}
			});
		}
	}
	
	public void hide() {
		iDialog.hide();
	}
	
	private static class Counter {
		private int iCounter;
		private boolean iError = false;
		private Counter(int counter) { iCounter = counter; }
		private boolean decrement(boolean error) {
			if (error) iError = true;
			return --iCounter == 0;
		}
		private boolean isError() { return iError; }
	}
}
