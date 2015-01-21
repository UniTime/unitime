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

import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseVariableRangeCreditUnitConfig extends VariableFixedCreditUnitConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Boolean iFractionalIncrementsAllowed;


	public static String PROP_FRACTIONAL_INCR_ALLOWED = "fractionalIncrementsAllowed";

	public BaseVariableRangeCreditUnitConfig() {
		initialize();
	}

	public BaseVariableRangeCreditUnitConfig(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Boolean isFractionalIncrementsAllowed() { return iFractionalIncrementsAllowed; }
	public Boolean getFractionalIncrementsAllowed() { return iFractionalIncrementsAllowed; }
	public void setFractionalIncrementsAllowed(Boolean fractionalIncrementsAllowed) { iFractionalIncrementsAllowed = fractionalIncrementsAllowed; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof VariableRangeCreditUnitConfig)) return false;
		if (getUniqueId() == null || ((VariableRangeCreditUnitConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((VariableRangeCreditUnitConfig)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "VariableRangeCreditUnitConfig["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "VariableRangeCreditUnitConfig[" +
			"\n	CourseCreditFormat: " + getCourseCreditFormat() +
			"\n	CourseOwner: " + getCourseOwner() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	DefinesCreditAtCourseLevel: " + getDefinesCreditAtCourseLevel() +
			"\n	FractionalIncrementsAllowed: " + getFractionalIncrementsAllowed() +
			"\n	MaxUnits: " + getMaxUnits() +
			"\n	MinUnits: " + getMinUnits() +
			"\n	SubpartOwner: " + getSubpartOwner() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
