/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2009 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;


import org.cpsolver.ifs.util.DistanceMetric;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.LocationDAO;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class ClassRoomInfo implements Serializable, Comparable<ClassRoomInfo>{
    /**
	 * 
	 */
	private static final long serialVersionUID = 210491037628622512L;
	private Long iId = null;
    private String iName = null;
    private int iPreference = 0;
    private int iCapacity;
    private Double iX = null, iY = null;
    private boolean iIgnoreTooFar;
    private transient Location iLocation = null;
    private String iNote = null;
    private boolean iIgnoreRoomChecks;
    private String iRoomType = null;
    private transient static DistanceMetric sDistanceMetric = null;
    
    public ClassRoomInfo(Location location, int preference) {
        iLocation = location;
        iId = location.getUniqueId();
        iName = location.getLabel();
        iCapacity = location.getCapacity();
        iPreference = preference;
        iX = location.getCoordinateX();
        iY = location.getCoordinateY();
        iIgnoreTooFar = location.isIgnoreTooFar().booleanValue();
        iIgnoreRoomChecks = location.isIgnoreRoomCheck();
        iRoomType = location.getRoomTypeLabel();
    }
    
    public ClassRoomInfo(Location location, int preference, String note) {
    	this(location, preference);
    	iNote = note;
    }
    
    public Long getLocationId() { return iId; }
    public String getName() { return iName; }
    public int getPreference() { return iPreference; }
    public void setPreference(int preference) { iPreference = preference; }
    public int getCapacity() { return iCapacity; }
    public boolean isIgnoreTooFar() { return iIgnoreTooFar; }
    public boolean hasNote() { return iNote != null; }
    public String getNote() { return iNote; }
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
        		" ("+(hasNote()?getNote()+", ":"")+getCapacity()+" seats, " + iRoomType + ")'>"+
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
    
    public Double getCoordX() { return iX; }
    public Double getCoordY() { return iY; }
    
    public double getDistance(ClassRoomInfo other) {
    	if (isIgnoreTooFar() || other.isIgnoreTooFar()) return 0;
    	if (sDistanceMetric == null) {
    		sDistanceMetric = new DistanceMetric(
    				DistanceMetric.Ellipsoid.valueOf(ApplicationProperties.getProperty("unitime.distance.ellipsoid", DistanceMetric.Ellipsoid.LEGACY.name())));
    	}
    	return sDistanceMetric.getDistanceInMeters(getLocationId(), getCoordX(), getCoordY(), other.getLocationId(), other.getCoordX(), other.getCoordY());
    }
    
    public String getNameHtml() {
        return
            "<span title='"+PreferenceLevel.int2string(getPreference())+" "+getName()+
            " ("+getCapacity()+" seats)' style='color:"+
            PreferenceLevel.int2color(getPreference())+";'>"+
            getName()+
            "</span>";
    }

	public boolean isIgnoreRoomChecks() {
		return iIgnoreRoomChecks;
	}

	public void setIgnoreRoomChecks(boolean ignoreRoomChecks) {
		iIgnoreRoomChecks = ignoreRoomChecks;
	}

}
