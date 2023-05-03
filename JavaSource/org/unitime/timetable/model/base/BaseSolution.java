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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
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
@MappedSuperclass
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

	public BaseSolution() {
	}

	public BaseSolution(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "solution_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "solution_seq")
	})
	@GeneratedValue(generator = "solution_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "created", nullable = false)
	public Date getCreated() { return iCreated; }
	public void setCreated(Date created) { iCreated = created; }

	@Column(name = "valid", nullable = false)
	public Boolean isValid() { return iValid; }
	@Transient
	public Boolean getValid() { return iValid; }
	public void setValid(Boolean valid) { iValid = valid; }

	@Column(name = "commited", nullable = false)
	public Boolean isCommited() { return iCommited; }
	@Transient
	public Boolean getCommited() { return iCommited; }
	public void setCommited(Boolean commited) { iCommited = commited; }

	@Column(name = "commit_date", nullable = true)
	public Date getCommitDate() { return iCommitDate; }
	public void setCommitDate(Date commitDate) { iCommitDate = commitDate; }

	@Column(name = "note", nullable = true, length = 1000)
	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	@Column(name = "creator", nullable = true, length = 250)
	public String getCreator() { return iCreator; }
	public void setCreator(String creator) { iCreator = creator; }

	@Formula("(select p.value from %SCHEMA%.solver_parameter p, %SCHEMA%.solver_parameter_def d where p.solution_id = uniqueid and d.uniqueid = p.solver_param_def_id and d.name='Basic.Mode')")
	public String getSolverMode() { return iSolverMode; }
	public void setSolverMode(String solverMode) { iSolverMode = solverMode; }

	@Formula("(select s.description from %SCHEMA%.solver_parameter p, %SCHEMA%.solver_parameter_def d, %SCHEMA%.solver_predef_setting s where p.solution_id = uniqueId and d.uniqueid = p.solver_param_def_id and d.name='General.SettingsId' and concat(s.uniqueid,'') = p.value)")
	public String getSolverConfiguration() { return iSolverConfiguration; }
	public void setSolverConfiguration(String solverConfiguration) { iSolverConfiguration = solverConfiguration; }

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "owner_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public SolverGroup getOwner() { return iOwner; }
	public void setOwner(SolverGroup owner) { iOwner = owner; }

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinFormula(" ( select si.uniqueid from %SCHEMA%.solver_info si, %SCHEMA%.solver_info_def d where si.type=1 and si.solver_info_def_id=d.uniqueid and d.name='GlobalInfo' and si.solution_id=uniqueid ) ")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public SolutionInfo getGlobalInfo() { return iGlobalInfo; }
	public void setGlobalInfo(SolutionInfo globalInfo) { iGlobalInfo = globalInfo; }

	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name = "solution_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<SolverParameter> getParameters() { return iParameters; }
	public void setParameters(Set<SolverParameter> parameters) { iParameters = parameters; }
	public void addToParameters(SolverParameter solverParameter) {
		if (iParameters == null) iParameters = new HashSet<SolverParameter>();
		iParameters.add(solverParameter);
	}
	@Deprecated
	public void addToparameters(SolverParameter solverParameter) {
		addToParameters(solverParameter);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "solution", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<SolutionInfo> getSolutionInfo() { return iSolutionInfo; }
	public void setSolutionInfo(Set<SolutionInfo> solutionInfo) { iSolutionInfo = solutionInfo; }
	public void addToSolutionInfo(SolutionInfo solutionInfo) {
		if (iSolutionInfo == null) iSolutionInfo = new HashSet<SolutionInfo>();
		iSolutionInfo.add(solutionInfo);
	}
	@Deprecated
	public void addTosolutionInfo(SolutionInfo solutionInfo) {
		addToSolutionInfo(solutionInfo);
	}

	@OneToMany(mappedBy = "solution", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<StudentEnrollment> getStudentEnrollments() { return iStudentEnrollments; }
	public void setStudentEnrollments(Set<StudentEnrollment> studentEnrollments) { iStudentEnrollments = studentEnrollments; }
	public void addToStudentEnrollments(StudentEnrollment studentEnrollment) {
		if (iStudentEnrollments == null) iStudentEnrollments = new HashSet<StudentEnrollment>();
		iStudentEnrollments.add(studentEnrollment);
	}
	@Deprecated
	public void addTostudentEnrollments(StudentEnrollment studentEnrollment) {
		addToStudentEnrollments(studentEnrollment);
	}

	@OneToMany(mappedBy = "solution", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToAssignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet<Assignment>();
		iAssignments.add(assignment);
	}
	@Deprecated
	public void addToassignments(Assignment assignment) {
		addToAssignments(assignment);
	}

	@OneToMany(mappedBy = "solution", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<JointEnrollment> getJointEnrollments() { return iJointEnrollments; }
	public void setJointEnrollments(Set<JointEnrollment> jointEnrollments) { iJointEnrollments = jointEnrollments; }
	public void addToJointEnrollments(JointEnrollment jointEnrollment) {
		if (iJointEnrollments == null) iJointEnrollments = new HashSet<JointEnrollment>();
		iJointEnrollments.add(jointEnrollment);
	}
	@Deprecated
	public void addTojointEnrollments(JointEnrollment jointEnrollment) {
		addToJointEnrollments(jointEnrollment);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Solution)) return false;
		if (getUniqueId() == null || ((Solution)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Solution)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
