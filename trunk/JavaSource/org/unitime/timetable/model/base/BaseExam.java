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


/**
 * This is an object that contains data related to the EXAM table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="EXAM"
 */

public abstract class BaseExam extends org.unitime.timetable.model.PreferenceGroup  implements Serializable {

	public static String REF = "Exam";
	public static String PROP_NAME = "name";
	public static String PROP_NOTE = "note";
	public static String PROP_LENGTH = "length";
	public static String PROP_MAX_NBR_ROOMS = "maxNbrRooms";
	public static String PROP_SEATING_TYPE = "seatingType";
	public static String PROP_ASSIGNED_PREFERENCE = "assignedPreference";
	public static String PROP_EXAM_TYPE = "examType";
	public static String PROP_AVG_PERIOD = "avgPeriod";
	public static String PROP_UNIQUE_ID_ROLLED_FORWARD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_EXAM_SIZE = "examSize";
	public static String PROP_PRINT_OFFSET = "printOffset";


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
	private java.lang.Integer examSize;
	private java.lang.Integer printOffset;
	private java.lang.Integer maxNbrRooms;
	private java.lang.Integer seatingType;
	private java.lang.String assignedPreference;
	private java.lang.Integer examType;
	private java.lang.Integer avgPeriod;
	private java.lang.Long uniqueIdRolledForwardFrom;

	// many to one
	private org.unitime.timetable.model.Session session;
	private org.unitime.timetable.model.ExamPeriod assignedPeriod;

	// collections
	private java.util.Set owners;
	private java.util.Set assignedRooms;
	private java.util.Set instructors;
	private java.util.Set conflicts;






	/**
	 * Return the value associated with the column: NAME
	 */
	public java.lang.String getName () {
		return name;
	}

	/**
	 * Set the value related to the column: NAME
	 * @param name the NAME value
	 */
	public void setName (java.lang.String name) {
		this.name = name;
	}



	/**
	 * Return the value associated with the column: NOTE
	 */
	public java.lang.String getNote () {
		return note;
	}

	/**
	 * Set the value related to the column: NOTE
	 * @param note the NOTE value
	 */
	public void setNote (java.lang.String note) {
		this.note = note;
	}



	/**
	 * Return the value associated with the column: LENGTH
	 */
	public java.lang.Integer getLength () {
		return length;
	}

	/**
	 * Set the value related to the column: LENGTH
	 * @param length the LENGTH value
	 */
	public void setLength (java.lang.Integer length) {
		this.length = length;
	}

    /**
     * Return the value associated with the column: EXAM_SIZE
     */
    public java.lang.Integer getExamSize () {
        return examSize;
    }

    /**
     * Set the value related to the column: EXAM_SIZE
     * @param length the EXAM_SIZE value
     */
    public void setExamSize (java.lang.Integer examSize) {
        this.examSize = examSize;
    }

    /**
     * Return the value associated with the column: PRINT_OFFSET
     */
    public java.lang.Integer getPrintOffset () {
        return printOffset;
    }

    /**
     * Set the value related to the column: PRINT_OFFSET
     * @param length the PRINT_OFFSET value
     */
    public void setPrintOffset (java.lang.Integer printOffset) {
        this.printOffset = printOffset;
    }
    
    
	/**
	 * Return the value associated with the column: MAX_NBR_ROOMS
	 */
	public java.lang.Integer getMaxNbrRooms () {
		return maxNbrRooms;
	}

	/**
	 * Set the value related to the column: MAX_NBR_ROOMS
	 * @param maxNbrRooms the MAX_NBR_ROOMS value
	 */
	public void setMaxNbrRooms (java.lang.Integer maxNbrRooms) {
		this.maxNbrRooms = maxNbrRooms;
	}



	/**
	 * Return the value associated with the column: SEATING_TYPE
	 */
	public java.lang.Integer getSeatingType () {
		return seatingType;
	}

	/**
	 * Set the value related to the column: SEATING_TYPE
	 * @param seatingType the SEATING_TYPE value
	 */
	public void setSeatingType (java.lang.Integer seatingType) {
		this.seatingType = seatingType;
	}



	/**
	 * Return the value associated with the column: ASSIGNED_PREF
	 */
	public java.lang.String getAssignedPreference () {
		return assignedPreference;
	}

	/**
	 * Set the value related to the column: ASSIGNED_PREF
	 * @param assignedPreference the ASSIGNED_PREF value
	 */
	public void setAssignedPreference (java.lang.String assignedPreference) {
		this.assignedPreference = assignedPreference;
	}



	/**
	 * Return the value associated with the column: EXAM_TYPE
	 */
	public java.lang.Integer getExamType () {
		return examType;
	}

	/**
	 * Set the value related to the column: EXAM_TYPE
	 * @param examType the EXAM_TYPE value
	 */
	public void setExamType (java.lang.Integer examType) {
		this.examType = examType;
	}



	/**
	 * Return the value associated with the column: AVG_PERIOD
	 */
	public java.lang.Integer getAvgPeriod () {
		return avgPeriod;
	}

	/**
	 * Set the value related to the column: AVG_PERIOD
	 * @param avgPeriod the AVG_PERIOD value
	 */
	public void setAvgPeriod (java.lang.Integer avgPeriod) {
		this.avgPeriod = avgPeriod;
	}



	/**
	 * Return the value associated with the column: UID_ROLLED_FWD_FROM
	 */
	public java.lang.Long getUniqueIdRolledForwardFrom () {
		return uniqueIdRolledForwardFrom;
	}

	/**
	 * Set the value related to the column: UID_ROLLED_FWD_FROM
	 * @param uniqueIdRolledForwardFrom the UID_ROLLED_FWD_FROM value
	 */
	public void setUniqueIdRolledForwardFrom (java.lang.Long uniqueIdRolledForwardFrom) {
		this.uniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom;
	}



	/**
	 * Return the value associated with the column: SESSION_ID
	 */
	public org.unitime.timetable.model.Session getSession () {
		return session;
	}

	/**
	 * Set the value related to the column: SESSION_ID
	 * @param session the SESSION_ID value
	 */
	public void setSession (org.unitime.timetable.model.Session session) {
		this.session = session;
	}



	/**
	 * Return the value associated with the column: ASSIGNED_PERIOD
	 */
	public org.unitime.timetable.model.ExamPeriod getAssignedPeriod () {
		return assignedPeriod;
	}

	/**
	 * Set the value related to the column: ASSIGNED_PERIOD
	 * @param assignedPeriod the ASSIGNED_PERIOD value
	 */
	public void setAssignedPeriod (org.unitime.timetable.model.ExamPeriod assignedPeriod) {
		this.assignedPeriod = assignedPeriod;
	}


	/**
	 * Return the value associated with the column: owners
	 */
	public java.util.Set getOwners () {
		return owners;
	}

	/**
	 * Set the value related to the column: owners
	 * @param owners the owners value
	 */
	public void setOwners (java.util.Set owners) {
		this.owners = owners;
	}

	public void addToowners (org.unitime.timetable.model.ExamOwner examOwner) {
		if (null == getOwners()) setOwners(new java.util.HashSet());
		getOwners().add(examOwner);
	}



	/**
	 * Return the value associated with the column: assignedRooms
	 */
	public java.util.Set getAssignedRooms () {
		return assignedRooms;
	}

	/**
	 * Set the value related to the column: assignedRooms
	 * @param assignedRooms the assignedRooms value
	 */
	public void setAssignedRooms (java.util.Set assignedRooms) {
		this.assignedRooms = assignedRooms;
	}



	/**
	 * Return the value associated with the column: instructors
	 */
	public java.util.Set getInstructors () {
		return instructors;
	}

	/**
	 * Set the value related to the column: instructors
	 * @param instructors the instructors value
	 */
	public void setInstructors (java.util.Set instructors) {
		this.instructors = instructors;
	}



	/**
	 * Return the value associated with the column: conflicts
	 */
	public java.util.Set getConflicts () {
		return conflicts;
	}

	/**
	 * Set the value related to the column: conflicts
	 * @param conflicts the conflicts value
	 */
	public void setConflicts (java.util.Set conflicts) {
		this.conflicts = conflicts;
	}





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
