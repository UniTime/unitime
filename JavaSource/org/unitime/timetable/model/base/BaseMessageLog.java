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

import org.unitime.timetable.model.MessageLog;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TIME_STAMP = "timeStamp";
	public static String PROP_LOG_LEVEL = "level";
	public static String PROP_MESSAGE = "message";
	public static String PROP_LOGGER = "logger";
	public static String PROP_THREAD = "thread";
	public static String PROP_NDC = "ndc";
	public static String PROP_EXCEPTION = "exception";

	public BaseMessageLog() {
		initialize();
	}

	public BaseMessageLog(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	public Integer getLevel() { return iLevel; }
	public void setLevel(Integer level) { iLevel = level; }

	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	public String getLogger() { return iLogger; }
	public void setLogger(String logger) { iLogger = logger; }

	public String getThread() { return iThread; }
	public void setThread(String thread) { iThread = thread; }

	public String getNdc() { return iNdc; }
	public void setNdc(String ndc) { iNdc = ndc; }

	public String getException() { return iException; }
	public void setException(String exception) { iException = exception; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof MessageLog)) return false;
		if (getUniqueId() == null || ((MessageLog)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((MessageLog)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
