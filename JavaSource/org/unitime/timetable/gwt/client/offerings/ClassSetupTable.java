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
package org.unitime.timetable.gwt.client.offerings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.ClassSetupInterface;
import org.unitime.timetable.gwt.shared.ClassSetupInterface.ClassLine;
import org.unitime.timetable.gwt.shared.ClassSetupInterface.ClassSetupColumn;
import org.unitime.timetable.gwt.shared.ClassSetupInterface.Reference;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class ClassSetupTable extends UniTimeTable<ClassLine> {
	protected static final CourseMessages MESSAGES = GWT.create(CourseMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private ClassSetupInterface iData;
	private NumberFormat sRoomRatioFormat = NumberFormat.getFormat("##0.0##");
	
	public ClassSetupTable(ClassSetupInterface data) {
		iData = data;
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (final ClassSetupColumn col: ClassSetupColumn.values()) {
			if (isColumnVisible(col)) {
				final UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(col));
				header.add(h);
				h.setHorizontalAlignment(getColumnAlighment(col));
			}
		}
		addRow(null, header);
		setData(data.getClassLines());
		setUnlimited(data.isUnlimited());
	}
	
	protected List<Widget> generateRow(ClassLine clazz) {
		List<Widget> line = new ArrayList<>();
		for (ClassSetupColumn col: ClassSetupColumn.values())
			if (isColumnVisible(col)) {
				if (clazz.getCancelled() && !isCellVisibleWhenCancelled(col)) continue;
				Widget w = getColumnWidget(col, clazz);
				line.add(w);
			}
		return line;
	}
	
	protected void addRow(ClassLine clazz) {
		addRow(clazz, generateRow(clazz));
	}
	
	public void setData(List<ClassLine> classes) {
		clearTable(1);
		if (classes != null)
			for (ClassLine clazz: classes)
				addRow(clazz);
	}
	
	protected void onLimitChange() {}
	
	public void setUnlimited(Boolean unlimited) {
		if (Boolean.TRUE.equals(unlimited)) {
			for (final ClassSetupColumn col: ClassSetupColumn.values()) {
				int idx = getIndex(col);
				if (idx >= 0)
					setColumnVisible(idx, isColumnVisibleWhenUnlimited(col));
			}
		} else {
			for (final ClassSetupColumn col: ClassSetupColumn.values()) {
				int idx = getIndex(col);
				if (idx >= 0)
					setColumnVisible(idx, true);
			}
		}
	}
	
	public boolean isColumnVisibleWhenUnlimited(ClassSetupColumn column) {
		switch (column) {
		case SNAPSHOT:
		case LIMIT:
		case ROOM:
		case NBR_ROOMS:
		case ROOM_RATIO:
		case SPLIT_ATTENDANCE:
			return false;
		default:
			return true;
		}
	}
	
	public boolean isCellVisibleWhenCancelled(ClassSetupColumn column) {
		switch (column) {
		case SNAPSHOT:
		// case LIMIT:
		case NBR_ROOMS:
		case ROOM_RATIO:
		case SPLIT_ATTENDANCE:
		case DEPARTMENT:
		case DATE_PATTERN:
		case LMS:
		case DISPLAY_INSTRUCTOR:
		case STUDENT_SCHEDULING:
			return false;
		default:
			return true;
		}
	}
	
	public boolean isColumnVisible(ClassSetupColumn column) {
		switch (column) {
		case SNAPSHOT: return iData.isDisplaySnapshotLimit();
		case LMS: return iData.isDisplayLms();
		case DISPLAY_INSTRUCTOR: return iData.isDisplayInstructors();
		case STUDENT_SCHEDULING: return iData.isDisplayEnabledForStudentScheduling();
		case EXTERNAL_ID: return iData.isDisplayExternalId() || iData.isEditExternalId();
		case ENROLLMENT: return iData.isDisplayEnrollments();
		case TIME:
		case ROOM:
			for (ClassLine line: iData.getClassLines()) {
				if (!" ".equals(line.getTime())) return true;
				if (!" ".equals(line.getRoom())) return true;
			}
			return false;
		case INSTRUCTOR:
			for (ClassLine line: iData.getClassLines())
				if (!" ".equals(line.getInstructor())) return true;
			return false;
		default: return true;
		}
	}
	
	public String getColumnName(ClassSetupColumn column) {
		switch (column) {
		case ERROR: return "";
		case CLASS_NAME: return "";
		case EXTERNAL_ID: return MESSAGES.columnExternalId();
		case BUTTONS: return "";
		case ENROLLMENT: return MESSAGES.columnEnroll();
		case CANCELLED: return "";
		case SNAPSHOT: return MESSAGES.columnSnapshotLimitBr();
		case LIMIT:
			if (iData.isDisplayMaxLimit())
				return MESSAGES.columnLimit() + "<br>" + MESSAGES.columnMin() + " - " + MESSAGES.columnMax();
			else
				return MESSAGES.columnLimit();
		case ROOM_RATIO: return MESSAGES.columnRoomRatioBr();
		case NBR_ROOMS: return MESSAGES.columnNbrRms();
		case SPLIT_ATTENDANCE: return MESSAGES.columnSplitAttnd();
		case DEPARTMENT: return MESSAGES.columnManagingDepartment();
		case DATE_PATTERN: return MESSAGES.columnDatePattern();
		case LMS: return MESSAGES.columnLms();
		case DISPLAY_INSTRUCTOR: return MESSAGES.columnDisplayInstr();
		case STUDENT_SCHEDULING: return MESSAGES.columnStudentScheduling();
		case TIME: return MESSAGES.columnAssignedTime();
		case ROOM: return MESSAGES.columnAssignedRoom();
		case INSTRUCTOR: return MESSAGES.columnInstructors();
		default: return column.name();
		}
	}
	
	public HorizontalAlignmentConstant getColumnAlighment(ClassSetupColumn column) {
		switch (column) {
		case LIMIT:
			return HasHorizontalAlignment.ALIGN_CENTER;
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	public int getIndex(ClassSetupColumn column) {
		if (!isColumnVisible(column)) return -1;
		int index = 0;
		for (ClassSetupColumn c: ClassSetupColumn.values())
			if (c.ordinal() < column.ordinal() && isColumnVisible(c)) index ++;
		return index;
	}
	
	protected void updateLine(ClassLine line) {
		for (int row = 1; row < getRowCount(); row++) {
			if (line.getClassId().equals(getData(row).getClassId())) {
				setRow(row, line, generateRow(line));
				break;
			}
		}
	}
	
	protected int getRow(ClassLine line) {
		if (line == null) return -1;
		for (int row = 1; row < getRowCount(); row++)
			if (line.equals(getData(row))) return row;
		return -1;
	}
	
	protected boolean focus(Widget w) {
		if (w instanceof Focusable) {
			((Focusable)w).setFocus(true);
			return true;
		}
		return false;
	}
	
	protected void cancelClass(ClassLine line, boolean cancel) {
		if (cancel == line.getCancelled() || !line.isCanCancel()) return;
		for (ClassLine l: iData.getLines(line))
			if (l.isCanCancel())
				l.setCancelled(cancel);
		setData(iData.getClassLines());
		onLimitChange();
		P buttons = (P)getWidget(getRow(line), getIndex(ClassSetupColumn.BUTTONS));
		focus(buttons.getWidget(4));
	}
	
	protected void deleteClass(ClassLine line) {
		int row = getRow(line);
		iData.getClassLines().removeAll(iData.getLines(line));
		setData(iData.getClassLines());
		onLimitChange();
		if (row >= getRowCount()) row = getRowCount() - 1;
		P buttons = (P)getWidget(row, getIndex(ClassSetupColumn.BUTTONS));
		focus(buttons.getWidget(2));
	}
	
	protected void duplicateClass(ClassLine line) {
		Map<Long, Long> parents = new HashMap<>();
		Set<Long> ids = new HashSet<>(); 
		List<ClassLine> copies = new ArrayList<>();
		int row = -1;
		for (int i = 0; i < iData.getClassLines().size(); i++) {
			ClassLine l = iData.getClassLines().get(i);
			if (l.equals(line)) {
				ClassLine copy = iData.copy(l);
				copies.add(copy);
				parents.put(l.getClassId(), copy.getClassId());
				ids.add(l.getClassId());
				row = i + 1;
			} else if (l.getParentId() != null && ids.contains(l.getParentId())) {
				ids.add(l.getClassId());
				row = i + 1;
				if (Boolean.FALSE.equals(l.getCancelled()) && parents.containsKey(l.getParentId())) {
					ClassLine copy = iData.copy(l);
					copy.setParentId(parents.get(l.getParentId()));
					parents.put(l.getClassId(), copy.getClassId());
					copies.add(copy);
				}
			}
		}
		iData.getClassLines().addAll(row, copies);
		setData(iData.getClassLines());
		onLimitChange();
		P buttons = (P)getWidget(row + 1, getIndex(ClassSetupColumn.BUTTONS));
		focus(buttons.getWidget(3));
	}
	
	protected void moveClassUp(ClassLine line) {
		int subIdx = iData.getSubpartIndex(line.getSubpartId());
		ClassLine newParent = iData.getClassLine(iData.getPreviousParentId(line));
		if (newParent == null) return;
		List<ClassLine> data = iData.getLines(line);
		List<ClassLine> parents = iData.getLines(newParent);
		ClassLine prev = null;
		for (ClassLine l: parents) {
			if (prev == null) prev = l;
			else if (newParent.getClassId().equals(l.getParentId()) && iData.getSubpartIndex(l.getSubpartId()) <= subIdx)
				prev = iData.getLastLine(l);
		}
		int row = iData.getClassLines().indexOf(prev) + 1;
		iData.getClassLines().removeAll(data);
		iData.getClassLines().addAll(row, data);
		line.setParentId(newParent.getClassId());
		setData(iData.getClassLines());
		onLimitChange();
		P buttons = (P)getWidget(row + 1, getIndex(ClassSetupColumn.BUTTONS));
		if (!focus(buttons.getWidget(0))) focus(buttons.getWidget(1));
	}
	
	protected void moveClassDown(ClassLine line) {
		int subIdx = iData.getSubpartIndex(line.getSubpartId());
		ClassLine newParent = iData.getClassLine(iData.getNextParentId(line));
		if (newParent == null) return;
		List<ClassLine> parents = iData.getLines(newParent);
		List<ClassLine> data = iData.getLines(line);
		ClassLine prev = null;
		for (ClassLine l: parents) {
			if (prev == null) prev = l;
			else if (newParent.getClassId().equals(l.getParentId()) && iData.getSubpartIndex(l.getSubpartId()) < subIdx)
				prev = iData.getLastLine(l);
		}
		iData.getClassLines().removeAll(data);
		int row = iData.getClassLines().indexOf(prev) + 1;
		iData.getClassLines().addAll(row, data);
		line.setParentId(newParent.getClassId());
		setData(iData.getClassLines());
		onLimitChange();
		P buttons = (P)getWidget(row + 1, getIndex(ClassSetupColumn.BUTTONS));
		if (!focus(buttons.getWidget(1))) focus(buttons.getWidget(0));
	}

	public Widget getColumnWidget(ClassSetupColumn column, final ClassLine line) {
		switch (column) {
		case ERROR:
			P error = new P("class-error");
			if (line.hasError()) {
				Image err = new Image(RESOURCES.attention());
				err.setTitle(line.getError());
				err.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						UniTimeConfirmationDialog.alert(line.getError());
					}
				});
				error.add(err);
			}
			return error;
		case CLASS_NAME:
			Label className = new Label(line.getLabel());
			className.getElement().getStyle().setPaddingLeft(20 * line.getIndent(), Unit.PX);
			className.addStyleName("class-name");
			if (!line.isEditable() || line.getCancelled())
				className.addStyleName("not-editable");
			return className;
		case BUTTONS:
			final P buttons = new P("class-buttons");
			if (iData.getPreviousParentId(line) != null && iData.canMoveAway(line)) {
				ImageButton up = new ImageButton(RESOURCES.orderUp());
				up.setTitle(MESSAGES.titleMoveClassUp());
				up.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						moveClassUp(line);
					}
				});
				up.setTabIndex(2000 + 5 * getRowCount());
				buttons.add(up);
			} else {
				Image blank = new Image(RESOURCES.blank());
				buttons.add(blank);			
			}
			if (iData.getNextParentId(line) != null && iData.canMoveAway(line)) {
				ImageButton down = new ImageButton(RESOURCES.orderDown());
				down.setTitle(MESSAGES.titleMoveClassDown());
				down.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						moveClassDown(line);
					}
				});
				down.setTabIndex(2000 + 5 * getRowCount() + 1);
				buttons.add(down);
			} else {
				Image blank = new Image(RESOURCES.blank());
				buttons.add(blank);			
			}
			if (iData.canDelete(line)) {
				ImageButton delete = new ImageButton(RESOURCES.delete());
				delete.setTitle(MESSAGES.titleRemoveClassFromIO());
				delete.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						deleteClass(line);
					}
				});
				delete.setTabIndex(2000 + 5 * getRowCount() + 2);
				buttons.add(delete);
			} else {
				Image blank = new Image(RESOURCES.blank());
				buttons.add(blank);
			}
			if (Boolean.FALSE.equals(line.getCancelled())) {
				ImageButton add = new ImageButton(RESOURCES.add());
				add.setTitle(MESSAGES.titleAddClassToIO());
				add.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						duplicateClass(line);
					}
				});
				add.setTabIndex(2000 + 5 * getRowCount() + 3);
				buttons.add(add);
			} else {
				Image blank = new Image(RESOURCES.blank());
				buttons.add(blank);
			}
			if (iData.canCancel(line)) {
				if (line.getCancelled()) {
					ImageButton reopen = new ImageButton(RESOURCES.reopenClass());
					reopen.setTitle(MESSAGES.titleReopenClass());
					reopen.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							cancelClass(line, false);
						}
					});
					reopen.setTabIndex(2000 + 5 * getRowCount() + 4);
					buttons.add(reopen);
				} else {
					ImageButton cancel = new ImageButton(RESOURCES.cancelClass());
					cancel.setTitle(MESSAGES.titleCancelClass());
					cancel.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							cancelClass(line, true);
						}
					});
					cancel.setTabIndex(2000 + 5 * getRowCount() + 4);
					buttons.add(cancel);
				}
			} else {
				Image blank = new Image(RESOURCES.blank());
				buttons.add(blank);
			}
			return buttons;
		case ENROLLMENT:
			Label enrollment = new Label(line.getEnrollment() == null ? "" : line.getEnrollment().toString());
			enrollment.addStyleName("class-enrollment");
			if (!line.isEditable() || line.getCancelled())
				enrollment.addStyleName("not-editable");
			return enrollment;
		case CANCELLED:
			if (line.getCancelled())
				return new Cancelled(line);
			else
				return new Label();
		case LIMIT:
			return new ClassLimit(line);
		case SNAPSHOT:
			if (iData.isEditSnapshotLimits()) {
				final NumberBox snapshot = new NumberBox();
				snapshot.setMaxLength(5);
				snapshot.setWidth("40px");
				snapshot.setValue(line.getSnapshotLimit());
				snapshot.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						try {
							line.setSnapshotLimit(Integer.parseInt(event.getValue()));
						} catch (NumberFormatException e) {
							line.setSnapshotLimit(null);
							snapshot.setText("");
						}
						onLimitChange();
					}
				});
				snapshot.addStyleName("class-snapshot");
				snapshot.setReadOnly(!line.isEditable() || line.getCancelled());
				snapshot.setTabIndex(9000 + getRowCount());
				return snapshot;
			} else {
				Label snapshot = new Label(line.getSnapshotLimit() == null ? "" : line.getSnapshotLimit().toString());
				snapshot.addStyleName("class-snapshot");
				return snapshot;
			}
		case ROOM_RATIO:
			final NumberBox roomRatio = new NumberBox();
			roomRatio.setDecimal(true);
			roomRatio.setMaxLength(6);
			roomRatio.setWidth("40px");
			roomRatio.addStyleName("class-room-ratio");
			roomRatio.setValue(sRoomRatioFormat.format(line.getRoomRatio() == null ? 1.0f : line.getRoomRatio()));
			roomRatio.setReadOnly(!line.isEditable());
			roomRatio.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					try {
						line.setRoomRatio(Float.parseFloat(event.getValue()));
					} catch (NumberFormatException e) {
						line.setRoomRatio(1.0f);
					}
					roomRatio.setValue(sRoomRatioFormat.format(line.getRoomRatio()));
				}
			});
			roomRatio.setTabIndex(10000 + getRowCount());
			return roomRatio;
		case NBR_ROOMS:
			final NumberBox nbrRooms = new NumberBox();
			nbrRooms.setMaxLength(5);
			nbrRooms.setDecimal(false);
			nbrRooms.setWidth("40px");
			nbrRooms.addStyleName("class-nbr-rooms");
			nbrRooms.setValue(line.getNumberOfRooms());
			nbrRooms.setReadOnly(!line.isEditable());
			nbrRooms.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					try {
						line.setNumberOfRooms(Integer.parseInt(event.getValue()));
					} catch (NumberFormatException e) {
						line.setNumberOfRooms(1);
						nbrRooms.setText("1");
					}
					CheckBox splitAttendance = (CheckBox)getWidget(getRowForWidget(nbrRooms), getIndex(ClassSetupColumn.SPLIT_ATTENDANCE));
					splitAttendance.setEnabled(line.getNumberOfRooms() != null && line.getNumberOfRooms() > 1);
					if (!splitAttendance.isEnabled() && splitAttendance.getValue())
						splitAttendance.setValue(false, true);
				}
			});
			nbrRooms.setTabIndex(11000 + getRowCount());
			return nbrRooms;
		case SPLIT_ATTENDANCE:
			final CheckBox splitAttendance = new CheckBox();
			splitAttendance.setValue(line.getSplitAttendance());
			splitAttendance.addStyleName("class-split-attendace");
			splitAttendance.setEnabled(line.isEditable() && (line.getNumberOfRooms() != null && line.getNumberOfRooms() > 1));
			splitAttendance.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					line.setSplitAttendance(event.getValue());
				}
			});
			splitAttendance.setTabIndex(12000 + getRowCount());
			return splitAttendance;
		case DEPARTMENT:
			final ListBox department = new ListBox();
			department.addStyleName("class-department");
			department.setWidth("150px");
			for (Reference d: iData.getDepartments()) {
				if (d.isSelectable() || d.getId().equals(line.getDepartmentId()))
					department.addItem(d.getLabel(), d.getId().toString());
				if (d.getId().equals(line.getDepartmentId()))
					department.setSelectedIndex(department.getItemCount() - 1);
			}
			department.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					line.setDepartmentId(Long.valueOf(department.getSelectedValue()));
				}
			});
			department.setEnabled(line.isEditable());
			department.setTabIndex(13000 + getRowCount());
			return department;
		case DATE_PATTERN:
			final ListBox datePattern = new ListBox();
			datePattern.setWidth("150px");
			datePattern.addStyleName("class-date-pattern");
			for (Reference d: iData.getDatePatterns()) {
				if (d.isSelectable() || d.getId().equals(line.getDatePatternId()))
					datePattern.addItem(d.getLabel(), d.getId().toString());
				if (d.getId().equals(line.getDatePatternId()))
					datePattern.setSelectedIndex(datePattern.getItemCount() - 1);
			}
			datePattern.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					String dp = datePattern.getSelectedValue();
					if (dp == null || dp.isEmpty())
						line.setDatePatternId(null);
					else
						line.setDatePatternId(Long.valueOf(dp));
				}
			});
			datePattern.setEnabled(line.isEditable() && line.isEditableDatePattern());
			datePattern.setTabIndex(14000 + getRowCount());
			return datePattern;
		case DISPLAY_INSTRUCTOR:
			CheckBox displayInstructor = new CheckBox();
			displayInstructor.addStyleName("class-display-instructor");
			displayInstructor.setValue(line.getDisplayInstructors());
			displayInstructor.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					line.setDisplayInstructors(event.getValue());
					onLimitChange();
				}
			});
			displayInstructor.setEnabled(line.isEditable());
			displayInstructor.setTabIndex(16000 + getRowCount());
			return displayInstructor;
		case STUDENT_SCHEDULING:
			CheckBox studentScheduling = new CheckBox();
			studentScheduling.addStyleName("class-display-instructor");
			studentScheduling.setValue(line.getEnabledForStudentScheduling());
			studentScheduling.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					line.setEnabledForStudentScheduling(event.getValue());
					onLimitChange();
				}
			});
			studentScheduling.setEnabled(line.isEditable());
			studentScheduling.setTabIndex(17000 + getRowCount());
			return studentScheduling;
		case EXTERNAL_ID:
			UniTimeTextBox extId = new UniTimeTextBox();
			extId.addStyleName("class-external-id");
			extId.setMaxLength(40);
			extId.setWidth("135px");
			extId.setValue(line.getExternalId() == null ? "" : line.getExternalId());
			extId.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					if (event.getValue() == null || event.getValue().isEmpty()) {
						line.setExternalId(null);
					} else {
						line.setExternalId(event.getValue());
					}
				}
			});
			if (line.getCancelled())
				extId.addStyleName("not-editable");
			extId.setReadOnly(!line.isEditable() || !iData.isEditExternalId());
			extId.setTabIndex(1000 + getRowCount());
			return extId;
		case LMS:
			final ListBox lms = new ListBox();
			lms.addStyleName("class-lms");
			lms.setWidth("150px");
			for (Reference d: iData.getLMSs()) {
				if (d.isSelectable() || d.getId().equals(line.getLMS()))
					lms.addItem(d.getLabel(), d.getId().toString());
				if (d.getId().equals(line.getLMS()))
					lms.setSelectedIndex(lms.getItemCount() - 1);
			}
			lms.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					String id = lms.getSelectedValue();
					if (id == null || id.isEmpty())
						line.setLMS(null);
					else
						line.setLMS(Long.valueOf(id));
				}
			});
			lms.setEnabled(line.isEditable());
			lms.setTabIndex(15000 + getRowCount());
			return lms;
		case TIME:
			HTML time = new HTML(line.getTime() == null ? "" : line.getTime());
			time.addStyleName("class-time");
			if (line.getCancelled()) time.addStyleName("class-cancelled");
			else if (!line.isEditable()) time.addStyleName("not-editable");
			return time;
		case ROOM:
			HTML room = new HTML(line.getRoom() == null ? "" : line.getRoom());
			room.addStyleName("class-room");
			if (line.getCancelled()) room.addStyleName("class-cancelled");
			else if (!line.isEditable()) room.addStyleName("not-editable");
			return room;
		case INSTRUCTOR:
			HTML instructor = new HTML(line.getInstructor() == null ? "" : line.getInstructor());
			instructor.addStyleName("class-instructor");
			if (line.getCancelled()) instructor.addStyleName("class-cancelled");
			else if (!line.isEditable()) instructor.addStyleName("not-editable");
			return instructor;
		default: return null;
		}
	}
	
	class ClassLimit extends P {
		private NumberBox iMin, iMax;
		ClassLimit(ClassLine line) {
			super("class-limit");
			iMin = new NumberBox(); iMin.setMaxLength(5);
			iMin.setValue(line.getMinClassLimit());
			iMin.setReadOnly(!line.isEditable() || line.getCancelled());
			iMin.setWidth("40px");
			iMin.setTabIndex(7000 + 2 * getRowCount());
			if (line.getCancelled()) iMin.addStyleName("class-cancelled");
			add(iMin);
			iMax = new NumberBox(); iMax.setMaxLength(5);
			iMax.setValue(line.getMaxClassLimit());
			iMax.setReadOnly(!line.isEditable() || line.getCancelled());
			iMax.setWidth("40px");
			iMax.getElement().getStyle().setMarginLeft(2, Unit.PX);
			iMax.setVisible(iData.isDisplayMaxLimit());
			iMax.setTabIndex(7000 + 2 * getRowCount() + 1);
			if (line.getCancelled()) iMax.addStyleName("class-cancelled");
			add(iMax);
			iMin.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					try {
						line.setMinClassLimit(Integer.parseInt(event.getValue()));
					} catch (NumberFormatException e) {
						line.setMinClassLimit(null);
						iMin.setText("");
					}
					line.setMaxClassLimit(line.getMinClassLimit());
					iMax.setText(iMin.getText());
					onLimitChange();
				}
			});
			iMax.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					try {
						line.setMaxClassLimit(Integer.parseInt(event.getValue()));
					} catch (NumberFormatException e) {
						line.setMaxClassLimit(line.getMinClassLimit());
						iMax.setValue(line.getMaxClassLimit());
					}
					if (line.getMaxClassLimit() != null && line.getMinClassLimit() != null && line.getMaxClassLimit() < line.getMinClassLimit()) {
						line.setMaxClassLimit(line.getMinClassLimit());
						iMax.setValue(line.getMaxClassLimit());
					}
					onLimitChange();
				}
			});
		}
		public void setMaxVisible(boolean visible) {
			iMax.setVisible(visible);
		}
	}
	
	private class Cancelled extends Label implements HasColSpan {
		private Cancelled(ClassLine line) {
			addStyleName("class-cancelled");
			setText(MESSAGES.classNoteCancelled(line.getLabel()));
		}

		@Override
		public int getColSpan() {
			int colSpan = 1;
			for (ClassSetupColumn c: ClassSetupColumn.values())
				if (isColumnVisible(c) && !isCellVisibleWhenCancelled(c))
					colSpan ++;
			return colSpan;
		}
		
	}
}
