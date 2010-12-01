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

import org.unitime.timetable.model.CourseHistory;
import org.unitime.timetable.model.History;

public abstract class BaseCourseHistory extends History implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iOldNumber;
	private String iNewNumber;


	public static String PROP_OLD_NUMBER = "oldNumber";
	public static String PROP_NEW_NUMBER = "newNumber";

	public BaseCourseHistory() {
		initialize();
	}

	public BaseCourseHistory(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getOldNumber() { return iOldNumber; }
	public void setOldNumber(String oldNumber) { iOldNumber = oldNumber; }

	public String getNewNumber() { return iNewNumber; }
	public void setNewNumber(String newNumber) { iNewNumber = newNumber; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseHistory)) return false;
		if (getUniqueId() == null || ((CourseHistory)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseHistory)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CourseHistory["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseHistory[" +
			"\n	NewNumber: " + getNewNumber() +
			"\n	NewValue: " + getNewValue() +
			"\n	OldNumber: " + getOldNumber() +
			"\n	OldValue: " + getOldValue() +
			"\n	Session: " + getSession() +
			"\n	SessionId: " + getSessionId() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
