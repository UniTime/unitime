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

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.ScriptPage.DateTimeBox;
import org.unitime.timetable.gwt.client.events.SessionDatesSelector;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.ScriptInterface;
import org.unitime.timetable.gwt.shared.TaskInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.ScriptParameterInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.ExecutionStatus;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskExecutionInterface;
import org.unitime.timetable.gwt.shared.TaskInterface.TaskOptionsInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class TaskEditor extends UniTimeDialogBox {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	
	private TaskOptionsInterface iOptions;
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
	
	public TaskEditor(TaskInterface task, TaskOptionsInterface options) {
		super(false, true);
		iOptions = options;
		
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
					dates.add(ServerDateTimeFormat.toLocalDate(e.getExecutionDate()));
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
				doSave(iTask);
			}
		});
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
	
	protected void doSave(TaskInterface task) {}
	
	protected void setErrorMessage(String message) {
		iBottom.setErrorMessage(message);
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
