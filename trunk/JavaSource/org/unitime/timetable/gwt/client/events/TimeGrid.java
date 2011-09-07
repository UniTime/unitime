/*
 * UniTime 3.0 (University Timetabling Application)
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.ImageLink;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class TimeGrid extends Composite {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	private VerticalPanel iContainer;
	private SimplePanel iScrollPanel;
	private AbsolutePanel iPanel;
	private AbsolutePanel iGrid;
	private HorizontalPanel iHeader, iDock;
	private SimplePanel[] iSeparators = new SimplePanel[8];
	private SimplePanel iWorkingHours;
	private ImageLink iCalendar;
	
	private ArrayList<ArrayList<Meeting>> iMeetings = new ArrayList<ArrayList<Meeting>>();
	@SuppressWarnings("unchecked")
	private ArrayList<Meeting>[][] iMeetingTable = new ArrayList[7][24 * 60 / 5];
	
	private int iCellWidth = 150;
	private int iCellHeight = 60;
	private int iNrDays = 5;
	private int iStart = 0;
	private int iEnd = 24;
	private boolean iScroll = false;
	
	private ArrayList<MeetingClickHandler> iMeetingClickHandlers = new ArrayList<MeetingClickHandler>();
	private HashMap<Long, String> iColors = new HashMap<Long, String>();
	
	private boolean iOneWeek = false, iRoomResource = false;
	
	public TimeGrid() {
		this(new HashMap<Long, String>(), 5, (int) (0.9 * Window.getClientWidth() / 5), false, false, 0, 24);
	}
	
	private List<Label> iDayLabels = new ArrayList<Label>();
	
	public TimeGrid(HashMap<Long, String> colors, int nrDays, int cellWidth, boolean print, boolean scroll, int start, int end) {
		iColors = colors;
		iNrDays = nrDays;
		iCellWidth = cellWidth;
		iStart = start;
		iEnd = end;
		iScroll = scroll;
		
		iContainer = new VerticalPanel();
		iContainer.setStyleName("unitime-TimeGrid");
		iHeader = new HorizontalPanel();

		iDock = new HorizontalPanel();
		
		iPanel = new AbsolutePanel();
		AbsolutePanel times = new AbsolutePanel();
		
		for (int i = -1; i < iNrDays; i++) {
			SimplePanel sp = new SimplePanel();
			sp.setStyleName("header-time-interval");
			if (i < 0) {
				sp.setWidth("30px");
				iCalendar = new ImageLink();
				iCalendar.setImage(new Image(RESOURCES.calendar()));
				iCalendar.setTarget(null);
				iCalendar.setTitle("Export in iCalendar format.");
				iCalendar.setVisible(false);
				if (!print)
					sp.setWidget(iCalendar);
			} else {
				sp.setWidth(String.valueOf(iCellWidth));
				Label l = new Label(CONSTANTS.longDays()[i]);
				iDayLabels.add(l);
				sp.setWidget(l);
			}
			iHeader.add(sp);
		}
		
		iPanel.setSize(String.valueOf(iNrDays * iCellWidth + (scroll ? 0 : 5)), String.valueOf(iCellHeight * (iEnd - iStart)));
		times.setSize("30", String.valueOf(iCellHeight * (iEnd - iStart)));
		iGrid = new AbsolutePanel();
		iGrid.setSize("100%", "100%");
        
		iPanel.setStyleName("bottom-line");
		
		iWorkingHours = new SimplePanel();
		iWorkingHours.setStyleName("working-hours");
		iWorkingHours.setSize(String.valueOf(2 + iCellWidth * 5), String.valueOf(iCellHeight * 10));
		iGrid.add(iWorkingHours, 0, 15 * iCellHeight / 2 - (iCellHeight * iStart));
		
		for (int i = iStart; i < iEnd; i++) {
			
			//create major interval
			SimplePanel sp1 = new SimplePanel();
			sp1.setStyleName("major-time-interval");
			iGrid.add(sp1, 0, iCellHeight * (i - iStart));

			SimplePanel sp3 = new SimplePanel();
			sp3.setStyleName("dummy-time-interval");
			Label title = new Label(i == 0 ? "12am": i <= 11 ? i + "am" : i == 12 ? "12pm" : (i-12) + "pm");
			sp3.setWidget(title);
			times.add(sp3, 0, iCellHeight * (i - iStart));

			SimplePanel sp2 = new SimplePanel();
			sp2.setStyleName("minor-time-interval");
			iGrid.add(sp2, 0, iCellHeight * (i - iStart) + iCellHeight / 2);
			
			SimplePanel sp4 = new SimplePanel();
			sp4.setStyleName("dummy-time-interval");
			times.add(sp4, 0, iCellHeight * (i - iStart) + iCellHeight / 2);			
		}

		for (int day = 0; day < iNrDays + (scroll ? 0 : 1); day++) {
			iSeparators[day] = new SimplePanel();
			iSeparators[day].setStyleName("day-separator");
			iGrid.add(iSeparators[day], iCellWidth * day, 0);
		}
		
        iPanel.add(iGrid);
        
        iDock.add(times);
        iDock.add(iPanel);
        iContainer.add(iHeader);
        iHeader.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        
        if (scroll) {
    		iScrollPanel = new ScrollPanel();
        	iScrollPanel.setStyleName("calendar-scroll");
        } else {
        	iScrollPanel = new SimplePanel();
        }
        
    	iScrollPanel.add(iDock);
    	iContainer.add(iScrollPanel);
    	iScrollPanel.setWidth(String.valueOf(iNrDays * iCellWidth + 30 + (scroll ? ToolBox.getScrollBarWidth() : 5)));

        initWidget(iContainer);
	}
	
	public boolean isOneWeek() { return iOneWeek; }
	public void setOneWeek(boolean oneWeek) { iOneWeek = oneWeek; }
	public boolean isRoomResource() { return iRoomResource; }
	public void setRoomResource(boolean roomResource) { iRoomResource = roomResource; }
	
	public void setCalendarUrl(String url) {
		iCalendar.setUrl(url);
		iCalendar.setVisible(true);
	}
	
	public String getCalendarUrl() {
		return iCalendar.getUrl();
	}
	
	public TimeGrid getPrintWidget() {
		int firstHour = firstSlot() / 12;
		int lastHour = (11 + lastSlot()) / 12;
		TimeGrid tg = new TimeGrid(iColors, iNrDays, (int) (0.9 * Window.getClientWidth() / iNrDays), true, false, (firstHour < 7 ? firstHour : 7), (lastHour > 18 ? lastHour : 18));
		tg.setOneWeek(isOneWeek());
		tg.setRoomResource(isRoomResource());
		return tg;
	}
	
	public void addPrintEvent(EventInterface event) {
		addPrintEvent(event, event.getMeetings());
	}
	
	public void addPrintEvent(EventInterface event, Collection<MeetingInterface> meetings) {
		for (Meeting m : addEvent(event, meetings)) {
			m.setDummy();
			m.addStyleName("meeting-selected-noshadow");
		}
	}
	
	public void labelDays(WeekInterface first, WeekInterface last) {
		for (int i = 0; i < iDayLabels.size(); i++) {
			iDayLabels.get(i).setText(CONSTANTS.longDays()[i] +
					(first == null ? "" : " " + first.getDayNames().get(i)) +
					(last == null ? "" : " - " + last.getDayNames().get(i)));
		}
	}
	
	public void setNrDays(int days) {
		if (iNrDays == days) return;
		iNrDays = days;
		iCellWidth = (int)(0.9 * Window.getClientWidth() / days);
		iPanel.setWidth(String.valueOf(iNrDays * iCellWidth + (iScroll ? 0 : 5)));
		iWorkingHours.setWidth(String.valueOf(2 + iCellWidth * (iNrDays < 5 ? iNrDays : 5)));
        iScrollPanel.setWidth(String.valueOf(iNrDays * iCellWidth + 30 + (iScroll ? ToolBox.getScrollBarWidth() : 5)));
		for (int day = 0; day < 7 + (iScroll ? 0 : 1); day++) {
			if (day < days + (iScroll ? 0 : 1)) {
				if (iSeparators[day] == null) {
					iSeparators[day] = new SimplePanel();
					iSeparators[day].setStyleName("day-separator");
					iGrid.add(iSeparators[day], iCellWidth * day, 0);
				} else {
					DOM.setStyleAttribute(iSeparators[day].getElement(), "left", String.valueOf(iCellWidth * day));
					iSeparators[day].setVisible(true);
				}
			} else {
				if (iSeparators[day] != null) 
					iSeparators[day].setVisible(false);
			}
		}
		iHeader.clear();
		iDayLabels.clear();
		for (int i = -1; i < iNrDays; i++) {
			SimplePanel sp = new SimplePanel();
			sp.setStyleName("header-time-interval");
			if (i < 0) {
				if (iCalendar != null) 
					sp.setWidget(iCalendar);
				sp.setWidth("30px");
			} else {
				sp.setWidth(String.valueOf(iCellWidth));
				Label l = new Label(CONSTANTS.longDays()[i]);
				iDayLabels.add(l);
				sp.setWidget(l);
			}
			iHeader.add(sp);
		}
		for (ArrayList<Meeting> meetings: iMeetings)
			for (Meeting meeting: meetings) 
				meeting.move();
	}
	
	public int firstSlot() {
		for (int slot = 0; slot < 24 * 60 / 5; slot++) {
			for (int day = 0; day < 7; day++) {
				if (iMeetingTable[day][slot] != null && !iMeetingTable[day][slot].isEmpty())
					return slot;
			}
		}
		return 24 * 60 / 5 + 1;
	}
	
	public int lastSlot() {
		for (int slot = 24 * 60 / 5 - 1; slot >= 0; slot--) {
			for (int day = 0; day < 7; day++) {
				if (iMeetingTable[day][slot] != null && !iMeetingTable[day][slot].isEmpty())
					return slot;
			}
		}
		return 0;
	}

	public void shrink() {
		boolean hasSat = false, hasSun = false;
		for (int slot = 0; slot < 24 * 60 / 5; slot++) {
			if (iMeetingTable[5][slot] !=null && !iMeetingTable[5][slot].isEmpty()) hasSat = true;
			if (iMeetingTable[6][slot] !=null && !iMeetingTable[6][slot].isEmpty()) hasSun = true;
		}
		if (!hasSat && !hasSun) setNrDays(5);
		else if (!hasSun) setNrDays(6);
		else setNrDays(7);
	}
	
	public String getWidth() {
		return String.valueOf(iNrDays * iCellWidth + 30 + ToolBox.getScrollBarWidth());
	}
	
	public void scrollDown() {
        iScrollPanel.setWidth(String.valueOf(iNrDays * iCellWidth + 30 + ToolBox.getScrollBarWidth()));
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				if (iScrollPanel instanceof ScrollPanel)
					((ScrollPanel)iScrollPanel).setVerticalScrollPosition(350);
			}
		});
	}
	
	public void clear() {
		for (ArrayList<Meeting> meetings: iMeetings)
			for (Meeting meeting: meetings)
				iPanel.remove(meeting);
		iMeetings.clear();
		for (int i = 0; i < iMeetingTable.length; i++)
			for (int j = 0 ; j < iMeetingTable[i].length; j++)
				if (iMeetingTable[i][j] != null) iMeetingTable[i][j].clear();
	}
	
	public String getColor(EventInterface event) {
		String color = iColors.get(event.getId());
		if (color == null) {
			color = CONSTANTS.meetingColors()[iColors.size() % CONSTANTS.meetingColors().length];
			iColors.put(event.getId(), color);
		}
		return color;
	}
	
	protected Meeting addMeeting(EventInterface event, int day, int startSlot, int length, String name, ArrayList<String> note, String title, String color, ArrayList<Meeting> meetings) {
		int col = -1;
		for (int i = 0; i < length; i++) {
			if (iMeetingTable[day][startSlot + i] != null) {
				for (Iterator<Meeting> j = iMeetingTable[day][startSlot + i].iterator(); j.hasNext(); ) {
					Meeting m = j.next();
					if (m.getColumn() > col) col = m.getColumn();
				}
			}
		}
		col += 1;
		int cols = col + 1;
		if (col > 0) {
			boolean used[] = new boolean[cols - 1];
			for (int i = 0; i < cols - 1; i++) used[i] = false;
			for (int i = 0; i < length; i++) {
				if (iMeetingTable[day][startSlot + i] != null) {
					for (Iterator<Meeting> j = iMeetingTable[day][startSlot + i].iterator(); j.hasNext(); ) {
						Meeting m = j.next();
						used[m.getColumn()] = true;
					}
				}
			}
			for (int i = 0; i < cols - 1; i++)
				if (!used[i]) {col = i; cols--; break; }
		}
        Meeting meeting = new Meeting(event, name, note, day, startSlot, length, col, cols, meetings);
        meeting.addStyleName(color);

        meeting.setTitle(title);
        iPanel.add(meeting);
		for (int i = 0; i < length; i++) {
			if (iMeetingTable[day][startSlot + i] == null) iMeetingTable[day][startSlot + i] = new ArrayList<Meeting>();
			for (Iterator<Meeting> j = iMeetingTable[day][startSlot + i].iterator(); j.hasNext(); ) {
				Meeting m = j.next();
				m.setNrColumns(cols);
			}
			iMeetingTable[day][startSlot + i].add(meeting);
		}
        return meeting;
	}
	
	public ArrayList<Meeting> addEvent(EventInterface event) {
		return addEvent(event, event.getMeetings());
	}
	
	public ArrayList<Meeting> addEvent(EventInterface event, Collection<MeetingInterface> eventMeetings) {
		String color = getColor(event);
		final ArrayList<Meeting> done = new ArrayList<Meeting>();
		ArrayList<MeetingInterface> meetings = new ArrayList<MeetingInterface>(eventMeetings);
		while (!meetings.isEmpty()) {
			MeetingInterface meeting = null;
			TreeSet<MeetingInterface> dates = new TreeSet<MeetingInterface>(new Comparator<MeetingInterface>() {
				public int compare(MeetingInterface m1, MeetingInterface m2) {
					int cmp = new Integer(m1.getDayOfYear()).compareTo(new Integer(m2.getDayOfYear()));
					if (cmp != 0) return cmp;
					return m1.getId().compareTo(m2.getId());
				}
			});
			for (Iterator<MeetingInterface> i = meetings.iterator(); i.hasNext(); ) {
				MeetingInterface m = i.next();
				if (meeting == null) {
					meeting = m;
					dates.add(m);
					i.remove();
				} else if (meeting.getStartSlot() == m.getStartSlot() && meeting.getEndSlot() == m.getEndSlot() &&
						meeting.getDayOfWeek() == m.getDayOfWeek()) {
					dates.add(m);
					i.remove();
				}
			}
			String dateString = null;
			TreeSet<String> rooms = new TreeSet<String>();
			int lastDay = 0;
			String endDate = null;
			for (MeetingInterface m: dates) {
				if (m.getLocation() != null) rooms.add(m.getLocation().getName());
				if (dateString == null) {
					dateString = m.getMeetingDate();
					lastDay = m.getDayOfYear();
					endDate = null;
				} else if (lastDay == m.getDayOfYear()) {
				} else if (lastDay + 7 == m.getDayOfYear()) {
					endDate = m.getMeetingDate();
					lastDay = m.getDayOfYear();
				} else {
					if (endDate != null) dateString += " - " + endDate;
					dateString += ", " + m.getMeetingDate();
					lastDay = m.getDayOfYear();
					endDate = null;
				}
			}
			if (endDate != null) {
				dateString += " - " + endDate;
			}
			ArrayList<String> notes = new ArrayList<String>();
			notes.add(meeting.getMeetingTime());
			if (!iOneWeek)
				notes.add(dateString);
			String roomString = "";
			for (String room: rooms) {
				if (!roomString.isEmpty()) roomString += ", ";
				roomString += room;
			}
			if (!iRoomResource)
				notes.add(roomString);
			if (event.hasInstructor())
				notes.add(event.getInstructor().replace("|", "<br>"));
			if (event.hasSponsor())
				notes.add(event.getSponsor());
			done.add(addMeeting(
					event,
					meeting.getDayOfWeek(), meeting.getStartSlot(), 
					meeting.getEndSlot() - meeting.getStartSlot(),
					event.getName() + " (" + (event.hasInstruction() ? event.getInstruction() : event.getType()) + ")", 
					notes, (event.hasInstruction() ? event.getInstruction() : event.getType()) + " " + event.getName() + ": " + 
					dateString + " " + meeting.getMeetingTime() + " " + roomString, color, done));
		}
		iMeetings.add(done);
		return done;
	}
	
	public interface MeetingClickHandler {
		public void onMeetingClick(MeetingClickEvent event);
	}
	
	public class MeetingClickEvent {
		private Meeting iMeeting;
		MeetingClickEvent(Meeting meeting) {
			iMeeting = meeting;
		}
		public EventInterface getEvent() { return iMeeting.getEvent(); }
	}
	
	public void addMeetingClickHandler(MeetingClickHandler h) {
		iMeetingClickHandlers.add(h);
	}

	public class Meeting extends AbsolutePanel {
		private EventInterface iEvent;
		private int iColumn, iDay, iNrColumns;
		private int iLeft, iWidth;
		private ArrayList<Meeting> iMeetings;
		private HorizontalPanel iHeaderPanel;
		private boolean iDummy = false;
		
		private Meeting(EventInterface event, String name, ArrayList<String> note, int day, int start, int length, int column, int nrColumns, ArrayList<Meeting> meetings) {
			super();
			iEvent = event;
			iMeetings = meetings;
			iDay = day;
			iColumn = column;
			iNrColumns = nrColumns;
	    	iHeaderPanel = new HorizontalPanel();
	        iHeaderPanel.setStylePrimaryName("header");
	        Label nameLabel = new Label(name);
	        nameLabel.setStyleName("label");
	        iHeaderPanel.add(nameLabel);
	        SimplePanel mbot = new SimplePanel();
	        mbot.setStylePrimaryName("footer");
	        String notes = "";
	        String delim = "<br>";
	        if (note.size() > 2 && length < 12) delim = ", ";
	        if (note.size() > 3 && length < 18) delim = ", ";
	        for (String n: note) {
	        	if (n == null || n.isEmpty()) continue;
	        	if (notes.length() > 0) notes += delim;
	        	notes += "<span  style=\"white-space: nowrap\">" + n + "</span>";
	        }
	        mbot.add(new HTML(notes));
	        setStylePrimaryName("meeting");
	        add(iHeaderPanel);
	        add(mbot);
	        iWidth = (iCellWidth - 6) / nrColumns + (column + 1 != nrColumns && nrColumns > 1 ? -3 : 0);
	        setSize(String.valueOf(iWidth), String.valueOf(iCellHeight * length / 12 - 3));
	        DOM.setStyleAttribute(getElement(), "position", "absolute");
	        iLeft = 4 + iCellWidth * day + column * (iCellWidth - 6) / nrColumns;
			DOM.setStyleAttribute(getElement(), "left", String.valueOf(iLeft));
			DOM.setStyleAttribute(getElement(), "top", String.valueOf(1 + iCellHeight * start / 12 - iCellHeight * iStart));

			sinkEvents(Event.ONCLICK);
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEMOVE);
			sinkEvents(Event.ONMOUSEOUT);
		}
		
		public EventInterface getEvent() {
			return iEvent;
		}
		
		public void addIcon(Widget image) {
			iHeaderPanel.add(image);
		}
		
		public void onBrowserEvent(Event event) {
			if (iDummy) return;
		    com.google.gwt.user.client.Element target = DOM.eventGetTarget(event);
		    boolean anchor = false;
		    for (; target != null; target = DOM.getParent(target)) {
		    	String tag = DOM.getElementProperty(target, "tagName");
		    	if (tag.equalsIgnoreCase("a")) {
		    		anchor = true;
		    		break;
		    	} else if (tag.equalsIgnoreCase("div")) {
		    		break;
		    	}
		    }
			EventTarget related = event.getRelatedEventTarget();
		    switch (DOM.eventGetType(event)) {
			case Event.ONCLICK:
				setWidth(String.valueOf(iWidth));
				DOM.setStyleAttribute(getElement(), "zIndex", "");
				DOM.setStyleAttribute(getElement(), "left", String.valueOf(iLeft));
				for (Meeting meeting: iMeetings)
					meeting.removeStyleName("meeting-selected");
				if (!anchor) {
					MeetingClickEvent e = new MeetingClickEvent(Meeting.this);
					for (MeetingClickHandler h: iMeetingClickHandlers)
						h.onMeetingClick(e);
				}
				break;
			case Event.ONMOUSEOVER:
		        if (related == null || !getElement().isOrHasChild((Element)related.cast())) {
					setWidth(String.valueOf(iCellWidth - 6));
					getElement().getStyle().setZIndex(1000);
					DOM.setStyleAttribute(getElement(), "left", String.valueOf(4 + iCellWidth * iDay));
					for (Meeting meeting: iMeetings)
						meeting.addStyleName("meeting-selected");
		        }
				break;
			case Event.ONMOUSEOUT:
		        if (related == null || !getElement().isOrHasChild((Element)related.cast())) {
					setWidth(String.valueOf(iWidth));
					DOM.setStyleAttribute(getElement(), "zIndex", "");
					DOM.setStyleAttribute(getElement(), "left", String.valueOf(iLeft));
					for (Meeting meeting: iMeetings)
						meeting.removeStyleName("meeting-selected");
		        }
				break;
			case Event.ONMOUSEMOVE:
				int relativeX = event.getClientX() - getElement().getAbsoluteLeft() + getElement().getScrollLeft() + getElement().getOwnerDocument().getScrollLeft();
				if (relativeX < iLeft - 6 - iDay * iCellWidth || relativeX > iLeft - 2 - iDay * iCellWidth + iWidth) {
					setWidth(String.valueOf(iWidth));
					DOM.setStyleAttribute(getElement(), "zIndex", "");
					DOM.setStyleAttribute(getElement(), "left", String.valueOf(iLeft));
				}
				break;
			}
		    super.onBrowserEvent(event);
		}
		
		public void setNrColumns(int nrColumns) {
			if (nrColumns == iNrColumns) return;
			iNrColumns = nrColumns;
			move();
		}
		
		public void move() {
	        iWidth = (iCellWidth - 6) / iNrColumns + (iColumn + 1 != iNrColumns && iNrColumns > 1 ? -3 : 0);
	        iLeft = 4 + iCellWidth * iDay + iColumn * (iCellWidth - 6) / iNrColumns;
			setWidth(String.valueOf(iWidth));
			DOM.setStyleAttribute(getElement(), "left", String.valueOf(iLeft));
		}
		
		public void setTitle(String title) {
			super.setTitle(title);
		}
		
		public int getColumn() {
			return iColumn;
		}
		
		public int getDay() {
			return iDay;
		}
		
		public int getNrColumns() {
			return iNrColumns;
		}
		
		public void add(Widget w) {
            super.add(w, getElement());
        }
		
		public void setDummy() {
			iDummy = true;
		}
	}
}
