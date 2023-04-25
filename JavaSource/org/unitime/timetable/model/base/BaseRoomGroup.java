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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseRoomGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iAbbv;
	private String iDescription;
	private Boolean iGlobal;
	private Boolean iDefaultGroup;

	private Department iDepartment;
	private Session iSession;
	private Set<Location> iRooms;

	public BaseRoomGroup() {
	}

	public BaseRoomGroup(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "room_group_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "room_group_seq")
	})
	@GeneratedValue(generator = "room_group_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = false, length = 60)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "abbv", nullable = false, length = 60)
	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	@Column(name = "description", nullable = true, length = 1000)
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	@Column(name = "global", nullable = false)
	public Boolean isGlobal() { return iGlobal; }
	@Transient
	public Boolean getGlobal() { return iGlobal; }
	public void setGlobal(Boolean global) { iGlobal = global; }

	@Column(name = "default_group", nullable = false)
	public Boolean isDefaultGroup() { return iDefaultGroup; }
	@Transient
	public Boolean getDefaultGroup() { return iDefaultGroup; }
	public void setDefaultGroup(Boolean defaultGroup) { iDefaultGroup = defaultGroup; }

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "department_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToMany(mappedBy = "roomGroups")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<Location> getRooms() { return iRooms; }
	public void setRooms(Set<Location> rooms) { iRooms = rooms; }
	public void addTorooms(Location location) {
		if (iRooms == null) iRooms = new HashSet<Location>();
		iRooms.add(location);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomGroup)) return false;
		if (getUniqueId() == null || ((RoomGroup)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomGroup)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "RoomGroup["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "RoomGroup[" +
			"\n	Abbv: " + getAbbv() +
			"\n	DefaultGroup: " + getDefaultGroup() +
			"\n	Department: " + getDepartment() +
			"\n	Description: " + getDescription() +
			"\n	Global: " + getGlobal() +
			"\n	Name: " + getName() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
