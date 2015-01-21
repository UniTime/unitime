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
package org.unitime.timetable.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author Tomas Muller
 */
public class PdfEventHandler extends PdfPageEventHelper {
	private static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	private BaseFont baseFont;
	private float fontSize;
	
	private Date dateTime = null;
	private Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	
    /**
     * Constructor for PdfEventHandler
     * 
     */
    public PdfEventHandler() throws DocumentException, IOException {

    	super();
	    
    	Font font = PdfFont.getSmallFont();
		setBaseFont(font.getBaseFont());
		setFontSize(font.getSize());
		
        return;
     }

    /**
     * Initialize Pdf footer
     * @param document
     * @param outputStream
     * @return PdfWriter
     */
    public static PdfWriter initFooter(Document document, OutputStream outputStream) 
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
		cb.showTextAligned(PdfContentByte.ALIGN_CENTER, MESSAGES.pdfCopyright(Constants.getVersion()),
				(document.left() + document.right()) / 2, 20, 0);
		cb.endText();
			
        return;
    }

	private BaseFont getBaseFont() {
		return baseFont;
	}

	private void setBaseFont(BaseFont baseFont) {
		this.baseFont = baseFont;
	}

	private float getFontSize() {
		return fontSize;
	}

	private void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	private Date getDateTime() {
		return dateTime;
	}

	private void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	private Formats.Format<Date> getDateFormat() {
		return dateFormat;
	}

}
