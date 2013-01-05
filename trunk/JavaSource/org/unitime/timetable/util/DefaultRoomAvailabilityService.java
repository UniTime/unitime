/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.Query;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.EventDateMapping;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;

public class DefaultRoomAvailabilityService implements RoomAvailabilityInterface {
    private Vector<CacheElement> iCache = new Vector<CacheElement>();
    
    public String getTimeStamp(Date startTime, Date endTime, String excludeType) {
        TimeFrame time = new TimeFrame(startTime, endTime);
        CacheElement cache = get(time, excludeType);
        return (cache==null?null:cache.getTimestamp());
    }
    
    public CacheElement get(TimeFrame time, String excludeType) {
        synchronized (iCache) {
            for (CacheElement cache : iCache) if (cache.cover(time) && cache.exclude(excludeType)) return cache;
        }
        return null;
    }
    public Collection<TimeBlock> getRoomAvailability(Location location, Date startTime, Date endTime, String excludeType) {
        if (location.getPermanentId()==null) return null;
        EventDateMapping.Class2EventDateMap class2eventDateMap = (sClassType.equals(excludeType) ? EventDateMapping.getMapping(location.getSession().getUniqueId()) : null);
        TimeFrame time = new TimeFrame(startTime, endTime);
        synchronized(iCache) {
            CacheElement cache = get(time, excludeType);
            if (cache!=null) return cache.get(location.getPermanentId(), excludeType);
            Calendar start = Calendar.getInstance(Locale.US); start.setTime(startTime);
            int startMin = 60*start.get(Calendar.HOUR_OF_DAY) + start.get(Calendar.MINUTE);
            start.add(Calendar.MINUTE, -startMin);
            Calendar end = Calendar.getInstance(Locale.US); end.setTime(endTime);
            int endMin = 60*end.get(Calendar.HOUR_OF_DAY) + start.get(Calendar.MINUTE);
            end.add(Calendar.MINUTE, -endMin);
            int startSlot = (startMin - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
            int endSlot = (endMin - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
            TreeSet<TimeBlock> ret = new TreeSet<TimeBlock>();
            String exclude = null;
            if (excludeType!=null) {
                if (sFinalExamType.equals(excludeType))
                    exclude = "FinalExamEvent";
                else if (sMidtermExamType.equals(excludeType))
                    exclude = "MidtermExamEvent";
                else if (sClassType.equals(excludeType))
                    exclude = "ClassEvent";
            }
            Query q = new _RootDAO().getSession().createQuery(
                    "select m from Meeting m where m.locationPermanentId=:locPermId and "+
                    "m.approvalStatus = 1 and "+
                    "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                    "m.startPeriod<:endSlot and m.stopPeriod>:startSlot"+
                    (exclude!=null?" and m.event.class!="+exclude:""))
                    .setLong("locPermId", location.getPermanentId())
                    .setDate("startDate", start.getTime())
                    .setDate("endDate", end.getTime())
                    .setInteger("startSlot", startSlot)
                    .setInteger("endSlot", endSlot)
                    .setCacheable(true);
            for (Iterator i=q.list().iterator();i.hasNext();) {
                Meeting m = (Meeting)i.next();
                MeetingTimeBlock block = new MeetingTimeBlock(m, class2eventDateMap);
                if (block.getStartTime() != null)
                	ret.add(block);
            }
            return ret;
        }
    }
    public void activate(Session session, Date startTime, Date endTime, String excludeType, boolean waitForSync) {
        TimeFrame time = new TimeFrame(startTime, endTime);
        EventDateMapping.Class2EventDateMap class2eventDateMap = (sClassType.equals(excludeType) ? EventDateMapping.getMapping(session.getUniqueId()) : null);
        synchronized(iCache) {
            CacheElement cache = get(time, excludeType);
            if (cache==null) {
                cache = new CacheElement(time, excludeType);
                iCache.insertElementAt(cache, 0);
            }
            cache.update(class2eventDateMap);
        }
    }
    
    public void startService() {}
    public void stopService() {}
    
    public static class TimeFrame {
        private Date iStart, iEnd;
        private int iStartSlot, iEndSlot;
        public TimeFrame(Date startTime, Date endTime) {
            Calendar start = Calendar.getInstance(Locale.US); start.setTime(startTime);
            int startMin = 60*start.get(Calendar.HOUR_OF_DAY) + start.get(Calendar.MINUTE);
            start.add(Calendar.MINUTE, -startMin);
            Calendar end = Calendar.getInstance(Locale.US); end.setTime(endTime);
            int endMin = 60*end.get(Calendar.HOUR_OF_DAY) + start.get(Calendar.MINUTE);
            end.add(Calendar.MINUTE, -endMin);
            iStartSlot = (startMin - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
            iEndSlot = (endMin - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
            iStart = start.getTime(); 
            iEnd = end.getTime(); 
        }
        public Date getStartDate() { return iStart; }
        public Date getEndDate() { return iEnd; }
        public int getStartSlot() { return iStartSlot; }
        public int getEndSlot() { return iEndSlot; }
        public int hashCode() {
            return iStart.hashCode() ^ iEnd.hashCode();
        }
        public boolean equals(Object o) {
            if (o==null || !(o instanceof TimeFrame)) return false;
            TimeFrame t = (TimeFrame)o;
            return getStartDate().equals(t.getStartDate()) && getEndDate().equals(t.getEndDate()) &&
                getStartSlot()==t.getStartSlot() && getEndSlot()==t.getEndSlot();
        }
        public String toString() {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy");
            int start = getStartSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
            int end = getEndSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
            return df.format(getStartDate())+" - "+df.format(getEndDate())+" "+
                Constants.toTime(start)+" - "+Constants.toTime(end);
        }
    }

    public static class CacheElement{
        private TimeFrame iTime;
        private Hashtable<Long, TreeSet<TimeBlock>> iAvailability = new Hashtable();
        private String iTimestamp = null;
        private String iExcludeType = null;
        public CacheElement(TimeFrame time, String excludeType) {
            iTime = time;
            iExcludeType = excludeType;
        };
        public void update(EventDateMapping.Class2EventDateMap class2eventDateMap) {
            iAvailability.clear();
            String exclude = null;
            if (iExcludeType!=null) {
                if (sFinalExamType.equals(iExcludeType))
                    exclude = "FinalExamEvent";
                else if (sMidtermExamType.equals(iExcludeType))
                    exclude = "MidtermExamEvent";
                else if (sClassType.equals(iExcludeType))
                    exclude = "ClassEvent";	
            }
            Query q = new _RootDAO().getSession().createQuery(
                    "select m from Meeting m where m.locationPermanentId!=null and "+
                    "m.approvalStatus = 1 and "+
                    "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                    "m.startPeriod<:endSlot and m.stopPeriod>:startSlot" + 
                    (exclude==null?"":" and m.event.class!="+exclude))
                    .setDate("startDate", iTime.getStartDate())
                    .setDate("endDate", iTime.getEndDate())
                    .setInteger("startSlot", iTime.getStartSlot())
                    .setInteger("endSlot", iTime.getEndSlot())
                    .setCacheable(true);
            for (Iterator i=q.list().iterator();i.hasNext();) {
                Meeting m = (Meeting)i.next(); 
                TreeSet<TimeBlock> blocks = iAvailability.get(m.getLocationPermanentId());
                if (blocks==null) {
                    blocks = new TreeSet(); iAvailability.put(m.getLocationPermanentId(), blocks);
                }
                MeetingTimeBlock block = new MeetingTimeBlock(m, class2eventDateMap);
                if (block.getStartTime() != null)
                	blocks.add(block);
            }
            iTimestamp = new Date().toString();
        }
        public TreeSet<TimeBlock> get(Long roomPermId, String excludeType) {
            TreeSet<TimeBlock> roomAvailability = iAvailability.get(roomPermId);
            if (roomAvailability==null || excludeType==null || excludeType.equals(iExcludeType)) return roomAvailability;
            TreeSet<TimeBlock> ret = new TreeSet();
            for (TimeBlock block : roomAvailability) {
            	if (excludeType.equals(block.getEventType())) continue;
                ret.add(block);
            }
            return ret;
        }
        public TimeFrame getTimeFrame() { return iTime; }
        public String getExcludeType() { return iExcludeType; }
        public boolean exclude(String type) {
        	return (iExcludeType==null || iExcludeType.equals(type));
        }
        public boolean cover(TimeFrame time) {
            return (iTime.getStartDate().compareTo(time.getStartDate())<=0 && 
                    time.getEndDate().compareTo(iTime.getEndDate())<=0 && 
                    iTime.getStartSlot()<=time.getStartSlot() && 
                    time.getEndSlot()<=iTime.getEndSlot());
        }
        public String getTimestamp() { return iTimestamp; }
        public String toString() {
            return iTime.toString();
        }
    }
    
    public static class MeetingTimeBlock implements TimeBlock, Comparable<TimeBlock> {
		private static final long serialVersionUID = -5557707709984628517L;
		Long iMeetingId;
        String iEventName, iEventType;
        Date iStart, iEnd;
        public MeetingTimeBlock(Meeting m, EventDateMapping.Class2EventDateMap class2eventDateMap) {
            iMeetingId = m.getUniqueId();
            iEventName = m.getEvent().getEventName();
            iEventType = m.getEvent().getEventTypeLabel();
            iStart = m.getTrueStartTime(class2eventDateMap);
            iEnd = m.getTrueStopTime(class2eventDateMap);
        }
        public Long getId() { return iMeetingId; }
        public String getEventName() { return iEventName; }
        public String getEventType() { return iEventType; }
        public Date getStartTime() { return iStart; }
        public Date getEndTime() { return iEnd; }
        public String toString() {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm");
            SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
            return getEventName()+" ("+getEventType()+") "+df.format(getStartTime())+" - "+df2.format(getEndTime());
        }
        public boolean equals(Object o) {
            if (o==null || !(o instanceof MeetingTimeBlock)) return false;
            MeetingTimeBlock m = (MeetingTimeBlock)o;
            return getId().equals(m.getId());
        }
        public int hashCode() {
            return getId().hashCode();
        }
        public int compareTo(TimeBlock block) {
            int cmp = getStartTime().compareTo(block.getStartTime());
            if (cmp!=0) return cmp;
            cmp = getEndTime().compareTo(block.getEndTime());
            if (cmp!=0) return cmp;
            return getEventName().compareTo(block.getEventName());
        }
    }
}
