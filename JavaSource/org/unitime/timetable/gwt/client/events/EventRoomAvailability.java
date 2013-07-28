/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.Components;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.ToolBox.Page;
import org.unitime.timetable.gwt.client.events.AcademicSessionSelectionBox.AcademicSession;
import org.unitime.timetable.gwt.client.events.AcademicSessionSelectionBox.AcademicSessionFilter;
import org.unitime.timetable.gwt.client.events.AddMeetingsDialog.SortRoomsBy;
import org.unitime.timetable.gwt.client.events.EventAdd.EventPropertiesProvider;
import org.unitime.timetable.gwt.client.events.EventResourceTimetable.HistoryToken;
import org.unitime.timetable.gwt.client.events.EventResourceTimetable.PageType;
import org.unitime.timetable.gwt.client.events.StartEndTimeSelector.StartEndTime;
import org.unitime.timetable.gwt.client.events.TimeGrid.MeetingClickEvent;
import org.unitime.timetable.gwt.client.events.TimeGrid.MeetingClickHandler;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.TimeSelector.TimeUtils;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeHandler;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.DateInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventDetailRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RequestSessionDetails;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.SelectionInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;
import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth.Flag;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EventRoomAvailability extends Composite implements AcademicSessionFilter, EventPropertiesProvider {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormatShort());
	
	private HistoryToken iHistoryToken = null;
	
	private SimplePanel iRootPanel;
	
	private SimpleForm iPanel, iFilter;
	private UniTimeHeaderPanel iHeader, iFooter, iFilterHeader;
	private AcademicSessionSelectionBox iSession;
	private int iSessionRow;
	private StartEndTimeSelector iTimes;
	private SessionDatesSelector iDates;
	private RoomFilterBox iRooms;
	private VerticalPanel iTables;
	
	private EventPropertiesRpcResponse iProperties = null;
	private boolean iInitialized = false;
	
	private StartEndTime iSelectedTimes = null;
	private List<Integer> iSelectedDates = null;
	private List<ResourceInterface> iSelectedRooms = null;
	private List<EventInterface> iData;
	private List<SessionMonth> iSessionMonths = null;
	
	private EventDetail iEventDetail;
	private EventAdd iEventAdd;
	private MeetingClickHandler iMeetingClickHandler;
	
	public EventRoomAvailability() {
		iHistoryToken = new HistoryToken(PageType.Availability);
		
		iFilter = new SimpleForm(2);
		iFilter.removeStyleName("unitime-NotPrintableBottomLine");
		iFilter.getColumnFormatter().setWidth(0, "120px");
		
		iFilterHeader = new UniTimeHeaderPanel(MESSAGES.sectFilter());
		iFilterHeader.addButton("add", MESSAGES.buttonAddEvent(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iEventAdd.setEvent(null);
				iEventAdd.show();
			}
		});
		iFilterHeader.setEnabled("add", false);
		iFilter.addHeaderRow(iFilterHeader);
		iFilterHeader.addButton("clear", MESSAGES.buttonClear(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iDates.setValue(null);
				iTimes.setValue(null);
				iRooms.setValue("flag:Event");
				hideResults();
				changeUrl();
			}
		});
		iFilterHeader.addButton("search", MESSAGES.buttonSearch(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				reload(true);
			}
		});
		
		iSession = new AcademicSessionSelectionBox(iHistoryToken.getParameter("term")) {
			@Override
			protected void onInitializationSuccess(List<AcademicSession> sessions) {
				iFilter.setVisible(sessions != null && !sessions.isEmpty());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						try {
							((UniTimePageHeader)RootPanel.get(Components.header.id()).getWidget(0)).hideSessionInfo();
						} catch (Exception e) {
							// Try one more time in 5 seconds
							new Timer() {
								@Override
								public void run() {
									try {
										((UniTimePageHeader)RootPanel.get(Components.header.id()).getWidget(0)).hideSessionInfo();
									} catch (Exception e) {}
								}
							}.schedule(5000);
						}
					}
				});
			}
			
			@Override
			protected void onInitializationFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedLoadSessions(caught.getMessage()), caught);
			}
		};
		iSession.setFilter(this);
		iSessionRow = iFilter.addRow(MESSAGES.propAcademicSession(), iSession);
		
		iDates = new SessionDatesSelector(iSession);
		iDates.setCanSelectPast(true);
		ToolBox.setMaxWidth(iDates.getElement().getStyle(), Math.round(0.9 * ToolBox.getClientWidth() - 120)+ "px");
		iFilter.addRow(MESSAGES.propDates(), iDates);
		
		iTimes = new StartEndTimeSelector(); iTimes.setDiff(12);
		iFilter.addRow(MESSAGES.propTimes(), iTimes);
		
		iRooms = new RoomFilterBox(iSession);
		iFilter.addRow(MESSAGES.propLocations(), iRooms);

		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		iPanel.addRow(iFilter);
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("add", MESSAGES.buttonAddEvent(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iEventAdd.setEvent(null);
				iEventAdd.show();
			}
		});
		iHeader.setEnabled("add", false);
		iHeader.addButton("print", MESSAGES.buttonPrint(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				HashMap<Long, String> colors = new HashMap<Long, String>();
				List<Page> pages = new ArrayList<Page>();
				if (iSelectedDates.size() == 1) {
					final Date date = iDates.getDate(iSelectedDates.get(0));
					int year = Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
					int month = Integer.parseInt(DateTimeFormat.getFormat("MM").format(date));
					int dow = (SingleDateSelector.firstDayOfWeek(year, month) + Integer.parseInt(DateTimeFormat.getFormat("dd").format(date)) - 1) % 7;
					int days[] = new int[] {dow};
					WeekInterface week = new WeekInterface();
					week.setDayOfYear(iSelectedDates.get(0) - dow);
					for (int i = 0; i < 7; i++) {
						week.addDayName(new DateInterface(
								sDateFormat.format(iDates.getDate(week.getDayOfYear() + i)),
								month,
								week.getDayOfYear() + i));
					}
					List<WeekInterface> weeks = new ArrayList<WeekInterface>(); weeks.add(week);
					boolean past = !iDates.isEnabled(date) || iDates.hasFlag(date, Flag.PAST);
					for (int i = 0; i < iSelectedDates.size(); i++) {
						int startHour = 7;
						if (iSelectedTimes.getStart() != null) {
							startHour = Math.max(0, (iSelectedTimes.getStart() - 6) / 12);
						}
						int endHour = 18;
						if (iSelectedTimes.getEnd() != null) {
							endHour = Math.min(24, (17 + iSelectedTimes.getEnd()) / 12);
						}

						final TimeGrid grid = new TimeGrid(colors, days, (int)(1000 / days.length), 55, true, false, startHour, endHour);
						grid.setResourceType(ResourceType.ROOM);
						grid.setSelectedWeeks(weeks);
						List<ResourceInterface> rooms = new ArrayList<EventInterface.ResourceInterface>(iSelectedRooms);
						grid.setRoomResources(rooms);
						grid.setMode(TimeGrid.Mode.OVERLAP);
						for (EventInterface event: sortedEvents()) {
							List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
							for (MeetingInterface meeting: event.getMeetings()) {
								if (meeting.getApprovalStatus() != ApprovalStatus.Pending && meeting.getApprovalStatus() != ApprovalStatus.Approved) continue;
								if (iSelectedDates.get(i) == meeting.getDayOfYear() && meeting.hasLocation())
									meetings.add(meeting);
							}
							if (!meetings.isEmpty())
								grid.addEvent(event, meetings);
						}
						grid.labelDays(week, null);
						grid.setCalendarUrl(null);
						grid.yellow(iSelectedTimes.getStart() == null ? 90 : iSelectedTimes.getStart(), iSelectedTimes.getEnd() == null ? 210 : iSelectedTimes.getEnd());
						if (past) grid.gray(0, 1);
						grid.showVerticalSplit();
						grid.addMeetingClickHandler(iMeetingClickHandler);
						pages.add(new Page() {
							@Override
							public String getName() { return DateTimeFormat.getFormat(CONSTANTS.meetingDateFormat()).format(date); }
							@Override
							public String getUser() { return ""; }
							@Override
							public String getSession() { return ""; }
							@Override
							public Element getBody() { return grid.getElement(); }
						});
					}
				} else {
					int[] days = new int[iSelectedDates.size()];
					WeekInterface week = new WeekInterface();
					week.setDayOfYear(iSelectedDates.get(0));
					List<String> dows = new ArrayList<String>();
					int lastPast = -1;
					for (int i = 0; i < iSelectedDates.size(); i++) {
						Date date = iDates.getDate(iSelectedDates.get(i));
						int year = Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
						int month = Integer.parseInt(DateTimeFormat.getFormat("MM").format(date));
						int dow = (SingleDateSelector.firstDayOfWeek(year, month) + Integer.parseInt(DateTimeFormat.getFormat("dd").format(date)) - 1) % 7;
						days[i] = i;
						week.addDayName(new DateInterface(sDateFormat.format(date), month, iSelectedDates.get(i)));
						dows.add(CONSTANTS.days()[dow]);
						if (!iDates.isEnabled(date) || iDates.hasFlag(date, Flag.PAST))
							lastPast = i;
					}
					List<WeekInterface> weeks = new ArrayList<WeekInterface>(); weeks.add(week);
					for (final ResourceInterface room: iSelectedRooms) {
						int startHour = 7;
						if (iSelectedTimes.getStart() != null) {
							startHour = Math.max(0, (iSelectedTimes.getStart() - 6) / 12);
						}
						int endHour = 18;
						if (iSelectedTimes.getEnd() != null) {
							endHour = Math.min(24, (17 + iSelectedTimes.getEnd()) / 12);
						}

						final TimeGrid grid = new TimeGrid(colors, days, (int)(1000 / days.length), 55, true, false, startHour, endHour);
						grid.setResourceType(ResourceType.ROOM);
						grid.setSelectedWeeks(weeks);
						List<ResourceInterface> rooms = new ArrayList<EventInterface.ResourceInterface>(); rooms.add(room);
						grid.setRoomResources(rooms);
						grid.setMode(TimeGrid.Mode.OVERLAP);
						for (EventInterface event: sortedEvents()) {
							List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
							for (MeetingInterface meeting: event.getMeetings()) {
								if (meeting.getApprovalStatus() != ApprovalStatus.Pending && meeting.getApprovalStatus() != ApprovalStatus.Approved) continue;
								if (meeting.getMeetingDate() != null && meeting.hasLocation() && meeting.getLocation().getId().equals(room.getId())) {
									int idx = iSelectedDates.indexOf(meeting.getDayOfYear());
									if (idx >= 0) {
										meeting.setGridIndex(idx);
										meetings.add(meeting);
									}
								}
							}
							if (!meetings.isEmpty())
								grid.addEvent(event, meetings);
						}
						grid.labelDays(dows, week);
						grid.setCalendarUrl(null);
						grid.yellow(iSelectedTimes.getStart() == null ? 90 : iSelectedTimes.getStart(), iSelectedTimes.getEnd() == null ? 210 : iSelectedTimes.getEnd());
						if (lastPast >= 0) grid.gray(0, lastPast);
						grid.addMeetingClickHandler(iMeetingClickHandler);
						pages.add(new Page() {
							@Override
							public String getName() {
								return room.getName();
							}

							@Override
							public String getUser() {
								return room.getRoomType();
							}

							@Override
							public String getSession() {
								return (room.hasSize() ? MESSAGES.hintRoomCapacity(room.getSize().toString()) : "");
							}

							@Override
							public Element getBody() {
								return grid.getElement();
							}
							
						});
					}
				}
				ToolBox.print(pages);
			}
		});
		iHeader.addButton("sort", MESSAGES.buttonSortBy(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				MenuBar menu = new MenuBar(true);
				for (final SortRoomsBy sortBy: SortRoomsBy.values()) {
					if (sortBy == SortRoomsBy.DISTANCE && !iRooms.hasChip(new Chip("flag", "Nearby"))) continue;
					MenuItem item = new MenuItem(
							(sortBy.ordinal() == EventCookie.getInstance().getRoomsSortBy() ? "&uarr; " :
							(sortBy.ordinal() + SortRoomsBy.values().length == EventCookie.getInstance().getRoomsSortBy()) ? "&darr; " : "") +
							AddMeetingsDialog.getSortRoomsByName(sortBy), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							if (sortBy.ordinal() == EventCookie.getInstance().getRoomsSortBy()) {
								EventCookie.getInstance().setSortRoomsBy(SortRoomsBy.values().length + sortBy.ordinal());
							} else {
								EventCookie.getInstance().setSortRoomsBy(sortBy.ordinal());
							}
							populate(iData, EventCookie.getInstance().getRoomsSortBy());
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
		iPanel.addHeaderRow(iHeader);

		iTables = new VerticalPanel();
		iPanel.addRow(iTables);
		
		iFooter = iHeader.clonePanel();
		iPanel.addRow(iFooter);
		iRootPanel = new SimplePanel(iPanel);
		initWidget(iRootPanel);

		hideResults();
		
		iSession.addAcademicSessionChangeHandler(new AcademicSessionChangeHandler() {
			@Override
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				loadProperties(new AsyncCallback<EventPropertiesRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {
					}
					@Override
					public void onSuccess(EventPropertiesRpcResponse result) {
						if (!iInitialized)
							setup(true);
						else
							reload(false);
					}
				});
			}
		});
		
		iEventDetail = new EventDetail(this) {
			@Override
			protected void onHide() {
				iRootPanel.setWidget(iPanel);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageEventRoomAvailability());
				if (!isShowingResults())
					reload(true);
				changeUrl();
			}
			@Override
			protected void onShow() {
				iRootPanel.setWidget(iEventDetail);
				changeUrl();
			}
			@Override
			protected void edit() {
				hide();
				iEventAdd.setEvent(getEvent());
				iEventAdd.show();
			}
			@Override
			protected EventInterface getPrevious(Long eventId) {
				return null;
			}
			@Override
			protected EventInterface getNext(Long eventId) {
				return null;
			}
			@Override
			protected void previous(final EventInterface event) {
				LoadingWidget.execute(EventDetailRpcRequest.requestEventDetails(iSession.getAcademicSessionId(), event.getId()), new AsyncCallback<EventInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						UniTimeNotifications.error(MESSAGES.failedLoad(event.getName(), caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(EventInterface result) {
						LoadingWidget.getInstance().hide();
						setEvent(result);
						changeUrl();
					}
				}, MESSAGES.waitLoading(event.getName()));
			}
			@Override
			protected void next(final EventInterface event) {
				previous(event);
			}
			@Override
			public void hide() {
				super.hide();
			}
			@Override
			protected void onApprovalOrReject(Long eventId, EventInterface event) {
				if (iData != null)
					populate(tinker(new GwtRpcResponseList<EventInterface>(iData), eventId, event), null);
			}
		};
		
		iEventAdd = new EventAdd(iSession, this) {
			@Override
			protected void onHide() {
				iSession.setFilter(EventRoomAvailability.this);
				iFilter.setWidget(iSessionRow, 1, iSession);
				final EventInterface modified = iEventAdd.getEvent(), detail = iEventDetail.getEvent(), saved = iEventAdd.getSavedEvent();
				if (saved != null) {
					if (iData != null)
						populate(tinker(new GwtRpcResponseList<EventInterface>(iData), (saved.getId() == null ? modified.getId() : saved.getId()), saved), null);
					if (saved.getId() != null) {
						iEventDetail.setEvent(saved);
						iEventDetail.show();
					} else {
						iRootPanel.setWidget(iPanel);
						UniTimePageLabel.getInstance().setPageName(MESSAGES.pageEventRoomAvailability());
					}
				} else if (modified != null && detail != null && detail.getId().equals(modified.getId())) {
					LoadingWidget.execute(EventDetailRpcRequest.requestEventDetails(iSession.getAcademicSessionId(), modified.getId()), new AsyncCallback<EventInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							UniTimeNotifications.error(MESSAGES.failedLoad(detail.getName(), caught.getMessage()), caught);
						}
						@Override
						public void onSuccess(EventInterface result) {
							LoadingWidget.getInstance().hide();
							iEventDetail.setEvent(result);
							iEventDetail.show();
						}
					}, MESSAGES.waitLoading(detail.getName()));
				} else {
					iRootPanel.setWidget(iPanel);
					UniTimePageLabel.getInstance().setPageName(MESSAGES.pageEventRoomAvailability());
				}
				changeUrl();
			}
			@Override
			protected void onShow() {
				iRootPanel.setWidget(iEventAdd);
				changeUrl();
			}
		};
		
		iMeetingClickHandler = new MeetingClickHandler() {
			@Override
			public void onMeetingClick(final MeetingClickEvent event) {
				if (!event.getEvent().isCanView()) return;
				LoadingWidget.execute(EventDetailRpcRequest.requestEventDetails(iSession.getAcademicSessionId(), event.getEvent().getId()), new AsyncCallback<EventInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						UniTimeNotifications.error(MESSAGES.failedLoad(event.getEvent().getName(), caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(EventInterface result) {
						LoadingWidget.getInstance().hide();
						iEventDetail.setEvent(result);
						iEventDetail.show();
					}
				}, MESSAGES.waitLoading(event.getEvent().getName()));
			}
		};
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (iInitialized) {
					iHistoryToken.reset(event.getValue());
					setup(false);
					if (LoadingWidget.getInstance().isShowing())
						LoadingWidget.getInstance().hide();
				}
			}
		});
	}
	
	private Integer getOccupancy(ResourceInterface room) {
		int startSlot = (iSelectedTimes.getStart() == null ? 90 : iSelectedTimes.getStart());
		int endSlot = (iSelectedTimes.getEnd() == null ? 210 : iSelectedTimes.getEnd());
		int use = 0;
		for (EventInterface event: iData) {
			for (MeetingInterface meeting: event.getMeetings()) {
				if (room.equals(meeting.getLocation())) {
					int start = Math.max(startSlot, meeting.getStartSlot());
					int end = Math.min(endSlot, meeting.getEndSlot());
					use += Math.max(0, end - start);
				}
			}
		}
		return use;
	}
	
	private Comparator<ResourceInterface> getSortRoomsComparator(SortRoomsBy sortBy, final boolean preferSize) {
		switch (sortBy) {
		case CAPACITY:
			return new Comparator<ResourceInterface>() {
				@Override
				public int compare(ResourceInterface r1, ResourceInterface r2) {
					int cmp = (r1.getSize() == null ? new Integer(0) : r1.getSize()).compareTo(r2.getSize() == null ? new Integer(0) : r2.getSize());
					if (cmp != 0) return cmp;
					cmp = r1.getName().compareTo(r2.getName());
					return (cmp == 0 ? r1.getId().compareTo(r2.getId()) : cmp);
				}
				
			};
		case NAME:
			return new Comparator<ResourceInterface>() {
				@Override
				public int compare(ResourceInterface r1, ResourceInterface r2) {
					int cmp = r1.getName().compareTo(r2.getName());
					return (cmp == 0 ? r1.getId().compareTo(r2.getId()) : cmp);
				}
				
			};
		case DISTANCE:
			return new Comparator<ResourceInterface>() {
				@Override
				public int compare(ResourceInterface r1, ResourceInterface r2) {
					int cmp = (r1.getDistance() == null ? new Double(0.0) : r1.getDistance()).compareTo(r2.getDistance() == null ? new Double(0.0) : r2.getDistance());
					if (cmp != 0) return cmp;
					if (preferSize) {
						cmp = (r1.getSize() == null ? new Integer(0) : r1.getSize()).compareTo(r2.getSize() == null ? new Integer(0) : r2.getSize());
						if (cmp != 0) return cmp;
					}
					cmp = r1.getName().compareTo(r2.getName());
					return (cmp == 0 ? r1.getId().compareTo(r2.getId()) : cmp);
				}
			};
		case AVAILABILITY:
			return new Comparator<ResourceInterface>() {
				@Override
				public int compare(ResourceInterface r1, ResourceInterface r2) {
					int cmp = getOccupancy(r1).compareTo(getOccupancy(r2));
					if (cmp != 0) return cmp;
					cmp = (r1.getDistance() == null ? new Double(0.0) : r1.getDistance()).compareTo(r2.getDistance() == null ? new Double(0.0) : r2.getDistance());
					if (cmp != 0) return cmp;
					if (preferSize) {
						cmp = (r1.getSize() == null ? new Integer(0) : r1.getSize()).compareTo(r2.getSize() == null ? new Integer(0) : r2.getSize());
						if (cmp != 0) return cmp;
					}
					cmp = r1.getName().compareTo(r2.getName());
					return (cmp == 0 ? r1.getId().compareTo(r2.getId()) : cmp);
				}
			};
		default:
			return null;
		}
	}
	
	private Comparator<ResourceInterface> inverse(final Comparator<ResourceInterface> cmp) {
		return new Comparator<ResourceInterface>() {
			@Override
			public int compare(ResourceInterface r1, ResourceInterface r2) {
				return - cmp.compare(r1, r2);
			}
		};
	}

	@Override
	public boolean accept(AcademicSession session) {
		return session.has(AcademicSession.Flag.HasEvents);
	}
	
	private void hideResults() {
		for (int i = 1; i < iPanel.getRowCount(); i++)
			iPanel.getRowFormatter().setVisible(i, false);
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export", false);
		iHeader.setEnabled("operations", false);
	}
	
	private void showResults() {
		for (int i = 1; i < iPanel.getRowCount(); i++)
			iPanel.getRowFormatter().setVisible(i, true);
		iHeader.setEnabled("print", true);
		// iHeader.setEnabled("export", iTable.getRowCount() > 1);
		// iHeader.setEnabled("operations", getSelectedTab() > 0 && iTable.getRowCount() > 1);
	}
	
	private boolean isShowingResults() {
		return iPanel.equals(iRootPanel.getWidget()) && iPanel.getRowFormatter().isVisible(1);
	}
	
	private void setup(boolean init) {
		if (init)
			iInitialized = true;
		boolean reload = init;
		boolean isDefault = true;
		if (iHistoryToken.isChanged("rooms", "flag:Event", iRooms.getValue())) {
			iRooms.setValue(iHistoryToken.getParameter("rooms", "flag:Event"), true);
			reload = true;
			if (!iRooms.getValue().equals("flag:Event")) {
				isDefault = false;
			}
		}
		if (iHistoryToken.isChanged("after", iTimes.getValue().getStart() == null ? "" : iTimes.getValue().getStart().toString()) || iHistoryToken.isChanged("before", iTimes.getValue().getEnd() == null ? "" : iTimes.getValue().getEnd().toString())) {
			String after = iHistoryToken.getParameter("after");
			String before = iHistoryToken.getParameter("before");
			iTimes.setValue(new StartEndTime(after == null || after.isEmpty() ? null : Integer.valueOf(after), before == null || before.isEmpty() ? null : Integer.valueOf(before)));
			reload = true;
			if (iTimes.getValue().getStart() != null)
				isDefault = false;
		}
		String dates = "";
		for (Integer date: iDates.getSelectedDays()) {
			dates += (dates.isEmpty() ? "" : ",") + date;
		}
		if (iHistoryToken.isChanged("dates", dates)) {
			dates = iHistoryToken.getParameter("dates");
			List<Date> val = new ArrayList<Date>();
			if (dates != null && !dates.isEmpty()) {
				for (String d: dates.split(","))
					val.add(iDates.getDate(Integer.valueOf(d)));
			}
			iDates.setValue(val);
			reload = true;
			if (iDates.getSelectedDaysCount() > 0)
				isDefault = false;
		}
		if (iHistoryToken.isChanged("term", iSession.getAcademicSessionAbbreviation()) && iHistoryToken.getParameter("term") != null)
			iSession.selectSession(iHistoryToken.getParameter("term"), null);
		if (iHistoryToken.hasParameter("event")) {
			if ("add".equals(iHistoryToken.getParameter("event"))) {
				iEventAdd.setEvent(null);
				iEventAdd.show();
			} else {
				Long eventId = Long.valueOf(iHistoryToken.getParameter("event"));
				LoadingWidget.execute(EventDetailRpcRequest.requestEventDetails(iSession.getAcademicSessionId(), eventId), new AsyncCallback<EventInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						UniTimeNotifications.error(MESSAGES.failedLoad(MESSAGES.anEvent(), caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(EventInterface result) {
						LoadingWidget.getInstance().hide();
						iEventDetail.setEvent(result);
						iEventDetail.show();
					}
				}, MESSAGES.waitLoading(MESSAGES.anEvent()));
			}
		} else {
			iRootPanel.setWidget(iPanel);
			if (iHistoryToken.hasParameter("term") && iHistoryToken.isChanged("term", iSession.getAcademicSessionAbbreviation())) {
				iSession.selectSession(iHistoryToken.getParameter("term"), null);
			} else if (reload && iProperties != null) {
				reload(isShowingResults() || !isDefault);
			}
		}
	}
	
	private EventFilterRpcRequest getEventsRequest() {
		EventFilterRpcRequest request = new EventFilterRpcRequest();
		request.setSessionId(iSession.getAcademicSessionId());
		request.setCommand(FilterRpcRequest.Command.ENUMERATE);
		iSelectedTimes = iTimes.getValue();
		if (iSelectedTimes.getStart() != null) {
			int startHour = Math.max(0, (iSelectedTimes.getStart() - 6) / 12);
			request.setOption("after", TimeUtils.slot2time(12 * startHour));
		}
		if (iSelectedTimes.getEnd() != null) {
			int endHour = Math.min(24, (17 + iSelectedTimes.getEnd()) / 12);
			request.setOption("before", TimeUtils.slot2time(12 * endHour));
		}
		iSelectedDates = iDates.getSelectedDays(); 
		for (Integer date: iSelectedDates) {
			request.addOption("dates", date.toString());
		}
		return request;		
	}
	
	private void reload(boolean loadData) {
		if (loadData) {
			if (iSession.getAcademicSessionId() == null) {
				UniTimeNotifications.warn(MESSAGES.warnNoSession());
			} else if (iProperties == null) {
				UniTimeNotifications.warn(MESSAGES.warnNoEventProperties(iSession.getAcademicSessionName()));
			} else if (iDates.getSelectedDaysCount() <= 0) {
				UniTimeNotifications.warn(MESSAGES.errorNoDateSelected());
			} else {
				ResourceInterface resource = new ResourceInterface();
				resource.setType(ResourceType.ROOM);
				resource.setName(iRooms.getValue());
				LoadingWidget.execute(iRooms.getElementsRequest(), new AsyncCallback<FilterRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						UniTimeNotifications.error(MESSAGES.failedLoad(MESSAGES.resourceRoom().toLowerCase(), caught.getMessage()), caught);
						hideResults();
					}

					@Override
					public void onSuccess(FilterRpcResponse result) {
						if (result == null) return;
						iSelectedRooms = new ArrayList<ResourceInterface>();
						if (result.hasResults())
							for (FilterRpcResponse.Entity room: result.getResults()) {
								iSelectedRooms.add(new ResourceInterface(room));
							}
						if (iRooms.getChip("size") != null)
							Collections.sort(iSelectedRooms, new Comparator<ResourceInterface>() {
								@Override
								public int compare(ResourceInterface r1, ResourceInterface r2) {
									int cmp = (r1.getSize() == null ? new Integer(-1) : r1.getSize()).compareTo(r2.getSize() == null ? new Integer(-1) : r2.getSize());
									if (cmp != 0) return cmp;
									return r1.compareTo(r2);
								}
							});
						else
							Collections.sort(iSelectedRooms);
						
						if (!result.hasResults() || result.getResults().isEmpty()) {
							UniTimeNotifications.error(MESSAGES.errorNoMatchingRooms());
							hideResults();
							return;
						}
						
						LoadingWidget.execute(EventLookupRpcRequest.findEvents(iSession.getAcademicSessionId(), null, getEventsRequest(), iRooms.getElementsRequest(), CONSTANTS.maxMeetings()), 
								new AsyncCallback<GwtRpcResponseList<EventInterface>>() {
							@Override
							public void onSuccess(GwtRpcResponseList<EventInterface> result) {
								populate(result, EventCookie.getInstance().getRoomsSortBy());
							}
					
							@Override
							public void onFailure(Throwable caught) {
								UniTimeNotifications.error(MESSAGES.failedLoad(MESSAGES.resourceRoom().toLowerCase(), caught.getMessage()), caught);
								hideResults();
							}
						}, MESSAGES.waitLoadingTimetable(MESSAGES.resourceRoom().toLowerCase(), iSession.getAcademicSessionName()));
					}
					
				}, MESSAGES.waitLoadingTimetable(MESSAGES.resourceRoom().toLowerCase(), iSession.getAcademicSessionName()));
			}
		} else {
			iData = null;
			hideResults();
		}
	}
	
	private void loadProperties(final AsyncCallback<EventPropertiesRpcResponse> callback) {
		iProperties = null;
		iSessionMonths = null;
		iFilterHeader.setEnabled("add", false);
		iFooter.setEnabled("add", false);
		if (iSession.getAcademicSessionId() != null) {
			RPC.execute(EventPropertiesRpcRequest.requestEventProperties(iSession.getAcademicSessionId()), new AsyncCallback<EventPropertiesRpcResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					UniTimeNotifications.error(MESSAGES.failedLoad(iSession.getAcademicSessionName(), caught.getMessage()), caught);
					if (callback != null)
						callback.onFailure(caught);
				}
				@Override
				public void onSuccess(final EventPropertiesRpcResponse result) {
					RPC.execute(new RequestSessionDetails(iSession.getAcademicSessionId()), new AsyncCallback<GwtRpcResponseList<SessionMonth>>() {
						@Override
						public void onFailure(Throwable caught) {
							UniTimeNotifications.error(MESSAGES.failedLoad(iSession.getAcademicSessionName(), caught.getMessage()), caught);
							if (callback != null)
								callback.onFailure(caught);
						}
						
						@Override
						public void onSuccess(GwtRpcResponseList<SessionMonth> months) {
							iProperties = result;
							iSessionMonths = months;
							iFilterHeader.setEnabled("add", result.isCanAddEvent() && "true".equals(iHistoryToken.getParameter("addEvent", "true")));
							iFooter.setEnabled("add", result.isCanAddEvent() && "true".equals(iHistoryToken.getParameter("addEvent", "true")));
							iEventAdd.setup(result);
							if (callback != null)
								callback.onSuccess(result);
						}
					});
				}
			});
		}
	}
	
	private Collection<EventInterface> sortedEvents() {
		TreeSet<EventInterface> data = new TreeSet<EventInterface>(new Comparator<EventInterface>() {
			@Override
			public int compare(EventInterface e1, EventInterface e2) {
				int cmp = new Integer(e1.getMeetings().first().getDayOfYear()).compareTo(e2.getMeetings().first().getDayOfYear());
				if (cmp != 0) return cmp;
				cmp = new Integer(e1.getMeetings().first().getStartSlot()).compareTo(e2.getMeetings().first().getStartSlot());
				if (cmp != 0) return cmp;
				return e1.compareTo(e2);
			}
		});
		data.addAll(iData);
		return data;
	}
	
	private void populate(List<EventInterface> result, Integer sortBy) {
		for (int i = 0; i < iTables.getWidgetCount(); i++) {
			Widget w = iTables.getWidget(i);
			if (w instanceof TimeGrid)
				((TimeGrid)w).destroy();
		}
		iData = result;
		if (sortBy != null && sortBy >= 0 && sortBy < SortRoomsBy.values().length) {
			Comparator<ResourceInterface> comparator = getSortRoomsComparator(SortRoomsBy.values()[sortBy], iRooms.getChip("size") != null);
			if (comparator != null)
				Collections.sort(iSelectedRooms, comparator);
		} else {
			if (sortBy != null && sortBy >= SortRoomsBy.values().length && sortBy < 2 * SortRoomsBy.values().length) {
				Comparator<ResourceInterface> comparator = getSortRoomsComparator(SortRoomsBy.values()[sortBy - SortRoomsBy.values().length], iRooms.getChip("size") != null);
				if (comparator != null)
					Collections.sort(iSelectedRooms, inverse(comparator));
			}
		}
		iTables.clear();
		HashMap<Long, String> colors = new HashMap<Long, String>();
		if (iSelectedDates.size() == 1) {
			Date date = iDates.getDate(iSelectedDates.get(0));
			int year = Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
			int month = Integer.parseInt(DateTimeFormat.getFormat("MM").format(date));
			int dow = (SingleDateSelector.firstDayOfWeek(year, month) + Integer.parseInt(DateTimeFormat.getFormat("dd").format(date)) - 1) % 7;
			int days[] = new int[] {dow};
			WeekInterface week = new WeekInterface();
			week.setDayOfYear(iSelectedDates.get(0) - dow);
			for (int i = 0; i < 7; i++) {
				week.addDayName(new DateInterface(
						sDateFormat.format(iDates.getDate(week.getDayOfYear() + i)),
						month,
						week.getDayOfYear() + i));
			}
			List<WeekInterface> weeks = new ArrayList<WeekInterface>(); weeks.add(week);
			boolean past = !iDates.isEnabled(date) || iDates.hasFlag(date, Flag.PAST);
			for (int i = 0; i < iSelectedDates.size(); i++) {
				int startHour = 7;
				if (iSelectedTimes.getStart() != null) {
					startHour = Math.max(0, (iSelectedTimes.getStart() - 6) / 12);
				}
				int endHour = 18;
				if (iSelectedTimes.getEnd() != null) {
					endHour = Math.min(24, (17 + iSelectedTimes.getEnd()) / 12);
				}

				TimeGrid grid = new TimeGrid(colors, days, (int)(0.9 * ToolBox.getClientWidth() / days.length), false, false, startHour, endHour);
				grid.setResourceType(ResourceType.ROOM);
				grid.setSelectedWeeks(weeks);
				List<ResourceInterface> rooms = new ArrayList<EventInterface.ResourceInterface>(iSelectedRooms);
				grid.setRoomResources(rooms);
				grid.setMode(TimeGrid.Mode.OVERLAP);
				for (EventInterface event: sortedEvents()) {
					List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
					for (MeetingInterface meeting: event.getMeetings()) {
						if (meeting.getApprovalStatus() != ApprovalStatus.Pending && meeting.getApprovalStatus() != ApprovalStatus.Approved) continue;
						if (iSelectedDates.get(i) == meeting.getDayOfYear() && meeting.hasLocation())
							meetings.add(meeting);
					}
					if (!meetings.isEmpty())
						grid.addEvent(event, meetings);
				}
				grid.labelDays(week, null);
				grid.setCalendarUrl(null);
				grid.yellow(iSelectedTimes.getStart() == null ? 90 : iSelectedTimes.getStart(), iSelectedTimes.getEnd() == null ? 210 : iSelectedTimes.getEnd());
				if (past) grid.gray(0, 1);
				grid.showVerticalSplit();
				grid.addMeetingClickHandler(iMeetingClickHandler);
				iTables.add(grid);
			}
		} else {
			int[] days = new int[iSelectedDates.size()];
			WeekInterface week = new WeekInterface();
			week.setDayOfYear(iSelectedDates.get(0));
			List<String> dows = new ArrayList<String>();
			int lastPast = -1;
			for (int i = 0; i < iSelectedDates.size(); i++) {
				Date date = iDates.getDate(iSelectedDates.get(i));
				int year = Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
				int month = Integer.parseInt(DateTimeFormat.getFormat("MM").format(date));
				int dow = (SingleDateSelector.firstDayOfWeek(year, month) + Integer.parseInt(DateTimeFormat.getFormat("dd").format(date)) - 1) % 7;
				days[i] = i;
				week.addDayName(new DateInterface(sDateFormat.format(date), month, iSelectedDates.get(i)));
				dows.add(CONSTANTS.days()[dow]);
				if (!iDates.isEnabled(date) || iDates.hasFlag(date, Flag.PAST))
					lastPast = i;
			}
			List<WeekInterface> weeks = new ArrayList<WeekInterface>(); weeks.add(week);
			for (ResourceInterface room: iSelectedRooms) {
				HorizontalPanel hp = new HorizontalPanel();
				hp.setStyleName("unitime-RoomAvailabilityHeader");
				HTML name = new HTML(room.getNameWithSizeAndHint(), false); name.setStyleName("name");
				hp.add(name);
				HTML type = new HTML(room.getRoomType(), false); type.setStyleName("type");
				hp.add(type);
				HTML size = new HTML(""); //room.getSize() == null ? "" : MESSAGES.hintRoomCapacity(room.getSize().toString()), false)
				size.setStyleName("size");
				hp.add(size);
				hp.setCellHorizontalAlignment(name, HasHorizontalAlignment.ALIGN_LEFT);
				hp.setCellHorizontalAlignment(type, HasHorizontalAlignment.ALIGN_CENTER);
				hp.setCellHorizontalAlignment(size, HasHorizontalAlignment.ALIGN_RIGHT);
				hp.setCellVerticalAlignment(name, HasVerticalAlignment.ALIGN_BOTTOM);
				hp.setCellVerticalAlignment(type, HasVerticalAlignment.ALIGN_BOTTOM);
				hp.setCellVerticalAlignment(size, HasVerticalAlignment.ALIGN_BOTTOM);
				hp.setCellWidth(name, "33%");
				hp.setCellWidth(type, "34%");
				hp.setCellWidth(size, "33%");
				if (iTables.getWidgetCount() > 0) hp.addStyleName("unitime-TopLineDash");
				iTables.add(hp);
				
				int startHour = 7;
				if (iSelectedTimes.getStart() != null) {
					startHour = Math.max(0, (iSelectedTimes.getStart() - 6) / 12);
				}
				int endHour = 18;
				if (iSelectedTimes.getEnd() != null) {
					endHour = Math.min(24, (17 + iSelectedTimes.getEnd()) / 12);
				}

				TimeGrid grid = new TimeGrid(colors, days, (int)(0.9 * ToolBox.getClientWidth() / days.length), false, false, startHour, endHour);
				grid.setResourceType(ResourceType.ROOM);
				grid.setSelectedWeeks(weeks);
				List<ResourceInterface> rooms = new ArrayList<EventInterface.ResourceInterface>(); rooms.add(room);
				grid.setRoomResources(rooms);
				grid.setMode(TimeGrid.Mode.OVERLAP);
				for (EventInterface event: sortedEvents()) {
					List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
					for (MeetingInterface meeting: event.getMeetings()) {
						if (meeting.getApprovalStatus() != ApprovalStatus.Pending && meeting.getApprovalStatus() != ApprovalStatus.Approved) continue;
						if (meeting.getMeetingDate() != null && meeting.hasLocation() && meeting.getLocation().getId().equals(room.getId())) {
							int idx = iSelectedDates.indexOf(meeting.getDayOfYear());
							if (idx >= 0) {
								meeting.setGridIndex(idx);
								meetings.add(meeting);
							}
						}
					}
					if (!meetings.isEmpty())
						grid.addEvent(event, meetings);
				}
				grid.labelDays(dows, week);
				grid.setCalendarUrl(null);
				grid.yellow(iSelectedTimes.getStart() == null ? 90 : iSelectedTimes.getStart(), iSelectedTimes.getEnd() == null ? 210 : iSelectedTimes.getEnd());
				if (lastPast >= 0) grid.gray(0, lastPast);
				grid.addMeetingClickHandler(iMeetingClickHandler);
				iTables.add(grid);
			}
		}
		showResults();
		changeUrl();
	}
	
	@Override
	public Long getSessionId() {
		return iSession.getAcademicSessionId();
	}

	@Override
	public EventPropertiesRpcResponse getProperties() {
		return iProperties;
	}

	@Override
	public List<SelectionInterface> getSelection() {
		List<SelectionInterface> selection = new ArrayList<EventInterface.SelectionInterface>();
		for (int i = 0; i < iTables.getWidgetCount(); i++) {
			Widget w = iTables.getWidget(i);
			if (w instanceof TimeGrid)
				for (SelectionInterface s: ((TimeGrid)w).getSelections()) {
					SelectionInterface x = new SelectionInterface();
					x.setLength(s.getLength());
					x.setStartSlot(s.getStartSlot());
					for (ResourceInterface r: s.getLocations())
						x.addLocation(r);
					for (Integer d: s.getDays())
						x.addDay(iSelectedDates.get(d - iSelectedDates.get(0)));
					selection.add(x);
				}
		}
		return selection;
	}

	@Override
	public String getRoomFilter() {
		return iRooms.getValue();
	}
	
	@Override
	public List<Date> getSelectedDates() {
		return iDates.getValue();
	}

	@Override
	public StartEndTime getSelectedTime() {
		return iTimes.getValue();
	}
	
	@Override
	public ContactInterface getMainContact() {
		return iProperties == null ? null : iProperties.getMainContact();
	}
	
	@Override
	public SessionMonth.Flag getDateFlag(EventType type, Date date) {
		if (iSessionMonths == null || iSessionMonths.isEmpty()) return null;
		if (date == null) return null;
		int m = Integer.parseInt(DateTimeFormat.getFormat("MM").format(date));
		for (SessionMonth month: iSessionMonths)
			if (m == month.getMonth() + 1) {
				int d = Integer.parseInt(DateTimeFormat.getFormat("dd").format(date)) - 1;
				if (month.hasFlag(d, SessionMonth.Flag.FINALS) && type != EventType.FinalExam) return SessionMonth.Flag.FINALS;
				if (month.hasFlag(d, SessionMonth.Flag.BREAK)) return SessionMonth.Flag.BREAK;
				if (month.hasFlag(d, SessionMonth.Flag.WEEKEND)) return SessionMonth.Flag.WEEKEND;
				if (month.hasFlag(d, SessionMonth.Flag.HOLIDAY)) return SessionMonth.Flag.HOLIDAY;
				return null;
			}
		return null;
	}
	
	protected void changeUrl() {
		iHistoryToken.reset(null);
		iHistoryToken.setParameter("term", iSession.getAcademicSessionAbbreviation());
		iHistoryToken.setParameter("rooms", iRooms.getValue().trim());
		if (iEventDetail.equals(iRootPanel.getWidget()))
			iHistoryToken.setParameter("event", iEventDetail.getEvent().getId());
		else if (iEventAdd.equals(iRootPanel.getWidget())) {
			Long id = iEventAdd.getEventId();
			iHistoryToken.setParameter("event", id == null ? "add" : id.toString());
		}
		iSelectedTimes = iTimes.getValue();
		if (iTimes.getValue().getStart() != null) {
			iHistoryToken.setParameter("after", iTimes.getValue().getStart().toString());
		}
		if (iTimes.getValue().getEnd() != null) {
			iHistoryToken.setParameter("before", iTimes.getValue().getEnd().toString());
		}
		iSelectedDates = iDates.getSelectedDays(); 
		String dates = "";
		for (Integer date: iDates.getSelectedDays()) {
			dates += (dates.isEmpty() ? "" : ",") + date;
		}
		if (!dates.isEmpty())
			iHistoryToken.setParameter("dates", dates);
		iHistoryToken.mark();
		Client.fireGwtPageChanged(new Client.GwtPageChangeEvent());
	}
	
	private GwtRpcResponseList<EventInterface> tinker(GwtRpcResponseList<EventInterface> data, Long oldEventId, EventInterface newEvent) {
		for (Iterator<EventInterface> i = data.iterator(); i.hasNext(); ) {
			EventInterface event = i.next();

			if (event.getId().equals(oldEventId)) {
				i.remove(); continue;
			} else if (event.hasConflicts()) {
				for (Iterator<EventInterface> j = event.getConflicts().iterator(); j.hasNext(); )
					if (j.next().getId().equals(oldEventId)) j.remove();
			}
			
			if (newEvent != null && newEvent.getId() != null && event.inConflict(newEvent)) {
				event.addConflict(event.createConflictingEvent(newEvent));
			}
		}
		
		if (newEvent != null && newEvent.getId() != null)
			data.add(newEvent);
		
		return data;
	}
}
