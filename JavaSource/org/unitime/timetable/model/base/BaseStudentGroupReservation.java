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

import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseStudentGroupReservation extends Reservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private StudentGroup iGroup;


	public BaseStudentGroupReservation() {
		initialize();
	}

	public BaseStudentGroupReservation(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public StudentGroup getGroup() { return iGroup; }
	public void setGroup(StudentGroup group) { iGroup = group; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentGroupReservation)) return false;
		if (getUniqueId() == null || ((StudentGroupReservation)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentGroupReservation)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "StudentGroupReservation["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentGroupReservation[" +
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	Group: " + getGroup() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
