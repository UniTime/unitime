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
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertyInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsColumn;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsPageMode;

import com.google.gwt.aria.client.Roles;
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
	private RoomsColumn iSortBy = null;
	private boolean iAsc = true;
	private RoomsPageMode iMode = null;
	private String iDepartment = null;
	private List<Operation> iSortOperations = new ArrayList<Operation>();
	private List<Operation> iShowHideOperations = new ArrayList<Operation>();
	private List<Operation> iDepartmentOperations = new ArrayList<Operation>();
	private List<Operation> iOtherOperations = new ArrayList<Operation>();
	private RoomPropertiesInterface iProperties = null;
	private int iFirstFeatureTypeColumn = -1;
	
	public RoomsTable(RoomsPageMode mode) {
		setStyleName("unitime-Rooms");
		iMode = mode;

		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (RoomsColumn column: RoomsColumn.values()) {
			UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column), getColumnAlignment(column));
			header.add(h);
		}
		
		addRow(null, header);
		
		for (final RoomsColumn column: RoomsColumn.values()) {
			UniTimeTableHeader h = header.get(column.ordinal());
			if (hasShowHideOperation(column)) {
				Operation op = new AriaOperation() {
					@Override
					public void execute() {
						boolean visible = isColumnVisible(column.ordinal());
						setColumnVisible(column.ordinal(), !visible);
						RoomCookie.getInstance().set(iMode, column, !visible);
					}
					@Override
					public boolean isApplicable() {
						return column.in(iFlags);
					}
					@Override
					public boolean hasSeparator() { 
						return false;
					}
					@Override
					public String getName() { return isColumnVisible(column.ordinal()) ? MESSAGES.opHide(getColumnName(column).replace("<br>", " ")) : MESSAGES.opShow(getColumnName(column).replace("<br>", " ")); }
					@Override
					public String getAriaLabel() { return isColumnVisible(column.ordinal()) ? ARIA.opHide(getColumnName(column).replace("<br>", " ")) : ARIA.opShow(getColumnName(column).replace("<br>", " ")); }
				};
				iShowHideOperations.add(op);
				if (column.ordinal() > 0) header.get(0).addOperation(op);
				RoomsColumn g = getShowHideGroup(column);
				if (g == null) {
					h.addOperation(op);
				} else {
					for (RoomsColumn c: RoomsColumn.values()) {
						if (g.equals(getShowHideGroup(c)))
							header.get(c.ordinal()).addOperation(op);
					}
				}
			}
		}
		
		for (final DeptMode d: DeptMode.values()) {
			Operation op = new Operation() {
				@Override
				public void execute() {
					RoomCookie.getInstance().setDeptMode(d.ordinal());
					refreshTable();
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
			for (RoomsColumn c: RoomsColumn.values()) {
				if (hasDepartmentOperation(c))
					header.get(c.ordinal()).addOperation(op);
			}
		}
		
		for (final RoomsColumn column: RoomsColumn.values()) {
			if (RoomsComparator.isApplicable(column)) {
				final UniTimeTableHeader h = header.get(column.ordinal());
				Operation op = new SortOperation() {
					@Override
					public void execute() {
						if (column == iSortBy) {
							iAsc = !iAsc;
						} else {
							iSortBy = column;
							iAsc = true;
						}
						RoomCookie.getInstance().setSortRoomsBy(getSortBy());
						sort();
					}
					@Override
					public boolean isApplicable() { return getRowCount() > 1 && h.isVisible(); }
					@Override
					public boolean hasSeparator() { return true; }
					@Override
					public String getName() { return MESSAGES.opSortBy(getColumnName()); }
					@Override
					public String getColumnName() { return h.getHTML().replace("<br>", " "); }
				};
				iSortOperations.add(op);
				h.addOperation(op);
			}
		}
		
		addOperation(new Operation() {

			@Override
			public void execute() {
				final RoomsTable table = new RoomsTable(iMode);
				table.setProperties(iProperties);
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
		
		setVisible(false);
	}
	
	public String getColumnName(RoomsColumn column) {
		switch (column) {
		case NAME: return MESSAGES.colName();
		case TYPE: return MESSAGES.colType();
		case EXTERNAL_ID: return MESSAGES.colExternalId();
		case CAPACITY: return MESSAGES.colCapacity();
		case EXAM_CAPACITY: return MESSAGES.colExaminationCapacity();
		case AREA: return MESSAGES.colArea(CONSTANTS.roomAreaUnitsShort());
		case COORDINATES: return MESSAGES.colCoordinates();
		case DISTANCE_CHECK: return MESSAGES.colDistances();
		case ROOM_CHECK: return MESSAGES.colRoomCheck();
		case MAP: return MESSAGES.colMap();
		case PICTURES: return MESSAGES.colPictures();
		case PREFERENCE: return MESSAGES.colPreference();
		case AVAILABILITY: return MESSAGES.colAvailability();
		case DEPARTMENTS: return MESSAGES.colDepartments();
		case CONTROL_DEPT: return MESSAGES.colControl();
		case EXAM_TYPES: return MESSAGES.colExamTypes();
		case PERIOD_PREF: return MESSAGES.colPeriodPreferences();
		case EVENT_DEPARTMENT: return MESSAGES.colEventDepartment();
		case EVENT_STATUS: return MESSAGES.colEventStatus();
		case EVENT_AVAILABILITY: return MESSAGES.colEventAvailability();
		case EVENT_MESSAGE: return MESSAGES.colEventMessage();
		case BREAK_TIME: return MESSAGES.colBreakTime();
		case FEATURES: return MESSAGES.colFeatures();
		case GROUPS: return MESSAGES.colGroups();
		default: return column.name();
		}
	}
	
	public void refreshTable() {
		for (int r = 1; r < getRowCount(); r++) {
			for (int c = 0; c < getCellCount(r); c++) {
				Widget w = getWidget(r, c);
				if (w instanceof HasRefresh)
					((HasRefresh)w).refresh();
			}
		}
	}
	
	protected HorizontalAlignmentConstant getColumnAlignment(RoomsColumn column) {
		switch (column) {
		case CAPACITY:
		case EXAM_CAPACITY:
		case AREA:
			return HasHorizontalAlignment.ALIGN_RIGHT;
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	protected boolean hasShowHideOperation(RoomsColumn column) {
		switch (column) {
		case NAME:
			return false;
		default:
			return true;
		}
	}
	
	protected boolean hasDepartmentOperation(RoomsColumn column) {
		switch (column) {
		case DEPARTMENTS:
		case CONTROL_DEPT:
		case EVENT_DEPARTMENT:
			return true;
		default:
			return false;
		}
	}
	
	protected RoomsColumn getShowHideGroup(RoomsColumn column) {
		switch (column) {
		case EXTERNAL_ID:
		case TYPE:
			return RoomsColumn.EXTERNAL_ID;
		case CAPACITY:
		case EXAM_CAPACITY:
		case AREA:
		case COORDINATES:
		case DISTANCE_CHECK:
		case ROOM_CHECK:
			return RoomsColumn.CAPACITY;
		case PREFERENCE:
		case AVAILABILITY:
		case DEPARTMENTS:
		case CONTROL_DEPT:
			return RoomsColumn.PREFERENCE;
		case EVENT_DEPARTMENT:
		case EVENT_AVAILABILITY:
		case EVENT_STATUS:
		case EVENT_MESSAGE:
		case BREAK_TIME:
			return RoomsColumn.EVENT_DEPARTMENT;
		case EXAM_TYPES:
		case PERIOD_PREF:
			return RoomsColumn.EXAM_TYPES;
		default:
			return null;
		}
	}
	
	protected boolean hasFeatureTypes() {
		return iProperties != null && iProperties.getFeatureTypes() != null && !iProperties.getFeatureTypes().isEmpty();
	}
	
	protected int countFeatureTypes() {
		return iProperties == null ? 0 : iProperties.getFeatureTypes().size();
	}
	
	protected FeatureTypeInterface getFeatureType(int index) {
		return iProperties.getFeatureTypes().get(index);
	}
	
	protected void setupFeatureTypes() {
		iFirstFeatureTypeColumn = getCellCount(0);
		SmartTableRow<RoomDetailInterface> smartRow = getSmartRow(0);
		for (int i = 0; i < countFeatureTypes(); i++) {
			FeatureTypeInterface type = getFeatureType(i);
			final UniTimeTableHeader header = new UniTimeTableHeader(type.getAbbreviation());
			final int column = i + iFirstFeatureTypeColumn;
			
			SmartTableCell cell = new SmartTableCell(smartRow, header);
			getFlexCellFormatter().setStyleName(0, column, "unitime-ClickableTableHeader");
			header.setColumn(column);
			setWidget(0, column, cell);
			Roles.getColumnheaderRole().set(getCellFormatter().getElement(0, column));
			
			final int flag = (1 << (RoomsColumn.values().length + i));
			final int ftIdx = i;
			Operation op = new AriaOperation() {
				@Override
				public void execute() {
					boolean visible = isColumnVisible(column);
					setColumnVisible(column, !visible);
					RoomCookie.getInstance().set(iMode, ftIdx, !visible);
				}
				@Override
				public boolean isApplicable() {
					return ((iFlags & flag) != 0);
				}
				@Override
				public boolean hasSeparator() { 
					return false;
				}
				@Override
				public String getName() { return isColumnVisible(header.getColumn()) ? MESSAGES.opHide(header.getHTML().replace("<br>", " ")) : MESSAGES.opShow(header.getHTML().replace("<br>", " ")); }
				@Override
				public String getAriaLabel() { return isColumnVisible(header.getColumn()) ? ARIA.opHide(header.getHTML().replace("<br>", " ")) : ARIA.opShow(header.getHTML().replace("<br>", " ")); }
			};
			getHeader(MESSAGES.colName()).getOperations().add(iShowHideOperations.size(), op);
			getHeader(MESSAGES.colFeatures()).addOperation(op);
			iShowHideOperations.add(op);
			header.addOperation(op);
			setColumnVisible(column, false);
		}
	}
	
	public void setProperties(RoomPropertiesInterface properties) {
		iProperties = properties;
		setupFeatureTypes();
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
			iSortBy = RoomsColumn.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = RoomsColumn.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	protected void addOperation(Operation op) {
		iOtherOperations.add(op);
		getHeader(MESSAGES.colName()).addOperation(op);
	}
	
	public void sort() {
		if (iSortBy == null) return;
		UniTimeTableHeader header = getHeader(iSortBy.ordinal());
		sort(header, new RoomsComparator(iSortBy, true), iAsc);
	}

	public void setDepartment(String department) { iDepartment = department; }
	
	protected Widget getCell(final RoomDetailInterface room, final RoomsColumn column) {
		if (iProperties == null || !iProperties.isCanSeeCourses()) {
			switch (column) {
			case DISTANCE_CHECK:
			case ROOM_CHECK:
			case PREFERENCE:
			case AVAILABILITY:
			case DEPARTMENTS:
			case CONTROL_DEPT:
				return null;
			}
		}
		if (iProperties == null || !iProperties.isCanSeeExams()) {
			switch (column) {
			case EXAM_CAPACITY:
			case EXAM_TYPES:
			case PERIOD_PREF:
				return null;
			}
		}
		if (iProperties == null || !iProperties.isCanSeeEvents()) {
			switch (column) {
			case EVENT_DEPARTMENT:
			case EVENT_MESSAGE:
			case EVENT_AVAILABILITY:
			case EVENT_STATUS:
			case BREAK_TIME:
				return null;
			}
		}
		switch (column) {
		case NAME:
			return new RoomNameCell(room);
			
		case EXTERNAL_ID:
			if (!room.hasExternalId()) return null;
			return new Label(room.getExternalId());
		
		case TYPE:
			return new Label(room.getRoomType().getLabel());
			
		case CAPACITY:
			if (room.getCapacity() == null) return null;
			return new IntegerCell(room.getCapacity());
		
		case EXAM_CAPACITY:
			if (room.getExamCapacity() == null) return null;
			return new IntegerCell(room.getExamCapacity());
		
		case AREA:
			if (room.getArea() == null) return null;
			return new RoomAreaCell(room.getArea());
		
		case COORDINATES:
			if (!room.hasCoordinates()) return null;
			return new Label(MESSAGES.coordinates(room.getX(), room.getY()));
			
		case DISTANCE_CHECK:
			return new Image(!room.isIgnoreTooFar() ? RESOURCES.on() : RESOURCES.off());
		
		case ROOM_CHECK:
			return new Image(!room.isIgnoreRoomCheck() ? RESOURCES.on() : RESOURCES.off());
		
		case PREFERENCE:
			boolean hasPreferences = false;
			for (DepartmentInterface department: room.getDepartments())
				if (department.getPreference() != null) {
					hasPreferences = true; break;
				}
			if (hasPreferences)
				return new PreferenceCell(room.getDepartments());
			else
				return null;
		
		case MAP:
			if (!room.hasMiniMapUrl()) return null;
			return new MapCell(room);
		
		case PICTURES:
			if (!room.hasPictures()) return null;
			return new PicturesCell(room);
		
		case AVAILABILITY:
			if (room.getAvailability() == null) return null;
			return new AvailabilityCell(room, false);
			
		case DEPARTMENTS:
			return new DepartmentCell(room.getDepartments(), room.getControlDepartment());
		
		case CONTROL_DEPT:
			return new DepartmentCell(room.getControlDepartment());
		
		case EXAM_TYPES:
			return new ExamTypesCell(room.getUniqueId(), room.getExamTypes());
		
		case PERIOD_PREF:
			if (room.getPeriodPreference() == null) return null;
			if (iDepartment != null && room.hasExamTypes())
				for (ExamTypeInterface type: room.getExamTypes())
					if (iDepartment.equals(type.getReference()))
						return new PeriodPreferenceCell(room, type);
			return null;
		
		case EVENT_DEPARTMENT:
			if (room.getEventDepartment() == null) return null;
			final DepartmentCell edc = new DepartmentCell(room.getEventDepartment());
			if (room.isCanSeeEventAvailability()) {
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
			}
			return edc;
		
		case EVENT_STATUS:
			if (room.getEventStatus() != null || room.getDefaultEventStatus() != null)
				return new StatusCell(room.getEventStatus(), room.getDefaultEventStatus());
			else
				return null;
		
		case EVENT_AVAILABILITY:
			if (room.getEventAvailability() == null) return null;
			return new AvailabilityCell(room, true);
		
		case EVENT_MESSAGE:
			if (room.hasEventNote() || room.hasDefaultEventNote())
				return new NoteCell(room.getEventNote(), room.getDefaultEventNote());
			else
				return null;
			
		case BREAK_TIME:
			if (room.getBreakTime() != null || room.getDefaultBreakTime() != null)
				return new BreakTimeCell(room.getBreakTime(), room.getDefaultBreakTime());
			else
				return null;
		
		case GROUPS:
			if (room.getGroups().isEmpty()) return null;
			return new GroupsCell(room.getGroups());

		case FEATURES:
			List<FeatureInterface> features = room.getFeatures((Long)null);
			if (features.isEmpty()) return null;
			return new FeaturesCell(features);
		}
		return null;
	}
	
	public int addRoom(final RoomDetailInterface room) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		for (RoomsColumn column: RoomsColumn.values()) {
			Widget cell = getCell(room, column);
			if (cell == null) {
				cell = new P();
			} else if (hasShowHideOperation(column)) {
				show(column);
			}
			widgets.add(cell);
		}

		if (hasFeatureTypes())
			for (int i = 0; i < countFeatureTypes(); i++) {
				FeatureTypeInterface type = getFeatureType(i);
				List<FeatureInterface> featuresOfType = room.getFeatures(type);
				widgets.add(new FeaturesCell(featuresOfType));
				if (!featuresOfType.isEmpty()) show(i);
			}
		
		int row = addRow(room, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
		
		if (!isVisible()) setVisible(true);
		
		return row;
	}
	
	protected void resetVisibility() {
		for (RoomsColumn column: RoomsColumn.values()) {
			setColumnVisible(column.ordinal(), !hasShowHideOperation(column));
		}
		if (hasFeatureTypes())
			for (int i = 0; i < countFeatureTypes(); i++)
				setColumnVisible(iFirstFeatureTypeColumn + i, false);
	}
	
	protected void show(RoomsColumn column) {
		if (RoomCookie.getInstance().get(iMode, column) && !isColumnVisible(column.ordinal())) setColumnVisible(column.ordinal(), true);
		iFlags = column.set(iFlags);
	}
	
	protected void show(int ftIdx) {
		int flag = (1 << (RoomsColumn.values().length + ftIdx));
		if (RoomCookie.getInstance().get(iMode, ftIdx) && !isColumnVisible(iFirstFeatureTypeColumn + ftIdx)) setColumnVisible(iFirstFeatureTypeColumn + ftIdx, true);
		if ((iFlags & flag) == 0)
			iFlags += flag;
	}
	
	@Override
	public void clearTable(int headerRows) {
		super.clearTable(headerRows);
		resetVisibility();
		iFlags = 0;
		setVisible(false);
	}
	
	public int getFlags() { return iFlags; }
	
	public boolean isVisible(RoomsColumn column) {
		return isColumnVisible(column.ordinal());
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
	
	class RoomNameCell extends Label {
		RoomNameCell(final RoomDetailInterface room) {
			super(room.hasDisplayName() ? MESSAGES.label(room.getLabel(), room.getDisplayName()) : room.getLabel());
			addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					RoomHint.showHint(RoomNameCell.this.getElement(), room.getUniqueId(), room.getPrefix(), room.getProperty("distance", null), true);
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					RoomHint.hideHint();
				}
			});
			if (iDepartment != null && iProperties != null && iProperties.isCanSeeCourses()) {
				for (DepartmentInterface d: room.getDepartments()) {
					if (iDepartment.equals(d.getDeptCode()) && d.getPreference() != null) {
						getElement().getStyle().setColor(d.getPreference().getColor());
						room.setPrefix(d.getPreference().getName());
					}
				}
			}
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
				iFeatures.put(feature, p);
				add(p);
			}
		}

		@Override
		public void refresh() {
			for (Map.Entry<FeatureInterface, P> e: iFeatures.entrySet()) {
				P p = e.getValue();
				FeatureInterface feature = e.getKey();
				if (feature.getDepartment() != null)
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
	
	public static class PictureCell extends Image {
		public PictureCell(RoomPictureInterface picture) {
			super();
			setStyleName("picture");
			setUrl(GWT.getHostPageBaseURL() + "picture?id=" + picture.getUniqueId());
			setAltText(picture.getName());
		}
	}
	
	public static class PicturesCell extends P {
		public PicturesCell(RoomDetailInterface room) {
			super("pictures");
			if (room.hasPictures()) {
				for (RoomPictureInterface picture: room.getPictures())
					add(new PictureCell(picture));
			}
		}
	}
	
	public static class MapCell extends Image {
		public MapCell(RoomDetailInterface room) {
			super();
			if (room.hasMiniMapUrl()) {
				setStyleName("map");
				setUrl(room.getMiniMapUrl());
				setTitle(MESSAGES.titleRoomMap(room.getLabel()));
			}
		}
	}
	
	public static class AvailabilityCell extends P implements HasRefresh {
		private boolean iEvents;
		private RoomDetailInterface iRoom;
		
		public AvailabilityCell(RoomDetailInterface room, boolean events) {
			super("availability");
			iRoom = room;
			iEvents = events;
			addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					RoomSharingHint.showHint(getElement(), iRoom.getUniqueId(), iEvents);
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					RoomSharingHint.hideHint();
				}
			});
			refresh();
		}
		
		@Override
		public void refresh() {
			clear(); setHTML("");
			RoomCookie cookie = RoomCookie.getInstance();
			if (iEvents) {
				if (cookie.isGridAsText()) {
					if (iRoom.getEventAvailability() != null) {
						P p = new P("text");
						p.setHTML(iRoom.getEventAvailability());
						add(p);
					}
				} else {
					Image availability = new Image(GWT.getHostPageBaseURL() + "pattern?loc=" + iRoom.getUniqueId() + "&e=1&v=" + (cookie.areRoomsHorizontal() ? "0" : "1") + (cookie.hasMode() ? "&s=" + cookie.getMode() : ""));
					availability.setStyleName("grid");
					add(availability);
				}
			} else {
				if (cookie.isGridAsText()) {
					if (iRoom.getAvailability() != null && !iRoom.getAvailability().isEmpty()) {
						P p = new P("text");
						p.setHTML(iRoom.getAvailability());
						add(p);
					}
				} else {
					Image availability = new Image(GWT.getHostPageBaseURL() + "pattern?loc=" + iRoom.getUniqueId() + "&v=" + (cookie.areRoomsHorizontal() ? "0" : "1") + (cookie.hasMode() ? "&s=" + cookie.getMode() : ""));
					availability.setStyleName("grid");
					add(availability);
				}
				if (iRoom.getRoomSharingNote() != null && !iRoom.getRoomSharingNote().isEmpty()) {
					P p = new P("note");
					p.setHTML(iRoom.getRoomSharingNote());
					p.setTitle(iRoom.getRoomSharingNote());
					add(p);
				}
			}
		}
	}
	
	public static class PeriodPreferenceCell extends P implements HasRefresh {
		private RoomDetailInterface iRoom;
		private ExamTypeInterface iType;
		
		public PeriodPreferenceCell(RoomDetailInterface room, ExamTypeInterface type) {
			super("periodpref");
			iRoom = room; iType = type;
			addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					PeriodPreferencesHint.showHint(getElement(), iRoom.getUniqueId(), iType.getId());
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					RoomSharingHint.hideHint();
				}
			});
			refresh();
		}
		
		@Override
		public void refresh() {
			clear(); setHTML("");
			RoomCookie cookie = RoomCookie.getInstance();
			if (cookie.isGridAsText() || !iType.isFinal()) {
				if (iRoom.getPeriodPreference() != null)
					setHTML(iRoom.getPeriodPreference());
			} else {
				add(new Image(GWT.getHostPageBaseURL() + "pattern?loc=" + iRoom.getUniqueId() + "&xt=" + iType.getId() + "&v=1" + (cookie.hasMode() ? "&s=" + cookie.getMode() : "")));
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
	
	public RoomDetailInterface getRoom(Long roomId) {
		if (roomId == null) return null;
		for (int i = 1; i < getRowCount(); i++) {
			if (roomId.equals(getData(i).getUniqueId())) return getData(i);
		}
		return null;
	}
	
	public RoomDetailInterface getPrevious(Long roomId) {
		if (roomId == null) return null;
		for (int i = 2; i < getRowCount(); i++) {
			if (roomId.equals(getData(i).getUniqueId()))
				return getData(i - 1);
		}
		return null;
	}
	
	public RoomDetailInterface getNext(Long roomId) {
		if (roomId == null) return null;
		for (int i = 1; i < getRowCount() - 1; i++) {
			if (roomId.equals(getData(i).getUniqueId()))
				return getData(i + 1);
		}
		return null;
	}
}