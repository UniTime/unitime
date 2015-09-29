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
package org.unitime.timetable.model;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseIndividualReservation;



/**
 * @author Tomas Muller
 */
public class IndividualReservation extends BaseIndividualReservation {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public IndividualReservation () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public IndividualReservation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	@Override
	public boolean isApplicable(Student student, CourseRequest request) {
		return getStudents().contains(student);
	}

	@Override
	public int getPriority() {
		return ApplicationProperty.ReservationPriorityIndividual.intValue();
	}

	@Override
	public boolean isCanAssignOverLimit() {
		return ApplicationProperty.ReservationCanOverLimitIndividual.isTrue();
	}

	@Override
	public boolean isMustBeUsed() {
		return ApplicationProperty.ReservationMustBeUsedIndividual.isTrue();
	}

	@Override
	public boolean isAllowOverlap() {
		return ApplicationProperty.ReservationAllowOverlapIndividual.isTrue();
	}

	@Override
	public int getReservationLimit() {
		return getStudents().size();
	}
}
