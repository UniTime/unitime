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
