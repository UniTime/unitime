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

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.RoomPref;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseRoomPref extends Preference implements Serializable {
	private static final long serialVersionUID = 1L;

	private Location iRoom;

	public BaseRoomPref() {
	}

	public BaseRoomPref(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "room_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Location getRoom() { return iRoom; }
	public void setRoom(Location room) { iRoom = room; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomPref)) return false;
		if (getUniqueId() == null || ((RoomPref)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomPref)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "RoomPref["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "RoomPref[" +
			"\n	Note: " + getNote() +
			"\n	Owner: " + getOwner() +
			"\n	PrefLevel: " + getPrefLevel() +
			"\n	Room: " + getRoom() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
