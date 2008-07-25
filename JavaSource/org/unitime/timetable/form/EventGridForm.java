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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;

public class EventGridForm extends EventAddForm {
    private String iMode = null;
    public static String sModeAll = "All Events";
    public static String sModeApproved = "All Approved Events";
    public static String sModeWaiting = "All Events Waiting Approval";
    public static String[] sModes = new String[] {
        sModeAll,
        sModeApproved,
        sModeWaiting
    };
    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        iMode = sModeAll;
    }

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

    public String getMode() { return iMode; }
    public void setMode(String mode) { iMode = mode; }
    public String[] getModes() {
        return sModes;
    }

    public void save (HttpSession session) {
        session.setAttribute("EventGrid.EventType", getEventType());
        session.setAttribute("EventGrid.SessionId", getSessionId());        
        session.setAttribute("EventGrid.StartTime", getStartTime());
        session.setAttribute("EventGrid.StopTime", getStopTime());
        session.setAttribute("EventGrid.MeetingDates", getMeetingDates());
        session.setAttribute("EventGrid.MinCapacity", getMinCapacity());
        session.setAttribute("EventGrid.MaxCapacity", getMaxCapacity());
        session.setAttribute("EventGrid.BuildingId", getBuildingId());
        session.setAttribute("EventGrid.RoomNumber", getRoomNumber());
        session.setAttribute("EventGrid.LookAtNearLocations", getLookAtNearLocations());
        session.setAttribute("EventGrid.IsAddMeetings", getIsAddMeetings());
        session.setAttribute("EventGrid.RoomTypes", getRoomTypes());
        session.setAttribute("EventGrid.RoomGroups", getRoomGroups());
        session.setAttribute("EventGrid.RoomFeatures", getRoomFeatures());
        session.setAttribute("EventGrid.Mode", getMode());
    }
    
    public void load (HttpSession session) {
        setEventType((String) session.getAttribute("EventGrid.EventType"));
        setSessionId((Long) session.getAttribute("EventGrid.SessionId"));        
        setStartTime((Integer) session.getAttribute("EventGrid.StartTime"));
        setStopTime((Integer) session.getAttribute("EventGrid.StopTime"));
        setMeetingDates((TreeSet<Date>) session.getAttribute("EventGrid.MeetingDates"));
        setMinCapacity((String) session.getAttribute("EventGrid.MinCapacity"));
        setMaxCapacity((String) session.getAttribute("EventGrid.MaxCapacity"));
        setBuildingId((Long) session.getAttribute("EventGrid.BuildingId"));
        setRoomNumber((String) session.getAttribute("EventGrid.RoomNumber"));
        setLookAtNearLocations((Boolean) session.getAttribute("EventGrid.LookAtNearLocations"));
        setIsAddMeetings((Boolean)session.getAttribute("EventGrid.IsAddMeetings"));
        setRoomTypes((Long[]) session.getAttribute("EventGrid.RoomTypes"));
        setRoomGroups((Long[]) session.getAttribute("EventGrid.RoomGroups"));
        setRoomFeatures((Long[]) session.getAttribute("EventGrid.RoomFeatures"));
        setMode((String)session.getAttribute("EventGrid.Mode"));
    }
    
    public String getDatesTable() {
        return getDatesTable(false);
    }
}
