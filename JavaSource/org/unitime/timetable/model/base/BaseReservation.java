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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Reservation;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseReservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iExpirationDate;
	private Integer iLimit;
	private Date iStartDate;
	private Boolean iInclusive;

	private InstructionalOffering iInstructionalOffering;
	private Set<InstrOfferingConfig> iConfigurations;
	private Set<Class_> iClasses;

	public BaseReservation() {
	}

	public BaseReservation(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "reservation_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "reservation_seq")
	})
	@GeneratedValue(generator = "reservation_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "expiration_date", nullable = true)
	public Date getExpirationDate() { return iExpirationDate; }
	public void setExpirationDate(Date expirationDate) { iExpirationDate = expirationDate; }

	@Column(name = "reservation_limit", nullable = true)
	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	@Column(name = "start_date", nullable = true)
	public Date getStartDate() { return iStartDate; }
	public void setStartDate(Date startDate) { iStartDate = startDate; }

	@Column(name = "inclusive", nullable = true)
	public Boolean isInclusive() { return iInclusive; }
	@Transient
	public Boolean getInclusive() { return iInclusive; }
	public void setInclusive(Boolean inclusive) { iInclusive = inclusive; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "offering_id", nullable = false)
	public InstructionalOffering getInstructionalOffering() { return iInstructionalOffering; }
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) { iInstructionalOffering = instructionalOffering; }

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "reservation_config",
		joinColumns = { @JoinColumn(name = "reservation_id") },
		inverseJoinColumns = { @JoinColumn(name = "config_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<InstrOfferingConfig> getConfigurations() { return iConfigurations; }
	public void setConfigurations(Set<InstrOfferingConfig> configurations) { iConfigurations = configurations; }
	public void addToconfigurations(InstrOfferingConfig instrOfferingConfig) {
		if (iConfigurations == null) iConfigurations = new HashSet<InstrOfferingConfig>();
		iConfigurations.add(instrOfferingConfig);
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "reservation_class",
		joinColumns = { @JoinColumn(name = "reservation_id") },
		inverseJoinColumns = { @JoinColumn(name = "class_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<Class_> getClasses() { return iClasses; }
	public void setClasses(Set<Class_> classes) { iClasses = classes; }
	public void addToclasses(Class_ class_) {
		if (iClasses == null) iClasses = new HashSet<Class_>();
		iClasses.add(class_);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Reservation)) return false;
		if (getUniqueId() == null || ((Reservation)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Reservation)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Reservation["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Reservation[" +
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	Inclusive: " + getInclusive() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	StartDate: " + getStartDate() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
