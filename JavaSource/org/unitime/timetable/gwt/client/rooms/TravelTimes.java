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

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.events.RoomFilterBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class TravelTimes extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iForm;
	private Matrix iMatrix;
	private UniTimeHeaderPanel iHeader, iFooter;
	private RoomFilterBox iRoomFilter;
	private AriaButton iShow;
	
	public TravelTimes() {
		iForm = new SimpleForm();
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectTravelTimesInMintes());
		iHeader.addButton("save", MESSAGES.buttonSave(), 75, new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				iHeader.showLoading();
				RPC.execute(TravelTimesRequest.saveRooms(iMatrix.getRooms()), new AsyncCallback<TravelTimeResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(MESSAGES.failedToSaveMatrix(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedToSaveMatrix(caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(TravelTimeResponse result) {
						iHeader.clearMessage();
						iMatrix.onSaveChanges();
						iMatrix.setEditable(false);
						iHeader.setEnabled("save", false);
						iHeader.setEnabled("back", false);
						iHeader.setEnabled("edit", true);
						iShow.setEnabled(true);
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), 75, new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				iHeader.clearMessage();
				iMatrix.onCancelChanges();
				iMatrix.setEditable(false);
				iHeader.setEnabled("save", false);
				iHeader.setEnabled("back", false);
				iHeader.setEnabled("edit", true);
				iShow.setEnabled(true);
			}
		});
		iHeader.addButton("edit", MESSAGES.buttonEdit(), 75, new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				iHeader.clearMessage();
				iMatrix.setEditable(true);
				iHeader.setEnabled("save", true);
				iHeader.setEnabled("back", true);
				iHeader.setEnabled("edit", false);
				iShow.setEnabled(false);
			}
		});

		iHeader.setEnabled("save", false);
		iHeader.setEnabled("back", false);
		iHeader.setEnabled("edit", false);
		
		iRoomFilter = new RoomFilterBox(new Session());
		iRoomFilter.setValue("department:Managed");
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(4);
		Label label = new Label(MESSAGES.propFilter()); 
		hp.add(label);
		hp.setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);
		hp.add(iRoomFilter);
		iShow = new AriaButton(MESSAGES.buttonShow());
		iShow.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iHeader.setVisible(true);
				iHeader.setEnabled("edit", false);
				iFooter.setVisible(false);
				iMatrix.clear();
				LoadingWidget.showLoading(MESSAGES.waitLoadingTravelTimes());
				iRoomFilter.getElements(new AsyncCallback<List<FilterRpcResponse.Entity>>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.hideLoading();
						iHeader.setErrorMessage(MESSAGES.failedToLoadRooms(caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedToLoadRooms(caught.getMessage()), caught);
					}

					@Override
					public void onSuccess(List<FilterRpcResponse.Entity> result) {
						if (result == null || result.isEmpty()) {
							LoadingWidget.hideLoading();
							iHeader.setErrorMessage(MESSAGES.errorNoRoomsMatchingFilter());
						} else if (result.size() == 1) {
							LoadingWidget.hideLoading();
							iHeader.setErrorMessage(MESSAGES.errorOnlyOneRoomIsMatchingFilter());
						} else {
							TravelTimesRequest request = TravelTimesRequest.loadRooms();
							for (FilterRpcResponse.Entity e: result) {
								request.addRoom(new Room(e.getUniqueId(), null));
							}
							RPC.execute(request, new AsyncCallback<TravelTimeResponse>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.hideLoading();
									iHeader.setErrorMessage(MESSAGES.failedToLoadMatrix(caught.getMessage()));
									UniTimeNotifications.error(MESSAGES.failedToLoadMatrix(caught.getMessage()), caught);
								}

								@Override
								public void onSuccess(TravelTimeResponse result) {
									iFooter.setVisible(true);
									LoadingWidget.hideLoading();
									if (result.hasRooms()) {
										iMatrix.init(result.getRooms());
										iHeader.clearMessage();
										iMatrix.setEditable(false);
										iHeader.setEnabled("edit", true);
									} else {
										iHeader.setErrorMessage(MESSAGES.failedToLoadMatrixNoRooms());
									}
								}
							});							
						}
					}
					
				});
			}
		});
		hp.add(iShow);
		hp.setCellVerticalAlignment(iShow, HasVerticalAlignment.ALIGN_TOP);
		iShow.setWidth("75px");
		iForm.addRow(hp);

		iForm.addHeaderRow(iHeader);
		
		iMatrix = new Matrix();
		
		ScrollPanel scroll = new ScrollPanel(iMatrix);
		ToolBox.setMaxWidth(scroll.getElement().getStyle(), (Window.getClientWidth() - 100) + "px");
		
		iForm.addRow(scroll);
		
		iFooter = iHeader.clonePanel("");
		iForm.addBottomRow(iFooter);
		
		iHeader.setVisible(false);
		iFooter.setVisible(false);
		
		initWidget(iForm);
	}
	
	public static class P extends AbsolutePanel implements HasMouseDownHandlers {
		private String iCaption;
		
		private P(String caption, String... styles) {
			iCaption = caption;
			if (caption != null)
				getElement().setInnerHTML(caption);
			for (String style: styles)
				if (style != null && !style.isEmpty())
					addStyleName(style);
			sinkEvents(Event.ONMOUSEDOWN);
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
		    case Event.ONMOUSEDOWN:
		    	MouseDownEvent.fireNativeEvent(event, this);
		    	event.stopPropagation();
		    	event.preventDefault();
		    	break;
			}
		}
		
		@Override
		public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
			return addHandler(handler, MouseDownEvent.getType());
		}

		public String getCaption() { return iCaption; }
	}
	
	private class Matrix extends FlexTable {
		private List<Room> iRooms;
		
		public Matrix() {
			super();
			setStyleName("unitime-TravelTimes");
			setCellPadding(0);
			setCellSpacing(1);
		}
		
		public void clear() {
			for (int row = getRowCount() - 1; row >= 0; row--)
				removeRow(row);
		}
		
		public void init(List<Room> rooms) {
			iRooms = rooms;
			
			clear();

			int col = 1;
			int row = 0;
			for (Room room: getRooms()) {
				setWidget(row, col, new P(room.getName(), "horizontal-header")); 
				getCellFormatter().setVerticalAlignment(row, col, HasVerticalAlignment.ALIGN_BOTTOM);
				col++;
			}
			
			boolean rMark = false;
			Building prev = null;
			for (Room room: getRooms()) {
				row ++; col = 0;
				if (row > 1)
					if (prev == null) {
						if (room.getBuilding() != null)
							rMark = !rMark;
					} else {
						if (!prev.equals(room.getBuilding())) {
							rMark = !rMark;
						}
					}
				
				setWidget(row, col, new P(room.getName(), "vertical-header"));
				getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
				col ++;
				
				Building otherPrev = null;
				boolean cMark = false;
				for (Room other: getRooms()) {
					if (col > 1)
						if (otherPrev == null) {
							if (other.getBuilding() != null)
								cMark = !cMark;
						} else {
							if (!otherPrev.equals(other.getBuilding()))
								cMark = !cMark;
						}
					if (row == col) {
						setWidget(row, col, new Time(row, col)); col++;
					} else {
						Time cell = new Time(row, col, room, other);
						if (row > col) {
							cell.setOther((Time)getWidget(col, row));
							if (rMark) cell.addStyleName("mark");
						} else {
							if (cMark) cell.addStyleName("mark");
						}
						setWidget(row, col++, cell);
					}
					otherPrev = other.getBuilding();
				}
				prev = room.getBuilding();
				setWidget(row, col, new P(room.getName(), "vertical-header"));
				getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_LEFT);
			}
			
			col = 1;
			row ++;
			for (Room room: getRooms()) {
				setWidget(row, col, new P(room.getName(), "horizontal-header")); 
				getCellFormatter().setVerticalAlignment(row, col, HasVerticalAlignment.ALIGN_TOP);
				col++;
			}
			
		}
		
		public List<Room> getRooms() { return iRooms ;}

		public void setEditable(boolean editable) {
			for (int row = 1; row < getRowCount() - 1; row++)
				for (int col = 1; col < getCellCount(row) - 1; col++) {
					Time time = (Time) getWidget(row, col);
					if (editable) {
						time.getElement().getStyle().clearBorderColor();
						time.setReadOnly(row == col);
					} else {
						time.getElement().getStyle().setBorderColor("transparent");
						time.setReadOnly(true);
					}
				}
		}
		
		public void onCancelChanges() {
			for (int row = 1; row < getRowCount() - 1; row++)
				for (int col = 1; col < getCellCount(row) - 1; col++) {
					Time time = (Time) getWidget(row, col);
					time.onCancelChanges();
				}
		}
		
		public void onSaveChanges() {
			for (int row = 1; row < getRowCount() - 1; row++)
				for (int col = 1; col < getCellCount(row) - 1; col++) {
					Time time = (Time) getWidget(row, col);
					time.onSaveChanges();
				}
		}

	}
	
	
	private KeyDownHandler iKeyDownHandler = new KeyDownHandler() {
		@Override
		public void onKeyDown(KeyDownEvent event) {
        	Time time = (Time) event.getSource();
        	
            if (time.isReadOnly() || !time.isEnabled()) return;

            int row = time.getRow();
            int col = time.getColumn();

            switch (event.getNativeEvent().getKeyCode()) {
            case KeyCodes.KEY_DOWN:
            	while (true) {
            		row ++;
            		if (row > iMatrix.getRowCount() - 2) {
            			row = 1;
            			col ++;
            			if (col > iMatrix.getCellCount(row) - 2) col = 1;
            		}
            		Time t = (Time)iMatrix.getWidget(row, col);
            		if (t.isEnabled() && !t.isReadOnly()) {
            			t.setFocus(true); t.selectAll(); break;
            		}
            	}
            	time.cancelKey();
            	return;
            case KeyCodes.KEY_UP:
            	while (true) {
            		row --;
                	if (row <= 0) {
                		row = iMatrix.getRowCount() - 2;
                		col --;
                		if (col <= 0) col = iMatrix.getCellCount(row) - 2;
                	}
                	Time t = (Time)iMatrix.getWidget(row, col);
            		if (t.isEnabled() && !t.isReadOnly()) {
            			t.setFocus(true); t.selectAll(); break;
            		}
            	}
            	time.cancelKey();
            	return;
            case KeyCodes.KEY_LEFT:
            	while (true) {
            		col --;
            		if (col <= 0) {
            			col = iMatrix.getCellCount(row) - 2;
            			row --;
            			if (row <= 0) row = iMatrix.getRowCount() - 2;
            		}
            		Time t = (Time)iMatrix.getWidget(row, col);
            		if (t.isEnabled() && !t.isReadOnly()) {
            			t.setFocus(true); t.selectAll(); break;
            		}
            	}
            	time.cancelKey();
            	return;
            case KeyCodes.KEY_RIGHT:
            	while (true) {
            		col ++;
            		if (col > iMatrix.getCellCount(row) - 2) {
            			col = 1;
            			row ++;
            			if (row > iMatrix.getRowCount() - 2) row = 1;
            		}
            		Time t = (Time)iMatrix.getWidget(row, col);
            		if (t.isEnabled() && !t.isReadOnly()) {
            			t.setFocus(true); t.selectAll(); break;
            		}
            	}
            	time.cancelKey();
            	return;
            }
		}
		
	};
	
	private KeyPressHandler iKeyPressHandler = new KeyPressHandler() {
        @Override
        public void onKeyPress(KeyPressEvent event) {
        	if (!Character.isDigit(event.getCharCode()) && event.getUnicodeCharCode() != 0)
            	((TextBox)event.getSource()).cancelKey();
        }
    };

	public class Time extends TextBox {
		private Time iOther = null;
		private int iRow, iCol;
		private Room iR1, iR2;
		private boolean iDefault = false;
		private Integer iInitialTravel = null;
		
		public Time(int row, int col) {
    		super();
    		iRow = row; iCol = col;
    		setReadOnly(true); setEnabled(false);
    		setStyleName("gwt-SuggestBox");
			addStyleName("cell");
			addStyleName("disabled");
		}
		
		public Time(int row, int col, Room r1, Room r2) {
			super();
			iRow = row; iCol = col;
			setStyleName("gwt-SuggestBox");
			addStyleName("cell");
			iR1 = r1; iR2 = r2;
			iInitialTravel = r1.getTravelTime(r2.getId());
			if (iInitialTravel == null) {
				Integer distance = r1.getDistance(r2.getId());
				if (distance != null) {
					setValue(distance.toString());
					iDefault = true;
					addStyleName("default");
				}
			} else {
				setValue(iInitialTravel.toString());
			}
			final PopupPanel popup = new PopupPanel(true, false);
			popup.setStyleName("unitime-PopupHint");
			popup.add(new P(r1.getName() + " &harr; " + r2.getName()));
			addFocusHandler(new FocusHandler() {
				@Override
				public void onFocus(FocusEvent event) {
					if (!popup.isShowing())
						popup.showRelativeTo(Time.this);
					if (iDefault)
						removeStyleName("default");
				}
			});
			addBlurHandler(new BlurHandler() {
				@Override
				public void onBlur(BlurEvent event) {
					if (popup.isShowing()) popup.hide();
					if (iDefault)
						addStyleName("default");
				}
			});
			setMaxLength(3);
			addKeyDownHandler(iKeyDownHandler);
			addKeyPressHandler(iKeyPressHandler);
		}
		
		public void onCancelChanges() {
			if (iRow == iCol) return;
			iR1.setTravelTime(iR2, iInitialTravel);
			if (iInitialTravel == null) {
				Integer distance = iR1.getDistance(iR2.getId());
				if (distance != null) {
					setValue(distance.toString());
					iDefault = true;
					addStyleName("default");
				}
			} else {
				setValue(iInitialTravel.toString());
			}
		}
		
		public void onSaveChanges() {
			if (iRow == iCol) return;
			iInitialTravel = iR1.getTravelTime(iR2);
		}
		
		private void onChange(ValueChangeEvent<String> event) {
			Integer travel = null;
			try {
				if (!event.getValue().isEmpty())
					travel = Integer.valueOf(event.getValue());
			} catch (Exception e) {}
			iR1.setTravelTime(iR2, travel);
			if (travel != null) {
				if (iDefault) {
					iDefault = false;
					removeStyleName("default");
				}
			} else {
				Integer distance = iR1.getDistance(iR2.getId());
				if (distance != null) {
					setValue(distance.toString());
					iDefault = true;
					addStyleName("default");
				}
			}
		}
		
		public int getRow() { return iRow; }
		public int getColumn() { return iCol; }
		
		public void setOther(Time other) {
			iOther = other;
			iOther.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					setValue(event.getValue());
					iOther.onChange(event);
					onChange(event);
				}
			});
			other.iOther = this;
			addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					iOther.setValue(event.getValue());
					iOther.onChange(event);
					onChange(event);
				}
			});
		}
		
		public Room getFirstRoom() { return iR1; }
		public Room getSecondRoom() { return iR2; }
		
	}
	
	public static class Building implements IsSerializable {
		private Long iId;
		private String iName;
		
		public Building() {}
		public Building(Long id, String name) { iId = id; iName = name; }
		
		public Long getId() { return iId; }
		public String getName() { return iName; }

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Building)) return false;
			return getId().equals(((Building)o).getId());
		}
	}
	
	
	public static class Room implements IsSerializable {
		private Long iId;
		private String iName;
		private Building iBuilding;
		private Map<Long, Integer> iTravelTimes = new HashMap();
		private Map<Long, Integer> iDistances = new HashMap();
		
		public Room() {}
		public Room(Long id, String name) { this(id, name, null); }
		public Room(Long id, String name, Building building) {
			iId = id; iName = name;
			iBuilding = building;
		}
		
		public Long getId() { return iId; }
		public String getName() { return iName; }
		public boolean hasBuilding() { return iBuilding != null; }
		public Building getBuilding() { return iBuilding; }
		
		public Integer getTravelTime(Long roomId) {
			return iTravelTimes.get(roomId);
		}
		public Integer getTravelTime(Room room) {
			return getTravelTime(room.getId());
		}
		
		public void setTravelTime(Long roomId, Integer time) {
			if (time == null)
				iTravelTimes.remove(roomId);
			else
				iTravelTimes.put(roomId, time);
		}
		public void setTravelTime(Room room, Integer time) {
			setTravelTime(room.getId(), time);
		}
		
		public Integer getDistance(Long roomId) {
			return iDistances.get(roomId);
		}
		public Integer getDistance(Room room) {
			return getDistance(room.getId());
		}
		
		public void setDistance(Long roomId, Integer time) {
			if (time == null)
				iDistances.remove(roomId);
			else
				iDistances.put(roomId, time);
		}
		public void setDistance(Room room, Integer time) {
			setDistance(room.getId(), time);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Room)) return false;
			return getId().equals(((Room)o).getId());
		}
	}
	
	private class Session implements AcademicSessionProvider {
		private List<AcademicSessionChangeHandler> iHandlers = new ArrayList<AcademicSessionProvider.AcademicSessionChangeHandler>();
		private Long iId;
		private String iName;
		
		private Session() {
			RPC.execute(TravelTimesRequest.init(), new AsyncCallback<TravelTimeResponse>() {

				@Override
				public void onFailure(Throwable caught) {
					iHeader.setErrorMessage("Failed to initialize: " + caught.getMessage());
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
	
	public static class TravelTimesRequest implements GwtRpcRequest<TravelTimeResponse> {
		public static enum Command implements IsSerializable {
			INIT,
			LOAD,
			SAVE
		}
		
		private Command iCommand;
		private List<Room> iRooms;
		
		public TravelTimesRequest() {}
		private TravelTimesRequest(Command command, List<Room> rooms) { iCommand = command; iRooms = rooms; }
		
		public static TravelTimesRequest loadRooms() { return new TravelTimesRequest(Command.LOAD, null); }
		public static TravelTimesRequest saveRooms(List<Room> rooms) { return new TravelTimesRequest(Command.SAVE, rooms); }
		public static TravelTimesRequest init() { return new TravelTimesRequest(Command.INIT, null); }
		
		public Command getCommand() { return iCommand; }
		
		public boolean hasRooms() { return iRooms != null && !iRooms.isEmpty(); }
		public List<Room> getRooms() { return iRooms; }
		public void addRoom(Room room) {
			if (iRooms == null) { iRooms = new ArrayList<Room>(); }
			iRooms.add(room);
		}
		
		@Override
		public String toString() {
			return getCommand().name() + (hasRooms() ? getRooms() : "");
		}
	}
	
	public static class TravelTimeResponse implements GwtRpcResponse {
		private Long iSessionId;
		private String iSessionName;
		private List<Room> iRooms;
		
		public TravelTimeResponse() {}
		public TravelTimeResponse(List<Room> rooms) { iRooms = rooms; }
		public TravelTimeResponse(Long sessionId, String sessionName) { iSessionId = sessionId; iSessionName = sessionName; }
		
		public boolean hasRooms() { return iRooms != null && !iRooms.isEmpty(); }
		public List<Room> getRooms() { return iRooms; }
		public void addRoom(Room room) {
			if (iRooms == null) { iRooms = new ArrayList<Room>(); }
			iRooms.add(room);
		}
		
		public Long getSessionId() { return iSessionId; }
		public String getSessionName() { return iSessionName; }
	}

}
