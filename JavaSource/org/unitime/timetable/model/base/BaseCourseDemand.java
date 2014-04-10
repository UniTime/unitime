/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentEnrollmentMessage;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCourseDemand implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iPriority;
	private Boolean iWaitlist;
	private Boolean iAlternative;
	private Date iTimestamp;
	private String iChangedBy;

	private Student iStudent;
	private FreeTime iFreeTime;
	private Set<CourseRequest> iCourseRequests;
	private Set<StudentEnrollmentMessage> iEnrollmentMessages;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_PRIORITY = "priority";
	public static String PROP_WAITLIST = "waitlist";
	public static String PROP_IS_ALTERNATIVE = "alternative";
	public static String PROP_TIMESTAMP = "timestamp";
	public static String PROP_CHANGED_BY = "changedBy";

	public BaseCourseDemand() {
		initialize();
	}

	public BaseCourseDemand(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getPriority() { return iPriority; }
	public void setPriority(Integer priority) { iPriority = priority; }

	public Boolean isWaitlist() { return iWaitlist; }
	public Boolean getWaitlist() { return iWaitlist; }
	public void setWaitlist(Boolean waitlist) { iWaitlist = waitlist; }

	public Boolean isAlternative() { return iAlternative; }
	public Boolean getAlternative() { return iAlternative; }
	public void setAlternative(Boolean alternative) { iAlternative = alternative; }

	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	public String getChangedBy() { return iChangedBy; }
	public void setChangedBy(String changedBy) { iChangedBy = changedBy; }

	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	public FreeTime getFreeTime() { return iFreeTime; }
	public void setFreeTime(FreeTime freeTime) { iFreeTime = freeTime; }

	public Set<CourseRequest> getCourseRequests() { return iCourseRequests; }
	public void setCourseRequests(Set<CourseRequest> courseRequests) { iCourseRequests = courseRequests; }
	public void addTocourseRequests(CourseRequest courseRequest) {
		if (iCourseRequests == null) iCourseRequests = new HashSet<CourseRequest>();
		iCourseRequests.add(courseRequest);
	}

	public Set<StudentEnrollmentMessage> getEnrollmentMessages() { return iEnrollmentMessages; }
	public void setEnrollmentMessages(Set<StudentEnrollmentMessage> enrollmentMessages) { iEnrollmentMessages = enrollmentMessages; }
	public void addToenrollmentMessages(StudentEnrollmentMessage studentEnrollmentMessage) {
		if (iEnrollmentMessages == null) iEnrollmentMessages = new HashSet<StudentEnrollmentMessage>();
		iEnrollmentMessages.add(studentEnrollmentMessage);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseDemand)) return false;
		if (getUniqueId() == null || ((CourseDemand)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseDemand)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CourseDemand["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseDemand[" +
			"\n	Alternative: " + getAlternative() +
			"\n	ChangedBy: " + getChangedBy() +
			"\n	FreeTime: " + getFreeTime() +
			"\n	Priority: " + getPriority() +
			"\n	Student: " + getStudent() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Waitlist: " + getWaitlist() +
			"]";
	}
}
