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
import com.google.gwt.resources.client.ImageResource;
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
	
	public void updateButtons() {
		for (int row = 1; row < getRowCount(); row++) {
			for (int col = 0; col < getCellCount(row); col++) {
				Widget w = getWidget(row, col);
				if (w != null && w instanceof HasUpdate)
					((HasUpdate)w).update(row);
			}
		}
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
		case DATE:
		case ROOM:
			return iData.isHasTimeRooms();
		case INSTRUCTOR:
			return iData.isHasInstructors();
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
		case DATE: return MESSAGES.columnAssignedDatePattern();
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
		for (ClassLine l: iData.getLines(line)) {
			if (l.isCanCancel())
				l.setCancelled(cancel);
			int row = getRow(l);
			setRow(insertRow(row), l, generateRow(l));
			removeRow(row+1);
		}
		updateButtons();
		onLimitChange();
		P buttons = (P)getWidget(getRow(line), getIndex(ClassSetupColumn.BUTTONS));
		focus(buttons.getWidget(4));
	}
	
	protected void deleteClass(ClassLine line) {
		int row = getRow(line);
		List<ClassLine> deletes = iData.getLines(line);
		iData.getClassLines().removeAll(deletes);
		for (int idx = 0; idx < deletes.size(); idx++)
			removeRow(row);
		updateButtons();
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
		int idx = 0;
		for (ClassLine l: copies)
			setRow(insertRow(row + 1 + (idx++)), l, generateRow(l));
		updateButtons();
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
		int oldRow = iData.getClassLines().indexOf(line) + 1;
		iData.getClassLines().removeAll(data);
		for (int idx = 0; idx < data.size(); idx++)
			removeRow(oldRow);
		line.setParentId(newParent.getClassId());
		int newRow = iData.getClassLines().indexOf(prev) + 1;
		iData.getClassLines().addAll(newRow, data);
		int idx = 0;
		for (ClassLine l: data)
			setRow(insertRow(newRow + 1 + (idx++)), l, generateRow(l));
		updateButtons();
		onLimitChange();
		P buttons = (P)getWidget(newRow + 1, getIndex(ClassSetupColumn.BUTTONS));
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
		int oldRow = iData.getClassLines().indexOf(line) + 1;
		iData.getClassLines().removeAll(data);
		for (int idx = 0; idx < data.size(); idx++)
			removeRow(oldRow);
		line.setParentId(newParent.getClassId());
		int newRow = iData.getClassLines().indexOf(prev) + 1;
		iData.getClassLines().addAll(newRow, data);
		int idx = 0;
		for (ClassLine l: data)
			setRow(insertRow(newRow + 1 + (idx++)), l, generateRow(l));
		updateButtons();
		onLimitChange();
		P buttons = (P)getWidget(newRow + 1, getIndex(ClassSetupColumn.BUTTONS));
		if (!focus(buttons.getWidget(1))) focus(buttons.getWidget(0));
	}

	public Widget getColumnWidget(ClassSetupColumn column, final ClassLine line) {
		switch (column) {
		case ERROR:
			Error error = new Error(line);
			error.update(getRowCount());
			return error;
		case CLASS_NAME:
			Label className = new Label(line.getLabel());
			className.getElement().getStyle().setPaddingLeft(20 * line.getIndent(), Unit.PX);
			className.addStyleName("class-name");
			if (!line.isEditable() || line.getCancelled())
				className.addStyleName("not-editable");
			return className;
		case BUTTONS:
			final Buttons buttons = new Buttons();
			int row = getRowCount();
			Operation up = new Operation(line, RESOURCES.orderUp(), MESSAGES.titleMoveClassUp(), 0,
					new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							moveClassUp(line);
						}
					},
					new Check() {
						@Override
						public boolean check(ClassLine line) {
							return iData.getPreviousParentId(line) != null && iData.canMoveAway(line);
						}
					});
			up.update(row);
			buttons.add(up);
			Operation down = new Operation(line, RESOURCES.orderDown(), MESSAGES.titleMoveClassDown(), 1,
					new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							moveClassDown(line);
						}
					},
					new Check() {
						@Override
						public boolean check(ClassLine line) {
							return iData.getNextParentId(line) != null && iData.canMoveAway(line);
						}
					});
			down.update(row);
			buttons.add(down);
			Operation delete = new Operation(line, RESOURCES.delete(), MESSAGES.titleRemoveClassFromIO(), 2,
					new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							deleteClass(line);
						}
					},
					new Check() {
						@Override
						public boolean check(ClassLine line) {
							return iData.canDelete(line);
						}
					});
			delete.update(row);
			buttons.add(delete);
			Operation add = new Operation(line, RESOURCES.add(), MESSAGES.titleAddClassToIO(), 3,
					new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							duplicateClass(line);
						}
					},
					new Check() {
						@Override
						public boolean check(ClassLine line) {
							return Boolean.FALSE.equals(line.getCancelled());
						}
					});
			add.update(row);
			buttons.add(add);
			MultiOperation cancel = new MultiOperation(line,
					new ImageResource[] { RESOURCES.reopenClass(), RESOURCES.cancelClass()},
					new String[] { MESSAGES.titleReopenClass(), MESSAGES.titleCancelClass()},
					4,
					new ClickHandler[] {
						new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								cancelClass(line, false);
							}
						},
						new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								cancelClass(line, true);
							}
						}
					},
					new MultiCheck() {
						@Override
						public int check(ClassLine line) {
							if (iData.canCancel(line)) {
								if (line.getCancelled()) {
									return 0;
								} else {
									return 1;
								}
							} else {
								return -1;
							}
						}
					});
			cancel.update(row);
			buttons.add(cancel);
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
			ClassLimit limit = new ClassLimit(line);
			limit.update(getRowCount());
			return limit;
		case SNAPSHOT:
			if (iData.isEditSnapshotLimits()) {
				final MyNumberBox snapshot = new MyNumberBox(9000);
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
				snapshot.update(getRowCount());
				return snapshot;
			} else {
				Label snapshot = new Label(line.getSnapshotLimit() == null ? "" : line.getSnapshotLimit().toString());
				snapshot.addStyleName("class-snapshot");
				return snapshot;
			}
		case ROOM_RATIO:
			final MyNumberBox roomRatio = new MyNumberBox(10000);
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
						line.setRoomRatio((float)NumberFormat.getDecimalFormat().parse(event.getValue()));
					} catch (NumberFormatException e) {
						try {
							line.setRoomRatio(Float.parseFloat(event.getValue()));
						} catch (NumberFormatException f) {
							line.setRoomRatio(1.0f);
						}
					}
					roomRatio.setValue(sRoomRatioFormat.format(line.getRoomRatio()));
				}
			});
			roomRatio.update(getRowCount());
			return roomRatio;
		case NBR_ROOMS:
			final MyNumberBox nbrRooms = new MyNumberBox(11000);
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
			nbrRooms.update(getRowCount());
			return nbrRooms;
		case SPLIT_ATTENDANCE:
			final MyCheckBox splitAttendance = new MyCheckBox(12000);
			splitAttendance.setValue(line.getSplitAttendance());
			splitAttendance.addStyleName("class-split-attendace");
			splitAttendance.setEnabled(line.isEditable() && (line.getNumberOfRooms() != null && line.getNumberOfRooms() > 1));
			splitAttendance.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					line.setSplitAttendance(event.getValue());
				}
			});
			splitAttendance.update(getRowCount());
			return splitAttendance;
		case DEPARTMENT:
			final MyListBox department = new MyListBox(13000);
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
			department.update(getRowCount());
			return department;
		case DATE_PATTERN:
			final MyListBox datePattern = new MyListBox(14000);
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
			datePattern.update(getRowCount());
			return datePattern;
		case DISPLAY_INSTRUCTOR:
			MyCheckBox displayInstructor = new MyCheckBox(16000);
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
			displayInstructor.update(getRowCount());
			return displayInstructor;
		case STUDENT_SCHEDULING:
			MyCheckBox studentScheduling = new MyCheckBox(17000);
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
			studentScheduling.update(getRowCount());
			return studentScheduling;
		case EXTERNAL_ID:
			MyTextBox extId = new MyTextBox(1000);
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
			extId.update(getRowCount());
			return extId;
		case LMS:
			final MyListBox lms = new MyListBox(15000);
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
			lms.update(15000);
			return lms;
		case TIME:
			HTML time = new HTML(line.getTime() == null ? "" : line.getTime());
			time.addStyleName("class-time");
			if (line.getCancelled()) time.addStyleName("class-cancelled");
			else if (!line.isEditable()) time.addStyleName("not-editable");
			return time;
		case DATE:
			HTML date = new HTML(line.getDate() == null ? "" : line.getDate());
			date.addStyleName("class-date");
			if (line.getCancelled()) date.addStyleName("class-cancelled");
			else if (!line.isEditable()) date.addStyleName("not-editable");
			return date;
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
	
	class ClassLimit extends P implements HasUpdate {
		private NumberBox iMin, iMax;
		ClassLimit(ClassLine line) {
			super("class-limit");
			iMin = new NumberBox(); iMin.setMaxLength(5);
			iMin.setValue(line.getMinClassLimit());
			iMin.setReadOnly(!line.isEditable() || line.getCancelled());
			iMin.setWidth("40px");
			if (line.getCancelled()) iMin.addStyleName("class-cancelled");
			add(iMin);
			iMax = new NumberBox(); iMax.setMaxLength(5);
			iMax.setValue(line.getMaxClassLimit());
			iMax.setReadOnly(!line.isEditable() || line.getCancelled());
			iMax.setWidth("40px");
			iMax.getElement().getStyle().setMarginLeft(2, Unit.PX);
			iMax.setVisible(iData.isDisplayMaxLimit());
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
		@Override
		public void update(int row) {
			iMin.setTabIndex(7000 + 2 * row);
			iMax.setTabIndex(7000 + 2 * row + 1);
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
	
	private interface Check {
		public boolean check(ClassLine line);
	}
	
	private interface HasUpdate {
		public void update(int row);
	}
	
	private class Buttons extends P implements HasUpdate {
		private Buttons() {
			super("class-buttons");
		}

		@Override
		public void update(int row) {
			for (int i = 0; i < getWidgetCount(); i++)
				((HasUpdate)getWidget(i)).update(row);
		}
	}
	
	private class Operation extends P implements HasUpdate, Focusable {
		private Image iBlank;
		private ImageButton iButton;
		private ClassLine iLine;
		private int iButtonIndex;
		private ClickHandler iClickHandler;
		private Check iCheck;
		private boolean iLastCheck = false;
		
		private Operation(ClassLine line, ImageResource image, String title, int buttonIndex, ClickHandler click, Check check) {
			iButton = new ImageButton(image);
			iButton.setTitle(title);
			iButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (isEnabled())
						iClickHandler.onClick(event);
				}
			});
			iBlank = new Image(RESOURCES.blank());
			iLine = line; iClickHandler = click; iCheck = check;
			iButtonIndex = buttonIndex;
			add(iBlank);
		}
		
		@Override
		public void update(int row) {
			boolean check = iCheck.check(iLine);
			if (check != iLastCheck) {
				iLastCheck = check;
				clear();
				if (iLastCheck) {
					add(iButton);
				} else {
					add(iBlank);
				}
			}
			if (check)
				iButton.setTabIndex(2000 + 5 * row + iButtonIndex);
		}
		
		@Override
		public void setFocus(boolean focus) {
			if (iLastCheck)
				iButton.setFocus(focus);
		}

		@Override
		public int getTabIndex() {
			if (iLastCheck)
				return iButton.getTabIndex();
			else
				return -1;
		}

		@Override
		public void setAccessKey(char key) {
			iButton.setAccessKey(key);
		}

		@Override
		public void setTabIndex(int index) {
			iButton.setTabIndex(index);
		}
	}
	
	private interface MultiCheck {
		public int check(ClassLine line);
	}
	
	private class MultiOperation extends P implements HasUpdate, Focusable {
		private ClassLine iLine;
		private Image iBlank;
		private ImageButton[] iButton;
		private int iButtonIndex;
		protected ClickHandler[] iClickHandler;
		protected MultiCheck iCheck;
		protected int iLastCheck = -1;
		
		private MultiOperation(ClassLine line, ImageResource[] image, String[] title, int buttonIndex, ClickHandler[] click, MultiCheck check) {
			iBlank = new Image(RESOURCES.blank());
			iButton = new ImageButton[image.length];
			for (int i = 0; i < image.length; i++) {
				iButton[i] = new ImageButton(image[i]);
				iButton[i].setTitle(title[i]);
				iButton[i].addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (iLastCheck >= 0)
							iClickHandler[iLastCheck].onClick(event);
					}
				});
			}
			iLine = line; iClickHandler = click; iCheck = check;
			iButtonIndex = buttonIndex;
			add(iBlank);
		}
		
		@Override
		public void update(int row) {
			int check = iCheck.check(iLine);
			if (check != iLastCheck) {
				iLastCheck = check;
				clear();
				if (iLastCheck >= 0) {
					add(iButton[iLastCheck]);
				} else {
					add(iBlank);
				}
			}
			if (check >= 0)
				iButton[iLastCheck].setTabIndex(2000 + 5 * row + iButtonIndex);
		}
		
		@Override
		public void setFocus(boolean focus) {
			if (iLastCheck >= 0)
				iButton[iLastCheck].setFocus(focus);
		}

		@Override
		public int getTabIndex() {
			if (iLastCheck >= 0)
				return iButton[iLastCheck].getTabIndex();
			else
				return -1;
		}

		@Override
		public void setAccessKey(char key) {
			for (int i = 0; i < iButton.length; i++)
				iButton[i].setAccessKey(key);
		}

		@Override
		public void setTabIndex(int index) {
			if (iLastCheck >= 0)
				iButton[iLastCheck].setTabIndex(index);
		}
	}
	
	private class MyNumberBox extends NumberBox implements HasUpdate {
		private int iBaseIndex;
		MyNumberBox(int baseIndex) {
			iBaseIndex = baseIndex;
		}
		@Override
		public void update(int row) {
			setTabIndex(iBaseIndex + row);
		}
	}
	
	private class MyCheckBox extends CheckBox implements HasUpdate {
		private int iBaseIndex;
		MyCheckBox(int baseIndex) {
			iBaseIndex = baseIndex;
		}
		@Override
		public void update(int row) {
			setTabIndex(iBaseIndex + row);
		}
	}
	
	private class MyListBox extends ListBox implements HasUpdate {
		private int iBaseIndex;
		MyListBox(int baseIndex) {
			iBaseIndex = baseIndex;
		}
		@Override
		public void update(int row) {
			setTabIndex(iBaseIndex + row);
		}
	}
	
	private class MyTextBox extends UniTimeTextBox implements HasUpdate {
		private int iBaseIndex;
		MyTextBox(int baseIndex) {
			iBaseIndex = baseIndex;
		}
		@Override
		public void update(int row) {
			setTabIndex(iBaseIndex + row);
		}
	}
	
	private class Error extends P implements HasUpdate {
		private ClassLine iLine;
		private Image iError;
		
		Error(ClassLine line) {
			super("class-error");
			iLine = line;
			iError = new Image(RESOURCES.attention());
			iError.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					UniTimeConfirmationDialog.alert(iLine.getError());
				}
			});
		}
		
		@Override
		public void update(int row) {
			clear();
			if (iLine.hasError()) {
				iError.setTitle(iLine.getError());
				add(iError);
			}
		}
	}
}
