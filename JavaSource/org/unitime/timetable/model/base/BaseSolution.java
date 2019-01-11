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
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
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
	private String iSolverMode;
	private String iSolverConfiguration;

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

	public String getSolverMode() { return iSolverMode; }
	public void setSolverMode(String solverMode) { iSolverMode = solverMode; }

	public String getSolverConfiguration() { return iSolverConfiguration; }
	public void setSolverConfiguration(String solverConfiguration) { iSolverConfiguration = solverConfiguration; }

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
