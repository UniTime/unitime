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
package org.unitime.timetable.gwt.client;

import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.widgets.AcademicSessionSelector;
import org.unitime.timetable.gwt.widgets.StudentSectioningWidget;
import org.unitime.timetable.gwt.widgets.UserAuthentication;
import org.unitime.timetable.gwt.widgets.UserAuthentication.UserAuthenticatedEvent;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class StudentSectioning implements EntryPoint {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);

	public void onModuleLoad() {
		final UserAuthentication userAuthentication = new UserAuthentication(true);
		RootPanel.get("logIn").add(userAuthentication);
		
		iSectioningService.whoAmI(new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				userAuthentication.authenticate();
			}
			public void onSuccess(String result) {
				userAuthentication.authenticated(result);
			}
		});
		
		initAsync(userAuthentication);
	}
	
	private void initAsync(final UserAuthentication userAuthentication) {
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				init(userAuthentication);
			}
			public void onFailure(Throwable reason) {
				Label error = new Label(MESSAGES.failedToLoadTheApp(reason.getMessage()));
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("loading").setVisible(false);
				RootPanel.get("body").add(error);
			}
		});
	}
	
	private void init(final UserAuthentication userAuthentication) {
		final AcademicSessionSelector sessionSelector = new AcademicSessionSelector();
		RootPanel.get("sessionSelector").add(sessionSelector);
		
		final StudentSectioningWidget widget = new StudentSectioningWidget(sessionSelector, userAuthentication);
		RootPanel.get("loading").setVisible(false);
		RootPanel.get("body").add(widget);
		
		userAuthentication.addUserAuthenticatedHandler(new UserAuthentication.UserAuthenticatedHandler() {
			public void onLogIn(UserAuthenticatedEvent event) {
				sessionSelector.selectSession();
			}

			public void onLogOut(UserAuthenticatedEvent event) {
				if (!event.isGuest()) {
					widget.clear();
					sessionSelector.selectSession(null);
				}
				userAuthentication.authenticate();
			}
		});
		
		sessionSelector.addAcademicSessionChangeHandler(new AcademicSessionProvider.AcademicSessionChangeHandler() {
			public void onAcademicSessionChange(AcademicSessionProvider.AcademicSessionChangeEvent event) {
				widget.clear();
				widget.lastRequest(event.getNewAcademicSessionId());
			}
		});
		
		iSectioningService.lastAcademicSession(new AsyncCallback<String[]>() {
			public void onFailure(Throwable caught) {
				if (!userAuthentication.isShowing())
					sessionSelector.selectSession();
			}
			public void onSuccess(String[] result) {
				sessionSelector.selectSession(result);
				widget.lastRequest(sessionSelector.getAcademicSessionId());
			}
		});
	}
	
}
