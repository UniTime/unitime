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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
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
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:timetable.pdf")
public class ExportTimetablePDF extends TableExporter {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static Format<Date> sDateFormatMeeting = Formats.getDateFormat(Formats.Pattern.TIMETABLE_GRID_DATE);
	protected static Format<Number> sUtilizationFormat = Formats.getConcurrentNumberFormat(Formats.Pattern.UTILIZATION);
	protected static int sHeaderWidth = 100;
	protected static int sLineHeight = 13;

	@Autowired private ApplicationContext applicationContext;

	@Override
	public String reference() {
		return "timetable.pdf";
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
		helper.setup("application/pdf", reference(), true);
		try {
			Document document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document, helper.getOutputStream());
			writer.setPageEvent(new PdfEventHandler());
			int index = 0;
			int margin = 50;
			int width = 1000;
			for (TimetableGridModel model: response.getModels())
				width = Math.max(width, pageWidth(filter, model, response.getWeekOffset()));
			int displayMode = Integer.valueOf(filter.getParameterValue("dispMode", "0")); 
			int height = (displayMode == 2 ? (2*margin + width) * 22 / 17 - 2*margin : (2*margin + width) * 17 / 22 - 2*margin);
			for (TimetableGridModel model: response.getModels())
				height = Math.max(height, pageHeight(filter, model, response.getWeekOffset(), true));
			document.setPageSize(new Rectangle(width + 2*margin, height + 2*margin));
			document.setMargins(margin, margin, margin, margin);
			document.open();
			int used = 0;
			int tm = (Integer.valueOf(filter.getParameterValue("dispMode", "0")) == 0 ? 0 : 25);
			for (TimetableGridModel model: response.getModels()) {
				if (used > 0 && pageHeight(filter, model, response.getWeekOffset(), false) > height - used) {
					document.newPage(); used = 0;
				}
				TimetableGrid tg = new TimetableGrid(filter, model, index++, width, response.getWeekOffset(), used == 0);
				PdfContentByte canvas = writer.getDirectContent();
				tg.print(canvas, margin, margin + used, height + 2 * margin, null);
				used += tg.getHeight() + tm;
			}
			if (document != null)
				document.close();
		} catch (DocumentException e) {
			throw new IOException(e.getMessage(), e);
		}
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
		
		protected void print(PdfContentByte canvas, int x, int y, int pageHeight, P parent) throws DocumentException {
			if (isStyle("unitime-TimetableGrid")) {
				setHeight(getHeight() - 2);
				setWidth(getWidth() - 1);
			}
			if (isStyle("vertical-separator")) {
				setHeight(parent.getHeight());
				setHeight(getHeight() - 1);
			}
			if (isStyle("horizontal-separator")) {
				setWidth(parent.getWidth());
				setWidth(getWidth() - 1);
			}
			Rectangle rect = new Rectangle(x + getLeft(), pageHeight - (y + getTop()), x + getLeft() + getWidth() - 1, pageHeight - (y + getTop() + getHeight() - 1));
			if (parent != null && parent.isStyle("meeting")) {
				rect.setBackgroundColor(Color.WHITE);
			}
			if (iBgColor != null) {
				Pattern p = Pattern.compile("rgb\\(([0-9]+),([0-9]+),([0-9]+)\\)");
				Matcher m = p.matcher(iBgColor);
				if (m.matches()) {
					rect.setBackgroundColor(new Color(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))));
				}
			}
			if (parent != null && parent.isStyle("meeting")) {
				if (parent.iBgColor != null) {
					Pattern p = Pattern.compile("rgb\\(([0-9]+),([0-9]+),([0-9]+)\\)");
					Matcher m = p.matcher(parent.iBgColor);
					if (m.matches()) {
						Color color = new Color(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
						if (isStyle("header"))
							rect.setBackgroundColor(color.darker());
						else
							rect.setBackgroundColor(color);
					}
				}
			}
			if (rect.getBackgroundColor() != null)
				canvas.rectangle(rect);
	        
	        if (hasText()) {
	        	boolean bold = isStyle("grid-name", "horizontal-header", "vertical-header");
	        	boolean italics = iItalics;
	        	int alignment = Element.ALIGN_CENTER;
	        	Font font = PdfFont.getSmallFont(bold, italics);
	        	if (iColor != null) {
	        		Pattern p = Pattern.compile("rgb\\(([0-9]+),([0-9]+),([0-9]+)\\)");
					Matcher m = p.matcher(iColor);
					if (m.matches()) {
						font.setColor(new Color(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))));
					}
	        	}
	        	int idx = 0;
	        	for (String line: getText().split("\\n")) {
		        	Phrase p = new Phrase(line, font);
		        	ColumnText ct = new ColumnText(canvas);
		        	ct.setSimpleColumn(p, x + getLeft() + 2, pageHeight - (y + getTop() + 2 + sLineHeight * idx), x + getLeft() + getWidth() - 4, pageHeight - (y + getTop() + 2 + sLineHeight * idx + 15f), 8f, alignment);
		        	ct.go();
		        	idx ++;
	        	}
	        }
	        
	        if (isStyle("footer") && parent.isStyle("meeting")) {
	        	canvas.setColorStroke(Color.BLACK);
	        	canvas.moveTo(x + getLeft(), pageHeight - (y + getTop()));
	        	canvas.lineTo(x + getLeft(), pageHeight - (y + getTop() + getHeight() - 1));
	        	canvas.lineTo(x + getLeft() + getWidth() - 1, pageHeight - (y + getTop() + getHeight() - 1));
	        	canvas.lineTo(x + getLeft() + getWidth() - 1, pageHeight - (y + getTop()));
	        	canvas.moveTo(x + getLeft(), pageHeight - (y + getTop()));
	        	canvas.closePathStroke();
	        }
	        
	        if (isStyle("header") && parent.isStyle("meeting")) {
	        	canvas.setColorStroke(Color.BLACK);
	        	canvas.moveTo(x + getLeft(), pageHeight - (y + getTop() + getHeight()));
	        	canvas.lineTo(x + getLeft(), pageHeight - (y + getTop()));
	        	canvas.lineTo(x + getLeft() + getWidth() - 1, pageHeight - (y + getTop()));
	        	canvas.lineTo(x + getLeft() + getWidth() - 1, pageHeight - (y + getTop() + getHeight()));
	        	canvas.moveTo(x + getLeft(), pageHeight - (y + getTop() + getHeight()));
	        	canvas.closePathStroke();
	        }
			if (isStyle("vertical-separator")) {
				canvas.setColorStroke(Color.GRAY);
	        	canvas.moveTo(x + getLeft(), pageHeight - (y + getTop() + getHeight()));
	        	canvas.lineTo(x + getLeft(), pageHeight - (y + getTop()));
	        	canvas.moveTo(x + getLeft(), pageHeight - (y + getTop() + getHeight()));
	        	canvas.closePathStroke();
			}
			if (isStyle("horizontal-separator")) {
				canvas.setColorStroke(Color.GRAY);
				canvas.moveTo(x + getLeft(), pageHeight - (y + getTop()));
	        	canvas.lineTo(x + getLeft() + getWidth(), pageHeight - (y + getTop()));
	        	canvas.moveTo(x + getLeft(), pageHeight - (y + getTop()));
	        	canvas.closePathStroke();
			}
			if (isStyle("header-interval")) {
				canvas.setColorStroke(Color.GRAY);
				canvas.moveTo(x + getLeft() + getWidth(), pageHeight - (y + getTop()));
				canvas.lineTo(x + getLeft(), pageHeight - (y + getTop()));
				canvas.lineTo(x + getLeft(), pageHeight - (y + getTop() + getHeight()));
				canvas.moveTo(x + getLeft() + getWidth(), pageHeight - (y + getTop()));
				canvas.closePathStroke();
			}
			if (isStyle("unitime-TimetableGrid")) {
				canvas.setColorStroke(Color.GRAY);
	        	canvas.moveTo(x + getLeft(), pageHeight - (y + getTop() + getHeight()));
	        	canvas.lineTo(x + getLeft(), pageHeight - (y + getTop()));
	        	canvas.lineTo(x + getLeft() + getWidth() - 1, pageHeight - (y + getTop()));
	        	canvas.lineTo(x + getLeft() + getWidth() - 1, pageHeight - (y + getTop() + getHeight()));
	        	canvas.lineTo(x + getLeft(), pageHeight - (y + getTop() + getHeight()));
	        	canvas.closePathStroke();
			}
	        
	        for (P p: iContent) {
	        	p.print(canvas, x + getLeft(), y + getTop(), pageHeight, this);
	        }
		}
	}
	
	protected static class TimetableGrid extends P {
		private List<Meeting> iMeetings = new ArrayList<Meeting>();
		private List<Background> iBackbrounds = new ArrayList<Background>();
		private int iCellWidth;
		
		public TimetableGrid(FilterInterface filter, final TimetableGridModel model, int index, int pageWidth, int weekOffset, boolean showHeader) {
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
				int headerLines = (showHeader ? 2 : 0);
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
				iCellWidth = (pageWidth - sHeaderWidth) / (nrDays * nrTimes);
				if (iCellWidth < 50) iCellWidth = 50;
				setSize(sHeaderWidth + (nrDays * nrTimes) * iCellWidth + 2 * nrDays, (headerLines + nrLines) * sLineHeight + 1);
				
				if (headerLines > 0) {
					P name = new P("grid-name");
					name.setSize(sHeaderWidth, headerLines * sLineHeight);
					add(name);
				}
				
				P verticalHeader = new P("vertical-header");
				verticalHeader.setSize(sHeaderWidth, nrLines * sLineHeight + 1);
				add(verticalHeader, 0, headerLines * sLineHeight);
				
				P panel = new P("timetable-panel");
				panel.setSize((nrDays * nrTimes) * iCellWidth + 2 * nrDays, nrLines * sLineHeight + 1);
				if (headerLines > 0) {
					P horizontalHeader = new P("horizontal-header");
					horizontalHeader.setSize((nrDays * nrTimes) * iCellWidth + 2 * nrDays, headerLines * sLineHeight);
					add(horizontalHeader, sHeaderWidth, 0);
					
					for (int i = 0; i < 7; i++) {
						int d = (i + weekOffset) % 7;
						if (!hasDay[d]) continue;
						for (int t = 0; t < nrTimes; t++) {
							int j = dayIndex[d] * nrTimes + t;
							P hi = new P("header-interval");
							hi.setSize(iCellWidth, headerLines * sLineHeight);
							hi.setText(CONSTANTS.days()[d] + "\n" + slot2time(startSlot + t * step));
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
					lastVShead.setRight(0);
					lastVShead.setTop(0);
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
				
				add(panel, sHeaderWidth, headerLines * sLineHeight);
				P lastVSpan = new P("vertical-separator");
				lastVSpan.setRight(0);
				lastVSpan.setTop(0);
				panel.add(lastVSpan);
				
		        P hs = new P("horizontal-separator");
		        panel.add(hs, 0, 0);
		        final P hi = new P("header-interval");
		        hi.setSize(sHeaderWidth, nrLines * sLineHeight);
		        hi.setText(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
		        if (model.hasNameColor()) hi.setColor(model.getNameColor());
		        verticalHeader.add(hi, 0, 0);
		        
		        P lastHSpan = new P("horizontal-separator");
				lastHSpan.setLeft(0);
				lastHSpan.setBottom(0);
				panel.add(lastHSpan);
				P lastHShead = new P("horizontal-separator");
				lastHShead.setLeft(0);
				lastHShead.setBottom(0);
				verticalHeader.add(lastHShead);
				
				for (TimetableGridBackground cell: model.getBackgrounds()) {
		        	if (!hasDay[cell.getDay()]) continue;
		        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
		        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
		        	Background b = new Background(cell);
		        	iBackbrounds.add(b);
		        	b.setHeight(nrLines * sLineHeight);
			        int start = cell.getSlot();
			        int stop = cell.getSlot() + cell.getLength();
			        if (start < startSlot) start = startSlot;
			        if (stop  > endSlot) stop = endSlot;
			        b.setWidth(stop * iCellWidth / step - start * iCellWidth / step);
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
			        m.setHeight(1 + lines * sLineHeight);
			        int start = cell.getSlot();
			        int stop = cell.getSlot() + cell.getLength();
			        if (start < startSlot) start = startSlot;
			        if (stop  > endSlot) stop = endSlot;
			        m.setWidth(1 + (stop - startSlot) * iCellWidth / step - (start - startSlot) * iCellWidth / step);
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
				iCellWidth = (pageWidth - sHeaderWidth) / nrTimes;
				setSize(sHeaderWidth + nrTimes * iCellWidth + 2, (headerLines + nrLines) * sLineHeight + 2);
				
				final P name = new P("grid-name");
				name.setSize(sHeaderWidth, headerLines * sLineHeight);
				name.setText(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
				if (model.hasNameColor()) name.setColor(model.getNameColor());
				add(name);
				P verticalHeader = new P("vertical-header");
				verticalHeader.setSize(sHeaderWidth, nrLines * sLineHeight + 1);
				add(verticalHeader, 0, headerLines * sLineHeight);

				P horizontalHeader = new P("horizontal-header");
				horizontalHeader.setSize(nrTimes * iCellWidth + 1, headerLines * sLineHeight);
				add(horizontalHeader, sHeaderWidth, 0);
				
				P panel = new P("timetable-panel");
				panel.setSize(nrTimes * iCellWidth + 1, nrLines * sLineHeight + 1);
				add(panel, sHeaderWidth, headerLines * sLineHeight);

				for (int i = 0; i < nrTimes; i++) {
					P hi = new P("header-interval");
					hi.setSize(iCellWidth, headerLines * sLineHeight);
					hi.setText(slot2time(startSlot + i * step));
					horizontalHeader.add(hi, i * iCellWidth, 0);
					
					P vs = new P("vertical-separator");
					panel.add(vs, i * iCellWidth, 0);
				}
				P lastVSpan = new P("vertical-separator");
				lastVSpan.setRight(0);
				lastVSpan.setTop(0);
				panel.add(lastVSpan);
				P lastVShead = new P("vertical-separator");
				lastVShead.setRight(0);
				lastVShead.setTop(0);
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
				lastHSpan.setLeft(0);
				lastHSpan.setBottom(0);
				panel.add(lastHSpan);
				P lastHShead = new P("horizontal-separator");
				lastHShead.setLeft(0);
				lastHShead.setBottom(0);
				verticalHeader.add(lastHShead);
				
		        for (TimetableGridBackground cell: model.getBackgrounds()) {
		        	if (!hasDay[cell.getDay()]) continue;
		        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
		        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
		        	Background b = new Background(cell);
		        	iBackbrounds.add(b);
		        	int i = toDayIdx[cell.getDay()];
		        	b.setHeight((dayIndex[i + 1] - dayIndex[i]) * sLineHeight);
			        int start = cell.getSlot();
			        int stop = cell.getSlot() + cell.getLength();
			        if (start < startSlot) start = startSlot;
			        if (stop  > endSlot) stop = endSlot;
			        b.setWidth(stop * iCellWidth / step - start * iCellWidth / step);
			        panel.add(b, (start - startSlot) * iCellWidth / step, dayIndex[i] * sLineHeight);
				}
		        
		        for (TimetableGridCell cell: model.getCells()) {
		        	if (!hasDay[cell.getDay()]) continue;
		        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
		        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
		        	Meeting m = new Meeting(cell, showRoom, showInstructors, showTimes, showPreferences, showDate);
		        	iMeetings.add(m);
			        m.setHeight(1 + cell.getNrLines() * sLineHeight);
			        int start = cell.getSlot();
			        int stop = cell.getSlot() + cell.getLength();
			        if (start < startSlot) start = startSlot;
			        if (stop  > endSlot) stop = endSlot;
			        m.setWidth(1 + (stop - startSlot) * iCellWidth / step - (start - startSlot) * iCellWidth / step);
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
				iCellWidth = (pageWidth - sHeaderWidth) / nrColumns;
				setSize(sHeaderWidth + nrColumns * iCellWidth + 2, (headerLines + linesPerTime * nrTimes) * sLineHeight + 2);
				
				final P name = new P("grid-name");
				name.setSize(sHeaderWidth, headerLines * sLineHeight);
				name.setText(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
				if (model.hasNameColor()) name.setColor(model.getNameColor());
				add(name);
				
				P verticalHeader = new P("vertical-header");
				verticalHeader.setSize(sHeaderWidth, linesPerTime * nrTimes * sLineHeight + 1);
				add(verticalHeader, 0, headerLines * sLineHeight);
				
				P horizontalHeader = new P("horizontal-header");
				horizontalHeader.setSize(nrColumns * iCellWidth + 1, headerLines * sLineHeight);
				add(horizontalHeader, sHeaderWidth, 0);
				
				P panel = new P("timetable-panel");
				panel.setSize(nrColumns * iCellWidth + 1, linesPerTime * nrTimes * sLineHeight + 1);
				add(panel, sHeaderWidth, headerLines * sLineHeight);

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
				lastVSpan.setRight(0);
				lastVSpan.setTop(0);
				panel.add(lastVSpan);
				P lastVShead = new P("vertical-separator");
				lastVShead.setRight(0);
				lastVShead.setTop(0);
				horizontalHeader.add(lastVShead);
				
		        for (int i = 0; i < nrTimes; i++) {
					P hs = new P("horizontal-separator");
					panel.add(hs, 0, sLineHeight * linesPerTime * i);

					P hi = new P("header-interval");
					hi.setSize(sHeaderWidth, linesPerTime * sLineHeight);
					hi.setText(slot2time(startSlot + step * i));
					verticalHeader.add(hi, 0, sLineHeight * linesPerTime * i);
				}
		        P lastHSpan = new P("horizontal-separator");
				lastHSpan.setLeft(0);
				lastHSpan.setBottom(0);
				panel.add(lastHSpan);
				P lastHShead = new P("horizontal-separator");
				lastHShead.setLeft(0);
				lastHShead.setBottom(0);
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
		        	b.setHeight(stop * sLineHeight * linesPerTime / step - start * sLineHeight * linesPerTime / step);
		        	int i = toDayIdx[cell.getDay()];
			        b.setWidth((colIndex[1 + i] - colIndex[i]) * iCellWidth);
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
			        m.setHeight(1 + (stop - startSlot) * sLineHeight * linesPerTime / step - (start - startSlot) * sLineHeight * linesPerTime / step);
			        m.setWidth(1 + iCellWidth * cell.getNrLines());
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
				iCellWidth = (pageWidth - sHeaderWidth) / nrTimes;
				setSize(sHeaderWidth + nrTimes * iCellWidth + 2, (headerLines + nrLines) * sLineHeight + 2);
				
				final P name = new P("grid-name");
				name.setSize(sHeaderWidth, headerLines * sLineHeight);
				name.setText(model.getName() + (comment != null && !comment.isEmpty() ? "\n" + comment : ""));
				if (model.hasNameColor()) name.setColor(model.getNameColor());
				add(name);
				
				P verticalHeader = new P("vertical-header");
				verticalHeader.setSize(sHeaderWidth, nrLines * sLineHeight + 1);
				add(verticalHeader, 0, headerLines * sLineHeight);
				
				P horizontalHeader = new P("horizontal-header");
				horizontalHeader.setSize(nrTimes * iCellWidth + 1, headerLines * sLineHeight);
				add(horizontalHeader, sHeaderWidth, 0);
				
				P panel = new P("timetable-panel");
				panel.setSize(nrTimes * iCellWidth + 1, nrLines * sLineHeight + 1);
				add(panel, sHeaderWidth, headerLines * sLineHeight);

				for (int i = 0; i < nrTimes; i++) {
					P hi = new P("header-interval");
					hi.setSize(iCellWidth, headerLines * sLineHeight);
					hi.setText(slot2time(startSlot + i * step));
					horizontalHeader.add(hi, i * iCellWidth, 0);
					
					P vs = new P("vertical-separator");
					panel.add(vs, i * iCellWidth, 0);
				}
				P lastVSpan = new P("vertical-separator");
				lastVSpan.setRight(0);
				lastVSpan.setTop(0);
				panel.add(lastVSpan);
				P lastVShead = new P("vertical-separator");
				lastVShead.setRight(0);
				lastVShead.setTop(0);
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
				lastHSpan.setLeft(0);
				lastHSpan.setBottom(0);
				panel.add(lastHSpan);
				P lastHShead = new P("horizontal-separator");
				lastHShead.setLeft(0);
				lastHShead.setBottom(0);
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
				        	b.setHeight((dayIndex[1 + d] - dayIndex[d]) * sLineHeight);
					        int start = cell.getSlot();
					        int stop = cell.getSlot() + cell.getLength();
					        if (start < startSlot) start = startSlot;
					        if (stop  > endSlot) stop = endSlot;
					        b.setWidth(stop * iCellWidth / step - start * iCellWidth / step);
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
					        m.setHeight(1 + cell.getNrLines() * sLineHeight);
					        int start = cell.getSlot();
					        int stop = cell.getSlot() + cell.getLength();
					        if (start < startSlot) start = startSlot;
					        if (stop  > endSlot) stop = endSlot;
					        m.setWidth(1 + (stop - startSlot) * iCellWidth / step - (start - startSlot) * iCellWidth / step);
					        panel.add(m, (start - startSlot) * iCellWidth / step, (dayIndex[d] + cell.getIndex()) * sLineHeight);						
						}
		        	}
		        }
			}
		}
	}
	
	protected static float textWidth(Font font, TimetableGridCell cell, boolean showRoom, boolean showInstructor, boolean showTime, boolean showPreference, boolean showDate) {
		float width = 0;
		if (cell.getNrNames() > 0) {
			for (String name: cell.getNames())
				width = Math.max(width, font.getBaseFont().getWidthPoint(name, font.getSize()));
		}
		if (showTime && cell.hasTime()) width = Math.max(width, font.getBaseFont().getWidthPoint(cell.getTime(), font.getSize()));
        if (showDate && cell.hasDate()) width = Math.max(width, font.getBaseFont().getWidthPoint(cell.getDate(), font.getSize()));
        if (showRoom && cell.getNrRooms() > 0)
        	for (String room: cell.getRooms())
        		width = Math.max(width, font.getBaseFont().getWidthPoint(room, font.getSize()));
        if (showInstructor && cell.getNrInstructors() > 0)
        	for (String instructor: cell.getInstructors())
        		width = Math.max(width, font.getBaseFont().getWidthPoint(instructor, font.getSize()));
        if (showPreference && cell.hasPreference()) 
        	width = Math.max(width, font.getBaseFont().getWidthPoint(cell.getPreference().replaceAll("\\<[^>]*>",""), font.getSize()));
        return width;
	}
	
	protected int pageWidth(FilterInterface filter, final TimetableGridModel model, int weekOffset) {
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
		Font font = PdfFont.getSmallFont();
		
		int nrColumns = 0;
		for (int i = 0; i < 7; i++) {
			int d = (i + weekOffset) % 7;
			if (hasDay[d]) nrColumns += model.getNrLines(d, 1);
		}
		
		float widthPerSlot = 50f / step;
		float widthPerColumn = 50f;
        for (TimetableGridCell cell: model.getCells()) {
        	if (!hasDay[cell.getDay()]) continue;
        	if (cell.getSlot() + cell.getLength() <= startSlot) continue;
        	if (cell.getSlot() >= startSlot + step * nrTimes) continue;
        	float textWidth = textWidth(font, cell, showRoom, showInstructors, showTimes, showPreferences, showDate) + 10f;
	        int start = cell.getSlot();
	        int stop = cell.getSlot() + cell.getLength();
	        if (start < startSlot) start = startSlot;
	        if (stop  > endSlot) stop = endSlot;
        	widthPerSlot = Math.max(widthPerSlot, textWidth / (stop - start));
        	widthPerColumn = Math.max(widthPerColumn, textWidth);
        }
        
        if (displayMode == 0) {
        	if (widthPerSlot > 14) widthPerSlot = 14f;
	        return Math.round(sHeaderWidth + nrDays * step * nrTimes * widthPerSlot + 2 * nrDays);
		} else if (displayMode == 2) {
			return Math.round(sHeaderWidth + nrColumns * widthPerColumn);
		} else {
			return Math.min(1500, Math.round(sHeaderWidth + step * nrTimes * widthPerSlot));
		}
	}
	
	protected int pageHeight(FilterInterface filter, final TimetableGridModel model, int weekOffset, boolean showHeader) {
		int displayMode = Integer.valueOf(filter.getParameterValue("dispMode", "0"));
		boolean hasDay[] = { true, true, true, true, true, false, false };
		String days = filter.getParameterValue("days");
		if (days != null && days.length() == 7 && days.indexOf('1') >= 0) {
			for (int i = 0; i < 7; i++)
				if (days.charAt(i) == '1') {
					hasDay[i] = true;
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
		String comment = getComment(model);
		
		if (displayMode == 0) {
			int headerLines = (showHeader ? 2 : 0);
			int nrLines = 0;
			for (int i = 0; i < 7; i++) {
				int d = (i + weekOffset) % 7;
				int dayLines = model.getNrLines(d, 2);
				if (hasDay[d] && nrLines < dayLines) nrLines = dayLines;
			}
			return (headerLines + nrLines) * sLineHeight + 1;
		} else if (displayMode == 1) {
			int nrLines = 0;
			for (int i = 0; i < 7; i++) {
				int d = (i + weekOffset) % 7;
				if (hasDay[d]) nrLines += model.getNrLines(d, 2);
			}
			int headerLines = 1 + (comment != null && !comment.isEmpty() ? 1 : 0);
			return (headerLines + nrLines) * sLineHeight + 2;
		} else if (displayMode == 2) {
			int linesPerTime = 2;
			int headerLines = 1 + (comment != null && !comment.isEmpty() ? 1 : 0);
			return (headerLines + linesPerTime * nrTimes) * sLineHeight + 2;
		} else if (displayMode == 3) {
			int nrLines = 0;
			for (int d = 0; d < 365; d++) {
				int date = d + model.getFirstSessionDay();
				if (model.getFirstDay() >= 0 && (date < model.getFirstDay() || date > model.getFirstDay() + 6)) continue;
				int day = d % 7;
				if (hasDay[day] && model.hasDate(day, date)) {
					nrLines += model.getNrDateLines(day, date, 2);
				}
			}

			int headerLines = 1 + (comment != null && !comment.isEmpty() ? 1 : 0);
			return (headerLines + nrLines) * sLineHeight + 2;	
		}
		return 0;
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
	        iHeader.setHeight(sLineHeight * cell.getNrNames());
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
	        
	        // setText(cell.getName("\n") + "\n" + notes);
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
}