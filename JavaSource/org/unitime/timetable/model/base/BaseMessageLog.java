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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.MessageLog;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseMessageLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iTimeStamp;
	private Integer iLevel;
	private String iMessage;
	private String iLogger;
	private String iThread;
	private String iNdc;
	private String iException;


	public BaseMessageLog() {
	}

	public BaseMessageLog(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "message_log_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "message_log_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "time_stamp", nullable = false)
	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	@Column(name = "log_level", nullable = false)
	public Integer getLevel() { return iLevel; }
	public void setLevel(Integer level) { iLevel = level; }

	@Column(name = "message", nullable = true)
	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	@Column(name = "logger", nullable = false, length = 255)
	public String getLogger() { return iLogger; }
	public void setLogger(String logger) { iLogger = logger; }

	@Column(name = "thread", nullable = true, length = 100)
	public String getThread() { return iThread; }
	public void setThread(String thread) { iThread = thread; }

	@Column(name = "ndc", nullable = true)
	public String getNdc() { return iNdc; }
	public void setNdc(String ndc) { iNdc = ndc; }

	@Column(name = "exception", nullable = true)
	public String getException() { return iException; }
	public void setException(String exception) { iException = exception; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof MessageLog)) return false;
		if (getUniqueId() == null || ((MessageLog)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((MessageLog)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "MessageLog["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "MessageLog[" +
			"\n	Exception: " + getException() +
			"\n	Level: " + getLevel() +
			"\n	Logger: " + getLogger() +
			"\n	Message: " + getMessage() +
			"\n	Ndc: " + getNdc() +
			"\n	Thread: " + getThread() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
