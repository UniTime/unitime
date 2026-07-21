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

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.IdLabel;
import org.unitime.timetable.gwt.client.admin.TimetableManagersPage.TimetableManagerEditRequest.Operation;
import org.unitime.timetable.gwt.client.aria.AriaCheckBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class TimetableManagersPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iListHeader, iListFooter;
	private CheckBox iShowAllManagers;
	private TableWidget iTable;
	private UniTimeHeaderPanel iHeader, iFooter;
	private TimetableManagerInterface iManager;
	private Lookup iLookup;
	
	public TimetableManagersPage() {
		iPanel = new SimpleForm(3);
		initWidget(iPanel);
		iPanel.addStyleName("unitime-TimetableManagersPage");
		iListHeader = new UniTimeHeaderPanel();
		iListHeader.addButton("add", COURSE.actionAddTimetableManager(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("add", false);
				editManager(null);
			}
		});
		iListHeader.setEnabled("add", false);
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
		iShowAllManagers = new CheckBox(COURSE.checkShowAllManagers());
		iShowAllManagers.setValue("1".equals(ToolBox.getCookie("TimetableManagers.ShowAll")));
		iShowAllManagers.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				ToolBox.setCookie("TimetableManagers.ShowAll", event.getValue() ? "1" : "0");
				showManagers();
			}
		});
		iListFooter.insertLeft(iShowAllManagers, true);
		iTable = new TableWidget();
		iTable.addStyleName("table");
		
		iHeader = new UniTimeHeaderPanel("");
		iHeader.addButton("save", COURSE.actionSaveManager(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateManager();
			}
		});
		iHeader.getButton("save").setAccessKey(COURSE.accessSaveManager().charAt(0));
		iHeader.getButton("save").setTitle(COURSE.titleSaveManager(COURSE.accessSaveManager()));
		iHeader.setEnabled("save", false);
		iHeader.addButton("update", COURSE.actionUpdateManager(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateManager();
			}
		});
		iHeader.getButton("update").setAccessKey(COURSE.accessUpdateManager().charAt(0));
		iHeader.getButton("update").setTitle(COURSE.titleUpdateManager(COURSE.accessUpdateManager()));
		iHeader.setEnabled("update", false);
		
		iHeader.addButton("delete", COURSE.actionDeleteManager(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteManager();
			}
		});
		iHeader.getButton("delete").setAccessKey(COURSE.accessDeleteManager().charAt(0));
		iHeader.getButton("delete").setTitle(COURSE.titleDeleteManager(COURSE.accessDeleteManager()));
		iHeader.setEnabled("delete", false);
		
		iHeader.addButton("back", COURSE.actionBackToManagers(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(null, false);
				showManagers(iManager == null ? null : iManager.getManagerId());
			}
		});
		iHeader.getButton("back").setAccessKey(COURSE.accessBackToManagers().charAt(0));
		iHeader.getButton("back").setTitle(COURSE.titleBackToManagers(COURSE.accessBackToManagers()));
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
			showManagers();
		else if ("add".equals(token))
			editManager(null);
		else {
			try {
				editManager(Long.valueOf(token));
			} catch (NumberFormatException e) {
				showManagers();
			}
		}
	}
	
	protected void showManagers() {
		showManagers(null);
	}
	
	protected void deleteManager() {
		UniTimeConfirmationDialog.confirm(COURSE.confirmDeleteManager(), new Command() {
			@Override
			public void execute() {
				RPC.execute(new TimetableManagerEditRequest(Operation.DELETE, iManager.getManagerId()), new AsyncCallback<TimetableManagerEditResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MSG.failedToDeleteData(caught.getMessage()));
						UniTimeNotifications.error(MSG.failedToDeleteData(caught.getMessage()), caught);
						ToolBox.checkAccess(caught);					
					}

					@Override
					public void onSuccess(TimetableManagerEditResponse result) {
						History.newItem(null, false);
						showManagers(null);
					}
				});
			}
		});
	}
	
	protected void showManagers(final Long managerId) {
		UniTimePageLabel.getInstance().setPageName(MSG.pageTimetableManagers());
		iPanel.clear();
		iListHeader.setEnabled("add", false);
		iListHeader.setEnabled("csv", false);
		iListHeader.setEnabled("pdf", false);
		iPanel.addHeaderRow(iListHeader);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new TimetableManagersRequest(iShowAllManagers.getValue()), new AsyncCallback<TimetableManagersResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(TimetableManagersResponse result) {
				LoadingWidget.getInstance().hide();
				iTable.setData(result.getManagersTable());
				iPanel.addRow(iTable);
				iPanel.addBottomRow(iListFooter);
				iListHeader.setHeaderTitle(result.getManagersTable().getName());
				iListHeader.setEnabled("add", result.isCanAdd());
				if (managerId != null)
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						LineInterface line = iTable.getData(row);
						if (line != null && managerId.equals(line.getId())) {
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
	
	
	private TextBox iFirstName, iMiddleName, iLastName, iAcadTitle, iExternalId, iEmailAddress;
	private Button iLookupButton;
	private ListBox iDepartments, iSolverGroups, iRoles;
	private DepartmentsTable iDepartmentsTable, iSolverGroupsTable;
	private RolesTable iRolesTable;
	
	protected void editManager(Long managerId) {
		Window.scrollTo(0, 0);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new TimetableManagerEditRequest(managerId == null ? Operation.ADD : Operation.EDIT, managerId), new AsyncCallback<TimetableManagerEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToLoadData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);				
			}

			@Override
			public void onSuccess(TimetableManagerEditResponse result) {
				LoadingWidget.getInstance().hide();
				UniTimePageLabel.getInstance().setPageName(result.getManagerId() == null ? MSG.pageAddTimetableManager() : MSG.pageEditTimetableManager());
				iHeader.setEnabled("save", result.getManagerId() == null);
				iHeader.setEnabled("update", result.getManagerId() != null);
				iHeader.setEnabled("delete", result.isCanDelete() && result.getManagerId() != null);
				iHeader.setEnabled("back", true);
				iManager = result.getManager();
				if (iManager == null) iManager = new TimetableManagerInterface();

				iHeader.setHeaderTitle(iManager.hasFormattedName() ? iManager.getFormattedName() : "");
				iPanel.clear();
				iPanel.addHeaderRow(iHeader);
				
				if (result.hasSessionName())
					iPanel.addRow(MSG.propAcademicSession(), new Label(result.getSessionName()));
				
				if (iLookup == null) {
					iLookup = new Lookup();
					iLookup.setOptions("mustHaveExternalId");
					iLookup.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
						@Override
						public void onValueChange(ValueChangeEvent<PersonInterface> event) {
							PersonInterface person = event.getValue();
							if (person != null) {
								iFirstName.setValue(person.getFirstName() == null ? "" : person.getFirstName(), true);
								iMiddleName.setValue(person.getMiddleName() == null ? "" : person.getMiddleName(), true);
								iLastName.setValue(person.getLastName() == null ? "" : person.getLastName(), true);
								iAcadTitle.setValue(person.getAcademicTitle() == null ? "" : person.getAcademicTitle(), true);
								iExternalId.setValue(person.getId() == null ? "" : person.getId(), true);
								iEmailAddress.setValue(person.getEmail() == null ? "" : person.getEmail(), true);
							}
						}
					});
					iLookupButton = new Button(COURSE.actionLookupManager());
					iLookupButton.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							iLookup.setQuery(iFirstName.getText() + " " + iLastName.getText());
							iLookup.center();
						}
					});
				}
				iFirstName = new TextBox();
				iFirstName.setWidth("350px"); iFirstName.setMaxLength(100);
				if (iManager.hasFirstName()) iFirstName.setText(iManager.getFirstName());
				P fn = new P("first-name"); fn.add(iFirstName); fn.add(iLookupButton);
				iPanel.addRow(COURSE.fieldFirstName() + ":", fn);
				iFirstName.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iManager.setFirstName(event.getValue());
					}
				});
				
				iMiddleName = new TextBox();
				iMiddleName.setWidth("350px"); iMiddleName.setMaxLength(100);
				if (iManager.hasMiddleName()) iMiddleName.setText(iManager.getMiddleName());
				iPanel.addRow(COURSE.fieldMiddleName() + ":", iMiddleName);
				iMiddleName.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iManager.setMiddleName(event.getValue());
					}
				});
				
				iLastName = new TextBox();
				iLastName.setWidth("350px"); iLastName.setMaxLength(100);
				if (iManager.hasLastName()) iLastName.setText(iManager.getLastName());
				iPanel.addRow(COURSE.fieldLastName() + ":", iLastName);
				iLastName.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iManager.setLastName(event.getValue());
					}
				});
				
				iAcadTitle = new TextBox();
				iAcadTitle.setWidth("200px"); iAcadTitle.setMaxLength(50);
				if (iManager.hasAcadTitle()) iAcadTitle.setText(iManager.getAcadTitle());
				iPanel.addRow(COURSE.fieldAcademicTitle() + ":", iAcadTitle);
				iAcadTitle.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iManager.setAcadTitle(event.getValue());
					}
				});
				
				iExternalId = new TextBox();
				iExternalId.setWidth("270px"); iExternalId.setMaxLength(40);
				if (iManager.hasExternalId()) iExternalId.setText(iManager.getExternalId());
				iPanel.addRow(COURSE.propertyExternalId(), iExternalId);
				iExternalId.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iManager.setExternalId(event.getValue());
					}
				});
				
				iEmailAddress = new TextBox();
				iEmailAddress.setWidth("350px"); iEmailAddress.setMaxLength(200);
				if (iManager.hasEmail()) iEmailAddress.setText(iManager.getEmail());
				iPanel.addRow(COURSE.columnEmailAddress() + ":", iEmailAddress);
				iEmailAddress.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iManager.setEmail(event.getValue());
					}
				});
				
				if (result.hasDepartments()) {
					iPanel.addHeaderRow(COURSE.fieldDepartments());
					iDepartmentsTable = new DepartmentsTable(result.getDepartments()) {
						@Override
						public boolean removeDepartment(Long id) {
							iManager.removeDepartmentId(id);
							return super.removeDepartment(id);
						}
					};
					iDepartments = new ListBox();
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
								iManager.addDepartmentId(id);
							}
						}
					});
					iPanel.addRow("", dp);
					iPanel.addRow("", iDepartmentsTable);
					if (iManager.hasDepartmentIds())
						for (Long id: iManager.getDepartmentIds())
							iDepartmentsTable.addDepartment(id);
					iDepartmentsTable.sort();
				}
				
				if (result.hasSolverGroups()) {
					iPanel.addHeaderRow(COURSE.sectSolverGroups());
					iSolverGroupsTable = new DepartmentsTable(result.getSolverGroups()) {
						@Override
						public boolean removeDepartment(Long id) {
							iManager.removeSolverGroupId(id);
							return super.removeDepartment(id);
						}
					};
					iSolverGroups = new ListBox();
					iSolverGroups.addItem(COURSE.itemSelect(), "");
					for (IdLabel item: result.getSolverGroups())
						iSolverGroups.addItem(item.getLabel(), item.getId().toString());
					P dp = new P("solver-groups-list");
					dp.add(iSolverGroups);
					Button button = new Button(COURSE.actionAddSolverGroup());
					dp.add(button);
					button.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (iSolverGroups.getSelectedIndex() > 0) {
								Long id = Long.valueOf(iSolverGroups.getSelectedValue());
								iSolverGroupsTable.addDepartment(id);
								iManager.addSolverGroupId(id);
							}
						}
					});
					iPanel.addRow("", dp);
					iPanel.addRow("", iSolverGroupsTable);
					if (iManager.hasSolverGroupIds())
						for (Long id: iManager.getSolverGroupIds())
							iSolverGroupsTable.addDepartment(id);
					iSolverGroupsTable.sort();
				}
				
				if (result.hasRoles()) {
					iPanel.addHeaderRow(COURSE.columnRoles());
					iRolesTable = new RolesTable(result.getRoles());
					iRoles = new ListBox();
					iRoles.addItem(COURSE.itemSelect(), "");
					for (IdLabel item: result.getRoles())
						iRoles.addItem(item.getLabel(), item.getId().toString());
					P dp = new P("roles-list");
					dp.add(iRoles);
					Button button = new Button(COURSE.actionAddRole());
					dp.add(button);
					button.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (iRoles.getSelectedIndex() > 0) {
								Long id = Long.valueOf(iRoles.getSelectedValue());
								ManagerRoleInterface mr = new ManagerRoleInterface(id, iRolesTable.getRowCount() == 1, true);
								iRolesTable.addRole(mr);
								iManager.addManagerRole(mr);
							}
						}
					});
					iPanel.addRow("", dp);
					iPanel.addRow("", iRolesTable);
					if (iManager.hasManagerRoles())
						for (ManagerRoleInterface mr: iManager.getManagerRoles())
							iRolesTable.addRole(mr);
					iRolesTable.sort();
				}
				
				if (result.hasMultipleSessions()) {
					iPanel.addHeaderRow(COURSE.columnAcademicSessionsToUpdate());
					P sp = new P("session-list");
					for (final IdLabel item: result.getOtherSessions()) {
						CheckBox ch = new CheckBox(item.getLabel());
						ch.setValue(item.getId().equals(result.getSessionId()) || iManager.hasSessionId(item.getId()));
						ch.setEnabled(!item.getId().equals(result.getSessionId()));
						sp.add(ch);
						ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								if (event.getValue()) {
									iManager.addSessionId(item.getId());
								} else {
									iManager.removeSessionId(item.getId());
								}
							}
						});
					}
					iPanel.addRow("", sp);
				}
				
				iPanel.addBottomRow(iFooter);
			}
		});
	}
	
	protected void saveOrUpdateManager() {
		if (validateManager()) {
			RPC.execute(new TimetableManagerEditRequest(Operation.SAVE, iManager), new AsyncCallback<TimetableManagerEditResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(TimetableManagerEditResponse result) {
					History.newItem(null, false);
					showManagers(result.getManagerId());
				}
			});
		}
	}
	
	protected boolean validateManager() {
		List<String> errors = new ArrayList<String>();
		if (!iManager.hasFirstName())
			errors.add(COURSE.errorRequiredField(COURSE.fieldFirstName()));
		if (!iManager.hasLastName())
			errors.add(COURSE.errorRequiredField(COURSE.fieldLastName()));
		if (!iManager.hasExternalId())
			errors.add(COURSE.errorRequiredField(COURSE.columnExternalId()));
		if (!iManager.hasEmail())
			errors.add(COURSE.errorRequiredField(COURSE.columnEmailAddress()));
		if (!iManager.hasManagerRoles())
			errors.add(COURSE.errorManagerHasNoRoles());
		
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
	
	protected class DepartmentsTable extends UniTimeTable<IdLabel>{
		List<IdLabel> iDepartments;
		
		public DepartmentsTable(List<IdLabel> departments) {
			iDepartments = departments;
		}
		public IdLabel getDepartment(Long id) {
			if (iDepartments == null || id == null) return null;
			for (IdLabel item: iDepartments)
				if (id.equals(item.getId())) return item;
			return null;	
		}
		
		public boolean addDepartment(final Long deptId) {
			IdLabel dept = getDepartment(deptId);
			if (dept == null) return false;
			for (int row = 0; row < getRowCount(); row++)
				if (dept.equals(getData(row))) return false;
			ImageButton remove = new ImageButton(RESOURCES.delete());
			remove.setAltText(ARIA.buttonDeleteThisLine());
			remove.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					removeDepartment(deptId);
				}
			});
			addRow(dept, new Label(dept.getLabel()), remove);
			return true;
		}
		
		public boolean removeDepartment(Long id) {
			for (int row = 0; row < getRowCount(); row++)
				if (id.equals(getData(row).getId())) {
					removeRow(row);
					return true;
				}
			return false;
		}
		
		public void sort() {
			super.sort(null, new Comparator<IdLabel>() {
				@Override
				public int compare(IdLabel o1, IdLabel o2) {
					return o1.compareTo(o2);
				}
			}, true);
		}
	}
	
	protected class RolesTable extends UniTimeTable<ManagerRoleInterface>{
		List<IdLabel> iRoles;
		
		public RolesTable(List<IdLabel> roles) {
			iRoles = roles;
			List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
			header.add(new UniTimeTableHeader(COURSE.columnPrimaryRole()));
			header.add(new UniTimeTableHeader(COURSE.fieldRole()));
			header.add(new UniTimeTableHeader(COURSE.columnReceiveEmails()));
			header.add(new UniTimeTableHeader());
			addRow(null, header);
		}
		public IdLabel getRole(Long id) {
			if (iRoles == null || id == null) return null;
			for (IdLabel item: iRoles)
				if (id.equals(item.getId())) return item;
			return null;	
		}
		
		public boolean addRole(final ManagerRoleInterface mr) {
			IdLabel role = getRole(mr.getRoleId());
			if (role == null) return false;
			for (int row = 1; row < getRowCount(); row++)
				if (mr.equals(getData(row))) return false;
			ImageButton remove = new ImageButton(RESOURCES.delete());
			remove.setAltText(ARIA.buttonDeleteThisLine());
			remove.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					removeRole(mr);
					iManager.removeManagerRole(mr);
				}
			});
			CheckBox primary = new AriaCheckBox(DOM.createInputRadio("primary"));
			primary.setValue(mr.isPrimary());
			Roles.getRadioRole().setAriaLabelledbyProperty(primary.getElement(), Id.of(getCellFormatter().getElement(0, 0)));
			primary.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					for (int row = 1; row < getRowCount(); row++) {
						ManagerRoleInterface x = getData(row);
						x.setPrimary(x.equals(mr)); 
					}
				}
			});
			Element ch = DOM.createInputCheck();
			CheckBox email = new AriaCheckBox(ch);
			email.setValue(mr.isReceiveEmails());
			Roles.getCheckboxRole().setAriaLabelledbyProperty(ch, Id.of(getCellFormatter().getElement(0, 3)));
			email.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					mr.setReceiveEmails(event.getValue());
				}
			});
			 
			addRow(mr, primary, new Label(role.getLabel()), email, remove);
			return true;
		}
		
		public boolean removeRole(ManagerRoleInterface mr) {
			for (int row = 0; row < getRowCount(); row++)
				if (mr.equals(getData(row))) {
					removeRow(row);
					if (mr.isPrimary() && getRowCount() > 1)
						((CheckBox)getWidget(1, 0)).setValue(true, true);
					return true;
				}
			return false;
		}
		
		public void sort() {
			super.sort(null, new Comparator<ManagerRoleInterface>() {
				@Override
				public int compare(ManagerRoleInterface o1, ManagerRoleInterface o2) {
					if (o1.isPrimary() != o2.isPrimary())
						return (o1.isPrimary() ? -1 : 1);
					int cmp = getRole(o1.getRoleId()).compareTo(getRole(o2.getRoleId()));
					if (cmp != 0) return cmp;
					return o1.getRoleId().compareTo(o2.getRoleId());
				}
			}, true);
		}
	}
	
	protected void exportData(String format) {
		String query = "output=managers." + format + "&all=" + (iShowAllManagers.getValue() ? "1" : "0") + "&sort=" + iTable.getSortCookie();
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

	public static class TimetableManagersRequest implements GwtRpcRequest<TimetableManagersResponse> {
		private boolean iShowAllManagers = false;
		private boolean iExport = false;
		
		public TimetableManagersRequest() {}
		public TimetableManagersRequest(boolean showAll) {
			iShowAllManagers = showAll;
		}
		
		public boolean isShowAllManagers() { return iShowAllManagers; }
		public void setShowAllManagers(boolean showAllManagers) { iShowAllManagers = showAllManagers; }
		public boolean isExport() { return iExport; }
		public void setExport(boolean export) { iExport = export; }
	}
	
	public static class TimetableManagersResponse implements GwtRpcResponse {
		private TableInterface iManagersTable;
		private boolean iCanAdd = false;
		
		public TableInterface getManagersTable() { return iManagersTable; }
		public void setManagersTable(TableInterface table) { iManagersTable = table; }
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
	}
	
	public static class TimetableManagerEditRequest implements GwtRpcRequest<TimetableManagerEditResponse> {
		private Long iManagerId;
		private TimetableManagerInterface iManager;
		private Operation iOperation;
		
		public TimetableManagerEditRequest() {}
		public TimetableManagerEditRequest(Operation operation, Long managerId) {
			iOperation = operation; iManagerId = managerId;
		}
		public TimetableManagerEditRequest(Operation operation, TimetableManagerInterface manager) {
			iOperation = operation;
			iManager = manager;
			iManagerId = (manager == null ? null : manager.getManagerId());
		}
		
		public Long getManagerId() { return iManagerId; }
		public void setManagerId(Long managerId) { iManagerId = managerId; }
		public TimetableManagerInterface getManager() { return iManager; }
		public void setManager(TimetableManagerInterface manager) { iManager = manager; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public static enum Operation {
			ADD, EDIT, SAVE, DELETE,
		}
	}
	
	public static class TimetableManagerInterface implements IsSerializable {
		private Long iManagerId;
		private String iFormattedName;
		private String iFirstName, iMiddleName, iLastName, iAcadTitle, iExternalId, iEmail;
		private Set<Long> iDepartmentIds, iSolverGroupIds, iSessionIds;
		private Set<ManagerRoleInterface> iManagerRoles;
		
		public Long getManagerId() { return iManagerId; }
		public void setManagerId(Long managerId) { iManagerId = managerId; }
		
		public boolean hasFormattedName() { return iFormattedName != null && !iFormattedName.isEmpty(); }
		public String getFormattedName() { return iFormattedName; }
		public void setFormattedName(String name) { iFormattedName = name; }
		
		public boolean hasFirstName() { return iFirstName != null && !iFirstName.isEmpty(); }
		public String getFirstName() { return iFirstName; }
		public void setFirstName(String firstName) { iFirstName = firstName; }
		public boolean hasMiddleName() { return iMiddleName != null && !iMiddleName.isEmpty(); }
		public String getMiddleName() { return iMiddleName; }
		public void setMiddleName(String middleName) { iMiddleName = middleName; }
		public boolean hasLastName() { return iLastName != null && !iLastName.isEmpty(); }
		public String getLastName() { return iLastName; }
		public void setLastName(String lastName) { iLastName = lastName; }
		public boolean hasAcadTitle() { return iAcadTitle != null && !iAcadTitle.isEmpty(); }
		public String getAcadTitle() { return iAcadTitle; }
		public void setAcadTitle(String acadTitle) { iAcadTitle = acadTitle; }
		public boolean hasExternalId() { return iExternalId != null && !iExternalId.isEmpty(); }
		public String getExternalId() { return iExternalId; }
		public void setExternalId(String externalId) { iExternalId = externalId; }
		public boolean hasEmail() { return iEmail != null && !iEmail.isEmpty(); }
		public String getEmail() { return iEmail; }
		public void setEmail(String email) { iEmail = email; }
		
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

		public boolean hasSolverGroupIds() { return iSolverGroupIds != null && !iSolverGroupIds.isEmpty(); }
		public void addSolverGroupId(Long id) {
			if (iSolverGroupIds == null) iSolverGroupIds = new HashSet<Long>();
			iSolverGroupIds.add(id);
		}
		public void removeSolverGroupId(Long id) {
			if (iSolverGroupIds != null && id != null) iSolverGroupIds.remove(id);
		}
		public Set<Long> getSolverGroupIds() { return iSolverGroupIds; }
		public boolean hasSolverGroupId(Long id) {
			if (iSolverGroupIds == null) return false;
			return iSolverGroupIds.contains(id);
		}

		public boolean hasManagerRoles() { return iManagerRoles != null && !iManagerRoles.isEmpty(); }
		public void addManagerRole(Long id, boolean primary, boolean receiveEmails) {
			if (iManagerRoles == null) iManagerRoles = new HashSet<ManagerRoleInterface>();
			iManagerRoles.add(new ManagerRoleInterface(id, primary, receiveEmails));
		}
		public void addManagerRole(ManagerRoleInterface role) {
			if (iManagerRoles == null) iManagerRoles = new HashSet<ManagerRoleInterface>();
			iManagerRoles.add(role);
		}
		public void removeManagerRole(ManagerRoleInterface role) {
			if (iManagerRoles != null) iManagerRoles.remove(role);
		}
		public Set<ManagerRoleInterface> getManagerRoles() { return iManagerRoles; }
		public ManagerRoleInterface getManagerRole(Long id) {
			if (iManagerRoles == null || id == null) return null;
			for (ManagerRoleInterface role: iManagerRoles)
				if (id.equals(role.getRoleId())) return role;
			return null;
		}
		
		public boolean hasSessionIds() { return iSessionIds != null && !iSessionIds.isEmpty(); }
		public void addSessionId(Long id) {
			if (iSessionIds == null) iSessionIds = new HashSet<Long>();
			iSessionIds.add(id);
		}
		public void removeSessionId(Long id) {
			if (iSessionIds != null) iSessionIds.remove(id);
		}
		public Set<Long> getSessionIds() { return iSessionIds; }
		public boolean hasSessionId(Long id) {
			if (iSessionIds == null) return false;
			return iSessionIds.contains(id);
		}
	}
	
	public static class ManagerRoleInterface implements IsSerializable {
		private Long iRoleId;
		private boolean iPrimary = false;
		private boolean iRecieveEmails = false;
		
		public ManagerRoleInterface() {}
		public ManagerRoleInterface(Long roleId, boolean primary, boolean recieveEmails) {
			iRoleId = roleId; iPrimary = primary; iRecieveEmails = recieveEmails;
		}
		
		public Long getRoleId() { return iRoleId; }
		public void setRoleId(Long id) { iRoleId = id; }
		public boolean isPrimary() { return iPrimary; }
		public void setPrimary(boolean primary) { iPrimary = primary; }
		public boolean isReceiveEmails() { return iRecieveEmails; }
		public void setReceiveEmails(boolean emails) { iRecieveEmails = emails; }
		
		@Override
		public int hashCode() { return iRoleId.hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ManagerRoleInterface)) return false;
			return getRoleId().equals(((ManagerRoleInterface)o).getRoleId());
		}
	}
	
	public static class TimetableManagerEditResponse implements GwtRpcResponse {
		public TimetableManagerInterface iManager;
		private boolean iCanDelete = false;
		private List<IdLabel> iDepartments, iSolverGroups, iRoles, iOtherSessions;
		private Long iSessionId;
		private String iSessionName;
		
		public TimetableManagerEditResponse() {}

		public TimetableManagerInterface getManager() { return iManager; }
		public void setManager(TimetableManagerInterface manager) { iManager = manager; }
		public Long getManagerId() { return (iManager == null ? null : iManager.getManagerId()); }
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
		
		public void addSolverGroup(Long id, String label) {
			if (iSolverGroups == null) iSolverGroups = new ArrayList<IdLabel>();
			iSolverGroups.add(new IdLabel(id, label));
		}
		public List<IdLabel> getSolverGroups() { return iSolverGroups; }
		public boolean hasSolverGroups() { return iSolverGroups != null && !iSolverGroups.isEmpty(); }
		public IdLabel getSolverGroup(Long id) {
			if (iSolverGroups == null || id == null) return null;
			for (IdLabel item: iSolverGroups)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public void addRole(Long id, String label) {
			if (iRoles == null) iRoles = new ArrayList<IdLabel>();
			iRoles.add(new IdLabel(id, label));
		}
		public List<IdLabel> getRoles() { return iRoles; }
		public boolean hasRoles() { return iRoles != null && !iRoles.isEmpty(); }
		public IdLabel getRole(Long id) {
			if (iRoles == null || id == null) return null;
			for (IdLabel item: iRoles)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public void addOtherSession(Long id, String label) {
			if (iOtherSessions == null) iOtherSessions = new ArrayList<IdLabel>();
			iOtherSessions.add(new IdLabel(id, label));
		}
		public List<IdLabel> getOtherSessions() { return iOtherSessions; }
		public boolean hasOtherSessions() { return iOtherSessions != null && !iOtherSessions.isEmpty(); }
		public boolean hasMultipleSessions() { return iOtherSessions != null && iOtherSessions.size() > 1; }
		public IdLabel getOtherSession(Long id) {
			if (iOtherSessions == null || id == null) return null;
			for (IdLabel item: iOtherSessions)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
	}
}
