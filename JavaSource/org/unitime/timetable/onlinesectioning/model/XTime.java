/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;

public class XTime implements Serializable {
	private static final long serialVersionUID = 1L;
	private int iSlot;
	private int iLength;
	private int iBreakTime = 0;
	private int iDays;
	private BitSet iWeeks = null;
	private Long iDatePatternId = null;
	private String iDatePatternName = null;
	
	public XTime() {
	}
	
	public XTime(Assignment assignment, XExactTimeConversion conversion) {
		iDays = assignment.getDays();
		iSlot = assignment.getStartSlot();
		if (assignment.getTimePattern().getType() == TimePattern.sTypeExactTime) {
			iLength = conversion.getLength(assignment.getDays(), assignment.getClazz().getSchedulingSubpart().getMinutesPerWk());
			iBreakTime = conversion.getBreakTime(assignment.getDays(), assignment.getClazz().getSchedulingSubpart().getMinutesPerWk());
		} else {
			iLength = assignment.getTimePattern().getSlotsPerMtg();
			iBreakTime = assignment.getTimePattern().getBreakTime();
		}
		iDatePatternId = assignment.getDatePattern().getUniqueId();
		iDatePatternName = datePatternName(assignment);
		iWeeks = assignment.getDatePattern().getPatternBitSet();
	}
	
	public XTime(FreeTime free, BitSet freeTimePattern) {
		iSlot = free.getStartSlot();
		iLength = free.getLength();
		iDays = free.getDayCode();
		iWeeks = freeTimePattern;
	}
	
	public XTime(TimeLocation time) {
		iDays = time.getDayCode();
		iSlot = time.getStartSlot();
		iLength = time.getLength();
		iBreakTime = time.getBreakTime();
		iDatePatternId = time.getDatePatternId();
		iDatePatternName = time.getDatePatternName();
		iWeeks = time.getWeekCode();
	}
	
	public int getSlot() { return iSlot; }
	public int getLength() { return iLength; }
	public int getDays() { return iDays; }
	public int getBreakTime() { return iBreakTime; }
	public BitSet getWeeks() { return iWeeks; }
	public Long getDatePatternId() { return iDatePatternId; }
	public String getDatePatternName() { return iDatePatternName; }
	
	public String toDaysString() {
		String ret = "";
		for (int i = 0; i < Constants.DAY_CODES.length; i++)
			if ((getDays() & Constants.DAY_CODES[i]) != 0) ret += Constants.DAY_NAMES_SHORT[i];
		return ret;
	}
	
	public String toStartString() {
        int min = getSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        int h = min / 60;
        int m = min % 60;
        return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h >= 12 ? "p" : "a");
	}
	
	public String toStopString() {
		int min = (getSlot() + getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - getBreakTime();
        int h = min / 60;
        int m = min % 60;
        return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h >= 12 ? "p" : "a");
	}
	
    public boolean shareHours(XTime other) {
        return (getSlot() + getLength() > other.getSlot()) && (other.getSlot() + other.getLength() > getSlot());
    }
	
    public boolean shareDays(XTime other) {
        return (getDays() & other.getDays()) != 0;
    }
    
    public boolean shareWeeks(XTime other) {
    	return getWeeks() == null ? true : other.getWeeks() == null ? true : getWeeks().intersects(other.getWeeks());
    }
    
    public boolean hasIntersection(XTime other) {
        return shareDays(other) && shareHours(other) && shareWeeks(other);
    }
    
    /** Used slots */
    public Enumeration<Integer> getSlots() {
        return new SlotsEnum();
    }

    /** Used start slots (for each meeting) */
    public Enumeration<Integer> getStartSlots() {
        return new StartSlotsEnum();
    }

	
	@Override
	public String toString() {
		return (getDatePatternName() == null ? "" : getDatePatternName() + " ") + toDaysString() + " " + toStartString() + " - " + toStopString();
	}
	
    public static String datePatternName(Assignment assignment) {
    	BitSet weekCode = assignment.getDatePattern().getPatternBitSet();
    	if (weekCode.isEmpty()) return assignment.getDatePattern().getName();
    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	Session session = assignment.getDatePattern().getSession();
    	Date start = DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear()); 
    	cal.setTime(start);
    	int idx = weekCode.nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	Date first = null;
    	int dayCode = assignment.getDays();
    	while (idx < weekCode.size() && first == null) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((dayCode & DayCode.MON.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((dayCode & DayCode.TUE.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((dayCode & DayCode.WED.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((dayCode & DayCode.THU.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((dayCode & DayCode.FRI.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((dayCode & DayCode.SAT.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((dayCode & DayCode.SUN.getCode()) != 0) first = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	if (first == null) return assignment.getDatePattern().getName();
    	cal.setTime(start);
    	idx = weekCode.length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	Date last = null;
    	while (idx >= 0 && last == null) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((dayCode & DayCode.MON.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((dayCode & DayCode.TUE.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((dayCode & DayCode.WED.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((dayCode & DayCode.THU.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((dayCode & DayCode.FRI.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((dayCode & DayCode.SAT.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((dayCode & DayCode.SUN.getCode()) != 0) last = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, -1); idx--;
    	}
    	if (last == null) return assignment.getDatePattern().getName();
        Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
    	return dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last));
    }
    
    private class StartSlotsEnum implements Enumeration<Integer> {
        int day = -1;
        boolean hasNext = false;

        private StartSlotsEnum() {
            hasNext = nextDay();
        }

        boolean nextDay() {
            do {
                day++;
                if (day >= Constants.DAY_CODES.length)
                    return false;
            } while ((Constants.DAY_CODES[day] & iDays) == 0);
            return true;
        }

        @Override
        public boolean hasMoreElements() {
            return hasNext;
        }

        @Override
        public Integer nextElement() {
            int slot = (day * Constants.SLOTS_PER_DAY) + iSlot;
            hasNext = nextDay();
            return slot;
        }
    }
    
    private class SlotsEnum extends StartSlotsEnum {
        int pos = 0;

        private SlotsEnum() {
            super();
        }

        private boolean nextSlot() {
            if (pos + 1 < iLength) {
                pos++;
                return true;
            }
            if (nextDay()) {
                pos = 0;
                return true;
            }
            return false;
        }

        @Override
        public Integer nextElement() {
            int slot = (day * Constants.SLOTS_PER_DAY) + iSlot + pos;
            hasNext = nextSlot();
            return slot;
        }
    }
    
    public TimeLocation toTimeLocation() {
    	return new TimeLocation(getDays(), getSlot(), getLength(), 0, 0.0, getDatePatternId(), getDatePatternName(), getWeeks(), getBreakTime());
    }
}
