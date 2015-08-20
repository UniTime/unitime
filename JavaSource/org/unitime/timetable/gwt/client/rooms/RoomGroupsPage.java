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
package org.unitime.timetable.gwt.client.rooms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.rooms.RoomsTable.DeptMode;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomGroupsColumn;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsPageMode;
import org.unitime.timetable.gwt.shared.RoomInterface.SearchRoomGroupsRequest;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;

/**
 * @author Tomas Muller
 */
public class RoomGroupsPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private Session iSession = null;
	private RoomFilterBox iFilter = null;
	private AriaButton iSearch = null;
	private AriaButton iNew = null;
	private AriaButton iMore = null;	

	private SimpleForm iGroupsPanel = null;
	private SimplePanel iRootPanel;
	
	private SimplePanel iPanel = null;
	private HorizontalPanel iFilterPanel = null;
	private RoomPropertiesInterface iProperties = null;
	private UniTimeHeaderPanel iGlobalGroupsHeader = null;
	private RoomGroupsTable iGlobalGroupsTable = null;
	private int iGlobalGroupsRow = -1;
	private UniTimeHeaderPanel iDepartmentalGroupsHeader = null;
	private RoomGroupsTable iDepartmentalGroupsTable = null;
	private int iDepartmentalGroupsRow = -1;
	private RoomsPageMode iMode = RoomsPageMode.COURSES;
	
	private RoomGroupEdit iRoomGroupEdit = null;
	
	public RoomGroupsPage() {
		if (Location.getParameter("mode") != null)
			iMode = RoomsPageMode.valueOf(Location.getParameter("mode").toUpperCase());
		
		iPanel = new SimplePanel();
		
		iGroupsPanel = new SimpleForm();
		
		iFilterPanel = new HorizontalPanel();
		iFilterPanel.setSpacing(3);
		
		Label filterLabel = new Label(MESSAGES.propFilter());
		iFilterPanel.add(filterLabel);
		iFilterPanel.setCellVerticalAlignment(filterLabel, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iSession = new Session();
		iFilter = new RoomFilterBox(iSession);
		iFilterPanel.add(iFilter);
		
		iSearch = new AriaButton(MESSAGES.buttonSearch());
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iSearch);
		
		iMore = new AriaButton(MESSAGES.buttonMoreOperations());
		iMore.setEnabled(false);
		iMore.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iMore);

		iNew = new AriaButton(MESSAGES.buttonAddNewRoomGroup());
		iNew.setEnabled(false);
		iNew.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iNew);
		
		int filterRow = iGroupsPanel.addRow(iFilterPanel);
		iGroupsPanel.getCellFormatter().setHorizontalAlignment(filterRow, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		iGroupsPanel.setWidth("100%");
		
		iGlobalGroupsHeader = new UniTimeHeaderPanel(MESSAGES.headerGlobalRoomGroups());
		iGlobalGroupsRow = iGroupsPanel.addHeaderRow(iGlobalGroupsHeader);
		iGlobalGroupsTable = new RoomGroupsTable(true) {
			protected void doSort(RoomGroupsColumn column) {
				super.doSort(column);
				iDepartmentalGroupsTable.setSortBy(RoomCookie.getInstance().getRoomGroupsSortBy());
			}
		};
		iGroupsPanel.addRow(iGlobalGroupsTable);
		iGroupsPanel.getRowFormatter().setVisible(iGlobalGroupsRow, false);
		iGroupsPanel.getRowFormatter().setVisible(iGlobalGroupsRow + 1, false);
		
		iDepartmentalGroupsHeader = new UniTimeHeaderPanel(MESSAGES.headerDepartmentalRoomGroups());
		iDepartmentalGroupsRow = iGroupsPanel.addHeaderRow(iDepartmentalGroupsHeader);
		iDepartmentalGroupsTable = new RoomGroupsTable(false) {
			protected void doSort(RoomGroupsColumn column) {
				super.doSort(column);
				iGlobalGroupsTable.setSortBy(RoomCookie.getInstance().getRoomGroupsSortBy());
			}
		};
		iGroupsPanel.addRow(iDepartmentalGroupsTable);
		iGroupsPanel.getRowFormatter().setVisible(iDepartmentalGroupsRow, false);
		iGroupsPanel.getRowFormatter().setVisible(iDepartmentalGroupsRow + 1, false);
		
		iRootPanel = new SimplePanel(iGroupsPanel);
		iPanel.setWidget(iRootPanel);
		
		initWidget(iPanel);
		
		iSearch.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				changeUrl();
				search();
			}
		});
		
		iMore.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				
				MenuBar sortItems = new MenuBar(true);
				for (final RoomGroupsColumn column: RoomGroupsColumn.values()) {
					if (RoomGroupsComparator.isApplicable(column)) {
						if ((iGlobalGroupsTable.getRowCount() <= 1 || iGlobalGroupsTable.getNbrCells(column) == 0) &&
							(iDepartmentalGroupsTable.getRowCount() <= 1 || iDepartmentalGroupsTable.getNbrCells(column) == 0)) continue;
						String name = iGlobalGroupsTable.getColumnName(column, 0).replace("<br>", " ");
						MenuItem item = new MenuItem(name, true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								int sortBy = RoomCookie.getInstance().getRoomGroupsSortBy();
								if (sortBy == (1 + column.ordinal()) || sortBy == (-1 - column.ordinal())) {
									sortBy = -sortBy;
								} else {
									sortBy = 1 + column.ordinal();
								}
								RoomCookie.getInstance().setSortRoomGroupsBy(sortBy);
								iGlobalGroupsTable.setSortBy(sortBy);
								iDepartmentalGroupsTable.setSortBy(sortBy);
							}
						});
						Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), UniTimeHeaderPanel.stripAccessKey(MESSAGES.opSortBy(name)));
						sortItems.addItem(item);
					}
				}
				MenuItem sortMenu = new MenuItem(MESSAGES.opSort(), sortItems);
				sortMenu.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(sortMenu);
				
				MenuBar deptItems = new MenuBar(true);
				for (final DeptMode d: DeptMode.values()) {
					String name = (RoomCookie.getInstance().getDeptMode() == d.ordinal() ? MESSAGES.opUncheck(d.getName()) : MESSAGES.opCheck(d.getName()));
					MenuItem item = new MenuItem(name, true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							RoomCookie.getInstance().setDeptMode(d.ordinal());
							iGlobalGroupsTable.refreshTable();
							iDepartmentalGroupsTable.refreshTable();
						}
					});
					Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), d.getName());
					deptItems.addItem(item);
				}
				MenuItem deptMenu = new MenuItem(MESSAGES.opDepartmentFormat(), deptItems);
				deptMenu.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(deptMenu);
				
				if (iProperties.isCanExportRoomGroups()) {
					menu.addSeparator();
					
					MenuItem exportPdf = new MenuItem(MESSAGES.opExportPDF(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							export("roomgroups.pdf");
						}
					});
					Roles.getMenuitemRole().setAriaLabelProperty(exportPdf.getElement(), MESSAGES.opExportPDF());
					menu.addItem(exportPdf);
					
					MenuItem exportCsv = new MenuItem(MESSAGES.opExportCSV(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							export("roomgroups.csv");
						}
					});
					Roles.getMenuitemRole().setAriaLabelProperty(exportCsv.getElement(), MESSAGES.opExportCSV());
					menu.addItem(exportCsv);
				}
				
				popup.add(menu);
				popup.showRelativeTo((UIObject)event.getSource());
				menu.focus();
			}
		});
		
		iNew.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iRoomGroupEdit == null) return;
				Chip dept = iFilter.getChip("department");
				iRoomGroupEdit.setGroup(null, dept == null ? null : dept.getValue());
				LoadingWidget.execute(iFilter.getElementsRequest(), new AsyncCallback<FilterRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						iFilter.setErrorHint(MESSAGES.failedToLoadRooms(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedToLoadRooms(caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(FilterRpcResponse result) {
						iFilter.clearHint();
						if (result == null || result.getResults() == null || result.getResults().isEmpty()) {
							iFilter.setErrorHint(MESSAGES.errorNoRoomsMatchingFilter());
						} else {
							iRoomGroupEdit.setRooms(result.getResults());
							iRoomGroupEdit.show();
						}
					}
				}, MESSAGES.waitLoadingRooms());
			}
		});
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iFilter.setValue(event.getValue(), true);
				if (iGroupsPanel.getRowFormatter().isVisible(iGlobalGroupsRow) || iGroupsPanel.getRowFormatter().isVisible(iDepartmentalGroupsRow))
					search();
			}
		});
		
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingPage());
		RPC.execute(new RoomPropertiesRequest(), new AsyncCallback<RoomPropertiesInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilter.setErrorHint(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(RoomPropertiesInterface result) {
				LoadingWidget.getInstance().hide();
				iProperties = result;
				iSession.fireChange();
				initialize();
			}
		});
	}
	
	protected void changeUrl() {
		String token = null;
		token = iFilter.getValue();
		RoomCookie.getInstance().setHash(iMode, token);
		if (!History.getToken().equals(token))
			History.newItem(token, false);
		Client.fireGwtPageChanged(new Client.GwtPageChangeEvent());
	}
	
	protected void search() {
		iMore.setEnabled(false);
		iGlobalGroupsTable.clearTable(1);
		iDepartmentalGroupsTable.clearTable(1);
		iGroupsPanel.getRowFormatter().setVisible(iGlobalGroupsRow, false);
		iGroupsPanel.getRowFormatter().setVisible(iGlobalGroupsRow + 1, false);
		iGroupsPanel.getRowFormatter().setVisible(iDepartmentalGroupsRow, false);
		iGroupsPanel.getRowFormatter().setVisible(iDepartmentalGroupsRow + 1, false);
		SearchRoomGroupsRequest request = new SearchRoomGroupsRequest();
		request.setFilter(iFilter.getElementsRequest());
		LoadingWidget.execute(request, new AsyncCallback<GwtRpcResponseList<GroupInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				iFilter.setErrorHint(MESSAGES.failedToLoadRoomGroups(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadRoomGroups(caught.getMessage()), caught);
			}
			@Override
			public void onSuccess(GwtRpcResponseList<GroupInterface> result) {
				iFilter.clearHint();
				if (result == null || result.isEmpty()) {
					iFilter.setErrorHint(MESSAGES.errorNoRoomGroups());
				} else {
					Chip dept = iFilter.getChip("department");
					boolean skipDepartmental = false;
					DepartmentInterface department = null;
					if (dept != null) {
						for (ExamTypeInterface type: iProperties.getExamTypes())
							if (type.getReference().equals(dept.getValue())) {
								skipDepartmental = true;
								break;
							}
						for (DepartmentInterface d: iProperties.getDepartments())
							if (dept.getValue().equals(d.getDeptCode())) {
								department = d; break;
							}
					}
					for (GroupInterface group: result) {
						if (group.isDepartmental()) {
							if (skipDepartmental) continue;
							if (department != null && !department.equals(group.getDepartment())) continue;
							if (dept == null && !group.hasRooms()) continue;
							iDepartmentalGroupsTable.addGroup(group);
						} else {
							iGlobalGroupsTable.addGroup(group);
						}
					}
					iDepartmentalGroupsTable.sort();
					iGlobalGroupsTable.sort();
				}
				iMore.setEnabled(iDepartmentalGroupsTable.getRowCount() > 1 || iGlobalGroupsTable.getRowCount() > 1);
				iGroupsPanel.getRowFormatter().setVisible(iGlobalGroupsRow, iGlobalGroupsTable.getRowCount() > 1);
				iGroupsPanel.getRowFormatter().setVisible(iGlobalGroupsRow + 1, iGlobalGroupsTable.getRowCount() > 1);
				iGroupsPanel.getRowFormatter().setVisible(iDepartmentalGroupsRow, iDepartmentalGroupsTable.getRowCount() > 1);
				iGroupsPanel.getRowFormatter().setVisible(iDepartmentalGroupsRow + 1, iDepartmentalGroupsTable.getRowCount() > 1);
			}
		}, MESSAGES.waitLoadingRoomGroups());
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
		RoomCookie cookie = RoomCookie.getInstance();
		String query = "output=" + format + "&sort=" + cookie.getRoomsSortBy() + (cookie.hasMode() ? "&mode=" + cookie.getMode() : "") + "&dm=" + cookie.getDeptMode();
		if (iProperties.getAcademicSessionId() != null)
			query += "&sid=" + iProperties.getAcademicSessionId();
				
		FilterRpcRequest rooms = iFilter.getElementsRequest();
		if (rooms.hasOptions()) {
			for (Map.Entry<String, Set<String>> option: rooms.getOptions().entrySet()) {
				for (String value: option.getValue()) {
					query += "&r:" + option.getKey() + "=" + URL.encodeQueryString(value);
				}
			}
		}
		
		if (rooms.getText() != null && !rooms.getText().isEmpty())
			query += "&r:text=" + URL.encodeQueryString(rooms.getText());
				
		return query;
	}
	
	private boolean iInitialized = false;
	protected void initialize() {
		if (iInitialized) return;
		if (iProperties == null) return;
		iNew.setEnabled(iProperties.isCanAddDepartmentalRoomGroup() || iProperties.isCanAddGlobalRoomGroup()); 
		if (History.getToken() != null && !History.getToken().isEmpty()) {
			iFilter.setValue(History.getToken(), true);
		} else {
			iFilter.setValue(RoomCookie.getInstance().getHash(iMode), true);
		}
		
		iRoomGroupEdit = new RoomGroupEdit(iProperties) {
			@Override
			protected void onShow() {
				RoomHint.hideHint();
				iRootPanel.setWidget(iRoomGroupEdit);
			}
			
			@Override
			protected void onHide(boolean refresh) {
				iRootPanel.setWidget(iGroupsPanel);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageRooms());
				if (refresh && (iGroupsPanel.getRowFormatter().isVisible(iGlobalGroupsRow) || iGroupsPanel.getRowFormatter().isVisible(iDepartmentalGroupsRow))) search();
			}
		};
		
		iGlobalGroupsTable.addMouseClickListener(new MouseClickListener<GroupInterface>() {
			@Override
			public void onMouseClick(final TableEvent<GroupInterface> event) {
				GroupInterface group = event.getData();
				if (group == null) return;
				if (group.canEdit() || group.canDelete()) {
					Chip dept = iFilter.getChip("department");
					iRoomGroupEdit.setGroup(group, dept == null ? null : dept.getValue());
					LoadingWidget.execute(iFilter.getElementsRequest(), new AsyncCallback<FilterRpcResponse>() {
						@Override
						public void onFailure(Throwable caught) {
							iFilter.setErrorHint(MESSAGES.failedToLoadRooms(caught.getMessage()));
							UniTimeNotifications.error(MESSAGES.failedToLoadRooms(caught.getMessage()), caught);
						}
						@Override
						public void onSuccess(FilterRpcResponse result) {
							iFilter.clearHint();
							if (result == null || result.getResults() == null || result.getResults().isEmpty()) {
								iFilter.setErrorHint(MESSAGES.errorNoRoomsMatchingFilter());
							} else {
								iRoomGroupEdit.setRooms(result.getResults());
								iRoomGroupEdit.show();
							}
						}
					}, MESSAGES.waitLoadingRooms());
				}
			}
		});
		
		iDepartmentalGroupsTable.addMouseClickListener(new MouseClickListener<GroupInterface>() {
			@Override
			public void onMouseClick(final TableEvent<GroupInterface> event) {
				GroupInterface group = event.getData();
				if (group == null) return;
				if (group.canEdit() || group.canDelete()) {
					Chip dept = iFilter.getChip("department");
					iRoomGroupEdit.setGroup(group, dept == null ? null : dept.getValue());
					LoadingWidget.execute(iFilter.getElementsRequest(), new AsyncCallback<FilterRpcResponse>() {
						@Override
						public void onFailure(Throwable caught) {
							iFilter.setErrorHint(MESSAGES.failedToLoadRooms(caught.getMessage()));
							UniTimeNotifications.error(MESSAGES.failedToLoadRooms(caught.getMessage()), caught);
						}
						@Override
						public void onSuccess(FilterRpcResponse result) {
							iFilter.clearHint();
							if (result == null || result.getResults() == null || result.getResults().isEmpty()) {
								iFilter.setErrorHint(MESSAGES.errorNoRoomsMatchingFilter());
							} else {
								iRoomGroupEdit.setRooms(result.getResults());
								iRoomGroupEdit.show();
							}
						}
					}, MESSAGES.waitLoadingRooms());
				}
			}
		});
		
		iInitialized = true;
	}
	
	private class Session implements AcademicSessionProvider {
		private List<AcademicSessionChangeHandler> iHandlers = new ArrayList<AcademicSessionProvider.AcademicSessionChangeHandler>();
		
		private Session() {
		}
		
		@Override
		public void selectSession(Long sessionId, AsyncCallback<Boolean> callback) {
			callback.onSuccess(false);
		}
		
		@Override
		public String getAcademicSessionName() {
			return iProperties == null ? null : iProperties.getAcademicSessionName();
		}
		
		@Override
		public Long getAcademicSessionId() {
			return iProperties == null ? null : iProperties.getAcademicSessionId();
		}
		
		@Override
		public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {
			iHandlers.add(handler);
		}
		
		@Override
		public AcademicSessionInfo getAcademicSessionInfo() {
			return null;
		}
		
		protected void fireChange() {
			AcademicSessionProvider.AcademicSessionChangeEvent event = new AcademicSessionProvider.AcademicSessionChangeEvent() {
				@Override
				public Long getNewAcademicSessionId() {
					return iProperties == null ? null : iProperties.getAcademicSessionId();
				}
				@Override
				public Long getOldAcademicSessionId() {
					return null;
				}
				@Override
				public boolean isChanged() {
					return true;
				}
			};
			for (AcademicSessionChangeHandler h: iHandlers)
				h.onAcademicSessionChange(event);
		}
	}
}
