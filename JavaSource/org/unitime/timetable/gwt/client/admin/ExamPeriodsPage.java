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

import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.IdLabel;
import org.unitime.timetable.gwt.client.admin.ExamPeriodsPage.ExamPeriodEditRequest.Operation;
import org.unitime.timetable.gwt.client.events.SessionDatesSelector;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class ExamPeriodsPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static ExaminationMessages EXAM = GWT.create(ExaminationMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iListHeader, iListFooter;
	private TableWidget iTable;
	private UniTimeHeaderPanel iHeader, iFooter;
	private ExamPeriodInterface iPeriod;
	private ExamPeriodSetupInterface iSetup;
	
	public ExamPeriodsPage() {
		iPanel = new SimpleForm();
		initWidget(iPanel);
		iPanel.addStyleName("unitime-ExamPeriodsPage");
		iListHeader = new UniTimeHeaderPanel();
		iListHeader.addButton("add", EXAM.actionAddExaminationPeriod(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("add", false);
				editPeriod(null);
			}
		});
		iListHeader.setEnabled("add", false);
		iListHeader.getButton("add").setTitle(EXAM.titleAddExaminationPeriod());
		
		iListHeader.addButton("pdf", EXAM.actionExportPdf(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				exportData("pdf");
			}
		});
		iListHeader.getButton("pdf").setAccessKey(EXAM.accessExportPdf().charAt(0));
		iListHeader.getButton("pdf").setTitle(EXAM.titleExportPdf(EXAM.accessExportPdf()));
		iListHeader.setEnabled("pdf", false);
		iListHeader.addButton("csv", EXAM.actionExportCsv(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				exportData("csv");
			}
		});
		iListHeader.getButton("csv").setAccessKey(EXAM.accessExportCsv().charAt(0));
		iListHeader.getButton("csv").setTitle(EXAM.titleExportCsv(EXAM.accessExportCsv()));
		iListHeader.setEnabled("csv", false);
		
		
		iListFooter = iListHeader.clonePanel();
		iTable = new TableWidget();
		iTable.addStyleName("table");
		
		iHeader = new UniTimeHeaderPanel("");
		iHeader.addButton("save", EXAM.actionSaveExaminationPeriod(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iPeriod != null)
					saveOrUpdatePeriod(null);
				else
					saveSetup();
			}
		});
		iHeader.setEnabled("save", false);
		iHeader.addButton("update", EXAM.actionUpdateExaminationPeriod(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iPeriod != null)
					saveOrUpdatePeriod(null);
				else
					saveSetup();
			}
		});
		iHeader.setEnabled("update", false);
		iHeader.addButton("previous", EXAM.actionExamPrevious(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdatePeriod(getPrevId(iPeriod.getPeriodId()));
			}
		});
		iHeader.setEnabled("previous", false);
		iHeader.addButton("next", EXAM.actionExamNext(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdatePeriod(getNextId(iPeriod.getPeriodId()));
			}
		});
		iHeader.setEnabled("next", false);
		iHeader.addButton("delete", EXAM.actionDeleteExaminationPeriod(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(EXAM.confirmDeleteExamPerid(), new Command() {
					@Override
					public void execute() {
						deletePeriod();
					}
				});
			}
		});
		iHeader.setEnabled("delete", false);
		iHeader.addButton("back", EXAM.actionBackToExaminationPeriods(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(null, false);
				showPeriods(iPeriod == null ? null : iPeriod.getPeriodId());
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
			showPeriods();
		else if ("add".equals(token))
			editPeriod(null);
		else {
			try {
				Long id = Long.valueOf(token);
				if (id >= 0)
					editPeriod(id);
				else
					setupPeriods(-id);
			} catch (NumberFormatException e) {
				showPeriods();
			}
		}
	}
	
	protected void showPeriods() {
		showPeriods(null);
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

	protected void showPeriods(final Long patternId) {
		UniTimePageLabel.getInstance().setPageName(MSG.pageExaminationPeriods());
		iPanel.clear();
		iListHeader.setEnabled("add", false);
		iListHeader.setEnabled("csv", false);
		iListHeader.setEnabled("pdf", false);
		for (String op: iListHeader.getOperations())
			if (op.startsWith("setup-")) iListFooter.setEnabled(op, false);
		iPanel.addHeaderRow(iListHeader);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new ExamPeriodsRequest(), new AsyncCallback<ExamPeriodsResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ExamPeriodsResponse result) {
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
				
				if (result.hasCanSetupPeriodTypes()) {
					for (final IdLabel item: result.getCanSetupPeriodTypes()) {
						String op = "setup-" + item.getId();
						if (iListHeader.getOperations().contains(op)) {
							iListHeader.setEnabled(op, true);
						} else {
							iListHeader.addButton(op, EXAM.actionSetupExaminationPeriods(item.getLabel()), new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									setupPeriods(item.getId());
								}
							});
							iListHeader.getButton(op).setTitle(EXAM.titleSetupExaminationPeriods(item.getLabel()));
						}
					}
				}
			}
		});		
	}
	
	private ListBox iExamType, iPreference;
	private SingleDateSelector iDate;
	private TimeSelector iStartSlot;
	private NumberBox iLength, iStartOffset, iStopOffset;
	
	protected void editPeriod(final Long periodId) {
		Window.scrollTo(0, 0);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new ExamPeriodEditRequest(periodId == null ? Operation.ADD : Operation.EDIT, periodId), new AsyncCallback<ExamPeriodEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToLoadData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final ExamPeriodEditResponse result) {
				LoadingWidget.getInstance().hide();
				UniTimePageLabel.getInstance().setPageName(result.getPeriodId() == null ? MSG.pageAddExaminationPeriod() : MSG.pageEditExaminationPeriod());
				iPeriod = result.getPeriod();
				iSetup = null;
				if (iPeriod == null)
					iPeriod = new ExamPeriodInterface();
				iHeader.setEnabled("save", result.getPeriodId() == null);
				iHeader.setEnabled("update", result.getPeriodId() != null);
				iHeader.setEnabled("delete", result.isCanDelete() && result.getPeriodId() != null);
				iHeader.setEnabled("previous", result.getPeriodId() != null && getPrevId(result.getPeriodId()) != null);
				iHeader.setEnabled("next", result.getPeriodId() != null && getNextId(result.getPeriodId()) != null);
				iHeader.setEnabled("back", true);
				
				iHeader.setHeaderTitle(iPeriod.getPeriodId() == null ? EXAM.sectAddExaminationPeriod() : EXAM.sectEditExaminationPeriod());
				iPanel.clear();
				iHeader.clearMessage();
				iPanel.addHeaderRow(iHeader);
				
				if (iPeriod.isCanEdit() && result.hasExamTypes()) {
					iExamType = new ListBox();
					iExamType.addItem(EXAM.itemSelect(), "");
					for (IdLabel item: result.getExamTypes()) {
						iExamType.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(iPeriod.getExamTypeId()))
							iExamType.setSelectedIndex(iExamType.getItemCount() - 1);
					}
					iExamType.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							if (iExamType.getSelectedIndex() <= 0)
								iPeriod.setExamTypeId(null);
							else
								iPeriod.setExamTypeId(Long.valueOf(iExamType.getSelectedValue()));
						}
					});
					iPanel.addRow(EXAM.propExamType(), iExamType);
				} else {
					IdLabel item = result.getExamType(iPeriod.getExamTypeId());
					if (item != null)
						iPanel.addRow(EXAM.propExamType(), new Label(item.getLabel()));	
				}
				iDate = new SingleDateSelector(new AcademicSessionProvider() {
					@Override
					public void selectSession(Long sessionId, AsyncCallback<Boolean> callback) {}
					@Override
					public String getAcademicSessionName() { return result.getSessionName(); }
					@Override
					public AcademicSessionInfo getAcademicSessionInfo() { return null; }
					@Override
					public Long getAcademicSessionId() { return result.getSessionId(); }
					@Override
					public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {}
				});
				iDate.setValue(iPeriod.getDate());
				iDate.setEnabled(iPeriod.isCanEdit());
				iDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
					@Override
					public void onValueChange(ValueChangeEvent<Date> event) {
						iPeriod.setDate(event.getValue());
					}
				});
				iPanel.addRow(EXAM.propertyPeriodDate(), iDate);
				
				iStartSlot = new TimeSelector();
				iStartSlot.setValue(iPeriod.getStartSlot());
				iStartSlot.setEnabled(iPeriod.isCanEdit());
				iStartSlot.addValueChangeHandler(new ValueChangeHandler<Integer>() {
					@Override
					public void onValueChange(ValueChangeEvent<Integer> event) {
						iPeriod.setStartSlot(event.getValue());
					}
				});
				iPanel.addRow(EXAM.propPeriodStartTime(), iStartSlot);
				
				iLength = new NumberBox();
				iLength.setMaxLength(4); iLength.setWidth("40px");
				iLength.setDecimal(false); iLength.setNegative(false);
				iLength.setValue(iPeriod.getLength());
				iLength.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iPeriod.setLength(iLength.toInteger());
					}
				});
				iLength.setEnabled(iPeriod.isCanEdit());
				iPanel.addRow(EXAM.propPeriodLength(), withMinutes(iLength));
				
				iStartOffset = new NumberBox();
				iStartOffset.setMaxLength(4); iStartOffset.setWidth("40px");
				iStartOffset.setDecimal(false); iStartOffset.setNegative(false);
				iStartOffset.setValue(iPeriod.getStartOffset());
				iStartOffset.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iPeriod.setStartOffset(iStartOffset.toInteger());
					}
				});
				iStartOffset.setEnabled(iPeriod.isCanEdit());
				iPanel.addRow(EXAM.propEventStartOffset(), withMinutes(iStartOffset));
				
				iStopOffset = new NumberBox();
				iStopOffset.setMaxLength(4); iStopOffset.setWidth("40px");
				iStopOffset.setDecimal(false); iStopOffset.setNegative(false);
				iStopOffset.setValue(iPeriod.getStopOffset());
				iStopOffset.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iPeriod.setStopOffset(iStopOffset.toInteger());
					}
				});
				iStopOffset.setEnabled(iPeriod.isCanEdit());
				iPanel.addRow(EXAM.propEventStopOffset(), withMinutes(iStopOffset));
				
				if (result.hasPreferences()) {
					iPreference = new ListBox();
					if (iPeriod.getPreferenceId() == null)
						iPreference.addItem(EXAM.itemSelect(), "");
					for (IdLabel item: result.getPreferences()) {
						iPreference.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(iPeriod.getPreferenceId()))
							iPreference.setSelectedIndex(iPreference.getItemCount() - 1);
					}
					iPreference.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							if (iPreference.getSelectedIndex() < 0 || iPreference.getSelectedValue().isEmpty())
								iPeriod.setPreferenceId(null);
							else
								iPeriod.setPreferenceId(Long.valueOf(iPreference.getSelectedValue()));
						}
					});
					iPanel.addRow(EXAM.propExamType(), iPreference);
				}
				
				iPanel.addBottomRow(iFooter);
			}
			
		});
	}
	
	protected Widget withMinutes(Widget w) {
		P line = new P("line-with-mins");
		line.add(w);
		P mins = new P(DOM.createSpan()); mins.setText(EXAM.noteMinutes());
		line.add(mins);
		return line;
	}
	
	protected void saveOrUpdatePeriod(final Long nextPeriodId) {
		if (validatePeriod()) {
			RPC.execute(new ExamPeriodEditRequest(Operation.SAVE, iPeriod), new AsyncCallback<ExamPeriodEditResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(ExamPeriodEditResponse result) {
					if (nextPeriodId != null) {
						History.newItem(nextPeriodId.toString(), false);
						editPeriod(nextPeriodId);
					} else {
						History.newItem(null, false);
						showPeriods(result.getPeriodId());
					}
				}
			});
		}
	}
	
	protected boolean validatePeriod() {
		List<String> errors = new ArrayList<String>();
		if (iPeriod.getExamTypeId() == null)
			errors.add(EXAM.errorExamTypeIsRequired());
		if (iPeriod.getDate() == null)
			errors.add(EXAM.errorExamDateIsNotValid());
		if (iPeriod.getStartSlot() == null)
			errors.add(EXAM.errorStartTimeIsRequired());
		if (iPeriod.getLength() == null || iPeriod.getLength() <= 0)
			errors.add(EXAM.errorLengthIsRequired());
		else if (iPeriod.getLength() % 5 != 0)
			errors.add(EXAM.errorInvalidLength5(iPeriod.getLength()));
		if (iPeriod.getStartOffset() != null) {
			if (iPeriod.getStartOffset() < 0)
				errors.add(EXAM.errorInvalidStartOffsetNegative(iPeriod.getStartOffset()));
			else if (iPeriod.getStartOffset() % 5 != 0)
				errors.add(EXAM.errorInvalidStartOffset5(iPeriod.getStartOffset()));
		}
		if (iPeriod.getStopOffset() != null) {
			if (iPeriod.getStopOffset() < 0)
				errors.add(EXAM.errorInvalidStopOffsetNegative(iPeriod.getStartOffset()));
			else if (iPeriod.getStopOffset() % 5 != 0)
				errors.add(EXAM.errorInvalidStopOffset5(iPeriod.getStartOffset()));
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
	
	private TimeSelector[] iSetupStartSlot;
	private NumberBox[] iSetupLength, iSetupStartOffset, iSetupStopOffset;
	private SessionDatesSelector iDates;
	
	protected void setupPeriods(final Long examTypeId) {
		Window.scrollTo(0, 0);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new ExamPeriodEditRequest(Operation.LOAD_SETUP).setExamTypeId(examTypeId), new AsyncCallback<ExamPeriodEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToLoadData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final ExamPeriodEditResponse result) {
				LoadingWidget.getInstance().hide();
				iSetup = result.getSetup();
				if (iSetup == null) {
					showPeriods(null);
					return;
				}
				UniTimePageLabel.getInstance().setPageName(MSG.pageSetupExaminationPeriods());
				History.newItem("-" + examTypeId, false);
				iPeriod = null;
				iHeader.setEnabled("save", !iSetup.hasPattern() || iSetup.getPattern().indexOf('1') < 0);
				iHeader.setEnabled("update", iSetup.hasPattern() && iSetup.getPattern().indexOf('1') >= 0);
				iHeader.setEnabled("delete", false);
				iHeader.setEnabled("previous", false);
				iHeader.setEnabled("next", false);
				iHeader.setEnabled("back", true);
				
				iHeader.setHeaderTitle(EXAM.sectSetupExaminationPeriods());
				iPanel.clear();
				iHeader.clearMessage();
				iPanel.addHeaderRow(iHeader);
				
				IdLabel item = result.getExamType(iSetup.getExamTypeId());
				if (item != null)
					iPanel.addRow(EXAM.propExamType(), new Label(item.getLabel()));
				if (iSetup.hasItems()) {
					iSetupStartSlot = new TimeSelector[5];
					iSetupLength = new NumberBox[5];
					iSetupStartOffset = new NumberBox[5];
					iSetupStopOffset = new NumberBox[5];
					for (int idx = 0; idx < iSetup.countItems(); idx++) {
						final int i = idx;
						final PeriodSetupItemInterface setup = iSetup.getItem(i);
						iSetupStartSlot[i] = new TimeSelector();
						iSetupStartSlot[i].setValue(setup.getStartSlot());
						iSetupStartSlot[i].addValueChangeHandler(new ValueChangeHandler<Integer>() {
							@Override
							public void onValueChange(ValueChangeEvent<Integer> event) {
								setup.setStartSlot(event.getValue());
							}
						});
						iPanel.addRow(
								i == 0 ? EXAM.prop1stPeriodStartTime() :
								i == 1 ? EXAM.prop2ndPeriodStartTime() :
								i == 2 ? EXAM.prop3rdPeriodStartTime() : 
								i == 3 ? EXAM.prop4thPeriodStartTime() :
									EXAM.prop5thPeriodStartTime(), iSetupStartSlot[i]);
						
						iSetupLength[i] = new NumberBox();
						iSetupLength[i].setMaxLength(4); iSetupLength[i].setWidth("40px");
						iSetupLength[i].setDecimal(false); iSetupLength[i].setNegative(false);
						iSetupLength[i].setValue(setup.getLength());
						iSetupLength[i].addValueChangeHandler(new ValueChangeHandler<String>() {
							@Override
							public void onValueChange(ValueChangeEvent<String> event) {
								setup.setLength(iSetupLength[i].toInteger());
							}
						});
						iPanel.addRow(
								i == 0 ? EXAM.prop1stPeriodLength() :
								i == 1 ? EXAM.prop2ndPeriodLength() :
								i == 2 ? EXAM.prop3rdPeriodLength() : 
								i == 3 ? EXAM.prop4thPeriodLength() :
									EXAM.prop5thPeriodLength(), withMinutes(iSetupLength[i]));
						
						iSetupStartOffset[i] = new NumberBox();
						iSetupStartOffset[i].setMaxLength(4); iSetupStartOffset[i].setWidth("40px");
						iSetupStartOffset[i].setDecimal(false); iSetupStartOffset[i].setNegative(false);
						iSetupStartOffset[i].setValue(setup.getStartOffset());
						iSetupStartOffset[i].addValueChangeHandler(new ValueChangeHandler<String>() {
							@Override
							public void onValueChange(ValueChangeEvent<String> event) {
								setup.setStartOffset(iSetupStartOffset[i].toInteger());
							}
						});
						iPanel.addRow(i == 0 ? EXAM.prop1stEventStartOffset() :
							i == 1 ? EXAM.prop2ndEventStartOffset() :
							i == 2 ? EXAM.prop3rdEventStartOffset() : 
							i == 3 ? EXAM.prop4thEventStartOffset() :
								EXAM.prop5thEventStartOffset(), withMinutes(iSetupStartOffset[i]));
						
						iSetupStopOffset[i] = new NumberBox();
						iSetupStopOffset[i].setMaxLength(4); iSetupStopOffset[i].setWidth("40px");
						iSetupStopOffset[i].setDecimal(false); iSetupStopOffset[i].setNegative(false);
						iSetupStopOffset[i].setValue(setup.getStopOffset());
						iSetupStopOffset[i].addValueChangeHandler(new ValueChangeHandler<String>() {
							@Override
							public void onValueChange(ValueChangeEvent<String> event) {
								setup.setStopOffset(iSetupStopOffset[i].toInteger());
							}
						});
						iPanel.addRow(i == 0 ? EXAM.prop1stEventStopOffset() :
							i == 1 ? EXAM.prop2ndEventStopOffset() :
							i == 2 ? EXAM.prop3rdEventStopOffset() : 
							i == 3 ? EXAM.prop4thEventStopOffset() :
								EXAM.prop5thEventStopOffset(), withMinutes(iSetupStopOffset[i]));
					}
				}
				
				iDates = new SessionDatesSelector(iSetup.getMonths());
				if (iSetup.hasPattern())
					iDates.setPattern(iSetup.getPattern());
				iPanel.addRow(EXAM.propExaminationDates(), iDates);
				
				iPanel.addBottomRow(iFooter);
			}
			
		});
	}
	
	protected void saveSetup() {
		if (validateSetup()) {
			RPC.execute(new ExamPeriodEditRequest(Operation.SAVE_SETUP, iSetup), new AsyncCallback<ExamPeriodEditResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(ExamPeriodEditResponse result) {
					History.newItem(null, false);
					showPeriods(null);
				}
			});
		}
	}
	
	protected boolean validateSetup() {
		iSetup.setPattern(iDates.getPattern());
		List<String> errors = new ArrayList<String>();
		for (int i = 0; i < iSetup.countItems(); i++) {
			String prefix = (i == 0 ? EXAM.prop1stPeriod() :
				i == 1 ? EXAM.prop2ndPeriod() :
				i == 2 ? EXAM.prop3rdPeriod() :
				i == 3 ? EXAM.prop4thPeriod() : EXAM.prop5thPeriod());
			final PeriodSetupItemInterface setup = iSetup.getItem(i);
			if (setup.getStartSlot() == null && i > 0) continue;
			if (setup.getStartSlot() == null)
				errors.add(prefix + " " + EXAM.errorStartTimeIsRequired());
			if (setup.getLength() == null || setup.getLength() <= 0)
				errors.add(prefix + " " + EXAM.errorLengthIsRequired());
			else if (setup.getLength() % 5 != 0)
				errors.add(prefix + " " + EXAM.errorInvalidLength5(setup.getLength()));
			if (setup.getStartOffset() != null) {
				if (setup.getStartOffset() < 0)
					errors.add(prefix + " " + EXAM.errorInvalidStartOffsetNegative(setup.getStartOffset()));
				else if (setup.getStartOffset() % 5 != 0)
					errors.add(prefix + " " + EXAM.errorInvalidStartOffset5(setup.getStartOffset()));
			}
			if (setup.getStopOffset() != null) {
				if (setup.getStopOffset() < 0)
					errors.add(prefix + " " + EXAM.errorInvalidStopOffsetNegative(setup.getStartOffset()));
				else if (setup.getStopOffset() % 5 != 0)
					errors.add(prefix + " " + EXAM.errorInvalidStopOffset5(setup.getStartOffset()));
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
	
	protected void deletePeriod() {
		RPC.execute(new ExamPeriodEditRequest(Operation.DELETE, iPeriod.getPeriodId()), new AsyncCallback<ExamPeriodEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MSG.failedToDeleteData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToDeleteData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);					
			}

			@Override
			public void onSuccess(ExamPeriodEditResponse result) {
				History.newItem(null, false);
				showPeriods(null);
			}
		});
	}
	
	protected void exportData(String format) {
		String query = "output=exam-periods." + format + "&sort=" + iTable.getSortCookie();
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
	
	public static class ExamPeriodsRequest implements GwtRpcRequest<ExamPeriodsResponse> {
		private boolean iExport = false;

		public boolean isExport() { return iExport; }
		public void setExport(boolean export) { iExport = export; }
	}
	
	public static class ExamPeriodsResponse implements GwtRpcResponse {
		private TableInterface iTable;
		private boolean iCanAdd = false;
		private List<IdLabel> iCanSetupPeriodTypes = null;
		
		public TableInterface getTable() { return iTable; }
		public void setTable(TableInterface table) { iTable = table; }
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
		
		public void addCanSetupPeriodType(Long id, String label) {
			if (iCanSetupPeriodTypes == null) iCanSetupPeriodTypes = new ArrayList<IdLabel>();
			iCanSetupPeriodTypes.add(new IdLabel(id, label));
		}
		public List<IdLabel> getCanSetupPeriodTypes() { return iCanSetupPeriodTypes; }
		public boolean hasCanSetupPeriodTypes() { return iCanSetupPeriodTypes != null && !iCanSetupPeriodTypes.isEmpty(); }
		public IdLabel getCanSetupPeriodType(Long id) {
			if (iCanSetupPeriodTypes == null || id == null) return null;
			for (IdLabel item: iCanSetupPeriodTypes)
				if (id.equals(item.getId())) return item;
			return null;
		}
	}
	
	public static class ExamPeriodEditRequest implements GwtRpcRequest<ExamPeriodEditResponse> {
		private Long iPeriodId;
		private ExamPeriodInterface iPeriod;
		private Operation iOperation;
		private Long iExamTypeId;
		private ExamPeriodSetupInterface iSetup;
		
		public ExamPeriodEditRequest() {}
		public ExamPeriodEditRequest(Operation operation) {
			iOperation = operation;
		}
		public ExamPeriodEditRequest(Operation operation, Long PeriodId) {
			iOperation = operation; iPeriodId = PeriodId;
		}
		public ExamPeriodEditRequest(Operation operation, ExamPeriodInterface period) {
			iOperation = operation;
			iPeriod = period;
			iPeriodId = (period == null ? null : period.getPeriodId());
		}
		public ExamPeriodEditRequest(Operation operation, ExamPeriodSetupInterface setup) {
			iOperation = operation;
			iExamTypeId = (setup == null ? null : setup.getExamTypeId());
			iSetup = setup;
		}
		
		public Long getPeriodId() { return iPeriodId; }
		public void setPeriodId(Long PeriodId) { iPeriodId = PeriodId; }
		public ExamPeriodInterface getPeriod() { return iPeriod; }
		public void setPeriod(ExamPeriodInterface period) { iPeriod = period; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public Long getExamTypeId() { return iExamTypeId; }
		public ExamPeriodEditRequest setExamTypeId(Long examTypeId) { iExamTypeId = examTypeId; return this; }
		public ExamPeriodSetupInterface getSetup() { return iSetup; }
		public void setSetup(ExamPeriodSetupInterface setup) { iSetup = setup; }
		
		public static enum Operation {
			ADD, EDIT, SAVE, DELETE, LOAD_SETUP, SAVE_SETUP
		}
	}
	
	public static class ExamPeriodEditResponse implements GwtRpcResponse {
		private ExamPeriodInterface iPeriod;
		private List<IdLabel> iExamTypes, iPreferences;
		private Long iSessionId;
		private String iSessionName;
		private boolean iCanDelete = false;
		private ExamPeriodSetupInterface iSetup;
		
		public ExamPeriodInterface getPeriod() { return iPeriod; }
		public void setPeriod(ExamPeriodInterface period) { iPeriod = period; }
		public Long getPeriodId() { return iPeriod == null ? null : iPeriod.getPeriodId(); }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public String getSessionName() { return iSessionName; }
		public void setSessionName(String name) { iSessionName = name; }
		public boolean hasSessionName() { return iSessionName != null && !iSessionName.isEmpty(); }
		
		public void addExamType(Long id, String label) {
			if (iExamTypes == null) iExamTypes = new ArrayList<IdLabel>();
			iExamTypes.add(new IdLabel(id, label));
		}
		public List<IdLabel> getExamTypes() { return iExamTypes; }
		public boolean hasExamTypes() { return iExamTypes != null && !iExamTypes.isEmpty(); }
		public IdLabel getExamType(Long id) {
			if (iExamTypes == null || id == null) return null;
			for (IdLabel item: iExamTypes)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public void addPreference(Long id, String label) {
			if (iPreferences == null) iPreferences = new ArrayList<IdLabel>();
			iPreferences.add(new IdLabel(id, label));
		}
		public List<IdLabel> getPreferences() { return iPreferences; }
		public boolean hasPreferences() { return iPreferences != null && !iPreferences.isEmpty(); }
		public IdLabel getPreference(Long id) {
			if (iPreferences == null || id == null) return null;
			for (IdLabel item: iPreferences)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
		
		public ExamPeriodSetupInterface getSetup() { return iSetup; }
		public void setSetup(ExamPeriodSetupInterface setup) { iSetup = setup; }
	}
	
	public static class ExamPeriodSetupInterface implements IsSerializable {
		private Long iExamTypeId;
		private Long iSessionId;
		private String iExamTypeName;
		private List<SessionMonth> iMonths;
		private List<PeriodSetupItemInterface> iItems;
		private int iBaseOffset = 0;
		private String iPattern;

		public Long getExamTypeId() { return iExamTypeId; }
		public void setExamTypeId(Long examTypeId) { iExamTypeId = examTypeId; }
		public String getExamTypeName() { return iExamTypeName; }
		public void setExamTypeName(String name) { iExamTypeName = name; }
		public boolean hasExamTypeName() { return iExamTypeName != null && !iExamTypeName.isEmpty(); }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		
		public void addMonth(SessionMonth month) {
			if (iMonths == null) iMonths = new ArrayList<SessionMonth>();
			iMonths.add(month);
		}
		public boolean hasMonths() { return iMonths != null && !iMonths.isEmpty(); }
		public List<SessionMonth> getMonths() { return iMonths; }
		
		public void setBaseOffset(int offset) { iBaseOffset = offset; }
		public int getBaseOffset() { return iBaseOffset; }		
		
		public void addItem(PeriodSetupItemInterface item) {
			if (iItems == null) iItems = new ArrayList<PeriodSetupItemInterface>();
			iItems.add(item);
		}
		public boolean hasItems() { return iItems != null && !iItems.isEmpty(); }
		public List<PeriodSetupItemInterface> getItems() { return iItems; }
		public PeriodSetupItemInterface getItem(int index) {
			if (iItems == null || index >= iItems.size()) return null;
			return iItems.get(index);
		}
		public int countItems() { return iItems == null ? 0 : iItems.size(); }
		
		public boolean hasPattern() { return iPattern != null && !iPattern.isEmpty(); }
		public void setPattern(String pattern) { iPattern = pattern; }
		public String getPattern() { return iPattern; }
	}
	
	public static class PeriodSetupItemInterface implements IsSerializable {
		private Integer iStartSlot, iLength, iStartOffset, iStopOffset;
		
		public Integer getStartSlot() { return iStartSlot; }
		public void setStartSlot(Integer startSlot) { iStartSlot = startSlot; }
		public Integer getLength() { return iLength; }
		public void setLength(Integer length) { iLength = length; }
		public Integer getStartOffset() { return iStartOffset; }
		public void setStartOffset(Integer startOffset) { iStartOffset = startOffset; }
		public Integer getStopOffset() { return iStopOffset; }
		public void setStopOffset(Integer stopOffset) { iStopOffset = stopOffset; }
	}
	
	public static class ExamPeriodInterface implements IsSerializable {
		private Long iPeriodId;
		private Long iExamTypeId;
		private Date iExamDate;
		private Integer iStartSlot, iLength, iStartOffset, iStopOffset;
		private Long iPreferenceId;
		private boolean iCanEdit = true;
		
		public Long getPeriodId() { return iPeriodId; }
		public void setPeriodId(Long periodId) { iPeriodId = periodId; }
		public Long getExamTypeId() { return iExamTypeId; }
		public void setExamTypeId(Long examTypeId) { iExamTypeId = examTypeId; }

		public Integer getStartSlot() { return iStartSlot; }
		public void setStartSlot(Integer startSlot) { iStartSlot = startSlot; }
		public Integer getLength() { return iLength; }
		public void setLength(Integer length) { iLength = length; }
		public Integer getStartOffset() { return iStartOffset; }
		public void setStartOffset(Integer startOffset) { iStartOffset = startOffset; }
		public Integer getStopOffset() { return iStopOffset; }
		public void setStopOffset(Integer stopOffset) { iStopOffset = stopOffset; }

		public Date getDate() { return iExamDate; }
		public void setDate(Date date) { iExamDate = date; }
		
		public Long getPreferenceId() { return iPreferenceId; }
		public void setPreferenceId(Long preferenceId) { iPreferenceId = preferenceId; }
		public boolean isCanEdit() { return iCanEdit; }
		public void setCanEdit(boolean canEdit) { iCanEdit = canEdit; }
	}

}
