/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;

import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

public class WebTable extends Composite {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);

	private Row[] iHeader;
	private String iEmptyMessage = MESSAGES.tableEmpty();
	private Row[] iRows;
	
	private ArrayList<RowClickHandler> iRowClickHandlers = new ArrayList<RowClickHandler>();
	private ArrayList<RowDoubleClickHandler> iRowDoubleClickHandlers = new ArrayList<RowDoubleClickHandler>();
	
	private RowSelectingFlexTable iTable;
	
	private int iSelectedRow = -1;
	
	private boolean iSelectSameIdRows = false;
	
	public WebTable() {
		iTable = new RowSelectingFlexTable();
		iTable.setCellPadding(2);
		iTable.setCellSpacing(0);
		initWidget(iTable);
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
					c.setStyleName(cell.getStyleName());
					cells.add(c);
				}
				data[i] = new Row(cells);
			}
			x.setData(data);
		} else {
			x.setEmptyMessage(iEmptyMessage);
		}
		return x;
	}
	
	public void setHeader(Row... header) {
		iHeader = header;
		for (int i=0; i<header.length; i++) {
			for (int j=0; j<header[i].getNrCells(); j++) {
				Cell cell = header[i].getCell(j);
				if (cell.getWidget() == null)
					iTable.setHTML(i, j, (cell.getValue() == null || cell.getValue().isEmpty() ? "&nbsp;" : cell.getValue()));
				else
					iTable.setWidget(i, j, cell.getWidget());
				iTable.getFlexCellFormatter().setStyleName(i, j, (cell.getStyleName() == null ? "unitime-TableHeader" : cell.getStyleName()));
				iTable.getFlexCellFormatter().setWidth(i, j, (cell.getWidth() == null ? (100 / header.length) + "%" : cell.getWidth()));
				iTable.getFlexCellFormatter().setColSpan(i, j, cell.getColSpan());
				iTable.getFlexCellFormatter().setVerticalAlignment(i, j, cell.getVerticalAlignment());
				iTable.getFlexCellFormatter().setHorizontalAlignment(i, j, cell.getHorizontalAlignment());
			}
		}
	}
	
	public void setColumnVisible(int col, boolean visible) {
		for (int row = 0; row < iTable.getRowCount(); row++) {
			iTable.getFlexCellFormatter().setVisible(row, col, visible);
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
	}
	
	public void setData(Row... rows) {
		setSelectedRow(-1);
		if (rows==null || rows.length==0) {
			clearData(true);
			return;
		}
		clearData(false);
		iRows = rows;
		for (int i=0; i<iRows.length; i++) {
			if (iRows[i] == null) continue;
			iRows[i].setRowIdx(i);
			iRows[i].setTable(this);
			iTable.getRowFormatter().setStyleName(i+getHeaderRowsCount(), null);
			for (int j=0; j<iRows[i].getNrCells(); j++) {
				Cell cell = iRows[i].getCell(j);
				cell.setColIdx(j);
				cell.setRow(iRows[i]);
				if (cell.getWidget() == null)
					iTable.setHTML(i+getHeaderRowsCount(), j, (cell.getValue() == null || cell.getValue().isEmpty() ? "&nbsp;" : cell.getValue()));
				else
					iTable.setWidget(i+getHeaderRowsCount(), j, cell.getWidget());
				iTable.getFlexCellFormatter().setColSpan(i+getHeaderRowsCount(), j, cell.getColSpan());
				iTable.getFlexCellFormatter().setStyleName(i+getHeaderRowsCount(), j, cell.getStyleName());
				iTable.getFlexCellFormatter().setWidth(i+getHeaderRowsCount(), j, cell.getWidth());
				iTable.getFlexCellFormatter().setVerticalAlignment(i+getHeaderRowsCount(), j, cell.getVerticalAlignment());
				iTable.getFlexCellFormatter().setHorizontalAlignment(i+getHeaderRowsCount(), j, cell.getHorizontalAlignment());
			}
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
						iTable.getRowFormatter().setStyleName(getHeaderRowsCount() + r.getRowIdx(), null);	
				}
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

	public static class Row {
		private String iId;
		private Cell[] iCells;
		private int iRowIdx = -1;
		private WebTable iTable;
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
					t.getFlexCellFormatter().setColSpan(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getColSpan());
					t.getFlexCellFormatter().setStyleName(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getStyleName());
					t.getFlexCellFormatter().setWidth(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getWidth());
					t.getFlexCellFormatter().setVerticalAlignment(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getVerticalAlignment());
					t.getFlexCellFormatter().setHorizontalAlignment(getRowIdx() + iTable.getHeaderRowsCount(), col, cell.getHorizontalAlignment());
			}
		}
	}
	
	public static class Cell {
		String iValue;
		int iColSpan = 1;
		String iStyleName = null;
		String iWidth = null;
		Row iRow = null;
		int iColIdx = -1;
		VerticalAlignmentConstant iVerticalAlignment = HasVerticalAlignment.ALIGN_TOP;
		HorizontalAlignmentConstant iHorizontalAlignment = HasHorizontalAlignment.ALIGN_LEFT;
		
		public Cell(String value) {
			iValue = value;
		}
		public Cell(String value, int colSpan, String width) {
			iValue = value;
			iColSpan = colSpan;
			iWidth = width;
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
	}
	
	public static class CheckboxCell extends Cell {
		private CheckBox iCheck = new CheckBox();
		
		public CheckboxCell(boolean check) {
			super(null);
			iCheck.setValue(check);
			iCheck.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
		}
		
		public boolean isChecked() { return iCheck.getValue(); }
		public String getValue() { return iCheck.getValue().toString(); }
		public Widget getWidget() { return iCheck; }
	}
	
	public static class IconCell extends Cell {
		private Image iIcon = null;
		private HTML iLabel = null;
		private HorizontalPanel iPanel = null;
		
		public IconCell(ImageResource resource, String title, String text) {
			super(null);
			iIcon = new Image(resource);
			iIcon.setTitle(title);
			iLabel = new HTML(text, false);
			iPanel = new HorizontalPanel();
			iPanel.add(iIcon);
			iPanel.add(iLabel);
			iIcon.getElement().getStyle().setPaddingRight(3, Unit.PX);
		}
		
		public String getValue() { return iLabel.getText(); }
		public Widget getWidget() { return iPanel; }
		public void setStyleName(String styleName) {
			super.setStyleName(styleName);
			iLabel.setStyleName(styleName);
			iLabel.getElement().getStyle().setBorderWidth(0, Unit.PX);
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
		
		protected void add(String text) {
			iText += text;
			HTML h = new HTML(text, false);
			iPanel.add(h);
			iContent.add(h);
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
			if (names != null) {
				separator = separator.replace(" ", "&nbsp;");
				for (int i = 0; i < names.size(); i++) {
					String text = names.get(i) + (i + 1 < names.size() ? separator : "");
					String email = (emails != null && i < emails.size() ? emails.get(i) : null);
					if (email != null && !email.isEmpty()) {
						iText += text;
						HorizontalPanel p = new HorizontalPanel();
						Anchor a = new Anchor();
						a.setHref("mailto:" + email);
						a.setHTML(DOM.toString(new Image(RESOURCES.email()).getElement()));
						a.setTitle("Send " + names.get(i) + " an email.");
						a.setStyleName("unitime-SimpleLink");
						a.addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								event.stopPropagation();
							}
						});
						p.add(a);
						HTML h = new HTML(text, false);
						p.add(h);
						iPanel.add(p);
						iContent.add(h);
					} else
						add(text);
				}
			}
		}
	}
	
	public class RowSelectingFlexTable extends FlexTable {
		public RowSelectingFlexTable() {
			super();
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
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
				}
				break;
			case Event.ONMOUSEOUT:
				if (row >= getHeaderRowsCount() && row < getHeaderRowsCount() + iRows.length && row - getHeaderRowsCount() != iSelectedRow) {
					if (isSelectSameIdRows()) {
						String id = iRows[row - getHeaderRowsCount()].getId();
						String sid = getSelectedRowId();
						for (Row r: iRows) {
							if (id.equals(r.getId()))
								getRowFormatter().setStyleName(getHeaderRowsCount() + r.getRowIdx(), (id.equals(sid)? "unitime-TableRowSelectedHover" : null));	
						}
					} else {
						getRowFormatter().setStyleName(row, (row - getHeaderRowsCount() == iSelectedRow? "unitime-TableRowSelectedHover" : null));
					}
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
	
}
