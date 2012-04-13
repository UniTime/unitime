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
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConglictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class MeetingTable extends UniTimeTable<MeetingInterface[]>{
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.meetingDateFormat());
	
	private Command iAddMeetingsCommand;

	public MeetingTable() {
		setStyleName("unitime-EventMeetings");
		List<Widget> header = new ArrayList<Widget>();
		UniTimeTableHeader hTimes = new UniTimeTableHeader("&otimes;", HasHorizontalAlignment.ALIGN_CENTER);
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
		hTimes.addOperation(new Operation() {
			@Override
			public void execute() {
				for (int row = getRowCount() - 1; row >= 1; row--) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox && getData(row)[0].getId() == null) {
						while (row + 1 < getRowCount() && getData(row + 1).length == 2)
							removeRow(row + 1);
						removeRow(row);
					}
				}
			}
			@Override
			public boolean isApplicable() {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox && getData(row)[0].getId() == null) return true;
				}
				return false;
			}
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public String getName() {
				return MESSAGES.opDeleteNewMeetings();
			}
		});
		hTimes.addOperation(new Operation() {
			@Override
			public void execute() {
				for (int row = getRowCount() - 1; row >= 1; row--) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue() && getData(row)[0].getId() == null) {
							while (row + 1 < getRowCount() && getData(row + 1).length == 2)
								removeRow(row + 1);
							removeRow(row);
						}
					}
				}
			}
			@Override
			public boolean isApplicable() {
				boolean selected = false, allNew = true;
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) {
							selected = true;
							if (getData(row)[0].getId() != null) allNew = false;
						}
					}
				}
				return selected && allNew;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.opDeleteSelectedMeetings();
			}
		});
		hTimes.addOperation(new Operation() {
			@Override
			public void execute() {
				iAddMeetingsCommand.execute();
			}
			@Override
			public boolean isApplicable() {
				return iAddMeetingsCommand != null;
			}
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public String getName() {
				return MESSAGES.opAddMeetings();
			}
		});
		
		header.add(hTimes);
		UniTimeTableHeader hDate = new UniTimeTableHeader(MESSAGES.colDate());
		header.add(hDate);
		UniTimeTableHeader hTime = new UniTimeTableHeader(MESSAGES.colTime()); 
		header.add(hTime);
		UniTimeTableHeader hLocation = new UniTimeTableHeader(MESSAGES.colLocation());
		header.add(hLocation);
		UniTimeTableHeader hCapacity = new UniTimeTableHeader(MESSAGES.colCapacity(), HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hCapacity);
		UniTimeTableHeader hApproval = new UniTimeTableHeader(MESSAGES.colApproval());
		header.add(hApproval);
		
		hDate.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.DATE)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return false; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colDate()); }
		});
		hTime.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.TIME)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return false; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colTime()); }
		});
		hLocation.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.LOCATION)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return false; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colLocation()); }
		});
		hCapacity.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.CAPACITY)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return false; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colCapacity()); }
		});
		hApproval.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.APPROVAL)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return false; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colApproval()); }
		});
		
		addRow(null, header);
	}
	
	public void setAddMeetingsCommand(Command command) {
		iAddMeetingsCommand = command;
	}
	
	public boolean hasMeeting(MeetingInterface meeting) {
		for (int row = 1; row < getRowCount(); row++)
			if (meeting.equals(getData(row)[0])) return true;
		return false;
	}
	
	public void add(MeetingInterface meeting) {
		List<Widget> row = new ArrayList<Widget>();
		row.add(new CheckBoxCell());
		row.add(new Label(sDateFormat.format(meeting.getMeetingDate())));
		row.add(new Label(meeting.getMeetingTime()));
		if (meeting.getLocation() == null) {
			row.add(new Label(""));
			row.add(new Label(""));
		} else {
			row.add(new Label(meeting.getLocationName()));
			row.add(new NumberCell(meeting.getLocation().getSize() == null ? "N/A" : meeting.getLocation().getSize().toString()));
		}
		row.add(new Label(meeting.getId() == null ? MESSAGES.approvalNewMeeting() : meeting.getApprovalDate() == null ? MESSAGES.approvalNotApproved() : sDateFormat.format(meeting.getApprovalDate())));
		if (meeting.getId() == null)
			row.get(row.size() - 1).addStyleName("new-meeting");
		else if (!meeting.isApproved())
			row.get(row.size() - 1).addStyleName("not-approved");
		int meetingRow = addRow(new MeetingInterface[] {meeting}, row);
		if (meeting.isPast())
			getRowFormatter().addStyleName(meetingRow, "past-meeting");
		if (meeting.hasConflicts()) {
			for (MeetingConglictInterface m: meeting.getConflicts()) {
				List<Widget> r = new ArrayList<Widget>();
				r.add(new Label(""));
				r.add(new Label(MESSAGES.conflictWith(m.getName()), false));
				r.add(new Label(m.getMeetingTime()));
				r.add(new Label(""));
				r.add(new NumberCell(""));
				r.add(new Label(m.getApprovalDate() == null ? MESSAGES.approvalNotApproved() : sDateFormat.format(m.getApprovalDate())));
				if (!m.isApproved())
					r.get(r.size() - 1).addStyleName("not-approved");
				r.get(1).getElement().getStyle().setPaddingLeft(5, Unit.PX);
				int conflictRow = addRow(new MeetingInterface[] {meeting, m}, r);
				getRowFormatter().addStyleName(conflictRow, "conflict");
			}
		}
	}
	
	public int compare(MeetingInterface[] m1, MeetingInterface[] m2, SortBy sortBy) {
		int cmp = compare(m1[0], m2[0], sortBy);
		if (cmp != 0) return cmp;
		if (m1.length == 2) {
			if (m2.length == 2) return compare(m1[1], m2[1], sortBy);
			else return 1; 
		} else {
			return -1;
		}
	}
	
	public static enum SortBy {
		DATE, TIME, LOCATION, CAPACITY, APPROVAL
	}
	
	protected Comparator<MeetingInterface[]> createComparator(final SortBy sortyBy) {
		return new Comparator<MeetingInterface[]>() {
			@Override
			public int compare(MeetingInterface[] o1, MeetingInterface[] o2) {
				return MeetingTable.this.compare(o1, o2, sortyBy);
			}
		};
	}
	
	public int compare(MeetingInterface m1, MeetingInterface m2, SortBy sortBy) {
		int cmp;
		switch (sortBy) {
		case APPROVAL:
			if (m1.getId() == null && m2.getId() != null) return -1;
			if (m1.getId() != null && m2.getId() == null) return 1;
			Date now = new Date();
			cmp = (m1.getApprovalDate() == null ? now : m1.getApprovalDate()).compareTo(m2.getApprovalDate() == null ? now : m2.getApprovalDate());
			if (cmp != 0) return cmp;
		case DATE:
			if (m1 instanceof MeetingConglictInterface && m2 instanceof MeetingConglictInterface) {
				cmp = ((MeetingConglictInterface)m1).getName().compareTo(((MeetingConglictInterface)m2).getName());
				if (cmp != 0) return cmp;
			}
			cmp = m1.getMeetingDate().compareTo(m2.getMeetingDate());
			if (cmp != 0) return cmp;
		case TIME:
			cmp = new Integer(m1.getStartSlot()).compareTo(m2.getStartSlot());
			if (cmp != 0) return cmp;
			cmp = new Integer(m1.getEndSlot()).compareTo(m2.getEndSlot());
			if (cmp != 0) return cmp;
			cmp = m1.getMeetingDate().compareTo(m2.getMeetingDate());
			if (cmp != 0) return cmp;
		case LOCATION:
			if (m1 instanceof MeetingConglictInterface && m2 instanceof MeetingConglictInterface) {
				cmp = ((MeetingConglictInterface)m1).getName().compareTo(((MeetingConglictInterface)m2).getName());
				if (cmp != 0) return cmp;
				cmp = new Integer(m1.getStartSlot()).compareTo(m2.getStartSlot());
				if (cmp != 0) return cmp;
				cmp = new Integer(m1.getEndSlot()).compareTo(m2.getEndSlot());
				if (cmp != 0) return cmp;
			}
			cmp = m1.getLocationName().compareTo(m2.getLocationName());
			if (cmp != 0) return cmp;
			cmp = m1.getMeetingDate().compareTo(m2.getMeetingDate());
			if (cmp != 0) return cmp;
			cmp = new Integer(m1.getStartSlot()).compareTo(m2.getStartSlot());
			if (cmp != 0) return cmp;
			cmp = new Integer(m1.getEndSlot()).compareTo(m2.getEndSlot());
			if (cmp != 0) return cmp;
			break;
		case CAPACITY:
			if (m1 instanceof MeetingConglictInterface && m2 instanceof MeetingConglictInterface) {
				cmp = ((MeetingConglictInterface)m1).getName().compareTo(((MeetingConglictInterface)m2).getName());
				if (cmp != 0) return cmp;
				cmp = new Integer(m1.getStartSlot()).compareTo(m2.getStartSlot());
				if (cmp != 0) return cmp;
				cmp = new Integer(m1.getEndSlot()).compareTo(m2.getEndSlot());
				if (cmp != 0) return cmp;
			}
			cmp = (m1.getLocation() == null ? new Integer(-1) : m1.getLocation().getSize()).compareTo(m2.getLocation() == null ? new Integer(-1) : m2.getLocation().getSize());
			if (cmp != 0) return cmp;
			cmp = m1.getLocationName().compareTo(m2.getLocationName());
			if (cmp != 0) return cmp;
			cmp = m1.getMeetingDate().compareTo(m2.getMeetingDate());
			if (cmp != 0) return cmp;
			cmp = new Integer(m1.getStartSlot()).compareTo(m2.getStartSlot());
			if (cmp != 0) return cmp;
			cmp = new Integer(m1.getEndSlot()).compareTo(m2.getEndSlot());
			if (cmp != 0) return cmp;
		}
		
		return m1.compareTo(m2);
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
	
	public static class CheckBoxCell extends CheckBox implements HasCellAlignment {
		
		public CheckBoxCell() {
			super();
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_CENTER;
		}

	}
}
