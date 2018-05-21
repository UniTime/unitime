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

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.TimeSelector.TimeUtils;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.TaskInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.ScriptParameterInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.DeleteTaskDetailsRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.GetTaskExecutionLogRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskExecutionInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskExecutionLogInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Tomas Muller
 */
public class TaskDetail extends SimpleForm implements TakesValue<TaskInterface> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private static DateTimeFormat sDateFormatMeeting = ServerDateTimeFormat.getFormat(CONSTANTS.meetingDateFormat());
	
	private UniTimeHeaderPanel iHeader, iFooter, iLogHeader;
	private TaskInterface iTask;
	private TaskExecutionsTable iExecutions;
	private int iLogRow;
	private HTML iLog;
	
	public TaskDetail() {
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("edit", MESSAGES.buttonEditTask(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doEdit();
			}
		});
		iHeader.addButton("delete", MESSAGES.buttonDeleteTask(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(MESSAGES.confirmDeleteTask(), new Command() {
					@Override
					public void execute() {
						LoadingWidget.getInstance().show(MESSAGES.waitDelete(getValue().getName()));
						RPC.execute(new DeleteTaskDetailsRpcRequest(getValue().getId()), new AsyncCallback<TaskInterface>(){
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								iHeader.setErrorMessage(MESSAGES.failedDelete(getValue().getName(), caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedDelete(getValue().getName(), caught.getMessage()), caught);
							}

							@Override
							public void onSuccess(TaskInterface result) {
								LoadingWidget.getInstance().hide();
								doBack();
							}
						});
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doBack();
			}
		});
		iFooter = iHeader.clonePanel();
		iExecutions = new TaskExecutionsTable();
		iExecutions.setAllowSelection(true);
		iExecutions.setAllowMultiSelect(false);
		iExecutions.addMouseClickListener(new MouseClickListener<TaskExecutionInterface>() {
			@Override
			public void onMouseClick(TableEvent<TaskExecutionInterface> event) {
				if (iExecutions.getSelectedRow() > 0) {
					final TaskExecutionInterface execution = iExecutions.getData(iExecutions.getSelectedRow());
					RPC.execute(new GetTaskExecutionLogRpcRequest(execution.getId()), new AsyncCallback<TaskExecutionLogInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
							getRowFormatter().setVisible(iLogRow, false);
							getRowFormatter().setVisible(iLogRow + 1, false);
						}

						@Override
						public void onSuccess(TaskExecutionLogInterface result) {
							if (result != null && result.hasLog()) {
								iLogHeader.setHeaderTitle(MESSAGES.sectScriptLog(sDateFormatMeeting.format(execution.getExecutionDate()) + " " + execution.getExecutionTime(CONSTANTS)));
								iLog.setHTML(result.getLog());
								getRowFormatter().setVisible(iLogRow, true);
								getRowFormatter().setVisible(iLogRow + 1, true);
							} else {
								getRowFormatter().setVisible(iLogRow, false);
								getRowFormatter().setVisible(iLogRow + 1, false);
							}
						}
					});
				} else {
					getRowFormatter().setVisible(iLogRow, false);
					getRowFormatter().setVisible(iLogRow + 1, false);
				}
			}
		});
		
		iLogHeader = new UniTimeHeaderPanel();
		iLog = new HTML();
	}
	
	protected void doBack() {}
	
	protected void doEdit() {}

	@Override
	public void setValue(TaskInterface task) {
		iTask = task;
		clear();
		iHeader.setHeaderTitle(iTask.getName());
		iHeader.clearMessage(); iHeader.setEnabled("edit", iTask.canEdit()); iHeader.setEnabled("delete", iTask.canEdit());
		addHeaderRow(iHeader);
		addRow(MESSAGES.propScript(), new Label(iTask.getScript().getName()));
		addRow(MESSAGES.propTaskOwner(), new Label(iTask.getOwner().getFormattedName(false)));
		if (iTask.getScript().getDescription() != null && !iTask.getScript().getDescription().isEmpty())
			addRow(MESSAGES.propDescription(), new HTML(iTask.getScript().getDescription()));
		if (iTask.getScript().hasParameters())
			for (ScriptParameterInterface parameter: iTask.getScript().getParameters()) {
				String value = iTask.getParameter(parameter.getName());
				if (value == null) value = parameter.getValue();
				if (value == null) value = parameter.getDefaultValue();
				if (parameter.hasOptions() && value != null && !value.isEmpty()) {
					if (parameter.isMultiSelect()) {
						String ids = value;
						value = "";
						for (String id: ids.split(","))
							value += (value.isEmpty() ? "" : "<br>") + parameter.getOption(id);
					} else {
						value = parameter.getOption(value);
					}
				}
				if ("slot".equalsIgnoreCase(parameter.getType()) || "time".equalsIgnoreCase(parameter.getType()) && value != null && !value.isEmpty()) {
					try {
						value = TimeUtils.slot2time(Integer.parseInt(value));
					} catch (Exception e) {}
				}
				if ("file".equalsIgnoreCase(parameter.getType()) && value != null) {
					addRow(parameter.getLabel() + ":", new Anchor(value, GWT.getHostPageBaseURL() + "/taskfile?t=" + iTask.getId()));
				} else {
					addRow(parameter.getLabel() + ":", new HTML(value == null ? "<i>" + MESSAGES.notSet() + "</i>" : value));
				}
			}
		if (iTask.getEmail() != null && !iTask.getEmail().isEmpty()) {
			addRow(MESSAGES.propEmail(), new HTML(iTask.getEmail().replace("\n", "<br>")));
		}
		addHeaderRow(MESSAGES.sectTaskExecutions());
		iExecutions.setValue(iTask.getExecutions());
		addRow(iExecutions);
		iLogRow = addHeaderRow(iLogHeader);
		iLog.setHTML("");
		addRow(iLog);
		getRowFormatter().setVisible(iLogRow, false);
		getRowFormatter().setVisible(iLogRow + 1, false);
		
		addBottomRow(iFooter);
	}

	@Override
	public TaskInterface getValue() {
		return iTask;
	}
}