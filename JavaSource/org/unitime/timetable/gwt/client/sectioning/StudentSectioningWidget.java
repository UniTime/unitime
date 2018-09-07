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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTabBar;
import org.unitime.timetable.gwt.client.sectioning.TimeGrid.Meeting;
import org.unitime.timetable.gwt.client.widgets.CourseFinder;
import org.unitime.timetable.gwt.client.widgets.CourseFinderClasses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderCourses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDetails;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDialog;
import org.unitime.timetable.gwt.client.widgets.DataProvider;
import org.unitime.timetable.gwt.client.widgets.ImageLink;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.CancelSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.CancelSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAllSpecialRegistrationsRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationStatus;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck.EligibilityFlag;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface;
import org.unitime.timetable.gwt.shared.UserAuthenticationProvider;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class StudentSectioningWidget extends Composite implements HasResizeHandlers {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	public static final GwtMessages GWT_MESSAGES = GWT.create(GwtMessages.class);

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private AcademicSessionProvider iSessionSelector;
	private UserAuthenticationProvider iUserAuthentication;
	
	private VerticalPanel iPanel;
	private P iFooter;
	private AriaButton iRequests, iReset, iSchedule, iEnroll, iPrint, iExport = null, iSave, iStartOver, iDegreePlan, iSubmitSpecReg, iGetSpecRegs;
	private AriaTabBar iAssignmentTab;
	private DockPanel iAssignmentDock;
	private FocusPanel iAssignmentPanel;
	private ImageLink iCalendar = null;
	private P iWaiting = null, iWaitingMessage;
	
	private CourseRequestsTable iCourseRequests;
	private WebTable iAssignments;
	private TimeGrid iAssignmentGrid;
	private SuggestionsBox iSuggestionsBox;
	private CheckBox iShowUnassignments;
	private Label iTotalCredit;
	private P iGridMessage;
	private Image iTotalCreditRequestsStatus;
	private P iTotalCreditRequests;
	
	private ArrayList<ClassAssignmentInterface.ClassAssignment> iLastResult, iLastEnrollment;
	private ClassAssignmentInterface iLastAssignment, iSavedAssignment = null, iSpecialRegAssignment = null;
	private CourseRequestInterface iSavedRequest = null;
	private ArrayList<HistoryItem> iHistory = new ArrayList<HistoryItem>();
	private boolean iInRestore = false;
	private boolean iTrackHistory = true;
	private boolean iOnline;
	private SpecialRegistrationContext iSpecRegCx = new SpecialRegistrationContext();
	private StudentSectioningPage.Mode iMode = null;
	private OnlineSectioningInterface.EligibilityCheck iEligibilityCheck = null;
	private PinDialog iPinDialog = null;
	private boolean iScheduleChanged = false;
	private ScheduleStatus iStatus = null;
	private AriaButton iQuickAdd;
	private CourseFinder iQuickAddFinder = null;
	private SuggestionsBox iQuickAddSuggestions = null;
	
	private CheckBox iCustomCheckbox = null;
	private DegreePlansSelectionDialog iDegreePlansSelectionDialog = null;
	private DegreePlanDialog iDegreePlanDialog = null;
	private SpecialRegistrationSelectionDialog iSpecialRegistrationSelectionDialog = null;

	public StudentSectioningWidget(boolean online, AcademicSessionProvider sessionSelector, UserAuthenticationProvider userAuthentication, StudentSectioningPage.Mode mode, boolean history) {
		iMode = mode;
		iOnline = online;
		iSpecRegCx.setRequestKey(Location.getParameter("reqKey"));
		iSessionSelector = sessionSelector;
		iUserAuthentication = userAuthentication;
		iTrackHistory = history;
		
		iPanel = new VerticalPanel();
		iPanel.addStyleName("unitime-SchedulingAssistant");
		
		iCourseRequests = new CourseRequestsTable(iSessionSelector, iMode.isSectioning(), iOnline, iSpecRegCx);
		iCourseRequests.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<CourseRequestInterface> event) {
				if (iTotalCreditRequests != null) {
					iTotalCreditRequestsStatus.setVisible(false);
					if (!isChanged() && iSavedRequest != null && iSavedRequest.getMaxCreditOverrideStatus() != null) {
						switch (iSavedRequest.getMaxCreditOverrideStatus()) {
						case CREDIT_HIGH:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestError());
							iTotalCreditRequestsStatus.setAltText(iSavedRequest.getCreditWarning() + "\n" + MESSAGES.creditStatusTooHigh() + (iSavedRequest.hasCreditNote() ? "\n" + MESSAGES.overrideNote(iSavedRequest.getCreditNote()) : ""));
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case OVERRIDE_REJECTED:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestError());
							iTotalCreditRequestsStatus.setAltText(iSavedRequest.getCreditWarning() + "\n" + MESSAGES.creditStatusDenied() + (iSavedRequest.hasCreditNote() ? "\n" + MESSAGES.overrideNote(iSavedRequest.getCreditNote()) : ""));
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case OVERRIDE_CANCELLED:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestNeeded());
							iTotalCreditRequestsStatus.setAltText(iSavedRequest.getCreditWarning() + "\n" + MESSAGES.creditStatusCancelled() + (iSavedRequest.hasCreditNote() ? "\n" + MESSAGES.overrideNote(iSavedRequest.getCreditNote()) : ""));
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case CREDIT_LOW:
						case OVERRIDE_NEEDED:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestNeeded());
							iTotalCreditRequestsStatus.setAltText(iSavedRequest.getCreditWarning() + (iSavedRequest.hasCreditNote() ? "\n" + MESSAGES.overrideNote(iSavedRequest.getCreditNote()) : ""));
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case OVERRIDE_PENDING:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestPending());
							iTotalCreditRequestsStatus.setAltText(iSavedRequest.getCreditWarning() + "\n" + MESSAGES.creditStatusPending() + (iSavedRequest.hasCreditNote() ? "\n" + MESSAGES.overrideNote(iSavedRequest.getCreditNote()) : ""));
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case OVERRIDE_APPROVED:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestSaved());
							iTotalCreditRequestsStatus.setAltText(MESSAGES.creditStatusApproved() + (iSavedRequest.hasCreditNote() ? "\n" + MESSAGES.overrideNote(iSavedRequest.getCreditNote()) : ""));
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case SAVED:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestSaved());
							iTotalCreditRequestsStatus.setAltText(iSavedRequest.hasCreditNote() ? MESSAGES.overrideNote(iSavedRequest.getCreditNote()) : "");
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						}
					}
					float[] credit = iCourseRequests.getRequest().getCreditRange();
					if (credit[1] > 0) {
						if (credit[0] != credit[1])
							iTotalCreditRequests.setText(MESSAGES.totalCreditRange(credit[0], credit[1]));
						else
							iTotalCreditRequests.setText(MESSAGES.totalCredit(credit[0]));
					} else {
						iTotalCreditRequests.setText("");
					}
				}
				if (!iMode.isSectioning() && iSavedRequest != null && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_REGISTER)) {
					if (!iSavedRequest.equals(iCourseRequests.getRequest())) {
						iScheduleChanged = true;
						iSave.addStyleName("unitime-EnrollButton");
						iStatus.warning(iSavedRequest.isEmpty() ? MESSAGES.warnRequestsEmptyOnCourseRequest() : MESSAGES.warnRequestsChangedOnCourseRequest(), false);
					} else if (iScheduleChanged) {
						iScheduleChanged = false;
						iSave.removeStyleName("unitime-EnrollButton");
						clearMessage();
					}
					return;
				}
				if (iLastAssignment == null || !iLastAssignment.isCanEnroll() || iEligibilityCheck == null || !iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL))
					return;
				if (!iScheduleChanged) {
					courses: for (ClassAssignmentInterface.CourseAssignment course: iLastAssignment.getCourseAssignments()) {
						if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
						for (CourseRequestInterface.Request r: event.getValue().getCourses()) {
							if (r.hasRequestedCourse(course)) continue courses;
						}
						for (CourseRequestInterface.Request r: event.getValue().getAlternatives()) {
							if (r.hasRequestedCourse(course)) continue courses;
						}
						iScheduleChanged = true;
						iStatus.warning(MESSAGES.warnScheduleChangedOnCourseRequest(), false);
						iEnroll.addStyleName("unitime-EnrollButton");
						return;
					}
				} else if (MESSAGES.warnScheduleChangedOnCourseRequest().equals(iStatus.getMessage())) {
					updateScheduleChangedNoteIfNeeded();
				}
			}
		});
		
		iPanel.add(iCourseRequests);
		
		iFooter = new P("unitime-SchedulingAssistantButtons");
		
		P leftFooterPanel = new P("left-panel");
		iDegreePlan = new AriaButton(MESSAGES.buttonDegreePlan());
		iDegreePlan.setTitle(MESSAGES.hintDegreePlan());
		iDegreePlan.setVisible(false);
		iDegreePlan.setEnabled(false);
		leftFooterPanel.add(iDegreePlan);
		
		iRequests = new AriaButton(RESOURCES.arrowBack(), MESSAGES.buttonRequests());
		iRequests.setTitle(MESSAGES.hintRequests());
		iRequests.setVisible(false);
		iRequests.setEnabled(false);
		leftFooterPanel.add(iRequests);

		iReset = new AriaButton(MESSAGES.buttonReset());
		iReset.setTitle(MESSAGES.hintReset());
		iReset.setVisible(false);
		iReset.setEnabled(false);
		iReset.getElement().getStyle().setMarginLeft(4, Unit.PX);
		leftFooterPanel.add(iReset);
		iFooter.add(leftFooterPanel);
		
		if (mode == StudentSectioningPage.Mode.REQUESTS) {
			iTotalCreditRequestsStatus = new Image(); iTotalCreditRequestsStatus.addStyleName("credit-status"); iTotalCreditRequestsStatus.setVisible(false);
			iTotalCreditRequestsStatus.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (iTotalCreditRequestsStatus.getAltText() != null && !iTotalCreditRequestsStatus.getAltText().isEmpty())
						UniTimeConfirmationDialog.info(iTotalCreditRequestsStatus.getAltText());
				}
			});
			iCourseRequests.setCreditStatusIcon(iTotalCreditRequestsStatus);
			iTotalCreditRequests = new P("credit-text");
			P credit = new P("center-panel", "total-request-credit");
			credit.add(iTotalCreditRequestsStatus);
			credit.add(iTotalCreditRequests);
			iFooter.add(credit);
		}

		P rightFooterPanel = new P("right-panel");
		iFooter.add(rightFooterPanel);
		
		iStartOver = new AriaButton(MESSAGES.buttonStartOver());
		iStartOver.setTitle(MESSAGES.hintStartOver());
		leftFooterPanel.add(iStartOver);
		iStartOver.setVisible(false);
		iStartOver.setEnabled(false);
		iStartOver.getElement().getStyle().setMarginLeft(4, Unit.PX);
		
		iGetSpecRegs = new AriaButton(MESSAGES.buttonGetSpecRegs());
		iGetSpecRegs.setTitle(MESSAGES.hintGetSpecRegs());
		leftFooterPanel.add(iGetSpecRegs);
		iGetSpecRegs.setVisible(false);
		iGetSpecRegs.setEnabled(false);
		iGetSpecRegs.getElement().getStyle().setMarginLeft(4, Unit.PX);

		iSchedule = new AriaButton(MESSAGES.buttonSchedule(), RESOURCES.arrowForward());
		iSchedule.setTitle(MESSAGES.hintSchedule());
		if (mode.isSectioning())
			rightFooterPanel.add(iSchedule);
		iSchedule.setVisible(mode.isSectioning());
		iSchedule.setEnabled(mode.isSectioning());
		
		iSave = new AriaButton(MESSAGES.buttonSave());
		iSave.setTitle(MESSAGES.hintSave());
		if (!mode.isSectioning())
			rightFooterPanel.add(iSave);
		iSave.setVisible(!mode.isSectioning());
		iSave.setEnabled(false);

		iEnroll = new AriaButton(MESSAGES.buttonEnroll());
		iEnroll.setTitle(MESSAGES.hintEnroll());
		iEnroll.setVisible(false);
		iEnroll.setEnabled(false);
		iEnroll.getElement().getStyle().setMarginLeft(4, Unit.PX);
		rightFooterPanel.add(iEnroll);
		
		iWaiting = new P("unitime-Waiting");
		iWaiting.add(new Image(RESOURCES.loading_small()));
		iWaitingMessage = new P("waiting-message");
		iWaiting.add(iWaitingMessage);
		iWaiting.setVisible(false);
		rightFooterPanel.add(iWaiting);
		
		iSubmitSpecReg = new AriaButton(MESSAGES.buttonSubmitSpecReg());
		iSubmitSpecReg.setTitle(MESSAGES.hintSpecialRegistration());
		iSubmitSpecReg.setVisible(false);
		iSubmitSpecReg.setEnabled(false);
		iSubmitSpecReg.getElement().getStyle().setMarginLeft(4, Unit.PX);
		rightFooterPanel.add(iSubmitSpecReg);

		iPrint = new AriaButton(MESSAGES.buttonPrint());
		iPrint.setTitle(MESSAGES.hintPrint());
		iPrint.setVisible(false);
		iPrint.setEnabled(false);
		iPrint.getElement().getStyle().setMarginLeft(4, Unit.PX);
		rightFooterPanel.add(iPrint);

		if (CONSTANTS.allowCalendarExport()) {
			iExport = new AriaButton(MESSAGES.buttonExport());
			iExport.setTitle(MESSAGES.hintExport());
			iExport.setVisible(false);
			iExport.setEnabled(false);
			iExport.getElement().getStyle().setMarginLeft(4, Unit.PX);
			rightFooterPanel.add(iExport);
		}
		
		iPanel.add(iFooter);
		
		iStatus = new ScheduleStatus();
		iPanel.add(iStatus);
		
		iLastResult = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
		
		iQuickAdd = new AriaButton(RESOURCES.quickAddCourse(), MESSAGES.buttonQuickAdd());
		iQuickAdd.setTitle(MESSAGES.hintQuickAdd());
		iQuickAdd.setStyleName("unitime-QuickAddButton");
		iQuickAdd.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getQuickAddFinder().findCourse();
			}
		});
		iQuickAdd.setEnabled(false);
		iQuickAdd.setVisible(false);
		
		iDegreePlan.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.waitListDegreePlans());
				iSectioningService.listDegreePlans(iOnline, iSessionSelector.getAcademicSessionId(), null, new AsyncCallback<List<DegreePlanInterface>>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						if (caught instanceof SectioningException) {
							SectioningException s = (SectioningException)caught;
							if (s.isInfo())
								iStatus.info(s.getMessage());
							else if (s.isWarning())
								iStatus.warning(s.getMessage());
							else if (s.isError())
								iStatus.error(s.getMessage());
							else
								iStatus.error(MESSAGES.failedListDegreePlans(s.getMessage()), s);
						} else {
							iStatus.error(MESSAGES.failedListDegreePlans(caught.getMessage()), caught);
						}
					}
					@Override
					public void onSuccess(List<DegreePlanInterface> result) {
						LoadingWidget.getInstance().hide();
						if (result == null || result.isEmpty()) {
							iStatus.info(MESSAGES.failedNoDegreePlans());
						} else {
							CourseFinderDetails details = new CourseFinderDetails();
							details.setDataProvider(new DataProvider<CourseAssignment, String>() {
								@Override
								public void getData(CourseAssignment source, AsyncCallback<String> callback) {
									iSectioningService.retrieveCourseDetails(iSessionSelector.getAcademicSessionId(), source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
								}
							});
							CourseFinderClasses classes = new CourseFinderClasses(true, iSpecRegCx);
							classes.setDataProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
								@Override
								public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
									iSectioningService.listClasses(iOnline, iSessionSelector.getAcademicSessionId(), source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
								}
							});
							if (iDegreePlanDialog == null) {
								iDegreePlanDialog = new DegreePlanDialog(iMode, iCourseRequests, new DegreePlanDialog.AssignmentProvider() {
									@Override
									public ClassAssignmentInterface getSavedAssignment() {
										return iSavedAssignment;
									}
									@Override
									public ClassAssignmentInterface getLastAssignment() {
										return iLastAssignment;
									}
								}, details, classes) {
									protected void doBack() {
										super.doBack();
										iDegreePlansSelectionDialog.show();
									}
									protected void doApply() {
										updateHistory();
										super.doApply();
										addHistory();
									}
								};
							}
							if (iDegreePlansSelectionDialog == null) {
								iDegreePlansSelectionDialog = new DegreePlansSelectionDialog() {
									public void doSubmit(DegreePlanInterface plan) {
										super.doSubmit(plan);
										iDegreePlanDialog.open(plan, true);
									}
								};
							}
							if (result.size() == 1)
								iDegreePlanDialog.open(result.get(0), false);
							else
								iDegreePlansSelectionDialog.open(result);
						}
					}
				});
				
			}
		});

		initWidget(iPanel);
		
		init();
	}
	
	/*
	private void initAsync() {
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				init();
			}
			public void onFailure(Throwable reason) {
				Label error = new Label(MESSAGES.failedToLoadTheApp(reason.getMessage()));
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("loading").setVisible(false);
				RootPanel.get("body").add(error);
			}
		});
	}
	*/
	
	private void addHistory() {
		if (iInRestore || !iTrackHistory || iUserAuthentication.getUser() == null) return;
		iHistory.add(new HistoryItem());
		History.newItem(String.valueOf(iHistory.size() - 1), false);
	}
	
	private void updateHistory() {
		if (iInRestore || !iTrackHistory || iUserAuthentication.getUser() == null) return;
		if (!iHistory.isEmpty())
			iHistory.remove(iHistory.size() - 1);
		addHistory();
	}
	
	private void init() {
		if (CONSTANTS.allowCalendarExport()) {
			iCalendar = new ImageLink();
			iCalendar.setImage(new Image(RESOURCES.calendar()));
			iCalendar.setTarget(null);
			iCalendar.setTitle(MESSAGES.exportICalendar());
			iCalendar.setAriaLabel(MESSAGES.exportICalendar());
		}

		iAssignments = new WebTable();
		iAssignments.setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colLock()),
				new WebTable.Cell(MESSAGES.colSubject()),
				new WebTable.Cell(MESSAGES.colCourse()),
				new WebTable.Cell(MESSAGES.colSubpart()),
				new WebTable.Cell(MESSAGES.colClass()),
				new WebTable.Cell(MESSAGES.colLimit()).aria(ARIA.colLimit()),
				new WebTable.Cell(MESSAGES.colDays()),
				new WebTable.Cell(MESSAGES.colStart()),
				new WebTable.Cell(MESSAGES.colEnd()),
				new WebTable.Cell(MESSAGES.colDate()),
				new WebTable.Cell(MESSAGES.colRoom()),
				new WebTable.Cell(MESSAGES.colInstructor()),
				new WebTable.Cell(MESSAGES.colParent()),
				new WebTable.Cell(MESSAGES.colNote()),
				new WebTable.Cell(MESSAGES.colCredit()),
				(iCalendar != null ? new WebTable.WidgetCell(iCalendar, MESSAGES.colIcons()) : new WebTable.Cell(MESSAGES.colIcons()))
			));
		iAssignments.setWidth("100%");
		iAssignments.setEmptyMessage(MESSAGES.emptySchedule());
		
		ScrollPanel assignmentsPanel = new ScrollPanel(iAssignments);
		assignmentsPanel.setStyleName("body");
		assignmentsPanel.getElement().getStyle().setOverflowY(Overflow.HIDDEN);
		
		final P panel = new P("unitime-Panel");
		panel.add(assignmentsPanel);
		
		iTotalCredit = new Label("", false);
		iShowUnassignments = new CheckBox(MESSAGES.showUnassignments());
		iQuickAdd.addStyleName("left");
		P bottom = new P("footer");
		iTotalCredit.addStyleName("center");
		iTotalCredit.getElement().getStyle().setMarginTop(3, Unit.PX);
		iShowUnassignments.addStyleName("right");
		iShowUnassignments.getElement().getStyle().setMarginTop(3, Unit.PX);
		bottom.add(iQuickAdd);
		bottom.add(iShowUnassignments);
		bottom.add(iTotalCredit);
		panel.add(bottom);

		iShowUnassignments.setVisible(false);
		String showUnassignments = Cookies.getCookie("UniTime:Unassignments");
		iShowUnassignments.setValue(showUnassignments == null || "1".equals(showUnassignments));
		
		iAssignmentGrid = new TimeGrid();
		iGridMessage = new P("unitime-TimeGridMessage"); iGridMessage.setVisible(false);
		final P gridPanel = new P("unitime-TimeGridPanel");
		gridPanel.add(iGridMessage);
		gridPanel.add(iAssignmentGrid);
		
		iAssignmentTab = new AriaTabBar();
		iAssignmentTab.addTab(MESSAGES.tabClasses(), true);
		iAssignmentTab.addTab(MESSAGES.tabTimetable(), true);
		iAssignmentTab.selectTab(0);
		iAssignmentTab.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if (event.getSelectedItem() == 0) {
					iAssignmentPanel.setWidget(panel);
					AriaStatus.getInstance().setHTML(ARIA.listOfClasses());
				} else {
					iAssignmentGrid.shrink();
					iAssignmentPanel.setWidget(gridPanel);
					iAssignmentGrid.scrollDown();
					AriaStatus.getInstance().setHTML(ARIA.timetable());
				}
				addHistory();
				ResizeEvent.fire(StudentSectioningWidget.this, StudentSectioningWidget.this.getOffsetWidth(), StudentSectioningWidget.this.getOffsetHeight());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						iAssignmentPanel.setFocus(true);
					}
				});
			}
		});
		iAssignmentPanel = new FocusPanel(panel);
		iAssignmentPanel.setStyleName("unitime-ClassScheduleTabPanel");
		iAssignmentPanel.addStyleName("unitime-FocusPanel");
		
		final Character classesAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabClasses());
		final Character timetableAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabTimetable());
		iAssignmentPanel.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (iAssignmentTab.getSelectedTab() == 0) {
					if (event.getNativeKeyCode()==KeyCodes.KEY_DOWN) {
						do {
							iAssignments.setSelectedRow(iAssignments.getSelectedRow()+1);
						} while (iAssignments.getRows()[iAssignments.getSelectedRow()] != null && !iAssignments.getRows()[iAssignments.getSelectedRow()].isSelectable());
						
						if (iAssignments.getSelectedRow() >= 0 && iAssignments.getSelectedRow() < iAssignments.getRows().length && iAssignments.getRows()[iAssignments.getSelectedRow()] != null)
							AriaStatus.getInstance().setHTML(ARIA.classSelected(1 + iAssignments.getSelectedRow(), iAssignments.getRowsCount(), iAssignments.getRows()[iAssignments.getSelectedRow()].getAriaLabel()));
					}
					if (event.getNativeKeyCode()==KeyCodes.KEY_UP) {
						do {
							iAssignments.setSelectedRow(iAssignments.getSelectedRow()==0?iAssignments.getRowsCount()-1:iAssignments.getSelectedRow()-1);
						} while (iAssignments.getRows()[iAssignments.getSelectedRow()] != null && !iAssignments.getRows()[iAssignments.getSelectedRow()].isSelectable());
						
						if (iAssignments.getSelectedRow() >= 0 && iAssignments.getSelectedRow() < iAssignments.getRows().length && iAssignments.getRows()[iAssignments.getSelectedRow()] != null)
							AriaStatus.getInstance().setHTML(ARIA.classSelected(1 + iAssignments.getSelectedRow(), iAssignments.getRowsCount(), iAssignments.getRows()[iAssignments.getSelectedRow()].getAriaLabel()));
					}
					if (event.getNativeKeyCode()==KeyCodes.KEY_ENTER) {
						updateHistory();
						showSuggestionsAsync(iAssignments.getSelectedRow());
					}						
				}
				int tab = -1;
				if (classesAccessKey != null && event.getNativeEvent().getCtrlKey() && (
						event.getNativeKeyCode() == classesAccessKey || event.getNativeKeyCode() == Character.toUpperCase(classesAccessKey))) {
					tab = 0;
				}
				if (timetableAccessKey != null && event.getNativeEvent().getCtrlKey() && (
						event.getNativeKeyCode() == timetableAccessKey || event.getNativeKeyCode() == Character.toUpperCase(timetableAccessKey))) {
					tab = 1;
				}
				if (tab >= 0) {
					iAssignmentTab.selectTab(tab);
					event.preventDefault();
				}
			}
		});
		
		iAssignmentDock = new DockPanel();
		iAssignmentDock.setStyleName("unitime-ClassSchedulePanel");
		iAssignmentDock.setSpacing(0);
		iAssignmentDock.add(iAssignmentPanel, DockPanel.SOUTH);
		iAssignmentDock.add(iAssignmentTab, DockPanel.WEST);
		iAssignmentDock.setCellWidth(iAssignmentTab, "33%");
		iAssignmentDock.setCellVerticalAlignment(iAssignmentTab, HasVerticalAlignment.ALIGN_BOTTOM);
		iAssignmentDock.setCellHorizontalAlignment(iAssignmentTab, HasHorizontalAlignment.ALIGN_LEFT);
		Roles.getTabpanelRole().set(iAssignmentDock.getElement());
		Roles.getTabpanelRole().setAriaOwnsProperty(iAssignmentDock.getElement(), Id.of(iAssignmentTab.getElement()));
		
		P header = new P("unitime-MainHeader");
		header.setHTML(MESSAGES.headerClassSchedule());
		header.getElement().getStyle().setTextAlign(TextAlign.CENTER);
		iAssignmentDock.add(header, DockPanel.CENTER);
		iAssignmentDock.setCellHorizontalAlignment(header, HasHorizontalAlignment.ALIGN_CENTER);
		iAssignmentDock.setCellWidth(header, "34%");
		
		P padding = new P("unitime-HeaderPadding");
		padding.setHTML("&nbsp;");
		iAssignmentDock.add(padding, DockPanel.EAST);
		iAssignmentDock.setCellWidth(padding, "33%");

		iRequests.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				prev();
				addHistory();
			}
		});
		
		iReset.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearMessage();
				LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
				iSectioningService.section(iOnline, iCourseRequests.getRequest(), null, new AsyncCallback<ClassAssignmentInterface>() {
					public void onFailure(Throwable caught) {
						iStatus.error(MESSAGES.exceptionSectioningFailed(caught.getMessage()), caught);
						LoadingWidget.getInstance().hide();
						updateHistory();
					}
					public void onSuccess(ClassAssignmentInterface result) {
						fillIn(result);
						addHistory();
					}
				});
			}
		});
		
		iSchedule.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				iCourseRequests.changeTip();
				clearMessage();
				iCourseRequests.validate(new AsyncCallback<Boolean>() {
					public void onSuccess(Boolean result) {
						updateHistory();
						if (result) {
							LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
							iSectioningService.section(iOnline, iCourseRequests.getRequest(), iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
								public void onFailure(Throwable caught) {
									iStatus.error(MESSAGES.exceptionSectioningFailed(caught.getMessage()), caught);
									LoadingWidget.getInstance().hide();
									updateHistory();
								}
								public void onSuccess(ClassAssignmentInterface result) {
									fillIn(result);
									addHistory();
								}
							});								
						} else {
							String error = iCourseRequests.getFirstError();
							iStatus.error(error == null ? MESSAGES.validationFailed() : MESSAGES.validationFailedWithMessage(error));
							LoadingWidget.getInstance().hide();
							updateHistory();
						}
					}
					public void onFailure(Throwable caught) {
						iStatus.error(MESSAGES.validationFailedWithMessage(caught.getMessage()), caught);
						LoadingWidget.getInstance().hide();
						updateHistory();
					}
				});
			}
		});
		
		iStartOver.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isChanged()) {
					UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), iMode.isSectioning() ? MESSAGES.queryLeaveChangesOnClassSchedule() : MESSAGES.queryLeaveChangesOnCourseRequests(), new Command() {
						@Override
						public void execute() {
							clearMessage();
							clear(false);
							iStartOver.setVisible(false);
							iStartOver.setEnabled(false);
							iSpecRegCx.reset(iEligibilityCheck);
							addHistory();
							lastRequest(iSessionSelector.getAcademicSessionId(), null, true, false);
						}
					});
				} else {
					clearMessage();
					clear(false);
					iStartOver.setVisible(false);
					iStartOver.setEnabled(false);
					iSpecRegCx.reset(iEligibilityCheck);
					addHistory();
					lastRequest(iSessionSelector.getAcademicSessionId(), null, true, false);
				}
			}
		});
		
		iAssignments.addRowClickHandler(new WebTable.RowClickHandler() {
			public void onRowClick(WebTable.RowClickEvent event) {
				if (iLastResult.get(event.getRowIdx()) == null) return;
				updateHistory();
				showSuggestionsAsync(event.getRowIdx());
			}
		});
		
		iAssignmentGrid.addMeetingClickHandler(new TimeGrid.MeetingClickHandler() {
			public void onMeetingClick(TimeGrid.MeetingClickEvent event) {
				updateHistory();
				showSuggestionsAsync(event.getRowIndex());
			}
		});
		
		iAssignmentGrid.addPinClickHandler(new TimeGrid.PinClickHandler() {
			public void onPinClick(TimeGrid.PinClickEvent event) {
				((HasValue<Boolean>)iAssignments.getRows()[event.getRowIndex()].getCell(0).getWidget()).setValue(event.isPinChecked(), false);
				iLastResult.get(event.getRowIndex()).setPinned(event.isPinChecked());
				updateHistory();
			}
		});
		
		iEnroll.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Command enroll = new Command() {
					@Override
					public void execute() {
						clearMessage();
						LoadingWidget.getInstance().show(MESSAGES.waitEnroll());
						final ArrayList<ClassAssignmentInterface.ClassAssignment> lastEnrollment = new ArrayList<ClassAssignmentInterface.ClassAssignment>(iLastResult);
						iSectioningService.enroll(iOnline, iCourseRequests.getRequest(), iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
							public void onSuccess(ClassAssignmentInterface result) {
								LoadingWidget.getInstance().hide();
								iSavedAssignment = result;
								iStartOver.setVisible(iSavedAssignment != null && !iSavedAssignment.getCourseAssignments().isEmpty());
								iStartOver.setEnabled(iSavedAssignment != null && !iSavedAssignment.getCourseAssignments().isEmpty());
								fillIn(result);
								if (result.hasRequest())
									iCourseRequests.setRequest(result.getRequest());
								if (!result.hasMessages())
									iStatus.done(MESSAGES.enrollOK());
								updateHistory();
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.RECHECK_AFTER_ENROLLMENT)) {
									iSectioningService.checkEligibility(iOnline, iMode.isSectioning(), iSessionSelector.getAcademicSessionId(),
											iEligibilityCheck.getStudentId(), (String)null,
											new AsyncCallback<OnlineSectioningInterface.EligibilityCheck>() {
												@Override
												public void onFailure(Throwable caught) {
												}

												@Override
												public void onSuccess(OnlineSectioningInterface.EligibilityCheck result) {
													setElibibilityCheckDuringEnrollment(result);
												}
											});
								}
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG) && result.hasMessages())
									checkSpecialRegistrationAfterFailedSubmitSchedule(lastEnrollment, false);
							}
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								iStatus.error(MESSAGES.enrollFailed(caught.getMessage()), caught);
								updateHistory();
								if (caught instanceof SectioningException) {
									SectioningException se = (SectioningException) caught;
									EligibilityCheck check = se.getEligibilityCheck();
									if (check != null) {
										setElibibilityCheckDuringEnrollment(check);
										if (check.hasFlag(EligibilityFlag.PIN_REQUIRED)) {
											if (iPinDialog == null) iPinDialog = new PinDialog();
											PinDialog.PinCallback callback = new PinDialog.PinCallback() {
												@Override
												public void onFailure(Throwable caught) {
													iStatus.error(MESSAGES.exceptionFailedEligibilityCheck(caught.getMessage()), caught);
												}
												@Override
												public void onSuccess(OnlineSectioningInterface.EligibilityCheck result) {
													setElibibilityCheckDuringEnrollment(result);
													if (result.hasFlag(EligibilityFlag.CAN_ENROLL) && !result.hasFlag(EligibilityFlag.RECHECK_BEFORE_ENROLLMENT))
														iEnroll.click();
												}
												@Override
												public void onMessage(OnlineSectioningInterface.EligibilityCheck result) {
													if (result.hasMessage()) {
														iStatus.error(result.getMessage());
													} else if (result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.PIN_REQUIRED)) {
														iStatus.warning(MESSAGES.exceptionAuthenticationPinRequired());
													} else {
														clearMessage(false);
													}
												}
											};
											iPinDialog.checkEligibility(iOnline, iMode.isSectioning(), iSessionSelector.getAcademicSessionId(), null, callback);
										}
									}
									if (se.hasErrors()) iLastAssignment.setErrors(se.getErrors());
									if (se.hasSectionMessages()) {
										for (CourseAssignment ca: iLastAssignment.getCourseAssignments()) {
											for (ClassAssignment c: ca.getClassAssignments()) {
												c.setError(se.getSectionMessage(c.getClassId()));
											}
										}
										fillIn(iLastAssignment);
										iStatus.error(caught.getMessage() , false);
									}
								}
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG))
									checkSpecialRegistrationAfterFailedSubmitSchedule(lastEnrollment, false);
							}
						});
					}
				};
				enroll = confirmEnrollment(enroll);
				enroll.execute();
			}
		});
		
		iSubmitSpecReg.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				checkSpecialRegistrationAfterFailedSubmitSchedule(new ArrayList<ClassAssignmentInterface.ClassAssignment>(iLastResult), true);
			}
		});
		
		iGetSpecRegs.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.waitSpecialRegistration());
				iSectioningService.retrieveAllSpecialRequests(
					new RetrieveAllSpecialRegistrationsRequest(iSessionSelector.getAcademicSessionId(), iEligibilityCheck.getStudentId()),
					new AsyncCallback<List<RetrieveSpecialRegistrationResponse>>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iStatus.error(MESSAGES.retrieveAllSpecialRegistrationsFail(caught.getMessage()), caught);
							updateHistory();
						}

						@Override
						public void onSuccess(List<RetrieveSpecialRegistrationResponse> response) {
							LoadingWidget.getInstance().hide();
							if (!response.isEmpty())
								getSpecialRegistrationSelectionDialog().open(response);
							else
								iStatus.info(MESSAGES.failedNoSpecialRegistrations());
							updateHistory();
						}
					});
			}
		});
		
		iPrint.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				boolean allSaved = true;
				for (ClassAssignmentInterface.ClassAssignment clazz: iLastResult) {
					if (clazz != null && !clazz.isFreeTime() && !clazz.isTeachingAssignment() && !clazz.isSaved()) allSaved = false;
				}
				Widget w = iAssignments.getPrintWidget(0, 5, 15);
				w.setWidth("100%");
				ToolBox.print((allSaved && !isChanged() ? MESSAGES.studentSchedule() : MESSAGES.studentScheduleNotEnrolled()),
						(CONSTANTS.printReportShowUserName() ? iUserAuthentication.getUser() : ""),
						iSessionSelector.getAcademicSessionName(),
						iAssignmentGrid.getPrintWidget(900),
						w,
						iStatus);
			}
		});
		
		if (iExport != null)
			iExport.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					ToolBox.open(iCalendar.getUrl());
				}
			});
		
		if (iTrackHistory) {
			History.addValueChangeHandler(new ValueChangeHandler<String>() {
				public void onValueChange(ValueChangeEvent<String> event) {
					if (!event.getValue().isEmpty()) {
						int item = iHistory.size() - 1;
						try {
							item = Integer.parseInt(event.getValue());
						} catch (NumberFormatException e) {}
						if (item < 0) item = 0;
						if (item >= iHistory.size()) item = iHistory.size() - 1;
						if (item >= 0) iHistory.get(item).restore();
					} else if (isChanged()) {
						UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), iMode.isSectioning() ? MESSAGES.queryLeaveChangesOnClassSchedule() : MESSAGES.queryLeaveChangesOnCourseRequests(), new Command() {
							@Override
							public void execute() {
								iCourseRequests.clear();
								if (!iSchedule.isVisible()) prev();
							}
						});
					}
				}
			});
		}
		
		iSessionSelector.addAcademicSessionChangeHandler(new AcademicSessionSelector.AcademicSessionChangeHandler() {
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				addHistory();
			}
		});
		
		iSave.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addHistory();
				iCourseRequests.changeTip();
				clearMessage();
				iCourseRequests.validate(new AsyncCallback<Boolean>() {
					public void onSuccess(Boolean result) {
						if (result) {
							LoadingWidget.getInstance().show(MESSAGES.courseRequestsSaving());
							final CourseRequestInterface request = iCourseRequests.getRequest();
							iSectioningService.saveRequest(request, new AsyncCallback<CourseRequestInterface>() {
								public void onSuccess(final CourseRequestInterface result) {
									if (result != null) {
										if (iScheduleChanged) {
											iScheduleChanged = false;
											clearMessage();
										}
										iSavedRequest = result;
										iCourseRequests.setValue(result, false);
										iCourseRequests.notifySaveSucceeded();
										iStatus.done(MESSAGES.saveRequestsOK());
										UniTimeConfirmationDialog.confirm(MESSAGES.saveRequestsConfirmation(), RESOURCES.statusDone(), new Command() {
											@Override
											public void execute() {
												Scheduler.get().scheduleDeferred(new ScheduledCommand() {
													@Override
													public void execute() {
														printConfirmation(result);
													}
												});
											}
										});
									}
									LoadingWidget.getInstance().hide();
									updateHistory();
								}
								public void onFailure(Throwable caught) {
									iStatus.error(MESSAGES.saveRequestsFail(caught.getMessage()), caught);
									LoadingWidget.getInstance().hide();
									updateHistory();
								}
							});
						} else {
							if (iSavedRequest != null && !iSavedRequest.equals(iCourseRequests.getRequest())) {
								iScheduleChanged = true;
								iSave.addStyleName("unitime-EnrollButton");
								iStatus.warning(iSavedRequest.isEmpty() ? MESSAGES.warnRequestsEmptyOnCourseRequest() : MESSAGES.warnRequestsChangedOnCourseRequest(), false);
							}
							updateHistory();
						}
					}
					public void onFailure(Throwable caught) {
						if (caught != null) {
							iStatus.error(MESSAGES.validationFailedWithMessage(caught.getMessage()), caught);
						} else {
							String error = iCourseRequests.getFirstError();
							iStatus.error(error == null ? MESSAGES.validationFailed() : MESSAGES.validationFailedWithMessage(error));
						}
						if (iSavedRequest != null && !iSavedRequest.equals(iCourseRequests.getRequest())) {
							iScheduleChanged = true;
						}
						updateHistory();
					}
				});
			}
		});
		
		iShowUnassignments.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				Cookies.setCookie("UniTime:Unassignments", "1");
				fillIn(iLastAssignment);
			}
		});
	}
	
	public void openSuggestionsBox(int rowIndex) {
		if (iSuggestionsBox == null) {
			iSuggestionsBox = new SuggestionsBox(iAssignmentGrid.getColorProvider(), iOnline, iSpecRegCx);
			
			iSuggestionsBox.addCloseHandler(new CloseHandler<PopupPanel>() {
				public void onClose(CloseEvent<PopupPanel> event) {
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iAssignmentPanel.setFocus(true);
						}
					});
				}
			});
			
			iSuggestionsBox.addSuggestionSelectedHandler(new SuggestionsBox.SuggestionSelectedHandler() {
				public void onSuggestionSelected(SuggestionsBox.SuggestionSelectedEvent event) {
					ClassAssignmentInterface result = event.getSuggestion();
					clearMessage();
					fillIn(result);
					addHistory();
				}
			});

			iSuggestionsBox.addQuickDropHandler(new SuggestionsBox.QuickDropHandler() {
				@Override
				public void onQuickDrop(SuggestionsBox.QuickDropEvent event) {
					final CourseRequestInterface undo = iCourseRequests.getRequest();
					iCourseRequests.dropCourse(event.getAssignment());
					LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
					CourseRequestInterface r = iCourseRequests.getRequest(); r.setNoChange(true);
					iSectioningService.section(iOnline, r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iStatus.error(MESSAGES.exceptionSectioningFailed(caught.getMessage()), caught);
							iCourseRequests.setRequest(undo);
						}
						public void onSuccess(ClassAssignmentInterface result) {
							fillIn(result);
							addHistory();
						}
					});
				}
			});
		}
		iAssignments.setSelectedRow(rowIndex);
		clearMessage();
		iSuggestionsBox.open(iCourseRequests.getRequest(), iLastResult, rowIndex, iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.ALTERNATIVES_DROP), useDefaultConfirmDialog());
	}
	
	private void fillIn(ClassAssignmentInterface result) {
		iLastEnrollment = null;
		iLastResult.clear();
		iLastAssignment = result;
		String calendarUrl = GWT.getHostPageBaseURL() + "calendar?sid=" + iSessionSelector.getAcademicSessionId() + "&cid=";
		String ftParam = "&ft=";
		boolean hasError = false;
		float totalCredit = 0f;
		if (!result.getCourseAssignments().isEmpty() || CONSTANTS.allowEmptySchedule()) {
			ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
			iAssignmentGrid.clear(true);
			for (final ClassAssignmentInterface.CourseAssignment course: result.getCourseAssignments()) {
				if (course.isAssigned()) {
					boolean firstClazz = true;
					for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
						if (clazz.getClassId() != null)
							calendarUrl += clazz.getCourseId() + "-" + clazz.getClassId() + ",";
						else if (clazz.isFreeTime())
							ftParam += clazz.getDaysString(CONSTANTS.shortDays()) + "-" + clazz.getStart() + "-" + clazz.getLength() + ",";
						String style = (firstClazz && !rows.isEmpty() ? "top-border-dashed": "");
						WebTable.Row row = null;

						WebTable.IconsCell icons = new WebTable.IconsCell();
						if (clazz.isTeachingAssignment()) {
							if (clazz.isInstructing())
								icons.add(RESOURCES.isInstructing(), MESSAGES.instructing(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));
							style += (clazz.isInstructing() ? " text-steelblue" : " text-steelblue-italic");
						} else if (clazz.isSaved())
							icons.add(RESOURCES.saved(), MESSAGES.saved(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));
						else if (clazz.isDummy())
							icons.add(RESOURCES.unassignment(), MESSAGES.unassignment(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));
						else if (!clazz.isFreeTime() && result.isCanEnroll())
							icons.add(RESOURCES.assignment(), MESSAGES.assignment(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));
						SpecialRegistrationStatus specRegStatus = iSpecRegCx.getStatus(clazz);
						if (specRegStatus != null) {
							String error = iSpecRegCx.getError(clazz);
							switch (specRegStatus) {
							case Draft:
								icons.add(RESOURCES.specRegDraft(), (error != null ? error + "\n" : "") + MESSAGES.hintSpecRegDraft(), true);
								break;
							case Approved:
								icons.add(RESOURCES.specRegApproved(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegApproved(), true);
								break;
							case Cancelled:
								icons.add(RESOURCES.specRegCancelled(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegCancelled(), true);
								break;
							case Pending:
								icons.add(RESOURCES.specRegPending(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegPending(), true);
								break;
							case Rejected:
								icons.add(RESOURCES.specRegRejected(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegRejected(), true);
								break;
							}
							if (clazz.hasError() && specRegStatus != SpecialRegistrationStatus.Approved) {
								style += " text-red";
								hasError = true;
							}
						} else if (clazz.hasError()) {
							icons.add(RESOURCES.error(), clazz.getError());
							style += " text-red";
							hasError = true;
						}
						if (course.isLocked())
							icons.add(RESOURCES.courseLocked(), MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr()));
						if (clazz.isOfHighDemand())
							icons.add(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()));
						if (clazz != null && clazz.hasOverlapNote())
							icons.add(RESOURCES.overlap(), clazz.getOverlapNote());
						if (clazz.isCancelled())
							icons.add(RESOURCES.cancelled(), MESSAGES.classCancelled(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));

						if (!clazz.isTeachingAssignment())
							totalCredit += clazz.guessCreditCount();
						if (clazz.isAssigned()) {
							row = new WebTable.Row(
								clazz.isDummy() || clazz.isTeachingAssignment() ? new WebTable.Cell(null) : new WebTable.LockCell(clazz.isPinned(), course.isFreeTime() ? ARIA.freeTimePin(clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours())) : ARIA.classPin(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection())), MESSAGES.hintLocked(), MESSAGES.hintUnlocked()),
								new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject() : "").aria(firstClazz ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
								new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr(CONSTANTS.showCourseTitle()) : "").aria(firstClazz ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr(CONSTANTS.showCourseTitle())),
								new WebTable.Cell(clazz.getSubpart()),
								new WebTable.Cell(clazz.getSection()),
								new WebTable.Cell(clazz.getLimitString()),
								new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())).aria(clazz.getDaysString(CONSTANTS.longDays(), " ")),
								new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())).aria(clazz.getStartStringAria(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())).aria(clazz.getEndStringAria(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getDatePattern()),
								(clazz.hasDistanceConflict() ? new WebTable.RoomCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
								new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
								new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
								new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
								new WebTable.AbbvTextCell(clazz.getCredit()),
								icons);
						} else {
							row = new WebTable.Row(
									clazz.isDummy() || clazz.isTeachingAssignment() ? new WebTable.Cell(null) : new WebTable.LockCell(clazz.isPinned() , course.isFreeTime() ? ARIA.freeTimePin(clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours())) : ARIA.classPin(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection())), MESSAGES.hintLocked(), MESSAGES.hintUnlocked()),
									new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject() : ""),
									new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr(CONSTANTS.showCourseTitle()) : ""),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(MESSAGES.arrangeHours(), 3, null),
									new WebTable.Cell(clazz.hasDatePattern() ? clazz.getDatePattern() : ""),
									(clazz.hasDistanceConflict() ? new WebTable.RoomCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
									new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
									new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									new WebTable.AbbvTextCell(clazz.getCredit()),
									icons);
						}
						if (course.isFreeTime()) {
							row.setAriaLabel(ARIA.freeTimeAssignment(clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours())));
						} else if (clazz.isTeachingAssignment()) {
							row.setStyleName("teaching-assignment");
							if (clazz.isInstructing())
								row.setAriaLabel(ARIA.instructingAssignment(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
									clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours()));
							else
								row.setAriaLabel(ARIA.teachingAssignment(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
										clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours()));
						} else {
							row.setAriaLabel(ARIA.classAssignment(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
								clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours()));
						}
						final ArrayList<TimeGrid.Meeting> meetings = (clazz.isFreeTime() ? null : iAssignmentGrid.addClass(clazz, rows.size()));
						// row.setId(course.isFreeTime() ? "Free " + clazz.getDaysString() + " " +clazz.getStartString() + " - " + clazz.getEndString() : course.getCourseId() + ":" + clazz.getClassId());
						final int index = rows.size();
						if (!clazz.isDummy() && !clazz.isTeachingAssignment())
							((HasValueChangeHandlers<Boolean>)row.getCell(0).getWidget()).addValueChangeHandler(new ValueChangeHandler<Boolean>() {
								@Override
								public void onValueChange(ValueChangeEvent<Boolean> event) {
									if (meetings == null) {
										iLastResult.get(index).setPinned(event.getValue());
									} else {
										for (Meeting m: meetings) {
											m.setPinned(event.getValue());
											iLastResult.get(m.getIndex()).setPinned(event.getValue());
										}
									}
								}
							});
						rows.add(row);
						iLastResult.add(clazz.isDummy() || clazz.isTeachingAssignment() ? null : clazz);
						for (WebTable.Cell cell: row.getCells())
							cell.setStyleName(style);
						firstClazz = false;
					}
				} else {
					String style = "text-red" + (!rows.isEmpty() ? " top-border-dashed": "");
					WebTable.Row row = null;
					String unassignedMessage = MESSAGES.courseNotAssigned();
					if (course.hasEnrollmentMessage())
						unassignedMessage = course.getEnrollmentMessage();
					else if (course.getNote() != null)
						unassignedMessage = course.getNote();
					else if (course.getOverlaps()!=null && !course.getOverlaps().isEmpty()) {
						unassignedMessage = "";
						for (Iterator<String> i = course.getOverlaps().iterator(); i.hasNext();) {
							String x = i.next();
							if (unassignedMessage.isEmpty())
								unassignedMessage += MESSAGES.conflictWithFirst(x);
							else if (!i.hasNext())
								unassignedMessage += MESSAGES.conflictWithLast(x);
							else
								unassignedMessage += MESSAGES.conflictWithMiddle(x);
						}
						if (course.getInstead() != null)
							unassignedMessage += MESSAGES.conflictAssignedAlternative(course.getInstead());
						unassignedMessage += ".";
					} else if (course.isNotAvailable()) {
						if (course.isFull())
							unassignedMessage = MESSAGES.courseIsFull();
						else
							unassignedMessage = MESSAGES.classNotAvailable();
					} else if (course.isLocked()) {
						unassignedMessage = MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr());
					}
					
					WebTable.IconsCell icons = new WebTable.IconsCell();
					if (course.isLocked())
						icons.add(RESOURCES.courseLocked(), course.getNote() != null ? course.getNote() : MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr()));
					
					WebTable.CheckboxCell waitList = null;
					Boolean w = iCourseRequests.getWaitList(course.getCourseName());
					if (w != null) {
						waitList = new WebTable.CheckboxCell(w, MESSAGES.toggleWaitList(), ARIA.titleRequestedWaitListForCourse(MESSAGES.course(course.getSubject(), course.getCourseNbr())));
						waitList.getWidget().setStyleName("toggle");
						((CheckBox)waitList.getWidget()).addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								clearMessage();
								iCourseRequests.setWaitList(course.getCourseName(), event.getValue());
								LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
								CourseRequestInterface r = iCourseRequests.getRequest(); r.setNoChange(true);
								iSectioningService.section(iOnline, r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
									public void onFailure(Throwable caught) {
										iStatus.error(MESSAGES.exceptionSectioningFailed(caught.getMessage()), caught);
										LoadingWidget.getInstance().hide();
										updateHistory();
									}
									public void onSuccess(ClassAssignmentInterface result) {
										fillIn(result);
										addHistory();
									}
								});
							}
						});
					}

					for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
						if (clazz.isAssigned()) {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr(CONSTANTS.showCourseTitle())),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())).aria(clazz.getDaysString(CONSTANTS.longDays(), " ")),
									new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())).aria(clazz.getStartStringAria(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())).aria(clazz.getEndStringAria(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getDatePattern()),
									new WebTable.Cell(unassignedMessage, 3, null),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									(waitList != null ? waitList : new WebTable.AbbvTextCell(clazz.getCredit())),
									icons);
							if (course.isFreeTime())
								row.setAriaLabel(ARIA.freeTimeUnassignment(clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()), unassignedMessage));
							else
								row.setAriaLabel(ARIA.courseUnassginment(MESSAGES.course(course.getSubject(), course.getCourseNbr()), unassignedMessage));
						} else if (clazz.getClassId() != null) {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr(CONSTANTS.showCourseTitle())),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(MESSAGES.arrangeHours(), 3, null),
									new WebTable.Cell(clazz.hasDatePattern() ? clazz.getDatePattern() : ""),
									new WebTable.Cell(unassignedMessage, 3, null),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									(waitList != null ? waitList : new WebTable.AbbvTextCell(clazz.getCredit())),
									icons);
							if (course.isFreeTime())
								row.setAriaLabel(ARIA.freeTimeUnassignment("", unassignedMessage));
							else
								row.setAriaLabel(ARIA.courseUnassginment(MESSAGES.course(course.getSubject(), course.getCourseNbr()), unassignedMessage));
						} else {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr(CONSTANTS.showCourseTitle())),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(unassignedMessage, 7, null),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									(waitList != null ? waitList : new WebTable.AbbvTextCell(clazz.getCredit())),
									icons);
							if (course.isFreeTime())
								row.setAriaLabel(ARIA.freeTimeUnassignment("", unassignedMessage));
							else
								row.setAriaLabel(ARIA.courseUnassginment(MESSAGES.course(course.getSubject(), course.getCourseNbr()), unassignedMessage));
						}
						row.setId(course.isFreeTime() ? CONSTANTS.freePrefix() + clazz.getDaysString(CONSTANTS.shortDays()) + " " +clazz.getStartString(CONSTANTS.useAmPm()) + " - " + clazz.getEndString(CONSTANTS.useAmPm()) : course.getCourseId() + ":" + clazz.getClassId());
						iLastResult.add(clazz.isDummy() || clazz.isTeachingAssignment() ? null : clazz);
						break;
					}
					if (row == null) {
						if (waitList != null) {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.getSubject()),
									new WebTable.Cell(course.getCourseNbr(CONSTANTS.showCourseTitle())),
									new WebTable.Cell(unassignedMessage, 11, null),
									waitList,
									icons);
						} else {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.getSubject()),
									new WebTable.Cell(course.getCourseNbr(CONSTANTS.showCourseTitle())),
									new WebTable.Cell(unassignedMessage, 12, null),
									icons);
						}
						row.setId(course.getCourseId().toString());
						row.setAriaLabel(ARIA.courseUnassginment(MESSAGES.course(course.getSubject(), course.getCourseNbr()), unassignedMessage));
						iLastResult.add(course.addClassAssignment());
					}
					for (WebTable.Cell cell: row.getCells())
						cell.setStyleName(style);
					row.getCell(row.getNrCells() - 1).setStyleName("text-red-centered" + (!rows.isEmpty() ? " top-border-dashed": ""));
					rows.add(row);
				}
				if (iSavedAssignment != null && !course.isFreeTime() && !course.isTeachingAssignment() && iShowUnassignments.getValue()) {
					for (ClassAssignmentInterface.CourseAssignment saved: iSavedAssignment.getCourseAssignments()) {
						if (!saved.isAssigned() || saved.isFreeTime() || saved.isTeachingAssignment() || !course.getCourseId().equals(saved.getCourseId())) continue;
						classes: for (ClassAssignmentInterface.ClassAssignment clazz: saved.getClassAssignments()) {
							for (ClassAssignmentInterface.ClassAssignment x: course.getClassAssignments())
								if (clazz.getClassId().equals(x.getClassId())) continue classes;
							String style = "text-gray";
							WebTable.Row row = null;
							
							WebTable.IconsCell icons = new WebTable.IconsCell();
							SpecialRegistrationStatus specRegStatus = iSpecRegCx.getStatus(clazz);
							if (specRegStatus != null) {
								String error = iSpecRegCx.getError(clazz);
								switch (specRegStatus) {
								case Draft:
									icons.add(RESOURCES.specRegDraft(), (error != null ? error + "\n" : "") + MESSAGES.hintSpecRegDraft(), true);
									break;
								case Approved:
									icons.add(RESOURCES.specRegApproved(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegApproved(), true);
									break;
								case Cancelled:
									icons.add(RESOURCES.specRegCancelled(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegCancelled(), true);
									break;
								case Pending:
									icons.add(RESOURCES.specRegPending(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegPending(), true);
									break;
								case Rejected:
									icons.add(RESOURCES.specRegRejected(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegRejected(), true);
									break;
								}
							} else if (clazz.hasError()) {
								icons.add(RESOURCES.error(), clazz.getError());
							}
							if (clazz.isSaved())
								icons.add(RESOURCES.unassignment(), MESSAGES.unassignment(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));
							if (clazz.isOfHighDemand())
								icons.add(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()));
							if (clazz.isCancelled())
								icons.add(RESOURCES.cancelled(), MESSAGES.classCancelled(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));
							
							if (clazz.isAssigned()) {
								row = new WebTable.Row(
										new WebTable.Cell(null),
										new WebTable.Cell("").aria(course.getSubject()),
										new WebTable.Cell("").aria(course.getCourseNbr(CONSTANTS.showCourseTitle())),
										new WebTable.Cell(clazz.getSubpart()),
										new WebTable.Cell(clazz.getSection()),
										new WebTable.Cell(clazz.getLimitString()),
										new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())).aria(clazz.getDaysString(CONSTANTS.longDays(), " ")),
										new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())).aria(clazz.getStartStringAria(CONSTANTS.useAmPm())),
										new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())).aria(clazz.getEndStringAria(CONSTANTS.useAmPm())),
										new WebTable.Cell(clazz.getDatePattern()),
										(clazz.hasDistanceConflict() ? new WebTable.RoomCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
										new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
										new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
										new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
										new WebTable.AbbvTextCell(clazz.getCredit()),
										icons);								
							} else {
								row = new WebTable.Row(
										new WebTable.Cell(null),
										new WebTable.Cell("").aria(course.getSubject()),
										new WebTable.Cell("").aria(course.getCourseNbr(CONSTANTS.showCourseTitle())),
										new WebTable.Cell(clazz.getSubpart()),
										new WebTable.Cell(clazz.getSection()),
										new WebTable.Cell(clazz.getLimitString()),
										new WebTable.Cell(MESSAGES.arrangeHours(), 3, null),
										new WebTable.Cell(clazz.hasDatePattern() ? clazz.getDatePattern() : ""),
										(clazz.hasDistanceConflict() ? new WebTable.RoomCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
										new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
										new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
										new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
										new WebTable.AbbvTextCell(clazz.getCredit()),
										icons);
							}
							row.setAriaLabel(ARIA.previousAssignment(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
									clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours()));
							rows.add(row);
							row.setSelectable(false);
							iLastResult.add(null);
							for (WebTable.Cell cell: row.getCells())
								cell.setStyleName(style);
						}
					}
				}
			}
			if (iSavedAssignment != null && iShowUnassignments.getValue()) {
				courses: for (ClassAssignmentInterface.CourseAssignment course: iSavedAssignment.getCourseAssignments()) {
					if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
					for (ClassAssignmentInterface.CourseAssignment x: result.getCourseAssignments())
						if (course.getCourseId().equals(x.getCourseId())) continue courses;
					boolean firstClazz = true;
					for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
						String style = "text-gray" + (firstClazz && !rows.isEmpty() ? " top-border-dashed": "");
						WebTable.Row row = null;
						
						WebTable.IconsCell icons = new WebTable.IconsCell();
						SpecialRegistrationStatus specRegStatus = iSpecRegCx.getStatus(clazz);
						if (specRegStatus != null) {
							String error = iSpecRegCx.getError(clazz);
							switch (specRegStatus) {
							case Draft:
								icons.add(RESOURCES.specRegDraft(), (error != null ? error + "\n" : "") + MESSAGES.hintSpecRegDraft(), true);
								break;
							case Approved:
								icons.add(RESOURCES.specRegApproved(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegApproved(), true);
								break;
							case Cancelled:
								icons.add(RESOURCES.specRegCancelled(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegCancelled(), true);
								break;
							case Pending:
								icons.add(RESOURCES.specRegPending(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegPending(), true);
								break;
							case Rejected:
								icons.add(RESOURCES.specRegRejected(), (error != null ? error + "\n" : "") + "\n" + MESSAGES.hintSpecRegRejected(), true);
								break;
							}
						} else if (clazz.hasError()) {
							icons.add(RESOURCES.error(), clazz.getError());
						}
						if (clazz.isSaved())
							icons.add(RESOURCES.unassignment(), MESSAGES.unassignment(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));
						if (clazz.isOfHighDemand())
							icons.add(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()));
						if (clazz.isCancelled())
							icons.add(RESOURCES.cancelled(), MESSAGES.classCancelled(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));

						if (clazz.isAssigned()) {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(firstClazz ? course.getSubject() : "").aria(firstClazz ? "" : course.getSubject()),
									new WebTable.Cell(firstClazz ? course.getCourseNbr(CONSTANTS.showCourseTitle()) : "").aria(firstClazz ? "" : course.getCourseNbr(CONSTANTS.showCourseTitle())),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())).aria(clazz.getDaysString(CONSTANTS.longDays(), " ")),
									new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())).aria(clazz.getStartStringAria(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())).aria(clazz.getEndStringAria(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getDatePattern()),
									(clazz.hasDistanceConflict() ? new WebTable.RoomCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
									new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
									new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									new WebTable.AbbvTextCell(clazz.getCredit()),
									icons);
						} else {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(firstClazz ? course.getSubject() : "").aria(firstClazz ? "" : course.getSubject()),
									new WebTable.Cell(firstClazz ? course.getCourseNbr(CONSTANTS.showCourseTitle()) : "").aria(firstClazz ? "" : course.getCourseNbr(CONSTANTS.showCourseTitle())),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(MESSAGES.arrangeHours(), 3, null),
									new WebTable.Cell(clazz.hasDatePattern() ? clazz.getDatePattern() : ""),
									(clazz.hasDistanceConflict() ? new WebTable.RoomCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
									new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
									new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									new WebTable.AbbvTextCell(clazz.getCredit()),
									icons);
						}
						row.setAriaLabel(ARIA.previousAssignment(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
								clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours()));
						rows.add(row);
						row.setSelectable(false);
						iLastResult.add(null);
						for (WebTable.Cell cell: row.getCells())
							cell.setStyleName(style);
						firstClazz = false;
					}
				}
			}
			for (ClassAssignmentInterface.CourseAssignment course: result.getCourseAssignments()) {
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					if (clazz.isFreeTime()) {
						CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
						ft.setLength(clazz.getLength());
						ft.setStart(clazz.getStart());
						for (int d: clazz.getDays()) ft.addDay(d);
						iAssignmentGrid.addFreeTime(ft);
					}
				}
			}

			WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
			int idx = 0;
			for (WebTable.Row row: rows) rowArray[idx++] = row;
			iAssignmentGrid.shrink();
			// iAssignmentPanel.setWidth(iAssignmentGrid.getWidth() + "px");
			if (hasNotAssignedClass(result)) {
				iGridMessage.setText(MESSAGES.timeGridNotAssignedTimes());
				iGridMessage.setVisible(true);
			} else {
				iGridMessage.setVisible(false);
			}
			iAssignments.setData(rowArray);
			if (LoadingWidget.getInstance().isShowing())
				LoadingWidget.getInstance().hide();
			iPanel.remove(iCourseRequests);
			iPanel.insert(iAssignmentDock, 0);
			iRequests.setVisible(true); iRequests.setEnabled(true);
			if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_RESET)) { iReset.setVisible(true); iReset.setEnabled(true); }
			if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.QUICK_ADD_DROP)) { iQuickAdd.setVisible(true); iQuickAdd.setEnabled(true); }
			iEnroll.setVisible(result.isCanEnroll() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL));
			iSubmitSpecReg.setVisible(!iEnroll.isVisible() && iSpecRegCx.isCanSubmit() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG));
			iSubmitSpecReg.setEnabled(!iEnroll.isVisible() && iSpecRegCx.isCanSubmit() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG));
			iGetSpecRegs.setVisible(iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.HAS_SPECREG));
			iGetSpecRegs.setEnabled(iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.HAS_SPECREG));
			if (iEligibilityCheck != null && iEligibilityCheck.hasCheckboxMessage()) {
				if (iCustomCheckbox == null) {
					iCustomCheckbox = new CheckBox(iEligibilityCheck.getCheckboxMessage(), true);
					((HorizontalPanel)iFooter.getWidget(1)).insert(iCustomCheckbox, 0);
					((HorizontalPanel)iFooter.getWidget(1)).setCellVerticalAlignment(iCustomCheckbox, HasVerticalAlignment.ALIGN_MIDDLE);
					iCustomCheckbox.getElement().getStyle().setPaddingRight(10, Unit.PX);
					iCustomCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							iEnroll.setEnabled(event.getValue());
						}
					});
				}
				iEnroll.setEnabled(iCustomCheckbox.getValue());
				iCustomCheckbox.setEnabled(true);
				iCustomCheckbox.setVisible(true);
			} else {
				iEnroll.setEnabled(result.isCanEnroll() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL));
			}
			iPrint.setVisible(true); iPrint.setEnabled(true);
			iStartOver.setVisible(iSavedAssignment != null);
			iStartOver.setEnabled(iSavedAssignment != null);
			if (iExport != null) {
				iExport.setVisible(true); iExport.setEnabled(true);
			}
			iSchedule.setVisible(false); iSchedule.setEnabled(false);
			iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
			iAssignmentGrid.scrollDown();
			if (iCalendar != null) {
				if (calendarUrl.endsWith(",")) calendarUrl = calendarUrl.substring(0, calendarUrl.length() - 1);
				calendarUrl += ftParam;
				if (calendarUrl.endsWith(",")) calendarUrl = calendarUrl.substring(0, calendarUrl.length() - 1);
				iAssignmentGrid.setCalendarUrl(calendarUrl);
				iCalendar.setUrl(calendarUrl);
			}
			ResizeEvent.fire(this, getOffsetWidth(), getOffsetHeight());
			if (iAssignmentTab.getSelectedTab() == 0) {
				AriaStatus.getInstance().setHTML(ARIA.listOfClasses());
			} else {
				AriaStatus.getInstance().setHTML(ARIA.timetable());
			}
			if (result.hasMessages()) {
				if (hasError) {
					iStatus.error(result.getMessages("<br>"));
				} else { 
					iStatus.warning(result.getMessages("<br>"));
				}
			} else {
				updateScheduleChangedNoteIfNeeded();
			}
			iTotalCredit.setVisible(totalCredit > 0f);
			iTotalCredit.setText(MESSAGES.totalCredit(totalCredit));
		} else {
			iTotalCredit.setVisible(false);
			iStatus.error(MESSAGES.noSchedule());
			if (LoadingWidget.getInstance().isShowing())
				LoadingWidget.getInstance().hide();
		}
	}
	
	public void prev() {
		iPanel.remove(iAssignmentDock);
		iPanel.insert(iCourseRequests, 0);
		iRequests.setVisible(false); iRequests.setEnabled(false);
		iReset.setVisible(false); iReset.setEnabled(false);
		iQuickAdd.setVisible(false); iQuickAdd.setEnabled(false);
		iEnroll.setVisible(false); iEnroll.setEnabled(false);
		iGetSpecRegs.setVisible(false); iGetSpecRegs.setEnabled(false);
		iSubmitSpecReg.setVisible(false); iSubmitSpecReg.setEnabled(false);
		if (iCustomCheckbox != null) {
			iCustomCheckbox.setVisible(false); iCustomCheckbox.setEnabled(false);
		}
		iPrint.setVisible(false); iPrint.setEnabled(false);
		if (iExport != null) {
			iExport.setVisible(false); iExport.setEnabled(false);
		}
		iSchedule.setVisible(iMode.isSectioning()); iSchedule.setEnabled(iMode.isSectioning());
		iSave.setVisible(!iMode.isSectioning()); iSave.setEnabled(!iMode.isSectioning() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_REGISTER));
		if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.DEGREE_PLANS)) {
			iDegreePlan.setVisible(true); iDegreePlan.setEnabled(true);
		}
		clearMessage();
		ResizeEvent.fire(this, getOffsetWidth(), getOffsetHeight());
		AriaStatus.getInstance().setHTML(ARIA.courseRequests());
		updateScheduleChangedNoteIfNeeded();
	}
	
	public void clear(boolean switchToRequests) {
		if (iShowUnassignments != null)
			iShowUnassignments.setVisible(false);
		iSavedAssignment = null; iLastAssignment = null; iSpecialRegAssignment = null;
		iCourseRequests.clear();
		iLastResult.clear();
		iAssignments.clearData(true);
		iAssignmentGrid.clear(true);
		iShowUnassignments.setVisible(true);
		if (iRequests.isVisible() && switchToRequests) {
			prev();
		}
	}
	
	public void clear() {
		clear(true);
	}
	
	public void checkEligibility(final Long sessionId, final Long studentId, final boolean saved, final AsyncCallback<OnlineSectioningInterface.EligibilityCheck> ret) {
		LoadingWidget.getInstance().show(MESSAGES.courseRequestsLoading());
		iStartOver.setVisible(false); iStartOver.setEnabled(false);
		iSpecRegCx.reset();
		iSectioningService.checkEligibility(iOnline, iMode.isSectioning(), sessionId, studentId, null, new AsyncCallback<OnlineSectioningInterface.EligibilityCheck>() {
			@Override
			public void onSuccess(OnlineSectioningInterface.EligibilityCheck result) {
				clearMessage(false);
				iEligibilityCheck = result;
				iSpecRegCx.update(result);
				iCourseRequests.setCanWaitList(result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_WAITLIST));
				iCourseRequests.setArrowsVisible(!result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.NO_REQUEST_ARROWS));
				if (result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_USE_ASSISTANT)) {
					if (result.hasMessage() && (iMode.isSectioning() && !result.hasFlag(EligibilityFlag.CAN_ENROLL))) {
						iStatus.error(iEligibilityCheck.getMessage());
					} else if (result.hasMessage() && (!iMode.isSectioning() && !result.hasFlag(EligibilityFlag.CAN_REGISTER))) {
						iStatus.error(iEligibilityCheck.getMessage());
					} else if (result.hasMessage()) {
						iStatus.warning(result.getMessage());
					} else {
						clearMessage();
					}
					if (result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.PIN_REQUIRED)) {
						if (iPinDialog == null) iPinDialog = new PinDialog();
						LoadingWidget.getInstance().hide();
						PinDialog.PinCallback callback = new PinDialog.PinCallback() {
							@Override
							public void onFailure(Throwable caught) {
								iStatus.error(MESSAGES.exceptionFailedEligibilityCheck(caught.getMessage()), caught);
								iSchedule.setVisible(iMode.isSectioning()); iSchedule.setEnabled(iMode.isSectioning());
								iSave.setVisible(!iMode.isSectioning()); iSave.setEnabled(!iMode.isSectioning() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_REGISTER));
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.DEGREE_PLANS)) {
									iDegreePlan.setVisible(true); iDegreePlan.setEnabled(true);
								}
								lastRequest(sessionId, studentId, saved, true);
								if (ret != null) ret.onSuccess(iEligibilityCheck);
							}
							@Override
							public void onSuccess(OnlineSectioningInterface.EligibilityCheck result) {
								iCourseRequests.setCanWaitList(result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_WAITLIST));
								iCourseRequests.setArrowsVisible(!result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.NO_REQUEST_ARROWS));
								iEligibilityCheck = result;
								iSpecRegCx.update(result);
								iSchedule.setVisible(iMode.isSectioning()); iSchedule.setEnabled(iMode.isSectioning());
								iSave.setVisible(!iMode.isSectioning()); iSave.setEnabled(!iMode.isSectioning() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_REGISTER));
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.DEGREE_PLANS)) {
									iDegreePlan.setVisible(true); iDegreePlan.setEnabled(true);
								}
								lastRequest(sessionId, studentId, saved, true);
								if (ret != null) ret.onSuccess(iEligibilityCheck);
							}
							@Override
							public void onMessage(OnlineSectioningInterface.EligibilityCheck result) {
								if (result.hasMessage()) {
									iStatus.error(result.getMessage());
								} else if (result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.PIN_REQUIRED)) {
									iStatus.warning(MESSAGES.exceptionAuthenticationPinRequired());
								} else {
									clearMessage(false);
								}
							}
						};
						iPinDialog.checkEligibility(iOnline, iMode.isSectioning(), sessionId, null, callback);
					} else {
						iSchedule.setVisible(iMode.isSectioning()); iSchedule.setEnabled(iMode.isSectioning());
						iSave.setVisible(!iMode.isSectioning()); iSave.setEnabled(!iMode.isSectioning() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_REGISTER));
						if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.DEGREE_PLANS)) {
							iDegreePlan.setVisible(true); iDegreePlan.setEnabled(true);
						}
						lastRequest(sessionId, studentId, saved, true);
						if (ret != null) ret.onSuccess(iEligibilityCheck);
					}
				} else {
					iCourseRequests.setCanWaitList(false);
					LoadingWidget.getInstance().hide();
					if (result.hasMessage()) {
						iStatus.error(result.getMessage());
					}
					iSchedule.setVisible(false);  iSchedule.setEnabled(false);
					iSave.setVisible(false); iSave.setEnabled(false);
					iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
					if (result.hasMessage()) {
						iStatus.warning(result.getMessage());
					} else {
						clearMessage();
					}
					if (ret != null) ret.onFailure(new SectioningException(result.getMessage()));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iEligibilityCheck = null;
				if (ret != null) ret.onFailure(caught);
				iStatus.error(MESSAGES.exceptionFailedEligibilityCheck(caught.getMessage()), caught);
			}
		});
	}
	
	private void lastResult(final CourseRequestInterface request, final Long sessionId, final Long studentId, boolean saved, final boolean changeViewIfNeeded) {
		AsyncCallback<ClassAssignmentInterface> callback = new AsyncCallback<ClassAssignmentInterface>() {
			public void onFailure(Throwable caught) {
				iStatus.error(caught.getMessage(), caught);
				LoadingWidget.getInstance().hide();
			}
			public void onSuccess(final ClassAssignmentInterface saved) {
				iSavedAssignment = saved;
				iSpecialRegAssignment = null;
				iShowUnassignments.setVisible(true);
				if (iSpecRegCx.hasRequestKey() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG)) {
					iSectioningService.retrieveSpecialRequest(new RetrieveSpecialRegistrationRequest(sessionId, studentId, iSpecRegCx.getRequestKey()), new AsyncCallback<RetrieveSpecialRegistrationResponse>() {
						@Override
						public void onFailure(Throwable caught) {
							iSpecRegCx.update(iEligibilityCheck);
							iSpecRegCx.setSpecRegRequestKeyValid(false);
							iSpecRegCx.setStatus(null);
							iSpecRegCx.setNote(null);
							iSpecRegCx.clearChanges();
							fillIn(saved);
							updateHistory();
							iStatus.error(MESSAGES.requestSpecialRegistrationFail(caught.getMessage()), caught);
						}
						@Override
						public void onSuccess(RetrieveSpecialRegistrationResponse specReg) {
							iSpecRegCx.setSpecRegRequestKeyValid(true);
							iSpecRegCx.setRequestId(specReg.getRequestId());
							iSpecRegCx.setNote(specReg.getNote());
							iSpecRegCx.setStatus(specReg.getStatus());
							iSpecRegCx.setChanges(specReg);
							if (specReg.hasChanges()) {
								final CourseRequestInterface courseRequests = iCourseRequests.getRequest();
								courseRequests.setTimeConflictsAllowed(specReg.hasTimeConflict()); courseRequests.setSpaceConflictsAllowed(specReg.hasSpaceConflict());
								for (ClassAssignmentInterface.ClassAssignment ch: specReg.getChanges()) {
									if (ch.isCourseAssigned()) courseRequests.addCourse(new RequestedCourse(ch.getCourseId(), CONSTANTS.showCourseTitle() ? ch.getCourseNameWithTitle() : ch.getCourseName()));
								}
								LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
								iSectioningService.section(iOnline, courseRequests, iLastResult, specReg.getChanges(), new AsyncCallback<ClassAssignmentInterface>() {
									public void onSuccess(ClassAssignmentInterface result) {
										fillIn(result);
										iSpecialRegAssignment = iLastAssignment;
										iCourseRequests.setRequest(courseRequests);
										addHistory();
									}
									public void onFailure(Throwable caught) {
										iStatus.error(MESSAGES.exceptionSectioningFailed(caught.getMessage()), caught);
										LoadingWidget.getInstance().hide();
									}
								});
							} else {
								fillIn(saved);
								updateHistory();
							}
						}
					});
				} else if (request.isSaved() || !CONSTANTS.checkLastResult()) {
					if ((saved.isEnrolled() && (changeViewIfNeeded || CONSTANTS.startOverCanChangeView())) || iRequests.isVisible()) {
						fillIn(saved);
						updateHistory();
					} else {
						iLastAssignment = saved;
						iLastResult = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
						for (final ClassAssignmentInterface.CourseAssignment course: saved.getCourseAssignments()) {
							if (course.isAssigned())
								for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
									iLastResult.add(clazz.isDummy() ? null : clazz);
								}
						}
						iStartOver.setVisible(true);
						iStartOver.setEnabled(true);
						updateScheduleChangedNoteIfNeeded();
						LoadingWidget.getInstance().hide();
					}
				} else {
					iCourseRequests.validate(new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
						}
						@Override
						public void onSuccess(Boolean result) {
							if (result) {
								ArrayList<ClassAssignmentInterface.ClassAssignment> classes = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
								for (ClassAssignmentInterface.CourseAssignment course: saved.getCourseAssignments())
									classes.addAll(course.getClassAssignments());
								iSectioningService.section(iOnline, request, classes, new AsyncCallback<ClassAssignmentInterface>() {
									public void onFailure(Throwable caught) {
										LoadingWidget.getInstance().hide();
									}
									public void onSuccess(ClassAssignmentInterface result) {
										fillIn(result);
										updateHistory();
									}
								});
							} else {
								LoadingWidget.getInstance().hide();
							}
						}
					});
				}
			}
		};
		if (saved)
			iSectioningService.savedResult(iOnline, request.getAcademicSessionId(), request.getStudentId(), callback);
		else
			iSectioningService.lastResult(iOnline, request.getAcademicSessionId(), callback);
	}
	
	public void lastRequest(final Long sessionId, final Long studentId, final boolean saved, final boolean changeViewIfNeeded) {
		if (!LoadingWidget.getInstance().isShowing())
			LoadingWidget.getInstance().show(MESSAGES.courseRequestsLoading());
		
		AsyncCallback<CourseRequestInterface> callback =  new AsyncCallback<CourseRequestInterface>() {
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				clear(changeViewIfNeeded);
				if (!MESSAGES.exceptionNoStudent().equals(caught.getMessage()) || iEligibilityCheck == null || !iEligibilityCheck.hasFlag(EligibilityFlag.IS_ADMIN, EligibilityFlag.IS_ADVISOR))
					iStatus.error(caught.getMessage(), caught);
			}
			public void onSuccess(final CourseRequestInterface request) {
				if (request.isSaved())
					iSavedRequest = request;
				else if (!iMode.isSectioning() && iSavedRequest == null) {
					iSectioningService.savedRequest(iOnline, iMode.isSectioning(), sessionId, studentId, new AsyncCallback<CourseRequestInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							iStatus.error(caught.getMessage(), caught);
						}
						@Override
						public void onSuccess(CourseRequestInterface savedRequest) {
							iSavedRequest = savedRequest;
						}
					});
				}
				clear(changeViewIfNeeded);
				/*
				if (request.isSaved() && request.getCourses().isEmpty()) {
					LoadingWidget.getInstance().hide();
					return;
				}*/
				iCourseRequests.setRequest(request);
				updateHistory();
				if (iSchedule.isVisible() || iRequests.isVisible()) {
					lastResult(request, sessionId, studentId, saved, changeViewIfNeeded);
				} else {
					LoadingWidget.getInstance().hide();
					iStartOver.setVisible(true);
					iStartOver.setEnabled(true);
					if (request.hasErrorMessage() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL, EligibilityFlag.CAN_REGISTER))
						if (iEligibilityCheck.hasMessage())
							iStatus.warning(iEligibilityCheck.getMessage() + "\n" + request.getErrorMessaeg());
						else
							iStatus.warning(request.getErrorMessaeg());
				}
			}
		};
		
		if (saved)
			iSectioningService.savedRequest(iOnline, iMode.isSectioning(), sessionId, studentId, callback);
		else
			iSectioningService.lastRequest(iOnline, iMode.isSectioning(), sessionId, callback);
	}

	public void showSuggestionsAsync(final int rowIndex) {
		if (rowIndex < 0) return;
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				openSuggestionsBox(rowIndex);
			}
			public void onFailure(Throwable caught) {
				iStatus.error(MESSAGES.exceptionSuggestionsFailed(caught.getMessage()), caught);
			}
		});
	}
	
	public class HistoryItem {
		private CourseRequestInterface iRequest;
		private ClassAssignmentInterface iAssignment;
		private boolean iFirstPage;
		private Long iSessionId;
		private String iUser;
		private String iMessage = null;
		private ScheduleStatus.Level iMessageLevel = null;
		private int iTab = 0;
		private SpecialRegistrationContext iSRCx;
		private ClassAssignmentInterface iSRassignment = null;
		
		private HistoryItem() {
			iRequest = iCourseRequests.getRequest();
			iAssignment = iLastAssignment;
			iFirstPage = iSchedule.isVisible();
			iSessionId = iSessionSelector.getAcademicSessionId();
			iUser = iUserAuthentication.getUser();
			iMessage = iStatus.getMessage(); iMessageLevel = iStatus.getLevel();
			iTab = iAssignmentTab.getSelectedTab();
			iSRCx = new SpecialRegistrationContext(iSpecRegCx);
			iSRassignment = iSpecialRegAssignment;
		}
		
		public void restore() {
			if (isChanged() && ((iUser != null && !iUser.equals(iUserAuthentication.getUser())) || (iSessionId != null && !iSessionId.equals(iSessionSelector.getAcademicSessionId())))) {
				UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), iMode.isSectioning() ? MESSAGES.queryLeaveChangesOnClassSchedule() : MESSAGES.queryLeaveChangesOnCourseRequests(), new Command() {
					@Override
					public void execute() {
						doRestore();
					}
				});
			} else {
				doRestore();
			}
		}

		protected void doRestore() {
			iInRestore = true;
			iUserAuthentication.setUser(iUser, new AsyncCallback<Boolean>() {
				public void onSuccess(Boolean result) {
					if (result) {
						iSessionSelector.selectSession(iSessionId, new AsyncCallback<Boolean>() {
							public void onSuccess(Boolean result) {
								if (result) {
									iSpecRegCx.copy(iSRCx);
									iSpecialRegAssignment = iSRassignment;
									iCourseRequests.setRequest(iRequest);
									if (iTab != iAssignmentTab.getSelectedTab())
										iAssignmentTab.selectTab(iTab);
									if (iFirstPage) {
										if (!iSchedule.isVisible()) prev();
										iCourseRequests.changeTip();
									} else {
										if (iAssignment != null) fillIn(iAssignment);
									}
									iStatus.setMessage(iMessageLevel, iMessage);
									if (!iMode.isSectioning() && iSavedRequest != null && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_REGISTER) && !iSavedRequest.equals(iCourseRequests.getRequest())) {
										iScheduleChanged = true;
										iSave.addStyleName("unitime-EnrollButton");
										iStatus.warning(iSavedRequest.isEmpty() ? MESSAGES.warnRequestsEmptyOnCourseRequest() : MESSAGES.warnRequestsChangedOnCourseRequest(), false);
									}
								}
								iInRestore = false;
								ResizeEvent.fire(StudentSectioningWidget.this, getOffsetWidth(), getOffsetHeight());
							}
							public void onFailure(Throwable reason) {
								iInRestore = false;
							}
						});
					} else {
						iInRestore = false;
					}
				}
				public void onFailure(Throwable reason) {
					iInRestore = false;
				}
			});
		}
	}
	
	public void setData(CourseRequestInterface request, ClassAssignmentInterface response) {
		clear();
		iCourseRequests.setRequest(request);
		if (response != null) {
			if (request.isSaved()) {
				iSavedAssignment = response;
				iShowUnassignments.setVisible(true);
			}
			fillIn(response);
		}
	}

	@Override
	public HandlerRegistration addResizeHandler(ResizeHandler handler) {
		return addHandler(handler, ResizeEvent.getType());
	}
	
	public void updateScheduleChangedNoteIfNeeded() {
		if (iScheduleChanged) {
			iScheduleChanged = false;
			clearMessage();
		}
		if (iLastAssignment == null || !iLastAssignment.isCanEnroll() || iEligibilityCheck == null || !iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL)) {
			if (iLastAssignment != null && (iSpecialRegAssignment != null || iSavedAssignment != null) && iSpecRegCx.isCanSubmit() && iSubmitSpecReg.isVisible()) {
				for (ClassAssignmentInterface.CourseAssignment course: iLastAssignment.getCourseAssignments()) {
					if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
					classes: for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
						for (ClassAssignmentInterface.CourseAssignment saved: (iSpecialRegAssignment == null ? iSavedAssignment : iSpecialRegAssignment).getCourseAssignments()) {
							if (!saved.isAssigned() || saved.isFreeTime() || saved.isTeachingAssignment() || !course.getCourseId().equals(saved.getCourseId())) continue;
							for (ClassAssignmentInterface.ClassAssignment x: saved.getClassAssignments()) {
								if (clazz.getClassId().equals(x.getClassId())) continue classes;
							}
						}
						//if (!clazz.isSaved() && !clazz.hasError()) {
						iScheduleChanged = true;
						iSubmitSpecReg.addStyleName("unitime-EnrollButton");
						iStatus.warning(MESSAGES.warnSpecialRegistrationChanged(), false);
						//}
					}
					for (ClassAssignmentInterface.CourseAssignment saved: (iSpecialRegAssignment == null ? iSavedAssignment : iSpecialRegAssignment).getCourseAssignments()) {
						if (!saved.isAssigned() || saved.isFreeTime() || saved.isTeachingAssignment() || !course.getCourseId().equals(saved.getCourseId())) continue;
						classes: for (ClassAssignmentInterface.ClassAssignment clazz: saved.getClassAssignments()) {
							for (ClassAssignmentInterface.ClassAssignment x: course.getClassAssignments()) {
								if (clazz.getClassId().equals(x.getClassId())) continue classes;
							}
							if (clazz.isSaved() && !clazz.hasError()) {
								iScheduleChanged = true;
								iSubmitSpecReg.addStyleName("unitime-EnrollButton");
								iStatus.warning(MESSAGES.warnSpecialRegistrationChanged(), false);
							}
						}
					}
				}
				courses: for (ClassAssignmentInterface.CourseAssignment course: (iSpecialRegAssignment == null ? iSavedAssignment : iSpecialRegAssignment).getCourseAssignments()) {
					if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
					for (ClassAssignmentInterface.CourseAssignment x: iLastAssignment.getCourseAssignments())
						if (course.getCourseId().equals(x.getCourseId())) continue courses;
					for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
						if (clazz.isSaved() && !clazz.hasError()) {
							iScheduleChanged = true;
							iSubmitSpecReg.addStyleName("unitime-EnrollButton");
							iStatus.warning(MESSAGES.warnSpecialRegistrationChanged(), false);
						}
					}
				}
			}
			return;
		}
		if (iLastAssignment != null && iSpecialRegAssignment != null && iSpecRegCx.isCanSubmit()) {
			boolean changed = false;
			for (ClassAssignmentInterface.CourseAssignment course: iLastAssignment.getCourseAssignments()) {
				if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
				classes: for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					for (ClassAssignmentInterface.CourseAssignment saved: iSpecialRegAssignment.getCourseAssignments()) {
						if (!saved.isAssigned() || saved.isFreeTime() || saved.isTeachingAssignment() || !course.getCourseId().equals(saved.getCourseId())) continue;
						for (ClassAssignmentInterface.ClassAssignment x: saved.getClassAssignments()) {
							if (clazz.getClassId().equals(x.getClassId())) continue classes;
						}
					}
					changed = true; break;
				}
				for (ClassAssignmentInterface.CourseAssignment saved: iSpecialRegAssignment.getCourseAssignments()) {
					if (!saved.isAssigned() || saved.isFreeTime() || saved.isTeachingAssignment() || !course.getCourseId().equals(saved.getCourseId())) continue;
					classes: for (ClassAssignmentInterface.ClassAssignment clazz: saved.getClassAssignments()) {
						for (ClassAssignmentInterface.ClassAssignment x: course.getClassAssignments()) {
							if (clazz.getClassId().equals(x.getClassId())) continue classes;
						}
						if (clazz.isSaved() && !clazz.hasError()) {
							changed = true; break;
						}
					}
				}
			}
			courses: for (ClassAssignmentInterface.CourseAssignment course: iSpecialRegAssignment.getCourseAssignments()) {
				if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
				for (ClassAssignmentInterface.CourseAssignment x: iLastAssignment.getCourseAssignments())
					if (course.getCourseId().equals(x.getCourseId())) continue courses;
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					if (clazz.isSaved() && !clazz.hasError()) {
						changed = true; break;
					}
				}
			}
			if (!changed) return;
		}
		boolean cr = iSchedule.isVisible();
		boolean empty = true;
		if (iSavedAssignment != null)
			courses: for (ClassAssignmentInterface.CourseAssignment course: iSavedAssignment.getCourseAssignments()) {
				if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					if (clazz.isSaved()) { empty = false; break courses; }
				}
			}
		for (ClassAssignmentInterface.CourseAssignment course: iLastAssignment.getCourseAssignments()) {
			if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
			for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
				if (!clazz.isSaved() && !clazz.hasError()) {
					iScheduleChanged = true;
					iEnroll.addStyleName("unitime-EnrollButton");
					if (cr)
						iStatus.warning(empty ? MESSAGES.warnScheduleEmptyOnCourseRequest() : MESSAGES.warnScheduleChangedOnCourseRequest(), false);
					else
						iStatus.warning(empty ? MESSAGES.warnScheduleEmptyOnClassSchedule() : MESSAGES.warnScheduleChangedOnClassSchedule(), false);
					return;
				}
			}
			if (iSavedAssignment != null)
				for (ClassAssignmentInterface.CourseAssignment saved: iSavedAssignment.getCourseAssignments()) {
					if (!saved.isAssigned() || saved.isFreeTime() || saved.isTeachingAssignment() || !course.getCourseId().equals(saved.getCourseId())) continue;
					classes: for (ClassAssignmentInterface.ClassAssignment clazz: saved.getClassAssignments()) {
						for (ClassAssignmentInterface.ClassAssignment x: course.getClassAssignments()) {
							if (clazz.getClassId().equals(x.getClassId())) continue classes;
						}
						if (clazz.isSaved() && !clazz.hasError()) {
							iScheduleChanged = true;
							iEnroll.addStyleName("unitime-EnrollButton");
							iStatus.warning(cr ? MESSAGES.warnScheduleChangedOnCourseRequest() : MESSAGES.warnScheduleChangedOnClassSchedule(), false);
						}
					}
				}
		}
		if (iSavedAssignment != null)
			courses: for (ClassAssignmentInterface.CourseAssignment course: iSavedAssignment.getCourseAssignments()) {
				if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
				for (ClassAssignmentInterface.CourseAssignment x: iLastAssignment.getCourseAssignments())
					if (course.getCourseId().equals(x.getCourseId())) continue courses;
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					if (clazz.isSaved() && !clazz.hasError()) {
						iScheduleChanged = true;
						iEnroll.addStyleName("unitime-EnrollButton");
						iStatus.warning(cr ? MESSAGES.warnScheduleChangedOnCourseRequest() : MESSAGES.warnScheduleChangedOnClassSchedule(), false);
					}
				}
			}
		CourseRequestInterface request = iCourseRequests.getRequest();
		courses: for (ClassAssignmentInterface.CourseAssignment course: iLastAssignment.getCourseAssignments()) {
			if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse(course)) continue courses;
			}
			for (CourseRequestInterface.Request r: request.getAlternatives()) {
				if (r.hasRequestedCourse(course)) continue courses;
			}
			iScheduleChanged = true;
			iEnroll.addStyleName("unitime-EnrollButton");
			iStatus.warning(cr ? MESSAGES.warnScheduleChangedOnCourseRequest() : MESSAGES.warnScheduleChangedOnClassSchedule(), false);
		}
	}
	
	public boolean isChanged() {
		return iScheduleChanged;
	}
	
	public void clearMessage() {
		clearMessage(true);
	}
	
	public void clearMessage(boolean showEligibility) {
		if (iEligibilityCheck != null && iOnline && showEligibility) {
			if (iEligibilityCheck.hasFlag(EligibilityFlag.PIN_REQUIRED))
				iStatus.error(MESSAGES.exceptionAuthenticationPinNotProvided(), false);
			else if (iEligibilityCheck.hasMessage() && (iMode.isSectioning() && !iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL)))
				iStatus.error(iEligibilityCheck.getMessage(), false);
			else if (iEligibilityCheck.hasMessage() && (!iMode.isSectioning() && !iEligibilityCheck.hasFlag(EligibilityFlag.CAN_REGISTER)))
				iStatus.error(iEligibilityCheck.getMessage(), false);
			else if (iEligibilityCheck.hasMessage() && iEligibilityCheck.hasFlag(EligibilityFlag.RECHECK_BEFORE_ENROLLMENT))
				iStatus.warning(iEligibilityCheck.getMessage(), false);
			else if (iEligibilityCheck.hasMessage())
				iStatus.info(iEligibilityCheck.getMessage(), false);
			else { 
				iStatus.clear();
				iEnroll.removeStyleName("unitime-EnrollButton");
				iSave.removeStyleName("unitime-EnrollButton");
				iSubmitSpecReg.removeStyleName("unitime-EnrollButton");
			}
		} else {
			iStatus.clear();
			iEnroll.removeStyleName("unitime-EnrollButton");
			iSave.removeStyleName("unitime-EnrollButton");
			iSubmitSpecReg.removeStyleName("unitime-EnrollButton");
		}
		if (isChanged())
			updateScheduleChangedNoteIfNeeded();
	}
	
	protected void setElibibilityCheckDuringEnrollment(EligibilityCheck check) {
		iEligibilityCheck = check;
		iSpecRegCx.update(check);
		iCourseRequests.setCanWaitList(check.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_WAITLIST));
		iCourseRequests.setArrowsVisible(!check.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.NO_REQUEST_ARROWS));
		if (check.hasFlag(EligibilityFlag.CAN_ENROLL)) {
			iEnroll.setVisible(true);
			if (check.hasCheckboxMessage()) {
				if (iCustomCheckbox == null) {
					iCustomCheckbox = new CheckBox(iEligibilityCheck.getCheckboxMessage(), true);
					((HorizontalPanel)iFooter.getWidget(1)).insert(iCustomCheckbox, 0);
					((HorizontalPanel)iFooter.getWidget(1)).setCellVerticalAlignment(iCustomCheckbox, HasVerticalAlignment.ALIGN_MIDDLE);
					iCustomCheckbox.getElement().getStyle().setPaddingRight(10, Unit.PX);
					iCustomCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							iEnroll.setEnabled(event.getValue());
						}
					});
				} else {
					iCustomCheckbox.setHTML(check.getCheckboxMessage());
				}
				iEnroll.setEnabled(iCustomCheckbox.getValue());
				iCustomCheckbox.setEnabled(true);
				iCustomCheckbox.setVisible(true);
			} else {
				iEnroll.setEnabled(true);
			}
		} else {
			iEnroll.setEnabled(false);
			iEnroll.setVisible(false);
			if (iCustomCheckbox != null) {
				iCustomCheckbox.setVisible(false);
				iCustomCheckbox.setEnabled(false);
			}
		}
	}
	
	public boolean hasNotAssignedClass(ClassAssignmentInterface result) {
		for (final ClassAssignmentInterface.CourseAssignment course: result.getCourseAssignments()) {
			if (course.isAssigned() && !course.isFreeTime()) {
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					if (!clazz.isAssigned()) return true;
				}
			}
		}
		return false;
	}
	
	public List<String> getCoursesToDrop() {
		if (iLastAssignment != null && iSavedAssignment != null) {
			List<String> ret = new ArrayList<String>();
			courses: for (ClassAssignmentInterface.CourseAssignment course: iSavedAssignment.getCourseAssignments()) {
				if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
				for (ClassAssignmentInterface.CourseAssignment x: iLastAssignment.getCourseAssignments())
					if (course.getCourseId().equals(x.getCourseId()) && x.isAssigned())
						continue courses;
				ret.add(MESSAGES.course(course.getSubject(), course.getCourseNbr()));
			}
			return ret;
		}
		return null;
	}
	
	public boolean useDefaultConfirmDialog() {
		return iEligibilityCheck == null || !iEligibilityCheck.hasFlag(EligibilityFlag.GWT_CONFIRMATIONS);
	}

	protected Command confirmEnrollment(final Command callback) {
		if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CONFIRM_DROP)) {
			final List<String> drops = getCoursesToDrop();
			if (drops != null && !drops.isEmpty()) {
				return new Command() {
					@Override
					public void execute() {
						UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), MESSAGES.confirmEnrollmentCourseDrop(ToolBox.toString(drops)), callback);
					}
				};
			}
		}
		return callback;
	}

	protected CourseFinder getQuickAddFinder() {
		if (iQuickAddFinder == null) {
			iQuickAddFinder = new CourseFinderDialog();
			((CourseFinderDialog)iQuickAddFinder).setText(MESSAGES.dialogQuickAdd());
			((CourseFinderDialog)iQuickAddFinder).addCloseHandler(new CloseHandler<PopupPanel>() {
				public void onClose(CloseEvent<PopupPanel> event) {
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iAssignmentPanel.setFocus(true);
						}
					});
				}
			});
			CourseFinderCourses courses = new CourseFinderCourses(CONSTANTS.showCourseTitle(), CONSTANTS.courseFinderSuggestWhenEmpty());
			courses.setDataProvider(new DataProvider<String, Collection<CourseAssignment>>() {
				@Override
				public void getData(String source, AsyncCallback<Collection<CourseAssignment>> callback) {
					iSectioningService.listCourseOfferings(iSessionSelector.getAcademicSessionId(), source, null, callback);
				}
			});
			CourseFinderDetails details = new CourseFinderDetails();
			details.setDataProvider(new DataProvider<CourseAssignment, String>() {
				@Override
				public void getData(CourseAssignment source, AsyncCallback<String> callback) {
					iSectioningService.retrieveCourseDetails(iSessionSelector.getAcademicSessionId(), source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
				}
			});
			CourseFinderClasses classes = new CourseFinderClasses(true, iSpecRegCx);
			classes.setDataProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
				@Override
				public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
					iSectioningService.listClasses(iOnline, iSessionSelector.getAcademicSessionId(), source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
				}
			});
			courses.setCourseDetails(details, classes);
			iQuickAddFinder.setTabs(courses);
			iQuickAddFinder.addSelectionHandler(new SelectionHandler<RequestedCourse>() {
				@Override
				public void onSelection(final SelectionEvent<RequestedCourse> event) {
					if (event.getSelectedItem() == null || event.getSelectedItem().isEmpty()) {
						iStatus.warning(MESSAGES.courseSelectionNoCourseSelected());
						return;
					}
					if (iCourseRequests.hasCourse(event.getSelectedItem())) {
						UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), MESSAGES.confirmQuickDrop(event.getSelectedItem().getCourseName()), new Command() {
							@Override
							public void execute() {
								final CourseRequestInterface undo = iCourseRequests.getRequest();
								iCourseRequests.dropCourse(event.getSelectedItem());
								LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
								CourseRequestInterface r = iCourseRequests.getRequest(); r.setNoChange(true);
								iSectioningService.section(iOnline, r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
									public void onFailure(Throwable caught) {
										LoadingWidget.getInstance().hide();
										iStatus.error(MESSAGES.exceptionSectioningFailed(caught.getMessage()), caught);
										iCourseRequests.setRequest(undo);
									}
									public void onSuccess(ClassAssignmentInterface result) {
										fillIn(result);
										addHistory();
										iQuickAddFinder.setValue(null, true);
									}
								});
							}
						});
					} else {
						if (iQuickAddSuggestions == null) {
							iQuickAddSuggestions = new SuggestionsBox(iAssignmentGrid.getColorProvider(), iOnline, iSpecRegCx);
							iQuickAddSuggestions.addCloseHandler(new CloseHandler<PopupPanel>() {
								public void onClose(CloseEvent<PopupPanel> event) {
									Scheduler.get().scheduleDeferred(new ScheduledCommand() {
										@Override
										public void execute() {
											iAssignmentPanel.setFocus(true);
										}
									});
								}
							});
						}
						iQuickAddSuggestions.open(iCourseRequests.getRequest(), iLastResult, event.getSelectedItem(), useDefaultConfirmDialog(), new AsyncCallback<ClassAssignmentInterface>() {
							@Override
							public void onSuccess(ClassAssignmentInterface result) {
								clearMessage();
								if (!iCourseRequests.hasCourse(event.getSelectedItem()))
									iCourseRequests.addCourse(event.getSelectedItem());
								fillIn(result);
								addHistory();
								iQuickAddFinder.setValue(null, true);
							}
							
							@Override
							public void onFailure(Throwable caught) {
								if (caught != null) iStatus.error(caught.getMessage());
								iAssignmentPanel.setFocus(true);
							}
						});
					}
				}
			});
		}
		return iQuickAddFinder;
	}
	
	protected void checkSpecialRegistrationAfterFailedSubmitSchedule(ArrayList<ClassAssignmentInterface.ClassAssignment> lastEnrollment, final boolean showWaiting) {
		if (!iSpecRegCx.isCanSubmit()) return;
		iLastEnrollment = lastEnrollment;
		final boolean enrl = iEnroll.isVisible();
		final boolean print = iPrint.isVisible();
		final boolean spreg = iSubmitSpecReg.isVisible();
		if (showWaiting)
			LoadingWidget.getInstance().show(MESSAGES.waitOverridesCheck());
		else {
			iEnroll.setEnabled(false); iEnroll.setVisible(false);
			iPrint.setEnabled(false); iPrint.setVisible(false);
			iSubmitSpecReg.setEnabled(false); iSubmitSpecReg.setVisible(false);
			iWaitingMessage.setText(MESSAGES.waitOverridesCheck());
			iWaiting.setVisible(true);
		}
		iSpecialRegAssignment = null;
		iSectioningService.checkSpecialRequestEligibility(
				new SpecialRegistrationEligibilityRequest(iSessionSelector.getAcademicSessionId(), iEligibilityCheck.getStudentId(), iSpecRegCx.getRequestId(), iLastEnrollment, iLastAssignment == null ? null : iLastAssignment.getErrors()),
				new AsyncCallback<SpecialRegistrationEligibilityResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						if (showWaiting) LoadingWidget.getInstance().hide();
						else {
							iWaiting.setVisible(false);
							iEnroll.setEnabled(enrl); iEnroll.setVisible(enrl);
							iSubmitSpecReg.setEnabled(spreg); iSubmitSpecReg.setVisible(spreg);
							iPrint.setEnabled(print); iPrint.setVisible(print);
						}
						iStatus.error(MESSAGES.requestSpecialRegistrationFail(caught.getMessage()), caught);
					}

					@Override
					public void onSuccess(SpecialRegistrationEligibilityResponse response) {
						if (showWaiting) LoadingWidget.getInstance().hide();
						else {
							iWaiting.setVisible(false);
							iEnroll.setEnabled(enrl); iEnroll.setVisible(enrl);
							iSubmitSpecReg.setEnabled(spreg); iSubmitSpecReg.setVisible(spreg);
							iPrint.setEnabled(print); iPrint.setVisible(print);
						}
						final Collection<ErrorMessage> errors = response.getErrors();
						if (response.isCanSubmit() && errors != null && !errors.isEmpty()) {
							CheckCoursesResponse confirm = new CheckCoursesResponse();
							confirm.setConfirmation(0, MESSAGES.dialogRequestOverrides(),
									MESSAGES.buttonRequestOverrides(), MESSAGES.buttonCancelRequest(),
									MESSAGES.titleRequestOverrides(), MESSAGES.titleCancelRequest());
							confirm.addConfirmation(MESSAGES.messageRegistrationErrorsDetected(), 0, -1);
							confirm.addConfirmation(MESSAGES.messageRequestOverridesNote(), 0, 2);
							final CourseRequestInterface.CourseMessage note = confirm.addConfirmation(iSpecRegCx.getNote() == null ? "" : iSpecRegCx.getNote(), 0, 3); note.setCode("REQUEST_NOTE");
							confirm.addConfirmation(MESSAGES.messageRequestOverridesOptions(), 0, 4);
							confirm.addConfirmation(MESSAGES.messageRequestOverridesDisclaimer(), 0, 7);
							for (ErrorMessage e: errors) {
								if ("IGNORE".equals(e.getCode())) {
									continue;
								} else if (iEligibilityCheck.hasOverride(e.getCode())) {
									confirm.addMessage(null, e.getCourse(), e.getCode(), e.getMessage(), 0);
								} else {
									confirm.addMessage(null, e.getCourse(), e.getCode(), e.getMessage() + " (" + e.getCode() + ")", 0);
								}
							}
							if (response.hasCancelErrors()) {
								confirm.addConfirmation(MESSAGES.messageRequestOverridesCancel(), 0, 5);
								for (ErrorMessage e: response.getCancelErrors())
									confirm.addMessage(null, e.getCourse(), e.getCode(), e.getMessage(), 0, 6);
							}
							CourseRequestsConfirmationDialog.confirm(confirm, 0, new AsyncCallback<Boolean>() {
								@Override
								public void onSuccess(Boolean result) {
									if (result) {
										clearMessage();
										LoadingWidget.getInstance().show(MESSAGES.waitSpecialRegistration());
										iSectioningService.submitSpecialRequest(
												new SubmitSpecialRegistrationRequest(iSessionSelector.getAcademicSessionId(), iEligibilityCheck.getStudentId(), iSpecRegCx.getRequestKey(), iSpecRegCx.getRequestId(), iCourseRequests.getRequest(),
														iLastEnrollment != null ? iLastEnrollment : iLastResult, errors, note.getMessage()),
												new AsyncCallback<SubmitSpecialRegistrationResponse>() {
													@Override
													public void onFailure(Throwable caught) {
														LoadingWidget.getInstance().hide();
														iStatus.error(MESSAGES.submitSpecialRegistrationFail(caught.getMessage()), caught);
														updateHistory();
													}

													@Override
													public void onSuccess(SubmitSpecialRegistrationResponse response) {
														LoadingWidget.getInstance().hide();
														if (response.isSuccess())
															iSpecialRegAssignment = iLastAssignment;
														iSpecRegCx.setStatus(response.getStatus());
														iSpecRegCx.setRequestId(response.getRequestId());
														iSpecRegCx.setNote(note.getMessage());
														if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG)) {
															iEligibilityCheck.setFlag(EligibilityFlag.HAS_SPECREG, true);
															iGetSpecRegs.setVisible(true);
															iGetSpecRegs.setEnabled(true);
														}
														if (!iSpecRegCx.isCanSubmit()) {
															iSubmitSpecReg.setVisible(false);
															iSubmitSpecReg.setEnabled(false);
														}
														for (CourseAssignment ca: iLastAssignment.getCourseAssignments())
															for (ClassAssignment a: ca.getClassAssignments()) {
																a.setError(null); a.setSpecRegStatus(null);
																for (ErrorMessage f: errors) {
																	if (a.getExternalId() != null && a.getExternalId().equals(f.getSection())) {
																		if (a.hasError())
																			a.setError(a.getError() + "\n" + f.getMessage());
																		else
																			a.setError(f.getMessage());
																		a.setSpecRegStatus(response.getStatus());
																	}
																}
															}
														fillIn(iLastAssignment);
														iSpecRegCx.setChanges(iLastAssignment);
														if (response.isSuccess()) {
															iStatus.done(response.hasMessage() ? response.getMessage() : MESSAGES.submitSecialRegistrationOK());
														} else {
															iStatus.error(response.getMessage());
														}
														updateHistory();
													}
												});
									}
								}
								@Override
								public void onFailure(Throwable caught) {
									iStatus.error(MESSAGES.requestSpecialRegistrationFail(caught.getMessage()), caught);
								}
							});
						} else if (response.hasMessage()) {
							if (showWaiting)
								iStatus.warning(response.getMessage());
							else
								UniTimeConfirmationDialog.alert(response.getMessage());
						} else if (response.isCanSubmit()) {
							if (showWaiting)
								iStatus.warning(MESSAGES.errorNoRegistrationErrorsDetected());
							else
								UniTimeConfirmationDialog.alert(MESSAGES.errorNoRegistrationErrorsDetected());
						}
					}
				});
	}
	
	protected SpecialRegistrationSelectionDialog getSpecialRegistrationSelectionDialog() {
		if (iSpecialRegistrationSelectionDialog == null) {
			iSpecialRegistrationSelectionDialog = new SpecialRegistrationSelectionDialog(iSpecRegCx) {
				public void doSubmit(final RetrieveSpecialRegistrationResponse specReg) {
					super.doSubmit(specReg);
					if (specReg == null) {
						iSpecRegCx.update(iEligibilityCheck);
						iSpecRegCx.setRequestId(null);
						iSpecRegCx.setStatus(null);
						iSpecRegCx.setNote(null);
						iSpecRegCx.clearChanges();
						iSpecialRegAssignment = iSavedAssignment;
						fillIn(iSavedAssignment);
						addHistory();
					} else {
						iSpecRegCx.setRequestId(specReg.getRequestId());
						iSpecRegCx.setStatus(specReg.getStatus());
						iSpecRegCx.setNote(specReg.getNote());
						iSpecRegCx.setChanges(specReg);
						iSpecialRegAssignment = null;
						if (specReg.hasChanges()) {
							final CourseRequestInterface courseRequests = iCourseRequests.getRequest();
							courseRequests.setTimeConflictsAllowed(specReg.hasTimeConflict()); courseRequests.setSpaceConflictsAllowed(specReg.hasSpaceConflict());
							Set<Long> specRegDrops = new HashSet<Long>();
							for (ClassAssignmentInterface.ClassAssignment ch: specReg.getChanges())
								if (!ch.isCourseAssigned()) specRegDrops.add(ch.getCourseId());
							for (ClassAssignmentInterface.ClassAssignment ch: specReg.getChanges())
								if (ch.isCourseAssigned() && (!specRegDrops.contains(ch.getCourseId()) || ch.getSpecRegStatus() != null))
									courseRequests.addCourse(new RequestedCourse(ch.getCourseId(), CONSTANTS.showCourseTitle() ? ch.getCourseNameWithTitle() : ch.getCourseName()));
							LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
							iSectioningService.section(iOnline, courseRequests, iLastResult, specReg.getChanges(), new AsyncCallback<ClassAssignmentInterface>() {
								public void onSuccess(ClassAssignmentInterface result) {
									fillIn(result);
									iCourseRequests.setRequest(courseRequests);
									addHistory();
								}
								public void onFailure(Throwable caught) {
									iStatus.error(MESSAGES.exceptionSectioningFailed(caught.getMessage()), caught);
									LoadingWidget.getInstance().hide();
								}
							});
						} else {
							fillIn(iSavedAssignment);
							addHistory();
						}
					}
				}
				public void doCancel(String requestId, final AsyncCallback<Boolean> callback) {
					CancelSpecialRegistrationRequest req = new CancelSpecialRegistrationRequest();
					req.setRequestId(requestId);
					req.setSessionId(iSessionSelector.getAcademicSessionId());
					req.setStudentId(iEligibilityCheck.getStudentId());
					iSectioningService.cancelSpecialRequest(req, new AsyncCallback<CancelSpecialRegistrationResponse>() {
						@Override
						public void onSuccess(CancelSpecialRegistrationResponse result) {
							if (result.isFailure() && result.hasMessage())
								iStatus.error(MESSAGES.cancelSpecialRegistrationFail(result.getMessage()));
							callback.onSuccess(result.isSuccess());
						}
						
						@Override
						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
							iStatus.error(MESSAGES.cancelSpecialRegistrationFail(caught.getMessage()), caught);
						}
					});
				}
			};
		}
		return iSpecialRegistrationSelectionDialog;
	}
	
	protected void printConfirmation(CourseRequestInterface savedRequests) {
		WebTable requests = new WebTable();
		requests.setEmptyMessage(StudentSectioningWidget.MESSAGES.emptyRequests());
		requests.setHeader(new WebTable.Row(
				new WebTable.Cell(StudentSectioningWidget.MESSAGES.colPriority(), 1, "25px"),
				new WebTable.Cell(StudentSectioningWidget.MESSAGES.colCourse(), 1, "75px"),
				new WebTable.Cell(StudentSectioningWidget.MESSAGES.colTitle(), 1, "200px"),
				new WebTable.Cell(StudentSectioningWidget.MESSAGES.colCredit(), 1, "20px"),
				new WebTable.Cell(StudentSectioningWidget.MESSAGES.colPreferences(), 1, "100px"),
				new WebTable.Cell(StudentSectioningWidget.MESSAGES.colWarnings(), 1, "200px"),
				new WebTable.Cell(StudentSectioningWidget.MESSAGES.colStatus(), 1, "20px"),
				new WebTable.Cell(StudentSectioningWidget.MESSAGES.colWaitList(), 1, "20px")
				));

		ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
		boolean hasPref = false, hasWarn = false, hasWait = false;
		NumberFormat df = NumberFormat.getFormat("0.#");
		CheckCoursesResponse check = new CheckCoursesResponse(savedRequests.getConfirmations());
		hasWarn = savedRequests.hasConfirmations();
		int priority = 1;
		for (Request request: savedRequests.getCourses()) {
			if (!request.hasRequestedCourse()) continue;
			boolean first = true;
			if (request.isWaitList()) hasWait = true;
			for (RequestedCourse rc: request.getRequestedCourse()) {
				if (rc.isCourse()) {
					ImageResource icon = null; String iconText = null;
					String msg = check.getMessage(rc.getCourseName(), "\n", "CREDIT");
					if (check.isError(rc.getCourseName()) && (rc.getStatus() == null || rc.getStatus() != RequestedCourseStatus.OVERRIDE_REJECTED)) {
						icon = RESOURCES.requestError(); iconText = (msg);
					} else if (rc.getStatus() != null) {
						switch (rc.getStatus()) {
						case ENROLLED:
							icon = RESOURCES.requestEnrolled(); iconText = (MESSAGES.enrolled(rc.getCourseName()));
							break;
						case OVERRIDE_NEEDED:
							icon = RESOURCES.requestNeeded(); iconText = (MESSAGES.overrideNeeded(msg));
							break;
						case SAVED:
							icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.requested(rc.getCourseName()));
							break;				
						case OVERRIDE_REJECTED:
							icon = RESOURCES.requestRejected(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideRejected(rc.getCourseName()));
							break;
						case OVERRIDE_PENDING:
							icon = RESOURCES.requestPending(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overridePending(rc.getCourseName()));
							break;
						case OVERRIDE_CANCELLED:
							icon = RESOURCES.requestCancelled(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideCancelled(rc.getCourseName()));
							break;
						case OVERRIDE_APPROVED:
							icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideApproved(rc.getCourseName()));
							break;
						default:
							if (check.isError(rc.getCourseName()))
								icon = RESOURCES.requestError(); iconText = (msg);
						}
					}
					if (rc.hasStatusNote()) iconText += "\n" + MESSAGES.overrideNote(rc.getStatusNote());
					Collection<String> prefs = null;
					if (rc.hasSelectedIntructionalMethods()) {
						if (rc.hasSelectedClasses()) {
							prefs = new ArrayList<String>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
							prefs.addAll(new TreeSet<String>(rc.getSelectedIntructionalMethods()));
							prefs.addAll(new TreeSet<String>(rc.getSelectedClasses()));
						} else {
							prefs = new TreeSet<String>(rc.getSelectedIntructionalMethods());
						}
					} else if (rc.hasSelectedClasses()) {
						prefs = new TreeSet<String>(rc.getSelectedClasses());
					}
					String status = "";
					if (rc.getStatus() != null) {
						switch (rc.getStatus()) {
						case ENROLLED: status = MESSAGES.reqStatusEnrolled(); break;
						case OVERRIDE_APPROVED: status = MESSAGES.reqStatusApproved(); break;
						case OVERRIDE_CANCELLED: status = MESSAGES.reqStatusCancelled(); break;
						case OVERRIDE_PENDING: status = MESSAGES.reqStatusPending(); break;
						case OVERRIDE_REJECTED: status = MESSAGES.reqStatusRejected(); break;
						}
					}
					if (status.isEmpty()) status = MESSAGES.reqStatusRegistered();
					if (prefs != null) hasPref = true;
					WebTable.Cell credit = new WebTable.Cell(rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
					credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
					String note = null;
					if (check != null) note = check.getMessage(rc.getCourseName(), "\n", "CREDIT");
					if (rc.hasStatusNote()) note = (note == null ? "" : note + "\n") + rc.getStatusNote();
					P messages = new P("text-pre-wrap"); messages.setText(note);
					WebTable.Row row = new WebTable.Row(
							new WebTable.Cell(first ? MESSAGES.courseRequestsPriority(priority) : ""),
							new WebTable.Cell(rc.getCourseName()),
							new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
							credit, 
							new WebTable.Cell(ToolBox.toString(prefs)),
							new WebTable.WidgetCell(messages, note),
							(icon == null ? new WebTable.Cell(status) : new WebTable.IconCell(icon, iconText, status)),
							(first && request.isWaitList() ? new WebTable.IconCell(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed(), "") : new WebTable.Cell(""))
							);
					if (priority > 1 && first)
						for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-dashed");
					rows.add(row);
				} else if (rc.isFreeTime()) {
					String  free = "";
					for (FreeTime ft: rc.getFreeTime()) {
						if (!free.isEmpty()) free += ", ";
						free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
					}
					WebTable.Row row = new WebTable.Row(
							new WebTable.Cell(first ? MESSAGES.courseRequestsPriority(priority) : ""),
							new WebTable.Cell(CONSTANTS.freePrefix() + free, 3, null),
							new WebTable.Cell(""),
							new WebTable.Cell(""),
							new WebTable.IconCell(RESOURCES.requestSaved(), MESSAGES.requested(free), MESSAGES.reqStatusRegistered()),
							new WebTable.Cell(""));
					if (priority > 1 && first)
						for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-dashed");
					rows.add(row);
				}
				first = false;
			}
			priority ++;
		}
		priority = 1;
		for (Request request: savedRequests.getAlternatives()) {
			if (!request.hasRequestedCourse()) continue;
			boolean first = true;
			if (request.isWaitList()) hasWait = true;
			for (RequestedCourse rc: request.getRequestedCourse()) {
				if (rc.isCourse()) {
					ImageResource icon = null; String iconText = null;
					String msg = check.getMessage(rc.getCourseName(), "\n", "CREDIT");
					if (check.isError(rc.getCourseName()) && (rc.getStatus() == null || rc.getStatus() != RequestedCourseStatus.OVERRIDE_REJECTED)) {
						icon = RESOURCES.requestError(); iconText = (msg);
					} else if (rc.getStatus() != null) {
						switch (rc.getStatus()) {
						case ENROLLED:
							icon = RESOURCES.requestEnrolled(); iconText = (MESSAGES.enrolled(rc.getCourseName()));
							break;
						case OVERRIDE_NEEDED:
							icon = RESOURCES.requestNeeded(); iconText = (MESSAGES.overrideNeeded(msg));
							break;
						case SAVED:
							icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.requested(rc.getCourseName()));
							break;				
						case OVERRIDE_REJECTED:
							icon = RESOURCES.requestRejected(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideRejected(rc.getCourseName()));
							break;
						case OVERRIDE_PENDING:
							icon = RESOURCES.requestPending(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overridePending(rc.getCourseName()));
							break;
						case OVERRIDE_CANCELLED:
							icon = RESOURCES.requestCancelled(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideCancelled(rc.getCourseName()));
							break;
						case OVERRIDE_APPROVED:
							icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideApproved(rc.getCourseName()));
							break;
						default:
							if (check.isError(rc.getCourseName()))
								icon = RESOURCES.requestError(); iconText = (msg);
						}
					}
					if (rc.hasStatusNote()) iconText += "\n" + MESSAGES.overrideNote(rc.getStatusNote());
					Collection<String> prefs = null;
					if (rc.hasSelectedIntructionalMethods()) {
						if (rc.hasSelectedClasses()) {
							prefs = new ArrayList<String>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
							prefs.addAll(new TreeSet<String>(rc.getSelectedIntructionalMethods()));
							prefs.addAll(new TreeSet<String>(rc.getSelectedClasses()));
						} else {
							prefs = new TreeSet<String>(rc.getSelectedIntructionalMethods());
						}
					} else if (rc.hasSelectedClasses()) {
						prefs = new TreeSet<String>(rc.getSelectedClasses());
					}
					if (prefs != null) hasPref = true;
					String status = "";
					if (rc.getStatus() != null) {
						switch (rc.getStatus()) {
						case ENROLLED: status = MESSAGES.reqStatusEnrolled(); break;
						case OVERRIDE_APPROVED: status = MESSAGES.reqStatusApproved(); break;
						case OVERRIDE_CANCELLED: status = MESSAGES.reqStatusCancelled(); break;
						case OVERRIDE_PENDING: status = MESSAGES.reqStatusPending(); break;
						case OVERRIDE_REJECTED: status = MESSAGES.reqStatusRejected(); break;
						}
					}
					if (status.isEmpty()) status = MESSAGES.reqStatusRegistered();
					WebTable.Cell credit = new WebTable.Cell(rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
					credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
					String note = null;
					if (check != null) note = check.getMessage(rc.getCourseName(), "\n", "CREDIT");
					if (rc.hasStatusNote()) note = (note == null ? "" : note + "\n") + rc.getStatusNote();
					P messages = new P("text-pre-wrap"); messages.setText(note);
					WebTable.Row row = new WebTable.Row(
							new WebTable.Cell(first ? MESSAGES.courseRequestsAlternative(priority) : ""),
							new WebTable.Cell(rc.getCourseName()),
							new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
							credit,
							new WebTable.Cell(ToolBox.toString(prefs)),
							new WebTable.WidgetCell(messages, note),
							(icon == null ? new WebTable.Cell(status) : new WebTable.IconCell(icon, iconText, status)),
							(first && request.isWaitList() ? new WebTable.IconCell(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed(), "") : new WebTable.Cell(""))
							);
					if (first)
						for (WebTable.Cell cell: row.getCells()) cell.setStyleName(priority == 1 ? "top-border-solid" : "top-border-dashed");
					rows.add(row);
				} else if (rc.isFreeTime()) {
					String  free = "";
					for (FreeTime ft: rc.getFreeTime()) {
						if (!free.isEmpty()) free += ", ";
						free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
					}
					WebTable.Row row = new WebTable.Row(
							new WebTable.Cell(first ? MESSAGES.courseRequestsPriority(priority) : ""),
							new WebTable.Cell(CONSTANTS.freePrefix() + free, 3, null),
							new WebTable.Cell(""),
							new WebTable.Cell(""),
							new WebTable.IconCell(RESOURCES.requestSaved(), MESSAGES.requested(free), MESSAGES.reqStatusRegistered()),
							new WebTable.Cell(""));
					if (first)
						for (WebTable.Cell cell: row.getCells()) cell.setStyleName(priority == 1 ? "top-border-solid" : "top-border-dashed");
					rows.add(row);
				}
				first = false;
			}
			priority ++;
		}
		
		if (savedRequests.getMaxCreditOverrideStatus() != null) {
			ImageResource icon = null;
			String status = "";
			String note = null;
			String iconText = null;
			if (savedRequests.hasCreditWarning()) {
				note = savedRequests.getCreditWarning();
				iconText = savedRequests.getCreditWarning();
				hasWarn = true;
			}
			switch (savedRequests.getMaxCreditOverrideStatus()) {
			case CREDIT_HIGH:
				icon = RESOURCES.requestError();
				status = MESSAGES.reqStatusRejected();
				iconText += "\n" + MESSAGES.creditStatusTooHigh();
				break;
			case OVERRIDE_REJECTED:
				icon = RESOURCES.requestError();
				status = MESSAGES.reqStatusRejected();
				iconText += "\n" + MESSAGES.creditStatusDenied();
				break;
			case CREDIT_LOW:
			case OVERRIDE_NEEDED:
				icon = RESOURCES.requestNeeded();
				status = MESSAGES.reqStatusWarning();
				break;
			case OVERRIDE_CANCELLED:
				icon = RESOURCES.requestNeeded();
				status = MESSAGES.reqStatusCancelled();
				iconText += "\n" + MESSAGES.creditStatusCancelled();
				break;
			case OVERRIDE_PENDING:
				icon = RESOURCES.requestPending();
				status = MESSAGES.reqStatusPending();
				iconText += "\n" + MESSAGES.creditStatusPending();
				break;
			case OVERRIDE_APPROVED:
				icon = RESOURCES.requestSaved();
				status = MESSAGES.reqStatusApproved();
				iconText += (iconText == null ? "" : iconText + "\n") + MESSAGES.creditStatusApproved();
				break;
			case SAVED:
				icon = RESOURCES.requestSaved();
				status = MESSAGES.reqStatusRegistered();
				break;
			}
			if (savedRequests.hasCreditNote()) {
				note = (note == null ? "" : note + "\n") + savedRequests.getCreditNote();
				hasWarn = true;
			}
			float[] range = savedRequests.getCreditRange();
			WebTable.Cell credit = new WebTable.Cell(range != null ? range[0] < range[1] ? df.format(range[0]) + " - " + df.format(range[1]) : df.format(range[0]) : "");
			credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			P messages = new P("text-pre-wrap"); messages.setText(note);
			WebTable.Row row = new WebTable.Row(
					new WebTable.Cell(MESSAGES.rowRequestedCredit(), 2, null),
					new WebTable.Cell(""),
					credit,
					new WebTable.Cell(""),
					new WebTable.WidgetCell(messages, note),
					(icon == null ? new WebTable.Cell(status) : new WebTable.IconCell(icon, iconText, status)),
					new WebTable.Cell("")
					);
			for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-solid");
			row.getCell(0).setStyleName("top-border-solid text-bold");
			rows.add(row);
		}

		WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
		int idx = 0;
		for (WebTable.Row row: rows) rowArray[idx++] = row;
		
		requests.setData(rowArray);
		requests.setColumnVisible(4, hasPref);
		requests.setColumnVisible(5, hasWarn);
		requests.setColumnVisible(7, hasWait);
		
		P credit = new P("unitime-StatusLine");
		float[] range = savedRequests.getCreditRange();
		if (range != null && range[1] > 0f) {
			if (range[0] == range[1]) credit.setText(MESSAGES.requestedCredit(range[0]));
			else credit.setText(MESSAGES.requestedCreditRange(range[0], range[1]));
		}
		
		ToolBox.print(GWT_MESSAGES.pageStudentCourseRequests(),
				iUserAuthentication.getUser(),
				iSessionSelector.getAcademicSessionName(),
				requests//, credit
				);
	}
}
