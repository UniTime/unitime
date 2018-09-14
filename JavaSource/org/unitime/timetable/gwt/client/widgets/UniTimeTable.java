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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.aria.AriaCheckBox;
import org.unitime.timetable.gwt.client.aria.AriaHiddenLabel;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;

/**
 * @author Tomas Muller
 */
public class UniTimeTable<T> extends FlexTable implements SimpleForm.HasMobileScroll, HasEnabled {
	
	private List<MouseOverListener<T>> iMouseOverListeners = new ArrayList<MouseOverListener<T>>();
	private List<MouseOutListener<T>> iMouseOutListeners = new ArrayList<MouseOutListener<T>>();
	private List<MouseClickListener<T>> iMouseClickListeners = new ArrayList<MouseClickListener<T>>();
	private List<MouseDoubleClickListener<T>> iMouseDoubleClickListeners = new ArrayList<MouseDoubleClickListener<T>>();
	private List<DataChangedListener<T>> iDataChangedListeners = new ArrayList<DataChangedListener<T>>();
	
	private PopupPanel iHintPanel = null;
	protected HintProvider<T> iHintProvider = null;
	
	protected int iLastHoverRow = -1;
	protected Map<Integer,String> iLastHoverBackgroundColor = new HashMap<Integer, String>();
	private boolean iAllowSelection = false, iAllowMultiSelect= true;
	private boolean iEnabled = true;
	
	public UniTimeTable() {
		setCellPadding(2);
		setCellSpacing(0);
		sinkEvents(Event.ONMOUSEOVER);
		sinkEvents(Event.ONMOUSEOUT);
		sinkEvents(Event.ONCLICK);
		sinkEvents(Event.ONKEYDOWN);
		sinkEvents(Event.ONDBLCLICK);
		setStylePrimaryName("unitime-MainTable");
		iHintPanel = new PopupPanel();
		iHintPanel.setStyleName("unitime-PopupHint");
		Roles.getGridRole().set(getElement());
	}
	
	public void setAllowSelection(boolean allow) { iAllowSelection = allow; }
	public boolean isAllowSelection() { return iAllowSelection; }
	
	public void setAllowMultiSelect(boolean allow) { iAllowMultiSelect = allow; }
	public boolean isAllowMultiSelect() { return iAllowMultiSelect; }
	
	public boolean isCanSelectRow(int row) { return true; }

	public void clearTable(int headerRows) {
		for (int row = getRowCount() - 1; row >= headerRows; row--)
			removeRow(row);
		iLastHoverBackgroundColor.clear();
	}
	
	public void clearTable() {
		clearTable(0);
	}
	
	public int addRow(T data, Widget... widgets) {
		List<Widget> list = new ArrayList<Widget>();
		for (Widget widget: widgets)
			list.add(widget);
		return addRow(data, list);
	}
	
	public int addRow(T data, List<? extends Widget> widgets) {
		int row = getRowCount();
		setRow(row, data, widgets);
		return row;
	}
	
	public void setRow(int row, T data, List<? extends Widget> widgets) {
		SmartTableRow<T> oldRow = getSmartRow(row);
		if (oldRow != null && oldRow.getData() != null) {
			DataChangedEvent<T> event = new DataChangedEvent<T>(oldRow.getData(), row);
			for (DataChangedListener<T> listener: iDataChangedListeners)
				listener.onDataRemoved(event);
		}
		SmartTableRow smartRow = new SmartTableRow(data);
		int col = 0, x = 0;
		for (Widget widget: widgets) {
			SmartTableCell cell = new SmartTableCell(smartRow, widget);
			int colspan = 1;
			if (widget instanceof HasColSpan) {
				colspan = ((HasColSpan)widget).getColSpan();
				getFlexCellFormatter().setColSpan(row, col, colspan);
			}
			if (widget instanceof HasStyleName && ((HasStyleName)widget).getStyleName() != null)
				getFlexCellFormatter().setStyleName(row, col, ((HasStyleName)widget).getStyleName());
			if (widget instanceof HasAdditionalStyleNames) {
				List<String> styleNames = ((HasAdditionalStyleNames)widget).getAdditionalStyleNames();
				if (styleNames != null)
					for (String styleName: styleNames)
						getFlexCellFormatter().addStyleName(row, col, styleName);
			}
			if (widget instanceof  HasCellAlignment)
				getFlexCellFormatter().setHorizontalAlignment(row, col, ((HasCellAlignment)widget).getCellAlignment());
			if (widget instanceof  HasVerticalCellAlignment)
				getFlexCellFormatter().setVerticalAlignment(row, col, ((HasVerticalCellAlignment)widget).getVerticalCellAlignment());
			if (widget instanceof HasColumn)
				((HasColumn)widget).setColumn(col);
			setWidget(row, col, cell);
			if (widget instanceof AriaHiddenLabel) {
				getFlexCellFormatter().addStyleName(row, col, "rowheader");
				Roles.getRowheaderRole().set(getCellFormatter().getElement(row, col));
			} else if (widget instanceof UniTimeTableHeader) {
				Roles.getColumnheaderRole().set(getCellFormatter().getElement(row, col));
			} else {
				Roles.getGridcellRole().set(getCellFormatter().getElement(row, col));
			}
			x += colspan;
			if (row > 0) {
				if (colspan == 1) {
					getCellFormatter().setVisible(row, col, isColumnVisible(x - 1));
				} else {
					int span = 0;
					for (int h = x - colspan; h < x; h++)
						if (isColumnVisible(h)) span ++;
					getCellFormatter().setVisible(row, col, span > 0);
					getFlexCellFormatter().setColSpan(row, col, Math.max(1, span));
				}
			}
			col++;
		}
		Roles.getRowRole().set(getRowFormatter().getElement(row));
		if (data != null) {
			DataChangedEvent<T> event = new DataChangedEvent<T>(data, row);
			for (DataChangedListener<T> listener: iDataChangedListeners)
				listener.onDataInserted(event);
		}
	}
	
	public boolean isColumnVisible(int col) {
		if (getRowCount() <= 0) return true;
		for (int c = 0; c < getCellCount(0); c++) {
			col -= getFlexCellFormatter().getColSpan(0, c);
			if (col < 0) return getCellFormatter().isVisible(0, c);
		}
		return true;
	}
	
	public void setColumnVisible(int col, boolean visible) {
		for (int r = 0; r < getRowCount(); r++) {
			if (r == 0) {
				// now colspans for the first row
				getCellFormatter().setVisible(r, col, visible);
				continue;
			}
			int x = 0;
			for (int c = 0; c < getCellCount(r); c++) {
				Widget w = getWidget(r, c);
				int colSpan = (w instanceof HasColSpan ? ((HasColSpan)w).getColSpan() : 1);
				x += colSpan;
				if (x > col) {
					if (colSpan > 1) {
						// use first row to count the colspan
						int span = 0;
						for (int h = x - colSpan; h < x; h++)
							if (isColumnVisible(h)) span ++;
						getCellFormatter().setVisible(r, c, span > 0);
						getFlexCellFormatter().setColSpan(r, c, Math.max(1, span));
					} else {
						getCellFormatter().setVisible(r, c, visible);
					}
					break;
				}
			}
		}
	}
	
	public static class SmartTableRow<T> {
		private List<SmartTableCell> iCells = new ArrayList<SmartTableCell>();
		private T iData = null;
		
		public SmartTableRow(T data) {
			iData = data;
		}
		
		public T getData() {
			return iData;
		}
		
		public boolean hasData() {
			return iData != null;
		}
		
		public List<SmartTableCell> getCells() { return iCells; }
		
		public Comparator<SmartTableRow<T>> getComparator(final Comparator<T> cmp) {
			return new Comparator<SmartTableRow<T>>() {
				public int compare(SmartTableRow<T> a, SmartTableRow<T> b) {
					return cmp.compare(a.getData(), b.getData());
				}
			};
		}
	}
	
	public static class SmartTableCell extends Composite {
		SmartTableRow iRow;
		
		public SmartTableCell(SmartTableRow row, Widget widget) {
			iRow = row;
			row.getCells().add(this);
			initWidget(widget);
		}
		
		public SmartTableRow getRow() { return iRow; }
		
		public boolean focus() {
			if (getWidget() instanceof HasFocus) {
				return ((HasFocus)getWidget()).focus();
			} else if (getWidget() instanceof Focusable) {
				((Focusable)getWidget()).setFocus(true);
				if (getWidget() instanceof TextBoxBase)
					((TextBoxBase)getWidget()).selectAll();
				return true;
			}
			return false;
		}
		
		public Widget getInnerWidget() {
			return getWidget();
		}
	}
	
	public Widget getWidget(int row, int col) {
		Widget w = super.getWidget(row, col);
		if (w == null) return w;
		if (w instanceof SmartTableCell)
			return ((SmartTableCell)w).getInnerWidget();
		return w;
	}
	
	public Widget replaceWidget(int row, int col, Widget widget) {
		Widget w = super.getWidget(row, col);
		if (w == null)
			super.setWidget(row, col, widget);
		else if (w instanceof SmartTableCell)
			super.setWidget(row, col, new SmartTableCell(((SmartTableCell)w).getRow(), widget));
		else
			super.setWidget(row, col, widget);
		return w;
	}

	private boolean focus(int row, int col) {
		if (!getRowFormatter().isVisible(row) || col >= getCellCount(row)) return false;
		Widget w = super.getWidget(row, col);
		if (w == null || !w.isVisible()) return false;
		if (w instanceof SmartTableCell) {
			return ((SmartTableCell)w).focus();
		} else if (w instanceof HasFocus) {
			return ((HasFocus)w).focus();
		} else if (w instanceof Focusable) {
			((Focusable)w).setFocus(true);
			if (w instanceof TextBoxBase)
				((TextBoxBase)w).selectAll();
			return true;
		}
		return false;
	}
	
	public T getData(int row) {
		SmartTableRow<T> r = getSmartRow(row);
		return (r == null ? null : r.getData());
	}
	
	public List<T> getData() {
		List<T> ret = new ArrayList<T>();
		for (int row = 0; row < getRowCount(); row++) {
			T data = getData(row);
			if (data != null) ret.add(data);
		}
		return ret;
	}

	
	public SmartTableRow<T> getSmartRow(int row) {
		if (row < 0 || row >= getRowCount()) return null;
		for (int col = 0; col < getCellCount(row); col++) {
			Widget w = super.getWidget(row, col);
			if (w != null && w instanceof SmartTableCell)
				return ((SmartTableCell)w).getRow();
		}
		return null;
	}
	
	private void swapRows(int r0, int r1) {
		if (r0 == r1) return;
		if (r0 > r1) {
			swapRows(r1, r0);
		} else { // r0 < r1
			Element body = getBodyElement();
			Element a = DOM.getChild(body, r0);
			Element b = DOM.getChild(body, r1);
			body.removeChild(a);
			body.removeChild(b);
			DOM.insertChild(body, b, r0);
			DOM.insertChild(body, a, r1);
		}
	}
	
	public void sort(int column, final Comparator<T> rowComparator) {
		sort(getHeader(column), rowComparator);
	}
	
	public void sort(String columnName, final Comparator<T> rowComparator) {
		sort(getHeader(columnName), rowComparator);
	}
	
	public void sort(UniTimeTableHeader header, final Comparator<T> rowComparator) {
		if (header != null) {
			sort(header, rowComparator, header.getOrder() == null ? true : !header.getOrder());
		} else {
			sort(header, rowComparator, true);
		}
	}

	public void sort(UniTimeTableHeader header, final Comparator<T> rowComparator, boolean asc) {
		if (header != null) {
			for (int i = 0; i < getCellCount(0); i++) {
				Widget w = getWidget(0, i);
				if (w != null && w instanceof UniTimeTableHeader) {
					UniTimeTableHeader h = (UniTimeTableHeader)w;
					h.setOrder(null);
				}
			}
			header.setOrder(asc);
		}
		Element body = getBodyElement();
		ArrayList<Object[]> rows = new ArrayList<Object[]>();
		for (int row = 0; row < getRowCount(); row++) {
			SmartTableRow<T> r = getSmartRow(row);
			if (r != null && r.hasData()) {
				rows.add(new Object[] {r, getRowFormatter().getElement(row)});
			}
		}
		if (asc) {
			Collections.sort(rows,new Comparator<Object[]>() {
				public int compare(Object[] a, Object[] b) {
					return rowComparator.compare(((SmartTableRow<T>)a[0]).getData(), ((SmartTableRow<T>)b[0]).getData());
				}
			});
		} else {
			Collections.sort(rows,new Comparator<Object[]>() {
				public int compare(Object[] a, Object[] b) {
					return - rowComparator.compare(((SmartTableRow<T>)a[0]).getData(), ((SmartTableRow<T>)b[0]).getData());
				}
			});
		}
		int idx = 0;
		List<DataChangedEvent<T>> changeEvents = new ArrayList<DataChangedEvent<T>>();
		for (int row = 0; row < getRowCount(); row++) {
			SmartTableRow<T> a = getSmartRow(row);
			if (a != null && a.hasData()) {
				Object[] o = rows.get(idx++);
				int otherRow = DOM.getChildIndex(body, (Element)o[1]);
				swapRows(row, otherRow);
				changeEvents.add(new DataChangedEvent<T>(((SmartTableRow<T>)o[0]).getData(), row));
			}
		}
		for (DataChangedListener<T> listener: iDataChangedListeners) 
			listener.onDataSorted(changeEvents);
	}
	
	public void sortByRow(int column, final Comparator<Integer> rowComparator) {
		sortByRow(getHeader(column), rowComparator);
	}
	
	public void sortByRow(String columnName, final Comparator<Integer> rowComparator) {
		sortByRow(getHeader(columnName), rowComparator);
	}
	
	public void sortByRow(UniTimeTableHeader header, final Comparator<Integer> rowComparator) {
		if (header != null) {
			sortByRow(header, rowComparator, header.getOrder() == null ? true : !header.getOrder());
		} else {
			sortByRow(header, rowComparator, true);
		}
	}
	
	public void sortByRow(UniTimeTableHeader header, final Comparator<Integer> rowComparator, boolean asc) {
		if (header != null) {
			for (int i = 0; i < getCellCount(0); i++) {
				Widget w = getWidget(0, i);
				if (w != null && w instanceof UniTimeTableHeader) {
					UniTimeTableHeader h = (UniTimeTableHeader)w;
					h.setOrder(null);
				}
			}
			header.setOrder(asc);
		}
		Element body = getBodyElement();
		ArrayList<Object[]> rows = new ArrayList<Object[]>();
		for (int row = 0; row < getRowCount(); row++) {
			SmartTableRow<T> r = getSmartRow(row);
			if (r != null && r.hasData()) {
				rows.add(new Object[] {r, getRowFormatter().getElement(row), row});
			}
		}
		if (asc) {
			Collections.sort(rows,new Comparator<Object[]>() {
				public int compare(Object[] a, Object[] b) {
					return rowComparator.compare((Integer)a[2], (Integer)b[2]);
				}
			});
		} else {
			Collections.sort(rows,new Comparator<Object[]>() {
				public int compare(Object[] a, Object[] b) {
					return - rowComparator.compare((Integer)a[2], (Integer)b[2]);
				}
			});
		}
		int idx = 0;
		List<DataChangedEvent<T>> changeEvents = new ArrayList<DataChangedEvent<T>>();
		for (int row = 0; row < getRowCount(); row++) {
			SmartTableRow<T> a = getSmartRow(row);
			if (a != null && a.hasData()) {
				Object[] o = rows.get(idx++);
				int otherRow = DOM.getChildIndex(body, (Element)o[1]);
				swapRows(row, otherRow);
				changeEvents.add(new DataChangedEvent<T>(((SmartTableRow<T>)o[0]).getData(), row));
			}
		}
		for (DataChangedListener<T> listener: iDataChangedListeners) 
			listener.onDataSorted(changeEvents);
	}
	
	public boolean canSwapRows(T a, T b) {
		return true;
	}
	
	public void onBrowserEvent(final Event event) {
		Element td = getEventTargetCell(event);
		if (td==null) return;
	    final Element tr = DOM.getParent(td);
		int col = DOM.getChildIndex(tr, td);
	    Element body = DOM.getParent(tr);
	    int row = DOM.getChildIndex(body, tr);
	    
	    Widget widget = getWidget(row, col);
	    SmartTableRow<T> r = getSmartRow(row);
	    boolean hasData = (r != null && r.getData() != null);
	    
	    TableEvent<T> tableEvent = new TableEvent<T>(event, row, col, tr, td, hasData ? r.getData() : null);

	    Widget hint = null;
		if (widget instanceof HasHint) {
			String html = ((HasHint)widget).getHint();
			if (html != null && !html.isEmpty())
				hint = new HTML(html, false);
		}
		if (hint == null && iHintProvider != null)
			hint = iHintProvider.getHint(tableEvent);

		String style = getRowFormatter().getStyleName(row);

		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEOVER:
			if (hasData) {
				if (!iMouseClickListeners.isEmpty())
					getRowFormatter().getElement(row).getStyle().setCursor(Cursor.POINTER);
				boolean selected = false;
				if (isAllowSelection()) {
					if ("unitime-TableRowSelectedHover".equals(style)) {
						selected = true;
					} else if ("unitime-TableRowSelected".equals(style)) {
						getRowFormatter().setStyleName(row, "unitime-TableRowSelectedHover");
						selected = true;
					} else {
						getRowFormatter().setStyleName(row, "unitime-TableRowHover");
					}
				} else {
					getRowFormatter().addStyleName(row, "unitime-TableRowHover");
				}
				iLastHoverRow = row;
				if (!selected) {
					String color = getRowFormatter().getElement(row).getStyle().getBackgroundColor();
					if (color != null && !color.isEmpty()) {
						getRowFormatter().getElement(row).getStyle().clearBackgroundColor();
						iLastHoverBackgroundColor.put(row, color);
					} else {
						iLastHoverBackgroundColor.remove(row);
					}
				}
			}
			if (!iHintPanel.isShowing() && hint != null) {
				iHintPanel.setWidget(hint);
				iHintPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
					@Override
					public void setPosition(int offsetWidth, int offsetHeight) {
						boolean top = (tr.getAbsoluteBottom() - Window.getScrollTop() + 15 + offsetHeight > Window.getClientHeight());
						iHintPanel.setPopupPosition(
								Math.max(Math.min(event.getClientX(), tr.getAbsoluteRight() - offsetWidth - 15), tr.getAbsoluteLeft() + 15),
								top ? tr.getAbsoluteTop() - offsetHeight - 15 : tr.getAbsoluteBottom() + 15);
					}
				});
			}
			for (MouseOverListener<T> listener: iMouseOverListeners)
				listener.onMouseOver(tableEvent);
			break;
		case Event.ONMOUSEOUT:
			if (hasData) {
				if (!iMouseClickListeners.isEmpty())
					getRowFormatter().getElement(row).getStyle().clearCursor();
				boolean selected = false;
				if (isAllowSelection()) {
					if ("unitime-TableRowHover".equals(style)) {
						getRowFormatter().setStyleName(row, null);	
					} else if ("unitime-TableRowSelectedHover".equals(style)) {
						getRowFormatter().setStyleName(row, "unitime-TableRowSelected");
						selected = true;
					}
				} else {
					getRowFormatter().removeStyleName(row, "unitime-TableRowHover");
				}
				if (!selected) {
					String color = iLastHoverBackgroundColor.remove(row);
					if (color != null && !color.isEmpty()) {
						getRowFormatter().getElement(row).getStyle().setBackgroundColor(color);
					}
				}
				iLastHoverRow = -1;
			}
			if (iHintPanel.isShowing()) iHintPanel.hide();
			for (MouseOutListener<T> listener: iMouseOutListeners)
				listener.onMouseOut(tableEvent);
			break;
		case Event.ONMOUSEMOVE:
			if (iHintPanel.isShowing()) {
				boolean top = (tr.getAbsoluteBottom() - Window.getScrollTop() + 15 + iHintPanel.getOffsetHeight() > Window.getClientHeight());
				iHintPanel.setPopupPosition(
						Math.max(Math.min(event.getClientX(), tr.getAbsoluteRight() - iHintPanel.getOffsetWidth() - 15), tr.getAbsoluteLeft() + 15),
						top ? tr.getAbsoluteTop() - iHintPanel.getOffsetHeight() - 15 : tr.getAbsoluteBottom() + 15);
			}
			break;
		case Event.ONCLICK:
			if (isAllowSelection() && hasData && isCanSelectRow(row) && isEnabled()) {
				Element element = DOM.eventGetTarget(event);
				while (element.getPropertyString("tagName").equalsIgnoreCase("div"))
					element = DOM.getParent(element);
				if (isAllowMultiSelect()) {
					if (element.getPropertyString("tagName").equalsIgnoreCase("td")) {
						boolean hover = ("unitime-TableRowHover".equals(style) || "unitime-TableRowSelectedHover".equals(style));
						boolean selected = !("unitime-TableRowSelected".equals(style) || "unitime-TableRowSelectedHover".equals(style));
						getRowFormatter().setStyleName(row, "unitime-TableRow" + (selected ? "Selected" : "") + (hover ? "Hover" : ""));
					}
				} else {
					int old = getSelectedRow();
					if (old != row && old >= 0)
						setSelected(old, false);
					boolean hover = ("unitime-TableRowHover".equals(style) || "unitime-TableRowSelectedHover".equals(style));
					getRowFormatter().setStyleName(row, "unitime-TableRowSelected" + (hover ? "Hover" : ""));
				}
			}
			if (iHintPanel != null && iHintPanel.isShowing())
				iHintPanel.hide();
			for (MouseClickListener<T> listener: iMouseClickListeners)
				listener.onMouseClick(tableEvent);
			break;
		case Event.ONDBLCLICK:
			/*
			if (isAllowSelection() && hasData && isCanSelectRow(row)) {
				Element element = DOM.eventGetTarget(event);
				while (element.getPropertyString("tagName").equalsIgnoreCase("div"))
					element = DOM.getParent(element);
				if (isAllowMultiSelect()) {
					if (element.getPropertyString("tagName").equalsIgnoreCase("td")) {
						boolean hover = ("unitime-TableRowHover".equals(style) || "unitime-TableRowSelectedHover".equals(style));
						boolean selected = !("unitime-TableRowSelected".equals(style) || "unitime-TableRowSelectedHover".equals(style));
						getRowFormatter().setStyleName(row, "unitime-TableRow" + (selected ? "Selected" : "") + (hover ? "Hover" : ""));
					}
				} else {
					int old = getSelectedRow();
					if (old != row && old >= 0)
						setSelected(old, false);
					boolean hover = ("unitime-TableRowHover".equals(style) || "unitime-TableRowSelectedHover".equals(style));
					getRowFormatter().setStyleName(row, "unitime-TableRowSelected" + (hover ? "Hover" : ""));
				}
			}
			*/
			if (iHintPanel != null && iHintPanel.isShowing())
				iHintPanel.hide();
			for (MouseDoubleClickListener<T> listener: iMouseDoubleClickListeners)
				listener.onMouseDoubleClick(tableEvent);
			break;
		case Event.ONKEYDOWN:
			if (event.getKeyCode() == KeyCodes.KEY_RIGHT && (event.getAltKey() || event.getMetaKey())) {
				do {
					col++;
					if (col >= getCellCount(row)) break;
				} while (!focus(row, col));
				event.stopPropagation();
		    	event.preventDefault();
			}
			if (event.getKeyCode() == KeyCodes.KEY_LEFT && (event.getAltKey() || event.getMetaKey())) {
				do {
					col--;
					if (col < 0) break;
				} while (!focus(row, col));
				event.stopPropagation();
		    	event.preventDefault();
			}
			if (event.getKeyCode() == KeyCodes.KEY_UP && (event.getAltKey() || event.getMetaKey())) {
				do {
					row--;
					if (row < 0) break;
				} while (!focus(row, col));
				event.stopPropagation();
		    	event.preventDefault();
			} else if (hasData && event.getKeyCode() == KeyCodes.KEY_UP && event.getCtrlKey()) {
				SmartTableRow<T> up = getSmartRow(row - 1);
				if (up != null && up.getData() != null && canSwapRows(r.getData(), up.getData())) {
					getRowFormatter().removeStyleName(row, "unitime-TableRowHover");
					getRowFormatter().removeStyleName(row - 1, "unitime-TableRowHover");
					swapRows(row - 1, row);
					focus(row - 1, col);
					if (!iDataChangedListeners.isEmpty()) {
						List<DataChangedEvent<T>> e = new ArrayList<DataChangedEvent<T>>();
						e.add(new DataChangedEvent<T>(up.getData(), row));
						e.add(new DataChangedEvent<T>(r.getData(), row - 1));
						for (DataChangedListener<T> listener: iDataChangedListeners) {
							listener.onDataMoved(e);
						}
					}
				}
				event.stopPropagation();
		    	event.preventDefault();
			}
			if (event.getKeyCode() == KeyCodes.KEY_DOWN && (event.getAltKey() || event.getMetaKey())) {
				do {
					row++;
					if (row >= getRowCount()) break;
				} while (!focus(row, col));
				event.stopPropagation();
		    	event.preventDefault();
			} else if (hasData && event.getKeyCode() == KeyCodes.KEY_DOWN && event.getCtrlKey()) {
				SmartTableRow<T> dn = getSmartRow(row + 1);
				if (dn != null && dn.getData() != null && canSwapRows(r.getData(), dn.getData())) {
					getRowFormatter().removeStyleName(row, "unitime-TableRowHover");
					getRowFormatter().removeStyleName(row + 1, "unitime-TableRowHover");
					swapRows(row + 1, row);
					focus(row + 1, col);
					if (!iDataChangedListeners.isEmpty()) {
						List<DataChangedEvent<T>> e = new ArrayList<DataChangedEvent<T>>();
						e.add(new DataChangedEvent<T>(dn.getData(), row));
						e.add(new DataChangedEvent<T>(r.getData(), row + 1));
						for (DataChangedListener<T> listener: iDataChangedListeners) {
							listener.onDataMoved(e);
						}
					}
				}
				event.stopPropagation();
		    	event.preventDefault();
			}
			break;
	    }
	}
	
	public void clearHover() {
		if (iLastHoverRow >= 0 && iLastHoverRow < getRowCount()) {
			boolean selected = false;
			if (isAllowSelection()) {
				String style = getRowFormatter().getStyleName(iLastHoverRow);
				selected = ("unitime-TableRowSelected".equals(style) || "unitime-TableRowSelectedHover".equals(style));
				getRowFormatter().setStyleName(iLastHoverRow, "unitime-TableRow" + (selected ? "Selected" : ""));
			} else {
				getRowFormatter().removeStyleName(iLastHoverRow, "unitime-TableRowHover");
			}
			if (!selected) {
				String color = iLastHoverBackgroundColor.remove(iLastHoverRow);
				if (color != null && !color.isEmpty()) {
					getRowFormatter().getElement(iLastHoverRow).getStyle().setBackgroundColor(color);
				}
			}
		}
		iLastHoverRow = -1;
	}
	
	public boolean isSelected(int row) {
		if (isAllowSelection()) {
			String style = getRowFormatter().getStyleName(row);
			return "unitime-TableRowSelected".equals(style) || "unitime-TableRowSelectedHover".equals(style);
		} else {
			return false;
		}
	}
	
	public void setSelected(int row, boolean selected) {
		if (!isCanSelectRow(row)) return;
		if (isAllowSelection() && !isAllowMultiSelect() && selected) {
			int old = getSelectedRow();
			if (old >= 0 && old != row) setSelected(old, false);
		}
		if (isAllowSelection()) {
			String style = getRowFormatter().getStyleName(row);
			boolean hover = ("unitime-TableRowHover".equals(style) || "unitime-TableRowSelectedHover".equals(style));
			boolean wasSelected = ("unitime-TableRowSelected".equals(style) || "unitime-TableRowSelectedHover".equals(style));
			getRowFormatter().setStyleName(row, "unitime-TableRow" + (selected ? "Selected" : "") + (hover ? "Hover" : ""));
			if (!hover && wasSelected != selected) {
				if (selected) {
					String color = getRowFormatter().getElement(row).getStyle().getBackgroundColor();
					if (color != null && !color.isEmpty()) {
						getRowFormatter().getElement(row).getStyle().clearBackgroundColor();
						iLastHoverBackgroundColor.put(row, color);
					}
				} else {
					String color = iLastHoverBackgroundColor.remove(row);
					if (color != null && !color.isEmpty()) {
						getRowFormatter().getElement(row).getStyle().setBackgroundColor(color);
					}
				}
			}
		}
	}
	
	public int getSelectedCount() {
		int selected = 0;
		for (int row = 0; row < getRowCount(); row ++)
			if (isSelected(row)) selected ++;
		return selected;
	}
	
	public int getSelectedRow() {
		for (int row = 0; row < getRowCount(); row ++)
			if (isSelected(row)) return row;
		return -1;
	}
	
	public static class TableEvent<T> {
		private Event iSourceEvent;
		private int iRow;
		private int iCol;
		private Element iTD;
		private Element iTR;
		private T iData;
		
		public TableEvent(Event sourceEvent, int row, int col, Element tr, Element td, T data) {
			iRow = row;
			iCol = col;
			iTR = tr;
			iTD = td;
			iData = data;
			iSourceEvent = sourceEvent;
		}
		
		public int getRow() { return iRow; }
		public int getCol() { return iCol; }
		public T getData() { return iData; }
		public Element getRowElement() { return iTR; }
		public Element getCellElement() { return iTD; }
		public Event getSourceEvent() { return iSourceEvent; }
	}
	
	public static interface MouseOverListener<T> {
		public void onMouseOver(TableEvent<T> event);
	}
	
	public void addMouseOverListener(MouseOverListener<T> mouseOverListener) {
		iMouseOverListeners.add(mouseOverListener);
	}

	public static interface MouseOutListener<T> {
		public void onMouseOut(TableEvent<T> event);
	}
	
	public void addMouseOutListener(MouseOutListener<T> mouseOutListener) {
		iMouseOutListeners.add(mouseOutListener);
	}

	public static interface MouseClickListener<T> {
		public void onMouseClick(TableEvent<T> event);
	}
	
	public void addMouseClickListener(MouseClickListener<T> mouseClickListener) {
		iMouseClickListeners.add(mouseClickListener);
	}
	
	public static interface MouseDoubleClickListener<T> {
		public void onMouseDoubleClick(TableEvent<T> event);
	}
	
	public void addMouseDoubleClickListener(MouseDoubleClickListener<T> mouseDoubleClickListener) {
		iMouseDoubleClickListeners.add(mouseDoubleClickListener);
	}

	public static class DataChangedEvent<T> {
		private T iData;
		private int iRow;
		public DataChangedEvent(T data, int row) {
			iData = data; iRow = row;
		}
		public T getData() { return iData; }
		public int getRow() { return iRow; }
	}
	
	public interface DataChangedListener<T> {
		public void onDataInserted(DataChangedEvent<T> event);
		public void onDataRemoved(DataChangedEvent<T> event);
		public void onDataMoved(List<DataChangedEvent<T>> events);
		public void onDataSorted(List<DataChangedEvent<T>> events);
	}
	
	public void addDataChangedListener(DataChangedListener<T> listener) {
		iDataChangedListeners.add(listener);
	}
	
	public UniTimeTableHeader getHeader(String name) {
		if (getRowCount() <= 0) return null;
		if (name == null) {
			Widget w = getWidget(0, 0);
			return (w != null && w instanceof UniTimeTableHeader ? (UniTimeTableHeader)w : null);
		}
		for (int i = 0; i < getCellCount(0); i++) {
			Widget w = getWidget(0, i);
			if (w != null && w instanceof UniTimeTableHeader) {
				UniTimeTableHeader h = (UniTimeTableHeader)w;
				if (h.getHTML().equals(name)) return h;
			}
		}
		UniTimeNotifications.warn("Header named " + name + " does not exist!");
		return null;
	}
	
	public UniTimeTableHeader getHeader(int col) {
		if (getRowCount() <= 0 || getCellCount(0) <= col) return null;
		for (int c = 0; c < getCellCount(0); c++) {
			col -= getFlexCellFormatter().getColSpan(0, c);
			if (col < 0) {
				Widget w = getWidget(0, c);
				return (w != null && w instanceof UniTimeTableHeader ? (UniTimeTableHeader)w : null);
			}
		}
		return null;
	}
	
	public static interface HasFocus {
		public boolean focus();
	}

	public static interface HasHint {
		public String getHint();
	}
	
	public static interface HasColSpan {
		public int getColSpan();
	}
	
	public static interface HasCellAlignment {
		public HorizontalAlignmentConstant getCellAlignment();
	}
	
	public static interface HasVerticalCellAlignment {
		public VerticalAlignmentConstant getVerticalCellAlignment();
	}
	
	public static interface HasStyleName {
		public String getStyleName();
	}
	
	public static interface HasAdditionalStyleNames {
		public List<String> getAdditionalStyleNames();
	}
	
	public static interface HasDataUpdate {
		public void update();
	}
	
	public static interface HasColumn {
		public int getColumn();
		public void setColumn(int column);
	}

	
	public static interface HintProvider<T> {
		Widget getHint(TableEvent<T> event);
	}
	
	public void setHintProvider(HintProvider<T> hintProvider) {
		iHintProvider = hintProvider;
	}
	
	public static class NumberCell extends HTML implements HasCellAlignment {
		public NumberCell(String text) {
			super(text, false);
		}
		
		public NumberCell(int text) {
			super(String.valueOf(text), false);
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	public static class CenterredCell extends HTML implements HasCellAlignment {
		public CenterredCell(String text) {
			super(text, false);
		}
		
		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_CENTER;
		}
	}
	
	public static class CheckBoxCell extends AriaCheckBox implements HasCellAlignment {
		
		public CheckBoxCell() {
			super();
			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			});
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_CENTER;
		}

	}
	
	public void setBackGroundColor(int row, String color) {
		String style = getRowFormatter().getStyleName(row);
		if (style != null && !style.isEmpty()) {
			if (color == null || color.isEmpty())
				iLastHoverBackgroundColor.remove(row);
			else
				iLastHoverBackgroundColor.put(row, color);
		} else {
			if (color == null || color.isEmpty())
				getRowFormatter().getElement(row).getStyle().clearBackgroundColor();
			else
				getRowFormatter().getElement(row).getStyle().setBackgroundColor(color);
		}
	}
	
	public static interface HasRefresh {
		public void refresh();
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
	
	public int getRowForWidget(Widget w) {
		for (Element td = w.getElement(); td != null; td = DOM.getParent(td)) {
			if (td.getPropertyString("tagName").equalsIgnoreCase("td")) {
				Element tr = DOM.getParent(td);
				Element body = DOM.getParent(tr);
				if (body == getBodyElement())
					return DOM.getChildIndex(body, tr);
			}
			if (td == getBodyElement()) { return -1; }
		}
		return -1;
	}
	
	public void setEmptyMessage(String message) {
		addRow(null, new EmptyRow(message));
	}
	
	public class EmptyRow extends P implements UniTimeTable.HasColSpan {
		public EmptyRow(String message) {
			super("empty-row");
			setText(message);
		}
		@Override
		public int getColSpan() {
			return getRowCount() == 0 ? 1 : getCellCount(0);
		}
	}

	@Override
	public boolean isEnabled() {
		return iEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		iEnabled = enabled;
	}
}
