/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.events.TimeGrid.MeetingClickEvent;
import org.unitime.timetable.gwt.client.events.TimeGrid.MeetingClickHandler;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.IntervalSelector;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.WeekSelector;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeHandler;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventDetailRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TabBar;

/**
 * @author Tomas Muller
 */
public class EventResourceTimetable extends Composite implements EventTable.MeetingFilter {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	
	private SimplePanel iRootPanel;
	
	private EventDetail iEventDetail;
	private EventAdd iEventAdd;
	
	private SimpleForm iPanel, iFilter;
	private DockPanel iDockPanel;
	private SimplePanel iGridOrTablePanel;
	private UniTimeHeaderPanel iHeader, iFooter, iFilterHeader;
	private TimeGrid iTimeGrid;
	private EventTable iTable;
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
	private IntervalSelector<String> iViewMode;
	private TabBar iTabBar;
	
	private EventPropertiesRpcResponse iProperties = null;
	
	public EventResourceTimetable(String type) {
		iLocDate = Window.Location.getParameter("date");
		iLocRoom = Window.Location.getParameter("room");
		
		iFilter = new SimpleForm(2);
		iFilter.removeStyleName("unitime-NotPrintableBottomLine");
		iFilter.getColumnFormatter().setWidth(0, "120px");
		
		iFilterHeader = new UniTimeHeaderPanel(MESSAGES.sectFilter());
		iFilterHeader.addButton("search", MESSAGES.buttonSearch(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				resourceTypeChanged(true);
			}
		});
		iLookup = new Lookup();
		iLookup.setOptions("mustHaveExternalId");
		iLookup.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				if (event.getValue() != null) {
					iResources.setText(event.getValue().getId());
					resourceTypeChanged(false);
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
				iEventAdd.reset(iRooms.getValue(),
						(iTimeGrid == null ? null : iTimeGrid.getSelections()),
						iLookup.getValue() != null ? new EventInterface.ContactInterface(iLookup.getValue()) :
						iProperties == null ? null : iProperties.getMainContact(),
						(iProperties == null ? false : iProperties.isCanLookupPeople()),
						(iProperties == null ? false : iProperties.isCanAddCourseEvent()));
				iEventAdd.show();
			}
		});
		iFilterHeader.setEnabled("add", false);
		iFilter.addHeaderRow(iFilterHeader);
		
		iSession = new AcademicSessionSelectionBox() {
			@Override
			protected void onInitializationSuccess(List<AcademicSession> sessions) {
				iFilter.setVisible(sessions != null && !sessions.isEmpty());
				if (iSession.getAcademicSessionId() != null)
					loadProperties();
			}
			
			@Override
			protected void onInitializationFailure(Throwable caught) {
				iFilterHeader.setErrorMessage(caught.getMessage());
			}
		};
		iFilter.addRow(MESSAGES.propAcademicSession(), iSession);

		iEvents = new EventFilterBox(iSession) {
			@Override
			protected void initAsync() {
				super.initAsync();
				resourceTypeChanged(true);
			}
		};
		
		iFilter.addRow(MESSAGES.propEventFilter(), iEvents);
		if (Window.Location.getParameter("events") != null)
			iEvents.setValue(Window.Location.getParameter("events"));
		
		iRooms = new RoomFilterBox(iSession) {
			@Override
			protected void initAsync() {
				super.initAsync();
				resourceTypeChanged(true);
			}
		};
		iFilter.addRow(MESSAGES.propRoomFilter(), iRooms);
		if (Window.Location.getParameter("rooms") != null)
			iRooms.setValue(Window.Location.getParameter("rooms"));
		
		iResourceTypes = new ListBox();
		for (ResourceType resource: ResourceType.values()) {
			if (resource.isVisible())
				iResourceTypes.addItem(resource.getPageTitle(), resource.toString());
		}
		
		iResourceTypes.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iResources.setText("");
				UniTimePageLabel.getInstance().setPageName(getResourceType().getPageTitle());
				hideResults();
				resourceTypeChanged(false);
			}
		});
		iFilter.addRow(MESSAGES.propResourceType(), iResourceTypes);
		if (type != null)
			iFilter.getRowFormatter().setVisible(iFilter.getRowCount() - 1, false);
		
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
		iResources.getTextBox().addFocusHandler(new FocusHandler() {
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
		String typeString = (type != null ? type : Window.Location.getParameter("type"));
		if (typeString != null)
			for (int idx = 0; idx < iResourceTypes.getItemCount(); idx ++) {
				if (iResourceTypes.getValue(idx).equals(typeString.toUpperCase())) {
					iResourceTypes.setSelectedIndex(idx);
				}
			}
		String name = Window.Location.getParameter("name");
		if (name != null) {
			iResources.setText(name);
		}
		UniTimePageLabel.getInstance().setPageName(getResourceType().getPageTitle());
		
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		iPanel.addRow(iFilter);
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		iWeekPanel = new WeekSelector(iSession);
		iWeekPanel.addValueChangeHandler(new ValueChangeHandler<WeekSelector.Interval>() {
			@Override
			public void onValueChange(ValueChangeEvent<WeekSelector.Interval> e) {
				iTable.populateTable(iData, EventResourceTimetable.this, iProperties != null && iProperties.isCanLookupPeople());
				populateGrid();
				changeUrl();
			}
		});
		iRoomPanel = new RoomSelector();
		
		iTabBar = new TabBar();
		iTabBar.addTab(MESSAGES.tabGrid(), true);
		iTabBar.addTab(MESSAGES.tabTable(), true);
		iTabBar.selectTab("table".equalsIgnoreCase(Window.Location.getParameter("tab")) ? 1 : 0);
		iTabBar.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				iGridOrTablePanel.setWidget(iTabBar.getSelectedTab() == 0 ? iTimeGrid : iTable);
				iHeader.setHeaderTitle(name(event.getSelectedItem() == 0));
				changeUrl();
			}
		});
		
		iGridOrTablePanel = new SimplePanel();
		iGridOrTablePanel.setStyleName("unitime-TabPanel");
		
		final Character gridAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabGrid());
		final Character tableAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.tabTable());
		if (gridAccessKey != null || tableAccessKey != null) {
			RootPanel.get().addDomHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					if (!iRootPanel.getWidget().equals(iPanel)) return;
					if (gridAccessKey != null && event.getNativeEvent().getCtrlKey() && (
							event.getNativeKeyCode() == gridAccessKey || event.getNativeKeyCode() == Character.toUpperCase(gridAccessKey))) {
						iTabBar.selectTab(0);
						if (iTable != null) iTable.clearHover();
						event.preventDefault();
					}
					if (tableAccessKey != null && event.getNativeEvent().getCtrlKey() && (
							event.getNativeKeyCode() == tableAccessKey || event.getNativeKeyCode() == Character.toUpperCase(tableAccessKey))) {
						iTabBar.selectTab(1);
						if (iTimeGrid != null) iTimeGrid.hideSelectionPopup();
						event.preventDefault();
					}
				}
			}, KeyUpEvent.getType());
		}
		
		iDockPanel = new DockPanel();
		iDockPanel.setSpacing(0);
		iDockPanel.add(iGridOrTablePanel, DockPanel.SOUTH);
		iDockPanel.add(iRoomPanel, DockPanel.WEST);
		iDockPanel.setCellHorizontalAlignment(iRoomPanel, HasHorizontalAlignment.ALIGN_LEFT);
		iDockPanel.add(iTabBar, DockPanel.CENTER);
		iDockPanel.setCellVerticalAlignment(iTabBar, HasVerticalAlignment.ALIGN_BOTTOM);
		iDockPanel.setCellHorizontalAlignment(iTabBar, HasHorizontalAlignment.ALIGN_CENTER);
		iDockPanel.add(iWeekPanel, DockPanel.EAST);
		iDockPanel.setCellHorizontalAlignment(iWeekPanel, HasHorizontalAlignment.ALIGN_RIGHT);

		iPanel.addRow(iDockPanel);
		
		hideResults();
		
		iFooter = iHeader.clonePanel();
		iPanel.addRow(iFooter);
		iRootPanel = new SimplePanel(iPanel);
		initWidget(iRootPanel);
		
		iRoomPanel.addValueChangeHandler(new ValueChangeHandler<IntervalSelector<ResourceInterface>.Interval>() {
			@Override
			public void onValueChange(ValueChangeEvent<IntervalSelector<ResourceInterface>.Interval> e) {
				iTable.populateTable(iData, EventResourceTimetable.this, iProperties != null && iProperties.isCanLookupPeople());
				populateGrid();
				changeUrl();
			}
		});

		for (int i = 1; i < iPanel.getRowCount(); i++)
			iPanel.getRowFormatter().setVisible(i, false);
					
		iHeader.addButton("print", MESSAGES.buttonPrint(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				
				EventTable table  = new EventTable();
				table.populateTable(iData, EventResourceTimetable.this, iProperties != null && iProperties.isCanLookupPeople());
				
				TimeGrid tg = iTimeGrid.getPrintWidget();
				for (EventInterface event: iData) {
					List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
					for (MeetingInterface meeting: event.getMeetings())
						if (!filter(meeting))
							meetings.add(meeting);
					if (!meetings.isEmpty())
						tg.addPrintEvent(event, meetings);
				}
				if (iWeekPanel.getValue() != null)
					tg.labelDays(iWeekPanel.getValue().getFirst(), iWeekPanel.getValue().getLast());
				ToolBox.print(iHeader.getHeaderTitle(),
						"", "", 
						tg,
						table
						);
			}
		});
		iHeader.addButton("export", MESSAGES.buttonExportICal(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				if (iTimeGrid.getCalendarUrl() != null)
					ToolBox.open(iTimeGrid.getCalendarUrl());
			}
		});
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export", false);
		
		iSession.addAcademicSessionChangeHandler(new AcademicSessionChangeHandler() {
			@Override
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				loadProperties();
			}
		});
		
		iEventDetail = new EventDetail() {
			@Override
			protected void onHide() {
				iRootPanel.setWidget(iPanel);
				UniTimePageLabel.getInstance().setPageName(getResourceType().getPageTitle());
			}
			@Override
			protected void onShow() {
				if (iTable != null) iTable.clearHover();
				iRootPanel.setWidget(iEventDetail);
			}
		};
		
		iEventAdd = new EventAdd(iSession) {
			@Override
			protected void onHide() {
				iRootPanel.setWidget(iPanel);
				UniTimePageLabel.getInstance().setPageName(getResourceType().getPageTitle());
			}
			@Override
			protected void onShow() {
				if (iTable != null) iTable.clearHover();
				iRootPanel.setWidget(iEventAdd);
			}
		};
		
		iMeetingClickHandler = new MeetingClickHandler() {
			@Override
			public void onMeetingClick(final MeetingClickEvent event) {
				if (!event.getEvent().isCanView()) return;
				iFilterHeader.clearMessage();
				LoadingWidget.execute(EventDetailRpcRequest.requestEventDetails(iSession.getAcademicSessionId(), event.getEvent().getId()), new AsyncCallback<EventInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						iFilterHeader.setErrorMessage(MESSAGES.failedLoad(event.getEvent().getName(), caught.getMessage()));
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
		
		iTable = new EventTable();
		iTable.addMouseClickListener(new MouseClickListener<EventInterface[]>() {
			@Override
			public void onMouseClick(final TableEvent<EventInterface[]> event) {
				EventInterface e = event.getData()[event.getData().length - 1];
				if (!e.isCanView()) return;
				iFilterHeader.clearMessage();
				LoadingWidget.execute(EventDetailRpcRequest.requestEventDetails(iSession.getAcademicSessionId(), e.getId()), new AsyncCallback<EventInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						iFilterHeader.setErrorMessage(MESSAGES.failedLoad(event.getData()[0].getName(), caught.getMessage()));
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
	
	private void resourceTypeChanged(boolean allowEmptyResource) {
		if (iProperties == null || !iRooms.isInitialized() || !iEvents.isInitialized()) return;
		ResourceType type = getResourceType();
		if (type != null) {
			if (type == ResourceType.ROOM) {
				iFilter.getRowFormatter().setVisible(iResourcesRow, false);
				iFilterHeader.setEnabled("lookup", false);
				if (iSession.getAcademicSessionId() != null && (!iRooms.getValue().isEmpty() || !iEvents.getValue().isEmpty())) {
					ResourceInterface resource = new ResourceInterface();
					resource.setType(ResourceType.ROOM);
					resource.setName(iRooms.getValue());
					if (allowEmptyResource)
						resourceChanged(resource);
				}
			} else {
				iFilter.getRowFormatter().setVisible(iResourcesRow, type != ResourceType.PERSON);
				((Label)iFilter.getWidget(iResourcesRow, 0)).setText(type.getLabel().substring(0,1).toUpperCase() + type.getLabel().substring(1) + ":");
				iFilterHeader.setEnabled("lookup", iProperties != null && iProperties.isCanLookupPeople() && getResourceType() == ResourceType.PERSON);
				if (iSession.getAcademicSessionId() != null && ((type == ResourceType.PERSON && allowEmptyResource) || getResourceName() != null)) {
					iFilterHeader.clearMessage();
					LoadingWidget.execute(ResourceLookupRpcRequest.findResource(iSession.getAcademicSessionId(), type, getResourceName()),
							new AsyncCallback<GwtRpcResponseList<ResourceInterface>>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iFilterHeader.setErrorMessage(caught.getMessage());
							hideResults();
						}
						@Override
						public void onSuccess(GwtRpcResponseList<ResourceInterface> result) {
							LoadingWidget.getInstance().hide();
							resourceChanged(result.get(0));
						}
					}, "Loading " + type.getLabel() + (type != ResourceType.PERSON ? " " + getResourceName() : "") + " ...");
					/*
					LoadingWidget.getInstance().show("Loading " + type.getLabel() + (type != ResourceType.PERSON ? " " + getResourceName() : "") + " ...");
					RPC.execute(ResourceLookupRpcRequest.findResource(iSession.getAcademicSessionId(), type, getResourceName()),
							new AsyncCallback<GwtRpcResponseList<ResourceInterface>>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iFilterHeader.setErrorMessage(caught.getMessage());
							hideResults();
						}
						@Override
						public void onSuccess(GwtRpcResponseList<ResourceInterface> result) {
							LoadingWidget.getInstance().hide();
							resourceChanged(result.get(0));
						}
					});
					*/
				}				
			}
		}
	}
	
	private void resourceChanged(ResourceInterface resource) {
		final String locDate = iLocDate;
		final String locRoom = iLocRoom;
		iResource = resource;
		iFilterHeader.clearMessage();
		LoadingWidget.execute(EventLookupRpcRequest.findEvents(iSession.getAcademicSessionId(), iResource, iEvents.getElementsRequest(), iRooms.getElementsRequest(), CONSTANTS.maxMeetings()), 
				new AsyncCallback<GwtRpcResponseList<EventInterface>>() {
			@Override
			public void onSuccess(GwtRpcResponseList<EventInterface> result) {
				iData = result;
				LoadingWidget.getInstance().hide();
				changeUrl();
				if (iData.isEmpty()) {
					iFilterHeader.setErrorMessage(MESSAGES.failedNoEvents((iResource.getType() == ResourceType.PERSON ? "" : iResource.getType().getLabel() + " ") + iResource.getName(), iSession.getAcademicSessionName()));
					hideResults();
				} else {
					Collections.sort(iData);
					
					TreeSet<ResourceInterface> rooms = new TreeSet<ResourceInterface>();
					int nrMeetings = 0;
					for (EventInterface event: iData) {
						for (MeetingInterface meeting: event.getMeetings()) {
							if (meeting.getLocation() != null) rooms.add(meeting.getLocation());
						}
						nrMeetings += event.getMeetings().size();
					}
					iRoomPanel.setValues(new ArrayList<ResourceInterface>(rooms));
					iRoomPanel.setValue(iRoomPanel.parse(locRoom));

					if (iWeekPanel.getAllWeeks() != null) {
						List<WeekInterface> weeks = new ArrayList<WeekInterface>();
						for (WeekInterface week: iWeekPanel.getAllWeeks()) {
							boolean hasEvents = false;
							events: for (EventInterface event: iData) {
								for (MeetingInterface meeting: event.getMeetings()) {
									if (meeting.getDayOfYear() >= week.getDayOfYear() && meeting.getDayOfYear() < week.getDayOfYear() + 7) {
										hasEvents = true;
										break events;
									}
								}
							}
							if (hasEvents) weeks.add(week);
						}						
						iWeekPanel.setValues(weeks);
					}
					iWeekPanel.setValue(iWeekPanel.parse(locDate));

					if (nrMeetings > CONSTANTS.maxMeetings())
						iHeader.setErrorMessage(MESSAGES.warnTooManyMeetings(CONSTANTS.maxMeetings()));
					else
						iHeader.clearMessage();
					
					int nrDays = 4;
					int firstSlot = -1, lastSlot = -1;
					for (EventInterface event: iData) {
						for (MeetingInterface meeting: event.getMeetings()) {
							if (firstSlot < 0 || firstSlot > meeting.getStartSlot()) firstSlot = meeting.getStartSlot();
							if (lastSlot < 0 || lastSlot < meeting.getEndSlot()) lastSlot = meeting.getEndSlot();
							nrDays = Math.max(nrDays, meeting.getDayOfWeek());
						}
					}
					nrDays ++;
					int days[] = new int[nrDays];
					for (int i = 0; i < days.length; i++) days[i] = i;
					int firstHour = firstSlot / 12;
					int lastHour = 1 + (lastSlot - 1) / 12;
					if (firstHour <= 7 && firstHour > 0 && ((firstSlot % 12) <= 6)) firstHour--;
					HashMap<Long, String> colors = new HashMap<Long, String>();
					
					if (iTimeGrid != null) iTimeGrid.destroy();
					iTimeGrid = new TimeGrid(colors, days, (int)(0.9 * Window.getClientWidth() / nrDays), false, false, (firstHour < 7 ? firstHour : 7), (lastHour > 18 ? lastHour : 18));
					iTimeGrid.addMeetingClickHandler(iMeetingClickHandler);
					populateGrid();
					iTable.populateTable(iData, EventResourceTimetable.this, iProperties != null && iProperties.isCanLookupPeople());
					iGridOrTablePanel.setWidget(iTabBar.getSelectedTab() == 0 ? iTimeGrid : iTable);
										
					showResults();
				}
			}
	
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilterHeader.setErrorMessage(caught.getMessage());
				hideResults();
			}
		}, MESSAGES.waitLoadingTimetable(resource.getType() == ResourceType.ROOM ? MESSAGES.resourceRoom().toLowerCase() : resource.getName(), iSession.getAcademicSessionName()));
	}
	
	private void populateGrid() {
		iTimeGrid.clear();
		iHeader.setHeaderTitle(name(iTabBar.getSelectedTab() == 0));
		// iTableHeader.setHeaderTitle(name(false));
		iTimeGrid.setResourceType(getResourceType());
		iTimeGrid.setSelectedWeeks(iWeekPanel.getSelected());
		iTimeGrid.setRoomResources(iRoomPanel.getSelected());
		iTimeGrid.setMode(gridMode());
		for (EventInterface event: sortedEvents()) {
			List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
			for (MeetingInterface meeting: event.getMeetings()) {
				if (!filter(meeting))
					meetings.add(meeting);
			}
			if (!meetings.isEmpty())
				iTimeGrid.addEvent(event, meetings);
		}
		iTimeGrid.shrink();
		if (iWeekPanel.getValue() != null)
			iTimeGrid.labelDays(iWeekPanel.getValue().getFirst(), iWeekPanel.getValue().getLast());
	}
	
	private String name(boolean timetable) {
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
		return (timetable ? MESSAGES.sectTimetable(resource, session) : MESSAGES.sectEvents(resource, session));
	}
	
	@Override
	public boolean filter(MeetingInterface meeting) {
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
		if ("0".equals(Window.Location.getParameter("eq"))) return TimeGrid.Mode.FILLSPACE;
		switch (iResource.getType()) {
		case ROOM:
			return (iRoomPanel.isOne() || iWeekPanel.isOne() ? TimeGrid.Mode.OVERLAP : TimeGrid.Mode.PROPORTIONAL);
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
	
	protected void changeUrl() {
		FilterRpcRequest events = iEvents.getElementsRequest();
		FilterRpcRequest rooms = iRooms.getElementsRequest();
		if (iWeekPanel.getValue() != null && !iWeekPanel.getValue().isAll()) {
			events.setOption("from", String.valueOf(iWeekPanel.getValue().getFirst().getDayOfYear()));
			events.setOption("to", String.valueOf((iWeekPanel.getValue().isOne() ? iWeekPanel.getValue().getFirst() : iWeekPanel.getValue().getLast()).getDayOfYear() + 6));
		}
		if (iRoomPanel.getValue() != null && !iRoomPanel.getValue().isAll()) {
			for (ResourceInterface resource: iRoomPanel.getSelected())
				events.addOption("room", resource.getId().toString());
		}
		String query = "sid=" + iSession.getAcademicSessionId() +
			(iResource == null || iResource.getType() == null ? "" : "&type=" + iResource.getType().toString().toLowerCase()) +
			(iResource == null || iResource.getId() == null ? "" : "&id=" + iResource.getId()) +
			(iResource == null || iResource.getExternalId() == null ? "" : "&ext=" + iResource.getExternalId());
		
		if (events.getOptions() != null) {
			for (Map.Entry<String, Set<String>> option: events.getOptions().entrySet()) {
				for (String value: option.getValue()) {
					query += "&e:" + option.getKey() + "=" + URL.encodeQueryString(value);
				}
			}
		}
		if (events.getText() != null && !events.getText().isEmpty()) {
			query += "&e:text=" + URL.encodeQueryString(events.getText());
		}
		if (rooms.getOptions() != null && (iRoomPanel.getValue() == null || iRoomPanel.getValue().isAll())) {
			for (Map.Entry<String, Set<String>> option: rooms.getOptions().entrySet()) {
				for (String value: option.getValue()) {
					query += "&r:" + option.getKey() + "=" + URL.encodeQueryString(value);
				}
			}
			if (rooms.getText() != null && !rooms.getText().isEmpty()) {
				query += "&r:text=" + URL.encodeQueryString(rooms.getText());
			}
		}
		if (iTimeGrid != null)
			RPC.execute(EncodeQueryRpcRequest.encode(query), new AsyncCallback<EncodeQueryRpcResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					iHeader.setEnabled("export", false);
					iTimeGrid.setCalendarUrl(null);
				}
				@Override
				public void onSuccess(EncodeQueryRpcResponse result) {
					iTimeGrid.setCalendarUrl(GWT.getHostPageBaseURL() + "calendar?q=" + result.getQuery());
					iHeader.setEnabled("export", true);
				}
			});
		
		iLocDate = iWeekPanel.getSelection();
		iLocRoom = iRoomPanel.getSelection();
		changeUrl(Window.Location.getParameter("page"),
				"term=" + URL.encodeQueryString(iSession.getAcademicSessionAbbreviation()) +
				"&type=" + iResourceTypes.getValue(iResourceTypes.getSelectedIndex()).toLowerCase() +
				(iResource == null || iResource.getAbbreviation() == null || iResource.getType() == ResourceType.PERSON ? "" : "&name=" + URL.encodeQueryString(iResource.getAbbreviation())) +
				(iLocDate.isEmpty() ? "" : "&date=" + URL.encodeQueryString(iLocDate)) +
				(iEvents.getValue().isEmpty() ? "" : "&events=" + URL.encodeQueryString(iEvents.getValue().trim())) +
				(iRooms.getValue().isEmpty() ? "" : "&rooms=" + URL.encodeQueryString(iRooms.getValue().trim())) +
				(iLocRoom.isEmpty() ? "" : "&room=" + URL.encodeQueryString(iLocRoom)) +
				"&tab=" + (iTabBar.getSelectedTab() == 0 ? "grid" : "table"));
	}
	
	private native static void changeUrl(String page, String query) /*-{
		try {
			$wnd.history.pushState(query, "", "gwt.jsp?page=" + page + "&" + query);
		} catch (err) {
		}
	}-*/;
	
	private class RoomSelector extends IntervalSelector<ResourceInterface> {
		public RoomSelector() {
			super(true);
		}
		
		@Override
		public Interval parse(String query) {
			if (query == null || getValues() == null) return new Interval();

			ResourceInterface first = null, last = null;
			for (ResourceInterface e: getValues()) {
				if (e.getName().equalsIgnoreCase(query)) return new Interval(e);
				if (e.getName().toLowerCase().startsWith(query.toLowerCase())) {
					if (first == null) first = e;
					last = e;
				} else {
					if (first != null) return new Interval(first, last);
				}
			}
			
			Interval ret = super.parse(query);
			return (ret == null ? new Interval() : ret);
		}
		
		@Override
		protected String getDisplayString(Interval interval) {
			if (interval == null || interval.isAll()) return MESSAGES.allRooms();
			if (interval.isOne()) {
				String hint = interval.getFirst().getRoomType() +
					(interval.getFirst().hasSize() ? ", " + interval.getFirst().getSize() + " seats" : "") +
					(interval.getFirst().hasDistance() ? ", " + Math.round(interval.getFirst().getDistance()) + " m" : "");
				return interval.getFirst().getName() + " <span class='item-hint'>" + hint + "</span>";
			} else {
				return "&nbsp;&nbsp;&nbsp;" + interval.getFirst().getName() + " - " + interval.getLast().getName();
			}
		}
		
		@Override
		protected String getReplaceString(Interval interval) {
			if (interval == null || interval.isAll()) return MESSAGES.allRooms();
			return interval.isOne() ? interval.getFirst().getName() : interval.getFirst().getName() + " - " + interval.getLast().getName();			

		}
		
		public String getSelection() {
			if (getValue() == null || getValue().isAll()) return "";
			return getValue().isOne() ? getValue().getFirst().getName() : getValue().getFirst().getName() + "-" + getValue().getLast().getName();			
		}
	}
	
	private void hideResults() {
		for (int i = 1; i < iPanel.getRowCount(); i++)
			iPanel.getRowFormatter().setVisible(i, false);
		// iGridOrTablePanel.setVisible(false);
		// iTabBar.setVisible(false);
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export", false);
	}
	
	private void showResults() {
		for (int i = 1; i < iPanel.getRowCount(); i++)
			iPanel.getRowFormatter().setVisible(i, true);
		iHeader.setEnabled("print", true);
		// iGridOrTablePanel.setVisible(true);
		// iTabBar.setVisible(true);
	}
	
	private void loadProperties() {
		iProperties = null;
		iFilterHeader.setEnabled("lookup", false);
		iFilterHeader.setEnabled("add", false);
		if (iSession.getAcademicSessionId() != null) {
			RPC.execute(EventPropertiesRpcRequest.requestEventProperties(iSession.getAcademicSessionId()), new AsyncCallback<EventPropertiesRpcResponse>() {
				@Override
				public void onFailure(Throwable caught) {
				}
				@Override
				public void onSuccess(EventPropertiesRpcResponse result) {
					iProperties = result;
					iFilterHeader.setEnabled("lookup", result.isCanLookupPeople() && getResourceType() == ResourceType.PERSON);
					iFilterHeader.setEnabled("add", result.isCanAddEvent());
					iEventAdd.setup(result);
					resourceTypeChanged(true);
				}
			});
			iLookup.setOptions("mustHaveExternalId,session=" + iSession.getAcademicSessionId());
		} else {
			iLookup.setOptions("mustHaveExternalId");
		}
	}
}
