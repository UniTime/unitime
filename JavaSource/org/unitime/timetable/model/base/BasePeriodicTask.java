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
import java.util.HashSet;
import java.util.Set;

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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_EMAIL = "email";
	public static String PROP_INPUT_FILE = "inputFile";

	public BasePeriodicTask() {
		initialize();
	}

	public BasePeriodicTask(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	public byte[] getInputFile() { return iInputFile; }
	public void setInputFile(byte[] inputFile) { iInputFile = inputFile; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Script getScript() { return iScript; }
	public void setScript(Script script) { iScript = script; }

	public TimetableManager getOwner() { return iOwner; }
	public void setOwner(TimetableManager owner) { iOwner = owner; }

	public Set<TaskParameter> getParameters() { return iParameters; }
	public void setParameters(Set<TaskParameter> parameters) { iParameters = parameters; }
	public void addToparameters(TaskParameter taskParameter) {
		if (iParameters == null) iParameters = new HashSet<TaskParameter>();
		iParameters.add(taskParameter);
	}

	public Set<TaskExecution> getSchedule() { return iSchedule; }
	public void setSchedule(Set<TaskExecution> schedule) { iSchedule = schedule; }
	public void addToschedule(TaskExecution taskExecution) {
		if (iSchedule == null) iSchedule = new HashSet<TaskExecution>();
		iSchedule.add(taskExecution);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PeriodicTask)) return false;
		if (getUniqueId() == null || ((PeriodicTask)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PeriodicTask)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
