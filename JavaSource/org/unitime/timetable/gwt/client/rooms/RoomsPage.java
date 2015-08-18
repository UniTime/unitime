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
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.AriaOperation;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingDisplayMode;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsColumn;
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

/**
 * @author Tomas Muller
 */
public class RoomsPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private Session iSession = null;
	private RoomFilterBox iFilter = null;
	private AriaButton iSearch = null;
	private AriaButton iNew = null;
	private AriaButton iMore = null;
	private RoomsTable iRoomsTable = null;
	private RoomDetail iRoomDetail = null;
	
	private VerticalPanel iRoomsPanel = null;
	private SimplePanel iRootPanel;
	
	private SimplePanel iPanel = null;
	private HorizontalPanel iFilterPanel = null;
	private RoomsPageMode iMode = RoomsPageMode.COURSES;
	private RoomPropertiesInterface iProperties = null;
	private RoomEdit iRoomEdit;
	
	public RoomsPage() {
		if (Location.getParameter("mode") != null)
			iMode = RoomsPageMode.valueOf(Location.getParameter("mode").toUpperCase());

		iPanel = new SimplePanel();
		
		iRoomsPanel = new VerticalPanel();
		
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

		iNew = new AriaButton(MESSAGES.buttonAddNew());
		iNew.setEnabled(false);
		iNew.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iNew);
				
		iRoomsPanel.add(iFilterPanel);
		iRoomsPanel.setCellHorizontalAlignment(iFilterPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		iRoomsPanel.setWidth("100%");
		
		iRootPanel = new SimplePanel(iRoomsPanel);
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
				if (iRoomsTable == null) return;
				
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				
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
				
				MenuBar orientation = null;
				if (!RoomCookie.getInstance().isGridAsText() && (iRoomsTable.isVisible(RoomsColumn.AVAILABILITY) || iRoomsTable.isVisible(RoomsColumn.PERIOD_PREF) || iRoomsTable.isVisible(RoomsColumn.EVENT_AVAILABILITY))) {
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
				if (RoomCookie.getInstance().isGridAsText() && (iRoomsTable.isVisible(RoomsColumn.AVAILABILITY) || iRoomsTable.isVisible(RoomsColumn.PERIOD_PREF) || iRoomsTable.isVisible(RoomsColumn.EVENT_AVAILABILITY))) {
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
				if (!RoomCookie.getInstance().isGridAsText() && RoomCookie.getInstance().areRoomsHorizontal() && (iRoomsTable.isVisible(RoomsColumn.AVAILABILITY) || iRoomsTable.isVisible(RoomsColumn.PERIOD_PREF) || iRoomsTable.isVisible(RoomsColumn.EVENT_AVAILABILITY))) {
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
				if (!RoomCookie.getInstance().isGridAsText() && !RoomCookie.getInstance().areRoomsHorizontal() && (iRoomsTable.isVisible(RoomsColumn.AVAILABILITY) || iRoomsTable.isVisible(RoomsColumn.PERIOD_PREF) || iRoomsTable.isVisible(RoomsColumn.EVENT_AVAILABILITY))) {
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
		});
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				try {
					RoomDetailInterface room = (iRoomsTable == null ? null : iRoomsTable.getRoom(Long.parseLong(event.getValue())));
					if (room != null) {
						iRoomDetail.setRoom(room);
						iRoomDetail.show();
					}
				} catch (NumberFormatException e) {
					iFilter.setValue(event.getValue(), true);
					if (iRoomDetail.equals(iRootPanel.getWidget()))
						iRoomDetail.hide();
					else if (iRoomsTable != null && iRoomsTable.isVisible()) 
						search();
				}
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
				if (!RoomCookie.getInstance().hasOrientation())
					RoomCookie.getInstance().setOrientation(iProperties.isGridAsText(), iProperties.isHorizontal());
				iRoomsTable = new RoomsTable(iMode, iProperties);
				iRoomsTable.getElement().getStyle().setMarginTop(10, Unit.PX);
				iRoomsPanel.add(iRoomsTable);
				iRoomDetail.setProperties(iProperties);
				iSession.fireChange();
				initialize();
			}
		});
		
		iRoomDetail = new RoomDetail() {
			@Override
			protected void onHide() {
				iRootPanel.setWidget(iRoomsPanel);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageRooms());
				if (iRoomsTable != null && iRoomsTable.isVisible()) search();
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
		
		iNew.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iRoomEdit.setRoom(null);
				iRoomEdit.show();
			}
		});
	}
	
	private boolean iInitialized = false;
	protected void initialize() {
		if (iInitialized) return;
		if (iProperties == null) return;
		iRoomEdit = new RoomEdit(iProperties) {
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
					if (iRoomsTable.isVisible()) search();
				} else {
					if (detail != null)
						iRoomDetail.setRoom(detail);
					iRoomDetail.show(message);
				}
				changeUrl();
			}
		};
		iNew.setEnabled(iProperties.isCanAddRoom() || iProperties.isCanAddNonUniversity());
		if (History.getToken() != null && !History.getToken().isEmpty()) {
			try {
				final Long roomId = Long.parseLong(History.getToken());
				FilterRpcRequest rooms = iFilter.createRpcRequest();
				rooms.setCommand(FilterRpcRequest.Command.ENUMERATE);
				rooms.addOption("id", roomId.toString());
				iFilter.setValue(RoomCookie.getInstance().getHash(iMode), true);
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
			} catch (NumberFormatException e) {
				iFilter.setValue(History.getToken(), true);
			}
		} else {
			iFilter.setValue(RoomCookie.getInstance().getHash(iMode), true);
		}
		
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
				if (event.getData() == null) return;
				iRoomDetail.setRoom(event.getData());
				iRoomDetail.show();
			}
		});
		
		iInitialized = true;
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
	
	protected void search() {
		if (iRoomsTable == null) return;
		iMore.setEnabled(false);
		iRoomsTable.clearTable(1);
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
					//scroll
				}
				iMore.setEnabled(iRoomsTable.getRowCount() > 1);
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
		String token = null;
		if (iRoomDetail.equals(iRootPanel.getWidget())) {
			token = iRoomDetail.getRoom().getUniqueId().toString();
		} else {
			token = iFilter.getValue();
			RoomCookie.getInstance().setHash(iMode, token);
		}
		if (!History.getToken().equals(token))
			History.newItem(token, false);
		Client.fireGwtPageChanged(new Client.GwtPageChangeEvent());
	}
}
