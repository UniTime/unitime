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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMajorConcentration;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePosMajorConcentration implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iCode;
	private String iName;

	private PosMajor iMajor;

	public BasePosMajorConcentration() {
	}

	public BasePosMajorConcentration(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "pos_major_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "code", nullable = false, length = 40)
	public String getCode() { return iCode; }
	public void setCode(String code) { iCode = code; }

	@Column(name = "name", nullable = false, length = 100)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "major_id", nullable = false)
	public PosMajor getMajor() { return iMajor; }
	public void setMajor(PosMajor major) { iMajor = major; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PosMajorConcentration)) return false;
		if (getUniqueId() == null || ((PosMajorConcentration)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PosMajorConcentration)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PosMajorConcentration["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "PosMajorConcentration[" +
			"\n	Code: " + getCode() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Major: " + getMajor() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
