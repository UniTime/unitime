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
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.events.EventCookie.EventFlag;
import org.unitime.timetable.gwt.client.events.MeetingTable.MeetingsSortBy;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MultiMeetingInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class EventTable extends UniTimeTable<EventInterface[]> {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	private static DateTimeFormat sDateFormatShort = DateTimeFormat.getFormat(CONSTANTS.eventDateFormatShort());
	private static DateTimeFormat sDateFormatLong = DateTimeFormat.getFormat(CONSTANTS.eventDateFormatLong());
	
	private boolean iShowMainContact = false;
	private String iSortBy = null; 

	public EventTable() {
		setStyleName("unitime-EventMeetings");
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		
		UniTimeTableHeader hTimes = new UniTimeTableHeader("&otimes;", HasHorizontalAlignment.ALIGN_CENTER);
		header.add(hTimes);
		
		hTimes.addOperation(new Operation() {
			@Override
			public void execute() {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						ch.setValue(true);
					}
				}
			}
			@Override
			public boolean isApplicable() {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (!ch.getValue()) return true;
					}
				}
				return false;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.opSelectAll();
			}
		});
		hTimes.addOperation(new Operation() {
			@Override
			public void execute() {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						ch.setValue(getData(row)[0].hasConflicts());
					}
				}
			}
			@Override
			public boolean isApplicable() {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						if (getData(row)[0].hasConflicts()) return true;
					}
				}
				return false;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.opSelectAllConflicting();
			}
		});
		hTimes.addOperation(new Operation() {
			@Override
			public void execute() {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						ch.setValue(false);
					}
				}
			}
			@Override
			public boolean isApplicable() {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) return true;
					}
				}
				return false;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.opClearSelection();
			}
		});
		hTimes.addOperation(new EventOperation() {
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public String getName() {
				return (hasSelection() ? MESSAGES.opApproveSelectedMeetings() : MESSAGES.opApproveAllMeetings());
			}
			@Override
			public boolean isApplicable(EventInterface event) {
				for (MeetingInterface meeting: event.getMeetings())
					if (meeting.isCanApprove()) return true;
				return false;
			}
			@Override
			public void execute(int row, EventInterface event) {
			}
		});
		hTimes.addOperation(new EventOperation() {
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public boolean allMustMatch(boolean hasSelection) {
				return false;
			}
			@Override
			public String getName() {
				return (hasSelection() ? MESSAGES.opInquireSelectedMeetings() : MESSAGES.opInquireAllMeetings());
			}
			@Override
			public boolean isApplicable(EventInterface event) {
				for (MeetingInterface meeting: event.getMeetings())
					if (meeting.isCanApprove()) return true;
				return false;
			}
			@Override
			public void execute(int row, EventInterface event) {
			}
		});
		hTimes.addOperation(new EventOperation() {
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return (hasSelection() ? MESSAGES.opRejectSelectedMeetings() : MESSAGES.opRejectAllMeetings());
			}
			public boolean isApplicable(EventInterface event) {
				for (MeetingInterface meeting: event.getMeetings())
					if (meeting.isCanApprove()) return true;
				return false;
			}
			@Override
			public void execute(int row, EventInterface event) {
			}
		});
		
		UniTimeTableHeader hName = new UniTimeTableHeader(MESSAGES.colName());
		header.add(hName);

		UniTimeTableHeader hSection = new UniTimeTableHeader(MESSAGES.colSection(), HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hSection);

		UniTimeTableHeader hType = new UniTimeTableHeader(MESSAGES.colType());
		header.add(hType);
		
		UniTimeTableHeader hDate = new UniTimeTableHeader(MESSAGES.colDate());
		header.add(hDate);
		

		UniTimeTableHeader hTimePub = new UniTimeTableHeader(MESSAGES.colPublishedTime()); 
		header.add(hTimePub);
		UniTimeTableHeader hTimeAll = new UniTimeTableHeader(MESSAGES.colAllocatedTime()); 
		header.add(hTimeAll);
		UniTimeTableHeader hTimeSetup = new UniTimeTableHeader(MESSAGES.colSetupTimeShort(), HasHorizontalAlignment.ALIGN_RIGHT); 
		header.add(hTimeSetup);
		UniTimeTableHeader hTimeTeardown = new UniTimeTableHeader(MESSAGES.colTeardownTimeShort(), HasHorizontalAlignment.ALIGN_RIGHT); 
		header.add(hTimeTeardown);

		UniTimeTableHeader hLocation = new UniTimeTableHeader(MESSAGES.colLocation());
		header.add(hLocation);
		
		UniTimeTableHeader hCapacity = new UniTimeTableHeader(MESSAGES.colCapacity());
		header.add(hCapacity);

		UniTimeTableHeader hEnrollment = new UniTimeTableHeader(MESSAGES.colEnrollment(), HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hEnrollment);

		UniTimeTableHeader hLimit = new UniTimeTableHeader(MESSAGES.colLimit(), HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hLimit);

		UniTimeTableHeader hSponsor = new UniTimeTableHeader(MESSAGES.colSponsorOrInstructor());
		header.add(hSponsor);

		UniTimeTableHeader hContact = new UniTimeTableHeader(MESSAGES.colMainContact());
		header.add(hContact);

		UniTimeTableHeader hApproval = new UniTimeTableHeader(MESSAGES.colApproval());
		header.add(hApproval);

		addRow(null, header);
		
		addHideOperation(hTimePub, EventFlag.SHOW_PUBLISHED_TIME);
		addHideOperation(hTimeAll, EventFlag.SHOW_ALLOCATED_TIME);
		addHideOperation(hTimeSetup, EventFlag.SHOW_SETUP_TIME);
		addHideOperation(hTimeTeardown, EventFlag.SHOW_TEARDOWN_TIME);
		addHideOperation(hCapacity, EventFlag.SHOW_CAPACITY);
		addHideOperation(hEnrollment, EventFlag.SHOW_ENROLLMENT);
		addHideOperation(hLimit, EventFlag.SHOW_LIMIT);
		addHideOperation(hContact, EventFlag.SHOW_MAIN_CONTACT);
		
		addSortByOperation(hName, EventSortBy.NAME);
		addSortByOperation(hSection, EventSortBy.SECTION);
		addSortByOperation(hType, EventSortBy.TYPE);
		addSortByOperation(hDate, EventSortBy.TYPE);
		addSortByOperation(hTimePub, EventSortBy.PUBLISHED_TIME);
		addSortByOperation(hTimeAll, EventSortBy.ALLOCATED_TIME);
		addSortByOperation(hTimeSetup, EventSortBy.SETUP_TIME);
		addSortByOperation(hTimeTeardown, EventSortBy.TEARDOWN_TIME);
		addSortByOperation(hLocation, EventSortBy.LOCATION);
		addSortByOperation(hCapacity, EventSortBy.CAPACITY);
		addSortByOperation(hEnrollment, EventSortBy.ENROLLMENT);
		addSortByOperation(hLimit, EventSortBy.LIMIT);
		addSortByOperation(hSponsor, EventSortBy.SPONSOR);
		addSortByOperation(hContact, EventSortBy.MAIN_CONTACT);
		addSortByOperation(hApproval, EventSortBy.APPROVAL);
		
		for (int i = 0; i < getCellCount(0); i++)
			getCellFormatter().setStyleName(0, i, "unitime-ClickableTableHeaderNoBorderLine");

		resetColumnVisibility();
	}
	
	private void add(EventInterface event, MeetingFilter filter) {
		TreeSet<MeetingInterface> meetings = new TreeSet<MeetingInterface>();
		boolean approvable = false;
		for (MeetingInterface meeting: event.getMeetings())
			if (filter == null || !filter.filter(meeting)) {
				meetings.add(meeting);
				if (meeting.isCanApprove()) approvable = true;
			}
		if (meetings.isEmpty()) return;

		List<Widget> row = new ArrayList<Widget>();
		if (approvable) {
			row.add(new CheckBoxCell());
			if (!isColumnVisible(0)) setColumnVisible(0, true);
		} else {
			row.add(new HTML("&nbsp;"));
		}

		if (event.hasCourseNames()) {
			String name = "";
			String section = "";
			if (event.getType() == EventType.Course) { name = event.getName(); section = "&nbsp;"; }
			for (String cn: event.getCourseNames())
				if (name.isEmpty()) {
					name += cn;
				} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
					name += "<br><span class='no-control'>" + cn + "</span>";
				} else {
					name += "<br>" + cn;
				}
			if (event.hasExternalIds())
				for (String ex: event.getExternalIds()) {
					if (section.isEmpty()) {
						section += ex;
					} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
						section += "<br><span class='no-control'>" + ex + "</span>";
					} else {
						section += "<br>" + ex;
					}
				}
			else if (event.hasSectionNumber())
				section = event.getSectionNumber();
			row.add(new HTML(name, false));
			row.add(new NumberCell(section));
			row.add(new Label(event.getInstruction() == null ? event.getType().getAbbreviation() : event.getInstruction(), false));
			if (!isColumnVisible(getHeader(MESSAGES.colSection()).getColumn())) setColumnVisible(getHeader(MESSAGES.colSection()).getColumn(), true);
		} else {
			row.add(new HTML(event.getName()));
			row.add(new HTML("&nbsp;"));
			row.add(new Label(event.getType().getAbbreviation(), false));
		}

		String[] mtgs = new String[] {"", "", "", "", "", "", ""};
		String approval = "", prevApproval = null;
		String[] prev = null;
		boolean prevPast = false;
		for (MultiMeetingInterface m: EventInterface.getMultiMeetings(meetings, true, true)) {
			String[] mtg = new String[] {
					m.getDays(CONSTANTS.days(), CONSTANTS.shortDays()) + " " + (m.getNrMeetings() == 1 ? sDateFormatLong.format(m.getFirstMeetingDate()) : sDateFormatShort.format(m.getFirstMeetingDate()) + " - " + sDateFormatLong.format(m.getLastMeetingDate())),
					MeetingTable.meetingTime(m.getMeetings().first()),
					MeetingTable.allocatedTime(m.getMeetings().first()),
					String.valueOf(m.getMeetings().first().getStartOffset()),
					String.valueOf(- m.getMeetings().first().getEndOffset()),
					m.getLocationNameWithHint(),
					(m.getMeetings().first().getLocation() == null ? "" : m.getMeetings().first().getLocation().hasSize() ? m.getMeetings().first().getLocation().getSize().toString() : "")
					};
			for (int i = 0; i < mtgs.length; i++) {
				mtgs[i] += (mtgs[i].isEmpty() ? "" : "<br>") + (prev != null && prevPast == m.isPast() && prev[i].equals(mtg[i]) ? "" : ((m.isPast() ? "<span class='past-meeting'>" : "") + mtg[i] + (m.isPast() ? "</span>" : "")));
			}
			approval += (approval.isEmpty() ? "" : "<br>") + (prev != null && prevPast == m.isPast() && prevApproval.equals(m.isApproved() ? sDateFormat.format(m.getApprovalDate()) : "") ? "" : 
					(m.isApproved() ?
					m.isPast() ? "<span class='past-meeting'>" + sDateFormat.format(m.getApprovalDate()) + "</span>" : sDateFormat.format(m.getApprovalDate()) :
					m.isPast() ? "<span class='not-approved-past'>" + MESSAGES.approvalNotApprovedPast() + "</span>" : "<span class='not-approved'>" + MESSAGES.approvalNotApproved() + "</span>"));
			prev = mtg; prevPast = m.isPast(); prevApproval = (m.isApproved() ? sDateFormat.format(m.getApprovalDate()) : "");
		}
		for (int i = 0; i < mtgs.length; i++) {
			if (i == 3 || i == 4 || i == 6)
				row.add(new NumberCell(mtgs[i]));
			else
				row.add(new HTML(mtgs[i], false));
		}
		
		if (event.hasEnrollment() && iShowMainContact) {
			row.add(new NumberCell(event.getEnrollment().toString()));
			if (!isColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn())) setColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn(), true);
		} else {
			row.add(new HTML("&nbsp;"));
		}

		if (event.hasMaxCapacity() && iShowMainContact) {
			row.add(new NumberCell(event.getMaxCapacity().toString()));
			if (!isColumnVisible(getHeader(MESSAGES.colLimit()).getColumn())) setColumnVisible(getHeader(MESSAGES.colLimit()).getColumn(), true);
		} else {
			row.add(new HTML("&nbsp;"));
		}
		
		if (event.hasInstructors()) {
			row.add(new HTML(event.getInstructorNames("<br>"), false));
		} else {
			row.add(new Label(event.hasSponsor() ? event.getSponsor().getName() : ""));
		}
		
		if (iShowMainContact) {
			row.add(new HTML(event.hasContact() ? event.getContact().getName() : "&nbsp;"));
		} else {
			row.add(new HTML("&nbsp;"));
		}
			
		row.add(new HTML(approval, false));
		
		int rowNumber = addRow(new EventInterface[] {event}, row);
		getRowFormatter().addStyleName(rowNumber, "event-row");
		for (int i = 0; i < getCellCount(rowNumber); i++)
			getCellFormatter().addStyleName(rowNumber, i, "event-cell");
		
		if (event.hasConflicts())
			for (EventInterface conflict: event.getConflicts())
				addConflict(event, conflict, filter);
	}
	
	private void addConflict(EventInterface parent, EventInterface event, MeetingFilter filter) {
		TreeSet<MeetingInterface> meetings = new TreeSet<MeetingInterface>();
		for (MeetingInterface meeting: event.getMeetings())
			if (filter == null || !filter.filter(meeting)) {
				meetings.add(meeting);
			}
		if (meetings.isEmpty()) return;

		List<Widget> row = new ArrayList<Widget>();
		
		row.add(new CenterredCell(MESSAGES.signConflict()));

		if (event.hasCourseNames()) {
			String name = "";
			String section = "";
			if (event.getType() == EventType.Course) { name = event.getName(); section = "&nbsp;"; }
			for (String cn: event.getCourseNames())
				if (name.isEmpty()) {
					name += cn;
				} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
					name += "<br><span class='no-control'>" + cn + "</span>";
				} else {
					name += "<br>" + cn;
				}
			if (event.hasExternalIds())
				for (String ex: event.getExternalIds()) {
					if (section.isEmpty()) {
						section += ex;
					} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
						section += "<br><span class='no-control'>" + ex + "</span>";
					} else {
						section += "<br>" + ex;
					}
				}
			else if (event.hasSectionNumber())
				section = event.getSectionNumber();
			row.add(new HTML(name, false));
			row.add(new NumberCell(section));
			row.add(new Label(event.getInstruction() == null ? event.getType().getAbbreviation() : event.getInstruction(), false));
			if (!isColumnVisible(getHeader(MESSAGES.colSection()).getColumn())) setColumnVisible(getHeader(MESSAGES.colSection()).getColumn(), true);
		} else {
			row.add(new HTML(event.getName()));
			row.add(new HTML("&nbsp;"));
			row.add(new Label(event.getType().getAbbreviation(), false));
		}

		String[] mtgs = new String[] {"", "", "", "", "", ""};
		String approval = "", prevApproval = null;
		String[] prev = null;
		boolean prevPast = false;
		for (MultiMeetingInterface m: EventInterface.getMultiMeetings(meetings, true, true)) {
			String[] mtg = new String[] {
					m.getDays(CONSTANTS.days(), CONSTANTS.shortDays()) + " " + (m.getNrMeetings() == 1 ? sDateFormatLong.format(m.getFirstMeetingDate()) : sDateFormatShort.format(m.getFirstMeetingDate()) + " - " + sDateFormatLong.format(m.getLastMeetingDate())),
					MeetingTable.meetingTime(m.getMeetings().first()),
					MeetingTable.allocatedTime(m.getMeetings().first()),
					String.valueOf(m.getMeetings().first().getStartOffset()),
					String.valueOf(- m.getMeetings().first().getEndOffset()),
					m.getLocationNameWithHint()
					};
			for (int i = 0; i < mtgs.length; i++) {
				mtgs[i] += (mtgs[i].isEmpty() ? "" : "<br>") + (prev != null && prevPast == m.isPast() && prev[i].equals(mtg[i]) ? "" : ((m.isPast() ? "<span class='past-meeting'>" : "") + mtg[i] + (m.isPast() ? "</span>" : "")));
			}
			approval += (approval.isEmpty() ? "" : "<br>") + (prev != null && prevPast == m.isPast() && prevApproval.equals(m.isApproved() ? sDateFormat.format(m.getApprovalDate()) : "") ? "" : 
					(m.isApproved() ?
					m.isPast() ? "<span class='past-meeting'>" + sDateFormat.format(m.getApprovalDate()) + "</span>" : sDateFormat.format(m.getApprovalDate()) :
					m.isPast() ? "<span class='not-approved-past'>" + MESSAGES.approvalNotApprovedPast() + "</span>" : "<span class='not-approved'>" + MESSAGES.approvalNotApproved() + "</span>"));
			prev = mtg; prevPast = m.isPast(); prevApproval = (m.isApproved() ? sDateFormat.format(m.getApprovalDate()) : "");
		}
		for (int i = 0; i < mtgs.length; i++) {
			if (i == 3 || i == 4)
				row.add(new NumberCell(mtgs[i]));
			else
				row.add(new HTML(mtgs[i], false));
		}
		
		if (event.hasEnrollment() && iShowMainContact) {
			row.add(new NumberCell(event.getEnrollment().toString()));
			if (!isColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn())) setColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn(), true);
		} else {
			row.add(new HTML("&nbsp;"));
		}

		if (event.hasMaxCapacity() && iShowMainContact) {
			row.add(new NumberCell(event.getMaxCapacity().toString()));
			if (!isColumnVisible(getHeader(MESSAGES.colLimit()).getColumn())) setColumnVisible(getHeader(MESSAGES.colLimit()).getColumn(), true);
		} else {
			row.add(new HTML("&nbsp;"));
		}
		
		if (event.hasInstructors()) {
			row.add(new HTML(event.getInstructorNames("<br>"), false));
		} else {
			row.add(new Label(event.hasSponsor() ? event.getSponsor().getName() : ""));
		}
		
		if (iShowMainContact) {
			row.add(new HTML(event.hasContact() ? event.getContact().getName() : "&nbsp;"));
		} else {
			row.add(new HTML("&nbsp;"));
		}
			
		row.add(new HTML(approval, false));
		
		int rowNumber = addRow(new EventInterface[] {parent, event}, row);
		getRowFormatter().addStyleName(rowNumber, "conflict");
		for (int i = 0; i < getCellCount(rowNumber); i++)
			getCellFormatter().addStyleName(rowNumber, i, "conflict-cell");
	}
	
	public void resetColumnVisibility() {
		setColumnVisible(0, false);
		setColumnVisible(getHeader(MESSAGES.colSection()).getColumn(), false);
		setColumnVisible(getHeader(MESSAGES.colPublishedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
		setColumnVisible(getHeader(MESSAGES.colAllocatedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_ALLOCATED_TIME));
		setColumnVisible(getHeader(MESSAGES.colSetupTimeShort()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_SETUP_TIME));
		setColumnVisible(getHeader(MESSAGES.colTeardownTimeShort()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_TEARDOWN_TIME));
		setColumnVisible(getHeader(MESSAGES.colMainContact()).getColumn(), iShowMainContact && EventCookie.getInstance().get(EventFlag.SHOW_MAIN_CONTACT));
		setColumnVisible(getHeader(MESSAGES.colLimit()).getColumn(), iShowMainContact && EventCookie.getInstance().get(EventFlag.SHOW_LIMIT));
		setColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn(), iShowMainContact && EventCookie.getInstance().get(EventFlag.SHOW_ENROLLMENT));
		setColumnVisible(getHeader(MESSAGES.colCapacity()).getColumn(), iShowMainContact && EventCookie.getInstance().get(EventFlag.SHOW_CAPACITY));
	}
	
	public void populateTable(List<EventInterface> events, MeetingFilter filter, boolean showMainContact) {
		clearTable(1);
		iShowMainContact = showMainContact;
		resetColumnVisibility();
		if (events != null)
			for (EventInterface event: events)
				add(event, filter);
		if (iSortBy != null)
			sort(createComparator(EventSortBy.valueOf(iSortBy)));
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public String getSortBy() { return iSortBy; }
	public void setSortBy(String sortBy) {
		iSortBy = sortBy;
		if (iSortBy != null)
			sort(createComparator(EventSortBy.valueOf(iSortBy)));
	}
	
	public static enum EventSortBy {
		NAME, SECTION, TYPE, DATE, PUBLISHED_TIME, ALLOCATED_TIME, SETUP_TIME, TEARDOWN_TIME, LOCATION, CAPACITY, SPONSOR, MAIN_CONTACT, APPROVAL, LIMIT, ENROLLMENT
	}
	
	protected void onSortByChanded() {}
	
	protected void addSortByOperation(final UniTimeTableHeader header, final EventSortBy sortBy) {
		final Comparator<EventInterface[]> comparator = createComparator(sortBy);
		header.addOperation(new Operation() {
			@Override
			public void execute() { sort(comparator); iSortBy = sortBy.name(); onSortByChanded(); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(header.getHTML()); }
		});
	}
	
	private boolean iFirstHideOperation = true;
	protected void addHideOperation(final UniTimeTableHeader header, final EventFlag flag) {
		final boolean separator = iFirstHideOperation; iFirstHideOperation = false;
		Operation op = new Operation() {
			@Override
			public void execute() {
				boolean visible = isColumnVisible(header.getColumn());
				setColumnVisible(header.getColumn(), !visible);
				EventCookie.getInstance().set(flag, !visible);
				if (flag == EventFlag.SHOW_ALLOCATED_TIME && visible) {
					UniTimeTableHeader other = getHeader(MESSAGES.colPublishedTime());
					if (!isColumnVisible(other.getColumn())) {
						setColumnVisible(other.getColumn(), true);
						EventCookie.getInstance().set(EventFlag.SHOW_PUBLISHED_TIME, true);
					}
				} else if (flag == EventFlag.SHOW_PUBLISHED_TIME && visible) {
					UniTimeTableHeader other = getHeader(MESSAGES.colAllocatedTime());
					if (!isColumnVisible(other.getColumn())) {
						setColumnVisible(other.getColumn(), true);
						EventCookie.getInstance().set(EventFlag.SHOW_ALLOCATED_TIME, true);
					}
				}
			}
			@Override
			public boolean isApplicable() {
				switch (flag) {
				case SHOW_LIMIT:
				case SHOW_ENROLLMENT:
				case SHOW_MAIN_CONTACT:
				case SHOW_CAPACITY:
					return iShowMainContact;
				default:
					return true;
				}
			}
			@Override
			public boolean hasSeparator() { return separator; }
			@Override
			public String getName() { return isColumnVisible(header.getColumn()) ? MESSAGES.opHide(header.getHTML()) : MESSAGES.opShow(header.getHTML()); }
		};
		getHeader(null).addOperation(op);
		getHeader(MESSAGES.colName()).addOperation(ifNotSelectable(op));
		switch (flag) {
		case SHOW_ALLOCATED_TIME:
		case SHOW_PUBLISHED_TIME:
		case SHOW_SETUP_TIME:
		case SHOW_TEARDOWN_TIME:
			getHeader(MESSAGES.colAllocatedTime()).addOperation(op);
			getHeader(MESSAGES.colPublishedTime()).addOperation(op);
			getHeader(MESSAGES.colSetupTimeShort()).addOperation(op);
			getHeader(MESSAGES.colTeardownTimeShort()).addOperation(op);
			break;
		case SHOW_CAPACITY:
		case SHOW_LIMIT:
		case SHOW_ENROLLMENT:
			getHeader(MESSAGES.colLocation()).addOperation(op);
			header.addOperation(op);
			break;
		case SHOW_MAIN_CONTACT:
			getHeader(MESSAGES.colApproval()).addOperation(op);
			header.addOperation(op);
			break;
		default:
			header.addOperation(op);
		}
	}
	
	private Operation ifNotSelectable(final Operation op) {
		return new Operation() {
			@Override
			public void execute() {
				op.execute();
			}
			@Override
			public String getName() {
				return op.getName();
			}
			@Override
			public boolean isApplicable() {
				return op.isApplicable() && !isColumnVisible(getHeader(null).getColumn());
			}
			@Override
			public boolean hasSeparator() {
				return op.hasSeparator();
			}
		};
	}
		
	protected Comparator<EventInterface[]> createComparator(final EventSortBy sortBy) {
		return new Comparator<EventInterface[]>() {
			@Override
			public int compare(EventInterface[] o1, EventInterface[] o2) {
				int cmp = EventTable.compare(o1[0], o2[0], sortBy);
				if (cmp != 0) return cmp;
				if (o1.length == 2) {
					if (o2.length == 2) return EventTable.compare(o1[1], o2[1], sortBy);
					else return 1; 
				} else {
					return -1;
				}
			}
		};
	}
	
	public static int compareMeetings(EventInterface o1, EventInterface o2, MeetingsSortBy sortBy) {
		Iterator<MeetingInterface> i1 = o1.getMeetings().iterator(), i2 = o2.getMeetings().iterator();
		while (i1.hasNext() && i2.hasNext()) {
			int cmp = MeetingTable.compare(i1.next(), i2.next(), sortBy);
			if (cmp != 0) return cmp;
		}
		return (i1.hasNext() ? i2.hasNext() ? 0 : 1 : i2.hasNext() ? -1 : 0);
	}
	
	private static int compare(String s1, String s2) {
		if (s1 == null || s1.isEmpty()) {
			return (s2 == null || s2.isEmpty() ? 0 : 1);
		} else {
			return (s2 == null || s2.isEmpty() ? -1 : s1.compareToIgnoreCase(s2));
		}
	}
	
	public static int compare(EventInterface o1, EventInterface o2, EventSortBy sortBy) {
		int cmp;
		switch (sortBy) {
		case NAME:
			cmp = o1.getName().compareTo(o2.getName());
			if (cmp != 0) return cmp;
			cmp = o1.getType().compareTo(o2.getType());
			if (cmp != 0) return cmp;
			break;
		case SECTION:
			if (o1.hasExternalIds()) {
				if (o2.hasExternalIds()) {
					cmp = o1.getExternalIds().get(0).compareTo(o2.getExternalIds().get(0));
					if (cmp != 0) return cmp;
				} else return -1;
			} else if (o2.hasExternalIds()) return 1;
		case TYPE:
			cmp = o1.getType().compareTo(o2.getType());
			if (cmp != 0) return cmp;
			if (o1.getInstructionType() != null) {
				cmp = o1.getInstructionType().compareTo(o2.getInstructionType());
				if (cmp != 0) return cmp;
			}
			break;
		case DATE:
			cmp = compareMeetings(o1, o2, MeetingsSortBy.DATE);
			if (cmp != 0) return cmp;
			break;
		case PUBLISHED_TIME:
			cmp = compareMeetings(o1, o2, MeetingsSortBy.PUBLISHED_TIME);
			if (cmp != 0) return cmp;
			break;
		case ALLOCATED_TIME:
			cmp = compareMeetings(o1, o2, MeetingsSortBy.ALLOCATED_TIME);
			if (cmp != 0) return cmp;
			break;
		case SETUP_TIME:
			cmp = compareMeetings(o1, o2, MeetingsSortBy.SETUP_TIME);
			if (cmp != 0) return cmp;
			break;
		case TEARDOWN_TIME:
			cmp = compareMeetings(o1, o2, MeetingsSortBy.TEARDOWN_TIME);
			if (cmp != 0) return cmp;
			break;
		case LOCATION:
			cmp = compareMeetings(o1, o2, MeetingsSortBy.LOCATION);
			if (cmp != 0) return cmp;
			break;
		case CAPACITY:
			cmp = compareMeetings(o1, o2, MeetingsSortBy.CAPACITY);
			if (cmp != 0) return cmp;
			break;
		case SPONSOR:
			cmp = compare(o1.getInstructorNames("|"), o2.getInstructorNames("|"));
			if (cmp != 0) return cmp;
			cmp = compare(o1.hasSponsor() ? o1.getSponsor().getName() : null, o2.hasSponsor() ? o2.getSponsor().getName() : null);
			if (cmp != 0) return cmp;
			break;
		case MAIN_CONTACT:
			cmp = compare(o1.hasContact() ? o1.getContact().getName() : null, o2.hasContact() ? o2.getContact().getName() : null);
			if (cmp != 0) return cmp;
			break;
		case APPROVAL:
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
			cmp = f1.compareTo(f2);
			if (cmp != 0) return cmp;
			if (d1 != null && d2 != null) {
				cmp = d1.compareTo(d2);
				if (cmp != 0) return cmp;
			}
			break;
		case LIMIT:
			cmp = -(o1.hasMaxCapacity() ? o1.getMaxCapacity() : new Integer(0)).compareTo(o2.hasMaxCapacity() ? o2.getMaxCapacity() : new Integer(0));
			if (cmp != 0) return cmp;
			break;
		case ENROLLMENT:
			cmp = -(o1.hasEnrollment() ? o1.getEnrollment() : new Integer(0)).compareTo(o2.hasEnrollment() ? o2.getEnrollment() : new Integer(0));
			if (cmp != 0) return cmp;
			break;
		}
		return o1.compareTo(o2);
	}
	
	public interface MeetingFilter {
		public boolean filter(MeetingInterface meeting);
	}

	protected abstract class EventOperation implements Operation {
		@Override
		public void execute() {
			if (hasSelection()) {
				for (int row = getRowCount() - 1; row >= 1; row--) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) {
							EventInterface e = getData(row)[0];
							if (isApplicable(e)) execute(row, e);
						}
					}
				}
			} else if (allowNoSelection()) {
				for (int row = getRowCount() - 1; row >= 1; row--) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						EventInterface e = getData(row)[0];
						if (isApplicable(e)) execute(row, e);
					}
				}
			}
		}

		public boolean hasSelection() {
			for (int row = 1; row < getRowCount(); row++) {
				Widget w =  getWidget(row, 0);
				if (w != null && w instanceof CheckBox) {
					CheckBox ch = (CheckBox)w;
					if (ch.getValue()) return true;
				}
			}
			return false;
		}

		@Override
		public boolean isApplicable() {
			boolean hasSelection = hasSelection();
			boolean allMustMatch = allMustMatch(hasSelection);
			if (hasSelection) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) {
							EventInterface e = getData(row)[0];
							if (isApplicable(e)) {
								if (!allMustMatch) return true;
							} else {
								if (allMustMatch) return false;
							}
						}
					}
				}
				return allMustMatch;
			} else if (allowNoSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						EventInterface e = getData(row)[0];
						if (isApplicable(e)) {
							if (!allMustMatch) return true;
						} else {
							if (allMustMatch) return false;
						}
					}
				}
				return allMustMatch;
			} else {
				return false;
			}
		}
		
		public List<EventInterface> events() {
			List<EventInterface> events = new ArrayList<EventInterface>();
			if (hasSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) {
							EventInterface e = getData(row)[0];
							if (isApplicable(e)) events.add(e);
						}
					}
				}
			} else if (allowNoSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						EventInterface e = getData(row)[0];
						if (isApplicable(e)) events.add(e);
					}
				}
			}
			return events;
		}
		
		public List<Integer> rows() {
			List<Integer> rows = new ArrayList<Integer>();
			if (hasSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) {
							EventInterface e = getData(row)[0];
							if (isApplicable(e)) rows.add(row);
						}
					}
				}
			} else if (allowNoSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						EventInterface e = getData(row)[0];
						if (isApplicable(e)) rows.add(row);
					}
				}
			}
			return rows;
		}
		
		public boolean allMustMatch(boolean hasSelection) {
			return hasSelection;
		}
		
		public boolean allowNoSelection() {
			return false;
		}
		
		public abstract boolean isApplicable(EventInterface event);
		public abstract void execute(int row, EventInterface event);

		@Override
		public boolean hasSeparator() { return false; }
	}
	
	public EventInterface next(Long eventId) {
		boolean next = false;
		for (int row = 1; row < getRowCount(); row ++) {
			EventInterface[] data = getData(row);
			if (data.length == 1) {
				if (next)
					return data[0];
				else if (eventId.equals(data[0].getId()))
					next = true;
			}
		}
		return null;
	}
	
	public EventInterface previous(Long eventId) {
		EventInterface prev = null;
		for (int row = 1; row < getRowCount(); row ++) {
			EventInterface[] data = getData(row);
			if (data.length == 1) {
				if (eventId.equals(data[0].getId()))
					return prev;
				else
					prev = data[0];
			}
		}
		return null;
	}

}