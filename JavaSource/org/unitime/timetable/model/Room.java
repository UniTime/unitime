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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseRoom;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.ExternalRoomDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.RoomDeptDAO;
import org.unitime.timetable.util.LocationPermIdGenerator;


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
		java.lang.Long permanentId,
		java.lang.Integer capacity,
		java.lang.Integer coordinateX,
		java.lang.Integer coordinateY,
		java.lang.Boolean ignoreTooFar,
		java.lang.Boolean ignoreRoomCheck) {

		super (
			uniqueId,
			permanentId,
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
		r.setRoomType(getRoomType());
		r.setSession(getSession());
		r.setPermanentId(getPermanentId());
		r.setExamCapacity(getExamCapacity());
		r.setExamType(getExamType());
		return r;
	}
	
	public Room findSameRoomInSession(Session newSession) throws Exception{
		if (newSession == null) {
			return(null);
		}
		Room newRoom = null;
		RoomDAO rDao = new RoomDAO();
		newRoom = (Room) rDao.getSession().createCriteria(Room.class)
			.add(Restrictions.eq("permanentId", getPermanentId()))
			.add(Restrictions.eq("session.uniqueId", newSession.getUniqueId()))
			.setCacheable(true).uniqueResult();
		if (newRoom == null && getExternalUniqueId() != null) {			
			newRoom = (Room)rDao.getSession().createCriteria(Room.class)
				.add(Restrictions.eq("externalUniqueId", getExternalUniqueId()))
				.add(Restrictions.eq("session.uniqueId", newSession.getUniqueId()))
				.setCacheable(true).uniqueResult();			
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
    
	public void addExternalRoomDept(ExternalRoomDepartment externalRoomDept, Set externalRoomDepts){
		Department dept = Department.findByDeptCode(externalRoomDept.getDepartmentCode(), this.getSession().getUniqueId());
		RoomDept roomDept = null;
		RoomDeptDAO rdDao = new RoomDeptDAO();
		if (dept != null){
			roomDept = new RoomDept();
			roomDept.setRoom(this);
			roomDept.setControl(new Boolean(ExternalRoomDepartment.isControllingExternalDept(externalRoomDept, externalRoomDepts)));
			roomDept.setDepartment(dept);
			this.addToroomDepts(roomDept);
			dept.addToroomDepts(roomDept);
			rdDao.saveOrUpdate(roomDept);
		}
	}
    
	public static void addNewExternalRoomsToSession(Session session){
		ExternalRoomDAO erDao = new ExternalRoomDAO();
		BuildingDAO bDao = new BuildingDAO();
		RoomDAO rDao = new RoomDAO();
		String query = "from ExternalRoom er where er.building.session.uniqueId=:sessionId ";
		query += " and er.externalUniqueId not in (select r.externalUniqueId from Room r where r.session.uniqueId =:sessionId)";
		query += " and er.classification in ('classroom', 'classLab')";
		List l = erDao.getQuery(query).setLong("sessionId", session.getUniqueId()).list();
		if (l != null){
			ExternalRoom er = null;
			Room r = null;
			Building b = null;
			for (Iterator erIt = l.iterator(); erIt.hasNext();){
				er = (ExternalRoom) erIt.next();
				b = Building.findByExternalIdAndSession(er.getBuilding().getExternalUniqueId(), session);
				
				if (b == null){
					b = new Building();
					b.setAbbreviation(er.getBuilding().getAbbreviation());
					b.setCoordinateX(er.getBuilding().getCoordinateX());
					b.setCoordinateY(er.getBuilding().getCoordinateY());
					b.setExternalUniqueId(er.getBuilding().getExternalUniqueId());
					b.setName(er.getBuilding().getDisplayName());
					b.setSession(session);
					bDao.saveOrUpdate(b);
				}
				r = new Room();
				r.setBuilding(b);
				r.setCapacity(er.getCapacity());
				r.setExamCapacity(er.getExamCapacity());
				r.setClassification(er.getClassification());
				r.setCoordinateX(er.getCoordinateX());
				r.setCoordinateY(er.getCoordinateY());
				r.setDisplayName(er.getDisplayName());
				r.setExternalUniqueId(er.getExternalUniqueId());
				r.setIgnoreRoomCheck(new Boolean(false));
				r.setIgnoreTooFar(new Boolean(false));
				r.setRoomNumber(er.getRoomNumber());
				r.setRoomType(er.getRoomType());
				r.setSession(session);
				LocationPermIdGenerator.setPermanentId(r);
				if (er.getRoomFeatures() != null){
					ExternalRoomFeature erf = null;
					GlobalRoomFeature grf = null;
					for (Iterator erfIt = er.getRoomFeatures().iterator(); erfIt.hasNext();){
						erf = (ExternalRoomFeature) erfIt.next();
						grf = GlobalRoomFeature.findGlobalRoomFeatureForLabel(erf.getValue());
						if (grf != null){
							r.addTofeatures(grf);
						}
					}
				}
				rDao.saveOrUpdate(r);
				ExternalRoomDepartment toExternalRoomDept = null;
				for(Iterator erdIt = er.getRoomDepartments().iterator(); erdIt.hasNext();){
					toExternalRoomDept = (ExternalRoomDepartment) erdIt.next();
					r.addExternalRoomDept(toExternalRoomDept, er.getRoomDepartments());
					
				}
			}
		}
	
	}
	
	public String getRoomTypeLabel() {
	    return getRoomType().getLabel();
	}

}