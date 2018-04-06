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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.unitime.timetable.export.Exporter.Printer;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.export.PDFPrinter.F;

/**
 * @author Tomas Muller
 */
public class XLSPrinter implements Printer {
	private static Pattern sNumber = Pattern.compile("[+-]?[0-9]*\\.?[0-9]*[a-z]?");
	private OutputStream iOutput;
	private Workbook iWorkbook;
	private Object[] iLastLine = null;
	private boolean iCheckLast = false;
	private Set<Integer> iHiddenColumns = new HashSet<Integer>();
	private Sheet iSheet;
	private int iRowNum = 0;
	private Map<String, CellStyle> iStyles;
	private Map<String, Font> iFonts = new HashMap<String, Font>();
	private Map<String, Short> iColors = new HashMap<String, Short>();
	
	
	public XLSPrinter(OutputStream output, boolean checkLast) {
		iOutput = output;
		iCheckLast = checkLast;
		iWorkbook = new HSSFWorkbook();
		iSheet = iWorkbook.createSheet();
		iSheet.setDisplayGridlines(false);
		iSheet.setPrintGridlines(false);
		iSheet.setFitToPage(true);
		iSheet.setHorizontallyCenter(true);
        PrintSetup printSetup = iSheet.getPrintSetup();
        printSetup.setLandscape(true);
        iSheet.setAutobreaks(true);
        printSetup.setFitHeight((short)1);
        printSetup.setFitWidth((short)1);
        iStyles = new HashMap<String, CellStyle>();
        
        CellStyle style;
        
        style = iWorkbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setFont(getFont(true, false, false, Color.BLACK));
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setWrapText(true);
        iStyles.put("header", style);
        
        style = iWorkbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setFont(getFont(false, false, false, Color.BLACK));
        style.setWrapText(true);
        iStyles.put("plain", style);
        
        style = iWorkbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setFont(getFont(false, false, false, Color.BLACK));
        iStyles.put("number", style);
	}
	
	@Override
	public String getContentType() {
		return "application/vnd.ms-excel";
	}
	
	@Override
	public void hideColumn(int col) {
		iHiddenColumns.add(col);
	}
	
	@Override
	public void printHeader(String... fields) {
		Row headerRow = iSheet.createRow(iRowNum++);
		
		int cellIdx = 0;
		int nrLines = 1;
		for (int idx = 0; idx < fields.length; idx++) {
			if (iHiddenColumns.contains(idx)) continue;
			Cell cell = headerRow.createCell(cellIdx++);
			cell.setCellStyle(iStyles.get("header"));
			cell.setCellValue(fields[idx]);
			if (fields[idx] != null)
				nrLines = Math.max(nrLines, fields[idx].split("\n").length);
		}
		if (nrLines > 1)
			headerRow.setHeightInPoints(nrLines * iSheet.getDefaultRowHeightInPoints() + 1f);
	}
	
	@Override
	public void printLine(String... fields) {
		int cellIdx = 0;
		Row row = iSheet.createRow(iRowNum++);
		int nrLines = 1;
		for (int idx = 0; idx < fields.length; idx++) {
			if (iHiddenColumns.contains(idx)) continue;
			if (iHiddenColumns.contains(idx)) continue;
			Cell cell = row.createCell(cellIdx ++);
			
			String f = fields[idx];
			if (f == null || f.isEmpty() || (iCheckLast && f.equals(iLastLine == null || idx >= iLastLine.length ? null : iLastLine[idx]))) f = "";
			
			boolean number = sNumber.matcher(f).matches();
			
			cell.setCellStyle(iStyles.get(number ? "number" : "plain"));
			if (f == null || f.isEmpty()) {
			} else if (number) {
				cell.setCellValue(Double.valueOf(f));
			} else {
				nrLines = Math.max(nrLines, f.split("\n").length);
				cell.setCellValue(f);
			}
		}
		if (nrLines > 1)
			row.setHeightInPoints(nrLines * iSheet.getDefaultRowHeightInPoints() + 1f);
		iLastLine = fields;
	}
	
	public void printLine(A... fields) {
		int cellIdx = 0;
		Row row = iSheet.createRow(iRowNum++);
		int nrLines = 1;
		for (int idx = 0; idx < fields.length; idx++) {
			if (iHiddenColumns.contains(idx)) continue;
			Cell cell = row.createCell(cellIdx ++);
			
			A f = fields[idx];
			if (f == null || f.isEmpty() || (iCheckLast && f.equals(iLastLine == null || idx >= iLastLine.length ? null : iLastLine[idx]))) {
				f = new A();
				if (fields[idx] != null && fields[idx].has(F.NOSEPARATOR))
					f.set(F.NOSEPARATOR);
			}
			
			cell.setCellStyle(getStyle(f, iLastLine == null && !f.has(F.NOSEPARATOR), f.getPattern()));
			
			if (f.hasBufferedImage()) {
				try {
					addImageToSheet(cellIdx - 1, iRowNum - 1, (HSSFSheet)iSheet, f.getBufferedImage(), EXPAND_ROW_AND_COLUMN);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (f.isNumber()) {
				cell.setCellValue(f.getNumber());
			} else if (f.isDate()) {
				cell.setCellValue(f.getDate());
			} else if (f.hasText()) {
				boolean number = sNumber.matcher(f.getText()).matches();
				if (number && f.has(F.RIGHT)) {
					cell.setCellValue(Double.valueOf(f.getText()));
				} else {
					cell.setCellValue(f.getText());
					nrLines = Math.max(nrLines, f.getText().split("\n").length);
				}
			} else if (f.hasChunks()) {
				StringBuffer text = new StringBuffer();
				List<Object[]> font = new ArrayList<Object[]>(); 
				for (A g: f.getChunks()) {
					if (g.hasText()) {
						if (text.length() > 0) text.append(f.has(F.INLINE) ? " " : "\n");
						font.add(new Object[] {text.length(), getFont(g.has(F.BOLD), g.has(F.ITALIC), g.has(F.UNDERLINE), g.getColor()).getIndex()});
						text.append(g.getText());
					}
					if (g.hasChunks()) {
						for (A h: g.getChunks()) {
							if (h.hasText()) {
								if (text.length() > 0) text.append(" ");
								font.add(new Object[] {text.length(), getFont(h.has(F.BOLD), h.has(F.ITALIC), h.has(F.UNDERLINE), h.getColor()).getIndex()});
								text.append(h.getText());
							}
						}
					}
				}
				nrLines = Math.max(nrLines, text.toString().split("\n").length);
				font.add(new Object[] {text.length(), (short)0});
				HSSFRichTextString value = new HSSFRichTextString(text.toString());
				for (int i = 0; i < font.size() - 1; i++)
					value.applyFont((int)font.get(i)[0], (int)font.get(1 + i)[0], (short)font.get(i)[1]);
				cell.setCellValue(value);
			}
		}
		if (nrLines > 1)
			row.setHeightInPoints(Math.max(nrLines * iSheet.getDefaultRowHeightInPoints() + 1f, row.getHeightInPoints()));
		iLastLine = fields;
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
	
	protected CellStyle getStyle(A f, boolean dashed, String format) {
		String styleId = (dashed ? "D" : "")
				+ (f.has(F.BOLD) ? "b" : "") + (f.has(F.ITALIC) ? "i" : "") + (f.has(F.UNDERLINE) ? "u" : "")
				+ (f.has(F.RIGHT) ? "R" : f.has(F.CENTER) ? "C" : "L")
				+ (f.hasColor() ? "#" + Integer.toHexString(f.getColor().getRGB()) : "")
				+ (format == null ? "" : "|" + format);
		CellStyle style = iStyles.get(styleId);
		if (style == null) {
			style = iWorkbook.createCellStyle();
			if (dashed) {
				style.setBorderTop(BorderStyle.DASHED);
		        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
			}
			style.setAlignment(f.has(F.RIGHT) ? HorizontalAlignment.RIGHT : f.has(F.CENTER) ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.TOP);
			style.setFont(getFont(f.has(F.BOLD), f.has(F.ITALIC), f.has(F.UNDERLINE), f.getColor()));
        	style.setWrapText(true);
        	if (format != null)
        		style.setDataFormat(iWorkbook.createDataFormat().getFormat(format));
        	iStyles.put(styleId, style);
		}
		return style;
	}
	
	public static final int EXPAND_ROW = 1;
    public static final int EXPAND_COLUMN = 2;
    public static final int EXPAND_ROW_AND_COLUMN = 3;
    public static final int OVERLAY_ROW_AND_COLUMN = 7;
    
    protected void addImageToSheet(int colNumber, int rowNumber, HSSFSheet sheet, BufferedImage image, int resizeBehaviour) throws IOException {
        double reqImageWidthMM = image.getWidth() / ConvertImageUnits.PIXELS_PER_MILLIMETRES;
        double reqImageHeightMM = image.getHeight() / ConvertImageUnits.PIXELS_PER_MILLIMETRES;
        addImageToSheet(colNumber, rowNumber, sheet, image, reqImageWidthMM, reqImageHeightMM, resizeBehaviour);
    }
    
    protected void addImageToSheet(int colNumber, int rowNumber, HSSFSheet sheet, BufferedImage image, double reqImageWidthMM, double reqImageHeightMM, int resizeBehaviour) throws IOException {
        ClientAnchorDetail colClientAnchorDetail = fitImageToColumns(sheet, colNumber, reqImageWidthMM, resizeBehaviour);
        ClientAnchorDetail rowClientAnchorDetail = fitImageToRows(sheet, rowNumber, reqImageHeightMM, resizeBehaviour);

        HSSFClientAnchor anchor = new HSSFClientAnchor(0,
                                      0,
                                      colClientAnchorDetail.getInset(),
                                      rowClientAnchorDetail.getInset(),
                                      (short)colClientAnchorDetail.getFromIndex(),
                                      rowClientAnchorDetail.getFromIndex(),
                                      (short)colClientAnchorDetail.getToIndex(),
                                      rowClientAnchorDetail.getToIndex());

        anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ImageIO.write(image, "PNG", bytes);

        int index = sheet.getWorkbook().addPicture(bytes.toByteArray(), HSSFWorkbook.PICTURE_TYPE_PNG);

        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        patriarch.createPicture(anchor, index);
    }
    
    private ClientAnchorDetail fitImageToColumns(HSSFSheet sheet, int colNumber, double reqImageWidthMM, int resizeBehaviour) {
        double colWidthMM;
        double colCoordinatesPerMM;
        int pictureWidthCoordinates;
        ClientAnchorDetail colClientAnchorDetail = null;

        colWidthMM = ConvertImageUnits.widthUnits2Millimetres((short)sheet.getColumnWidth(colNumber));

        if (colWidthMM < reqImageWidthMM) {
            if (resizeBehaviour == EXPAND_COLUMN || resizeBehaviour == EXPAND_ROW_AND_COLUMN) {
            	sheet.setColumnWidth(colNumber, ConvertImageUnits.millimetres2WidthUnits(reqImageWidthMM));
            	colWidthMM = reqImageWidthMM;
                colCoordinatesPerMM = ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS / colWidthMM;
                pictureWidthCoordinates = (int)(reqImageWidthMM * colCoordinatesPerMM);
                colClientAnchorDetail = new ClientAnchorDetail(colNumber, colNumber, pictureWidthCoordinates);
            } else if (resizeBehaviour == OVERLAY_ROW_AND_COLUMN || resizeBehaviour == EXPAND_ROW) {
            	colClientAnchorDetail = calculateColumnLocation(sheet, colNumber, reqImageWidthMM);
            }
        } else {
        	colCoordinatesPerMM = ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS / colWidthMM;
            pictureWidthCoordinates = (int)(reqImageWidthMM * colCoordinatesPerMM);
            colClientAnchorDetail = new ClientAnchorDetail(colNumber, colNumber, pictureWidthCoordinates);
        }
        return(colClientAnchorDetail);
    }
    
    private ClientAnchorDetail calculateColumnLocation(HSSFSheet sheet, int startingColumn, double reqImageWidthMM) {
    	ClientAnchorDetail anchorDetail;
    	double totalWidthMM = 0.0D;
    	double colWidthMM = 0.0D;
    	double overlapMM;
    	double coordinatePositionsPerMM;
    	int toColumn = startingColumn;
    	int inset;

    	while (totalWidthMM < reqImageWidthMM) {
    		colWidthMM = ConvertImageUnits.widthUnits2Millimetres((short)(sheet.getColumnWidth(toColumn)));
    		totalWidthMM += (colWidthMM + ConvertImageUnits.CELL_BORDER_WIDTH_MILLIMETRES);
    		toColumn++;
    	}
    	toColumn--;

    	if((int)totalWidthMM == (int)reqImageWidthMM) {
    		anchorDetail = new ClientAnchorDetail(startingColumn, toColumn, ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS);
    	} else {
    		overlapMM = reqImageWidthMM - (totalWidthMM - colWidthMM);
    		if(overlapMM < 0) {
    			overlapMM = 0.0D;
    		}
    		coordinatePositionsPerMM = ConvertImageUnits.TOTAL_COLUMN_COORDINATE_POSITIONS / colWidthMM;
    		inset = (int)(coordinatePositionsPerMM * overlapMM);
    		anchorDetail = new ClientAnchorDetail(startingColumn, toColumn, inset);
    	}
    	return(anchorDetail);
    }
    
    private ClientAnchorDetail fitImageToRows(HSSFSheet sheet, int rowNumber, double reqImageHeightMM, int resizeBehaviour) {
        double rowCoordinatesPerMM;
        int pictureHeightCoordinates;
        ClientAnchorDetail rowClientAnchorDetail = null;

        HSSFRow row = sheet.getRow(rowNumber);
        if (row == null) {
        	row = sheet.createRow(rowNumber);
        }

        double rowHeightMM = row.getHeightInPoints() / ConvertImageUnits.POINTS_PER_MILLIMETRE;

        if (rowHeightMM < reqImageHeightMM) {
            if (resizeBehaviour == EXPAND_ROW || resizeBehaviour == EXPAND_ROW_AND_COLUMN) {
            	row.setHeightInPoints((float)(reqImageHeightMM * ConvertImageUnits.POINTS_PER_MILLIMETRE));
                rowHeightMM = reqImageHeightMM;
                rowCoordinatesPerMM = ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS / rowHeightMM;
                pictureHeightCoordinates = (int)(reqImageHeightMM * rowCoordinatesPerMM);
                rowClientAnchorDetail = new ClientAnchorDetail(rowNumber, rowNumber, pictureHeightCoordinates);
            } else if (resizeBehaviour == OVERLAY_ROW_AND_COLUMN || resizeBehaviour == EXPAND_COLUMN) {
            	rowClientAnchorDetail = calculateRowLocation(sheet, rowNumber, reqImageHeightMM);
            }
        } else {
            rowCoordinatesPerMM = ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS / rowHeightMM;
            pictureHeightCoordinates = (int)(reqImageHeightMM * rowCoordinatesPerMM);
            rowClientAnchorDetail = new ClientAnchorDetail(rowNumber, rowNumber, pictureHeightCoordinates);
        }
        
        return rowClientAnchorDetail;
    }
    
    private ClientAnchorDetail calculateRowLocation(HSSFSheet sheet, int startingRow, double reqImageHeightMM) {
        ClientAnchorDetail clientAnchorDetail;
        HSSFRow row;
        double rowHeightMM = 0.0D;
        double totalRowHeightMM = 0.0D;
        double overlapMM;
        double rowCoordinatesPerMM;
        int toRow = startingRow;
        int inset;

        while (totalRowHeightMM < reqImageHeightMM) {
            row = sheet.getRow(toRow);
            if(row == null) {
                row = sheet.createRow(toRow);
            }
            rowHeightMM = row.getHeightInPoints() / ConvertImageUnits.POINTS_PER_MILLIMETRE;
            totalRowHeightMM += rowHeightMM;
            toRow++;
        }
        toRow--;

        if ((int)totalRowHeightMM == (int)reqImageHeightMM) {
            clientAnchorDetail = new ClientAnchorDetail(startingRow, toRow, ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS);
        } else {
            overlapMM = reqImageHeightMM - (totalRowHeightMM - rowHeightMM);
            if(overlapMM < 0) {
                overlapMM = 0.0D;
            }
            rowCoordinatesPerMM = ConvertImageUnits.TOTAL_ROW_COORDINATE_POSITIONS / rowHeightMM;
            inset = (int)(overlapMM * rowCoordinatesPerMM);
            clientAnchorDetail = new ClientAnchorDetail(startingRow, toRow, inset);
        }
        
        return clientAnchorDetail;
    }
    
    public static class ClientAnchorDetail {
        public int iFromIndex;
        public int iToIndex;
        public int iInset;

        public ClientAnchorDetail(int fromIndex, int toIndex, int inset) {
            iFromIndex = fromIndex;
            iToIndex = toIndex;
            iInset = inset;
        }

        public int getFromIndex() { return iFromIndex; }
        public int getToIndex() { return iToIndex; }
        public int getInset() { return iInset; }
    }
    
    public static class ConvertImageUnits {
        public static final int TOTAL_COLUMN_COORDINATE_POSITIONS = 1023;
        public static final int TOTAL_ROW_COORDINATE_POSITIONS = 255;
        public static final int PIXELS_PER_INCH = 96;
        public static final double PIXELS_PER_MILLIMETRES = 3.78;
        public static final double POINTS_PER_MILLIMETRE = 2.83;
        public static final double CELL_BORDER_WIDTH_MILLIMETRES = 2.0d;
        public static final short EXCEL_COLUMN_WIDTH_FACTOR = 256;
        public static final int UNIT_OFFSET_LENGTH = 7;
        public static final int[] UNIT_OFFSET_MAP = new int[] { 0, 36, 73, 109, 146, 182, 219 };

        public static short pixel2WidthUnits(int pxs) {
            short widthUnits = (short) (EXCEL_COLUMN_WIDTH_FACTOR * (pxs / UNIT_OFFSET_LENGTH));
            widthUnits += UNIT_OFFSET_MAP[(pxs % UNIT_OFFSET_LENGTH)];
            return widthUnits;
        }

        public static int widthUnits2Pixel(short widthUnits) {
            int pixels = (widthUnits / EXCEL_COLUMN_WIDTH_FACTOR) * UNIT_OFFSET_LENGTH;
            int offsetWidthUnits = widthUnits % EXCEL_COLUMN_WIDTH_FACTOR;
            pixels += Math.round(offsetWidthUnits / ((float) EXCEL_COLUMN_WIDTH_FACTOR / UNIT_OFFSET_LENGTH));
            return pixels;
        }

        public static double widthUnits2Millimetres(short widthUnits) {
            return ConvertImageUnits.widthUnits2Pixel(widthUnits) / ConvertImageUnits.PIXELS_PER_MILLIMETRES;
        }

        public static int millimetres2WidthUnits(double millimetres) {
            return ConvertImageUnits.pixel2WidthUnits((int)(millimetres * ConvertImageUnits.PIXELS_PER_MILLIMETRES));
        }
    }

	
	@Override
	public void flush() {
		iLastLine = null;
	}
	
	@Override
	public void close() throws IOException {
		for (short col = 0; col <= iSheet.getRow(0).getLastCellNum(); col++)
			if (iSheet.getColumnWidth(col) == 256 * iSheet.getDefaultColumnWidth())
				iSheet.autoSizeColumn(col);
		iWorkbook.write(iOutput);
		iWorkbook.close();
	}
}
