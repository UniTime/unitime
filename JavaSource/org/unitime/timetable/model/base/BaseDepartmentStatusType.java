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

import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.RefTableEntry;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseDepartmentStatusType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iStatus;
	private Integer iApply;
	private Integer iOrd;


	public static String PROP_STATUS = "status";
	public static String PROP_APPLY = "apply";
	public static String PROP_ORD = "ord";

	public BaseDepartmentStatusType() {
		initialize();
	}

	public BaseDepartmentStatusType(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Integer getStatus() { return iStatus; }
	public void setStatus(Integer status) { iStatus = status; }

	public Integer getApply() { return iApply; }
	public void setApply(Integer apply) { iApply = apply; }

	public Integer getOrd() { return iOrd; }
	public void setOrd(Integer ord) { iOrd = ord; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof DepartmentStatusType)) return false;
		if (getUniqueId() == null || ((DepartmentStatusType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((DepartmentStatusType)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "DepartmentStatusType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "DepartmentStatusType[" +
			"\n	Apply: " + getApply() +
			"\n	Label: " + getLabel() +
			"\n	Ord: " + getOrd() +
			"\n	Reference: " + getReference() +
			"\n	Status: " + getStatus() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
