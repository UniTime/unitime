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

import org.unitime.timetable.gwt.client.events.EventCookie.EventFlag;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConglictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
	
	private int iColumnPublishedTime, iColumnAllocatedTime, iColumnSetupTime, iColumnTeardownTime, iColumnLocation, iColumnCapacity;

	public MeetingTable() {
		setStyleName("unitime-EventMeetings");
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
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
		hTimes.addOperation(new MeetingOperation() {
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return (hasSelection() ? MESSAGES.opDeleteSelectedMeetings() : MESSAGES.opDeleteNewMeetings());
			}
			@Override
			public boolean isApplicable(MeetingInterface meeting) {
				return meeting.getId() == null;
			}
			@Override
			public void execute(int row, MeetingInterface meeting) {
				while (row + 1 < getRowCount() && getData(row + 1).length == 2)
					removeRow(row + 1);
				removeRow(row);
			}
		});
		hTimes.addOperation(new MeetingOperation() {
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public String getName() {
				return (hasSelection() ? MESSAGES.opApproveSelectedMeetings() : MESSAGES.opApproveAllMeetings());
			}
			@Override
			public boolean isApplicable(MeetingInterface meeting) {
				return meeting.getId() != null && meeting.isCanApprove();
			}
			@Override
			public void execute(int row, MeetingInterface meeting) {
			}
		});
		hTimes.addOperation(new MeetingOperation() {
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
			public boolean isApplicable(MeetingInterface meeting) {
				return meeting.getId() != null && meeting.isCanApprove();
			}
			@Override
			public void execute(int row, MeetingInterface meeting) {
			}
		});
		hTimes.addOperation(new MeetingOperation() {
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return (hasSelection() ? MESSAGES.opRejectSelectedMeetings() : MESSAGES.opRejectAllMeetings());
			}
			@Override
			public boolean isApplicable(MeetingInterface meeting) {
				return meeting.getId() != null && meeting.isCanApprove();
			}
			@Override
			public void execute(int row, MeetingInterface meeting) {
			}
		});
		
		header.add(hTimes);
		UniTimeTableHeader hDate = new UniTimeTableHeader(MESSAGES.colDate());
		header.add(hDate);
		UniTimeTableHeader hTimePub = new UniTimeTableHeader(MESSAGES.colPublishedTime()); 
		iColumnPublishedTime = header.size();
		header.add(hTimePub);
		UniTimeTableHeader hTimeAll = new UniTimeTableHeader(MESSAGES.colAllocatedTime()); 
		iColumnAllocatedTime = header.size();
		header.add(hTimeAll);
		UniTimeTableHeader hTimeSetup = new UniTimeTableHeader(MESSAGES.colSetupTimeShort(), HasHorizontalAlignment.ALIGN_RIGHT); 
		iColumnSetupTime = header.size();
		header.add(hTimeSetup);
		UniTimeTableHeader hTimeTeardown = new UniTimeTableHeader(MESSAGES.colTeardownTimeShort(), HasHorizontalAlignment.ALIGN_RIGHT); 
		iColumnTeardownTime = header.size();
		header.add(hTimeTeardown);

		UniTimeTableHeader hLocation = new UniTimeTableHeader(MESSAGES.colLocation());
		iColumnLocation = header.size();
		header.add(hLocation);
		UniTimeTableHeader hCapacity = new UniTimeTableHeader(MESSAGES.colCapacity(), HasHorizontalAlignment.ALIGN_RIGHT);
		iColumnCapacity = header.size();
		header.add(hCapacity);
		UniTimeTableHeader hApproval = new UniTimeTableHeader(MESSAGES.colApproval());
		header.add(hApproval);
		
		hTimes.addOperation(new MeetingOperation() {
			@Override
			public void execute() {
				Integer so = null, eo = null;
				boolean soSame = true, eoSame = true;
				for (MeetingInterface m: meetings()) {
					if (so == null) so = m.getStartOffset();
					else if (m.getStartOffset() != so) soSame = false;
					if (eo == null) eo = -m.getEndOffset();
					else if (-m.getEndOffset() != eo) eoSame = false;
				}
				final UniTimeDialogBox dialog = new UniTimeDialogBox(true, false);
				SimpleForm simple = new SimpleForm();
				simple.removeStyleName("unitime-NotPrintableBottomLine");
				final NumberBox setupTime = new NumberBox();
				if (soSame && so != null) setupTime.setValue(so); 
				simple.addRow(MESSAGES.propSetupTime(), setupTime);
				final NumberBox teardownTime = new NumberBox();
				if (eoSame && eo != null) teardownTime.setValue(eo);
				simple.addRow(MESSAGES.propTeardownTime(), teardownTime);
				UniTimeHeaderPanel footer = new UniTimeHeaderPanel();
				footer.addButton("ok", MESSAGES.buttonOk(), 75, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						for (Integer row: rows()) {
							MeetingInterface meeting = getData(row)[0];
							if (setupTime.toInteger() != null)
								meeting.setStartOffset(setupTime.toInteger());
							if (teardownTime.toInteger() != null)
								meeting.setEndOffset(-teardownTime.toInteger());
							((NumberCell)getWidget(row, iColumnSetupTime)).setText(String.valueOf(meeting.getStartOffset()));
							((NumberCell)getWidget(row, iColumnTeardownTime)).setText(String.valueOf(-meeting.getEndOffset()));
							((Label)getWidget(row, iColumnPublishedTime)).setText(meetingTime(meeting));
						}
						dialog.hide();						
					}
				});
				footer.addButton("cancel", MESSAGES.buttonCancel(), 75, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						dialog.hide();
					}
				});
				simple.addBottomRow(footer);
				dialog.setWidget(simple);
				dialog.setText(MESSAGES.dlgChangeOffsets());
				dialog.setEscapeToHide(true);
				dialog.center();
			}
			@Override
			public String getName() {
				return MESSAGES.opChangeOffsets();
			}
			@Override
			public boolean isApplicable(MeetingInterface meeting) {
				return meeting.getId() == null;
			}
			@Override
			public void execute(int row, MeetingInterface meeting) {
			}
		});
		
		Operation opTimePub = new Operation() {
			@Override
			public void execute() {
				boolean visible = isColumnVisible(iColumnPublishedTime);
				setColumnVisible(iColumnPublishedTime, !visible);
				EventCookie.getInstance().set(EventFlag.SHOW_PUBLISHED_TIME, !visible);
				boolean other = isColumnVisible(iColumnAllocatedTime);
				if (visible && !other) {
					setColumnVisible(iColumnAllocatedTime, !other);
					EventCookie.getInstance().set(EventFlag.SHOW_ALLOCATED_TIME, !other);
				}
			}
			@Override
			public boolean isApplicable() { return true; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return isColumnVisible(iColumnPublishedTime) ? MESSAGES.opHide(MESSAGES.colPublishedTime()) : MESSAGES.opShow(MESSAGES.colPublishedTime()); }
		};
		header.get(0).addOperation(opTimePub);
		header.get(iColumnPublishedTime).addOperation(opTimePub);
		header.get(iColumnAllocatedTime).addOperation(opTimePub);
		header.get(iColumnSetupTime).addOperation(opTimePub);
		header.get(iColumnTeardownTime).addOperation(opTimePub);
		
		Operation opTimeAll = new Operation() {
			@Override
			public void execute() {
				boolean visible = isColumnVisible(iColumnAllocatedTime);
				setColumnVisible(iColumnAllocatedTime, !visible);
				EventCookie.getInstance().set(EventFlag.SHOW_ALLOCATED_TIME, !visible);
				boolean other = isColumnVisible(iColumnPublishedTime);
				if (visible && !other) {
					setColumnVisible(iColumnPublishedTime, !other);
					EventCookie.getInstance().set(EventFlag.SHOW_PUBLISHED_TIME, !other);
				}
			}
			@Override
			public boolean isApplicable() { return true; }
			@Override
			public boolean hasSeparator() { return false; }
			@Override
			public String getName() { return isColumnVisible(iColumnAllocatedTime) ? MESSAGES.opHide(MESSAGES.colAllocatedTime()) : MESSAGES.opShow(MESSAGES.colAllocatedTime()); }
		};
		header.get(0).addOperation(opTimeAll);
		header.get(iColumnPublishedTime).addOperation(opTimeAll);
		header.get(iColumnAllocatedTime).addOperation(opTimeAll);
		header.get(iColumnSetupTime).addOperation(opTimeAll);
		header.get(iColumnTeardownTime).addOperation(opTimeAll);
		
		Operation opTimeSetup = new Operation() {
			@Override
			public void execute() {
				boolean visible = isColumnVisible(iColumnSetupTime);
				setColumnVisible(iColumnSetupTime, !visible);
				EventCookie.getInstance().set(EventFlag.SHOW_SETUP_TIME, !visible);
			}
			@Override
			public boolean isApplicable() { return true; }
			@Override
			public boolean hasSeparator() { return false; }
			@Override
			public String getName() { return isColumnVisible(iColumnSetupTime) ? MESSAGES.opHide(MESSAGES.colSetupTime()) : MESSAGES.opShow(MESSAGES.colSetupTime()); }
		};
		header.get(0).addOperation(opTimeSetup);
		header.get(iColumnPublishedTime).addOperation(opTimeSetup);
		header.get(iColumnAllocatedTime).addOperation(opTimeSetup);
		header.get(iColumnSetupTime).addOperation(opTimeSetup);
		header.get(iColumnTeardownTime).addOperation(opTimeSetup);
	
		Operation opTimeTeardown = new Operation() {
			@Override
			public void execute() {
				boolean visible = isColumnVisible(iColumnTeardownTime);
				setColumnVisible(iColumnTeardownTime, !visible);
				EventCookie.getInstance().set(EventFlag.SHOW_TEARDOWN_TIME, !visible);
			}
			@Override
			public boolean isApplicable() { return true; }
			@Override
			public boolean hasSeparator() { return false; }
			@Override
			public String getName() { return isColumnVisible(iColumnTeardownTime) ? MESSAGES.opHide(MESSAGES.colTeardownTime()) : MESSAGES.opShow(MESSAGES.colTeardownTime()); }
		};
		header.get(0).addOperation(opTimeTeardown);
		header.get(iColumnPublishedTime).addOperation(opTimeTeardown);
		header.get(iColumnAllocatedTime).addOperation(opTimeTeardown);
		header.get(iColumnSetupTime).addOperation(opTimeTeardown);
		header.get(iColumnTeardownTime).addOperation(opTimeTeardown);
		
		Operation opCapacity = new Operation() {
			@Override
			public void execute() {
				boolean visible = isColumnVisible(iColumnCapacity);
				setColumnVisible(iColumnCapacity, !visible);
				EventCookie.getInstance().set(EventFlag.SHOW_CAPACITY, !visible);
			}
			@Override
			public boolean isApplicable() { return true; }
			@Override
			public boolean hasSeparator() { return false; }
			@Override
			public String getName() { return isColumnVisible(iColumnCapacity) ? MESSAGES.opHide(MESSAGES.colCapacity()) : MESSAGES.opShow(MESSAGES.colCapacity()); }
		};
		header.get(0).addOperation(opCapacity);
		header.get(iColumnLocation).addOperation(opCapacity);
		header.get(iColumnCapacity).addOperation(opCapacity);

		// Add sorting operations
		hDate.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.DATE)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colDate()); }
		});
		hTimePub.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.PUBLISHED_TIME)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colPublishedTime()); }
		});
		hTimeSetup.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.SETUP_TIME)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colSetupTime()); }
		});
		hTimeAll.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.ALLOCATED_TIME)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colAllocatedTime()); }
		});
		hTimeTeardown.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.TEARDOWN_TIME)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colTeardownTime()); }
		});
		hLocation.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.LOCATION)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colLocation()); }
		});
		hCapacity.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.CAPACITY)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colCapacity()); }
		});
		hApproval.addOperation(new Operation() {
			@Override
			public void execute() { sort(createComparator(SortBy.APPROVAL)); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(MESSAGES.colApproval()); }
		});
				
		addRow(null, header);
		
		setColumnVisible(iColumnPublishedTime, EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
		setColumnVisible(iColumnAllocatedTime, EventCookie.getInstance().get(EventFlag.SHOW_ALLOCATED_TIME));
		setColumnVisible(iColumnSetupTime, EventCookie.getInstance().get(EventFlag.SHOW_SETUP_TIME));
		setColumnVisible(iColumnTeardownTime, EventCookie.getInstance().get(EventFlag.SHOW_TEARDOWN_TIME));
		setColumnVisible(iColumnCapacity, EventCookie.getInstance().get(EventFlag.SHOW_CAPACITY));
	}

	@Override
	public void clearTable(int headerRows) {
		super.clearTable(headerRows);
		setColumnVisible(iColumnPublishedTime, EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
		setColumnVisible(iColumnAllocatedTime, EventCookie.getInstance().get(EventFlag.SHOW_ALLOCATED_TIME));
		setColumnVisible(iColumnSetupTime, EventCookie.getInstance().get(EventFlag.SHOW_SETUP_TIME));
		setColumnVisible(iColumnTeardownTime, EventCookie.getInstance().get(EventFlag.SHOW_TEARDOWN_TIME));
		setColumnVisible(iColumnCapacity, EventCookie.getInstance().get(EventFlag.SHOW_CAPACITY));
		setColumnVisible(0, false);
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
		if (meeting.getId() == null || (meeting.isCanEdit() || meeting.isCanApprove())) {
			row.add(new CheckBoxCell());
			if (!isColumnVisible(0)) setColumnVisible(0, true);
		} else {
			row.add(new HTML("&nbsp;"));
		}
		row.add(new Label(sDateFormat.format(meeting.getMeetingDate())));
		row.add(new Label(meetingTime(meeting)));
		row.add(new Label(allocatedTime(meeting)));
		row.add(new NumberCell(meeting.getStartOffset()));
		row.add(new NumberCell(- meeting.getEndOffset()));
		
		if (meeting.getLocation() == null) {
			row.add(new Label(""));
			row.add(new Label(""));
		} else {
			row.add(new Label(meeting.getLocationName()));
			row.add(new NumberCell(meeting.getLocation().getSize() == null ? "N/A" : meeting.getLocation().getSize().toString()));
		}
		row.add(new Label(meeting.getId() == null ? MESSAGES.approvalNewMeeting() : 
			meeting.getApprovalDate() == null ? (meeting.isPast() ? MESSAGES.approvalNotApprovedPast() : MESSAGES.approvalNotApproved()) : 
			sDateFormat.format(meeting.getApprovalDate())));
		if (meeting.getId() == null)
			row.get(row.size() - 1).addStyleName("new-meeting");
		else if (!meeting.isApproved()) {
			if (meeting.isPast())
				row.get(row.size() - 1).addStyleName("not-approved-past");
			else
				row.get(row.size() - 1).addStyleName("not-approved");
		}
		int meetingRow = addRow(new MeetingInterface[] {meeting}, row);
		if (meeting.isPast())
			getRowFormatter().addStyleName(meetingRow, "past-meeting");
		if (meeting.hasConflicts()) {
			for (MeetingConglictInterface m: meeting.getConflicts()) {
				List<Widget> r = new ArrayList<Widget>();
				r.add(new Label(""));
				r.add(new Label(MESSAGES.conflictWith(m.getName()), false));
				r.add(new Label(meetingTime(m)));
				r.add(new Label(allocatedTime(m)));
				r.add(new NumberCell(m.getStartOffset()));
				r.add(new NumberCell(- m.getEndOffset()));
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
		DATE, PUBLISHED_TIME, ALLOCATED_TIME, SETUP_TIME, TEARDOWN_TIME, LOCATION, CAPACITY, APPROVAL
	}
	
	protected Comparator<MeetingInterface[]> createComparator(final SortBy sortyBy) {
		return new Comparator<MeetingInterface[]>() {
			@Override
			public int compare(MeetingInterface[] o1, MeetingInterface[] o2) {
				return MeetingTable.this.compare(o1, o2, sortyBy);
			}
		};
	}
	
	private int compateByApproval(MeetingInterface m1, MeetingInterface m2) {
		if (m1.getId() == null && m2.getId() != null) return -1;
		if (m1.getId() != null && m2.getId() == null) return 1;
		Date now = new Date();
		return (m1.getApprovalDate() == null ? now : m1.getApprovalDate()).compareTo(m2.getApprovalDate() == null ? now : m2.getApprovalDate());
	}
	
	private int compareByDate(MeetingInterface m1, MeetingInterface m2) {
		if (m1 instanceof MeetingConglictInterface && m2 instanceof MeetingConglictInterface) {
			int cmp = ((MeetingConglictInterface)m1).getName().compareTo(((MeetingConglictInterface)m2).getName());
			if (cmp != 0) return cmp;
		}
		return m1.getMeetingDate().compareTo(m2.getMeetingDate());
	}
	
	private int compareByAllocatedTime(MeetingInterface m1, MeetingInterface m2) {
		int cmp = new Integer(m1.getStartSlot()).compareTo(m2.getStartSlot());
		if (cmp != 0) return cmp;
		return new Integer(m1.getEndSlot()).compareTo(m2.getEndSlot());
	}
	
	private int compareByPublishedTime(MeetingInterface m1, MeetingInterface m2) {
		int cmp = new Integer((5 * m1.getStartSlot()) + m1.getStartOffset()).compareTo((5 * m2.getStartSlot()) + m2.getStartOffset());
		if (cmp != 0) return cmp;
		return new Integer((5 * m1.getEndSlot()) + m2.getEndOffset()).compareTo((5 * m2.getEndSlot()) + m2.getEndOffset());
	}

	private int compareBySetupTime(MeetingInterface m1, MeetingInterface m2) {
		return new Integer(m1.getStartOffset()).compareTo(m2.getStartOffset());
	}

	private int compareByTeardownTime(MeetingInterface m1, MeetingInterface m2) {
		return new Integer(m1.getEndOffset()).compareTo(m2.getEndOffset());
	}
	
	private int compareByLocation(MeetingInterface m1, MeetingInterface m2) {
		return m1.getLocationName().compareTo(m2.getLocationName());
	}
	
	private int compareByCapacity(MeetingInterface m1, MeetingInterface m2) {
		return (m1.getLocation() == null ? new Integer(-1) : m1.getLocation().getSize()).compareTo(m2.getLocation() == null ? new Integer(-1) : m2.getLocation().getSize());
	}

	private int compareFallback(MeetingInterface m1, MeetingInterface m2) {
		int cmp = compareByDate(m1, m2);
		if (cmp != 0) return cmp;
		cmp = compareByPublishedTime(m1, m2);
		if (cmp != 0) return cmp;
		cmp = compareByLocation(m1, m2);
		if (cmp != 0) return cmp;
		return m1.compareTo(m2);
	}
	
	public int compare(MeetingInterface m1, MeetingInterface m2, SortBy sortBy) {
		int cmp;
		switch (sortBy) {
		case APPROVAL:
			cmp = compateByApproval(m1, m2);
			return (cmp == 0 ? compareFallback(m1, m2) : cmp);
		case DATE:
			cmp = compareByDate(m1, m2);
			return (cmp == 0 ? compareFallback(m1, m2) : cmp);
		case SETUP_TIME:
			cmp = compareBySetupTime(m1, m2);
			return (cmp == 0 ? compareFallback(m1, m2) : cmp);
		case TEARDOWN_TIME:
			cmp = compareByTeardownTime(m1, m2);
			return (cmp == 0 ? compareFallback(m1, m2) : cmp);
		case PUBLISHED_TIME:
			cmp = compareBySetupTime(m1, m2);
			return (cmp == 0 ? compareFallback(m1, m2) : cmp);
		case ALLOCATED_TIME:
			cmp = compareByAllocatedTime(m1, m2);
			return (cmp == 0 ? compareFallback(m1, m2) : cmp);
		case LOCATION:
			cmp = compareByLocation(m1, m2);
			return (cmp == 0 ? compareFallback(m1, m2) : cmp);
		case CAPACITY:
			cmp = compareByCapacity(m1, m2);
			return (cmp == 0 ? compareFallback(m1, m2) : cmp);
		}
		
		return m1.compareTo(m2);
	}
	
	public static String meetingTime(MeetingInterface m) {
		if (m.getStartSlot() == 0 && m.getStartOffset() == 0 && m.getEndOffset() == 0 && m.getEndSlot() == 288)
			return MESSAGES.timeAllDay();
		
		String startTime = m.getStartTime(CONSTANTS.useAmPm(), true);
		int start = 5 * m.getStartSlot() + m.getStartOffset();
		if (start == 0 || start == 1440) startTime = MESSAGES.timeMidnitgh();
		if (start == 720) startTime = MESSAGES.timeNoon();
		
		String endTime = m.getEndTime(CONSTANTS.useAmPm(), true);
		int end = 5 * m.getEndSlot() + m.getEndOffset();
		if (end == 0 || end == 1440) endTime = MESSAGES.timeMidnitgh();
		if (end == 720) endTime = MESSAGES.timeNoon();
		
		return startTime + " - " + endTime;
	}
	
	public static String allocatedTime(MeetingInterface m) {
		if (m.getStartSlot() == 0 && m.getEndSlot() == 288)
			return MESSAGES.timeAllDay();
		
		String startTime = m.getStartTime(CONSTANTS.useAmPm(), false);
		if (m.getStartSlot() == 0 || m.getStartSlot() == 288) startTime = MESSAGES.timeMidnitgh();
		if (m.getStartSlot() == 144) startTime = MESSAGES.timeNoon();
		
		String endTime = m.getEndTime(CONSTANTS.useAmPm(), false);
		if (m.getEndSlot() == 0 || m.getEndSlot() == 288) endTime = MESSAGES.timeMidnitgh();
		if (m.getEndSlot() == 144) endTime = MESSAGES.timeNoon();
		
		return startTime + " - " + endTime;
	}

	public static class NumberCell extends HTML implements HasCellAlignment {
		public NumberCell(String text) {
			super(text, false);
		}
		
		public NumberCell(int text) {
			super(String.valueOf(text), false);
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
	
	private abstract class MeetingOperation implements Operation {
		@Override
		public void execute() {
			if (hasSelection()) {
				for (int row = getRowCount() - 1; row >= 1; row--) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) {
							MeetingInterface m = getData(row)[0];
							if (isApplicable(m)) execute(row, m);
						}
					}
				}
			} else {
				for (int row = getRowCount() - 1; row >= 1; row--) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						MeetingInterface m = getData(row)[0];
						if (isApplicable(m)) execute(row, m);
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
							MeetingInterface m = getData(row)[0];
							if (isApplicable(m)) {
								if (!allMustMatch) return true;
							} else {
								if (allMustMatch) return false;
							}
						}
					}
				}
				return allMustMatch;
			} else {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						MeetingInterface m = getData(row)[0];
						if (isApplicable(m)) {
							if (!allMustMatch) return true;
						} else {
							if (allMustMatch) return false;
						}
					}
				}
				return allMustMatch;
			}
		}
		
		public List<MeetingInterface> meetings() {
			List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
			if (hasSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) {
							MeetingInterface m = getData(row)[0];
							if (isApplicable(m)) meetings.add(m);
						}
					}
				}
			} else {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						MeetingInterface m = getData(row)[0];
						if (isApplicable(m)) meetings.add(m);
					}
				}
			}
			return meetings;
		}
		
		public List<Integer> rows() {
			List<Integer> rows = new ArrayList<Integer>();
			if (hasSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) {
							MeetingInterface m = getData(row)[0];
							if (isApplicable(m)) rows.add(row);
						}
					}
				}
			} else {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						MeetingInterface m = getData(row)[0];
						if (isApplicable(m)) rows.add(row);
					}
				}
			}
			return rows;
		}
		
		public boolean allMustMatch(boolean hasSelection) {
			return hasSelection;
		}
		
		public abstract boolean isApplicable(MeetingInterface meeting);
		public abstract void execute(int row, MeetingInterface meeting);

		@Override
		public boolean hasSeparator() { return false; }
	}
}
