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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomFeature;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseExternalRoomFeature implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iValue;

	private ExternalRoom iRoom;

	public BaseExternalRoomFeature() {
	}

	public BaseExternalRoomFeature(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "pref_group_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = false, length = 20)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "value", nullable = false, length = 20)
	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "external_room_id", nullable = false)
	public ExternalRoom getRoom() { return iRoom; }
	public void setRoom(ExternalRoom room) { iRoom = room; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExternalRoomFeature)) return false;
		if (getUniqueId() == null || ((ExternalRoomFeature)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExternalRoomFeature)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ExternalRoomFeature["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "ExternalRoomFeature[" +
			"\n	Name: " + getName() +
			"\n	Room: " + getRoom() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Value: " + getValue() +
			"]";
	}
}
