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

import java.io.OutputStream;

import org.unitime.localization.impl.Localization;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfBoolean;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfDocument;
import com.lowagie.text.pdf.PdfIndirectReference;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfString;

public class PdfWriter extends com.lowagie.text.pdf.PdfWriter {
	
	protected PdfWriter(PdfDocument document, OutputStream os) {
		super(document, os);
	}
	
	public static PdfWriter getInstance(Document document, OutputStream os) throws DocumentException {
		PdfDocument pdf = new PdfDocument();
		document.addDocListener(pdf);
        PdfWriter writer = new PdfWriter(pdf, os);
        pdf.addWriter(writer);
        writer.addViewerPreference(PdfName.DISPLAYDOCTITLE, new PdfBoolean(true));
        return writer;
	}
	
	protected PdfDictionary getCatalog(PdfIndirectReference rootObj) {
		PdfDictionary ret = super.getCatalog(rootObj);
		ret.put(PdfName.LANG, new PdfString(Localization.getLocale(), PdfObject.TEXT_UNICODE));
		return ret;
	}
}
