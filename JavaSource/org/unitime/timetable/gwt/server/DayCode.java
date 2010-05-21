/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.server;

import java.util.ArrayList;
import java.util.Collection;

public enum DayCode {
	MON (64, 0, "M" , "Mon", "Monday"),
	TUE (32, 1, "T" , "Tue", "Tuesday"),
	WED (16, 2, "W" , "Wed", "Wednesday"),
	THU ( 8, 3, "R" , "Thu", "Thursday" ),
	FRI ( 4, 4, "F" , "Fri", "Friday"),
	SAT ( 2, 5, "S" , "Sat", "Saturday"),
	SUN ( 1, 6, "U" , "Sun", "Sunday");
	
	private final int iCode;
	private final int iIndex;
	private final String iAbbv;
	private final String iShort;
	private final String iName;
	
	DayCode(int code, int index, String abbv, String shortName, String name) { iCode = code; iIndex = index; iAbbv = abbv; iShort = shortName; iName = name;  }
	public int getIndex() { return iIndex; }
	public int getCode() { return iCode; }
	public String getAbbv() { return iAbbv; }
	public String getShort() { return iShort; }
	public String getName() { return iName; }
	
	public static int nrDays(int days) {
		int nrDays = 0;
		for (DayCode dc: DayCode.values())
	    	if ((days & dc.getCode())!=0) nrDays++;
		return nrDays;
	}

	public static ArrayList<DayCode> toDayCodes(int days) {
		ArrayList<DayCode> dayCodes = new ArrayList<DayCode>(DayCode.values().length);
		for (DayCode dc: DayCode.values())
	    	if ((days & dc.getCode())!=0) dayCodes.add(dc);
		return dayCodes;
	}

	public static String toString(int days) {
		StringBuffer daysStr = new StringBuffer();
		for (DayCode dc: DayCode.values())
	    	if ((days & dc.getCode())!=0) daysStr.append(dc.getAbbv());
		return daysStr.toString();
	}
	
	public static ArrayList<DayCode> toDayCodes(String days) {
		ArrayList<DayCode> dayCodes = new ArrayList<DayCode>(DayCode.values().length);
		for (DayCode dc: DayCode.values())
	    	if (days.indexOf(dc.getAbbv()) >= 0) dayCodes.add(dc);
		return dayCodes;
	}

	public static ArrayList<DayCode> toDayCodes(Collection<Integer> days) {
		ArrayList<DayCode> dayCodes = new ArrayList<DayCode>(DayCode.values().length);
		for (DayCode dc: DayCode.values())
	    	if (days.contains(dc.getIndex())) dayCodes.add(dc);
		return dayCodes;
	}

	public static String toString(ArrayList<DayCode> days) {
		StringBuffer daysStr = new StringBuffer();
		for (DayCode dc: days)
			daysStr.append(dc.getAbbv());
		return daysStr.toString();
	}
	
	public static int toInt(ArrayList<DayCode> days) {
		int dayCode = 0;
		for (DayCode dc: days)
			dayCode |= dc.getCode();
		return dayCode;
	}

	public int nrDays(ArrayList<DayCode> days) {
		return days.size();
	}

}
