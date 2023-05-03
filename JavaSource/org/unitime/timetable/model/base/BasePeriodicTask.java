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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.PeriodicTask;
import org.unitime.timetable.model.Script;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TaskExecution;
import org.unitime.timetable.model.TaskParameter;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePeriodicTask implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iEmail;
	private byte[] iInputFile;

	private Session iSession;
	private Script iScript;
	private TimetableManager iOwner;
	private Set<TaskParameter> iParameters;
	private Set<TaskExecution> iSchedule;

	public BasePeriodicTask() {
	}

	public BasePeriodicTask(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "task_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "task_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = false, length = 128)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "email", nullable = true, length = 1000)
	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	@Column(name = "input_file", nullable = true)
	public byte[] getInputFile() { return iInputFile; }
	public void setInputFile(byte[] inputFile) { iInputFile = inputFile; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "script_id", nullable = false)
	public Script getScript() { return iScript; }
	public void setScript(Script script) { iScript = script; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	public TimetableManager getOwner() { return iOwner; }
	public void setOwner(TimetableManager owner) { iOwner = owner; }

	@OneToMany(mappedBy = "task", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<TaskParameter> getParameters() { return iParameters; }
	public void setParameters(Set<TaskParameter> parameters) { iParameters = parameters; }
	public void addToParameters(TaskParameter taskParameter) {
		if (iParameters == null) iParameters = new HashSet<TaskParameter>();
		iParameters.add(taskParameter);
	}
	@Deprecated
	public void addToparameters(TaskParameter taskParameter) {
		addToParameters(taskParameter);
	}

	@OneToMany(mappedBy = "task", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<TaskExecution> getSchedule() { return iSchedule; }
	public void setSchedule(Set<TaskExecution> schedule) { iSchedule = schedule; }
	public void addToSchedule(TaskExecution taskExecution) {
		if (iSchedule == null) iSchedule = new HashSet<TaskExecution>();
		iSchedule.add(taskExecution);
	}
	@Deprecated
	public void addToschedule(TaskExecution taskExecution) {
		addToSchedule(taskExecution);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PeriodicTask)) return false;
		if (getUniqueId() == null || ((PeriodicTask)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PeriodicTask)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PeriodicTask["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "PeriodicTask[" +
			"\n	Email: " + getEmail() +
			"\n	InputFile: " + getInputFile() +
			"\n	Name: " + getName() +
			"\n	Owner: " + getOwner() +
			"\n	Script: " + getScript() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
