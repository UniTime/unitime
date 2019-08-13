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
package org.unitime.timetable.gwt.client.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.aria.AriaCheckBox;
import org.unitime.timetable.gwt.client.aria.AriaHiddenLabel;
import org.unitime.timetable.gwt.client.aria.AriaToggleButton;
import org.unitime.timetable.gwt.client.aria.HasAriaLabel;
import org.unitime.timetable.gwt.client.rooms.RoomHint;
import org.unitime.timetable.gwt.client.widgets.SimpleForm.HasMobileScroll;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.IdValue;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

/**
 * @author Tomas Muller
 */
public class WebTable extends Composite implements HasMobileScroll {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	private Row[] iHeader;
	private String iEmptyMessage = MESSAGES.tableEmpty();
	private Row[] iRows;
	
	private ArrayList<RowClickHandler> iRowClickHandlers = new ArrayList<RowClickHandler>();
	private ArrayList<RowDoubleClickHandler> iRowDoubleClickHandlers = new ArrayList<RowDoubleClickHandler>();
	
	private ArrayList<RowOverHandler> iRowOverHandlers = new ArrayList<RowOverHandler>();
	private ArrayList<RowOutHandler> iRowOutHandlers = new ArrayList<RowOutHandler>();
	private ArrayList<RowMoveHandler> iRowMoveHandlers = new ArrayList<RowMoveHandler>();
	
	private RowSelectingFlexTable iTable;
	
	private int iSelectedRow = -1;
	
	private boolean iSelectSameIdRows = false;
	
	public WebTable() {
		iTable = new RowSelectingFlexTable();
		iTable.setCellPadding(2);
		iTable.setCellSpacing(0);
		iTable.setStyleName("unitime-WebTable");
		initWidget(iTable);
		Roles.getGridRole().set(iTable.getElement());
	}
	
	public Widget getPrintWidget(int... skipColumns) {
		ArrayList<Integer> skip = new ArrayList<Integer>();
		for (int c: skipColumns) skip.add(c);
		return getPrintWidget(skip);
	}

	public Widget getPrintWidget(ArrayList<Integer> skipColumns) {
		WebTable x = new WebTable();
		if (iHeader != null) {
			Row[] header = new Row[iHeader.length];
			for (int i = 0; i < iHeader.length; i++) {
				int col = 0;
				ArrayList<Cell> cells = new ArrayList<Cell>();
				for (int j = 0; j < iHeader[i].getNrCells(); j++) {
					Cell cell = iHeader[i].getCell(j);
					int newColSpan = 0;
					for (int c = 0; c < cell.getColSpan(); c++) {
						if (!skipColumns.contains(col + c)) newColSpan++;
					}
					col += cell.getColSpan();
					if (newColSpan == 0) continue;
					Cell c = new Cell(cell.getValue(), newColSpan, cell.getWidth());
					c.setStyleName(cell.getStyleName());
					cells.add(c);
				}
				header[i] = new Row(cells);
			}
			x.setHeader(header);
		}
		if (iRows != null && iRows.length > 0) {
			Row[] data = new Row[iRows.length];
			for (int i = 0; i < iRows.length; i++) {
				int col = 0;
				ArrayList<Cell> cells = new ArrayList<Cell>();
				for (int j = 0; j < iRows[i].getNrCells(); j++) {
					Cell cell = iRows[i].getCell(j);
					int newColSpan = 0;
					for (int c = 0; c < cell.getColSpan(); c++) {
						if (!skipColumns.contains(col + c)) newColSpan++;
					}
					col += cell.getColSpan();
					if (newColSpan == 0) continue;
					Cell c = new Cell(cell.getValue(), newColSpan, cell.getWidth());
					c.setWordWrap(true);
					c.setStyleName(cell.getStyleName());
					cells.add(c);
				}
				data[i] = new Row(cells);
			}
			x.setData(data);
		} else {
			x.setEmptyMessage(iEmptyMessage);
		}
		x.setWidth("940px");
		return x;
	}
	
	public void setHeader(Row... header) {
		iHeader = header;
		for (int i=0; i<header.length; i++) {
			for (int j=0; j<header[i].getNrCells(); j++) {
				Cell cell = header[i].getCell(j);
				if (cell.getWidget() == null) {
					iTable.setHTML(i, j, (cell.getValue() == null || cell.getValue().isEmpty() ? "&nbsp;" : cell.getValue()));
					if (cell.getAriaLabel() != null && !cell.getAriaLabel().isEmpty())
						Roles.getGridcellRole().setAriaLabelProperty(iTable.getCellFormatter().getElement(i, j), cell.getAriaLabel());
				} else
					iTable.setWidget(i, j, cell.getWidget());
				iTable.getFlexCellFormatter().setWordWrap(i, j, cell.getWordWrap());
				iTable.getFlexCellFormatter().setStyleName(i, j, (cell.getStyleName() == null ? "unitime-TableHeader" : cell.getStyleName()));
				if (cell.getWidth() != null)
					iTable.getFlexCellFormatter().setWidth(i, j, cell.getWidth());
				iTable.getFlexCellFormatter().setColSpan(i, j, cell.getColSpan());
				iTable.getFlexCellFormatter().setVerticalAlignment(i, j, cell.getVerticalAlignment());
				iTable.getFlexCellFormatter().setHorizontalAlignment(i, j, cell.getHorizontalAlignment());
				iTable.getFlexCellFormatter().getElement(i, j).setTitle(cell.getTitle());
				Roles.getColumnheaderRole().set(iTable.getCellFormatter().getElement(i, j));
			}
			Roles.getRowRole().set(iTable.getRowFormatter().getElement(i));
			
			if (header[i].getAriaLabel() != null && !header[i].getAriaLabel().isEmpty()) {
				iTable.setWidget(i, header[i].getNrCells(), new AriaHiddenLabel(header[i].getAriaLabel()));
			} else {
				iTable.setHTML(i, header[i].getNrCells(), "");
			}
			Roles.getRowheaderRole().set(iTable.getCellFormatter().getElement(i, header[i].getNrCells()));
			iTable.getFlexCellFormatter().setStyleName(i, header[i].getNrCells(), "unitime-TableHeader");
			iTable.getFlexCellFormatter().addStyleName(i, header[i].getNrCells(), "rowheader");
		}
	}
	
	public boolean isColumnVisible(int col) {
		if (iTable.getRowCount() <= 0) return true;
		for (int c = 0; c < iTable.getCellCount(0); c++) {
			col -= iTable.getFlexCellFormatter().getColSpan(0, c);
			if (col < 0) return iTable.getCellFormatter().isVisible(0, c);
		}
		return true;
	}
	
	public void setColumnVisible(int col, boolean visible) {
		for (int r = 0; r < iTable.getRowCount(); r++) {
			int x = 0;
			for (int c = 0; c < iTable.getCellCount(r); c++) {
				int colSpan = iTable.getFlexCellFormatter().getColSpan(r, c);
				x += colSpan;
				if (x > col) {
					if (colSpan > 1 && r > 0) {
						// use first row to count the colspan
						int span = 0;
						for (int h = x - colSpan; h < x; h++)
							if (isColumnVisible(h)) span ++;
						iTable.getCellFormatter().setVisible(r, c, span > 0);
						iTable.getFlexCellFormatter().setColSpan(r, c, Math.max(1, span));
					} else {
						iTable.getCellFormatter().setVisible(r, c, visible);
					}
					break;
				}
			}
		}
	}

	public int getColumnsCount() {
		if (iHeader==null || iHeader.length==0) return 1;
		int ret = 0;
		for (int i=0; i<iHeader[0].getNrCells(); i++)
			ret += iHeader[0].getCell(i).getColSpan();
		return ret;
	}
	
	public int getHeaderRowsCount() {
		if (iHeader==null) return 0;
		return iHeader.length;
	}
	
	public int getRowsCount() {
		if (iRows==null) return 0;
		return iRows.length;
	}

	public void setEmptyMessage(String emptyMessage) {
		iEmptyMessage = emptyMessage;
		if (iRows==null || iRows.length==0) {
			iTable.setHTML(getHeaderRowsCount(), 0, iEmptyMessage);
			if (iEmptyMessage!=null) {
				iTable.getFlexCellFormatter().setColSpan(getHeaderRowsCount(), 0, getColumnsCount());
				iTable.getFlexCellFormatter().setStyleName(getHeaderRowsCount(), 0, "unitime-TableEmpty");
			} else {
				iTable.getFlexCellFormatter().setColSpan(getHeaderRowsCount(), 0, 1);
				iTable.getFlexCellFormatter().setStyleName(getHeaderRowsCount(), 0, null);
			}
		}
	}
	
	public void clearData(boolean showEmptyMessage) {
		for (int row = iTable.getRowCount() - 1; row >= getHeaderRowsCount(); row--) {
			iTable.removeRow(row);
		}
		iRows = null;
		if (showEmptyMessage)
			setEmptyMessage(iEmptyMessage);
	}
	
	public void clearData(int keepRows) {
		for (int row = iTable.getRowCount() - 1; row >= getHeaderRowsCount() + keepRows; row--) {
			iTable.removeRow(row);
		}
	}
	
	public boolean isEmpty() {
		return iRows == null || iRows.length == 0;
	}
	
	public void setData(Row... rows) {
		setSelectedRow(-1);
		if (rows==null || rows.length==0) {
			clearData(true);
			return;
		}
		clearData(rows.length);
		iRows = rows;
		for (int i=0; i<iRows.length; i++) {
			if (iRows[i] == null) continue;
			iRows[i].setRowIdx(i);
			iRows[i].setTable(this);
			iTable.getRowFormatter().setStyleName(i+getHeaderRowsCount(), iRows[i].getStyleName());
			for (int j=0; j<iRows[i].getNrCells(); j++) {
				Cell cell = iRows[i].getCell(j);
				cell.setColIdx(j);
				cell.setRow(iRows[i]);
				if (cell.getWidget() == null) {
					iTable.setHTML(i+getHeaderRowsCount(), j, (cell.getValue() == null || cell.getValue().isEmpty() ? "&nbsp;" : cell.getValue()));
					if (cell.getAriaLabel() != null && !cell.getAriaLabel().isEmpty())
						Roles.getGridcellRole().setAriaLabelProperty(iTable.getCellFormatter().getElement(i + getHeaderRowsCount(), j), cell.getAriaLabel());
				} else
					iTable.setWidget(i+getHeaderRowsCount(), j, cell.getWidget());
				iTable.getFlexCellFormatter().setVisible(i+getHeaderRowsCount(), j, true);
				iTable.getFlexCellFormatter().setWordWrap(i+getHeaderRowsCount(), j, cell.getWordWrap());
				iTable.getFlexCellFormatter().setColSpan(i+getHeaderRowsCount(), j, cell.getColSpan());
				iTable.getFlexCellFormatter().setStyleName(i+getHeaderRowsCount(), j, cell.getStyleName());
				iTable.getFlexCellFormatter().setWidth(i+getHeaderRowsCount(), j, cell.getWidth());
				iTable.getFlexCellFormatter().setVerticalAlignment(i+getHeaderRowsCount(), j, cell.getVerticalAlignment());
				iTable.getFlexCellFormatter().setHorizontalAlignment(i+getHeaderRowsCount(), j, cell.getHorizontalAlignment());
				iTable.getFlexCellFormatter().getElement(i+getHeaderRowsCount(), j).setTitle(cell.getTitle());
				Roles.getGridcellRole().set(iTable.getCellFormatter().getElement(i + getHeaderRowsCount(), j));
			}
			Roles.getRowRole().set(iTable.getRowFormatter().getElement(i + getHeaderRowsCount()));
			
			for (int j=iTable.getCellCount(i+getHeaderRowsCount()) - 1; j >= iRows[i].getNrCells(); j--)
				iTable.removeCell(i+getHeaderRowsCount(), j);
			
			if (iRows[i].getAriaLabel() != null && !iRows[i].getAriaLabel().isEmpty()) {
				iTable.setWidget(i + getHeaderRowsCount(), iRows[i].getNrCells(), new AriaHiddenLabel(iRows[i].getAriaLabel()));
			} else {
				iTable.setHTML(i + getHeaderRowsCount(), iRows[i].getNrCells(), "");
			}
			Roles.getRowheaderRole().set(iTable.getCellFormatter().getElement(i + getHeaderRowsCount(), iRows[i].getNrCells()));
			iTable.getFlexCellFormatter().setStyleName(i + getHeaderRowsCount(), iRows[i].getNrCells(), iRows[i].getCell(iRows[i].getNrCells() - 1).getStyleName());
			iTable.getFlexCellFormatter().addStyleName(i + getHeaderRowsCount(), iRows[i].getNrCells(), "rowheader");
		}
	}
	
	public void setData(String[]... lines) {
		Row[] rows = new Row[lines.length];
		for (int i=0; i<lines.length; i++) {
			rows[i] = new Row(lines[i]);
		}
		setData(rows);
	}
	
	public void addRowClickHandler(RowClickHandler rowClickHandler) {
		iRowClickHandlers.add(rowClickHandler);
	}
	
	public void addRowDoubleClickHandler(RowDoubleClickHandler rowDoubleClickHandler) {
		iRowDoubleClickHandlers.add(rowDoubleClickHandler);
	}
	
	public void addRowOverHandler(RowOverHandler rowOverHandler) {
		iRowOverHandlers.add(rowOverHandler);
	}

	public void addRowOutHandler(RowOutHandler rowOutHandler) {
		iRowOutHandlers.add(rowOutHandler);
	}

	public void addRowMoveHandler(RowMoveHandler rowMoveHandler) {
		iRowMoveHandlers.add(rowMoveHandler);
	}

	public void fireRowClickEvent(Event event, int row) {
		RowClickEvent e = new RowClickEvent(event, iRows[row], row);
		for (RowClickHandler h: iRowClickHandlers)
			h.onRowClick(e);
	}
	
	public void fireDoubleRowClickEvent(Event event, int row) {
		RowDoubleClickEvent e = new RowDoubleClickEvent(event, iRows[row], row);
		for (RowDoubleClickHandler h: iRowDoubleClickHandlers)
			h.onRowDoubleClick(e);
	}
	
	public void setSelectedRow(int row) {
		if (iSelectedRow>=0) {
			if (isSelectSameIdRows() && iRows != null && iSelectedRow < iRows.length) {
				String id = iRows[iSelectedRow].getId();
				for (Row r: iRows) {
					if (id.equals(r.getId()))
						iTable.getRowFormatter().setStyleName(getHeaderRowsCount() + r.getRowIdx(), r.getStyleName());
				}
			} else if (iRows != null && iSelectedRow < iRows.length) {
				iTable.getRowFormatter().setStyleName(iSelectedRow + getHeaderRowsCount(), iRows[iSelectedRow].getStyleName());
			} else {
				iTable.getRowFormatter().setStyleName(iSelectedRow + getHeaderRowsCount(), null);
			}
		}
		if (row<0 || iRows==null || iRows.length==0) {
			iSelectedRow = -1;
		} else {
			iSelectedRow = row % iRows.length;
			if (isSelectSameIdRows()) {
				String id = iRows[iSelectedRow].getId();
				for (Row r: iRows) {
					if (id.equals(r.getId()))
						iTable.getRowFormatter().setStyleName(getHeaderRowsCount() + r.getRowIdx(), "unitime-TableRowSelected");	
				}
			} else {
				iTable.getRowFormatter().setStyleName(iSelectedRow + getHeaderRowsCount(), "unitime-TableRowSelected");
			}
		}
	}
	
	public int getSelectedRow() {
		return iSelectedRow;
	}
	
	public Row[] getRows() {
		return iRows;
	}
	
	public void setSelectSameIdRows(boolean selectSameIdRows) {
		iSelectSameIdRows = selectSameIdRows;
	}
	
	public boolean isSelectSameIdRows() {
		return iSelectSameIdRows;
	}
	
	public String getSelectedRowId() {
		if (iSelectedRow < 0 || iSelectedRow >= iRows.length) return null;
		return iRows[iSelectedRow].getId();
	}

	public static class Row implements HasAriaLabel {
		private String iId;
		private Cell[] iCells;
		private int iRowIdx = -1;
		private WebTable iTable;
		private boolean iSelectable = true;
		private String iAriaLabel = null;
		private String iStyleName = null;
		public Row(Cell... cells) {
			iCells = cells;
		}
		public Row(ArrayList<Cell> cells) {
			iCells = new Cell[cells.size()];
			for (int i = 0; i < iCells.length; i++)
				iCells[i] = cells.get(i);
		}
		public Row(String... cells) {
			iCells = new Cell[cells.length];
			for (int i=0; i<cells.length;i++)
				iCells[i] = new Cell(cells[i]);
		}
		public int getNrCells() { return iCells.length; }
		public Cell getCell(int idx) { return iCells[idx]; }
		public Cell[] getCells() { return iCells; }
		public String getId() { return iId; }
		public void setId(String id) { iId = id; }
		public int getRowIdx() { return iRowIdx; }
		public void setRowIdx(int rowIdx) { iRowIdx = rowIdx; }
		public void setTable(WebTable table) { iTable = table; }
		public WebTable getTable() { return iTable; }
		public boolean isSelectable() { return iSelectable; }
		public void setSelectable(boolean selectable) { iSelectable = selectable; }
		public void setCell(int col, Cell cell) {
			iCells[col] = cell;
			if (iTable != null) {
				FlexTable t = iTable.iTable;
				cell.setColIdx(col);
				cell.setRow(this);
				if (cell.getWidget() == null)
					t.setHTML(getRowIdx() + iTable.getHeaderRowsCount(), col, (cell.getValue() == null || cell.getValue().isEmpty() ? "&nbsp;" : cell.getValue()));
				else
					t.setWidget(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getWidget());
				t.getFlexCellFormatter().setWordWrap(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getWordWrap());
				t.getFlexCellFormatter().setColSpan(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getColSpan());
				t.getFlexCellFormatter().setStyleName(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getStyleName());
				t.getFlexCellFormatter().setWidth(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getWidth());
				t.getFlexCellFormatter().setVerticalAlignment(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getVerticalAlignment());
				t.getFlexCellFormatter().setHorizontalAlignment(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getHorizontalAlignment());
				t.getFlexCellFormatter().getElement(getRowIdx() + iTable.getHeaderRowsCount(), col).setTitle(cell.getTitle());
			}
		}
		public String getStyleName() { return iStyleName; }
		public void setStyleName(String styleName) { iStyleName = styleName; }
		@Override
		public String getAriaLabel() {
			return iAriaLabel;
		}
		@Override
		public void setAriaLabel(String text) {
			iAriaLabel = text;
		}
	}
	
	public static class Cell implements HasAriaLabel {
		String iValue;
		int iColSpan = 1;
		String iStyleName = null;
		String iWidth = null;
		Row iRow = null;
		int iColIdx = -1;
		boolean iWrap = false;
		VerticalAlignmentConstant iVerticalAlignment = HasVerticalAlignment.ALIGN_TOP;
		HorizontalAlignmentConstant iHorizontalAlignment = HasHorizontalAlignment.ALIGN_LEFT;
		String iAriaLabel = null;
		String iTitle = null;
		
		public Cell(String value) {
			iValue = value;
		}
		public Cell(String value, int colSpan, String width) {
			iValue = value;
			iColSpan = colSpan;
			iWidth = width;
		}
		public Cell(String value, boolean wrap) {
			iValue = value;
			iWrap = wrap;
		}
		public String getValue() { return iValue; }
		public int getColSpan() { return iColSpan; }
		public void setColSpan(int colSpan) { iColSpan = colSpan; }
		public String getStyleName() { return iStyleName; }
		public void setStyleName(String styleName) {
			iStyleName = styleName;
			if (iRow != null)
				iRow.iTable.iTable.getCellFormatter().setStyleName(iRow.iTable.getHeaderRowsCount() + iRow.iRowIdx, iColIdx, iStyleName);
		}
		public String getWidth() { return iWidth; }
		public void setWitdh(String width) { iWidth = width; }
		public void setColIdx(int colIdx) { iColIdx = colIdx; }
		public int getColIdx() { return iColIdx; }
		public void setRow(Row row) { iRow = row; }
		public Row getRow() { return iRow; }
		public Widget getWidget() { return null; }
		public VerticalAlignmentConstant getVerticalAlignment() { return iVerticalAlignment; }
		public void setVerticalAlignment(VerticalAlignmentConstant vertical) { iVerticalAlignment = vertical; }
		public HorizontalAlignmentConstant getHorizontalAlignment() { return iHorizontalAlignment; }
		public void setHorizontalAlignment(HorizontalAlignmentConstant vertical) { iHorizontalAlignment = vertical; }
		public boolean getWordWrap() { return iWrap; }
		public void setWordWrap(boolean wrap) { iWrap = wrap; }
		public void setTitle(String title) { iTitle = title; }
		public String getTitle() { return iTitle; }
		public Cell title(String title) { iTitle = title; return this; }
		
		@Override
		public String toString() {
			return getValue();
		}
		@Override
		public String getAriaLabel() {
			return iAriaLabel;
		}
		@Override
		public void setAriaLabel(String text) {
			iAriaLabel = text;
		}
		
		public Cell aria(String text) {
			iAriaLabel = text;
			return this;
		}
	}
	
	public static class CheckboxCell extends Cell implements HasAriaLabel {
		private AriaCheckBox iCheck = new AriaCheckBox();
		
		public CheckboxCell(boolean check, String text, String ariaLabel) {
			super(null);
			if (text != null)
				iCheck.setText(text);
			iCheck.setValue(check);
			iCheck.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			if (ariaLabel != null) setAriaLabel(ariaLabel);
		}
		
		public CheckboxCell(boolean check, String ariaLabel) {
			this(check, null, ariaLabel);
		}
		
		public CheckboxCell(boolean check, String ariaLabel, final String onTitle, final String offTitle) {
			this(check, null, ariaLabel);
			iCheck.setTitle(iCheck.getValue() ? onTitle : offTitle);
			iCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iCheck.setTitle(event.getValue() ? onTitle : offTitle);
				}
			});
		}
		
		public boolean isChecked() { return iCheck.getValue(); }
		public String getValue() { return iCheck.getValue().toString(); }
		public Widget getWidget() { return iCheck; }

		@Override
		public String getAriaLabel() {
			return iCheck.getAriaLabel();
		}
		@Override
		public void setAriaLabel(String text) {
			iCheck.setAriaLabel(text);
		}
	}
	
	public static class LockCell extends Cell implements HasAriaLabel {
		private HasValue<Boolean> iCheck = null;
		
		public LockCell(boolean check, String text, String ariaLabel) {
			super(null);
			if (CONSTANTS.listOfClassesUseLockIcon()) {
				iCheck = new AriaToggleButton(RESOURCES.locked(), RESOURCES.unlocked());
			} else {
				iCheck = new AriaCheckBox();
			}
			if (text != null)
				((HasText)iCheck).setText(text);
			iCheck.setValue(check);
			((HasClickHandlers)iCheck).addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
			if (ariaLabel != null) setAriaLabel(ariaLabel);
		}
		
		public LockCell(boolean check, String ariaLabel) {
			this(check, null, ariaLabel);
		}
		
		public LockCell(boolean check, String ariaLabel, final String onTitle, final String offTitle) {
			this(check, null, ariaLabel);
			getWidget().setTitle(iCheck.getValue() ? onTitle : offTitle);
			iCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					getWidget().setTitle(event.getValue() ? onTitle : offTitle);
				}
			});
		}
		
		public boolean isChecked() { return iCheck.getValue(); }
		public String getValue() { return iCheck.getValue().toString(); }
		public Widget getWidget() { return (Widget)iCheck; }

		@Override
		public String getAriaLabel() {
			return ((HasAriaLabel)iCheck).getAriaLabel();
		}

		@Override
		public void setAriaLabel(String text) {
			((HasAriaLabel)iCheck).setAriaLabel(text);
		}
	}
	
	public static class IconCell extends Cell {
		private Image iIcon = null;
		private HTML iLabel = null;
		private HorizontalPanel iPanel = null;
		
		public IconCell(ImageResource resource, final String title, String text) {
			super(null);
			iIcon = new Image(resource);
			iIcon.setTitle(title);
			iIcon.setAltText(title);
			if (text != null && !text.isEmpty()) {
				iLabel = new HTML(text, false);
				iPanel = new HorizontalPanel();
				iPanel.setStyleName("icon");
				iPanel.add(iIcon);
				iPanel.add(iLabel);
				iIcon.getElement().getStyle().setPaddingRight(3, Unit.PX);
				iPanel.setCellVerticalAlignment(iIcon, HasVerticalAlignment.ALIGN_MIDDLE);
			}
			if (title != null && !title.isEmpty()) {
				iIcon.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						UniTimeConfirmationDialog.info(title);
					}
				});
			}
		}
		
		public String getValue() { return (iPanel == null ? iIcon.getTitle() : iLabel.getText()); }
		public Widget getWidget() { return (iPanel == null ? iIcon : iPanel); }
		public Image getIcon() { return iIcon; }
		public void setStyleName(String styleName) {
			super.setStyleName(styleName);
			if (iLabel != null) {
				iLabel.setStyleName(styleName);
				iLabel.getElement().getStyle().setBorderWidth(0, Unit.PX);
			}
		}
	}
	
	public static class IconsCell extends Cell {
		private HorizontalPanel iPanel = null;
		
		public IconsCell() {
			super(null);
			iPanel = new HorizontalPanel();
			iPanel.setStyleName("icons");
		}
		
		public IconsCell add(ImageResource resource, final String title) {
			return add(resource, title, false);
		}
		
		public IconsCell add(ImageResource resource, final String title, final boolean html) {
			if (resource == null) return this;
			Image icon = new Image(resource);
			String text = title;
			if (html) { HTML h = new HTML(title); text = h.getText(); }
			icon.setTitle(text);
			icon.setAltText(text);
			if (iPanel.getWidgetCount() > 0)
				icon.getElement().getStyle().setMarginLeft(3, Unit.PX);
			iPanel.add(icon);
			iPanel.setCellVerticalAlignment(icon, HasVerticalAlignment.ALIGN_MIDDLE);
			if (title != null && !title.isEmpty()) {
				icon.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						UniTimeConfirmationDialog.info(title, html);
					}
				});
			}
			return this;
		}
		
		public String getValue() { 
			String value = "";
			for (int i = 0; i < iPanel.getWidgetCount(); i++) {
				if (i > 0) value += ", ";
				value += ((Image)iPanel.getWidget(i)).getTitle();
			}
			return value;
		}
		public Widget getWidget() { return iPanel; }
		public void setStyleName(String styleName) {
			super.setStyleName(styleName);
		}
	}
	
	public static class NoteCell extends Cell {
		private P iNote = null;
		private Image iIcon = null;
		
		public NoteCell(String text, final String title) {
			super(null);
			if (Window.getClientWidth() <= 800 && title != null && !title.isEmpty()) {
				iIcon = new Image(RESOURCES.note());
				iIcon.setTitle(title);
				iIcon.setAltText(title);
				iIcon.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						UniTimeConfirmationDialog.info(title);
					}
				});
			} else {
				iNote = new P("unitime-Note");
				iNote.setHTML(text);
				if (title != null)  {
					iNote.setTitle(title);
					iNote.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							event.stopPropagation();
							UniTimeConfirmationDialog.info(title);
						}
					});
				}
			}
		}
		
		public String getValue() { return iNote != null ? iNote.getHTML() : iIcon.getTitle(); }
		public Widget getWidget() { return iNote != null ? iNote : iIcon; }
		public void setStyleName(String styleName) {
			super.setStyleName(styleName);
		}
	}
	
	public static class AbbvTextCell extends Cell {
		private P iNote = null;
		
		public AbbvTextCell(String text) {
			super(null);
			iNote = new P("unitime-Note");
			if (text != null && text.indexOf('|') >= 0) {
				iNote.setHTML(text.substring(0, text.indexOf('|')));
				iNote.setTitle(text.substring(text.indexOf('|') + 1).replace("\n", "<br>"));
			} else {
				iNote.setHTML(text == null ? "" : text.replace("\n", "<br>"));
				if (text != null) iNote.setTitle(text);
			}
		}
		
		public AbbvTextCell(String text, String title) {
			super(null);
			iNote = new P("unitime-Note");
			iNote.setHTML(text == null ? "" : text.replace("\n", "<br>"));
			if (title != null) iNote.setTitle(title);
		}
		
		public String getValue() { return iNote.getHTML(); }
		public Widget getWidget() { return iNote; }
		public void setStyleName(String styleName) {
			super.setStyleName(styleName);
		}
	}
	
	public static class WidgetCell extends Cell {
		private Widget iWidget = null;
		
		public WidgetCell(Widget widget, String text, int colspan, String width) {
			super(text, colspan, width);
			iWidget = widget;
		}
		
		public WidgetCell(Widget widget, String text) {
			super(text);
			iWidget = widget;
		}

		public Widget getWidget() { return iWidget; }
	}
	
	public static class MultiCell extends Cell {
		protected Panel iPanel = new FlowPanel();
		protected String iText = "";
		protected ArrayList<UIObject> iContent = new ArrayList<UIObject>();

		public MultiCell(ArrayList<String> names, String separator) {
			super(null);
			if (names != null) {
				separator = separator.replace(" ", "&nbsp;");
				for (int i = 0; i < names.size(); i++)
					add(names.get(i) + (i + 1 < names.size() ? separator : ""));
			}
		}
		
		protected HTML add(String text) {
			iText += text;
			HTML h = new HTML(text, false);
			iPanel.add(h);
			iContent.add(h);
			return h;
		}
		
		public String getValue() { return iText; }
		public Widget getWidget() { return iPanel; }
		public void setStyleName(String styleName) {
			super.setStyleName(styleName);
			for (UIObject c: iContent) {
				c.setStyleName(styleName);
				c.getElement().getStyle().setBorderWidth(0, Unit.PX);
			}
		}
	}
	
	public static class InstructorCell extends MultiCell {
		
		public InstructorCell(ArrayList<String> names, ArrayList<String> emails, String separator) {
			super(null, separator);
			if (names != null && !names.isEmpty()) {
				separator = separator.replace(" ", "&nbsp;");
				for (int i = 0; i < names.size(); i++) {
					String text = names.get(i) + (i + 1 < names.size() ? separator : "");
					String email = (emails != null && i < emails.size() ? emails.get(i) : null);
					if (email != null && !email.isEmpty()) {
						iText += text;
						HorizontalPanel p = new HorizontalPanel();
						p.setStyleName("instructor");
						Anchor a = new Anchor();
						a.setHref("mailto:" + email);
						a.setHTML(new Image(RESOURCES.email()).getElement().getString());
						a.setTitle(MESSAGES.sendEmail(names.get(i)));
						a.setStyleName("unitime-SimpleLink");
						a.addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								event.stopPropagation();
							}
						});
						p.add(a);
						p.setCellVerticalAlignment(a, HasVerticalAlignment.ALIGN_MIDDLE);
						HTML h = new HTML(text, false);
						h.getElement().getStyle().setMarginLeft(2, Unit.PX);
						p.add(h);
						iPanel.add(p);
						iContent.add(h);
					} else
						add(text);
				}
			} else {
				add("&nbsp;");
			}
		}
	}
	
	public static class RoomCell extends MultiCell {
		
		public RoomCell(ImageResource resource, final String title, List<IdValue> rooms, String separator) {
			super(null, separator);
			Image icon = null;
			if (resource != null) {
				icon = new Image(resource);
				icon.setTitle(title);
				icon.setAltText(title);
				icon.getElement().getStyle().setPaddingRight(3, Unit.PX);
				iPanel.add(icon);
				icon.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						UniTimeConfirmationDialog.info(title);
					}
				});
			}
			if (rooms != null && !rooms.isEmpty()) {
				separator = separator.replace(" ", "&nbsp;");
				for (Iterator<IdValue> i = rooms.iterator(); i.hasNext(); ) {
					final IdValue room = i.next();
					final HTML h = (icon == null ? add(room.getValue() + (i.hasNext() ? separator : "")) : new HTML(room.getValue() + (i.hasNext() ? separator : ""), false));
					if (icon != null) {
						HorizontalPanel hp = new HorizontalPanel();
						hp.add(icon);
						hp.add(h);
						hp.setCellVerticalAlignment(icon, HasVerticalAlignment.ALIGN_MIDDLE);
						iContent.add(h);
						iPanel.add(hp);
						icon = null;
						hp.addStyleName("room");
					}
					if (room.getId() != null) {
						h.addMouseOverHandler(new MouseOverHandler() {
							@Override
							public void onMouseOver(MouseOverEvent event) {
								RoomHint.showHint(h.getElement(), room.getId(), null, null, true);
							}
						});
						h.addMouseOutHandler(new MouseOutHandler() {
							@Override
							public void onMouseOut(MouseOutEvent event) {
								RoomHint.hideHint();
							}
						});
					}
				}
			} else {
				if (icon != null) {
					iPanel.add(icon);
				} else {
					add("&nbsp;");
				}
			}
		}
		
		public RoomCell(List<IdValue> rooms, String separator) {
			this(null, null, rooms, separator);
		}		
	}
	
	public class RowSelectingFlexTable extends FlexTable {
		public RowSelectingFlexTable() {
			super();
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONMOUSEMOVE);
			sinkEvents(Event.ONCLICK);
			sinkEvents(Event.ONDBLCLICK);
		}
		public void onBrowserEvent(Event event) {
			if (iRows==null || iRows.length==0) return;

			Element td = getEventTargetCell(event);
			if (td==null) return;
		    Element tr = DOM.getParent(td);
		    Element body = DOM.getParent(tr);
		    int row = DOM.getChildIndex(body, tr);

			if (row >= getHeaderRowsCount() && row < getHeaderRowsCount() + iRows.length && !iRows[row - getHeaderRowsCount()].isSelectable())
				return;
			
			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER:
				if (row >= getHeaderRowsCount() && row < getHeaderRowsCount() + iRows.length) {
					if (isSelectSameIdRows()) {
						String id = iRows[row - getHeaderRowsCount()].getId();
						String sid = getSelectedRowId();
						for (Row r: iRows) {
							if (id.equals(r.getId()))
								getRowFormatter().setStyleName(getHeaderRowsCount() + r.getRowIdx(), (id.equals(sid)? "unitime-TableRowSelectedHover" : "unitime-TableRowHover"));	
						}
					} else {
						getRowFormatter().setStyleName(row, (row - getHeaderRowsCount() == iSelectedRow? "unitime-TableRowSelectedHover" : "unitime-TableRowHover"));	
					}
					RowOverEvent e = new RowOverEvent(event, iRows[row - getHeaderRowsCount()], row - getHeaderRowsCount());
					for (RowOverHandler h: iRowOverHandlers)
						h.onRowOver(e);
					if (!iRowClickHandlers.isEmpty())
						getRowFormatter().getElement(row).getStyle().setCursor(Cursor.POINTER);
				}
				break;
			case Event.ONMOUSEOUT:
				if (row >= getHeaderRowsCount() && row < getHeaderRowsCount() + iRows.length && row - getHeaderRowsCount() != iSelectedRow) {
					if (isSelectSameIdRows()) {
						String id = iRows[row - getHeaderRowsCount()].getId();
						String sid = getSelectedRowId();
						for (Row r: iRows) {
							if (id.equals(r.getId()))
								getRowFormatter().setStyleName(getHeaderRowsCount() + r.getRowIdx(), (id.equals(sid)? "unitime-TableRowSelectedHover" : r.getStyleName()));	
						}
					} else {
						getRowFormatter().setStyleName(row, (row - getHeaderRowsCount() == iSelectedRow? "unitime-TableRowSelectedHover" : iRows[row - getHeaderRowsCount()].getStyleName()));
					}
				}
				if (row >= getHeaderRowsCount() && row < getHeaderRowsCount() + iRows.length) {
					RowOutEvent e = new RowOutEvent(event, iRows[row - getHeaderRowsCount()], row - getHeaderRowsCount());
					for (RowOutHandler h: iRowOutHandlers)
						h.onRowOut(e);
					if (!iRowClickHandlers.isEmpty())
						getRowFormatter().getElement(row).getStyle().clearCursor();
				}
				break;
			case Event.ONCLICK:
				if (row >= getHeaderRowsCount() && row < getHeaderRowsCount() + iRows.length) {
					RowClickEvent e = new RowClickEvent(event, iRows[row - getHeaderRowsCount()], row - getHeaderRowsCount());
					for (RowClickHandler h: iRowClickHandlers)
						h.onRowClick(e);
				}
				break;
			case Event.ONDBLCLICK:
				if (row >= getHeaderRowsCount() && row < getHeaderRowsCount() + iRows.length) {
					RowDoubleClickEvent e = new RowDoubleClickEvent(event, iRows[row - getHeaderRowsCount()], row - getHeaderRowsCount());
					for (RowDoubleClickHandler h: iRowDoubleClickHandlers)
						h.onRowDoubleClick(e);
				}
				break;
			case Event.ONMOUSEMOVE:
				RowMoveEvent e = new RowMoveEvent(event, iRows[row - getHeaderRowsCount()], row - getHeaderRowsCount());
				for (RowMoveHandler h: iRowMoveHandlers)
					h.onRowMove(e);
				break;
			}
		}
	}
	
	public FlexTable getTable() {
		return iTable;
	}
	
	public static class RowClickEvent {
		private Event iEvent;
		private Row iRow;
		private int iRowIdx;
		private RowClickEvent(Event event, Row row, int rowIdx) {
			iEvent = event;
			iRow = row;
			iRowIdx = rowIdx;
		}
		public Event getEvent() { return iEvent; }
		public Row getRow() { return iRow; }
		public int getRowIdx() { return iRowIdx; }
	}
		
	public static interface RowClickHandler {
		public void onRowClick(RowClickEvent event);
	}
	
	public static class RowDoubleClickEvent {
		private Event iEvent;
		private Row iRow;
		private int iRowIdx;
		private RowDoubleClickEvent(Event event, Row row, int rowIdx) {
			iEvent = event;
			iRow = row;
			iRowIdx = rowIdx;
		}
		public Event getEvent() { return iEvent; }
		public Row getRow() { return iRow; }
		public int getRowIdx() { return iRowIdx; }
	}
	
	public static interface RowDoubleClickHandler {
		public void onRowDoubleClick(RowDoubleClickEvent event);
	}
	
	public static class RowOverEvent extends RowClickEvent {
		private RowOverEvent(Event event, Row row, int rowIdx) {
			super(event, row, rowIdx);
		}
	}

	public static interface RowOverHandler {
		public void onRowOver(RowOverEvent event);
	}
	
	public static class RowOutEvent extends RowClickEvent {
		private RowOutEvent(Event event, Row row, int rowIdx) {
			super(event, row, rowIdx);
		}
	}

	public static interface RowOutHandler {
		public void onRowOut(RowOutEvent event);
	}
	
	public static class RowMoveEvent extends RowClickEvent {
		private RowMoveEvent(Event event, Row row, int rowIdx) {
			super(event, row, rowIdx);
		}
	}

	public static interface RowMoveHandler {
		public void onRowMove(RowMoveEvent event);
	}

}
