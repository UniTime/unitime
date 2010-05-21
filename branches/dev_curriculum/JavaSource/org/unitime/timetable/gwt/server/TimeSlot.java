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

public enum TimeSlot {
	SLOT_LENGTH_MINS     (  5, "Number of minutes in one time slot."),
	FIRST_SLOT_TIME_MIN  (  0, "Time of the first slot in minutes"),
	NR_SLOTS_PER_DAY     (288, "Number of slots per day."),
	DAY_SLOTS_FIRST      (  TimeSlot.toSlot(7, 30), "First day-time slot."),
	DAY_SLOTS_LAST       (  TimeSlot.toSlot(17, 30) - 1, "Last day-time slot."),
	NR_DAY_SLOTS         ( DAY_SLOTS_LAST.value() - DAY_SLOTS_FIRST.value() + 1, "Number of day-time slots.");

	private int iValue;
	private String iDescription;
	
	TimeSlot(int value, String description) { iValue = value; iDescription = description; }
	public int value() { return iValue; }
	public String description() { return iDescription; }

	public static int toSlot(int hour, int minute) {
		int min = 60 * hour + minute;
		return (min - FIRST_SLOT_TIME_MIN.value()) / SLOT_LENGTH_MINS.value();
	}
	
	public static int toHour(int slot) {
		int min = slot * SLOT_LENGTH_MINS.value() + FIRST_SLOT_TIME_MIN.value();
		return min / 60;
	}
	
	public static int toMinute(int slot) {
		int min = slot * SLOT_LENGTH_MINS.value() + FIRST_SLOT_TIME_MIN.value();
		return min % 60;
	}
	
	public static String toString(int hour, int minute) {
        return (hour > 12 ? hour - 12 : hour) + ":" + (minute < 10 ? "0" : "") + minute + (hour >= 12 ? "p" : "a");
	}
	
	public static String toString(int slot) {
		return toString(toHour(slot), toMinute(slot));
	}
	
	public static int toEndSlot(int startSlot, int lengthInSlots, int breakTime) {
		return startSlot + lengthInSlots * SLOT_LENGTH_MINS.value() - breakTime;
	}
}
