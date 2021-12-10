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
package org.unitime.timetable.gwt.client.departments;

import java.util.logging.Logger;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.shared.DepartmentInterface;
import org.unitime.timetable.gwt.shared.DepartmentInterface.GetDepartmentsRequest;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentsDataResponse;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class DepartmentsPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	private SimplePanel iPanel;
	private SimpleForm iListDepartmentsForm;
	private UniTimeHeaderPanel iListDepartmentsHeader, iListDepartmentsFooter;
	private DepartmentsTable iDepartmentsTable;
	private DepartmentsEdit iDepartmentsEdit;
	private UniTimeWidget<CheckBox> iShowAllDept;

	public DepartmentsPage() {
		iPanel = new SimplePanel();

		iListDepartmentsForm = new SimpleForm();

		// Header
		iListDepartmentsHeader = new UniTimeHeaderPanel(
				MESSAGES.sectDepartments());

		// Department ADD Button
		iListDepartmentsHeader.addButton("add", MESSAGES.buttonAddDepartment(),
				new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						addDepartment();
					}
				});
		// iListDepartmentsHeader.setEnabled("add", true);

		// EXPORT PDF Button
		iListDepartmentsHeader.addButton("export", MESSAGES.buttonExportPDF(),
				new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						ToolBox.open(GWT.getHostPageBaseURL()
								+ "export?output=departments.pdf&sort="
								+ iDepartmentsTable.getSortBy()
								+ "&showAllDept="
								+ iShowAllDept.getWidget().getValue());
					}
				});
		// iListDepartmentsHeader.setEnabled("export", true);
		iListDepartmentsForm.addHeaderRow(iListDepartmentsHeader);

		// Grid
		iDepartmentsTable = new DepartmentsTable();
		iListDepartmentsForm.addRow(iDepartmentsTable);
		iDepartmentsTable.setAllowSelection(true);
		iDepartmentsTable.setAllowMultiSelect(false);

		iListDepartmentsFooter = iListDepartmentsHeader.clonePanel("");
		iListDepartmentsForm.addBottomRow(iListDepartmentsFooter);

		// Footer Show All dept checkbox
		iShowAllDept = new UniTimeWidget<CheckBox>(new CheckBox());
		iListDepartmentsForm
				.addRow("Show all departments (including departments with no manager and no subject area):",
						iShowAllDept);

		// Hook up a handler to find out when it's clicked.
		iShowAllDept.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// load
				LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());

				// Get all departments
				listDepartments();
			}
		});

		// load
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());

		// Get all departments
		listDepartments();

		// UI to update, add or back
		iDepartmentsEdit = new DepartmentsEdit() {
			
			//Override on back event from DepartmentEdit page
			@Override
			protected void onBack(boolean refresh, final Long DepartmentId) {
				iPanel.setWidget(iListDepartmentsForm);
				UniTimePageLabel.getInstance().setPageName(
						MESSAGES.pageDepartments());
				if (refresh) {
					LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
					RPC.execute(new GetDepartmentsRequest(),new AsyncCallback<DepartmentsDataResponse>() {
								@Override
								public void onSuccess(DepartmentsDataResponse result) {
									iListDepartmentsHeader.setEnabled("add",result.isCanAdd());
									iListDepartmentsHeader.setEnabled("export",result.isCanExportPdf());
									iListDepartmentsHeader.setEnabled("updateData", result.isCanUpdate());
									iDepartmentsTable.setData(result.getDepartments(),iShowAllDept.getWidget().getValue());
									LoadingWidget.getInstance().hide();
									if (DepartmentId != null)
										for (int i = 0; i < iDepartmentsTable.getRowCount(); i++) {
											DepartmentInterface b = iDepartmentsTable.getData(i);
											if (b != null && b.getId().equals(DepartmentId)) {
												iDepartmentsTable.getRowFormatter().getElement(i).scrollIntoView();
												iDepartmentsTable.setSelected(i, true);
												break;
											}
										}
								}

								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
									iListDepartmentsHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
									ToolBox.checkAccess(caught);
								}

							});
				} else {
					if (iDepartmentsTable.getSelectedRow() >= 0)
						iDepartmentsTable.setSelected(iDepartmentsTable.getSelectedRow(), false);
					if (DepartmentId != null)
						for (int i = 0; i < iDepartmentsTable.getRowCount(); i++) {
							DepartmentInterface b = iDepartmentsTable.getData(i);
							if (b != null && b.getId().equals(DepartmentId)) {
								iDepartmentsTable.getRowFormatter().getElement(i).scrollIntoView();
								iDepartmentsTable.setSelected(i, true);
								break;
							}
						}
				}
			}

			//Override isAbbreviationUnique from DepartmentEdit page
			@Override
			protected boolean isAbbreviationUnique(
					DepartmentInterface Department) {
				for (int i = 0; i < iDepartmentsTable.getRowCount(); i++) {
					DepartmentInterface b = iDepartmentsTable.getData(i);
					if (b != null && !b.getId().equals(Department.getId()) && b.getAbbreviation().equalsIgnoreCase(
									Department.getAbbreviation())) {
						return false;
					}
				}
				return true;
			}
		};
		iPanel.setWidget(iListDepartmentsForm);
		initWidget(iPanel);
	}

	protected void addDepartment() {
		// Logger log = Logger.getLogger(DepartmentsPage.class.getName());
		// log.info("addDepartment");
		iDepartmentsEdit.setValue(null);
		iPanel.setWidget(iDepartmentsEdit);
		iDepartmentsEdit.show();
		UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAddDepartment());
		iDepartmentsTable.clearHover();

	}

	protected void editDepartment(DepartmentInterface department) {
		Logger log = Logger.getLogger(DepartmentsPage.class.getName());
		// log.info("editDepartment");
		// log.info("department" + department.getName());
		iDepartmentsEdit.setValue(department);
		iPanel.setWidget(iDepartmentsEdit);
		iDepartmentsEdit.show();
		UniTimePageLabel.getInstance().setPageName(
				MESSAGES.pageEditDepartment());
		iDepartmentsTable.clearHover();
	}

	protected void listDepartments() {
		// Get all departments
		RPC.execute(new GetDepartmentsRequest(),
				new AsyncCallback<DepartmentsDataResponse>() {
					@Override
					public void onSuccess(DepartmentsDataResponse result) {
						iListDepartmentsHeader.setEnabled("add",result.isCanAdd());
						iListDepartmentsHeader.setEnabled("export",result.isCanExportPdf());
						Logger log = Logger.getLogger(DepartmentsPage.class
								.getName());
						// list departments
						iDepartmentsTable.setData(result.getDepartments(),iShowAllDept.getWidget().getValue());
						LoadingWidget.getInstance().hide();

						// Click to edit
						iDepartmentsTable
								.addMouseClickListener(new MouseClickListener<DepartmentInterface>() {
									@Override
									public void onMouseClick(
											TableEvent<DepartmentInterface> event) {
										if (event.getData() != null
												&& event.getData().isCanEdit()) {
											iDepartmentsTable.setSelected(
													event.getRow(), true);
											editDepartment(event.getData());
										}
									}
								});
					}

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.error(
								MESSAGES.failedLoadData(caught.getMessage()),
								caught);
						iListDepartmentsHeader.setErrorMessage(MESSAGES
								.failedLoadData(caught.getMessage()));
						ToolBox.checkAccess(caught);
					}

				});
	}
}
