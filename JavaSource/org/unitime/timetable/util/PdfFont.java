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

import java.awt.Color;
import java.util.Hashtable;

import org.unitime.timetable.defaults.ApplicationProperty;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;

/**
 * @author Tomas Muller
 */
public class PdfFont {
	private static Hashtable<String, Font> sFontCache = new Hashtable<String, Font>();
	
	private static Font getFont(float size, boolean fixed, boolean bold, boolean italic) {
		if (ApplicationProperty.PdfFontCache.isTrue()) {
			Font font = sFontCache.get(size + (fixed ? "F": "") + (bold ? "B": "") + (italic ? "I" : ""));
			if (font == null) {
				font = createFont(size, fixed, bold, italic);
				sFontCache.put(size + (fixed ? "F": "") + (bold ? "B": "") + (italic ? "I" : ""), font);
			}
			return font;
		} else {
			return createFont(size, fixed, bold, italic);
		}
	}
	
	private static Font createFont(float size, boolean fixed, boolean bold, boolean italic) {
		String font = null;
		if (fixed)
			font = ApplicationProperty.PdfFontFixed.value();
		else if (bold) {
			if (italic)
				font = ApplicationProperty.PdfFontBoldItalic.value();
			else
				font = ApplicationProperty.PdfFontBold.value();
		} else if (italic)
			font = ApplicationProperty.PdfFontItalic.value();
		else
			font = ApplicationProperty.PdfFontNormal.value();
		if (font != null && !font.isEmpty()) {
			try {
				BaseFont base = BaseFont.createFont(font, BaseFont.IDENTITY_H, true);
				if (base != null) return new Font(base, size);
			} catch (Throwable t) {}
		}
		font = (fixed ? ApplicationProperty.PdfFontFixed.value() : ApplicationProperty.PdfFontNormal.value());
		if (font != null && !font.isEmpty()) {
			try {
				BaseFont base = BaseFont.createFont(font, BaseFont.IDENTITY_H, true);
				if (base != null) return new Font(base, size, italic && bold ? Font.BOLDITALIC : italic ? Font.ITALIC : bold ? Font.BOLD : Font.NORMAL);
			} catch (Throwable t) {}
		}
		if (fixed)
			return FontFactory.getFont(bold ? italic ? FontFactory.COURIER_BOLDOBLIQUE : FontFactory.COURIER_BOLD : italic ? FontFactory.COURIER_OBLIQUE : FontFactory.COURIER, size);
		else
			return FontFactory.getFont(bold ? italic ? FontFactory.HELVETICA_BOLDOBLIQUE : FontFactory.HELVETICA_BOLD : italic ? FontFactory.HELVETICA_OBLIQUE : FontFactory.HELVETICA, size);
	}
		
	public static Font getBigFont(boolean bold, boolean italic) {
		return getFont(ApplicationProperty.PdfFontSizeBig.floatValue(), false, bold, italic);
	}
	
	public static Font getBigFont(boolean bold) {
		return getBigFont(bold, false);
	}
	
	public static Font getBigFont() {
		return getBigFont(false, false);
	}

	public static Font getFont(boolean bold, boolean italic) {
		return getFont(ApplicationProperty.PdfFontSizeNormal.floatValue(), false, bold, italic);
	}
	
	public static Font getFont(boolean bold) {
		return getFont(bold, false);
	}
	
	public static Font getFont() {
		return getFont(false, false);
	}
	
	public static Font getFont(boolean bold, boolean italic, boolean underline, Color color) {
		Font font = getFont(bold, italic);
		if (underline) font.setStyle(font.getStyle() + Font.UNDERLINE);
		if (color != null) font.setColor(color);
		return font;
	}

	public static Font getFont(boolean bold, boolean italic, Color color) {
		return getFont(bold, italic, false, color);
	}

	public static Font getSmallFont(boolean bold, boolean italic) {
		return getFont(ApplicationProperty.PdfFontSizeSmall.floatValue(), false, bold, italic);
	}
	
	public static Font getSmallFont(boolean bold) {
		return getSmallFont(bold, false);
	}
	
	public static Font getSmallFont() {
		return getSmallFont(false, false);
	}
	
	public static Font getSmallFont(boolean bold, boolean italic, boolean underline, Color color) {
		Font font = getSmallFont(bold, italic);
		if (underline) font.setStyle(font.getStyle() + Font.UNDERLINE);
		if (color != null) font.setColor(color);
		return font;
	}
	
	public static Font getSmallFont(boolean bold, boolean italic, Color color) {
		return getSmallFont(bold, italic, false, color);
	}

	public static Font getFixedFont() {
		return getFont(ApplicationProperty.PdfFontSizeFixed.floatValue(), true, false, false);
	}
}
