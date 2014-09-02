/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaTabBar;
import org.unitime.timetable.gwt.client.events.AcademicSessionSelectionBox.AcademicSession;
import org.unitime.timetable.gwt.client.events.AcademicSessionSelectionBox.AcademicSessionFilter;
import org.unitime.timetable.gwt.client.events.EventMeetingTable.EventMeetingRow;
import org.unitime.timetable.gwt.client.events.StartEndTimeSelector.StartEndTime;
import org.unitime.timetable.gwt.client.events.TimeGrid.MeetingClickEvent;
import org.unitime.timetable.gwt.client.events.TimeGrid.MeetingClickHandler;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.IntervalSelector;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.TimeSelector.TimeUtils;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.WeekSelector;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.RequestSessionDetails;
import org.unitime.timetable.gwt.shared.EventInterface.SaveFilterDefaultRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.SaveOrApproveEventRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.SelectionInterface;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeHandler;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.ApproveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventDetailRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MessageInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;
import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author Tomas Muller
 */
public class EventResourceTimetable extends Composite implements EventMeetingTable.MeetingFilter, EventAdd.EventPropertiesProvider, AcademicSessionFilter {
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	
	private EventDetail iEventDetail;
	private EventAdd iEventAdd;
	
	private SimpleForm iPanel, iFilter;
	private DockPanel iDockPanel;
	private SimplePanel iGridOrTablePanel;
	private UniTimeHeaderPanel iHeader, iFooter, iFilterHeader;
	private TimeGrid iTimeGrid;
	private EventMeetingTable iTable;
	private ResourceInterface iResource;
	private List<EventInterface> iData;
	private WeekSelector iWeekPanel;
	private RoomSelector iRoomPanel;
	private AcademicSessionSelectionBox iSession;
	private ListBox iResourceTypes;
	private SuggestBox iResources;
	private int iResourcesRow = -1;
	private EventFilterBox iEvents = null;
	private RoomFilterBox iRooms = null;
	private String iLocDate = null, iLocRoom = null;
	private MeetingClickHandler iMeetingClickHandler;
	private Lookup iLookup;
	private AriaTabBar iTabBar;
	private ApproveDialog iApproveDialog;
	private int iSessionRow = -1;
	private Set<Integer> iMatchingWeeks = new HashSet<Integer>();
	private Set<Long> iMatchingRooms = new HashSet<Long>();
	
	private EventPropertiesRpcResponse iProperties = null;
	private HistoryToken iHistoryToken = null;
	private PageType iType = null;
	private boolean iInitialized = false;
	private List<EventInterface> iBack = new ArrayList<EventInterface>();
	private List<SessionMonth> iSessionMonths = null;
	
	public static enum PageType {
		Timetable("tab", "0", "title", MESSAGES.pageEventTimetable(), "rooms", "", "showClear", "true"),
		Events("filter", "events", "rooms", "flag:Event", "events", "mode:\"My Events\"", "type", "room", "title", MESSAGES.pageEvents(),
				"fixedTitle", "true", "fixedType", "true", "tab", "1", "showClear", "true", "saveAsDefault", "events,rooms"),
		RoomTimetable("type", "room", "fixedType", "true", "title", MESSAGES.pageRoomTimetable(), "showClear", "true"),
		Classes(
				"type", "subject", "fixedType", "true", "events", "type:Class", "tab", "1", "filter", "classes",
				"rooms", "", "title", MESSAGES.pageClasses(), "fixedTitle", "true", "addEvent", "false", "showFilter", "false", "showClear", "false"),
		Exams(
				"type", "subject", "fixedType", "true", "events", "type:\"Final Exam\" type:\"Midterm Exam\"",
				"tab", "1", "filter", "exams", "rooms", "", "title", MESSAGES.pageExaminations(), "fixedTitle", "true", "addEvent", "false", "showFilter", "false", "showClear", "false"),
		Personal(
				"type", "person", "fixedType", "true", "events", "", "filter", "person", "rooms", "", "title", MESSAGES.pagePersonalTimetable(),
				"addEvent", "false", "fixedTitle", "true", "showFilter", "false", "showClear", "false"
				),
		Availability("title", MESSAGES.pageEventRoomAvailability(), "rooms", "flag:Event");
		
		
		String[] iParams = null; 
		PageType(String... params) { iParams = params; }
		
		public String[] getParams() { return iParams; }
	}
	
	public EventResourceTimetable(PageType type) {
		iHistoryToken = new HistoryToken(type);
		iType = type;
		
		iFilter = new SimpleForm(2);
		iFilter.removeStyleName("unitime-NotPrintableBottomLine");
		iFilter.getColumnFormatter().setWidth(0, "120px");
		
		iFilterHeader = new UniTimeHeaderPanel(MESSAGES.sectFilter());
		iLookup = new Lookup();
		iLookup.setOptions("mustHaveExternalId");
		iLookup.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				if (event.getValue() != null) {
					iResources.setText(event.getValue().getId());
					resourceTypeChanged(true);
				}
			}
		});
		iFilterHeader.addButton("lookup", MESSAGES.buttonLookup(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iLookup.center();
			}
		});
		iFilterHeader.setEnabled("lookup", false);
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
				// Event filter
				iEvents.setValue(iHistoryToken.getDefaultParameter("events", ""));
				
				// Room filter
				iRooms.setValue(iHistoryToken.getDefaultParameter("rooms", ""));
				
				// Resource type
				if (!"true".equals(iHistoryToken.getParameter("fixedType", "false"))) {
					String typeString = iHistoryToken.getDefaultParameter("type", "room");
					if (typeString != null)
						for (int idx = 0; idx < iResourceTypes.getItemCount(); idx ++) {
							if (iResourceTypes.getValue(idx).equalsIgnoreCase(typeString)) {
								iResourceTypes.setSelectedIndex(idx);
							}
						}					
				}
				
				// Subject, curriculum, department, etc.
				iResources.setValue(iHistoryToken.getDefaultParameter("name", ""));

				// Room selection
				iLocRoom = iHistoryToken.getDefaultParameter("room", "");
				
				// Week selection
				iLocDate = iHistoryToken.getDefaultParameter("date", "");

				hideResults();
				changeUrl();
				UniTimePageLabel.getInstance().setPageName(getPageName());
				resourceTypeChanged(false);
			}
		});
		iFilterHeader.setEnabled("clear", "true".equals(iHistoryToken.getParameter("showClear", "true")));
		iFilterHeader.addButton("search", MESSAGES.buttonSearch(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				resourceTypeChanged(true);
			}
		});

		iSession = new AcademicSessionSelectionBox(iHistoryToken.getParameter("term")) {
			@Override
			protected void onInitializationSuccess(List<AcademicSession> sessions) {
				iFilter.setVisible(sessions != null && !sessions.isEmpty());
				UniTimePageHeader.getInstance().getRight().setVisible(false);
				UniTimePageHeader.getInstance().getRight().setPreventDefault(true);
			}
			
			@Override
			protected void onInitializationFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedLoadSessions(caught.getMessage()), caught);
			}
		};
		iSession.setFilter(this);
		iSessionRow = iFilter.addRow(MESSAGES.propAcademicSession(), iSession);
		
		iEvents = new EventFilterBox(iSession);
		if ("true".equals(iHistoryToken.getParameter("showFilter", "true")))
			iFilter.addRow(MESSAGES.propEventFilter(), iEvents);
		
		iRooms = new RoomFilterBox(iSession);
		if ("true".equals(iHistoryToken.getParameter("showFilter", "true")))
			iFilter.addRow(MESSAGES.propRoomFilter(), iRooms);
		
		iResourceTypes = new ListBox();
		for (ResourceType resource: ResourceType.values()) {
			if (resource.isVisible())
				iResourceTypes.addItem(resource.getPageTitle(CONSTANTS), resource.toString());
		}
		
		iResourceTypes.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iResources.setText("");
				UniTimePageLabel.getInstance().setPageName(getPageName());
				hideResults();
				if (iProperties != null)
					resourceTypeChanged(isShowingResults());
			}
		});
		int row = iFilter.addRow(MESSAGES.propResourceType(), iResourceTypes);
		if ("true".equals(iHistoryToken.getParameter("fixedType", "false")))
			iFilter.getRowFormatter().setVisible(row, false);
		
		iResources = new SuggestBox(new SuggestOracle() {
			@Override
			public void requestDefaultSuggestions(Request request, Callback callback) {
				requestSuggestions(request, callback);
			}
			@Override
			public void requestSuggestions(final Request request, final Callback callback) {
				if (iSession.getAcademicSessionId() != null) {
					RPC.execute(
							ResourceLookupRpcRequest.findResources(iSession.getAcademicSessionId(),
									ResourceType.valueOf(iResourceTypes.getValue(iResourceTypes.getSelectedIndex())),
									request.getQuery(),
									request.getLimit()), new AsyncCallback<GwtRpcResponseList<ResourceInterface>>() {
								@Override
								public void onFailure(final Throwable caught) {
									ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
									suggestions.add(new Suggestion() {
										@Override
										public String getDisplayString() {
											return "<font color='red'>" + caught.getMessage() + "</font>";
										}
										@Override
										public String getReplacementString() {
											return "";
										}});
									callback.onSuggestionsReady(request, new Response(suggestions));
									ToolBox.checkAccess(caught);
								}
								@Override
								public void onSuccess(GwtRpcResponseList<ResourceInterface> result) {
									ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
									for (ResourceInterface resource: result) {
										suggestions.add(new ResourceSuggestion(resource));
									}
									callback.onSuggestionsReady(request, new Response(suggestions));
								}
							});
				}
			}
			@Override
			public boolean isDisplayStringHTML() { return true; }
			});
		iResources.getValueBox().addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				iResources.showSuggestionList();
			}
		});
		iResourcesRow = iFilter.addRow(MESSAGES.propResource(), iResources);
		iResources.addSelectionHandler(new SelectionHandler<Suggestion>() {
			public void onSelection(SelectionEvent<Suggestion> event) {
				if (event.getSelectedItem() instanceof ResourceSuggestion) {
					resourceChanged(((ResourceSuggestion)event.getSelectedItem()).getResource());
				}
			}
		});
		iFilter.getRowFormatter().setVisible(iResourcesRow, false);

		iResourceTypes.setSelectedIndex(ResourceType.ROOM.ordinal());
		
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		iPanel.addRow(iFilter);
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		iWeekPanel = new WeekSelector(iSession);
		iWeekPanel.addValueChangeHandler(new ValueChangeHandler<WeekSelector.Interval>() {
			@Override
			public void onValueChange(ValueChangeEvent<WeekSelector.Interval> e) {
				iLocDate = iWeekPanel.getSelection();
				tabOrDataChanged(true);
			}
		});
		iWeekPanel.setFilter(new WeekSelector.Filter<WeekInterface>() {
			@Override
			public boolean filter(WeekInterface week) {
				return !iMatchingWeeks.contains(week.getDayOfYear());
			}
			@Override
			public boolean isEmpty() {
				return iMatchingRooms.isEmpty();
			}
		});
		iRoomPanel = new RoomSelector();
		iRoomPanel.setFilter(new WeekSelector.Filter<ResourceInterface>() {
			@Override
			public boolean filter(ResourceInterface room) {
				return !iMatchingRooms.contains(room.getId());
			}
			@Override
			public boolean isEmpty() {
				return iMatchingRooms.isEmpty();
			}
		});
		
		iTabBar = new AriaTabBar();
		iTabBar.addTab(MESSAGES.tabGrid(), true);
		iTabBar.addTab(MESSAGES.tabEventTable(), true);
		iTabBar.addTab(MESSAGES.tabMeetingTable(), true);
		iTabBar.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				tabOrDataChanged(true);
			}
		});
		
		iGridOrTablePanel = new SimplePanel();
		iGridOrTablePanel.setStyleName("unitime-TabPanel");
		
		final Character gridAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabGrid());
		final Character eventsAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabEventTable());
		final Character meetingsAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabMeetingTable());
		if (gridAccessKey != null || eventsAccessKey != null || meetingsAccessKey != null) {
			RootPanel.get().addDomHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					if (!iRootPanel.getWidget().equals(iPanel)) return;
					int tab = -1;
					if (gridAccessKey != null && event.getNativeEvent().getCtrlKey() && (
							event.getNativeKeyCode() == gridAccessKey || event.getNativeKeyCode() == Character.toUpperCase(gridAccessKey))) {
						tab = 0;
					}
					if (eventsAccessKey != null && event.getNativeEvent().getCtrlKey() && (
							event.getNativeKeyCode() == eventsAccessKey || event.getNativeKeyCode() == Character.toUpperCase(eventsAccessKey))) {
						tab = 1;
					}
					if (meetingsAccessKey != null && event.getNativeEvent().getCtrlKey() && (
							event.getNativeKeyCode() == meetingsAccessKey || event.getNativeKeyCode() == Character.toUpperCase(meetingsAccessKey))) {
						tab = 2;
					}
					if (tab >= 0) {
						iTabBar.selectTab(tab);
						event.preventDefault();
					}
				}
			}, KeyUpEvent.getType());
		}
		
		iDockPanel = new DockPanel();
		iDockPanel.setStyleName("unitime-EventResults");
		iDockPanel.setSpacing(0);
		iDockPanel.add(iGridOrTablePanel, DockPanel.SOUTH);
		iDockPanel.add(iRoomPanel, DockPanel.WEST);
		iDockPanel.setCellHorizontalAlignment(iRoomPanel, HasHorizontalAlignment.ALIGN_LEFT);
		iDockPanel.add(iTabBar, DockPanel.CENTER);
		iDockPanel.setCellVerticalAlignment(iTabBar, HasVerticalAlignment.ALIGN_BOTTOM);
		iDockPanel.setCellHorizontalAlignment(iTabBar, HasHorizontalAlignment.ALIGN_CENTER);
		iDockPanel.add(iWeekPanel, DockPanel.EAST);
		iDockPanel.setCellHorizontalAlignment(iWeekPanel, HasHorizontalAlignment.ALIGN_RIGHT);
		Roles.getTabpanelRole().set(iDockPanel.getElement());
		Roles.getTabpanelRole().setAriaOwnsProperty(iDockPanel.getElement(), Id.of(iTabBar.getElement()));

		iPanel.addRow(iDockPanel);
		
		hideResults();
		
		iFooter = iHeader.clonePanel();
		iPanel.addRow(iFooter);
		iRootPanel = new SimplePanel(iPanel);
		initWidget(iRootPanel);
		
		iRoomPanel.addValueChangeHandler(new ValueChangeHandler<IntervalSelector<ResourceInterface>.Interval>() {
			@Override
			public void onValueChange(ValueChangeEvent<IntervalSelector<ResourceInterface>.Interval> e) {
				iLocRoom = iRoomPanel.getSelection();
				tabOrDataChanged(true);
			}
		});

		for (int i = 1; i < iPanel.getRowCount(); i++)
			iPanel.getRowFormatter().setVisible(i, false);
					
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
				final EventMeetingTable table = new EventMeetingTable(getSelectedTab() <= 1 ? EventMeetingTable.Mode.ListOfEvents : EventMeetingTable.Mode.ListOfMeetings, false, EventResourceTimetable.this);
				table.setMeetingFilter(EventResourceTimetable.this);
				table.setShowMainContact(iProperties != null && iProperties.isCanLookupMainContact());
				table.setEvents(iData);
				table.setSortBy(iTable.getSortBy());
				table.getElement().getStyle().setWidth(1040, Unit.PX);
				
				int firstSlot = 84, lastSlot = 216;
				boolean skipDays = iEvents.hasChip(new FilterBox.Chip("day", null));
				boolean hasDay[] = new boolean[] {
						!skipDays || iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[0])),
						!skipDays || iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[1])),
						!skipDays || iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[2])),
						!skipDays || iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[3])),
						!skipDays || iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[4])),
						iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[5])),
						iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[6])) };
				for (EventInterface event: iData) {
					if (event.getType() == EventType.Unavailabile && !iEvents.hasChip(new Chip("type", "Not Available"))) continue;
					for (MeetingInterface meeting: event.getMeetings()) {
						if (filterEvent(event, meeting)) continue;
						if (meeting.getApprovalStatus() != ApprovalStatus.Pending && meeting.getApprovalStatus() != ApprovalStatus.Approved) continue;
						if (firstSlot > meeting.getStartSlot()) firstSlot = meeting.getStartSlot();
						if (lastSlot < meeting.getEndSlot()) lastSlot = meeting.getEndSlot();
						hasDay[meeting.getDayOfWeek()] = true;
					}
				}
				Chip after = iEvents.getChip("after");
				if (after != null) {
					Integer slot = TimeUtils.parseTime(CONSTANTS, after.getValue(), null);
					if (slot != null && firstSlot > slot) firstSlot = slot;
				}
				Chip before = iEvents.getChip("before");
				if (before != null) {
					Integer slot = TimeUtils.parseTime(CONSTANTS, before.getValue(), firstSlot);
					if (slot != null && lastSlot < slot) lastSlot = slot;
				}
				int nrDays = 0;
				for (boolean d: hasDay) if (d) nrDays++;
				int days[] = new int[nrDays];
				int d = 0;
				for (int i = 0; i < 7; i++)
					if (hasDay[i]) days[d++] = i;
				int firstHour = firstSlot / 12;
				int lastHour = 1 + (lastSlot - 1) / 12;
				if (firstHour <= 7 && firstHour > 0 && ((firstSlot % 12) <= 6)) firstHour--;
				HashMap<Long, String> colors = new HashMap<Long, String>();
				
				final TimeGrid tg = new TimeGrid(colors, days, (int)(1000 / nrDays), 55, true, false, (firstHour < 7 ? firstHour : 7), (lastHour > 18 ? lastHour : 18));
				tg.setResourceType(getResourceType());
				tg.setSelectedWeeks(iWeekPanel.getSelected());
				tg.setRoomResources(iRoomPanel.getSelected());
				tg.setMode(gridMode());
				tg.showVerticalSplit();

				for (EventInterface event: sortedEvents()) {
					List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
					for (MeetingInterface meeting: event.getMeetings()) {
						if (meeting.getApprovalStatus() != ApprovalStatus.Pending && meeting.getApprovalStatus() != ApprovalStatus.Approved) continue;
						if (meeting.getMeetingDate() != null && !filterEvent(event, meeting))
							meetings.add(meeting);
					}
					if (!meetings.isEmpty())
						tg.addEvent(event, meetings);
				}
				if (iWeekPanel.getValue() != null)
					tg.labelDays(iWeekPanel.getValue().getFirst(), iWeekPanel.getValue().getLast());
				
				// Move header row to thead
				Element headerRow = table.getRowFormatter().getElement(0);
				Element tableElement = table.getElement();
				Element thead = DOM.createTHead();
				tableElement.insertFirst(thead);
				headerRow.getParentElement().removeChild(headerRow);
				thead.appendChild(headerRow);
				
				ToolBox.print(
						new ToolBox.Page() {
							@Override
							public String getName() { return iHeader.getHeaderTitle(); }
							@Override
							public String getUser() { return iRoomPanel.getValue().toString(); }
							@Override
							public String getSession() { return iWeekPanel.getValue().toString(); }
							@Override
							public Element getBody() { return tg.getElement(); }
						},
						new ToolBox.Page() {
							@Override
							public String getName() { return iHeader.getHeaderTitle(); }
							@Override
							public String getUser() { return iRoomPanel.getValue().toString(); }
							@Override
							public String getSession() { return iWeekPanel.getValue().toString(); }
							@Override
							public Element getBody() { return table.getElement(); }
						}
						);
			}
		});

		iHeader.addButton("export", MESSAGES.buttonExport(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				if (iProperties != null && iProperties.isCanExportCSV()) {
					final PopupPanel popup = new PopupPanel(true);
					MenuBar menu = new MenuBar(true);
					MenuItem exportPdf = new MenuItem(MESSAGES.opExportPDF(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							export("output=" + (getSelectedTab() <= 1 ? "events" : "meetings") + ".pdf&flags=" + EventCookie.getInstance().getFlags());
						}
					});
					exportPdf.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(exportPdf);
					MenuItem exportCsv = new MenuItem(MESSAGES.opExportCSV(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							export("output=" + (getSelectedTab() <= 1 ? "events" : "meetings") + ".csv&flags=" + EventCookie.getInstance().getFlags());
						}
					});
					exportCsv.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(exportCsv);
					MenuItem exportIcs = new MenuItem(MESSAGES.opExportICalendar(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							export("output=events.ics");
						}
					});
					exportIcs.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(exportIcs);
					MenuItem copyIcs = new MenuItem(MESSAGES.opCopyToClipboardICalendar(), true, new Command() {
						@Override
						public void execute() {
							popup.hide();
							copyToClipboard("output=events.ics");
						}
					});
					copyIcs.getElement().getStyle().setCursor(Cursor.POINTER);
					menu.addItem(copyIcs);
					popup.add(menu);
					popup.showRelativeTo((UIObject)clickEvent.getSource());
					menu.focus();
				} else {
					export("output=events.ics");
				}
			}
		});
		iHeader.addButton("operations", MESSAGES.buttonMoreOperations(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new PopupPanel(true);
				iTable.getHeader(0).setMenu(popup);
				popup.showRelativeTo((UIObject)event.getSource());
				((MenuBar)popup.getWidget()).focus();
			}
		});
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export", false);
		iHeader.setEnabled("operations", false);
		
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
							resourceTypeChanged(isShowingResults());
					}
				});
			}
		});
		
		iEventDetail = new EventDetail(this) {
			@Override
			protected void onHide() {
				iRootPanel.setWidget(iPanel);
				UniTimePageLabel.getInstance().setPageName(getPageName());
				if (!isShowingResults())
					resourceTypeChanged(true);
				changeUrl();
			}
			@Override
			protected void onShow() {
				iBack.clear();
				if (iTable != null) iTable.clearHover();
				iRootPanel.setWidget(iEventDetail);
				changeUrl();
			}
			@Override
			protected void edit() {
				super.hide();
				iEventAdd.setEvent(getEvent());
				iEventAdd.show();
			}
			@Override
			protected EventInterface getPrevious(Long eventId) {
				return iTable.previous(eventId);
			}
			@Override
			protected EventInterface getNext(Long eventId) {
				return iTable.next(eventId);
			}
			@Override
			protected void previous(final EventInterface event) {
				iBack.add(getEvent());
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
				if (!iBack.isEmpty()) {
					EventInterface last = iBack.remove(iBack.size() - 1);
					setEvent(last);
				} else {
					super.hide();
				}
			}
			@Override
			protected void onApprovalOrReject(Long eventId, EventInterface event) {
				if (iData != null)
					populate(tinker(new GwtRpcResponseList<EventInterface>(iData), eventId, event));
			}
		};
		
		iEventAdd = new EventAdd(iSession, this) {
			@Override
			protected void onHide() {
				iSession.setFilter(EventResourceTimetable.this);
				iFilter.setWidget(iSessionRow, 1, iSession);
				final EventInterface modified = iEventAdd.getEvent(), detail = iEventDetail.getEvent(), saved = iEventAdd.getSavedEvent();
				if (saved != null) {
					if (iData != null)
						populate(tinker(new GwtRpcResponseList<EventInterface>(iData), (saved.getId() == null ? modified.getId() : saved.getId()), saved));
					if (saved.getId() != null) {
						iEventDetail.setEvent(saved);
						iEventDetail.show();
					} else {
						iRootPanel.setWidget(iPanel);
						UniTimePageLabel.getInstance().setPageName(getPageName());
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
							if (result == null) {
								iRootPanel.setWidget(iPanel);
								UniTimePageLabel.getInstance().setPageName(getPageName());
							} else {
								iEventDetail.setEvent(result);
								iEventDetail.show();
							}
						}
					}, MESSAGES.waitLoading(detail.getName()));
				} else {
					iRootPanel.setWidget(iPanel);
					UniTimePageLabel.getInstance().setPageName(getPageName());
				}
				changeUrl();
			}
			@Override
			protected void onShow() {
				if (iTable != null) iTable.clearHover();
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
		
		iTable = new EventMeetingTable(EventMeetingTable.Mode.ListOfEvents, true, this) {
			@Override
			protected void onSortByChanded(EventComparator.EventMeetingSortBy sortBy, boolean asc) {
				changeUrl();
			}
			@Override
			protected void onColumnShownOrHid(int eventCookieFlags) {
				changeUrl();
			}
		};
		iTable.setMeetingFilter(this);
		iTable.addMouseClickListener(new MouseClickListener<EventMeetingRow>() {
			@Override
			public void onMouseClick(final TableEvent<EventMeetingRow> event) {
				if (event.getData() == null) return;
				final EventInterface e = event.getData().getEvent();
				if (e == null || !e.isCanView()) return;
				LoadingWidget.execute(EventDetailRpcRequest.requestEventDetails(iSession.getAcademicSessionId(), e.getId()), new AsyncCallback<EventInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						UniTimeNotifications.error(MESSAGES.failedLoad(e.getName(), caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(EventInterface result) {
						LoadingWidget.getInstance().hide();
						iEventDetail.setEvent(result);
						iEventDetail.show();
					}
				}, MESSAGES.waitLoading(e.getName()));
			}
		});
		
		iApproveDialog = new ApproveDialog(this) {
			@Override
			protected void onSubmit(ApproveEventRpcRequest.Operation operation, List<EventMeetingRow> data, String message, boolean emailConfirmation) {
				Map<EventInterface, List<MeetingInterface>> event2meetings = new HashMap<EventInterface, List<MeetingInterface>>();
				List<EventInterface> events = new ArrayList<EventInterface>();
				for (EventMeetingRow row: data) {
					if (row.getMeeting() != null) {
						List<MeetingInterface> meetings = event2meetings.get(row.getEvent());
						if (meetings == null) {
							meetings = new ArrayList<EventInterface.MeetingInterface>();
							event2meetings.put(row.getEvent(), meetings);
							events.add(row.getEvent());
						}
						meetings.add(row.getMeeting());
					} else {
						events.add(row.getEvent());
					}
				}
				onSubmit(operation, events.iterator(), event2meetings, message, emailConfirmation, new GwtRpcResponseList<EventInterface>(iData));
			}
			
			protected void onSubmit(final ApproveEventRpcRequest.Operation operation, final Iterator<EventInterface> events, final Map<EventInterface, List<MeetingInterface>> event2meetings, final String message, final boolean emailConfirmation, final GwtRpcResponseList<EventInterface> data) {
				if (events.hasNext()) {
					final EventInterface event = events.next();
					List<MeetingInterface> meetings = event2meetings.get(event);
					if (meetings == null) {
						meetings = new ArrayList<MeetingInterface>();
						for (MeetingInterface meeting: event.getMeetings()) {
							switch(operation) {
							case APPROVE:
							case REJECT:
								if (meeting.isCanApprove() && !filter(event, meeting)) meetings.add(meeting);
								break;
							case CANCEL:
								if (meeting.isCanCancel() && !filter(event, meeting)) meetings.add(meeting);
								break;
							case INQUIRE:
								if (meeting.isCanInquire() && !filter(event, meeting)) meetings.add(meeting);
								break;
							}
						}
					}
					if (meetings.isEmpty()) {
                        onSubmit(operation, events, event2meetings, message, emailConfirmation, data);
					} else {
						switch (operation) {
						case APPROVE: LoadingWidget.getInstance().show(MESSAGES.waitForApproval(event.getName())); break;
						case INQUIRE: LoadingWidget.getInstance().show(MESSAGES.waitForInquiry(event.getName())); break;
						case REJECT: LoadingWidget.getInstance().show(MESSAGES.waitForRejection(event.getName())); break;
						case CANCEL: LoadingWidget.getInstance().show(MESSAGES.waitForCancellation(event.getName())); break;
						}
						RPC.execute(ApproveEventRpcRequest.createRequest(operation, iSession.getAcademicSessionId(), event, meetings, message, emailConfirmation), new AsyncCallback<SaveOrApproveEventRpcResponse>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.error(caught);
								onSubmit(operation, events, event2meetings, message, emailConfirmation, data);
							}
							@Override
							public void onSuccess(SaveOrApproveEventRpcResponse result) {
								LoadingWidget.getInstance().hide();
								if (result.hasMessages())
									for (MessageInterface m: result.getMessages()) {
										if (m.isError())
											UniTimeNotifications.warn(m.getMessage());
										else if (m.isWarning())
											UniTimeNotifications.error(m.getMessage());
										else
											UniTimeNotifications.info(m.getMessage());
									}
								switch (operation) {
								case APPROVE:
								case REJECT:
								case CANCEL:
									tinker(data, event.getId(), result.getEvent());
								}
								onSubmit(operation, events, event2meetings, message, emailConfirmation, data);
							}
						});
					}
				} else {
					LoadingWidget.getInstance().hide();
					populate(data);
				}
			}
		};
		iTable.setOperation(EventMeetingTable.OperationType.Approve, iApproveDialog);
		iTable.setOperation(EventMeetingTable.OperationType.Reject, iApproveDialog);
		iTable.setOperation(EventMeetingTable.OperationType.Inquire, iApproveDialog);
		iTable.setOperation(EventMeetingTable.OperationType.Cancel, iApproveDialog);
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (iInitialized) {
					if ("event=add".equals(event.getValue()))
						iHistoryToken.parse(event.getValue());
					else
						iHistoryToken.reset(event.getValue());
					setup(false);
					if (LoadingWidget.getInstance().isShowing())
						LoadingWidget.getInstance().hide();
				}
			}
		});
	}
	
	private int getSelectedTab() {
		return iTabBar.getSelectedTab();
	}
	
	private void tabOrDataChanged(boolean keepSelection) {
		if (iTimeGrid != null) iTimeGrid.hideSelectionPopup();
		if (iTable != null) iTable.clearHover();
		if (getSelectedTab() == 0) {
			int nrDays = 4;
			int firstSlot = 84, lastSlot = 216;
			for (EventInterface event: iData) {
				if (event.getType() == EventType.Unavailabile && !iEvents.hasChip(new Chip("type", "Not Available"))) continue;
				for (MeetingInterface meeting: event.getMeetings()) {
					if (filterEvent(event, meeting)) continue;
					if (meeting.getApprovalStatus() != ApprovalStatus.Pending && meeting.getApprovalStatus() != ApprovalStatus.Approved) continue;
					if (firstSlot > meeting.getStartSlot()) firstSlot = meeting.getStartSlot();
					if (lastSlot < meeting.getEndSlot()) lastSlot = meeting.getEndSlot();
					nrDays = Math.max(nrDays, meeting.getDayOfWeek());
				}
			}
			Chip after = iEvents.getChip("after");
			if (after != null) {
				Integer slot = TimeUtils.parseTime(CONSTANTS, after.getValue(), null);
				if (slot != null && firstSlot > slot) firstSlot = slot;
			}
			Chip before = iEvents.getChip("before");
			if (before != null) {
				Integer slot = TimeUtils.parseTime(CONSTANTS, before.getValue(), firstSlot);
				if (slot != null && lastSlot < slot) lastSlot = slot;
			}
			nrDays ++;
			int days[] = new int[nrDays];
			for (int i = 0; i < days.length; i++) days[i] = i;
			int firstHour = firstSlot / 12;
			int lastHour = 1 + (lastSlot - 1) / 12;
			if (firstHour <= 7 && firstHour > 0 && ((firstSlot % 12) <= 6)) firstHour--;
			HashMap<Long, String> colors = new HashMap<Long, String>();
			List<SelectionInterface> selections = null;

			if (iTimeGrid != null) {
				if (keepSelection) selections = iTimeGrid.getSelections();
				iTimeGrid.destroy();
			}
			iTimeGrid = new TimeGrid(colors, days, (int)(0.9 * ToolBox.getClientWidth() / nrDays), false, false, (firstHour < 7 ? firstHour : 7), (lastHour > 18 ? lastHour : 18));
			iTimeGrid.addMeetingClickHandler(iMeetingClickHandler);

			iTimeGrid.clear();
			iTimeGrid.setResourceType(getResourceType());
			iTimeGrid.setSelectedWeeks(iWeekPanel.getSelected());
			iTimeGrid.setRoomResources(iRoomPanel.getSelected());
			if (selections != null) iTimeGrid.getSelections().addAll(selections);
			iTimeGrid.setMode(gridMode());
			int first = iWeekPanel.getFirstDayOfYear(), last = iWeekPanel.getLastDayOfYear();
			for (EventInterface event: sortedEvents()) {
				List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
				for (MeetingInterface meeting: event.getMeetings()) {
					if (meeting.getApprovalStatus() != ApprovalStatus.Pending && meeting.getApprovalStatus() != ApprovalStatus.Approved) continue;
					if (meeting.getMeetingDate() != null && !filterEvent(event, meeting) && first <= meeting.getDayOfYear() && meeting.getDayOfYear() <= last)
						meetings.add(meeting);
				}
				if (!meetings.isEmpty())
					iTimeGrid.addEvent(event, meetings);
			}
			iTimeGrid.shrink(iEvents.hasChip(new FilterBox.Chip("day", null)), iEvents.hasChip(new Chip("type", "Not Available")),
					iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[0])),
					iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[1])),
					iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[2])),
					iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[3])),
					iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[4])),
					iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[5])),
					iEvents.hasChip(new FilterBox.Chip("day", CONSTANTS.longDays()[6])));
			List<WeekInterface> selected = iWeekPanel.getSelected();
			if (selected != null && !selected.isEmpty())
				iTimeGrid.labelDays(selected.get(0), selected.size() == 1 ? null : selected.get(selected.size() - 1));
			iGridOrTablePanel.setWidget(iTimeGrid);
		} else {
			iTable.setMode(getSelectedTab() == 1 ? EventMeetingTable.Mode.ListOfEvents : EventMeetingTable.Mode.ListOfMeetings);
			iTable.setEvents(iData);
			iGridOrTablePanel.setWidget(iTable);
		}
		iHeader.setHeaderTitle(name(getSelectedTab()));
		changeUrl();
	}
	
	private void setup(boolean init) {
		if (init)
			iInitialized = true;
		boolean reload = init;
		boolean isDefault = true;
		if (iHistoryToken.isChanged("events", "", iEvents.getValue())) {
			iEvents.setValue(iHistoryToken.getParameter("events", ""), true);
			reload = true;
			if (!iEvents.getValue().equals("")) isDefault = false;
		}
		if (iHistoryToken.isChanged("type", "room", iResourceTypes.getValue(iResourceTypes.getSelectedIndex()).toLowerCase())) {
			String typeString = iHistoryToken.getParameter("type", "room");
			if (typeString != null)
				for (int idx = 0; idx < iResourceTypes.getItemCount(); idx ++) {
					if (iResourceTypes.getValue(idx).equalsIgnoreCase(typeString)) {
						iResourceTypes.setSelectedIndex(idx);
					}
				}
			reload = true;
		}
		if (iHistoryToken.isChanged("rooms", (getResourceType() == ResourceType.ROOM ? "flag:Event" : ""), iRooms.getValue())) {
			iRooms.setValue(iHistoryToken.getParameter("rooms", (getResourceType() == ResourceType.ROOM ? "flag:Event" : "")), true);
			reload = true;
			if (!iRooms.getValue().equals(getResourceType() == ResourceType.ROOM ? "flag:Event" : "")) {
				isDefault = false;
			}
		}
		if (iHistoryToken.isChanged("name", "", iResources.getText())) {
			iResources.setText(iHistoryToken.getParameter("name", ""));
			reload = (getResourceType() != ResourceType.ROOM);
			if (!iResources.getText().equals("")) isDefault = false;
		}
		UniTimePageLabel.getInstance().setPageName(getPageName());
		if (iHistoryToken.isChanged("tab", "0", String.valueOf(getSelectedTab()))) {
			iTabBar.selectTab(Integer.parseInt(iHistoryToken.getParameter("tab", "0")), isShowingResults());
		}
		boolean fireWeekPanel = false, fireRoomPanel = false;
		if (iHistoryToken.isChanged("date", iLocDate)) {
			iLocDate = iHistoryToken.getParameter("date");
			if (iWeekPanel.hasValues()) {
				iWeekPanel.setValue(iWeekPanel.parse(iLocDate));
				iWeekPanel.setFilterEnabled(!iMatchingWeeks.isEmpty());
				fireWeekPanel = isShowingResults();
			}
		}
		if (iHistoryToken.isChanged("room", iLocRoom)) {
			iLocRoom = iHistoryToken.getParameter("room");
			if (iRoomPanel.hasValues()) {
				iRoomPanel.setFilterEnabled(!iMatchingRooms.isEmpty());
				iRoomPanel.setValue(iRoomPanel.parse(iLocRoom));
				fireRoomPanel = isShowingResults();
			}
		}
		if (iHistoryToken.hasParameter("sort"))
			iTable.setSortBy(iHistoryToken.getParameter("sort"));
		if (iHistoryToken.hasParameter("event")) {
			if (iHistoryToken.isChanged("term", iSession.getAcademicSessionAbbreviation()) && iHistoryToken.getParameter("term") != null)
				iSession.selectSession(iHistoryToken.getParameter("term"), null);
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
				resourceTypeChanged(isShowingResults() || !isDefault);
			}
		}
		if (fireWeekPanel)
			ValueChangeEvent.fire(iWeekPanel, iWeekPanel.getValue());
		if (fireRoomPanel)
			ValueChangeEvent.fire(iRoomPanel, iRoomPanel.getValue());
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
	
	public ResourceType getResourceType() {
		if (iResourceTypes.getSelectedIndex() < 0)
			return null;
		return ResourceType.values()[iResourceTypes.getSelectedIndex()];
	}
	
	public String getResourceName() {
		if (getResourceType() == ResourceType.PERSON && (iProperties == null || !iProperties.isCanLookupPeople())) return "";
		return (iResources.getText() == null || iResources.getText().isEmpty() ? null : iResources.getText());
	}
	
	private void resourceTypeChanged(boolean loadData) {
		ResourceType type = getResourceType();
		if (type == null) {
			UniTimeNotifications.warn(MESSAGES.warnNoResourceType());
			return;
		}
		if (type == ResourceType.ROOM) {
			iFilter.getRowFormatter().setVisible(iResourcesRow, false);
			iFilterHeader.setEnabled("lookup", false);
			if (loadData) {
				if (iSession.getAcademicSessionId() == null) {
					UniTimeNotifications.warn(MESSAGES.warnNoSession());
				} else if (iProperties == null) {
					UniTimeNotifications.warn(MESSAGES.warnNoEventProperties(iSession.getAcademicSessionName()));
				} else {
					ResourceInterface resource = new ResourceInterface();
					resource.setType(ResourceType.ROOM);
					resource.setName(iRooms.getValue());
					resourceChanged(resource);
				}
			}
		} else {
			iFilter.getRowFormatter().setVisible(iResourcesRow, type != ResourceType.PERSON);
			((Label)iFilter.getWidget(iResourcesRow, 0)).setText(type.getName(CONSTANTS) + ":");
			iFilterHeader.setEnabled("lookup", iProperties != null && iProperties.isCanLookupPeople() && getResourceType() == ResourceType.PERSON);
			if (getResourceName() != null || (type == ResourceType.PERSON && loadData)) {
				if (iSession.getAcademicSessionId() == null) {
					UniTimeNotifications.warn(MESSAGES.warnNoSession());
				} else if (iProperties == null) {
					UniTimeNotifications.warn(MESSAGES.warnNoEventProperties(iSession.getAcademicSessionName()));
				} else {
					LoadingWidget.execute(ResourceLookupRpcRequest.findResource(iSession.getAcademicSessionId(), type, getResourceName()),
							new AsyncCallback<GwtRpcResponseList<ResourceInterface>>() {
						@Override
						public void onFailure(Throwable caught) {
							UniTimeNotifications.error(MESSAGES.failedLoad(getResourceName(), caught.getMessage()), caught);
							hideResults();
						}
						@Override
						public void onSuccess(GwtRpcResponseList<ResourceInterface> result) {
							resourceChanged(result.get(0));
						}
					}, MESSAGES.waitLoading(type.getName(CONSTANTS) + (type != ResourceType.PERSON ? " " + getResourceName() : "")));
				}
			} else if (getResourceName() == null && loadData) {
				UniTimeNotifications.warn(MESSAGES.warnNoResourceName(CONSTANTS.resourceName()[getResourceType().ordinal()].toLowerCase()));
				iResources.setFocus(true);
			}
		}
		if (!loadData) {
			iData = null;
			hideResults();
		}
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
	
	private void resourceChanged(final ResourceInterface resource) {
		iResource = resource;
		LoadingWidget.execute(iRooms.getElementsRequest(), new AsyncCallback<FilterRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedLoad(resource.getType() == ResourceType.ROOM ? MESSAGES.resourceRoom().toLowerCase() : resource.getName(), caught.getMessage()), caught);
				hideResults();
			}

			@Override
			public void onSuccess(FilterRpcResponse result) {
				if (result == null) return;
				List<ResourceInterface> rooms = new ArrayList<ResourceInterface>();
				if (result.hasResults())
					for (FilterRpcResponse.Entity room: result.getResults()) {
						rooms.add(new ResourceInterface(room));
					}
				Collections.sort(rooms);
				iRoomPanel.setValues(rooms);
				
				if (!result.hasResults() || result.getResults().isEmpty()) {
					UniTimeNotifications.error(MESSAGES.errorNoMatchingRooms());
					hideResults();
					return;
				}
				
				LoadingWidget.execute(EventLookupRpcRequest.findEvents(iSession.getAcademicSessionId(), iResource, iEvents.getElementsRequest(), iRooms.getElementsRequest(), CONSTANTS.maxMeetings()), 
						new AsyncCallback<GwtRpcResponseList<EventInterface>>() {
					@Override
					public void onSuccess(GwtRpcResponseList<EventInterface> result) {
						if (result != null) populate(result);
						iApproveDialog.reset(iProperties);
					}
			
					@Override
					public void onFailure(Throwable caught) {
						UniTimeNotifications.error(MESSAGES.failedLoad(resource.getType() == ResourceType.ROOM ? MESSAGES.resourceRoom().toLowerCase() : resource.getName(), caught.getMessage()), caught);
						hideResults();
					}
				}, MESSAGES.waitLoadingTimetable(resource.getType() == ResourceType.ROOM ? MESSAGES.resourceRoom().toLowerCase() : resource.getName(), iSession.getAcademicSessionName()));
			}
			
		}, MESSAGES.waitLoadingTimetable(resource.getType() == ResourceType.ROOM ? MESSAGES.resourceRoom().toLowerCase() : resource.getName(), iSession.getAcademicSessionName()));
	}
	
	private void populate(GwtRpcResponseList<EventInterface> result) {
		iData = result;
		if (iData.isEmpty())
			UniTimeNotifications.warn(MESSAGES.failedNoEvents());
		
		Collections.sort(iData);
		
		iMatchingRooms.clear();
		int nrMeetings = 0;
		for (EventInterface event: iData) {
			for (MeetingInterface meeting: event.getMeetings()) {
				if (meeting.getLocation() != null) iMatchingRooms.add(meeting.getLocation().getId());
			}
			nrMeetings += event.getMeetings().size();
		}
		iRoomPanel.setValue(iRoomPanel.parse(iLocRoom));
		iRoomPanel.setFilterEnabled(!iMatchingRooms.isEmpty());

		iMatchingWeeks.clear();
		for (WeekInterface week: iWeekPanel.getValues()) {
			boolean hasEvents = false;
			events: for (EventInterface event: iData) {
				for (MeetingInterface meeting: event.getMeetings()) {
					if (meeting.getDayOfYear() >= week.getDayOfYear() && meeting.getDayOfYear() < week.getDayOfYear() + 7) {
						hasEvents = true;
						break events;
					}
				}
			}
			if (hasEvents) iMatchingWeeks.add(week.getDayOfYear());
		}
		iWeekPanel.setValue(iWeekPanel.parse(iLocDate));
		iWeekPanel.setFilterEnabled(!iMatchingWeeks.isEmpty());

		if (nrMeetings > CONSTANTS.maxMeetings())
			iHeader.setErrorMessage(MESSAGES.warnTooManyMeetings(CONSTANTS.maxMeetings()));
		else
			iHeader.clearMessage();
		
		tabOrDataChanged(false);
							
		showResults();
		
		changeUrl();
	}
	
	private String name(int tab) {
		String resource;
		if (iResource.getType() == ResourceType.ROOM) {
			if (iRoomPanel.getValue() != null && iRoomPanel.getValue().isOne()) {
				resource = iRoomPanel.getValue().getFirst().getName();
			} else if (iRoomPanel.getValues() != null && iRoomPanel.getValues().size() == 1) {
				resource = iRoomPanel.getValues().get(0).getName();
			} else {
				resource = MESSAGES.resourceRoom();
			}
		} else {
			resource = iResource.getNameWithHint();
		}
		String session = iSession.getAcademicSessionName();
		if (iWeekPanel.getValue() != null && !iWeekPanel.getValue().isAll())
			session = iWeekPanel.getValue().toString().toLowerCase();
		switch (tab) {
		case 0: return MESSAGES.sectTimetable(resource, session);
		case 1: return MESSAGES.sectEventList(resource, session);
		case 2: return MESSAGES.sectMeetingList(resource, session);
		}
		return null;
	}
	
	@Override
	public boolean filter(EventInterface event, MeetingInterface meeting) {
		// if (event != null && event.getId() == null) return true;
		if (event != null && event.getType() == EventType.Unavailabile && !iEvents.hasChip(new Chip("type", "Not Available"))) return true;
		return filterEvent(event, meeting);
	}
	
	public boolean filterEvent(EventInterface event, MeetingInterface meeting) {
		if (event != null && event.getType() == EventType.Unavailabile && !iEvents.hasChip(new Chip("type", "Not Available"))) {
			if (getResourceType() != ResourceType.ROOM || gridMode() != TimeGrid.Mode.OVERLAP) return true;
		}
		if (iWeekPanel.getValue() != null && !iWeekPanel.getValue().isAll()) {
			int firstDayOfYear = iWeekPanel.getValue().getFirst().getDayOfYear();
			int lastDayOfYear = (iWeekPanel.getValue().isOne() ? iWeekPanel.getValue().getFirst() : iWeekPanel.getValue().getLast()).getDayOfYear() + 6;
			if (meeting.getDayOfYear() < firstDayOfYear || meeting.getDayOfYear() > lastDayOfYear)
				return true;
		}
		if (iRoomPanel.getValue() != null && !iRoomPanel.getValue().isAll()) {
			if (iRoomPanel.getValue().isOne()) {
				if (!iRoomPanel.getValue().getFirst().getName().equals(meeting.getLocationName())) return true;
			} else {
				if (iRoomPanel.getValue().getFirst().getName().compareTo(meeting.getLocationName()) > 0) return true;
				if (iRoomPanel.getValue().getLast().getName().compareTo(meeting.getLocationName()) < 0) return true;
			}
		}
		
		return false;
	}
	
	private TimeGrid.Mode gridMode() {
		if ("0".equals(iHistoryToken.getParameter("eq"))) return TimeGrid.Mode.FILLSPACE;
		switch (iResource.getType()) {
		case ROOM:
			return (iRoomPanel.isOne() || (iWeekPanel.isOne() && iRoomPanel.getSelected().size() <= 20) ? TimeGrid.Mode.OVERLAP : TimeGrid.Mode.PROPORTIONAL);
		case PERSON:
			return TimeGrid.Mode.OVERLAP;
		default:
			return (iRoomPanel.isOne() || (iWeekPanel.isOne() && iRoomPanel.getSelected().size() <= 10) ? TimeGrid.Mode.OVERLAP : TimeGrid.Mode.PROPORTIONAL);
		}
	}
		
	public static class ResourceSuggestion implements Suggestion {
		private ResourceInterface iResource;
		
		public ResourceSuggestion(ResourceInterface resource) {
			iResource = resource;
		}
		
		public ResourceInterface getResource() {
			return iResource;
		}

		@Override
		public String getDisplayString() {
			if (iResource.hasTitle())
				return iResource.getTitle();
			if (iResource.hasAbbreviation() && !iResource.getAbbreviation().equals(iResource.getName()))
				return iResource.getAbbreviation() + " - " + iResource.getName();
			return (iResource.hasAbbreviation() ? iResource.getAbbreviation() : iResource.getName());
		}

		@Override
		public String getReplacementString() {
			return (iResource.hasAbbreviation() ? iResource.getAbbreviation() : iResource.getName());
		}
	}
	
	protected String query(String extra) {
		String query = "sid=" + iSession.getAcademicSessionId() +
				(iResource == null || iResource.getType() == null ? "" : "&type=" + iResource.getType().toString().toLowerCase()) +
				(iResource == null || iResource.getId() == null ? "" : "&id=" + iResource.getId()) +
				(iResource == null || iResource.getExternalId() == null ? "" : "&ext=" + iResource.getExternalId());
		
		FilterRpcRequest events = iEvents.getElementsRequest();
		if (iWeekPanel.getValue() != null && !iWeekPanel.getValue().isAll()) {
			events.setOption("from", String.valueOf(iWeekPanel.getValue().getFirst().getDayOfYear()));
			events.setOption("to", String.valueOf((iWeekPanel.getValue().isOne() ? iWeekPanel.getValue().getFirst() : iWeekPanel.getValue().getLast()).getDayOfYear() + 6));
		}
		if (iRoomPanel.getValue() != null && !iRoomPanel.getValue().isAll()) {
			for (ResourceInterface resource: iRoomPanel.getSelected())
				events.addOption("room", resource.getId().toString());
		} else {
			FilterRpcRequest rooms = iRooms.getElementsRequest();
			if (rooms.hasOptions()) {
				for (Map.Entry<String, Set<String>> option: rooms.getOptions().entrySet()) {
					for (String value: option.getValue()) {
						query += "&r:" + option.getKey() + "=" + URL.encodeQueryString(value);
					}
				}
			}
			if (rooms.getText() != null && !rooms.getText().isEmpty()) {
				query += "&r:text=" + URL.encodeQueryString(rooms.getText());
			}
		}

		if (events.hasOptions()) {
			for (Map.Entry<String, Set<String>> option: events.getOptions().entrySet()) {
				for (String value: option.getValue()) {
					query += "&e:" + option.getKey() + "=" + URL.encodeQueryString(value);
				}
			}
		}
		if (events.getText() != null && !events.getText().isEmpty()) {
			query += "&e:text=" + URL.encodeQueryString(events.getText());
		}
		
		if (iTable.hasSortBy())
			query += "&sort=" + iTable.getSortBy();
		
		if (extra != null && !extra.isEmpty()) query += "&" + extra;
		
		if (iEvents.hasChip(new Chip("type", "Not Available")) || (getResourceType() == ResourceType.ROOM && gridMode() == TimeGrid.Mode.OVERLAP))
			query += "&ua=1";
		
		return query;
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
	
	protected void copyToClipboard(String format) {
		RPC.execute(EncodeQueryRpcRequest.encode(query(format)), new AsyncCallback<EncodeQueryRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(EncodeQueryRpcResponse result) {
				final UniTimeDialogBox dialog = new UniTimeDialogBox(true, false);
				dialog.setText(MESSAGES.opCopyToClipboardICalendar());
				dialog.setEscapeToHide(true);
				final TextArea ta = new TextArea();
				ta.setStyleName("unitime-TextArea");
				ta.setVisibleLines(5);
				ta.setCharacterWidth(80);
				ta.setText(GWT.getHostPageBaseURL() + "export?q=" + result.getQuery());
				UniTimeWidget<TextArea> w = new UniTimeWidget<TextArea>(ta);
				w.setHint(MESSAGES.hintCtrlCToCopy());
				dialog.setWidget(w);
				Scheduler.get().scheduleDeferred(new Command() {
					@Override
					public void execute() {
						ta.setFocus(true);
						ta.selectAll();
					}
				});
				dialog.center();
			}
		});
	}
	
	private native static void nativeCopyToClipboard(Element e)/*-{
		alert('here');
		e.focus();
		e.select();
		$doc.execCommand('Copy');
		alert('Copied');
	}-*/;
		
	protected void changeUrl() {
		if (iTimeGrid != null) {
			RPC.execute(EncodeQueryRpcRequest.encode(query("output=events.ics")), new AsyncCallback<EncodeQueryRpcResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					iHeader.setEnabled("export", false);
					iHeader.setEnabled("operations", false);
					iTimeGrid.setCalendarUrl(null);
				}
				@Override
				public void onSuccess(EncodeQueryRpcResponse result) {
					iTimeGrid.setCalendarUrl(GWT.getHostPageBaseURL() + "export?q=" + result.getQuery());
					iHeader.setEnabled("export", true);
					iHeader.setEnabled("operations", getSelectedTab() > 0);
				}
			});
		}
		
		iHistoryToken.reset(null);
		iHistoryToken.setParameter("term", iSession.getAcademicSessionAbbreviation());
		iHistoryToken.setParameter("type", iResourceTypes.getValue(iResourceTypes.getSelectedIndex()).toLowerCase());
		if (iResource != null && iResource.getAbbreviation() != null && iResource.getType() != ResourceType.PERSON)
			 iHistoryToken.setParameter("name", iResource.getAbbreviation());
		if (iLocDate != null)
			iHistoryToken.setParameter("date", iLocDate);
		iHistoryToken.setParameter("events", iEvents.getValue().trim());
		iHistoryToken.setParameter("rooms", iRooms.getValue().trim());
		if (iLocRoom != null)
			iHistoryToken.setParameter("room", iLocRoom);
		iHistoryToken.setParameter("tab", String.valueOf(getSelectedTab()));
		if (iEventDetail.equals(iRootPanel.getWidget()))
			iHistoryToken.setParameter("event", iEventDetail.getEvent().getId());
		else if (iEventAdd.equals(iRootPanel.getWidget())) {
			Long id = iEventAdd.getEventId();
			iHistoryToken.setParameter("event", id == null ? "add" : id.toString());
		}
		if (iTable.hasSortBy())
			iHistoryToken.setParameter("sort", iTable.getSortBy());
		iHistoryToken.mark();
		Client.fireGwtPageChanged(new Client.GwtPageChangeEvent());
	}
	
	private class RoomSelector extends IntervalSelector<ResourceInterface> {
		public RoomSelector() {
			super(true);
			addValueChangeHandler(new ValueChangeHandler<Interval>() {
				@Override
				public void onValueChange(ValueChangeEvent<Interval> event) {
					setAriaLabel(ARIA.roomSelection(toAriaString()));
				}
			});
			
			addFocusHandler(new FocusHandler() {
				@Override
				public void onFocus(FocusEvent event) {
					setAriaLabel(ARIA.roomSelection(toAriaString()));
				}
			});
		}
		
		@Override
		public Interval parse(String query) {
			if (query == null || query.isEmpty() || getValues() == null) return new Interval();
			
			Interval ret = null;
			for (int i = 0; i < getValues().size(); i++) {
				ResourceInterface first = getValues().get(i); 
				if (query.equalsIgnoreCase(first.getName()))
					return new Interval(first);
				for (int j = i + 1; j < getValues().size(); j++) {
					ResourceInterface last = getValues().get(j);
					if (query.equalsIgnoreCase(first.getName() + "-" + last.getName()) || query.equalsIgnoreCase(first.getName() + " - " + last.getName()))
						return new Interval(first, last);
					if (filter(first) || filter(last)) continue;
					if (query.toLowerCase().startsWith(first.getName().toLowerCase()) && (
						(first.getName() + "-" + last.getName()).toLowerCase().startsWith(query.toLowerCase()) || (first.getName() + " - " + last.getName()).toLowerCase().startsWith(query.toLowerCase())))
						ret = new Interval(first, last);
					if (query.toLowerCase().startsWith(first.getName().toLowerCase()) && last.getName().indexOf(' ') >= 0) {
						String building = last.getName().substring(0, last.getName().lastIndexOf(' '));
						String room = last.getName().substring(last.getName().lastIndexOf(' ') + 1);
						if (first.getName().startsWith(building + " ") && ((first.getName() + "-" + room).toLowerCase().startsWith(query.toLowerCase()) || (first.getName() + " - " + room).toLowerCase().startsWith(query.toLowerCase())))
							ret = new Interval(first, last);
					}
				}
			}
			if (ret != null) return ret;

			ResourceInterface first = null, last = null;
			for (ResourceInterface e: getValues()) {
				if (isFilterEnabled() && filter(e)) continue;
				if (e.getName().equalsIgnoreCase(query)) return new Interval(e);
				if (e.getName().toLowerCase().startsWith(query.toLowerCase())) {
					if (first == null) first = e;
					last = e;
				} else {
					if (first != null) return new Interval(first, last);
				}
			}
			if (first != null) return new Interval(first, last);
			
			return new Interval();
		}
		
		@Override
		protected String getDisplayString(Interval interval) {
			if (interval == null || interval.isAll()) {
				return interval.isEnableFilter() ? MESSAGES.itemAllRoomsWithFilter() : MESSAGES.itemAllRooms();
			}
			if (interval.isOne()) {
				String hint = interval.getFirst().getRoomType() +
					(interval.getFirst().hasSize() ? ", " + MESSAGES.hintRoomCapacity("" + interval.getFirst().getSize()) : "") +
					(interval.getFirst().hasDistance() ? ", " + MESSAGES.hintRoomDistance("" + Math.round(interval.getFirst().getDistance())) : "");
				return interval.getFirst().getName() + " <span class='item-hint'>" + hint + "</span>";
			} else {
				return "&nbsp;&nbsp;&nbsp;" + interval.getFirst().getName() + " - " + interval.getLast().getName();
			}
		}
		
		@Override
		protected String getReplaceString(Interval interval) {
			if (interval == null || interval.isAll())
				return interval.isEnableFilter() ? MESSAGES.itemAllRoomsWithFilter() : MESSAGES.itemAllRooms();
			return interval.isOne() ? interval.getFirst().getName() : interval.getFirst().getName() + " - " + interval.getLast().getName();			

		}
		
		public String getSelection() {
			if (getValue() == null || getValue().isAll()) return "";
			return getValue().isOne() ? getValue().getFirst().getName() : getValue().getFirst().getName() + "-" + getValue().getLast().getName();			
		}
		
		@Override
		public void setValue(Interval value, boolean fireEvents) {
			if (value != null && ((value.getFirst() != null && filter(value.getFirst())) || (value.getLast() != null && filter(value.getLast()))))
				setFilterEnabled(false);
			super.setValue(value, fireEvents);
		}
	}
	
	private void hideResults() {
		for (int i = 1; i < iPanel.getRowCount(); i++)
			iPanel.getRowFormatter().setVisible(i, false);
		// iGridOrTablePanel.setVisible(false);
		// iTabBar.setVisible(false);
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export", false);
		iHeader.setEnabled("operations", false);
	}
	
	private void showResults() {
		for (int i = 1; i < iPanel.getRowCount(); i++)
			iPanel.getRowFormatter().setVisible(i, true);
		iHeader.setEnabled("print", true);
		iHeader.setEnabled("export", iTable.getRowCount() > 1);
		iHeader.setEnabled("operations", getSelectedTab() > 0 && iTable.getRowCount() > 1);
		// iGridOrTablePanel.setVisible(true);
		// iTabBar.setVisible(true);
	}
	
	private boolean isShowingResults() {
		return iPanel.equals(iRootPanel.getWidget()) && iPanel.getRowFormatter().isVisible(1);
	}
	
	private void loadProperties(final AsyncCallback<EventPropertiesRpcResponse> callback) {
		iProperties = null;
		iSessionMonths = null;
		iTable.setShowMainContact(false);
		iFilterHeader.setEnabled("lookup", false);
		iFilterHeader.setEnabled("add", false);
		iFooter.setEnabled("add", false);
		if (iSession.getAcademicSessionId() != null) {
			RPC.execute(EventPropertiesRpcRequest.requestEventProperties(iSession.getAcademicSessionId(), iType.name()), new AsyncCallback<EventPropertiesRpcResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					UniTimeNotifications.error(MESSAGES.failedLoad(iSession.getAcademicSessionName(), caught.getMessage()), caught);
					if (callback != null)
						callback.onFailure(caught);
				}
				@Override
				public void onSuccess(final EventPropertiesRpcResponse result) {
					if (result.isCanSaveFilterDefaults() && "true".equals(iHistoryToken.getParameter("showFilter", "true"))) {
						iHistoryToken.setDefaultParameter("events", result.getFilterDefault("events"));
						iEvents.setDefaultValueProvider(new TakesValue<String>() {
							@Override
							public void setValue(final String value) {
								RPC.execute(new SaveFilterDefaultRpcRequest(iType.name() + ".events", iEvents.getValue()),
										new AsyncCallback<GwtRpcResponse>() {
											@Override
											public void onFailure(Throwable caught) {
												UniTimeNotifications.error(MESSAGES.failedSaveAsDefault(caught.getMessage()), caught);
											}
											@Override
											public void onSuccess(GwtRpcResponse result) {
												iHistoryToken.setDefaultParameter("events", value);
											}
										});					
							}

							@Override
							public String getValue() {
								return iHistoryToken.getDefaultParameter("events", "");
							}
						});
						iHistoryToken.setDefaultParameter("rooms", result.getFilterDefault("rooms"));
						iRooms.setDefaultValueProvider(new TakesValue<String>() {
							@Override
							public void setValue(final String value) {
								RPC.execute(new SaveFilterDefaultRpcRequest(iType.name() + ".rooms", iRooms.getValue()),
										new AsyncCallback<GwtRpcResponse>() {
											@Override
											public void onFailure(Throwable caught) {
												UniTimeNotifications.error(MESSAGES.failedSaveAsDefault(caught.getMessage()), caught);
											}
											@Override
											public void onSuccess(GwtRpcResponse result) {
												iHistoryToken.setDefaultParameter("rooms", value);
											}
										});					
							}

							@Override
							public String getValue() {
								return iHistoryToken.getDefaultParameter("rooms", "");
							}
						});
					}
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
							iEvents.setOtherVisible(result.isCanLookupMainContact() || result.isCanLookupPeople());
							iFilterHeader.setEnabled("lookup", result.isCanLookupPeople() && getResourceType() == ResourceType.PERSON);
							iFilterHeader.setEnabled("add", result.isCanAddEvent() && "true".equals(iHistoryToken.getParameter("addEvent", "true")));
							iFooter.setEnabled("add", result.isCanAddEvent() && "true".equals(iHistoryToken.getParameter("addEvent", "true")));
							iEventAdd.setup(result);
							iTable.setShowMainContact(result.isCanLookupMainContact());
							iApproveDialog.reset(result);
							if (callback != null)
								callback.onSuccess(result);
						}
					});
				}
			});
			iLookup.setOptions("mustHaveExternalId,session=" + iSession.getAcademicSessionId());

		} else {
			iLookup.setOptions("mustHaveExternalId");
		}
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
		return (iTimeGrid == null ? null : iTimeGrid.getSelections());
	}

	@Override
	public String getRoomFilter() {
		return iRooms.getValue();
	}
	
	@Override
	public List<Date> getSelectedDates() {
		return null;
	}

	@Override
	public StartEndTime getSelectedTime() {
		return null;
	}

	@Override
	public ContactInterface getMainContact() {
		return iLookup.getValue() != null ? new EventInterface.ContactInterface(iLookup.getValue()) : iProperties == null ? null : iProperties.getMainContact();
	}
	
	@Override
	public boolean isTooEarly(int startSlot, int endSlot) {
		if (iProperties == null || !iProperties.hasTooEarlySlot()) return false;
		return (startSlot > 0 && startSlot <= iProperties.getTooEarlySlot()) || (startSlot == 0 && endSlot <= iProperties.getTooEarlySlot());
	}
	
	public static class HistoryToken {
		private String iType = null;
		private Map<String, String> iParams = new HashMap<String, String>();
		private Map<String, String> iDefaults = new HashMap<String, String>();
		
		public HistoryToken(PageType type) {
			iType = type.name();
			
			// 1. take page type defaults --> DEFAULTS
			if (type.getParams() != null)
				for (int i = 0; 1 + i < type.getParams().length; i += 2)
					iDefaults.put(type.getParams()[i], type.getParams()[i + 1]);

			// 2. take page parameters --> DEFAULTS (on top of the page type defaults)
			for (Map.Entry<String, List<String>> params: Window.Location.getParameterMap().entrySet())
				iDefaults.put(params.getKey(), params.getValue().get(0));
			
			// 3. take cookie --> PARAMS (override defaults)
			String cookie = EventCookie.getInstance().getHash(iType);
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
			EventCookie.getInstance().setHash(iType, toString("event"));
		}
	}
	
	public String getPageName() {
		String pageName = iHistoryToken.getParameter("title", "Events");
		ResourceType resource = getResourceType();
		if (!"true".equals(iHistoryToken.getParameter("fixedTitle")) && resource != null)
			pageName = resource.getPageTitle(CONSTANTS);
		return pageName;
	}

	@Override
	public boolean accept(AcademicSession session) {
		String filter = iHistoryToken.getParameter("filter");
		if ("classes".equals(filter))
			return session.has(AcademicSession.Flag.HasClasses);
		if ("exams".equals(filter))
			return session.has(AcademicSession.Flag.HasFinalExams) || session.has(AcademicSession.Flag.HasMidtermExams);
		if ("events".equals(filter))
			return session.has(AcademicSession.Flag.HasEvents);
		if ("person".equals(filter))
			return session.has(AcademicSession.Flag.HasClasses) || session.has(AcademicSession.Flag.HasFinalExams) || session.has(AcademicSession.Flag.HasMidtermExams);
		return true;
	}

	@Override
	public SessionMonth.Flag getDateFlag(EventType type, Date date) {
		if (iSessionMonths == null || iSessionMonths.isEmpty()) return null;
		if (date == null) return null;
		int m = Integer.parseInt(ServerDateTimeFormat.getFormat("MM").format(date));
		for (SessionMonth month: iSessionMonths)
			if (m == month.getMonth() + 1) {
				int d = Integer.parseInt(ServerDateTimeFormat.getFormat("dd").format(date)) - 1;
				if (month.hasFlag(d, SessionMonth.Flag.FINALS) && type != EventType.FinalExam) return SessionMonth.Flag.FINALS;
				if (month.hasFlag(d, SessionMonth.Flag.BREAK)) return SessionMonth.Flag.BREAK;
				if (month.hasFlag(d, SessionMonth.Flag.WEEKEND)) return SessionMonth.Flag.WEEKEND;
				if (month.hasFlag(d, SessionMonth.Flag.HOLIDAY)) return SessionMonth.Flag.HOLIDAY;
				return null;
			}
		return null;
	}
}
