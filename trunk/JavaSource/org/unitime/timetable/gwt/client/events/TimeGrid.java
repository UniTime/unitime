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

import org.unitime.timetable.gwt.client.widgets.ImageLink;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class TimeGrid extends Composite {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	private P iContainer;
	private ScrollPanel iScrollPanel;
	private P iPanel;
	private P iGrid;
	private P iHeader, iDock;
	private P iVLines;
	private P[] iSeparators = new P[7];
	private P iWorkingHours;
	private P iTimes;
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
	private int iTotalNrColumns = 0;
	
	private ArrayList<MeetingClickHandler> iMeetingClickHandlers = new ArrayList<MeetingClickHandler>();
	private HashMap<Long, String> iColors = new HashMap<Long, String>();
	
	private boolean iRoomResource = false;
	private List<WeekInterface> iSelectedWeeks = null;
	
	public static enum Mode {
		FILLSPACE,
		PROPORTIONAL,
		OVERLAP
	}
	
	private Mode iMode = Mode.FILLSPACE;
	
	public TimeGrid() {
		this(new HashMap<Long, String>(), 5, (int) (0.9 * Window.getClientWidth() / 5), false, false, 0, 24);
	}
	
	private List<P> iDayLabels = new ArrayList<P>();
	
	public TimeGrid(HashMap<Long, String> colors, int nrDays, int cellWidth, boolean print, boolean scroll, int start, int end) {
		iColors = colors;
		iNrDays = nrDays;
		iCellWidth = cellWidth;
		iStart = start;
		iEnd = end;
		iScroll = scroll;
		
		iContainer = new P("unitime-TimeGrid");
		iContainer.setSize(40 + iNrDays * iCellWidth, scroll ? 575 : 25 + iCellHeight * (iEnd - iStart));
		
		iHeader = new P("calendar-header");
		iHeader.setWidth(iNrDays * iCellWidth);

		iDock = new P("calendar-dock");
		iDock.setHeight(iCellHeight * (iEnd - iStart) + 5);
		
		iPanel = new P("calendar-panel");
		iPanel.setSize(iNrDays * iCellWidth + 5, iCellHeight * (iEnd - iStart));
		iTimes = new P("calendar-times");
		iTimes.setHeight(iCellHeight * (iEnd - iStart));

		iCalendar = new ImageLink();
		iCalendar.setImage(new Image(RESOURCES.calendar()));
		iCalendar.setTarget(null);
		iCalendar.setTitle(MESSAGES.exportICalendar());
		iCalendar.addStyleName("calendar");
		if (!print)
			iContainer.add(iCalendar);
		
		for (int i = 0; i < iNrDays; i++) {
			P sp = new P("header-time-interval");
			sp.setWidth(iCellWidth);
			sp.setText(CONSTANTS.longDays()[i]);
			iHeader.add(sp, i * iCellWidth, 0);
			iDayLabels.add(sp);
		}
		
		iGrid = new P("calendar-grid");
		
		iWorkingHours = new P("working-hours");
		iWorkingHours.setSize(iCellWidth * 5, iCellHeight * 10);
		iGrid.add(iWorkingHours, 0, 15 * iCellHeight / 2 - (iCellHeight * iStart));
		
		for (int i = iStart; i < iEnd; i++) {
			
			//create major interval
			P sp1 = new P("major-time-interval");
			iGrid.add(sp1, 0, iCellHeight * (i - iStart));

			P sp3 = new P("dummy-time-interval");
			sp3.setText(CONSTANTS.useAmPm() ? (i == 0 ? "12am": i <= 11 ? i + "am" : i == 12 ? "12pm" : (i-12) + "pm") : String.valueOf(i));
			iTimes.add(sp3, 0, iCellHeight * (i - iStart));

			P sp2 = new P("minor-time-interval");
			iGrid.add(sp2, 0, iCellHeight * (i - iStart) + 25);
			
			P sp4 = new P("dummy-time-interval");
			iTimes.add(sp4, 0, iCellHeight * (i - iStart) + 25);			
		}

		for (int day = 0; day < iNrDays; day++) {
			iSeparators[day] = new P("day-separator");
			iGrid.add(iSeparators[day], iCellWidth * day, 0);
		}
		P lastSeparator = new P("day-separator");
		lastSeparator.getElement().getStyle().setRight(0, Unit.PX);
		lastSeparator.getElement().getStyle().setTop(0, Unit.PX);
		lastSeparator.getElement().getStyle().setPosition(Position.ABSOLUTE);
		iGrid.add(lastSeparator);
		
        iPanel.add(iGrid, 0, 0);
        
        iDock.add(iTimes, 0, 0);
        iDock.add(iPanel, 30, 0);
        
        iContainer.add(iHeader, 30, 0);
        
        iVLines = new P("calendar-grid");
        iPanel.add(iVLines, 0, 0);

        if (scroll) {
    		iScrollPanel = new ScrollPanel(iDock);
        	iScrollPanel.setStyleName("calendar-scroll");
        	iContainer.add(iScrollPanel, 0, 20);
        } else {
        	iContainer.add(iDock, 0, 20);
        }

        initWidget(iContainer);
	}
	
	public List<WeekInterface> getSelectedWeeks() { return iSelectedWeeks; }
	public void setSelectedWeeks(List<WeekInterface> weeks) {
		iSelectedWeeks = weeks;
	}
	public boolean isRoomResource() { return iRoomResource; }
	public void setRoomResource(boolean roomResource) { iRoomResource = roomResource; }
	public void setMode(Mode mode) { iMode = mode; }
	public Mode getMode() { return iMode; }
	
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
		tg.setSelectedWeeks(getSelectedWeeks());
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
		iContainer.setWidth(40 + iNrDays * iCellWidth);
		iHeader.setWidth(iNrDays * iCellWidth);
		iPanel.setWidth(iNrDays * iCellWidth);
		for (int day = 0; day < 7 + (iScroll ? 0 : 1); day++) {
			if (day < days + (iScroll ? 0 : 1)) {
				if (iSeparators[day] == null) {
					iSeparators[day] = new P("day-separator");
					iGrid.add(iSeparators[day], iCellWidth * day, 0);
				} else {
					iSeparators[day].getElement().getStyle().setLeft(iCellWidth * day, Unit.PX);
					iSeparators[day].setVisible(true);
				}
			} else {
				if (iSeparators[day] != null) 
					iSeparators[day].setVisible(false);
			}
		}
		iHeader.clear();
		iDayLabels.clear();
		for (int i = 0; i < iNrDays; i++) {
			P sp = new P("header-time-interval");
			sp.setWidth(iCellWidth);
			sp.setText(CONSTANTS.longDays()[i]);
			iHeader.add(sp, i * iCellWidth, 0);
			iDayLabels.add(sp);
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
		
		iVLines.clear();
		if (iMode == Mode.OVERLAP && iSelectedWeeks != null && iSelectedWeeks.size() > 1) {
			for (int d = 0; d < iNrDays; d++) {
				for (int w = 0; w < iSelectedWeeks.size(); w++) {
					if (w > 0)
						iVLines.add(new P("week-separator"), 3 + iCellWidth * d + w * (iCellWidth - 6) / iSelectedWeeks.size(), 0);
					P p = new P("week-title"); p.setHTML(iSelectedWeeks.get(w).getDayNames().get(d).replaceAll("/", "<br>"));
					p.setWidth((iCellWidth - 6) / iSelectedWeeks.size());
					iVLines.add(p, 3 + iCellWidth * d + w * (iCellWidth - 6) / iSelectedWeeks.size(), 0);
				}
			}
		}
	}
	
	public void clear() {
		for (ArrayList<Meeting> meetings: iMeetings)
			for (Meeting meeting: meetings)
				iPanel.remove(meeting);
		iMeetings.clear();
		for (int i = 0; i < iMeetingTable.length; i++)
			for (int j = 0 ; j < iMeetingTable[i].length; j++)
				if (iMeetingTable[i][j] != null) iMeetingTable[i][j].clear();
		iTotalNrColumns = 0;
	}
	
	public String getColor(EventInterface event) {
		String color = iColors.get(event.getId());
		if (color == null) {
			color = CONSTANTS.meetingColors()[iColors.size() % CONSTANTS.meetingColors().length];
			iColors.put(event.getId(), color);
		}
		return color;
	}
	
	protected Meeting addMeeting(EventInterface event, int day, int startSlot, int length, String name, ArrayList<String> note, String title, String color, int firstWeekIndex, int nrMeetings, ArrayList<Meeting> meetings) {
		switch (iMode) {
		case PROPORTIONAL: {
			boolean used[] = new boolean[iTotalNrColumns + nrMeetings];
			for (int i = 0; i < used.length; i++) used[i] = false;
			for (int i = 0; i < length; i++) {
				if (iMeetingTable[day][startSlot + i] != null) {
					for (Iterator<Meeting> j = iMeetingTable[day][startSlot + i].iterator(); j.hasNext(); ) {
						Meeting m = j.next();
						for (int k = 0; k < m.getNrMeetings(); k++)
							used[m.getColumn() + k] = true;
					}
				}
			}
			int col = 0;
			i: for (int i = 0; i <= iTotalNrColumns; i ++) {
				for (int j = 0; j < nrMeetings; j++)
					if (used[i + j]) continue i;
				col = i;
				break;
			}
			if (iTotalNrColumns < col + nrMeetings) {
				iTotalNrColumns = col + nrMeetings;
				for (int i = 0; i < iPanel.getWidgetCount(); i++) {
					Widget w = iPanel.getWidget(i);
					if (w instanceof Meeting) ((Meeting)w).move();
				}
			}
			Meeting meeting = new Meeting(event, name, note, day, startSlot, length, col, 1, nrMeetings, meetings);
	        meeting.addStyleName(color);
	        meeting.setTitle(title);
	        iPanel.add(meeting);
			for (int i = 0; i < length; i++) {
				if (iMeetingTable[day][startSlot + i] == null) iMeetingTable[day][startSlot + i] = new ArrayList<Meeting>();
				iMeetingTable[day][startSlot + i].add(meeting);
			}
	        return meeting;
		}
		case FILLSPACE: {
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
	        Meeting meeting = new Meeting(event, name, note, day, startSlot, length, col, cols, 1, meetings);
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
		case OVERLAP: {
			int overlap = -1;
			for (int i = 0; i < length; i++) {
				if (iMeetingTable[day][startSlot + i] != null) {
					for (Iterator<Meeting> j = iMeetingTable[day][startSlot + i].iterator(); j.hasNext(); ) {
						Meeting m = j.next();
						if (m.getColumn() == firstWeekIndex && m.getNrColumns() > overlap) overlap = m.getNrColumns();
					}
				}
			}
			overlap += 1;
			int overlaps = overlap + 1;
			if (overlap > 0) {
				boolean used[] = new boolean[overlaps - 1];
				for (int i = 0; i < overlaps - 1; i++) used[i] = false;
				for (int i = 0; i < length; i++) {
					if (iMeetingTable[day][startSlot + i] != null) {
						for (Iterator<Meeting> j = iMeetingTable[day][startSlot + i].iterator(); j.hasNext(); ) {
							Meeting m = j.next();
							if (m.getColumn() == firstWeekIndex)
								used[m.getNrColumns()] = true;
						}
					}
				}
				for (int i = 0; i < overlaps - 1; i++)
					if (!used[i]) {overlap = i; overlaps--; break; }
			}
			
			Meeting meeting = new Meeting(event, name, note, day, startSlot, length, firstWeekIndex, overlap, nrMeetings, meetings);
	        meeting.addStyleName(color);
	        meeting.setTitle(title);
	        iPanel.add(meeting);
			for (int i = 0; i < length; i++) {
				if (iMeetingTable[day][startSlot + i] == null) iMeetingTable[day][startSlot + i] = new ArrayList<Meeting>();
				iMeetingTable[day][startSlot + i].add(meeting);
			}
	        return meeting;
		}
		default:
			return null;
		}
	}
	
	private int weekIndex(MeetingInterface m) {
		for (int i = 0; i < iSelectedWeeks.size(); i++) {
			if (iSelectedWeeks.get(i).getDayOfYear() <= m.getDayOfYear() && m.getDayOfYear() <= iSelectedWeeks.get(i).getDayOfYear() + 6)
				return i;
		}
		return -1;
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
			MeetingInterface prev = null;
			for (Iterator<MeetingInterface> i = meetings.iterator(); i.hasNext(); ) {
				MeetingInterface m = i.next();
				if (meeting == null) {
					meeting = m;
					prev = m;
					dates.add(m);
					i.remove();
				} else if (meeting.getStartSlot() == m.getStartSlot() && meeting.getEndSlot() == m.getEndSlot() &&
						meeting.getDayOfWeek() == m.getDayOfWeek()) {
					if (iMode == Mode.OVERLAP && (weekIndex(prev) != weekIndex(m) && weekIndex(prev) + 1 != weekIndex(m))) continue;
					dates.add(m);
					prev = m;
					i.remove();
				}
			}
			String dateString = null;
			TreeSet<String> rooms = new TreeSet<String>();
			int lastDay = 0;
			String endDate = null;
			TreeSet<Integer> days = new TreeSet<Integer>();
			for (MeetingInterface m: dates) {
				days.add(m.getDayOfYear());
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
			if (dates.size() != getSelectedWeeks().size())
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
					(meeting.isApproved() ? "" : "<i>") + event.getName() + " (" + (event.hasInstruction() ? event.getInstruction() : event.getType()) + ")" + (meeting.isApproved() ? "" : " -- not approved</i>"), 
					notes, (event.hasInstruction() ? event.getInstruction() : event.getType()) + " " + event.getName() + ": " + 
					dateString + " " + meeting.getMeetingTime() + " " + roomString, color, 
					weekIndex(meeting), days.size(), done));
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
		private int iNrMeetings;
		
		private Meeting(EventInterface event, String name, ArrayList<String> note, int day, int start, int length, int column, int nrColumns, int nrMeetings, ArrayList<Meeting> meetings) {
			super();
			iEvent = event;
			iMeetings = meetings;
			iDay = day;
			iColumn = column;
			iNrMeetings = nrMeetings;
			iNrColumns = nrColumns;
	    	iHeaderPanel = new HorizontalPanel();
	        iHeaderPanel.setStylePrimaryName("header");
	        HTML nameLabel = new HTML(name);
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
	        switch (iMode) {
	        case PROPORTIONAL:
		        iWidth = Math.max(3, iNrMeetings * (iCellWidth - 6) / iTotalNrColumns + (iColumn + iNrMeetings != iTotalNrColumns && iTotalNrColumns > 1 ? -3 : 0));
		        iLeft = 4 + iCellWidth * iDay + iColumn * (iCellWidth - 6) / iTotalNrColumns;
		        break;
	        case FILLSPACE:
		        iWidth = (iCellWidth - 6) / iNrColumns + (iColumn + 1 != iNrColumns && iNrColumns > 1 ? -3 : 0);
		        iLeft = 4 + iCellWidth * iDay + iColumn * (iCellWidth - 6) / iNrColumns;
		        break;
	        case OVERLAP:
	        	iWidth = iNrMeetings * (iCellWidth - 6) / iSelectedWeeks.size() + (iColumn + iNrMeetings != iSelectedWeeks.size() && iSelectedWeeks.size() > 1 ? -3 : 0) - 5 * iNrColumns;
	        	iLeft = 4 + iCellWidth * iDay + iColumn * (iCellWidth - 6) / iSelectedWeeks.size() + 5 * iNrColumns;
	        }
	        getElement().getStyle().setWidth(iWidth, Unit.PX);
	        getElement().getStyle().setHeight(iCellHeight * length / 12 - 3, Unit.PX);
	        getElement().getStyle().setPosition(Position.ABSOLUTE);
			getElement().getStyle().setLeft(iLeft, Unit.PX);
			getElement().getStyle().setTop(1 + iCellHeight * start / 12 - iCellHeight * iStart, Unit.PX);

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
				getElement().getStyle().setWidth(iWidth, Unit.PX);
				getElement().getStyle().clearZIndex();
				getElement().getStyle().setLeft(iLeft, Unit.PX);
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
					getElement().getStyle().setWidth(iCellWidth - 6, Unit.PX);
					getElement().getStyle().setZIndex(1000);
					getElement().getStyle().setLeft(4 + iCellWidth * iDay, Unit.PX);
					for (Meeting meeting: iMeetings)
						meeting.addStyleName("meeting-selected");
		        }
				break;
			case Event.ONMOUSEOUT:
		        if (related == null || !getElement().isOrHasChild((Element)related.cast())) {
					getElement().getStyle().setWidth(iWidth, Unit.PX);
					getElement().getStyle().clearZIndex();
					getElement().getStyle().setLeft(iLeft, Unit.PX);
					for (Meeting meeting: iMeetings)
						meeting.removeStyleName("meeting-selected");
		        }
				break;
			case Event.ONMOUSEMOVE:
				int relativeX = event.getClientX() - getElement().getAbsoluteLeft() + getElement().getScrollLeft() + getElement().getOwnerDocument().getScrollLeft();
				if (relativeX < iLeft - 6 - iDay * iCellWidth || relativeX > iLeft - 2 - iDay * iCellWidth + iWidth) {
					getElement().getStyle().setWidth(iWidth, Unit.PX);
					getElement().getStyle().clearZIndex();
					getElement().getStyle().setLeft(iLeft, Unit.PX);
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
	        switch (iMode) {
	        case PROPORTIONAL:
	        	iWidth = Math.max(3, iNrMeetings * (iCellWidth - 6) / iTotalNrColumns + (iColumn + iNrMeetings != iTotalNrColumns && iTotalNrColumns > 1 ? -3 : 0));
	        	iLeft = 4 + iCellWidth * iDay + iColumn * (iCellWidth - 6) / iTotalNrColumns;
	        	break;
	        case FILLSPACE:
		        iWidth = (iCellWidth - 6) / iNrColumns + (iColumn + 1 != iNrColumns && iNrColumns > 1 ? -3 : 0);
		        iLeft = 4 + iCellWidth * iDay + iColumn * (iCellWidth - 6) / iNrColumns;
		        break;
	        case OVERLAP:
	        	iWidth = iNrMeetings * (iCellWidth - 6) / iSelectedWeeks.size() + (iColumn + iNrMeetings != iSelectedWeeks.size() && iSelectedWeeks.size() > 1 ? -3 : 0) - 5 * iNrColumns;
	        	iLeft = 4 + iCellWidth * iDay + iColumn * (iCellWidth - 6) / iSelectedWeeks.size() + 5 * iNrColumns;
	        	break;
	        }
			getElement().getStyle().setWidth(iWidth, Unit.PX);
			getElement().getStyle().setLeft(iLeft, Unit.PX);
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
		
		public int getNrMeetings() {
			return iNrMeetings;
		}
	}
}
