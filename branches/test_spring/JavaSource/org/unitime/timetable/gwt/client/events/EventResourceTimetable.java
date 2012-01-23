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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.WeekSelector;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasColSpan;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.services.EventService;
import org.unitime.timetable.gwt.services.EventServiceAsync;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.IdValueInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MultiMeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author Tomas Muller
 */
public class EventResourceTimetable extends Composite {
	private SimpleForm iPanel, iFilter;
	private SimplePanel iGridPanel, iTablePanel;
	private UniTimeHeaderPanel iHeader, iTableHeader, iFooter, iFilterHeader;
	private TimeGrid iTimeGrid;
	private UniTimeTable<EventInterface> iTable;
	private ResourceInterface iResource;
	private List<EventInterface> iData;
	private WeekSelector iWeekPanel;
	private ListBox iSessions;
	private ListBox iResourceTypes;
	private SuggestBox iResources;
	private boolean iCanLookupPeople = false;
	private int iResourcesRow = -1, iWeekRow = -1, iLastRow = -1;
	
	private static EventResourceTimetable sInstance = null;
	
	private final EventServiceAsync iEventService = GWT.create(EventService.class);
	
	public EventResourceTimetable(String type) {
		sInstance = this; 
		
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
		
		iSessions = new ListBox();
		iFilter.addRow("Academic Session:", iSessions);
		iEventService.findSessions(Window.Location.getParameter("term"), new AsyncCallback<List<IdValueInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				iFilterHeader.setErrorMessage(caught.getMessage());
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(List<IdValueInterface> result) {
				for (int idx = 0; idx < result.size(); idx++) {
					IdValueInterface idVal = result.get(idx);
					iSessions.addItem(idVal.getValue(), idVal.getId());
					if (idVal.isSelected()) iSessions.setSelectedIndex(idx);
				}
				if (iSessions.getItemCount() > 0)
					iFilter.setVisible(true);
				if (iSessions.getSelectedIndex() >= 0)
					sessionChanged();
			}
		});
		iSessions.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				sessionChanged();
			}
		});
		
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
				if (iSessions.getSelectedIndex() >= 0 && iResourceTypes.getSelectedIndex() >= 0) {
					iEventService.findResources(iSessions.getValue(iSessions.getSelectedIndex()),
							ResourceType.valueOf(iResourceTypes.getValue(iResourceTypes.getSelectedIndex())),
							request.getQuery(), request.getLimit(), new AsyncCallback<List<ResourceInterface>>() {
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
								public void onSuccess(List<ResourceInterface> result) {
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
		resourceTypeChanged(true);
		
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		iPanel.addRow(iFilter);
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		iWeekPanel = new WeekSelector();
		iWeekPanel.addWeekChangedHandler(new WeekSelector.WeekChangedHandler() {
			@Override
			public void onWeekChanged(WeekSelector.WeekChangedEvent e) {
				iTimeGrid.clear();
				populateEventTable(iTable);
				if (e.getSelection().isAllWeeks()) {
					if (iResource.getType() != ResourceType.PERSON)
						changeUrl(Window.Location.getParameter("page"),
								iResource.getSessionAbbv(),
								iResourceTypes.getValue(iResourceTypes.getSelectedIndex()), iResource.getAbbreviation(), null);
					iHeader.setHeaderTitle(iResource.getNameWithHint() + " timetable for " + iResource.getSessionName());
					iTableHeader.setHeaderTitle(iResource.getNameWithHint() + " events for " + iResource.getSessionName());
					iTimeGrid.setNrWeeks(e.getSelection().getNrWeeks());
					for (EventInterface event: iData) {
						iTimeGrid.addEvent(event);
					}
				} else {
					iHeader.setHeaderTitle(iResource.getNameWithHint() + " timetable for " + e.getSelection().getReplacementString().toLowerCase());
					iTableHeader.setHeaderTitle(iResource.getNameWithHint() + " events for " + e.getSelection().getReplacementString().toLowerCase());
					iTimeGrid.setNrWeeks(e.getSelection().getNrWeeks());
					if (iResource.getType() != ResourceType.PERSON)
						changeUrl(Window.Location.getParameter("page"), iResource.getSessionAbbv(),
								iResourceTypes.getValue(iResourceTypes.getSelectedIndex()),
								iResource.getAbbreviation(),
								e.getSelection().getFirstDay() + (e.getSelection().isOneWeek() ? "": "-" + e.getSelection().getLastDay()));
					for (EventInterface event: iData) {
						List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
						for (MeetingInterface meeting: event.getMeetings()) {
							if (meeting.getDayOfYear() >= e.getSelection().getFirstDayOfYear() && meeting.getDayOfYear() <= e.getSelection().getLastDayOfYear()) {
								meetings.add(meeting);
							}
						}
						if (!meetings.isEmpty())
							iTimeGrid.addEvent(event, meetings);
					}
				}
				iTimeGrid.shrink();
				iTimeGrid.labelDays(e.getSelection().getFirstWeek(), e.getSelection().getLastWeek());
			}
		});
		iWeekRow = iPanel.addRow(iWeekPanel);
		iPanel.getCellFormatter().setHorizontalAlignment(iWeekRow, 0,  HasHorizontalAlignment.ALIGN_RIGHT);
		iGridPanel = new SimplePanel();
		iPanel.addRow(iGridPanel);
		iTableHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iTableHeader);
		iTablePanel = new SimplePanel();
		iPanel.addRow(iTablePanel);
		iFooter = iHeader.clonePanel();
		iLastRow = iPanel.addBottomRow(iFooter);
		initWidget(iPanel);

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
				if (iWeekPanel.getSelection().isAllWeeks()) {
					for (EventInterface event: iData)
						tg.addPrintEvent(event);
				} else {
					for (EventInterface event: iData) {
						List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
						for (MeetingInterface meeting: event.getMeetings())
							if (meeting.getDayOfYear() >= iWeekPanel.getSelection().getFirstDayOfYear() && meeting.getDayOfYear() <= iWeekPanel.getSelection().getLastDayOfYear())
								meetings.add(meeting);
						if (!meetings.isEmpty())
							tg.addPrintEvent(event, meetings);
					}
				}
				tg.labelDays(iWeekPanel.getSelection().getFirstWeek(), iWeekPanel.getSelection().getLastWeek());
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
				ToolBox.open(iTimeGrid.getCalendarUrl());
			}
		});
		iHeader.setTitle("Export timetable in iCalendar format.");
		iHeader.setEnabled("print", false);
		iHeader.setEnabled("export", false);
		
		iEventService.canLookupPeople(new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(Boolean result) {
				iCanLookupPeople = (result == null ? false: result);
				iFilterHeader.setEnabled("lookup", iCanLookupPeople && getResourceType() == ResourceType.PERSON);
			}
		});
	}
	
	private void sessionChanged() {
		resourceTypeChanged(true);
	}
	
	public ResourceType getResourceType() {
		if (iResourceTypes.getSelectedIndex() < 0)
			return null;
		return ResourceType.values()[iResourceTypes.getSelectedIndex()];
	}
	
	public String getSessionName() {
		if (iSessions.getSelectedIndex() < 0)
			return null;
		return iSessions.getValue(iSessions.getSelectedIndex());
	}
	
	public String getResourceName() {
		if (getResourceType() == ResourceType.PERSON && !iCanLookupPeople) return "";
		return (iResources.getText() == null || iResources.getText().isEmpty() ? null : iResources.getText());
	}
	
	private void resourceTypeChanged(boolean allowEmptyResource) {
		ResourceType type = getResourceType();
		if (type != null) {
			iFilter.getRowFormatter().setVisible(iResourcesRow, type != ResourceType.PERSON);
			((Label)iFilter.getWidget(iResourcesRow, 0)).setText(type.getLabel().substring(0,1).toUpperCase() + type.getLabel().substring(1) + ":");
			iFilterHeader.setEnabled("lookup", iCanLookupPeople && getResourceType() == ResourceType.PERSON);
			if (getSessionName() != null && ((type == ResourceType.PERSON && allowEmptyResource) || getResourceName() != null)) {
				iFilterHeader.clearMessage();
				LoadingWidget.getInstance().show("Loading " + type.getLabel() + (type != ResourceType.PERSON ? " " + getResourceName() : "") + " ...");
				iEventService.findResource(getSessionName(), type, getResourceName(), new AsyncCallback<ResourceInterface>() {
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
					public void onSuccess(ResourceInterface result) {
						LoadingWidget.getInstance().hide();
						resourceChanged(result);
					}
				});
			}
		}
	}
	
	private void resourceChanged(ResourceInterface resource) {
		LoadingWidget.getInstance().show("Loading " + resource.getName() + " timetable for " + resource.getSessionName() + " ...");
		iResource = resource;
		iFilterHeader.clearMessage();
		iEventService.findEvents(iResource, new AsyncCallback<List<EventInterface>>() {
			
			@Override
			public void onSuccess(List<EventInterface> result) {
				iData = result;
				if (iResource.getType() != ResourceType.PERSON)
					changeUrl(Window.Location.getParameter("page"), iResource.getSessionAbbv(), iResourceTypes.getValue(iResourceTypes.getSelectedIndex()), iResource.getAbbreviation(), null);
				LoadingWidget.getInstance().hide();
				if (iData.isEmpty()) {
					iFilterHeader.setErrorMessage((iResource.getType() == ResourceType.PERSON ? "" : iResource.getType().getLabel().substring(0, 1).toUpperCase() + iResource.getType().getLabel().substring(1) + " ") + iResource.getName() + " has no events in " + iResource.getSessionName() + "."); 
					for (int i = 1; i < iPanel.getRowCount(); i++)
						iPanel.getRowFormatter().setVisible(i, i == iLastRow);
					iGridPanel.setVisible(false);
					iHeader.setEnabled("print", false);
					iHeader.setEnabled("export", false);
				} else {
					Collections.sort(iData);

					iHeader.setHeaderTitle(iResource.getNameWithHint() + " timetable for " + iResource.getSessionName());
					iHeader.setMessage(null);
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
					int firstHour = firstSlot / 12;
					int lastHour = 1 + (lastSlot - 1) / 12;
					HashMap<Long, String> colors = new HashMap<Long, String>();
					iTimeGrid = new TimeGrid(colors, nrDays, (int)(0.9 * Window.getClientWidth() / nrDays), false, false, (firstHour < 7 ? firstHour : 7), (lastHour > 18 ? lastHour : 18));
					String eventIds = "";
					iTimeGrid.setNrWeeks(-1);
					iTimeGrid.setRoomResource(iResource.getType() == ResourceType.ROOM);
					for (EventInterface event: iData) {
						iTimeGrid.addEvent(event);
						if (!eventIds.isEmpty()) eventIds += ",";
						eventIds += event.getId();
					}
					if (iResource.hasCalendar()) {
						iTimeGrid.setCalendarUrl(GWT.getHostPageBaseURL() + "calendar?q=" + iResource.getCalendar());
						iHeader.setEnabled("export", true);
					} else {
						iHeader.setEnabled("export", false);
					}
					iGridPanel.setWidget(iTimeGrid);
					
					iTableHeader.setHeaderTitle(iResource.getNameWithHint() + " events for " + iResource.getSessionName());
					iTable = createEventTable();
					populateEventTable(iTable);
					iTablePanel.setWidget(iTable);
					
					if (iResource.hasWeeks()) {
						iWeekPanel.clearWeeks();
						for (WeekInterface week: iResource.getWeeks()) {
							boolean hasEvents = false;
							events: for (EventInterface event: iData) {
								for (MeetingInterface meeting: event.getMeetings()) {
									if (meeting.getDayOfYear() >= week.getDayOfYear() && meeting.getDayOfYear() < week.getDayOfYear() + 7) {
										hasEvents = true;
										break events;
									}
								}
							}
							if (!hasEvents) continue;
							iWeekPanel.addWeek(week);
						}						
						iWeekPanel.select(Window.Location.getParameter("date"));
					}
					
					iHeader.setEnabled("print", true);
					for (int i = 1; i < iPanel.getRowCount(); i++)
						iPanel.getRowFormatter().setVisible(i, i != iWeekRow || iResource.hasWeeks());
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
						int cmp = (o1.hasInstructor() ? o1.getInstructor() : o1.hasSponsor() ? o1.getSponsor() : "").compareTo(
								o2.hasInstructor() ? o2.getInstructor() : o2.hasSponsor() ? o2.getSponsor() : "");
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
				line.add(new Label(event.getInstruction() == null ? event.getType() : event.getInstruction(), false));
			} else {
				line.add(new DoubleCell(event.getName()));
				line.add(new Label(event.getType(), false));
			}
			String date = "", time = "", room = "";
			TreeSet<MeetingInterface> meetings = null;
			if (iWeekPanel.getSelection().isAllWeeks()) {
				meetings = event.getMeetings();
			} else {
				meetings = new TreeSet<MeetingInterface>();
				for (MeetingInterface meeting: event.getMeetings())
					if (meeting.getDayOfYear() >= iWeekPanel.getSelection().getFirstDayOfYear() && meeting.getDayOfYear() <= iWeekPanel.getSelection().getLastDayOfYear())
						meetings.add(meeting);
			}
			if (meetings.isEmpty()) continue;

			String prevDate = "", prevTime = "", prevRoom = "";
			boolean prevPast = false;
			for (MultiMeetingInterface m: EventInterface.getMultiMeetings(meetings, false, false)) {
				if (!date.isEmpty()) { date += "<br>"; time += "<br>"; room += "<br>"; }
				if (prevPast != m.isPast() || !prevDate.equals(m.getMeetingDates()))
					date += (m.isPast() ? "<span style='font-style:italic;color:gray;'>" : "") + m.getMeetingDates() + (m.isPast() ? "</span>" : "");
				if (prevPast != m.isPast() || !prevTime.equals(m.getMeetingTime()))
					time += (m.isPast() ? "<span style='font-style:italic;color:gray;'>" : "") + m.getMeetingTime() + (m.isPast() ? "</span>" : "");
				if (prevPast != m.isPast() || !prevRoom.equals(m.getLocationName())) {
					room += (m.isPast() ? "<span style='font-style:italic;color:gray;'>" : "") + m.getLocationNameWithHint() + (m.isPast() ? "</span>" : "");
				}
				prevPast = m.isPast();
				prevDate = m.getMeetingDates(); prevTime = m.getMeetingTime(); prevRoom = m.getLocationName();
			}
			line.add(new HTML(date, false));
			line.add(new HTML(time, false));
			line.add(new HTML(room, false));
			if (event.hasInstructor()) {
				line.add(new HTML(event.getInstructor().replace("|", "<br>"), false));
			} else {
				line.add(new Label(event.hasSponsor() ? event.getSponsor() : ""));
			}
			int row = table.addRow(event, line);
			table.getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP);
		}
		table.getElement().getStyle().setWidth(100, Unit.PCT);
		setColumnVisible(table, 3, !iWeekPanel.getSelection().isOneWeek());
		setColumnVisible(table, 5, iResource.getType() != ResourceType.ROOM);
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
	
	private native JavaScriptObject createLookupCallback() /*-{
		return function(person) {
			@org.unitime.timetable.gwt.client.events.EventResourceTimetable::personFound(Ljava/lang/String;)(person[0]);
	    };
	 }-*/;
	
	private native static void changeUrl(String page, String term, String type, String name, String date) /*-{
		try {
			var state = "term=" + term + "&type=" + type.toLowerCase() + "&name=" + name.replace(' ', '+') + (date == null ? "" : "&date=" + date);
			$wnd.history.pushState(state, "", "gwt.jsp?page=" + page + "&" + state);
		} catch (err) {
		}
	}-*/;
}
