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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.OnlineSectioningLog;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseOnlineSectioningLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iTimeStamp;
	private String iStudent;
	private String iOperation;
	private byte[] iAction;
	private Integer iResult;
	private String iUser;
	private Long iCpuTime;
	private Long iWallTime;
	private String iMessage;
	private Long iApiGetTime;
	private Long iApiPostTime;
	private String iApiException;

	private Session iSession;

	public BaseOnlineSectioningLog() {
	}

	public BaseOnlineSectioningLog(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "sectioning_log_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "sectioning_log_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "time_stamp", nullable = false)
	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	@Column(name = "student", nullable = false, length = 40)
	public String getStudent() { return iStudent; }
	public void setStudent(String student) { iStudent = student; }

	@Column(name = "operation", nullable = false, length = 20)
	public String getOperation() { return iOperation; }
	public void setOperation(String operation) { iOperation = operation; }

	@Column(name = "action", nullable = false)
	public byte[] getAction() { return iAction; }
	public void setAction(byte[] action) { iAction = action; }

	@Column(name = "result", nullable = true)
	public Integer getResult() { return iResult; }
	public void setResult(Integer result) { iResult = result; }

	@Column(name = "user_id", nullable = true, length = 40)
	public String getUser() { return iUser; }
	public void setUser(String user) { iUser = user; }

	@Column(name = "cpu_time", nullable = true)
	public Long getCpuTime() { return iCpuTime; }
	public void setCpuTime(Long cpuTime) { iCpuTime = cpuTime; }

	@Column(name = "wall_time", nullable = true)
	public Long getWallTime() { return iWallTime; }
	public void setWallTime(Long wallTime) { iWallTime = wallTime; }

	@Column(name = "message", nullable = true, length = 255)
	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	@Column(name = "api_get_time", nullable = true)
	public Long getApiGetTime() { return iApiGetTime; }
	public void setApiGetTime(Long apiGetTime) { iApiGetTime = apiGetTime; }

	@Column(name = "api_post_time", nullable = true)
	public Long getApiPostTime() { return iApiPostTime; }
	public void setApiPostTime(Long apiPostTime) { iApiPostTime = apiPostTime; }

	@Column(name = "api_exception", nullable = true, length = 255)
	public String getApiException() { return iApiException; }
	public void setApiException(String apiException) { iApiException = apiException; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof OnlineSectioningLog)) return false;
		if (getUniqueId() == null || ((OnlineSectioningLog)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((OnlineSectioningLog)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "OnlineSectioningLog["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "OnlineSectioningLog[" +
			"\n	Action: " + getAction() +
			"\n	ApiException: " + getApiException() +
			"\n	ApiGetTime: " + getApiGetTime() +
			"\n	ApiPostTime: " + getApiPostTime() +
			"\n	CpuTime: " + getCpuTime() +
			"\n	Message: " + getMessage() +
			"\n	Operation: " + getOperation() +
			"\n	Result: " + getResult() +
			"\n	Session: " + getSession() +
			"\n	Student: " + getStudent() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	User: " + getUser() +
			"\n	WallTime: " + getWallTime() +
			"]";
	}
}
