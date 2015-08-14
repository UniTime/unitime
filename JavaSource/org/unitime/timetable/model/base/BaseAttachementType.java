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

import org.unitime.timetable.model.AttachementType;
import org.unitime.timetable.model.RefTableEntry;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseAttachementType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iAbbreviation;
	private Integer iVisibility;


	public static String PROP_ABBREVIATION = "abbreviation";
	public static String PROP_VISIBILITY = "visibility";

	public BaseAttachementType() {
		initialize();
	}

	public BaseAttachementType(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }

	public Integer getVisibility() { return iVisibility; }
	public void setVisibility(Integer visibility) { iVisibility = visibility; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof AttachementType)) return false;
		if (getUniqueId() == null || ((AttachementType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((AttachementType)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "AttachementType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "AttachementType[" +
			"\n	Abbreviation: " + getAbbreviation() +
			"\n	Label: " + getLabel() +
			"\n	Reference: " + getReference() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Visibility: " + getVisibility() +
			"]";
	}
}
