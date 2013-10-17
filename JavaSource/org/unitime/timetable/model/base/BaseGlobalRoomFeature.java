/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public abstract class BaseGlobalRoomFeature extends RoomFeature implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iSisReference;
	private String iSisValue;

	private Session iSession;

	public static String PROP_SIS_REFERENCE = "sisReference";
	public static String PROP_SIS_VALUE = "sisValue";

	public BaseGlobalRoomFeature() {
		initialize();
	}

	public BaseGlobalRoomFeature(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getSisReference() { return iSisReference; }
	public void setSisReference(String sisReference) { iSisReference = sisReference; }

	public String getSisValue() { return iSisValue; }
	public void setSisValue(String sisValue) { iSisValue = sisValue; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof GlobalRoomFeature)) return false;
		if (getUniqueId() == null || ((GlobalRoomFeature)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((GlobalRoomFeature)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "GlobalRoomFeature["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "GlobalRoomFeature[" +
			"\n	Abbv: " + getAbbv() +
			"\n	FeatureType: " + getFeatureType() +
			"\n	Label: " + getLabel() +
			"\n	Session: " + getSession() +
			"\n	SisReference: " + getSisReference() +
			"\n	SisValue: " + getSisValue() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
