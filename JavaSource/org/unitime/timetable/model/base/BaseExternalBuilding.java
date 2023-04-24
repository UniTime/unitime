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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseExternalBuilding implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iAbbreviation;
	private Double iCoordinateX;
	private Double iCoordinateY;
	private String iDisplayName;

	private Session iSession;
	private Set<ExternalRoom> iRooms;

	public BaseExternalBuilding() {
	}

	public BaseExternalBuilding(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "external_building_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "external_building_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "abbreviation", nullable = false, length = 20)
	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }

	@Column(name = "coordinate_x", nullable = true)
	public Double getCoordinateX() { return iCoordinateX; }
	public void setCoordinateX(Double coordinateX) { iCoordinateX = coordinateX; }

	@Column(name = "coordinate_y", nullable = true)
	public Double getCoordinateY() { return iCoordinateY; }
	public void setCoordinateY(Double coordinateY) { iCoordinateY = coordinateY; }

	@Column(name = "display_name", nullable = true, length = 100)
	public String getDisplayName() { return iDisplayName; }
	public void setDisplayName(String displayName) { iDisplayName = displayName; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@OneToMany
	@JoinColumn(name = "external_bldg_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<ExternalRoom> getRooms() { return iRooms; }
	public void setRooms(Set<ExternalRoom> rooms) { iRooms = rooms; }
	public void addTorooms(ExternalRoom externalRoom) {
		if (iRooms == null) iRooms = new HashSet<ExternalRoom>();
		iRooms.add(externalRoom);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExternalBuilding)) return false;
		if (getUniqueId() == null || ((ExternalBuilding)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExternalBuilding)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ExternalBuilding["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ExternalBuilding[" +
			"\n	Abbreviation: " + getAbbreviation() +
			"\n	CoordinateX: " + getCoordinateX() +
			"\n	CoordinateY: " + getCoordinateY() +
			"\n	DisplayName: " + getDisplayName() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
