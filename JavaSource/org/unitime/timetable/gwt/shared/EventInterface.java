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

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class EventInterface implements Comparable<EventInterface>, IsSerializable, GwtRpcResponse {
	private Long iEventId;
	private String iEventName;
	private EventType iEventType;
	private String iEventEmail;
	private TreeSet<MeetingInterface> iMeetings = null;
	private ContactInterface iContact;
	private SponsoringOrganizationInterface iSponsor;
	private Set<ContactInterface> iInstructors;
	private List<ContactInterface> iAdditionalContacts;
	private String iLastChange = null;
	private TreeSet<NoteInterface> iNotes;
	private Date iExpirationDate = null;
	
	private List<String> iCourseNames = null;
	private List<String> iCourseTitles = null;
	private String iInstruction = null;
	private Integer iInstructionType = null, iMaxCapacity = null, iEnrollment;
	private boolean iReqAttendance = false;
	private List<String> iExternalIds = null;
	private String iSectionNumber = null;
	private boolean iCanView = false, iCanEdit = false;
	private List<RelatedObjectInterface> iRelatedObjects = null;
	private Set<EventInterface> iConflicts;
	private String iMessage = null;
	
	public static enum ResourceType implements IsSerializable {
		ROOM("room", true),
		SUBJECT("subject", true),
		CURRICULUM("curriculum", true),
		DEPARTMENT("department", true),
		PERSON("person", true),
		COURSE("course", false);
		
		private String iLabel;
		private boolean iVisible;
		
		ResourceType(String label, boolean visible) { iLabel = label; iVisible = visible; }
		
		public String getLabel() { return iLabel; }
		public String getPageTitle(GwtConstants constants) {
			return constants.resourceType()[ordinal()];
		}
		public String getName(GwtConstants constants) {
			return constants.resourceName()[ordinal()];
		}
		public boolean isVisible() { return iVisible; }
	}
	
	public static enum EventType implements IsSerializable {
		Class,
		FinalExam,
		MidtermExam,
		Course,
		Special,
		Unavailabile,
		Message,
		;
		
		public String getAbbreviation(GwtConstants constants) { return constants.eventTypeAbbv()[ordinal()]; }
		public String getName(GwtConstants constants) { return constants.eventTypeName()[ordinal()]; }
		public int getType() { return ordinal(); }
		public String toString() { return name(); }
	}
	
	public static enum ApprovalStatus implements IsSerializable {
		Pending,
		Approved,
		Rejected,
		Cancelled,
		Deleted,
		;
		
		public String getName(GwtConstants constants) { return constants.eventApprovalStatus()[ordinal()]; }
		public int getType() { return ordinal(); }
		public String toString() { return name(); }
	}

	public EventInterface() {}
	
	public Long getId() { return iEventId; }
	public void setId(Long id) { iEventId = id; }
	public String getName() { return iEventName; }
	public void setName(String name) { iEventName = name; }
	public EventType getType() { return iEventType; }
	public void setType(EventType type) { iEventType = type; }
	public SponsoringOrganizationInterface getSponsor() { return iSponsor; }
	public void setSponsor(SponsoringOrganizationInterface sponsor) { iSponsor = sponsor; }
	public boolean hasSponsor() { return iSponsor != null; }

	public String getEmail() { return iEventEmail; }
	public boolean hasEmail() { return iEventEmail != null && !iEventEmail.isEmpty(); }
	public void setEmail(String email) { iEventEmail = email; }
	
	public TreeSet<NoteInterface> getNotes() { return iNotes; }
	public boolean hasNotes() { return iNotes != null && !iNotes.isEmpty(); }
	public void addNote(NoteInterface note) {
		if (iNotes == null) iNotes = new TreeSet<NoteInterface>();
		iNotes.add(note);
	}
	public boolean hasEventNote() {
		return hasNotes() && !getEventNote().isEmpty();
	}
	public String getEventNote() {
		String note = "";
		if (hasNotes())
			for (NoteInterface n: getNotes()) {
				if (n.getNote() != null && !n.getNote().isEmpty()) {
					if (!note.isEmpty()) note += "\n";
					note += n.getNote();
				}
			}
		return note;
	}

	public String getLastChange() { return iLastChange; }
	public boolean hasLastChange() { return iLastChange != null && !iLastChange.isEmpty(); }
	public void setLastChange(String lastChange) { iLastChange = lastChange; }

	public boolean hasMaxCapacity() { return iMaxCapacity != null; }
	public Integer getMaxCapacity() { return iMaxCapacity; }
	public void setMaxCapacity(Integer maxCapacity) { iMaxCapacity = maxCapacity; }
	
	public boolean hasRequiredAttendance() { return iReqAttendance; }
	public void setRequiredAttendance(boolean reqAttendance) { iReqAttendance = reqAttendance; }

	public boolean hasEnrollment() { return iEnrollment != null; }
	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }
	
	public Date getExpirationDate() { return iExpirationDate; }
	public void setExpirationDate(Date expirationDate) { iExpirationDate = expirationDate; }
	public boolean hasExpirationDate() { return iExpirationDate != null; }
	public boolean hasPendingMeetings() {
		if (iMeetings == null) return false;
		for (MeetingInterface meeting: iMeetings) {
			if (meeting.getApprovalStatus() == ApprovalStatus.Pending)
				return true;
		}
		return false;
	}

	public Set<ContactInterface> getInstructors() { return iInstructors; }
	public void addInstructor(ContactInterface instructor) {
		if (iInstructors == null) iInstructors = new TreeSet<ContactInterface>();
		iInstructors.add(instructor);
	}
	public String getInstructorNames(String separator, GwtMessages messages) { 
		if (!hasInstructors()) return "";
		String ret = "";
		for (ContactInterface instructor: getInstructors()) {
			ret += (ret.isEmpty() ? "" : separator) + instructor.getName(messages);
		}
		return ret;
	}
	public String getInstructorEmails(String separator) { 
		if (!hasInstructors()) return "";
		String ret = "";
		for (ContactInterface instructor: getInstructors()) {
			ret += (ret.isEmpty() ? "" : separator) + (instructor.getEmail() == null ? "" : instructor.getEmail());
		}
		return ret;
	}
	public boolean hasInstructors() { return iInstructors != null && !iInstructors.isEmpty(); }
	
	public ContactInterface getContact() { return iContact; }
	public void setContact(ContactInterface contact) { iContact = contact; }
	public boolean hasContact() { return iContact != null; }
	
	public List<ContactInterface> getAdditionalContacts() { return iAdditionalContacts; }
	public void addAdditionalContact(ContactInterface contact) {
		if (iAdditionalContacts == null) iAdditionalContacts = new ArrayList<ContactInterface>();
		iAdditionalContacts.add(contact);
	}
	public String getAdditionalContactNames(String separator, GwtMessages messages) { 
		if (!hasAdditionalContacts()) return "";
		String ret = "";
		for (ContactInterface contact: getAdditionalContacts()) {
			ret += (ret.isEmpty() ? "" : separator) + contact.getName(messages);
		}
		return ret;
	}
	public boolean hasAdditionalContacts() { return iAdditionalContacts != null && !iAdditionalContacts.isEmpty(); }
	
	public boolean hasMeetings() { return iMeetings != null && !iMeetings.isEmpty(); }
	public void addMeeting(MeetingInterface meeting) {
		if (iMeetings == null) iMeetings = new TreeSet<MeetingInterface>();
		iMeetings.add(meeting);
	}
	public TreeSet<MeetingInterface> getMeetings() { return iMeetings; }
	public void setMeetings(TreeSet<MeetingInterface> meetings) { iMeetings = meetings; }
	
	public boolean hasCourseNames() { return iCourseNames != null && !iCourseNames.isEmpty(); }
	public void addCourseName(String name) {
		if (iCourseNames == null) iCourseNames = new ArrayList<String>();
		iCourseNames.add(name);
	}
	public List<String> getCourseNames() {
		return iCourseNames;
	}
	public boolean hasCourseTitles() { return iCourseTitles != null && !iCourseTitles.isEmpty(); }
	public void addCourseTitle(String title) {
		if (iCourseTitles == null) iCourseTitles = new ArrayList<String>();
		iCourseTitles.add(title);
	}
	public List<String> getCourseTitles() {
		return iCourseTitles;
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
		if (!iExternalIds.contains(externalId))
			iExternalIds.add(externalId);
	}

	public boolean hasSectionNumber() { return iSectionNumber != null && !iSectionNumber.isEmpty(); }
	public String getSectionNumber() { return iSectionNumber; }
	public void setSectionNumber(String sectionNumber) { iSectionNumber = sectionNumber; }
	
	public boolean hasRelatedObjects() { return iRelatedObjects != null && !iRelatedObjects.isEmpty(); }
	public void addRelatedObject(RelatedObjectInterface relatedObject) {
		if (iRelatedObjects == null) iRelatedObjects = new ArrayList<RelatedObjectInterface>();
		iRelatedObjects.add(relatedObject);
	}
	public List<RelatedObjectInterface> getRelatedObjects() { return iRelatedObjects; }
	
	public boolean isCanView() { return iCanView; }
	public void setCanView(boolean canView) { iCanView = canView; }
	
	public boolean isCanEdit() { return iCanEdit; }
	public void setCanEdit(boolean canEdit) { iCanEdit = canEdit; }

	public boolean hasConflicts() { return iConflicts != null && !iConflicts.isEmpty(); }
	public void addConflict(EventInterface conflict) {
		if (iConflicts == null) iConflicts = new TreeSet<EventInterface>();
		iConflicts.add(conflict);
	}
	public Set<EventInterface> getConflicts() { return iConflicts; }
	public void setConflicts(Set<EventInterface> conflicts) {
		iConflicts = conflicts;
	}
	
	public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }
	
	public int hashCode() { return getId().hashCode(); }
	public boolean equals(Object o) {
		if (o == null || !(o instanceof EventInterface)) return false;
		return getId().equals(((EventInterface)o).getId());
	}
	public int compareTo(EventInterface event) {
		int cmp = getType().compareTo(event.getType());
		if (cmp != 0) return cmp;
		if (hasInstructionType() && event.hasInstructionType()) {
			cmp = getInstructionType().compareTo(event.getInstructionType());
			if (cmp != 0) return cmp;
		}
		cmp = getName().compareTo(event.getName());
		if (cmp != 0) return cmp;
		return getId().compareTo(event.getId());
	}
	public String toString() {
		return getType() + ": " + getName();
	}
	
	public boolean inConflict(MeetingInterface meeting) {
		if (hasMeetings())
			for (MeetingInterface m: getMeetings())
				if (m.inConflict(meeting)) return true;
		return false;
	}
	
	public boolean inConflict(EventInterface event) {
		if (event.hasMeetings())
			for (MeetingInterface meeting: event.getMeetings())
				if (inConflict(meeting)) return true;
		return false;
	}
	
	public EventInterface createConflictingEvent(EventInterface event) {
		EventInterface conflict = new EventInterface();
		conflict.setId(event.getId());
		conflict.setCanView(event.isCanView());
		conflict.setContact(event.getContact());
		conflict.setName(event.getName());
		conflict.setType(event.getType());
		conflict.setSponsor(event.getSponsor());
		conflict.setEnrollment(event.getEnrollment());
		conflict.setMaxCapacity(event.getMaxCapacity());
		conflict.setInstruction(event.getInstruction());
		conflict.setInstructionType(event.getInstructionType());
		conflict.setSectionNumber(event.getSectionNumber());
		if (event.hasCourseNames())
			for (String courseName: event.getCourseNames())
				conflict.addCourseName(courseName);
		if (event.hasCourseTitles())
			for (String courseTitle: event.getCourseTitles())
				conflict.addCourseTitle(courseTitle);
		if (event.hasExternalIds())
			for (String extId: event.getExternalIds())
				conflict.addExternalId(extId);
		if (event.hasInstructors())
			for (ContactInterface instructor: event.getInstructors())
				conflict.addInstructor(instructor);
		if (event.hasMeetings())
			for (MeetingInterface m: event.getMeetings())
				if (inConflict(m)) conflict.addMeeting(m);
		return conflict;
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
		
		public String toString() { return getValue(); }
	}
	
	public static class ResourceInterface implements IsSerializable, Comparable<ResourceInterface> {
		private ResourceType iResourceType;
		private Long iResourceId;
		private String iExternalId;
		private String iAbbreviation;
		private String iResourceName;
		private String iTitle;
		private Integer iSize = null;
		private Double iDistance = null;
		private String iRoomType = null;
		private int iBreakTime = 0;
		private String iMessage = null;

		public ResourceInterface() {}
		public ResourceInterface(FilterRpcResponse.Entity room) {
			setType(ResourceType.ROOM);
			setId(room.getUniqueId());
			setAbbreviation(room.getAbbreviation());
			setName(room.getName());
			String capacity = room.getProperty("capacity", null);
			setSize(capacity == null ? null : Integer.valueOf(capacity));
			String distance = room.getProperty("distance", null);
			setDistance(distance == null ? null : Double.valueOf(distance));
			setRoomType(room.getProperty("type", null));
			setBreakTime(Integer.parseInt(room.getProperty("breakTime" ,"0")));
			setMessage(room.getProperty("message", null));
		}
		
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
		public boolean hasDistance() { return iDistance != null && Math.round(iDistance) > 0; }
		public Double getDistance() { return iDistance; }
		public void setRoomType(String type) { iRoomType = type; }
		public boolean hasRoomType() { return iRoomType != null; }
		public String getRoomType() { return iRoomType; }
		public int getBreakTime() { return iBreakTime; }
		public void setBreakTime(int breakTime) { iBreakTime = breakTime; }
		public void setMessage(String message) { iMessage = message; }
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public String getMessage() { return iMessage; }
		
		public String getNameWithHint() {
			if (iResourceName == null || iResourceName.isEmpty()) return "";
			return "<span onmouseover=\"showGwtRoomHint(this, '" + iResourceId + "', '', '" + (iDistance != null ? Math.round(iDistance) : "") + "');\" onmouseout=\"hideGwtRoomHint();\">" + iResourceName + "</span>";
		}
		
		public String getNameWithSizeAndHint() {
			if (iResourceName == null || iResourceName.isEmpty()) return "";
			return "<span onmouseover=\"showGwtRoomHint(this, '" + iResourceId + "', '', '" + (iDistance != null ? Math.round(iDistance) : "") + "');\" onmouseout=\"hideGwtRoomHint();\">" + iResourceName + (iSize != null && iSize > 0 ? " (" + iSize + ")" : "")+ "</span>";
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
	
	public static class DateInterface implements IsSerializable {
		String iLabel;
		int iMonth, iDay;
		
		public DateInterface() {}
		public DateInterface(String label, int month, int day) { iLabel = label; iMonth = month; iDay = day; }
		
		public int getMonth() { return iMonth; }
		public void setMonth(int month) { iMonth = month; }
		
		public int getDay() { return iDay; }
		public void setDay(int day) { iDay = day; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public String toString() { return getLabel(); }
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o instanceof String) return getLabel().equals((String)o);
			if (o instanceof DateInterface) {
				DateInterface d = (DateInterface)o;
				return getMonth() == d.getMonth() && getDay() == d.getDay();
			}
			return false;
		}
	}
	
	public static class WeekInterface implements IsSerializable {
		private int iDayOfYear;
		private List<DateInterface> iDayNames = new ArrayList<DateInterface>();
		
		public WeekInterface() {}
		
		public int getDayOfYear() { return iDayOfYear; }
		public void setDayOfYear(int dayOfYear) { iDayOfYear = dayOfYear; }
		
		public int getLastDayOfYear() {
			if (iDayNames.isEmpty()) return getDayOfYear() + 6;
			return Math.max(getDayOfYear() + 6, iDayNames.get(iDayNames.size() - 1).getDay());
		}
		
		public void addDayName(DateInterface name) { iDayNames.add(name); }
		public List<DateInterface> getDayNames() { return iDayNames; }
		
		public String getName() { return getDayNames().get(0) + " - " + getDayNames().get(getDayNames().size() - 1); }
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof WeekInterface)) return false;
			return getDayOfYear() == ((WeekInterface)o).getDayOfYear();
		}
		
		public String toString() { return getName(); }
	}
	
	public static class MeetingInterface implements Comparable<MeetingInterface>, IsSerializable {
		private ResourceInterface iLocation;
		private Long iMeetingId;
		private Date iMeetingDate;
		private int iStartSlot;
		private int iEndSlot;
		private int iStartOffset = 0, iEndOffset = 0;
		private int iDayOfWeek;
		private int iDayOfYear;
		private boolean iPast = false, iCanEdit = false, iCanDelete = false, iCanCancel = false, iCanApprove = false, iCanInquire = false;
		private Date iApprovalDate = null;
		private ApprovalStatus iApprovalStatus = ApprovalStatus.Pending;
		private Long iStartTime, iStopTime;
		private Set<MeetingConflictInterface> iConflicts;
		
		public MeetingInterface() {}
		
		public Long getId() { return iMeetingId; }
		public void setId(Long id) { iMeetingId = id; }
		public Date getMeetingDate() { return iMeetingDate; }
		public void setMeetingDate(Date date) { iMeetingDate = date; }
		public int getStartSlot() { return iStartSlot; }
		public void setStartSlot(int slot) { iStartSlot = slot; }
		public int getEndSlot() { return iEndSlot; }
		public void setEndSlot(int slot) { iEndSlot = slot; }
		public int getStartOffset() { return iStartOffset; }
		public void setStartOffset(int offset) { iStartOffset = offset; }
		public int getEndOffset() { return iEndOffset; }
		public void setEndOffset(int offset) { iEndOffset = offset; }
		public int getDayOfWeek() { return iDayOfWeek; }
		public void setDayOfWeek(int dayOfWeek) { iDayOfWeek = dayOfWeek; }
		public int getDayOfYear() { return iDayOfYear; }
		public void setDayOfYear(int dayOfYear) { iDayOfYear = dayOfYear; }
		public String getStartTime(GwtConstants constants, boolean useOffsets) {
			int min = 5 * iStartSlot + (useOffsets ? iStartOffset : 0);
			int h = min / 60;
	        int m = min % 60;
	        if (constants != null && min == 0)
	        	return constants.timeMidnight();
	        if (constants != null && min == 720)
	        	return constants.timeNoon();
	        if (constants == null || constants.useAmPm()) {
	        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
			} else {
				return h + ":" + (m < 10 ? "0" : "") + m;
			}
		}
		public String getEndTime(GwtConstants constants, boolean useOffsets) {
			int min = 5 * iEndSlot + (useOffsets ? iEndOffset : 0);
			int h = min / 60;
	        int m = min % 60;
	        if (constants != null && min == 720)
	        	return constants.timeNoon();
	        if (constants != null && min == 1440)
	        	return constants.timeMidnight();
	        if (constants == null || constants.useAmPm()) {
	        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
			} else {
				return h + ":" + (m < 10 ? "0" : "") + m;
			}
			
		}
		public String getMeetingTime(GwtConstants constants) {
			if (isArrangeHours()) return "";
			if (constants != null && iStartSlot == 0 && iStartOffset == 0 && iEndSlot == 288 && iEndOffset == 0)
				return constants.timeAllDay();
			return getStartTime(constants, true) + " - " + getEndTime(constants, true);
		}
		public String getAllocatedTime(GwtConstants constants) {
			if (isArrangeHours()) return "";
			if (constants != null && iStartSlot == 0 && iEndSlot == 288)
				return constants.timeAllDay();
			return getStartTime(constants, false) + " - " + getEndTime(constants, false);
		}
		public ResourceInterface getLocation() { return iLocation; }
		public boolean hasLocation() { return iLocation != null; }
		public String getLocationName() { return (iLocation == null ? "" : iLocation.getName()); }
		public String getLocationNameWithHint() {
			return (iLocation == null ? "" : iLocation.getNameWithHint());
		}
		public void setLocation(ResourceInterface resource) { iLocation = resource; }
		public boolean isPast() { return iPast; }
		public void setPast(boolean past) { iPast = past; }
		public boolean isCanEdit() { return iCanEdit; }
		public void setCanEdit(boolean canEdit) { iCanEdit = canEdit; }
		public boolean isCanDelete() { return iCanDelete || iMeetingId == null; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
		public boolean isCanCancel() { return iCanCancel; }
		public void setCanCancel(boolean canCancel) { iCanCancel = canCancel; }
		public boolean isCanApprove() { return iCanApprove; }
		public void setCanApprove(boolean canApprove) { iCanApprove = canApprove; }
		public boolean isApproved() { return iApprovalStatus == ApprovalStatus.Approved; }
		public boolean isCanInquire() { return iCanInquire; }
		public void setCanInquire(boolean canInquire) { iCanInquire = canInquire; }
		public ApprovalStatus getApprovalStatus() { return iApprovalStatus == null ? ApprovalStatus.Pending : iApprovalStatus; }
		public void setApprovalStatus(ApprovalStatus status) { iApprovalStatus = status; }
		public void setApprovalStatus(Integer status) { iApprovalStatus = (status == null ? ApprovalStatus.Pending : ApprovalStatus.values()[status]); }
		public Date getApprovalDate() { return iApprovalDate; }
		public void setApprovalDate(Date date) {  iApprovalDate = date; }
		public boolean isArrangeHours() { return iMeetingDate == null; }
		public boolean isAllDay() { return iStartSlot == 0 && iEndSlot == 288; }
		
		public Long getStopTime() { return iStopTime; }
		public void setStopTime(Long stopTime) { iStopTime = stopTime; }
		public Long getStartTime() { return iStartTime; }
		public void setStartTime(Long startTime) { iStartTime = startTime; }
		
		public boolean hasConflicts() {
			return iConflicts != null && !iConflicts.isEmpty();
		}
		public boolean inConflict() {
			if (iConflicts == null) return false;
			for (MeetingConflictInterface conflict: iConflicts)
				if (conflict.getType() != EventType.Message) return true;
			return false;
		}
		public void addConflict(MeetingConflictInterface conflict) {
			if (iConflicts == null) iConflicts = new TreeSet<MeetingConflictInterface>();
			iConflicts.add(conflict);
		}
		public Set<MeetingConflictInterface> getConflicts() { return iConflicts; }
		public void setConflicts(Set<MeetingConflictInterface> conflicts) {
			iConflicts = conflicts;
		}
		
		public int compareTo(MeetingInterface meeting) {
			int cmp = new Integer(getDayOfYear()).compareTo(meeting.getDayOfYear());
			if (cmp != 0) return cmp;
			cmp = new Integer(getStartSlot()).compareTo(meeting.getStartSlot());
			if (cmp != 0) return cmp;
			cmp = getLocationName().compareTo(meeting.getLocationName());
			if (cmp != 0) return cmp;
			return (getId() == null ? meeting.getId() == null ? 0 : -1 : meeting.getId() == null ? 1 : getId().compareTo(meeting.getId()));
		}
		
		public int hashCode() {
			return (getId() == null ? toString().hashCode() : getId().hashCode());
		}
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof MeetingInterface)) return false;
			MeetingInterface m = (MeetingInterface)o;
			if (getId() != null && m.getId() != null) 
				return getId().equals(m.getId());
			return getDayOfYear() == m.getDayOfYear() && EventInterface.equals(getLocation(), m.getLocation()) && getStartSlot() == m.getStartSlot() && getEndSlot() == m.getEndSlot();
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public String toString() {
			return (getMeetingDate() == null ? "" : (1 + getMeetingDate().getMonth()) + "/" + getMeetingDate().getDate() + " ") +
					getAllocatedTime(null) + (getLocation() == null ? "" : " " + getLocationName());
		}
		
		public boolean inConflict(MeetingInterface meeting) {
			return getDayOfYear() == meeting.getDayOfYear() && 
					getStartSlot() < meeting.getEndSlot() && meeting.getStartSlot() < getEndSlot() &&
					getLocation() != null &&  getLocation().equals(meeting.getLocation());
		}
	}
	
	public static class MeetingConflictInterface extends MeetingInterface {
		private Long iEventId;
		private String iEventName;
		private EventType iEventType;
		private Integer iLimit, iEnrollment;
		private SponsoringOrganizationInterface iSponsor;
		private List<ContactInterface> iInstructors;
		
		public MeetingConflictInterface() {}
		
		public Long getEventId() { return iEventId; }
		public void setEventId(Long id) { iEventId = id; }
		public String getName() { return iEventName; }
		public void setName(String name) { iEventName = name; }
		public EventType getType() { return iEventType; }
		public void setType(EventType type) { iEventType = type; }
		
		public boolean hasLimit() { return iLimit != null; }
		public Integer getLimit() { return iLimit; }
		public void setLimit(Integer limit) { iLimit = limit; }

		public boolean hasEnrollment() { return iEnrollment != null; }
		public Integer getEnrollment() { return iEnrollment; }
		public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

		public SponsoringOrganizationInterface getSponsor() { return iSponsor; }
		public void setSponsor(SponsoringOrganizationInterface sponsor) { iSponsor = sponsor; }
		public boolean hasSponsor() { return iSponsor != null; }

		public List<ContactInterface> getInstructors() { return iInstructors; }
		public void addInstructor(ContactInterface instructor) {
			if (iInstructors == null) iInstructors = new ArrayList<ContactInterface>();
			iInstructors.add(instructor);
		}
		public String getInstructorNames(String separator, GwtMessages messages) { 
			if (!hasInstructors()) return "";
			String ret = "";
			for (ContactInterface instructor: getInstructors()) {
				ret += (ret.isEmpty() ? "" : separator) + instructor.getName(messages);
			}
			return ret;
		}
		public String getInstructorEmails(String separator) { 
			if (!hasInstructors()) return "";
			String ret = "";
			for (ContactInterface instructor: getInstructors()) {
				ret += (ret.isEmpty() ? "" : separator) + (instructor.getEmail() == null ? "" : instructor.getEmail());
			}
			return ret;
		}
		public boolean hasInstructors() { return iInstructors != null && !iInstructors.isEmpty(); }

		public int compareTo(MeetingInterface conflict) {
			int cmp = getType().compareTo(((MeetingConflictInterface)conflict).getType());
			if (cmp != 0) return cmp;
			cmp = new Integer(getDayOfYear()).compareTo(conflict.getDayOfYear());
			if (cmp != 0) return cmp;
			cmp = new Integer(getStartSlot()).compareTo(conflict.getStartSlot());
			if (cmp != 0) return cmp;
			cmp = new Integer(getEndSlot()).compareTo(conflict.getEndSlot());
			if (cmp != 0) return cmp;
			cmp = getName().compareTo(((MeetingConflictInterface)conflict).getName());
			if (cmp != 0) return cmp;
			return (getId() == null ? new Long(-1) : getId()).compareTo(conflict.getId() == null ? new Long(-1) : conflict.getId());
		}
		
		public String toString() {
			return getName() + " " + super.toString();
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
	        		new String[] {"M", "T", "W", "Th", "F", "S", "Su"}, "Daily");
	    }
	    
	    public String getDays(GwtConstants constants) {
	    	return getDays(constants.days(), constants.shortDays(), constants.daily());
	    }
	    
	    public String getDays(String[] dayNames, String[] shortDyNames, String daily) {
	    	if (isArrangeHours()) return "";
	        int nrDays = 0;
	        int dayCode = 0;
	        for (MeetingInterface meeting : getMeetings()) {
	        	if (meeting.getMeetingDate() == null) continue;
	        	int dc = (1 << meeting.getDayOfWeek());
	            if ((dayCode & dc)==0) nrDays++;
	            dayCode |= dc;
	        }
	        if (nrDays == 7) return daily;
	        String ret = "";
	        for (int i = 0; i < 7; i++) {
	        	if ((dayCode & (1 << i)) != 0)
	        		ret += (nrDays == 1 ? dayNames : shortDyNames)[i];
	        }
	        return ret;
	    }
	    
	    public boolean isArrangeHours() { return iMeetings.first().isArrangeHours(); }
	    
	    /*
	    public String getMeetingDates() {
	    	if (iMeetings.size() == 1)
	    		return iMeetings.first().getMeetingDate();
	    	return iMeetings.first().getMeetingDate() + " - " + iMeetings.last().getMeetingDate();
	    }
	    */
	    
	    public Date getFirstMeetingDate() {
	    	return iMeetings.first().getMeetingDate();
	    }
	    
	    public Date getLastMeetingDate() {
	    	return iMeetings.last().getMeetingDate();
	    }
	    
	    public int getNrMeetings() {
	    	return iMeetings.size();
	    }
	    
	    public String getLocationName() {
	    	return iMeetings.first().getLocationName();
	    }

	    public String getLocationNameWithHint() {
	    	return iMeetings.first().getLocationNameWithHint();
	    }
	    
	    public String getLocationCapacity() {
	    	return (iMeetings.first().getLocation() == null ? "" : iMeetings.first().getLocation().getSize() == null ? "" : iMeetings.first().getLocation().getSize().toString());
	    }

	    
	    public Date getApprovalDate() {
	    	Date date = null;
	    	for (MeetingInterface m: iMeetings) {
	    		if (m.getApprovalDate() == null) continue;
	    		if (date == null || date.after(m.getApprovalDate())) date = m.getApprovalDate();
	    	}
	    	return date;
	    }
	    
	    public ApprovalStatus getApprovalStatus() {
	    	return iMeetings.first().getApprovalStatus();
	    }
	}
	
	public static TreeSet<MultiMeetingInterface> getMultiMeetings(Collection<MeetingInterface> meetings, boolean checkPast) {
		return getMultiMeetings(meetings, checkPast, null, null);
	}
	
    public static TreeSet<MultiMeetingInterface> getMultiMeetings(Collection<MeetingInterface> meetings, boolean checkPast, DateFlagsProvider flags, EventType type) {
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
            SessionMonth.Flag flag = (flags == null ? null : flags.getDateFlag(type, meeting.getMeetingDate()));
            for (MeetingInterface m : meetingSet) {
            	if (m.getMeetingTime(null).equals(meeting.getMeetingTime(null)) &&
            		m.getLocationName().equals(meeting.getLocationName()) &&
            		(!checkPast || m.isPast() == meeting.isPast()) && 
            		(m.getApprovalStatus() == meeting.getApprovalStatus()) &&
            		(flags == null || flag == flags.getDateFlag(type, m.getMeetingDate()))) {
            		if (m.getDayOfYear() - meeting.getDayOfYear() < 7) dow.add(m.getDayOfWeek());
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
    
    public static class ContactInterface implements IsSerializable, Comparable<ContactInterface> {
    	private String iFirstName, iMiddleName, iLastName;
    	private String iExternalId, iEmail, iPhone;
    	
    	public ContactInterface() {}
    	
    	public ContactInterface(PersonInterface person) {
    		iFirstName = person.getFirstName();
    		iMiddleName = person.getMiddleName();
    		iLastName = person.getLastName();
    		iExternalId = person.getId();
    		iEmail = person.getEmail();
    		iPhone = person.getPhone();
    	}
    	
    	public void setFirstName(String name) { iFirstName = name; }
    	public boolean hasFirstName() { return iFirstName != null && !iFirstName.isEmpty(); }
    	public String getFirstName() { return iFirstName; }

    	public void setMiddleName(String name) { iMiddleName = name; }
    	public boolean hasMiddleName() { return iMiddleName != null && !iMiddleName.isEmpty(); }
    	public String getMiddleName() { return iMiddleName; }

    	public void setLastName(String name) { iLastName = name; }
    	public boolean hasLastName() { return iLastName != null && !iLastName.isEmpty(); }
    	public String getLastName() { return iLastName; }

    	public void setExternalId(String externalId) { iExternalId = externalId; }
    	public boolean hasExternalId() { return iExternalId != null && !iExternalId.isEmpty(); }
    	public String getExternalId() { return iExternalId; }

    	public void setEmail(String email) { iEmail = email; }
    	public boolean hasEmail() { return iEmail != null && !iEmail.isEmpty(); }
    	public String getEmail() { return iEmail; }

    	public void setPhone(String phone) { iPhone = phone; }
    	public boolean hasPhone() { return iPhone != null && !iPhone.isEmpty(); }
    	public String getPhone() { return iPhone; }
    	
    	public String getName(GwtMessages messages) {
    		if (messages == null) return toString();
    		return messages.formatName(
    				hasFirstName() ? getFirstName() : "",
    				hasMiddleName() ? getMiddleName() : "",
    				hasLastName() ? getLastName() : "");
    	}
    	
    	public String getShortName() {
            String name = "";
            if (hasFirstName())
            	name += getFirstName().substring(0, 1) + " ";
            if (hasMiddleName())
            	name += getMiddleName().substring(0, 1) + " ";
            if (hasLastName())
            	name += getLastName();
            return name.trim();
    	}
    	
    	public boolean equals(Object o) {
    		if (o == null || !(o instanceof ContactInterface)) return false;
    		if (getExternalId() != null)
    			return getExternalId().equals(((ContactInterface)o).getExternalId());
    		return toString().equals(((ContactInterface)o).toString());
    	}
    	
    	public String toString() { 
    		return (hasLastName() ? getLastName() : "") + (hasFirstName() || hasMiddleName() ?
    				", " + (hasFirstName() ? getFirstName() + (hasMiddleName() ? " " + getMiddleName() : "") : getMiddleName()) : "");
    	}

		@Override
		public int compareTo(ContactInterface c) {
			return toString().compareToIgnoreCase(c.toString());
		}
    }
    
    public static class SponsoringOrganizationInterface implements IsSerializable {
    	private String iName, iEmail;
    	private Long iUniqueId;
    	
    	public SponsoringOrganizationInterface() {}
    	
    	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
    	public Long getUniqueId() { return iUniqueId; }

    	public void setName(String name) { iName = name; }
    	public boolean hasName() { return iName != null && !iName.isEmpty(); }
    	public String getName() { return iName; }

    	public void setEmail(String email) { iEmail = email; }
    	public boolean hasEmail() { return iEmail != null && !iEmail.isEmpty(); }
    	public String getEmail() { return iEmail; }

    	public String toString() { return getName(); }
    }
    
    public static class NoteInterface implements IsSerializable, Comparable<NoteInterface> {
    	private Long iId = null;
    	private Date iDate;
    	private String iUser;
    	private NoteType iType;
    	private String iMeetings;
    	private String iNote;
    	private String iAttachment;
    	
    	public static enum NoteType {
    		Create("Create"),
    		AddMeetings("Update"),
    		Approve("Approve"),
    		Reject("Reject"),
    		Delete("Delete"),
    		Edit("Edit"),
    		Inquire("Inquire"),
    		Cancel("Cancel");
    		
    		private String iName;
    		
    		NoteType(String name) { iName = name; }
    		
    		public String getName() { return iName; }
    		public String toString() { return iName; }
    	}
    	
    	public NoteInterface() {}
    	    	
    	public Long getId() { return iId; }
    	public void setId(Long id) { iId = id; }

    	public Date getDate() { return iDate; }
    	public void setDate(Date date) { iDate = date; }
    	
    	public String getUser() { return iUser; }
    	public void setUser(String user) { iUser = user; }
    	
    	public NoteType getType() { return iType; }
    	public void setType(NoteType type) { iType = type; }
    	
    	public String getMeetings() { return iMeetings; }
    	public void setMeetings(String meetings) { iMeetings = meetings; }
    	
    	public String getNote() { return iNote; }
    	public void setNote(String note) { iNote = note; }
    	
    	public boolean hasAttachment() { return iAttachment != null && !iAttachment.isEmpty(); }
    	public String getAttachment() { return iAttachment; }
    	public void setAttachment(String attachment) { iAttachment = attachment; }
    	
		@Override
		public int compareTo(NoteInterface note) {
			int cmp = getDate() == null ? 1 : note.getDate() == null ? -1 : getDate().compareTo(note.getDate());
			if (cmp != 0) return cmp;
			return getType().compareTo(note.getType());
		}
    }
    
    public static class RelatedObjectInterface implements IsSerializable {
    	private Long iUniqueId;
    	private RelatedObjectType iType;
    	private List<String> iCourseNames = null;
    	private List<String> iCourseTitles = null;
    	private String iName;
    	private String iNote;
    	private String iInstruction = null;
    	private Integer iInstructionType = null, iMaxCapacity = null;
    	private List<ContactInterface> iInstructors;
		private Set<ResourceInterface> iLocations = new TreeSet<ResourceInterface>();
		private String iDate, iTime, iConflicts;
    	private List<String> iExternalIds;
    	private String iSectionNumber = null;
    	private long[] iSelection = null;
    	private String iDetailPage = null;

    	public static enum RelatedObjectType {
    		Offering,
    		Course,
    		Config,
    		Class,
    		Examination
    	}
    	
    	public RelatedObjectInterface() {}
    	
    	public Long getUniqueId() { return iUniqueId; }
    	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
    	public RelatedObjectType getType() { return iType; }
    	public void setType(RelatedObjectType type) { iType = type; }
    	public String getName() { return iName; }
    	public void setName(String name) { iName = name; }
    	public boolean hasNote() { return iNote != null && !iNote.isEmpty(); }
    	public String getNote() { return iNote; }
    	public void setNote(String note) { iNote = note; }
    	
    	public boolean hasDetailPage() { return iDetailPage != null && !iDetailPage.isEmpty(); }
    	public String getDetailPage() { return iDetailPage; }
    	public void setDetailPage(String page) { iDetailPage = page; }
    	
    	public boolean hasCourseNames() { return iCourseNames != null && !iCourseNames.isEmpty(); }
    	public void addCourseName(String name) {
    		if (iCourseNames == null) iCourseNames = new ArrayList<String>();
    		iCourseNames.add(name);
    	}
    	public List<String> getCourseNames() {
    		return iCourseNames;
    	}
    	
    	public boolean hasCourseTitles() { return iCourseTitles != null && !iCourseTitles.isEmpty(); }
    	public void addCourseTitle(String title) {
    		if (iCourseTitles == null) iCourseTitles = new ArrayList<String>();
    		iCourseTitles.add(title);
    	}
    	public List<String> getCourseTitles() {
    		return iCourseTitles;
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
    		if (!iExternalIds.contains(externalId))
    			iExternalIds.add(externalId);
    	}

    	public boolean hasSectionNumber() { return iSectionNumber != null && !iSectionNumber.isEmpty(); }
    	public String getSectionNumber() { return iSectionNumber; }
    	public void setSectionNumber(String sectionNumber) { iSectionNumber = sectionNumber; }
    	
		public Set<ResourceInterface> getLocations() { return iLocations; }
		public boolean hasLocations() { return iLocations != null && !iLocations.isEmpty(); }
		public void addLocation(ResourceInterface location) { iLocations.add(location); }

		public List<ContactInterface> getInstructors() { return iInstructors; }
		public void addInstructor(ContactInterface instructor) {
			if (iInstructors == null) iInstructors = new ArrayList<ContactInterface>();
			iInstructors.add(instructor);
		}
		public String getInstructorNames(String separator, GwtMessages messages) { 
			if (!hasInstructors()) return "";
			String ret = "";
			for (ContactInterface instructor: getInstructors()) {
				ret += (ret.isEmpty() ? "" : separator) + instructor.getName(messages);
			}
			return ret;
		}
		public boolean hasInstructors() { return iInstructors != null && !iInstructors.isEmpty(); }

		public boolean hasMaxCapacity() { return iMaxCapacity != null; }
		public Integer getMaxCapacity() { return iMaxCapacity; }
		public void setMaxCapacity(Integer maxCapacity) { iMaxCapacity = maxCapacity; }
		
		public String getTime() { return iTime; }
		public boolean hasTime() { return iTime != null && !iTime.isEmpty(); }
		public void setTime(String time) { iTime = time; }
		
		public String getDate() { return iDate; }
		public boolean hasDate() { return iDate != null && !iDate.isEmpty(); }
		public void setDate(String date) { iDate = date; }
		
		public String getConflicts() { return iConflicts; }
		public boolean hasConflicts() { return iConflicts != null && !iConflicts.isEmpty(); }
		public void setConflicts(String conflicts) { iConflicts = conflicts; }
		
		public long[] getSelection() { return iSelection; }
		public boolean hasSelection() { return iSelection != null; }
		public void setSelection(long[] selection) { iSelection = selection; }
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof RelatedObjectInterface)) return false;
			RelatedObjectInterface r = (RelatedObjectInterface)o;
			return getType() == r.getType() && getUniqueId().equals(r.getUniqueId());
		}
		
		public int hashCode() { return getUniqueId().hashCode(); }
		
		public String toString() {
			return getName() + " (" + getType() + ")";
		}
    }
    
	public static class SelectionInterface implements IsSerializable {
		private Set<Integer> iDays = new TreeSet<Integer>();
		private int iStartSlot, iLength;
		private Set<ResourceInterface> iLocations = new TreeSet<ResourceInterface>();
		
		public SelectionInterface() {}
		
		public Set<Integer> getDays() { return iDays; }
		public void addDay(int day) { iDays.add(day); }
		
		public int getStartSlot() { return iStartSlot; }
		public void setStartSlot(int startSlot) { iStartSlot = startSlot; }
		
		public int getLength() { return iLength; }
		public void setLength(int length) { iLength = length; }
		
		public Set<ResourceInterface> getLocations() { return iLocations; }
		public void addLocation(ResourceInterface location) { iLocations.add(location); }
		
		public String toString() { 
			return "Selection{days:" + getDays() + ", start:" + getStartSlot() + ", length:" + getLength() + ", rooms:" + getLocations() + "}";
		}
	}
	
	public static abstract class EventRpcRequest<T extends GwtRpcResponse> implements GwtRpcRequest<T> {
		private Long iSessionId;
		
		public EventRpcRequest() {}
		public EventRpcRequest(Long sessionId) {
			setSessionId(sessionId);
		}

		public boolean hasSessionId() { return iSessionId != null; }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
	}
	
	public static abstract class FilterRpcRequest extends EventRpcRequest<FilterRpcResponse> {
		public static enum Command implements IsSerializable {
			LOAD,
			SUGGESTIONS,
			ENUMERATE,
		}
		
		private Command iCommand;
		private String iText;
		private HashMap<String, Set<String>> iOptions;
		
		public FilterRpcRequest() {}
		
		public Command getCommand() { return iCommand; }
		public void setCommand(Command command) { iCommand = command; }
		public boolean hasText() { return iText != null && !iText.isEmpty(); }
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
		public boolean hasOptions() { return iOptions != null && !iOptions.isEmpty(); }
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
		
		public void setOptions(String command, Collection<String> values) {
			if (iOptions == null) iOptions = new HashMap<String, Set<String>>();
			Set<String> options = iOptions.get(command);
			if (options == null) {
				options = new HashSet<String>();
				iOptions.put(command, options);
			} else {
				options.clear();
			}
			options.addAll(values);
		}
		
		public boolean isEmpty() {
			if (hasText()) return false;
			if (hasOptions()) {
				int extra = 0;
				if (hasOption("user")) extra ++;
				if (hasOption("role")) extra ++;
				return getOptions().size() == extra; 
			} else return true;
		}
		
		public String toQueryString() {
			String ret = "";
			if (hasOptions()) {
				for (Map.Entry<String, Set<String>> option: getOptions().entrySet()) {
					for (String value: option.getValue()) {
						ret += (ret.isEmpty() ? "" : " ") + option.getKey() + ":\"" + value + "\"";
					}
				}
			}
			if (hasText())
				ret += (ret.isEmpty() ? "" : " ") + getText();
			return ret;
		}
		
		@Override
		public String toString() { return (getCommand() == null ? "NULL" : getCommand().name()) + "(" + getSessionId() + "," + iOptions + "," + getText() + ")"; }
	}
	
	public static class EventFilterRpcRequest extends FilterRpcRequest {
		public EventFilterRpcRequest() {}
	}
	
	public static class RoomFilterRpcRequest extends FilterRpcRequest {
		public RoomFilterRpcRequest() {}
	}
	
	public static class FilterRpcResponse implements GwtRpcResponse {
		private HashMap<String, ArrayList<Entity>> iEntities = null;
		
		public FilterRpcResponse() {}
		
		public boolean hasEntities() {
			return iEntities != null && !iEntities.isEmpty();
		}
		
		public boolean hasEntities(String type) {
			List<Entity> entities = getEntities(type);
			return entities != null && !entities.isEmpty();
		}
		
		public Set<String> getTypes() {
			return iEntities == null ? null : new TreeSet<String>(iEntities.keySet());
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
		
		public void addSuggestion(String message, String replacement, String hint, String command) {
			add("suggestion", new Entity(0l, replacement, message, "hint", hint, "command", command));
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
				int cmp = getProperty("order", "").compareTo(e.getProperty("order", ""));
				if (cmp != 0) return cmp;
				return getName().compareToIgnoreCase(e.getName());
			}
			public String toString() { return getName(); }
		}
		
		public String toString() { return (iEntities == null ? "null" : iEntities.toString()); }
	}
	
	public static class ResourceLookupRpcRequest extends EventRpcRequest<GwtRpcResponseList<ResourceInterface>> {
		private ResourceType iResourceType;
		private String iName;
		private Integer iLimit;
		
		public ResourceLookupRpcRequest() {}
		
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
	
	public static class EventLookupRpcRequest extends EventRpcRequest<GwtRpcResponseList<EventInterface>> {
		private ResourceType iResourceType;
		private Long iResourceId;
		private String iResourceExternalId;
		private EventFilterRpcRequest iEventFilter;
		private RoomFilterRpcRequest iRoomFilter;
		private int iLimit = -1;
		
		public EventLookupRpcRequest() {}
		
		public ResourceType getResourceType() { return iResourceType; }
		public void setResourceType(ResourceType resourceType) { iResourceType = resourceType; }

		public Long getResourceId() { return iResourceId; }
		public void setResourceId(Long resourceId) { iResourceId = resourceId; }
		
		public boolean hasResourceExternalId() { return iResourceExternalId != null && !iResourceExternalId.isEmpty(); }
		public String getResourceExternalId() { return iResourceExternalId; }
		public void setResourceExternalId(String resourceExternalId) { iResourceExternalId = resourceExternalId; }
		
		public EventFilterRpcRequest getEventFilter() { return iEventFilter; }
		public void setEventFilter(EventFilterRpcRequest eventFilter) { iEventFilter = eventFilter; }

		public RoomFilterRpcRequest getRoomFilter() { return iRoomFilter; }
		public void setRoomFilter(RoomFilterRpcRequest roomFilter) { iRoomFilter = roomFilter; }
		
		public boolean hasLimit() { return iLimit > 0; }
		public int getLimit() { return iLimit; }
		public void setLimit(int limit) { iLimit = limit; }
		
		public static EventLookupRpcRequest findEvents(Long sessionId, ResourceInterface resource, EventFilterRpcRequest eventFilter, RoomFilterRpcRequest roomFilter, int limit) {
			EventLookupRpcRequest request = new EventLookupRpcRequest();
			request.setSessionId(sessionId);
			if (resource != null) {
				request.setResourceType(resource.getType());
				request.setResourceId(resource.getId());
				request.setResourceExternalId(resource.getExternalId());
			} else {
				request.setResourceType(ResourceType.ROOM);
			}
			request.setEventFilter(eventFilter);
			request.setRoomFilter(roomFilter);
			request.setLimit(limit);
			return request;
		}
		
		@Override
		public String toString() { return "sessionId=" + getSessionId() + ",resource=" + getResourceType() + "@" + (hasResourceExternalId() ? getResourceExternalId() : getResourceId()); }
	}
	
	public static class EncodeQueryRpcRequest implements GwtRpcRequest<EncodeQueryRpcResponse> {
		private String iQuery;
		
		public EncodeQueryRpcRequest() {}
		public EncodeQueryRpcRequest(String query) { iQuery = query; }

		public String getQuery() { return iQuery; }
		public void setQuery(String query) { iQuery = query; }
		
		public static EncodeQueryRpcRequest encode(String query) {
			return new EncodeQueryRpcRequest(query); 
		}
		
		@Override
		public String toString() { return getQuery(); }
	}
	
	public static class EncodeQueryRpcResponse implements GwtRpcResponse {
		private String iQuery;
		
		public EncodeQueryRpcResponse() {}
		public EncodeQueryRpcResponse(String query) { iQuery = query; }

		public String getQuery() { return iQuery; }
		public void setQuery(String query) { iQuery = query; }
	}
	
	public static class EventPropertiesRpcRequest extends EventRpcRequest<EventPropertiesRpcResponse> {
		public EventPropertiesRpcRequest() {}
		public EventPropertiesRpcRequest(Long sessionId) { 
			setSessionId(sessionId);
		}
		
		public static EventPropertiesRpcRequest requestEventProperties(Long sessionId) {
			return new EventPropertiesRpcRequest(sessionId);
		}
		
		@Override
		public String toString() { return hasSessionId() ? getSessionId().toString() : "NULL"; }
	}
	
	public static class StandardEventNoteInterface implements IsSerializable, Comparable<StandardEventNoteInterface> {
		private Long iId;
		private String iReference, iNote;
		
		public StandardEventNoteInterface() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getReference() { return iReference; }
		public void setReference(String reference) { iReference = reference; }
		
		public String getNote() { return iNote; }
		public void setNote(String note) { iNote = note; }
		
		@Override
		public String toString() { return getReference() + ": " + getNote(); }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof StandardEventNoteInterface)) return false;
			StandardEventNoteInterface n = (StandardEventNoteInterface)o;
			return getId().equals(n.getId());
		}
		
		public int compareTo(StandardEventNoteInterface n) {
			return toString().compareToIgnoreCase(n.toString());
		}
	}
	
	public static class EventPropertiesRpcResponse implements GwtRpcResponse {
		private boolean iCanLookupPeople = false, iCanLookupContacts = false, iCanAddEvent = false, iCanAddCourseEvent = false, iCanAddUnavailableEvent = false, iCanExportCSV = false, iCanSetExpirationDate = false;
		private List<SponsoringOrganizationInterface> iSponsoringOrganizations = null;
		private ContactInterface iMainContact = null;
		private Set<StandardEventNoteInterface> iStandardNotes = null;
		private Boolean iEmailConfirmation = null;
	
		public EventPropertiesRpcResponse() {}
		
		public boolean isCanLookupPeople() { return iCanLookupPeople; }
		public void setCanLookupPeople(boolean canLookupPeople) { iCanLookupPeople = canLookupPeople; }
		
		public boolean isCanExportCSV() { return iCanExportCSV; }
		public void setCanExportCSV(boolean canExportCSV) { iCanExportCSV = canExportCSV; }
		
		public boolean isCanLookupContacts() { return iCanLookupContacts; }
		public void setCanLookupContacts(boolean canLookupContacts) { iCanLookupContacts = canLookupContacts; }

		public boolean isCanAddEvent() { return iCanAddEvent; }
		public void setCanAddEvent(boolean canAddEvent) { iCanAddEvent = canAddEvent; }

		public boolean isCanAddCourseEvent() { return iCanAddCourseEvent; }
		public void setCanAddCourseEvent(boolean canAddEvent) { iCanAddCourseEvent = canAddEvent; }
		
		public boolean isCanAddUnavailableEvent() { return iCanAddUnavailableEvent; }
		public void setCanAddUnavailableEvent(boolean canAddEvent) { iCanAddUnavailableEvent = canAddEvent; }
		
		public boolean isCanSetExpirationDate() { return iCanSetExpirationDate; }
		public void setCanSetExpirationDate(boolean canSetExpirationDate) { iCanSetExpirationDate = canSetExpirationDate; }

		public boolean hasSponsoringOrganizations() { return iSponsoringOrganizations != null && !iSponsoringOrganizations.isEmpty(); }
		public List<SponsoringOrganizationInterface> getSponsoringOrganizations() { return iSponsoringOrganizations; }
		public void addSponsoringOrganization(SponsoringOrganizationInterface sponsor) {
			if (iSponsoringOrganizations == null) iSponsoringOrganizations = new ArrayList<SponsoringOrganizationInterface>();
			iSponsoringOrganizations.add(sponsor);
		}
		
		public boolean hasMainContact() { return iMainContact != null; }
		public ContactInterface getMainContact() { return iMainContact; }
		public void setMainContact(ContactInterface mainContact) { iMainContact = mainContact; }
		
		public boolean hasStandardNotes() { return iStandardNotes != null && !iStandardNotes.isEmpty(); }
		public Set<StandardEventNoteInterface> getStandardNotes() { return iStandardNotes; }
		public void addStandardNote(StandardEventNoteInterface note) {
			if (iStandardNotes == null) { iStandardNotes = new TreeSet<StandardEventNoteInterface>(); }
			iStandardNotes.add(note);
		}
		
		public boolean hasEmailConfirmation() { return iEmailConfirmation != null; }
		public boolean isEmailConfirmation() { return iEmailConfirmation != null && iEmailConfirmation.booleanValue(); }
		public void setEmailConfirmation(Boolean emailConfirmation) { iEmailConfirmation = emailConfirmation ;}
	}
	
	public static class EventDetailRpcRequest extends EventRpcRequest<EventInterface> {
		private Long iEventId;
		public EventDetailRpcRequest() {}
		
		public Long getEventId() { return iEventId; }
		public void setEventId(Long eventId) { iEventId = eventId; }
		
		public static EventDetailRpcRequest requestEventDetails(Long sessionId, Long eventId) {
			EventDetailRpcRequest request = new EventDetailRpcRequest();
			request.setSessionId(sessionId);
			request.setEventId(eventId);
			return request;
		}
		
		@Override
		public String toString() { return "sessionId=" + getSessionId() + ",eventId=" + getEventId(); }
	}
	
	public static class EventRoomAvailabilityRpcRequest extends EventRpcRequest<EventRoomAvailabilityRpcResponse> {
		private Long iEventId;
		private EventType iEventType;
		private Integer iStartSlot, iEndSlot;
		private List<Integer> iDates;
		private List<Long> iLocations;
		private List<MeetingInterface> iMeetings;
		
		public EventRoomAvailabilityRpcRequest() {}
		
		public void setStartSlot(Integer startSlot) { iStartSlot = startSlot; }
		public Integer getStartSlot() { return iStartSlot; }
		
		public void setEndSlot(Integer endSlot) { iEndSlot = endSlot; }
		public Integer getEndSlot() { return iEndSlot; }
		
		public void setDates(List<Integer> dates) { iDates = dates; }
		public boolean hasDates() { return iDates != null && !iDates.isEmpty(); }
		public List<Integer> getDates() { return iDates; }
		
		public void setLocations(List<Long> locations) { iLocations = locations; }
		public boolean hasLocations() { return iLocations != null && !iLocations.isEmpty(); }
		public List<Long> getLocations() { return iLocations; }
		
		public void setMeetings(List<MeetingInterface> meetings) { iMeetings = meetings; }
		public boolean hasMeetings() { return iMeetings != null && !iMeetings.isEmpty(); }
		public List<MeetingInterface> getMeetings() { return iMeetings; }
		
		public boolean hasEventId() { return iEventId != null; }
		public Long getEventId() { return iEventId; }
		public void setEventId(Long eventId) { iEventId = eventId; }
		
		public boolean hasEventType() { return iEventType != null; }
		public EventType getEventType() { return iEventType; }
		public void setEventType(EventType type) { iEventType = type; }
		
		public static EventRoomAvailabilityRpcRequest checkAvailability(int startSlot, int endSlot, List<Integer> dates, List<FilterRpcResponse.Entity> locations, Long eventId, Long sessionId) {
			EventRoomAvailabilityRpcRequest request = new EventRoomAvailabilityRpcRequest();
			request.setStartSlot(startSlot);
			request.setEndSlot(endSlot);
			request.setDates(dates);
			List<Long> locationIds = new ArrayList<Long>();
			for (FilterRpcResponse.Entity location: locations)
				locationIds.add(Long.valueOf(location.getProperty("permId", null)));
			request.setLocations(locationIds);
			request.setEventId(eventId);
			request.setSessionId(sessionId);
			return request;
		}
		
		public static EventRoomAvailabilityRpcRequest checkAvailability(List<MeetingInterface> meetings, Long eventId, EventType eventType, Long sessionId) {
			EventRoomAvailabilityRpcRequest request = new EventRoomAvailabilityRpcRequest();
			request.setMeetings(meetings);
			request.setEventId(eventId);
			request.setEventType(eventType);
			request.setSessionId(sessionId);
			return request;
		}
		
		@Override
		public String toString() { return "sessionId="+ getSessionId() + (hasMeetings() ? ",meetings=" + getMeetings() : ",start=" + getStartSlot() + ",end=" + getEndSlot() + ",dates=" + getDates() + ",locations=" + getLocations()); }
	}
	
	public static class EventRoomAvailabilityRpcResponse implements GwtRpcResponse {
		private Map<Integer, Map<Long, Set<MeetingConflictInterface>>> iOverlaps = new HashMap<Integer, Map<Long, Set<MeetingConflictInterface>>>();
		private List<MeetingInterface> iMeetings;
		
		public EventRoomAvailabilityRpcResponse() {}
		
		public void setMeetings(List<MeetingInterface> meetings) { iMeetings = meetings; }
		public boolean hasMeetings() { return iMeetings != null && !iMeetings.isEmpty(); }
		public List<MeetingInterface> getMeetings() { return iMeetings; }
		
		public void addOverlap(Integer date, Long locationId, MeetingConflictInterface conflict) {
			Map<Long, Set<MeetingConflictInterface>> loc2overlaps = iOverlaps.get(date);
			if (loc2overlaps == null) {
				loc2overlaps = new HashMap<Long, Set<MeetingConflictInterface>>();
				iOverlaps.put(date, loc2overlaps);
			}
			Set<MeetingConflictInterface> overlaps = loc2overlaps.get(locationId);
			if (overlaps == null) {
				overlaps = new TreeSet<MeetingConflictInterface>();
				loc2overlaps.put(locationId, overlaps);
			}
			overlaps.add(conflict);
		}
		public boolean hasOverlaps() {
			return iOverlaps != null && !iOverlaps.isEmpty();
		}
		
		public boolean isAvailable(Integer date, Long locationId) {
			Set<MeetingConflictInterface> overlaps = getOverlaps(date, locationId);
			return (overlaps == null || overlaps.isEmpty());
		}
		
		public Set<MeetingConflictInterface> getOverlaps(Integer date, Long locationId) {
			Map<Long, Set<MeetingConflictInterface>> loc2overlaps = iOverlaps.get(date);
			return (loc2overlaps == null ? null : loc2overlaps.get(locationId));
		}
	}
	
	public static class RelatedObjectLookupRpcRequest extends EventRpcRequest<GwtRpcResponseList<RelatedObjectLookupRpcResponse>> {
		public static enum Level implements IsSerializable {
			SESSION,
			SUBJECT,
			OFFERING,
			COURSE,
			CONFIG,
			SUBPART,
			CLASS,
			NONE
		}
		private Long iUniqueId;
		private Long iCourseId;
		private Level iLevel;
		
		public RelatedObjectLookupRpcRequest() {}
		
		public Long getUniqueId() { return iUniqueId; }
		public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
		
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		
		public Level getLevel() { return iLevel; }
		public void setLevel(Level level) { iLevel = level; }
		
		@Override
		public String toString() {
			return getLevel().name() + "[" + getUniqueId() + "]";
		}
		
		public static RelatedObjectLookupRpcRequest getChildren(Long sessionId, Level level, Long uniqueId) {
			RelatedObjectLookupRpcRequest request = new RelatedObjectLookupRpcRequest();
			request.setSessionId(sessionId);
			request.setLevel(level);
			request.setUniqueId(uniqueId);
			return request;
		}
		
		public static RelatedObjectLookupRpcRequest getChildren(Long sessionId, RelatedObjectLookupRpcResponse... response) {
			RelatedObjectLookupRpcRequest request = new RelatedObjectLookupRpcRequest();
			request.setSessionId(sessionId);
			request.setLevel(response[response.length - 1].getLevel());
			request.setUniqueId(response[response.length - 1].getUniqueId());
			for (RelatedObjectLookupRpcResponse r: response)
				if (r.getLevel() == Level.COURSE)
					request.setCourseId(r.getUniqueId());
			return request;
		}
	}
	
	public static class RelatedObjectLookupRpcResponse implements GwtRpcResponse {
		private RelatedObjectLookupRpcRequest.Level iLevel;
		private Long iUniqueId;
		private String iName, iText;
		private RelatedObjectInterface iRelatedObject = null;
		
		public RelatedObjectLookupRpcResponse() {}
		public RelatedObjectLookupRpcResponse(RelatedObjectLookupRpcRequest.Level level, Long uniqueId, String name) {
			this(level, uniqueId, name, null, null);
		}
		public RelatedObjectLookupRpcResponse(RelatedObjectLookupRpcRequest.Level level, Long uniqueId, String name, String text) {
			this(level, uniqueId, name, text, null);
		}
		public RelatedObjectLookupRpcResponse(RelatedObjectLookupRpcRequest.Level level, Long uniqueId, String name, RelatedObjectInterface related) {
			this(level, uniqueId, name, null, related);
		}
		public RelatedObjectLookupRpcResponse(RelatedObjectLookupRpcRequest.Level level, Long uniqueId, String name, String text, RelatedObjectInterface related) {
			iLevel = level; iUniqueId = uniqueId; iName = name; iText = text; iRelatedObject = related;
		}
		
		public Long getUniqueId() { return iUniqueId; }
		public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
		
		public RelatedObjectLookupRpcRequest.Level getLevel() { return iLevel; }
		public void setLevel(RelatedObjectLookupRpcRequest.Level level) { iLevel = level; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getText() { return iText; }
		public void setText(String text) { iText = text; }
		
		public String getLabel() {
			return getName() + (getText() == null || getText().isEmpty() ? "" : " - " + getText());
		}

		public boolean hasRelatedObject() { return iRelatedObject != null; }
		public RelatedObjectInterface getRelatedObject() { return iRelatedObject; }
		public void setRelatedObject(RelatedObjectInterface relatedObject) { iRelatedObject = relatedObject; }
		
		public boolean equals(Object o) {
			if (o == null || ! (o instanceof RelatedObjectLookupRpcResponse)) return false;
			RelatedObjectLookupRpcResponse r = (RelatedObjectLookupRpcResponse)o;
			return EventInterface.equals(getLevel(), r.getLevel()) && EventInterface.equals(getUniqueId(), r.getUniqueId());
		}
		
		public String toString() {
			return getLevel() + "@" + getUniqueId() + " " + getLabel();
		}
	}
	
	public static class EventEnrollmentsRpcRequest extends EventRpcRequest<GwtRpcResponseList<ClassAssignmentInterface.Enrollment>> {
		private List<RelatedObjectInterface> iRelatedObjects = null;
		private List<MeetingInterface> iMeetings = null;
		private Long iEventId;
		
		public EventEnrollmentsRpcRequest() {}
		
		public boolean hasRelatedObjects() { return iRelatedObjects != null && !iRelatedObjects.isEmpty(); }
		public void setRelatedObjects(List<RelatedObjectInterface> objects) { iRelatedObjects = objects; }
		public void addRelatedObject(RelatedObjectInterface relatedObject) {
			if (iRelatedObjects == null) iRelatedObjects = new ArrayList<RelatedObjectInterface>();
			iRelatedObjects.add(relatedObject);
		}
		public List<RelatedObjectInterface> getRelatedObjects() { return iRelatedObjects; }
		
		public boolean hasMeetings() { return iMeetings != null && !iMeetings.isEmpty(); }
		public void setMeetings(List<MeetingInterface> meetings) { iMeetings = meetings; }
		public void addMeeting(MeetingInterface meeting) {
			if (iMeetings == null) iMeetings = new ArrayList<MeetingInterface>();
			iMeetings.add(meeting);
		}
		public List<MeetingInterface> getMeetings() { return iMeetings; }

		public boolean hasEventId() { return iEventId != null; }
		public Long getEventId() { return iEventId; }
		public void setEventId(Long eventId) { iEventId = eventId; }
		
		public String toString() {
			return "objects=" + (hasRelatedObjects() ? iRelatedObjects.toString() : "NULL") +
				", meetings=" + (hasMeetings() ? getMeetings().toString() : "NULL");
		}
		
		public static EventEnrollmentsRpcRequest getEnrollmentsForEvent(Long eventId, Long sessionId) {
			EventEnrollmentsRpcRequest request = new EventEnrollmentsRpcRequest();
			request.setEventId(eventId);
			request.setSessionId(sessionId);
			return request;
		}
		
		public static EventEnrollmentsRpcRequest getEnrollmentsForRelatedObjects(List<RelatedObjectInterface> objects, List<MeetingInterface> meetings, Long eventId, Long sessionId) {
			EventEnrollmentsRpcRequest request = new EventEnrollmentsRpcRequest();
			request.setRelatedObjects(objects);
			request.setMeetings(meetings);
			request.setEventId(eventId);
			request.setSessionId(sessionId);
			return request;
		}
	}
	
	public static abstract class SaveOrApproveEventRpcRequest extends EventRpcRequest<SaveOrApproveEventRpcResponse> {
		public static enum Operation implements IsSerializable {
			APPROVE,
			REJECT,
			INQUIRE,
			CREATE,
			UPDATE,
			DELETE,
			CANCEL
		}
		private EventInterface iEvent;
		private String iMessage;
		private boolean iEmailConfirmation = true;
		
		public EventInterface getEvent() { return iEvent; }
		public void setEvent(EventInterface event) { iEvent = event; }

		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public void setMessage(String message) { iMessage = message; }
		public String getMessage() { return iMessage; }
		
		public boolean isEmailConfirmation() { return iEmailConfirmation; }
		public void setEmailConfirmation(boolean emailConfirmation) { iEmailConfirmation = emailConfirmation ;}
		
		public abstract Operation getOperation();
	}
	
	public static class SaveEventRpcRequest extends SaveOrApproveEventRpcRequest {
		
		public SaveEventRpcRequest() {}
		
		@Override
		public Operation getOperation() {
			return (getEvent().hasMeetings() ? getEvent().getId() == null ? Operation.CREATE : Operation.UPDATE : Operation.DELETE);
		}
		
		public static SaveEventRpcRequest saveEvent(EventInterface event, Long sessionId, String message, boolean emailConfirmation) {
			SaveEventRpcRequest request = new SaveEventRpcRequest();
			request.setEvent(event);
			request.setSessionId(sessionId);
			request.setMessage(message);
			request.setEmailConfirmation(emailConfirmation);
			return request;
		}
		
		public String toString() {
			return getEvent() + (getEvent().hasMeetings() ? " " + getEvent().getMeetings() : "");
		}
	}
	
	public static class MessageInterface implements IsSerializable {
		public static enum Level implements IsSerializable {
			INFO,
			WARN,
			ERROR
		}
		private Level iLevel;
		private String iMessage;
		
		public MessageInterface() {}
		
		public MessageInterface(Level level, String message) {
			iLevel = level; iMessage = message;
		}
		
		public Level getLevel() { return iLevel; }
		public boolean isInfo() { return iLevel == Level.INFO; }
		public boolean isWarning() { return iLevel == Level.WARN; }
		public boolean isError() { return iLevel == Level.ERROR; }
		
		public String getMessage() { return iMessage; }
		
		public String toString() {
			return getLevel() + ": " + getMessage();
		}
	}
	
	public static class SaveOrApproveEventRpcResponse implements GwtRpcResponse {
		private EventInterface iEvent;
		private List<MessageInterface> iMessages = null;
		private List<NoteInterface> iNotes = null;
		private TreeSet<MeetingInterface> iUpdatedMeetings = null, iCreatedMeetings = null, iDeletedMeetings = null, iCancelledMeetings = null;
		
		public SaveOrApproveEventRpcResponse() {}
		
		public void setEvent(EventInterface event) { iEvent = event; }
		public boolean hasEvent() { return iEvent != null; }
		public boolean hasEventWithId() { return iEvent != null && iEvent.getId() != null; }
		public EventInterface getEvent() { return iEvent; }
		
		public boolean hasMessages() { return iMessages != null && !iMessages.isEmpty(); }
		public List<MessageInterface> getMessages() { return iMessages; }
		public void addMessage(MessageInterface.Level level, String message) {
			if (iMessages == null) iMessages = new ArrayList<MessageInterface>();
			iMessages.add(new MessageInterface(level, message));
		}
		public void info(String message) { addMessage(MessageInterface.Level.INFO, message); }
		public void warn(String message) { addMessage(MessageInterface.Level.WARN, message); }
		public void error(String message) { addMessage(MessageInterface.Level.ERROR, message); }
		
		public List<NoteInterface> getNotes() { return iNotes; }
		public boolean hasNotes() { return iNotes != null && !iNotes.isEmpty(); }
		public void addNote(NoteInterface note) {
			if (iNotes == null) iNotes = new ArrayList<NoteInterface>();
			iNotes.add(note);
		}
		
		public boolean hasCreatedMeetings() { return iCreatedMeetings != null && !iCreatedMeetings.isEmpty(); }
		public void addCreatedMeeting(MeetingInterface meeting) {
			if (iCreatedMeetings == null) iCreatedMeetings = new TreeSet<MeetingInterface>();
			iCreatedMeetings.add(meeting);
		}
		public TreeSet<MeetingInterface> getCreatedMeetings() { return iCreatedMeetings; }

		public boolean hasUpdatedMeetings() { return iUpdatedMeetings != null && !iUpdatedMeetings.isEmpty(); }
		public void addUpdatedMeeting(MeetingInterface meeting) {
			if (iUpdatedMeetings == null) iUpdatedMeetings = new TreeSet<MeetingInterface>();
			iUpdatedMeetings.add(meeting);
		}
		public TreeSet<MeetingInterface> getUpdatedMeetings() { return iUpdatedMeetings; }

		public boolean hasDeletedMeetings() { return iDeletedMeetings != null && !iDeletedMeetings.isEmpty(); }
		public void addDeletedMeeting(MeetingInterface meeting) {
			if (iDeletedMeetings == null) iDeletedMeetings = new TreeSet<MeetingInterface>();
			iDeletedMeetings.add(meeting);
		}
		public TreeSet<MeetingInterface> getDeletedMeetings() { return iDeletedMeetings; }

		public boolean hasCancelledMeetings() { return iCancelledMeetings != null && !iCancelledMeetings.isEmpty(); }
		public void addCancelledMeeting(MeetingInterface meeting) {
			if (iCancelledMeetings == null) iCancelledMeetings = new TreeSet<MeetingInterface>();
			iCancelledMeetings.add(meeting);
		}
		public TreeSet<MeetingInterface> getCancelledMeetings() { return iCancelledMeetings; }
}
	
	public static class ApproveEventRpcRequest extends SaveOrApproveEventRpcRequest {
		private Operation iOperation;
		private TreeSet<MeetingInterface> iMeetings;
		
		public ApproveEventRpcRequest() {}
		
		@Override
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public boolean hasMeetings() { return iMeetings != null && !iMeetings.isEmpty(); }
		public void addMeeting(MeetingInterface meeting) {
			if (iMeetings == null) iMeetings = new TreeSet<MeetingInterface>();
			iMeetings.add(meeting);
		}
		public TreeSet<MeetingInterface> getMeetings() { return iMeetings; }
		
		public static ApproveEventRpcRequest createRequest(Operation operation, Long sessionId, EventInterface event, List<MeetingInterface> meetings, String message, boolean emailConfirmation) {
			ApproveEventRpcRequest request = new ApproveEventRpcRequest();
			request.setOperation(operation);
			request.setMessage(message);
			request.setEmailConfirmation(emailConfirmation);
			request.setSessionId(sessionId);
			request.setEvent(event);
			if (meetings != null)
				for (MeetingInterface meeting: meetings)
					request.addMeeting(meeting);
			return request;
		}

		public String toString() {
			return getOperation() + " " + getEvent().getName() + " " + getMeetings();
		}
	}
	
	public static interface DateFormatter {
		public String formatFirstDate(Date date);
		public String formatLastDate(Date date);
	}
    
    public static String toString(Collection<MeetingInterface> meetings, GwtConstants constants, String separator, DateFormatter df) {
    	String ret = "";
    	for (MultiMeetingInterface m: getMultiMeetings(meetings, false)) {
    		if (!ret.isEmpty()) ret += separator;
    		ret += (m.getDays(constants.shortDays(), constants.shortDays(), "") + " " +
    				(m.isArrangeHours() ? constants.arrangeHours() : m.getNrMeetings() == 1 ? df.formatLastDate(m.getFirstMeetingDate()) : df.formatFirstDate(m.getFirstMeetingDate()) + " - " + df.formatLastDate(m.getLastMeetingDate())) + " " +
    				m.getMeetings().first().getMeetingTime(constants) + " " + m.getLocationName()).trim();
    	}
    	return ret;
    }
    
    public static enum EventFlag implements IsSerializable {
		SHOW_PUBLISHED_TIME,
		SHOW_ALLOCATED_TIME,
		SHOW_SETUP_TIME,
		SHOW_TEARDOWN_TIME,
		SHOW_CAPACITY,
		SHOW_LIMIT,
		SHOW_ENROLLMENT,
		SHOW_MAIN_CONTACT,
		SHOW_SPONSOR,
		SHOW_SECTION,
		SHOW_TITLE,
		SHOW_APPROVAL,
		SHOW_NOTE;
		
		public int flag() { return 1 << ordinal(); }
		public boolean in(int flags) {
			return (flags & flag()) != 0;
		}
		public int set(int flags) {
			return (in(flags) ? flags : flags + flag());
		}
		public int clear(int flags) {
			return (in(flags) ? flags - flag() : flags);
		}
	}
    
	public static final int sDefaultEventFlags =
				EventFlag.SHOW_PUBLISHED_TIME.flag() +
				EventFlag.SHOW_MAIN_CONTACT.flag() +
				EventFlag.SHOW_SPONSOR.flag() +
				EventFlag.SHOW_CAPACITY.flag() +
				EventFlag.SHOW_TITLE.flag() + 
				EventFlag.SHOW_APPROVAL.flag();
	
	public static class SessionMonth implements IsSerializable {
		public static enum Flag implements IsSerializable {
			START,
			END,
			FINALS,
			HOLIDAY,
			BREAK,
			SELECTED,
			DISABLED,
			PAST,
			WEEKEND,
			DATE_MAPPING_CLASS,
			DATE_MAPPING_EVENT;
			
			public int flag() { return 1 << ordinal(); }
		}
		
		private int iYear, iMonth;
		private int[] iDays;
		
		public SessionMonth() {}
		public SessionMonth(int year, int month) {
			iYear = year; iMonth = month;
			iDays = new int[31];
		}
		public int getYear() { return iYear; }
		public int getMonth() { return iMonth; }
		public boolean hasFlag(int day, Flag f) {
			return (iDays[day] & f.flag()) != 0;
		}
		public void setFlag(int day, Flag f) {
			if (!hasFlag(day, f)) iDays[day] += f.flag();
		}
		public void clearFlag(int day, Flag f) {
			if (hasFlag(day, f)) iDays[day] -= f.flag();
		}
		public int getFlags(int day) {
			return iDays[day];
		}
		public int getFirst(Flag flag) {
			for (int i = 0; i < iDays.length; i++)
				if (hasFlag(i, flag)) return i;
			return -1;
		}
	}
	
	public static class RequestSessionDetails extends EventRpcRequest<GwtRpcResponseList<SessionMonth>> {

		public RequestSessionDetails() {}
		public RequestSessionDetails(Long sessionId) { setSessionId(sessionId); }
		
		@Override
		public String toString() {
			return getSessionId().toString();
		}
	}
	
	public static interface DateFlagsProvider {
		public SessionMonth.Flag getDateFlag(EventType type, Date date);
	}
}
