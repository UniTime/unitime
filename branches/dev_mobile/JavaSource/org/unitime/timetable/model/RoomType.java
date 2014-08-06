/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.List;
import java.util.TreeSet;

import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseRoomType;
import org.unitime.timetable.model.dao.RoomTypeDAO;
import org.unitime.timetable.model.dao.RoomTypeOptionDAO;




/**
 * @author Tomas Muller
 */
public class RoomType extends BaseRoomType implements Comparable<RoomType> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RoomType () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public RoomType (Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static TreeSet<RoomType> findAll() {
	    return new TreeSet<RoomType>(RoomTypeDAO.getInstance().findAll());
	}

	public static TreeSet<RoomType> findAll(Long sessionId) {
		return new TreeSet<RoomType>(RoomTypeDAO.getInstance().getSession().createQuery(
				"select distinct t from Location l inner join l.roomType t where l.session.uniqueId = :sessionId")
				.setLong("sessionId", sessionId).setCacheable(true).list());
	}

	public static TreeSet<RoomType> findAll(boolean isRoom) {
        return new TreeSet<RoomType>(RoomTypeDAO.getInstance().getSession().createCriteria(RoomType.class).add(Restrictions.eq("room", isRoom)).setCacheable(true).list());
    }

    public static RoomType findByReference(String ref) {
        return (RoomType)RoomTypeDAO.getInstance().getSession().createCriteria(RoomType.class).add(Restrictions.eq("reference", ref)).setCacheable(true).uniqueResult();
    }
    
    public int compareTo(RoomType t) {
        int cmp = getOrd().compareTo(t.getOrd());
        if (cmp!=0) return cmp;
        cmp = getLabel().compareTo(t.getLabel());
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(t.getUniqueId() == null ? -1 : t.getUniqueId());
    }
    
    public RoomTypeOption getOption(Department department) {
    	if (department == null) {
    		RoomTypeOption opt = new RoomTypeOption(this, department);
    		opt.setStatus(RoomTypeOption.getDefaultStatus());
        	opt.setBreakTime(ApplicationProperty.RoomDefaultBreakTime.intValue(getReference()));
    		return opt;
    	}
        RoomTypeOption opt = (RoomTypeOption)RoomTypeOptionDAO.getInstance().getSession().createQuery(
    			"from RoomTypeOption where department.uniqueId = :departmentId and roomType.uniqueId = :roomTypeId")
    			.setLong("departmentId", department.getUniqueId())
    			.setLong("roomTypeId", getUniqueId())
    			.setCacheable(true)
    			.uniqueResult();
        if (opt==null) opt = new RoomTypeOption(this, department);
        if (opt.getStatus() == null) opt.setStatus(RoomTypeOption.getDefaultStatus());
        if (opt.getBreakTime() == null)
        	opt.setBreakTime(ApplicationProperty.RoomDefaultBreakTime.intValue(getReference()));
        return opt;
    }
    
    @Deprecated
    public boolean canScheduleEvents(Long sessionId) {
    	for (RoomTypeOption option: (List<RoomTypeOption>)RoomTypeDAO.getInstance().getSession().createQuery(
                "select distinct o from " + (isRoom() ? "Room" : "NonUniversityLocation") + " r, RoomTypeOption o " +
                "where r.roomType.uniqueId = :roomTypeId and r.session.uniqueId = :sessionId and "+
                "r.eventDepartment.allowEvents = true and r.eventDepartment = o.department and r.roomType = o.roomType")
                .setLong("roomTypeId", getUniqueId())
                .setLong("sessionId", sessionId)
    			.setCacheable(true).uniqueResult()) {
    		if (option.canScheduleEvents()) return true;
    	}
    	return false;

    }
    
    public int countRooms() {
        return ((Number)RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(distinct r.permanentId) from "+(isRoom()?"Room":"NonUniversityLocation")+" r where r.roomType.uniqueId=:roomTypeId"
        ).setLong("roomTypeId", getUniqueId()).setCacheable(true).uniqueResult()).intValue();
    }

    public int countRooms(Long sessionId) {
        return ((Number)RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(r) from "+(isRoom()?"Room":"NonUniversityLocation")+" r where r.roomType.uniqueId=:roomTypeId and r.session.uniqueId=:sessionId"
        ).setLong("roomTypeId", getUniqueId()).setLong("sessionId",sessionId).setCacheable(true).uniqueResult()).intValue();
    }

    public int countManagableRooms() {
        return ((Number)RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(distinct r.permanentId) from "+(isRoom()?"Room":"NonUniversityLocation")+" r " +
                "where r.roomType.uniqueId=:roomTypeId and "+
                "r.eventDepartment.allowEvents = true"
        ).setLong("roomTypeId", getUniqueId()).setCacheable(true).uniqueResult()).intValue();
    }
    
    public List<Location> getManagableRooms(Long sessionId) {
        return (List<Location>)RoomTypeDAO.getInstance().getSession().createQuery(
                "select r from "+(isRoom()?"Room":"NonUniversityLocation")+" r " +
                "where r.roomType.uniqueId=:roomTypeId and r.session.uniqueId=:sessionId and "+
                "r.eventDepartment.allowEvents = true"
        ).setLong("roomTypeId", getUniqueId()).setLong("sessionId",sessionId).setCacheable(true).uniqueResult();
    }


    public int countManagableRooms(Long sessionId) {
        return ((Number)RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(r) from "+(isRoom()?"Room":"NonUniversityLocation")+" r " +
                "where r.roomType.uniqueId=:roomTypeId and r.session.uniqueId=:sessionId and "+
                "r.eventDepartment.allowEvents = true"
        ).setLong("roomTypeId", getUniqueId()).setLong("sessionId",sessionId).setCacheable(true).uniqueResult()).intValue();
    }

    public int countManagableRoomsOfBuilding(Long buildingId) {
        return ((Number)RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(r) from Room r " +
                "where r.roomType.uniqueId=:roomTypeId and r.building.uniqueId=:buildingId and "+
                "r.eventDepartment.allowEvents = true"
        ).setLong("roomTypeId", getUniqueId()).setLong("buildingId",buildingId).setCacheable(true).uniqueResult()).intValue();
    }
}
