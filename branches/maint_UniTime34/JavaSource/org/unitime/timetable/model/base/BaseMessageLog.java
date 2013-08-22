/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;

import org.unitime.timetable.model.MessageLog;

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
