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
}