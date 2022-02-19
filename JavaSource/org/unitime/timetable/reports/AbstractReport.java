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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public abstract class AbstractReport implements ReportWriter.Listener {
	protected ReportWriter iWriter;
	protected Mode iMode;
	
	protected static enum Mode {
		LegacyPdfLetter,
		LegacyPdfLedger,
		LegacyText,
		CSV,
		PDF,
		XLS,
	}
	
	public AbstractReport(Mode mode, OutputStream out, String title, String title2, String subject, String session) throws IOException, DocumentException{
		iMode = mode;
		switch (mode) {
		case LegacyPdfLetter:
			iWriter = new PdfLegacyReport(PdfLegacyReport.sModeNormal, out, title, title2, subject, session);
			break;
		case LegacyPdfLedger:
			iWriter = new PdfLegacyReport(PdfLegacyReport.sModeLedger, out, title, title2, subject, session);
			break;
		case LegacyText:
			iWriter = new PdfLegacyReport(PdfLegacyReport.sModeText, out, title, title2, subject, session);
			break;
		case CSV:
			iWriter = new CsvReportWriter(out, title, title2, subject, session);
			break;
		case PDF:
			iWriter = new PdfReportWriter(out, title, title2, subject, session);
			break;
		case XLS:
			iWriter = new XlsReportWriter(out, title, title2, subject, session);
			break;
		}
		iWriter.setListener(this);
	}
	
	public ReportWriter getWriter() { return iWriter; }
	
	protected void setFooter(String footer) {
		getWriter().setFooter(footer);
	}
	
	protected void setHeaderLine(Line... line) {
		getWriter().setHeader(line);
	}
	
	protected Line[] getHeader() {
		return getWriter().getHeader();
	}
	
	protected void setPageName(String pageName) {
		getWriter().setPageName(pageName);
	}
	
	protected void setCont(String cont) {
		getWriter().setCont(cont);
	}
	
	protected Cell lpad(String s, int len) {
		return new Cell(s, Alignment.Right, len);
	}
	
	protected Cell lpad(String s, char c, int len) {
		return new Cell(s, Alignment.Right, len).withPadding(c);
	}
	
	protected Cell mpad(String s, int len) {
		return new Cell(s, Alignment.Center, len);
	}
	
	protected Cell mpad(String s, char c, int len) {
		return new Cell(s, Alignment.Center, len).withPadding(c);
	}
	
	protected Cell rpad(String s, int len) {
		return new Cell(s, Alignment.Left, len);
	}
	
	protected Cell rpad(String s, char c, int len) {
		return new Cell(s, Alignment.Left, len).withPadding(c);
	}
	
	protected Cell rpad(Cell c, int len) {
		return new Cell(c).withAlignment(Alignment.Left).withLength(len);
	}
	
	public void printHeader() throws DocumentException {
		printHeader(true);
	}
	
	public void printHeader(boolean newPage) throws DocumentException {
		getWriter().printHeader(newPage);
	}

	@Override
	public void headerPrinted() {};
	
	protected void newPage() throws DocumentException {
		getWriter().newPage();
	}
	
	public void lastPage() throws DocumentException {
		getWriter().lastPage();
	}
	
	public void open(File file) throws DocumentException, IOException {
    	open(new FileOutputStream(file));
    }
	
	public void open(OutputStream out) throws DocumentException, IOException {
		getWriter().open(out);
	}
	
	public void close() throws IOException, DocumentException {
		getWriter().close();
	}
	
	protected int getLineNumber() {
		return getWriter().getLineNumber();
	}
	
	protected int getNrLinesPerPage() {
		return getWriter().getNrLinesPerPage();
	}
	
	protected int getNrCharsPerLine() {
		return getWriter().getNrCharsPerLine();
	}
	
	protected int getSeparatorNrLines() {
		return getWriter().getSeparatorNrLines();
	}
	
	protected boolean isSkipRepeating() {
		return getWriter().isSkipRepeating();
	}
	
	public static String getExtension(Mode mode) {
		switch (mode) {
		case LegacyPdfLetter:
		case LegacyPdfLedger:
		case PDF:
			return ".pdf";
		case LegacyText:
			return ".txt";
		case CSV:
			return ".csv";
		case XLS:
			return ".xls";
		default:
			return ".pdf";
		}
	}
	
	public static String getExtension(int mode) {
		return getExtension(Mode.values()[mode]);
	}
	
	public String getExtension() {
		return getExtension(iMode);
	}
	
	protected void println() throws DocumentException {
		printLine(new Line());
	}
	
	protected void printSeparator(Cell... cells) throws DocumentException {
		if (cells.length == 0)
			getWriter().printSeparator(null);
		else
			getWriter().printSeparator(new Line(cells));
	}
	
	protected void println(Line... line) throws DocumentException {
		if (line.length == 0)
			printLine(null);
		else if (line.length == 1)
			printLine(line[0]);
		else
			printLine(new Line(line));
	}
	
	protected void println(Cell... cells) throws DocumentException {
		printLine(new Line(cells));
	}
	
	protected void printLine(Line line) throws DocumentException {
		getWriter().printLine(line);
	}
	
	protected static enum Alignment { Left, Center, Right };
	
	protected static Cell NULL = new Cell("").withColSpan(0).withSeparator("");
	
	protected static class Cell {
		private String iText;
		private Alignment iAlignment;
		private int iLength = 0;
		private int iColSpan = 1;
		private char iPadding = ' ';
		private Cell[] iCells = null;
		private String iCellSeparator = " ";
		
		public Cell(String text, Alignment alignment, int length) {
			iText = text; iAlignment = alignment; iLength = length;
		}
		
		public Cell(String text) {
			this(text, Alignment.Left, text.length());
		}
		
		public Cell(Cell... cells) {
			iCells = cells;
			iAlignment = Alignment.Left;
		}
		
		public boolean isEmpty() {
			if (iText != null && !iText.isEmpty()) return false;
			if (iCells != null)
				for (Cell cell: iCells)
					if (!cell.isEmpty()) return false;
			return true;
		}
		
		public String getText() { return iText; }
		public Cell[] getCells() { return iCells; }
		public int getLength() { return iLength; }
		public int getColSpan() { return iColSpan; }
		public Alignment getAlignment() { return iAlignment; }
		public char getPadding() { return iPadding; }
		public String getCellSeparator() { return iCellSeparator; }
		
		public Cell withAlignment(Alignment alignment) { iAlignment = alignment; return this; }
		public Cell withLength(int length) { iLength = length; return this; }
		public Cell withColSpan(int colSpan) { iColSpan = colSpan; return this; }
		public Cell withPadding(char padding) { iPadding = padding; return this; }
		public Cell withSeparator(String separator) { iCellSeparator = separator; return this; }
		
		public String render() {
	    	StringBuffer ret = new StringBuffer();
	    	StringBuffer text = new StringBuffer();
	    	if (getText() != null) {
	    		text.append(getText());
	    	}
	    	if (getCells() != null) {
	    		if (getText() != null) text.append(getCellSeparator());
	    		for (int i = 0; i < getCells().length; i++) {
	    			if (i > 0) text.append(getCells()[i-1].getCellSeparator());
	    			text.append(getCells()[i].render());
	    		}
	    	}
	    	if (getLength() > 0) {
	        	switch (getAlignment()) {
	    		case Left:
	    			ret.append(rpad(text.toString(), getPadding(), getLength())); break;
	    		case Right:
	    			ret.append(lpad(text.toString(), getPadding(), getLength())); break;
	    		case Center:
	    			ret.append(mpad(text.toString(), getPadding(), getLength())); break;
	    		default:
	    			ret.append(rpad(text.toString(), getPadding(), getLength())); break;
	        	}    		
	    	} else {
	    		ret.append(text.toString());
	    	}
	    	return ret.toString();
	    }
		
		@Override
		public String toString() {
			return render();
		}
		
		@Override
		public boolean equals(Object o) {
			return toString().equals(o.toString());
		}
		
		public static String lpad(String s, char ch, int len) {
	        if (s==null) s="";
	        if (s.length()>len) return s.substring(0,len);
	        while (s.length()<len) s = ch + s;
	        return s;
	    }
	    
	    public static String lpad(String s, int len) {
	        if (s==null) s="";
	        if (s.length()>len) return s.substring(0,len);
	        return lpad(s,' ',len);
	    }

	    protected static String rpad(String s, char ch, int len) {
	        if (s==null) s="";
	        if (s.length()>len) return s.substring(0,len);
	        while (s.length()<len) s = s + ch;
	        return s;
	    }
	    
	    public static String rpad(String s, int len) {
	        if (s==null) s="";
	        if (s.length()>len) return s.substring(0,len);
	        return rpad(s,' ',len);
	    }
	    
	    protected static String mpad(String s, char ch, int len) {
	        if (s==null) s="";
	        if (s.length()>len) return s.substring(0,len);
	        while (s.length()<len) 
	            if (s.length()%2==0) s = s + ch; else s = ch + s;
	        return s;
	    }

	    public static String mpad(String s, int len) {
	        return mpad(s,' ',len);
	    }
	    
	    protected static String mpad(String s1, String s2, char ch, int len) {
	        String m = "";
	        while ((s1+m+s2).length()<len) m += ch;
	        return s1+m+s2;
	    }
	}
	
	protected static class Line {
		private Cell[] iCells;
		private Line[] iLines;
		private String iLineSeparator = "| ";
		private Alignment iAlignment = Alignment.Left;
		private int iLength = 0;
		
		public Line() {
			iCells = new Cell[] {};
		}
		
		public Line(Cell... cells) {
			iCells = cells;
			if (iCells.length == 1) {
				iAlignment = iCells[0].getAlignment();
			}
		}
		
		public Line(String... cells) {
			iCells = new Cell[cells.length];
			for (int i = 0; i < cells.length; i++)
				iCells[i] = new Cell(cells[i].trim(), Alignment.Left, cells[i].length());	
		}
		
		public Line(Line... lines) {
			iLines = lines;
		}
		
		public boolean isEmpty() {
			if (iCells != null)
				for (Cell cell: iCells)
					if (!cell.isEmpty()) return false;
			if (iLines != null)
				for (Line line: iLines)
					if (!line.isEmpty()) return false;
			return true;
		}
		
		public Cell[] getCells() { return iCells; }
		public Line[] getLines() { return iLines; }
		
		public String getLineSeparator() { return iLineSeparator; }
		public Line withLineSeparator(String sep) {
			iLineSeparator = sep;
			return this;
		}
		
		public int getLength() { return iLength; }
		public Alignment getAlignment() { return iAlignment; }
		public Line withAlignment(Alignment alignment) { iAlignment = alignment; return this; }
		public Line withLength(int length) { iLength = length; return this; }
		
	    public String render() {
	    	if (getLines() != null) {
	    		StringBuffer ret = new StringBuffer();
	    		for (int i = 0; i < getLines().length; i++) {
	    			if (i > 0) ret.append(getLines()[i - 1].getLineSeparator());
	    			ret.append(getLines()[i].render());
	    		}
	    		if (getLength() > 0) {
	    			switch (getAlignment()) {
	    			case Left: return Cell.rpad(ret.toString(), getLength());
	    			case Right: return Cell.lpad(ret.toString(), getLength());
	    			case Center: return Cell.mpad(ret.toString(), getLength());
	    			default: return Cell.rpad(ret.toString(), getLength());
	    			}
	    		} else {
	    			return ret.toString();
	    		}
	    	}
	    	if (getCells() != null) {
	    		StringBuffer ret = new StringBuffer();
	    		for (int i = 0; i < getCells().length; i++) {
	    			if (i > 0) ret.append(getCells()[i - 1].getCellSeparator());
	    			ret.append(getCells()[i].render());
	    		}
	    		if (getLength() > 0) {
	    			switch (getAlignment()) {
	    			case Left: return Cell.rpad(ret.toString(), getLength());
	    			case Right: return Cell.lpad(ret.toString(), getLength());
	    			case Center: return Cell.mpad(ret.toString(), getLength());
	    			default: return Cell.rpad(ret.toString(), getLength());
	    			}
	    		} else {
	    			return ret.toString();
	    		}
	    	}
	    	return "";
	    }
	    
	    @Override
	    public String toString() { return render(); }
	}
}
