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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventInterface implements IsSerializable {
	private Long iEventId;
	private String iEventName;
	private String iEventType;
	private List<MeetingInterface> iMeetings = null;
	
	public static enum ResourceType {
		ROOM("room", "Room Timetable"),
		SUBJECT("subject", "Subject Timetable"),
		CURRICULUM("curriculum", "Curriculum Timetable"),
		DEPARTMENT("department", "Departmental Timetable");
		
		private String iLabel;
		private String iPageTitle;
		
		ResourceType(String label, String title) { iLabel = label; iPageTitle = title; }
		
		public String getLabel() { return iLabel; }
		public String getPageTitle() { return iPageTitle; }
	}

	public EventInterface() {}
	
	public Long getId() { return iEventId; }
	public void setId(Long id) { iEventId = id; }
	public String getName() { return iEventName; }
	public void setName(String name) { iEventName = name; }
	public String getType() { return iEventType; }
	public void setType(String type) { iEventType = type; }
	public boolean hasMeetings() { return iMeetings != null && !iMeetings.isEmpty(); }
	public void addMeeting(MeetingInterface meeting) {
		if (iMeetings == null) iMeetings = new ArrayList<MeetingInterface>();
		iMeetings.add(meeting);
	}
	public List<MeetingInterface> getMeetings() { return iMeetings; }
	
	public static class ResourceInterface implements IsSerializable {
		private ResourceType iResourceType;
		private Long iResourceId;
		private String iResourceName;
		private Long iSessionId;
		private String iSessionName;

		public ResourceInterface() {}
		
		public ResourceType getType() { return iResourceType; }
		public void setType(ResourceType type) { iResourceType = type; }
		public Long getId() { return iResourceId; }
		public void setId(Long id) { iResourceId = id; }
		public String getName() { return iResourceName; }
		public void setName(String name) { iResourceName = name; }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public String getSessionName() { return iSessionName; }
		public void setSessionName(String sessionName) { iSessionName = sessionName; }
		
		public String toString() {
			return getType().getLabel() + " " + getName();
		}
	}
	
	public static class MeetingInterface implements IsSerializable {
		private ResourceInterface iLocation;
		private Long iMeetingId;
		private String iMeetingTime;
		private String iMeetingDate;
		private int iStartSlot;
		private int iEndSlot;
		private int iDayOfWeek;
		private int iDayOfYear;
		
		public MeetingInterface() {}
		
		public Long getId() { return iMeetingId; }
		public void setId(Long id) { iMeetingId = id; }
		public String getMeetingDate() { return iMeetingDate; }
		public void setMeetingDate(String date) { iMeetingDate = date; }
		public int getStartSlot() { return iStartSlot; }
		public void setStartSlot(int slot) { iStartSlot = slot; }
		public int getEndSlot() { return iEndSlot; }
		public void setEndSlot(int slot) { iEndSlot = slot; }
		public int getDayOfWeek() { return iDayOfWeek; }
		public void setDayOfWeek(int dayOfWeek) { iDayOfWeek = dayOfWeek; }
		public int getDayOfYear() { return iDayOfYear; }
		public void setDayOfYear(int dayOfYear) { iDayOfYear = dayOfYear; }
		public String getMeetingTime() { return iMeetingTime; }
		public void setMeetingTime(String time) { iMeetingTime = time; }	
		public ResourceInterface getLocation() { return iLocation; }
		public void setLocation(ResourceInterface resource) { iLocation = resource; }
	}

}
