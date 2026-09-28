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
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.SolverConfigsPage.SolverConfigEditRequest.Operation;
import org.unitime.timetable.gwt.client.admin.SolverConfigsPage.SolverConfigInterface.Appearance;
import org.unitime.timetable.gwt.client.aria.AriaCheckBox;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget.InstructorAvailabilityModel;
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
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingModel;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SolverConfigsPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iListHeader, iListFooter;
	private TableWidget iTable;
	private UniTimeHeaderPanel iHeader, iFooter;
	private SolverConfigInterface iConfig;
	
	public SolverConfigsPage() {
		iPanel = new SimpleForm(3);
		initWidget(iPanel);
		iPanel.addStyleName("unitime-SolverConfigsPage");
		iListHeader = new UniTimeHeaderPanel();
		iListHeader.addButton("add", COURSE.actionAddNewSolverConfig(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("add", false);
				editSolverConfig(null);
			}
		});
		iListHeader.getButton("add").setAccessKey(COURSE.accessAddNewSolverConfig().charAt(0));
		iListHeader.getButton("add").setTitle(COURSE.titleAddNewSolverConfig(COURSE.accessAddNewSolverConfig()));
		iListHeader.setEnabled("add", false);

		iListFooter = iListHeader.clonePanel();		
		
		iTable = new TableWidget();
		iTable.addStyleName("table");
		
		iHeader = new UniTimeHeaderPanel("");
		iHeader.addButton("save", COURSE.actionSaveSolverConfig(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateSolverConfig();
			}
		});
		iHeader.getButton("save").setAccessKey(COURSE.accessSaveSolverConfig().charAt(0));
		iHeader.getButton("save").setTitle(COURSE.titleSaveSolverConfig(COURSE.accessSaveSolverConfig()));
		iHeader.setEnabled("save", false);
		iHeader.addButton("update", COURSE.actionUpdateSolverConfig(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateSolverConfig();
			}
		});
		iHeader.getButton("update").setAccessKey(COURSE.accessUpdateSolverConfig().charAt(0));
		iHeader.getButton("update").setTitle(COURSE.titleUpdateSolverConfig(COURSE.accessUpdateSolverConfig()));
		iHeader.setEnabled("update", false);
		
		iHeader.addButton("export", COURSE.actionExportSolverConfig(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				exportData(iConfig.getSolverConfigId());
			}
		});
		iHeader.getButton("export").setAccessKey(COURSE.accessExportSolverConfig().charAt(0));
		iHeader.getButton("export").setTitle(COURSE.titleExportSolverConfig(COURSE.accessExportSolverConfig()));
		iHeader.setEnabled("export", false);
		
		iHeader.addButton("delete", COURSE.actionDeleteSolverConfig(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteSolverConfig();
			}
		});
		iHeader.getButton("delete").setAccessKey(COURSE.accessDeleteSolverConfig().charAt(0));
		iHeader.getButton("delete").setTitle(COURSE.titleDeleteSolverConfig(COURSE.accessDeleteSolverConfig()));
		iHeader.setEnabled("delete", false);
		
		iHeader.addButton("back", COURSE.actionBackToSolverConfigs(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(null, false);
				showSolverConfigs(iConfig == null ? null : iConfig.getSolverConfigId());
			}
		});
		iHeader.getButton("back").setAccessKey(COURSE.accessBackToSolverConfigs().charAt(0));
		iHeader.getButton("back").setTitle(COURSE.titleBackToSolverConfigs(COURSE.accessBackToSolverConfigs()));
		iHeader.setEnabled("back", false);
		
		iFooter = iHeader.clonePanel("");
		
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
			showSolverConfigs();
		else if ("add".equals(token))
			editSolverConfig(null);
		else {
			try {
				editSolverConfig(Long.valueOf(token));
			} catch (NumberFormatException e) {
				showSolverConfigs();
			}
		}
	}
	
	protected void showSolverConfigs() {
		showSolverConfigs(null);
	}
	
	protected void deleteSolverConfig() {
		UniTimeConfirmationDialog.confirm(COURSE.confirmDeleteSolverConfig(), new Command() {
			@Override
			public void execute() {
				RPC.execute(new SolverConfigEditRequest(Operation.DELETE, iConfig.getSolverConfigId()), new AsyncCallback<SolverConfigEditResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MSG.failedToDeleteData(caught.getMessage()));
						UniTimeNotifications.error(MSG.failedToDeleteData(caught.getMessage()), caught);
						ToolBox.checkAccess(caught);					
					}

					@Override
					public void onSuccess(SolverConfigEditResponse result) {
						History.newItem(null, false);
						showSolverConfigs(null);
					}
				});
			}
		});
	}

	protected void showSolverConfigs(final Long configId) {
		UniTimePageLabel.getInstance().setPageName(MSG.pageSolverConfigurations());
		iPanel.clear();
		iListHeader.setEnabled("add", false);
		iPanel.addHeaderRow(iListHeader);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new SolverConfigsRequest(), new AsyncCallback<SolverConfigsResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(SolverConfigsResponse result) {
				LoadingWidget.getInstance().hide();
				iTable.setData(result.getSolverConfigsTable());
				iPanel.addRow(iTable);
				iPanel.addBottomRow(iListFooter);
				iListHeader.setHeaderTitle(result.getSolverConfigsTable().getName());
				iListHeader.setEnabled("add", true);
				if (configId != null)
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						LineInterface line = iTable.getData(row);
						if (line != null && configId.equals(line.getId())) {
							Element el = iTable.getRowFormatter().getElement(row);
							ToolBox.scrollToElement(el);
							ToolBox.focusOnRow(el);
						}
					}
				iListHeader.setEnabled("csv", true);
				iListHeader.setEnabled("pdf", true);
			}
		});		
	}
	
	private TextBox iName, iReference;
	private ListBox iAppearance;
	private int iAppearanceLine;
	
	protected void editSolverConfig(Long configId) {
		Window.scrollTo(0, 0);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new SolverConfigEditRequest(configId == null ? Operation.ADD : Operation.EDIT, configId), new AsyncCallback<SolverConfigEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToLoadData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);				
			}

			@Override
			public void onSuccess(SolverConfigEditResponse result) {
				LoadingWidget.getInstance().hide();
				UniTimePageLabel.getInstance().setPageName(result.getSolverConfigId() == null ? MSG.pageAddSolverConfiguration() : MSG.pageEditSolverConfiguration());
				iHeader.setEnabled("save", result.getSolverConfigId() == null);
				iHeader.setEnabled("update", result.getSolverConfigId() != null);
				iHeader.setEnabled("delete", result.getSolverConfigId() != null);
				iHeader.setEnabled("export", result.getSolverConfigId() != null);
				iHeader.setEnabled("back", true);
				iConfig = result.getSolverConfig();
				if (iConfig == null) iConfig = new SolverConfigInterface();

				iHeader.setHeaderTitle(result.getSolverConfigId() == null ? COURSE.sectAddSolverConfiguration() : COURSE.sectEditSolverConfiguration());
				iPanel.clear();
				iHeader.clearMessage();
				iPanel.addHeaderRow(iHeader);
				
				iReference = new TextBox();
				iReference.setWidth("375px"); iReference.setMaxLength(100);
				if (iConfig.hasReference()) iReference.setText(iConfig.getReference());
				iPanel.addRow(COURSE.fieldReference() + ":", iReference);
				iReference.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iConfig.setReference(event.getValue());
					}
				});
				
				iName = new TextBox();
				iName.setWidth("375px"); iName.setMaxLength(1000);
				if (iConfig.hasName()) iName.setText(iConfig.getName());
				iPanel.addRow(COURSE.fieldName() + ":", iName);
				iName.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iConfig.setName(event.getValue());
					}
				});
				
				iAppearance = new ListBox();
				iAppearance.addItem(COURSE.solverConfigAppearanceTimetables(), Appearance.TIMETABLES.name());
				iAppearance.addItem(COURSE.solverConfigAppearanceSolver(), Appearance.SOLVER.name());
				iAppearance.addItem(COURSE.solverConfigAppearanceExamSolver(), Appearance.EXAM_SOLVER.name());
				iAppearance.addItem(COURSE.solverConfigAppearanceStudentSolver(), Appearance.STUDENT_SOLVER.name());
				iAppearance.addItem(COURSE.solverConfigAppearanceInstructorSolver(), Appearance.INSTRUCTOR_SOLVER.name());
				iAppearance.setSelectedIndex(iConfig.getAppearance() == null ? 1 : iConfig.getAppearance().ordinal());
				iAppearance.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						iConfig.setAppearance(Appearance.valueOf(iAppearance.getSelectedValue()));
						appearanceChanged();
					}
				});
				iAppearanceLine = iPanel.addRow(COURSE.fieldAppearance() + ":", iAppearance);
				
				appearanceChanged();
			}
		});
	}
	
	protected void exportData(Long configId) {
		String query = "output=solver-config.properties&configId=" + configId;
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
	
	protected void saveOrUpdateSolverConfig() {
		if (validateSolverConfig()) {
			RPC.execute(new SolverConfigEditRequest(Operation.SAVE, iConfig), new AsyncCallback<SolverConfigEditResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(SolverConfigEditResponse result) {
					History.newItem(null, false);
					showSolverConfigs(result.getSolverConfigId());
				}
			});
		}
	}
	
	protected boolean validateSolverConfig() {
		List<String> errors = new ArrayList<String>();
		if (!iConfig.hasReference())
			errors.add(COURSE.errorRequiredField(COURSE.fieldReference()));
		if (!iConfig.hasName())
			errors.add(COURSE.errorRequiredField(COURSE.fieldName()));
		
		if (iConfig.hasGroups()) {
			for (SolverParameterGroupInterface g: iConfig.getGroups()) {
				if (!g.hasParameters()) continue;
				if (g.getSolverType() != iConfig.getAppearance().getSolverType()) continue;
				for (SolverParameterInterface p: g.getParameters()) {
					if (!p.isUseDefault() && !p.hasValue()) {
						if (!"text".equals(p.getType()))
							errors.add(COURSE.errorRequiredField(p.getDescription()));
					}
				}
			}
		}
		
		if (errors.isEmpty())
			iHeader.clearMessage();
		else {
			String message = "";
			for (String e: errors)
				message += (message.isEmpty() ? "" : "\n") + e;
			iHeader.setErrorMessage(message);
		}

		return errors.isEmpty();
	}
	
	protected void appearanceChanged() {
		while (iPanel.getRowCount() > iAppearanceLine + 1)
			iPanel.removeRow(iPanel.getRowCount() - 1);
		
		if (iConfig.hasGroups()) {
			SolverParameterGroupInterface first = null;
			for (SolverParameterGroupInterface g: iConfig.getGroups()) {
				if (!g.hasParameters()) continue;
				if (g.getSolverType() != iConfig.getAppearance().getSolverType()) continue;
				iPanel.addHeaderRow(g.getLabel());
				for (SolverParameterInterface p: g.getParameters()) {
					SolverSettingWidget w = new SolverSettingWidget(p);
					if (w.isSameLine()) {
						iPanel.addRow(w.getCheckBox(), w.getWidget());
					} else {
						iPanel.addRow(w.getCheckBox());
						iPanel.addRow(w.getWidget());
					}
					if (first == null) first = g;
				}
				if (g.equals(first)) {
					P hint = new P("default-hint"); hint.setText(MSG.hintDefaultCheckbox());
					iPanel.addRow(hint);
				}
			}
		}
		iPanel.addBottomRow(iFooter);
	}
	
	protected class SolverSettingWidget {
		Widget iWidget;
		AriaCheckBox iCheck;
		
		SolverSettingWidget(SolverParameterInterface p) {
			iCheck = new AriaCheckBox();
			iCheck.setText(p.getDescription() + ":");
			iCheck.setAriaLabel(ARIA.useDefaultForSolverConfiguration(p.getDescription()));
			iCheck.setValue(p.isUseDefault());
			iWidget = generateWidget(p);
		}
		
		protected TextBox getTextBasedWidget(final SolverParameterInterface p) {
			if ("double".equalsIgnoreCase(p.getType()) || "float".equalsIgnoreCase(p.getType())) {
				NumberBox box = new NumberBox();
				box.setDecimal(true);
				box.setNegative(true);
				return box;
			} else if ("long".equalsIgnoreCase(p.getType()) || "integer".equalsIgnoreCase(p.getType())) {
				NumberBox box = new NumberBox();
				box.setDecimal(false);
				box.setNegative(true);
				return box;
			} else {
				TextBox box = new TextBox();
				box.setWidth("375px"); box.setMaxLength(2048);
				return box;
			}
				
		}
		
		protected Widget generateWidget(final SolverParameterInterface p) {
			if ("boolean".equals(p.getType())) {
				final AriaCheckBox widget = new AriaCheckBox();
				if (p.isUseDefault())
					widget.setValue(p.hasDefaultValue() ? "true".equalsIgnoreCase(p.getDefaultValue()) || "on".equalsIgnoreCase(p.getDefaultValue()) || "1".equalsIgnoreCase(p.getDefaultValue()) : false);
				else
					widget.setValue(p.hasValue() ? "true".equalsIgnoreCase(p.getValue()) || "on".equalsIgnoreCase(p.getValue()) || "1".equalsIgnoreCase(p.getValue()) : false);
				p.setValue(widget.getValue() ? "true" : "false");
				widget.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						p.setValue(event.getValue() ? "true" : "false");
					}
				});
				iCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if (event.getValue()) {
							p.setUseDefault(true);
							widget.setEnabled(false);
							widget.setValue(p.hasDefaultValue() ? "true".equalsIgnoreCase(p.getDefaultValue()) || "on".equalsIgnoreCase(p.getDefaultValue()) || "1".equalsIgnoreCase(p.getDefaultValue()) : false);
							p.setValue(widget.getValue() ? "true" : "false");
						} else {
							p.setUseDefault(false);
							widget.setEnabled(true);
						}
					}
				});
				widget.setEnabled(!p.isUseDefault());
				widget.setAriaLabel(ARIA.valueForSolverConfiguration(p.getDescription()));
				return widget;
			} else if (p.getType().startsWith("enum(") && p.getType().endsWith(")")) {
				final Enum widget = new Enum(p);
				if (p.isUseDefault())
					widget.setValue(p.hasDefaultValue() ? p.getDefaultValue() : "");
				else
					widget.setValue(p.hasValue() ? p.getValue() : "");
				p.setValue(widget.getValue());
				widget.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						p.setValue(event.getValue());
					}
				});
				iCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if (event.getValue()) {
							p.setUseDefault(true);
							widget.setEnabled(false);
							widget.setValue(p.hasDefaultValue() ? p.getDefaultValue() : "");
							p.setValue(widget.getValue());
						} else {
							p.setUseDefault(false);
							widget.setEnabled(true);
						}
					}
				});
				widget.setEnabled(!p.isUseDefault());
				Roles.getListboxRole().setAriaLabelProperty(widget.getElement(), ARIA.valueForSolverConfiguration(p.getDescription()));
				return widget;
			} else if ("timepref".equals(p.getType())) {
				final TimePrefWidget widget = new TimePrefWidget(p);
				widget.addValueChangeHandler(new ValueChangeHandler<RoomSharingModel>() {
					@Override
					public void onValueChange(ValueChangeEvent<RoomSharingModel> event) {
						p.setValue(((InstructorAvailabilityModel)event.getValue()).getPattern());
					}
				});
				iCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if (event.getValue()) {
							p.setUseDefault(true);
							widget.setPattern(p.hasDefaultValue() ? p.getDefaultValue() : "");
							widget.setEditable(false);
							widget.setModel(widget.getModel());
							p.setValue(widget.getPattern());
						} else {
							p.setUseDefault(false);
							widget.setEditable(true);
							widget.setModel(widget.getModel());
						}
					}
				});
				return widget;
			} else {
				TextBox widget = getTextBasedWidget(p);
				widget.addStyleName("unitime-TextBox");
				if (p.isUseDefault())
					widget.setValue(p.hasDefaultValue() ? p.getDefaultValue() : "");
				else
					widget.setValue(p.hasValue() ? p.getValue() : "");
				p.setValue(widget.getValue());
				widget.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						p.setValue(event.getValue());
					}
				});
				iCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						if (event.getValue()) {
							p.setUseDefault(true);
							widget.setEnabled(false);
							widget.setValue(p.hasDefaultValue() ? p.getDefaultValue() : "");
							p.setValue(widget.getValue());
						} else {
							p.setUseDefault(false);
							widget.setEnabled(true);
						}
					}
				});
				widget.setEnabled(!p.isUseDefault());
				Roles.getTextboxRole().setAriaLabelProperty(widget.getElement(), ARIA.valueForSolverConfiguration(p.getDescription()));
				return widget;
			}
		}
		
		public Widget getWidget() { return iWidget; }
		public CheckBox getCheckBox() { return iCheck; }
		public boolean isSameLine() {
			if (iWidget instanceof InstructorAvailabilityWidget)
				return false;
			return true;
		}
	}
	
	public static class Enum extends ListBox implements HasValue<String> {
		public Enum(final SolverParameterInterface p) {
			getElement().getStyle().setProperty("min-width", "375px");
			String[] list = p.getType().substring("enum(".length(), p.getType().length() - 1).split(",");
			for (String item: list)
				addItem(item.trim());
			addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					ValueChangeEvent.fire(Enum.this, getValue());
				}
			});
		}
		
		public int index(String item) {
			if (item == null) return 0;
			for (int i = 0; i < getItemCount(); i++) {
				if (getItemText(i).equalsIgnoreCase(item.trim()))
					return i;
			}
			return 0;
		}

		@Override
		public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
			return addHandler(handler, ValueChangeEvent.getType());
		}

		@Override
		public String getValue() {
			return getSelectedItemText();
		}

		@Override
		public void setValue(String value) {
			setSelectedIndex(index(value));
		}

		@Override
		public void setValue(String value, boolean fireEvents) {
			setSelectedIndex(index(value));
			if (fireEvents)
				ValueChangeEvent.fire(Enum.this, getValue());
		}
	}
	
	public static class TimePrefWidget extends InstructorAvailabilityWidget {
		public TimePrefWidget(final SolverParameterInterface p) {
			super();
			addStyleName("TimePrefWidget");
			RPC.execute(InstructorAvailabilityRequest.load(null), new AsyncCallback<InstructorAvailabilityModel>() {
				@Override
				public void onFailure(Throwable caught) {
					UniTimeNotifications.error(caught);
				}

				@Override
				public void onSuccess(final InstructorAvailabilityModel model) {
					if (p.isUseDefault())
						model.setPattern(p.hasDefaultValue() ? p.getDefaultValue() : "");
					else
						model.setPattern(p.hasValue() ? p.getValue() : "");
					setEditable(!p.isUseDefault());
					setShowLegend(true);
					setModel(model);
				}
			});
		}
		
		public String getPattern() {
			return ((InstructorAvailabilityModel)getModel()).getPattern();
		}
		
		public void setPattern(String pattern) {
			((InstructorAvailabilityModel)getModel()).setPattern(pattern);
		}
	}
	
	public static class SolverConfigsRequest implements GwtRpcRequest<SolverConfigsResponse> {
	}
	
	public static class SolverConfigsResponse implements GwtRpcResponse {
		private TableInterface iSolverConfigsTable;
		
		public TableInterface getSolverConfigsTable() { return iSolverConfigsTable; }
		public void setSolverConfigsTable(TableInterface table) { iSolverConfigsTable = table; }
	}
	
	public static class SolverConfigEditRequest implements GwtRpcRequest<SolverConfigEditResponse> {
		private Long iSolverConfigId;
		private SolverConfigInterface iSolverConfig;
		private Operation iOperation;
		private Appearance iAppearance;
		
		public SolverConfigEditRequest() {}
		public SolverConfigEditRequest(Operation operation) {
			iOperation = operation;
		}
		public SolverConfigEditRequest(Operation operation, Long solverConfigId) {
			iOperation = operation; iSolverConfigId = solverConfigId;
		}
		public SolverConfigEditRequest(Operation operation, SolverConfigInterface solverConfig) {
			iOperation = operation;
			iSolverConfig = solverConfig;
			iSolverConfigId = (solverConfig == null ? null : solverConfig.getSolverConfigId());
		}
		public SolverConfigEditRequest(Operation operation, Appearance appearance) {
			iOperation = operation;
			iAppearance = appearance;
		}
		
		public Long getSolverConfigId() { return iSolverConfigId; }
		public void setSolverConfigId(Long solverConfigId) { iSolverConfigId = solverConfigId; }
		public SolverConfigInterface getSolverConfig() { return iSolverConfig; }
		public void setSolverConfig(SolverConfigInterface solverConfig) { iSolverConfig = solverConfig; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		public Appearance getAppearance() { return iAppearance; }
		public void setAppearance(Appearance appearance) { iAppearance = appearance; }

		public static enum Operation {
			ADD, EDIT, SAVE, DELETE,
		}
	}
	
	public static class SolverConfigEditResponse implements GwtRpcResponse {
		private SolverConfigInterface iSolverConfig;
		
		public SolverConfigInterface getSolverConfig() { return iSolverConfig; }
		public void setSolverConfig(SolverConfigInterface config) { iSolverConfig = config; }
		public Long getSolverConfigId() { return iSolverConfig == null ? null : iSolverConfig.getSolverConfigId(); }
	}
	
	public static class SolverParameterInterface implements IsSerializable {
		private String iName;
		private String iDescription;
		private boolean iUseDefault;
		private String iType;
		private String iDefaultValue;
		private String iValue;
		
		public SolverParameterInterface() {}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public boolean hasName() { return iName != null && !iName.isEmpty(); }

		public String getDescription() { return iDescription; }
		public void setDescription(String description) { iDescription = description; }
		public boolean hasDescription() { return iDescription != null && !iDescription.isEmpty(); }

		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		public boolean hasType() { return iType != null && !iType.isEmpty(); }

		public String getDefaultValue() { return iDefaultValue; }
		public void setDefaultValue(String defaultValue) { iDefaultValue = defaultValue; }
		public boolean hasDefaultValue() { return iDefaultValue != null && !iDefaultValue.isEmpty(); }

		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }
		public boolean hasValue() { return iValue != null && !iValue.isEmpty(); }

		public boolean isUseDefault() { return iUseDefault; }
		public void setUseDefault(boolean useDefault) { iUseDefault = useDefault; }
	}
	
	
	public static class SolverParameterGroupInterface implements IsSerializable {
		private Long iId;
		private String iLabel;
		private SolverType iType;
		private List<SolverParameterInterface> iParameters;

		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }

		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		public boolean hasLabel() { return iLabel != null && !iLabel.isEmpty(); }
		
		public SolverType getSolverType() { return iType; }
		public void setSolverType(SolverType type) { iType = type; }
		
		public boolean hasParameters() { return iParameters != null && !iParameters.isEmpty(); }
		public void addParameter(SolverParameterInterface parameter) {
			if (iParameters == null) iParameters = new ArrayList<SolverParameterInterface>();
			iParameters.add(parameter);
		}
		public List<SolverParameterInterface> getParameters() { return iParameters; }
		public SolverParameterInterface getParameter(String name) {
			if (iParameters == null || name == null) return null;
			for (SolverParameterInterface param: iParameters)
				if (name.equals(param.getName())) return param;
			return null;
		}
	}
	
	public static class SolverConfigInterface implements IsSerializable {
		private Long iSolverConfigId;
		private String iName, iReference;
		private Appearance iAppearance = Appearance.SOLVER;
		private List<SolverParameterGroupInterface> iGroups;
		
		public Long getSolverConfigId() { return iSolverConfigId; }
		public void setSolverConfigId(Long managerId) { iSolverConfigId = managerId; }
		
		public boolean hasName() { return iName != null && !iName.isEmpty(); }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public boolean hasReference() { return iReference != null && !iReference.isEmpty(); }
		public String getReference() { return iReference; }
		public void setReference(String Reference) { iReference = Reference; }
		
		public Appearance getAppearance() { return iAppearance; }
		public void setAppearance(Appearance appearance) { iAppearance = appearance; }
		
		public boolean hasGroups() { return iGroups != null && !iGroups.isEmpty(); }
		public void addGroup(SolverParameterGroupInterface group) {
			if (iGroups == null) iGroups = new ArrayList<SolverParameterGroupInterface>();
			iGroups.add(group);
		}
		public List<SolverParameterGroupInterface> getGroups() { return iGroups; }
		public SolverParameterGroupInterface getGroup(Long id) {
			if (iGroups == null || id == null) return null;
			for (SolverParameterGroupInterface group: iGroups)
				if (id.equals(group.getId())) return  group;
			return null;
		}
		public SolverParameterInterface getParameter(Long groupId, String name) {
			SolverParameterGroupInterface group = getGroup(groupId);
			return group == null ? null : group.getParameter(name);
		}
		
		public static enum Appearance {
			TIMETABLES(SolverType.COURSE),
			SOLVER(SolverType.COURSE),
			EXAM_SOLVER(SolverType.EXAM),
			STUDENT_SOLVER(SolverType.STUDENT),
			INSTRUCTOR_SOLVER(SolverType.INSTRUCTOR),
			;
			SolverType iType;
			private Appearance(SolverType type) { iType = type; }
			public SolverType getSolverType() { return iType; }
		}
	}

}
