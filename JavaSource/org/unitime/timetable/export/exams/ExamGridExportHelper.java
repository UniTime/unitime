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
package org.unitime.timetable.export.exams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.Exporter.TableAware;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.export.PDFPrinter.F;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridCell;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridModel;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridPeriod;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridTable;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.resources.GwtConstants;

public class ExamGridExportHelper {
	protected static GwtConstants CONST = Localization.create(GwtConstants.class);
	private FilterInterface iFilter;
	private List<ExamGridPeriod> iPeriods;
	private int iDispMode = 0;
	private int iRow = 0, iCol = 0;
	private static int sHeaderRepeat = Integer.MAX_VALUE;
	private List<ExamGridPeriod> iDates;
	private List<ExamGridPeriod> iTimes;
	private List<ExamGridPeriod> iWeekTimes;
	
	private List<TableRow> iTable = new ArrayList<TableRow>();
	
	public ExamGridExportHelper(FilterInterface filter, ExamGridTable table) {
		iFilter = filter;
		iPeriods = new ArrayList<ExamGridPeriod>();
		iDates = new ArrayList<ExamGridPeriod>();
		iTimes = new ArrayList<ExamGridPeriod>();
		iWeekTimes = new ArrayList<ExamGridPeriod>();
		Set<String> dates = new HashSet<String>();
		Set<String> times = new HashSet<String>();
		Set<String> weekTimes = new HashSet<String>();
		for (ExamGridPeriod p: table.getPeriods())
			if (include(p)) {
				iPeriods.add(p);
				if (dates.add(p.getDateLabel())) iDates.add(p);
				if (times.add(p.getTimeLabel())) iTimes.add(p);
				if (weekTimes.add(p.getWeekLabel() + "\n" + p.getTimeLabel())) iWeekTimes.add(p);
			}
		iDispMode = Integer.valueOf(filter.getParameterValue("dispMode", "0"));
		Set<Integer> indexes = new TreeSet<Integer>();
		if (iDispMode <= 1) { // InRow
			indexes.add(0);
		} else if (iDispMode <= 3) { // PerDay
			for (ExamGridPeriod p: iPeriods)
				indexes.add(p.getDate());
		} else { // PerWeek
			for (int i = 0; i < 7; i++)
				indexes.add(i);
		}
		for (ExamGridModel model: table.getModels()) {
			addHeader(model);
			for (int idx: indexes)
				addRow(model, idx);
		}
	}
	
	public int getRowCount() {
		return iTable.size();
	}
	
	public String getTableName(int row) {
		if (row < 0 || row >= iTable.size()) return null;
		return iTable.get(row).getTableName();
	}
	
	public void setTableName(int row, String tableName) {
		while (iTable.size() <= row) iTable.add(new TableRow());
		TableRow r = iTable.get(row);
		r.setTableName(tableName);
	}
	
	public int getCellCount(int row) {
		if (row < 0 || row >= iTable.size()) return 0;
		return iTable.get(row).getCellCount();
	}
	
	public int getMaxRowSpan(int row) {
		if (row < 0 || row >= iTable.size()) return 0;
		TableRow r = iTable.get(row);
		int ret = 1;
		for (int col = 0; col < r.getCellCount(); col++) {
			TableCell c = r.get(col);
			if (c.getNbrItems() > ret) ret = c.getNbrItems();
		}
		return ret;
	}
	
	public TableCell getElement(int row, int col) {
		while (iTable.size() <= row) iTable.add(new TableRow());
		TableRow r = iTable.get(row);
		while (r.getCellCount() <= col) r.getCells().add(new TableCell());
		return r.get(col);
	}
	
	public A getA(int row, int col, int index, TableAware helper) {
		TableCell c = getElement(row, col);
		if (index < c.getNbrItems())
			return c.toA(helper, index);
		A a = new A(" ");
		a.setColSpan(0);
		a.set(F.HAIR_BORDER);
		if (c.hasClassName("ver-br-left"))
			a.set(F.LEFT_BORDER);
		return a;
	}

	
	protected void setWidget(int row, int col, Widget widget) {
		getElement(row, col).setWidget(widget);
	}
	
	protected ExamGridExportHelper getCellFormatter() {
		return this;
	}
	
	protected boolean include(ExamGridPeriod period) {
		String date = iFilter.getParameterValue("date");
		if (date != null && !date.isEmpty()) {
			int i = date.indexOf('|');
			if (i >= 0) {
				int d1 = Integer.valueOf(date.substring(0, i));
				int d2 = Integer.valueOf(date.substring(i + 1));
				return d1 <= period.getDate() && period.getDate() <= d2;
			} else {
				if (period.getDate() != Integer.valueOf(date)) return false;
			}
		}
		String start = iFilter.getParameterValue("start");
		if (start != null && !start.isEmpty() && period.getStart() < Integer.valueOf(start)) return false;
		String end = iFilter.getParameterValue("end");
		if (end != null && !end.isEmpty() && period.getEnd() > Integer.valueOf(end)) return false;
		return true;
	}
	
	protected int getTableWidth(ExamGridTable table) {
		if (iDispMode == 0) {
			int ret = 0;
			for (int i = 0; i < iPeriods.size(); i++) {
				if ((ret % sHeaderRepeat) == 0) ret++;
				ret++;
			}
			return ret;
		} else if (iDispMode == 1) {
			int ret = 0;
			for (int i = 0; i < table.getModels().size(); i++) {
				if ((ret % sHeaderRepeat) == 0) ret++;
				ret++;
			}
			return ret;
		} else if (iDispMode == 2) {
			return 1 + iTimes.size();
		} else if (iDispMode == 3) {
			return 1 + iDates.size();
		} else if (iDispMode == 4) {
			return 1 + iWeekTimes.size();
		} else {
			Set<Integer> dayOfWeeks = new HashSet<Integer>();
			for (ExamGridPeriod p: iPeriods) {
				dayOfWeeks.add(p.getDayOfWeek());
			}
			return 1 + dayOfWeeks.size();
		}
	}
	
	protected void addHeader(ExamGridModel model) {
		if (iDispMode == 0) { // InRowHorizontal
			if (iRow % sHeaderRepeat != 0) return; // only the first time
			int row = iRow ++;
			int col = iCol;
			setWidget(row, col, new Header(row, col));
			if (row > 0)
				getCellFormatter().getElement(row, col).addClassName("hor-br-top");
			col++;
			Integer prev = null;
			String date = iFilter.getParameterValue("date");
			boolean showDate = (date == null || date.isEmpty() || date.contains("|"));
			for (ExamGridPeriod p: iPeriods) {
				Header h = new Header(row, col, p, false, showDate, true);
				setWidget(row, col, h);
				if (prev == null || !prev.equals(p.getDate()))
					getCellFormatter().getElement(row, col).addClassName("ver-br-left");
				prev = p.getDate();
				if (row > 0)
					getCellFormatter().getElement(row, col).addClassName("hor-br-top");
				col++;
			}
		} else if (iDispMode == 1) { // InRowVertical
			if (iCol % sHeaderRepeat != 0) return; // only the first time
			int col = iCol ++;
			int row = iRow;
			setWidget(row, col, new Header(row, col));
			if (col > 0)
				getCellFormatter().getElement(row, col).addClassName("ver-br-left");
			row++;
			Integer prev = null;
			String date = iFilter.getParameterValue("date");
			boolean showDate = (date == null || date.isEmpty() || date.contains("|"));
			for (ExamGridPeriod p: iPeriods) {
				Header h = new Header(row, col, p, false, showDate, true);
				setWidget(row, col, h);
				if (prev == null || !prev.equals(p.getDate()))
					getCellFormatter().getElement(row, col).addClassName("hor-br-top");
				prev = p.getDate();
				if (col > 0)
					getCellFormatter().getElement(row, col).addClassName("ver-br-left");
				row++;
			}
		} else if (iDispMode == 2) { // PerDayHorizontal
			int row = iRow ++;
			int col = iCol;
			setWidget(row, col, new Header(row, col, model));
			setTableName(row, model.getName());
			if (row > 0)
				getCellFormatter().getElement(row, col).addClassName("hor-br-top");
			col++;
			Integer prev = null;
			for (ExamGridPeriod p: iTimes) {
				Header h = new Header(row, col, p, false, false, true);
				setWidget(row, col, h);
				if (row > 0)
					getCellFormatter().getElement(row, col).addClassName("hor-br-top");
				if (prev == null)
					getCellFormatter().getElement(row, col).addClassName("ver-br-left");
				prev = p.getStart();
				col++;
			}
		} else if (iDispMode == 3) { // PerDayVertical
			if (iCol > 0) {
				iRow += 1 + iTimes.size();
				iCol = 0;
			}
			int row = iRow;
			int col = iCol ++;
			setWidget(row, col, new Header(row, col, model));
			setTableName(row, model.getName());
			if (iRow > 0)
				getCellFormatter().getElement(row, col).addClassName("hor-br-top");
			row++;
			for (ExamGridPeriod p: iTimes) {
				Header h = new Header(row, col, p, false, false, true);
				setWidget(row, col, h);
				if (row == iRow + 1)
					getCellFormatter().getElement(row, col).addClassName("hor-br-top-thin");
				row++;
			}
		} else if (iDispMode == 4) { // PerWeekHorizontal
			int row = iRow ++;
			int col = iCol;
			setWidget(row, col, new Header(row, col, model));
			setTableName(row, model.getName());
			if (iRow > 0)
				getCellFormatter().getElement(row, col).addClassName("hor-br-top");
			col++;
			Integer prev = null;
			for (ExamGridPeriod p: iWeekTimes) {
				Header h = new Header(row, col, p, true, false, true);
				setWidget(row, col, h);
				if (prev == null || !prev.equals(p.getWeek()))
					getCellFormatter().getElement(row, col).addClassName("ver-br-left");
				prev = p.getWeek();
				if (iRow > 0)
					getCellFormatter().getElement(row, col).addClassName("hor-br-top");
				col++;
			}
		} else if (iDispMode == 5) { // PerWeekVertical
			if (iCol > 0) {
				iRow += 1 + iWeekTimes.size();
				iCol = 0;
			}
			int col = iCol ++;
			int row = iRow;
			setWidget(row, col, new Header(row, col, model));
			setTableName(row, model.getName());
			if (iRow > 0)
				getCellFormatter().getElement(row, col).addClassName("hor-br-top");
			row++;
			Integer prev = null;
			for (ExamGridPeriod p: iWeekTimes) {
				Header h = new Header(row, col, p, true, false, true);
				setWidget(row, col, h);
				if (prev == null || !prev.equals(p.getWeek()))
					getCellFormatter().getElement(row, col).addClassName("hor-br-top-thin");
				prev = p.getWeek();
				row++;
			}
		}
	}
	
	protected void addRow(ExamGridModel model, int index) {
		List<ExamGridPeriod> periods = null;
		Integer firstIndex = null;
		if (iDispMode <= 1) {
			periods = iPeriods;
		} else if (iDispMode <= 3) {
			periods = new ArrayList<ExamGridPeriod>();
			boolean hasPeriod = false;
			t: for (ExamGridPeriod t: iTimes) {
				for (ExamGridPeriod p: iPeriods) {
					if (firstIndex == null || firstIndex > p.getDate())
						firstIndex = p.getDate();
					if (p.getDate() == index && t.getStart().equals(p.getStart())) {
						periods.add(p);
						hasPeriod = true;
						continue t;
					}
				}
				ExamGridPeriod d = new ExamGridPeriod();
				d.setStart(t.getStart());
				d.setDate(index);
				periods.add(d);
			}
			if (!hasPeriod) return;
		} else {
			boolean hasPeriod = false;
			periods = new ArrayList<ExamGridPeriod>();
			t: for (ExamGridPeriod t: iWeekTimes) {
				for (ExamGridPeriod p: iPeriods) {
					if (firstIndex == null || firstIndex > p.getDayOfWeek())
						firstIndex = p.getDayOfWeek();
					if (p.getDayOfWeek() == index && t.getStart().equals(p.getStart()) && t.getWeek().equals(p.getWeek())) {
						periods.add(p);
						hasPeriod = true;
						continue t;
					}
				}
				ExamGridPeriod d = new ExamGridPeriod();
				d.setWeek(t.getWeek());
				d.setStart(t.getStart());
				d.setDayOfWeek(index);
				periods.add(d);
			}
			if (!hasPeriod) return;
		}
		
		if (iDispMode == 0 || iDispMode == 2 || iDispMode == 4) { // Horizontal
			if (iDispMode == 0) {
				setWidget(iRow, iCol, new Header(iRow, iCol, model));
				if ((iRow % sHeaderRepeat) == 1)
					getCellFormatter().getElement(iRow, iCol).addClassName("hor-br-top");
			} else if (iDispMode == 2) {
				setWidget(iRow, iCol, new Header(iRow, iCol, periods.get(0), false, true, false));
				if (firstIndex == index)
					getCellFormatter().getElement(iRow, iCol).addClassName("hor-br-top-thin");
			} else {
				setWidget(iRow, iCol, new Header(iRow, iCol, CONST.longDays()[index]));
				if (firstIndex == index)
					getCellFormatter().getElement(iRow, iCol).addClassName("hor-br-top-thin");
			}
			Integer prev = null;
			int col = iCol + 1;
			for (ExamGridPeriod p: periods) {
				List<ExamGridCell> cells = model.getCells(p.getId());
				if (cells == null || cells.isEmpty()) {
					Empty e = new Empty(model, p);
					setWidget(iRow, col, e);
				} else {
					MultiCell mc = new MultiCell();
					for (int i = 0; i < cells.size(); i++) {
						ExamGridCell cell = cells.get(i);
						Cell c = new Cell(model, p, cell, i, cells.size());
						mc.add(c);
					}
					setWidget(iRow, col, mc);
				}
				if (iDispMode == 0) {
					if ((iRow % sHeaderRepeat) == 1)
						getCellFormatter().getElement(iRow, col).addClassName("hor-br-top");
					if (prev == null || !prev.equals(p.getDate()))
						getCellFormatter().getElement(iRow, col).addClassName("ver-br-left");
					prev = p.getDate();
				} else if (iDispMode == 2) {
					if (firstIndex == index)
						getCellFormatter().getElement(iRow, col).addClassName("hor-br-top-thin");
					if (prev == null)
						getCellFormatter().getElement(iRow, col).addClassName("ver-br-left");
					prev = p.getStart();
				} else if (iDispMode == 4) {
					if (firstIndex == index)
						getCellFormatter().getElement(iRow, col).addClassName("hor-br-top-thin");
					if (prev == null || !prev.equals(p.getWeek()))
						getCellFormatter().getElement(iRow, col).addClassName("ver-br-left");
					prev = p.getWeek();
				}

				col++;
			}
			iRow ++;
		} else if (iDispMode == 1 || iDispMode == 3 || iDispMode == 5) { // Vertical
			if (iDispMode == 1) {
				setWidget(iRow, iCol, new Header(iRow, iCol, model));
			} else if (iDispMode == 3) {
				setWidget(iRow, iCol, new Header(iRow, iCol, periods.get(0), false, true, false));
				if (iRow > 0)
					getCellFormatter().getElement(iRow, iCol).addClassName("hor-br-top");
			} else {
				setWidget(iRow, iCol, new Header(iRow, iCol, CONST.longDays()[index]));
				if (iRow > 0)
					getCellFormatter().getElement(iRow, iCol).addClassName("hor-br-top");
			}
			if (iCol == 1 || (iDispMode == 1 && (iCol % sHeaderRepeat) == 1))
				getCellFormatter().getElement(iRow, iCol).addClassName("ver-br-left");
			Integer prev = null;
			int row = iRow + 1;
			for (ExamGridPeriod p: periods) {
				List<ExamGridCell> cells = model.getCells(p.getId());
				if (cells == null || cells.isEmpty()) {
					Empty e = new Empty(model, p);
					setWidget(row, iCol, e);
				} else {
					MultiCell mc = new MultiCell();
					for (int i = 0; i < cells.size(); i++) {
						ExamGridCell cell = cells.get(i);
						Cell c = new Cell(model, p, cell, i, cells.size());
						mc.add(c);
					}
					setWidget(row, iCol, mc);
				}
				if (iDispMode == 1) {
					if (prev == null || !prev.equals(p.getDate()))
						getCellFormatter().getElement(row, iCol).addClassName("hor-br-top");
					prev = p.getDate();
				} else if (iDispMode == 3) {
					if (prev == null)
						getCellFormatter().getElement(row, iCol).addClassName("hor-br-top-thin");
					prev = p.getStart();
				} else if (iDispMode == 5) {
					if (prev == null || !prev.equals(p.getWeek()))
						getCellFormatter().getElement(row, iCol).addClassName("hor-br-top-thin");
					prev = p.getWeek();
				}
				if (iCol == 1 || (iDispMode == 1 && (iCol % sHeaderRepeat) == 1))
					getCellFormatter().getElement(row, iCol).addClassName("ver-br-left");
				row++;
			}
			iCol ++;
		}
	}
	
	protected class Header extends Widget {
		protected Header(int row, int col) {
			addStyleName("grid-header");
			String date = iFilter.getParameterValue("date");
			if (date != null && !date.isEmpty()) {
				if (date.contains("|"))
					setText(iPeriods.get(0).getWeekLabel());
				else
					setText(iPeriods.get(0).getDateLabel());
			}
		}
		protected Header(int row, int col, ExamGridPeriod period, boolean showWeek, boolean showDate, boolean showTime) {
			addStyleName("grid-header");
			if (showWeek) {
				if (showDate) {
					if (showTime)
						setText(period.getWeekLabel() + "\n" + period.getDateLabel() + "\n" + period.getTimeLabel());		
					else
						setText(period.getWeekLabel() + "\n" + period.getDateLabel());
				} else if (showTime)
					setText(period.getWeekLabel() + "\n" + period.getTimeLabel());
			} else {
				if (showDate) {
					if (showTime)
						setText(period.getDateLabel() + "\n" + period.getTimeLabel());		
					else
						setText(period.getDateLabel());
				} else if (showTime)
					setText(period.getTimeLabel());
			}
		}
		protected Header(int row, int col, ExamGridModel model) {
			addStyleName("grid-header");
			if (model.hasSize())
				setText(model.getName() + " (" + model.getSize() + ")");
			else
				setText(model.getName());				
		}
		
		protected Header(int row, int col, String text) {
			addStyleName("grid-header");
			setText(text);
		}
		
	}
	
	protected class MultiCell extends Widget {
		List<Cell> iCells = new ArrayList<Cell>();
		
		protected MultiCell() { addStyleName("multi-cell"); }
		
		protected void add(Cell cell) { iCells.add(cell); }
		public List<Cell> getCells() { return iCells; }
		
		@Override
		public String toString() {
			String ret = "";
			for (Cell cell: getCells()) {
				if (!ret.isEmpty())
					ret += "\n------------\n";
				ret += cell.toString();
			}
			return ret;
		}
		
		@Override
		public A toA(TableAware helper) {
			A a = new A();
			if (getBackgroundColor() != null)
				a.setBackground(getBackgroundColor());
			for (Cell cell: getCells()) {
				A b = cell.toA(helper);
				a.add(b);
			}
			return a;
		}
		
		public int getNbrItems() { return getCells().size(); }
		public A toA(TableAware helper, int index) {
			return getCells().get(index).toA(helper);
		}
	}
	
	protected class Empty extends Widget {
		protected Empty(ExamGridModel model, ExamGridPeriod period) {
			if (period == null || period.getId() == null)
				addStyleName("blank-cell");
			else {
				addStyleName("empty-cell");
				String bgColor = model.getPeriodBgColor(period.getId());
				if (bgColor != null)
					setBackgroundColor(bgColor);
				else if (period.hasBgColor())
					setBackgroundColor(period.getBgColor());
			}
		}
	}
	
	protected class Cell extends Widget {
		protected ExamGridCell iCell;
		
		protected Cell(ExamGridModel model, ExamGridPeriod period, ExamGridCell cell, int idx, int span) {
			iCell = cell;
			setText(cell.toString());
			addStyleName("cell");
			if (cell.hasBgColor()) {
				setBackgroundColor(cell.getBgColor());
				cell.addStyle("background-color:" + cell.getBgColor());
			}
		}
		
		public ExamGridCell getExamGridCell() { return iCell; }
				
		@Override
		public A toA(TableAware helper) {
			A a = helper.toA(iCell);
			if (iCell.hasBgColor())
				a.setBackground(iCell.getBgColor());
			return a;
		}
	}
	
	protected class TableRow {
		private String iTableName;
		
		private List<TableCell> iCells = new ArrayList<TableCell>();
		
		public List<TableCell> getCells() { return iCells; }
		public int getCellCount() { return iCells.size(); }
		public TableCell get(int col) { return iCells.get(col); }
		
		public String getTableName() { return iTableName; }
		public void setTableName(String tableName) { iTableName = tableName; }
	}
	
	public class TableCell {
		private Widget iWidget = null;
		private Set<String> iStyleName = new TreeSet<String>();
		
		public void setWidget(Widget widget) { iWidget = widget; }
		public Widget getWidget() { return iWidget; }
		
		public void addClassName(String name) { iStyleName.add(name); }
		public boolean hasClassName(String name) { return iStyleName.contains(name); }
		
		@Override
		public String toString() {
			return iWidget == null ? "" : iWidget.toString();
		}
		
		public A toA(TableAware helper) {
			A a = (iWidget == null ? new A("") : iWidget.toA(helper));
			a.center();
			a.set(F.HAIR_BORDER);
			if (hasClassName("hor-br-top-thin"))
				a.set(F.TOP_BORDER);
			if (hasClassName("ver-br-left"))
				a.set(F.LEFT_BORDER);
			return a;
		}
		
		public A toA(TableAware helper, int index) {
			A a = (iWidget == null ? new A("") : iWidget.toA(helper, index));
			a.center();
			if (a.hasChunks())
				for (A b: a.getChunks())
					b.center();
			a.set(F.HAIR_BORDER);
			if (index == 0) {
				if (hasClassName("hor-br-top-thin"))
					a.set(F.TOP_BORDER);
			}
			if (hasClassName("ver-br-left"))
				a.set(F.LEFT_BORDER);
			return a;
		}
		
		public int getNbrItems() { return iWidget == null ? 1 : iWidget.getNbrItems(); }
	}
	
	public class Widget {
		private Set<String> iStyleName = new TreeSet<String>();
		private String iBgColor = null;
		private String iText = null;
		
		public void addStyleName(String name) { iStyleName.add(name); }
		public boolean hasStyleName(String name) { return iStyleName.contains(name); }
		
		public void setBackgroundColor(String color) { iBgColor = color; }
		public String getBackgroundColor() { return iBgColor; }
		
		public void setText(String text) { iText = text; }
		public String getText() { return iText; }
		
		public int getNbrItems() { return 1; }
		
		@Override
		public String toString() {
			return (iText == null ? "" : iText);
		}
		
		public A toA(TableAware helper) {
			String s = toString();
			A a = new A(s == null || s.isEmpty() ? " " : s);
			if (iBgColor != null) a.setBackground(iBgColor);
			if (hasStyleName("grid-header")) {
				a.bold();
				a.setBackground("#f1f3f9");
			}
			if (hasStyleName("blank-cell"))
				a.setBackground("#c8c8c8");
			return a;
		}
		
		public A toA(TableAware helper, int index) {
			return toA(helper);
		}
	}

}
