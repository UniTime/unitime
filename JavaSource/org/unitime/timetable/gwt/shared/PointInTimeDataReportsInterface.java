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

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseBoolean;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseLong;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Stephanie Schluttenhofer
 */
public class PointInTimeDataReportsInterface implements IsSerializable {
	
	public static class Flag implements IsSerializable {
		private int iValue;
		private String iText;
		
		public Flag() {}
		
		public int getValue() { return iValue; }
		public void setValue(int value) { iValue = value; }
		
		public String getText() { return iText; }
		public void setText(String text) { iText = text; }
		
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
	
	public static class Parameter implements IsSerializable {
		private String iType, iName;
		private List<IdValue> iValues = new ArrayList<IdValue>();
		private boolean iMultiSelect;
		private boolean iTextField;
		private String iDefaultTextValue;
		
		public Parameter() {}
		
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public List<IdValue> values() { return iValues; }
		
		public boolean isMultiSelect() { return iMultiSelect; }
		public void setMultiSelect(boolean multiSelect) { iMultiSelect = multiSelect; }

		public boolean isTextField() { return iTextField; }
		public void setTextField(boolean textField) { this.iTextField = textField; }
		
		public String getDefaultTextValue() { return iDefaultTextValue; }
		public void setDefaultTextValue(String defaultValue) { this.iDefaultTextValue = defaultValue; }
	}
	
	public static class Report implements GwtRpcResponse {
		private String iId = null;
		private String iName, iDescription;
		private List<Parameter> iParameters = new ArrayList<Parameter>();
		private int iFlags = 0;

		public Report() {}
		
		public String getId() { return iId; }
		public void setId(String id) { iId = id; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public String getDescription() { return iDescription == null ? "" : iDescription; }
		public void setDescription(String description) { iDescription = description; }
		public int getFlags() { return iFlags; }
		public void setFlags(int flags) { iFlags = flags; }
		public void addParameter(Parameter parameter) { iParameters.add(parameter); }
		public List<Parameter> getParameters() { return iParameters; }
		
		@Override
		public String toString() { return getName(); }
		
		public boolean parametersContain(String type) {
			if (type == null || type.isEmpty()){
				return(false);
			}
			boolean found = false;
			for (Parameter param : getParameters()){
				if (type.equals(param.getType())){
					found = true;
					break;
				}
			}
			return(found);
		}
	}
	
	public static class PITDParametersInterface implements GwtRpcResponse {
		private List<Flag> iFlags = new ArrayList<Flag>();
		private List<Parameter> iParameters = new ArrayList<Parameter>();
		private boolean iEditable = false;
		
		public PITDParametersInterface() {}
		
		public void addFlag(Flag flag) { iFlags.add(flag); }
		public List<Flag> getFlags() { return iFlags; }
		
		public void addParameter(Parameter parameter) { iParameters.add(parameter); }
		public List<Parameter> getParameters() { return iParameters; }
		
		public void setEditable(boolean editable) { iEditable = editable; }
		public boolean isEditable() { return iEditable; }
	}
	
	public static class PITDParametersRpcRequest implements GwtRpcRequest<PITDParametersInterface> {
		@Override
		public String toString() { return null; }
	}
	
	public static class PITDQueriesRpcRequest implements GwtRpcRequest<GwtRpcResponseList<Report>> {
		
		public PITDQueriesRpcRequest() {}
	}
	
	public static class Table implements GwtRpcResponse {
		private List<String[]> iData = new ArrayList<String[]>();
		
		public Table() {}
		
		public void add(String... line) { iData.add(line); }
		public int size() { return iData.size(); }
		public String[] get(int row) { return iData.get(row); }
	}
	
	public static class PITDExecuteRpcRequest implements GwtRpcRequest<Table> {
		private Report iReport;
		private List<IdValue> iParameters = new ArrayList<IdValue>();
		
		public PITDExecuteRpcRequest() {}
		
		public void setReport(Report report) { iReport = report; }
		public Report getReport() { return iReport; }
		
		public void addParameter(String value, String text) {
			iParameters.add(new IdValue(value, text));
		}
		public List<IdValue> getParameters() { return iParameters; }
				
		@Override
		public String toString() {
			return iReport.getName() + " {options: " + getParameters() + "}";
		}
	}
	
	public static class PITDStoreRpcRequest extends Report implements GwtRpcRequest<GwtRpcResponseLong> {
		public PITDStoreRpcRequest() { super(); }
		public PITDStoreRpcRequest(Report query) {
			super();
			setId(query.getId());
			setName(query.getName());
			setDescription(query.getDescription());
			setFlags(query.getFlags());
		}
	}
	
	public static class PITDDeleteRpcRequest implements GwtRpcRequest<GwtRpcResponseBoolean> {
		private Long iId = null;

		public PITDDeleteRpcRequest() {}
		public PITDDeleteRpcRequest(Long id) {
			iId = id;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		@Override
		public String toString() {
			return iId == null ? "null" : iId.toString();
		}
	}
	
	public static class PITDSetBackRpcRequest implements GwtRpcRequest<GwtRpcResponseNull> {
		private String iHistory;
		private List<Long> iIds = new ArrayList<Long>();
		private String iType;
		
		public PITDSetBackRpcRequest() {}		
		
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
		public String toString() { return"courses" + "#" + getHistory(); }
	}
}
