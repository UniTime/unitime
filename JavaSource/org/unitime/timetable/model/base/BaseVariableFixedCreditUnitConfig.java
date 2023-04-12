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

import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseVariableFixedCreditUnitConfig extends CourseCreditUnitConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Float iMinUnits;
	private Float iMaxUnits;


	public BaseVariableFixedCreditUnitConfig() {
	}

	public BaseVariableFixedCreditUnitConfig(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "min_units", nullable = true)
	public Float getMinUnits() { return iMinUnits; }
	public void setMinUnits(Float minUnits) { iMinUnits = minUnits; }

	@Column(name = "max_units", nullable = true)
	public Float getMaxUnits() { return iMaxUnits; }
	public void setMaxUnits(Float maxUnits) { iMaxUnits = maxUnits; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof VariableFixedCreditUnitConfig)) return false;
		if (getUniqueId() == null || ((VariableFixedCreditUnitConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((VariableFixedCreditUnitConfig)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "VariableFixedCreditUnitConfig["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "VariableFixedCreditUnitConfig[" +
			"\n	CourseOwner: " + getCourseOwner() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	DefinesCreditAtCourseLevel: " + getDefinesCreditAtCourseLevel() +
			"\n	MaxUnits: " + getMaxUnits() +
			"\n	MinUnits: " + getMinUnits() +
			"\n	SubpartOwner: " + getSubpartOwner() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
