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

import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PitCourseOffering;
import org.unitime.timetable.model.PitInstrOfferingConfig;
import org.unitime.timetable.model.PitInstructionalOffering;
import org.unitime.timetable.model.PointInTimeData;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitInstructionalOffering implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iInstrOfferingPermId;
	private Integer iDemand;
	private Integer iLimit;
	private Long iUniqueIdRolledForwardFrom;
	private String iExternalUniqueId;
	private Integer iEnrollment;
	private Integer iCtrlCourseId;

	private PointInTimeData iPointInTimeData;
	private InstructionalOffering iInstructionalOffering;
	private Set<PitCourseOffering> iPitCourseOfferings;
	private Set<PitInstrOfferingConfig> iPitInstrOfferingConfigs;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_INSTR_OFFERING_PERM_ID = "instrOfferingPermId";
	public static String PROP_DEMAND = "demand";
	public static String PROP_OFFR_LIMIT = "limit";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";

	public BasePitInstructionalOffering() {
		initialize();
	}

	public BasePitInstructionalOffering(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getInstrOfferingPermId() { return iInstrOfferingPermId; }
	public void setInstrOfferingPermId(Integer instrOfferingPermId) { iInstrOfferingPermId = instrOfferingPermId; }

	public Integer getDemand() { return iDemand; }
	public void setDemand(Integer demand) { iDemand = demand; }

	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	public Integer getCtrlCourseId() { return iCtrlCourseId; }
	public void setCtrlCourseId(Integer ctrlCourseId) { iCtrlCourseId = ctrlCourseId; }

	public PointInTimeData getPointInTimeData() { return iPointInTimeData; }
	public void setPointInTimeData(PointInTimeData pointInTimeData) { iPointInTimeData = pointInTimeData; }

	public InstructionalOffering getInstructionalOffering() { return iInstructionalOffering; }
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) { iInstructionalOffering = instructionalOffering; }

	public Set<PitCourseOffering> getPitCourseOfferings() { return iPitCourseOfferings; }
	public void setPitCourseOfferings(Set<PitCourseOffering> pitCourseOfferings) { iPitCourseOfferings = pitCourseOfferings; }
	public void addTopitCourseOfferings(PitCourseOffering pitCourseOffering) {
		if (iPitCourseOfferings == null) iPitCourseOfferings = new HashSet<PitCourseOffering>();
		iPitCourseOfferings.add(pitCourseOffering);
	}

	public Set<PitInstrOfferingConfig> getPitInstrOfferingConfigs() { return iPitInstrOfferingConfigs; }
	public void setPitInstrOfferingConfigs(Set<PitInstrOfferingConfig> pitInstrOfferingConfigs) { iPitInstrOfferingConfigs = pitInstrOfferingConfigs; }
	public void addTopitInstrOfferingConfigs(PitInstrOfferingConfig pitInstrOfferingConfig) {
		if (iPitInstrOfferingConfigs == null) iPitInstrOfferingConfigs = new HashSet<PitInstrOfferingConfig>();
		iPitInstrOfferingConfigs.add(pitInstrOfferingConfig);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitInstructionalOffering)) return false;
		if (getUniqueId() == null || ((PitInstructionalOffering)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitInstructionalOffering)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "PitInstructionalOffering["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitInstructionalOffering[" +
			"\n	Demand: " + getDemand() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	InstrOfferingPermId: " + getInstrOfferingPermId() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	PointInTimeData: " + getPointInTimeData() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
