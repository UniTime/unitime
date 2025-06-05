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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradeMode;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationOperation;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationStatus;
import org.unitime.timetable.gwt.shared.TableInterface.NaturalOrderComparator;

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
	private ArrayList<ErrorMessage> iErrors = null;
	private Set<Note> iNotes = null;
	private Set<RetrieveSpecialRegistrationResponse> iSpecialRegistrations = null;
	private boolean iCanEnroll = true;
	private boolean iCanSetCriticalOverrides = false;
	private double iValue = 0.0;
	private Float iCurrentCredit = null;
	
	public ClassAssignmentInterface() {}
	
	public ArrayList<CourseAssignment> getCourseAssignments() { return iAssignments; }
	public void add(CourseAssignment a) { iAssignments.add(a); }
	public List<ClassAssignment> getClassAssignments() {
		List<ClassAssignment> ret = new ArrayList<ClassAssignment>();
		for (CourseAssignment a: iAssignments)
			ret.addAll(a.getClassAssignments());
		return ret;
	}
	
	public void clear() {
		iAssignments.clear();
		if (iMessages != null) iMessages.clear();
		if (iErrors != null) iErrors.clear();
	}
	
	public void addMessage(String message) {
		if (iMessages == null) iMessages = new ArrayList<String>();
		iMessages.add(message);
	}
	public boolean hasMessages() {
		return iMessages != null && !iMessages.isEmpty();
	}
	public boolean isError() {
		for (CourseAssignment a: iAssignments)
			for (ClassAssignment ca: a.getClassAssignments())
				if (ca != null && ca.hasError()) return true;
		return false;
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
	
	public void addError(ErrorMessage error) {
		if (iErrors == null) iErrors = new ArrayList<ErrorMessage>();
		iErrors.add(error);
	}
	public void setErrors(Collection<ErrorMessage> errors) {
		if (iErrors == null) {
			iErrors = new ArrayList<ErrorMessage>();
		} else {
			iErrors.clear();
		}
		iErrors.addAll(errors);
	}
	public boolean hasErrors() {
		return iErrors != null && !iErrors.isEmpty();
	}
	public ArrayList<ErrorMessage> getErrors() { return iErrors; }
	
	public boolean isCanEnroll() { return iCanEnroll; }
	public void setCanEnroll(boolean canEnroll) { iCanEnroll = canEnroll; }
	
	public boolean isCanSetCriticalOverrides() { return iCanSetCriticalOverrides; }
	public void setCanSetCriticalOverrides(boolean canSetCriticalOverrides) { iCanSetCriticalOverrides = canSetCriticalOverrides; }
	
	public double getValue() { return iValue; }
	public void setValue(double value) { iValue = value; }
	
	private CourseRequestInterface iRequest = null, iAdvisorRequest = null;
	private Set<Long> iAdvisorWaitListedCourseIds = null;
	
	public boolean hasRequest() { return iRequest != null; }
	public void setRequest(CourseRequestInterface request) { iRequest = request; }
	public CourseRequestInterface getRequest() { return iRequest; }
	
	public boolean hasAdvisorRequest() { return iAdvisorRequest != null && (!iAdvisorRequest.isEmpty() || iAdvisorRequest.hasCreditNote()); }
	public void setAdvisorRequest(CourseRequestInterface request) { iAdvisorRequest = request; }
	public CourseRequestInterface getAdvisorRequest() { return iAdvisorRequest; }
	
	public Set<Long> getAdvisorWaitListedCourseIds() { return iAdvisorWaitListedCourseIds; }
	public void setAdvisorWaitListedCourseIds(Set<Long> advisorWaitListedCourseIds) { iAdvisorWaitListedCourseIds = advisorWaitListedCourseIds; }
	
	public boolean isEnrolled() {
		for (CourseAssignment course: getCourseAssignments())
			if (course.isAssigned() && !course.isFreeTime() && !course.isTeachingAssignment()) return true;
		return false;
	}
	
	public boolean hasNotes() { return iNotes != null && !iNotes.isEmpty(); }
	public void addNote(Note note) {
		if (iNotes == null) iNotes = new TreeSet<Note>();
		iNotes.add(note);
	}
	public Set<Note> getNotes() { return iNotes; }
	
	public boolean hasSpecialRegistrations() { return iSpecialRegistrations != null && !iSpecialRegistrations.isEmpty(); }
	public void addSpecialRegistrations(RetrieveSpecialRegistrationResponse reg) {
		if (iSpecialRegistrations == null) iSpecialRegistrations = new TreeSet<RetrieveSpecialRegistrationResponse>();
		iSpecialRegistrations.add(reg);
	}
	public void setSpecialRegistrations(Collection<RetrieveSpecialRegistrationResponse> regs) {
		if (regs == null || regs.isEmpty()) return;
		iSpecialRegistrations = new TreeSet<RetrieveSpecialRegistrationResponse>(regs);
	}
	public Set<RetrieveSpecialRegistrationResponse> getSpecialRegistrations() { return iSpecialRegistrations; }
	
	public static class CourseAssignment implements IsSerializable, Serializable, Comparable<CourseAssignment> {
		private static final long serialVersionUID = 1L;
		private Long iCourseId = null;
		private boolean iAssigned = true, iTeachingAssigment = false;
		private String iSubject, iCourseNbr, iTitle, iNote, iCreditText = null, iCreditAbbv = null;
		private boolean iHasUniqueName = true, iHasCrossList = false;
		private Integer iLimit = null, iProjected = null, iEnrollment = null, iLastLike = null, iRequested = null, iSnapShotLimit = null;
		
		private ArrayList<String> iOverlaps = null;
		private boolean iNotAvailable = false, iFull = false, iLocked = false, iCanWaitList = false, iHasIncompReqs = false;
		private String iInstead;
		private String iEnrollmentMessage = null;
		private String iConflictMessage = null;
		private Date iRequestedDate = null;
		private Date iWaitListedDate = null;
		private Integer iSelection = null;
		private Float iOverMaxCredit;
		private ArrayList<CodeLabel> iOverrides = null;

		private ArrayList<ClassAssignment> iAssignments = new ArrayList<ClassAssignment>();
		private Set<IdValue> iInstructionalMethods = null;
		private boolean iHasNoInstructionalMethod = false;

		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public boolean isFreeTime() { return (iCourseId == null); }
		
		public boolean isAssigned() { return iAssigned; }
		public void setAssigned(boolean assigned) { iAssigned = assigned; }
		
		public boolean isTeachingAssignment() { return iTeachingAssigment; }
		public void setTeachingAssignment(boolean ta) { iTeachingAssigment = ta; }

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
		public boolean hasNote() { return iNote != null && !iNote.isEmpty(); }
		
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
		
		public float[] guessCreditRange() {
			if (!hasCredit()) return new float[] {0f, 0f};
			MatchResult r = RegExp.compile("(\\d+\\.?\\d*)-(\\d+\\.?\\d*)").exec(getCreditAbbv());
			if (r != null) return new float[] {Float.parseFloat(r.getGroup(1)), Float.parseFloat(r.getGroup(2))};
			float credit = guessCreditCount();
			return new float[] { credit, credit };
		}

		public boolean hasUniqueName() { return iHasUniqueName; }
		public void setHasUniqueName(boolean hasUniqueName) { iHasUniqueName = hasUniqueName; }
		
		public boolean hasCrossList() { return iHasCrossList; }
		public void setHasCrossList(boolean hasCrossList) { iHasCrossList = hasCrossList; }

		public void addOverlap(String overlap) {
			if (iOverlaps == null) iOverlaps = new ArrayList<String>();
			if (!iOverlaps.contains(overlap))
				iOverlaps.add(overlap);
		}
		public ArrayList<String> getOverlaps() { return iOverlaps; }
		
		public boolean isNotAvailable() { return iNotAvailable; }
		public void setNotAvailable(boolean notAvailable) { iNotAvailable = notAvailable; }
		
		public boolean isFull() { return iFull; }
		public void setFull(boolean full) { iFull = full; }

		public boolean hasConflictMessage() { return iConflictMessage != null && !iConflictMessage.isEmpty(); }
		public void setConflictMessage(String conflictMessage) { iConflictMessage = conflictMessage; }
		public String getConflictMessage() { return iConflictMessage; }

		public boolean hasHasIncompReqs() { return iHasIncompReqs; }
		public void setHasIncompReqs(boolean incompReqs) { iHasIncompReqs = incompReqs; }
		
		public boolean isOverMaxCredit() { return iOverMaxCredit != null; }
		public Float getOverMaxCredit() { return iOverMaxCredit; }
		public void setOverMaxCredit(Float maxCredit) { iOverMaxCredit = maxCredit; }
		
		public boolean isLocked() { return iLocked; }
		public void setLocked(boolean locked) { iLocked = locked; }
		
		public boolean isCanWaitList() { return iCanWaitList; }
		public void setCanWaitList(boolean waitList) { iCanWaitList = waitList; }
		
		public boolean hasOverrides() { return iOverrides != null && !iOverrides.isEmpty(); }
		public void addOverride(String overrideCode, String overrideLabel) {
			if (iOverrides == null) iOverrides = new ArrayList<CodeLabel>();
			iOverrides.add(new CodeLabel(overrideCode, overrideLabel));
		}
		public List<CodeLabel> getOverrides() { return iOverrides; }

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
		
		public Integer getSnapShotLimit() { return iSnapShotLimit; }
		public void setSnapShotLimit(Integer limit) { iSnapShotLimit = limit; }
		
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
		public void setAvailability(int[] availability) {
			iRequested = null;
			if (availability == null) {
				iEnrollment = null;
				iLimit = null;
			} else {
				iEnrollment = availability[0];
				iLimit = availability[1];
				if (availability.length > 2)
					iRequested = availability[2];
			}
		}
		
		public String getEnrollmentMessage() { return iEnrollmentMessage; }
		public boolean hasEnrollmentMessage() { return iEnrollmentMessage != null && !iEnrollmentMessage.isEmpty(); }
		public void setEnrollmentMessage(String message) { iEnrollmentMessage = message; }
		
		public boolean hasInstructionalMethods() { return iInstructionalMethods != null && !iInstructionalMethods.isEmpty(); }
		public Set<IdValue> getInstructionalMethods() { return iInstructionalMethods; }
		public void addInstructionalMethod(Long id, String value) {
			if (iInstructionalMethods == null)
				iInstructionalMethods = new TreeSet<IdValue>();
			iInstructionalMethods.add(new IdValue(id, value));
		}
		public boolean isHasNoInstructionalMethod() { return iHasNoInstructionalMethod; }
		public void setHasNoInstructionalMethod(boolean hasNoInstructionalMethod) { iHasNoInstructionalMethod = hasNoInstructionalMethod; }
		public boolean hasInstructionalMethodSelection() {
			if (hasInstructionalMethods()) {
				return getInstructionalMethods().size() + (isHasNoInstructionalMethod() ? 1 : 0) > 1;
			} else {
				return false;
			}
		}
		
		public String toString() {
			return (isFreeTime() ? "Free Time" : getSubject() + " " + getCourseNbr()) + ": " + (isAssigned() ? getClassAssignments() : "NOT ASSIGNED");
		}
		
		public Date getRequestedDate() { return iRequestedDate; }
		public void setRequestedDate(Date ts) { iRequestedDate = ts; }
		
		public Date getWaitListedDate() { return iWaitListedDate; }
		public void setWaitListedDate(Date ts) { iWaitListedDate = ts; }
		
		public Integer getRequested() { return iRequested; }
		public void setRequested(Integer requested) { iRequested = requested; }
		public Integer getSelection() { return iSelection; }
		public void setSelection(Integer selection) { iSelection = selection; }
		public boolean hasSelection() { return iSelection != null; }

		@Override
		public int compareTo(CourseAssignment c) {
			if (hasSelection()) {
				if (c.hasSelection()) {
					int cmp = getSelection().compareTo(c.getSelection());
					if (cmp != 0) return cmp;
				} else {
					return -1;
				}
			} else if (c.hasSelection()) { return 1; }
			return getCourseName().compareTo(c.getCourseName());
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
		private ArrayList<IdValue> iRooms = new ArrayList<IdValue>();
		private boolean iAlternative = false, iHasAlternatives = true, iDistanceConflict = false, iTeachingAssigment = false, iInstructing = false;
		private String iDatePattern = null;
		private String iSubject, iCourseNbr, iSubpart, iSection, iParentSection, iNumber, iTitle;
		private int[] iLimit = null;
		private Boolean iAvailable = null;
		private boolean iPin = false;
		private int iBackToBackDistance = 0;
		private String iBackToBackRooms = null;
		private boolean iSaved = false, iDummy = false, iCancelled = false;
		private Integer iExpected = null;
		private String iOverlapNote = null;
		private String iNote = null;
		private String iCredit = null;
		private String iError = null, iWarn = null, iInfo = null;
		private Date iEnrolledDate = null;
		private String iExternalId = null;
		private SpecialRegistrationStatus iSpecRegStatus = null;
		private SpecialRegistrationOperation iSpecRegOperation = null;
		private GradeMode iGradeMode = null;
		private Float iCreditHour = null, iCreditMin = null, iCreditMax = null;
		private Boolean iCanWaitList = null;
		private boolean iLongDistanceConflict = false;
		
		public ClassAssignment() {}
		public ClassAssignment(CourseAssignment course) {
			iCourseId = course.getCourseId();
			iSubject = course.getSubject();
			iCourseNbr = course.getCourseNbr();
			iCourseAssigned = course.isAssigned();
			iTitle = course.getTitle();
			iCanWaitList = course.isCanWaitList();
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
		
		public String getExternalId() { return iExternalId; }
		public void setExternalId(String extId) { iExternalId = extId; }
		
		public Preference getSelection() { return getSelection(false); }
		public Preference getSelection(boolean required) { return new Preference(iClassId, iSection.length() <= 4 ? iSubpart + " " + iSection : iSection, required); }

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
		public void addRoom(Long id, String name) {
			if (iRooms == null) iRooms = new ArrayList<IdValue>();
			iRooms.add(new IdValue(id, name));
		}
		public ArrayList<IdValue> getRooms() { return iRooms; }
		
		public String getRooms(String delim) {
			if (iRooms == null) return "";
			String ret = "";
			for (IdValue room: iRooms) {
				if (!ret.isEmpty()) ret += delim;
				ret += room.getValue();
			}
			return ret;
		}
		
		public boolean isUnlimited() { return iLimit != null && (iLimit[1] < 0 || iLimit[1] >= 9999); }
		public int[] getLimit() { return iLimit; }
		public void setLimit(int[] limit) { iLimit = limit; }
		public String getLimitString() {
			if (iLimit == null) return "";
			if (iLimit[1] >= 9999 || iLimit[1] < 0) return "&infin;";
			if (iLimit[0] < 0) return String.valueOf(iLimit[1]);
			return (iLimit[1] > iLimit[0] ? iLimit[1] - iLimit[0] : 0) + " / " + iLimit[1];
		}
		public boolean isAvailable() {
			if (iAvailable != null) return iAvailable;
			if (iLimit == null) return true;
			if (iLimit[1] < 0) return true;
			if (iLimit[0] < 0) return (iLimit[1] != 0);
			return iLimit[0] < iLimit[1];
		}
		public int getAvailableLimit() {
			if (iLimit == null) return 9999;
			if (iLimit[0] < 0) return 9999;
			return iLimit[1] - iLimit[0];
		}
		public void setAvailable(Boolean available) {
			iAvailable = available;
		}
		
		public boolean isPinned() { return iPin; }
		public void setPinned(boolean pin) { iPin = pin; }
		
		public boolean hasAlternatives() { return iHasAlternatives; }
		public void setHasAlternatives(boolean alternatives) { iHasAlternatives = alternatives; }
		
		public boolean isTeachingAssignment() { return iTeachingAssigment; }
		public void setTeachingAssignment(boolean ta) { iTeachingAssigment = ta; }
		
		public boolean isInstructing() { return iInstructing; }
		public void setInstructing(boolean instructing) { iInstructing = instructing; }
		
		public boolean hasDistanceConflict() { return iDistanceConflict; }
		public void setDistanceConflict(boolean distanceConflict) { iDistanceConflict = distanceConflict; }
		
		public boolean hasLongDistanceConflict() { return iLongDistanceConflict; }
		public void setLongDistanceConflict(boolean longDistanceConflict) { iLongDistanceConflict = longDistanceConflict; }
		
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
		public void addError(String error) { iError = (iError == null || iError.isEmpty() ? "" : iError + "\n") + error; }
		public boolean hasError() { return iError != null && !iError.isEmpty(); }
		public String getError() { return iError; }
		
		public void setWarn(String warn) { iWarn = warn; }
		public void addWarn(String warn) { iWarn = (iWarn == null || iWarn.isEmpty() ? "" : iWarn + "\n") + warn; }
		public boolean hasWarn() { return iWarn != null && !iWarn.isEmpty(); }
		public String getWarn() { return iWarn; }
		
		public void setInfo(String info) { iInfo = info; }
		public void addInfo(String info) { iInfo = (iInfo == null || iInfo.isEmpty() ? "" : iInfo + "\n") + info; }
		public boolean hasInfo() { return iInfo != null && !iInfo.isEmpty(); }
		public String getInfo() { return iInfo; }
		
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
			if (note == null || note.isEmpty()) return;
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
		
		public boolean equalsIgnoreCase(RequestedCourse requestedCourse) {
			if (requestedCourse == null || !requestedCourse.isCourse()) return false;
			if (requestedCourse.hasCourseId())
				return requestedCourse.getCourseId().equals(getCourseId());
			else
				return getCourseName().equalsIgnoreCase(requestedCourse.getCourseName()) || getCourseNameWithTitle().equalsIgnoreCase(requestedCourse.getCourseName());
		}
		
		public Date getEnrolledDate() { return iEnrolledDate; }
		public void setEnrolledDate(Date ts) { iEnrolledDate = ts; }
		
		public SpecialRegistrationStatus getSpecRegStatus() { return iSpecRegStatus; }
		public void setSpecRegStatus(SpecialRegistrationStatus status) { iSpecRegStatus = status; }
		
		public SpecialRegistrationOperation getSpecRegOperation() { return iSpecRegOperation; }
		public void setSpecRegOperation(SpecialRegistrationOperation operation) { iSpecRegOperation = operation; }
		
		public GradeMode getGradeMode() { return iGradeMode; }
		public void setGradeMode(GradeMode mode) { iGradeMode = mode; }
		
		public Float getCreditHour() { return iCreditHour; }
		public void setCreditHour(Float creditHour) { iCreditHour = creditHour; }
		
		public Float getCreditMin() { return iCreditMin; }
		public Float getCreditMax() { return iCreditMax; }
		public void setCreditRange(Float creditMin, Float creditMax) { iCreditMin = creditMin; iCreditMax = creditMax; }
		public boolean hasVariableCredit() { return iCreditMin != null && iCreditMax != null && iCreditMin < iCreditMax; }
		
		public boolean isCanWaitList() { return iCanWaitList != null && iCanWaitList.booleanValue(); }
		public void setCanWaitList(Boolean canWaitList) { iCanWaitList = canWaitList; }
	}
	
	public static class Group implements IsSerializable, Serializable, Comparable<Group> {
		private static final long serialVersionUID = 1L;
		private String iType;
		private String iName;
		private String iTitle;
		
		public Group() {}
		public Group(String type, String name, String title) {
			iType = type; iName = name; iTitle = title;
		}
		public Group(String name, String title) {
			this(null, name, title);
		}
		
		public String getType() { return iType; }
		public void setType(String type) { iType = type;}
		public boolean hasType() { return iType != null && !iType.isEmpty(); }
		public String getTypeNotNull() { return iType == null ? "" : iType; }
		public boolean sameType(String type) {
			if (!hasType())
				return type == null || type.isEmpty();
			else 
				return getType().equals(type); 
		}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		public boolean hasTitle() { return iTitle != null && !iTitle.isEmpty(); }
		@Override
		public int compareTo(Group g) {
			int cmp = getTypeNotNull().compareTo(g.getTypeNotNull());
			if (cmp != 0) return cmp;
			return getName().compareTo(g.getName());
		}
	}
	
	public static class CodeLabel implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private String iCode, iLabel;
		
		public CodeLabel() {}
		public CodeLabel(String code, String label) {
			iCode = code; iLabel = label;
		}
		
		public String getCode() { return iCode; }
		public void setCode(String code) { iCode = code; }
		public boolean hasCode() { return iCode != null && !iCode.isEmpty(); }
		public String getLabel() { return (iLabel == null || iLabel.isEmpty() ? iCode : iLabel); }
		public void setLable(String label) { iLabel = label; }
		public boolean hasLabel() { return iLabel != null && !iLabel.isEmpty(); }
		
		@Override
		public String toString() { return (iCode == null || iCode.isEmpty() ? "" : iCode); }
		public boolean isEmpty() { return iCode == null || iCode.isEmpty(); }
	}
	
	public static class Student implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private long iId;
		private Long iSessionId = null;
		private String iExternalId, iName, iEmail;
		private List<CodeLabel> iArea, iClassification, iMajor, iAccommodation, iMinor, iConcentration, iDegree, iProgram, iCampus;
		private String iDefaultCampus = null;
		private List<String> iAdvisor;
		private Set<Group> iGroups;
		private boolean iCanShowExternalId = false, iCanSelect = false;
		private boolean iCanUseAssitant = false, iCanRegister = false;
		private WaitListMode iMode = null;
		
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
		
		public void setCanSelect(boolean canSelect) { iCanSelect = canSelect; }
		public boolean isCanSelect() { return iCanSelect; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getEmail() { return iEmail; }
		public void setEmail(String email) { iEmail = email; }
		
		public boolean hasArea() { return iArea != null && !iArea.isEmpty(); }
		public String getArea(String delim) { 
			if (iArea == null) return "";
			String ret = "";
			for (CodeLabel area: iArea) {
				if (!ret.isEmpty()) ret += delim;
				ret += area.getCode();
			}
			return ret;
		}
		public void addArea(String area, String label) {
			if (iArea == null) iArea = new ArrayList<CodeLabel>();
			iArea.add(new CodeLabel(area, label));
		}
		public List<CodeLabel> getAreas() { return iArea; }
		
		public boolean hasClassification() { return iClassification != null && !iClassification.isEmpty(); }
		public String getClassification(String delim) { 
			if (iClassification == null) return "";
			String ret = "";
			for (CodeLabel classification: iClassification) {
				if (!ret.isEmpty()) ret += delim;
				ret += classification.getCode();
			}
			return ret;
		}
		public void addClassification(String classification, String label) {
			if (iClassification == null) iClassification = new ArrayList<CodeLabel>();
			iClassification.add(new CodeLabel(classification, label));
		}
		public List<CodeLabel> getClassifications() { return iClassification; }

		public boolean hasMajor() { return iMajor != null && !iMajor.isEmpty(); }
		public String getMajor(String delim) { 
			if (iMajor == null) return "";
			String ret = "";
			for (CodeLabel major: iMajor) {
				if (!ret.isEmpty()) ret += delim;
				ret += major.getCode();
			}
			return ret;
		}
		public void addMajor(String major, String label) {
			if (iMajor == null) iMajor = new ArrayList<CodeLabel>();
			iMajor.add(new CodeLabel(major, label));
		}
		public List<CodeLabel> getMajors() { return iMajor; }
		
		public boolean hasConcentration() {
			if (iConcentration == null || iConcentration.isEmpty()) return false;
			for (CodeLabel conc: iConcentration)
				if (!conc.isEmpty()) return true;
			return false;
		}
		public String getConcentration(String delim) { 
			if (iConcentration == null) return "";
			String ret = "";
			for (Iterator<CodeLabel> i = iConcentration.iterator(); i.hasNext(); ) {
				CodeLabel conc = i.next();
				if (conc.hasCode()) ret += conc.getCode();
				if (i.hasNext()) ret += delim;
			}
			return ret;
		}
		public void addConcentration(String conc, String label) {
			if (iConcentration == null) iConcentration = new ArrayList<CodeLabel>();
			iConcentration.add(new CodeLabel(conc, label));
		}
		public List<CodeLabel> getConcentrations() { return iConcentration; }
		
		public boolean hasDegree() {
			if (iDegree == null || iDegree.isEmpty()) return false;
			for (CodeLabel degr: iDegree)
				if (!degr.isEmpty()) return true;
			return false;
		}
		public String getDegree(String delim) { 
			if (iDegree == null) return "";
			String ret = "";
			for (Iterator<CodeLabel> i = iDegree.iterator(); i.hasNext(); ) {
				CodeLabel deg = i.next();
				if (deg.hasCode()) ret += deg.getCode();
				if (i.hasNext()) ret += delim;
			}
			return ret;
		}
		public void addDegree(String degree, String label) {
			if (iDegree == null) iDegree = new ArrayList<CodeLabel>();
			iDegree.add(new CodeLabel(degree, label));
		}
		public List<CodeLabel> getDegrees() { return iDegree; }
		
		public boolean hasProgram() {
			if (iProgram == null || iProgram.isEmpty()) return false;
			for (CodeLabel prog: iProgram)
				if (!prog.isEmpty()) return true;
			return false;
		}
		public String getProgram(String delim) { 
			if (iProgram == null) return "";
			String ret = "";
			for (Iterator<CodeLabel> i = iProgram.iterator(); i.hasNext(); ) {
				CodeLabel prog = i.next();
				if (prog.hasCode()) ret += prog.getCode();
				if (i.hasNext()) ret += delim;
			}
			return ret;
		}
		public void addProgram(String program, String label) {
			if (iProgram == null) iProgram = new ArrayList<CodeLabel>();
			iProgram.add(new CodeLabel(program, label));
		}
		public List<CodeLabel> getPrograms() { return iProgram; }
		
		public boolean hasCampus() {
			if (iCampus == null || iCampus.isEmpty()) return false;
			for (CodeLabel camp: iCampus)
				if (!camp.isEmpty()) return true;
			return false;
		}
		public String getCampus(String delim) { 
			if (iCampus == null) return "";
			String ret = "";
			for (Iterator<CodeLabel> i = iCampus.iterator(); i.hasNext(); ) {
				CodeLabel prog = i.next();
				if (prog.hasCode()) ret += prog.getCode();
				if (i.hasNext()) ret += delim;
			}
			return ret;
		}
		public void addCampus(String campus, String label) {
			if (iCampus == null) iCampus = new ArrayList<CodeLabel>();
			iCampus.add(new CodeLabel(campus, label));
		}
		public List<CodeLabel> getCampuses() { return iCampus; }

		public void setDefaultCampus(String campus) { iDefaultCampus = campus; }
		public String getDefaultCampus() { return iDefaultCampus; }
		public boolean hasDefaultCampus() { return iDefaultCampus != null && !iDefaultCampus.isEmpty(); }
		
		public boolean hasAdvisor() { return iAdvisor != null && !iAdvisor.isEmpty(); }
		public String getAdvisor(String delim) { 
			if (iAdvisor == null) return "";
			String ret = "";
			for (String advisor: iAdvisor) {
				if (!ret.isEmpty()) ret += delim;
				ret += advisor;
			}
			return ret;
		}
		public void addAdvisor(String advisor) {
			if (advisor == null || advisor.isEmpty()) return;
			if (iAdvisor == null) iAdvisor = new ArrayList<String>();
			iAdvisor.add(advisor);
		}
		public List<String> getAdvisors() { return iAdvisor; }
		
		public boolean hasMinor() { return iMinor != null && !iMinor.isEmpty(); }
		public String getMinor(String delim) { 
			if (iMinor == null) return "";
			String ret = "";
			for (CodeLabel minor: iMinor) {
				if (!ret.isEmpty()) ret += delim;
				ret += minor.getCode();
			}
			return ret;
		}
		public void addMinor(String minor, String label) {
			if (iMinor == null) iMinor = new ArrayList<CodeLabel>();
			iMinor.add(new CodeLabel(minor, label));
		}
		public List<CodeLabel> getMinors() { return iMinor; }
		
		public boolean hasGroup() { return hasGroups(null); }
		public String getGroup(String delim) { return getGroup(null, delim); }
		public void addGroup(String group, String title) { addGroup(null, group, title); }
		public void removeGroup(String group) { removeGroup(null, group); }
		public List<Group> getGroups() { return getGroups(null); }
		public boolean hasGroup(String group) {
			if (iGroups == null) return false;
			for (Group g: iGroups) {
				if (g.getName().equals(group)) return true;
			}
			return false;
			
		}
		
		public boolean hasGroups() { return iGroups != null && !iGroups.isEmpty(); }
		public void addGroup(String type, String group, String title) {
			if (iGroups == null) iGroups = new TreeSet<Group>();
			iGroups.add(new Group(type, group, title));
		}
		public void removeGroup(String type, String group) {
			if (iGroups == null) return;
			for (Iterator<Group> i = iGroups.iterator(); i.hasNext(); ) {
				Group g = i.next();
				if (g.sameType(type) && g.getName().equals(group)) { i.remove(); }
			}
		}
		public List<Group> getGroups(String type) {
			if (iGroups == null) return null;
			List<Group> groups = new ArrayList<Group>();
			for (Group g: iGroups) {
				if (g.sameType(type)) {
					groups.add(g);
				}
			}
			return groups;
		}
		public Set<String> getGroupTypes() {
			if (iGroups == null) return null;
			Set<String> types = new HashSet<String>();
			for (Group g: iGroups)
				if (g.hasType()) types.add(g.getType());
			return types;
		}
		public boolean hasGroups(String type) {
			if (iGroups == null || iGroups.isEmpty()) return false;
			for (Group g: iGroups) {
				if (g.sameType(type)) return true;
			}
			return false;
		}
		public String getGroup(String type, String delim) {
			if (iGroups == null) return "";
			boolean html = "<br>".equalsIgnoreCase(delim);
			String ret = "";
			for (Group g: iGroups) {
				if (g.sameType(type)) {
					if (!ret.isEmpty()) ret += delim;
					ret += (html && g.hasTitle() ? "<span title='" + g.getTitle() + "'>" + g.getName() + "</span>" : g.getName());
				}
			}
			return ret;
		}
		
		public boolean hasAccommodation() { return iAccommodation != null && !iAccommodation.isEmpty(); }
		public String getAccommodation(String delim) { 
			if (iAccommodation == null) return "";
			String ret = "";
			for (CodeLabel accommodation: iAccommodation) {
				if (!ret.isEmpty()) ret += delim;
				ret += accommodation.getCode();
			}
			return ret;
		}
		public void addAccommodation(String accommodation, String label) {
			if (iAccommodation == null) iAccommodation = new ArrayList<CodeLabel>();
			iAccommodation.add(new CodeLabel(accommodation, label));
		}
		public List<CodeLabel> getAccommodations() { return iAccommodation; }
		
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
		public WaitListMode getWaitListMode() {
			if (iMode == null) return WaitListMode.None;
			return iMode;
		}
		public void setWaitListMode(WaitListMode mode) {
			iMode = mode;
		}
	}
	public static class Enrollment implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Student iStudent;
		private CourseAssignment iCourse = null;
		private int iPriority = 0;
		private String iAlternative = null;
		private Date iRequestedDate = null, iEnrolledDate = null, iApprovedDate = null, iWaitListedDate = null;
		private String iReservation = null;
		private String iApprovedBy = null;
		private List<Conflict> iConflicts = null;
		private Boolean iWaitList = null, iNoSub = null; 
		private String iEnrollmentMessage = null;
		private String iWaitListedPosition = null;
		private String iWaitListReplacement = null;
		private Integer iCritical = null;
		
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
		
		public boolean hasRequestedDate() { return iRequestedDate != null; }
		public Date getRequestedDate() { return iRequestedDate; }
		public void setRequestedDate(Date ts) { iRequestedDate = ts; }
		
		public boolean hasWaitListedDate() {
			return iWaitListedDate != null && isWaitList() && getStudent().getWaitListMode() == WaitListMode.WaitList;
		}
		public Date getWaitListedDate() { return iWaitListedDate; }
		public void setWaitListedDate(Date ts) { iWaitListedDate = ts; }
		
		public boolean hasEnrolledDate() { return iEnrolledDate != null; }
		public Date getEnrolledDate() { return iEnrolledDate; }
		public void setEnrolledDate(Date ts) { iEnrolledDate = ts; }
		
		public boolean hasApprovedDate() { return iApprovedDate != null; }
		public Date getApprovedDate() { return iApprovedDate; }
		public void setApprovedDate(Date ts) { iApprovedDate = ts; }
		public String getApprovedBy() { return iApprovedBy; }
		public void setApprovedBy(String approvedBy) { iApprovedBy = approvedBy; }
		
		public boolean hasWaitList() { return iWaitList != null; }
		public boolean isWaitList() { return iWaitList != null && iWaitList.booleanValue(); }
		public void setWaitList(Boolean waitList) { iWaitList = waitList; }
		
		public boolean hasNoSub() { return iNoSub != null; }
		public boolean isNoSub() { return iNoSub != null && iNoSub.booleanValue(); }
		public void setNoSub(Boolean noSub) { iNoSub = noSub; }
		
		public String getWaitListedPosition() { return iWaitListedPosition; }
		public boolean hasWaitListedPosition() {
			return iWaitListedPosition != null && !iWaitListedPosition.isEmpty() && isWaitList() && getStudent().getWaitListMode() == WaitListMode.WaitList;
		}
		public void setWaitListedPosition(String pos) { iWaitListedPosition = pos; }
		
		public String getWaitListReplacement() { return iWaitListReplacement; }
		public boolean hasWaitListedReplacement() {
			return iWaitListReplacement != null && !iWaitListReplacement.isEmpty() && isWaitList() && getStudent().getWaitListMode() == WaitListMode.WaitList;
		}
		public void setWaitListedReplacement(String replacesCourse) { iWaitListReplacement = replacesCourse; }
		
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

		public boolean hasReservation() { return iReservation != null && !iReservation.isEmpty(); }
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
		public void addEnrollmentMessage(String message) {
			if (iEnrollmentMessage == null)
				iEnrollmentMessage = message;
			else
				iEnrollmentMessage += "\n" + message;
		}
		
		public boolean hasCritical() { return iCritical != null; }
		public boolean isCritical() { return iCritical != null && iCritical.intValue() == 1; }
		public boolean isImportant() { return iCritical != null && iCritical.intValue() == 2; }
		public boolean isVital() { return iCritical != null && iCritical.intValue() == 3; }
		public boolean isLC() { return iCritical != null && iCritical.intValue() == 4; }
		public boolean isVisitingLC() { return iCritical != null && iCritical.intValue() == 5; }
		public Integer getCritical() { return iCritical; }
		public void setCritical(Integer critical) { iCritical = critical; }
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
		private Integer iLimit, iOther, iProjection, iEnrollment, iWaitlist, iReservation, iAvailable, iUnassigned, iUnassignedPrimary, iSnapshot, iNoSub;
		private Integer iTotalEnrollment, iTotalWaitlist, iTotalReservation, iTotalUnassigned, iTotalUnassignedPrimary, iTotalNoSub;
		private Integer iSwap, iTotalSwap;
		private Integer iConsentNeeded, iTotalConsentNeeded;
		private Integer iOverrideNeeded, iTotalOverrideNeeded;
		private ClassAssignment iAssignment;
		private int iLevel = 0;
		private Boolean iControl;
		private Long iMasterCourseId;
		private String iMasterSubject, iMasterCourseNbr;
		private Boolean iNoMatch;
		
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
		
		public Integer getSnapshot() { return iSnapshot; }
		public void setSnapshot(Integer snapshot) { iSnapshot = snapshot; }
		public boolean hasSnapshot() { return iSnapshot != null; }
		
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
		
		public Integer getSwap() { return iSwap; }
		public void setSwap(Integer swap) { iSwap = swap; }
		public boolean hasSwap() { return iSwap != null; }
		
		public Integer getNoSub() { return iNoSub; }
		public void setNoSub(Integer noSub) { iNoSub = noSub; }
		public boolean hasNoSub() { return iNoSub != null; }
		
		public Integer getUnassigned() { return iUnassigned; }
		public void setUnassigned(Integer unassigned) { iUnassigned = unassigned; }
		public boolean hasUnassigned() { return iUnassigned != null; }

		public Integer getUnassignedPrimary() { return iUnassignedPrimary; }
		public void setUnassignedPrimary(Integer unassigned) { iUnassignedPrimary = unassigned; }
		public boolean hasUnassignedPrimary() { return iUnassignedPrimary != null; }
		
		public Integer getUnassignedAlternative() {
			if (iUnassigned == null) return null;
			return iUnassigned - (iUnassignedPrimary == null ? 0 : iUnassignedPrimary.intValue());
		}

		public Integer getReservation() { return iReservation; }
		public void setReservation(Integer reservation) { iReservation = reservation; }
		public boolean hasReservation() { return iReservation !=null; }

		public Integer getTotalEnrollment() { return iTotalEnrollment; }
		public void setTotalEnrollment(Integer enrollment) { iTotalEnrollment = enrollment; }
		public boolean hasTotalEnrollment() { return iTotalEnrollment != null; }

		public Integer getTotalWaitlist() { return iTotalWaitlist; }
		public void setTotalWaitlist(Integer waitlist) { iTotalWaitlist = waitlist; }
		public boolean hasTotalWaitlist() { return iTotalWaitlist != null; }
		
		public Integer getTotalSwap() { return iTotalSwap; }
		public void setTotalSwap(Integer swap) { iTotalSwap = swap; }
		public boolean hasTotalSwap() { return iTotalSwap != null; }

		public Integer getTotalNoSub() { return iTotalNoSub; }
		public void setTotalNoSub(Integer noSub) { iTotalNoSub = noSub; }
		public boolean hasTotalNoSub() { return iTotalNoSub != null; }
		
		public Integer getTotalUnassigned() { return iTotalUnassigned; }
		public void setTotalUnassigned(Integer unassigned) { iTotalUnassigned = unassigned; }
		public boolean hasTotalUnassigned() { return iTotalUnassigned != null; }

		public Integer getTotalUnassignedPrimary() { return iTotalUnassignedPrimary; }
		public void setTotalUnassignedPrimary(Integer unassigned) { iTotalUnassignedPrimary = unassigned; }
		public boolean hasTotalUnassignedPrimary() { return iTotalUnassignedPrimary != null; }
		
		public Integer getTotalUnassignedAlternative() {
			if (iTotalUnassigned == null) return null;
			return iTotalUnassigned - (iTotalUnassignedPrimary == null ? 0 : iTotalUnassignedPrimary.intValue());
		}

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

		public Integer getOverrideNeeded() { return iOverrideNeeded; }
		public void setOverrideNeeded(Integer overrideNeeded) { iOverrideNeeded = overrideNeeded; }
		public int hasOverrideNeeded() { return iOverrideNeeded; }
		
		public Integer getTotalOverrideNeeded() { return iTotalOverrideNeeded; }
		public void setTotalOverrideNeeded(Integer totalOverrideNeeded) { iTotalOverrideNeeded = totalOverrideNeeded; }
		public int hasTotalOverrideNeeded() { return iTotalOverrideNeeded; }

		public int getLevel() { return iLevel; }
		public void setLevel(int level) { iLevel = level; }
		public void incLevel() { iLevel ++; }
		public String getIndent() {
			return getIndent("&nbsp;&nbsp;");
		}
		public String getIndent(String ind) {
			String indent = "";
			for (int i = 0; i < iLevel; i++) indent += ind;
			return indent;
		}
		
		public void setControl(Boolean control) { iControl = control; }
		public Boolean isControl() { return iControl; }
		
		public void setNoMatch(Boolean noMatch) { iNoMatch = noMatch; }
		public Boolean isNoMatch() { return iNoMatch; }
		
		public Long getMasterCouresId() { return (iMasterCourseId != null ? iMasterCourseId : iCourseId); }
		public void setMasterCourseId(Long courseId) { iMasterCourseId = courseId; }

		public String getMasterSubject() { return (iMasterSubject != null ? iMasterSubject : iSubject); }
		public void setMasterSubject(String subject) { iMasterSubject = subject; }

		public String getMasterCourseNbr() { return (iMasterCourseNbr != null ? iMasterCourseNbr : iCourseNbr); }
		public void setMasterCourseNbr(String courseNbr) { iMasterCourseNbr = courseNbr; }
	}
	
	public static class StudentInfo implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Student iStudent;
		private Integer iEnrollment, iWaitlist, iReservation, iRequested, iUnassigned, iNoSub, iSwap;
		private Integer iTotalEnrollment, iTotalWaitlist, iTotalReservation, iTotalUnassigned, iTotalNoSub, iTotalSwap;
		private Integer iConsentNeeded, iTotalConsentNeeded;
		private Integer iTopWaitingPriority;
		private Date iRequestedDate = null, iEnrolledDate = null, iApprovedDate = null, iEmailDate = null, iWaitListedDate = null;
		private String iStatus, iNote;
		private Float iCredit, iTotalCredit;
		private Map<String, Float> iIMCredit, iIMTotalCredit;
		private Integer iNrDistanceConflicts, iLongestDistanceMinutes, iOverlappingMinutes;
		private Integer iTotalNrDistanceConflicts, iTotalLongestDistanceMinutes, iTotalOverlappingMinutes;
		private Integer iFreeTimeOverlappingMins, iTotalFreeTimeOverlappingMins;
		private Integer iPrefInstrMethConflict, iTotalPrefInstrMethConflict;
		private Integer iPrefSectionConflict, iTotalPrefSectionConflict;
		private float[] iRequestCredit = null, iRequestTotalCredit = null;
		private Integer iOverrideNeeded, iTotalOverrideNeeded;
		private Boolean iMyStudent;
		private AdvisedInfoInterface iAdvised;
		private String iPreference;
		private String iPin;
		private Boolean iPinReleased;
		
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
		
		public Integer getNoSub() { return iNoSub; }
		public void setNoSub(Integer noSub) { iNoSub = noSub; }
		public boolean hasNoSub() { return iNoSub != null; }
		
		public Integer getSwap() { return iSwap; }
		public void setSwap(Integer swap) { iSwap = swap; }
		public boolean hasSwap() { return iSwap != null; }
		
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
		
		public Integer getTotalNoSub() { return iTotalNoSub; }
		public void setTotalNoSub(Integer noSub) { iTotalNoSub = noSub; }
		public boolean hasTotalNoSub() { return iTotalNoSub != null; }
		
		public Integer getTotalSwap() { return iTotalSwap; }
		public void setTotalSwap(Integer swap) { iTotalSwap = swap; }
		public boolean hasTotalSwap() { return iTotalSwap != null; }
		
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
		
		public Integer getOverrideNeeded() { return iOverrideNeeded; }
		public void setOverrideNeeded(Integer overrideNeeded) { iOverrideNeeded = overrideNeeded; }
		public int hasOverrideNeeded() { return iOverrideNeeded; }
		
		public Integer getTotalOverrideNeeded() { return iTotalOverrideNeeded; }
		public void setTotalOverrideNeeded(Integer totalOverrideNeeded) { iTotalOverrideNeeded = totalOverrideNeeded; }
		public int hasTotalOverrideNeeded() { return iTotalOverrideNeeded; }

		public Integer getTopWaitingPriority() { return iTopWaitingPriority; }
		public void setTopWaitingPriority(Integer topWaitingPriority) { iTopWaitingPriority = topWaitingPriority; }
		public int hasTopWaitingPriority() { return iTopWaitingPriority; }
		
		public Date getRequestedDate() { return iRequestedDate; }
		public void setRequestedDate(Date ts) { iRequestedDate = ts; }
		
		public Date getWaitListedDate() { return iWaitListedDate; }
		public void setWaitListedDate(Date ts) { iWaitListedDate = ts; }

		public Date getEnrolledDate() { return iEnrolledDate; }
		public void setEnrolledDate(Date ts) { iEnrolledDate = ts; }
		
		public Date getApprovedDate() { return iApprovedDate; }
		public void setApprovedDate(Date ts) { iApprovedDate = ts; }
		
		public Date getEmailDate() { return iEmailDate; }
		public void setEmailDate(Date ts) { iEmailDate = ts; }

		public String getStatus() { return iStatus; }
		public void setStatus(String status) { iStatus = status; }
		public void setStatus(StudentStatusInfo status) {
			if (status == null) {
				iStatus = "";
				iStudent.setCanRegister(true);
				iStudent.setCanUseAssistant(true);
			} else {
				iStatus = status.getReference();
				iStudent.setCanRegister(status.isCanRegister());
				iStudent.setCanUseAssistant(status.isCanUseAssistant());
			}
		}
		
		public String getNote() { return iNote; }
		public boolean hasNote() { return iNote != null && !iNote.isEmpty(); }
		public void setNote(String note) { iNote = note; }
		
		public boolean hasCredit() { return iCredit != null && iCredit > 0; }
		public void setCredit(Float credit) { iCredit = credit; }
		public Float getCredit() { return iCredit; }

		public boolean hasTotalCredit() { return iTotalCredit != null && iTotalCredit > 0; }
		public void setTotalCredit(Float totalCredit) { iTotalCredit = totalCredit; }
		public Float getTotalCredit() { return iTotalCredit; }
		
		public boolean hasIMCredit() { return iIMCredit != null && !iIMCredit.isEmpty(); }
		public Map<String, Float> getIMCredit() { return iIMCredit; }
		public void setIMCredit(String im, Float credit) {
			if (iIMCredit == null) iIMCredit = new HashMap<String, Float>();
			if (credit == null || credit.floatValue() == 0f) {
				iIMCredit.remove(im);
			} else {
				iIMCredit.put(im, credit);
			}
		}
		public void addIMCredit(String im, float credit) {
			if (credit <= 0f) return;
			if (iIMCredit == null) iIMCredit = new HashMap<String, Float>();
			Float prev = iIMCredit.get(im);
			iIMCredit.put(im, credit + (prev == null ? 0f: prev.floatValue()));
		}
		public float getIMCredit(String im) {
			if (iIMCredit == null) return 0f;
			Float cred = iIMCredit.get(im);
			return (cred == null ? 0f : cred.floatValue());
		}
		public Set<String> getCreditIMs() {
			return (iIMCredit == null ? new TreeSet<String>() : new TreeSet<String>(iIMCredit.keySet()));
		}
		
		public boolean hasIMTotalCredit() { return iIMTotalCredit != null && !iIMTotalCredit.isEmpty(); }
		public Map<String, Float> getIMTotalCredit() { return iIMTotalCredit; }
		public void setIMTotalCredit(String im, Float credit) {
			if (iIMTotalCredit == null) iIMTotalCredit = new HashMap<String, Float>();
			if (credit == null || credit.floatValue() == 0f) {
				iIMTotalCredit.remove(im);
			} else {
				iIMTotalCredit.put(im, credit);
			}
		}
		public void addIMTotalCredit(String im, float credit) {
			if (credit <= 0f) return;
			if (iIMTotalCredit == null) iIMTotalCredit = new HashMap<String, Float>();
			Float prev = iIMTotalCredit.get(im);
			iIMTotalCredit.put(im, credit + (prev == null ? 0f: prev.floatValue()));
		}
		public float getIMTotalCredit(String im) {
			if (iIMTotalCredit == null) return 0f;
			Float cred = iIMTotalCredit.get(im);
			return (cred == null ? 0f : cred.floatValue());
		}
		public Set<String> getTotalCreditIMs() {
			return (iIMTotalCredit == null ? new TreeSet<String>() : new TreeSet<String>(iIMTotalCredit.keySet()));
		}
		
		public boolean hasRequestCredit() { return iRequestCredit != null && iRequestCredit[1] > 0f; }
		public void setRequestCredit(float min, float max) { iRequestCredit = new float[] { min, max }; }
		public float getRequestCreditMin() { return iRequestCredit == null ? 0f : iRequestCredit[0]; }
		public float getRequestCreditMax() { return iRequestCredit == null ? 0f : iRequestCredit[1]; }
		
		public boolean hasTotalRequestCredit() { return iRequestTotalCredit != null && iRequestTotalCredit[1] > 0f; }
		public void setTotalRequestCredit(float min, float max) { iRequestTotalCredit = new float[] { min, max }; }
		public float getTotalRequestCreditMin() { return iRequestTotalCredit == null ? 0f : iRequestTotalCredit[0]; }
		public float getTotalRequestCreditMax() { return iRequestTotalCredit == null ? 0f : iRequestTotalCredit[1]; }
		
		public boolean hasDistanceConflicts() { return iNrDistanceConflicts != null && iNrDistanceConflicts > 0; }
		public void setNrDistanceConflicts(Integer nrDistanceConflicts) { iNrDistanceConflicts = nrDistanceConflicts; }
		public Integer getNrDistanceConflicts() { return iNrDistanceConflicts; }
		public void setLongestDistanceMinutes(Integer longestDistance) { iLongestDistanceMinutes = longestDistance; }
		public Integer getLongestDistanceMinutes() { return iLongestDistanceMinutes; }
		
		public boolean hasOverlappingMinutes() { return iOverlappingMinutes != null && iOverlappingMinutes > 0; }
		public void setOverlappingMinutes(Integer overlapMins) { iOverlappingMinutes = overlapMins; }
		public Integer getOverlappingMinutes() { return iOverlappingMinutes; }

		public boolean hasTotalDistanceConflicts() { return iTotalNrDistanceConflicts != null && iTotalNrDistanceConflicts > 0; }
		public void setTotalNrDistanceConflicts(Integer nrDistanceConflicts) { iTotalNrDistanceConflicts = nrDistanceConflicts; }
		public Integer getTotalNrDistanceConflicts() { return iTotalNrDistanceConflicts; }
		public void setTotalLongestDistanceMinutes(Integer longestDistance) { iTotalLongestDistanceMinutes = longestDistance; }
		public Integer getTotalLongestDistanceMinutes() { return iTotalLongestDistanceMinutes; }
		
		public boolean hasTotalOverlappingMinutes() { return iTotalOverlappingMinutes != null && iTotalOverlappingMinutes > 0; }
		public void setTotalOverlappingMinutes(Integer overlapMins) { iTotalOverlappingMinutes = overlapMins; }
		public Integer getTotalOverlappingMinutes() { return iTotalOverlappingMinutes; }
		
		public boolean hasFreeTimeOverlappingMins() { return iFreeTimeOverlappingMins != null && iFreeTimeOverlappingMins > 0; }
		public void setFreeTimeOverlappingMins(Integer overlapMins) { iFreeTimeOverlappingMins = overlapMins; }
		public Integer getFreeTimeOverlappingMins() { return iFreeTimeOverlappingMins; }
		public boolean hasTotalFreeTimeOverlappingMins() { return iTotalFreeTimeOverlappingMins != null && iTotalFreeTimeOverlappingMins > 0; }
		public void setTotalFreeTimeOverlappingMins(Integer overlapMins) { iTotalFreeTimeOverlappingMins = overlapMins; }
		public Integer getTotalFreeTimeOverlappingMins() { return iTotalFreeTimeOverlappingMins; }
		
		public boolean hasPrefInstrMethConflict() { return iPrefInstrMethConflict != null && iPrefInstrMethConflict > 0; }
		public void setPrefInstrMethConflict(Integer conf) { iPrefInstrMethConflict = conf; }
		public Integer getPrefInstrMethConflict() { return iPrefInstrMethConflict; }
		public boolean hasTotalPrefInstrMethConflict() { return iTotalPrefInstrMethConflict != null && iTotalPrefInstrMethConflict > 0; }
		public void setTotalPrefInstrMethConflict(Integer conf) { iTotalPrefInstrMethConflict = conf; }
		public Integer getTotalPrefInstrMethConflict() { return iTotalPrefInstrMethConflict; }
		public boolean hasPrefSectionConflict() { return iPrefSectionConflict != null && iPrefSectionConflict > 0; }
		public void setPrefSectionConflict(Integer conf) { iPrefSectionConflict = conf; }
		public Integer getPrefSectionConflict() { return iPrefSectionConflict; }
		public boolean hasTotalPrefSectionConflict() { return iTotalPrefSectionConflict != null && iTotalPrefSectionConflict > 0; }
		public void setTotalPrefSectionConflict(Integer conf) { iTotalPrefSectionConflict = conf; }
		public Integer getTotalPrefSectionConflict() { return iTotalPrefSectionConflict; }
		
		public boolean isMyStudent() { return iMyStudent != null && iMyStudent.booleanValue(); }
		public Boolean getMyStudent() { return iMyStudent; }
		public void setMyStudent(Boolean myStudent) { iMyStudent = myStudent; }
		
		public boolean isAdvised() { return iAdvised != null; }
		public AdvisedInfoInterface getAdvisedInfo() { return iAdvised; }
		public void setAdvisedInfo(AdvisedInfoInterface advised) { iAdvised = advised; }
		
		public boolean hasPreference() { return iPreference != null && !iPreference.isEmpty(); }
		public void setPreference(String pref) { iPreference = pref; }
		public String getPreference() { return iPreference; }
		
		public boolean hasPin() { return iPin != null && !iPin.isEmpty(); }
		public void setPin(String pin) { iPin = pin; }
		public String getPin() { return iPin; }
		
		public void setPinReleased(Boolean released) { iPinReleased = released; }
		public boolean hasPinReleased() { return hasPin() && Boolean.TRUE.equals(iPinReleased); }
	}

	public static class SectioningAction implements IsSerializable, Serializable, Comparable<SectioningAction> {
		private static final long serialVersionUID = 1L;
		private Long iLogId;
		private Student iStudent;
		private Date iTimeStamp;
		private String iOperation;
		private String iUser;
		private String iMessage;
		private String iResult;
		private Long iCpuTime;
		private Long iWallTime;
		
		public SectioningAction() {
		}
		
		public Long getLogId() { return iLogId; }
		public void setLogId(Long id) { iLogId = id; }

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
		
		public Long getWallTime() { return iWallTime; }
		public void setWallTime(Long wallTime) { iWallTime = wallTime; }

		@Override
		public int compareTo(SectioningAction a) {
			return a.getTimeStamp().compareTo(getTimeStamp());
		}
	}
	
	public static class IdValue implements IsSerializable, Serializable, Comparable<IdValue> {
		private static final long serialVersionUID = 1L;
		private Long iId;
		private String iValue;
		
		public IdValue() {}
		public IdValue(Long id, String value) { 
			iId = id; iValue = value;
		}
		
		public Long getId() { return iId; }
		public String getValue() { return iValue; }
		
		@Override
		public int compareTo(IdValue other) {
			return getValue().compareTo(other.getValue());
		}
		
		@Override
		public String toString() {
			return getValue();
		}
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof IdValue)) return false;
			return getId().equals(((IdValue)o).getId());
		}
	}
	
	public static class ErrorMessage implements IsSerializable, Serializable, Comparable<ErrorMessage> {
		private static final long serialVersionUID = 1L;
		private String iCourse;
		private String iSection;
		private String iCode;
		private String iMessage;
		
		public static enum UniTimeCode {
			UT_LOCKED,
			UT_DISABLED,
			UT_STRUCTURE("LINK"),
			UT_TIME_CNF("TIME"),
			UT_NOT_AVAILABLE("CLOS"),
			UT_CANCEL,
			UT_DEADLINE,
			UT_GRADE_MODE,
			;
			
			private String iCode;
			
			UniTimeCode() {}
			UniTimeCode(String code) { iCode = code; }
			
			String code() { return (iCode == null ? name() : iCode); }
		}
		
		public ErrorMessage() {}
		public ErrorMessage(String course, String section, String code, String message) {
			iCourse = course;
			iSection = section;
			iCode = code;
			iMessage = message;
		}
		public ErrorMessage(String course, String section, UniTimeCode code, String message) {
			iCourse = course;
			iSection = section;
			iCode = code.code();
			iMessage = message;
		}
		
		public void setCourse(String course) { iCourse = course; }
		public String getCourse() { return iCourse; }
		public void setSection(String section) { iSection = section; }
		public String getSection() { return iSection; }
		public void setCode(String code) { iCode = code; }
		public String getCode() { return iCode; }
		public void setMessage(String message) { iMessage = message; }
		public String getMessage() { return iMessage; }
		
		@Override
		public int compareTo(ErrorMessage other) {
			return NaturalOrderComparator.compare(toString(), other.toString());
		}
		
		@Override
		public String toString() {
			return getCourse() + (getSection() == null ? "" : " " + getSection()) + ": " + getMessage();
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ErrorMessage)) return false;
			return toString().equals(o.toString());
		}
	}
	
	public static class Note implements IsSerializable, Serializable, Comparable<Note> {
		private static final long serialVersionUID = 1L;
		private Long iId;
		private Date iTimeStamp;
		private String iMessage;
		private String iOwner;
		
		public Note() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public Date getTimeStamp() { return iTimeStamp; }
		public void setTimeStamp(Date date) { iTimeStamp = date; }
		
		public String getMessage() { return iMessage; }
		public void setMessage(String message) { iMessage = message; }
		
		public String getOwner() { return iOwner; }
		public void setOwner(String owner) { iOwner = owner; }
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Note)) return false;
			return getId().equals(((Note)o).getId());
		}
		
		@Override
		public int compareTo(Note other) {
			int cmp = -getTimeStamp().compareTo(other.getTimeStamp());
			if (cmp != 0) return cmp;
			return getId().compareTo(other.getId());
		}
	}
	
	public static class AdvisedInfoInterface implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Float iMinCredit, iMaxCredit;
		private Float iPercentage;
		private String iMessage, iNotAssignedMessage;
		private Integer iMissingCritical, iMissingPrimary;
		private Integer iNotAssignedCritical, iNotAssignedPrimary;
		private Integer iAdvisorCritical = null;
		
		public AdvisedInfoInterface() {}
		
		public boolean hasMinCredit() { return iMinCredit != null; }
		public Float getMinCredit() { return iMinCredit; }
		public void setMinCredit(Float cred) { iMinCredit = cred; }
		
		public boolean hasMaxCredit() { return iMaxCredit != null; }
		public Float getMaxCredit() { return iMaxCredit; }
		public void setMaxCredit(Float cred) { iMaxCredit = cred; }
		
		public boolean hasPercentage() { return iPercentage != null; }
		public Float getPercentage() { return iPercentage; }
		public void setPercentage(Float p) { iPercentage = p; }
		
		public boolean hasMissingCritical() { return iMissingCritical != null; }
		public Integer getMissingCritical() { return iMissingCritical; }
		public void setMissingCritical(Integer missing) { iMissingCritical = missing; }
		
		public boolean hasMissingPrimary() { return iMissingPrimary != null; }
		public Integer getMissingPrimary() { return iMissingPrimary; }
		public void setMissingPrimary(Integer missing) { iMissingPrimary = missing; }
		
		public boolean hasNotAssignedCritical() { return iNotAssignedCritical != null; }
		public Integer getNotAssignedCritical() { return iNotAssignedCritical; }
		public void setNotAssignedCritical(Integer notAssigned) { iNotAssignedCritical = notAssigned; }
		
		public boolean hasNotAssignedPrimary() { return iNotAssignedPrimary != null; }
		public Integer getNotAssignedPrimary() { return iNotAssignedPrimary; }
		public void setNotAssignedPrimary(Integer notAssigned) { iNotAssignedPrimary = notAssigned; }
		
		public String getMessage() { return iMessage; }
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public void setMessage(String message) { iMessage = message; }
		public void addMessage(String message) {
			iMessage = (iMessage == null ? "" : iMessage + "\n") +  message;
		}
		
		public String getNotAssignedMessage() { return iNotAssignedMessage; }
		public boolean hasNotAssignedMessage() { return iNotAssignedMessage != null && !iNotAssignedMessage.isEmpty(); }
		public void setNotAssignedMessage(String message) { iNotAssignedMessage = message; }
		public void addNotAssignedMessage(String message) {
			iNotAssignedMessage = (iNotAssignedMessage == null ? "" : iNotAssignedMessage + "\n") +  message;
		}
		
		public void setAdvisorCritical(Integer advisorCritical) { iAdvisorCritical = advisorCritical; }
		public boolean isAdvisorCritical() { return iAdvisorCritical != null && iAdvisorCritical.intValue() == 1; }
		public boolean isAdvisorImportant() { return iAdvisorCritical != null && iAdvisorCritical.intValue() == 2; }
		public boolean isAdvisorVital() { return iAdvisorCritical != null && iAdvisorCritical.intValue() == 3; }
	}
	
	public boolean hasCurrentCredit() { return iCurrentCredit != null && iCurrentCredit > 0f; }
	public void setCurrentCredit(Float curCredit) { iCurrentCredit = curCredit; }
	public Float getCurrentCredit() { return iCurrentCredit; }
}
