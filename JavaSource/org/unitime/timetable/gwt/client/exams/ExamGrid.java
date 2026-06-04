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
package org.unitime.timetable.gwt.client.exams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridCell;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridModel;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridPeriod;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridTable;
import org.unitime.timetable.gwt.client.tables.TableWidget.CellWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.FilterInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlexTable;

public class ExamGrid extends FlexTable {
	protected static GwtConstants CONST = GWT.create(GwtConstants.class);
	private FilterInterface iFilter;
	private List<ExamGridPeriod> iPeriods;
	private int iDispMode = 0;
	private int iRow = 0, iCol = 0;
	private static int sHeaderRepeat = 11;
	private List<ExamGridPeriod> iDates;
	private List<ExamGridPeriod> iTimes;
	private List<ExamGridPeriod> iWeekTimes;
	private int iCellMinWidth = 100;
	
	public ExamGrid(FilterInterface filter, ExamGridTable table) {
		addStyleName("unitime-ExamGrid");
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
		int w = (ToolBox.getClientWidth() - 20) / getTableWidth(table);
		if (w > 100)
			iCellMinWidth = w;
		if (iCellMinWidth > 250) iCellMinWidth = 250;
		for (ExamGridModel model: table.getModels()) {
			addHeader(model);
			for (int idx: indexes)
				addRow(model, idx);
		}
		if (getRowCount() > 0)
			for (int c = 0; c < getCellCount(0); c++)
				getCellFormatter().getElement(0, c).getStyle().setWidth(iCellMinWidth, Unit.PX);
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
	
	protected class Header extends P {
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
	
	protected class MultiCell extends P {
		protected MultiCell() {
			addStyleName("multi-cell");
		}
		
	}
	
	protected class Empty extends P {
		protected Empty(ExamGridModel model, ExamGridPeriod period) {
			if (period == null || period.getId() == null)
				addStyleName("blank-cell");
			else {
				addStyleName("empty-cell");
				String bgColor = model.getPeriodBgColor(period.getId());
				if (bgColor != null)
					getElement().getStyle().setBackgroundColor(bgColor);
				else if (period.hasBgColor())
					getElement().getStyle().setBackgroundColor(period.getBgColor());
			}
		}
	}
	
	protected class Cell extends CellWidget {
		protected Cell(ExamGridModel model, ExamGridPeriod period, ExamGridCell cell, int idx, int span) {
			super(cell);
			addStyleName("cell");
			if (cell.hasBgColor())
				getElement().getStyle().setBackgroundColor(cell.getBgColor());
			if (cell.hasUrl())
				getElement().setTabIndex(0);
		}
	}
	
	
}
