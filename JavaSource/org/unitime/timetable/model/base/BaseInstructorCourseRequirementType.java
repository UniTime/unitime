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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.unitime.timetable.model.InstructorCourseRequirementType;
import org.unitime.timetable.model.RefTableEntry;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseInstructorCourseRequirementType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iLength;
	private Integer iSortOrder;


	public BaseInstructorCourseRequirementType() {
	}

	public BaseInstructorCourseRequirementType(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "length", nullable = false)
	public Integer getLength() { return iLength; }
	public void setLength(Integer length) { iLength = length; }

	@Column(name = "sort_order", nullable = false)
	public Integer getSortOrder() { return iSortOrder; }
	public void setSortOrder(Integer sortOrder) { iSortOrder = sortOrder; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructorCourseRequirementType)) return false;
		if (getUniqueId() == null || ((InstructorCourseRequirementType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructorCourseRequirementType)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "InstructorCourseRequirementType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "InstructorCourseRequirementType[" +
			"\n	Label: " + getLabel() +
			"\n	Length: " + getLength() +
			"\n	Reference: " + getReference() +
			"\n	SortOrder: " + getSortOrder() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
