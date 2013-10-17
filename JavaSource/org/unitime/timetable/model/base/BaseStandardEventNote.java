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

import org.unitime.timetable.model.StandardEventNote;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public abstract class BaseStandardEventNote implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iReference;
	private String iNote;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_REFERENCE = "reference";
	public static String PROP_NOTE = "note";

	public BaseStandardEventNote() {
		initialize();
	}

	public BaseStandardEventNote(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getReference() { return iReference; }
	public void setReference(String reference) { iReference = reference; }

	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof StandardEventNote)) return false;
		if (getUniqueId() == null || ((StandardEventNote)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StandardEventNote)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
