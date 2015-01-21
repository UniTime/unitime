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

import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.RefTableEntry;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePositionType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iSortOrder;


	public static String PROP_SORT_ORDER = "sortOrder";

	public BasePositionType() {
		initialize();
	}

	public BasePositionType(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Integer getSortOrder() { return iSortOrder; }
	public void setSortOrder(Integer sortOrder) { iSortOrder = sortOrder; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PositionType)) return false;
		if (getUniqueId() == null || ((PositionType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PositionType)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "PositionType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "PositionType[" +
			"\n	Label: " + getLabel() +
			"\n	Reference: " + getReference() +
			"\n	SortOrder: " + getSortOrder() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
