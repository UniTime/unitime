/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import org.unitime.timetable.gwt.client.events.EventAdd.EventPropertiesProvider;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.TimeSelector.TimeUtils;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.AriaOperation;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.EventFlag;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConflictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MultiMeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;

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

/**
 * @author Tomas Muller
 */
public class EventMeetingTable extends UniTimeTable<EventMeetingTable.EventMeetingRow> implements HasValue<List<EventMeetingTable.EventMeetingRow>> {
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static DateTimeFormat sDateFormatApproval = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	private static DateTimeFormat sDateFormatExpiration = ServerDateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	private static DateTimeFormat sDateFormatShort = ServerDateTimeFormat.getFormat(CONSTANTS.eventDateFormatShort());
	private static DateTimeFormat sDateFormatLong = ServerDateTimeFormat.getFormat(CONSTANTS.eventDateFormatLong());
	private static DateTimeFormat sDateFormatMeeting = ServerDateTimeFormat.getFormat(CONSTANTS.meetingDateFormat());
	private static DateTimeFormat sDateFormatAria = ServerDateTimeFormat.getFormat(CONSTANTS.dateSelectionDateFormat());
	
	public static enum ModeFlag {
		ShowEventDetails,
		ShowMeetings,
		ShowOptionalColumns,
		MustShowApproval,
		AllowApproveAll,
		HideTitle,
		CanHideDuplicitiesForMeetings,
		;
		
		public int flag() { return 1 << ordinal(); }
		public boolean in(int flags) {
			return (flags & flag()) != 0;
		}
		public int set(int flags) {
			return (in(flags) ? flags : flags + flag());
		}
		public int clear(int flags) {
			return (in(flags) ? flags - flag() : flags);
		}
	}
	
	public static enum Mode {
		ListOfEvents(ModeFlag.ShowEventDetails, ModeFlag.ShowOptionalColumns, ModeFlag.CanHideDuplicitiesForMeetings),
		ListOfMeetings(ModeFlag.ShowEventDetails, ModeFlag.ShowMeetings, ModeFlag.ShowOptionalColumns),
		MeetingsOfAnEvent(ModeFlag.ShowMeetings, ModeFlag.ShowOptionalColumns, ModeFlag.MustShowApproval, ModeFlag.AllowApproveAll, ModeFlag.HideTitle),
		ApprovalOfEvents(ModeFlag.ShowEventDetails, ModeFlag.MustShowApproval),
		ApprovalOfMeetings(ModeFlag.ShowEventDetails, ModeFlag.ShowMeetings, ModeFlag.MustShowApproval),
		ApprovalOfSingleEventMeetings(ModeFlag.ShowMeetings, ModeFlag.MustShowApproval);
		
		private int iFlags = 0;
		Mode(ModeFlag... flags) {
			for (ModeFlag flag: flags)
				iFlags = flag.set(iFlags);
		}
		
		public boolean hasFlag(ModeFlag flag) { return flag.in(iFlags); }
	}
	
	public static enum OperationType {
		Approve, Reject, Inquire, AddMeetings, Cancel, Delete, Modify
	}
	
	private Mode iMode = null;
	
	private boolean iShowMainContact = false;
	private EventMeetingSortBy iSortBy = null;
	private boolean iAsc = true;
	private boolean iSelectable = true, iEditable = false;
	private Map<OperationType, Implementation> iImplementations = new HashMap<OperationType, Implementation>();
	private MeetingFilter iMeetingFilter = null;
	private EventPropertiesProvider iPropertiesProvider = null;

	public EventMeetingTable(Mode mode, boolean selectable, EventPropertiesProvider properties) {
		setStyleName("unitime-EventMeetings");
		
		iMode = mode;
		iSelectable = selectable;
		iPropertiesProvider = properties;
		
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
				return isEditable() && row.isCanDelete();
			}
			@Override
			public void execute(int row, EventMeetingRow event) {
				while (row + 1 < getRowCount() && getData(row + 1).hasParent())
					removeRow(row + 1);
				if (getData(row).getMeeting().getId() == null)
					removeRow(row);
				else {
					MeetingInterface meeting = getData(row).getMeeting();
					meeting.setApprovalStatus(ApprovalStatus.Deleted);
					meeting.setCanApprove(false); meeting.setCanCancel(false); meeting.setCanInquire(false); meeting.setCanEdit(false); meeting.setCanDelete(false);
					getRowFormatter().addStyleName(row, "deleted-row");
					setWidget(row, 0, new HTML("&nbsp;"));
					HTML approval = (HTML)getWidget(row, getHeader(MESSAGES.colApproval()).getColumn());
					approval.setStyleName("deleted-meeting");
					approval.setText(MESSAGES.approvalDeleted());
					ValueChangeEvent.fire(EventMeetingTable.this, getValue());
				}
			}
			@Override
			public void execute() {
				super.execute();
				if (hasOperation(OperationType.Delete))
					getOperation(OperationType.Delete).execute(EventMeetingTable.this, OperationType.Delete, null);
			}
		});
		hTimes.addOperation(new EventMeetingOperation() {
			@Override
			public boolean hasSeparator() {
				return true;
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
				return hasOperation(OperationType.Inquire) && row.isCanInquire();
			}
			@Override
			public void execute(int row, EventMeetingRow event) {
			}
			@Override
			public void execute() {
				getOperation(OperationType.Inquire).execute(EventMeetingTable.this, OperationType.Inquire, data());
			}
			@Override
			public boolean allowNoSelection() {
				return getMode().hasFlag(ModeFlag.AllowApproveAll);
			}
		});
		hTimes.addOperation(new EventMeetingOperation() {
			@Override
			public boolean hasSeparator() {
				return false;
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
			@Override
			public boolean allowNoSelection() {
				return getMode().hasFlag(ModeFlag.AllowApproveAll);
			}
		});
		hTimes.addOperation(new EventMeetingOperation() {
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				if (hasOperation(OperationType.Delete))
					return (hasSelection() ? MESSAGES.opCancelSelectedMeetingsNoPopup() : MESSAGES.opCancelAllMeetingsNoPopup());
				else
					return (hasSelection() ? MESSAGES.opCancelSelectedMeetings() : MESSAGES.opCancelAllMeetings());
			}
			@Override
			public boolean isApplicable(EventMeetingRow row) {
				return (hasOperation(OperationType.Cancel) && row.isCanCancel()) || (isEditable() && row.isCanCancel());
			}
			@Override
			public void execute(int row, EventMeetingRow event) {
				if (isEditable()) {
					while (row + 1 < getRowCount() && getData(row + 1).hasParent())
						removeRow(row + 1);
					if (getData(row).getMeeting().getId() == null)
						removeRow(row);
					else {
						getData(row).getMeeting().setApprovalStatus(ApprovalStatus.Cancelled);
						getRowFormatter().addStyleName(row, "cancelled-row");
						setWidget(row, 0, new HTML("&nbsp;"));
						HTML approval = (HTML)getWidget(row, getHeader(MESSAGES.colApproval()).getColumn());
						approval.setStyleName("cancelled-meeting");
						approval.setText(MESSAGES.approvalCancelled());
						ValueChangeEvent.fire(EventMeetingTable.this, getValue());
					}					
				}
			}
			@Override
			public void execute() {
				super.execute();
				if (hasOperation(OperationType.Cancel))
					getOperation(OperationType.Cancel).execute(EventMeetingTable.this, OperationType.Cancel, data());
			}
			@Override
			public boolean allowNoSelection() {
				return getMode().hasFlag(ModeFlag.AllowApproveAll);
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
				getOperation(OperationType.Reject).execute(EventMeetingTable.this, OperationType.Reject, data());
			}
			@Override
			public boolean allowNoSelection() {
				return getMode().hasFlag(ModeFlag.AllowApproveAll);
			}
		});
		hTimes.addOperation(new EventMeetingOperation() {
			@Override
            public void execute() {
				getOperation(OperationType.Modify).execute(EventMeetingTable.this, OperationType.Modify, data());
			}
            @Override
            public String getName() {
                    return MESSAGES.opModifyMeetings();
            }
    		@Override
    		public boolean isApplicable() {
    			Integer start = null, end = null;
    			boolean hasSelection = hasSelection();
    			if (hasSelection) {
    				for (int row = 1; row < getRowCount(); row++) {
    					Widget w =  getWidget(row, 0);
    					if (w != null && w instanceof CheckBox) {
    						CheckBox ch = (CheckBox)w;
    						if (ch.getValue()) {
    							EventMeetingRow e = getData(row);
    							if (!isApplicable(e)) return false;
								if (start == null) { start = e.getMeeting().getStartSlot(); end = e.getMeeting().getEndSlot(); }
								else if (start != e.getMeeting().getStartSlot() || end != e.getMeeting().getEndSlot()) return false;
    						}
    					}
    				}
    				return true;
    			} else if (allowNoSelection()) {
    				boolean canSelect = false;
    				for (int row = 1; row < getRowCount(); row++) {
    					Widget w =  getWidget(row, 0);
    					if (w != null && w instanceof CheckBox) {
    						EventMeetingRow e = getData(row);
    						if (!isApplicable(e)) return false;
							if (start == null) { start = e.getMeeting().getStartSlot(); end = e.getMeeting().getEndSlot(); }
							else if (start != e.getMeeting().getStartSlot() || end != e.getMeeting().getEndSlot()) return false;
							canSelect = true;
    					}
    				}
    				return canSelect;
    			} else {
    				return false;
    			}
    		}
    		@Override
    		public boolean allMustMatch(boolean hasSelection) {
    			return true;
    		}
    		@Override
            public boolean isApplicable(EventMeetingRow data) {
                    return isEditable() && hasOperation(OperationType.Modify) && data.getMeeting() != null && (data.getMeeting().getId() == null || data.getMeeting().isCanDelete() || data.getMeeting().isCanCancel());
            }
            @Override
            public void execute(int row, EventMeetingRow data) {}
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

		UniTimeTableHeader hNote = new UniTimeTableHeader(MESSAGES.colNote());
		header.add(hNote);

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
		
		UniTimeTableHeader hLastChange = new UniTimeTableHeader(MESSAGES.colLastChange());
		header.add(hLastChange);

		addRow(null, header);
		
		final Operation titleOp = addHideOperation(hTitle, EventFlag.SHOW_TITLE, new Check() {
			@Override
			public boolean isChecked() {
				return true;
			}
		});
		final Operation noteOp = addHideOperation(hNote, EventFlag.SHOW_NOTE, new Check() {
			@Override
			public boolean isChecked() {
				return !titleOp.isApplicable();
			}
		});
		addHideOperation(hTimePub, EventFlag.SHOW_PUBLISHED_TIME, new Check() {
			@Override
			public boolean isChecked() {
				return !titleOp.isApplicable() && !noteOp.isApplicable();
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
		addHideOperation(hLastChange, EventFlag.SHOW_LAST_CHANGE);
		
		Operation hideDuplicitiesForMeetings = new AriaOperation() {
			@Override
			public boolean isApplicable() {
				return iMode.hasFlag(ModeFlag.CanHideDuplicitiesForMeetings);
			}

			@Override
			public boolean hasSeparator() {
				return true;
			}

			@Override
			public void execute() {
				EventCookie.getInstance().setHideDuplicitiesForMeetings(!EventCookie.getInstance().isHideDuplicitiesForMeetings());
				int colDate = getHeader(MESSAGES.colDate()).getColumn();
				int colApproval = getHeader(MESSAGES.colApproval()).getColumn();
				for (int row = 1; row < getRowCount(); row++) {
					EventMeetingRow data = getData(row);
					if (data == null) continue;
					EventInterface event = data.getEvent();
					String[] mtgs = new String[] {"", "", "", "", "", "", ""};
					String prevApproval = null;
					String[] prev = null;
					String prevSpan = null;
					String approval = "";
					boolean globalUnavailability = event != null && event.getId() != null && event.getId() < 0 && event.getType() == EventType.Unavailabile;
					for (MultiMeetingInterface m: EventInterface.getMultiMeetings(data.getMeetings(getMeetingFilter()), true, globalUnavailability ? null : iPropertiesProvider, event == null ? null : event.getType())) {
						String[] mtg = new String[] {
								m.isArrangeHours() ? CONSTANTS.arrangeHours() : (m.getDays(CONSTANTS) + " " + (m.getNrMeetings() == 1 ? sDateFormatLong.format(m.getFirstMeetingDate()) : sDateFormatShort.format(m.getFirstMeetingDate()) + " - " + sDateFormatLong.format(m.getLastMeetingDate()))),
								m.getMeetings().first().getMeetingTime(CONSTANTS),
								m.getMeetings().first().getAllocatedTime(CONSTANTS),
								String.valueOf(m.getMeetings().first().getStartOffset()),
								String.valueOf(- m.getMeetings().first().getEndOffset()),
								m.getLocationNameWithHint(),
								(m.getMeetings().first().getLocation() == null ? "" : m.getMeetings().first().getLocation().hasSize() ? m.getMeetings().first().getLocation().getSize().toString() : MESSAGES.notApplicable())
								};
						if (!m.isArrangeHours() && !m.isPast()) {
							SessionMonth.Flag dateFlag = (globalUnavailability || iPropertiesProvider == null ? null : iPropertiesProvider.getDateFlag(event == null ? null : event.getType(), m.getFirstMeetingDate()));
							if (dateFlag != null) {
								switch (dateFlag) {
								case FINALS:
									mtg[0] = "<span class='finals' title=\"" + MESSAGES.hintFinals() + "\">" + mtg[0] + "</span>";
									break;
								case BREAK:
									mtg[0] = "<span class='break' title=\"" + MESSAGES.hintBreak() + "\">" + mtg[0] + "</span>";
									break;
								case HOLIDAY:
									mtg[0] = "<span class='holiday' title=\"" + MESSAGES.hintHoliday() + "\">" + mtg[0] + "</span>";
									break;
								case WEEKEND:
									mtg[0] = "<span class='weekend' title=\"" + MESSAGES.hintWeekend() + "\">" + mtg[0] + "</span>";
									break;
								}
							}
						}
						if (!m.isArrangeHours() && iPropertiesProvider != null && iPropertiesProvider.isTooEarly(m.getMeetings().first().getStartSlot(), m.getMeetings().first().getEndSlot())) {
							for (int i = 1; i <= 2; i++)
								mtg[i] = "<span class='early' title=\"" + MESSAGES.hintTooEarly() + "\">" + mtg[i] + "</span>";
						}
						String span = "";
						if (m.getApprovalStatus() == ApprovalStatus.Cancelled)
							span = "cancelled-meeting";
						else if (m.getApprovalStatus() == ApprovalStatus.Rejected)
							span = "rejected-meeting";
						else if (m.isPast())
							span = "past-meeting";
						for (int i = 0; i < mtgs.length; i++) {
							mtgs[i] += (mtgs[i].isEmpty() ? "" : "<br>") + (prev != null && span.equals(prevSpan) && prev[i == 6 ? i - 1 : i].equals(mtg[i == 6 ? i - 1 : i]) ? MESSAGES.repeatingSymbol() : (!span.isEmpty() ? "<span class='" + span + "'>" : "") + mtg[i] + (!span.isEmpty() ? "</span>" : ""));
						}
						String thisApproval = (
								m.getApprovalStatus() == ApprovalStatus.Approved ? sDateFormatApproval.format(m.getApprovalDate()) :
								m.getApprovalStatus() == ApprovalStatus.Cancelled ? MESSAGES.approvalCancelled() :
								m.getApprovalStatus() == ApprovalStatus.Rejected ? MESSAGES.approvalRejected() :
								"");
									
						approval += (approval.isEmpty() ? "" : "<br>") + (prev != null && span.equals(prevSpan) && prevApproval.equals(thisApproval) ? MESSAGES.repeatingSymbol() : 
								(m.getApprovalStatus() == ApprovalStatus.Approved ?
								m.isPast() ? "<span class='past-meeting'>" + sDateFormatApproval.format(m.getApprovalDate()) + "</span>" : sDateFormatApproval.format(m.getApprovalDate()) :
								m.getApprovalStatus() == ApprovalStatus.Cancelled ? "<span class='cancelled-meeting'>" + MESSAGES.approvalCancelled() + "</span>":
								m.getApprovalStatus() == ApprovalStatus.Rejected ? "<span class='rejected-meeting'>" + MESSAGES.approvalRejected() + "</span>":
								event != null && event.getType() == EventType.Unavailabile ? "" : 
								m.getFirstMeetingDate() == null ? "" : m.isPast() ? "<span class='not-approved-past'>" + MESSAGES.approvalNotApprovedPast() + "</span>" :
								event != null && event.getExpirationDate() != null ? "<span class='not-approved'>" + MESSAGES.approvalExpire(sDateFormatExpiration.format(event.getExpirationDate())) + "</span>" : 
								"<span class='not-approved'>" + MESSAGES.approvalNotApproved() + "</span>"));
						if (EventCookie.getInstance().isHideDuplicitiesForMeetings()) {
							prev = mtg; prevSpan = span; prevApproval = thisApproval;
						}
					}
					for (int i = 0; i < mtgs.length; i++) {
						if (i == 3 || i == 4 || i == 6)
							setWidget(row, colDate + i, new NumberCell(mtgs[i]));
						else
							setWidget(row, colDate + i, new HTML(mtgs[i], false));
					}
					setWidget(row, colApproval, new HTML(approval == null ? "" : approval, false));
				}
			}

			@Override
			public String getName() {
				return EventCookie.getInstance().isHideDuplicitiesForMeetings()
						? MESSAGES.opUncheck(MESSAGES.opHideRepeatingInformation())
						: MESSAGES.opCheck(MESSAGES.opHideRepeatingInformation());
			}
			@Override
			public String getAriaLabel() {
				return EventCookie.getInstance().isHideDuplicitiesForMeetings()
						? ARIA.opUncheck(MESSAGES.opHideRepeatingInformation())
						: ARIA.opCheck(MESSAGES.opHideRepeatingInformation());
			}
		};
		hDate.addOperation(hideDuplicitiesForMeetings);
		hTimePub.addOperation(hideDuplicitiesForMeetings);
		hTimeAll.addOperation(hideDuplicitiesForMeetings);
		hTimeSetup.addOperation(hideDuplicitiesForMeetings);
		hTimeTeardown.addOperation(hideDuplicitiesForMeetings);
		hLocation.addOperation(hideDuplicitiesForMeetings);
		hCapacity.addOperation(hideDuplicitiesForMeetings);
		hApproval.addOperation(hideDuplicitiesForMeetings);

		addSortByOperation(hName, EventMeetingSortBy.NAME);
		addSortByOperation(hSection, EventMeetingSortBy.SECTION);
		addSortByOperation(hType, EventMeetingSortBy.TYPE);
		addSortByOperation(hTitle, EventMeetingSortBy.TITLE);
		addSortByOperation(hNote, EventMeetingSortBy.NOTE);
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
		addSortByOperation(hLastChange, EventMeetingSortBy.LAST_CHANGE);
		
		hTimes.addOperation(new AriaOperation() {
			@Override
			public void execute() {
				EventCookie.getInstance().setAutomaticallyApproveNewMeetings(!EventCookie.getInstance().isAutomaticallyApproveNewMeetings());
				for (int row = 1; row < getRowCount(); row++) {
					EventMeetingRow data = getData(row);
					if (data.hasMeeting() && data.getMeeting().getId() == null && data.getMeeting().isCanApprove() && data.hasEvent() && (data.getEvent().getType() == EventType.Special || data.getEvent().getType() == EventType.Course)) {
						HTML approval = (HTML)getWidget(row, getHeader(MESSAGES.colApproval()).getColumn());
						if (EventCookie.getInstance().isAutomaticallyApproveNewMeetings()) {
							approval.setStyleName("new-approved-meeting");
							approval.setText(MESSAGES.approvelNewApprovedMeeting());
						} else {
							approval.setStyleName("new-meeting");
							approval.setText(MESSAGES.approvalNewMeeting());
						}
					}
				}
				ValueChangeEvent.fire(EventMeetingTable.this, getValue());
			}
			@Override
			public boolean isApplicable() {
				for (int row = 1; row < getRowCount(); row++) {
					EventMeetingRow data = getData(row);
					if (data.hasMeeting() && data.getMeeting().getId() == null && data.getMeeting().isCanApprove() && data.hasEvent() && (data.getEvent().getType() == EventType.Special || data.getEvent().getType() == EventType.Course))
						return true;
				}
				return false;
			}
			@Override
			public boolean hasSeparator() {
				return true;
			}
			@Override
			public String getName() {
				return EventCookie.getInstance().isAutomaticallyApproveNewMeetings()
						? MESSAGES.opUncheck(MESSAGES.opAutomaticApproval())
						: MESSAGES.opCheck(MESSAGES.opAutomaticApproval());
			}
			@Override
			public String getAriaLabel() {
				return EventCookie.getInstance().isAutomaticallyApproveNewMeetings()
						? ARIA.opUncheck(MESSAGES.opAutomaticApproval())
						: ARIA.opCheck(MESSAGES.opAutomaticApproval());
			}
			
		});
		
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
		return (hasOperation(OperationType.Approve) && data.isCanApprove()) ||
				(hasOperation(OperationType.Cancel) && data.isCanCancel() && (!getMode().hasFlag(ModeFlag.ShowEventDetails) || isShowMainContact())) ||
				(hasOperation(OperationType.Inquire) && data.isCanInquire()) ||
				(isEditable() && (data.isEditable() || data.isCanCancel() || data.isCanDelete()));
	}
	
	public void add(EventMeetingRow data) {
		if (!getMode().hasFlag(ModeFlag.ShowMeetings) && data.getMeetings(getMeetingFilter()).isEmpty()) return;
		
		List<Widget> row = new ArrayList<Widget>();
		
		EventInterface event = data.getEvent();
		MeetingInterface meeting = data.getMeeting();
		MeetingConflictInterface conflict = (meeting instanceof MeetingConflictInterface ? (MeetingConflictInterface) meeting : null );
		if (conflict != null && conflict.getType() == EventType.Message && getMode().hasFlag(ModeFlag.ShowEventDetails)) return;
		
		if (data.hasParent()) {
			if (conflict != null && conflict.getType() == EventType.Message)
				row.add(new CenterredCell(MESSAGES.signMessage()));
			else
				row.add(new CenterredCell(MESSAGES.signConflict()));
		} else if (!isSelectable()) {
			row.add(new HTML(MESSAGES.signSelected()));
		} else if (isSelectable(data)) {
			CheckBoxCell check = new CheckBoxCell();
			check.setAriaLabel(data.toAriaString(getMode().hasFlag(ModeFlag.ShowEventDetails)));
			row.add(check);
			if (!isColumnVisible(0)) setColumnVisible(0, true);
		} else {
			row.add(new HTML("&nbsp;"));
		}
		
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
				row.add(new Label(event.getInstruction() == null ? event.getType().getAbbreviation(CONSTANTS) : event.getInstruction(), false));
				row.add(new MultiLineCell(title));
				if (event.hasEventNote() && getMode().hasFlag(ModeFlag.ShowEventDetails)) {
					MultiLineCell note = new MultiLineCell(event.getEventNote("\n").split("\\n"));
					note.setTitle(event.getEventNote("\n"));
					note.addStyleName("note");
					row.add(note);
				} else {
					row.add(new HTML("&nbsp;"));	
				}
				if (!section.isEmpty() && !isColumnVisible(getHeader(MESSAGES.colSection()).getColumn()) && EventCookie.getInstance().get(EventFlag.SHOW_TITLE) && getMode().hasFlag(ModeFlag.ShowOptionalColumns) && !getMode().hasFlag(ModeFlag.HideTitle))
					setColumnVisible(getHeader(MESSAGES.colSection()).getColumn(), true);
				if (!title.isEmpty() && !isColumnVisible(getHeader(MESSAGES.colTitle()).getColumn()) && EventCookie.getInstance().get(EventFlag.SHOW_TITLE) && getMode().hasFlag(ModeFlag.ShowOptionalColumns) && !getMode().hasFlag(ModeFlag.HideTitle)) 
					setColumnVisible(getHeader(MESSAGES.colTitle()).getColumn(), true);
				if (event.hasEventNote() && !isColumnVisible(getHeader(MESSAGES.colNote()).getColumn()) && EventCookie.getInstance().get(EventFlag.SHOW_NOTE) && getMode().hasFlag(ModeFlag.ShowOptionalColumns) && !getMode().hasFlag(ModeFlag.HideTitle)) 
					setColumnVisible(getHeader(MESSAGES.colNote()).getColumn(), true);
			} else {
				row.add(new HTML(event.getName()));
				row.add(new HTML("&nbsp;"));
				row.add(new Label(event.getType().getAbbreviation(CONSTANTS), false));
				row.add(new HTML("&nbsp;"));
				if (event.hasEventNote() && getMode().hasFlag(ModeFlag.ShowEventDetails)) {
					MultiLineCell note = new MultiLineCell(event.getEventNote("\n").split("\\n"));
					note.setTitle(event.getEventNote("\n"));
					note.addStyleName("note");
					row.add(note);
				} else {
					row.add(new HTML("&nbsp;"));	
				}
				if (event.hasEventNote() && !isColumnVisible(getHeader(MESSAGES.colNote()).getColumn()) && EventCookie.getInstance().get(EventFlag.SHOW_NOTE) && getMode().hasFlag(ModeFlag.ShowOptionalColumns) && !getMode().hasFlag(ModeFlag.HideTitle)) 
					setColumnVisible(getHeader(MESSAGES.colNote()).getColumn(), true);
			}
		} else if (conflict != null) {
			row.add(new HTML(conflict.getName()));
			row.add(new HTML("&nbsp;"));
			row.add(new HTML(conflict.getType().getAbbreviation(CONSTANTS), false));
			row.add(new HTML("&nbsp;"));
			row.add(new HTML("&nbsp;"));
		} else {
			row.add(new HTML());
			row.add(new HTML());
			row.add(new HTML());
			row.add(new HTML());
			row.add(new HTML());
		}

		String approval = "";
		boolean allCancelledOrRejected = false;
		if (meeting != null) {
			if (conflict != null && (conflict.getType() == EventType.Message || conflict.getType() == EventType.Unavailabile) && conflict.isAllDay() && !getMode().hasFlag(ModeFlag.ShowEventDetails)) {
				row.add(new HTMLWithColSpan(conflict.getName(), true, 5));
				row.get(row.size() - 1).addStyleName("indent");
			} else {
				if (conflict != null && !getMode().hasFlag(ModeFlag.ShowEventDetails)) {
					row.add(new HTML(conflict.getType() == EventType.Unavailabile || conflict.getType() == EventType.Message ? conflict.getName() : MESSAGES.conflictWith(conflict.getName()), false));
					row.get(row.size() - 1).addStyleName("indent");
				} else {
					if (meeting.isArrangeHours())
						row.add(new Label(CONSTANTS.arrangeHours()));
					else {
						Label meetingDate = new Label(sDateFormatMeeting.format(meeting.getMeetingDate()), false);
						SessionMonth.Flag dateFlag = (iPropertiesProvider == null ? null : iPropertiesProvider.getDateFlag(event == null ? null : event.getType(), meeting.getMeetingDate()));
						if (dateFlag != null) {
							switch (dateFlag) {
							case FINALS:
								meetingDate.setTitle(MESSAGES.hintFinals());
								meetingDate.addStyleName("finals");
								break;
							case BREAK:
								meetingDate.setTitle(MESSAGES.hintBreak());
								meetingDate.addStyleName("break");
								break;
							case HOLIDAY:
								meetingDate.setTitle(MESSAGES.hintHoliday());
								meetingDate.addStyleName("holiday");
								break;
							case WEEKEND:
								meetingDate.setTitle(MESSAGES.hintWeekend());
								meetingDate.addStyleName("weekend");
								break;
							}
						}
						row.add(meetingDate);
					}
				}
				Label meetingTime = new Label(meeting.getMeetingTime(CONSTANTS), false);
				Label allocatedTime = new Label(meeting.getAllocatedTime(CONSTANTS), false);
				if (!meeting.isArrangeHours() && iPropertiesProvider != null && iPropertiesProvider.isTooEarly(meeting.getStartSlot(), meeting.getEndSlot())) {
					meetingTime.setTitle(MESSAGES.hintTooEarly());
					meetingTime.addStyleName("early");
					allocatedTime.setTitle(MESSAGES.hintTooEarly());
					allocatedTime.addStyleName("early");
				}
				row.add(meetingTime);
				row.add(allocatedTime);
				row.add(new NumberCell(meeting.getStartOffset()));
				row.add(new NumberCell(- meeting.getEndOffset()));
			}
			if (meeting.getLocation() == null) {
				if (data.hasParent() && data.getParent().hasMeeting() && data.getParent().getMeeting().getLocation() != null) {
					row.add(new HTML(data.getParent().getMeeting().getLocationNameWithHint(), false));
					row.add(new NumberCell(data.getParent().getMeeting().getLocation().getSize() == null ? MESSAGES.notApplicable() : data.getParent().getMeeting().getLocation().getSize().toString()));
				} else {
					row.add(new Label(""));
					row.add(new Label(""));
				}
			} else {
				row.add(new HTML(meeting.getLocationNameWithHint(), false));
				row.add(new NumberCell(meeting.getLocation().getSize() == null ? MESSAGES.notApplicable() : meeting.getLocation().getSize().toString()));
			}
			if (meeting.isPast() || (data.hasParent() && data.getParent().hasMeeting() && data.getParent().getMeeting().isPast()))
				for (int i = row.size() - 7; i < row.size(); i++)
					row.get(i).addStyleName("past-meeting");
		} else {
			String[] mtgs = new String[] {"", "", "", "", "", "", ""};
			String prevApproval = null;
			String[] prev = null;
			String prevSpan = null;
			allCancelledOrRejected = true;
			boolean globalUnavailability = event != null && event.getId() != null && event.getId() < 0 && event.getType() == EventType.Unavailabile;
			for (MultiMeetingInterface m: EventInterface.getMultiMeetings(data.getMeetings(getMeetingFilter()), true, globalUnavailability ? null : iPropertiesProvider, event == null ? null : event.getType())) {
				String[] mtg = new String[] {
						m.isArrangeHours() ? CONSTANTS.arrangeHours() : (m.getDays(CONSTANTS) + " " + (m.getNrMeetings() == 1 ? sDateFormatLong.format(m.getFirstMeetingDate()) : sDateFormatShort.format(m.getFirstMeetingDate()) + " - " + sDateFormatLong.format(m.getLastMeetingDate()))),
						m.getMeetings().first().getMeetingTime(CONSTANTS),
						m.getMeetings().first().getAllocatedTime(CONSTANTS),
						String.valueOf(m.getMeetings().first().getStartOffset()),
						String.valueOf(- m.getMeetings().first().getEndOffset()),
						m.getLocationNameWithHint(),
						(m.getMeetings().first().getLocation() == null ? "" : m.getMeetings().first().getLocation().hasSize() ? m.getMeetings().first().getLocation().getSize().toString() : MESSAGES.notApplicable())
						};
				if (!m.isArrangeHours() && !m.isPast()) {
					SessionMonth.Flag dateFlag = (globalUnavailability || iPropertiesProvider == null ? null : iPropertiesProvider.getDateFlag(event == null ? null : event.getType(), m.getFirstMeetingDate()));
					if (dateFlag != null) {
						switch (dateFlag) {
						case FINALS:
							mtg[0] = "<span class='finals' title=\"" + MESSAGES.hintFinals() + "\">" + mtg[0] + "</span>";
							break;
						case BREAK:
							mtg[0] = "<span class='break' title=\"" + MESSAGES.hintBreak() + "\">" + mtg[0] + "</span>";
							break;
						case HOLIDAY:
							mtg[0] = "<span class='holiday' title=\"" + MESSAGES.hintHoliday() + "\">" + mtg[0] + "</span>";
							break;
						case WEEKEND:
							mtg[0] = "<span class='weekend' title=\"" + MESSAGES.hintWeekend() + "\">" + mtg[0] + "</span>";
							break;
						}
					}
				}
				if (!m.isArrangeHours() && iPropertiesProvider != null && iPropertiesProvider.isTooEarly(m.getMeetings().first().getStartSlot(), m.getMeetings().first().getEndSlot())) {
					for (int i = 1; i <= 2; i++)
						mtg[i] = "<span class='early' title=\"" + MESSAGES.hintTooEarly() + "\">" + mtg[i] + "</span>";
				}
				String span = "";
				if (m.getApprovalStatus() == ApprovalStatus.Cancelled)
					span = "cancelled-meeting";
				else if (m.getApprovalStatus() == ApprovalStatus.Rejected)
					span = "rejected-meeting";
				else if (m.isPast())
					span = "past-meeting";
				for (int i = 0; i < mtgs.length; i++) {
					mtgs[i] += (mtgs[i].isEmpty() ? "" : "<br>") + (prev != null && span.equals(prevSpan) && prev[i == 6 ? i - 1 : i].equals(mtg[i == 6 ? i - 1 : i]) ? MESSAGES.repeatingSymbol() : (!span.isEmpty() ? "<span class='" + span + "'>" : "") + mtg[i] + (!span.isEmpty() ? "</span>" : ""));
				}
				String thisApproval = (
						m.getApprovalStatus() == ApprovalStatus.Approved ? sDateFormatApproval.format(m.getApprovalDate()) :
						m.getApprovalStatus() == ApprovalStatus.Cancelled ? MESSAGES.approvalCancelled() :
						m.getApprovalStatus() == ApprovalStatus.Rejected ? MESSAGES.approvalRejected() :
						"");
							
				approval += (approval.isEmpty() ? "" : "<br>") + (prev != null && span.equals(prevSpan) && prevApproval.equals(thisApproval) ? MESSAGES.repeatingSymbol() : 
						(m.getApprovalStatus() == ApprovalStatus.Approved ?
						m.isPast() ? "<span class='past-meeting'>" + sDateFormatApproval.format(m.getApprovalDate()) + "</span>" : sDateFormatApproval.format(m.getApprovalDate()) :
						m.getApprovalStatus() == ApprovalStatus.Cancelled ? "<span class='cancelled-meeting'>" + MESSAGES.approvalCancelled() + "</span>":
						m.getApprovalStatus() == ApprovalStatus.Rejected ? "<span class='rejected-meeting'>" + MESSAGES.approvalRejected() + "</span>":
						event != null && event.getType() == EventType.Unavailabile ? "" : 
						m.getFirstMeetingDate() == null ? "" : m.isPast() ? "<span class='not-approved-past'>" + MESSAGES.approvalNotApprovedPast() + "</span>" :
						event != null && event.getExpirationDate() != null ? "<span class='not-approved'>" + MESSAGES.approvalExpire(sDateFormatExpiration.format(event.getExpirationDate())) + "</span>" : 
						"<span class='not-approved'>" + MESSAGES.approvalNotApproved() + "</span>"));
				if (EventCookie.getInstance().isHideDuplicitiesForMeetings()) {
					prev = mtg; prevSpan = span; prevApproval = thisApproval;
				}
				if (m.getApprovalStatus() != ApprovalStatus.Cancelled && m.getApprovalStatus() != ApprovalStatus.Rejected)
					allCancelledOrRejected = false;
			}
			for (int i = 0; i < mtgs.length; i++) {
				if (i == 3 || i == 4 || i == 6)
					row.add(new NumberCell(mtgs[i]));
				else
					row.add(new HTML(mtgs[i], false));
			}
		}
		
		if (event != null && event.hasEnrollment()) {
			row.add(new NumberCell(event.getEnrollment().toString()));
		} else {
			row.add(new HTML("&nbsp;"));
		}
		
		if (event != null && event.hasMaxCapacity()) {
			row.add(new NumberCell(event.getMaxCapacity().toString()));
		} else {
			row.add(new HTML("&nbsp;"));
		}
		
		if (event != null && event.hasInstructors()) {
			row.add(new HTML(event.getInstructorNames("<br>", MESSAGES), false));
		} else if (event != null && event.hasSponsor()) {
			row.add(new Label(event.getSponsor().getName()));
		} else {
			row.add(new HTML("&nbsp;"));
		}
		
		if (event != null && iShowMainContact) {
			row.add(new HTML(event.hasContact() ? event.getContact().getName(MESSAGES) : "&nbsp;"));
			if (!isColumnVisible(getHeader(MESSAGES.colMainContact()).getColumn()) && EventCookie.getInstance().get(EventFlag.SHOW_MAIN_CONTACT) && getMode().hasFlag(ModeFlag.ShowOptionalColumns)) {
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
					conflict != null && (conflict.getType() == EventType.Unavailabile || conflict.getType() == EventType.Message) ? "" :
					meeting.getApprovalStatus() == ApprovalStatus.Deleted ? "<span class='deleted-meeting'>" + MESSAGES.approvalDeleted() + "</span>":
					meeting.getApprovalStatus() == ApprovalStatus.Cancelled ? "<span class='cancelled-meeting'>" + MESSAGES.approvalCancelled() + "</span>":
					meeting.getApprovalStatus() == ApprovalStatus.Rejected ? "<span class='rejected-meeting'>" + MESSAGES.approvalRejected() + "</span>":
					meeting.getMeetingDate() == null ? "" :
					meeting.getId() == null ? event != null && event.getType() == EventType.Unavailabile ? event.getId() != null && event.getId() < 0l ? "" : "<span class='new-meeting'>" + MESSAGES.approvalNewUnavailabiliyMeeting() + "</span>" :
					event != null && (event.getType() == EventType.Class || event.getType() == EventType.FinalExam || event.getType() == EventType.MidtermExam) ? "<span class='new-approved-meeting'>" + MESSAGES.approvelNewApprovedMeeting() + "</span>" :
					meeting.isCanApprove() && EventCookie.getInstance().isAutomaticallyApproveNewMeetings() ? "<span class='new-approved-meeting'>" + MESSAGES.approvelNewApprovedMeeting() + "</span>" : "<span class='new-meeting'>" + MESSAGES.approvalNewMeeting() + "</span>" :
					meeting.isApproved() ? 
							past ? "<span class='past-meeting'>" + sDateFormatApproval.format(meeting.getApprovalDate()) + "</span>" : sDateFormatApproval.format(meeting.getApprovalDate()) :
							past ? "<span class='not-approved-past'>" + MESSAGES.approvalNotApprovedPast() + "</span>" : 
							event != null && event.getExpirationDate() != null ? "<span class='not-approved'>" + MESSAGES.approvalExpire(sDateFormatExpiration.format(event.getExpirationDate())) + "</span>" :
							"<span class='not-approved'>" + MESSAGES.approvalNotApproved() + "</span>"));
		} else {
			row.add(new HTML(approval == null ? "" : approval, false));
		}
		
		if (event != null && event.getLastNote() != null && iShowMainContact) {
			NoteInterface note = event.getLastNote();
			row.add(new Label(sDateFormatApproval.format(note.getDate()) + " " + note.getType().getName()));
		} else {
			row.add(new HTML("&nbsp;"));
		}

		if (!getMode().hasFlag(ModeFlag.MustShowApproval) && !isColumnVisible(getHeader(MESSAGES.colApproval()).getColumn()) && EventCookie.getInstance().get(EventFlag.SHOW_APPROVAL)) {
			if (event != null)
				switch (event.getType()) {
				case Course:
				case Special:
					setColumnVisible(getHeader(MESSAGES.colApproval()).getColumn(), true);
				}
		}
		
		int rowNumber = addRow(data, row);
		
		if (meeting != null)
			getRowFormatter().addStyleName(rowNumber,
					meeting.getApprovalStatus() == ApprovalStatus.Deleted ? "deleted-row" :
					meeting.getApprovalStatus() == ApprovalStatus.Cancelled ? "cancelled-row" :
					meeting.getApprovalStatus() == ApprovalStatus.Rejected ? "rejected-row" : "meeting-row");
		else if (event != null)
			getRowFormatter().addStyleName(rowNumber, allCancelledOrRejected ? "event-cancelled-row" : "event-row");
		
		if (data.hasParent()) {
			row.get(1).addStyleName("indent");
			if (conflict != null && conflict.getType() == EventType.Message) {
				getRowFormatter().addStyleName(rowNumber, "message-row");
				for (int i = 0; i < getCellCount(rowNumber); i++)
					getCellFormatter().addStyleName(rowNumber, i, "message-cell");
			} else {
				getRowFormatter().addStyleName(rowNumber, "conflict-row");
				for (int i = 0; i < getCellCount(rowNumber); i++)
					getCellFormatter().addStyleName(rowNumber, i, "conflict-cell");
			}
		} else if (meeting != null) {
			for (int i = 0; i < getCellCount(rowNumber); i++)
				getCellFormatter().addStyleName(rowNumber, i, "meeting-cell");
		} else if (event != null) {
			for (int i = 0; i < getCellCount(rowNumber); i++)
				getCellFormatter().addStyleName(rowNumber, i, "event-cell");
		}

		if (!data.hasParent()) {
			if (meeting != null) {
				if (meeting.getApprovalStatus() == ApprovalStatus.Pending ||  meeting.getApprovalStatus() == ApprovalStatus.Approved) {
					if (meeting.hasConflicts()) {
						for (MeetingConflictInterface cMeeting: meeting.getConflicts())
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
		cols.add(getHeader(MESSAGES.colTitle()).getColumn());
		cols.add(getHeader(MESSAGES.colNote()).getColumn());
		cols.add(getHeader(MESSAGES.colType()).getColumn());
		cols.add(getHeader(MESSAGES.colEnrollment()).getColumn());
		cols.add(getHeader(MESSAGES.colLimit()).getColumn());
		cols.add(getHeader(MESSAGES.colSponsorOrInstructor()).getColumn());
		cols.add(getHeader(MESSAGES.colMainContact()).getColumn());
		cols.add(getHeader(MESSAGES.colLastChange()).getColumn());
		return cols;
	}
	
	public void resetColumnVisibility() {
		if (getRowCount() <= 1) {
			setColumnVisible(0, false);
			setColumnVisible(getHeader(MESSAGES.colSection()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colTitle()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colNote()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colMainContact()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colApproval()).getColumn(), getMode().hasFlag(ModeFlag.MustShowApproval));
			setColumnVisible(getHeader(MESSAGES.colLastChange()).getColumn(), false);
		}
		setColumnVisible(getHeader(MESSAGES.colName()).getColumn(), getMode().hasFlag(ModeFlag.ShowEventDetails));
		setColumnVisible(getHeader(MESSAGES.colType()).getColumn(), getMode().hasFlag(ModeFlag.ShowEventDetails));
		if (getMode().hasFlag(ModeFlag.ShowOptionalColumns)) {
			setColumnVisible(getHeader(MESSAGES.colPublishedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_PUBLISHED_TIME));
			setColumnVisible(getHeader(MESSAGES.colAllocatedTime()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_ALLOCATED_TIME));
			setColumnVisible(getHeader(MESSAGES.colSetupTimeShort()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_SETUP_TIME));
			setColumnVisible(getHeader(MESSAGES.colTeardownTimeShort()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_TEARDOWN_TIME));
			setColumnVisible(getHeader(MESSAGES.colLimit()).getColumn(), getMode().hasFlag(ModeFlag.ShowEventDetails) && EventCookie.getInstance().get(EventFlag.SHOW_LIMIT));
			setColumnVisible(getHeader(MESSAGES.colEnrollment()).getColumn(), getMode().hasFlag(ModeFlag.ShowEventDetails) && EventCookie.getInstance().get(EventFlag.SHOW_ENROLLMENT));
			setColumnVisible(getHeader(MESSAGES.colCapacity()).getColumn(), EventCookie.getInstance().get(EventFlag.SHOW_CAPACITY));
			setColumnVisible(getHeader(MESSAGES.colSponsorOrInstructor()).getColumn(), getMode().hasFlag(ModeFlag.ShowEventDetails) && EventCookie.getInstance().get(EventFlag.SHOW_SPONSOR));
			setColumnVisible(getHeader(MESSAGES.colLastChange()).getColumn(), isShowMainContact() && getMode().hasFlag(ModeFlag.ShowEventDetails) && EventCookie.getInstance().get(EventFlag.SHOW_LAST_CHANGE));
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
			setColumnVisible(getHeader(MESSAGES.colNote()).getColumn(), false);
			setColumnVisible(getHeader(MESSAGES.colApproval()).getColumn(), getMode().hasFlag(ModeFlag.MustShowApproval));
			setColumnVisible(getHeader(MESSAGES.colLastChange()).getColumn(), false);
		}
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public String getSortBy() { return iSortBy == null ? null : (iAsc ? "" : "-") + iSortBy.ordinal(); }
	public void setSortBy(String sortBy) {
		if (sortBy == null || sortBy.isEmpty()) {
			iSortBy = null;
		} else if (sortBy.startsWith("+")) {
			iAsc = true;
			iSortBy = EventMeetingSortBy.values()[Integer.parseInt(sortBy.substring(1))];
		} else if (sortBy.startsWith("-")) {
			iAsc = false;
			iSortBy = EventMeetingSortBy.values()[Integer.parseInt(sortBy.substring(1))];
		} else {
			iAsc = true;
			iSortBy = (sortBy == null ? null : EventMeetingSortBy.values()[Integer.parseInt(sortBy)]);	
		}
		sort();
	}
	
	protected void addSortByOperation(final UniTimeTableHeader header, final EventMeetingSortBy sortBy) {
		header.addOperation(new Operation() {
			@Override
			public void execute() {
				if (header.getOrder() != null)
					iAsc = !header.getOrder();
				else
					iAsc = true;
				iSortBy = sortBy;
				sort(); onSortByChanded(sortBy, iAsc);
			}
			@Override
			public boolean isApplicable() { return getRowCount() > 1; }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(header.getHTML()); }
		});
	}
	
	protected void onSortByChanded(EventMeetingSortBy sortBy, boolean asc) {};
	
	protected void onColumnShownOrHid(int eventCookieFlags) {}

	protected Operation addHideOperation(final UniTimeTableHeader header, final EventFlag flag) {
		return addHideOperation(header, flag, null);
	}
	
	protected Operation addHideOperation(final UniTimeTableHeader header, final EventFlag flag, final Check separator) {
		Operation op = new AriaOperation() {
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
				case SHOW_MAIN_CONTACT:
				case SHOW_LAST_CHANGE:
					return iShowMainContact && getMode().hasFlag(ModeFlag.ShowEventDetails);
				case SHOW_LIMIT:
				case SHOW_ENROLLMENT:
				case SHOW_SPONSOR:
				case SHOW_NOTE:
					return getMode().hasFlag(ModeFlag.ShowEventDetails);
				case SHOW_TITLE:
					return isColumnVisible(getHeader(MESSAGES.colSection()).getColumn());
				case SHOW_APPROVAL:
					return !getMode().hasFlag(ModeFlag.MustShowApproval);
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
			@Override
			public String getAriaLabel() { return isColumnVisible(header.getColumn()) ? ARIA.opHide(header.getHTML()) : ARIA.opShow(header.getHTML()); }
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
		case SHOW_LAST_CHANGE:
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
		if (iSortBy != null) {
			UniTimeTableHeader header = null;
			switch (iSortBy) {
			case NAME:
				header = getHeader(MESSAGES.colName()); 
				break;
			case SECTION:
				header = getHeader(MESSAGES.colSection()); 
				break;
			case TYPE:
				header = getHeader(MESSAGES.colType()); 
				break;
			case DATE:
				header = getHeader(MESSAGES.colDate()); 
				break;
			case PUBLISHED_TIME:
				header = getHeader(MESSAGES.colPublishedTime()); 
				break;
			case ALLOCATED_TIME:
				header = getHeader(MESSAGES.colAllocatedTime()); 
				break;
			case SETUP_TIME:
				header = getHeader(MESSAGES.colSetupTimeShort()); 
				break;
			case TEARDOWN_TIME:
				header = getHeader(MESSAGES.colTeardownTimeShort()); 
				break;
			case LOCATION:
				header = getHeader(MESSAGES.colLocation()); 
				break;
			case CAPACITY:
				header = getHeader(MESSAGES.colCapacity()); 
				break;
			case SPONSOR:
				header = getHeader(MESSAGES.colSponsorOrInstructor()); 
				break;
			case MAIN_CONTACT:
				header = getHeader(MESSAGES.colMainContact()); 
				break;
			case APPROVAL:
				header = getHeader(MESSAGES.colApproval()); 
				break;
			case LIMIT:
				header = getHeader(MESSAGES.colLimit()); 
				break;
			case ENROLLMENT:
				header = getHeader(MESSAGES.colEnrollment()); 
				break;
			case TITLE:
				header = getHeader(MESSAGES.colTitle()); 
				break;
			case NOTE:
				header = getHeader(MESSAGES.colNote()); 
				break;
			case LAST_CHANGE:
				header = getHeader(MESSAGES.colLastChange()); 
				break;
			}
			sort(header, new Comparator<EventMeetingRow>() {
				@Override
				public int compare(EventMeetingRow o1, EventMeetingRow o2) {
					int cmp = compareRows(o1.hasParent() ? o1.getParent() : o1, o2.hasParent() ? o2.getParent() : o2);
					if (cmp != 0) return cmp;
					return compareRows(o1.hasParent() ? o1 : null, o2.hasParent() ? o2 : null);
				}
			}, iAsc);
		}
		if (getMode().hasFlag(ModeFlag.ShowMeetings) && getMode().hasFlag(ModeFlag.ShowEventDetails)) {
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
				if (getMode().hasFlag(ModeFlag.ShowMeetings)) {
					for (MeetingInterface meeting: event.getMeetings())
						if (getMeetingFilter() == null || !getMeetingFilter().filter(event, meeting))
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
		if (getMode().hasFlag(ModeFlag.ShowMeetings)) {
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
			if (iMeeting != null && iMeeting instanceof MeetingConflictInterface)
				return ((MeetingConflictInterface)iMeeting).getEventId();
			return null;
		}
		
		public boolean hasMeeting() { return iMeeting != null; }
		public MeetingInterface getMeeting() {
			if (iMeeting != null && iMeeting.getId() == null) {
				if (EventCookie.getInstance().isAutomaticallyApproveNewMeetings() && iMeeting.isCanApprove())
					iMeeting.setApprovalStatus(ApprovalStatus.Approved);
				else
					iMeeting.setApprovalStatus(ApprovalStatus.Pending);
			}
			return iMeeting;
		}
		
		public boolean hasParent() { return iParent != null; }
		public EventMeetingRow getParent() { return iParent; }
		
		public boolean inConflict() {
			if (iMeeting != null && iMeeting.inConflict()) return true;
			if (iEvent != null && iEvent.hasConflicts()) {
				if (iMeeting == null) return true;
				for (EventInterface conflict: iEvent.getConflicts()) {
					if (conflict.getType() == EventType.Message) continue;
					if (conflict.inConflict(iMeeting)) return true;
				}
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
		
		public boolean isCanInquire() {
			if (iMeeting != null) return iMeeting.isCanInquire();
			if (iEvent != null && iEvent.hasMeetings())
				for (MeetingInterface meeting: iEvent.getMeetings())
					if (meeting.isCanInquire()) return true;
			return false;
		}

		public boolean isCanCancel() {
			if (iMeeting != null) return iMeeting.isCanCancel();
			if (iEvent != null && iEvent.hasMeetings())
				for (MeetingInterface meeting: iEvent.getMeetings())
					if (meeting.isCanCancel()) return true;
			return false;
		}
		
		public boolean isCanDelete() {
			if (iMeeting != null) return iMeeting.isCanDelete();
			if (iEvent != null && iEvent.hasMeetings())
				for (MeetingInterface meeting: iEvent.getMeetings())
					if (meeting.isCanDelete()) return true;
			return false;
		}

		public boolean isEditable() {
			return iMeeting != null && (iMeeting.getId() == null || iMeeting.isCanEdit());
		}
		
		public List<MeetingInterface> getMeetings(MeetingFilter filter) {
			List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
			if (iMeeting != null) {
				if (filter == null || !filter.filter(iParent == null ? iEvent : iParent.iEvent, iMeeting)) meetings.add(iMeeting);
			} else if (iEvent != null && iEvent.hasMeetings()) {
				for (MeetingInterface meeting: iEvent.getMeetings())
					if (filter == null || !filter.filter(iParent == null ? iEvent : iParent.iEvent, meeting)) meetings.add(meeting);
			}
			return meetings;
		}
		
		public String toAriaString(boolean includeEventInfo) {
			String label = "";
			if (includeEventInfo && hasEvent()) {
				label += (getEvent().getType() == null || getEvent().getType() == EventType.Unavailabile ? "" : getEvent().getType().getAbbreviation(CONSTANTS));
				if (getEvent().getName() != null)
					label += " " + getEvent().getName();
			}
			if (hasMeeting() && getMeeting().getMeetingDate() != null) {
				String room = "", hint = "";
				if (getMeeting().getLocation() != null) {
					room = getMeeting().getLocation().getName();
					if (getMeeting().getLocation().hasRoomType())
						hint = getMeeting().getLocation().getRoomType();
				}
				label += ARIA.dateTimeRoomSelection(sDateFormatAria.format(getMeeting().getMeetingDate()), TimeUtils.slot2aria(getMeeting().getStartSlot()), TimeUtils.slot2aria(getMeeting().getEndSlot()), room, hint);
			}
			return label;
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
		
		public MultiLineCell(String... values) {
			super();
			setWordWrap(false);
			iValue = new ArrayList<String>();
			for (String value: values)
				iValue.add(value);
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
		public boolean filter(EventInterface event, MeetingInterface meeting);
	}
	
	public interface Implementation {
		public void execute(EventMeetingTable source, OperationType operation, List<EventMeetingRow> selection);
	}
	
	public interface Check {
		public boolean isChecked();
	}
	
	public static class HTMLWithColSpan extends HTML implements UniTimeTable.HasColSpan {
		private int iColspan = 1;
		public HTMLWithColSpan(String html, boolean wordWrap, int colspan) {
			super(html, wordWrap);
			iColspan = colspan;
		}
		@Override
		public int getColSpan() { return iColspan; }
	}
	
}
