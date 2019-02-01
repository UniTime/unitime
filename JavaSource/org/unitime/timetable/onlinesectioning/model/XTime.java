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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

import org.cpsolver.coursett.model.TimeLocation;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.duration.DurationModel;

/**
 * @author Tomas Muller
 */
@SerializeWith(XTime.XTimeSerializer.class)
public class XTime implements Serializable, Externalizable {
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
	
	public XTime(ObjectInput input) throws IOException, ClassNotFoundException {
		readExternal(input);
	}
	
	public XTime(Assignment assignment, XExactTimeConversion conversion, String datePatternFormat) {
		iDays = assignment.getDays();
		iSlot = assignment.getStartSlot();
		if (assignment.getTimePattern().getType() == TimePattern.sTypeExactTime) {
			DurationModel dm = assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
			int minPerMtg = dm.getExactTimeMinutesPerMeeting(assignment.getClazz().getSchedulingSubpart().getMinutesPerWk(), assignment.getDatePattern(), assignment.getDays());
			iLength = conversion.getLength(minPerMtg);
			iBreakTime = conversion.getBreakTime(minPerMtg);
		} else {
			iLength = assignment.getTimePattern().getSlotsPerMtg();
			iBreakTime = assignment.getTimePattern().getBreakTime();
		}
		iDatePatternId = assignment.getDatePattern().getUniqueId();
		iDatePatternName = datePatternName(assignment, datePatternFormat);
		iWeeks = assignment.getDatePattern().getPatternBitSet();
	}
	
	public XTime(DatePattern pattern, String datePatternFormat) {
		iDays = 0;
		iSlot = 0;
		iLength = 0;
		iBreakTime = 0;
		iDatePatternId = pattern.getUniqueId();
    	if ("never".equals(datePatternFormat)) iDatePatternName = pattern.getName();
    	else if ("extended".equals(datePatternFormat) && pattern.getType() != DatePattern.sTypeExtended) iDatePatternName = pattern.getName();
    	else if ("alternate".equals(datePatternFormat) && pattern.getType() == DatePattern.sTypeAlternate) iDatePatternName = pattern.getName();
    	else {
    		Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
    		Date first = pattern.getStartDate();
    		Date last = pattern.getEndDate();
    		iDatePatternName = dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last));
    	}
		iWeeks = pattern.getPatternBitSet();
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
        return other != null && shareDays(other) && shareHours(other) && shareWeeks(other);
    }
    
    public int nrSharedDays(XTime anotherLocation) {
        int ret = 0;
        for (int i = 0; i < Constants.NR_DAYS; i++) {
            if ((getDays() & Constants.DAY_CODES[i]) == 0)
                continue;
            if ((anotherLocation.getDays() & Constants.DAY_CODES[i]) == 0)
                continue;
            ret++;
        }
        return ret;
    }

    public int nrSharedHours(XTime anotherLocation) {
        int end = Math.min(getSlot() + getLength(), anotherLocation.getSlot() + anotherLocation.getLength());
        int start = Math.max(getSlot(), anotherLocation.getSlot());
        return (end < start ? 0 : end - start);
    }
    
    public int share(XTime other) {
        if (!hasIntersection(other)) return 0;
        return nrSharedDays(other) * nrSharedHours(other) * Constants.SLOT_LENGTH_MIN;
    }
    
    /** Used slots */
    public Enumeration<Integer> getSlots() {
        return new SlotsEnum();
    }

    /** Used start slots (for each meeting) */
    public Enumeration<Integer> getStartSlots() {
        return new StartSlotsEnum();
    }

    public boolean equals(Object o) {
    	if (o == null) return false;
    	if (o instanceof XTime) {
    		XTime x = (XTime)o;
    		return getDays() == x.getDays() && getSlot() == x.getSlot() && getLength() == x.getLength() && getWeeks().equals(x.getWeeks());
    	}
    	if (o instanceof TimeLocation) {
    		TimeLocation t = (TimeLocation)o;
    		return getDays() == t.getDayCode() && getSlot() == t.getStartSlot() && getLength() == t.getLength() && getWeeks().equals(t.getWeekCode());
    	}
    	return false;
    }
	
	@Override
	public String toString() {
		return (getDatePatternName() == null ? "" : getDatePatternName() + " ") + toDaysString() + " " + toStartString() + " - " + toStopString();
	}
	
    public static String datePatternName(Assignment assignment, String format) {
    	if ("never".equals(format)) return assignment.getDatePattern().getName();
    	if ("extended".equals(format) && assignment.getDatePattern().getType() != DatePattern.sTypeExtended) return assignment.getDatePattern().getName();
    	if ("alternate".equals(format) && assignment.getDatePattern().getType() == DatePattern.sTypeAlternate) return assignment.getDatePattern().getName();
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
    
    public static class XTimeSerializer implements Externalizer<XTime> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XTime object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XTime readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XTime(input);
		}
    	
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iSlot = in.readInt();
		iLength = in.readInt();
		iBreakTime = in.readInt();
		iDays = in.readInt();
		iWeeks = (BitSet)in.readObject();
		iDatePatternId = in.readLong();
		if (iDatePatternId < 0) iDatePatternId = null;
		iDatePatternName = (String)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(iSlot);
		out.writeInt(iLength);
		out.writeInt(iBreakTime);
		out.writeInt(iDays);
		out.writeObject(iWeeks);
		out.writeLong(iDatePatternId == null ? -1 : iDatePatternId);
		out.writeObject(iDatePatternName);
	}
}
