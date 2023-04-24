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
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.unitime.timetable.model.ContactCategory;
import org.unitime.timetable.model.RefTableEntry;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseContactCategory extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iMessage;
	private Boolean iHasRole;
	private String iEmail;


	public BaseContactCategory() {
	}

	public BaseContactCategory(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "message", nullable = true, length = 2048)
	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	@Column(name = "has_role", nullable = false)
	public Boolean isHasRole() { return iHasRole; }
	@Transient
	public Boolean getHasRole() { return iHasRole; }
	public void setHasRole(Boolean hasRole) { iHasRole = hasRole; }

	@Column(name = "email", nullable = true, length = 1000)
	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ContactCategory)) return false;
		if (getUniqueId() == null || ((ContactCategory)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ContactCategory)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ContactCategory["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "ContactCategory[" +
			"\n	Email: " + getEmail() +
			"\n	HasRole: " + getHasRole() +
			"\n	Label: " + getLabel() +
			"\n	Message: " + getMessage() +
			"\n	Reference: " + getReference() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
