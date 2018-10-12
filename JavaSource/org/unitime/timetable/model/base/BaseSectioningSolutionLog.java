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

import org.unitime.timetable.model.SectioningSolutionLog;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseSectioningSolutionLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iTimeStamp;
	private String iInfo;
	private byte[] iData;

	private Session iSession;
	private TimetableManager iOwner;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TIME_STAMP = "timeStamp";
	public static String PROP_INFO = "info";
	public static String PROP_DATA = "data";

	public BaseSectioningSolutionLog() {
		initialize();
	}

	public BaseSectioningSolutionLog(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	public String getInfo() { return iInfo; }
	public void setInfo(String info) { iInfo = info; }

	public byte[] getData() { return iData; }
	public void setData(byte[] data) { iData = data; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public TimetableManager getOwner() { return iOwner; }
	public void setOwner(TimetableManager owner) { iOwner = owner; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SectioningSolutionLog)) return false;
		if (getUniqueId() == null || ((SectioningSolutionLog)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SectioningSolutionLog)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "SectioningSolutionLog["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SectioningSolutionLog[" +
			"\n	Data: " + getData() +
			"\n	Info: " + getInfo() +
			"\n	Owner: " + getOwner() +
			"\n	Session: " + getSession() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
