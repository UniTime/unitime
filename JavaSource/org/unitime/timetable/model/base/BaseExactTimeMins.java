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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.ExactTimeMins;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseExactTimeMins implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iMinsPerMtgMin;
	private Integer iMinsPerMtgMax;
	private Integer iNrSlots;
	private Integer iBreakTime;


	public BaseExactTimeMins() {
	}

	public BaseExactTimeMins(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "exact_time_mins_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "exact_time_mins_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "mins_min", nullable = false, length = 4)
	public Integer getMinsPerMtgMin() { return iMinsPerMtgMin; }
	public void setMinsPerMtgMin(Integer minsPerMtgMin) { iMinsPerMtgMin = minsPerMtgMin; }

	@Column(name = "mins_max", nullable = false, length = 4)
	public Integer getMinsPerMtgMax() { return iMinsPerMtgMax; }
	public void setMinsPerMtgMax(Integer minsPerMtgMax) { iMinsPerMtgMax = minsPerMtgMax; }

	@Column(name = "nr_slots", nullable = false, length = 4)
	public Integer getNrSlots() { return iNrSlots; }
	public void setNrSlots(Integer nrSlots) { iNrSlots = nrSlots; }

	@Column(name = "break_time", nullable = false, length = 3)
	public Integer getBreakTime() { return iBreakTime; }
	public void setBreakTime(Integer breakTime) { iBreakTime = breakTime; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExactTimeMins)) return false;
		if (getUniqueId() == null || ((ExactTimeMins)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExactTimeMins)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ExactTimeMins["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ExactTimeMins[" +
			"\n	BreakTime: " + getBreakTime() +
			"\n	MinsPerMtgMax: " + getMinsPerMtgMax() +
			"\n	MinsPerMtgMin: " + getMinsPerMtgMin() +
			"\n	NrSlots: " + getNrSlots() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
