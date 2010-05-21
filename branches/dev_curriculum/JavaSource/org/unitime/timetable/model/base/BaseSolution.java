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
 * This is an object that contains data related to the SOLUTION table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SOLUTION"
 */

public abstract class BaseSolution  implements Serializable {

	public static String REF = "Solution";
	public static String PROP_CREATED = "created";
	public static String PROP_VALID = "valid";
	public static String PROP_COMMITED = "commited";
	public static String PROP_COMMIT_DATE = "commitDate";
	public static String PROP_NOTE = "note";
	public static String PROP_CREATOR = "creator";


	// constructors
	public BaseSolution () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseSolution (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseSolution (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.SolverGroup owner,
		java.util.Date created,
		java.lang.Boolean valid,
		java.lang.Boolean commited) {

		this.setUniqueId(uniqueId);
		this.setOwner(owner);
		this.setCreated(created);
		this.setValid(valid);
		this.setCommited(commited);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.util.Date created;
	private java.lang.Boolean valid;
	private java.lang.Boolean commited;
	private java.util.Date commitDate;
	private java.lang.String note;
	private java.lang.String creator;

	// many to one
	private org.unitime.timetable.model.SolverGroup owner;
	private org.unitime.timetable.model.SolutionInfo globalInfo;

	// collections
	private java.util.Set parameters;
	private java.util.Set solutionInfo;
	private java.util.Set studentEnrollments;
	private java.util.Set assignments;
	private java.util.Set jointEnrollments;



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
	 * Return the value associated with the column: CREATED
	 */
	public java.util.Date getCreated () {
		return created;
	}

	/**
	 * Set the value related to the column: CREATED
	 * @param created the CREATED value
	 */
	public void setCreated (java.util.Date created) {
		this.created = created;
	}



	/**
	 * Return the value associated with the column: VALID
	 */
	public java.lang.Boolean isValid () {
		return valid;
	}

	/**
	 * Set the value related to the column: VALID
	 * @param valid the VALID value
	 */
	public void setValid (java.lang.Boolean valid) {
		this.valid = valid;
	}



	/**
	 * Return the value associated with the column: COMMITED
	 */
	public java.lang.Boolean isCommited () {
		return commited;
	}

	/**
	 * Set the value related to the column: COMMITED
	 * @param commited the COMMITED value
	 */
	public void setCommited (java.lang.Boolean commited) {
		this.commited = commited;
	}



	/**
	 * Return the value associated with the column: COMMIT_DATE
	 */
	public java.util.Date getCommitDate () {
		return commitDate;
	}

	/**
	 * Set the value related to the column: COMMIT_DATE
	 * @param commitDate the COMMIT_DATE value
	 */
	public void setCommitDate (java.util.Date commitDate) {
		this.commitDate = commitDate;
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
	 * Return the value associated with the column: CREATOR
	 */
	public java.lang.String getCreator () {
		return creator;
	}

	/**
	 * Set the value related to the column: CREATOR
	 * @param creator the CREATOR value
	 */
	public void setCreator (java.lang.String creator) {
		this.creator = creator;
	}



	/**
	 * Return the value associated with the column: OWNER_ID
	 */
	public org.unitime.timetable.model.SolverGroup getOwner () {
		return owner;
	}

	/**
	 * Set the value related to the column: OWNER_ID
	 * @param owner the OWNER_ID value
	 */
	public void setOwner (org.unitime.timetable.model.SolverGroup owner) {
		this.owner = owner;
	}



	/**
	 * Return the value associated with the column: globalInfo
	 */
	public org.unitime.timetable.model.SolutionInfo getGlobalInfo () {
		return globalInfo;
	}

	/**
	 * Set the value related to the column: globalInfo
	 * @param globalInfo the globalInfo value
	 */
	public void setGlobalInfo (org.unitime.timetable.model.SolutionInfo globalInfo) {
		this.globalInfo = globalInfo;
	}



	/**
	 * Return the value associated with the column: parameters
	 */
	public java.util.Set getParameters () {
		return parameters;
	}

	/**
	 * Set the value related to the column: parameters
	 * @param parameters the parameters value
	 */
	public void setParameters (java.util.Set parameters) {
		this.parameters = parameters;
	}

	public void addToparameters (org.unitime.timetable.model.SolverParameter solverParameter) {
		if (null == getParameters()) setParameters(new java.util.HashSet());
		getParameters().add(solverParameter);
	}



	/**
	 * Return the value associated with the column: solutionInfo
	 */
	public java.util.Set getSolutionInfo () {
		return solutionInfo;
	}

	/**
	 * Set the value related to the column: solutionInfo
	 * @param solutionInfo the solutionInfo value
	 */
	public void setSolutionInfo (java.util.Set solutionInfo) {
		this.solutionInfo = solutionInfo;
	}

	public void addTosolutionInfo (org.unitime.timetable.model.SolverInfo solverInfo) {
		if (null == getSolutionInfo()) setSolutionInfo(new java.util.HashSet());
		getSolutionInfo().add(solverInfo);
	}



	/**
	 * Return the value associated with the column: studentEnrollments
	 */
	public java.util.Set getStudentEnrollments () {
		return studentEnrollments;
	}

	/**
	 * Set the value related to the column: studentEnrollments
	 * @param studentEnrollments the studentEnrollments value
	 */
	public void setStudentEnrollments (java.util.Set studentEnrollments) {
		this.studentEnrollments = studentEnrollments;
	}

	public void addTostudentEnrollments (org.unitime.timetable.model.StudentEnrollment studentEnrollment) {
		if (null == getStudentEnrollments()) setStudentEnrollments(new java.util.HashSet());
		getStudentEnrollments().add(studentEnrollment);
	}



	/**
	 * Return the value associated with the column: assignments
	 */
	public java.util.Set getAssignments () {
		return assignments;
	}

	/**
	 * Set the value related to the column: assignments
	 * @param assignments the assignments value
	 */
	public void setAssignments (java.util.Set assignments) {
		this.assignments = assignments;
	}

	public void addToassignments (org.unitime.timetable.model.Assignment assignment) {
		if (null == getAssignments()) setAssignments(new java.util.HashSet());
		getAssignments().add(assignment);
	}



	/**
	 * Return the value associated with the column: jointEnrollments
	 */
	public java.util.Set getJointEnrollments () {
		return jointEnrollments;
	}

	/**
	 * Set the value related to the column: jointEnrollments
	 * @param jointEnrollments the jointEnrollments value
	 */
	public void setJointEnrollments (java.util.Set jointEnrollments) {
		this.jointEnrollments = jointEnrollments;
	}

	public void addTojointEnrollments (org.unitime.timetable.model.JointEnrollment jointEnrollment) {
		if (null == getJointEnrollments()) setJointEnrollments(new java.util.HashSet());
		getJointEnrollments().add(jointEnrollment);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Solution)) return false;
		else {
			org.unitime.timetable.model.Solution solution = (org.unitime.timetable.model.Solution) obj;
			if (null == this.getUniqueId() || null == solution.getUniqueId()) return false;
			else return (this.getUniqueId().equals(solution.getUniqueId()));
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
