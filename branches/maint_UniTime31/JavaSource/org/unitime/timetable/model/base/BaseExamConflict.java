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

public abstract class BaseExamConflict implements Serializable {

    public static String REF = "ExamConflict";


	// constructors
	public BaseExamConflict() {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExamConflict (java.lang.Long uniqueId) {
        this.setUniqueId(uniqueId);
        initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExamConflict (
		java.lang.Long uniqueId,
		java.lang.Integer conflictType) {

	    this.setUniqueId(uniqueId);
        this.setConflictType(conflictType);
        initialize();
	}

    protected void initialize () {}

	private int hashCode = Integer.MIN_VALUE;

    // primary key
    private java.lang.Long uniqueId;

    // fields
	private java.lang.Integer conflictType;
	private java.lang.Integer nrStudents;
	private java.lang.Integer nrInstructors;
	private java.lang.Double distance;
	
	// sets
	private java.util.Set exams;
	private java.util.Set students;
	private java.util.Set instructors;

    public java.lang.Long getUniqueId () {
        return uniqueId;
    }

    public void setUniqueId (java.lang.Long uniqueId) {
        this.uniqueId = uniqueId;
        this.hashCode = Integer.MIN_VALUE;
    }

    public void setConflictType(java.lang.Integer conflictType) {
        this.conflictType = conflictType;
    }
    
    public java.lang.Integer getConflictType() {
        return conflictType;
    }
    
    public void setNrStudents(java.lang.Integer nrStudents) {
        this.nrStudents = nrStudents;
    }
    
    public java.lang.Integer getNrStudents() {
        return nrStudents;
    }
    
    public void setNrInstructors(java.lang.Integer nrInstructors) {
        this.nrInstructors = nrInstructors;
    }
    
    public java.lang.Integer getNrInstructors() {
        return nrInstructors;
    }

    public void setDistance(java.lang.Double distance) {
        this.distance = distance;
    }
    
    public java.lang.Double getDistance() {
        return distance;
    }
    
    public void setExams(java.util.Set exams) {
        this.exams = exams;
    }
    
    public java.util.Set getExams() {
        return exams;
    }
    
    public void setStudents(java.util.Set students) {
        this.students = students;
    }
    
    public java.util.Set getStudents() {
        return students;
    }
    
    public void setInstructors(java.util.Set instructors) {
        this.instructors = instructors;
    }
    
    public java.util.Set getInstructors() {
        return instructors;
    }

	public boolean equals (Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof org.unitime.timetable.model.ExamConflict)) return false;
        else {
            org.unitime.timetable.model.ExamConflict examConflict = (org.unitime.timetable.model.ExamConflict) obj;
            if (null == this.getUniqueId() || null == examConflict.getUniqueId()) return false;
            else return (this.getUniqueId().equals(examConflict.getUniqueId()));
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
