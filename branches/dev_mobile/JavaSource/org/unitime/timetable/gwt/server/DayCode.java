/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.util.ArrayList;
import java.util.Collection;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;

/**
 * @author Tomas Muller
 */
public enum DayCode {
	MON (64),
	TUE (32),
	WED (16),
	THU ( 8),
	FRI ( 4),
	SAT ( 2),
	SUN ( 1);
	
	private static StudentSectioningConstants CFG = Localization.create(StudentSectioningConstants.class);
	
	private final int iCode;
	
	DayCode(int code) { iCode = code; }
	
	public int getCode() { return iCode; }
	public int getIndex() { return ordinal(); }
	public String getAbbv() { return CFG.shortDays()[ordinal()]; }
	public String getShort() { return CFG.days()[ordinal()]; }
	public String getName() { return CFG.longDays()[ordinal()]; }
	
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
	    	if (days.contains(dc.ordinal())) dayCodes.add(dc);
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
