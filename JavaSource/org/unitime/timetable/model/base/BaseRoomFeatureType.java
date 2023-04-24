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

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.RoomFeatureType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseRoomFeatureType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Boolean iShowInEventManagement;
	private Boolean iShowInInstructorSurvey;


	public BaseRoomFeatureType() {
	}

	public BaseRoomFeatureType(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "events", nullable = false)
	public Boolean isShowInEventManagement() { return iShowInEventManagement; }
	@Transient
	public Boolean getShowInEventManagement() { return iShowInEventManagement; }
	public void setShowInEventManagement(Boolean showInEventManagement) { iShowInEventManagement = showInEventManagement; }

	@Column(name = "survey", nullable = false)
	public Boolean isShowInInstructorSurvey() { return iShowInInstructorSurvey; }
	@Transient
	public Boolean getShowInInstructorSurvey() { return iShowInInstructorSurvey; }
	public void setShowInInstructorSurvey(Boolean showInInstructorSurvey) { iShowInInstructorSurvey = showInInstructorSurvey; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomFeatureType)) return false;
		if (getUniqueId() == null || ((RoomFeatureType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomFeatureType)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "RoomFeatureType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "RoomFeatureType[" +
			"\n	Label: " + getLabel() +
			"\n	Reference: " + getReference() +
			"\n	ShowInEventManagement: " + getShowInEventManagement() +
			"\n	ShowInInstructorSurvey: " + getShowInInstructorSurvey() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
