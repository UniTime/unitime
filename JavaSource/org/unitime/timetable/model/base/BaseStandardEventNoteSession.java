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

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StandardEventNote;
import org.unitime.timetable.model.StandardEventNoteSession;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseStandardEventNoteSession extends StandardEventNote implements Serializable {
	private static final long serialVersionUID = 1L;

	private Session iSession;

	public BaseStandardEventNoteSession() {
	}

	public BaseStandardEventNoteSession(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StandardEventNoteSession)) return false;
		if (getUniqueId() == null || ((StandardEventNoteSession)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StandardEventNoteSession)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "StandardEventNoteSession["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StandardEventNoteSession[" +
			"\n	Note: " + getNote() +
			"\n	Reference: " + getReference() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
