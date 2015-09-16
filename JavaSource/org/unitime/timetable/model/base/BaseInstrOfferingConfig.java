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

import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseInstrOfferingConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iLimit;
	private Boolean iUnlimitedEnrollment;
	private String iName;
	private Long iUniqueIdRolledForwardFrom;

	private InstructionalOffering iInstructionalOffering;
	private ClassDurationType iClassDurationType;
	private InstructionalMethod iInstructionalMethod;
	private Set<SchedulingSubpart> iSchedulingSubparts;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CONFIG_LIMIT = "limit";
	public static String PROP_UNLIMITED_ENROLLMENT = "unlimitedEnrollment";
	public static String PROP_NAME = "name";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	public BaseInstrOfferingConfig() {
		initialize();
	}

	public BaseInstrOfferingConfig(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	public Boolean isUnlimitedEnrollment() { return iUnlimitedEnrollment; }
	public Boolean getUnlimitedEnrollment() { return iUnlimitedEnrollment; }
	public void setUnlimitedEnrollment(Boolean unlimitedEnrollment) { iUnlimitedEnrollment = unlimitedEnrollment; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public InstructionalOffering getInstructionalOffering() { return iInstructionalOffering; }
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) { iInstructionalOffering = instructionalOffering; }

	public ClassDurationType getClassDurationType() { return iClassDurationType; }
	public void setClassDurationType(ClassDurationType classDurationType) { iClassDurationType = classDurationType; }

	public InstructionalMethod getInstructionalMethod() { return iInstructionalMethod; }
	public void setInstructionalMethod(InstructionalMethod instructionalMethod) { iInstructionalMethod = instructionalMethod; }

	public Set<SchedulingSubpart> getSchedulingSubparts() { return iSchedulingSubparts; }
	public void setSchedulingSubparts(Set<SchedulingSubpart> schedulingSubparts) { iSchedulingSubparts = schedulingSubparts; }
	public void addToschedulingSubparts(SchedulingSubpart schedulingSubpart) {
		if (iSchedulingSubparts == null) iSchedulingSubparts = new HashSet<SchedulingSubpart>();
		iSchedulingSubparts.add(schedulingSubpart);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstrOfferingConfig)) return false;
		if (getUniqueId() == null || ((InstrOfferingConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstrOfferingConfig)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "InstrOfferingConfig["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "InstrOfferingConfig[" +
			"\n	ClassDurationType: " + getClassDurationType() +
			"\n	InstructionalMethod: " + getInstructionalMethod() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"\n	UnlimitedEnrollment: " + getUnlimitedEnrollment() +
			"]";
	}
}
