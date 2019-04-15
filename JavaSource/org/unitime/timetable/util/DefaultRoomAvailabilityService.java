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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventDateMapping;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.FinalExamEvent;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.MidtermExamEvent;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.LocationDAO;

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
            TreeSet<TimeBlock> ret = new TreeSet<TimeBlock>();
            Class<? extends Event> exclude = null;
            ExamType examType = null;
            if (excludeType!=null) {
                if (sFinalExamType.equals(excludeType))
                    exclude = FinalExamEvent.class;
                else if (sMidtermExamType.equals(excludeType))
                    exclude = MidtermExamEvent.class;
                else if (sClassType.equals(excludeType))
                    exclude = ClassEvent.class;
                else {
                	exclude = ExamEvent.class;
                	examType = ExamType.findByReference(excludeType);
                }
            }
            for (Meeting m: (List<Meeting>)LocationDAO.getInstance().getSession().createQuery(
                    "select m from Meeting m where m.locationPermanentId=:locPermId and "+
                    "m.approvalStatus = 1 and "+
                    "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                    "m.startPeriod<:endSlot and m.stopPeriod>:startSlot"+
                    (examType != null ? " and m.event.uniqueId not in (select x.uniqueId from ExamEvent x where x.exam.examType = " + examType.getUniqueId() + ")" :
                    exclude != null ? " and m.event.class!=" + exclude.getSimpleName() : ""))
                    .setLong("locPermId", location.getPermanentId())
                    .setDate("startDate", time.getStartDate())
                    .setDate("endDate", time.getEndDate())
                    .setInteger("startSlot", time.getStartSlot())
                    .setInteger("endSlot", time.getEndSlot())
                    .setCacheable(true).list()) {
                MeetingTimeBlock block = new MeetingTimeBlock(m, class2eventDateMap);
                if (block.getStartTime() != null)
                	ret.add(block);
            }
            if (ApplicationProperty.RoomAvailabilityIncludeOtherTerms.isTrue() && excludeType != null) {
            	if (ClassEvent.class.isAssignableFrom(exclude)) {
            		for (Meeting m: (List<Meeting>)LocationDAO.getInstance().getSession().createQuery(
                            "select m from ClassEvent e inner join e.meetings m where m.locationPermanentId=:locPermId and "+
                            "m.approvalStatus = 1 and e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId != :sessionId and "+
                            "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                            "m.startPeriod<:endSlot and m.stopPeriod>:startSlot")
                            .setLong("locPermId", location.getPermanentId())
                            .setLong("sessionId", location.getSession().getUniqueId())
                            .setDate("startDate", time.getStartDate())
                            .setDate("endDate", time.getEndDate())
                            .setInteger("startSlot", time.getStartSlot())
                            .setInteger("endSlot", time.getEndSlot())
                            .setCacheable(true).list()) {
                        MeetingTimeBlock block = new MeetingTimeBlock(m, class2eventDateMap);
                        if (block.getStartTime() != null)
                        	ret.add(block);
                    }
            	} else if (ExamEvent.class.isAssignableFrom(exclude)) {
            		for (Meeting m: (List<Meeting>)LocationDAO.getInstance().getSession().createQuery(
                            "select m from " + exclude.getSimpleName() + " e inner join e.meetings m where m.locationPermanentId=:locPermId and "+
                            "m.approvalStatus = 1 and e.exam.session.uniqueId != :sessionId and "+
                            "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                            "m.startPeriod<:endSlot and m.stopPeriod>:startSlot"+
                            (examType != null ? " and e.exam.examType = " + examType.getUniqueId() : ""))
                            .setLong("locPermId", location.getPermanentId())
                            .setLong("sessionId", location.getSession().getUniqueId())
                            .setDate("startDate", time.getStartDate())
                            .setDate("endDate", time.getEndDate())
                            .setInteger("startSlot", time.getStartSlot())
                            .setInteger("endSlot", time.getEndSlot())
                            .setCacheable(true).list()) {
                        MeetingTimeBlock block = new MeetingTimeBlock(m, class2eventDateMap);
                        if (block.getStartTime() != null)
                        	ret.add(block);
                    }
            	}
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
            cache.update(class2eventDateMap, session.getUniqueId(), iInstructorAvailabilityEnabled);
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
        private Map<Long, TreeSet<TimeBlock>> iAvailability = new HashMap<Long, TreeSet<TimeBlock>>();
        private Map<String, TreeSet<TimeBlock>> iInstructorAvailability = new HashMap<String, TreeSet<TimeBlock>>();
        private String iTimestamp = null;
        private String iExcludeType = null;
        public CacheElement(TimeFrame time, String excludeType) {
            iTime = time;
            iExcludeType = excludeType;
        }

        public void update(EventDateMapping.Class2EventDateMap class2eventDateMap, Long sessionId, boolean includeInstructors) {
        	iAvailability.clear();
        	iInstructorAvailability.clear();
            Class<? extends Event> exclude = null;
            ExamType examType = null;
            if (iExcludeType!=null) {
                if (sFinalExamType.equals(iExcludeType))
                    exclude = FinalExamEvent.class;
                else if (sMidtermExamType.equals(iExcludeType))
                    exclude = MidtermExamEvent.class;
                else if (sClassType.equals(iExcludeType))
                    exclude = ClassEvent.class;
                else {
                	exclude = ExamEvent.class;
                	examType = ExamType.findByReference(iExcludeType);
                }
            }
            addAll(LocationDAO.getInstance().getSession().createQuery(
                    "select m from Meeting m where m.locationPermanentId!=null and "+
                    "m.approvalStatus = 1 and "+
                    "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                    "m.startPeriod<:endSlot and m.stopPeriod>:startSlot" +
                    (examType != null ? " and m.event.uniqueId not in (select x.uniqueId from ExamEvent x where x.exam.examType = " + examType.getUniqueId() + ")" :
                    	exclude == null ? "" : " and m.event.class!=" + exclude.getSimpleName()))
                    .setDate("startDate", iTime.getStartDate())
                    .setDate("endDate", iTime.getEndDate())
                    .setInteger("startSlot", iTime.getStartSlot())
                    .setInteger("endSlot", iTime.getEndSlot())
                    .setCacheable(true)
                    .list(), class2eventDateMap);
            if (sessionId != null && ApplicationProperty.RoomAvailabilityIncludeOtherTerms.isTrue() && exclude != null) {
            	if (ClassEvent.class.isAssignableFrom(exclude)) {
            		addAll(LocationDAO.getInstance().getSession().createQuery(
                            "select m from ClassEvent e inner join e.meetings m where m.locationPermanentId in (select l.permanentId from Location l where l.session = :sessionId) and "+
                            "m.approvalStatus = 1 and e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId != :sessionId and "+
                            "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                            "m.startPeriod<:endSlot and m.stopPeriod>:startSlot")
                            .setLong("sessionId", sessionId)
                            .setDate("startDate", iTime.getStartDate())
                            .setDate("endDate", iTime.getEndDate())
                            .setInteger("startSlot", iTime.getStartSlot())
                            .setInteger("endSlot", iTime.getEndSlot())
                            .setCacheable(true).list(), class2eventDateMap);
            	} else if (ExamEvent.class.isAssignableFrom(exclude)) {
            		addAll(LocationDAO.getInstance().getSession().createQuery(
                            "select m from " + exclude.getSimpleName() + " e inner join e.meetings m where m.locationPermanentId in (select l.permanentId from Location l where l.session = :sessionId) and "+
                            "m.approvalStatus = 1 and e.exam.session.uniqueId != :sessionId and "+
                            "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                            "m.startPeriod<:endSlot and m.stopPeriod>:startSlot" +
                            (examType != null ? " and e.exam.examType = " + examType.getUniqueId() : ""))
                            .setLong("sessionId", sessionId)
                            .setDate("startDate", iTime.getStartDate())
                            .setDate("endDate", iTime.getEndDate())
                            .setInteger("startSlot", iTime.getStartSlot())
                            .setInteger("endSlot", iTime.getEndSlot())
                            .setCacheable(true).list(), class2eventDateMap);
            	}
            }
            if (sessionId != null && includeInstructors) {
            	addAllInstructors(LocationDAO.getInstance().getSession().createQuery(
            			"select distinct m, i.externalUniqueId from Meeting m left outer join m.event.additionalContacts c, DepartmentalInstructor i where " +
                         "i.department.session.uniqueId = :sessionId and i.externalUniqueId is not null and "+
                         "(m.event.mainContact.externalUniqueId = i.externalUniqueId or c.externalUniqueId = i.externalUniqueId) and "+
                         "m.approvalStatus = 1 and "+
                         "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                         "m.startPeriod<:endSlot and m.stopPeriod>:startSlot"+
                         (examType != null ? " and m.event.uniqueId not in (select x.uniqueId from ExamEvent x where x.exam.examType = " + examType.getUniqueId() + ")" :
                        	 exclude!=null?" and m.event.class!="+exclude.getSimpleName():""))
                         .setDate("startDate", iTime.getStartDate())
                         .setDate("endDate", iTime.getEndDate())
                         .setLong("sessionId", sessionId)
                         .setInteger("startSlot", iTime.getStartSlot())
                         .setInteger("endSlot", iTime.getEndSlot())
                         .setCacheable(true).list(), class2eventDateMap);
            	if (ApplicationProperty.RoomAvailabilityIncludeOtherTerms.isTrue() && exclude != null) {
            		if (ClassEvent.class.isAssignableFrom(exclude)) {
            			addAllInstructors(LocationDAO.getInstance().getSession().createQuery(
                                "select m, ci.instructor.externalUniqueId from ClassEvent e inner join e.meetings m inner join e.clazz.classInstructors ci where "+
                                "ci.lead = true and m.approvalStatus = 1 and e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId != :sessionId and "+
                                "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                                "m.startPeriod<:endSlot and m.stopPeriod>:startSlot")
                                .setLong("sessionId", sessionId)
                                .setDate("startDate", iTime.getStartDate())
                                .setDate("endDate", iTime.getEndDate())
                                .setInteger("startSlot", iTime.getStartSlot())
                                .setInteger("endSlot", iTime.getEndSlot())
                                .setCacheable(true).list(), class2eventDateMap);
                	} else if (ExamEvent.class.isAssignableFrom(exclude)) {
                		addAllInstructors(LocationDAO.getInstance().getSession().createQuery(
                                "select m, di.externalUniqueId from " + exclude.getSimpleName() + " e inner join e.meetings m inner join e.exam.instructors di where  "+
                                "m.approvalStatus = 1 and e.exam.session.uniqueId != :sessionId and "+
                                "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                                "m.startPeriod<:endSlot and m.stopPeriod>:startSlot" +
                                (examType != null ? " and e.exam.examType = " + examType.getUniqueId() : ""))
                                .setLong("sessionId", sessionId)
                                .setDate("startDate", iTime.getStartDate())
                                .setDate("endDate", iTime.getEndDate())
                                .setInteger("startSlot", iTime.getStartSlot())
                                .setInteger("endSlot", iTime.getEndSlot())
                                .setCacheable(true).list(), class2eventDateMap);
                	}
                }
            }
            iTimestamp = new Date().toString();
        }
        
        private void add(Meeting m, EventDateMapping.Class2EventDateMap class2eventDateMap) {
        	TreeSet<TimeBlock> blocks = iAvailability.get(m.getLocationPermanentId());
            if (blocks==null) {
                blocks = new TreeSet(); iAvailability.put(m.getLocationPermanentId(), blocks);
            }
            MeetingTimeBlock block = new MeetingTimeBlock(m, class2eventDateMap);
            if (block.getStartTime() != null)
            	blocks.add(block);
        }
        private void addAll(List<Meeting> meetings, EventDateMapping.Class2EventDateMap class2eventDateMap) {
        	if (meetings != null)
        		for (Meeting m: meetings)
        			add(m, class2eventDateMap);
        }
        private void add(Meeting m, String instructorExternalId, EventDateMapping.Class2EventDateMap class2eventDateMap) {
        	TreeSet<TimeBlock> blocks = iInstructorAvailability.get(instructorExternalId);
            if (blocks==null) {
                blocks = new TreeSet(); iInstructorAvailability.put(instructorExternalId, blocks);
            }
            MeetingTimeBlock block = new MeetingTimeBlock(m, class2eventDateMap);
            if (block.getStartTime() != null)
            	blocks.add(block);
        }
        private void addAllInstructors(List<Object[]> meetings, EventDateMapping.Class2EventDateMap class2eventDateMap) {
        	if (meetings != null)
        		for (Object[] o: meetings) {
        			Meeting m = (Meeting)o[0];
                	String id = (String)o[1];
        			add(m, id, class2eventDateMap);
        		}
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
        public TreeSet<TimeBlock> get(String instructorExternalId, String excludeType) {
        	if (instructorExternalId == null) return null;
            TreeSet<TimeBlock> instructorAvailability = iInstructorAvailability.get(instructorExternalId);
            if (instructorAvailability==null || excludeType==null || excludeType.equals(iExcludeType)) return instructorAvailability;
            TreeSet<TimeBlock> ret = new TreeSet();
            for (TimeBlock block : instructorAvailability) {
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
		Long iEventId, iMeetingId, iLocationPermanentId;
        String iEventName, iEventType;
        Date iStart, iEnd;
        public MeetingTimeBlock(Meeting m, EventDateMapping.Class2EventDateMap class2eventDateMap) {
        	iEventId = m.getEvent().getUniqueId();
            iMeetingId = m.getUniqueId();
            iLocationPermanentId = m.getLocationPermanentId();
            iEventName = m.getEvent().getEventName();
            iEventType = m.getEvent().getEventTypeAbbv();
            iStart = m.getTrueStartTime(class2eventDateMap);
            iEnd = m.getTrueStopTime(class2eventDateMap);
        }
        public Long getEventId() { return iEventId; }
        public Long getMeetingId() { return iMeetingId; }
        public Long getLocationPermanentId() { return iLocationPermanentId; }
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
            if (cache!=null) return cache.get(instructor.getExternalUniqueId(), excludeType);
            TreeSet<TimeBlock> ret = new TreeSet<TimeBlock>();
            Class<? extends Event> exclude = null;
            ExamType examType = null;
            if (excludeType!=null) {
                if (sFinalExamType.equals(excludeType))
                    exclude = FinalExamEvent.class;
                else if (sMidtermExamType.equals(excludeType))
                    exclude = MidtermExamEvent.class;
                else if (sClassType.equals(excludeType))
                    exclude = ClassEvent.class;
                else {
                	exclude = ExamEvent.class;
                	examType = ExamType.findByReference(excludeType);
                }
            }
            for (Meeting m: (List<Meeting>)LocationDAO.getInstance().getSession().createQuery(
            		"select m from Meeting m left outer join m.event.additionalContacts c where " +
            		"(m.event.mainContact.externalUniqueId = :user or c.externalUniqueId = :user) and "+
            		"m.approvalStatus = 1 and "+
                    "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                    "m.startPeriod<:endSlot and m.stopPeriod>:startSlot"+
                    (examType != null ? " and m.event.uniqueId not in (select x.uniqueId from ExamEvent x where x.exam.examType = " + examType.getUniqueId() + ")" :
                    	exclude != null ? " and m.event.class!=" + exclude.getSimpleName() : ""))
                    .setString("user", instructor.getExternalUniqueId())
                    .setDate("startDate", time.getStartDate())
                    .setDate("endDate", time.getEndDate())
                    .setInteger("startSlot", time.getStartSlot())
                    .setInteger("endSlot", time.getEndSlot())
                    .setCacheable(true).list()) {
                MeetingTimeBlock block = new MeetingTimeBlock(m, class2eventDateMap);
                if (block.getStartTime() != null)
                	ret.add(block);
            }
            if (ApplicationProperty.RoomAvailabilityIncludeOtherTerms.isTrue() && excludeType != null) {
            	if (ClassEvent.class.isAssignableFrom(exclude)) {
            		for (Meeting m: (List<Meeting>)LocationDAO.getInstance().getSession().createQuery(
                            "select m from ClassEvent e inner join e.meetings m inner join e.clazz.classInstructors ci where "+
                            "ci.instructor.externalUniqueId = :user and ci.lead = true and "+
                            "m.approvalStatus = 1 and e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId != :sessionId and "+
                            "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                            "m.startPeriod<:endSlot and m.stopPeriod>:startSlot")
            				.setString("user", instructor.getExternalUniqueId())
                            .setLong("sessionId", instructor.getDepartment().getSession().getUniqueId())
                            .setDate("startDate", time.getStartDate())
                            .setDate("endDate", time.getEndDate())
                            .setInteger("startSlot", time.getStartSlot())
                            .setInteger("endSlot", time.getEndSlot())
                            .setCacheable(true).list()) {
                        MeetingTimeBlock block = new MeetingTimeBlock(m, class2eventDateMap);
                        if (block.getStartTime() != null)
                        	ret.add(block);
                    }
            	} else if (ExamEvent.class.isAssignableFrom(exclude)) {
            		for (Meeting m: (List<Meeting>)LocationDAO.getInstance().getSession().createQuery(
                            "select m from " + exclude.getSimpleName() + " e inner join e.meetings m inner join e.exam.instructors di where  "+
                            "di.externalUniqueId = :user and "+
                            "m.approvalStatus = 1 and e.exam.session.uniqueId != :sessionId and "+
                            "m.meetingDate>=:startDate and m.meetingDate<=:endDate and "+
                            "m.startPeriod<:endSlot and m.stopPeriod>:startSlot" + 
                            (examType != null ? " and e.exam.examType = " + examType.getUniqueId() : ""))
            				.setString("user", instructor.getExternalUniqueId())
                            .setLong("sessionId", instructor.getDepartment().getSession().getUniqueId())
                            .setDate("startDate", time.getStartDate())
                            .setDate("endDate", time.getEndDate())
                            .setInteger("startSlot", time.getStartSlot())
                            .setInteger("endSlot", time.getEndSlot())
                            .setCacheable(true).list()) {
                        MeetingTimeBlock block = new MeetingTimeBlock(m, class2eventDateMap);
                        if (block.getStartTime() != null)
                        	ret.add(block);
                    }
            	}
            }
            return ret;
        }
	}
}
