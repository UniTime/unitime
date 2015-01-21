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

import org.unitime.timetable.model.LocationPicture;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomPicture;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseRoomPicture extends LocationPicture implements Serializable {
	private static final long serialVersionUID = 1L;

	private Room iLocation;


	public BaseRoomPicture() {
		initialize();
	}

	public BaseRoomPicture(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Room getLocation() { return iLocation; }
	public void setLocation(Room location) { iLocation = location; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomPicture)) return false;
		if (getUniqueId() == null || ((RoomPicture)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomPicture)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "RoomPicture["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "RoomPicture[" +
			"\n	ContentType: " + getContentType() +
			"\n	DataFile: " + getDataFile() +
			"\n	FileName: " + getFileName() +
			"\n	Location: " + getLocation() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
