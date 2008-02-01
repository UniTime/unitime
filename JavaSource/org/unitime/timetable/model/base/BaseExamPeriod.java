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
package org.unitime.timetable.model.base;

import java.io.Serializable;

public abstract class BaseExamPeriod implements Serializable {

    public static String REF = "ExamPeriod";


	// constructors
	public BaseExamPeriod() {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExamPeriod (java.lang.Long uniqueId) {
        this.setUniqueId(uniqueId);
        initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExamPeriod (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.Integer dateOffset,
		java.lang.Integer startSlot,
		java.lang.Integer length) {

	    this.setUniqueId(uniqueId);
        this.setSession(session);
        this.setDateOffset(dateOffset);
        this.setStartSlot(startSlot);
        this.setLength(length);
        initialize();
	}

    protected void initialize () {}

	private int hashCode = Integer.MIN_VALUE;

    // primary key
    private java.lang.Long uniqueId;

    // fields
	private org.unitime.timetable.model.Session session;
	private java.lang.Integer dateOffset;
	private java.lang.Integer startSlot;
	private java.lang.Integer length;

    public java.lang.Long getUniqueId () {
        return uniqueId;
    }

    public void setUniqueId (java.lang.Long uniqueId) {
        this.uniqueId = uniqueId;
        this.hashCode = Integer.MIN_VALUE;
    }

    public void setSession(org.unitime.timetable.model.Session session) {
	    this.session = session;
	}
	
	public org.unitime.timetable.model.Session getSession() {
	    return session;
	}
	
	public void setDateOffset(java.lang.Integer dateOffset) {
	    this.dateOffset = dateOffset;
	}
	
	public java.lang.Integer getDateOffset() {
	    return dateOffset;
	}

	public void setStartSlot(java.lang.Integer startSlot) {
	    this.startSlot = startSlot;
	}
	
	public java.lang.Integer getStartSlot() {
	    return startSlot;
	}
	
	public void setLength(java.lang.Integer length) {
	    this.length = length;
	}
	
	public java.lang.Integer getLength() {
	    return length;
	}

    public boolean equals (Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof org.unitime.timetable.model.ExamPeriod)) return false;
        else {
            org.unitime.timetable.model.ExamPeriod examPeriod = (org.unitime.timetable.model.ExamPeriod) obj;
            if (null == this.getUniqueId() || null == examPeriod.getUniqueId()) return false;
            else return (this.getUniqueId().equals(examPeriod.getUniqueId()));
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