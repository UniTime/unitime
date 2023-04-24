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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
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

	public BaseInstructorSurvey() {
	}

	public BaseInstructorSurvey(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "external_uid", nullable = false, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "email", nullable = true, length = 200)
	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	@Column(name = "note", nullable = true, length = 2048)
	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	@Column(name = "submitted", nullable = true)
	public Date getSubmitted() { return iSubmitted; }
	public void setSubmitted(Date submitted) { iSubmitted = submitted; }

	@Column(name = "changed_by", nullable = true, length = 40)
	public String getChangedBy() { return iChangedBy; }
	public void setChangedBy(String changedBy) { iChangedBy = changedBy; }

	@Column(name = "changed_ts", nullable = true)
	public Date getChanged() { return iChanged; }
	public void setChanged(Date changed) { iChanged = changed; }

	@Column(name = "applied_dept", nullable = true, length = 50)
	public String getAppliedDeptCode() { return iAppliedDeptCode; }
	public void setAppliedDeptCode(String appliedDeptCode) { iAppliedDeptCode = appliedDeptCode; }

	@Column(name = "applied_ts", nullable = true)
	public Date getApplied() { return iApplied; }
	public void setApplied(Date applied) { iApplied = applied; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@OneToMany(mappedBy = "instructorSurvey", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<InstructorCourseRequirement> getCourseRequirements() { return iCourseRequirements; }
	public void setCourseRequirements(Set<InstructorCourseRequirement> courseRequirements) { iCourseRequirements = courseRequirements; }
	public void addTocourseRequirements(InstructorCourseRequirement instructorCourseRequirement) {
		if (iCourseRequirements == null) iCourseRequirements = new HashSet<InstructorCourseRequirement>();
		iCourseRequirements.add(instructorCourseRequirement);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructorSurvey)) return false;
		if (getUniqueId() == null || ((InstructorSurvey)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructorSurvey)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
