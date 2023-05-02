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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeatureType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseRoomFeature implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iLabel;
	private String iAbbv;
	private String iDescription;

	private RoomFeatureType iFeatureType;
	private Set<Location> iRooms;

	public BaseRoomFeature() {
	}

	public BaseRoomFeature(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "room_feature_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "room_feature_seq")
	})
	@GeneratedValue(generator = "room_feature_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "label", nullable = false, length = 60)
	public String getLabel() { return iLabel; }
	public void setLabel(String label) { iLabel = label; }

	@Column(name = "abbv", nullable = true, length = 60)
	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	@Column(name = "description", nullable = true, length = 1000)
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "feature_type_id", nullable = true)
	public RoomFeatureType getFeatureType() { return iFeatureType; }
	public void setFeatureType(RoomFeatureType featureType) { iFeatureType = featureType; }

	@ManyToMany(mappedBy = "features")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<Location> getRooms() { return iRooms; }
	public void setRooms(Set<Location> rooms) { iRooms = rooms; }
	public void addToRooms(Location location) {
		if (iRooms == null) iRooms = new HashSet<Location>();
		iRooms.add(location);
	}
	@Deprecated
	public void addTorooms(Location location) {
		addToRooms(location);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomFeature)) return false;
		if (getUniqueId() == null || ((RoomFeature)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomFeature)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "RoomFeature["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "RoomFeature[" +
			"\n	Abbv: " + getAbbv() +
			"\n	Description: " + getDescription() +
			"\n	FeatureType: " + getFeatureType() +
			"\n	Label: " + getLabel() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
