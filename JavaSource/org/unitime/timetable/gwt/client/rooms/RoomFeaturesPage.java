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
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFeaturesColumn;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsPageMode;
import org.unitime.timetable.gwt.shared.RoomInterface.SearchRoomFeaturesRequest;

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
public class RoomFeaturesPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private Session iSession = null;
	private RoomFilterBox iFilter = null;
	private AriaButton iSearch = null;
	private AriaButton iNew = null;
	private AriaButton iMore = null;	

	private SimpleForm iFeaturesPanel = null;
	private SimplePanel iRootPanel;
	
	private SimplePanel iPanel = null;
	private HorizontalPanel iFilterPanel = null;
	private RoomPropertiesInterface iProperties = null;
	private UniTimeHeaderPanel iGlobalFeaturesHeader = null;
	private RoomFeaturesTable iGlobalFeaturesTable = null;
	private int iGlobalFeaturesRow = -1;
	private UniTimeHeaderPanel iDepartmentalFeaturesHeader = null;
	private RoomFeaturesTable iDepartmentalFeaturesTable = null;
	private int iDepartmentalFeaturesRow = -1;
	private RoomsPageMode iMode = RoomsPageMode.COURSES;
	
	private RoomFeatureEdit iRoomFeatureEdit = null;
	
	public RoomFeaturesPage() {
		if (Location.getParameter("mode") != null)
			iMode = RoomsPageMode.valueOf(Location.getParameter("mode").toUpperCase());
		
		iPanel = new SimplePanel();
		
		iFeaturesPanel = new SimpleForm();
		
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

		iNew = new AriaButton(MESSAGES.buttonAddNewRoomFeature());
		iNew.setEnabled(false);
		iNew.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iNew);
		
		int filterRow = iFeaturesPanel.addRow(iFilterPanel);
		iFeaturesPanel.getCellFormatter().setHorizontalAlignment(filterRow, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		iFeaturesPanel.setWidth("100%");
		
		iGlobalFeaturesHeader = new UniTimeHeaderPanel(MESSAGES.headerGlobalRoomFeatures());
		iGlobalFeaturesRow = iFeaturesPanel.addHeaderRow(iGlobalFeaturesHeader);
		iGlobalFeaturesTable = new RoomFeaturesTable(true) {
			protected void doSort(RoomFeaturesColumn column) {
				super.doSort(column);
				iDepartmentalFeaturesTable.setSortBy(RoomCookie.getInstance().getRoomFeaturesSortBy());
			}
		};
		iFeaturesPanel.addRow(iGlobalFeaturesTable);
		iFeaturesPanel.getRowFormatter().setVisible(iGlobalFeaturesRow, false);
		iFeaturesPanel.getRowFormatter().setVisible(iGlobalFeaturesRow + 1, false);
		
		iDepartmentalFeaturesHeader = new UniTimeHeaderPanel(MESSAGES.headerDepartmentalRoomFeatures());
		iDepartmentalFeaturesRow = iFeaturesPanel.addHeaderRow(iDepartmentalFeaturesHeader);
		iDepartmentalFeaturesTable = new RoomFeaturesTable(false) {
			protected void doSort(RoomFeaturesColumn column) {
				super.doSort(column);
				iGlobalFeaturesTable.setSortBy(RoomCookie.getInstance().getRoomFeaturesSortBy());
			}
		};
		iFeaturesPanel.addRow(iDepartmentalFeaturesTable);
		iFeaturesPanel.getRowFormatter().setVisible(iDepartmentalFeaturesRow, false);
		iFeaturesPanel.getRowFormatter().setVisible(iDepartmentalFeaturesRow + 1, false);
		
		iRootPanel = new SimplePanel(iFeaturesPanel);
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
				for (final RoomFeaturesColumn column: RoomFeaturesColumn.values()) {
					if (RoomFeaturesComparator.isApplicable(column)) {
						if ((iGlobalFeaturesTable.getRowCount() <= 1 || iGlobalFeaturesTable.getNbrCells(column) == 0) &&
							(iDepartmentalFeaturesTable.getRowCount() <= 1 || iDepartmentalFeaturesTable.getNbrCells(column) == 0)) continue;
						String name = iGlobalFeaturesTable.getColumnName(column, 0).replace("<br>", " ");
						MenuItem item = new MenuItem(name, true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								int sortBy = RoomCookie.getInstance().getRoomFeaturesSortBy();
								if (sortBy == (1 + column.ordinal()) || sortBy == (-1 - column.ordinal())) {
									sortBy = -sortBy;
								} else {
									sortBy = 1 + column.ordinal();
								}
								RoomCookie.getInstance().setSortRoomFeaturesBy(sortBy);
								iGlobalFeaturesTable.setSortBy(sortBy);
								iDepartmentalFeaturesTable.setSortBy(sortBy);
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
							iGlobalFeaturesTable.refreshTable();
							iDepartmentalFeaturesTable.refreshTable();
						}
					});
					Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), d.getName());
					deptItems.addItem(item);
				}
				MenuItem deptMenu = new MenuItem(MESSAGES.opDepartmentFormat(), deptItems);
				deptMenu.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(deptMenu);
				
				if (iProperties.isCanExportRoomFeatures()) {
					menu.addSeparator();
					
					MenuItem exportPdf = new MenuItem(MESSAGES.opExportPDF(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							export("roomfeatures.pdf");
						}
					});
					Roles.getMenuitemRole().setAriaLabelProperty(exportPdf.getElement(), MESSAGES.opExportPDF());
					menu.addItem(exportPdf);
					
					MenuItem exportCsv = new MenuItem(MESSAGES.opExportCSV(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							export("roomfeatures.csv");
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
				if (iRoomFeatureEdit == null) return;
				Chip dept = iFilter.getChip("department");
				iRoomFeatureEdit.setFeature(null, dept == null ? null : dept.getValue());
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
							iRoomFeatureEdit.setRooms(result.getResults());
							iRoomFeatureEdit.show();
						}
					}
				}, MESSAGES.waitLoadingRooms());
			}
		});
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iFilter.setValue(event.getValue(), true);
				if (iFeaturesPanel.getRowFormatter().isVisible(iGlobalFeaturesRow) || iFeaturesPanel.getRowFormatter().isVisible(iDepartmentalFeaturesRow))
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
		iGlobalFeaturesTable.clearTable(1);
		iDepartmentalFeaturesTable.clearTable(1);
		iFeaturesPanel.getRowFormatter().setVisible(iGlobalFeaturesRow, false);
		iFeaturesPanel.getRowFormatter().setVisible(iGlobalFeaturesRow + 1, false);
		iFeaturesPanel.getRowFormatter().setVisible(iDepartmentalFeaturesRow, false);
		iFeaturesPanel.getRowFormatter().setVisible(iDepartmentalFeaturesRow + 1, false);
		SearchRoomFeaturesRequest request = new SearchRoomFeaturesRequest();
		request.setFilter(iFilter.getElementsRequest());
		LoadingWidget.execute(request, new AsyncCallback<GwtRpcResponseList<FeatureInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				iFilter.setErrorHint(MESSAGES.failedToLoadRoomFeatures(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadRoomFeatures(caught.getMessage()), caught);
			}
			@Override
			public void onSuccess(GwtRpcResponseList<FeatureInterface> result) {
				iFilter.clearHint();
				if (result == null || result.isEmpty()) {
					iFilter.setErrorHint(MESSAGES.errorNoRoomFeatures());
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
					for (FeatureInterface Feature: result) {
						if (Feature.isDepartmental()) {
							if (skipDepartmental) continue;
							if (department != null && !department.equals(Feature.getDepartment())) continue;
							if (dept == null && !Feature.hasRooms()) continue;
							iDepartmentalFeaturesTable.addFeature(Feature);
						} else {
							iGlobalFeaturesTable.addFeature(Feature);
						}
					}
					iDepartmentalFeaturesTable.sort();
					iGlobalFeaturesTable.sort();
				}
				iMore.setEnabled(iDepartmentalFeaturesTable.getRowCount() > 1 || iGlobalFeaturesTable.getRowCount() > 1);
				iFeaturesPanel.getRowFormatter().setVisible(iGlobalFeaturesRow, iGlobalFeaturesTable.getRowCount() > 1);
				iFeaturesPanel.getRowFormatter().setVisible(iGlobalFeaturesRow + 1, iGlobalFeaturesTable.getRowCount() > 1);
				iFeaturesPanel.getRowFormatter().setVisible(iDepartmentalFeaturesRow, iDepartmentalFeaturesTable.getRowCount() > 1);
				iFeaturesPanel.getRowFormatter().setVisible(iDepartmentalFeaturesRow + 1, iDepartmentalFeaturesTable.getRowCount() > 1);
			}
		}, MESSAGES.waitLoadingRoomFeatures());
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
		iNew.setEnabled(iProperties.isCanAddDepartmentalRoomFeature() || iProperties.isCanAddGlobalRoomFeature()); 
		if (History.getToken() != null && !History.getToken().isEmpty()) {
			iFilter.setValue(History.getToken(), true);
		} else {
			iFilter.setValue(RoomCookie.getInstance().getHash(iMode), true);
		}
		
		iRoomFeatureEdit = new RoomFeatureEdit(iProperties) {
			@Override
			protected void onShow() {
				RoomHint.hideHint();
				iRootPanel.setWidget(iRoomFeatureEdit);
			}
			
			@Override
			protected void onHide(boolean refresh) {
				iRootPanel.setWidget(iFeaturesPanel);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageRoomFeatures());
				if (refresh && (iFeaturesPanel.getRowFormatter().isVisible(iGlobalFeaturesRow) || iFeaturesPanel.getRowFormatter().isVisible(iDepartmentalFeaturesRow))) search();
			}
		};
		
		iGlobalFeaturesTable.addMouseClickListener(new MouseClickListener<FeatureInterface>() {
			@Override
			public void onMouseClick(final TableEvent<FeatureInterface> event) {
				FeatureInterface Feature = event.getData();
				if (Feature == null) return;
				if (Feature.canEdit() || Feature.canDelete()) {
					Chip dept = iFilter.getChip("department");
					iRoomFeatureEdit.setFeature(Feature, dept == null ? null : dept.getValue());
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
								iRoomFeatureEdit.setRooms(result.getResults());
								iRoomFeatureEdit.show();
							}
						}
					}, MESSAGES.waitLoadingRooms());
				}
			}
		});
		
		iDepartmentalFeaturesTable.addMouseClickListener(new MouseClickListener<FeatureInterface>() {
			@Override
			public void onMouseClick(final TableEvent<FeatureInterface> event) {
				FeatureInterface Feature = event.getData();
				if (Feature == null) return;
				if (Feature.canEdit() || Feature.canDelete()) {
					Chip dept = iFilter.getChip("department");
					iRoomFeatureEdit.setFeature(Feature, dept == null ? null : dept.getValue());
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
								iRoomFeatureEdit.setRooms(result.getResults());
								iRoomFeatureEdit.show();
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
