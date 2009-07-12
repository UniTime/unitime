/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2009, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;

import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.LocationDAO;

/**
 * @author Tomas Muller
 */
public class ClassRoomInfo implements Serializable, Comparable<ClassRoomInfo>{
    private Long iId = null;
    private String iName = null;
    private int iPreference = 0;
    private int iCapacity;
    private int iX = -1, iY = -1;
    private boolean iIgnoreTooFar;
    private transient Location iLocation = null;
    
    public ClassRoomInfo(Location location, int preference) {
        iLocation = location;
        iId = location.getUniqueId();
        iName = location.getLabel();
        iCapacity = location.getCapacity();
        iPreference = preference;
        iX = (location.getCoordinateX()==null?-1:location.getCoordinateX().intValue());
        iY = (location.getCoordinateY()==null?-1:location.getCoordinateY().intValue());
        iIgnoreTooFar = location.isIgnoreTooFar().booleanValue();
    }
    
    public Long getLocationId() { return iId; }
    public String getName() { return iName; }
    public int getPreference() { return iPreference; }
    public void setPreference(int preference) { iPreference = preference; }
    public int getCapacity() { return iCapacity; }
    public boolean isIgnoreTooFar() { return iIgnoreTooFar; }
    public Location getLocation() {
        if (iLocation==null) iLocation = new LocationDAO().get(getLocationId());
        return iLocation;
    }
    public Location getLocation(org.hibernate.Session hibSession) {
        return new LocationDAO().get(getLocationId(), hibSession);
    }
    
    
    public String toString() {
    	int pref = getPreference();
    	boolean s = false;
    	if (pref>5000) {
    		s=true;
    		pref-=5000;
    	}
        return "<span style='color:"+PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(pref))+";' " +
        		"title='"+PreferenceLevel.prolog2string(PreferenceLevel.int2prolog(pref))+" "+getName()+
        		" ("+getCapacity()+" seats)'>"+
        		(s?"<s>":"")+
        		getName()+
        		(s?"</s>":"")+
        		"</span>";
    }
    
    public int compareTo(ClassRoomInfo room) {
        int cmp = -Double.compare(getCapacity(), room.getCapacity());
        if (cmp!=0) return cmp;
        cmp = getName().compareTo(room.getName());
        if (cmp!=0) return cmp;
        return getLocationId().compareTo(room.getLocationId());
    }
    
    public boolean equals(Object o) {
        if (o==null || !(o instanceof ClassRoomInfo)) return false;
        return getLocationId().equals(((ClassRoomInfo)o).getLocationId());
    }
    
    public int hashCode() {
        return getLocationId().hashCode();
    }
    
    public int getCoordX() { return iX; }
    public int getCoordY() { return iY; }
    
    public int getDistance(ClassRoomInfo other) {
    	if (isIgnoreTooFar() || other.isIgnoreTooFar()) return 0;
        if (getCoordX()<0 || getCoordY()<0 || other.getCoordX()<0 || other.getCoordY()<0) return 10000;
        int dx = getCoordX()-other.getCoordX();
        int dy = getCoordY()-other.getCoordY();
        return (int)Math.sqrt(dx*dx+dy*dy);
    }
    
    public String getNameHtml() {
        return
            "<span title='"+PreferenceLevel.int2string(getPreference())+" "+getName()+
            " ("+getCapacity()+" seats)' style='color:"+
            PreferenceLevel.int2color(getPreference())+";'>"+
            getName()+
            "</span>";
    }

}
