/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfEventHandler extends PdfPageEventHelper {

	private BaseFont baseFont;
	private int fontSize;
	
	private Date dateTime = null;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mmaa");
	
    /**
     * Constructor for PdfEventHandler
     * 
     */
    public PdfEventHandler() throws DocumentException, IOException {

    	super();
	    
		setBaseFont(
			BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED));
		setFontSize(12);
		
        return;
     }

    /**
     * Initialize Pdf footer
     * @param document
     * @param outputStream
     * @return PdfWriter
     */
    public static PdfWriter initFooter(Document document, FileOutputStream outputStream) 
    		throws DocumentException, IOException {
    	
		PdfWriter iWriter = PdfWriter.getInstance(document, outputStream);
		iWriter.setPageEvent(new PdfEventHandler());
    	
		return iWriter;
    }
    /**
     * Print footer string on each page
     * @param writer
     * @param document
     */
    public void onEndPage(PdfWriter writer, Document document) {
	    
    	if(getDateTime() == null) {
    		setDateTime(new Date());
    	}
    	
		PdfContentByte cb = writer.getDirectContent();
		cb.beginText();
		cb.setFontAndSize(getBaseFont(), getFontSize());
		cb.showTextAligned(PdfContentByte.ALIGN_LEFT, getDateFormat().format(getDateTime()), 
			    document.left(), 20, 0);
		cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, String.valueOf(document.getPageNumber()), 
			    document.right(), 20, 0);
		cb.endText();
			
        return;
    }

	private BaseFont getBaseFont() {
		return baseFont;
	}

	private void setBaseFont(BaseFont baseFont) {
		this.baseFont = baseFont;
	}

	private int getFontSize() {
		return fontSize;
	}

	private void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	private Date getDateTime() {
		return dateTime;
	}

	private void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	private SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

}
