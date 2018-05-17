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

import org.unitime.timetable.model.PeriodicTask;
import org.unitime.timetable.model.TaskExecution;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXEC_DATE = "executionDate";
	public static String PROP_EXEC_PERIOD = "executionPeriod";
	public static String PROP_STATUS = "executionStatus";
	public static String PROP_CREATED_DATE = "createdDate";
	public static String PROP_SCHEDULED_DATE = "scheduledDate";
	public static String PROP_QUEUED_DATE = "queuedDate";
	public static String PROP_STARTED_DATE = "startedDate";
	public static String PROP_FINISHED_DATE = "finishedDate";
	public static String PROP_LOG_FILE = "logFile";
	public static String PROP_OUTPUT_FILE = "outputFile";
	public static String PROP_OUTPUT_NAME = "outputName";
	public static String PROP_OUTPUT_CONTENT = "outputContentType";
	public static String PROP_STATUS_MESSAGE = "statusMessage";

	public BaseTaskExecution() {
		initialize();
	}

	public BaseTaskExecution(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getExecutionDate() { return iExecutionDate; }
	public void setExecutionDate(Integer executionDate) { iExecutionDate = executionDate; }

	public Integer getExecutionPeriod() { return iExecutionPeriod; }
	public void setExecutionPeriod(Integer executionPeriod) { iExecutionPeriod = executionPeriod; }

	public Integer getExecutionStatus() { return iExecutionStatus; }
	public void setExecutionStatus(Integer executionStatus) { iExecutionStatus = executionStatus; }

	public Date getCreatedDate() { return iCreatedDate; }
	public void setCreatedDate(Date createdDate) { iCreatedDate = createdDate; }

	public Date getScheduledDate() { return iScheduledDate; }
	public void setScheduledDate(Date scheduledDate) { iScheduledDate = scheduledDate; }

	public Date getQueuedDate() { return iQueuedDate; }
	public void setQueuedDate(Date queuedDate) { iQueuedDate = queuedDate; }

	public Date getStartedDate() { return iStartedDate; }
	public void setStartedDate(Date startedDate) { iStartedDate = startedDate; }

	public Date getFinishedDate() { return iFinishedDate; }
	public void setFinishedDate(Date finishedDate) { iFinishedDate = finishedDate; }

	public String getLogFile() { return iLogFile; }
	public void setLogFile(String logFile) { iLogFile = logFile; }

	public byte[] getOutputFile() { return iOutputFile; }
	public void setOutputFile(byte[] outputFile) { iOutputFile = outputFile; }

	public String getOutputName() { return iOutputName; }
	public void setOutputName(String outputName) { iOutputName = outputName; }

	public String getOutputContentType() { return iOutputContentType; }
	public void setOutputContentType(String outputContentType) { iOutputContentType = outputContentType; }

	public String getStatusMessage() { return iStatusMessage; }
	public void setStatusMessage(String statusMessage) { iStatusMessage = statusMessage; }

	public PeriodicTask getTask() { return iTask; }
	public void setTask(PeriodicTask task) { iTask = task; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof TaskExecution)) return false;
		if (getUniqueId() == null || ((TaskExecution)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TaskExecution)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
