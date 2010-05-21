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
package org.unitime.timetable.model.base;

import java.io.Serializable;

public abstract class BaseExamLocationPref implements Serializable {

    public static String REF = "ExamLocationPref";


	// constructors
	public BaseExamLocationPref() {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExamLocationPref (java.lang.Long uniqueId) {
        this.setUniqueId(uniqueId);
        initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExamLocationPref (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Location location,
		org.unitime.timetable.model.PreferenceLevel prefLevel,
		org.unitime.timetable.model.ExamPeriod period) {

	    this.setUniqueId(uniqueId);
        this.setLocation(location);
        this.setPrefLevel(prefLevel);
        this.setExamPeriod(period);
        initialize();
	}

    protected void initialize () {}

	private int hashCode = Integer.MIN_VALUE;

    // primary key
    private java.lang.Long uniqueId;

    // fields
	private org.unitime.timetable.model.Location location;
	private org.unitime.timetable.model.PreferenceLevel prefLevel;
	private org.unitime.timetable.model.ExamPeriod examPeriod;


    public java.lang.Long getUniqueId () {
        return uniqueId;
    }

    public void setUniqueId (java.lang.Long uniqueId) {
        this.uniqueId = uniqueId;
        this.hashCode = Integer.MIN_VALUE;
    }

    public void setLocation(org.unitime.timetable.model.Location location) { 
        this.location = location;
	}
	
	public org.unitime.timetable.model.Location getLocation() {
	    return location;
	}
	
    public void setPrefLevel(org.unitime.timetable.model.PreferenceLevel prefLevel) { 
        this.prefLevel = prefLevel;
    }
    
    public org.unitime.timetable.model.PreferenceLevel getPrefLevel() {
        return prefLevel;
    }

    public void setExamPeriod(org.unitime.timetable.model.ExamPeriod examPeriod) { 
        this.examPeriod = examPeriod;
    }
    
    public org.unitime.timetable.model.ExamPeriod getExamPeriod() {
        return examPeriod;
    }

    public boolean equals (Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof org.unitime.timetable.model.ExamLocationPref)) return false;
        else {
            org.unitime.timetable.model.ExamLocationPref examLocationPref = (org.unitime.timetable.model.ExamLocationPref) obj;
            if (null == this.getUniqueId() || null == examLocationPref.getUniqueId()) return false;
            else return (this.getUniqueId().equals(examLocationPref.getUniqueId()));
        }
    }

    public int hashCode () {
        if (Integer.MIN_VALUE == this.hashCode) {
            if (null == this.getUniqueId()) return super.hashCode();
            else {
                String hashStr = this.getClass().getName() + ":" + this.getUniqueId().hashCode();
                this.hashCode = hashStr.hashCode();
            }
        }
        return this.hashCode;
    }


	public String toString () {
		return super.toString();
	}


}
