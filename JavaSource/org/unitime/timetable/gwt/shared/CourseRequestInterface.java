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

public class CourseRequestInterface implements IsSerializable {
	private Long iSessionId, iStudentId;
	private ArrayList<Request> iCourses = new ArrayList<Request>();
	private ArrayList<Request> iAlternatives = new ArrayList<Request>();
	
	public CourseRequestInterface() {}

	public Long getAcademicSessionId() { return iSessionId; }
	public void setAcademicSessionId(Long sessionId) { iSessionId = sessionId; }
	
	public Long getStudentId() { return iStudentId; }
	public void setStudentId(Long studentId) { iStudentId = studentId; }
	
	public ArrayList<Request> getCourses() { return iCourses; }
	public ArrayList<Request> getAlternatives() { return iAlternatives; }

	public static class FreeTime implements IsSerializable {
		private ArrayList<Integer> iDays = new ArrayList<Integer>();
		private int iStart;
		private int iLength;
		public FreeTime() {}
		
		public void addDay(int day) { iDays.add(day); }
		public ArrayList<Integer> getDays() { return iDays; }
		public String getDaysString(String[] shortDays) {
			if (iDays == null) return "";
			String ret = "";
			for (int day: iDays)
				ret += shortDays[day];
			return ret;
		}
		
		public int getStart() { return iStart; }
		public void setStart(int startSlot) { iStart = startSlot; }
		public String getStartString() {
	        int h = iStart / 12;
	        int m = 5 * (iStart % 12);
	        return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + ( h == 24 ? "a" : h >= 12 ? "p" : "a");
		}

		
		public int getLength() { return iLength; }
		public void setLength(int length) { iLength = length; }
		public String getEndString() {
			int h = (iStart + iLength) / 12;
			int m = 5 * ((iStart + iLength) % 12);
	        return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + ( h == 24 ? "a" : h >= 12 ? "p" : "a");
		}
		
		public String toString(String[] shortDays) {
			return getDaysString(shortDays) + " " + getStartString() + " - " + getEndString();
		}
		
		public String toString() {
			return "Free " + toString(new String[] {"M", "T", "W", "R", "F", "S", "U"});
		}
	}
	
	public static class Request implements IsSerializable {
		private ArrayList<FreeTime> iRequestedFreeTime = null;
		private String iRequestedCourse = null;
		private String iFirstAlternative = null;
		private String iSecondAlternative = null;;
		private boolean iAlternative = false;
		
		public Request() {}
		
		public boolean isAlternative() { return iAlternative; }
		public void setAlternative(boolean alternative) { iAlternative = alternative; }
		
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
		
		public String toString() {
			return (hasRequestedFreeTime() ? iRequestedFreeTime.toString() : hasRequestedCourse() ? iRequestedCourse : "-") +
				(hasFirstAlternative() ? ", " + iFirstAlternative : "") +
				(hasSecondAlternative() ? ", " + iSecondAlternative : "");
		}
	}
	
	public String toString() {
		String ret = "CourseRequests(student = " + iSessionId + ", session = " + iSessionId + ", requests = {";
		int idx = 1;
		for (Request r: iCourses)
			ret += "\n   " + (idx++) + ". " + r;
		idx = 1;
		for (Request r: iAlternatives)
			ret += "\n  A" + (idx++) + ". " + r;
		return ret + "\n})";
		
	}
}
