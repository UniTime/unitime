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

import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseManagerRole implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Boolean iPrimary;
	private Boolean iReceiveEmails;

	private Roles iRole;
	private TimetableManager iTimetableManager;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_IS_PRIMARY = "primary";
	public static String PROP_RECEIVE_EMAILS = "receiveEmails";

	public BaseManagerRole() {
		initialize();
	}

	public BaseManagerRole(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Boolean isPrimary() { return iPrimary; }
	public Boolean getPrimary() { return iPrimary; }
	public void setPrimary(Boolean primary) { iPrimary = primary; }

	public Boolean isReceiveEmails() { return iReceiveEmails; }
	public Boolean getReceiveEmails() { return iReceiveEmails; }
	public void setReceiveEmails(Boolean receiveEmails) { iReceiveEmails = receiveEmails; }

	public Roles getRole() { return iRole; }
	public void setRole(Roles role) { iRole = role; }

	public TimetableManager getTimetableManager() { return iTimetableManager; }
	public void setTimetableManager(TimetableManager timetableManager) { iTimetableManager = timetableManager; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ManagerRole)) return false;
		if (getUniqueId() == null || ((ManagerRole)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ManagerRole)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ManagerRole["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ManagerRole[" +
			"\n	Primary: " + getPrimary() +
			"\n	ReceiveEmails: " + getReceiveEmails() +
			"\n	Role: " + getRole() +
			"\n	TimetableManager: " + getTimetableManager() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
