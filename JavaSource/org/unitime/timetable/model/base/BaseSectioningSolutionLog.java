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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.SectioningSolutionLog;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSectioningSolutionLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iTimeStamp;
	private String iInfo;
	private byte[] iData;
	private String iNote;
	private String iConfig;

	private Session iSession;
	private TimetableManager iOwner;

	public BaseSectioningSolutionLog() {
	}

	public BaseSectioningSolutionLog(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "sct_solution_log_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "sct_solution_log_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "time_stamp", nullable = false)
	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	@Column(name = "info", nullable = false, length = 10000)
	public String getInfo() { return iInfo; }
	public void setInfo(String info) { iInfo = info; }

	@Column(name = "data", nullable = false)
	public byte[] getData() { return iData; }
	public void setData(byte[] data) { iData = data; }

	@Column(name = "note", nullable = true, length = 2000)
	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	@Column(name = "config", nullable = true, length = 100)
	public String getConfig() { return iConfig; }
	public void setConfig(String config) { iConfig = config; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	public TimetableManager getOwner() { return iOwner; }
	public void setOwner(TimetableManager owner) { iOwner = owner; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SectioningSolutionLog)) return false;
		if (getUniqueId() == null || ((SectioningSolutionLog)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SectioningSolutionLog)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "SectioningSolutionLog["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SectioningSolutionLog[" +
			"\n	Config: " + getConfig() +
			"\n	Data: " + getData() +
			"\n	Info: " + getInfo() +
			"\n	Note: " + getNote() +
			"\n	Owner: " + getOwner() +
			"\n	Session: " + getSession() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
