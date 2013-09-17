/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.events.EventAdd.EventPropertiesProvider;
import org.unitime.timetable.gwt.client.events.StartEndTimeSelector.StartEndTime;
import org.unitime.timetable.gwt.client.rooms.RoomHint;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.TimeSelector.TimeUtils;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConflictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.UIObject;

public class AddMeetingsDialog extends UniTimeDialogBox {
	private static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private static DateTimeFormat sDayOfWeek = DateTimeFormat.getFormat("EEEE");
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	private static DateTimeFormat sAriaDateFormat = DateTimeFormat.getFormat(CONSTANTS.dateSelectionDateFormat());
	
	private SimpleForm iDatesForm, iAvailabilityForm;
	private UniTimeHeaderPanel iDatesHeader, iAvailabilityHeader;
	
	private P iRoomAvailability;
	private AriaTextBox iText;
	private Set<String> iSelected = new HashSet<String>();
	private EventRoomAvailabilityRpcResponse iResponse;
	private Map<String, P> iPanels = new HashMap<String, P>();

	private StartEndTimeSelector iTimes;
	private SessionDatesSelector iDates;
	private RoomFilterBox iRooms;
	private List<Entity> iMatchingRooms;
	
	private ScrollPanel iScrollDates;
	private ScrollPanel iScrollRooms;
	private int iIndex = 0, iStep = 10;
	private Long iEventId = null;
	
	private AsyncCallback<List<MeetingInterface>> iCallback;
	private AcademicSessionProvider iSession;
	private EventPropertiesProvider iProperties;
	
	
	public AddMeetingsDialog(AcademicSessionProvider session, EventPropertiesProvider propeties, AsyncCallback<List<MeetingInterface>> callback) {
		super(true, true);
		setAnimationEnabled(false);
		
		iCallback = callback;
		iSession = session;
		iProperties = propeties;
		
		setText(MESSAGES.dialogAddMeetings());
		setEscapeToHide(true);
		
		iDatesForm = new SimpleForm();
		
		iDatesHeader = new UniTimeHeaderPanel();
		iDatesHeader.setRotateFocus(true);
		iDatesHeader.addButton("next", MESSAGES.buttonNext(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iDates.getSelectedDaysCount() == 0) {
					iDatesHeader.setErrorMessage(MESSAGES.errorNoDateSelected());
					AriaStatus.getInstance().setText(MESSAGES.errorNoDateSelected());
					return;
				} else if (getStartSlot() == null) {
					iDatesHeader.setErrorMessage(MESSAGES.errorNoStartTime());
					AriaStatus.getInstance().setText(MESSAGES.errorNoStartTime());
					return;
				} else if (getEndSlot() == null) {
					iDatesHeader.setErrorMessage(MESSAGES.errorNoEndTime());
					AriaStatus.getInstance().setText(MESSAGES.errorNoEndTime());
					return;
				}
				LoadingWidget.getInstance().show(MESSAGES.waitCheckingRoomAvailability());
				iRooms.getElements(new AsyncCallback<List<Entity>>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iDatesHeader.setErrorMessage(caught.getMessage());
						AriaStatus.getInstance().setText(caught.getMessage());
					}
					@Override
					public void onSuccess(List<Entity> result) {
						iMatchingRooms = result;
						if (result == null || result.isEmpty()) {
							LoadingWidget.getInstance().hide();
							iDatesHeader.setErrorMessage(MESSAGES.errorNoMatchingRooms());
							AriaStatus.getInstance().setText(MESSAGES.errorNoMatchingRooms());
						} else if (iDates.getSelectedDaysCount() > 0) {
							iDatesHeader.clearMessage();
							if (iProperties != null && iProperties.isTooEarly(getStartSlot(), getEndSlot())) {
								iAvailabilityHeader.setWarningMessage(MESSAGES.warnMeetingTooEarly(TimeUtils.slot2time(getStartSlot()) + " - " + TimeUtils.slot2time(getEndSlot())));
								AriaStatus.getInstance().setText(MESSAGES.warnMeetingTooEarly(TimeUtils.slot2aria(getStartSlot()) + " - " + TimeUtils.slot2aria(getEndSlot())));
							} else {
								iAvailabilityHeader.clearMessage();
							}
							RPC.execute(EventRoomAvailabilityRpcRequest.checkAvailability(
										getStartSlot(), getEndSlot(), getDates(), getRooms(), iEventId, iSession.getAcademicSessionId()
									), new AsyncCallback<EventRoomAvailabilityRpcResponse>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									iDatesHeader.setErrorMessage(caught.getMessage());
									AriaStatus.getInstance().setText(caught.getMessage());
								}
								
								@Override
								public void onSuccess(EventRoomAvailabilityRpcResponse result) {
									LoadingWidget.getInstance().hide();
									populate(result, 0, EventCookie.getInstance().getRoomsSortBy());
									setWidget(iAvailabilityForm);
									recenter();
									iText.setFocus(true);
								}
							});
						}
					}
				});
			}
		});
		
		iDatesForm.addHeaderRow(iDatesHeader);
		
		SimpleForm form = new SimpleForm(); form.removeStyleName("unitime-NotPrintableBottomLine");
		
		iDates = new SessionDatesSelector(session);
		form.addRow(MESSAGES.propDates(), iDates);
		
		iTimes = new StartEndTimeSelector();
		form.addRow(MESSAGES.propTimes(), iTimes);
		
		iRooms = new RoomFilterBox(session);
		form.addRow(MESSAGES.propLocations(), iRooms);
		
		iScrollDates = new ScrollPanel(form);
		iScrollDates.setStyleName("unitime-VerticalScrollPanel");
		
		iDatesForm.addRow(iScrollDates);
		
		iDatesForm.addBottomRow(iDatesHeader.clonePanel());
		
		iAvailabilityForm = new SimpleForm();
		
		iAvailabilityHeader = new UniTimeHeaderPanel();
		iAvailabilityHeader.setRotateFocus(true);
		
		iAvailabilityHeader.addButton("dates", MESSAGES.buttonPrevious(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setWidget(iDatesForm);
				recenter();
				iResponse = null;
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						iDates.setFocus(true);
					}
				});
			}
		});
		iAvailabilityHeader.addButton("prev", MESSAGES.buttonLeft(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				populate(iResponse, iIndex - iStep, null);
				recenter();
			}
		});
		iAvailabilityHeader.addButton("next", MESSAGES.buttonRight(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				populate(iResponse, iIndex + iStep, null);
				recenter();
			}
		});
		iAvailabilityHeader.addButton("more", MESSAGES.buttonMoreOperations(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				for (final SortRoomsBy sortBy: SortRoomsBy.values()) {
					if (sortBy == SortRoomsBy.DISTANCE && !iRooms.hasChip(new Chip("flag", "Nearby"))) continue;
					MenuItem item = new MenuItem(
							(sortBy.ordinal() == EventCookie.getInstance().getRoomsSortBy() ? "&uarr; " :
							(sortBy.ordinal() + SortRoomsBy.values().length == EventCookie.getInstance().getRoomsSortBy()) ? "&darr; " : "") +
							MESSAGES.opSortBy(getSortRoomsByName(sortBy)), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							if (sortBy.ordinal() == EventCookie.getInstance().getRoomsSortBy()) {
								EventCookie.getInstance().setSortRoomsBy(SortRoomsBy.values().length + sortBy.ordinal());
							} else {
								EventCookie.getInstance().setSortRoomsBy(sortBy.ordinal());
							}
							populate(iResponse, 0, EventCookie.getInstance().getRoomsSortBy());
							recenter();
						}
					});
					item.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(item);
				}
				menu.addSeparator();
				MenuItem swapItem = new MenuItem(MESSAGES.opSwapAxes(), true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						EventCookie.getInstance().setRoomsHorizontal(!EventCookie.getInstance().areRoomsHorizontal());
						populate(iResponse, 0, EventCookie.getInstance().getRoomsSortBy());
						recenter();
					}
				});
				swapItem.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(swapItem);
				MenuItem expandOrCollapseAll = new MenuItem(EventCookie.getInstance().isExpandRoomConflicts() ? MESSAGES.opCollapseAll() : MESSAGES.opExpandAll(), true, new Command() {
					@Override
					public void execute() {
						popup.hide();
						EventCookie.getInstance().setExpandRoomConflicts(!EventCookie.getInstance().isExpandRoomConflicts());
						populate(iResponse, iIndex, EventCookie.getInstance().getRoomsSortBy());
						recenter();
					}
				});
				expandOrCollapseAll.getElement().getStyle().setCursor(Cursor.POINTER);
				menu.addItem(expandOrCollapseAll);
				menu.setVisible(true);
				menu.setFocusOnHoverEnabled(true);
				popup.add(menu);
				popup.showRelativeTo((UIObject)event.getSource());
				if (event.getSource() instanceof Focusable) {
					final Focusable focusable = (Focusable)event.getSource();
					popup.addCloseHandler(new CloseHandler<PopupPanel>() {
						@Override
						public void onClose(CloseEvent<PopupPanel> event) {
							focusable.setFocus(true);
						}
					});
				}
				menu.focus();
			}
		});
		iAvailabilityHeader.addButton("select", MESSAGES.buttonSelect(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
				List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
				
				for (Integer date: getDates()) {
					for (Entity room: getRooms()) {
						if (isSelected(date, room)) {
							MeetingInterface meeting = new MeetingInterface();
							
							meeting.setDayOfYear(date);
							meeting.setStartSlot(getStartSlot());
							meeting.setEndSlot(getEndSlot());
							meeting.setStartOffset(0);
							meeting.setEndOffset(-Integer.parseInt(room.getProperty("breakTime", "0")));

							
							ResourceInterface location = new ResourceInterface();
							location.setId(room.getUniqueId());
							location.setType(ResourceType.ROOM);
							location.setName(room.getName());
							location.setSize(Integer.valueOf(room.getProperty("capacity", null)));
							meeting.setLocation(location);
							
							meeting.setConflicts(getConflicts(date, room));

							meetings.add(meeting);
						}
					}
				}
				
				iCallback.onSuccess(meetings);
			}
		});
		
		iAvailabilityForm.addHeaderRow(iAvailabilityHeader);
		
		iRoomAvailability = new P("unitime-MeetingSelection");
		
		iText = new AriaTextBox();
		iText.setStyleName("text");
		
		iScrollRooms = new ScrollPanel(iRoomAvailability);

		iAvailabilityForm.addRow(iScrollRooms);
		
		iAvailabilityForm.addNotPrintableBottomRow(iAvailabilityHeader.clonePanel());
		
		setWidget(iDatesForm);
		
		addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
			}
		});
		
		iText.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (iResponse == null) return;
		    	if (EventCookie.getInstance().areRoomsHorizontal()) {
		        	int min = iIndex, max = Math.min(iIndex + iStep, getRooms().size()) - 1;
		        	switch (event.getNativeKeyCode()) {
	            	case KeyCodes.KEY_DOWN:
	            		if (iHoverDate != null && iHoverLoc != null) {
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).removeStyleName("hover");
	            			int idx = Math.min(Math.max(0, getDates().indexOf(iHoverDate) + 1), getDates().size() - 1);
	            			iHoverDate = getDates().get(idx);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		} else {
	            			iHoverDate = getDates().get(0);
	            			iHoverLoc = getRooms().get(min);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		}
	            		iScrollRooms.ensureVisible(iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()));
	            		break;
	            	case KeyCodes.KEY_UP:
	            		if (iHoverDate != null && iHoverLoc != null) {
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).removeStyleName("hover");
	            			int idx = Math.min(Math.max(0, getDates().indexOf(iHoverDate) - 1), getDates().size() - 1);
	            			iHoverDate = getDates().get(idx);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		} else {
	            			iHoverDate = getDates().get(getDates().size() - 1);
	            			iHoverLoc = getRooms().get(min);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		}
	            		iScrollDates.ensureVisible(iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()));
	            		break;
	            	case KeyCodes.KEY_RIGHT:
	            		if (iHoverDate != null && iHoverLoc != null) {
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).removeStyleName("hover");
	            			int idx = Math.min(Math.max(min, getRooms().indexOf(iHoverLoc) + 1), max);
	            			iHoverLoc = getRooms().get(idx);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		} else {
	            			iHoverDate = getDates().get(0);
	            			iHoverLoc = getRooms().get(min);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		}
	            		break;
	            	case KeyCodes.KEY_LEFT:
	            		if (iHoverDate != null && iHoverLoc != null) {
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).removeStyleName("hover");
	            			int idx = Math.min(Math.max(min, getRooms().indexOf(iHoverLoc) - 1), max);
	            			iHoverLoc = getRooms().get(idx);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		} else {
	            			iHoverDate = getDates().get(0);
	            			iHoverLoc = getRooms().get(max);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		}
	            		break;
	            	case KeyCodes.KEY_PAGEDOWN:
	            		if (iIndex + iStep < getRooms().size())
	            			populate(iResponse, iIndex + iStep, null);
	            		break;
	            	case KeyCodes.KEY_PAGEUP:
	            		if (iIndex > 0)
	            			populate(iResponse, iIndex - iStep, null);
	            		break;
	            	case 32:
	            	case KeyCodes.KEY_ENTER:
	            		if (iHoverDate != null && iHoverLoc != null) {
	            			setSelected(iHoverDate, iHoverLoc, !isSelected(iHoverDate, iHoverLoc));
	            		}
	            		break;
	            	}
		    	} else {
		    		int min = iIndex, max = Math.min(iIndex + iStep, getDates().size()) - 1;
		    		switch (event.getNativeKeyCode()) {
	            	case KeyCodes.KEY_RIGHT:
	            		if (iHoverDate != null && iHoverLoc != null) {
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).removeStyleName("hover");
	            			int idx = Math.min(Math.max(min, getDates().indexOf(iHoverDate) + 1), max);
	            			iHoverDate = getDates().get(idx);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		} else {
	            			iHoverDate = getDates().get(min);
	            			iHoverLoc = getRooms().get(0);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		}
	            		iScrollRooms.ensureVisible(iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()));
	            		break;
	            	case KeyCodes.KEY_LEFT:
	            		if (iHoverDate != null && iHoverLoc != null) {
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).removeStyleName("hover");
	            			int idx = Math.min(Math.max(min, getDates().indexOf(iHoverDate) - 1), max);
	            			iHoverDate = getDates().get(idx);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		} else {
	            			iHoverDate = getDates().get(max);
	            			iHoverLoc = getRooms().get(0);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		}
	            		iScrollDates.ensureVisible(iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()));
	            		break;
	            	case KeyCodes.KEY_DOWN:
	            		if (iHoverDate != null && iHoverLoc != null) {
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).removeStyleName("hover");
	            			int idx = Math.min(Math.max(0, getRooms().indexOf(iHoverLoc) + 1), getRooms().size() - 1);
	            			iHoverLoc = getRooms().get(idx);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		} else {
	            			iHoverDate = getDates().get(min);
	            			iHoverLoc = getRooms().get(0);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		}
	            		break;
	            	case KeyCodes.KEY_UP:
	            		if (iHoverDate != null && iHoverLoc != null) {
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).removeStyleName("hover");
	            			int idx = Math.min(Math.max(0, getRooms().indexOf(iHoverLoc) - 1), getRooms().size() - 1);
	            			iHoverLoc = getRooms().get(idx);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		} else {
	            			iHoverDate = getDates().get(min);
	            			iHoverLoc = getRooms().get(getRooms().size() - 1);
	            			iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
	            		}
	            		break;
	            	case KeyCodes.KEY_PAGEDOWN:
	            		if (iIndex + iStep < getDates().size())
	            			populate(iResponse, iIndex + iStep, null);
	            		break;
	            	case KeyCodes.KEY_PAGEUP:
	            		if (iIndex > 0)
	            			populate(iResponse, iIndex - iStep, null);
	            		break;
	            	case 32:
	            	case KeyCodes.KEY_ENTER:
	            		if (iHoverDate != null && iHoverLoc != null) {
	            			setSelected(iHoverDate, iHoverLoc, !isSelected(iHoverDate, iHoverLoc));
	            		}
	            		break;
		    		}
		    	}
		    	iText.setAriaLabel(toAriaLabel(true, true, false));
			}
		});
		iText.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				switch (event.getNativeEvent().getKeyCode()) {
				case KeyCodes.KEY_RIGHT:
				case KeyCodes.KEY_LEFT:
				case KeyCodes.KEY_UP:
				case KeyCodes.KEY_DOWN:
				case KeyCodes.KEY_PAGEDOWN:
				case KeyCodes.KEY_PAGEUP:
					event.preventDefault();
				}
			}
		});
		iText.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				switch (event.getNativeEvent().getKeyCode()) {
            	case 32:
            		if (iText.getValue().isEmpty())
            			event.preventDefault();
            		return;
				case KeyCodes.KEY_ENTER:
            		event.preventDefault();
            		return;
				}
			}
		});
		iText.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				if (iHoverDate != null && iHoverLoc != null)
					iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).addStyleName("hover");
				iText.setAriaLabel(toAriaLabel(true, true, false));
			}
		});
		iText.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if (iHoverDate != null && iHoverLoc != null) {
					iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()).removeStyleName("hover");
				}
			}
		});
	}
	
	static enum SortRoomsBy {
		CAPACITY,
		NAME,
		DISTANCE,
		AVAILABILITY
	}
	
	public static String getSortRoomsByName(SortRoomsBy sortBy) {
		switch (sortBy) {
		case CAPACITY:
			return MESSAGES.colCapacity();
		case NAME:
			return MESSAGES.colName();
		case DISTANCE:
			return MESSAGES.colRoomDistance();
		case AVAILABILITY:
			return MESSAGES.colRoomAvailability();
		default:
			return null;
		}
	}
	
	public static Comparator<Entity> getSortRoomsComparator(SortRoomsBy sortBy, final boolean preferSize, final List<Integer> dates, final EventRoomAvailabilityRpcResponse availability) {
		switch (sortBy) {
		case CAPACITY:
			return new Comparator<Entity>() {
				@Override
				public int compare(Entity r1, Entity r2) {
					int cmp = Integer.valueOf(r1.getProperty("capacity", "0")).compareTo(Integer.valueOf(r2.getProperty("capacity", "0")));
					if (cmp != 0) return cmp;
					cmp = r1.getName().compareTo(r2.getName());
					return (cmp == 0 ? r1.getUniqueId().compareTo(r2.getUniqueId()) : cmp);
				}
				
			};
		case NAME:
			return new Comparator<Entity>() {
				@Override
				public int compare(Entity r1, Entity r2) {
					int cmp = r1.getName().compareTo(r2.getName());
					return (cmp == 0 ? r1.getUniqueId().compareTo(r2.getUniqueId()) : cmp);
				}
				
			};
		case DISTANCE:
			return new Comparator<Entity>() {
				@Override
				public int compare(Entity r1, Entity r2) {
					int cmp = Integer.valueOf(r1.getProperty("distance", "0")).compareTo(Integer.valueOf(r2.getProperty("distance", "0")));
					if (cmp != 0) return cmp;
					if (preferSize) {
						cmp = Integer.valueOf(r1.getProperty("capacity", "0")).compareTo(Integer.valueOf(r2.getProperty("capacity", "0")));
						if (cmp != 0) return cmp;
					}
					cmp = r1.getName().compareTo(r2.getName());
					return (cmp == 0 ? r1.getUniqueId().compareTo(r2.getUniqueId()) : cmp);
				}
			};
		case AVAILABILITY:
			return new Comparator<Entity>() {
				@Override
				public int compare(Entity r1, Entity r2) {
					if (dates != null && availability != null) {
						Long p1 = Long.valueOf(r1.getProperty("permId", "-1"));
						Long p2 = Long.valueOf(r2.getProperty("permId", "-1"));
						int a1 = 0, a2 = 0;
						for (Integer date: dates) {
							Set<MeetingConflictInterface> c1 = availability.getOverlaps(date, p1);
							Set<MeetingConflictInterface> c2 = availability.getOverlaps(date, p2);
							if (c1 == null || c1.isEmpty()) a1 ++;
							if (c2 == null || c2.isEmpty()) a2 ++;
						}
						if (a1 > a2) return -1;
						if (a1 < a2) return 1;						
					}
					int cmp = Integer.valueOf(r1.getProperty("distance", "0")).compareTo(Integer.valueOf(r2.getProperty("distance", "0")));
					if (cmp != 0) return cmp;
					if (preferSize) {
						cmp = Integer.valueOf(r1.getProperty("capacity", "0")).compareTo(Integer.valueOf(r2.getProperty("capacity", "0")));
						if (cmp != 0) return cmp;
					}
					cmp = r1.getName().compareTo(r2.getName());
					return (cmp == 0 ? r1.getUniqueId().compareTo(r2.getUniqueId()) : cmp);
				}
				
			};
		default:
			return null;
		}
	}
	
	public static Comparator<Entity> inverse(final Comparator<Entity> cmp) {
		return new Comparator<Entity>() {
			@Override
			public int compare(Entity r1, Entity r2) {
				return - cmp.compare(r1, r2);
			}
		};
	}
	
	public void showDialog(Long eventId) {
		iStep = (Window.getClientWidth() - 300) / 105;
		ToolBox.setMaxHeight(iScrollRooms.getElement().getStyle(), (Window.getClientHeight() - 200) + "px");
		ToolBox.setMaxHeight(iScrollDates.getElement().getStyle(), (Window.getClientHeight() - 200) + "px");
		int nrMonths = Math.max(3, Math.min(5, (Window.getClientWidth() - 300) / 225));
		iDates.setWidth((225 * nrMonths) + "px");
		
		iResponse = null;
		iEventId = eventId;
		setWidget(iDatesForm);

		center();
		RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
		iDates.setFocus(true);
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && getElement().getClientHeight() > Window.getClientHeight() - 100)
			recenter();
	}
	
	@Override
	public void hide() {
		super.hide();
		GwtHint.getInstance().hide();
	}
		
	public List<Entity> getRooms() { return iMatchingRooms; }
	public Integer getStartSlot() { return iTimes.getValue().getStart(); }
	public Integer getEndSlot() { return iTimes.getValue().getEnd(); }
	public List<Integer> getDates() { return iDates.getSelectedDays(); }
	public boolean isSelected(Integer date, Entity room) {
		return iSelected.contains(date + ":" + room.getUniqueId());
	}
	
	public void reset(String roomFilterValue, List<Date> selectedDates, StartEndTime selectedTimes) {
		reset(roomFilterValue, null, selectedDates, selectedTimes);
	}
	
	public void reset(String roomFilterValue, List<MeetingInterface> meetings, List<Date> selectedDates, StartEndTime selectedTimes) {
		iMatchingRooms = null;
		iDates.setValue(selectedDates);
		iTimes.setValue(selectedTimes, true); iTimes.setDiff(12);
		iRooms.setValue(roomFilterValue == null || roomFilterValue.isEmpty() ? "flag:Event" : roomFilterValue.contains("flag:All") || roomFilterValue.contains("flag:Event") ? roomFilterValue : "flag:Event " + roomFilterValue, true);
		iSelected.clear();
		if (meetings != null && !meetings.isEmpty()) {
			MeetingInterface first = meetings.get(0);
			iTimes.setValue(new StartEndTimeSelector.StartEndTime(first.getStartSlot(), first.getEndSlot()), true);
			TreeSet<Date> dates = new TreeSet<Date>();
			Set<String> rooms = new TreeSet<String>();
			for (MeetingInterface m: meetings) {
				dates.add(m.getMeetingDate());
				if (m.getLocation() != null) {
					iSelected.add(m.getDayOfYear() + ":" + m.getLocation().getId());
					rooms.add(m.getLocationName());
				}
			}
			iDates.setValue(new ArrayList<Date>(dates));
			String roomFilter = "";
			for (String room: rooms) {
				if (!roomFilter.isEmpty()) roomFilter += " or ";
				roomFilter += room;
			}
			iRooms.setValue((roomFilterValue == null || roomFilterValue.isEmpty() ? "flag:Event" : roomFilterValue.contains("flag:All") || roomFilterValue.contains("flag:Event") ? roomFilterValue : "flag:Event " + roomFilterValue) + (roomFilter.isEmpty() ? "" : " " + roomFilter), true);
		}
	}

	private Integer iHoverDate = null;
	private Entity iHoverLoc = null;
	
	private void populate(EventRoomAvailabilityRpcResponse response, int index, Integer sortBy) {
		if (sortBy != null && sortBy >= 0 && sortBy < SortRoomsBy.values().length) {
			Comparator<Entity> comparator = getSortRoomsComparator(SortRoomsBy.values()[sortBy], iRooms.getChip("size") != null, getDates(), response);
			if (comparator != null)
				Collections.sort(iMatchingRooms, comparator);
		} else if (sortBy != null && sortBy >= SortRoomsBy.values().length && sortBy < 2 * SortRoomsBy.values().length) {
			Comparator<Entity> comparator = getSortRoomsComparator(SortRoomsBy.values()[sortBy - SortRoomsBy.values().length], iRooms.getChip("size") != null, getDates(), response);
			if (comparator != null)
				Collections.sort(iMatchingRooms, inverse(comparator));
		}
		
		if (EventCookie.getInstance().areRoomsHorizontal())
			populateHorizontal(response, index);
		else
			populateVertical(response, index);
		AriaStatus.getInstance().setText(toAriaLabel(false, true, true));
	}
	
	protected String conflicts2html(Set<MeetingConflictInterface> conflicts) {
		String conf = "";
		int count = 0;
		for (MeetingConflictInterface event: conflicts) {
			if (EventCookie.getInstance().isExpandRoomConflicts() || conflicts.size() == 1) {
				conf += (conf.isEmpty() ? "" : "<br>") + event.getName() +
						(event.getType() == EventType.Unavailabile ? "" : "<br><span class='type'>" +
								(event.hasLimit() ?
										event.hasEnrollment() ? MESSAGES.addMeetingsLimitEnrollmentAndType(event.getLimit(), event.getEnrollment(),  CONSTANTS.eventTypeShort()[event.getType().ordinal()])
										: MESSAGES.addMeetingsLimitAndType(event.getLimit(), CONSTANTS.eventTypeShort()[event.getType().ordinal()])
								: event.getType().getAbbreviation(CONSTANTS)) + "</span>") +
						"<br><span class='time'>" + TimeUtils.slot2short(event.getStartSlot()) + " - " + TimeUtils.slot2short(event.getEndSlot()) + "</span>";							
			} else {
				if (count == 2 && conflicts.size() > 2) { conf += "<br>..."; break; }
				conf += (conf.isEmpty() ? "" : "<br>") + event.getName() + (event.getType() == EventType.Unavailabile ? "" : " (" + CONSTANTS.eventTypeShort()[event.getType().ordinal()] + ")");
			}
			count ++;
		}
		return conf;
	}
	
	protected String conflicts2text(Entity room, Date d, Set<MeetingConflictInterface> conflicts) {
		String capacity = room.getProperty("capacity", null);
		String distance = room.getProperty("distance", null);
		String hint = room.getProperty("type", null) +
				(capacity != null ? ", " + MESSAGES.hintRoomCapacity(capacity) : "") +
				(distance != null && !"0".equals(distance) ? ", " + MESSAGES.hintRoomDistance(distance) : "");
		String message = ARIA.dateTimeRoomSelection(sAriaDateFormat.format(d), TimeUtils.slot2aria(getStartSlot()), TimeUtils.slot2aria(getEndSlot()), room.getName(), hint);
		if (conflicts != null && !conflicts.isEmpty()) {
			message += ". " + MESSAGES.propConflicts();
			for (MeetingConflictInterface event: conflicts)
				message += " " + (event.getType() == EventType.Unavailabile ? "" : event.getType().getAbbreviation(CONSTANTS)) + " " + event.getName() + " " + ARIA.timeSelection(TimeUtils.slot2aria(event.getStartSlot()), TimeUtils.slot2aria(event.getEndSlot()));
		} else {
			message += ARIA.selectionAvailable();
		}
		return message;
	}
	
	protected String conflicts2hint(Entity room, Date d, Set<MeetingConflictInterface> conflicts) {
		String capacity = room.getProperty("capacity", null);
		String distance = room.getProperty("distance", null);
		String hint = room.getProperty("type", null) +
				(capacity != null ? ", " + MESSAGES.hintRoomCapacity(capacity) : "") +
				(distance != null && !"0".equals(distance) ? ", " + MESSAGES.hintRoomDistance(distance) : "");
		String message = MESSAGES.dateTimeHint(sDayOfWeek.format(d), sDateFormat.format(d), TimeUtils.slot2short(getStartSlot()), TimeUtils.slot2short(getEndSlot())) + 
				"<br>" + room.getName() + " (" + hint + ")";
		if (conflicts != null && !conflicts.isEmpty()) {
			boolean hasSponsorOrInstructor = false, hasLimit = false, hasEnrollment = false;
			for (MeetingConflictInterface event: conflicts) {
				if (event.hasSponsor() || event.hasInstructors()) hasSponsorOrInstructor = true;
				if (event.hasLimit()) hasLimit = true;
				if (event.hasEnrollment()) hasEnrollment = true;
			}
			message += "<br>" + MESSAGES.propConflicts();
			message += "<table border='0' class='table'><tr class='row'>" +
					"<td class='header'>" + MESSAGES.colName() + "</td>" +
					"<td class='header'>" + MESSAGES.colType() + "</td>" +
					"<td class='header'>" + MESSAGES.colTime() + "</td>" +
					(hasEnrollment ? "<td class='header'>" + MESSAGES.colEnrollment() + "</td>" : "") +
					(hasLimit ? "<td class='header'>" + MESSAGES.colLimit() + "</td>" : "") +
					(hasSponsorOrInstructor ? "<td class='header'>" + MESSAGES.colSponsorOrInstructor() + "</td>" : "") +
					"</tr>";
			for (MeetingConflictInterface event: conflicts)
				message += "<tr class='row'><td>" + event.getName() + "</td><td>" + event.getType().getAbbreviation(CONSTANTS) + "</td>" +
					"<td>" + TimeUtils.slot2short(event.getStartSlot()) + " - " + TimeUtils.slot2short(event.getEndSlot()) + "</td>" + 
					(hasEnrollment ? "<td align='right'>" + (event.hasEnrollment() ? event.getEnrollment().toString() : "") + "</td>" : "") +
					(hasLimit ? "<td align='right'>" + (event.hasLimit() ? event.getLimit().toString() : "") + "</td>" : "") +
					(hasSponsorOrInstructor ? "<td>" + (event.hasSponsor() ? event.getSponsor().getName() : event.hasInstructors() ? event.getInstructorNames("<br>", MESSAGES) : "") + "</td>" : "") +
					"</tr>";
			message += "</table>";
		}
		return message;
	}
	
	private void populateHorizontal(EventRoomAvailabilityRpcResponse response, int index) {
		iResponse = response;
		iIndex = index;
		if (iIndex < 0) iIndex = 0;
		if (iIndex >= getRooms().size()) iIndex = iStep * (getRooms().size() / iStep);
		
		iAvailabilityHeader.setEnabled("prev", iIndex > 0);
		iAvailabilityHeader.setEnabled("next", iIndex + iStep < getRooms().size());
		iRoomAvailability.clear();
		iRoomAvailability.add(iText);
		iPanels.clear();
		
		P box = new P("box"); iRoomAvailability.add(box);
		
		P row = new P("row"); box.add(row);
		
		row.add(new P("corner"));
		
		for (int i = iIndex; i < iIndex + iStep && i < getRooms().size(); i++) {
			final Entity room = getRooms().get(i);
			final P p = new P("room");
			p.setHTML(MESSAGES.singleRoomSelection(room.getName(), room.getProperty("type", null), room.getProperty("capacity", null)));
			p.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					if (iHoverDate != null && iHoverLoc != null) {
						P p = iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId());
						if (p != null) p.removeStyleName("hover");
					}
					((P)event.getSource()).addStyleName("hover");
					RoomHint.showHint(p.getElement(), room.getUniqueId(), "", room.getProperty("distance", ""));
				}
			});
			p.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					((P)event.getSource()).removeStyleName("hover");
					RoomHint.hideHint();
				}
			});
			row.add(p);
			p.addMouseDownHandler(new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					boolean selected = true;
					for (Integer date: getDates()) {
						if (!isSelected(date, room)) {
							selected = false;
							setSelected(date, room, true);
						}
					}
					if (selected) {
						for (final Integer date: getDates())
							setSelected(date, room, false);
					}
					event.preventDefault();
					event.stopPropagation();
					iText.setFocus(true);
				}
			});
			
		}
		
		for (final Integer date: getDates()) {
			row = new P("row"); box.add(row);
			
			final P day = new P("date");
			final Date d = iDates.getDate(date);
			day.setHTML(MESSAGES.dateTimeHeader(sDayOfWeek.format(d), sDateFormat.format(d), TimeUtils.slot2short(getStartSlot()), TimeUtils.slot2short(getEndSlot())));
			row.add(day);
			day.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					if (iHoverDate != null && iHoverLoc != null) {
						P p = iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId());
						if (p != null) p.removeStyleName("hover");
					}
					((P)event.getSource()).addStyleName("hover");
				}
			});
			day.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					((P)event.getSource()).removeStyleName("hover");
				}
			});
			day.addMouseDownHandler(new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					boolean selected = true;
					for (int i = iIndex; i < iIndex + iStep && i < getRooms().size(); i++) {
						Entity room = getRooms().get(i);
						if (!isSelected(date, room)) {
							selected = false;
							setSelected(date, room, true);
						}
					}
					if (selected) {
						for (int i = iIndex; i < iIndex + iStep && i < getRooms().size(); i++) {
							Entity room = getRooms().get(i);
							setSelected(date, room, false);
						}
					}
					event.preventDefault();
					event.stopPropagation();
					iText.setFocus(true);
				}
			});
			
			for (int i = iIndex; i < iIndex + iStep && i < getRooms().size(); i++) {
				final Entity room = getRooms().get(i);
				final Set<MeetingConflictInterface> conflicts = response.getOverlaps(date, Long.valueOf(room.getProperty("permId", null)));
				
				final P p = new P("cell");
				
				if (conflicts == null || conflicts.isEmpty()) {
					p.addStyleName("free");
				} else {
					p.addStyleName("conflict");
					p.setHTML(conflicts2html(conflicts));
				}
				Roles.getTextboxRole().setAriaLabelProperty(p.getElement(), conflicts2text(room, d, conflicts));
				
				iPanels.put(date + ":" + room.getUniqueId(), p);
				
				p.addMouseDownHandler(new MouseDownHandler() {
					@Override
					public void onMouseDown(MouseDownEvent event) {
						setSelected(date, room, !isSelected(date, room));
						event.preventDefault();
						event.stopPropagation();
						iText.setFocus(true);
					}
				});
				
				if (isSelected(date, room))
					p.addStyleName("selected");
				
				row.add(p);
				
				p.addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						if (iHoverDate != null && iHoverLoc != null) {
							P p = iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId());
							if (p != null) p.removeStyleName("hover");
						}
						((P)event.getSource()).addStyleName("hover");
						iHoverDate = date;
						iHoverLoc = room;
						GwtHint.showHint(p.getElement(), conflicts2hint(room, d, conflicts));
						event.preventDefault();
						event.stopPropagation();
						iText.setFocus(true);
					}
				});
				
				p.addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						((P)event.getSource()).removeStyleName("hover");
						GwtHint.hideHint();
					}
				});
			}
		}
		Set<String> selected = new HashSet<String>();
		for (Integer date: getDates())
			for (Entity room: getRooms()) {
				String selection = date + ":" + room.getUniqueId();
				if (iSelected.contains(selection)) selected.add(selection);
			}
		iSelected = selected;
		iAvailabilityHeader.setEnabled("select", !selected.isEmpty());
		if (iHoverDate != null && iHoverLoc != null && iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()) == null) {
			iHoverDate = null; iHoverLoc = null;
		}
	}
	
	private void populateVertical(EventRoomAvailabilityRpcResponse response, int index) {
		iResponse = response;
		iIndex = index;
		if (iIndex < 0) iIndex = 0;
		if (iIndex >= getDates().size()) iIndex = iStep * (getDates().size() / iStep);
		
		iAvailabilityHeader.setEnabled("prev", iIndex > 0);
		iAvailabilityHeader.setEnabled("next", iIndex + iStep < getDates().size());
		iRoomAvailability.clear();
		iRoomAvailability.add(iText);
		iPanels.clear();
		
		P box = new P("box"); iRoomAvailability.add(box);
		
		P row = new P("row"); box.add(row);
		
		row.add(new P("corner"));
		
		for (int i = iIndex; i < iIndex + iStep && i < getDates().size(); i++) {
			final Integer date = getDates().get(i);
			final P day = new P("room");
			final Date d = iDates.getDate(date);
			day.setHTML(MESSAGES.dateTimeHeader(sDayOfWeek.format(d), sDateFormat.format(d), TimeUtils.slot2short(getStartSlot()), TimeUtils.slot2short(getEndSlot())));
			row.add(day);
			day.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					if (iHoverDate != null && iHoverLoc != null) {
						P p = iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId());
						if (p != null) p.removeStyleName("hover");
					}
					((P)event.getSource()).addStyleName("hover");
				}
			});
			day.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					((P)event.getSource()).removeStyleName("hover");
				}
			});
			day.addMouseDownHandler(new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					boolean selected = true;
					for (Entity room: getRooms()) {
						if (!isSelected(date, room)) {
							selected = false;
							setSelected(date, room, true);
						}
					}
					if (selected) {
						for (Entity room: getRooms()) {
							setSelected(date, room, false);
						}
					}
					event.preventDefault();
					event.stopPropagation();
					iText.setFocus(true);
				}
			});			
		}
		
		for (int ri = 0; ri < getRooms().size(); ri++) {
			row = new P("row"); box.add(row);
			
			final Entity room = getRooms().get(ri);
			final P prm = new P("date");
			prm.setHTML(MESSAGES.singleRoomSelection(room.getName(), room.getProperty("type", null), room.getProperty("capacity", null)));
			prm.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					if (iHoverDate != null && iHoverLoc != null) {
						P p = iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId());
						if (p != null) p.removeStyleName("hover");
					}
					((P)event.getSource()).addStyleName("hover");
					RoomHint.showHint(prm.getElement(), room.getUniqueId(), "", room.getProperty("distance", ""));
				}
			});
			prm.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					((P)event.getSource()).removeStyleName("hover");
					RoomHint.hideHint();
				}
			});
			row.add(prm);
			prm.addMouseDownHandler(new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					boolean selected = true;
					for (int i = iIndex; i < iIndex + iStep && i < getDates().size(); i++) {
						Integer date = getDates().get(i);
						if (!isSelected(date, room)) {
							selected = false;
							setSelected(date, room, true);
						}
					}
					if (selected) {
						for (int i = iIndex; i < iIndex + iStep && i < getDates().size(); i++) {
							Integer date = getDates().get(i);
							setSelected(date, room, false);
						}
					}
					event.preventDefault();
					event.stopPropagation();
					iText.setFocus(true);
				}
			});
			
			for (int i = iIndex; i < iIndex + iStep && i < getDates().size(); i++) {
				final Integer date = getDates().get(i);
				final Set<MeetingConflictInterface> conflicts = response.getOverlaps(date, Long.valueOf(room.getProperty("permId", null)));
				final Date d = iDates.getDate(date);
				
				final P p = new P("cell");
				
				if (conflicts == null || conflicts.isEmpty()) {
					p.addStyleName("free");
				} else {
					p.addStyleName("conflict");
					p.setHTML(conflicts2html(conflicts));
				}
				Roles.getTextboxRole().setAriaLabelProperty(p.getElement(), conflicts2text(room, d, conflicts));
				
				iPanels.put(date + ":" + room.getUniqueId(), p);
				
				p.addMouseDownHandler(new MouseDownHandler() {
					@Override
					public void onMouseDown(MouseDownEvent event) {
						setSelected(date, room, !isSelected(date, room));
						event.preventDefault();
						event.stopPropagation();
						iText.setFocus(true);
					}
				});
				
				if (isSelected(date, room))
					p.addStyleName("selected");
				
				row.add(p);
				
				p.addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						if (iHoverDate != null && iHoverLoc != null) {
							P p = iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId());
							if (p != null) p.removeStyleName("hover");
						}
						((P)event.getSource()).addStyleName("hover");
						iHoverDate = date;
						iHoverLoc = room;
						GwtHint.showHint(p.getElement(), conflicts2hint(room, d, conflicts));
						event.preventDefault();
						event.stopPropagation();
						iText.setFocus(true);
					}
				});
				
				p.addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						((P)event.getSource()).removeStyleName("hover");
						GwtHint.hideHint();
					}
				});
			}
		}
		Set<String> selected = new HashSet<String>();
		for (Integer date: getDates())
			for (Entity room: getRooms()) {
				String selection = date + ":" + room.getUniqueId();
				if (iSelected.contains(selection)) selected.add(selection);
			}
		iSelected = selected;
		iAvailabilityHeader.setEnabled("select", !selected.isEmpty());
		if (iHoverDate != null && iHoverLoc != null && iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()) == null) {
			iHoverDate = null; iHoverLoc = null;
		}
	}
		
	public Set<MeetingConflictInterface> getConflicts(Integer date, Entity room) {
		return iResponse.getOverlaps(date, Long.valueOf(room.getProperty("permId", null)));
	}
	
	public void setSelected(Integer date, Entity room, boolean selected) {
		String dateStr = sAriaDateFormat.format(iDates.getDate(date));
		String roomStr = room.getName();
		if (!"1".equals(room.getProperty("overbook", "0"))) {
			Set<MeetingConflictInterface> conf = getConflicts(date, room);
			if (conf != null && !conf.isEmpty()) {
				AriaStatus.getInstance().setText(ARIA.dateRoomCanNotSelect(dateStr, roomStr));
				return;
			}
		}
		String selection = date + ":" + room.getUniqueId();
		P p = iPanels.get(selection);
		if (selected) {
			p.addStyleName("selected");
			iSelected.add(selection);
		} else {
			p.removeStyleName("selected");
			iSelected.remove(selection);
		}
		iAvailabilityHeader.setEnabled("select", !iSelected.isEmpty());
		AriaStatus.getInstance().setText(selected ? ARIA.dateRoomSelected(dateStr, roomStr) : ARIA.dateRoomUnselected(dateStr, roomStr));
		iText.setText("");
	}
	
	public void recenter() {
		GwtHint.getInstance().hide();
		
		iScrollRooms.getElement().getStyle().clearHeight();
		if (getElement().getClientHeight() > Window.getClientHeight() - 100)
			iScrollRooms.getElement().getStyle().setHeight(Window.getClientHeight() - 200, Unit.PX);
		
		iScrollDates.getElement().getStyle().clearHeight();
		if (getElement().getClientHeight() > Window.getClientHeight() - 100) {
			iScrollDates.getElement().getStyle().setHeight(Window.getClientHeight() - 200, Unit.PX);
		}

		int left = (Window.getClientWidth() - getOffsetWidth()) >> 1;
	    int top = (Window.getClientHeight() - getOffsetHeight()) >> 1;
		setPopupPosition(Math.max(Window.getScrollLeft() + left, 0), Math.max( Window.getScrollTop() + top, 0));
	}
	
	public String toAriaLabel(boolean hover, boolean description, boolean selection) {
		String label = "";
		if (hover && iHoverDate != null && iHoverLoc != null) {
			P p = iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId());
			if (iSelected.contains(iHoverDate + ":" + iHoverLoc.getUniqueId())) {
				label = ARIA.selectedSelection(Roles.getTextboxRole().getAriaLabelProperty(p.getElement()));
			} else {
				label = Roles.getTextboxRole().getAriaLabelProperty(p.getElement());
			}
		} else if (description) {
			if (EventCookie.getInstance().areRoomsHorizontal())
				label = ARIA.meetingSelectionDescriptionRoomsHorizontal(getDates().size(), 1 + iIndex, Math.min(iIndex + iStep, getRooms().size()));
			else
				label = ARIA.meetingSelectionDescriptionDatesHorizontal(getRooms().size(), 1 + iIndex, Math.min(iIndex + iStep, getDates().size()));
		}
		if (selection) {
			if (iSelected.isEmpty()) {
				label += " " + ARIA.meetingSelectionNothingSelected();
			} else {
				String selected = "";
				for (Integer date: getDates())
					for (Entity room: getRooms()) {
						if (iSelected.contains(date + ":" + room.getUniqueId())) {
							if (!selected.isEmpty()) selected += ", ";
							selected += ARIA.dateRoomSelection(sAriaDateFormat.format(iDates.getDate(date)), room.getName());
						}
					}
				label += " " + ARIA.meetingSelectionSelected(selected);
			}			
		}
		return label;
	}
}