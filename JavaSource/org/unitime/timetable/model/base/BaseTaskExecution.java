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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.PeriodicTask;
import org.unitime.timetable.model.TaskExecution;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseTaskExecution implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iExecutionDate;
	private Integer iExecutionPeriod;
	private Integer iExecutionStatus;
	private Date iCreatedDate;
	private Date iScheduledDate;
	private Date iQueuedDate;
	private Date iStartedDate;
	private Date iFinishedDate;
	private String iLogFile;
	private byte[] iOutputFile;
	private String iOutputName;
	private String iOutputContentType;
	private String iStatusMessage;

	private PeriodicTask iTask;

	public BaseTaskExecution() {
	}

	public BaseTaskExecution(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "task_execution_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "task_execution_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "exec_date", nullable = false)
	public Integer getExecutionDate() { return iExecutionDate; }
	public void setExecutionDate(Integer executionDate) { iExecutionDate = executionDate; }

	@Column(name = "exec_period", nullable = false)
	public Integer getExecutionPeriod() { return iExecutionPeriod; }
	public void setExecutionPeriod(Integer executionPeriod) { iExecutionPeriod = executionPeriod; }

	@Column(name = "status", nullable = false)
	public Integer getExecutionStatus() { return iExecutionStatus; }
	public void setExecutionStatus(Integer executionStatus) { iExecutionStatus = executionStatus; }

	@Column(name = "created_date", nullable = false)
	public Date getCreatedDate() { return iCreatedDate; }
	public void setCreatedDate(Date createdDate) { iCreatedDate = createdDate; }

	@Column(name = "scheduled_date", nullable = false)
	public Date getScheduledDate() { return iScheduledDate; }
	public void setScheduledDate(Date scheduledDate) { iScheduledDate = scheduledDate; }

	@Column(name = "queued_date", nullable = true)
	public Date getQueuedDate() { return iQueuedDate; }
	public void setQueuedDate(Date queuedDate) { iQueuedDate = queuedDate; }

	@Column(name = "started_date", nullable = true)
	public Date getStartedDate() { return iStartedDate; }
	public void setStartedDate(Date startedDate) { iStartedDate = startedDate; }

	@Column(name = "finished_date", nullable = true)
	public Date getFinishedDate() { return iFinishedDate; }
	public void setFinishedDate(Date finishedDate) { iFinishedDate = finishedDate; }

	@Column(name = "log_file", nullable = true)
	public String getLogFile() { return iLogFile; }
	public void setLogFile(String logFile) { iLogFile = logFile; }

	@Column(name = "output_file", nullable = true)
	public byte[] getOutputFile() { return iOutputFile; }
	public void setOutputFile(byte[] outputFile) { iOutputFile = outputFile; }

	@Column(name = "output_name", nullable = true, length = 260)
	public String getOutputName() { return iOutputName; }
	public void setOutputName(String outputName) { iOutputName = outputName; }

	@Column(name = "output_content", nullable = true, length = 260)
	public String getOutputContentType() { return iOutputContentType; }
	public void setOutputContentType(String outputContentType) { iOutputContentType = outputContentType; }

	@Column(name = "status_message", nullable = true, length = 260)
	public String getStatusMessage() { return iStatusMessage; }
	public void setStatusMessage(String statusMessage) { iStatusMessage = statusMessage; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	public PeriodicTask getTask() { return iTask; }
	public void setTask(PeriodicTask task) { iTask = task; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TaskExecution)) return false;
		if (getUniqueId() == null || ((TaskExecution)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TaskExecution)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "TaskExecution["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TaskExecution[" +
			"\n	CreatedDate: " + getCreatedDate() +
			"\n	ExecutionDate: " + getExecutionDate() +
			"\n	ExecutionPeriod: " + getExecutionPeriod() +
			"\n	ExecutionStatus: " + getExecutionStatus() +
			"\n	FinishedDate: " + getFinishedDate() +
			"\n	LogFile: " + getLogFile() +
			"\n	OutputContentType: " + getOutputContentType() +
			"\n	OutputFile: " + getOutputFile() +
			"\n	OutputName: " + getOutputName() +
			"\n	QueuedDate: " + getQueuedDate() +
			"\n	ScheduledDate: " + getScheduledDate() +
			"\n	StartedDate: " + getStartedDate() +
			"\n	StatusMessage: " + getStatusMessage() +
			"\n	Task: " + getTask() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
