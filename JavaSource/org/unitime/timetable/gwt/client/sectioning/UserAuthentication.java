/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import java.util.ArrayList;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaDialogBox;
import org.unitime.timetable.gwt.client.aria.AriaPasswordTextBox;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.page.InfoPanel;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.UserAuthenticationProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author Tomas Muller
 */
public class UserAuthentication implements UserAuthenticationProvider {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	private InfoPanel iPanel;
	
	private AriaButton iLogIn, iSkip, iLookup;
	
	private AriaTextBox iUserName;
	private AriaPasswordTextBox iUserPassword;
	private AriaTextBox iPin = null;
	
	private DialogBox iDialog;
	
	private Label iError;
	
	private ArrayList<UserAuthenticatedHandler> iUserAuthenticatedHandlers = new ArrayList<UserAuthenticatedHandler>();
	
	private static final SectioningServiceAsync sSectioningService = GWT.create(SectioningService.class);
	
	private static AsyncCallback<String> sAuthenticateCallback = null;
	
	private boolean iLoggedIn = false;
	private boolean iGuest = false;
	private String iLastUser = null;
	private boolean iAllowGuest = false;
	
	private Command iOnLoginCommand = null;
	
	private Lookup iLookupDialog = null;

	public UserAuthentication(InfoPanel panel, boolean allowGuest) {
		iAllowGuest = allowGuest;
		iPanel = panel;
		iPanel.setPreventDefault(true);
		iPanel.setVisible(true);
		iPanel.setText(MESSAGES.userNotAuthenticated());
		iPanel.setHint(MESSAGES.userHint());
		iPanel.setAriaLabel(ARIA.userNotAuthenticated());
		iPanel.setInfo(null);
		
		iDialog = new AriaDialogBox();
		iDialog.setText(MESSAGES.dialogAuthenticate());
		iDialog.setAnimationEnabled(true);
		iDialog.setAutoHideEnabled(false);
		iDialog.setGlassEnabled(true);
		iDialog.setModal(true);
		iDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				iUserName.setText("");
				iUserPassword.setText("");
				if (iPin != null) iPin.setText("");
			}
		});
		
		FlexTable grid = new FlexTable();
		grid.setCellPadding(5);
		grid.setCellSpacing(0);
		int row = 0;
		grid.setText(row, 0, MESSAGES.username());
		iUserName = new AriaTextBox();
		iUserName.setStyleName("gwt-SuggestBox");
		iUserName.setAriaLabel(ARIA.propUserName());
		grid.setWidget(row, 1, iUserName);
		row++;
		
		grid.setText(row, 0, MESSAGES.password());
		iUserPassword = new AriaPasswordTextBox();
		iUserPassword.setStyleName("gwt-SuggestBox");
		iUserPassword.setAriaLabel(ARIA.propPassword());
		grid.setWidget(row, 1, iUserPassword);
		row++;
		
		if (CONSTANTS.hasAuthenticationPin()) {
			grid.setText(row, 0, MESSAGES.pin());
			iPin = new AriaTextBox();
			iPin.setStyleName("gwt-SuggestBox");
			iPin.setAriaLabel(ARIA.propPinNumber());
			grid.setWidget(row, 1, iPin);	
			row++;
		}
		
		iError = new Label();
		iError.setStyleName("unitime-ErrorMessage");
		iError.setVisible(false);
		grid.getFlexCellFormatter().setColSpan(row, 0, 2);
		grid.setWidget(row, 0, iError);
		row++;
		
		HorizontalPanel buttonPanelWithPad = new HorizontalPanel();
		buttonPanelWithPad.setWidth("100%");
		HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.setSpacing(5);
		buttonPanelWithPad.add(buttonPanel);
		buttonPanelWithPad.setCellHorizontalAlignment(buttonPanel, HasHorizontalAlignment.ALIGN_RIGHT);

		grid.getFlexCellFormatter().setColSpan(row, 0, 2);
		grid.setWidget(row, 0, buttonPanelWithPad);
		row++;
		
		iLogIn = new AriaButton(MESSAGES.buttonUserLogin());
		buttonPanel.add(iLogIn);
		
		iSkip = new AriaButton(MESSAGES.buttonUserSkip());
		buttonPanel.add(iSkip);
		iSkip.setVisible(iAllowGuest);
		iSkip.setAriaLabel(ARIA.buttonLogInAsGuest());
		
		iLookupDialog = new Lookup();
		iLookupDialog.setOptions("mustHaveExternalId,source=students");
		iLookupDialog.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				if (event.getValue() != null)
					sSectioningService.logIn("LOOKUP", event.getValue().getId(), null, sAuthenticateCallback);
			}
		});
		iLookup = new AriaButton(MESSAGES.buttonUserLookup());
		buttonPanel.add(iLookup);
		iLookup.setVisible(false);
		
		iSkip.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				logIn(true);
			}
		});
		
		iLogIn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				logIn(false);
			}
		});
		
		iLookup.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iDialog.hide();
				iLookupDialog.center();
			}
		});
		
		iUserName.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iUserPassword.selectAll();
							iUserPassword.setFocus(true);
						}
					});
				}
				if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE && iAllowGuest) {
					logIn(true);
				}
			}
		});

		iUserPassword.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					logIn(false);
				}
				if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE && iAllowGuest) {
					logIn(true);
				}
			}
		});
		
		iDialog.add(grid);
		
		ClickHandler ch = new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (iLoggedIn) logOut();
				else authenticate();
			}
		};
		
		iPanel.setClickHandler(ch);
		
		sAuthenticateCallback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				AriaStatus.getInstance().setText(caught.getMessage());
				iError.setText(caught.getMessage());
				iError.setVisible(true);
				iUserName.setEnabled(true);
				iUserPassword.setEnabled(true);
				if (iPin != null)
					iPin.setEnabled(true);
				iLogIn.setEnabled(true);
				iSkip.setEnabled(true);
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						iUserName.selectAll();
						iUserName.setFocus(true);
					}
				});
			}
			public void onSuccess(String result) {
				iUserName.setEnabled(true);
				iUserPassword.setEnabled(true);
				if (iPin != null)
					iPin.setEnabled(true);
				iLogIn.setEnabled(true);
				iSkip.setEnabled(true);
				iError.setVisible(false);
				iDialog.hide();
				iPanel.setText(MESSAGES.userLabel(result));
				iLastUser = result;
				iPanel.setHint(MESSAGES.userHintLogout());
				iPanel.setAriaLabel(ARIA.userAuthenticated(result));
				iLoggedIn = true;
				iGuest = false;
				UserAuthenticatedEvent e = new UserAuthenticatedEvent(iGuest);
				for (UserAuthenticatedHandler h: iUserAuthenticatedHandlers)
					h.onLogIn(e);
				if (iOnLoginCommand != null) iOnLoginCommand.execute();
				Client.reloadMenu();
			}
		};
	}
	
	public void setLookupOptions(String options) { iLookupDialog.setOptions(options); }
	
	public void setAllowLookup(boolean allow) {
		iLookup.setVisible(allow);
	}
	
	public boolean isShowing() {
		return iDialog.isShowing();
	}
	
	public void authenticate() {
		AriaStatus.getInstance().setText(ARIA.authenticationDialogOpened());
		iError.setVisible(false);
		iDialog.center();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				iUserName.selectAll();
				iUserName.setFocus(true);
			}
		});
	}
	
	public void authenticated(String user) {
		if (iDialog.isShowing()) iDialog.hide();
		iLoggedIn = true;
		iGuest = MESSAGES.userGuest().equals(user);
		iPanel.setText(MESSAGES.userLabel(user));
		iLastUser = user;
		iPanel.setHint(iGuest ? MESSAGES.userHintLogin() : MESSAGES.userHintLogout());
		iPanel.setAriaLabel(iGuest ? ARIA.userGuest() : ARIA.userAuthenticated(user));
	}
	
	private void logIn(boolean guest) {
		iError.setVisible(false);
		if (guest) {
			sSectioningService.logOut(new AsyncCallback<Boolean>() {
				public void onFailure(Throwable caught) { }
				public void onSuccess(Boolean result) {
					iLoggedIn = true; iGuest = true;
					iDialog.hide();
					iPanel.setText(MESSAGES.userLabel(MESSAGES.userGuest()));
					iPanel.setHint(MESSAGES.userHintLogin());
					iPanel.setAriaLabel(ARIA.userGuest());
					iLastUser = MESSAGES.userGuest();
					UserAuthenticatedEvent e = new UserAuthenticatedEvent(iGuest);
					for (UserAuthenticatedHandler h: iUserAuthenticatedHandlers)
						h.onLogIn(e);
					if (iOnLoginCommand != null) iOnLoginCommand.execute();
					Client.reloadMenu();
				}
			});
			return;
		}
		iUserName.setEnabled(false);
		iUserPassword.setEnabled(false);
		if (iPin != null)
			iPin.setEnabled(false);
		iLogIn.setEnabled(false);
		iSkip.setEnabled(false);
		sSectioningService.logIn(iUserName.getText(), iUserPassword.getText(), (iPin == null ? null : iPin.getText()), sAuthenticateCallback);
	}
	
	public void logOut() {
		sSectioningService.logOut(new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) { }
			public void onSuccess(Boolean result) {
				if (result) {
					UserAuthenticatedEvent e = new UserAuthenticatedEvent(iGuest);
					iPanel.setHint(MESSAGES.userHintClose());
					iPanel.setText(MESSAGES.userNotAuthenticated());
					iPanel.setAriaLabel(ARIA.userNotAuthenticated());
					iLastUser = null;
					for (UserAuthenticatedHandler h: iUserAuthenticatedHandlers)
						h.onLogOut(e);
					iLoggedIn = false;
					Client.reloadMenu();
				} else {
					sSectioningService.whoAmI(new AsyncCallback<String>() {
						@Override
						public void onFailure(Throwable caught) {
							UserAuthenticatedEvent e = new UserAuthenticatedEvent(iGuest);
							iPanel.setHint(MESSAGES.userHintClose());
							iPanel.setText(MESSAGES.userNotAuthenticated());
							iPanel.setAriaLabel(ARIA.userNotAuthenticated());
							iLastUser = null;
							for (UserAuthenticatedHandler h: iUserAuthenticatedHandlers)
								h.onLogOut(e);
							iLoggedIn = false;
							Client.reloadMenu();
						}

						@Override
						public void onSuccess(String result) {
							authenticated(result);
							UserAuthenticatedEvent e = new UserAuthenticatedEvent(iGuest);
							for (UserAuthenticatedHandler h: iUserAuthenticatedHandlers)
								h.onLogOut(e);
							Client.reloadMenu();
						}
					});
				}
			}
		});
	}
	
	public String getUser() {
		return iLastUser;
	}
	
	public void setUser(final String user, final AsyncCallback<Boolean> callback) {
		iOnLoginCommand = null;
		if (user == null) {
			callback.onSuccess(false);
			authenticate();
		} else if (user.equals(iLastUser)) {
			callback.onSuccess(true);
		} else if (user.equals(MESSAGES.userGuest())) {
			logIn(true);
			callback.onSuccess(true);
		} else {
			iOnLoginCommand = new Command() {
				public void execute() {
					callback.onSuccess(user.equals(getUser()));
				}
			};
			iPanel.setText(user);
			iPanel.setAriaLabel(ARIA.userAuthenticated(user));
			authenticate();
		}
	}
	
	public static class UserAuthenticatedEvent {
		private boolean iGuest = false;
		private UserAuthenticatedEvent(boolean guest) { iGuest = guest; }
		public boolean isGuest() { return iGuest; }
	}
	
	public static interface UserAuthenticatedHandler {
		public void onLogIn(UserAuthenticatedEvent event);
		public void onLogOut(UserAuthenticatedEvent event);
	}
	
	public void addUserAuthenticatedHandler(UserAuthenticatedHandler h) {
		iUserAuthenticatedHandlers.add(h);
	}
	
	public static void personFound(String externalUniqueId) {
		sSectioningService.logIn("LOOKUP", externalUniqueId, null, sAuthenticateCallback);
	}
	
	public boolean isLoggedIn() {
		return iLoggedIn;
	}
	
	private native JavaScriptObject createLookupCallback() /*-{
		return function(person) {
			@org.unitime.timetable.gwt.client.sectioning.UserAuthentication::personFound(Ljava/lang/String;)(person[0]);
	    };
	 }-*/;
	
}
