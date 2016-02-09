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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.events.AcademicSessionSelectionBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.AriaOperation;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeHandler;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.SaveFilterDefaultRpcRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingDisplayMode;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsPageMode;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Window;
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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class RoomsPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private AcademicSessionProvider iSession = null;
	private RoomFilterBox iFilter = null;
	private AriaButton iSearch = null;
	private AriaButton iNew = null;
	private AriaButton iMore = null;
	private AriaButton iEditRoomSharing = null;
	private RoomsTable iRoomsTable = null;
	private RoomDetail iRoomDetail = null;
	
	private VerticalPanel iRoomsPanel = null;
	private SimplePanel iRootPanel;
	
	private SimplePanel iPanel = null;
	private SimpleForm iFilterForm = null;
	private HorizontalPanel iFilterPanel = null;
	private RoomsPageMode iMode = RoomsPageMode.COURSES;
	private RoomPropertiesInterface iProperties = null;
	private RoomEdit iRoomEdit;
	private RoomDepartmentsEdit iRoomDepartmentsEdit;
	private UniTimeHeaderPanel iHeaderPanel = null;
	private HistoryToken iHistoryToken = null;
	
	public RoomsPage() {
		if (Location.getParameter("mode") != null)
			iMode = RoomsPageMode.valueOf(Location.getParameter("mode").toUpperCase());
		iHistoryToken = new HistoryToken(iMode);

		iPanel = new SimplePanel();
		
		iRoomsPanel = new VerticalPanel();
		iRoomsPanel.setWidth("100%");
		
		ClickHandler clickSearch = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				changeUrl();
				search(null);
			}
		};
		
		ClickHandler clickNew = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iRoomEdit.setRoom(null);
				iRoomEdit.show();
			}
		};
		
		ClickHandler clickMore = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iRoomsTable == null) return;
				
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new UniTimeTableHeader.MenuBarWithAccessKeys();
				
				if (iProperties != null && iProperties.isCanSaveFilterDefaults()) {
					MenuItem item = new MenuItem(MESSAGES.buttonClear(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							iFilter.setValue(iHistoryToken.getDefaultParameter("q", ""), true);
							hideResults();
							changeUrl();
						}
					});
					Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), UniTimeHeaderPanel.stripAccessKey(MESSAGES.buttonClear()));
					menu.addItem(item);
					menu.addSeparator();
				}
				
				boolean first = true;
				List<Operation> showHide = iRoomsTable.getShowHideOperations();
				if (!showHide.isEmpty()) {
					MenuBar submenu = new MenuBar(true);
					for (final Operation op: showHide) {
						MenuItem item = new MenuItem(op.getName(), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								op.execute();
							}
						});
						if (op instanceof AriaOperation)
							Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), ((AriaOperation)op).getAriaLabel());
						else
							Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), UniTimeHeaderPanel.stripAccessKey(op.getName()));
						submenu.addItem(item);
					}
					MenuItem columns = new MenuItem(MESSAGES.opColumns(), submenu);
					columns.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(columns);
					first = false;
				}
				
				List<Operation> sorts = iRoomsTable.getSortOperations();
				if (!sorts.isEmpty()) {
					MenuBar submenu = new MenuBar(true);
					for (final Operation op: sorts) {
						String name = op.getName();
						if (op instanceof HasColumnName)
							name = ((HasColumnName)op).getColumnName();
						MenuItem item = new MenuItem(name, true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								op.execute();
							}
						});
						if (op instanceof AriaOperation)
							Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), ((AriaOperation)op).getAriaLabel());
						else
							Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), UniTimeHeaderPanel.stripAccessKey(op.getName()));
						submenu.addItem(item);
					}
					MenuItem columns = new MenuItem(MESSAGES.opSort(), submenu);
					columns.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(columns);
					first = false;
				}
				
				if (iRoomsTable.hasDepartmentOperations()) {
					List<Operation> depts = iRoomsTable.getDepartmentOperations();
					if (!depts.isEmpty()) {
						MenuBar submenu = new MenuBar(true);
						for (final Operation op: depts) {
							MenuItem item = new MenuItem(op.getName(), true, new Command() {
								@Override
								public void execute() {
									popup.hide();
									op.execute();
								}
							});
							if (op instanceof AriaOperation)
								Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), ((AriaOperation)op).getAriaLabel());
							else
								Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), UniTimeHeaderPanel.stripAccessKey(op.getName()));
							submenu.addItem(item);
						}
						MenuItem columns = new MenuItem(MESSAGES.opDepartmentFormat(), submenu);
						columns.getElement().getStyle().setCursor(Cursor.POINTER);
						menu.addItem(columns);
						first = false;
					}
				}

				if (iRoomsTable.hasOrientationOperations()) {
					MenuBar orientation = null;
					if (!RoomCookie.getInstance().isGridAsText()) {
						if (orientation == null) orientation = new MenuBar(true);
						MenuItem item = new MenuItem(MESSAGES.opOrientationAsText(), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								RoomCookie.getInstance().setOrientation(true, RoomCookie.getInstance().areRoomsHorizontal());
								iRoomsTable.refreshTable();
							}
						});
						Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), MESSAGES.opOrientationAsText());
						orientation.addItem(item);
					}
					if (RoomCookie.getInstance().isGridAsText()) {
						if (orientation == null) orientation = new MenuBar(true);
						MenuItem item = new MenuItem(MESSAGES.opOrientationAsGrid(), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								RoomCookie.getInstance().setOrientation(false, RoomCookie.getInstance().areRoomsHorizontal());
								iRoomsTable.refreshTable();
							}
						});
						Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), MESSAGES.opOrientationAsGrid());
						orientation.addItem(item);
					}
					if (!RoomCookie.getInstance().isGridAsText() && RoomCookie.getInstance().areRoomsHorizontal()) {
						if (orientation == null) orientation = new MenuBar(true);
						MenuItem item = new MenuItem(MESSAGES.opOrientationVertical(), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								RoomCookie.getInstance().setOrientation(false, false);
								iRoomsTable.refreshTable();
							}
						});
						Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), MESSAGES.opOrientationVertical());
						orientation.addItem(item);
					}
					if (!RoomCookie.getInstance().isGridAsText() && !RoomCookie.getInstance().areRoomsHorizontal()) {
						if (orientation == null) orientation = new MenuBar(true);
						MenuItem item = new MenuItem(MESSAGES.opOrientationHorizontal(), true, new Command() {
							@Override
							public void execute() {
								popup.hide();
								RoomCookie.getInstance().setOrientation(false, true);
								iRoomsTable.refreshTable();
							}
						});
						Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), MESSAGES.opOrientationHorizontal());
						orientation.addItem(item);
					}
					if (iProperties != null && iProperties.hasModes() && !RoomCookie.getInstance().isGridAsText()) {
						boolean firstMode = true;
						for (final RoomSharingDisplayMode mode: iProperties.getModes()) {
							if (!mode.toHex().equals(RoomCookie.getInstance().getMode())) {
								if (orientation == null) orientation = new MenuBar(true);
								else if (firstMode) orientation.addSeparator();
								firstMode = false;
								MenuItem item = new MenuItem(mode.getName(), true, new Command() {
									@Override
									public void execute() {
										popup.hide();
										RoomCookie.getInstance().setMode(RoomCookie.getInstance().areRoomsHorizontal(), mode.toHex());
										iRoomsTable.refreshTable();
									}
								});
								Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), mode.getName());
								orientation.addItem(item);
							}
						}
					}
					if (orientation != null) {
						MenuItem columns = new MenuItem(MESSAGES.opOrientation(), orientation);
						columns.getElement().getStyle().setCursor(Cursor.POINTER);
						menu.addItem(columns);
						first = false;
					};
				}
				
				for (final Operation op: iRoomsTable.getOtherOperations()) {
					MenuItem item = new MenuItem(op.getName(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							op.execute();
						}
					});
					if (op instanceof AriaOperation)
						Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), ((AriaOperation)op).getAriaLabel());
					else
						Roles.getMenuitemRole().setAriaLabelProperty(item.getElement(), UniTimeHeaderPanel.stripAccessKey(op.getName()));
					if (op.hasSeparator() && !first)
						menu.addSeparator();
					menu.addItem(item);
					first = false;
				}
				
				popup.add(menu);
				popup.showRelativeTo((UIObject)event.getSource());
				menu.focus();
			}
		};
		
		ClickHandler clickEditRoomSharing = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RoomFilterRpcRequest request = iFilter.getElementsRequest();
				request.getOptions().remove("department");
				LoadingWidget.execute(request, new AsyncCallback<FilterRpcResponse>() {
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
							UniTimeNotifications.error(MESSAGES.errorNoRoomsMatchingFilter());
						} else {
							iRoomDepartmentsEdit.setRooms(result.getResults(), iRoomsTable.hasSortBy() ? new Integer(iRoomsTable.getSortBy()) : null);
							iRoomDepartmentsEdit.show();
						}
					}
				}, MESSAGES.waitLoadingRooms());
				
			}
		};
		
		if (iMode.hasSessionSelection()) {
			iFilterForm = new SimpleForm();
			
			iHeaderPanel = new UniTimeHeaderPanel(MESSAGES.sectFilter());
			iFilterForm.addHeaderRow(iHeaderPanel);
			
			iSession = new AcademicSessionSelectionBox(iHistoryToken.getParameter("term"), "Rooms") {
				@Override
				protected void onInitializationSuccess(List<AcademicSession> sessions) {
					iFilterForm.setVisible(sessions != null && !sessions.isEmpty());
					UniTimePageHeader.getInstance().getRight().setVisible(false);
					UniTimePageHeader.getInstance().getRight().setPreventDefault(true);
					setup(getAcademicSessionId(), CONSTANTS.searchWhenPageIsLoaded() && (iHistoryToken.hasParameter("id") || iHistoryToken.hasParameter("q")));
				}
				
				@Override
				protected void onInitializationFailure(Throwable caught) {
					UniTimeNotifications.error(MESSAGES.failedLoadSessions(caught.getMessage()), caught);
				}
			};
			iSession.addAcademicSessionChangeHandler(new AcademicSessionChangeHandler() {
				@Override
				public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
					setup(event.getNewAcademicSessionId(), iRoomsTable != null && iRoomsTable.isVisible() && iRoomsTable.getRowCount() > 1);					
				}
			});;
			iFilterForm.addRow(MESSAGES.propAcademicSession(), (Widget)iSession);
			
			iFilter = new RoomFilterBox(iSession);
			iFilterForm.addRow(MESSAGES.propRoomFilter(), iFilter);
			
			iRoomsPanel.add(iFilterForm);
			
			iHeaderPanel.addButton("search", MESSAGES.buttonSearch(), clickSearch);
			iHeaderPanel.addButton("more", MESSAGES.buttonMoreOperations(), clickMore);
			iHeaderPanel.addButton("new", MESSAGES.buttonAddNewRoom(), clickNew);
			iHeaderPanel.addButton("sharing", MESSAGES.buttonEditRoomSharing(), clickEditRoomSharing);
			iHeaderPanel.setEnabled("more", false);
			iHeaderPanel.setEnabled("new", false);
			iHeaderPanel.setEnabled("sharing", false);
		} else {
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
			iSearch.addClickHandler(clickSearch);
			iFilterPanel.add(iSearch);
			
			iMore = new AriaButton(MESSAGES.buttonMoreOperations());
			iMore.setEnabled(false);
			iMore.addStyleName("unitime-NoPrint");
			iMore.addClickHandler(clickMore);
			iFilterPanel.add(iMore);

			iNew = new AriaButton(MESSAGES.buttonAddNewRoom());
			iNew.setEnabled(false);
			iNew.addStyleName("unitime-NoPrint");
			iNew.addClickHandler(clickNew);
			iFilterPanel.add(iNew);
			
			iEditRoomSharing = new AriaButton(MESSAGES.buttonEditRoomSharing());
			iEditRoomSharing.setEnabled(false);
			iEditRoomSharing.setVisible(false);
			iEditRoomSharing.addStyleName("unitime-NoPrint");
			iEditRoomSharing.addClickHandler(clickEditRoomSharing);
			iFilterPanel.add(iEditRoomSharing);
					
			iRoomsPanel.add(iFilterPanel);
			iRoomsPanel.setCellHorizontalAlignment(iFilterPanel, HasHorizontalAlignment.ALIGN_CENTER);
			
			setup(null, CONSTANTS.searchWhenPageIsLoaded() && (iHistoryToken.hasParameter("id") || iHistoryToken.hasParameter("q")));
		}
		
		iRoomsTable = new RoomsTable(iMode);
		iRoomsTable.getElement().getStyle().setMarginTop(10, Unit.PX);
		iRoomsPanel.add(iRoomsTable);
		
		iRootPanel = new SimplePanel(iRoomsPanel);
		iPanel.setWidget(iRootPanel);
		
		initWidget(iPanel);
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (!iInitialized) return;
				if ("id=add".equals(event.getValue()))
					iHistoryToken.parse(event.getValue());
				else
					iHistoryToken.reset(event.getValue());
				updateFilter(iRoomsTable.isVisible());
			}
		});
		
		iRoomsTable.addOperation(new Operation() {
			@Override
			public void execute() {
				export("rooms.pdf");
			}
			
			@Override
			public boolean isApplicable() {
				return iRoomsTable.getRowCount() > 0 && (iProperties != null && iProperties.isCanExportPdf());
			}
			
			@Override
			public boolean hasSeparator() {
				return false;
			}
			
			@Override
			public String getName() {
				return MESSAGES.opExportPDF();
			}
		});
		
		iRoomsTable.addOperation(new Operation() {
			@Override
			public void execute() {
				export("rooms.csv");
			}
			
			@Override
			public boolean isApplicable() {
				return iRoomsTable.getRowCount() > 0 && (iProperties != null && iProperties.isCanExportCsv());
			}
			
			@Override
			public boolean hasSeparator() {
				return false;
			}
			
			@Override
			public String getName() {
				return MESSAGES.opExportCSV();
			}
		});
		
		iRoomsTable.addMouseClickListener(new MouseClickListener<RoomDetailInterface>() {
			@Override
			public void onMouseClick(final TableEvent<RoomDetailInterface> event) {
				if (event.getData() == null || !event.getData().isCanShowDetail()) return;
				iRoomDetail.setRoom(event.getData());
				iRoomDetail.show();
			}
		});		
		
		iRoomDetail = new RoomDetail(iMode) {
			@Override
			protected void onHide() {
				iRootPanel.setWidget(iRoomsPanel);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageRooms());
				if (iRoomsTable != null && iRoomsTable.isVisible()) search(getRoom() == null ? null : getRoom().getUniqueId());
				changeUrl();
			}
			@Override
			protected void onShow() {
				iRootPanel.setWidget(iRoomDetail);
				changeUrl();
			}
			@Override
			protected void edit() {
				final Long roomId = (getRoom() == null ? null : getRoom().getUniqueId());
				if (roomId != null) {
					FilterRpcRequest rooms = iFilter.createRpcRequest();
					rooms.setCommand(FilterRpcRequest.Command.ENUMERATE);
					rooms.addOption("id", getRoom().getUniqueId().toString());
					rooms.setSessionId(iProperties.getAcademicSessionId());
					LoadingWidget.execute(rooms, new AsyncCallback<FilterRpcResponse>() {
						@Override
						public void onFailure(Throwable caught) {
							UniTimeNotifications.error(MESSAGES.failedToLoadRoomDetails(caught.getMessage()), caught);
						}
						
						@Override
						public void onSuccess(FilterRpcResponse result) {
							if (result == null || result.getResults() == null || result.getResults().isEmpty()) {
								UniTimeNotifications.error(MESSAGES.errorRoomDoesNotExist(roomId.toString()));
							} else {
								iRoomEdit.setRoom((RoomDetailInterface)result.getResults().get(0));
								iRoomEdit.show();
							}
						}
					}, MESSAGES.waitLoadingRoomDetails());
				} else {
					hide();
				}
			}
			@Override
			protected RoomDetailInterface getPrevious(Long roomId) {
				return iRoomsTable == null ? null : iRoomsTable.getPrevious(roomId);
			}
			@Override
			protected RoomDetailInterface getNext(Long roomId) {
				return iRoomsTable == null ? null : iRoomsTable.getNext(roomId);
			}
			@Override
			protected void previous(final RoomDetailInterface room) {
				setRoom(room);
				changeUrl();
			}
			@Override
			protected void next(final RoomDetailInterface room) {
				setRoom(room);
				changeUrl();
			}
			@Override
			public void hide() {
				super.hide();
			}
		};
		
		iRoomEdit = new RoomEdit(iMode) {
			@Override
			protected void onShow() {
				iRootPanel.setWidget(iRoomEdit);
				changeUrl();
			}
			@Override
			protected void onHide(RoomDetailInterface detail, boolean canShowDetail, String message) {
				if (!canShowDetail || (detail == null && getRoom().getUniqueId() == null)) {
					iRootPanel.setWidget(iRoomsPanel);
					UniTimePageLabel.getInstance().setPageName(MESSAGES.pageRooms());
					if (iRoomsTable.isVisible()) search(detail == null ? null : detail.getUniqueId());
				} else {
					if (detail != null)
						iRoomDetail.setRoom(detail);
					iRoomDetail.show(message);
				}
				changeUrl();
			}
		};
		
		iRoomDepartmentsEdit = new RoomDepartmentsEdit() {
			@Override
			protected void onShow() {
				iRootPanel.setWidget(iRoomDepartmentsEdit);
			}
			
			@Override
			protected void onHide(boolean refresh) {
				iRootPanel.setWidget(iRoomsPanel);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageRooms());
				if (refresh && iRoomsTable.isVisible()) search(null);
			}
		};
		
		iFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (iEditRoomSharing != null) iEditRoomSharing.setEnabled(iRoomDepartmentsEdit.setDepartmentOrExamType(iFilter) && iRoomDepartmentsEdit.canEdit());
				if (iHeaderPanel != null) iHeaderPanel.setEnabled("sharing",
						iProperties != null && (iProperties.isCanEditRoomExams() || iProperties.isCanEditDepartments()) &&
						iRoomDepartmentsEdit.setDepartmentOrExamType(iFilter) && iRoomDepartmentsEdit.canEdit());
			}
		});
	}
	
	private boolean iInitialized = false;
	protected void setup(final Long sessionId, final boolean search) {
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingPage());
		RPC.execute(new RoomPropertiesRequest(sessionId, iMode.name()), new AsyncCallback<RoomPropertiesInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilter.setErrorHint(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(RoomPropertiesInterface result) {
				LoadingWidget.getInstance().hide();
				
				iProperties = result;
				
				if (iProperties.isCanSaveFilterDefaults()) {
					iHistoryToken.setDefaultParameter("q", result.getFilterDefault("filter"));
					iFilter.setDefaultValueProvider(new TakesValue<String>() {
						@Override
						public void setValue(final String value) {
							RPC.execute(new SaveFilterDefaultRpcRequest(iMode.name() + ".filter", iFilter.getValue()),
									new AsyncCallback<GwtRpcResponse>() {
										@Override
										public void onFailure(Throwable caught) {
											UniTimeNotifications.error(MESSAGES.failedSaveAsDefault(caught.getMessage()), caught);
										}
										@Override
										public void onSuccess(GwtRpcResponse result) {
											iHistoryToken.setDefaultParameter("q", value);
										}
									});					
						}

						@Override
						public String getValue() {
							return iHistoryToken.getDefaultParameter("q", "");
						}
					});
				} else {
					if (result.hasFilterDefault("filter"))
						iHistoryToken.setDefaultParameter("q", result.getFilterDefault("filter"));
					iFilter.setDefaultValueProvider(null);
				}
				
				if (!RoomCookie.getInstance().hasOrientation())
					RoomCookie.getInstance().setOrientation(iProperties.isGridAsText(), iProperties.isHorizontal());
				
				iRoomDetail.setProperties(iProperties);
				iRoomsTable.setProperties(iProperties);
				iRoomEdit.setProperties(iProperties);
				iRoomDepartmentsEdit.setProperties(iProperties);

				if (iSession instanceof Session)
					((Session)iSession).fireChange();

				if (iNew != null) iNew.setEnabled(iProperties.isCanAddRoom() || iProperties.isCanAddNonUniversity());
				if (iHeaderPanel != null) iHeaderPanel.setEnabled("new", iProperties.isCanAddRoom() || iProperties.isCanAddNonUniversity());
				
				if (iEditRoomSharing != null) {
					iEditRoomSharing.setVisible(iProperties.isCanEditRoomExams() || iProperties.isCanEditDepartments());
					iEditRoomSharing.setEnabled(iRoomDepartmentsEdit.setDepartmentOrExamType(iFilter) && iRoomDepartmentsEdit.canEdit());
				}
				if (iHeaderPanel != null) {
					iHeaderPanel.setEnabled("sharing",
						iProperties != null && (iProperties.isCanEditRoomExams() || iProperties.isCanEditDepartments()) &&
						iRoomDepartmentsEdit.setDepartmentOrExamType(iFilter) && iRoomDepartmentsEdit.canEdit());
				}
				
				if (sessionId != null && iSession instanceof AcademicSessionSelectionBox) {
					iHistoryToken.setParameter("term", ((AcademicSessionSelectionBox)iSession).getAcademicSessionAbbreviation());
					iHistoryToken.mark();
				}
				updateFilter(search);
				iInitialized = true;
			}
		});
	}
	
	protected void updateFilter(boolean search) {
		iFilter.setValue(iHistoryToken.getParameter("q"), true);
		if (iSession instanceof AcademicSessionSelectionBox && iHistoryToken.isChanged("term", ((AcademicSessionSelectionBox)iSession).getAcademicSessionAbbreviation()) && iHistoryToken.getParameter("term") != null)
			((AcademicSessionSelectionBox)iSession).selectSession(iHistoryToken.getParameter("term"), null);
		if (iHistoryToken.hasParameter("id")) {
			if ("add".equals(iHistoryToken.getParameter("id"))) {
				iRoomEdit.setRoom(null);
				iRoomEdit.show();
			} else {
				final Long roomId = Long.valueOf(iHistoryToken.getParameter("id"));
				FilterRpcRequest rooms = iFilter.createRpcRequest();
				rooms.setCommand(FilterRpcRequest.Command.ENUMERATE);
				rooms.addOption("id", roomId.toString());
				iFilter.setValue(iHistoryToken.getParameter("q"), true);
				rooms.setSessionId(iProperties.getAcademicSessionId());
				LoadingWidget.execute(rooms, new AsyncCallback<FilterRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						UniTimeNotifications.error(MESSAGES.failedToLoadRoomDetails(caught.getMessage()), caught);
					}
					
					@Override
					public void onSuccess(FilterRpcResponse result) {
						if (result == null || result.getResults() == null || result.getResults().isEmpty()) {
							UniTimeNotifications.error(MESSAGES.errorRoomDoesNotExist(roomId.toString()));
						} else {
							iRoomDetail.setRoom((RoomDetailInterface)result.getResults().get(0));
							iRoomDetail.show();
						}
					}
				}, MESSAGES.waitLoadingRoomDetails());
			}
		} else if (iRoomDetail.isVisible()) {
			iRoomDetail.hide();
		} else if (iRoomEdit.isVisible()) {
			iRoomEdit.hide(iRoomEdit.getRoom(), false, null);
		} else if (search) {
			search(null);
		}
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
		int flags = (iRoomsTable == null ? cookie.getFlags(iMode) : cookie.getFlags(iMode) & iRoomsTable.getFlags());
		String query = "output=" + format + "&flags=" + flags + "&sort=" + cookie.getRoomsSortBy() +
				"&orientation=" + (cookie.isGridAsText() ? "text" : cookie.areRoomsHorizontal() ? "horizontal" : "vertical") + (cookie.hasMode() ? "&mode=" + cookie.getMode() : "") +
				"&dm=" + cookie.getDeptMode();
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
	
	protected void hideResults() {
		if (iRoomsTable == null) return;
		if (iMore != null) iMore.setEnabled(false);
		if (iHeaderPanel != null) iHeaderPanel.setEnabled("more", false);
		iRoomsTable.clearTable(1);
	}
	
	protected void search(final Long roomId) {
		if (iRoomsTable == null) return;
		hideResults();
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
					Chip dept = iFilter.getChip("department");
					iRoomsTable.setDepartment(dept == null ? null : dept.getValue());
					for (FilterRpcResponse.Entity entity: result.getResults())
						iRoomsTable.addRoom((RoomDetailInterface)entity);
					iRoomsTable.sort();
				}
				if (iMore != null) iMore.setEnabled(iRoomsTable.getRowCount() > 1);
				if (iHeaderPanel != null) iHeaderPanel.setEnabled("more", iRoomsTable.getRowCount() > 1);
				iRoomsTable.scrollTo(roomId);
			}
		}, MESSAGES.waitLoadingRooms());
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
	
	protected void changeUrl() {
		iHistoryToken.reset(null);
		if (iSession instanceof AcademicSessionSelectionBox)
			iHistoryToken.setParameter("term", ((AcademicSessionSelectionBox)iSession).getAcademicSessionAbbreviation());
		iHistoryToken.setParameter("q", iFilter.getValue());
		if (iRoomDetail.equals(iRootPanel.getWidget())) {
			iHistoryToken.setParameter("id", iRoomDetail.getRoom().getUniqueId() == null ? "add" : iRoomDetail.getRoom().getUniqueId().toString());
		}
		if (iRoomEdit.equals(iRootPanel.getWidget())) {
			iHistoryToken.setParameter("id", iRoomEdit.getRoom().getUniqueId() == null ? "add" : iRoomEdit.getRoom().getUniqueId().toString());
		}
		iHistoryToken.mark();
		Client.fireGwtPageChanged(new Client.GwtPageChangeEvent());
	}
	
	public static class HistoryToken {
		private RoomsPageMode iMode = null;
		private Map<String, String> iParams = new HashMap<String, String>();
		private Map<String, String> iDefaults = new HashMap<String, String>();
		
		public HistoryToken(RoomsPageMode mode) {
			iMode = mode;
			
			// 1. take page type defaults --> DEFAULTS
			String query = iMode.getQuery();
			if (query != null) {
				for (String pair: query.split("\\&")) {
					int idx = pair.indexOf('=');
					if (idx >= 0) {
						String key = pair.substring(0, idx);
						if (Location.getParameter(key) == null)
							iDefaults.put(key, URL.decodeQueryString(pair.substring(idx + 1)));
					}
				}
			}
			
			// 2. take page parameters --> DEFAULTS (on top of the page type defaults)
			for (Map.Entry<String, List<String>> params: Window.Location.getParameterMap().entrySet())
				iDefaults.put(params.getKey(), params.getValue().get(0));
			
			// 3. take cookie --> PARAMS (override defaults)
			String cookie = RoomCookie.getInstance().getHash(iMode); 
			if (cookie != null) {
				for (String pair: cookie.split("\\&")) {
					int idx = pair.indexOf('=');
					if (idx >= 0) {
						String key = pair.substring(0, idx);
						if (Location.getParameter(key) == null)
							iParams.put(key, URL.decodeQueryString(pair.substring(idx + 1)));
					}
				}
			}
			
			// 4. take page token (hash) --> PARAMS (override cookie)
			parse(History.getToken());
		}
		
		public void reset(String token) {
			iParams.clear();
			parse(token);
		}
		
		public void parse(String token) {
			if (token != null && !token.isEmpty())
				for (String pair: token.split("\\&")) {
					int idx = pair.indexOf('=');
					if (idx >= 0)
						iParams.put(pair.substring(0, idx), URL.decodeQueryString(pair.substring(idx + 1)));
				}
		}
		
		public void setParameter(String key, String value) {
			if (value == null) {
				iParams.remove(key);
			} else {
				String defaultValue = iDefaults.get(key);
				if (value.equals(defaultValue))
					iParams.remove(key);
				else
					iParams.put(key, value);
			}
		}
		
		public void setParameter(String key, Long value) {
			setParameter(key, value == null ? null : value.toString());
		}
		
		@Override
		public String toString() {
			return toString(null);
		}
		
		public String toString(String skip) {
			String ret = "";
			for (String key: new TreeSet<String>(iParams.keySet())) {
				if (key.equals(skip)) continue;
				if (!ret.isEmpty()) ret += "&";
				ret += key + "=" + URL.encodeQueryString(iParams.get(key));
			}
			return ret;
		}
		
		public String getParameter(String key, String defaultValue) {
			String value = getParameter(key);
			return (value == null ? defaultValue : value);
		}
		
		public String getParameter(String key) {
			String value = iParams.get(key);
			return (value == null ? iDefaults.get(key) : value);
		}
		
		public String getDefaultParameter(String key, String defaultValue) {
			String value = iDefaults.get(key);
			return (value == null ? defaultValue : value);
		}
		
		public void setDefaultParameter(String key, String value) {
			if (value != null)
				iDefaults.put(key, value);
		}
		
		public boolean hasParameter(String key) {
			return getParameter(key) != null;
		}
		
		public boolean isChanged(String key, String value) {
			String v = getParameter(key);
			return (v == null ? value != null : !v.equals(value));
		}
		
		public boolean isChanged(String key, String defaultValue, String value) {
			String v = getParameter(key);
			return (v == null ? !defaultValue.equals(value) : !v.equals(value));
		}
		
		public void mark() {
			String token = toString();
			if (!History.getToken().equals(token))
				History.newItem(token, false);
			RoomCookie.getInstance().setHash(iMode, toString("id"));
		}
	}
}
