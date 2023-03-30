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

import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseInstructorSurvey extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iExternalUniqueId;
	private String iEmail;
	private String iNote;
	private Date iSubmitted;
	private String iChangedBy;
	private Date iChanged;
	private String iAppliedDeptCode;
	private Date iApplied;

	private Session iSession;
	private Set<InstructorCourseRequirement> iCourseRequirements;

	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_EMAIL = "email";
	public static String PROP_NOTE = "note";
	public static String PROP_SUBMITTED = "submitted";
	public static String PROP_CHANGED_BY = "changedBy";
	public static String PROP_CHANGED_TS = "changed";
	public static String PROP_APPLIED_DEPT = "appliedDeptCode";
	public static String PROP_APPLIED_TS = "applied";

	public BaseInstructorSurvey() {
		initialize();
	}

	public BaseInstructorSurvey(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	public Date getSubmitted() { return iSubmitted; }
	public void setSubmitted(Date submitted) { iSubmitted = submitted; }

	public String getChangedBy() { return iChangedBy; }
	public void setChangedBy(String changedBy) { iChangedBy = changedBy; }

	public Date getChanged() { return iChanged; }
	public void setChanged(Date changed) { iChanged = changed; }

	public String getAppliedDeptCode() { return iAppliedDeptCode; }
	public void setAppliedDeptCode(String appliedDeptCode) { iAppliedDeptCode = appliedDeptCode; }

	public Date getApplied() { return iApplied; }
	public void setApplied(Date applied) { iApplied = applied; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<InstructorCourseRequirement> getCourseRequirements() { return iCourseRequirements; }
	public void setCourseRequirements(Set<InstructorCourseRequirement> courseRequirements) { iCourseRequirements = courseRequirements; }
	public void addTocourseRequirements(InstructorCourseRequirement instructorCourseRequirement) {
		if (iCourseRequirements == null) iCourseRequirements = new HashSet<InstructorCourseRequirement>();
		iCourseRequirements.add(instructorCourseRequirement);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructorSurvey)) return false;
		if (getUniqueId() == null || ((InstructorSurvey)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructorSurvey)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "InstructorSurvey["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "InstructorSurvey[" +
			"\n	Applied: " + getApplied() +
			"\n	AppliedDeptCode: " + getAppliedDeptCode() +
			"\n	Changed: " + getChanged() +
			"\n	ChangedBy: " + getChangedBy() +
			"\n	Email: " + getEmail() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Note: " + getNote() +
			"\n	Session: " + getSession() +
			"\n	Submitted: " + getSubmitted() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
