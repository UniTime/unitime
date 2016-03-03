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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.util.Constants;


/**
 * @author Tomas Muller
 */
public class ClassTimeInfo implements Serializable, Comparable<ClassTimeInfo> {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	private static final long serialVersionUID = -342155197631035341L;
    private Long iClassId;

	private int iStartSlot;
    
    private int iPreference;
    
    private Long iTimePatternId = null;
    private transient TimePattern iTimePattern = null;
    private int iHashCode;

    private int iDayCode;
    private int iLength;
    private int iNrMeetings;
    private int iBreakTime;
    private int iMinsPerMtg;
    
    private ClassDateInfo iDate = null;
    
    private List<Date> iDates = null;

    public ClassTimeInfo(Long classId, int dayCode, int startTime, int length, int minsPerMtg, int pref, TimePattern timePattern, ClassDateInfo date, int breakTime, List<Date> dates) {
        iClassId = classId;
        iPreference = pref;
        iStartSlot = startTime;
        iDayCode = dayCode;
        iMinsPerMtg = minsPerMtg;
        iLength = length;
        iBreakTime = breakTime;
        iNrMeetings = 0;
        for (int i=0;i<Constants.DAY_CODES.length;i++) {
            if ((iDayCode & Constants.DAY_CODES[i])==0) continue;
            iNrMeetings++;
        }
        iHashCode = combine(combine(iDayCode, iStartSlot),combine(iLength, date.getId().hashCode()));
        iDate = date;
        iTimePatternId = timePattern.getUniqueId();
        iTimePattern = timePattern;
        iDates = dates;
    }
    
    public ClassTimeInfo(Assignment assignment, int preference, int datePreference) {
		this(assignment.getClassId(),
				assignment.getDays().intValue(),
				assignment.getStartSlot().intValue(),
				assignment.getSlotPerMtg(),
				assignment.getMinutesPerMeeting(),
				preference,
				assignment.getTimePattern(),
				new ClassDateInfo(assignment, datePreference),
				assignment.getBreakTime(),
				assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getDurationModel().getDates(
						assignment.getClazz().getSchedulingSubpart().getMinutesPerWk(), assignment.getDatePattern(), assignment.getDays(), assignment.getMinutesPerMeeting()));
    }
    
    public ClassTimeInfo(ClassTimeInfo time, ClassDateInfo date, List<Date> dates) {
    	iClassId = time.iClassId;
    	iPreference = time.getPreference();
    	iStartSlot = time.getStartSlot();
    	iDayCode = time.getDayCode();
    	iMinsPerMtg = time.getMinutesPerMeeting();
    	iLength = time.getLength();
    	iBreakTime = time.getBreakTime();
    	iNrMeetings = time.getNrMeetings();
    	iDate = date;
    	iTimePatternId = time.getTimePatternId();
    	if (time.iTimePattern != null)
    		iTimePattern = time.iTimePattern;
    	iHashCode = combine(combine(iDayCode, iStartSlot),combine(iLength, date.getId().hashCode()));
    	iDates = dates;
    }
    
    public ClassTimeInfo(Assignment assignment) {
    	this(assignment, 0, 0);
    }
    
    public int getNrMeetings() {
        return iNrMeetings;
    }
    
    public int getBreakTime() {
        return iBreakTime;
    }
    
    public int getDayCode() { return iDayCode; }
    
    public int getMinutesPerMeeting() { return iMinsPerMtg; }

    public String getDayHeader() { 
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<Constants.DAY_CODES.length;i++)
            if ((iDayCode & Constants.DAY_CODES[i])!=0)
                sb.append(CONSTANTS.shortDays()[i]);
        return sb.toString(); 
    }

    public String getStartTimeHeader() { 
    	int min = iStartSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
    	return Constants.toTime(min);
    }

    public String getEndTimeHeader() { 
    	int min = (iStartSlot + iLength) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - getBreakTime();
    	return Constants.toTime(min);
    }

    public String getEndTimeHeaderNoAdj() { 
    	int min = (iStartSlot + iLength) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
    	return Constants.toTime(min);
    }

    public int getStartSlot() { return iStartSlot; }
    

    public boolean shareDays(ClassTimeInfo anotherLocation) {
        return ((iDayCode & anotherLocation.iDayCode)!=0);
    }

    public boolean shareHours(ClassTimeInfo anotherLocation) {
    	return (iStartSlot+iLength > anotherLocation.iStartSlot) && (anotherLocation.iStartSlot+anotherLocation.iLength > iStartSlot);
    }

    public boolean shareWeeks(ClassTimeInfo anotherLocation) {
        return getDate().getPattern().intersects(anotherLocation.getDate().getPattern());
    }

    public boolean overlaps(ClassTimeInfo anotherLocation) {
        return shareDays(anotherLocation) && shareHours(anotherLocation) && shareWeeks(anotherLocation);
    }    
    
    public String getName() { return getDayHeader()+" "+getStartTimeHeader(); }
    public String getLongName() { return getDayHeader()+" "+getStartTimeHeader()+" - "+getEndTimeHeader()+" "+getDatePatternName(); }
    public String getLongNameNoAdj() { return getDayHeader()+" "+getStartTimeHeader()+" - "+getEndTimeHeaderNoAdj()+" "+getDatePatternName(); }
    
    public String getNameHtml() {
        return
            "<span onmouseover=\"showGwtTimeHint(this, '" + iClassId + "," + iDayCode + "," + iStartSlot + "');\" onmouseout=\"hideGwtTimeHint();\"" +
            " style='color:" + PreferenceLevel.int2color(getPreference()) + ";'>" +
            getName()+
            "</span>";
    }

    public String getLongNameHtml() {
        return
            "<span onmouseover=\"showGwtTimeHint(this, '" + iClassId + "," + iDayCode + "," + iStartSlot + "');\" onmouseout=\"hideGwtTimeHint();\"" +
            " style='color:" + PreferenceLevel.int2color(getPreference()) + ";'>" +
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
    public ClassDateInfo getDate() { return iDate; }
    public Long getDatePatternId() { return getDate().getId(); }
    public DatePattern getDatePattern() { return getDate().getDatePattern(); }
    public String getDatePatternName() { return getDate().getName(); }

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
    	int cmp = getDate().compareTo(time.getDate());
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
    
    public List<Date> getDates() {
    	return iDates; 
    }
    
    public TimeBlock overlaps(Collection<TimeBlock> times) {
    	if (times==null || times.isEmpty()) return null;
        int breakTimeStart = ApplicationProperty.RoomAvailabilityClassBreakTimeStart.intValue(); 
        int breakTimeStop = ApplicationProperty.RoomAvailabilityClassBreakTimeStop.intValue();
        for (Date date: getDates()) {
        	DummyTimeBlock dummy = new DummyTimeBlock(date, breakTimeStart, breakTimeStop);
        	for (TimeBlock time: times)
        		if (dummy.overlaps(time)) return time;
        }
        return null;
    }
    
    public List<TimeBlock> allOverlaps(Collection<TimeBlock> times) {
    	if (times==null || times.isEmpty()) return null;
        int breakTimeStart = ApplicationProperty.RoomAvailabilityClassBreakTimeStart.intValue(); 
        int breakTimeStop = ApplicationProperty.RoomAvailabilityClassBreakTimeStop.intValue();
        List<TimeBlock> blocks = new ArrayList<TimeBlock>();
        for (Date date: getDates()) {
        	DummyTimeBlock dummy = new DummyTimeBlock(date, breakTimeStart, breakTimeStop);
        	for (TimeBlock time: times)
        		if (dummy.overlaps(time)) blocks.add(time);
        }
        return blocks;
    }

    public class DummyTimeBlock implements TimeBlock {
		private static final long serialVersionUID = -3806087343289917036L;
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
