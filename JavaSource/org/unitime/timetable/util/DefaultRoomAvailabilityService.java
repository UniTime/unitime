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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.EventType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;

public class DefaultRoomAvailabilityService implements RoomAvailabilityInterface {
    private static Log sLog = LogFactory.getLog(DefaultRoomAvailabilityService.class);
    private Vector<CacheElement> iCache = new Vector<CacheElement>();
    
    public String getTimeStamp(Date startTime, Date endTime) {
        TimeFrame time = new TimeFrame(startTime, endTime);
        CacheElement cache = get(time);
        return (cache==null?null:cache.getTimestamp());
    }
    
    public CacheElement get(TimeFrame time) {
        synchronized (iCache) {
            for (CacheElement cache : iCache) if (cache.cover(time)) return cache;
        }
        return null;
    }
    public Collection<TimeBlock> getRoomAvailability(Location location, Date startTime, Date endTime, String[] excludeTypes) {
        if (location.getPermanentId()==null) return null;
        TimeFrame time = new TimeFrame(startTime, endTime);
        synchronized(iCache) {
            CacheElement cache = get(time);
            if (cache!=null) return cache.get(location.getPermanentId(), excludeTypes);
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
            if (excludeTypes!=null && excludeTypes.length>0) {
                for (int i=0;i<excludeTypes.length;i++) {
                    if (exclude==null) exclude=""; else exclude+=",";
                    if (sFinalExamType.equals(excludeTypes[i])) exclude+="'"+EventType.sEventTypeFinalExam+"'";
                    else if (sEveningExamType.equals(excludeTypes[i])) exclude+="'"+EventType.sEventTypeEveningExam+"'";
                    else if (sClassType.equals(excludeTypes[i])) exclude+="'"+EventType.sEventTypeClass+"'";
                }
            }
            Query q = new _RootDAO().getSession().createQuery(
                    "select m from Meeting m where m.locationPermanentId=:locPermId and "+
                    "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                    "m.startPeriod<:endSlot and m.stopPeriod>:startSlot"+
                    (exclude!=null?excludeTypes.length==1?" and m.eventType.reference!="+exclude:" and m.eventType.reference not in ("+exclude+")":""))
                    .setLong("locPermId", location.getPermanentId())
                    .setDate("startDate", start.getTime())
                    .setDate("endDate", end.getTime())
                    .setInteger("startSlot", startSlot)
                    .setInteger("endSlot", endSlot)
                    .setCacheable(true);
            for (Iterator i=q.iterate();i.hasNext();) {
                Meeting m = (Meeting)i.next(); 
                ret.add(new MeetingTimeBlock(m));
            }
            return ret;
        }
    }
    public void activate(Session session, Date startTime, Date endTime, boolean waitForSync) {
        TimeFrame time = new TimeFrame(startTime, endTime);
        synchronized(iCache) {
            CacheElement cache = get(time);
            if (cache==null) {
                cache = new CacheElement(time);
                iCache.add(cache);
            }
            cache.update();
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
            int startHour = start/60;
            int startMin = start%60;
            int end = getEndSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
            int endHour = end/60;
            int endMin = end%60;
            return df.format(getStartDate())+" - "+df.format(getEndDate())+" "+
                (startHour>12?startHour-12:startHour)+":"+(startMin<10?"0":"")+startMin+(startHour>=12?"p":"a")+" - "+
                (endHour>12?endHour-12:endHour)+":"+(endMin<10?"0":"")+endMin+(endHour>=12?"p":"a");
        }
    }

    public static class CacheElement{
        private TimeFrame iTime;
        private Hashtable<Long, TreeSet<TimeBlock>> iAvailability = new Hashtable();
        private String iTimestamp = null;
        public CacheElement(TimeFrame time) {
            iTime = time;
        };
        public void update() {
            iAvailability.clear();
            Query q = new _RootDAO().getSession().createQuery(
                    "select m from Meeting m where m.locationPermanentId!=null and "+
                    "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                    "m.startPeriod<:endSlot and m.stopPeriod>:startSlot")
                    .setDate("startDate", iTime.getStartDate())
                    .setDate("endDate", iTime.getEndDate())
                    .setInteger("startSlot", iTime.getStartSlot())
                    .setInteger("endSlot", iTime.getEndSlot())
                    .setCacheable(true);
            for (Iterator i=q.iterate();i.hasNext();) {
                Meeting m = (Meeting)i.next(); 
                TreeSet<TimeBlock> blocks = iAvailability.get(m.getLocationPermanentId());
                if (blocks==null) {
                    blocks = new TreeSet(); iAvailability.put(m.getLocationPermanentId(), blocks);
                }
                blocks.add(new MeetingTimeBlock(m));
            }
            iTimestamp = new Date().toString();
        }
        public TreeSet<TimeBlock> get(Long roomPermId, String[] excludeTypes) {
            TreeSet<TimeBlock> roomAvailability = iAvailability.get(roomPermId);
            if (roomAvailability==null || excludeTypes==null || excludeTypes.length==0) return roomAvailability;
            TreeSet<TimeBlock> ret = new TreeSet();
            blocks: for (TimeBlock block : roomAvailability) {
                String ref = ((MeetingTimeBlock)block).getEventRef();
                for (int i=0;i<excludeTypes.length;i++) {
                    if (sFinalExamType.equals(excludeTypes[i]) && EventType.sEventTypeFinalExam.equals(ref))
                        continue blocks;
                    if (sEveningExamType.equals(excludeTypes[i]) && EventType.sEventTypeEveningExam.equals(ref))
                        continue blocks;
                    if (sClassType.equals(excludeTypes[i]) && EventType.sEventTypeClass.equals(ref))
                        continue blocks;
                }
                ret.add(block);
            }
            return ret;
        }
        public TimeFrame getTimeFrame() { return iTime; }
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
        Long iMeetingId;
        String iEventName, iEventType, iEventRef;
        Date iStart, iEnd;
        public MeetingTimeBlock(Meeting m) {
            iMeetingId = m.getUniqueId();
            iEventName = m.getEvent().getEventName();
            iEventType = m.getEventType().getLabel();
            iEventRef = m.getEventType().getReference();
            iStart = m.getStartTime();
            iEnd = m.getStopTime();
        }
        public Long getId() { return iMeetingId; }
        public String getEventName() { return iEventName; }
        public String getEventType() { return iEventType; }
        public String getEventRef() { return iEventRef; }
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
