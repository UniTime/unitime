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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeCourseInterface;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class CourseRequestInterface implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;
	private Long iSessionId, iStudentId;
	private ArrayList<Request> iCourses = new ArrayList<Request>();
	private ArrayList<Request> iAlternatives = new ArrayList<Request>();
	private boolean iSaved = false;
	private boolean iNoChange = false;
	private boolean iAllowTimeConf = false, iAllowRoomConf = false;
	private Boolean iUpdateLastRequest = null;
	private RequestedCourse iLastCourse = null;
	private List<CourseMessage> iConfirmations = null;
	private Float iMaxCredit = null;
	private Float iMaxCreditOverride = null;
	private RequestedCourseStatus iMaxCreditOverrideStatus = null;
	private String iMaxCreditOverrideExternalId = null;
	private Date iMaxCreditOverrideTimeStamp = null;
	
	public CourseRequestInterface() {}

	public Long getAcademicSessionId() { return iSessionId; }
	public void setAcademicSessionId(Long sessionId) { iSessionId = sessionId; }
	
	public Long getStudentId() { return iStudentId; }
	public void setStudentId(Long studentId) { iStudentId = studentId; }
	
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
	
	public boolean isSaved() { return iSaved; }
	public void setSaved(boolean saved) { iSaved = saved; }
	
	public boolean isNoChange() { return iNoChange; }
	public void setNoChange(boolean noChange) { iNoChange = noChange; }
	
	public boolean areTimeConflictsAllowed() { return iAllowTimeConf; }
	public void setTimeConflictsAllowed(boolean allow) { iAllowTimeConf = allow; }
	public boolean areSpaceConflictsAllowed() { return iAllowRoomConf; }
	public void setSpaceConflictsAllowed(boolean allow) { iAllowRoomConf = allow; }
	
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
	
	public boolean addCourse(RequestedCourse course) {
		iLastCourse = course;
		if (getRequestPriority(course) != null) return false;
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
	
	public float[] getCreditRange() {
		List<Float> mins = new ArrayList<Float>();
		List<Float> maxs = new ArrayList<Float>();
		int nrCourses = 0;
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
					mins.add(min); maxs.add(max); nrCourses ++;
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
		float min = 0f, max = 0f;
		for (int i = 0; i < nrCourses; i++) {
			min += mins.get(i);
			max += maxs.get(maxs.size() - i - 1);
		}
		return new float[] {min, max};
	}
	
	public float getCredit() {
		List<Float> credits = new ArrayList<Float>();
		int nrCourses = 0;
		for (Request r: getCourses()) {
			if (r.hasRequestedCourse()) {
				Float credit = null;
				for (RequestedCourse rc: r.getRequestedCourse()) {
					if (rc.hasCredit()) {
						if (credit == null || credit < rc.getCreditMin()) credit = rc.getCreditMin();
					}
				}
				if (credit != null) {
					credits.add(credit); nrCourses ++;
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
		float total = 0f;
		for (int i = 0; i < nrCourses; i++) {
			total += credits.get(credits.size() - i - 1);
		}
		return total;
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
				ret += (1 << day);
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
	}
	
	public static class RequestedCourse implements IsSerializable, Serializable, Comparable<RequestedCourse> {
		private static final long serialVersionUID = 1L;
		private Long iCourseId;
		private String iCourseName;
		private String iCourseTitle;
		private Boolean iReadOnly = null;
		private Boolean iCanDelete = null;
		private List<FreeTime> iFreeTime;
		private Set<String> iSelectedIntructionalMethods;
		private Set<String> iSelectedClasses;
		private float[] iCredit = null;
		private RequestedCourseStatus iStatus = null;
		private String iOverrideExternalId = null;
		private Date iOverrideTimeStamp = null;
		
		public RequestedCourse() {}
		public RequestedCourse(List<FreeTime> freeTime) {
			iFreeTime = freeTime;
		}
		public RequestedCourse(CourseAssignment course, boolean showTitle) {
			iCourseId = course.getCourseId();
			iCourseName = (course.hasUniqueName() && !showTitle ? course.getCourseName() : course.getCourseNameWithTitle()); 
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
		
		public boolean isEmpty() { return !isCourse() && !isFreeTime(); }
		
		public boolean hasSelectedIntructionalMethods() { return iSelectedIntructionalMethods != null && !iSelectedIntructionalMethods.isEmpty(); }
		public Set<String> getSelectedIntructionalMethods() { return iSelectedIntructionalMethods; }
		public void setSelectedIntructionalMethod(String id, boolean value) {
			if (iSelectedIntructionalMethods == null) iSelectedIntructionalMethods = new HashSet<String>();
			if (value)
				iSelectedIntructionalMethods.add(id);
			else
				iSelectedIntructionalMethods.remove(id);
		}
		public boolean isSelectedIntructionalMethod(String id) {
			if (iSelectedIntructionalMethods == null) return false;
			return iSelectedIntructionalMethods.contains(id);
		}
		public int getNrSelectedIntructionalMethods() {
			return (iSelectedIntructionalMethods == null ? 0 : iSelectedIntructionalMethods.size());
		}
		public boolean sameSelectedIntructionalMethods(RequestedCourse rc) {
			if (getNrSelectedIntructionalMethods() != rc.getNrSelectedIntructionalMethods()) return false;
			if (hasSelectedIntructionalMethods()) {
				for (String id: getSelectedIntructionalMethods())
					if (!rc.isSelectedIntructionalMethod(id)) return false;
			}
			return true;
		}
		
		public boolean hasSelectedClasses() { return iSelectedClasses != null && !iSelectedClasses.isEmpty(); }
		public Set<String> getSelectedClasses() { return iSelectedClasses; }
		public void setSelectedClasses(Set<String> classes) { 
			if (iSelectedClasses == null)
				iSelectedClasses = new HashSet<String>();
			else
				iSelectedClasses.clear();
			if (classes != null)
				iSelectedClasses.addAll(classes);
		}
		public void setSelectedClass(String id, boolean value) {
			if (iSelectedClasses == null) iSelectedClasses = new HashSet<String>();
			if (value)
				iSelectedClasses.add(id);
			else
				iSelectedClasses.remove(id);
		}
		public boolean isSelectedClass(String id) {
			if (iSelectedClasses == null || id == null) return false;
			return iSelectedClasses.contains(id);
		}
		public int getNrSelectedClasses() {
			return (iSelectedClasses == null ? 0 : iSelectedClasses.size());
		}
		public boolean sameSelectedClasses(RequestedCourse rc) {
			if (getNrSelectedClasses() != rc.getNrSelectedClasses()) return false;
			if (hasSelectedClasses()) {
				for (String id: getSelectedClasses())
					if (!rc.isSelectedClass(id)) return false;
			}
			return true;
		}
		
		@Override
		public int hashCode() {
			return (hasCourseId() ? getCourseId().hashCode() : getCourseName().hashCode());
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
	
	public static class Request implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private List<RequestedCourse> iRequestedCourse = null;
		private Boolean iWaitList = false;
		private Date iTimeStamp = null;
		
		public Request() {}
		
		public List<RequestedCourse> getRequestedCourse() { return iRequestedCourse; }
		public RequestedCourse getRequestedCourse(int index) {
			if (iRequestedCourse != null && index < iRequestedCourse.size())
				return iRequestedCourse.get(index);
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
		
		public boolean hasWaitList() { return iWaitList != null; }
		public boolean isWaitList() { return iWaitList != null && iWaitList.booleanValue(); }
		public void setWaitList(Boolean waitList) { iWaitList = waitList; }
		
		public boolean hasTimeStamp() { return iTimeStamp != null; }
		public Date getTimeStamp() { return iTimeStamp; }
		public void setTimeStamp(Date ts) { iTimeStamp = ts; }
		
		public String toString() {
			return (hasRequestedCourse() ? iRequestedCourse.toString() : "-") + (isWaitList() ? " (w)" : "");
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Request)) return false;
			Request r = (Request)o;
			if (isWaitList() != r.isWaitList() || getRequestedCourse().size() != r.getRequestedCourse().size()) return false;
			for (int i = 0; i < getRequestedCourse().size(); i++) {
				RequestedCourse c1 = getRequestedCourse(i);
				RequestedCourse c2 = r.getRequestedCourse(i);
				if (!c1.equals(c2) || !c1.sameSelectedClasses(c2) || !c1.sameSelectedIntructionalMethods(c2)) return false;
			}
			return true;
		}
	}
	
	public String toString() {
		String ret = "CourseRequests(student = " + iStudentId + ", session = " + iSessionId + ", requests = {";
		int idx = 1;
		for (Request r: iCourses)
			ret += "\n   " + (idx++) + ". " + r;
		idx = 1;
		for (Request r: iAlternatives)
			ret += "\n  A" + (idx++) + ". " + r;
		return ret + "\n})";
		
	}
	
	public static class RequestPriority implements IsSerializable, Serializable {
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
				default: return MESSAGES.degreeRequestedAlternativeSecondAlt(iPriority);
				}
			} else {
				switch (iChoice) {
				case 0: return MESSAGES.degreeRequestedCourse(iPriority);
				case 1: return MESSAGES.degreeRequestedCourseFirstAlt(iPriority);
				default: return MESSAGES.degreeRequestedCourseSecondAlt(iPriority);
				}
			}
			
		}
	}
	
	public static class CheckCoursesResponse implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Set<CourseMessage> iMessages = new TreeSet<CourseMessage>();
		
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
		
		public List<CourseMessage> getMessages(String courseName) {
			List<CourseMessage> ret = new ArrayList<CourseMessage>();
			if (hasMessages())
				for (CourseMessage m: getMessages())
					if (m.hasCourse() && courseName.equals(m.getCourse())) ret.add(m);
			return ret;
		}
		
		public String getMessage(String courseName, String delim) {
			if (!hasMessages()) return null;
			String ret = null;
			if (hasMessages())
				for (CourseMessage m: getMessages())
					if (m.hasCourse() && courseName.equals(m.getCourse())) {
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
		
		public String getMessageWithColor(String courseName, String delim) {
			if (!hasMessages()) return null;
			String ret = null;
			if (hasMessages())
				for (CourseMessage m: getMessages())
					if (m.hasCourse() && courseName.equals(m.getCourse())) {
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
	
	public void addConfirmationError(Long courseId, String course, String code, String message, RequestedCourseStatus status) {
		if (iConfirmations == null) iConfirmations = new ArrayList<CourseMessage>();
		CourseMessage m = new CourseMessage();
		m.setCourseId(courseId);
		m.setCourse(course);
		m.setCode(code);
		m.setMessage(message);
		m.setError(true);
		m.setConfirm(null);
		m.setStatus(status);
		iConfirmations.add(m);
	}
	
	public void addConfirmationMessage(Long courseId, String course, String code, String message, RequestedCourseStatus status) {
		if (iConfirmations == null) iConfirmations = new ArrayList<CourseMessage>();
		CourseMessage m = new CourseMessage();
		m.setCourseId(courseId);
		m.setCourse(course);
		m.setCode(code);
		m.setMessage(message);
		m.setError(false);
		m.setConfirm(null);
		m.setStatus(status);
		iConfirmations.add(m);
	}
	
	public void addConfirmationMessage(Long courseId, String course, String code, String message) {
		addConfirmationMessage(courseId, course, code, message, null);
	}
}
