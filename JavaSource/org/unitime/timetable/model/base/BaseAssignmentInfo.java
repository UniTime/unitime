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

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.AssignmentInfo;
import org.unitime.timetable.model.SolverInfo;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseAssignmentInfo extends SolverInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private Assignment iAssignment;


	public BaseAssignmentInfo() {
		initialize();
	}

	public BaseAssignmentInfo(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Assignment getAssignment() { return iAssignment; }
	public void setAssignment(Assignment assignment) { iAssignment = assignment; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof AssignmentInfo)) return false;
		if (getUniqueId() == null || ((AssignmentInfo)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((AssignmentInfo)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "AssignmentInfo["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "AssignmentInfo[" +
			"\n	Assignment: " + getAssignment() +
			"\n	Data: " + getData() +
			"\n	Definition: " + getDefinition() +
			"\n	Opt: " + getOpt() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
