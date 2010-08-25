package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SimpleEditInterface implements IsSerializable {
	
	public static enum Type {
		area("Academic Areas"),
		classification("Academic Classifications"),
		major("Majors"),
		minor("Minors");
	
		private String iPageName;
		
		Type(String pageName) {
			iPageName = pageName;
		}
		
		public String getTitle() { return iPageName; }
	}
	
	public static enum FieldType {
		text, toggle, list, multi;
	}
	
	private Type iType = null;
	private List<Record> iRecords = new ArrayList<Record>();
	private Field[] iFields = null;
	private boolean iEditable = true;
	private int[] iSort = null;
	
	public SimpleEditInterface() {
	}

	public SimpleEditInterface(Type type, Field... fields) {
		iType = type;
		iFields = fields;
	}
	
	public Type getType() { return iType; }
	
	public List<Record> getRecords() { return iRecords; }
	public Record addRecord(Long uniqueId) {
		Record r = new Record(uniqueId, iFields.length);
		iRecords.add(r);
		return r;
	}
	
	public Record insertEmptyRecord(int pos) {
		Record r = new Record(null, iFields.length);
		iRecords.add(pos, r);
		return r;
	}
	
	public void moveRecord(int row, int before) {
		Record r = iRecords.get(row);
		iRecords.remove(row);
		iRecords.add(before + (row < before ? -1 : 0), r);
	}
	
	public Record getRecord(Long uniqueId) {
		for (Record r: iRecords)
			if (r.getUniqueId() != null && r.getUniqueId().equals(uniqueId))
				return r;
		return null;
	}
	
	public List<Record> getNewRecords() {
		List<Record> ret = new ArrayList<Record>();
		for (Record r: iRecords) {
			if (r.getUniqueId() != null || r.isEmpty()) continue;
			ret.add(r);
		}
		return ret;
	}
	
	public Field[] getFields() { return iFields; }
	
	public int indexOf(String name) {
		for (int i = 0; i < iFields.length; i++)
			if (iFields[i].getName().equals(name)) return i;
		return -1;
	}
	
	public boolean isEditable() { return iEditable; }
	public void setEditable(boolean editable) { iEditable = editable; }
	
	public int[] getSortBy() { return iSort; }
	public void setSortBy(int... columns) { iSort = columns; }
	public Comparator<Record> getComparator() {
		return new RecordComparator();
	}
	
	public class RecordComparator implements Comparator<Record> {
		public int compare(Record r1, Record r2) {
			if (getSortBy() != null) {
				for (int i: getSortBy()) {
					String s1 = r1.getText(getFields()[i], i);
					String s2 = r2.getText(getFields()[i], i);
					int cmp = s1.compareTo(s2);
					if (cmp != 0) return cmp;
				}
			} else {
				for (int i = 0; i < r1.getValues().length; i++) {
					String s1 = r1.getText(getFields()[i], i);
					String s2 = r2.getText(getFields()[i], i);
					int cmp = s1.compareTo(s2);
					if (cmp != 0) return cmp;
				}
			}
			return r1.getUniqueId().compareTo(r2.getUniqueId());
		}
	}
	
	public static class Record implements IsSerializable {
		private Long iUniqueId = null;
		private String[] iValues = null;
		
		public Record() {
		}
		
		public Record(Long uniqueId, int nrFields) {
			iUniqueId = uniqueId;
			iValues = new String[nrFields];
			for (int i = 0; i < nrFields; i++)
				iValues[i] = null;
		}
		
		public Long getUniqueId() { return iUniqueId; }
		public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
		
		public void setField(int index, String value) {
			iValues[index] = value;
		}
		
		public String getField(int index) {
			return iValues[index];
		}
		
		public void addToField(int index, String value) {
			if (iValues[index] == null)
				iValues[index] = value;
			else
				iValues[index] += "|" + value;
		}
		
		public String[] getValues(int index) {
			return (iValues[index] == null ? new String[] {} : iValues[index].split("\\|"));
		}
		
		public String[] getValues() { return iValues; }
		public void setValues(String[] values) { iValues = values; }
		
		public String getText(Field f, int index) {
			String value = getField(index);
			if (value == null) return "";
			if (f.getType() == FieldType.list) {
				for (ListItem item: f.getValues()) {
					if (item.getValue().equals(value)) return item.getText();
				}
			} else if (f.getType() == FieldType.multi) {
				String text = "";
				for (String val: getValues(index)) {
					for (ListItem item: f.getValues()) {
						if (item.getValue().equals(val)) {
							if (!text.isEmpty()) text += ", ";
							text += item.getText();
						}
					}
				}
				return text;
			}
			return value;
		}
		
		public boolean isEmpty() {
			if (getUniqueId() != null) return false;
			for (String v: iValues) {
				if (v != null && !v.isEmpty()) return false;
			}
			return true;
		}
	}
	
	public static class ListItem implements IsSerializable {
		private String iValue, iText;
		public ListItem() {}
		public ListItem(String value, String text) {
			iValue = value; iText = text;
		}
		public String getValue() { return iValue; }
		public String getText() { return iText; }
	}
	
	public static class Field implements IsSerializable {
		private String iName = null;
		private FieldType iType = null;
		private int iLength = 0, iWidth = 0;
		private List<ListItem> iValues = null;
		
		public Field() {}
		
		public Field(String name, FieldType type, int width) {
			iName = name;
			iType = type;
			iWidth = width;
		}
		
		public Field(String name, FieldType type, int width, int length) {
			this(name, type, width);
			iLength = length;
		}
		
		public Field(String name, FieldType type, int width, List<ListItem> values) {
			this(name, type, width);
			iValues = values;
		}

		public String getName() { return iName; }
		public FieldType getType() { return iType; }
		public int getLength() { return iLength; }
		public int getWidth() { return iWidth; }
		public List<ListItem> getValues() { return iValues; }
		public void addValue(ListItem item) {
			if (iValues == null) iValues = new ArrayList<ListItem>();
			iValues.add(item);
		}
		
		public int hashCode() {
			return getName().hashCode();
		}
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Field)) return false;
			return getName().equals(((Field)o).getName());
		}
	}
}
