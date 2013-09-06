/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.export;

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
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PDFPrinter implements Printer {
	private static Pattern sNumber = Pattern.compile("[+-]?[0-9]*\\.?[0-9]*[a-z]?");
	private OutputStream iOutput;
	private String[] iLastLine = null;
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
			cell.addElement(new Chunk(f, font));		
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

}
