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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.EventDateMapping;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseEventDateMapping implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iClassDateOffset;
	private Integer iEventDateOffset;
	private String iNote;

	private Session iSession;

	public BaseEventDateMapping() {
	}

	public BaseEventDateMapping(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "date_mapping_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "date_mapping_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "class_date", nullable = true)
	public Integer getClassDateOffset() { return iClassDateOffset; }
	public void setClassDateOffset(Integer classDateOffset) { iClassDateOffset = classDateOffset; }

	@Column(name = "event_date", nullable = true)
	public Integer getEventDateOffset() { return iEventDateOffset; }
	public void setEventDateOffset(Integer eventDateOffset) { iEventDateOffset = eventDateOffset; }

	@Column(name = "note", nullable = true, length = 1000)
	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof EventDateMapping)) return false;
		if (getUniqueId() == null || ((EventDateMapping)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((EventDateMapping)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "EventDateMapping["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "EventDateMapping[" +
			"\n	ClassDateOffset: " + getClassDateOffset() +
			"\n	EventDateOffset: " + getEventDateOffset() +
			"\n	Note: " + getNote() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
