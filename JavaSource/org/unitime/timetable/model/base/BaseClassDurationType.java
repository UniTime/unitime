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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.io.Serializable;

import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.RefTableEntry;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseClassDurationType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iAbbreviation;
	private String iImplementation;
	private String iParameter;
	private Boolean iVisible;


	public BaseClassDurationType() {
	}

	public BaseClassDurationType(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "abbreviation", nullable = false, length = 20)
	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }

	@Column(name = "implementation", nullable = false, length = 255)
	public String getImplementation() { return iImplementation; }
	public void setImplementation(String implementation) { iImplementation = implementation; }

	@Column(name = "parameter", nullable = true, length = 200)
	public String getParameter() { return iParameter; }
	public void setParameter(String parameter) { iParameter = parameter; }

	@Column(name = "visible", nullable = true)
	public Boolean isVisible() { return iVisible; }
	@Transient
	public Boolean getVisible() { return iVisible; }
	public void setVisible(Boolean visible) { iVisible = visible; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ClassDurationType)) return false;
		if (getUniqueId() == null || ((ClassDurationType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ClassDurationType)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ClassDurationType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "ClassDurationType[" +
			"\n	Abbreviation: " + getAbbreviation() +
			"\n	Implementation: " + getImplementation() +
			"\n	Label: " + getLabel() +
			"\n	Parameter: " + getParameter() +
			"\n	Reference: " + getReference() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Visible: " + getVisible() +
			"]";
	}
}
