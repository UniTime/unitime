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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.AdvisorSectioningPref;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Student;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseAdvisorCourseRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iPriority;
	private Boolean iSubstitute;
	private Integer iAlternative;
	private Date iTimestamp;
	private String iChangedBy;
	private String iCredit;
	private String iCourse;
	private String iNotes;
	private Boolean iCritical;

	private Student iStudent;
	private CourseOffering iCourseOffering;
	private FreeTime iFreeTime;
	private Set<AdvisorSectioningPref> iPreferences;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_PRIORITY = "priority";
	public static String PROP_SUBSTITUTE = "substitute";
	public static String PROP_ALTERNATIVE = "alternative";
	public static String PROP_TIME_STAMP = "timestamp";
	public static String PROP_CHANGED_BY = "changedBy";
	public static String PROP_CREDIT = "credit";
	public static String PROP_COURSE = "course";
	public static String PROP_NOTES = "notes";
	public static String PROP_CRITICAL = "critical";

	public BaseAdvisorCourseRequest() {
		initialize();
	}

	public BaseAdvisorCourseRequest(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getPriority() { return iPriority; }
	public void setPriority(Integer priority) { iPriority = priority; }

	public Boolean isSubstitute() { return iSubstitute; }
	public Boolean getSubstitute() { return iSubstitute; }
	public void setSubstitute(Boolean substitute) { iSubstitute = substitute; }

	public Integer getAlternative() { return iAlternative; }
	public void setAlternative(Integer alternative) { iAlternative = alternative; }

	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	public String getChangedBy() { return iChangedBy; }
	public void setChangedBy(String changedBy) { iChangedBy = changedBy; }

	public String getCredit() { return iCredit; }
	public void setCredit(String credit) { iCredit = credit; }

	public String getCourse() { return iCourse; }
	public void setCourse(String course) { iCourse = course; }

	public String getNotes() { return iNotes; }
	public void setNotes(String notes) { iNotes = notes; }

	public Boolean isCritical() { return iCritical; }
	public Boolean getCritical() { return iCritical; }
	public void setCritical(Boolean critical) { iCritical = critical; }

	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	public CourseOffering getCourseOffering() { return iCourseOffering; }
	public void setCourseOffering(CourseOffering courseOffering) { iCourseOffering = courseOffering; }

	public FreeTime getFreeTime() { return iFreeTime; }
	public void setFreeTime(FreeTime freeTime) { iFreeTime = freeTime; }

	public Set<AdvisorSectioningPref> getPreferences() { return iPreferences; }
	public void setPreferences(Set<AdvisorSectioningPref> preferences) { iPreferences = preferences; }
	public void addTopreferences(AdvisorSectioningPref advisorSectioningPref) {
		if (iPreferences == null) iPreferences = new HashSet<AdvisorSectioningPref>();
		iPreferences.add(advisorSectioningPref);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof AdvisorCourseRequest)) return false;
		if (getUniqueId() == null || ((AdvisorCourseRequest)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((AdvisorCourseRequest)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "AdvisorCourseRequest["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "AdvisorCourseRequest[" +
			"\n	Alternative: " + getAlternative() +
			"\n	ChangedBy: " + getChangedBy() +
			"\n	Course: " + getCourse() +
			"\n	CourseOffering: " + getCourseOffering() +
			"\n	Credit: " + getCredit() +
			"\n	Critical: " + getCritical() +
			"\n	FreeTime: " + getFreeTime() +
			"\n	Notes: " + getNotes() +
			"\n	Priority: " + getPriority() +
			"\n	Student: " + getStudent() +
			"\n	Substitute: " + getSubstitute() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
