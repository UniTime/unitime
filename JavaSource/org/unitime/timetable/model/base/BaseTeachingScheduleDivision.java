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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.TeachingScheduleAssignment;
import org.unitime.timetable.model.TeachingScheduleDivision;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseTeachingScheduleDivision implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private Integer iGroups;
	private Integer iHours;
	private Integer iParallels;
	private Integer iOrder;

	private InstructionalOffering iOffering;
	private InstrOfferingConfig iConfig;
	private ItypeDesc iItype;
	private InstructorAttribute iAttribute;
	private Set<TeachingScheduleAssignment> iAssignments;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_NR_GROUPS = "groups";
	public static String PROP_NR_HOURS = "hours";
	public static String PROP_NR_PARALLELS = "parallels";
	public static String PROP_ORD = "order";

	public BaseTeachingScheduleDivision() {
		initialize();
	}

	public BaseTeachingScheduleDivision(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public Integer getGroups() { return iGroups; }
	public void setGroups(Integer groups) { iGroups = groups; }

	public Integer getHours() { return iHours; }
	public void setHours(Integer hours) { iHours = hours; }

	public Integer getParallels() { return iParallels; }
	public void setParallels(Integer parallels) { iParallels = parallels; }

	public Integer getOrder() { return iOrder; }
	public void setOrder(Integer order) { iOrder = order; }

	public InstructionalOffering getOffering() { return iOffering; }
	public void setOffering(InstructionalOffering offering) { iOffering = offering; }

	public InstrOfferingConfig getConfig() { return iConfig; }
	public void setConfig(InstrOfferingConfig config) { iConfig = config; }

	public ItypeDesc getItype() { return iItype; }
	public void setItype(ItypeDesc itype) { iItype = itype; }

	public InstructorAttribute getAttribute() { return iAttribute; }
	public void setAttribute(InstructorAttribute attribute) { iAttribute = attribute; }

	public Set<TeachingScheduleAssignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<TeachingScheduleAssignment> assignments) { iAssignments = assignments; }
	public void addToassignments(TeachingScheduleAssignment teachingScheduleAssignment) {
		if (iAssignments == null) iAssignments = new HashSet<TeachingScheduleAssignment>();
		iAssignments.add(teachingScheduleAssignment);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof TeachingScheduleDivision)) return false;
		if (getUniqueId() == null || ((TeachingScheduleDivision)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TeachingScheduleDivision)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "TeachingScheduleDivision["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "TeachingScheduleDivision[" +
			"\n	Attribute: " + getAttribute() +
			"\n	Config: " + getConfig() +
			"\n	Groups: " + getGroups() +
			"\n	Hours: " + getHours() +
			"\n	Itype: " + getItype() +
			"\n	Name: " + getName() +
			"\n	Offering: " + getOffering() +
			"\n	Order: " + getOrder() +
			"\n	Parallels: " + getParallels() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
