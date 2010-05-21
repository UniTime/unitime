/*
 * UniTime 4.0 (University Timetabling Application)
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

import com.google.gwt.user.client.rpc.IsSerializable;

public class ClassAssignmentInterface implements IsSerializable {
	private ArrayList<CourseAssignment> iAssignments = new ArrayList<CourseAssignment>();
	private ArrayList<String> iMessages = null;
	
	public ClassAssignmentInterface() {}
	
	public ArrayList<CourseAssignment> getCourseAssignments() { return iAssignments; }
	public void add(CourseAssignment a) { iAssignments.add(a); }
	public void clear() {
		iAssignments.clear();
		if (iMessages != null) iMessages.clear();
	}
	
	public void addMessage(String message) {
		if (iMessages == null) iMessages = new ArrayList<String>();
		iMessages.add(message);
	}
	public boolean hasMessages() {
		return iMessages != null && !iMessages.isEmpty();
	}
	public ArrayList<String> getMessages() { return iMessages; }
	public String getMessages(String delim) {
		String ret = "";
		if (iMessages == null) return ret;
		for (String message: iMessages) {
			if (!ret.isEmpty()) ret += delim;
			ret += message;
		}
		return ret;
	}
	
	public static class CourseAssignment implements IsSerializable {
		private Long iCourseId = null;
		private boolean iAssigned = true;
		private String iSubject, iCourseNbr, iTitle, iNote;
		private boolean iHasUniqueName = true;
		
		private ArrayList<String> iOverlaps = null;
		private boolean iNotAvailable = false;
		private String iInstead;

		private ArrayList<ClassAssignment> iAssignments = new ArrayList<ClassAssignment>();

		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public boolean isFreeTime() { return (iCourseId == null); }
		
		public boolean isAssigned() { return iAssigned; }
		public void setAssigned(boolean assigned) { iAssigned = assigned; }

		public String getSubject() { return iSubject; }
		public void setSubject(String subject) { iSubject = subject; }
		
		public String getCourseNbr() { return iCourseNbr; }
		public void setCourseNbr(String courseNbr) { iCourseNbr = courseNbr; }

		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }

		public String getNote() { return iNote; }
		public void setNote(String note) { iNote = note; }
		
		public boolean hasUniqueName() { return iHasUniqueName; }
		public void setHasUniqueName(boolean hasUniqueName) { iHasUniqueName = hasUniqueName; }

		public void addOverlap(String overlap) {
			if (iOverlaps == null) iOverlaps = new ArrayList<String>();
			iOverlaps.add(overlap);
		}
		public ArrayList<String> getOverlaps() { return iOverlaps; }
		
		public boolean isNotAvailable() { return iNotAvailable; }
		public void setNotAvailable(boolean notAvailable) { iNotAvailable = notAvailable; }
		
		public void setInstead(String instead) { iInstead = instead; }
		public String getInstead() { return iInstead; }
		
		public ArrayList<ClassAssignment> getClassAssignments() { return iAssignments; }
		public ClassAssignment addClassAssignment() { 
			ClassAssignment a = new ClassAssignment(this);
			iAssignments.add(a);
			return a;
		}
	}
	
	public static class ClassAssignment implements IsSerializable {
		private boolean iCourseAssigned = true;
		private Long iCourseId, iClassId, iSubpartId;
		private ArrayList<Integer> iDays = new ArrayList<Integer>();
		private int iStart, iLength, iBreakTime = 0;
		private ArrayList<String> iInstructos = new ArrayList<String>();
		private ArrayList<String> iInstructoEmails = new ArrayList<String>();
		private ArrayList<String> iRooms = new ArrayList<String>();
		private boolean iAlternative = false, iHasAlternatives = true, iDistanceConflict = false;
		private String iDatePattern = null;
		private String iSubject, iCourseNbr, iSubpart, iSection, iParentSection;
		private int[] iLimit = null;
		private boolean iPin = false;
		private int iBackToBackDistance = 0;
		private String iBackToBackRooms = null;
		private boolean iSaved = false;
		private Integer iExpected = null;
		
		public ClassAssignment() {}
		public ClassAssignment(CourseAssignment course) {
			iCourseId = course.getCourseId();
			iSubject = course.getSubject();
			iCourseNbr = course.getCourseNbr();
			iCourseAssigned = course.isAssigned();
		}
		
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public boolean isFreeTime() { return (iCourseId == null); }
		
		public boolean isCourseAssigned() { return iCourseAssigned; }
		public void setCourseAssigned(boolean courseAssigned) { iCourseAssigned = courseAssigned; }
		
		public String getSubject() { return iSubject; }
		public void setSubject(String subject) { iSubject = subject; }
		
		public String getCourseNbr() { return iCourseNbr; }
		public void setCourseNbr(String courseNbr) { iCourseNbr = courseNbr; }
		
		public String getSubpart() { return iSubpart; }
		public void setSubpart(String subpart) { iSubpart = subpart; }
		
		public String getSection() { return iSection; }
		public void setSection(String section) { iSection = section; }

		public String getParentSection() { return iParentSection; }
		public void setParentSection(String parentSection) { iParentSection = parentSection; }

		public boolean isAlternative() { return iAlternative; }
		public void setAlternative(boolean alternative) { iAlternative = alternative; }
				
		public Long getClassId() { return iClassId; }
		public void setClassId(Long classId) { iClassId = classId; }

		public Long getSubpartId() { return iSubpartId; }
		public void setSubpartId(Long subpartId) { iSubpartId = subpartId; }

		public void addDay(int day) {
			if (iDays == null) iDays = new ArrayList<Integer>();
			iDays.add(day);
		}
		public ArrayList<Integer> getDays() { return iDays; }
		public String getDaysString(String[] shortDays) {
			if (iDays == null) return "";
			String ret = "";
			for (int day: iDays)
				ret += shortDays[day];
			return ret;
		}
		public boolean isAssigned() { return iDays != null && !iDays.isEmpty(); }
		
		public int getStart() { return iStart; }
		public void setStart(int start) { iStart = start; }
		public String getStartString() {
			if (!isAssigned()) return "";
	        int h = iStart / 12;
	        int m = 5 * (iStart % 12);
	        return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
		}
		
		public int getLength() { return iLength; }
		public void setLength(int length) { iLength = length; }
		public String getEndString() {
			if (!isAssigned()) return "";
			int h = (5 * (iStart + iLength) - iBreakTime) / 60;
			int m = (5 * (iStart + iLength) - iBreakTime) % 60;
	        return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
		}
		
		public String getTimeString(String[] shortDays) {
			if (!isAssigned()) return "";
			return getDaysString(shortDays) + " " + getStartString() + " - " + getEndString();
		}
		
		public int getBreakTime() { return iBreakTime; }
		public void setBreakTime(int breakTime) { iBreakTime = breakTime; }
		
		public boolean hasDatePattern() { return iDatePattern != null && !iDatePattern.isEmpty(); }
		public String getDatePattern() { return iDatePattern; }
		public void setDatePattern(String datePattern) { iDatePattern = datePattern; }
		
		public boolean hasInstructors() { return iInstructos != null && !iInstructos.isEmpty(); }
		public void addInstructor(String instructor) {
			if (iInstructos == null) iInstructos = new ArrayList<String>();
			iInstructos.add(instructor);
		}
		public ArrayList<String> getInstructors() { return iInstructos; }
		public String getInstructors(String delim) {
			if (iInstructos == null) return "";
			String ret = "";
			for (String instructor: iInstructos) {
				if (!ret.isEmpty()) ret += delim;
				ret += instructor;
			}
			return ret;
		}
		public String getInstructorWithEmails(String delim) {
			if (iInstructos == null) return "";
			String ret = "";
			for (int i = 0; i < iInstructos.size(); i++) {
				if (!ret.isEmpty()) ret += delim;
				String email = (iInstructoEmails != null && i < iInstructoEmails.size() ? iInstructoEmails.get(i) : null);
				if (email != null && !email.isEmpty()) {
					ret += "<A class=\"unitime-SimpleLink\" href=\"mailto:" + email + "\">" + iInstructos.get(i) + "</A>";
				} else  ret += iInstructos.get(i);
			}
			return ret;
		}

		public boolean hasInstructorEmails() { return iInstructoEmails != null && !iInstructoEmails.isEmpty(); }
		public void addInstructoEmailr(String instructorEmail) {
			if (iInstructoEmails == null) iInstructoEmails = new ArrayList<String>();
			iInstructoEmails.add(instructorEmail);
		}
		public ArrayList<String> getInstructorEmails() { return iInstructoEmails; }

		public boolean hasRoom() { return iRooms != null && !iRooms.isEmpty(); }
		public void addRoom(String room) {
			if (iRooms == null) iRooms = new ArrayList<String>();
			iRooms.add(room);
		}
		public ArrayList<String> getRooms() { return iRooms; }
		public String getRooms(String delim) {
			if (iRooms == null) return "";
			String ret = "";
			for (String room: iRooms) {
				if (!ret.isEmpty()) ret += delim;
				ret += room;
			}
			return ret;
		}
		
		public boolean isUnlimited() { return iLimit != null && iLimit[1] >= 9999; }
		public int[] getLimit() { return iLimit; }
		public void setLimit(int[] limit) { iLimit = limit; }
		public String getLimitString() {
			if (iLimit == null) return "";
			if (iLimit[1] >= 9999 || iLimit[1] < 0) return "&infin;";
			if (iLimit[0] < 0) return String.valueOf(iLimit[1]);
			return (iLimit[1] - iLimit[0]) + " / " + iLimit[1];
		}
		public boolean isAvailable() {
			if (iLimit == null) return true;
			if (iLimit[0] < 0) return (iLimit[1] == 0);
			return iLimit[0] < iLimit[1];
		}
		public int getAvailableLimit() {
			if (iLimit == null) return 9999;
			if (iLimit[0] < 0) return 9999;
			return iLimit[1] - iLimit[0];
		}
		
		public boolean isPinned() { return iPin; }
		public void setPinned(boolean pin) { iPin = pin; }
		
		public boolean hasAlternatives() { return iHasAlternatives; }
		public void setHasAlternatives(boolean alternatives) { iHasAlternatives = alternatives; }
		
		public boolean hasDistanceConflict() { return iDistanceConflict; }
		public void setDistanceConflict(boolean distanceConflict) { iDistanceConflict = distanceConflict; }
		
		public int getBackToBackDistance() { return iBackToBackDistance; }
		public void setBackToBackDistance(int backToBackDistance) { iBackToBackDistance = backToBackDistance; }

		public String getBackToBackRooms() { return iBackToBackRooms; }
		public void setBackToBackRooms(String backToBackRooms) { iBackToBackRooms = backToBackRooms; }

		public boolean isSaved() { return iSaved; }
		public void setSaved(boolean saved) { iSaved = saved; }
		
		public void setExpected(int expected) { iExpected = expected; }
		public void setExpected(double expected) { iExpected = (int)Math.round(expected); }
		public boolean hasExpected() { return iExpected != null; }
		public int getExpected() { return (iExpected == null ? 0 : iExpected); }
		public boolean isOfHighDemand() {
			return isAvailable() && !isUnlimited() && hasExpected() && getExpected() > getAvailableLimit();
		}
	}
}
