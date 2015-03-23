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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.ScriptInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.ScriptInterface.DeleteScriptRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.ExecuteScriptRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.GetScriptOptionsRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.LoadAllScriptsRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.SaveOrUpdateScriptRpcRequest;
import org.unitime.timetable.gwt.shared.ScriptInterface.ScriptOptionsInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.ScriptParameterInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class ScriptPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private static DateTimeFormat sTS = DateTimeFormat.getFormat(CONSTANTS.timeStampFormatShort());

	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iBottom, iQueueHeader, iLogHeader;
	private ListBox iName;
	private HTML iDescription;
	private int iDescriptionRow;
	
	private UniTimeTable<QueueItemInterface> iQueue;
	private HTML iLog;
	private int iQueueRow, iLogRow;
	
	private List<ScriptInterface> iScripts = null;
	private Map<String, String> iParams = new HashMap<String, String>();
	
	private SaveOrUpdateDialog iDialog = new SaveOrUpdateDialog();
	private int iLastSelectedRow = -1;
	
	public ScriptPage() {
		iForm = new SimpleForm(2);
		iForm.removeStyleName("unitime-NotPrintableBottomLine");
		
		iQueueHeader = new UniTimeHeaderPanel(MESSAGES.sectScriptQueue());
		iQueueHeader.addButton("refresh", MESSAGES.buttonRefresh(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refreshQueue(null, null);
			}
		});
		iQueueRow = iForm.addHeaderRow(iQueueHeader);
		
		iQueue = new UniTimeTable<ScriptInterface.QueueItemInterface>();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(MESSAGES.colName()));
		header.add(new UniTimeTableHeader(MESSAGES.colStatus()));
		header.add(new UniTimeTableHeader(MESSAGES.colProgress()));
		header.add(new UniTimeTableHeader(MESSAGES.colOwner()));
		header.add(new UniTimeTableHeader(MESSAGES.colSession()));
		header.add(new UniTimeTableHeader(MESSAGES.colCreated()));
		header.add(new UniTimeTableHeader(MESSAGES.colStarted()));
		header.add(new UniTimeTableHeader(MESSAGES.colFinished()));
		header.add(new UniTimeTableHeader(MESSAGES.colOutput()));
		header.add(new UniTimeTableHeader(""));
		iQueue.addRow(null, header);
		iQueue.setAllowSelection(true);
		iForm.addRow(iQueue);
		
		iLogHeader = new UniTimeHeaderPanel();
		iLogRow = iForm.addHeaderRow(iLogHeader);
		iLog = new HTML();
		iForm.addRow(iLog);
		
		iForm.getRowFormatter().setVisible(iQueueRow, false);
		iForm.getRowFormatter().setVisible(iQueueRow + 1, false);
		iForm.getRowFormatter().setVisible(iLogRow, false);
		iForm.getRowFormatter().setVisible(iLogRow + 1, false);
		
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectScript());
		
		iHeader.addButton("execute", MESSAGES.buttonExecute(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!Window.confirm(MESSAGES.confirmScriptExecution(iName.getItemText(iName.getSelectedIndex())))) return;
				LoadingWidget.getInstance().show(MESSAGES.waitExecuting(iName.getItemText(iName.getSelectedIndex())));
				RPC.execute(ExecuteScriptRpcRequest.executeScript(Long.valueOf(iName.getValue(iName.getSelectedIndex())), iName.getItemText(iName.getSelectedIndex()), iParams),
						new AsyncCallback<QueueItemInterface>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								iHeader.setErrorMessage(MESSAGES.failedExecution(caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedExecution(caught.getMessage()), caught);
							}

							@Override
							public void onSuccess(QueueItemInterface result) {
								LoadingWidget.getInstance().hide();
								iHeader.clearMessage();
								refreshQueue(null, result == null ? null : result.getId());
							}
						});
			}
		});
		iHeader.setEnabled("execute", false);
		
		iHeader.addButton("add", MESSAGES.buttonAddNew(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iDialog.addScript();
			}
		});
		iHeader.setEnabled("add", false);
		
		iHeader.addButton("edit", MESSAGES.buttonEdit(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ScriptInterface script = getScript();
				if (script != null)
					iDialog.editScript(script);
			}
		});
		iHeader.setEnabled("edit", false);

		
		iForm.addHeaderRow(iHeader);
		
		iName = new ListBox();
		iName.addItem(MESSAGES.itemSelect(), "-1");
		iName.setSelectedIndex(0);
		
		iForm.addRow(MESSAGES.propName(), iName);
		
		iDescription = new HTML();
		iDescriptionRow = iForm.addRow(MESSAGES.propDescription(), iDescription);
		iForm.getRowFormatter().setVisible(iDescriptionRow, false);
		
		iBottom = iHeader.clonePanel("");
		iForm.addBottomRow(iBottom);
		
		initWidget(iForm);
		
		iName.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				scriptChanged();
				if (iName.getItemCount() > 0)
					History.newItem(iName.getValue(iName.getSelectedIndex()), false);
			}
		});
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				reload(Long.valueOf(event.getValue()));
			}
		});
		
		reload(History.getToken() == null || History.getToken().isEmpty() ? null : Long.valueOf(History.getToken()));
		
		refreshQueue(null, null);
		new Timer() {
			@Override
			public void run() {
				refreshQueue(null, null);
			}
		}.scheduleRepeating(5000);
		
		iQueue.addMouseClickListener(new MouseClickListener<ScriptInterface.QueueItemInterface>() {
			@Override
			public void onMouseClick(TableEvent<QueueItemInterface> event) {
				if (iLastSelectedRow >= 1)
					iQueue.setSelected(iLastSelectedRow, false);
				if (event.getData() != null && iLastSelectedRow != event.getRow()) {
					iQueue.setSelected(event.getRow(), true);
					showLog(event.getData());
					iLastSelectedRow = event.getRow();
				} else {
					showLog(null);
					iLastSelectedRow = -1;
				}
			}
		});
	}
	
	private ScriptInterface getScript() {
		if (iScripts != null) {
			for (ScriptInterface s: iScripts)
				if (s.getId().toString().equals(iName.getValue(iName.getSelectedIndex()))) {
					return s;
				}
		}
		return null;
	}
	
	private void populate(GwtRpcResponseList<QueueItemInterface> queue, Long selectId) {
		if (iQueue.getSelectedRow() > 0 && selectId == null) {
			QueueItemInterface q = iQueue.getData(iQueue.getSelectedRow());
			if (q != null) selectId = q.getId();
		}
		QueueItemInterface selectedQueue = null;
		iQueue.clearTable(1);
		iLastSelectedRow = -1;
		
		for (final QueueItemInterface q: queue) {
			List<Widget> line = new ArrayList<Widget>();
			line.add(new Label(q.getName(), false));
			line.add(new Label(q.getStatus(), false));
			line.add(new Label(q.getProgress(), false));
			line.add(new Label(q.getOwner(), false));
			line.add(new Label(q.getSession(), false));
			line.add(new Label(q.getCreated() == null ? "" : sTS.format(q.getCreated()), false));
			line.add(new Label(q.getStarted() == null ? "" : sTS.format(q.getStarted()), false));
			line.add(new Label(q.getFinished() == null ? "" : sTS.format(q.getFinished()), false));
			if (q.getOtuput() != null) {
				line.add(new Anchor(q.getOtuput().substring(1 + q.getOtuput().lastIndexOf('.')), "temp/" + q.getOtuput()));
			} else {
				line.add(new Label(""));
			}
			if (q.isCanDelete()) {
				Image delete = new Image(RESOURCES.delete());
				delete.setTitle(MESSAGES.titleDeleteRow());
				delete.getElement().getStyle().setCursor(Cursor.POINTER);
				delete.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						refreshQueue(q.getId(), null);
					}
				});
				line.add(delete);
			} else {
				line.add(new Label(""));
			}
			
			iQueue.addRow(q, line);
			
			if (selectId != null && selectId.equals(q.getId())) {
				iQueue.setSelected(iQueue.getRowCount() - 1, true);
				iLastSelectedRow = iQueue.getRowCount() - 1;
				selectedQueue = q;
			}
		}
		
		iForm.getRowFormatter().setVisible(iQueueRow, iQueue.getRowCount() > 1);
		iForm.getRowFormatter().setVisible(iQueueRow + 1, iQueue.getRowCount() > 1);
		showLog(selectedQueue);
	}
	
	private void refreshQueue(Long deleteId, final Long selectId) {
		RPC.execute(new ScriptInterface.GetQueueTableRpcRequest(deleteId), new AsyncCallback<GwtRpcResponseList<QueueItemInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<QueueItemInterface> result) {
				populate(result, selectId);
			}
		});
	}
	
	private void showLog(QueueItemInterface item) {
		if (item == null || item.getLog() == null || item.getLog().isEmpty()) {
			iForm.getRowFormatter().setVisible(iLogRow, false);
			iForm.getRowFormatter().setVisible(iLogRow + 1, false);
		} else {
			iLogHeader.setHeaderTitle(MESSAGES.sectScriptLog(item.getName()));
			iForm.getRowFormatter().setVisible(iLogRow, true);
			iForm.getRowFormatter().setVisible(iLogRow + 1, true);
			iLog.setHTML(item.getLog());
		}
	}
	
	private void scriptChanged() {
		ScriptInterface script = getScript();
		if (script == null) {
			iForm.getRowFormatter().setVisible(iDescriptionRow, false);
			while (iForm.getRowCount() > iDescriptionRow + 2)
				iForm.removeRow(1 + iDescriptionRow);
			iHeader.setEnabled("edit", false);
			iHeader.setEnabled("execute", false);
			iParams.clear();
		} else {
			iDescription.setHTML(script.getDescription());
			iForm.getRowFormatter().setVisible(iDescriptionRow, true);
			iHeader.setEnabled("edit", script.canEdit());
			iHeader.setEnabled("execute", script.canExecute());
			iParams.clear();
			while (iForm.getRowCount() > iDescriptionRow + 2)
				iForm.removeRow(1 + iDescriptionRow);
			if (script.hasParameters()) {
				for (final ScriptParameterInterface param: script.getParameters()) {
					Widget widget = null;
					if (param.hasOptions()) {
						final ListBox list = new ListBox(param.isMultiSelect());
						if (!param.isMultiSelect()) list.addItem(MESSAGES.itemSelect());
						for (ScriptInterface.ListItem item: param.getOptions()) {
							list.addItem(item.getText(), item.getValue());
							if (param.getDefaultValue() != null && param.getDefaultValue().equalsIgnoreCase(item.getValue()))
								list.setSelectedIndex(list.getItemCount() - 1);
						}
						list.addChangeHandler(new ChangeHandler() {
							@Override
							public void onChange(ChangeEvent event) {
								if (param.isMultiSelect()) {
									String value = "";
									for (int i = 0; i < list.getItemCount(); i++)
										if (list.isItemSelected(i))
											value += (value.isEmpty() ? "" : ",") + list.getValue(i);
									iParams.put(param.getName(), value);
								} else {
									if (list.getSelectedIndex() <= 0)
										iParams.remove(param.getName());
									else
										iParams.put(param.getName(), list.getValue(list.getSelectedIndex()));
								}
							}
						});
						widget = list;
					} else if ("boolean".equalsIgnoreCase(param.getType())) {
						CheckBox ch = new CheckBox();
						ch.setValue("true".equalsIgnoreCase(param.getDefaultValue()));
						ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								if (event.getValue() == null)
									iParams.remove(param.getName());
								else
									iParams.put(param.getName(), event.getValue() ? "true" : "false");
							}
						});
						widget = ch;
					} else if ("file".equalsIgnoreCase(param.getType())) {
						UniTimeFileUpload upload = new UniTimeFileUpload(); upload.reset();
						widget = upload;
					} else {
						TextBox text = new TextBox();
						text.setStyleName("unitime-TextBox");
						text.setWidth("400px");
						if (param.getDefaultValue() != null)
						text.setText(param.getDefaultValue());
						text.addValueChangeHandler(new ValueChangeHandler<String>() {
							@Override
							public void onValueChange(ValueChangeEvent<String> event) {
								if (event.getValue() == null)
									iParams.remove(param.getName());
								else
									iParams.put(param.getName(), event.getValue());
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
	}
	
	public void reload(final Long scriptId) {
		RPC.execute(new GetScriptOptionsRpcRequest(), new AsyncCallback<ScriptOptionsInterface>() {

			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(ScriptOptionsInterface result) {
				iHeader.setEnabled("add", result.canAdd());
				iDialog.setup(result);
				RPC.execute(new LoadAllScriptsRpcRequest(), new AsyncCallback<GwtRpcResponseList<ScriptInterface>>() {

					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
					}

					@Override
					public void onSuccess(GwtRpcResponseList<ScriptInterface> result) {
						iScripts = result;
						iName.clear();
						iName.addItem(MESSAGES.itemSelect(), "-1");
						iName.setSelectedIndex(0);
						for (ScriptInterface script: result) {
							if (!script.canExecute() && !script.canEdit() && !script.canDelete()) continue;
							iName.addItem(script.getName(), script.getId().toString());
							if (scriptId != null && scriptId.equals(script.getId()))
								iName.setSelectedIndex(iName.getItemCount() - 1);
						}
						scriptChanged();
					}
				});
			}
		});
	}

	private class SaveOrUpdateDialog extends UniTimeDialogBox {
		private SimpleForm iDialogForm;
		private TextBox iName;
		private TextArea iDescription;
		private ListBox iPermission;
		private ListBox iEngine;
		private TextArea iScript;
		private UniTimeHeaderPanel iBottom;
		private Long iScriptId = null;
		private UniTimeTable<ScriptParameterInterface> iParams;
		
		SaveOrUpdateDialog() {
			super(false, true);
			
			iDialogForm = new SimpleForm(2);
			
			iName = new TextBox();
			iName.setStyleName("unitime-TextBox");
			iName.setWidth("400px");
			iName.setMaxLength(128);
			iName.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					iBottom.clearMessage();
				}
			});
			iDialogForm.addRow(MESSAGES.propName(), iName);
			
			iDescription = new TextArea();
			iDescription.setStyleName("unitime-TextArea");
			iDescription.setVisibleLines(3);
			iDescription.setCharacterWidth(80);
			iDescription.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					iBottom.clearMessage();
				}
			});
			iDialogForm.addRow(MESSAGES.propDescription(), iDescription);
			
			iEngine = new ListBox(false);
			iEngine.addItem(MESSAGES.itemSelect());
			iEngine.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					iBottom.clearMessage();
				}
			});
			iDialogForm.addRow(MESSAGES.propEngine(), iEngine);
			
			iPermission = new ListBox(false);
			iPermission.addItem(MESSAGES.itemNone());
			iPermission.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					iBottom.clearMessage();
				}
			});
			iDialogForm.addRow(MESSAGES.propPermission(), iPermission);
			
			iScript = new TextArea();
			iScript.setStyleName("unitime-TextArea");
			iScript.setVisibleLines(20);
			iScript.setCharacterWidth(80);
			iScript.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					iBottom.clearMessage();
				}
			});
			iDialogForm.addRow(MESSAGES.propScript(), iScript);
			
			iParams = new UniTimeTable<ScriptParameterInterface>();
			iDialogForm.addRow(MESSAGES.propParameters(), iParams);
			
			List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
			header.add(new UniTimeTableHeader(MESSAGES.colName()));
			header.add(new UniTimeTableHeader(MESSAGES.colLabel()));
			header.add(new UniTimeTableHeader(MESSAGES.colType()));
			header.add(new UniTimeTableHeader(MESSAGES.colDefaultValue()));
			header.add(new UniTimeTableHeader(""));
			iParams.addRow(null, header);
			
			ClickHandler save = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (!validate()) return;
					ScriptInterface script = new ScriptInterface();
					script.setId(iScriptId);
					script.setName(iName.getText());
					script.setScript(iScript.getText());
					script.setDescription(iDescription.getText());
					script.setEngine(iEngine.getSelectedIndex() <= 0 ? null : iEngine.getValue(iEngine.getSelectedIndex()));
					script.setPermission(iPermission.getSelectedIndex() <= 0 ? null : iPermission.getValue(iPermission.getSelectedIndex()));
					for (int i = 1; i < iParams.getRowCount(); i++) {
						ScriptParameterInterface p = iParams.getData(i);
						if (p != null && p.getName() != null && !p.getName().isEmpty())
							script.addParameter(p);
					}
					hide();
					LoadingWidget.getInstance().show(MESSAGES.waitSavingData());
					RPC.execute(new SaveOrUpdateScriptRpcRequest(script), new AsyncCallback<ScriptInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iHeader.setErrorMessage(MESSAGES.failedSave(caught.getMessage()));
							UniTimeNotifications.error(MESSAGES.failedSave(caught.getMessage()), caught);
						}
						@Override
						public void onSuccess(ScriptInterface result) {
							LoadingWidget.getInstance().hide();
							reload(result.getId());
						}
					});
				}
			};
			
			iBottom = new UniTimeHeaderPanel();
			iBottom.addButton("save", MESSAGES.opScriptSave(), save);
			iBottom.addButton("update", MESSAGES.opScriptUpdate(), save);
			iBottom.addButton("export", MESSAGES.opScriptExport(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					RPC.execute(EncodeQueryRpcRequest.encode("output=script.xml&script=" + iScriptId), new AsyncCallback<EncodeQueryRpcResponse>() {
						@Override
						public void onFailure(Throwable caught) {
						}
						@Override
						public void onSuccess(EncodeQueryRpcResponse result) {
							ToolBox.open(GWT.getHostPageBaseURL() + "export?q=" + result.getQuery());
						}
					});
				}
			});
			iBottom.addButton("delete", MESSAGES.opScriptDelete(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (iScriptId != null) {
						hide();
						LoadingWidget.getInstance().show(MESSAGES.waitDelete(iName.getText()));
						RPC.execute(new DeleteScriptRpcRequest(iScriptId, iName.getText()), new AsyncCallback<ScriptInterface>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								iHeader.setErrorMessage(MESSAGES.failedDelete(iName.getText(), caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedDelete(iName.getText(), caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(ScriptInterface result) {
								LoadingWidget.getInstance().hide();
								reload(null);
							}
						});
					}
				}
			});
			iBottom.addButton("back", MESSAGES.opScriptBack(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
				}
			});
			iDialogForm.addBottomRow(iBottom);
			
			setWidget(iDialogForm);
		}
		
		private void addParam(final ScriptParameterInterface param) {
			List<Widget> line = new ArrayList<Widget>();
			
			final TextBox name = new TextBox();
			name.setStyleName("unitime-TextBox");
			name.setMaxLength(128);
			name.setWidth("125px");
			if (param.getName() != null) name.setText(param.getName());
			name.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					param.setName(event.getValue());
					if (!event.getValue().isEmpty()) {
						if (iParams.getWidget(iParams.getRowCount() - 1, 0).equals(name))
							addParam(new ScriptParameterInterface());
					}
					iBottom.clearMessage();
				}
			});
			line.add(name);
			
			TextBox label = new TextBox();
			label.setStyleName("unitime-TextBox");
			label.setMaxLength(256);
			label.setWidth("125px");
			if (param.getLabel() != null) label.setText(param.getLabel());
			label.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					param.setLabel(event.getValue());
					iBottom.clearMessage();
				}
			});
			line.add(label);
			
			TextBox type = new TextBox();
			type.setStyleName("unitime-TextBox");
			type.setMaxLength(2048);
			type.setWidth("125px");
			if (param.getType() != null) type.setText(param.getType());
			type.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					param.setType(event.getValue());
					iBottom.clearMessage();
				}
			});
			line.add(type);
			
			TextBox defaultValue = new TextBox();
			defaultValue.setStyleName("unitime-TextBox");
			defaultValue.setMaxLength(2048);
			defaultValue.setWidth("125px");
			if (param.getDefaultValue() != null) defaultValue.setText(param.getDefaultValue());
			defaultValue.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					param.setDefaultValue(event.getValue());
					iBottom.clearMessage();
				}
			});
			line.add(defaultValue);
			
			Image delete = new Image(RESOURCES.delete());
			delete.setTitle(MESSAGES.titleDeleteRow());
			delete.getElement().getStyle().setCursor(Cursor.POINTER);
			delete.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (iParams.getRowCount() > 2)
						iParams.removeRow(iParams.getCellForEvent(event).getRowIndex());
				}
			});
			line.add(delete);
			
			iParams.addRow(param, line);
		}
		
		private boolean validate() {
			if (iName.getText().isEmpty()) {
				iBottom.setErrorMessage(MESSAGES.errorNameIsRequired());
				return false;
			}
			for (ScriptInterface script: iScripts) {
				if (iScriptId != null && iScriptId.equals(script.getId())) continue;
				if (iName.getText().equals(script.getName())) {
					iBottom.setErrorMessage(MESSAGES.errorNameNotUnique());
					return false;
				}
			}
			if (iEngine.getSelectedIndex() <= 0) {
				iBottom.setErrorMessage(MESSAGES.errorEngineIsRequired());
				return false;
			}
			if (iScript.getText().isEmpty()) {
				iBottom.setErrorMessage(MESSAGES.errorScriptIsRequired());
				return false;
			}
			Set<String> names = new HashSet<String>();
			for (int i = 1; i < iParams.getRowCount(); i++) {
				ScriptParameterInterface p = iParams.getData(i);
				if (p != null && p.getName() != null && !p.getName().isEmpty()) {
					if (!names.add(p.getName())) {
						iBottom.setErrorMessage(MESSAGES.errorParameterNameNotUnique(p.getName()));
						return false;
					}
					if (p.getType() == null || p.getType().isEmpty()) {
						iBottom.setErrorMessage(MESSAGES.errorParameterTypeRequired(p.getName()));
						return false;
					}
				}
			}
			return true;
		}
		
		public void addScript() {
			iScriptId = null;
			setText(MESSAGES.dialogAddScript());
			iName.setText("");
			iDescription.setText("");
			iEngine.setSelectedIndex(0);
			iPermission.setSelectedIndex(0);
			iScript.setText("");
			iParams.clearTable(1);
			addParam(new ScriptParameterInterface());
			center();
			iBottom.setEnabled("save", true);
			iBottom.setEnabled("update", false);
			iBottom.setEnabled("delete", false);
			iBottom.setEnabled("export", false);
		}
		
		public void editScript(ScriptInterface script) {
			iScriptId = script.getId();
			setText(MESSAGES.dialogEditScript());
			iName.setText(script.getName());
			iDescription.setText(script.getDescription());
			iPermission.setSelectedIndex(0);
			if (script.getPermission() != null)
				for (int i = 0; i < iPermission.getItemCount(); i++)
					if (script.getPermission().equals(iPermission.getValue(i))) {
						iPermission.setSelectedIndex(i); break;
					}
			iEngine.setSelectedIndex(0);
			if (script.getEngine() != null)
				for (int i = 0; i < iEngine.getItemCount(); i++)
					if (script.getEngine().equals(iEngine.getValue(i))) {
						iEngine.setSelectedIndex(i); break;
					}
			iScript.setText(script.getScript());
			iParams.clearTable(1);
			if (script.hasParameters())
				for (ScriptParameterInterface param: script.getParameters()) {
					ScriptParameterInterface p = new ScriptParameterInterface();
					p.setDefaultValue(param.getDefaultValue());
					p.setLabel(param.getLabel());
					p.setType(param.getType());
					p.setName(param.getName());
					addParam(p);
				}
			addParam(new ScriptParameterInterface());
			center();
			iBottom.setEnabled("save", false);
			iBottom.setEnabled("update", script.canEdit());
			iBottom.setEnabled("delete", script.canDelete());
			iBottom.setEnabled("export", true);
		}
		
		public void setup(ScriptOptionsInterface options) {
			iPermission.clear();
			iPermission.addItem(MESSAGES.itemNone(), "");
			for (String permission: options.getPermissions())
				iPermission.addItem(permission);
			iEngine.clear();
			iEngine.addItem(MESSAGES.itemSelect(), "");
			for (String engine: options.getEngines())
				iEngine.addItem(engine);
		}
		
	}
}
