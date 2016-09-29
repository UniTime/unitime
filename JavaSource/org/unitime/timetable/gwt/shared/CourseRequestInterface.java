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

import org.unitime.timetable.gwt.resources.StudentSectioningMessages;

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
	private String iLastCourse = null;
	
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
	
	public boolean addCourse(String course) {
		iLastCourse = course;
		if (getRequestPriority(course) != null) return false;
		for (CourseRequestInterface.Request r: getCourses()) {
			if (!r.hasRequestedFreeTime() && !r.hasRequestedCourse()) {
				r.setRequestedCourse(course);
				return true;
			}
		}
		CourseRequestInterface.Request r = new CourseRequestInterface.Request();
		r.setRequestedCourse(course);
		getCourses().add(r);
		return true;
	}
	
	public boolean hasLastCourse() { return iLastCourse != null; }
	
	public String getLastCourse() { return iLastCourse; }
	
	public RequestPriority getRequestPriority(String course) {
		if (course == null || course.isEmpty()) return null;
		int priority = 1;
		for (CourseRequestInterface.Request r: getCourses()) {
			if (course.equalsIgnoreCase(r.getRequestedCourse()))
				return new RequestPriority(false, priority, 0, r);
			if (course.equalsIgnoreCase(r.getFirstAlternative()))
				return new RequestPriority(false, priority, 1, r);
			if (course.equalsIgnoreCase(r.getSecondAlternative()))
				return new RequestPriority(false, priority, 2, r);
			priority ++;
		}
		priority = 1;
		for (CourseRequestInterface.Request r: getAlternatives()) {
			if (course.equalsIgnoreCase(r.getRequestedCourse()))
				return new RequestPriority(true, priority, 0, r);
			if (course.equalsIgnoreCase(r.getFirstAlternative()))
				return new RequestPriority(true, priority, 1, r);
			if (course.equalsIgnoreCase(r.getSecondAlternative()))
				return new RequestPriority(true, priority, 2, r);
			priority ++;
		}
		return null;
	}

	public static class FreeTime implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private ArrayList<Integer> iDays = new ArrayList<Integer>();
		private int iStart;
		private int iLength;
		public FreeTime() {}
		
		public void addDay(int day) { iDays.add(day); }
		public ArrayList<Integer> getDays() { return iDays; }
		public String getDaysString(String[] shortDays, String separator) {
			if (iDays == null) return "";
			String ret = "";
			for (int day: iDays)
				ret += (ret.isEmpty() ? "" : separator) + shortDays[day];
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
	}
	
	public static class RequestedCourse {
		private Long iCourseId;
		private String iCourseName;
		
		public RequestedCourse() {}
		
		public Long getCourseId() { return iCourseId; }
		public boolean hasCourseId() { return iCourseId != null; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		
		public String getCourseName() { return iCourseName; }
		public void setCourseName(String courseName) { iCourseName = courseName; }
		
		@Override
		public int hashCode() {
			return (hasCourseId() ? getCourseId().hashCode() : getCourseName().hashCode());
		}
		
		@Override
		public String toString() {
			return getCourseName();
		}
	}
	
	public static class Request implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private ArrayList<FreeTime> iRequestedFreeTime = null;
		private String iRequestedCourse = null;
		private String iFirstAlternative = null;
		private String iSecondAlternative = null;
		private Boolean iWaitList = false;
		private Integer iReadOnly = null;
		
		public Request() {}
		
		public String getRequestedCourse() { return iRequestedCourse; }
		public void setRequestedCourse(String requestedCourse) { iRequestedCourse = requestedCourse; }
		public boolean hasRequestedCourse() { return iRequestedCourse != null && !iRequestedCourse.isEmpty(); }

		public ArrayList<FreeTime> getRequestedFreeTime() { return iRequestedFreeTime; }
		public void addRequestedFreeTime(FreeTime ft) { 
			if (iRequestedFreeTime == null)
				iRequestedFreeTime = new ArrayList<FreeTime>();
			iRequestedFreeTime.add(ft);
		}
		public boolean hasRequestedFreeTime() { return iRequestedFreeTime != null && !iRequestedFreeTime.isEmpty(); }
		
		public String getFirstAlternative() { return iFirstAlternative; }
		public void setFirstAlternative(String firstAlternative) { iFirstAlternative = firstAlternative; }
		public boolean hasFirstAlternative() { return iFirstAlternative != null && !iFirstAlternative.isEmpty(); }
		
		public String getSecondAlternative() { return iSecondAlternative; }
		public void setSecondAlternative(String secondAlternative) { iSecondAlternative = secondAlternative; }
		public boolean hasSecondAlternative() { return iSecondAlternative != null && !iSecondAlternative.isEmpty(); }
		
		public boolean hasWaitList() { return iWaitList != null; }
		public boolean isWaitList() { return iWaitList != null && iWaitList.booleanValue(); }
		public void setWaitList(Boolean waitList) { iWaitList = waitList; }
		
		public boolean isReadOnly() { return iReadOnly != null && iReadOnly.intValue() >= 0; }
		public boolean isRequestedCourseReadOnly() { return iReadOnly != null && iReadOnly == 0; }
		public boolean isFirstAlternativeReadOnly() { return iReadOnly != null && iReadOnly == 1; }
		public boolean isSecondAlternativeReadOnly() { return iReadOnly != null && iReadOnly == 2; }
		public void setReadOnly(Integer readOnly) { iReadOnly = readOnly; }
		
		public String toString() {
			return (hasRequestedFreeTime() ? iRequestedFreeTime.toString() : hasRequestedCourse() ? iRequestedCourse : "-") +
				(hasFirstAlternative() ? ", " + iFirstAlternative : "") +
				(hasSecondAlternative() ? ", " + iSecondAlternative : "") +
				(isWaitList() ? " (w)" : "");
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
