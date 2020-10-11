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
package org.unitime.timetable.gwt.client.teachingschedule;

import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.sectioning.ScheduleStatus;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Clazz;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseGroupDivision;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.DeleteTeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.GetTeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.MeetingAssignment;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.ValidationError;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionEvent;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionHandler;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.TeachingScheduleMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TeachingScheduleDetailPage extends SimpleForm {
	public static TeachingScheduleMessages MESSAGES = GWT.create(TeachingScheduleMessages.class);
	public static GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private UniTimeHeaderPanel iTitleAndButtons, iBottomButtons;
	private TeachingScheduleCourseSelectionBox iCourseBox;
	private TeachingScheduleDivisionsTable iCourseDivisionsTable;
	private ScheduleStatus iStatus;
	private boolean iHasCourseId = false;
	
	public TeachingScheduleDetailPage() {
		addStyleName("unitime-CourseMeetingsDetail");
		iTitleAndButtons = new UniTimeHeaderPanel(MESSAGES.sectCourseDetails());
		iTitleAndButtons.addButton("back", MESSAGES.buttonBackToDetail(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open("gwt.jsp?page=teachingSchedules" + (iCourseDivisionsTable.getOffering() != null ? "&subject=" + iCourseDivisionsTable.getOffering().getSubjectAreaId() : ""));
			}
		});
		iTitleAndButtons.addButton("delete", MESSAGES.buttonDelete(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(MESSAGES.confirmDelete(iCourseDivisionsTable.getOffering().getCourseName()), new Command() {
					@Override
					public void execute() {
						LoadingWidget.getInstance().show(MESSAGES.pleaseWait());
						RPC.execute(new DeleteTeachingSchedule(iCourseDivisionsTable.getOffering().getOfferingId()), new AsyncCallback<GwtRpcResponseNull>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.error(caught.getMessage(), caught);
							}
							@Override
							public void onSuccess(GwtRpcResponseNull result) {
								ToolBox.open("gwt.jsp?page=teachingSchedules&subject=" + iCourseDivisionsTable.getOffering().getSubjectAreaId());
							}
						});
					}
				});
			}
		});
		iTitleAndButtons.addButton("edit", MESSAGES.buttonEdit(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open("gwt.jsp?page=editTeachingSchedule" + (iCourseDivisionsTable.getOffering() != null ? "&course=" + iCourseDivisionsTable.getOffering().getCourseId() : ""));
			}
		});
		addHeaderRow(iTitleAndButtons);
		iTitleAndButtons.setEnabled("delete", false);
		iTitleAndButtons.setEnabled("edit", false);
		
		iCourseBox = new TeachingScheduleCourseSelectionBox();
		iCourseBox.setWidth("130px");
		addRow(MESSAGES.propInstructionalOffering(), iCourseBox);
		iCourseBox.addCourseSelectionHandler(new CourseSelectionHandler() {			
			@Override
			public void onCourseSelection(CourseSelectionEvent event) {
				populate(null);
				if (iCourseBox.getValue().hasCourseId()) {
					LoadingWidget.getInstance().show(MESSAGES.pleaseWait());
					RPC.execute(new GetTeachingSchedule(iCourseBox.getValue().getCourseId(), null), new AsyncCallback<TeachingSchedule>() {
						@Override
						public void onSuccess(TeachingSchedule result) {
							populate(result);
							LoadingWidget.getInstance().hide();
						}
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(caught.getMessage(), caught);
						}
					});
				} else {
					iTitleAndButtons.setEnabled("division", false);
				}
			}
		});
		
		iCourseDivisionsTable = new TeachingScheduleDivisionsTable();
		iStatus = new ScheduleStatus();
		
		iBottomButtons = iTitleAndButtons.clonePanel("");
		addBottomRow(iBottomButtons);
		
		if (Window.Location.getParameter("course") != null) {
			Long courseId = Long.valueOf(Window.Location.getParameter("course"));
			LoadingWidget.getInstance().show(MESSAGES.pleaseWait());
			RPC.execute(new GetTeachingSchedule(courseId, null), new AsyncCallback<TeachingSchedule>() {
				@Override
				public void onSuccess(TeachingSchedule result) {
					iHasCourseId = true;
					populate(result);
					LoadingWidget.getInstance().hide();
				}
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					UniTimeNotifications.error(caught.getMessage(), caught);
				}
			});
		}
	}
	
	public void populate(TeachingSchedule offering) {
		iTitleAndButtons.setEnabled("delete", offering != null);
		iTitleAndButtons.setEnabled("edit", offering != null);
		clear();
		iCourseDivisionsTable.setOffering(offering, true);
		if (offering == null) {
			addHeaderRow(iTitleAndButtons);
			if (!iHasCourseId) addRow(MESSAGES.propInstructionalOffering(), iCourseBox);
		} else {
			addHeaderRow(iTitleAndButtons);
			if (!iHasCourseId) addRow(MESSAGES.propInstructionalOffering(), iCourseBox);
			
			addRow(new UniTimeHeaderPanel(offering.getCourseName()));
			addRow(iCourseDivisionsTable);
			List<ValidationError> errors = iCourseDivisionsTable.getOffering().getDivisionErrors();
			if (!errors.isEmpty()) {
				String message = "";
				for (ValidationError error: errors) {
					message += (message.isEmpty() ? "" : "\n") + error.toString(MESSAGES, CONSTANTS);
				}
				iStatus.error(message);
				addRow(iStatus);
				for (int i = 1; i < iCourseDivisionsTable.getRowCount(); i++) {
					CourseGroupDivision cd = iCourseDivisionsTable.getData(i);
					for (ValidationError error: errors)
						if (cd.getGroup().equals(error.getGroup()) && ((error.getDivision() != null && error.getDivision().equals(cd.getDivision())) || (error.getDivision() == null && cd.isMaster()))) {
							iCourseDivisionsTable.getRowFormatter().addStyleName(i, "error");
							break;
						}
				}
			}
			
			for (final Clazz clazz: iCourseDivisionsTable.getOffering().getClasses()) {
				addRow(new UniTimeHeaderPanel(clazz.getName()));
				TeachingScheduleAssignmentsTable table = new TeachingScheduleAssignmentsTable(iCourseDivisionsTable.getOffering(), clazz, true); 
				addRow(table);
				final ScheduleStatus status = new ScheduleStatus();
				List<ValidationError> clazzErrors = iCourseDivisionsTable.getOffering().getAssignmentErrors(clazz);
				if (!clazzErrors.isEmpty()) {
					String message = "";
					for (ValidationError error: clazzErrors) {
						message += (message.isEmpty() ? "" : "\n") + error.toString(MESSAGES, CONSTANTS);
					}
					status.error(message, false);
					addRow(status);
					for (int i = 1; i < table.getRowCount(); i++) {
						MeetingAssignment ma = table.getData(i);
						for (ValidationError error: clazzErrors)
							if (ma.equals(error.getMeetingAssignment())) {
								table.getRowFormatter().addStyleName(i, "error");
								break;
							}
					}
				}
			}
		}
		addBottomRow(iBottomButtons);
	}
}
