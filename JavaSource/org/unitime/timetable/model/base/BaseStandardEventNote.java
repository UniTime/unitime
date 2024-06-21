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

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.StandardEventNote;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseStandardEventNote implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iReference;
	private String iNote;


	public BaseStandardEventNote() {
	}

	public BaseStandardEventNote(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "pref_group_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "reference", nullable = false, length = 20)
	public String getReference() { return iReference; }
	public void setReference(String reference) { iReference = reference; }

	@Column(name = "note", nullable = true, length = 1000)
	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StandardEventNote)) return false;
		if (getUniqueId() == null || ((StandardEventNote)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StandardEventNote)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "StandardEventNote["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StandardEventNote[" +
			"\n	Note: " + getNote() +
			"\n	Reference: " + getReference() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
