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
import org.unitime.timetable.gwt.client.admin.SolverGroupsPage.SolverGroupEditRequest.Operation;
import org.unitime.timetable.gwt.client.admin.TimetableManagersPage.DepartmentsTable;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SearchableListBox;
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

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class SolverGroupsPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iListHeader, iListFooter;
	private TableWidget iTable;
	private UniTimeHeaderPanel iHeader, iFooter;
	private SolverGroupInterface iGroup;
	
	public SolverGroupsPage() {
		iPanel = new SimpleForm();
		initWidget(iPanel);
		iPanel.addStyleName("unitime-SolverGroupsPage");
		iListHeader = new UniTimeHeaderPanel();
		iListHeader.addButton("add", COURSE.actionAddSolverGroup(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("add", false);
				editGroup(null);
			}
		});
		iListHeader.setEnabled("add", false);
		iListHeader.addButton("delete-all", COURSE.actionDeleteAllSolverGroups(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(COURSE.confirmDeleteAllSolverGroups(), new Command() {
					@Override
					public void execute() {
						RPC.execute(new SolverGroupEditRequest(Operation.DELETE_ALL), new AsyncCallback<SolverGroupEditResponse>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								iHeader.setErrorMessage(MSG.failedToDeleteData(caught.getMessage()));
								UniTimeNotifications.error(MSG.failedToDeleteData(caught.getMessage()), caught);
								ToolBox.checkAccess(caught);
							}

							@Override
							public void onSuccess(SolverGroupEditResponse result) {
								History.newItem(null, false);
								showGroups(null);
							}
						});
					}
				});
			}
		});
		iListHeader.getButton("delete-all").setTitle(COURSE.titleDeleteAllSolverGroups());
		iListHeader.setEnabled("delete-all", false);
		iListHeader.addButton("auto-setup", COURSE.actionAutoSetupSolverGroups(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(COURSE.confirmCreateNewSolverGroups(), new Command() {
					@Override
					public void execute() {
						RPC.execute(new SolverGroupEditRequest(Operation.AUTO_SETUP), new AsyncCallback<SolverGroupEditResponse>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
								UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
								ToolBox.checkAccess(caught);
							}

							@Override
							public void onSuccess(SolverGroupEditResponse result) {
								History.newItem(null, false);
								showGroups(null);
							}
						});
					}
				});
			}
		});
		iListHeader.getButton("auto-setup").setTitle(COURSE.titleAutoSetupSolverGroups());
		iListHeader.setEnabled("auto-setup", false);
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
		iHeader.addButton("save", COURSE.actionSaveManager(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateGroup();
			}
		});
		iHeader.getButton("save").setAccessKey(COURSE.accessSaveManager().charAt(0));
		iHeader.getButton("save").setTitle(COURSE.titleSaveManager(COURSE.accessSaveManager()));
		iHeader.setEnabled("save", false);
		iHeader.addButton("update", COURSE.actionUpdateManager(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateGroup();
			}
		});
		iHeader.getButton("update").setAccessKey(COURSE.accessUpdateManager().charAt(0));
		iHeader.getButton("update").setTitle(COURSE.titleUpdateManager(COURSE.accessUpdateManager()));
		iHeader.setEnabled("update", false);
		
		iHeader.addButton("delete", COURSE.actionDeleteManager(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteGroup();
			}
		});
		iHeader.getButton("delete").setAccessKey(COURSE.accessDeleteManager().charAt(0));
		iHeader.getButton("delete").setTitle(COURSE.titleDeleteManager(COURSE.accessDeleteManager()));
		iHeader.setEnabled("delete", false);
		
		iHeader.addButton("back", COURSE.actionBackToManagers(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(null, false);
				showGroups(iGroup == null ? null : iGroup.getSolverGroupId());
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
			showGroups();
		else if ("add".equals(token))
			editGroup(null);
		else {
			try {
				editGroup(Long.valueOf(token));
			} catch (NumberFormatException e) {
				showGroups();
			}
		}
	}
	
	protected void showGroups() {
		showGroups(null);
	}
	
	protected void showGroups(final Long groupId) {
		UniTimePageLabel.getInstance().setPageName(MSG.pageSolverGroups());
		iPanel.clear();
		iListHeader.setEnabled("add", false);
		iListHeader.setEnabled("delete-all", false);
		iListHeader.setEnabled("auto-setup", false);
		iListHeader.setEnabled("csv", false);
		iListHeader.setEnabled("pdf", false);
		iPanel.addHeaderRow(iListHeader);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new SolverGroupsRequest(), new AsyncCallback<SolverGroupsResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(SolverGroupsResponse result) {
				LoadingWidget.getInstance().hide();
				iTable.setData(result.getSolverGroupsTable());
				iPanel.addRow(iTable);
				iPanel.addBottomRow(iListFooter);
				iListHeader.setHeaderTitle(result.getSolverGroupsTable().getName());
				iListHeader.setEnabled("add", result.isCanAdd());
				iListHeader.setEnabled("delete-all", result.isCanDeleteAll());
				iListHeader.setEnabled("auto-setup", result.isCanAutoSetup());
				if (groupId != null)
					for (int row = 1; row < iTable.getRowCount(); row ++) {
						LineInterface line = iTable.getData(row);
						if (line != null && groupId.equals(line.getId())) {
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
	
	protected void deleteGroup() {
		UniTimeConfirmationDialog.confirm(COURSE.confirmDeleteSolverGroup(), new Command() {
			@Override
			public void execute() {
				RPC.execute(new SolverGroupEditRequest(Operation.DELETE, iGroup.getSolverGroupId()), new AsyncCallback<SolverGroupEditResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MSG.failedToDeleteData(caught.getMessage()));
						UniTimeNotifications.error(MSG.failedToDeleteData(caught.getMessage()), caught);
						ToolBox.checkAccess(caught);					
					}

					@Override
					public void onSuccess(SolverGroupEditResponse result) {
						History.newItem(null, false);
						showGroups(null);
					}
				});
			}
		});
	}
	
	private TextBox iAbbv, iName;
	private ListBox iDepartments, iManagers;
	private DepartmentsTable iDepartmentsTable, iManagersTable;
	
	protected void editGroup(Long groupId) {
		Window.scrollTo(0, 0);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new SolverGroupEditRequest(groupId == null ? Operation.ADD : Operation.EDIT, groupId), new AsyncCallback<SolverGroupEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToLoadData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);				
			}

			@Override
			public void onSuccess(SolverGroupEditResponse result) {
				LoadingWidget.getInstance().hide();
				UniTimePageLabel.getInstance().setPageName(result.getSolverGroupId() == null ? MSG.pageAddSolverGroup() : MSG.pageEditSolverGroup());
				iHeader.setEnabled("save", result.getSolverGroupId() == null);
				iHeader.setEnabled("update", result.getSolverGroupId() != null);
				iHeader.setEnabled("delete", result.isCanDelete() && result.getSolverGroupId() != null);
				iHeader.setEnabled("back", true);
				iGroup = result.getSolverGroup();
				if (iGroup == null) iGroup = new SolverGroupInterface();
				
				iHeader.setHeaderTitle(result.getSolverGroupId() == null ? COURSE.sectAddSolverGroup() : COURSE.sectEditSolverGroup());
				iPanel.clear();
				iHeader.clearMessage();
				iPanel.addHeaderRow(iHeader);
				
				iAbbv = new TextBox();
				iAbbv.setWidth("350px"); iAbbv.setMaxLength(50);
				if (iGroup.hasAbbreviation()) iAbbv.setText(iGroup.getAbbreviation());
				iPanel.addRow(COURSE.fieldAbbreviation() + ":", iAbbv);
				iAbbv.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iGroup.setAbbreviation(event.getValue());
					}
				});
				
				iName = new TextBox();
				iName.setWidth("350px"); iName.setMaxLength(50);
				if (iGroup.hasName()) iName.setText(iGroup.getName());
				iPanel.addRow(COURSE.fieldName() + ":", iName);
				iName.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iGroup.setName(event.getValue());
					}
				});
				
				if (result.hasDepartments()) {
					iPanel.addHeaderRow(COURSE.fieldDepartments());
					if (result.isCanEditDepartments()) {
						iDepartmentsTable = new DepartmentsTable(result.getDepartments()) {
							@Override
							public boolean removeDepartment(Long id) {
								iGroup.removeDepartmentId(id);
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
									iGroup.addDepartmentId(id);
									if (!iGroup.hasAbbreviation() || !iGroup.hasName()) {
										IdLabel dept = iDepartmentsTable.getDepartment(id);
										if (dept != null && !iGroup.hasAbbreviation()) {
											String code = dept.getLabel();
											if (code.indexOf(" - ") >= 0)
												code = code.substring(0, code.indexOf(" - "));
											iAbbv.setValue(code, true);
										}
										if (dept != null && !iGroup.hasName())
											iName.setValue(dept.getLabel(), true);
									}
								}
							}
						});
						iPanel.addRow("", iDepartmentsTable);
						iPanel.addRow("", dp);
						if (iGroup.hasDepartmentIds())
							for (Long id: iGroup.getDepartmentIds())
								iDepartmentsTable.addDepartment(id);
						iDepartmentsTable.sort();
					} else {
						for (IdLabel item: result.getDepartments())
							if (iGroup.hasDepartmentId(item.getId()))
								iPanel.addRow("", new Label(item.getLabel()));
					}
				}
				
				if (result.hasManagers()) {
					iPanel.addHeaderRow(COURSE.fieldManagers());
					iManagersTable = new DepartmentsTable(result.getManagers()) {
						@Override
						public boolean removeDepartment(Long id) {
							iGroup.removeManagerId(id);
							return super.removeDepartment(id);
						}
					};
					iManagers = new ListBox();
					iManagers.addItem("", "");
					for (IdLabel item: result.getManagers())
						iManagers.addItem(item.getLabel(), item.getId().toString());
					P dp = new P("managers-list");
					SearchableListBox mgrSearch = new SearchableListBox(iManagers); 
					Roles.getListboxRole().setAriaLabelProperty(mgrSearch.getElement(), ARIA.listSelectItem(COURSE.columnTimetableManager()));
					dp.add(mgrSearch);
					Button button = new Button(COURSE.actionAddTimetableManager());
					dp.add(button);
					button.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (iManagers.getSelectedIndex() > 0) {
								Long id = Long.valueOf(iManagers.getSelectedValue());
								iManagersTable.addDepartment(id);
								iGroup.addManagerId(id);
							}
						}
					});
					iPanel.addRow("", iManagersTable);
					iPanel.addRow("", dp);
					if (iGroup.hasManagerIds())
						for (Long id: iGroup.getManagerIds())
							iManagersTable.addDepartment(id);
					iManagersTable.sort();
				}
				
				iPanel.addBottomRow(iFooter);
			}
		});
	}
	
	protected void saveOrUpdateGroup() {
		if (validateGroup()) {
			RPC.execute(new SolverGroupEditRequest(Operation.SAVE, iGroup), new AsyncCallback<SolverGroupEditResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(SolverGroupEditResponse result) {
					History.newItem(null, false);
					showGroups(result.getSolverGroupId());
				}
			});
		}
	}
	
	protected boolean validateGroup() {
		List<String> errors = new ArrayList<String>();
		if (!iGroup.hasAbbreviation())
			errors.add(COURSE.errorRequiredField(COURSE.fieldAbbreviation()));
		if (!iGroup.hasName())
			errors.add(COURSE.errorRequiredField(COURSE.fieldName()));
		if (!iGroup.hasDepartmentIds())
			errors.add(COURSE.errorRequiredField(COURSE.fieldDepartments()));
		
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
	
	protected void exportData(String format) {
		String query = "output=solver-groups." + format + "&sort=" + iTable.getSortCookie();
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
	
	public static class SolverGroupsRequest implements GwtRpcRequest<SolverGroupsResponse> {
	}
	
	public static class SolverGroupsResponse implements GwtRpcResponse {
		private TableInterface iSolverGroupsTable;
		private boolean iCanAdd = false, iCanAutoSetup = false, iCanDeleteAll = false;
		
		public TableInterface getSolverGroupsTable() { return iSolverGroupsTable; }
		public void setSolverGroupsTable(TableInterface table) { iSolverGroupsTable = table; }
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
		public boolean isCanAutoSetup() { return iCanAutoSetup; }
		public void setCanAutoSetup(boolean canAutoSetup) { iCanAutoSetup = canAutoSetup; }
		public boolean isCanDeleteAll() { return iCanDeleteAll; }
		public void setCanDeleteAll(boolean canDeleteAll) { iCanDeleteAll = canDeleteAll; }
	}
	
	public static class SolverGroupEditRequest implements GwtRpcRequest<SolverGroupEditResponse> {
		private Long iSolverGroupId;
		private SolverGroupInterface iSolverGroup;
		private Operation iOperation;
		
		public SolverGroupEditRequest() {}
		public SolverGroupEditRequest(Operation operation) {
			iOperation = operation;
		}
		public SolverGroupEditRequest(Operation operation, Long solverGroupId) {
			iOperation = operation; iSolverGroupId = solverGroupId;
		}
		public SolverGroupEditRequest(Operation operation, SolverGroupInterface solverGroup) {
			iOperation = operation;
			iSolverGroup = solverGroup;
			iSolverGroupId = (solverGroup == null ? null : solverGroup.getSolverGroupId());
		}
		
		public Long getSolverGroupId() { return iSolverGroupId; }
		public void setSolverGroupId(Long managerId) { iSolverGroupId = managerId; }
		public SolverGroupInterface getSolverGroup() { return iSolverGroup; }
		public void setSolverGroup(SolverGroupInterface manager) { iSolverGroup = manager; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }

		
		public static enum Operation {
			ADD, EDIT, SAVE, DELETE,
			AUTO_SETUP, DELETE_ALL,
		}
	}
	
	public static class SolverGroupEditResponse implements GwtRpcResponse {
		private SolverGroupInterface iSolverGroup;
		private Long iSessionId;
		private String iSessionName;
		private List<IdLabel> iDepartments, iManagers;
		private boolean iCanDelete = false, iCanEditDepartments = true;
		
		public SolverGroupInterface getSolverGroup() { return iSolverGroup; }
		public void setSolverGroup(SolverGroupInterface manager) { iSolverGroup = manager; }
		public Long getSolverGroupId() { return iSolverGroup == null ? null : iSolverGroup.getSolverGroupId(); }
		
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
		
		public void addManager(Long id, String label) {
			if (iManagers == null) iManagers = new ArrayList<IdLabel>();
			iManagers.add(new IdLabel(id, label));
		}
		public List<IdLabel> getManagers() { return iManagers; }
		public boolean hasManagers() { return iManagers != null && !iManagers.isEmpty(); }
		public IdLabel getManager(Long id) {
			if (iManagers == null || id == null) return null;
			for (IdLabel item: iManagers)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
		public boolean isCanEditDepartments() { return iCanEditDepartments; }
		public void setCanEditDepartments(boolean canEditDepartments) { iCanEditDepartments = canEditDepartments; }

	}
	
	public static class SolverGroupInterface implements IsSerializable {
		private Long iSolverGroupId;
		private String iName, iAbbreviation;
		private Set<Long> iDepartmentIds, iManagerIds;
		
		public Long getSolverGroupId() { return iSolverGroupId; }
		public void setSolverGroupId(Long managerId) { iSolverGroupId = managerId; }
		
		public boolean hasName() { return iName != null && !iName.isEmpty(); }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public boolean hasAbbreviation() { return iAbbreviation != null && !iAbbreviation.isEmpty(); }
		public String getAbbreviation() { return iAbbreviation; }
		public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }
		
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
		
		public boolean hasManagerIds() { return iManagerIds != null && !iManagerIds.isEmpty(); }
		public void addManagerId(Long id) {
			if (iManagerIds == null) iManagerIds = new HashSet<Long>();
			iManagerIds.add(id);
		}
		public void removeManagerId(Long id) {
			if (iManagerIds != null && id != null) iManagerIds.remove(id);
		}
		public Set<Long> getManagerIds() { return iManagerIds; }
		public boolean hasManagerId(Long id) {
			if (iManagerIds == null) return false;
			return iManagerIds.contains(id);
		}
	}

}
