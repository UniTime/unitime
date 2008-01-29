/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseRoom;
import org.unitime.timetable.model.dao.RoomDAO;


public class Room extends BaseRoom {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Room () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Room (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public Room (
		java.lang.Long uniqueId,
		java.lang.Integer capacity,
		java.lang.Integer coordinateX,
		java.lang.Integer coordinateY,
		java.lang.Boolean ignoreTooFar,
		java.lang.Boolean ignoreRoomCheck) {

		super (
			uniqueId,
			capacity,
			coordinateX,
			coordinateY,
			ignoreTooFar,
			ignoreRoomCheck);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public String bldgAbbvRoomNumber() {
		return getBuildingAbbv() + " " + getRoomNumber();
	}
	
	public String toString () {
		return bldgAbbvRoomNumber();
	}
	
    /** Request attribute name for available rooms **/
    public static String ROOM_LIST_ATTR_NAME = "roomsList";
	
    /**
     * Returns room label of the form BLDG ROOM e.g. HTM 101
     * This method is used as a getter for property label
     * @return Room Label
     */
    public String getLabel() {
        return this.bldgAbbvRoomNumber();
    }
    
	public void addToroomDepts (org.unitime.timetable.model.RoomDept roomDept) {
		if (null == getRoomDepts()) setRoomDepts(new java.util.HashSet());
		getRoomDepts().add(roomDept);
	}
	
	public Object clone() {
		Room r = new Room();
		r.setBuilding(r.getBuilding());
		r.setCapacity(getCapacity());
		r.setClassification(getClassification());
		r.setCoordinateX(getCoordinateX());
		r.setCoordinateY(getCoordinateY());
		r.setDisplayName(getDisplayName());
		r.setExternalUniqueId(getExternalUniqueId());
		r.setIgnoreRoomCheck(isIgnoreRoomCheck());
		r.setIgnoreTooFar(isIgnoreTooFar());
		r.setPattern(getPattern());
		r.setRoomNumber(getRoomNumber());
		r.setScheduledRoomType(getScheduledRoomType());
		r.setSession(getSession());
		return r;
	}
	
	public Room findSameRoomInSession(Session newSession) throws Exception{
		if (newSession == null) {
			return(null);
		}
		Room newRoom = null;
		RoomDAO rDao = new RoomDAO();
		Building newBuilding = getBuilding().findSameBuildingInSession(newSession);
//		String query = "from Room r where r.building.uniqueId = " + newBuilding.getUniqueId().toString();
//		query += " and r.session.uniqueId = " + newSession.getUniqueId().toString();
//		query += " and r.roomNumber = '" + getRoomNumber() + "'";
		List rooms = rDao.getSession().createCriteria(Room.class)
			.add(Restrictions.eq("building.uniqueId", newBuilding.getUniqueId()))
			.add(Restrictions.eq("session.uniqueId", newSession.getUniqueId()))
			.add(Restrictions.eq("roomNumber", getRoomNumber()))
			.setCacheable(true).list();
		if (rooms != null && rooms.size() == 1) {
			newRoom = (Room) rooms.get(0);
		} else if (getExternalUniqueId() != null) {			
			rooms = rDao.getSession().createCriteria(Room.class)
				.add(Restrictions.eq("externalUniqueId", getExternalUniqueId()))
				.add(Restrictions.eq("session.uniqueId", newSession.getUniqueId()))
				.setCacheable(true).list();
			
			if (rooms != null && rooms.size() == 1){
				newRoom = (Room) rooms.get(0);
			}
		}
		return(newRoom);
	}
    
    public static Room findByBldgIdRoomNbr(Long bldgId, String roomNbr, Long sessionId) {
        return (Room)new RoomDAO().getSession().createQuery(
                "select r from Room r where r.building.uniqueId=:bldgId and r.roomNumber=:roomNbr and r.session.uniqueId=:sessionId").
                setLong("bldgId", bldgId).
                setString("roomNbr", roomNbr).
                setLong("sessionId", sessionId).
                uniqueResult();
    }
}