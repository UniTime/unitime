/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.base;

import java.util.List;

import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RoomDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseRoomDAO extends _RootDAO<Room,Long> {

	private static RoomDAO sInstance;

	public static RoomDAO getInstance() {
		if (sInstance == null) sInstance = new RoomDAO();
		return sInstance;
	}

	public Class<Room> getReferenceClass() {
		return Room.class;
	}

	@SuppressWarnings("unchecked")
	public List<Room> findByRoomType(org.hibernate.Session hibSession, Long roomTypeId) {
		return hibSession.createQuery("from Room x where x.roomType.uniqueId = :roomTypeId").setLong("roomTypeId", roomTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Room> findByBuilding(org.hibernate.Session hibSession, Long buildingId) {
		return hibSession.createQuery("from Room x where x.building.uniqueId = :buildingId").setLong("buildingId", buildingId).list();
	}
}
