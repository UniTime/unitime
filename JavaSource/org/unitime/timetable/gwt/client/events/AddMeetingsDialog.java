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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.UIObject;

public class AddMeetingsDialog extends UniTimeDialogBox {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private static DateTimeFormat sDayOfWeek = DateTimeFormat.getFormat("EEEE");
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	
	private SimpleForm iDatesForm, iAvailabilityForm;
	private UniTimeHeaderPanel iDatesHeader, iAvailabilityHeader;
	
	private P iRoomAvailability;
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
	
	
	public AddMeetingsDialog(AcademicSessionProvider session, AsyncCallback<List<MeetingInterface>> callback) {
		super(true, false);
		setAnimationEnabled(false);
		
		iCallback = callback;
		iSession = session;
		
		setText(MESSAGES.dialogAddMeetings());
		setEscapeToHide(true);
		
		iDatesForm = new SimpleForm();
		
		iDatesHeader = new UniTimeHeaderPanel();
		iDatesHeader.addButton("next", MESSAGES.buttonNext(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iDates.getSelectedDaysCount() == 0) {
					iDatesHeader.setErrorMessage(MESSAGES.errorNoDateSelected());
					return;
				}
				LoadingWidget.getInstance().show(MESSAGES.waitCheckingRoomAvailability());
				iRooms.getElements(new AsyncCallback<List<Entity>>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iDatesHeader.setErrorMessage(caught.getMessage());
					}
					@Override
					public void onSuccess(List<Entity> result) {
						iMatchingRooms = result;
						if (result == null || result.isEmpty()) {
							LoadingWidget.getInstance().hide();
							iDatesHeader.setErrorMessage(MESSAGES.errorNoMatchingRooms());
						} else if (iDates.getSelectedDaysCount() > 0) {
							iDatesHeader.clearMessage();
							RPC.execute(EventRoomAvailabilityRpcRequest.checkAvailability(
										getStartSlot(), getEndSlot(), getDates(), getRooms(), iEventId, iSession.getAcademicSessionId()
									), new AsyncCallback<EventRoomAvailabilityRpcResponse>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									iDatesHeader.setErrorMessage(caught.getMessage());
								}
								
								@Override
								public void onSuccess(EventRoomAvailabilityRpcResponse result) {
									LoadingWidget.getInstance().hide();
									populate(result, 0, EventCookie.getInstance().getRoomsSortBy());
									setWidget(iAvailabilityForm);
									recenter();
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
		
		iAvailabilityHeader.addButton("dates", MESSAGES.buttonPrevious(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setWidget(iDatesForm);
				recenter();
				iResponse = null;
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
		iAvailabilityHeader.addButton("sort", MESSAGES.buttonSortBy(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				for (final SortRoomsBy sortBy: SortRoomsBy.values()) {
					if (sortBy == SortRoomsBy.DISTANCE && !iRooms.hasChip(new Chip("flag", "Nearby"))) continue;
					MenuItem item = new MenuItem(MESSAGES.opSortBy(getSortRoomsByName(sortBy)), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							EventCookie.getInstance().setSortRoomsBy(sortBy.ordinal());
							populate(iResponse, 0, sortBy.ordinal());
							recenter();
						}
					});
					item.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(item);
				}
				menu.setVisible(true);
				menu.setFocusOnHoverEnabled(true);
				popup.add(menu);
				popup.showRelativeTo((UIObject)event.getSource());
				((MenuBar)popup.getWidget()).focus();
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
	
	public void reset(String roomFilterValue) {
		reset(roomFilterValue, null);
	}

	public void reset(String roomFilterValue, List<MeetingInterface> meetings) {
		iMatchingRooms = null;
		iDates.setValue(new ArrayList<Date>());
		iTimes.setValue(new StartEndTimeSelector.StartEndTime(7*12 + 6, 17*12 + 6), true);
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
		iResponse = response;
		iIndex = index;
		if (iIndex < 0) iIndex = 0;
		if (iIndex >= getRooms().size()) iIndex = iStep * (getRooms().size() / iStep);
		
		if (sortBy != null && sortBy >= 0 && sortBy < SortRoomsBy.values().length) {
			Comparator<Entity> comparator = getSortRoomsComparator(SortRoomsBy.values()[sortBy], iRooms.getChip("size") != null, getDates(), iResponse);
			if (comparator != null)
				Collections.sort(iMatchingRooms, comparator);
		}
		
		iAvailabilityHeader.setEnabled("prev", iIndex > 0);
		iAvailabilityHeader.setEnabled("next", iIndex + iStep < getRooms().size());
		iRoomAvailability.clear();
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
					String conf = "";
					int count = 0;
					for (MeetingConflictInterface event: conflicts) {
						if (count == 3) { conf += "<br>..."; break; }
						conf += (conf.isEmpty() ? "" : "<br>") + event.getName() + (event.getType() == EventType.Unavailabile ? "" : " (" + event.getType().getAbbreviation(CONSTANTS) + ")");
						count ++;
					}
					p.setHTML(conf);
				}
				
				iPanels.put(date + ":" + room.getUniqueId(), p);
				
				p.addMouseDownHandler(new MouseDownHandler() {
					@Override
					public void onMouseDown(MouseDownEvent event) {
						setSelected(date, room, !isSelected(date, room));
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
						String capacity = room.getProperty("capacity", null);
						String distance = room.getProperty("distance", null);
						String hint = room.getProperty("type", null) +
								(capacity != null ? ", " + MESSAGES.hintRoomCapacity(capacity) : "") +
								(distance != null && !"0".equals(distance) ? ", " + MESSAGES.hintRoomDistance(distance) : "");
						String message = MESSAGES.dateTimeHint(sDayOfWeek.format(d), sDateFormat.format(d), TimeUtils.slot2short(getStartSlot()), TimeUtils.slot2short(getEndSlot())) + 
								"<br>" + room.getName() + " (" + hint + ")";
						if (conflicts != null && !conflicts.isEmpty()) {
							message += "<br>" + MESSAGES.propConflicts();
							for (MeetingConflictInterface conflictingEvent: conflicts)
								message += (conflicts.size() == 1 ? "" : "<br>&nbsp;&nbsp;&nbsp;") + conflictingEvent.getName() +
								(conflictingEvent.getType() == EventType.Unavailabile ? "" :" (" + conflictingEvent.getType().getAbbreviation(CONSTANTS) + (conflictingEvent.hasLimit() ? ", " + MESSAGES.eventGridLimit(conflictingEvent.getLimit()) : "") + ")");
						}
						GwtHint.showHint(p.getElement(), message);
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
		if (!"1".equals(room.getProperty("overbook", "0"))) {
			Set<MeetingConflictInterface> conf = getConflicts(date, room);
			if (conf != null && !conf.isEmpty()) return;
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
	
    @Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
    	super.onPreviewNativeEvent(event);
    	if (iResponse == null) return;
    	int min = iIndex, max = Math.min(iIndex + iStep, getRooms().size()) - 1;
    	if (event.getTypeInt() == Event.ONKEYDOWN) {
        	switch (DOM.eventGetKeyCode((Event)event.getNativeEvent())) {
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
        		if (iHoverDate != null && iHoverLoc != null) {
        			setSelected(iHoverDate, iHoverLoc, !isSelected(iHoverDate, iHoverLoc));
        		}
        	}
    	}
    }
 	
}