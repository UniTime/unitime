/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2009, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class ClassTimeInfo implements Serializable, Comparable<ClassTimeInfo> {
	private static SimpleDateFormat sDateFormatShort = new SimpleDateFormat("MM/dd", Locale.US);
    private int iStartSlot;
    
    private int iPreference;
    
    private Long iTimePatternId = null;
    private transient TimePattern iTimePattern = null;
    private int iHashCode;

    private int iDayCode;
    private int iLength;
    private int iNrMeetings;
    private int iBreakTime;
    
    private BitSet iWeekCode;
    private Long iDatePatternId = null;
    private transient DatePattern iDatePattern = null;

    public ClassTimeInfo(int dayCode, int startTime, int length, int pref, TimePattern timePattern, DatePattern datePattern, int breakTime) {
        iPreference = pref;
        iStartSlot = startTime;
        iDayCode = dayCode;
        iLength = length;
        iBreakTime = breakTime;
        iNrMeetings = 0;
        for (int i=0;i<Constants.DAY_CODES.length;i++) {
            if ((iDayCode & Constants.DAY_CODES[i])==0) continue;
            iNrMeetings++;
        }
        iHashCode = combine(combine(iDayCode, iStartSlot),iLength);
        iDatePatternId = datePattern.getUniqueId();
        iDatePattern = datePattern;
        iWeekCode = datePattern.getPatternBitSet();
        iTimePatternId = timePattern.getUniqueId();
        iTimePattern = timePattern;
    }
    
    public ClassTimeInfo(Assignment assignment, int preference) {
		this(assignment.getDays().intValue(),
				assignment.getStartSlot().intValue(),
				assignment.getSlotPerMtg(),
				preference,
				assignment.getTimePattern(),
				assignment.getDatePattern(),
				assignment.getBreakTime());
    }
    
    public int getNrMeetings() {
        return iNrMeetings;
    }
    
    public int getBreakTime() {
        return iBreakTime;
    }
    
    public int getDayCode() { return iDayCode; }

    public String getDayHeader() { 
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<Constants.DAY_CODES.length;i++)
            if ((iDayCode & Constants.DAY_CODES[i])!=0)
                sb.append(Constants.DAY_NAMES_SHORT[i]);
        return sb.toString(); 
    }

    public String getStartTimeHeader() { 
    	int min = iStartSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        int h = min/60;
        int m = min%60;
        return (h>12?h-12:h)+":"+(m<10?"0":"")+m+(h>=12?"p":"a");
    }

    public String getEndTimeHeader() { 
    	int min = (iStartSlot + iLength) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - getBreakTime();
        int m = min % 60;
        int h = min / 60;
        return (h>12?h-12:h)+":"+(m<10?"0":"")+m+(h>=12?"p":"a");
    }

    public String getEndTimeHeaderNoAdj() { 
    	int min = (iStartSlot + iLength) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        int m = min % 60;
        int h = min / 60;
        return (h>12?h-12:h)+":"+(m<10?"0":"")+m+(h>=12?"p":"a");
    }

    public int getStartSlot() { return iStartSlot; }
    

    public boolean shareDays(ClassTimeInfo anotherLocation) {
        return ((iDayCode & anotherLocation.iDayCode)!=0);
    }

    public boolean shareHours(ClassTimeInfo anotherLocation) {
    	return (iStartSlot+iLength > anotherLocation.iStartSlot) && (anotherLocation.iStartSlot+anotherLocation.iLength > iStartSlot);
    }

    public boolean shareWeeks(ClassTimeInfo anotherLocation) {
        return iWeekCode.intersects(anotherLocation.iWeekCode);
    }

    public boolean overlaps(ClassTimeInfo anotherLocation) {
        return shareDays(anotherLocation) && shareHours(anotherLocation) && shareWeeks(anotherLocation);
    }    
    
    public String getName() { return getDayHeader()+" "+getStartTimeHeader(); }
    public String getLongName() { return getDayHeader()+" "+getStartTimeHeader()+" - "+getEndTimeHeader()+" "+getDatePatternName(); }
    public String getLongNameNoAdj() { return getDayHeader()+" "+getStartTimeHeader()+" - "+getEndTimeHeaderNoAdj()+" "+getDatePatternName(); }
    
    public String getNameHtml() {
        return
            "<span title='"+PreferenceLevel.int2string(getPreference())+" "+getLongName()+"' style='color:"+PreferenceLevel.int2color(getPreference())+";'>"+
            getName()+
            "</span>";
    }

    public String getLongNameHtml() {
        return
            "<span title='"+PreferenceLevel.int2string(getPreference())+" "+getLongName()+"' style='color:"+PreferenceLevel.int2color(getPreference())+";'>"+
            getDayHeader()+" "+getStartTimeHeader()+" - "+getEndTimeHeader()+
            "</span>";
    }

    public int getPreference() { return iPreference; }

    public int getLength() { return iLength; }

    public int getNrSlotsPerMeeting() { return iLength; }

    public Long getTimePatternId() { return iTimePatternId; }
    public TimePattern getTimePattern() {
    	if (iTimePattern==null)
    		iTimePattern = TimePatternDAO.getInstance().get(iTimePatternId);
        return iTimePattern;
    }
    public TimePattern getTimePattern(org.hibernate.Session hibSession) {
    	return TimePatternDAO.getInstance().get(iTimePatternId, hibSession);
    }
    public Long getDatePatternId() { return iDatePatternId; }
    public DatePattern getDatePattern() {
    	if (iDatePattern==null)
    		iDatePattern = DatePatternDAO.getInstance().get(iDatePatternId);
    	return iDatePattern;
    }
    public BitSet getWeekCode() { return iWeekCode; }
    public String getDatePatternName() { return getDatePattern().getName(); }

    public String toString() { return getName(); }
    public int hashCode() {
        return iHashCode;
    }
    
    public boolean equals(Object o) {
    	if (o==null || !(o instanceof ClassTimeInfo)) return false;
    	ClassTimeInfo t = (ClassTimeInfo)o;
    	if (getStartSlot()!=t.getStartSlot()) return false;
    	if (getLength()!=t.getLength()) return false;
    	if (getDayCode()!=t.getDayCode()) return false;
    	return ToolBox.equals(getTimePatternId(),t.getTimePatternId()) && ToolBox.equals(getDatePatternId(),t.getDatePatternId());
    }
    
    public int compareTo(ClassTimeInfo time) {
    	int cmp = getDatePattern().compareTo(time.getDatePattern());
    	if (cmp!=0) return cmp;
    	cmp = getTimePattern().compareTo(time.getTimePattern());
    	if (cmp!=0) return cmp;
    	cmp = getDayCode() - time.getDayCode();
    	if (cmp!=0) return cmp;
    	cmp = getStartSlot() - time.getStartSlot();
    	if (cmp!=0) return cmp;
    	return hashCode() - time.hashCode();
    }

    private static int combine(int a, int b) {
        int ret = 0;
        for (int i=0;i<15;i++) ret = ret | ((a & (1<<i))<<i) | ((b & (1<<i))<<(i+1));
        return ret;
    }
    
    public String getId() {
    	return getDatePatternId()+":"+getTimePatternId()+":"+getDayCode()+":"+getStartSlot();
    }
    
    public Vector<Date> getDates() {
    	Vector<Date> dates = new Vector();
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(getDatePattern().getStartDate()); cal.setLenient(true);
        for (int idx=0;idx<getDatePattern().getPattern().length();idx++) {
        	if (getDatePattern().getPattern().charAt(idx)=='1') {
        		boolean offered = false;
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                	case Calendar.MONDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_MON]) != 0); break;
                	case Calendar.TUESDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_TUE]) != 0); break;
                	case Calendar.WEDNESDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_WED]) != 0); break;
                	case Calendar.THURSDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_THU]) != 0); break;
                	case Calendar.FRIDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_FRI]) != 0); break;
                	case Calendar.SATURDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_SAT]) != 0); break;
                	case Calendar.SUNDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_SUN]) != 0); break;
                }
                if (offered) {
                	dates.add(cal.getTime());
                }
            }
        	cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return dates; 
    }
    
    public TimeBlock overlaps(Collection<TimeBlock> times) {
    	if (times==null || times.isEmpty()) return null;
        int breakTimeStart = Integer.parseInt(ApplicationProperties.getProperty("tmtbl.room.availability.class.breakTime.start", "0"));
        int breakTimeStop = Integer.parseInt(ApplicationProperties.getProperty("tmtbl.room.availability.class.breakTime.stop", "0"));
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(getDatePattern().getStartDate()); cal.setLenient(true);
        for (int idx=0;idx<getDatePattern().getPattern().length();idx++) {
        	if (getDatePattern().getPattern().charAt(idx)=='1') {
        		boolean offered = false;
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                	case Calendar.MONDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_MON]) != 0); break;
                	case Calendar.TUESDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_TUE]) != 0); break;
                	case Calendar.WEDNESDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_WED]) != 0); break;
                	case Calendar.THURSDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_THU]) != 0); break;
                	case Calendar.FRIDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_FRI]) != 0); break;
                	case Calendar.SATURDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_SAT]) != 0); break;
                	case Calendar.SUNDAY : offered = ((getDayCode() & Constants.DAY_CODES[Constants.DAY_SUN]) != 0); break;
                }
                if (offered) {
                	DummyTimeBlock dummy = new DummyTimeBlock(cal.getTime(), breakTimeStart, breakTimeStop);
                	for (TimeBlock time: times)
                		if (dummy.overlaps(time)) {
                			System.out.println(dummy+" overlaps with "+time);
                			return time;
                		}
                }
            }
        	cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return null;
    }

    public class DummyTimeBlock implements TimeBlock {
    	private Date iD1, iD2;
    	private DummyTimeBlock(Date d, int breakTimeStart, int breakTimeStop) {
    		Calendar c = Calendar.getInstance(Locale.US);
    		c.setTime(d);
            int min = getStartSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - breakTimeStart;
            c.set(Calendar.HOUR, min/60);
            c.set(Calendar.MINUTE, min%60);
            iD1 = c.getTime();
            min = (getStartSlot() + getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + breakTimeStop;
            c.setTime(d);
            c.set(Calendar.HOUR, min/60);
            c.set(Calendar.MINUTE, min%60);
            iD2 = c.getTime();
    	}
    	public String getEventName() { return "Dummy event"; }
    	public String getEventType() { return RoomAvailabilityInterface.sClassType; }
    	public Date getStartTime() { return iD1; }
    	public Date getEndTime() { return iD2; }
    	public boolean overlaps(TimeBlock block) {
    		return block.getStartTime().compareTo(iD2)<0 && iD1.compareTo(block.getEndTime()) < 0;
    	}
    	public String toString() {
    		return iD1+" - "+iD2;
    	}
    }
}
