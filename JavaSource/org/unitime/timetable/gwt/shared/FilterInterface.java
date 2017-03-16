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
import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcResponse;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class FilterInterface implements GwtRpcResponse, Serializable {
	private static final long serialVersionUID = 0l;
	private List<FilterParameterInterface> iParameters;
	
	public boolean hasParameters() { return iParameters != null && !iParameters.isEmpty(); }
	public List<FilterParameterInterface> getParameters() { return iParameters; }
	public void addParameter(FilterParameterInterface parameter) {
		if (iParameters == null)
			iParameters = new ArrayList<FilterParameterInterface>();
		iParameters.add(parameter);
	}
	public FilterParameterInterface getParameter(String name) {
		if (iParameters == null) return null;
		for (FilterParameterInterface param: iParameters)
			if (name.equals(param.getName())) return param;
		return null;
	}
	
	public String getParameterValue(String name) {
		FilterParameterInterface param = getParameter(name);
		return (param == null ? null : param.getValue() == null ? param.getDefaultValue() : param.getValue());
	}
	
	public String getParameterValue(String name, String defaultValue) {
		String value = getParameterValue(name);
		return value == null ? defaultValue : value;
	}

	public static class FilterParameterInterface implements IsSerializable, Comparable<FilterParameterInterface> {
		private String iName, iLabel, iType, iValue, iDefault;
		private List<ListItem> iOptions = null;
		private boolean iMultiSelect = false;
		
		public FilterParameterInterface() {}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }

		public String getDefaultValue() { return iDefault; }
		public void setDefaultValue(String defaultValue) {
			iDefault = defaultValue;
			if (hasOptions() && defaultValue != null) {
				for (ListItem option: getOptions())
					if (defaultValue.equals(option.getValue())) return;
				iDefault = getOptions().get(0).getValue();
			}
		}
		
		public boolean hasOptions() { return iOptions != null && !iOptions.isEmpty(); }
		public void addOption(String value, String text) {
			if (iOptions == null)
				iOptions = new ArrayList<ListItem>();
			iOptions.add(new ListItem(value, text));
		}
		public List<ListItem> getOptions() { return iOptions; }
		
		public boolean isMultiSelect() { return iMultiSelect; }
		public void setMultiSelect(boolean multiSelect) { iMultiSelect = multiSelect; }
		
		@Override
		public String toString() {
			return getName() + "=" + (getValue() == null ? getDefaultValue() : getValue());
		}
		
		@Override
		public int compareTo(FilterParameterInterface o) {
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
		
		@Override
		public String toString() {
			return getValue() + ": " + getText();
		}
	}	
}
