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


/**
 * This is an object that contains data related to the ASSIGNMENT table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="ASSIGNMENT"
 */

public abstract class BaseAssignment  implements Serializable {

	public static String REF = "Assignment";
	public static String PROP_DAYS = "days";
	public static String PROP_START_SLOT = "startSlot";
	public static String PROP_CLASS_ID = "classId";
	public static String PROP_CLASS_NAME = "className";


	// constructors
	public BaseAssignment () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseAssignment (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseAssignment (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.TimePattern timePattern,
		org.unitime.timetable.model.Solution solution,
		org.unitime.timetable.model.Class_ clazz) {

		this.setUniqueId(uniqueId);
		this.setTimePattern(timePattern);
		this.setSolution(solution);
		this.setClazz(clazz);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer days;
	private java.lang.Integer startSlot;
	private java.lang.Long classId;
	private java.lang.String className;

	// many to one
	private org.unitime.timetable.model.TimePattern timePattern;
	private org.unitime.timetable.model.Solution solution;
	private org.unitime.timetable.model.Class_ clazz;

	// collections
	private java.util.Set instructors;
	private java.util.Set rooms;
	private java.util.Set assignmentInfo;
	private java.util.Set constraintInfo;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="sequence"
     *  column="UNIQUEID"
     */
	public java.lang.Long getUniqueId () {
		return uniqueId;
	}

	/**
	 * Set the unique identifier of this class
	 * @param uniqueId the new ID
	 */
	public void setUniqueId (java.lang.Long uniqueId) {
		this.uniqueId = uniqueId;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: DAYS
	 */
	public java.lang.Integer getDays () {
		return days;
	}

	/**
	 * Set the value related to the column: DAYS
	 * @param days the DAYS value
	 */
	public void setDays (java.lang.Integer days) {
		this.days = days;
	}



	/**
	 * Return the value associated with the column: SLOT
	 */
	public java.lang.Integer getStartSlot () {
		return startSlot;
	}

	/**
	 * Set the value related to the column: SLOT
	 * @param startSlot the SLOT value
	 */
	public void setStartSlot (java.lang.Integer startSlot) {
		this.startSlot = startSlot;
	}



	/**
	 * Return the value associated with the column: CLASS_ID
	 */
	public java.lang.Long getClassId () {
		return classId;
	}

	/**
	 * Set the value related to the column: CLASS_ID
	 * @param classId the CLASS_ID value
	 */
	public void setClassId (java.lang.Long classId) {
		this.classId = classId;
	}



	/**
	 * Return the value associated with the column: CLASS_NAME
	 */
	public java.lang.String getClassName () {
		return className;
	}

	/**
	 * Set the value related to the column: CLASS_NAME
	 * @param className the CLASS_NAME value
	 */
	public void setClassName (java.lang.String className) {
		this.className = className;
	}



	/**
	 * Return the value associated with the column: TIME_PATTERN_ID
	 */
	public org.unitime.timetable.model.TimePattern getTimePattern () {
		return timePattern;
	}

	/**
	 * Set the value related to the column: TIME_PATTERN_ID
	 * @param timePattern the TIME_PATTERN_ID value
	 */
	public void setTimePattern (org.unitime.timetable.model.TimePattern timePattern) {
		this.timePattern = timePattern;
	}



	/**
	 * Return the value associated with the column: SOLUTION_ID
	 */
	public org.unitime.timetable.model.Solution getSolution () {
		return solution;
	}

	/**
	 * Set the value related to the column: SOLUTION_ID
	 * @param solution the SOLUTION_ID value
	 */
	public void setSolution (org.unitime.timetable.model.Solution solution) {
		this.solution = solution;
	}



	/**
	 * Return the value associated with the column: CLASS_ID
	 */
	public org.unitime.timetable.model.Class_ getClazz () {
		return clazz;
	}

	/**
	 * Set the value related to the column: CLASS_ID
	 * @param clazz the CLASS_ID value
	 */
	public void setClazz (org.unitime.timetable.model.Class_ clazz) {
		this.clazz = clazz;
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
	 * Return the value associated with the column: rooms
	 */
	public java.util.Set getRooms () {
		return rooms;
	}

	/**
	 * Set the value related to the column: rooms
	 * @param rooms the rooms value
	 */
	public void setRooms (java.util.Set rooms) {
		this.rooms = rooms;
	}



	/**
	 * Return the value associated with the column: assignmentInfo
	 */
	public java.util.Set getAssignmentInfo () {
		return assignmentInfo;
	}

	/**
	 * Set the value related to the column: assignmentInfo
	 * @param assignmentInfo the assignmentInfo value
	 */
	public void setAssignmentInfo (java.util.Set assignmentInfo) {
		this.assignmentInfo = assignmentInfo;
	}

	public void addToassignmentInfo (org.unitime.timetable.model.SolverInfo solverInfo) {
		if (null == getAssignmentInfo()) setAssignmentInfo(new java.util.HashSet());
		getAssignmentInfo().add(solverInfo);
	}



	/**
	 * Return the value associated with the column: constraintInfo
	 */
	public java.util.Set getConstraintInfo () {
		return constraintInfo;
	}

	/**
	 * Set the value related to the column: constraintInfo
	 * @param constraintInfo the constraintInfo value
	 */
	public void setConstraintInfo (java.util.Set constraintInfo) {
		this.constraintInfo = constraintInfo;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Assignment)) return false;
		else {
			org.unitime.timetable.model.Assignment assignment = (org.unitime.timetable.model.Assignment) obj;
			if (null == this.getUniqueId() || null == assignment.getUniqueId()) return false;
			else return (this.getUniqueId().equals(assignment.getUniqueId()));
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