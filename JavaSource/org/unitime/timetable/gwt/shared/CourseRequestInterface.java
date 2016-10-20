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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private Boolean iUpdateLastRequest = null;
	private RequestedCourse iLastCourse = null;
	
	public CourseRequestInterface() {}

	public Long getAcademicSessionId() { return iSessionId; }
	public void setAcademicSessionId(Long sessionId) { iSessionId = sessionId; }
	
	public Long getStudentId() { return iStudentId; }
	public void setStudentId(Long studentId) { iStudentId = studentId; }
	
	public ArrayList<Request> getCourses() { return iCourses; }
	public ArrayList<Request> getAlternatives() { return iAlternatives; }
	
	public boolean isSaved() { return iSaved; }
	public void setSaved(boolean saved) { iSaved = saved; }
	
	public boolean isNoChange() { return iNoChange; }
	public void setNoChange(boolean noChange) { iNoChange = noChange; }
	
	public boolean isUpdateLastRequest() { return iUpdateLastRequest == null || iUpdateLastRequest.booleanValue(); }
	public void setUpdateLastRequest(boolean updateLastRequest) { iUpdateLastRequest = updateLastRequest; }
	
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
	
	public static class RequestedCourse implements IsSerializable, Serializable, Comparable<RequestedCourse> {
		private static final long serialVersionUID = 1L;
		private Long iCourseId;
		private String iCourseName;
		private Boolean iReadOnly = null;
		private List<FreeTime> iFreeTime;
		private Set<String> iSelectedIntructionalMethods;
		private Set<String> iSelectedClasses;
		
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
		
		public List<FreeTime> getFreeTime() { return iFreeTime; }
		public boolean isFreeTime() { return iFreeTime != null && !iFreeTime.isEmpty(); }
		public void setFreeTime(List<FreeTime> freeTime) { iFreeTime = freeTime; }
		public void addFreeTime(FreeTime freeTime) {
			if (iFreeTime == null) iFreeTime = new ArrayList<FreeTime>();
			iFreeTime.add(freeTime);
		}
		
		public boolean isReadOnly() { return iReadOnly != null && iReadOnly.booleanValue(); }
		public void setReadOnly(Boolean readOnly) { iReadOnly = readOnly; }
		
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
			if (iSelectedClasses == null) return false;
			return iSelectedClasses.contains(id);
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
						(hasSelectedClasses() ? " section:" + getSelectedClasses() : "");
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
	
	public static class Request implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private List<RequestedCourse> iRequestedCourse = null;
		private Boolean iWaitList = false;
		
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
		
		public boolean hasWaitList() { return iWaitList != null; }
		public boolean isWaitList() { return iWaitList != null && iWaitList.booleanValue(); }
		public void setWaitList(Boolean waitList) { iWaitList = waitList; }
		
		public String toString() {
			return (hasRequestedCourse() ? iRequestedCourse.toString() : "-") + (isWaitList() ? " (w)" : "");
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
	
	public static class RequestPriority implements IsSerializable {
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
}
