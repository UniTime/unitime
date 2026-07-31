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
import org.unitime.timetable.gwt.client.admin.DistributionTypesPage.DistributionTypeEditRequest.Operation;
import org.unitime.timetable.gwt.client.admin.MultiSelect.Item;
import org.unitime.timetable.gwt.client.admin.TimetableManagersPage.DepartmentsTable;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
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
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class DistributionTypesPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iListHeader, iListFooter;
	private TableWidget iTable;
	private UniTimeHeaderPanel iHeader, iFooter;
	private DistributionTypeInterface iType;
	
	public DistributionTypesPage() {
		iPanel = new SimpleForm();
		initWidget(iPanel);
		iPanel.addStyleName("unitime-DistributionTypesPage");
		iListHeader = new UniTimeHeaderPanel();
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
		
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("update", COURSE.actionUpdateDistributionType(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateType(null);
			}
		});
		iHeader.setEnabled("update", false);
		iHeader.addButton("previous", COURSE.actionPreviousDatePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateType(getPrevId(iType.getTypeId()));
			}
		});
		iHeader.setEnabled("previous", false);
		iHeader.addButton("next", COURSE.actionNextDatePattern(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateType(getNextId(iType.getTypeId()));
			}
		});
		iHeader.setEnabled("next", false);
		iHeader.addButton("back", COURSE.actionBackToDistributionTypes(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(null, false);
				showTypes(iType == null ? null : iType.getTypeId());
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
			showTypes();
		else if ("add".equals(token))
			editType(null);
		else {
			try {
				editType(Long.valueOf(token));
			} catch (NumberFormatException e) {
				showTypes();
			}
		}
	}
	
	protected void showTypes() {
		showTypes(null);
	}
	
	protected Long getNextId(Long typeId) {
		if (iTable == null || typeId == null) return null;
		for (int row = 0; row < iTable.getRowCount(); row++) {
			LineInterface line = iTable.getData(row);
			if (line != null && typeId.equals(line.getId())) {
				LineInterface next = iTable.getData(row + 1);
				return (next == null ? null : next.getId());
			}
		}
		return null;
	}
	
	protected Long getPrevId(Long typeId) {
		if (iTable == null || typeId == null) return null;
		for (int row = 0; row < iTable.getRowCount(); row++) {
			LineInterface line = iTable.getData(row);
			if (line != null && typeId.equals(line.getId())) {
				LineInterface prev = iTable.getData(row - 1);
				return (prev == null ? null : prev.getId());
			}
		}
		return null;
	}

	protected void showTypes(final Long patternId) {
		UniTimePageLabel.getInstance().setPageName(MSG.pageDistributionTypes());
		iPanel.clear();
		iListHeader.setEnabled("csv", false);
		iListHeader.setEnabled("pdf", false);
		iPanel.addHeaderRow(iListHeader);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new DistributionTypesRequest(), new AsyncCallback<DistributionTypesResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(DistributionTypesResponse result) {
				LoadingWidget.getInstance().hide();
				iTable.setData(result.getTable());
				iPanel.addRow(iTable);
				iPanel.addBottomRow(iListFooter);
				iListHeader.setHeaderTitle(result.getTable().getName());
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
			}
		});		
	}
	
	private TextBox iAbbreviation, iName;
	private CheckBox iVisible, iInstructor, iSurvey;
	private TextArea iDescription;
	private MultiSelect<Long> iPrefs;
	private ListBox iDepartments;
	private DepartmentsTable iDepartmentsTable;

	protected void editType(Long typeId) {
		Window.scrollTo(0, 0);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new DistributionTypeEditRequest(Operation.EDIT, typeId), new AsyncCallback<DistributionTypeEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToLoadData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(DistributionTypeEditResponse result) {
				LoadingWidget.getInstance().hide();
				iType = result.getType();
				UniTimePageLabel.getInstance().setPageName(MSG.pageEditDistributionType());
				iHeader.setEnabled("update", result.getTypeId() != null);
				iHeader.setEnabled("previous", result.getTypeId() != null && getPrevId(result.getTypeId()) != null);
				iHeader.setEnabled("next", result.getTypeId() != null && getNextId(result.getTypeId()) != null);
				iHeader.setEnabled("back", true);
				
				iHeader.setHeaderTitle(iType.getReference());
				iPanel.clear();
				iHeader.clearMessage();
				iPanel.addHeaderRow(iHeader);
				
				if (iType.getId() != null)
					iPanel.addRow(COURSE.fieldId() + ":", new Label(iType.getTypeId().toString()));
				
				iAbbreviation = new TextBox();
				iAbbreviation.setWidth("400px"); iAbbreviation.setMaxLength(60);
				if (iType.hasAbbreviation()) iAbbreviation.setText(iType.getAbbreviation());
				iPanel.addRow(COURSE.fieldAbbreviation() + ":", iAbbreviation);
				iAbbreviation.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iType.setAbbreviation(event.getValue());
					}
				});
				
				iName = new TextBox();
				iName.setWidth("400px"); iName.setMaxLength(50);
				if (iType.hasName()) iName.setText(iType.getName());
				iPanel.addRow(COURSE.propDatePatternName(), iName);
				iName.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iType.setName(event.getValue());
					}
				});
				
				if (iType.isExam() != null)
					iPanel.addRow(COURSE.fieldType() + ":", new Label(iType.isExam() ? COURSE.itemDistTypeExams() : COURSE.itemDistTypeCourses()));
				
				iVisible = new CheckBox();
				iVisible.setValue(iType.isVisible());
				iPanel.addRow(COURSE.fieldVisible() + ":", iVisible);
				iVisible.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						iType.setVisible(event.getValue());
					}
				});
				
				if (Boolean.FALSE.equals(iType.isExam())) {
					iInstructor = new CheckBox();
					iInstructor.setValue(iType.isInstructor());
					iPanel.addRow(COURSE.fieldAllowInstructorPreference() + ":", iInstructor);
					iInstructor.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							iType.setInstructor(event.getValue());
							if (event.getValue()) {
								iSurvey.setEnabled(true);
							} else {
								iSurvey.setValue(false, true);
								iSurvey.setEnabled(false);
							}
						}
					});
					
					iSurvey = new CheckBox();
					iSurvey.setValue(iType.isSurvey());
					iPanel.addRow(COURSE.fieldAllowInstructorSurvey() + ":", iSurvey);
					iSurvey.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							iType.setSurvey(event.getValue());
						}
					});
					iSurvey.setEnabled(iInstructor.getValue());
				}
				
				if (iType.isSequencing() != null)
					iPanel.addRow(COURSE.fieldSequencingRequired(), new Label(iType.isSequencing() ? COURSE.yes() : COURSE.no()));
				if (result.hasPreferences()) {
					iPrefs = new MultiSelect<Long>();
					for (final IdLabel item: result.getPreferences()) {
						Item ch = iPrefs.addItem(item.getId(), item.getLabel());
						iPrefs.setSelected(item.getId(), iType.hasPreferenceId(item.getId()));
						if (item.hasColor())
							ch.getElement().getStyle().setColor(item.getColor());
						ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								if (event.getValue())
									iType.addPreferenceId(item.getId());
								else
									iType.removePreferenceId(item.getId());
							}
						});
					}
					iPanel.addRow(COURSE.fieldAllowPreferences() + ":", iPrefs);
				}
				
				iDescription = new TextArea();
				iDescription.setStyleName("unitime-TextArea");
				iDescription.setVisibleLines(5);
				iDescription.setCharacterWidth(160);
				if (iType.hasDescription())
					iDescription.setText(iType.getDescription());
				iDescription.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iType.setDescription(event.getValue());
					}
				});
				iPanel.addRow(COURSE.fieldDescription() + ":", iDescription);
				
				if (result.hasDepartments()) {
					iDepartmentsTable = new DepartmentsTable(result.getDepartments()) {
						@Override
						public boolean removeDepartment(Long id) {
							iType.removeDepartmentId(id);
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
								iType.addDepartmentId(id);
							}
						}
					});
					iPanel.addHeaderRow(COURSE.sectRestrictAccess());
					iPanel.addRow(COURSE.propDepartments(), iDepartmentsTable);
					iPanel.addRow("", dp);
					if (iType.hasDepartmentIds())
						for (Long id: iType.getDepartmentIds())
							iDepartmentsTable.addDepartment(id);
					iDepartmentsTable.sort();
				}
				
				iPanel.addBottomRow(iFooter);
			}
		});
	}
	
	protected void updateType(final Long nextTypeId) {
		if (validateType()) {
			RPC.execute(new DistributionTypeEditRequest(Operation.SAVE, iType), new AsyncCallback<DistributionTypeEditResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(DistributionTypeEditResponse result) {
					if (nextTypeId != null) {
						History.newItem(nextTypeId.toString(), false);
						editType(nextTypeId);
					} else {
						History.newItem(null, false);
						showTypes(result.getTypeId());
					}
				}
			});
		}
	}
	
	protected boolean validateType() {
		List<String> errors = new ArrayList<String>();
		if (!iType.hasAbbreviation())
			errors.add(COURSE.errorRequiredField(COURSE.fieldAbbreviation()));
		if (!iType.hasName())
			errors.add(COURSE.errorRequiredField(COURSE.fieldName()));
		
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
		String query = "output=distribution-types." + format + "&sort=" + iTable.getSortCookie();
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

	public static class DistributionTypesRequest implements GwtRpcRequest<DistributionTypesResponse> {
		private boolean iExport = false;

		public boolean isExport() { return iExport; }
		public void setExport(boolean export) { iExport = export; }
	}
	
	public static class DistributionTypesResponse implements GwtRpcResponse {
		private TableInterface iTable;
		
		public TableInterface getTable() { return iTable; }
		public void setTable(TableInterface table) { iTable = table; }
	}
	
	public static class DistributionTypeEditRequest implements GwtRpcRequest<DistributionTypeEditResponse> {
		private Long iTypeId;
		private DistributionTypeInterface iType;
		private Operation iOperation;
		
		public DistributionTypeEditRequest() {}
		public DistributionTypeEditRequest(Operation operation) {
			iOperation = operation;
		}
		public DistributionTypeEditRequest(Operation operation, Long typeId) {
			iOperation = operation; iTypeId = typeId;
		}
		public DistributionTypeEditRequest(Operation operation, DistributionTypeInterface type) {
			iOperation = operation;
			iType = type;
			iTypeId = (type == null ? null : type.getTypeId());
		}
		
		public Long getTypeId() { return iTypeId; }
		public void setTypeId(Long typeId) { iTypeId = typeId; }
		public DistributionTypeInterface getType() { return iType; }
		public void setType(DistributionTypeInterface type) { iType = type; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public static enum Operation {
			EDIT, SAVE
		}
	}
	
	public static class DistributionTypeEditResponse implements GwtRpcResponse {
		private DistributionTypeInterface iType;
		private List<IdLabel> iDepartments, iPreferences;
		
		public DistributionTypeInterface getType() { return iType; }
		public void setType(DistributionTypeInterface type) { iType = type; }
		public Long getTypeId() { return iType == null ? null : iType.getTypeId(); }
		
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
		
		public void addPreference(Long id, String label, String color) {
			if (iPreferences == null) iPreferences = new ArrayList<IdLabel>();
			IdLabel item = new IdLabel(id, label); item.setColor(color);
			iPreferences.add(item);
		}
		public List<IdLabel> getPreferences() { return iPreferences; }
		public boolean hasPreferences() { return iPreferences != null && !iPreferences.isEmpty(); }
		public IdLabel getPreference(Long id) {
			if (iPreferences == null || id == null) return null;
			for (IdLabel item: iPreferences)
				if (id.equals(item.getId())) return item;
			return null;
		}
	}
	
	public static class DistributionTypeInterface implements IsSerializable {
		private Long iTypeId;
		private Integer iId;
		private String iReference, iAbbreviation, iName, iDescription;
		private Boolean iExam, iInstructor, iSurvey, iSequencing, iVisible;
		private Set<Long> iDepartmentIds, iPreferenceIds;
		
		public Long getTypeId() { return iTypeId; }
		public void setTypeId(Long typeId) { iTypeId = typeId; }
		public Integer getId() { return iId; }
		public void setId(Integer id) { iId = id; }
		
		public boolean hasReference() { return iReference != null && !iReference.isEmpty(); }
		public String getReference() { return iReference; }
		public void setReference(String value) { iReference = value; }
		public boolean hasAbbreviation() { return iAbbreviation != null && !iAbbreviation.isEmpty(); }
		public String getAbbreviation() { return iAbbreviation; }
		public void setAbbreviation(String value) { iAbbreviation = value; }
		public boolean hasName() { return iName != null && !iName.isEmpty(); }
		public String getName() { return iName; }
		public void setName(String value) { iName = value; }
		public boolean hasDescription() { return iDescription != null && !iDescription.isEmpty(); }
		public String getDescription() { return iDescription; }
		public void setDescription(String value) { iDescription = value; }
		
		public boolean hasExam() { return iExam != null; }
		public Boolean isExam() { return iExam; }
		public void setExam(Boolean value) { iExam = value; }
		public boolean hasInstructor() { return iInstructor != null; }
		public Boolean isInstructor() { return iInstructor; }
		public void setInstructor(Boolean value) { iInstructor = value; }
		public boolean hasSurvey() { return iSurvey != null; }
		public Boolean isSurvey() { return iSurvey; }
		public void setSurvey(Boolean value) { iSurvey = value; }
		public boolean hasSequencing() { return iSequencing != null; }
		public Boolean isSequencing() { return iSequencing; }
		public void setSequencing(Boolean value) { iSequencing = value; }
		public boolean hasVisible() { return iVisible != null; }
		public Boolean isVisible() { return iVisible; }
		public void setVisible(Boolean value) { iVisible = value; }
		
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
		
		public boolean hasPreferenceIds() { return iPreferenceIds != null && !iPreferenceIds.isEmpty(); }
		public void addPreferenceId(Long id) {
			if (iPreferenceIds == null) iPreferenceIds = new HashSet<Long>();
			iPreferenceIds.add(id);
		}
		public void removePreferenceId(Long id) {
			if (iPreferenceIds != null && id != null) iPreferenceIds.remove(id);
		}
		public Set<Long> getPreferenceIds() { return iPreferenceIds; }
		public boolean hasPreferenceId(Long id) {
			if (iPreferenceIds == null) return false;
			return iPreferenceIds.contains(id);
		}
		
	}
}
