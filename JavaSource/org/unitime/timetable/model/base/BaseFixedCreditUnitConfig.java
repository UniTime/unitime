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

import java.io.Serializable;

import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.FixedCreditUnitConfig;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseFixedCreditUnitConfig extends CourseCreditUnitConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Float iFixedUnits;


	public BaseFixedCreditUnitConfig() {
	}

	public BaseFixedCreditUnitConfig(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "fixed_units", nullable = true)
	public Float getFixedUnits() { return iFixedUnits; }
	public void setFixedUnits(Float fixedUnits) { iFixedUnits = fixedUnits; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof FixedCreditUnitConfig)) return false;
		if (getUniqueId() == null || ((FixedCreditUnitConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((FixedCreditUnitConfig)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "FixedCreditUnitConfig["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "FixedCreditUnitConfig[" +
			"\n	CourseOwner: " + getCourseOwner() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	DefinesCreditAtCourseLevel: " + getDefinesCreditAtCourseLevel() +
			"\n	FixedUnits: " + getFixedUnits() +
			"\n	SubpartOwner: " + getSubpartOwner() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
