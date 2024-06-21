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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.AdvisorSectioningPref;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseAdvisorSectioningPref implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iLabel;
	private Boolean iRequired;

	private AdvisorCourseRequest iCourseRequest;

	public BaseAdvisorSectioningPref() {
	}

	public BaseAdvisorSectioningPref(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "pref_group_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "label", nullable = true, length = 60)
	public String getLabel() { return iLabel; }
	public void setLabel(String label) { iLabel = label; }

	@Column(name = "required", nullable = false)
	public Boolean isRequired() { return iRequired; }
	@Transient
	public Boolean getRequired() { return iRequired; }
	public void setRequired(Boolean required) { iRequired = required; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "request_id", nullable = false)
	public AdvisorCourseRequest getCourseRequest() { return iCourseRequest; }
	public void setCourseRequest(AdvisorCourseRequest courseRequest) { iCourseRequest = courseRequest; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof AdvisorSectioningPref)) return false;
		if (getUniqueId() == null || ((AdvisorSectioningPref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((AdvisorSectioningPref)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "AdvisorSectioningPref["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "AdvisorSectioningPref[" +
			"\n	CourseRequest: " + getCourseRequest() +
			"\n	Label: " + getLabel() +
			"\n	Required: " + getRequired() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
