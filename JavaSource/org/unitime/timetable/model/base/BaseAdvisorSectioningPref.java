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

import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.AdvisorSectioningPref;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseAdvisorSectioningPref implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iLabel;
	private Boolean iRequired;

	private AdvisorCourseRequest iCourseRequest;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_LABEL = "label";
	public static String PROP_REQUIRED = "required";

	public BaseAdvisorSectioningPref() {
		initialize();
	}

	public BaseAdvisorSectioningPref(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getLabel() { return iLabel; }
	public void setLabel(String label) { iLabel = label; }

	public Boolean isRequired() { return iRequired; }
	public Boolean getRequired() { return iRequired; }
	public void setRequired(Boolean required) { iRequired = required; }

	public AdvisorCourseRequest getCourseRequest() { return iCourseRequest; }
	public void setCourseRequest(AdvisorCourseRequest courseRequest) { iCourseRequest = courseRequest; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof AdvisorSectioningPref)) return false;
		if (getUniqueId() == null || ((AdvisorSectioningPref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((AdvisorSectioningPref)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
