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
package org.unitime.timetable.gwt.client.instructor;

import java.util.ArrayList;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.rooms.RoomHint;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributesColumn;
import org.unitime.timetable.gwt.shared.InstructorInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.GetInstructorAttributesRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.GetInstructorsRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorAttributePropertiesInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorAttributePropertiesRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.SetLastDepartmentRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Tomas Muller
 */
public class InstructorAttributesPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private ListBox iFilter = null;
	private SimpleForm iAttributesPanel = null;
	private SimplePanel iRootPanel;
	
	private SimplePanel iPanel = null;
	private UniTimeHeaderPanel iFilterPanel = null;
	private InstructorAttributePropertiesInterface iProperties = null;
	private UniTimeHeaderPanel iGlobalAttributesHeader = null;
	private InstructorAttributesTable iGlobalAttributesTable = null;
	private int iGlobalAttributesRow = -1;
	private UniTimeHeaderPanel iDepartmentalAttributesHeader = null;
	private InstructorAttributesTable iDepartmentalAttributesTable = null;
	private int iDepartmentalAttributesRow = -1;
	
	private InstructorAttributeEdit iInstructorAttributeEdit = null;
	
	public InstructorAttributesPage() {
		iPanel = new SimplePanel();
		
		iAttributesPanel = new SimpleForm();
		iAttributesPanel.setWidth("100%");
		iAttributesPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		ClickHandler clickSearch = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				search(null);
			}
		};
		
		ClickHandler clickNew = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				edit(null);
			}
		};
		
		iInstructorAttributeEdit = null;
		
		iFilterPanel = new UniTimeHeaderPanel(MESSAGES.propDepartment());
		
		iFilter = new ListBox();
		iFilter.setStyleName("unitime-TextBox");
		iFilterPanel.insertLeft(iFilter, false);
		iFilter.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				DepartmentInterface dept = getDepartment();
				iFilterPanel.setEnabled("search", dept != null && dept.isCanSeeAttributes());
				iFilterPanel.setEnabled("new", iProperties != null && (iProperties.isCanAddGlobalAttribute() || (dept != null && dept.isCanAddAttribute())));
			}
		});
		iFilter.getElement().getStyle().setMarginLeft(5, Unit.PX);
		
		iFilterPanel.addButton("search", MESSAGES.buttonSearch(), clickSearch);
		iFilterPanel.addButton("new", MESSAGES.buttonAddNewInstructorAttribute(), clickNew);
		iFilterPanel.setEnabled("search", false);
		iFilterPanel.setEnabled("new", false);
		
		int filterRow = iAttributesPanel.addHeaderRow(iFilterPanel);
		iAttributesPanel.getCellFormatter().setHorizontalAlignment(filterRow, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		setup();
		
		iGlobalAttributesHeader = new UniTimeHeaderPanel(MESSAGES.headerGlobalInstructorAttributes());
		iGlobalAttributesRow = iAttributesPanel.addHeaderRow(iGlobalAttributesHeader);
		iGlobalAttributesTable = new InstructorAttributesTable() {
			protected void doSort(AttributesColumn column) {
				super.doSort(column);
				iDepartmentalAttributesTable.setSortBy(InstructorCookie.getInstance().getSortAttributesBy());
			}
		};
		iAttributesPanel.addRow(iGlobalAttributesTable);
		iAttributesPanel.getRowFormatter().setVisible(iGlobalAttributesRow, false);
		iAttributesPanel.getRowFormatter().setVisible(iGlobalAttributesRow + 1, false);
		
		iDepartmentalAttributesHeader = new UniTimeHeaderPanel(MESSAGES.headerDepartmentalInstructorAttributes());
		iDepartmentalAttributesRow = iAttributesPanel.addHeaderRow(iDepartmentalAttributesHeader);
		iDepartmentalAttributesTable = new InstructorAttributesTable() {
			protected void doSort(AttributesColumn column) {
				super.doSort(column);
				iGlobalAttributesTable.setSortBy(InstructorCookie.getInstance().getSortAttributesBy());
			}
		};
		iAttributesPanel.addRow(iDepartmentalAttributesTable);
		iAttributesPanel.getRowFormatter().setVisible(iDepartmentalAttributesRow, false);
		iAttributesPanel.getRowFormatter().setVisible(iDepartmentalAttributesRow + 1, false);
		
		iRootPanel = new SimplePanel(iAttributesPanel);
		iPanel.setWidget(iRootPanel);
		
		iGlobalAttributesTable.addMouseClickListener(new MouseClickListener<AttributeInterface>() {
			@Override
			public void onMouseClick(final TableEvent<AttributeInterface> event) {
				if (event.getData() != null && (event.getData().canAssign() || event.getData().canDelete()))
					edit(event.getData());
			}
		});
		
		iDepartmentalAttributesTable.addMouseClickListener(new MouseClickListener<AttributeInterface>() {
			@Override
			public void onMouseClick(final TableEvent<AttributeInterface> event) {
				if (event.getData() != null && (event.getData().canAssign() || event.getData().canDelete()))
					edit(event.getData());
			}
		});
		
		initWidget(iPanel);
	}
	
	protected DepartmentInterface getDepartment() {
		if (iFilter.getSelectedIndex() >= 0)
			return iProperties.getDepartment(Long.valueOf(iFilter.getValue(iFilter.getSelectedIndex())));
		return null;
	}
	
	private void edit(AttributeInterface attribute) {
		if (iInstructorAttributeEdit == null) return;
		DepartmentInterface dept = getDepartment();
		iInstructorAttributeEdit.setAttribute(attribute, dept);
		if (dept == null) {
			iFilterPanel.clearMessage();
			iInstructorAttributeEdit.setInstructors(new ArrayList<InstructorInterface>());
			iInstructorAttributeEdit.show();
		} else {
			GetInstructorsRequest request = new GetInstructorsRequest();
			request.setDepartmentId(dept.getId());
			LoadingWidget.execute(request, new AsyncCallback<GwtRpcResponseList<InstructorInterface>>() {
				@Override
				public void onFailure(Throwable caught) {
					iFilterPanel.setErrorMessage(MESSAGES.failedToLoadRooms(caught.getMessage()));
					UniTimeNotifications.error(MESSAGES.failedToLoadRooms(caught.getMessage()), caught);
				}
				@Override
				public void onSuccess(GwtRpcResponseList<InstructorInterface> result) {
					iFilterPanel.clearMessage();
					iInstructorAttributeEdit.setInstructors(result);
					iInstructorAttributeEdit.show();
				}
			}, MESSAGES.waitLoadingInstructors());
		}
	}
	
	protected void setup() {
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingPage());
		RPC.execute(new InstructorAttributePropertiesRequest(), new AsyncCallback<InstructorAttributePropertiesInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilterPanel.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(InstructorAttributePropertiesInterface result) {
				LoadingWidget.getInstance().hide();
				iProperties = result;
				iInstructorAttributeEdit = new InstructorAttributeEdit(iProperties) {
					@Override
					protected void onShow() {
						RoomHint.hideHint();
						iRootPanel.setWidget(iInstructorAttributeEdit);
						Client.fireGwtPageChanged(new Client.GwtPageChangeEvent());
					}
					
					@Override
					protected void onHide(boolean refresh, AttributeInterface attribute) {
						iRootPanel.setWidget(iAttributesPanel);
						UniTimePageLabel.getInstance().setPageName(MESSAGES.pageInstructorAttributes());
						Client.fireGwtPageChanged(new Client.GwtPageChangeEvent());
						if (refresh && (iAttributesPanel.getRowFormatter().isVisible(iGlobalAttributesRow) || iAttributesPanel.getRowFormatter().isVisible(iDepartmentalAttributesRow))) search(attribute == null ? null : attribute.getId());
					}					
				};
				
				iFilter.clear();
				iFilter.addItem(MESSAGES.itemSelect(), "-1");
				iFilter.setSelectedIndex(0);
				for (DepartmentInterface d: iProperties.getDepartments()) {
					if (d.isCanSeeAttributes()) {
						iFilter.addItem(d.getDeptCode() + " - " + d.getLabel(), d.getId().toString());
						if (d.getId().equals(iProperties.getLastDepartmentId()))
							iFilter.setSelectedIndex(iFilter.getItemCount() - 1);
					}
				}
				
				DepartmentInterface dept = getDepartment();
				iFilterPanel.setEnabled("search", dept != null && dept.isCanSeeAttributes());
				iFilterPanel.setEnabled("new", iProperties != null && (iProperties.isCanAddGlobalAttribute() || (dept != null && dept.isCanAddAttribute())));
			}
		});
	}
		
	protected void hideResults() {
		iGlobalAttributesTable.clearTable(1);
		iDepartmentalAttributesTable.clearTable(1);
		iAttributesPanel.getRowFormatter().setVisible(iGlobalAttributesRow, false);
		iAttributesPanel.getRowFormatter().setVisible(iGlobalAttributesRow + 1, false);
		iAttributesPanel.getRowFormatter().setVisible(iDepartmentalAttributesRow, false);
		iAttributesPanel.getRowFormatter().setVisible(iDepartmentalAttributesRow + 1, false);
	}
	
	protected void search(final Long attributeId) {
		hideResults();
		final DepartmentInterface dept = getDepartment();
		GetInstructorAttributesRequest request = new GetInstructorAttributesRequest();
		if (dept != null) {
			request.setDepartmentId(dept.getId());
			RPC.execute(new SetLastDepartmentRequest(dept.getId()), new AsyncCallback<GwtRpcResponseNull>() {
				@Override
				public void onFailure(Throwable caught) {}
				@Override
				public void onSuccess(GwtRpcResponseNull result) {}
			});
		}
		LoadingWidget.execute(request, new AsyncCallback<GwtRpcResponseList<AttributeInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				iFilterPanel.setErrorMessage(MESSAGES.failedToLoadInstructorAttributes(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadInstructorAttributes(caught.getMessage()), caught);
			}
			@Override
			public void onSuccess(GwtRpcResponseList<AttributeInterface> result) {
				iFilterPanel.clearMessage();
				if (result == null || result.isEmpty()) {
					iFilterPanel.setErrorMessage(MESSAGES.errorNoInstructorAttributes());
				} else {
					for (AttributeInterface attribute: result) {
						if (attribute.isDepartmental()) {
							if (dept != null && !dept.equals(attribute.getDepartment())) continue;
							iDepartmentalAttributesTable.addAttribute(attribute);
						} else {
							iGlobalAttributesTable.addAttribute(attribute);
						}
					}
					iDepartmentalAttributesTable.sort();
					iGlobalAttributesTable.sort();
				}
				iAttributesPanel.getRowFormatter().setVisible(iGlobalAttributesRow, iGlobalAttributesTable.getRowCount() > 1);
				iAttributesPanel.getRowFormatter().setVisible(iGlobalAttributesRow + 1, iGlobalAttributesTable.getRowCount() > 1);
				iAttributesPanel.getRowFormatter().setVisible(iDepartmentalAttributesRow, iDepartmentalAttributesTable.getRowCount() > 1);
				iAttributesPanel.getRowFormatter().setVisible(iDepartmentalAttributesRow + 1, iDepartmentalAttributesTable.getRowCount() > 1);
				iDepartmentalAttributesTable.scrollTo(attributeId);
				iGlobalAttributesTable.scrollTo(attributeId);
			}
		}, MESSAGES.waitLoadingInstructorAttributes());
	}
	
	protected void export(String format) {
		RPC.execute(EncodeQueryRpcRequest.encode(query(format)), new AsyncCallback<EncodeQueryRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(EncodeQueryRpcResponse result) {
				ToolBox.open(GWT.getHostPageBaseURL() + "export?q=" + result.getQuery());
			}
		});
	}
	
	protected String query(String format) {
		InstructorCookie cookie = InstructorCookie.getInstance();
		String query = "output=" + format + "&sort=" + cookie.getSortAttributesBy();
		DepartmentInterface dept = getDepartment();
		if (dept != null)
			query += "&department=" + dept.getId();
		return query;
	}
}