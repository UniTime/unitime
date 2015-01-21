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

import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseManagerRole implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Boolean iPrimary;
	private Boolean iReceiveEmails;

	private Roles iRole;
	private TimetableManager iTimetableManager;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_IS_PRIMARY = "primary";
	public static String PROP_RECEIVE_EMAILS = "receiveEmails";

	public BaseManagerRole() {
		initialize();
	}

	public BaseManagerRole(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Boolean isPrimary() { return iPrimary; }
	public Boolean getPrimary() { return iPrimary; }
	public void setPrimary(Boolean primary) { iPrimary = primary; }

	public Boolean isReceiveEmails() { return iReceiveEmails; }
	public Boolean getReceiveEmails() { return iReceiveEmails; }
	public void setReceiveEmails(Boolean receiveEmails) { iReceiveEmails = receiveEmails; }

	public Roles getRole() { return iRole; }
	public void setRole(Roles role) { iRole = role; }

	public TimetableManager getTimetableManager() { return iTimetableManager; }
	public void setTimetableManager(TimetableManager timetableManager) { iTimetableManager = timetableManager; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ManagerRole)) return false;
		if (getUniqueId() == null || ((ManagerRole)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ManagerRole)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ManagerRole["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ManagerRole[" +
			"\n	Primary: " + getPrimary() +
			"\n	ReceiveEmails: " + getReceiveEmails() +
			"\n	Role: " + getRole() +
			"\n	TimetableManager: " + getTimetableManager() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
