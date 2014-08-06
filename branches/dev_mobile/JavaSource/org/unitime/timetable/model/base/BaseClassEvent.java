/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Event;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseClassEvent extends Event implements Serializable {
	private static final long serialVersionUID = 1L;

	private Class_ iClazz;


	public BaseClassEvent() {
		initialize();
	}

	public BaseClassEvent(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Class_ getClazz() { return iClazz; }
	public void setClazz(Class_ clazz) { iClazz = clazz; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ClassEvent)) return false;
		if (getUniqueId() == null || ((ClassEvent)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ClassEvent)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ClassEvent["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ClassEvent[" +
			"\n	Clazz: " + getClazz() +
			"\n	Email: " + getEmail() +
			"\n	EventName: " + getEventName() +
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	MainContact: " + getMainContact() +
			"\n	MaxCapacity: " + getMaxCapacity() +
			"\n	MinCapacity: " + getMinCapacity() +
			"\n	SponsoringOrganization: " + getSponsoringOrganization() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
