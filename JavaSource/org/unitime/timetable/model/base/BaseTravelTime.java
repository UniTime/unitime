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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TravelTime;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseTravelTime implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iLocation1Id;
	private Long iLocation2Id;
	private Integer iDistance;

	private Session iSession;

	public BaseTravelTime() {
	}

	public BaseTravelTime(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "travel_time_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "travel_time_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "loc1_id", nullable = false)
	public Long getLocation1Id() { return iLocation1Id; }
	public void setLocation1Id(Long location1Id) { iLocation1Id = location1Id; }

	@Column(name = "loc2_id", nullable = false)
	public Long getLocation2Id() { return iLocation2Id; }
	public void setLocation2Id(Long location2Id) { iLocation2Id = location2Id; }

	@Column(name = "distance", nullable = false)
	public Integer getDistance() { return iDistance; }
	public void setDistance(Integer distance) { iDistance = distance; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TravelTime)) return false;
		if (getUniqueId() == null || ((TravelTime)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TravelTime)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "TravelTime["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TravelTime[" +
			"\n	Distance: " + getDistance() +
			"\n	Location1Id: " + getLocation1Id() +
			"\n	Location2Id: " + getLocation2Id() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
