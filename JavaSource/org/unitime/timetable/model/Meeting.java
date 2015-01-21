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
 
package org.unitime.timetable.model;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.unitime.timetable.model.base.BaseMeeting;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.MeetingDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
public class Meeting extends BaseMeeting implements Comparable<Meeting> {
	private static final long serialVersionUID = 1L;
	private Location location = null;
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public Meeting () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Meeting (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static enum Status {
		PENDING,
		APPROVED,
		REJECTED,
		CANCELLED,
		;
	}

	@Override
	public Object clone()  {
		Meeting newMeeting = new Meeting();
		newMeeting.setClassCanOverride(isClassCanOverride());
		newMeeting.setLocationPermanentId(getLocationPermanentId());
		newMeeting.setMeetingDate(getMeetingDate());
		newMeeting.setStartOffset(getStartOffset());
		newMeeting.setStartPeriod(getStartPeriod());
		newMeeting.setStopOffset(getStopOffset());
		newMeeting.setStopPeriod(getStopPeriod());
        newMeeting.setStatus(Meeting.Status.PENDING);
		
		return(newMeeting);
	}

	public int compareTo(Meeting other) {
		int cmp = getMeetingDate().compareTo(other.getMeetingDate());
		if (cmp!=0) return cmp;
		cmp = getStartPeriod().compareTo(other.getStartPeriod());
		if (cmp!=0) return cmp;
		cmp = getRoomLabel().compareTo(other.getRoomLabel());
		if (cmp!=0) return cmp;
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(other.getUniqueId() == null ? -1 : other.getUniqueId());
	}
	
	public void setLocation(Location location) {
		this.location = location;
		if (location == null) {
			setLocationPermanentId(null);
		} else {
			setLocationPermanentId(location.getPermanentId());
		}
	}
	
	public Location getLocation(){
		if (location != null){
			return(location);
		}
		if(getLocationPermanentId() == null){
			return(null);
		}
		if (getMeetingDate() == null){
			return(null);
		}
		Session session = null;
		if (getEvent() != null){
			session = getEvent().getSession();
		}
		if (session!=null) {
		    location = (Location)RoomDAO.getInstance().getSession().createQuery(
		            "select r from Room r where r.permanentId = :permId and r.session.uniqueId=:sessionId")
		            .setLong("sessionId", session.getUniqueId())
		            .setLong("permId", getLocationPermanentId())
		            .setCacheable(true)
		            .uniqueResult();
		    if (location==null)
		        location = (Location)RoomDAO.getInstance().getSession().createQuery(
		            "select r from NonUniversityLocation r where r.permanentId = :permId and r.session.uniqueId=:sessionId")
		            .setLong("sessionId", session.getUniqueId())
		            .setLong("permId", getLocationPermanentId())
		            .setCacheable(true)
		            .uniqueResult();
		    return location;
		}
		long distance = -1;
		List<Location> locations = (List<Location>)LocationDAO.getInstance().getSession().createQuery(
				"from Location where permanentId = :permId").
				setLong("permId", getLocationPermanentId()).
				setCacheable(true).list();
		if (!locations.isEmpty()) {
			for (Location loc: locations) {
				if (loc.getSession().getStatusType().isTestSession()) continue;
				long dist = loc.getSession().getDistance(getMeetingDate());
				if (location==null || distance>dist) {
					location = loc;
					distance = dist;
				}
			}
			for (Location loc: locations) {
				if (!loc.getSession().getStatusType().isTestSession()) continue;
				long dist = loc.getSession().getDistance(getMeetingDate());
				if (location==null || distance>dist) {
					location = loc;
					distance = dist;
				}
			}
		}
		return(location);
	}
	
	public List<Meeting> getTimeRoomOverlaps(){
	    if (getLocationPermanentId()==null) return new ArrayList<Meeting>();
		return (MeetingDAO.getInstance()).getSession().createQuery(
		        "from Meeting m where m.meetingDate=:meetingDate and m.startPeriod < :stopPeriod and m.stopPeriod > :startPeriod and " +
		        "m.locationPermanentId = :locPermId and m.uniqueId != :uniqueId and m.approvalStatus <= 1")
		        .setDate("meetingDate", getMeetingDate())
		        .setInteger("stopPeriod", getStopPeriod())
		        .setInteger("startPeriod", getStartPeriod())
		        .setLong("locPermId", getLocationPermanentId())
		        .setLong("uniqueId", this.getUniqueId())
		        .list();
	}

    public List<Meeting> getApprovedTimeRoomOverlaps(){
        if (getLocationPermanentId()==null) return new ArrayList<Meeting>();
        return (MeetingDAO.getInstance()).getSession().createQuery(
                "from Meeting m where m.meetingDate=:meetingDate and m.startPeriod < :stopPeriod and m.stopPeriod > :startPeriod and " +
                "m.locationPermanentId = :locPermId and m.uniqueId != :uniqueId and m.approvalStatus = 1")
                .setDate("meetingDate", getMeetingDate())
                .setInteger("stopPeriod", getStopPeriod())
                .setInteger("startPeriod", getStartPeriod())
                .setLong("locPermId", getLocationPermanentId())
                .setLong("uniqueId", this.getUniqueId())
                .list();
    }
    
    public static List<Meeting> findOverlaps(Date meetingDate, int startPeriod, int stopPeriod, Long locationPermId){
        return (MeetingDAO.getInstance()).getSession().createQuery(
                "from Meeting m where m.meetingDate=:meetingDate and m.startPeriod < :stopPeriod and m.stopPeriod > :startPeriod and " +
                "m.locationPermanentId = :locPermId and m.approvalStatus <= 1")
                .setDate("meetingDate", meetingDate)
                .setInteger("stopPeriod", stopPeriod)
                .setInteger("startPeriod", startPeriod)
                .setLong("locPermId", locationPermId)
                .list();
    }

    public boolean hasTimeRoomOverlaps(){
        return ((Number)MeetingDAO.getInstance().getSession().createQuery(
                "select count(m) from Meeting m where m.meetingDate=:meetingDate and m.startPeriod < :stopPeriod and m.stopPeriod > :startPeriod and " +
                "m.locationPermanentId = :locPermId and m.uniqueId != :uniqueId and m.approvalStatus <= 1")
                .setDate("meetingDate", getMeetingDate())
                .setInteger("stopPeriod", getStopPeriod())
                .setInteger("startPeriod", getStartPeriod())
                .setLong("locPermId", getLocationPermanentId())
                .setLong("uniqueId", this.getUniqueId())
                .uniqueResult()).longValue()>0;
	}
	
	public String toString() {
		return (DateFormat.getDateInstance(DateFormat.SHORT).format(getMeetingDate())+" "+
		        (isAllDay()?"All Day":startTime()+" - "+stopTime())+
		        (getLocation()==null?"":", "+getLocation().getLabel()));
	}
	
	public String getTimeLabel() {
        return dateStr() + " " + startTime() + " - " + stopTime();
    }
	
	public String getRoomLabel() {
        return getLocation() == null?"":getLocation().getLabel();
    }
	
	private String periodToTime(Integer slot, Integer offset){
		if (slot==null) return("");
		int min = slot.intValue()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
		if (offset!=null) min += offset;
		return Constants.toTime(min);
	}
	
	public String startTime(){
		return(periodToTime(getStartPeriod(), getStartOffset()));
	}

	public String stopTime(){
		return(periodToTime(getStopPeriod(), getStopOffset()));
	}
	
	public String startTimeNoOffset(){
		return(periodToTime(getStartPeriod(), 0));
	}

	public String stopTimeNoOffset(){
		return(periodToTime(getStopPeriod(), 0));
	}
	
	public String dateStr() {
	    return Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD).format(getMeetingDate());
	}
	
	public Date getStartTime() {
		Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getMeetingDate());
        int min = (getStartPeriod().intValue()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN)+(getStartOffset()==null?0:getStartOffset());
        c.set(Calendar.HOUR, min/60);
        c.set(Calendar.MINUTE, min%60);
        return c.getTime();
	}

	public Date getTrueStartTime() {
		Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getMeetingDate());
        int min = (getStartPeriod().intValue()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN);
        c.set(Calendar.HOUR, min/60);
        c.set(Calendar.MINUTE, min%60);
        return c.getTime();
	}

	public Date getStopTime() {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getMeetingDate());
        int min = (getStopPeriod().intValue()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN)+(getStopOffset()==null?0:getStopOffset());
        c.set(Calendar.HOUR, min/60);
        c.set(Calendar.MINUTE, min%60);
        return c.getTime();
    }
    
	public Date getTrueStopTime() {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getMeetingDate());
        int min = (getStopPeriod().intValue()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN);
        c.set(Calendar.HOUR, min/60);
        c.set(Calendar.MINUTE, min%60);
        return c.getTime();
    }

	public Date getTrueStartTime(EventDateMapping.Class2EventDateMap class2eventDateMap) {
		Calendar c = Calendar.getInstance(Locale.US);
		Date meetingDate = class2eventDateMap == null ? getMeetingDate() : class2eventDateMap.getClassDate(getMeetingDate());
		if (meetingDate == null) return null;
        c.setTime(meetingDate);
        int min = (getStartPeriod().intValue()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN);
        c.set(Calendar.HOUR, min/60);
        c.set(Calendar.MINUTE, min%60);
        return c.getTime();
	}
	
	public Date getTrueStopTime(EventDateMapping.Class2EventDateMap class2eventDateMap) {
        Calendar c = Calendar.getInstance(Locale.US);
		Date meetingDate = class2eventDateMap == null ? getMeetingDate() : class2eventDateMap.getClassDate(getMeetingDate());
		if (meetingDate == null) return null;
        c.setTime(meetingDate);
        int min = (getStopPeriod().intValue()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN);
        c.set(Calendar.HOUR, min/60);
        c.set(Calendar.MINUTE, min%60);
        return c.getTime();
    }
	
	public int getDayOfWeek() {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getMeetingDate());
        return c.get(Calendar.DAY_OF_WEEK);
    }
    
    public boolean isApproved() { return getApprovalStatus() != null && getApprovalStatus().equals(Status.APPROVED.ordinal()); }
    
    public boolean overlaps(Meeting meeting) {
        if (getMeetingDate().getTime()!=meeting.getMeetingDate().getTime()) return false;
        return getStartPeriod()<meeting.getStopPeriod() && meeting.getStartPeriod()<getStopPeriod();
    }
    
    public boolean isAllDay() {
        return getStartPeriod()==0 && getStopPeriod()==Constants.SLOTS_PER_DAY;
    }
    
    public void setStatus(Status status) {
    	setApprovalStatus(status.ordinal());
    }
    
    public Status getStatus() {
    	return (getApprovalStatus() == null ? Status.PENDING : Status.values()[getApprovalStatus()]);
    }
}
