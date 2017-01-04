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

import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitCourseOffering;
import org.unitime.timetable.model.PitStudent;
import org.unitime.timetable.model.PitStudentClassEnrollment;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitStudentClassEnrollment implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iTimestamp;
	private String iChangedBy;

	private PitStudent iPitStudent;
	private PitCourseOffering iPitCourseOffering;
	private PitClass iPitClass;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TIMESTAMP = "timestamp";
	public static String PROP_CHANGED_BY = "changedBy";

	public BasePitStudentClassEnrollment() {
		initialize();
	}

	public BasePitStudentClassEnrollment(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	public String getChangedBy() { return iChangedBy; }
	public void setChangedBy(String changedBy) { iChangedBy = changedBy; }

	public PitStudent getPitStudent() { return iPitStudent; }
	public void setPitStudent(PitStudent pitStudent) { iPitStudent = pitStudent; }

	public PitCourseOffering getPitCourseOffering() { return iPitCourseOffering; }
	public void setPitCourseOffering(PitCourseOffering pitCourseOffering) { iPitCourseOffering = pitCourseOffering; }

	public PitClass getPitClass() { return iPitClass; }
	public void setPitClass(PitClass pitClass) { iPitClass = pitClass; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitStudentClassEnrollment)) return false;
		if (getUniqueId() == null || ((PitStudentClassEnrollment)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitStudentClassEnrollment)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "PitStudentClassEnrollment["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitStudentClassEnrollment[" +
			"\n	ChangedBy: " + getChangedBy() +
			"\n	PitClass: " + getPitClass() +
			"\n	PitCourseOffering: " + getPitCourseOffering() +
			"\n	PitStudent: " + getPitStudent() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
