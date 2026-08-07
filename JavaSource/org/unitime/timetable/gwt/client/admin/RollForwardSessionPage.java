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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.exams.ReportQueueTable;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.CancelledClassAction;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.DistributionMode;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.IdLabel;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.Operation;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollAction;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardError;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardErrors;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardSessionRequest;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardSessionResponse;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.StudentEnrollmentMode;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueType;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class RollForwardSessionPage extends Composite {
	protected static CourseMessages MSG = GWT.create(CourseMessages.class);
	protected static GwtMessages GWT_MSG = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iPanel;
	private ReportQueueTable iQueue;
	private RollForwardSessionResponse iConfig;
	private UniTimeHeaderPanel iHeader, iFooter;
	private RollForwardSessionInterface iData;
	private int iFirstRow = -1;
	private ErrorsWidget iErrors;
	
	public RollForwardSessionPage() {
		iPanel = new SimpleForm(3);
		iPanel.addStyleName("unitime-RollForwardSessionPage");
		initWidget(iPanel);
		
		iQueue = new ReportQueueTable(QueueType.RollForward);
		iQueue.addMouseClickListener(new MouseClickListener<QueueItemInterface>() {
			@Override
			public void onMouseClick(TableEvent<QueueItemInterface> event) {
				if (event.getData() != null && iQueue.isSelected(event.getRow())) {
					History.newItem(event.getData().getId(), false);
					populate(event.getData().getId());
				} else {
					History.newItem("", false);
					clearForm();
				}
			}
		});
		iQueue.attach(iPanel, MSG.sectRollForwardsInProgress());
		iConfig = new RollForwardSessionResponse();
		iData = new RollForwardSessionInterface();

		iErrors = new ErrorsWidget();
		
		iHeader = new UniTimeHeaderPanel(MSG.sectRollForwardActions());
		iHeader.addButton("refresh", MSG.buttonRefresh(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				init();
				iQueue.refreshQueue(null);
			}
		});
		iHeader.addButton("execute", MSG.actionRollForward(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				executeRoll();
			}
		});
		iHeader.getButton("execute").setTitle(MSG.titleRollForward(MSG.accessRollForward()));
		iHeader.getButton("execute").setAccessKey(MSG.accessRollForward().charAt(0));
		iHeader.setEnabled("execute", false);
		iFooter = iHeader.clonePanel("");
		
		init();
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (event.getValue().isEmpty()) {
					if (iQueue.getSelectedRow() >= 0)
						iQueue.setSelected(iQueue.getSelectedRow(), false);
					iQueue.refreshQueue(null);
					clearForm();
				} else {
					iQueue.refreshQueue(event.getValue());
					populate(event.getValue());
				}
			}
		});
		if (History.getToken() != null && !History.getToken().isEmpty()) {
			iQueue.refreshQueue(History.getToken());
			populate(History.getToken());
		}
	}
	
	protected void clearForm() {
		iData.setRollForwardDepartments(false);
		iData.setRollForwardSessionConfig(false);
		iData.setRollForwardManagers(false);
		iData.setRollForwardRoomData(false);
		iData.setRollForwardDatePatterns(false);
		iData.setRollForwardTimePatterns(false);
		iData.setRollForwardLearningManagementSystems(false);
		iData.setRollForwardSubjectAreas(false);
		iData.setRollForwardInstructorData(false);
		iData.setRollForwardCourseOfferings(false);
		iData.setRollForwardClassInstructors(false);
		iData.setRollForwardOfferingCoordinators(false);
		iData.setRollForwardTeachingRequests(false);
		iData.setAddNewCourseOfferings(false);
		iData.setRollForwardExamConfiguration(false);
		iData.setRollForwardMidtermExams(false);
		iData.setRollForwardFinalExams(false);
		iData.setRollForwardStudents(false);
		iData.setRollForwardCurricula(false);
		iData.setRollForwardReservations(false);
		iData.setRollForwardPeriodicTasks(false);
		initForm();
		iErrors.clearErrors();
	}
	
	protected void init() {
		RPC.execute(new RollForwardSessionRequest(Operation.LOAD, iData), new AsyncCallback<RollForwardSessionResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(GWT_MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(RollForwardSessionResponse result) {
				iConfig = result;
				if (iConfig.getDates() != null) {
					iData.setExpirationCourseReservations(iConfig.getDates().getExpirationDate("course"));
					iData.setExpirationCurriculumReservations(iConfig.getDates().getExpirationDate("curriculum"));
					iData.setExpirationGroupReservations(iConfig.getDates().getExpirationDate("group"));
					iData.setExpirationUniversalReservations(iConfig.getDates().getExpirationDate("universal"));
					iData.setStartDateCourseReservations(iConfig.getDates().getStartDate("course"));
					iData.setStartDateCurriculumReservations(iConfig.getDates().getStartDate("curriculum"));
					iData.setStartDateGroupReservations(iConfig.getDates().getStartDate("group"));
					iData.setStartDateUniversalReservations(iConfig.getDates().getStartDate("universal"));
				}
				iData.setSessionToRollForwardTo(iConfig.getToSessionId());
				initForm();
			}
		});		
	}
	
	protected void populate(final String id) {
		iHeader.showLoading();
		RPC.execute(new RollForwardSessionRequest(Operation.POPULATE, id), new AsyncCallback<RollForwardSessionResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.clearMessage();
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(RollForwardSessionResponse result) {
				iHeader.clearMessage();
				if (result.getData() != null) {
					iData = result.getData();
					initForm();
					iErrors.clearErrors();
				}
			}
		});
	}
	
	protected void executeRoll() {
		if (!validateRoll()) return;
		LoadingWidget.getInstance().show(GWT_MSG.waitPlease());
		iHeader.clearMessage();
		RPC.execute(new RollForwardSessionRequest(Operation.EXECUTE, iData), new AsyncCallback<RollForwardSessionResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(RollForwardSessionResponse result) {
				LoadingWidget.getInstance().hide();
				iQueue.refreshQueue(result.getQueueId());
			}
		});
	}
	
	protected boolean validateRoll() {
		List<String> errors = new ArrayList<String>();
		if (iData.getSessionToRollForwardTo() == null)
			errors.add(MSG.errorRollForwardMissingToSession());
		boolean oneSelected = false;
		if (iData.getRollForwardDepartments()) {
			oneSelected = true;
			if (iData.getSessionToRollDeptsFowardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardDepartments()));
		}
		if (iData.getRollForwardSessionConfig()) {
			oneSelected = true;
			if (iData.getSessionToRollSessionConfigForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardSessionConfiguration()));
		}
		if (iData.getRollForwardManagers()) {
			oneSelected = true;
			if (iData.getSessionToRollManagersForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardManagers()));
		}
		if (iData.getRollForwardRoomData()) {
			oneSelected = true;
			if (iData.getSessionToRollRoomDataForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardRooms()));
		}
		if (iData.getRollForwardDatePatterns()) {
			oneSelected = true;
			if (iData.getSessionToRollDatePatternsForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardDatePatterns()));
		}
		if (iData.getRollForwardTimePatterns()) {
			oneSelected = true;
			if (iData.getSessionToRollTimePatternsForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardTimePatterns()));
		}
		if (iData.getRollForwardLearningManagementSystems()) {
			oneSelected = true;
			if (iData.getSessionToRollLearningManagementSystemsForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardLMS()));
		}
		if (iData.getRollForwardSubjectAreas()) {
			oneSelected = true;
			if (iData.getSessionToRollSubjectAreasForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardSubjectAreas()));
		}
		if (iData.getRollForwardInstructorData()) {
			oneSelected = true;
			if (iData.getSessionToRollInstructorDataForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardInstructors()));
			if (!iData.hasRollForwardDepartmentIds())
				errors.add(MSG.errorRollForwardMissingDepartment(MSG.rollForwardInstructors()));
		}
		if (iData.getRollForwardCourseOfferings()) {
			oneSelected = true;
			if (iData.getSessionToRollCourseOfferingsForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardCourseOfferings()));
			if (!iData.hasRollForwardSubjectAreaIds())
				errors.add(MSG.errorRollForwardMissingSubjectArea(MSG.rollForwardCourseOfferings()));
		}
		if (iData.getRollForwardClassInstructors()) {
			oneSelected = true;
			if (iData.getRollForwardClassInstrSubjectIds() == null || iData.getRollForwardClassInstrSubjectIds().isEmpty())
				errors.add(MSG.errorRollForwardMissingSubjectArea(MSG.rollForwardClassInstructors()));
		}
		if (iData.getRollForwardOfferingCoordinators()) {
			oneSelected = true;
			if (iData.getRollForwardOfferingCoordinatorsSubjectIds() == null || iData.getRollForwardOfferingCoordinatorsSubjectIds().isEmpty())
				errors.add(MSG.errorRollForwardMissingSubjectArea(MSG.rollForwardOfferingCoordinators()));
		}
		if (iData.getRollForwardTeachingRequests()) {
			oneSelected = true;
			if (iData.getRollForwardTeachingRequestsSubjectIds() == null || iData.getRollForwardTeachingRequestsSubjectIds().isEmpty())
				errors.add(MSG.errorRollForwardMissingSubjectArea(MSG.rollForwardTeachingRequests()));
		}
		if (iData.getAddNewCourseOfferings()) {
			oneSelected = true;
			if (iData.getAddNewCourseOfferingsSubjectIds() == null || iData.getAddNewCourseOfferingsSubjectIds().isEmpty())
				errors.add(MSG.errorRollForwardMissingSubjectArea(MSG.rollForwardNewCourses()));
		}
		if (iData.getRollForwardExamConfiguration()) {
			oneSelected = true;
			if (iData.getSessionToRollExamConfigurationForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardExamConfiguration()));
		}
		if (iData.getRollForwardMidtermExams()) {
			oneSelected = true;
		}
		if (iData.getRollForwardFinalExams()) {
			oneSelected = true;
		}
		if (iData.getRollForwardStudents()) {
			oneSelected = true;
			if (iData.getRollForwardStudentsMode() == null)
				errors.add(MSG.errorRollForwardInvalidCourseDemandAction("NULL"));
			if (iData.getRollForwardStudentsMode() == StudentEnrollmentMode.POINT_IN_TIME_CLASS_ENROLLMENTS && 
					iData.getPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom() == null)
				errors.add(MSG.errorRollForwardGeneric(MSG.rollForwardLastLikeStudentCourseRequests(), MSG.errorRequiredField(MSG.rollForwardPITStudentClassEnrollments())));
		}
		if (iData.getRollForwardCurricula()) {
			oneSelected = true;
			if (iData.getSessionToRollCurriculaForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardCurricula()));
		}
		if (iData.getRollForwardReservations()) {
			oneSelected = true;
			if (iData.getSessionToRollReservationsForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardReservations()));
			if (iData.getRollForwardReservationsSubjectIds() == null || iData.getRollForwardReservationsSubjectIds().isEmpty())
				errors.add(MSG.errorRollForwardMissingSubjectArea(MSG.rollForwardReservations()));
			if (!iData.getRollForwardCourseReservations() &&
				!iData.getRollForwardCurriculumReservations() &&
				!iData.getRollForwardGroupReservations() &&
				!iData.getRollForwardUniversalReservations())
				errors.add(MSG.errorRollForwardGeneric(MSG.rollForwardReservations(), MSG.errorNoReservationTypeSelected()));
		}
		if (iData.getRollForwardPeriodicTasks()) {
			oneSelected = true;
			if (iData.getSessionToRollPeriodicTasksFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(MSG.rollForwardScheduledTasks()));
		}
		if (!oneSelected) {
			errors.add(MSG.errorRollForwardMissingAction());
		}

		iErrors.setErrors(errors);

		return errors.isEmpty();
	}
	
	protected void initForm() {
		if (iFirstRow >= 0)
			for (int row = iPanel.getRowCount() - 1; row >= iFirstRow; row--)
				iPanel.removeRow(row);
		iFirstRow = iPanel.addHeaderRow(iHeader);
		iPanel.addRow(iErrors);
		
		SingleIdListBox sessionToRollForwardTo = new SingleIdListBox(iConfig.getToSessions(), iData.getSessionToRollForwardTo());
		sessionToRollForwardTo.addValueChangeHandler(new ValueChangeHandler<Long>() {
			@Override
			public void onValueChange(ValueChangeEvent<Long> event) {
				iData.setSessionToRollForwardTo(event.getValue());
				init();
			}
		});
		
		int row = iPanel.addRow(MSG.propSessionToRollForwardTo(), sessionToRollForwardTo);
		iPanel.getRowFormatter().addStyleName(row, "extra-space-below");
		
		if (!iConfig.hasFromSessions()) {
			iHeader.setEnabled("execute", false);
			iPanel.addBottomRow(iFooter);
			return;
		}
		
		iPanel.addRow(new SimpleAction(MSG.propRollDepartmentsForwardFromSession(), null,
				iData.getRollForwardDepartments(), iData.getSessionToRollDeptsFowardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardDepartments(check);
				iData.setSessionToRollDeptsFowardFrom(fromSessionId);
			}
		});
		
		iPanel.addRow(new SimpleAction(MSG.propRollSessionConfigFromSession(), MSG.infoRollSessionConfigFromSession(),
				iData.getRollForwardSessionConfig(), iData.getSessionToRollSessionConfigForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardSessionConfig(check);
				iData.setSessionToRollSessionConfigForwardFrom(fromSessionId);
			}
		});
		
		iPanel.addRow(new SimpleAction(MSG.propRollManagersFromSession(), null,
				iData.getRollForwardManagers(), iData.getSessionToRollManagersForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardManagers(check);
				iData.setSessionToRollManagersForwardFrom(fromSessionId);
			}
		});

		iPanel.addRow(new SimpleAction(MSG.propRollRoomsFromSession(), null,
				iData.getRollForwardRoomData(), iData.getSessionToRollRoomDataForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardRoomData(check);
				iData.setSessionToRollRoomDataForwardFrom(fromSessionId);
			}
		});

		iPanel.addRow(new SimpleAction(MSG.propRollDatePatternsFromSession(), null,
				iData.getRollForwardDatePatterns(), iData.getSessionToRollDatePatternsForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardDatePatterns(check);
				iData.setSessionToRollDatePatternsForwardFrom(fromSessionId);
			}
		});

		iPanel.addRow(new SimpleAction(MSG.propRollTimePatternsFromSession(), null,
				iData.getRollForwardTimePatterns(), iData.getSessionToRollTimePatternsForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardTimePatterns(check);
				iData.setSessionToRollTimePatternsForwardFrom(fromSessionId);
			}
		});
		
		iPanel.addRow(new SimpleAction(MSG.propRollLMSFromSession(), null,
				iData.getRollForwardLearningManagementSystems(), iData.getSessionToRollLearningManagementSystemsForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardLearningManagementSystems(check);
				iData.setSessionToRollLearningManagementSystemsForwardFrom(fromSessionId);
			}
		});
		
		iPanel.addRow(new SimpleAction(MSG.propRollSubjectsFromSession(), null,
				iData.getRollForwardSubjectAreas(), iData.getSessionToRollSubjectAreasForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardSubjectAreas(check);
				iData.setSessionToRollSubjectAreasForwardFrom(fromSessionId);
			}
		});
		
		SimpleAction rollForwardInstructorData = new SimpleAction(MSG.propRollInstructorsFromSession(), null,
				iData.getRollForwardInstructorData(), iData.getSessionToRollInstructorDataForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardInstructorData(check);
				iData.setSessionToRollInstructorDataForwardFrom(fromSessionId);
			}
		}; 
		iPanel.addRow(rollForwardInstructorData);
		MultipleIdsListBox departments = new MultipleIdsListBox(iConfig.getDepartments(), iData.getRollForwardDepartmentIds());
		departments.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<Long>> event) {
				iData.setRollForwardDepartmentIds(event.getValue());
			}
		});
		addSubRow(rollForwardInstructorData.getCheckBox(), MSG.propForDepartments(), departments);
		
		SimpleAction rollForwardCourseOfferings = new SimpleAction(MSG.propRollCoursesFormSession(), null,
				iData.getRollForwardCourseOfferings(), iData.getSessionToRollCourseOfferingsForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardCourseOfferings(check);
				iData.setSessionToRollCourseOfferingsForwardFrom(fromSessionId);
			}
		}; 
		iPanel.addRow(rollForwardCourseOfferings);
		MultipleIdsListBox rollForwardSubjectAreaIds = new MultipleIdsListBox(iConfig.getSubjects(), iData.getRollForwardSubjectAreaIds());
		rollForwardSubjectAreaIds.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<Long>> event) {
				iData.setRollForwardSubjectAreaIds(event.getValue());
			}
		});
		addSubRow(rollForwardCourseOfferings.getCheckBox(), MSG.propForSubjectAreas(), rollForwardSubjectAreaIds);
		CheckBox rollForwardWaitListsProhibitedOverrides = new CheckBox(MSG.checkIncludeWaitListAndOverrides());
		rollForwardWaitListsProhibitedOverrides.setValue(Boolean.TRUE.equals(iData.getRollForwardWaitListsProhibitedOverrides()));
		rollForwardWaitListsProhibitedOverrides.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iData.setRollForwardWaitListsProhibitedOverrides(event.getValue());
			}
		});
		addSubRow(rollForwardCourseOfferings.getCheckBox(), rollForwardWaitListsProhibitedOverrides);
		if (Boolean.TRUE.equals(iConfig.getParentCourses())) {
			CheckBox rollForwardParentOfferings = new CheckBox(MSG.checkIncludeParentOfferings());
			rollForwardParentOfferings.setValue(Boolean.TRUE.equals(iData.getRollForwardParentOfferings()));
			rollForwardParentOfferings.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iData.setRollForwardParentOfferings(event.getValue());
				}
			});
			addSubRow(rollForwardCourseOfferings.getCheckBox(), rollForwardParentOfferings);
		}
		
		MultiSelection subpartTimePrefsAction = new MultiSelection("subpartTimePrefsAction") {
			public void update(String option) {
				iData.setSubpartTimePrefsAction(RollAction.valueOf(option));
			}
		};
		subpartTimePrefsAction.addOption(MSG.optRollSubpartTimePrefs(), RollAction.ROLL_PREFS_ACTION.name(), iData.getSubpartTimePrefsAction() == RollAction.ROLL_PREFS_ACTION);
		subpartTimePrefsAction.addOption(MSG.optNotRollSubpartTimePrefs(), RollAction.DO_NOT_ROLL_ACTION.name(), iData.getSubpartTimePrefsAction() == RollAction.DO_NOT_ROLL_ACTION);
		addSubRow(rollForwardCourseOfferings.getCheckBox(), MSG.propSubpartLevelTimePrefs(), subpartTimePrefsAction);
		
		MultiSelection subpartLocationPrefsAction = new MultiSelection("subpartLocationPrefsAction") {
			public void update(String option) {
				iData.setSubpartLocationPrefsAction(RollAction.valueOf(option));
			}
		};
		subpartLocationPrefsAction.addOption(MSG.optRollSubpartRoomPrefs(), RollAction.ROLL_PREFS_ACTION.name(), iData.getSubpartLocationPrefsAction() == RollAction.ROLL_PREFS_ACTION);
		subpartLocationPrefsAction.addOption(MSG.optNotRollSubpartRoomPrefs(), RollAction.DO_NOT_ROLL_ACTION.name(), iData.getSubpartLocationPrefsAction() == RollAction.DO_NOT_ROLL_ACTION);
		addSubRow(rollForwardCourseOfferings.getCheckBox(), MSG.propSubpartLevelRoomPrefs(), subpartLocationPrefsAction);
		
		MultiSelection classPrefsAction = new MultiSelection("classPrefsAction") {
			public void update(String option) {
				iData.setClassPrefsAction(RollAction.valueOf(option));
			}
		};
		classPrefsAction.addOption(MSG.optNoRollClassPrefs(), RollAction.DO_NOT_ROLL_ACTION.name(), iData.getClassPrefsAction() == RollAction.DO_NOT_ROLL_ACTION);
		classPrefsAction.addOption(MSG.optPushClassPrefsUp(), RollAction.PUSH_UP_ACTION.name(), iData.getClassPrefsAction() == RollAction.PUSH_UP_ACTION);
		if (Boolean.TRUE.equals(iConfig.getAllowClassPrefs()))
			classPrefsAction.addOption(MSG.optRollClassPrefs(), RollAction.ROLL_PREFS_ACTION.name(), iData.getClassPrefsAction() == RollAction.ROLL_PREFS_ACTION);
		addSubRow(rollForwardCourseOfferings.getCheckBox(), MSG.propClassLevelPrefs(), classPrefsAction);
		
		MultiSelection rollForwardDistributions = new MultiSelection("rollForwardDistributions") {
			public void update(String option) {
				iData.setRollForwardDistributions(DistributionMode.valueOf(option));
			}
		};
		rollForwardDistributions.addOption(MSG.optRollDistPrefsAll(), DistributionMode.ALL.name(), iData.getRollForwardDistributions() == DistributionMode.ALL);
		rollForwardDistributions.addOption(MSG.optRollDistPrefsMixed(), DistributionMode.MIXED.name(), iData.getRollForwardDistributions() == DistributionMode.MIXED);
		rollForwardDistributions.addOption(MSG.optRollDistPrefsSubparts(), DistributionMode.SUBPART.name(), iData.getRollForwardDistributions() == DistributionMode.SUBPART);
		rollForwardDistributions.addOption(MSG.optRollDistPrefsNone(), DistributionMode.NONE.name(), iData.getRollForwardDistributions() == DistributionMode.NONE);
		addSubRow(rollForwardCourseOfferings.getCheckBox(), MSG.propDistributionPrefs(), rollForwardDistributions);
		
		MultiSelection cancelledClassAction = new MultiSelection("cancelledClassAction") {
			public void update(String option) {
				iData.setCancelledClassAction(CancelledClassAction.valueOf(option));
			}
		};
		cancelledClassAction.addOption(MSG.optCancelledClassesKeep(), CancelledClassAction.KEEP.name(), iData.getCancelledClassAction() == CancelledClassAction.KEEP);
		cancelledClassAction.addOption(MSG.optCancelledClassesReopen(), CancelledClassAction.REOPEN.name(), iData.getCancelledClassAction() == CancelledClassAction.REOPEN);
		cancelledClassAction.addOption(MSG.optCancelledClassesSkip(), CancelledClassAction.SKIP.name(), iData.getCancelledClassAction() == CancelledClassAction.SKIP);
		addSubRow(rollForwardCourseOfferings.getCheckBox(), MSG.propCancelledClasses(), cancelledClassAction);
		
		iPanel.addRow(new ForSubjectsAction(MSG.propRollClassInstructorsForSubjects(),
				iData.getRollForwardClassInstructors(), iData.getRollForwardClassInstrSubjectIds()) {
			@Override
			public void update(Boolean check, List<Long> fromSessionId) {
				iData.setRollForwardClassInstructors(check);
				iData.setRollForwardClassInstrSubjectIds(fromSessionId);
			}
		});
		
		iPanel.addRow(new ForSubjectsAction(MSG.propRollOfferingCoordinatorsForSubjects(),
				iData.getRollForwardOfferingCoordinators(), iData.getRollForwardOfferingCoordinatorsSubjectIds()) {
			@Override
			public void update(Boolean check, List<Long> fromSessionId) {
				iData.setRollForwardOfferingCoordinators(check);
				iData.setRollForwardOfferingCoordinatorsSubjectIds(fromSessionId);
			}
		});
		
		iPanel.addRow(new ForSubjectsAction(MSG.propRollTeachingRequestsForSubjects(),
				iData.getRollForwardTeachingRequests(), iData.getRollForwardTeachingRequestsSubjectIds()) {
			@Override
			public void update(Boolean check, List<Long> fromSessionId) {
				iData.setRollForwardTeachingRequests(check);
				iData.setRollForwardTeachingRequestsSubjectIds(fromSessionId);
			}
		});
		
		iPanel.addRow(new ForSubjectsAction(MSG.propAddNewCoursesForSubjects(),
				MSG.infoAddNewCoursesForSubjects(),
				iData.getAddNewCourseOfferings(), iData.getAddNewCourseOfferingsSubjectIds()) {
			@Override
			public void update(Boolean check, List<Long> fromSessionId) {
				iData.setAddNewCourseOfferings(check);
				iData.setAddNewCourseOfferingsSubjectIds(fromSessionId);
			}
		});

		iPanel.addRow(new SimpleAction(MSG.propRollExamConfigFromSession(), null,
				iData.getRollForwardExamConfiguration(), iData.getSessionToRollExamConfigurationForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardExamConfiguration(check);
				iData.setSessionToRollExamConfigurationForwardFrom(fromSessionId);
			}
		});
		
		CheckBox rollForwardMidtermExams = new CheckBox(MSG.propRollMidtermExams());
		rollForwardMidtermExams.setValue(Boolean.TRUE.equals(iData.getRollForwardMidtermExams()));
		rollForwardMidtermExams.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iData.setRollForwardMidtermExams(event.getValue());
			}
		});
		iPanel.addRow(rollForwardMidtermExams);
		MultiSelection midtermExamsPrefsAction = new MultiSelection("midtermExamsPrefsAction") {
			public void update(String option) {
				iData.setMidtermExamsPrefsAction(RollAction.valueOf(option));
			}
		};
		midtermExamsPrefsAction.addOption(MSG.prefMidtermExamsAll(), RollAction.EXAMS_ALL_PREF.name(), iData.getMidtermExamsPrefsAction() == RollAction.EXAMS_ALL_PREF);
		midtermExamsPrefsAction.addOption(MSG.prefMidtermExamsRoom(), RollAction.EXAMS_ROOM_PREFS.name(), iData.getMidtermExamsPrefsAction() == RollAction.EXAMS_ROOM_PREFS);
		midtermExamsPrefsAction.addOption(MSG.prefMidtermExamsNone(), RollAction.EXAMS_NO_PREF.name(), iData.getMidtermExamsPrefsAction() == RollAction.EXAMS_NO_PREF);
		addSubRow(rollForwardMidtermExams, MSG.propPreferences(), midtermExamsPrefsAction);
		
		CheckBox rollForwardFinalExams = new CheckBox(MSG.propRollFinalExams());
		rollForwardFinalExams.setValue(Boolean.TRUE.equals(iData.getRollForwardFinalExams()));
		rollForwardFinalExams.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iData.setRollForwardFinalExams(event.getValue());
			}
		});
		iPanel.addRow(rollForwardFinalExams);
		MultiSelection finalExamsPrefsAction = new MultiSelection("finalExamsPrefsAction") {
			public void update(String option) {
				iData.setFinalExamsPrefsAction(RollAction.valueOf(option));
			}
		};
		finalExamsPrefsAction.addOption(MSG.prefFinalExamsAll(), RollAction.EXAMS_ALL_PREF.name(), iData.getFinalExamsPrefsAction() == RollAction.EXAMS_ALL_PREF);
		finalExamsPrefsAction.addOption(MSG.prefFinalExamsRoom(), RollAction.EXAMS_ROOM_PREFS.name(), iData.getFinalExamsPrefsAction() == RollAction.EXAMS_ROOM_PREFS);
		finalExamsPrefsAction.addOption(MSG.prefFinalExamsNone(), RollAction.EXAMS_NO_PREF.name(), iData.getFinalExamsPrefsAction() == RollAction.EXAMS_NO_PREF);
		addSubRow(rollForwardFinalExams, MSG.propPreferences(), finalExamsPrefsAction);
		
		CheckBox rollForwardStudents = new CheckBox(MSG.propImportLastLikes());
		rollForwardStudents.setValue(Boolean.TRUE.equals(iData.getRollForwardStudents()));
		rollForwardStudents.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iData.setRollForwardStudents(event.getValue());
			}
		});
		iPanel.addRow(rollForwardStudents);
		MultiSelection rollForwardStudentsMode = new MultiSelection("rollForwardStudentsMode") {
			public void update(String option) {
				iData.setRollForwardStudentsMode(StudentEnrollmentMode.valueOf(option));
			}
		};
		rollForwardStudentsMode.addOption(MSG.optLastLikeCopy(), StudentEnrollmentMode.LAST_LIKE.name(), iData.getRollForwardStudentsMode() == StudentEnrollmentMode.LAST_LIKE);
		rollForwardStudentsMode.addOption(MSG.optLastLikeEnrls(), StudentEnrollmentMode.STUDENT_CLASS_ENROLLMENTS.name(), iData.getRollForwardStudentsMode() == StudentEnrollmentMode.STUDENT_CLASS_ENROLLMENTS);
		rollForwardStudentsMode.addOption(MSG.optLastLikeCourseReqs(), StudentEnrollmentMode.STUDENT_COURSE_REQUESTS.name(), iData.getRollForwardStudentsMode() == StudentEnrollmentMode.STUDENT_COURSE_REQUESTS);
		rollForwardStudentsMode.addOption(MSG.optLastLikePIT(), StudentEnrollmentMode.POINT_IN_TIME_CLASS_ENROLLMENTS.name(), iData.getRollForwardStudentsMode() == StudentEnrollmentMode.POINT_IN_TIME_CLASS_ENROLLMENTS);
		addSubRow(rollForwardStudents, rollForwardStudentsMode);
		SingleIdListBox pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom = new SingleIdListBox(iConfig.getPointInTimes(), iData.getPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom());
		pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom.addValueChangeHandler(new ValueChangeHandler<Long>() {
			@Override
			public void onValueChange(ValueChangeEvent<Long> event) {
				iData.setPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom(event.getValue());
			}
		});
		addSubRow(rollForwardStudents, MSG.propPointInTimeSnapshot(), pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom);
		
		iPanel.addRow(new SimpleAction(MSG.propRollCurriculaFromSession(), MSG.infoRollCurriculaFromSession(),
				iData.getRollForwardCurricula(), iData.getSessionToRollCurriculaForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardCurricula(check);
				iData.setSessionToRollCurriculaForwardFrom(fromSessionId);
			}
		});
		
		SimpleAction rollForwardReservations = new SimpleAction(MSG.propRollReservationsFromSession(), null,
				iData.getRollForwardReservations(), iData.getSessionToRollReservationsForwardFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardReservations(check);
				iData.setSessionToRollReservationsForwardFrom(fromSessionId);
			}
		}; 
		iPanel.addRow(rollForwardReservations);
		MultipleIdsListBox rollForwardReservationsSubjectIds = new MultipleIdsListBox(iConfig.getSubjects(), iData.getRollForwardReservationsSubjectIds());
		rollForwardReservationsSubjectIds.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<Long>> event) {
				iData.setRollForwardReservationsSubjectIds(event.getValue());
			}
		});
		addSubRow(rollForwardReservations.getCheckBox(), MSG.propForSubjectAreas(), rollForwardReservationsSubjectIds);
		
		CheckBox rollForwardCourseReservations = new CheckBox(MSG.optIncludeCourseReservations());
		rollForwardCourseReservations.setValue(Boolean.TRUE.equals(iData.getRollForwardCourseReservations()));
		rollForwardCourseReservations.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iData.setRollForwardCourseReservations(event.getValue());
			}
		});
		addSubRow(rollForwardReservations.getCheckBox(), rollForwardCourseReservations);
		SingleDateSelector startDateCourseReservations = new SingleDateSelector();
		startDateCourseReservations.setValue(iData.getStartDateCourseReservations());
		startDateCourseReservations.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iData.setStartDateCourseReservations(event.getValue());
			}
		});
		addSubSubRow(rollForwardReservations.getCheckBox(), rollForwardCourseReservations, MSG.propNewStartDate(), startDateCourseReservations, MSG.infoNewStartDateCourse());
		SingleDateSelector expirationCourseReservations = new SingleDateSelector();
		expirationCourseReservations.setValue(iData.getExpirationCourseReservations());
		expirationCourseReservations.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iData.setExpirationCourseReservations(event.getValue());					
			}
		});
		addSubSubRow(rollForwardReservations.getCheckBox(), rollForwardCourseReservations, MSG.propNewExpirationDate(), expirationCourseReservations, MSG.infoNewExpirationDateCourse());

		CheckBox rollForwardCurriculumReservations = new CheckBox(MSG.optIncludeCurriculumReservations());
		rollForwardCurriculumReservations.setValue(Boolean.TRUE.equals(iData.getRollForwardCurriculumReservations()));
		rollForwardCurriculumReservations.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iData.setRollForwardCurriculumReservations(event.getValue());
			}
		});
		addSubRow(rollForwardReservations.getCheckBox(), rollForwardCurriculumReservations);
		SingleDateSelector startDateCurriculumReservations = new SingleDateSelector();
		startDateCurriculumReservations.setValue(iData.getStartDateCurriculumReservations());
		startDateCurriculumReservations.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iData.setStartDateCurriculumReservations(event.getValue());
			}
		});
		addSubSubRow(rollForwardReservations.getCheckBox(), rollForwardCurriculumReservations, MSG.propNewStartDate(), startDateCurriculumReservations, MSG.infoNewStartDateCurriculum());
		SingleDateSelector expirationCurriculumReservations = new SingleDateSelector();
		expirationCurriculumReservations.setValue(iData.getExpirationCurriculumReservations());
		expirationCurriculumReservations.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iData.setExpirationCurriculumReservations(event.getValue());					
			}
		});
		addSubSubRow(rollForwardReservations.getCheckBox(), rollForwardCurriculumReservations, MSG.propNewExpirationDate(), expirationCurriculumReservations, MSG.infoNewExpirationDateCurriculum());		

		CheckBox rollForwardGroupReservations = new CheckBox(MSG.optIncludeStudentGroupReservations());
		rollForwardGroupReservations.setValue(Boolean.TRUE.equals(iData.getRollForwardGroupReservations()));
		rollForwardGroupReservations.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iData.setRollForwardGroupReservations(event.getValue());
			}
		});
		addSubRow(rollForwardReservations.getCheckBox(), rollForwardGroupReservations);
		SingleDateSelector startDateGroupReservations = new SingleDateSelector();
		startDateGroupReservations.setValue(iData.getStartDateGroupReservations());
		startDateGroupReservations.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iData.setStartDateGroupReservations(event.getValue());
			}
		});
		addSubSubRow(rollForwardReservations.getCheckBox(), rollForwardGroupReservations, MSG.propNewStartDate(), startDateGroupReservations, MSG.infoNewStartDateGroup());
		SingleDateSelector expirationGroupReservations = new SingleDateSelector();
		expirationGroupReservations.setValue(iData.getExpirationGroupReservations());
		expirationGroupReservations.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iData.setExpirationGroupReservations(event.getValue());					
			}
		});
		addSubSubRow(rollForwardReservations.getCheckBox(), rollForwardGroupReservations, MSG.propNewExpirationDate(), expirationGroupReservations, MSG.infoNewExpirationDateGroup());
		CheckBox createStudentGroupsIfNeeded = new CheckBox(MSG.optCreateStudentGroupsForReservations());
		createStudentGroupsIfNeeded.setValue(Boolean.TRUE.equals(iData.getCreateStudentGroupsIfNeeded()));
		createStudentGroupsIfNeeded.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iData.setCreateStudentGroupsIfNeeded(event.getValue());
			}
		});
		addSubSubRow(rollForwardReservations.getCheckBox(), rollForwardGroupReservations, createStudentGroupsIfNeeded);
		
		CheckBox rollForwardUniversalReservations = new CheckBox(MSG.optIncludeStudentUniversalReservations());
		rollForwardUniversalReservations.setValue(Boolean.TRUE.equals(iData.getRollForwardUniversalReservations()));
		rollForwardUniversalReservations.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iData.setRollForwardUniversalReservations(event.getValue());
			}
		});
		addSubRow(rollForwardReservations.getCheckBox(), rollForwardUniversalReservations);
		SingleDateSelector startDateUniversalReservations = new SingleDateSelector();
		startDateUniversalReservations.setValue(iData.getStartDateUniversalReservations());
		startDateUniversalReservations.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iData.setStartDateUniversalReservations(event.getValue());
			}
		});
		addSubSubRow(rollForwardReservations.getCheckBox(), rollForwardUniversalReservations, MSG.propNewStartDate(), startDateUniversalReservations, MSG.infoNewStartDateUniversal());
		SingleDateSelector expirationUniversalReservations = new SingleDateSelector();
		expirationUniversalReservations.setValue(iData.getExpirationUniversalReservations());
		expirationUniversalReservations.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				iData.setExpirationUniversalReservations(event.getValue());					
			}
		});
		addSubSubRow(rollForwardReservations.getCheckBox(), rollForwardUniversalReservations, MSG.propNewExpirationDate(), expirationUniversalReservations, MSG.infoNewExpirationDateUniversal());
		
		iPanel.addRow(new SimpleAction(MSG.propRollScheduledTasksFromSession(), null,
				iData.getRollForwardPeriodicTasks(), iData.getSessionToRollPeriodicTasksFrom()) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardPeriodicTasks(check);
				iData.setSessionToRollPeriodicTasksFrom(fromSessionId);
			}
		});
		
		iHeader.setEnabled("execute", true);
		iPanel.addBottomRow(iFooter);
	}
	
	protected int addSubRow(CheckBox ch, String label, Widget widget) {
		final int ret = iPanel.addRow(label, widget);
		iPanel.getCellFormatter().addStyleName(ret, 0, "sub-header");
		iPanel.getRowFormatter().setVisible(ret, ch.getValue());
		ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iPanel.getRowFormatter().setVisible(ret, event.getValue());		
			}
		});
		return ret;
	}
	
	protected int addSubRow(CheckBox ch, Widget widget) {
		final int ret = iPanel.addRow(widget);
		iPanel.getCellFormatter().addStyleName(ret, 0, "sub-header");
		iPanel.getRowFormatter().setVisible(ret, ch.getValue());
		ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iPanel.getRowFormatter().setVisible(ret, event.getValue());		
			}
		});
		return ret;
	}
	
	protected int addSubSubRow(CheckBox ch1, CheckBox ch2, String label, Widget widget, String note) {
		P n = new P("note"); n.setText(note); 
		int ret = iPanel.addRow(label, widget, n);
		iPanel.getCellFormatter().addStyleName(ret, 0, "sub-sub-header");
		iPanel.getRowFormatter().setVisible(ret, ch1.getValue() && ch2.getValue());
		ch1.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iPanel.getRowFormatter().setVisible(ret, event.getValue() && ch2.getValue());		
			}
		});
		ch2.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iPanel.getRowFormatter().setVisible(ret, event.getValue() && ch1.getValue());		
			}
		});
		return ret;
	}
	
	protected int addSubSubRow(CheckBox ch1, CheckBox ch2, Widget widget) {
		int ret = iPanel.addRow(widget);
		iPanel.getCellFormatter().addStyleName(ret, 0, "sub-sub-header");
		iPanel.getRowFormatter().setVisible(ret, ch1.getValue() && ch2.getValue());
		ch1.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iPanel.getRowFormatter().setVisible(ret, event.getValue() && ch2.getValue());		
			}
		});
		ch2.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iPanel.getRowFormatter().setVisible(ret, event.getValue() && ch1.getValue());		
			}
		});
		return ret;
	}
	
	protected static class SingleIdListBox extends ListBox implements HasValue<Long>{
		public SingleIdListBox(List<IdLabel> items) {
			addItem(MSG.itemSelect(), "");
			if (items != null)
				for (IdLabel item: items)
					addItem(item.getLabel(), item.getId().toString());
			addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					ValueChangeEvent.fire(SingleIdListBox.this, getValue());
				}
			});
		}
		
		public SingleIdListBox(List<IdLabel> items, Long value) {
			this(items);
			setValue(value);
		}

		@Override
		public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Long> handler) {
			return addHandler(handler, ValueChangeEvent.getType());
		}

		@Override
		public Long getValue() {
			if (getSelectedIndex() <= 0) return null;
			return Long.valueOf(getSelectedValue());
		}

		@Override
		public void setValue(Long value) {
			if (value == null) {
				setSelectedIndex(0);
			} else {
				for (int i = 1; i < getItemCount(); i++)
					if (getValue(i).equals(value.toString())) {
						setSelectedIndex(i);
						return;
					}
				setSelectedIndex(0);
			}
		}

		@Override
		public void setValue(Long value, boolean fireEvents) {
			setValue(value);
			if (fireEvents)
				ValueChangeEvent.fire(this, getValue());
		}
	}
	
	protected static class MultipleIdsListBox extends ListBox implements HasValue<List<Long>>{
		public MultipleIdsListBox(List<IdLabel> items) {
			setMultipleSelect(true);
			setVisibleItemCount(items == null ? 1 : Math.min(7, items.size()));
			if (items != null)
				for (IdLabel item: items)
					addItem(item.getLabel(), item.getId().toString());
			addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					ValueChangeEvent.fire(MultipleIdsListBox.this, getValue());
				}
			});
		}
		
		public MultipleIdsListBox(List<IdLabel> items, List<Long> value) {
			this(items);
			setValue(value);
		}

		@Override
		public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Long>> handler) {
			return addHandler(handler, ValueChangeEvent.getType());
		}

		@Override
		public List<Long> getValue() {
			List<Long> ret = new ArrayList<Long>();
			for (int i = 0; i < getItemCount(); i++)
				if (isItemSelected(i))
					ret.add(Long.valueOf(getValue(i)));
			return ret;
		}

		@Override
		public void setValue(List<Long> value) {
			for (int i = 0; i < getItemCount(); i++)
				setItemSelected(i, value != null && value.contains(Long.valueOf(getValue(i))));
		}

		@Override
		public void setValue(List<Long> value, boolean fireEvents) {
			setValue(value);
			if (fireEvents)
				ValueChangeEvent.fire(this, getValue());
		}
	}
	
	protected class SimpleAction extends P {
		private CheckBox iCheckBox;
		private SingleIdListBox iListBox;
		private P iNote;
		SimpleAction(String label, String note, Boolean check, Long fromSessionId) {
			super("roll-action");
			iCheckBox = new CheckBox(label);
			iCheckBox.setValue(Boolean.TRUE.equals(check));
			iListBox = new SingleIdListBox(iConfig.getFromSessions(), fromSessionId);
			Roles.getListboxRole().setAriaLabelProperty(iListBox.getElement(), label);
			add(iCheckBox);
			add(iListBox);
			if (note != null) {
				iNote = new P("note");
				iNote.setText(note);
				iNote.setVisible(iCheckBox.getValue());
				add(iNote);
			}
			iCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (iNote != null) iNote.setVisible(event.getValue());
					update(event.getValue(), iListBox.getValue());
				}
			});
			iListBox.addValueChangeHandler(new ValueChangeHandler<Long>() {
				@Override
				public void onValueChange(ValueChangeEvent<Long> event) {
					update(iCheckBox.getValue(), event.getValue());
				}
			});
		}
		
		public void update(Boolean check, Long fromSessionId) {
			
		}
		
		public CheckBox getCheckBox() { return iCheckBox; }
	}
	
	protected class ForSubjectsAction extends P {
		private CheckBox iCheckBox;
		private MultipleIdsListBox iListBox;
		private P iNote;
		ForSubjectsAction(String label, Boolean check, List<Long> subjectIds) {
			this(label, null, check, subjectIds);
		}
		ForSubjectsAction(String label, String note, Boolean check, List<Long> subjectIds) {
			super("roll-action-subjects");
			iCheckBox = new CheckBox(label);
			iCheckBox.setValue(Boolean.TRUE.equals(check));
			iListBox = new MultipleIdsListBox(iConfig.getSubjects(), subjectIds);
			Roles.getListboxRole().setAriaLabelProperty(iListBox.getElement(), label);
			if (note != null && iConfig.hasSubjects()) {
				iNote = new P("note");
				iNote.setText(note);
				add(iNote);
				P checkWithNote = new P("check-with-note");
				checkWithNote.add(iCheckBox);
				checkWithNote.add(iNote);
				add(checkWithNote);
			} else {
				add(iCheckBox);
			}
			add(iListBox);
			iCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					update(event.getValue(), iListBox.getValue());
				}
			});
			iListBox.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
				@Override
				public void onValueChange(ValueChangeEvent<List<Long>> event) {
					update(iCheckBox.getValue(), event.getValue());
				}
			});
		}

		public void update(Boolean check, List<Long> ids) {
			
		}
		
		public CheckBox getCheckBox() { return iCheckBox; }
	}
	
	protected class MultiSelection extends P {
		private String iName;
		public MultiSelection(String name) {
			super("multi-selection");
			iName = name;
		}
		public void addOption(String label, final String option, boolean checked) {
			RadioButton button = new RadioButton(iName, label);
			button.setValue(checked);
			button.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (event.getValue())
						update(option);
				}
			});
			add(button);
		}
		
		public void update(String option) {
			
		}
	}
	
	public static class ErrorsWidget extends P {
		P iErrors;
		public ErrorsWidget() {
			super("unitime-ErrorMessages");
			P header = new P(); header.setText(MSG.formValidationErrors());
			add(header);
			iErrors = new P(DOM.createElement("ul"));
			add(iErrors);
			setVisible(false);
		}
		
		protected void clearErrors() {
			iErrors.clear();
			setVisible(false);
		}
		
		protected void setErrors(String... message) {
			iErrors.clear();
			for (String m: message) {
				P p = new P(DOM.createElement("li"));
				p.setText(m);
				iErrors.add(p);
			}
			setVisible(iErrors.getWidgetCount() > 0);
		}
		
		protected void setErrors(List<String> errors) {
			iErrors.clear();
			if (errors == null || errors.isEmpty()) {
				setVisible(false);
			} else {
				for (String m: errors) {
					P p = new P(DOM.createElement("li"));
					p.setText(m);
					iErrors.add(p);
				}
				setVisible(iErrors.getWidgetCount() > 0);
			}
		}
		
		protected void setErrors(RollForwardErrors errors) {
			iErrors.clear();
			if (errors == null || errors.isEmpty()) {
				setVisible(false);
			} else {
				for (RollForwardError e: errors) {
					P p = new P(DOM.createElement("li"));
					p.setText(e.getMessage());
					iErrors.add(p);
				}
				setVisible(iErrors.getWidgetCount() > 0);
			}
		}
	}
}
