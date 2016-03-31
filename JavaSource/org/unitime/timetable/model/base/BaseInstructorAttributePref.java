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

import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.Preference;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseInstructorAttributePref extends Preference implements Serializable {
	private static final long serialVersionUID = 1L;

	private InstructorAttribute iAttribute;


	public BaseInstructorAttributePref() {
		initialize();
	}

	public BaseInstructorAttributePref(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public InstructorAttribute getAttribute() { return iAttribute; }
	public void setAttribute(InstructorAttribute attribute) { iAttribute = attribute; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructorAttributePref)) return false;
		if (getUniqueId() == null || ((InstructorAttributePref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructorAttributePref)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "InstructorAttributePref["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "InstructorAttributePref[" +
			"\n	Attribute: " + getAttribute() +
			"\n	Owner: " + getOwner() +
			"\n	PrefLevel: " + getPrefLevel() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
