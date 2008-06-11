/* 
 * UniTime 3.1 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2008, UniTime.org
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
 
package org.unitime.timetable.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.unitime.timetable.model.base.BaseMeeting;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.MeetingDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.util.Constants;

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

	/**
	 * Constructor for required fields
	 */
	public Meeting (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Event event,
		java.util.Date meetingDate,
		java.lang.Integer startPeriod,
		java.lang.Integer stopPeriod,
		java.lang.Boolean classCanOverride) {

		super (
			uniqueId,
			event,
			meetingDate,
			startPeriod,
			stopPeriod,
			classCanOverride);
	}

/*[CONSTRUCTOR MARKER END]*/

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
		
		return(newMeeting);
	}

	public int compareTo(Meeting other) {
		int cmp = getMeetingDate().compareTo(other.getMeetingDate());
		if (cmp!=0) return cmp;
		cmp = getStartPeriod().compareTo(other.getStartPeriod());
		if (cmp!=0) return cmp;
		cmp = getRoomLabel().compareTo(other.getRoomLabel());
		if (cmp!=0) return cmp;
		return getUniqueId().compareTo(other.getUniqueId());
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
		Session session = getEvent().getSession();
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
		List locations = LocationDAO.getInstance().getSession().createQuery(
				"from Location where permanentId = :permId").
				setLong("permId", getLocationPermanentId()).
				setCacheable(true).list();
		if (!locations.isEmpty()) {
			for (Iterator it = locations.iterator(); it.hasNext(); ) {
				Location loc = (Location)it.next();
				long dist = loc.getSession().getDistance(getMeetingDate());
				if (location==null || distance>dist) {
					location = loc;
					distance = dist;
				}
			}
		}
		return(location);
	}
	
	public List<?> getTimeRoomOverlaps(){
		return (MeetingDAO.getInstance()).getSession().createQuery("from Meeting m where m.meetingDate=:meetingDate and m.startPeriod < :stopPeriod and m.stopPeriod > :startPeriod and m.locationPermanentId = :locPermId and m.uniqueId != :uniqueId")
		.setDate("meetingDate", getMeetingDate())
		.setInteger("stopPeriod", getStopPeriod())
		.setInteger("startPeriod", getStartPeriod())
		.setLong("locPermId", getLocationPermanentId())
		.setLong("uniqueId", this.getUniqueId())
		.list();
	}

	public boolean hasTimeRoomOverlaps(){
		Long count = (Long)MeetingDAO.getInstance().getSession().createQuery("select count(m) from Meeting m where m.meetingDate=:meetingDate and m.startPeriod < :stopPeriod and m.stopPeriod > :startPeriod and m.locationPermanentId = :locPermId and m.uniqueId != :uniqueId")
		.setDate("meetingDate", getMeetingDate())
		.setInteger("stopPeriod", getStopPeriod())
		.setInteger("startPeriod", getStartPeriod())
		.setLong("locPermId", getLocationPermanentId())
		.setLong("uniqueId", this.getUniqueId())
		.uniqueResult();
		return(count > 0);
	}
	
	public String toString() {
		return (DateFormat.getDateInstance(DateFormat.SHORT).format(getMeetingDate()) + " " + startTime() + " - " + stopTime() +  (getLocation() == null?"":", " + getLocation().getLabel()));
	}
	
	public String getTimeLabel() {
        return dateStr() + " " + startTime() + " - " + stopTime();
    }
	
	public String getRoomLabel() {
        return getLocation() == null?"":getLocation().getLabel();
    }
	
	private String periodToTime(Integer slot, Integer offset){
		if (slot == null){
			return("");
		}
		int min = slot.intValue()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
		if (offset!=null) min += offset;
		int hours = (min/60);
		int minutes = min%60;
		String amPm = "a";
		if (hours >= 12){
			amPm = "p";
			hours -= 12;
		}
		if (hours == 0) {
			hours = 12;
		}
		return(hours + ":" + (minutes<10?"0":"") + minutes + amPm);
	}
	
	public String startTime(){
		return(periodToTime(getStartPeriod(), getStartOffset()));
	}

	public String stopTime(){
		return(periodToTime(getStopPeriod(), getStopOffset()));
	}
	
	public String dateStr() {
	    return new SimpleDateFormat("EEE MM/dd").format(getMeetingDate());
	}
	
	public Date getStartTime() {
		Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getMeetingDate());
        int min = (getStartPeriod().intValue()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN)+(getStartOffset()==null?0:getStartOffset());
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
    
    public int getDayOfWeek() {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getMeetingDate());
        return c.get(Calendar.DAY_OF_WEEK);
    }
    
    public boolean isApproved() { return getApprovedDate()!=null; }
}