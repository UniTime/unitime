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
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.unitime.timetable.reports.AbstractReport.Alignment;
import org.unitime.timetable.reports.AbstractReport.Line;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public class XlsReportWriter implements ReportWriter {
	private Workbook iWorkbook;
	private OutputStream iOutput;
	private Sheet iSheet;
	private int iPageNo = -1;
    private int iLineNo = 0;
    private int iNrColumns = 0, iMaxColumns = 0;
    private boolean iHeaderPrinted = false;
    private boolean iEmpty = true;
    private String iFooter = null, iPageName = null;
    private Map<String, CellStyle> iStyles = new HashMap<String, CellStyle>();
	private Map<String, Font> iFonts = new HashMap<String, Font>();
	private Map<String, Short> iColors = new HashMap<String, Short>();

    private Line iHeaderLine[] = null;

	public XlsReportWriter(OutputStream out, String title, String title2, String subject, String session) throws DocumentException, IOException {
		if (out != null) open(out);
	}
	
	@Override
	public void setFooter(String footer) {
		iFooter = footer;
		if (iWorkbook != null) 
			try {
				iWorkbook.setSheetName(iPageNo, footer.replaceAll("/", "-").replaceAll(":", "")); 
			} catch (IllegalArgumentException e) {}
	}

	@Override
	public void setHeader(Line... line) {
		if (iHeaderPrinted) {
			try {
				printSeparator(null);
			} catch (DocumentException e) {}
		}
		iHeaderLine = line;
		iNrColumns = 0;
		if (line != null) {
			for (Line l: line) {
				if (l.isEmpty()) continue;
				int cols = countColumns(l);
				if (cols > iNrColumns)
					iNrColumns = cols;
			}
		}
		if (iMaxColumns < iNrColumns)
			iMaxColumns = iNrColumns;
		iHeaderPrinted = false;
	}

	@Override
	public Line[] getHeader() { return iHeaderLine; }

	@Override
	public void printLine(Line line) throws DocumentException {
		render(line, iSheet.createRow(iLineNo++), false, 0);
		iEmpty = false;
	}

	@Override
	public void close() throws IOException, DocumentException {
		lastPage();
		iWorkbook.write(iOutput);
		iWorkbook.close();
	}

	@Override
	public void open(OutputStream out) throws DocumentException, IOException {
		iOutput = out;
		iWorkbook = new HSSFWorkbook();
		createSheet();
	}
	
	protected void createSheet() {
		iSheet = iWorkbook.createSheet();
		// iSheet.setDisplayGridlines(false);
		iSheet.setPrintGridlines(false);
		iSheet.setFitToPage(true);
		iSheet.setHorizontallyCenter(true);
        PrintSetup printSetup = iSheet.getPrintSetup();
        printSetup.setLandscape(true);
        iSheet.setAutobreaks(true);
        printSetup.setFitHeight((short)1);
        printSetup.setFitWidth((short)1);
		iPageNo ++;
		iLineNo = 0;
		iMaxColumns = 0;
		iEmpty = true;
		iFooter = null;
		iPageName = null;
		if (iHeaderLine != null && iHeaderLine.length > 0) {
			try {
				printHeader(false);
			} catch (DocumentException e) {}
			iMaxColumns = iNrColumns;
		}
	}
	
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
			font.setFontHeightInPoints((short)10);
			font.setFontName("Arial");
			iFonts.put(fontId, font);
		}
		return font;
	}
	
	protected CellStyle getStyle(boolean header, Alignment a) {
		String styleId = (header ? "H" : "") + (a.name().charAt(0));
		CellStyle style = iStyles.get(styleId);
		if (style == null) {
			style = iWorkbook.createCellStyle();
			style.setAlignment(a == Alignment.Left ? HorizontalAlignment.LEFT : a == Alignment.Right ? HorizontalAlignment.RIGHT : HorizontalAlignment.CENTER);
			style.setVerticalAlignment(VerticalAlignment.TOP);
			style.setFont(getFont(header, false, false, Color.BLACK));
        	style.setWrapText(true);
        	if (header) {
        		style.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
        		style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        	}
        	iStyles.put(styleId, style);
		}
		return style;
	}
	
	protected CellStyle cloneStyle(CellStyle style, String name) {
		CellStyle clone = iStyles.get(name);
		if (clone != null) return clone;
		clone = iWorkbook.createCellStyle();
		clone.setFont(iWorkbook.getFontAt(style.getFontIndex()));
		clone.setVerticalAlignment(VerticalAlignment.TOP);
		clone.setAlignment(style.getAlignmentEnum());
		clone.setBorderBottom(style.getBorderBottomEnum());
		clone.setBorderTop(style.getBorderTopEnum());
		clone.setBorderLeft(style.getBorderLeftEnum());
		clone.setBorderRight(style.getBorderRightEnum());
		clone.setBottomBorderColor(style.getBottomBorderColor());
		clone.setTopBorderColor(style.getTopBorderColor());
		clone.setLeftBorderColor(style.getLeftBorderColor());
		clone.setRightBorderColor(style.getRightBorderColor());
		clone.setWrapText(true);
		clone.setFillForegroundColor(style.getFillForegroundColor());
		clone.setFillPattern(style.getFillPatternEnum());
		iStyles.put(name, clone);
		return clone;
	}
	
	protected String getStyleName(CellStyle style) {
		for (Map.Entry<String, CellStyle> e: iStyles.entrySet()) {
			if (e.getValue().getIndex() == style.getIndex()) return e.getKey();
		}
		return null;
	}
	
	protected CellStyle addBottomRow(CellStyle style) {
		if (style.getBorderBottomEnum() == BorderStyle.THIN) return style;
		String name = getStyleName(style);
		if (name == null) return style;
		CellStyle clone = cloneStyle(style, name + "|B");
		clone.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		clone.setBorderBottom(BorderStyle.THIN);
		return clone;
	}
	
	protected CellStyle addTopRow(CellStyle style) {
		if (style.getBorderTopEnum() == BorderStyle.THIN) return style;
		String name = getStyleName(style);
		if (name == null) return style;
		CellStyle clone = cloneStyle(style, name + "|T");
		clone.setTopBorderColor(IndexedColors.BLACK.getIndex());
		clone.setBorderTop(BorderStyle.THIN);
		return clone;
	}
	
	protected CellStyle addLeftRow(CellStyle style) {
		if (style.getBorderLeftEnum() == BorderStyle.THIN) return style;
		String name = getStyleName(style);
		if (name == null) return style;
		CellStyle clone = cloneStyle(style, name + "|L");
		clone.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		clone.setBorderLeft(BorderStyle.THIN);
		return clone;
	}
	
	protected CellStyle addRightRow(CellStyle style) {
		if (style.getBorderRightEnum() == BorderStyle.THIN) return style;
		String name = getStyleName(style);
		if (name == null) return style;
		CellStyle clone = cloneStyle(style, name + "|R");
		clone.setRightBorderColor(IndexedColors.BLACK.getIndex());
		clone.setBorderRight(BorderStyle.THIN);
		return clone;
	}

	@Override
	public void setPageName(String pageName) {
		iPageName = pageName;
	}

	@Override
	public void setCont(String cont) {}

	@Override
	public void printHeader(boolean newPage) throws DocumentException {
		if (!iEmpty && newPage)
			newPage();
		if (iHeaderLine != null) {
			iHeaderPrinted = true;
			boolean first = true;
			for (Line line: iHeaderLine) {
				if (line.isEmpty()) continue;
				Row row = iSheet.createRow(iLineNo++);
				render(line, row, true, 0);
				iEmpty = false;
				if (first) {
					for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++)
						row.getCell(c).setCellStyle(addTopRow(row.getCell(c).getCellStyle()));
					first = false;
				}
			}
			printSeparator(null);
		}
	}

	@Override
	public void newPage() throws DocumentException {
		lastPage();
		createSheet();
	}

	@Override
	public void lastPage() throws DocumentException {
		for (short col = 0; col < iMaxColumns; col++)
			if (iSheet.getColumnWidth(col) == 256 * iSheet.getDefaultColumnWidth())
				iSheet.autoSizeColumn(col);
		if (iPageName != null && iFooter == null) {
			try {
				iWorkbook.setSheetName(iPageNo, iPageName.replaceAll("/", "-").replaceAll(":", ""));
			} catch (IllegalArgumentException e) {}
		}
		printSeparator(null);
	}

	@Override
	public int getLineNumber() { return iLineNo; }

	@Override
	public int getNrLinesPerPage() { return 0; }

	@Override
	public int getNrCharsPerLine() { return 1000; }

	@Override
	public void printSeparator(Line line) throws DocumentException {
		if (iLineNo > 0) {
			Row row = iSheet.getRow(iLineNo - 1);
			if (row != null)
				for (int c = 0; c < iNrColumns; c++) {
					Cell cell = row.getCell(c);
					if (cell != null) {
						if (cell.getCellStyle() != null)
							cell.setCellStyle(addBottomRow(cell.getCellStyle()));
						else
							cell.setCellStyle(addBottomRow(getStyle(false, Alignment.Left)));
					} else {
						cell = row.createCell(c);
						cell.setCellStyle(addBottomRow(getStyle(false, Alignment.Left)));
					}
				}
		}
	}

	@Override
	public int getSeparatorNrLines() { return 0; }
	
	private String render(AbstractReport.Cell cell) {
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
	
    private int render(Line line, Row row, boolean header, int col) {
    	if (line == null) return col;
    	if (line.getLines() != null) {
    		for (int i = 0; i < line.getLines().length; i++) {
    			col = render(line.getLines()[i], row, header, col);
    		}
    	}
    	if (line.getCells() != null) {
    		String leftOver = null;
    		for (AbstractReport.Cell cell: line.getCells()) {
    			if (cell.getColSpan() == 0) {
    				leftOver = ((leftOver == null || leftOver.isEmpty() ? "" : leftOver + " ") + render(cell)).trim();
    				continue;
    			}
    			Cell c = row.createCell(col);
    			CellStyle style = getStyle(header, cell.getAlignment());
    			c.setCellStyle(style);
    			c.setCellValue((leftOver == null || leftOver.isEmpty() ? "" : leftOver + " ") + render(cell));
    			Cell last = c;
    			if (cell.getColSpan() > 1) {
    				for (int x = 1; x < cell.getColSpan(); x++) {
    					Cell d = row.createCell(col + x);
    					d.setCellStyle(style);
    					last = d;
    				}
    				iSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), col, col + cell.getColSpan() - 1));
    			}
    			if (cell.getCellSeparator().trim().equals("|"))
    				last.setCellStyle(addRightRow(style));
    			col += cell.getColSpan();
    			leftOver = null;
    		}
    		if (row.getLastCellNum() < 0 && iNrColumns > 0) {
    			for (int i = 0; i < iNrColumns; i++) {
    				Cell c = row.createCell(i);
    				c.setCellStyle(getStyle(header, Alignment.Left));
    			}
    			iSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, iNrColumns - 1));
    		}
    		if (iHeaderPrinted) {
				row.getCell(row.getFirstCellNum()).setCellStyle(addLeftRow(row.getCell(0).getCellStyle()));
				row.getCell(row.getLastCellNum() - 1).setCellStyle(addRightRow(row.getCell(row.getLastCellNum() - 1).getCellStyle()));
			}
    	}
		return col;
    }
    
    private int countColumns(Line line) {
    	if (line == null) return 0;
		int cols = 0;
    	if (line.getLines() != null) {
    		for (Line l: line.getLines())
    			cols += countColumns(l);
    	}
    	if (line.getCells() != null) {
    		for (AbstractReport.Cell cell: line.getCells())
    			cols += cell.getColSpan();
    	}
    	return cols;
    }

	@Override
	public void setListener(Listener listener) {}

	@Override
	public boolean isSkipRepeating() { return true; }
}
