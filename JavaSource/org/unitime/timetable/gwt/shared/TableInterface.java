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
package org.unitime.timetable.gwt.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcResponse;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class TableInterface implements GwtRpcResponse, Serializable {
	private static final long serialVersionUID = 1L;
	private TableHeaderIterface[] iHeader = null;
	private List<TableRowInterface> iRows = new ArrayList<TableRowInterface>();
	private String iName, iErrorMessage;
	private Boolean iShowPrefLegend;
	private String iTableId;
	
	public TableInterface() {}
	public TableInterface(String id, String name) {
		iTableId = id;
		iName = name;
	}
	
	public String getTableId() { return iTableId; }
	public void setTableId(String id) { iTableId = id; }

	public boolean hasName() { return iName != null && !iName.isEmpty(); }
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	
	public boolean hasErrorMessage() { return iErrorMessage != null && !iErrorMessage.isEmpty(); }
	public String getErrorMessage() { return iErrorMessage; }
	public void setErrorMessage(String errorMessage) { iErrorMessage = errorMessage; }
	
	public void setShowPrefLegend(boolean showPrefLegend) { iShowPrefLegend = showPrefLegend; }
	public boolean isShowPrefLegend() { return iShowPrefLegend != null && iShowPrefLegend.booleanValue(); }
	public boolean hasColumnDescriptions() {
		if (iHeader == null) return false;
		for (TableHeaderIterface h: iHeader)
			if (h.hasDescription()) return true;
		return false;
	}
	
	public void setHeader(TableHeaderIterface... header) { iHeader = header; }
	public TableHeaderIterface[] getHeader() { return iHeader; }
	public TableHeaderIterface getHeader(int index) {
		return iHeader != null && index < iHeader.length ? iHeader[index] : null;
	}
	
	public List<TableRowInterface> getRows() { return iRows; }
	public void addRow(TableRowInterface row) { iRows.add(row); }
	
	public static class TableHeaderIterface implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private String iName;
		private boolean iComparable = true;
		private Alignment iAlignment = Alignment.LEFT;
		private boolean iVisible = true;
		private String iDescription = null;
		
		public TableHeaderIterface() {}
		
		public TableHeaderIterface(String name, boolean comparable, Alignment alignment) {
			iName = name; iComparable = comparable; iAlignment = alignment;
		}
		
		public TableHeaderIterface(String name, boolean comparable) {
			this(name, comparable, Alignment.LEFT);
		}
		
		public TableHeaderIterface(String name) {
			this(name, true, Alignment.LEFT);
		}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public boolean isComparable() { return iComparable; }
		public TableHeaderIterface setComparable(boolean comparable) { iComparable = comparable; return this; }
		
		public Alignment getAlignment() { return iAlignment; }
		public TableHeaderIterface setAlignment(Alignment alignment) { iAlignment = alignment; return this; }
		
		public boolean isVisible() { return iVisible; }
		public TableHeaderIterface setVisible(boolean visible) { iVisible = visible; return this; }
		
		public boolean hasDescription() { return iDescription != null && !iDescription.isEmpty(); }
		public String getDescription() { return iDescription; }
		public TableHeaderIterface setDescription(String description) { iDescription = description; return this; }
	}
	
	public static class TableRowInterface implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private TableCellInterface[] iCells = null;
		private Long iId;
		private String iLink, iLinkName;
		private boolean iSelected = false;
		
		public TableRowInterface() {}
		
		public TableRowInterface(TableCellInterface... cells) {
			iCells = cells;
		}
		
		public TableRowInterface(Long id, String link, String linkName, TableCellInterface... cells) {
			iId = id;
			iLink = link; iLinkName = linkName;
			iCells = cells;
		}
		
		public TableRowInterface(Long id, boolean selected, TableCellInterface... cells) {
			iId = id;
			iSelected = selected;
			iCells = cells;
		}
		
		public TableCellInterface[] getCells() { return iCells; }
		public void setCells(TableCellInterface... cells) { iCells = cells; }
		public TableCellInterface getCell(int index) {
			return (iCells != null && index < iCells.length ? iCells[index] : null);
		}
		public int getNrCells() { return iCells == null ? 0 : iCells.length; }
		
		public boolean hasLink() { return iLink != null && !iLink.isEmpty(); }
		public String getLink() { return iLink; }
		public void setLink(String link) { iLink = link; }
		public String getLinkName() { return iLinkName; }
		public void setLinkName(String name) { iLinkName = name; }
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public boolean hasId() { return iId != null; }
		
		public TableRowInterface setSelected(boolean selected) { iSelected = selected; return this; }
		public boolean isSelected() { return iSelected; }
		
		public static int compare(TableCellInterface c1, TableCellInterface c2) {
			return (c1 == null ? (c2 == null ? 0 : -1) : c2 == null ? 1 : c1.compareTo(c2));
		}
		
		public int compareTo(TableRowInterface row, int column, boolean asc) {
			int cmp = compare(getCell(column), row.getCell(column));
			if (cmp != 0) return (asc ? cmp : -cmp);
			for (int i = 0; i < Math.max(getNrCells(), row.getNrCells()); i++) {
				cmp = compare(getCell(i), row.getCell(i));
				if (cmp != 0) return cmp;
			}
			return 0;
		}
	}
	
	public static class TableCellInterface<T extends Comparable<T>> implements IsSerializable, Serializable, Comparable<TableCellInterface> {
		private static final long serialVersionUID = 1L;
		private String iFormattedValue;
		private String iStyleName, iColor;
		private T iValue;
		private String iTitle = null;
		private boolean iUnderline = false;
		
		public TableCellInterface() {}
		public TableCellInterface(T value) {
			iValue = value;
		}
		public TableCellInterface(T value, String formatted) {
			iValue = value; iFormattedValue = formatted;
		}

		public T getValue() { return iValue; }
		public TableCellInterface<T> setValue(T value) { iValue = value; return this; }
		public boolean hasValue() { return getValue() != null; }
		
		public TableCellInterface<T> setFormattedValue(String formattedValue) { iFormattedValue = formattedValue; return this; }
		public String getFormattedValue() { return iFormattedValue == null ? iValue == null ? "" : iValue.toString() : iFormattedValue; }
		
		public boolean hasStyleName() { return iStyleName != null && !iStyleName.isEmpty(); }
		public TableCellInterface<T> setStyleName(String styleName) { iStyleName = styleName; return this; }
		public String getStyleName() { return iStyleName; }
		
		public boolean hasColor() { return iColor != null && !iColor.isEmpty(); }
		public TableCellInterface<T> setColor(String color) { iColor = color; return this; }
		public String getColor() { return iColor; }
		
		public boolean hasTitle() { return iTitle != null && !iTitle.isEmpty(); }
		public String getTitle() { return iTitle; }
		public TableCellInterface<T> setTitle(String title) { iTitle = title; return this; }
		
		public boolean isUnderlined() { return iUnderline; }
		public void setUnderlined(boolean underline) { iUnderline = underline; }
		
		@Override
		public int compareTo(TableCellInterface c) {
			try {
				return getValue() == null ? c.getValue() == null ? 0 : -1 : c.getValue() == null ? 1 : getValue().compareTo((T) c.getValue());
			} catch (Exception e) {
				return NaturalOrderComparator.compare(getFormattedValue(), c.getFormattedValue());
			}
		}
	}
	
	public static class TableCellText extends TableCellInterface<String> {
		private static final long serialVersionUID = 1L;
		
		public TableCellText() { super(); }
		public TableCellText(String value) { super(value); }
		
		public String getValueNotNull() { return getValue() != null ? getValue() : ""; }
		
		@Override
		public String getFormattedValue() { return super.getFormattedValue() != null ? super.getFormattedValue() : getValueNotNull(); }
		
		@Override
		public int compareTo(TableCellInterface c) {
			if (c instanceof TableCellText)
				return NaturalOrderComparator.compare(getValueNotNull(), ((TableCellText)c).getValueNotNull());
			return super.compareTo(c);
		}
	}
	
	public static class TableCellBoolean extends TableCellInterface<Boolean> {
		private static final long serialVersionUID = 1L;
		
		public TableCellBoolean() { super(); }
		public TableCellBoolean(Boolean value) { super(value); }
		
		public String getValueNotNull() { return getValue() != null ? (getValue().booleanValue() ? "1" : "0") : ""; }
		
		@Override
		public String getFormattedValue() { return super.getFormattedValue() != null ? super.getFormattedValue() : getValueNotNull(); }
		
		@Override
		public int compareTo(TableCellInterface c) {
			if (c instanceof TableCellBoolean)
				return NaturalOrderComparator.compare(getValueNotNull(), ((TableCellBoolean)c).getValueNotNull());
			return super.compareTo(c);
		}
	}
	
	public static class TableCellClassName extends TableCellText {
		private static final long serialVersionUID = 1L;
		private List<String> iAlternatives = null;
		
		public TableCellClassName() { super(); }
		public TableCellClassName(String value) { super(value); }
		
		public boolean hasAlternatives() { return iAlternatives != null && !iAlternatives.isEmpty(); }
		public List<String> getAlternatives() { return iAlternatives; }
		public void addAlternative(String alternative) {
			if (iAlternatives == null) iAlternatives = new ArrayList<String>();
			iAlternatives.add(alternative);
		}
	}
	
	public static class TableCellClickableClassName extends TableCellClassName {
		private static final long serialVersionUID = 1L;
		private Long iClassId;
		
		public TableCellClickableClassName() { super(); }
		public TableCellClickableClassName(Long id, String value) { super(value); iClassId = id; }
		
		public Long getClassId() { return iClassId; }
		public void setClassId(Long classId) { iClassId = classId; }
	}
	
	public static class TableCellTime extends TableCellText {
		private static final long serialVersionUID = 1L;
		public String iId;
		
		public TableCellTime() { super(); }
		public TableCellTime(String value) { super(value); }
		
		public boolean hasId() { return iId != null && !iId.isEmpty(); }
		public String getId() { return iId; }
		public TableCellTime setId(String id) { iId = id; return this; }
	}
	
	public static class TableCellRooms extends TableCellText {
		private static final long serialVersionUID = 1L;
		public List<String[]> iRooms = new ArrayList<String[]>();
		
		public TableCellRooms() { super(); }
		
		public void add(String name, String color, Long id, String preference) {
			iRooms.add(new String[] {name, color, id.toString(), preference});
		}
		
		public int getNrRooms() { return iRooms.size(); }
		public String getName(int index) { return iRooms.get(index)[0]; }
		public String getColor(int index) { return iRooms.get(index)[1]; }
		public String getId(int index) { return iRooms.get(index)[2]; }
		public String getPreference(int index) { return iRooms.get(index)[3]; }
		
		public String getValue(String separator) {
			String ret = "";
			for (String[] room: iRooms) {
				ret += (ret.isEmpty() ? "" : separator) + room[0];
			}
			return ret;
		}
		
		@Override
		public String getValue() {
			return getValue(", ");
		}
		
		@Override
		public String getFormattedValue() {
			return getValue(", ");
		}
	}
	
	public static class TableCellItems extends TableCellText {
		private static final long serialVersionUID = 1L;
		public List<String[]> iItems = new ArrayList<String[]>();
		
		public TableCellItems() { super(); }
		
		public TableCellItems(Collection<String> items) {
			super();
			if (items != null)
				for (String item: items)
					add(item, null, null);
		}
				
		public void add(String name, String formatted, String color, Long id) {
			iItems.add(new String[] {name, formatted, color, (id == null ? null : id.toString())});
		}
		
		public void add(String name, String color, Long id) {
			iItems.add(new String[] {name, null, color, (id == null ? null : id.toString())});
		}
		
		public int getNrItems() { return iItems.size(); }
		public String getValue(int index) { return iItems.get(index)[0]; }
		public String getFormattedValue(int index) { return iItems.get(index)[1] == null ? iItems.get(index)[0] : iItems.get(index)[1]; }
		public String getColor(int index) { return iItems.get(index)[2]; }
		public String getId(int index) { return iItems.get(index)[3]; }
		
		public String getValue(String separator) {
			String ret = "";
			for (String[] item: iItems) {
				ret += (ret.isEmpty() ? "" : separator) + item[0];
			}
			return ret;
		}

		@Override
		public String getValue() {
			return getValue(", ");
		}
		
		public String getFormattedValue(String separator) {
			String ret = "";
			for (String[] item: iItems) {
				ret += (ret.isEmpty() ? "" : separator) + (item[1] == null ? item[0] : item[1]);
			}
			return ret;
		}
		
		@Override
		public String getFormattedValue() {
			return getFormattedValue(", ");
		}
	}
	
	public static class TableCellMulti extends TableCellText {
		private static final long serialVersionUID = 1L;
		public List<TableCellInterface> iChunks = new ArrayList<TableCellInterface>();
		
		public TableCellMulti() { super(); }
		public TableCellMulti(String value) { super(value); }
		
		public TableCellInterface add(TableCellInterface chunk) {
			iChunks.add(chunk);
			return chunk;
		}
		
		public TableCellInterface add(String chunk) {
			return add(new TableCellText(chunk));
		}
		
		public int getNrChunks() { return iChunks.size(); }
		public TableCellInterface get(int index) { return iChunks.get(index); }
		public TableCellInterface last() { return iChunks.isEmpty() ? null : iChunks.get(iChunks.size() - 1); }
		public List<TableCellInterface> getChunks() { return iChunks; }

		@Override
		public String getValue() {
			String ret = "";
			for (TableCellInterface item: iChunks) {
				Object value = item.getValue();
				ret += (ret == null ? "" : value.toString());
			}
			return ret;
		}
		
		@Override
		public String getFormattedValue() {
			String ret = "";
			for (TableCellInterface item: iChunks) {
				ret += item.getFormattedValue();
			}
			return ret;
		}
		
		@Override
		public int compareTo(TableCellInterface c) {
			if (c instanceof TableCellMulti) {
				TableCellMulti m = (TableCellMulti)c;
				for (int i = 0; i < getNrChunks() && i < m.getNrChunks(); i++) {
					int cmp = get(i).compareTo(m.get(i));
					if (cmp != 0) return cmp;
				}
			}
			return super.compareTo(c);
		}
	}
	
	public static class TableCellMultiLine extends TableCellMulti {
		private static final long serialVersionUID = 1L;
		
		public TableCellMultiLine() { super(); }
	}
	
	public static class TableCellChange extends TableCellText {
		private static final long serialVersionUID = 1L;
		public TableCellInterface iFirst, iSecond;
		
		public TableCellChange() {}
		public TableCellChange(TableCellInterface first, TableCellInterface second) {
			iFirst = first; iSecond = second;
		}
		
		public TableCellInterface getFirst() { return iFirst; }
		public void setFirst(TableCellInterface first) { iFirst = first; }
		
		public TableCellInterface getSecond() { return iSecond; }
		public void setSecond(TableCellInterface second) { iSecond = second; }
		
		@Override
		public String getValue() {
			if (getFirst() != null && getSecond() != null && getFirst().compareTo(getSecond()) == 0) return getFirst().toString();
			return (getFirst() == null ? "N/A" : getFirst()) + " \u2192 " + (getSecond() == null ? "N/A" : getSecond());
		}
		
		@Override
		public String getFormattedValue() {
			if (getFirst() != null && getSecond() != null && getFirst().compareTo(getSecond()) == 0) return getFirst().getFormattedValue();
			return (getFirst() == null ? "N/A" : getFirst().getFormattedValue()) + " \u2192 " + (getSecond() == null ? "N/A" : getSecond().getFormattedValue());
		}
		
		@Override
		public int compareTo(TableCellInterface c) {
			if (c instanceof TableCellChange) {
				TableCellChange ch = (TableCellChange)c;
				int cmp = compare(getFirst(), ch.getFirst());
				if (cmp != 0) return cmp;
				return compare(getSecond(), ch.getSecond());
			}
			return super.compareTo(c);
		}
		
		private static int compare(TableCellInterface c1, TableCellInterface c2) {
			if (c1 == null) return (c2 == null ? 0 : -1);
			if (c2 == null) return 1;
			return c1.compareTo(c2);
		}
	}
	
	public static enum Alignment {
		LEFT, CENTER, RIGHT;
	}


	public static class NaturalOrderComparator {
		static int compareRight(String a, String b) {
			int bias = 0;
			int ia = 0;
			int ib = 0;

			// The longest run of digits wins.  That aside, the greatest
			// value wins, but we can't know that it will until we've scanned
			// both numbers to know that they have the same magnitude, so we
			// remember it in BIAS.
			for (;; ia++, ib++) {
				char ca = charAt(a, ia);
				char cb = charAt(b, ib);

				if (!Character.isDigit(ca) && !Character.isDigit(cb)) {
					return bias;
				} else if (!Character.isDigit(ca)) {
					return -1;
				} else if (!Character.isDigit(cb)) {
					return +1;
				} else if (ca < cb) {
					if (bias == 0) {
						bias = -1;
					}
				} else if (ca > cb) {
					if (bias == 0)
						bias = +1;
				} else if (ca == 0 && cb == 0) {
					return bias;
				}
			}
		}

		@SuppressWarnings("deprecation")
		public static int compare(String a, String b) {
			int ia = 0, ib = 0;
			int nza = 0, nzb = 0;
			char ca, cb;
			int result;

			while (true) {
				// only count the number of zeroes leading the last number compared
				nza = nzb = 0;

				ca = charAt(a, ia); cb = charAt(b, ib);

				// skip over leading spaces or zeros
				while (Character.isSpace(ca) || ca == '0') {
					if (ca == '0') {
						nza++;
					} else {
						// only count consecutive zeroes
						nza = 0;
					}

					ca = charAt(a, ++ia);
				}

				while (Character.isSpace(cb) || cb == '0') {
					if (cb == '0') {
						nzb++;
					} else {
						// only count consecutive zeroes
						nzb = 0;
					}

					cb = charAt(b, ++ib);
				}

				// process run of digits
				if (Character.isDigit(ca) && Character.isDigit(cb)) {
					if ((result = compareRight(a.substring(ia), b.substring(ib))) != 0) {
						return result;
					}
				}

				if (ca == 0 && cb == 0) {
					// The strings compare the same.  Perhaps the caller
					// will want to call strcmp to break the tie.
					return nza - nzb;
				}

				if (ca < cb) {
					return -1;
				} else if (ca > cb) {
					return +1;
				}

				++ia; ++ib;
			}
		}

		static char charAt(String s, int i) {
			if (i >= s.length()) {
				return 0;
			} else {
				return s.charAt(i);
			}
		}
	}
}
