/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.sectioning;

import org.unitime.timetable.gwt.client.sectioning.UserAuthentication.UserAuthenticatedEvent;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class StudentSectioningPage extends Composite {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	public static enum Mode {
		SECTIONING(true),
		REQUESTS(false);
		boolean iSectioning;
		private Mode(boolean isSectioning) { iSectioning = isSectioning; }
		public boolean isSectioning() { return iSectioning; }
	};
	
	public StudentSectioningPage(final Mode mode) {
		Grid titlePanel = new Grid(1, 3);
		titlePanel.getCellFormatter().setWidth(0, 0, "33%");
		titlePanel.getCellFormatter().setWidth(0, 1, "34%");
		titlePanel.getCellFormatter().setWidth(0, 2, "33%");
		titlePanel.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		titlePanel.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		titlePanel.getCellFormatter().getElement(0, 2).getStyle().setPaddingLeft(10, Unit.PX);
		titlePanel.setHTML(0, 0, "&nbsp;");
		
		final UserAuthentication userAuthentication = new UserAuthentication(mode.isSectioning() ? !CONSTANTS.isAuthenticationRequired() : false);
		titlePanel.setWidget(0, 1, userAuthentication);
		
		if (Window.Location.getParameter("student") == null)
			iSectioningService.whoAmI(new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {
					userAuthentication.authenticate();
				}
				public void onSuccess(String result) {
					if (MESSAGES.userGuest().equals(result)) { // user is guest (i.e., not truly authenticated)
						if (!mode.isSectioning() || CONSTANTS.isAuthenticationRequired() || CONSTANTS.tryAuthenticationWhenGuest())
							userAuthentication.authenticate();
						else
							userAuthentication.authenticated(result);
					} else {
						userAuthentication.authenticated(result);
					}
				}
			});
		
		final AcademicSessionSelector sessionSelector = new AcademicSessionSelector(mode);
		titlePanel.setWidget(0, 2, sessionSelector);
		
		iSectioningService.isAdminOrAdvisor(new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(Boolean result) {
				if (result) {
					userAuthentication.setAllowLookup(true);
					if (Window.Location.getParameter("session") != null)
						sessionSelector.selectSession(Long.valueOf(Window.Location.getParameter("session")), new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
							}

							@Override
							public void onSuccess(Boolean result) {
								if (Window.Location.getParameter("student") != null)
									UserAuthentication.personFound(Window.Location.getParameter("student"));
							}
						});
				}
			}
		});

		
		RootPanel.get("UniTimeGWT:Header").clear();
		RootPanel.get("UniTimeGWT:Header").add(titlePanel);

		final StudentSectioningWidget widget = new StudentSectioningWidget(true, sessionSelector, userAuthentication, mode, true);
		
		initWidget(widget);

		userAuthentication.addUserAuthenticatedHandler(new UserAuthentication.UserAuthenticatedHandler() {
			public void onLogIn(UserAuthenticatedEvent event) {
				if (!mode.isSectioning())
					sessionSelector.selectSession(null, false);
				sessionSelector.selectSession();
			}

			public void onLogOut(UserAuthenticatedEvent event) {
				if (!event.isGuest()) {
					widget.clear();
					// sessionSelector.selectSession(null);
				}
				userAuthentication.authenticate();
			}
		});
		
		sessionSelector.addAcademicSessionChangeHandler(new AcademicSessionProvider.AcademicSessionChangeHandler() {
			public void onAcademicSessionChange(AcademicSessionProvider.AcademicSessionChangeEvent event) {
				if (event.isChanged())
					widget.clear();
				widget.lastRequest(event.getNewAcademicSessionId());
				userAuthentication.setLookupOptions("mustHaveExternalId,source=students,session=" + event.getNewAcademicSessionId());
			}
		});
		
		if (Window.Location.getParameter("session") == null)
			iSectioningService.lastAcademicSession(mode.isSectioning(), new AsyncCallback<AcademicSessionProvider.AcademicSessionInfo>() {
				public void onFailure(Throwable caught) {
					if (!userAuthentication.isShowing())
						sessionSelector.selectSession();
				}
				public void onSuccess(AcademicSessionProvider.AcademicSessionInfo result) {
					sessionSelector.selectSession(result, false);
					widget.lastRequest(sessionSelector.getAcademicSessionId());
					userAuthentication.setLookupOptions("mustHaveExternalId,source=students,session=" + sessionSelector.getAcademicSessionId());
				}
			});
	}
}
