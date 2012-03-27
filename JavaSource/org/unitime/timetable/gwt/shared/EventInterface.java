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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.command.client.GwtRpcImplementedBy;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;

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
	private boolean iCanView = false, iCanEdit = false;
	
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
	
	public boolean isCanView() { return iCanView; }
	public void setCanView(boolean canView) { iCanView = canView; }
	public boolean isCanEdit() { return iCanEdit; }
	public void setCanEdit(boolean canEdit) { iCanEdit = canEdit; }
	
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
    
	public static class SelectionInterface implements IsSerializable {
		private Set<Integer> iDays = new TreeSet<Integer>();
		private int iStartSlot, iLength;
		private ResourceInterface iLocation;
		
		public SelectionInterface() {}
		
		public Set<Integer> getDays() { return iDays; }
		public void addDay(int day) { iDays.add(day); }
		
		public int getStartSlot() { return iStartSlot; }
		public void setStartSlot(int startSlot) { iStartSlot = startSlot; }
		
		public int getLength() { return iLength; }
		public void setLength(int length) { iLength = length; }
		
		public ResourceInterface getLocation() { return iLocation; }
		public void setLocation(ResourceInterface location) { iLocation = location; }
	}
	
	public static abstract class FilterRpcRequest implements GwtRpcRequest<FilterRpcResponse> {
		public static enum Command implements IsSerializable {
			LOAD,
			SUGGESTIONS,
			ENUMERATE,
		}
		
		private Command iCommand;
		private Long iSessionId;
		private String iText;
		private HashMap<String, Set<String>> iOptions;
		
		public FilterRpcRequest() {}
		
		public Command getCommand() { return iCommand; }
		public void setCommand(Command command) { iCommand = command; }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public String getText() { return iText; }
		public void setText(String text) { iText = text; }
		public Set<String> getOptions(String command) {
			return (iOptions == null ? null : iOptions.get(command));
		}
		public boolean hasOption(String command) {
			Set<String> options = getOptions(command);
			return (options != null && options.size() == 1);
		}
		public String getOption(String command) {
			Set<String> options = getOptions(command);
			return (options == null || options.isEmpty() ? null : options.iterator().next());
		}
		public Map<String, Set<String>> getOptions() {
			return iOptions;
		}
		public boolean hasOptions(String command) {
			Set<String> options = getOptions(command);
			return (options != null && !options.isEmpty());
		}
		public void addOption(String command, String value) {
			if (iOptions == null) iOptions = new HashMap<String, Set<String>>();
			Set<String> options = iOptions.get(command);
			if (options == null) {
				options = new HashSet<String>();
				iOptions.put(command, options);
			}
			options.add(value);
		}
		
		public void setOption(String command, String value) {
			if (iOptions == null) iOptions = new HashMap<String, Set<String>>();
			Set<String> options = iOptions.get(command);
			if (options == null) {
				options = new HashSet<String>();
				iOptions.put(command, options);
			} else {
				options.clear();
			}
			options.add(value);
		}
		
		@Override
		public String toString() { return (getCommand() == null ? "NULL" : getCommand().name()) + "(" + getSessionId() + "," + iOptions + "," + getText() + ")"; }
	}
	
	@GwtRpcImplementedBy("org.unitime.timetable.events.EventFilterBackend")
	public static class EventFilterRpcRequest extends FilterRpcRequest {
		public EventFilterRpcRequest() {}
	}
	
	@GwtRpcImplementedBy("org.unitime.timetable.events.RoomFilterBackend")
	public static class RoomFilterRpcRequest extends FilterRpcRequest {
		public RoomFilterRpcRequest() {}
	}
	
	public static class FilterRpcResponse implements GwtRpcResponse {
		private HashMap<String, ArrayList<Entity>> iEntities = null;
		
		public FilterRpcResponse() {}
		
		public boolean hasEntities(String type) {
			List<Entity> entities = getEntities(type);
			return entities != null && !entities.isEmpty();
		}
		
		public List<Entity> getEntities(String type) {
			return (iEntities == null ? null : iEntities.get(type));
		}
		
		public void add(String type, Entity entity) {
			if (iEntities == null) iEntities = new HashMap<String, ArrayList<Entity>>();
			ArrayList<Entity> entities = iEntities.get(type);
			if (entities == null) {
				entities = new ArrayList<Entity>();
				iEntities.put(type, entities);
			}
			entities.add(entity);
		}
		
		public void add(String type, Collection<Entity> entity) {
			if (iEntities == null) iEntities = new HashMap<String, ArrayList<Entity>>();
			ArrayList<Entity> entities = iEntities.get(type);
			if (entities == null) {
				entities = new ArrayList<Entity>(entity);
				iEntities.put(type, entities);
			} else {
				entities.addAll(entity);
			}
		}
		
		public void addResult(Entity entity) { add("results", entity); }
		public boolean hasResults() { return hasEntities("results"); }
		public List<Entity> getResults() { return getEntities("results"); }
		
		public void addSuggestion(String message, String replacement, String hint) {
			add("suggestion", new Entity(0l, replacement, message, "hint", hint));
		}
		
		public boolean hasSuggestions() { return hasEntities("suggestion"); }
		
		public List<Entity> getSuggestions() { return getEntities("suggestion"); }
		
		public static class Entity implements IsSerializable, Comparable<Entity> {
			private Long iUniqueId;
			private String iAbbv, iName;
			private int iCount = 0;
			private HashMap<String, String> iParams;
			
			public Entity() {}
			
			public Entity(Long uniqueId, String abbv, String name, String... properties) {
				iUniqueId = uniqueId;
				iAbbv = abbv;
				iName = name;
				for (int i = 0; i + 1 < properties.length; i += 2)
					if (properties[i + 1] != null)
						setProperty(properties[i], properties[i + 1]);
			}
			
			public Long getUniqueId() { return iUniqueId; }
			public String getAbbreviation() { return iAbbv; }
			public String getName() { return iName; }
			public int getCount() { return iCount; }
			public void setCount(int count) { iCount = count; }
			public void incCount() { iCount ++; }
			
			public void setProperty(String property, String value) {
				if (iParams == null) iParams = new HashMap<String, String>();
				iParams.put(property, value);
			}
			
			public String getProperty(String property, String defaultValue) {
				String value = (iParams == null ? null : iParams.get(property));
				return (value == null ? defaultValue : value);
			}
			
			public int hasCode() { return getUniqueId().hashCode(); }
			public boolean equals(Object o) {
				if (o == null || !(o instanceof Entity)) return false;
				Entity e = (Entity)o;
				return getUniqueId().equals(e.getUniqueId()) && getName().equals(e.getName());
			}
			public int compareTo(Entity e) {
				if (getUniqueId() < 0) {
					return (e.getUniqueId() >= 0 ? -1 : e.getUniqueId().compareTo(getUniqueId()));
				} else if (e.getUniqueId() < 0) return 1;
				return getName().compareToIgnoreCase(e.getName());
			}
			public String toString() { return getName(); }
		}
		
		public String toString() { return (iEntities == null ? "null" : iEntities.toString()); }
	}
	
	@GwtRpcImplementedBy("org.unitime.timetable.events.ResourceLookupBackend")
	public static class ResourceLookupRpcRequest implements GwtRpcRequest<GwtRpcResponseList<ResourceInterface>> {
		private Long iSessionId;
		private ResourceType iResourceType;
		private String iName;
		private Integer iLimit;
		
		public ResourceLookupRpcRequest() {}
		
		public Long getSessionId() {  return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		
		public ResourceType getResourceType() { return iResourceType; }
		public void setResourceType(ResourceType resourceType) { iResourceType = resourceType; }
		
		public String getName() { return iName; }
		public boolean hasName() { return iName != null && !iName.isEmpty(); }
		public void setName(String name) { iName = name; }
		
		public boolean hasLimit() { return iLimit != null && iLimit > 0; }
		public Integer getLimit() { return iLimit; }
		public void setLimit(Integer limit) { iLimit = limit; }
		
		public String toString() { return getResourceType().getLabel() + (getName() == null || getName().isEmpty() ? "" : "{" + getName() + "}"); }
		
		public static ResourceLookupRpcRequest findResource(Long sessionId, ResourceType type, String name) {
			ResourceLookupRpcRequest request = new ResourceLookupRpcRequest();
			request.setSessionId(sessionId);
			request.setLimit(1);
			request.setResourceType(type);
			request.setName(name);
			return request;
		}
		
		public static ResourceLookupRpcRequest findResources(Long sessionId, ResourceType type, String name, int limit) {
			ResourceLookupRpcRequest request = new ResourceLookupRpcRequest();
			request.setSessionId(sessionId);
			request.setLimit(limit);
			request.setResourceType(type);
			request.setName(name);
			return request;
		}
	}
	
	@GwtRpcImplementedBy("org.unitime.timetable.events.EventLookupBackend")
	public static class EventLookupRpcRequest implements GwtRpcRequest<GwtRpcResponseList<EventInterface>> {
		private Long iSessionId;
		private ResourceType iResourceType;
		private Long iResourceId;
		private String iResourceExternalId;
		private FilterRpcRequest iEventFilter, iRoomFilter;
		private int iLimit = -1;
		
		public EventLookupRpcRequest() {}
		
		public Long getSessionId() {  return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		
		public ResourceType getResourceType() { return iResourceType; }
		public void setResourceType(ResourceType resourceType) { iResourceType = resourceType; }

		public Long getResourceId() { return iResourceId; }
		public void setResourceId(Long resourceId) { iResourceId = resourceId; }
		
		public boolean hasResourceExternalId() { return iResourceExternalId != null && !iResourceExternalId.isEmpty(); }
		public String getResourceExternalId() { return iResourceExternalId; }
		public void setResourceExternalId(String resourceExternalId) { iResourceExternalId = resourceExternalId; }
		
		public FilterRpcRequest getEventFilter() { return iEventFilter; }
		public void setEventFilter(FilterRpcRequest eventFilter) { iEventFilter = eventFilter; }

		public FilterRpcRequest getRoomFilter() { return iRoomFilter; }
		public void setRoomFilter(FilterRpcRequest roomFilter) { iRoomFilter = roomFilter; }
		
		public boolean hasLimit() { return iLimit > 0; }
		public int getLimit() { return iLimit; }
		public void setLimit(int limit) { iLimit = limit; }
		
		public static EventLookupRpcRequest findEvents(Long sessionId, ResourceInterface resource, FilterRpcRequest eventFilter, FilterRpcRequest roomFilter, int limit) {
			EventLookupRpcRequest request = new EventLookupRpcRequest();
			request.setSessionId(sessionId);
			request.setResourceType(resource.getType());
			request.setResourceId(resource.getId());
			request.setResourceExternalId(resource.getExternalId());
			request.setEventFilter(eventFilter);
			request.setRoomFilter(roomFilter);
			request.setLimit(limit);
			return request;
		}
	}
	
	@GwtRpcImplementedBy("org.unitime.timetable.events.QueryEncoderBackend")
	public static class EncodeQueryRpcRequest implements GwtRpcRequest<EncodeQueryRpcResponse> {
		private String iQuery;
		
		public EncodeQueryRpcRequest() {}
		public EncodeQueryRpcRequest(String query) { iQuery = query; }

		public String getQuery() { return iQuery; }
		public void setQuery(String query) { iQuery = query; }
		
		public static EncodeQueryRpcRequest encode(String query) {
			return new EncodeQueryRpcRequest(query); 
		}
	}
	
	public static class EncodeQueryRpcResponse implements GwtRpcResponse {
		private String iQuery;
		
		public EncodeQueryRpcResponse() {}
		public EncodeQueryRpcResponse(String query) { iQuery = query; }

		public String getQuery() { return iQuery; }
		public void setQuery(String query) { iQuery = query; }
	}
	
	@GwtRpcImplementedBy("org.unitime.timetable.events.EventPropertiesBackend")
	public static class EventPropertiesRpcRequest implements GwtRpcRequest<EventPropertiesRpcResponse> {
		
		public EventPropertiesRpcRequest() {}
		
		public static EventPropertiesRpcRequest requestEventProperties() {
			return new EventPropertiesRpcRequest();
		}
	}
	
	public static class EventPropertiesRpcResponse implements GwtRpcResponse {
		private boolean iCanLookupPeople = false;
	
		public EventPropertiesRpcResponse() {}
		
		public boolean isCanLookupPeople() { return iCanLookupPeople; }
		public void setCanLookupPeople(boolean canLookupPeople) { iCanLookupPeople = canLookupPeople; }
	}
	
}
