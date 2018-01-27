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
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.rooms.RoomHint;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.TimeSelector.TimeUtils;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridBackground;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridCell;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridModel;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridCell.Property;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class TimetableGrid extends Composite {
	public static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	public static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static DateTimeFormat sDateFormatMeeting = ServerDateTimeFormat.getFormat(CONSTANTS.timetableGridDateFormat());
	protected static NumberFormat sUtilizationFormat = NumberFormat.getFormat(CONSTANTS.utilizationFormat());
	private static int sHeaderWidth = 100;
	private static int sLineHeight = 15;
	
	private P iContainer;
	private List<Meeting> iMeetings = new ArrayList<Meeting>();
	private List<Background> iBackbrounds = new ArrayList<Background>();
	private int iCellWidth;
	
	public TimetableGrid(FilterInterface filter, final TimetableGridModel model, int index, int pageWidth, int weekOffset) {
		int displayMode = Integer.valueOf(filter.getParameterValue("dispMode", "0"));
		boolean hasDay[] = { true, true, true, true, true, false, false };
		String days = filter.getParameterValue("days");
		int nrDays = 0;
		if (days != null && days.length() == 7 && days.indexOf('1') >= 0) {
			for (int i = 0; i < 7; i++)
				if (days.charAt(i) == '1') {
					hasDay[i] = true;
					nrDays ++;
				} else {
					hasDay[i] = false;
				}
		}
		int nrTimes = 20;
		int startSlot = 90;
		int step = 6;
		String[] times = filter.getParameterValue("times", "90|222|6").split("\\|");
		if (times != null && times.length == 3) {
			startSlot = Integer.parseInt(times[0]);
			step = Integer.parseInt(times[2]);
			nrTimes = (Integer.parseInt(times[1]) - startSlot) / step; 
		}
		int endSlot = startSlot + step * nrTimes;
		boolean showPreferences = "1".equals(filter.getParameterValue("showPreferences"));
		boolean showInstructors = "1".equals(filter.getParameterValue("showInstructors"));
		boolean showTimes = "1".equals(filter.getParameterValue("showTimes"));
		boolean showRoom = !"0".equals(filter.getParameterValue("resource"));
		boolean showDate = "-100".equals(filter.getParameterValue("weeks"));
		String comment = getComment(model);
		
		if (displayMode == 0) {
			int headerLines = 2;
			if ((index % 3) != 0) headerLines = 0;
			int nrLines = 0;
			int[] dayIndex = new int[7];
			int x = 0;
			int[] dayLines = new int[7];
			for (int i = 0; i < 7; i++) {
				int d = (i + weekOffset) % 7;
				dayLines[d] = model.getNrLines(d, 2);
				if (hasDay[d] && nrLines < dayLines[d]) nrLines = dayLines[d];
				dayIndex[d] = x;
				if (hasDay[d]) x++;
			}
			iContainer = new P("unitime-TimetableGrid");
			iCellWidth = (pageWidth - sHeaderWidth) / (nrDays * nrTimes);
			if (iCellWidth < 50) iCellWidth = 50;
			iContainer.setSize(sHeaderWidth + (nrDays * nrTimes) * iCellWidth + 2 * nrDays, (headerLines + nrLines) * sLineHeight + 1);
			
			if (headerLines > 0) {
				P name = new P("grid-name");
				name.setSize(sHeaderWidth, headerLines * sLineHeight);
				iContainer.add(name);
			}
			
			P verticalHeader = new P("vertical-header");
			verticalHeader.setSize(sHeaderWidth, nrLines * sLineHeight + 1);
			iContainer.add(verticalHeader, 0, headerLines * sLineHeight);
			
			P panel = new P("timetable-panel");
			panel.setSize((nrDays * nrTimes) * iCellWidth + 2 * nrDays, nrLines * sLineHeight + 1);
			if (headerLines > 0) {
				P horizontalHeader = new P("horizontal-header");
				horizontalHeader.setSize((nrDays * nrTimes) * iCellWidth + 2 * nrDays, headerLines * sLineHeight);
				iContainer.add(horizontalHeader, sHeaderWidth, 0);
				
				for (int i = 0; i < 7; i++) {
					int d = (i + weekOffset) % 7;
					if (!hasDay[d]) continue;
					for (int t = 0; t < nrTimes; t++) {
						int j = dayIndex[d] * nrTimes + t;
						P hi = new P("header-interval");
						hi.setSize(iCellWidth, headerLines * sLineHeight);
						hi.setHTML(CONSTANTS.days()[d] + "<br>" + TimeUtils.slot2time(startSlot + t * step));
						horizontalHeader.add(hi, j * iCellWidth + 2 * dayIndex[d], 0);
						if (t == 0 && dayIndex[d] > 0) {
							P vs = new P("vertical-double-separator");
							panel.add(vs, j * iCellWidth + 2 * dayIndex[d] - 2, 0);
							P vs2 = new P("vertical-double-separator");
							horizontalHeader.add(vs2, j * iCellWidth + 2 * dayIndex[d] - 2, 0);
						} else {
							P vs = new P("vertical-separator");
							panel.add(vs, j * iCellWidth + 2 * dayIndex[d], 0);
						}
					}
				}
				P lastVShead = new P("vertical-separator");
				lastVShead.getElement().getStyle().setRight(0, Unit.PX);
				lastVShead.getElement().getStyle().setTop(0, Unit.PX);
				lastVShead.getElement().getStyle().setPosition(Position.ABSOLUTE);
				horizontalHeader.add(lastVShead);
			} else {
				for (int d = 0; d < 7; d++) {
					if (!hasDay[d]) continue;
					for (int t = 0; t < nrTimes; t++) {
						int i = dayIndex[d] * nrTimes + t;
						if (t == 0 && dayIndex[d] > 0) {
							P vs = new P("vertical-double-separator");
							panel.add(vs, i * iCellWidth + 2 * dayIndex[d] - 2, 0);
						} else {
							P vs = new P("vertical-separator");
							panel.add(vs, i * iCellWidth + 2 * dayIndex[d], 0);
						}
					}
				}
			}
			
			iContainer.add(panel, sHeaderWidth, headerLines * sLineHeight);
			P lastVSpan = new P("vertical-separator");
			lastVSpan.getElement().getStyle().setRight(0, Unit.PX);
			lastVSpan.getElement().getStyle().setTop(0, Unit.PX);
			lastVSpan.getElement().getStyle().setPosition(Position.ABSOLUTE);
			panel.add(lastVSpan);
			
	        P hs = new P("horizontal-separator");
	        panel.add(hs, 0, 0);
	        final P hi = new P("header-interval");
	        hi.setSize(sHeaderWidth, nrLines * sLineHeight);
	        hi.setHTML(model.getName() + (comment != null && !comment.isEmpty() ? "<br>" + comment : ""));
	        if (model.hasNameColor()) hi.getElement().getStyle().setColor(model.getNameColor());
	        if (model.getResourceType() == 0 && model.getResourceId() != null) {
	        	hi.addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						RoomHint.showHint(hi.getElement(), model.getResourceId(), null, null, true);
					}
				});
	        	hi.addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						RoomHint.hideHint();
					}
				});
	        } else {
	        	hi.setTitle(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
	        }
	        verticalHeader.add(hi, 0, 0);
	        
	        P lastHSpan = new P("horizontal-separator");
			lastHSpan.getElement().getStyle().setLeft(0, Unit.PX);
			lastHSpan.getElement().getStyle().setBottom(0, Unit.PX);
			lastHSpan.getElement().getStyle().setPosition(Position.ABSOLUTE);
			panel.add(lastHSpan);
			P lastHShead = new P("horizontal-separator");
			lastHShead.getElement().getStyle().setLeft(0, Unit.PX);
			lastHShead.getElement().getStyle().setBottom(0, Unit.PX);
			lastHShead.getElement().getStyle().setPosition(Position.ABSOLUTE);
			verticalHeader.add(lastHShead);
			
			for (TimetableGridBackground cell: model.getBackgrounds()) {
	        	if (!hasDay[cell.getDay()]) continue;
	        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
	        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
	        	Background b = new Background(cell);
	        	iBackbrounds.add(b);
	        	b.getElement().getStyle().setHeight(nrLines * sLineHeight, Unit.PX);
		        int start = cell.getSlot();
		        int stop = cell.getSlot() + cell.getLength();
		        if (start < startSlot) start = startSlot;
		        if (stop  > endSlot) stop = endSlot;
		        b.getElement().getStyle().setWidth(stop * iCellWidth / step - start * iCellWidth / step, Unit.PX);
		        panel.add(b, dayIndex[cell.getDay()] * nrTimes * iCellWidth + (start - startSlot) * iCellWidth / step + 2 * dayIndex[cell.getDay()], 0);
			}
	        
	        for (TimetableGridCell cell: model.getCells()) {
	        	if (!hasDay[cell.getDay()]) continue;
	        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
	        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
	        	Meeting m = new Meeting(cell, showRoom, showInstructors, showTimes, showPreferences, showDate);
	        	iMeetings.add(m);
		        int lines = cell.getNrLines();
		        if (dayLines[cell.getDay()] < nrLines && (cell.getIndex() + cell.getNrLines() == dayLines[cell.getDay()])) {
		        	lines += nrLines - dayLines[cell.getDay()];
		        }
		        m.getElement().getStyle().setHeight(1 + lines * sLineHeight, Unit.PX);
		        int start = cell.getSlot();
		        int stop = cell.getSlot() + cell.getLength();
		        if (start < startSlot) start = startSlot;
		        if (stop  > endSlot) stop = endSlot;
		        m.getElement().getStyle().setWidth(1 + (stop - startSlot) * iCellWidth / step - (start - startSlot) * iCellWidth / step, Unit.PX);
		        panel.add(m, dayIndex[cell.getDay()] * nrTimes * iCellWidth + (start - startSlot) * iCellWidth / step + 2 * dayIndex[cell.getDay()], cell.getIndex() * sLineHeight);
	        }
		} else if (displayMode == 1) {
			int nrLines = 0;
			int[] dayIndex = new int[8];
			int[] toDayIdx = new int[7];
			for (int i = 0; i < 7; i++) {
				int d = (i + weekOffset) % 7;
				dayIndex[i] = nrLines;
				toDayIdx[d] = i;
				if (hasDay[d]) nrLines += model.getNrLines(d, 2);
			}
			dayIndex[7] = nrLines;

			int headerLines = 1 + (comment != null && !comment.isEmpty() ? 1 : 0);
			iContainer = new P("unitime-TimetableGrid");
			iCellWidth = (pageWidth - sHeaderWidth) / nrTimes;
			iContainer.setSize(sHeaderWidth + nrTimes * iCellWidth + 2, (headerLines + nrLines) * sLineHeight + 2);
			
			final P name = new P("grid-name");
			name.setSize(sHeaderWidth, headerLines * sLineHeight);
			name.setHTML(model.getName() + (comment != null && !comment.isEmpty() ? "<br>" + comment : ""));
			if (model.hasNameColor()) name.getElement().getStyle().setColor(model.getNameColor());
			if (model.getResourceType() == 0 && model.getResourceId() != null) {
	        	name.addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						RoomHint.showHint(name.getElement(), model.getResourceId(), null, null, true);
					}
				});
	        	name.addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						RoomHint.hideHint();
					}
				});
	        } else {
	        	name.setTitle(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
	        }
			iContainer.add(name);
			
			P verticalHeader = new P("vertical-header");
			verticalHeader.setSize(sHeaderWidth, nrLines * sLineHeight + 1);
			iContainer.add(verticalHeader, 0, headerLines * sLineHeight);
			
			P horizontalHeader = new P("horizontal-header");
			horizontalHeader.setSize(nrTimes * iCellWidth + 1, headerLines * sLineHeight);
			iContainer.add(horizontalHeader, sHeaderWidth, 0);
			
			P panel = new P("timetable-panel");
			panel.setSize(nrTimes * iCellWidth + 1, nrLines * sLineHeight + 1);
			iContainer.add(panel, sHeaderWidth, headerLines * sLineHeight);

			for (int i = 0; i < nrTimes; i++) {
				P hi = new P("header-interval");
				hi.setSize(iCellWidth, headerLines * sLineHeight);
				hi.setText(TimeUtils.slot2time(startSlot + i * step));
				horizontalHeader.add(hi, i * iCellWidth, 0);
				
				P vs = new P("vertical-separator");
				panel.add(vs, i * iCellWidth, 0);
			}
			P lastVSpan = new P("vertical-separator");
			lastVSpan.getElement().getStyle().setRight(0, Unit.PX);
			lastVSpan.getElement().getStyle().setTop(0, Unit.PX);
			lastVSpan.getElement().getStyle().setPosition(Position.ABSOLUTE);
			panel.add(lastVSpan);
			P lastVShead = new P("vertical-separator");
			lastVShead.getElement().getStyle().setRight(0, Unit.PX);
			lastVShead.getElement().getStyle().setTop(0, Unit.PX);
			lastVShead.getElement().getStyle().setPosition(Position.ABSOLUTE);
			horizontalHeader.add(lastVShead);
			
	        for (int i = 0; i < 7; i++) {
	        	int d = (i + weekOffset) % 7;
	        	if (!hasDay[d]) continue;
				P hs = new P("horizontal-separator");
				panel.add(hs, 0, sLineHeight * dayIndex[i]);

				P hi = new P("header-interval");
				hi.setSize(sHeaderWidth, (dayIndex[1 + i] - dayIndex[i]) * sLineHeight);
				hi.setText(CONSTANTS.longDays()[d]);
				verticalHeader.add(hi, 0, sLineHeight * dayIndex[i]);
			}
	        P lastHSpan = new P("horizontal-separator");
			lastHSpan.getElement().getStyle().setLeft(0, Unit.PX);
			lastHSpan.getElement().getStyle().setBottom(0, Unit.PX);
			lastHSpan.getElement().getStyle().setPosition(Position.ABSOLUTE);
			panel.add(lastHSpan);
			P lastHShead = new P("horizontal-separator");
			lastHShead.getElement().getStyle().setLeft(0, Unit.PX);
			lastHShead.getElement().getStyle().setBottom(0, Unit.PX);
			lastHShead.getElement().getStyle().setPosition(Position.ABSOLUTE);
			verticalHeader.add(lastHShead);
			
	        for (TimetableGridBackground cell: model.getBackgrounds()) {
	        	if (!hasDay[cell.getDay()]) continue;
	        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
	        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
	        	Background b = new Background(cell);
	        	iBackbrounds.add(b);
	        	int i = toDayIdx[cell.getDay()];
	        	b.getElement().getStyle().setHeight((dayIndex[i + 1] - dayIndex[i]) * sLineHeight, Unit.PX);
		        int start = cell.getSlot();
		        int stop = cell.getSlot() + cell.getLength();
		        if (start < startSlot) start = startSlot;
		        if (stop  > endSlot) stop = endSlot;
		        b.getElement().getStyle().setWidth(stop * iCellWidth / step - start * iCellWidth / step, Unit.PX);
		        panel.add(b, (start - startSlot) * iCellWidth / step, dayIndex[i] * sLineHeight);
			}
	        
	        for (TimetableGridCell cell: model.getCells()) {
	        	if (!hasDay[cell.getDay()]) continue;
	        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
	        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
	        	Meeting m = new Meeting(cell, showRoom, showInstructors, showTimes, showPreferences, showDate);
	        	iMeetings.add(m);
		        m.getElement().getStyle().setHeight(1 + cell.getNrLines() * sLineHeight, Unit.PX);
		        int start = cell.getSlot();
		        int stop = cell.getSlot() + cell.getLength();
		        if (start < startSlot) start = startSlot;
		        if (stop  > endSlot) stop = endSlot;
		        m.getElement().getStyle().setWidth(1 + (stop - startSlot) * iCellWidth / step - (start - startSlot) * iCellWidth / step, Unit.PX);
		        panel.add(m, (start - startSlot) * iCellWidth / step, (dayIndex[toDayIdx[cell.getDay()]] + cell.getIndex()) * sLineHeight);
	        }
		} else if (displayMode == 2) {
			int nrColumns = 0;
			int[] colIndex = new int[8];
			int[] toDayIdx = new int[7];
			for (int i = 0; i < 7; i++) {
				int d = (i + weekOffset) % 7;
				colIndex[i] = nrColumns;
				toDayIdx[d] = i;
				if (hasDay[d]) nrColumns += model.getNrLines(d, 1);
			}
			colIndex[7] = nrColumns;
			int linesPerTime = 2;

			int headerLines = 1 + (comment != null && !comment.isEmpty() ? 1 : 0);
			iContainer = new P("unitime-TimetableGrid");
			iCellWidth = (pageWidth - sHeaderWidth) / nrColumns;
			iContainer.setSize(sHeaderWidth + nrColumns * iCellWidth + 2, (headerLines + linesPerTime * nrTimes) * sLineHeight + 2);
			
			final P name = new P("grid-name");
			name.setSize(sHeaderWidth, headerLines * sLineHeight);
			name.setHTML(model.getName() + (comment != null && !comment.isEmpty() ? "<br>" + comment : ""));
			if (model.hasNameColor()) name.getElement().getStyle().setColor(model.getNameColor());
			if (model.getResourceType() == 0 && model.getResourceId() != null) {
	        	name.addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						RoomHint.showHint(name.getElement(), model.getResourceId(), null, null, true);
					}
				});
	        	name.addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						RoomHint.hideHint();
					}
				});
	        } else {
				name.setTitle(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
	        }
			iContainer.add(name);
			
			P verticalHeader = new P("vertical-header");
			verticalHeader.setSize(sHeaderWidth, linesPerTime * nrTimes * sLineHeight + 1);
			iContainer.add(verticalHeader, 0, headerLines * sLineHeight);
			
			P horizontalHeader = new P("horizontal-header");
			horizontalHeader.setSize(nrColumns * iCellWidth + 1, headerLines * sLineHeight);
			iContainer.add(horizontalHeader, sHeaderWidth, 0);
			
			P panel = new P("timetable-panel");
			panel.setSize(nrColumns * iCellWidth + 1, linesPerTime * nrTimes * sLineHeight + 1);
			iContainer.add(panel, sHeaderWidth, headerLines * sLineHeight);

			for (int i = 0; i < 7; i++) {
				int d = (i + weekOffset) % 7;
				if (!hasDay[d]) continue;
				P hi = new P("header-interval");
				hi.setSize(iCellWidth * model.getNrLines(d, 1), headerLines * sLineHeight);
				hi.setText(CONSTANTS.longDays()[d]);
				horizontalHeader.add(hi, colIndex[i] * iCellWidth, 0);
				
				P vs = new P("vertical-separator");
				panel.add(vs, colIndex[i] * iCellWidth, 0);
			}
			P lastVSpan = new P("vertical-separator");
			lastVSpan.getElement().getStyle().setRight(0, Unit.PX);
			lastVSpan.getElement().getStyle().setTop(0, Unit.PX);
			lastVSpan.getElement().getStyle().setPosition(Position.ABSOLUTE);
			panel.add(lastVSpan);
			P lastVShead = new P("vertical-separator");
			lastVShead.getElement().getStyle().setRight(0, Unit.PX);
			lastVShead.getElement().getStyle().setTop(0, Unit.PX);
			lastVShead.getElement().getStyle().setPosition(Position.ABSOLUTE);
			horizontalHeader.add(lastVShead);
			
	        for (int i = 0; i < nrTimes; i++) {
				P hs = new P("horizontal-separator");
				panel.add(hs, 0, sLineHeight * linesPerTime * i);

				P hi = new P("header-interval");
				hi.setSize(sHeaderWidth, linesPerTime * sLineHeight);
				hi.setText(TimeUtils.slot2time(startSlot + step * i));
				verticalHeader.add(hi, 0, sLineHeight * linesPerTime * i);
			}
	        P lastHSpan = new P("horizontal-separator");
			lastHSpan.getElement().getStyle().setLeft(0, Unit.PX);
			lastHSpan.getElement().getStyle().setBottom(0, Unit.PX);
			lastHSpan.getElement().getStyle().setPosition(Position.ABSOLUTE);
			panel.add(lastHSpan);
			P lastHShead = new P("horizontal-separator");
			lastHShead.getElement().getStyle().setLeft(0, Unit.PX);
			lastHShead.getElement().getStyle().setBottom(0, Unit.PX);
			lastHShead.getElement().getStyle().setPosition(Position.ABSOLUTE);
			verticalHeader.add(lastHShead);
			
	        for (TimetableGridBackground cell: model.getBackgrounds()) {
	        	if (!hasDay[cell.getDay()]) continue;
	        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
	        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
	        	Background b = new Background(cell);
	        	iBackbrounds.add(b);
		        int start = cell.getSlot();
		        int stop = cell.getSlot() + cell.getLength();
		        if (start < startSlot) start = startSlot;
		        if (stop  > endSlot) stop = endSlot;
	        	b.getElement().getStyle().setHeight(stop * sLineHeight * linesPerTime / step - start * sLineHeight * linesPerTime / step, Unit.PX);
	        	int i = toDayIdx[cell.getDay()];
		        b.getElement().getStyle().setWidth((colIndex[1 + i] - colIndex[i]) * iCellWidth, Unit.PX);
		        panel.add(b, colIndex[i] * iCellWidth, (start - startSlot) * sLineHeight * linesPerTime / step);
			}
	        
	        for (TimetableGridCell cell: model.getCells()) {
	        	if (!hasDay[cell.getDay()]) continue;
	        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
	        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
	        	Meeting m = new Meeting(cell, showRoom, showInstructors, showTimes, showPreferences, showDate);
	        	iMeetings.add(m);
		        int start = cell.getSlot();
		        int stop = cell.getSlot() + cell.getLength();
		        if (start < startSlot) start = startSlot;
		        if (stop  > endSlot) stop = endSlot;
		        m.getElement().getStyle().setHeight(1 + (stop - startSlot) * sLineHeight * linesPerTime / step - (start - startSlot) * sLineHeight * linesPerTime / step, Unit.PX);
		        m.getElement().getStyle().setWidth(1 + iCellWidth * cell.getNrLines(), Unit.PX);
		        panel.add(m, (colIndex[toDayIdx[cell.getDay()]] + cell.getIndex()) * iCellWidth, (start - startSlot) * sLineHeight * linesPerTime / step);
	        }
		} else if (displayMode == 3) {
			int nrLines = 0;
			int[] dayIndex = new int[366];
			for (int d = 0; d < 365; d++) {
				dayIndex[d] = nrLines;
				int date = d + model.getFirstSessionDay();
				if (model.getFirstDay() >= 0 && (date < model.getFirstDay() || date > model.getFirstDay() + 6)) continue;
				int day = d % 7;
				if (hasDay[day] && model.hasDate(day, date)) {
					nrLines += model.getNrDateLines(day, date, 2);
				}
			}
			dayIndex[365] = nrLines;

			int headerLines = 1 + (comment != null && !comment.isEmpty() ? 1 : 0);
			iContainer = new P("unitime-TimetableGrid");
			iCellWidth = (pageWidth - sHeaderWidth) / nrTimes;
			iContainer.setSize(sHeaderWidth + nrTimes * iCellWidth + 2, (headerLines + nrLines) * sLineHeight + 2);
			
			final P name = new P("grid-name");
			name.setSize(sHeaderWidth, headerLines * sLineHeight);
			name.setHTML(model.getName() + (comment != null && !comment.isEmpty() ? "<br>" + comment : ""));
			if (model.hasNameColor()) name.getElement().getStyle().setColor(model.getNameColor());
			if (model.getResourceType() == 0 && model.getResourceId() != null) {
	        	name.addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						RoomHint.showHint(name.getElement(), model.getResourceId(), null, null, true);
					}
				});
	        	name.addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						RoomHint.hideHint();
					}
				});
	        } else {
				name.setTitle(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
	        }
			iContainer.add(name);
			
			P verticalHeader = new P("vertical-header");
			verticalHeader.setSize(sHeaderWidth, nrLines * sLineHeight + 1);
			iContainer.add(verticalHeader, 0, headerLines * sLineHeight);
			
			P horizontalHeader = new P("horizontal-header");
			horizontalHeader.setSize(nrTimes * iCellWidth + 1, headerLines * sLineHeight);
			iContainer.add(horizontalHeader, sHeaderWidth, 0);
			
			P panel = new P("timetable-panel");
			panel.setSize(nrTimes * iCellWidth + 1, nrLines * sLineHeight + 1);
			iContainer.add(panel, sHeaderWidth, headerLines * sLineHeight);

			for (int i = 0; i < nrTimes; i++) {
				P hi = new P("header-interval");
				hi.setSize(iCellWidth, headerLines * sLineHeight);
				hi.setText(TimeUtils.slot2time(startSlot + i * step));
				horizontalHeader.add(hi, i * iCellWidth, 0);
				
				P vs = new P("vertical-separator");
				panel.add(vs, i * iCellWidth, 0);
			}
			P lastVSpan = new P("vertical-separator");
			lastVSpan.getElement().getStyle().setRight(0, Unit.PX);
			lastVSpan.getElement().getStyle().setTop(0, Unit.PX);
			lastVSpan.getElement().getStyle().setPosition(Position.ABSOLUTE);
			panel.add(lastVSpan);
			P lastVShead = new P("vertical-separator");
			lastVShead.getElement().getStyle().setRight(0, Unit.PX);
			lastVShead.getElement().getStyle().setTop(0, Unit.PX);
			lastVShead.getElement().getStyle().setPosition(Position.ABSOLUTE);
			horizontalHeader.add(lastVShead);
			
			for (int d = 0; d < 365; d++) {
				int date = d + model.getFirstSessionDay();
				if (model.getFirstDay() >= 0 && (date < model.getFirstDay() || date > model.getFirstDay() + 6)) continue;
				int day = d % 7;
				if (!hasDay[day] || !model.hasDate(day, date)) continue;
				P hs = new P("horizontal-separator");
				panel.add(hs, 0, sLineHeight * dayIndex[d]);

				P hi = new P("header-interval");
				hi.setSize(sHeaderWidth, (dayIndex[1 + d] - dayIndex[d]) * sLineHeight);
				hi.setText(sDateFormatMeeting.format(getDate(model.getFirstDate(), d)));
				verticalHeader.add(hi, 0, sLineHeight * dayIndex[d]);
			}
	        P lastHSpan = new P("horizontal-separator");
			lastHSpan.getElement().getStyle().setLeft(0, Unit.PX);
			lastHSpan.getElement().getStyle().setBottom(0, Unit.PX);
			lastHSpan.getElement().getStyle().setPosition(Position.ABSOLUTE);
			panel.add(lastHSpan);
			P lastHShead = new P("horizontal-separator");
			lastHShead.getElement().getStyle().setLeft(0, Unit.PX);
			lastHShead.getElement().getStyle().setBottom(0, Unit.PX);
			lastHShead.getElement().getStyle().setPosition(Position.ABSOLUTE);
			verticalHeader.add(lastHShead);
			
	        for (TimetableGridBackground cell: model.getBackgrounds()) {
	        	if (!hasDay[cell.getDay()]) continue;
	        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
	        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
	        	for (int d = 0; d < 365; d++) {
	        		int date = d + model.getFirstSessionDay();
	        		int day = d % 7;
					if (model.getFirstDay() >= 0 && (date < model.getFirstDay() || date > model.getFirstDay() + 6)) continue;
					if (cell.getDay() == day) {
			        	Background b = new Background(cell);
			        	iBackbrounds.add(b);
			        	b.getElement().getStyle().setHeight((dayIndex[1 + d] - dayIndex[d]) * sLineHeight, Unit.PX);
				        int start = cell.getSlot();
				        int stop = cell.getSlot() + cell.getLength();
				        if (start < startSlot) start = startSlot;
				        if (stop  > endSlot) stop = endSlot;
				        b.getElement().getStyle().setWidth(stop * iCellWidth / step - start * iCellWidth / step, Unit.PX);
				        panel.add(b, (start - startSlot) * iCellWidth / step, dayIndex[d] * sLineHeight);
					}
	        	}
			}
	        
	        for (TimetableGridCell cell: model.getCells()) {
	        	if (!hasDay[cell.getDay()]) continue;
	        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
	        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
	        	for (int d = 0; d < 365; d++) {
	        		int date = d + model.getFirstSessionDay();
					if (model.getFirstDay() >= 0 && (date < model.getFirstDay() || date > model.getFirstDay() + 6)) continue;
					int day = d % 7;
					if (cell.getDay() == day && cell.hasDate(date)) {
			        	Meeting m = new Meeting(cell, showRoom, showInstructors, showTimes, showPreferences, showDate);
			        	iMeetings.add(m);
				        m.getElement().getStyle().setHeight(1 + cell.getNrLines() * sLineHeight, Unit.PX);
				        int start = cell.getSlot();
				        int stop = cell.getSlot() + cell.getLength();
				        if (start < startSlot) start = startSlot;
				        if (stop  > endSlot) stop = endSlot;
				        m.getElement().getStyle().setWidth(1 + (stop - startSlot) * iCellWidth / step - (start - startSlot) * iCellWidth / step, Unit.PX);
				        panel.add(m, (start - startSlot) * iCellWidth / step, (dayIndex[d] + cell.getIndex()) * sLineHeight);						
					}
	        	}
	        }
		}
		
		initWidget(iContainer);
	}
	
	protected int min(int a, int b) {
		return (a < b ? a : b);
	}
	
	protected int max(int a, int b) {
		return (a > b ? a : b);
	}
	
	public class Meeting extends AbsolutePanel {
		private TimetableGridCell iCell;
		private List<Meeting> iSiblings = null;
		private Widget iTitle = null;
		
		private Meeting(TimetableGridCell cell, boolean showRoom, boolean showInstructor, boolean showTime, boolean showPreference, boolean showDate) {
			super();
			iCell = cell;
			
	        setStyleName("meeting");
	        if (cell.hasBackground())
	        	getElement().getStyle().setBackgroundColor(cell.getBackground());
	        
	        P header = new P("header", "label");
	        header.setHeight(sLineHeight * cell.getNrNames());
	        header.setHTML(cell.getName("<br>"));
	        header.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
	        if (cell.isItalics())
	        	header.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
	        add(header);

	        P footer = new P("footer");
	        String notes = "";
	        if (showTime && cell.hasTime()) notes += (notes.isEmpty() ? "" : "<br>") + cell.getTime();
	        if (showDate && cell.hasDate()) notes += (notes.isEmpty() ? "" : "<br>") + cell.getDate();
	        if (showRoom && cell.getNrRooms() > 0) notes += (notes.isEmpty() ? "" : "<br>") + cell.getRoom("<br>");
	        if (showInstructor && cell.getNrInstructors() > 0) notes += (notes.isEmpty() ? "" : "<br>") + cell.getInstructor("<br>");
	        if (showPreference && cell.hasPreference()) notes += (notes.isEmpty() ? "" : "<br>") + "<span style='color:rgb(200,200,200)'>" + cell.getPreference() + "</span>";
	        footer.setHTML(notes);
	        footer.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
	        add(footer);
	        
	        sinkEvents(Event.ONCLICK);
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			
			getElement().getStyle().setPosition(Position.ABSOLUTE);
		}
		
		public void onBrowserEvent(Event event) {
			EventTarget related = event.getRelatedEventTarget();
		    switch (DOM.eventGetType(event)) {
			case Event.ONCLICK:
				if (getCell().hasId() && getCell().getType() == TimetableGridCell.Type.Class) {
					UniTimeFrameDialog.openDialog(MESSAGES.dialogSuggestions(), "gwt.jsp?page=suggestions&menu=hide&id=" + getCell().getId(),"900","90%");
					select(false);
				}
				break;
			case Event.ONMOUSEOVER:
		        if (related == null || !getElement().isOrHasChild((Element)related.cast())) {
					select(true);
		        }
				break;
			case Event.ONMOUSEOUT:
		        if (related == null || !getElement().isOrHasChild((Element)related.cast())) {
		        	select(false);
		        }
				break;
			}
		   
		    super.onBrowserEvent(event);
		}
		
		public List<Meeting> getSiblings() {
			if (iSiblings == null) {
				iSiblings = new ArrayList<Meeting>();
				for (Meeting m: iMeetings) {
					if (getCell().sameClassOrMeeting(m.getCell()))
						iSiblings.add(m);
				}
				if (iSiblings.isEmpty())
					iSiblings.add(this);
			}
			return iSiblings;
		}
		
		protected Widget getCellTitle() {
			if (iTitle == null) {
				SimpleForm form = new SimpleForm();
				form.removeStyleName("unitime-NotPrintableBottomLine");
				form.addRow(MESSAGES.propName(), new HTML(iCell.getTitle("<br>&nbsp;&nbsp;&nbsp;") +
						(iCell.hasProperty(Property.EventType) ? " (" + iCell.getProperty(Property.EventType, "") + ")"
						:iCell.hasProperty(Property.Owner) ? " (" + iCell.getProperty(Property.Owner, "") + ")" : "")
						));
				if (iCell.hasTime())
					form.addRow(MESSAGES.propTimeGridTime(), new HTML((iCell.hasDays() ? iCell.getDays() + " " : "") + iCell.getTime()));
				if (iCell.hasDate())
					form.addRow(MESSAGES.propTimeGridDate(), new HTML(iCell.getDate()));
				if (iCell.getNrRooms() > 0)
					form.addRow(MESSAGES.propTimeGridLocation(), new HTML(iCell.getRoom("<br>")));
				if (iCell.getNrInstructors() > 0)
					form.addRow(MESSAGES.propTimeGridInstructor(), new HTML(iCell.getInstructor("<br>")));
				if (iCell.hasProperty(Property.StudentConflicts))
					form.addRow(MESSAGES.propTimeGridStudentConflicts(), new HTML(
							MESSAGES.formatStudentConflicts(
									iCell.getProperty(Property.StudentConflicts, "0"),
									iCell.getProperty(Property.StudentConflictsCommitted, "0"),
									iCell.getProperty(Property.StudentConflictsDistance, "0"),
									iCell.getProperty(Property.StudentConflictsHard, "0"))));
				if (iCell.hasProperty(Property.TimePreference))
					form.addRow(MESSAGES.propTimeGridTimePreferences(), new HTML(iCell.getProperty(Property.TimePreference, "0")));
				if (iCell.hasProperty(Property.RoomPreference))
					form.addRow(MESSAGES.propTimeGridRoomPreferences(), new HTML(iCell.getProperty(Property.RoomPreference, "0")));
				if (iCell.hasProperty(Property.DistributionPreference))
					form.addRow(MESSAGES.propTimeGridDistributionPreferences(), new HTML(iCell.getProperty(Property.DistributionPreference, "0")));
				if (iCell.hasProperty(Property.InitialAssignment)) {
					String initial = iCell.getProperty(Property.InitialAssignment, "-");
					form.addRow(MESSAGES.propTimeGridInitialAssignment(), new HTML(("-".equals(initial) ? "<i>" + MESSAGES.initialAssignmentCurrent() + "</i>" : initial)));
				}
				if (iCell.hasProperty(Property.PerturbationPenalty))
					form.addRow(MESSAGES.propTimeGridPerturbationPenalty(), new HTML(iCell.getProperty(Property.PerturbationPenalty, "0")));
				if (iCell.hasProperty(Property.DepartmentBalance))
					form.addRow(MESSAGES.propTimeGridDepartmentBalance(), new HTML(iCell.getProperty(Property.DepartmentBalance, "0")));
				if (iCell.hasProperty(Property.NonConflictingPlacements))
					form.addRow(MESSAGES.propTimeGridNonConflictingPlacements(), new HTML(iCell.getProperty(Property.NonConflictingPlacements, "0")));
				SimplePanel panel = new SimplePanel(form);
				panel.setStyleName("unitime-RoomHint");
				iTitle = panel;
			}
			return iTitle;
		}
		
		protected void select(boolean selected) {
			for (Meeting m: getSiblings()) {
				if (selected) {
					m.getElement().getStyle().setBackgroundColor("rgb(223,231,242)");
				} else {
					if (iCell.hasBackground())
			        	m.getElement().getStyle().setBackgroundColor(iCell.getBackground());
					else
						m.getElement().getStyle().clearBackgroundColor();
				}
			}
			if (getCellTitle() != null) {
				if (selected) {
					GwtHint.showHint(getElement(), getCellTitle());
				} else {
					GwtHint.hideHint();
				}
			}
		}
		
		public TimetableGridCell getCell() { return iCell; }
	}
	
	public class Background extends AbsolutePanel {
		private TimetableGridBackground iBackground;
		
		public Background(TimetableGridBackground background) {
			super();
			setStyleName("background");
			iBackground = background;
			
			getElement().getStyle().setPosition(Position.ABSOLUTE);
			if (background.hasBackground())
				getElement().getStyle().setBackgroundColor(background.getBackground());
		}
		
		public TimetableGridBackground getBackground() { return iBackground; }
	}

    protected static String getComment(TimetableGridModel model) {
    	if (model.getResourceType() == 0) {
    		return "(" + model.getSize() + ", " + sUtilizationFormat.format(model.getUtilization()) + ")";
    	} else if (model.getResourceType() >= 2) {
    		return "(" + model.getSize() + ")";
    	} else {
    		return null;
    	}
    }
    
    @SuppressWarnings("deprecation")
	protected static Date getDate(Date firstDate, int date) {
    	Date ret = new Date(firstDate.getTime());
    	ret.setDate(ret.getDate() + date);
    	return ret;
    }
}
