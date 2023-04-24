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

import org.unitime.timetable.model.CourseHistory;
import org.unitime.timetable.model.History;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseCourseHistory extends History implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iOldNumber;
	private String iNewNumber;


	public BaseCourseHistory() {
	}

	public BaseCourseHistory(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "old_number", nullable = true, length = 4)
	public String getOldNumber() { return iOldNumber; }
	public void setOldNumber(String oldNumber) { iOldNumber = oldNumber; }

	@Column(name = "new_number", nullable = true, length = 4)
	public String getNewNumber() { return iNewNumber; }
	public void setNewNumber(String newNumber) { iNewNumber = newNumber; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseHistory)) return false;
		if (getUniqueId() == null || ((CourseHistory)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseHistory)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "CourseHistory["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseHistory[" +
			"\n	NewNumber: " + getNewNumber() +
			"\n	NewValue: " + getNewValue() +
			"\n	OldNumber: " + getOldNumber() +
			"\n	OldValue: " + getOldValue() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
