/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.JointEnrollment;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolutionInfo;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.StudentEnrollment;

/**
 * @author Tomas Muller
 */
public abstract class BaseSolution implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iCreated;
	private Boolean iValid;
	private Boolean iCommited;
	private Date iCommitDate;
	private String iNote;
	private String iCreator;

	private SolverGroup iOwner;
	private SolutionInfo iGlobalInfo;
	private Set<SolverParameter> iParameters;
	private Set<SolutionInfo> iSolutionInfo;
	private Set<StudentEnrollment> iStudentEnrollments;
	private Set<Assignment> iAssignments;
	private Set<JointEnrollment> iJointEnrollments;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CREATED = "created";
	public static String PROP_VALID = "valid";
	public static String PROP_COMMITED = "commited";
	public static String PROP_COMMIT_DATE = "commitDate";
	public static String PROP_NOTE = "note";
	public static String PROP_CREATOR = "creator";

	public BaseSolution() {
		initialize();
	}

	public BaseSolution(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Date getCreated() { return iCreated; }
	public void setCreated(Date created) { iCreated = created; }

	public Boolean isValid() { return iValid; }
	public Boolean getValid() { return iValid; }
	public void setValid(Boolean valid) { iValid = valid; }

	public Boolean isCommited() { return iCommited; }
	public Boolean getCommited() { return iCommited; }
	public void setCommited(Boolean commited) { iCommited = commited; }

	public Date getCommitDate() { return iCommitDate; }
	public void setCommitDate(Date commitDate) { iCommitDate = commitDate; }

	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	public String getCreator() { return iCreator; }
	public void setCreator(String creator) { iCreator = creator; }

	public SolverGroup getOwner() { return iOwner; }
	public void setOwner(SolverGroup owner) { iOwner = owner; }

	public SolutionInfo getGlobalInfo() { return iGlobalInfo; }
	public void setGlobalInfo(SolutionInfo globalInfo) { iGlobalInfo = globalInfo; }

	public Set<SolverParameter> getParameters() { return iParameters; }
	public void setParameters(Set<SolverParameter> parameters) { iParameters = parameters; }
	public void addToparameters(SolverParameter solverParameter) {
		if (iParameters == null) iParameters = new HashSet<SolverParameter>();
		iParameters.add(solverParameter);
	}

	public Set<SolutionInfo> getSolutionInfo() { return iSolutionInfo; }
	public void setSolutionInfo(Set<SolutionInfo> solutionInfo) { iSolutionInfo = solutionInfo; }
	public void addTosolutionInfo(SolutionInfo solutionInfo) {
		if (iSolutionInfo == null) iSolutionInfo = new HashSet<SolutionInfo>();
		iSolutionInfo.add(solutionInfo);
	}

	public Set<StudentEnrollment> getStudentEnrollments() { return iStudentEnrollments; }
	public void setStudentEnrollments(Set<StudentEnrollment> studentEnrollments) { iStudentEnrollments = studentEnrollments; }
	public void addTostudentEnrollments(StudentEnrollment studentEnrollment) {
		if (iStudentEnrollments == null) iStudentEnrollments = new HashSet<StudentEnrollment>();
		iStudentEnrollments.add(studentEnrollment);
	}

	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToassignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet<Assignment>();
		iAssignments.add(assignment);
	}

	public Set<JointEnrollment> getJointEnrollments() { return iJointEnrollments; }
	public void setJointEnrollments(Set<JointEnrollment> jointEnrollments) { iJointEnrollments = jointEnrollments; }
	public void addTojointEnrollments(JointEnrollment jointEnrollment) {
		if (iJointEnrollments == null) iJointEnrollments = new HashSet<JointEnrollment>();
		iJointEnrollments.add(jointEnrollment);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Solution)) return false;
		if (getUniqueId() == null || ((Solution)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Solution)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Solution["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Solution[" +
			"\n	CommitDate: " + getCommitDate() +
			"\n	Commited: " + getCommited() +
			"\n	Created: " + getCreated() +
			"\n	Creator: " + getCreator() +
			"\n	Note: " + getNote() +
			"\n	Owner: " + getOwner() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Valid: " + getValid() +
			"]";
	}
}
