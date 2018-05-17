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

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.ScriptPage.DateTimeBox;
import org.unitime.timetable.gwt.client.events.SessionDatesSelector;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.client.widgets.TimeSelector.TimeUtils;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.ScriptInterface;
import org.unitime.timetable.gwt.shared.TaskInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.ScriptParameterInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.DeleteTaskDetailsRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.ExecutionStatus;
import org.unitime.timetable.gwt.shared.TaskInterface.GetTaskExecutionLogRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.GetTaskOptionsRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.GetTasksRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.MultiExecutionInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.SaveTaskDetailsRpcRequest;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskExecutionInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskExecutionLogInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskOptionsInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class TasksPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private static DateTimeFormat sDateFormatShort = ServerDateTimeFormat.getFormat(CONSTANTS.eventDateFormatShort());
	private static DateTimeFormat sDateFormatLong = ServerDateTimeFormat.getFormat(CONSTANTS.eventDateFormatLong());
	private static DateTimeFormat sDateFormatMeeting = ServerDateTimeFormat.getFormat(CONSTANTS.meetingDateFormat());
	private static DateTimeFormat sDateFormatTS = ServerDateTimeFormat.getFormat(CONSTANTS.timeStampFormatShort());
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	private UniTimeTable<TaskInterface> iTasksTable;
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
				new TaskEditor(null).center();
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
		
		iTasksTable = new UniTimeTable<TaskInterface>();
		iTasksTable.addStyleName("unitime-PeriodicTaskTable");
		iForm.addRow(iTasksTable);
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(MESSAGES.colTaskName()));
		header.add(new UniTimeTableHeader(MESSAGES.colTaskScript()));
		header.add(new UniTimeTableHeader(MESSAGES.colTaskOwner()));
		header.add(new UniTimeTableHeader(MESSAGES.colTaskParameters()));
		header.add(new UniTimeTableHeader(MESSAGES.colTaskScheduleDate()));
		header.add(new UniTimeTableHeader(MESSAGES.colTaskScheduleTime()));
		header.add(new UniTimeTableHeader(MESSAGES.colTaskStatus()));
		iTasksTable.addRow(null, header);
		
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
			public void doBack() {
				iPanel.setWidget(iForm);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageTasks());
				loadTasks(getValue() == null ? null : getValue().getId());
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
		int scrollRow = -1;
		if (tasks != null) {
			for (TaskInterface task: tasks) {
				List<Widget> line = new ArrayList<Widget>();
				line.add(new Label(task.getName()));
				line.add(new Label(task.getScript().getName()));
				line.add(new Label(task.getOwner().getFormattedName(false)));
				String parameters = "";
				if (task.getScript().hasParameters())
					for (ScriptParameterInterface parameter: task.getScript().getParameters()) {
						String value = task.getParameter(parameter.getName());
						if (parameter.hasOptions() && value != null && !value.isEmpty()) {
							if (parameter.isMultiSelect()) {
								String ids = value;
								value = "";
								for (String id: ids.split(","))
									value += (value.isEmpty() ? "" : ", ") + parameter.getOption(id);
							} else {
								value = parameter.getOption(value);
							}
						}
						if ("slot".equalsIgnoreCase(parameter.getType()) || "time".equalsIgnoreCase(parameter.getType()) && value != null && !value.isEmpty()) {
							try {
								value = TimeUtils.slot2time(Integer.parseInt(value));
							} catch (Exception e) {}
						}
						if (value != null)
							parameters += (parameters.isEmpty() ? "" : "<br>") + parameter.getLabel() + ": " + value;
					}
				final P p = new P("parameters"); p.setHTML(parameters);
				p.addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						GwtHint.showHint(event.getRelativeElement(), new HTML(p.getHTML(), true));
					}
				});
				p.addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						GwtHint.hideHint();
					}
				});
				line.add(p);
				String dates = "";
				String times = "";
				String statuses = "";
				for (MultiExecutionInterface exec: TaskInterface.getMultiExecutions(task.getExecutions(), false)) {
					if (exec.getNrMeetings() == 1) {
						dates += (dates.isEmpty() ? "" : "<br>") + "<span class='status-" + exec.getStatus().name().toLowerCase() + "'>" +
								exec.getDays(iOptions.getFirstDayOfWeek(), CONSTANTS) + " " + sDateFormatLong.format(exec.getFirstExecutionDate()) +
								"</span>";
						times += (times.isEmpty() ? "" : "<br>") + "<span class='status-" + exec.getStatus().name().toLowerCase() + "'>" +
								exec.getExecutionTime(CONSTANTS) + "</span>";
					} else {
						dates += (dates.isEmpty() ? "" : "<br>") + "<span class='status-" + exec.getStatus().name().toLowerCase() + "'>" +
								exec.getDays(iOptions.getFirstDayOfWeek(), CONSTANTS) + " " + 
								sDateFormatShort.format(exec.getFirstExecutionDate()) + " - " + sDateFormatLong.format(exec.getLastExecutionDate())+
								"</span>";
						times += (times.isEmpty() ? "" : "<br>") + "<span class='status-" + exec.getStatus().name().toLowerCase() + "'>" +
								exec.getExecutionTime(CONSTANTS) + "</span>";
					}
					statuses += (statuses.isEmpty() ? "" : "<br>") + "<span class='status-" + exec.getStatus().name().toLowerCase() + "'>" +
							CONSTANTS.taskStatus()[exec.getStatus().ordinal()] + "</span>";
				}
				line.add(new HTML(dates, false));
				line.add(new HTML(times, false));
				line.add(new HTML(statuses, false));
				iTasksTable.addRow(task, line);
				if (taskId != null && taskId.equals(task.getId())) scrollRow = iTasksTable.getRowCount() - 1;
			}
		}
		if (scrollRow >= 0)
			iTasksTable.getRowFormatter().getElement(scrollRow).scrollIntoView();
	}
	
	public class TaskDetail extends SimpleForm implements TakesValue<TaskInterface> {
		private UniTimeHeaderPanel iHeader, iFooter, iLogHeader;
		private TaskInterface iTask;
		private UniTimeTable<TaskExecutionInterface> iExecutions;
		private int iLogRow;
		private HTML iLog;
		
		public TaskDetail() {
			iHeader = new UniTimeHeaderPanel();
			iHeader.addButton("edit", MESSAGES.buttonEditTask(), 75, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					new TaskEditor(getValue()){
						@Override
						protected void doReload(TaskInterface task) {
							setValue(task);
						}
					}.center();
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
			iExecutions = new UniTimeTable<TaskExecutionInterface>();
			iExecutions.setAllowSelection(true);
			iExecutions.setAllowMultiSelect(false);
			iExecutions.addStyleName("unitime-PeriodicTaskTable");
			List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
			header.add(new UniTimeTableHeader(MESSAGES.colTaskScheduleDate()));
			header.add(new UniTimeTableHeader(MESSAGES.colTaskScheduleTime()));
			header.add(new UniTimeTableHeader(MESSAGES.colTaskQueued()));
			header.add(new UniTimeTableHeader(MESSAGES.colTaskStarted()));
			header.add(new UniTimeTableHeader(MESSAGES.colTaskFinished()));
			header.add(new UniTimeTableHeader(MESSAGES.colTaskStatus()));
			header.add(new UniTimeTableHeader(MESSAGES.colTaskStatusMessage()));
			header.add(new UniTimeTableHeader(MESSAGES.colTaskOutput()));
			iExecutions.addRow(null, header);
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
		
		public void doBack() {}

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
			iExecutions.clearTable(1);
			for (TaskExecutionInterface e: iTask.getExecutions()) {
				List<Widget> line = new ArrayList<Widget>();
				line.add(new Label(sDateFormatMeeting.format(e.getExecutionDate())));
				line.add(new Label(e.getExecutionTime(CONSTANTS)));
				line.add(new Label(e.getQueued() == null ? "" : sDateFormatTS.format(e.getQueued())));
				line.add(new Label(e.getStarted() == null ? "" : sDateFormatTS.format(e.getStarted())));
				line.add(new Label(e.getFinished() == null ? "" : sDateFormatTS.format(e.getFinished())));
				line.add(new Label(CONSTANTS.taskStatus()[e.getStatus().ordinal()]));
				Label message = new Label(e.getStatusMessage() == null ? "" : e.getStatusMessage()); message.addStyleName("status-message");
				if (e.getStatusMessage() != null)
					message.setTitle(e.getStatusMessage());
				line.add(message);
				if (e.getOutput() != null) {
					line.add(new Anchor(e.getOutput(), GWT.getHostPageBaseURL() + "/taskfile?e=" + e.getId()));
				} else {
					line.add(new Label(""));
				}
				for (Widget w: line)
					if (w != null)
						w.addStyleName("status-" + e.getStatus().name().toLowerCase());
				iExecutions.addRow(e, line);
			}
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
	
	public class TaskEditor extends UniTimeDialogBox {
		private SimpleForm iDialogForm;
		private TaskInterface iTask;
		private TextBox iName;
		private SessionDatesSelector iDates;
		private TimeSelector iTime;
		private ListBox iScript;
		private UniTimeHeaderPanel iBottom;
		private int iDescriptionRow = -1;
		private HTML iDescription;
		private CheckBox iSendEmail;
		private TextArea iEmailAddresses;
		private VerticalPanel iEmailPanel;
		private SimpleForm iForm;
		private ScrollPanel iScroll;
		
		public TaskEditor(TaskInterface task) {
			super(false, true);
			addStyleName("unitime-PeriodicTaskEditor");
			iTask = task;
			if (task == null) 
				iTask = new TaskInterface();
			setText(iTask.getId() == null ? MESSAGES.dialogAddTask() : MESSAGES.dialogEditTask(iTask.getName()));
			iDialogForm = new SimpleForm(2);
			
			iForm = new SimpleForm(); iForm.removeStyleName("unitime-NotPrintableBottomLine");
			iScroll = new ScrollPanel(iForm);
			iScroll.setStyleName("unitime-VerticalScrollPanel");
			iDialogForm.addRow(iScroll);
			
			iName = new TextBox();
			if (iTask.getName() != null) iName.setText(iTask.getName());
			iName.setStyleName("unitime-TextBox");
			iName.setWidth("400px");
			iName.setMaxLength(128);
			iName.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					iBottom.clearMessage();
				}
			});
			iForm.addRow(MESSAGES.propName(), iName);
			
			if (iTask.getOwner() != null) {
				iForm.addRow(MESSAGES.propTaskOwner(), new Label(iTask.getOwner().getFormattedName(false)));
			}
			
			iDates = new SessionDatesSelector(iOptions.getSessionMonths());
			if (iTask.hasExecutions()) {
				List<Date> dates = new ArrayList<Date>();
				for (TaskExecutionInterface e: iTask.getExecutions()) {
					if (e.getStatus() == ExecutionStatus.CREATED)
						dates.add(e.getExecutionDate());
				}
				iDates.setValue(dates);
			}
			iForm.addRow(MESSAGES.propTaskExecutionDates(), iDates);
			iDates.addValueChangeHandler(new ValueChangeHandler<List<Date>>() {
				@Override
				public void onValueChange(ValueChangeEvent<List<Date>> event) {
					iBottom.clearMessage();
				}
			});
			
			iTime = new TimeSelector();
			if (iTask.hasExecutions())
				iTime.setValue(iTask.getExecutions().last().getSlot());
			iForm.addRow(MESSAGES.propTaskStartTime(), iTime);
			iTime.addValueChangeHandler(new ValueChangeHandler<Integer>() {
				@Override
				public void onValueChange(ValueChangeEvent<Integer> event) {
					iBottom.clearMessage();
				}
			});
			
			if (iTask.getScript() != null) {
				iForm.addRow(MESSAGES.propScript(), new Label(iTask.getScript().getName()));
			} else {
				iScript = new ListBox();
				iScript.addItem(MESSAGES.itemSelect(), "-1");
				iScript.setSelectedIndex(0);
				for (ScriptInterface script: iOptions.getScripts()) {
					if (!script.canExecute() && !script.canEdit() && !script.canDelete()) continue;
					iScript.addItem(script.getName(), script.getId().toString());
				}
				iForm.addRow(MESSAGES.propScript(), iScript);
				iScript.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						scriptChanged(true);
						iBottom.clearMessage();
					}
				});
			}
			
			iDescription = new HTML();
			iDescriptionRow = iForm.addRow(MESSAGES.propDescription(), iDescription);
			if (iTask.getScript() != null)
				iDescription.setHTML(iTask.getScript().getDescription());
			
			iSendEmail = new CheckBox(MESSAGES.scriptSendEmailCheckbox()); iSendEmail.setValue(false);
			iEmailAddresses = new TextArea();
			iEmailAddresses.setStyleName("unitime-TextArea");
			iEmailAddresses.setVisibleLines(3);
			iEmailAddresses.setCharacterWidth(80);
			iEmailPanel = new VerticalPanel();
			iEmailPanel.add(iSendEmail);
			iEmailPanel.setCellHorizontalAlignment(iSendEmail, HasHorizontalAlignment.ALIGN_LEFT);
			iEmailPanel.add(iEmailAddresses);
			iEmailAddresses.setVisible(false);
			iSendEmail.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iEmailAddresses.setVisible(event.getValue());
				}
			});
			if (iTask.getEmail() != null) {
				iSendEmail.setValue(true);
				iEmailAddresses.setVisible(true);
				iEmailAddresses.setText(iTask.getEmail());
			}
			if (iTask.getId() == null && iOptions != null && iOptions.getManager() != null && iOptions.getManager().getEmail() != null)
				iEmailAddresses.setText(iOptions.getManager().getEmail());
			
			iForm.addRow(MESSAGES.propEmail(), iEmailPanel);
			
			iBottom = new UniTimeHeaderPanel();
			iBottom.addButton("save", iTask.getId() == null ? MESSAGES.opTaskSave() : MESSAGES.opTaskUpdate(), 75, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (!validate()) return;
					hide();
					LoadingWidget.getInstance().show(iTask.getId() == null ? MESSAGES.waitCreate(iName.getText()) : MESSAGES.waitUpdate(iName.getText()));
					RPC.execute(new SaveTaskDetailsRpcRequest(iTask), new AsyncCallback<TaskInterface>(){
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							if (iTask.getId() == null) {
								iBottom.setErrorMessage(MESSAGES.failedCreate(iName.getText(), caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedCreate(iName.getText(), caught.getMessage()), caught);
							} else {
								iBottom.setErrorMessage(MESSAGES.failedUpdate(iName.getText(), caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedUpdate(iName.getText(), caught.getMessage()), caught);
							}
							center();
						}

						@Override
						public void onSuccess(TaskInterface result) {
							LoadingWidget.getInstance().hide();
							doReload(result);
						}
					});
				}
			});
			/*
			iBottom.addButton("delete", MESSAGES.opTaskDelete(), 75, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (iTask.getId() != null) {
						hide();
						LoadingWidget.getInstance().show(MESSAGES.waitDelete(iName.getText()));
						RPC.execute(new DeleteTaskDetailsRpcRequest(iTask.getId()), new AsyncCallback<TaskInterface>(){
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								iBottom.setErrorMessage(MESSAGES.failedDelete(iName.getText(), caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedDelete(iName.getText(), caught.getMessage()), caught);
								center();
							}

							@Override
							public void onSuccess(TaskInterface result) {
								LoadingWidget.getInstance().hide();
								doReload(result);
							}
						});
					}
				}
			});
			iBottom.setEnabled("delete", iTask.getId() != null);
			*/
			iBottom.addButton("back", MESSAGES.opTaskBack(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
				}
			});
			iDialogForm.addBottomRow(iBottom);
			
			setWidget(iDialogForm);
			
			if (iTask.getScript() != null)
				scriptChanged(false);
			
			ToolBox.setMaxHeight(iScroll.getElement().getStyle(), (Window.getClientHeight() - 100) + "px");
		}
		
		protected void doReload(TaskInterface task) {
			loadTasks(task == null ? null : task.getId());
		}
		
		private boolean validate() {
			ScriptInterface script = getScript();
			if (iName.getText().isEmpty()) {
				iBottom.setErrorMessage(MESSAGES.errorNameIsRequired());
				return false;
			}
			iTask.setName(iName.getText());
			if (script == null) {
				iBottom.setErrorMessage(MESSAGES.errorItemNotSelected(MESSAGES.colTaskScript()));
				return false;
			} else {
				iTask.setScript(script);
			}
			List<Integer> dates = iDates.getSelectedDays();
			if (dates == null || dates.isEmpty()) {
				iBottom.setErrorMessage(MESSAGES.errorNoDateSelected());
				return false;
			}
			Integer slot = iTime.getValue();
			if (slot == null) {
				iBottom.setErrorMessage(MESSAGES.errorNoStartTime());
				return false;
			}
			iTask.clearExecutions();
			for (Integer date: dates) {
				TaskExecutionInterface e = new TaskExecutionInterface();
				e.setDayOfYear(date); e.setSlot(slot);
				iTask.addExecution(e);
			}
			iTask.setEmail(iSendEmail.getValue() && !iEmailAddresses.getText().isEmpty() ? iEmailAddresses.getText() : null);
			iBottom.clearMessage();
			return true;
		}
		
		private ScriptInterface getScript() {
			if (iOptions != null && iScript != null) {
				for (ScriptInterface s: iOptions.getScripts())
					if (s.getId().toString().equals(iScript.getValue(iScript.getSelectedIndex()))) {
						return s;
					}
			}
			
			return iTask.getScript();
		}
		
		private void scriptChanged(boolean clear) {
			ScriptInterface script = getScript();
			if (script == null) {
				iForm.getRowFormatter().setVisible(iDescriptionRow, false);
				while (iForm.getRowCount() > iDescriptionRow + 2)
					iForm.removeRow(1 + iDescriptionRow);
				iBottom.setEnabled("save", false);
				iTask.clearParameters();
			} else {
				iDescription.setHTML(script.getDescription());
				iForm.getRowFormatter().setVisible(iDescriptionRow, script.getDescription() != null && !script.getDescription().isEmpty());
				iBottom.setEnabled("save", script.canExecute());
				if (clear) iTask.clearParameters();
				while (iForm.getRowCount() > iDescriptionRow + 2)
					iForm.removeRow(1 + iDescriptionRow);
				if (script.hasParameters()) {
					for (final ScriptParameterInterface param: script.getParameters()) {
						if (param.getValue() != null) iTask.setParameter(param.getName(), param.getValue());
						String defaultValue = iTask.getParameter(param.getName());
						if (defaultValue == null) defaultValue = param.getValue();
						if (defaultValue == null) defaultValue = param.getDefaultValue();
						Widget widget = null;
						if (param.hasOptions()) {
							final ListBox list = new ListBox();
							list.setMultipleSelect(param.isMultiSelect());
							if (!param.isMultiSelect()) list.addItem(MESSAGES.itemSelect());
							for (ScriptInterface.ListItem item: param.getOptions()) {
								list.addItem(item.getText(), item.getValue());
								if (defaultValue != null) {
									if (param.isMultiSelect()) {
										for (String id: defaultValue.split(","))
											if (!id.isEmpty() && (id.equalsIgnoreCase(item.getValue()) || id.equalsIgnoreCase(item.getText()) || item.getText().startsWith(id + " - "))) {
												list.setItemSelected(list.getItemCount() - 1, true); break;
											}
									} else if (defaultValue.equalsIgnoreCase(item.getValue()) || defaultValue.equalsIgnoreCase(item.getText()) || item.getText().startsWith(defaultValue + " - "))
										list.setSelectedIndex(list.getItemCount() - 1);
								}
							}
							list.addChangeHandler(new ChangeHandler() {
								@Override
								public void onChange(ChangeEvent event) {
									if (param.isMultiSelect()) {
										String value = "";
										for (int i = 0; i < list.getItemCount(); i++)
											if (list.isItemSelected(i))
												value += (value.isEmpty() ? "" : ",") + list.getValue(i);
										iTask.setParameter(param.getName(), value);
									} else {
										iTask.setParameter(param.getName(), list.getValue(list.getSelectedIndex()));
									}
								}
							});
							widget = list;
						} else if ("boolean".equalsIgnoreCase(param.getType())) {
							CheckBox ch = new CheckBox();
							ch.setValue("true".equalsIgnoreCase(defaultValue));
							ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
								@Override
								public void onValueChange(ValueChangeEvent<Boolean> event) {
									iTask.setParameter(param.getName(), event.getValue() ? "true" : "false");
								}
							});
							widget = ch;
						} else if ("file".equalsIgnoreCase(param.getType())) {
							UniTimeFileUpload upload = new UniTimeFileUpload(); upload.reset();
							widget = upload;
						} else if ("textarea".equalsIgnoreCase(param.getType())) {
							TextArea textarea = new TextArea();
							textarea.setStyleName("unitime-TextArea");
							textarea.setVisibleLines(5);
							textarea.setCharacterWidth(80);
							if (defaultValue != null) textarea.setText(defaultValue);
							textarea.addValueChangeHandler(new ValueChangeHandler<String>() {
								@Override
								public void onValueChange(ValueChangeEvent<String> event) {
									iTask.setParameter(param.getName(), event.getValue());
								}
							});
							widget = textarea;
						} else if ("integer".equalsIgnoreCase(param.getType()) || "int".equalsIgnoreCase(param.getType()) || "long".equalsIgnoreCase(param.getType()) || "short".equalsIgnoreCase(param.getType()) || "byte".equalsIgnoreCase(param.getType())) {
							NumberBox text = new NumberBox();
							text.setDecimal(false); text.setNegative(true);
							if (defaultValue != null)
								text.setText(defaultValue);
							text.addValueChangeHandler(new ValueChangeHandler<String>() {
								@Override
								public void onValueChange(ValueChangeEvent<String> event) {
									iTask.setParameter(param.getName(), event.getValue());
								}
							});
							widget = text;
						} else if ("number".equalsIgnoreCase(param.getType()) || "float".equalsIgnoreCase(param.getType()) || "double".equalsIgnoreCase(param.getType())) {
							NumberBox text = new NumberBox();
							text.setDecimal(true); text.setNegative(true);
							if (defaultValue != null)
								text.setText(defaultValue);
							text.addValueChangeHandler(new ValueChangeHandler<String>() {
								@Override
								public void onValueChange(ValueChangeEvent<String> event) {
									iTask.setParameter(param.getName(), event.getValue());
								}
							});
							widget = text;
						} else if ("date".equalsIgnoreCase(param.getType())) {
							SingleDateSelector text = new SingleDateSelector();
							if (defaultValue != null)
								text.setText(defaultValue);
							final DateTimeFormat format = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
							text.addValueChangeHandler(new ValueChangeHandler<Date>() {
								@Override
								public void onValueChange(ValueChangeEvent<Date> event) {
									iTask.setParameter(param.getName(), format.format(event.getValue()));
								}
							});
							widget = text;
						} else if ("slot".equalsIgnoreCase(param.getType()) || "time".equalsIgnoreCase(param.getType())) {
							TimeSelector text = new TimeSelector();
							if (defaultValue != null)
								text.setText(defaultValue);
							text.addValueChangeHandler(new ValueChangeHandler<Integer>() {
								@Override
								public void onValueChange(ValueChangeEvent<Integer> event) {
									iTask.setParameter(param.getName(), event.getValue().toString());
								}
							});
							widget = text;
						} else if ("datetime".equalsIgnoreCase(param.getType()) || "timestamp".equalsIgnoreCase(param.getType())) {
							DateTimeBox text = new DateTimeBox();
							if (defaultValue != null)
								text.setText(defaultValue);
							text.addValueChangeHandler(new ValueChangeHandler<String>() {
								@Override
								public void onValueChange(ValueChangeEvent<String> event) {
									iTask.setParameter(param.getName(), event.getValue());
								}
							});
							widget = text;
						} else {
							TextBox text = new TextBox();
							text.setStyleName("unitime-TextBox");
							text.setWidth("400px");
							if (defaultValue != null)
								text.setText(defaultValue);
							text.addValueChangeHandler(new ValueChangeHandler<String>() {
								@Override
								public void onValueChange(ValueChangeEvent<String> event) {
									iTask.setParameter(param.getName(), event.getValue());
								}
							});
							widget = text;
						}
						int row = iForm.insertRow(iForm.getRowCount() - 1);
						iForm.setWidget(row, 0, new Label((param.getLabel() == null || param.getLabel().isEmpty() ? param.getName() : param.getLabel()) + ":", false));
						iForm.setWidget(row, 1, widget);
					}
				}
			}
			if (isShowing()) center();
		}
		
	}
}
