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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TeachingRequest;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseInstructionalOffering implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iInstrOfferingPermId;
	private Boolean iNotOffered;
	private Integer iDemand;
	private Integer iEnrollment;
	private Integer iLimit;
	private Long iUniqueIdRolledForwardFrom;
	private String iExternalUniqueId;
	private Boolean iByReservationOnly;
	private Integer iLastWeekToEnroll;
	private Integer iLastWeekToChange;
	private Integer iLastWeekToDrop;

	private Session iSession;
	private Set<CourseOffering> iCourseOfferings;
	private Set<InstrOfferingConfig> iInstrOfferingConfigs;
	private Set<Reservation> iReservations;
	private Set<OfferingCoordinator> iOfferingCoordinators;
	private Set<TeachingRequest> iTeachingRequests;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_INSTR_OFFERING_PERM_ID = "instrOfferingPermId";
	public static String PROP_NOT_OFFERED = "notOffered";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_REQ_RESERVATION = "byReservationOnly";
	public static String PROP_WK_ENROLL = "lastWeekToEnroll";
	public static String PROP_WK_CHANGE = "lastWeekToChange";
	public static String PROP_WK_DROP = "lastWeekToDrop";

	public BaseInstructionalOffering() {
		initialize();
	}

	public BaseInstructionalOffering(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getInstrOfferingPermId() { return iInstrOfferingPermId; }
	public void setInstrOfferingPermId(Integer instrOfferingPermId) { iInstrOfferingPermId = instrOfferingPermId; }

	public Boolean isNotOffered() { return iNotOffered; }
	public Boolean getNotOffered() { return iNotOffered; }
	public void setNotOffered(Boolean notOffered) { iNotOffered = notOffered; }

	public Integer getDemand() { return iDemand; }
	public void setDemand(Integer demand) { iDemand = demand; }

	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public Boolean isByReservationOnly() { return iByReservationOnly; }
	public Boolean getByReservationOnly() { return iByReservationOnly; }
	public void setByReservationOnly(Boolean byReservationOnly) { iByReservationOnly = byReservationOnly; }

	public Integer getLastWeekToEnroll() { return iLastWeekToEnroll; }
	public void setLastWeekToEnroll(Integer lastWeekToEnroll) { iLastWeekToEnroll = lastWeekToEnroll; }

	public Integer getLastWeekToChange() { return iLastWeekToChange; }
	public void setLastWeekToChange(Integer lastWeekToChange) { iLastWeekToChange = lastWeekToChange; }

	public Integer getLastWeekToDrop() { return iLastWeekToDrop; }
	public void setLastWeekToDrop(Integer lastWeekToDrop) { iLastWeekToDrop = lastWeekToDrop; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<CourseOffering> getCourseOfferings() { return iCourseOfferings; }
	public void setCourseOfferings(Set<CourseOffering> courseOfferings) { iCourseOfferings = courseOfferings; }
	public void addTocourseOfferings(CourseOffering courseOffering) {
		if (iCourseOfferings == null) iCourseOfferings = new HashSet<CourseOffering>();
		iCourseOfferings.add(courseOffering);
	}

	public Set<InstrOfferingConfig> getInstrOfferingConfigs() { return iInstrOfferingConfigs; }
	public void setInstrOfferingConfigs(Set<InstrOfferingConfig> instrOfferingConfigs) { iInstrOfferingConfigs = instrOfferingConfigs; }
	public void addToinstrOfferingConfigs(InstrOfferingConfig instrOfferingConfig) {
		if (iInstrOfferingConfigs == null) iInstrOfferingConfigs = new HashSet<InstrOfferingConfig>();
		iInstrOfferingConfigs.add(instrOfferingConfig);
	}

	public Set<Reservation> getReservations() { return iReservations; }
	public void setReservations(Set<Reservation> reservations) { iReservations = reservations; }
	public void addToreservations(Reservation reservation) {
		if (iReservations == null) iReservations = new HashSet<Reservation>();
		iReservations.add(reservation);
	}

	public Set<OfferingCoordinator> getOfferingCoordinators() { return iOfferingCoordinators; }
	public void setOfferingCoordinators(Set<OfferingCoordinator> offeringCoordinators) { iOfferingCoordinators = offeringCoordinators; }
	public void addToofferingCoordinators(OfferingCoordinator offeringCoordinator) {
		if (iOfferingCoordinators == null) iOfferingCoordinators = new HashSet<OfferingCoordinator>();
		iOfferingCoordinators.add(offeringCoordinator);
	}

	public Set<TeachingRequest> getTeachingRequests() { return iTeachingRequests; }
	public void setTeachingRequests(Set<TeachingRequest> teachingRequests) { iTeachingRequests = teachingRequests; }
	public void addToteachingRequests(TeachingRequest teachingRequest) {
		if (iTeachingRequests == null) iTeachingRequests = new HashSet<TeachingRequest>();
		iTeachingRequests.add(teachingRequest);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructionalOffering)) return false;
		if (getUniqueId() == null || ((InstructionalOffering)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructionalOffering)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "InstructionalOffering["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "InstructionalOffering[" +
			"\n	ByReservationOnly: " + getByReservationOnly() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	InstrOfferingPermId: " + getInstrOfferingPermId() +
			"\n	LastWeekToChange: " + getLastWeekToChange() +
			"\n	LastWeekToDrop: " + getLastWeekToDrop() +
			"\n	LastWeekToEnroll: " + getLastWeekToEnroll() +
			"\n	NotOffered: " + getNotOffered() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
