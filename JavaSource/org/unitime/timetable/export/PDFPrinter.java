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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.unitime.timetable.export.Exporter.Printer;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.util.Formats.Format;
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
	
	protected Font font(A f) {
		Font font = PdfFont.getFont(f.has(F.BOLD), f.has(F.ITALIC));
		if (f.getColorValue() != null) font.setColor(f.getColorValue());
		if (f.has(F.UNDERLINE)) font.setStyle(Font.UNDERLINE);
		return font;
	}
	
	protected float print(PdfPCell cell, Paragraph parent, A f, boolean underline) {
		Font font = font(f);
		if (f.has(F.UNDERLINE)) underline = true;
		float width = 0f;
		
		if (parent == null) {
			parent = new Paragraph();
			cell.addElement(parent);
			if (f.has(F.RIGHT)) {
				parent.setAlignment(Element.ALIGN_RIGHT);
			} else if (f.has(F.CENTER)) {
				parent.setAlignment(Element.ALIGN_CENTER);
			}
		}
		parent.setLeading(0f, underline ? 1.4f : 1.0f);
		
		if (f.hasImage()) {
			try {
				Chunk ch = new Chunk(f.getImage(), 0f, 0f);
				width += f.getImage().getScaledWidth();
				parent.add(ch);
			} catch (Exception e) {}
		}
		if (f.hasText()) {
			Chunk ch = new Chunk(f.getText(), font);
			width += f.getWidth(font);
			parent.add(ch);
		}
		
		if (f.hasChunks()) {
			if (f.has(F.INLINE)) {
				for (A g: f.getChunks()) {
					width += print(cell, parent, g, underline);
				}
			} else {
				for (A g: f.getChunks()) {
					Paragraph line = new Paragraph();
					if (g.has(F.RIGHT)) {
						line.setAlignment(Element.ALIGN_RIGHT);
					} else if (g.has(F.CENTER)) {
						line.setAlignment(Element.ALIGN_CENTER);
					}
					float w = print(cell, line, g, false);
					if (w == 0f) line.add(new Chunk(" ", font));
					width = Math.max(width, w);
					cell.addElement(line);
				}
			}
		}
		
		if (f.hasWidth()) return f.getWidth();
		return width;
	}
	
	public void printHeader(A... fields) {
		printHeader(0, 1, fields);
	}
	
	public void printHeader(int row, int rows, A... fields) {
		if (row == 0) {
			int cols = 0;
			for (A f: fields)
				cols += f.getColSpan();
			iTable = new PdfPTable(cols - iHiddenColumns.size());
			iMaxWidth = new float[cols];
			iTable.setHeaderRows(rows);
			iTable.setWidthPercentage(100);
			for (int i = 0; i < cols; i++)
				iMaxWidth[i] = 50f;
		}
		
		int col = 0;
		for (int idx = 0; idx < fields.length; idx++) {
			if (iHiddenColumns.contains(idx)) continue;
			A f = fields[idx];
			
			PdfPCell cell = new PdfPCell();
			if (row + f.getRowSpan() == rows)
				cell.setBorder(Rectangle.BOTTOM);
			else
				cell.setBorder(Rectangle.NO_BORDER);
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			
			float rpad = 0f;
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
			
			cell.setColspan(f.getColSpan());
			cell.setRowspan(f.getRowSpan());
			
			if (f.hasBackground()) {
				cell.setBackgroundColor(f.getBackground());
			}
			
			float width = print(cell, null, f, false);
			if (f.getColSpan() == 1)
				iMaxWidth[col] = Math.max(iMaxWidth[col], width + rpad);
			
			col += cell.getColspan();
			iTable.addCell(cell);
		}
		iLastLine = null;
	}
	
	public void printLine(A... fields) {
		PdfPCellEvent setLineDashEvent = new PdfPCellEvent() {
			@Override
			public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {
				PdfContentByte cb = canvas[PdfPTable.LINECANVAS];
				cb.setLineDash(new float[] {2, 2}, 0);
			}
		};
		
		int col = 0;
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
			
			if (f.getColSpan() > 1)
				cell.setColspan(f.getColSpan());
			if (f.getRowSpan() > 1)
				cell.setRowspan(f.getRowSpan());
			
			if (f.hasBackground()) {
				cell.setBackgroundColor(f.getBackground());
			}
			
			float width = print(cell, null, f, false);
			if (f.getColSpan() == 1 && !f.has(F.WRAP))
				iMaxWidth[col] = Math.max(iMaxWidth[col], width + rpad);
			
			col += cell.getColspan();
			iTable.addCell(cell);
		}
		iLastLine = fields;
	}
	
	@Override
	public void flush() {
		iLastLine = null;
	}
	
	private Document iDocument;
	private PdfWriter iWriter;
	
	public void flushTable(String tableName) throws IOException {
		try {
			float width = 0;
			float[] w = new float[iMaxWidth.length - iHiddenColumns.size()]; int wi = 0;
			for (int i = 0; i < iMaxWidth.length; i++) {
				float mw = iMaxWidth[i];
				if (!iHiddenColumns.contains(i)) { width += 15f + mw; w[wi++] = mw; }
			}
			if (iDocument == null) {
				iDocument = new Document(new Rectangle(60f + width, 60f + width * 0.75f), 30f, 30f, 30f, 30f);
				iWriter = PdfWriter.getInstance(iDocument, iOutput);
				iWriter.setPageEvent(new PdfEventHandler());
				iDocument.open();
			} else {
				iDocument.newPage();
				iDocument.setPageSize(new Rectangle(60f + width, 60f + width * 0.75f));
			}
			iTable.setWidths(w);
			iDocument.add(new Paragraph(tableName, PdfFont.getBigFont(true)));
			iDocument.add(iTable);
			iTable = null;
		} catch (DocumentException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			if (iDocument != null) {
				iDocument.close();
			} else if (iTable != null) {
				float width = 0;
				float[] w = new float[iMaxWidth.length - iHiddenColumns.size()]; int wi = 0;
				for (int i = 0; i < iMaxWidth.length; i++) {
					float mw = iMaxWidth[i];
					if (!iHiddenColumns.contains(i)) { width += 15f + mw; w[wi++] = mw; }
				}
				Document document = new Document(new Rectangle(60f + width, 60f + width * 0.75f), 30f, 30f, 30f, 30f);
				PdfWriter writer = PdfWriter.getInstance(document, iOutput);
				writer.setPageEvent(new PdfEventHandler());
				document.open();
				iTable.setWidths(w);
				document.add(iTable);
				document.close();				
			}
		} catch (DocumentException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	public static class A {
		private List<A> iChunks = null;
		private String iText = null;
		private int iFlag = 0;
		private String iColor = null;
		private BufferedImage iBufferedImage = null;
		private Image iImage = null;
		private Float iMaxWidth = null;
		private Double iNumber = null;
		private Date iDate = null;
		private Format<?> iFormat = null;
		private String iBackground = null;
		private int iRowSpan = 1, iColSpan = 1;
		private Integer iWidth = null;
		
		public A() {}
		
		public A(String text, F... flags) {
			iText = text;
			for (F f: flags)
				iFlag = f.set(iFlag);
			if (has(F.FIX_BR))
				iText = (text == null ? null : text.replace("<br>", "\n")); 
		}
		
		public A(Date date, Format<Date> format, F... flags) {
			iDate = date; iFormat = format;
			iText = (date == null ? null : format.format(date));
			for (F f: flags)
				iFlag = f.set(iFlag);
		}
		
		public A(Number number, Format<Number> format, F... flags) {
			iNumber = (number == null ? null : Double.valueOf(number.doubleValue())); iFormat = format;
			iText = (number == null ? null : format.format(number));
			for (F f: flags)
				iFlag = f.set(iFlag);
		}
		
		public A(Number number, F... flags) {
			iNumber = (number == null ? null : Double.valueOf(number.doubleValue())); iFormat = null;
			iText = (number == null ? null : number.toString());
			for (F f: flags)
				iFlag = f.set(iFlag);
		}
		
		public A(A... chunks) {
			iChunks = new ArrayList<A>();
			for (A ch: chunks)
				iChunks.add(ch);
		}
		
		public A(Collection<A> chunks) {
			iChunks = new ArrayList<A>(chunks);
		}
		
		public A(java.awt.Image image) {
			try {
				if (image instanceof BufferedImage)
					iBufferedImage = (BufferedImage)image;
				iImage = Image.getInstance(image, Color.WHITE);
			} catch (Exception e) {}
		}
		
		public A(Image image) {
			iImage = image;
		}
		
		public A(CellInterface cell, LineInterface line, A parent, int index) {
			
		}
		
		public void setColor(String color) {
			if (color != null && color.startsWith("#")) color = color.substring(1);
			iColor = color;
		}
		public boolean hasColor() { return iColor != null && !iColor.isEmpty(); }
		public String getColor() { return iColor; }
		public Color getColorValue() {
			if ("green".equals(iColor)) return Color.GREEN;
			if ("red".equals(iColor)) return Color.RED;
			if ("blue".equals(iColor)) return Color.BLUE;
			if ("black".equals(iColor)) return Color.BLACK;
			if ("gray".equals(iColor)) return Color.GRAY;
			try {
				return hasColor() ? new Color(Integer.parseInt(iColor,16)) : Color.BLACK; 
			} catch (Exception e) {
				e.printStackTrace();
				return Color.BLACK;
			}
		}
		
		public void setBackground(String color) {
			if (color != null && color.startsWith("#")) color = color.substring(1);
			iBackground = color;
		}
		public boolean hasBackground() { return iBackground != null && !iBackground.isEmpty(); }
		public Color getBackground() {
			if ("green".equals(iBackground)) return Color.GREEN;
			if ("red".equals(iBackground)) return Color.RED;
			if ("blue".equals(iBackground)) return Color.BLUE;
			if ("black".equals(iBackground)) return Color.BLACK;
			if ("gray".equals(iBackground)) return Color.GRAY;
			try {
				return hasBackground() ? new Color(Integer.parseInt(iBackground,16)) : Color.WHITE; 
			} catch (Exception e) {
				e.printStackTrace();
				return Color.BLACK;
			}
		}
		
		public String getText() { return iText == null ? "" : iText; }
		public boolean hasText() { return iText != null && !iText.isEmpty(); }
		public void setText(String text) { iText = text; }
		
		public boolean hasImage() { return iImage != null; }
		public Image getImage() { return iImage; }
		
		public Integer getWidth() { return iWidth; }
		public void setWidth(Integer width) { iWidth = width; }
		public boolean hasWidth() { return iWidth != null; }

		
		public boolean hasBufferedImage() { return iBufferedImage != null; }
		public BufferedImage getBufferedImage() { return iBufferedImage; }
		
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
		public A clear(F flag) { iFlag = flag.clear(iFlag); return this; }
		
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
		public A inline() { set(F.INLINE); return this; }
		public A wrap() { set(F.WRAP); return this; }
		
		public boolean hasMaxWidth() { return iMaxWidth != null; }
		public A maxWidth(Float maxWidth) { iMaxWidth = maxWidth; return this; }
		public Float getMaxWidth() { return iMaxWidth; }
		
		public boolean isNumber() { return iNumber != null; }
		public Double getNumber() { return iNumber; }
		public boolean isDate() { return iDate != null; }
		public Date getDate() { return iDate; }
		public String getPattern() { return iFormat == null ? null : iFormat.toPattern(); }
		
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
		
		public int getRowSpan() { return iRowSpan; }
		public void setRowSpan(int rowSpan) { iRowSpan = rowSpan; }
		public int getColSpan() { return iColSpan; }
		public void setColSpan(int colSpan) { iColSpan = colSpan; }
	} 
	
	public static enum F {
		ITALIC, BOLD, UNDERLINE, RIGHT, CENTER, NOSEPARATOR, INLINE, FIX_BR, WRAP
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
	
	public A[] toA(LineInterface line, boolean header) {
		List<A> ret = new ArrayList<A>();
		if (line.hasCells())
			for (CellInterface cell: line.getCells()) {
				A a = toA(cell, line, null, 0);
				if (!a.hasChunks() && (a.getText() == null || a.getText().isEmpty()))
					a.setText(" ");
				if (header) {
					a.bold();
				}
				ret.add(a);
			}
		return ret.toArray(new A[0]);
	}
	
	protected void applyStyle(A a, String styles) {
		for (String style: styles.split(";")) {
			if (style.indexOf(':') < 0) continue;
			String key = style.substring(0, style.indexOf(':')).trim();
			String value = style.substring(style.indexOf(':') + 1).trim();
			if ("font-weight".equalsIgnoreCase(key) && "bold".equalsIgnoreCase(value))
				a.bold();
			else if ("font-style".equalsIgnoreCase(key) && "italic".equalsIgnoreCase(value))
				a.italic();
			else if ("color".equalsIgnoreCase(key) && !"inherit".equals(value))
				a.setColor(value);
			else if ("background".equalsIgnoreCase(key))
				a.setBackground(value);
		}
	}
	
	protected A createCell(CellInterface cell) {
		try {
			if (cell.hasImage() && cell.getImage().getGenerator() != null) {
				return new A((java.awt.Image)cell.getImage().getGenerator().generate());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new A();
	}
	
	protected A toA(CellInterface cell, LineInterface line, A parent, int index) {
		A a = createCell(cell);
		if (cell.hasWidth()) a.setWidth(cell.getWidth());
		a.inline();
		if (parent != null && !cell.isInline() && index > 0)
			parent.clear(F.INLINE);
		if (parent != null && parent.has(F.BOLD)) a.bold();
		if (parent != null && parent.has(F.ITALIC)) a.italic();
		if (parent != null && parent.getColorValue() != null) a.setColor(parent.getColor());
		if (parent == null && !cell.hasNoWrap()) a.wrap();
		a.setColSpan(cell.getColSpan()); a.setRowSpan(cell.getRowSpan());
		if (cell.hasColor()) a.color(cell.getColor());
		if (parent == null) {
			if (line.hasBgColor()) a.setBackground(line.getBgColor());
			if (line.hasStyle()) applyStyle(a, line.getStyle());
		}
		if (cell.hasStyle()) applyStyle(a, cell.getStyle());
		if (cell.getTextAlignment() == Alignment.CENTER)
			a.center();
		else if (cell.getTextAlignment() == Alignment.RIGHT)
			a.right();
		if (a.getImage() != null) {
		} else if (cell.hasAria()) {
			a.setText(cell.getAria());
		} else if (cell.hasText() && !cell.isHtml()) {
			a.setText(cell.getText());
		} else if (cell.hasTitle()) {
			a.setText(cell.getTitle());
		} else if (cell.hasImage()) {
			if (cell.getImage().hasTitle())
				a.setText(cell.getImage().getTitle());
		}
		if (cell.hasIndent())
			for (int i = 0; i < cell.getIndent(); i++)
				a.setText("  " + (a.getText() == null ? "" : a.getText()));
		if (cell.hasItems() && a.getImage() == null && !cell.hasAria()) { 
			int i = 0;
			for (CellInterface c: cell.getItems()) {
				a.add(toA(c, line, a, i++));
			}
		}
		return a;
	}

}
