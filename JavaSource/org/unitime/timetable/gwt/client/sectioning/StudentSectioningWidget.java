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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTabBar;
import org.unitime.timetable.gwt.client.sectioning.StudentSectioningPage.Mode;
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
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
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
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface;
import org.unitime.timetable.gwt.shared.StudentSchedulingPreferencesInterface;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.CancelSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.CancelSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.ChangeGradeModesResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveAllSpecialRegistrationsRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationEligibilityResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationStatus;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SubmitSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.UpdateSpecialRegistrationRequest;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.UpdateSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseResponse;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CourseMessage;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestPriority;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck.EligibilityFlag;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradeMode;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
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
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.requestWaitListedDateFormat());

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private AcademicSessionProvider iSessionSelector;
	private UserAuthenticationProvider iUserAuthentication;
	
	private VerticalPanel iPanel;
	private P iFooter, iHeader;
	private AriaMultiButton iRequests, iReset, iSchedule, iEnroll, iPrint, iExport = null, iSave, iStartOver, iDegreePlan, iChangeGradeModes, iAdvisorReqs, iPreferences;
	
	private AriaTabBar iAssignmentTab;
	private DockPanel iAssignmentDock;
	private FocusPanel iAssignmentPanel;
	private ImageLink iCalendar = null;
	
	private CourseRequestsTable iCourseRequests;
	private WebTable iAssignments;
	private TimeGrid iAssignmentGrid;
	private SuggestionsBox iSuggestionsBox;
	private CheckBox iShowUnassignments;
	private Label iTotalCredit;
	private P iGridMessage;
	private Image iTotalCreditRequestsStatus;
	private P iTotalCreditRequests;
	private SpecialRegistrationsPanel iSpecialRegistrationsPanel;
	private WaitListsPanel iWaitListsPanel;
	
	private ArrayList<ClassAssignmentInterface.ClassAssignment> iLastResult, iLastEnrollment;
	private ClassAssignmentInterface iLastAssignment, iSavedAssignment = null, iSpecialRegAssignment = null;
	private CourseRequestInterface iSavedRequest = null;
	private ArrayList<HistoryItem> iHistory = new ArrayList<HistoryItem>();
	private boolean iInRestore = false;
	private boolean iTrackHistory = true;
	private StudentSectioningContext iContext;
	private SpecialRegistrationContext iSpecRegCx = new SpecialRegistrationContext();
	private StudentSectioningPage.Mode iMode = null;
	private OnlineSectioningInterface.EligibilityCheck iEligibilityCheck = null;
	private PinDialog iPinDialog = null;
	private boolean iScheduleChanged = false;
	private ScheduleStatus iStatus = null;
	private AriaButton iQuickAdd, iRequestVarTitleCourse;
	private CourseFinder iQuickAddFinder = null;
	private SuggestionsBox iQuickAddSuggestions = null;
	
	private CheckBox iCustomCheckbox = null;
	private DegreePlansSelectionDialog iDegreePlansSelectionDialog = null;
	private DegreePlanDialog iDegreePlanDialog = null;
	private StudentSchedulingPreferencesDialog iSchedulingPreferencesDialog = null;
	
	private ChangeGradeModesDialog iChangeGradeModesDialog = null;
	private RequestVariableTitleCourseDialog iRequestVariableTitleCourseDialog = null;
	private Float iCurrentCredit = null;
	
	private WaitListedRequestPreferences iWaitListedRequestPreferences = null;

	public StudentSectioningWidget(boolean online, AcademicSessionProvider sessionSelector, UserAuthenticationProvider userAuthentication, StudentSectioningPage.Mode mode, boolean history) {
		iMode = mode;
		iContext = new StudentSectioningContext();
		iContext.setOnline(online);
		iContext.setSectioning(mode.isSectioning());
		iContext.setSessionId(sessionSelector.getAcademicSessionId());
		iSessionSelector = sessionSelector;
		iUserAuthentication = userAuthentication;
		iTrackHistory = history;
		
		iPanel = new VerticalPanel();
		iPanel.addStyleName("unitime-SchedulingAssistant");
		
		iCourseRequests = new CourseRequestsTable(iContext, iSpecRegCx);
		iCourseRequests.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<CourseRequestInterface> event) {
				if (iTotalCreditRequests != null) {
					iTotalCreditRequestsStatus.setVisible(false);
					if (!isChanged() && iSavedRequest != null && iSavedRequest.getMaxCreditOverrideStatus() != null) {
						String cw = (iSavedRequest.hasCreditWarning() ? iSavedRequest.getCreditWarning() : iSavedRequest.hasMaxCredit() ? MESSAGES.creditWarning(iSavedRequest.getMaxCredit()) : null);
						String note = "";
						if (iSavedRequest.hasRequestorNote())
							note += "\n" + MESSAGES.requestNote(iSavedRequest.getRequestorNote());
						if (iSavedRequest.hasCreditNote())
							note += "\n" + MESSAGES.overrideNote(iSavedRequest.getCreditNote()); 
						
						switch (iSavedRequest.getMaxCreditOverrideStatus()) {
						case CREDIT_HIGH:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestNeeded());
							iTotalCreditRequestsStatus.setAltText(cw + "\n" + MESSAGES.creditStatusTooHigh() + note);
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case OVERRIDE_REJECTED:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestError());
							iTotalCreditRequestsStatus.setAltText(cw + "\n" + MESSAGES.creditStatusDenied() + note);
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case OVERRIDE_CANCELLED:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestNeeded());
							iTotalCreditRequestsStatus.setAltText(cw + "\n" + MESSAGES.creditStatusCancelled() + note);
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case CREDIT_LOW:
						case OVERRIDE_NEEDED:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestNeeded());
							iTotalCreditRequestsStatus.setAltText(cw + note);
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case OVERRIDE_PENDING:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestPending());
							iTotalCreditRequestsStatus.setAltText(cw + "\n" + MESSAGES.creditStatusPending() + note);
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case OVERRIDE_APPROVED:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestSaved());
							iTotalCreditRequestsStatus.setAltText(MESSAGES.creditStatusApproved() + note);
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						case SAVED:
							iTotalCreditRequestsStatus.setResource(RESOURCES.requestSaved());
							iTotalCreditRequestsStatus.setAltText(note.isEmpty() ? "" : note.substring(1));
							iTotalCreditRequestsStatus.setTitle(iTotalCreditRequestsStatus.getAltText());
							iTotalCreditRequestsStatus.setVisible(true);
							break;
						}
					}
					float[] credit = iCourseRequests.getRequest().getCreditRange(iEligibilityCheck == null ? null : iEligibilityCheck.getAdvisorWaitListedCourseIds());
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
		
		iHeader = new P("unitime-SchedulingAssistantButtons", "unitime-SchedulingAssistantButtonsHeader");
		iPanel.add(iHeader);
		
		iPanel.add(iCourseRequests);
		
		iFooter = new P("unitime-SchedulingAssistantButtons", "unitime-SchedulingAssistantButtonsFooter");
		
		P leftFooterPanel = new P("left-panel");
		P leftHeaderPanel = new P("left-panel");
		iPreferences = new AriaMultiButton(RESOURCES.preferences(), MESSAGES.buttonStudentSchedulingPreferences());
		iPreferences.setTitle(MESSAGES.hintStudentSchedulingPreferences());
		iPreferences.setVisible(false);
		iPreferences.setEnabled(false);
		leftFooterPanel.add(iPreferences);
		leftHeaderPanel.add(iPreferences.createClone()); 
		
		iDegreePlan = new AriaMultiButton(MESSAGES.buttonDegreePlan());
		iDegreePlan.setTitle(MESSAGES.hintDegreePlan());
		iDegreePlan.setVisible(false);
		iDegreePlan.setEnabled(false);
		leftFooterPanel.add(iDegreePlan);
		leftHeaderPanel.add(iDegreePlan.createClone());
		
		iAdvisorReqs = new AriaMultiButton(MESSAGES.buttonAdvisorRequests());
		iAdvisorReqs.setTitle(MESSAGES.hintAdvisorRequests());
		iAdvisorReqs.setVisible(false);
		iAdvisorReqs.setEnabled(false);
		leftFooterPanel.add(iAdvisorReqs);
		leftHeaderPanel.add(iAdvisorReqs.createClone());
		
		iRequests = (LocaleInfo.getCurrentLocale().isRTL() ? new AriaMultiButton(MESSAGES.buttonRequests(), RESOURCES.arrowForward()) : new AriaMultiButton(RESOURCES.arrowBack(), MESSAGES.buttonRequests()));
		iRequests.setTitle(MESSAGES.hintRequests());
		iRequests.setVisible(false);
		iRequests.setEnabled(false);
		leftFooterPanel.add(iRequests);
		leftHeaderPanel.add(iRequests.createClone());

		iReset = new AriaMultiButton(MESSAGES.buttonReset());
		iReset.setTitle(MESSAGES.hintReset());
		iReset.setVisible(false);
		iReset.setEnabled(false);
		leftFooterPanel.add(iReset);
		leftHeaderPanel.add(iReset.createClone());
		iFooter.add(leftFooterPanel);
		iHeader.add(leftHeaderPanel);
		
		if (mode == StudentSectioningPage.Mode.REQUESTS) {
			iTotalCreditRequestsStatus = new Image(); iTotalCreditRequestsStatus.addStyleName("credit-status"); iTotalCreditRequestsStatus.setVisible(false);
			iTotalCreditRequestsStatus.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (!iSpecRegCx.isAllowChangeRequestNote() || !iSpecRegCx.getChangeRequestorNoteInterface().changeRequestorCreditNote(iSavedRequest)) {
						if (iTotalCreditRequestsStatus.getAltText() != null && !iTotalCreditRequestsStatus.getAltText().isEmpty())
							UniTimeConfirmationDialog.info(iTotalCreditRequestsStatus.getAltText());
					}
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
		P rightHeaderPanel = new P("right-panel");
		iHeader.add(rightHeaderPanel);
		
		iStartOver = new AriaMultiButton(MESSAGES.buttonStartOver());
		iStartOver.setTitle(MESSAGES.hintStartOver());
		leftFooterPanel.add(iStartOver);
		iStartOver.setVisible(false);
		iStartOver.setEnabled(false);
		leftHeaderPanel.add(iStartOver.createClone());
		
		iSchedule = (LocaleInfo.getCurrentLocale().isRTL() ? new AriaMultiButton(RESOURCES.arrowBack(), MESSAGES.buttonSchedule()) : new AriaMultiButton(MESSAGES.buttonSchedule(), RESOURCES.arrowForward()));
		iSchedule.setTitle(MESSAGES.hintSchedule());
		if (mode.isSectioning()) {
			rightFooterPanel.add(iSchedule);
			rightHeaderPanel.add(iSchedule.createClone());
		}
		iSchedule.setVisible(mode.isSectioning());
		iSchedule.setEnabled(mode.isSectioning());
		
		iSave = new AriaMultiButton(MESSAGES.buttonSave());
		iSave.setTitle(MESSAGES.hintSave());
		if (!mode.isSectioning()) {
			rightFooterPanel.add(iSave);
			rightHeaderPanel.add(iSave.createClone());
		}
		iSave.setVisible(!mode.isSectioning());
		iSave.setEnabled(false);

		iEnroll = new AriaMultiButton(MESSAGES.buttonEnroll());
		iEnroll.setTitle(MESSAGES.hintEnroll());
		iEnroll.setVisible(false);
		iEnroll.setEnabled(false);
		rightFooterPanel.add(iEnroll);
		rightHeaderPanel.add(iEnroll.createClone());
		
		iChangeGradeModes = new AriaMultiButton(MESSAGES.buttonChangeGradeModes());
		iChangeGradeModes.setTitle(MESSAGES.hintChangeGradeModes());
		iChangeGradeModes.setVisible(false);
		iChangeGradeModes.setEnabled(false);
		rightFooterPanel.add(iChangeGradeModes);
		rightHeaderPanel.add(iChangeGradeModes.createClone());
		
		iRequestVarTitleCourse = new AriaMultiButton(RESOURCES.quickAddCourse(), MESSAGES.buttonRequestVariableTitleCourse());
		iRequestVarTitleCourse.setStyleName("unitime-QuickAddButton");
		iRequestVarTitleCourse.setTitle(MESSAGES.hintRequestVariableTitleCourse());
		iRequestVarTitleCourse.setVisible(false);
		iRequestVarTitleCourse.setEnabled(false);
		
		iPrint = new AriaMultiButton(MESSAGES.buttonPrint());
		iPrint.setTitle(MESSAGES.hintPrint());
		iPrint.setVisible(false);
		iPrint.setEnabled(false);
		rightFooterPanel.add(iPrint);
		rightHeaderPanel.add(iPrint.createClone());

		if (CONSTANTS.allowCalendarExport()) {
			iExport = new AriaMultiButton(MESSAGES.buttonExport());
			iExport.setTitle(MESSAGES.hintExport());
			iExport.setVisible(false);
			iExport.setEnabled(false);
			rightFooterPanel.add(iExport);
			rightHeaderPanel.add(iExport.createClone());
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
		
		iPreferences.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iSchedulingPreferencesDialog == null) {
					iSchedulingPreferencesDialog = new StudentSchedulingPreferencesDialog(iSessionSelector) {
						protected void doApply() {
							super.doApply();
							iSectioningService.setStudentSchedulingPreferences(iContext, getValue(), new AsyncCallback<Boolean>() {
								@Override
								public void onSuccess(Boolean result) {
									iStatus.info(MESSAGES.infoSchedulingPreferencesUpdated());
								}
								@Override
								public void onFailure(Throwable caught) {
									iStatus.error(MESSAGES.failedToUpdatePreferences(caught.getMessage()), caught);
								}
							});
						}
					};
				}
				iSectioningService.getStudentSchedulingPreferences(iContext, new AsyncCallback<StudentSchedulingPreferencesInterface>() {
					@Override
					public void onSuccess(StudentSchedulingPreferencesInterface result) {
						if (result != null) {
							iSchedulingPreferencesDialog.setValue(result);
							iSchedulingPreferencesDialog.center();
						}
					}
					@Override
					public void onFailure(Throwable caught) {
						iStatus.error(MESSAGES.failedToLoadPreferences(caught.getMessage()), caught);
					}
				});
			}
		});
		
		iDegreePlan.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.waitListDegreePlans());
				iSectioningService.listDegreePlans(iContext, new AsyncCallback<List<DegreePlanInterface>>() {
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
									iSectioningService.retrieveCourseDetails(iContext, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
								}
							});
							CourseFinderClasses classes = new CourseFinderClasses(false, iSpecRegCx);
							classes.setDataProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
								@Override
								public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
									iSectioningService.listClasses(iContext, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
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
		
		iAdvisorReqs.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.waitAdvisorRequests());
				iSectioningService.getAdvisorRequests(iContext, new AsyncCallback<CourseRequestInterface>() {

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
								iStatus.error(MESSAGES.failedAdvisorRequests(s.getMessage()), s);
						} else {
							iStatus.error(MESSAGES.failedAdvisorRequests(caught.getMessage()), caught);
						}
					}

					@Override
					public void onSuccess(CourseRequestInterface result) {
						LoadingWidget.getInstance().hide();
						new AdvisorCourseRequestsDialog(iCourseRequests, new DegreePlanDialog.AssignmentProvider() {
							@Override
							public ClassAssignmentInterface getSavedAssignment() {
								return iSavedAssignment;
							}
							@Override
							public ClassAssignmentInterface getLastAssignment() {
								return iLastAssignment;
							}
						}) {
							@Override
							protected void doApply() {
								updateHistory();
								super.doApply();
								addHistory();
							}
						}.open(result);
					}
					
				});
			}
		});
		
		iSpecRegCx.setChangeRequestorNote(new SpecialRegistrationInterface.ChangeRequestorNoteInterface() {
			@Override
			public boolean changeRequestorNote(final RequestedCourse rc) {
				if (rc == null || rc.getRequestId() == null || rc.getStatus() != RequestedCourseStatus.OVERRIDE_PENDING) return false;
				String message = null;
				if (iCourseRequests.getLastCheck() != null) {
					for (CourseMessage m: iCourseRequests.getLastCheck().getMessages(rc.getCourseName())) {
						if ("NO_ALT".equals(m.getCode())) continue;
						if ("OVERLAP".equals(m.getCode())) continue;
						if ("CREDIT".equals(m.getCode())) continue;
						if ("WL-OVERLAP".equals(m.getCode())) continue;
						if ("WL-INACTIVE".equals(m.getCode())) continue;
						if ("WL-CREDIT".equals(m.getCode())) continue;
						if (message == null)
							message = MESSAGES.courseMessage(m.getMessage());
						else
							message += "\n" + MESSAGES.courseMessage(m.getMessage());
					}
				}
				if (message == null && iSavedRequest != null && iSavedRequest.hasConfirmations()) {
					for (CourseMessage m: iSavedRequest.getConfirmations()) {
						if ("NO_ALT".equals(m.getCode())) continue;
						if ("OVERLAP".equals(m.getCode())) continue;
						if ("CREDIT".equals(m.getCode())) continue;
						if ("WL-OVERLAP".equals(m.getCode())) continue;
						if ("WL-INACTIVE".equals(m.getCode())) continue;
						if ("WL-CREDIT".equals(m.getCode())) continue;
						if (m.hasCourse() && rc.getCourseId().equals(m.getCourseId())) {
							if (message == null)
								message = MESSAGES.courseMessage(m.getMessage());
							else
								message += "\n" + MESSAGES.courseMessage(m.getMessage());
						}
					}
				}
				if (message == null) return false;
				CheckCoursesResponse confirm = new CheckCoursesResponse();
				confirm.setConfirmation(0, MESSAGES.dialogChangeRequestNote(rc.getCourseName()),
						MESSAGES.buttonChangeRequestNote(), MESSAGES.buttonHideRequestNote(),
						MESSAGES.titleChangeRequestNote(), MESSAGES.titleHideRequestNote());
				confirm.addConfirmation(MESSAGES.requestedWarnings(message), 0, 1);
				confirm.addConfirmation("\n" + MESSAGES.messageRequestOverridesNote(), 0, 2);
				final CourseRequestInterface.CourseMessage note = confirm.addConfirmation(rc.hasRequestorNote() ? rc.getRequestorNote() : "", 0, 3); note.setCode("REQUEST_NOTE");
				if (rc.hasRequestorNoteSuggestions())
					for (String suggestion: rc.getRequestorNoteSuggestions())
						note.addSuggestion(suggestion);
				if (rc.hasStatusNote())
					confirm.addConfirmation("\n" + MESSAGES.overrideNote(rc.getStatusNote()), 0, 4);
				CourseRequestsConfirmationDialog.confirm(confirm, 0, RESOURCES.statusInfo(), new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if (result) {
							final String requestorNote = note.getMessage();
							UpdateSpecialRegistrationRequest request = new UpdateSpecialRegistrationRequest(
									iContext,
									rc.getRequestId(), rc.getCourseId(),
									requestorNote, iMode == Mode.REQUESTS);
							iSectioningService.updateSpecialRequest(request, new AsyncCallback<UpdateSpecialRegistrationResponse>() {
								@Override
								public void onSuccess(UpdateSpecialRegistrationResponse result) {
									if (result.isFailure() && result.hasMessage()) {
										iStatus.error(MESSAGES.updateSpecialRegistrationFail(result.getMessage()));
									} else {
										CourseRequestInterface req = iCourseRequests.getValue();
										if (req.updateRequestorNote(rc.getRequestId(), requestorNote)) {
											iCourseRequests.setValue(req);
										}
										if (iSavedRequest != null) {
											iSavedRequest.updateRequestorNote(rc.getRequestId(), requestorNote);
											if (iWaitListsPanel != null) iWaitListsPanel.populate(iSavedRequest, iSavedAssignment);
										}
										updateHistory();
									}
								}
								
								@Override
								public void onFailure(Throwable caught) {
									iStatus.error(MESSAGES.updateSpecialRegistrationFail(caught.getMessage()), caught);
								}
							});
						}
					}
					@Override
					public void onFailure(Throwable caught) {}
				});
				return true;
			}

			@Override
			public boolean changeRequestorCreditNote(final CourseRequestInterface request) {
				if (request == null || request.getRequestId() == null || request.getMaxCreditOverrideStatus() != RequestedCourseStatus.OVERRIDE_PENDING) return false;
				String message = (request.hasCreditWarning() ? request.getCreditWarning() : MESSAGES.creditWarning(request.getMaxCredit()));
				CheckCoursesResponse confirm = new CheckCoursesResponse();
				confirm.setConfirmation(0, MESSAGES.dialogChangeCreditRequestNote(),
						MESSAGES.buttonChangeRequestNote(), MESSAGES.buttonHideRequestNote(),
						MESSAGES.titleChangeRequestNote(), MESSAGES.titleHideRequestNote());
				confirm.addConfirmation(MESSAGES.requestedWarnings(MESSAGES.courseMessage(message)), 0, 1);
				confirm.addConfirmation("\n" + MESSAGES.messageRequestOverridesNote(), 0, 2);
				final CourseRequestInterface.CourseMessage note = confirm.addConfirmation(request.hasRequestorNote() ? request.getRequestorNote() : "", 0, 3); note.setCode("REQUEST_NOTE");
				if (request.hasRequestorNoteSuggestions())
					for (String suggestion: request.getRequestorNoteSuggestions())
						note.addSuggestion(suggestion);
				if (request.hasCreditNote())
					confirm.addConfirmation("\n" + MESSAGES.overrideNote(request.getCreditNote()), 0, 4);
				CourseRequestsConfirmationDialog.confirm(confirm, 0, RESOURCES.statusInfo(), new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if (result) {
							final String requestorNote = note.getMessage();
							UpdateSpecialRegistrationRequest req = new UpdateSpecialRegistrationRequest(
									iContext,
									request.getRequestId(), null,
									requestorNote, iMode == Mode.REQUESTS);
							iSectioningService.updateSpecialRequest(req, new AsyncCallback<UpdateSpecialRegistrationResponse>() {
								@Override
								public void onSuccess(UpdateSpecialRegistrationResponse result) {
									if (result.isFailure() && result.hasMessage()) {
										iStatus.error(MESSAGES.updateSpecialRegistrationFail(result.getMessage()));
									} else {
										if (request.updateRequestorNote(request.getRequestId(), requestorNote)) {
											iCourseRequests.setValue(request);
											if (iWaitListsPanel != null) iWaitListsPanel.populate(request, iSavedAssignment);
										}
										updateHistory();
									}
								}
								
								@Override
								public void onFailure(Throwable caught) {
									iStatus.error(MESSAGES.updateSpecialRegistrationFail(caught.getMessage()), caught);
								}
							});
						}
					}
					@Override
					public void onFailure(Throwable caught) {}
				});
				return true;
			}

			@Override
			public boolean changeRequestorNote(final RetrieveSpecialRegistrationResponse reg, final String course, final Long courseId) {
				if (reg == null || reg.getRequestId() == null || reg.getStatus() != SpecialRegistrationStatus.Pending) return false;
				CheckCoursesResponse confirm = new CheckCoursesResponse();
				confirm.setConfirmation(0, MESSAGES.dialogChangeSpecRegRequestNote(),
						MESSAGES.buttonChangeRequestNote(), MESSAGES.buttonHideRequestNote(),
						MESSAGES.titleChangeRequestNote(), MESSAGES.titleHideRequestNote());
				if (reg.hasErrors()) {
					confirm.addConfirmation(MESSAGES.requestedApprovals(), 0, 1);
					for (ErrorMessage e: reg.getErrors())
						if ((course == null || course.isEmpty()) && "MAXI".equals(e.getCode()))
							confirm.addMessage(null, e.getCourse(), e.getCode(), e.getMessage(), 0, 2);
						else if (course != null && course.equals(e.getCourse()))
							confirm.addMessage(null, e.getCourse(), e.getCode(), e.getMessage(), 0, 2);
				}
				confirm.addConfirmation(MESSAGES.messageRequestOverridesNote(), 0, 3);
				String previousNote = (course == null || course.isEmpty() ? reg.getNote("MAXI") : reg.getNote(course));
				final CourseRequestInterface.CourseMessage note = confirm.addConfirmation(previousNote == null ? "" : previousNote, 0, 4); note.setCode("REQUEST_NOTE");
				if (reg.hasSuggestions())
					for (String suggestion: reg.getSuggestions())
						note.addSuggestion(suggestion);
				CourseRequestsConfirmationDialog.confirm(confirm, 0, RESOURCES.statusInfo(), new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if (result) {
							final String requestorNote = note.getMessage();
							UpdateSpecialRegistrationRequest request = new UpdateSpecialRegistrationRequest(
									iContext,
									reg.getRequestId(), courseId,
									requestorNote, iMode == Mode.REQUESTS);
							iSectioningService.updateSpecialRequest(request, new AsyncCallback<UpdateSpecialRegistrationResponse>() {
								@Override
								public void onSuccess(UpdateSpecialRegistrationResponse result) {
									if (result.isFailure() && result.hasMessage()) {
										iStatus.error(MESSAGES.updateSpecialRegistrationFail(result.getMessage()));
									} else {
										reg.setNote(course == null || course.isEmpty() ? "MAXI" : course, requestorNote);
										iSpecialRegistrationsPanel.populate(iSpecialRegistrationsPanel.getRegistrations(), iSavedAssignment);
										updateHistory();
									}
								}
								@Override
								public void onFailure(Throwable caught) {
									iStatus.error(MESSAGES.updateSpecialRegistrationFail(caught.getMessage()), caught);
								}
							});
						}
					}
					@Override
					public void onFailure(Throwable caught) {}
				});
				return true;
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
		WebTable.Cell grMd = new WebTable.Cell(MESSAGES.colGradeMode());
		grMd.setTitle(MESSAGES.colTitleGradeMode()); grMd.setAriaLabel(MESSAGES.colTitleGradeMode());
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
				grMd,
				(iCalendar != null ? new WebTable.WidgetCell(iCalendar, MESSAGES.colIcons()) : new WebTable.Cell(MESSAGES.colIcons()))
			));
		iAssignments.setWidth("100%");
		iAssignments.setEmptyMessage(MESSAGES.emptySchedule());
		
		ScrollPanel assignmentsPanel = new ScrollPanel(iAssignments);
		assignmentsPanel.setStyleName("body");
		assignmentsPanel.getElement().getStyle().setOverflowY(Overflow.HIDDEN);
		
		final P panel = new P("unitime-Panel");
		panel.add(assignmentsPanel);
		iRequestVarTitleCourse.addStyleName("left");
		
		iTotalCredit = new Label("", false);
		iShowUnassignments = new CheckBox(MESSAGES.showUnassignments());
		iQuickAdd.addStyleName("left");
		iRequestVarTitleCourse.addStyleName("left");
		P bottom = new P("footer");
		iTotalCredit.addStyleName("center");
		iTotalCredit.getElement().getStyle().setMarginTop(3, Unit.PX);
		iShowUnassignments.addStyleName("right");
		iShowUnassignments.getElement().getStyle().setMarginTop(3, Unit.PX);
		bottom.add(iQuickAdd);
		bottom.add(iRequestVarTitleCourse);
		bottom.add(iShowUnassignments);
		bottom.add(iTotalCredit);
		panel.add(bottom);
		
		iSpecialRegistrationsPanel = new SpecialRegistrationsPanel(iSpecRegCx) {
			public void doSubmit(final RetrieveSpecialRegistrationResponse specReg) {
				super.doSubmit(specReg);
				if (specReg == null) {
					iSpecRegCx.update(iEligibilityCheck);
					iSpecRegCx.setRequestId(null);
					iSpecRegCx.setStatus(null);
					iSpecialRegAssignment = iSavedAssignment;
					fillIn(iSavedAssignment);
					addHistory();
				} else {
					iSpecRegCx.setRequestId(specReg.getRequestId());
					iSpecRegCx.setStatus(specReg.getStatus());
					iSpecialRegAssignment = null;
					if ((specReg.hasChanges() && !specReg.isGradeModeChange() && !specReg.isCreditChange()) ||
						(specReg.hasChanges() && specReg.isVariableTitleCourseChange())) {
						final CourseRequestInterface courseRequests = iCourseRequests.getRequest();
						courseRequests.setTimeConflictsAllowed(specReg.hasTimeConflict());
						courseRequests.setSpaceConflictsAllowed(specReg.hasSpaceConflict());
						courseRequests.setLinkedConflictsAllowed(specReg.hasLinkedConflict());
						courseRequests.setDeadlineConflictsAllowed(specReg.isExtended());
						Set<Long> specRegDrops = new HashSet<Long>();
						Set<Long> specRegAdds = new HashSet<Long>();
						for (ClassAssignmentInterface.ClassAssignment ch: specReg.getChanges())
							if (!ch.isCourseAssigned()) specRegDrops.add(ch.getCourseId()); else specRegAdds.add(ch.getCourseId());
						for (ClassAssignmentInterface.ClassAssignment ch: specReg.getChanges())
							if (ch.isCourseAssigned() && (!specRegDrops.contains(ch.getCourseId()) || ch.getSpecRegStatus() != null)) {
								RequestedCourse rc = new RequestedCourse(ch.getCourseId(), CONSTANTS.showCourseTitle() ? ch.getCourseNameWithTitle() : ch.getCourseName());
								rc.setCanWaitList(ch.isCanWaitList());
								courseRequests.addCourse(rc);
							} else if (!ch.isCourseAssigned() && !specRegAdds.contains(ch.getCourseId())) {
								RequestedCourse rc = new RequestedCourse(ch.getCourseId(), CONSTANTS.showCourseTitle() ? ch.getCourseNameWithTitle() : ch.getCourseName());
								rc.setCanWaitList(ch.isCanWaitList());
								courseRequests.dropCourse(rc);
							}
						LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
						iSectioningService.section(courseRequests, iLastResult, specReg.getChanges(), new AsyncCallback<ClassAssignmentInterface>() {
							public void onSuccess(ClassAssignmentInterface result) {
								if (specReg.hasSpaceConflict() || specReg.hasTimeConflict() || specReg.hasLinkedConflict()) iSpecRegCx.setDisclaimerAccepted(true);
								iCourseRequests.setRequest(courseRequests);
								fillIn(result);
								addHistory();
								// Wait-list check 
								for (final ClassAssignmentInterface.CourseAssignment course: result.getCourseAssignments()) {
									if (!course.isFreeTime() && specReg.isAdd(course.getCourseId()) && !course.isAssigned() &&
										course.isCanWaitList() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_WAITLIST) &&
										course.isNotAvailable() && !iCourseRequests.isWaitListed(course.getCourseId())) {
										UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), 
												(course.isFull() ? MESSAGES.suggestionsNoChoicesCourseIsFull(course.getCourseName()) :
												(course.hasHasIncompReqs() ? MESSAGES.suggestionsNoChoicesDueToStudentPrefs(course.getCourseName()) :
												MESSAGES.suggestionsNoChoices(course.getCourseName())))+ "\n" + MESSAGES.confirmQuickWaitList(course.getCourseName()), new Command() {
											@Override
											public void execute() {
												final CourseRequestInterface undo = iCourseRequests.getRequest();
												iCourseRequests.setWaitList(course.getCourseId(), true);
												LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
												CourseRequestInterface r = iCourseRequests.getRequest(); r.setNoChange(true);
												iSectioningService.section(r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
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
										break;
									}
								}
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
				CancelSpecialRegistrationRequest req = new CancelSpecialRegistrationRequest(iContext);
				req.setRequestId(requestId);
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
		panel.add(iSpecialRegistrationsPanel);
		
		iWaitListsPanel = new WaitListsPanel(iSpecRegCx);
		iWaitListsPanel.getTable().addMouseClickListener(new MouseClickListener<RequestedCourse>() {
			@Override
			public void onMouseClick(TableEvent<RequestedCourse> event) {
				if (event.getData() != null) {
					CourseRequestLine line = iCourseRequests.getWaitListedLine(event.getData().getCourseId());
					if (line != null) {
						clearMessage();
						getWaitListedRequestPreferences().setSchedule(iLastAssignment);
						getWaitListedRequestPreferences().show(line, event.getData().getCourseId());
					}
				}
			}
		});
		panel.add(iWaitListsPanel);

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
				iSectioningService.section(iCourseRequests.getRequest(), null, new AsyncCallback<ClassAssignmentInterface>() {
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
							iSectioningService.section(iCourseRequests.getRequest(), iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
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
						if (caught != null)
							iStatus.error(MESSAGES.validationFailedWithMessage(caught.getMessage()), caught);
						else
							iStatus.error(MESSAGES.validationFailed());
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
							lastRequest(false);
						}
					});
				} else {
					clearMessage();
					clear(false);
					iStartOver.setVisible(false);
					iStartOver.setEnabled(false);
					iSpecRegCx.reset(iEligibilityCheck);
					addHistory();
					lastRequest(false);
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
						iSectioningService.enroll(iCourseRequests.getRequest(), iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
							public void onSuccess(ClassAssignmentInterface result) {
								LoadingWidget.getInstance().hide();
								iSavedAssignment = result;
								iStartOver.setVisible(iSavedAssignment != null && !iSavedAssignment.getCourseAssignments().isEmpty());
								iStartOver.setEnabled(iSavedAssignment != null && !iSavedAssignment.getCourseAssignments().isEmpty());
								if (result.hasCurrentCredit() && iEligibilityCheck != null)
									iEligibilityCheck.setCurrentCredit(result.getCurrentCredit());
								if (result.getRequest() != null && result.getRequest().hasWaitListChecks())
									iCourseRequests.setLastCheck(result.getRequest().getWaitListChecks());
								if (result.hasRequest()) {
									for (Request r: result.getRequest().getCourses()) {
										for (RequestedCourse rc: r.getRequestedCourse()) {
											if (rc.hasCourseId() && !iCourseRequests.isActive(rc) && !r.isWaitList())
												rc.setInactive(true);
										}
									}
									for (Request r: result.getRequest().getAlternatives()) {
										for (RequestedCourse rc: r.getRequestedCourse()) {
											if (rc.hasCourseId() && !iCourseRequests.isActive(rc) && !r.isWaitList())
												rc.setInactive(true);
										}
									}
									iCourseRequests.setRequest(result.getRequest());
									iSavedRequest = result.getRequest();
									if (iWaitListsPanel != null) iWaitListsPanel.populate(result.getRequest(), iSavedAssignment);
								}
								fillIn(result, iEligibilityCheck == null || !iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG));
								if (!result.hasMessages())
									iStatus.done(MESSAGES.enrollOK());
								updateHistory();
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.RECHECK_AFTER_ENROLLMENT)) {
									iSectioningService.checkEligibility(iContext,
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
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG)) {
									if (result.isError() || result.hasErrors()) {
										checkSpecialRegistrationAfterFailedSubmitSchedule(lastEnrollment, null, result, new Command() {
											@Override
											public void execute() {
												checkWaitListAfterSubmitSchedule();
											}
										});
									} else {
										checkWaitListAfterSubmitSchedule();
									}
									iSpecialRegistrationsPanel.populate(iSpecialRegistrationsPanel.getRegistrations(), iSavedAssignment);
								} else {
									checkWaitListAfterSubmitSchedule();
								}
							}
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG))
									iStatus.error(MESSAGES.enrollFailed(caught.getMessage()), (caught instanceof SectioningException && ((SectioningException)caught).isCanRequestOverride() ? false : true), caught);
								else
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
											iPinDialog.checkEligibility(iContext, callback, iSessionSelector.getAcademicSessionInfo());
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
									if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG) && se.isCanRequestOverride()) {
										checkSpecialRegistrationAfterFailedSubmitSchedule(lastEnrollment, caught, null, null);
									}
								}
							}
						});
					}
				};
				enroll = confirmEnrollment(enroll);
				enroll.execute();
			}
		});
		
		iChangeGradeModes.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				changeGradeModes(new ArrayList<ClassAssignmentInterface.ClassAssignment>(iLastResult), iSpecialRegistrationsPanel.getRegistrations());
			}
		});
		
		iRequestVarTitleCourse.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				requestVariableTitleCourse();
			}
		});
		
		iPrint.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				boolean allSaved = true;
				for (ClassAssignmentInterface.ClassAssignment clazz: iLastResult) {
					if (clazz != null && !clazz.isFreeTime() && !clazz.isTeachingAssignment() && !clazz.isSaved()) allSaved = false;
				}
				Widget w = iAssignments.getPrintWidget(0, 5, 16);
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
										if (iWaitListsPanel != null) iWaitListsPanel.populate(result, iSavedAssignment);
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
				Cookies.setCookie("UniTime:Unassignments", event.getValue() ? "1" : "0");
				fillIn(iLastAssignment);
			}
		});
	}
	
	public void openSuggestionsBox(int rowIndex) {
		ClassAssignmentInterface.ClassAssignment row = iLastResult.get(rowIndex);
		if (row != null && row.getCourseId() != null && iCourseRequests.isWaitListed(row.getCourseId()) && !row.isAssigned()) {
			iAssignments.setSelectedRow(rowIndex);
			clearMessage();
			getWaitListedRequestPreferences().setSchedule(iLastAssignment);
			getWaitListedRequestPreferences().show(iCourseRequests.getWaitListedLine(row.getCourseId()));
			return;
		}
		if (iSuggestionsBox == null) {
			iSuggestionsBox = new SuggestionsBox(iAssignmentGrid.getColorProvider(), iSpecRegCx);
			
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
					iSectioningService.section(r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
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
			
			iSuggestionsBox.addWaitListHandler(new SuggestionsBox.WaitListHandler() {
				@Override
				public void onWaitList(final SuggestionsBox.WaitListEvent event) {
					getWaitListedRequestPreferences().setSchedule(iLastAssignment);
					CourseRequestLine line = iCourseRequests.getWaitListedLine(event.getAssignment().getCourseId());
					getWaitListedRequestPreferences().showWaitListAssigned(line, event.getAssignment().getCourseId());
				}
			});
		}
		iAssignments.setSelectedRow(rowIndex);
		clearMessage();
		iSuggestionsBox.open(iCourseRequests.getRequest(), iLastResult, rowIndex,
				iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.ALTERNATIVES_DROP),
				iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_WAITLIST),
				useDefaultConfirmDialog());
	}
	
	private void fillIn(ClassAssignmentInterface result) {
		fillIn(result, true);
	}
	
	private void fillIn(ClassAssignmentInterface result, boolean popup) {
		iLastEnrollment = null;
		iLastResult.clear();
		iLastAssignment = result;
		String calendarUrl = GWT.getHostPageBaseURL() + "calendar?sid=" + iSessionSelector.getAcademicSessionId() + "&cid=";
		String ftParam = "&ft=";
		boolean hasError = false, hasWarning = false;
		iCurrentCredit = 0f;
		boolean hasGradeMode = false;
		if (!result.getCourseAssignments().isEmpty() || CONSTANTS.allowEmptySchedule()) {
			ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
			iAssignmentGrid.clear(true);
			for (final ClassAssignmentInterface.CourseAssignment course: result.getCourseAssignments()) {
				boolean firstClazz = true;
				boolean selfWaitListed = false;
				if (course.isAssigned()) {
					if (course.getCourseId() != null)
						iCourseRequests.activate(course);
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
						
						WebTable.CheckboxCell waitList = null;
						if (firstClazz && !clazz.isTeachingAssignment() && !clazz.isSaved() && !clazz.isFreeTime() && (clazz.isDummy() || result.isCanEnroll()) && clazz.hasError()) {
							boolean courseEnrolled = false;
							for (ClassAssignmentInterface.ClassAssignment x: course.getClassAssignments())
								if (x.isSaved()) { courseEnrolled = true; break; }
							if (!courseEnrolled) {
								Boolean w = iCourseRequests.getWaitList(course.getCourseId());
								if (w != null && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_WAITLIST) && course.isCanWaitList()) {
									waitList = new WebTable.CheckboxCell(w, MESSAGES.toggleWaitList(), ARIA.titleRequestedWaitListForCourse(MESSAGES.course(course.getSubject(), course.getCourseNbr())));
									waitList.getWidget().setStyleName("toggle");
									((CheckBox)waitList.getWidget()).addValueChangeHandler(new ValueChangeHandler<Boolean>() {
										@Override
										public void onValueChange(ValueChangeEvent<Boolean> event) {
											clearMessage();
											iCourseRequests.setWaitList(course.getCourseId(), event.getValue());
											
											LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
											CourseRequestInterface r = iCourseRequests.getRequest(); r.setNoChange(true);
											iSectioningService.section(r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
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
							}
						}
						
						GradeMode gradeMode = clazz.getGradeMode();
						SpecialRegistrationStatus specRegStatus = iSpecialRegistrationsPanel.getStatus(clazz);
						Float creditHour = null;
						if (specRegStatus != null) {
							String error = iSpecialRegistrationsPanel.getError(clazz);
							GradeMode gm = iSpecialRegistrationsPanel.getGradeMode(clazz);
							if (gm != null && gradeMode != null) gradeMode = gm;
							Float ch = iSpecialRegistrationsPanel.getCreditHours(clazz);
							if (ch != null) { creditHour = ch; iCurrentCredit += creditHour; }
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
							style += (specRegStatus != SpecialRegistrationStatus.Rejected ? " text-blue" : " text-red");
							if (clazz.hasError())
								hasError = true;
							else if (clazz.hasWarn())
								hasWarning = true;
						} else if (clazz.hasError()) {
							icons.add(RESOURCES.error(), clazz.getError(), true);
							style += " text-red";
							hasError = true;
						} else if (clazz.hasWarn()) {
							icons.add(RESOURCES.warning(), clazz.getWarn(), true);
							hasWarning = true;
						} else if (clazz.hasInfo()) {
							icons.add(RESOURCES.info(), clazz.getInfo(), true);
						}
						if (course.isLocked())
							icons.add(RESOURCES.courseLocked(), MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr()));
						if (clazz.isOfHighDemand())
							icons.add(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()));
						if (clazz != null && clazz.hasOverlapNote())
							icons.add(RESOURCES.overlap(), clazz.getOverlapNote());
						if (clazz.isCancelled())
							icons.add(RESOURCES.cancelled(), MESSAGES.classCancelled(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));

						if (!clazz.isTeachingAssignment() && creditHour == null)
							iCurrentCredit += clazz.guessCreditCount();
						if (gradeMode != null)
							hasGradeMode = true;
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
								(waitList != null ? waitList : creditHour != null ? new WebTable.Cell(MESSAGES.credit(creditHour)) : new WebTable.AbbvTextCell(clazz.getCredit())),
								(gradeMode == null ? new WebTable.Cell("") : new WebTable.Cell(gradeMode.getCode()).title(gradeMode.getLabel()).aria(gradeMode.getLabel())),
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
									(waitList != null ? waitList : creditHour != null ? new WebTable.Cell(MESSAGES.credit(creditHour)) : new WebTable.AbbvTextCell(clazz.getCredit())),
									(gradeMode == null ? new WebTable.Cell("") : new WebTable.Cell(gradeMode.getCode()).title(gradeMode.getLabel()).aria(gradeMode.getLabel())),
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
					
					if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_WAITLIST)) {
						CourseRequestLine line = iCourseRequests.getWaitListedLine(course.getCourseId());
						if (line != null && line.getWaitList() && course.getCourseId().equals(line.getValue().getWaitListSwapWithCourseOfferingId())) {
							selfWaitListed = true;
							for (ClassAssignmentInterface.ClassAssignment x: course.getClassAssignments())
								if (!x.isSaved()) {
									// different assignment proposed -> cannot self wait-list
									line.setWaitList(false);
									selfWaitListed = false;
									break;
								}
						}
					}
				}
				if (!iSpecialRegistrationsPanel.isDrop(course.getCourseId()) && iCourseRequests.isActive(course) && (!course.isAssigned() || selfWaitListed)) {
					String style = "text-red" + (!rows.isEmpty() && !selfWaitListed ? " top-border-dashed": "");
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
						else if (course.hasHasIncompReqs())
							unassignedMessage = MESSAGES.classNotAvailableDueToStudentPrefs();
						else
							unassignedMessage = MESSAGES.classNotAvailable();
					} else if (course.isLocked()) {
						unassignedMessage = MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr());
					}
					if (course.isOverMaxCredit())
						unassignedMessage = MESSAGES.conflictOverMaxCredit(course.getOverMaxCredit())
							+ (MESSAGES.courseNotAssigned().equals(unassignedMessage) ? "" : "\n" + unassignedMessage);
					WebTable.IconsCell icons = new WebTable.IconsCell();
					if (course.isLocked())
						icons.add(RESOURCES.courseLocked(), course.getNote() != null ? course.getNote() : MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr()));
					
					WebTable.CheckboxCell waitList = null;
					Boolean w = iCourseRequests.getWaitList(course.getCourseId());
					if (w != null && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_WAITLIST) && course.isCanWaitList()) {
						if (w.booleanValue()) {
							if (course.getWaitListedDate() != null & iSavedRequest != null && iSavedRequest.getStatus(course.getCourseId()) != RequestedCourseStatus.OVERRIDE_NEEDED) {
								style = (!rows.isEmpty() && !selfWaitListed ? "top-border-dashed": "");
								unassignedMessage = MESSAGES.conflictWaitListed(sDF.format(course.getWaitListedDate()));
							} else {
								style = "text-blue" + (!rows.isEmpty() && !selfWaitListed ? " top-border-dashed": "");
								unassignedMessage = MESSAGES.courseToBeWaitListed();
							}
							Request r = iCourseRequests.getWaitListedLine(course.getCourseId()).getValue();
							if (r.getWaitListSwapWithCourseOfferingId() != null && !selfWaitListed) {
								for (CourseAssignment c: result.getCourseAssignments()) {
									if (r.getWaitListSwapWithCourseOfferingId().equals(c.getCourseId()) && c.isAssigned() && !c.isTeachingAssignment())
										unassignedMessage += " " + MESSAGES.conflictWaitListSwapWithNoCourseOffering(c.getCourseName());
								}
							}
							RequestedCourse rc = r.getRequestedCourse(course.getCourseId());
							if (rc != null && (rc.hasSelectedIntructionalMethods() || rc.hasSelectedClasses())) {
								String pref = ToolBox.toString(rc.getRequiredPreferences());
								if (pref != null && !pref.isEmpty())
									unassignedMessage += "\n" + MESSAGES.conflictRequiredPreferences(pref);
							}
							if (course.getOverlaps()!=null && !course.getOverlaps().isEmpty()) {
								unassignedMessage += "\n<span class='unitime-ErrorText'>";
								boolean firstOverlap = true;
								for (Iterator<String> i = course.getOverlaps().iterator(); i.hasNext();) {
									String x = i.next();
									if (firstOverlap)
										unassignedMessage += MESSAGES.conflictWithFirst(x);
									else if (!i.hasNext())
										unassignedMessage += MESSAGES.conflictWithLast(x);
									else
										unassignedMessage += MESSAGES.conflictWithMiddle(x);
									firstOverlap = false;
								}
								if (course.getInstead() != null)
									unassignedMessage += MESSAGES.conflictAssignedAlternative(course.getInstead());
								unassignedMessage += ".</span>";
							}
							if (course.hasEnrollmentMessage() && (iSavedRequest == null || !iSavedRequest.hasConfirmations(course.getCourseName(), "WL-OVERLAP")))
								unassignedMessage += "\n" + course.getEnrollmentMessage();
							if (w.booleanValue() && !iSpecialRegistrationsPanel.canWaitList(course.getCourseId())) {
								unassignedMessage += "\n<span class='unitime-ErrorText'>" +
										MESSAGES.messageWaitListApprovalAlreadyRequested(course.getCourseName()) +
										"</span>";
							}
							RequestedCourseStatus status = (iSavedRequest == null ? null : iSavedRequest.getStatus(course.getCourseId()));
							if (status != null) {
								switch (status) {
								case OVERRIDE_NEEDED:
								case OVERRIDE_REJECTED:
									unassignedMessage += "<span class='unitime-ErrorText'>";
									break;
								case OVERRIDE_PENDING:
									unassignedMessage += "<span class='unitime-WarningText'>";
									break;
								case OVERRIDE_CANCELLED:
								case OVERRIDE_NOT_NEEDED:
								case WAITLIST_INACTIVE:
									unassignedMessage += "<span class='unitime-GrayText'>";
									break;
								case OVERRIDE_APPROVED:
									unassignedMessage += "<span class='unitime-GreenText'>";
									break;
								}
							}
							if (iSavedRequest != null)
								for (CourseMessage cm: iSavedRequest.getConfirmations(course.getCourseName()))
									if (!"WL-OVERLAP".equals(cm.getCode()))
										unassignedMessage += "\n" + cm.getMessage();
							if (status != null) {
								switch (status) {
								case OVERRIDE_NEEDED:
								case OVERRIDE_REJECTED:
								case OVERRIDE_PENDING:
								case OVERRIDE_CANCELLED:
								case WAITLIST_INACTIVE:
								case OVERRIDE_APPROVED:
								case OVERRIDE_NOT_NEEDED:
									unassignedMessage += "</span>";
									break;
								}
							}
							String msg = (iSavedRequest == null ? null : iSavedRequest.getConfirmation(course.getCourseName(), "\n"));
							if (status != null) {
								switch (status) {
								case OVERRIDE_NEEDED:
									icons.add(RESOURCES.requestNeeded(), MESSAGES.overrideNeeded(msg));
									break;
								case OVERRIDE_REJECTED:
									icons.add(RESOURCES.requestRejected(), (msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideRejectedWaitList(course.getCourseName()));
									break;
								case OVERRIDE_PENDING:
									icons.add(RESOURCES.requestPending(), (msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overridePending(course.getCourseName()));
									break;
								case WAITLIST_INACTIVE:
									icons.add(RESOURCES.waitListNotActive(), (msg == null ? "" : msg + "\n") + MESSAGES.waitListInactive(course.getCourseName()));
									break;
								case OVERRIDE_CANCELLED:
									icons.add(RESOURCES.requestCancelled(), (msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideCancelledWaitList(course.getCourseName()));
									break;
								case OVERRIDE_APPROVED:
									icons.add(RESOURCES.requestSaved(), (msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideApproved(course.getCourseName()));
									break;
								case OVERRIDE_NOT_NEEDED:
									icons.add(RESOURCES.requestNotNeeded(), (msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideNotNeeded(course.getCourseName()));
									break;
								case SAVED:
									icons.add(RESOURCES.requestSaved(), MESSAGES.courseWaitListed());
									break;
								default:
									if (iSavedRequest != null && iSavedRequest.isError(course.getCourseName()))
										icons.add(RESOURCES.requestError(), msg);
								}
							}
							/*
							RequestedCourse rc = iCourseRequests.getRequestedCourse(course.getCourseId());
							if (rc != null && rc.hasRequestorNote()) unassignedMessage += "\n<span class='unitime-GrayText'>" + rc.getRequestorNote() + "</span>";
							if (rc != null && rc.hasStatusNote()) unassignedMessage += "\n<span class='unitime-GrayText'>" + rc.getStatusNote() + "</span>";
							*/
						}
						waitList = new WebTable.CheckboxCell(w, MESSAGES.toggleWaitList(), ARIA.titleRequestedWaitListForCourse(MESSAGES.course(course.getSubject(), course.getCourseNbr())));
						waitList.getWidget().setStyleName("toggle");
						((CheckBox)waitList.getWidget()).addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								clearMessage();
								iCourseRequests.setWaitList(course.getCourseId(), event.getValue());
								
								LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
								CourseRequestInterface r = iCourseRequests.getRequest(); r.setNoChange(true);
								iSectioningService.section(r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
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
					
					if (selfWaitListed) {
						row = new WebTable.Row(
								new WebTable.Cell(null),
								new WebTable.Cell(null),
								new WebTable.Cell(null),
								new WebTable.PreCell(unassignedMessage, 11),
								waitList,
								new WebTable.Cell(null),
								icons);
						iLastResult.add(new ClassAssignment(course));
					} else {
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
										new WebTable.PreCell(unassignedMessage, 3),
										new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
										(waitList != null ? waitList : new WebTable.AbbvTextCell(clazz.getCredit())),
										(clazz.getGradeMode() == null ? new WebTable.Cell("") : new WebTable.Cell(clazz.getGradeMode().getCode()).title(clazz.getGradeMode().getLabel()).aria(clazz.getGradeMode().getLabel())),
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
										new WebTable.PreCell(unassignedMessage, 3),
										new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
										(waitList != null ? waitList : new WebTable.AbbvTextCell(clazz.getCredit())),
										(clazz.getGradeMode() == null ? new WebTable.Cell("") : new WebTable.Cell(clazz.getGradeMode().getCode()).title(clazz.getGradeMode().getLabel()).aria(clazz.getGradeMode().getLabel())),
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
										new WebTable.PreCell(unassignedMessage, 7),
										new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
										(waitList != null ? waitList : new WebTable.AbbvTextCell(clazz.getCredit())),
										(clazz.getGradeMode() == null ? new WebTable.Cell("") : new WebTable.Cell(clazz.getGradeMode().getCode()).title(clazz.getGradeMode().getLabel()).aria(clazz.getGradeMode().getLabel())),
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
					}
					if (row == null) {
						if (waitList != null) {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.getSubject()),
									new WebTable.Cell(course.getCourseNbr(CONSTANTS.showCourseTitle())),
									new WebTable.PreCell(unassignedMessage, 11),
									waitList,
									new WebTable.Cell(null),
									icons);
						} else {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.getSubject()),
									new WebTable.Cell(course.getCourseNbr(CONSTANTS.showCourseTitle())),
									new WebTable.PreCell(unassignedMessage, 12),
									new WebTable.Cell(null),
									icons);
						}
						row.setId(course.getCourseId().toString());
						row.setAriaLabel(ARIA.courseUnassginment(MESSAGES.course(course.getSubject(), course.getCourseNbr()), unassignedMessage));
						iLastResult.add(course.addClassAssignment());
					}
					for (WebTable.Cell cell: row.getCells())
						cell.setStyleName(style);
					row.getCell(row.getNrCells() - 1).setStyleName("text-red-centered" + (!rows.isEmpty() && !selfWaitListed ? " top-border-dashed": ""));
					rows.add(row);
					firstClazz = false;
				}
				if (iSavedAssignment != null && !course.isFreeTime() && !course.isTeachingAssignment() && iShowUnassignments.getValue()) {
					for (ClassAssignmentInterface.CourseAssignment saved: iSavedAssignment.getCourseAssignments()) {
						if (!saved.isAssigned() || saved.isFreeTime() || saved.isTeachingAssignment() || !course.getCourseId().equals(saved.getCourseId())) continue;
						classes: for (ClassAssignmentInterface.ClassAssignment clazz: saved.getClassAssignments()) {
							for (ClassAssignmentInterface.ClassAssignment x: course.getClassAssignments())
								if (clazz.getClassId().equals(x.getClassId())) continue classes;
							String style = "text-gray";
							WebTable.Row row = null;
							if (firstClazz && !rows.isEmpty()) style += " top-border-dashed";
							WebTable.IconsCell icons = new WebTable.IconsCell();
							GradeMode gradeMode = clazz.getGradeMode();
							Float creditHour = null;
							SpecialRegistrationStatus specRegStatus = iSpecialRegistrationsPanel.getStatus(clazz);
							if (specRegStatus != null) {
								String error = iSpecialRegistrationsPanel.getError(clazz);
								GradeMode gm = iSpecialRegistrationsPanel.getGradeMode(clazz);
								if (gm != null && gradeMode != null) gradeMode = gm;
								Float ch = iSpecialRegistrationsPanel.getCreditHours(clazz);
								if (ch != null) creditHour = ch;
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
								icons.add(RESOURCES.error(), clazz.getError(), true);
							} else if (clazz.hasWarn()) {
								icons.add(RESOURCES.warning(), clazz.getWarn(), true);
							} else if (clazz.hasInfo()) {
								icons.add(RESOURCES.info(), clazz.getInfo(), true);
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
										new WebTable.Cell(firstClazz ? course.getSubject() : "").aria(course.getSubject()),
										new WebTable.Cell(firstClazz ? course.getCourseNbr(CONSTANTS.showCourseTitle()) : "").aria(course.getCourseNbr(CONSTANTS.showCourseTitle())),
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
										(creditHour != null ? new WebTable.Cell(MESSAGES.credit(creditHour)) : new WebTable.AbbvTextCell(clazz.getCredit())),
										(gradeMode == null ? new WebTable.Cell("") : new WebTable.Cell(gradeMode.getCode()).title(gradeMode.getLabel()).aria(gradeMode.getLabel())),
										icons);								
							} else {
								row = new WebTable.Row(
										new WebTable.Cell(null),
										new WebTable.Cell(firstClazz ? course.getSubject() : "").aria(course.getSubject()),
										new WebTable.Cell(firstClazz ? course.getCourseNbr(CONSTANTS.showCourseTitle()) : "").aria(course.getCourseNbr(CONSTANTS.showCourseTitle())),
										new WebTable.Cell(clazz.getSubpart()),
										new WebTable.Cell(clazz.getSection()),
										new WebTable.Cell(clazz.getLimitString()),
										new WebTable.Cell(MESSAGES.arrangeHours(), 3, null),
										new WebTable.Cell(clazz.hasDatePattern() ? clazz.getDatePattern() : ""),
										(clazz.hasDistanceConflict() ? new WebTable.RoomCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(), ", ") : new WebTable.RoomCell(clazz.getRooms(), ", ")),
										new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
										new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
										new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
										(creditHour != null ? new WebTable.Cell(MESSAGES.credit(creditHour)) : new WebTable.AbbvTextCell(clazz.getCredit())),
										(gradeMode == null ? new WebTable.Cell("") : new WebTable.Cell(gradeMode.getCode()).title(gradeMode.getLabel()).aria(gradeMode.getLabel())),
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
						GradeMode gradeMode = clazz.getGradeMode();
						Float creditHour = null;
						SpecialRegistrationStatus specRegStatus = iSpecialRegistrationsPanel.getStatus(clazz);
						if (specRegStatus != null) {
							String error = iSpecialRegistrationsPanel.getError(clazz);
							GradeMode gm = iSpecialRegistrationsPanel.getGradeMode(clazz);
							if (gm != null && gradeMode != null) gradeMode = gm;
							Float ch = iSpecialRegistrationsPanel.getCreditHours(clazz);
							if (ch != null) creditHour = ch;
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
							icons.add(RESOURCES.error(), clazz.getError(), true);
						} else if (clazz.hasWarn()) {
							icons.add(RESOURCES.warning(), clazz.getWarn(), true);
						} else if (clazz.hasInfo()) {
							icons.add(RESOURCES.info(), clazz.getInfo(), true);
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
									(creditHour != null ? new WebTable.Cell(MESSAGES.credit(creditHour)) : new WebTable.AbbvTextCell(clazz.getCredit())),
									(gradeMode == null ? new WebTable.Cell("") : new WebTable.Cell(gradeMode.getCode()).title(gradeMode.getLabel()).aria(gradeMode.getLabel())),
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
									(creditHour != null ? new WebTable.Cell(MESSAGES.credit(creditHour)) : new WebTable.AbbvTextCell(clazz.getCredit())),
									(gradeMode == null ? new WebTable.Cell("") : new WebTable.Cell(gradeMode.getCode()).title(gradeMode.getLabel()).aria(gradeMode.getLabel())),
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
			iAssignments.setColumnVisible(15, hasGradeMode);
			if (LoadingWidget.getInstance().isShowing())
				LoadingWidget.getInstance().hide();
			iPanel.remove(iCourseRequests);
			iPanel.insert(iAssignmentDock, 1);
			iRequests.setVisible(true); iRequests.setEnabled(true);
			if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_RESET)) { iReset.setVisible(true); iReset.setEnabled(true); }
			if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.QUICK_ADD_DROP)) { iQuickAdd.setVisible(true); iQuickAdd.setEnabled(true); }
			iEnroll.setVisible(result.isCanEnroll() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL));
			if (hasGradeMode && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_CHANGE_GRADE_MODE, EligibilityFlag.CAN_CHANGE_VAR_CREDIT)) {
				iChangeGradeModes.setEnabled(true);
				iChangeGradeModes.setVisible(true);
				if (!iEligibilityCheck.hasFlag(EligibilityFlag.CAN_CHANGE_GRADE_MODE)) {
					iChangeGradeModes.setHTML(MESSAGES.buttonChangeVariableCredits());
					iChangeGradeModes.setTitle(MESSAGES.hintChangeVariableCredits());
				} else if (!iEligibilityCheck.hasFlag(EligibilityFlag.CAN_CHANGE_VAR_CREDIT)) {
					iChangeGradeModes.setHTML(MESSAGES.buttonChangeGradeModes());
					iChangeGradeModes.setTitle(MESSAGES.hintChangeGradeModes());
				} else {
					iChangeGradeModes.setHTML(MESSAGES.buttonChangeGradeModesAndVariableCredits());
					iChangeGradeModes.setTitle(MESSAGES.hintChangeGradeModesAndVariableCredits());
				}
			} else {
				iChangeGradeModes.setEnabled(false);
				iChangeGradeModes.setVisible(false);
			}
			if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_REQUEST_VAR_TITLE_COURSE)) {
				iRequestVarTitleCourse.setEnabled(true);
				iRequestVarTitleCourse.setVisible(true);
			} else {
				iRequestVarTitleCourse.setEnabled(false);
				iRequestVarTitleCourse.setVisible(false);
			}
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
			iPreferences.setVisible(false); iPreferences.setEnabled(false);
			iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
			iAdvisorReqs.setVisible(false); iAdvisorReqs.setEnabled(false);
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
					iStatus.error(result.getMessages("<br>"), popup);
				} else if (hasWarning) {
					iStatus.warning(result.getMessages("<br>"), popup);
				} else { 
					iStatus.info(result.getMessages("<br>"), popup);
				}
			} else {
				updateScheduleChangedNoteIfNeeded();
			}
			iTotalCredit.setVisible(iCurrentCredit > 0f);
			iTotalCredit.setText(MESSAGES.totalCredit(iCurrentCredit));
		} else {
			iTotalCredit.setVisible(false);
			iStatus.error(MESSAGES.noSchedule());
			if (LoadingWidget.getInstance().isShowing())
				LoadingWidget.getInstance().hide();
		}
	}
	
	public void prev() {
		iPanel.remove(iAssignmentDock);
		iPanel.insert(iCourseRequests, 1);
		iRequests.setVisible(false); iRequests.setEnabled(false);
		iReset.setVisible(false); iReset.setEnabled(false);
		iQuickAdd.setVisible(false); iQuickAdd.setEnabled(false);
		iEnroll.setVisible(false); iEnroll.setEnabled(false);
		iChangeGradeModes.setVisible(false); iChangeGradeModes.setEnabled(false);
		iRequestVarTitleCourse.setVisible(false); iRequestVarTitleCourse.setEnabled(false);
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
		if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.HAS_ADVISOR_REQUESTS)) {
			iAdvisorReqs.setVisible(true); iAdvisorReqs.setEnabled(true);
		}
		if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.SHOW_SCHEDULING_PREFS)) {
			iPreferences.setVisible(true); iPreferences.setEnabled(true);
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
	
	public void checkEligibility(final AsyncCallback<OnlineSectioningInterface.EligibilityCheck> ret) {
		checkEligibility(ret, null);
	}
	
	public void checkEligibility(final AsyncCallback<OnlineSectioningInterface.EligibilityCheck> ret, String pin) {
		LoadingWidget.getInstance().show(MESSAGES.courseRequestsLoading());
		iStartOver.setVisible(false); iStartOver.setEnabled(false);
		iSpecRegCx.reset();
		iContext.setPin(pin);
		iSectioningService.checkEligibility(iContext, new AsyncCallback<OnlineSectioningInterface.EligibilityCheck>() {
			@Override
			public void onSuccess(OnlineSectioningInterface.EligibilityCheck result) {
				clearMessage(false);
				iEligibilityCheck = result;
				iContext.setStudentId(result == null ? null : result.getStudentId());
				iSpecRegCx.update(result);
				iCourseRequests.setWaitListMode(
						result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_WAITLIST) ? WaitListMode.WaitList :
						result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_NO_SUBS) ? WaitListMode.NoSubs : WaitListMode.None);
				iCourseRequests.setArrowsVisible(!result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.NO_REQUEST_ARROWS),
						result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_NO_SUBS)
						&& !result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_WAITLIST));
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
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.HAS_ADVISOR_REQUESTS)) {
									iAdvisorReqs.setVisible(true); iAdvisorReqs.setEnabled(true);
								}
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.SHOW_SCHEDULING_PREFS)) {
									iPreferences.setVisible(true); iPreferences.setEnabled(true);
								}
								lastRequest(true);
								if (ret != null) ret.onSuccess(iEligibilityCheck);
							}
							@Override
							public void onSuccess(OnlineSectioningInterface.EligibilityCheck result) {
								iCourseRequests.setWaitListMode(
										result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_WAITLIST) ? WaitListMode.WaitList :
										result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_NO_SUBS) ? WaitListMode.NoSubs : WaitListMode.None);
								iCourseRequests.setArrowsVisible(!result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.NO_REQUEST_ARROWS),
										result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_NO_SUBS)
										&& !result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_WAITLIST));
								iEligibilityCheck = result;
								iContext.setStudentId(result == null ? null : result.getStudentId());
								
								iSpecRegCx.update(result);
								iSchedule.setVisible(iMode.isSectioning()); iSchedule.setEnabled(iMode.isSectioning());
								iSave.setVisible(!iMode.isSectioning()); iSave.setEnabled(!iMode.isSectioning() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_REGISTER));
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.DEGREE_PLANS)) {
									iDegreePlan.setVisible(true); iDegreePlan.setEnabled(true);
								}
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.HAS_ADVISOR_REQUESTS)) {
									iAdvisorReqs.setVisible(true); iAdvisorReqs.setEnabled(true);
								}
								if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.SHOW_SCHEDULING_PREFS)) {
									iPreferences.setVisible(true); iPreferences.setEnabled(true);
								}
								lastRequest(true);
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
						iPinDialog.checkEligibility(iContext, callback, iSessionSelector.getAcademicSessionInfo());
					} else {
						iSchedule.setVisible(iMode.isSectioning()); iSchedule.setEnabled(iMode.isSectioning());
						iSave.setVisible(!iMode.isSectioning()); iSave.setEnabled(!iMode.isSectioning() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_REGISTER));
						if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.DEGREE_PLANS)) {
							iDegreePlan.setVisible(true); iDegreePlan.setEnabled(true);
						}
						if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.HAS_ADVISOR_REQUESTS)) {
							iAdvisorReqs.setVisible(true); iAdvisorReqs.setEnabled(true);
						}
						if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.SHOW_SCHEDULING_PREFS)) {
							iPreferences.setVisible(true); iPreferences.setEnabled(true);
						}
						lastRequest(true);
						if (ret != null) ret.onSuccess(iEligibilityCheck);
					}
				} else {
					iCourseRequests.setWaitListMode(WaitListMode.None);
					LoadingWidget.getInstance().hide();
					if (result.hasMessage()) {
						iStatus.error(result.getMessage());
					}
					iSchedule.setVisible(false);  iSchedule.setEnabled(false);
					iSave.setVisible(false); iSave.setEnabled(false);
					iPreferences.setVisible(false); iPreferences.setEnabled(false);
					iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
					iAdvisorReqs.setVisible(false); iAdvisorReqs.setEnabled(false);
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
	
	private void lastResult(final CourseRequestInterface request, final boolean changeViewIfNeeded) {
		AsyncCallback<ClassAssignmentInterface> callback = new AsyncCallback<ClassAssignmentInterface>() {
			public void onFailure(Throwable caught) {
				iStatus.error(caught.getMessage(), caught);
				LoadingWidget.getInstance().hide();
			}
			public void onSuccess(final ClassAssignmentInterface saved) {
				iSavedAssignment = saved;
				iSpecialRegAssignment = null;
				iShowUnassignments.setVisible(true);
				if (iWaitListsPanel != null) iWaitListsPanel.populate(iSavedRequest, iSavedAssignment);
				if (request.isSaved() || !CONSTANTS.checkLastResult()) {
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
								iSectioningService.section(request, classes, new AsyncCallback<ClassAssignmentInterface>() {
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
				if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.HAS_SPECREG)) { // && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG)
					iSpecialRegistrationsPanel.showWaiting();
					iSectioningService.retrieveAllSpecialRequests(
							new RetrieveAllSpecialRegistrationsRequest(iContext),
							new AsyncCallback<List<RetrieveSpecialRegistrationResponse>>() {
								@Override
								public void onFailure(Throwable caught) {
									iStatus.error(MESSAGES.retrieveAllSpecialRegistrationsFail(caught.getMessage()), caught);
									iSpecialRegistrationsPanel.hideWaiting();
								}

								@Override
								public void onSuccess(List<RetrieveSpecialRegistrationResponse> response) {
									iSpecialRegistrationsPanel.hideWaiting();
									iSpecialRegistrationsPanel.populate(response, iSavedAssignment);
									if (!iSpecialRegistrationsPanel.isVisible())
										iStatus.info(MESSAGES.failedNoSpecialRegistrations());
									else if (iSpecialRegistrationsPanel.hasOneOrMoreFullyApproved() && (iStatus.getLevel() == null || iStatus.getLevel() == ScheduleStatus.Level.INFO))
										iStatus.info(MESSAGES.statusOneOrMoreFullyApprovedRequestsNotYetApplied());
									updateHistory();
								}
							});
				}
			}
		};
		iSpecialRegistrationsPanel.clearRegistrations();
		iSectioningService.savedResult(iContext, callback);
	}
	
	public void lastRequest(final boolean changeViewIfNeeded) {
		if (!LoadingWidget.getInstance().isShowing())
			LoadingWidget.getInstance().show(MESSAGES.courseRequestsLoading());
		
		AsyncCallback<CourseRequestInterface> callback =  new AsyncCallback<CourseRequestInterface>() {
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				clear(changeViewIfNeeded);
				if (MESSAGES.exceptionNoStudent().equals(caught.getMessage()) && iEligibilityCheck != null &&
					(iEligibilityCheck.hasFlag(EligibilityFlag.IS_ADMIN, EligibilityFlag.IS_ADVISOR, EligibilityFlag.IS_GUEST) || iEligibilityCheck.hasMessage())) {
					// do not show "No student." error for advisors and admins, or when the eligibility check already returned some other message
				} else {
					iStatus.error(caught.getMessage(), caught);
				}
			}
			public void onSuccess(final CourseRequestInterface request) {
				if (request.isSaved()) {
					iSavedRequest = request;
					if (iWaitListsPanel != null) iWaitListsPanel.populate(request, iSavedAssignment);
				} else if (!iMode.isSectioning() && iSavedRequest == null) {
					iSectioningService.savedRequest(iContext, new AsyncCallback<CourseRequestInterface>() {
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
				if (iWaitListsPanel != null) iWaitListsPanel.populate(iSavedRequest, iSavedAssignment);
				/*
				if (request.isSaved() && request.getCourses().isEmpty()) {
					LoadingWidget.getInstance().hide();
					return;
				}*/
				iCourseRequests.setRequest(request);
				updateHistory();
				if (iSchedule.isVisible() || iRequests.isVisible()) {
					lastResult(request, changeViewIfNeeded);
				} else {
					LoadingWidget.getInstance().hide();
					iStartOver.setVisible(true);
					iStartOver.setEnabled(true);
					if (request.hasErrorMessage() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL, EligibilityFlag.CAN_REGISTER))
						if (iEligibilityCheck.hasMessage())
							iStatus.warning(iEligibilityCheck.getMessage() + "\n" + request.getErrorMessaeg());
						else
							iStatus.warning(request.getErrorMessaeg());
					if (request.hasPopupMessage())
						UniTimeConfirmationDialog.info(request.getPopupMessage(), true);
				}
			}
		};
		
		iSectioningService.savedRequest(iContext, callback);
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
			iUser = iUserAuthentication.getUser();
			iMessage = iStatus.getMessage(); iMessageLevel = iStatus.getLevel();
			iTab = iAssignmentTab.getSelectedTab();
			iSRCx = new SpecialRegistrationContext(iSpecRegCx);
			iSRassignment = iSpecialRegAssignment;
		}
		
		public void restore() {
			if (isChanged() && ((iUser != null && !iUser.equals(iUserAuthentication.getUser())) || (iRequest.getSessionId() != null && !iRequest.getSessionId().equals(iSessionSelector.getAcademicSessionId())))) {
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
						iSessionSelector.selectSession(iRequest.getSessionId(), new AsyncCallback<Boolean>() {
							public void onSuccess(Boolean result) {
								if (result) {
									iSpecRegCx.copy(iSRCx);
									iContext.setSessionId(iRequest.getSessionId());
									iContext.setStudentId(iRequest.getStudentId());
									iSpecialRegAssignment = iSRassignment;
									if (iSpecialRegistrationsPanel.isVisible()) {
										iSpecialRegistrationsPanel.populate(iSpecialRegistrationsPanel.getRegistrations(), iSavedAssignment);
									}
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
		if (request.isSaved()) {
			iSavedRequest = request;
		}
		if (response != null) {
			if (request.isSaved()) {
				iSavedAssignment = response;
				iShowUnassignments.setVisible(true);
				if (iWaitListsPanel != null) iWaitListsPanel.populate(request, iSavedAssignment);
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
			return;
		}
		if (iLastAssignment != null && iSpecialRegAssignment != null) {
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
		if (iSavedRequest != null && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_WAITLIST)) {
			if (!iSavedRequest.sameWaitListedCourses(request)) {
				iScheduleChanged = true;
				iEnroll.addStyleName("unitime-EnrollButton");
				iStatus.warning(cr ? MESSAGES.warnScheduleChangedOnCourseRequest() : MESSAGES.warnScheduleChangedOnClassSchedule(), false);
			}
		}
	}
	
	public boolean isChanged() {
		return iScheduleChanged;
	}
	
	public void clearMessage() {
		clearMessage(true);
	}
	
	public void clearMessage(boolean showEligibility) {
		if (iEligibilityCheck != null && iContext.isOnline() && showEligibility) {
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
			}
		} else {
			iStatus.clear();
			iEnroll.removeStyleName("unitime-EnrollButton");
			iSave.removeStyleName("unitime-EnrollButton");
		}
		if (isChanged())
			updateScheduleChangedNoteIfNeeded();
	}
	
	protected void setElibibilityCheckDuringEnrollment(EligibilityCheck check) {
		iEligibilityCheck = check;
		iSpecRegCx.update(check);
		iCourseRequests.setWaitListMode(
				check.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_WAITLIST) ? WaitListMode.WaitList :
				check.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_NO_SUBS) ? WaitListMode.NoSubs : WaitListMode.None);
		iCourseRequests.setArrowsVisible(!check.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.NO_REQUEST_ARROWS),
				check.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_NO_SUBS)
				&& !check.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_WAITLIST));
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
	
	public String getCriticalCoursesToDrop() {
		if (iLastAssignment != null && iSavedAssignment != null && iSavedRequest != null) {
			boolean hasCrit = false, hasImp = false, hasVital = false;
			List<String> ret = new ArrayList<String>();
			for (ClassAssignmentInterface.CourseAssignment course: iSavedAssignment.getCourseAssignments()) {
				if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
				RequestPriority rp = iSavedRequest.getRequestPriority(course);
				if (rp == null || rp.isAlternative() || !rp.getRequest().isImportantOrMore()) continue;
				boolean hasCourse = false;
				for (RequestedCourse alt: rp.getRequest().getRequestedCourse()) {
					if (alt.getCourseId() == null) continue;
					for (ClassAssignmentInterface.CourseAssignment x: iLastAssignment.getCourseAssignments())
						if (alt.getCourseId().equals(x.getCourseId()) && x.isAssigned()) {
							hasCourse = true; break;
						}
				}
				if (!hasCourse) {
					if (rp.getRequest().isCritical()) hasCrit = true;
					if (rp.getRequest().isImportant()) hasImp = true;
					if (rp.getRequest().isVital()) hasVital = true;
					ret.add(MESSAGES.course(course.getSubject(), course.getCourseNbr()));
				}
			}
			if (hasCrit)
				return MESSAGES.confirmEnrollmentCriticalCourseDrop(ToolBox.toString(ret));
			if (hasVital)
				return MESSAGES.confirmEnrollmentVitalCourseDrop(ToolBox.toString(ret));
			if (hasImp)
				return MESSAGES.confirmEnrollmentImportantCourseDrop(ToolBox.toString(ret));
			return null;
		}
		return null;
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
	
	public List<String> getCourseChangesWithHonorsGradeMode() {
		if (iLastAssignment != null && iSavedAssignment != null) {
			List<String> ret = new ArrayList<String>();
			courses: for (ClassAssignmentInterface.CourseAssignment course: iSavedAssignment.getCourseAssignments()) {
				if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
				classes: for (ClassAssignmentInterface.ClassAssignment ca: course.getClassAssignments()) {
					if (ca.getGradeMode() != null && ca.getGradeMode().isHonor()) {
						for (ClassAssignmentInterface.CourseAssignment x: iLastAssignment.getCourseAssignments())
							if (course.getCourseId().equals(x.getCourseId()) && x.isAssigned())
								for (ClassAssignmentInterface.ClassAssignment y: x.getClassAssignments())
									if (ca.getClassId().equals(y.getClassId())) continue classes;
						ret.add(MESSAGES.course(course.getSubject(), course.getCourseNbr()));
						continue courses;
					}
				}
			}
			return ret;
		}
		return null;
	}
	
	public List<String> getCourseChangesWithVarbiableCredit() {
		if (iLastAssignment != null && iSavedAssignment != null) {
			List<String> ret = new ArrayList<String>();
			courses: for (ClassAssignmentInterface.CourseAssignment course: iSavedAssignment.getCourseAssignments()) {
				if (!course.isAssigned() || course.isFreeTime() || course.isTeachingAssignment()) continue;
				classes: for (ClassAssignmentInterface.ClassAssignment ca: course.getClassAssignments()) {
					if (ca.getCreditHour() != null && ca.hasVariableCredit() && ca.getCreditMin() < ca.getCreditHour()) {
						for (ClassAssignmentInterface.CourseAssignment x: iLastAssignment.getCourseAssignments())
							if (course.getCourseId().equals(x.getCourseId()) && x.isAssigned())
								for (ClassAssignmentInterface.ClassAssignment y: x.getClassAssignments())
									if (ca.getClassId().equals(y.getClassId())) continue classes;
						ret.add(MESSAGES.course(course.getSubject(), course.getCourseNbr()));
						continue courses;
					}
				}
			}
			return ret;
		}
		return null;
	}
	
	public boolean useDefaultConfirmDialog() {
		return iEligibilityCheck == null || !iEligibilityCheck.hasFlag(EligibilityFlag.GWT_CONFIRMATIONS);
	}
	
	protected Command confirmEnrollment(final Command callback) {
		return confirmEnrollmentDrop(confirmWaitListDrop(confirmEnrollmentHonors(confirmEnrollmentVariableCredits(confirmSectionSwapNoPref(callback)))));
	}

	protected Command confirmEnrollmentDrop(final Command callback) {
		if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CONFIRM_DROP)) {
			final String critical = getCriticalCoursesToDrop();
			if (critical != null) {
				return new Command() {
					@Override
					public void execute() {
						UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), critical, callback);
					}
				};
			}
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
	
	public List<String> getCoursesToDropWaitList() {
		if (iSavedRequest != null && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_WAITLIST)) {
			List<String> ret = new ArrayList<String>();
			r: for (Request r: iSavedRequest.getCourses()) {
				if (r.isWaitList() && r.hasRequestedCourse()) {
					for (RequestedCourse rc: r.getRequestedCourse())
						if (rc.getStatus() == RequestedCourseStatus.ENROLLED) continue r; 
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (rc.isCanWaitList() && rc.hasCourseId() && !Boolean.TRUE.equals(iCourseRequests.getWaitList(rc.getCourseId()))) {
							if (rc.getStatus() == RequestedCourseStatus.SAVED || rc.getStatus() == RequestedCourseStatus.OVERRIDE_APPROVED) {
								ret.add(rc.getCourseName());
							}
						}
					}
				}
			}
			return ret;
		}
		return null;
	}
	
	public Command confirmWaitListDrop(final Command callback) {
		if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CONFIRM_DROP)) {
			final List<String> wl = getCoursesToDropWaitList();
			if (wl != null && !wl.isEmpty()) {
				return new Command() {
					@Override
					public void execute() {
						UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), MESSAGES.confirmCourseDropFromWaitList(ToolBox.toString(wl)), callback);
					}
				};
			}
		}
		return callback;
	}
	
	protected Command confirmEnrollmentHonors(final Command callback) {
		if (iEligibilityCheck != null && iEligibilityCheck.hasGradeModes()) {
			final List<String> changes = getCourseChangesWithHonorsGradeMode();
			if (changes != null && !changes.isEmpty()) {
				return new Command() {
					@Override
					public void execute() {
						UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), MESSAGES.confirmEnrollmentHonorsGradeModeChange(ToolBox.toString(changes)), callback);
					}
				};
			}
		}
		return callback;
	}
	
	protected Command confirmEnrollmentVariableCredits(final Command callback) {
		if (iEligibilityCheck != null && iEligibilityCheck.hasGradeModes()) {
			final List<String> changes = getCourseChangesWithVarbiableCredit();
			if (changes != null && !changes.isEmpty()) {
				return new Command() {
					@Override
					public void execute() {
						UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), MESSAGES.confirmEnrollmentVariableCreditChange(ToolBox.toString(changes)), callback);
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
			CourseFinderCourses courses = new CourseFinderCourses(CONSTANTS.showCourseTitle(), CONSTANTS.courseFinderSuggestWhenEmpty(), CONSTANTS.courseFinderShowRequired(), iSpecRegCx);
			courses.setDataProvider(new DataProvider<String, Collection<CourseAssignment>>() {
				@Override
				public void getData(String source, AsyncCallback<Collection<CourseAssignment>> callback) {
					iSectioningService.listCourseOfferings(iContext, source, null, callback);
				}
			});
			CourseFinderDetails details = new CourseFinderDetails();
			details.setDataProvider(new DataProvider<CourseAssignment, String>() {
				@Override
				public void getData(CourseAssignment source, AsyncCallback<String> callback) {
					iSectioningService.retrieveCourseDetails(iContext, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
				}
			});
			CourseFinderClasses classes = new CourseFinderClasses(true, iSpecRegCx, courses.getRequiredCheckbox());
			classes.setDataProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
				@Override
				public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
					iSectioningService.listClasses(iContext, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
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
					if (iCourseRequests.hasCourseActive(event.getSelectedItem())) {
						UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), MESSAGES.confirmQuickDrop(event.getSelectedItem().getCourseName()), new Command() {
							@Override
							public void execute() {
								final CourseRequestInterface undo = iCourseRequests.getRequest();
								iCourseRequests.dropCourse(event.getSelectedItem());
								LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
								CourseRequestInterface r = iCourseRequests.getRequest(); r.setNoChange(true);
								iSectioningService.section(r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
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
							iQuickAddSuggestions = new SuggestionsBox(iAssignmentGrid.getColorProvider(), iSpecRegCx);
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
							iQuickAddSuggestions.addWaitListHandler(new SuggestionsBox.WaitListHandler() {
								@Override
								public void onWaitList(final SuggestionsBox.WaitListEvent event) {
									final CourseRequestInterface undo = iCourseRequests.getRequest();
									Request request = new Request(); request.setWaitList(true); request.addRequestedCourse(event.getCourse());
									iCourseRequests.addRequest(request);
									LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
									CourseRequestInterface r = iCourseRequests.getRequest(); r.setNoChange(true);
									iSectioningService.section(r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
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
						}
						iQuickAddSuggestions.open(iCourseRequests.getRequest(), iLastResult, event.getSelectedItem(), useDefaultConfirmDialog(), new AsyncCallback<ClassAssignmentInterface>() {
							@Override
							public void onSuccess(ClassAssignmentInterface result) {
								clearMessage();
								if (!iCourseRequests.hasCourseActive(event.getSelectedItem()))
									iCourseRequests.addCourse(event.getSelectedItem());
								fillIn(result);
								addHistory();
								iQuickAddFinder.setValue(null, true);
							}
							
							@Override
							public void onFailure(Throwable caught) {
								if (caught != null) iStatus.error(caught.getMessage());
								iAssignmentPanel.setFocus(true);
								if (event.getSelectedItem().isCanWaitList() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_WAITLIST)) {
									UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), caught.getMessage() + "\n" + MESSAGES.confirmQuickWaitList(event.getSelectedItem().getCourseName()), new Command() {
										@Override
										public void execute() {
											final CourseRequestInterface undo = iCourseRequests.getRequest();
											Request request = new Request(); request.setWaitList(true); request.addRequestedCourse(event.getSelectedItem());
											iCourseRequests.addRequest(request);
											LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
											CourseRequestInterface r = iCourseRequests.getRequest(); r.setNoChange(true);
											iSectioningService.section(r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
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
								}
							}
						});
					}
				}
			});
		}
		return iQuickAddFinder;
	}
	
	protected void requestEnrollmentOverrides(final SpecialRegistrationEligibilityResponse eligibilityResponse, final Command callWhenDone) {
		if (eligibilityResponse == null) {
			if (callWhenDone != null) callWhenDone.execute();
			return;
		}
		final Collection<ErrorMessage> errors = eligibilityResponse.getErrors();
		CheckCoursesResponse confirm = new CheckCoursesResponse();
		confirm.setConfirmation(0, MESSAGES.dialogRequestOverrides(),
				MESSAGES.buttonRequestOverrides(), MESSAGES.buttonCancelRequest(),
				MESSAGES.titleRequestOverrides(), MESSAGES.titleCancelRequest());
		confirm.addConfirmation(MESSAGES.messageRegistrationErrorsDetected(), 0, -1);
		confirm.addConfirmation(MESSAGES.messageRequestOverridesNote(), 0, 2);
		final Map<String, CourseRequestInterface.CourseMessage> notes = new HashMap<String, CourseRequestInterface.CourseMessage>();
		boolean hasCredit = false;
		for (ErrorMessage e: errors) {
			if ("IGNORE".equals(e.getCode())) continue;
			if ("MAXI".equals(e.getCode()) || "CREDIT".equals(e.getCode())) {
				hasCredit = true; continue;
			}
			if (e.getCourse() == null || e.getCourse().isEmpty()) continue;
			if (!notes.containsKey(e.getCourse())) {
				final CourseRequestInterface.CourseMessage note = confirm.addConfirmation("", 0, 3); note.setCode("REQUEST_NOTE");
				note.setCourse(e.getCourse());
				if (eligibilityResponse.hasSuggestions())
					for (String suggestion: eligibilityResponse.getSuggestions())
						note.addSuggestion(suggestion);
				notes.put(e.getCourse(), note);
			}
		}
		if (hasCredit) {
			final CourseRequestInterface.CourseMessage note = confirm.addConfirmation("", 0, 3); note.setCode("REQUEST_NOTE");
			note.setCourse(MESSAGES.tabRequestNoteMaxCredit());
			if (eligibilityResponse.hasSuggestions())
				for (String suggestion: eligibilityResponse.getSuggestions())
					note.addSuggestion(suggestion);
			notes.put("MAXI", note);
		}
		confirm.addConfirmation(MESSAGES.messageRequestOverridesOptions(), 0, 4);
		confirm.addConfirmation(MESSAGES.messageRequestOverridesDisclaimer(), 0, 7);
		if (iSpecRegCx.hasDisclaimer())
			confirm.addCheckBox(iSpecRegCx.getDisclaimer(), 0, 8);
		else
			confirm.addCheckBox(MESSAGES.messageRequestOverridesDisclaimerMessage(), 0, 8);
		for (ErrorMessage e: errors) {
			if ("IGNORE".equals(e.getCode())) {
				continue;
			} else if (iEligibilityCheck.hasOverride(e.getCode())) {
				confirm.addMessage(null, e.getCourse(), e.getCode(), e.getMessage(), 0);
			} else {
				confirm.addMessage(null, e.getCourse(), e.getCode(), e.getMessage(), 0);
			}
		}
		if (eligibilityResponse.hasCancelErrors()) {
			confirm.addConfirmation(MESSAGES.messageRequestOverridesCancel(), 0, 5);
			for (ErrorMessage e: eligibilityResponse.getCancelErrors())
				confirm.addMessage(null, e.getCourse(), e.getCode(), e.getMessage(), 0, 6);
		}
		CourseRequestsConfirmationDialog.confirm(confirm, 0, new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				if (result) {
					clearMessage();
					LoadingWidget.getInstance().show(MESSAGES.waitSpecialRegistration());
					Map<String, String> requestNotes = new HashMap<String, String>();
					for (Map.Entry<String, CourseRequestInterface.CourseMessage> e: notes.entrySet()) {
						requestNotes.put(e.getKey(), e.getValue().getMessage());
					}
					iSectioningService.submitSpecialRequest(
							new SubmitSpecialRegistrationRequest(iContext, iSpecRegCx.getRequestId(), iCourseRequests.getRequest(),
									iLastEnrollment != null ? iLastEnrollment : iLastResult, errors, requestNotes, eligibilityResponse.getCredit()),
							new AsyncCallback<SubmitSpecialRegistrationResponse>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									iStatus.error(MESSAGES.submitSpecialRegistrationFail(caught.getMessage()), caught);
									updateHistory();
									if (callWhenDone != null) callWhenDone.execute();
								}

								@Override
								public void onSuccess(SubmitSpecialRegistrationResponse response) {
									LoadingWidget.getInstance().hide();
									if (response.isSuccess()) {
										iSpecialRegAssignment = iLastAssignment;
									}
									iSpecRegCx.setStatus(response.getStatus());
									iSpecRegCx.setRequestId(response.getRequestId());
									if (response.hasRequests()) {
										List<RetrieveSpecialRegistrationResponse> requests = new ArrayList<RetrieveSpecialRegistrationResponse>(response.getRequests());
										for (RetrieveSpecialRegistrationResponse r: iSpecialRegistrationsPanel.getRegistrations()) {
											if (eligibilityResponse.isToBeCancelled(r.getRequestId())) continue;
											if (response.isCancelledRequest(r.getRequestId())) continue;
											if (response.hasRequest(r.getRequestId())) continue;
											requests.add(r);
										}
										Collections.sort(requests);
										iSpecialRegistrationsPanel.populate(requests, iSavedAssignment);
									}
									if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG)) {
										iEligibilityCheck.setFlag(EligibilityFlag.HAS_SPECREG, true);
									}
									for (CourseAssignment ca: iLastAssignment.getCourseAssignments())
										for (ClassAssignment a: ca.getClassAssignments()) {
											a.setError(null); a.setWarn(null); a.setInfo(null);
											a.setSpecRegStatus(null);
											for (ErrorMessage f: errors) {
												if (a.getExternalId() != null && a.getExternalId().equals(f.getSection())) {
													a.addError(f.getMessage());
													a.setSpecRegStatus(response.getStatus());
												}
											}
										}
									fillIn(iLastAssignment);
									if (response.isSuccess()) {
										iStatus.done(response.hasMessage() ? response.getMessage() : MESSAGES.submitSecialRegistrationOK());
									} else {
										iStatus.error(response.getMessage());
									}
									updateHistory();
									if (callWhenDone != null) callWhenDone.execute();
								}
							});
				} else {
					if (callWhenDone != null) callWhenDone.execute();
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				iStatus.error(MESSAGES.requestSpecialRegistrationFail(caught.getMessage()), caught);
				if (callWhenDone != null) callWhenDone.execute();
			}
		});
	}
	
	protected void checkSpecialRegistrationAfterFailedSubmitSchedule(ArrayList<ClassAssignmentInterface.ClassAssignment> lastEnrollment, Throwable exception, ClassAssignmentInterface result, final Command callWhenDone) {
		iLastEnrollment = lastEnrollment;
		final EnrollmentConfirmationDialog dialog = new EnrollmentConfirmationDialog(exception, result, new AsyncCallback<SpecialRegistrationEligibilityResponse>() {
			@Override
			public void onSuccess(SpecialRegistrationEligibilityResponse result) {
				requestEnrollmentOverrides(result, callWhenDone);
			}
			@Override
			public void onFailure(Throwable caught) {
				if (callWhenDone != null) callWhenDone.execute();
			}
		});
		dialog.center();
		iSectioningService.checkSpecialRequestEligibility(
				new SpecialRegistrationEligibilityRequest(iContext, iSpecRegCx.getRequestId(), iLastEnrollment, iLastAssignment == null ? null : iLastAssignment.getErrors()),
				new AsyncCallback<SpecialRegistrationEligibilityResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						dialog.showError(caught.getMessage());
					}

					@Override
					public void onSuccess(final SpecialRegistrationEligibilityResponse eligibilityResponse) {
						dialog.setResponse(eligibilityResponse);
					}
				});
	}
	
	protected void checkWaitListAfterSubmitSchedule() {
		if (iSavedAssignment.getRequest() != null && iSavedAssignment.getRequest().hasWaitListChecks()) {
			final CheckCoursesResponse lastCheck = iSavedAssignment.getRequest().getWaitListChecks();
			iCourseRequests.setLastCheck(lastCheck);
			if (lastCheck.isError()) {
				iStatus.error(lastCheck.getErrorMessage());
				return;
			}
			if (lastCheck.hasMessages()) {
				String error = null;
				for (CourseMessage m: lastCheck.getMessages()) {
					if (m.isConfirm() && !iSpecialRegistrationsPanel.canWaitList(m.getCourseId())) {
						if (error == null)
							error = MESSAGES.errorWaitListApprovalAlreadyRequested(m.getCourse());
						else if (!error.contains(MESSAGES.errorWaitListApprovalAlreadyRequested(m.getCourse())))
							error += "\n" + MESSAGES.errorWaitListApprovalAlreadyRequested(m.getCourse());
					}
				}
				if (error != null) {
					iStatus.error(error + "\n" + MESSAGES.errorWaitListApprovalCancelFirst());
					return;
				}
			}
			if (lastCheck.isConfirm()) {
				final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if (result) {
							LoadingWidget.getInstance().show(MESSAGES.waitRequestWaitListOverrides());
							iSectioningService.waitListSubmitOverrides(iSavedAssignment.getRequest(), lastCheck.getMaxCreditNeeded(), new AsyncCallback<CourseRequestInterface>() {
								@Override
								public void onSuccess(CourseRequestInterface result) {
									iSavedRequest = result;
									if (iWaitListsPanel != null) iWaitListsPanel.populate(result, iSavedAssignment);
									iCourseRequests.setValue(result, false);
									iCourseRequests.notifySaveSucceeded();
									fillIn(iLastAssignment, false);
									iStatus.done(MESSAGES.waitListOverridesRequested());
									LoadingWidget.getInstance().hide();
									updateHistory();
								}
								
								public void onFailure(Throwable caught) {
									iStatus.error(MESSAGES.failedRequestWaitListOverrides(caught.getMessage()), caught);
									LoadingWidget.getInstance().hide();
									updateHistory();
								}
							});
						} else {
							iStatus.warning(MESSAGES.waitListOverridesNotRequested());
						}
					}
					@Override
					public void onFailure(Throwable caught) {}
				};
				final Iterator<Integer> it = lastCheck.getConfirms().iterator();
				new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {}
					@Override
					public void onSuccess(Boolean accept) {
						if (accept && it.hasNext()) {
							CourseRequestsConfirmationDialog.confirm(lastCheck, it.next(), this);
						} else {
							callback.onSuccess(accept);
						}
					}
				}.onSuccess(true);
			}
		}
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
				new WebTable.Cell(iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_NO_SUBS) ? StudentSectioningWidget.MESSAGES.colNoSubs() : StudentSectioningWidget.MESSAGES.colWaitList(), 1, "20px")
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
					String msg = check.getMessage(rc.getCourseName(), "\n", "CREDIT", "REQUEST_NOTE");
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
						case OVERRIDE_NOT_NEEDED:
							icon = RESOURCES.requestNotNeeded(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideNotNeeded(rc.getCourseName()));
							break;
						default:
							if (check.isError(rc.getCourseName()))
								icon = RESOURCES.requestError(); iconText = (msg);
						}
					}
					if (rc.hasRequestorNote()) iconText += "\n" + MESSAGES.requestNote(rc.getRequestorNote());
					if (rc.hasStatusNote()) iconText += "\n" + MESSAGES.overrideNote(rc.getStatusNote());
					Collection<Preference> prefs = null;
					if (rc.hasSelectedIntructionalMethods()) {
						if (rc.hasSelectedClasses()) {
							prefs = new ArrayList<Preference>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
							prefs.addAll(new TreeSet<Preference>(rc.getSelectedIntructionalMethods()));
							prefs.addAll(new TreeSet<Preference>(rc.getSelectedClasses()));
						} else {
							prefs = new TreeSet<Preference>(rc.getSelectedIntructionalMethods());
						}
					} else if (rc.hasSelectedClasses()) {
						prefs = new TreeSet<Preference>(rc.getSelectedClasses());
					}
					String status = "";
					if (rc.getStatus() != null) {
						switch (rc.getStatus()) {
						case ENROLLED: status = MESSAGES.reqStatusEnrolled(); break;
						case OVERRIDE_APPROVED: status = MESSAGES.reqStatusApproved(); break;
						case OVERRIDE_CANCELLED: status = MESSAGES.reqStatusCancelled(); break;
						case OVERRIDE_PENDING: status = MESSAGES.reqStatusPending(); break;
						case OVERRIDE_REJECTED: status = MESSAGES.reqStatusRejected(); break;
						case OVERRIDE_NOT_NEEDED: status = MESSAGES.reqStatusNotNeeded(); break;
						}
					}
					if (status.isEmpty()) status = MESSAGES.reqStatusRegistered();
					if (prefs != null) hasPref = true;
					WebTable.Cell credit = new WebTable.Cell(rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
					credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
					String note = null;
					if (check != null) note = check.getMessage(rc.getCourseName(), "\n", "CREDIT", "REQUEST_NOTE");
					if (rc.hasRequestorNote()) note = (note == null ? "" : note + "\n") + rc.getRequestorNote();
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
					String note = null;
					if (check != null)
						note = check.getMessage(CONSTANTS.freePrefix() + free, "<br>");
					P messages = new P("text-pre-wrap"); messages.setText(note);
					WebTable.Row row = new WebTable.Row(
							new WebTable.Cell(first ? MESSAGES.courseRequestsPriority(priority) : ""),
							new WebTable.Cell(CONSTANTS.freePrefix() + free, 3, null),
							new WebTable.Cell(""),
							new WebTable.WidgetCell(messages, note),
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
					String msg = check.getMessage(rc.getCourseName(), "\n", "CREDIT", "REQUEST_NOTE");
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
						case OVERRIDE_NOT_NEEDED:
							icon = RESOURCES.requestNotNeeded(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideNotNeeded(rc.getCourseName()));
							break;
						default:
							if (check.isError(rc.getCourseName()))
								icon = RESOURCES.requestError(); iconText = (msg);
						}
					}
					if (rc.hasRequestorNote()) iconText += "\n" + MESSAGES.requestNote(rc.getRequestorNote());
					if (rc.hasStatusNote()) iconText += "\n" + MESSAGES.overrideNote(rc.getStatusNote());
					Collection<Preference> prefs = null;
					if (rc.hasSelectedIntructionalMethods()) {
						if (rc.hasSelectedClasses()) {
							prefs = new ArrayList<Preference>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
							prefs.addAll(new TreeSet<Preference>(rc.getSelectedIntructionalMethods()));
							prefs.addAll(new TreeSet<Preference>(rc.getSelectedClasses()));
						} else {
							prefs = new TreeSet<Preference>(rc.getSelectedIntructionalMethods());
						}
					} else if (rc.hasSelectedClasses()) {
						prefs = new TreeSet<Preference>(rc.getSelectedClasses());
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
						case OVERRIDE_NOT_NEEDED: status = MESSAGES.reqStatusNotNeeded(); break;
						}
					}
					if (status.isEmpty()) status = MESSAGES.reqStatusRegistered();
					WebTable.Cell credit = new WebTable.Cell(rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
					credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
					String note = null;
					if (check != null) note = check.getMessage(rc.getCourseName(), "\n", "CREDIT", "REQUEST_NOTE");
					if (rc.hasRequestorNote()) note = (note == null ? "" : note + "\n") + rc.getRequestorNote();
					if (rc.hasStatusNote()) note = (note == null ? "" : note + "\n") + rc.getStatusNote();
					P messages = new P("text-pre-wrap"); messages.setText(note);
					WebTable.Row row = new WebTable.Row(
							new WebTable.Cell(first ? MESSAGES.courseRequestsAlternate(priority) : ""),
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
				icon = RESOURCES.requestNeeded();
				status = MESSAGES.reqStatusWarning();
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
			if (savedRequests.hasRequestorNote()) {
				note = (note == null ? "" : note + "\n") + savedRequests.getRequestorNote();
				hasWarn = true;
			}
			if (savedRequests.hasCreditNote()) {
				note = (note == null ? "" : note + "\n") + savedRequests.getCreditNote();
				hasWarn = true;
			}
			float[] range = savedRequests.getCreditRange(iEligibilityCheck == null ? null : iEligibilityCheck.getAdvisorWaitListedCourseIds());
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
		float[] range = savedRequests.getCreditRange(iEligibilityCheck == null ? null : iEligibilityCheck.getAdvisorWaitListedCourseIds());
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
	
	protected void changeGradeModes(ArrayList<ClassAssignmentInterface.ClassAssignment> lastEnrollment, List<RetrieveSpecialRegistrationResponse> approvals) {
		if (iChangeGradeModesDialog == null) {
			iChangeGradeModesDialog = new ChangeGradeModesDialog(iContext, iStatus) {
				protected void onChange(ChangeGradeModesResponse response) {
					if (response.hasGradeModes()) {
						for (CourseAssignment course: iSavedAssignment.getCourseAssignments())
							for (ClassAssignment ca: course.getClassAssignments()) {
								GradeMode mode = response.getGradeMode(ca);
								if (mode != null) ca.setGradeMode(mode);
							}
					}
					if (response.hasCreditHours()) {
						for (CourseAssignment course: iSavedAssignment.getCourseAssignments())
							for (ClassAssignment ca: course.getClassAssignments()) {
								Float creditHour = response.getCreditHour(ca);
								if (creditHour != null) {
									ca.setCreditHour(creditHour);
									ca.setCredit(creditHour == 0f ? "" : MESSAGES.credit(creditHour));
								}
							}
					}
					if (response.hasRequests()) {
						List<RetrieveSpecialRegistrationResponse> requests = new ArrayList<RetrieveSpecialRegistrationResponse>(response.getRequests());
						for (RetrieveSpecialRegistrationResponse r: iSpecialRegistrationsPanel.getRegistrations()) {
							if (response.isToBeCancelled(r.getRequestId())) continue;
							if (response.hasRequest(r.getRequestId())) continue;
							requests.add(r);
						}
						Collections.sort(requests);
						iSpecialRegistrationsPanel.populate(requests, iSavedAssignment);
						if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG)) {
							iEligibilityCheck.setFlag(EligibilityFlag.HAS_SPECREG, true);
						}
					}
					fillIn(iSavedAssignment);
					addHistory();
				}
			};
		}
		if (iEligibilityCheck != null) {
			if (!iEligibilityCheck.hasFlag(EligibilityFlag.CAN_CHANGE_GRADE_MODE)) {
				iChangeGradeModesDialog.setText(MESSAGES.dialogChangeVariableCredit());
			} else if (!iEligibilityCheck.hasFlag(EligibilityFlag.CAN_CHANGE_VAR_CREDIT)) {
				iChangeGradeModesDialog.setText(MESSAGES.dialogChangeGradeMode());
			} else {
				iChangeGradeModesDialog.setText(MESSAGES.dialogChangeGradeModeAndVariableCredit());
			}
		}
		iChangeGradeModesDialog.changeGradeModes(lastEnrollment, approvals);
	}
	
	protected void requestVariableTitleCourse() {
		if (iRequestVariableTitleCourseDialog == null) {
			iRequestVariableTitleCourseDialog = new RequestVariableTitleCourseDialog(iContext, iStatus) {
				protected void onChange(final VariableTitleCourseResponse response) {
					if (response.hasRequests()) {
						List<RetrieveSpecialRegistrationResponse> requests = new ArrayList<RetrieveSpecialRegistrationResponse>(response.getRequests());
						for (RetrieveSpecialRegistrationResponse r: iSpecialRegistrationsPanel.getRegistrations()) {
							if (response.isToBeCancelled(r.getRequestId())) continue;
							if (response.hasRequest(r.getRequestId())) continue;
							requests.add(r);
						}
						Collections.sort(requests);
						iSpecialRegistrationsPanel.populate(requests, iSavedAssignment);
						if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_SPECREG)) {
							iEligibilityCheck.setFlag(EligibilityFlag.HAS_SPECREG, true);
						}
					}
					if (response.getCourse() != null) {
						getQuickAddFinder().setValue(response.getCourse(), true);
						getQuickAddFinder().findCourse();
					}
				}
			};
		}
		iRequestVariableTitleCourseDialog.requestVariableTitleCourse(
				iEligibilityCheck == null || !iEligibilityCheck.hasCurrentCredit() ? iCurrentCredit : iEligibilityCheck.getCurrentCredit(),
				iEligibilityCheck == null ? null : iEligibilityCheck.getMaxCredit());
	}
	
	public void setSessionId(Long sessionId) {
		iContext.setSessionId(sessionId);
	}
	
	public void setStudentId(Long studentId) {
		iContext.setStudentId(studentId);
	}
	
	protected WaitListedRequestPreferences getWaitListedRequestPreferences() {
		if (iWaitListedRequestPreferences == null) {
			iWaitListedRequestPreferences = new WaitListedRequestPreferences(iContext) {
				@Override
				protected void onSubmit() {
					super.onSubmit();
					LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
					CourseRequestInterface r = iCourseRequests.getRequest(); r.setNoChange(true);
					iSectioningService.section(r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
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
			};
		}
		return iWaitListedRequestPreferences;
	}
	
	public List<String> getSectionSwapsNoPrefs() {
		if (iLastAssignment != null) {
			List<String> ret = new ArrayList<String>();
			courses: for (ClassAssignmentInterface.CourseAssignment course: iLastAssignment.getCourseAssignments()) {
				if (!course.isAssigned() || !course.isCanWaitList() || course.isFreeTime() || course.isTeachingAssignment()) continue;
				CourseRequestLine line = iCourseRequests.getWaitListedLine(course.getCourseId());
				Request r = (line == null ? null : line.getValue());
				if (r != null && r.isWaitList() && course.getCourseId().equals(r.getWaitListSwapWithCourseOfferingId())) {
					for (RequestedCourse rc: r.getRequestedCourse()) {
						if (!course.getCourseId().equals(rc.getCourseId())) continue courses; // has higher priority course
						if (rc.getRequiredPreferences().isEmpty()) {
							ret.add(course.getCourseName());
						}
						break;
					}
				}
			}
			return ret;
		}
		return null;
	}
	
	protected Command confirmSectionSwapNoPref(final Command callback) {
		if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_WAITLIST)) {
			final List<String> changes = getSectionSwapsNoPrefs();
			if (changes != null && !changes.isEmpty()) {
				return new Command() {
					@Override
					public void execute() {
						UniTimeConfirmationDialog.confirm(useDefaultConfirmDialog(), MESSAGES.confirmSectionSwapNoPrefs(ToolBox.toString(changes)), callback);
					}
				};
			}
		}
		return callback;
	}
}
