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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Stephanie Scluttenhofer, Tomas Muller
 */
public class AssignClassInstructorsInterface implements IsSerializable, GwtRpcResponse {
	
	public static enum FieldType implements IsSerializable {
		textarea, number, toggle, list, add, delete, hasError;
	}
	
	public static enum Flag implements IsSerializable {
		HIDDEN,
		READ_ONLY,
		UNIQUE,
		UNIQUE_IF_SET,
		NOT_EMPTY,
		FLOAT,
		NEGATIVE,
		DEFAULT_CHECKED,
		HIDE_LABEL,
		;
		
		public int toInt() { return 1 << ordinal(); }
		public boolean has(int flags) { return (flags & toInt()) == toInt(); }
	}
	
	public static enum DataColumn implements IsSerializable {
		CLASS_UID,
		CLASS_PARENT_UID,
		IS_FIRST_RECORD_FOR_CLASS,
		HAS_ERROR,
		CLASS_NAME,
		CLASS_EXTERNAL_UID,
		DELETE,
		ADD,
		INSTR_NAME,
		PCT_SHARE,
		CHECK_CONFICTS,
		RESPONSIBILITY,
		DISPLAY,
		TIME,
		ROOM,
		FUNDING_DEPT
	}
	
	private List<Record> iRecords = new ArrayList<Record>();
	private Field[] iFields = null;
	private boolean iEditable = true;
	private PageName iPageName = null;
	private Long iConfigId = null;
	private Long iOfferingId = null;
	private boolean iSaveSuccessful = true;
	private String iErrors = null;
	private Long iNextConfigId = null;
	private Long iPreviousConfigId = null;
	private String iCourseName = null;
	private String iCourseCoordinators = null;
	private boolean iShowTimeAndRoom = false;
	
	public AssignClassInstructorsInterface() {
	}

	public AssignClassInstructorsInterface(Field... fields) {
		iFields = fields;
	}
	
	public Long getConfigId() { return iConfigId; }
	public void setConfigId(Long configId) { iConfigId = configId; }

	public Long getOfferingId() { return iOfferingId; }
	public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }

	public boolean isSaveSuccessful() { return iSaveSuccessful; }
	public void setSaveSuccessful(boolean iSaveSuccessful) { this.iSaveSuccessful = iSaveSuccessful; }

	public String getErrors() { return iErrors; }
	public void setErrors(String errors) { this.iErrors = errors;}

	public Long getNextConfigId() { return iNextConfigId; }
	public void setNextConfigId(Long nextConfigId) {this.iNextConfigId = nextConfigId; }

	public Long getPreviousConfigId() { return iPreviousConfigId; }
	public void setPreviousConfigId(Long previousConfigId) { this.iPreviousConfigId = previousConfigId; }

	public String getCourseName() { return iCourseName; }
	public void setCourseName(String courseName) { this.iCourseName = courseName;}

	public String getCourseCoordinators() { return iCourseCoordinators; }
	public void setCourseCoordinators(String courseCoordinators) { this.iCourseCoordinators = courseCoordinators; }

	public boolean isShowTimeAndRoom() {
		return iShowTimeAndRoom;
	}

	public void setShowTimeAndRoom(boolean showTimeAndRoom) {
		this.iShowTimeAndRoom = showTimeAndRoom;
	}

	public void setPageName(PageName name) { iPageName = name; }
	
	public boolean hasPageName() { return iPageName != null; }
	
	public PageName getPageName() { return iPageName; } 
	
	public List<Record> getRecords() { return iRecords; }
	public Record addRecord(Long uniqueId, boolean deletable) {
		Record r = new Record(uniqueId, iFields.length, deletable);
		for (int i = 0; i < iFields.length; i++)
			if (!iFields[i].isEditable()) r.setField(i, (String)null, false);
		iRecords.add(r);
		return r;
	}
	
	public Record addRecord(Long uniqueId) {
		return addRecord(uniqueId, true);
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
	
	public boolean isAllInstructorsDeletable() {
		for (Record r : getRecords()) {
			if (!r.isEditable()) {
				return false;
			} 
		}
		return true;
	}
	
	public RecordComparator getComparator() {
		return new RecordComparator();
	}
	
	public class RecordComparator implements Comparator<Record> {

		public int compare(int index, Record r1, Record r2) {
			if (index < 0)
				return (r1.getUniqueId() == null ? r2.getUniqueId() == null ? 0 : 1 : r1.getUniqueId() == null ? -1 : r1.getUniqueId().compareTo(r2.getUniqueId()));
			Field field = getFields()[index]; 
			String s1 = r1.getText(field, index);
			String s2 = r2.getText(field, index);
			if (s1 == null) return (s2 == null ? 0 : 1);
			if (s2 == null) return -1;
			switch (field.getType()) {
			default:
				try {
					Double d1 = Double.parseDouble(s1.isEmpty() ? "0": s1);
					Double d2 = Double.parseDouble(s2.isEmpty() ? "0": s2);
					return d1.compareTo(d2);
				} catch (NumberFormatException e) {
					return s1.compareTo(s2);
				}
			}
		}
		
		public int compare(Record r1, Record r2) {
			for (int i = 0; i < r1.getValues().length; i++) {
				int cmp = compare(i, r1, r2);
				if (cmp != 0) return cmp;
			}
			return compare(-1, r1, r2);
		}
	}
	
	public static class Record implements IsSerializable, GwtRpcResponse {
		private Long iUniqueId = null;
		private String[] iValues = null;
		private boolean[] iEditable = null;
		private boolean[] iVisible = null;
		private boolean iDeletable = true;
		
		public Record() {
		}
		
		public Record(Long uniqueId, int nrFields, boolean deletable) {
			iUniqueId = uniqueId;
			iValues = new String[nrFields];
			iEditable = new boolean[nrFields];
			iVisible = new boolean[nrFields];
			for (int i = 0; i < nrFields; i++) {
				iValues[i] = null;
				iEditable[i] = true;
			}
			iDeletable = deletable;
		}
		
		public Record(Long uniqueId, int nrFields) {
			this(uniqueId, nrFields, true);
		}
		
		public Long getUniqueId() { return iUniqueId; }
		public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
		
		public void setField(int index, String value, boolean editable) {
			iValues[index] = value;
			iEditable[index] = editable;
		}
		
		public void setField(int index, Record record) {
			iValues[index] = record.getField(index);
			iEditable[index] = record.isEditable(index);
			iVisible[index] = record.isVisible(index);
		}
		
		public void setField(int index, Record record, boolean visible) {
			iValues[index] = record.getField(index);
			iEditable[index] = record.isEditable(index);
			iVisible[index] = visible;
		}
		
		public void setField(int index, String value, boolean editable, boolean visible) {
			iValues[index] = value;
			iEditable[index] = editable;
			iVisible[index] = visible;
		}

		public void setField(int index, String value) {
			iValues[index] = value;
		}
		
		public String getField(int index) {
			return iValues[index];
		}
		
		public boolean isEditable(int index) {
			return iEditable[index];
		}
		
		public boolean isVisible(int index) {
			return iVisible[index];
		}
		
		public boolean isEditable() {
			for (boolean editable: iEditable)
				if (editable) return true;
			return false;
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
		
		public boolean isDeletable() { return iDeletable; }
		public void setDeletable(boolean deletable) { iDeletable = deletable; }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Record)) return false;
			Record r = (Record)o;
			if (getUniqueId() != null) return getUniqueId().equals(r.getUniqueId());
			return (r.getUniqueId() != null ? false : super.equals(o));
		}
		
		public void copyFrom(Record record) {
			iUniqueId = record.iUniqueId;
			iValues = record.iValues;
			iEditable = record.iEditable;
			iDeletable = record.iDeletable;
			iVisible = record.iVisible;
		}
		
		public void copyTo(Record record) {
			record.copyFrom(this);
		}
		
		public Record cloneRecord() {
			Record r = new Record(iUniqueId, iValues.length, iDeletable);
			for (int i = 0; i < iValues.length; i++) {
				r.iValues[i] = iValues[i];
				r.iEditable[i] = iEditable[i];
				r.iVisible[i] = iVisible[i];
			}
			return r;
		}
	}
	
	public static class ListItem implements IsSerializable, Comparable<ListItem> {
		private String iValue, iText;
		public ListItem() {}
		public ListItem(String value, String text) {
			iValue = value; iText = text;
		}
		public String getValue() { return iValue; }
		public String getText() { return iText; }
		@Override
		public int compareTo(ListItem o) {
			return getText().compareTo(o.getText());
		}
		@Override
		public int hashCode() { return getText().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ListItem)) return false;
			ListItem i = (ListItem)o;
			return getValue().equals(i.getValue());
		}
	}
	
	public static class Field implements IsSerializable {
		private String iName = null;
		private FieldType iType = null;
		private int iLength = 0, iWidth = 0, iHeight = 1, iFlags = 0;
		private List<ListItem> iValues = null;
		
		public Field() {}
		
		public Field(String name, FieldType type, int width, int height, int length, Flag... flags) {
			iName = name;
			iType = type;
			iWidth = width;
			iHeight = height;
			iLength = length;
			iFlags = 0;
			for (Flag flag: flags)
				if (flag != null)
					iFlags = iFlags | flag.toInt();
		}
				
		public Field(String name, FieldType type, int width, Flag... flags) {
			this(name, type, width, 1, 0, flags);
		}
		
		public Field(String name, FieldType type, int width, int length, Flag... flags) {
			this(name, type, width, 1, length, flags);
		}
		
		public Field(String name, FieldType type, int width, List<ListItem> values, Flag... flags) {
			this(name, type, width, 0, flags);
			iValues = values;
		}
		
		public String getName() { return iName; }
		public FieldType getType() { return iType; }
		public int getLength() { return iLength; }
		public int getWidth() { return iWidth; }
		public int getHeight() { return iHeight; }
		public List<ListItem> getValues() { return iValues; }
		public void addValue(ListItem item) {
			if (iValues == null) iValues = new ArrayList<ListItem>();
			iValues.add(item);
		}
		public boolean isEditable() { return !Flag.READ_ONLY.has(iFlags); }
		public boolean isVisible() { return !Flag.HIDDEN.has(iFlags); }
		public boolean isUnique() { return Flag.UNIQUE.has(iFlags); }
		public boolean isUniqueIfSet() { return Flag.UNIQUE_IF_SET.has(iFlags); }
		public boolean isNotEmpty() { return Flag.NOT_EMPTY.has(iFlags); }
		public boolean isAllowFloatingPoint() { return Flag.FLOAT.has(iFlags); }
		public boolean isAllowNegative() { return Flag.NEGATIVE.has(iFlags); }
		public boolean isCheckedByDefault() { return Flag.DEFAULT_CHECKED.has(iFlags); }
		public boolean isHideLabel() { return Flag.HIDE_LABEL.has(iFlags); }
		
		public int hashCode() {
			return getName().hashCode();
		}
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Field)) return false;
			return getName().equals(((Field)o).getName());
		}
	}
	
	public static class PageName implements IsSerializable, GwtRpcResponse {
		private String iSingular = null, iPlural = null;
		
		public PageName() {}
		public PageName(String name) { iSingular = name; }
		public PageName(String singularName, String pluralName) { iSingular = singularName; iPlural = pluralName; }
		
		public String singular() { return iSingular; }
		public String plural() { return iPlural == null ? iSingular + "s" : iPlural; }
		
		public String toString() { return plural(); }
	}
	
	public static abstract class SimpleEditRpcRequest implements IsSerializable {
		
		public SimpleEditRpcRequest() {}
		
	}
	
	public static class GetPageNameRpcRequest extends SimpleEditRpcRequest implements GwtRpcRequest<PageName> {
		public GetPageNameRpcRequest() {}
		
		public static GetPageNameRpcRequest getPageName() {
			GetPageNameRpcRequest request = new GetPageNameRpcRequest();
			return request;
		}
	}
	
	public static class LoadDataRpcRequest extends SimpleEditRpcRequest implements GwtRpcRequest<AssignClassInstructorsInterface> {
		private String iConfigIdStr;
		
		public LoadDataRpcRequest() {}
				
		public String getConfigIdStr() { return iConfigIdStr; }
		public void setConfigIdStr(String configIdStr) { this.iConfigIdStr = configIdStr; }

		public static LoadDataRpcRequest loadData(String configIdStr) {
			LoadDataRpcRequest request = new LoadDataRpcRequest();
			request.setConfigIdStr(configIdStr);
			return request;
		}
	}
	
	public static class SaveDataRpcRequest extends SimpleEditRpcRequest implements GwtRpcRequest<AssignClassInstructorsInterface> {
		private AssignClassInstructorsInterface iData;
		
		public SaveDataRpcRequest() {}

		public AssignClassInstructorsInterface getData() { return iData; }
		public void setData(AssignClassInstructorsInterface data) { iData = data; }

		public static SaveDataRpcRequest saveData(AssignClassInstructorsInterface data) {
			SaveDataRpcRequest request = new SaveDataRpcRequest();
			request.setData(data);
			return request;
		}

	}

	public static class SaveDataGoToPreviousRpcRequest extends SimpleEditRpcRequest implements GwtRpcRequest<AssignClassInstructorsInterface> {
		private AssignClassInstructorsInterface iData;
		
		public SaveDataGoToPreviousRpcRequest() {}

		public AssignClassInstructorsInterface getData() { return iData; }
		public void setData(AssignClassInstructorsInterface data) { iData = data; }

		public static SaveDataGoToPreviousRpcRequest saveDataAndPrev(AssignClassInstructorsInterface data) {
			SaveDataGoToPreviousRpcRequest request = new SaveDataGoToPreviousRpcRequest();
			request.setData(data);
			return request;
		}

	}

	public static class SaveDataGoToNextRpcRequest extends SimpleEditRpcRequest implements GwtRpcRequest<AssignClassInstructorsInterface> {
		private AssignClassInstructorsInterface iData;
		
		public SaveDataGoToNextRpcRequest() {}

		public AssignClassInstructorsInterface getData() { return iData; }
		public void setData(AssignClassInstructorsInterface data) { iData = data; }

		public static SaveDataGoToNextRpcRequest saveDataAndNext(AssignClassInstructorsInterface data) {
			SaveDataGoToNextRpcRequest request = new SaveDataGoToNextRpcRequest();
			request.setData(data);
			return request;
		}

	}
	
	public static class RemoveAllClassInstructorsDataRpcRequest extends SimpleEditRpcRequest implements GwtRpcRequest<AssignClassInstructorsInterface> {
		private AssignClassInstructorsInterface iData;
		
		public RemoveAllClassInstructorsDataRpcRequest() {}

		public AssignClassInstructorsInterface getData() { return iData; }
		public void setData(AssignClassInstructorsInterface data) { iData = data; }

		public static RemoveAllClassInstructorsDataRpcRequest removeInstructorData(AssignClassInstructorsInterface data) {
			RemoveAllClassInstructorsDataRpcRequest request = new RemoveAllClassInstructorsDataRpcRequest();
			request.setData(data);
			return request;
		}
	}
	
}
