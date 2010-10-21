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

import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.services.EventService;
import org.unitime.timetable.gwt.services.EventServiceAsync;
import org.unitime.timetable.gwt.shared.EventException;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class EventResourceTimetable extends Composite {
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader;
	private SimplePanel iSimple;
	private TimeGrid iTimeGrid;
	
	private final EventServiceAsync iEventService = GWT.create(EventService.class);
	
	public EventResourceTimetable() {
		iPanel = new SimpleForm(2);
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		iSimple = new SimplePanel();
		iPanel.addRow(iSimple);
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
		UniTimePageLabel.getInstance().setPageName(type.getPageTitle());
		LoadingWidget.getInstance().show("Loading " + type.getLabel() + " " + name + " ...");
		iHeader.addButton("print", "<u>P</u>rint", 'p', 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.print(iHeader.getHeaderTitle(),
						"", "", 
						iTimeGrid.getPrintWidget());
			}
		});
		iHeader.setEnabled("print", false);
		iEventService.findResource(term, type, name, new AsyncCallback<ResourceInterface>() {

			@Override
			public void onSuccess(ResourceInterface result) {
				LoadingWidget.getInstance().setMessage("Loading " + result.getName() + " timetable for " + result.getSessionName() + " ...");
				final ResourceInterface resource = result;
				iEventService.findEvents(resource, new AsyncCallback<List<EventInterface>>() {
					
					@Override
					public void onSuccess(List<EventInterface> result) {
						LoadingWidget.getInstance().hide();
						if (result.isEmpty()) {
							iHeader.setErrorMessage(type.getLabel().substring(0, 1).toUpperCase() + type.getLabel().substring(1) + " " + name + " has no events in " + term + "."); 
						} else {
							iHeader.setHeaderTitle(resource.getName() + " timetable for " + resource.getSessionName());
							iHeader.setMessage(null);
							int nrDays = 4;
							int firstSlot = -1, lastSlot = -1;
							for (EventInterface event: result) {
								for (MeetingInterface meeting: event.getMeetings()) {
									if (firstSlot < 0 || firstSlot > meeting.getStartSlot()) firstSlot = meeting.getStartSlot();
									if (lastSlot < 0 || lastSlot < meeting.getEndSlot()) lastSlot = meeting.getEndSlot();
									nrDays = Math.max(nrDays, meeting.getDayOfWeek());
								}
							}
							nrDays ++;
							int firstHour = firstSlot / 12;
							int lastHour = lastSlot / 12;
							iTimeGrid = new TimeGrid(nrDays, (int)(0.9 * Window.getClientWidth() / nrDays), false, false, (firstHour < 7 ? firstHour : 7), (lastHour > 18 ? lastHour : 18));
							String eventIds = "";
							for (int i = 0; i < result.size(); i++) {
								iTimeGrid.addEvent(result.get(i), i);
								if (!eventIds.isEmpty()) eventIds += ",";
								eventIds += result.get(i).getId();
							}
							iTimeGrid.setCalendarUrl(GWT.getHostPageBaseURL() + "calendar?sid=" + resource.getSessionId() + "&eid=" + eventIds);
							iSimple.setWidget(iTimeGrid);
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

}
