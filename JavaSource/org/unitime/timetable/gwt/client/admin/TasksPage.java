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

import java.util.List;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.TaskInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.GetTaskOptionsRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.GetTasksRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.SaveTaskDetailsRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskOptionsInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Tomas Muller
 */
public class TasksPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	private TasksTable iTasksTable;
	private TaskOptionsInterface iOptions = null;
	private TaskDetail iTaskDetail;
	private SimplePanel iPanel;
	
	public TasksPage() {
		iForm = new SimpleForm(2);
		iForm.removeStyleName("unitime-NotPrintableBottomLine");
		
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectScheduledTasks("..."));
		iHeader.addButton("add", MESSAGES.buttonAddNewTask(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				new TaskEditor(null, iOptions) {
					@Override
					protected void doSave(final TaskInterface task) {
						LoadingWidget.getInstance().show(task.getId() == null ? MESSAGES.waitCreate(task.getName()) : MESSAGES.waitUpdate(task.getName()));
						RPC.execute(new SaveTaskDetailsRpcRequest(task), new AsyncCallback<TaskInterface>(){
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								if (task.getId() == null) {
									setErrorMessage(MESSAGES.failedCreate(task.getName(), caught.getMessage()));
									UniTimeNotifications.error(MESSAGES.failedCreate(task.getName(), caught.getMessage()), caught);
								} else {
									setErrorMessage(MESSAGES.failedUpdate(task.getName(), caught.getMessage()));
									UniTimeNotifications.error(MESSAGES.failedUpdate(task.getName(), caught.getMessage()), caught);
								}
								center();
							}

							@Override
							public void onSuccess(TaskInterface result) {
								LoadingWidget.getInstance().hide();
								loadTasks(result == null ? null : result.getId());
							}
						});
					}
				}.center();
			}
		});
		iHeader.setEnabled("add", false);
		iHeader.addButton("refresh", MESSAGES.buttonRefresh(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadTasks(null);
			}
		});
		iForm.addHeaderRow(iHeader);
		
		iTasksTable = new TasksTable();
		iForm.addRow(iTasksTable);
		
		iFooter = iHeader.clonePanel("");
		iForm.addBottomRow(iFooter);
		
		iPanel = new SimplePanel(iForm);
		initWidget(iPanel);
		
		iHeader.showLoading();
		RPC.execute(new GetTaskOptionsRpcRequest(), new AsyncCallback<TaskOptionsInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
			}
			@Override
			public void onSuccess(TaskOptionsInterface result) {
				iOptions = result;
				iTasksTable.setOptions(result);
				iHeader.setEnabled("add", iOptions.canAdd());
				iHeader.setHeaderTitle(MESSAGES.sectScheduledTasks(iOptions.getSession().getName()));
				loadTasks(null);
			}
		});
		
		iTasksTable.addMouseClickListener(new MouseClickListener<TaskInterface>() {
			@Override
			public void onMouseClick(final TableEvent<TaskInterface> event) {
				/*
				if (event.getData() != null && event.getData().canEdit()) {
					new TaskEditor(new TaskInterface(event.getData())).center();
				}
				*/
				if (event.getData() != null && event.getData().canView()) {
					iTaskDetail.setValue(event.getData());
					UniTimePageLabel.getInstance().setPageName(MESSAGES.pageTaskDetails());
					iPanel.setWidget(iTaskDetail);
					Window.scrollTo(0, 0);
					GwtHint.hideHint();
				}
			}
		});
		
		iTaskDetail = new TaskDetail() {
			protected void doBack() {
				iPanel.setWidget(iForm);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageTasks());
				loadTasks(getValue() == null ? null : getValue().getId());
			}
			protected void doEdit() {
				new TaskEditor(getValue(), iOptions) {
					@Override
					protected void doSave(final TaskInterface task) {
						LoadingWidget.getInstance().show(task.getId() == null ? MESSAGES.waitCreate(task.getName()) : MESSAGES.waitUpdate(task.getName()));
						RPC.execute(new SaveTaskDetailsRpcRequest(task), new AsyncCallback<TaskInterface>(){
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								if (task.getId() == null) {
									setErrorMessage(MESSAGES.failedCreate(task.getName(), caught.getMessage()));
									UniTimeNotifications.error(MESSAGES.failedCreate(task.getName(), caught.getMessage()), caught);
								} else {
									setErrorMessage(MESSAGES.failedUpdate(task.getName(), caught.getMessage()));
									UniTimeNotifications.error(MESSAGES.failedUpdate(task.getName(), caught.getMessage()), caught);
								}
								center();
							}

							@Override
							public void onSuccess(TaskInterface result) {
								LoadingWidget.getInstance().hide();
								if (result != null)
									setValue(result);
								else
									doBack();
							}
						});
					}
				}.center();
			}
		};
	}
	
	protected void loadTasks(final Long taskId) {
		iHeader.showLoading();
		RPC.execute(new GetTasksRpcRequest(), new AsyncCallback<GwtRpcResponseList<TaskInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<TaskInterface> result) {
				iHeader.clearMessage();
				populateTasks(result, taskId);
			}
		});
	}
	
	protected void populateTasks(List<TaskInterface> tasks, Long taskId) {
		iTasksTable.clearTable(1);
		iTasksTable.setValue(tasks);
		if (taskId != null) iTasksTable.scrollToTask(taskId);
	}
}
