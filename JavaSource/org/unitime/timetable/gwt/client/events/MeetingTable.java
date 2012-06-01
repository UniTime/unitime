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
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConglictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class MeetingTable extends UniTimeTable<MeetingInterface[]> implements HasValue<List<MeetingInterface>>, ApproveDialog.CanHideUnimportantColumns {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.meetingDateFormat());
	
	private Command iAddMeetingsCommand;
	private ApproveDialog<MeetingInterface> iApproveDialog = null;
	private boolean iSelectable = true, iEditable = true;
	
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
				return isEditable() && (meeting.getId() == null || meeting.isCanEdit());
			}
			@Override
			public void execute(int row, MeetingInterface meeting) {
				while (row + 1 < getRowCount() && getData(row + 1).length == 2)
					removeRow(row + 1);
				removeRow(row);
				ValueChangeEvent.fire(MeetingTable.this, getValue());
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
				return iApproveDialog != null && meeting.getId() != null && meeting.isCanApprove();
			}
			@Override
			public void execute(int row, MeetingInterface meeting) {
			}
			@Override
			public void execute() {
				iApproveDialog.showApprove(meetings());
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
				return iApproveDialog != null && meeting.getId() != null && meeting.isCanApprove();
			}
			@Override
			public void execute(int row, MeetingInterface meeting) {
			}
			@Override
			public void execute() {
				iApproveDialog.showInquire(meetings());
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
				return iApproveDialog != null && meeting.getId() != null && meeting.isCanApprove();
			}
			@Override
			public void execute(int row, MeetingInterface meeting) {
			}
			@Override
			public void execute() {
				iApproveDialog.showReject(meetings());
			}
		});
		
		header.add(hTimes);
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
		UniTimeTableHeader hCapacity = new UniTimeTableHeader(MESSAGES.colCapacity(), HasHorizontalAlignment.ALIGN_RIGHT);
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
						int colSetup = getHeader(MESSAGES.colSetupTimeShort()).getColumn();
						int colTear = getHeader(MESSAGES.colTeardownTimeShort()).getColumn();
						int colPubl = getHeader(MESSAGES.colPublishedTime()).getColumn();
						for (Integer row: rows()) {
							MeetingInterface meeting = getData(row)[0];
							if (setupTime.toInteger() != null)
								meeting.setStartOffset(setupTime.toInteger());
							if (teardownTime.toInteger() != null)
								meeting.setEndOffset(-teardownTime.toInteger());
							((NumberCell)getWidget(row, colSetup)).setText(String.valueOf(meeting.getStartOffset()));
							((NumberCell)getWidget(row, colTear)).setText(String.valueOf(-meeting.getEndOffset()));
							((Label)getWidget(row, colPubl)).setText(meetingTime(meeting));
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
		
		addRow(null, header);
		
		// Add show/hide operations
		addHideOperation(hTimePub, EventFlag.SHOW_PUBLISHED_TIME);
		addHideOperation(hTimeAll, EventFlag.SHOW_ALLOCATED_TIME);
		addHideOperation(hTimeSetup, EventFlag.SHOW_SETUP_TIME);
		addHideOperation(hTimeTeardown, EventFlag.SHOW_TEARDOWN_TIME);
		addHideOperation(hCapacity, EventFlag.SHOW_CAPACITY);

		// Add sorting operations
		addSortByOperation(hDate, createComparator(MeetingsSortBy.DATE));
		addSortByOperation(hTimePub, createComparator(MeetingsSortBy.PUBLISHED_TIME));
		addSortByOperation(hTimeSetup, createComparator(MeetingsSortBy.SETUP_TIME));
		addSortByOperation(hTimeAll, createComparator(MeetingsSortBy.ALLOCATED_TIME));
		addSortByOperation(hTimeTeardown, createComparator(MeetingsSortBy.TEARDOWN_TIME));
		addSortByOperation(hLocation, createComparator(MeetingsSortBy.LOCATION));
		addSortByOperation(hCapacity, createComparator(MeetingsSortBy.CAPACITY));
		addSortByOperation(hApproval, createComparator(MeetingsSortBy.APPROVAL));
				
		resetColumnVisibility();
	}
	
	public void setSelectable(boolean selectable) { iSelectable = selectable; }
	public boolean isSelectable() { return iSelectable; }
	
	@Override
	public void clearTable(int headerRows) {
		super.clearTable(headerRows);
		resetColumnVisibility();
	}
	
	public void resetColumnVisibility() {
		setColumnVisible(getHeader(MESSAGES.colPublishedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
		setColumnVisible(getHeader(MESSAGES.colAllocatedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_ALLOCATED_TIME));
		setColumnVisible(getHeader(MESSAGES.colSetupTimeShort()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_SETUP_TIME));
		setColumnVisible(getHeader(MESSAGES.colTeardownTimeShort()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_TEARDOWN_TIME));
		setColumnVisible(getHeader(MESSAGES.colCapacity()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_CAPACITY));
		setColumnVisible(0, false);
	}
	

	@Override
	public void hideUnimportantColumns() {
		setColumnVisible(getHeader(MESSAGES.colPublishedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
		setColumnVisible(getHeader(MESSAGES.colAllocatedTime()).getColumn(), !EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
		setColumnVisible(getHeader(MESSAGES.colSetupTimeShort()).getColumn(), false);
		setColumnVisible(getHeader(MESSAGES.colTeardownTimeShort()).getColumn(), false);
		setColumnVisible(getHeader(MESSAGES.colCapacity()).getColumn(), false);
	}

	protected void addHideOperation(final UniTimeTableHeader header, final EventFlag flag) {
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
			public boolean isApplicable() { return true; }
			@Override
			public boolean hasSeparator() { return false; }
			@Override
			public String getName() { return isColumnVisible(header.getColumn()) ? MESSAGES.opHide(header.getHTML()) : MESSAGES.opShow(header.getHTML()); }
		};
		getHeader(null).addOperation(flag == EventFlag.SHOW_PUBLISHED_TIME ? separated(op) : op);
		getHeader(MESSAGES.colDate()).addOperation(ifNotSelectable(op));
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
			getHeader(MESSAGES.colLocation()).addOperation(op);
			header.addOperation(op);
			break;
		default:
			header.addOperation(op);
		}
	}
	
	private Operation separated(final Operation op) {
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
				return op.isApplicable();
			}
			@Override
			public boolean hasSeparator() {
				return true;
			}
		};
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
	
	public boolean hasAddMeetingsCommand() { return iAddMeetingsCommand != null; }
	public void setAddMeetingsCommand(Command command) { iAddMeetingsCommand = command; }
	public Command getAddMeetingsCommand() { return iAddMeetingsCommand; }

	public boolean hasApproveDialog() { return iApproveDialog != null; }
	public void setApproveDialog(ApproveDialog<MeetingInterface> dialog) { iApproveDialog = dialog; }
	public ApproveDialog<MeetingInterface> getApproveDialog() { return iApproveDialog; }

	
	public void setEditable(boolean editable) { iEditable = editable; }
	public boolean isEditable() { return iEditable; }
	
	public boolean hasMeeting(MeetingInterface meeting) {
		for (int row = 1; row < getRowCount(); row++)
			if (meeting.equals(getData(row)[0])) return true;
		return false;
	}
	
	public void add(MeetingInterface meeting) {
		List<Widget> row = new ArrayList<Widget>();
		if (!isSelectable()) {
			row.add(new HTML(MESSAGES.signSelected()));
		} else if (meeting.getId() == null || (meeting.isCanEdit() || meeting.isCanApprove())) {
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
		getRowFormatter().addStyleName(meetingRow, "meeting-row");
		if (meeting.isPast())
			getRowFormatter().addStyleName(meetingRow, "past-meeting");
		if (meeting.hasConflicts()) {
			for (MeetingConglictInterface m: meeting.getConflicts()) {
				List<Widget> r = new ArrayList<Widget>();
				r.add(new CenterredCell(MESSAGES.signConflict()));
				r.add(new Label(m.getType() == EventType.Unavailabile ? m.getName() : MESSAGES.conflictWith(m.getName()), false));
				r.add(new Label(meetingTime(m)));
				r.add(new Label(allocatedTime(m)));
				r.add(new NumberCell(m.getStartOffset()));
				r.add(new NumberCell(- m.getEndOffset()));
				r.add(new Label(""));
				r.add(new NumberCell(""));
				r.add(new Label(m.getType() == EventType.Unavailabile ? "" : m.getApprovalDate() == null ? MESSAGES.approvalNotApproved() : sDateFormat.format(m.getApprovalDate())));
				if (!m.isApproved() && m.getType() != EventType.Unavailabile)
					r.get(r.size() - 1).addStyleName("not-approved");
				r.get(1).getElement().getStyle().setPaddingLeft(5, Unit.PX);
				int conflictRow = addRow(new MeetingInterface[] {meeting, m}, r);
				getRowFormatter().addStyleName(conflictRow, "conflict");
			}
		}
	}
	
	protected void addSortByOperation(final UniTimeTableHeader header, final Comparator<MeetingInterface[]> comparator) {
		header.addOperation(new Operation() {
			@Override
			public void execute() { sort(comparator); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(header.getHTML()); }
		});
	}
	
	public static enum MeetingsSortBy {
		DATE, PUBLISHED_TIME, ALLOCATED_TIME, SETUP_TIME, TEARDOWN_TIME, LOCATION, CAPACITY, APPROVAL
	}
	
	protected Comparator<MeetingInterface[]> createComparator(final MeetingsSortBy sortBy) {
		return new Comparator<MeetingInterface[]>() {
			@Override
			public int compare(MeetingInterface[] m1, MeetingInterface[] m2) {
				int cmp = MeetingTable.compare(m1[0], m2[0], sortBy);
				if (cmp != 0) return cmp;
				if (m1.length == 2) {
					if (m2.length == 2) return MeetingTable.compare(m1[1], m2[1], sortBy);
					else return 1; 
				} else {
					return -1;
				}
			}
		};
	}
	
	private static int compateByApproval(MeetingInterface m1, MeetingInterface m2) {
		if (m1.getId() == null && m2.getId() != null) return -1;
		if (m1.getId() != null && m2.getId() == null) return 1;
		Date now = new Date();
		return (m1.getApprovalDate() == null ? now : m1.getApprovalDate()).compareTo(m2.getApprovalDate() == null ? now : m2.getApprovalDate());
	}
	
	private static int compareByDate(MeetingInterface m1, MeetingInterface m2) {
		if (m1 instanceof MeetingConglictInterface && m2 instanceof MeetingConglictInterface) {
			int cmp = ((MeetingConglictInterface)m1).getName().compareTo(((MeetingConglictInterface)m2).getName());
			if (cmp != 0) return cmp;
		}
		return m1.getMeetingDate().compareTo(m2.getMeetingDate());
	}
	
	private static int compareByAllocatedTime(MeetingInterface m1, MeetingInterface m2) {
		int cmp = new Integer(m1.getStartSlot()).compareTo(m2.getStartSlot());
		if (cmp != 0) return cmp;
		return new Integer(m1.getEndSlot()).compareTo(m2.getEndSlot());
	}
	
	private static int compareByPublishedTime(MeetingInterface m1, MeetingInterface m2) {
		int cmp = new Integer((5 * m1.getStartSlot()) + m1.getStartOffset()).compareTo((5 * m2.getStartSlot()) + m2.getStartOffset());
		if (cmp != 0) return cmp;
		return new Integer((5 * m1.getEndSlot()) + m2.getEndOffset()).compareTo((5 * m2.getEndSlot()) + m2.getEndOffset());
	}

	private static int compareBySetupTime(MeetingInterface m1, MeetingInterface m2) {
		return new Integer(m1.getStartOffset()).compareTo(m2.getStartOffset());
	}

	private static int compareByTeardownTime(MeetingInterface m1, MeetingInterface m2) {
		return new Integer(m2.getEndOffset()).compareTo(m1.getEndOffset());
	}
	
	private static int compareByLocation(MeetingInterface m1, MeetingInterface m2) {
		return m1.getLocationName().compareTo(m2.getLocationName());
	}
	
	private static int compareByCapacity(MeetingInterface m1, MeetingInterface m2) {
		return (m1.getLocation() == null ? new Integer(-1) : m1.getLocation().getSize()).compareTo(m2.getLocation() == null ? new Integer(-1) : m2.getLocation().getSize());
	}

	private static int compareFallback(MeetingInterface m1, MeetingInterface m2) {
		int cmp = compareByDate(m1, m2);
		if (cmp != 0) return cmp;
		cmp = compareByPublishedTime(m1, m2);
		if (cmp != 0) return cmp;
		cmp = compareByLocation(m1, m2);
		if (cmp != 0) return cmp;
		return m1.compareTo(m2);
	}
	
	public static int compare(MeetingInterface m1, MeetingInterface m2, MeetingsSortBy sortBy) {
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

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<MeetingInterface>> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public List<MeetingInterface> getValue() {
		List<MeetingInterface> value = new ArrayList<MeetingInterface>();
		for (int row = 1; row < getRowCount(); row++) {
			MeetingInterface[] data = getData(row);
			if (data != null && data.length == 1) value.add(data[0]);
		}
		return value;
	}

	@Override
	public void setValue(List<MeetingInterface> value) {
		setValue(value, false);
	}

	@Override
	public void setValue(List<MeetingInterface> value, boolean fireEvents) {
		clearTable(1);
		if (value != null)
			for (MeetingInterface meeting: value)
				add(meeting);
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}
}
