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
	private List<PropertyInterface> iProperties;
	
	public TableInterface() {}
	
	public List<LineInterface> getHeader() { return iHeader; }
	public void addHeader(LineInterface header) {
		if (iHeader == null) iHeader = new ArrayList<LineInterface>();
		iHeader.add(header);
	}
	public LineInterface addHeader() {
		LineInterface line = new LineInterface();
		addHeader(line);
		return line;
	}
	
	public List<LineInterface> getLines() { return iLines; }
	public void addLine(LineInterface line) {
		if (iLines == null) iLines = new ArrayList<LineInterface>();
		iLines.add(line);
	}
	public LineInterface addLine() {
		LineInterface line = new LineInterface();
		addLine(line);
		return line;
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
	
	public boolean hasProperties() { return iProperties != null && !iProperties.isEmpty(); }
	public void addProperty(PropertyInterface property) {
		if (iProperties == null) iProperties = new ArrayList<PropertyInterface>();
		iProperties.add(property);
	}
	public List<PropertyInterface> getProperties() { return iProperties; }
	public CellInterface addProperty(String text) {
		PropertyInterface p = new PropertyInterface();
		p.setName(text);
		p.setCell(new CellInterface());
		addProperty(p);
		return p.getCell();
	}
	
	public static class PropertyInterface implements IsSerializable {
		private String iName;
		private CellInterface iCell;
		private String iStyle;
		
		public PropertyInterface() {}
		
		public String getName() { return iName; }
		public PropertyInterface setName(String name) { iName = name; return this; }
		public CellInterface getCell() { return iCell; }
		public PropertyInterface setCell(CellInterface cell) { iCell = cell; return this; }
		
		public String getStyle() { return iStyle; }
		public void setStyle(String style) { iStyle = style; }
		public boolean hasStyle() { return iStyle != null && !iStyle.isEmpty(); }
		public PropertyInterface addStyle(String style) {
			if (iStyle == null) iStyle = style;
			else iStyle += style;
			return this;
		}
	}
	
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
		public CellInterface addCell() {
			CellInterface cell = new CellInterface();
			addCell(cell);
			return cell;
		}
		public CellInterface addCell(String text) {
			return addCell().setText(text);
		}
		
		public String getStyle() { return iStyle; }
		public void setStyle(String style) { iStyle = style; }
		public boolean hasStyle() { return iStyle != null && !iStyle.isEmpty(); }
		public LineInterface addStyle(String style) {
			if (iStyle == null) iStyle = style;
			else iStyle += style;
			return this;
		}
		
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
		private String iMouseOver, iMouseOut, iMouseClick;
		private ImageInterface iImage;
		private ButtonInterface iButton;
		private Integer iWidth;
		private String iUrl;
		private TableInterface iTable;
		private String iWarning;
		private CourseLinkInterface iCourseLink;
		private String iScript;
		
		public CellInterface() {}
		
		public String getText() { return iText; }
		public CellInterface setText(String text) { iText = text; return this; }
		public CellInterface setText(Integer text) { iText = (text == null ? "0" : text.toString()); return this; }
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
		
		public Integer getWidth() { return iWidth; }
		public CellInterface setWidth(Integer width) { iWidth = width; return this; }
		public boolean hasWidth() { return iWidth != null; }
		
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
		public String getMouseClick() { return iMouseClick; }
		public CellInterface setMouseClick(String mouseClick) { iMouseClick = mouseClick; return this; }
		public boolean hasMouseClick() { return iMouseClick != null && !iMouseClick.isEmpty(); }
		
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
		public ImageInterface setImage() { iImage = new ImageInterface(); return iImage; }
		public ImageInterface getImage() { return iImage; }
		public boolean hasImage() { return iImage != null && iImage.hasSource(); }
		public ImageInterface addImage() { 
			ImageInterface image = new ImageInterface();
			CellInterface cell = new CellInterface();
			cell.setImage(image);
			addItem(cell);
			return image;
		}

		public CellInterface setButton(ButtonInterface botton) { iButton = botton; return this; }
		public ButtonInterface setButton() { iButton = new ButtonInterface(); return iButton; }
		public ButtonInterface addButton() { 
			ButtonInterface button = new ButtonInterface();
			CellInterface cell = new CellInterface();
			cell.setButton(button);
			addItem(cell);
			return button;
		}
		public boolean hasButton() { return iButton != null && iButton.hasUrl(); }
		public ButtonInterface getButton() { return iButton; }
		
		public CellInterface setUrl(String url) { iUrl = url; return this; }
		public String getUrl() { return iUrl; }
		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
		
		public TableInterface getTable() { return iTable; }
		public void setTable(TableInterface table) { iTable = table; }
		
		public String getWarning() { return iWarning; }
		public void setWarning(String warning) { iWarning = warning; }
		public boolean hasWarning() { return iWarning != null && !iWarning.isEmpty(); }
		
		public boolean hasCourseLink() { return iCourseLink != null; }
		public CourseLinkInterface addCourseLink() { iCourseLink = new CourseLinkInterface(); return iCourseLink; }
		public CourseLinkInterface getCourseLink() { return iCourseLink; }
		
		public boolean hasScript() { return iScript != null; }
		public String getScript() { return iScript; }
		public CellInterface setScript(String script) { iScript = script; return this; }

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
		private String iStyle;
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
		
		public String getStyle() { return iStyle; }
		public void setStyle(String style) { iStyle = style; }
		public ImageInterface addStyle(String style) {
			if (iStyle == null) iStyle = style;
			else iStyle += style;
			return this;
		}
		public boolean hasStyle() { return iStyle != null && !iStyle.isEmpty(); }

	}
	
	public static class ButtonInterface implements IsSerializable {
		private String iUrl;
		private String iTitle;
		private String iText;
		
		public ButtonInterface() {}
		
		public ButtonInterface setUrl(String url) { iUrl = url; return this; }
		public String getUrl() { return iUrl; }
		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
		public String getTitle() { return iTitle; }
		public ButtonInterface setTitle(String title) { iTitle = title; return this; }
		public boolean hasTitle() { return iTitle != null && !iTitle.isEmpty(); }
		public String getText() { return iText; }
		public ButtonInterface setText(String text) { iText = text; return this; }
		public boolean hasText() { return iText != null && !iText.isEmpty(); }
	}
	
	public static class CourseLinkInterface implements IsSerializable {
		private Long iCourseId;
		private Boolean iAnchor = true;
		
		public CourseLinkInterface() {}
		
		public CourseLinkInterface setCourseId(Long courseId) { iCourseId = courseId; return this; }
		public Long getCourseId() { return iCourseId; }
		public CourseLinkInterface setAnchor(boolean anchor) { iAnchor = anchor; return this; }
		public boolean isAnchor() { return iAnchor == null || iAnchor.booleanValue(); }
	}
	
	public static interface FilterInterface {
		public boolean hasParameter(String name);
		public String getParameterValue(String name);
		public String getParameterValue(String name, String defaultValue);
	}
	
	public static interface ImageGenerator {
		Object generate();
	}
	
	public static class TimePrefInterface implements IsSerializable {
		private String iName;
		private List<TimePrefDay> iDayCodes = new ArrayList<TimePrefDay>();
		private List<TimePrefTime> iTimes = new ArrayList<TimePrefTime>();
		private List<TimePrefOption> iOptions = new ArrayList<TimePrefOption>();
		private boolean iVertical = false;
		private char[] iPattern = null;
		private char iDefault = '2';
		
		public TimePrefInterface() {}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public void addDays(int dayCode, String label) { iDayCodes.add(new TimePrefDay(dayCode, label)); }
		public List<TimePrefDay> getDays() { return iDayCodes; }
		
		public void addTime(int startSlot, String start, String stop) { iTimes.add(new TimePrefTime(startSlot, start, stop)); }
		public List<TimePrefTime> getTimes() { return iTimes; }
		
		public List<TimePrefOption> getOptions() { return iOptions; }
		public void addOption(char code, String label, String color, boolean isDefault) {
			TimePrefOption option = new TimePrefOption(code, label, color);
			iOptions.add(option);
			if (isDefault) iDefault = option.getCode();
		}
		public TimePrefOption getOption(char code) {
			for (TimePrefOption o: iOptions)
				if (o.getCode() == code) return o;
			return null;
		}
		public TimePrefOption getDefaultOption() { return getOption(iDefault); }
		public boolean isDefault(TimePrefOption option) { return iDefault == option.getCode(); }
		
		public boolean isVertical() { return iVertical; }
		public void setVertical(boolean vertical) { iVertical = vertical; }
		
		public String getPattern() { return iPattern == null ? null : new String(iPattern); }
		public void setPattern(String pattern) {
			if (pattern != null && pattern.length() == iTimes.size() * iDayCodes.size()) {
				iPattern = pattern.toCharArray();
			} else {
				iPattern = new char[iTimes.size() * iDayCodes.size()];
				for (int d = 0; d < iDayCodes.size(); d++)
					for (int t = 0; t < iTimes.size(); t++) {
						int index = d * iTimes.size() + t;
						iPattern[index] = iDefault;
					}
				if (pattern != null)
					for (int i = 0; i < pattern.length(); i++) {
						if (i > iPattern.length) break;
						iPattern[i] = pattern.charAt(i);
					}
			}
		}
		
		public TimePrefOption getPreference(int day, int time) {
			int index = day * iTimes.size() + time;
			return getOption(iPattern[index]);
		}
		
		public void setPreference(int day, int time, TimePrefOption option) {
			if (iPattern == null) setPattern(null);
			int index = day * iTimes.size() + time;
			iPattern[index] = option.getCode();
		}
		
	}

	public static class TimePrefOption implements IsSerializable {
		private char iCode;
		private String iLabel;
		private String iColor;
		
		public TimePrefOption() {}
		public TimePrefOption(char code, String label, String color) {
			iCode = code;
			iLabel = label;
			iColor = color;
		}
		
		public void setCode(char code) { iCode = code; }
		public char getCode() { return iCode; }
		public void setLabel(String label) { iLabel = label; }
		public String getLabel() { return iLabel; }
		public void setColor(String color) { iColor = color; }
		public String getColor() { return iColor; }
		
		@Override
		public String toString() { return getLabel(); }
	}
	
	public static class TimePrefDay implements IsSerializable {
		private int iDayCode;
		private String iLabel;
		
		public TimePrefDay() {}
		public TimePrefDay(int dayCode, String label) {
			iDayCode = dayCode; iLabel = label;
		}
		
		public void setDayCode(int dayCode) { iDayCode = dayCode; }
		public int getDayCode() { return iDayCode; }
		public void setLabel(String label) { iLabel = label; }
		public String getLabel() { return iLabel; }
	}
	
	public static class TimePrefTime implements IsSerializable {
		private int iStartSlot;
		private String iStartTime;
		private String iEndTime;
		
		public TimePrefTime() {}
		public TimePrefTime(int startSlot, String startTime, String endTime) {
			iStartSlot = startSlot; iStartTime = startTime; iEndTime = endTime;
		}
		
		public void setStartSlot(int startSlot) { iStartSlot = startSlot; }
		public int getStartSlot() { return iStartSlot; }
		public void setStartTime(String startTime) { iStartTime = startTime; }
		public String getStartTime() { return iStartTime; }
		public void setEndTime(String endTime) { iEndTime = endTime; }
		public String getEndTime() { return iEndTime; }
	}
}
