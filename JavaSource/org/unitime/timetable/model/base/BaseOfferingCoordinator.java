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

import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.TeachingResponsibility;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseOfferingCoordinator implements Serializable {
	private static final long serialVersionUID = 1L;

	private DepartmentalInstructor iInstructor;
	private InstructionalOffering iOffering;

	private TeachingResponsibility iResponsibility;
	private TeachingRequest iTeachingRequest;


	public BaseOfferingCoordinator() {
		initialize();
	}

	protected void initialize() {}

	public DepartmentalInstructor getInstructor() { return iInstructor; }
	public void setInstructor(DepartmentalInstructor instructor) { iInstructor = instructor; }

	public InstructionalOffering getOffering() { return iOffering; }
	public void setOffering(InstructionalOffering offering) { iOffering = offering; }

	public TeachingResponsibility getResponsibility() { return iResponsibility; }
	public void setResponsibility(TeachingResponsibility responsibility) { iResponsibility = responsibility; }

	public TeachingRequest getTeachingRequest() { return iTeachingRequest; }
	public void setTeachingRequest(TeachingRequest teachingRequest) { iTeachingRequest = teachingRequest; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof OfferingCoordinator)) return false;
		OfferingCoordinator offeringCoordinator = (OfferingCoordinator)o;
		if (getInstructor() == null || offeringCoordinator.getInstructor() == null || !getInstructor().equals(offeringCoordinator.getInstructor())) return false;
		if (getOffering() == null || offeringCoordinator.getOffering() == null || !getOffering().equals(offeringCoordinator.getOffering())) return false;
		return true;
	}

	public int hashCode() {
		if (getInstructor() == null || getOffering() == null) return super.hashCode();
		return getInstructor().hashCode() ^ getOffering().hashCode();
	}

	public String toString() {
		return "OfferingCoordinator[" + getInstructor() + ", " + getOffering() + "]";
	}

	public String toDebugString() {
		return "OfferingCoordinator[" +
			"\n	Instructor: " + getInstructor() +
			"\n	Offering: " + getOffering() +
			"\n	Responsibility: " + getResponsibility() +
			"\n	TeachingRequest: " + getTeachingRequest() +
			"]";
	}
}
