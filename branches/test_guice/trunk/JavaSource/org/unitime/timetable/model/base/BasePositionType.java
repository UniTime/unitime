/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.RefTableEntry;

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
