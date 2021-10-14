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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeCourseInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class CourseRequestInterface extends StudentSectioningContext implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<Request> iCourses = new ArrayList<Request>();
	private ArrayList<Request> iAlternatives = new ArrayList<Request>();
	private boolean iSaved = false;
	private boolean iNoChange = false;
	private boolean iAllowTimeConf = false, iAllowRoomConf = false, iLinkedConf = false, iDeadlineConf = false;
	private Boolean iUpdateLastRequest = null;
	private RequestedCourse iLastCourse = null;
	private List<CourseMessage> iConfirmations = null;
	private Float iMaxCredit = null;
	private Float iMaxCreditOverride = null;
	private RequestedCourseStatus iMaxCreditOverrideStatus = null;
	private String iMaxCreditOverrideExternalId = null;
	private Date iMaxCreditOverrideTimeStamp = null;
	private String iCreditWarning = null;
	private String iCreditNote = null;
	private String iErrorMessage = null;
	private String iSpecRegDashboardUrl = null;
	private String iRequestorNote = null;
	private String iRequestId = null;
	private String iPopupMessage = null;
	private Boolean iPinReleased = null;
	private WaitListMode iMode = null;
	private CheckCoursesResponse iWaitListChecks = null;
	
	public CourseRequestInterface() {}
	public CourseRequestInterface(StudentSectioningContext cx) {
		super(cx);
	}

	public ArrayList<Request> getCourses() { return iCourses; }
	public Request getCourse(int index) {
		if (iCourses != null && index < iCourses.size())
			return iCourses.get(index);
		return null;
	}
	public ArrayList<Request> getAlternatives() { return iAlternatives; }
	public Request getAlternative(int index) {
		if (iAlternatives != null && index < iAlternatives.size())
			return iAlternatives.get(index);
		return null;
	}
	public void addCourseCriticalFirst(Request request) {
		if (request.isCritical() || request.isImportant()) {
			int lastCritical = -1;
			for (int i = 0; i < getCourses().size(); i++)
				if (getCourses().get(i).isCritical() || getCourses().get(i).isImportant()) lastCritical = i;
			getCourses().add(lastCritical + 1, request);
		} else {
			getCourses().add(request);
		}
	}
	
	public boolean isActive(Long courseId) {
		if (courseId == null) return false;
		for (Request r: getCourses()) {
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse())
					if (courseId.equals(rc.getCourseId()) && !rc.isInactive())
						return true;
		}
		for (Request r: getAlternatives()) {
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse())
					if (courseId.equals(rc.getCourseId()) && !rc.isInactive())
						return true;
		}
		return false;
	}
	
	public boolean isWaitListed(Long courseId) {
		if (courseId == null) return false;
		for (Request r: getCourses()) {
			if (r.hasRequestedCourse() && r.isWaitList())
				for (RequestedCourse rc: r.getRequestedCourse())
					if (courseId.equals(rc.getCourseId())) return true;
		}
		for (Request r: getAlternatives()) {
			if (r.hasRequestedCourse() && r.isWaitList())
				for (RequestedCourse rc: r.getRequestedCourse())
					if (courseId.equals(rc.getCourseId())) return true;
		}
		return false;
	}
	
	public boolean sameWaitListedCourses(CourseRequestInterface other) {
		if (other == null) return false;
		int nrWaitListed = 0;
		r: for (Request r: getCourses()) {
			if (r.isWaitList()) {
				nrWaitListed ++;
				for (Request o: other.getCourses())
					if (o.isWaitList() && r.sameCourses(o)) continue r;
				return false;
			}
		}
		for (Request o: other.getCourses())
			if (o.isWaitList()) nrWaitListed --;
		return nrWaitListed == 0;
	}
	
	public boolean isSaved() { return iSaved; }
	public void setSaved(boolean saved) { iSaved = saved; }
	
	public boolean isNoChange() { return iNoChange; }
	public void setNoChange(boolean noChange) { iNoChange = noChange; }
	
	public boolean areTimeConflictsAllowed() { return iAllowTimeConf; }
	public void setTimeConflictsAllowed(boolean allow) { iAllowTimeConf = allow; }
	public boolean areSpaceConflictsAllowed() { return iAllowRoomConf; }
	public void setSpaceConflictsAllowed(boolean allow) { iAllowRoomConf = allow; }
	public boolean areLinkedConflictsAllowed() { return iLinkedConf; }
	public void setLinkedConflictsAllowed(boolean allow) { iLinkedConf = allow; }
	public boolean areDeadlineConflictsAllowed() { return iDeadlineConf; }
	public void setDeadlineConflictsAllowed(boolean allow) { iDeadlineConf = allow; }
	
	public boolean isUpdateLastRequest() { return iUpdateLastRequest == null || iUpdateLastRequest.booleanValue(); }
	public void setUpdateLastRequest(boolean updateLastRequest) { iUpdateLastRequest = updateLastRequest; }
	
	public boolean isEmpty() {
		return iCourses.isEmpty() && iAlternatives.isEmpty();
	}
	
	public boolean hasMaxCredit() { return iMaxCredit != null; }
	public void setMaxCredit(Float maxCredit) { iMaxCredit = maxCredit; }
	public Float getMaxCredit() { return iMaxCredit; }
	public boolean hasMaxCreditOverride() { return iMaxCreditOverride != null; }
	public void setMaxCreditOverride(Float maxCreditOverride) { iMaxCreditOverride = maxCreditOverride; }
	public Float getMaxCreditOverride() { return iMaxCreditOverride; }
	public void setMaxCreditOverrideStatus(RequestedCourseStatus status) { iMaxCreditOverrideStatus = status; }
	public RequestedCourseStatus getMaxCreditOverrideStatus() { return iMaxCreditOverrideStatus; }
	public void setMaxCreditOverrideExternalId(String externalId) { iMaxCreditOverrideExternalId = externalId; }
	public String getMaxCreditOverrideExternalId() { return iMaxCreditOverrideExternalId; }
	public void setMaxCreditOverrideTimeStamp(Date timeStamp) { iMaxCreditOverrideTimeStamp = timeStamp; }
	public Date getMaxCreditOverrideTimeStamp() { return iMaxCreditOverrideTimeStamp; }
	public boolean hasCreditWarning() { return iCreditWarning != null && !iCreditWarning.isEmpty(); }
	public String getCreditWarning() { return iCreditWarning; }
	public void setCreditWarning(String warning) { iCreditWarning = warning; }
	public boolean hasCreditNote() { return iCreditNote != null && !iCreditNote.isEmpty() && !" ".equals(iCreditNote); }
	public String getCreditNote() { return iCreditNote; }
	public void setCreditNote(String note) { iCreditNote = note; }
	
	public boolean hasPopupMessage() { return iPopupMessage != null && !iPopupMessage.isEmpty(); }
	public void setPopupMessage(String message) {
		if (message == null || message.isEmpty()) return;
		if (iPopupMessage == null) iPopupMessage = message;
		else if (!iPopupMessage.contains(message)) iPopupMessage += "\n" + message;
	}
	public String getPopupMessage() { return iPopupMessage; }
	
	public boolean addCourse(RequestedCourse course) {
		iLastCourse = course;
		RequestPriority rp = getRequestPriority(course);
		if (rp != null) {
			if (rp.getRequest().getRequestedCourse(rp.getChoice()).isInactive())
				dropCourse(course);
			else
				return false;
		}
		for (CourseRequestInterface.Request r: getCourses()) {
			if (r.isEmpty()) {
				r.addRequestedCourse(course);
				return true;
			}
		}
		CourseRequestInterface.Request r = new CourseRequestInterface.Request();
		r.addRequestedCourse(course);
		getCourses().add(r);
		return true;
	}
	
	public boolean dropCourse(RequestedCourse course) {
		iLastCourse = course;
		for (Iterator<CourseRequestInterface.Request> j = getCourses().iterator(); j.hasNext(); ) {
			CourseRequestInterface.Request r = j.next();
			if (r.hasRequestedCourse(course)) {
				for (Iterator<RequestedCourse> i = r.getRequestedCourse().iterator(); i.hasNext(); ) {
					if (course.equals(i.next())) i.remove();
				}
				if (!r.hasRequestedCourse()) j.remove();
				return true;
			}
		}
		for (Iterator<CourseRequestInterface.Request> j = getAlternatives().iterator(); j.hasNext(); ) {
			CourseRequestInterface.Request r = j.next();
			if (r.hasRequestedCourse(course)) {
				for (Iterator<RequestedCourse> i = r.getRequestedCourse().iterator(); i.hasNext(); ) {
					if (course.equals(i.next())) i.remove();
				}
				if (!r.hasRequestedCourse()) j.remove();
				return true;
			}
		}
		return false;
	}
	
	public boolean hasLastCourse() { return iLastCourse != null; }
	
	public RequestedCourse getLastCourse() { return iLastCourse; }
	
	private RequestPriority __getRequestPriority(Object course) {
		if (course == null) return null;
		int priority = 1;
		for (CourseRequestInterface.Request r: getCourses()) {
			if (r.hasRequestedCourse())
				for (int i = 0; i < r.getRequestedCourse().size(); i++)
					if (r.getRequestedCourse(i).equals(course)) return new RequestPriority(false, priority, i, r);
			priority ++;
		}
		priority = 1;
		for (CourseRequestInterface.Request r: getAlternatives()) {
			if (r.hasRequestedCourse())
				for (int i = 0; i < r.getRequestedCourse().size(); i++)
					if (r.getRequestedCourse(i).equals(course)) return new RequestPriority(true, priority, i, r);
			priority ++;
		}
		return null;
	}
	
	public RequestPriority getRequestPriority(CourseAssignment course) {
		return __getRequestPriority(course);
	}
	
	public RequestPriority getRequestPriority(DegreeCourseInterface course) {
		return __getRequestPriority(course);
	}
	
	public RequestPriority getRequestPriority(RequestedCourse course) {
		return __getRequestPriority(course);
	}
	
	public float[] getCreditRange(Set<Long> advisorWaitListedCourseIds) {
		List<Float> mins = new ArrayList<Float>();
		List<Float> maxs = new ArrayList<Float>();
		int nrCourses = 0;
		float tMin = 0f, tMax = 0f;
		for (Request r: getCourses()) {
			if (r.hasRequestedCourse()) {
				Float min = null, max = null;
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasCredit()) {
						if (min == null || min > rc.getCreditMin()) min = rc.getCreditMin();
						if (max == null || max < rc.getCreditMax()) max = rc.getCreditMax();
					}
				}
				if (min != null) {
					if (r.isWaitListOrNoSub(iMode, advisorWaitListedCourseIds)) {
						tMin += min; tMax += max;
					} else {
						mins.add(min); maxs.add(max); nrCourses ++;
					}
				}
			}
		}
		for (Request r: getAlternatives()) {
			if (r.hasRequestedCourse()) {
				Float min = null, max = null;
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasCredit()) {
						if (min == null || min > rc.getCreditMin()) min = rc.getCreditMin();
						if (max == null || max < rc.getCreditMax()) max = rc.getCreditMax();
					}
				}
				if (min != null) {
					mins.add(min); maxs.add(max);
				}
			}
		}
		Collections.sort(mins);
		Collections.sort(maxs);
		for (int i = 0; i < nrCourses; i++) {
			tMin += mins.get(i);
			tMax += maxs.get(maxs.size() - i - 1);
		}
		return new float[] {tMin, tMax};
	}
	
	public float getCredit(Set<Long> advisorWaitListedCourseIds) {
		List<Float> credits = new ArrayList<Float>();
		int nrCourses = 0;
		float total = 0f;
		for (Request r: getCourses()) {
			if (r.hasRequestedCourse()) {
				Float credit = null;
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasCredit()) {
						if (credit == null || credit < rc.getCreditMin()) credit = rc.getCreditMin();
					}
				}
				if (credit != null) {
					if (r.isWaitListOrNoSub(iMode, advisorWaitListedCourseIds)) {
						total += credit;
					} else {
						credits.add(credit); nrCourses ++;
					}
				}
			}
		}
		for (Request r: getAlternatives()) {
			if (r.hasRequestedCourse()) {
				Float credit = null;
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasCredit()) {
						if (credit == null || credit < rc.getCreditMin()) credit = rc.getCreditMin();
					}
				}
				if (credit != null) {
					credits.add(credit);
				}
			}
		}
		Collections.sort(credits);
		for (int i = 0; i < nrCourses; i++) {
			total += credits.get(credits.size() - i - 1);
		}
		return total;
	}
	
	public boolean isPinReleased() { return iPinReleased != null && iPinReleased.booleanValue(); }
	public void setPinReleased(boolean pinReleased) { iPinReleased = pinReleased; }
	public boolean hasReleasedPin() { return isPinReleased() && hasPin(); }
	
	public Set<Long> getWaitListedCourseIds() {
		Set<Long> courseIds = new HashSet<Long>();
		for (Request request: getCourses()) {
			if (request.hasRequestedCourse() && request.isWaitList())
				for (RequestedCourse rc: request.getRequestedCourse()) {
					if (rc.hasCourseId())
						courseIds.add(rc.getCourseId());
					break;
				}
		}
		return courseIds;
	}
	
	public Set<Long> getNoSubCourseIds() {
		Set<Long> courseIds = new HashSet<Long>();
		for (Request request: getCourses()) {
			if (request.hasRequestedCourse() && request.isNoSub())
				for (RequestedCourse rc: request.getRequestedCourse()) {
					if (rc.hasCourseId())
						courseIds.add(rc.getCourseId());
					break;
				}
		}
		return courseIds;
	}

	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseRequestInterface)) return false;
		CourseRequestInterface r = (CourseRequestInterface)o;
		if (getCourses().size() != r.getCourses().size()) return false;
		for (int i = 0; i < getCourses().size(); i++) {
			if (!getCourse(i).equals(r.getCourse(i))) return false;
		}
		if (getAlternatives().size() != r.getAlternatives().size()) return false;
		for (int i = 0; i < getAlternatives().size(); i++) {
			if (!getAlternative(i).equals(r.getAlternative(i))) return false;
		}
		return true;
	}
	
	public static class FreeTime implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private ArrayList<Integer> iDays = new ArrayList<Integer>();
		private int iStart;
		private int iLength;
		public FreeTime() {}
		public FreeTime(List<Integer> days, int start, int length) {
			if (days != null) iDays.addAll(days);
			iStart = start; iLength = length;
		}
		
		public void addDay(int day) { iDays.add(day); }
		public ArrayList<Integer> getDays() { return iDays; }
		public String getDaysString(String[] shortDays, String separator) {
			if (iDays == null) return "";
			String ret = "";
			for (int day: iDays)
				ret += (ret.isEmpty() ? "" : separator) + shortDays[day];
			return ret;
		}
		public int getDayCode() {
			int ret = 0;
			for (int day: iDays)
				ret += (1 << (6 - day));
			return ret;
		}
		
		public int getStart() { return iStart; }
		public void setStart(int startSlot) { iStart = startSlot; }
		public String getStartString(boolean useAmPm) {
	        int h = iStart / 12;
	        int m = 5 * (iStart % 12);
	        if (useAmPm)
	        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
	        else
				return h + ":" + (m < 10 ? "0" : "") + m;
		}

		
		public int getLength() { return iLength; }
		public void setLength(int length) { iLength = length; }
		public String getEndString(boolean useAmPm) {
			int h = (iStart + iLength) / 12;
			int m = 5 * ((iStart + iLength) % 12);
	        if (useAmPm)
	        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
	        else
				return h + ":" + (m < 10 ? "0" : "") + m;
		}
		
		public String toString(String[] shortDays, boolean useAmPm) {
			return getDaysString(shortDays, "") + " " + getStartString(useAmPm) + " - " + getEndString(useAmPm);
		}
		
		public String toString() {
			return "Free " + toString(new String[] {"M", "T", "W", "R", "F", "S", "U"}, true);
		}
		
		public String toAriaString(String[] longDays, boolean useAmPm) {
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
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof FreeTime)) return false;
			FreeTime f = (FreeTime)o;
			return f.getStart() == getStart() && f.getLength() == getLength() && f.getDayCode() == getDayCode();
		}
	}
	
	public static enum RequestedCourseStatus implements IsSerializable, Serializable {
		NEW_REQUEST,
		ENROLLED,
		SAVED,
		OVERRIDE_APPROVED,
		OVERRIDE_CANCELLED,
		OVERRIDE_PENDING,
		OVERRIDE_NEEDED,
		OVERRIDE_REJECTED,
		CREDIT_LOW, CREDIT_HIGH,
	}
	
	public static class Preference implements IsSerializable, Serializable, Comparable<Preference> {
		private static final long serialVersionUID = 1L;
		Long iId;
		String iText;
		boolean iRequired;
		
		public Preference() {}
		public Preference(Long id, String text, boolean required) {
			iId = id; iText = text; iRequired = required;
		}
		public Preference(Long id) {
			iId = id;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getText() { return iText; }
		public void setText(String text) { iText = text; }
		public boolean isRequired() { return iRequired; }
		public void setRequired(boolean required) { iRequired = required; }
		
		@Override
		public String toString() { return getText() + (isRequired() ? "!" : ""); }
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Preference)) return false;
			return getId().equals(((Preference)o).getId());
		}
		
		@Override
		public int compareTo(Preference p) {
			return getText().compareTo(p.getText());
		}
	}
	
	public static class RequestedCourse implements IsSerializable, Serializable, Comparable<RequestedCourse> {
		private static final long serialVersionUID = 1L;
		private Long iCourseId;
		private String iCourseName;
		private String iCourseTitle;
		private Boolean iReadOnly = null;
		private Boolean iCanDelete = null;
		private Boolean iCanChangeAlternatives = null;
		private Boolean iCanChangePriority = null;
		private List<FreeTime> iFreeTime;
		private Set<Preference> iSelectedIntructionalMethods;
		private Set<Preference> iSelectedClasses;
		private float[] iCredit = null;
		private RequestedCourseStatus iStatus = null;
		private String iStatusNote = null;
		private String iOverrideExternalId = null;
		private Date iOverrideTimeStamp = null;
		private String iRequestorNote = null;
		private String iRequestId = null;
		private Boolean iInactive = null;  
		private Boolean iCanWaitList = null;
		private String iWaitListPosition = null;
		
		public RequestedCourse() {}
		public RequestedCourse(List<FreeTime> freeTime) {
			iFreeTime = freeTime;
		}
		public RequestedCourse(CourseAssignment course, boolean showTitle) {
			iCourseId = course.getCourseId();
			iCourseName = (course.hasUniqueName() && !showTitle ? course.getCourseName() : course.getCourseNameWithTitle()); 
		}
		public RequestedCourse(Long courseId, String courseName) {
			iCourseId = courseId; iCourseName = courseName;
		}
		
		public boolean isCourse() { return hasCourseId() || hasCourseName(); }
		public Long getCourseId() { return iCourseId; }
		public boolean hasCourseId() { return iCourseId != null; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public String getCourseName() { return iCourseName; }
		public boolean hasCourseName() { return iCourseName != null && !iCourseName.isEmpty(); }
		public void setCourseName(String courseName) { iCourseName = courseName; }
		public String getCourseTitle() { return iCourseTitle; }
		public boolean hasCourseTitle() { return iCourseTitle != null && !iCourseTitle.isEmpty(); }
		public void setCourseTitle(String courseTitle) { iCourseTitle = courseTitle; }
		public boolean hasCredit() { return iCredit != null; }
		public float[] getCredit() { return iCredit; }
		public Float getCreditMin() { return iCredit == null ? null : iCredit[0]; }
		public Float getCreditMax() { return iCredit == null ? null : iCredit[1]; }
		public void setCredit(Float minCredit, Float maxCredit) { iCredit = (minCredit == null || maxCredit == null ? null : new float[] { minCredit, maxCredit }); }
		public void setCredit(Float credit) { iCredit = (credit == null ? null : new float[] { credit, credit }); }
		public void setCredit(float[] credit) { iCredit = credit; }
		public void setStatus(RequestedCourseStatus status) { iStatus = status; }
		public RequestedCourseStatus getStatus() { return iStatus; }
		public void setStatusNote(String note) {
			if (note == null)
				iStatusNote = null;
			else
				iStatusNote = note.replace("<br>", "\n");
		}
		public boolean hasStatusNote() { return iStatusNote != null && !iStatusNote.isEmpty() && !" ".equals(iStatusNote); }
		public String getStatusNote() { return iStatusNote; }
		public void setRequestorNote(String note) {
			if (note == null)
				iRequestorNote = null;
			else
				iRequestorNote = note.replace("<br>", "\n");
		}
		public boolean hasRequestorNote() { return iRequestorNote != null && !iRequestorNote.isEmpty() && !" ".equals(iRequestorNote); }
		public String getRequestorNote() { return iRequestorNote; }
		public void setRequestId(String id) { iRequestId = id; }
		public boolean hasRequestId() { return iRequestId != null && !iRequestId.isEmpty() && !" ".equals(iRequestId); }
		public String getRequestId() { return iRequestId; }
		public void setOverrideExternalId(String externalId) { iOverrideExternalId = externalId; }
		public String getOverrideExternalId() { return iOverrideExternalId; }
		public void setOverrideTimeStamp(Date timeStamp) { iOverrideTimeStamp = timeStamp; }
		public Date getOverrideTimeStamp() { return iOverrideTimeStamp; }
		
		public List<FreeTime> getFreeTime() { return iFreeTime; }
		public boolean isFreeTime() { return iFreeTime != null && !iFreeTime.isEmpty(); }
		public void setFreeTime(List<FreeTime> freeTime) { iFreeTime = freeTime; }
		public void addFreeTime(FreeTime freeTime) {
			if (iFreeTime == null) iFreeTime = new ArrayList<FreeTime>();
			iFreeTime.add(freeTime);
		}
		
		public boolean isReadOnly() { return iReadOnly != null && iReadOnly.booleanValue(); }
		public void setReadOnly(Boolean readOnly) { iReadOnly = readOnly; }
		
		public boolean isCanDelete() { return iCanDelete == null || iCanDelete.booleanValue(); }
		public void setCanDelete(Boolean canDelete) { iCanDelete = canDelete; }
		
		public boolean isCanChangePriority() { return iCanChangePriority == null || iCanChangePriority.booleanValue(); }
		public void setCanChangePriority(Boolean canChangePriority) { iCanChangePriority = canChangePriority; }
		
		public boolean isCanChangeAlternatives() { return iCanChangeAlternatives == null || iCanChangeAlternatives.booleanValue(); }
		public void setCanChangeAlternatives(Boolean canChangeAlternatives) { iCanChangeAlternatives = canChangeAlternatives; }
		
		public boolean isInactive() { return iInactive != null && iInactive.booleanValue(); }
		public void setInactive(Boolean inactive) { iInactive = inactive; }
		
		public boolean isCanWaitList() { return iCanWaitList != null && iCanWaitList.booleanValue(); }
		public void setCanWaitList(Boolean canWaitList) { iCanWaitList = canWaitList; }
		
		public boolean isCanNoSub() { return isCourse(); }
		
		public boolean isEmpty() { return !isCourse() && !isFreeTime(); }
		
		public boolean hasSelectedIntructionalMethods() { return iSelectedIntructionalMethods != null && !iSelectedIntructionalMethods.isEmpty(); }
		public Set<Preference> getSelectedIntructionalMethods() { return iSelectedIntructionalMethods; }
		public void setSelectedIntructionalMethod(Long id, String text, boolean required, boolean value) {
			setSelectedIntructionalMethod(new Preference(id, text, required), value);
		}
		public void setSelectedIntructionalMethod(Preference p, boolean value) {
			if (iSelectedIntructionalMethods == null) iSelectedIntructionalMethods = new HashSet<Preference>();
			iSelectedIntructionalMethods.remove(p);
			if (value)
				iSelectedIntructionalMethods.add(p);
		}
		public boolean isSelectedIntructionalMethod(Long id) {
			if (iSelectedIntructionalMethods == null) return false;
			return iSelectedIntructionalMethods.contains(new Preference(id));
		}
		public Preference getIntructionalMethodSelection(Long id) {
			if (iSelectedIntructionalMethods == null || id == null) return null;
			for (Preference p: iSelectedIntructionalMethods)
				if (p.getId().equals(id)) return p;
			return null;
		}
		public boolean isSelectedIntructionalMethod(Long id, boolean required) {
			if (iSelectedIntructionalMethods == null) return false;
			for (Preference p: iSelectedIntructionalMethods)
				if (p.getId().equals(id) && p.isRequired() == required) return true;
			return false;
		}
		public boolean isSelectedIntructionalMethod(Preference p) {
			return isSelectedIntructionalMethod(p.getId(), p.isRequired());
		}
		public int getNrSelectedIntructionalMethods() {
			return (iSelectedIntructionalMethods == null ? 0 : iSelectedIntructionalMethods.size());
		}
		public boolean sameSelectedIntructionalMethods(RequestedCourse rc) {
			if (getNrSelectedIntructionalMethods() != rc.getNrSelectedIntructionalMethods()) return false;
			if (hasSelectedIntructionalMethods()) {
				for (Preference p: getSelectedIntructionalMethods())
					if (!rc.isSelectedIntructionalMethod(p)) return false;
			}
			return true;
		}
		
		public boolean hasSelectedClasses() { return iSelectedClasses != null && !iSelectedClasses.isEmpty(); }
		public Set<Preference> getSelectedClasses() { return iSelectedClasses; }
		public void setSelectedClasses(Set<Preference> classes) { 
			if (iSelectedClasses == null)
				iSelectedClasses = new HashSet<Preference>();
			else
				iSelectedClasses.clear();
			if (classes != null)
				iSelectedClasses.addAll(classes);
		}
		public void setSelectedClass(Long id, String text, boolean required, boolean value) {
			setSelectedClass(new Preference(id, text, required), value);
		}
		public void setSelectedClass(Preference p, boolean value) {
			if (iSelectedClasses == null) iSelectedClasses = new HashSet<Preference>();
			iSelectedClasses.remove(p);
			if (value)
				iSelectedClasses.add(p);
		}
		public boolean isSelectedClass(Long id) {
			if (iSelectedClasses == null || id == null) return false;
			return iSelectedClasses.contains(new Preference(id));
		}
		public Preference getClassSelection(Long id) {
			if (iSelectedClasses == null || id == null) return null;
			for (Preference p: iSelectedClasses)
				if (p.getId().equals(id)) return p;
			return null;
		}
		public boolean isSelectedClass(Long id, boolean required) {
			if (iSelectedClasses == null) return false;
			for (Preference p: iSelectedClasses)
				if (p.getId().equals(id) && p.isRequired() == required) return true;
			return false;
		}
		public boolean isSelectedClass(Preference p) {
			return isSelectedClass(p.getId(), p.isRequired());
		}
		public int getNrSelectedClasses() {
			return (iSelectedClasses == null ? 0 : iSelectedClasses.size());
		}
		public boolean sameSelectedClasses(RequestedCourse rc) {
			if (getNrSelectedClasses() != rc.getNrSelectedClasses()) return false;
			if (hasSelectedClasses()) {
				for (Preference p: getSelectedClasses())
					if (!rc.isSelectedClass(p)) return false;
			}
			return true;
		}
		
		public void clearSelection() {
			if (iSelectedClasses != null) iSelectedClasses.clear();
			if (iSelectedIntructionalMethods != null) iSelectedIntructionalMethods.clear();
		}
		
		public String getWaitListPosition() { return iWaitListPosition; }
		public void setWaitListPosition(String wlPosition) { iWaitListPosition = wlPosition; }
		public boolean hasWaitListPosition() { return iWaitListPosition != null && !iWaitListPosition.isEmpty(); }
		
		@Override
		public int hashCode() {
			return (isCourse() ? getCourseName() : toString()).toLowerCase().hashCode();
		}
		
		@Override
		public String toString() {
			if (isCourse()) {
				return getCourseName() +
						(hasSelectedIntructionalMethods() ? " method:" + getSelectedIntructionalMethods() : "") +
						(hasSelectedClasses() ? " section:" + getSelectedClasses() : "") + 
						(isReadOnly() ? " [S]" : "") + (hasCourseId() ? "[i]" : "");
			}
			if (isFreeTime()) {
				String ret = "";
				for (FreeTime ft: getFreeTime())
					ret += (ret.isEmpty() ? "" : ", ") + ft.toString();
				return ret;
			}
			return "N/A";
		}
		
		public String toString(StudentSectioningConstants CONSTANTS) {
			if (isCourse()) return getCourseName();
			if (isFreeTime()) {
				String display = "";
				String lastDays = null;
				for (CourseRequestInterface.FreeTime ft: getFreeTime()) {
					if (display.length() > 0) display += ", ";
					String days = ft.getDaysString(CONSTANTS.shortDays(), "");
					if (ft.getDays().size() == CONSTANTS.freeTimeDays().length && !ft.getDays().contains(5) && !ft.getDays().contains(6)) days = "";
					display += (days.isEmpty() || days.equals(lastDays) ? "" : days + " ") + ft.getStartString(CONSTANTS.useAmPm()) + " - " + ft.getEndString(CONSTANTS.useAmPm());
					lastDays = days;
				}
				return CONSTANTS.freePrefix() + display;
			}
			return "";
		}
		
		public String toAriaString(StudentSectioningConstants CONSTANTS) {
			if (isCourse()) return getCourseName();
			if (isFreeTime()) {
				String status = "";
				for (FreeTime ft: getFreeTime())
					status += (status.isEmpty() ? "" : " ") + ft.toAriaString(CONSTANTS.longDays(), CONSTANTS.useAmPm());
				return status;
			}
			return "";
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o instanceof String) return ((String)o).equalsIgnoreCase(getCourseName());
			if (o instanceof Long) return ((Long)o).equals(getCourseId());
			if (o instanceof RequestedCourse) {
				RequestedCourse c = (RequestedCourse)o;
				if (c.hasCourseId() && hasCourseId())
					return getCourseId().equals(c.getCourseId());
				else if (c.hasCourseName() && hasCourseName())
					return getCourseName().equalsIgnoreCase(c.getCourseName());
				else if (c.isFreeTime() && isFreeTime())
					return c.getFreeTime().equals(getFreeTime());
			}
			if (o instanceof CourseAssignment) {
				CourseAssignment c = (CourseAssignment)o;
				if (hasCourseId())
					return getCourseId().equals(c.getCourseId());
				else if (hasCourseName())
					return getCourseName().equalsIgnoreCase(c.getCourseName()) || getCourseName().equalsIgnoreCase(c.getCourseNameWithTitle());
			}
			if (o instanceof DegreeCourseInterface) {
				DegreeCourseInterface c = (DegreeCourseInterface)o;
				if (hasCourseId() && c.getCourseId() != null)
					return getCourseId().equals(c.getCourseId());
				else if (hasCourseName())
					return getCourseName().equalsIgnoreCase(c.getCourseName()) || getCourseName().equalsIgnoreCase(c.getCourseNameWithTitle());
			}
			return false;
		}
		@Override
		public int compareTo(RequestedCourse o) {
			return toString().compareToIgnoreCase(o.toString());
		}
	}
	
	public boolean hasConfirmations() { return iConfirmations != null && !iConfirmations.isEmpty(); }
	public void addConfirmation(CourseMessage message) {
		if (iConfirmations != null) iConfirmations = new ArrayList<CourseMessage>();
		iConfirmations.add(message);
	}
	public List<CourseMessage> getConfirmations() { return iConfirmations; }
	public void setConfirmations(Collection<CourseMessage> confirmations) {
		iConfirmations = (confirmations == null ? null : new ArrayList<CourseMessage>(confirmations));
	}
	public List<CourseMessage> getConfirmations(String courseName) {
		List<CourseMessage> ret = new ArrayList<CourseMessage>();
		if (hasConfirmations())
			for (CourseMessage m: getConfirmations())
				if (m.hasCourse() && courseName.equals(m.getCourse())) ret.add(m);
		return ret;
	}
	public String getConfirmation(String courseName, String delim, String... exclude) {
		if (!hasConfirmations()) return null;
		String ret = null;
		if (hasConfirmations())
			m: for (CourseMessage m: getConfirmations())
				if (m.hasCourse() && courseName.equals(m.getCourse())) {
					for (String e: exclude)
						if (e.equals(m.getCode())) continue m;
					if (ret == null)
						ret = m.getMessage();
					else
						ret += delim + m.getMessage();
				}
		return ret;
	}
	public boolean isError(String courseName) {
		if (!hasConfirmations()) return false;
		if (hasConfirmations())
			for (CourseMessage m: getConfirmations())
				if (m.hasCourse() && courseName.equals(m.getCourse())) {
					if (m.isError()) return true;
				}
		return false;
	}
	
	public static class Request implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private List<RequestedCourse> iRequestedCourse = null;
		private Boolean iWaitList = false;
		private Boolean iNoSub = false;
		private Integer iCritical = null;
		private Date iTimeStamp = null;
		private Date iWaitListedTimeStamp = null;
		private String iFilter = null;
		private String iAdvisorCredit = null;
		private String iAdvisorNote = null;
		
		public Request() {}
		
		public List<RequestedCourse> getRequestedCourse() { return iRequestedCourse; }
		public int countRequestedCourses() { return iRequestedCourse == null ? 0 : iRequestedCourse.size(); }
		public RequestedCourse getRequestedCourse(int index) {
			if (iRequestedCourse != null && index < iRequestedCourse.size())
				return iRequestedCourse.get(index);
			return null;
		}
		public RequestedCourse getRequestedCourse(Long courseId) {
			if (iRequestedCourse == null) return null;
			for (RequestedCourse rc: iRequestedCourse)
				if (courseId.equals(rc.getCourseId())) return rc;
			return null;
		}
		public boolean hasRequestedCourse() { return iRequestedCourse != null && !iRequestedCourse.isEmpty(); } //&& !hasRequestedFreeTime(); }
		public void addRequestedCourse(RequestedCourse requestedCourse) {
			if (iRequestedCourse == null)
				iRequestedCourse = new ArrayList<RequestedCourse>();
			iRequestedCourse.add(requestedCourse);
		}
		public boolean hasRequestedCourse(CourseAssignment course) {
			if (iRequestedCourse == null) return false;
			for (RequestedCourse rc: iRequestedCourse)
				if (rc.equals(course)) return true;
			return false;
		}
		public boolean hasRequestedCourse(RequestedCourse course) {
			if (iRequestedCourse == null) return false;
			for (RequestedCourse rc: iRequestedCourse)
				if (rc.equals(course)) return true;
			return false;
		}
		public boolean hasRequestedCourseActive(RequestedCourse course) {
			if (iRequestedCourse == null) return false;
			for (RequestedCourse rc: iRequestedCourse)
				if (rc.equals(course) && !rc.isInactive()) return true;
			return false;
		}
		public RequestedCourse update(RequestedCourse rc) {
			if (iRequestedCourse == null) return null;
			for (int i = 0; i < iRequestedCourse.size(); i++) {
				RequestedCourse old = iRequestedCourse.get(i);
				if (rc.equals(old)) {
					iRequestedCourse.set(i, rc);
					return old;
				}
			}
			return null;
		}

		/*
		public List<FreeTime> getRequestedFreeTime() {
			if (iRequestedCourse != null)
				for (RequestedCourse course: iRequestedCourse)
					if (course.isFreeTime()) return course.getFreeTime();
			return null;
		}
		public boolean hasRequestedFreeTime() { return getRequestedFreeTime() != null; }
		*/
		
		public boolean isEmpty() { return !hasRequestedCourse(); }
		public boolean isReadOnly() {
			if (iRequestedCourse == null) return false;
			for (RequestedCourse rc: iRequestedCourse)
				if (rc.isReadOnly()) return true;
			return false;
		}
		
		public boolean isCanDelete() {
			if (iRequestedCourse == null) return true;
			for (RequestedCourse rc: iRequestedCourse)
				if (!rc.isCanDelete()) return false;
			return true;
		}
		
		public boolean isCanChangePriority() {
			if (iRequestedCourse == null) return true;
			for (RequestedCourse rc: iRequestedCourse)
				if (!rc.isCanChangePriority()) return false;
			return true;
		}
		
		public boolean isCanChangeAlternatives() {
			if (iRequestedCourse == null) return true;
			for (RequestedCourse rc: iRequestedCourse)
				if (!rc.isCanChangeAlternatives()) return false;
			return true;
		}
		
		public boolean hasWaitList() { return iWaitList != null; }
		public boolean isWaitList() { return iWaitList != null && iWaitList.booleanValue(); }
		public void setWaitList(Boolean waitList) { iWaitList = waitList; }
		
		public boolean isWaitListOrNoSub(WaitListMode wlMode, Set<Long> advisorWaitListedCourseIds) {
			if (wlMode == WaitListMode.WaitList && isCanWaitList()) {
				if (iWaitList != null && iWaitList.booleanValue()) return true;
			}
			if (wlMode == WaitListMode.NoSubs) {
				if (iNoSub != null && iNoSub.booleanValue()) return true;
			}
			if (advisorWaitListedCourseIds != null && hasRequestedCourse()) {
				for (RequestedCourse rc: getRequestedCourse()) {
					if (rc.hasCourseId() && advisorWaitListedCourseIds.contains(rc.getCourseId())) return true;
				}
			}
			return false;
		}

		public boolean isCanWaitList() {
			if (iRequestedCourse == null) return false;
			for (RequestedCourse rc: iRequestedCourse) {
				if (rc.isCanWaitList()) return true;
				break;
			}
			return false;
		}
		
		public boolean hasNoSub() { return iNoSub != null; }
		public boolean isNoSub() { return iNoSub != null && iNoSub.booleanValue(); }
		public void setNoSub(Boolean noSub) { iNoSub = noSub; }
		
		public boolean isCanNoSub() {
			if (iRequestedCourse == null) return false;
			for (RequestedCourse rc: iRequestedCourse) {
				if (rc.isCourse()) return true;
				break;
			}
			return false;
		}
		
		public boolean isWaitlistOrNoSub(WaitListMode wlMode) {
			if (wlMode == WaitListMode.WaitList) return isWaitList();
			if (wlMode == WaitListMode.NoSubs) return isNoSub();
			return false;
		}

		public boolean hasCritical() { return iCritical != null; }
		public boolean isCritical() { return iCritical != null && iCritical.intValue() == 1; }
		public boolean isImportant() { return iCritical != null && iCritical.intValue() == 2; }
		public Integer getCritical() { return iCritical; }
		public void setCritical(Integer critical) { iCritical = critical; }

		public boolean hasTimeStamp() { return iTimeStamp != null; }
		public Date getTimeStamp() { return iTimeStamp; }
		public void setTimeStamp(Date ts) { iTimeStamp = ts; }
		
		public boolean hasWaitListedTimeStamp() { return iWaitListedTimeStamp != null; }
		public Date getWaitListedTimeStamp() { return iWaitListedTimeStamp; }
		public void setWaitListedTimeStamp(Date ts) { iWaitListedTimeStamp = ts; }
		
		public boolean hasFilter() { return iFilter != null && !iFilter.isEmpty(); }
		public String getFilter() { return iFilter; }
		public void setFilter(String filter) { iFilter = filter; }
		
		public boolean hasAdvisorCredit() { return iAdvisorCredit != null && !iAdvisorCredit.isEmpty(); }
		public String getAdvisorCredit() { return iAdvisorCredit; }
		public void setAdvisorCredit(String credit) { iAdvisorCredit = credit; }
		public float getAdvisorCreditMin() {
			if (iAdvisorCredit == null || iAdvisorCredit.isEmpty()) return 0f;
			try {
				return Float.parseFloat(iAdvisorCredit.replaceAll("\\s",""));
			} catch (NumberFormatException e) {}
			if (iAdvisorCredit.contains("-")) {
				try {
					return Float.parseFloat(iAdvisorCredit.substring(0, iAdvisorCredit.indexOf('-')).replaceAll("\\s",""));
				} catch (NumberFormatException e) {}	
			}
			return 0f;
		}
		public float getAdvisorCreditMax() {
			if (iAdvisorCredit == null || iAdvisorCredit.isEmpty()) return 0f;
			try {
				return Float.parseFloat(iAdvisorCredit.replaceAll("\\s",""));
			} catch (NumberFormatException e) {}
			if (iAdvisorCredit.contains("-")) {
				try {
					return Float.parseFloat(iAdvisorCredit.substring(1 + iAdvisorCredit.indexOf('-')).replaceAll("\\s",""));
				} catch (NumberFormatException e) {}	
			}
			return 0f;
		}
		
		public boolean hasAdvisorNote() { return iAdvisorNote != null && !iAdvisorNote.isEmpty(); }
		public String getAdvisorNote() { return iAdvisorNote; }
		public void setAdvisorNote(String note) { iAdvisorNote = note; }
		public void addAdvisorNote(String note) {
			if (iAdvisorNote == null)
				iAdvisorNote = note;
			else if (!iAdvisorNote.contains(note))
				iAdvisorNote += "\n" + note;
		}
		
		public boolean isInactive() {
			if (iRequestedCourse == null) return false;
			// all requests are inactive -> inactive
			for (RequestedCourse rc: iRequestedCourse) {
				if (!rc.isInactive()) return false;
			}
			return true;
		}
		
		public boolean isActive() {
			if (iRequestedCourse == null) return false;
			// one request is active (not inactive) -> active
			for (RequestedCourse rc: iRequestedCourse) {
				if (!rc.isInactive()) return true;
			}
			return false;
		}
		
		public String toString() {
			return (hasRequestedCourse() ? iRequestedCourse.toString() : "-") + (isWaitList() ? " (w)" : "");
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Request)) return false;
			Request r = (Request)o;
			if (isWaitList() != r.isWaitList() || isNoSub() != r.isNoSub() || countRequestedCourses() != r.countRequestedCourses()) return false;
			for (int i = 0; i < countRequestedCourses(); i++) {
				RequestedCourse c1 = getRequestedCourse(i);
				RequestedCourse c2 = r.getRequestedCourse(i);
				if (!c1.equals(c2) || !c1.sameSelectedClasses(c2) || !c1.sameSelectedIntructionalMethods(c2)) return false;
			}
			if (!(hasAdvisorNote() ? getAdvisorNote() : "").equals(r.hasAdvisorNote() ? r.getAdvisorNote() : "")) return false;
			if (!(hasAdvisorCredit() ? getAdvisorCredit() : "").equals(r.hasAdvisorCredit() ? r.getAdvisorCredit() : "")) return false;
			return true;
		}
		
		public boolean sameCourses(Request r) {
			if (countRequestedCourses() != r.countRequestedCourses()) return false;
			for (int i = 0; i < countRequestedCourses(); i++) {
				RequestedCourse c1 = getRequestedCourse(i);
				RequestedCourse c2 = r.getRequestedCourse(i);
				if (!c1.equals(c2) || !c1.sameSelectedClasses(c2) || !c1.sameSelectedIntructionalMethods(c2)) return false;
			}
			return true;
		}
	}
	
	public String toString() {
		String ret = "CourseRequests(student = " + getStudentId() + ", session = " + getSessionId() + ", requests = {";
		int idx = 1;
		for (Request r: iCourses)
			ret += "\n   " + (idx++) + ". " + r;
		idx = 1;
		for (Request r: iAlternatives)
			ret += "\n  A" + (idx++) + ". " + r;
		return ret + "\n})";
		
	}
	
	public static class RequestPriority implements IsSerializable, Serializable, Comparable<RequestPriority> {
		private static final long serialVersionUID = 1L;
		private boolean iAlternative = false;
		private int iPriority = 0;
		private int iChoice = 0;
		private CourseRequestInterface.Request iRequest;
		
		RequestPriority(boolean alternative, int priority, int choice, CourseRequestInterface.Request request) {
			iAlternative = alternative;
			iPriority = priority;
			iChoice = choice;
			iRequest = request;
		}
		
		public boolean isAlternative() { return iAlternative; }
		public int getPriority() { return iPriority; }
		public int getChoice() { return iChoice; }
		public CourseRequestInterface.Request getRequest() { return iRequest; }
		
		public String toString() {
			if (iAlternative) {
				switch (iChoice) {
				case 0: return "Alt " + iPriority + ".";
				case 1: return "Alt " + iPriority + "A.";
				default: return "Alt " + iPriority + "B.";
				}
			} else {
				switch (iChoice) {
				case 0: return iPriority + ".";
				case 1: return iPriority + "A.";
				default: return iPriority + "B.";
				}
			}
		}
		
		public String toString(StudentSectioningMessages MESSAGES) {
			if (iAlternative) {
				switch (iChoice) {
				case 0: return MESSAGES.degreeRequestedAlternative(iPriority);
				case 1: return MESSAGES.degreeRequestedAlternativeFirstAlt(iPriority);
				case 2: return MESSAGES.degreeRequestedAlternativeSecondAlt(iPriority);
				default: return MESSAGES.degreeRequestedCourseAlt(iPriority, (iChoice > 26 ? "" + (char)('A' + (iChoice - 27) / 26) + (char)('A' + (iChoice - 1) % 26) : "" + (char)('A' + (iChoice - 1) % 26)));
				}
			} else {
				switch (iChoice) {
				case 0: return MESSAGES.degreeRequestedCourse(iPriority);
				case 1: return MESSAGES.degreeRequestedCourseFirstAlt(iPriority);
				case 2: return MESSAGES.degreeRequestedCourseSecondAlt(iPriority);
				default: return MESSAGES.degreeRequestedCourseAlt(iPriority,(iChoice > 26 ? "" + (char)('A' + (iChoice - 27) / 26) + (char)('A' + (iChoice - 1) % 26) : "" + (char)('A' + (iChoice - 1) % 26)));
				}
			}
			
		}

		@Override
		public int compareTo(RequestPriority p) {
			if (isAlternative() != p.isAlternative())
				return isAlternative() ? 1 : -1;
			if (getPriority() != p.getPriority())
				return (getPriority() < p.getPriority() ? -1 : 1);
			if (getChoice() != p.getChoice())
				return (getChoice() < p.getChoice() ? -1 : 1);
			return 0;
		}
	}
	
	public static class CheckCoursesResponse implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Set<CourseMessage> iMessages = new TreeSet<CourseMessage>();
		private Map<Integer, String[]> iConfirmationSetup = null;
		private String iErrorMessage = null;
		private String iCreditWarning = null;
		private String iCreditNote = null;
		private Float iMaxCreditNeeded = null;
		private RequestedCourseStatus iMaxCreditOverrideStatus = null;
		
		public CheckCoursesResponse() {}
		
		public CheckCoursesResponse(Collection<CourseMessage> messages) {
			if (messages != null && !messages.isEmpty())
				iMessages = new TreeSet<CourseMessage>(messages);
		}
		
		public boolean hasMessages() { return iMessages != null && !iMessages.isEmpty(); }
		public Set<CourseMessage> getMessages() { return iMessages; }
		public void addMessage(CourseMessage message) {
			iMessages.add(message);
		}
		
		public CourseMessage addMessage(Long courseId, String course, String code, String message, Integer confirm, int order) {
			CourseMessage m = new CourseMessage();
			m.setCourseId(courseId);
			m.setCourse(course);
			m.setCode(code);
			m.setMessage(message);
			m.setError(false);
			m.setConfirm(confirm);
			m.setOrder(order);
			addMessage(m);
			return m;
		}
		
		public CourseMessage addMessage(Long courseId, String course, String code, String message, Integer confirm) {
			CourseMessage m = new CourseMessage();
			m.setCourseId(courseId);
			m.setCourse(course);
			m.setCode(code);
			m.setMessage(message);
			m.setError(false);
			m.setConfirm(confirm);
			m.setOrder(0);
			addMessage(m);
			return m;
		}
		public CourseMessage addError(Long courseId, String course, String code, String message) {
			CourseMessage m = new CourseMessage();
			m.setCourseId(courseId);
			m.setCourse(course);
			m.setCode(code);
			m.setMessage(message);
			m.setError(true);
			m.setConfirm(null);
			addMessage(m);
			return m;
		}
		public CourseMessage addConfirmation(String message, Integer confirm, Integer order) {
			CourseMessage m = new CourseMessage();
			m.setCode("CONF_MSG");
			m.setMessage(message);
			m.setError(false);
			m.setConfirm(confirm);
			m.setOrder(order);
			addMessage(m);
			return m;
		}
		
		public CourseMessage addCheckBox(String message, Integer confirm, Integer order) {
			CourseMessage m = new CourseMessage();
			m.setCode("CHECK_BOX");
			m.setMessage(message);
			m.setError(false);
			m.setConfirm(confirm);
			m.setOrder(order);
			addMessage(m);
			return m;
		}
		
		public boolean isError() {
			if (hasMessages())
				for (CourseMessage m: getMessages())
					if (m.isError()) return true;
			return false;
		}
		public boolean isOK() { return !hasMessages(); }
		public boolean isWarning() { return hasMessages() && !isError(); }
		public boolean isConfirm() {
			if (hasMessages())
				for (CourseMessage m: getMessages())
					if (m.isConfirm()) return true;
			return false;
		}
		public Set<Integer> getConfirms() {
			Set<Integer> ret = new TreeSet<Integer>();
			if (hasMessages())
				for (CourseMessage m: getMessages())
					if (m.isConfirm()) ret.add(m.getConfirm());
			return ret;
		}
		
		public Float getMaxCreditNeeded() { return iMaxCreditNeeded; }
		public void setMaxCreditNeeded(Float maxCreditNeeded) { iMaxCreditNeeded = maxCreditNeeded; }
		
		public List<CourseMessage> getMessages(String courseName) {
			List<CourseMessage> ret = new ArrayList<CourseMessage>();
			if (hasMessages())
				for (CourseMessage m: getMessages())
					if (m.hasCourse() && courseName.equals(m.getCourse())) ret.add(m);
			return ret;
		}
		
		public String getMessage(String courseName, String delim, String... exclude) {
			if (!hasMessages()) return null;
			String ret = null;
			if (hasMessages())
				m: for (CourseMessage m: getMessages())
					if (m.hasCourse() && courseName.equals(m.getCourse())) {
						for (String e: exclude)
							if (e.equals(m.getCode())) continue m;
						if (ret == null)
							ret = m.getMessage();
						else
							ret += delim + m.getMessage();
					}
			return ret;
		}
		
		public RequestedCourseStatus getStatus(String courseName) {
			RequestedCourseStatus status = null;
			if (hasMessages())
				for (CourseMessage m: getMessages()) {
					if (m.getStatus() != null && m.hasCourse() && courseName.equals(m.getCourse())) {
						if (status == null || m.getStatus().ordinal() > status.ordinal()) status = m.getStatus();
					}
				}
			return status;
		}
		
		public String getMessageWithColor(String courseName, String delim, String... exclude) {
			if (!hasMessages()) return null;
			String ret = null;
			if (hasMessages())
				m: for (CourseMessage m: getMessages())
					if (m.hasCourse() && courseName.equals(m.getCourse())) {
						for (String e: exclude)
							if (e.equals(m.getCode())) continue m;
						if (ret == null)
							ret = (m.isError() ? "<span class='text-red'>" : "<span class='text-orange'>") + m.getMessage() + "</span>";
						else
							ret += delim + (m.isError() ? "<span class='text-red'>" : "<span class='text-orange'>") + m.getMessage() + "</span>";
					}
			return ret;
		}
		
		public boolean hasMessage(String courseName, String code) {
			if (hasMessages())
				for (CourseMessage m: getMessages())
					if (m.hasCourse() && courseName.equals(m.getCourse()) && code.equals(m.getCode())) return true;
			return false;
		}
		
		public boolean isError(String courseName) {
			if (!hasMessages()) return false;
			if (hasMessages())
				for (CourseMessage m: getMessages())
					if (m.hasCourse() && courseName.equals(m.getCourse())) {
						if (m.isError()) return true;
					}
			return false;
		}
		
		public boolean isConfirm(String courseName) {
			if (!hasMessages()) return false;
			if (hasMessages())
				for (CourseMessage m: getMessages())
					if (m.hasCourse() && courseName.equals(m.getCourse())) {
						if (m.isConfirm()) return true;
					}
			return false;
		}
		
		public String getConfirmations(int confirm, String delim) {
			if (!hasMessages()) return null;
			String ret = null;
			if (hasMessages())
				for (CourseMessage m: getMessages()) {
					if (confirm != m.getConfirm()) continue;
					if (ret == null)
						ret = (m.hasCourse() ? m.getCourse() + ": " : "") + m.getMessage();
					else
						ret += delim + (m.hasCourse() ? m.getCourse() + ": " : "") + m.getMessage();
				}
			return ret;
		}
		
		public void setConfirmation(int confirm, String dialogTitle, String yesButton, String noButton, String yesButtonTitle, String noButtonTitle) {
			if (iConfirmationSetup == null) iConfirmationSetup = new HashMap<Integer, String[]>();
			iConfirmationSetup.put(confirm, new String[] {dialogTitle, yesButton, noButton, yesButtonTitle, noButtonTitle});
		}
		public String getConfirmationTitle(int confirm, String defaultTitle) {
			if (iConfirmationSetup == null) return defaultTitle;
			String[] confirmation = iConfirmationSetup.get(confirm);
			return (confirmation == null || confirmation[0] == null ? defaultTitle : confirmation[0]);
		}
		public String getConfirmationYesButton(int confirm, String defaultTitle) {
			if (iConfirmationSetup == null) return defaultTitle;
			String[] confirmation = iConfirmationSetup.get(confirm);
			return (confirmation == null || confirmation[1] == null ? defaultTitle : confirmation[1]);
		}
		public String getConfirmationNoButton(int confirm, String defaultTitle) {
			if (iConfirmationSetup == null) return defaultTitle;
			String[] confirmation = iConfirmationSetup.get(confirm);
			return (confirmation == null || confirmation[2] == null ? defaultTitle : confirmation[2]);
		}
		public String getConfirmationYesButtonTitle(int confirm, String defaultTitle) {
			if (iConfirmationSetup == null) return defaultTitle;
			String[] confirmation = iConfirmationSetup.get(confirm);
			return (confirmation == null || confirmation[3] == null ? defaultTitle : confirmation[3]);
		}
		public String getConfirmationNoButtonTitle(int confirm, String defaultTitle) {
			if (iConfirmationSetup == null) return defaultTitle;
			String[] confirmation = iConfirmationSetup.get(confirm);
			return (confirmation == null || confirmation[4] == null ? defaultTitle : confirmation[4]);
		}
		
		public boolean hasErrorMessage() { return iErrorMessage != null && !iErrorMessage.isEmpty(); }
		public void setErrorMessage(String message) {
			if (iErrorMessage == null) iErrorMessage = message;
			else if (!iErrorMessage.contains(message)) iErrorMessage += "\n" + message;
		}
		public String getErrorMessage() { return iErrorMessage; }
		
		public boolean hasCreditWarning() { return iCreditWarning != null && !iCreditWarning.isEmpty(); }
		public String getCreditWarning() { return iCreditWarning; }
		public void setCreditWarning(String warning) { iCreditWarning = warning; }
		public boolean hasCreditNote() { return iCreditNote != null && !iCreditNote.isEmpty() && !" ".equals(iCreditNote); }
		public String getCreditNote() { return iCreditNote; }
		public void setCreditNote(String note) { iCreditNote = note; }
		
		public void setMaxCreditOverrideStatus(RequestedCourseStatus status) { iMaxCreditOverrideStatus = status; }
		public RequestedCourseStatus getMaxCreditOverrideStatus() { return iMaxCreditOverrideStatus; }
		
		@Override
		public String toString() { return hasMessages() ? getMessages().toString() : "[]"; }
	}
	
	public static class CourseMessage implements IsSerializable, Serializable, Comparable<CourseMessage> {
		private static final long serialVersionUID = 1L;
		private Long iCourseId;
		private String iCourse;
		private boolean iError = true;
		private String iMessage;
		private String iCode;
		private Integer iConfirm;
		private Integer iOrder;
		private RequestedCourseStatus iStatus;
		
		public CourseMessage() {}
		
		public Long getCourseId() { return iCourseId; }
		public boolean hasCourseId() { return iCourseId != null; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }

		public String getCourse() { return iCourse; }
		public boolean hasCourse() { return iCourse != null && !iCourse.isEmpty(); }
		public void setCourse(String course) { iCourse = course; }
		
		public boolean isError() { return iError; }
		public void setError(boolean error) { iError = error; }
		
		public boolean isConfirm() { return iConfirm != null; }
		public void setConfirm(Integer confirm) { iConfirm = confirm; }
		public int getConfirm() { return iConfirm == null ? -1 : iConfirm; }
		
		public void setOrder(Integer order) { iOrder = order; }
		public boolean hasOrder() { return iOrder != null; }
		public int getOrder() { return iOrder == null ? Integer.MAX_VALUE : iOrder; }
		
		public String getMessage() { return iMessage; }
		public void setMessage(String message) { iMessage = message; }
		
		public String getCode() { return iCode; }
		public void setCode(String code) { iCode = code; }
		
		public RequestedCourseStatus getStatus() { return iStatus; }
		public CourseMessage setStatus(RequestedCourseStatus status) { iStatus = status; return this; }
		
		@Override
		public String toString() { return (hasCourse() ? getCourse() + ": " : "") + getMessage() + (getStatus() == null ? "" : " (" + getStatus() + ")"); }
		
		@Override
		public int hashCode() { return (hasCourse() ? getCourse() + ":" + getCode() : getCode()).hashCode(); }
		
		protected int compare(int x, int y) {
			return (x < y) ? -1 : ((x == y) ? 0 : 1);
		}

		@Override
		public int compareTo(CourseMessage m) {
			int cmp = compare(getConfirm(), m.getConfirm());
			if (cmp != 0) return cmp;
			cmp = compare(getOrder(), m.getOrder());
			if (cmp != 0) return cmp;
			if (hasCourse() != m.hasCourse()) return (hasCourse() ? -1 : 1);
			if (hasCourse()) {
				cmp = getCourse().compareTo(m.getCourse());
				if (cmp != 0) return cmp;
			}
			return getCode().compareTo(m.getCode());
		}
	}
	
	public void addConfirmationError(Long courseId, String course, String code, String message, RequestedCourseStatus status, Integer order) {
		if (iConfirmations == null) iConfirmations = new ArrayList<CourseMessage>();
		CourseMessage m = new CourseMessage();
		m.setCourseId(courseId);
		m.setCourse(course);
		m.setCode(code);
		m.setMessage(message);
		m.setError(true);
		m.setConfirm(null);
		m.setStatus(status);
		m.setOrder(order);
		iConfirmations.add(m);
	}
	
	public void addConfirmationMessage(Long courseId, String course, String code, String message, RequestedCourseStatus status, Integer order) {
		if (iConfirmations == null) iConfirmations = new ArrayList<CourseMessage>();
		CourseMessage m = new CourseMessage();
		m.setCourseId(courseId);
		m.setCourse(course);
		m.setCode(code);
		m.setMessage(message);
		m.setError(false);
		m.setConfirm(null);
		m.setStatus(status);
		m.setOrder(order);
		iConfirmations.add(m);
	}
	
	public void addConfirmationMessage(Long courseId, String course, String code, String message, Integer order) {
		addConfirmationMessage(courseId, course, code, message, null, order);
	}
	
	public boolean hasErrorMessage() { return iErrorMessage != null && !iErrorMessage.isEmpty(); }
	public void setErrorMessage(String message) {
		if (iErrorMessage == null) iErrorMessage = message;
		else if (!iErrorMessage.contains(message)) iErrorMessage += "\n" + message;
	}
	public String getErrorMessaeg() { return iErrorMessage; }
	
	public String getSpecRegDashboardUrl() { return iSpecRegDashboardUrl; }
	public boolean hasSpecRegDashboardUrl() { return iSpecRegDashboardUrl != null && !iSpecRegDashboardUrl.isEmpty(); }
	public void setSpecRegDashboardUrl(String url) { iSpecRegDashboardUrl = url; }
	public void setRequestorNote(String note) {
		if (note == null)
			iRequestorNote = null;
		else
			iRequestorNote = note.replace("<br>", "\n");
	}
	public boolean hasRequestorNote() { return iRequestorNote != null && !iRequestorNote.isEmpty() && !" ".equals(iRequestorNote); }
	public String getRequestorNote() { return iRequestorNote; }
	public void setRequestId(String id) { iRequestId = id; }
	public boolean hasRequestId() { return iRequestId != null && !iRequestId.isEmpty() && !" ".equals(iRequestId); }
	public String getRequestId() { return iRequestId; }
	
	public boolean updateRequestorNote(String requestId, String note) {
		boolean changed = false;
		if (requestId.equals(getRequestId())) {
			setRequestorNote(note); changed = true;
		}
		for (Request r: getCourses()) {
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (requestId.equals(rc.getRequestId())) {
						rc.setRequestorNote(note); changed = true;
					}
				}
		}
		for (Request r: getAlternatives()) {
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (requestId.equals(rc.getRequestId())) {
						rc.setRequestorNote(note); changed = true;
					}
				}
		}
		return changed;
	}
	
	public void removeInactiveDuplicates() {
		Set<RequestedCourse> activeCourses = new HashSet<RequestedCourse>();
		for (Request r: iCourses) {
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse())
					if (!rc.isInactive() && rc.isCourse())
						activeCourses.add(rc);
		}
		for (Request r: iAlternatives) {
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse())
					if (!rc.isInactive() && rc.isCourse())
						activeCourses.add(rc);
		}
		for (Request r: iCourses) {
			if (r.hasRequestedCourse())
				for (Iterator<RequestedCourse> i = r.getRequestedCourse().iterator(); i.hasNext(); ) {
					RequestedCourse rc = i.next();
					if (rc.isInactive() && rc.isCourse() && activeCourses.contains(rc))
						i.remove();
				}
		}
		for (Request r: iAlternatives) {
			if (r.hasRequestedCourse())
				for (Iterator<RequestedCourse> i = r.getRequestedCourse().iterator(); i.hasNext(); ) {
					RequestedCourse rc = i.next();
					if (rc.isInactive() && rc.hasCourseId() && activeCourses.contains(rc))
						i.remove();
				}
		}
	}
	
	public void moveActiveSubstitutionsUp() {
		// Count the number of inactive course requests in the upper table
		int nrInactive = 0;
		for (CourseRequestInterface.Request request: getCourses())
			if (request.isInactive()) nrInactive ++;
		// For each inactive request, move one active request from substitutes up
		for (Iterator<CourseRequestInterface.Request> i = getAlternatives().iterator(); i.hasNext() && nrInactive > 0; ) {
			CourseRequestInterface.Request request = i.next();
			if (request.isActive()) {
				getCourses().add(request);
				i.remove();
				nrInactive --;
			}
		}
	}
	
	public boolean removeDuplicates() {
		Set<RequestedCourse> courses = new HashSet<RequestedCourse>();
		boolean deleted = false;
		for (Request r: iCourses) {
			if (r.hasRequestedCourse())
				for (Iterator<RequestedCourse> i = r.getRequestedCourse().iterator(); i.hasNext(); ) {
					RequestedCourse rc = i.next();
					if (rc.isCourse() && !courses.add(rc)) {
						i.remove();
						deleted = true;
					}
				}
		}
		for (Request r: iAlternatives) {
			if (r.hasRequestedCourse())
				for (Iterator<RequestedCourse> i = r.getRequestedCourse().iterator(); i.hasNext(); ) {
					RequestedCourse rc = i.next();
					if (rc.isCourse() && !courses.add(rc)) {
						i.remove();
						deleted = true;
					}
				}
		}
		return deleted;
	}
	
	public boolean applyAdvisorRequests(CourseRequestInterface req) {
		if (req == null || req.isEmpty()) return false;
		boolean changed = false;
		for (Request r: req.getCourses()) {
			if (!r.hasRequestedCourse()) continue;
			boolean skip = false;
			for (RequestedCourse rc: r.getRequestedCourse()) {
				if (rc.hasCourseId() && getRequestPriority(rc) != null) { skip = true; break; }
			}
			if (!skip) {
				getCourses().add(r);
				changed = true;
			}
		}
		for (Request r: req.getAlternatives()) {
			if (!r.hasRequestedCourse()) continue;
			boolean skip = false;
			for (RequestedCourse rc: r.getRequestedCourse()) {
				if (rc.hasCourseId() && getRequestPriority(rc) != null) { skip = true; break; }
			}
			if (!skip) {
				getAlternatives().add(r);
				changed = true;
			}
		}
		return changed;
	}
	
	public boolean hasWaitListMode() {
		return iMode != null;
	}
	public WaitListMode getWaitListMode() {
		if (iMode == null) return WaitListMode.None;
		return iMode;
	}
	public void setWaitListMode(WaitListMode mode) {
		iMode = mode;
	}
	
	public boolean hasWaitListChecks() {
		return iWaitListChecks != null;
	}
	public CheckCoursesResponse getWaitListChecks() {
		return iWaitListChecks;
	}
	public void setWaitListChecks(CheckCoursesResponse waitListChecks) {
		iWaitListChecks = waitListChecks;
		setConfirmations(waitListChecks == null ? null : waitListChecks.getMessages());
		setErrorMessage(waitListChecks == null ? null : waitListChecks.getErrorMessage());
		setCreditNote(waitListChecks == null ? null : waitListChecks.getCreditNote());
		setCreditWarning(waitListChecks == null ? null : waitListChecks.getCreditWarning());
		setMaxCreditOverride(waitListChecks == null ? null : waitListChecks.getMaxCreditNeeded());
		setMaxCreditOverrideStatus(waitListChecks == null ? null : waitListChecks.getMaxCreditOverrideStatus());
	}
	
	public RequestedCourseStatus getStatus(String courseName) {
		RequestedCourseStatus status = null;
		if (hasConfirmations())
			for (CourseMessage m: getConfirmations()) {
				if (m.getStatus() != null && m.hasCourse() && courseName.equals(m.getCourse())) {
					if (status == null || m.getStatus().ordinal() > status.ordinal()) status = m.getStatus();
				}
			}
		if (status != null) return status;
		for (Request r: getCourses())
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse())
					if (courseName.equals(rc.getCourseName()) && !rc.isInactive()) return rc.getStatus();
		for (Request r: getAlternatives())
			if (r.hasRequestedCourse())
				for (RequestedCourse rc: r.getRequestedCourse())
					if (courseName.equals(rc.getCourseName()) && !rc.isInactive()) return rc.getStatus();
		return null;
	}
}
