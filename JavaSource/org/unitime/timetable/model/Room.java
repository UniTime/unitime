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
package org.unitime.timetable.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseRoom;
import org.unitime.timetable.model.dao.ExternalRoomDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.RoomDeptDAO;
import org.unitime.timetable.util.LocationPermIdGenerator;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
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
		r.setArea(getArea());
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
		r.setExamTypes(new HashSet<ExamType>(getExamTypes()));
		r.setEventStatus(null);
		r.setBreakTime(getBreakTime());
		r.setNote(getNote());
		r.setEventAvailability(getEventAvailability());
		r.setShareNote(getShareNote());
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
    
	public static void addNewExternalRoomsToSession(Session session) {
		String query = "from ExternalRoom er where er.building.session.uniqueId=:sessionId and er.building.externalUniqueId is not null and er.externalUniqueId is not null";
		boolean updateExistingRooms = ApplicationProperty.BuildingsExternalUpdateExistingRooms.isTrue();
		if (!updateExistingRooms)
			query += " and er.externalUniqueId not in (select r.externalUniqueId from Room r where r.session.uniqueId =:sessionId " +
					"and r.externalUniqueId is not null)";
		boolean resetRoomFeatures = ApplicationProperty.BuildingsExternalUpdateExistingRoomFeatures.isTrue();
		boolean resetRoomDepartments = ApplicationProperty.BuildingsExternalUpdateExistingRoomDepartments.isTrue();
		String classifications = ApplicationProperty.BuildingsExternalUpdateClassification.value();
		if (classifications != null && !classifications.isEmpty()) {
			query += " and er.classification in :classifications";
		}
		org.hibernate.Session hibSession = ExternalRoomDAO.getInstance().getSession();
		Query q = hibSession.createQuery(query).setLong("sessionId", session.getUniqueId());
		if (classifications != null && !classifications.isEmpty()) {
			q.setParameterList("classifications", classifications.split(","), new StringType());
		}
		for (ExternalRoom er: (List<ExternalRoom>)q.list()) {
			Building b = Building.findByExternalIdAndSession(er.getBuilding().getExternalUniqueId(), session);
			if (b == null) {
				b = new Building();
				b.setAbbreviation(er.getBuilding().getAbbreviation());
				b.setCoordinateX(er.getBuilding().getCoordinateX());
				b.setCoordinateY(er.getBuilding().getCoordinateY());
				b.setExternalUniqueId(er.getBuilding().getExternalUniqueId());
				b.setName(er.getBuilding().getDisplayName());
				b.setSession(session);
				hibSession.saveOrUpdate(b);
				hibSession.flush();
			} else if (updateExistingRooms) {
				b.setAbbreviation(er.getBuilding().getAbbreviation());
				b.setCoordinateX(er.getBuilding().getCoordinateX());
				b.setCoordinateY(er.getBuilding().getCoordinateY());
				b.setName(er.getBuilding().getDisplayName());
				hibSession.saveOrUpdate(b);
			}
			Room r = (Room)hibSession.createQuery(
					"from Room r where r.building.session.uniqueId = :sessionId and r.externalUniqueId = :externalId")
					.setLong("sessionId", session.getUniqueId())
					.setString("externalId", er.getExternalUniqueId())
					.uniqueResult();
			if (r == null) {
				r = new Room();
				r.setBuilding(b);
				r.setCapacity(er.getCapacity());
				r.setExamCapacity(er.getExamCapacity());
				r.setClassification(er.getClassification());
				r.setCoordinateX(er.getCoordinateX());
				r.setCoordinateY(er.getCoordinateY());
				r.setArea(er.getArea());
				r.setDisplayName(er.getDisplayName());
				r.setExternalUniqueId(er.getExternalUniqueId());
				r.setIgnoreRoomCheck(new Boolean(false));
				r.setIgnoreTooFar(new Boolean(false));
				r.setRoomNumber(er.getRoomNumber());
				r.setRoomType(er.getRoomType());
				r.setSession(session);
				r.setFeatures(new HashSet<RoomFeature>());
				for (ExternalRoomFeature erf: er.getRoomFeatures()) {
					GlobalRoomFeature grf = GlobalRoomFeature.findGlobalRoomFeatureForLabel(session, erf.getValue());
					if (grf == null)
						grf = GlobalRoomFeature.findGlobalRoomFeatureForAbbv(session, erf.getName());
					if (grf != null) {
						grf.getRooms().add(r);
						r.getFeatures().add(grf);
					}
				}
				LocationPermIdGenerator.setPermanentId(r);
				hibSession.saveOrUpdate(r);
				hibSession.flush();
				for (ExternalRoomDepartment erd: er.getRoomDepartments())
					r.addExternalRoomDept(erd, er.getRoomDepartments());
			} else if (updateExistingRooms) {
				r.setBuilding(b);
				r.setCapacity(er.getCapacity());
				r.setExamCapacity(er.getExamCapacity());
				r.setClassification(er.getClassification());
				r.setCoordinateX(er.getCoordinateX());
				r.setCoordinateY(er.getCoordinateY());
				r.setArea(er.getArea());
				r.setDisplayName(er.getDisplayName());
				r.setRoomNumber(er.getRoomNumber());
				r.setRoomType(er.getRoomType());
				if (resetRoomFeatures) {
					for (Iterator<RoomFeature> i = r.getFeatures().iterator(); i.hasNext();) {
						RoomFeature rf = i.next();
						if (rf instanceof GlobalRoomFeature) {
							rf.getRooms().remove(r);
							i.remove();
						}
					}
					for (ExternalRoomFeature erf: er.getRoomFeatures()) {
						GlobalRoomFeature grf = GlobalRoomFeature.findGlobalRoomFeatureForLabel(session, erf.getValue());
						if (grf == null)
							grf = GlobalRoomFeature.findGlobalRoomFeatureForAbbv(session, erf.getName());
						if (grf != null) {
							grf.getRooms().add(r);
							r.getFeatures().add(grf);
						}
					}
				}
				hibSession.saveOrUpdate(r);
				if (resetRoomDepartments) {
					Map<String, ExternalRoomDepartment> code2extRoomDept = new HashMap<String, ExternalRoomDepartment>();
					for (ExternalRoomDepartment erd: er.getRoomDepartments())
						code2extRoomDept.put(erd.getDepartmentCode(), erd);
					for (Iterator<RoomDept> i = r.getRoomDepts().iterator(); i.hasNext(); ) {
						RoomDept rd = (RoomDept)i.next();
						ExternalRoomDepartment erd = code2extRoomDept.remove(rd.getDepartment().getDeptCode());
						if (erd != null) {
							rd.setControl(ExternalRoomDepartment.isControllingExternalDept(erd, er.getRoomDepartments()));
							hibSession.saveOrUpdate(rd);
						} else {
							rd.getDepartment().getRoomDepts().remove(rd);
							i.remove();
							hibSession.delete(rd);
						}
					}
					for (ExternalRoomDepartment erd: code2extRoomDept.values())
						r.addExternalRoomDept(erd, er.getRoomDepartments());
				}
			}
		}
		hibSession.flush();
	}
	
	public String getRoomTypeLabel() {
	    return getRoomType().getLabel();
	}

	@Override
    public List<Location> getFutureLocations() {
    	List<Location> ret = new ArrayList<Location>();
    	Set<Long> futureSessionIds = new HashSet<Long>();
    	Set<Long> blackListedSessionIds = new HashSet<Long>();

    	for (Location location: (List<Location>)LocationDAO.getInstance().getSession().createQuery(
    			"select f from Room l, Room f where " +
    			"l.uniqueId = :uniqueId and " +
    			"l.session.academicInitiative = f.session.academicInitiative and l.session.sessionBeginDateTime < f.session.sessionBeginDateTime and " +
    			"((l.permanentId = f.permanentId) or " + // match on permanent ids
    			"(not exists (from Location x where x.permanentId = f.permanentId and x.session = l.session) and " + // no match on permanent id exist
    			"l.roomType = f.roomType and " + // room type match
    			"((length(f.externalUniqueId) > 0 and l.externalUniqueId = f.externalUniqueId) or " + // external id match
    			"((f.externalUniqueId is null or length(f.externalUniqueId) = 0) and (l.externalUniqueId is null or length(l.externalUniqueId) = 0) and " + // no external id match
    			"f.building.abbreviation = l.building.abbreviation and f.roomNumber = l.roomNumber and f.capacity = l.capacity)))) " + // name & capacity match
    			"order by f.session.sessionBeginDateTime"
    			).setLong("uniqueId", getUniqueId()).setCacheable(true).list()) {
    		if (futureSessionIds.add(location.getSession().getUniqueId()))
    			ret.add(location);
    		else
    			blackListedSessionIds.add(location.getSession().getUniqueId());
    	}

    	if (!blackListedSessionIds.isEmpty())
    		for (Iterator<Location> i = ret.iterator(); i.hasNext(); ) { 
    			Location location = i.next();
    			if (blackListedSessionIds.contains(location.getSession().getUniqueId()))
    				i.remove();
    		}

    	return ret;
    }
}
