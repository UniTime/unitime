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
package org.unitime.timetable.gwt.client.rooms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.AriaOperation;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFlag;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertyInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsPageMode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class RoomsTable extends UniTimeTable<RoomDetailInterface>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private int iFlags = 0;
	private RoomsComparator.Column iSortBy = null;
	private boolean iAsc = true;
	private RoomsPageMode iMode = null;
	private String iDepartment = null;
	private List<Operation> iSortOperations = new ArrayList<Operation>();
	private List<Operation> iShowHideOperations = new ArrayList<Operation>();
	private List<Operation> iDepartmentOperations = new ArrayList<Operation>();
	private List<Operation> iOtherOperations = new ArrayList<Operation>();
	
	public RoomsTable(RoomsPageMode mode) {
		setStyleName("unitime-Rooms");
		iMode = mode;

		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		
		UniTimeTableHeader hName = new UniTimeTableHeader(MESSAGES.colName());
		header.add(hName);
		
		UniTimeTableHeader hType = new UniTimeTableHeader(MESSAGES.colType());
		header.add(hType);
		
		UniTimeTableHeader hCapacity = new UniTimeTableHeader(MESSAGES.colCapacity(), HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hCapacity);
		
		UniTimeTableHeader hExamCapacity = new UniTimeTableHeader(MESSAGES.colExaminationCapacity(), HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hExamCapacity);
		
		UniTimeTableHeader hArea = new UniTimeTableHeader(MESSAGES.colArea(), HasHorizontalAlignment.ALIGN_RIGHT);
		header.add(hArea);
		
		UniTimeTableHeader hCoordinates = new UniTimeTableHeader(MESSAGES.colCoordinates());
		header.add(hCoordinates);
		
		UniTimeTableHeader hDistances = new UniTimeTableHeader(MESSAGES.colDistances());
		header.add(hDistances);
		
		UniTimeTableHeader hRoomCheck = new UniTimeTableHeader(MESSAGES.colRoomCheck());
		header.add(hRoomCheck);
		
		UniTimeTableHeader hPreference = new UniTimeTableHeader(MESSAGES.colPreference());
		header.add(hPreference);
		
		UniTimeTableHeader hAvailability = new UniTimeTableHeader(MESSAGES.colAvailability());
		header.add(hAvailability);
		
		UniTimeTableHeader hDepartments = new UniTimeTableHeader(MESSAGES.colDepartments());
		header.add(hDepartments);
		
		UniTimeTableHeader hControl = new UniTimeTableHeader(MESSAGES.colControl());
		header.add(hControl);
		
		UniTimeTableHeader hExamTypes = new UniTimeTableHeader(MESSAGES.colExamTypes());
		header.add(hExamTypes);
		
		UniTimeTableHeader hPeriodPrefs = new UniTimeTableHeader(MESSAGES.colPeriodPreferences());
		header.add(hPeriodPrefs);
		
		UniTimeTableHeader hEventDepartment = new UniTimeTableHeader(MESSAGES.colEventDepartment());
		header.add(hEventDepartment);
		
		UniTimeTableHeader hEventStatus = new UniTimeTableHeader(MESSAGES.colEventStatus());
		header.add(hEventStatus);
		
		UniTimeTableHeader hEventAvailability = new UniTimeTableHeader(MESSAGES.colEventAvailability());
		header.add(hEventAvailability);
		
		UniTimeTableHeader hEventMessage = new UniTimeTableHeader(MESSAGES.colEventMessage());
		header.add(hEventMessage);
		
		UniTimeTableHeader hBreakTime = new UniTimeTableHeader(MESSAGES.colBreakTime());
		header.add(hBreakTime);
		
		UniTimeTableHeader hGroups = new UniTimeTableHeader(MESSAGES.colGroups());
		header.add(hGroups);
		
		UniTimeTableHeader hFeatures = new UniTimeTableHeader(MESSAGES.colFeatures());
		header.add(hFeatures);

		addRow(null, header);
				
		addHideOperation(hType, RoomFlag.SHOW_TYPE);
		addHideOperation(hCapacity, RoomFlag.SHOW_CAPACITY);
		addHideOperation(hExamCapacity, RoomFlag.SHOW_EXAM_CAPACITY);
		addHideOperation(hArea, RoomFlag.SHOW_AREA);
		addHideOperation(hCoordinates, RoomFlag.SHOW_COORDINATES);
		addHideOperation(hDistances, RoomFlag.SHOW_IGNORE_DISTANCES);
		addHideOperation(hRoomCheck, RoomFlag.SHOW_IGNORE_ROOM_CHECK);
		addHideOperation(hPreference, RoomFlag.SHOW_PREFERENCE);
		addHideOperation(hAvailability, RoomFlag.SHOW_AVAILABILITY);
		addHideOperation(hDepartments, RoomFlag.SHOW_DEPARTMENTS);
		addHideOperation(hControl, RoomFlag.SHOW_CONTROLLING_DEPARTMENT);
		addHideOperation(hExamTypes, RoomFlag.SHOW_EXAM_TYPES);
		addHideOperation(hPeriodPrefs, RoomFlag.SHOW_PERIOD_PREFERENCES);
		addHideOperation(hEventDepartment, RoomFlag.SHOW_EVENT_DEPARTMENT);
		addHideOperation(hEventStatus, RoomFlag.SHOW_EVENT_STATUS);
		addHideOperation(hEventAvailability, RoomFlag.SHOW_EVENT_AVAILABILITY);
		addHideOperation(hEventMessage, RoomFlag.SHOW_EVENT_MESSAGE);
		addHideOperation(hBreakTime, RoomFlag.SHOW_BREAK_TIME);
		addHideOperation(hGroups, RoomFlag.SHOW_GROUPS);
		addHideOperation(hFeatures, RoomFlag.SHOW_FEATURES);
		
		for (final DeptMode d: DeptMode.values()) {
			Operation op = new Operation() {
				@Override
				public void execute() {
					RoomCookie.getInstance().setDeptMode(d.ordinal());
					for (int r = 1; r < getRowCount(); r++) {
						for (int c = 0; c < getCellCount(r); c++) {
							Widget w = getWidget(r, c);
							if (w instanceof HasRefresh)
								((HasRefresh)w).refresh();
						}
					}
				}
				@Override
				public boolean isApplicable() { 
					return getRowCount() > 0;
				}
				@Override
				public boolean hasSeparator() {
					return d.ordinal() == 0;
				}
				@Override
				public String getName() {
					if (RoomCookie.getInstance().getDeptMode() == d.ordinal())
						return MESSAGES.opUncheck(d.getName());
					else
						return MESSAGES.opCheck(d.getName());
				}
			};
			iDepartmentOperations.add(op);
			hDepartments.addOperation(op);
			hControl.addOperation(op);
			hEventDepartment.addOperation(op);
		}
		
		addSortByOperation(hName, RoomsComparator.Column.NAME);
		addSortByOperation(hType, RoomsComparator.Column.TYPE);
		addSortByOperation(hCapacity, RoomsComparator.Column.CAPACITY);
		addSortByOperation(hExamCapacity, RoomsComparator.Column.EXAM_CAPACITY);
		addSortByOperation(hDistances, RoomsComparator.Column.DISTANCE);
		addSortByOperation(hRoomCheck, RoomsComparator.Column.ROOM_CHECK);
		addSortByOperation(hControl, RoomsComparator.Column.CONTROL);
		addSortByOperation(hEventDepartment, RoomsComparator.Column.EVENT_DEPT);
		addSortByOperation(hEventStatus, RoomsComparator.Column.EVENT_STATUS);
		addSortByOperation(hEventMessage, RoomsComparator.Column.EVENT_MESSAGE);
		addSortByOperation(hBreakTime, RoomsComparator.Column.BREAK_TIME);
		
		addOperation(new Operation() {

			@Override
			public void execute() {
				final RoomsTable table = new RoomsTable(iMode);
				table.setDepartment(iDepartment);
				for (int i = 1; i < getRowCount(); i++)
					table.addRoom(getData(i));
				table.getElement().getStyle().setWidth(1040, Unit.PX);
				
				// Move header row to thead
				Element headerRow = table.getRowFormatter().getElement(0);
				Element tableElement = table.getElement();
				Element thead = DOM.createTHead();
				tableElement.insertFirst(thead);
				headerRow.getParentElement().removeChild(headerRow);
				thead.appendChild(headerRow);
				
				ToolBox.print(
						new ToolBox.Page() {
							@Override
							public String getName() { return MESSAGES.pageRooms(); }
							@Override
							public String getUser() { return UniTimePageHeader.getInstance().getMiddle().getText(); }
							@Override
							public String getSession() { return UniTimePageHeader.getInstance().getRight().getText(); }
							@Override
							public Element getBody() { return table.getElement(); }
						}
						);
			}

			@Override
			public String getName() {
				return MESSAGES.buttonPrint();
			}

			@Override
			public boolean isApplicable() {
				return getRowCount() > 1;
			}

			@Override
			public boolean hasSeparator() {
				return true;
			}
		});
		
		
		for (int i = 0; i < getCellCount(0); i++)
			getCellFormatter().setStyleName(0, i, "unitime-ClickableTableHeader");
		
		resetVisibility();
		setSortBy(RoomCookie.getInstance().getRoomsSortBy());
	}
	
	public List<Operation> getSortOperations() {
		ArrayList<Operation> operations = new ArrayList<Operation>();
		for (Operation operation: iSortOperations)
			if (operation.isApplicable())
				operations.add(operation);
		return operations;
	}
	
	public List<Operation> getShowHideOperations() {
		ArrayList<Operation> operations = new ArrayList<Operation>();
		for (Operation operation: iShowHideOperations)
			if (operation.isApplicable())
				operations.add(operation);
		return operations;
	}
	
	public List<Operation> getDepartmentOperations() {
		ArrayList<Operation> operations = new ArrayList<Operation>();
		for (Operation operation: iDepartmentOperations)
			if (operation.isApplicable())
				operations.add(operation);
		return operations;
	}
	
	public List<Operation> getOtherOperations() {
		ArrayList<Operation> operations = new ArrayList<Operation>();
		for (Operation operation: iOtherOperations)
			if (operation.isApplicable())
				operations.add(operation);
		return operations;
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() { return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal(); }
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
		} else if (sortBy > 0) {
			iSortBy = RoomsComparator.Column.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = RoomsComparator.Column.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	protected void addOperation(Operation op) {
		iOtherOperations.add(op);
		getHeader(MESSAGES.colName()).addOperation(op);
	}
	
	protected void addSortByOperation(final UniTimeTableHeader header, final RoomsComparator.Column sortBy) {
		Operation op = new SortOperation() {
			@Override
			public void execute() {
				if (header.getOrder() != null)
					iAsc = !header.getOrder();
				else
					iAsc = true;
				iSortBy = sortBy;
				RoomCookie.getInstance().setSortRoomsBy(getSortBy());
				sort();
			}
			@Override
			public boolean isApplicable() { return getRowCount() > 1 && header.isVisible(); }
			@Override
			public boolean hasSeparator() { return true; }
			@Override
			public String getName() { return MESSAGES.opSortBy(getColumnName()); }
			@Override
			public String getColumnName() { return header.getHTML().replace("<br>", " "); }
		};
		iSortOperations.add(op);
		header.addOperation(op);
	}
	
	public void sort() {
		if (iSortBy == null) return;
		UniTimeTableHeader header = null;
		switch (iSortBy) {
		case NAME:
			header = getHeader(MESSAGES.colName()); 
			break;
		case TYPE:
			header = getHeader(MESSAGES.colType()); 
			break;
		case CAPACITY:
			header = getHeader(MESSAGES.colCapacity()); 
			break;
		case EXAM_CAPACITY:
			header = getHeader(MESSAGES.colExaminationCapacity()); 
			break;
		case DISTANCE:
			header = getHeader(MESSAGES.colDistances()); 
			break;
		case ROOM_CHECK:
			header = getHeader(MESSAGES.colRoomCheck()); 
			break;
		case CONTROL:
			header = getHeader(MESSAGES.colControl()); 
			break;
		case EVENT_DEPT:
			header = getHeader(MESSAGES.colEventDepartment()); 
			break;
		case EVENT_STATUS:
			header = getHeader(MESSAGES.colEventStatus()); 
			break;
		case EVENT_MESSAGE:
			header = getHeader(MESSAGES.colEventMessage()); 
			break;
		case BREAK_TIME:
			header = getHeader(MESSAGES.colBreakTime()); 
			break;
		}
		sort(header, new RoomsComparator(iSortBy, true), iAsc);
	}
	
	protected Operation addHideOperation(final UniTimeTableHeader header, final RoomFlag flag, final Check separator) {
		Operation op = new AriaOperation() {
			@Override
			public void execute() {
				boolean visible = isColumnVisible(header.getColumn());
				setColumnVisible(header.getColumn(), !visible);
				RoomCookie.getInstance().set(iMode, flag, !visible);
			}
			@Override
			public boolean isApplicable() {
				if (!flag.isShowWhenEmpty())
					return flag.in(iFlags);
				return true;
			}
			@Override
			public boolean hasSeparator() { 
				return separator != null && separator.isChecked();
			}
			@Override
			public String getName() { return isColumnVisible(header.getColumn()) ? MESSAGES.opHide(header.getHTML().replace("<br>", " ")) : MESSAGES.opShow(header.getHTML().replace("<br>", " ")); }
			@Override
			public String getAriaLabel() { return isColumnVisible(header.getColumn()) ? ARIA.opHide(header.getHTML().replace("<br>", " ")) : ARIA.opShow(header.getHTML().replace("<br>", " ")); }
		};
		iShowHideOperations.add(op);
		getHeader(MESSAGES.colName()).addOperation(op);
		switch (flag) {
		case SHOW_EVENT_AVAILABILITY:
		case SHOW_EVENT_DEPARTMENT:
		case SHOW_EVENT_MESSAGE:
		case SHOW_EVENT_STATUS:
		case SHOW_BREAK_TIME:
			getHeader(MESSAGES.colEventAvailability()).addOperation(op);
			getHeader(MESSAGES.colEventDepartment()).addOperation(op);
			getHeader(MESSAGES.colEventMessage()).addOperation(op);
			getHeader(MESSAGES.colEventStatus()).addOperation(op);
			getHeader(MESSAGES.colBreakTime()).addOperation(op);
			break;
		case SHOW_CAPACITY:
		case SHOW_EXAM_CAPACITY:
		case SHOW_AREA:
		case SHOW_COORDINATES:
			getHeader(MESSAGES.colExaminationCapacity()).addOperation(op);
			getHeader(MESSAGES.colCapacity()).addOperation(op);
			getHeader(MESSAGES.colArea()).addOperation(op);
			getHeader(MESSAGES.colCoordinates()).addOperation(op);
			break;
		case SHOW_DEPARTMENTS:
		case SHOW_CONTROLLING_DEPARTMENT:
		case SHOW_AVAILABILITY:
			getHeader(MESSAGES.colDepartments()).addOperation(op);
			getHeader(MESSAGES.colControl()).addOperation(op);
			getHeader(MESSAGES.colAvailability()).addOperation(op);
			break;
		case SHOW_IGNORE_DISTANCES:
		case SHOW_IGNORE_ROOM_CHECK:
			getHeader(MESSAGES.colRoomCheck()).addOperation(op);
			getHeader(MESSAGES.colDistances()).addOperation(op);
			break;
		case SHOW_PERIOD_PREFERENCES:
		case SHOW_EXAM_TYPES:
			getHeader(MESSAGES.colExaminationCapacity()).addOperation(op);
			getHeader(MESSAGES.colExamTypes()).addOperation(op);
			getHeader(MESSAGES.colPeriodPreferences()).addOperation(op);
			break;
		default:
			header.addOperation(op);
		}
		return op;
	}
	
	protected Operation addHideOperation(final UniTimeTableHeader header, final RoomFlag flag) {
		return addHideOperation(header, flag, null);
	}
	
	protected Integer getRow(Long roomId) {
		for (int row = 1; row < getRowCount(); row++) {
			RoomDetailInterface room = getData(row);
			if (room != null && roomId.equals(room.getUniqueId())) return row;
		}
		return null;
	}
	
	public void setDepartment(String department) { iDepartment = department; }
	
	public int addRoom(final RoomDetailInterface room) {
		RoomCookie cookie = RoomCookie.getInstance();
		
		List<Widget> widgets = new ArrayList<Widget>();
		final Label roomLabel = new Label(room.hasDisplayName() ? MESSAGES.label(room.getLabel(), room.getDisplayName()) : room.getLabel());
		roomLabel.addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				RoomHint.showHint(roomLabel.getElement(), room.getUniqueId(), room.getPrefix(), room.getProperty("distance", null), true);
			}
		});
		roomLabel.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				RoomHint.hideHint();
			}
		});
		if (iDepartment != null) {
			for (DepartmentInterface d: room.getDepartments()) {
				if (iDepartment.equals(d.getDeptCode()) && d.getPreference() != null) {
					roomLabel.getElement().getStyle().setColor(d.getPreference().getColor());
					room.setPrefix(d.getPreference().getName());
				}
			}
		}
		widgets.add(roomLabel);
		
		widgets.add(new Label(room.getRoomType().getLabel()));
		
		widgets.add(new IntegerCell(room.getCapacity()));
		widgets.add(new IntegerCell(room.getExamCapacity()));
		if (room.getExamCapacity() != null) show(RoomFlag.SHOW_EXAM_CAPACITY);
		
		widgets.add(new RoomAreaCell(room.getArea()));
		if (room.getArea() != null) show(RoomFlag.SHOW_AREA);
		
		widgets.add(new Label(room.hasCoordinates() ? MESSAGES.coordinates(room.getX(), room.getY()) : ""));
		if (room.hasCoordinates()) show(RoomFlag.SHOW_COORDINATES);
		
		widgets.add(new Image(!room.isIgnoreTooFar() ? RESOURCES.on() : RESOURCES.off()));
		widgets.add(new Image(!room.isIgnoreRoomCheck() ? RESOURCES.on() : RESOURCES.off()));
		
		widgets.add(new PreferenceCell(room.getDepartments()));
		if (room.hasPreference(iDepartment)) show(RoomFlag.SHOW_PREFERENCE);
		
		if (room.getAvailability() != null) {
			final HTML availability = new HTML(room.getAvailability());
			availability.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					RoomSharingHint.showHint(availability.getElement(), room.getUniqueId(), false);
				}
			});
			availability.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					RoomSharingHint.hideHint();
				}
			});
			if (room.getRoomSharingNote() != null && !room.getRoomSharingNote().isEmpty()) {
				P p = new P();
				p.add(availability);
				P note = new P("note");
				note.setHTML(room.getRoomSharingNote());
				note.setTitle(room.getRoomSharingNote());
				p.add(note);
				widgets.add(p);
			} else {
				widgets.add(availability);
			}
		} else if (room.isCanSeeAvailability()) {
			final Image availability = new Image("pattern?loc=" + room.getUniqueId() + "&v=" + (cookie.areRoomsHorizontal() ? "0" : "1") + (cookie.hasMode() ? "&s=" + cookie.getMode() : ""));
			availability.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					RoomSharingHint.showHint(availability.getElement(), room.getUniqueId(), false);
				}
			});
			availability.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					RoomSharingHint.hideHint();
				}
			});
			if (room.getRoomSharingNote() != null && !room.getRoomSharingNote().isEmpty()) {
				P p = new P();
				p.add(availability);
				P note = new P("note");
				note.setHTML(room.getRoomSharingNote());
				note.setTitle(room.getRoomSharingNote());
				p.add(note);
				widgets.add(p);
			} else {
				widgets.add(availability);
			}
		}
		
		widgets.add(new DepartmentCell(room.getDepartments(), room.getControlDepartment()));
		widgets.add(new DepartmentCell(room.getControlDepartment()));
		
		widgets.add(new ExamTypesCell(room.getUniqueId(), room.getExamTypes()));
		if (!room.getExamTypes().isEmpty()) show(RoomFlag.SHOW_EXAM_TYPES);
		
		Widget periodPrefs = null;
		if (room.getPeriodPreference() != null) {
			final HTML availability = new HTML(room.getPeriodPreference());
			for (final ExamTypeInterface t: room.getExamTypes()) {
				if (t.getReference().equals(iDepartment)) {
					availability.addMouseOverHandler(new MouseOverHandler() {
						@Override
						public void onMouseOver(MouseOverEvent event) {
							PeriodPreferencesHint.showHint(availability.getElement(), room.getUniqueId(), t.getId());
						}
					});
					availability.addMouseOutHandler(new MouseOutHandler() {
						@Override
						public void onMouseOut(MouseOutEvent event) {
							PeriodPreferencesHint.hideHint();
						}
					});
					break;
				}
			}
			periodPrefs = availability;
			show(RoomFlag.SHOW_PERIOD_PREFERENCES);
		} else {
			for (final ExamTypeInterface t: room.getExamTypes()) {
				if (t.getReference().equals(iDepartment)) {
					final Image pattern = new Image("pattern?loc=" + room.getUniqueId() + "&xt=" + t.getId() + "&v=" + (cookie.areRoomsHorizontal() ? "0" : "1") + (cookie.hasMode() ? "&s=" + cookie.getMode() : ""));
					show(RoomFlag.SHOW_PERIOD_PREFERENCES);
					pattern.addMouseOverHandler(new MouseOverHandler() {
						@Override
						public void onMouseOver(MouseOverEvent event) {
							PeriodPreferencesHint.showHint(pattern.getElement(), room.getUniqueId(), t.getId());
						}
					});
					pattern.addMouseOutHandler(new MouseOutHandler() {
						@Override
						public void onMouseOut(MouseOutEvent event) {
							PeriodPreferencesHint.hideHint();
						}
					});
					periodPrefs = pattern; break;
				}
			}
		}
		widgets.add(periodPrefs == null ? new Label("") : periodPrefs);
		
		final DepartmentCell edc = new DepartmentCell(room.getEventDepartment());
		if (room.getEventDepartment() != null && room.isCanSeeEventAvailability()) {
			edc.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					RoomSharingHint.showHint(edc.getElement(), room.getUniqueId(), true);
				}
			});
			edc.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					RoomSharingHint.hideHint();
				}
			});
			show(RoomFlag.SHOW_EVENT_DEPARTMENT);
		}
		widgets.add(edc);
		
		widgets.add(new StatusCell(room.getEventStatus(), room.getDefaultEventStatus()));
		if (room.getEventStatus() != null || room.getDefaultEventStatus() != null) show(RoomFlag.SHOW_EVENT_STATUS);
		
		Widget eventAvail = null;
		if (room.getEventAvailability() != null) {
			final HTML availability = new HTML(room.getEventAvailability());
			availability.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					RoomSharingHint.showHint(availability.getElement(), room.getUniqueId(), true);
				}
			});
			availability.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					RoomSharingHint.hideHint();
				}
			});
			eventAvail = availability;
			show(RoomFlag.SHOW_EVENT_AVAILABILITY);
		} else if (room.isCanSeeEventAvailability()) {
			final Image availability = new Image("pattern?loc=" + room.getUniqueId() + "&e=1&v=" + (cookie.areRoomsHorizontal() ? "0" : "1") + (cookie.hasMode() ? "&s=" + cookie.getMode() : ""));
			availability.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					RoomSharingHint.showHint(availability.getElement(), room.getUniqueId(), true);
				}
			});
			availability.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					RoomSharingHint.hideHint();
				}
			});
			eventAvail = availability;
			show(RoomFlag.SHOW_EVENT_AVAILABILITY);
		}
		widgets.add(eventAvail == null ? new Label("") : eventAvail);
		
		widgets.add(new NoteCell(room.getEventNote(), room.getDefaultEventNote()));
		if (room.hasEventNote() || room.hasDefaultEventNote()) show(RoomFlag.SHOW_EVENT_MESSAGE);
		
		widgets.add(new BreakTimeCell(room.getBreakTime(), room.getDefaultBreakTime()));
		if (room.getBreakTime() != null) show(RoomFlag.SHOW_BREAK_TIME);
		
		widgets.add(new GroupsCell(room.getGroups()));
		widgets.add(new FeaturesCell(room.getFeatures()));
		
		int row = addRow(room, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
		return row;
	}
	
	protected int getColumn(RoomFlag flag) {
		switch (flag) {
		case SHOW_AREA:
			return getHeader(MESSAGES.colArea()).getColumn();
		case SHOW_AVAILABILITY:
			return getHeader(MESSAGES.colAvailability()).getColumn();
		case SHOW_BREAK_TIME:
			return getHeader(MESSAGES.colBreakTime()).getColumn();
		case SHOW_CAPACITY:
			return getHeader(MESSAGES.colCapacity()).getColumn();
		case SHOW_CONTROLLING_DEPARTMENT:
			return getHeader(MESSAGES.colControl()).getColumn();
		case SHOW_COORDINATES:
			return getHeader(MESSAGES.colCoordinates()).getColumn();
		case SHOW_PREFERENCE:
			return getHeader(MESSAGES.colPreference()).getColumn();
		case SHOW_DEPARTMENTS:
			return getHeader(MESSAGES.colDepartments()).getColumn();
		case SHOW_EVENT_AVAILABILITY:
			return getHeader(MESSAGES.colEventAvailability()).getColumn();
		case SHOW_EVENT_DEPARTMENT:
			return getHeader(MESSAGES.colEventDepartment()).getColumn();
		case SHOW_EVENT_MESSAGE:
			return getHeader(MESSAGES.colEventMessage()).getColumn();
		case SHOW_EVENT_STATUS:
			return getHeader(MESSAGES.colEventStatus()).getColumn();
		case SHOW_EXAM_CAPACITY:
			return getHeader(MESSAGES.colExaminationCapacity()).getColumn();
		case SHOW_EXAM_TYPES:
			return getHeader(MESSAGES.colExamTypes()).getColumn();
		case SHOW_FEATURES:
			return getHeader(MESSAGES.colFeatures()).getColumn();
		case SHOW_GROUPS:
			return getHeader(MESSAGES.colGroups()).getColumn();
		case SHOW_IGNORE_DISTANCES:
			return getHeader(MESSAGES.colDistances()).getColumn();
		case SHOW_IGNORE_ROOM_CHECK:
			return getHeader(MESSAGES.colRoomCheck()).getColumn();
		case SHOW_PERIOD_PREFERENCES:
			return getHeader(MESSAGES.colPeriodPreferences()).getColumn();
		default:
			return 0;
		}
	}
	
	protected void resetVisibility() {
		RoomCookie cookie = RoomCookie.getInstance();
		for (RoomFlag flag: RoomFlag.values()) {
			setColumnVisible(getColumn(flag), cookie.get(iMode, flag) && flag.isShowWhenEmpty());
		}
	}
	
	protected void show(RoomFlag f) {
		int col = getColumn(f);
		if (RoomCookie.getInstance().get(iMode, f) && !isColumnVisible(col)) setColumnVisible(col, true);
		if (!f.isShowWhenEmpty())
			iFlags = f.set(iFlags);
	}
	
	@Override
	public void clearTable(int headerRows) {
		super.clearTable(headerRows);
		resetVisibility();
		iFlags = 0;
	}
	
	public int getFlags() { return iFlags; }
	
	public static class WaitingCell extends Image implements UniTimeTable.HasColSpan, UniTimeTable.HasCellAlignment {
		private int iColSpan;
		public WaitingCell(int colspan) {
			super(RESOURCES.loading_small());
			iColSpan = colspan;
		}

		@Override
		public int getColSpan() {
			return iColSpan;
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_CENTER;
		}
	}
	
	public static class IntegerCell extends Label implements UniTimeTable.HasCellAlignment, UniTimeTable.HasColSpan, TakesValue<Integer> {
		
		public IntegerCell(Integer value) {
			super();
			setValue(value);
		}
		
		@Override
		public void setValue(Integer value) {
			setText(value == null ? "" : value.toString());
		}
		
		@Override
		public Integer getValue() {
			return getText().isEmpty() ? null : new Integer(getText());
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}

		@Override
		public int getColSpan() {
			return 1;
		}
	}
	
	public static class RoomAreaCell extends HTML implements UniTimeTable.HasCellAlignment {
		public RoomAreaCell(Double value) {
			super(value == null ? "" : MESSAGES.roomArea(value));
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class FeaturesCell extends P implements HasRefresh {
		Map<FeatureInterface, P> iFeatures = new HashMap<FeatureInterface, P>();
		
		public FeaturesCell(List<? extends FeatureInterface> features) {
			super();
			setStyleName("features");
			for (FeatureInterface feature: features) {
				P p = new P("feature");
				p.setText(feature.getLabel());
				if (feature.getTitle() != null) p.setTitle(feature.getTitle());
				if (feature.getDepartment() != null) {
					p.setText(feature.getLabel() + " (" + RoomsTable.toString(feature.getDepartment()) + ")");
					if (feature.getDepartment().getColor() != null)
						p.getElement().getStyle().setColor(feature.getDepartment().getColor());
				}
				if (feature.getType() != null)
					p.setText(feature.getLabel() + " (" + feature.getType().getAbbreviation() + ")");
				iFeatures.put(feature, p);
				add(p);
			}
		}

		@Override
		public void refresh() {
			for (Map.Entry<FeatureInterface, P> e: iFeatures.entrySet()) {
				P p = e.getValue();
				FeatureInterface feature = e.getKey();
				if (feature.getType() == null && feature.getDepartment() != null)
					p.setText(feature.getLabel() + " (" + RoomsTable.toString(feature.getDepartment()) + ")");
			}
		}
	}
	
	public static class GroupsCell extends P implements HasRefresh {
		Map<GroupInterface, P> iGroups = new HashMap<GroupInterface, P>();
		
		public GroupsCell(List<? extends GroupInterface> groups) {
			super();
			setStyleName("groups");
			for (GroupInterface group: groups) {
				P p = new P("group");
				p.setText(group.getLabel());
				if (group.getTitle() != null) p.setTitle(group.getTitle());
				if (group.getDepartment() != null) {
					p.setText(group.getLabel() + " (" + RoomsTable.toString(group.getDepartment()) + ")");
					if (group.getDepartment().getColor() != null)
						p.getElement().getStyle().setColor(group.getDepartment().getColor());
				}
				iGroups.put(group, p);
				add(p);
			}
		}
		
		@Override
		public void refresh() {
			for (Map.Entry<GroupInterface, P> e: iGroups.entrySet()) {
				P p = e.getValue();
				GroupInterface group = e.getKey();
				if (group.getDepartment() != null)
					p.setText(group.getLabel() + " (" + RoomsTable.toString(group.getDepartment()) + ")");
			}
		}
	}
	
	public static class NoteCell extends P {
		public NoteCell(String note, String defaultNote) {
			super("note");
			if (note != null) {
				setHTML(note.replace("\\n", "<br>"));
				setTitle(note);
			} else if (defaultNote != null) {
				addStyleName("default");
				setHTML(defaultNote.replace("\\n", "<br>"));
				setTitle(defaultNote);
			}
		}
	}
	
	public static class StatusCell extends P {
		public StatusCell(Integer status, Integer defaultStatus) {
			super("status");
			if (status != null) {
				setHTML(CONSTANTS.eventStatusAbbv()[status]);
				setTitle(CONSTANTS.eventStatusName()[status]);
			} else if (defaultStatus != null) {
				addStyleName("default");
				setText(CONSTANTS.eventStatusAbbv()[defaultStatus]);
				setTitle(CONSTANTS.eventStatusName()[defaultStatus]);
			} else {
				setHTML(CONSTANTS.eventStatusAbbv()[0]);
				setTitle(CONSTANTS.eventStatusName()[0]);
			}
		}
	}
	
	public static class BreakTimeCell extends P {
		public BreakTimeCell(Integer breakTime, Integer defaultBreakTime) {
			super("break");
			if (breakTime != null) {
				if (breakTime != 0)
					setText(breakTime.toString());
			} else if (defaultBreakTime != null) {
				addStyleName("default");
				if (defaultBreakTime != 0)
					setText(defaultBreakTime.toString());
			}
		}
	}
	
	public static String toString(DepartmentInterface d) {
		RoomCookie cookie = RoomCookie.getInstance();
		switch (DeptMode.values()[cookie.getDeptMode()]) {
		case ABBV:
			return d.getExtAbbreviationWhenExist();
		case CODE:
			return d.getDeptCode();
		case ABBV_NAME:
			return d.getExtAbbreviationWhenExist() + " - " + d.getExtLabelWhenExist();
		case CODE_NAME:
			return d.getDeptCode() + " - " + d.getLabel();
		case NAME:
			return d.getExtLabelWhenExist();
		default:
			return d.getExtAbbreviationWhenExist();
		}
	}
	
	public static class DepartmentCell extends P implements HasRefresh {
		Map<DepartmentInterface, P> iP = new HashMap<DepartmentInterface, P>();
		
		public DepartmentCell(DepartmentInterface... departments) {
			super("departments");
			for (DepartmentInterface department: departments) {
				if (department == null) continue;
				P p = new P("department");
				p.setText(RoomsTable.toString(department));
				if (department.getTitle() != null) p.setTitle(department.getTitle());
				if (department.getColor() != null)
					p.getElement().getStyle().setColor(department.getColor());
				add(p);
				iP.put(department, p);
			}
		}
		
		public DepartmentCell(List<DepartmentInterface> departments, DepartmentInterface control) {
			super("departments");
			for (DepartmentInterface department: departments) {
				P p = new P("department");
				p.setText(RoomsTable.toString(department));
				if (department.getTitle() != null) p.setTitle(department.getTitle());
				if (department.getColor() != null)
					p.getElement().getStyle().setColor(department.getColor());
				if (department.equals(control))
					p.addStyleName("control");
				iP.put(department, p);
				add(p);
			}
		}
		
		@Override
		public void refresh() {
			for (Map.Entry<DepartmentInterface, P> e: iP.entrySet())
				e.getValue().setText(RoomsTable.toString(e.getKey()));
		}
	}
	
	public static class PreferenceCell extends DepartmentCell {
		public PreferenceCell(List<DepartmentInterface> departments) {
			addStyleName("departments");
			for (DepartmentInterface department: departments) {
				if (department.getPreference() == null) continue;
				P p = new P("department");
				p.setText(RoomsTable.toString(department));
				p.setTitle(department.getPreference().getName() + " " + department.getLabel());
				p.getElement().getStyle().setColor(department.getPreference().getColor());
				iP.put(department, p);
				add(p);
			}
		}
	}
	
	public static class ExamTypesCell extends P {
		public ExamTypesCell(final Long roomId, List<ExamTypeInterface> examTypes) {
			super("exam-types");
			for (final ExamTypeInterface examType: examTypes) {
				final P p = new P(examType.isFinal() ? "final" : "midterm");
				p.setText(examType.getReference());
				p.setTitle(examType.getLabel());
				p.addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						PeriodPreferencesHint.showHint(p.getElement(), roomId, examType.getId());
					}
				});
				p.addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						PeriodPreferencesHint.hideHint();
					}
				});
				add(p);
			}
		}
	}
	
	public static class RoomCell extends Label {
		public RoomCell(RoomPropertyInterface property) {
			super(property == null ? "" : property.getLabel());
			if (property != null) {
				if (property.getTitle() != null)
					setTitle(property.getTitle());
				if (property.getColor() != null)
					getElement().getStyle().setColor(property.getColor());
			}
		}
	}
	
	public interface Check {
		public boolean isChecked();
	}
	
	public static enum DeptMode {
		CODE(MESSAGES.fieldCode()),
		ABBV(MESSAGES.fieldAbbreviation()),
		NAME(MESSAGES.fieldName()),
		ABBV_NAME(MESSAGES.fieldAbbv() + " - " + MESSAGES.fieldName()),
		CODE_NAME(MESSAGES.fieldCode() + " - " + MESSAGES.fieldName());

		private String iName;
		
		DeptMode(String name) { iName = name; }
		
		public String getName() { return iName; }
	}
	
	public static interface SortOperation extends Operation, HasColumnName {}
	
	public static interface HasRefresh {
		public void refresh();
	}
}