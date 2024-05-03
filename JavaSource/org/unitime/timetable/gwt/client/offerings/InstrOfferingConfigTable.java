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
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface;
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface.InstrOfferingConfigColumn;
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface.Reference;
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface.SubpartLine;

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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class InstrOfferingConfigTable extends UniTimeTable<SubpartLine> {
	protected static final CourseMessages MESSAGES = GWT.create(CourseMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private InstrOfferingConfigInterface iData;
	private NumberFormat sRoomRatioFormat = NumberFormat.getFormat("##0.0##");
	
	public InstrOfferingConfigTable(InstrOfferingConfigInterface data) {
		iData = data;
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (final InstrOfferingConfigColumn col: InstrOfferingConfigColumn.values()) {
			if (isColumnVisible(col)) {
				final UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(col));
				header.add(h);
				h.setHorizontalAlignment(getColumnAlighment(col));
				if (col == InstrOfferingConfigColumn.MINS_PER_WK) {
					h.getElement().getStyle().setWidth(65, Unit.PX);
					h.getElement().getStyle().clearWhiteSpace();
				}
			}
		}
		addRow(null, header);
		setData(data.getSubpartLines());
		setUnlimited(data.isUnlimited());
	}
	
	protected List<Widget> generateRow(SubpartLine subpart) {
		List<Widget> line = new ArrayList<>();
		for (InstrOfferingConfigColumn col: InstrOfferingConfigColumn.values())
			if (isColumnVisible(col)) {
				Widget w = getColumnWidget(col, subpart);
				line.add(w);
			}
		return line;
	}
	
	protected void addRow(SubpartLine subpart) {
		addRow(subpart, generateRow(subpart));
	}
	
	public void setData(List<SubpartLine> subparts) {
		clearTable(1);
		if (subparts != null)
			for (SubpartLine subpart: subparts)
				addRow(subpart);
	}
	
	public void updateButtons() {
		for (int row = 1; row < getRowCount(); row++) {
			for (int col = 0; col < getCellCount(row); col++) {
				Widget w = getWidget(row, col);
				if (w != null && w instanceof HasUpdate)
					((HasUpdate)w).update(row);
			}
		}
		onLimitChange();
	}
	
	protected void onLimitChange() {}
	
	public void setUnlimited(Boolean unlimited) {
		if (Boolean.TRUE.equals(unlimited)) {
			for (final InstrOfferingConfigColumn col: InstrOfferingConfigColumn.values()) {
				int idx = getIndex(col);
				if (idx >= 0)
					setColumnVisible(idx, isColumnVisibleWhenUnlimited(col));
			}
		} else {
			for (final InstrOfferingConfigColumn col: InstrOfferingConfigColumn.values()) {
				int idx = getIndex(col);
				if (idx >= 0)
					setColumnVisible(idx, true);
			}
		}
	}
	
	protected void addSubpartLine(Long itype) {
		SubpartLine subpart = iData.addSubpartLine(iData.getInstructionalType(itype));
		addRow(subpart, generateRow(subpart));
		updateButtons();
		focus(getWidget(getRowCount() - 1, getIndex(InstrOfferingConfigColumn.LIMIT)));
	}
	
	public boolean isColumnVisibleWhenUnlimited(InstrOfferingConfigColumn column) {
		switch (column) {
		case LIMIT:
		case NBR_ROOMS:
		case ROOM_RATIO:
		case SPLIT_ATTENDANCE:
			return false;
		default:
			return true;
		}
	}
	
	public boolean isColumnVisible(InstrOfferingConfigColumn column) {
		switch (column) {
		default: return true;
		}
	}
	
	public String getColumnName(InstrOfferingConfigColumn column) {
		switch (column) {
		case ERROR: return "";
		case LABEL: return "";
		case BUTTONS: return "";
		case LIMIT:
			if (iData.isDisplayMaxLimit())
				return MESSAGES.columnSubpartLimitPerClass() + "<br>" + MESSAGES.columnMin() + " - " + MESSAGES.columnMax();
			else
				return MESSAGES.columnSubpartLimitPerClass();
		case ROOM_RATIO: return MESSAGES.columnRoomRatioBr();
		case NBR_ROOMS: return MESSAGES.columnSubpartNumberOfRooms();
		case SPLIT_ATTENDANCE: return MESSAGES.columnRoomSplitAttendance();
		case DEPARTMENT: return MESSAGES.columnSubpartManagingDepartment();
		case MINS_PER_WK: return (iData.getDurationTypeId() == null ? MESSAGES.columnSubpartMinutesPerWeek() : iData.getDurationType(iData.getDurationTypeId()).getLabel());
		case NBR_CLASSES: return MESSAGES.columnSubpartNumberOfClasses();
		default: return column.name();
		}
	}
	
	public HorizontalAlignmentConstant getColumnAlighment(InstrOfferingConfigColumn column) {
		switch (column) {
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}
	
	public int getIndex(InstrOfferingConfigColumn column) {
		if (!isColumnVisible(column)) return -1;
		int index = 0;
		for (InstrOfferingConfigColumn c: InstrOfferingConfigColumn.values())
			if (c.ordinal() < column.ordinal() && isColumnVisible(c)) index ++;
		return index;
	}
	
	protected void updateLine(SubpartLine line) {
		for (int row = 1; row < getRowCount(); row++) {
			if (line.getSubpartId().equals(getData(row).getSubpartId())) {
				setRow(row, line, generateRow(line));
				break;
			}
		}
	}
	
	protected int getRow(SubpartLine line) {
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
	
	public Widget getColumnWidget(InstrOfferingConfigColumn column, final SubpartLine line) {
		switch (column) {
		case ERROR:
			Error error = new Error(line);
			error.update(getRowCount());
			return error;
		case LABEL:
			SubpartLabel subpartLabel = new SubpartLabel(line);
			subpartLabel.update(getRowCount());
			return subpartLabel;
		case BUTTONS:
			final Buttons buttons = new Buttons();
			Operation right = new Operation(line, RESOURCES.arrowRight(), MESSAGES.titleMoveToChildLevel(),
					new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							int idx = iData.getSubpartLines().indexOf(line);
							if (idx <= 0) return;
							SubpartLine before = iData.getSubpartLines().get(idx - 1);
							if (before == null) return;
							line.setParentId(iData.getParentsWithMe(before).get(line.getIndent()).getSubpartId());
							for (SubpartLine l: iData.getLines(line))
								l.setIndent(l.getIndent() + 1);
							updateButtons();
							
							P buttons = (P)getWidget(idx + 1, getIndex(InstrOfferingConfigColumn.BUTTONS));
							focus(buttons.getWidget(line.getIndent() <= before.getIndent() ? 0 : 1));
						}
					},
					new Check() {
						@Override
						public boolean check(SubpartLine line) {
							if (!line.isEditable()) return false;
							int idx = iData.getSubpartLines().indexOf(line);
							if (idx <= 0) return false;
							SubpartLine before = iData.getSubpartLines().get(idx - 1);
							return before != null && line.getIndent() <= before.getIndent();
						}
					});
			right.update(getRowCount());
			buttons.add(right);
			Operation left = new Operation(line, RESOURCES.arrowLeft(), MESSAGES.titleMoveToParentLevel(),
					new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							if (line.getParentId() == null) return;
							SubpartLine parent = iData.getSubpartLine(line.getParentId());
							if (parent == null) return;
							List<SubpartLine> same = iData.getSameParent(line);
							if (same.indexOf(line) + 1 < same.size()) {
								SubpartLine next = same.get(same.size() - 1);
								List<SubpartLine> data = iData.getLines(line);

								int oldRow = iData.getSubpartLines().indexOf(line);
								iData.getSubpartLines().removeAll(data);
								for (int idx = 0; idx < data.size(); idx++)
									removeRow(oldRow + 1);

								int newRow = iData.getSubpartLines().indexOf(next) + iData.getLines(next).size();
								iData.getSubpartLines().addAll(newRow, data);
								int idx = 0;
								for (SubpartLine l: data)
									setRow(insertRow(newRow + 1 + (idx++)), l, generateRow(l));
							}
							
							line.setParentId(parent.getParentId());
							for (SubpartLine l: iData.getLines(line))
								l.setIndent(l.getIndent() - 1);
							updateButtons();
							
							P buttons = (P)getWidget(iData.getSubpartLines().indexOf(line) + 1, getIndex(InstrOfferingConfigColumn.BUTTONS));
							focus(buttons.getWidget(line.getIndent() > 0 ? 1 : 0));
						}
					},
					new Check() {
						@Override
						public boolean check(SubpartLine line) {
							if (!line.isEditable()) return false;
							return line.getIndent() > 0;
						}
					});
			left.update(getRowCount());
			buttons.add(left);
			Operation up = new Operation(line, RESOURCES.orderUp(), MESSAGES.titleMoveUp(),
					new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							List<SubpartLine> same = iData.getSameParent(line);
							int row = same.indexOf(line);
							if (row <= 0) return;
							SubpartLine prev = same.get(row - 1);
							List<SubpartLine> data = iData.getLines(line);

							int oldRow = iData.getSubpartLines().indexOf(line);
							iData.getSubpartLines().removeAll(data);
							for (int idx = 0; idx < data.size(); idx++)
								removeRow(oldRow + 1);

							int newRow = iData.getSubpartLines().indexOf(prev);
							iData.getSubpartLines().addAll(newRow, data);
							int idx = 0;
							for (SubpartLine l: data)
								setRow(insertRow(newRow + 1 + (idx++)), l, generateRow(l));
							
							updateButtons();
							
							P buttons = (P)getWidget(newRow + 1, getIndex(InstrOfferingConfigColumn.BUTTONS));
							focus(buttons.getWidget(row >= 2 ? 2 : 3));
						}
					},
					new Check() {
						@Override
						public boolean check(SubpartLine line) {
							int idx = iData.getSubpartLines().indexOf(line);
							for (int i = idx - 1; i >= 0; i--) {
								SubpartLine l = iData.getSubpartLines().get(i);
								if (l.getParentId() == null && line.getParentId() == null) return true;
								if (l.getParentId() != null && l.getParentId().equals(line.getParentId())) return true;
							}
							return false;
						}
					});
			up.update(getRowCount());
			buttons.add(up);
			Operation down = new Operation(line, RESOURCES.orderDown(), MESSAGES.titleMoveDown(),
					new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							List<SubpartLine> same = iData.getSameParent(line);
							int row = same.indexOf(line);
							if (row + 1 >= same.size()) return;
							SubpartLine next = same.get(row + 1);
							List<SubpartLine> data = iData.getLines(line);

							int oldRow = iData.getSubpartLines().indexOf(line);
							iData.getSubpartLines().removeAll(data);
							for (int idx = 0; idx < data.size(); idx++)
								removeRow(oldRow + 1);

							int newRow = iData.getSubpartLines().indexOf(next) + iData.getLines(next).size();
							iData.getSubpartLines().addAll(newRow, data);
							int idx = 0;
							for (SubpartLine l: data)
								setRow(insertRow(newRow + 1 + (idx++)), l, generateRow(l));
							
							updateButtons();
							
							P buttons = (P)getWidget(newRow + 1, getIndex(InstrOfferingConfigColumn.BUTTONS));
							focus(buttons.getWidget(row + 2 < same.size() ? 3 : 2));
						}
					},
					new Check() {
						@Override
						public boolean check(SubpartLine line) {
							if (!line.isEditable()) return false;
							int idx = iData.getSubpartLines().indexOf(line);
							for (int i = idx + 1; i < iData.getSubpartLines().size(); i++) {
								SubpartLine l = iData.getSubpartLines().get(i);
								if (l.getParentId() == null && line.getParentId() == null) return true;
								if (l.getParentId() != null && l.getParentId().equals(line.getParentId())) return true;
							}
							return false;
						}
					});
			down.update(getRowCount());
			buttons.add(down);
			Operation delete = new Operation(line, RESOURCES.delete(), MESSAGES.titleDeleteInstructionalType(),
					new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							List<SubpartLine> data = iData.getLines(line);

							int oldRow = iData.getSubpartLines().indexOf(line);
							iData.getSubpartLines().removeAll(data);
							for (int idx = 0; idx < data.size(); idx++)
								removeRow(oldRow + 1);
							
							updateButtons();
						}
					},
					new Check() {
						@Override
						public boolean check(SubpartLine line) {
							if (!line.isEditable()) return false;
							for (SubpartLine l: iData.getLines(line))
								if (!l.isCanDelete()) return false;
							return true;
						}
					});
			delete.update(getRowCount());
			buttons.add(delete);
			return buttons;
		case LIMIT:
			ClassLimit limit = new ClassLimit(line);
			return limit;
		case NBR_CLASSES:
			NumberBox nbrClasses = new NumberBox();
			nbrClasses.setWidth("40px");
			nbrClasses.setMaxLength(4);
			nbrClasses.setValue(line.getNumberOfClasses());
			nbrClasses.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					try {
						line.setNumberOfClasses(Integer.parseInt(event.getValue()));
					} catch (NumberFormatException e) {
						line.setNumberOfClasses(null);
						nbrClasses.setText("");
					}
					if (line.getNumberOfClasses() != null && line.getNumberOfClasses() > 100) {
						if (!Window.confirm(MESSAGES.confirmCreateTooManyClasses(line.getNumberOfClasses()))) {
							line.setNumberOfClasses(null);
							nbrClasses.setText("");
							nbrClasses.setFocus(true);
						}
					}
					onLimitChange();
				}
			});
			nbrClasses.addStyleName("subpart-nbr-classes");
			nbrClasses.setReadOnly(!line.isEditable());
			return nbrClasses;
		case MINS_PER_WK:
			final NumberBox minsPerWeek = new NumberBox();
			minsPerWeek.setWidth("40px");
			minsPerWeek.setMaxLength(4);
			minsPerWeek.setValue(line.getMinutesPerWeek());
			minsPerWeek.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					try {
						line.setMinutesPerWeek(Integer.parseInt(event.getValue()));
					} catch (NumberFormatException e) {
						line.setMinutesPerWeek(null);
						minsPerWeek.setText("");
					}
				}
			});
			minsPerWeek.addStyleName("subpart-mins-per-wk");
			minsPerWeek.setReadOnly(!line.isEditable());
			return minsPerWeek;
		case NBR_ROOMS:
			final NumberBox nbrRooms = new NumberBox();
			nbrRooms.setWidth("40px");
			nbrRooms.setMaxLength(5);
			nbrRooms.setValue(line.getNumberOfRooms());
			nbrRooms.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					try {
						line.setNumberOfRooms(Integer.parseInt(event.getValue()));
					} catch (NumberFormatException e) {
						line.setNumberOfRooms(null);
						nbrRooms.setText("");
					}
					CheckBox splitAttendance = (CheckBox)getWidget(getRowForWidget(nbrRooms), getIndex(InstrOfferingConfigColumn.SPLIT_ATTENDANCE));
					splitAttendance.setEnabled(line.getNumberOfRooms() != null && line.getNumberOfRooms() > 1);
					if (!splitAttendance.isEnabled() && splitAttendance.getValue())
						splitAttendance.setValue(false, true);
				}
			});
			nbrRooms.addStyleName("subpart-nbr-rooms");
			nbrRooms.setReadOnly(!line.isEditable());
			return nbrRooms;
		case ROOM_RATIO:
			final NumberBox roomRatio = new NumberBox();
			roomRatio.setDecimal(true);
			roomRatio.setMaxLength(6);
			roomRatio.setWidth("40px");
			roomRatio.addStyleName("subpart-room-ratio");
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
			return roomRatio;
		case SPLIT_ATTENDANCE:
			final CheckBox splitAttendance = new CheckBox();
			splitAttendance.setValue(line.getSplitAttendance());
			splitAttendance.addStyleName("subpart-split-attendace");
			splitAttendance.setEnabled(line.isEditable() && (line.getNumberOfRooms() != null && line.getNumberOfRooms() > 1));
			splitAttendance.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					line.setSplitAttendance(event.getValue());
				}
			});
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
			return department;
		default: return null;
		}
	}
	
	class ClassLimit extends P {
		private SubpartLine iLine;
		private NumberBox iMin, iMax;
		ClassLimit(SubpartLine line) {
			super("class-limit");
			iLine = line;
			iMin = new NumberBox(); iMin.setMaxLength(5);
			iMin.setValue(line.getMinClassLimit());
			iMin.setReadOnly(!line.isEditable());
			iMin.setWidth("40px");
			add(iMin);
			iMax = new NumberBox(); iMax.setMaxLength(5);
			iMax.setValue(line.getMaxClassLimit());
			iMax.setReadOnly(!line.isEditable());
			iMax.setWidth("40px");
			iMax.getElement().getStyle().setMarginLeft(2, Unit.PX);
			iMax.setVisible(iData.isDisplayMaxLimit());
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
					updateNumberOfClasses();
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
					updateNumberOfClasses();
					onLimitChange();
				}
			});
		}
		
		protected void updateNumberOfClasses() {
			if (iLine.getMaxClassLimit() != null && iData.getLimit() != null && !iData.isUnlimited()) {
				NumberBox nbrClasses = (NumberBox)InstrOfferingConfigTable.this.getWidget(getRowForWidget(ClassLimit.this), getIndex(InstrOfferingConfigColumn.NBR_CLASSES));
				SubpartLine parent = iData.getSubpartLine(iLine.getParentId());
				if (parent != null && parent.getMaxClassLimit() != null && parent.getNumberOfClasses() != null) {
					int classesPerParent = (int)Math.ceil(parent.getMaxClassLimit().doubleValue() / iLine.getMaxClassLimit());
					if (iLine.getMaxClassLimit() <= 0)
						iLine.setNumberOfClasses(null);
					else
						iLine.setNumberOfClasses(parent.getNumberOfClasses() * classesPerParent);
				} else {
					if (iLine.getMaxClassLimit() <= 0)
						iLine.setNumberOfClasses(null);
					else
						iLine.setNumberOfClasses((int)Math.ceil(iData.getLimit().doubleValue() / iLine.getMaxClassLimit()));
				}
				nbrClasses.setValue(iLine.getNumberOfClasses());
			}
		}
		
		public void setMaxVisible(boolean visible) {
			iMax.setVisible(visible);
		}
	}
	
	private interface Check {
		public boolean check(SubpartLine line);
	}
	
	private interface HasUpdate {
		public void update(int row);
	}
	
	private class Buttons extends P implements HasUpdate {
		private Buttons() {
			super("subpart-buttons");
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
		private SubpartLine iLine;
		private ClickHandler iClickHandler;
		private Check iCheck;
		private boolean iLastCheck = false;
		
		private Operation(SubpartLine line, ImageResource image, String title, ClickHandler click, Check check) {
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
	
	private class Error extends P implements HasUpdate {
		private SubpartLine iLine;
		private Image iError;
		private Image iLock;
		
		Error(SubpartLine line) {
			super("subpart-error");
			iLine = line;
			iError = new Image(RESOURCES.attention());
			iError.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					UniTimeConfirmationDialog.alert(iLine.getError(), true);
				}
			});
			iLock = new Image(RESOURCES.lock());
		}
		
		@Override
		public void update(int row) {
			clear();
			if (iLine.hasError()) {
				iError.setTitle(iLine.getError().replace("<br>","\n").replace("&nbsp;", " "));
				add(iError);
			} else if (iLine.isLocked()) {
				add(iLock);
			}
		}
	}
	
	private class SubpartLabel extends P implements HasUpdate {
		private SubpartLine iLine;
		private Label iLabel;
		
		SubpartLabel(SubpartLine line) {
			super("subpart-label");
			iLine = line;
			iLabel = new Label(line.getLabel());
			if (!iLine.isEditable())
				iLabel.addStyleName("not-editable");
			if (iLine.getIndent() > 0) {
				add(new Image(RESOURCES.indent()));
				getElement().getStyle().setPaddingLeft(20 * (iLine.getIndent() - 1), Unit.PX);
			}
			add(iLabel);
		}
		
		@Override
		public void update(int row) {
			clear();
			iLabel.setText(iLine.getLabel());// + " [" + iLine.getSubpartId() + ":" + iLine.getParentId() + "]"
			if (iLine.getIndent() > 0) {
				add(new Image(RESOURCES.indent()));
				getElement().getStyle().setPaddingLeft(20 * (iLine.getIndent() - 1), Unit.PX);
			} else {
				getElement().getStyle().setPaddingLeft(0, Unit.PX);
			}
			add(iLabel);
		}
	}
}
