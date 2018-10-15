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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.HasPageMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class TimetableGridInterface implements GwtRpcResponse {
	
	public static class TimetableGridFilterRequest implements GwtRpcRequest<TimetableGridFilterResponse>, Serializable {
		private static final long serialVersionUID = 0l;
		
	}
	
	public static class TimetableGridFilterResponse extends FilterInterface implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 0l;
		
	}
	
	public static class TimetableGridRequest implements GwtRpcRequest<TimetableGridResponse>, Serializable {
		private static final long serialVersionUID = 0l;
		private FilterInterface iFilter;
		
		public FilterInterface getFilter() { return iFilter; }
		public void setFilter(FilterInterface filter) { iFilter = filter; }
	}
	
	public static class TimetableGridResponse implements GwtRpcResponse, Serializable, HasPageMessages {
		private static final long serialVersionUID = 0l;
		private List<TimetableGridModel> iModels = new ArrayList<TimetableGridModel>();
		private String iDefaultDatePatternName;
		private List<PageMessage> iPageMessages = null;
		private List<TimetableGridLegend> iAssignedLegend = new ArrayList<TimetableGridLegend>();
		private List<TimetableGridLegend> iNotAssignedLegend = new ArrayList<TimetableGridLegend>();
		private int iWeekOffset = 0;
		
		public void addModel(TimetableGridModel model) { iModels.add(model); }
		public List<TimetableGridModel> getModels() { return iModels; }
		
		public void setDefaultDatePatternName(String datePatternName) { iDefaultDatePatternName = datePatternName; }
		public String getDefaultDatePatternName() { return iDefaultDatePatternName; }
		
		public boolean hasPageMessages() { return iPageMessages != null && !iPageMessages.isEmpty(); }
		public List<PageMessage> getPageMessages() { return iPageMessages; }
		public void addPageMessage(PageMessage message) {
			if (iPageMessages == null) iPageMessages = new ArrayList<PageMessage>();
			iPageMessages.add(message);
		}
		
		public void addAssignedLegend(String color, String label) { iAssignedLegend.add(new TimetableGridLegend(color, label)); }
		public List<TimetableGridLegend> getAssignedLegend() { return iAssignedLegend; }
		public void addNotAssignedLegend(String color, String label) { iNotAssignedLegend.add(new TimetableGridLegend(color, label)); }
		public List<TimetableGridLegend> getNotAssignedLegend() { return iNotAssignedLegend; }
		
		public int getWeekOffset() { return iWeekOffset; }
		public void setWeekOffset(int weekOffset) { iWeekOffset = weekOffset; }
	}
	
	public static class TimetableGridModel implements IsSerializable, Serializable {
		private static final long serialVersionUID = 0l;
		private int iResourceType;
		private Long iResourceId;
		private List<TimetableGridCell> iCells = new ArrayList<TimetableGridCell>();
		private List<TimetableGridBackground> iBackgrounds = new ArrayList<TimetableGridBackground>();
		private String iName;
		private int iSize = 0;
	    private Long iType = null;
		private int iFirstDay = -1, iFirstSessionDay = 0;
		private double iUtilization = 0.0;
		private Date iFirstDate = null;
		private String iNameColor = null;
		
		public TimetableGridModel() {}
		
		public TimetableGridModel(int resourceType, long resourceId) {
			this();
			iResourceType = resourceType; iResourceId = resourceId;
		}
		
		public int getResourceType() { return iResourceType; }
		public Long getResourceId() { return iResourceId; }
		
		public void setFirstDay(int firstDay) { iFirstDay = firstDay; }
		public int getFirstDay() { return iFirstDay; }
		
		public void setFirstSessionDay(int firstDay) { iFirstSessionDay = firstDay; }
		public int getFirstSessionDay() { return iFirstSessionDay; }
		
		public void setFirstDate(Date date) { iFirstDate = date; }
		public Date getFirstDate() { return iFirstDate; }

		public void setName(String name) { iName = name; }
		public String getName() { return iName; }

		public boolean hasNameColor() { return iNameColor != null && !iNameColor.isEmpty(); }
		public void setNameColor(String color) { iNameColor = color; }
		public String getNameColor() { return iNameColor; }

		public void setSize(int size) { iSize = size; }
		public int getSize() { return iSize; }
		
		public void setType(Long type) { iType = type; }
	    public Long getType() { return iType; }

		public void setUtilization(double utilization) { iUtilization = utilization; }
		public double getUtilization() { return iUtilization; }

		public void addCell(TimetableGridCell cell) { iCells.add(cell); }
		public List<TimetableGridCell> getCells() { return iCells; }
		
		public void addBackground(TimetableGridBackground background) { iBackgrounds.add(background); }
		public List<TimetableGridBackground> getBackgrounds() { return iBackgrounds; }
		
		public int getNrLines(int day, int minLines) {
			int lines = minLines;
			for (TimetableGridCell cell: getCells())
				if (cell.getDay() == day && cell.getIndex() + cell.getNrLines() > lines)
					lines = cell.getIndex() + cell.getNrLines();
			return lines;
		}
		
		public int getNrDateLines(int day, int date, int minLines) {
			int lines = minLines;
			for (TimetableGridCell cell: getCells())
				if (cell.getDay() == day && cell.getIndex() + cell.getNrLines() > lines && cell.hasDate(date))
					lines = cell.getIndex() + cell.getNrLines();
			return lines;
		}
		
		public boolean hasDate(int day, int date) {
			for (TimetableGridCell cell: getCells())
				if (cell.getDay() == day && cell.hasDate(date))
					return true;
			return false;
		}
		
		public boolean isAvailable(int day, int slot) {
			for (TimetableGridBackground bg: getBackgrounds())
				if (!bg.isAvailable() && bg.getDay() == day && bg.getSlot() <= slot && slot < bg.getSlot() + bg.getLength())
					return false;
			return true;
		}
		
		@Override
		public String toString() {
			return getName();
		}
	}
	
	public static class TimetableGridCell implements IsSerializable, Serializable, Comparable<TimetableGridCell> {
		private static final long serialVersionUID = 0l;
		private Type iType;
		private Long iId;
		private List<String> iNames;
		private List<String> iTitles;
		private String iDate;
		private String iTime;
		private List<String> iInstructors;
		private List<String> iRooms;
		private String iPreference;
		private int iDay, iSlot, iLength;
		private String iWeekCode;
		private Integer iNrLines = null, iIndex = null;
		private String iBackground;
		private boolean iItalics = false;
		private String iGroup;
		private boolean iCommitted = false;
		private Map<Integer, String> iProperties = null;
		private String iDays;
		
		public static enum Type implements IsSerializable {
			Class,
			Event,
			;
		}
		
		public static enum Property implements IsSerializable {
			TimePreference,
			StudentConflicts,
			StudentConflictsHard,
			StudentConflictsCommitted,
			StudentConflictsDistance,
			RoomPreference,
			InstructorPreference,
			InitialAssignment,
			PerturbationPenalty,
			DepartmentBalance,
			DistributionPreference,
			Owner,
			NonConflictingPlacements,
			EventType,
			;
		}
		
		public TimetableGridCell() {}
		public TimetableGridCell(TimetableGridCell cell, int day, String date) {
			iType = cell.iType;
			iId = cell.iId;
			iNames = (cell.iNames == null ? null : new ArrayList<String>(cell.iNames));
			iTitles = (cell.iTitles == null ? null : new ArrayList<String>(cell.iTitles));
			iDate = date;
			iTime = cell.iTime;
			iInstructors = (cell.iInstructors == null ? null : new ArrayList<String>(cell.iInstructors));
			iRooms = (cell.iRooms == null ? null : new ArrayList<String>(cell.iRooms));
			iPreference = cell.iPreference;
			iProperties = cell.iProperties;
			iDay = day;
			iSlot = cell.iSlot;
			iLength = cell.iLength;
			iWeekCode = cell.iWeekCode;
			iBackground = cell.iBackground;
			iItalics = cell.iItalics;
			iGroup = cell.iGroup;
			iDays = cell.iDays;
		}
		
		public void setType(Type type) { iType = type; }
		public boolean hasType() { return iType != null; }
		public Type getType() { return iType; }
		
		public void setId(Long id) { iId = id; }
		public Long getId() { return iId; }
		public boolean hasId() { return iId != null; }
		
		public void clearName() { iNames = null; }
		public void addName(String name) {
			if (iNames == null) iNames = new ArrayList<String>();
			iNames.add(name);
		}
		public int getNrNames() { return iNames == null ? 0 : iNames.size(); }
		public List<String> getNames() { return iNames; }
		public String getName(String delim) {
			if (iNames == null) return "";
			String ret = "";
			for (String name: iNames)
				ret += (ret.isEmpty() ? "" : delim) + name;
			return ret;
		}
		public void addTitle(String title) {
			if (iTitles == null) iTitles = new ArrayList<String>();
			iTitles.add(title);
		}
		public int getNrTitles() { return iTitles == null ? 0 : iTitles.size(); }
		public List<String> getTitles() { return iTitles; }
		public String getTitle(String delim) {
			if (iTitles == null || iTitles.isEmpty()) return getName(delim);
			if (iTitles == null) return "";
			String ret = "";
			for (String name: iTitles)
				ret += (ret.isEmpty() ? "" : delim) + name;
			return ret;
		}
		
		public String getDate() { return iDate; }
		public void setDate(String date) { iDate = date; }
		public boolean hasDate() { return iDate != null && !iDate.isEmpty(); }
		
		public String getTime() { return iTime; }
		public void setTime(String time) { iTime = time; }
		public boolean hasTime() { return iTime != null && !iTime.isEmpty(); }
		
		public String getDays() { return iDays; }
		public void setDays(String days) { iDays = days; }
		public boolean hasDays() { return iDays != null && !iDays.isEmpty(); }
		
		public void addInstructor(String name) {
			if (iInstructors == null) iInstructors = new ArrayList<String>();
			iInstructors.add(name);
		}
		public int getNrInstructors() { return iInstructors == null ? 0 : iInstructors.size(); }
		public List<String> getInstructors() { return iInstructors; }
		public String getInstructor(String delim) {
			if (iInstructors == null) return "";
			String ret = "";
			for (String name: iInstructors)
				ret += (ret.isEmpty() ? "" : delim) + name;
			return ret;
		}
		public void resetInstructors() { iInstructors = null; }

		public void setWeekCode(String weekCode) { iWeekCode = weekCode; }
		public boolean hasWeekCode() { return iWeekCode != null && !iWeekCode.isEmpty() && iWeekCode.indexOf('1') >= 0; }
	    public String getWeekCode() { return iWeekCode; }
	    public boolean getWeekCode(int n) {
	    	return iWeekCode != null && n < iWeekCode.length() && iWeekCode.charAt(n) == '1';
	    }
	    public int getWeekCodeNextBitSet(int n) {
	    	return iWeekCode != null && n < iWeekCode.length() ? iWeekCode.indexOf('1', n) : -1;
	    }
	    public int getFirstDate() {
	    	return iWeekCode == null ? -1 : iWeekCode.indexOf('1');
	    }
	    public boolean hasDate(int n) {
	    	return (iWeekCode == null || iWeekCode.length() <= n ? false : iWeekCode.charAt(n) == '1'); 
	    }
	    
		public void addRoom(String name) {
			if (iRooms == null) iRooms = new ArrayList<String>();
			iRooms.add(name);
		}
		public int getNrRooms() { return iRooms == null ? hasGroup() ? 1 : 0 : iRooms.size(); }
		public List<String> getRooms() {
			if (iRooms == null || iRooms.isEmpty()) {
				List<String> rooms = new ArrayList<String>();
				if (hasGroup()) rooms.add(getGroup());
				return rooms;
			} else {
				List<String> rooms = new ArrayList<String>(iRooms);
				if (hasGroup()) rooms.set(rooms.size() - 1, rooms.get(rooms.size() - 1) + " " + getGroup());
				return rooms;
			}
		}
		public String getRoom(String delim) {
			if (iRooms == null)
				return (hasGroup() ? getGroup() : "");
			String ret = "";
			for (String name: iRooms)
				ret += (ret.isEmpty() ? "" : delim) + name;
			if (hasGroup())
				ret += " " + getGroup();
			return ret;
		}
		
		public String getPreference() { return iPreference; }
		public void setPreference(String preference) { iPreference = preference; }
		public boolean hasPreference() { return iPreference != null && !iPreference.isEmpty(); }

		public boolean hasProperties() { return iProperties != null && !iProperties.isEmpty(); }
		public void setProperty(Property p, String value) {
			if (iProperties == null) iProperties = new HashMap<Integer, String>();
			iProperties.put(p.ordinal(), value);
		}
		public void setProperty(Property p, int value) {
			if (iProperties == null) iProperties = new HashMap<Integer, String>();
			iProperties.put(p.ordinal(), String.valueOf(value));
		}
		public String getProperty(Property p, String defaultValue) {
			if (iProperties == null) return defaultValue;
			String value = iProperties.get(p.ordinal());
			return (value == null ? defaultValue : value);
		}
		public boolean hasProperty(Property p) {
			return iProperties != null && iProperties.containsKey(p.ordinal());
		}

		public String getBackground() { return iBackground; }
		public void setBackground(String background) { iBackground = background; }
		public boolean hasBackground() { return iBackground != null && !iBackground.isEmpty(); }

		public int getDay() { return iDay; }
		public void setDay(int day) { iDay = day; }
		
		public int getSlot() { return iSlot; }
		public void setSlot(int slot) { iSlot = slot; }
		
		public int getLength() { return iLength; }
		public void setLength(int length) { iLength = length; }
		
		public String getGroup() { return iGroup; }
		public void setGroup(String group) { iGroup = group; }
		public boolean hasGroup() { return iGroup != null && !iGroup.isEmpty(); }

		@Override
		public int hashCode() {
			if (hasId()) return getId().hashCode();
			return getName(":").hashCode();
		}
		
		public boolean sameClassOrMeeting(TimetableGridCell c) {
			return getType() == c.getType() && hasId() && getId().equals(c.getId());
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof TimetableGridCell))
				return false;
			TimetableGridCell c = (TimetableGridCell)o;
			if (hasId() || c.hasId())
				return hasId() ? getId().equals(c.getId()) : false;
			return getSlot() == c.getSlot() && getDay() == c.getDay() && getLength() == c.getLength() && getName(":").equals(c.getName(":"));
		}

		@Override
		public int compareTo(TimetableGridCell c) {
			if (getFirstDate() != c.getFirstDate()) return (getFirstDate() < c.getFirstDate() ? -1 : 1);
			if (getDay() != c.getDay()) return (getDay() < c.getDay() ? -1 : 1);
			if (getSlot() != c.getSlot()) return (getSlot() < c.getSlot() ? -1 : 1);
			if (getLength() != c.getLength()) return (getLength() > c.getLength() ? -1 : 1);
			int cmp = getName(":").compareTo(c.getName(":"));
			if (cmp != 0) return cmp;
			if (getType() != c.getType()) return getType().compareTo(c.getType());
			return (hasId() ? getId() : new Long(0)).compareTo(c.hasId() ? c.getId() : new Long(0));
		}
		
		public int getMinLines(boolean showRoom, boolean showInstructor, boolean showTime, boolean showPreference, boolean showDate) {
			int lines = getNrNames() + (showTime && hasTime() ? 1 : 0) + (showDate && hasDate() ? 1 : 0) +
					(showRoom ? getNrRooms() : 0) + (showInstructor ? getNrInstructors() : 0) +
					(showPreference && hasPreference() ? 1 : 0);
			return lines < 2 ? 2 : lines;
		}
		
		public int getNrLines() { return (iNrLines == null ? 0 : iNrLines.intValue()); }
		public void setNrLines(int lines) { iNrLines = lines; }
		public int getIndex() { return (iIndex == null ? 0 : iIndex.intValue()); }
		public void setIndex(int index) { iIndex = index; }
		
		public boolean isItalics() { return iItalics; }
		public void setItalics(boolean italics) { iItalics = italics; }
		
		public boolean isCommitted() { return iCommitted; }
		public void setCommitted(boolean committed) { iCommitted = committed; }
		
		@Override
		public String toString() {
			String[] days = new String[] {"M","T","W","R","F","S","U"};
			return getNames().get(0) + " " + days[getDay()] + (hasTime() ? " " + getTime(): "") + (hasDate() ? " " + getDate() : ""); 
		}
	}
	
	public static class TimetableGridBackground implements IsSerializable, Serializable, Comparable<TimetableGridBackground> {
		private static final long serialVersionUID = 0l;
		private int iDay, iSlot, iLength;
		private String iBackground;
		private boolean iAvailable = true;

		public String getBackground() { return iBackground; }
		public void setBackground(String background) { iBackground = background; }
		public boolean hasBackground() { return iBackground != null && !iBackground.isEmpty(); }

		public int getDay() { return iDay; }
		public void setDay(int day) { iDay = day; }
		
		public int getSlot() { return iSlot; }
		public void setSlot(int slot) { iSlot = slot; }
		
		public int getLength() { return iLength; }
		public void setLength(int length) { iLength = length; }
		
		public boolean isAvailable() { return iAvailable; }
		public void setAvailable(boolean available) { iAvailable = available; }
		
		@Override
		public int compareTo(TimetableGridBackground c) {
			if (getDay() != c.getDay()) return (getDay() < c.getDay() ? -1 : 1);
			if (getSlot() != c.getSlot()) return (getSlot() < c.getSlot() ? -1 : 1);
			if (getLength() != c.getLength()) return (getLength() > c.getLength() ? -1 : 1);
			return (hasBackground() ? getBackground() : "").compareTo(c.hasBackground() ? c.getBackground() : "");
		}
		
		private static String slot2time(int slot) {
			int h = slot / 12;
	        int m = 5 * (slot % 12);
			return h + ":" + (m < 10 ? "0" : "") + m;
		}
		
		@Override
		public String toString() {
			String[] days = new String[] {"M","T","W","R","F","S","U"};
			return days[getDay()] + " " + slot2time(getSlot()) + " - " + slot2time(getSlot() + getLength()) + " " + getBackground(); 
		}
	}
	
	public static class TimetableGridLegend implements IsSerializable, Serializable {
		private static final long serialVersionUID = 0l;
		private String iColor;
		private String iLabel;
		
		public TimetableGridLegend() {}
		public TimetableGridLegend(String color, String label) { iColor = color; iLabel = label; }
		
		public String getColor() { return iColor; }
		public void setColor(String color) { iColor = color; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		@Override
		public String toString() { return iColor + ": " + iLabel; }
	}

}
