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
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseManagerRole implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Boolean iPrimary;
	private Boolean iReceiveEmails;

	private Roles iRole;
	private TimetableManager iTimetableManager;

	public BaseManagerRole() {
	}

	public BaseManagerRole(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "tmtbl_mgr_to_roles_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "tmtbl_mgr_to_roles_seq")
	})
	@GeneratedValue(generator = "tmtbl_mgr_to_roles_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "is_primary", nullable = true)
	public Boolean isPrimary() { return iPrimary; }
	@Transient
	public Boolean getPrimary() { return iPrimary; }
	public void setPrimary(Boolean primary) { iPrimary = primary; }

	@Column(name = "receive_emails", nullable = true)
	public Boolean isReceiveEmails() { return iReceiveEmails; }
	@Transient
	public Boolean getReceiveEmails() { return iReceiveEmails; }
	public void setReceiveEmails(Boolean receiveEmails) { iReceiveEmails = receiveEmails; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	public Roles getRole() { return iRole; }
	public void setRole(Roles role) { iRole = role; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "manager_id", nullable = false)
	public TimetableManager getTimetableManager() { return iTimetableManager; }
	public void setTimetableManager(TimetableManager timetableManager) { iTimetableManager = timetableManager; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ManagerRole)) return false;
		if (getUniqueId() == null || ((ManagerRole)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ManagerRole)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
