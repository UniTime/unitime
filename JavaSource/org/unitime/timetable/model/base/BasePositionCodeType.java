/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.PositionCodeType;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Staff;

public abstract class BasePositionCodeType implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iPositionCode;

	private PositionType iPositionType;
	private Set<Staff> iStaff;

	public static String PROP_POSITION_CODE = "positionCode";

	public BasePositionCodeType() {
		initialize();
	}

	public BasePositionCodeType(String positionCode) {
		setPositionCode(positionCode);
		initialize();
	}

	protected void initialize() {}

	public String getPositionCode() { return iPositionCode; }
	public void setPositionCode(String positionCode) { iPositionCode = positionCode; }

	public PositionType getPositionType() { return iPositionType; }
	public void setPositionType(PositionType positionType) { iPositionType = positionType; }

	public Set<Staff> getStaff() { return iStaff; }
	public void setStaff(Set<Staff> staff) { iStaff = staff; }
	public void addTostaff(Staff staff) {
		if (iStaff == null) iStaff = new HashSet<Staff>();
		iStaff.add(staff);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PositionCodeType)) return false;
		if (getPositionCode() == null || ((PositionCodeType)o).getPositionCode() == null) return false;
		return getPositionCode().equals(((PositionCodeType)o).getPositionCode());
	}

	public int hashCode() {
		if (getPositionCode() == null) return super.hashCode();
		return getPositionCode().hashCode();
	}

	public String toString() {
		return "PositionCodeType["+getPositionCode()+"]";
	}

	public String toDebugString() {
		return "PositionCodeType[" +
			"\n	PositionCode: " + getPositionCode() +
			"\n	PositionType: " + getPositionType() +
			"]";
	}
}
