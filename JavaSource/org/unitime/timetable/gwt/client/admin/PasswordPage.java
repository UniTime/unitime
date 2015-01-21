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
package org.unitime.timetable.gwt.client.admin;

import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class PasswordPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iForm;
	private TextBox iUser = null, iEmail = null;
	private UniTimeHeaderPanel iHeader, iFooter;
	private int iFooterRow;
	private PasswordTextBox iOldPassword, iNewPassword, iRetypePassword;
	
	public PasswordPage() {
		iForm = new SimpleForm();
		
		iHeader = new UniTimeHeaderPanel(MESSAGES.headerChangePassword());
		iFooter = iHeader.clonePanel("");
		iForm.addHeaderRow(iHeader);
		
		if ("1".equals(Window.Location.getParameter("reset"))) {
			iHeader.setHeaderTitle(MESSAGES.headerResetPassword());
			
			iFooter.addButton("reset", MESSAGES.buttonReset(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					iFooter.clearMessage();
					if (iEmail.getValue().isEmpty() || !iEmail.getValue().contains("@")) {
						iFooter.setErrorMessage(MESSAGES.errorEmailNotValid());
					} else {
						LoadingWidget.getInstance().show(MESSAGES.waitPasswordReset());
						RPC.execute(PasswordChangeRequest.resetPassword(iEmail.getValue()),
								new AsyncCallback<PasswordChangeResponse>() {
									@Override
									public void onFailure(Throwable caught) {
										LoadingWidget.getInstance().hide();
										iFooter.setErrorMessage(caught.getMessage());
									}
									@Override
									public void onSuccess(PasswordChangeResponse result) {
										LoadingWidget.getInstance().hide();
										iFooter.setMessage(MESSAGES.messagePasswordReset());
										iFooter.setEnabled("reset", false);
										for (int i = 1; i < iFooterRow; i++)
											iForm.getRowFormatter().setVisible(i, false);
									}
						});
					}
				}
			});
			
			iEmail = new TextBox(); iEmail.setStyleName("gwt-SuggestBox");
			iForm.addRow(MESSAGES.fieldEmail(), iEmail);
		} else {
			iFooter.addButton("change", MESSAGES.buttonChange(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					iFooter.clearMessage();
					if (iOldPassword.getValue().isEmpty()) {
						iFooter.setErrorMessage(MESSAGES.errorOldPasswordNotValid());
					} else if (iNewPassword.getValue().isEmpty()) {
						iFooter.setErrorMessage(MESSAGES.errorEnterNewPassword());
					} else if (!iNewPassword.getValue().equals(iRetypePassword.getValue())) {
						iFooter.setErrorMessage(MESSAGES.errorNewPasswordMismatch());
					} else {
						iFooter.setEnabled("change", false);
						RPC.execute(PasswordChangeRequest.changePassword(iUser == null ? null : iUser.getValue(), iOldPassword.getValue(), iNewPassword.getValue()),
								new AsyncCallback<PasswordChangeResponse>() {
									@Override
									public void onFailure(Throwable caught) {
										iFooter.setEnabled("change", true);
										iFooter.setErrorMessage(caught.getMessage());
									}
									@Override
									public void onSuccess(PasswordChangeResponse result) {
										iOldPassword.setValue("");
										iNewPassword.setValue("");
										iRetypePassword.setValue("");
										iFooter.setMessage(MESSAGES.messagePasswordChanged());
										iFooter.setEnabled("change", false);
										for (int i = 1; i < iFooterRow; i++)
											iForm.getRowFormatter().setVisible(i, false);
									}
						});
					}
				}
			});

			if (Window.Location.getParameter("user") != null) {
				iUser = new TextBox(); iUser.setStyleName("gwt-SuggestBox");
				iUser.setValue(Window.Location.getParameter("user"));
				iForm.addRow(MESSAGES.fieldUsername(), iUser);	
			}
			
			iOldPassword = new PasswordTextBox(); iOldPassword.setStyleName("gwt-SuggestBox");
			iForm.addRow(MESSAGES.fieldOldPassword(), iOldPassword);
			if (Window.Location.getParameter("key") != null)
				iOldPassword.setValue(Window.Location.getParameter("key"));
			
			iNewPassword = new PasswordTextBox(); iNewPassword.setStyleName("gwt-SuggestBox");
			iForm.addRow(MESSAGES.fieldNewPassword(), iNewPassword);

			iRetypePassword = new PasswordTextBox(); iRetypePassword.setStyleName("gwt-SuggestBox");
			iForm.addRow(MESSAGES.fieldRetypePassword(), iRetypePassword);
		}
		
		iFooterRow = iForm.addBottomRow(iFooter);
		
		initWidget(iForm);
	}
	
	public static class PasswordChangeResponse implements GwtRpcResponse {
	}
	
	public static class PasswordChangeRequest implements GwtRpcRequest<PasswordChangeResponse> {
		private String iUsername, iEmail, iOldPassword, iNewPassword;
		private boolean iReset = false;
		
		public PasswordChangeRequest() {}
		
		public void setUsername(String username) { iUsername = username; }
		public boolean hasUsername() { return iUsername != null && !iUsername.isEmpty(); }
		public String getUsername() { return iUsername; }

		public void setEmail(String email) { iEmail = email; }
		public boolean hasEmail() { return iEmail != null && !iEmail.isEmpty(); }
		public String getEmail() { return iEmail; }

		public void setOldPassword(String passwd) { iOldPassword = passwd; }
		public String getOldPassword() { return iOldPassword; }
		
		public void setNewPassword(String passwd) { iNewPassword = passwd; }
		public String getNewPassword() { return iNewPassword; }
		
		public void setReset(boolean reset) { iReset = reset; }
		public boolean isReset() { return iReset; }
		
		public static PasswordChangeRequest changePassword(String username, String oldPasswd, String newPasswd) {
			PasswordChangeRequest req = new PasswordChangeRequest();
			req.setUsername(username);
			req.setOldPassword(oldPasswd);
			req.setNewPassword(newPasswd);
			return req;
		}
		
		public static PasswordChangeRequest resetPassword(String email) {
			PasswordChangeRequest req = new PasswordChangeRequest();
			req.setEmail(email);
			req.setReset(true);
			return req;
		}
		
		public String toString() {
			return "";
		}
	}
}
