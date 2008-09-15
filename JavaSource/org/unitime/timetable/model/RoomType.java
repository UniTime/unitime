/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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

import java.util.TreeSet;

import org.hibernate.criterion.Restrictions;
import org.unitime.timetable.model.base.BaseRoomType;
import org.unitime.timetable.model.dao.RoomTypeDAO;
import org.unitime.timetable.model.dao.RoomTypeOptionDAO;




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

	/**
	 * Constructor for required fields
	 */
	public RoomType (
		Long uniqueId,
		java.lang.String reference) {

		super (
			uniqueId,
			reference);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static TreeSet<RoomType> findAll() {
	    return new TreeSet<RoomType>(RoomTypeDAO.getInstance().findAll());
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
        return getUniqueId().compareTo(t.getUniqueId());
    }
    
    public RoomTypeOption getOption(Session session) {
        RoomTypeOption opt = RoomTypeOptionDAO.getInstance().get(new RoomTypeOption(this, session)); 
        if (opt==null) opt = new RoomTypeOption(this, session);
        if (opt.getStatus()==null) opt.setStatus(RoomTypeOption.sStatusNoOptions);
        return opt;
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
                "inner join r.roomDepts rd inner join rd.department.timetableManagers m inner join m.managerRoles mr " +
                "where r.roomType.uniqueId=:roomTypeId and "+
                "rd.control=true and mr.role.reference=:eventMgr"
        ).setLong("roomTypeId", getUniqueId()).setString("eventMgr", Roles.EVENT_MGR_ROLE).setCacheable(true).uniqueResult()).intValue();
    }

    public int countManagableRooms(Long sessionId) {
        return ((Number)RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(r) from "+(isRoom()?"Room":"NonUniversityLocation")+" r " +
                "inner join r.roomDepts rd inner join rd.department.timetableManagers m inner join m.managerRoles mr " +
                "where r.roomType.uniqueId=:roomTypeId and r.session.uniqueId=:sessionId and "+
                "rd.control=true and mr.role.reference=:eventMgr"
        ).setLong("roomTypeId", getUniqueId()).setLong("sessionId",sessionId).setString("eventMgr", Roles.EVENT_MGR_ROLE).setCacheable(true).uniqueResult()).intValue();
    }

    public int countManagableRoomsOfBuilding(Long buildingId) {
        return ((Number)RoomTypeDAO.getInstance().getSession().createQuery(
                "select count(r) from Room r " +
                "inner join r.roomDepts rd inner join rd.department.timetableManagers m inner join m.managerRoles mr " +
                "where r.roomType.uniqueId=:roomTypeId and r.building.uniqueId=:buildingId and "+
                "rd.control=true and mr.role.reference=:eventMgr"
        ).setLong("roomTypeId", getUniqueId()).setLong("buildingId",buildingId).setString("eventMgr", Roles.EVENT_MGR_ROLE).setCacheable(true).uniqueResult()).intValue();
    }
}
