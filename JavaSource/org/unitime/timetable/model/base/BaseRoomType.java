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
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.io.Serializable;

import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.RoomType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseRoomType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iOrd;
	private Boolean iRoom;


	public BaseRoomType() {
	}

	public BaseRoomType(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "ord", nullable = false)
	public Integer getOrd() { return iOrd; }
	public void setOrd(Integer ord) { iOrd = ord; }

	@Column(name = "is_room", nullable = false)
	public Boolean isRoom() { return iRoom; }
	@Transient
	public Boolean getRoom() { return iRoom; }
	public void setRoom(Boolean room) { iRoom = room; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomType)) return false;
		if (getUniqueId() == null || ((RoomType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomType)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "RoomType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "RoomType[" +
			"\n	Label: " + getLabel() +
			"\n	Ord: " + getOrd() +
			"\n	Reference: " + getReference() +
			"\n	Room: " + getRoom() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
