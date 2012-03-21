/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class EventInterface implements Comparable<EventInterface>, IsSerializable {
	private Long iEventId;
	private String iEventName;
	private String iEventType;
	private TreeSet<MeetingInterface> iMeetings = null;
	private String iSponsor, iInstructor, iContact, iEmail;
	
	private List<String> iCourseNames = null;
	private String iInstruction = null;
	private Integer iInstructionType = null;
	private List<String> iExternalIds = null;
	
	public static enum ResourceType implements IsSerializable {
		ROOM("room", "Room Timetable", true),
		SUBJECT("subject", "Subject Timetable", true),
		CURRICULUM("curriculum", "Curriculum Timetable", true),
		DEPARTMENT("department", "Departmental Timetable", true),
		PERSON("person", "Personal Timetable", true),
		COURSE("course", "Course Timetable", false);
		
		private String iLabel;
		private String iPageTitle;
		private boolean iVisible;
		
		ResourceType(String label, String title, boolean visible) { iLabel = label; iPageTitle = title; iVisible = visible; }
		
		public String getLabel() { return iLabel; }
		public String getPageTitle() { return iPageTitle; }
		public boolean isVisible() { return iVisible; }
	}

	public EventInterface() {}
	
	public Long getId() { return iEventId; }
	public void setId(Long id) { iEventId = id; }
	public String getName() { return iEventName; }
	public void setName(String name) { iEventName = name; }
	public String getType() { return iEventType; }
	public void setType(String type) { iEventType = type; }
	public String getSponsor() { return iSponsor; }
	public void setSponsor(String sponsor) { iSponsor = sponsor; }
	public boolean hasSponsor() { return iSponsor != null && !iSponsor.isEmpty(); }
	public String getInstructor() { return iInstructor; }
	public void setInstructor(String instructor) { iInstructor = instructor; }
	public boolean hasInstructor() { return iInstructor != null && !iInstructor.isEmpty(); }
	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }
	public String getContact() { return iContact; }
	public void setContact(String contact) { iContact = contact; }
	public boolean hasContact() { return iContact != null && !iContact.isEmpty(); }
	public boolean hasMeetings() { return iMeetings != null && !iMeetings.isEmpty(); }
	public void addMeeting(MeetingInterface meeting) {
		if (iMeetings == null) iMeetings = new TreeSet<MeetingInterface>();
		iMeetings.add(meeting);
	}
	public TreeSet<MeetingInterface> getMeetings() { return iMeetings; }
	
	public boolean hasCourseNames() { return iCourseNames != null && !iCourseNames.isEmpty(); }
	public void addCourseName(String name) {
		if (iCourseNames == null) iCourseNames = new ArrayList<String>();
		iCourseNames.add(name);
	}
	public List<String> getCourseNames() {
		return iCourseNames;
	}
	public boolean hasInstruction() { return iInstruction != null && !iInstruction.isEmpty(); }
	public String getInstruction() { return iInstruction; }
	public void setInstruction(String instruction) { iInstruction = instruction; }
	public boolean hasInstructionType() { return iInstructionType != null; }
	public Integer getInstructionType() { return iInstructionType; }
	public void setInstructionType(Integer type) { iInstructionType = type; }
	public boolean hasExternalIds() { return iExternalIds != null && !iExternalIds.isEmpty(); }
	public List<String> getExternalIds() { return iExternalIds; }
	public void addExternalId(String externalId) {
		if (iExternalIds == null) iExternalIds = new ArrayList<String>();
		iExternalIds.add(externalId);
	}
	
	public int hashCode() { return getId().hashCode(); }
	public boolean equals(Object o) {
		if (o == null || !(o instanceof EventInterface)) return false;
		return getId().equals(((EventInterface)o).getId());
	}
	public int compareTo(EventInterface event) {
		int cmp = getType().compareTo(event.getType());
		if (cmp != 0) return cmp;
		if (hasInstructionType()) {
			cmp = getInstructionType().compareTo(event.getInstructionType());
			if (cmp != 0) return cmp;
		}
		cmp = getName().compareTo(event.getName());
		if (cmp != 0) return cmp;
		return getId().compareTo(event.getId());
	}
	
	public static class IdValueInterface implements IsSerializable {
		private String iId, iValue;
		private boolean iSelected = false;

		public IdValueInterface() {}
		public IdValueInterface(String id, String value) {
			iId = id; iValue = value;
		}
		public String getId() { return iId; }
		public void setId(String id) { iId = id; }
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }
		public boolean isSelected() { return iSelected; }
		public void setSelected(boolean selected) { iSelected = selected; }
	}
	
	public static class ResourceInterface implements IsSerializable, Comparable<ResourceInterface> {
		private ResourceType iResourceType;
		private Long iResourceId;
		private String iExternalId;
		private String iAbbreviation;
		private String iResourceName;
		private String iTitle;
		private Long iSessionId;
		private String iSessionName;
		private String iSessionAbbv;
		private List<WeekInterface> iWeeks = null;
		private String iCalendar;
		private String iHint = null;
		private Integer iSize = null;
		private Double iDistance = null;
		private String iRoomType = null;

		public ResourceInterface() {}
		
		public ResourceType getType() { return iResourceType; }
		public void setType(ResourceType type) { iResourceType = type; }
		public Long getId() { return iResourceId; }
		public void setId(Long id) { iResourceId = id; }
		public String getExternalId() { return iExternalId; }
		public void setExternalId(String id) { iExternalId = id; }
		public boolean hasAbbreviation() { return iAbbreviation != null && !iAbbreviation.isEmpty(); }
		public String getAbbreviation() { return iAbbreviation; }
		public void setAbbreviation(String abbv) { iAbbreviation = abbv; }
		public String getName() { return iResourceName; }
		public void setName(String name) { iResourceName = name; }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public String getSessionName() { return iSessionName; }
		public void setSessionName(String sessionName) { iSessionName = sessionName; }
		public String getSessionAbbv() { return iSessionAbbv; }
		public void setSessionAbbv(String sessionAbbv) { iSessionAbbv = sessionAbbv; }
		public boolean hasCalendar() { return iCalendar != null && !iCalendar.isEmpty(); }
		public String getCalendar() { return iCalendar; }
		public void setCalendar(String calendar) { iCalendar = calendar; }
		public boolean hasTitle() { return iTitle != null && !iTitle.isEmpty(); }
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		
		public void setSize(Integer size) { iSize = size; }
		public boolean hasSize() { return iSize != null; }
		public Integer getSize() { return iSize; }
		public void setDistance(Double distance) { iDistance = distance; }
		public boolean hasDistance() { return iDistance != null; }
		public Double getDistance() { return iDistance; }
		public void setRoomType(String type) { iRoomType = type; }
		public boolean hasRoomType() { return iRoomType != null; }
		public String getRoomType() { return iRoomType; }
		
		public String getHint() { return iHint; }
		public boolean hasHint() { return iHint != null && !iHint.isEmpty(); }
		public void setHint(String hint) { iHint = hint; }
		public String getNameWithHint() {
			if (iResourceName == null || iResourceName.isEmpty()) return "";
			if (iHint == null || iHint.isEmpty()) return iResourceName;
			return "<span onmouseover=\"showGwtHint(this, '" + iHint + "');\" onmouseout=\"hideGwtHint();\">" + iResourceName + "</span>";
		}
		
		public boolean hasWeeks() { return iWeeks != null && !iWeeks.isEmpty(); }
		public List<WeekInterface> getWeeks() { return iWeeks; }
		public void addWeek(WeekInterface week) {
			if (iWeeks == null) iWeeks = new ArrayList<WeekInterface>();
			iWeeks.add(week);
		}
		
		public String toString() {
			return getType().getLabel() + " " + getName();
		}
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ResourceInterface)) return false;
			return ((ResourceInterface)o).getId().equals(getId());
		}
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		public int compareTo(ResourceInterface r) {
			if (hasAbbreviation()) {
				int cmp = getAbbreviation().compareTo(r.getAbbreviation());
				if (cmp != 0) return cmp;
			}
			int cmp = getName().compareTo(r.getName());
			if (cmp != 0) return cmp;
			return getId().compareTo(r.getId());
		}
	}
	
	public static class WeekInterface implements IsSerializable {
		private int iDayOfYear;
		private List<String> iDayNames = new ArrayList<String>();
		
		public WeekInterface() {}
		
		public int getDayOfYear() { return iDayOfYear; }
		public void setDayOfYear(int dayOfYear) { iDayOfYear = dayOfYear; }
		
		public void addDayName(String name) { iDayNames.add(name); }
		public List<String> getDayNames() { return iDayNames; }
		
		public String getName() { return getDayNames().get(0) + " - " + getDayNames().get(getDayNames().size() - 1); }
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof WeekInterface)) return false;
			return getDayOfYear() == ((WeekInterface)o).getDayOfYear();
		}
	}
	
	public static class MeetingInterface implements Comparable<MeetingInterface>, IsSerializable {
		private ResourceInterface iLocation;
		private Long iMeetingId;
		private String iMeetingTime;
		private String iMeetingDate;
		private int iStartSlot;
		private int iEndSlot;
		private int iDayOfWeek;
		private int iDayOfYear;
		private boolean iPast;
		private Date iApprovalDate = null;
		private Long iStartTime, iStopTime;
		
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
		public String getLocationName() { return (iLocation == null ? "" : iLocation.getName()); }
		public String getLocationNameWithHint() {
			return (iLocation == null ? "" : iLocation.getNameWithHint());
		}
		public void setLocation(ResourceInterface resource) { iLocation = resource; }
		public boolean isPast() { return iPast; }
		public void setPast(boolean past) { iPast = past; }
		public boolean isApproved() { return iApprovalDate != null; }
		public Date getApprovalDate() { return iApprovalDate; }
		public void setApprovalDate(Date date) {  iApprovalDate = date; }
		
		public Long getStopTime() { return iStopTime; }
		public void setStopTime(Long stopTime) { iStopTime = stopTime; }
		public Long getStartTime() { return iStartTime; }
		public void setStartTime(Long startTime) { iStartTime = startTime; }
		
		public int compareTo(MeetingInterface meeting) {
			int cmp = new Integer(getDayOfYear()).compareTo(meeting.getDayOfYear());
			if (cmp != 0) return cmp;
			cmp = getLocationName().compareTo(meeting.getLocationName());
			if (cmp != 0) return cmp;
			return getId().compareTo(meeting.getId());
		}
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof MeetingInterface)) return false;
			return getId().equals(((MeetingInterface)o).getId());
		}
	}
	
    public static boolean equals(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }
	
	public static class MultiMeetingInterface implements Comparable<MultiMeetingInterface>, IsSerializable {
	    private TreeSet<MeetingInterface> iMeetings;
	    private boolean iPast = false;
	    
	    public MultiMeetingInterface(TreeSet<MeetingInterface> meetings, boolean past) {
	        iMeetings = meetings;
	        iPast = past;
	    }
	    
	    public boolean isPast() { return iPast; }
	    
	    public TreeSet<MeetingInterface> getMeetings() { return iMeetings; }

	    public int compareTo(MultiMeetingInterface m) {
	        return getMeetings().first().compareTo(m.getMeetings().first());
	    }
	    
	    public String getDays() {
	        return getDays(new String[] {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"},
	        		new String[] {"M", "T", "W", "Th", "F", "S", "Su"});
	    }
	    
	    public String getDays(String[] dayNames, String[] shortDyNames) {
	        int nrDays = 0;
	        int dayCode = 0;
	        for (MeetingInterface meeting : getMeetings()) {
	        	int dc = (1 << meeting.getDayOfWeek());
	            if ((dayCode & dc)==0) nrDays++;
	            dayCode |= dc;
	        }
	        String ret = "";
	        for (int i = 0; i < 7; i++) {
	        	if ((dayCode & (1 << i)) != 0)
	        		ret += (nrDays == 1 ? dayNames : shortDyNames)[i];
	        }
	        return ret;
	    }
	    
	    public String getMeetingTime() {
	    	return getDays() + " " + iMeetings.first().getMeetingTime();
	    }
	    
	    public String getMeetingDates() {
	    	if (iMeetings.size() == 1)
	    		return iMeetings.first().getMeetingDate();
	    	return iMeetings.first().getMeetingDate() + " - " + iMeetings.last().getMeetingDate();
	    }
	    
	    public String getLocationName() {
	    	return iMeetings.first().getLocationName();
	    }

	    public String getLocationNameWithHint() {
	    	return iMeetings.first().getLocationNameWithHint();
	    }
	    
	    public Date getApprovalDate() {
	    	return iMeetings.first().getApprovalDate();
	    }
	    
	    public boolean isApproved() {
	    	return iMeetings.first().isApproved();
	    }
	}
	
    public static TreeSet<MultiMeetingInterface> getMultiMeetings(Collection<MeetingInterface> meetings, boolean checkApproval, boolean checkPast) {
        TreeSet<MultiMeetingInterface> ret = new TreeSet<MultiMeetingInterface>();
        HashSet<MeetingInterface> meetingSet = new HashSet<MeetingInterface>(meetings);
        while (!meetingSet.isEmpty()) {
            MeetingInterface meeting = null;
            for (MeetingInterface m : meetingSet)
                if (meeting==null || meeting.compareTo(m) > 0)
                    meeting = m;
            meetingSet.remove(meeting);
            HashMap<Integer,MeetingInterface> similar = new HashMap<Integer, MeetingInterface>(); 
            TreeSet<Integer> dow = new TreeSet<Integer>(); dow.add(meeting.getDayOfWeek());
            for (MeetingInterface m : meetingSet) {
            	if (m.getMeetingTime().equals(meeting.getMeetingTime()) &&
            		m.getLocationName().equals(meeting.getLocationName()) &&
            		(!checkPast || m.isPast() == meeting.isPast()) && 
            		(!checkApproval ||( m.isApproved() == meeting.isApproved() && (!m.isApproved() || m.getApprovalDate().equals(meeting.getApprovalDate()))))) {
                    dow.add(m.getDayOfWeek());
                    similar.put(m.getDayOfYear(),m);
                }
            }
            TreeSet<MeetingInterface> multi = new TreeSet<MeetingInterface>(); multi.add(meeting);
            if (!similar.isEmpty()) {
            	int w = meeting.getDayOfWeek();
            	int y = meeting.getDayOfYear();
            	while (true) {
            		do {
            			y ++;
            			w = (w + 1) % 7;
            		} while (!dow.contains(w));
            		MeetingInterface m = similar.get(y);
            		if (m == null) break;
            		multi.add(m);
            		meetingSet.remove(m);
            	}
            }
            ret.add(new MultiMeetingInterface(multi, meeting.isPast()));
        }
        return ret;
    }

}
