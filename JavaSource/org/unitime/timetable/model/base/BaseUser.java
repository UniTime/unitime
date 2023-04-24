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
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.timetable.model.User;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseUser implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iUsername;
	private String iPassword;
	private String iExternalUniqueId;


	public BaseUser() {
	}

	public BaseUser(String username) {
		setUsername(username);
	}


	@Id
	@Column(name="username")
	public String getUsername() { return iUsername; }
	public void setUsername(String username) { iUsername = username; }

	@Column(name = "password", nullable = false, length = 25)
	public String getPassword() { return iPassword; }
	public void setPassword(String password) { iPassword = password; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof User)) return false;
		if (getUsername() == null || ((User)o).getUsername() == null) return false;
		return getUsername().equals(((User)o).getUsername());
	}

	@Override
	public int hashCode() {
		if (getUsername() == null) return super.hashCode();
		return getUsername().hashCode();
	}

	@Override
	public String toString() {
		return "User["+getUsername()+"]";
	}

	public String toDebugString() {
		return "User[" +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Password: " + getPassword() +
			"\n	Username: " + getUsername() +
			"]";
	}
}
