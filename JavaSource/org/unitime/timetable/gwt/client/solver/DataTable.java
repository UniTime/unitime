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
package org.unitime.timetable.gwt.client.solver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.TimeHint;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.rooms.RoomHint;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.SimpleForm.HasMobileScroll;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.TableInterface;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellInterface;
import org.unitime.timetable.gwt.shared.TableInterface.TableRowInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class DataTable extends UniTimeTable<TableInterface.TableRowInterface> implements HasValue<Integer>, HasMobileScroll {
	private Integer iValue = null;
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	
	public DataTable(TableInterface table) {
		addStyleName("unitime-DataTable");
		if (table != null)
			populate(table);
		addMouseClickListener(new MouseClickListener<TableInterface.TableRowInterface>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<TableRowInterface> event) {
				if (event.getData() != null && event.getData().hasLink()) {
					clearHover();
					GwtHint.hideHint();
					if (event.getData().getLinkName() != null)
						UniTimeFrameDialog.openDialog(event.getData().getLinkName(), event.getData().getLink(),"900","85%");
					else
						ToolBox.open(GWT.getHostPageBaseURL() + event.getData().getLink());
				}
			}
		});
	}
	
	public void populate(TableInterface table) {
		clearTable();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (final TableInterface.TableHeaderIterface th: table.getHeader()) {
			final UniTimeTableHeader h = new UniTimeTableHeader(th.getName());
			if (th.isComparable())
				h.addOperation(new UniTimeTableHeader.Operation() {
					@Override
					public void execute() {
						boolean asc = (h.getOrder() == null ? true : !h.getOrder());
						setValue(asc ? 1 + h.getColumn() : -1 - h.getColumn(), true);
					}
					
					@Override
					public boolean isApplicable() {
						return getRowCount() > 1;
					}
					
					@Override
					public boolean hasSeparator() {
						return false;
					}
					
					@Override
					public String getName() {
						return MESSAGES.opSortBy(th.getName());
					}
				});
			if (th.getAlignment() != null)
				switch (th.getAlignment()) {
				case CENTER:
					h.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
					break;
				case LEFT:
					h.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
					break;
				case RIGHT:
					h.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
					break;
				}
			header.add(h);
		}
		addRow(null, header);
		for (TableInterface.TableRowInterface row: table.getRows()) {
			List<Widget> line = new ArrayList<Widget>();
			int idx = 0;
			for (TableInterface.TableCellInterface cell: row.getCells()) {
				line.add(new DataTableCell(table.getHeader(idx++), cell));
			}
			int rowIdx = addRow(row, line);
			if (row.isSelected()) {
				getRowFormatter().setStyleName(rowIdx, "unitime-TableRowSelected");
			}
		}
		for (int i = 0; i < table.getHeader().length; i++)
			if (!table.getHeader(i).isVisible())
				setColumnVisible(i, false);
	}
	
	public static class DataTableCell extends P implements HasCellAlignment {
		private TableInterface.TableHeaderIterface iHeader;
		
		public DataTableCell(TableInterface.TableHeaderIterface header, TableInterface.TableCellInterface cell) {
			iHeader = header;
			if (cell.hasStyleName()) addStyleName(cell.getStyleName());
			if (cell.isUnderlined()) addStyleName("underlined");
			if (cell.hasColor()) getElement().getStyle().setColor(cell.getColor());
			if (cell instanceof TableInterface.TableCellTime) {
				final TableInterface.TableCellTime time = (TableInterface.TableCellTime)cell;
				if (time.hasId()) {
					addMouseOverHandler(new MouseOverHandler() {
						@Override
						public void onMouseOver(MouseOverEvent event) {
							TimeHint.showHint(DataTableCell.this.getElement(), time.getId());
						}
					});
					addMouseOutHandler(new MouseOutHandler() {
						@Override
						public void onMouseOut(MouseOutEvent event) {
							TimeHint.hideHint();
						}
					});
				}
			}
			if (cell instanceof TableInterface.TableCellRooms) {
				addStyleName("collection");
				final TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)cell; 
				for (int index = 0; index < rooms.getNrRooms(); index++) {
					final P room = new P("item");
					room.setText(rooms.getName(index) + (index + 1 < rooms.getNrRooms() ? "," : ""));
					room.getElement().getStyle().setColor(rooms.getColor(index));
					final String id = rooms.getId(index);
					final String preference = rooms.getPreference(index);
					if (id != null) {
						room.addMouseOverHandler(new MouseOverHandler() {
							@Override
							public void onMouseOver(MouseOverEvent event) {
								RoomHint.showHint(room.getElement(), Long.parseLong(id), preference, null, true);
							}
						});
						room.addMouseOutHandler(new MouseOutHandler() {
							@Override
							public void onMouseOut(MouseOutEvent event) {
								RoomHint.hideHint();
							}
						});
					}
					add(room);
				}
				return;
			}
			if (cell instanceof TableInterface.TableCellItems) {
				addStyleName("collection");
				final TableInterface.TableCellItems instructors = (TableInterface.TableCellItems)cell; 
				for (int index = 0; index < instructors.getNrItems(); index++) {
					final P instructor = new P("item");
					instructor.setText(instructors.getFormattedValue(index) + (index + 1 < instructors.getNrItems() ? "," : ""));
					if (instructors.getColor(index) != null)
						instructor.getElement().getStyle().setColor(instructors.getColor(index));
					add(instructor);
				}
				return;
			}
			if (cell instanceof TableInterface.TableCellMultiLine) {
				addStyleName("multiline");
				TableInterface.TableCellMultiLine multi = (TableInterface.TableCellMultiLine)cell;
				for (int index = 0; index < multi.getNrChunks(); index++) {
					add(new DataTableCell(header, multi.get(index)));
				}
				return;
			}
			if (cell instanceof TableInterface.TableCellMulti) {
				addStyleName("multi");
				TableInterface.TableCellMulti multi = (TableInterface.TableCellMulti)cell;
				for (int index = 0; index < multi.getNrChunks(); index++) {
					final P chunk = new P("chunk");
					TableCellInterface<?> m = multi.get(index);
					if (m.hasStyleName()) chunk.addStyleName(m.getStyleName());
					if (m.hasColor()) chunk.getElement().getStyle().setColor(m.getColor());
					chunk.setText(m.getFormattedValue());
					add(chunk);
				}
				return;
			}
			if (cell instanceof TableInterface.TableCellChange) {
				addStyleName("change");
				TableInterface.TableCellChange change = (TableInterface.TableCellChange)cell;
				if (change.getFirst() != null && change.getSecond() != null && change.getFirst().compareTo(change.getSecond()) == 0) {
					add(new DataTableCell(header, change.getFirst()));
					return;
				}
				if (change.getFirst() == null) {
					P notAssigned = new P("not-assigned"); notAssigned.setText(MESSAGES.notAssigned()); add(notAssigned);
				} else {
					add(new DataTableCell(header, change.getFirst()));
				}
				P arrow = new P("arrow"); arrow.setHTML(MESSAGES.assignmentArrow()); add(arrow);
				if (change.getSecond() == null) {
					P notAssigned = new P("not-assigned"); notAssigned.setText(MESSAGES.notAssigned()); add(notAssigned);
				} else {
					add(new DataTableCell(header, change.getSecond()));
				}
				return;
			}
			if (cell instanceof TableInterface.TableCellBoolean) {
				Boolean value = ((TableInterface.TableCellBoolean)cell).getValue();
				if (value != null && value.booleanValue())
					add(new Image(RESOURCES.on()));
				else if (value != null && !value.booleanValue())
					add(new Image(RESOURCES.off()));
			} else {
				setHTML(cell.getFormattedValue());
			}
			if (cell instanceof TableInterface.TableCellClassName && ((TableInterface.TableCellClassName)cell).hasAlternatives()) {
				addStyleName("collection");
				for (String name: ((TableInterface.TableCellClassName)cell).getAlternatives()) {
					final P alternative = new P("alternative");
					alternative.setText(name);
					add(alternative);
				}
			}
			if (cell instanceof TableInterface.TableCellClickableClassName) {
				final Long classId = ((TableInterface.TableCellClickableClassName)cell).getClassId();
				if (classId != null) {
					addStyleName("clickable");
					addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							UniTimeFrameDialog.openDialog(MESSAGES.dialogSuggestions(), "gwt.jsp?page=suggestions&menu=hide&id=" + classId,"900","90%");
						}
					});
				}
			}
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			if (iHeader == null || iHeader.getAlignment() == null)
				return HasHorizontalAlignment.ALIGN_LEFT;
			switch (iHeader.getAlignment()) {
			case CENTER: return HasHorizontalAlignment.ALIGN_CENTER;
			case LEFT: return HasHorizontalAlignment.ALIGN_LEFT;
			case RIGHT: return HasHorizontalAlignment.ALIGN_RIGHT;
			default: return HasHorizontalAlignment.ALIGN_LEFT;
			}
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Integer getValue() {
		return iValue;
	}

	@Override
	public void setValue(Integer value) {
		setValue(value, false);
	}

	@Override
	public void setValue(final Integer value, boolean fireEvents) {
		iValue = value;
		if (value != null && value.intValue() != 0) {
			final int column = (value > 0 ? value - 1 : -value - 1);
			final UniTimeTableHeader h = getHeader(column);
			if (h != null)
				sort(h, new Comparator<TableInterface.TableRowInterface>() {
					@Override
					public int compare(TableInterface.TableRowInterface r1, TableInterface.TableRowInterface r2) {
						return r1.compareTo(r2, column, true);
					}
				}, value > 0);
		}
		if (fireEvents)
			ValueChangeEvent.fire(this, getValue());
	}

}
