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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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
@MappedSuperclass
public abstract class BaseInstructionalOffering implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iInstrOfferingPermId;
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
	private String iNotes;
	private Integer iSnapshotLimit;
	private Date iSnapshotLimitDate;
	private Integer iWaitlistMode;

	private Session iSession;
	private Set<CourseOffering> iCourseOfferings;
	private Set<InstrOfferingConfig> iInstrOfferingConfigs;
	private Set<Reservation> iReservations;
	private Set<OfferingCoordinator> iOfferingCoordinators;
	private Set<TeachingRequest> iTeachingRequests;

	public BaseInstructionalOffering() {
	}

	public BaseInstructionalOffering(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "instructional_offering_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "instr_offr_seq")
	})
	@GeneratedValue(generator = "instructional_offering_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "instr_offering_perm_id", nullable = false, length = 20)
	public Long getInstrOfferingPermId() { return iInstrOfferingPermId; }
	public void setInstrOfferingPermId(Long instrOfferingPermId) { iInstrOfferingPermId = instrOfferingPermId; }

	@Column(name = "not_offered", nullable = false)
	public Boolean isNotOffered() { return iNotOffered; }
	@Transient
	public Boolean getNotOffered() { return iNotOffered; }
	public void setNotOffered(Boolean notOffered) { iNotOffered = notOffered; }

	@Formula("(      select sum( co.lastlike_demand +       (case when cox.lastlike_demand is null then 0 else cox.lastlike_demand end))      from %SCHEMA%.course_offering co left outer join %SCHEMA%.course_offering cox on co.demand_offering_id=cox.uniqueid       where co.instr_offr_id = uniqueid)")
	public Integer getDemand() { return iDemand; }
	public void setDemand(Integer demand) { iDemand = demand; }

	@Formula("(select count(distinct e.student_id) from %SCHEMA%.student_class_enrl e inner join %SCHEMA%.course_offering co on co.uniqueid = e.course_offering_id where co.instr_offr_id = uniqueid)")
	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	@Formula(" ( select sum(ioc.config_limit) from %SCHEMA%.instr_offering_config ioc where ioc.instr_offr_id = uniqueid ) ")
	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "req_reservation", nullable = false)
	public Boolean isByReservationOnly() { return iByReservationOnly; }
	@Transient
	public Boolean getByReservationOnly() { return iByReservationOnly; }
	public void setByReservationOnly(Boolean byReservationOnly) { iByReservationOnly = byReservationOnly; }

	@Column(name = "wk_enroll", nullable = true)
	public Integer getLastWeekToEnroll() { return iLastWeekToEnroll; }
	public void setLastWeekToEnroll(Integer lastWeekToEnroll) { iLastWeekToEnroll = lastWeekToEnroll; }

	@Column(name = "wk_change", nullable = true)
	public Integer getLastWeekToChange() { return iLastWeekToChange; }
	public void setLastWeekToChange(Integer lastWeekToChange) { iLastWeekToChange = lastWeekToChange; }

	@Column(name = "wk_drop", nullable = true)
	public Integer getLastWeekToDrop() { return iLastWeekToDrop; }
	public void setLastWeekToDrop(Integer lastWeekToDrop) { iLastWeekToDrop = lastWeekToDrop; }

	@Column(name = "notes", nullable = true, length = 2000)
	public String getNotes() { return iNotes; }
	public void setNotes(String notes) { iNotes = notes; }

	@Column(name = "snapshot_limit", nullable = true, length = 10)
	public Integer getSnapshotLimit() { return iSnapshotLimit; }
	public void setSnapshotLimit(Integer snapshotLimit) { iSnapshotLimit = snapshotLimit; }

	@Column(name = "snapshot_limit_date", nullable = true)
	public Date getSnapshotLimitDate() { return iSnapshotLimitDate; }
	public void setSnapshotLimitDate(Date snapshotLimitDate) { iSnapshotLimitDate = snapshotLimitDate; }

	@Column(name = "waitlist", nullable = true)
	public Integer getWaitlistMode() { return iWaitlistMode; }
	public void setWaitlistMode(Integer waitlistMode) { iWaitlistMode = waitlistMode; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instructionalOffering", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<CourseOffering> getCourseOfferings() { return iCourseOfferings; }
	public void setCourseOfferings(Set<CourseOffering> courseOfferings) { iCourseOfferings = courseOfferings; }
	public void addTocourseOfferings(CourseOffering courseOffering) {
		if (iCourseOfferings == null) iCourseOfferings = new HashSet<CourseOffering>();
		iCourseOfferings.add(courseOffering);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instructionalOffering", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<InstrOfferingConfig> getInstrOfferingConfigs() { return iInstrOfferingConfigs; }
	public void setInstrOfferingConfigs(Set<InstrOfferingConfig> instrOfferingConfigs) { iInstrOfferingConfigs = instrOfferingConfigs; }
	public void addToinstrOfferingConfigs(InstrOfferingConfig instrOfferingConfig) {
		if (iInstrOfferingConfigs == null) iInstrOfferingConfigs = new HashSet<InstrOfferingConfig>();
		iInstrOfferingConfigs.add(instrOfferingConfig);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instructionalOffering")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Reservation> getReservations() { return iReservations; }
	public void setReservations(Set<Reservation> reservations) { iReservations = reservations; }
	public void addToreservations(Reservation reservation) {
		if (iReservations == null) iReservations = new HashSet<Reservation>();
		iReservations.add(reservation);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "offering")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<OfferingCoordinator> getOfferingCoordinators() { return iOfferingCoordinators; }
	public void setOfferingCoordinators(Set<OfferingCoordinator> offeringCoordinators) { iOfferingCoordinators = offeringCoordinators; }
	public void addToofferingCoordinators(OfferingCoordinator offeringCoordinator) {
		if (iOfferingCoordinators == null) iOfferingCoordinators = new HashSet<OfferingCoordinator>();
		iOfferingCoordinators.add(offeringCoordinator);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "offering", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<TeachingRequest> getTeachingRequests() { return iTeachingRequests; }
	public void setTeachingRequests(Set<TeachingRequest> teachingRequests) { iTeachingRequests = teachingRequests; }
	public void addToteachingRequests(TeachingRequest teachingRequest) {
		if (iTeachingRequests == null) iTeachingRequests = new HashSet<TeachingRequest>();
		iTeachingRequests.add(teachingRequest);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructionalOffering)) return false;
		if (getUniqueId() == null || ((InstructionalOffering)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructionalOffering)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
			"\n	Notes: " + getNotes() +
			"\n	Session: " + getSession() +
			"\n	SnapshotLimit: " + getSnapshotLimit() +
			"\n	SnapshotLimitDate: " + getSnapshotLimitDate() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"\n	WaitlistMode: " + getWaitlistMode() +
			"]";
	}
}
