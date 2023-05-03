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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.timetable.model.PeriodicTask;
import org.unitime.timetable.model.TaskParameter;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
@IdClass(TaskParameterId.class)
public abstract class BaseTaskParameter implements Serializable {
	private static final long serialVersionUID = 1L;

	private PeriodicTask iTask;
	private String iName;
	private String iValue;


	public BaseTaskParameter() {
	}


	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "task_id")
	public PeriodicTask getTask() { return iTask; }
	public void setTask(PeriodicTask task) { iTask = task; }

	@Id
	@Column(name="name", length = 128)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "value", nullable = true, length = 2048)
	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TaskParameter)) return false;
		TaskParameter taskParameter = (TaskParameter)o;
		if (getTask() == null || taskParameter.getTask() == null || !getTask().equals(taskParameter.getTask())) return false;
		if (getName() == null || taskParameter.getName() == null || !getName().equals(taskParameter.getName())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getTask() == null || getName() == null) return super.hashCode();
		return getTask().hashCode() ^ getName().hashCode();
	}

	public String toString() {
		return "TaskParameter[" + getTask() + ", " + getName() + "]";
	}

	public String toDebugString() {
		return "TaskParameter[" +
			"\n	Name: " + getName() +
			"\n	Task: " + getTask() +
			"\n	Value: " + getValue() +
			"]";
	}
}
