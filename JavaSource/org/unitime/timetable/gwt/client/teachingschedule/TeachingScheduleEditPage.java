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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.sectioning.ScheduleStatus;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.AttributeType;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Clazz;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.GetAttributeTypes;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.GetTeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.SaveTeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.ValidationError;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionEvent;
import org.unitime.timetable.gwt.client.widgets.CourseSelectionHandler;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.TeachingScheduleMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

public class TeachingScheduleEditPage extends SimpleForm {
	public static TeachingScheduleMessages MESSAGES = GWT.create(TeachingScheduleMessages.class);
	public static GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private UniTimeHeaderPanel iTitleAndButtons, iBottomButtons;
	private ListBox iAttributeTypes;
	private TeachingScheduleCourseSelectionBox iCourseBox;
	private TeachingScheduleDivisionsTable iCourseDivisionsTable;
	private ScheduleStatus iStatus;
	private Map<Clazz, ScheduleStatus> iStatuses = new HashMap<Clazz, ScheduleStatus>();
	private boolean iHasCourseId = false;
	
	public TeachingScheduleEditPage() {
		iTitleAndButtons = new UniTimeHeaderPanel(MESSAGES.sectCourseDetails());
		iTitleAndButtons.addButton("back", MESSAGES.buttonBackToDetail(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iCourseDivisionsTable.getOffering() != null && iHasCourseId)
					ToolBox.open("gwt.jsp?page=teachingSchedule&course=" + iCourseDivisionsTable.getOffering().getCourseId());
				else {
					String subject = Window.Location.getParameter("subject");
					ToolBox.open("gwt.jsp?page=teachingSchedules" + (subject == null ? "" : "&subject=" + subject));
				}
			}
		});
		iTitleAndButtons.addButton("course", MESSAGES.buttonCourseSelection(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iTitleAndButtons.setHeaderTitle(MESSAGES.sectCourseDetails());
				iTitleAndButtons.setEnabled("back", true);
				iTitleAndButtons.setEnabled("course", false);
				iTitleAndButtons.setEnabled("division", true);
				iTitleAndButtons.setEnabled("divisionback", false);
				iTitleAndButtons.setEnabled("assignments", false);
				iTitleAndButtons.setEnabled("validate", false);
				iTitleAndButtons.setEnabled("save", false);

				clear();
				addHeaderRow(iTitleAndButtons);
				addRow(MESSAGES.propInstructionalOffering(), iCourseBox);
				addRow(MESSAGES.propAttributeType(), iAttributeTypes);
				addBottomRow(iBottomButtons);
			}
		});
		iTitleAndButtons.addButton("division", MESSAGES.buttonDivisions(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iCourseBox.getValue().hasCourseId()) {
					LoadingWidget.getInstance().show(MESSAGES.pleaseWait());
					RPC.execute(new GetTeachingSchedule(iCourseBox.getValue().getCourseId(), iAttributeTypes.getSelectedValue()), new AsyncCallback<TeachingSchedule>() {
						@Override
						public void onSuccess(TeachingSchedule result) {
							iCourseDivisionsTable.setOffering(result, false);
							iTitleAndButtons.setHeaderTitle(result.getCourseName());
							iTitleAndButtons.setEnabled("back", false);
							iTitleAndButtons.setEnabled("course", true);
							iTitleAndButtons.setEnabled("division", false);
							iTitleAndButtons.setEnabled("divisionback", false);
							iTitleAndButtons.setEnabled("assignments", true);
							iTitleAndButtons.setEnabled("validate", false);
							iTitleAndButtons.setEnabled("save", false);

							clear();
							addHeaderRow(iTitleAndButtons);
							addRow(iCourseDivisionsTable);
							addRow(iStatus);
							addBottomRow(iBottomButtons);
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
		iTitleAndButtons.addButton("divisionback", MESSAGES.buttonDivisionsBack(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iCourseDivisionsTable.getOffering() != null) {
					iTitleAndButtons.setHeaderTitle(iCourseDivisionsTable.getOffering().getCourseName());
					iTitleAndButtons.setEnabled("back", iHasCourseId);
					iTitleAndButtons.setEnabled("course", !iHasCourseId);
					iTitleAndButtons.setEnabled("division", false);
					iTitleAndButtons.setEnabled("divisionback", false);
					iTitleAndButtons.setEnabled("assignments", true);
					iTitleAndButtons.setEnabled("validate", false);
					iTitleAndButtons.setEnabled("save", false);
					
					clear();
					addHeaderRow(iTitleAndButtons);
					addRow(iCourseDivisionsTable);
					addRow(iStatus); iStatus.clear();
					addBottomRow(iBottomButtons);
				}
			}
		});
		iTitleAndButtons.addButton("assignments", MESSAGES.buttonAssignments(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.pleaseWait());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {

						List<ValidationError> errors = iCourseDivisionsTable.getOffering().getDivisionErrors();
						if (!errors.isEmpty()) {
							String message = "";
							for (ValidationError error: errors) {
								message += (message.isEmpty() ? "" : "\n") + error.toString(MESSAGES, CONSTANTS);
							}
							iStatus.error(message);
							LoadingWidget.getInstance().hide();
							return;
						} else {
							iStatus.clear();
						}
						
						iTitleAndButtons.setEnabled("back", false);
						iTitleAndButtons.setEnabled("course", false);
						iTitleAndButtons.setEnabled("division", false);
						iTitleAndButtons.setEnabled("divisionback", true);
						iTitleAndButtons.setEnabled("assignments", false);
						iTitleAndButtons.setEnabled("validate", true);
						iTitleAndButtons.setEnabled("save", true);
						
						clear();
						addHeaderRow(iTitleAndButtons);
						iStatuses.clear();
						
						for (final Clazz clazz: iCourseDivisionsTable.getOffering().getClasses()) {
							final ScheduleStatus status = new ScheduleStatus();
							final UniTimeHeaderPanel panel = new UniTimeHeaderPanel(clazz.getName());
							panel.addButton("validate", MESSAGES.buttonValidate(), new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									List<ValidationError> errors = iCourseDivisionsTable.getOffering().getAssignmentErrors(clazz);
									if (!errors.isEmpty()) {
										String message = "";
										for (ValidationError error: errors) {
											message += (message.isEmpty() ? "" : "\n") + error.toString(MESSAGES, CONSTANTS);
										}
										status.error(message, false);
									} else {
										status.info(MESSAGES.validationOk(clazz.getName()), false);
									}
								}
							});
							addRow(panel);
							addRow(new TeachingScheduleAssignmentsTable(iCourseDivisionsTable.getOffering(), clazz, false));
							addRow(status);
							iStatuses.put(clazz, status);
						}
						
						addBottomRow(iBottomButtons);
						LoadingWidget.getInstance().hide();
					}
				});
			}
		});
		iTitleAndButtons.addButton("validate", MESSAGES.buttonValidateAll(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.pleaseWait());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						for (Map.Entry<Clazz, ScheduleStatus> e: iStatuses.entrySet()) {
							Clazz clazz = e.getKey();
							ScheduleStatus status = e.getValue();
							List<ValidationError> errors = iCourseDivisionsTable.getOffering().getAssignmentErrors(clazz);
							if (!errors.isEmpty()) {
								String message = "";
								for (ValidationError error: errors) {
									message += (message.isEmpty() ? "" : "\n") + error.toString(MESSAGES, CONSTANTS);
								}
								status.error(message, false);
							} else {
								status.info(MESSAGES.validationOk(clazz.getName()), false);
							}
						}
						LoadingWidget.getInstance().hide();
					}
				});
			}
		});
		iTitleAndButtons.addButton("save", MESSAGES.buttonSave(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.pleaseWait());
				RPC.execute(new SaveTeachingSchedule(iCourseDivisionsTable.getOffering()), new AsyncCallback<GwtRpcResponseNull>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.error(caught.getMessage(), caught);
					}

					@Override
					public void onSuccess(GwtRpcResponseNull result) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.info(MESSAGES.savedOK());
						ToolBox.open("gwt.jsp?page=teachingSchedule&course=" + iCourseDivisionsTable.getOffering().getCourseId());
					}
				});
			}
		});
		iTitleAndButtons.setEnabled("back", true);
		iTitleAndButtons.setEnabled("course", false);
		iTitleAndButtons.setEnabled("division", false);
		iTitleAndButtons.setEnabled("assignments", false);
		iTitleAndButtons.setEnabled("divisionback", false);
		iTitleAndButtons.setEnabled("validate", false);
		iTitleAndButtons.setEnabled("save", false);
		
		addHeaderRow(iTitleAndButtons);
		
		iCourseBox = new TeachingScheduleCourseSelectionBox();
		iCourseBox.setWidth("130px");
		addRow(MESSAGES.propInstructionalOffering(), iCourseBox);
		iCourseBox.addCourseSelectionHandler(new CourseSelectionHandler() {			
			@Override
			public void onCourseSelection(CourseSelectionEvent event) {
				iCourseDivisionsTable.setOffering(null, false);
				if (event.getValue() != null && event.getValue().hasCourseId()) {
					iTitleAndButtons.setEnabled("division", true);
				} else {
					iTitleAndButtons.setEnabled("division", false);
				}
				if (event.getValue() != null && event.getValue().hasCourseName()) {
					for (int i = 1; i < iAttributeTypes.getItemCount(); i++) {
						if (event.getValue().getCourseName().endsWith(iAttributeTypes.getValue(i))) {
							iAttributeTypes.setSelectedIndex(i); break;
						}
					}
				}
			}
		});
		
		iAttributeTypes = new ListBox();
		addRow(MESSAGES.propAttributeType(), iAttributeTypes);
		iAttributeTypes.addItem("", "");
		RPC.execute(new GetAttributeTypes(), new AsyncCallback<GwtRpcResponseList<AttributeType>>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(caught.getMessage(), caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<AttributeType> result) {
				for (AttributeType at: result) {
					iAttributeTypes.addItem(at.getLabel(), at.getReference());
				}
			}
		});
		
		iCourseDivisionsTable = new TeachingScheduleDivisionsTable();
		iStatus = new ScheduleStatus();
		
		iBottomButtons = iTitleAndButtons.clonePanel("");
		addBottomRow(iBottomButtons);
		
		if (Window.Location.getParameter("course") != null) {
			Long courseId = Long.valueOf(Window.Location.getParameter("course"));
			RPC.execute(new GetTeachingSchedule(courseId, null), new AsyncCallback<TeachingSchedule>() {
				@Override
				public void onSuccess(TeachingSchedule result) {
					iHasCourseId = true;
					iCourseDivisionsTable.setOffering(result, false);
					iTitleAndButtons.setHeaderTitle(result.getCourseName());
					iTitleAndButtons.setEnabled("back", true);
					iTitleAndButtons.setEnabled("course", false);
					iTitleAndButtons.setEnabled("division", false);
					iTitleAndButtons.setEnabled("divisionback", false);
					iTitleAndButtons.setEnabled("assignments", true);
					iTitleAndButtons.setEnabled("validate", false);
					iTitleAndButtons.setEnabled("save", false);

					clear();
					addHeaderRow(iTitleAndButtons);
					addRow(iCourseDivisionsTable);
					addRow(iStatus);
					addBottomRow(iBottomButtons);
				}
				@Override
				public void onFailure(Throwable caught) {
					UniTimeNotifications.error(caught.getMessage(), caught);
				}
			});
		}
	}
}
