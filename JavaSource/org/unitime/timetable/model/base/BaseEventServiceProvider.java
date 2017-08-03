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

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseEventServiceProvider implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iReference;
	private String iLabel;
	private String iNote;
	private String iEmail;
	private Boolean iAllRooms;
	private Boolean iVisible;

	private Session iSession;
	private Department iDepartment;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_REFERENCE = "reference";
	public static String PROP_LABEL = "label";
	public static String PROP_NOTE = "note";
	public static String PROP_EMAIL = "email";
	public static String PROP_ALL_ROOMS = "allRooms";
	public static String PROP_VISIBLE = "visible";

	public BaseEventServiceProvider() {
		initialize();
	}

	public BaseEventServiceProvider(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getReference() { return iReference; }
	public void setReference(String reference) { iReference = reference; }

	public String getLabel() { return iLabel; }
	public void setLabel(String label) { iLabel = label; }

	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	public Boolean isAllRooms() { return iAllRooms; }
	public Boolean getAllRooms() { return iAllRooms; }
	public void setAllRooms(Boolean allRooms) { iAllRooms = allRooms; }

	public Boolean isVisible() { return iVisible; }
	public Boolean getVisible() { return iVisible; }
	public void setVisible(Boolean visible) { iVisible = visible; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof EventServiceProvider)) return false;
		if (getUniqueId() == null || ((EventServiceProvider)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((EventServiceProvider)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "EventServiceProvider["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "EventServiceProvider[" +
			"\n	AllRooms: " + getAllRooms() +
			"\n	Department: " + getDepartment() +
			"\n	Email: " + getEmail() +
			"\n	Label: " + getLabel() +
			"\n	Note: " + getNote() +
			"\n	Reference: " + getReference() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Visible: " + getVisible() +
			"]";
	}
}
