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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;


import org.cpsolver.ifs.util.DistanceMetric;
import org.unitime.timetable.defaults.ApplicationProperty;
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
        		"onmouseover=\"showGwtRoomHint(this, '" + iId + "', '" + PreferenceLevel.prolog2string(PreferenceLevel.int2prolog(pref)) + "'" + (hasNote() ? ", null, '" + getNote().replace("'", "\\'") + "'": "") + ");\" onmouseout=\"hideGwtRoomHint();\">"+
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
    		sDistanceMetric = new DistanceMetric(DistanceMetric.Ellipsoid.valueOf(ApplicationProperty.DistanceEllipsoid.value()));
    	}
    	return sDistanceMetric.getDistanceInMeters(getLocationId(), getCoordX(), getCoordY(), other.getLocationId(), other.getCoordX(), other.getCoordY());
    }
    
    public String getNameHtml() {
        return
        	"<span onmouseover=\"showGwtRoomHint(this, '" + iId + "', '" + PreferenceLevel.int2string(getPreference()) + "'" + ");\" onmouseout=\"hideGwtRoomHint();\">" +
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
