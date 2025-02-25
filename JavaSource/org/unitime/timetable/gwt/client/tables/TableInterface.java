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
package org.unitime.timetable.gwt.client.tables;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TableInterface implements IsSerializable {
	private String iName;
	private String iStyle;
	private String iErrorMessage;
	private String iClassName;
	private List<LineInterface> iHeader;
	private List<LineInterface> iLines;
	private String iAnchor;
	private List<LinkInteface> iLinks;
	
	public TableInterface() {}
	
	public List<LineInterface> getHeader() { return iHeader; }
	public void addHeader(LineInterface header) {
		if (iHeader == null) iHeader = new ArrayList<LineInterface>();
		iHeader.add(header);
	}
	
	public List<LineInterface> getLines() { return iLines; }
	public void addLine(LineInterface line) {
		if (iLines == null) iLines = new ArrayList<LineInterface>();
		iLines.add(line);
	}

	public List<LinkInteface> getLinks() { return iLinks; }
	public void addLink(LinkInteface link) {
		if (iLinks == null) iLinks = new ArrayList<LinkInteface>();
		iLinks.add(link);
	}

	public int getMaxColumns() {
		int ret = 0;
		if (iHeader != null)
			for (LineInterface line: iHeader) {
				int cols = line.getMaxColumns();
				if (ret < cols) ret = cols;
			}
		if (iLines != null)
			for (LineInterface line: iLines) {
				int cols = line.getMaxColumns();
				if (ret < cols) ret = cols;
			}
		return ret;
	}
	
	public void setName(String name) { iName = name; }
	public String getName() { return iName; }
	public boolean hasName() { return iName != null && !iName.isEmpty(); }
	
	public String getStyle() { return iStyle; }
	public void setStyle(String style) { iStyle = style; }
	public boolean hasStyle() { return iStyle != null && !iStyle.isEmpty(); }
	
	public String getClassName() { return iClassName; }
	public void setClassName(String className) { iClassName = className; }
	public boolean hasClassName() { return iClassName != null && !iClassName.isEmpty(); }
	
	public String getAnchor() { return iAnchor; }
	public void setAnchor(String anchor) { iAnchor = anchor; }
	public boolean hasAnchor() { return iAnchor != null && !iAnchor.isEmpty(); }
	
	public String getErrorMessage() { return iErrorMessage; }
	public void setErrorMessage(String error) { iErrorMessage = error; }
	public boolean hasErrorMessage() { return iErrorMessage != null && !iErrorMessage.isEmpty(); }
	
	public static class LineInterface implements IsSerializable {
		private ArrayList<CellInterface> iCells;
		private String iStyle;
		private String iClassName;
		private String iBgColor;
		private String iURL;
		private String iTitle;
		private String iWarning;
		private String iAnchor;
		
		public LineInterface() {}
		
		public List<CellInterface> getCells() { return iCells; }
		public void addCell(CellInterface cell) {
			if (iCells == null) iCells = new ArrayList<CellInterface>();
			iCells.add(cell);
		}
		public boolean hasCells() { return iCells != null && !iCells.isEmpty(); }
		
		public String getStyle() { return iStyle; }
		public void setStyle(String style) { iStyle = style; }
		public boolean hasStyle() { return iStyle != null && !iStyle.isEmpty(); }
		
		public String getClassName() { return iClassName; }
		public void setClassName(String className) { iClassName = className; }
		public boolean hasClassName() { return iClassName != null && !iClassName.isEmpty(); }
		
		public String getBgColor() { return iBgColor; }
		public void setBgColor(String bgColor) { iBgColor = bgColor; }
		public boolean hasBgColor() { return iBgColor != null && !iBgColor.isEmpty(); }
		
		public String getURL() { return iURL; }
		public void setURL(String url) { iURL = url; }
		public boolean hasURL() { return iURL != null && !iURL.isEmpty(); }
		
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		public boolean hasTitle() { return iTitle != null && !iTitle.isEmpty(); }
		
		public String getWarning() { return iWarning; }
		public void setWarning(String warning) { iWarning = warning; }
		public boolean hasWarning() { return iWarning != null && !iWarning.isEmpty(); }
		
		public String getAnchor() { return iAnchor; }
		public void setAnchor(String anchor) { iAnchor = anchor; }
		public boolean hasAnchor() { return iAnchor != null && !iAnchor.isEmpty(); }
		
		public int getMaxColumns() {
			if (iCells == null) return 0;
			int ret = 0;
			for (CellInterface cell: iCells)
				ret += cell.getColSpan();
			return ret;
		}
	}
	
	public static class LinkInteface implements IsSerializable {
		private String iHref;
		private String iText;
		
		public LinkInteface() {}
		
		public String getText() { return iText; }
		public LinkInteface setText(String text) { iText = text; return this; }
	
		public String getHref() { return iHref; }
		public LinkInteface setHref(String href) { iHref = href; return this; }
	}
	
	public static class CellInterface implements IsSerializable {
		public static enum Alignment {
			LEFT, CENTER, RIGHT,
			TOP, MIDLE, BOTTOM,
		}
		private String iText;
		private String iAria;
		private String iTitle;
		private String iStyle;
		private String iClassName;
		private List<CellInterface> iItems;
		private Integer iRowSpan;
		private Integer iColSpan;
		private Alignment iTextAlignment;
		private Alignment iHorizontalAlignment;
		private Boolean iNoWrap, iInline, iHtml;
		private List<String> iAnchors;
		private String iColor;
		private Integer iIndent;
		private String iMouseOver, iMouseOut;
		private ImageInterface iImage;
		
		public CellInterface() {}
		
		public String getText() { return iText; }
		public CellInterface setText(String text) { iText = text; return this; }
		public CellInterface setHtml(String text) { iText = text; iHtml = true; return this; }
		public boolean hasText() { return iText != null && !iText.isEmpty(); }
		@Deprecated
		public void addContent(String text) { if (iText == null) iText = text; else iText += text; }
		
		public String getAria() { return iAria; }
		public CellInterface setAria(String aria) { iAria = aria; return this; }
		public boolean hasAria() { return  iAria != null && !iAria.isEmpty(); }
		
		public String getStyle() { return iStyle; }
		public void setStyle(String style) { iStyle = style; }
		public CellInterface addStyle(String style) {
			if (iStyle == null) iStyle = style;
			else iStyle += style;
			return this;
		}
		public boolean hasStyle() { return iStyle != null && !iStyle.isEmpty(); }
		
		public String getClassName() { return iClassName; }
		public CellInterface setClassName(String className) { iClassName = className; return this; }
		public boolean hasClassName() { return iClassName != null && !iClassName.isEmpty(); }
		
		public String getTitle() { return iTitle; }
		public CellInterface setTitle(String title) { iTitle = title; return this; }
		public boolean hasTitle() { return iTitle != null && !iTitle.isEmpty(); }
		
		public String getColor() { return iColor; }
		public CellInterface setColor(String color) { iColor = color; return this; }
		public boolean hasColor() { return iColor != null && !iColor.isEmpty(); }
		
		public List<CellInterface> getItems() { return iItems; }
		public CellInterface addItem(CellInterface item) {
			if (iItems == null) iItems = new ArrayList<CellInterface>();
			iItems.add(item);
			return this;
		}
		public boolean hasItems() { return iItems != null && !iItems.isEmpty(); }
		public CellInterface add(String text) {
			CellInterface cell = new CellInterface();
			cell.setText(text);
			addItem(cell);
			return cell;
		}
		public CellInterface add(String text, boolean html) {
			return add(text).setHtml(html);
		}
		public CellInterface addBlankLine() {
			return add("").setInline(false);
		}
		
		public List<String> getAnchors() { return iAnchors; }
		public CellInterface addAnchor(String anchor) {
			if (iAnchors == null) iAnchors = new ArrayList();
			iAnchors.add(anchor);
			return this;
		}
		public boolean hasAnchors() { return iAnchors != null && !iAnchors.isEmpty(); }
		
		public Integer getIndent() { return iIndent; }
		public CellInterface setIndent(int indent) { iIndent = indent; return this; }
		public boolean hasIndent() { return iIndent != null && iIndent.intValue() > 0; }
		
		public String getMouseOver() { return iMouseOver; }
		public CellInterface setMouseOver(String mouseOver) { iMouseOver = mouseOver; return this; }
		public boolean hasMouseOver() { return iMouseOver != null && !iMouseOver.isEmpty(); }
		public String getMouseOut() { return iMouseOut; }
		public CellInterface setMouseOut(String mouseOut) { iMouseOut = mouseOut; return this; }
		public boolean hasMouseOut() { return iMouseOut != null && !iMouseOut.isEmpty(); }
		
		public int getRowSpan() { return (iRowSpan == null ? 1 : iRowSpan.intValue()); }
		public CellInterface setRowSpan(int rowSpan) { iRowSpan = rowSpan; return this; }
		public int getColSpan() { return iColSpan == null ? 1 : iColSpan.intValue(); }
		public CellInterface setColSpan(int colSpan) { iColSpan = colSpan; return this; }
		public Alignment getTextAlignment() { return iTextAlignment == null ? Alignment.LEFT : iTextAlignment; }
		public CellInterface setTextAlignment(Alignment alignment) { iTextAlignment = alignment; return this; }
		public Alignment getHorizontalAlignment() { return iHorizontalAlignment == null ? Alignment.TOP : iHorizontalAlignment; }
		public CellInterface setHorizontalAlignment(Alignment alignment) { iHorizontalAlignment = alignment; return this; }
		public boolean isNoWrap() { return iNoWrap != null && iNoWrap.booleanValue(); }
		public boolean isWrap() { return iNoWrap != null && !iNoWrap.booleanValue(); }
		public CellInterface setNoWrap(boolean noWrap) { iNoWrap = noWrap; return this; }
		public boolean isInline() { return iInline == null || iInline.booleanValue(); }
		public CellInterface setInline(boolean inline) { iInline = inline; return this; }
		public boolean isHtml() { return iHtml != null && iHtml.booleanValue(); }
		public CellInterface setHtml(boolean html) { iHtml = html; return this; }
		public boolean hasNoWrap() {
			if (isNoWrap()) return true;
			if (hasItems())
				for (CellInterface item: getItems())
					if (item.hasNoWrap()) return true;
			return false;
		}
		
		public CellInterface setImage(ImageInterface image) { iImage = image; return this; }
		public ImageInterface getImage() { return iImage; }
		public boolean hasImage() { return iImage != null && iImage.hasSource(); }
		
		@Override
		public String toString() {
			return toString(null, 0);
		}

		protected String toString(CellInterface parent, int idx) {
			String ret = "";
			if (parent != null && !isInline() && idx > 0) ret = "\n";
			if (hasIndent())
				for (int i = 0; i < getIndent(); i++)
					ret += "  ";
			if (hasAria()) {
				ret += getAria();
				return ret;
			} else if (hasText() && !isHtml()) {
				ret += getText();
			} else if (hasTitle()) {
				ret += getTitle();
			} else if (hasImage()) {
				if (getImage().hasTitle())
					ret += getImage().getTitle();
			}
			if (hasItems()) { 
				int i = 0;
				for (CellInterface cell: getItems()) {
					ret += cell.toString(this, i++);
				}
			}
			return ret;
		}
	}
	
	public static class ImageInterface implements IsSerializable {
		private String iSource;
		private String iTitle;
		private String iAlt;
		private transient ImageGenerator iGenerator;
		
		public ImageInterface() {}
		
		public ImageInterface setSource(String source) { iSource = source; return this; }
		public String getSource() { return iSource; }
		public boolean hasSource() { return iSource != null && !iSource.isEmpty(); }
		public String getTitle() { return iTitle; }
		public ImageInterface setTitle(String title) { iTitle = title; return this; }
		public boolean hasTitle() { return iTitle != null && !iTitle.isEmpty(); }
		public String getAlt() { return iAlt; }
		public ImageInterface setAlt(String alt) { iAlt = alt; return this; }
		public boolean hasAlt() { return iAlt != null && !iAlt.isEmpty(); }
		
		public ImageGenerator getGenerator() { return iGenerator; }
		public ImageInterface setGenerator(ImageGenerator generator) { iGenerator = generator; return this; }
	}
	
	public static interface FilterInterface {
		public boolean hasParameter(String name);
		public String getParameterValue(String name);
		public String getParameterValue(String name, String defaultValue);
	}
	
	public static interface ImageGenerator {
		Object generate();
	}
}
