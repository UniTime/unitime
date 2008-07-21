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
package org.unitime.timetable.form;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;

public class EventGridForm extends EventAddForm {
    private String iTable = null;
    
    public void setTable(String table) { iTable = table; }
    public String getTable() { return iTable; }

    public Collection<RoomType> getAllRoomTypes() {
        Collection<RoomType> ret = RoomType.findAll();
        if (getSessionId()!=null) {
            for (Iterator<RoomType> i=ret.iterator(); i.hasNext();) {
                RoomType t = (RoomType)i.next();
                if (t.countManagableRooms(getSessionId())<=0) {i.remove(); continue; }
            }
        }
        return ret;
    }
    
    public boolean isHasOutsideLocations() {
        if (getSessionId()==null) return false;
        Session session = new SessionDAO().get(getSessionId());
        boolean hasRoomType = false;
        for (RoomType roomType : RoomType.findAll(false)) {
            if (roomType.countManagableRooms(getSessionId())>0) return true;
        }
        return false;
    }
    
    public List getBuildings() {
        if (getSessionId()==null) return null;
        List ret = Building.findAll(getSessionId());
        buildings: for (Iterator i=ret.iterator();i.hasNext();) {
            Building b = (Building)i.next();
            for (RoomType roomType : RoomType.findAll(true)) {
                if (roomType.countManagableRoomsOfBuilding(b.getUniqueId())>0) continue buildings;
            }
            i.remove();
        }
        return ret;
    }


}
