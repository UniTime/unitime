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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.IdLabel;
import org.unitime.timetable.gwt.client.admin.DatePatternsPage.DatePatternEditRequest.Operation;
import org.unitime.timetable.gwt.client.admin.DatePatternsPage.DatePatternInterface.Type;
import org.unitime.timetable.gwt.client.admin.TimetableManagersPage.DepartmentsTable;
import org.unitime.timetable.gwt.client.events.SessionDatesSelector;
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
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;

import com.google.gwt.aria.client.Roles;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class DatePatternsPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iListHeader, iListFooter;
	private TableWidget iTable;
	private UniTimeHeaderPanel iHeader, iFooter;
	private DatePatternInterface iPattern;

	public DatePatternsPage() {
		iPanel = new SimpleForm();
		initWidget(iPanel);
		iPanel.addStyleName("unitime-DatePatternsPage");
		iListHeader = new UniTimeHeaderPanel();
		iListHeader.addButton("add", COURSE.actionAddDatePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("add", false);
				editPattern(null);
			}
		});
		iListHeader.setEnabled("add", false);
		iListHeader.getButton("add").setTitle(COURSE.titleAddDatePattern());
		
		iListHeader.addButton("assign", COURSE.actionAssingDepartmentsToDatePatterns(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MSG.waitPlease());
				RPC.execute(new DatePatternEditRequest(Operation.ASSIGN_DEPTS), new AsyncCallback<DatePatternEditResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iListHeader.setErrorMessage(caught.getMessage());
						UniTimeNotifications.error(caught.getMessage(), caught);
						ToolBox.checkAccess(caught);						
					}

					@Override
					public void onSuccess(DatePatternEditResponse result) {
						LoadingWidget.getInstance().hide();
						if (result.hasLog())
							download(result.getLog(), "assigned-departments");
						showPatterns(null);
					}
				});
			}
		});
		iListHeader.getButton("assign").setTitle(COURSE.titleAssingDepartmentsToDatePatterns());
		iListHeader.setEnabled("assign", false);
		iListHeader.addButton("push", COURSE.actionPushUpDatePatterns(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MSG.waitPlease());
				RPC.execute(new DatePatternEditRequest(Operation.PUSH_UP), new AsyncCallback<DatePatternEditResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iListHeader.setErrorMessage(caught.getMessage());
						UniTimeNotifications.error(caught.getMessage(), caught);
						ToolBox.checkAccess(caught);						
					}

					@Override
					public void onSuccess(DatePatternEditResponse result) {
						LoadingWidget.getInstance().hide();
						if (result.hasLog())
							download(result.getLog(), "push-up-date-patterns");
						showPatterns(null);
					}
				});
			}
		});
		iListHeader.getButton("push").setTitle(COURSE.titlePushUpDatePatterns());
		iListHeader.setEnabled("push", false);
		
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
		iHeader.addButton("save", COURSE.actionSaveDatePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPattern.setDefault(false);
				saveOrUpdatePattern(null);
			}
		});
		iHeader.setEnabled("save", false);
		iHeader.addButton("update", COURSE.actionUpdateDatePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPattern.setDefault(false);
				saveOrUpdatePattern(null);
			}
		});
		iHeader.setEnabled("update", false);
		iHeader.addButton("previous", COURSE.actionPreviousDatePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPattern.setDefault(false);
				saveOrUpdatePattern(getPrevId(iPattern.getPatternId()));
			}
		});
		iHeader.setEnabled("previous", false);
		iHeader.addButton("next", COURSE.actionNextDatePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPattern.setDefault(false);
				saveOrUpdatePattern(getNextId(iPattern.getPatternId()));
			}
		});
		iHeader.setEnabled("next", false);
		iHeader.addButton("delete", COURSE.actionDeleteDatePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(COURSE.confirmDeleteDatePattern(), new Command() {
					@Override
					public void execute() {
						deletePattern();
					}
				});
			}
		});
		iHeader.setEnabled("delete", false);
		iHeader.addButton("default", COURSE.actionMakeDatePatternDefaulf(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(COURSE.confirmDefaultDatePatternChange(), new Command() {
					@Override
					public void execute() {
						iPattern.setDefault(true);
						saveOrUpdatePattern(null);
					}
				});
			}
		});
		iHeader.setEnabled("default", false);
		iHeader.addButton("back", COURSE.actionBackToManagers(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(null, false);
				showPatterns(iPattern == null ? null : iPattern.getPatternId());
			}
		});
		iHeader.getButton("back").setAccessKey(COURSE.accessBackToManagers().charAt(0));
		iHeader.getButton("back").setTitle(COURSE.titleBackToManagers(COURSE.accessBackToManagers()));
		iHeader.setEnabled("back", false);
		
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
			showPatterns();
		else if ("add".equals(token))
			editPattern(null);
		else {
			try {
				editPattern(Long.valueOf(token));
			} catch (NumberFormatException e) {
				showPatterns();
			}
		}
	}
	
	protected void showPatterns() {
		showPatterns(null);
	}
	
	protected Long getNextId(Long patternId) {
		if (iTable == null || patternId == null) return null;
		for (int row = 0; row < iTable.getRowCount(); row++) {
			LineInterface line = iTable.getData(row);
			if (line != null && patternId.equals(line.getId())) {
				LineInterface next = iTable.getData(row + 1);
				return (next == null ? null : next.getId());
			}
		}
		return null;
	}
	
	protected Long getPrevId(Long patternId) {
		if (iTable == null || patternId == null) return null;
		for (int row = 0; row < iTable.getRowCount(); row++) {
			LineInterface line = iTable.getData(row);
			if (line != null && patternId.equals(line.getId())) {
				LineInterface prev = iTable.getData(row - 1);
				return (prev == null ? null : prev.getId());
			}
		}
		return null;
	}

	protected void showPatterns(final Long patternId) {
		UniTimePageLabel.getInstance().setPageName(MSG.pageDatePatterns());
		iPanel.clear();
		iListHeader.setEnabled("add", false);
		iListHeader.setEnabled("csv", false);
		iListHeader.setEnabled("pdf", false);
		iListHeader.setEnabled("assign", false);
		iListHeader.setEnabled("push", false);
		iPanel.addHeaderRow(iListHeader);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new DatePatternsRequest(), new AsyncCallback<DatePatternsResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(DatePatternsResponse result) {
				LoadingWidget.getInstance().hide();
				iTable.setData(result.getTable());
				iPanel.addRow(iTable);
				iPanel.addBottomRow(iListFooter);
				iListHeader.setHeaderTitle(result.getTable().getName());
				iListHeader.setEnabled("add", result.isCanAdd());
				if (patternId != null)
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						LineInterface line = iTable.getData(row);
						if (line != null && patternId.equals(line.getId())) {
							Element el = iTable.getRowFormatter().getElement(row);
							ToolBox.scrollToElement(el);
							ToolBox.focusOnRow(el);
						}
					}
				iListHeader.setEnabled("csv", result.getTable().hasLines());
				iListHeader.setEnabled("pdf", result.getTable().hasLines());
				iListHeader.setEnabled("assign", result.getTable().hasLines());
				iListHeader.setEnabled("push", result.getTable().hasLines());
			}
		});		
	}
	
	private TextBox iName;
	private CheckBox iDefault, iVisible;
	private NumberBox iNbrWeeks;
	private ListBox iType, iDepartments, iParents, iChildren;
	private DepartmentsTable iDepartmentsTable, iParentsTable, iChildrenTable;
	private int iDepartmentsRow, iParentsRow, iChildrenRow, iPatternRow;
	private SessionDatesSelector iDates;
	private int iBaseOffset = 0;
	
	protected void editPattern(Long patternId) {
		Window.scrollTo(0, 0);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new DatePatternEditRequest(patternId == null ? Operation.ADD : Operation.EDIT, patternId), new AsyncCallback<DatePatternEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToLoadData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(DatePatternEditResponse result) {
				LoadingWidget.getInstance().hide();
				UniTimePageLabel.getInstance().setPageName(result.getPatternId() == null ? MSG.pageAddDatePattern() : MSG.pageEditDatePattern());
				iBaseOffset = result.getBaseOffset();
				iPattern = result.getPattern();
				if (iPattern == null)
					iPattern = new DatePatternInterface();
				iHeader.setEnabled("save", result.getPatternId() == null);
				iHeader.setEnabled("update", result.getPatternId() != null);
				iHeader.setEnabled("delete", result.isCanDelete() && result.getPatternId() != null);
				iHeader.setEnabled("previous", result.getPatternId() != null && getPrevId(result.getPatternId()) != null);
				iHeader.setEnabled("next", result.getPatternId() != null && getNextId(result.getPatternId()) != null);
				iHeader.setEnabled("default", result.getPatternId() != null && (iPattern.getType() == Type.Standard || iPattern.getType() == Type.NonStandard || iPattern.getType() == Type.Alternate));
				iHeader.setEnabled("back", true);
				
				iHeader.setHeaderTitle(iPattern.getPatternId() == null ? COURSE.sectAddDatePattern() : COURSE.sectEditDatePattern());
				iPanel.clear();
				iHeader.clearMessage();
				iPanel.addHeaderRow(iHeader);
				
				iName = new TextBox();
				iName.setWidth("350px"); iName.setMaxLength(50);
				if (iPattern.hasName()) iName.setText(iPattern.getName());
				iPanel.addRow(COURSE.propDatePatternName(), iName);
				iName.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iPattern.setName(event.getValue());
					}
				});
				
				iType = new ListBox();
				iType.addItem(COURSE.datePatternTypeStandard());
				iType.addItem(COURSE.datePatternTypeAlternateWeeks());
				iType.addItem(COURSE.datePatternTypeNonStandard());
				iType.addItem(COURSE.datePatternTypeExtended());
				iType.addItem(COURSE.datePatternTypeAltPatternSet());
				if (iPattern.getType() == null) iPattern.setType(Type.Standard);
				iType.setSelectedIndex(iPattern.getType().ordinal());
				iPanel.addRow(COURSE.propDatePatternType(), iType);
				iType.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						iPattern.setType(Type.values()[iType.getSelectedIndex()]);
						iHeader.setEnabled("default", result.getPatternId() != null && (iPattern.getType() == Type.Standard || iPattern.getType() == Type.NonStandard || iPattern.getType() == Type.Alternate));
						if (iChildrenRow >= 0) {
							iPanel.getRowFormatter().setVisible(iChildrenRow, iPattern.getType() == Type.PatternSet);
							iPanel.getRowFormatter().setVisible(iChildrenRow + 1, iPattern.getType() == Type.PatternSet);
						}
						if (iParentsRow >= 0) {
							iPanel.getRowFormatter().setVisible(iParentsRow, iPattern.getType() != Type.PatternSet);
							iPanel.getRowFormatter().setVisible(iParentsRow + 1, iPattern.getType() != Type.PatternSet);
						}
						if (iDepartmentsRow >= 0) {
							iPanel.getRowFormatter().setVisible(iDepartmentsRow, iPattern.getType() == Type.Extended || iPattern.getType() == Type.PatternSet);
							iPanel.getRowFormatter().setVisible(iDepartmentsRow + 1, iPattern.getType() == Type.Extended || iPattern.getType() == Type.PatternSet);
						}
						if (iPatternRow >= 0) {
							iPanel.getRowFormatter().setVisible(iPatternRow, iPattern.getType() != Type.PatternSet);
						}
					}
				});
				
				iNbrWeeks = new NumberBox(); iNbrWeeks.setNegative(false); iNbrWeeks.setDecimal(false);
				iNbrWeeks.setValue(iPattern.getNbrWeeks());
				iPanel.addRow(COURSE.propDatePatternNbrWeeks(), iNbrWeeks);
				iNbrWeeks.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iPattern.setNbrWeeks(iNbrWeeks.toInteger());
					}
				});
				
				iVisible = new CheckBox();
				iVisible.setValue(iPattern.isVisible());
				iVisible.setEnabled(iPattern.getPatternId() != null);
				iPanel.addRow(COURSE.propDatePatternVisible(), iVisible);
				iVisible.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						iPattern.setVisible(event.getValue());
					}
				});
				
				iDefault = new CheckBox();
				iDefault.setValue(iPattern.isDefault());
				iDefault.setEnabled(false);
				iPanel.addRow(COURSE.propDatePatternDefault(), iDefault);
				
				iDepartmentsRow = -1;
				if (result.hasDepartments()) {
					iDepartmentsTable = new DepartmentsTable(result.getDepartments()) {
						@Override
						public boolean removeDepartment(Long id) {
							iPattern.removeDepartmentId(id);
							return super.removeDepartment(id);
						}
					};
					iDepartments = new ListBox();
					Roles.getListboxRole().setAriaLabelProperty(iDepartments.getElement(), ARIA.listSelectItem(COURSE.columnDepartment()));
					iDepartments.addItem(COURSE.itemSelect(), "");
					for (IdLabel item: result.getDepartments())
						iDepartments.addItem(item.getLabel(), item.getId().toString());
					P dp = new P("departments-list");
					dp.add(iDepartments);
					Button button = new Button(COURSE.actionAddDepartment());
					dp.add(button);
					button.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (iDepartments.getSelectedIndex() > 0) {
								Long id = Long.valueOf(iDepartments.getSelectedValue());
								iDepartmentsTable.addDepartment(id);
								iPattern.addDepartmentId(id);
							}
						}
					});
					iDepartmentsRow = iPanel.addRow(COURSE.propDatePatternDepartments(), dp);
					iPanel.addRow("", iDepartmentsTable);
					if (iPattern.hasDepartmentIds())
						for (Long id: iPattern.getDepartmentIds())
							iDepartmentsTable.addDepartment(id);
					iDepartmentsTable.sort();
					iPanel.getRowFormatter().setVisible(iDepartmentsRow, iPattern.getType() == Type.Extended || iPattern.getType() == Type.PatternSet);
					iPanel.getRowFormatter().setVisible(iDepartmentsRow + 1, iPattern.getType() == Type.Extended || iPattern.getType() == Type.PatternSet);
				}
				
				iParentsRow = -1;
				if (result.hasParentPatterns()) {
					iParentsTable = new DepartmentsTable(result.getParentPatterns()) {
						@Override
						public boolean removeDepartment(Long id) {
							iPattern.removeParentId(id);
							return super.removeDepartment(id);
						}
					};
					iParents = new ListBox();
					Roles.getListboxRole().setAriaLabelProperty(iParents.getElement(), ARIA.listSelectItem(COURSE.propDatePatternAltPatternSets()));
					iParents.addItem(COURSE.itemSelect(), "");
					for (IdLabel item: result.getParentPatterns())
						iParents.addItem(item.getLabel(), item.getId().toString());
					P dp = new P("pattern-list");
					dp.add(iParents);
					Button button = new Button(COURSE.actionAddAltPatternSet());
					dp.add(button);
					button.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (iParents.getSelectedIndex() > 0) {
								Long id = Long.valueOf(iParents.getSelectedValue());
								iParentsTable.addDepartment(id);
								iPattern.addParentId(id);
							}
						}
					});
					iParentsRow = iPanel.addRow(COURSE.propDatePatternAltPatternSets(), dp);
					iPanel.addRow("", iParentsTable);
					if (iPattern.hasParentIds())
						for (Long id: iPattern.getParentIds())
							iParentsTable.addDepartment(id);
					iParentsTable.sort();
					iPanel.getRowFormatter().setVisible(iParentsRow, iPattern.getType() != Type.PatternSet);
					iPanel.getRowFormatter().setVisible(iParentsRow + 1, iPattern.getType() != Type.PatternSet);
				}
				
				iChildrenRow = -1;
				if (result.hasParentPatterns()) {
					iChildrenTable = new DepartmentsTable(result.getChildPatterns()) {
						@Override
						public boolean removeDepartment(Long id) {
							iPattern.removeChildrenId(id);
							return super.removeDepartment(id);
						}
					};
					iChildren = new ListBox();
					Roles.getListboxRole().setAriaLabelProperty(iParents.getElement(), ARIA.listSelectItem(COURSE.propDatePatternChildren()));
					iChildren.addItem(COURSE.itemSelect(), "");
					for (IdLabel item: result.getChildPatterns())
						iChildren.addItem(item.getLabel(), item.getId().toString());
					P dp = new P("pattern-list");
					dp.add(iChildren);
					Button button = new Button(COURSE.actionAddDatePattern());
					dp.add(button);
					button.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (iChildren.getSelectedIndex() > 0) {
								Long id = Long.valueOf(iChildren.getSelectedValue());
								iChildrenTable.addDepartment(id);
								iPattern.addChildrenId(id);
							}
						}
					});
					iChildrenRow = iPanel.addRow(COURSE.propDatePatternChildren(), dp);
					iPanel.addRow("", iChildrenTable);
					if (iPattern.hasChildrenIds())
						for (Long id: iPattern.getChildrenIds())
							iChildrenTable.addDepartment(id);
					iChildrenTable.sort();
					iPanel.getRowFormatter().setVisible(iChildrenRow, iPattern.getType() == Type.PatternSet);
					iPanel.getRowFormatter().setVisible(iChildrenRow + 1, iPattern.getType() == Type.PatternSet);
				}
				
				iPatternRow = -1;
				if (result.hasMonths()) {
					iDates = new SessionDatesSelector(result.getMonths());
					iPatternRow = iPanel.addRow(COURSE.propDatePatternPattern(), iDates);
					if (iPattern.hasPattern()) {
						String pattern = iPattern.getPattern();
						int offset = iBaseOffset - iPattern.getOffset();
						if (offset < 0) {
							if (pattern.length() > -offset)
								pattern = pattern.substring(-offset);
							else
								pattern = null;
						} else {
							for (int i = 0; i < offset; i++)
								pattern = "0" + pattern;
						}
						iDates.setPattern(pattern);
					}
					iPanel.getRowFormatter().setVisible(iPatternRow, iPattern.getType() != Type.PatternSet);
				}
				
				iPanel.addBottomRow(iFooter);
			}
		});
	}
	
	protected void saveOrUpdatePattern(final Long nextPatternId) {
		if (validatePattern()) {
			RPC.execute(new DatePatternEditRequest(Operation.SAVE, iPattern), new AsyncCallback<DatePatternEditResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(DatePatternEditResponse result) {
					History.newItem(null, false);
					if (nextPatternId != null)
						editPattern(nextPatternId);
					else
						showPatterns(result.getPatternId());
				}
			});
		}
	}
	
	protected boolean validatePattern() {
		if (iPattern.getType() == Type.PatternSet) {
			iPattern.setPattern(null); iPattern.setOffset(0);
		} else if (iDates != null) {
			String pattern = iDates.getPattern();
			int offset = pattern.indexOf('1');
			if (offset < 0) {
				iPattern.setPattern(null); iPattern.setOffset(0);	
			} else {
				if (offset > 0) pattern = pattern.substring(offset);
				int last = pattern.lastIndexOf('1') + 1;
				if (pattern.length() > last)
					pattern = pattern.substring(0, last);
				iPattern.setPattern(pattern);
				iPattern.setOffset(iBaseOffset - offset);
			}
		}
		List<String> errors = new ArrayList<String>();
		if (!iPattern.hasName())
			errors.add(COURSE.errorRequiredField(COURSE.columnDatePatternName()));
		if (iPattern.getType() == null)
			errors.add(COURSE.errorRequiredField(COURSE.columnDatePatternType()));
		if (iPattern.hasDepartmentIds() && iPattern.getType() != Type.Extended && iPattern.getType() != Type.PatternSet)
			errors.add(COURSE.errorOnyExtDatePatternsHaveDepartments());
		if (iPattern.getType() != Type.PatternSet && !iPattern.hasPattern())
			errors.add(COURSE.errorRequiredField(COURSE.columnDatePatternPattern()));
		if (iPattern.getType() != Type.PatternSet && iPattern.hasPattern() && iPattern.getPattern().length() > 366)
			errors.add(COURSE.errorDatePatternCannotContainMoreThanAYear());
		
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
	
	protected void deletePattern() {
		RPC.execute(new DatePatternEditRequest(Operation.DELETE, iPattern.getPatternId()), new AsyncCallback<DatePatternEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MSG.failedToDeleteData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToDeleteData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);					
			}

			@Override
			public void onSuccess(DatePatternEditResponse result) {
				History.newItem(null, false);
				showPatterns(null);
			}
		});
	}
	
	protected void exportData(String format) {
		String query = "output=date-patterns." + format + "&sort=" + iTable.getSortCookie();
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
	
	public static class DatePatternsRequest implements GwtRpcRequest<DatePatternsResponse> {
		private boolean iExport = false;

		public boolean isExport() { return iExport; }
		public void setExport(boolean export) { iExport = export; }
	}
	
	public static class DatePatternsResponse implements GwtRpcResponse {
		private TableInterface iTable;
		private boolean iCanAdd = false;
		
		public TableInterface getTable() { return iTable; }
		public void setTable(TableInterface table) { iTable = table; }
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
	}
	
	public static class DatePatternEditRequest implements GwtRpcRequest<DatePatternEditResponse> {
		private Long iPatternId;
		private DatePatternInterface iPattern;
		private Operation iOperation;
		
		public DatePatternEditRequest() {}
		public DatePatternEditRequest(Operation operation) {
			iOperation = operation;
		}
		public DatePatternEditRequest(Operation operation, Long patternId) {
			iOperation = operation; iPatternId = patternId;
		}
		public DatePatternEditRequest(Operation operation, DatePatternInterface pattern) {
			iOperation = operation;
			iPattern = pattern;
			iPatternId = (pattern == null ? null : pattern.getPatternId());
		}
		
		public Long getPatternId() { return iPatternId; }
		public void setPatternId(Long patternId) { iPatternId = patternId; }
		public DatePatternInterface getPattern() { return iPattern; }
		public void setPattern(DatePatternInterface patterm) { iPattern = patterm; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public static enum Operation {
			ADD, EDIT, SAVE, DELETE, ASSIGN_DEPTS, PUSH_UP
		}
	}
	
	public static class DatePatternEditResponse implements GwtRpcResponse {
		private byte[] iLog;
		private DatePatternInterface iPattern;
		private List<IdLabel> iDepartments, iParentPatterns, iChildPatterns;
		private Long iSessionId;
		private String iSessionName;
		private List<SessionMonth> iMonths;
		private int iBaseOffset = 0;
		private boolean iCanDelete = false;
		
		public DatePatternInterface getPattern() { return iPattern; }
		public void setPattern(DatePatternInterface patterm) { iPattern = patterm; }
		public Long getPatternId() { return iPattern == null ? null : iPattern.getPatternId(); }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public String getSessionName() { return iSessionName; }
		public void setSessionName(String name) { iSessionName = name; }
		public boolean hasSessionName() { return iSessionName != null && !iSessionName.isEmpty(); }
		
		public void addDepartment(Long id, String label) {
			if (iDepartments == null) iDepartments = new ArrayList<IdLabel>();
			iDepartments.add(new IdLabel(id, label));
		}
		public List<IdLabel> getDepartments() { return iDepartments; }
		public boolean hasDepartments() { return iDepartments != null && !iDepartments.isEmpty(); }
		public IdLabel getDepartment(Long id) {
			if (iDepartments == null || id == null) return null;
			for (IdLabel item: iDepartments)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public void addParentPattern(Long id, String label) {
			if (iParentPatterns == null) iParentPatterns = new ArrayList<IdLabel>();
			iParentPatterns.add(new IdLabel(id, label));
		}
		public List<IdLabel> getParentPatterns() { return iParentPatterns; }
		public boolean hasParentPatterns() { return iParentPatterns != null && !iParentPatterns.isEmpty(); }
		public IdLabel getParentPattern(Long id) {
			if (iParentPatterns == null || id == null) return null;
			for (IdLabel item: iParentPatterns)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public void addChildPattern(Long id, String label) {
			if (iChildPatterns == null) iChildPatterns = new ArrayList<IdLabel>();
			iChildPatterns.add(new IdLabel(id, label));
		}
		public List<IdLabel> getChildPatterns() { return iChildPatterns; }
		public boolean hasChildPatterns() { return iChildPatterns != null && !iChildPatterns.isEmpty(); }
		public IdLabel getChildPattern(Long id) {
			if (iChildPatterns == null || id == null) return null;
			for (IdLabel item: iChildPatterns)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public void addMonth(SessionMonth month) {
			if (iMonths == null) iMonths = new ArrayList<SessionMonth>();
			iMonths.add(month);
		}
		public boolean hasMonths() { return iMonths != null && !iMonths.isEmpty(); }
		public List<SessionMonth> getMonths() { return iMonths; }
		
		public void setBaseOffset(int offset) { iBaseOffset = offset; }
		public int getBaseOffset() { return iBaseOffset; }
		
		public boolean hasLog() { return iLog != null; }
		public byte[] getLog() { return iLog; }
		public void setLog(byte[] log) { iLog = log; }
		
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
	}
	
	public static class DatePatternInterface implements IsSerializable {
		private Long iPatternId;
		private String iName;
		private Type iType;
		private Integer iNbrWeeks;
		private boolean iVisible = true, iDefault = false;
		private Set<Long> iDepartmentIds, iParentIds, iChildrenIds;
		private String iPattern;
		private Integer iOffset;
		
		public Long getPatternId() { return iPatternId; }
		public void setPatternId(Long patternId) { iPatternId = patternId; }

		public boolean hasName() { return iName != null && !iName.isEmpty(); }
		public String getName() { return iName; }
		public void setName(String firstName) { iName = firstName; }
		public Type getType() { return iType; }
		public void setType(Type type) { iType = type; }
		public Integer getNbrWeeks() { return iNbrWeeks; }
		public void setNbrWeeks(Integer nbrWeeks) { iNbrWeeks = nbrWeeks; }
		public boolean isVisible() { return iVisible; }
		public void setVisible(boolean visible) { iVisible = visible; }
		public boolean isDefault() { return iDefault; }
		public void setDefault(boolean isDefault) { iDefault = isDefault; }
		
		public boolean hasPattern() { return iPattern != null && !iPattern.isEmpty(); }
		public String getPattern() { return iPattern; }
		public void setPattern(String pattern) { iPattern = pattern; }
		public Integer getOffset() { return iOffset; }
		public void setOffset(Integer offset) { iOffset = offset; }
		
		
		public boolean hasDepartmentIds() { return iDepartmentIds != null && !iDepartmentIds.isEmpty(); }
		public void addDepartmentId(Long id) {
			if (iDepartmentIds == null) iDepartmentIds = new HashSet<Long>();
			iDepartmentIds.add(id);
		}
		public void removeDepartmentId(Long id) {
			if (iDepartmentIds != null && id != null) iDepartmentIds.remove(id);
		}
		public Set<Long> getDepartmentIds() { return iDepartmentIds; }
		public boolean hasDepartmentId(Long id) {
			if (iDepartmentIds == null) return false;
			return iDepartmentIds.contains(id);
		}
		
		public boolean hasParentIds() { return iParentIds != null && !iParentIds.isEmpty(); }
		public void addParentId(Long id) {
			if (iParentIds == null) iParentIds = new HashSet<Long>();
			iParentIds.add(id);
		}
		public void removeParentId(Long id) {
			if (iParentIds != null && id != null) iParentIds.remove(id);
		}
		public Set<Long> getParentIds() { return iParentIds; }
		public boolean hasParentId(Long id) {
			if (iParentIds == null) return false;
			return iParentIds.contains(id);
		}
		
		public boolean hasChildrenIds() { return iChildrenIds != null && !iChildrenIds.isEmpty(); }
		public void addChildrenId(Long id) {
			if (iChildrenIds == null) iChildrenIds = new HashSet<Long>();
			iChildrenIds.add(id);
		}
		public void removeChildrenId(Long id) {
			if (iChildrenIds != null && id != null) iChildrenIds.remove(id);
		}
		public Set<Long> getChildrenIds() { return iChildrenIds; }
		public boolean hasChildrenId(Long id) {
			if (iChildrenIds == null) return false;
			return iChildrenIds.contains(id);
		}
		
		public static enum Type {
			Standard,
			Alternate,
			NonStandard,
			Extended,
			PatternSet,
		};
	}
	

	public static final native String download(byte[] bytes, String name) /*-{
		var data = new Uint8Array(bytes);
		var blob = new Blob([data], {type: "text/plain"});
		if ($wnd.navigator && $wnd.navigator.msSaveOrOpenBlob) {
			$wnd.navigator.msSaveOrOpenBlob(blob, name + ".txt");
		} else {
			var link = $doc.createElement("a");
			link.href = $wnd.URL.createObjectURL(blob);
			link.download = name + ".txt";
			link.target = "_blank";
			$doc.body.appendChild(link);
			link.click();
			$doc.body.removeChild(link);
			$wnd.URL.revokeObjectURL(link.href);
		}
	}-*/;
}
