/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.client.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.services.EventService;
import org.unitime.timetable.gwt.services.EventServiceAsync;
import org.unitime.timetable.gwt.shared.EventException;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MultiMeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class EventResourceTimetable extends Composite {
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iTableHeader;
	private ListBox iWeek;
	private TimeGrid iTimeGrid;
	private UniTimeTable<EventInterface> iTable;
	private ResourceInterface iResource;
	private List<EventInterface> iData;
	
	private final EventServiceAsync iEventService = GWT.create(EventService.class);
	
	public EventResourceTimetable() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		iWeek = new ListBox();
		iPanel.addRow(iWeek);
		iPanel.getCellFormatter().setHorizontalAlignment(1, 0,  HasHorizontalAlignment.ALIGN_RIGHT);
		final SimplePanel gridPanel = new SimplePanel();
		iPanel.addRow(gridPanel);
		iTableHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iTableHeader);
		iTableHeader.setVisible(false);
		final SimplePanel tablePanel = new SimplePanel();
		iPanel.addRow(tablePanel);
		iPanel.addBottomRow(iHeader.clonePanel());
		initWidget(iPanel);
		
		String typeString = Window.Location.getParameter("type");
		if (typeString == null) throw new EventException("Resource type is not provided.");
		final ResourceType type = ResourceType.valueOf(typeString.toUpperCase());
		if (type == null) throw new EventException("Resource type " + typeString + " is not recognized.");
		final String term = Window.Location.getParameter("term");
		if (term == null) throw new EventException("Academic session is not provided.");
		final String name = Window.Location.getParameter("name");
		if (name == null) throw new EventException(type.getLabel().substring(0, 1).toUpperCase() + type.getLabel().substring(1) + " name is not provided.");
		final String start = Window.Location.getParameter("date");
		UniTimePageLabel.getInstance().setPageName(type.getPageTitle());
		LoadingWidget.getInstance().show("Loading " + type.getLabel() + " " + name + " ...");
		iHeader.addButton("print", "<u>P</u>rint", 'p', 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				
				UniTimeTable<EventInterface> table  = createEventTable();
				populateEventTable(table);
				table.getElement().getStyle().setProperty("page-break-before", "always");
				TimeGrid tg = iTimeGrid.getPrintWidget();
				WeekInterface week = null;
				if (iWeek.getSelectedIndex() == 0) {
					for (EventInterface event: iData)
						tg.addPrintEvent(event);
				} else if (iWeek.getSelectedIndex() > 0) {
					int dayOfYear = Integer.parseInt(iWeek.getValue(iWeek.getSelectedIndex()));
					for (WeekInterface w: iResource.getWeeks())
						if (w.getDayOfYear() == dayOfYear) { week = w; break; }
					for (EventInterface event: iData) {
						List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
						for (MeetingInterface meeting: event.getMeetings())
							if (meeting.getDayOfYear() >= dayOfYear && meeting.getDayOfYear() < dayOfYear + 7)
								meetings.add(meeting);
						if (!meetings.isEmpty())
							tg.addPrintEvent(event, meetings);
					}
				}
				tg.labelDays(week);
				ToolBox.print(iHeader.getHeaderTitle(),
						"", "", 
						tg,
						table
						);
			}
		});
		iHeader.setEnabled("print", false);
		iEventService.findResource(term, type, name, new AsyncCallback<ResourceInterface>() {

			@Override
			public void onSuccess(ResourceInterface result) {
				LoadingWidget.getInstance().setMessage("Loading " + result.getName() + " timetable for " + result.getSessionName() + " ...");
				iResource = result;
				iEventService.findEvents(iResource, new AsyncCallback<List<EventInterface>>() {
					
					@Override
					public void onSuccess(List<EventInterface> result) {
						iData = result;
						
						LoadingWidget.getInstance().hide();
						if (iData.isEmpty()) {
							iHeader.setErrorMessage(type.getLabel().substring(0, 1).toUpperCase() + type.getLabel().substring(1) + " " + name + " has no events in " + term + "."); 
						} else {
							Collections.sort(iData);

							iHeader.setHeaderTitle(iResource.getName() + " timetable for " + iResource.getSessionName());
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
							iTimeGrid.setOneWeek(false);
							iTimeGrid.setRoomResource(iResource.getType() == ResourceType.ROOM);
							for (EventInterface event: iData) {
								iTimeGrid.addEvent(event);
								if (!eventIds.isEmpty()) eventIds += ",";
								eventIds += event.getId();
							}
							iTimeGrid.setCalendarUrl(GWT.getHostPageBaseURL() + "calendar?sid=" + iResource.getSessionId() + "&eid=" + eventIds);
							gridPanel.setWidget(iTimeGrid);
							
							iTableHeader.setHeaderTitle(iResource.getName() + " events for " + iResource.getSessionName());
							iTableHeader.setVisible(true);
							iTable = createEventTable();
							populateEventTable(iTable);
							tablePanel.setWidget(iTable);
							
							iWeek.clear();
							iWeek.addItem("All weeks", "");
							iWeek.setSelectedIndex(0);
							
							if (iResource.hasWeeks()) {
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
									iWeek.addItem("Week " + week.getName(), String.valueOf(week.getDayOfYear()));
								}
								iWeek.addChangeHandler(new ChangeHandler() {
									@Override
									public void onChange(ChangeEvent changeEvent) {
										weekChanged();
									}
								});
								if (start != null) {
									DateTimeFormat df = DateTimeFormat.getFormat("MM/dd");
									String date = start;
									if ("today".equalsIgnoreCase(start)) {
										date = df.format(new Date());
									} else {
										try {
											date = df.format(df.parse(start));
										} catch (IllegalArgumentException e) {}
									}
									WeekInterface week = null;
									for (WeekInterface w: iResource.getWeeks()) {
										if (w.getDayNames().contains(date)) { week = w; break; }
									}
									if (week != null) {
										for (int i = 1; i < iWeek.getItemCount(); i++) {
											if (iWeek.getValue(i).equals(String.valueOf(week.getDayOfYear()))) {
												iWeek.setSelectedIndex(i);
												weekChanged();
												break;
											}
										}
									}
								}
								iPanel.getRowFormatter().setVisible(1, true);
							} else {
								iPanel.getRowFormatter().setVisible(1, false);
							}
							
							iHeader.setEnabled("print", true);
						}
					}
			
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(caught.getMessage());
					}
				});
			}

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(caught.getMessage());
			}

		});
	}
	
	private void weekChanged() {
		iTimeGrid.clear();
		populateEventTable(iTable);
		WeekInterface week = null;
		if (iWeek.getSelectedIndex() <= 0) {
			iHeader.setHeaderTitle(iResource.getName() + " timetable for " + iResource.getSessionName());
			iTableHeader.setHeaderTitle(iResource.getName() + " events for " + iResource.getSessionName());
			iTimeGrid.setOneWeek(false);
			for (EventInterface event: iData) {
				iTimeGrid.addEvent(event);
			}
		} else if (iWeek.getSelectedIndex() > 0) {
			iHeader.setHeaderTitle(iResource.getName() + " timetable for " + iWeek.getItemText(iWeek.getSelectedIndex()).toLowerCase());
			iTableHeader.setHeaderTitle(iResource.getName() + " events for " + iWeek.getItemText(iWeek.getSelectedIndex()).toLowerCase());
			iTimeGrid.setOneWeek(true);
			int dayOfYear = Integer.parseInt(iWeek.getValue(iWeek.getSelectedIndex()));
			for (WeekInterface w: iResource.getWeeks()) {
				if (w.getDayOfYear() == dayOfYear) { week = w; break; }
			}
			for (EventInterface event: iData) {
				List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
				for (MeetingInterface meeting: event.getMeetings()) {
					if (meeting.getDayOfYear() >= dayOfYear && meeting.getDayOfYear() < dayOfYear + 7) {
						meetings.add(meeting);
					}
				}
				if (!meetings.isEmpty())
					iTimeGrid.addEvent(event, meetings);
			}
		}
		iTimeGrid.labelDays(week);
		iTimeGrid.shrink();
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
		header.add(new UniTimeTableHeader("Approved"));
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
		header.get(7).addOperation(new Operation() {
			@Override
			public void execute() {
				table.sort(new Comparator<EventInterface>() {
					@Override
					public int compare(EventInterface o1, EventInterface o2) {
						int cmp = (o1.getMeetings().first().isApproved() ? o1.getMeetings().first().getApprovalDate() : "").compareTo(
								o2.getMeetings().first().isApproved() ? o2.getMeetings().first().getApprovalDate() : "");
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
				for (int i = 0; i < event.getCourseNames().size(); i++) {
					String cn = event.getCourseNames().get(i);
					String ext = event.getExternalIds().get(i);
					if (name.isEmpty()) {
						name += cn;
						section += ext;
					} else {
						name += "<br><span style='color:gray;'>" + cn + "</span>";
						section += "<br><span style='color:gray;'>" + ext + "</span>";
					}
				}
				line.add(new HTML(name, false));
				line.add(new NumberCell(section));
				line.add(new Label(event.getInstruction(), false));
			} else {
				line.add(new Label(event.getName(), true));
				line.add(new Label(""));
				line.add(new Label(event.getType(), false));
			}
			String date = "", time = "", room = "", approved = "";
			TreeSet<MeetingInterface> meetings = null;
			if (iWeek.getSelectedIndex() <= 0) {
				meetings = event.getMeetings();
			} else {
				meetings = new TreeSet<MeetingInterface>();
				int dayOfYear = Integer.parseInt(iWeek.getValue(iWeek.getSelectedIndex()));
				for (MeetingInterface meeting: event.getMeetings())
					if (meeting.getDayOfYear() >= dayOfYear && meeting.getDayOfYear() < dayOfYear + 7)
						meetings.add(meeting);
			}
			if (meetings.isEmpty()) continue;

			for (MultiMeetingInterface m: EventInterface.getMultiMeetings(meetings, false, false)) {
				if (!date.isEmpty()) { date += "<br>"; time += "<br>"; room += "<br>"; approved += "<br>"; }
				date += (m.isPast() ? "<span style='font-style:italic;color:gray;'>" : "") + m.getMeetingDates() + (m.isPast() ? "</span>" : "");
				time += (m.isPast() ? "<span style='font-style:italic;color:gray;'>" : "") + m.getMeetingTime() + (m.isPast() ? "</span>" : "");
				room += (m.isPast() ? "<span style='font-style:italic;color:gray;'>" : "") + m.getLocationName() + (m.isPast() ? "</span>" : "");
				approved += (m.isPast() ? "<span style='font-style:italic;color:gray;'>" : "") + (m.getMeetings().first().isApproved() ? m.getMeetings().first().getApprovalDate() : "") + (m.isPast() ? "</span>" : "");
			}
			line.add(new HTML(date, false));
			line.add(new HTML(time, false));
			line.add(new HTML(room, false));
			if (event.hasInstructor()) {
				line.add(new HTML(event.getInstructor(), false));
			} else {
				line.add(new Label(event.hasSponsor() ? event.getSponsor() : ""));
			}
			line.add(new HTML(approved, false));
			int row = table.addRow(event, line);
			table.getRowFormatter().setVerticalAlign(row, HasVerticalAlignment.ALIGN_TOP);
		}
		table.getElement().getStyle().setWidth(100, Unit.PCT);
		table.setColumnVisible(3, iWeek.getSelectedIndex() <= 0);
		table.setColumnVisible(5, iResource.getType() != ResourceType.ROOM);
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

}
