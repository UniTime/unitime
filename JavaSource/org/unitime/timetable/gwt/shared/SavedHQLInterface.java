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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseBoolean;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseLong;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class SavedHQLInterface implements IsSerializable {
	
	public static class Flag implements IsSerializable {
		private int iValue;
		private String iText;
		private String iAppearance;
		
		public Flag() {}
		
		public int getValue() { return iValue; }
		public void setValue(int value) { iValue = value; }
		
		public String getText() { return iText; }
		public void setText(String text) { iText = text; }
		
		public boolean isAppearance() { return iAppearance != null; }
		public String getAppearance() { return iAppearance; }
		public void setAppearance(String appearance) { iAppearance = appearance; }
	}
	
	public static class IdValue implements IsSerializable, Comparable<IdValue> {
		private String iValue, iText;
		
		public IdValue() {}
		public IdValue(String value, String text) {
			iValue = value;
			iText = text;
		}
		
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }
		
		public String getText() { return iText; }
		public void setText(String text) { iText = text; }

		@Override
		public int compareTo(IdValue o) {
			return getText().compareTo(o.getText());
		}
		
		@Override
		public String toString() { return getValue() + ": " + getText(); }
	}
	
	public static class Option implements IsSerializable {
		private String iType, iName;
		private List<IdValue> iValues = new ArrayList<IdValue>();
		private boolean iMultiSelect;
		
		public Option() {}
		
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public List<IdValue> values() { return iValues; }
		
		public boolean isMultiSelect() { return iMultiSelect; }
		public void setMultiSelect(boolean multiSelect) { iMultiSelect = multiSelect; }
	}
	
	public static class Query implements GwtRpcResponse {
		private Long iId = null;
		private String iName, iDescription, iQuery;
		private int iFlags = 0;
		private Set<Parameter> iParameters;

		public Query() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public String getDescription() { return iDescription == null ? "" : iDescription; }
		public void setDescription(String description) { iDescription = description; }
		public String getQuery() { return iQuery; }
		public void setQuery(String query) { iQuery = query; }
		public int getFlags() { return iFlags; }
		public void setFlags(int flags) { iFlags = flags; }
		
		public boolean hasParameters() { return iParameters != null && !iParameters.isEmpty(); }
		public Set<Parameter> getParameters() { return iParameters; }
		public void addParameter(Parameter parameter) {
			if (iParameters == null)
				iParameters = new TreeSet<Parameter>();
			iParameters.add(parameter);
		}
		public void clearParameters() {
			if (iParameters != null) iParameters.clear();
		}
		
		@Override
		public String toString() { return getName(); }
	}
	
	public static class Parameter implements IsSerializable, Comparable<Parameter> {
		private String iName, iLabel, iType, iValue, iDefault;
		private Set<ListItem> iOptions = null;
		private boolean iMultiSelect = false;
		
		public Parameter() {}
		public Parameter(Parameter p) {
			iName = p.iName; iLabel = p.iLabel; iType = p.iType; iValue = p.iValue; iDefault = p.iDefault;
			iOptions = p.iOptions; iMultiSelect = p.iMultiSelect;
		}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }

		public String getDefaultValue() { return iDefault; }
		public void setDefaultValue(String defaultValue) { iDefault = defaultValue; }
		
		public boolean hasOptions() { return iOptions != null && !iOptions.isEmpty(); }
		public void addOption(String value, String text) {
			if (iOptions == null)
				iOptions = new TreeSet<ListItem>();
			iOptions.add(new ListItem(value, text));
		}
		public Set<ListItem> getOptions() { return iOptions; }
		
		public boolean isMultiSelect() { return iMultiSelect; }
		public void setMultiSelect(boolean multiSelect) { iMultiSelect = multiSelect; }
		
		@Override
		public String toString() {
			return getName() + "=" + (getValue() == null ? getDefaultValue() : getValue());
		}
		
		@Override
		public int compareTo(Parameter o) {
			int cmp = getLabel().compareTo(o.getLabel());
			if (cmp != 0) return cmp;
			return getName().compareTo(o.getName());
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
		public int compareTo(ListItem item) {
			int cmp = getText().compareTo(item.getText());
			if (cmp != 0) return cmp;
			return getValue().compareTo(item.getValue());
		}
	}
	
	public static class HQLOptionsInterface implements GwtRpcResponse {
		private List<Flag> iFlags = new ArrayList<Flag>();
		private List<Option> iOptions = new ArrayList<Option>();
		private boolean iEditable = false;
		
		public HQLOptionsInterface() {}
		
		public void addFlag(Flag flag) { iFlags.add(flag); }
		public List<Flag> getFlags() { return iFlags; }
		
		public void addOption(Option option) { iOptions.add(option); }
		public List<Option> getOptions() { return iOptions; }
		
		public void setEditable(boolean editable) { iEditable = editable; }
		public boolean isEditable() { return iEditable; }
	}
	
	public static class HQLOptionsRpcRequest implements GwtRpcRequest<HQLOptionsInterface> {
		@Override
		public String toString() { return null; }
	}
	
	public static class HQLQueriesRpcRequest implements GwtRpcRequest<GwtRpcResponseList<Query>> {
		private String iAppearance;
		
		public HQLQueriesRpcRequest() {}
		public HQLQueriesRpcRequest(String appearance) {
			iAppearance = appearance;
		}
		
		public void setAppearance(String appearance) {
			iAppearance = appearance;
		}
		
		public String getAppearance() { return iAppearance; }
		
		@Override
		public String toString() { return iAppearance; }
	}
	
	public static class Table implements GwtRpcResponse {
		private List<String[]> iData = new ArrayList<String[]>();
		
		public Table() {}
		
		public void add(String... line) { iData.add(line); }
		public int size() { return iData.size(); }
		public String[] get(int row) { return iData.get(row); }
	}
	
	public static class HQLExecuteRpcRequest implements GwtRpcRequest<Table> {
		private Query iQuery;
		private List<IdValue> iOptions = new ArrayList<IdValue>();
		private int iFromRow, iMaxRows;
		
		public HQLExecuteRpcRequest() {}
		
		public void setQuery(Query query) { iQuery = query; }
		public Query getQuery() { return iQuery; }
		
		public void addOption(String value, String text) {
			iOptions.add(new IdValue(value, text));
		}
		public List<IdValue> getOptions() { return iOptions; }
		
		public void setFromRow(int fromRow) { iFromRow = fromRow; }
		public int getFromRow() { return iFromRow; }
		
		public void setMaxRows(int maxRows) { iMaxRows = maxRows; }
		public int getMaxRows() { return iMaxRows; }
		
		@Override
		public String toString() {
			return iQuery.getName() + " {options: " + getOptions() + ", from:" + getFromRow() + ", max:" + getMaxRows() + "}";
		}
	}
	
	public static class HQLStoreRpcRequest extends Query implements GwtRpcRequest<GwtRpcResponseLong> {
		public HQLStoreRpcRequest() { super(); }
		public HQLStoreRpcRequest(Query query) {
			super();
			setId(query.getId());
			setName(query.getName());
			setDescription(query.getDescription());
			setQuery(query.getQuery());
			setFlags(query.getFlags());
			if (query.hasParameters())
				for (Parameter p: query.getParameters())
					addParameter(new Parameter(p));
		}
	}
	
	public static class HQLDeleteRpcRequest implements GwtRpcRequest<GwtRpcResponseBoolean> {
		private Long iId = null;

		public HQLDeleteRpcRequest() {}
		public HQLDeleteRpcRequest(Long id) {
			iId = id;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		@Override
		public String toString() {
			return iId == null ? "null" : iId.toString();
		}
	}
	
	public static class HQLSetBackRpcRequest implements GwtRpcRequest<GwtRpcResponseNull> {
		private String iAppearance;
		private String iHistory;
		private List<Long> iIds = new ArrayList<Long>();
		private String iType;
		
		public HQLSetBackRpcRequest() {}
		
		public String getAppearance() { return iAppearance; }
		public void setAppearance(String appearance) { iAppearance = appearance; }
		
		public String getHistory() { return iHistory; }
		public void setHistory(String history) { iHistory = history; }

		public void addId(Long id) {
			if (!iIds.contains(id))
				iIds.add(id);
		}
		public List<Long> getIds() { return iIds; }
		
		public void setType(String type) { iType = type; }
		public String getType() { return iType; }
		
		@Override
		public String toString() { return getAppearance() + "#" + getHistory(); }
	}
}
