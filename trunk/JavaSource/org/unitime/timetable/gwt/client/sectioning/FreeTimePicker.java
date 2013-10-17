/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Vector;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;

/**
 * @author Tomas Muller
 */
public class FreeTimePicker extends Composite {
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);

	private CellSelectingGrid iGrid;
	
	private boolean[][] iSelected;
	private long[][] iLastSelectedTime;
	private long iTime = 0;
	
	private Vector<FreeTimeChangeHandler> iFreeTimeChangeHandlers = new Vector<FreeTimeChangeHandler>();
	
	public FreeTimePicker() {
		iGrid = new CellSelectingGrid(1 + CONSTANTS.freeTimeDays().length, CONSTANTS.freeTimePeriods().length);
		iGrid.setCellPadding(2);
		iGrid.setCellSpacing(0);
		iSelected = new boolean[CONSTANTS.freeTimeDays().length][CONSTANTS.freeTimePeriods().length - 1];
		iLastSelectedTime = new long[CONSTANTS.freeTimeDays().length][CONSTANTS.freeTimePeriods().length - 1];
		
		iGrid.setHTML(0, 0, "&nbsp;");
		iGrid.getCellFormatter().setStyleName(0, 0, "unitime-FreeTimePicker-TopLeftCorner");
		ToolBox.disableTextSelectInternal(iGrid.getCellFormatter().getElement(0, 0));
		
		for (int i=0; i<CONSTANTS.freeTimeDays().length; i++) {
			iGrid.setText(1 + i, 0, CONSTANTS.freeTimeDays()[i]);
			iGrid.getCellFormatter().setStyleName(1 + i, 0, "unitime-FreeTimePicker-VerticalHeader");
			ToolBox.disableTextSelectInternal(iGrid.getCellFormatter().getElement(1 + i, 0));
		}
		for (int i=0; i<CONSTANTS.freeTimePeriods().length - 1; i++) {
			iGrid.setText(0, 1 + i, CONSTANTS.freeTimePeriods()[i]);
			iGrid.getCellFormatter().setStyleName(0, 1 + i, "unitime-FreeTimePicker-HorizontalHeader");
			ToolBox.disableTextSelectInternal(iGrid.getCellFormatter().getElement(0, 1 + i));
		}
		for (int d=0; d<CONSTANTS.freeTimeDays().length; d++) {
			for (int p=0; p<CONSTANTS.freeTimePeriods().length - 1; p++) {
				iSelected[d][p] = false;
				iLastSelectedTime[d][p] = 0;
				iGrid.setHTML(1 + d, 1 + p, "&nbsp;");
				iGrid.getCellFormatter().setStyleName(1 + d, 1 + p, "unitime-FreeTimePicker-Slot");
				ToolBox.disableTextSelectInternal(td(d,p));
			}
		}
		update();
		initWidget(iGrid);
	}
			
	public boolean isSelected(int day, int period) {
		return iSelected[day][period];
	}
	
	private Element td(int day, int period) {
		return iGrid.getCellFormatter().getElement(day + 1, period + 1);
	}
		
	private String bg(int day, int period) {
		boolean mwf = (day % 2 == 0);
		boolean odd = (mwf ? period % 4 <= 1 : period % 6 > 2);
		boolean block = false;
		if (iDownDay>=0 && iOverDay>=0) {
			int d0 = (iDownDay < iOverDay ? iDownDay : iOverDay);
			int d1 = (iDownDay < iOverDay ? iOverDay : iDownDay);
			int p0 = (iDownPeriod < iOverPeriod ? iDownPeriod : iOverPeriod);
			int p1 = (iDownPeriod < iOverPeriod ? iOverPeriod : iDownPeriod);
			boolean mwfDown = (iDownDay % 2 == 0);
			if (d0 <= day && day <= d1 && p0 <= period && period <= p1 && (d1 - d0 <= 1 || mwfDown == mwf))
				block = true;
		}
		int r = 255;
		int g = 255;
		int b = 255;
		if (isSelected(day, period)) {
			b -= 55;
			g -= 55;
		} else if (mwf) {
			r -= (odd?15:30);
			g -= (odd?15:30);
		} else {
			r -= (odd?15:30);
			b -= (odd?15:30);
		}
		if (block) {
			r -= 25;
			g -= 25;
			b -= 25;
		}
		return "#" + (r<16?"0":"") + Integer.toHexString(r) + (g<16?"0":"") + Integer.toHexString(g) + (b<16?"0":"") + Integer.toHexString(b);
	}
	
	public int toSlot(int period) {
		return 6 * (period + 15);
	}
	
	public int toPeriod(int slot) {
		int p = slot / 6 - 15;
		if (p < 0) return 0;
		if (p >= CONSTANTS.freeTimePeriods().length) return CONSTANTS.freeTimePeriods().length - 1;
		return p;
	}
	
	private CourseRequestInterface.FreeTime generateOneFreeTime(boolean[][] s) {
		for (int p0=0; p0<CONSTANTS.freeTimePeriods().length - 1; p0++) {
			for (int d0=0; d0<CONSTANTS.freeTimeDays().length; d0++) {
				if (s[d0][p0]) {
					int lastP1 = p0;
					p1: for (int p1=p0+1; p1<CONSTANTS.freeTimePeriods().length - 1; p1++) {
						for (int d=0; d<CONSTANTS.freeTimeDays().length; d++) {
							if (s[d][p0] && !s[d][p1]) break p1;
						}
						lastP1 = p1;
						for (int d=0; d<CONSTANTS.freeTimeDays().length; d++)
							if (s[d][p0]) s[d][p1] = false;
					}
					CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
					for (int d=0; d<CONSTANTS.freeTimeDays().length; d++)
						if (s[d][p0]) { ft.addDay(d); s[d][p0] = false; }
					ft.setStart(toSlot(p0));
					ft.setLength(toSlot(lastP1 + 1) - toSlot(p0));
					return ft;
				}
			}
		}
		return null;
	}
	
	public ArrayList<CourseRequestInterface.FreeTime> getFreeTime() {
		boolean[][] s = new boolean[CONSTANTS.freeTimeDays().length][CONSTANTS.freeTimePeriods().length - 1];
		for (int d=0; d<CONSTANTS.freeTimeDays().length; d++) {
			for (int p=0; p<CONSTANTS.freeTimePeriods().length - 1; p++)
				s[d][p] = iSelected[d][p];
		}
		ArrayList<CourseRequestInterface.FreeTime> ret = new ArrayList<CourseRequestInterface.FreeTime>();
		CourseRequestInterface.FreeTime ft = null;
		while ((ft = generateOneFreeTime(s)) != null)
			ret.add(ft);
		return ret;
	}

	public void clearFreeTime() {
		for (int d=0; d<CONSTANTS.freeTimeDays().length; d++)
			for (int p=0; p<CONSTANTS.freeTimePeriods().length - 1; p++) {
				iSelected[d][p] = false;
				td(d, p).getStyle().setBackgroundColor(bg(d, p));
				iGrid.setHTML(1 + d, 1 + p, "&nbsp;");
			}
	}
	
	public void setFreeTime(ArrayList<CourseRequestInterface.FreeTime> freeTimes, boolean clearFirst) {
		if (clearFirst) clearFreeTime();
		for (CourseRequestInterface.FreeTime f: freeTimes) {
			for (int day: f.getDays()) {
				if (day < CONSTANTS.freeTimeDays().length) {
					for (int p = toPeriod(f.getStart()); p < toPeriod(f.getStart() + f.getLength()); p++)
						iSelected[day][p] = true;
				}
			}
		}
		generatePriorities();
		update();
	}
	
	private boolean generateOnePriority(boolean[][] s, int priority) {
		for (int p0=0; p0<CONSTANTS.freeTimePeriods().length - 1; p0++)
			for (int d0=0; d0<CONSTANTS.freeTimeDays().length; d0++) {
				if (s[d0][p0]) {
					boolean mwf0 = (d0 % 2 == 0);
					boolean odd0 = (mwf0 ? p0 % 4 <= 1 : p0 % 6 > 2);
					int first = 0;
					for (int i=0; i<CONSTANTS.freeTimeOneDay150().length; i++)
						if (Integer.parseInt(CONSTANTS.freeTimeOneDay150()[i]) <= p0) first = Integer.parseInt(CONSTANTS.freeTimeOneDay150()[i]);
					if (mwf0) {
						boolean hasM = false, hasF = false;
						boolean hasS = false, hasT = false;
						boolean allTheSame = true;
						for (int p1=first; p1<Math.min(first + 6, CONSTANTS.freeTimePeriods().length - 1); p1++) {
							if (s[0][p1]) hasM = true;
							if (s[4][p1]) hasF = true;
							if (p1 - first >= 2 && p1 - first < 4 && (s[0][p1] || s[2][p1] || s[4][p1])) hasS = true;
							if (p1 - first >= 4 && (s[0][p1] || s[2][p1] || s[4][p1])) hasT = true;
							if (d0!=0 && s[0][p1]) allTheSame = false;
							if (d0!=2 && s[2][p1]) allTheSame = false;
							if (d0!=4 && s[4][p1]) allTheSame = false;
						}
						if (((!hasM || !hasT || !hasF) && hasS && hasT) ||
							(allTheSame && (hasS || hasT))) {
							for (int p1=first; p1<Math.min(first + 6, CONSTANTS.freeTimePeriods().length - 1); p1++) {
								if (s[d0][p1]) {
									iGrid.setText(1 + d0, 1 + p1, String.valueOf(priority));
									s[d0][p1] = false;
								}
							}
							return true;
						}
					} else {
						boolean allTheSame = true;
						boolean hasT = false;
						for (int p1=first; p1<Math.min(first + 6, CONSTANTS.freeTimePeriods().length - 1); p1++) {
							if (p1 - first >= 4 && (s[1][p1] || s[3][p1])) hasT = true;
							if (d0!=1 && s[1][p1]) allTheSame = false;
							if (d0!=3 && s[3][p1]) allTheSame = false;
						}
						if (allTheSame || (hasT && 3 * (first / 3) != first)) {
							for (int p1=first; p1<Math.min(first + 6, CONSTANTS.freeTimePeriods().length - 1); p1++) {
								if (s[d0][p1]) {
									iGrid.setText(1 + d0, 1 + p1, String.valueOf(priority));
									s[d0][p1] = false;
								}
							}
							return true;
						}
					}
					p1: for (int p1=p0; p1<CONSTANTS.freeTimePeriods().length - 1; p1++)
						for (int d1=0; d1<CONSTANTS.freeTimeDays().length; d1++) {
							boolean mwf1 = (d1 % 2 == 0);
							boolean odd1 = (mwf1 ? p1 % 4 <= 1 : p1 % 6 > 2);
							if (mwf0 == mwf1 && odd0 != odd1) break p1;
							if (s[d1][p1]) {
								if (mwf0 == mwf1 && odd0 == odd1) {
									iGrid.setText(1 + d1, 1 + p1, String.valueOf(priority));
									s[d1][p1] = false;
								}
							}
						}
					return true;
				}
			}
		return false;
	}

	public void generatePriorities() {
		boolean[][] s = new boolean[CONSTANTS.freeTimeDays().length][CONSTANTS.freeTimePeriods().length - 1];
		for (int d=0; d<CONSTANTS.freeTimeDays().length; d++)
			for (int p=0; p<CONSTANTS.freeTimePeriods().length - 1; p++) {
				s[d][p] = iSelected[d][p];
				if (!iSelected[d][p]) iGrid.setHTML(1 + d, 1 + p, "&nbsp;");
			}
		int priority = 1;
		while (generateOnePriority(s, priority)) priority++;
	}

	public void update() {
		for (int d=0; d<CONSTANTS.freeTimeDays().length; d++) {
			for (int p=0; p<CONSTANTS.freeTimePeriods().length - 1; p++) {
				td(d, p).getStyle().setBackgroundColor(bg(d, p));
			}
		}
	}
	
	private int iDownDay = -1, iDownPeriod = -1;
	private int iOverDay= -1, iOverPeriod = -1;
	
	private void processMouseEvent(int eventType, int day, int period) {
	    switch (eventType) {
		case Event.ONMOUSEDOWN:
			iDownDay = day; iDownPeriod = period; iOverDay = day; iOverPeriod = period;
			break;
		case Event.ONMOUSEUP:
			if (iDownDay>=0 && iOverDay>=0) {
				int d0 = (iDownDay < iOverDay ? iDownDay : iOverDay);
				int d1 = (iDownDay < iOverDay ? iOverDay : iDownDay);
				int p0 = (iDownPeriod < iOverPeriod ? iDownPeriod : iOverPeriod);
				int p1 = (iDownPeriod < iOverPeriod ? iOverPeriod : iDownPeriod);
				boolean allSelected = true;
				boolean mwfDown = (iDownDay % 2 == 0);
				all: for (int d=d0; d<=d1; d++) {
					if (d1 - d0 <= 0 || (d % 2 == 0) == mwfDown) {
						for (int p=p0; p<=p1; p++)
							if (!iSelected[d][p]) {
								allSelected = false; break all;
							}
					}
				}
				long ts = iTime++;
				for (int d=d0; d<=d1; d++) {
					if (d1 - d0 <= 1 || (d % 2 == 0) == mwfDown) {
						for (int p=p0; p<=p1; p++) {
							iSelected[d][p] = !allSelected;
							iLastSelectedTime[d][p] = ts;
						}
					}
				}
			}
			iDownDay = -1; iDownPeriod = -1; iOverDay = -1; iOverPeriod = -1;
			generatePriorities();
			FreeTimeChangeEvent event = new FreeTimeChangeEvent();
			for (FreeTimeChangeHandler handler: iFreeTimeChangeHandlers)
				handler.onFreeTimeChange(event);
			break;
		case Event.ONMOUSEOVER:
			iOverDay = day; iOverPeriod = period;
			break;
		case Event.ONMOUSEOUT:
			iOverDay = -1; iOverPeriod = -1;
			break;
		}
		update();
	}
	
	public void addFreeTimeChangeHandler(FreeTimeChangeHandler handler) {
		iFreeTimeChangeHandlers.add(handler);
	}
	
	private class CellSelectingGrid extends Grid {
		private CellSelectingGrid(int rows, int cols) {
			super(rows, cols);
			sinkEvents(Event.ONMOUSEDOWN);
			sinkEvents(Event.ONMOUSEUP);
			sinkEvents(Event.ONCLICK);
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
		}
		
		public void onBrowserEvent(Event event) {
			Element td = getEventTargetCell(event);
			if (td==null) return;
		    Element tr = DOM.getParent(td);
		    Element body = DOM.getParent(tr);
		    int row = DOM.getChildIndex(body, tr);
		    int col = DOM.getChildIndex(tr, td);
		    if (row == 0 || col ==0) return;
		    processMouseEvent(DOM.eventGetType(event), row - 1, col - 1);
		}
	}
	
	public class FreeTimeChangeEvent {
		public ArrayList<CourseRequestInterface.FreeTime> getFreeTime() {
			return FreeTimePicker.this.getFreeTime();
		}
	}
	
	public static interface FreeTimeChangeHandler {
		public void onFreeTimeChange(FreeTimeChangeEvent event);
	}
}
