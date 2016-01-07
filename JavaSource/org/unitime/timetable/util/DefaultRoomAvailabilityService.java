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
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.EventDateMapping;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
public class DefaultRoomAvailabilityService implements RoomAvailabilityInterface {
    private Vector<CacheElement> iCache = new Vector<CacheElement>();
    private boolean iInstructorAvailabilityEnabled = false;
    
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
    public Collection<TimeBlock> getRoomAvailability(Long locationId, Date startTime, Date endTime, String excludeType) {
    	Location location = LocationDAO.getInstance().get(locationId);
        if (location == null || location.getPermanentId() == null) return null;
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
        iInstructorAvailabilityEnabled = ApplicationProperty.RoomAvailabilityIncludeInstructors.isTrue();
        TimeFrame time = new TimeFrame(startTime, endTime);
        EventDateMapping.Class2EventDateMap class2eventDateMap = (sClassType.equals(excludeType) ? EventDateMapping.getMapping(session.getUniqueId()) : null);
        synchronized(iCache) {
            CacheElement cache = get(time, excludeType);
            if (cache==null) {
                cache = new CacheElement(time, excludeType);
                iCache.insertElementAt(cache, 0);
            }
            cache.update(class2eventDateMap, iInstructorAvailabilityEnabled ? session.getUniqueId() : null);
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
        public void update(EventDateMapping.Class2EventDateMap class2eventDateMap, Long sessionId) {
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
            if (sessionId != null) {
                q = new _RootDAO().getSession().createQuery(
                		"select distinct m, -i.uniqueId from Meeting m left outer join m.event.additionalContacts c, DepartmentalInstructor i where " +
                        "i.department.session.uniqueId = :sessionId and i.externalUniqueId is not null and "+
                		"(m.event.mainContact.externalUniqueId = i.externalUniqueId or c.externalUniqueId = i.externalUniqueId) and "+
                		"m.approvalStatus = 1 and "+
                        "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                        "m.startPeriod<:endSlot and m.stopPeriod>:startSlot"+
                        (exclude!=null?" and m.event.class!="+exclude:""))
                        .setDate("startDate", iTime.getStartDate())
                        .setDate("endDate", iTime.getEndDate())
                        .setLong("sessionId", sessionId)
                        .setInteger("startSlot", iTime.getStartSlot())
                        .setInteger("endSlot", iTime.getEndSlot())
                        .setCacheable(true);
                for (Iterator i=q.list().iterator();i.hasNext();) {
                	Object[] o = (Object[])i.next();
                	Meeting m = (Meeting)o[0];
                	Long id = (Long)o[1];
                    TreeSet<TimeBlock> blocks = iAvailability.get(id);
                    if (blocks==null) {
                        blocks = new TreeSet(); iAvailability.put(id, blocks);
                    }
                    MeetingTimeBlock block = new MeetingTimeBlock(m, class2eventDateMap);
                    if (block.getStartTime() != null)
                    	blocks.add(block);
                }            	
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
		Long iEventId, iMeetingId;
        String iEventName, iEventType;
        Date iStart, iEnd;
        public MeetingTimeBlock(Meeting m, EventDateMapping.Class2EventDateMap class2eventDateMap) {
        	iEventId = m.getEvent().getUniqueId();
            iMeetingId = m.getUniqueId();
            iEventName = m.getEvent().getEventName();
            iEventType = m.getEvent().getEventTypeAbbv();
            iStart = m.getTrueStartTime(class2eventDateMap);
            iEnd = m.getTrueStopTime(class2eventDateMap);
        }
        public Long getEventId() { return iEventId; }
        public Long getMeetingId() { return iMeetingId; }
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
            return getMeetingId().equals(m.getMeetingId());
        }
        public int hashCode() {
            return getMeetingId().hashCode();
        }
        public int compareTo(TimeBlock block) {
            int cmp = getStartTime().compareTo(block.getStartTime());
            if (cmp!=0) return cmp;
            cmp = getEndTime().compareTo(block.getEndTime());
            if (cmp!=0) return cmp;
            return getEventName().compareTo(block.getEventName());
        }
    }

	@Override
	public Collection<TimeBlock> getInstructorAvailability(Long instructorId, Date startTime, Date endTime, String excludeType) {
        if (!iInstructorAvailabilityEnabled) return null;
        DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(instructorId);
        if (instructor == null || instructor.getExternalUniqueId() == null) return null;
        EventDateMapping.Class2EventDateMap class2eventDateMap = (sClassType.equals(excludeType) ? EventDateMapping.getMapping(instructor.getDepartment().getSession().getUniqueId()) : null);
        TimeFrame time = new TimeFrame(startTime, endTime);
        synchronized(iCache) {
            CacheElement cache = get(time, excludeType);
            if (cache!=null) return cache.get(-instructor.getUniqueId(), excludeType);
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
            		"select m from Meeting m left outer join m.event.additionalContacts c where " +
            		"(m.event.mainContact.externalUniqueId = :user or c.externalUniqueId = :user) and "+
            		"m.approvalStatus = 1 and "+
                    "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                    "m.startPeriod<:endSlot and m.stopPeriod>:startSlot"+
                    (exclude!=null?" and m.event.class!="+exclude:""))
                    .setString("user", instructor.getExternalUniqueId())
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
}
