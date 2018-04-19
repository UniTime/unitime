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
package org.unitime.timetable.export.solver;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridBackground;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridCell;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridFilterRequest;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridFilterResponse;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridModel;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridRequest;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridResponse;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:timetable.xls")
public class ExportTimetableXLS extends TableExporter {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static Format<Date> sDateFormatMeeting = Formats.getDateFormat(Formats.Pattern.TIMETABLE_GRID_DATE);
	protected static Format<Number> sUtilizationFormat = Formats.getConcurrentNumberFormat(Formats.Pattern.UTILIZATION);
	
	@Autowired private ApplicationContext applicationContext;

	@Override
	public String reference() {
		return "timetable.xls";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		GwtRpcImplementation<TimetableGridFilterRequest, TimetableGridFilterResponse> filterService = (GwtRpcImplementation<TimetableGridFilterRequest, TimetableGridFilterResponse>)applicationContext.getBean(TimetableGridFilterRequest.class.getName());
		TimetableGridFilterResponse filter = filterService.execute(new TimetableGridFilterRequest(), helper.getSessionContext());
		fillInFilter(filter, helper);
		
		GwtRpcImplementation<TimetableGridRequest, TimetableGridResponse> service = (GwtRpcImplementation<TimetableGridRequest, TimetableGridResponse>)applicationContext.getBean(TimetableGridRequest.class.getName());
		TimetableGridRequest request = new TimetableGridRequest();
		request.setFilter(filter);
		TimetableGridResponse response = service.execute(request, helper.getSessionContext());
		
		printTables(filter, response, helper);
	}
	
	protected void printTables(FilterInterface filter, TimetableGridResponse response, ExportHelper helper) throws IOException {
		helper.setup("application/vnd.ms-excel", reference(), true);
		ExportContext cx = new ExportContext();
		int index = 0;
		int dispMode = Integer.valueOf(filter.getParameterValue("dispMode", "0")); 
		if (dispMode == 0) {
			int used = 0;
			boolean hasDay[] = { true, true, true, true, true, false, false};
			String days = filter.getParameterValue("days");
			if (days != null && days.length() == 7 && days.indexOf('1') >= 0) {
				for (int i = 0; i < 7; i++)
					hasDay[i] = (days.charAt(i) == '1');
			}
			for (int i = 0; i < 7; i++) {
				if (!hasDay[i]) continue;
				String d = "";
				for (int j = 0; j < 7; j++) d += (i == j ? "1" : "0");
				filter.getParameter("days").setValue(d);
				Sheet sheet = cx.createSheet(CONSTANTS.longDays()[i]);
				used = 0;
				for (TimetableGridModel model: response.getModels()) {
					TimetableGrid tg = new TimetableGrid(filter, model, index++, response.getWeekOffset(), used == 0);
					tg.print(cx, sheet, 0, used, null);
					used += tg.getHeight();
				}
			}
		} else {
			for (TimetableGridModel model: response.getModels()) {
				Sheet sheet = cx.createSheet(model.getName());
				TimetableGrid tg = new TimetableGrid(filter, model, index++, response.getWeekOffset(), true);
				tg.print(cx, sheet, 0, 0, null);
			}
		}
		cx.write(helper.getOutputStream(), dispMode == 2);
	}
	
	protected static class P {
		private Integer iLeft, iTop, iRight, iBottom, iWidth, iHeight;
		private String iText, iStyle;
		private boolean iItalics = false;
		private String iColor, iBgColor;
		private List<P> iContent = new ArrayList<P>();
		
		public P(String style) {
			iStyle = style;
		}
		
		public void setWidth(int width) { iWidth = width; }
		public void setHeight(int height) { iHeight = height; }
		public void setSize(int width, int height) { setWidth(width); setHeight(height); }
		
		public void setText(String text) { iText = text; }
		public void setLeft(int left) { iLeft = left; }
		public void setRight(int right) { iRight = right; }
		public void setBottom(int bottom) { iBottom = bottom; }
		public void setTop(int top) { iTop = top; }
		public void setColor(String color) { iColor = color;}
		public void setBackgroundColor(String color) { iBgColor = color; }
		public void setItalics(boolean italics) { iItalics = italics; }
		public void add(P p) { iContent.add(p); }
		public void add(P p, int left, int top) { p.iTop = top; p.iLeft = left; iContent.add(p); }
		
		public int getLeft() {
			if (iLeft != null) return iLeft;
			if (iRight != null) return getWidth() - iRight;
			return 0;
		}
		
		public int getTop() {
			if (iTop != null) return iTop;
			if (iBottom != null) return getHeight() - iBottom;
			return 0;
		}
		
		public int getWidth() {
			if (iWidth != null) return iWidth;
			int width = 0;
			for (P p: iContent) {
				int w = p.getLeft() + p.getWidth();
				if (w > width) width = w;
			}
			return width;
		}
		
		public int getHeight() {
			if (iHeight != null) return iHeight;
			int height = 0;
			for (P p: iContent) {
				int h = p.getTop() + p.getHeight();
				if (h > height) height = h;
			}
			return height;
		}
		
		public boolean hasText() {
			return iText != null && !iText.isEmpty();
		}
		public String getText() {
			return iText;
		}
		
		public String getStyle() {
			return iStyle;
		}
		
		public boolean isStyle(String... style) {
			for (String s: style) {
				if (s.equals(iStyle)) return true;
			}
			return false;
		}
		
		protected void print(ExportContext cx, Sheet sheet, int x, int y, P parent) {
			if (isStyle("unitime-TimetableGrid")) {
				for (int r = y; r < y + getHeight(); r++ ) {
					Row row = sheet.createRow(r);
					if (cx.getRowHeight() != null)
						row.setHeightInPoints(cx.getRowHeight());
					else
						row.setHeightInPoints(sheet.getDefaultRowHeightInPoints());
					for (int c = x; c < x + getWidth(); c++) {
						row.createCell(c);
					}
				}
			}
			
			if (isStyle("vertical-separator")) {
				setHeight(parent.getHeight());
			}
			if (isStyle("horizontal-separator")) {
				setWidth(parent.getWidth());
			}
			Color bgColor = null;
			if (parent != null && parent.isStyle("meeting")) {
				bgColor = Color.WHITE;
			}
			if (iBgColor != null) {
				Pattern p = Pattern.compile("rgb\\(([0-9]+),([0-9]+),([0-9]+)\\)");
				Matcher m = p.matcher(iBgColor);
				if (m.matches()) {
					bgColor = new Color(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
				}
			}
			if (parent != null && parent.isStyle("meeting")) {
				if (parent.iBgColor != null) {
					Pattern p = Pattern.compile("rgb\\(([0-9]+),([0-9]+),([0-9]+)\\)");
					Matcher m = p.matcher(parent.iBgColor);
					if (m.matches()) {
						Color color = new Color(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
						if (isStyle("header"))
							bgColor = color.darker();
						else
							bgColor = color;
					}
				} else {
					Color color = Color.WHITE;
					if (isStyle("header"))
						bgColor = Color.LIGHT_GRAY;
					else
						bgColor = color;
				}
			}
			
			Cell cell = null;
			
	        if (isStyle("header") && parent.isStyle("meeting")) {
	        	cell = sheet.getRow(y + getTop()).getCell(x + getLeft());
				cell.setCellStyle(cx.getMeetingHeaderStyle(this, parent, bgColor));
	        }
	        
			if (isStyle("footer") && parent.isStyle("meeting")) {
				cell = sheet.getRow(y + getTop()).getCell(x + getLeft());
				cell.setCellStyle(cx.getMeetingFooterStyle(this, parent,  bgColor));
			}
			
			if (isStyle("header-interval")) {
				cell = sheet.getRow(y + getTop()).getCell(x + getLeft());
				cell.setCellStyle(cx.getHeaderIntervalStyle(this));
			}
			
			if (isStyle("grid-name")) {
				cell = sheet.getRow(y + getTop()).getCell(x + getLeft());
				cell.setCellStyle(cx.getGridNameStyle(this));
			}
			
			if (cell != null) {
				if (hasText()) cell.setCellValue(getText());
				if (getHeight() > 1 || getWidth() > 1) {
					try  {
						sheet.addMergedRegion(new CellRangeAddress(y + getTop(), y + getTop() + getHeight() - 1, x + getLeft(), x + getLeft() + getWidth() - 1));
					} catch (IllegalStateException e) {
						System.err.println(e.getMessage());
					}
					for (int dx = 0; dx < getWidth() + 1; dx++)
						for (int dy = 0; dy < getHeight() + 1; dy++) {
							if (dx == 0 && dy == 0) continue;
							Row r = sheet.getRow(y + getTop() + dy);
							if (r == null) continue;
							Cell c = r.getCell(x + getLeft() + dx);
							if (c == null) continue;
							CellStyle lineStyle = cx.getLineStyle(this, dx, dy, bgColor, parent, c.getCellStyle());
							if (lineStyle != null) c.setCellStyle(lineStyle);
						}
				}
			}
			
			if (isStyle("horizontal-separator")) {
				for (int dx = 0; dx < getWidth(); dx++) {
					Cell c = sheet.getRow(y + getTop()).getCell(x + getLeft() + dx);
					CellStyle lineStyle = cx.getLineStyle(this, dx, 0, bgColor, parent, c.getCellStyle());
					if (lineStyle != null) c.setCellStyle(lineStyle);
				}
			}
			
			if (isStyle("vertical-separator")) {
				for (int dy = 0; dy < getHeight(); dy++) {
					Cell c = sheet.getRow(y + getTop() + dy).getCell(x + getLeft());
					CellStyle lineStyle = cx.getLineStyle(this, 0, dy, bgColor, parent, c.getCellStyle());
					if (lineStyle != null) c.setCellStyle(lineStyle);
				}
			}
			
			if (isStyle("unitime-TimetableGrid") || isStyle("background")) {
				for (int dx = 0; dx < getWidth(); dx++)
					for (int dy = 0; dy < getHeight(); dy++) {
						Cell c = sheet.getRow(y + getTop() + dy).getCell(x + getLeft() + dx); 
						CellStyle lineStyle = cx.getLineStyle(this, dx, dy, bgColor, parent, c.getCellStyle());
						if (lineStyle != null) c.setCellStyle(lineStyle);
					}
			}
			
	        for (P p: iContent) {
	        	p.print(cx, sheet, x + getLeft(), y + getTop(), this);
	        }
		}
	}
	
	protected static class TimetableGrid extends P {
		private List<Meeting> iMeetings = new ArrayList<Meeting>();
		private List<Background> iBackbrounds = new ArrayList<Background>();
		
		public TimetableGrid(FilterInterface filter, final TimetableGridModel model, int index, int weekOffset, boolean showHeader) {
			super("unitime-TimetableGrid");
			
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
				int headerLines = (showHeader ? 1 : 0);
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
				setSize(1 + (nrDays * nrTimes * step), headerLines + nrLines);
				
				if (headerLines > 0) {
					P name = new P("grid-name");
					name.setSize(1, headerLines);
					for (int i = 0; i < 7; i++) {
						int d = (i + weekOffset) % 7;
						if (!hasDay[d]) continue;
						name.setText(CONSTANTS.longDays()[d]);
						break;
					}
					add(name);
				}
				
				P verticalHeader = new P("vertical-header");
				verticalHeader.setSize(1, nrLines);
				add(verticalHeader, 0, headerLines);
				
				P panel = new P("timetable-panel");
				panel.setSize(nrDays * nrTimes * step, nrLines);
				if (headerLines > 0) {
					P horizontalHeader = new P("horizontal-header");
					horizontalHeader.setSize(nrDays * nrTimes, headerLines);
					add(horizontalHeader, 1, 0);
					P lastVShead = new P("vertical-separator");
					lastVShead.setRight(0);
					lastVShead.setTop(0);
					horizontalHeader.add(lastVShead);
					
					for (int i = 0; i < 7; i++) {
						int d = (i + weekOffset) % 7;
						if (!hasDay[d]) continue;
						for (int t = 0; t < nrTimes; t++) {
							int j = dayIndex[d] * nrTimes + t;
							P vs = new P("vertical-separator");
							panel.add(vs, j * step, 0);
							P hi = new P("header-interval");
							hi.setSize(step, headerLines);
							hi.setText(slot2time(startSlot + t * step));
							horizontalHeader.add(hi, j * step, 0);
						}
					}
				} else {
					for (int d = 0; d < 7; d++) {
						if (!hasDay[d]) continue;
						for (int t = 0; t < nrTimes; t++) {
							int i = dayIndex[d] * nrTimes + t;
							P vs = new P("vertical-separator");
							panel.add(vs, i * step, 0);
						}
					}
				}
				
				add(panel, 1, headerLines);
				P lastVSpan = new P("vertical-separator");
				lastVSpan.setRight(0);
				lastVSpan.setTop(0);
				panel.add(lastVSpan);
				
		        P lastHSpan = new P("horizontal-separator");
				lastHSpan.setLeft(0);
				lastHSpan.setBottom(0);
				panel.add(lastHSpan);
				P lastHShead = new P("horizontal-separator");
				lastHShead.setLeft(0);
				lastHShead.setBottom(0);
				verticalHeader.add(lastHShead);
				
				P hs = new P("horizontal-separator");
		        panel.add(hs, 0, 0);
		        final P hi = new P("header-interval");
		        hi.setSize(1, nrLines);
		        hi.setText(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
		        if (model.hasNameColor()) hi.setColor(model.getNameColor());
		        verticalHeader.add(hi, 0, 0);
		        
				for (TimetableGridBackground cell: model.getBackgrounds()) {
		        	if (!hasDay[cell.getDay()]) continue;
		        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
		        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
		        	Background b = new Background(cell);
		        	iBackbrounds.add(b);
		        	b.setHeight(nrLines);
			        int start = cell.getSlot();
			        int stop = cell.getSlot() + cell.getLength();
			        if (start < startSlot) start = startSlot;
			        if (stop  > endSlot) stop = endSlot;
			        b.setWidth(stop - start);
			        panel.add(b, dayIndex[cell.getDay()] * nrTimes * step + start - startSlot, 0);
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
			        m.setHeight(lines);
			        int start = cell.getSlot();
			        int stop = cell.getSlot() + cell.getLength();
			        if (start < startSlot) start = startSlot;
			        if (stop  > endSlot) stop = endSlot;
			        m.setWidth(stop - start);
			        panel.add(m, dayIndex[cell.getDay()] * nrTimes * step + start - startSlot, cell.getIndex());
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
				setSize(1 + nrTimes * step, headerLines + nrLines);
				
				final P name = new P("grid-name");
				name.setSize(1, headerLines);
				name.setText(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
				if (model.hasNameColor()) name.setColor(model.getNameColor());
				add(name);
				P verticalHeader = new P("vertical-header");
				verticalHeader.setSize(1, nrLines);
				add(verticalHeader, 0, headerLines);

				P horizontalHeader = new P("horizontal-header");
				horizontalHeader.setSize(nrTimes * step, headerLines);
				add(horizontalHeader, 1, 0);
				
				P panel = new P("timetable-panel");
				panel.setSize(nrTimes * step, nrLines);
				add(panel, 1, headerLines);

				P lastVSpan = new P("vertical-separator");
				lastVSpan.setRight(0);
				lastVSpan.setTop(0);
				panel.add(lastVSpan);
				P lastVShead = new P("vertical-separator");
				lastVShead.setRight(0);
				lastVShead.setTop(0);
				horizontalHeader.add(lastVShead);
		        P lastHSpan = new P("horizontal-separator");
				lastHSpan.setLeft(0);
				lastHSpan.setBottom(0);
				panel.add(lastHSpan);
				P lastHShead = new P("horizontal-separator");
				lastHShead.setLeft(0);
				lastHShead.setBottom(0);
				verticalHeader.add(lastHShead);

				for (int i = 0; i < nrTimes; i++) {
					P vs = new P("vertical-separator");
					panel.add(vs, i * step, 0);
					P hi = new P("header-interval");
					hi.setSize(step, headerLines);
					hi.setText(slot2time(startSlot + i * step));
					horizontalHeader.add(hi, i * step, 0);
				}
				
		        for (int i = 0; i < 7; i++) {
		        	int d = (i + weekOffset) % 7;
		        	if (!hasDay[d]) continue;
					P hi = new P("header-interval");
					hi.setSize(1, dayIndex[1 + i] - dayIndex[i]);
					hi.setText(CONSTANTS.longDays()[d]);
					verticalHeader.add(hi, 0, dayIndex[i]);
					P hs = new P("horizontal-separator");
					panel.add(hs, 0, dayIndex[i]);
				}
				
		        for (TimetableGridBackground cell: model.getBackgrounds()) {
		        	if (!hasDay[cell.getDay()]) continue;
		        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
		        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
		        	Background b = new Background(cell);
		        	iBackbrounds.add(b);
		        	int i = toDayIdx[cell.getDay()];
		        	b.setHeight(dayIndex[i + 1] - dayIndex[i]);
			        int start = cell.getSlot();
			        int stop = cell.getSlot() + cell.getLength();
			        if (start < startSlot) start = startSlot;
			        if (stop  > endSlot) stop = endSlot;
			        b.setWidth(stop  - start);
			        panel.add(b, start - startSlot, dayIndex[i]);
				}
		        
		        for (TimetableGridCell cell: model.getCells()) {
		        	if (!hasDay[cell.getDay()]) continue;
		        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
		        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
		        	Meeting m = new Meeting(cell, showRoom, showInstructors, showTimes, showPreferences, showDate);
		        	iMeetings.add(m);
			        m.setHeight(cell.getNrLines());
			        int start = cell.getSlot();
			        int stop = cell.getSlot() + cell.getLength();
			        if (start < startSlot) start = startSlot;
			        if (stop  > endSlot) stop = endSlot;
			        m.setWidth(stop - start);
			        panel.add(m, start - startSlot, dayIndex[toDayIdx[cell.getDay()]] + cell.getIndex());
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
				int linesPerTime = step;

				int headerLines = 1 + (comment != null && !comment.isEmpty() ? 1 : 0);
				setSize(1 + nrColumns, headerLines + linesPerTime * nrTimes);
				
				final P name = new P("grid-name");
				name.setSize(1, headerLines);
				name.setText(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
				if (model.hasNameColor()) name.setColor(model.getNameColor());
				add(name);
				
				P verticalHeader = new P("vertical-header");
				verticalHeader.setSize(1, linesPerTime * nrTimes);
				add(verticalHeader, 0, headerLines);
				
				P horizontalHeader = new P("horizontal-header");
				horizontalHeader.setSize(nrColumns, headerLines);
				add(horizontalHeader, 1, 0);
				
				P panel = new P("timetable-panel");
				panel.setSize(nrColumns, linesPerTime * nrTimes);
				add(panel, 1, headerLines);
				
				P lastVSpan = new P("vertical-separator");
				lastVSpan.setRight(0);
				lastVSpan.setTop(0);
				panel.add(lastVSpan);
				P lastVShead = new P("vertical-separator");
				lastVShead.setRight(0);
				lastVShead.setTop(0);
				horizontalHeader.add(lastVShead);
		        P lastHSpan = new P("horizontal-separator");
				lastHSpan.setLeft(0);
				lastHSpan.setBottom(0);
				panel.add(lastHSpan);
				P lastHShead = new P("horizontal-separator");
				lastHShead.setLeft(0);
				lastHShead.setBottom(0);
				verticalHeader.add(lastHShead);

				for (int i = 0; i < 7; i++) {
					int d = (i + weekOffset) % 7;
					if (!hasDay[d]) continue;
					P vs = new P("vertical-separator");
					panel.add(vs, colIndex[i], 0);
					P hi = new P("header-interval");
					hi.setSize(model.getNrLines(d, 1), headerLines);
					hi.setText(CONSTANTS.longDays()[d]);
					horizontalHeader.add(hi, colIndex[i], 0);
				}
				
		        for (int i = 0; i < nrTimes; i++) {
					P hs = new P("horizontal-separator");
					panel.add(hs, 0, linesPerTime * i);
					P hi = new P("header-interval");
					hi.setSize(1, linesPerTime);
					hi.setText(slot2time(startSlot + step * i));
					verticalHeader.add(hi, 0, linesPerTime * i);
				}
				
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
		        	b.setHeight(stop * linesPerTime / step - start * linesPerTime / step);
		        	int i = toDayIdx[cell.getDay()];
			        b.setWidth(colIndex[1 + i] - colIndex[i]);
			        panel.add(b, colIndex[i], (start - startSlot) * linesPerTime / step);
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
			        m.setHeight((stop - startSlot) * linesPerTime / step - (start - startSlot) * linesPerTime / step);
			        m.setWidth(cell.getNrLines());
			        panel.add(m, (colIndex[toDayIdx[cell.getDay()]] + cell.getIndex()), (start - startSlot) * linesPerTime / step);
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
				setSize(1 + nrTimes * step, headerLines + nrLines);
				
				final P name = new P("grid-name");
				name.setSize(1, headerLines);
				name.setText(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
				if (model.hasNameColor()) name.setColor(model.getNameColor());
				add(name);
				
				P verticalHeader = new P("vertical-header");
				verticalHeader.setSize(1, nrLines);
				add(verticalHeader, 0, headerLines);
				
				P horizontalHeader = new P("horizontal-header");
				horizontalHeader.setSize(nrTimes * step, headerLines);
				add(horizontalHeader, 1, 0);
				
				P panel = new P("timetable-panel");
				panel.setSize(nrTimes * step, nrLines);
				add(panel, 1, headerLines);
				
				P lastVSpan = new P("vertical-separator");
				lastVSpan.setRight(0);
				lastVSpan.setTop(0);
				panel.add(lastVSpan);
				P lastVShead = new P("vertical-separator");
				lastVShead.setRight(0);
				lastVShead.setTop(0);
				horizontalHeader.add(lastVShead);
		        P lastHSpan = new P("horizontal-separator");
				lastHSpan.setLeft(0);
				lastHSpan.setBottom(0);
				panel.add(lastHSpan);
				P lastHShead = new P("horizontal-separator");
				lastHShead.setLeft(0);
				lastHShead.setBottom(0);
				verticalHeader.add(lastHShead);

				for (int i = 0; i < nrTimes; i++) {
					P vs = new P("vertical-separator");
					panel.add(vs, i * step, 0);
					P hi = new P("header-interval");
					hi.setSize(step, headerLines);
					hi.setText(slot2time(startSlot + i * step));
					horizontalHeader.add(hi, i * step, 0);
				}
				
				for (int d = 0; d < 365; d++) {
					int date = d + model.getFirstSessionDay();
					if (model.getFirstDay() >= 0 && (date < model.getFirstDay() || date > model.getFirstDay() + 6)) continue;
					int day = d % 7;
					if (!hasDay[day] || !model.hasDate(day, date)) continue;
					P hs = new P("horizontal-separator");
					panel.add(hs, 0, dayIndex[d]);

					P hi = new P("header-interval");
					hi.setSize(1, dayIndex[1 + d] - dayIndex[d]);
					hi.setText(sDateFormatMeeting.format(getDate(model.getFirstDate(), d)));
					verticalHeader.add(hi, 0, dayIndex[d]);
				}
				
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
				        	b.setHeight(dayIndex[1 + d] - dayIndex[d]);
					        int start = cell.getSlot();
					        int stop = cell.getSlot() + cell.getLength();
					        if (start < startSlot) start = startSlot;
					        if (stop  > endSlot) stop = endSlot;
					        b.setWidth(stop - start);
					        panel.add(b, start - startSlot, dayIndex[d]);
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
					        m.setHeight(cell.getNrLines());
					        int start = cell.getSlot();
					        int stop = cell.getSlot() + cell.getLength();
					        if (start < startSlot) start = startSlot;
					        if (stop  > endSlot) stop = endSlot;
					        m.setWidth(stop - start);
					        panel.add(m, start - startSlot, dayIndex[d] + cell.getIndex());						
						}
		        	}
		        }
			}
		}
	}
	
	protected static class Meeting extends P {
		private TimetableGridCell iCell;
		private P iHeader, iFooter;
		
		private Meeting(TimetableGridCell cell, boolean showRoom, boolean showInstructor, boolean showTime, boolean showPreference, boolean showDate) {
			super("meeting");
			iCell = cell;
			
	        if (cell.hasBackground())
	        	setBackgroundColor(cell.getBackground());
	        
	        iHeader = new P("header");
	        iHeader.setHeight(cell.getNrNames());
	        iHeader.setText(cell.getName("\n"));
	        if (cell.isItalics())
	        	iHeader.setItalics(true);
	        add(iHeader, 0, 0);

	        iFooter = new P("footer");
	        String notes = "";
	        if (showTime && cell.hasTime()) notes += (notes.isEmpty() ? "" : "\n") + cell.getTime();
	        if (showDate && cell.hasDate()) notes += (notes.isEmpty() ? "" : "\n") + cell.getDate();
	        if (showRoom && cell.getNrRooms() > 0) notes += (notes.isEmpty() ? "" : "\n") + cell.getRoom("\n");
	        if (showInstructor && cell.getNrInstructors() > 0) notes += (notes.isEmpty() ? "" : "\n") + cell.getInstructor("\n");
	        if (showPreference && cell.hasPreference()) notes += (notes.isEmpty() ? "" : "\n") + cell.getPreference().replaceAll("\\<[^>]*>","");
	        iFooter.setText(notes);
	        add(iFooter, 0, iHeader.getHeight());
		}
		
		@Override
		public void setHeight(int height) {
			super.setHeight(height);
			iFooter.setHeight(height - iHeader.getHeight());
		}
		
		@Override
		public void setWidth(int width) {
			super.setWidth(width);
			iHeader.setWidth(width);
			iFooter.setWidth(width);
		}
		
		public TimetableGridCell getCell() { return iCell; }
	}
	
	protected static class Background extends P {
		private TimetableGridBackground iBackground;
		
		public Background(TimetableGridBackground background) {
			super("background");
			iBackground = background;
			
			if (background.hasBackground())
				setBackgroundColor(background.getBackground());
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
	
	public static String slot2time(int slot) {
		if (CONSTANTS.useAmPm()) {
			if (slot == 0) return CONSTANTS.timeMidnight();
			if (slot == 144) return CONSTANTS.timeNoon();
			if (slot == 288) return CONSTANTS.timeMidnightEnd();
		}
		int h = slot / 12;
        int m = 5 * (slot % 12);
        if (CONSTANTS.useAmPm())
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + " " + (h == 24 ? CONSTANTS.timeAm() : h >= 12 ? CONSTANTS.timePm() : CONSTANTS.timeAm());
        else
			return h + ":" + (m < 10 ? "0" : "") + m;
	}
	
	public static class ExportContext {
		private Workbook iWorkbook;
		private Map<String, CellStyle> iStyles = new HashMap<String, CellStyle>();
		private Map<String, Font> iFonts = new HashMap<String, Font>();
		private Map<String, Short> iColors = new HashMap<String, Short>();
		private int iFontSize = 12;
		private String iFontName = "Arial";
		private Float iRowHeight = null;

		public ExportContext() {
			iWorkbook = new HSSFWorkbook();
			iFontSize = ApplicationProperty.TimeGridXLSFontSize.intValue();
			iFontName = ApplicationProperty.TimeGridXLSFontName.value();
			iRowHeight = ApplicationProperty.TimeGridXLSRowHeight.floatValue();
		}
		
		public Workbook getWorkbook() { return iWorkbook; } 
		
		public Float getRowHeight() { return iRowHeight; }
		
		protected Font getFont(boolean bold, boolean italic, boolean underline, Color c) {
			Short color = null;
			if (c == null) c = Color.BLACK;
			if (c != null) {
				String colorId = Integer.toHexString(c.getRGB());
				color = iColors.get(colorId);
				if (color == null) {
					HSSFPalette palette = ((HSSFWorkbook)iWorkbook).getCustomPalette();
					HSSFColor clr = palette.findSimilarColor(c.getRed(), c.getGreen(), c.getBlue());
					color = (clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndex());
					iColors.put(colorId, color);
				}
			}
			String fontId = (bold ? "b" : "") + (italic ? "i" : "") + (underline ? "u" : "") + (color == null ? "" : color);
			Font font = iFonts.get(fontId);
			if (font == null) {
				font = iWorkbook.createFont();
				font.setBold(bold);
				font.setItalic(italic);
				font.setUnderline(underline ? Font.U_SINGLE : Font.U_NONE);
				font.setColor(color);
				font.setFontHeightInPoints((short)iFontSize);
				font.setFontName(iFontName);
				iFonts.put(fontId, font);
			}
			return font;
		}
		
		protected Font getFont(P p) {
			return getFont(p, null);
		}
		
		protected Font getFont(P p, P parent) {
			boolean bold = p.isStyle("grid-name", "horizontal-header", "vertical-header");
        	if (p.isStyle("header") && parent != null && parent.isStyle("meeting")) bold = true;
        	Color color = null;
        	if (p.iColor != null) {
        		Pattern pt = Pattern.compile("rgb\\(([0-9]+),([0-9]+),([0-9]+)\\)");
				Matcher m = pt.matcher(p.iColor);
				if (m.matches())
					color = new Color(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        	}
        	return getFont(bold, p.iItalics, false, color == null ? Color.BLACK : color);
		}
		
		protected CellStyle getMeetingHeaderStyle(P p, P parent, Color bgColor) {
			if (bgColor == null) bgColor = Color.WHITE;
			String styleId = "meeting-header-" + Integer.toHexString(bgColor.getRGB()) + (p.iItalics ? "-italics" : "");
			CellStyle style = iStyles.get(styleId);
			if (style == null) {
				style = iWorkbook.createCellStyle();
		        style.setBorderTop(BorderStyle.THICK);
		        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		        style.setBorderLeft(BorderStyle.THICK);
		        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		        style.setBorderRight(BorderStyle.THICK);
		        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.TOP);
		        style.setFont(getFont(parent));
		        String colorId = Integer.toHexString(bgColor.getRGB());
				Short color = iColors.get(colorId);
				if (color == null) {
					HSSFPalette palette = ((HSSFWorkbook)iWorkbook).getCustomPalette();
					HSSFColor clr = palette.findSimilarColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue());
					color = (clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndex());
					iColors.put(colorId, color);
				}
		        style.setFillForegroundColor(color);
		        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		        style.setWrapText(true);
		        iStyles.put(styleId, style);
			}
			return style;
		}
		
		protected CellStyle getMeetingFooterStyle(P p, P parent, Color bgColor) {
			if (bgColor == null) bgColor = Color.WHITE;
			String styleId = "meeting-footer-" + Integer.toHexString(bgColor.getRGB());
			CellStyle style = iStyles.get(styleId);
			if (style == null) {
				style = iWorkbook.createCellStyle();
		        style.setBorderBottom(BorderStyle.THICK);
		        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		        style.setBorderLeft(BorderStyle.THICK);
		        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		        style.setBorderRight(BorderStyle.THICK);
		        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.TOP);
		        style.setFont(getFont(parent));
		        String colorId = Integer.toHexString(bgColor.getRGB());
				Short color = iColors.get(colorId);
				if (color == null) {
					HSSFPalette palette = ((HSSFWorkbook)iWorkbook).getCustomPalette();
					HSSFColor clr = palette.findSimilarColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue());
					color = (clr == null ? IndexedColors.WHITE.getIndex() : clr.getIndex());
					iColors.put(colorId, color);
				}
		        style.setFillForegroundColor(color);
		        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		        style.setWrapText(true);
		        iStyles.put(styleId, style);
			}
			return style;
		}
		
		protected CellStyle getHeaderIntervalStyle(P p) {
			String styleId = "header-interval";
			CellStyle style = iStyles.get(styleId);
			if (style == null) {
				style = iWorkbook.createCellStyle();
		        style.setBorderLeft(BorderStyle.THIN);
		        style.setLeftBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
		        style.setBorderTop(BorderStyle.THIN);
		        style.setTopBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
		        if (p.getWidth() == 1) {
		        	style.setBorderRight(BorderStyle.THIN);
		        	style.setRightBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
		        }
		        style.setFont(getFont(p));
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.TOP);
		        style.setWrapText(true);
		        iStyles.put(styleId, style);
			}
			return style;
		}
		
		protected CellStyle getGridNameStyle(P p) {
			String styleId = "grid-name";
			CellStyle style = iStyles.get(styleId);
			if (style == null) {
				style = iWorkbook.createCellStyle();
		        style.setBorderLeft(BorderStyle.THIN);
		        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		        style.setBorderTop(BorderStyle.THIN);
		        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		        style.setFont(getFont(p));
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.TOP);
		        style.setWrapText(true);
		        iStyles.put(styleId, style);
			}
			return style;
		}
		
		protected CellStyle getLineStyle(P p, int dx, int dy, Color bgColor, P parent, CellStyle ps) {
			boolean thinTop = false, thinLeft = false, thinBottom = false, thinRight = false;
			boolean thickTop = false, thickLeft = false, thickBottom = false, thickRight = false;
			if (parent != null && parent.isStyle("meeting")) {
				if (dy == 0 & p.isStyle("header")) thickTop = true;
				if (dx == 0) thickLeft = true;
				if (dy == p.getHeight() - 1 && p.isStyle("footer")) thickBottom = true;
				if (dx == p.getWidth() - 1) thickRight = true;
				if (dx == p.getWidth()) {
					thickLeft = (dy < p.getHeight());
					bgColor = null; thickTop = false; thickBottom = false; thickRight = false;
				}
				if (p.isStyle("footer") && dy == p.getHeight() && dx < p.getWidth()) {
					thickTop = true;
					bgColor = null; thickLeft = false; thickBottom = false; thickRight = false;
				}
			} else if (p.isStyle("unitime-TimetableGrid")) {
				if (dy == 0) thinTop = true;
				if (dx == 0) thinLeft = true;
				if (dy == p.getHeight() - 1) thinBottom = true;
				if (dx == p.getWidth() - 1) thinRight = true;
			} else if (p.isStyle("header-interval") || p.isStyle("grid-name")) {
				if (dy == 0) thinTop = true;
				if (dx == 0) thinLeft = true;
			} else if (p.isStyle("horizontal-separator")) {
				if (dy == 0) thinTop = true;
			} else if (p.isStyle("vertical-separator")) {
				if (dx == 0) thinLeft = true;
			} else if (p.isStyle("background")) {
			} else {
				return null;
			}
			Short color = null;
			if (bgColor != null) {
				String colorId = Integer.toHexString(bgColor.getRGB());
				color = iColors.get(colorId);
				if (color == null) {
					HSSFPalette palette = ((HSSFWorkbook)iWorkbook).getCustomPalette();
					HSSFColor clr = palette.findSimilarColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue());
					color = (clr == null ? IndexedColors.WHITE.getIndex() : clr.getIndex());
					iColors.put(colorId, color);
				}
			}
			if (ps != null) {
				if (ps.getBorderTopEnum() == BorderStyle.THICK)
					thickTop = true;
				if (ps.getBorderTopEnum() == BorderStyle.THIN)
					thinTop = true;
				if (ps.getBorderLeftEnum() == BorderStyle.THICK)
					thickLeft = true;
				if (ps.getBorderLeftEnum() == BorderStyle.THIN)
					thinLeft = true;
				if (ps.getBorderBottomEnum() == BorderStyle.THICK)
					thickBottom = true;
				if (ps.getBorderBottomEnum() == BorderStyle.THIN)
					thinBottom = true;
				if (ps.getBorderRightEnum() == BorderStyle.THICK)
					thickRight = true;
				if (ps.getBorderRightEnum() == BorderStyle.THIN)
					thinRight = true;
				if (color == null && ps.getFillPatternEnum() == FillPatternType.SOLID_FOREGROUND)
					color = ps.getFillForegroundColor(); 
			}
			String styleId = "line" + (thickTop ? "-ttop" : thinTop ? "-top" : "") + (thickLeft ? "-tleft" : thinLeft ? "-left" : "")
					+ (thickBottom ? "-tbottom" : thinBottom ? "-bottom" : "") + (thickRight ? "-tright" : thinRight ? "-right" : "")
					+ (color == null ? "" : "-c" + color);
			if ("line".equals(styleId)) return null;
			CellStyle style = iStyles.get(styleId);
			if (style == null) {
				style = iWorkbook.createCellStyle();
				if (thickLeft) {
					style.setBorderLeft(BorderStyle.THICK);
					style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
				} else if (thinLeft) {
					style.setBorderLeft(BorderStyle.THIN);
					style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				}
				if (thickTop) {
					style.setBorderTop(BorderStyle.THICK);
					style.setTopBorderColor(IndexedColors.BLACK.getIndex());
				} else if (thinTop) {
					style.setBorderTop(BorderStyle.THIN);
					style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				}
				if (thickRight) {
					style.setBorderRight(BorderStyle.THICK);
					style.setRightBorderColor(IndexedColors.BLACK.getIndex());
				} else if (thinRight) {
					style.setBorderRight(BorderStyle.THIN);
					style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				}
				if (thickBottom) {
					style.setBorderBottom(BorderStyle.THICK);
					style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
				} else if (thinBottom) {
					style.setBorderBottom(BorderStyle.THIN);
					style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
				}
				if (color != null) {
					style.setFillForegroundColor(color);
					style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				}
				style.setWrapText(true);
				style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.TOP);
		        style.setFont(getFont(false, false, false, Color.BLACK));
		        iStyles.put(styleId, style);
			}
			return style;
		}
		
		protected Sheet createSheet(String name) {
			Sheet sheet = null;
			try {
				sheet = (name == null ? iWorkbook.createSheet() : iWorkbook.createSheet(name.replace('/', '-').replace('\\','-').replace('?', ' ').replace('*', ' ').replace(':', '-').replace('[', '(').replace(']', ')')));
			} catch (IllegalArgumentException e) {
				sheet = iWorkbook.createSheet();
			}
			sheet.setDisplayGridlines(false);
			sheet.setPrintGridlines(false);
			sheet.setFitToPage(true);
			sheet.setHorizontallyCenter(true);
	        PrintSetup printSetup = sheet.getPrintSetup();
	        printSetup.setLandscape(true);
	        sheet.setAutobreaks(true);
	        printSetup.setFitHeight((short)1);
	        printSetup.setFitWidth((short)1);
	        return sheet;
		}
		
		protected void write(OutputStream output, boolean vertical) throws IOException {
			int headerWidth = (int) Math.round(256 * ApplicationProperty.TimeGridXLSHeaderWidth.doubleValue());
			int cellWidth = (int) Math.round(256 * (vertical ? ApplicationProperty.TimeGridXLSCellWidthVertical.doubleValue() : ApplicationProperty.TimeGridXLSCellWidth.doubleValue()));
			for (int s = 0; s < iWorkbook.getNumberOfSheets(); s++) {
				Sheet sheet = iWorkbook.getSheetAt(s);
				sheet.setColumnWidth(0, headerWidth);
				for (short col = 1; col <= sheet.getRow(0).getLastCellNum(); col++) {
					sheet.setColumnWidth(col, cellWidth);
				}
			}
			iWorkbook.write(output);
			iWorkbook.close();
		}
	}
}