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
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.events.EventCookie.EventFlag;
import org.unitime.timetable.gwt.client.events.EventTable.EventSortBy;
import org.unitime.timetable.gwt.client.events.EventTable.MeetingFilter;
import org.unitime.timetable.gwt.client.events.MeetingTable.MeetingsSortBy;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class EventMeetingTable extends UniTimeTable<EventMeetingTable.EventMeetingRow> implements HasValue<List<EventMeetingTable.EventMeetingRow>>, ApproveDialog.CanHideUnimportantColumns {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	
	private boolean iShowMainContact = false;
	private String iSortBy = null; 
	private boolean iSelectable = true;
	private ApproveDialog<EventMeetingRow> iApproveDialog = null;
	private MeetingFilter iMeetingFilter = null;

	public EventMeetingTable() {
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
						ch.setValue(inConflict(getData(row)));
					}
				}
			}
			public boolean inConflict(EventMeetingRow row) {
				if (row.getEvent().hasConflicts()) {
					for (EventInterface conflict: row.getEvent().getConflicts())
						if (conflict.inConflict(row.getMeeting())) return true;
				}
				return false;
			}
			
			@Override
			public boolean isApplicable() {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox)
						if (inConflict(getData(row))) return true;
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
						ch.setValue(getData(row).getMeeting().hasConflicts());
					}
				}
			}
			@Override
			public boolean isApplicable() {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						if (getData(row).getMeeting().hasConflicts()) return true;
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
		hTimes.addOperation(new EventMeetingOperation() {
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public String getName() {
				return (hasSelection() ? MESSAGES.opApproveSelectedMeetings() : MESSAGES.opApproveAllMeetings());
			}
			@Override
			public boolean isApplicable(EventMeetingRow row) {
				return iApproveDialog != null && row.getMeeting().isCanApprove();
			}
			@Override
			public void execute(int row, EventMeetingRow event) {
			}
			@Override
			public void execute() {
				iApproveDialog.showApprove(events());
			}
		});
		hTimes.addOperation(new EventMeetingOperation() {
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
			public boolean isApplicable(EventMeetingRow row) {
				return iApproveDialog != null && row.getMeeting().isCanApprove();
			}
			@Override
			public void execute(int row, EventMeetingRow event) {
			}
			@Override
			public void execute() {
				iApproveDialog.showInquire(events());
			}
		});
		hTimes.addOperation(new EventMeetingOperation() {
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return (hasSelection() ? MESSAGES.opRejectSelectedMeetings() : MESSAGES.opRejectAllMeetings());
			}
			@Override
			public boolean isApplicable(EventMeetingRow row) {
				return iApproveDialog != null && row.getMeeting().isCanApprove();
			}
			@Override
			public void execute(int row, EventMeetingRow event) {
			}
			@Override
			public void execute() {
				iApproveDialog.showReject(events());
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
		addHideOperation(hSponsor, EventFlag.SHOW_SPONSOR);
		addHideOperation(hContact, EventFlag.SHOW_MAIN_CONTACT);
		
		addSortByOperation(hName, EventSortBy.NAME);
		addSortByOperation(hSection, EventSortBy.SECTION);
		addSortByOperation(hType, EventSortBy.TYPE);
		addSortByOperation(hDate, EventSortBy.DATE);
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
	
	public void setSelectable(boolean selectable) { iSelectable = selectable; }
	public boolean isSelectable() { return iSelectable; }
	public boolean hasApproveDialog() { return iApproveDialog != null; }
	public void setApproveDialog(ApproveDialog<EventMeetingRow> dialog) { iApproveDialog = dialog; }
	public ApproveDialog<EventMeetingRow> getApproveDialog() { return iApproveDialog; }
	public void setMeetingFilter(MeetingFilter filter) { iMeetingFilter = filter; }
	public void setShowMainContact(boolean show) { iShowMainContact = show; }
	public boolean isShowMainContact() { return iShowMainContact; }
	
	private void add(EventInterface event) {
		TreeSet<MeetingInterface> meetings = new TreeSet<MeetingInterface>();
		for (MeetingInterface meeting: event.getMeetings())
			if (iMeetingFilter == null || !iMeetingFilter.filter(meeting)) {
				meetings.add(meeting);
			}
		if (meetings.isEmpty()) return;

		for (MeetingInterface meeting: new TreeSet<MeetingInterface>(meetings)) {
			add(event, meeting);
		}
	}
	
	private void add(EventInterface event, MeetingInterface meeting) {
		List<Widget> row = new ArrayList<Widget>();
		if (!isSelectable()) {
			row.add(new HTML(MESSAGES.signSelected()));
		} else if (meeting.isCanApprove()) {
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
					// name += "<br><span class='no-control'>" + cn + "</span>";
				} else {
					// name += "<br>" + cn;
				}
			if (event.hasExternalIds())
				for (String ex: event.getExternalIds()) {
					if (section.isEmpty()) {
						section += ex;
					} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
						// section += "<br><span class='no-control'>" + ex + "</span>";
					} else {
						// section += "<br>" + ex;
					}
				}
			else if (event.hasSectionNumber())
				section = event.getSectionNumber();
			row.add(new Hideable(new HTML(name, false)));
			row.add(new Hideable(new NumberCell(section)));
			row.add(new Hideable(new Label(event.getInstruction() == null ? event.getType().getAbbreviation() : event.getInstruction(), false)));
			if (!section.isEmpty() && !isColumnVisible(getHeader(MESSAGES.colSection()).getColumn())) setColumnVisible(getHeader(MESSAGES.colSection()).getColumn(), true);
		} else {
			row.add(new Hideable(new HTML(event.getName())));
			row.add(new Hideable(new HTML("&nbsp;")));
			row.add(new Hideable(new Label(event.getType().getAbbreviation(), false)));
		}
		
		row.add(new Label(sDateFormat.format(meeting.getMeetingDate())));
		row.add(new Label(meeting.getMeetingTime(CONSTANTS)));
		row.add(new Label(meeting.getAllocatedTime(CONSTANTS)));
		row.add(new NumberCell(meeting.getStartOffset()));
		row.add(new NumberCell(- meeting.getEndOffset()));
		
		if (meeting.getLocation() == null) {
			row.add(new Label(""));
			row.add(new Label(""));
		} else {
			row.add(new Label(meeting.getLocationName()));
			row.add(new NumberCell(meeting.getLocation().getSize() == null ? "N/A" : meeting.getLocation().getSize().toString()));
		}
		
		if (event.hasEnrollment() && iShowMainContact) {
			row.add(new Hideable(new NumberCell(event.getEnrollment().toString())));
			if (!isColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn())) setColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn(), true);
		} else {
			row.add(new Hideable(new HTML("&nbsp;")));
		}

		if (event.hasMaxCapacity() && iShowMainContact) {
			row.add(new Hideable(new NumberCell(event.getMaxCapacity().toString())));
			if (!isColumnVisible(getHeader(MESSAGES.colLimit()).getColumn())) setColumnVisible(getHeader(MESSAGES.colLimit()).getColumn(), true);
		} else {
			row.add(new Hideable(new HTML("&nbsp;")));
		}
		
		if (event.hasInstructors()) {
			row.add(new Hideable(new HTML(event.getInstructorNames(", "), false)));
		} else {
			row.add(new Hideable(new Label(event.hasSponsor() ? event.getSponsor().getName() : "")));
		}
		
		if (iShowMainContact) {
			row.add(new Hideable(new HTML(event.hasContact() ? event.getContact().getName() : "&nbsp;")));
		} else {
			row.add(new Hideable(new HTML("&nbsp;")));
		}

		if (meeting.isPast())
			for (Widget w: row)
				if (!(w instanceof Hideable)) w.addStyleName("past-meeting");

		row.add(new HTML(meeting.isApproved() ?
				meeting.isPast() ? "<span class='past-meeting'>" + sDateFormat.format(meeting.getApprovalDate()) + "</span>" : sDateFormat.format(meeting.getApprovalDate()) :
				meeting.isPast() ? "<span class='not-approved-past'>" + MESSAGES.approvalNotApprovedPast() + "</span>" : "<span class='not-approved'>" + MESSAGES.approvalNotApproved() + "</span>"));
		
		addRow(new EventMeetingRow(event, meeting, null), row);
		
		if (event.hasConflicts())
			for (EventInterface conflict: event.getConflicts())
				addConflict(event, meeting, conflict);
	}
	
	private void addConflict(EventInterface parent, MeetingInterface parentMeeting, EventInterface event) {
		TreeSet<MeetingInterface> meetings = new TreeSet<MeetingInterface>();
		for (MeetingInterface meeting: event.getMeetings()) {
			if (meeting.inConflict(parentMeeting) && (iMeetingFilter == null || !iMeetingFilter.filter(meeting))) {
				meetings.add(meeting);
			}
		}
		if (meetings.isEmpty()) return;
		
		for (MeetingInterface meeting: new TreeSet<MeetingInterface>(meetings)) {
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
						// name += "<br><span class='no-control'>" + cn + "</span>";
					} else {
						// name += "<br>" + cn;
					}
				if (event.hasExternalIds())
					for (String ex: event.getExternalIds()) {
						if (section.isEmpty()) {
							section += ex;
						} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
							// section += "<br><span class='no-control'>" + ex + "</span>";
						} else {
							// section += "<br>" + ex;
						}
					}
				else if (event.hasSectionNumber())
					section = event.getSectionNumber();
				row.add(new Hideable(new HTML(name, false)));
				row.add(new Hideable(new NumberCell(section)));
				row.add(new Hideable(new Label(event.getInstruction() == null ? event.getType().getAbbreviation() : event.getInstruction(), false)));
				if (!isColumnVisible(getHeader(MESSAGES.colSection()).getColumn())) setColumnVisible(getHeader(MESSAGES.colSection()).getColumn(), true);
			} else {
				row.add(new Hideable(new HTML(event.getName())));
				row.add(new Hideable(new HTML("&nbsp;")));
				row.add(new Hideable(new Label(event.getType().getAbbreviation(), false)));
			}
			
			row.add(new Label(sDateFormat.format(meeting.getMeetingDate())));
			row.add(new Label(meeting.getMeetingTime(CONSTANTS)));
			row.add(new Label(meeting.getAllocatedTime(CONSTANTS)));
			row.add(new NumberCell(meeting.getStartOffset()));
			row.add(new NumberCell(- meeting.getEndOffset()));
			
			if (meeting.getLocation() == null) {
				row.add(new Label(""));
				row.add(new Label(""));
			} else {
				row.add(new Label(meeting.getLocationName()));
				row.add(new NumberCell(meeting.getLocation().getSize() == null ? "N/A" : meeting.getLocation().getSize().toString()));
			}
		
			if (event.hasEnrollment() && iShowMainContact) {
				row.add(new Hideable(new NumberCell(event.getEnrollment().toString())));
				if (!isColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn())) setColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn(), true);
			} else {
				row.add(new Hideable(new HTML("&nbsp;")));
			}

			if (event.hasMaxCapacity() && iShowMainContact) {
				row.add(new Hideable(new NumberCell(event.getMaxCapacity().toString())));
				if (!isColumnVisible(getHeader(MESSAGES.colLimit()).getColumn())) setColumnVisible(getHeader(MESSAGES.colLimit()).getColumn(), true);
			} else {
				row.add(new Hideable(new HTML("&nbsp;")));
			}
			
			if (event.hasInstructors()) {
				row.add(new Hideable(new HTML(event.getInstructorNames(", "), false)));
			} else {
				row.add(new Hideable(new Label(event.hasSponsor() ? event.getSponsor().getName() : "")));
			}
			

			if (iShowMainContact) {
				row.add(new Hideable(new HTML(event.hasContact() ? event.getContact().getName() : "&nbsp;")));
			} else {
				row.add(new Hideable(new HTML("&nbsp;")));
			}
			
			if (meeting.isPast())
				for (Widget w: row)
					if (!(w instanceof Hideable)) w.addStyleName("past-meeting");
			
			row.add(new HTML(meeting.isApproved() ?
					meeting.isPast() ? "<span class='past-meeting'>" + sDateFormat.format(meeting.getApprovalDate()) + "</span>" : sDateFormat.format(meeting.getApprovalDate()) :
					meeting.isPast() ? "<span class='not-approved-past'>" + MESSAGES.approvalNotApprovedPast() + "</span>" : "<span class='not-approved'>" + MESSAGES.approvalNotApproved() + "</span>"));
			
			int rowNumber = addRow(new EventMeetingRow(parent, parentMeeting, new EventMeetingRow(event, meeting, null)), row);
			getRowFormatter().addStyleName(rowNumber, "conflict");
			for (int i = 0; i < getCellCount(rowNumber); i++)
				getCellFormatter().addStyleName(rowNumber, i, "conflict-cell");
		}
	}
	
	public void resetColumnVisibility() {
		if (getRowCount() <= 1) {
			setColumnVisible(0, false);
			setColumnVisible(getHeader(MESSAGES.colSection()).getColumn(), false);
		}
		setColumnVisible(getHeader(MESSAGES.colPublishedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
		setColumnVisible(getHeader(MESSAGES.colAllocatedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_ALLOCATED_TIME));
		setColumnVisible(getHeader(MESSAGES.colSetupTimeShort()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_SETUP_TIME));
		setColumnVisible(getHeader(MESSAGES.colTeardownTimeShort()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_TEARDOWN_TIME));
		setColumnVisible(getHeader(MESSAGES.colMainContact()).getColumn(), iShowMainContact && EventCookie.getInstance().get(EventFlag.SHOW_MAIN_CONTACT));
		setColumnVisible(getHeader(MESSAGES.colLimit()).getColumn(), iShowMainContact && EventCookie.getInstance().get(EventFlag.SHOW_LIMIT));
		setColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn(), iShowMainContact && EventCookie.getInstance().get(EventFlag.SHOW_ENROLLMENT));
		setColumnVisible(getHeader(MESSAGES.colCapacity()).getColumn(), iShowMainContact && EventCookie.getInstance().get(EventFlag.SHOW_CAPACITY));
		setColumnVisible(getHeader(MESSAGES.colSponsorOrInstructor()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_SPONSOR));
	}
	
	@Override
	public void hideUnimportantColumns() {
		setColumnVisible(getHeader(MESSAGES.colPublishedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
		setColumnVisible(getHeader(MESSAGES.colAllocatedTime()).getColumn(), !EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
		setColumnVisible(getHeader(MESSAGES.colSetupTimeShort()).getColumn(), false);
		setColumnVisible(getHeader(MESSAGES.colTeardownTimeShort()).getColumn(), false);
		setColumnVisible(getHeader(MESSAGES.colMainContact()).getColumn(), false);
		setColumnVisible(getHeader(MESSAGES.colLimit()).getColumn(), false);
		setColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn(), false);
		setColumnVisible(getHeader(MESSAGES.colCapacity()).getColumn(), false);
		setColumnVisible(getHeader(MESSAGES.colSponsorOrInstructor()).getColumn(), false);
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public String getSortBy() { return iSortBy; }
	public void setSortBy(String sortBy) {
		sort(sortBy == null || sortBy.isEmpty() ? null : EventSortBy.valueOf(sortBy));
	}
	
	protected void onSortByChanded(EventSortBy sortBy) {
		iSortBy = (sortBy == null ? null : sortBy.name());
	}
	
	protected void addSortByOperation(final UniTimeTableHeader header, final EventSortBy sortBy) {
		header.addOperation(new Operation() {
			@Override
			public void execute() { sort(sortBy); onSortByChanded(sortBy); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(header.getHTML()); }
		});
	}

	public void sort(EventSortBy sortBy) {
		if (sortBy != null) sort(createComparator(sortBy));
		iSortBy = (sortBy == null ? null : sortBy.name());
		hideSomeCells();
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
		case SHOW_SPONSOR:
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
		
	protected Comparator<EventMeetingRow> createComparator(final EventSortBy sortBy) {
		return new Comparator<EventMeetingRow>() {
			@Override
			public int compare(EventMeetingRow o1, EventMeetingRow o2) {
				int cmp = EventMeetingTable.compare(o1, o2, sortBy);
				if (cmp != 0) return cmp;
				if (o1.isConflict()) {
					if (o2.isConflict()) return EventMeetingTable.compare(o1.getConflict(), o2.getConflict(), sortBy);
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
	
	public static int compare(EventMeetingRow o1, EventMeetingRow o2, EventSortBy sortBy) {
		int cmp;
		switch (sortBy) {
		case NAME:
			cmp = o1.getEvent().getName().compareTo(o2.getEvent().getName());
			if (cmp != 0) return cmp;
			cmp = o1.getEvent().getType().compareTo(o2.getEvent().getType());
			if (cmp != 0) return cmp;
			break;
		case SECTION:
			if (o1.getEvent().hasExternalIds()) {
				if (o2.getEvent().hasExternalIds()) {
					cmp = o1.getEvent().getExternalIds().get(0).compareTo(o2.getEvent().getExternalIds().get(0));
					if (cmp != 0) return cmp;
				} else return -1;
			} else if (o2.getEvent().hasExternalIds()) return 1;
		case TYPE:
			cmp = o1.getEvent().getType().compareTo(o2.getEvent().getType());
			if (cmp != 0) return cmp;
			if (o1.getEvent().getInstructionType() != null) {
				cmp = o1.getEvent().getInstructionType().compareTo(o2.getEvent().getInstructionType());
				if (cmp != 0) return cmp;
			}
			break;
		case DATE:
			cmp = MeetingTable.compare(o1.getMeeting(), o2.getMeeting(), MeetingsSortBy.DATE);
			if (cmp != 0) return cmp;
			break;
		case PUBLISHED_TIME:
			cmp = MeetingTable.compare(o1.getMeeting(), o2.getMeeting(), MeetingsSortBy.PUBLISHED_TIME);
			if (cmp != 0) return cmp;
			break;
		case ALLOCATED_TIME:
			cmp = MeetingTable.compare(o1.getMeeting(), o2.getMeeting(), MeetingsSortBy.ALLOCATED_TIME);
			if (cmp != 0) return cmp;
			break;
		case SETUP_TIME:
			cmp = MeetingTable.compare(o1.getMeeting(), o2.getMeeting(), MeetingsSortBy.SETUP_TIME);
			if (cmp != 0) return cmp;
			break;
		case TEARDOWN_TIME:
			cmp = MeetingTable.compare(o1.getMeeting(), o2.getMeeting(), MeetingsSortBy.TEARDOWN_TIME);
			if (cmp != 0) return cmp;
			break;
		case LOCATION:
			cmp = MeetingTable.compare(o1.getMeeting(), o2.getMeeting(), MeetingsSortBy.LOCATION);
			if (cmp != 0) return cmp;
			break;
		case CAPACITY:
			cmp = MeetingTable.compare(o1.getMeeting(), o2.getMeeting(), MeetingsSortBy.CAPACITY);
			if (cmp != 0) return cmp;
			break;
		case SPONSOR:
			cmp = compare(o1.getEvent().getInstructorNames("|"), o2.getEvent().getInstructorNames("|"));
			if (cmp != 0) return cmp;
			cmp = compare(o1.getEvent().hasSponsor() ? o1.getEvent().getSponsor().getName() : null, o2.getEvent().hasSponsor() ? o2.getEvent().getSponsor().getName() : null);
			if (cmp != 0) return cmp;
			break;
		case MAIN_CONTACT:
			cmp = compare(o1.getEvent().hasContact() ? o1.getEvent().getContact().getName() : null, o2.getEvent().hasContact() ? o2.getEvent().getContact().getName() : null);
			if (cmp != 0) return cmp;
			break;
		case APPROVAL:
			cmp = MeetingTable.compare(o1.getMeeting(), o2.getMeeting(), MeetingsSortBy.APPROVAL);
			if (cmp != 0) return cmp;
			break;
		case LIMIT:
			cmp = -(o1.getEvent().hasMaxCapacity() ? o1.getEvent().getMaxCapacity() : new Integer(0)).compareTo(o2.getEvent().hasMaxCapacity() ? o2.getEvent().getMaxCapacity() : new Integer(0));
			if (cmp != 0) return cmp;
			break;
		case ENROLLMENT:
			cmp = -(o1.getEvent().hasEnrollment() ? o1.getEvent().getEnrollment() : new Integer(0)).compareTo(o2.getEvent().hasEnrollment() ? o2.getEvent().getEnrollment() : new Integer(0));
			if (cmp != 0) return cmp;
			break;
		}
		cmp = o1.getEvent().compareTo(o2.getEvent());
		if (cmp != 0) return cmp;
		return o1.getMeeting().compareTo(o2.getMeeting());
	}
	
	protected abstract class EventMeetingOperation implements Operation {
		@Override
		public void execute() {
			if (hasSelection()) {
				for (int row = getRowCount() - 1; row >= 1; row--) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) {
							EventMeetingRow e = getData(row);
							if (isApplicable(e)) execute(row, e);
						}
					}
				}
			} else if (allowNoSelection()) {
				for (int row = getRowCount() - 1; row >= 1; row--) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						EventMeetingRow e = getData(row);
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
							EventMeetingRow e = getData(row);
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
						EventMeetingRow e = getData(row);
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
		
		public List<EventMeetingRow> events() {
			List<EventMeetingRow> events = new ArrayList<EventMeetingRow>();
			if (hasSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) {
							EventMeetingRow e = getData(row);
							if (isApplicable(e)) events.add(e);
						}
					}
				}
			} else if (allowNoSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						EventMeetingRow e = getData(row);
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
							EventMeetingRow e = getData(row);
							if (isApplicable(e)) rows.add(row);
						}
					}
				}
			} else if (allowNoSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						EventMeetingRow e = getData(row);
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
		
		public abstract boolean isApplicable(EventMeetingRow event);
		public abstract void execute(int row, EventMeetingRow event);

		@Override
		public boolean hasSeparator() { return false; }
	}
	
	public EventInterface next(Long eventId) {
		boolean next = false;
		for (int row = 1; row < getRowCount(); row ++) {
			EventMeetingRow data = getData(row);
			if (!data.isConflict()) {
				if (next && !eventId.equals(data.getEvent().getId()))
					return data.getEvent();
				else if (eventId.equals(data.getEvent().getId()))
					next = true;
			}
		}
		return null;
	}
	
	public EventInterface previous(Long eventId) {
		EventInterface prev = null;
		for (int row = 1; row < getRowCount(); row ++) {
			EventMeetingRow data = getData(row);
			if (!data.isConflict()) {
				if (eventId.equals(data.getEvent().getId()))
					return prev;
				else
					prev = data.getEvent();
			}
		}
		return null;
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<EventMeetingRow>> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public List<EventMeetingRow> getValue() {
		List<EventMeetingRow> value = new ArrayList<EventMeetingRow>();
		for (int row = 1; row < getRowCount(); row++) {
			EventMeetingRow data = getData(row);
			if (data != null && !data.isConflict()) value.add(data);
		}
		return value;
	}

	@Override
	public void setValue(List<EventMeetingRow> value) {
		setValue(value, false);
	}

	@Override
	public void setValue(List<EventMeetingRow> value, boolean fireEvents) {
		clearTable(1);
		resetColumnVisibility();
		if (value != null)
			for (EventMeetingRow event: value)
				add(event.getEvent(), event.getMeeting());
		if (iSortBy != null)
			sort(createComparator(EventSortBy.valueOf(iSortBy)));
		hideSomeCells();
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}

	public void setEvents(List<EventInterface> value) {
		setEvents(value, false);
	}

	public void setEvents(List<EventInterface> value, boolean fireEvents) {
		clearTable(1);
		resetColumnVisibility();
		if (value != null)
			for (EventInterface event: value)
				add(event);
		if (iSortBy != null)
			sort(createComparator(EventSortBy.valueOf(iSortBy)));
		hideSomeCells();
		if (fireEvents)
			ValueChangeEvent.fire(this, getValue());
	}

	public static class EventMeetingRow {
		EventInterface iEvent;
		MeetingInterface iMeeting;
		EventMeetingRow iConflict;
		
		EventMeetingRow(EventInterface event, MeetingInterface meeting, EventMeetingRow conflict) {
			iEvent = event; iMeeting = meeting; iConflict = conflict;
		}
		
		public EventInterface getEvent() { return iEvent; }
		public MeetingInterface getMeeting() { return iMeeting; }
		
		public boolean isConflict() { return iConflict != null; }
		public EventMeetingRow getConflict() { return iConflict; }
	}
	
	public static class Hideable extends SimplePanel implements HasCellAlignment {
		public Hideable(Widget child) {
			super(child);
		}
		@Override
		public void setVisible(boolean visible) {
			getWidget().setVisible(visible);
		}
		@Override
		public boolean isVisible() {
			return getWidget().isVisible();
		}
		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			if (getWidget() instanceof HasCellAlignment)
				return ((HasCellAlignment)getWidget()).getCellAlignment();
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	public void hideSomeCells() {
		Long eventId = null, conflictId = null;
		for (int row = 1; row < getRowCount(); row++) {
			EventMeetingRow data = getData(row);
			if (!data.isConflict()) {
				if (eventId == null || !eventId.equals(data.getEvent().getId())) {
					for (int col = 0; col < getCellCount(row); col++) {
						Widget w = getWidget(row, col);
						if (w instanceof Hideable) w.setVisible(true);
						getCellFormatter().addStyleName(row, col, "event-cell");
					}
				} else {
					for (int col = 0; col < getCellCount(row); col++) {
						Widget w = getWidget(row, col);
						if (w instanceof Hideable) w.setVisible(false);
						getCellFormatter().removeStyleName(row, col, "event-cell");
					}
				}
			} else {
				if (conflictId == null || !conflictId.equals(data.getConflict().getEvent().getId())) {
					for (int col = 0; col < getCellCount(row); col++) {
						Widget w = getWidget(row, col);
						if (w instanceof Hideable) w.setVisible(true);
						getCellFormatter().removeStyleName(row, col, "event-cell");
					}
				} else {
					for (int col = 0; col < getCellCount(row); col++) {
						Widget w = getWidget(row, col);
						if (w instanceof Hideable) w.setVisible(false);
						getCellFormatter().removeStyleName(row, col, "event-cell");
					}
				}
			}
			eventId = data.getEvent().getId();
			conflictId = (data.isConflict() ? data.getConflict().getEvent().getId() : null);
		}
	}
}
