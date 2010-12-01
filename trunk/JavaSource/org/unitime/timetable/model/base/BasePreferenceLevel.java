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

import org.unitime.timetable.model.PreferenceLevel;

public abstract class BasePreferenceLevel implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iPrefId;
	private String iPrefProlog;
	private String iPrefName;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_PREF_ID = "prefId";
	public static String PROP_PREF_PROLOG = "prefProlog";
	public static String PROP_PREF_NAME = "prefName";

	public BasePreferenceLevel() {
		initialize();
	}

	public BasePreferenceLevel(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getPrefId() { return iPrefId; }
	public void setPrefId(Integer prefId) { iPrefId = prefId; }

	public String getPrefProlog() { return iPrefProlog; }
	public void setPrefProlog(String prefProlog) { iPrefProlog = prefProlog; }

	public String getPrefName() { return iPrefName; }
	public void setPrefName(String prefName) { iPrefName = prefName; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PreferenceLevel)) return false;
		if (getUniqueId() == null || ((PreferenceLevel)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PreferenceLevel)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "PreferenceLevel["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PreferenceLevel[" +
			"\n	PrefId: " + getPrefId() +
			"\n	PrefName: " + getPrefName() +
			"\n	PrefProlog: " + getPrefProlog() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
