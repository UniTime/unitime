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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.IdLabel;
import org.unitime.timetable.gwt.client.admin.ApplicationConfigPage.ApplicationSettingEditRequest.Operation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class ApplicationConfigPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iListHeader, iListFooter;
	private TableWidget iTable;
	private UniTimeHeaderPanel iHeader, iFooter;
	private String iSettingId;
	private ApplicationSettingInterface iSetting;
	private CheckBox iShowAll;
	private boolean iShowAllChanged = false;
	
	public ApplicationConfigPage() {
		iPanel = new SimpleForm();
		initWidget(iPanel);
		iPanel.addStyleName("unitime-ApplicationConfigPage");
		iListHeader = new UniTimeHeaderPanel();
		iListHeader.addButton("add", COURSE.actionAddSetting(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("add", false);
				editSetting(null);
			}
		});
		iListHeader.setEnabled("add", false);
		iListHeader.getButton("add").setAccessKey(COURSE.accessAddSetting().charAt(0));
		iListHeader.getButton("add").setTitle(COURSE.titleAddSetting(COURSE.accessAddSetting()));
		
		iListHeader.addButton("pdf", COURSE.actionExportPdf(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				exportData("pdf");
			}
		});
		iListHeader.getButton("pdf").setAccessKey(COURSE.accessExportPdf().charAt(0));
		iListHeader.getButton("pdf").setTitle(COURSE.titleExportPdf(COURSE.accessExportPdf()));
		iListHeader.setEnabled("pdf", false);
		iListHeader.addButton("csv", COURSE.actionExportCsv(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				exportData("csv");
			}
		});
		iListHeader.getButton("csv").setAccessKey(COURSE.accessExportCsv().charAt(0));
		iListHeader.getButton("csv").setTitle(COURSE.titleExportCsv(COURSE.accessExportCsv()));
		iListHeader.setEnabled("csv", false);
		
		
		iListFooter = iListHeader.clonePanel();
		iTable = new TableWidget();
		iTable.addStyleName("table");
		
		iHeader = new UniTimeHeaderPanel("");
		iHeader.addButton("save", COURSE.actionSaveSetting(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateSetting(false);
			}
		});
		iHeader.setEnabled("save", false);
		iHeader.getButton("save").setAccessKey(COURSE.accessSaveSetting().charAt(0));
		iHeader.getButton("save").setTitle(COURSE.titleSaveSetting(COURSE.accessSaveSetting()));

		iHeader.addButton("update", COURSE.actionUpdateSetting(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateSetting(true);
			}
		});
		iHeader.setEnabled("update", false);
		iHeader.getButton("update").setAccessKey(COURSE.accessUpdateSetting().charAt(0));
		iHeader.getButton("update").setTitle(COURSE.titleUpdateSetting(COURSE.accessUpdateSetting()));
		
		iHeader.addButton("delete", COURSE.actionDeleteSetting(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(COURSE.confirmDeleteAppConfig(), new Command() {
					@Override
					public void execute() {
						deleteSetting();
					}
				});
			}
		});
		iHeader.setEnabled("delete", false);
		iHeader.getButton("delete").setAccessKey(COURSE.accessDeleteSetting().charAt(0));
		iHeader.getButton("delete").setTitle(COURSE.titleDeleteSetting(COURSE.accessDeleteSetting()));

		iHeader.addButton("back", COURSE.actionCancelSetting(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(null, false);
				showSettings(iSettingId);
			}
		});
		iHeader.setEnabled("back", false);
		iHeader.getButton("back").setAccessKey(COURSE.accessCancelSetting().charAt(0));
		iHeader.getButton("back").setTitle(COURSE.titleCancelSetting(COURSE.accessCancelSetting()));
		
		iShowAll = new CheckBox(COURSE.checkShowAllAppSettings());
		iShowAll.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iShowAllChanged = true;
				showSettings(null);
			}
		});
		iListFooter.insertLeft(iShowAll, true);
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				tokenChanged(event.getValue());
			}
		});
		tokenChanged(History.getToken());
	}
	
	protected void tokenChanged(String token) {
		if (token == null || token.isEmpty())
			showSettings(null);
		else if ("add".equals(token))
			editSetting(null);
		else
			editSetting(token);
	}
	
	protected void showSettings(final String settingId) {
		UniTimePageLabel.getInstance().setPageName(MSG.pageApplicationConfiguration());
		iPanel.clear();
		iListHeader.setEnabled("add", false);
		iListHeader.setEnabled("csv", false);
		iListHeader.setEnabled("pdf", false);
		iPanel.addHeaderRow(iListHeader);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		ApplicationConfigRequest request = new ApplicationConfigRequest();
		if (iShowAllChanged)
			request.setShowAllProperties(iShowAll.getValue());
		request.setHash(settingId);
		RPC.execute(request, new AsyncCallback<ApplicationConfigResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ApplicationConfigResponse result) {
				LoadingWidget.getInstance().hide();
				iTable.setData(result.getTable());
				iPanel.addRow(iTable);
				iPanel.addBottomRow(iListFooter);
				iListHeader.setHeaderTitle(result.getTable().getName());
				iListHeader.setEnabled("add", result.isCanAdd());
				if (settingId != null)
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						LineInterface line = iTable.getData(row);
						if (line != null && ("#" + settingId).equals(line.getURL())) {
							Element el = iTable.getRowFormatter().getElement(row);
							ToolBox.scrollToElement(el);
							ToolBox.focusOnRow(el);
						}
					}
				iListHeader.setEnabled("csv", result.getTable().hasLines());
				iListHeader.setEnabled("pdf", result.getTable().hasLines());
			}
		});		
	}
	
	private TextBox iKey;
	private CheckBox iAllSessions;
	private List<CheckBox> iSessions;
	private int iSessionsRow;
	private TextArea iDescription;
	
	protected void editSetting(String setting) {
		iSettingId = setting;
		Window.scrollTo(0, 0);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new ApplicationSettingEditRequest(iSettingId == null ? Operation.ADD : Operation.EDIT, iSettingId), new AsyncCallback<ApplicationSettingEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToLoadData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ApplicationSettingEditResponse result) {
				LoadingWidget.getInstance().hide();
				UniTimePageLabel.getInstance().setPageName(result.getKey() == null || !result.isCanDelete() ? MSG.pageAddApplicationSetting() : MSG.pageEditApplicationSetting());
				iSettingId = result.getKey();
				iSetting = result.getSetting();
				if (iSetting == null)
					iSetting = new ApplicationSettingInterface();
				iHeader.setEnabled("save", iSettingId == null || !result.isCanDelete());
				iHeader.setEnabled("update", iSettingId != null && result.isCanDelete());
				iHeader.setEnabled("delete", result.isCanDelete() && iSettingId != null);
				iHeader.setEnabled("back", true);
				
				iHeader.setHeaderTitle(iSettingId == null || !result.isCanDelete() ? COURSE.sectAddAppSetting() : COURSE.sectEditAppSetting());
				iPanel.clear();
				iHeader.clearMessage();
				iPanel.addHeaderRow(iHeader);
				
				if (iSettingId == null || !result.isCanDelete()) {
					iKey = new TextBox();
					iKey.setWidth("1000px"); iKey.setMaxLength(1000);
					if (iSetting.hasKey())
						iKey.setValue(iSetting.getKey());
					iPanel.addRow(COURSE.propAppConfigKey(), iKey);
					iKey.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							iSetting.setKey(event.getValue());
						}
					});
				} else {
					iKey = null;
					iPanel.addRow(COURSE.propAppConfigKey(), new Label(iSetting.getKey()));
				}
				
				iAllSessions = new CheckBox(COURSE.checkAppConfigAppliesToAllSessions());
				iAllSessions.setValue(iSetting.isAllSessions());
				iAllSessions.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						iSetting.setAllSessions(event.getValue());
						iPanel.getRowFormatter().setVisible(iSessionsRow, !iSetting.isAllSessions());
					}
				});
				iPanel.addRow(COURSE.propAppConfigAppliesTo(), iAllSessions);
				iSessions = new ArrayList<CheckBox>();
				P panel = new P("sessions-selection");
				for (final IdLabel session: result.getSessions()) {
					CheckBox ch = new CheckBox(session.getLabel());
					if (session.getColor() != null) ch.getElement().getStyle().setColor(session.getColor());
					ch.setValue(iSetting.hasSessionId(session.getId()));
					ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							if (event.getValue())
								iSetting.addSessionId(session.getId());
							else
								iSetting.removeSessionId(session.getId());
						}
					});
					iSessions.add(ch);
					panel.add(ch);
				}
				iSessionsRow = iPanel.addRow("", panel);
				iPanel.getRowFormatter().setVisible(iSessionsRow, !iSetting.isAllSessions());
				
				if (iSetting.hasType())
					iPanel.addRow(COURSE.propAppConfigType(), new Label(iSetting.getType()));
				if (iSetting.hasDefaultValue())
					iPanel.addRow(COURSE.propAppConfigDefault(), new Label(iSetting.getDefaultValue()));
				if (iSetting.hasPossibleValues()) {
					final ListBox value = new ListBox();
					if (!iSetting.hasDefaultValue())
						value.addItem(COURSE.itemSelect(), "");
					for (String item: iSetting.getPossibleValues()) {
						value.addItem(item);
						if (item.equalsIgnoreCase(iSetting.getValue()))
							value.setSelectedIndex(value.getItemCount() - 1);
					}
					value.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							iSetting.setValue(value.getSelectedValue());
						}
					});
					iPanel.addRow(COURSE.propAppConfigValue(), value);
				} else if ("Boolean".equals(iSetting.getType())) {
					CheckBox value = new CheckBox();
					value.setValue("true".equalsIgnoreCase(iSetting.getValue()));
					value.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							iSetting.setValue(event.getValue() ? "true" : "false");
						}
					});
					iPanel.addRow(COURSE.propAppConfigValue(), value);
				} else if ("Integer".equals(iSetting.getType()) || "Long".equals(iSetting.getType())) {
					final NumberBox value = new NumberBox();
					value.setDecimal(false); value.setNegative(true);
					if (iSetting.hasValue())
						value.setText(iSetting.getValue());
					value.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							Long val = value.toLong();
							iSetting.setValue(val == null ? null : val.toString());
						}
					});
					iPanel.addRow(COURSE.propAppConfigValue(), value);
				} else if ("Double".equals(iSetting.getType()) || "Float".equals(iSetting.getType()) || "Number".equals(iSetting.getType())) {
					final NumberBox value = new NumberBox();
					value.setDecimal(true); value.setNegative(true);
					if (iSetting.hasValue())
						value.setText(iSetting.getValue());
					value.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							Double val = value.toDouble();
							iSetting.setValue(val == null ? null : val.toString());
						}
					});
					iPanel.addRow(COURSE.propAppConfigValue(), value);
				} else {
					final TextArea value = new TextArea();
					if (iSetting.hasValue())
						value.setText(iSetting.getValue());
					value.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							iSetting.setValue(event.getValue());
						}
					});
					value.setStyleName("unitime-TextArea");
					value.setVisibleLines(10);
					value.setWidth("1000px");
					iPanel.addRow(COURSE.propAppConfigValue(), value);
				}
				
				iDescription = new TextArea();
				if (iSetting.hasDescription())
					iDescription.setText(iSetting.getDescription());
				iDescription.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iSetting.setDescription(event.getValue());
					}
				});
				iDescription.setStyleName("unitime-TextArea");
				iDescription.setVisibleLines(5);
				iDescription.setWidth("1000px");
				iPanel.addRow(COURSE.propAppConfigDescription(), iDescription);
				
				iPanel.addBottomRow(iFooter);
			}
		});
	}
	
	protected void saveOrUpdateSetting(boolean update) {
		if (validateSetting()) {
			RPC.execute(new ApplicationSettingEditRequest(update ? Operation.UPDATE : Operation.SAVE, iSetting), new AsyncCallback<ApplicationSettingEditResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(ApplicationSettingEditResponse result) {
					History.newItem(null, false);
					showSettings(iSetting.getKey());
				}
			});
		}
	}
	
	protected boolean validateSetting() {
		List<String> errors = new ArrayList<String>();
		if (!iSetting.hasKey())
			errors.add(COURSE.errorRequiredField(COURSE.columnAppConfigKey()));
		if (iSetting.hasReference() && iSetting.hasKey() && iSetting.getKey().indexOf('%') >= 0)
			errors.add(COURSE.errorSettingMissingReference(iSetting.getReference()));
		if (!iSetting.isAllSessions() && !iSetting.hasSessionIds())
			errors.add(COURSE.errorRequiredField(COURSE.columnAcademicSession()));
		if (iSetting.hasDefaultValue() && iSetting.hasPossibleValues() && !iSetting.hasValue())
			errors.add(COURSE.errorRequiredField(COURSE.columnAppConfigValue()));
		
		if (errors.isEmpty())
			iHeader.clearMessage();
		else {
			String message = "";
			for (String e: errors)
				message += (message.isEmpty() ? "" : "\n") + e;
			iHeader.setErrorMessage(message);
			UniTimeNotifications.error(message);
		}

		return errors.isEmpty();
	}
	
	protected void deleteSetting() {
		RPC.execute(new ApplicationSettingEditRequest(Operation.DELETE, iSettingId), new AsyncCallback<ApplicationSettingEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MSG.failedToDeleteData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToDeleteData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);					
			}

			@Override
			public void onSuccess(ApplicationSettingEditResponse result) {
				History.newItem(null, false);
				showSettings(iSettingId);
			}
		});
	}
	
	protected void exportData(String format) {
		String query = "output=application-config." + format + "&sort=" + iTable.getSortCookie();
		RPC.execute(EncodeQueryRpcRequest.encode(query), new AsyncCallback<EncodeQueryRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(EncodeQueryRpcResponse result) {
				ToolBox.open(GWT.getHostPageBaseURL() + result.getExportUrl());
			}
		});
	}
	
	public static class ApplicationConfigRequest implements GwtRpcRequest<ApplicationConfigResponse> {
		private Boolean iShowAllProperties;
		private String iHash;
		private boolean iExport = false;
		
		public boolean hasShowAllProperties() { return iShowAllProperties != null; }
		public boolean isShowAllProperties() { return Boolean.TRUE.equals(iShowAllProperties); }
		public void setShowAllProperties(Boolean showAllProperties) { iShowAllProperties = showAllProperties; }
		
		public boolean isExport() { return iExport; }
		public void setExport(boolean export) { iExport = export; }
		
		public String getHash() { return iHash; }
		public void setHash(String hash) { iHash = hash; }
		public boolean hasHash() { return iHash != null && !iHash.isEmpty(); }
	}
	
	public static class ApplicationConfigResponse implements GwtRpcResponse {
		private TableInterface iTable;
		private boolean iShowAllProperties;
		private boolean iCanAdd = false;
		
		public TableInterface getTable() { return iTable; }
		public void setTable(TableInterface table) { iTable = table; }
		public boolean isShowAllProperties() { return iShowAllProperties; }
		public void setShowAllProperties(boolean showAllProperties) { iShowAllProperties = showAllProperties; }
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
	}
	
	public static class ApplicationSettingEditRequest implements GwtRpcRequest<ApplicationSettingEditResponse> {
		private String iKey;
		private ApplicationSettingInterface iSetting;
		private Operation iOperation;
		
		public ApplicationSettingEditRequest() {}
		public ApplicationSettingEditRequest(Operation operation) {
			iOperation = operation;
		}
		public ApplicationSettingEditRequest(Operation operation, String key) {
			iOperation = operation; iKey = key;
		}
		public ApplicationSettingEditRequest(Operation operation, ApplicationSettingInterface setting) {
			iOperation = operation;
			iSetting = setting;
			iKey = (setting == null ? null : setting.getKey());
		}
		
		public String getKey() { return iKey; }
		public void setKey(String key) { iKey = key; }
		public boolean hasKey() { return iKey != null && !iKey.isEmpty(); }

		public ApplicationSettingInterface getSetting() { return iSetting; }
		public void setSetting(ApplicationSettingInterface setting) { iSetting = setting; }
		
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public static enum Operation {
			ADD, EDIT, SAVE, UPDATE, DELETE,
		}
	}
	
	public static class ApplicationSettingInterface implements IsSerializable {
		private String iKey;
		private String iValue;
		private List<String> iPossibleValues = null;
		private String iDefaultValue;
		private String iDescription;
		private String iType;
		private String iReference;
		private Set<Long> iSessionIds;
		private boolean iAllSessions = true;
		
		public String getKey() { return iKey; }
		public void setKey(String key) { iKey = key; }
		public boolean hasKey() { return iKey != null && !iKey.isEmpty(); }
		
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }
		public boolean hasValue() { return iValue != null && !iValue.isEmpty(); }
		
		public void addPossibleValue(String value) {
			if (iPossibleValues == null) iPossibleValues = new ArrayList<String>();
			iPossibleValues.add(value);
		}
		public List<String> getPossibleValues() { return iPossibleValues; }
		public boolean hasPossibleValues() { return iPossibleValues != null && !iPossibleValues.isEmpty(); }
		public boolean hasPossibleValue(String value) {
			if (iPossibleValues == null || value == null) return false;
			for (String item: iPossibleValues)
				if (value.equals(item)) return true;
			return false;
		}
		
		public String getDefaultValue() { return iDefaultValue; }
		public void setDefaultValue(String defaultValue) { iDefaultValue = defaultValue; }
		public boolean hasDefaultValue() { return iDefaultValue != null && !iDefaultValue.isEmpty(); }

		public String getDescription() { return iDescription; }
		public void setDescription(String description) { iDescription = description; }
		public boolean hasDescription() { return iDescription != null && !iDescription.isEmpty(); }

		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		public boolean hasType() { return iType != null && !iType.isEmpty(); }

		public String getReference() { return iReference; }
		public void setReference(String reference) { iReference = reference; }
		public boolean hasReference() { return iReference != null && !iReference.isEmpty(); }

		public boolean hasSessionIds() { return iSessionIds != null && !iSessionIds.isEmpty(); }
		public void addSessionId(Long id) {
			if (iSessionIds == null) iSessionIds = new HashSet<Long>();
			iSessionIds.add(id);
		}
		public void removeSessionId(Long id) {
			if (iSessionIds != null && id != null) iSessionIds.remove(id);
		}
		public Set<Long> getSessionIds() { return iSessionIds; }
		public boolean hasSessionId(Long id) {
			if (iSessionIds == null) return false;
			return iSessionIds.contains(id);
		}
		public void setSessionIds(Collection<Long> sessionIds) {
			if (sessionIds == null || sessionIds.isEmpty())
				iSessionIds = null;
			else
				iSessionIds = new HashSet<Long>(sessionIds);
		}
		
		public boolean isAllSessions() { return iAllSessions; }
		public void setAllSessions(boolean allSessions) { iAllSessions = allSessions; }
	}
	
	public static class ApplicationSettingEditResponse implements GwtRpcResponse {
		private ApplicationSettingInterface iSetting;
		private Long iCurrentSessionId;
		private List<IdLabel> iSessions;
		private boolean iCanDelete = false;
	
		public ApplicationSettingInterface getSetting() { return iSetting; }
		public void setSetting(ApplicationSettingInterface setting) { iSetting = setting; }
		
		public String getKey() { return iSetting == null ? null : iSetting.getKey(); }
		
		public Long getCurrentSessionId() { return iCurrentSessionId; }
		public void setCurrentSessionId(Long sessionId) { iCurrentSessionId = sessionId; }

		public IdLabel addSession(Long id, String label) {
			if (iSessions == null) iSessions = new ArrayList<IdLabel>();
			IdLabel item = new IdLabel(id, label);
			iSessions.add(item);
			return item;
		}
		public List<IdLabel> getSessions() { return iSessions; }
		public boolean hasSessions() { return iSessions != null && !iSessions.isEmpty(); }
		public IdLabel getSession(Long id) {
			if (iSessions == null || id == null) return null;
			for (IdLabel item: iSessions)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
	}

}
