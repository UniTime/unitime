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
import java.util.Date;
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
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasColSpan;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.client.widgets.WeekSelector;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventDetailRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MultiMeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author Tomas Muller
 */
public class EventResourceTimetable extends Composite {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	
	private SimpleForm iPanel, iFilter;
	private SimplePanel iGridPanel, iTablePanel;
	private UniTimeHeaderPanel iHeader, iTableHeader, iFooter, iFilterHeader;
	private TimeGrid iTimeGrid;
	private UniTimeTable<EventInterface> iTable;
	private ResourceInterface iResource;
	private List<EventInterface> iData;
	private WeekSelector iWeekPanel;
	private RoomSelector iRoomPanel;
	private AcademicSessionSelectionBox iSession;
	private ListBox iResourceTypes;
	private SuggestBox iResources;
	private boolean iCanLookupPeople = false;
	private int iResourcesRow = -1, iWeekRow = -1, iLastRow = -1;
	private EventFilterBox iEvents = null;
	private RoomFilterBox iRooms = null;
	private String iLocDate = null, iLocRoom = null;
	private MeetingClickHandler iMeetingClickHandler;
	
	private static EventResourceTimetable sInstance = null;
	
	public EventResourceTimetable(String type) {
		sInstance = this; 
		
		iLocDate = Window.Location.getParameter("date");
		iLocRoom = Window.Location.getParameter("room");
		
		iFilter = new SimpleForm(2);
		iFilter.removeStyleName("unitime-NotPrintableBottomLine");
		iFilter.getColumnFormatter().setWidth(0, "120px");
		
		iFilterHeader = new UniTimeHeaderPanel("Filter");
		iFilterHeader.addButton("search", "<u>S</u>earch", 's', 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				resourceTypeChanged(true);
			}
		});
		Lookup.getInstance().setOptions("mustHaveExternalId");
		Lookup.getInstance().setCallback(createLookupCallback());
		iFilterHeader.addButton("lookup", "<u>L</u>ookup", 'p', 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Lookup.getInstance().center();
			}
		});
		iFilterHeader.setEnabled("lookup", false);
		iFilter.addHeaderRow(iFilterHeader);
		
		iSession = new AcademicSessionSelectionBox() {
			@Override
			protected void onInitializationSuccess(List<AcademicSession> sessions) {
				iFilter.setVisible(sessions != null && !sessions.isEmpty());
				if (iSession.getAcademicSessionId() != null)
					Lookup.getInstance().setOptions("mustHaveExternalId,session=" + iSession.getAcademicSessionId());
			}
			
			@Override
			protected void onInitializationFailure(Throwable caught) {
				iFilterHeader.setErrorMessage(caught.getMessage());
			}
		};
		iFilter.addRow("Academic Session:", iSession);

		iEvents = new EventFilterBox(iSession) {
			@Override
			protected void initAsync() {
				super.initAsync();
				resourceTypeChanged(true);
			}
		};
		
		iFilter.addRow("Event Filter:", iEvents);
		if (Window.Location.getParameter("events") != null)
			iEvents.setValue(Window.Location.getParameter("events"));
		
		iRooms = new RoomFilterBox(iSession) {
			@Override
			protected void initAsync() {
				super.initAsync();
				resourceTypeChanged(true);
			}
		};
		iFilter.addRow("Room Filter:", iRooms);
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
				for (int i = 1; i < iPanel.getRowCount(); i++)
					iPanel.getRowFormatter().setVisible(i, false);
				iGridPanel.setVisible(false);
				resourceTypeChanged(false);
			}
		});
		iFilter.addRow("Resource Type:", iResourceTypes);
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
		iResourcesRow = iFilter.addRow("Resource:", iResources);
		iResources.addSelectionHandler(new SelectionHandler<Suggestion>() {
			public void onSelection(SelectionEvent<Suggestion> event) {
				if (event.getSelectedItem() instanceof ResourceSuggestion) {
					resourceChanged(((ResourceSuggestion)event.getSelectedItem()).getResource());
				}
			}
		});

		iResourceTypes.setSelectedIndex(ResourceType.PERSON.ordinal());
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
				populateEventTable(iTable);
				populateGrid();
				changeUrl();
			}
		});
		iRoomPanel = new RoomSelector();
		iWeekRow = iPanel.addRow(iRoomPanel, iWeekPanel);
		iPanel.getCellFormatter().setHorizontalAlignment(iWeekRow, 0,  HasHorizontalAlignment.ALIGN_LEFT);
		iPanel.getCellFormatter().setHorizontalAlignment(iWeekRow, 1,  HasHorizontalAlignment.ALIGN_RIGHT);
		iGridPanel = new SimplePanel();
		iPanel.addRow(iGridPanel);
		iTableHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iTableHeader);
		iTablePanel = new SimplePanel();
		iPanel.addRow(iTablePanel);
		iFooter = iHeader.clonePanel();
		iLastRow = iPanel.addBottomRow(iFooter);
		initWidget(iPanel);
		
		iRoomPanel.addValueChangeHandler(new ValueChangeHandler<IntervalSelector<ResourceInterface>.Interval>() {
			@Override
			public void onValueChange(ValueChangeEvent<IntervalSelector<ResourceInterface>.Interval> e) {
				populateEventTable(iTable);
				populateGrid();
				changeUrl();
			}
		});

		for (int i = 1; i < iPanel.getRowCount(); i++)
			iPanel.getRowFormatter().setVisible(i, false);
		iGridPanel.setVisible(false);
					
		iHeader.addButton("print", "<u>P</u>rint", 'p', 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				
				UniTimeTable<EventInterface> table  = createEventTable();
				populateEventTable(table);
				// table.getElement().getStyle().setProperty("page-break-before", "always");
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
		iHeader.addButton("export", "E<u>x</u>port", 'x', 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				if (iTimeGrid.getCalendarUrl() != null)
					ToolBox.open(iTimeGrid.getCalendarUrl());
			}
		});
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export", false);
		
		RPC.execute(EventPropertiesRpcRequest.requestEventProperties(), new AsyncCallback<EventPropertiesRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(EventPropertiesRpcResponse result) {
				iCanLookupPeople = (result == null ? false: result.isCanLookupPeople());
				iFilterHeader.setEnabled("lookup", iCanLookupPeople && getResourceType() == ResourceType.PERSON);
			}
		});
		
		final EventDetail detail = new EventDetail();
		final ScrollPanel detailScroll = new ScrollPanel(detail);
		detailScroll.setHeight(((int)(0.8 * Window.getClientHeight())) + "px");
		detailScroll.setStyleName("unitime-ScrollPanel");
		final UniTimeDialogBox detailDialog = new UniTimeDialogBox(true, false);
		detailDialog.setEscapeToHide(true);
		detailDialog.setWidget(detailScroll);
		detailDialog.setText("Event Detail");
		detailDialog.addOpenHandler(new OpenHandler<UniTimeDialogBox>() {
			@Override
			public void onOpen(OpenEvent<UniTimeDialogBox> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
				detailScroll.setHeight(Math.min(detail.getElement().getScrollHeight(), Window.getClientHeight() * 80 / 100) + "px");
				detailDialog.setPopupPosition(
						Math.max(Window.getScrollLeft() + (Window.getClientWidth() - detailDialog.getOffsetWidth()) / 2, 0),
						Math.max(Window.getScrollTop() + (Window.getClientHeight() - detailDialog.getOffsetHeight()) / 2, 0));
			}
		});
		detailDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
			}
		});
		
		iMeetingClickHandler = new MeetingClickHandler() {
			@Override
			public void onMeetingClick(final MeetingClickEvent event) {
				LoadingWidget.getInstance().show("Loading " + event.getEvent().getName() + "...");
				RPC.execute(EventDetailRpcRequest.requestEventDetails(iSession.getAcademicSessionId(), event.getEvent().getId()), new AsyncCallback<EventInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().show("Failed to load " + event.getEvent().getName() + ": " + caught.getMessage());
					}

					@Override
					public void onSuccess(EventInterface result) {
						LoadingWidget.getInstance().hide();
						detail.setEvent(result);
						detailDialog.center();
					}
				});
			}
		};
		
		iTable = createEventTable();
		iTable.addMouseClickListener(new MouseClickListener<EventInterface>() {
			@Override
			public void onMouseClick(final TableEvent<EventInterface> event) {
				LoadingWidget.getInstance().show("Loading " + event.getData().getName() + "...");
				RPC.execute(EventDetailRpcRequest.requestEventDetails(iSession.getAcademicSessionId(), event.getData().getId()), new AsyncCallback<EventInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().show("Failed to load " + event.getData().getName() + ": " + caught.getMessage());
					}

					@Override
					public void onSuccess(EventInterface result) {
						LoadingWidget.getInstance().hide();
						detail.setEvent(result);
						detailDialog.center();
					}
				});
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
		if (getResourceType() == ResourceType.PERSON && !iCanLookupPeople) return "";
		return (iResources.getText() == null || iResources.getText().isEmpty() ? null : iResources.getText());
	}
	
	private void resourceTypeChanged(boolean allowEmptyResource) {
		if (iSession.getAcademicSessionId() != null)
			Lookup.getInstance().setOptions("mustHaveExternalId,session=" + iSession.getAcademicSessionId());
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
				iFilterHeader.setEnabled("lookup", iCanLookupPeople && getResourceType() == ResourceType.PERSON);
				if (iSession.getAcademicSessionId() != null && ((type == ResourceType.PERSON && allowEmptyResource) || getResourceName() != null)) {
					iFilterHeader.clearMessage();
					LoadingWidget.getInstance().show("Loading " + type.getLabel() + (type != ResourceType.PERSON ? " " + getResourceName() : "") + " ...");
					RPC.execute(ResourceLookupRpcRequest.findResource(iSession.getAcademicSessionId(), type, getResourceName()),
							new AsyncCallback<GwtRpcResponseList<ResourceInterface>>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iFilterHeader.setErrorMessage(caught.getMessage());
							for (int i = 1; i < iPanel.getRowCount(); i++)
								iPanel.getRowFormatter().setVisible(i, i == iLastRow);
							iGridPanel.setVisible(false);
							iHeader.setEnabled("print", false);
							iHeader.setEnabled("export", false);
						}
						@Override
						public void onSuccess(GwtRpcResponseList<ResourceInterface> result) {
							LoadingWidget.getInstance().hide();
							resourceChanged(result.get(0));
						}
					});
				}				
			}
		}
	}
	
	private void resourceChanged(ResourceInterface resource) {
		final String locDate = iLocDate;
		final String locRoom = iLocRoom;
		LoadingWidget.getInstance().show("Loading " + (resource.getType() == ResourceType.ROOM ? "room" : resource.getName()) + " timetable for " + iSession.getAcademicSessionName() + " ...");
		iResource = resource;
		iFilterHeader.clearMessage();
		RPC.execute(EventLookupRpcRequest.findEvents(iSession.getAcademicSessionId(), iResource, iEvents.getElementsRequest(), iRooms.getElementsRequest(), CONSTANTS.maxMeetings()), 
				new AsyncCallback<GwtRpcResponseList<EventInterface>>() {
			@Override
			public void onSuccess(GwtRpcResponseList<EventInterface> result) {
				iData = result;
				LoadingWidget.getInstance().hide();
				changeUrl();
				if (iData.isEmpty()) {
					iFilterHeader.setErrorMessage("No events found for " + (iResource.getType() == ResourceType.PERSON ? "" : iResource.getType().getLabel() + " ") + iResource.getName() + " in " + iSession.getAcademicSessionName() + "."); 
					for (int i = 1; i < iPanel.getRowCount(); i++)
						iPanel.getRowFormatter().setVisible(i, i == iLastRow);
					iGridPanel.setVisible(false);
					iHeader.setEnabled("print", false);
					iHeader.setEnabled("export", false);
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
						iHeader.setErrorMessage("There are more than " + CONSTANTS.maxMeetings() + " meetings matching the filter. Only " + CONSTANTS.maxMeetings() + " meetings are loaded.");
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
					HashMap<Long, String> colors = new HashMap<Long, String>();
					
					if (iTimeGrid != null) iTimeGrid.destroy();
					iTimeGrid = new TimeGrid(colors, days, (int)(0.9 * Window.getClientWidth() / nrDays), false, false, (firstHour < 7 ? firstHour : 7), (lastHour > 18 ? lastHour : 18));
					iTimeGrid.addMeetingClickHandler(iMeetingClickHandler);
					populateGrid();
					iGridPanel.setWidget(iTimeGrid);
					
					populateEventTable(iTable);
					iTablePanel.setWidget(iTable);
										
					iHeader.setEnabled("print", true);
					for (int i = 1; i < iPanel.getRowCount(); i++)
						iPanel.getRowFormatter().setVisible(i, true);
					iGridPanel.setVisible(true);
				}
			}
	
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilterHeader.setErrorMessage(caught.getMessage());
				for (int i = 1; i < iPanel.getRowCount(); i++)
					iPanel.getRowFormatter().setVisible(i, i == iLastRow);
				iGridPanel.setVisible(false);
				iHeader.setEnabled("print", false);
				iHeader.setEnabled("export", false);
			}
		});
	}
	
	private void populateGrid() {
		iTimeGrid.clear();
		iHeader.setHeaderTitle(name(true));
		iTableHeader.setHeaderTitle(name(false));
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
		String ret;
		if (iResource.getType() == ResourceType.ROOM) {
			if (iRoomPanel.getValue() != null && iRoomPanel.getValue().isOne()) {
				ret = iRoomPanel.getValue().getFirst().getName();
			} else if (iRoomPanel.getValues() != null && iRoomPanel.getValues().size() == 1) {
				ret = iRoomPanel.getValues().get(0).getName();
			} else {
				ret = "Room";
			}
		} else {
			ret = iResource.getNameWithHint();
		}
		ret += (timetable ? " timetable for " : " events for ");
		if (iWeekPanel.getValue() == null || iWeekPanel.getValue().isAll())
			ret += iSession.getAcademicSessionName();
		else
			ret += iWeekPanel.getValue().toString().toLowerCase();
		return ret;
	}
	
	private boolean filter(MeetingInterface meeting) {
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
	
	private UniTimeTable<EventInterface> createEventTable() {
		final UniTimeTable table = new UniTimeTable<EventInterface>();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader("Name"));
		header.add(new UniTimeTableHeader("Section"));
		header.add(new UniTimeTableHeader("Type"));
		header.add(new UniTimeTableHeader("Date"));
		header.add(new UniTimeTableHeader("Time"));
		header.add(new UniTimeTableHeader("Location"));
		header.add(new UniTimeTableHeader("Instructor / Sponsor"));
		header.add(new UniTimeTableHeader("Approval"));
		header.get(0).addOperation(new Operation() {
			@Override
			public void execute() {
				table.sort(new Comparator<EventInterface>() {
					@Override
					public int compare(EventInterface o1, EventInterface o2) {
						int cmp = o1.getName().compareTo(o2.getName());
						if (cmp != 0) return cmp;
						cmp = o1.getType().compareTo(o2.getType());
						if (cmp != 0) return cmp;
						return o1.getId().compareTo(o2.getId());
					}
				});
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return "Sort by Name";
			}
		});
		header.get(1).addOperation(new Operation() {
			@Override
			public void execute() {
				table.sort(new Comparator<EventInterface>() {
					@Override
					public int compare(EventInterface o1, EventInterface o2) {
						if (o1.hasExternalIds()) {
							if (o2.hasExternalIds()) {
								int cmp = o1.getExternalIds().get(0).compareTo(o2.getExternalIds().get(0));
								if (cmp != 0) return cmp;
							} else return -1;
						} else if (o2.hasExternalIds()) return 1;
						int cmp = o1.getType().compareTo(o2.getType());
						if (cmp != 0) return cmp;
						if (o1.getInstructionType() != null) {
							cmp = o1.getInstructionType().compareTo(o2.getInstructionType());
							if (cmp != 0) return cmp;
						}
						cmp = o1.getName().compareTo(o2.getName());
						if (cmp != 0) return cmp;
						return o1.getId().compareTo(o2.getId());
					}
				});
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return "Sort by Section";
			}
		});
		header.get(2).addOperation(new Operation() {
			@Override
			public void execute() {
				table.sort(new Comparator<EventInterface>() {
					@Override
					public int compare(EventInterface o1, EventInterface o2) {
						int cmp = o1.getType().compareTo(o2.getType());
						if (cmp != 0) return cmp;
						if (o1.getInstructionType() != null) {
							cmp = o1.getInstructionType().compareTo(o2.getInstructionType());
							if (cmp != 0) return cmp;
						}
						cmp = o1.getName().compareTo(o2.getName());
						if (cmp != 0) return cmp;
						return o1.getId().compareTo(o2.getId());
					}
				});
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return "Sort by Type";
			}
		});
		header.get(3).addOperation(new Operation() {
			@Override
			public void execute() {
				table.sort(new Comparator<EventInterface>() {
					@Override
					public int compare(EventInterface o1, EventInterface o2) {
						int cmp = o1.getMeetings().first().compareTo(o2.getMeetings().first());
						if (cmp != 0) return cmp;
						cmp = o1.getName().compareTo(o2.getName());
						if (cmp != 0) return cmp;
						cmp = o1.getType().compareTo(o2.getType());
						if (cmp != 0) return cmp;
						return o1.getId().compareTo(o2.getId());
					}
				});
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return "Sort by Date";
			}
		});
		header.get(4).addOperation(new Operation() {
			@Override
			public void execute() {
				table.sort(new Comparator<EventInterface>() {
					@Override
					public int compare(EventInterface o1, EventInterface o2) {
						int cmp = new Integer(o1.getMeetings().first().getDayOfWeek()).compareTo(o2.getMeetings().first().getDayOfWeek());
						if (cmp != 0) return cmp;
						cmp = new Integer(o1.getMeetings().first().getStartSlot()).compareTo(o2.getMeetings().first().getStartSlot());
						if (cmp != 0) return cmp;
						cmp = o1.getMeetings().first().compareTo(o2.getMeetings().first());
						if (cmp != 0) return cmp;
						cmp = o1.getName().compareTo(o2.getName());
						if (cmp != 0) return cmp;
						cmp = o1.getType().compareTo(o2.getType());
						if (cmp != 0) return cmp;
						return o1.getId().compareTo(o2.getId());
					}
				});
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return "Sort by Time";
			}
		});
		header.get(5).addOperation(new Operation() {
			@Override
			public void execute() {
				table.sort(new Comparator<EventInterface>() {
					@Override
					public int compare(EventInterface o1, EventInterface o2) {
						int cmp = o1.getMeetings().first().getLocationName().compareTo(o2.getMeetings().first().getLocationName());
						if (cmp != 0) return cmp;
						cmp = o1.getMeetings().first().compareTo(o2.getMeetings().first());
						if (cmp != 0) return cmp;
						cmp = o1.getName().compareTo(o2.getName());
						if (cmp != 0) return cmp;
						cmp = o1.getType().compareTo(o2.getType());
						if (cmp != 0) return cmp;
						return o1.getId().compareTo(o2.getId());
					}
				});
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return "Sort by Location";
			}
		});
		header.get(6).addOperation(new Operation() {
			@Override
			public void execute() {
				table.sort(new Comparator<EventInterface>() {
					@Override
					public int compare(EventInterface o1, EventInterface o2) {
						int cmp = o1.getInstructorNames("|").compareTo(o2.getInstructorNames("|"));
						if (cmp != 0) return cmp;
						cmp = o1.getName().compareTo(o2.getName());
						if (cmp != 0) return cmp;
						cmp = o1.getType().compareTo(o2.getType());
						if (cmp != 0) return cmp;
						return o1.getId().compareTo(o2.getId());
					}
				});
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return "Sort by Instructor / Sponsor";
			}
		});
		header.get(7).addOperation(new Operation() {
			@Override
			public void execute() {
				table.sort(new Comparator<EventInterface>() {
					@Override
					public int compare(EventInterface o1, EventInterface o2) {
						int a1 = 0, a2 = 0;
						Date d1 = null, d2 = null;
						for (MeetingInterface m: o1.getMeetings()) {
							if (m.isApproved()) a1++;
							if (m.isApproved() && d1 == null) d1 = m.getApprovalDate();
						}
						for (MeetingInterface m: o2.getMeetings()) {
							if (m.isApproved()) a2++;
							if (m.isApproved() && d2 == null) d2 = m.getApprovalDate();
						}
						Float f1 = ((float)a1) / o1.getMeetings().size();
						Float f2 = ((float)a2) / o2.getMeetings().size();
						int cmp = f1.compareTo(f2);
						if (cmp != 0) return cmp;
						if (d1 != null && d2 != null) {
							cmp = d1.compareTo(d2);
							if (cmp != 0) return cmp;
						}
						cmp = o1.getName().compareTo(o2.getName());
						if (cmp != 0) return cmp;
						cmp = o1.getType().compareTo(o2.getType());
						if (cmp != 0) return cmp;
						return o1.getId().compareTo(o2.getId());
					}
				});
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return "Sort by Approval";
			}
		});
		table.addRow(null, header);
		return table;
	}
	
	public void populateEventTable(UniTimeTable<EventInterface> table) {
		table.clearTable(1);
		for (EventInterface event: iData) {
			List<Widget> line = new ArrayList<Widget>();
			if (event.hasCourseNames()) {
				String name = "";
				String section = "";
				String prevCn = "", prevExt = "";
				for (int i = 0; i < event.getCourseNames().size(); i++) {
					String cn = event.getCourseNames().get(i);
					String ext = event.getExternalIds().get(i);
					if (name.isEmpty()) {
						name += cn;
						section += ext;
					} else if (event.getInstruction() != null) {
						name += "<br><span style='color:gray;'>" + (cn.equals(prevCn) ? "" : cn) + "</span>";
						section += "<br><span style='color:gray;'>" + (ext.equals(prevExt) ? "" : ext) + "</span>";
					} else {
						name += "<br>" + (cn.equals(prevCn) ? "" : cn);
						section += "<br>" + (ext.equals(prevExt) ? "" : ext);
					}
					prevCn = cn; prevExt = ext;
				}
				line.add(new HTML(name, false));
				line.add(new NumberCell(section));
				line.add(new Label(event.getInstruction() == null ? event.getType().getAbbreviation() : event.getInstruction(), false));
			} else {
				line.add(new DoubleCell(event.getName()));
				line.add(new Label(event.getType().getAbbreviation(), false));
			}
			String date = "", time = "", room = "", approved = "";
			TreeSet<MeetingInterface> meetings = new TreeSet<MeetingInterface>();
			for (MeetingInterface meeting: event.getMeetings())
				if (!filter(meeting))
					meetings.add(meeting);
			if (meetings.isEmpty()) continue;

			String prevDate = "", prevTime = "", prevRoom = "", prevApproved = "";
			boolean prevPast = false;
			for (MultiMeetingInterface m: EventInterface.getMultiMeetings(meetings, true, true)) {
				if (!date.isEmpty()) { date += "<br>"; time += "<br>"; room += "<br>"; approved += "<br>"; }
				if (prevPast != m.isPast() || !prevDate.equals(m.getMeetingDates()))
					date += (m.isPast() ? "<span style='font-style:italic;color:gray;'>" : "") + m.getMeetingDates() + (m.isPast() ? "</span>" : "");
				if (prevPast != m.isPast() || !prevTime.equals(m.getMeetingTime()))
					time += (m.isPast() ? "<span style='font-style:italic;color:gray;'>" : "") + m.getMeetingTime() + (m.isPast() ? "</span>" : "");
				if (prevPast != m.isPast() || !prevRoom.equals(m.getApprovalDate())) {
					room += (m.isPast() ? "<span style='font-style:italic;color:gray;'>" : "") + m.getLocationNameWithHint() + (m.isPast() ? "</span>" : "");
				}
				if (!prevApproved.equals(m.isApproved() ? sDateFormat.format(m.getApprovalDate()) : "-")) {
					if (!m.isApproved()) {
						approved += "<span style='font-style:italic;color:red;'>not approved</span>";
					} else {
						approved += sDateFormat.format(m.getApprovalDate());
					}
				}
				prevPast = m.isPast(); prevApproved = (m.isApproved() ? sDateFormat.format(m.getApprovalDate()) : "-");
				prevDate = m.getMeetingDates(); prevTime = m.getMeetingTime(); prevRoom = m.getLocationName();
			}
			line.add(new HTML(date, false));
			line.add(new HTML(time, false));
			line.add(new HTML(room, false));
			if (event.hasInstructors()) {
				line.add(new HTML(event.getInstructorNames("<br>"), false));
			} else {
				line.add(new Label(event.hasSponsor() ? event.getSponsor().getName() : ""));
			}
			line.add(new HTML(approved, false));
			int row = table.addRow(event, line);
			table.getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP);
		}
		table.getElement().getStyle().setWidth(100, Unit.PCT);
		setColumnVisible(table, 3, iWeekPanel.getValue() == null || !iWeekPanel.getValue().isOne());
		setColumnVisible(table, 5, iRoomPanel.getValue() == null || !iRoomPanel.getValue().isOne());
	}
	
	public void setColumnVisible(UniTimeTable<EventInterface> table, int col, boolean visible) {
		for (int r = 0; r < table.getRowCount(); r++) {
			table.getCellFormatter().setVisible(r, col - (table.getFlexCellFormatter().getColSpan(r, 0) == 2 ? 1 : 0), visible);
		}
	}
	
	public static class NumberCell extends HTML implements HasCellAlignment {
		public NumberCell(String text) {
			super(text, false);
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class DoubleCell extends HTML implements HasColSpan {
		public DoubleCell(String text) {
			super(text, false);
		}

		@Override
		public int getColSpan() {
			return 2;
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
	
	public static void personFound(String externalUniqueId) {
		sInstance.iResources.setText(externalUniqueId);
		sInstance.resourceTypeChanged(false);
	}
	
	private String serialize(IsSerializable object) throws SerializationException {
		SerializationStreamFactory factory = (SerializationStreamFactory)RPC;
		SerializationStreamWriter writer = factory.createStreamWriter();
		writer.writeObject(object);
		return URL.encodeQueryString(writer.toString());
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
				(iLocRoom.isEmpty() ? "" : "&room=" + URL.encodeQueryString(iLocRoom)));
	}
	
	private native JavaScriptObject createLookupCallback() /*-{
		return function(person) {
			@org.unitime.timetable.gwt.client.events.EventResourceTimetable::personFound(Ljava/lang/String;)(person[0]);
	    };
	 }-*/;
	
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
			if (interval == null || interval.isAll()) return "All Rooms";
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
			if (interval == null || interval.isAll()) return "All Rooms";
			return interval.isOne() ? interval.getFirst().getName() : interval.getFirst().getName() + " - " + interval.getLast().getName();			

		}
		
		public String getSelection() {
			if (getValue() == null || getValue().isAll()) return "";
			return getValue().isOne() ? getValue().getFirst().getName() : getValue().getFirst().getName() + "-" + getValue().getLast().getName();			
		}
	}
}
