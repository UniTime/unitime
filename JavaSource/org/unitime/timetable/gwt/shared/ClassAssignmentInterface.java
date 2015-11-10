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
package org.unitime.timetable.gwt.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class ClassAssignmentInterface implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<CourseAssignment> iAssignments = new ArrayList<CourseAssignment>();
	private ArrayList<String> iMessages = null;
	private boolean iCanEnroll = true;
	private double iValue = 0.0;
	
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
	
	public boolean isCanEnroll() { return iCanEnroll; }
	public void setCanEnroll(boolean canEnroll) { iCanEnroll = canEnroll; }
	
	public double getValue() { return iValue; }
	public void setValue(double value) { iValue = value; }
	
	private CourseRequestInterface iRequest = null;
	
	public boolean hasRequest() { return iRequest != null; }
	public void setRequest(CourseRequestInterface request) { iRequest = request; }
	public CourseRequestInterface getRequest() { return iRequest; }
	
	public static class CourseAssignment implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iCourseId = null;
		private boolean iAssigned = true;
		private String iSubject, iCourseNbr, iTitle, iNote, iCreditText = null, iCreditAbbv = null;
		private boolean iHasUniqueName = true;
		private Integer iLimit = null, iProjected = null, iEnrollment = null, iLastLike = null;
		
		private ArrayList<String> iOverlaps = null;
		private boolean iNotAvailable = false, iLocked = false;
		private String iInstead;
		private boolean iWaitListed = false;
		private String iEnrollmentMessage = null;

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
		public String getCourseNbr(boolean includeTitle) {
			return getCourseNbr() + (includeTitle & hasTitle() ? " - " + getTitle() : "");
		}

		public String getCourseName() {
			return isFreeTime() ? "Free Time" : getSubject() + " " + getCourseNbr();
		}

		public String getCourseNameWithTitle() {
			return isFreeTime() ? "Free Time" : hasTitle() ? getSubject() + " " + getCourseNbr() + " - " + getTitle() : getSubject() + " " + getCourseNbr();
		}

		public boolean equalsIgnoreCase(String requestedCourse) {
			return getCourseName().equalsIgnoreCase(requestedCourse) || getCourseNameWithTitle().equalsIgnoreCase(requestedCourse);
		}

		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		public boolean hasTitle() { return iTitle != null && !iTitle.isEmpty(); }

		public String getNote() { return iNote; }
		public void setNote(String note) { iNote = note; }
		
		public boolean hasCredit() { return iCreditAbbv != null && !iCreditAbbv.isEmpty(); }
		public String getCreditText() { return iCreditText; }
		public void setCreditText(String creditText) { iCreditText = creditText; }
		public String getCreditAbbv() { return iCreditAbbv; }
		public void setCreditAbbv(String creditAbbv) { iCreditAbbv = creditAbbv; }
		public String getCredit() { return hasCredit() ? getCreditAbbv() + "|" + getCreditText() : null; }
		
		public float guessCreditCount() {
			if (!hasCredit()) return 0f;
			MatchResult m = RegExp.compile("\\d+\\.?\\d*").exec(getCreditAbbv());
			if (m != null) return Float.parseFloat(m.getGroup(0));
			return 0f;
		}

		public boolean hasUniqueName() { return iHasUniqueName; }
		public void setHasUniqueName(boolean hasUniqueName) { iHasUniqueName = hasUniqueName; }

		public void addOverlap(String overlap) {
			if (iOverlaps == null) iOverlaps = new ArrayList<String>();
			if (!iOverlaps.contains(overlap))
				iOverlaps.add(overlap);
		}
		public ArrayList<String> getOverlaps() { return iOverlaps; }
		
		public boolean isNotAvailable() { return iNotAvailable; }
		public void setNotAvailable(boolean notAvailable) { iNotAvailable = notAvailable; }
		
		public boolean isLocked() { return iLocked; }
		public void setLocked(boolean locked) { iLocked = locked; }

		public void setInstead(String instead) { iInstead = instead; }
		public String getInstead() { return iInstead; }
		
		public ArrayList<ClassAssignment> getClassAssignments() { return iAssignments; }
		public ClassAssignment addClassAssignment() { 
			ClassAssignment a = new ClassAssignment(this);
			iAssignments.add(a);
			return a;
		}
		
		public Integer getLimit() { return iLimit; }
		public void setLimit(Integer limit) { iLimit = limit; }
		public String getLimitString() {
			if (iLimit == null)  return "";
			if (iLimit < 0) return "&infin;";
			return iLimit.toString();
		}
		
		public Integer getProjected() { return iProjected; }
		public void setProjected(Integer projected) { iProjected = projected; }
		public String getProjectedString() {
			if (iProjected == null || iProjected == 0)  return "";
			if (iProjected < 0) return "&infin;";
			return iProjected.toString();
		}

		public Integer getLastLike() { return iLastLike; }
		public void setLastLike(Integer lastLike) { iLastLike = lastLike; }
		public String getLastLikeString() {
			if (iLastLike == null || iLastLike == 0)  return "";
			if (iLastLike < 0) return "&infin;";
			return iLastLike.toString();
		}
		
		public Integer getEnrollment() { return iEnrollment; }
		public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }
		public String getEnrollmentString() {
			if (iEnrollment == null || iEnrollment == 0)  return "";
			if (iEnrollment < 0) return "&infin;";
			return iEnrollment.toString();
		}
		
		public boolean isWaitListed() { return iWaitListed; }
		public void setWaitListed(boolean waitListed) { iWaitListed = waitListed; }
		
		public String getEnrollmentMessage() { return iEnrollmentMessage; }
		public boolean hasEnrollmentMessage() { return iEnrollmentMessage != null && !iEnrollmentMessage.isEmpty(); }
		public void setEnrollmentMessage(String message) { iEnrollmentMessage = message; }
		
		public String toString() {
			return (isFreeTime() ? "Free Time" : getSubject() + " " + getCourseNbr()) + ": " + (isAssigned() ? getClassAssignments() : "NOT ASSIGNED");
		}
	}
	
	public static class ClassAssignment implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private boolean iCourseAssigned = true;
		private Long iCourseId, iClassId, iSubpartId;
		private ArrayList<Integer> iDays = new ArrayList<Integer>();
		private int iStart, iLength, iBreakTime = 0;
		private ArrayList<String> iInstructos = new ArrayList<String>();
		private ArrayList<String> iInstructoEmails = new ArrayList<String>();
		private ArrayList<String> iRooms = new ArrayList<String>();
		private boolean iAlternative = false, iHasAlternatives = true, iDistanceConflict = false;
		private String iDatePattern = null;
		private String iSubject, iCourseNbr, iSubpart, iSection, iParentSection, iNumber, iTitle;
		private int[] iLimit = null;
		private boolean iPin = false;
		private int iBackToBackDistance = 0;
		private String iBackToBackRooms = null;
		private boolean iSaved = false, iDummy = false, iCancelled = false;
		private Integer iExpected = null;
		private String iOverlapNote = null;
		private String iNote = null;
		private String iCredit = null;
		private String iError = null;
		
		public ClassAssignment() {}
		public ClassAssignment(CourseAssignment course) {
			iCourseId = course.getCourseId();
			iSubject = course.getSubject();
			iCourseNbr = course.getCourseNbr();
			iCourseAssigned = course.isAssigned();
			iTitle = course.getTitle();
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
		public String getCourseNbr(boolean includeTitle) {
			return getCourseNbr() + (includeTitle & hasTitle() ? " - " + getTitle() : "");
		}
		
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		public boolean hasTitle() { return iTitle != null && !iTitle.isEmpty(); }
		
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
		public String getDaysString(String[] shortDays, String separator) {
			if (iDays == null) return "";
			String ret = "";
			for (int day: iDays)
				ret += (ret.isEmpty() ? "" : separator) + shortDays[day];
			return ret;
		}
		public String getDaysString(String[] shortDays) {
			return getDaysString(shortDays, "");
		}
		public boolean isAssigned() { return iDays != null && !iDays.isEmpty(); }
		
		public int getStart() { return iStart; }
		public void setStart(int start) { iStart = start; }
		public String getStartString(boolean useAmPm) {
			if (!isAssigned()) return "";
	        int h = iStart / 12;
	        int m = 5 * (iStart % 12);
	        if (useAmPm)
	        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
	        else
				return h + ":" + (m < 10 ? "0" : "") + m;
		}
		public String getStartStringAria(boolean useAmPm) {
			if (!isAssigned()) return "";
	        int h = iStart / 12;
	        int m = 5 * (iStart % 12);
	        if (useAmPm)
	        	return (h > 12 ? h - 12 : h) + (m == 0 ? "" : (m < 10 ? " 0" : " ") + m) + (h == 24 ? " AM" : h >= 12 ? " PM" : " AM");
	        else
				return h + ":" + (m < 10 ? "0" : "") + m;
		}
		
		public int getLength() { return iLength; }
		public void setLength(int length) { iLength = length; }
		public String getEndString(boolean useAmPm) {
			if (!isAssigned()) return "";
			int h = (5 * (iStart + iLength) - iBreakTime) / 60;
			int m = (5 * (iStart + iLength) - iBreakTime) % 60;
			if (useAmPm)
				return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
			else
				return h + ":" + (m < 10 ? "0" : "") + m;
		}
		public String getEndStringAria(boolean useAmPm) {
			if (!isAssigned()) return "";
			int h = (5 * (iStart + iLength) - iBreakTime) / 60;
			int m = (5 * (iStart + iLength) - iBreakTime) % 60;
	        if (useAmPm)
	        	return (h > 12 ? h - 12 : h) + (m == 0 ? "" : (m < 10 ? " 0" : " ") + m) + (h == 24 ? " AM" : h >= 12 ? " PM" : " AM");
	        else
				return h + ":" + (m < 10 ? "0" : "") + m;
		}
		
		public String getTimeString(String[] shortDays, boolean useAmPm, String arrangeHours) {
			if (!isAssigned()) return (iClassId == null ? "" : arrangeHours);
			return getDaysString(shortDays) + " " + getStartString(useAmPm) + " - " + getEndString(useAmPm);
		}
		
		public String getTimeStringAria(String[] longDays, boolean useAmPm, String arrangeHours) {
			if (!isAssigned()) return (iClassId == null ? "" : arrangeHours);
	        int h = iStart / 12;
	        int m = 5 * (iStart % 12);
	        String ret = getDaysString(longDays, " ") + " from ";
	        if (useAmPm)
	        	ret += (h > 12 ? h - 12 : h) + (m == 0 ? "" : (m < 10 ? " 0" : " ") + m) + (h == 24 ? " AM" : h >= 12 ? " PM" : " AM");
	        else
	        	ret += h + " " + (m < 10 ? "0" : "") + m;
	        h = (iStart + iLength) / 12;
			m = 5 * ((iStart + iLength) % 12);
			ret += " to ";
	        if (useAmPm)
	        	ret += (h > 12 ? h - 12 : h) + (m == 0 ? "" : (m < 10 ? " 0" : " ") + m) + (h == 24 ? " AM" : h >= 12 ? " PM" : " AM");
	        else
	        	ret += h + " " + (m < 10 ? "0" : "") + m;
	        return ret;  
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
		public void addInstructoEmail(String instructorEmail) {
			if (iInstructoEmails == null) iInstructoEmails = new ArrayList<String>();
			iInstructoEmails.add(instructorEmail == null ? "" : instructorEmail);
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
			return (iLimit[1] > iLimit[0] ? iLimit[1] - iLimit[0] : 0) + " / " + iLimit[1];
		}
		public boolean isAvailable() {
			if (iLimit == null) return true;
			if (iLimit[0] < 0) return (iLimit[1] != 0);
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
		
		public boolean isDummy() { return iDummy; }
		public void setDummy(boolean dummy) { iDummy = dummy; }
		
		public boolean isCancelled() { return iCancelled; }
		public void setCancelled(boolean cancelled) { iCancelled = cancelled; }
		
		public void setError(String error) { iError = error; }
		public boolean hasError() { return iError != null && !iError.isEmpty(); }
		public String getError() { return iError; }
		
		public void setExpected(Integer expected) { iExpected = expected; }
		public boolean hasExpected() { return iExpected != null; }
		public int getExpected() { return (iExpected == null ? 0 : iExpected); }
		public boolean isOfHighDemand() {
			return isAvailable() && !isUnlimited() && hasExpected() && getExpected() + (isSaved() ? -1 : 0) >= getAvailableLimit();
		}
		
		public String toString() {
			return (isFreeTime() ? "Free Time" : getSubpart() + " " + getSection()) + 
					(isAssigned() ? " " + getTimeString(new String[] {"M","T","W","R","F","S","X"}, true, "") : "") +
					(hasRoom() ? " " + getRooms(",") : "") +
					(isSaved() || isPinned() || isOfHighDemand() || hasAlternatives() || hasDistanceConflict() || isUnlimited() ? "[" +
							(isSaved() ? "s" : "") + (isPinned() ? "p" : "") + (isOfHighDemand() ? "h" : "") + (hasAlternatives() ? "a" : "") +
							(hasDistanceConflict() ? "d" : "") + (isUnlimited() ? "u" : "") + (isCancelled() ? "c" : "") +
							"]" : "");
		}
		
		public String getClassNumber() { return iNumber; }
		public void setClassNumber(String number) { iNumber = number; }
		
		public boolean hasNote() {
			return iNote != null && !iNote.isEmpty();
		}
		public String getNote() { return (iNote == null ? "" : iNote); }
		public void setNote(String note) { iNote = note; }
		public void addNote(String note) {
			addNote(note, "\n");
		}
		public void addNote(String note, String separator) {
			if (note == null || note.isEmpty()) return;
			if (iNote == null || iNote.isEmpty())
				iNote = note;
			else {
				if (separator == null) {
					if (iNote.endsWith(".") || iNote.endsWith(","))
						iNote += " ";
					else
						iNote += "; ";
				} else {
					iNote += separator;
				}
				iNote += note;
			}
		}
		
		public void setOverlapNote(String note) { iOverlapNote = note; }
		public boolean hasOverlapNote() { return iOverlapNote != null && !iOverlapNote.isEmpty(); }
		public String getOverlapNote() { return iOverlapNote; }
		
		public String getOverlapAndNote(String overlapStyle) {
			String ret = "";
			if (hasOverlapNote()) {
				ret += (overlapStyle != null ? "<span class='" + overlapStyle + "'>" + getOverlapNote() + "</span>" : getOverlapNote());
			}
			if (hasNote()) {
				if (!ret.isEmpty()) ret += (overlapStyle == null ? "\n" : "<br>");
				ret += (overlapStyle == null ? getNote() : getNote().replace("\n", "<br>"));
			}
			return ret;
		}
		
		public boolean hasCredit() {
			return iCredit != null && !iCredit.isEmpty();
		}
		public String getCredit() { return (iCredit == null ? "" : iCredit); }
		public void setCredit(String credit) { iCredit = credit; }
		
		public float guessCreditCount() {
			if (!hasCredit()) return 0f;
			MatchResult m = RegExp.compile("\\d+\\.?\\d*").exec(getCredit());
			if (m != null) return Float.parseFloat(m.getGroup(0));
			return 0f;
		}

		public String getCourseName() {
			return isFreeTime() ? "Free Time" : getSubject() + " " + getCourseNbr();
		}

		public String getCourseNameWithTitle() {
			return isFreeTime() ? "Free Time" : hasTitle() ? getSubject() + " " + getCourseNbr() + " - " + getTitle() : getSubject() + " " + getCourseNbr();
		}

		public boolean equalsIgnoreCase(String requestedCourse) {
			return getCourseName().equalsIgnoreCase(requestedCourse) || getCourseNameWithTitle().equalsIgnoreCase(requestedCourse);
		}
	}
	
	public static class Student implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private long iId;
		private Long iSessionId = null;
		private String iExternalId, iName;
		private List<String> iArea, iClassification, iMajor, iGroup, iAccommodation;
		private boolean iCanShowExternalId = false;
		private boolean iCanUseAssitant = false, iCanRegister = false;
		
		public Student() {}

		public void setId(long id) { iId = id; }
		public long getId() { return iId; }
		
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getSessionId() { return iSessionId; }
		
		public String getExternalId() { return iExternalId; }
		public void setExternalId(String externalId) { iExternalId = externalId; }
		
		public void setCanShowExternalId(boolean canShowExternalId) { iCanShowExternalId = canShowExternalId; }
		public boolean isCanShowExternalId() { return iExternalId != null && iCanShowExternalId; }
		
		public void setCanUseAssistant(boolean canUseAssistant) { iCanUseAssitant = canUseAssistant; }
		public boolean isCanUseAssistant() { return iCanUseAssitant; }
		
		public void setCanRegister(boolean canRegister) { iCanRegister = canRegister; }
		public boolean isCanRegister() { return iCanRegister; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public boolean hasArea() { return iArea != null && !iArea.isEmpty(); }
		public String getArea(String delim) { 
			if (iArea == null) return "";
			String ret = "";
			for (String area: iArea) {
				if (!ret.isEmpty()) ret += delim;
				ret += area;
			}
			return ret;
		}
		public void addArea(String area) {
			if (iArea == null) iArea = new ArrayList<String>();
			iArea.add(area);
		}
		public List<String> getAreas() { return iArea; }
		
		public boolean hasClassification() { return iClassification != null && !iClassification.isEmpty(); }
		public String getClassification(String delim) { 
			if (iClassification == null) return "";
			String ret = "";
			for (String classification: iClassification) {
				if (!ret.isEmpty()) ret += delim;
				ret += classification;
			}
			return ret;
		}
		public void addClassification(String classification) {
			if (iClassification == null) iClassification = new ArrayList<String>();
			iClassification.add(classification);
		}
		public List<String> getClassifications() { return iClassification; }

		public boolean hasMajor() { return iMajor != null && !iMajor.isEmpty(); }
		public String getMajor(String delim) { 
			if (iMajor == null) return "";
			String ret = "";
			for (String major: iMajor) {
				if (!ret.isEmpty()) ret += delim;
				ret += major;
			}
			return ret;
		}
		public void addMajor(String major) {
			if (iMajor == null) iMajor = new ArrayList<String>();
			iMajor.add(major);
		}
		public List<String> getMajors() { return iMajor; }
		
		public boolean hasGroup() { return iGroup != null && !iGroup.isEmpty(); }
		public String getGroup(String delim) { 
			if (iGroup == null) return "";
			String ret = "";
			for (String group: iGroup) {
				if (!ret.isEmpty()) ret += delim;
				ret += group;
			}
			return ret;
		}
		public void addGroup(String group) {
			if (iGroup == null) iGroup = new ArrayList<String>();
			iGroup.add(group);
		}
		public List<String> getGroups() { return iGroup; }
		
		public boolean hasAccommodation() { return iAccommodation != null && !iAccommodation.isEmpty(); }
		public String getAccommodation(String delim) { 
			if (iAccommodation == null) return "";
			String ret = "";
			for (String accommodation: iAccommodation) {
				if (!ret.isEmpty()) ret += delim;
				ret += accommodation;
			}
			return ret;
		}
		public void addAccommodation(String accommodation) {
			if (iAccommodation == null) iAccommodation = new ArrayList<String>();
			iAccommodation.add(accommodation);
		}
		public List<String> getAccommodations() { return iAccommodation; }
		
		public String getCurriculum(String delim) {
			if (!hasArea()) return "";
			String ret = "";
			for (int i = 0; i < iArea.size(); i++) {
				if (!ret.isEmpty()) ret += delim;
				ret += iArea.get(i) + " " + iClassification.get(i);
				if (iMajor != null && i < iMajor.size())
					ret += " " + iMajor.get(i);
			}
			return ret;
		}

		public String getAreaClasf(String delim) {
			if (!hasArea()) return "";
			String ret = "";
			for (int i = 0; i < iArea.size(); i++) {
				if (!ret.isEmpty()) ret += delim;
				ret += iArea.get(i) + " " + iClassification.get(i);
			}
			return ret;
		}
	}
	public static class Enrollment implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Student iStudent;
		private CourseAssignment iCourse = null;
		private int iPriority = 0;
		private String iAlternative = null;
		private Date iRequestedDate = null, iEnrolledDate = null, iApprovedDate = null;
		private String iReservation = null;
		private String iApprovedBy = null;
		private List<Conflict> iConflicts = null;
		private Boolean iWaitList = null; 
		private String iEnrollmentMessage = null;
		
		public Enrollment() {}
		
		public Student getStudent() { return iStudent; }
		public void setStudent(Student student) { iStudent = student; }
		
		public CourseAssignment getCourse() { return iCourse; }
		public void setCourse(CourseAssignment course) { iCourse = course; }
		
		public int getPriority() { return iPriority; }
		public void setPriority(int priority) { iPriority = priority; }
		
		public boolean isAlternative() { return iAlternative != null; }
		public void setAlternative(String course) { iAlternative = course; }
		public String getAlternative() { return (iAlternative == null ? "" : iAlternative); }
		
		public Date getRequestedDate() { return iRequestedDate; }
		public void setRequestedDate(Date ts) { iRequestedDate = ts; }

		public Date getEnrolledDate() { return iEnrolledDate; }
		public void setEnrolledDate(Date ts) { iEnrolledDate = ts; }
		
		public Date getApprovedDate() { return iApprovedDate; }
		public void setApprovedDate(Date ts) { iApprovedDate = ts; }
		public String getApprovedBy() { return iApprovedBy; }
		public void setApprovedBy(String approvedBy) { iApprovedBy = approvedBy; }
		
		public boolean hasWaitList() { return iWaitList != null; }
		public boolean isWaitList() { return iWaitList != null && iWaitList.booleanValue(); }
		public void setWaitList(Boolean waitList) { iWaitList = waitList; }
		
		public String getClasses(String subpart, String delim, boolean showClassNumbers) {
			if (getCourse() == null || getCourse().getClassAssignments().isEmpty()) return "";
			String ret = "";
			TreeSet<String> sections = new TreeSet<String>();
			for (ClassAssignment c: getCourse().getClassAssignments()) {
				if (subpart.equals(c.getSubpart()))
					sections.add(showClassNumbers && c.getClassNumber() != null ? c.getClassNumber() : c.getSection());
			}
			for (String section: sections) {
				if (!ret.isEmpty()) ret += delim;
				ret += section;
			}
			return ret;
		}
		
		public boolean hasClasses() {
			return getCourse() != null && !getCourse().getClassAssignments().isEmpty();
		}
		
		public List<ClassAssignment> getClasses() {
			return getCourse() == null ? null : getCourse().getClassAssignments();
		}
		
		public Long getCourseId() {
			return getCourse() == null ? null : getCourse().getCourseId();
		}

		public String getCourseName() {
			return getCourse() == null ? null : getCourse().getCourseName();
		}

		public String getReservation() { return iReservation; }
		public void setReservation(String reservation) { iReservation = reservation; }
		
		public boolean hasConflict() { return iConflicts != null && !iConflicts.isEmpty(); }
		public void addConflict(Conflict conflict) {
			if (iConflicts == null) iConflicts = new ArrayList<ClassAssignmentInterface.Conflict>();
			iConflicts.add(conflict);
		}
		public List<Conflict> getConflicts() { return iConflicts; }
		
		public String getEnrollmentMessage() { return iEnrollmentMessage; }
		public boolean hasEnrollmentMessage() { return iEnrollmentMessage != null && !iEnrollmentMessage.isEmpty(); }
		public void setEnrollmentMessage(String message) { iEnrollmentMessage = message; }
	}
	
	public static class Conflict implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private String iName, iType, iDate, iTime, iRoom, iStyle;
		
		public Conflict() {}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		
		public String getDate() { return iDate; }
		public void setDate(String date) { iDate = date; }
		
		public String getTime() { return iTime; }
		public void setTime(String time) { iTime = time; }
		
		public String getRoom() { return iRoom; }
		public void setRoom(String room) { iRoom = room; }
		
		public boolean hasStyle() { return iStyle != null && !iStyle.isEmpty(); }
		public String getStyle() { return iStyle; }
		public void setStyle(String style) { iStyle = style; }
		
		@Override
		public String toString() {
			return getName() + " " + getType() + " " + getDate() + " " + getTime() + " " + getRoom();
		}
	}
	
	public static class EnrollmentInfo implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private String iArea, iMajor, iClassification;
		private String iSubject, iCourseNbr, iConfig, iSubpart, iClazz, iTitle, iConsent;
		private Long iCourseId, iOfferingId, iSubjectId, iConfigId, iSubpartId, iClazzId;
		private Integer iLimit, iOther, iProjection, iEnrollment, iWaitlist, iReservation, iAvailable, iUnassigned;
		private Integer iTotalEnrollment, iTotalWaitlist, iTotalReservation, iTotalUnassigned;
		private Integer iConsentNeeded, iTotalConsentNeeded;
		private ClassAssignment iAssignment;
		private int iLevel = 0;
		
		public EnrollmentInfo() {}
		
		public String getArea() { return iArea; }
		public void setArea(String area) { iArea = area; }
		
		public String getMajor() { return iMajor; }
		public void setMajor(String major) { iMajor = major; }
		
		public String getClassification() { return iClassification; }
		public void setClassification(String classification) { iClassification = classification; }
		
		public String getSubject() { return iSubject; }
		public void setSubject(String subject) { iSubject = subject; }
		
		public String getCourseNbr() { return iCourseNbr; }
		public void setCourseNbr(String courseNbr) { iCourseNbr = courseNbr; }
		
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }

		public String getConsent() { return iConsent; }
		public void setConsent(String consent) { iConsent = consent; }

		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		
		public Long getOfferingId() { return iOfferingId; }
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }

		public Long getSubjectId() { return iSubjectId; }
		public void setSubjectId(Long subjectId) { iSubjectId = subjectId; }
		
		public String getConfig() { return iConfig; }
		public void setConfig(String config) { iConfig = config; }
		
		public Long getConfigId() { return iConfigId; }
		public void setConfigId(Long configId) { iConfigId = configId; }
		
		public String getSubpart() { return iSubpart; }
		public void setSubpart(String subpart) { iSubpart = subpart; }
		
		public Long getSubpartId() { return iSubpartId; }
		public void setSubpartId(Long subpartId) { iSubpartId = subpartId; }
		
		public String getClazz() { return iClazz; }
		public void setClazz(String clazz) { iClazz = clazz; }
		
		public Long getClazzId() { return iClazzId; }
		public void setClazzId(Long clazzId) { iClazzId = clazzId; }
		
		public Integer getLimit() { return iLimit; }
		public void setLimit(Integer limit) { iLimit = limit; }
		public boolean hasLimit() { return iLimit != null; }
		
		public Integer getOther() { return iOther; }
		public void setOther(Integer other) { iOther = other; }
		public boolean hasOther() { return iOther != null; }

		public Integer getEnrollment() { return iEnrollment; }
		public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }
		public boolean hasEnrollment() { return iEnrollment != null; }
		
		public Integer getProjection() { return iProjection ; }
		public void setProjection(Integer projection) { iProjection = projection; }
		public boolean hasProjection() { return iProjection != null; }
		
		public Integer getWaitlist() { return iWaitlist; }
		public void setWaitlist(Integer waitlist) { iWaitlist = waitlist; }
		public boolean hasWaitlist() { return iWaitlist != null; }
		
		public Integer getUnassigned() { return iUnassigned; }
		public void setUnassigned(Integer unassigned) { iUnassigned = unassigned; }
		public boolean hasUnassigned() { return iUnassigned != null; }

		public Integer getReservation() { return iReservation; }
		public void setReservation(Integer reservation) { iReservation = reservation; }
		public boolean hasReservation() { return iReservation !=null; }

		public Integer getTotalEnrollment() { return iTotalEnrollment; }
		public void setTotalEnrollment(Integer enrollment) { iTotalEnrollment = enrollment; }
		public boolean hasTotalEnrollment() { return iTotalEnrollment != null; }

		public Integer getTotalWaitlist() { return iTotalWaitlist; }
		public void setTotalWaitlist(Integer waitlist) { iTotalWaitlist = waitlist; }
		public boolean hasTotalWaitlist() { return iTotalWaitlist != null; }
		
		public Integer getTotalUnassigned() { return iTotalUnassigned; }
		public void setTotalUnassigned(Integer unassigned) { iTotalUnassigned = unassigned; }
		public boolean hasTotalUnassigned() { return iTotalUnassigned != null; }

		public Integer getTotalReservation() { return iTotalReservation; }
		public void setTotalReservation(Integer reservation) { iTotalReservation = reservation; }
		public boolean hasTotalReservation() { return iTotalReservation !=null; }
		
		public Integer getAvailable() { return iAvailable; }
		public void setAvailable(Integer available) { iAvailable = available; }
		public boolean hasAvailable() { return iAvailable !=null; }
		
		public void setAssignment(ClassAssignment assignment) { iAssignment = assignment; }
		public ClassAssignment getAssignment() { return iAssignment; }
		
		public Integer getConsentNeeded() { return iConsentNeeded; }
		public void setConsentNeeded(Integer consentNeeded) { iConsentNeeded = consentNeeded; }
		public int hasConsentNeeded() { return iConsentNeeded; }
		
		public Integer getTotalConsentNeeded() { return iTotalConsentNeeded; }
		public void setTotalConsentNeeded(Integer totalConsentNeeded) { iTotalConsentNeeded = totalConsentNeeded; }
		public int hasTotalConsentNeeded() { return iTotalConsentNeeded; }
		
		public int getLevel() { return iLevel; }
		public void setLevel(int level) { iLevel = level; }
		public void incLevel() { iLevel ++; }
		public String getIndent() {
			String indent = "";
			for (int i = 0; i < iLevel; i++) indent += "&nbsp;&nbsp;";
			return indent;
		}
	}
	
	public static class StudentInfo implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Student iStudent;
		private Integer iEnrollment, iWaitlist, iReservation, iRequested, iUnassigned;
		private Integer iTotalEnrollment, iTotalWaitlist, iTotalReservation, iTotalUnassigned;
		private Integer iConsentNeeded, iTotalConsentNeeded;
		private Integer iTopWaitingPriority;
		private Date iRequestedDate = null, iEnrolledDate = null, iApprovedDate = null, iEmailDate = null;
		private String iStatus;
		private Float iCredit, iTotalCredit;
		
		public StudentInfo() {}
		
		public Student getStudent() { return iStudent; }
		public void setStudent(Student student) { iStudent = student; }
		
		public Integer getRequested() { return iRequested; }
		public void setRequested(Integer requested) { iRequested = requested; }
		public boolean hasRequested() { return iRequested != null; }

		public Integer getEnrollment() { return iEnrollment; }
		public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }
		public boolean hasEnrollment() { return iEnrollment != null; }
		
		public Integer getWaitlist() { return iWaitlist; }
		public void setWaitlist(Integer waitlist) { iWaitlist = waitlist; }
		public boolean hasWaitlist() { return iWaitlist != null; }
		
		public Integer getUnassigned() { return iUnassigned; }
		public void setUnassigned(Integer unassigned) { iUnassigned = unassigned; }
		public boolean hasUnassigned() { return iUnassigned != null; }

		public Integer getReservation() { return iReservation; }
		public void setReservation(Integer reservation) { iReservation = reservation; }
		public boolean hasReservation() { return iReservation !=null; }
		
		public Integer getTotalEnrollment() { return iTotalEnrollment; }
		public void setTotalEnrollment(Integer enrollment) { iTotalEnrollment = enrollment; }
		public boolean hasTotalEnrollment() { return iTotalEnrollment != null; }
	
		public Integer getTotalWaitlist() { return iTotalWaitlist; }
		public void setTotalWaitlist(Integer waitlist) { iTotalWaitlist = waitlist; }
		public boolean hasTotalWaitlist() { return iTotalWaitlist != null; }
		
		public Integer getTotalUnassigned() { return iTotalUnassigned; }
		public void setTotalUnassigned(Integer unassigned) { iTotalUnassigned = unassigned; }
		public boolean hasTotalUnassigned() { return iTotalUnassigned != null; }
		
		public Integer getTotalReservation() { return iTotalReservation; }
		public void setTotalReservation(Integer reservation) { iTotalReservation = reservation; }
		public boolean hasTotalReservation() { return iTotalReservation !=null; }
		
		public Integer getConsentNeeded() { return iConsentNeeded; }
		public void setConsentNeeded(Integer consentNeeded) { iConsentNeeded = consentNeeded; }
		public int hasConsentNeeded() { return iConsentNeeded; }
		
		public Integer getTotalConsentNeeded() { return iTotalConsentNeeded; }
		public void setTotalConsentNeeded(Integer totalConsentNeeded) { iTotalConsentNeeded = totalConsentNeeded; }
		public int hasTotalConsentNeeded() { return iTotalConsentNeeded; }

		public Integer getTopWaitingPriority() { return iTopWaitingPriority; }
		public void setTopWaitingPriority(Integer topWaitingPriority) { iTopWaitingPriority = topWaitingPriority; }
		public int hasTopWaitingPriority() { return iTopWaitingPriority; }
		
		public Date getRequestedDate() { return iRequestedDate; }
		public void setRequestedDate(Date ts) { iRequestedDate = ts; }

		public Date getEnrolledDate() { return iEnrolledDate; }
		public void setEnrolledDate(Date ts) { iEnrolledDate = ts; }
		
		public Date getApprovedDate() { return iApprovedDate; }
		public void setApprovedDate(Date ts) { iApprovedDate = ts; }
		
		public Date getEmailDate() { return iEmailDate; }
		public void setEmailDate(Date ts) { iEmailDate = ts; }

		public String getStatus() { return iStatus; }
		public void setStatus(String status) { iStatus = status; }
		
		public boolean hasCredit() { return iCredit != null && iCredit > 0; }
		public void setCredit(Float credit) { iCredit = credit; }
		public Float getCredit() { return iCredit; }

		public boolean hasTotalCredit() { return iTotalCredit != null && iTotalCredit > 0; }
		public void setTotalCredit(Float totalCredit) { iTotalCredit = totalCredit; }
		public Float getTotalCredit() { return iTotalCredit; }
}

	public static class SectioningAction implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Student iStudent;
		private Date iTimeStamp;
		private String iOperation;
		private String iUser;
		private String iMessage;
		private String iProto;
		private String iResult;
		private Long iCpuTime;
		
		public SectioningAction() {
		}

		public Student getStudent() { return iStudent; }
		public void setStudent(Student student) { iStudent = student; }
		
		public Date getTimeStamp() { return iTimeStamp; }
		public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }
		
		public String getOperation() { return iOperation; }
		public void setOperation(String operation) { iOperation = operation; }
		
		public String getUser() { return iUser; }
		public void setUser(String user) { iUser = user; }
		
		public String getMessage() { return iMessage; }
		public void setMessage(String message) { iMessage = message; }
		
		public String getResult() { return iResult; }
		public void setResult(String result) { iResult = result; }
		
		public Long getCpuTime() { return iCpuTime; }
		public void setCpuTime(Long cpuTime) { iCpuTime = cpuTime; }

		public String getProto() { return iProto; }
		public void setProto(String proto) { iProto = proto; }
}
	
}
