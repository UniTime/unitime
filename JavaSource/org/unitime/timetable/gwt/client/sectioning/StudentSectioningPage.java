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

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.access.AccessControlClient;
import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.sectioning.UserAuthentication.UserAuthenticatedEvent;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionMatcher;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.SectioningProperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

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
	
	private SimplePanel iContainer = new SimplePanel();
	private AccessControlClient iAccessControlClient = null;
	private HandlerRegistration iWindowCloseHandler = null;
	
	public StudentSectioningPage(final Mode mode) {
		initWidget(iContainer);
		
		iAccessControlClient = new AccessControlClient(){
			@Override
			protected void executeWhenAccessIsGranted() {
				init(mode);
			}
			@Override
			protected void openMainPage() {
				if (iWindowCloseHandler != null) iWindowCloseHandler.removeHandler();
				super.openMainPage();
			}
		};
		iAccessControlClient.checkAccess();
	}
	
	private void init(final Mode mode) {
		final UserAuthentication userAuthentication = new UserAuthentication(UniTimePageHeader.getInstance().getMiddle(), mode.isSectioning() ? !CONSTANTS.isAuthenticationRequired() : false);
		
		if (Location.getParameter("student") == null)
			iSectioningService.whoAmI(new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {
					if (!mode.isSectioning() || CONSTANTS.isAuthenticationRequired() || CONSTANTS.tryAuthenticationWhenGuest()) {
						if (CONSTANTS.allowUserLogin())
							userAuthentication.authenticate();
						else if (!mode.isSectioning() || CONSTANTS.isAuthenticationRequired())
							ToolBox.open(GWT.getHostPageBaseURL() + "login.action?target=" + URL.encodeQueryString(Location.getHref()));
						else
							userAuthentication.authenticated(null);
					}
				}
				public void onSuccess(String result) {
					if (result == null) { // not authenticated
						if (!mode.isSectioning() || CONSTANTS.isAuthenticationRequired() || CONSTANTS.tryAuthenticationWhenGuest()) {
							if (CONSTANTS.allowUserLogin())
								userAuthentication.authenticate();
							else if (!mode.isSectioning() || CONSTANTS.isAuthenticationRequired())
								ToolBox.open(GWT.getHostPageBaseURL() + "login.action?target=" + URL.encodeQueryString(Location.getHref()));
							else
								userAuthentication.authenticated(result);
						} else {
							userAuthentication.authenticated(result);
						}
					} else {
						userAuthentication.authenticated(result);
					}
				}
			});
		
		final AcademicSessionSelector sessionSelector = new AcademicSessionSelector(UniTimePageHeader.getInstance().getRight(), mode);
		
		iSectioningService.getProperties(null, new AsyncCallback<SectioningProperties>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(SectioningProperties result) {
				userAuthentication.setAllowLookup(result.isAdminOrAdvisor());
				if (Location.getParameter("session") != null || Location.getParameter("term") != null)
					sessionSelector.selectSession(new AcademicSessionMatcher() {
						protected boolean matchCampus(AcademicSessionInfo info, String campus) {
							if (info.hasExternalCampus() && campus.equalsIgnoreCase(info.getExternalCampus())) return true;
							return campus.equalsIgnoreCase(info.getCampus());
						}

						protected boolean matchTerm(AcademicSessionInfo info, String term) {
							if (info.hasExternalTerm() && term.equalsIgnoreCase(info.getExternalTerm())) return true;
							return term.equalsIgnoreCase(info.getTerm() + info.getYear()) || term.equalsIgnoreCase(info.getYear() + info.getTerm()) || term.equalsIgnoreCase(info.getTerm() + info.getYear() + info.getCampus());
						}

						protected boolean matchSession(AcademicSessionInfo info, String session) {
							if (info.hasExternalTerm() && info.hasExternalCampus() && session.equalsIgnoreCase(info.getExternalTerm() + info.hasExternalCampus())) return true;
							return session.equalsIgnoreCase(info.getTerm() + info.getYear() + info.getCampus()) || session.equalsIgnoreCase(info.getTerm() + info.getYear()) || session.equals(info.getSessionId().toString());
						}

						@Override
						public boolean match(AcademicSessionInfo info) {
							String campus = Location.getParameter("campus");
							if (campus != null && !matchCampus(info, campus)) return false;
							String term = Location.getParameter("term");
							if (term != null && !matchTerm(info, term)) return false;
							String session = Location.getParameter("session");
							if (session != null && !matchSession(info, session)) return false;
							return true;
						}
					}, new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
						}

						@Override
						public void onSuccess(Boolean result) {
							if (Location.getParameter("student") != null && userAuthentication.isAllowLookup())
								UserAuthentication.personFound(Location.getParameter("student"));
						}
					});
			}
		});
		
		UniTimePageHeader.getInstance().getLeft().setVisible(false);
		UniTimePageHeader.getInstance().getLeft().setPreventDefault(true);
		
		final StudentSectioningWidget widget = new StudentSectioningWidget(true, sessionSelector, userAuthentication, mode, true);
		
		iContainer.setWidget(widget);
		
		UniTimePageHeader.getInstance().getRight().setClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (widget.isChanged()) {
					UniTimeConfirmationDialog.confirm(widget.useDefaultConfirmDialog(), mode.isSectioning() ? MESSAGES.queryLeaveChangesOnClassSchedule() : MESSAGES.queryLeaveChangesOnCourseRequests(), new Command() {
						@Override
						public void execute() {
							sessionSelector.selectSession();
						}
					});
				} else {
					sessionSelector.selectSession();
				}
			}
		});
		UniTimePageHeader.getInstance().getMiddle().setClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (CONSTANTS.allowUserLogin()) {
					if (widget.isChanged()) {
						UniTimeConfirmationDialog.confirm(widget.useDefaultConfirmDialog(), mode.isSectioning() ? MESSAGES.queryLeaveChangesOnClassSchedule() : MESSAGES.queryLeaveChangesOnCourseRequests(), new Command() {
							@Override
							public void execute() {
								if (userAuthentication.isLoggedIn())
									userAuthentication.logOut();
								else
									userAuthentication.authenticate();
							}
						});
					} else {
						if (userAuthentication.isLoggedIn())
							userAuthentication.logOut();
						else
							userAuthentication.authenticate();
					}
				} else if (userAuthentication.isAllowLookup()) {
					userAuthentication.doLookup();
				} else if (userAuthentication.isLoggedIn()) {
					ToolBox.open(GWT.getHostPageBaseURL() + "logout.action");
				} else {
					ToolBox.open(GWT.getHostPageBaseURL() + "login.action?target=" + URL.encodeQueryString(Location.getHref()));
				}
			}
		});


		userAuthentication.addUserAuthenticatedHandler(new UserAuthentication.UserAuthenticatedHandler() {
			public void onLogIn(UserAuthenticatedEvent event) {
				if (!mode.isSectioning())
					sessionSelector.selectSession(null, false);
				sessionSelector.selectSession();
			}

			public void onLogOut(UserAuthenticatedEvent event) {
				if (!event.isGuest()) {
					widget.clearMessage();
					widget.clear();
					// sessionSelector.selectSession(null);
				}
				userAuthentication.authenticate();
			}
		});
		
		sessionSelector.addAcademicSessionChangeHandler(new AcademicSessionProvider.AcademicSessionChangeHandler() {
			public void onAcademicSessionChange(AcademicSessionProvider.AcademicSessionChangeEvent event) {
				if (event.isChanged()) {
					widget.clearMessage();
					widget.clear();
				}
				widget.setSessionId(event.getNewAcademicSessionId());
				widget.setStudentId(null);
				widget.checkEligibility(null, userAuthentication.getPin());
				userAuthentication.setLookupOptions("mustHaveExternalId,source=students,session=" + event.getNewAcademicSessionId());
			}
		});
		
		if (Location.getParameter("session") == null && Location.getParameter("term") == null)
			iSectioningService.lastAcademicSession(mode.isSectioning(), new AsyncCallback<AcademicSessionProvider.AcademicSessionInfo>() {
				public void onFailure(Throwable caught) {
					if (!userAuthentication.isShowing() && !UniTimeFrameDialog.hasDialog())
						sessionSelector.selectSession();
				}
				public void onSuccess(AcademicSessionProvider.AcademicSessionInfo result) {
					sessionSelector.selectSession(result, true);
				}
			});
		
		iWindowCloseHandler = Window.addWindowClosingHandler(new Window.ClosingHandler() {
			@Override
			public void onWindowClosing(ClosingEvent event) {
				if (widget.isChanged()) {
					if (LoadingWidget.getInstance().isShowing())
						LoadingWidget.getInstance().hide();
					event.setMessage(mode.isSectioning() ? MESSAGES.queryLeaveChangesOnClassSchedule() : MESSAGES.queryLeaveChangesOnCourseRequests());
				}
			}
		});
	}
}
