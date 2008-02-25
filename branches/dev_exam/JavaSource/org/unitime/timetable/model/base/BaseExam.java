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

public abstract class BaseExam extends org.unitime.timetable.model.PreferenceGroup implements Serializable {

	public static String REF = "Exam";

	// constructors
	public BaseExam () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExam (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// fields
	private java.lang.String name;
	private java.lang.String note;
	private java.lang.Integer length;
	private java.lang.Integer maxNbrRooms;
	private java.lang.Integer seatingType;

	// many to one
	private org.unitime.timetable.model.Session session;
	private org.unitime.timetable.model.ExamPeriod assignedPeriod;

    // collections
    private java.util.Set owners;
    private java.util.Set assignedRooms;
    private java.util.Set instructors;
    private java.util.Set conflicts;

	public java.lang.String getName() { return name; }
	public void setName(java.lang.String name) { this.name = name; }
	
	public java.lang.String getNote() { return note; }
	public void setNote(java.lang.String note) { this.note = note; }
	
	public java.lang.Integer getLength() { return length; }
	public void setLength(java.lang.Integer length) { this.length = length; }
	
	public java.lang.Integer getMaxNbrRooms() { return maxNbrRooms; }
	public void setMaxNbrRooms(java.lang.Integer maxNbrRooms) { this.maxNbrRooms = maxNbrRooms; }
	
	public java.lang.Integer getSeatingType() { return seatingType; }
	public void setSeatingType(java.lang.Integer seatingType) { this.seatingType = seatingType; }
	
	public org.unitime.timetable.model.Session getSession() { return session; }
	public void setSession(org.unitime.timetable.model.Session session) { this.session = session; }
	
	public org.unitime.timetable.model.ExamPeriod getAssignedPeriod() { return assignedPeriod; }
	public void setAssignedPeriod(org.unitime.timetable.model.ExamPeriod assignedPeriod) { this.assignedPeriod = assignedPeriod; }

	public java.util.Set getOwners() { return owners; }
	public void setOwners(java.util.Set owners) { this.owners = owners; }
	
	public java.util.Set getAssignedRooms() { return assignedRooms; }
	public void setAssignedRooms(java.util.Set assignedRooms) { this.assignedRooms = assignedRooms; }
	
	public java.util.Set getInstructors() { return instructors; }
	public void setInstructors(java.util.Set instructors) { this.instructors = instructors; }
	
	public java.util.Set getConflicts() { return conflicts; }
	public void setConflicts(java.util.Set conflicts) { this.conflicts = conflicts; }

	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Exam)) return false;
		else {
			org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam) obj;
			if (null == this.getUniqueId() || null == exam.getUniqueId()) return false;
			else return (this.getUniqueId().equals(exam.getUniqueId()));
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