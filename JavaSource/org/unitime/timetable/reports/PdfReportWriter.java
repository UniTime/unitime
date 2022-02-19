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
package org.unitime.timetable.reports;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.reports.AbstractReport.Cell;
import org.unitime.timetable.reports.AbstractReport.Line;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPRow;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author Tomas Muller
 */
public class PdfReportWriter implements ReportWriter {
	private Document iDocument;
	private PdfWriter iWriter;
	private String iTitle, iTitle2;
	private String iSession;
	private int iPageNo = 0;
    private int iLineNo = 0;
    private boolean iEmpty = true;
    
    private String iPageId = null;
    private String iCont = null;
    private String iFooter = null;
    private String iSubject = null;
    private Line iHeaderLine[] = null;
    private PdfPTable iTable = null;
    private Font iFont = null;
    private Font iBoldFont = null;
    private boolean iNewPage = true;
    private Listener iListener = null;
	
	public PdfReportWriter(OutputStream out, String title, String title2, String subject, String session) throws DocumentException, IOException {
        iTitle = title;
        iTitle2 = title2;
        iSubject = subject;
        iSession = session;
		iFont = PdfFont.createFont(ApplicationProperty.PdfFontSizeExams.floatValue(), false, false, false);
		iBoldFont = PdfFont.createFont(ApplicationProperty.PdfFontSizeExams.floatValue(), false, true, false);
		if (out != null) open(out);
	}

	@Override
	public void setFooter(String footer) {
		iFooter = footer;
	}

	@Override
	public void setHeader(Line... line) {
		try {
			if (iTable != null) finishCurrentTable();
		} catch (DocumentException e) {
			Debug.error(e);
		}
		iHeaderLine = line;
	}

	@Override
	public Line[] getHeader() {
		return iHeaderLine;
	}

	@Override
	public void printLine(Line line) throws DocumentException {
		if (iTable == null) {
			String text = (line == null ? " " : line.render().trim());
			Paragraph p = new Paragraph(text.isEmpty() ? " " : text, iFont);
			switch (line.getAlignment()) {
			case Center: p.setAlignment(Element.ALIGN_CENTER); break;
			case Left: p.setAlignment(Element.ALIGN_LEFT); break;
			case Right: p.setAlignment(Element.ALIGN_RIGHT); break;
			}
        	iDocument.add(p);
        	iLineNo ++;
        } else {
			if (line == null) return;
			List<PdfPCell> cells = render(line, iFont);
			int colspan = 0;
			for (Iterator<PdfPCell> i = cells.iterator(); i.hasNext(); ) {
				PdfPCell cell = i.next();
				colspan += cell.getColspan();
				if (!i.hasNext() && colspan < iTable.getNumberOfColumns()) {
					cell.setColspan(iTable.getNumberOfColumns() - colspan + cell.getColspan());
				}
				iTable.addCell(cell);
			}
			iTable.completeRow();
			iLineNo ++;
		}
		iNewPage = false;
		iEmpty = false;
		if (iLineNo >= getNrLinesPerPage()) newPage();
	}

	@Override
	public void close() throws IOException, DocumentException {
		if (iEmpty) {
			Paragraph p = new Paragraph("Nothing to report.", iFont);
        	p.setAlignment(Element.ALIGN_LEFT);
        	iDocument.add(p);
		}
		lastPage();
		iDocument.close();
        iWriter.close();
	}

	@Override
	public void open(OutputStream out) throws DocumentException, IOException {
		iDocument = new Document(PageSize.LETTER.rotate(), 36, 36, 60, 48);
		iWriter = PdfWriter.getInstance(iDocument, out);
		iWriter.setPageEvent(new PdfEventHandler() {
			@Override
			public void onEndPage(PdfWriter writer, Document document) {
				try {
					printFooter(writer, document);
				} catch (DocumentException e) {
					Debug.error("Failed to print footer: " + e.getMessage() , e);
				}
			}
			@Override
			public void onStartPage(PdfWriter writer, Document document) {
				try {
					printHeader(writer, document);
				} catch (DocumentException e) {
					Debug.error("Failed to print header: " + e.getMessage() , e);
				}
			}
		});
		iDocument.addTitle(iTitle);
		iDocument.addAuthor("UniTime "+Constants.getVersion()+", www.unitime.org");
		if (iSubject != null)
			iDocument.addSubject(iSubject);
		iDocument.addCreator("UniTime "+Constants.getVersion()+", www.unitime.org");
		iDocument.open();
	}
	
	protected void printFooter(PdfWriter writer, Document document) throws DocumentException {
        PdfContentByte cb = writer.getDirectContent();
		cb.beginText();
		cb.setFontAndSize(iFont.getBaseFont(), iFont.getSize());
		cb.showTextAligned(PdfContentByte.ALIGN_LEFT, (iFooter == null ? "" : iFooter), document.left(), document.bottom() - 12, 0);
		cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, (iPageId == null || iPageId.isEmpty() ? "" : iPageId), document.right(), document.bottom() - 12, 0);
		cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "Page " + (iPageNo + 1), (document.left() + document.right()) / 2, document.bottom() - 12, 0);
		cb.endText();
        iPageNo++;
    }
	
	protected void printHeader(PdfWriter writer, Document document) throws DocumentException {
		PdfContentByte cb = writer.getDirectContent();
		if (iCont != null && !iCont.isEmpty()) {
			cb.beginText();
			cb.setFontAndSize(iFont.getBaseFont(), iFont.getSize());
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "UniTime "+Constants.getVersion(), document.left(), document.top() + 24, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, iTitle, document.right(), document.top() + 24, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_CENTER, iTitle2, (document.left() + document.right()) / 2, document.top() + 24, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, new SimpleDateFormat("EEE MMM dd, yyyy").format(new Date()), document.left(), document.top() + 16, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, iSession, document.right(), document.top() + 16, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, "("+iCont+" Continued)", document.right(), document.top() + 4, 0);
			cb.endText();
			cb.setColorStroke(Color.BLACK);
			cb.moveTo(document.left(), document.top() + 12);
			cb.lineTo(document.right(), document.top() + 12);
			cb.closePathStroke();
		} else {
			cb.beginText();
			cb.setFontAndSize(iFont.getBaseFont(), iFont.getSize());
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "UniTime "+Constants.getVersion(), document.left(), document.top() + 14, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, iTitle, document.right(), document.top() + 14, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_CENTER, iTitle2, (document.left() + document.right()) / 2, document.top() + 14, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, new SimpleDateFormat("EEE MMM dd, yyyy").format(new Date()), document.left(), document.top() + 4, 0);
			cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, iSession, document.right(), document.top() + 4, 0);
			cb.endText();
			cb.setColorStroke(Color.BLACK);
			cb.moveTo(document.left(), document.top());
			cb.lineTo(document.right(), document.top());
			cb.closePathStroke();
		}
		iLineNo = 0;
		iNewPage = true;
		if (iListener != null) iListener.headerPrinted();
    }

	@Override
	public void setPageName(String pageName) {
		iPageId = pageName;
	}

	@Override
	public void setCont(String cont) {
		iCont = cont;
	}
	
	protected void computeColumnWidths(Cell cell, List<Float> widths) {
		if (cell.getColSpan() == 0) return;
		float width = cell.getLength();
		if (width == 0f) width = 1f;
		widths.add(width / cell.getColSpan());
	}
	
	protected void computeColumnWidths(Line line, List<Float> widths) {
		if (line.getCells() != null) {
			int length = 0;
			for (Cell c: line.getCells()) {
				length += c.getLength();
				if (c.getColSpan() == 0) continue;
				for (int i = 0; i < c.getColSpan(); i++)
					widths.add(1f + ((length == 0 ? 1f : (float)length) / c.getColSpan()));
				length = 0;
			}
		}
		if (line.getLines() != null) {
			for (int i = 0; i < line.getLines().length; i++) {
				computeColumnWidths(line.getLines()[i], widths);
			}
		}
	}
	
	protected float[] getColumnWidth(Line line) {
		List<Float> cols = new ArrayList<Float>();
		computeColumnWidths(iHeaderLine[Math.max(0, iHeaderLine.length - 2)], cols);
		float[] ret = new float[cols.size()];
		for (int i = 0; i < cols.size(); i++)
			ret[i] = cols.get(i);
		return ret;
	}
	
	@Override
	public void printHeader(boolean newPage) throws DocumentException {
		finishCurrentTable();
		createNewTable();
	}
	
	protected void finishCurrentTable() throws DocumentException {
		if (iTable != null) {
			if (!iTable.getRows().isEmpty()) {
				PdfPRow row = iTable.getRow(iTable.getRows().size() - 1);
				for (PdfPCell c: row.getCells())
					if (c != null)
						c.setBorder(c.getBorder() | PdfPCell.BOTTOM);
			}
			iDocument.add(iTable);
			iTable = null;
		}
	}
	
	protected void createNewTable() throws DocumentException {
		if (iTable != null) finishCurrentTable();
		if (iHeaderLine != null && iHeaderLine.length > 0) {
			iTable = new PdfPTable(getColumnWidth(iHeaderLine[0]));
			iTable.getDefaultCell().setBorder(0);
			iTable.setWidthPercentage(100f);
			iTable.setSpacingBefore(4);
			for (Line line: iHeaderLine) {
				if (!line.isEmpty()) {
					for (PdfPCell cell: render(line, iBoldFont)) {
						cell.setBackgroundColor(Color.LIGHT_GRAY);
						iTable.addCell(cell);
					}
					iTable.completeRow();
					iLineNo ++;
				}
			}
			iTable.setHeaderRows(iTable.getRows().size());
			if (!iTable.getRows().isEmpty()) {
				PdfPRow row = iTable.getRow(iTable.getRows().size() - 1);
				for (PdfPCell c: row.getCells())
					if (c != null)
						c.setBorder(c.getBorder() | PdfPCell.BOTTOM);
				row = iTable.getRow(0);
				for (PdfPCell c: row.getCells())
					if (c != null)
						c.setBorder(c.getBorder() | PdfPCell.TOP);
			}
		}
	}

	@Override
	public void newPage() throws DocumentException {
		if (iTable != null) finishCurrentTable();
		if (!iNewPage) {
			iDocument.newPage();
		}
		createNewTable();
	}

	@Override
	public void lastPage() throws DocumentException {
		finishCurrentTable();
	}

	@Override
	public int getLineNumber() {
		return iLineNo;
	}

	@Override
	public int getNrLinesPerPage() {
		return (int)((iDocument.top() - iDocument.bottom()) / (1.5 * iFont.getSize())) - 1; 
	}

	@Override
	public int getNrCharsPerLine() {
		return 1000;
	}
	
	private String render(Cell cell) {
    	StringBuffer ret = new StringBuffer();
    	if (cell.getText() != null) {
    		if (cell.getPadding() != ' ' && cell.getText().length() < cell.getLength())
    			ret.append(cell.render());
    		else
    			ret.append(cell.getText());
    	}
    	if (cell.getCells() != null) {
    		if (cell.getText() != null) {
    			if (cell.getCellSeparator().isEmpty()) 
    				ret.append(" ");
    			else
    				ret.append(cell.getCellSeparator());
    		}
    		for (int i = 0; i < cell.getCells().length; i++) {
    			if (i > 0) {
    				if (cell.getCells()[i-1].getCellSeparator().isEmpty())
    					ret.append(" ");
    				else
    					ret.append(cell.getCells()[i-1].getCellSeparator());
    			}
    			ret.append(render(cell.getCells()[i]));
    		}
    	}
    	return ret.toString();
    }
    
    private List<PdfPCell> render(Line line, Font font) {
    	if (line == null) return null;
    	if (line.getLines() != null) {
    		List<PdfPCell> ret = new ArrayList<PdfPCell>();
    		for (int i = 0; i < line.getLines().length; i++) {
    			ret.addAll(render(line.getLines()[i], font));
    		}
    		return ret;
    	}
    	if (line.getCells() != null) {
    		List<PdfPCell> ret = new ArrayList<PdfPCell>();
    		String leftOver = null;
    		for (Cell cell: line.getCells()) {
    			if (cell.getColSpan() == 0) {
    				leftOver = ((leftOver == null || leftOver.isEmpty() ? "" : leftOver + " ") + render(cell)).trim();
    				continue;
    			}
    			PdfPCell c = new PdfPCell();
    			c.setVerticalAlignment(Element.ALIGN_TOP);
    			Paragraph ch = new Paragraph((leftOver == null || leftOver.isEmpty() ? "" : leftOver + " ") + render(cell), font);
    			if (cell.getAlignment() != null)
    				switch (cell.getAlignment()) {
    				case Center: ch.setAlignment(Element.ALIGN_CENTER); break;
    				case Left: ch.setAlignment(Element.ALIGN_LEFT); break;
    				case Right: ch.setAlignment(Element.ALIGN_RIGHT); c.setPaddingRight(5f); break;
    				}
    			c.setFixedHeight(iFont.getSize() * 1.5f);
    			ch.setLeading(0, 1);
    			c.addElement(ch);
    			c.setBorder(0);
    			c.setColspan(cell.getColSpan());
    			if (cell.getCellSeparator() != null && cell.getCellSeparator().trim().equals("|"))
    				c.setBorder(PdfPCell.RIGHT);
    			ret.add(c);
    			leftOver = null;
    		}
    		if (ret.isEmpty()) {
    			PdfPCell c = new PdfPCell();
    			c.setVerticalAlignment(Element.ALIGN_TOP);
    			c.setBorder(0);
    			c.setColspan(iTable.getNumberOfColumns());
    			c.setFixedHeight(iFont.getSize() * 1.5f);
    			ret.add(c);		
    		}
    		if (ret.size() > 0) {
    			ret.get(0).setBorder(ret.get(0).getBorder() | PdfPCell.LEFT);
    			ret.get(ret.size() - 1).setBorder(ret.get(ret.size() - 1).getBorder() | PdfPCell.RIGHT);
    		}
    		return ret;
    	}
    	return null;
    }

	@Override
	public void printSeparator(Line line) throws DocumentException{
		if (iTable != null && !iTable.getRows().isEmpty()) {
			PdfPRow row = iTable.getRow(iTable.getRows().size() - 1);
			for (PdfPCell c: row.getCells())
				if (c != null)
					c.setBorder(c.getBorder() | PdfPCell.BOTTOM);
		}
	}

	@Override
	public int getSeparatorNrLines() {
		return 0;
	}

	@Override
	public void setListener(Listener listener) {
		iListener = listener;
	}
	
	@Override
	public boolean isSkipRepeating() { return true; }
}
