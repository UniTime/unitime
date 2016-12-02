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
package org.unitime.timetable.export;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.unitime.timetable.export.Exporter.Printer;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author Tomas Muller
 */
public class PDFPrinter implements Printer {
	private static Pattern sNumber = Pattern.compile("[+-]?[0-9]*\\.?[0-9]*[a-z]?");
	private OutputStream iOutput;
	private Object[] iLastLine = null;
	private boolean iCheckLast = false;
	private PdfPTable iTable = null;
	private float[] iMaxWidth = null;
	private Set<Integer> iHiddenColumns = new HashSet<Integer>();

	List<Element> iElements = new ArrayList<Element>();
	
	public PDFPrinter(OutputStream output, boolean checkLast) {
		iOutput = output;
		iCheckLast = checkLast;
	}
	
	@Override
	public String getContentType() {
		return "application/pdf";
	}
	
	@Override
	public void hideColumn(int col) {
		iHiddenColumns.add(col);
	}

	@Override
	public void printHeader(String... fields) {
		iTable = new PdfPTable(fields.length - iHiddenColumns.size());
		iMaxWidth = new float[fields.length];
		iTable.setHeaderRows(1);
		iTable.setWidthPercentage(100);

		for (int idx = 0; idx < fields.length; idx++) {
			if (iHiddenColumns.contains(idx)) continue;
			String f = fields[idx];
			
			PdfPCell cell = new PdfPCell();
			cell.setBorder(Rectangle.BOTTOM);
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			
			Font font = PdfFont.getFont(true);
			Paragraph ch = new Paragraph(f, font);
			ch.setLeading(0f, 1f);
			cell.addElement(ch);		
			iTable.addCell(cell);
			
			float width = 0; 
			if (f.indexOf('\n')>=0) {
				for (StringTokenizer s = new StringTokenizer(f,"\n"); s.hasMoreTokens();)
					width = Math.max(width,font.getBaseFont().getWidthPoint(s.nextToken(), font.getSize()));
			} else 
				width = Math.max(width,font.getBaseFont().getWidthPoint(f, font.getSize()));
			iMaxWidth[idx] = width;
		}
	}

	@Override
	public void printLine(String... fields) {
		PdfPCellEvent setLineDashEvent = new PdfPCellEvent() {
			@Override
			public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {
				PdfContentByte cb = canvas[PdfPTable.LINECANVAS];
				cb.setLineDash(new float[] {2, 2}, 0);
			}
		};
		
		for (int idx = 0; idx < fields.length; idx++) {
			if (iHiddenColumns.contains(idx)) continue;
			String f = fields[idx];
			if (f == null || f.isEmpty() || (iCheckLast && f.equals(iLastLine == null || idx >= iLastLine.length ? null : iLastLine[idx]))) f = "";
			
			boolean number = sNumber.matcher(f).matches();

			Font font = PdfFont.getFont();
			Phrase p = new Phrase(f, PdfFont.getSmallFont());
			
			PdfPCell cell = new PdfPCell(p);
			cell.setBorder(iLastLine == null ? Rectangle.TOP : Rectangle.NO_BORDER);
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			cell.setHorizontalAlignment(number ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
			cell.setPaddingBottom(4f);
			cell.setCellEvent(setLineDashEvent);
			if (number) cell.setPaddingRight(10f);
			iTable.addCell(cell);
			
			float width = 0; 
			if (f.indexOf('\n')>=0) {
				for (StringTokenizer s = new StringTokenizer(f,"\n"); s.hasMoreTokens();)
					width = Math.max(width,font.getBaseFont().getWidthPoint(s.nextToken(), font.getSize()));
			} else 
				width = Math.max(width,font.getBaseFont().getWidthPoint(f, font.getSize()));
			iMaxWidth[idx] = Math.max(iMaxWidth[idx], width + (number ? 10 : 0));
		}
		iLastLine = fields;
	}
	
	public void printLine(A... fields) {
		PdfPCellEvent setLineDashEvent = new PdfPCellEvent() {
			@Override
			public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {
				PdfContentByte cb = canvas[PdfPTable.LINECANVAS];
				cb.setLineDash(new float[] {2, 2}, 0);
			}
		};
		
		for (int idx = 0; idx < fields.length; idx++) {
			if (iHiddenColumns.contains(idx)) continue;
			A f = fields[idx];
			if (f == null || f.isEmpty() || (iCheckLast && f.equals(iLastLine == null || idx >= iLastLine.length ? null : iLastLine[idx]))) {
				f = new A();
				if (fields[idx] != null && fields[idx].has(F.NOSEPARATOR))
					f.set(F.NOSEPARATOR);
			}
			
			PdfPCell cell = new PdfPCell();
			float rpad = 0f;
			cell.setBorder(iLastLine == null && !f.has(F.NOSEPARATOR) ? Rectangle.TOP : Rectangle.NO_BORDER);
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			if (f.has(F.RIGHT)) {
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setPaddingRight(10f);
				rpad = 10f;
			} else if (f.has(F.CENTER)) {
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			} else {
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			}
			cell.setPaddingBottom(4f);
			cell.setCellEvent(setLineDashEvent);
			
			if (f.hasImage()) {
				try {
					cell.addElement(new Chunk(f.getImage(), 0f, 0f));
					iMaxWidth[idx] = Math.max(iMaxWidth[idx], f.getImage().getScaledWidth());
				} catch (Exception e) {}
			}

			if (f.hasText()) {
				Font font = PdfFont.getFont(f.has(F.BOLD), f.has(F.ITALIC));
				if (f.getColor() != null) font.setColor(f.getColor());
				if (f.has(F.UNDERLINE)) font.setStyle(Font.UNDERLINE);
				Paragraph ch = new Paragraph(f.getText(), font);
				ch.setLeading(0f, 1f);
				cell.addElement(ch);
				float width = f.getWidth(font);
				iMaxWidth[idx] = Math.max(iMaxWidth[idx], width + rpad);
			}
			
			if (f.hasChunks()) {
				boolean underline = false;
				for (A g: f.getChunks()) {
					if (g.hasImage()) {
						try {
							cell.addElement(new Chunk(g.getImage(), 0f, 0f));
							iMaxWidth[idx] = Math.max(iMaxWidth[idx], g.getImage().getScaledWidth());
						} catch (Exception e) {}
					}
					if (g.hasText()) {
						Font font = PdfFont.getFont(g.has(F.BOLD), g.has(F.ITALIC));
						if (g.getColor() != null) font.setColor(g.getColor());
						if (g.has(F.UNDERLINE)) font.setStyle(Font.UNDERLINE);
						Paragraph ch = new Paragraph(g.getText(), font);
						ch.setLeading(0f, underline ? 1.4f : 1.0f);
						cell.addElement(ch);
						float width = g.getWidth(font);
						iMaxWidth[idx] = Math.max(iMaxWidth[idx], width + rpad);
						underline = g.has(F.UNDERLINE);
					}
				}
			}
			
			iTable.addCell(cell);
		}
		iLastLine = fields;
	}
	
	@Override
	public void flush() {
		iLastLine = null;
	}

	@Override
	public void close() throws IOException {
		try {
			float width = 0;
			float[] w = new float[iMaxWidth.length - iHiddenColumns.size()]; int wi = 0;
			for (int i = 0; i < iMaxWidth.length; i++)
				if (!iHiddenColumns.contains(i)) { width += 15f + iMaxWidth[i]; w[wi++] = iMaxWidth[i]; }
			Document document = new Document(new Rectangle(60f + width, 60f + width * 0.75f), 30f, 30f, 30f, 30f);
			PdfWriter writer = PdfWriter.getInstance(document, iOutput);
			writer.setPageEvent(new PdfEventHandler());
			document.open();
			iTable.setWidths(w);
			document.add(iTable);
			document.close();
		} catch (DocumentException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	public static class A {
		private List<A> iChunks = null;
		private String iText = null;
		private int iFlag = 0;
		private String iColor = null;
		private Image iImage = null;
		private Float iMaxWidth = null;
		
		public A() {}
		
		public A(String text, F... flags) {
			iText = text;
			for (F f: flags)
				iFlag = f.set(iFlag);
		}
		
		public A(A... chunks) {
			iChunks = new ArrayList<A>();
			for (A ch: chunks)
				iChunks.add(ch);
		}
		
		public A(java.awt.Image image) {
			try {
				iImage = Image.getInstance(image, Color.WHITE);
			} catch (Exception e) {}
		}
		
		public A(Image image) {
			iImage = image;
		}
		
		public void setColor(String color) {
			if (color != null && color.startsWith("#")) color = color.substring(1);
			iColor = color;
		}
		public boolean hasColor() { return iColor != null && !iColor.isEmpty(); }
		public Color getColor() {
			try {
				return hasColor() ? new Color(Integer.parseInt(iColor,16)) : Color.BLACK; 
			} catch (Exception e) {
				e.printStackTrace();
				return Color.BLACK;
			}
		}
		
		public String getText() { return iText == null ? "" : iText; }
		public boolean hasText() { return iText != null && !iText.isEmpty(); }
		
		public boolean hasImage() { return iImage != null; }
		public Image getImage() { return iImage; }
		
		public A add(A chunk) {
			if (iChunks == null) iChunks = new ArrayList<A>();
			iChunks.add(chunk);
			return this;
		}
		public List<A> getChunks() { return iChunks; }
		public boolean hasChunks() { return iChunks != null && !iChunks.isEmpty(); }
		public int size() { return iChunks == null ? 0 : iChunks.size(); }
		public A getChunk(int idx) { return iChunks.get(idx); }
		
		public boolean has(F flag) { return flag.in(iFlag); }
		public A set(F flag) { iFlag = flag.set(iFlag); return this; }
		
		public boolean isEmpty() { return !hasChunks() && !hasText() && !hasImage(); }
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof A)) return false;
			A a = (A)o;
			if (size() != a.size() || !getText().equals(a.getText())) return false;
			for (int i = 0; i < size(); i++)
				if (!getChunk(i).equals(a.getChunk(i))) return false;
			return true;
		}
		
		public A bold() { set(F.BOLD); return this; }
		public A italic() { set(F.ITALIC); return this; }
		public A underline() { set(F.UNDERLINE); return this; }
		public A color(String color) { setColor(color); return this; }
		public A center() { set(F.CENTER); return this; }
		public A right() { set(F.RIGHT); return this; }
		
		public boolean hasMaxWidth() { return iMaxWidth != null; }
		public A maxWidth(Float maxWidth) { iMaxWidth = maxWidth; return this; }
		public Float getMaxWidth() { return iMaxWidth; }
		
		private float width(String text, Font font) {
			float ret = font.getBaseFont().getWidthPoint(text, font.getSize());
			return (iMaxWidth == null ? ret : Math.min(iMaxWidth, ret));
		}
		
		public float getWidth(Font font) {
			if (hasText()) {
				if (getText().indexOf('\n')>=0) {
					float width = 0f;
					for (StringTokenizer s = new StringTokenizer(getText(),"\n"); s.hasMoreTokens();)
						width = Math.max(width, width(s.nextToken(), font));
					return width;
				} else {
					return width(getText(), font);
				}
			} else {
				return 0f;
			}
		}
	} 
	
	public static enum F {
		ITALIC, BOLD, UNDERLINE, RIGHT, CENTER, NOSEPARATOR,
		;
		
		public int flag() { return 1 << ordinal(); }
		public boolean in(int flags) {
			return (flags & flag()) != 0;
		}
		public int set(int flags) {
			return (in(flags) ? flags : flags + flag());
		}
		public int clear(int flags) {
			return (in(flags) ? flags - flag() : flags);
		}
	}

}
