/*
 * UniTime 4.0 (University Timetabling Application)
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
package org.unitime.timetable.gwt.server;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;

public class UniTimePrincipal implements Principal, Serializable {
	private static final long serialVersionUID = 1L;

	private String iExternalId;
	private String iName;
	private Long iManagerId = null;
	private HashMap<Long, Long> iStudentId = new HashMap<Long, Long>();
	
	public UniTimePrincipal(String externalId, String name) {
		if (externalId == null) throw new NullPointerException();
		iExternalId = externalId;
		iName = name;
	}
	
	public String getExternalId() { return iExternalId; }
	public void setExternalId(String externalId) { iExternalId = externalId; }
	
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	
	public boolean isManager() { return iManagerId != null; }
	public Long getManagerId() { return iManagerId; }
	public void setManagerId(Long managerId) { iManagerId = managerId; }
	
	public Long getStudentId(Long sessionId) { return iStudentId.get(sessionId); }
	public void addStudentId(Long sessionId, Long studentId) { iStudentId.put(sessionId, studentId); }
	
	public int hashCode() { return iExternalId.hashCode(); }
	public boolean equals(Object o) {
		if (o == null || !(o instanceof UniTimePrincipal)) return false;
		return getExternalId().equals(((UniTimePrincipal)o).getExternalId());
	}
	public String toString() { return "UniTimePrincipal{id:" + getExternalId() + ", name:" + getName() + "}"; }	
}
