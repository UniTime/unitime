/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.List;

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
		
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }
		
		public String getText() { return iText; }
		public void setText(String text) { iText = text; }

		@Override
		public int compareTo(IdValue o) {
			return getText().compareTo(o.getText());
		}
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
	
	public static class Query implements IsSerializable {
		private Long iId = null;
		private String iName, iDescription, iQuery;
		private int iFlags = 0;

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
	}
	
}
