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

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.rooms.TravelTimes.TravelTimeResponse;
import org.unitime.timetable.gwt.client.rooms.TravelTimes.TravelTimesRequest;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
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
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFlag;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsPageMode;

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
	
	private RoomFilterBox iFilter = null;
	private AriaButton iSearch = null;
	private AriaButton iNew = null;
	private AriaButton iExport = null;
	private RoomsTable iRoomsTable = null;
	
	private VerticalPanel iRoomsPanel = null;
	
	private SimplePanel iPanel = null;
	private HorizontalPanel iFilterPanel = null;
	private RoomsPageMode iMode = RoomsPageMode.COURSES;
	private RoomPropertiesInterface iProperties = null;
	
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
		
		iFilter = new RoomFilterBox(new Session());
		iFilter.setValue("department:Managed");
		iFilterPanel.add(iFilter);
		
		iSearch = new AriaButton(MESSAGES.buttonSearch());
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iSearch);
		
		iExport = new AriaButton(MESSAGES.buttonExport());
		iExport.addStyleName("unitime-NoPrint");
		iExport.setEnabled(false);
		iFilterPanel.add(iExport);		

		iNew = new AriaButton(MESSAGES.buttonAddNew());
		iNew.setEnabled(false);
		iNew.addStyleName("unitime-NoPrint");
		iFilterPanel.add(iNew);
				
		iRoomsPanel.add(iFilterPanel);
		iRoomsPanel.setCellHorizontalAlignment(iFilterPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		iRoomsTable = new RoomsTable(iMode);
		iRoomsTable.getElement().getStyle().setMarginTop(10, Unit.PX);
		
		iRoomsPanel.add(iRoomsTable);
		
		iRoomsPanel.setWidth("100%");
		
		iPanel.setWidget(iRoomsPanel);
		
		initWidget(iPanel);
		
		iSearch.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(iFilter.getValue(), false);
				search();
			}
		});
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iFilter.setValue(event.getValue(), true);
				if (!iFilter.getValue().isEmpty())
					search();
			}
		});
		
		if (History.getToken() != null)
			iFilter.setValue(History.getToken(), true);
		else
			iFilter.setValue(RoomCookie.getInstance().getHash(iMode), true);
		
		RPC.execute(new RoomPropertiesRequest(), new AsyncCallback<RoomPropertiesInterface>() {
			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(RoomPropertiesInterface result) {
				iProperties = result;
				if (iProperties.isCanExportCsv() || iProperties.isCanExportPdf())
					iExport.setEnabled(true);
			}
		});
		
		iExport.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				if (iProperties.isCanExportPdf()) {
					MenuItem exportPdf = new MenuItem(MESSAGES.opExportPDF(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							export("rooms.pdf");
						}
					});
					exportPdf.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(exportPdf);
				}
				if (iProperties.isCanExportCsv()) {
					MenuItem exportCsv = new MenuItem(MESSAGES.opExportCSV(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							export("rooms.csv");
						}
					});
					exportCsv.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(exportCsv);
				}
				popup.add(menu);
				popup.showRelativeTo((UIObject)event.getSource());
				menu.focus();
			}
		});
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
		int flags = cookie.getFlags(iMode);
		for (RoomFlag f: RoomFlag.values())
			if (!f.isShowWhenEmpty() && !f.in(iRoomsTable.getFlags()))
				flags = f.clear(flags);
		String query = "output=" + format + "&flags=" + flags + "&sort=" + cookie.getRoomsSortBy() +
				"&horizontal=" + (cookie.areRoomsHorizontal() ? "1" : "0") + (cookie.hasMode() ? "&mode=" + cookie.getMode() : "") +
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
					for (FilterRpcResponse.Entity entity: result.getResults())
						iRoomsTable.addRoom((RoomDetailInterface)entity, dept == null ? null : dept.getValue());
				}
			}
		}, MESSAGES.waitLoadingRooms());
	}

	private class Session implements AcademicSessionProvider {
		private List<AcademicSessionChangeHandler> iHandlers = new ArrayList<AcademicSessionProvider.AcademicSessionChangeHandler>();
		private Long iId;
		private String iName;
		
		private Session() {
			RPC.execute(TravelTimesRequest.init(), new AsyncCallback<TravelTimeResponse>() {

				@Override
				public void onFailure(Throwable caught) {
					iFilter.setErrorHint(MESSAGES.failedToInitialize(caught.getMessage()));
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(TravelTimeResponse result) {
					iId = result.getSessionId(); iName = result.getSessionName();
					fireChange();
				}
			});
		}
		
		@Override
		public void selectSession(Long sessionId, AsyncCallback<Boolean> callback) {
			callback.onSuccess(false);
		}
		
		@Override
		public String getAcademicSessionName() {
			return iName;
		}
		
		@Override
		public Long getAcademicSessionId() {
			return iId;
		}
		
		@Override
		public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {
			iHandlers.add(handler);
		}
		
		@Override
		public AcademicSessionInfo getAcademicSessionInfo() {
			return null;
		}
		
		private void fireChange() {
			AcademicSessionProvider.AcademicSessionChangeEvent event = new AcademicSessionProvider.AcademicSessionChangeEvent() {
				@Override
				public Long getNewAcademicSessionId() {
					return iId;
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
