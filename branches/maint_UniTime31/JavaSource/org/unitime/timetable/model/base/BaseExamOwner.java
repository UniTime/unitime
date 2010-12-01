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

public abstract class BaseExamOwner implements Serializable {

    public static String REF = "ExamOwner";


	// constructors
	public BaseExamOwner() {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExamOwner (java.lang.Long uniqueId) {
        this.setUniqueId(uniqueId);
        initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExamOwner (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Exam exam,
		java.lang.Long ownerId,
		java.lang.Integer ownerType,
		org.unitime.timetable.model.CourseOffering course) {

	    this.setUniqueId(uniqueId);
        this.setExam(exam);
        this.setOwnerId(ownerId);
        this.setOwnerType(ownerType);
        this.setCourse(course);
        initialize();
	}

    protected void initialize () {}

	private int hashCode = Integer.MIN_VALUE;

    // primary key
    private java.lang.Long uniqueId;

    // fields
	private org.unitime.timetable.model.Exam exam;
	private java.lang.Long ownerId;
	private java.lang.Integer ownerType;
	private org.unitime.timetable.model.CourseOffering course;

    public java.lang.Long getUniqueId () {
        return uniqueId;
    }

    public void setUniqueId (java.lang.Long uniqueId) {
        this.uniqueId = uniqueId;
        this.hashCode = Integer.MIN_VALUE;
    }

    public void setExam(org.unitime.timetable.model.Exam exam) {
	    this.exam = exam;
	}
	
	public org.unitime.timetable.model.Exam getExam() {
	    return exam;
	}
	
	public void setOwnerId(java.lang.Long ownerId) {
	    this.ownerId = ownerId;
	}
	
	public java.lang.Long getOwnerId() {
	    return ownerId;
	}

	public void setOwnerType(java.lang.Integer ownerType) {
	    this.ownerType = ownerType;
	}
	
	public java.lang.Integer getOwnerType() {
	    return ownerType;
	}
	
	public org.unitime.timetable.model.CourseOffering getCourse() {
	    return course;
	}
	
	public void setCourse(org.unitime.timetable.model.CourseOffering course) {
	    this.course = course;
	}

	public boolean equals (Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof org.unitime.timetable.model.ExamOwner)) return false;
        else {
            org.unitime.timetable.model.ExamOwner examOwner = (org.unitime.timetable.model.ExamOwner) obj;
            if (null == this.getUniqueId() || null == examOwner.getUniqueId()) return false;
            else return (this.getUniqueId().equals(examOwner.getUniqueId()));
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
