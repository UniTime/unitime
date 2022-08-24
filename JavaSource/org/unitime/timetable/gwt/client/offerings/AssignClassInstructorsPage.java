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
package org.unitime.timetable.gwt.client.offerings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasFocus;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.DataColumn;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.Field;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.FieldType;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.ListItem;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.PageName;
import org.unitime.timetable.gwt.shared.AssignClassInstructorsInterface.Record;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Stephanie Schluttenhofer 
 *
 */
public class AssignClassInstructorsPage extends Composite {

	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	public static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	protected SimpleForm iPanel;
	protected Label iCourseTitle;
	protected HorizontalPanel iCoordinators; 
	protected HTML iCourseCoordinator;
	protected UniTimeHeaderPanel iHeader, iBottom;
	protected PageName iPageName = null;
	protected UniTimeTable<Record> iTable;
//	protected boolean iHasLazy = false;
	
	protected AssignClassInstructorsInterface iData;
	protected SimplePanel iSimple;
	
	protected boolean iEditable = true;
	protected boolean[] iVisible = null;
	
	public AssignClassInstructorsPage() {
		ClickHandler save = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String valid = validate();
				if (valid != null) {
					iHeader.setErrorMessage(valid);
					return;
				}
				iHeader.setMessage(MESSAGES.waitSavingData());
				LoadingWidget.showLoading(MESSAGES.waitSavingData());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						iData.getRecords().clear();
						iData.getRecords().addAll(iTable.getData());
						RPC.execute(AssignClassInstructorsInterface.SaveDataRpcRequest.saveData(iData), new AsyncCallback<AssignClassInstructorsInterface>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.hideLoading();
								iHeader.setErrorMessage(MESSAGES.failedSave(caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedSave(caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(AssignClassInstructorsInterface result) {
								iData = result;
								if (!iData.isSaveSuccessful()) {
									refreshTable();
									LoadingWidget.hideLoading();
									iHeader.setErrorMessage(MESSAGES.failedSave(iData.getErrors()));
									UniTimeNotifications.error(MESSAGES.failedSave(iData.getErrors()), null, null);									
								} else {
									LoadingWidget.hideLoading();
									ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.action?io=" + iData.getOfferingId());
								}
							}
						});
					}
				});
			}
		};
		
		ClickHandler removeInstrs = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!Window.confirm(MESSAGES.confirmRemoveClassInstructors())) return;
				String valid = validate();
				if (valid != null) {
					iHeader.setErrorMessage(valid);
					return;
				}
				iHeader.setMessage(MESSAGES.waitSavingData());
				LoadingWidget.showLoading(MESSAGES.waitSavingData());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						iData.getRecords().clear();
						iData.getRecords().addAll(iTable.getData());
						RPC.execute(AssignClassInstructorsInterface.RemoveAllClassInstructorsDataRpcRequest.removeInstructorData(iData), new AsyncCallback<AssignClassInstructorsInterface>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.hideLoading();
								iHeader.setErrorMessage(MESSAGES.failedSave(caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedSave(caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(AssignClassInstructorsInterface result) {
								iData = result;
								LoadingWidget.hideLoading();
								ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.action?io=" + iData.getOfferingId());
							}
						});
					}
				});
			}
		};
		
		ClickHandler prev = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String valid = validate();
				if (valid != null) {
					iHeader.setErrorMessage(valid);
					return;
				}
				iHeader.setMessage(MESSAGES.waitSavingData());
				LoadingWidget.showLoading(MESSAGES.waitSavingData());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {

					@Override
					public void execute() {
						iData.getRecords().clear();
						iData.getRecords().addAll(iTable.getData());
						RPC.execute(AssignClassInstructorsInterface.SaveDataGoToPreviousRpcRequest.saveDataAndPrev(iData), new AsyncCallback<AssignClassInstructorsInterface>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.hideLoading();
								iHeader.setErrorMessage(MESSAGES.failedSave(caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedSave(caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(AssignClassInstructorsInterface result) {
								iData = result;
								refreshTable();
								LoadingWidget.hideLoading();
								if (!iData.isSaveSuccessful()) {
									iHeader.setErrorMessage(MESSAGES.failedSave(iData.getErrors()));
									UniTimeNotifications.error(MESSAGES.failedSave(iData.getErrors()), null, null);									
								}
							}
						});
					}
				});
			}
		};
		ClickHandler next = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String valid = validate();
				if (valid != null) {
					iHeader.setErrorMessage(valid);
					return;
				}
				iHeader.setMessage(MESSAGES.waitSavingData());
				LoadingWidget.showLoading(MESSAGES.waitSavingData());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {

					@Override
					public void execute() {
						iData.getRecords().clear();
						iData.getRecords().addAll(iTable.getData());
						RPC.execute(AssignClassInstructorsInterface.SaveDataGoToNextRpcRequest.saveDataAndNext(iData), new AsyncCallback<AssignClassInstructorsInterface>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.hideLoading();
								iHeader.setErrorMessage(MESSAGES.failedSave(caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedSave(caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(AssignClassInstructorsInterface result) {
								iData = result;
								refreshTable();
								LoadingWidget.hideLoading();
								if (!iData.isSaveSuccessful()) {
									iHeader.setErrorMessage(MESSAGES.failedSave(iData.getErrors()));
									UniTimeNotifications.error(MESSAGES.failedSave(iData.getErrors()), null, null);									
								}
							}
						});
					}
				});
			}
		};
			
		ClickHandler back = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.showLoading(MESSAGES.waitPlease());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						LoadingWidget.hideLoading();
						ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.action?io=" + iData.getOfferingId());
					}
				});
			}
		};
		
		
		
		iPanel = new SimpleForm();		
		
		iHeader = new UniTimeHeaderPanel();
		
		iCourseTitle = new Label();
		
		iHeader.add(iCourseTitle);		 
		
		iHeader.addButton("unassignAll", MESSAGES.buttonUnassignAll(), 75, removeInstrs);
		iHeader.addButton("save", MESSAGES.buttonSave(), 75, save);
		iHeader.addButton("prev", MESSAGES.buttonPrevious(), 75, prev);
		iHeader.addButton("next", MESSAGES.buttonNext(), 75, next);
		iHeader.addButton("back", MESSAGES.buttonBack(), 75, back);
		iPanel.addHeaderRow(iHeader);

		iCoordinators = new HorizontalPanel();
		iCoordinators.setSpacing(3);
		iCoordinators.add(new Label(MESSAGES.labelCourseCoordinators()));
		iCourseCoordinator = new HTML("");
		iCourseCoordinator.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
		iCoordinators.add(iCourseCoordinator);
		iPanel.addRow(iCoordinators);

		iTable = new UniTimeTable<Record>();
		iTable.setAllowSelection(true);
		iPanel.addRow(iTable);
		
		iBottom = iHeader.clonePanel();
		iPanel.addNotPrintableBottomRow(iBottom);
		
		iSimple = new SimplePanel(iPanel);
		
		initWidget(iSimple);
						
		RPC.execute(AssignClassInstructorsInterface.GetPageNameRpcRequest.getPageName(), new AsyncCallback<PageName>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(MESSAGES.failedWrongEditType(caught.getMessage()));
				UniTimeNotifications.error(caught.getMessage(), caught);
			}

			@Override
			public void onSuccess(PageName result) {
				iPageName = result;
				UniTimePageLabel.getInstance().setPageName(iPageName.plural());
				init();
			}
		});
	}
	
	public void init() {
		load(null);
	}
	
	public void load(final AsyncCallback<Boolean> callback) {
		iBottom.setVisible(false);
		iCourseTitle.setText("");
		iCoordinators.setVisible(false);
		iCourseCoordinator.setText("");
		iHeader.setEnabled("add", false);
		iHeader.setEnabled("save", false);
		iHeader.setEnabled("prev", false);
		iHeader.setEnabled("next", false);
		iHeader.setEnabled("unassignAll", false);
		iHeader.setEnabled("back", false);
		iHeader.setMessage(MESSAGES.waitLoadingData());
		LoadingWidget.showLoading(MESSAGES.waitLoadingData());
		
		iTable.clearTable();
		
		
		RPC.execute(AssignClassInstructorsInterface.LoadDataRpcRequest.loadData(Window.Location.getParameter("configId")), new AsyncCallback<AssignClassInstructorsInterface>() {

			@Override
			public void onSuccess(AssignClassInstructorsInterface result) {
				iData = result;
				
				refreshTable();
				LoadingWidget.hideLoading();
				if (callback != null) callback.onSuccess(true);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				iHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
				if (callback != null) callback.onFailure(caught);
			}
		});
	}
	
	protected List<UniTimeTableHeader> header(boolean top) {
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		int col = 0;
		for (final Field field: iData.getFields()) {
			String name = field.getName();
			if (field.isHideLabel()) {
				name = "";
			}
			UniTimeTableHeader cell = new UniTimeTableHeader(name);
			if (!top) { cell.addStyleName("unitime-TopLineDash"); cell.getElement().getStyle().setPaddingTop(2, Unit.PX); }
			header.add(cell);
			if (field.getType() == FieldType.number)
				cell.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			if (col == DataColumn.DISPLAY.ordinal()) {
				cell.addOperation(
						new Operation() {
							@Override
							public String getName() {
								return MESSAGES.opSelectAll();
							}
							@Override
							public boolean hasSeparator() {
								return false;
							}
							@Override
							public boolean isApplicable() {
								return true;
							}
							@SuppressWarnings("rawtypes")
							@Override
							public void execute() {
								for (int row = 0; row < iTable.getRowCount(); row++) {
									Record r = iTable.getData(row);
									if (r != null && r.isEditable()) {
										r.setField(DataColumn.DISPLAY.ordinal(), Boolean.TRUE.toString(), r.isEditable(DataColumn.DISPLAY.ordinal()), r.isVisible(DataColumn.DISPLAY.ordinal()));
										((CheckBox)((UniTimeWidget)((MyCell)iTable.getWidget(row, DataColumn.DISPLAY.ordinal())).getInnerWidget()).getWidget()).setValue(true);
									}
								}
							}
						}
						);
				cell.addOperation(
						new Operation() {
	
							@Override
							public String getName() {
								return MESSAGES.opClearSelection();
							}
							@Override
							public boolean hasSeparator() {
								return false;
							}
							@Override
							public boolean isApplicable() {
								return true;
							}
							@SuppressWarnings("rawtypes")
							@Override
							public void execute() {
								for (int row = 0; row < iTable.getRowCount(); row++) {
									Record r = iTable.getData(row);
									if (r != null && r.isEditable()) {
										r.setField(DataColumn.DISPLAY.ordinal(), Boolean.FALSE.toString());
										((CheckBox)((UniTimeWidget)((MyCell)iTable.getWidget(row, DataColumn.DISPLAY.ordinal())).getInnerWidget()).getWidget()).setValue(false);
									}
								}
							}
						}
						);
			}
			col++;
		}
		if (iData.isEditable() && iEditable) {
			header.add(new UniTimeTableHeader());
			header.add(new UniTimeTableHeader());
		}
		return header;
	}
	
	private void refreshTable() {
		refreshTable(null);
	}
	
	private void refreshTable(String hidden) {
		iCourseTitle.setText(iData.getCourseName());
		iCourseCoordinator.setText(iData.getCourseCoordinators());
		iCoordinators.setVisible(iData.getCourseCoordinators() != null && !iData.getCourseCoordinators().equals(""));

		UniTimePageLabel.getInstance().setPageName(iPageName.plural());
		iTable.clearTable();
		
		iTable.setAllowSelection(false);

		iTable.addRow(null, header(true));
		
		if (iVisible == null) {
			iVisible = new boolean[iData.getFields().length];
			for (int i = 0; i < iVisible.length; i++) iVisible[i] = iData.getFields()[i].isVisible() && (hidden == null || !hidden.contains("|" + iData.getFields()[i].getName() + "|"));
		}
		
		int row = 1;
		for (Record r: iData.getRecords()) {
			fillRow(r, row++);
		}
		
		iBottom.setVisible(true);
		if (iData.isEditable()) {
			iHeader.setEnabled("back", iEditable);
			iHeader.setEnabled("save", iEditable);
			iHeader.setEnabled("prev", iEditable && iData.getPreviousConfigId() != null);
			iHeader.setEnabled("next", iEditable && iData.getNextConfigId() != null);
			iHeader.setEnabled("add", !iEditable);
			if (iData.isAllInstructorsDeletable()) {
				iHeader.setEnabled("unassignAll", iEditable);
			} else {
				iHeader.setEnabled("unassignAll", false);
			}
		}
		if (iData.isShowTimeAndRoom()) {
			iVisible[DataColumn.TIME.ordinal()] = true;
			iVisible[DataColumn.ROOM.ordinal()] = true;
		} else {
			iVisible[DataColumn.TIME.ordinal()] = false;
			iVisible[DataColumn.ROOM.ordinal()] = false;			
		}
		
		for (int i = 0; i < iVisible.length; i++) 
			iTable.setColumnVisible(i, iVisible[i]);
		
		iHeader.clearMessage();
	}
	
	protected void fillRow(Record record, int row) {
		List<Widget> line = new ArrayList<Widget>();
		int col = 0;
		for (Field field: iData.getFields()) {
			MyCell cell = new MyCell(iData.isEditable() && iEditable && record.isEditable(col), field, record, col, false);
			line.add(cell);
			col++;
		}
		iTable.setRow(row, record, line);
	}
	
	public class MyCell extends Composite implements HasFocus, HasCellAlignment {
		private Field iField;
		private Record iRecord;
		private int iIndex;
		
		public MyCell(boolean editable, final Field field, final Record record, final int index, boolean detail) {
			iField = field; iRecord = record; iIndex = index;
			if (editable) {
				switch (field.getType()) {
				case hasError:
					if (iEditable 
						&& record.getField(index) == Boolean.TRUE.toString() && record.isEditable(index)) {
							final Image error = new Image(RESOURCES.warning());
							error.setTitle(MESSAGES.fieldError());
							initWidget(new UniTimeWidget<Image>(error));
					} else {
						initWidget(new UniTimeWidget<Label>(new Label()));
					}
					break;

				case add:
					if (iEditable 
						&& record.getField(index) == Boolean.TRUE.toString() && record.isEditable(index)) {
							final Image add = new Image(RESOURCES.add());
							add.getElement().getStyle().setCursor(Cursor.POINTER);
							add.setTitle(MESSAGES.fieldInsertRowBelow());
							add.addClickHandler(new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									int row = iTable.getCellForEvent(event).getRowIndex();
									
									int recId = iData.getRecords().size() ;
									Record rec = iData.addRecord(Long.valueOf(recId));
									Record oldRec = iTable.getData(row);
									rec.setField(DataColumn.CLASS_UID.ordinal(), oldRec.getField(DataColumn.CLASS_UID.ordinal()), false);
									rec.setField(DataColumn.IS_FIRST_RECORD_FOR_CLASS.ordinal(), Boolean.FALSE.toString(), false);
									rec.setField(DataColumn.HAS_ERROR.ordinal(), Boolean.FALSE.toString(), false);
									rec.setField(DataColumn.DELETE.ordinal(), Boolean.TRUE.toString());
									rec.setField(DataColumn.ADD.ordinal(), Boolean.TRUE.toString());
									rec.setField(DataColumn.CLASS_NAME.ordinal(), oldRec.getField(DataColumn.CLASS_NAME.ordinal()), false, false);
									rec.setField(DataColumn.CLASS_EXTERNAL_UID.ordinal(), oldRec.getField(DataColumn.CLASS_EXTERNAL_UID.ordinal()), false, false);
									rec.setField(DataColumn.TIME.ordinal(), oldRec.getField(DataColumn.TIME.ordinal()), false, false);
									rec.setField(DataColumn.ROOM.ordinal(), oldRec.getField(DataColumn.ROOM.ordinal()), false, false);
									rec.setField(DataColumn.DISPLAY.ordinal(), oldRec.getField(DataColumn.DISPLAY.ordinal()), oldRec.isEditable(DataColumn.DISPLAY.ordinal()), false);
									rec.setField(DataColumn.INSTR_NAME.ordinal(), "", oldRec.isEditable(DataColumn.INSTR_NAME.ordinal()), true);
									rec.setField(DataColumn.PCT_SHARE.ordinal(), "0", oldRec.isEditable(DataColumn.PCT_SHARE.ordinal()), true);
									rec.setField(DataColumn.CHECK_CONFICTS.ordinal(), Boolean.TRUE.toString(), oldRec.isEditable(DataColumn.CHECK_CONFICTS.ordinal()), true);
									rec.setField(DataColumn.RESPONSIBILITY.ordinal(), oldRec.getField(DataColumn.RESPONSIBILITY.ordinal()), oldRec.isEditable(DataColumn.RESPONSIBILITY.ordinal()), oldRec.isVisible(DataColumn.RESPONSIBILITY.ordinal()));
									rec.setField(DataColumn.FUNDING_DEPT.ordinal(), oldRec.getField(DataColumn.FUNDING_DEPT.ordinal()), oldRec.isEditable(DataColumn.FUNDING_DEPT.ordinal()), false);
									oldRec.setField(DataColumn.DELETE.ordinal(), Boolean.TRUE.toString(), true, true);
									fillRow(oldRec, row);
									fillRow(rec, iTable.insertRow(row + 1));
								}
							});
							initWidget(new UniTimeWidget<Image>(add));
					} else {
						initWidget(new UniTimeWidget<Label>(new Label()));
					}
					break;
				case delete:
					if (iData.isEditable() && iEditable && record.isDeletable() 
							&& record.getField(index) == Boolean.TRUE.toString() && record.isEditable(index)) {
							final Image delete = new Image(RESOURCES.delete());
							delete.setTitle(MESSAGES.titleDeleteRow());
							delete.getElement().getStyle().setCursor(Cursor.POINTER);
							delete.addClickHandler(new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									int row = iTable.getCellForEvent(event).getRowIndex();
									Record delRec = iTable.getData(row);
									Record nextRec = iTable.getData(row + 1);
									Record nextNextRec = iTable.getData(row + 2);
									
									if (delRec.getField(DataColumn.IS_FIRST_RECORD_FOR_CLASS.ordinal()).equals(Boolean.TRUE.toString()) 
											&& nextRec != null 
											&& nextRec.getField(DataColumn.CLASS_UID.ordinal()).equals(delRec.getField(DataColumn.CLASS_UID.ordinal()))){
										nextRec.setField(DataColumn.IS_FIRST_RECORD_FOR_CLASS.ordinal(), Boolean.TRUE.toString(), false, false);
										nextRec.setField(DataColumn.CLASS_NAME.ordinal(), delRec.getField(DataColumn.CLASS_NAME.ordinal()), false, true);
										nextRec.setField(DataColumn.CLASS_EXTERNAL_UID.ordinal(), delRec.getField(DataColumn.CLASS_EXTERNAL_UID.ordinal()), false, true);
										nextRec.setField(DataColumn.TIME.ordinal(), delRec.getField(DataColumn.TIME.ordinal()), false, true);
										nextRec.setField(DataColumn.ROOM.ordinal(), delRec.getField(DataColumn.ROOM.ordinal()), false, true);
										nextRec.setField(DataColumn.DISPLAY.ordinal(), delRec.getField(DataColumn.DISPLAY.ordinal()), delRec.isEditable(DataColumn.DISPLAY.ordinal()), true);
										nextRec.setField(DataColumn.FUNDING_DEPT.ordinal(), delRec.getField(DataColumn.FUNDING_DEPT.ordinal()), delRec.isEditable(DataColumn.FUNDING_DEPT.ordinal()), true);
										if (nextNextRec != null 
												&& !nextNextRec.getField(DataColumn.CLASS_UID.ordinal()).equals(nextRec.getField(DataColumn.CLASS_UID.ordinal()))){
											nextRec.setField(DataColumn.DELETE.ordinal(), Boolean.FALSE.toString(), false, false);											
										}
										fillRow(nextRec, row + 1);
										iData.getRecords().remove(iTable.getData(row));
										iTable.removeRow(row);
									} else {
										if (row > 1) {
											Record prevRec = iTable.getData(row - 1);
											if (prevRec.getField(DataColumn.CLASS_UID.ordinal()).equals(delRec.getField(DataColumn.CLASS_UID.ordinal()))
													&& prevRec.getField(DataColumn.IS_FIRST_RECORD_FOR_CLASS.ordinal()).equals(Boolean.TRUE.toString())
													&& (nextRec == null 
													|| !nextRec.getField(DataColumn.CLASS_UID.ordinal()).equals(delRec.getField(DataColumn.CLASS_UID.ordinal())))
													){
										
												prevRec.setField(DataColumn.DELETE.ordinal(), Boolean.FALSE.toString(), false, false);											
												fillRow(prevRec, row - 1);
											}
										}
										iData.getRecords().remove(iTable.getData(row));
										iTable.removeRow(row);
									}
								}
							});
							initWidget(new UniTimeWidget<Image>(delete));
					} else {
						initWidget(new UniTimeWidget<Label>(new Label()));
					}

					break;
				case textarea:
					final TextArea textarea = new TextArea();
					textarea.setStyleName("unitime-TextArea");
					if (detail) {
						textarea.setVisibleLines(Math.max(5, field.getHeight()));
						textarea.setCharacterWidth(Math.max(80, field.getWidth()));
					} else {
						textarea.setVisibleLines(field.getHeight() <= 0 ? 2 : Math.min(3, field.getHeight()));
						textarea.setCharacterWidth(field.getWidth() <= 0 ? 40: Math.min(60, field.getWidth()));
					}
					textarea.setText(record.getField(index));
					textarea.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, textarea.getText());
							setError(null);
						}
					});
					initWidget(new UniTimeWidget<TextArea>(textarea));
					if (iEditable && record.getUniqueId() == null) {
						textarea.addChangeHandler(new ChangeHandler() {
							@Override
							public void onChange(ChangeEvent event) {
								if (iData.getRecords().indexOf(iRecord) == iData.getRecords().size() - 1 && !record.isEmpty())
									fillRow(iData.addRecord(null), iTable.insertRow(iTable.getRowCount()));
							}
						});
					}
					break;
				case number:
					final NumberBox number = new NumberBox();
					number.getElement().getStyle().setTextAlign(TextAlign.RIGHT);
					number.setText(record.getField(index));
					number.setDecimal(field.isAllowFloatingPoint());
					number.setNegative(field.isAllowNegative());
					number.setWidth(field.getWidth() + "px");
					number.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, number.getText());
							setError(null);
						}
					});
					initWidget(new UniTimeWidget<TextBox>(number));
					if (iEditable && record.getUniqueId() == null) {
						number.addChangeHandler(new ChangeHandler() {
							@Override
							public void onChange(ChangeEvent event) {
								if (iData.getRecords().indexOf(iRecord) == iData.getRecords().size() - 1 && !record.isEmpty())
									fillRow(iData.addRecord(null), iTable.insertRow(iTable.getRowCount()));
							}
						});
					}
					break;
				case list:
					final ListBox list = new ListBox();
					list.setMultipleSelect(false);
					list.setStyleName("unitime-TextBox");
					if (((record.getField(index) == null || record.getField(index).isEmpty()))
						&& (field.getValues().isEmpty() || !field.getValues().get(0).getValue().isEmpty())) {
						list.addItem("", "");
					}
					for (ListItem item: field.getValues())
						list.addItem(item.getText(), item.getValue());
					for (int i = 0; i < list.getItemCount(); i++)
						if (list.getValue(i).equals(record.getField(index)))
							list.setSelectedIndex(i);
					list.setVisible(record.isVisible(index));

					list.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, (list.getSelectedIndex() < 0 || list.getValue(list.getSelectedIndex()).isEmpty() ? null : list.getValue(list.getSelectedIndex())));
							setError(null);
						}
					});
					initWidget(new UniTimeWidget<ListBox>(list));
					if (iEditable && record.getUniqueId() == null) {
						list.addChangeHandler(new ChangeHandler() {
							@Override
							public void onChange(ChangeEvent event) {
								if (iData.getRecords().indexOf(iRecord) == iData.getRecords().size() - 1 && !record.isEmpty())
									fillRow(iData.addRecord(null), iTable.insertRow(iTable.getRowCount()));
							}
						});
					}
					break;
				case toggle:
					final CheckBox check = new CheckBox();
					check.setValue(record.getField(index) == null ? null : "true".equalsIgnoreCase(record.getField(index)));
					if (record.getField(index) == null && field.isCheckedByDefault()) check.setValue(true);
					check.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							record.setField(index, check.getValue() == null ? null : check.getValue() ? "true" : "false");
							setError(null);
						}
					});
					check.setVisible(record.isVisible(index));
					initWidget(new UniTimeWidget<CheckBox>(check));
					if (iEditable && record.getUniqueId() == null) {
						check.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								if (iData.getRecords().indexOf(iRecord) == iData.getRecords().size() - 1 && !record.isEmpty())
									fillRow(iData.addRecord(null), iTable.insertRow(iTable.getRowCount()));
							}
						});
					}
					break;
				}
			} else {
				switch (field.getType()) {
				case toggle:
					if (record.getField(index) != null && !"true".equals(record.getField(index)) && !"false".equals(record.getField(index))) {
						initWidget(new HTML(record.getField(index), false)); break;
					}
					Image image = new Image((record.getField(index) == null && field.isCheckedByDefault()) || (record.getField(index) != null && "true".equalsIgnoreCase(record.getField(index))) ? RESOURCES.on() : RESOURCES.off());
					image.setVisible(record.isVisible(index));
					initWidget(image);
					break;
				case textarea:
					HTML html = new HTML(getValue());
					html.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE);
					html.setVisible(record.isVisible(index));
					initWidget(html);
					break;
				case add:
					initWidget(new UniTimeWidget<Label>(new Label()));
					break;
				case delete:
					initWidget(new UniTimeWidget<Label>(new Label()));
					break;
				default:
					Label label = new Label(getValue());
					initWidget(label);
					label.setVisible(record.isVisible(index));
					if (field.getType() == FieldType.number)
						label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
					break;
				}
			}
			setError(null);
		}
		
		public void setError(String message) {
			if (getWidget() instanceof UniTimeWidget<?>) {
				UniTimeWidget<?> w = (UniTimeWidget<?>)getWidget();
				if (message == null || message.isEmpty()) {
					w.clearHint();
				} else
					w.setErrorHint(message);
			}
		}
						
		public String getValue() {
			String value = iRecord.getField(iIndex);
			if (value == null) return "";
			if (iField.getType() == FieldType.list) {
				for (ListItem item: iField.getValues()) {
					if (item.getValue().equals(value)) return item.getText();
				}
			} 
			return value;
		}
		
		public Record getRecord() { return iRecord; }
		
		public boolean focus() { 
			Widget w = getWidget();
			if (w instanceof UniTimeWidget<?>)
				w = ((UniTimeWidget<?>)w).getWidget();
			if (w instanceof Focusable) {
				((Focusable)w).setFocus(true);
				if (w instanceof TextBox)
					((TextBox)w).selectAll();
				return true;
			}
			return false;
		}
		
		public Widget getInnerWidget() { return getWidget(); }

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			switch (iField.getType()) {
			case toggle:
				return HasHorizontalAlignment.ALIGN_CENTER;
			default:
				return HasHorizontalAlignment.ALIGN_LEFT;
			}
		}

	}

	public String validate() {
		String valid = null;
		Map<Integer, Map<String, MyCell>> uniqueMap = new HashMap<Integer, Map<String, MyCell>>();
		for (int row = 0; row < iTable.getRowCount(); row++) {
			AssignClassInstructorsInterface.Record record = iTable.getData(row);
			if (record == null || record.isEmpty()) continue;
			for (int col = 0; col < iData.getFields().length; col ++) {
				Field field = iData.getFields()[col];
				String value = record.getField(col);
				MyCell widget = (MyCell)iTable.getWidget(row, col);
				widget.setError(null);
				if (!field.isEditable()) continue;
				if (field.isUnique()) {
					Map<String, MyCell> values = uniqueMap.get(col);
					if (values == null) {
						values = new HashMap<String, MyCell>(); uniqueMap.put(col, values);
					}
					if (value == null || value.isEmpty()) {
						widget.setError(MESSAGES.errorMustBeSet(field.getName()));
						if (valid == null) {
							valid = MESSAGES.errorMustBeSet(field.getName());
						}
					} else {
						MyCell old = values.put(value, widget);
						if (old != null) {
							widget.setError(MESSAGES.errorMustBeUnique(field.getName()));
							old.setError(MESSAGES.errorMustBeUnique(field.getName()));
							if (valid == null) {
								valid = MESSAGES.errorMustBeUnique(field.getName());
							}
						}
					}
				} 
				if (field.isUniqueIfSet()) {
					Map<String, MyCell> values = uniqueMap.get(col);
					if (values == null) {
						values = new HashMap<String, MyCell>(); uniqueMap.put(col, values);
					}
					if (!(value == null || value.isEmpty())) {
						MyCell old = values.put(value, widget);
						if (old != null) {
							widget.setError(MESSAGES.errorMustBeUnique(field.getName()));
							old.setError(MESSAGES.errorMustBeUnique(field.getName()));
							if (valid == null) {
								valid = MESSAGES.errorMustBeUnique(field.getName());
							}
						}
					}
				} 
				if (field.isNotEmpty()) {
					if (value == null || value.isEmpty()) {
						widget.setError(MESSAGES.errorMustBeSet(field.getName()));
						if (valid == null) {
							valid = MESSAGES.errorMustBeSet(field.getName());
						}
					}
				}
				switch (field.getType()) {
				case textarea:
					if (value != null && value.length() > field.getLength()) {
						widget.setError(MESSAGES.errorTooLong(field.getName()));
						if (valid == null) {
							valid = MESSAGES.errorTooLong(field.getName());
						}
					}
					break;
				default:
					break;
				}
			}
		}
		return valid;
	}
	
	public static class SimpleFormWithMouseOver extends SimpleForm {
		public SimpleFormWithMouseOver() {
			super();
			
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
		}
		
		public void onBrowserEvent(final Event event) {
			Element td = getEventTargetCell(event);
			if (td==null) return;
		    final Element tr = DOM.getParent(td);
			int col = DOM.getChildIndex(tr, td);
		    Element body = DOM.getParent(tr);
		    int row = DOM.getChildIndex(body, tr);
		    
		    Widget widget = getWidget(row, col);
		    if (widget != null && widget instanceof UniTimeHeaderPanel) {
		    	super.onBrowserEvent(event);
		    	return;
		    }
		    
			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				getRowFormatter().addStyleName(row, "hover");
				break;
			case Event.ONMOUSEOUT:
				getRowFormatter().removeStyleName(row, "hover");
				break;
			}
			
			super.onBrowserEvent(event);
		}
	}
	

}
