/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;

import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Meeting;

public abstract class BaseMeeting implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iMeetingDate;
	private Integer iStartPeriod;
	private Integer iStartOffset;
	private Integer iStopPeriod;
	private Integer iStopOffset;
	private Long iLocationPermanentId;
	private Boolean iClassCanOverride;
	private Date iApprovedDate;

	private Event iEvent;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_MEETING_DATE = "meetingDate";
	public static String PROP_START_PERIOD = "startPeriod";
	public static String PROP_START_OFFSET = "startOffset";
	public static String PROP_STOP_PERIOD = "stopPeriod";
	public static String PROP_STOP_OFFSET = "stopOffset";
	public static String PROP_LOCATION_PERM_ID = "locationPermanentId";
	public static String PROP_CLASS_CAN_OVERRIDE = "classCanOverride";
	public static String PROP_APPROVED_DATE = "approvedDate";

	public BaseMeeting() {
		initialize();
	}

	public BaseMeeting(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Date getMeetingDate() { return iMeetingDate; }
	public void setMeetingDate(Date meetingDate) { iMeetingDate = meetingDate; }

	public Integer getStartPeriod() { return iStartPeriod; }
	public void setStartPeriod(Integer startPeriod) { iStartPeriod = startPeriod; }

	public Integer getStartOffset() { return iStartOffset; }
	public void setStartOffset(Integer startOffset) { iStartOffset = startOffset; }

	public Integer getStopPeriod() { return iStopPeriod; }
	public void setStopPeriod(Integer stopPeriod) { iStopPeriod = stopPeriod; }

	public Integer getStopOffset() { return iStopOffset; }
	public void setStopOffset(Integer stopOffset) { iStopOffset = stopOffset; }

	public Long getLocationPermanentId() { return iLocationPermanentId; }
	public void setLocationPermanentId(Long locationPermanentId) { iLocationPermanentId = locationPermanentId; }

	public Boolean isClassCanOverride() { return iClassCanOverride; }
	public Boolean getClassCanOverride() { return iClassCanOverride; }
	public void setClassCanOverride(Boolean classCanOverride) { iClassCanOverride = classCanOverride; }

	public Date getApprovedDate() { return iApprovedDate; }
	public void setApprovedDate(Date approvedDate) { iApprovedDate = approvedDate; }

	public Event getEvent() { return iEvent; }
	public void setEvent(Event event) { iEvent = event; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Meeting)) return false;
		if (getUniqueId() == null || ((Meeting)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Meeting)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Meeting["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Meeting[" +
			"\n	ApprovedDate: " + getApprovedDate() +
			"\n	ClassCanOverride: " + getClassCanOverride() +
			"\n	Event: " + getEvent() +
			"\n	LocationPermanentId: " + getLocationPermanentId() +
			"\n	MeetingDate: " + getMeetingDate() +
			"\n	StartOffset: " + getStartOffset() +
			"\n	StartPeriod: " + getStartPeriod() +
			"\n	StopOffset: " + getStopOffset() +
			"\n	StopPeriod: " + getStopPeriod() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
