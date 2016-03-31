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

import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.RefTableEntry;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseInstructorAttributeType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Boolean iConjunctive;
	private Boolean iRequired;


	public static String PROP_CONJUNCTIVE = "conjunctive";
	public static String PROP_REQUIRED = "required";

	public BaseInstructorAttributeType() {
		initialize();
	}

	public BaseInstructorAttributeType(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Boolean isConjunctive() { return iConjunctive; }
	public Boolean getConjunctive() { return iConjunctive; }
	public void setConjunctive(Boolean conjunctive) { iConjunctive = conjunctive; }

	public Boolean isRequired() { return iRequired; }
	public Boolean getRequired() { return iRequired; }
	public void setRequired(Boolean required) { iRequired = required; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructorAttributeType)) return false;
		if (getUniqueId() == null || ((InstructorAttributeType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructorAttributeType)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "InstructorAttributeType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "InstructorAttributeType[" +
			"\n	Conjunctive: " + getConjunctive() +
			"\n	Label: " + getLabel() +
			"\n	Reference: " + getReference() +
			"\n	Required: " + getRequired() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
