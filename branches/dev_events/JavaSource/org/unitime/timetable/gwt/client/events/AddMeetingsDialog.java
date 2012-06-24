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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.ToolBox;
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
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConglictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

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
	
	private ScrollPanel iScroll;
	private int iIndex = 0, iStep = 10;
	
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
										getStartSlot(), getEndSlot(), getDates(), getRooms(), iSession.getAcademicSessionId()
									), new AsyncCallback<EventRoomAvailabilityRpcResponse>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									iDatesHeader.setErrorMessage(caught.getMessage());
								}
								
								@Override
								public void onSuccess(EventRoomAvailabilityRpcResponse result) {
									LoadingWidget.getInstance().hide();
									populate(result, 0);
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
		
		iDates = new SessionDatesSelector(session);
		iDatesForm.addRow(MESSAGES.propDates(), iDates);
		
		iTimes = new StartEndTimeSelector();
		iDatesForm.addRow(MESSAGES.propTimes(), iTimes);
		
		iRooms = new RoomFilterBox(session);
		iDatesForm.addRow(MESSAGES.propLocations(), iRooms);
		
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
				populate(iResponse, iIndex - 10);
				recenter();
			}
		});
		iAvailabilityHeader.addButton("next", MESSAGES.buttonRight(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				populate(iResponse, iIndex + 10);
				recenter();
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
							meeting.setEndOffset(Integer.parseInt(room.getProperty("breakTime", "10")));

							
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
		
		iScroll = new ScrollPanel(iRoomAvailability);

		iAvailabilityForm.addRow(iScroll);
		
		iAvailabilityForm.addNotPrintableBottomRow(iAvailabilityHeader.clonePanel());
		
		setWidget(iDatesForm);
		
		addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
			}
		});
		
		getElement().getStyle().setProperty("width", "auto");
	}
	
	public void showDialog() {
		iStep = (Window.getClientWidth() - 300) / 105;
		ToolBox.setMaxHeight(iScroll.getElement().getStyle(), (Window.getClientHeight() - 200) + "px");
		ToolBox.setMaxWidth(iDatesForm.getElement().getStyle(), (Window.getClientWidth() - 200) + "px");
		
		iResponse = null;
		setWidget(iDatesForm);
		center();
		RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
	}
		
	public List<Entity> getRooms() { return iMatchingRooms; }
	public Integer getStartSlot() { return iTimes.getValue().getStart(); }
	public Integer getEndSlot() { return iTimes.getValue().getEnd(); }
	public List<Integer> getDates() { return iDates.getSelectedDays(); }
	public boolean isSelected(Integer date, Entity room) {
		return iSelected.contains(date + ":" + room.getUniqueId());
	}
	
	public void reset(String roomFilterValue) {
		iMatchingRooms = null;
		iDates.setValue(new ArrayList<Date>());
		iTimes.setValue(new StartEndTimeSelector.StartEndTime(7*12 + 6, 17*12 + 6));
		iRooms.setValue(roomFilterValue == null || roomFilterValue.isEmpty() ? "department:Event" : roomFilterValue.contains("department:") ? roomFilterValue : "department:Event " + roomFilterValue, true);
	}

	private Integer iHoverDate = null;
	private Entity iHoverLoc = null;
	
	private void populate(EventRoomAvailabilityRpcResponse response, int index) {
		iResponse = response;
		iIndex = index;
		if (iIndex < 0) iIndex = 0;
		if (iIndex >= getRooms().size()) iIndex = iStep * (getRooms().size() / iStep);
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
					GwtHint.showHint(p.getElement(), room.getProperty("mouseOver", ""));
				}
			});
			p.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					((P)event.getSource()).removeStyleName("hover");
					GwtHint.hideHint();
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
				final Set<MeetingConglictInterface> conflicts = response.getOverlaps(date, Long.valueOf(room.getProperty("permId", null)));
				
				final P p = new P("cell");
				
				if (conflicts == null || conflicts.isEmpty()) {
					p.addStyleName("free");
				} else {
					p.addStyleName("conflict");
					String conf = "";
					int count = 0;
					for (MeetingConglictInterface event: conflicts) {
						if (count == 3) { conf += "<br>..."; break; }
						conf += (conf.isEmpty() ? "" : "<br>") + event.getName() + " (" + event.getType().getAbbreviation() + ")";
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
							for (MeetingConglictInterface conflictingEvent: conflicts)
								message += (conflicts.size() == 1 ? "" : "<br>&nbsp;&nbsp;&nbsp;") + conflictingEvent.getName() + " (" + conflictingEvent.getType().getAbbreviation() + ")";
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
		
	public Set<MeetingConglictInterface> getConflicts(Integer date, Entity room) {
		return iResponse.getOverlaps(date, Long.valueOf(room.getProperty("permId", null)));
	}
	
	public void setSelected(Integer date, Entity room, boolean selected) {
		if (!"1".equals(room.getProperty("overbook", "0"))) {
			Set<MeetingConglictInterface> conf = getConflicts(date, room);
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
		iScroll.getElement().getStyle().clearHeight();
		if (getElement().getClientHeight() > Window.getClientHeight() - 100)
			iScroll.getElement().getStyle().setHeight(Window.getClientHeight() - 200, Unit.PX);
		
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
        		iScroll.ensureVisible(iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()));
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
        		iScroll.ensureVisible(iPanels.get(iHoverDate + ":" + iHoverLoc.getUniqueId()));
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
        			populate(iResponse, iIndex + iStep);
        		break;
        	case KeyCodes.KEY_PAGEUP:
        		if (iIndex > 0)
        			populate(iResponse, iIndex - iStep);
        		break;
        	case 32:
        		if (iHoverDate != null && iHoverLoc != null) {
        			setSelected(iHoverDate, iHoverLoc, !isSelected(iHoverDate, iHoverLoc));
        		}
        	}
    	}
    }
 	
}