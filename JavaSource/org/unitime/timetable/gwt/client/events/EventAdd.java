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
package org.unitime.timetable.gwt.client.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.events.AcademicSessionSelectionBox.AcademicSession;
import org.unitime.timetable.gwt.client.events.EventMeetingTable.EventMeetingRow;
import org.unitime.timetable.gwt.client.events.EventMeetingTable.OperationType;
import org.unitime.timetable.gwt.client.events.StartEndTimeSelector.StartEndTime;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.SaveOrApproveEventRpcResponse;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.DateFlagsProvider;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventEnrollmentsRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConflictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MessageInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectLookupRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SaveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.SelectionInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;
import org.unitime.timetable.gwt.shared.EventInterface.StandardEventNoteInterface;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class EventAdd extends Composite implements EventMeetingTable.Implementation, AcademicSessionSelectionBox.AcademicSessionFilter {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	
	private String iMainExternalId = null;
	private UniTimeWidget<TextBox> iName;
	private NumberBox iLimit;
	private ListBox iSponsors;
	private UniTimeWidget<ListBox> iEventType;
	private TextArea iNotes, iEmails;
	private TextBox iMainFName, iMainMName, iMainPhone, iMainTitle;
	private UniTimeWidget<TextBox> iMainLName, iMainEmail;
	private CheckBox iReqAttendance;
	private ListBox iStandardNotes;
	private SingleDateSelector iExpirationDate;
	private CheckBox iMainContactChanged;
	private int iMainContactChangedRow;
	private ContactInterface iOriginalContact;
	
	private SimpleForm iMainContact;
	private SimpleForm iCoursesForm;
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter, iMeetingsHeader;
	private CheckBox iEmailConfirmationHeader, iEmailConfirmationFooter;
	
	private EventMeetingTable iMeetings;
	
	private CourseRelatedObjectsTable iCourses;
	
	private AddMeetingsDialog iEventAddMeetings, iEventModifyMeetings;
	private AcademicSessionSelectionBox iSession;
	private Lookup iLookup, iAdditionalLookup;
	private UniTimeTable<ContactInterface> iContacts;
	private int iContactRow, iAcademicTitleRow;
	
	private EnrollmentTable iEnrollments;
	private UniTimeHeaderPanel iEnrollmentHeader;
	private int iEnrollmentRow;
	private Button iLookupButton, iAdditionalLookupButton, iStandardNotesButton, iMainContactResetButton;
	private UniTimeDialogBox iStandardNotesBox;
	
	private EventInterface iEvent, iSavedEvent;
	private EventPropertiesProvider iProperties;
	private int iSessionRow = -1;
	
	private UniTimeFileUpload iFileUpload;
	private List<MeetingInterface> iSelection = null;
	private CheckBox iShowDeleted;
			
	public EventAdd(AcademicSessionSelectionBox session, EventPropertiesProvider properties) {
		iSession = session;
		iProperties = properties;
		iForm = new SimpleForm();
		
		iLookup = new Lookup();
		iLookup.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				if (event.getValue() != null) {
					iMainExternalId = event.getValue().getId();
					iMainFName.setText(event.getValue().getFirstName() == null ? "" : event.getValue().getFirstName());
					iMainMName.setText(event.getValue().getMiddleName() == null ? "" : event.getValue().getMiddleName());
					iMainLName.getWidget().setText(event.getValue().getLastName() == null ? "" : event.getValue().getLastName());
					iMainTitle.setText(event.getValue().getAcademicTitle() == null ? "" : event.getValue().getAcademicTitle());
					iMainPhone.setText(event.getValue().getPhone() == null ? "" : event.getValue().getPhone());
					iMainEmail.getWidget().setText(event.getValue().getEmail() == null ? "" : event.getValue().getEmail());
					iOriginalContact = new ContactInterface(event.getValue());
					iMainContactChanged.setValue(false, true);
					checkMainContactChanged();
				}
			}
		});
		iAdditionalLookup = new Lookup();
		iAdditionalLookup.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				if (event.getValue() != null) {
					final ContactInterface contact = new ContactInterface(event.getValue());
					List<Widget> row = new ArrayList<Widget>();
					row.add(new Label(contact.getName(MESSAGES), false));
					row.add(new Label(contact.hasEmail() ? contact.getEmail() : "", false));
					row.add(new Label(contact.hasPhone() ? contact.getPhone() : "", false));
					Image remove = new Image(RESOURCES.delete());
					remove.addStyleName("remove");
					remove.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							for (int row = 1; row < iContacts.getRowCount(); row ++)
								if (contact.equals(iContacts.getData(row))) {
									iContacts.removeRow(row);
									break;
								}
							iForm.getRowFormatter().setVisible(iContactRow, iContacts.getRowCount() > 1);
						}
					});
					row.add(remove);
					int nrInstructors = 0;
					for (int r = 1; r < iContacts.getRowCount(); r ++)
						if (iContacts.getData(r) == null) nrInstructors ++;
					int rowNum;
					if (nrInstructors == 0) {
						rowNum = iContacts.addRow(contact, row);
					} else {
						rowNum = iContacts.insertRow(iContacts.getRowCount() - nrInstructors);
						iContacts.setRow(rowNum, contact, row);
					}
					for (int col = 0; col < iContacts.getCellCount(rowNum); col++)
						iContacts.getCellFormatter().addStyleName(rowNum, col, "additional-contact");
				}
				iForm.getRowFormatter().setVisible(iContactRow, iContacts.getRowCount() > 1);
			}
		});
		iLookup.setOptions("mustHaveExternalId" + (iSession.getAcademicSessionId() == null ? "" : ",session=" + iSession.getAcademicSessionId()));
		iAdditionalLookup.setOptions("mustHaveExternalId" + (iSession.getAcademicSessionId() == null ? "" : ",session=" + iSession.getAcademicSessionId()));
		iSession.addAcademicSessionChangeHandler(new AcademicSessionProvider.AcademicSessionChangeHandler() {
			@Override
			public void onAcademicSessionChange(AcademicSessionProvider.AcademicSessionChangeEvent event) {
				iLookup.setOptions("mustHaveExternalId,session=" + event.getNewAcademicSessionId());
				iAdditionalLookup.setOptions("mustHaveExternalId,session=" + event.getNewAcademicSessionId());
			}
		});
		
		
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectEvent());
		ClickHandler clickCreateOrUpdate = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iSavedEvent = null;
				validate(new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						UniTimeNotifications.error(MESSAGES.failedValidation(caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(Boolean result) {
						if (result) {
							final EventInterface event = getEvent();
							LoadingWidget.getInstance().show(event.getId() == null ? MESSAGES.waitCreate(event.getName()) : MESSAGES.waitUpdate(event.getName()));
							RPC.execute(SaveEventRpcRequest.saveEvent(getEvent(), iSession.getAcademicSessionId(), getMessage(), isSendEmailConformation()), new AsyncCallback<SaveOrApproveEventRpcResponse>() {

								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									String message = (event.getId() == null ? MESSAGES.failedCreate(event.getName(), caught.getMessage()) : MESSAGES.failedUpdate(event.getName(), caught.getMessage()));
									iHeader.setErrorMessage(message);
									UniTimeNotifications.error(message, caught);
								}

								@Override
								public void onSuccess(SaveOrApproveEventRpcResponse result) {
									LoadingWidget.getInstance().hide();
									iSavedEvent = result.getEvent();
									if (result.hasMessages())
										for (MessageInterface m: result.getMessages()) {
											if (m.isError())
												UniTimeNotifications.warn(m.getMessage());
											else if (m.isWarning())
												UniTimeNotifications.error(m.getMessage());
											else
												UniTimeNotifications.info(m.getMessage());
										}
									hide();
								}
							});
						}
					}
				});
			}
		};
		iHeader.addButton("create", MESSAGES.buttonCreateEvent(), 100, clickCreateOrUpdate);
		iHeader.addButton("update", MESSAGES.buttonUpdateEvent(), 100, clickCreateOrUpdate);
		iHeader.addButton("delete", MESSAGES.buttonDeleteEvent(), 100, new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				if (!Window.confirm(MESSAGES.confirmDeleteEvent())) return;
				final EventInterface event = getEvent();
				if (event.hasMeetings()) event.getMeetings().clear();
				LoadingWidget.getInstance().show(MESSAGES.waitDelete(event.getName()));
				RPC.execute(SaveEventRpcRequest.saveEvent(event, iSession.getAcademicSessionId(), getMessage(), isSendEmailConformation()), new AsyncCallback<SaveOrApproveEventRpcResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.failedDelete(event.getName(), caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedDelete(event.getName(), caught.getMessage()), caught);
					}

					@Override
					public void onSuccess(SaveOrApproveEventRpcResponse result) {
						LoadingWidget.getInstance().hide();
						iSavedEvent = result.getEvent();
						if (result.hasMessages())
							for (MessageInterface m: result.getMessages()) {
								if (m.isError())
									UniTimeNotifications.warn(m.getMessage());
								else if (m.isWarning())
									UniTimeNotifications.error(m.getMessage());
								else
									UniTimeNotifications.info(m.getMessage());
							}
						hide();
					}
				});
			}
		});
		iHeader.addButton("cancel", MESSAGES.buttonCancelEvent(), 100, new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				if (!Window.confirm(MESSAGES.confirmCancelEvent())) return;
				final EventInterface event = getEvent();
				if (event.hasMeetings()) {
					for (Iterator<MeetingInterface> i = event.getMeetings().iterator(); i.hasNext(); ) {
						MeetingInterface m = i.next();
						if (m.getId() == null)
							i.remove();
						else if (m.isCanCancel())
							m.setApprovalStatus(ApprovalStatus.Cancelled);
						else if (m.isCanDelete() && (m.getApprovalStatus() == ApprovalStatus.Pending || m.getApprovalStatus() == ApprovalStatus.Approved))
							i.remove();
					}
				}
				LoadingWidget.getInstance().show(MESSAGES.waitCancel(event.getName()));
				RPC.execute(SaveEventRpcRequest.saveEvent(event, iSession.getAcademicSessionId(), getMessage(), isSendEmailConformation()), new AsyncCallback<SaveOrApproveEventRpcResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.failedCancel(event.getName(), caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedCancel(event.getName(), caught.getMessage()), caught);
					}

					@Override
					public void onSuccess(SaveOrApproveEventRpcResponse result) {
						LoadingWidget.getInstance().hide();
						iSavedEvent = result.getEvent();
						if (result.hasMessages())
							for (MessageInterface m: result.getMessages()) {
								if (m.isError())
									UniTimeNotifications.warn(m.getMessage());
								else if (m.isWarning())
									UniTimeNotifications.error(m.getMessage());
								else
									UniTimeNotifications.info(m.getMessage());
							}
						hide();
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		iForm.addHeaderRow(iHeader);
		
		iSessionRow = iForm.addRow(MESSAGES.propAcademicSession(), new Label());
		
		iName = new UniTimeWidget<TextBox>(new TextBox());
		iName.getWidget().setStyleName("unitime-TextBox");
		iName.getWidget().setMaxLength(100);
		iName.getWidget().setWidth("480px");
		iName.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iName.clearHint();
				iHeader.clearMessage();
			}
		});
		iForm.addRow(MESSAGES.propEventName(), iName);
		
		iSponsors = new ListBox();
		iForm.addRow(MESSAGES.propSponsor(), iSponsors);
		
		iEventType = new UniTimeWidget<ListBox>(new ListBox());
		iEventType.getWidget().addItem(EventInterface.EventType.Special.getName(CONSTANTS), EventInterface.EventType.Special.name());
		iForm.addRow(MESSAGES.propEventType(), iEventType);
		
		iLimit = new NumberBox();
		iLimit.setStyleName("unitime-TextBox");
		iLimit.setMaxLength(10);
		iLimit.setWidth("50px");
		iForm.addRow(MESSAGES.propAttendance(), iLimit);

		iCourses = new CourseRelatedObjectsTable(iSession);
		iCourses.addValueChangeHandler(new ValueChangeHandler<List<RelatedObjectInterface>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<RelatedObjectInterface>> event) {
				checkEnrollments(event.getValue(), iMeetings.getMeetings());
			}
		});
		
		iReqAttendance = new CheckBox(MESSAGES.checkRequiredAttendance());
						
		iMainContact = new SimpleForm();
		iMainContact.getElement().getStyle().clearWidth();
		iMainContact.removeStyleName("unitime-NotPrintableBottomLine");
		
		iLookupButton = new Button(MESSAGES.buttonLookupMainContact());
		iLookupButton.setWidth("75px");
		Character lookupAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonLookupMainContact());
		if (lookupAccessKey != null) iLookupButton.setAccessKey(lookupAccessKey);
		iLookupButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (!iLookupButton.isVisible()) return;
					iLookup.setQuery((iMainFName.getText() + (iMainMName.getText().isEmpty() ? "" : " " + iMainMName.getText()) + " " + iMainLName.getWidget().getText()).trim()); 
					iLookup.center();
				}
		});
		iLookupButton.setVisible(false);
		
		iAdditionalLookupButton = new Button(MESSAGES.buttonLookupAdditionalContact());
		iAdditionalLookupButton.setWidth("125px");
		Character additionalLookupAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonLookupAdditionalContact());
		if (additionalLookupAccessKey != null) iAdditionalLookupButton.setAccessKey(additionalLookupAccessKey);
		iAdditionalLookupButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (iAdditionalLookupButton.isVisible()) iAdditionalLookup.center();
				}
		});
		iAdditionalLookupButton.setVisible(false);
		
		iMainContactResetButton = new Button(MESSAGES.buttonResetMainContact());
		iMainContactResetButton.setWidth("75px");
		Character resetAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonResetMainContact());
		if (resetAccessKey != null) iMainContactResetButton.setAccessKey(resetAccessKey);
		iMainContactResetButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (iLookupButton.isVisible()) { iOriginalContact = null; }
					if (iOriginalContact == null) {
						iMainExternalId = null;
						iMainFName.setText("");
						iMainMName.setText("");
						iMainLName.getWidget().setText("");
						iMainTitle.setText("");
						iMainPhone.setText("");
						iMainEmail.getWidget().setText("");
					} else {
						iMainExternalId = iOriginalContact.getExternalId();
						iMainFName.setText(iOriginalContact.hasFirstName() ? iOriginalContact.getFirstName() : "");
						iMainMName.setText(iOriginalContact.hasMiddleName() ? iOriginalContact.getMiddleName() : "");
						iMainLName.getWidget().setText(iOriginalContact.hasLastName() ? iOriginalContact.getLastName() : "");
						iMainTitle.setText(iOriginalContact.hasAcademicTitle() ? iOriginalContact.getAcademicTitle() : "");
						iMainPhone.setText(iOriginalContact.hasPhone() ? iOriginalContact.getPhone() : "");
						iMainEmail.getWidget().setText(iOriginalContact.hasEmail() ? iOriginalContact.getEmail() : "");
					}
					iMainContactChanged.setValue(false, true);
					checkMainContactChanged();
				}
		});
		iMainContactResetButton.setVisible(false);
		
		
		iMainFName = new TextBox();
		iMainFName.setStyleName("unitime-TextBox");
		iMainFName.setMaxLength(100);
		iMainFName.setWidth("285px");
		iMainContact.addRow(MESSAGES.propFirstName(), iMainFName);
		iMainContact.setWidget(0, 2, iLookupButton);
		iMainContact.setWidget(0, 3, iMainContactResetButton);
		
		iMainMName = new TextBox();
		iMainMName.setStyleName("unitime-TextBox");
		iMainMName.setMaxLength(100);
		iMainMName.setWidth("285px");
		iMainContact.addRow(MESSAGES.propMiddleName(), iMainMName);
		
		iMainLName = new UniTimeWidget<TextBox>(new TextBox());
		iMainLName.getWidget().setStyleName("unitime-TextBox");
		iMainLName.getWidget().setMaxLength(100);
		iMainLName.getWidget().setWidth("285px");
		iMainLName.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iMainLName.clearHint();
				iHeader.clearMessage();
			}
		});
		iMainContact.addRow(MESSAGES.propLastName(), iMainLName);
		
		iMainTitle = new TextBox();
		iMainTitle.setStyleName("unitime-TextBox");
		iMainTitle.setMaxLength(50);
		iMainTitle.setWidth("285px");
		iAcademicTitleRow = iMainContact.addRow(MESSAGES.propAcademicTitle(), iMainTitle);
		
		iMainEmail = new UniTimeWidget<TextBox>(new TextBox());
		iMainEmail.getWidget().setStyleName("unitime-TextBox");
		iMainEmail.getWidget().setMaxLength(200);
		iMainEmail.getWidget().setWidth("285px");
		iMainEmail.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iMainEmail.clearHint();
				iHeader.clearMessage();
			}
		});
		iMainContact.addRow(MESSAGES.propEmail(), iMainEmail);
		
		iMainPhone = new TextBox();
		iMainPhone.setStyleName("unitime-TextBox");
		iMainPhone.setMaxLength(35);
		iMainPhone.setWidth("285px");
		iMainContact.addRow(MESSAGES.propPhone(), iMainPhone);
		iMainContact.setWidget(iMainContact.getRowCount() - 1, 2, iAdditionalLookupButton);
		iMainContact.getFlexCellFormatter().setColSpan(iMainContact.getRowCount() - 1, 2, 2);
		
		ValueChangeHandler<String> checkMainContactHandler = new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				checkMainContactChanged();
			}
		};
		iMainFName.addValueChangeHandler(checkMainContactHandler);
		iMainMName.addValueChangeHandler(checkMainContactHandler);
		iMainLName.getWidget().addValueChangeHandler(checkMainContactHandler);
		iMainTitle.addValueChangeHandler(checkMainContactHandler);
		iMainPhone.addValueChangeHandler(checkMainContactHandler);
		iMainEmail.getWidget().addValueChangeHandler(checkMainContactHandler);
		
		iForm.addRow(MESSAGES.propMainContact(), iMainContact);
		
		iMainContactChanged = new CheckBox(MESSAGES.checkYourContactChange());
		iMainContactChangedRow = iForm.addRow("", iMainContactChanged);
		iForm.getRowFormatter().setVisible(iMainContactChangedRow, false);
		iForm.getCellFormatter().setStyleName(iMainContactChangedRow, 1, "unitime-CheckNotConfirmed");
		
		iMainContactChanged.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				checkMainContactChanged();
				iHeader.clearMessage();
			}
		});
		
		iContacts = new UniTimeTable<ContactInterface>();
		iContacts.setStyleName("unitime-EventContacts");
		
		List<Widget> contactHeader = new ArrayList<Widget>();
		contactHeader.add(new UniTimeTableHeader(MESSAGES.colNamePerson()));
		contactHeader.add(new UniTimeTableHeader(MESSAGES.colEmail()));
		contactHeader.add(new UniTimeTableHeader(MESSAGES.colPhone()));
		contactHeader.add(new UniTimeTableHeader("&nbsp;"));
		iContacts.addRow(null, contactHeader);
		
		iContactRow = iForm.addRow(MESSAGES.propAdditionalContacts(), iContacts);
		iForm.getRowFormatter().setVisible(iContactRow, false);
		
		iEmails = new TextArea();
		iEmails.setStyleName("unitime-TextArea");
		iEmails.setVisibleLines(3);
		iEmails.setCharacterWidth(80);
		UniTimeWidget<TextArea> emailsWithHint = new UniTimeWidget<TextArea>(iEmails);
		emailsWithHint.setHint(MESSAGES.hintAdditionalEmails());
		iForm.addRow(MESSAGES.propAdditionalEmails(), emailsWithHint);
		
		iStandardNotes = new ListBox();
		iStandardNotes.setVisibleItemCount(10);
		iStandardNotes.setWidth("600px");
		iStandardNotes.addDoubleClickHandler(new DoubleClickHandler() {
			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				String text = iNotes.getText();
				if (!text.isEmpty() && !text.endsWith("\n"))
					text += "\n";
				text += iStandardNotes.getValue(iStandardNotes.getSelectedIndex());
				iNotes.setText(text);
				iStandardNotesBox.hide();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						iNotes.setFocus(true);							
					}
				});
			}
		});
		iStandardNotes.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					String text = iNotes.getText();
					if (!text.isEmpty() && !text.endsWith("\n"))
						text += "\n";
					text += iStandardNotes.getValue(iStandardNotes.getSelectedIndex());
					iNotes.setText(text);
					event.preventDefault();
					event.stopPropagation();
					iStandardNotesBox.hide();
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iNotes.setFocus(true);							
						}
					});
				}
			}
		});
		UniTimeWidget<ListBox> standardNotesWithHint = new UniTimeWidget<ListBox>(iStandardNotes);
		standardNotesWithHint.setHint(MESSAGES.hintStandardNoteDoubleClickToSelect());
		SimpleForm standardNotesForm = new SimpleForm();
		standardNotesForm.addRow(standardNotesWithHint);
		final UniTimeHeaderPanel standardNotesFooter = new UniTimeHeaderPanel();
		standardNotesForm.addRow(standardNotesFooter);
		iStandardNotesBox = new UniTimeDialogBox(true, false);
		iStandardNotesBox.setText(MESSAGES.dialogStandardNotes());
		iStandardNotesBox.setWidget(standardNotesForm);
		standardNotesFooter.addButton("select", MESSAGES.buttonSelect(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iStandardNotes.getSelectedIndex() >= 0) {
					String text = iNotes.getText();
					if (!text.isEmpty() && !text.endsWith("\n"))
						text += "\n";
					text += iStandardNotes.getValue(iStandardNotes.getSelectedIndex());
					iNotes.setText(text);
				}
				iStandardNotesBox.hide();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						iNotes.setFocus(true);							
					}
				});
			}
		});
		standardNotesFooter.addButton("cancel", MESSAGES.buttonCancel(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iStandardNotesBox.hide();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						iNotes.setFocus(true);							
					}
				});				
			}
		});
		standardNotesFooter.setEnabled("select", false);
		iStandardNotes.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				standardNotesFooter.setEnabled("select", iStandardNotes.getSelectedIndex() >= 0);
			}
		});
		
		iNotes = new TextArea();
		iNotes.setStyleName("unitime-TextArea");
		iNotes.setVisibleLines(5);
		iNotes.setCharacterWidth(80);
		VerticalPanel notesPanel = new VerticalPanel();
		notesPanel.add(iNotes);
		notesPanel.setSpacing(0);
		iStandardNotesButton = new Button(MESSAGES.buttonStandardNotes(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iStandardNotes.getItemCount() > 0) {
					iStandardNotesBox.center();
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iStandardNotes.setFocus(true);							
						}
					});
				}
			}
		});
		Character standardNotesButtonAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonStandardNotes());
		if (standardNotesButtonAccessKey != null) iStandardNotesButton.setAccessKey(standardNotesButtonAccessKey);
		iStandardNotesButton.setVisible(false);
		iStandardNotesButton.getElement().getStyle().setMarginTop(2, Unit.PX);
		notesPanel.add(iStandardNotesButton);
		notesPanel.setCellHorizontalAlignment(iStandardNotesButton, HasHorizontalAlignment.ALIGN_RIGHT);
		 
		int row = iForm.addRow(MESSAGES.propAdditionalInformation(), notesPanel);
		Roles.getTextboxRole().setAriaLabelledbyProperty(iNotes.getElement(), Id.of(iForm.getWidget(row, 0).getElement()));
		
		iFileUpload = new UniTimeFileUpload();
		iForm.addRow(MESSAGES.propAttachment(), iFileUpload);
		
		iExpirationDate = new SingleDateSelector();
		iExpirationDate.setFirstDate(iExpirationDate.today());
		iForm.addRow(MESSAGES.propExpirationDate(), iExpirationDate);
		
		iCoursesForm = new SimpleForm();
		iCoursesForm.addHeaderRow(MESSAGES.sectRelatedCourses());
		iCoursesForm.removeStyleName("unitime-NotPrintableBottomLine");
		iCoursesForm.addRow(iCourses);
		iCoursesForm.addRow(iReqAttendance);
		iForm.addRow(iCoursesForm);
		
		iEventType.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				EventType type = getEventType();
				iName.setReadOnly(type == EventType.Class || type == EventType.MidtermExam || type == EventType.FinalExam);
				iEvent.setType(type);
				iCoursesForm.setVisible(type == EventType.Course);
				iForm.getRowFormatter().setVisible(iForm.getRow(MESSAGES.propAttendance()), type == EventType.Special);
				iForm.getRowFormatter().setVisible(iForm.getRow(MESSAGES.propSponsor()), type != EventType.Unavailabile && type != EventType.Class && type != EventType.MidtermExam && type != EventType.FinalExam && iSponsors.getItemCount() > 0);
				iForm.getRowFormatter().setVisible(iForm.getRow(MESSAGES.propExpirationDate()), getProperties() != null && (getProperties().isCanSetExpirationDate() || iExpirationDate.getValue() != null) && type != EventType.Unavailabile && type != EventType.Class && type != EventType.MidtermExam && type != EventType.FinalExam);
				if (iMeetings.getRowCount() > 1) {
					LoadingWidget.getInstance().show(MESSAGES.waitCheckingRoomAvailability());
					RPC.execute(EventRoomAvailabilityRpcRequest.checkAvailability(iMeetings.getMeetings(), getEventId(), getEventType(), iSession.getAcademicSessionId()), new AsyncCallback<EventRoomAvailabilityRpcResponse>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(MESSAGES.failedRoomAvailability(caught.getMessage()), caught);
						}

						@Override
						public void onSuccess(EventRoomAvailabilityRpcResponse result) {
							LoadingWidget.getInstance().hide();
							iMeetings.setMeetings(iEvent, result.getMeetings());
							checkEnrollments(iCourses.getValue(), iMeetings.getMeetings());
							showCreateButtonIfApplicable();
						}
					});
				}
			}
		});
		
		iEventAddMeetings = new AddMeetingsDialog(session, iProperties, new AsyncCallback<List<MeetingInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedAddMeetings(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(List<MeetingInterface> result) {
				LoadingWidget.getInstance().show(MESSAGES.waitCheckingRoomAvailability());
				RPC.execute(EventRoomAvailabilityRpcRequest.checkAvailability(result, getEventId(), getEventType(), iSession.getAcademicSessionId()), new AsyncCallback<EventRoomAvailabilityRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.error(MESSAGES.failedRoomAvailability(caught.getMessage()), caught);
					}

					@Override
					public void onSuccess(EventRoomAvailabilityRpcResponse result) {
						LoadingWidget.getInstance().hide();
						addMeetings(result.getMeetings());
						iEventAddMeetings.reset(iProperties == null ? null : iProperties.getRoomFilter(), iProperties == null ? null : iProperties.getSelectedDates(), iProperties == null ? null : iProperties.getSelectedTime());
					}
				});
			}
		});
		iEventModifyMeetings = new AddMeetingsDialog(session, iProperties, new AsyncCallback<List<MeetingInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedChangeMeetings(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(List<MeetingInterface> result) {
				final List<MeetingInterface> meetings = iMeetings.getMeetings();
				if (!iEventType.isReadOnly())
					iEvent.setType(getEventType());
				RPC.execute(EventRoomAvailabilityRpcRequest.checkAvailability(result, getEventId(), getEventType(), iSession.getAcademicSessionId()), new AsyncCallback<EventRoomAvailabilityRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.error(MESSAGES.failedRoomAvailability(caught.getMessage()), caught);
					}

					@Override
					public void onSuccess(EventRoomAvailabilityRpcResponse result) {
						LoadingWidget.getInstance().hide();
						List<MeetingInterface> added = new ArrayList<EventInterface.MeetingInterface>(result.getMeetings());
						current: for (Iterator<MeetingInterface> i = meetings.iterator(); i.hasNext(); ) {
							MeetingInterface meeting = i.next();
							if (meeting.getApprovalStatus() != ApprovalStatus.Pending && meeting.getApprovalStatus() != ApprovalStatus.Approved) continue;
							if (!iSelection.contains(meeting)) continue;
							for (Iterator<MeetingInterface> j = added.iterator(); j.hasNext();) {
								MeetingInterface m = j.next();
								if (m.getDayOfYear() == meeting.getDayOfYear() && EventInterface.equals(meeting.getLocation(), m.getLocation()) && meeting.getStartSlot() == m.getStartSlot() && meeting.getEndSlot() == m.getEndSlot()) {
									j.remove();
									continue current;
								}
							}
							if (meeting.getId() == null) {
								i.remove();
							} else if (meeting.getApprovalStatus() == ApprovalStatus.Cancelled || meeting.getApprovalStatus() == ApprovalStatus.Deleted) {
								// already cancelled or deleted
							} else if (meeting.isCanDelete()) {
								meeting.setApprovalStatus(ApprovalStatus.Deleted);
								meeting.setCanApprove(false); meeting.setCanCancel(false); meeting.setCanInquire(false); meeting.setCanEdit(false); meeting.setCanDelete(false);
							} else if (meeting.isCanCancel()) {
								meeting.setApprovalStatus(ApprovalStatus.Cancelled);
								meeting.setCanApprove(false); meeting.setCanCancel(false); meeting.setCanInquire(false); meeting.setCanEdit(false); meeting.setCanDelete(false);
							}
						}
						added: for (MeetingInterface meeting: added) {
							if (meeting.getApprovalStatus() != ApprovalStatus.Pending && meeting.getApprovalStatus() != ApprovalStatus.Approved) continue; 
							for (MeetingInterface existing: meetings) {
								if (existing.getApprovalStatus() != ApprovalStatus.Pending && existing.getApprovalStatus() != ApprovalStatus.Approved) continue;
								if (existing.inConflict(meeting)) {
									UniTimeNotifications.warn(MESSAGES.warnNewMeetingOverlaps(meeting.toString(), existing.toString()));
									continue added;
								}
							}
							meetings.add(meeting);
						}
						Collections.sort(meetings);
						
						boolean hasSelection = false;
						for (int row = 1; row < iMeetings.getRowCount(); row++) {
							Widget w =  iMeetings.getWidget(row, 0);
							if (w != null && w instanceof CheckBox) {
								CheckBox ch = (CheckBox)w;
								if (ch.getValue()) { hasSelection = true; break; }
							}
						}

						iMeetings.setMeetings(iEvent, meetings);
						showCreateButtonIfApplicable();
						ValueChangeEvent.fire(iMeetings, iMeetings.getValue());
						
						if (hasSelection)
							rows: for (int row = 1; row < iMeetings.getRowCount(); row++) {
								Widget w =  iMeetings.getWidget(row, 0);
								if (w != null && w instanceof CheckBox) {
									CheckBox ch = (CheckBox)w;
									MeetingInterface meeting = iMeetings.getData(row).getMeeting();
									for (MeetingInterface m: result.getMeetings()) {
										if (m.getDayOfYear() == meeting.getDayOfYear() && EventInterface.equals(meeting.getLocation(), m.getLocation()) && meeting.getStartSlot() == m.getStartSlot() && meeting.getEndSlot() == m.getEndSlot()) {
											ch.setValue(true);
											continue rows;
										}
									}
								}
							}
					}
				});
			}
		});
		iEventModifyMeetings.setText(MESSAGES.dialogModifyMeetings());
		
		iMeetingsHeader = new UniTimeHeaderPanel(MESSAGES.sectMeetings());
		iMeetingsHeader.addButton("add", MESSAGES.buttonAddMeetings(), 100, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iEventAddMeetings.showDialog(getEventId(), getConflicts());
			}
		});
		iMeetingsHeader.addButton("operations", MESSAGES.buttonMoreOperations(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				iMeetings.getHeader(0).setMenu(popup);
				popup.showRelativeTo((UIObject)event.getSource());
				((MenuBar)popup.getWidget()).focus();
			}
		});
		iForm.addHeaderRow(iMeetingsHeader);
		
		iMeetings = new EventMeetingTable(EventMeetingTable.Mode.MeetingsOfAnEvent, true, iProperties); iMeetings.setEditable(true);
		iMeetings.setOperation(EventMeetingTable.OperationType.AddMeetings, this);
		iMeetings.setOperation(EventMeetingTable.OperationType.Delete, this);
		iMeetings.setOperation(EventMeetingTable.OperationType.Cancel, this);
		iMeetings.setOperation(EventMeetingTable.OperationType.Modify, this);
		iMeetings.addValueChangeHandler(new ValueChangeHandler<List<EventMeetingRow>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<EventMeetingRow>> event) {
				checkEnrollments(iCourses.getValue(), iMeetings.getMeetings());
				showCreateButtonIfApplicable();
			}
		});

		
		iForm.addRow(iMeetings);
		
		iShowDeleted = new CheckBox("<i>" + MESSAGES.showDeletedMeetings() + "</i>", true);
		iForm.addRow(iShowDeleted);
		iForm.getCellFormatter().setHorizontalAlignment(iForm.getRowCount() - 1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		iShowDeleted.setValue(EventCookie.getInstance().isShowDeletedMeetings());
		iShowDeleted.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iMeetings.setMeetings(iEvent, iMeetings.getMeetings());
				EventCookie.getInstance().setShowDeletedMeetings(event.getValue());
				if (event.getValue())
					iMeetings.removeStyleName("unitime-EventMeetingsHideDeleted");
				else
					iMeetings.addStyleName("unitime-EventMeetingsHideDeleted");
			}
		});
		if (!iShowDeleted.getValue())
			iMeetings.addStyleName("unitime-EventMeetingsHideDeleted");
		
		iEnrollments = new EnrollmentTable(false, true);
		iEnrollments.getTable().setStyleName("unitime-Enrollments");
		iEnrollmentHeader = new UniTimeHeaderPanel(MESSAGES.sectEnrollments());
		iEnrollmentRow = iForm.addHeaderRow(iEnrollmentHeader);
		iForm.addRow(iEnrollments.getTable());
		iForm.getRowFormatter().setVisible(iEnrollmentRow, false);
		iForm.getRowFormatter().setVisible(iEnrollmentRow + 1, false);
		
		iFooter = iHeader.clonePanel("");
		
		iEmailConfirmationHeader = new CheckBox(MESSAGES.checkSendEmailConfirmation(), true);
		iEmailConfirmationHeader.addStyleName("toggle");
		iHeader.insertRight(iEmailConfirmationHeader, true);
		
		iEmailConfirmationFooter = new CheckBox(MESSAGES.checkSendEmailConfirmation(), true);
		iEmailConfirmationFooter.addStyleName("toggle");
		iFooter.insertRight(iEmailConfirmationFooter, true);
		
		iEmailConfirmationHeader.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iEmailConfirmationFooter.setValue(event.getValue(), false);
			}
		});
		iEmailConfirmationFooter.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iEmailConfirmationHeader.setValue(event.getValue(), false);
			}
		});
		
		iForm.addNotPrintableBottomRow(iFooter);
		
		iMeetings.addMouseClickListener(new UniTimeTable.MouseClickListener<EventMeetingRow>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<EventMeetingRow> event) {
				EventMeetingRow row = event.getData();
				if (row == null) return;
				if (row.getParent() != null) row = row.getParent();
				MeetingInterface meeting = row.getMeeting();
				if (meeting == null) return;
				if (iMeetings.isSelectable(row) && (meeting.getId() == null || meeting.isCanCancel() || meeting.isCanDelete())) {
					List<EventMeetingRow> selection = new ArrayList<EventMeetingRow>();
					selection.add(row);
					execute(iMeetings, OperationType.Modify, selection);
				} else if (!row.getMeeting().isPast() && (row.getMeeting().getApprovalStatus() == ApprovalStatus.Cancelled || row.getMeeting().getApprovalStatus() == ApprovalStatus.Deleted)) {
					List<EventMeetingRow> selection = new ArrayList<EventMeetingRow>();
					selection.add(row);
					execute(iMeetings, OperationType.Modify, selection);
				}
			}
		});
		
		initWidget(iForm);
	}
	
	public String getMessage() {
		return iNotes.getText();
	}
	
	public boolean isSendEmailConformation() {
		return !iEmailConfirmationHeader.isVisible() || iEmailConfirmationHeader.getValue();
	}
	
	public boolean isEventAdd() { return iEvent == null || iEvent.getId() == null; }
	public Long getEventId() { return iEvent == null ? null : iEvent.getId(); }
	
	public EventInterface.EventType getEventType() {
		return (iEventType.isReadOnly() ? iEvent.getType() : iEventType.getWidget().getSelectedIndex() < 0 ? null : EventType.valueOf(iEventType.getWidget().getValue(iEventType.getWidget().getSelectedIndex())));
	}
	
	public boolean hasMainContactChanged() {
		if (iOriginalContact == null) return false;
		boolean changed = false;
		if (!iMainFName.getText().equals(iOriginalContact.hasFirstName() ? iOriginalContact.getFirstName() : "")) {
			iMainFName.addStyleName("unitime-TextChanged");
			changed = true;
		} else {
			iMainFName.removeStyleName("unitime-TextChanged");
		}
		if (!iMainMName.getText().equals(iOriginalContact.hasMiddleName() ? iOriginalContact.getMiddleName() : "")) {
			iMainMName.addStyleName("unitime-TextChanged");
			changed = true;
		} else {
			iMainMName.removeStyleName("unitime-TextChanged");
		}
		if (!iMainLName.getWidget().getText().equals(iOriginalContact.hasLastName() ? iOriginalContact.getLastName() : "")) {
			iMainLName.getWidget().addStyleName("unitime-TextChanged");
			changed = true;
		} else {
			iMainLName.getWidget().removeStyleName("unitime-TextChanged");
		}
		if (!iMainTitle.getText().equals(iOriginalContact.hasAcademicTitle() ? iOriginalContact.getAcademicTitle() : "")) {
			iMainTitle.addStyleName("unitime-TextChanged");
			changed = true;
		} else {
			iMainTitle.removeStyleName("unitime-TextChanged");
		}
		if (!iMainPhone.getText().equals(iOriginalContact.hasPhone() ? iOriginalContact.getPhone() : "")) {
			iMainPhone.addStyleName("unitime-TextChanged");
			changed = true;
		} else {
			iMainPhone.removeStyleName("unitime-TextChanged");
		}
		if (!iMainEmail.getWidget().getText().equals(iOriginalContact.hasEmail() ? iOriginalContact.getEmail() : "")) {
			iMainEmail.getWidget().addStyleName("unitime-TextChanged");
			changed = true;
		} else {
			iMainEmail.getWidget().removeStyleName("unitime-TextChanged");
		}
		return changed;
	}
	
	public void checkMainContactChanged() {
		if (hasMainContactChanged()) {
			if (iOriginalContact != null && iOriginalContact.getExternalId() != null && iProperties != null && iProperties.getMainContact() != null &&
				!iOriginalContact.getExternalId().equals(iProperties.getMainContact().getExternalId())) {
				iMainContactChanged.setText(iMainContactChanged.getValue()
						? MESSAGES.confirmMainContactChange(iOriginalContact.getName(MESSAGES))
						: MESSAGES.checkMainContactChange(iOriginalContact.getName(MESSAGES)));
			} else {
				iMainContactChanged.setText(iMainContactChanged.getValue()
						? MESSAGES.confirmYourContactChange()
						: MESSAGES.checkYourContactChange());	
			}
			iForm.getRowFormatter().setVisible(iMainContactChangedRow, true);
			iMainContactResetButton.setVisible(true);
			if (iMainContactChanged.getValue()) {
				iForm.getCellFormatter().setStyleName(iMainContactChangedRow, 1, "unitime-CheckConfirmed");
			} else {
				iForm.getCellFormatter().setStyleName(iMainContactChangedRow, 1, "unitime-CheckNotConfirmed");
			}
		} else {
			iForm.getRowFormatter().setVisible(iMainContactChangedRow, false);
			iMainContactResetButton.setVisible(iOriginalContact != null && iLookupButton.isVisible());
		}
	}
	
	public EventInterface getEvent() {
		iEvent.setName(iName.getWidget().getText());
		if (!iEventType.isReadOnly())
			iEvent.setType(getEventType());
		iEvent.setMaxCapacity(iLimit.toInteger());
		if (iEvent.getContact() == null) { iEvent.setContact(new ContactInterface()); }
		iEvent.getContact().setExternalId(iMainExternalId);
		iEvent.getContact().setFirstName(iMainFName.getText());
		iEvent.getContact().setMiddleName(iMainMName.getText());
		iEvent.getContact().setLastName(iMainLName.getWidget().getText());
		iEvent.getContact().setAcademicTitle(iMainTitle.getText());
		iEvent.getContact().setEmail(iMainEmail.getWidget().getText());
		iEvent.getContact().setPhone(iMainPhone.getText());
		
		if (getEventType() == EventType.Unavailabile && (!iEvent.getContact().hasLastName() || !iEvent.getContact().hasEmail())) {
			ContactInterface mainContact = getProperties().getMainContact();
			if (mainContact != null) {
				iEvent.getContact().setExternalId(mainContact.getExternalId());
				iEvent.getContact().setFirstName(mainContact.getFirstName());
				iEvent.getContact().setMiddleName(mainContact.getMiddleName());
				iEvent.getContact().setLastName(mainContact.getLastName());
				iEvent.getContact().setAcademicTitle(mainContact.getAcademicTitle());
				iEvent.getContact().setEmail(mainContact.getEmail());
				iEvent.getContact().setPhone(mainContact.getPhone());
			}
		}

		if (iEvent.hasAdditionalContacts()) iEvent.getAdditionalContacts().clear();
		for (ContactInterface contact: iContacts.getData())
			iEvent.addAdditionalContact(contact);
		
		iEvent.setEmail(iEmails.getText());
		iEvent.setExpirationDate(iExpirationDate.getValueInServerTimeZone());

		/*
		if (iEvent.hasNotes() && iEvent.getNotes().last().getDate() == null)
			iEvent.getNotes().remove(iEvent.getNotes().last());
		if (!iNotes.getText().isEmpty()) {
			NoteInterface note = new NoteInterface();
			note.setNote(iNotes.getText());
			note.setType(iEvent.getId() == null ? NoteType.Create : NoteType.AddMeetings);
			iEvent.addNote(note);
		}
		*/
		
		if (iEvent.hasMeetings())
			iEvent.getMeetings().clear();
		for (MeetingInterface meeting: iMeetings.getMeetings())
			iEvent.addMeeting(meeting);
		
		if (iSponsors.getSelectedIndex() > 0) {
			Long sponsorId = Long.valueOf(iSponsors.getValue(iSponsors.getSelectedIndex()));
			SponsoringOrganizationInterface sponsor = null;
			List<SponsoringOrganizationInterface> sponsors = (getProperties() == null ? null : getProperties().getSponsoringOrganizations());
			for (SponsoringOrganizationInterface s: sponsors)
				if (s.getUniqueId().equals(sponsorId)) { sponsor = s; break; }
			iEvent.setSponsor(sponsor);
		} else {
			iEvent.setSponsor(null);
		}
		
		if (iEvent.getType() == EventType.Course) {
			if (iEvent.hasRelatedObjects())
				iEvent.getRelatedObjects().clear();
			for (RelatedObjectInterface related: iCourses.getValue())
				iEvent.addRelatedObject(related);
			iEvent.setRequiredAttendance(iReqAttendance.getValue());
		} else if (iEvent.getType() == EventType.Special || iEvent.getType() == EventType.Unavailabile) {
			if (iEvent.hasRelatedObjects())
				iEvent.getRelatedObjects().clear();
			iEvent.setRequiredAttendance(false);
		}
		
		if (iEvent.getType() == EventType.Unavailabile) {
			iEvent.setSponsor(null);
			iEvent.setMaxCapacity(null);
		}
		
		return iEvent;
	}
	
	public EventInterface getSavedEvent() {
		return iSavedEvent;
	}
	
	protected void addMeetings(List<MeetingInterface> meetings) {
		List<MeetingInterface> existingMeetings = iMeetings.getMeetings();
		if (!iEventType.isReadOnly())
			iEvent.setType(getEventType());
		if (meetings != null && !meetings.isEmpty())
			meetings: for (MeetingInterface meeting: meetings) {
				if (meeting.getApprovalStatus() != ApprovalStatus.Pending && meeting.getApprovalStatus() != ApprovalStatus.Approved) continue; 
				for (MeetingInterface existing: existingMeetings) {
					if (existing.getApprovalStatus() != ApprovalStatus.Pending && existing.getApprovalStatus() != ApprovalStatus.Approved) continue;
					if (existing.inConflict(meeting)) {
						UniTimeNotifications.warn(MESSAGES.warnNewMeetingOverlaps(meeting.toString(), existing.toString()));
						continue meetings;
					}
				}
				iMeetings.add(new EventMeetingRow(iEvent, meeting));
			}
		ValueChangeEvent.fire(iMeetings, iMeetings.getValue());
	}
	
	private int iLastScrollTop, iLastScrollLeft;
	public void show() {
		UniTimePageLabel.getInstance().setPageName(iEvent.getId() == null ? MESSAGES.pageAddEvent() : MESSAGES.pageEditEvent());
		setVisible(true);
		iLastScrollLeft = Window.getScrollLeft();
		iLastScrollTop = Window.getScrollTop();
		onShow();
		Window.scrollTo(0, 0);
		if (iForm.getRowFormatter().isVisible(iSessionRow)) {
			iSession.setFilter(this);
			iForm.setWidget(iSessionRow, 1, iSession);
		}
		iFileUpload.check();
	}
	
	public void hide() {
		setVisible(false);
		onHide();
		Window.scrollTo(iLastScrollLeft, iLastScrollTop);
	}
	
	protected void onHide() {
	}
	
	protected void onShow() {
	}
	
	public void setup(EventPropertiesRpcResponse properties) {
		EventInterface event = (isAttached() && isVisible() && iEvent != null && iEvent.getId() == null ? getEvent() : null);
		String notes = getMessage();
		iSponsors.clear();
		if (properties.hasSponsoringOrganizations()) {
			iSponsors.addItem(MESSAGES.itemSelect(), "");
			for (SponsoringOrganizationInterface sponsor: properties.getSponsoringOrganizations())
				iSponsors.addItem(sponsor.getName(), sponsor.getUniqueId().toString());
		}
		if (event != null) {
			// Fix event type
			if (event.getType() == EventType.Unavailabile && !properties.isCanAddUnavailableEvent())
				event.setType(EventType.Special);
			if (event.getType() == EventType.Course && !properties.isCanAddCourseEvent())
				event.setType(EventType.Special);
			if (event.getType() == EventType.Special && !properties.isCanAddSpecialEvent() && properties.isCanAddCourseEvent())
				event.setType(EventType.Course);
			// Remove all meetings and related objects
			if (event.hasMeetings()) event.getMeetings().clear();
			if (event.hasRelatedObjects()) event.getRelatedObjects().clear();
			if (!properties.isCanLookupAdditionalContacts()) { // Can not lookup additional contacts
				// Clear additional contacts
				if (event.hasAdditionalContacts()) event.getAdditionalContacts().clear();
			}
			if (!properties.isCanLookupMainContact()) { // Can not lookup main contact
				// Clear main contact if different from the user
				if (event.hasContact() && event.getContact().hasExternalId()) {
					if (!event.getContact().getExternalId().equals(properties.hasMainContact() ? properties.getMainContact().getExternalId() : null))
						event.setContact(null);
				} else if (event.hasContact() && !event.getContact().hasExternalId()) {
					event.setContact(null);
				}
			}
			// Clear expiration date, if cannot set
			if (!properties.isCanSetExpirationDate())
				event.setExpirationDate(null);
			
			iEnrollments.clear();
			iForm.getRowFormatter().setVisible(iEnrollmentRow, false);
			iForm.getRowFormatter().setVisible(iEnrollmentRow + 1, false);
			
			// Reset the event, but keep the original contact
			ContactInterface original = iOriginalContact;
			setEvent(event, false);
			iOriginalContact = original;
			if (iOriginalContact != null && !iOriginalContact.hasExternalId() && iLookupButton.isVisible())
				iOriginalContact = null;
			
			if (notes != null && !notes.isEmpty())
				iNotes.setText(notes);
			
			// Re-check the change in the main contact
			checkMainContactChanged();
		}
		EventType type = getEventType();
		iForm.getRowFormatter().setVisible(iForm.getRow(MESSAGES.propSponsor()), type != EventType.Unavailabile && type != EventType.Class && type != EventType.MidtermExam && type != EventType.FinalExam && iSponsors.getItemCount() > 0);
		iForm.getRowFormatter().setVisible(iForm.getRow(MESSAGES.propExpirationDate()), (properties.isCanSetExpirationDate() || iExpirationDate.getValue() != null) && type != EventType.Unavailabile && type != EventType.Class && type != EventType.MidtermExam && type != EventType.FinalExam);
	}
	
	public void setEvent(EventInterface event) {
		setEvent(event, true);
	}
	
	public void setEvent(EventInterface event, boolean resetUpload) {
		iMainContact.getRowFormatter().setVisible(iAcademicTitleRow, getProperties() != null && getProperties().isCanEditAcademicTitle());
		iEmailConfirmationHeader.setValue(getProperties() == null ||  getProperties().isEmailConfirmation());
		iEmailConfirmationHeader.setVisible(getProperties() == null || getProperties().hasEmailConfirmation());
		iEmailConfirmationFooter.setValue(getProperties() == null ||  getProperties().isEmailConfirmation());
		iEmailConfirmationFooter.setVisible(getProperties() == null || getProperties().hasEmailConfirmation());
		if (resetUpload) iFileUpload.reset();
		iShowDeleted.setValue(EventCookie.getInstance().isShowDeletedMeetings(), true);
		iForm.getRowFormatter().setVisible(iSessionRow, event == null || event.getId() == null);
		iEvent = (event == null ? new EventInterface() : event);
		if (event == null && getProperties() != null && getProperties().hasFilterDefault("emails"))
			iEvent.setEmail(getProperties().getFilterDefault("emails"));
		iSavedEvent = null;
		iHeader.clearMessage();
		iName.getWidget().setText(iEvent.getName() == null ? "" : iEvent.getName());
		iName.clearHint();
		iName.setText(iEvent.getName() == null ? "" : iEvent.getName());
		if (iEvent.hasSponsor()) {
			for (int i = 1; i < iSponsors.getItemCount(); i++) {
				if (iSponsors.getValue(i).equals(iEvent.getSponsor().getUniqueId().toString())) {
					iSponsors.setSelectedIndex(i); break;
				}
			}
		} else {
			iSponsors.setSelectedIndex(0);
		}
		
		boolean canAddSpecialEvent = (getProperties() == null ? false : getProperties().isCanAddSpecialEvent());
		boolean canAddCourseEvent = (getProperties() == null ? false : getProperties().isCanAddCourseEvent());
		boolean canAddUnavailableEvent = (getProperties() == null ? false : getProperties().isCanAddUnavailableEvent());
		iEventType.getWidget().clear();
		if (canAddSpecialEvent)
			iEventType.getWidget().addItem(EventInterface.EventType.Special.getName(CONSTANTS), EventInterface.EventType.Special.name());
		if (canAddCourseEvent)
			iEventType.getWidget().addItem(EventInterface.EventType.Course.getName(CONSTANTS), EventInterface.EventType.Course.name());
		if (canAddUnavailableEvent)
			iEventType.getWidget().addItem(EventInterface.EventType.Unavailabile.getName(CONSTANTS), EventInterface.EventType.Unavailabile.name());
		
		if (iEvent.getType() == null) {
			iEventType.getWidget().setSelectedIndex(0);
			iEventType.setReadOnly(false);
		} else if (iEvent.getId() == null) {
			iEventType.getWidget().setSelectedIndex(0);
			for (int i = 1; i < iEventType.getWidget().getItemCount(); i++) {
				if (iEventType.getWidget().getValue(i).equals(iEvent.getType().name())) {
					iEventType.getWidget().setSelectedIndex(i); break;
				}
			}
			iEventType.setReadOnly(false);
		} else {
			iEventType.setText(iEvent.getType().getName(CONSTANTS));
			iEventType.setReadOnly(true);
		}

		iLimit.setValue(iEvent.hasMaxCapacity() ? iEvent.getMaxCapacity() : null);
		iNotes.setText("");
		iEmails.setText(iEvent.hasEmail() ? iEvent.getEmail() : "");
		iExpirationDate.setValueInServerTimeZone(iEvent.getExpirationDate());
		if (iEvent.getType() == EventType.Course) {
			iCourses.setValue(iEvent.getRelatedObjects());
			iReqAttendance.setValue(iEvent.hasRequiredAttendance());
		} else {
			if (canAddCourseEvent) iCourses.setValue(null);
			iReqAttendance.setValue(false);
		}
		
		if (iEvent.hasMeetings()) {
			iMeetings.setMeetings(iEvent, iEvent.getMeetings());
		} else {
			iMeetings.setValue(null);
			List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
			List<SelectionInterface> selection = (iProperties == null ? null : iProperties.getSelection());
			if (selection != null && !selection.isEmpty()) {
				for (SelectionInterface s: selection) {
					for (Integer day: s.getDays()) {
						for (ResourceInterface room: s.getLocations()) {
							MeetingInterface meeting = new MeetingInterface();
							meeting.setStartSlot(s.getStartSlot());
							meeting.setEndSlot(s.getStartSlot() + s.getLength());
							meeting.setStartOffset(0);
							meeting.setEndOffset(-room.getBreakTime());
							meeting.setDayOfYear(day);
							meeting.setLocation(room);
							meeting.setCanDelete(true);
							meetings.add(meeting);
						}
					}
				}
			}
			if (!meetings.isEmpty()) {
				LoadingWidget.getInstance().show(MESSAGES.waitCheckingRoomAvailability());
				RPC.execute(EventRoomAvailabilityRpcRequest.checkAvailability(meetings, getEventId(), getEventType(), iSession.getAcademicSessionId()), new AsyncCallback<EventRoomAvailabilityRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.error(MESSAGES.failedRoomAvailability(caught.getMessage()), caught);
					}

					@Override
					public void onSuccess(EventRoomAvailabilityRpcResponse result) {
						LoadingWidget.getInstance().hide();
						iMeetings.setMeetings(iEvent, result.getMeetings());
						showCreateButtonIfApplicable();
					}
				});
			}
		}
		
		if (iEvent.hasContact()) {
			iMainExternalId = iEvent.getContact().getExternalId();
			iMainFName.setText(iEvent.getContact().hasFirstName() ? iEvent.getContact().getFirstName() : "");
			iMainMName.setText(iEvent.getContact().hasMiddleName() ? iEvent.getContact().getMiddleName() : "");
			iMainLName.getWidget().setText(iEvent.getContact().hasLastName() ? iEvent.getContact().getLastName() : "");
			iMainTitle.setText(iEvent.getContact().hasAcademicTitle() ? iEvent.getContact().getAcademicTitle() : "");
			iMainPhone.setText(iEvent.getContact().hasPhone() ? iEvent.getContact().getPhone() : "");
			iMainEmail.getWidget().setText(iEvent.getContact().hasEmail() ? iEvent.getContact().getEmail() : "");
			iOriginalContact = iEvent.getContact();
		} else {
			ContactInterface mainContact = (getProperties() == null || getProperties().isCanLookupMainContact() ? null : getProperties().getMainContact());
			if (mainContact != null) {
				iMainExternalId = mainContact.getExternalId();
				iMainFName.setText(mainContact.getFirstName() == null ? "" : mainContact.getFirstName());
				iMainMName.setText(mainContact.getMiddleName() == null ? "" : mainContact.getMiddleName());
				iMainLName.getWidget().setText(mainContact.getLastName() == null ? "" : mainContact.getLastName());
				iMainTitle.setText(mainContact.getAcademicTitle() == null ? "" : mainContact.getAcademicTitle());
				iMainPhone.setText(mainContact.getPhone() == null ? "" : mainContact.getPhone());
				iMainEmail.getWidget().setText(mainContact.getEmail() == null ? "" : mainContact.getEmail());
				iOriginalContact = getProperties().getMainContact();
			} else {
				iMainExternalId = null;
				iMainFName.setText("");
				iMainMName.setText("");
				iMainLName.getWidget().setText("");
				iMainTitle.setText("");
				iMainPhone.setText("");
				iMainEmail.getWidget().setText("");
				iOriginalContact = null;
			}
		}
		iMainContactChanged.setValue(false, true);
		
		iContacts.clearTable(1);
		if (iEvent.hasAdditionalContacts()) {
			for (final ContactInterface contact: iEvent.getAdditionalContacts()) {
				List<Widget> row = new ArrayList<Widget>();
				row.add(new Label(contact.getName(MESSAGES), false));
				row.add(new Label(contact.hasEmail() ? contact.getEmail() : "", false));
				row.add(new Label(contact.hasPhone() ? contact.getPhone() : "", false));
				Image remove = new Image(RESOURCES.delete());
				remove.addStyleName("remove");
				remove.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						for (int row = 1; row < iContacts.getRowCount(); row ++)
							if (contact.equals(iContacts.getData(row))) {
								iContacts.removeRow(row);
								break;
							}
						iForm.getRowFormatter().setVisible(iContactRow, iContacts.getRowCount() > 1);
					}
				});
				row.add(remove);
				int rowNum = iContacts.addRow(contact, row);
				for (int col = 0; col < iContacts.getCellCount(rowNum); col++)
					iContacts.getCellFormatter().addStyleName(rowNum, col, "additional-contact");
			}
		}
		if (iEvent.hasInstructors()) {
			for (final ContactInterface contact: iEvent.getInstructors()) {
				List<Widget> row = new ArrayList<Widget>();
				row.add(new Label(contact.getName(MESSAGES), false));
				row.add(new Label(contact.hasEmail() ? contact.getEmail() : "", false));
				row.add(new Label(contact.hasPhone() ? contact.getPhone() : "", false));
				int rowNum = iContacts.addRow(null, row);
				for (int col = 0; col < iContacts.getCellCount(rowNum); col++)
					iContacts.getCellFormatter().addStyleName(rowNum, col, "instructor-contact");
			}
		}
		iForm.getRowFormatter().setVisible(iContactRow, iContacts.getRowCount() > 1);
		
		iLookupButton.setVisible(getProperties() == null ? false : getProperties().isCanLookupMainContact());
		iAdditionalLookupButton.setVisible(getProperties() == null ? false : getProperties().isCanLookupAdditionalContacts());
		
		boolean canSeeStandardNotes = (getProperties() == null ? false : getProperties().isCanLookupMainContact() && getProperties().hasStandardNotes()); 
		iStandardNotesButton.setVisible(canSeeStandardNotes);
		iStandardNotes.clear();
		if (canSeeStandardNotes)
			for (StandardEventNoteInterface note: getProperties().getStandardNotes())
				iStandardNotes.addItem(note.toString(), note.getNote());
		
		iEventAddMeetings.reset(iProperties == null ? null : iProperties.getRoomFilter(), iProperties == null ? null : iProperties.getSelectedDates(), iProperties == null ? null : iProperties.getSelectedTime());
		
		DomEvent.fireNativeEvent(Document.get().createChangeEvent(), iEventType.getWidget());
		
		boolean canDelete = (iEvent.getId() != null);
		if (canDelete && iEvent.hasMeetings()) {
			for (MeetingInterface meeting: iEvent.getMeetings()) {
				if (!meeting.isCanDelete()) { canDelete = false; break; }
			}
		}
		boolean canCancel = (iEvent.getId() != null);
		if (canCancel && iEvent.hasMeetings()) {
			boolean hasAMeetingToCancel = false;
			for (MeetingInterface meeting: iEvent.getMeetings()) {
				if (!meeting.isCanDelete() && !meeting.isCanCancel() && (meeting.getApprovalStatus() == ApprovalStatus.Approved || meeting.getApprovalStatus() == ApprovalStatus.Pending)) {
					canCancel = false; break;
				}
				if (meeting.isCanCancel() || meeting.isCanDelete())
					hasAMeetingToCancel = true;
			}
			if (!hasAMeetingToCancel) canCancel = false;
		}
		iHeader.setEnabled("delete", canDelete);
		iHeader.setEnabled("cancel", canCancel);
		showCreateButtonIfApplicable();
		if (iEvent.getId() == null && (getProperties() == null || !getProperties().isCanAddEvent()))
			UniTimeNotifications.warn(MESSAGES.warnCannotAddEvent(iSession.getAcademicSessionName()));
		iHeader.setEnabled("update", iEvent.getId() != null);
		checkMainContactChanged();
	}
	
	public static class CourseRelatedObjectLine {
		List<RelatedObjectLookupRpcResponse> iSubjects, iCourses, iSubparts, iClasses;
		
		public List<RelatedObjectLookupRpcResponse> getSubjects() { return iSubjects; }
		public void setSubjects(List<RelatedObjectLookupRpcResponse> subjects) { iSubjects = subjects; }
		public RelatedObjectLookupRpcResponse getSubject(String value) {
			if (value == null || value.isEmpty() || iSubjects == null) return null;
			for (RelatedObjectLookupRpcResponse r: iSubjects)
				if (r.getUniqueId() != null && r.getUniqueId().toString().equals(value)) return r;
			return null;
		}
		
		public List<RelatedObjectLookupRpcResponse> getCourses() { return iCourses; }
		public void setCourses(List<RelatedObjectLookupRpcResponse> courses) { iCourses = courses; }
		public RelatedObjectLookupRpcResponse getCourse(String value) {
			if (value == null || value.isEmpty() || iCourses == null) return null;
			for (RelatedObjectLookupRpcResponse r: iCourses)
				if (r.getUniqueId() != null && r.getUniqueId().toString().equals(value)) return r;
			return null;
		}

		public List<RelatedObjectLookupRpcResponse> getSubparts() { return iSubparts; }
		public void setSubparts(List<RelatedObjectLookupRpcResponse> subparts) { iSubparts = subparts; }
		public RelatedObjectLookupRpcResponse getSubpart(String value) {
			if (value == null || value.isEmpty() || iSubparts == null) return null;
			for (RelatedObjectLookupRpcResponse r: iSubparts)
				if (r.getUniqueId() != null && (r.getLevel() + ":" + r.getUniqueId()).equals(value)) return r;
			return null;
		}

		public List<RelatedObjectLookupRpcResponse> getClasses() { return iClasses; }
		public void setClasses(List<RelatedObjectLookupRpcResponse> classes) { iClasses = classes; }
		public RelatedObjectLookupRpcResponse getClass(String value) {
			if (value == null || value.isEmpty() || iClasses == null) return null;
			for (RelatedObjectLookupRpcResponse r: iClasses)
				if (r.getUniqueId() != null && r.getUniqueId().toString().equals(value)) return r;
			return null;
		}

	}
	
	private List<RelatedObjectInterface> iLastRelatedObjects = null;
	private List<MeetingInterface> iLastMeetings = null;
	public void checkEnrollments(final List<RelatedObjectInterface> relatedObjects, final List<MeetingInterface> meetings) {
		if (relatedObjects == null || relatedObjects.isEmpty() || getEventType() != EventType.Course) {
			iForm.getRowFormatter().setVisible(iEnrollmentRow, false);
			iForm.getRowFormatter().setVisible(iEnrollmentRow + 1, false);
		} else {
			iForm.getRowFormatter().setVisible(iEnrollmentRow, true);
			iForm.getRowFormatter().setVisible(iEnrollmentRow + 1, true);
			if (relatedObjects.equals(iLastRelatedObjects) && meetings.equals(iLastMeetings)) return;
			iEnrollmentHeader.showLoading();
			iLastMeetings = meetings; iLastRelatedObjects = relatedObjects;
			RPC.execute(EventEnrollmentsRpcRequest.getEnrollmentsForRelatedObjects(relatedObjects, meetings, iEvent.getId(), iProperties.getSessionId()), new AsyncCallback<GwtRpcResponseList<ClassAssignmentInterface.Enrollment>>() {
				@Override
				public void onFailure(Throwable caught) {
					if (relatedObjects.equals(iLastRelatedObjects) && meetings.equals(iLastMeetings)) {
						iEnrollments.clear();
						UniTimeNotifications.error(MESSAGES.failedNoEnrollments(caught.getMessage()), caught);
					}
				}

				@Override
				public void onSuccess(GwtRpcResponseList<Enrollment> result) {
					if (relatedObjects.equals(iLastRelatedObjects) && meetings.equals(iLastMeetings)) {
						iEnrollmentHeader.clearMessage();
						iEnrollments.clear();
						iEnrollments.populate(result, null);
					}
				}
			});
		}
	}

	public static class CourseRelatedObjectsTable extends UniTimeTable<CourseRelatedObjectLine> implements HasValue<List<RelatedObjectInterface>> {
		private Timer iChangeTimer = null;
		private static int sChangeWaitTime = 500;
		private AcademicSessionProvider iSession = null;
		private List<RelatedObjectInterface> iLastChange = null;
		
		public CourseRelatedObjectsTable(AcademicSessionProvider session) {
			iSession = session;
			setStyleName("unitime-EventOwners");
			
			List<Widget> header = new ArrayList<Widget>();
			header.add(new UniTimeTableHeader(MESSAGES.colSubject()));
			header.add(new UniTimeTableHeader(MESSAGES.colCourseNumber()));
			header.add(new UniTimeTableHeader(MESSAGES.colConfigOrSubpart()));
			header.add(new UniTimeTableHeader(MESSAGES.colClassNumber()));
			header.add(new UniTimeTableHeader("&nbsp;"));
			
			addRow(null, header);
			
			iChangeTimer = new Timer() {
				@Override
				public void run() {
					List<RelatedObjectInterface> value = getValue();
					if (iLastChange != null && iLastChange.equals(value)) return;
					iLastChange = value;
					ValueChangeEvent.fire(CourseRelatedObjectsTable.this, value);
				}
			};
		}
		
		private void addLine(final RelatedObjectInterface data) {
			List<Widget> row = new ArrayList<Widget>();
			
			final CourseRelatedObjectLine line = new CourseRelatedObjectLine();
			
			final ListBox subject = new ListBox();
			subject.addStyleName("subject");
			subject.addItem("-", "");
			subject.setSelectedIndex(0);
			RPC.execute(RelatedObjectLookupRpcRequest.getChildren(iSession.getAcademicSessionId(), RelatedObjectLookupRpcRequest.Level.SESSION, iSession.getAcademicSessionId()), new AsyncCallback<GwtRpcResponseList<RelatedObjectLookupRpcResponse>>() {

				@Override
				public void onFailure(Throwable caught) {
					UniTimeNotifications.error(MESSAGES.failedLoad(MESSAGES.colSubjects(), caught.getMessage()), caught);
				}

				@Override
				public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
					line.setSubjects(result);
					Long selectedId = (data != null && data.hasSelection() ? data.getSelection()[0] : null);
					int selectedIdx = -1;
					for (RelatedObjectLookupRpcResponse r: result) {
						subject.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getUniqueId().toString());
						if (selectedId != null && selectedId.equals(r.getUniqueId())) selectedIdx = subject.getItemCount() - 1;
					}
					if (selectedIdx >= 0)
						subject.setSelectedIndex(selectedIdx);
					DomEvent.fireNativeEvent(Document.get().createChangeEvent(), subject);
				}
			});
			
			row.add(subject);
			
			final ListBox course = new ListBox();
			course.addStyleName("course");
			course.addItem(MESSAGES.itemNotApplicable(), "");
			course.setSelectedIndex(0);
			
			subject.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					final RelatedObjectLookupRpcResponse rSubject = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
					if (rSubject == null) {
						course.clear();
						course.addItem(MESSAGES.itemNotApplicable(), "");
						course.setSelectedIndex(0);
						DomEvent.fireNativeEvent(Document.get().createChangeEvent(), course);
					} else {
						course.clear();
						RPC.execute(RelatedObjectLookupRpcRequest.getChildren(iSession.getAcademicSessionId(), rSubject), new AsyncCallback<GwtRpcResponseList<RelatedObjectLookupRpcResponse>>() {
							@Override
							public void onFailure(Throwable caught) {
								UniTimeNotifications.error(MESSAGES.failedLoad(MESSAGES.colCourses(), caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
								RelatedObjectLookupRpcResponse res = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
								if (!rSubject.equals(res)) return;
								course.clear();
								line.setCourses(result);
								if (result.size() > 1)
									course.addItem("-", "");
								Long selectedId = (data != null && data.hasSelection() ? data.getSelection()[1] : null);
								int selectedIdx = -1;
								for (RelatedObjectLookupRpcResponse r: result) {
									course.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getUniqueId().toString());
									if (selectedId != null && selectedId.equals(r.getUniqueId())) selectedIdx = course.getItemCount() - 1;
								}
								if (selectedIdx >= 0)
									course.setSelectedIndex(selectedIdx);
								DomEvent.fireNativeEvent(Document.get().createChangeEvent(), course);
							}
						});
						if (line.equals(getData(getRowCount() - 1))) addLine(null);
					}

					iChangeTimer.schedule(sChangeWaitTime);
				}
			});
			
			row.add(course);
			
			final ListBox subpart = new ListBox();
			subpart.addStyleName("subpart");
			subpart.addItem(MESSAGES.itemNotApplicable(), "");
			subpart.setSelectedIndex(0);
			
			course.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					RelatedObjectLookupRpcResponse rSubject = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
					final RelatedObjectLookupRpcResponse rCourse = (course.getSelectedIndex() < 0 ? null : line.getCourse(course.getValue(course.getSelectedIndex())));
					if (rCourse == null) {
						subpart.clear();
						subpart.addItem(MESSAGES.itemNotApplicable(), "");
						DomEvent.fireNativeEvent(Document.get().createChangeEvent(), subpart);
					} else {
						subpart.clear();
						RPC.execute(RelatedObjectLookupRpcRequest.getChildren(iSession.getAcademicSessionId(), rSubject, rCourse), new AsyncCallback<GwtRpcResponseList<RelatedObjectLookupRpcResponse>>() {
							@Override
							public void onFailure(Throwable caught) {
								UniTimeNotifications.error(MESSAGES.failedLoad(MESSAGES.colConfigsOrSubparts(), caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
								RelatedObjectLookupRpcResponse res = (course.getSelectedIndex() < 0 ? null : line.getCourse(course.getValue(course.getSelectedIndex())));
								if (!rCourse.equals(res)) return;
								subpart.clear();
								line.setSubparts(result);
								Long selectedId = (data != null && data.hasSelection() && data.getSelection().length >= 3 ? data.getSelection()[2] : null);
								int selectedIdx = -1;
								RelatedObjectLookupRpcRequest.Level selectedLevel = RelatedObjectLookupRpcRequest.Level.NONE;
								if (data != null && data.hasSelection()) {
									switch (data.getType()) {
									case Config: selectedLevel = RelatedObjectLookupRpcRequest.Level.CONFIG; break;
									case Course: selectedLevel = RelatedObjectLookupRpcRequest.Level.COURSE; break;
									case Offering: selectedLevel = RelatedObjectLookupRpcRequest.Level.OFFERING; break;
									case Class: selectedLevel = RelatedObjectLookupRpcRequest.Level.SUBPART; break;
									}
								}
								for (RelatedObjectLookupRpcResponse r: result) {
									subpart.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getLevel() + ":" + r.getUniqueId().toString());
									if (selectedLevel == r.getLevel()) {
										switch (r.getLevel()) {
										case COURSE:
										case OFFERING:
											selectedIdx = subpart.getItemCount() - 1;
											break;
										case SUBPART:
										case CONFIG:
											if (r.getUniqueId().equals(selectedId))
												selectedIdx = subpart.getItemCount() - 1;
											break;
										}
									}
								}
								if (selectedIdx >= 0)
									subpart.setSelectedIndex(selectedIdx);
								DomEvent.fireNativeEvent(Document.get().createChangeEvent(), subpart);
							}
						});
					}
					
					iChangeTimer.schedule(sChangeWaitTime);
				}
			});
			
			row.add(subpart);
			
			final ListBox clazz = new ListBox();
			clazz.addStyleName("class");
			clazz.addItem(MESSAGES.itemNotApplicable(), "");
			clazz.setSelectedIndex(0);
			
			subpart.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					RelatedObjectLookupRpcResponse rSubject = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
					RelatedObjectLookupRpcResponse rCourse = (course.getSelectedIndex() < 0 ? null : line.getCourse(course.getValue(course.getSelectedIndex())));
					final RelatedObjectLookupRpcResponse rSubpart = (subpart.getSelectedIndex() < 0 ? null : line.getSubpart(subpart.getValue(subpart.getSelectedIndex())));
					if (rSubpart == null) {
						clazz.clear();
						clazz.addItem(MESSAGES.itemNotApplicable(), "");
						clazz.setSelectedIndex(0);
					} else {
						clazz.clear();
						RPC.execute(RelatedObjectLookupRpcRequest.getChildren(iSession.getAcademicSessionId(), rSubject, rCourse, rSubpart), new AsyncCallback<GwtRpcResponseList<RelatedObjectLookupRpcResponse>>() {
							@Override
							public void onFailure(Throwable caught) {
								UniTimeNotifications.error(MESSAGES.failedLoad(MESSAGES.colClasses(), caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
								RelatedObjectLookupRpcResponse res = (subpart.getSelectedIndex() < 0 ? null : line.getSubpart(subpart.getValue(subpart.getSelectedIndex())));
								if (!rSubpart.equals(res)) return;
								clazz.clear();
								line.setClasses(result);
								if (result.size() > 1)
									clazz.addItem("-", "");
								Long selectedId = (data != null && data.hasSelection() && data.getSelection().length >= 4 ? data.getSelection()[3] : null);
								int selectedIdx = -1;
								for (int idx = 0; idx < result.size(); idx++) {
									RelatedObjectLookupRpcResponse r = result.get(idx);
									clazz.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getUniqueId().toString());
									if (selectedId != null && selectedId.equals(r.getUniqueId())) selectedIdx = idx + (result.size() > 1 ? 1 : 0);
								}
								if (selectedIdx >= 0)
									clazz.setSelectedIndex(selectedIdx);
							}
						});
					}
					
					iChangeTimer.schedule(sChangeWaitTime);
				}
			});
			
			clazz.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					iChangeTimer.schedule(sChangeWaitTime);
				}
			});
			
			row.add(clazz);
			
			Image remove = new Image(RESOURCES.delete());
			remove.addStyleName("remove");
			remove.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					for (int row = 1; row < getRowCount(); row ++)
						if (line.equals(getData(row))) {
							removeRow(row);
							break;
						}
					if (getRowCount() <= 1) addLine(null);
					iChangeTimer.schedule(sChangeWaitTime);
				}
			});
			row.add(remove);
			
			addRow(line, row);
		}

		@Override
		public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<RelatedObjectInterface>> handler) {
			return addHandler(handler, ValueChangeEvent.getType());
		}

		@Override
		public List<RelatedObjectInterface> getValue() {
			List<RelatedObjectInterface> objects = new ArrayList<RelatedObjectInterface>();
			for (int row = 1; row < getRowCount(); row ++) {
				CourseRelatedObjectLine line = getData(row);
				ListBox subject = (ListBox)getWidget(row, 0);
				RelatedObjectLookupRpcResponse rSubject = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
				ListBox course = (ListBox)getWidget(row, 1);
				RelatedObjectLookupRpcResponse rCourse = (course.getSelectedIndex() < 0 ? null : line.getCourse(course.getValue(course.getSelectedIndex())));
				ListBox subpart = (ListBox)getWidget(row, 2);
				RelatedObjectLookupRpcResponse rSubpart = (subpart.getSelectedIndex() < 0 ? null : line.getSubpart(subpart.getValue(subpart.getSelectedIndex())));
				ListBox clazz = (ListBox)getWidget(row, 3);
				RelatedObjectLookupRpcResponse rClazz = (clazz.getSelectedIndex() < 0 ? null : line.getClass(clazz.getValue(clazz.getSelectedIndex())));
				if (rClazz != null && rClazz.getRelatedObject() != null) {
					objects.add(rClazz.getRelatedObject()); continue;
				}
				if (rSubpart != null && rSubpart.getRelatedObject() != null) {
					objects.add(rSubpart.getRelatedObject()); continue;
				}
				if (rCourse != null && rCourse.getRelatedObject() != null) {
					objects.add(rCourse.getRelatedObject()); continue;
				}
				if (rSubject != null && rSubject.getRelatedObject() != null) {
					objects.add(rSubject.getRelatedObject()); continue;
				}
			}
			return objects;
		}

		@Override
		public void setValue(List<RelatedObjectInterface> value) {
			setValue(value, false);
		}

		@Override
		public void setValue(List<RelatedObjectInterface> value, boolean fireEvents) {
			iLastChange = null;
			clearTable(1);
			if (value != null)
				for (RelatedObjectInterface line: value)
					addLine(line);
			addLine(null);
			addLine(null);
			if (fireEvents)
				ValueChangeEvent.fire(CourseRelatedObjectsTable.this, getValue());
		}
		
	}
	
	
	public void validate(final AsyncCallback<Boolean> callback) {
		iHeader.clearMessage();
		boolean valid = true;
		if (iName.getWidget().getText().isEmpty() && getEventType() != EventType.Unavailabile) {
			iName.setErrorHint(MESSAGES.reqEventName());
			UniTimeNotifications.error(MESSAGES.reqEventName());
			iHeader.setErrorMessage(MESSAGES.reqEventName());
			valid = false;
		} else {
			iName.clearHint();
		}
		if (iMainLName.getWidget().getText().isEmpty() && getEventType() != EventType.Unavailabile) {
			UniTimeNotifications.error(MESSAGES.reqMainContactLastName());
			if (valid)
				iHeader.setErrorMessage(MESSAGES.reqMainContactLastName());
			valid = false;
		} else {
			iMainLName.clearHint();
		}
		if (iMainEmail.getWidget().getText().isEmpty() && getEventType() != EventType.Unavailabile) {
			UniTimeNotifications.error(MESSAGES.reqMainContactEmail());
			if (valid)
				iHeader.setErrorMessage(MESSAGES.reqMainContactEmail());
			valid = false;
		} else {
			iMainEmail.clearHint();
		}
		if (hasMainContactChanged() && !iMainContactChanged.getValue()) {
			UniTimeNotifications.error(iMainContactChanged.getText());
			if (valid)
				iHeader.setErrorMessage(iMainContactChanged.getText());
			valid = false;
		}
		if (iMeetings.getValue().isEmpty() && iEvent.getId() == null) {
			UniTimeNotifications.error(MESSAGES.reqMeetings());
			if (valid)
				iHeader.setErrorMessage(MESSAGES.reqMeetings());
			valid = false;
		}
		if (iExpirationDate.getValue() != null && iExpirationDate.getValue().before(iExpirationDate.today()) && iForm.getRowFormatter().isVisible(iForm.getRow(MESSAGES.propExpirationDate()))) {
			for (EventMeetingRow row: iMeetings.getValue()) {
				if (row.getMeeting().getId() == null && row.getMeeting().getApprovalStatus() == ApprovalStatus.Pending) { // there is a new meeting that could get expired
					UniTimeNotifications.error(MESSAGES.errorExpirationDateInPast());
					if (valid)
						iHeader.setErrorMessage(MESSAGES.errorExpirationDateInPast());
					valid = false;
					break;
				}
			}
		}
		if (getEventType() == EventType.Course && iCourses.getValue().isEmpty()) {
			UniTimeNotifications.error(MESSAGES.reqCoursesOrClasses());
			if (valid)
				iHeader.setErrorMessage(MESSAGES.reqCoursesOrClasses());
			valid = false;
		}
		callback.onSuccess(valid);
	}
	
	protected EventPropertiesRpcResponse getProperties() {
		return iProperties == null ? null : iProperties.getProperties();
	}

	public static interface EventPropertiesProvider extends DateFlagsProvider {
		public Long getSessionId();
		public EventPropertiesRpcResponse getProperties();
		public List<SelectionInterface> getSelection(); 
		public String getRoomFilter();
		public ContactInterface getMainContact();
		public List<Date> getSelectedDates();
		public StartEndTime getSelectedTime();
		public boolean isTooEarly(int startSlot, int endSlot);
	}
	
	private void showCreateButtonIfApplicable() {
		iHeader.setEnabled("create", iEvent.getId() == null && getProperties() != null && getProperties().isCanAddEvent() && iMeetings.getRowCount() > 1);
		if (iEvent.getId() == null && getProperties() != null && getProperties().isCanAddEvent() && getProperties().hasEmailConfirmation()) {
			iEmailConfirmationHeader.setVisible(iMeetings.getRowCount() > 1);
			iEmailConfirmationFooter.setVisible(iMeetings.getRowCount() > 1);
		}
	}


	@Override
	public void execute(EventMeetingTable source, OperationType operation, List<EventMeetingRow> selection) {
		switch (operation) {
		case AddMeetings:
			iEventAddMeetings.showDialog(getEventId(), getConflicts());
			break;
		case Delete:
		case Cancel:
			checkEnrollments(iCourses.getValue(), iMeetings.getMeetings());
			showCreateButtonIfApplicable();
			break;
		case Modify:
			iSelection = new ArrayList<MeetingInterface>();
			for (EventMeetingRow row: selection)
				iSelection.add(row.getMeeting());
			List<MeetingConflictInterface> conflicts = new ArrayList<MeetingConflictInterface>();
			for (MeetingInterface m: source.getMeetings()) {
				if ((m.getApprovalStatus() == ApprovalStatus.Approved || m.getApprovalStatus() == ApprovalStatus.Pending) && !iSelection.contains(m))
					conflicts.add(generateConflict(m));
			}
			iEventModifyMeetings.reset(iProperties == null ? null : iProperties.getRoomFilter(), iSelection, iProperties == null ? null : iProperties.getSelectedDates(), iProperties == null ? null : iProperties.getSelectedTime());
			iEventModifyMeetings.showDialog(getEventId(), conflicts);
			break;
		}
	}
	
	protected MeetingConflictInterface generateConflict(MeetingInterface m) {
		MeetingConflictInterface conflict = new MeetingConflictInterface(m);
		conflict.setEventId(getEventId());
		conflict.setName(iName.getWidget().getText());
		conflict.setType(getEventType());
		return conflict;
	}
	
	protected List<MeetingConflictInterface> getConflicts() {
		List<MeetingConflictInterface> conflicts = new ArrayList<MeetingConflictInterface>();
		for (MeetingInterface m: iMeetings.getMeetings()) {
			if (m.getApprovalStatus() == ApprovalStatus.Approved || m.getApprovalStatus() == ApprovalStatus.Pending)
				conflicts.add(generateConflict(m));
		}
		return conflicts;
	}

	@Override
	public boolean accept(AcademicSession session) {
		return session.has(AcademicSession.Flag.CanAddEvents);
	}

}
