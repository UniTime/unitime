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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.IdLabel;
import org.unitime.timetable.gwt.client.admin.TimePatternsPage.TimePatternEditRequest.Operation;
import org.unitime.timetable.gwt.client.admin.TimePatternsPage.TimePatternInterface.Type;
import org.unitime.timetable.gwt.client.admin.TimetableManagersPage.DepartmentsTable;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DayCode;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimePatternModel;
import org.unitime.timetable.gwt.client.offerings.TimePreferenceWidget;
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
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;

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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class TimePatternsPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static GwtConstants CONST = GWT.create(GwtConstants.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iListHeader, iListFooter;
	private TableWidget iTable;
	private UniTimeHeaderPanel iHeader, iFooter;
	private TimePatternInterface iPattern;
	
	public TimePatternsPage() {
		iPanel = new SimpleForm();
		initWidget(iPanel);
		iPanel.addStyleName("unitime-TimePatternsPage");
		iListHeader = new UniTimeHeaderPanel();
		iListHeader.addButton("add", COURSE.actionAddTimePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("add", false);
				editPattern(null);
			}
		});
		iListHeader.setEnabled("add", false);
		iListHeader.getButton("add").setTitle(COURSE.titleAddTimePattern());
		
		iListHeader.addButton("assign", COURSE.actionAssingDepartmentsToTimePatterns(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MSG.waitPlease());
				RPC.execute(new TimePatternEditRequest(Operation.ASSIGN_DEPTS), new AsyncCallback<TimePatternEditResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iListHeader.setErrorMessage(caught.getMessage());
						UniTimeNotifications.error(caught.getMessage(), caught);
						ToolBox.checkAccess(caught);						
					}

					@Override
					public void onSuccess(TimePatternEditResponse result) {
						LoadingWidget.getInstance().hide();
						if (result.hasLog())
							DatePatternsPage.download(result.getLog(), "assigned-departments");
						showPatterns(null);
					}
				});
			}
		});
		iListHeader.getButton("assign").setTitle(COURSE.titleAssingDepartmentsToTimePatterns());
		iListHeader.setEnabled("assign", false);
		iListHeader.addButton("exact", COURSE.actionExactTimesCSV(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				exportData("exact-time-classes.csv");
			}
		});
		iListHeader.getButton("exact").setTitle(COURSE.titleExactTimesCSV());
		iListHeader.setEnabled("exact", false);
		iListHeader.addButton("pdf", COURSE.actionExportPdf(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				exportData("time-patterns.pdf");
			}
		});
		iListHeader.getButton("pdf").setAccessKey(COURSE.accessExportPdf().charAt(0));
		iListHeader.getButton("pdf").setTitle(COURSE.titleExportPdf(COURSE.accessExportPdf()));
		iListHeader.setEnabled("pdf", false);
		iListHeader.addButton("csv", COURSE.actionExportCsv(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				exportData("time-patterns.csv");
			}
		});
		iListHeader.getButton("csv").setAccessKey(COURSE.accessExportCsv().charAt(0));
		iListHeader.getButton("csv").setTitle(COURSE.titleExportCsv(COURSE.accessExportCsv()));
		iListHeader.setEnabled("csv", false);
		iListFooter = iListHeader.clonePanel();
		
		iTable = new TableWidget();
		iTable.addStyleName("table");
		
		iHeader = new UniTimeHeaderPanel("");
		iHeader.addButton("save", COURSE.actionSaveTimePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdatePattern(null);
			}
		});
		iHeader.setEnabled("save", false);
		iHeader.addButton("update", COURSE.actionUpdateTimePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdatePattern(null);
			}
		});
		iHeader.setEnabled("update", false);
		iHeader.addButton("previous", COURSE.actionPreviousTimePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdatePattern(getPrevId(iPattern.getPatternId()));
			}
		});
		iHeader.setEnabled("previous", false);
		iHeader.addButton("next", COURSE.actionNextTimePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdatePattern(getNextId(iPattern.getPatternId()));
			}
		});
		iHeader.setEnabled("next", false);
		iHeader.addButton("delete", COURSE.actionDeleteTimePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(COURSE.confirmTimeDatePattern(), new Command() {
					@Override
					public void execute() {
						deletePattern();
					}
				});
			}
		});
		iHeader.setEnabled("delete", false);
		iHeader.addButton("back", COURSE.actionBackToTimePatterns(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(null, false);
				showPatterns(iPattern == null ? null : iPattern.getPatternId());
			}
		});
		iHeader.setEnabled("back", false);
		iFooter = iHeader.clonePanel();
		
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
	
	protected void showPatterns() {
		showPatterns(null);
	}
	
	protected void showPatterns(final Long patternId) {
		UniTimePageLabel.getInstance().setPageName(MSG.pageTimePatterns());
		iPanel.clear();
		iListHeader.setEnabled("add", false);
		iListHeader.setEnabled("csv", false);
		iListHeader.setEnabled("pdf", false);
		iListHeader.setEnabled("assign", false);
		iListHeader.setEnabled("exact", false);
		iPanel.addHeaderRow(iListHeader);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new TimePatternsRequest(), new AsyncCallback<TimePatternsResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(TimePatternsResponse result) {
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
				iListHeader.setEnabled("exact", result.getTable().hasLines());
			}
		});		
	}
	
	private TextBox iName;
	private ListBox iType, iDepartments;
	private CheckBox iVisible;
	private NumberBox iNbrMtgs, iMinPerMtg, iSlotsPerMtg, iBreak;
	private int iDepartmentsRow = -1, iExampleRow = -1;
	private DepartmentsTable iDepartmentsTable;
	private TimePreferenceWidget iTimePref;
	private UniTimeWidget<TextArea> iDays, iStarts;
	
	protected void editPattern(Long patternId) {
		Window.scrollTo(0, 0);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new TimePatternEditRequest(patternId == null ? Operation.ADD : Operation.EDIT, patternId), new AsyncCallback<TimePatternEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToLoadData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(TimePatternEditResponse result) {
				LoadingWidget.getInstance().hide();
				UniTimePageLabel.getInstance().setPageName(result.getPatternId() == null ? MSG.pageAddDatePattern() : MSG.pageEditDatePattern());
				iPattern = result.getPattern();
				if (iPattern == null)
					iPattern = new TimePatternInterface();
				iHeader.setEnabled("save", result.getPatternId() == null);
				iHeader.setEnabled("update", result.getPatternId() != null);
				iHeader.setEnabled("delete", result.isCanDelete() && result.getPatternId() != null);
				iHeader.setEnabled("previous", result.getPatternId() != null && getPrevId(result.getPatternId()) != null);
				iHeader.setEnabled("next", result.getPatternId() != null && getNextId(result.getPatternId()) != null);
				iHeader.setEnabled("back", true);
				
				iHeader.setHeaderTitle(iPattern.getPatternId() == null ? COURSE.sectAddTimePattern() : COURSE.sectEditTimePattern());
				iPanel.clear();
				iHeader.clearMessage();
				iPanel.addHeaderRow(iHeader);
				
				iName = new TextBox();
				iName.setWidth("350px"); iName.setMaxLength(50);
				if (iPattern.hasName()) iName.setText(iPattern.getName());
				iPanel.addRow(COURSE.propTimePatternName(), iName);
				iName.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iPattern.setName(event.getValue());
					}
				});
				
				iType = new ListBox();
				iType.addItem(COURSE.timePatterTypeStandard());
				iType.addItem(COURSE.timePatterTypeEvening());
				iType.addItem(COURSE.timePatterTypeSaturday());
				iType.addItem(COURSE.timePatterTypeMorning());
				iType.addItem(COURSE.timePatterTypeExtended());
				if (iPattern.isCanEdit() || iPattern.getType() == Type.ExactTime)
					iType.addItem(COURSE.timePatterTypeExactTime());
				if (iPattern.getType() == null) iPattern.setType(Type.Standard);
				if (!iPattern.isCanEdit() && iPattern.getType() == Type.ExactTime)
					iType.setEnabled(false);
				iType.setSelectedIndex(iPattern.getType().ordinal());
				iType.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						iPattern.setType(Type.values()[iType.getSelectedIndex()]);
						if (iDepartmentsRow >= 0) {
							iPanel.getRowFormatter().setVisible(iDepartmentsRow, iPattern.getType() == Type.Extended || iPattern.getType() == Type.ExactTime);
							iPanel.getRowFormatter().setVisible(iDepartmentsRow + 1, iPattern.getType() == Type.Extended || iPattern.getType() == Type.ExactTime);
						}
					}
				});

				P typeLine = new P("pattern-type");
				if (iPattern.isCanEdit()) {
					iNbrMtgs = new NumberBox();
					iNbrMtgs.setMaxLength(1); iNbrMtgs.setWidth("20px");
					iNbrMtgs.setNegative(false); iNbrMtgs.setDecimal(false);
					iNbrMtgs.setValue(iPattern.getNbrMtgs());
					iNbrMtgs.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							iPattern.setNbrMtgs(iNbrMtgs.toInteger());
							patternDefinitionChanged();
						}
					});
					iMinPerMtg = new NumberBox();
					iMinPerMtg.setMaxLength(3); iMinPerMtg.setWidth("40px");
					iMinPerMtg.setNegative(false); iMinPerMtg.setDecimal(false);
					iMinPerMtg.setValue(iPattern.getMinPerMtg());
					iMinPerMtg.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							iPattern.setMinPerMtg(iMinPerMtg.toInteger());
							if (iPattern.getMinPerMtg() != null)
								iSlotsPerMtg.setValue(String.valueOf((4 + iPattern.getMinPerMtg() + (iPattern.getBreakTime() == null ? 0 : iPattern.getBreakTime().intValue())) / 5), true);
							patternDefinitionChanged();
						}
					});
					typeLine.add(iNbrMtgs);
					typeLine.add(new HTML("&nbsp;&times;&nbsp;"));
					typeLine.add(iMinPerMtg);
					typeLine.add(iType);
				} else {
					typeLine.add(new HTML(iPattern.getNbrMtgs() + " &times; " + iPattern.getMinPerMtg()));
					typeLine.add(iType);
					iNbrMtgs = null;
					iMinPerMtg = null;
				}
				iPanel.addRow(COURSE.propTimePatternType(), typeLine);
				
				iVisible = new CheckBox();
				iVisible.setValue(iPattern.isVisible());
				iVisible.setEnabled(iPattern.getPatternId() != null);
				iPanel.addRow(COURSE.propTimePatternVisible(), iVisible);
				iVisible.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						iPattern.setVisible(event.getValue());
					}
				});
				
				if (iPattern.isCanEdit()) {
					iSlotsPerMtg = new NumberBox();
					iSlotsPerMtg.setMaxLength(3); iSlotsPerMtg.setWidth("50px");
					iSlotsPerMtg.setNegative(false); iSlotsPerMtg.setDecimal(false);
					iSlotsPerMtg.setValue(iPattern.getSlotsPerMtg());
					iSlotsPerMtg.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							iPattern.setSlotsPerMtg(iSlotsPerMtg.toInteger());
							patternDefinitionChanged();
						}
					});
					P slots = new P("slots-per-mtg");
					Label slotHint = new Label(COURSE.hintTimePatternSlotsPerMeeting()); slotHint.addStyleName("slots-per-mtg-hint");
					slots.add(iSlotsPerMtg); slots.add(slotHint);
					iPanel.addRow(COURSE.propTimePatternSlotsPerMeeting(), slots);					
				} else {
					iSlotsPerMtg = null;
					P slots = new P("slots-per-mtg");
					Label slotLabel = new Label(iPattern.getSlotsPerMtg() == null ? "" : iPattern.getSlotsPerMtg().toString());
					slotLabel.addStyleName("slots-per-mtg-number");
					Label slotHint = new Label(COURSE.hintTimePatternSlotsPerMeeting()); slotHint.addStyleName("slots-per-mtg-hint");
					slots.add(slotLabel); slots.add(slotHint);
					iPanel.addRow(COURSE.propTimePatternSlotsPerMeeting(), slots);					
				}
				
				iBreak = new NumberBox();
				iBreak.setMaxLength(3); iBreak.setWidth("50px");
				iBreak.setNegative(false); iBreak.setDecimal(false);
				iBreak.setValue(iPattern.getBreakTime());
				iBreak.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iPattern.setBreakTime(iBreak.toInteger());
						patternDefinitionChanged();
					}
				});
				iPanel.addRow(COURSE.propTimePatternBreakTime(), iBreak);
				
				if (iPattern.isCanEdit()) {
					TextArea days = new TextArea();
					days.setStyleName("unitime-TextArea");
					days.setVisibleLines(7);
					days.setCharacterWidth(10);
					if (iPattern.hasDays()) {
						String str = "";
						for (Integer d: iPattern.getDays()) {
							str += (str.isEmpty() ? "" : "\n") + dayCodesToStr(daysIntToCodes(d));
						}
						days.setText(str);
					}
					iDays = new UniTimeWidget<TextArea>(days);
					days.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							String error = populateDays(event.getValue());
							if (error == null)
								iDays.clearHint();
							else
								iDays.setErrorHint(error);
							patternDefinitionChanged();
						}
					});
					iPanel.addRow(COURSE.propTimePatternDays(), iDays);
					TextArea starts = new TextArea();
					starts.setStyleName("unitime-TextArea");
					starts.setVisibleLines(7);
					starts.setCharacterWidth(10);
					if (iPattern.hasStarts()) {
						String str = "";
						for (Integer s: iPattern.getStarts()) {
							str += (str.isEmpty() ? "" : "\n") + slot2time(s);
						}
						starts.setText(str);
					}
					iStarts = new UniTimeWidget<TextArea>(starts);
					starts.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							String error = populateStarts(event.getValue());
							if (error == null)
								iStarts.clearHint();
							else
								iStarts.setErrorHint(error);
							patternDefinitionChanged();
						}
					});
					iPanel.addRow(COURSE.propTimePatternStartTimes(), iStarts);
				} else {
					if (iPattern.hasDays()) {
						P days = new P("days");
						for (Integer d: iPattern.getDays()) {
							if (days.getWidgetCount() > 0)
								days.add(new Label(", "));
							days.add(new Label((dayCodesToStr(daysIntToCodes(d)))));
						}
						iPanel.addRow(COURSE.propTimePatternDays(), days);
					}
					if (iPattern.hasStarts()) {
						P starts = new P("starts");
						for (Integer s: iPattern.getStarts()) {
							if (starts.getWidgetCount() > 0) starts.add(new Label(", "));
							starts.add(new Label(slot2time(s)));
						}
						iPanel.addRow(COURSE.propTimePatternStartTimes(), starts);
					}
					iDays = null;
					iStarts = null;
				}
				
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
					iPanel.getRowFormatter().setVisible(iDepartmentsRow, iPattern.getType() == Type.Extended || iPattern.getType() == Type.ExactTime);
					iPanel.getRowFormatter().setVisible(iDepartmentsRow + 1, iPattern.getType() == Type.Extended || iPattern.getType() == Type.ExactTime);
				}
				
				iTimePref = new TimePreferenceWidget(false, new ArrayList<>(), true);
				iExampleRow = iPanel.addRow(COURSE.propTimePatternExample(), iTimePref);
				iPanel.getRowFormatter().setVisible(iExampleRow, false);
				
				iPanel.addBottomRow(iFooter);
				
				patternDefinitionChanged();
			}
		});
	}
	
	protected void patternDefinitionChanged() {
		if (iPattern.hasDays() && iPattern.hasStarts() && iPattern.getSlotsPerMtg() != null && iPattern.getType() != Type.ExactTime) {
			TimePatternModel model = new TimePatternModel();
			model.setName(iPattern.getName());
			for (Integer start: iPattern.getStarts())
				model.addTime(start);
			for (Integer days: iPattern.getDays())
				model.addDays(days);
			model.setDayOffset(iPattern.getFirstDayOfWeek());
			Collections.sort(model.getDays(), new Comparator<Integer>() {
				@Override
				public int compare(Integer d1, Integer d2) {
					if (model.getDayOffset() == 0) {
						return -d1.compareTo(d2);
					} else {
						for (int i = 0; i < DayCode.values().length; i++) {
							int idx = (i + model.getDayOffset()) % 7;
							boolean a = (d1 & DayCode.values()[idx].getCode()) != 0;
							boolean b = (d2 & DayCode.values()[idx].getCode()) != 0;
							if (a != b)
								return (a ? -1 : 1);
						}
						return 0;
					}
				}
			});
			model.setLength(5 * iPattern.getSlotsPerMtg() - (iPattern.getBreakTime() == null ? 0 : iPattern.getBreakTime().intValue()));
			iTimePref.setModel(model);
			iPanel.getRowFormatter().setVisible(iExampleRow, true);
		} else {
			iPanel.getRowFormatter().setVisible(iExampleRow, false);
		}
	}
	
	private String populateDays(String dayCodes) {
		if (iPattern.hasDays()) iPattern.getDays().clear();
		String error = null;
		for (String token: dayCodes.split("[\n, ]")) {
			if (token.trim().isEmpty()) continue;
			int dayCode = getDayCode(token.trim(), 0, 0);
			if (dayCode < 0) {
				if (error == null) error = COURSE.errorInvalidDaysForToken(token);
			} else if (iPattern.getNbrMtgs() != null && iPattern.getNbrMtgs() != daysIntToCodes(dayCode).size()) {
				if (error == null) error = COURSE.errorWrongNumberOfDaysForToken(token);
			} else if (iPattern.hasDay(dayCode)) {
				if (error == null) error = COURSE.errorDuplicateDaysToken(token);
			} else {
				iPattern.addDay(dayCode);
			}
		}
		return error;
	}
	
	private String populateStarts(String startTimes) {
		if (iPattern.hasStarts()) iPattern.getStarts().clear();
		String error = null;
		for (String token: startTimes.split("[\n, ]")) {
			if (token.trim().isEmpty()) continue;
			try {
				int slot = getSlot(token.trim());
				if (slot < 0) {
					if (error == null) error = COURSE.errorNotValidTimeForToken(token);
				} else if (iPattern.hasStart(slot)) {
					if (error == null) error = COURSE.errorDiplicateTimeToken(token);
				} else {
					iPattern.addStart(slot);
				}
			} catch (Exception e) {
				if (error == null) error = e.getMessage();
			}
		}
		return error;
	}
	
	private List<DayCode> daysIntToCodes(int dayCode) {
		List<DayCode> codes = new ArrayList<DayCode>();
		for (DayCode dc: DayCode.values())
			if ((dc.getCode() & dayCode) != 0)
				codes.add(dc);
		return codes;
	}
	
	private String dayCodesToStr(List<DayCode> dc) {
		if (dc.size() == 1) {
			return CONST.days()[dc.get(0).ordinal()];
		}
		String ret = "";
		for (DayCode c: dc)
			ret += CONST.shortDays()[c.ordinal()];
		return ret;
	}
	
	private String slot2time(int startSlot) {
		int min = startSlot * 5;
        int h = min / 60;
        int m = min % 60;
        return h + (m < 10 ? "0" : "") + m;
	}
	
	private int getDayCode(String token, int day, int dayCode) {
		if (day==DayCode.values().length) {
			if (token.length()==0) return dayCode;
			else return -1;
		}
		if (token.startsWith(CONST.shortDays()[day])) {
			int code = getDayCode(token.substring(CONST.shortDays()[day].length()),day+1,dayCode + DayCode.values()[day].getCode());
			if (code>=0) return code;
		}
		if (token.startsWith(CONST.days()[day])) {
			int code = getDayCode(token.substring(CONST.days()[day].length()),day+1,dayCode + DayCode.values()[day].getCode());
			if (code>=0) return code;
		}
		return getDayCode(token, day+1, dayCode);
	}
	
	private int getSlot(String token) throws Exception {
		try {
			int time = Integer.parseInt(token.trim());
			int hour = time/100;
			int min = time%100;
			if (hour>=24)
				throw new Exception(COURSE.errorWrongHoursForTimeToken(token, hour));
			if (min>=60)
				throw new Exception(COURSE.errorWrongMinutesForTimeToken(token, min));
			if ((min%5)!=0)
				throw new Exception(COURSE.errorMinutesNotDivisibleByFiveForTimeToken(token, min));
			if (iPattern.getSlotsPerMtg() != null) {
				int endTime = hour * 60 + min + (5 * iPattern.getSlotsPerMtg());
				if (endTime > 24*60)
					throw new Exception(COURSE.errorTimeGoesOverMidnightForToken(token));
			}
			return (hour*60 + min)/5;
		} catch (NumberFormatException ex) {
			throw new Exception(COURSE.errorTimeNotNumberForToken(token));
		}
	}
	
	protected void saveOrUpdatePattern(final Long nextPatternId) {
		if (validatePattern()) {
			RPC.execute(new TimePatternEditRequest(Operation.SAVE, iPattern), new AsyncCallback<TimePatternEditResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(TimePatternEditResponse result) {
					if (nextPatternId != null) {
						History.newItem(nextPatternId.toString(), false);
						editPattern(nextPatternId);
					} else {
						History.newItem(null, false);
						showPatterns(result.getPatternId());
					}
				}
			});
		}
	}
	
	protected boolean validatePattern() {
		List<String> errors = new ArrayList<String>();
		if (!iPattern.hasName())
			errors.add(COURSE.errorRequiredField(COURSE.columnTimePatternName()));
		if (iPattern.getType() == null)
			errors.add(COURSE.errorRequiredField(COURSE.columnTimePatternType()));
		if (iPattern.hasDepartmentIds() && iPattern.getType() != Type.Extended && iPattern.getType() != Type.ExactTime)
			errors.add(COURSE.errorOnyExtDatePatternsHaveDepartments());
		if (iPattern.getType() != Type.ExactTime) {
			if (iPattern.getNbrMtgs() == null || iPattern.getNbrMtgs() <= 0)
				errors.add(COURSE.errorNumberOfMeetingsPerWeekRequired());
			if (iPattern.getMinPerMtg() == null || iPattern.getMinPerMtg() <= 0)
				errors.add(COURSE.errorMinutesPerMeetingRequired());
			if (iPattern.getSlotsPerMtg() == null || iPattern.getSlotsPerMtg() <= 0)
				errors.add(COURSE.errorNumberOfSlotsPerMeetingRequired());
			if (iDays != null) {
				String error = populateDays(iDays.getWidget().getText());
				if (error != null) errors.add(error);
				if (error == null)
					iDays.clearHint();
				else
					iDays.setErrorHint(error);
			}
			if (!iPattern.hasDays())
				errors.add(COURSE.errorRequiredField(COURSE.columnTimePatternDays()));
			if (iStarts != null) {
				String error = populateStarts(iStarts.getWidget().getText());
				if (error != null) errors.add(error);
				if (error == null)
					iStarts.clearHint();
				else
					iStarts.setErrorHint(error);
			}
			if (!iPattern.hasStarts())
				errors.add(COURSE.errorRequiredField(COURSE.columnTimePatternTimes()));
		} else {
			if (iPattern.getNbrMtgs() == null) iPattern.setNbrMtgs(0);
			if (iPattern.getMinPerMtg() == null) iPattern.setMinPerMtg(0);
			if (iPattern.getSlotsPerMtg() == null) iPattern.setSlotsPerMtg(0);
		}
		if (iPattern.getBreakTime() == null)
			iPattern.setBreakTime(0);
		
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
		RPC.execute(new TimePatternEditRequest(Operation.DELETE, iPattern.getPatternId()), new AsyncCallback<TimePatternEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MSG.failedToDeleteData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToDeleteData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);					
			}

			@Override
			public void onSuccess(TimePatternEditResponse result) {
				History.newItem(null, false);
				showPatterns(null);
			}
		});
	}
	
	protected void exportData(String format) {
		String query = "output=" + format + "&sort=" + iTable.getSortCookie();
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
	
	public static class TimePatternsRequest implements GwtRpcRequest<TimePatternsResponse> {
		private boolean iExport = false;

		public boolean isExport() { return iExport; }
		public void setExport(boolean export) { iExport = export; }
	}
	
	public static class TimePatternsResponse implements GwtRpcResponse {
		private TableInterface iTable;
		private boolean iCanAdd = false;
		
		public TableInterface getTable() { return iTable; }
		public void setTable(TableInterface table) { iTable = table; }
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
	}
	
	public static class TimePatternEditRequest implements GwtRpcRequest<TimePatternEditResponse> {
		private Long iPatternId;
		private TimePatternInterface iPattern;
		private Operation iOperation;
		
		public TimePatternEditRequest() {}
		public TimePatternEditRequest(Operation operation) {
			iOperation = operation;
		}
		public TimePatternEditRequest(Operation operation, Long patternId) {
			iOperation = operation; iPatternId = patternId;
		}
		public TimePatternEditRequest(Operation operation, TimePatternInterface pattern) {
			iOperation = operation;
			iPattern = pattern;
			iPatternId = (pattern == null ? null : pattern.getPatternId());
		}
		
		public Long getPatternId() { return iPatternId; }
		public void setPatternId(Long patternId) { iPatternId = patternId; }
		public TimePatternInterface getPattern() { return iPattern; }
		public void setPattern(TimePatternInterface patterm) { iPattern = patterm; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public static enum Operation {
			ADD, EDIT, SAVE, DELETE, ASSIGN_DEPTS
		}
	}
	
	public static class TimePatternEditResponse implements GwtRpcResponse {
		private byte[] iLog;
		private TimePatternInterface iPattern;
		private List<IdLabel> iDepartments;
		private Long iSessionId;
		private String iSessionName;
		private boolean iCanDelete = false;
		
		public TimePatternInterface getPattern() { return iPattern; }
		public void setPattern(TimePatternInterface patterm) { iPattern = patterm; }
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
		
		public boolean hasLog() { return iLog != null; }
		public byte[] getLog() { return iLog; }
		public void setLog(byte[] log) { iLog = log; }
		
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
	}
	
	public static class TimePatternInterface implements IsSerializable, Comparator<Integer> {
		private Long iPatternId;
		private String iName;
		private Type iType;
		private boolean iVisible = true, iCanEdit = true;
		private Integer iNbrMtgs, iMinPerMtg, iSlotsPerMtg, iBreakTime;
		private Set<Integer> iDays;
		private Set<Integer> iStarts;
		private Set<Long> iDepartmentIds;
		private Integer iFirstDayOfWeek;
		
		public Long getPatternId() { return iPatternId; }
		public void setPatternId(Long patternId) { iPatternId = patternId; }

		public boolean hasName() { return iName != null && !iName.isEmpty(); }
		public String getName() { return iName; }
		public void setName(String firstName) { iName = firstName; }
		public Type getType() { return iType; }
		public void setType(Type type) { iType = type; }
		public Integer getNbrMtgs() { return iNbrMtgs; }
		public void setNbrMtgs(Integer nbrMtgs) { iNbrMtgs = nbrMtgs; }
		public Integer getMinPerMtg() { return iMinPerMtg; }
		public void setMinPerMtg(Integer minPerMtg) { iMinPerMtg = minPerMtg; }
		public Integer getSlotsPerMtg() { return iSlotsPerMtg; }
		public void setSlotsPerMtg(Integer slotPerMtg) { iSlotsPerMtg = slotPerMtg; }
		public Integer getBreakTime() { return iBreakTime; }
		public void setBreakTime(Integer breakTime) { iBreakTime = breakTime; }
		public boolean isVisible() { return iVisible; }
		public void setVisible(boolean visible) { iVisible = visible; }
		public boolean isCanEdit() { return iCanEdit; }
		public void setCanEdit(boolean canEdit) { iCanEdit = canEdit; }
		
		public boolean hasDays() { return iDays != null && !iDays.isEmpty(); }
		public void addDay(Integer day) {
			if (iDays == null) iDays = new TreeSet<Integer>(this);
			iDays.add(day);
		}
		public Set<Integer> getDays() { return iDays; }
		public boolean hasDay(Integer day) {
			return iDays != null && iDays.contains(day);
		}
		
		public boolean hasStarts() { return iStarts != null && !iStarts.isEmpty(); }
		public void addStart(Integer start) {
			if (iStarts == null) iStarts = new TreeSet<Integer>();
			iStarts.add(start);
		}
		public Set<Integer> getStarts() { return iStarts; }
		public boolean hasStart(Integer start) {
			return iStarts != null && iStarts.contains(start);
		}
		
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
		
		public static enum Type {
			Standard,
	    	Evening,
	    	Saturday,
	    	Morning,
	    	Extended,
	    	ExactTime,
		};
		
		public void setFirstDayOfWeek(Integer firstDayOfWeek) { iFirstDayOfWeek = firstDayOfWeek; }
		public int getFirstDayOfWeek() { return iFirstDayOfWeek == null ? 0 : iFirstDayOfWeek.intValue(); }
		
		@Override
		public int compare(Integer d1, Integer d2) {
			if (getFirstDayOfWeek() == 0) {
				return -d1.compareTo(d2);
			} else {
				for (int i = 0; i < DayCode.values().length; i++) {
					int idx = (i + getFirstDayOfWeek()) % 7;
					boolean a = (d1 & DayCode.values()[idx].getCode()) != 0;
					boolean b = (d2 & DayCode.values()[idx].getCode()) != 0;
					if (a != b)
						return (a ? -1 : 1);
				}
				return 0;
			}
		}
	}
}
