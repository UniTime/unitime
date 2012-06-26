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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventFlag;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConglictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MultiMeetingInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class EventMeetingTable extends UniTimeTable<EventMeetingTable.EventMeetingRow> implements HasValue<List<EventMeetingTable.EventMeetingRow>> {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	private static DateTimeFormat sDateFormatShort = DateTimeFormat.getFormat(CONSTANTS.eventDateFormatShort());
	private static DateTimeFormat sDateFormatLong = DateTimeFormat.getFormat(CONSTANTS.eventDateFormatLong());
	private static DateTimeFormat sDateFormatMeeting = DateTimeFormat.getFormat(CONSTANTS.meetingDateFormat());
	
	public static enum Mode {
		ListOfEvents(true, false, true, false),
		ListOfMeetings(true, true, true, false),
		MeetingsOfAnEvent(false, true, true, true),
		ApprovalOfEvents(true, false, false, true),
		ApprovalOfMeetings(true, true, false, true),
		ApprovalOfSingleEventMeetings(false, true, false, true);
		
		private boolean iShowEventDetails, iShowMeetings, iShowOptionalColumns, iMustShowApproval;
		
		Mode(boolean showEventDetails, boolean showMeetings, boolean showOptionalColumns, boolean mustShowApproval) {
			iShowEventDetails = showEventDetails; iShowMeetings = showMeetings; iShowOptionalColumns = showOptionalColumns; iMustShowApproval = mustShowApproval;
		}
		
		public boolean isShowEventDetails() { return iShowEventDetails; }
		public boolean isShowMeetings() { return iShowMeetings; }
		public boolean isShowOptionalColumns() { return iShowOptionalColumns; }
		public boolean isMustShowApproval() { return iMustShowApproval; }
	}
	
	public static enum OperationType {
		Approve, Reject, Inquire, AddMeetings
	}
	
	private Mode iMode = null;
	
	private boolean iShowMainContact = false;
	private EventMeetingSortBy iSortBy = null; 
	private boolean iSelectable = true, iEditable = false;
	private Map<OperationType, Implementation> iImplementations = new HashMap<OperationType, Implementation>();
	private MeetingFilter iMeetingFilter = null;

	public EventMeetingTable(Mode mode, boolean selectable) {
		setStyleName("unitime-EventMeetings");
		
		iMode = mode;
		iSelectable = selectable;
		
		if (getRowCount() > 0) clearTable();
		
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
						ch.setValue(getData(row).inConflict());
					}
				}
			}
			
			@Override
			public boolean isApplicable() {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox)
						if (getData(row).inConflict()) return true;
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
				getOperation(OperationType.AddMeetings).execute(EventMeetingTable.this, OperationType.AddMeetings, null);
			}
			@Override
			public boolean isApplicable() {
				return hasOperation(OperationType.AddMeetings);
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
		hTimes.addOperation(new EventMeetingOperation() {
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return (hasSelection() ? MESSAGES.opDeleteSelectedMeetings() : MESSAGES.opDeleteNewMeetings());
			}
			@Override
			public boolean isApplicable(EventMeetingRow row) {
				return isEditable() && row.isEditable();
			}
			@Override
			public void execute(int row, EventMeetingRow event) {
				while (row + 1 < getRowCount() && getData(row + 1).hasParent())
					removeRow(row + 1);
				removeRow(row);
				ValueChangeEvent.fire(EventMeetingTable.this, getValue());
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
				return hasOperation(OperationType.Approve) && row.isCanApprove();
			}
			@Override
			public void execute(int row, EventMeetingRow event) {
			}
			@Override
			public void execute() {
				getOperation(OperationType.Approve).execute(EventMeetingTable.this, OperationType.Approve, data());
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
				return hasOperation(OperationType.Inquire) && row.isCanApprove();
			}
			@Override
			public void execute(int row, EventMeetingRow event) {
			}
			@Override
			public void execute() {
				getOperation(OperationType.Approve).execute(EventMeetingTable.this, OperationType.Inquire, data());
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
				return hasOperation(OperationType.Reject) && row.isCanApprove();
			}
			@Override
			public void execute(int row, EventMeetingRow event) {
			}
			@Override
			public void execute() {
				getOperation(OperationType.Approve).execute(EventMeetingTable.this, OperationType.Reject, data());
			}
		});
		hTimes.addOperation(new EventMeetingOperation() {
			@Override
            public void execute() {
				Integer so = null, eo = null;
                boolean soSame = true, eoSame = true;
                for (EventMeetingRow r: data()) {
                	MeetingInterface m = r.getMeeting();
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
                        	MeetingInterface meeting = getData(row).getMeeting();
                        	if (setupTime.toInteger() != null)
                        		meeting.setStartOffset(setupTime.toInteger());
                            if (teardownTime.toInteger() != null)
                                meeting.setEndOffset(-teardownTime.toInteger());
                            ((NumberCell)getWidget(row, colSetup)).setText(String.valueOf(meeting.getStartOffset()));
                            ((NumberCell)getWidget(row, colTear)).setText(String.valueOf(-meeting.getEndOffset()));
                            ((Label)getWidget(row, colPubl)).setText(meeting.getMeetingTime(CONSTANTS));
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
            public boolean isApplicable(EventMeetingRow data) {
                    return isEditable() && data.getMeeting() != null && (data.getMeeting().getId() == null || data.getMeeting().isCanEdit());
            }
            @Override
            public void execute(int row, EventMeetingRow data) {}
		});
		
		UniTimeTableHeader hName = new UniTimeTableHeader(MESSAGES.colName());
		header.add(hName);

		UniTimeTableHeader hSection = new UniTimeTableHeader(MESSAGES.colSection(), HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hSection);

		UniTimeTableHeader hType = new UniTimeTableHeader(MESSAGES.colType());
		header.add(hType);
		
		UniTimeTableHeader hTitle = new UniTimeTableHeader(MESSAGES.colTitle());
		header.add(hTitle);

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
		
		final Operation titleOp = addHideOperation(hTitle, EventFlag.SHOW_TITLE, new Check() {
			@Override
			public boolean isChecked() {
				return true;
			}
		});
		addHideOperation(hTimePub, EventFlag.SHOW_PUBLISHED_TIME, new Check() {
			@Override
			public boolean isChecked() {
				return !titleOp.isApplicable();
			}
		});
		addHideOperation(hTimeAll, EventFlag.SHOW_ALLOCATED_TIME);
		addHideOperation(hTimeSetup, EventFlag.SHOW_SETUP_TIME);
		addHideOperation(hTimeTeardown, EventFlag.SHOW_TEARDOWN_TIME);
		addHideOperation(hCapacity, EventFlag.SHOW_CAPACITY);
		addHideOperation(hEnrollment, EventFlag.SHOW_ENROLLMENT);
		addHideOperation(hLimit, EventFlag.SHOW_LIMIT);
		addHideOperation(hSponsor, EventFlag.SHOW_SPONSOR);
		addHideOperation(hContact, EventFlag.SHOW_MAIN_CONTACT);
		addHideOperation(hApproval, EventFlag.SHOW_APPROVAL);
		
		addSortByOperation(hName, EventMeetingSortBy.NAME);
		addSortByOperation(hSection, EventMeetingSortBy.SECTION);
		addSortByOperation(hType, EventMeetingSortBy.TYPE);
		addSortByOperation(hTitle, EventMeetingSortBy.TITLE);
		addSortByOperation(hDate, EventMeetingSortBy.DATE);
		addSortByOperation(hTimePub, EventMeetingSortBy.PUBLISHED_TIME);
		addSortByOperation(hTimeAll, EventMeetingSortBy.ALLOCATED_TIME);
		addSortByOperation(hTimeSetup, EventMeetingSortBy.SETUP_TIME);
		addSortByOperation(hTimeTeardown, EventMeetingSortBy.TEARDOWN_TIME);
		addSortByOperation(hLocation, EventMeetingSortBy.LOCATION);
		addSortByOperation(hCapacity, EventMeetingSortBy.CAPACITY);
		addSortByOperation(hEnrollment, EventMeetingSortBy.ENROLLMENT);
		addSortByOperation(hLimit, EventMeetingSortBy.LIMIT);
		addSortByOperation(hSponsor, EventMeetingSortBy.SPONSOR);
		addSortByOperation(hContact, EventMeetingSortBy.MAIN_CONTACT);
		addSortByOperation(hApproval, EventMeetingSortBy.APPROVAL);
		
		for (int i = 0; i < getCellCount(0); i++)
			getCellFormatter().setStyleName(0, i, "unitime-ClickableTableHeaderNoBorderLine");

		resetColumnVisibility();
	}
	
	public Mode getMode() { return iMode; }
	public void setMode(Mode mode) { iMode = mode; }
	
	public void setSelectable(boolean selectable) { iSelectable = selectable; }
	public boolean isSelectable() { return iSelectable; }
	
	public void setEditable(boolean editable) { iEditable = editable; }
	public boolean isEditable() { return iEditable; }
	
	public MeetingFilter getMeetingFilter() { return iMeetingFilter; }
	
	public void setMeetingFilter(MeetingFilter filter) { iMeetingFilter = filter; }
	public void setShowMainContact(boolean show) { iShowMainContact = show; }
	public boolean isShowMainContact() { return iShowMainContact; }
	
	public void setOperation(OperationType operation, Implementation command) {
		if (command == null)
			iImplementations.remove(operation);
		else
			iImplementations.put(operation, command);
	}
	public Implementation getOperation(OperationType operation) { return iImplementations.get(operation); }
	public boolean hasOperation(OperationType operation) { return getOperation(operation) != null; }
	
	protected boolean isSelectable(EventMeetingRow data) {
		return (hasOperation(OperationType.Approve) && data.isCanApprove()) || (isEditable() && data.isEditable());
	}
	
	public void add(EventMeetingRow data) {
		if (!getMode().isShowMeetings() && data.getMeetings(getMeetingFilter()).isEmpty()) return;
		
		List<Widget> row = new ArrayList<Widget>();
		
		if (data.hasParent()) {
			row.add(new CenterredCell(MESSAGES.signConflict()));
		} else if (!isSelectable()) {
			row.add(new HTML(MESSAGES.signSelected()));
		} else if (isSelectable(data)) {
			row.add(new CheckBoxCell());
			if (!isColumnVisible(0)) setColumnVisible(0, true);
		} else {
			row.add(new HTML("&nbsp;"));
		}
		
		EventInterface event = data.getEvent();
		MeetingInterface meeting = data.getMeeting();
		MeetingConglictInterface conflict = (meeting instanceof MeetingConglictInterface ? (MeetingConglictInterface) meeting : null );
		
		if (event != null && event.getType() != null) {
			if (event.hasCourseNames()) {
				List<String> name = new ArrayList<String>();
				List<String> section = new ArrayList<String>();
				List<String> title = new ArrayList<String>();
				if (event.getType() == EventType.Course) { name.add(event.getName()); section.add("&nbsp;"); }
				for (String cn: event.getCourseNames())
					if (name.isEmpty()) {
						name.add(cn);
					} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
						name.add("<span class='no-control'>" + cn + "</span>");
					} else {
						name.add(cn);
					}
				if (event.hasExternalIds())
					for (String ex: event.getExternalIds()) {
						if (section.isEmpty()) {
							section.add(ex);
						} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
							section.add("<span class='no-control'>" + ex + "</span>");
						} else {
							section.add(ex);
						}
					}
				else if (event.hasSectionNumber()) {
					section.clear(); section.add(event.getSectionNumber());
				}
				if (event.hasCourseTitles()) {
					String last = null;
					for (String ct: event.getCourseTitles()) {
						if (last != null && !last.isEmpty() && last.equals(ct))
							ct = "";
						else
							last = ct;
						if (title.isEmpty()) {
							title.add(ct);
						} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
							title.add("<span class='no-control'>" + ct + "</span>");
						} else {
							title.add(ct);
						}
					}
				}
				row.add(new MultiLineCell(name));
				row.add(new MultiLineNumberCell(section));
				row.add(new Label(event.getInstruction() == null ? event.getType().getAbbreviation() : event.getInstruction(), false));
				row.add(new MultiLineCell(title));
				if (!section.isEmpty() && !isColumnVisible(getHeader(MESSAGES.colSection()).getColumn())) setColumnVisible(getHeader(MESSAGES.colSection()).getColumn(), true);
				if (!title.isEmpty() && !isColumnVisible(getHeader(MESSAGES.colTitle()).getColumn()) && EventCookie.getInstance().get(EventFlag.SHOW_TITLE) && getMode().isShowOptionalColumns()) 
					setColumnVisible(getHeader(MESSAGES.colTitle()).getColumn(), true);
			} else {
				row.add(new HTML(event.getName()));
				row.add(new HTML("&nbsp;"));
				row.add(new Label(event.getType().getAbbreviation(), false));
				row.add(new HTML("&nbsp;"));
			}
		} else if (conflict != null) {
			row.add(new HTML(conflict.getName()));
			row.add(new HTML("&nbsp;"));
			row.add(new HTML(conflict.getType().getAbbreviation(), false));
			row.add(new HTML("&nbsp;"));
		} else {
			row.add(new HTML());
			row.add(new HTML());
			row.add(new HTML());
			row.add(new HTML());
		}

		String approval = "";
		if (meeting != null) {
			if (conflict != null) {
				row.add(new HTML(conflict.getType() == EventType.Unavailabile ? conflict.getName() : MESSAGES.conflictWith(conflict.getName()), false));
				row.get(row.size() - 1).addStyleName("indent");
			} else {
				row.add(new Label(meeting.isArrangeHours() ? CONSTANTS.arrangeHours() : sDateFormatMeeting.format(meeting.getMeetingDate())));
			}
			row.add(new Label(meeting.getMeetingTime(CONSTANTS)));
			row.add(new Label(meeting.getAllocatedTime(CONSTANTS)));
			row.add(new NumberCell(meeting.getStartOffset()));
			row.add(new NumberCell(- meeting.getEndOffset()));
			if (meeting.getLocation() == null) {
				if (data.hasParent() && data.getParent().hasMeeting() && data.getParent().getMeeting().getLocation() != null) {
					row.add(new HTML(data.getParent().getMeeting().getLocationNameWithHint()));
					row.add(new NumberCell(data.getParent().getMeeting().getLocation().getSize() == null ? MESSAGES.notApplicable() : data.getParent().getMeeting().getLocation().getSize().toString()));
				} else {
					row.add(new Label(""));
					row.add(new Label(""));
				}
			} else {
				row.add(new HTML(meeting.getLocationNameWithHint()));
				row.add(new NumberCell(meeting.getLocation().getSize() == null ? MESSAGES.notApplicable() : meeting.getLocation().getSize().toString()));
			}
			if (meeting.isPast() || (data.hasParent() && data.getParent().hasMeeting() && data.getParent().getMeeting().isPast()))
				for (int i = row.size() - 7; i < row.size(); i++)
					row.get(i).addStyleName("past-meeting");
		} else {
			String[] mtgs = new String[] {"", "", "", "", "", "", ""};
			String prevApproval = null;
			String[] prev = null;
			boolean prevPast = false;
			for (MultiMeetingInterface m: EventInterface.getMultiMeetings(data.getMeetings(getMeetingFilter()), true, true)) {
				String[] mtg = new String[] {
						m.isArrangeHours() ? CONSTANTS.arrangeHours() : (m.getDays(CONSTANTS) + " " + (m.getNrMeetings() == 1 ? sDateFormatLong.format(m.getFirstMeetingDate()) : sDateFormatShort.format(m.getFirstMeetingDate()) + " - " + sDateFormatLong.format(m.getLastMeetingDate()))),
						m.getMeetings().first().getMeetingTime(CONSTANTS),
						m.getMeetings().first().getAllocatedTime(CONSTANTS),
						String.valueOf(m.getMeetings().first().getStartOffset()),
						String.valueOf(- m.getMeetings().first().getEndOffset()),
						m.getLocationNameWithHint(),
						(m.getMeetings().first().getLocation() == null ? "" : m.getMeetings().first().getLocation().hasSize() ? m.getMeetings().first().getLocation().getSize().toString() : MESSAGES.notApplicable())
						};
				for (int i = 0; i < mtgs.length; i++) {
					mtgs[i] += (mtgs[i].isEmpty() ? "" : "<br>") + (prev != null && prevPast == m.isPast() && prev[i == 6 ? i - 1 : i].equals(mtg[i == 6 ? i - 1 : i]) ? "" : ((m.isPast() ? "<span class='past-meeting'>" : "") + mtg[i] + (m.isPast() ? "</span>" : "")));
				}
				approval += (approval.isEmpty() ? "" : "<br>") + (prev != null && prevPast == m.isPast() && prevApproval.equals(m.isApproved() ? sDateFormat.format(m.getApprovalDate()) : "") ? "" : 
						(m.isApproved() ?
						m.isPast() ? "<span class='past-meeting'>" + sDateFormat.format(m.getApprovalDate()) + "</span>" : sDateFormat.format(m.getApprovalDate()) :
						m.getFirstMeetingDate() == null ? "" : m.isPast() ? "<span class='not-approved-past'>" + MESSAGES.approvalNotApprovedPast() + "</span>" : "<span class='not-approved'>" + MESSAGES.approvalNotApproved() + "</span>"));
				prev = mtg; prevPast = m.isPast(); prevApproval = (m.isApproved() ? sDateFormat.format(m.getApprovalDate()) : "");
			}
			for (int i = 0; i < mtgs.length; i++) {
				if (i == 3 || i == 4 || i == 6)
					row.add(new NumberCell(mtgs[i]));
				else
					row.add(new HTML(mtgs[i], false));
			}			
		}
		
		if (event != null && event.hasEnrollment() && iShowMainContact) {
			row.add(new NumberCell(event.getEnrollment().toString()));
		} else {
			row.add(new HTML("&nbsp;"));
		}
		
		if (event != null && event.hasMaxCapacity() && iShowMainContact) {
			row.add(new NumberCell(event.getMaxCapacity().toString()));
		} else {
			row.add(new HTML("&nbsp;"));
		}
		
		if (event != null && event.hasInstructors()) {
			row.add(new HTML(event.getInstructorNames("<br>"), false));
		} else if (event != null && event.hasSponsor()) {
			row.add(new Label(event.getSponsor().getName()));
		} else {
			row.add(new HTML("&nbsp;"));
		}
		
		if (event != null && iShowMainContact) {
			row.add(new HTML(event.hasContact() ? event.getContact().getName() : "&nbsp;"));
			if (isColumnVisible(getHeader(MESSAGES.colMainContact()).getColumn()) && EventCookie.getInstance().get(EventFlag.SHOW_MAIN_CONTACT) && getMode().isShowOptionalColumns()) {
				switch (event.getType()) {
				case Course:
				case Special:
					setColumnVisible(getHeader(MESSAGES.colMainContact()).getColumn(), true);
				}
			}
			
		} else {
			row.add(new HTML("&nbsp;"));
		}

		if (meeting != null) {
			boolean past = meeting.isPast() || (data.hasParent() && data.getParent().hasMeeting() && data.getParent().getMeeting().isPast());
			row.add(new HTML(
					conflict != null && conflict.getType() == EventType.Unavailabile ? "" :
					meeting.getMeetingDate() == null ? "" :
					meeting.getId() == null ? "<span class='new-meeting'>" + MESSAGES.approvalNewMeeting() + "</span>" :
					meeting.isApproved() ? 
							past ? "<span class='past-meeting'>" + sDateFormat.format(meeting.getApprovalDate()) + "</span>" : sDateFormat.format(meeting.getApprovalDate()) :
							past ? "<span class='not-approved-past'>" + MESSAGES.approvalNotApprovedPast() + "</span>" : "<span class='not-approved'>" + MESSAGES.approvalNotApproved() + "</span>"));
		} else {
			row.add(new HTML(approval == null ? "" : approval, false));
		}

		if (!getMode().isMustShowApproval() && !isColumnVisible(getHeader(MESSAGES.colApproval()).getColumn()) && EventCookie.getInstance().get(EventFlag.SHOW_APPROVAL)) {
			if (event != null)
				switch (event.getType()) {
				case Course:
				case Special:
					setColumnVisible(getHeader(MESSAGES.colApproval()).getColumn(), true);
				}
		}
		
		int rowNumber = addRow(data, row);
		
		if (meeting != null)
			getRowFormatter().addStyleName(rowNumber, "meeting-row");
		else if (event != null)
			getRowFormatter().addStyleName(rowNumber, "event-row");
		
		if (data.hasParent()) {
			row.get(1).addStyleName("indent");
			getRowFormatter().addStyleName(rowNumber, "conflict-row");
			for (int i = 0; i < getCellCount(rowNumber); i++)
				getCellFormatter().addStyleName(rowNumber, i, "conflict-cell");
		} else if (meeting != null) {
			for (int i = 0; i < getCellCount(rowNumber); i++)
				getCellFormatter().addStyleName(rowNumber, i, "meeting-cell");
		} else if (event != null) {
			for (int i = 0; i < getCellCount(rowNumber); i++)
				getCellFormatter().addStyleName(rowNumber, i, "event-cell");
		}

		if (!data.hasParent()) {
			if (meeting != null) {
				if (meeting.hasConflicts()) {
					for (MeetingConglictInterface cMeeting: meeting.getConflicts())
						add(new EventMeetingRow(null, cMeeting, data));
				} else if (event.hasConflicts()) {
					for (EventInterface cEvent: event.getConflicts()) {
						if (cEvent.hasMeetings())
							for (MeetingInterface cMeeting: cEvent.getMeetings()) {
								if (meeting.inConflict(cMeeting))
									add(new EventMeetingRow(cEvent, cMeeting, data));
							}
					}
				}
			} else if (event != null) {
				if (event.hasConflicts())
					for (EventInterface cEvent: event.getConflicts())
						add(new EventMeetingRow(cEvent, null, data));
			}
		}
	}
	
	private Set<Integer> getEventColumns() {
		Set<Integer> cols = new HashSet<Integer>();
		cols.add(getHeader(MESSAGES.colName()).getColumn());
		cols.add(getHeader(MESSAGES.colSection()).getColumn());
		cols.add(getHeader(MESSAGES.colType()).getColumn());
		cols.add(getHeader(MESSAGES.colEnrollment()).getColumn());
		cols.add(getHeader(MESSAGES.colLimit()).getColumn());
		cols.add(getHeader(MESSAGES.colSponsorOrInstructor()).getColumn());
		cols.add(getHeader(MESSAGES.colMainContact()).getColumn());
		return cols;
	}
	
	public void resetColumnVisibility() {
		if (getRowCount() <= 1) {
			setColumnVisible(0, false);
			setColumnVisible(getHeader(MESSAGES.colSection()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colTitle()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colMainContact()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colApproval()).getColumn(), getMode().isMustShowApproval());
		}
		setColumnVisible(getHeader(MESSAGES.colName()).getColumn(), getMode().isShowEventDetails());
		setColumnVisible(getHeader(MESSAGES.colType()).getColumn(), getMode().isShowEventDetails());
		if (getMode().isShowOptionalColumns()) {
			setColumnVisible(getHeader(MESSAGES.colPublishedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
			setColumnVisible(getHeader(MESSAGES.colAllocatedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_ALLOCATED_TIME));
			setColumnVisible(getHeader(MESSAGES.colSetupTimeShort()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_SETUP_TIME));
			setColumnVisible(getHeader(MESSAGES.colTeardownTimeShort()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_TEARDOWN_TIME));
			setColumnVisible(getHeader(MESSAGES.colLimit()).getColumn(), getMode().isShowEventDetails() && iShowMainContact && EventCookie.getInstance().get(EventFlag.SHOW_LIMIT));
			setColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn(), getMode().isShowEventDetails() && iShowMainContact && EventCookie.getInstance().get(EventFlag.SHOW_ENROLLMENT));
			setColumnVisible(getHeader(MESSAGES.colCapacity()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_CAPACITY));
			setColumnVisible(getHeader(MESSAGES.colSponsorOrInstructor()).getColumn(), getMode().isShowEventDetails() && EventCookie.getInstance().get(EventFlag.SHOW_SPONSOR));
		} else {
			setColumnVisible(getHeader(MESSAGES.colPublishedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
			setColumnVisible(getHeader(MESSAGES.colAllocatedTime()).getColumn(), !EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
			setColumnVisible(getHeader(MESSAGES.colSetupTimeShort()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colTeardownTimeShort()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colLimit()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colCapacity()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colSponsorOrInstructor()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colMainContact()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colTitle()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colApproval()).getColumn(), getMode().isMustShowApproval());
		}
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public Integer getSortBy() { return iSortBy == null ? null : iSortBy.ordinal(); }
	public void setSortBy(Integer sortBy) {
		iSortBy = (sortBy == null ? null : EventMeetingSortBy.values()[sortBy]); sort();
	}
	
	protected void addSortByOperation(final UniTimeTableHeader header, final EventMeetingSortBy sortBy) {
		header.addOperation(new Operation() {
			@Override
			public void execute() { iSortBy = sortBy; sort(); onSortByChanded(sortBy); }
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(header.getHTML()); }
		});
	}
	
	protected void onSortByChanded(EventMeetingSortBy sortBy) {};
	
	protected void onColumnShownOrHid(int eventCookieFlags) {}

	protected Operation addHideOperation(final UniTimeTableHeader header, final EventFlag flag) {
		return addHideOperation(header, flag, null);
	}
	
	protected Operation addHideOperation(final UniTimeTableHeader header, final EventFlag flag, final Check separator) {
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
				onColumnShownOrHid(EventCookie.getInstance().getFlags());
			}
			@Override
			public boolean isApplicable() {
				switch (flag) {
				case SHOW_LIMIT:
				case SHOW_ENROLLMENT:
				case SHOW_MAIN_CONTACT:
					return iShowMainContact && getMode().isShowEventDetails();
				case SHOW_SPONSOR:
					return getMode().isShowEventDetails();
				case SHOW_TITLE:
					return isColumnVisible(getHeader(MESSAGES.colSection()).getColumn());
				case SHOW_APPROVAL:
					return !getMode().isMustShowApproval();
				default:
					return true;
				}
			}
			@Override
			public boolean hasSeparator() { 
				return separator != null && separator.isChecked();
			}
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
		case SHOW_TITLE:
			getHeader(MESSAGES.colSection()).addOperation(op);
			getHeader(MESSAGES.colType()).addOperation(op);
			header.addOperation(op);
			break;
		default:
			header.addOperation(op);
		}
		return op;
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
	
	public void sort() {
		if (getSortBy() != null) {
			sort(new Comparator<EventMeetingRow>() {
				@Override
				public int compare(EventMeetingRow o1, EventMeetingRow o2) {
					int cmp = compareRows(o1.hasParent() ? o1.getParent() : o1, o2.hasParent() ? o2.getParent() : o2);
					if (cmp != 0) return cmp;
					return compareRows(o1.hasParent() ? o1 : null, o2.hasParent() ? o2 : null);
				}
			});
		}
		if (getMode().isShowMeetings() && getMode().isShowEventDetails()) {
			Long eventId = null, conflictId = null;
			Set<Integer> eventCols = getEventColumns();
			int line = 0;
			for (int row = 1; row < getRowCount(); row++) {
				EventMeetingRow data = getData(row);
				if (!data.hasParent()) {
					if (eventId == null || !eventId.equals(data.getEventId())) {
						line = 0;
						for (int col = 0; col < getCellCount(row); col++)
							getCellFormatter().addStyleName(row, col, "event-cell");
						for (int col: eventCols) {
							Widget w = getWidget(row, col);
							if (w instanceof MultiLineCell)
								((MultiLineCell)w).showLine(line);
							else
								w.setVisible(true);
						}
					} else {
						for (int col = 0; col < getCellCount(row); col++)
							getCellFormatter().removeStyleName(row, col, "event-cell");
						for (int col: eventCols) {
							Widget w = getWidget(row, col);
							if (w instanceof MultiLineCell)
								((MultiLineCell)w).showLine(line);
							else
								w.setVisible(false);
						}
					}
				} else {
					if (conflictId == null || !conflictId.equals(data.getEventId())) {
						line = 0;
						for (int col: eventCols) {
							Widget w = getWidget(row, col);
							if (w instanceof MultiLineCell)
								((MultiLineCell)w).showLine(line);
							else
								w.setVisible(true);
						}
					} else {
						for (int col: eventCols) {
							Widget w = getWidget(row, col);
							if (w instanceof MultiLineCell)
								((MultiLineCell)w).showLine(line);
							else
								w.setVisible(false);
						}
					}
				}
				eventId = (data.hasParent() ? null : data.getEventId());
				conflictId = (data.hasParent() ? data.getEventId() : null);
				line++;
			}			
		}
	}
	
	protected int compareRows(EventMeetingRow r1, EventMeetingRow r2) {
		// Null first
		if (r1 == null) return (r2 == null ? 0 : -1);
		if (r2 == null) return 1;
		
		// Compare event properties (if applicable)
		if (r1.hasEvent() && r2.hasEvent()) {
			int cmp = EventComparator.compareEvents(r1.getEvent(), r2.getEvent(), iSortBy);
			if (cmp != 0) return cmp;
		}
		
		// Compare meeting properties (if applicable)
		if (r1.hasMeeting() && r2.hasMeeting()) {
			int cmp = EventComparator.compareMeetings(r1.getMeeting(), r2.getMeeting(), iSortBy);
			if (cmp != 0) return cmp;
		} else {
			Iterator<MeetingInterface> i1 = r1.getMeetings(getMeetingFilter()).iterator(), i2 = r2.getMeetings(getMeetingFilter()).iterator();
			while (i1.hasNext() && i2.hasNext()) {
				int cmp = EventComparator.compareMeetings(i1.next(), i2.next(), iSortBy);
				if (cmp != 0) return cmp;
			}
		}
		
		// Fallback 1
		if (r1.hasEvent() && r2.hasEvent()) {
			int cmp = EventComparator.compareFallback(r1.getEvent(), r2.getEvent());
			if (cmp != 0) return cmp;
		}
		if (r1.hasMeeting() && r2.hasMeeting()) {
			int cmp = EventComparator.compareFallback(r1.getMeeting(), r2.getMeeting());
			if (cmp != 0) return cmp;
		} else {
			Iterator<MeetingInterface> i1 = r1.getMeetings(getMeetingFilter()).iterator(), i2 = r2.getMeetings(getMeetingFilter()).iterator();
			while (i1.hasNext() && i2.hasNext()) {
				int cmp = EventComparator.compareFallback(i1.next(), i2.next());
				if (cmp != 0) return cmp;
			}
			if (i1.hasNext() && !i2.hasNext()) return 1;
			if (!i1.hasNext() && i2.hasNext()) return -1;
		}
		
		// Fallback 2
		if (r1.hasMeeting() && r2.hasMeeting()) {
			return r1.getMeeting().compareTo(r2.getMeeting());
		} else  if (r1.hasEvent() && r2.hasEvent()) {
			return r1.getEvent().compareTo(r2.getEvent());
		} else {
			return 0;
		}
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
		
		public List<EventMeetingRow> data() {
			List<EventMeetingRow> data = new ArrayList<EventMeetingRow>();
			if (hasSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						CheckBox ch = (CheckBox)w;
						if (ch.getValue()) {
							EventMeetingRow e = getData(row);
							if (isApplicable(e)) data.add(e);
						}
					}
				}
			} else if (allowNoSelection()) {
				for (int row = 1; row < getRowCount(); row++) {
					Widget w =  getWidget(row, 0);
					if (w != null && w instanceof CheckBox) {
						EventMeetingRow e = getData(row);
						if (isApplicable(e)) data.add(e);
					}
				}
			}
			return data;
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
			return true;
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
			if (!data.hasParent()) {
				if (next && !eventId.equals(data.getEventId()))
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
			if (!data.hasParent()) {
				if (eventId.equals(data.getEventId()))
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
			if (data != null && !data.hasParent()) value.add(data);
		}
		return value;
	}
	
	public List<MeetingInterface> getMeetings() {
		List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
		for (int row = 1; row < getRowCount(); row++) {
			EventMeetingRow data = getData(row);
			if (data != null && data.hasMeeting() && !data.hasParent()) meetings.add(data.getMeeting());
		}
		return meetings;
	}
	
	public List<EventInterface> getEvents() {
		List<EventInterface> events = new ArrayList<EventInterface>();
		for (int row = 1; row < getRowCount(); row++) {
			EventMeetingRow data = getData(row);
			if (data != null && data.hasEvent() && !data.hasParent()) events.add(data.getEvent());
		}
		return events;
	}
	
	public Map<EventInterface, List<MeetingInterface>> getEventMeetings() {
		Map<EventInterface, List<MeetingInterface>> event2meetings = new HashMap<EventInterface, List<MeetingInterface>>();
		for (int row = 1; row < getRowCount(); row++) {
			EventMeetingRow data = getData(row);
			if (data == null || data.hasParent() || !data.hasEvent()) continue;
			List<MeetingInterface> meetings = event2meetings.get(data.getEvent());
			if (meetings == null) {
				meetings = new ArrayList<MeetingInterface>();
				event2meetings.put(data.getEvent(), meetings);
			}
			if (data.hasMeeting())
				meetings.add(data.getMeeting());
			else
				meetings.addAll(data.getMeetings(getMeetingFilter()));
		}
		return event2meetings;
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
			for (EventMeetingRow row: value)
				add(row);
		sort();
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}
	
	public void setEvents(Collection<EventInterface> events) {
		setEvents(events, false);
	}
	
	public void setEvents(Collection<EventInterface> events, boolean fireEvents) {
		List<EventMeetingRow> rows = new ArrayList<EventMeetingTable.EventMeetingRow>();
		if (events != null) {
			for (EventInterface event: events) {
				if (getMode().isShowMeetings()) {
					for (MeetingInterface meeting: event.getMeetings())
						if (getMeetingFilter() == null || !getMeetingFilter().filter(meeting))
							rows.add(new EventMeetingRow(event, meeting));
				} else {
					rows.add(new EventMeetingRow(event, null));
				}
			}
		}
		setValue(rows, fireEvents);
	}
	
	public void setMeetings(EventInterface event, Collection<MeetingInterface> meetings) {
		setMeetings(event, meetings, false);
	}
	
	public void setMeetings(EventInterface event, Collection<MeetingInterface> meetings, boolean fireEvents) {
		List<EventMeetingRow> rows = new ArrayList<EventMeetingTable.EventMeetingRow>();
		if (getMode().isShowMeetings()) {
			for (MeetingInterface meeting: meetings)
				rows.add(new EventMeetingRow(event, meeting));
		} else {
			rows.add(new EventMeetingRow(event, null));
		}
		setValue(rows, fireEvents);
	}

	public static class EventMeetingRow {
		EventInterface iEvent;
		MeetingInterface iMeeting;
		EventMeetingRow iParent;
		
		EventMeetingRow(EventInterface event, MeetingInterface meeting, EventMeetingRow parent) {
			iEvent = event; iMeeting = meeting; iParent = parent;
		}
		EventMeetingRow(EventInterface event, MeetingInterface meeting) {
			this(event, meeting, null);
		}
		
		public boolean hasEvent() { return iEvent != null; }
		public EventInterface getEvent() { return iEvent; }
		public Long getEventId() {
			if (iEvent != null) return iEvent.getId();
			if (iMeeting != null && iMeeting instanceof MeetingConglictInterface)
				return ((MeetingConglictInterface)iMeeting).getEventId();
			return null;
		}
		
		public boolean hasMeeting() { return iMeeting != null; }
		public MeetingInterface getMeeting() { return iMeeting; }
		
		public boolean hasParent() { return iParent != null; }
		public EventMeetingRow getParent() { return iParent; }
		
		public boolean inConflict() {
			if (iMeeting != null && iMeeting.hasConflicts()) return true;
			if (iEvent != null && iEvent.hasConflicts()) {
				if (iMeeting == null) return true;
				for (EventInterface conflict: iEvent.getConflicts())
					if (conflict.inConflict(iMeeting)) return true;
			}
			return false;
		}
		
		public boolean isCanApprove() {
			if (iMeeting != null) return iMeeting.isCanApprove();
			if (iEvent != null && iEvent.hasMeetings())
				for (MeetingInterface meeting: iEvent.getMeetings())
					if (meeting.isCanApprove()) return true;
			return false;
		}
		
		public boolean isEditable() {
			return iMeeting != null && (iMeeting.getId() == null || iMeeting.isCanEdit());
		}
		
		public List<MeetingInterface> getMeetings(MeetingFilter filter) {
			List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
			if (iMeeting != null) {
				if (filter == null || !filter.filter(iMeeting)) meetings.add(iMeeting);
			} else if (iEvent != null) {
				for (MeetingInterface meeting: iEvent.getMeetings())
					if (filter == null || !filter.filter(meeting)) meetings.add(meeting);
			}
			return meetings;
		}
	}
	
	private static class MultiLineCell extends HTML {
		List<String> iValue;
		
		public MultiLineCell(List<String> value) {
			super();
			setWordWrap(false);
			iValue = value;
			showLine(null);
		}
		
		public void showLine(Integer line) {
			if (line != null) {
				setHTML(line >= 0 && line < iValue.size() ? iValue.get(line) : "");
			} else {
				String html = "";
				for (String value: iValue) {
					if (!html.isEmpty()) html += "<br>";
					html += value;
				}
				setHTML(html);
			}
		}
	}
	
	private static class MultiLineNumberCell extends MultiLineCell implements HasCellAlignment {
		public MultiLineNumberCell(List<String> value) {
			super(value);
		}
		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public interface MeetingFilter {
		public boolean filter(MeetingInterface meeting);
	}
	
	public interface Implementation {
		public void execute(EventMeetingTable source, OperationType operation, List<EventMeetingRow> selection);
	}
	
	public interface Check {
		public boolean isChecked();
	}
	
	
}
