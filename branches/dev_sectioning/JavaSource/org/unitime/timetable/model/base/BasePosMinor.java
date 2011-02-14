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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;

public abstract class BasePosMinor implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iCode;
	private String iName;

	private Session iSession;
	private Set<AcademicArea> iAcademicAreas;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_CODE = "code";
	public static String PROP_NAME = "name";

	public BasePosMinor() {
		initialize();
	}

	public BasePosMinor(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getCode() { return iCode; }
	public void setCode(String code) { iCode = code; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<AcademicArea> getAcademicAreas() { return iAcademicAreas; }
	public void setAcademicAreas(Set<AcademicArea> academicAreas) { iAcademicAreas = academicAreas; }
	public void addToacademicAreas(AcademicArea academicArea) {
		if (iAcademicAreas == null) iAcademicAreas = new HashSet<AcademicArea>();
		iAcademicAreas.add(academicArea);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PosMinor)) return false;
		if (getUniqueId() == null || ((PosMinor)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PosMinor)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "PosMinor["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "PosMinor[" +
			"\n	Code: " + getCode() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Name: " + getName() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
